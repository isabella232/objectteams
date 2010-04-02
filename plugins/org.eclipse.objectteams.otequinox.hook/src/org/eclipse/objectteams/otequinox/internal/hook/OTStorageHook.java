/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007, 2009 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal.hook;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.internal.adaptor.EclipseStorageHook;
import org.eclipse.objectteams.otequinox.hook.AspectPermission;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.hooks.StorageHook;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.eclipse.osgi.framework.util.Headers;
import org.eclipse.osgi.framework.util.KeyedElement;
import org.osgi.framework.BundleException;

/**
 * This hook essentially intercepts calls to getManifest() as to insert directives
 * from otequinox.forced.exports (as read from config.ini or other locations).
 *  
 * @author stephan
 * @since 1.1.4
 */
public class OTStorageHook implements StorageHook 
{
	// property names:
	private static final String FORCED_EXPORTS_DENIED  = "otequinox.forced.exports.denied"; // //$NON-NLS-1$
	private static final String FORCED_EXPORTS_GRANTED = "otequinox.forced.exports.granted"; // //$NON-NLS-1$
	// legacy:
	private static final String FORCED_EXPORTS = "otequinox.forced.exports"; // //$NON-NLS-1$
	
	// terminal tokens:
	private static final String EXPORT_PACKAGE = "Export-Package"; //$NON-NLS-1$
	private static final String XFRIENDS = "x-friends:="; //$NON-NLS-1$

	private static final String KEY = OTStorageHook.class.getName();
	private static final int HASHCODE = KEY.hashCode();
	private static final int STORAGE_VERSION = 1;
	
	
	private static HashMap<String, String> grantedForcedExports = null;
	private static HashMap<String, String> deniedForcedExports = null;
	
	private static void readForcedExports() {
		// read forced exports from config.ini (system properties), or file(s)
		deniedForcedExports= new HashMap<String, String>();
		grantedForcedExports= new HashMap<String, String>();
		readForcedExportsFromProperty(FORCED_EXPORTS_DENIED, AspectPermission.DENY);
		readForcedExportsFromProperty(FORCED_EXPORTS_GRANTED, AspectPermission.GRANT);
		// legacy:
		readForcedExportsFromProperty(FORCED_EXPORTS, AspectPermission.GRANT);
	}

	private static void readForcedExportsFromProperty(String propKey, AspectPermission perm) {
		String value= System.getProperty(propKey);
		if (value != null) {
			if (value.length() > 0 && value.charAt(0) == '@') {
				// follows: comma-separated list for filenames to read from:
				int pos = 1;
				while (pos < value.length()) {
					int comma = value.indexOf(',', pos);
					String fileName;
					if (comma > -1) {
						fileName = value.substring(pos, comma);
						pos = comma+1;
					} else {
						fileName = value.substring(pos);
						pos = value.length();
					}
					parseForcedExportsFile(new File(fileName), perm);
				}
			} else {
				parseForcedExports(value, perm);
			}
		}
	}

