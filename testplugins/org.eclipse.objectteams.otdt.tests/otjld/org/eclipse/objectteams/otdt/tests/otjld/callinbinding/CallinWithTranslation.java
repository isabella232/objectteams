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
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otjld.callinbinding;

import junit.framework.Test;

import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

public class CallinWithTranslation extends AbstractOTJLDTest {
	
	public CallinWithTranslation(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test0c11_implicitlyInheritingStaticRoleMethod1"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return CallinWithTranslation.class;
	}


    // a callin binding without signatures lifts its parameter
    // 4.4.1-otjld-callin-with-parameter-lifting-1
    public void test441_callinWithParameterLifting1() {
       
       runConformTest(
            new String[] {
		"Team441cwpl1.java",
			    "\n" +
			    "public team class Team441cwpl1 {\n" +
			    "	public class R playedBy T441cwpl1 {\n" +
			    "		void aspect (R other) {\n" +
			    "			System.out.print(other.getValue());\n" +
			    "		}\n" +
			    "		String getValue() {\n" +
			    "			return \"K\";\n" +
			    "		}\n" +
			    "		aspect <- after foo;\n" +
			    "	}\n" +
			    "	public Team441cwpl1(T441cwpl1 o1, T441cwpl1 o2) {\n" +
			    "		activate();\n" +
			    "		o1.foo(o2);\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team441cwpl1(new T441cwpl1(), new T441cwpl1());\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T441cwpl1.java",
			    "\n" +
			    "public class T441cwpl1 {\n" +
			    "	void foo(T441cwpl1 other) {\n" +
			    "		System.out.print(\"O\");\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a callin binding with array result lifting
    // 4.4.1-otjld-callin-with-result-lifting-2
    public void test441_callinWithResultLifting2() {
       
       runConformTest(
            new String[] {
		"Team441cwrl2.java",
			    "\n" +
			    "public team class Team441cwrl2 {\n" +
			    "    public class R playedBy T441cwrl2 {\n" +
			    "        callin R[] aspect() {\n" +
			    "            R[] ra = base.aspect();\n" +
			    "            System.out.print(ra[0].getValue());\n" +
			    "            return ra;\n" +
			    "        }\n" +
			    "        String getValue() {\n" +
			    "            return \"K\";\n" +
			    "        }\n" +
			    "        aspect <- replace foo;\n" +
			    "    }\n" +
			    "    public Team441cwrl2(T441cwrl2 o1, T441cwrl2 o2) {\n" +
			    "        activate();\n" +
			    "        o1.foo();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team441cwrl2(new T441cwrl2(), new T441cwrl2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T441cwrl2.java",
			    "\n" +
			    "public class T441cwrl2 {\n" +
			    "    T441cwrl2[] foo() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "        T441cwrl2 b = new T441cwrl2();\n" +
			    "        T441cwrl2[] ba = new T441cwrl2[1];\n" +
			    "        ba[0] = b;\n" +
			    "        return ba;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin binding with array result lifting , base is inner class
    // 4.4.1-otjld-callin-with-result-lifting-2i
    public void test441_callinWithResultLifting2i() {
       
       runConformTest(
            new String[] {
		"Team441cwrl2i.java",
			    "\n" +
			    "public team class Team441cwrl2i {\n" +
			    "    public class R playedBy T441cwrl2i.Inner {\n" +
			    "        callin R[] aspect() {\n" +
			    "            R[] ra = base.aspect();\n" +
			    "            System.out.print(ra[0].getValue());\n" +
			    "            return ra;\n" +
			    "        }\n" +
			    "        String getValue() {\n" +
			    "            return \"K\";\n" +
			    "        }\n" +
			    "        aspect <- replace foo;\n" +
			    "    }\n" +
			    "    public Team441cwrl2i(T441cwrl2i.Inner o1, T441cwrl2i.Inner o2) {\n" +
			    "        activate();\n" +
			    "        o1.foo();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T441cwrl2i outer= new T441cwrl2i();\n" +
			    "        new Team441cwrl2i(outer.new Inner(), outer.new Inner());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T441cwrl2i.java",
			    "\n" +
			    "public class T441cwrl2i {\n" +
			    "    public class Inner {\n" +
			    "        Inner[] foo() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "            Inner b = new Inner();\n" +
			    "            Inner[] ba = new Inner[1];\n" +
			    "            ba[0] = b;\n" +
			    "            return ba;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin binds an overloaded base method without specifying signatures
    // 4.4.2-otjld-ambiguous-callin-binding-1
    public void test442_ambiguousCallinBinding1() {
        runNegativeTest(
            new String[] {
		"T442acb1.java",
			    "\n" +
			    "public class T442acb1 {\n" +
			    "		void foo() {}\n" +
			    "		void foo(int i) {}\n" +
			    "}\n" +
			    "	\n",
		"Team442acb1.java",
			    "\n" +
			    "public team class Team442acb1 {\n" +
			    "	protected class R playedBy T442acb1 {\n" +
			    "		void bar () { }\n" +
			    "		bar <- after foo;\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            null);
    }

    // a callin binds a base method that is overloaded along extends without specifying signatures
    // 4.4.2-otjld-ambiguous-callin-binding-2
    public void test442_ambiguousCallinBinding2() {
        runNegativeTest(
            new String[] {
		"T442acb2_1.java",
			    "\n" +
			    "public class T442acb2_1 {\n" +
			    "		void foo(int i) {}\n" +
			    "}\n" +
			    "	\n",
		"T442acb2_2.java",
			    "\n" +
			    "public class T442acb2_2 extends T442acb2_1 {\n" +
			    "		void foo() {}\n" +
			    "}\n" +
			    "	\n",
		"Team442acb2.java",
			    "\n" +
			    "public team class Team442acb2 {\n" +
			    "	protected class R playedBy T442acb2_2 {\n" +
			    "		void bar () { }\n" +
			    "		bar <- after foo;\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            null);
    }

    // a role method return a result which is lowered for passing back to the base
    // 4.4.3-otjld-callin-with-result-lowering-1
    public void test443_callinWithResultLowering1() {
       
       runConformTest(
            new String[] {
		"T443cwrl1Main.java",
			    "\n" +
			    "public class T443cwrl1Main {\n" +
			    "	public static void main (String[] args) {\n" +
			    "		Team443cwrl1 t = new Team443cwrl1();\n" +
			    "		t.test(new T443cwrl1(\"OK\"));\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T443cwrl1.java",
			    "\n" +
			    "public class T443cwrl1 {\n" +
			    "	private String value;\n" +
			    "	public T443cwrl1(String value) { this.value = value; }\n" +
			    "	public T443cwrl1 getOther() { return null; }\n" +
			    "	public String getValue() { return this.value; }\n" +
			    "}\n" +
			    "	\n",
		"Team443cwrl1.java",
			    "\n" +
			    "public team class Team443cwrl1 {\n" +
			    "	R masterR;\n" +
			    "	public class R playedBy T443cwrl1 {\n" +
			    "		callin R getMasterR() {\n" +
			    "			return masterR;\n" +
			    "		}\n" +
			    "		getMasterR <- replace getOther;\n" +
			    "	}\n" +
			    "	public void test (T443cwrl1 as R r) {\n" +
			    "		masterR = r;\n" +
			    "		activate();\n" +
			    "		T443cwrl1 obj = new T443cwrl1(\"NOK\");\n" +
			    "		T443cwrl1 other = obj.getOther();\n" +
			    "		System.out.print(other.getValue());\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // A callin method lifts its arg and passes it (lowered) back to the base call
    // 4.4.4-otjld-callin-replace-lifts-arg-1
    public void test444_callinReplaceLiftsArg1() {
       
       runConformTest(
            new String[] {
		"Team444crla1_2.java",
			    "\n" +
			    "public team class Team444crla1_2 extends Team444crla1_1 {\n" +
			    "    protected class R1 {\n" +
			    "        grumble <- replace test;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        (new Team444crla1_2()).activate();\n" +
			    "        (new T444crla1_1()).test(new T444crla1_2());        \n" +
			    "    }\n" +
			    "}\n" +
			    "	\n",
		"T444crla1_1.java",
			    "\n" +
			    "public class T444crla1_1 {\n" +
			    "    void test(T444crla1_2 b) {\n" +
			    "        b.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "	\n",
		"T444crla1_2.java",
			    "\n" +
			    "public class T444crla1_2 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}	\n" +
			    "	\n",
		"Team444crla1_1.java",
			    "\n" +
			    "public team class Team444crla1_1 {\n" +
			    "    protected class R0 playedBy T444crla1_2 {}\n" +
			    "    protected class R2 extends R0 {}\n" +
			    "    protected class R1 playedBy T444crla1_1 {\n" +
			    "        callin void grumble(R2 r) {\n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.grumble(r);\n" +
			    "        }\n" +
			    "\n" +
			    "    }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // A callin method casts its arg and passes it (downcasted) back to the base call
    // 4.4.5-otjld-callin-replace-casts-arg-1f
    public void test445_callinReplaceCastsArg1f() {
        runNegativeTestMatching(
            new String[] {
		"Team445crca1f.java",
			    "\n" +
			    "public team class Team445crca1f {\n" +
			    "    protected class R playedBy T445crca1f {\n" +
			    "        callin void rm(Object o) {\n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.rm(o);\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T445crca1f.java",
			    "\n" +
			    "public class T445crca1f {\n" +
			    "    public void bm(String s) {\n" +
			    "        System.out.print(s);\n" +
			    "    }\n" +
			    "}   \n" +
			    "    \n"
            },
            "4.5(d)");
    }

    // A callin method casts its arg and passes it (downcasted) back to the base call
    // 4.4.5-otjld-callin-replace-casts-arg-1
    public void test445_callinReplaceCastsArg1() {
       
       runConformTest(
            new String[] {
		"Team445crca1.java",
			    "\n" +
			    "public team class Team445crca1 {\n" +
			    "    protected class R playedBy T445crca1 {\n" +
			    "        callin <T> void rm(T o) {\n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.rm(o);\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T445crca1 b = new T445crca1();\n" +
			    "        Team445crca1 t = new Team445crca1();\n" +
			    "        t.activate();\n" +
			    "        b.bm(\"K\");\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"T445crca1.java",
			    "\n" +
			    "public class T445crca1 {\n" +
			    "    public void bm(String s) {\n" +
			    "        System.out.print(s);\n" +
			    "    }\n" +
			    "}   \n" +
			    "    \n"
            },
            "OK");
    }

    // a callin method lifts an array of bases (inner class)
    // 4.4.6-otjld-callin-array-arg-lifting-1
    public void test446_callinArrayArgLifting1() {
       
       runConformTest(
            new String[] {
		"Team446caal1.java",
			    "\n" +
			    "public team class Team446caal1 {\n" +
			    "    protected class R playedBy T446caal1.Inner {\n" +
			    "        callin void printArray(R[] rs) {\n" +
			    "            System.out.print(\"->\");\n" +
			    "            base.printArray(rs);\n" +
			    "            System.out.print(\"<-\");\n" +
			    "        }\n" +
			    "        void printArray(R[] rs) <- replace void printArray(T446caal1.Inner[] is);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team446caal1().activate();\n" +
			    "        new T446caal1().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T446caal1.java",
			    "\n" +
			    "public class T446caal1 {\n" +
			    "    public class Inner {\n" +
			    "        void printArray(Inner[] inners) {\n" +
			    "            for (Inner i : inners)\n" +
			    "                i.print();\n" +
			    "        }\n" +
			    "        void print() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        Inner i= new Inner();\n" +
			    "        i.printArray(new Inner[]{i});\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "->OK<-");
    }

    public void testBug473392() {
    	runConformTest(
    		new String[] {
    	"t1/MyTeam.java",
    			"package t1;\n" + 
    			"\n" + 
    			"import b1.MyBase;\n" + 
    			"\n" + 
    			"public team class MyTeam {\n" + 
    			"    protected team class Mid { \n" + 
    			"        \n" + 
    			"        protected class R playedBy MyBase {\n" + 
    			"            callin void rm1(R r) {\n" + 
    			"                System.out.println(\"rm1 \" + r.getClass().getName());\n" + 
    			"                base.rm1(r);\n" + 
    			"            }\n" + 
    			"            callin1: rm1 <- replace id, process;\n" + 
    			"            callin R rm2(R r) {\n" + 
    			"                System.out.println(\"rm2\");\n" + 
    			"                return this;\n" + 
    			"            }\n" + 
    			"            callin2: rm2 <- replace id\n" + 
    			"                base when (false);\n" + 
    			"\n" + 
    			"            precedence callin2, callin1;\n" + 
    			"        }\n" + 
    			"    }\n" + 
    			"    \n" + 
    			"    public static void main(String[] args) {\n" + 
    			"        new MyTeam().new Mid().activate();\n" + 
    			"        new MyBase().process(new MyBase());\n" + 
    			"    }\n" + 
    			"}\n",
    	"b1/MyBase.java",
    			"package b1;\n" + 
    			"\n" + 
    			"public class MyBase {\n" + 
    			"    MyBase id(MyBase other) { return this; }\n" + 
    			"    public MyBase process(MyBase other) {\n" + 
    			"        other = id(other);\n" + 
    			"        System.out.println(other.getClass().getName());\n" + 
    			"        return null;\n" + 
    			"    }\n" + 
    			"}\n"
    		},
    		"rm1 t1.MyTeam$__OT__Mid$__OT__R\n" + 
			"rm1 t1.MyTeam$__OT__Mid$__OT__R\n" + 
			"b1.MyBase");
    }

    public void testBug531011_secondary() {
    	runNegativeTest(
    		new String[] {
    			"Main.java",
    			"public class Main {\n" +
    			"	public static void main(String... args) {\n" +
    			"		new t.Team1().activate();\n" +
    			"		System.out.print(b.Base.<String>m());\n" +
    			"	}\n" +
    			"}\n",
    			"b/Base.java",
    			"package b;\n" +
    			"public class Base {\n" +
    			"	public static <T> T m() { return null; }\n" +
    			"}\n",
    			"t/Team1.java",
    			"package t;\n" +
    			"import base b.Base;\n" +
    			"public team class Team1 {\n" +
    			"	protected class R playedBy Base {\n" +
    			"		R() { base(); }\n" +
    			"		<T> T rm() <- replace T m()\n" +
    			"			with { result -> result }\n" +
    			"		static callin <T> T rm() {\n" +
    			"			return base.rm();\n" +
    			"		}\n" +
    			"	}\n" +
    			"}\n"
    		},
			"----------\n" + 
			"1. ERROR in t\\Team1.java (at line 6)\n" + 
			"	<T> T rm() <- replace T m()\n" + 
			"	      ^^\n" + 
			"T cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in t\\Team1.java (at line 6)\n" + 
			"	<T> T rm() <- replace T m()\n" + 
			"	      ^^\n" + 
			"T cannot be resolved to a type\n" + 
			"----------\n");
    }
}
