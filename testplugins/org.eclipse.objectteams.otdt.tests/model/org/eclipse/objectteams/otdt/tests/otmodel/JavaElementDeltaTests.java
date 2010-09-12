/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann
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
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.objectteams.otdt.tests.ModifyingResourceTests;

import junit.framework.Test;

/**
 * These test ensure that modifications in OT/J projects are correctly reported as
 * IJavaElementDeltas.
 */
public class JavaElementDeltaTests extends ModifyingResourceTests {

public static Test suite() {
	return buildModelTestSuite(JavaElementDeltaTests.class);
}

// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_PREFIX =  "";
//	TESTS_NAMES = new String[] { "" };
//	TESTS_NUMBERS = new int[] { 100772 };
//	TESTS_RANGE = new int[] { 83304, -1 };
}

public JavaElementDeltaTests(String name) {
	super(name);
}

/*
 * Add the OT/J nature to an existing project.
 */
public void testAddOTJavaNature() throws CoreException {
	try {
		createProject("P");
		addJavaNature("P");
		startDeltas();
		addOTJavaNature("P");
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CONTENT}\n" + 
			"	ResourceDelta(/P/.project)[*]"
		);
	} finally {
		stopDeltas();
		deleteProject("P");
	}
}

/*
 * Add a role to a team class.
 */
public void testAddRoleToTeam() throws CoreException {
	ICompilationUnit copy = null;
	DeltaListener listener = new DeltaListener(ElementChangedEvent.POST_CHANGE);
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		addOTJavaNature("P");
		createFolder("P/src/p1");
		createFile("/P/src/p1/MyTeam.java", 
					"package p1;\n" +
					"public team class MyTeam {\n" +
					"	void foo() {}\n"+
				    "}\n");

		ICompilationUnit unit = getCompilationUnit("P", "src", "p1", "MyTeam.java");
		copy = unit.getWorkingCopy(null);

		// add role to working copy
		copy.getBuffer().setContents(
				"package p1;\n" +
				"public team class MyTeam {\n" +
				"	void foo() {}\n"+
				"   protected class Role {}\n" +
				"}\n");

		// commit working copy
		JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE);
		copy.commitWorkingCopy(true, null);
		assertEquals(
			"Unexpected delta after committing working copy",
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		p1[*]: {CHILDREN}\n" +
			"			MyTeam.java[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n" +
			"				MyTeam[*]: {CHILDREN | FINE GRAINED}\n" +
			"					Role[+]: {}",
			listener.toString());
	} finally {
		JavaCore.removeElementChangedListener(listener);
		if (copy != null) copy.discardWorkingCopy();
		deleteProject("P");
	}
}

/*
 * add a callin binding to a role class.
 */
public void testAddCallinToRole1() throws CoreException {
	ICompilationUnit copy = null;
	DeltaListener listener = new DeltaListener(ElementChangedEvent.POST_CHANGE);
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		addOTJavaNature("P");
		createFolder("P/src/p1");
		createFile("/P/src/p1/Base.java",
					"package p1;\n" +
					"public class Base {\n" +
					"    void bar() {}" +
					"}\n");
		createFile("/P/src/p1/MyTeam.java", 
					"package p1;\n" +
					"public team class MyTeam {\n" +
					"   protected class Role {\n" +
					"		void foo() {}\n" +
					"	}\n"+
				    "}\n");

		ICompilationUnit unit = getCompilationUnit("P", "src", "p1", "MyTeam.java");
		copy = unit.getWorkingCopy(null);

		// add callin to working copy
		copy.getBuffer().setContents(
				"package p1;\n" +
				"public team class MyTeam {\n" +
				"   protected class Role {\n" +
				"		void foo() {}\n" +
				"		foo <- after bar;\n" + // <= added
				"	}\n"+
			    "}\n");

		// commit working copy
		JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE);
		copy.commitWorkingCopy(true, null);
		assertEquals(
			"Unexpected delta after committing working copy",
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		p1[*]: {CHILDREN}\n" +
			"			MyTeam.java[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n" +
			"				MyTeam[*]: {CHILDREN | FINE GRAINED}\n" +
			"					Role[*]: {CHILDREN | FINE GRAINED}\n" +
			"						foo <- bar[+]: {}",
			listener.toString());
	} finally {
		JavaCore.removeElementChangedListener(listener);
		if (copy != null) copy.discardWorkingCopy();
		deleteProject("P");
	}
}

