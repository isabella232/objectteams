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
package org.eclipse.objectteams.otdt.ui.tests.refactoring.move;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveStaticMembersProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;

/**
 * Part of the tests in this class have been copied from the original class
 * <code>MoveMembersTests</code> in the test suite
 *<code>org.eclipse.jdt.ui.tests.refactoring</code> provided by Eclipse.
 *
 * @author brcan
 *  
 */
public class MoveStaticMethodTests extends RefactoringTest
{
    private static final String REFACTORING_PATH = "MoveStaticMethod/";

    public MoveStaticMethodTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new MySetup(new TestSuite(MoveStaticMethodTests.class));
    }

    public static Test setUpTest(Test someTest)
    {
        return new MySetup(someTest);
    }

    protected String getRefactoringPath()
    {
        return REFACTORING_PATH;
    }

    private static MoveRefactoring createRefactoring(IMember[] members, IType destination)
    	throws JavaModelException
    {
        return createRefactoring(destination.getJavaProject(), members, destination.getFullyQualifiedName('.'));
    }

    private static MoveRefactoring createRefactoring(IJavaProject project, IMember[] members, String destination)
    	throws JavaModelException
    {
        MoveStaticMembersProcessor processor = new MoveStaticMembersProcessor(
                members, JavaPreferencesSettings.getCodeGenerationSettings(project));
        if (processor == null)
        {
            return null;
        }
        processor.setDestinationTypeFullyQualifiedName(destination);

        return new MoveRefactoring(processor);
    }

    //helper methods for passing tests
    private void methodHelper_passing(
            String[] methodNames,
            String[][] signatures,
            String srcCuName,
            String destCuName) throws Exception
    {
        fieldMethodTypeHelper_passing(
                new String[0], methodNames, signatures, new String[0], srcCuName, destCuName);
    }

    private void fieldMethodTypeHelper_passing(
            String[] fieldNames,
            String[] methodNames,
            String[][] signatures,
            String[] typeNames,
            String srcCuName,
            String destCuName) throws Exception
    {
        IPackageFragment packForSrc = getPackageP();
        IPackageFragment packForDest = getPackageP();
        fieldMethodTypePackageHelper_passing(
                fieldNames, methodNames, signatures, typeNames, packForSrc, packForDest, srcCuName, destCuName);
    }

    private void fieldMethodTypePackageHelper_passing(
            String[] fieldNames,
            String[] methodNames,
            String[][] signatures,
            String[] typeNames,
            IPackageFragment packForSrc,
            IPackageFragment packForDest,
            String srcCuName,
            String destTypeName) throws Exception
    {
    	String[] destNames= destTypeName.split("[$]");
    	String destCUName = destNames[0];
        //ParticipantTesting.reset();
        ICompilationUnit srcCU = createCUfromTestFile(packForSrc, srcCuName);
		ICompilationUnit destCU = createCUfromTestFile(packForDest, destCUName);
        IType srcType = getType(srcCU, srcCuName);
        IType destType = getType(destCU, destCUName);
        for (int i=1; i<destNames.length; i++)
        	destType= destType.getType(destNames[i]);
        IField[] fields = getFields(srcType, fieldNames);
        IMethod[] methods = getMethods(srcType, methodNames, signatures);
        IType[] types = getMemberTypes(srcType, typeNames);

        IType destinationType = destType;
        IMember[] members = merge(methods, fields, types);
        // FIXME(SH): is this needed (was doubly commented)?
        //String[] handles= ParticipantTesting.createHandles(members);
        //MoveArguments[] args= new MoveArguments[handles.length];
        //for (int i = 0; i < args.length; i++) {
        //	args[i]= new MoveArguments(destinationType, true);
        //}
        MoveRefactoring ref = createRefactoring(members, destinationType);

        RefactoringStatus result = performRefactoringWithStatus(ref);
        assertTrue("precondition was supposed to pass",
                result.getSeverity() <= RefactoringStatus.WARNING);
        //ParticipantTesting.testMove(handles, args);

        String expected;
        String actual;

        expected = getFileContents(getOutputTestFileName(srcCuName));
        actual = srcCU.getSource();
        assertEqualLines("incorrect modification of " + srcCuName, expected, actual);

        expected = getFileContents(getOutputTestFileName(destCUName));
        actual = destCU.getSource();
        assertEqualLines("incorrect modification of " + destTypeName, expected, actual);
    }

    //helper methods for failing tests
    private void fieldMethodTypeHelper_failing(
            String[] fieldNames,
            String[] methodNames,
            String[][] signatures,
            String[] typeNames,
            int errorLevel,
            String destinationTypeName) throws Exception
    {
        IPackageFragment packForA = getPackageP();
        IPackageFragment packForB = getPackageP();
        fieldMethodTypePackageHelper_failing(
                fieldNames, methodNames, signatures, typeNames, errorLevel, destinationTypeName, packForA, packForB);
    }

    private void fieldMethodTypePackageHelper_failing(
            String[] fieldNames,
            String[] methodNames,
            String[][] signatures,
            String[] typeNames,
            int errorLevel,
            String destinationTypeName,
            IPackageFragment packForA,
            IPackageFragment packForB) throws Exception
    {
        ICompilationUnit cuA = createCUfromTestFile(packForA, "A");
        ICompilationUnit cuB = createCUfromTestFile(packForB, "B");
        try
        {
            IType typeA = getType(cuA, "A");
            IField[] fields = getFields(typeA, fieldNames);
            IMethod[] methods = getMethods(typeA, methodNames, signatures);
            IType[] types = getMemberTypes(typeA, typeNames);

            MoveRefactoring ref = createRefactoring(
                    typeA.getJavaProject(), merge(methods, fields, types), destinationTypeName);
            if (ref == null)
            {
                assertEquals(errorLevel, RefactoringStatus.FATAL);
                return;
            }
            RefactoringStatus result = performRefactoring(ref);
            assertNotNull("precondition was supposed to fail", result);
            assertEquals("precondition was supposed to fail", errorLevel,
                    result.getSeverity());
        }
        finally
        {
            performDummySearch();
            cuA.delete(false, null);
            cuB.delete(false, null);
        }
    }
    
    /********** tests *********/
	public void test0() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test1() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test2() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test3() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test4() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test9() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test10() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test11() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test12() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test13() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test14() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test15() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test16() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test17() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test18() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test19() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test20() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test28() throws Exception
    {
        methodHelper_passing(
                new String[] { "m", "n" },
                new String[][] { new String[0], new String[0] },
                "A",
                "B");
    }

    public void test29() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void test41() throws Exception
    {
        methodHelper_passing(
                new String[] { "m" },
                new String[][] { new String[0] },
                "A",
                "B");
    }

    public void testFail0() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[0] },
                new String[0],
                RefactoringStatus.FATAL,
                "p.B");
    }

    public void testFail1() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[0] },
                new String[0],
                RefactoringStatus.ERROR,
                "p.B.X");
    }

    public void testFail2() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[0] },
                new String[0],
                RefactoringStatus.ERROR,
                "p.B");
    }

    public void testFail3() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[] { "I", "I" } },
                new String[0],
                RefactoringStatus.ERROR,
                "p.B");
    }

    public void testFail4() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[] { "I", "I" } },
                new String[0],
                RefactoringStatus.WARNING,
                "p.B");
    }

    public void testFail5() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[] { "I", "I" } },
                new String[0],
                RefactoringStatus.WARNING,
                "p.B");
    }

    public void testFail9() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[0] },
                new String[0],
                RefactoringStatus.WARNING, // visibility adjustment
                "p.B");
    }

    public void testFail10() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[0] },
                new String[0],
                RefactoringStatus.WARNING, // visibility adjustment
                "p.B");
    }

    public void testFail13() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[0] },
                new String[0],
                RefactoringStatus.WARNING, // visibility adjustment
                "p.B");
    }

    public void testFail15() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[0] },
                new String[0],
                RefactoringStatus.WARNING,
                "p.B");
    }

    public void testFail17() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[0] },
                new String[0],
                RefactoringStatus.FATAL,
                "java.lang.Object");
    }

    public void testFail18() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[0] },
                new String[0],
                RefactoringStatus.FATAL,
                "p.DontExist");
    }

    public void testFail19() throws Exception
    {
        fieldMethodTypeHelper_failing(
                new String[0],
                new String[] { "m" },
                new String[][] { new String[0] },
                new String[0],
                RefactoringStatus.ERROR,
                "p.B");
    }
    
    public void testMoveToTeamclass() throws Exception
    {
        methodHelper_passing(new String[] { "m" }, new String[][] { new String[0] }, "B", "T");
    }

    public void testMoveToNestedTeamclass1() throws Exception
    {
        methodHelper_passing(new String[] { "m" }, new String[][] { new String[0] }, "B", "T");
    }
    public void testMoveToNestedTeamclass2() throws Exception
    {
        methodHelper_passing(new String[] { "m" }, new String[][] { new String[0] }, "B", "T$TR");
    }
    public void testMoveToRoleclass() throws Exception
    {
        methodHelper_passing(new String[] { "m" }, new String[][] { new String[0] }, "B", "T$R");
    }
}