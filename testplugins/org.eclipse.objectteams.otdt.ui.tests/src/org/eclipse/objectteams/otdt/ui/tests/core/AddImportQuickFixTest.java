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
 * $Id: AddImportQuickFixTest.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.core;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.tests.core.ProjectTestSetup;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.Workbench;

public class AddImportQuickFixTest extends OTQuickFixTest {
	

	private static final Class THIS= AddImportQuickFixTest.class;


	public AddImportQuickFixTest(String name) {
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

	public void testChangeFQNToBaseImport1() throws Exception {
		// base:
		IPackageFragment pack1base= fSourceFolder.createPackageFragment("test.base1", false, null);
		StringBuffer buf= new StringBuffer();
		buf = new StringBuffer();
		buf.append("package test.base1;\n");
		buf.append("public class Base1 {\n");
		buf.append("}\n");
		pack1base.createCompilationUnit("Base1.java", buf.toString(), false, null);

		// team:
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test.import1", false, null);
		buf = new StringBuffer();
		buf.append("package test.import1;\n");	
		buf.append("public team class T {\n");
		buf.append("	protected class R2 playedBy test.base1.Base1 {\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= null;
		for (Object prop : proposals)
			if (((CUCorrectionProposal)prop).getRelevance() > 0)
				proposal = (CUCorrectionProposal)prop;
		assertNotNull("Need proposal with positive relevance", proposal);
		assertEquals("Wrong quickfix label", "\"Change qualified reference to using 'import base test...Base1;'\"", proposal.getDisplayString());
		
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test.import1;\n");
		buf.append("\n");	
		buf.append("import base test.base1.Base1;\n");
		buf.append("\n");	
		buf.append("public team class T {\n");
		buf.append("	protected class R2 playedBy Base1 {\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
		
	// ROFI variant:
	public void testChangeFQNToBaseImport2() throws Exception {
		// base:
		IPackageFragment pack1base= fSourceFolder.createPackageFragment("test.base1", false, null);
		StringBuffer buf= new StringBuffer();
		buf = new StringBuffer();
		buf.append("package test.base1;\n");
		buf.append("public class Base1 {\n");
		buf.append("}\n");
		pack1base.createCompilationUnit("Base1.java", buf.toString(), false, null);

		// team:
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test.import1", false, null);
		buf = new StringBuffer();
		buf.append("package test.import1;\n");	
		buf.append("public team class T {\n");
		buf.append("}\n");
		ICompilationUnit teamUnit = pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		// ROFI:
		IPackageFragment teamPack = fSourceFolder.createPackageFragment("test.import1.T", true, null);
		buf = new StringBuffer();
		buf.append("team package test.import1.T;\n");	
		buf.append("protected class R2 playedBy test.base1.Base1 {\n");
		buf.append("}\n");
		ICompilationUnit cu= teamPack.createCompilationUnit("R2.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= null;
		for (Object prop : proposals)
			if (((CUCorrectionProposal)prop).getRelevance() > 0)
				proposal = (CUCorrectionProposal)prop;
		assertNotNull("Need proposal with positive relevance", proposal);
		assertEquals("Wrong quickfix label", "\"Change qualified reference to using 'import base test...Base1;'\"", proposal.getDisplayString());
		
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("team package test.import1.T;\n");
		buf.append("protected class R2 playedBy Base1 {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

		// fetch and analyze team content which is not covered by the preview:
		IEditorPart part= JavaUI.openInEditor(teamUnit);
		assertNotNull("Failed to open team unit in editor", part);
		IDocument document = JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposal.apply(document);
		// not needed: teamUnit.makeConsistent(null);
		IImportDeclaration[] imports = teamUnit.getImports();
		assertNotNull("Expected an import", imports);
		assertEquals("Expected one import", 1, imports.length);
		assertEquals("Unexpected import", "test.base1.Base1", imports[0].getElementName());
		assertEquals("Expected base import", ExtraCompilerModifiers.AccBase, imports[0].getFlags());
		
		// drop editors whose content has been modified by apply(),
		// so that tearDown can delete files without asking the user:
		Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
	}

	/* Adding a base import to a team on behalf of a role file. */
	public void testAddBaseImportForRofi1() throws Exception {
		// base:
		IPackageFragment pack1base= fSourceFolder.createPackageFragment("test.base", false, null);
		StringBuffer buf= new StringBuffer();
		buf = new StringBuffer();
		buf.append("package test.base;\n");
		buf.append("protected class Base1 {\n");
		buf.append("}\n");
		pack1base.createCompilationUnit("Base1.java", buf.toString(), false, null);

		// team:
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test.import1", false, null);
		buf = new StringBuffer();
		buf.append("package test.import1;\n");	
		buf.append("public team class T {\n");
		buf.append("}\n");
		pack1.createCompilationUnit("T.java", buf.toString(), false, null);

		// role:
		IPackageFragment pack1T= fSourceFolder.createPackageFragment("test.import1.T", false, null);
		buf = new StringBuffer();
		buf.append("team package test.import1.T;\n");
		buf.append("protected class R2 playedBy Base1 {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1T.createCompilationUnit("R2.java", buf.toString(), false, null);

		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 6);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test.import1;\n");
		buf.append("\n");	
		buf.append("import base test.base.Base1;\n");
		buf.append("\n");	
		buf.append("public team class T {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	// this time files already have content
	public void testAddBaseImportForRofi2() throws Exception {
		StringBuffer buf;
		// base:
		IPackageFragment pack1base= fSourceFolder.createPackageFragment("test.base", false, null);
		buf = new StringBuffer();
		buf.append("package test.base;\n");
		buf.append("protected class Base1 {\n");
		buf.append("}\n");
		pack1base.createCompilationUnit("Base1.java", buf.toString(), false, null);

		// team:
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test.import1", false, null);
		buf = new StringBuffer();
		buf.append("package test.import1;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("import base java.awt.Window;\n");
		buf.append("\n");
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy Window;\n");
		buf.append("    \n");
		buf.append("    List l;\n");
		buf.append("    \n");
		buf.append("}\n");
		pack1.createCompilationUnit("T.java", buf.toString(), false, null);

		// role:
		IPackageFragment pack1T= fSourceFolder.createPackageFragment("test.import1.T", false, null);
		buf = new StringBuffer();
		buf.append("team package test.import1.T;\n");
		buf.append("protected class R2 playedBy Base1 {\n");
		buf.append("	void foo() {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1T.createCompilationUnit("R2.java", buf.toString(), false, null);

		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 6);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test.import1;\n");
		buf.append("\n");	
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("import base java.awt.Window;\n");
		buf.append("\n");
		buf.append("import base test.base.Base1;\n");
		buf.append("\n");
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy Window;\n");
		buf.append("    \n");
		buf.append("    List l;\n");
		buf.append("    \n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	// for role types rather than proposing an import change type to anchored:
	public void testChangeTypeToAnchored1() throws CoreException {
		StringBuffer buf;
		// team:
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test.import1", false, null);
		buf = new StringBuffer();
		buf.append("package test.import1;\n");
		buf.append("\n");
		buf.append("public team class T {\n");
		buf.append("    public class R {}\n");
		buf.append("}\n");
		pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		// client
		IPackageFragment pack2= fSourceFolder.createPackageFragment("test.import2", false, null);
		buf = new StringBuffer();
		buf.append("package test.import2;\n");
		buf.append("\n");
		buf.append("import test.import1.T;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    final T t = new T();\n");
		buf.append("    void test() {\n");
		buf.append("    	R r = t.new R();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack2.createCompilationUnit("C.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 7);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test.import2;\n");
		buf.append("\n");
		buf.append("import test.import1.T;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    final T t = new T();\n");
		buf.append("    void test() {\n");
		buf.append("    	R<@t> r = t.new R();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	// for role types rather than proposing an import change type to anchored (role file):
	public void testChangeTypeToAnchored2() throws CoreException {
		StringBuffer buf;
		// team:
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test.import1", false, null);
		buf = new StringBuffer();
		buf.append("package test.import1;\n");
		buf.append("\n");
		buf.append("/** @role R */\n");
		buf.append("public team class T {\n");
		buf.append("}\n");
		pack1.createCompilationUnit("T.java", buf.toString(), false, null);

		IPackageFragment teampack= fSourceFolder.createPackageFragment("test.import1.T", false, null);
		buf = new StringBuffer();
		buf.append("team package test.import1.T;\n");
		buf.append("\n");
		buf.append("public class R {}\n");
		teampack.createCompilationUnit("R.java", buf.toString(), false, null);
		
		// client
		IPackageFragment pack2= fSourceFolder.createPackageFragment("test.import2", false, null);
		buf = new StringBuffer();
		buf.append("package test.import2;\n");
		buf.append("\n");
		buf.append("import test.import1.T;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    final T t = new T();\n");
		buf.append("    void test() {\n");
		buf.append("    	R r = t.new R();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu = pack2.createCompilationUnit("C.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 7);
		assertCorrectLabels(proposals);
		
		CUCorrectionProposal proposal= (CUCorrectionProposal) proposals.get(0);
		String preview= getPreviewContent(proposal);

		buf= new StringBuffer();
		buf.append("package test.import2;\n");
		buf.append("\n");
		buf.append("import test.import1.T;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    final T t = new T();\n");
		buf.append("    void test() {\n");
		buf.append("    	R<@t> r = t.new R();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
}