/*
 * add a callin binding to a role class - use the reconciler.
 */
public void testAddCallinToRole1r() throws CoreException {
	ICompilationUnit copy = null;
	DeltaListener listener = new DeltaListener(ElementChangedEvent.POST_RECONCILE);
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		addOTJavaNature("P");
		createFolder("P/src/p1");
		createFile("/P/src/p1/Base.java",
					"package p1;\n" +
					"public class Base {\n" +
					"    void bar() {}" +
					"}\n");
		createFile("/P/src/p1/MyTeam.java", 
					"package p1;\n" +
					"public team class MyTeam {\n" +
					"   protected class Role {\n" +
					"		void foo() {}\n" +
					"	}\n"+
				    "}\n");

		ICompilationUnit unit = getCompilationUnit("P", "src", "p1", "MyTeam.java");
		copy = unit.getWorkingCopy(null);

		// add callin to working copy
		copy.getBuffer().setContents(
				"package p1;\n" +
				"public team class MyTeam {\n" +
				"   protected class Role {\n" +
				"		void foo() {}\n" +
				"		foo <- after bar;\n" + // <= added
				"	}\n"+
			    "}\n");

		// commit working copy
		JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_RECONCILE);
		copy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertEquals(
			"Unexpected delta after committing working copy",
			"MyTeam[*]: {CHILDREN | FINE GRAINED}\n" +
			"	Role[*]: {CHILDREN | FINE GRAINED}\n" +
			"		foo <- bar[+]: {}",
			listener.toString());
	} finally {
		JavaCore.removeElementChangedListener(listener);
		if (copy != null) copy.discardWorkingCopy();
		deleteProject("P");
	}
}

/*
 * add a callin binding to a role class that already has another callin binding.
 * Witness for bogus add+remove deltas for unchanged callin binding. 
 */
public void testAddCallinToRole2() throws CoreException {
	ICompilationUnit copy = null;
	DeltaListener listener = new DeltaListener(ElementChangedEvent.POST_CHANGE);
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		addOTJavaNature("P");
		createFolder("P/src/p1");
		createFile("/P/src/p1/Base.java",
					"package p1;\n" +
					"public class Base {\n" +
					"    void bar() {}" +
					"}\n");
		createFile("/P/src/p1/MyTeam.java", 
					"package p1;\n" +
					"public team class MyTeam {\n" +
					"   protected class Role {\n" +
					"		void foo() {}\n" +
					"		void foo2() {}\n" +
					"		foo <- after bar;\n" +
					"	}\n"+
				    "}\n");

		ICompilationUnit unit = getCompilationUnit("P", "src", "p1", "MyTeam.java");
		copy = unit.getWorkingCopy(null);

		// add callin to working copy
		copy.getBuffer().setContents(
				"package p1;\n" +
				"public team class MyTeam {\n" +
				"   protected class Role {\n" +
				"		void foo() {}\n" +
				"		void foo2() {}\n" +
				"		foo2 <- after bar;\n" + // <= added
				"		foo <- after bar;\n" +
				"	}\n"+
			    "}\n");

		// commit working copy
		JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE);
		copy.commitWorkingCopy(true, null);
		assertEquals(
			"Unexpected delta after committing working copy",
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		p1[*]: {CHILDREN}\n" +
			"			MyTeam.java[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n" +
			"				MyTeam[*]: {CHILDREN | FINE GRAINED}\n" +
			"					Role[*]: {CHILDREN | FINE GRAINED}\n" +
			"						foo2 <- bar[+]: {}",
			listener.toString());
	} finally {
		JavaCore.removeElementChangedListener(listener);
		if (copy != null) copy.discardWorkingCopy();
		deleteProject("P");
	}
}

