/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
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
package org.eclipse.objectteams.otdt.internal.pde.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.objectteams.otdt.core.ext.OTJavaNature;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;

import base org.eclipse.pde.internal.core.ClasspathComputer;

/**
 * This team handles classpath updating issues
 * @author mosconi
 * @since 1.2.4
 */
@SuppressWarnings("restriction")
public team class ClasspathComputerAdapter {
	protected class ClasspathComputer playedBy ClasspathComputer {
		
		// re-add OTRE container for OT plug-in projects:
		static void updateOTClasspath(IProject project) throws CoreException {
			OTREContainer.initializeOTJProject(project);
			sortClasspathEntries(project);
		}
		void updateOTClasspath(IProject project) <- after void setClasspath(IProject project, IPluginModelBase model)
			when (OTJavaNature.hasOTJavaNature(project));		
	}
	
	/** 
	 * Make sure OTRE comes before requiredPlugins on the project's classpath.
	 * This is needed to ensure that org.objectteams.Team is accessible.
	 * When a project finds a dependent plugin which depends on otequinox,
	 * Team will be found via that project leading to an error because Team is
	 * not re-exported from an OT plugin project.
	 * @throws CoreException 
	 */
	public static void sortClasspathEntries(IProject project) throws CoreException {
		IJavaProject javaProject= (IJavaProject) project.getNature(JavaCore.NATURE_ID);
		IClasspathEntry[] entries= javaProject.getRawClasspath();
		IClasspathEntry[] newEntries= new IClasspathEntry[entries.length];
		IClasspathEntry requiredPlugins= null;
		int newOTREPos= -1;
		int j=0; // index into newEntries
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				IPath containerPath= entries[i].getPath();
				if (containerPath.equals(PDECore.REQUIRED_PLUGINS_CONTAINER_PATH)) {
					requiredPlugins= entries[i];// store intermediate
					newOTREPos= j++; // leave empty slot in newEntries
					continue;
				} else if (containerPath.segment(0).equals(OTREContainer.OTRE_CONTAINER_NAME)) {
					if (newOTREPos>-1)
						newEntries[newOTREPos]= entries[i];
					else 
						return; // requiredPlugins was not found before OTRE
					continue;
				}
			}
			newEntries[j++]= entries[i];
		}
		if (newOTREPos > -1 && requiredPlugins != null && j<newEntries.length) {
			newEntries[j]= requiredPlugins;
			javaProject.setRawClasspath(newEntries, new NullProgressMonitor());
		}
	}

}
