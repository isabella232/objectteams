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
package org.eclipse.objectteams.otdt.ui.tests.refactoring.inlinecallin;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.core.CallinMapping;
import org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.inlinecallin.InlineCallinRefactoring;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;

/**
 * 
 * @author Johannes Gebauer
 */
@SuppressWarnings("restriction")
public class InlineCallinTests extends RefactoringTest {
	private static final String REFACTORING_PATH = "InlineCallin/";

	public InlineCallinTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new MySetup(new TestSuite(InlineCallinTests.class));
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

	private static InlineCallinRefactoring createInlineCallinRefactoring(IMethod roleMethod, ICallinMapping[] callinMappings, IMethod[] baseMethods)
			throws JavaModelException {
		IJavaProject project = null;
		if (callinMappings[0] != null) {
			project = callinMappings[0].getJavaProject();
			InlineCallinRefactoring refactoring = new InlineCallinRefactoring(roleMethod, callinMappings, baseMethods);
			return refactoring;
		}
		return null;
	}

	/**
	 * The <code>ICompilationUnit</code> containing the role that defines the
	 * <code>ICallinMapping</code> at position 0 and its base at position 1.
	 * 
	 */
	private void performInlineCallin_pass(String[] cuNames, String roleMethodName, String[] roleMethodSignature, String[] baseMethodNames, String[][] baseMethodSignatures,
			int[] mappingKinds, boolean deleteRoleMethod, String roleName, String baseName) throws Exception {
		ICompilationUnit[] cus = createCUs(cuNames);
		try {

			IType roleType = getType(cus[0], roleName);
			IType baseType = getType(cus[1], baseName);

			IMethod roleMethod = roleType.getMethod(roleMethodName, roleMethodSignature);
			assertTrue("role method " + roleMethod.getElementName() + " does not exist", roleMethod.exists());

			IMethod[] baseMethods = new IMethod[baseMethodNames.length];
			for (int i = 0; i < baseMethods.length; i++) {
				baseMethods[i] = baseType.getMethod(baseMethodNames[i], baseMethodSignatures[i]);
				assertTrue("base method " + baseMethods[i].getElementName() + " does not exist", baseMethods[i].exists());

			}
			
			ICallinMapping[] mappings = new ICallinMapping[baseMethodNames.length];
			for (int i = 0; i < mappings.length; i++) {
				mappings[i] = getCallinMethodMapping(roleType, roleMethod, baseMethods[i], mappingKinds[i]);
				assertNotNull("the described callin method mapping does not exist", mappings[i]);
			}

			InlineCallinRefactoring ref = createInlineCallinRefactoring(roleMethod, mappings, baseMethods);

			assertTrue("activation", ref.checkInitialConditions(new NullProgressMonitor()).isOK());

			ref.setDeleteRoleMethod(deleteRoleMethod);
	
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
	 * The <code>ICompilationUnit</code> containing the role that defines the
	 * <code>ICallinMapping</code> at position 0 and its base at position 1.
	 * 
	 */
	private void performInlineCallin_failing(String[] cuNames, String roleMethodName, String[] roleMethodSignature, String[] baseMethodNames,
			String[][] baseMethodSignatures, int[] mappingKinds, boolean deleteRoleMethod, String roleName, String baseName) throws Exception {
		ICompilationUnit[] cus = createCUs(cuNames);
		try {

			IType roleType = getType(cus[0], roleName);
			IType baseType = getType(cus[1], baseName);

			IMethod roleMethod = roleType.getMethod(roleMethodName, roleMethodSignature);
			assertTrue("role method " + roleMethod.getElementName() + " does not exist", roleMethod.exists());

			IMethod[] baseMethods = new IMethod[baseMethodNames.length];
			for (int i = 0; i < baseMethods.length; i++) {
				baseMethods[i] = baseType.getMethod(baseMethodNames[i], baseMethodSignatures[i]);
				assertTrue("base method " + baseMethods[i].getElementName() + " does not exist", baseMethods[i].exists());

			}

			ICallinMapping[] mappings = new ICallinMapping[baseMethodNames.length];
			for (int i = 0; i < mappings.length; i++) {
				mappings[i] = getCallinMethodMapping(roleType, roleMethod, baseMethods[i], mappingKinds[i]);
				assertNotNull("the described callin method mapping does not exist", mappings[i]);
			}

			InlineCallinRefactoring ref = createInlineCallinRefactoring(roleMethod, mappings, baseMethods);

			assertTrue("activation", ref.checkInitialConditions(new NullProgressMonitor()).isOK());

			ref.setDeleteRoleMethod(deleteRoleMethod);
			
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

	private ICallinMapping getCallinMethodMapping(IType roleType, IMethod roleMethod, IMethod baseMethod, int mappingKind) throws JavaModelException {
		IRoleType role = (IRoleType) OTModelManager.getOTElement(roleType);
		assertNotNull("type " + roleType.getElementName() + " is not a role", role);
		
		IMethodMapping[] mappings = role.getMethodMappings();
		
		for (int i = 0; i < mappings.length; i++) {
			if (mappings[i] instanceof ICallinMapping) {
				ICallinMapping mapping = (ICallinMapping) mappings[i];
				
				for (int j = 0; j < mapping.getBoundBaseMethods().length; j++) {
					IMethod roleMethod2 = mapping.getRoleMethod();
					IMethod baseMethod2 = mapping.getBoundBaseMethods()[j];
					if (roleMethod2.equals(roleMethod) && baseMethod2.equals(baseMethod) && mapping.getCallinKind() == mappingKind) {
						return mapping;
					}
				}
			}
		}
		return null;
	}
	

	/******* tests ******************/

	/* Inline Callin Tests */
	
	public void testSimpleBeforeCallin() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] }, new int[] { CallinMapping.KIND_BEFORE }, false, "R", "B");
	}

