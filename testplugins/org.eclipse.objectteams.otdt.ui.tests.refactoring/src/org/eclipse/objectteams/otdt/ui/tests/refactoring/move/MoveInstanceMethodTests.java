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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.ui.tests.refactoring.infra.TextRangeUtil;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.objectteams.otdt.internal.refactoring.corext.base.OTRefactoringStatusCodes;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;

/**
 * @author brcan
 *  
 */
@SuppressWarnings({ "restriction", "nls" })
public class MoveInstanceMethodTests extends RefactoringTest
{
    private static final String REFACTORING_PATH = "MoveInstanceMethod/";

    private static final int PARAMETER = 0;
    private static final int FIELD = 1;

    private boolean toSucceed;

    public MoveInstanceMethodTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (false)
        {
            TestSuite suite = new TestSuite(MoveInstanceMethodTests.class.getName());
            suite.addTest(new MoveInstanceMethodTests("testRoleclass1"));
	        return new MySetup(suite);
        }
        return new MySetup(new TestSuite(MoveInstanceMethodTests.class));
    }

    protected String getRefactoringPath()
    {
        return REFACTORING_PATH + successPath();
    }

    private String successPath()
    {
        return toSucceed ? "/canMove/" : "/cannotMove/";
    }

    private void performAndCheckSelectedCU_passing(
            String[] cuQNames, String selectionCuQName,
            int startLine, int startColumn, int endLine, int endColumn,
            int newReceiverType, String newReceiverName,
            boolean inlineDelegator, boolean removeDelegator, boolean expectWarning)
    	throws Exception
    {
        int selectionCuIndex = firstIndexOf(selectionCuQName, cuQNames);
        Assert.isTrue(selectionCuIndex != -1,
                       "parameter selectionCuQName must match some String in cuQNames.");
        performRefactoring_passing(cuQNames, selectionCuIndex, startLine, startColumn, endLine,
                endColumn, newReceiverType, newReceiverName, null,
                inlineDelegator, removeDelegator, expectWarning);
    }

    private void performAndCheckSelectedCU_passing_std_info(
            String[] cuQNames, String selectionCuQName,
            int startLine, int startColumn, int endLine, int endColumn,
            int newReceiverType, String newReceiverName,
            boolean inlineDelegator, boolean removeDelegator, boolean expectWarning)
    	throws Exception
    {
        int selectionCuIndex = firstIndexOf(selectionCuQName, cuQNames);
        Assert.isTrue(selectionCuIndex != -1,
                       "parameter selectionCuQName must match some String in cuQNames.");
        performRefactoring_passing_expecting_specific_warning(cuQNames, selectionCuIndex, startLine, startColumn, endLine,
                endColumn, newReceiverType, newReceiverName, null,
                inlineDelegator, removeDelegator, "The 'Move Instance Method' Refactoring is not yet fully OT-aware!");
    }

    private int firstIndexOf(String one, String[] others)
    {
        for (int idx = 0; idx < others.length; idx++)
        {
            if (one == null && others[idx] == null || one.equals(others[idx]))
            {
                return idx;
            }
        }
        return -1;
    }

    private void performRefactoring_passing(
            String[] cuQNames, int selectionCuIndex,
            int startLine, int startColumn, int endLine, int endColumn,
            int newReceiverType, String newReceiverName, String newMethodName,
            boolean inlineDelegator, boolean removeDelegator, boolean expectWarning)
    	throws Exception
    {
        Assert.isTrue(0 <= selectionCuIndex
                && selectionCuIndex < cuQNames.length);

        toSucceed = true;

        ICompilationUnit[] cus = createCUs(cuQNames);
        ICompilationUnit selectionCu = cus[selectionCuIndex];

        ISourceRange selection = TextRangeUtil.getSelection(selectionCu,
                startLine, startColumn, endLine, endColumn);
        IMethod method = getMethod(selectionCu, selection);
        assertNotNull(method);
        MoveInstanceMethodProcessor refProc = new MoveInstanceMethodProcessor(
		        								method,
		        								JavaPreferencesSettings.getCodeGenerationSettings(selectionCu.getJavaProject()));
   		Refactoring ref= new MoveRefactoring(refProc);

		assertNotNull("refactoring should be created", ref);
		RefactoringStatus preconditionResult= ref
			.checkInitialConditions(new NullProgressMonitor());

		assertTrue("activation was supposed to be successful",
			preconditionResult.isOK());

        chooseNewReceiver(refProc, newReceiverType, newReceiverName);

        refProc.setInlineDelegator(inlineDelegator);
		refProc.setRemoveDelegator(removeDelegator);
		refProc.setDeprecateDelegates(false);
        if (newMethodName != null)
        {
            refProc.setMethodName(newMethodName);
        }
        preconditionResult.merge(ref.checkFinalConditions(new NullProgressMonitor()));
        if (!expectWarning) {
        	if (!preconditionResult.isOK()) {
        		System.out.println(getName()+":"+preconditionResult);
        		System.out.println("Error:"+preconditionResult.getMessageMatchingSeverity(RefactoringStatus.ERROR));
        		System.out.println("Warning:"+preconditionResult.getMessageMatchingSeverity(RefactoringStatus.WARNING));
        	}
	        assertTrue("precondition was supposed to pass", preconditionResult
	                .isOK());
	
	        assertFalse("no warnings expected", preconditionResult.hasWarning());
        } else {
        	assertTrue("warning expected", preconditionResult.hasWarning());
        }
        
        performChange(ref, false);
        for (int idx = 0; idx < cus.length; idx++)
        {
            String outputTestFileName = getOutputTestFileName(getSimpleName(cuQNames[idx]));
            assertEqualLines("Incorrect inline in " + outputTestFileName,
                    getFileContents(outputTestFileName), cus[idx].getSource());
        }
    }

    private void performRefactoring_passing_expecting_specific_warning(
            String[] cuQNames, int selectionCuIndex,
            int startLine, int startColumn, int endLine, int endColumn,
            int newReceiverType, String newReceiverName, String newMethodName,
            boolean inlineDelegator, boolean removeDelegator, String expectedWarning)
    	throws Exception
    {
        Assert.isTrue(0 <= selectionCuIndex
                && selectionCuIndex < cuQNames.length);

        toSucceed = true;

        ICompilationUnit[] cus = createCUs(cuQNames);
        ICompilationUnit selectionCu = cus[selectionCuIndex];

        ISourceRange selection = TextRangeUtil.getSelection(selectionCu,
                startLine, startColumn, endLine, endColumn);
        IMethod method = getMethod(selectionCu, selection);
        assertNotNull(method);
        MoveInstanceMethodProcessor refProc = new MoveInstanceMethodProcessor(
		        								method,
		        								JavaPreferencesSettings.getCodeGenerationSettings(selectionCu.getJavaProject()));
   		Refactoring ref= new MoveRefactoring(refProc);

		assertNotNull("refactoring should be created", ref);
		RefactoringStatus preconditionResult= ref
			.checkInitialConditions(new NullProgressMonitor());

		assertTrue("activation was supposed to be successful",
			preconditionResult.isOK());

        chooseNewReceiver(refProc, newReceiverType, newReceiverName);

        refProc.setInlineDelegator(inlineDelegator);
		refProc.setRemoveDelegator(removeDelegator);
		refProc.setDeprecateDelegates(false);
        if (newMethodName != null)
        {
            refProc.setMethodName(newMethodName);
        }
        preconditionResult.merge(ref.checkFinalConditions(new NullProgressMonitor()));
       	assertTrue("expected a warning", preconditionResult.hasInfo());
       	assertEquals("expected specific warning", expectedWarning, preconditionResult.getMessageMatchingSeverity(RefactoringStatus.INFO));
        
        performChange(ref, false);
        for (int idx = 0; idx < cus.length; idx++)
        {
            String outputTestFileName = getOutputTestFileName(getSimpleName(cuQNames[idx]));
            assertEqualLines("Incorrect inline in " + outputTestFileName,
                    getFileContents(outputTestFileName), cus[idx].getSource());
        }
    }

	private void performAndCheckSelectedCU_failing(
	        String[] cuQNames, String selectionCuQName,
            int startLine, int startColumn, int endLine, int endColumn,
            int newReceiverType, String newReceiverName,
            boolean inlineDelegator, boolean removeDelegator, int errorCode)
		throws Exception
    {
        int selectionCuIndex = firstIndexOf(selectionCuQName, cuQNames);
        Assert.isTrue(selectionCuIndex != -1,
                       "parameter selectionCuQName must match some String in cuQNames.");
        performRefactoring_failing(cuQNames, selectionCuIndex, startLine, startColumn,
                endLine, endColumn, newReceiverType, newReceiverName, 
                null, inlineDelegator, removeDelegator, errorCode);
    }

	private void performRefactoring_failing(
	        String[] cuQNames, int selectionCuIndex,
            int startLine, int startColumn, int endLine, int endColumn,
            int newReceiverType, String newReceiverName, String newMethodName,
            boolean inlineDelegator, boolean removeDelegator, int errorCode)
		throws Exception
    {
        Assert.isTrue(0 <= selectionCuIndex
                && selectionCuIndex < cuQNames.length);

        toSucceed = false;

        ICompilationUnit[] cus = createCUs(cuQNames);
        ICompilationUnit selectionCu = cus[selectionCuIndex];

        ISourceRange selection = TextRangeUtil.getSelection(selectionCu,
                startLine, startColumn, endLine, endColumn);
        IMethod method = getMethod(selectionCu, selection);
        assertNotNull(method);
        MoveInstanceMethodProcessor refProc = new MoveInstanceMethodProcessor(
									                    method,
									                    JavaPreferencesSettings.getCodeGenerationSettings(selectionCu.getJavaProject()));
   		Refactoring ref= new MoveRefactoring(refProc);
		RefactoringStatus result= ref.checkInitialConditions(
									   new NullProgressMonitor());

		if (!result.isOK())
		{
		    assertEquals(errorCode, result.getEntryMatchingSeverity(
		            RefactoringStatus.ERROR).getCode());
		    return;
		}
		else
		{
		    chooseNewReceiver(refProc, newReceiverType, newReceiverName);

		    refProc.setInlineDelegator(inlineDelegator);
		    refProc.setRemoveDelegator(removeDelegator);
		    refProc.setDeprecateDelegates(false);
		    if (newMethodName != null)
		        refProc.setMethodName(newMethodName);

		    result.merge(ref
		    		.checkFinalConditions(new NullProgressMonitor()));

		    assertTrue("precondition checking is expected to fail.", !result.isOK());
		    assertNotNull("precondition result is expected to contain an error.", result.getEntryMatchingSeverity(RefactoringStatus.ERROR));
		    assertEquals(errorCode, result.getEntryMatchingSeverity(
		            RefactoringStatus.ERROR).getCode());
		}
    }	

	private void performConditionChecking_warnings(
	        String[] cuQNames, String selectionCuQName,
            int startLine, int startColumn, int endLine, int endColumn,
            int newReceiverType, String newReceiverName, String newMethodName,
            boolean inlineDelegator, boolean removeDelegator, int errorCode)
		throws Exception
    {
        int selectionCuIndex = firstIndexOf(selectionCuQName, cuQNames);
        Assert.isTrue(selectionCuIndex != -1,
                       "parameter selectionCuQName must match some String in cuQNames.");
        Assert.isTrue(0 <= selectionCuIndex
                && selectionCuIndex < cuQNames.length);

        toSucceed = true;

        ICompilationUnit[] cus = createCUs(cuQNames);
        ICompilationUnit selectionCu = cus[selectionCuIndex];

        ISourceRange selection = TextRangeUtil.getSelection(selectionCu,
                startLine, startColumn, endLine, endColumn);
        IMethod method = getMethod(selectionCu, selection);
        assertNotNull(method);
        MoveInstanceMethodProcessor refProc= new MoveInstanceMethodProcessor(
									                method,
									                JavaPreferencesSettings.getCodeGenerationSettings(selectionCu.getJavaProject()));
        Refactoring ref = new MoveRefactoring(refProc);
        RefactoringStatus result = ref.checkInitialConditions(
		        new NullProgressMonitor());
		if (!result.isOK())
		{
		    assertEquals(errorCode, result.getEntryMatchingSeverity(
		            RefactoringStatus.ERROR).getCode());
		    return;
		}
		else
		{
		    chooseNewReceiver(refProc, newReceiverType, newReceiverName);

		    refProc.setInlineDelegator(inlineDelegator);
		    refProc.setRemoveDelegator(removeDelegator);
		    refProc.setDeprecateDelegates(false);
		    if (newMethodName != null)
		        refProc.setMethodName(newMethodName);

		    result.merge(ref
		            .checkFinalConditions(new NullProgressMonitor()));

		    assertTrue("warnings expected", result.hasWarning());
		    assertEquals(errorCode, result.getEntryMatchingSeverity(
		            RefactoringStatus.WARNING).getCode());
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

    private static IMethod getMethod(ICompilationUnit cu,
            ISourceRange sourceRange) throws JavaModelException
    {
        IJavaElement[] jes = cu.codeSelect(sourceRange.getOffset(), sourceRange
                .getLength());
        if (jes.length != 1 || !(jes[0] instanceof IMethod))
            return null;
        return (IMethod)jes[0];
    }

    private static void chooseNewReceiver(
            MoveInstanceMethodProcessor refProc, int newReceiverType,
            String newReceiverName)
    {
        IVariableBinding chosen = null;
        IVariableBinding[] possibleNewReceivers = refProc.getPossibleTargets();
        for (int i = 0; i < possibleNewReceivers.length; i++)
        {
            IVariableBinding candidate = possibleNewReceivers[i];
            if (candidate.getName().equals(newReceiverName)
                    && typeMatches(newReceiverType, candidate))
            {
                assertNull(chosen);
                chosen = candidate;
            }
        }
        assertNotNull("Expected new receiver not available.", chosen);
        refProc.setTarget(chosen);
    }

    private static boolean typeMatches(int newReceiverType,
            IVariableBinding newReceiver)
    {
        return newReceiverType == PARAMETER && newReceiver.isParameter()
                || newReceiverType == FIELD && newReceiver.isField();
    }

    /********** tests *********/
// test method template    
//  public void test0() throws Exception
//  {
//      helper1(new String[] { "p1.A", "p2.B", "p3.C" }, "p1.A", 7, 17, 7, 20,
//              PARAMETER, "b", false, false);
//  }
//	public void testFail0() throws Exception
//	{
//		failHelper1("p1.IA", 5, 17, 5, 20, PARAMETER, "b", true, true, RefactoringStatusCodes.SELECT_METHOD_IMPLEMENTATION);	
//	}

    // TODO(SH): more tests for combinations of inline,removeDelegate!
    
    /* Testing receiver types */
	public void testFieldNewReceiver() throws Exception {
		performAndCheckSelectedCU_passing(new String[] { "p.A", "p.B" }, "p.A", 7, 17, 7, 19, FIELD, "b", true, true,
				true);// warn: change-field-to-default-vis
	}

	public void testParameterNewReceiver() throws Exception {
		performAndCheckSelectedCU_passing(new String[] { "p.A", "p.B" }, "p.A", 5, 17, 5, 19, PARAMETER, "b", true,
				true, false);
	}

	/* Testing team classes */
	public void testTeamclass1() throws Exception {
		performAndCheckSelectedCU_passing(new String[] { "p.T", "p.O" }, "p.O", 7, 17, 7, 20, FIELD, "_t", true, true,
		true);// warn: change-field-to-default-vis
	}

	public void testTeamclass2() throws Exception {
		performAndCheckSelectedCU_passing(new String[] { "p.T", "p.O" }, "p.O", 5, 17, 5, 20, PARAMETER, "t", false,
				false, false);
	}

	/* Testing role classes */
	public void testRoleclass1() throws Exception {
		// XXX(SH): expect info: The 'Move Instance Method' Refactoring is not yet fully OT-aware!
		performAndCheckSelectedCU_passing_std_info(new String[] { "p.T", "p.O" }, "p.O", 8, 17, 8, 20, FIELD, "_r", true, false,
				false);
	}

	public void testRoleclass2() throws Exception {
		// XXX(SH): expect info: The 'Move Instance Method' Refactoring is not yet fully OT-aware!
		performAndCheckSelectedCU_passing_std_info(new String[] { "p.T", "p.O" }, "p.O", 7, 17, 7, 20, PARAMETER, "r", true,
				false, false);
	}

	/* Testing nested teams */
	public void testNestedTeam1() throws Exception {
		// XXX(SH): expect info: The 'Move Instance Method' Refactoring is not yet fully OT-aware!
		performAndCheckSelectedCU_passing_std_info(new String[] { "p.T", "p.O" }, "p.O", 8, 17, 8, 20, FIELD, "_nT", true,
				false, false);
	}

	public void testNestedTeam2() throws Exception {
		// XXX(SH): expect info: The 'Move Instance Method' Refactoring is not yet fully OT-aware!
		performAndCheckSelectedCU_passing_std_info(new String[] { "p.T", "p.O" }, "p.O", 7, 17, 7, 20, PARAMETER, "nT", true,
				false, false);
	}

	/* Testing role files */
	// TODO(gbr) implement tests for role files
	// public void testRoleFile1() throws Exception
	// {
	// }
	//
	// public void testRoleFile2() throws Exception
	// {
	// }

	public void testRoleClassWithLongMethodSpecsInCallinBinding() throws Exception {
		performConditionChecking_warnings(new String[] { "p.O", "p.S", "p.B", "p.T" }, "p.O", 8, 17, 8, 18, FIELD,
				"_s", null, true, true,
				// FIXME(SH):
				// OTRefactoringStatusCodes.OVERLOADING
				-1);
	}

	public void testRoleClassWithLongMethodSpecsInCalloutBinding() throws Exception {
		performConditionChecking_warnings(new String[] { "p.O", "p.S", "p.B", "p.T" }, "p.O", 8, 17, 8, 18, FIELD,
				"_s", null, true, true,
				// FIXME(SH):
				// OTRefactoringStatusCodes.OVERLOADING
				-1);
	}

	public void testExplicitlyInheritedRoleClassWithLongMethodSpecs() throws Exception {
		performConditionChecking_warnings(new String[] { "p.O", "p.S", "p.B", "p.T" }, "p.O", 8, 17, 8, 18, FIELD,
				"_s", null, true, true,
				// FIXME(SH):
				// OTRefactoringStatusCodes.OVERLOADING
				-1);
	}

	public void testImplicitlyInheritedRoleClassWithLongMethodSpecs() throws Exception {
		performConditionChecking_warnings(new String[] { "p.O", "p.S", "p.B", "p.T1", "p.T2" }, "p.O", 8, 17, 8, 18,
				FIELD, "_s", null, true, true,
				// FIXME(SH):
				// OTRefactoringStatusCodes.OVERLOADING
				-1);
	}

	public void testTargetMethodAccessesPrivateFeatureOfEnclosingType1() throws Exception {
		performAndCheckSelectedCU_passing(new String[] { "p.A", "p.B", "p.C" }, "p.A", 8, 17, 8, 19, FIELD, "_c", true,
				true, true);// warn: move needs to open visibility of field
	}

	public void testTargetMethodAccessesPrivateFeatureOfEnclosingType2() throws Exception {
		performAndCheckSelectedCU_passing(new String[] { "p.A", "p.B", "p.C" }, "p.A", 7, 17, 7, 19, PARAMETER, "c",
				true, true, true);// warn: move needs to open visibility of
									// field
	}

	public void testTargetMethodAccessesProtectedFeatureOfEnclosingType1() throws Exception {
		performAndCheckSelectedCU_passing(new String[] { "p1.A", "p1.B", "p2.C" }, "p1.A", 10, 17, 10, 19, FIELD, "_c",
				true, true, true);// warn: move needs to open visibility of
									// field
	}

	public void testTargetMethodAccessesProtectedFeatureOfEnclosingType2() throws Exception {
		performAndCheckSelectedCU_passing(new String[] { "p1.A", "p1.B", "p2.C" }, "p1.A", 9, 17, 9, 19, PARAMETER,
				"c", true, true, true);// warn: move needs to open visibility of
										// field
	}

	public void testTargetMethodAccessesPackageVisibleFeatureOfEnclosingType1() throws Exception {
		performAndCheckSelectedCU_passing(new String[] { "p1.A", "p1.B", "p2.C" }, "p1.A", 10, 17, 10, 19, FIELD, "_c",
				true, true, true);// warn: move needs to open visibility of
									// field
	}

	public void testTargetMethodAccessesPackageVisibleFeatureOfEnclosingType2() throws Exception {
		performAndCheckSelectedCU_passing(new String[] { "p1.A", "p1.B", "p2.C" }, "p1.A", 9, 17, 9, 19, PARAMETER,
				"c", true, true, true);// warn: move needs to open visibility of
										// field
	}
    //tests for ambiguity in method specs
	public void testAmbiguousRoleMethodSpecifierInCallinBinding1()
    	throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.O", "p.S", "p.B", "p.T"},
                "p.O", 8, 17, 8, 18, FIELD, "_s",
                true, true, OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER);
    }
    public void testAmbiguousRoleMethodSpecifierInCallinBinding2()
    	throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.O", "p.S", "p.T"},
                "p.O", 5, 17, 5, 18, PARAMETER, "s",
                true, true, OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER);
    }
    public void _testAmbiguousBaseMethodSpecifierInCallinBinding1()
    	throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.O", "p.S", "p.B", "p.T"},
                "p.O", 8, 17, 8, 18, FIELD, "_s",
                false, false,
				OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER);
    }
    public void testAmbiguousBaseMethodSpecifierInCallinBinding2()
		throws Exception
	{
	    performAndCheckSelectedCU_failing(
	            new String[] {"p.O", "p.S", "p.T"},
	            "p.O", 5, 17, 5, 18, PARAMETER, "s",
	            true, true, OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER);
	}
    public void testAmbiguousRoleMethodSpecifierInCalloutBinding1()
    	throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.O", "p.S", "p.B", "p.T"},
                "p.O", 8, 17, 8, 18, FIELD, "_s",
                true, true, OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER);
    }
    public void testAmbiguousRoleMethodSpecifierInCalloutBinding2()
    	throws Exception
	{
	    performAndCheckSelectedCU_failing(
	            new String[] {"p.O", "p.S", "p.T"},
	            "p.O", 5, 17, 5, 18, PARAMETER, "s",
	            true, true, OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER);
	}
    public void _testAmbiguousBaseMethodSpecifierInCalloutBinding1()
    	throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.O", "p.S", "p.B", "p.T"},
                "p.O", 8, 17, 8, 18, FIELD, "_s",
                true, true, OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER);
    }
    public void testAmbiguousBaseMethodSpecifierInCalloutBinding2()
		throws Exception
	{
	    performAndCheckSelectedCU_failing(
	            new String[] {"p.O", "p.S", "p.T"},
	            "p.O", 5, 17, 5, 18, PARAMETER, "s",
	            false, false,
				OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER);
	}
    //tests for overriding
    public void _testMovedMethodIsOverriddenInBaseClass()
		throws Exception
	{
        performAndCheckSelectedCU_failing(
	            new String[] {"p.O", "p.S", "p.T"},
	            "p.O", 7, 17, 7, 18, FIELD, "_s",
	            true, true, OTRefactoringStatusCodes.BASE_METHOD_OVERRIDES_MOVED_METHOD);
	}
    public void _testMovedMethodIsOverriddenInRoleClass()
    	throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.O", "p.S", "p.T"},
                "p.O", 7, 17, 7, 18, FIELD, "_s",
                true, true, OTRefactoringStatusCodes.ROLE_METHOD_OVERRIDES_MOVED_METHOD);
    }
    public void _testMovedMethodIsOverriddenInExplicitlyInheritedRoleClass()
    	throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.O", "p.S", "p.T"},
                "p.O", 7, 17, 7, 18, FIELD, "_s",
                true, true, OTRefactoringStatusCodes.ROLE_METHOD_OVERRIDES_MOVED_METHOD);
    }
    public void _testMovedMethodIsOverriddenInImplicitlyInheritedRoleClass()
    	throws Exception
	{
        performAndCheckSelectedCU_failing(
                new String[] {"p.O", "p.S", "p.T1", "p.T2"},
                "p.O", 7, 17, 7, 18, FIELD, "_s",
                true, true, OTRefactoringStatusCodes.ROLE_METHOD_OVERRIDES_MOVED_METHOD);
	}
    //tests for duplication
    public void testDuplicateMethodInNewReceiver()
    	throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.O", "p.S"},
                "p.O", 7, 17, 7, 17, FIELD, "_s",
                true, true, -1);
    }
    public void testDuplicateMethodInBaseClassReceiver()
    	throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.O", "p.S", "p.T"},
                "p.O", 7, 17, 7, 17, FIELD, "_s",
                true, true, -1);
    }

    public void _testDuplicateMethodInNewParameterReceiver1()
    throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.A", "p.B"},
                "p.A", 6, 17, 6, 18, PARAMETER, "b",
                true, true, OTRefactoringStatusCodes.DUPLICATE_METHOD_IN_NEW_RECEIVER);
    }
    
    public void _testDuplicateMethodInNewParameterReceiver2()
    throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.A", "p.B"},
                "p.A", 8, 17, 8, 18, PARAMETER, "b",
                true, true, OTRefactoringStatusCodes.DUPLICATE_METHOD_IN_NEW_RECEIVER);
    }
    
    public void _testTargetMethodIsPrivate()
    	throws Exception
    {
        performAndCheckSelectedCU_failing(
                new String[] {"p.A", "p.B"},
                "p.A", 6, 18, 6, 20, PARAMETER, "b",
                true, true, OTRefactoringStatusCodes.CANNOT_MOVE_PRIVATE);
    }
    
}