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
package org.eclipse.objectteams.otdt.ui.tests.refactoring.pushdown;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;

/**
 * Copy of {@link org.eclipse.jdt.ui.tests.refactoring.PushDownTests}.
 * 
 * @author Johannes Gebauer
 * 
 */
public class PushDownTests extends RefactoringTest {

	private static final Class clazz= PushDownTests.class;
	
	private static final String REFACTORING_PATH= "PushDown/";

	public PushDownTests(String name) {
		super(name);
	}
	
	protected String getRefactoringPath() {
		return REFACTORING_PATH;
	}
	

	public static Test suite() {
		return new MySetup(new TestSuite(PushDownTests.class));
	}

	private void performPullUp_pass(String[] cuNames, String[] methodNames, String[][] signatures, String[] fieldNames, String nameOfDeclaringType)
			throws Exception {
		ICompilationUnit[] cus = createCUs(cuNames);
		try {

			IType declaringType = getType(cus[0], nameOfDeclaringType);
			IMethod[] methods = getMethods(declaringType, methodNames, signatures);
			IField[] fields = getFields(declaringType, fieldNames);
			IMember[] members = merge(methods, fields);

			PushDownRefactoringProcessor processor = createRefactoringProcessor(members);
			Refactoring ref = processor.getRefactoring();

			assertTrue("activation", ref.checkInitialConditions(new NullProgressMonitor()).isOK());

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
	
	private void performPullUp_fail(String[] cuNames, String[] methodNames, String[][] signatures, String[] fieldNames, String nameOfDeclaringType)
			throws Exception {
		ICompilationUnit[] cus = createCUs(cuNames);
		try {

			IType declaringType = getType(cus[0], nameOfDeclaringType);
			IMethod[] methods = getMethods(declaringType, methodNames, signatures);
			IField[] fields = getFields(declaringType, fieldNames);
			IMember[] members = merge(methods, fields);

			PushDownRefactoringProcessor processor = createRefactoringProcessor(members);
			Refactoring ref = processor.getRefactoring();

			assertTrue("activation", ref.checkInitialConditions(new NullProgressMonitor()).isOK());
			
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
	
	private static PushDownRefactoringProcessor createRefactoringProcessor(IMember[] methods) throws JavaModelException {
		IJavaProject project = null;
		if (methods != null && methods.length > 0)
			project = methods[0].getJavaProject();
		if (RefactoringAvailabilityTester.isPullUpAvailable(methods)) {
			PushDownRefactoringProcessor processor = new PushDownRefactoringProcessor(methods);
			new ProcessorBasedRefactoring(processor);
			return processor;
		}
		return null;
	}
	
	private ICompilationUnit[] createCUs(String[] cuNames) throws Exception {
		ICompilationUnit[] cus = new ICompilationUnit[cuNames.length];

		for (int idx = 0; idx < cuNames.length; idx++) {
			Assert.isNotNull(cuNames[idx]);
			cus[idx] = createCUfromTestFile(getPackageP(), cuNames[idx]);
		}
		return cus;
	}
	
	
	//--------------------------------------------------------
	public void testPushDownMethodToImplicitSubclass() throws Exception {
		performPullUp_pass(new String[] { "TSuper", "T", "B" }, new String[] { "rm" }, new String[][] { new String[0] }, new String[] {}, "R");
	}
	
	public void testDeclaringTypeHasADirectPhantomSubRole() throws Exception {
		performPullUp_fail(new String[] { "TSuper", "T1", "T2" }, new String[] { "rm" }, new String[][] { new String[0] }, new String[] {}, "R");
	}
	
	public void testPushedDownMethodReferencedInMethodBinding1() throws Exception {
		performPullUp_fail(new String[] { "B1", "B2", "T" }, new String[] { "bm" }, new String[][] { new String[0] }, new String[] {}, "B1");
	}
	
	public void testPushedDownMethodReferencedInMethodBinding2() throws Exception {
		performPullUp_fail(new String[] { "B1", "B2", "T" }, new String[] { "bm" }, new String[][] { new String[0] }, new String[] {}, "B1");
	}
	
	public void testPushedDownMethodReferencedInMethodBinding3() throws Exception {
		performPullUp_fail(new String[] { "T1", "T2", "B" }, new String[] { "rm" }, new String[][] { new String[0] }, new String[] {}, "Role");
	}	
	
	public void testPushedDownMethodIsReferencedInRegularGuard1() throws Exception {
		performPullUp_fail(new String[] { "T", "B" }, new String[] { "b" }, new String[][] { new String[0] }, new String[] {}, "R1");
	}	
	
	public void testPushedDownMethodIsReferencedInRegularGuard2() throws Exception {
		performPullUp_fail(new String[] { "T", "B" }, new String[] { "b" }, new String[][] { new String[0] }, new String[] {}, "R1");
	}	
	
	public void testPushedDownMethodIsReferencedInBaseGuard1() throws Exception {
		performPullUp_fail(new String[] { "B1", "B2", "T" }, new String[] { "b" }, new String[][] { new String[0] }, new String[] {}, "B1");
	}
	
	public void testPushedDownMethodIsReferencedInBaseGuard2() throws Exception {
		performPullUp_fail(new String[] { "B1", "B2", "T" }, new String[] { "b" }, new String[][] { new String[0] }, new String[] {}, "B1");
	}

	public void testPushedDownMethodIsReferencedInBaseGuard3() throws Exception {
		performPullUp_fail(new String[] { "B1", "B2", "T" }, new String[] { "b" }, new String[][] { new String[0] }, new String[] {}, "B1");
	}
	
	public void testPushedDownMethodIsReferencedInParameterMapping1() throws Exception {
		performPullUp_fail(new String[] { "T", "B" }, new String[] { "f" }, new String[][] { new String[0] }, new String[] {}, "R1");
	}

	public void testPushedDownMethodIsReferencedInParameterMapping2() throws Exception {
		performPullUp_fail(new String[] { "T", "B" }, new String[] { "f" }, new String[][] { new String[0] }, new String[] {}, "R1");
	}

	// See Ticket #286
	public void _testPushedDownMethodIsReferencedInParameterMapping3() throws Exception {
		performPullUp_fail(new String[] { "T", "B" }, new String[] { "f" }, new String[][] { new String[0] }, new String[] {}, "R1");
	}
	
	/* Push Down Field Tests */
	public void testPushDownFieldToImplicitSubclass() throws Exception {
		performPullUp_pass(new String[] { "TSuper", "T", "B" }, new String[] {}, new String[][] { new String[0] }, new String[] { "f" }, "R");
	}
	
	public void testPushedDownFieldReferencedInCalloutToField() throws Exception {
		performPullUp_fail(new String[] { "B1", "B2", "T" }, new String[] {}, new String[][] { new String[0] }, new String[] { "f" }, "B1");
	}
	
	public void testPushDownTypeAnchorInstance() throws Exception {
		performPullUp_fail(new String[] { "T1", "T2", "T3" }, new String[] {}, new String[][] { new String[0] }, new String[] { "anchor" }, "T1");
	}
	
	public void testPushedDownFieldWouldShadowAnInheritedField() throws Exception {
		performPullUp_fail(new String[] { "B", "T1", "T2" }, new String[] {}, new String[][] { new String[0] }, new String[] { "f" }, "B");
	}
	
	public void testPushedDownFieldIsReferencedInRegularGuard1() throws Exception {
		performPullUp_fail(new String[] { "T", "B" }, new String[] {}, new String[][] { new String[0] }, new String[] { "b" }, "R1");
	}

	public void testPushedDownFieldIsReferencedInRegularGuard2() throws Exception {
		performPullUp_fail(new String[] { "T", "B" }, new String[] {}, new String[][] { new String[0] }, new String[] { "b" }, "R1");
	}

	public void testPushedDownFieldIsReferencedInBaseGuard1() throws Exception {
		performPullUp_fail(new String[] { "B1", "B2", "T" }, new String[] {}, new String[][] { new String[0] }, new String[] { "b" }, "B1");
	}

	public void testPushedDownFieldIsReferencedInBaseGuard2() throws Exception {
		performPullUp_fail(new String[] { "B1", "B2", "T" }, new String[] {}, new String[][] { new String[0] }, new String[] { "b" }, "B1");
	}

	public void testPushedDownFieldIsReferencedInBaseGuard3() throws Exception {
		performPullUp_fail(new String[] { "B1", "B2", "T" }, new String[] {}, new String[][] { new String[0] }, new String[] { "b" }, "B1");
	}
	
	public void testPushedDownFieldIsReferencedInParameterMapping1() throws Exception {
		performPullUp_fail(new String[] { "T", "B" }, new String[] {}, new String[][] { new String[0] }, new String[] { "f" }, "R1");
	}
	
	public void testPushedDownFieldIsReferencedInParameterMapping2() throws Exception {
		performPullUp_fail(new String[] { "T", "B" }, new String[] {}, new String[][] { new String[0] }, new String[] { "f" }, "R1");
	}
	
	public void testOverrideImplicitInheritedMethod() throws Exception {
		performPullUp_fail(new String[] { "A", "T1", "T2" }, new String[] { "m" }, new String[][] { new String[0] }, new String[] {}, "A");
	}
	
	public void testOverrideExplicitInheritedMethod() throws Exception {
		performPullUp_pass(new String[] { "T2", "T1", "A" }, new String[] { "m" }, new String[][] { new String[0] }, new String[] {}, "R");
	}
	
	// See Ticket #286
	public void _testPushedDownFieldIsReferencedInParameterMapping3() throws Exception {
		performPullUp_fail(new String[] { "T", "B" }, new String[] {}, new String[][] { new String[0] }, new String[] { "f" }, "R1");
	}

}
