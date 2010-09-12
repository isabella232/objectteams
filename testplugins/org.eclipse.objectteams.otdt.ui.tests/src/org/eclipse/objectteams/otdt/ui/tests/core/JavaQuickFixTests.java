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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.text.correction.proposals.NewCUUsingWizardProposal;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Testing whether standard Java quickfixes work in OT/J code, too.
 * 
 * @author stephan
 * @since 0.7.0
 */
public class JavaQuickFixTests extends OTQuickFixTest {
	private static final Class THIS= JavaQuickFixTests.class;

	public JavaQuickFixTests(String name) {
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
	
	// the following three test try to reproduce Bug 311890 -  [assist] support "create class" quickfix for OT/J references
	
	// create a class for an unresolved playedBy declaration
	public void testCreateClass1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 6);
		// create class / interface / enum B
		// add type parameter B to R / T1
		// fix project setup
		assertCorrectLabels(proposals);

		for (Object prop : proposals) {
			if (prop instanceof NewCUUsingWizardProposal) {
				NewCUUsingWizardProposal proposal= (NewCUUsingWizardProposal)prop;
				if ("Create class 'B'".equals(proposal.getDisplayString())) {
					return; // done
				}
			}
		}
		fail("Expected proposal not found");
	}
	
	// create a class for an unresolved playedBy declaration in a nested role
	public void testCreateClass2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T1 {\n");
		buf.append("    protected team class Mid {\n");
		buf.append("    	protected class R playedBy B {\n");
		buf.append("    	}\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 7);
		// create class / interface / enum B
		// add type parameter B to R / Mid / T1
		// fix project setup
		assertCorrectLabels(proposals);

		for (Object prop : proposals) {
			if (prop instanceof NewCUUsingWizardProposal) {
				NewCUUsingWizardProposal proposal= (NewCUUsingWizardProposal)prop;
				if ("Create class 'B'".equals(proposal.getDisplayString())) {
					return; // done
				}
			}
		}
		fail("Expected proposal not found");
	}


	// create a class for an unresolved playedBy declaration
	public void testCreateClass3() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T1 {\n");
		buf.append("    protected class R playedBy B {\n");
		buf.append("    }\n");
		buf.append("    public void foo(B as R o) {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T1.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList proposals= collectCorrections(cu, astRoot, 2, 1, null);
		assertNumberOfProposals(proposals, 5);
		// create class / interface / enum B
		// add type parameter B to T1
		// fix project setup
		assertCorrectLabels(proposals);

		for (Object prop : proposals) {
			if (prop instanceof NewCUUsingWizardProposal) {
				NewCUUsingWizardProposal proposal= (NewCUUsingWizardProposal)prop;
				if ("Create class 'B'".equals(proposal.getDisplayString())) {
					return; // done
				}
			}
		}
		fail("Expected proposal not found");
	}
	
}
