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
 * $Id: CodeCompletionTest.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.core;

import static org.eclipse.jdt.internal.codeassist.RelevanceConstants.*;

import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal;
import org.eclipse.jdt.testplugin.TestOptions;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.tests.core.CoreTests;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.objectteams.otdt.internal.ui.assist.CompletionAdaptor;
import org.eclipse.objectteams.otdt.ui.tests.util.JavaProjectHelper;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests for code completion including the UI part 
 * i.e., testing the actual rewriting and also selections. 
 * 
 * @author stephan
 * @since 1.1.8
 */
public class CodeCompletionTest extends CoreTests {

	private static final int CALLOUT_BASE_RELEVANCE = R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_METHOD_OVERIDE+R_NON_RESTRICTED;
	private static final int INTERESTING_CALLIN_CALLOUT_PROPOSAL = CompletionAdaptor.R_METHOD_MAPPING * CALLOUT_BASE_RELEVANCE;
	private static final int VERY_INTERESTING_CALLIN_CALLOUT_PROPOSAL = CompletionAdaptor.R_METHOD_MAPPING * (CALLOUT_BASE_RELEVANCE+R_EXPECTED_TYPE);

	private static final Class<CodeCompletionTest> THIS= CodeCompletionTest.class;

	public static Test allTests() {
		return new ProjectTestSetup(new TestSuite(THIS));
	}

	public static Test setUpTest(Test test) {
		return new ProjectTestSetup(test);
	}

	@SuppressWarnings("unused") // dead code inside
	public static Test suite() {
		if (true) {
			return allTests();
		} else {
			TestSuite suite= new TestSuite();
			suite.addTest(new CodeCompletionTest("testOverrideRole3"));
			return new ProjectTestSetup(suite);
		}
	}

	private IJavaProject fJProject1;

	public CodeCompletionTest(String name) {
		super(name);
	}

	private void codeComplete(ICompilationUnit cu, int offset, CompletionProposalCollector collector) throws JavaModelException {
		waitBeforeCoreCompletion(); // ??
		cu.codeComplete(offset, collector);
	}

	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {
		fJProject1= JavaProjectHelper.createOTJavaProject("OTTestProject1", "bin");
		JavaProjectHelper.addRTJar(fJProject1);
		JavaProjectHelper.addRequiredProject(fJProject1, ProjectTestSetup.getProject());

		Hashtable<String, String> options= TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		options.put(JavaCore.CODEASSIST_FIELD_PREFIXES, "f");
		JavaCore.setOptions(options);

		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, true);
		store.setValue(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, false);
		store.setValue(PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS, false);

		StubUtility.setCodeTemplate(CodeTemplateContextType.OVERRIDECOMMENT_ID, "/* (non-Javadoc)\n * ${see_to_overridden}\n */", null); // TODO(SH): this one is currently used for method bindings, see TODO in CompletionAdaptor.Stubs#createCallout
		StubUtility.setCodeTemplate(CodeTemplateContextType.DELEGATECOMMENT_ID, "/* (non-Javadoc)\n * ${see_to_target}\n */", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "//TODO\n${body_statement}", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORCOMMENT_ID, "/**\n * Constructor.\n */", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODCOMMENT_ID, "/**\n * Method.\n */", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORSTUB_ID, "//TODO\n${body_statement}", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.GETTERCOMMENT_ID, "/**\n * @return the ${bare_field_name}\n */", fJProject1);
		StubUtility.setCodeTemplate(CodeTemplateContextType.SETTERCOMMENT_ID, "/**\n * @param ${param} the ${bare_field_name} to set\n */", fJProject1);
		
