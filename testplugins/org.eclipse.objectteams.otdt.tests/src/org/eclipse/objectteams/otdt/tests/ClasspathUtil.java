/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 GK Software AG
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
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;

/** Constants for the classpath of OT/J Projects.*/
public class ClasspathUtil {

	// === OT Paths: ===
	public static final String OTRE_PATH     	 = new OTREContainer().getResolvedPathString();
	public static final String OTDT_PATH 		 = JavaCore.getClasspathVariable(OTDTPlugin.OTDT_INSTALLDIR).toOSString();
	public static final String OTRE_MIN_JAR_PATH = getOTDTJarPath("otre_min");
	public static final String OTAGENT_JAR_PATH  = getOTDTJarPath("otre_agent");
	public static final IPath  BCEL_JAR_PATH     = OTREContainer.BCEL_PATH;

	private static String getOTDTJarPath(String jarName) {
		return OTDT_PATH + File.separator + "lib" + File.separator + jarName + ".jar";
	}

}
