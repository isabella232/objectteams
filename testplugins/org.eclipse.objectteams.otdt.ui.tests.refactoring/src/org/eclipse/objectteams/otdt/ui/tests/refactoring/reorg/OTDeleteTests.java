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
package org.eclipse.objectteams.otdt.ui.tests.refactoring.reorg;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgQueries;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaDeleteProcessor;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgUtils;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.DeleteRefactoring;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;

/**
 * @author brcan
 */
public class OTDeleteTests extends RefactoringTest
{
    private static final String REFACTORING_PATH = "Delete/";
    private static final String CU_NAME = "T";
    private ICompilationUnit _cuT;

    public OTDeleteTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new MySetup(new TestSuite(OTDeleteTests.class));
    }

    public static Test setUpTest(Test someTest)
    {
        return new MySetup(someTest);
    }

    protected String getRefactoringPath()
    {
        return REFACTORING_PATH;
    }

    private void loadFileSetup() throws Exception
    {
        _cuT = createCUfromTestFile(getPackageP(), CU_NAME);
        assertTrue("T.java does not exist", _cuT.exists());
    }

    private void checkDelete(IJavaElement[] elems, boolean deleteCu)
        throws JavaModelException, Exception
    {
        ICompilationUnit newCuT = null;
        try
        {
            DeleteRefactoring refactoring = createRefactoring(elems);
            assertNotNull(refactoring);
            RefactoringStatus status = performRefactoring(refactoring, true);
            assertEquals("precondition was supposed to pass", null, status);

            newCuT = getPackageP().getCompilationUnit(CU_NAME + ".java");
            assertTrue("T.java does not exist", newCuT.exists() == !deleteCu);
            if (!deleteCu)
            {
                assertEqualLines(
                        "incorrect content of T.java",
                        getFileContents(getOutputTestFileName(CU_NAME)),
                        newCuT.getSource());
            }
        }
        finally
        {
            performDummySearch();
            if (newCuT != null && newCuT.exists())
            {
                newCuT.delete(true, null);
            }
            if (_cuT != null && _cuT.exists())
            {
                _cuT.delete(true, null);
                _cuT = null;
            }
        }
    }

    private DeleteRefactoring createRefactoring(Object[] elements)
        throws CoreException
    {
        JavaDeleteProcessor processor = new JavaDeleteProcessor(elements);
        DeleteRefactoring result = new DeleteRefactoring(processor);
        processor.setQueries(createReorgQueries());
        return result;
    }

    private IReorgQueries createReorgQueries()
    {
        return new MockReorgQueries();
    }

    //---- tests ----    
    public void testDeleteTeamclass() throws Exception
    {
//      ParticipantTesting.reset();
        loadFileSetup();
        IJavaElement aTeam = _cuT.getType("T");
        IJavaElement[] elems = new IJavaElement[] { aTeam };
        checkDelete(elems, true);
//      String[] handles = ParticipantTesting.createHandles(elem0);
//      ParticipantTesting.testDelete(handles);        
    }
    
    public void testDeleteNestedTeamclass() throws Exception
    {
        loadFileSetup();
        IJavaElement nestedTeam = _cuT.getType("T").getType("TR");
        IJavaElement[] elems = new IJavaElement[] { nestedTeam };
        checkDelete(elems, false);
    }

    public void testDeleteRoleclass() throws Exception
    {
    	loadFileSetup();
    	IType type = _cuT.getType("T").getType("R");
    	IRoleType role = (IRoleType)OTModelManager.getOTElement(type); 
    	IJavaElement[] elems = new IJavaElement[] { role };
    	checkDelete(elems, false);
    }

    @SuppressWarnings("restriction")
	public void testGetRoleclassName() throws Exception
    {
    	loadFileSetup();
    	IType type = _cuT.getType("T").getType("R");
    	IRoleType role = (IRoleType)OTModelManager.getOTElement(type); 
    	String message1= ReorgUtils.getName(role);
    	assertEquals("role 'R'", message1);
    }

    public void testDeleteCalloutMapping() throws Exception
    {
    	loadFileSetup();
    	IType type = _cuT.getType("T").getType("R");
    	IRoleType role = (IRoleType)OTModelManager.getOTElement(type); 
    	IMethodMapping[] mappings = role.getMethodMappings(); 
    	IJavaElement[] elems = new IJavaElement[] { mappings[0] };
    	checkDelete(elems, false);
    }

    public void testDeleteCalloutToFieldMapping() throws Exception
    {
    	loadFileSetup();
    	IType type = _cuT.getType("T").getType("R");
    	IRoleType role = (IRoleType)OTModelManager.getOTElement(type); 
    	IMethodMapping[] mappings = role.getMethodMappings(); 
    	IJavaElement[] elems = new IJavaElement[] { mappings[0] };
    	checkDelete(elems, false);
    }

    public void testDeleteCallinMapping() throws Exception
    {
    	loadFileSetup();
    	IType type = _cuT.getType("T").getType("R");
    	IRoleType role = (IRoleType)OTModelManager.getOTElement(type); 
    	IMethodMapping[] mappings = role.getMethodMappings(); 
    	IJavaElement[] elems = new IJavaElement[] { mappings[0] };
    	checkDelete(elems, false);
    }
}