		fBeforeImports= "";
		fAfterImports= "";
		fMembers= "";
		fLocals= "";
		fTrigger= '\0';
		fWaitBeforeCompleting= false;
	}

	protected void tearDown() throws Exception {
		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setToDefault(PreferenceConstants.CODEGEN_ADD_COMMENTS);
		store.setToDefault(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS);
		store.setToDefault(PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS);
		closeAllEditors();
		JavaProjectHelper.delete(fJProject1);
	}
	
	public static void closeEditor(IEditorPart editor) {
		IWorkbenchPartSite site;
		IWorkbenchPage page;
		if (editor != null && (site= editor.getSite()) != null && (page= site.getPage()) != null)
			page.closeEditor(editor, false);
	}
	
	public static void closeAllEditors() {
		IWorkbenchWindow[] windows= PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int j= 0; j < pages.length; j++) {
				IEditorReference[] editorReferences= pages[j].getEditorReferences();
				for (int k= 0; k < editorReferences.length; k++)
					closeEditor(editorReferences[k].getEditor(false));
			}
		}
	}

	/* Legacy style of testing: */
	public void _testCreateCalloutCompletion() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		// create base class:
		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		String contents= buf.toString();

		pack1.createCompilationUnit("B.java", contents, false, null);
		
		// create team class:
		IPackageFragment pack2= sourceFolder.createPackageFragment("test2", false, null);
		buf= new StringBuffer();
		buf.append("package test2;\n");
		buf.append("import base test1.B;\n");
		buf.append("public team class T {\n");
		buf.append("    public class R playedBy B {\n");
		buf.append("        fo\n");
		buf.append("    }\n");
		buf.append("}\n");
		contents= buf.toString();

		ICompilationUnit cuT= pack2.createCompilationUnit("T.java", contents, false, null);
		
		fEditor= (JavaEditor)JavaUI.openInEditor(cuT);

		String str= "        fo";

		int offset= contents.indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cuT, offset);
		collector.setReplacementLength(0);
		codeComplete(cuT, offset, collector);
		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();

		assertTrue("proposals", proposals.length >= 1);
		IJavaCompletionProposal selected= null;
		for (IJavaCompletionProposal javaCompletionProposal : proposals) {
			if (javaCompletionProposal.getDisplayString().startsWith("foo()  void - Binding")) {
				selected= javaCompletionProposal;
				break;
			}
		}
		assertNotNull("expected proposal", selected);

		IDocument doc= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		selected.apply(doc);	

		buf= new StringBuffer();
		buf.append("package test2;\n" +
			"import base test1.B;\n" +
			"public team class T {\n" +
			"    public class R playedBy B {\n" +
			"\n" +
	        "        /* (non-Javadoc)\n" +
	        "         * @see test1.B#foo()\n" +
	        "         */\n" +
			"        void foo() -> void foo();\n" +
			"        \n" +
			"    }\n" +
			"}\n" +
			"");
		assertEqualString(doc.get(), buf.toString());
	}
	// helpers for above:
	private CompletionProposalCollector createCollector(ICompilationUnit cu, int offset) throws PartInitException, JavaModelException {
		CompletionProposalCollector collector= new CompletionProposalCollector(cu);
		collector.setInvocationContext(createContext(offset, cu));
		return collector;
	}

	private JavaContentAssistInvocationContext createContext(int offset, ICompilationUnit cu) throws PartInitException, JavaModelException {
		JavaEditor editor= (JavaEditor) JavaUI.openInEditor(cu);
		ISourceViewer viewer= editor.getViewer();
		return new JavaContentAssistInvocationContext(viewer, offset, editor);
	}
	
	// ==== START REAL TESTS: ====
	
	/** At the empty space within a role one could create a constructor. */
	public void testCreateRoleConstructor() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        |", 
				"R(", 
				"        /**\n" +
				"         * Constructor.\n" +
				"         */\n" +
				"        public R(B b) {\n" +
				"            //TODO\n" +
				"\n" +
				"        }",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);		
	}

	public void testCreateCallout1() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        fo|", 
				"foo(", 
				        "\n" + // TODO(SH): initial newline is not intended?
				"        /* (non-Javadoc)\n" +
				"         * @see test1.B#foo()\n" +
				"         */\n" +
				"        void |foo|() -> void foo();\n" +
				"        ",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
	// abstract method exists:
	public void testCreateCallout1a() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"	 	 abstract void foo();\n"+
				"        fo|", 
				"foo(", 
				"	 	 abstract void foo();\n"+
				        "\n" + // TODO(SH): initial newline is not intended?
				"        /* (non-Javadoc)\n" +
				"         * @see test1.B#foo()\n" +
				"         */\n" +
				"        void |foo|() -> void foo();\n" +
				"        ",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	// ROFI
	public void testCreateCallout2() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertROFIBodyProposal(
				"    fo|", 
				"foo(", 
				        "\n" + // TODO(SH): initial newline is not intended?
				"    /* (non-Javadoc)\n" +
				"     * @see test1.B#foo()\n" +
				"     */\n" +
				"    void |foo|() -> void foo();\n" +
				"    ",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
	
	public void testCreateCalloutOverride1() throws Exception {
		IPackageFragment pkg = CompletionTestSetup.getTestPackage("p1");
		pkg.createCompilationUnit("SuperTeam.java", 
				"package test1.p1;\n" +
				"public team class SuperTeam {\n" +
				"	public class MyRole {\n" +
				"		String blub(int i) { return \"\"; }\n" +
				"	}\n" + 
				"}\n",
				true, null);
		
		pkg.createCompilationUnit("ABase.java", 
				"package test1.p1;\n" +
				"public class ABase {\n" +
				"	String blub(int k) { return null; }\n" +
				"}\n", 
				true, null);
		
		StringBuffer subTeamContent = new StringBuffer(); 
		subTeamContent.append("package test1;\n");
		subTeamContent.append("import test1.p1.SuperTeam;\n");
		subTeamContent.append("import base test1.p1.ABase;\n");
		subTeamContent.append("public team class Completion_testCreateCalloutOverride1 extends SuperTeam {\n");
		subTeamContent.append("    protected class MyRole playedBy ABase {\n");
		subTeamContent.append("		\n");
		subTeamContent.append("    }\n");
		subTeamContent.append("}");
		
		StringBuffer expectedContent = new StringBuffer(); 
		expectedContent.append("package test1;\n");
		expectedContent.append("import test1.p1.SuperTeam;\n");
		expectedContent.append("import base test1.p1.ABase;\n");
		expectedContent.append("public team class Completion_testCreateCalloutOverride1 extends SuperTeam {\n");
		expectedContent.append("    protected class MyRole playedBy ABase {\n");
		expectedContent.append("\n");
		expectedContent.append("        /* (non-Javadoc)\n");
		expectedContent.append("         * @see test1.p1.ABase#blub(int)\n");
		expectedContent.append("         */\n");
		expectedContent.append("        String blub(int k) => String blub(int k);\n");
		expectedContent.append("		\n");
		expectedContent.append("    }\n");
		expectedContent.append("}");

		String completeAfter = "		";
		int pos = subTeamContent.indexOf(completeAfter)+completeAfter.length();
		int posAfter = expectedContent.indexOf("String");
		
		// discriminate from overriding "blub(int i)":
		assertProposal("blub(int k", null, null, subTeamContent, new Region(pos, 0), expectedContent, new Region(posAfter, "String".length()), 0); 
	}
	
	
	// callout override to field
	public void testCreateCalloutOverride2() throws Exception {
		IPackageFragment pkg = CompletionTestSetup.getTestPackage("p1");
		pkg.createCompilationUnit("SuperTeam.java", 
				"package test1.p1;\n" +
				"public team class SuperTeam {\n" +
				"	public class MyRole {\n" +
				"		void setBlub(int i) { }\n" +
				"	}\n" + 
				"}\n",
				true, null);
		
		pkg.createCompilationUnit("ABase.java", 
				"package test1.p1;\n" +
				"public class ABase {\n" +
				"	int blub;\n" +
				"}\n", 
				true, null);
		
		StringBuffer subTeamContent = new StringBuffer(); 
		subTeamContent.append("package test1;\n");
		subTeamContent.append("import test1.p1.SuperTeam;\n");
		subTeamContent.append("import base test1.p1.ABase;\n");
		subTeamContent.append("public team class Completion_testCreateCalloutOverride1 extends SuperTeam {\n");
		subTeamContent.append("    protected class MyRole playedBy ABase {\n");
		subTeamContent.append("		set\n");
		subTeamContent.append("    }\n");
		subTeamContent.append("}");
		
		StringBuffer expectedContent = new StringBuffer(); 
		expectedContent.append("package test1;\n");
		expectedContent.append("import test1.p1.SuperTeam;\n");
		expectedContent.append("import base test1.p1.ABase;\n");
		expectedContent.append("public team class Completion_testCreateCalloutOverride1 extends SuperTeam {\n");
		expectedContent.append("    protected class MyRole playedBy ABase {\n");
		expectedContent.append("\n");
		expectedContent.append("        /**\n");
		expectedContent.append("         * \n");
		expectedContent.append("         */\n");
		expectedContent.append("        void setBlub(int blub) => set int blub;\n");
		expectedContent.append("		\n");
		expectedContent.append("    }\n");
		expectedContent.append("}");

		String completeAfter = "		set";
		int pos = subTeamContent.indexOf(completeAfter)+completeAfter.length();
		int posAfter = expectedContent.indexOf("		");
		
		// discriminate from overriding "setBlub(int i)":
		assertProposal("setBlub(int)", null, null, subTeamContent, new Region(pos, 0), expectedContent, new Region(posAfter+2, 0), 0);  
	}
	

	public void testCompleteCallout1() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        void foo() ->|", 
				"foo(", 
				"        void foo() ->| void foo();|" + // SH: why selected?
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	public void testCompleteCallout2() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        foo -> |", 
				"foo(", 
				"        foo -> foo; |" + // SH: trailing space? 
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	/* short, no callinModifier, callin-method, follows: short callin. */
	public void testCompleteCallout3() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        callin void bar() {}\n"+
				"        void foo() ->|\n" +             // no leading space, inserted below (good)
				"        bar <- replace clone;\n",
				"foo(", 
				"        callin void bar() {}\n" + 
				"        void foo() ->| void foo();|\n" +
				"        bar <- replace clone;\n" +
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	/* short, prefix typed, follows: method */
	public void testCompleteCallout4() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        foo -> f|", 
				"foo(", 
				"        foo -> |foo;|",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	public void testCompleteCallout5() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"		|", 
				"foo(",
				"\n"+
		        "        /* (non-Javadoc)\n" +
		        "         * @see test1.B#foo()\n" +
		        "         */\n" +
				"        void |foo|() -> void foo();\n" +
				"		", 
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	public void testCompleteCallout6() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertROFIBodyProposal(
				"	|", 
				"foo(",
				"\n"+
		        "    /* (non-Javadoc)\n" +
		        "     * @see test1.B#foo()\n" +
		        "     */\n" +
				"    void |foo|() -> void foo();\n" +
				"	", 
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	// callout to static from ROFI
	public void testCompleteCallout7() throws Exception {
		createBaseClass("    public static void foo() {}\n");
		assertROFIBodyProposal(
				"	|", 
				"foo(",
				"\n"+
		        "    /* (non-Javadoc)\n" +
		        "     * @see test1.B#foo()\n" +
		        "     */\n" +
				"    void |foo|() -> void foo();\n" +
				"	", 
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	/** A base method spec with return type and beginning of the selector is searched. */
	public void testCompletionMethodSpecLong1() throws Exception {
		createBaseClass("");
		assertTypeBodyProposal(
	            "        String toString() => String toStr|",
	            "toString(",
	            "        String toString() => |String toString();|",
	            INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
	
		
	/** A base method spec without return type and beginning of the selector is searched. */
	public void testCompletionMethodSpecLong2() throws Exception {
		createBaseClass("");
		assertTypeBodyProposal(
	            "        String toString() => toStr|",
	            "toString(",
	            "        String toString() => |String toString();|",
	            INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
		
	/** A base method spec with return type and no selector is searched. */
	public void testCompletionMethodSpecLong3() throws Exception {
		createBaseClass("");
		assertTypeBodyProposal(
	            "        String toString() => String |",
	            "toString(",
	            "        String toString() => String toString(); |", // trailing space?
	            INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
		
	/* short, no callinModifier, follows: method */
	public void testCompleteCallin1() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        void bar() {}\n"+
				"        bar <-|\n" +                    // no space, inserted below (good)
				"        void zork() {}\n",
				"foo(", 
				"        void bar() {}\n" + 
				"        bar <- |before| foo;\n" +
				"        void zork() {}\n" +
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
	// concrete method exists, create full callin binding (cf. testCreateCallout1a()).
	public void testCreateCallin1a() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"	 	 void foo() {}\n"+
				"        fo|", 
				"foo(", 
				"	 	 void foo() {}\n"+
				        "\n" + // TODO(SH): initial newline is not intended?
				"        /* (non-Javadoc)\n" +
				"         * @see test1.B#foo()\n" +
				"         */\n" +
				"        void |foo|() <- before void foo();\n" +
				"        ",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	/* short, with callinModifier, follows: callout binding. */
	public void testCompleteCallin2() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        void bar() {}\n"+
				"        bar <- after |\n" + 
				"        void foo() -> void foo();\n",
				"foo(", 
				"        void bar() {}\n" + 
				"        bar <- after foo; |\n" +        // SH: trailing space?
				"        void foo() -> void foo();\n" +
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
	
	
	/* short, with callinModifier, follows: callout binding, non-keyword return type. */
	public void testCompleteCallin2a() throws Exception {
		createBaseClass("    public String foo() { return null; }\n");
		assertTypeBodyProposal(
				"        String bar() { return \"\"; }\n"+
				"        bar <- after |\n" + 
				"        String foo() -> String foo();\n",
				"foo(", 
				"        String bar() { return \"\"; }\n" + 
				"        bar <- after foo; |\n" +        // SH: trailing space?
				"        String foo() -> String foo();\n" +
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
	
	/* long, with callinModifier, follows: field. */
	public void testCompleteCallin3() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        void bar() {}\n"+
				"        void bar() <-after |\n" + // cannot insert space before modifier (OK)
				"        int i;\n",
				"foo(", 
				"        void bar() {}\n" + 
				"        void bar() <-after void foo(); |\n" + 
				"        int i;\n" +
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
	
	/* long, no callinModifier, callin-method, follows: field. */
	public void testCompleteCallin4() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        callin void bar() {}\n"+
				"        void bar() <- |\n" + 
				"        int i;\n",
				"foo(", 
				"        callin void bar() {}\n" + 
				"        void bar() <- |replace| void foo(); \n" +
				"        int i;\n" +
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
	
	/* long, no callinModifier, callin-method, follows: method. */
	public void testCompleteCallin5() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        callin void bar() {}\n"+
				"        void bar() <-|\n" +               // no leading space, inserted below (good)
				"        int zork() {}\n",
				"foo(", 
				"        callin void bar() {}\n" + 
				"        void bar() <- |replace| void foo();\n" +
				"        int zork() {}\n" +
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
	
	/* long, no callinModifier, callin-method, follows: short callin. */
	public void testCompleteCallin6() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        callin void bar() {}\n"+
				"        void bar() <-|\n" +               // no leading space, inserted below (good)
				"        bar <- replace clone;\n",
				"foo(", 
				"        callin void bar() {}\n" + 
				"        void bar() <- |replace| void foo();\n" +
				"        bar <- replace clone;\n" +
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	/* long, no callinModifier, callin-method, follows: short callin. */
	public void testCompleteCallin7() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        callin void bar() {}\n"+
				"        bar <- replace |\n" + 
				"        bar <- replace clone;\n",
				"foo", 
				"        callin void bar() {}\n" + 
				"        bar <- replace foo; |\n" +    // SH: trailing space?
				"        bar <- replace clone;\n" +
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
	
	/* short, prefix typed, with callinModifier, follows: callout binding. */
	public void testCompleteCallin8() throws Exception {
		createBaseClass("    public void foo() {}\n");
		assertTypeBodyProposal(
				"        void bar() {}\n"+
				"        bar <- after f|\n" + 
				"        void foo() -> void foo();\n",
				"foo(", 
				"        void bar() {}\n" + 
				"        bar <- after |foo;|\n" +    
				"        void foo() -> void foo();\n" +
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}
	
	public void testCompleteBasecall1() throws Exception {
		createBaseClass("    public String getBaseText(Object object) {return null;}\n");
		assertTypeBodyProposal(
				"        callin String getText(Object o) {\n"+
				"        	 return base.|\n" +
				"        }\n" +
				"        getText <- replace getBaseText;\n",
				"", // should only have one proposal, so accept any to see if there are others
				"        callin String getText(Object o) {\n"+
				"        	 return base.getText(|o|)\n" +
				"        }\n" +
				"        getText <- replace getBaseText;\n" +
				"",
				0); // should only have one proposal, so accept any to see if there are others
	}
	
	public void testCompleteTSuperCall1()  throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		String contents= "package test1;\n" +
				"\n" +
				"public team class CompletionTeam0 {\n" +
				"    protected class R {\n" +
				"        void foomethod(int i) {}\n" +
				"        void foomethod(String s) {}\n" +
				"        void barmethod(int i) {}\n" +
				"    }\n" +
				"}\n";
		pack1.createCompilationUnit("CompletionTeam0.java", contents, false, null);

		contents= "package test1;\n" +
				"\n" +
				"public team class CompletionTeam1 extends CompletionTeam0 {\n" +
				"    protected class R {\n" +
				"        void foomethod(int i) {\n" +
				"            tsuper.//here\n" +
				"        }\n" +
				"    }\n" +
				"}\n";
		ICompilationUnit cu1= pack1.createCompilationUnit("CompletionTeam1.java", contents, false, null);

		String str= "//here";

		int offset= contents.indexOf(str);

		CompletionProposalCollector collector= createCollector(cu1, offset);
		collector.setReplacementLength(0);

		codeComplete(cu1, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		
		// strictly expect one method only.
		assertEquals(1, proposals.length);
		assertAppliedProposal(contents, proposals[0], "foomethod()"); // arguments are not inserted by this method :(
	}

	// see Trac #126
	public void testCompleteInCallinMethod1() throws Exception {
		createBaseClass("    public String getText() { return null; }\n");
		ICompletionProposal proposal = assertTypeBodyProposal(
				"        void foo(int i) {} \n" +
				"        callin void getText() {\n" +
				"            int idx = 3;\n" +
				"        	 foo|;\n" +
				"        }\n" +
				"        getText <- replace getText;\n",
				"foo", // this is just step 1.
				"        void foo(int i) {} \n" +
				"        callin void getText() {\n" +
				"            int idx = 3;\n" +
				"        	 foo(|i|);\n" +
				"        }\n" +
				"        getText <- replace getText;\n" +
				"",
				0); // should only have one proposal, so accept any to see if there are others
		// this is the interesting check: choices??
		assertChoices(proposal, new String[][]{new String[]{"i", "idx", "0"}});
	}
	
	/* Full c-t-f declaration */
	public void testCalloutToField1() throws Exception {
		createBaseClass("public int myField;\n");
		assertTypeBodyProposal(
				"        void bar() {}\n"+
				"        get|\n"+
				"        void blub() {}\n",
				"getMyField",
				"        void bar() {}\n"+
				"        /**\n" +
				"         * \n" + // TODO(SH): define method binding comment templates
				"         */\n" +
				"        int getMyField() -> get int myField;\n"+
				"        |\n"+    // TODO(SH): newline not intended?
				"        void blub() {}\n"+
				"",
				INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	/* after modifier w/o trailing space */
	public void testCalloutToField2() throws Exception {
		createBaseClass("public int myField;\n");
		assertTypeBodyProposal(
				"        void bar() {}\n"+
				"        int getMyField() -> get|\n"+
				"        void blub() {}\n",
				" int",
				"        void bar() {}\n"+
				"        int getMyField() -> get int myField;|\n"+
				"        void blub() {}\n"+
				"",
				VERY_INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	/* after modifier w/ trailing space */
	public void testCalloutToField3() throws Exception {
		createBaseClass("public int myField;\n");
		assertTypeBodyProposal(
				"        void bar() {}\n"+
				"        void setMyField(int v) -> set |\n"+
				"        void blub() {}\n",
				"int",
				"        void bar() {}\n"+
				"        void setMyField(int v) -> set int myField;|\n"+
				"        void blub() {}\n"+
				"",
				VERY_INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	/* after modifier w/ trailing space - multiple base fields */
	public void testCalloutToField4() throws Exception {
		createBaseClass("public int myField;\n"+
				        "public int yourField;\n"+
				        "public boolean nobodysField;");
		assertTypeBodyProposal(
				"        void bar() {}\n"+
				"        int getMyField() -> get |\n"+
				"        void blub() {}\n",
				"int y",
				"        void bar() {}\n"+
				"        int getMyField() -> get int yourField;|\n"+
				"        void blub() {}\n"+
				"",
				VERY_INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	/* after modifier w/ trailing space - multiple base fields, mismatching type */
	public void testCalloutToField5() throws Exception {
		createBaseClass("public int myField;\n"+
				        "public int yourField;\n"+
				        "public boolean nobodysField;");
		assertNosuchTypeBodyProposal(
				"        void bar() {}\n"+
				"        int getMyField() -> get |\n"+
				"        void blub() {}\n",
				"boolean",
				VERY_INTERESTING_CALLIN_CALLOUT_PROPOSAL);
	}

	/* Don't accidentally propose a getter for _OT$base: */
	public void testCalloutToField6() throws Exception {
		createBaseClass("public int myField;\n"+
				        "public int yourField;\n"+
				        "public boolean nobodysField;");
		assertNosuchTypeBodyProposal(
				"        void bar() {}\n"+
				"        get|\n"+
				"        void blub() {}\n",
				"get_",
				100);
	}

	public void testPlayedBy1() throws Exception {
		createBaseClass("test2", "MyBase", "");
		fBeforeImports= "";
		fAfterImports= "\nimport base test2.MyBase;\n"; 
		assertTypeBodyProposal(
				"protected class R playedBy MyBa| {\n" +
				"}\n", 
				"MyBase",
				"protected class R playedBy MyBase| {\n" +
				"}\n", 
				0, false);
	}

	public void testPlayedBy2() throws Exception {
		createBaseClass("test2", "AClass", "");
		fBeforeImports= "";
		fAfterImports= "\nimport base test2.AClass;\n"; 
		assertTypeBodyProposal(
				"protected class AClass playedBy A| {\n" +
				"}\n", 
				"ACl", 
				"protected class AClass playedBy AClass| {\n" +
				"}\n", 
				0, false);
	}
	
	public void testBaseGuard1() throws Exception {
		createBaseClass("test2", "AClass", "public boolean check() { return false; }");
		fBeforeImports = "import base test2.AClass;";
		fAfterImports = "import base test2.AClass;";
		assertTypeBodyProposal(
				"protected class ARole playedBy AClass\n" +
				"        base when (base.|)\n" +
				"{\n" +
				"}\n",
				"che",
				"protected class ARole playedBy AClass\n" +
				"        base when (base.check()|)\n" +
				"{\n" +
				"}\n",
				0, false);
	}
	
	public void testBaseGuard2() throws Exception {
		createBaseClass("test2", "AClass", "");
		assertTypeBodyProposal(
				"protected class ARole playedBy AClass\n" +
				"        ba|\n" +
				"{\n" +
				"}\n",
				"base",
				"protected class ARole playedBy AClass\n" +
				"        base when (|)\n" +
				"{\n" +
				"}\n",
				0, false);
	}
	public void testBaseGuard3() throws Exception {
		createBaseClass("test2", "AClass", "public boolean check() { return false; }");
		assertTypeBodyProposal(
				"protected class ARole playedBy AClass {\n" +
				"    toString <- after check w| \n" +
				"}\n",
				"wh",
				"protected class ARole playedBy AClass {\n" +
				"    toString <- after check when (|) \n" +
				"}\n",
				0, false);
	}
	public void testBaseGuard4() throws Exception {
		createBaseClass("test2", "AClass", "public String check() { return false; }");
		assertTypeBodyProposal(
				"protected class ARole playedBy AClass {\n" +
				"    String toString() <- after String check() w| \n" +
				"}\n",
				"wh",
				"protected class ARole playedBy AClass {\n" +
				"    String toString() <- after String check() when (|) \n" +
				"}\n",
				0, false);
	}
	
	
	public void testRoleTag1() throws Exception {
		IPackageFragment teamPkg = CompletionTestSetup.getTestPackage("MyTeam");
		teamPkg.createCompilationUnit("MyRole.java", 
				"team package test1.MyTeam;\n" +
				"public class MyRole {}\n", 
				true, null);
		StringBuffer teamContent = new StringBuffer(); 
		teamContent.append("package test1;\n");
		teamContent.append("/** @role My */\n");
		teamContent.append("public team class MyTeam {\n");
		teamContent.append("    MyRole roleField;\n"); // trigger loading
		teamContent.append("    protected class MyOtherRole {}\n");
		teamContent.append("}");
		
		StringBuffer expectedContent = new StringBuffer(); 
		expectedContent.append("package test1;\n");
		expectedContent.append("/** @role MyRole */\n");
		expectedContent.append("public team class MyTeam {\n");
		expectedContent.append("    MyRole roleField;\n");
		expectedContent.append("    protected class MyOtherRole {}\n");
		expectedContent.append("}");

		String completeAfter = "@role My";
		int pos = teamContent.indexOf(completeAfter)+completeAfter.length();
		
		assertProposal("My", null, null, teamContent, new Region(pos, 0), expectedContent, new Region(pos+4, 0), 0); // 4: len(MyRole)-len(My)
	}
	
	public void testRoleTag2() throws Exception {
		IPackageFragment teamPkg = CompletionTestSetup.getTestPackage("MyTeam");
		teamPkg.createCompilationUnit("MyRole.java", 
				"team package test1.MyTeam;\n" +
				"public class MyRole {}\n", 
				true, null);
		StringBuffer teamContent = new StringBuffer(); 
		teamContent.append("package test1;\n");
		teamContent.append("/** @role  */\n");
		teamContent.append("public team class MyTeam {\n");
		teamContent.append("    MyRole roleField;\n"); // trigger loading
		teamContent.append("    protected class MyOtherRole {}\n");
		teamContent.append("}");
		
		StringBuffer expectedContent = new StringBuffer(); 
		expectedContent.append("package test1;\n");
		expectedContent.append("/** @role MyRole */\n");
		expectedContent.append("public team class MyTeam {\n");
		expectedContent.append("    MyRole roleField;\n");
		expectedContent.append("    protected class MyOtherRole {}\n");
		expectedContent.append("}");

		String completeAfter = "@role ";
		int pos = teamContent.indexOf(completeAfter)+completeAfter.length();
		
		assertProposal("", null, null, teamContent, new Region(pos, 0), expectedContent, new Region(pos+6, 0), 0); // 6: len(MyRole) 
	}
	
	
	public void testCreateMethod1() throws Exception {
		IPackageFragment pkg = CompletionTestSetup.getTestPackage("p1");
		pkg.createCompilationUnit("SuperTeam.java", 
				"package test1.p1;\n" +
				"public team class SuperTeam {\n" +
				"	public class MyRole {\n" +
				"		String blub(int i) { return \"\"; }\n" +
				"	}\n" + 
				"}\n",
				true, null);
		
		StringBuffer subTeamContent = new StringBuffer(); 
		subTeamContent.append("package test1;\n");
		subTeamContent.append("import test1.p1.SuperTeam;\n");
		subTeamContent.append("public team class Completion_testCreateMethod1 extends SuperTeam {\n");
		subTeamContent.append("    protected class MyRole {\n");
		subTeamContent.append("        blu\n");
		subTeamContent.append("    }\n");
		subTeamContent.append("}");
		
		StringBuffer expectedContent = new StringBuffer(); 
		expectedContent.append("package test1;\n");
		expectedContent.append("import test1.p1.SuperTeam;\n");
		expectedContent.append("public team class Completion_testCreateMethod1 extends SuperTeam {\n");
		expectedContent.append("    protected class MyRole {\n");
		expectedContent.append("        /* (non-Javadoc)\n");
		expectedContent.append("         * @see test1.p1.SuperTeam.MyRole#blub(int)\n");
		expectedContent.append("         */\n");
		expectedContent.append("        @Override\n");
		expectedContent.append("        String blub(int i) {\n");
		expectedContent.append("            //TODO\n");
		expectedContent.append("            return tsuper.blub(i);\n");
		expectedContent.append("        }\n");
		expectedContent.append("    }\n");
		expectedContent.append("}");

		String completeAfter = "blu";
		int pos = subTeamContent.indexOf(completeAfter)+completeAfter.length();
		int posAfter = expectedContent.indexOf("}")+1;
		
		assertProposal("", null, null, subTeamContent, new Region(pos, 0), expectedContent, new Region(posAfter, 0), 0); 
	}

	// override role, simple case
	public void testOverrideRole1() throws Exception {
		IPackageFragment pkg = CompletionTestSetup.getTestPackage("p1");
		pkg.createCompilationUnit("SuperTeam.java", 
				"package test1.p1;\n" +
				"public team class SuperTeam {\n" +
				"	public class MyRole {\n" +
				"	}\n" + 
				"}\n",
				true, null);
		
		StringBuffer subTeamContent = new StringBuffer(); 
		subTeamContent.append("package test1;\n");
		subTeamContent.append("import test1.p1.SuperTeam;\n");
		subTeamContent.append("public team class Completion_testOverrideRole1 extends SuperTeam {\n");
		subTeamContent.append("    \n");
		subTeamContent.append("}");
		
		StringBuffer expectedContent = new StringBuffer(); 
		expectedContent.append("package test1;\n");
		expectedContent.append("import test1.p1.SuperTeam;\n");
		expectedContent.append("public team class Completion_testOverrideRole1 extends SuperTeam {\n");
		expectedContent.append("    @Override\n");
		expectedContent.append("    public class MyRole {\n");
		expectedContent.append("    }\n");
		expectedContent.append("}");

		String completeAfter = "    ";
		int pos = subTeamContent.indexOf(completeAfter)+completeAfter.length();
		int posAfter = expectedContent.indexOf("}")+1;
		
		assertProposal("MyRole - Override", null, null, subTeamContent, new Region(pos, 0), expectedContent, new Region(posAfter, 0), 0); 
	}
	
	// override role, role file with mentioning in the team
	public void testOverrideRole2() throws Exception {
		IPackageFragment pkg = CompletionTestSetup.getTestPackage("p1");
		pkg.createCompilationUnit("SuperTeam.java", 
				"package test1.p1;\n" +
				"public team class SuperTeam {\n" +
				"	RoleFile field\n;" + // help the compiler to find the role file
				"}\n",
				true, null);
		IPackageFragment rolePack = CompletionTestSetup.getTestPackage("p1.SuperTeam");
		rolePack.createCompilationUnit("RoleFile.java", 
				"team package test1.p1.SuperTeam;\n" +
				"protected class RoleFile { }\n", 
				true, null);
		
		StringBuffer subTeamContent = new StringBuffer(); 
		subTeamContent.append("package test1;\n");
		subTeamContent.append("import test1.p1.SuperTeam;\n");
		subTeamContent.append("public team class Completion_testOverrideRole1 extends SuperTeam {\n");
		subTeamContent.append("    \n");
		subTeamContent.append("}");
		
		StringBuffer expectedContent = new StringBuffer(); 
		expectedContent.append("package test1;\n");
		expectedContent.append("import test1.p1.SuperTeam;\n");
		expectedContent.append("public team class Completion_testOverrideRole1 extends SuperTeam {\n");
		expectedContent.append("    @Override\n");
		expectedContent.append("    protected class RoleFile {\n");
		expectedContent.append("    }\n");
		expectedContent.append("}");

		String completeAfter = "    ";
		int pos = subTeamContent.indexOf(completeAfter)+completeAfter.length();
		int posAfter = expectedContent.indexOf("}")+1;
		
		assertProposal("RoleFile - Override", null, null, subTeamContent, new Region(pos, 0), expectedContent, new Region(posAfter, 0), 0); 
	}	
	
	// override role, role file, without mentioning in the team (requires search engine help)
	public void testOverrideRole3() throws Exception {
		IPackageFragment pkg = CompletionTestSetup.getTestPackage("p1");
		pkg.createCompilationUnit("SuperTeam.java", 
				"package test1.p1;\n" +
				"public team class SuperTeam {\n" + // no mentioning of RoleFile
				"}\n",
				true, null);
		IPackageFragment rolePack = CompletionTestSetup.getTestPackage("p1.SuperTeam");
		ICompilationUnit rofiCU = rolePack.createCompilationUnit("RoleFile.java", 
				"team package test1.p1.SuperTeam;\n" +
				"protected class RoleFile { }\n", 
				true, null);
		
		StringBuffer subTeamContent = new StringBuffer(); 
		subTeamContent.append("package test1;\n");
		subTeamContent.append("import test1.p1.SuperTeam;\n");
		subTeamContent.append("public team class Completion_testOverrideRole1 extends SuperTeam {\n");
		subTeamContent.append("    \n");
		subTeamContent.append("}");
		
		StringBuffer expectedContent = new StringBuffer(); 
		expectedContent.append("package test1;\n");
		expectedContent.append("import test1.p1.SuperTeam;\n");
		expectedContent.append("public team class Completion_testOverrideRole1 extends SuperTeam {\n");
		expectedContent.append("    @Override\n");
		expectedContent.append("    protected class RoleFile {\n");
		expectedContent.append("    }\n");
		expectedContent.append("}");

		String completeAfter = "    ";
		int pos = subTeamContent.indexOf(completeAfter)+completeAfter.length();
		int posAfter = expectedContent.indexOf("}")+1;
		
		assertProposal("RoleFile - Override", null, null, subTeamContent, new Region(pos, 0), expectedContent, new Region(posAfter, 0), 0); 
	}	
	
	// == Below: Helper methods/fields. ==
	
	private void createBaseClass(String classBody) 
			throws JavaModelException, CoreException 
	{
		createBaseClass("test1", "B", classBody);
	}
	private void createBaseClass(String basePackage, String className, String classBody) 
			throws JavaModelException, CoreException 
	{
		// create a base class:
		StringBuffer buf= new StringBuffer();
		buf.append("package "+basePackage+";\n");
		buf.append("public class "+className+" {\n");
		buf.append(classBody);
		buf.append("}\n");
		String contents= buf.toString();

		IPackageFragment basePkg = CompletionTestSetup.getAbsoluteTestPackage(basePackage);
		basePkg.createCompilationUnit(className+".java", contents, true, null);
	}

	// from 3.4 AbstractCompletionTest:
	protected static final String CARET= "|";

	private ICompilationUnit fCU;
	private JavaEditor fEditor;

	private String fBeforeImports;
	private String fAfterImports;
	private String fMembers;
	private String fLocals;
	private char fTrigger;
	private boolean fWaitBeforeCompleting;
	
	protected void assertChoices(ICompletionProposal proposal, String[][] expected) {
		assertTrue("Not a ParameterGuessingProposal", proposal instanceof ParameterGuessingProposal);
		ParameterGuessingProposal pgProposal = (ParameterGuessingProposal)proposal;
		ICompletionProposal[][] choices = new ProposalAdaptor().getChoices(pgProposal);
		assertEquals("Not same number of choices", expected.length, choices.length);
		for (int i=0; i<choices.length; i++) {
			assertEquals("Not same number of nested choices", expected[i].length, choices[i].length);
			for (int j=0; j<choices.length; j++) {
				assertEquals("Unexpected choice", expected[i][j], choices[i][j].getDisplayString());
			}
		}
	}
	
	protected ICompletionProposal assertTypeBodyProposal(String before, String selector, String expected, int requiredRelevance) 
			throws CoreException 
	{
		return assertTypeBodyProposal(before, selector, expected, requiredRelevance, true);
	}
	protected ICompletionProposal assertTypeBodyProposal(String before, String selector, String expected, int requiredRelevance, boolean addRole) 
			throws CoreException 
	{
		StringBuffer contents= new StringBuffer();
		IRegion preSelection= assembleClassBodyTestCUExtractSelection(contents, before, fBeforeImports, addRole);
		StringBuffer result= new StringBuffer();
		IRegion expectedSelection= assembleClassBodyTestCUExtractSelection(result, expected, fAfterImports, addRole);
		
		return assertProposal(selector, null, null, contents, preSelection, result, expectedSelection, requiredRelevance);
	}

	protected ICompletionProposal assertROFIBodyProposal(String before, String selector, String expected, int requiredRelevance) 
			throws CoreException 
	{
		IPackageFragment testPkg = CompletionTestSetup.getTestPackage(null);
		String teamName = "Completion_" + getName();
		testPkg.createCompilationUnit(teamName + ".java", 
				"package test1;\n" +
				"/** @role RoFiRole */\n" +
				"public team class Completion_"+getName()+" {}\n", 
				true, null);
		
		StringBuffer contents= new StringBuffer();
		IRegion preSelection= assembleROFIBodyTestCUExtractSelection(contents, before, fBeforeImports);
		StringBuffer result= new StringBuffer();
		IRegion expectedSelection= assembleROFIBodyTestCUExtractSelection(result, expected, fAfterImports);
		
		return assertProposal(selector, teamName, "RoFiRole", contents, preSelection, result, expectedSelection, requiredRelevance);
	}

	protected void assertNosuchTypeBodyProposal(String before, String selector, int requiredRelevance) 
			throws CoreException 
	{
		StringBuffer contents= new StringBuffer();
		IRegion preSelection= assembleClassBodyTestCUExtractSelection(contents, before, fBeforeImports, true);
		
		assertNosuchProposal(selector, contents, preSelection, requiredRelevance);
	}

	private IRegion assembleClassBodyTestCUExtractSelection(StringBuffer buffer, String javadocLine, String imports, boolean addRole) {
		String prefix= "package test1;\n" +
		imports +
		"\n" +
		"public team class Completion_" + getName() + " {\n" + // SH: added: team, removed <T> (crashed!!) FIXME!!
//{ObjectTeams:
		(addRole ?
		"    protected class R playedBy B {\n" : "") +
// SH}
		fLocals +
		""; // SH: removed blanks
		String postfix= "\n" +
		"\n" +
		fMembers +
//{ObjectTeams:
		(addRole ?
		"    }\n":"")+
// SH}
		"}\n";
		StringBuffer lineBuffer= new StringBuffer(javadocLine);
		int firstPipe= lineBuffer.indexOf(CARET);
		int secondPipe;
		if (firstPipe == -1) {
			firstPipe= lineBuffer.length();
			secondPipe= firstPipe;
		} else {
			lineBuffer.replace(firstPipe, firstPipe + CARET.length(), "");
			secondPipe= lineBuffer.indexOf(CARET, firstPipe);
			if (secondPipe ==-1)
				secondPipe= firstPipe;
			else
				lineBuffer.replace(secondPipe, secondPipe + CARET.length(), "");
		}
		buffer.append(prefix + lineBuffer + postfix);
		return new Region(firstPipe + prefix.length(), secondPipe - firstPipe);
	}

	private IRegion assembleROFIBodyTestCUExtractSelection(StringBuffer buffer, String javadocLine, String imports) {
		String teamName = "Completion_" + getName();
		String prefix= "team package test1."+teamName+";\n" +
		imports +
		"\n" +
		"protected class RoFiRole playedBy B {\n" +
		fLocals +
		"";
		String postfix= "\n" +
		"\n" +
		fMembers +
		"}\n";
		StringBuffer lineBuffer= new StringBuffer(javadocLine);
		int firstPipe= lineBuffer.indexOf(CARET);
		int secondPipe;
		if (firstPipe == -1) {
			firstPipe= lineBuffer.length();
			secondPipe= firstPipe;
		} else {
			lineBuffer.replace(firstPipe, firstPipe + CARET.length(), "");
			secondPipe= lineBuffer.indexOf(CARET, firstPipe);
			if (secondPipe ==-1)
				secondPipe= firstPipe;
			else
				lineBuffer.replace(secondPipe, secondPipe + CARET.length(), "");
		}
		buffer.append(prefix + lineBuffer + postfix);
		return new Region(firstPipe + prefix.length(), secondPipe - firstPipe);
	}

	private ICompletionProposal assertProposal(String selector, String relativePackage, String typeName, StringBuffer contents, IRegion preSelection, StringBuffer result, IRegion expectedSelection, int requiredRelevance) throws CoreException {
//{ObjectTeams: made package and file name configurable via new arguments `relativePackage'/`typeName':
		IPackageFragment pkg = (relativePackage == null) ? CompletionTestSetup.getAnonymousTestPackage() : CompletionTestSetup.getTestPackage(relativePackage);
		fCU= (typeName == null) ? createCU(pkg, contents.toString()) : pkg.createCompilationUnit(typeName + ".java", contents.toString(), false, null); 
// SH}
		fEditor= (JavaEditor) EditorUtility.openInEditor(fCU);
		IDocument doc;
		ITextSelection postSelection;
		ICompletionProposal proposal = null;
		try {
			proposal= findNonNullProposal(selector, fCU, preSelection, requiredRelevance);
			doc= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
//{ObjectTeams: selection needs to be set in the editor:
			fEditor.getSelectionProvider().setSelection(new TextSelection(preSelection.getOffset(), preSelection.getLength()));
// SH}
			apply(fEditor, doc, proposal, preSelection);
			postSelection= (ITextSelection) fEditor.getSelectionProvider().getSelection();
		} finally {
			JavaProjectHelper.emptyDisplayLoop();
			closeEditor(fEditor);
		}

		assertEquals(result.toString(), doc.get());
		assertEquals("selection offset", expectedSelection.getOffset(), postSelection.getOffset());
		assertEquals("selection length", expectedSelection.getLength(), postSelection.getLength());
		return proposal;
	}

	private void assertNosuchProposal(String selector, StringBuffer contents, IRegion preSelection, int requiredRelevance) 
			throws CoreException 
	{
		fCU= createCU(CompletionTestSetup.getAnonymousTestPackage(), contents.toString());
		fEditor= (JavaEditor) EditorUtility.openInEditor(fCU);
		try {
			ICompletionProposal proposal= findNamedProposal(selector, fCU, preSelection, requiredRelevance);
			assertNull("found unexpected proposal: "+selector, proposal);
		} finally {
			closeEditor(fEditor);
		}
	}
	
	// from 3.4 CodeCompletionTest
	private void assertAppliedProposal(String contents, IJavaCompletionProposal proposal, String completion) {
		IDocument doc= new Document(contents);
		proposal.apply(doc);
		int offset2= contents.indexOf("//here");
		String result= contents.substring(0, offset2) + completion + contents.substring(offset2);
		assertEquals(result, doc.get());
	}

	private ICompilationUnit createCU(IPackageFragment pack1, String contents) throws JavaModelException {
		ICompilationUnit cu= pack1.createCompilationUnit("Completion_" + getName() + ".java", contents, false, null);
		return cu;
	}

	private ICompletionProposal findNonNullProposal(String prefix, ICompilationUnit cu, IRegion selection, int requiredRelevance) throws JavaModelException, PartInitException {
		ICompletionProposal proposal= findNamedProposal(prefix, cu, selection, requiredRelevance);
		assertNotNull("no proposal starting with \"" + prefix + "\"", proposal);
		return proposal;
	}

	private ICompletionProposal findNamedProposal(String prefix, ICompilationUnit cu, IRegion selection, int requiredRelevance) throws JavaModelException, PartInitException {
		ICompletionProposal[] proposals= collectProposals(cu, selection);
		
		ICompletionProposal found= null;
		for (int i= 0; i < proposals.length; i++) {
			if (proposals[i] instanceof IJavaCompletionProposal) 
				if (((IJavaCompletionProposal)proposals[i]).getRelevance() < requiredRelevance)
					continue;
			String displayString= proposals[i].getDisplayString();
			if (displayString.startsWith(prefix)) {
				if (found == null || displayString.equals(prefix))
					found= proposals[i];
			}
		}
		return found;
	}

	private ICompletionProposal[] collectProposals(ICompilationUnit cu, IRegion selection) throws JavaModelException, PartInitException {
		waitBeforeCoreCompletion();
		ContentAssistant assistant= new ContentAssistant();
		assistant.setDocumentPartitioning(IJavaPartitions.JAVA_PARTITIONING);
		IContentAssistProcessor javaProcessor= new JavaCompletionProcessor(fEditor, assistant, getContentType());

		ICompletionProposal[] proposals= javaProcessor.computeCompletionProposals(fEditor.getViewer(), selection.getOffset());
		return proposals;
	}

	/**
	 * Invokes {@link Thread#sleep(long)} if {@link #waitBeforeCompleting(boolean)} was set to
	 * <code>true</code> or camel case completions are enabled. For some reasons, inner types and
	 * camel case matches don't show up otherwise.
	 * 
	 * @since 3.2
	 */
	private void waitBeforeCoreCompletion() {
	    if (fWaitBeforeCompleting || JavaCore.ENABLED.equals(JavaCore.getOption(JavaCore.CODEASSIST_CAMEL_CASE_MATCH))) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException x) {
			}
		}
    }

	protected String getContentType() {
		return IDocument.DEFAULT_CONTENT_TYPE;
	}

	private void apply(ITextEditor editor, IDocument doc, ICompletionProposal proposal, IRegion selection) {
		if (proposal instanceof ICompletionProposalExtension2) {
			ICompletionProposalExtension2 ext= (ICompletionProposalExtension2) proposal;
			ITextViewer viewer= (ITextViewer) editor.getAdapter(ITextOperationTarget.class);
			ext.selected(viewer, false);
			viewer.setSelectedRange(selection.getOffset(), selection.getLength());
			ext.apply(viewer, fTrigger, 0, selection.getOffset());
			Point range= proposal.getSelection(doc);
			if (range != null)
				viewer.setSelectedRange(range.x, range.y);
		} else if (proposal instanceof ICompletionProposalExtension) {
			ICompletionProposalExtension ext= (ICompletionProposalExtension) proposal;
			ext.apply(doc, fTrigger, selection.getOffset() + selection.getLength());
		} else {
			proposal.apply(doc);
		}
	}	
}
