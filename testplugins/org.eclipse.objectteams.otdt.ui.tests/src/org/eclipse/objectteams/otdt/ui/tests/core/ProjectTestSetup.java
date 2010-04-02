/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ProjectTestSetup.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.core;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jdt.testplugin.TestOptions;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;

//{OT_COPY_PASTE: Copy of class org.eclipse.jdt.ui.tests.core.ProjectTestSetup
/**
 * Sets up an 1.5 project with rtstubs15.jar and compiler, code formatting, code generation, and template options.
 */
public class ProjectTestSetup extends TestSetup {

	public static final String PROJECT_NAME= "TestSetupProject";

	public static IJavaProject getProject() {
		IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		return JavaCore.create(project);
	}

	public static IClasspathEntry[] getDefaultClasspath() throws CoreException {
		IPath[] rtJarPath= JavaProjectHelper.findRtJar(JavaProjectHelper.RT_STUBS_15);
//{ObjectTeams: OTRE:
		IPath otrePath = new Path("OTRE");
/* orig:
		return new IClasspathEntry[] {  JavaCore.newLibraryEntry(rtJarPath[0], rtJarPath[1], rtJarPath[2], true) };
  :giro */
		return new IClasspathEntry[] {  JavaCore.newLibraryEntry(rtJarPath[0], rtJarPath[1], rtJarPath[2], true), JavaCore.newContainerEntry(otrePath) };
// SH}
	}


	private IJavaProject fJProject;

	private boolean fAutobuilding;

	public ProjectTestSetup(Test test) {
		super(test);
	}

	/* (non-Javadoc)
	 * @see junit.extensions.TestSetup#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		IJavaProject project= getProject();
		if (project.exists()) { // allow nesting of ProjectTestSetups
			return;
		}

		fAutobuilding = CoreUtility.setAutoBuilding(false);
//{ObjectTeams: create an OT project and keep otre.jar (don't overwrite with setRawClasspath)
/* orig: 
		fJProject= JavaProjectHelper.createJavaProject(PROJECT_NAME, "bin");
		fJProject.setRawClasspath(getDefaultClasspath(), null);
  :giro */
        fJProject = org.eclipse.objectteams.otdt.ui.tests.util.JavaProjectHelper.createOTJavaProject(PROJECT_NAME, "bin");
        JavaProjectHelper.addRTJar(fJProject);        
//gbr}

		TestOptions.initializeProjectOptions(fJProject);

		JavaCore.setOptions(TestOptions.getDefaultOptions());
		TestOptions.initializeCodeGenerationOptions();
		JavaPlugin.getDefault().getCodeTemplateStore().load();
	}

	protected void tearDown() throws Exception {
		if (fJProject != null) {
			JavaProjectHelper.delete(fJProject);
			CoreUtility.setAutoBuilding(fAutobuilding);
		}
	}

}
//gbr}