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
package org.eclipse.objectteams.otdt.ui.tests.refactoring.pullup;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.structure.PullUpRefactoringProcessor;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;

/**
 * Example TestCase to practice testing for the diploma thesis.
 * 
 * @author Johannes Gebauer
 */
public class PullUpTests extends RefactoringTest {
	private static final String REFACTORING_PATH = "PullUp/";

	public PullUpTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new MySetup(new TestSuite(PullUpTests.class));
	}

	public static Test setUpTest(Test test) {
		return new MySetup(test);
	}

	protected String getRefactoringPath() {
		return REFACTORING_PATH;
	}

	private ICompilationUnit[] createCUs(String[] cuNames) throws Exception {
		ICompilationUnit[] cus = new ICompilationUnit[cuNames.length];

		for (int idx = 0; idx < cuNames.length; idx++) {
			Assert.isNotNull(cuNames[idx]);
			cus[idx] = createCUfromTestFile(getPackageP(), cuNames[idx]);
		}
		return cus;
	}

	private String createInputTestFileName(ICompilationUnit[] cus, int idx) {
		return getInputTestFileName(getSimpleNameOfCu(cus[idx].getElementName()));
	}

	private String createOutputTestFileName(ICompilationUnit[] cus, int idx) {
		return getOutputTestFileName(getSimpleNameOfCu(cus[idx].getElementName()));
	}

	private String getSimpleNameOfCu(String compUnit) {
		int dot = compUnit.lastIndexOf('.');
		return compUnit.substring(0, dot);
	}

	private void setTargetClass(PullUpRefactoringProcessor processor, int targetClassIndex) throws JavaModelException {
		IType[] possibleClasses = getPossibleTargetClasses(processor);
		processor.setDestinationType(getPossibleTargetClasses(processor)[possibleClasses.length - 1 - targetClassIndex]);
	}

	private IType[] getPossibleTargetClasses(PullUpRefactoringProcessor processor) throws JavaModelException {
		return processor.getCandidateTypes(new RefactoringStatus(), new NullProgressMonitor());
	}

	private static IMethod[] getMethods(IMember[] members) {
		List l = Arrays.asList(JavaElementUtil.getElementsOfType(members, IJavaElement.METHOD));
		return (IMethod[]) l.toArray(new IMethod[l.size()]);
	}

	private static PullUpRefactoringProcessor createRefactoringProcessor(IMember[] methods) throws JavaModelException {
		IJavaProject project = null;
		if (methods != null && methods.length > 0)
			project = methods[0].getJavaProject();
		if (RefactoringAvailabilityTester.isPullUpAvailable(methods)) {
			PullUpRefactoringProcessor processor = new PullUpRefactoringProcessor(methods, JavaPreferencesSettings.getCodeGenerationSettings(project));
			new ProcessorBasedRefactoring(processor);
			return processor;
		}
		return null;
	}

	/**
	 * The <code>ICompilationUnit</code> containing the declaring type must be
	 * at position 0.
	 * 
	 * @param fieldNames
	 *            TODO
	 */
	private void performPullUp_pass(String[] cuNames, String[] methodNames, String[][] signatures, String[] fieldNames, boolean deleteAllInSourceType,
			boolean deleteAllMatchingMethods, int targetClassIndex, String nameOfDeclaringType) throws Exception {
		ICompilationUnit[] cus = createCUs(cuNames);
		try {

			IType declaringType = getType(cus[0], nameOfDeclaringType);
			IMethod[] methods = getMethods(declaringType, methodNames, signatures);
			IField[] fields = getFields(declaringType, fieldNames);
			IMember[] members = merge(methods, fields);

			PullUpRefactoringProcessor processor = createRefactoringProcessor(members);
			Refactoring ref = processor.getRefactoring();

			assertTrue("activation", ref.checkInitialConditions(new NullProgressMonitor()).isOK());

			setTargetClass(processor, targetClassIndex);

			if (deleteAllInSourceType)
				processor.setDeletedMethods(methods);
			if (deleteAllMatchingMethods)
				processor.setDeletedMethods(getMethods(processor.getMatchingElements(new NullProgressMonitor(), false)));

			RefactoringStatus checkInputResult = ref.checkFinalConditions(new NullProgressMonitor());
			assertTrue("precondition was supposed to pass", !checkInputResult.hasError());
			performChange(ref, false);

			for (int i = 0; i < cus.length; i++) {
				String expected = getFileContents(getOutputTestFileName(cuNames[i]));
				String actual = cus[i].getSource();
				assertEqualLines(expected, actual);
			}
		} finally {
			performDummySearch();
			for (int i = 0; i < cus.length; i++) {
				cus[i].delete(false, null);
			}

		}
	}

	/**
	 * The <code>ICompilationUnit</code> containing the declaring type must be
	 * at position 0.
	 */
	private void performPullUp_warning(String[] cuNames, String[] methodNames, String[][] signatures, boolean deleteAllInSourceType,
			boolean deleteAllMatchingMethods, int targetClassIndex, String nameOfDeclaringType) throws Exception {
		ICompilationUnit[] cus = createCUs(cuNames);
		try {

			IType declaringType = getType(cus[0], nameOfDeclaringType);
			IMethod[] methods = getMethods(declaringType, methodNames, signatures);

			PullUpRefactoringProcessor processor = createRefactoringProcessor(methods);
			Refactoring ref = processor.getRefactoring();

			
			assertTrue("activation", ref.checkInitialConditions(new NullProgressMonitor()).isOK());
			
			setTargetClass(processor, targetClassIndex);

			if (deleteAllInSourceType)
				processor.setDeletedMethods(methods);
			if (deleteAllMatchingMethods)
				processor.setDeletedMethods(getMethods(processor.getMatchingElements(new NullProgressMonitor(), false)));

			RefactoringStatus checkInputResult = ref.checkFinalConditions(new NullProgressMonitor());
			RefactoringStatusEntry[] initialConditionStatus = checkInputResult.getEntries();
			assertEquals("precondition was supposed to give a warning", RefactoringStatus.WARNING, initialConditionStatus[0].getSeverity());
			assertEquals("precondition was supposed to give only one warning", initialConditionStatus.length, 1);

			performChange(ref, false);

			for (int i = 0; i < cus.length; i++) {
				String expected = getFileContents(getOutputTestFileName(cuNames[i]));
				String actual = cus[i].getSource();
				assertEqualLines(expected, actual);
			}
		} finally {
			performDummySearch();
			for (int i = 0; i < cus.length; i++) {
				cus[i].delete(false, null);
			}

		}
	}
	
	private void performPullUp_failing(String[] cuNames, String[] methodNames, String[][] signatures, String[] fieldNames, boolean deleteAllInSourceType,
			boolean deleteAllMatchingMethods, int targetClassIndex, String nameOfDeclaringType) throws Exception {
		ICompilationUnit[] cus = createCUs(cuNames);
		try {

			IType declaringType = getType(cus[0], nameOfDeclaringType);
			IMethod[] methods = getMethods(declaringType, methodNames, signatures);
			IField[] fields = getFields(declaringType, fieldNames);
			IMember[] members = merge(methods, fields);

			PullUpRefactoringProcessor processor = createRefactoringProcessor(members);
			Refactoring ref = processor.getRefactoring();

			setTargetClass(processor, targetClassIndex);

			if (deleteAllInSourceType)
				processor.setDeletedMethods(methods);
			if (deleteAllMatchingMethods)
				processor.setDeletedMethods(getMethods(processor.getMatchingElements(new NullProgressMonitor(), false)));

			RefactoringStatus result = performRefactoring(ref);
			assertNotNull("precondition was supposed to fail.", result);
            assertTrue("precondition was supposed to fail.", !result.isOK());
			assertNotNull("precondition result is expected to contain an error.", result.getEntryMatchingSeverity(RefactoringStatus.ERROR));

		} finally {
			performDummySearch();
			for (int i = 0; i < cus.length; i++) {
				cus[i].delete(false, null);
			}

		}
	}

	/******* tests ******************/

	/* Pull up Method Tests */
	
	public void testPullUpBoundMethod() throws Exception {
		performPullUp_warning(new String[] { "B", "BSuper", "T" }, new String[] { "bm" }, new String[][] { new String[0] }, true, false, 0, "B");
	}

	public void testPullUpCallinMethod() throws Exception {
		performPullUp_pass(new String[] { "T", "TSuper", "B" }, new String[] { "rm" }, new String[][] { new String[0] }, new String[] {}, true, false, 0, "R");
	}

	public void testPullUpMethodToImplicitSuperclass() throws Exception {
		performPullUp_pass(new String[] { "T", "TSuper", "B" }, new String[] { "rm" }, new String[][] { new String[0] }, new String[] {}, true, false, 0, "R");
	}

	public void testDeletionOfMatchingMethods() throws Exception {
		performPullUp_pass(new String[] { "T", "T2", "TSuper", "B" }, new String[] { "rm" }, new String[][] { new String[0] }, new String[] {}, true, true, 0,
				"R");
	}

	public void testReferencingRoleInDeclaredLifting() throws Exception {
		performPullUp_failing(new String[] { "T", "TSuper", "B" }, new String[] { "m" }, new String[][] { new String[] { "QB;" } }, new String[] {}, true,
				false, 0, "T");
	}

	public void testPulledUpMethodIsOverriddenInImplicitSubRole() throws Exception {
		performPullUp_failing(new String[] { "T1", "T2", "TSuper", }, new String[] { "m" }, new String[][] { new String[0] }, new String[] {}, true, false, 0,
				"R");
	}

	public void testAmbiguityInExplicitRelatedRole1() throws Exception {
		performPullUp_failing(new String[] { "T", "B" }, new String[] { "m" }, new String[][] { new String[0] }, new String[] {}, true, false, 0, "R1");
	}

	public void testAmbiguityInExplicitRelatedRole2() throws Exception {
		performPullUp_failing(new String[] { "T", "B" }, new String[] { "m" }, new String[][] { new String[0] }, new String[] {}, true, false, 0, "R1");
	}

	public void testAmbiguityInImplicitRelatedRole1() throws Exception {
		performPullUp_failing(new String[] { "T1", "T2", "TSuper", "B" }, new String[] { "m" }, new String[][] { new String[0] }, new String[] {}, true, false,
				0, "R");
	}

	public void testAmbiguityInImplicitRelatedRole2() throws Exception {
		performPullUp_failing(new String[] { "T1", "T2", "TSuper", "B" }, new String[] { "m" }, new String[][] { new String[0] }, new String[] {}, true, false,
				0, "R");
	}

	public void testAmbiguityInBase1() throws Exception {
		performPullUp_failing(new String[] { "B2", "B1", "T" }, new String[] { "bm" }, new String[][] { new String[0] }, new String[] {}, true, false, 0, "B2");
	}

	public void testAmbiguityInBase2() throws Exception {
		performPullUp_failing(new String[] { "B2", "B1", "T" }, new String[] { "bm" }, new String[][] { new String[0] }, new String[] {}, true, false, 0, "B2");
	}

	public void testPullUpCallinIntoRegularClass() throws Exception {
		performPullUp_failing(new String[] { "T", "C" }, new String[] { "m" }, new String[][] { new String[0] }, new String[] {}, true, false, 0, "R");
	}

	/* Pull up Field Tests */
	
	public void testPullUpAnchoredTypeField() throws Exception {
		performPullUp_failing(new String[] { "T2", "T1", "T3" }, new String[] {}, new String[][] { new String[0] }, new String[] { "extern" }, true, false, 0,
				"T2");
	}
	
	public void testPulledUpFieldWillBeShadowed() throws Exception {
		performPullUp_failing(new String[] { "T2", "T1", "T3" }, new String[] {}, new String[][] { new String[0] }, new String[] { "f" }, true, false, 0,
				"Role");
	}
	
	public void testPullUpCalloutFieldInBase() throws Exception {
		performPullUp_pass(new String[] { "B1", "B2", "T" }, new String[] {}, new String[][] { new String[0] }, new String[] { "f" }, true, false, 0, "B1");
	}
	
	public void testPullUpFieldToImplicitSuperclass() throws Exception {
		performPullUp_pass(new String[] { "T", "TSuper" }, new String[] {}, new String[][] { new String[0] }, new String[] { "f" }, true, false, 0, "R");
	}
}
