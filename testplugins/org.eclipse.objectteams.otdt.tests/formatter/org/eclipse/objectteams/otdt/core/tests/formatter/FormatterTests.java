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
 * $Id: FormatterTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.tests.formatter;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.Test;

import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.objectteams.otdt.tests.AbstractJavaModelTests;
import org.eclipse.text.edits.TextEdit;

/** 
 * Test infra-structure copied from org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public class FormatterTests extends AbstractJavaModelTests {
		
	public static final int UNKNOWN_KIND = 0;
	public static final String IN = "_in";
	public static final String OUT = "_out";
	public static final boolean DEBUG = false;
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private long time;
	
	static {
//		TESTS_NUMBERS = new int[] { 620 } ;
	}
	public static Test suite() {
		return buildModelTestSuite(FormatterTests.class);
	}

	public FormatterTests(String name) {
		super(name);
	}
	
	/**
	 * Returns the OS path to the directory that contains this plugin.
	 */
	protected String getPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.eclispe.objectteams.otdt.tests").getEntry("/");
			return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getSourceWorkspacePath() {
		return getPluginDirectoryPath() +  java.io.File.separator + "workspace";
	}
	
	private String runFormatter(CodeFormatter codeFormatter, String source, int kind, int indentationLevel, int offset, int length, String lineSeparator) {
//		long time = System.currentTimeMillis();
		TextEdit edit = codeFormatter.format(kind, source, offset, length, indentationLevel, lineSeparator);//$NON-NLS-1$
//		System.out.println((System.currentTimeMillis() - time) + " ms");
		if (edit == null) return null;
//		System.out.println(edit.getChildrenSize() + " edits");
		String result = org.eclipse.jdt.internal.core.util.Util.editedString(source, edit);

		if (length == source.length()) {
//			time = System.currentTimeMillis();
			edit = codeFormatter.format(kind, result, 0, result.length(), indentationLevel, lineSeparator);//$NON-NLS-1$
//			System.out.println((System.currentTimeMillis() - time) + " ms");
			if (edit == null) return null;
//			assertEquals("Should not have edits", 0, edit.getChildren().length);
			final String result2 = org.eclipse.jdt.internal.core.util.Util.editedString(result, edit);
			if (!result.equals(result2)) {
				assertSourceEquals("Different reformatting", Util.convertToIndependantLineDelimiter(result), Util.convertToIndependantLineDelimiter(result2));
			}
		}
		return result;
	}

	/**
	 * Create project and set the jar placeholder.
	 */
	public void setUpSuite() throws Exception {
		// ensure autobuilding is turned off
		IWorkspaceDescription description = getWorkspace().getDescription();
		if (description.isAutoBuilding()) {
			description.setAutoBuilding(false);
			getWorkspace().setDescription(description);
		}
		setUpJavaProject("Formatter", "1.5"); //$NON-NLS-1$ $NON-NLS-2$
		if (DEBUG) {
			this.time = System.currentTimeMillis();
		}
	}	

	/**
	 * Reset the jar placeholder and delete project.
	 */
	public void tearDownSuite() throws Exception {
		this.deleteProject("Formatter"); //$NON-NLS-1$
		if (DEBUG) {
			System.out.println("Time spent = " + (System.currentTimeMillis() - this.time));//$NON-NLS-1$
		}
		super.tearDown();
	}	

	private String getIn(String compilationUnitName) {
		assertNotNull(compilationUnitName);
		int dotIndex = compilationUnitName.indexOf('.');
		assertTrue(dotIndex != -1);
		return compilationUnitName.substring(0, dotIndex) + IN + compilationUnitName.substring(dotIndex);
	}
	
	private String getOut(String compilationUnitName) {
		assertNotNull(compilationUnitName);
		int dotIndex = compilationUnitName.indexOf('.');
		assertTrue(dotIndex != -1);
		return compilationUnitName.substring(0, dotIndex) + OUT + compilationUnitName.substring(dotIndex);
	}

	private void assertLineEquals(String actualContents, String originalSource, String expectedContents, boolean checkNull) {
		if (actualContents == null) {
			assertTrue("actualContents is null", checkNull);
			assertEquals(expectedContents, originalSource);
			return;
		}
		assertSourceEquals("Different source", Util.convertToIndependantLineDelimiter(expectedContents), actualContents);
	}

	private void runTest(String packageName, String compilationUnitName) {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getEclipse21Settings());
		preferences.number_of_empty_lines_to_preserve = 0;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, packageName, compilationUnitName, CodeFormatter.K_COMPILATION_UNIT, 0);
	}

	private void runTest(CodeFormatter codeFormatter, String packageName, String compilationUnitName, int kind, int indentationLevel) {
		runTest(codeFormatter, packageName, compilationUnitName, kind, indentationLevel, false, 0, -1);
	}
	private void runTest(CodeFormatter codeFormatter, String packageName, String compilationUnitName, int kind, int indentationLevel, boolean checkNull, int offset, int length) {
		runTest(codeFormatter, packageName, compilationUnitName, kind, indentationLevel, checkNull, offset, length, null);
	}

	private void runTest(CodeFormatter codeFormatter, String packageName, String compilationUnitName, int kind, int indentationLevel, boolean checkNull, int offset, int length, String lineSeparator) {
		try {
			ICompilationUnit sourceUnit = getCompilationUnit("Formatter" , "", packageName, getIn(compilationUnitName)); //$NON-NLS-1$ //$NON-NLS-2$
			String s = sourceUnit.getSource();
			assertNotNull(s);
			ICompilationUnit outputUnit = getCompilationUnit("Formatter" , "", packageName, getOut(compilationUnitName)); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(outputUnit);
			String result;
			if (length == -1) {
				result = runFormatter(codeFormatter, s, kind, indentationLevel, offset, s.length(), lineSeparator);
			} else {
				result = runFormatter(codeFormatter, s, kind, indentationLevel, offset, length, lineSeparator);
			}
			assertLineEquals(result, s, outputUnit.getSource(), checkNull);
		} catch (JavaModelException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	String getSource(ASTNode astNode, char[] source) {
		String result = new String(CharOperation.subarray(source, astNode.getStartPosition() + 1, astNode.getStartPosition() + astNode.getLength() - 1));
		if (result.endsWith("\\n")) {
			return result.substring(0, result.length() - 2) + LINE_SEPARATOR;
		}
		return result;
	}

	public void test001() {
		runTest("test001", "T1.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test002() {
		runTest("test002", "T1.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test003() {
		runTest("test003", "T1.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test004() {
		runTest("test004", "T1.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test005() {
		runTest("test005", "T1.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test006() {
		runTest("test006", "T1.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test007() {
		runTest("test007", "Class1.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
}
