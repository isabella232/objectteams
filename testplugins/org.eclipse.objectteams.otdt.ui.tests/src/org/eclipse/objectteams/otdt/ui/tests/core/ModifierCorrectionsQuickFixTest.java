/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Technical University Berlin - Initial API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.core;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal;

/**
 * Test OT-specific quickfixes (here: modifier corrections).
 * @author stephan
 * @since 1.2.1
 */
// structure OT_COPY_PASTE from {@link org.eclipse.jdt.ui.tests.quickfix.ModifierCorrectionsQuickFixTest}
public class ModifierCorrectionsQuickFixTest extends OTQuickFixTest {
	

	private static final Class THIS= ModifierCorrectionsQuickFixTest.class;


	public ModifierCorrectionsQuickFixTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return setUpTest(new TestSuite(THIS));
	}
	
	public static Test setUpTest(Test test) {
		return new ProjectTestSetup(test);
	}
	
	public static Test suite() {
		return allTests();
	}


	/* calling a non-public role constructor from the team. */
	public void testRoleCtorCalled1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R {\n");
		buf.append("        R(){}\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        R r = new R();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T.java", buf.toString(), false, null);

		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R {\n");
		buf.append("        protected R(){}\n");
		buf.append("    }\n");
		buf.append("    void foo() {\n");
		buf.append("        R r = new R();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	/* calling a non-public role constructor from a sibling role. */
	public void testRoleCtorCalled2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R {\n");
		buf.append("        R(){}\n");
		buf.append("    }\n");
		buf.append("    protected class R2 {\n");
		buf.append("        void foo() {\n");
		buf.append("            R r = new R();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T.java", buf.toString(), false, null);

		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R {\n");
		buf.append("        protected R(){}\n");
		buf.append("    }\n");
		buf.append("    protected class R2 {\n");
		buf.append("        void foo() {\n");
		buf.append("            R r = new R();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/* calling a non-public role constructor from a sibling role file. */
	public void testRoleCtorCalled3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		IPackageFragment pack1T= fSourceFolder.createPackageFragment("test1.T", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R {\n");
		buf.append("        R(){}\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		buf = new StringBuffer();
		buf.append("team package test1.T;\n");
		buf.append("protected class R2 {\n");
		buf.append("    void foo() {\n");
		buf.append("        R r = new R();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1T.createCompilationUnit("R2.java", buf.toString(), false, null);

		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R {\n");
		buf.append("        protected R(){}\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	/* a non-public constructor of a role file is called from a sibling role. */
	public void testRoleCtorCalled4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		IPackageFragment pack1T= fSourceFolder.createPackageFragment("test1.T", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("team package test1.T;\n");
		buf.append("protected class R2 {\n");
		buf.append("    R2(){}\n");
		buf.append("}\n");
		pack1T.createCompilationUnit("R2.java", buf.toString(), false, null);
		
		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R {\n");
		buf.append("        void foo() {\n");
		buf.append("            R2 r = new R2();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T.java", buf.toString(), false, null);

		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("team package test1.T;\n");
		buf.append("protected class R2 {\n");
		buf.append("    protected R2(){}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/* calling a non-public role constructor as externalized. */
	public void testRoleCtorCalled5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    public class R {\n");
		buf.append("        R(){}\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Main {\n");
		buf.append("    void foo(final T t) {\n");
		buf.append("        R<@t> r = t.new R();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Main.java", buf.toString(), false, null);

		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot, 2, null);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    public class R {\n");
		buf.append("        public R(){}\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/* calling a non-public role method on externalized. */
	public void testRoleMethodCalled() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    public class R {\n");
		buf.append("        void bar(){}\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Main {\n");
		buf.append("    void foo(final T t, R<@t> r) {\n");
		buf.append("        r.bar();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Main.java", buf.toString(), false, null);

		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    public class R {\n");
		buf.append("        public void bar(){}\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	public void testCalloutToPrivate() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class BaseBar {\n");
		buf.append("    private int foo() { return 0; }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("BaseBar.java", buf.toString(), false, null);
		
		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class TeamFoo {\n");
		buf.append("    protected class R playedBy BaseBar {\n");
		buf.append("    	@SuppressWarnings(\"decapsulation\")\n");
		buf.append("		int goo() -> int foo();\n");
		buf.append("	}\n");
		buf.append("	void t(R r) {\n");
		buf.append("		int j = r.goo();\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("TeamFoo.java", buf.toString(), false, null);

		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class TeamFoo {\n");
		buf.append("    protected class R playedBy BaseBar {\n");
		buf.append("    	@SuppressWarnings(\"decapsulation\")\n");
		buf.append("        protected\n");
		buf.append("		int goo() -> int foo();\n");
		buf.append("	}\n");
		buf.append("	void t(R r) {\n");
		buf.append("		int j = r.goo();\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	
	public void testCalloutViaExternalized() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class BaseBar {\n");
		buf.append("    int foo() { return 0; }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("BaseBar.java", buf.toString(), false, null);
		
		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class TeamFoo {\n");
		buf.append("    public class R playedBy BaseBar {\n");
		buf.append("		int goo() -> int foo();\n");
		buf.append("	}\n");
		buf.append("	void t(final TeamFoo t, R<@t> r) {\n");
		buf.append("		int j = r.goo();\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("TeamFoo.java", buf.toString(), false, null);

		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class TeamFoo {\n");
		buf.append("    public class R playedBy BaseBar {\n");
		buf.append("		public int goo() -> int foo();\n");
		buf.append("	}\n");
		buf.append("	void t(final TeamFoo t, R<@t> r) {\n");
		buf.append("		int j = r.goo();\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/** @SuppressWarnings("bindingconventions") was added a wrong location. */
	public void testSuppressWarnings1() throws Exception {
		IPackageFragment pack1 = fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class BaseBar {\n");
		buf.append("    int foo() { return 0; }\n");
		buf.append("}\n");
		pack1.createCompilationUnit("BaseBar.java", buf.toString(), false, null);
		
		IPackageFragment pack2 = fSourceFolder.createPackageFragment("test2", false, null);
		buf = new StringBuffer();
		buf.append("package test2;\n");
		buf.append("import test1.BaseBar;\n");
		buf.append("public team class TeamFoo {\n");
		buf.append("    public class R playedBy BaseBar {\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack2.createCompilationUnit("TeamFoo.java", buf.toString(), false, null);

		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test2;\n");
		buf.append("import test1.BaseBar;\n");
		buf.append("public team class TeamFoo {\n");
		buf.append("    @SuppressWarnings(\"bindingconventions\")\n");
		buf.append("    public class R playedBy BaseBar {\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
}
