/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2015 GK Software AG.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 *		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otredyn.bytecode.asm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handle files specified via system property {@code ot.weavable}.
 * Each line in such file is interpreted as a prefix for fully qualified class names (dot-separated).
 * Classes matching to this prefix are considered weavable, others will never be passed to the transformer.
 */
public class WeavableRegionReader {
	
	/** Initialized from property {@code ot.weavable}. */
	private final static String WEAVABLE_FILE = System.getProperty("ot.weavable");

	/**	Marker for comment lines in the "weavable" file. */    
	private final static String COMMENT_MARKER = "#";
	 
	private static List<String> weavablePrefixes = null;

	public static boolean isWeavable(String className) {
		ensureInitialized();
		if (weavablePrefixes.isEmpty())
			return true; // not filtering
		for (String prefix: weavablePrefixes)
			if (className.startsWith(prefix))
				return true;
		return false;
	}
	
	private static synchronized void ensureInitialized() {
		if (weavablePrefixes == null)
			weavablePrefixes = readWeavablePrefixes();
	}
	
	/**
	 * @return a list of prefixes of class names
	 */
	private static List<String> readWeavablePrefixes() {
		if (WEAVABLE_FILE == null)
			return Collections.emptyList();
		List<String> result = new ArrayList<String>();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(WEAVABLE_FILE)))) {
 			while (in.ready()) {
				String nextLine = in.readLine();
				String nextTeam = nextLine.trim();
				if (nextTeam.startsWith(COMMENT_MARKER))
					continue; // this is a comment line
				if (!nextTeam.equals("")) {
					result.add(nextTeam.trim());
				}
			}
		} catch (Exception e) {
			System.err.println("File input error: weavable file '" + WEAVABLE_FILE + "' can not be found!");
		}
		return result;
	}
}