/*
 * changing the callin modifier of an existing callin binding
 */
public void testModifyCallin1() throws CoreException {
	ICompilationUnit copy = null;
	DeltaListener listener = new DeltaListener(ElementChangedEvent.POST_CHANGE);
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		addOTJavaNature("P");
		createFolder("P/src/p1");
		createFile("/P/src/p1/Base.java",
					"package p1;\n" +
					"public class Base {\n" +
					"    void bar() {}" +
					"}\n");
		createFile("/P/src/p1/MyTeam.java", 
					"package p1;\n" +
					"public team class MyTeam {\n" +
					"   protected class Role {\n" +
					"		void foo() {}\n" +
					"		void foo2() {}\n" +
					"		foo <- after bar;\n" +
					"		foo2 <- after bar;\n" + 
					"	}\n"+
				    "}\n");

		ICompilationUnit unit = getCompilationUnit("P", "src", "p1", "MyTeam.java");
		copy = unit.getWorkingCopy(null);

		// add callin to working copy
		copy.getBuffer().setContents(
				"package p1;\n" +
				"public team class MyTeam {\n" +
				"   protected class Role {\n" +
				"		void foo() {}\n" +
				"		void foo2() {}\n" +
				"		foo <- before bar;\n" + // <= changed
				"		foo2 <- after bar;\n" + 
				"	}\n"+
			    "}\n");

		// commit working copy
		JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE);
		copy.commitWorkingCopy(true, null);
		assertEquals(
			"Unexpected delta after committing working copy",
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		p1[*]: {CHILDREN}\n" +
			"			MyTeam.java[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n" +
			"				MyTeam[*]: {CHILDREN | FINE GRAINED}\n" +
			"					Role[*]: {CHILDREN | FINE GRAINED}\n" +
			"						foo <- bar[*]: {MODIFIERS CHANGED}",
			listener.toString());
	} finally {
		JavaCore.removeElementChangedListener(listener);
		if (copy != null) copy.discardWorkingCopy();
		deleteProject("P");
	}
}

/*
 * adding a callin label to an existing callin binding
 */
public void testModifyCallin2() throws CoreException {
	ICompilationUnit copy = null;
	DeltaListener listener = new DeltaListener(ElementChangedEvent.POST_CHANGE);
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		addOTJavaNature("P");
		createFolder("P/src/p1");
		createFile("/P/src/p1/Base.java",
					"package p1;\n" +
					"public class Base {\n" +
					"    void bar() {}" +
					"}\n");
		createFile("/P/src/p1/MyTeam.java", 
					"package p1;\n" +
					"public team class MyTeam {\n" +
					"   protected class Role {\n" +
					"		void foo() {}\n" +
					"		void foo2() {}\n" +
					"		foo <- after bar;\n" +
					"		foo2 <- after bar;\n" + 
					"	}\n"+
				    "}\n");

		ICompilationUnit unit = getCompilationUnit("P", "src", "p1", "MyTeam.java");
		copy = unit.getWorkingCopy(null);

		// add callin to working copy
		copy.getBuffer().setContents(
				"package p1;\n" +
				"public team class MyTeam {\n" +
				"   protected class Role {\n" +
				"		void foo() {}\n" +
				"		void foo2() {}\n" +
				"		this_one: foo <- after bar;\n" + // <= changed
				"		foo2 <- after bar;\n" + 
				"	}\n"+
			    "}\n");

		// commit working copy
		JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE);
		copy.commitWorkingCopy(true, null);
		assertEquals(
			"Unexpected delta after committing working copy",
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		p1[*]: {CHILDREN}\n" +
			"			MyTeam.java[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n" +
			"				MyTeam[*]: {CHILDREN | FINE GRAINED}\n" +
			"					Role[*]: {CHILDREN | FINE GRAINED}\n" +
			"						foo <- bar[*]: {CONTENT}",
			listener.toString());
	} finally {
		JavaCore.removeElementChangedListener(listener);
		if (copy != null) copy.discardWorkingCopy();
		deleteProject("P");
	}
}

/*
 * changing an existing callout binding from => (bug) to ->
 */
