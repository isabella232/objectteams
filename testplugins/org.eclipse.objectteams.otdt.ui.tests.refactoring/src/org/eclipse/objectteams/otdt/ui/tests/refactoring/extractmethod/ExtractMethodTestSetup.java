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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring.extractmethod;

import java.util.Hashtable;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.objectteams.otdt.ui.tests.util.JavaProjectHelper;
import org.eclipse.objectteams.otdt.ui.tests.util.TestOptions;

/**
 * @author brcan
 */
@SuppressWarnings({ "nls", "restriction" })
public class ExtractMethodTestSetup extends TestSetup
{
	private IJavaProject _javaProject;
	private IPackageFragmentRoot _root;
	private static final String CONTAINER = "src";

//	private IPackageFragment _selectionPackage;
	
    private IPackageFragment _teamClassPackage;
    private IPackageFragment _roleClassPackage;
	private IPackageFragment _nestedTeamPackage;
	private IPackageFragment _rolefilePackage;
	private IPackageFragment _rolefileParentPackage;
	private IPackageFragment _focusTypePackage;
	private IPackageFragment _overloadingPackage;
	
    public ExtractMethodTestSetup(Test test)
    {
        super(test);
    }
    
	public IPackageFragmentRoot getRoot() {
		return _root;
	}

    @SuppressWarnings("unchecked")
	protected void setUp() throws Exception
    {
		Hashtable options = TestOptions.getFormatterOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "0");
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		JavaCore.setOptions(options);
		TestOptions.initializeCodeGenerationOptions();
		JavaPlugin.getDefault().getCodeTemplateStore().load();		
		
		_javaProject = JavaProjectHelper.createOTJavaProject("TestProject", "bin");
		JavaProjectHelper.addRTJar(_javaProject);
		_root = JavaProjectHelper.addSourceContainer(_javaProject, CONTAINER);
		
		RefactoringCore.getUndoManager().flush();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		description.setAutoBuilding(false);
		workspace.setDescription(description);

		//new packages for OT/J tests
		_teamClassPackage = getRoot().createPackageFragment("team_in", true, null);
		_roleClassPackage = getRoot().createPackageFragment("role_in", true, null);
		_rolefilePackage       = getRoot().createPackageFragment("roleFile_in.TeamWithRoleFile", true, null);
		_rolefileParentPackage = getRoot().createPackageFragment("roleFile_in", true, null);
		_nestedTeamPackage = getRoot().createPackageFragment("nestedTeam_in", true, null);
		_focusTypePackage = getRoot().createPackageFragment("focusType_in", true, null);
		_overloadingPackage = getRoot().createPackageFragment("overloading_in", true, null);
		
//		_selectionPackage = getRoot().createPackageFragment("selection", true, null);
    }

	protected void tearDown() throws Exception
	{
		RefactoringTest.performDummySearch(_javaProject);
		JavaProjectHelper.delete(_javaProject);
	}
	
//	public IPackageFragment getSelectionPackage()
//	{
//	    return _selectionPackage;
//	}

	public IPackageFragment getTeamClassPackage()
	{
	    return _teamClassPackage;
	}

	public IPackageFragment getRoleClassPackage()
	{
	    return _roleClassPackage;
	}
	
	public IPackageFragment getNestedTeamPackage()
	{
	    return _nestedTeamPackage;
	}

	public IPackageFragment getRoleFilePackage()
	{
	    return _rolefilePackage;
	}

	public IPackageFragment getRoleFileParentPackage()
	{
	    return _rolefileParentPackage;
	}
	
	public IPackageFragment getFocusTypePackage()
	{
	    return _focusTypePackage;
	}
	
	public IPackageFragment getOverloadingPackage()
	{
	    return _overloadingPackage;
	}
}
