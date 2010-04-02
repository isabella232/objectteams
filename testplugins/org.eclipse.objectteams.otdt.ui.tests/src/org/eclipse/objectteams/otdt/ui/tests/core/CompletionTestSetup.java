/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CompletionTestSetup.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.core;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.ui.tests.core.ProjectTestSetup;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.testplugin.JavaProjectHelper;

/*
 * Copied from org.eclipse.jdt.text.tests (Version 3.4 as of Mar 07, 2008)
 */
class CompletionTestSetup extends ProjectTestSetup {

	// SH: added parameter
	public static IPackageFragment getTestPackage(String relativeName) throws CoreException {
		String packageName = "test1";
		if (relativeName != null)
			packageName += '.'+relativeName;
		return getAbsoluteTestPackage(packageName);
	}
	public static IPackageFragment getAbsoluteTestPackage(String packageName) throws CoreException {
		IJavaProject project= getProject();
		IPackageFragmentRoot root= project.getPackageFragmentRoot("src");
		if (!root.exists())
			root= JavaProjectHelper.addSourceContainer(project, "src");
		
		IPackageFragment fragment= root.getPackageFragment(packageName);
		if (!fragment.exists())
			fragment= root.createPackageFragment(packageName, false, null);
		
		return fragment;
	}
	
	private static int fAnonymousSoureFolderCounter= 0;
	public static IPackageFragment getAnonymousTestPackage() throws CoreException {
		IJavaProject project= getProject();
		String sourceFolder= "src" + fAnonymousSoureFolderCounter++;
		IPackageFragmentRoot root= project.getPackageFragmentRoot(sourceFolder);
		if (!root.exists())
			root= JavaProjectHelper.addSourceContainer(project, sourceFolder);
		
		IPackageFragment fragment= root.getPackageFragment("test1");
		if (!fragment.exists())
			fragment= root.createPackageFragment("test1", false, null);
		
		return fragment;
	}
	
	public CompletionTestSetup(Test test) {
		super(test);
	}
}