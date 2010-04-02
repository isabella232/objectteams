/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AspectBindingReader.java 23451 2010-02-04 20:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.compiler.adaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;
import org.eclipse.pde.core.build.IBuild;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ClasspathUtilCore;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.RequiredPluginsClasspathContainer;
import org.objectteams.LiftingVetoException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Utility to read the aspectBinding extension from a projects plugin.xml,
 * in order to provide this information during compilation.
 * 
 * References to an AspectBindingReader are passed along the full information flow
 * from PDEAdaptor to BaseImportChecker as a source for fetching aspect binding information.
 * 
 * @author stephan
 * @since 1.1.5
 */
@SuppressWarnings("restriction")
public class AspectBindingReader {

	// XML-Structure of aspectBindings in plugin.xml:
	private final static String ASPECT_BINDING       = "aspectBinding";     //$NON-NLS-1$
	private final static String    BASE_PLUGIN       = "basePlugin";        //$NON-NLS-1$
	private final static String       ID             = "id"; //attribute    //$NON-NLS-1$
	private final static String       FORCED_EXPORTS = "forcedExports";     //$NON-NLS-1$
	private final static String    TEAM              = "team";              //$NON-NLS-1$
	private final static String       CLASS          = "class"; //attribute //$NON-NLS-1$
	
	private static final String SELF                 = "SELF";  //value     //$NON-NLS-1$
	// Note: we do NOT read the requiredFragment element, because when creating access rules
	//       the packages of the fragment will report the host bundle as their providingBundle.
	//       (see PDEAdaptor.RequiredPluginsClasspathContainer#updateRule())
	
	private static SAXParserFactory fSAXFactory;
	
	// == local cached storage: ==
	
	// main date storage: teamClassName -> basePluginName* 
	private HashMap<String, HashSet<String>> team2basePlugins = null;
	
	private HashMap<String, HashSet<String>> base2forcedExports = null;
	private HashSet<String> teamsAdaptingSelf= new HashSet<String>();
	
	// reverse info with more details: basePluginName -> AdaptedBaseBundle
	private HashMap<String, AdaptedBaseBundle> adaptationInfos= new HashMap<String, AdaptedBaseBundle>();
	
	private String project;
	private IProject iProject;
	
	/** This field serves as a time stamp to track whether an
	 * aspect binding reader has been reloaded. */
	Object token;

	private boolean hasChanges;
	
	// for accessing objects of type IPluginModelBase:
	PluginModelManager fPluginModelManager;

	/**
	 * Create and initialize an AspectBindingReader, i.e., try to read aspect binding info from the project's plugin.xml.
	 * 
	 * @param project the project whose aspect bindings should be analyzed.
	 * @throws LiftingVetoException  if the project was not ready for reading plugin.xml.
	 */
	public AspectBindingReader(IProject project) throws LiftingVetoException {
		if (!this.readAspectBindings(project, getSaxParserFactory()))
			throw new LiftingVetoException();
		this.project= project.getName();
		this.iProject= project;
		this.fPluginModelManager = PDECore.getDefault().getModelManager();
	}

	/** Is the given team declared to adapt classes from its own plug-in ("self")? */
	public boolean isAdaptingSelf(String teamName) {
		return this.teamsAdaptingSelf.contains(teamName);
	}

	/** Get the base plug-in adapted by the given team. */
	public Set<String> getBasePlugins(String teamName) {
		if (this.team2basePlugins != null)
			return this.team2basePlugins.get(teamName);
		return null;
	}
	
	/** Is the bundle identified by this symbolic name an adapted base bundle.? */
	public boolean isAdaptedBase(String symbolicName) {
		return this.adaptationInfos.containsKey(symbolicName);
	}
	
	/** Get the names of all teams that adapt the given base bundle. */
	public Set<String> getTeamsForBase(String basePluginName) {
		AdaptedBaseBundle info = this.adaptationInfos.get(basePluginName);
		if (info == null) 
			return new HashSet<String>();
		return info.adaptingTeams;
	}

	/** Get (lazily create) the detailed info for an adapted base bundle. */
	public AdaptedBaseBundle getAdaptationInfo(String basePluginName) {
		AdaptedBaseBundle result= this.adaptationInfos.get(basePluginName);
		if (result == null) {
			result= new AdaptedBaseBundle(basePluginName, this);
			this.adaptationInfos.put(basePluginName, result);
		}
		return result;
	}


