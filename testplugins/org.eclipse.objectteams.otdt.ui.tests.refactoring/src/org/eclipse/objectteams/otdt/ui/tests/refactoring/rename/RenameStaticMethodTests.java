/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Fraunhofer FIRST - Initial API and implementation
 * 	   Technical University Berlin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.objectteams.otdt.ui.tests.refactoring.rename;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameNonVirtualMethodProcessor;

/**
 * The tests in this class have initially been copied from the original class
 * <code>RenameStaticMethodTests</code> in the test suite
 *<code>org.eclipse.jdt.ui.tests.refactoring</code> provided by Eclipse. 
 * 
 * @author brcan
 */
public class RenameStaticMethodTests extends RefactoringTest
{
    private static final String REFACTORING_PATH = "RenameStaticMethod/";

    public RenameStaticMethodTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new MySetup(new TestSuite(RenameStaticMethodTests.class));
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

    private void performRenamingMtoK_failing() throws Exception
    {
        performRenameRefactoring_failing("m", "k", new String[0]);
    }

    private void performRenameRefactoring_failing(
            String methodName,
            String newMethodName,
            String[] signatures)
    	throws Exception
    {
        IType classA = getType(createCUfromTestFile(getPackageP(), "A"), "A");
        try
        {
            RenameMethodProcessor processor =
                createProcessor(classA.getMethod(methodName, signatures));
            RenameRefactoring refactoring = createRefactoring(processor);
            processor.setNewElementName(newMethodName);
            RefactoringStatus result = performRefactoring(refactoring);
            assertNotNull("precondition was supposed to fail", result);
        }
        finally
        {
            performDummySearch();
            classA.getCompilationUnit().delete(true, null);
        }
    }

    private void performRenaming_passing() throws Exception
    {
        performRenamingMtoK_passing(true);
    }

    /**
     * Rename method m to k and update all references.
     */
    private void performRenamingMtoK_passing(boolean updateReferences)
    	throws Exception
    {
        performRenameRefactoring_passing1("m", "k", new String[0], updateReferences);
    }

	private void performRenameRefactoring_passing2(
	        String methodName,
	        String newMethodName,
	        String[] signatures)
		throws Exception
	{
	    performRenameRefactoring_passing1(methodName, newMethodName, signatures, true);
	}

    private void performRenameRefactoring_passing1(
            String methodName,
            String newMethodName,
            String[] signatures,
            boolean updateReferences)
    	throws Exception
    {
        ICompilationUnit cu = createCUfromTestFile(getPackageP(), "A");
        try
        {
            IType classA = getType(cu, "A");
            RenameMethodProcessor processor =
                createProcessor(classA.getMethod(methodName, signatures));
            RenameRefactoring refactoring = createRefactoring(processor);
            processor.setUpdateReferences(updateReferences);
            processor.setNewElementName(newMethodName);
            assertEquals("was supposed to pass", null,
                    performRefactoring(refactoring));
            assertEqualLines("invalid renaming",
                    getFileContents(getOutputTestFileName("A")), cu.getSource());

            assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
                    .anythingToUndo());
            assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
                    .anythingToRedo());

            RefactoringCore.getUndoManager().performUndo(null,
                    new NullProgressMonitor());
            assertEqualLines("invalid undo",
                    getFileContents(getInputTestFileName("A")), cu.getSource());

            assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager()
                    .anythingToUndo());
            assertTrue("anythingToRedo", RefactoringCore.getUndoManager()
                    .anythingToRedo());

            RefactoringCore.getUndoManager().performRedo(null,
                    new NullProgressMonitor());
            assertEqualLines("invalid redo",
                    getFileContents(getOutputTestFileName("A")), cu.getSource());
        }
        finally
        {
            performDummySearch();
            cu.delete(true, null);
        }
    }

    /********** tests **********/
// test method template
//	public void test0() throws Exception
//	{
//		helper0_passing();
//	}
    
	public void testFail0() throws Exception{
	    performRenamingMtoK_failing();
	}
	
	public void testFail1() throws Exception{
	    performRenamingMtoK_failing();
	}
	
	public void testFail2() throws Exception{
	    performRenamingMtoK_failing();
	}
	
	//testFail3 deleted
	
	public void testFail4() throws Exception{
	    performRenamingMtoK_failing();
	}
	
	public void testFail5() throws Exception{
	    performRenamingMtoK_failing();
	}
	
	public void testFail6() throws Exception{
	    performRenamingMtoK_failing();
	}
	
	public void testFail7() throws Exception{
	    performRenamingMtoK_failing();
	}
	
	public void testFail8() throws Exception{
	    performRenamingMtoK_failing();
	}
	
	public void test0() throws Exception{
	    performRenaming_passing();
	}
	
	public void test1() throws Exception{
	    performRenaming_passing();
	}
	
	public void test2() throws Exception{
	    performRenaming_passing();
	}
	
	public void test3() throws Exception{
	    performRenaming_passing();
	}
	
	public void test4() throws Exception{
	    performRenaming_passing();
	}
	
	public void test5() throws Exception{
	    performRenaming_passing();
	}
	
	public void test6() throws Exception{
	    performRenaming_passing();
	}
	
	public void test7() throws Exception{
	    performRenameRefactoring_passing2("m", "k", new String[]{Signature.SIG_INT});
	}
	
	public void test8() throws Exception{
	    performRenameRefactoring_passing2("m", "k", new String[]{Signature.SIG_INT});
	}
	
	public void test9() throws Exception{
	    performRenameRefactoring_passing1("m", "k", new String[]{Signature.SIG_INT}, false);
	}
	
	public void test10() throws Exception
	{
		ICompilationUnit cuA = createCUfromTestFile(getPackageP(), "A");
		ICompilationUnit cuB = createCUfromTestFile(getPackageP(), "B");

		IType classB = getType(cuB, "B");
		RenameMethodProcessor processor =
		    createProcessor(classB.getMethod("method", new String[0]));
		RenameRefactoring refactoring = createRefactoring(processor);
		processor.setUpdateReferences(true);
		processor.setNewElementName("newmethod");
		assertEquals("was supposed to pass", null, performRefactoring(refactoring));
		assertEqualLines("invalid renaming in A", getFileContents(
		        getOutputTestFileName("A")), cuA.getSource());
		assertEqualLines("invalid renaming in B", getFileContents(
		        getOutputTestFileName("B")), cuB.getSource());
	}

	public void test11() throws Exception
	{
		IPackageFragment packageA =
		    getRoot().createPackageFragment("a", false, new NullProgressMonitor());
		IPackageFragment packageB =
		    getRoot().createPackageFragment("b", false, new NullProgressMonitor());
		try
		{
			ICompilationUnit cuA = createCUfromTestFile(packageA, "A");
			ICompilationUnit cuB = createCUfromTestFile(packageB, "B");
	
			IType classA = getType(cuA, "A");
			RenameMethodProcessor processor =
			    createProcessor(classA.getMethod("method2", new String[0]));
			RenameRefactoring refactoring = createRefactoring(processor);
			processor.setUpdateReferences(true);
			processor.setNewElementName("fred");
			assertEquals("was supposed to pass", null, performRefactoring(refactoring));
			assertEqualLines("invalid renaming in A", getFileContents(
			        getOutputTestFileName("A")), cuA.getSource());
			assertEqualLines("invalid renaming in B", getFileContents(
			        getOutputTestFileName("B")), cuB.getSource());
		}
		finally
		{
			packageA.delete(true, new NullProgressMonitor());
			packageB.delete(true, new NullProgressMonitor());
		}
	}
}