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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameVirtualMethodProcessor;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;

/**
 * @author brcan
 *  
 */
public class RenameMethodInInterfaceTests extends RefactoringTest
{
    private static final String REFACTORING_PATH = "RenameMethodInInterface/";

    public RenameMethodInInterfaceTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new MySetup(new TestSuite(RenameMethodInInterfaceTests.class));
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
        return new RenameVirtualMethodProcessor(method);
    }

    private void performRenamingMtoK_failing(String[] cuQNames)
    	throws Exception
    {
        performRenameRefactoring_failing(cuQNames, "m", "k", new String[0]);
    }
    
    private void performRenameRefactoring_failing(
            String[] cuQNames,
            String methodName,
            String newMethodName,
            String[] signatures)
    	throws Exception
    {
        ICompilationUnit[] cus = createCUs(cuQNames);
        IType interfaceI = null;
        for (int idx = 0; idx < cus.length; idx++)
        {
            if (cus[idx].getElementName().equals("I.java"))
            {
                interfaceI = getType(cus[idx], "I");
            }
        }
        RenameMethodProcessor processor = createProcessor(
                interfaceI.getMethod(methodName, signatures));
        RenameRefactoring ref = createRefactoring(processor);
        processor.setNewElementName(newMethodName);
        RefactoringStatus result = performRefactoring(ref);
        assertNotNull("precondition was supposed to fail", result);
    }

    private void performRenaming_passing(String [] cuQNames)
    	throws Exception
    {
        performRenamingMtoK_passing(cuQNames, true);
    }
    
    /**
     * Rename method m to k and update all references.
     */
    private void performRenamingMtoK_passing(
            String [] cuQNames,
            boolean updateReferences)
    	throws Exception
    {
        performRenameRefactoring_passing(
                cuQNames, "m", "k", new String[0], true, updateReferences);
    }


    private void performRenameRefactoring_passing(
            String[] cuQNames,
            String methodName, 
            String newMethodName,
            String[] signatures, 
            boolean shouldPass, 
            boolean updateReferences)
    	throws Exception
    {
        ICompilationUnit[] cus = createCUs(cuQNames);
        IType interfaceI = null;
        for (int idx = 0; idx < cus.length; idx++)
        {
            if (cus[idx].getElementName().equals("I.java"))
            {
                interfaceI = getType(cus[idx], "I");
            }
        }
        RenameMethodProcessor processor = createProcessor(
                interfaceI.getMethod(methodName, signatures));
        RenameRefactoring ref = createRefactoring(processor);
        processor.setUpdateReferences(updateReferences);
        processor.setNewElementName(newMethodName);
        assertEquals("was supposed to pass", null, performRefactoring(ref));
        if (!shouldPass)
        {
            for (int idx = 0; idx < cus.length; idx++)
            {
	            assertTrue("incorrect renaming because of a java model bug",
	                    !getFileContents(
	                            getOutputTestFileName(getSimpleName(cuQNames[idx]))).
	                            equals(cus[idx].getSource()));
            }
            return;
        }
        for (int idx = 0; idx < cus.length; idx++)
        {
	        assertEqualLines("incorrect renaming",
	                getFileContents(
	                        getOutputTestFileName(
	                                getSimpleName(cuQNames[idx]))), cus[idx].getSource());
        }	
        assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
                .anythingToRedo());
        //assertEquals("1 to undo", 1,
        // Refactoring.getUndoManager().getRefactoringLog().size());

        RefactoringCore.getUndoManager().performUndo(null,
                new NullProgressMonitor());
        for (int idx = 0; idx < cus.length; idx++)
        {
            assertEqualLines("invalid undo",
                    getFileContents(
                            getInputTestFileName(
                                    getSimpleName(cuQNames[idx]))), cus[idx].getSource());
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
	                getFileContents(
	                        getOutputTestFileName(
	                                getSimpleName(cuQNames[idx]))), cus[idx].getSource());
        }
    }

    private ICompilationUnit[] createCUs(String[] qualifiedNames)
		throws Exception
	{
	    ICompilationUnit[] cus = new ICompilationUnit[qualifiedNames.length];
	
	    for (int idx = 0; idx < qualifiedNames.length; idx++)
	    {
	        Assert.isNotNull(qualifiedNames[idx]);
	        cus[idx] = createCUfromTestFile(getRoot().createPackageFragment(
	                	getQualifier(qualifiedNames[idx]), true, null),
	                	getSimpleName(qualifiedNames[idx]));
	    }
	    return cus;
	}
    
    private String getQualifier(String qualifiedName)
    {
        int dot = qualifiedName.lastIndexOf('.');
        return qualifiedName.substring(0, dot != -1 ? dot : 0);
    }

    private String getSimpleName(String qualifiedName)
    {
        return qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
    }

    /********** tests **********/
    //passing
    public void testUpdateImplementationInRoleClass() throws Exception
    {
        performRenaming_passing(new String[] {"p.I", "p.T"});
    }
    public void testUpdateImplementationInExplicitlyInheritedRoleClass()
    	throws Exception
    {
        performRenaming_passing(new String[] {"p.I", "p.T"});
    }
    public void testUpdateImplementationInImplicitlyInheritedRoleClass()
    	throws Exception
    {
        performRenaming_passing(new String[] {"p.I", "p.T1", "p.T2"});
    }
    //failing
    public void testMethodAlreadyExistsInRoleClass() throws Exception
    {
        performRenamingMtoK_failing(new String[] {"p.I", "p.T"});
    }
    public void testMethodAlreadyExistsInExplicitlyInheritedRoleClass()
    	throws Exception
    {
        performRenamingMtoK_failing(new String[] {"p.I", "p.T"});
    }
    public void testMethodAlreadyExistsInImplicitlyInheritedRoleClass()
	    throws Exception
	{
	    performRenamingMtoK_failing(new String[] {"p.I", "p.T1", "p.T2"});
	}

}