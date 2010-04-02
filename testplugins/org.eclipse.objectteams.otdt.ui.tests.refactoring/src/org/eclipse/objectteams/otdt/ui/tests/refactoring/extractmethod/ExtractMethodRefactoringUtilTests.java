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
package org.eclipse.objectteams.otdt.ui.tests.refactoring.extractmethod;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.Corext;
import org.eclipse.jdt.internal.corext.refactoring.ParameterInfo;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.internal.refactoring.adaptor.extractmethod.ExtractMethodAmbuguityMsgCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.adaptor.extractmethod.ExtractMethodOverloadingMsgCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.corext.base.OTRefactoringStatusCodes;
import org.eclipse.objectteams.otdt.internal.refactoring.util.RefactoringUtil;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.FileBasedRefactoringTest;

/**
 * @author brcan
 * 
 */
@SuppressWarnings( { "restriction", "nls" })
public class ExtractMethodRefactoringUtilTests extends FileBasedRefactoringTest {
	public static final String SQUARE_BRACKET_OPEN = "/*[*/";
	public static final int SQUARE_BRACKET_OPEN_LENGTH = SQUARE_BRACKET_OPEN.length();
	public static final String SQUARE_BRACKET_CLOSE = "/*]*/";
	public static final int SQUARE_BRACKET_CLOSE_LENGTH = SQUARE_BRACKET_CLOSE.length();

	// Regular classes
	private IType _a;
	private IType _b;
	private IType _c;
	private IType _q;
	private IType _s;

	// Team class
	private IType _t1;

	// Role classes
	private IRoleType _t2r1;
	private IRoleType _t3r1;
	private IRoleType _t3r2;

	private ExtractMethodRefactoring _refactoring;

	public ExtractMethodRefactoringUtilTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new Suite(ExtractMethodRefactoringUtilTests.class);
	}

	public void setUpSuite() throws Exception {
		setTestProjectDir("ExtractMethodRefactoringUtil");
		super.setUpSuite();

		_a = getType(getTestProjectDir(), "src", "roleAndBaseHierarchy1", "A");

		_b = getType(getTestProjectDir(), "src", "roleAndBaseHierarchy1", "B");

		_c = getType(getTestProjectDir(), "src", "roleAndBaseHierarchy1", "C");

		_q = getType(getTestProjectDir(), "src", "roleAndBaseHierarchy1", "Q");

		_s = getType(getTestProjectDir(), "src", "roleAndBaseHierarchy1", "S");

		_t1 = getType(getTestProjectDir(), "src", "roleAndBaseHierarchy1", "T1");

		_t2r1 = getRole(getTestProjectDir(), "src", "roleAndBaseHierarchy1", "T2", "R1");

		_t3r1 = getRole(getTestProjectDir(), "src", "roleAndBaseHierarchy1", "T3", "R1");

		_t3r2 = getRole(getTestProjectDir(), "src", "roleAndBaseHierarchy1", "T3", "R2");
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	private int[] getSelection(String source) {
		int start = -1;
		int end = -1;
		int includingStart = source.indexOf(SQUARE_BRACKET_OPEN);
		int excludingStart = source.indexOf(SQUARE_BRACKET_CLOSE);
		int includingEnd = source.lastIndexOf(SQUARE_BRACKET_CLOSE);
		int excludingEnd = source.lastIndexOf(SQUARE_BRACKET_OPEN);

		if (includingStart > excludingStart && excludingStart != -1) {
			includingStart = -1;
		} else if (excludingStart > includingStart && includingStart != -1) {
			excludingStart = -1;
		}

		if (includingEnd < excludingEnd) {
			includingEnd = -1;
		} else if (excludingEnd < includingEnd) {
			excludingEnd = -1;
		}

		if (includingStart != -1) {
			start = includingStart;
		} else {
			start = excludingStart + SQUARE_BRACKET_CLOSE_LENGTH;
		}

		if (excludingEnd != -1) {
			end = excludingEnd;
		} else {
			end = includingEnd + SQUARE_BRACKET_CLOSE_LENGTH;
		}

		assertTrue("Selection invalid", start >= 0 && end >= 0 && end >= start);

		int[] result = new int[] { start, end - start };
		// System.out.println("|"+ source.substring(result[0], result[0] +
		// result[1]) + "|");
		return result;
	}

	@SuppressWarnings("unchecked")
	// raw List from ExtractMethodRefactoring.
	private String[] fetchNewParameterTypes() {
		List parameterInfos = _refactoring.getParameterInfos();
		String[] parameterTypes = new String[parameterInfos.size()];
		for (int i = 0; i < parameterTypes.length; i++)
			parameterTypes[i] = ((ParameterInfo) parameterInfos.get(i)).getNewTypeName();
		return parameterTypes;
	}

	/********** tests **********/
	// =====================================================================================
	// Testing overloading
	// =====================================================================================
	public void testCheckOverloading1() throws Exception {
		// focus type -> A
		ICompilationUnit cu = _a.getCompilationUnit();
		int[] selection = getSelection(cu.getSource());
		_refactoring = new ExtractMethodRefactoring(cu, selection[0], selection[1]);
		_refactoring.setMethodName("f");
		_refactoring.setVisibility(Modifier.PROTECTED);
		_refactoring.checkInitialConditions(new NullProgressMonitor());
		IMethod[] inheritedMethods = TypeHelper.getInheritedMethods(_a, true, true, true, null);
		RefactoringStatus expected = RefactoringUtil.addOverloadingWarning(new ExtractMethodOverloadingMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkOverloading(inheritedMethods, _refactoring.getMethodName(), fetchNewParameterTypes(),
				new ExtractMethodOverloadingMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
	}

	public void testCheckOverloading2() throws Exception {
		// focus type -> B
		ICompilationUnit cu = _b.getCompilationUnit();
		int[] selection = getSelection(cu.getSource());
		_refactoring = new ExtractMethodRefactoring(cu, selection[0], selection[1]);
		_refactoring.setMethodName("g");
		_refactoring.setVisibility(Modifier.PROTECTED);
		_refactoring.checkInitialConditions(new NullProgressMonitor());
		IMethod[] inheritedMethods = TypeHelper.getInheritedMethods(_b, true, true, true, null);
		RefactoringStatus expected = RefactoringUtil.addOverloadingWarning(new ExtractMethodOverloadingMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkOverloading(inheritedMethods, _refactoring.getMethodName(), fetchNewParameterTypes(),
				new ExtractMethodOverloadingMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
	}

	public void testCheckOverloading3() throws Exception {
		// focus type -> C
		ICompilationUnit cu = _c.getCompilationUnit();
		int[] selection = getSelection(cu.getSource());
		_refactoring = new ExtractMethodRefactoring(cu, selection[0], selection[1]);
		_refactoring.setMethodName("g");
		_refactoring.setVisibility(Modifier.PROTECTED);
		_refactoring.checkInitialConditions(new NullProgressMonitor());
		IMethod[] inheritedMethods = TypeHelper.getInheritedMethods(_c, true, true, true, null);
		RefactoringStatus expected = RefactoringUtil.addOverloadingWarning(new ExtractMethodOverloadingMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkOverloading(inheritedMethods, _refactoring.getMethodName(), fetchNewParameterTypes(),
				new ExtractMethodOverloadingMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
	}

	public void testCheckOverloading4() throws Exception {
		// focus type -> Q
		ICompilationUnit cu = _q.getCompilationUnit();
		int[] selection = getSelection(cu.getSource());
		_refactoring = new ExtractMethodRefactoring(cu, selection[0], selection[1]);
		_refactoring.setMethodName("m");
		_refactoring.setVisibility(Modifier.PROTECTED);
		_refactoring.checkInitialConditions(new NullProgressMonitor());
		IMethod[] inheritedMethods = TypeHelper.getInheritedMethods(_q, true, true, true, null);
		RefactoringStatus expected = RefactoringUtil.addOverloadingWarning(new ExtractMethodOverloadingMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkOverloading(inheritedMethods, _refactoring.getMethodName(), fetchNewParameterTypes(),
				new ExtractMethodOverloadingMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
	}

	public void testCheckOverloading5() throws Exception {
		// focus type -> S
		ICompilationUnit cu = _s.getCompilationUnit();
		int[] selection = getSelection(cu.getSource());
		_refactoring = new ExtractMethodRefactoring(cu, selection[0], selection[1]);
		_refactoring.setMethodName("m");
		_refactoring.setVisibility(Modifier.PROTECTED);
		_refactoring.checkInitialConditions(new NullProgressMonitor());
		IMethod[] inheritedMethods = TypeHelper.getInheritedMethods(_s, true, true, true, null);
		RefactoringStatus expected = RefactoringUtil.addOverloadingWarning(new ExtractMethodOverloadingMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkOverloading(inheritedMethods, _refactoring.getMethodName(), fetchNewParameterTypes(),
				new ExtractMethodOverloadingMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
	}

	public void testCheckOverloading6() throws Exception {
		// focus type -> T1
		ICompilationUnit cu = _t1.getCompilationUnit();
		int[] selection = getSelection(cu.getSource());
		_refactoring = new ExtractMethodRefactoring(cu, selection[0], selection[1]);
		_refactoring.setMethodName("m");
		_refactoring.setVisibility(Modifier.PROTECTED);
		_refactoring.checkInitialConditions(new NullProgressMonitor());
		IMethod[] inheritedMethods = TypeHelper.getInheritedMethods(_t1, true, true, true, null);
		RefactoringStatus expected = RefactoringUtil.addOverloadingWarning(new ExtractMethodOverloadingMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkOverloading(inheritedMethods, _refactoring.getMethodName(), fetchNewParameterTypes(),
				new ExtractMethodOverloadingMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
	}

	public void testCheckOverloading7() throws Exception {
		// focus type -> T2.R1
		ICompilationUnit cu = _t2r1.getCompilationUnit();
		int[] selection = getSelection(cu.getSource());
		_refactoring = new ExtractMethodRefactoring(cu, selection[0], selection[1]);
		_refactoring.setMethodName("m");
		_refactoring.setVisibility(Modifier.PROTECTED);
		_refactoring.checkInitialConditions(new NullProgressMonitor());
		IMethod[] inheritedMethods = TypeHelper.getInheritedMethods(_t2r1, true, true, true, null);
		RefactoringStatus expected = RefactoringUtil.addOverloadingWarning(new ExtractMethodOverloadingMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkOverloading(inheritedMethods, _refactoring.getMethodName(), fetchNewParameterTypes(),
				new ExtractMethodOverloadingMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
	}

	public void testCheckOverloading8() throws Exception {
		// focus type -> T3.R2
		ICompilationUnit cu = _t3r2.getCompilationUnit();
		int[] selection = getSelection(cu.getSource());
		_refactoring = new ExtractMethodRefactoring(cu, selection[0], selection[1]);
		_refactoring.setMethodName("rm");
		_refactoring.setVisibility(Modifier.PROTECTED);
		_refactoring.checkInitialConditions(new NullProgressMonitor());
		IMethod[] inheritedMethods = TypeHelper.getInheritedMethods(_t3r2, true, true, true, null);
		RefactoringStatus expected = RefactoringUtil.addOverloadingWarning(new ExtractMethodOverloadingMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkOverloading(inheritedMethods, _refactoring.getMethodName(), fetchNewParameterTypes(),
				new ExtractMethodOverloadingMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
	}

	// checking overloading including hidden (private) methods, because method
	// bindings can refer to otherwise hidden base methods (decapsulation)
	/* test overloading of private base method */
	public void testCheckOverloading9() throws Exception {
		// focus type -> C
		ICompilationUnit cu = _c.getCompilationUnit();
		int[] selection = getSelection(cu.getSource());
		_refactoring = new ExtractMethodRefactoring(cu, selection[0], selection[1]);
		_refactoring.setMethodName("n");
		_refactoring.setVisibility(Modifier.PRIVATE);
		_refactoring.checkInitialConditions(new NullProgressMonitor());
		IMethod[] inheritedMethods = TypeHelper.getInheritedMethods(_c, true, true, false, null);
		RefactoringStatus expected = RefactoringUtil.addOverloadingWarning(new ExtractMethodOverloadingMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkOverloading(inheritedMethods, _refactoring.getMethodName(), fetchNewParameterTypes(),
				new ExtractMethodOverloadingMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
	}

	/* test overloading of private role method */
	public void testCheckOverloading10() throws Exception {
		// focus type -> T3.R2
		ICompilationUnit cu = _t3r2.getCompilationUnit();
		int[] selection = getSelection(cu.getSource());
		_refactoring = new ExtractMethodRefactoring(cu, selection[0], selection[1]);
		_refactoring.setMethodName("k");
		_refactoring.setVisibility(Modifier.PRIVATE);
		_refactoring.checkInitialConditions(new NullProgressMonitor());
		IMethod[] inheritedMethods = TypeHelper.getInheritedMethods(_t3r2, true, true, false, null);
		RefactoringStatus expected = RefactoringUtil.addOverloadingWarning(new ExtractMethodOverloadingMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkOverloading(inheritedMethods, _refactoring.getMethodName(), fetchNewParameterTypes(),
				new ExtractMethodOverloadingMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING));
	}

	// =====================================================================================
	// Testing ambiguity
	// =====================================================================================

	public void testCheckAmbiguityInRoleMethodSpec1() throws Exception {
		// check ambiguous role method specifier in callin binding of T2.R1
		RefactoringStatus expected = RefactoringUtil.addAmbiguityFatalError(null, new ExtractMethodAmbuguityMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkForAmbiguousRoleMethodSpecs(_t2r1, "m", new ExtractMethodAmbuguityMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER));
	}

	public void testCheckAmbiguityInRoleMethodSpec2() throws Exception {
		// check ambiguous role method specifier in callin binding of T3.R1
		RefactoringStatus expected = RefactoringUtil.addAmbiguityFatalError(null, new ExtractMethodAmbuguityMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkForAmbiguousRoleMethodSpecs(_t3r1, "m", new ExtractMethodAmbuguityMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER));
	}

	public void testCheckAmbiguityInRoleMethodSpec3() throws Exception {
		// check ambiguous role method specifier in callout binding of T3.R2
		RefactoringStatus expected = RefactoringUtil.addAmbiguityFatalError(null, new ExtractMethodAmbuguityMsgCreator());
		RefactoringStatus actual = RefactoringUtil.checkForAmbiguousRoleMethodSpecs(_t3r2, "m", new ExtractMethodAmbuguityMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER));
	}

	public void testCheckAmbiguityInBaseMethodSpec1() throws Exception {
		// check ambiguous base method specifier in callin binding of T2.R1
		RefactoringStatus expected = RefactoringUtil.addAmbiguityFatalError(null, new ExtractMethodAmbuguityMsgCreator());
		ArrayList<IRoleType> boundRole = new ArrayList<IRoleType>();
		boundRole.add(_t2r1);
		RefactoringStatus actual = RefactoringUtil.checkForAmbiguousBaseMethodSpecs(boundRole, "f", "", new ExtractMethodAmbuguityMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER));
	}

	public void testCheckAmbiguityInBaseMethodSpec2() throws Exception {
		// check ambiguous base method specifier in callout binding of T3.R2
		RefactoringStatus expected = RefactoringUtil.addAmbiguityFatalError(null, new ExtractMethodAmbuguityMsgCreator());
		ArrayList<IRoleType> boundRole = new ArrayList<IRoleType>();
		boundRole.add(_t3r2);
		RefactoringStatus actual = RefactoringUtil.checkForAmbiguousBaseMethodSpecs(boundRole, "f", "", new ExtractMethodAmbuguityMsgCreator());
		assertEquals(expected.getSeverity(), actual.getSeverity());
		assertNotNull(expected.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER));
		assertNotNull(actual.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER));
	}
}