	public void testSimpleAfterCallin() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_AFTER }, false, "R", "B");
	}

	public void testSimpleReplaceCallin() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] }, new int[] { CallinMapping.KIND_REPLACE }, false, "R", "B");
	}
	
	public void testAfterCallinWithReturn() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_AFTER }, false, "R", "B");
	}	
	
	public void testReplaceCallinResultTunneling1() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_REPLACE }, false, "R", "B");
	}

	public void testReplaceCallinResultTunneling2() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] }, new int[] { CallinMapping.KIND_REPLACE }, false, "R", "B");
	}
		
	public void testReplaceCallinWithReturn() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] }, new int[] { CallinMapping.KIND_REPLACE }, false, "R", "B");
	}
		
	public void testDifferentParamNames() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[] { "I" }, new String[] { "m" }, new String[][] { new String[] { "I" } }, new int[] { CallinMapping.KIND_REPLACE }, false, "R", "B");
	}	
	
	public void testDifferentParamLength() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[] { "I" }, new String[] { "m" }, new String[][] { new String[] { "I", "QString;" } }, new int[] { CallinMapping.KIND_REPLACE }, false, "R",
				"B");
	}	
	
	public void testDifferentParamLengthBeforeCallin() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[] { "I" }, new String[] { "m" }, new String[][] { new String[] { "I", "QString;" } }, new int[] { CallinMapping.KIND_BEFORE }, false, "R",
				"B");
	}	
	
	public void testSimpleParameterMapping() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[] { "I" }, new String[] { "m" }, new String[][] { new String[] { "I", "I" } },
				new int[] { CallinMapping.KIND_AFTER }, false, "R", "B");
	}

	public void testReplaceWithParameterMapping() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[] { "I" }, new String[] { "m" }, new String[][] { new String[] { "I", "I" } },
				new int[] { CallinMapping.KIND_REPLACE }, false, "R", "B");
	}
		
	public void testReplaceCallinWithMultipleReturns() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_REPLACE }, false, "R", "B");
	}
	
	public void testReplaceCallinWithParameter() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[] { "I", "QString;" }, new String[] { "m" }, new String[][] { new String[] { "I", "QString;" } }, new int[] { CallinMapping.KIND_REPLACE },
				false, "R", "B");
	}
	
	public void testCallinMappingWithSignature() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_AFTER }, false, "R", "B");
	}
	
	public void testMultipleBaseMethodCallin() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_AFTER }, false, "R", "B");
	}
	
	public void testRoleMethodReferencesCallout() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_AFTER }, false, "R", "B");
	}
	
	public void testRoleMethodReferencesCalloutToFieldGet() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_AFTER }, false, "R", "B");
	}
	
	public void testRoleMethodReferencesCalloutToFieldSet() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_AFTER }, false, "R", "B");
	}
	
	public void testParameterTunneling1() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[] { "I" } }, new int[] { CallinMapping.KIND_REPLACE }, false, "R", "B");
	}	
	
	public void testParameterTunneling2() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[] { "I" } }, new int[] { CallinMapping.KIND_REPLACE }, false, "R", "B");
	}	
	
	public void testInlineMultipleCallins() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m1", "m2" }, new String[][] { new String[0], new String[0] },
				new int[] { CallinMapping.KIND_AFTER, CallinMapping.KIND_AFTER }, false, "R", "B");
	}
	
	public void testDeleteRoleMethod() throws Exception {
		performInlineCallin_pass(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_REPLACE }, true, "R", "B");
	}
	
	public void testDeleteReferencedRoleMethod() throws Exception {
		performInlineCallin_failing(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_BEFORE }, true, "R", "B");
	}
	
	public void testInlineBoundCallin1() throws Exception {
		performInlineCallin_failing(new String[] { "T1", "B", "T2" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_REPLACE }, false, "R", "B");
	}
	
	public void testInlineBoundCallin2() throws Exception {
		performInlineCallin_failing(new String[] { "T1", "B", "T2" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_REPLACE }, false, "R", "B");
	}
	
	public void testDependencyToRole1() throws Exception {
		performInlineCallin_failing(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_BEFORE }, false, "R", "B");
	}
	
	public void testDependencyToRole2() throws Exception {
		performInlineCallin_failing(new String[] { "T", "B" }, "n", new String[0], new String[] { "m" }, new String[][] { new String[0] },
				new int[] { CallinMapping.KIND_BEFORE }, false, "R", "B");
	}
	
}
