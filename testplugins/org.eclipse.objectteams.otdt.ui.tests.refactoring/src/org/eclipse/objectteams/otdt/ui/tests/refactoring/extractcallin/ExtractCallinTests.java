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
package org.eclipse.objectteams.otdt.ui.tests.refactoring.extractcallin;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.objectteams.otdt.internal.core.CallinMapping;
import org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.extractcallin.ExtractCallinRefactoring;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;

/**
 * @author Johannes Gebauer
 * 
 */
@SuppressWarnings("restriction")
public class ExtractCallinTests extends RefactoringTest {
	private static final String REFACTORING_PATH = "ExtractCallin/";

	public ExtractCallinTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new MySetup(new TestSuite(ExtractCallinTests.class));
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

	private ExtractCallinRefactoring createExtractCallinRefactoring(IMethod baseMethod, IType role, int mappingKind) {
		ExtractCallinRefactoring refactoring = new ExtractCallinRefactoring(baseMethod, role, mappingKind);
		return refactoring;
	}

	private void performPullUp_pass(String[] cuNames, String baseMethodName, String[] baseMethodSignature, int mappingKind, boolean deleteRoleMethod,
			String baseName, String roleName, boolean copyBaseMethod) throws Exception {
		ICompilationUnit[] cus = createCUs(cuNames);
		try {

			IType baseType = getType(cus[0], baseName);
			IType roleType = getType(cus[1], roleName);

			IMethod baseMethod = baseType.getMethod(baseMethodName, baseMethodSignature);
			assertTrue("base method " + baseMethod.getElementName() + " does not exist", baseMethod.exists());

			ExtractCallinRefactoring ref = createExtractCallinRefactoring(baseMethod, roleType, mappingKind);

			assertTrue("activation", ref.checkInitialConditions(new NullProgressMonitor()).isOK());

			ref.setDeleteBaseMethod(deleteRoleMethod);
	
			ref.setCopyBaseMethod(copyBaseMethod);
			
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

	/******* tests ******************/

	/* Extract Callin Tests */
	
	public void testExtractSimpleBeforeCallin() throws Exception {
		performPullUp_pass(new String[] { "B", "T" }, "m", new String[0], CallinMapping.KIND_BEFORE, false, "B", "R", false);
	}
	
	public void testExtractSimpleReplaceCallin1() throws Exception {
		performPullUp_pass(new String[] { "B", "T" }, "m", new String[0], CallinMapping.KIND_REPLACE, false, "B", "R", true);
	}
	
	public void testExtractSimpleAfterCallin() throws Exception {
		performPullUp_pass(new String[] { "B", "T" }, "m", new String[0], CallinMapping.KIND_AFTER, false, "B", "R", false);
	}
	
	public void testExtractSimpleReplaceCallin2() throws Exception {
		performPullUp_pass(new String[] { "B", "T" }, "m", new String[0], CallinMapping.KIND_REPLACE, false, "B", "R", false);
	}
	
	public void testExtractCallinWithParameter() throws Exception {
		performPullUp_pass(new String[] { "B", "T" }, "m", new String[] { "I" }, CallinMapping.KIND_BEFORE, false, "B", "R", false);
	}
	
	public void testExtractCallinWithParameterMapping() throws Exception {
		performPullUp_pass(new String[] { "B", "T" }, "m", new String[] { "I", "I" }, CallinMapping.KIND_BEFORE, false, "B", "R", false);
	}
}
