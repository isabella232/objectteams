/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann.
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
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.core;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal;


/**
 * Testing corrections for missing precedence declarations.
 * @author stephan
 * @since 0.7.0
 */
public class PrecedenceQuickFixTest extends OTQuickFixTest {
	private static final Class THIS= PrecedenceQuickFixTest.class;

	public PrecedenceQuickFixTest(String name) {
		super(name);
	}

	@SuppressWarnings("unchecked")
	public static Test allTests() {
		return setUpTest(new TestSuite(THIS));
	}
	
	public static Test suite() {
		return allTests();
	}

	public static Test setUpTest(Test test) {
		return new ProjectTestSetup(test);
	}

	// QuickFix adds a binding-level precedence declaration,
	// one callin label already exists, other label is generated.
	public void testAddPlainBindingPrecedence() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B {\n");
		buf.append("    void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B {\n");
		buf.append("        void rm1() {}\n");
		buf.append("        void rm2() {}\n");
		buf.append("    	rm1 <- before foo;\n");
		buf.append("    	ci2: rm2 <- before foo;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B {\n");
		buf.append("        void rm1() {}\n");
		buf.append("        void rm2() {}\n");
		buf.append("    	callin1: rm1 <- before foo;\n");
		buf.append("    	ci2: rm2 <- before foo;\n");
		buf.append("\n");
		buf.append("        precedence callin1, ci2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	// QuickFix adds a binding-level "precedence after" declaration
	public void testAddAfterBindingPrecedence() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B {\n");
		buf.append("    void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B {\n");
		buf.append("        void rm1() {}\n");
		buf.append("        void rm2() {}\n");
		buf.append("    	rm1 <- after foo;\n");
		buf.append("    	ci2: rm2 <- after foo;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B {\n");
		buf.append("        void rm1() {}\n");
		buf.append("        void rm2() {}\n");
		buf.append("    	callin1: rm1 <- after foo;\n");
		buf.append("    	ci2: rm2 <- after foo;\n");
		buf.append("\n");
		buf.append("        precedence after callin1, ci2;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	// QuickFix adds a role-level precedence declaration
	public void testAddAfterRolePrecedence() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B {\n");
		buf.append("    void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B {\n");
		buf.append("        void rm1() {}\n");
		buf.append("    	rm1 <- after foo;\n");
		buf.append("    }\n");
		buf.append("    protected class R2 playedBy B {\n");
		buf.append("        void rm2() {}\n");
		buf.append("    	ci2: rm2 <- after foo;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T1 {\n");
		buf.append("\n");
		buf.append("    precedence after R, R2;\n");
		buf.append("    protected class R playedBy B {\n");
		buf.append("        void rm1() {}\n");
		buf.append("    	rm1 <- after foo;\n");
		buf.append("    }\n");
		buf.append("    protected class R2 playedBy B {\n");
		buf.append("        void rm2() {}\n");
		buf.append("    	ci2: rm2 <- after foo;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
}
