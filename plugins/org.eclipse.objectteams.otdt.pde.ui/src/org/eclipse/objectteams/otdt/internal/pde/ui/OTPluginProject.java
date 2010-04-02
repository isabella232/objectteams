/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTPluginProject.java 23470 2010-02-05 19:13:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.pde.ui;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;

public class OTPluginProject
{
	public static void makeOTPlugin(IProject project) throws CoreException
	{
		addOTNatureAndBuilder(project);
		OTREContainer.initializeOTJProject(project);
		// require base-imports for base classes per default:
		IJavaProject javaProject = JavaCore.create(project);
		String value = javaProject.getOption(OTDTPlugin.OT_COMPILER_BINDING_CONVENTIONS, true);
		if (!value.equals(JavaCore.ERROR))
			javaProject.setOption(OTDTPlugin.OT_COMPILER_BINDING_CONVENTIONS, JavaCore.ERROR);
	}

	public static void addOTNatureAndBuilder(IProject project) throws CoreException
	{
		IProjectDescription prjDesc = project.getDescription();
		prjDesc.setNatureIds(OTDTPlugin.createProjectNatures(prjDesc));
		ICommand[] buildSpecs = prjDesc.getBuildSpec();
		prjDesc.setBuildSpec(replaceOrAddOTBuilder(prjDesc, buildSpecs));
		project.setDescription(prjDesc, null);
	}

	private static ICommand[] replaceOrAddOTBuilder(IProjectDescription prjDesc, ICommand[] buildSpecs) 
	{
		ICommand otBuildCmd = OTDTPlugin.createProjectBuildCommand(prjDesc);
		// replace existing Java builder?
		for(int i=0; i<buildSpecs.length; i++) {
			if (buildSpecs[i].getBuilderName().equals(JavaCore.BUILDER_ID)) {
				buildSpecs[i] = otBuildCmd;
				return buildSpecs;
			}
		}
		// not found, add to front:
		int len = buildSpecs.length;
		System.arraycopy(buildSpecs, 0, buildSpecs = new ICommand[len+1], 1, len);
		buildSpecs[0] = otBuildCmd;
		return buildSpecs;
	}
}
