/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2013 GK Software AG.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otjld.regression;

import junit.framework.Test;

import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

public class ComplexStructures extends AbstractOTJLDTest {
	
	public ComplexStructures(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "testBug391876"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}

	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return ComplexStructures.class;
	}
	
	public void testRoleCtor01() {
		runConformTest(
			new String[] {
				"SomeTeam.java",
				"public team class SomeTeam {\n" + 
				"\n" + 
				"	public class BGraph {\n" + 
				"		public BGraph() { }\n" + 
				"	}\n" + 
				"\n" + 
				"\n" + 
				"	public void test() {\n" + 
				"		final Features f = new Features();\n" + 
				"		BasicGraph<@f> g3 = f.newBasicGraphException();\n" + 
				"		g3.print();\n" + 
				"	}\n" + 
				"\n" + 
				"	public team class Features {\n" + 
				"		protected BasicGraph newBasicGraphException() {\n" + 
				"			return new BasicGraph();\n" + 
				"		}\n" + 
				"\n" + 
				"		public team class BasicGraph playedBy BGraph {\n" + 
				"			public BasicGraph() { base(); }\n" + 
				"\n" + 
				"			public void print() {\n" + 
				"				System.out.println(\"I am a basic graph ...\");\n" + 
				"			}\n" + 
				"		}\n" + 
				"	}\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new SomeTeam().test();\n" + 
				"	}\n" + 
				"}\n"
			},
			"I am a basic graph ...");
	}
	
	public void testMultiLevelSuper() {
		runConformTest(
			new String[] {
				"t/M.java",
				"package t;\n" +
				"import p.*;\n" +
				"public class M {\n" +
				"	public static void main(String... args) {\n" +
				"		new Team1().activate();\n" +
				"		new Base4().m(5);\n" +
				"		new Base4().m(0);\n" +
				"	}\n" +
				"}\n",
				"p/Base0.java",
				"package p;\n" +
				"public class Base0 {\n" +
				"	public void m(int i) {\n" +
				"		System.out.println(\"Base0.m()\");\n" +
				"	}\n" +
				"}\n",
				"p/Base1.java",
				"package p;\n" +
				"public class Base1 extends Base0 {\n" +
				"	public void m(int i) {\n" +
				"		if (i != 1)\n" +
				"			super.m(i);\n" +
				"	}\n" +
				"}\n",
				"p/Base2.java",
				"package p;\n" +
				"public class Base2 extends Base1 {\n" +
				"	public void m(int i) {\n" +
				"		if (i != 2)\n" +
				"			super.m(i);\n" +
				"	}\n" +
				"}\n",
				"p/Base3.java",
				"package p;\n" +
				"public class Base3 extends Base2 {\n" +
				"	public void m(int i) {\n" +
				"		if (i != 3)\n" +
				"			super.m(i);\n" +
				"	}\n" +
				"}\n",
				"p/Base4.java",
				"package p;\n" +
				"public class Base4 extends Base3 {\n" +
				"	public void m(int i) {\n" +
				"		if (i != 4)\n" +
				"			super.m(i);\n" +
				"	}\n" +
				"}\n",
				"t/Team0.java",
				"package t;\n" +
				"import base p.Base1;\n" +
				"import base p.Base3;\n" +
				"public team class Team0 {\n" +
				"	protected class R0 playedBy Base1 {\n" +
				"	}\n" +
				"	protected class R1 extends R0 playedBy Base3 { }\n" +
				"}\n",
				"t/Team1.java",
				"package t;\n" +
				"import base p.Base4;\n" +
				"public team class Team1 extends Team0 {\n" +
				"	protected class R2 extends R1 playedBy Base4 {\n" +
				"		void q(int i) <- replace void m(int i) base when (i == 5);\n" +
				"		callin void q(int i) {\n" +
				"			System.out.println(\"Role\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"Role\nBase0.m()");
	}
}