	/**
	 * Adds the definitions from a given file to the set of granted forced exports.
	 * @param file file to read from
	 * @param perm either ALLOW or DENY, determines how the found forced export decls are interpreted.
	 */
	synchronized public static void parseForcedExportsFile(File file, AspectPermission perm) {
		StringBuffer newVal = new StringBuffer();
		try {
			// read content of one file:
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF8")); //$NON-NLS-1$
			String line;
			boolean afterClosingBrace = false;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					// may need to replace linebreak after ']' with ',' - to match one-line syntax in config.ini
					char lastReadChar = line.charAt(line.length()-1);
					if (afterClosingBrace && lastReadChar != ',')
						newVal.append(',');
					afterClosingBrace = lastReadChar == ']';
						
					newVal.append(line);
				}
			}
			parseForcedExports(newVal.toString(), perm);
		} catch (IOException e) {
			fwLog.log(new FrameworkLogEntry(OTStorageHook.class.getName(),
						    "failed to read forcedExports file "+file.getName(), //$NON-NLS-1$
						    FrameworkLogEntry.ERROR, e, null));
		}							
	}
	/* Adds the given definitions to the set of granted forced exports. */
	private static void parseForcedExports(String value, AspectPermission perm) {
		HashMap<String, String> map = getForcedExportsMap(perm);
		if (map == null) return; // DONT_CARE
		int pos = 0;
		String[] values = new String[2]; // { BaseBundleID , PackageExports }
		while (true) {
			pos = getForcedExportForOneBase(value, pos, values);
			String plugin= values[0];
			String pack= values[1];
			if (map.containsKey(plugin)) {
				String oldPack = map.get(plugin);
				pack = oldPack+','+pack;  // append to existing definition
			}
			map.put(plugin, pack);
			if (pos >= value.length())
				break; // eot: done without errors
			if (value.charAt(pos) != ',')
				throwSyntaxError(value, pos, "missing ','"); //$NON-NLS-1$
			pos++; // skip the ','
		}
	}
	/**
	 * fetch one chunk of forced exports specification from `spec`:
	 * <ul>
	 * <li>my.base.plugin[<em>specification-of-force-exported-packages</em>]<em>tail</em></li>
	 * </ul>
	 * is split into:
	 * <ul>
	 * <li>my.base.plugin</li>
	 * <li><em>specification-of-force-exported-packages</em></li>
	 * </ul>
	 * and return value points to <em>tail</em>
	 * @param spec where to read from
	 * @param start where to start reading (within spec)
	 * @param value store result chunks in this array: [0] = base plugin, [1] = packages specification
	 * @return position after what has been interpreted so far
	 */
	private static int getForcedExportForOneBase(String spec, int start, String[] values) {
		int open = spec.indexOf('[', start);
		if (open == -1)
			throwSyntaxError(spec, start, "missing '['"); //$NON-NLS-1$
		int close = spec.indexOf(']', start);
		if (close == -1)
			throwSyntaxError(spec, open, "missing ']'"); //$NON-NLS-1$
		values[0] = spec.substring(start, open);
		values[1] = spec.substring(open+1, close);
		return close+1;
	}

	private static void throwSyntaxError(String spec, int pos, String string) {
		throw new RuntimeException("Illegal syntax in "+FORCED_EXPORTS+" directive at position "+pos+" (not counting whitespace): "+string+"\n value is:\n"+spec); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$		
	}

	private static HashMap<String, String> getForcedExportsMap(AspectPermission perm) {
		switch (perm) {
			case GRANT:     return grantedForcedExports;
			case DENY:      return deniedForcedExports;
			case UNDEFINED: return null; 
//			default: // not implementing a default case, want to be warned when new enum-constants are added
		}
		// for binary compatibility; see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=265744
		throw new IncompatibleClassChangeError("enum "+AspectPermission.class.getName()+" has changed unexpectedly."); //$NON-NLS-1$ //$NON-NLS-2$
	}

	static FrameworkLog fwLog;
	
	BaseData bundleData;
	EclipseStorageHook manifestProvider;	
	
	public OTStorageHook(BaseData bundleData, EclipseStorageHook manifestProvider) {
		this.bundleData = bundleData;
		this.manifestProvider = manifestProvider;
		if (fwLog == null && bundleData != null)
			fwLog = bundleData.getAdaptor().getFrameworkLog();
	}
	
	public void copy(StorageHook storageHook) {
		OTStorageHook orig= (OTStorageHook)storageHook;
		this.bundleData= orig.bundleData;
		this.manifestProvider= orig.manifestProvider;
	}

	public StorageHook create(BaseData bundledata) throws BundleException {
		return new OTStorageHook(bundledata, this.manifestProvider);
	}

	public boolean forgetStartLevelChange(int startlevel) {
		return false;
	}

	public boolean forgetStatusChange(int status) {
		return false;
	}

	public Dictionary<?,?> getManifest(boolean firstLoad) throws BundleException 
	{
		final Dictionary<?,?> orig= this.manifestProvider.create(this.bundleData).getManifest(firstLoad);
		
		// wrap the original answer:
		return new Headers(orig.size()) 
		{
			@Override
			public Object get(Object key) {
				Object value = orig.get(key);
				if (!EXPORT_PACKAGE.equals(key)) 
					return value;
				synchronized (OTStorageHook.class){
					if (grantedForcedExports == null)
						readForcedExports();
				}
				String exports= grantedForcedExports.get(OTStorageHook.this.bundleData.getSymbolicName());
				if (exports != null) {
					// yes, we need to add forced exports:
					String packages= (String)orig.get(EXPORT_PACKAGE);
					if (!exports.contains(XFRIENDS)) {
						// invalid directive:
						grantedForcedExports.remove(OTStorageHook.this.bundleData.getSymbolicName()); 
						logError("config.ini: missing x-friends directive in forced export of "+exports); //$NON-NLS-1$
						return packages; // don't install illegal forced exports
					}
					if (packages != null)
						return packages+','+exports;
					return exports;
				}
				return value;
			}
			// other methods simply delegate:
			@Override public synchronized int size()             { return orig.size(); }
			@Override public synchronized Enumeration<?> keys()  { return orig.keys(); }
		};
	}

	static void logError(String message) {
		fwLog.log(new FrameworkLogEntry(OTStorageHook.class.getName(),
									    message,
									    FrameworkLogEntry.ERROR,
									    null, null));
	}

	public int getStorageVersion() {
		return STORAGE_VERSION;
	}

	@SuppressWarnings("unchecked")
	public void initialize(final Dictionary manifest) throws BundleException {
		// no-op
	}

	public StorageHook load(BaseData bundledata, DataInputStream is)
			throws IOException 
	{
		return new OTStorageHook(bundledata, this.manifestProvider);
	}

	public boolean matchDNChain(String pattern) {
		return false;
	}

	public void save(DataOutputStream os) throws IOException {
		// no-op
	}

	public void validate() throws IllegalArgumentException {
		// no-op
	}

	public Object getKey() {
		return KEY;
	}
	
	public boolean compare(KeyedElement other) {
		return other.getKey() == KEY;
	}

	public int getKeyHashCode() {
		return HASHCODE;
	}

	/** 
	 * Query whether any forced-exports are declared in config.ini (or other location) 
	 * for the given aspect bundle.
	 * @param aspectBundleId
	 * @return list of pairs (baseBundleId x packageName)
	 */ 
	public static ArrayList<String[]> getForcedExportsByAspect(String aspectBundleId, AspectPermission perm) 
	{
		// can be queried before we had a chance to initialize our data structures
		synchronized (OTStorageHook.class) {
			if (grantedForcedExports == null)
				readForcedExports();
		}

		ArrayList<String[]> result= new ArrayList<String[]>(5);
		
		HashMap<String, String> map = getForcedExportsMap(perm);
		if (map == null) 
			return result; // DONT_CARE: useless query.
		
		for (Map.Entry<String,String> entry: map.entrySet()) {
			String export= entry.getValue();
			int start = 0;
			while (start >= 0 && start < export.length()) {
				if (start > 0) {
					// skip separator after previous entry
					if (export.charAt(start) == ',')
						start++;
					else
						logError("Error parsing forced exports: "+export+", comma expected at position "+start); //$NON-NLS-1$ //$NON-NLS-2$
				}
				int pos= export.indexOf(';'+XFRIENDS, start);
				if (pos == -1)
					break;
				String packageName = export.substring(start, pos);
				List<String> aspectBundles = new ArrayList<String>(); 
				start = scanAspectBundles(export, pos+XFRIENDS.length()+1, aspectBundles);
				for (String aspect : aspectBundles) {
					if (aspect.equals(aspectBundleId)) {
						result.add(new String[]{entry.getKey(), packageName});
					}
				}
			}
		}
		return result;
	}

	private static int scanAspectBundles(String export, int pos, List<String> result) {
		String termChars = ",]"; //$NON-NLS-1$
		if (export.charAt(pos) == '"') {
			pos++;
			termChars = "\""; //$NON-NLS-1$
		}
		int start = pos;
		while (pos < export.length()) {
			char c = export.charAt(pos);
			switch (c) {
			case ',':
			case ']':
			case '"':
				String next = export.substring(start, pos);
				result.add(next);
				start = pos+1;
				if (termChars.indexOf(c) != -1)
					return start;
				break;
			}
			pos++;
		}
		logError("Unterminated forced exports: "+export); //$NON-NLS-1$
		if (pos > start)
			result.add(export.substring(start));
		return export.length();
	}

}