	/** Is the given package force-exported? */
	public String getForcedExportingBase(String packageName) {
		if (this.base2forcedExports == null)
			return null;
		for (Entry<String, HashSet<String>> entry: this.base2forcedExports.entrySet()) {
			if (entry.getValue().contains(packageName))
				return entry.getKey();
		}
		return null;
	}

	public HashSet<String> getForcedExports(String symbolicName) {
		if (this.base2forcedExports == null)
			return null;
		return this.base2forcedExports.get(symbolicName);
	}

	static SAXParserFactory getSaxParserFactory() {
		if (fSAXFactory == null) 
			fSAXFactory = SAXParserFactory.newInstance();
		return fSAXFactory;
	}
	

	/** Read all the <aspectBinding> declarations from plugin.xml. 
	 * @return whether or not reading plugin.xml was successful.
	 */ 
	private boolean readAspectBindings (IProject project, SAXParserFactory factory)  {
		SAXParser parser;
		try {
			parser = factory.newSAXParser();
			IFile file = project.getFile("plugin.xml"); //$NON-NLS-1$
			if (!file.exists())
				return false;
			collectAspectBindings(file, parser);
			this.token= new Object();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	/** Stage 1: remember which team declares to adapt classes from which base plug-in. */
	void recordAspectBinding(String teamClass, String basePluginID) {
		if (SELF.equals(basePluginID.toUpperCase())) {
			this.teamsAdaptingSelf.add(teamClass);
			return;
		}
		if (this.team2basePlugins == null)
			this.team2basePlugins = new HashMap<String, HashSet<String>>();
		HashSet<String> plugins = this.team2basePlugins.get(teamClass);
		if (plugins == null) {
			plugins = new HashSet<String>();
			this.team2basePlugins.put(teamClass, plugins);
		}
		plugins.add(basePluginID);
		
		// and store detailed reverse info:
		AdaptedBaseBundle adaptationInfo = getAdaptationInfo(basePluginID);
		adaptationInfo.adaptingTeams.add(teamClass);
	}

	void recordForcedExports(String basePlugin, String exports) {
		if (this.base2forcedExports == null) 
			this.base2forcedExports= new HashMap<String, HashSet<String>>();
		HashSet<String> baseExports= this.base2forcedExports.get(basePlugin);
		if (baseExports == null) 
			this.base2forcedExports.put(basePlugin, baseExports= new HashSet<String>());
		String[] singleExports= exports.split(","); //$NON-NLS-1$
		for (int i = 0; i < singleExports.length; i++)
			baseExports.add(singleExports[i].trim());
		// TODO(SH): not yet checked: are the exports really packages of basePlugin?
	}

	void collectAspectBindings(IFile file, SAXParser parser) {
		try {
			parser.parse(file.getContents(), new DefaultHandler() {
				String basePluginID = null;
				ArrayList<String> teamClasses = null;
				StringBuffer forcedExports= null;
				@Override
				public void startElement(String uri, String localName, String name, Attributes attributes) 
						throws SAXException 
				{
					if (name.equals(ASPECT_BINDING)) 
						this.teamClasses = new ArrayList<String>();
					else if (this.teamClasses != null) { // within an aspectBinding element?
						if (name.equals(BASE_PLUGIN)) {
							this.basePluginID = attributes.getValue(ID);
						} else if (name.equals(TEAM)) {                  
							String teamClass = attributes.getValue(CLASS);
							if (teamClass == null)
								throw new SAXException("team element lacking \"class\" attribute"); //$NON-NLS-1$
							this.teamClasses.add(teamClass);
						} else if (name.equals(FORCED_EXPORTS)) {
							this.forcedExports= new StringBuffer();
						}
					}
				}
				@Override
				public void characters(char[] ch, int start, int length) throws SAXException 
				{
					if (this.forcedExports != null)
						this.forcedExports.append(ch, start, length);
				}
				@Override
				public void endElement(String uri, String localName, String name)
						throws SAXException 
				{
					if (name.equals(ASPECT_BINDING)) {
						if (this.basePluginID == null)
							throw new SAXException("aspectBinding missing a \"basePlugin\" element"); //$NON-NLS-1$
						for (String	teamClass : this.teamClasses) 
							recordAspectBinding(teamClass, this.basePluginID);
						this.basePluginID = null;
						this.teamClasses = null;
					} else if (name.equals(FORCED_EXPORTS)) {
						if (this.forcedExports != null && this.forcedExports.length() > 0)
							recordForcedExports(this.basePluginID, this.forcedExports.toString());
						this.forcedExports= null;
					}
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	// ===== Below: Updating. =====

	/** When plugin.xml has changed re-read our data and perhaps clear
	 *  cached data in the JavaModelManager and RequirePluginsClasspathContainer. */
	void reload() {
		this.hasChanges= true;
		
		HashMap<String,HashSet<String>> oldForcedExports= this.base2forcedExports;
		HashMap<String,HashSet<String>> oldTeamBindings= this.team2basePlugins;
		// clear internal storage:
		this.team2basePlugins = null;   // be sure to initialize new sets if needed, so we can compare sets using mapHasChanged
		this.base2forcedExports = null;
		this.adaptationInfos.clear();   // these two are created by default (and no need to compare old/new)
		this.teamsAdaptingSelf.clear();
		
		if (!this.readAspectBindings(this.iProject, getSaxParserFactory())) {
			OTDTPlugin.getDefault().getLog().log(new Status(Status.ERROR, OTDTPlugin.PLUGIN_ID, "Unable to re-read plugin.xml!;")); //$NON-NLS-1$
			return ;
		}
		
		// remove cached data if forced exports or team bindings have changed:
		if (   mapHasChanged(oldForcedExports, this.base2forcedExports)
		    || mapHasChanged(oldTeamBindings,  this.team2basePlugins)) 
		{
			resetRequiredPluginsClasspathContainer(this.iProject);
		}
	}
	
	/** Destructively read the flag indicating changed aspect binding data. */
	boolean fetchHasChanges() {
		try {
			return this.hasChanges;
		} finally {
			this.hasChanges= false;
		}
	}
	
	private void resetRequiredPluginsClasspathContainer(IProject project) {
		IJavaProject jProject = JavaCore.create(project);
		IPluginModelBase model = fPluginModelManager.findModel(project);
		try {
			IBuild build = ClasspathUtilCore.getBuild(model);
			RequiredPluginsClasspathContainer container = new RequiredPluginsClasspathContainer(model, build);
			// this triggers recomputing the classpath:
			JavaCore.setClasspathContainer(PDECore.REQUIRED_PLUGINS_CONTAINER_PATH, new IJavaProject[]{jProject}, new IClasspathContainer[] {container}, null);
			// AspectBindingReader is automatically shared via the ResourceProjectAdaptor.OTEquinoxProject
			// see org.eclipse.objectteams.otdt.internal.compiler.adaptor.AdaptorActivator.JavaCore.setClasspathContainer(..)
		} catch (CoreException ce) {
			OTDTPlugin.getExceptionHandler().logException("Failed to reload classpath container for "+project, ce); //$NON-NLS-1$
		}
	}

	private <T> boolean mapHasChanged(HashMap<String,T> oldMap, 
									  HashMap<String,T> newMap) 
	{
		if (oldMap == null || newMap== null)
			return oldMap != newMap; // null and non-null?
		HashSet<String> newKeys= new HashSet<String>(newMap.keySet());
		for (Map.Entry<String, T> oldEntry : oldMap.entrySet()) {
			T newVal= newMap.get(oldEntry.getKey());
			if (newVal == null)
				return true;       // removed entry
			if (!newVal.equals(oldEntry.getValue()))
				return true;	   // changed value (simple or complex)
			newKeys.remove(oldEntry.getKey());
		}
		return !newKeys.isEmpty(); // added entries in newKeys?		
	}
	
	// ===== Debug: =====
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		String result= "AspectBindingReader for project "+this.project;
		if (this.team2basePlugins != null)
			result+= "\n\t known teams: "+this.team2basePlugins.size();
		if (this.teamsAdaptingSelf != null)
			result+= "\n\t self-adaption teams: "+this.teamsAdaptingSelf.size();
		if (this.base2forcedExports != null)
			result+= "\n\t plugins with forced exports: "+this.base2forcedExports.size();
		return result;
	}
}
