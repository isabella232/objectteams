/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010, 2013 Stephan Herrmann
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
package org.eclipse.objectteams.otdt.tests.otjld.rolesandteams;

import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

import junit.framework.Test;

public class TeamNesting extends AbstractOTJLDTest {
	
	public TeamNesting(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "testBug411449" };
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return TeamNesting.class;
	}

    // a role inherits features from all possible super roles within nested teams - one level only
    // 1.1.10-otjld-full-inheritance-structure-1
    public void test1110_fullInheritanceStructure1() {
       
       runConformTest(
            new String[] {
		"T1110fis1.java",
			    "\n" +
			    "public class T1110fis1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team1110fis1_1 t = new Team1110fis1_1();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1110fis1_1.java",
			    "\n" +
			    "public team class Team1110fis1_1 {\n" +
			    "    public team class Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"11\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"11\");\n" +
			    "            }\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"11\");\n" +
			    "            }\n" +
			    "            public void m4() {\n" +
			    "                System.out.print(\"11\");\n" +
			    "            }\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"11\");\n" +
			    "            }\n" +
			    "            public void m6() {\n" +
			    "                System.out.print(\"11\");\n" +
			    "            }\n" +
			    "            public void m7() {\n" +
			    "                System.out.print(\"11\");\n" +
			    "            }\n" +
			    "            public void m8() {\n" +
			    "                System.out.print(\"11\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"12\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"12\");\n" +
			    "            }\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"12\");\n" +
			    "            }\n" +
			    "            public void m4() {\n" +
			    "                System.out.print(\"12\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "	    System.out.print(\"R1:\");\n" +
			    "            dotests(new R1());\n" +
			    "	    System.out.print(\"R2:\");\n" +
			    "	    dotests(new R2());\n" +
			    "        }\n" +
			    "        void dotests(R1 r) {\n" +
			    "            r.m1();\n" +
			    "            r.m2();\n" +
			    "            r.m3();\n" +
			    "            r.m4();\n" +
			    "            r.m5();\n" +
			    "            r.m6();\n" +
			    "            r.m7();\n" +
			    "            r.m8();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Mid2 extends Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"21\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"21\");\n" +
			    "            }\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"21\");\n" +
			    "            }\n" +
			    "            public void m6() {\n" +
			    "                System.out.print(\"21\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"22\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"22\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test () {\n" +
			    "        Mid1 m = new Mid1();\n" +
			    "	System.out.print(\"-M1\");\n" +
			    "        m.test();\n" +
			    "        m = new Mid2();\n" +
			    "	System.out.print(\"-M2\");\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "-M1R1:1111111111111111R2:1212121211111111-M2R1:2121111121211111R2:2222121221211111");
    }

    // a role inherits features from all possible super roles within nested teams - one compilation
    // 1.1.10-otjld-full-inheritance-structure-2
    public void test1110_fullInheritanceStructure2() {
       
       runConformTest(
            new String[] {
		"T1110fis2.java",
			    "\n" +
			    "public class T1110fis2 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team1110fis2_2 t = new Team1110fis2_2();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1110fis2_2.java",
			    "\n" +
			    "public team class Team1110fis2_2 extends Team1110fis2_1 {\n" +
			    "    public team class Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"211\");\n" +
			    "            }\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"211\");\n" +
			    "            }\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"211\");\n" +
			    "            }\n" +
			    "            public void m7() {\n" +
			    "                System.out.print(\"211\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"212\");\n" +
			    "            }\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"212\");\n" +
			    "            }            \n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Mid2 {\n" +
			    "        public class R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"221\");\n" +
			    "            }\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"221\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"222\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1110fis2_1.java",
			    "\n" +
			    "public team class Team1110fis2_1 {\n" +
			    "    public team class Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m4() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m6() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m7() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m8() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"112\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"112\");\n" +
			    "            }\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"112\");\n" +
			    "            }\n" +
			    "            public void m4() {\n" +
			    "                System.out.print(\"112\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "	    System.out.print(\"R1:\");\n" +
			    "            dotests(new R1());\n" +
			    "	    System.out.print(\"R2:\");\n" +
			    "	    dotests(new R2());\n" +
			    "        }\n" +
			    "        void dotests(R1 r) {\n" +
			    "            r.m1();System.out.print(\".\");\n" +
			    "            r.m2();System.out.print(\".\");\n" +
			    "            r.m3();System.out.print(\".\");\n" +
			    "            r.m4();System.out.print(\".\");\n" +
			    "            r.m5();System.out.print(\".\");\n" +
			    "            r.m6();System.out.print(\".\");\n" +
			    "            r.m7();System.out.print(\".\");\n" +
			    "            r.m8();System.out.print(\".\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Mid2 extends Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"121\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"121\");\n" +
			    "            }\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"121\");\n" +
			    "            }\n" +
			    "            public void m6() {\n" +
			    "                System.out.print(\"121\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"122\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"122\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test () {\n" +
			    "        Mid1 m = new Mid1();\n" +
			    "	System.out.print(\"-M1\");\n" +
			    "        m.test();\n" +
			    "        m = new Mid2();\n" +
			    "	System.out.print(\"-M2\");\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "-M1R1:211.111.211.111.211.111.211.111.R2:212.112.212.112.211.111.211.111.-M2R1:221.121.211.111.221.121.211.111.R2:222.122.212.112.221.121.211.111.");
    }

    // a role inherits features from all possible super roles within nested teams - separate compilation
    // 1.1.10-otjld-full-inheritance-structure-3
    public void test1110_fullInheritanceStructure3() {
       
       runConformTest(
            new String[] {
		"T1110fis3.java",
			    "\n" +
			    "public class T1110fis3 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team1110fis3_2 t = new Team1110fis3_2();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1110fis3_1.java",
			    "\n" +
			    "public team class Team1110fis3_1 {\n" +
			    "    public team class Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m4() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m6() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m7() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m8() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"112\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"112\");\n" +
			    "            }\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"112\");\n" +
			    "            }\n" +
			    "            public void m4() {\n" +
			    "                System.out.print(\"112\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "	    System.out.print(\"R1:\");\n" +
			    "            dotests(new R1());\n" +
			    "	    System.out.print(\"R2:\");\n" +
			    "	    dotests(new R2());\n" +
			    "        }\n" +
			    "        void dotests(R1 r) {\n" +
			    "            r.m1();System.out.print(\".\");\n" +
			    "            r.m2();System.out.print(\".\");\n" +
			    "            r.m3();System.out.print(\".\");\n" +
			    "            r.m4();System.out.print(\".\");\n" +
			    "            r.m5();System.out.print(\".\");\n" +
			    "            r.m6();System.out.print(\".\");\n" +
			    "            r.m7();System.out.print(\".\");\n" +
			    "            r.m8();System.out.print(\".\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Mid2 extends Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"121\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"121\");\n" +
			    "            }\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"121\");\n" +
			    "            }\n" +
			    "            public void m6() {\n" +
			    "                System.out.print(\"121\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"122\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"122\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test () {\n" +
			    "        Mid1 m = new Mid1();\n" +
			    "	System.out.print(\"-M1\");\n" +
			    "        m.test();\n" +
			    "        m = new Mid2();\n" +
			    "	System.out.print(\"-M2\");\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1110fis3_2.java",
			    "\n" +
			    "public team class Team1110fis3_2 extends Team1110fis3_1 {\n" +
			    "    public team class Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"211\");\n" +
			    "            }\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"211\");\n" +
			    "            }\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"211\");\n" +
			    "            }\n" +
			    "            public void m7() {\n" +
			    "                System.out.print(\"211\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"212\");\n" +
			    "            }\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"212\");\n" +
			    "            }            \n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Mid2 {\n" +
			    "        public class R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"221\");\n" +
			    "            }\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"221\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"222\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "-M1R1:211.111.211.111.211.111.211.111.R2:212.112.212.112.211.111.211.111.-M2R1:221.121.211.111.221.121.211.111.R2:222.122.212.112.221.121.211.111.");
    }

    // a role inherits features from all possible super roles within nested teams - more combinations - separate compilation
    // 1.1.10-otjld-full-inheritance-structure-4
    public void test1110_fullInheritanceStructure4() {
       
       runConformTest(
            new String[] {
		"T1110fis4.java",
			    "\n" +
			    "public class T1110fis4 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team1110fis4_2 t = new Team1110fis4_2();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1110fis4_1.java",
			    "\n" +
			    "public team class Team1110fis4_1 {\n" +
			    "    public team class Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "            public void m8() {\n" +
			    "                System.out.print(\"111\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m4() {\n" +
			    "                System.out.print(\"112\");\n" +
			    "            }\n" +
			    "            public void m8() {\n" +
			    "                System.out.print(\"112\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Mid2 extends Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"121\");\n" +
			    "            }\n" +
			    "            public void m7() {\n" +
			    "                System.out.print(\"121\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"122\");\n" +
			    "            }\n" +
			    "            public void m6() {\n" +
			    "                System.out.print(\"122\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1110fis4_2.java",
			    "\n" +
			    "public team class Team1110fis4_2 extends Team1110fis4_1 {\n" +
			    "    public team class Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m6() {\n" +
			    "                System.out.print(\"211\");\n" +
			    "            }\n" +
			    "            public void m7() {\n" +
			    "                System.out.print(\"211\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"212\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Mid2 {\n" +
			    "        public class R1 {\n" +
			    "            public void m1() {\n" +
			    "                System.out.print(\"221\");\n" +
			    "            }\n" +
			    "            public void m2() {\n" +
			    "                System.out.print(\"221\");\n" +
			    "            }\n" +
			    "            public void m3() {\n" +
			    "                System.out.print(\"221\");\n" +
			    "            }\n" +
			    "            public void m4() {\n" +
			    "                System.out.print(\"221\");\n" +
			    "            }\n" +
			    "            public void m5() {\n" +
			    "                System.out.print(\"221\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "        }\n" +
			    "        void dotests(R1 r) {\n" +
			    "            r.m1();System.out.print(\".\");\n" +
			    "            r.m2();System.out.print(\".\");\n" +
			    "            r.m3();System.out.print(\".\");\n" +
			    "            r.m4();System.out.print(\".\");\n" +
			    "            r.m5();System.out.print(\".\");\n" +
			    "            r.m6();System.out.print(\".\");\n" +
			    "            r.m7();System.out.print(\".\");\n" +
			    "            r.m8();System.out.print(\".\");\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "	    System.out.print(\"R1:\");\n" +
			    "            dotests(new R1());\n" +
			    "	    System.out.print(\"R2:\");\n" +
			    "	    dotests(new R2());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test () {\n" +
			    "        Mid2 m = new Mid2();\n" +
			    "	System.out.print(\"-M2\");\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "-M2R1:221.221.221.221.221.211.121.111.R2:212.122.221.112.221.122.121.112.");
    }

    // a role inherits features from all possible super roles within nested teams - super and tsuper tree (note, that some of these are re-bound, so full tree is not possible.
    // 1.1.10-otjld-full-inheritance-structure-5
    public void test1110_fullInheritanceStructure5() {
       
       runConformTest(
            new String[] {
		"T1110fis5.java",
			    "\n" +
			    "public class T1110fis5 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team1110fis5_2 t = new Team1110fis5_2();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1110fis5_1.java",
			    "\n" +
			    "public team class Team1110fis5_1 {\n" +
			    "    public team class Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"111.\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"112.\");\n" +
			    "                super.m();\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Mid2 extends Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"121.\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"122.\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1110fis5_2.java",
			    "\n" +
			    "public team class Team1110fis5_2 extends Team1110fis5_1 {\n" +
			    "    public team class Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"211.\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"212.\");\n" +
			    "                tsuper.m();\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Mid2 {\n" +
			    "        public class R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"221.\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"222.\");\n" +
			    "                tsuper.m();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            R2 r = new R2();\n" +
			    "            r.m();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test () {\n" +
			    "        Mid2 m = new Mid2();\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "222.122.");
    }

    // a role inherits features from all possible super roles within nested teams - super and tsuper tree (note, that some of these are re-bound, so full tree is not possible.
    // use qualified tsuper to achieve order of old tsuper-precedence (pre 0.8M3)
    // 1.1.10-otjld-full-inheritance-structure-5
    public void test1110_fullInheritanceStructure5a() {
       
       runConformTest(
            new String[] {
		"T1110fis5a.java",
			    "\n" +
			    "public class T1110fis5a {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team1110fis5a_2 t = new Team1110fis5a_2();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1110fis5a_1.java",
			    "\n" +
			    "public team class Team1110fis5a_1 {\n" +
			    "    public team class Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"111.\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"112.\");\n" +
			    "                super.m();\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Mid2 extends Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"121.\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"122.\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1110fis5a_2.java",
			    "\n" +
			    "public team class Team1110fis5a_2 extends Team1110fis5a_1 {\n" +
			    "    public team class Mid1 {\n" +
			    "        public class R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"211.\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"212.\");\n" +
			    "                tsuper.m();\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Mid2 {\n" +
			    "        public class R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"221.\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class R2 extends R1 {\n" +
			    "            public void m() {\n" +
			    "                System.out.print(\"222.\");\n" +
			    "                Mid2.tsuper.m();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            R2 r = new R2();\n" +
			    "            r.m();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test () {\n" +
			    "        Mid2 m = new Mid2();\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "222.212.112.221.");
    }

    // A role inherits callin bindings from several tsuper classes
    // 1.1.11-otjld-role-inherits-callin-from-tsupers-1
    public void test1111_roleInheritsCallinFromTsupers1() {
       
       runConformTest(
            new String[] {
		"Team1111ricft1_2.java",
			    "\n" +
			    "public team class Team1111ricft1_2 extends Team1111ricft1_1 {\n" +
			    "	protected abstract team class Mid1 {\n" +
			    "		protected abstract class R {\n" +
			    "			void print3() { System.out.print(\"!\"); }\n" +
			    "			print3 <- after test3;\n" +
			    "		}\n" +
			    "	}\n" +
			    "	protected team class Mid2 { // not abstract\n" +
			    "		protected class R {} // not abstract\n" +
			    "	}\n" +
			    "	Team1111ricft1_2() {\n" +
			    "		Mid2 m = new Mid2();\n" +
			    "		m.activate();\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team1111ricft1_2();\n" +
			    "		T1111ricft1 b = new T1111ricft1();\n" +
			    "		b.test1();\n" +
			    "		b.test2();\n" +
			    "		b.test3();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T1111ricft1.java",
			    "\n" +
			    "public class T1111ricft1 {\n" +
			    "	public void test1() {}\n" +
			    "	public void test2() {}\n" +
			    "	public void test3() {}\n" +
			    "}\n" +
			    "	\n",
		"Team1111ricft1_1.java",
			    "\n" +
			    "public team class Team1111ricft1_1 {\n" +
			    "	protected abstract team class Mid1 {\n" +
			    "		protected abstract class R playedBy T1111ricft1 {\n" +
			    "			abstract void print1();\n" +
			    "			print1 <- before test1;\n" +
			    "		}\n" +
			    "	}\n" +
			    "	protected team class Mid2 extends Mid1 {\n" +
			    "		protected class R {\n" +
			    "			void print1() { System.out.print(\"O\"); }\n" +
			    "			void print2() { System.out.print(\"K\"); }\n" +
			    "			print2 <- after test2;\n" +
			    "		}\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK!");
    }

    // A role inherits named callin bindings from several tsuper classes
    // 1.1.11-otjld-role-inherits-callin-from-tsupers-2
    public void test1111_roleInheritsCallinFromTsupers2() {
       
       runConformTest(
            new String[] {
		"Team1111ricft2_2.java",
			    "\n" +
			    "public team class Team1111ricft2_2 extends Team1111ricft2_1 {\n" +
			    "	protected abstract team class Mid1 {\n" +
			    "		protected abstract class R {\n" +
			    "			void print3() { System.out.print(\"!\"); }\n" +
			    "			c3: print3 <- after test3;\n" +
			    "		}\n" +
			    "	}\n" +
			    "	protected team class Mid2 { // not abstract\n" +
			    "		protected class R {} // not abstract\n" +
			    "	}\n" +
			    "	Team1111ricft2_2() {\n" +
			    "		Mid2 m = new Mid2();\n" +
			    "		m.activate();\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team1111ricft2_2();\n" +
			    "		T1111ricft2 b = new T1111ricft2();\n" +
			    "		b.test1();\n" +
			    "		b.test2();\n" +
			    "		b.test3();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T1111ricft2.java",
			    "\n" +
			    "public class T1111ricft2 {\n" +
			    "	public void test1() {}\n" +
			    "	public void test2() {}\n" +
			    "	public void test3() {}\n" +
			    "}\n" +
			    "	\n",
		"Team1111ricft2_1.java",
			    "\n" +
			    "public team class Team1111ricft2_1 {\n" +
			    "	protected abstract team class Mid1 {\n" +
			    "		protected abstract class R playedBy T1111ricft2 {\n" +
			    "			abstract void print1();\n" +
			    "			c1: print1 <- before test1;\n" +
			    "		}\n" +
			    "	}\n" +
			    "	protected team class Mid2 extends Mid1 {\n" +
			    "		protected class R {\n" +
			    "			void print1() { System.out.print(\"O\"); }\n" +
			    "			void print2() { System.out.print(\"K\"); }\n" +
			    "			c2: print2 <- after test2;\n" +
			    "		}\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK!");
    }

    // a nested team overrides a regular role
    // 1.1.12-otjld-team-overrides-regular-1
    public void test1112_teamOverridesRegular1() {
       
       runConformTest(
            new String[] {
		"Team1112tor1_2.java",
			    "\n" +
			    "public team class Team1112tor1_2 extends Team1112tor1_1 {\n" +
			    "	protected team class R {\n" +
			    "		protected class InnerR {\n" +
			    "			protected void test() {\n" +
			    "				System.out.print(\"K\");\n" +
			    "			}\n" +
			    "		}\n" +
			    "		protected void test() {\n" +
			    "			tsuper.test();\n" +
			    "			InnerR ir = new InnerR();\n" +
			    "			ir.test();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	Team1112tor1_2() {\n" +
			    "		test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team1112tor1_2();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team1112tor1_1.java",
			    "\n" +
			    "public team class Team1112tor1_1 {\n" +
			    "	protected class R {\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(\"O\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	void test() {\n" +
			    "		R r = new R();\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a regular role tries to override a team role
    // 1.1.13-otjld-regular-overrides-team
    public void test1113_regularOverridesTeam() {
        runNegativeTestMatching(
            new String[] {
		"Team1113rot_2.java",
			    "\n" +
			    "public team class Team1113rot_2 extends Team1113rot_1 {\n" +
			    "	protected class R {}\n" +
			    "}\n" +
			    "	\n",
		"Team1113rot_1.java",
			    "\n" +
			    "public team class Team1113rot_1 {\n" +
			    "	protected team class R {}\n" +
			    "}\n" +
			    "	\n"
            },
            "1.5(a)");
    }

    // simple tsuper is used to select the closest version among different tsupers in a nested team
    // 1.1.14-otjld-tsuper-in-nested-1
    public void test1114_tsuperInNested1() {
       
       runConformTest(
            new String[] {
		"Team1114tin1_2.java",
			    "\n" +
			    "public team class Team1114tin1_2 {\n" +
			    "    protected team class Mid1 {\n" +
			    "        protected class R {\n" +
			    "            void test() {\n" +
			    "                System.out.print(\"OK\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 {\n" +
			    "        protected class R {\n" +
			    "            protected void test() {\n" +
			    "                tsuper.test();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            R r = new R();\n" +
			    "            r.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public Team1114tin1_2() {\n" +
			    "        Mid2 m = new Mid2();\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1114tin1_2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team1114tin1_1.java",
			    "\n" +
			    "public team class Team1114tin1_1 {\n" +
			    "    protected team class Mid1 {\n" +
			    "        protected class R {\n" +
			    "            void test() {\n" +
			    "                System.out.print(\"NOTOK1\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 {\n" +
			    "        protected class R {\n" +
			    "            void test() {\n" +
			    "                System.out.print(\"NOTOK2\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // qualified tsuper is used to select the closest version among different tsupers in a nested team
    // 1.1.14-otjld-tsuper-in-nested-1a
    public void test1114_tsuperInNested1a() {
       
       runConformTest(
            new String[] {
		"Team1114tin1a_2.java",
			    "\n" +
			    "public team class Team1114tin1a_2 {\n" +
			    "    protected team class Mid1 {\n" +
			    "        protected class R {\n" +
			    "            void test() {\n" +
			    "                System.out.print(\"OK\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 {\n" +
			    "        protected class R {\n" +
			    "            protected void test() {\n" +
			    "                Mid2.tsuper.test();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            R r = new R();\n" +
			    "            r.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public Team1114tin1a_2() {\n" +
			    "        Mid2 m = new Mid2();\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1114tin1a_2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team1114tin1a_1.java",
			    "\n" +
			    "public team class Team1114tin1a_1 {\n" +
			    "    protected team class Mid1 {\n" +
			    "        protected class R {\n" +
			    "            void test() {\n" +
			    "                System.out.print(\"NOTOK1\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 {\n" +
			    "        protected class R {\n" +
			    "            void test() {\n" +
			    "                System.out.print(\"NOTOK2\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // qualified tsuper is used to select a version among different tsupers in a nested team
    // 1.1.14-otjld-tsuper-in-nested-2
    public void test1114_tsuperInNested2() {
       
       runConformTest(
            new String[] {
		"Team1114tin2_2.java",
			    "\n" +
			    "public team class Team1114tin2_2 extends Team1114tin2_1 {\n" +
			    "    protected team class Mid1 {\n" +
			    "        protected class R {\n" +
			    "            void test() {\n" +
			    "                System.out.print(\"NOTOK2\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 {\n" +
			    "        protected class R {\n" +
			    "            void test() {\n" +
			    "                Team1114tin2_2.tsuper.test();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            R r = new R();\n" +
			    "            r.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public Team1114tin2_2() {\n" +
			    "        Mid2 m = new Mid2();\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1114tin2_2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team1114tin2_1.java",
			    "\n" +
			    "public team class Team1114tin2_1 {\n" +
			    "    protected team class Mid1 {\n" +
			    "        protected class R {\n" +
			    "            void test() {\n" +
			    "                System.out.print(\"NOTOK1\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 {\n" +
			    "        protected class R {\n" +
			    "            void test() {\n" +
			    "                System.out.print(\"OK\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }
    
    // Bug 330002 - Wrong linearization of tsuper calls in diamond inheritance
    public void _test1114_tsuperInNested3() {
    	runConformTest(
    		new String[] {
    	"T1114tin3Main.java",
    			"public class T1114tin3Main {\n" +
    			"    public static void main(String[] args) {\n" +
    			"        new Team1114tin3_1().test();\n" +
    			"    }\n" +
    			"}\n",
    	"Team1114tin3_0.java",
    			"public team class Team1114tin3_0 {\n" + 
    			"    protected abstract team class TeamA {\n" + 
    			"        protected abstract class R {\n" + 
    			"            protected void print() { System.out.println(\"topR\"); }\n" + 
    			"        }\n" + 
    			"        public void test() {\n" + 
    			"            System.out.println(\"topT\");\n" + 
    			"            new R().print();\n" + 
    			"        }\n" + 
    			"    }\n" + 
    			"\n" + 
    			"    protected team class TeamB extends TeamA {\n" + 
    			"        protected class R {\n" + 
    			"            protected void print() {\n" + 
    			"                System.out.println(\"0B\");\n" + 
    			"                tsuper.print();\n" + 
    			"            }\n" + 
    			"        }\n" + 
    			"        public void test() {\n" + 
    			"            System.out.println(\"TeamB\");\n" + 
    			"            super.test();\n" + 
    			"        }\n" + 
    			"    }\n" + 
    			"}",
    	"Team1114tin3_1.java",
    			"public team class Team1114tin3_1 extends Team1114tin3_0 {\n" + 
    			"    @Override\n" + 
    			"    protected team class TeamA {\n" + 
    			"        @Override\n" + 
    			"        protected class R {\n" + 
    			"            @Override\n" + 
    			"            protected void print() {\n" + 
    			"                System.out.println(\"1A\");\n" + 
    			"                tsuper.print();\n" + 
    			"            }\n" + 
    			"        }\n" + 
    			"        @Override\n" + 
    			"        public void test() {\n" + 
    			"            System.out.println(\"TeamA\");\n" + 
    			"            tsuper.test();\n" + 
    			"        }\n" + 
    			"    }\n" + 
    			"    protected team class TeamB {\n" + 
    			"        protected class R {\n" + 
    			"            protected void print() {\n" + 
    			"                System.out.println(\"bottomB\");\n" + 
    			"                tsuper.print();\n" + 
    			"            }\n" + 
    			"        }\n" + 
    			"    }\n" + 
    			"    void test() {\n" + 
    			"        new TeamB().test();\n" + 
    			"    }\n" + 
    			"}"
    		},
    		"TeamB\n" +
    		"TeamA\n" +
    		"topT\n" +
    		"0B\n" +
    		"1A\n" +
    		"topR\n");
    }

    // a nested team extends a toplevel team, role signature contains role
    // 1.1.15-otjld-nested-team-extends-regular-team-1
    public void test1115_nestedTeamExtendsRegularTeam1() {
       
       runConformTest(
            new String[] {
		"Team1115ntert1.java",
			    "\n" +
			    "public team class Team1115ntert1 {\n" +
			    "    public team class Team1115ntert1_2 extends Team1115ntert1_1 {\n" +
			    "        protected void test() {\n" +
			    "            R r1 = new R(\"O\");\n" +
			    "            R r2 = new R(\"K\");\n" +
			    "            r1.test(r2);\n" +
			    "        }        \n" +
			    "    }\n" +
			    "    public Team1115ntert1 () {\n" +
			    "        Team1115ntert1_2 t = new Team1115ntert1_2();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1115ntert1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1115ntert1_1.java",
			    "\n" +
			    "public abstract team class Team1115ntert1_1 {\n" +
			    "    protected class R {\n" +
			    "        protected String val;\n" +
			    "        protected R(String v) {\n" +
			    "            val = v;\n" +
			    "        }\n" +
			    "        protected void test(R other) {\n" +
			    "            System.out.print(val+other.val);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }
    
    // a nested team extends a toplevel team, role signature contains role, constructor not visible
    // 1.1.15-otjld-nested-team-extends-regular-team-1
    public void test1115_nestedTeamExtendsRegularTeam1f() {
       
       runNegativeTest(
            new String[] {
		"Team1115ntert1f.java",
			    "\n" +
			    "public team class Team1115ntert1f {\n" +
			    "    public team class Team1115ntert1f_2 extends Team1115ntert1f_1 {\n" +
			    "        protected void test() {\n" +
			    "            R r1 = new R(\"O\");\n" +
			    "            R r2 = new R(\"K\");\n" +
			    "            r1.test(r2);\n" +
			    "        }        \n" +
			    "    }\n" +
			    "}\n",
		"Team1115ntert1f_1.java",
			    "\n" +
			    "public abstract team class Team1115ntert1f_1 {\n" +
			    "    protected class R {\n" +
			    "        protected String val;\n" +
			    "        R(String v) {\n" +
			    "            val = v;\n" +
			    "        }\n" +
			    "        protected void test(R other) {\n" +
			    "            System.out.print(val+other.val);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n"
            },
            "----------\n" + 
    		"1. ERROR in Team1115ntert1f.java (at line 5)\n" + 
    		"	R r1 = new R(\"O\");\n" + 
    		"	       ^^^^^^^^^^\n" + 
    		"The role constructor Team1115ntert1f.Team1115ntert1f_2.R(String) is not visible (OTJLD 1.2.1(e)).\n" + 
    		"----------\n" + 
    		"2. ERROR in Team1115ntert1f.java (at line 6)\n" + 
    		"	R r2 = new R(\"K\");\n" + 
    		"	       ^^^^^^^^^^\n" + 
    		"The role constructor Team1115ntert1f.Team1115ntert1f_2.R(String) is not visible (OTJLD 1.2.1(e)).\n" + 
    		"----------\n");
    }

    // A role has incompatible tsuper and ttsuper
    // 1.1.16-otjld_testbug-incompatible-tsupers-1
    public void _testbug_test1116_incompatibleTsupers1() {
        runNegativeTestMatching(
            new String[] {
		"Team1116it1_1.java",
			    "\n" +
			    "public team class Team1116it1_1 {\n" +
			    "    protected team class Mid1 {\n" +
			    "        //protected class R1 {}\n" +
			    "        //protected class R2 extends R1 {}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1116it1_2.java",
			    "\n" +
			    "public team class Team1116it1_2 extends Team1116it1_1 {\n" +
			    "    protected team class Mid0 {\n" +
			    "        protected class R0 {}\n" +
			    "        //protected class R2 /*extends R0*/ {}\n" +
			    "    }\n" +
			    "    protected team class Mid1 extends Mid0 {\n" +
			    "        protected class R1 extends R0 {}\n" +
			    "        //protected class R2 extends R1 {}\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "1.5(a)");
    }

    // a nested team has three levels of sub-teams
    // 1.1.17-otjld-multilevel-inner-inheritance-1
    public void test1117_multilevelInnerInheritance1() {
        runConformTest(
            new String[] {
		"n1117/Team1117mii1.java",
			    "\n" +
			    "package n1117;\n" +
			    "public team class Team1117mii1 {\n" +
			    "    public team class T0 \n" +
			    "    { \n" +
			    "        public class R1 {} \n" +
			    "        public class R2 {} \n" +
			    "        public class R3 {} \n" +
			    "    } \n" +
			    "	 @SuppressWarnings(\"override\")\n" +
			    "    public team class T1 extends T0 \n" +
			    "    { \n" +
			    "        public class R1 {} \n" +
			    "        public class R2 {} \n" +
			    "        public class R3 {} \n" +
			    "    } \n" +
			    "	 @SuppressWarnings(\"override\")\n" +
			    "    public team class T2 extends T1 \n" +
			    "    { \n" +
			    "        public class R1 {} \n" +
			    "        public class R2 {} \n" +
			    "        public class R3 {} \n" +
			    "    } \n" +
			    "	 @SuppressWarnings(\"override\")\n" +
			    "    public team class T3 extends T2 \n" +
			    "    { \n" +
			    "        public class R1 {} \n" +
			    "        public class R2 {} \n" +
			    "        public class R3 {} \n" +
			    "    } \n" +
			    "}\n" +
			    "    \n"
            });
    }

    // a nested team has two levels of sub-teams
    // 1.1.17-otjld-multilevel-inner-inheritance-2
    public void test1117_multilevelInnerInheritance2() {
        runConformTest(
            new String[] {
		"n1117/Team1117mii2.java",
			    "\n" +
			    "package n1117;\n" +
			    "public team class Team1117mii2 {\n" +
			    "    public team class T0 \n" +
			    "    { \n" +
			    "        public class R1 {} \n" +
			    "        public class R2 {} \n" +
			    "        public class R3 {} \n" +
			    "    } \n" +
			    "    public team class T1 extends T0 \n" +
			    "    { \n" +
			    "        public class R1 {} \n" +
			    "        public class R2 {} \n" +
			    "        public class R3 {} \n" +
			    "    } \n" +
			    "    public team class T2 extends T1 \n" +
			    "    { \n" +
			    "        public class R1 {} \n" +
			    "        public class R2 {} \n" +
			    "        public class R3 {} \n" +
			    "    } \n" +
			    "}\n" +
			    "    \n"
            });
    }

    // a nested team has two levels of sub-teams - roles are in role files
    // 1.1.17-otjld-multilevel-inner-inheritance-3
    public void test1117_multilevelInnerInheritance3() {
    	myWriteFiles(new String[]{
			"n1117/Team1117mii3.java",
				    "\n" +
				    "package n1117;\n" +
				    "public team class Team1117mii3 {\n" +
				    "}\n"
    		});
        runConformTest(
            new String[] {
        "n1117/T1117mii3Main.java",
        		"package n1117;\n" +
        		"public class T1117mii3Main {\n" +
        		"	public static void main(String... args) {\n" +
        		"		final Team1117mii3 outer = new Team1117mii3();\n" +
        		"		final T1117mii3_2<@outer> inner = new T1117mii3_2<@outer>();\n" +
        		"		System.out.print(inner.new R3());\n" +
        		"	}\n" +
        		"}\n",
		"n1117/Team1117mii3/T1117mii3_2.java",
			    "\n" +
			    "team package n1117.Team1117mii3;\n" +
			    "public team class T1117mii3_2 extends T1117mii3_1 \n" +
			    "{ \n" +
			    "	 @Override\n" +
			    "    public class R1 {} \n" +
			    "	 @Override\n" +
			    "    public class R2 {} \n" +
			    "	 @Override\n" +
			    "    public class R3 {} \n" +
			    "} \n" +
			    "    \n",
		"n1117/Team1117mii3/T1117mii3_0.java",
			    "\n" +
			    "team package n1117.Team1117mii3;\n" +
			    "public team class T1117mii3_0 \n" +
			    "{ \n" +
			    "    public class R1 {} \n" +
			    "    public class R2 {} \n" +
			    "    public class R3 {} \n" +
			    "} \n" +
			    "    \n",
		"n1117/Team1117mii3/T1117mii3_1.java",
			    "\n" +
			    "team package n1117.Team1117mii3;\n" +
			    "public team class T1117mii3_1 extends T1117mii3_0 \n" +
			    "{ \n" +
			    "	 @Override\n" +
			    "    public class R1 {} \n" +
			    "	 @Override\n" +
			    "    public class R2 {} \n" +
			    "	 @Override\n" +
			    "    public class R3 {} \n" +
			    "} \n" +
			    "    \n"
            },
		    null, /*expectedOutput*/
            null,/*class libraries*/
            false,/*flushOutputDirectory*/
            null/*vmarguments*/);
    }

    // roles introduce extends
    // 1.1.18-otjld-role-overrides-extends-1
    public void test1118_roleOverridesExtends1() {
        runConformTest(
            new String[] {
		"n1118/Team1118roe1_2.java",
			    "\n" +
			    "package n1118;\n" +
			    "public team class  Team1118roe1_2 extends Team1118roe1_1\n" +
			    "{ \n" +
			    "    public team class T1 {\n" +
			    "        public class R1 {} \n" +
			    "        public class R2 extends R1{} \n" +
			    "        public class R3 {} \n" +
			    "    }\n" +
			    "    public team class T2 extends T1 {\n" +
			    "        public class R1 {} \n" +
			    "        public class R2 {} \n" +
			    "        public class R3 {} \n" +
			    "    }\n" +
			    "} \n" +
			    "    \n",
		"n1118/Team1118roe1_1.java",
			    "\n" +
			    "package n1118;\n" +
			    "public team class  Team1118roe1_1\n" +
			    "{ \n" +
			    "    public team class T1 {\n" +
			    "        public class R1 {} \n" +
			    "        public class R2 {} \n" +
			    "        public class R3 extends R2 {} \n" +
			    "    }\n" +
			    "    public team class T2 extends T1 {\n" +
			    "        public class R1 {} \n" +
			    "        public class R2 {} \n" +
			    "        public class R3 {} \n" +
			    "    }\n" +
			    "} \n" +
			    "    \n"
            });
    }

    // roles inherits incompatible "extends"
    // was org.eclipse.objectteams.otdt.tests.superhierarchy.OTSuperTypeHierarchyTest013.testGetAllSuperclasses_TA2TB2TC2R1()
    public void test1118_roleOverridesExtends2() {
        runNegativeTest(
            new String[] {
		"n1118/Team1118roe2_2.java",
				"package n1118;\n" + 
				"public team class Team1118roe2_2 extends Team1118roe2_1 {\n" + 
				"        @Override\n"+
				"        public team class TB1 {\n" + 
				"                @Override\n"+
				"                public team class TC1 {\n" + 
				"                        @Override\n"+
				"                        public class R1 {}\n" + 
				"                }\n" + 
				"                @Override\n"+
				"                public team class TC2 extends TC1 {\n" + 
				"                        @Override\n"+
				"                        public class R1 extends C2 {}\n" + 
				"                }\n" + 
				"        }\n" + 
				"        @Override\n"+
				"        public team class TB2 extends TB1 {\n" + 
				"                @Override\n"+
				"                public team class TC1 {\n" + 
				"                        @Override\n"+
				"                        public class R1 extends C1 {}\n" + 
				"                }\n" + 
				"                @Override\n"+
				"                public team class TC2 {\n" + 
				"                        @Override\n"+
				"                        public class R1 {}\n" + 
				"                }\n" + 
				"        }\n" + 
				"}",
		"n1118/Team1118roe2_1.java",
				"package n1118;\n" + 
				"public team class Team1118roe2_1 {\n" + 
				"        public team class TB1 {\n" + 
				"                public team class TC1 {\n" + 
				"                        public class R1 {}\n" + 
				"                }\n" + 
				"                public team class TC2 extends TC1 {\n" + 
				"                        @Override\n"+
				"                        public class R1 {}\n" + 
				"                }\n" + 
				"        }\n" + 
				"        public team class TB2 extends TB1 {\n" + 
				"                @Override\n"+
				"                public team class TC1 {\n" + 
				"                        @Override\n"+
				"                        public class R1 {}\n" + 
				"                }\n" + 
				"                @Override\n"+
				"                public team class TC2 {\n" + 
				"                        @Override\n"+
				"                        public class R1 extends C3 {}\n" + 
				"                }\n" + 
				"        }\n" + 
				"}",
		"n1118/C0.java",
				"package n1118;\n" + 
				"public class C0 {}\n",
		"n1118/C1.java",
				"package n1118;\n" + 
				"public class C1 extends C0 {}\n",
		"n1118/C2.java",
				"package n1118;\n" + 
				"public class C2 extends C0 {}\n",
		"n1118/C3.java",
				"package n1118;\n" + 
				"public class C3 extends C0 {}\n",
            },
            "----------\n" + 
    		"1. ERROR in n1118\\Team1118roe2_2.java (at line 26)\n" + 
    		"	public class R1 {}\n" + 
    		"	             ^^\n" + 
    		"Role inherits incompatible \'extends\' declarations: C1 is not a sub-type of C2 (\'extends C2\' is inherited is from n1118.Team1118roe2_2.TB1.TC2.R1) (OTJLD 1.3.2(b)).\n" + 
    		"----------\n" + 
    		"2. ERROR in n1118\\Team1118roe2_2.java (at line 26)\n" + 
    		"	public class R1 {}\n" + 
    		"	             ^^\n" + 
    		"Role inherits incompatible \'extends\' declarations: C1 is not a sub-type of C3 (\'extends C3\' is inherited is from n1118.Team1118roe2_1.TB2.TC2.R1) (OTJLD 1.3.2(b)).\n" + 
    		"----------\n");
    }

    // some roles are missing in the lattice of tsuper types
    // 1.1.19-otjld-incomplete-tsuper-structure-1
    public void test1119_incompleteTsuperStructure1() {
       
       runConformTest(
            new String[] {
		"Team1119its1_2.java",
			    "\n" +
			    "public team class Team1119its1_2 extends Team1119its1_1 {\n" +
			    "    public team class M1 {\n" +
			    "        protected class R2 { \n" +
			    "            void m() { System.out.print(\"R2.m;\"); } \n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class M2 extends M1 {\n" +
			    "        protected class R3 {\n" +
			    "            protected void m() { System.out.print(\"R3.m;\"); }\n" +
			    "        }\n" +
			    "        protected void run() {\n" +
			    "            (new R1()).m();\n" +
			    "            (new R2()).m();\n" +
			    "            (new R3()).m();        \n" +
			    "        }\n" +
			    "    }\n" +
			    "    public Team1119its1_2() {\n" +
			    "        (new M2()).run();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1119its1_2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team1119its1_1.java",
			    "\n" +
			    "public team class Team1119its1_1 {\n" +
			    "    public team class M1 {\n" +
			    "        protected class R1 { void m() {} }\n" +
			    "        protected class R2 { void m() {} }\n" +
			    "        protected class R3 { void m() {} }\n" +
			    "    }\n" +
			    "    public team class M2 extends M1 {\n" +
			    "        protected class R1 {\n" +
			    "            void m() { System.out.print(\"R1.m;\"); }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "R1.m;R2.m;R3.m;");
    }

    // some roles are missing in the lattice of tsuper types
    // 1.1.19-otjld-incomplete-tsuper-structure-2
    public void test1119_incompleteTsuperStructure2() {
       
       runConformTest(
            new String[] {
		"Team1119its2_2.java",
			    "\n" +
			    "public team class Team1119its2_2 extends Team1119its2_1 {\n" +
			    "    public team class M1 {\n" +
			    "        protected class R2 { \n" +
			    "            protected void m() { System.out.print(\"R2.m;\"); } \n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class M2 extends M1 {\n" +
			    "        protected class R3 {\n" +
			    "            protected void m() { System.out.print(\"R3.m;\"); }\n" +
			    "        }\n" +
			    "        protected void run() {\n" +
			    "            (new R1()).m();\n" +
			    "            (new R2()).m();\n" +
			    "            (new R3()).m();        \n" +
			    "        }\n" +
			    "    }\n" +
			    "    public Team1119its2_2() {\n" +
			    "        (new M2()).run();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1119its2_2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team1119its2_1.java",
			    "\n" +
			    "public team class Team1119its2_1 {\n" +
			    "    public team class M1 {\n" +
			    "    }\n" +
			    "    public team class M2 extends M1 {\n" +
			    "        protected class R1 {\n" +
			    "            protected void m() { System.out.print(\"R1.m;\"); }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "R1.m;R2.m;R3.m;");
    }

    // A team inherits two incomparable roles of the same name
    // 1.1.20-otjld-illegal-name-clash-1
    public void test1120_illegalNameClash1() {
        runNegativeTestMatching(
            new String[] {
		"Team1120inc1_2.java",
			    "\n" +
			    "public team class Team1120inc1_2 extends Team1120inc1_1 {\n" +
			    "    public team class T0 {\n" +
			    "        public class R0 {}\n" +
			    "        public class R1 extends R0 {}\n" +
			    "    }\n" +
			    "    public team class T1 extends T0 {\n" +
			    "        public class R1 {}\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team1120inc1_1.java",
			    "\n" +
			    "public team class Team1120inc1_1 {\n" +
			    "    public team class T0 {\n" +
			    "        public class R1 {}\n" +
			    "    }\n" +
			    "    public team class T1 extends T0 {\n" +
			    "        public class R0 {}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1.5(d)");
    }

    // using tsuper in a setting of diamond inheritance
    // 1.1.21-otjld-diamond-inheritance-1
    public void test1121_diamondInheritance1() {
       
       runConformTest(
            new String[] {
		"Team1121di1_2.java",
			    "\n" +
			    "public team class Team1121di1_2 extends Team1121di1_1 {\n" +
			    "    public class R2 {\n" +
			    "        public void test() {\n" +
			    "            tsuper.test();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main (String[] args) {\n" +
			    "        final Team1121di1_2 t = new Team1121di1_2();\n" +
			    "        R2<@t> r2 = t.new R2();\n" +
			    "        r2.test();    \n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team1121di1_1.java",
			    "\n" +
			    "public team class Team1121di1_1 {\n" +
			    "    protected class R1 {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected class R2 extends R1 {}\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // using tsuper in a setting of diamond inheritance - indirectly, invible
    // 1.1.21-otjld-diamond-inheritance-1f
    public void test1121_diamondInheritance1f() {
        runNegativeTest(
            new String[] {
		"Team1121di1f_2.java",
			    "\n" +
			    "public team class Team1121di1f_2 extends Team1121di1f_1 {\n" +
			    "	 @Override\n" +
			    "    public class R2 {\n" +
			    "	 	 @Override\n" +
			    "        public void test() {\n" +
			    "            tsuper.test();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1121di1f_1.java",
			    "\n" +
			    "public team class Team1121di1f_1 {\n" +
			    "    protected class R1 {\n" +
			    "        void test() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected class R2 extends R1 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team1121di1f_2.java (at line 7)\n" + 
    		"	tsuper.test();\n" + 
    		"	       ^^^^\n" + 
    		"Indirect tsuper method inherited from Team1121di1f_1.R1 is not visible (OTJLD 1.2.1(e)).\n" + 
    		"----------\n");
    }

    // using tsuper in a setting of diamond inheritance - role R1 reused from byte code
    // 1.1.21-otjld-diamond-inheritance-2
    public void test1121_diamondInheritance2() {
       
       runConformTest(
            new String[] {
		"Team1121di2_2.java",
			    "\n" +
			    "public team class Team1121di2_2 extends Team1121di2_1 {\n" +
			    "    public class R2 {\n" +
			    "        public void test() {\n" +
			    "            tsuper.test();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main (String[] args) {\n" +
			    "        final Team1121di2_2 t = new Team1121di2_2();\n" +
			    "        R2<@t> r2 = t.new R2();\n" +
			    "        r2.test();    \n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team1121di2_1.java",
			    "\n" +
			    "public team class Team1121di2_1 {\n" +
			    "    protected class R1 {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected class R2 extends R1 {}\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // callin arg anchored to "base", problem(s) observed in ordertesting.OrderCustomerSC
    // 1.1.22-otjld-layered-teams-1
    public void test1122_layeredTeams1() {
       
       runConformTest(
            new String[] {
		"Team1122lt1_3.java",
			    "\n" +
			    "public team class Team1122lt1_3 {\n" +
			    "    protected team class TI playedBy Team1122lt1_2 {\n" +
			    "        protected class R1 playedBy R0<@base> {\n" +
			    "            // base.R0 getOther1() -> base.R0 getOther();\n" +
			    "            abstract R0<@TI.base> getOther1(R0<@TI.base> o);\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            getOther1 -> getOther;\n" +
			    "            void dummy2(R0<@TI.base> thut) {\n" +
			    "                System.out.print(\"!\");\n" +
			    "            }\n" +
			    "            dummy2 <- after dummy;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team1122lt1_3() {\n" +
			    "        new TI(new Team1122lt1_2()).activate();\n" +
			    "        this.activate();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1122lt1_3();\n" +
			    "        new T1122lt1().test(null);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T1122lt1.java",
			    "\n" +
			    "public class T1122lt1 {\n" +
			    "    T1122lt1 getOther (T1122lt1 o) {\n" +
			    "        return this;\n" +
			    "    }\n" +
			    "    void test(T1122lt1 that) {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1122lt1_1.java",
			    "\n" +
			    "public team class Team1122lt1_1 {\n" +
			    "    public class R0 playedBy T1122lt1 {\n" +
			    "        R0 getOther(R0 r) -> T1122lt1 getOther(T1122lt1 o);\n" +
			    "        void dummy(R0 thot) {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        dummy <- after test;\n" +
			    "    }\n" +
			    "    Team1122lt1_1() {\n" +
			    "        this.activate();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1122lt1_2.java",
			    "\n" +
			    "public team class Team1122lt1_2 extends Team1122lt1_1 {}\n" +
			    "    \n"
            },
            "OK!");
    }

    // a role references a role which the lower team inherits from its super
    // 1.1.22-otjld-layered-teams-2
    public void test1122_layeredTeams2() {
       
       runConformTest(
            new String[] {
		"Team1122lt2_3.java",
			    "\n" +
			    "public team class Team1122lt2_3 {\n" +
			    "    protected class R2 playedBy Team1122lt2_2 {\n" +
			    "        @SuppressWarnings(\"decapsulation\") // as per result.test()!\n" +
			    "        String getMsg() -> R<@base> getR()\n" +
			    "            with { result <- result.test() }\n" +
			    "    }\n" +
			    "    Team1122lt2_3() {\n" +
			    "        R2 r = new R2(new Team1122lt2_2());\n" +
			    "        System.out.print(r.getMsg());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1122lt2_3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1122lt2_1.java",
			    "\n" +
			    "public team class Team1122lt2_1 {\n" +
			    "    public class R {\n" +
			    "        String test() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1122lt2_2.java",
			    "\n" +
			    "public team class Team1122lt2_2 extends Team1122lt2_1 {\n" +
			    "    public R getR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role references a role which the lower team inherits from its super - signature defined in super team
    // 1.1.22-otjld-layered-teams-3
    public void test1122_layeredTeams3() {
       
       runConformTest(
            new String[] {
		"Team1122lt3_3.java",
			    "\n" +
			    "public team class Team1122lt3_3 {\n" +
			    "    protected class R2 playedBy Team1122lt3_2 {\n" +
			    "        @SuppressWarnings(\"decapsulation\") // as per result.test()!\n" +
			    "        String getMsg() -> R<@base> getR()\n" +
			    "            with { result <- result.test() }\n" +
			    "    }\n" +
			    "    Team1122lt3_3() {\n" +
			    "        R2 r = new R2(new Team1122lt3_2());\n" +
			    "        System.out.print(r.getMsg());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1122lt3_3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1122lt3_1.java",
			    "\n" +
			    "public team class Team1122lt3_1 {\n" +
			    "    public class R {\n" +
			    "        String test() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public R getR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1122lt3_2.java",
			    "\n" +
			    "public team class Team1122lt3_2 extends Team1122lt3_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // callin requires lifting of non-public role to role-of-role
    // 1.1.22-otjld-layered-teams-4
    public void test1122_layeredTeams4() {
       
       runConformTest(
            new String[] {
		"Team1122lt4_2.java",
			    "\n" +
			    "public team class Team1122lt4_2 {\n" +
			    "    protected team class Mid playedBy Team1122lt4_1 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "	protected class RInner playedBy R<@base> {\n" +
			    "	    protected void test() { System.out.print(\"OK\"); }\n" +
			    "	}\n" +
			    "	void testMid(RInner r) {\n" +
			    "	    r.test();\n" +
			    "	}\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "	void testMid(RInner r) <- after void test(R<@base> r);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "	Team1122lt4_2 t = new Team1122lt4_2();\n" +
			    "	t.activate();\n" +
			    "	new Team1122lt4_1().run();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1122lt4_1.java",
			    "\n" +
			    "public team class Team1122lt4_1 {\n" +
			    "    protected class R {\n" +
			    "    }\n" +
			    "    protected void test(R r) {\n" +
			    "    }\n" +
			    "    public void run() {\n" +
			    "	R r = new R();\n" +
			    "	test(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // callin requires lifting of non-public role to role-of-role - declared lifting at base
    // 1.1.22-otjld-layered-teams-5
    public void test1122_layeredTeams5() {
       
       runConformTest(
            new String[] {
		"l2/Team1122lt5_3.java",
			    "\n" +
			    "package l2;\n" +
			    "import base l1.Team1122lt5_1;\n" +
			    "import base l1.Team1122lt5_2;\n" +
			    "public team class Team1122lt5_3 {\n" +
			    "    protected team class Unbound {\n" +
			    "    }\n" +
			    "    protected team class Mid0 extends Unbound playedBy Team1122lt5_1 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "	protected class RInner playedBy R<@base> {\n" +
			    "	    protected void test() { System.out.print(\"OK\"); }\n" +
			    "	}\n" +
			    "	void testMid(RInner r) {}\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "	void testMid(RInner r) <- before void test(R<@base> r);\n" +
			    "    }\n" +
			    "    protected team class Mid1 extends Mid0 playedBy Team1122lt5_2 {\n" +
			    "	void testMid(RInner r) {\n" +
			    "	    r.test();\n" +
			    "	}\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "	Team1122lt5_3 t = new Team1122lt5_3();\n" +
			    "	t.activate();\n" +
			    "	new l1.Team1122lt5_2().run();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"l0/T1122lt5.java",
			    "\n" +
			    "package l0;\n" +
			    "public class T1122lt5 {}\n" +
			    "    \n",
		"l1/Team1122lt5_1.java",
			    "\n" +
			    "package l1;\n" +
			    "public team class Team1122lt5_1 {\n" +
			    "    protected class R {\n" +
			    "    }\n" +
			    "    protected void test(R r) {\n" +
			    "	System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"l1/Team1122lt5_2.java",
			    "\n" +
			    "package l1;\n" +
			    "import l0.T1122lt5;\n" +
			    "public team class Team1122lt5_2 extends Team1122lt5_1 {\n" +
			    "    @SuppressWarnings(\"bindingconventions\")\n" +
			    "    protected class R playedBy T1122lt5 {}\n" +
			    "    protected void test(T1122lt5 as R r) {\n" +
			    "	super.test(r);\n" +
			    "    }\n" +
			    "    public void run() {\n" +
			    "	T1122lt5 b = new T1122lt5();\n" +
			    "	test(b);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // callin requires lifting of non-public role to role-of-role -  base method spec requires role strengthening for parameter
    // 1.1.22-otjld-layered-teams-6
    public void test1122_layeredTeams6() {
       
       runConformTest(
            new String[] {
		"Team1122lt6_3.java",
			    "\n" +
			    "public team class Team1122lt6_3 {\n" +
			    "    protected team class Mid playedBy Team1122lt6_2 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "	protected class RInner playedBy R<@base> {\n" +
			    "	    protected void test() { System.out.print(\"OK\"); }\n" +
			    "	}\n" +
			    "	void testMid(RInner r) {\n" +
			    "	    r.test();\n" +
			    "	}\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "	void testMid(RInner r) <- before void test(R<@base> r);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "	Team1122lt6_3 t = new Team1122lt6_3();\n" +
			    "	t.activate();\n" +
			    "	new Team1122lt6_2().run(new T1122lt6());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T1122lt6.java",
			    "\n" +
			    "public class T1122lt6 {}\n" +
			    "    \n",
		"Team1122lt6_1.java",
			    "\n" +
			    "public team class Team1122lt6_1 {\n" +
			    "    protected class R {\n" +
			    "    }\n" +
			    "    protected void test(R r) {\n" +
			    "	System.out.print(\"!\");\n" +
			    "    }\n" +
			    "    protected void test(T1122lt6 b) {\n" +
			    "	System.out.print(\"??\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1122lt6_2.java",
			    "\n" +
			    "public team class Team1122lt6_2 extends Team1122lt6_1 {\n" +
			    "    protected class R playedBy T1122lt6 {}\n" +
			    "    public void run(T1122lt6 as R r) {\n" +
			    "	test(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // callin requires lifting of non-public role to role-of-role - matching the base method spec requires lowering
    // 1.1.22-otjld-layered-teams-7
    public void test1122_layeredTeams7() {
        runNegativeTestMatching(
            new String[] {
		"Team1122lt7_2.java",
			    "\n" +
			    "public team class Team1122lt7_2 {\n" +
			    "    protected team class Mid playedBy Team1122lt7_1 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "		 protected class RInner playedBy R<@base> {\n" +
			    "	    	 protected void test() { System.out.print(\"OK\"); }\n" +
			    "		 }\n" +
			    "		 void testMid(RInner r) {\n" +
			    "	    	 r.test();\n" +
			    "		 }\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" + // decapsulating R
			    "		 void testMid(RInner r) <- before void test(R<@base> r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T1122lt7.java",
			    "\n" +
			    "public class T1122lt7 {}\n" +
			    "    \n",
		"Team1122lt7_1.java",
			    "\n" +
			    "public team class Team1122lt7_1 {\n" +
			    "    protected class R playedBy T1122lt7 {\n" +
			    "    }\n" +
			    "    protected void test(T1122lt7 b) {\n" +
			    "	System.out.print(\"??\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team1122lt7_2.java (at line 12)\n" + 
    		"	void testMid(RInner r) <- before void test(R<@base> r);\n" + 
    		"	                                           ^\n" + 
    		"Method specifier finds no direct match, argument of closest match is T1122lt7 instead of R<@base> (OTJLD 4.1(c)).\n" + 
    		"----------\n");
    }
    
    // callout to private method of lower role caused "Cannot externalize non-public role R1 (OTJLD 1.2.2(a))"
    // plus 2 follow-up errors.
    public void test1122_layeredTeams8() {
    	runConformTest(
    		new String[] {
    	"T1122lt8Main.java",
    			"public class T1122lt8Main {\n" +
    			"	public static void main(String... args) {\n" +
    			"		Team1122lt8_1 t1 = new Team1122lt8_1();\n" +
    			"		new Team1122lt8_2(t1).activate();\n" +
    			"		t1.test();\n" +
    			"	}\n" +
    			"}\n",
    	"Team1122lt8_2.java",
    			"public team class Team1122lt8_2 {\n" +
    			"	final Team1122lt8_1 theTeam;\n" +
    			"	public Team1122lt8_2(Team1122lt8_1 other) {\n" +
    			"		theTeam = other;\n" +
    			"	}\n" +
    			"	@SuppressWarnings(\"decapsulation\")\n" +
    			"	protected class R2 playedBy R1<@theTeam> {\n" +
    			"		void k() <- after void test();\n" +
    			"		void k() -> void k();\n" +
    			"	}\n" +
    			"}\n",
    	"Team1122lt8_1.java",
    			"public team class Team1122lt8_1 {\n" +
    			"	protected class R1 {\n" +
    			"		private void test() {\n" +
    			"			System.out.print(\"O\");\n" +
    			"		}\n" +
    			"		private void k() {\n" +
    			"			System.out.print(\"K\");\n" +
    			"		}\n" +
    			"		protected void callTest() { test(); }\n" +
    			"	}\n" +
    			"	public void test() {\n" +
    			"		new R1().callTest();\n" +
    			"	}\n" +
    			"}\n"
    		}, 
    		"OK");
    }

    // Bug 400404 - [compiler] No enclosing instance of the type TOuter.T1 is accessible in scope - layered team and inheritance
    public void test1122_layerdTeams9() {
       runConformTest(
            new String[] {
		"Team1122lt9_2.java",
			    "\n" +
			    "public team class Team1122lt9_2 {\n" +
			    "  public team class T1 {\n" + 
			    "    protected final Team1122lt9_1 thatTeam = new Team1122lt9_1();\n" + 
			    "    protected class R playedBy R1<@thatTeam> {" +
			    "      @SuppressWarnings(\"basecall\")\n" +
			    "      callin String fixedToString() {\n" +
			    "        return \"OK\";\n" +
			    "      }\n" +
			    "    }\n" + 
			    "  }\n" +
			    "  public team class T2 extends T1 {\n" +
			    "    @Override protected class R {\n" + 
			    "      String fixedToString() <- replace String toString();\n" +
			    "    }\n" +
			    "  }\n" +
			    "  void test() {\n" +
			    "    T2 t = new T2();\n" +
			    "    t.activate();\n" +
			    "    System.out.print(t.thatTeam.getR1(\"NOTOK\"));\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new Team1122lt9_2().test();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"Team1122lt9_1.java",
			    "\n" +
			    "public team class Team1122lt9_1 {\n" +
			    "  public class R1 {\n" +
			    "    String val;\n" +
			    "    public R1(String v) { this.val = v; }\n" +
			    "    @Override\n" +
			    "    public String toString() { return val; }\n" +
			    "  }\n" +
			    "  public R1 getR1(String v) { return new R1(v); }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // a nested team is played by a regular class, its (inner) role by an inner of the base - (we had a problem with splitting creation methods in conjunction with base class decapsulation )
    // 1.1.23-otjld-team-layered-on-regular-nested-1
    public void test1123_teamLayeredOnRegularNested1() {
       
       runConformTest(
            new String[] {
		"Team1123tlorn1.java",
			    "\n" +
			    "public team class Team1123tlorn1 {\n" +
			    "    protected team class Mid playedBy T1123tlorn1 {\n" +
			    "        Mid(T1123tlorn1 aBase) {\n" +
			    "            this.activate();\n" +
			    "        }\n" +
			    "        void go() {\n" +
			    "            System.out.print(\"Mid.setup()\");\n" +
			    "        }\n" +
			    "        go <- before testInner;\n" +
			    "\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        protected class R playedBy T1123tlorn1.Inner {\n" +
			    "            callin void test(String m) {\n" +
			    "                base.test(null);\n" +
			    "                System.out.print(m);\n" +
			    "            }\n" +
			    "            void test(String m) <- replace void test() \n" +
			    "                with {m <- \"R.test()\" };\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        org.objectteams.Team t = new Team1123tlorn1();\n" +
			    "        t.activate();\n" +
			    "        new T1123tlorn1().testInner(\"-OK-\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T1123tlorn1.java",
			    "\n" +
			    "public class T1123tlorn1 {\n" +
			    "    void testInner (String msg) {\n" +
			    "        new Inner(msg).test();\n" +
			    "    }\n" +
			    "    private class Inner {\n" +
			    "        String msg;\n" +
			    "        Inner(String msg) {\n" +
			    "            this.msg = msg;\n" +
			    "        }\n" +
			    "        void test() {\n" +
			    "            System.out.print(msg);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "Mid.setup()-OK-R.test()");
    }

    // a nested team is played by a regular class, its (inner) role by an inner of the base, illegal base.Inner syntax
    // 1.1.23-otjld-team-layered-on-regular-nested-2
    public void test1123_teamLayeredOnRegularNested2() {
        runNegativeTestMatching(
            new String[] {
		"Team1123tlorn2.java",
			    "\n" +
			    "public team class Team1123tlorn2 {\n" +
			    "    protected team class Mid playedBy T1123tlorn2 {\n" +
			    "        callin void setup(String msg) {\n" +
			    "            System.out.print(\"Mid.setup()\");\n" +
			    "            this.activate();\n" +
			    "            base.setup(msg);\n" +
			    "        }\n" +
			    "        setup <- replace getInner;\n" +
			    "\n" +
			    "        protected class R playedBy base.Inner {\n" +
			    "            callin void test(String m) {\n" +
			    "                base.test(null);\n" +
			    "                System.out.print(m);\n" +
			    "            }\n" +
			    "            void test(String m) <- replace void test() \n" +
			    "                with {m <- \"R.test()\" };\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T1123tlorn2.java",
			    "\n" +
			    "public class T1123tlorn2 {\n" +
			    "    Inner getInner (String msg) {\n" +
			    "        return new Inner(msg);\n" +
			    "    }\n" +
			    "    protected class Inner {\n" +
			    "        String msg;\n" +
			    "        Inner(String msg) {\n" +
			    "            this.msg = msg;\n" +
			    "        }\n" +
			    "        void test() {\n" +
			    "            System.out.print(msg);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "2.6(a)");
    }

    // a nested team is played by a regular class, its (inner) role by an inner of the base - base constructor needs explicit enclosing instance
    // 1.1.23-otjld-team-layered-on-regular-nested-3
    public void test1123_teamLayeredOnRegularNested3() {
       
       runConformTest(
            new String[] {
		"Team1123tlorn3.java",
			    "\n" +
			    "public team class Team1123tlorn3 {\n" +
			    "    protected team class Mid playedBy T1123tlorn3 {\n" +
			    "        protected void midTest() {\n" +
			    "            R r= new R(this, \"OK\");\n" +
			    "            r.test();\n" +
			    "        }\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        protected class R playedBy T1123tlorn3.Inner {\n" +
			    "            protected R(Mid encl, String msg) {\n" +
			    "                encl.base(msg);\n" +
			    "            }\n" +
			    "            public void test() -> void test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void doTest() {\n" +
			    "        Mid m= new Mid(new T1123tlorn3());\n" +
			    "        m.midTest();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team1123tlorn3 t = new Team1123tlorn3();\n" +
			    "        t.doTest();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T1123tlorn3.java",
			    "\n" +
			    "public class T1123tlorn3 {\n" +
			    "    void testInner (String msg) {\n" +
			    "        new Inner(msg).test();\n" +
			    "    }\n" +
			    "    private class Inner {\n" +
			    "        String msg;\n" +
			    "        Inner(String msg) {\n" +
			    "            this.msg = msg;\n" +
			    "        }\n" +
			    "        void test() {\n" +
			    "            System.out.print(msg);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a reflective method is used on a nested team
    // 1.1.24-otjld-generated-method-of-nested-team-1
    public void test1124_generatedMethodOfNestedTeam1() {
       
       runConformTest(
            new String[] {
		"p2/T1124gmont1Main.java",
			    "\n" +
			    "package p2;\n" +
			    "import p1.Team1124gmont1;\n" +
			    "public class T1124gmont1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1124gmont1 t = new Team1124gmont1();\n" +
			    "        Mid<@t> mid = t.new Mid();\n" +
			    "        if (!mid.isExecutingCallin())\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        else\n" +
			    "            System.out.print(\"NOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team1124gmont1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team1124gmont1 {\n" +
			    "    public team class Mid {\n" +
			    "        protected class R {}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a creator method is invoked on an externalized nested team
    // 1.1.24-otjld-generated-method-of-nested-team-2
    public void test1124_generatedMethodOfNestedTeam2() {
       
       runConformTest(
            new String[] {
		"p2/T1124gmont2Main.java",
			    "\n" +
			    "package p2;\n" +
			    "import p1.Team1124gmont2;\n" +
			    "public class T1124gmont2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1124gmont2 t = new Team1124gmont2();\n" +
			    "        t.new Mid();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team1124gmont2.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team1124gmont2 {\n" +
			    "    public team class Mid {\n" +
			    "        public Mid() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a cast method is used for a nested role
    // 1.1.24-otjld-generated-method-of-nested-team-3
    public void test1124_generatedMethodOfNestedTeam3() {
       
       runConformTest(
            new String[] {
		"Team1124gmont3.java",
			    "\n" +
			    "public team class Team1124gmont3 {\n" +
			    "    final Mid m = new Mid();\n" +
			    "    public team class Mid {\n" +
			    "        public class Inner {\n" +
			    "            public void test() {\n" +
			    "                System.out.print(\"OK\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public Object getInner() { return new Inner(); }\n" +
			    "    }\n" +
			    "    void test(Object o) {\n" +
			    "        Inner<@m> i = (Inner<@m>)o;\n" +
			    "        i.test();\n" +
			    "    }\n" +
			    "    public Team1124gmont3() {\n" +
			    "        test(m.getInner());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1124gmont3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a nested role accesses its outer-outer team
    // 1.1.25-otjld-outer-access-in-nested-team-1
    public void test1125_outerAccessInNestedTeam1() {
       
       runConformTest(
            new String[] {
		"Team1125oaint1.java",
			    "\n" +
			    "public team class Team1125oaint1 {\n" +
			    "    String val = \"OK\";\n" +
			    "    public team class Mid {\n" +
			    "        public class R {\n" +
			    "            public void test() {\n" +
			    "                System.out.print(Team1125oaint1.this.val);\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1125oaint1 t = new Team1125oaint1();\n" +
			    "        final Mid<@t> m = t.new Mid();\n" +
			    "        R<@m> r = m.new R();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a nested role accesses its outer-outer team - team inheritance
    // 1.1.25-otjld-outer-access-in-nested-team-2
    public void test1125_outerAccessInNestedTeam2() {
       
       runConformTest(
            new String[] {
		"Team1125oaint2_2.java",
			    "\n" +
			    "public team class Team1125oaint2_2 extends Team1125oaint2_1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1125oaint2_1 t = new Team1125oaint2_2();\n" +
			    "        final Mid<@t> m = t.new Mid();\n" +
			    "        R<@m> r = new R<@m>();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1125oaint2_1.java",
			    "\n" +
			    "public team class Team1125oaint2_1 {\n" +
			    "    String val = \"OK\";\n" +
			    "    public team class Mid {\n" +
			    "        public class R {\n" +
			    "            public void test() {\n" +
			    "                System.out.print(Team1125oaint2_1.this.val);\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a nested role accesses its outer-outer team - team inheritance
    // 1.1.25-otjld-outer-access-in-nested-team-3
    public void test1125_outerAccessInNestedTeam3() {
       
       runConformTest(
            new String[] {
		"Team1125oaint3_2.java",
			    "\n" +
			    "public team class Team1125oaint3_2 extends Team1125oaint3_1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1125oaint3_1 t = new Team1125oaint3_2();\n" +
			    "        final Mid<@t> m = new Mid<@t>();\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1125oaint3_1.java",
			    "\n" +
			    "public team class Team1125oaint3_1 {\n" +
			    "    String val = \"OK\";\n" +
			    "    public team class Mid {\n" +
			    "        public class R {\n" +
			    "            static protected void test() {\n" +
			    "                System.out.print(Team1125oaint3_1.this.val);\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public void test() {\n" +
			    "            R.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a nested role accesses a static method of the intermediate enclosing team/role
    // 1.1.25-otjld-outer-access-in-nested-team-4
    public void test1125_outerAccessInNestedTeam4() {
       
       runConformTest(
            new String[] {
		"Team1125oaint4.java",
			    "\n" +
			    "public team class Team1125oaint4 {\n" +
			    "    protected team class Mid {\n" +
			    "        private static void print() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        protected class Inner {\n" +
			    "            protected void test() {\n" +
			    "                Mid.print();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            new Inner().test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new Mid().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1125oaint4().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // nested teams adapt a lower team - with method decapsulation
    // 1.1.26-otjld-nesting-and-layering-1
    public void test1126_nestingAndLayering1() {
       
       runConformTest(
            new String[] {
		"Team1126nal1_2.java",
			    "\n" +
			    "public team class Team1126nal1_2 {\n" +
			    "    protected team class Mid1 playedBy Team1126nal1_1 {\n" +
			    "        protected class RU playedBy R<@base> {\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            public void testRU() -> void test();\n" +
			    "        }\n" +
			    "        RU getR() -> R<@base> getR();\n" +
			    "        protected void testMid1() {\n" +
			    "            getR().testRU();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team1126nal1_2() {\n" +
			    "        Mid1 m = new Mid1(new Team1126nal1_1());\n" +
			    "        m.testMid1();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1126nal1_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal1_1.java",
			    "\n" +
			    "public team class Team1126nal1_1 {\n" +
			    "    protected R getR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public class R {\n" +
			    "        private void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // nested teams with implicit inheritance adapt a hierarchy of lower teams - with method decapsulation
    // 1.1.26-otjld-nesting-and-layering-2
    public void test1126_nestingAndLayering2() {
       
       runConformTest(
            new String[] {
		"Team1126nal2_3.java",
			    "\n" +
			    "public team class Team1126nal2_3 {\n" +
			    "    protected team class Mid1 playedBy Team1126nal2_1 {\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 playedBy Team1126nal2_2 {\n" +
			    "        public class RU playedBy R<@base> {\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            void testRU() -> void test();\n" +
			    "        }\n" +
			    "        RU getR() -> R<@base> getR();\n" +
			    "        public void testMid2() {\n" +
			    "            getR().testRU();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team1126nal2_3() {\n" +
			    "        Mid2 m = new Mid2(new Team1126nal2_2());\n" +
			    "        m.testMid2();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1126nal2_3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal2_1.java",
			    "\n" +
			    "public team class Team1126nal2_1 {\n" +
			    "    public R getR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal2_2.java",
			    "\n" +
			    "public team class Team1126nal2_2 extends Team1126nal2_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // nested teams with implicit inheritance adapt a hierarchy of lower teams - with private method decapsulation
    // 1.1.26-otjld-nesting-and-layering-3p
    public void test1126_nestingAndLayering3p() {
       
       runConformTest(
            new String[] {
		"Team1126nal3p_3.java",
			    "\n" +
			    "public team class Team1126nal3p_3 {\n" +
			    "    protected team class Mid1 playedBy Team1126nal3p_1 {\n" +
			    "        protected class RU playedBy R<@base> {\n" +
			    "        }\n" +
			    "        RU getR() -> R<@base> getR();\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 playedBy Team1126nal3p_2 {\n" +
			    "        protected class RU  {\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            protected void testRU(char c) -> void test(char c);\n" +
			    "        }\n" +
			    "        protected void testMid2() {\n" +
			    "            getR().testRU('K');\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team1126nal3p_3() {\n" +
			    "        Mid2 m = new Mid2(new Team1126nal3p_2());\n" +
			    "        m.testMid2();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1126nal3p_3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal3p_1.java",
			    "\n" +
			    "public team class Team1126nal3p_1 {\n" +
			    "    protected R getR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public class R {\n" +
			    "        private void test(char c) {\n" +
			    "            System.out.print(\"O\"+c);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal3p_2.java",
			    "\n" +
			    "public team class Team1126nal3p_2 extends Team1126nal3p_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // nested teams with implicit inheritance adapt a hierarchy of lower teams - with private method decapsulation - double compile re-using binary role
    // 1.1.26-otjld-nesting-and-layering-3p2
    public void test1126_nestingAndLayering3p2() {
       
       runConformTest(
            new String[] {
		"Team1126nal3p2_3.java",
			    "\n" +
			    "public team class Team1126nal3p2_3 {\n" +
			    "    protected team class Mid1 playedBy Team1126nal3p2_1 {\n" +
			    "        protected class RU playedBy R<@base> {\n" +
			    "        }\n" +
			    "        RU getR() -> R<@base> getR();\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 playedBy Team1126nal3p2_2 {\n" +
			    "        protected class RU  {\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            protected void testRU(char c) -> void test(char c);\n" +
			    "        }\n" +
			    "        protected void testMid2() {\n" +
			    "            getR().testRU('K');\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team1126nal3p2_3() {\n" +
			    "        Mid2 m = new Mid2(new Team1126nal3p2_2());\n" +
			    "        m.testMid2();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1126nal3p2_3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal3p2_1.java",
			    "\n" +
			    "public team class Team1126nal3p2_1 {\n" +
			    "    protected R getR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public class R {\n" +
			    "        private void test(char c) {\n" +
			    "            System.out.print(\"O\"+c);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal3p2_2.java",
			    "\n" +
			    "public team class Team1126nal3p2_2 extends Team1126nal3p2_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // nested teams with implicit inheritance adapt a hierarchy of lower teams - with private method decapsulation
    // three step compilation: access lower team from binary
    public void test1126_nestingAndLayering3p3() {
       compileOrder = new String[][]{{"Team1126nal3p_1.java"}, {"Team1126nal3p_2.java"}, {"Team1126nal3p_3.java"}};
       runConformTest(
            new String[] {
		"Team1126nal3p_3.java",
			    "\n" +
			    "public team class Team1126nal3p_3 {\n" +
			    "    protected team class Mid1 playedBy Team1126nal3p_1 {\n" +
			    "        protected class RU playedBy R<@base> {\n" +
			    "        }\n" +
			    "        RU getR() -> R<@base> getR();\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 playedBy Team1126nal3p_2 {\n" +
			    "        protected class RU  {\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            protected void testRU(char c) -> void test(char c);\n" +
			    "        }\n" +
			    "        protected void testMid2() {\n" +
			    "            getR().testRU('K');\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team1126nal3p_3() {\n" +
			    "        Mid2 m = new Mid2(new Team1126nal3p_2());\n" +
			    "        m.testMid2();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1126nal3p_3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal3p_1.java",
			    "\n" +
			    "public team class Team1126nal3p_1 {\n" +
			    "    protected R getR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public class R {\n" +
			    "        private void test(char c) {\n" +
			    "            System.out.print(\"O\"+c);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal3p_2.java",
			    "\n" +
			    "public team class Team1126nal3p_2 extends Team1126nal3p_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // nested teams with implicit inheritance adapt a hierarchy of lower teams - method is protected
    // 1.1.26-otjld-nesting-and-layering-3
    public void test1126_nestingAndLayering3() {
       
       runConformTest(
            new String[] {
		"p2/Team1126nal3_3.java",
			    "\n" +
			    "package p2;\n" +
			    "import base p1.Team1126nal3_1;\n" +
			    "import base p1.Team1126nal3_2;\n" +
			    "\n" +
			    "public team class Team1126nal3_3 {\n" +
			    "    protected team class Mid1 playedBy Team1126nal3_1 {\n" +
			    "        protected class RU playedBy R<@base> {\n" +
			    "        }\n" +
			    "        @SuppressWarnings(\"decapsulation\") // getR() is in a different team and package but not public\n" +
			    "        RU getR() -> R<@base> getR();\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 playedBy Team1126nal3_2 {\n" +
			    "        protected class RU  {\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            void testRU() -> void test();\n" +
			    "        }\n" +
			    "        protected void testMid2() {\n" +
			    "            getR().testRU();\n" +
			    "        }\n" +
			    "        protected Mid2() {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team1126nal3_3() {\n" +
			    "        Mid2 m = new Mid2();\n" +
			    "        m.testMid2();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1126nal3_3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team1126nal3_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team1126nal3_1 {\n" +
			    "    protected R getR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team1126nal3_2.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team1126nal3_2 extends Team1126nal3_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // nested teams with implicit inheritance adapt a hierarchy of lower teams - method is protected - incremenatl (i.e., double) build
    // 1.1.26-otjld-nesting-and-layering-3i
    public void test1126_nestingAndLayering3i() {
       
       runConformTest(
            new String[] {
		"p2/Team1126nal3i_3.java",
			    "\n" +
			    "package p2;\n" +
			    "import base p1.Team1126nal3i_1;\n" +
			    "import base p1.Team1126nal3i_2;\n" +
			    "\n" +
			    "public team class Team1126nal3i_3 {\n" +
			    "    protected team class Mid1 playedBy Team1126nal3i_1 {\n" +
			    "        protected class RU playedBy R<@base> {\n" +
			    "        }\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        RU getR() -> R<@base> getR();\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 playedBy Team1126nal3i_2 {\n" +
			    "        protected class RU  {\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            void testRU() -> void test();\n" +
			    "        }\n" +
			    "        protected void testMid2() {\n" +
			    "            getR().testRU();\n" +
			    "        }\n" +
			    "        protected Mid2() {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team1126nal3i_3() {\n" +
			    "        Mid2 m = new Mid2();\n" +
			    "        m.testMid2();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1126nal3i_3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team1126nal3i_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team1126nal3i_1 {\n" +
			    "    protected R getR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team1126nal3i_2.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team1126nal3i_2 extends Team1126nal3i_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // nested teams with implicit inheritance adapt a hierarchy of lower teams
    // 1.1.26-otjld-nesting-and-layering-4
    public void test1126_nestingAndLayering4() {
       
       runConformTest(
            new String[] {
		"Team1126nal4_3.java",
			    "\n" +
			    "public team class Team1126nal4_3 {\n" +
			    "    @SuppressWarnings(\"roletypesyntax\")\n" +
			    "    protected team class Mid1 playedBy Team1126nal4_1 {\n" +
			    "        protected class RU playedBy base.R { }\n" +
			    "        void receiveR(RU ru) { System.out.print(\"Mid1.\"); }\n" +
			    "        void receiveR(RU ru) <- after void receiveR(base.R r);\n" +
			    "        Mid1(Team1126nal4_1 b) {\n" +
			    "            b.test('o');\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 playedBy Team1126nal4_2 {\n" +
			    "        void receiveR(RU ru) { System.out.print(\"Mid2.\"); }\n" +
			    "    }\n" +
			    "    Team1126nal4_3() {\n" +
			    "        activate();\n" +
			    "        new Mid2(new Team1126nal4_2());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1126nal4_3();\n" +
			    "        Team1126nal4_1 t = new Team1126nal4_1();\n" +
			    "        t.test('p');\n" +
			    "        t.test('q');\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal4_1.java",
			    "\n" +
			    "public team class Team1126nal4_1 {\n" +
			    "    public class R {\n" +
			    "        char c;\n" +
			    "        protected R(char c) { this.c = c; }\n" +
			    "        protected void print() { System.out.print(\"R\"+c+\".\"); }\n" +
			    "    }\n" +
			    "    public void receiveR(R r) {\n" +
			    "        r.print();\n" +
			    "    }\n" +
			    "    void test(char c) {\n" +
			    "        receiveR(new R(c));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal4_2.java",
			    "\n" +
			    "public team class Team1126nal4_2 extends Team1126nal4_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "Ro.Mid2.Rp.Ro.Mid1.Mid1.Rq.Mid1.");
    }

    // nested teams with implicit inheritance adapt a hierarchy of lower team -- callout to role field
    // 1.1.26-otjld_nesting-and-layering-4f
    public void _nesting_test1126_andLayering4f() {
       
       runConformTest(
            new String[] {
		"Team1126nal4f_3.java",
			    "\n" +
			    "public team class Team1126nal4f_3 {\n" +
			    "    protected team class Mid1 playedBy Team1126nal4f_1 {\n" +
			    "        protected class RU playedBy R<@base> {\n" +
			    "            int getI() -> get int i;\n" +
			    "        }\n" +
			    "        void receiveR(RU ru) { System.out.print(\"Mid1(RU\"+ru.getI()+\").\"); }\n" +
			    "        void receiveR(RU ru) <- after void receiveR(R<@base> r);\n" +
			    "        Mid1(Team1126nal4f_1 b) {\n" +
			    "            b.test(1);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 playedBy Team1126nal4f_2 {\n" +
			    "        void receiveR(RU ru) { System.out.print(\"Mid2(RU\"+ru.getI()+\").\"); }\n" +
			    "    }\n" +
			    "    Team1126nal4f_3() {\n" +
			    "        activate();\n" +
			    "        new Mid2(new Team1126nal4f_2());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1126nal4f_3();\n" +
			    "        new Team1126nal4f_1().test(2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal4f_1.java",
			    "\n" +
			    "public team class Team1126nal4f_1 {\n" +
			    "    public class R {\n" +
			    "        int i;\n" +
			    "        protected R(int i) { this.i = i; }\n" +
			    "        protected void print() { System.out.print(\"R\"+i+\".\"); }\n" +
			    "    }\n" +
			    "    public void receiveR(R r) {\n" +
			    "        r.print();\n" +
			    "    }\n" +
			    "    void test(int i) {\n" +
			    "        receiveR(new R(i));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal4f_2.java",
			    "\n" +
			    "public team class Team1126nal4f_2 extends Team1126nal4f_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // nested teams with implicit inheritance adapt a hierarchy of lower teams - callout to private role field
    // 1.1.26-otjld-nesting-and-layering-4pf
    public void test1126_nestingAndLayering4pf() {
       
       runConformTest(
            new String[] {
		"Team1126nal4pf_3.java",
			    "\n" +
			    "public team class Team1126nal4pf_3 {\n" +
			    "    protected team class Mid1 playedBy Team1126nal4pf_1 {\n" +
			    "        protected class RU playedBy R<@base> {\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            protected String getI() -> get String i;\n" +
			    "            // TODO(SH): check field with role type!\n" +
			    "        }\n" +
			    "        void receiveR(RU ru) { System.out.print(\"Mid1(RU\"+ru.getI()+\")!\"); }\n" +
			    "        void receiveR(RU ru) <- after void receiveR(R<@base> r);\n" +
			    "        Mid1(Team1126nal4pf_1 b) {\n" +
			    "            System.out.print(\"LMid1-\");\n" +
			    "            b.test(\"a\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class Mid2 extends Mid1 playedBy Team1126nal4pf_2 {\n" +
			    "        void receiveR(RU ru) { System.out.print(\"Mid2(RU\"+ru.getI()+\")!\"); }\n" +
			    "    }\n" +
			    "    Team1126nal4pf_3() {\n" +
			    "        activate();\n" +
			    "        new Mid2(new Team1126nal4pf_2());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1126nal4pf_3();\n" +
			    "        new Team1126nal4pf_1().test(\"b\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal4pf_1.java",
			    "\n" +
			    "public team class Team1126nal4pf_1 {\n" +
			    "    public class R {\n" +
			    "        private String i;\n" +
			    "        protected R(String i) { this.i = i; }\n" +
			    "        protected void print() { System.out.print(\"R\"+i+\".\"); }\n" +
			    "    }\n" +
			    "    public void receiveR(R r) {\n" +
			    "        r.print();\n" +
			    "    }\n" +
			    "    void test(String i) {\n" +
			    "        receiveR(new R(i));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal4pf_2.java",
			    "\n" +
			    "public team class Team1126nal4pf_2 extends Team1126nal4pf_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "LMid1-Ra.Mid2(RUa)!Rb.LMid1-Ra.Mid1(RUa)!Mid1(RUb)!");
    }

    // two-level base anchored types
    // 1.1.26-otjld-nesting-and-layering-5
    public void test1126_nestingAndLayering5() {
       
       runConformTest(
            new String[] {
		"Team1126nal5_2.java",
			    "\n" +
			    "public team class Team1126nal5_2 {\n" +
			    "    protected team class TopTeam playedBy Team1126nal5_1 {\n" +
			    "        public class TopRole playedBy BaseMid<@base> {\n" +
			    "            BaseRole<@base> r; // <= here @base refers to base-anchored type BaseMid<@base>!\n" +
			    "            TopRole(BaseMid<@TopTeam.base> m) {\n" +
			    "                r= getR();\n" +
			    "            }\n" +
			    "            BaseRole<@base> getR() -> BaseRole<@base> getR();\n" +
			    "            protected void test2() {\n" +
			    "                r.test3();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test1() {\n" +
			    "            TopRole tr= getMid();\n" +
			    "            tr.test2();\n" +
			    "        }\n" +
			    "        TopRole getMid() -> BaseMid<@base> getMid();\n" +
			    "    }\n" +
			    "    void test0() {\n" +
			    "        new TopTeam(new Team1126nal5_1()).test1();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1126nal5_2().test0();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1126nal5_1.java",
			    "\n" +
			    "public team class Team1126nal5_1 {\n" +
			    "    public team class BaseMid {\n" +
			    "        public class BaseRole {\n" +
			    "            public void test3() {\n" +
			    "                System.out.print(\"OK\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public BaseRole getR() {\n" +
			    "            return new BaseRole();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public BaseMid getMid() {\n" +
			    "        return new BaseMid();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // the ctor of a nested team assigns a final field - reported by kmeier on 06/10/21
    // 1.1.27-otjld-constructor-of-nested-team-1
    public void test1127_constructorOfNestedTeam1() {
       
       runConformTest(
            new String[] {
		"Team1127cont1.java",
			    "\n" +
			    "public team class Team1127cont1 {\n" +
			    "        public team class CA {\n" +
			    "                public class X {\n" +
			    "                        protected void test(){\n" +
			    "                            System.out.print(\"OK\");\n" +
			    "                        }\n" +
			    "                }\n" +
			    "                protected void test() {\n" +
			    "                    new X().test();\n" +
			    "                }\n" +
			    "        }\n" +
			    "        \n" +
			    "        public team class CB {\n" +
			    "                private final CA ca;\n" +
			    "                protected CB(CA ca) {\n" +
			    "                        this.ca = ca;\n" +
			    "                }\n" +
			    "                protected void test() {\n" +
			    "                    ca.test();\n" +
			    "                }\n" +
			    "        }\n" +
			    "        Team1127cont1() {\n" +
			    "            CB cb = new CB(new CA());\n" +
			    "            cb.test();\n" +
			    "        }\n" +
			    "        public static void main(String[] args) {\n" +
			    "            new Team1127cont1();\n" +
			    "        }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // the ctor of a role in a nested team is duplicated during implicit inheritance (phantom type) - reported by Robin Sedlaczek
    // 1.1.27-otjld-constructor-of-nested-team-2
    public void test1127_constructorOfNestedTeam2() {
       
       runConformTest(
            new String[] {
		"Team1127cont2_2.java",
			    "\n" +
			    "public team class Team1127cont2_2 extends Team1127cont2_1 {\n" +
			    "    public class OuterRole playedBy T1127cont2 { \n" +
			    "        String getRStr()  => String getBStr()\n" +
			    "            with { result <- tsuper.getRStr() + \"!\" }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        OuterRole or= new OuterRole(new T1127cont2());\n" +
			    "        System.out.print(or.getRStr());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1127cont2_2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T1127cont2.java",
			    "\n" +
			    "public class T1127cont2 {\n" +
			    "    String getBStr() { return \"OK\"; }\n" +
			    "}\n" +
			    "    \n",
		"Team1127cont2_1.java",
			    "\n" +
			    "@SuppressWarnings(\"unused\")\n" +
			    "public team class Team1127cont2_1 {\n" +
			    "    private int teamVal;\n" +
			    "    public class OuterRole playedBy T1127cont2 { \n" +
			    "        private int outerVal;\n" +
			    "        protected String getRStr() -> String getBStr();\n" +
			    "    }\n" +
			    "    public team class Mid { \n" +
			    "        public class InnerRole playedBy OuterRole { \n" +
			    "            private int innerVal;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // a role tries to hide another role from an outer team scope
    // 1.1.28-otjld-role-hides-outer-role-1
    public void test1128_roleHidesOuterRole1() {
        runNegativeTestMatching(
            new String[] {
		"Team1128rhor1.java",
			    "\n" +
			    "public team class Team1128rhor1 {\n" +
			    "	protected class Role {}\n" +
			    "	protected team class NestedTeam {\n" +
			    "		protected class Role {}\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "1.4(a)");
    }

    // a callin in a nested role mentions the base class of another toplevel role
    // 1.1.29-otjld-nested-callin-1
    public void test1129_nestedCallin1() {
       
       runConformTest(
            new String[] {
		"T1129nc1Main.java",
			    "\n" +
			    "import p.T1129nc1_1;\n" +
			    "import p.T1129nc1_2;\n" +
			    "public class T1129nc1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1129nc1();\n" +
			    "        new T1129nc1_2().test(new T1129nc1_1());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p/T1129nc1_1.java",
			    "\n" +
			    "package p;\n" +
			    "public class T1129nc1_1 {\n" +
			    "    public void print() { System.out.print(\"OK\"); }\n" +
			    "}\n" +
			    "    \n",
		"p/T1129nc1_2.java",
			    "\n" +
			    "package p;\n" +
			    "public class T1129nc1_2 {\n" +
			    "    public void test(T1129nc1_1 other) {\n" +
			    "        // nop\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1129nc1.java",
			    "\n" +
			    "import base p.T1129nc1_1;\n" +
			    "import base p.T1129nc1_2;\n" +
			    "public team class Team1129nc1 {\n" +
			    "    protected class R1 playedBy T1129nc1_1 {\n" +
			    "        void print() -> void print();\n" +
			    "    }\n" +
			    "    protected team class InnerTeam {\n" +
			    "        protected class R2 playedBy T1129nc1_2 {\n" +
			    "            @SuppressWarnings(\"basecall\")\n" +
			    "            callin void t(R1 other) {\n" +
			    "                other.print();\n" +
			    "            }\n" +
			    "            void t(R1 other) <- replace void test(T1129nc1_1 other);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team1129nc1() {\n" +
			    "        new InnerTeam().activate();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin in a nested role mentions the base class of another toplevel role - array type - witness for NPE in CallinImplementor
    // 1.1.29-otjld-nested-callin-2
    public void test1129_nestedCallin2() {
       
       runConformTest(
            new String[] {
		"T1129nc2Main.java",
			    "\n" +
			    "import p.T1129nc2_2;\n" +
			    "public class T1129nc2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1129nc2();\n" +
			    "        new T1129nc2_2().getOther()[0].print();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p/T1129nc2_1.java",
			    "\n" +
			    "package p;\n" +
			    "public class T1129nc2_1 {\n" +
			    "    public void print() { System.out.print(\"OK\"); }\n" +
			    "}\n" +
			    "    \n",
		"p/T1129nc2_2.java",
			    "\n" +
			    "package p;\n" +
			    "public class T1129nc2_2 {\n" +
			    "    public T1129nc2_1[] getOther() {\n" +
			    "        return null;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1129nc2.java",
			    "\n" +
			    "import base p.T1129nc2_1;\n" +
			    "import base p.T1129nc2_2;\n" +
			    "public team class Team1129nc2 {\n" +
			    "    protected class R1 playedBy T1129nc2_1 {\n" +
			    "        protected R1() {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class InnerTeam {\n" +
			    "        protected class R2 playedBy T1129nc2_2 {\n" +
			    "            @SuppressWarnings(\"basecall\")\n" +
			    "            callin R1[] getOther() {\n" +
			    "                return new R1[]{new R1()};\n" +
			    "            }\n" +
			    "            R1[] getOther() <- replace T1129nc2_1[] getOther();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team1129nc2() {\n" +
			    "        new InnerTeam().activate();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin mentions a nested role in its signature - binding without signatures
    // 1.1.29-otjld-nested-callin-3
    public void test1129_nestedCallin3() {
       
       runConformTest(
            new String[] {
		"Team1129nc3.java",
			    "\n" +
			    "public team class Team1129nc3 {\n" +
			    "  protected team class Mid playedBy T1129nc3 {\n" +
			    "    @SuppressWarnings(\"decapsulation\")\n" +
			    "    protected class InnerRole playedBy T1129nc3.InnerBase {\n" +
			    "      protected void test() -> void test();\n" +
			    "    }\n" +
			    "    callin void intercept(InnerRole ir) {\n" +
			    "      base.intercept(ir);\n" +
			    "      System.out.print(\"K\");\n" +
			    "    }\n" +
			    "    intercept <- replace joinpoint;\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new Team1129nc3().activate();\n" +
			    "    new T1129nc3().start();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"T1129nc3.java",
			    "\n" +
			    "public class T1129nc3 {\n" +
			    "  void joinpoint(InnerBase ib) {\n" +
			    "    ib.test();\n" +
			    "  }\n" +
			    "  private static class InnerBase { \n" +
			    "    void test() { System.out.print(\"O\"); }\n" +
			    "  }\n" +
			    "  void start() {\n" +
			    "    joinpoint(new InnerBase());\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // a callin mentions a nested role in its signature - binding with signatures
    // 1.1.29-otjld-nested-callin-4
    public void test1129_nestedCallin4() {
       
       runConformTest(
            new String[] {
		"Team1129nc4.java",
			    "\n" +
			    "public team class Team1129nc4 {\n" +
			    "  protected team class Mid playedBy T1129nc4 {\n" +
			    "    @SuppressWarnings(\"decapsulation\")\n" +
			    "    protected class InnerRole playedBy T1129nc4.InnerBase {\n" +
			    "      protected void test() -> void test();\n" +
			    "    }\n" +
			    "    callin void intercept(InnerRole ir) {\n" +
			    "      base.intercept(ir);\n" +
			    "      System.out.print(\"K\");\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"decapsulation\")\n" +
			    "    void intercept(InnerRole ir) <- replace void joinpoint(T1129nc4.InnerBase ib);\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new Team1129nc4().activate();\n" +
			    "    new T1129nc4().start();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"T1129nc4.java",
			    "\n" +
			    "public class T1129nc4 {\n" +
			    "  void joinpoint(InnerBase ib) {\n" +
			    "    ib.test();\n" +
			    "  }\n" +
			    "  private static class InnerBase { \n" +
			    "    void test() { System.out.print(\"O\"); }\n" +
			    "  }\n" +
			    "  void start() {\n" +
			    "    joinpoint(new InnerBase());\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // a third-level role has a callin
    // 1.1.30-otjld-deeply-nested-team-1
    public void test1130_deeplyNestedTeam1() {
       
       runConformTest(
            new String[] {
		"Team1130dnt1.java",
			    "\n" +
			    "public team class Team1130dnt1 {\n" +
			    "  protected team class Level1 playedBy T1130dnt1 {\n" +
			    "    @SuppressWarnings(\"roleinstantiation\")\n" +
			    "    public Level1(T1130dnt1 b) {\n" +
			    "      new Level2(b);\n" +
			    "    }\n" +
			    "    protected team class Level2 playedBy T1130dnt1 {\n" +
			    "      protected Level2 (T1130dnt1 b) {\n" +
			    "	this.activate();\n" +
			    "      }\n" +
			    "      Level3 innermost;\n" +
			    "      protected class Level3 playedBy T1130dnt1 {\n" +
			    "	k <- after wurgs;\n" +
			    "	void k() {\n" +
			    "	  System.out.print(\"K\");\n" +
			    "	} \n" +
			    "      }\n" +
			    "    }\n" +
			    "  }\n" +
			    "  @SuppressWarnings(\"roleinstantiation\")\n" +
			    "  Team1130dnt1(T1130dnt1 b) {\n" +
			    "    new Level1(b);\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    T1130dnt1 b = new T1130dnt1();\n" +
			    "    new Team1130dnt1(b);\n" +
			    "    b.wurgs();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"T1130dnt1.java",
			    "\n" +
			    "public class T1130dnt1 {\n" +
			    "  void wurgs() {\n" +
			    "    System.out.print(\"O\");\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // role refers to base of sibling role - witness for NPE in TeamAnchor.getStrengthenedRole() resp. AssertionError in RTB.<init>
    // 1.1.31-otjld-stacked-teams-1
    public void test1131_stackedTeams1() {
       
       runConformTest(
            new String[] {
		"Team1131lt1_2.java",
			    "\n" +
			    "public team class Team1131lt1_2 {\n" +
			    "  protected class R2 playedBy Team1131lt1_1 {\n" +
			    "    getData <- replace getR1\n" +
			    "      base when (!isExecutingCallin());\n" +
			    "    @SuppressWarnings(\"basecall\")\n" +
			    "    callin R1<@base> getData() {\n" +
			    "      R3 r3 = new R3();\n" +
			    "      return r3.calculate(this);\n" +
			    "    }\n" +
			    "  }\n" +
			    "  protected class R3 {\n" +
			    "    protected R1<@lowerTeam> calculate(final Team1131lt1_1 lowerTeam) {\n" +
			    "      return lowerTeam.getR1(\"OK\");\n" +
			    "    }\n" +
			    "  }\n" +
			    "  void test() {\n" +
			    "    this.activate();\n" +
			    "    final Team1131lt1_1 t1 = new Team1131lt1_1();\n" +
			    "    System.out.print(t1.getR1(\"NOTOK\"));\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new Team1131lt1_2().test();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"Team1131lt1_1.java",
			    "\n" +
			    "public team class Team1131lt1_1 {\n" +
			    "  public class R1 {\n" +
			    "    String val;\n" +
			    "    public R1(String v) { this.val = v; }\n" +
			    "    public String toString() { return val; }\n" +
			    "  }\n" +
			    "  public R1 getR1(String v) { return new R1(v); }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // role refers to base of sibling role - witness for NPE in TeamAnchor.getStrengthenedRole() resp. AssertionError in RTB.<init> - error case
    // 1.1.31-otjld-stacked-teams-2
    public void test1131_stackedTeams2() {
        runNegativeTestMatching(
            new String[] {
		"Team1131lt2_2.java",
			    "\n" +
			    "public team class Team1131lt2_2 {\n" +
			    "  protected class R2 playedBy Team1131lt2_1 {\n" +
			    "    getData <- replace getR1\n" +
			    "      base when (!isExecutingCallin());\n" +
			    "    @SuppressWarnings(\"basecall\")\n" +
			    "    callin R1<@base> getData() {\n" +
			    "      R3 r3 = new R3();\n" +
			    "      R2 r = this;\n" +
			    "      return r3.calculate(r);\n" +
			    "    }\n" +
			    "  }\n" +
			    "  protected class R3 {\n" +
			    "    protected R1<@lowerTeam> calculate(final Team1131lt2_1 lowerTeam) {\n" +
			    "      return lowerTeam.getR1(\"OK\");\n" +
			    "    }\n" +
			    "  }\n" +
			    "  void test() {\n" +
			    "    this.activate();\n" +
			    "    final Team1131lt2_1 t1 = new Team1131lt2_1();\n" +
			    "    System.out.print(t1.getR1(\"NOTOK\"));\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new Team1131lt2_2().test();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"Team1131lt2_1.java",
			    "\n" +
			    "public team class Team1131lt2_1 {\n" +
			    "  public class R1 {\n" +
			    "    String val;\n" +
			    "    public R1(String v) { this.val = v; }\n" +
			    "    public String toString() { return val; }\n" +
			    "  }\n" +
			    "  public R1 getR1(String v) { return new R1(v); }\n" +
			    "}\n" +
			    "  \n"
            },
            "1.2.2(e)");
    }

    public void testBug411449() {
    	runConformTest(
    		new String[] {
    			"X.java",
    			"public team class X {\n" +
    			"	final R r;\n" +
    			"	public X(R r) {\n" +
    			"		this.r = r;\n" +
    			"	}\n" +
    			"	protected class R {}\n" +
    			"}\n"
    		});
    }
}
