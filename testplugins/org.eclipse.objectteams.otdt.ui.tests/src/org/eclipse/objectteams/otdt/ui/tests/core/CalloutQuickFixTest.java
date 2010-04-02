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
 * $Id: CalloutQuickFixTest.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.core;

import java.util.ArrayList;
import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;

/**
 * Test OT-specific quickfixes (here: callout related issues).
 * @author stephan
 * @since 1.2.8
 */
public class CalloutQuickFixTest extends OTQuickFixTest {


	private static final Class THIS= CalloutQuickFixTest.class;
	
	public CalloutQuickFixTest(String name) {
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

	@SuppressWarnings("unchecked")
	@Override
	protected void addOptions(Hashtable options) {
		super.addOptions(options);
		options.put(OTDTPlugin.OT_COMPILER_INFERRED_CALLOUT, JavaCore.WARNING);
	}
	
	/* Converting a field read to explicitly use a callout-to-field. */
	public void testConvertFieldAccessToCalloutCall1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);

		String[] expectedProposals = new String[2];
		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        int getVal() -> get int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(getVal());\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[1] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);
	}

		
	/* Converting a field read (this-qualified) to explicitly use a callout-to-field. */
	public void testConvertFieldAccessToCalloutCall2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(this.val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		
		String[] expectedProposals = new String[2];

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        int getVal() -> get int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(this.getVal());\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(this.val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[1] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);

	}
	
	/* Converting a field assignment to explicitly use a callout-to-field. */
	public void testConvertFieldAccessToCalloutCall3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void foo(){\n");
		buf.append("        	val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		
		String[] expectedProposals = new String[2];

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void setVal(int val) -> set int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	setVal(3);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[1] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);

	}
	
	/* Converting a field assignment (this-qualified) to explicitly use a callout-to-field. */
	public void testConvertFieldAccessToCalloutCall4() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		
		String[] expectedProposals = new String[2];

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void setVal(int val) -> set int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.setVal(3);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		expectedProposals[0] = buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[1] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);
	}
	
	
	/* Convert field access to call to existing callout-to-field. */
	public void testConvertFieldAccessToCalloutCall5() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        int getVal() -> get int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		
		String[] expectedProposals = new String[2];


		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        int getVal() -> get int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(getVal());\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        int getVal() -> get int val;\n");
		buf.append("\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(val);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[1] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);
	}

	/* Convert field assignment to call to existing callout-to-field. */
	public void testConvertFieldAccessToCalloutCall6() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public int val;\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void setVal(int val) -> set int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 2);
		assertCorrectLabels(proposals);
		
		String[] expectedProposals = new String[2];


		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void setVal(int val) -> set int val;\n");
		buf.append("\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.setVal(3);\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();

		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void setVal(int val) -> set int val;\n");
		buf.append("\n");
		buf.append("        @SuppressWarnings(\"inferredcallout\")\n");
		buf.append("        void foo(){\n");
		buf.append("        	this.val = 3;\n");
		buf.append("		}\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[1] = buf.toString();

		assertExpectedExistInProposals(proposals, expectedProposals);
	}
}
