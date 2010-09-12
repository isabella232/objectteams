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

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.corext.refactoring.ParameterInfo;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;

/**
 * @author brcan
 * 
 */
@SuppressWarnings({ "nls", "restriction" })
public class ExtractMethodTests extends AbstractSelectionTestCase
{
	private static ExtractMethodTestSetup _testSetup;
	
    public ExtractMethodTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
		_testSetup = new ExtractMethodTestSetup(new TestSuite(ExtractMethodTests.class));
		return _testSetup;
	}

	protected IPackageFragmentRoot getRoot()
	{
		return _testSetup.getRoot();
	}
	
	protected String getResourceLocation()
	{
		return "ExtractMethod/";
	}
	
	protected String adaptName(String name)
	{
	    if (getName().startsWith("testFocusType"))
	    {
	        return super.adaptName(name);
	    }
		return name + "_" + getName() + ".java";
	}	
	
//	protected void selectionTest(int startLine, int startColumn, int endLine, int endColumn)
//		throws Exception
//	{
//		ICompilationUnit unit = createCU(getSelectionPackage(), "A");
//		String source = unit.getSource();
//		int[] selection = getSelection(source);
//		ISourceRange expected =
//		    TextRangeUtil.getSelection(unit, startLine, startColumn, endLine, endColumn);
//		assertEquals(expected.getOffset(), selection[0]);
//		assertEquals(expected.getLength(), selection[1]);
//	}
//	
//	private IPackageFragment getSelectionPackage() throws JavaModelException
//	{
//		return _testSetup.getSelectionPackage();
// 	}
	
	protected void performTest(
	        IPackageFragment packageFragment,
	        String[] ids,
	        int mode,
	        String outputFolder) throws Exception
	{
		performTest(packageFragment, ids, mode, outputFolder, null, null, 0);
	}
	
	@SuppressWarnings("unchecked")
	protected void performTest(
	        IPackageFragment packageFragment,
	        String[] ids,
	        int mode,
	        String outputFolder,
	        String[] newNames,
	        int[] newOrder,
	        int destination) throws Exception
	{
	    ICompilationUnit[] compUnits = createCUs(packageFragment, ids);
		String source = compUnits[0].getSource();
		int[] selection = getSelection(source);
		ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(
		        compUnits[0],
		        selection[0],
		        selection[1]);
		refactoring.setMethodName("extracted");
		refactoring.setVisibility(Modifier.PROTECTED);
		RefactoringStatus status =
		    refactoring.checkInitialConditions(new NullProgressMonitor());
		switch (mode)
		{
			case VALID_SELECTION:
				assertTrue(status.isOK());
				break;
			case INVALID_SELECTION:
				if (!status.isOK())
					return;
		}
		List parameters = refactoring.getParameterInfos();
		if (newNames != null && newNames.length > 0)
		{
			for (int idx= 0; idx < newNames.length; idx++)
			{
				if (newNames[idx] != null)
				{
					((ParameterInfo)parameters.get(idx)).setNewName(newNames[idx]);
				}
			}
		}
		if (newOrder != null && newOrder.length > 0)
		{
			assertTrue(newOrder.length == parameters.size());
			List current = new ArrayList(parameters);
			for (int idx= 0; idx < newOrder.length; idx++)
			{
				parameters.set(newOrder[idx], current.get(idx));
			}
		}
		refactoring.setDestination(destination);
		
		String out = null;
		switch (mode)
		{
			case COMPARE_WITH_OUTPUT:
			    out = getProofedContent(outputFolder, ids[0]);
			    break;		
		}
		performTest(compUnits[0], refactoring, mode, out, true);
	}
	
    private ICompilationUnit[] createCUs(
            IPackageFragment packageFragment,
            String[] ids)
		throws Exception
	{
	    ICompilationUnit[] cus = new ICompilationUnit[ids.length];
	
	    for (int idx = 0; idx < ids.length; idx++)
	    {
	        Assert.isNotNull(ids[idx]);
	        cus[idx] = createCU(packageFragment, ids[idx]);
	    }
	    return cus;
	}

	protected int getCheckingStyle()
	{
		return CheckConditionsOperation.FINAL_CONDITIONS;
	}

	/********** tests **********/
	//=====================================================================================
	// Testing team classes
	//=====================================================================================

	//extract method invocation in team
	public void testTeamclass1() throws Exception
    {
	    performTest(
	            _testSetup.getTeamClassPackage(),
	            new String[]{"T"},
	            COMPARE_WITH_OUTPUT,
	            "team_out");
    }
	//extract expression in team 
	public void testTeamclass2() throws Exception
    {
	    performTest(
	            _testSetup.getTeamClassPackage(),
	            new String[]{"T"},
	            COMPARE_WITH_OUTPUT,
	            "team_out");
    }

	//=====================================================================================
	// Testing role classes
	//=====================================================================================

	//extract method invocation in unbound role
	public void testRoleclass1() throws Exception
    {
	    performTest(
	            _testSetup.getRoleClassPackage(),
	            new String[]{"T"},
	            COMPARE_WITH_OUTPUT,
	            "role_out");
    }
	//extract expression in unbound role
	public void testRoleclass2() throws Exception
	{
	    performTest(
	            _testSetup.getRoleClassPackage(),
	            new String[]{"T"},
	            COMPARE_WITH_OUTPUT,
	            "role_out");
	}

	//=====================================================================================
	// Testing nested teams
	//=====================================================================================

	/* passing */
	//extract method invocation in unbound nested team
	public void testNestedTeam1() throws Exception
    {
	    performTest(
	            _testSetup.getNestedTeamPackage(),
	            new String[]{"T1"},
	            COMPARE_WITH_OUTPUT, 
	            "nestedTeam_out");
    }
	//extract method invocation in inner role of unbound nested team
	public void testNestedTeam2() throws Exception
    {
	    performTest(
	            _testSetup.getNestedTeamPackage(),
	            new String[]{"T1"},
	            COMPARE_WITH_OUTPUT, 
	            "nestedTeam_out");
    }

	/* failing */
	//extract method invocation in bound nested team -> ambiguous base method spec
	public void testNestedTeam3() throws Exception
    {
		performTest(
		        _testSetup.getNestedTeamPackage(),
		        new String[] {"B", "T1"},
		        INVALID_SELECTION,
		        "nestedTeam_out");
    }
	//extract method invocation in bound nested team -> ambiguous role method spec
	public void testNestedTeam4() throws Exception
    {
		performTest(
		        _testSetup.getNestedTeamPackage(),
		        new String[] {"T1", "B"},
		        INVALID_SELECTION,
		        "nestedTeam_out");
    }
	//extract method invocation in bound inner role of nested team ->
	//ambiguous base method spec 
	public void testNestedTeam5() throws Exception
    {
		performTest(
		        _testSetup.getNestedTeamPackage(),
		        new String[] {"B", "T1"},
		        INVALID_SELECTION,
		        "nestedTeam_out");
    }
	//extract method invocation in bound inner role of nested team ->
	//ambiguous role method spec 
	public void testNestedTeam6() throws Exception
    {
		performTest(
		        _testSetup.getNestedTeamPackage(),
		        new String[] {"T1", "B"},
		        INVALID_SELECTION,
		        "nestedTeam_out");
    }
	
	//=====================================================================================
	// Testing role files
	//=====================================================================================

	//extract method invocation in role file (unbound role)
	public void testRoleFile1() throws Exception
	{
		establishTeamCU("TeamWithRoleFile.java");
	    performTest(
	            _testSetup.getRoleFilePackage(),
	            new String[]{"R"},
			    COMPARE_WITH_OUTPUT,
			    "roleFile_out/TeamWithRoleFile");

	}

	//extract expression in role file (unbound role)
	public void testRoleFile2() throws Exception
	{
		establishTeamCU("TeamWithRoleFile.java");
	    performTest(
	            _testSetup.getRoleFilePackage(),
	            new String[]{"R"},
	            COMPARE_WITH_OUTPUT,
	    		"roleFile_out/TeamWithRoleFile");
	}

	ICompilationUnit teamCU = null;

	private void establishTeamCU(String teamFileName) throws Exception, IOException {
		if (teamCU != null) return;
		IPackageFragment pack = _testSetup.getRoleFileParentPackage();
		teamCU = createCU(pack, teamFileName, getFileInputStream(myGetFilePath(pack, teamFileName)));
	}
	//	=====================================================================================
	// 	Testing overloading and/or ambiguity for different focus types
	//	=====================================================================================	

	//focus implicit role hierarchy
	public void testFocusTypeIsSuperroleInRoleHierarchy() throws Exception
    {
		performTest(
		        _testSetup.getFocusTypePackage(),
		        new String[] {"T1", "A", "B", "C"},
		        INVALID_SELECTION,
		        "focusType_out");
    }
	
	public void testFocusTypeIsMiddleRoleInRoleHierarchy() throws Exception
    {
		performTest(
		        _testSetup.getFocusTypePackage(),
		        new String[] {"T2", "T1", "A", "B", "C"},
		        INVALID_SELECTION,
		        "focusType_out");
    }

	public void testFocusTypeIsLowestRoleInRoleHierarchy() throws Exception
    {
		performTest(
		        _testSetup.getFocusTypePackage(),
		        new String[] {"T3", "T2", "T1", "A", "B", "C"},
		        INVALID_SELECTION,
		        "focusType_out");
    }

	//focus base hierarchy
	public void testFocusTypeIsSuperBasetypeInBaseHierarchy() throws Exception
    {
		performTest(
		        _testSetup.getFocusTypePackage(),
		        new String[] {"A", "B", "C", "T1", "T2", "T3"},
		        INVALID_SELECTION,
		        "focusType_out");        
    }
	
	public void testFocusTypeIsMiddleBasetypeInBaseHierarchy() throws Exception
    {
		performTest(
		        _testSetup.getFocusTypePackage(),
		        new String[] {"B", "A", "C", "T1", "T2", "T3"},
		        INVALID_SELECTION,
		        "focusType_out");
    }
	
	public void testFocusTypeIsLowestBasetypeInBaseHierarchy() throws Exception
    {
		performTest(
		        _testSetup.getFocusTypePackage(),
		        new String[] {"C", "B", "A", "T1", "T2", "T3"},
		        INVALID_SELECTION,
		        "focusType_out");
    }
		
	//focus team hierarchy
	public void testFocusTypeIsSuperteamInTeamHierachy() throws Exception
    {
		performTest(
		        _testSetup.getFocusTypePackage(),
		        new String[] {"FocusTeam1"},
		        INVALID_SELECTION,
		        "focusType_out");
    }
	
	public void testFocusTypeIsMiddleTeamInTeamHierachy() throws Exception
    {
		performTest(
		        _testSetup.getFocusTypePackage(),
		        new String[] {"FocusTeam2", "FocusTeam1"},
		        INVALID_SELECTION,
		        "focusType_out");
    }

	public void testFocusTypeIsLowestTeamInTeamHierarchy() throws Exception
    {
		performTest(
		        _testSetup.getFocusTypePackage(),
		        new String[] {"FocusTeam3", "FocusTeam2", "FocusTeam1"},
		        INVALID_SELECTION,
		        "focusType_out");
    }

	// role is a indirect subclass of target class, explicit inheritance only, short methodSpec
	public void test20() throws Exception
	{
		performTest(
		        _testSetup.getOverloadingPackage(),
		        new String[] {"A", "B", "Base1", "Team1"},
		        INVALID_SELECTION,
		        "overloading_out");
	}
	
	// role is a indirect subclass of target class, explicit + implicit inheritance, short methodSpec
	public void test21() throws Exception
	{
		performTest(
		        _testSetup.getOverloadingPackage(),
		        new String[] {"A", "B", "Base1", "Team1", "Team2"},
		        INVALID_SELECTION,
		        "overloading_out");
	}
	
	// role is a indirect subclass of target class, explicit + implicit inheritance, long methodSpec
	public void test22() throws Exception
	{
		performTest(
		        _testSetup.getOverloadingPackage(),
		        new String[] {"A", "B", "Base1", "Team1", "Team2"},
		        COMPARE_WITH_OUTPUT,
		        "overloading_out");
	}
	
	// bound base class is target class, short methodSpec 
	public void test23() throws Exception
	{
		performTest(
		        _testSetup.getOverloadingPackage(),
		        new String[] {"A", "Team1"},
		        INVALID_SELECTION,
		        "overloading_out");
	}
	
	// bound base class is direct/indirect subclass of target class, short methodSpec
	public void test24() throws Exception
	{
		performTest(
		        _testSetup.getOverloadingPackage(),
		        new String[] {"A", "Base1", "Team1"},
		        INVALID_SELECTION,
		        "overloading_out");
	}
	
	// base class is subclass of target class, long methodSpec
	public void test25() throws Exception
	{
		performTest(
		        _testSetup.getOverloadingPackage(),
		        new String[] {"A", "Base1", "Team1"},
		        COMPARE_WITH_OUTPUT,
		        "overloading_out");
	}
}
