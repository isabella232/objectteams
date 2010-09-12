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
package org.eclipse.objectteams.otdt.ui.tests.refactoring.rename;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameNonVirtualMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameMethodProcessor;

/**
 * The tests in this class have been inspired by the test class
 * <code>RenamePrivateMethodTests</code> in the test suite
 *<code>org.eclipse.jdt.ui.tests.refactoring</code> provided by Eclipse JDT.
 *  
 * @author brcan
 */
public class RenamePrivateMethodTests extends RefactoringTest
{
	private static final String REFACTORING_PATH = "RenamePrivateMethod/";

	public RenamePrivateMethodTests(String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new MySetup(new TestSuite(RenamePrivateMethodTests.class));
	}

	public static Test setUpTest(Test test)
	{
		return new MySetup(test);
	}
	
	protected String getRefactoringPath()
	{
		return REFACTORING_PATH;
	}
	
    private RenameRefactoring createRefactoring(RenameMethodProcessor processor)
    {
        return new RenameRefactoring(processor);
    }

    private RenameMethodProcessor createProcessor(IMethod method)
    {
        return new RenameNonVirtualMethodProcessor(method);
    }

	private void performRenameRefactoring_failing(
	        String methodName,
	        String newMethodName,
	        String[] signatures) 
		throws Exception
    {
		IType classA = getType(createCUfromTestFile(getPackageP(), "A"), "A");
		RenameMethodProcessor processor =
		    createProcessor(classA.getMethod(methodName, signatures));
		RenameRefactoring refactoring = createRefactoring(processor);
		processor.setNewElementName(newMethodName);
		RefactoringStatus result= performRefactoring(refactoring);
		assertNotNull("precondition was supposed to fail", result);
	}
	
	private void performRenaming_failing() throws Exception
	{
		performRenameRefactoring_failing("m", "k", new String[0]);
	}

	private void performRenaming_passing(
	        String cuNames[],
	        String declaringTypeName) throws Exception
	{
		performRenamingMtoK_passing(cuNames, declaringTypeName, true);
	}
	
	private void performRenamingMtoK_passing(
	        String cuNames[],
	        String declaringTypeName,
	        boolean updateReferences)
		throws Exception
	{
		performRenameRefactoring_passing(
		        cuNames, declaringTypeName, "m", "k", new String[0], updateReferences);
	}
	
	private void performRenameRefactoring_passing(
	        String[] cuNames,
	        String declaringTypeName,
	        String methodName,
	        String newMethodName,
	        String[] signatures,
	        boolean updateReferences)
		throws Exception
	{
		ICompilationUnit[] cus = createCUs(cuNames);
		IType declaringType = getType(cus[0], declaringTypeName);
		IMethod method = declaringType.getMethod(methodName, signatures);
		RenameMethodProcessor processor = createProcessor(method);
		RenameRefactoring refactoring = createRefactoring(processor);
		processor.setUpdateReferences(updateReferences);
		processor.setNewElementName(newMethodName);
		assertEquals("was supposed to pass", null, performRefactoring(refactoring));
        for (int idx = 0; idx < cus.length; idx++)
		{
    		assertEqualLines("invalid renaming!",
    				getFileContents(createOutputTestFileName(cus, idx)), cus[idx].getSource());
		}
				
		assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
				.anythingToUndo());
		assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
				.anythingToRedo());

		RefactoringCore.getUndoManager().performUndo(null,
				new NullProgressMonitor());
        for (int idx = 0; idx < cus.length; idx++)
		{
    		assertEqualLines("invalid undo",
    				getFileContents(createInputTestFileName(cus, idx)), cus[idx].getSource());
        }
        
		assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager()
				.anythingToUndo());
		assertTrue("anythingToRedo", RefactoringCore.getUndoManager()
				.anythingToRedo());

		RefactoringCore.getUndoManager().performRedo(null,
				new NullProgressMonitor());
        for (int idx = 0; idx < cus.length; idx++)
        {
    		assertEqualLines("invalid redo",
    		        getFileContents(createOutputTestFileName(cus, idx)), cus[idx].getSource());
        }
	}

    private ICompilationUnit[] createCUs(String[] cuNames) throws Exception
    {
        ICompilationUnit[] cus = new ICompilationUnit[cuNames.length];
	
	    for (int idx = 0; idx < cuNames.length; idx++)
	    {
	        Assert.isNotNull(cuNames[idx]);
	        cus[idx] = createCUfromTestFile(getPackageP(), cuNames[idx]);
	    }
	    return cus;
	}

    private String createInputTestFileName(ICompilationUnit[] cus, int idx)
    {
        return getInputTestFileName(getSimpleNameOfCu(cus[idx].getElementName()));
    }

    private String createOutputTestFileName(ICompilationUnit[] cus, int idx)
    {
        return getOutputTestFileName(getSimpleNameOfCu(cus[idx].getElementName()));
    }

    private String getSimpleNameOfCu(String compUnit)
    {
        int dot = compUnit.lastIndexOf('.');
        return compUnit.substring(0, dot);
    }

	/******* tests ******************/
	
    public void testUpdateReferenceInCalloutBinding1() throws Exception
    {
        performRenaming_passing(new String[]{"B", "T"}, "B");
    } 
	
    public void testUpdateReferenceInCalloutBinding2() throws Exception
    {
        performRenameRefactoring_passing(
                new String[]{"B", "T"}, "B", "getAmount", "getQuantity", null, true);
    }
	
    public void testUpdateReferenceInCallinBinding1() throws Exception
    {
        performRenaming_passing(new String[]{"B", "T"}, "B");
    }

    public void testUpdateReferenceInCallinBinding2() throws Exception
    {
        performRenaming_passing(new String[]{"B", "T"}, "B");
    }

    //renaming of private role method
    public void testPrivateMethodInTeamclass() throws Exception
    {
        performRenaming_passing(new String[]{"T"}, "T");
    }
    
    public void testUpdatePrivateTeamMethodInvocation1() throws Exception
    {
        performRenaming_passing(new String[]{"T"}, "T");
    }
    
    public void testUpdatePrivateTeamMethodInvocation2() throws Exception
    {
        performRenameRefactoring_passing(new String[]{"T", "B1"}, "T", "m1", "k1", null, true);
    }

}