public void testModifyCallout1() throws CoreException {
	ICompilationUnit copy = null;
	DeltaListener listener = new DeltaListener(ElementChangedEvent.POST_CHANGE);
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		addOTJavaNature("P");
		createFolder("P/src/p1");
		createFile("/P/src/p1/Base.java",
					"package p1;\n" +
					"public class Base {\n" +
					"    void bar() {}" +
					"}\n");
		createFile("/P/src/p1/MyTeam.java", 
					"package p1;\n" +
					"public team class MyTeam {\n" +
					"   protected class Role {\n" +
					"		abstract void foo() {}\n" +
					"		void foo2() {}\n" +
					"		foo => bar;\n" +
					"		foo2 <- after bar;\n" + 
					"	}\n"+
				    "}\n");

		ICompilationUnit unit = getCompilationUnit("P", "src", "p1", "MyTeam.java");
		copy = unit.getWorkingCopy(null);

		// add callin to working copy
		copy.getBuffer().setContents(
				"package p1;\n" +
				"public team class MyTeam {\n" +
				"   protected class Role {\n" +
				"		abstract void foo() {}\n" +
				"		void foo2() {}\n" +
				"		foo -> bar;\n" + 		// <= changed
				"		foo2 <- after bar;\n" + 
				"	}\n"+
			    "}\n");

		// commit working copy
		JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE);
		copy.commitWorkingCopy(true, null);
		assertEquals(
			"Unexpected delta after committing working copy",
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		p1[*]: {CHILDREN}\n" +
			"			MyTeam.java[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n" +
			"				MyTeam[*]: {CHILDREN | FINE GRAINED}\n" +
			"					Role[*]: {CHILDREN | FINE GRAINED}\n" +
			"						foo -> bar[*]: {MODIFIERS CHANGED}",
			listener.toString());
	} finally {
		JavaCore.removeElementChangedListener(listener);
		if (copy != null) copy.discardWorkingCopy();
		deleteProject("P");
	}
}

/*
 * changing a parameter name in an existing callout binding
 */
public void testModifyCallout2() throws CoreException {
	ICompilationUnit copy = null;
	DeltaListener listener = new DeltaListener(ElementChangedEvent.POST_CHANGE);
	try {
		createJavaProject("P", new String[] {"src"}, "bin");
		addOTJavaNature("P");
		createFolder("P/src/p1");
		createFile("/P/src/p1/Base.java",
					"package p1;\n" +
					"public class Base {\n" +
					"    void bar(int i) {}" +
					"}\n");
		createFile("/P/src/p1/MyTeam.java", 
					"package p1;\n" +
					"public team class MyTeam {\n" +
					"   protected class Role {\n" +
					"		void foo2() {}\n" +
					"		void foo(int j) => void bar(int i);\n" +
					"		foo2 <- after bar;\n" + 
					"	}\n"+
				    "}\n");

		ICompilationUnit unit = getCompilationUnit("P", "src", "p1", "MyTeam.java");
		copy = unit.getWorkingCopy(null);

		// add callin to working copy
		copy.getBuffer().setContents(
				"package p1;\n" +
				"public team class MyTeam {\n" +
				"   protected class Role {\n" +
				"		void foo2() {}\n" +
				"		void foo(int k) => void bar(int i);\n" +
				"		foo2 <- after bar;\n" + 
				"	}\n"+
			    "}\n");

		// commit working copy
		JavaCore.addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE);
		copy.commitWorkingCopy(true, null);
		assertEquals(
			"Unexpected delta after committing working copy",
			"P[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		p1[*]: {CHILDREN}\n" +
			"			MyTeam.java[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n" +
			"				MyTeam[*]: {CHILDREN | FINE GRAINED}\n" +
			"					Role[*]: {CHILDREN | FINE GRAINED}\n" +
			"						foo(int) -> bar(int)[*]: {CONTENT}",
			listener.toString());
	} finally {
		JavaCore.removeElementChangedListener(listener);
		if (copy != null) copy.discardWorkingCopy();
		deleteProject("P");
	}
}
}

