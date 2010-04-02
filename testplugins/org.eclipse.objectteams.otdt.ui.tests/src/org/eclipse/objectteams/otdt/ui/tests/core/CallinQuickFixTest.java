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
 * $Id: CallinQuickFixTest.java 23495 2010-02-05 23:15:16Z stephan $
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
 * Test OT-specific quickfixes (here: callin related issues).
 * @author stephan
 * @since 1.2.8
 */
public class CallinQuickFixTest extends OTQuickFixTest {
	
	private static final Class THIS= CallinQuickFixTest.class;

	public CallinQuickFixTest(String name) {
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
		// need to configure 5.4(c) to warning to enable quickfix for testSuppressWarning1():
		options.put(OTDTPlugin.OT_COMPILER_EXCEPTION_IN_GUARD, JavaCore.WARNING);
	}

	/* Suppressing a warning re exception thrown from guard predicate. */
	public void testSuppressWarning1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public void foo() {};\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(\"OK\");\n");
		buf.append("		}\n");
		buf.append("        boolean bad() throws Exception {\n");
		buf.append("        	throw new Exception(\"too bad\");\n");
		buf.append("		}\n");
		buf.append("		foo <- after foo when (bad());\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cuteam = pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cuteam);
		ArrayList proposals= collectCorrections(cuteam, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		String[] expectedProposals = new String[1];
		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        void foo(){\n");
		buf.append("        	System.out.print(\"OK\");\n");
		buf.append("		}\n");
		buf.append("        boolean bad() throws Exception {\n");
		buf.append("        	throw new Exception(\"too bad\");\n");
		buf.append("		}\n");
		buf.append("		@SuppressWarnings(\"exceptioninguard\")\n");
		buf.append("        foo <- after foo when (bad());\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();


		assertExpectedExistInProposals(proposals, expectedProposals);
	}

	/* Adjust a callin modifier to the bound role method (after -> replace). */
	public void testChangeCallinModifier1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B1 {\n");
		buf.append("    public void foo() {};\n");
		buf.append("}\n");
		pack1.createCompilationUnit("B1.java", buf.toString(), false, null);

		buf = new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"basecall\")\n");
		buf.append("        callin void foo() {\n");
		buf.append("        	System.out.print(\"OK\");\n");
		buf.append("		}\n");
		buf.append("		foo <- after foo;\n");
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
		buf.append("        @SuppressWarnings(\"basecall\") void foo() {\n");// removed callin modifier
		buf.append("        	System.out.print(\"OK\");\n");
		buf.append("		}\n");
		buf.append("		foo <- after foo;\n");
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[0] = buf.toString();
		buf= new StringBuffer();
		buf.append("package test1;\n");	
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B1 {\n");
		buf.append("        @SuppressWarnings(\"basecall\")\n");
		buf.append("        callin void foo() {\n");
		buf.append("        	System.out.print(\"OK\");\n");
		buf.append("		}\n");
		buf.append("		foo <- replace foo;\n");				// changed after to replace
		buf.append("    }\n");
		buf.append("}\n");
		expectedProposals[1] = buf.toString();


		assertExpectedExistInProposals(proposals, expectedProposals);
	}


}
