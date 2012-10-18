/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005-2010 Berlin Institute of Technology, Germany.
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
 * 	Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otjld.regression;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

import junit.framework.Test;

/**
 * @author Christine Hundt
 * @author Stephan Herrmann
 * @author Jürgen Widiker
 */
@SuppressWarnings("unchecked")
public class ReportedBugs extends AbstractOTJLDTest {
	
	public ReportedBugs(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "testBug391876"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return ReportedBugs.class;
	}

    
    String[] getClassLibraries(String jarFilename) {
    	String destPath = this.outputRootDirectoryPath+"/regression";
    	createOutputTestDirectory("/regression");
    	// upload the jar:
		Util.copy(getTestResourcePath(jarFilename), destPath);
    	// setup classpath:
    	String[] classPaths = getDefaultClassPaths();
    	int l = classPaths.length;
    	System.arraycopy(classPaths, 0, classPaths=new String[l+1], 0, l);
		classPaths[l] = this.outputRootDirectoryPath+"/regression/"+jarFilename;
		return classPaths;
    }

    // reported against GebitProposalComputer - typo in testcase
    // B.1.1-otjld-sh-1f
    public void testB11_sh1f() {
        runNegativeTest(
            new String[] {
		"TB11sh1fMain.java",
			    "\n" +
			    "public class TB11sh1fMain {\n" +
			    "   public TB11sh1fMain() {\n" +
			    "        // type in this declaration has a typo:\n" +
			    "        final TeamB11sh1f1 theTeam = new TeamB11sh1f();\n" +
			    "        theTeam.R1[] r1s = new theTeam.R1[1];\n" +
			    "        r1s[0] = theTeam.new R1();\n" +
			    "        theTeam.R3 r3 = r1s[0].getR2().getR3();\n" +
			    "        r3.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TeamB11sh1f.java",
			    "\n" +
			    "public team class TeamB11sh1f {\n" +
			    "    public class R1 {\n" +
			    "        public R2 getR2() {return new R2();}\n" +
			    "    }\n" +
			    "    public class R2 {\n" +
			    "        public R3 getR3() {return new R3();}\n" +
			    "    }\n" +
			    "    public class R3 {\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TB11sh1fMain.java (at line 5)\n" + 
    		"	final TeamB11sh1f1 theTeam = new TeamB11sh1f();\n" + 
    		"	      ^^^^^^^^^^^^\n" + 
    		"TeamB11sh1f1 cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"2. ERROR in TB11sh1fMain.java (at line 6)\n" + 
    		"	theTeam.R1[] r1s = new theTeam.R1[1];\n" + 
    		"	^^^^^^^\n" + 
    		"TeamB11sh1f1 cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"3. ERROR in TB11sh1fMain.java (at line 6)\n" + 
    		"	theTeam.R1[] r1s = new theTeam.R1[1];\n" + 
    		"	                       ^^^^^^^\n" + 
    		"TeamB11sh1f1 cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"4. ERROR in TB11sh1fMain.java (at line 7)\n" + 
    		"	r1s[0] = theTeam.new R1();\n" + 
    		"	                     ^^\n" + 
    		"TeamB11sh1f1.R1 cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"5. ERROR in TB11sh1fMain.java (at line 8)\n" + 
    		"	theTeam.R3 r3 = r1s[0].getR2().getR3();\n" + 
    		"	^^^^^^^\n" + 
    		"TeamB11sh1f1 cannot be resolved to a type\n" + 
    		"----------\n");
    }

    // reported by Robert in GebitProposalComputer
    // B.1.1-otjld-sh-1
    public void testB11_sh1() {
       
       runConformTest(
            new String[] {
		"TB11sh1Main.java",
			    "\n" +
			    "public class TB11sh1Main {\n" +
			    "   public static void main(String[] args) { \n" +
			    "        final TeamB11sh1 theTeam = new TeamB11sh1();\n" +
			    "        R1<@theTeam>[] r1s = new R1<@theTeam>[1];\n" +
			    "        r1s[0] = theTeam.new R1();\n" +
			    "        R3<@theTeam> r3 = r1s[0].getR2().getR3();\n" +
			    "        r3.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TeamB11sh1.java",
			    "\n" +
			    "public team class TeamB11sh1 {\n" +
			    "    public class R1 {\n" +
			    "        public R2 getR2() {return new R2();}\n" +
			    "    }\n" +
			    "    public class R2 {\n" +
			    "        public R3 getR3() {return new R3();}\n" +
			    "    }\n" +
			    "    public class R3 {\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // TPX-298
    // B.1.1-otjld-sh-2
    public void testB11_sh2() {
        runNegativeTestMatching(
            new String[] {
		"test/TeamB11sh2.java",
			    "\n" +
			    "package test;\n" +
			    "public team class TeamB11sh2 {\n" +
			    "    public class R extends TB11sh2 {\n" +
			    "        protected void m1(){} \n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"test/TB11sh2.java",
			    "\n" +
			    "package test;\n" +
			    "public class TB11sh2 {\n" +
			    "    public void m1() {} \n" +
			    "}    \n" +
			    "    \n"
            },
            "reduce");
    }

    // TPX-288
    // B.1.1-otjld-sh-3
    public void testB11_sh3() {
        runConformTest(
            new String[] {
		"TeamB11sh3.java",
			    "\n" +
			    "public team class TeamB11sh3 {\n" +
			    "    public class Role1 { \n" +
			    "       public synchronized void foo() {} \n" +
			    "    } \n" +
			    "}    \n" +
			    "    \n"
            });
    }

    // TPX-282
    // B.1.1-otjld-sh-4
    public void testB11_sh4() {
       Map customOptions = getCompilerOptions();
        runNegativeTest(
            new String[] {
		"TB11sh4.java",
			    "\n" +
			    "public class TB11sh4 {}    \n" +
			    "    \n",
		"TeamB11sh4_1.java",
			    "\n" +
			    "public team class TeamB11sh4_1 {\n" +
			    "    protected TeamB11sh4_1 (Role1 r) { \n" +
			    "       foo(); \n" +
			    "    } \n" +
			    "    public class Role1 {} \n" +
			    "}    \n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh4_1.java (at line 4)\n" + 
    		"	foo(); \n" + 
    		"	^^^\n" + 
    		"The method foo() is undefined for the type TeamB11sh4_1\n" + 
    		"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions,
            true/*generateOutput*/,
            false/*showCategory*/,
            false/*showWarningToken*/);
        runNegativeTest(
            new String[] {
		"TeamB11sh4_2.java",
			    "\n" +
			    "public team class TeamB11sh4_2 extends TeamB11sh4_1 {\n" +
			    "    TeamB11sh4_2(TB11sh4 as Role2 b) { \n" +
			    "       super(b); \n" +
			    "    } \n" +
			    "    @Override\n" +
			    "    public class Role1 extends Role2 playedBy TB11sh4 {} \n" +
			    "}    \n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh4_2.java (at line 3)\n" + 
    		"	TeamB11sh4_2(TB11sh4 as Role2 b) { \n" + 
    		"	                        ^^^^^\n" + 
    		"Role2 cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"2. ERROR in TeamB11sh4_2.java (at line 7)\n" + 
    		"	public class Role1 extends Role2 playedBy TB11sh4 {} \n" + 
    		"	                           ^^^^^\n" + 
    		"Role2 cannot be resolved to a type\n" + 
    		"----------\n",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            customOptions);
    }

    // TPX-262 follow up: not falsely recognizing CallinLabels
    // B.1.1-otjld-sh-5
    public void testB11_sh5() {
        runConformTest(
            new String[] {
		"TB11sh5.java",
			    "\n" +
			    "public class TB11sh5 {\n" +
			    "    int x = 1;\n" +
			    "    void foo(int i) { \n" +
			    "        switch (i) {\n" +
			    "        case Integer.MAX_VALUE: break;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            });
    }

    // TPX-262 follow up: syntax errors recognizing CallinLabels in wrong locations
    // B.1.1-otjld-sh-6
    public void testB11_sh6() {
        runNegativeTest(
            new String[] {
		"TB11sh6.java",
			    "\n" +
			    "public class TB11sh6 {\n" +
			    "    int team x = 1;\n" +
			    "    void foo(int i) { \n" +
			    "        switch (i) {\n" +
			    "        case Integer.MAX_VALUE: break;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TB11sh6.java (at line 3)\n" + 
    		"	int team x = 1;\n" + 
    		"	    ^^^^\n" + 
    		"Syntax error on token \"team\", delete this token\n" + 
    		"----------\n");
    }

    // TPX-262 follow up: syntax errors recognizing CallinLabels
    // B.1.1-otjld-sh-7
    public void testB11_sh7() {
        runNegativeTest(
            new String[] {
		"TB11sh7.java",
			    "\n" +
			    "public team class TB11sh7 {\n" +
			    "    protected class R playedBy TB11sh7_2 {\n" +
			    "        int team x = 1;\n" +
			    "        void foo(int i) { \n" +
			    "        }\n" +
			    "        myCallin:\n" +
			    "        foo <- after charAt;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TB11sh7_2.java",
				"public class TB11sh7_2 {\n" +
				"    char charAt(int i) { return 'c'; }\n" +
				"}\n"
            },
            "----------\n" + 
    		"1. ERROR in TB11sh7.java (at line 4)\n" + 
    		"	int team x = 1;\n" + 
    		"	    ^^^^\n" + 
    		"Syntax error on token \"team\", delete this token\n" + 
    		"----------\n");
    }

    // originally an OOSE error, stripped down by resix, problem was: not generating ReferencedTeams regarding team in a package
    // B.1.1-otjld-sh-8
    public void testB11_sh8() {
       Map customOptions = getCompilerOptions();
       myWriteFiles(
    		new String[] {
		"mypackage/config.txt",
			    "\n" +
			    "mypackage.SubTeamB11sh8_2\n" +
			    "    \n"});
       runConformTest(
            new String[] {
		"mypackage/B11sh8Main.java",
			    "\n" +
			    "package mypackage;\n" +
			    "public class B11sh8Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamB11sh8_1 t = new TeamB11sh8_1();\n" +
			    "        t.tm();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"mypackage/TeamB11sh8_1.java",
			    "\n" +
			    "package mypackage;\n" +
			    "public team class TeamB11sh8_1 {\n" +
			    "    public void tm() {}\n" +
			    "}\n" +
			    "    \n",
		"mypackage/TeamB11sh8_2.java",
			    "\n" +
			    "package mypackage;\n" +
			    "public team class TeamB11sh8_2 {\n" +
			    "    public class MySubRole playedBy TeamB11sh8_1 {\n" +
			    "        public void rm() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        void rm() <- before void tm()\n" +
			    "            base when(true);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"mypackage/SubTeamB11sh8_2.java",
			    "\n" +
			    "package mypackage;\n" +
			    "public team class SubTeamB11sh8_2 extends TeamB11sh8_2 {}\n" +
			    "    \n"
            },
            "OK",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            getTeamActivationVMArgs("mypackage/config.txt")/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // TPX-317 private methods accessible across extends
    // B.1.1-otjld-sh-9
    public void testB11_sh9() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh9.java",
			    "\n" +
			    "public team class TeamB11sh9 {\n" +
			    "     public class R1 { \n" +
			    "         private void m1() { } \n" +
			    "     } \n" +
			    "      \n" +
			    "     public class R2 extends R1 { \n" +
			    "         public void test() { \n" +
			    "             m1(); \n" +
			    "         } \n" +
			    "     } \n" +
			    "}\n" +
			    "    \n"
            },
            "1.2.1(e)");
    }

    // Bug reported by Karsten Meier (miniCRM): result in after binding may need lifting
    // B.1.1-otjld-sh-10
    public void testB11_sh10() {
       
       runConformTest(
            new String[] {
		"TeamB11sh10.java",
			    "\n" +
			    "public team class TeamB11sh10 {\n" +
			    "    @SuppressWarnings(\"decapsulation\") // get and set val\n" +
			    "    protected class R playedBy TB11sh10 {\n" +
			    "        void extend (R other, String s) {\n" +
			    "            other.setVal(other.getVal()+\"K\");\n" +
			    "        }\n" +
			    "        protected String getVal() -> get String val;\n" +
			    "        protected void setVal(String s) -> set String val;\n" +
			    "        void extend(R other, String s) <- after TB11sh10 getOther(String s) \n" +
			    "            with { other <- result, s <- s }\n" +
			    "    }\n" +
			    "    public static void main (String[] args) {\n" +
			    "        (new TeamB11sh10()).activate();\n" +
			    "        TB11sh10 b = new TB11sh10();\n" +
			    "        b.getOther(\"O\").print();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TB11sh10.java",
			    "\n" +
			    "public class TB11sh10 {\n" +
			    "    private String val = \"NOK\";\n" +
			    "    public void print() { System.out.print(val); }\n" +
			    "    public TB11sh10 getOther(String s) {\n" +
			    "        TB11sh10 result = new TB11sh10();\n" +
			    "        result.val = s;\n" +
			    "        return result;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // Bug reported by Karsten Meier 1.9.2005, incomplete parameter mapping
    // B.1.1-otjld-sh-11
    public void testB11_sh11() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh11.java",
			    "\n" +
			    "public team class TeamB11sh11 {\n" +
			    "	public class R playedBy TB11sh11 {\n" +
			    "		private R other;\n" +
			    "		@SuppressWarnings(\"unused\") private boolean flag;\n" +
			    "		void setOther(R other, boolean flag) {\n" +
			    "			this.other = other;\n" +
			    "			this.other.flag = flag;\n" +
			    "		}\n" +
			    "		void setOther(R other, boolean flag) <- after void setOther(TB11sh11 other) \n" +
			    "		with {\n" +
			    "			other <- other,\n" +
			    "			// flag  <- true // this is the bug\n" +
			    "		}	\n" +
			    "		String toString() <- after void setOther(TB11sh11 other);	\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"TB11sh11.java",
			    "\n" +
			    "public class TB11sh11 {\n" +
			    "        @SuppressWarnings(\"unused\")\n" +
			    "	private TB11sh11 other;\n" +
			    "	void setOther(TB11sh11 other) {\n" +
			    "		this.other = other;\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "OTJLD 4.4(a)");
    }

    // Bug reported by Karsten Meier 1.9.2005, incomplete parameter mapping - two errors corrected
    // B.1.1-otjld-sh-11b
    public void testB11_sh11b() {
        runNegativeTest(
            new String[] {
		"TeamB11sh11b.java",
			    "\n" +
			    "public team class TeamB11sh11b {\n" +
			    "	public class R playedBy TB11sh11b {\n" +
			    "		private R other;\n" +
			    "		protected boolean flag;\n" +
			    "		void setOther(R other, boolean flag) {\n" +
			    "			this.other = other;\n" +
			    "			this.other.flag = flag;\n" +
			    "		}\n" +
			    "		setOther:\n" +
			    "       @SuppressWarnings(\"hiding\")\n" +
			    "		void setOther(R other, boolean flag) <- after void setOther(TB11sh11b other) \n" +
			    "		with {\n" +
			    "			other <- other,\n" +
			    "			// flag  <- true // this is the bug\n" +
			    "		}	\n" +
			    "		toString:\n" +
			    "       @SuppressWarnings(\"hiding\")\n" +
			    "		String toString() <- after void setOther(TB11sh11b other);	\n" +
			    "		precedence after toString, setOther;\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"TB11sh11b.java",
			    "\n" +
			    "public class TB11sh11b {\n" +
			    "   @SuppressWarnings(\"unused\")\n" +
			    "	private TB11sh11b other;\n" +
			    "	void setOther(TB11sh11b other) {\n" +
			    "		this.other = other;\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh11b.java (at line 12)\n" + 
    		"	void setOther(R other, boolean flag) <- after void setOther(TB11sh11b other) \n" + 
    		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
    		"Incomplete parameter mapping: argument flag of role method void setOther(R other, boolean flag) is not mapped (OTJLD 4.4(a)).\n" + 
    		"----------\n");
    }

    // TPX-329 abstract method in non-abstract role
    // B.1.1-otjld-sh-12
    public void testB11_sh12() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh12.java",
			    "\n" +
			    "public team class TeamB11sh12 {\n" +
			    "    public class R {\n" +
			    "        public void test() {}\n" +
			    "        public abstract void foo() {\n" +
			    "            test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "abstract");
    }

    // detected while creating an example for discussion with Erik Ernst
    // B.1.1-otjld-sh-13
    public void testB11_sh13() {
       
       runConformTest(
            new String[] {
		"TeamB11sh13_2.java",
			    "\n" +
			    "public team class TeamB11sh13_2 {\n" +
			    "	protected class R2 playedBy TB11sh13 {\n" +
			    "		callin int log (int l1, int l2) {\n" +
			    "			System.out.print(l1+\"&\"+l2);\n" +
			    "			return 3 * base.log(l1, l2);\n" +
			    "		}\n" +
			    "		int log(int l1, int l2) <- replace int sum(int i1, int i2);\n" +
			    "	}\n" +
			    "	TeamB11sh13_2() { activate(); }\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new TeamB11sh13_1();\n" +
			    "		new TeamB11sh13_2();\n" +
			    "		System.out.print(\"=\"+(new TB11sh13()).sum(3,7));\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"TB11sh13.java",
			    "\n" +
			    "public class TB11sh13 {\n" +
			    "	public int sum(int i1, int i2) {\n" +
			    "        	return i1 + i2;\n" +
			    "        }\n" +
			    "\n" +
			    "}	\n" +
			    "	\n",
		"TeamB11sh13_1.java",
			    "\n" +
			    "public team class TeamB11sh13_1 {\n" +
			    "	public class R1 playedBy TB11sh13 {\n" +
			    "		callin void twice(int j) {\n" +
			    "			base.twice(2*j);\n" +
			    "		}\n" +
			    "		void twice(int j) <- replace int sum(int i1, int i2) \n" +
			    "		    with { j <- i1 }; // base call will tunnel the provided i2 unchanged\n" +
			    "	}\n" +
			    "	public TeamB11sh13_1() {  activate(); }\n" +
			    "}	\n" +
			    "	\n"
            },
            "3&7=39");
    }

    // TPX-320 nested copy of callout
    // B.1.1-otjld-sh-14
    public void testB11_sh14() {
       
       runConformTest(
            new String[] {
		"TeamB11sh14.java",
			    "\n" +
			    "public team class TeamB11sh14 {\n" +
			    "    public team class NestedTeam1\n" +
			    "    {\n" +
			    "        public class InnerRole playedBy TB11sh14\n" +
			    "        {\n" +
			    "            public void foo()\n" +
			    "            {\n" +
			    "                System.out.print(\"N\");\n" +
			    "                bar();\n" +
			    "            }\n" +
			    "            public abstract void bar();\n" +
			    "\n" +
			    "            bar -> extracted;\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public team class NestedTeam2 extends NestedTeam1\n" +
			    "    {\n" +
			    "        public class InnerRole \n" +
			    "        {\n" +
			    "            foo => extracted;\n" +
			    "        }\n" +
			    "        protected NestedTeam2() {\n" +
			    "            InnerRole r = new InnerRole(new TB11sh14());\n" +
			    "            r.foo();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    TeamB11sh14() {\n" +
			    "        new NestedTeam2();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh14();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TB11sh14.java",
			    "\n" +
			    "public class TB11sh14 {\n" +
			    "    public void extracted() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // TPX-366 renamed nested team
    // B.1.1-otjld-sh-15
    public void testB11_sh15() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh15.java",
			    "\n" +
			    "public team class TeamB11sh15 {\n" +
			    "	public team class TeamB11sh15 {}\n" +
			    "}		\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh15.java (at line 3)\n" + 
    		"	public team class TeamB11sh15 {}\n" + 
    		"	                  ^^^^^^^^^^^\n" + 
    		"The nested type TeamB11sh15 cannot hide an enclosing type\n" + 
    		"----------\n");
    }

    // white box: breaking MessageSend.resolve by externally invoking a shorthand callout
    // B.1.1-otjld-sh-16
    public void testB11_sh16() {
       
       runConformTest(
            new String[] {
		"TB11sh16Main.java",
			    "\n" +
			    "public class TB11sh16Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamB11sh16_1 t = new TeamB11sh16_1();\n" +
			    "        t.new R().bar();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh16.java",
			    "\n" +
			    "public class TB11sh16 {\n" +
			    "    void foo() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh16_1.java",
			    "\n" +
			    "public team class TeamB11sh16_1 {\n" +
			    "    public class R playedBy TB11sh16 {\n" +
			    "        public abstract void bar();\n" +
			    "        void bar() -> void foo();\n" +
			    "        public R() {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // local class in parameter mapping - reported by hsudhof, see SessionDeployment
    // B.1.1-otjld-sh-17
    public void testB11_sh17() {
       
       runConformTest(
            new String[] {
		"TeamB11sh17.java",
			    " \n" +
			    "public team class TeamB11sh17 {\n" +
			    "    protected class R playedBy TB11sh17 {\n" +
			    "        void rm (IB11sh17 delegate) {\n" +
			    "            delegate.print('K');\n" +
			    "        }    \n" +
			    "        void rm(IB11sh17 delegate) <- after void test(final char c) \n" +
			    "        with {\n" +
			    "            delegate <- new IB11sh17() {\n" +
			    "                public void print(char cp) {\n" +
			    "                    System.out.print(cp);\n" +
			    "                    System.out.print(c);\n" +
			    "                }\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh17().activate();\n" +
			    "        new TB11sh17().test('!');\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TB11sh17.java",
			    "\n" +
			    "public class TB11sh17 {\n" +
			    "    void test(char c) {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"IB11sh17.java",
			    "\n" +
			    "public interface IB11sh17 {\n" +
			    "    void print(char c);\n" +
			    "}  \n" +
			    "    \n"
            },
            "OK!");
    }

    // TPX-397: conflict between resolution of anchored types and sorting of field bindings
    // B.1.1-otjld-sh-18
    public void testB11_sh18() {
       
       runConformTest(
            new String[] {
		"p2/C3.java",
			    "\n" +
			    "package p2;\n" +
			    "import p1.C1;\n" +
			    "public class C3 extends C1.C2 {\n" +
			    "    public String c = \"K\";\n" +
			    "    public String a = \"O\";\n" +
			    "    public static void main(String[] args) {\n" +
			    "        C3 c3 = new C3();\n" +
			    "        System.out.print(c3.a+c3.c);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"p1/C1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class C1 {\n" +
			    "    public static class C2 { }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // copy inheritance for role with broken playedBy etc.
    // B.1.1-otjld-sh-19
    public void testB11_sh19() {
        runNegativeTest(
            new String[] {
		"TeamB11sh19_2.java",
			    "\n" +
			    "public team class TeamB11sh19_2 extends TeamB11sh19_1 {\n" +
			    "    protected class Role playedBy TB11sh19 {\n" +
			    "        void bar() {}\n" +
			    "        bar <- after m;\n" +
			    "        void n() -> void n();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh19.java",
			    "\n" +
			    "public class TB11sh19 {\n" +
			    "    void m() {}\n" +
			    "    void n() {}\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh19_3.java",
			    "\n" +
			    "public team class TeamB11sh19_3 extends TeamB11sh19_2 {\n" +
			    "    protected class Role {\n" +
			    "        Role(TB11sh19 o) {\n" +
			    "            tsuper(o);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh19_1.java",
			    "\n" +
			    "public team class TeamB11sh19_1 {\n" +
			    "    protected class Role playedBy Inexistent {\n" +
			    "        void foo() {}\n" +
			    "        foo <- after what;\n" +
			    "    }\n" +
			    "    protected class R2 {\n" +
			    "        Inexistent ie = new Inexistent();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // from Dehla's error log: enum fields have no declared type
    // B.1.1-otjld-sh-20
    public void testB11_sh20() {
       
       runConformTest(
            new String[] {
		"TeamB11sh20.java",
			    "\n" +
			    "public team class TeamB11sh20 {\n" +
			    "    protected enum Weekday { MO, DI, MI } \n" +
			    "    void print(Weekday w) {\n" +
			    "        switch (w) {\n" +
			    "        case MO: System.out.print(\"MO\");break;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh20().print(Weekday.MO);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "MO");
    }

    // conflict between weakening of a team's field's type and interface implementation - occurred in hierarchy:ColumnTree.<init>(MyTreeNode)
    // B.1.1-otjld-sh-21
    public void testB11_sh21() {
       
       runConformTest(
            new String[] {
		"TeamB11sh21_2.java",
			    "\n" +
			    "public team class TeamB11sh21_2 extends TeamB11sh21_1 {\n" +
			    "    protected class R implements IB11sh21 playedBy TB11sh21 {\n" +
			    "        boolean check() { return true; }\n" +
			    "    }\n" +
			    "    protected class R2 { \n" +
			    "        protected R2(IB11sh21 checkable) {\n" +
			    "            System.out.print(\"check:\");\n" +
			    "            System.out.print((checkable.check()) ? \"OK\" : \"NOK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    TeamB11sh21_2(TB11sh21 as R r) {\n" +
			    "        super(r);\n" +
			    "        new R2(this.r);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh21_2(new TB11sh21());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh21_1.java",
			    "\n" +
			    "public team class TeamB11sh21_1 {\n" +
			    "    R r;\n" +
			    "    protected class R {}\n" +
			    "    TeamB11sh21_1(R r) {\n" +
			    "        this.r = r;\n" +
			    "        System.out.print(\"ctor1:\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IB11sh21.java",
			    "\n" +
			    "public interface IB11sh21 {\n" +
			    "    boolean check();\n" +
			    "}\n" +
			    "    \n",
		"TB11sh21.java",
			    "\n" +
			    "public class TB11sh21 {}\n" +
			    "    \n"
            },
            "ctor1:check:OK");
    }

    // conflict between weakening of a team's field's type and interface implementation - same as above but method instead of ctor
    // B.1.1-otjld-sh-22
    public void testB11_sh22() {
       
       runConformTest(
            new String[] {
		"TeamB11sh22_2.java",
			    "\n" +
			    "public team class TeamB11sh22_2 extends TeamB11sh22_1 {\n" +
			    "    protected class R implements IB11sh22 playedBy TB11sh22 {\n" +
			    "        boolean check() { return true; }\n" +
			    "    }\n" +
			    "    public void checkR(IB11sh22 checkable) {\n" +
			    "        System.out.print(\"check:\");\n" +
			    "        System.out.print((checkable.check()) ? \"OK\" : \"NOK\");\n" +
			    "    }\n" +
			    "    TeamB11sh22_2(TB11sh22 as R r) {\n" +
			    "        super(r);\n" +
			    "        checkR(this.r);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh22_2(new TB11sh22());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh22_1.java",
			    "\n" +
			    "public team class TeamB11sh22_1 {\n" +
			    "    R r;\n" +
			    "    protected class R {}\n" +
			    "    TeamB11sh22_1(R r) {\n" +
			    "        this.r = r;\n" +
			    "        System.out.print(\"ctor1:\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IB11sh22.java",
			    "\n" +
			    "public interface IB11sh22 {\n" +
			    "    boolean check();\n" +
			    "}\n" +
			    "    \n",
		"TB11sh22.java",
			    "\n" +
			    "public class TB11sh22 {}\n" +
			    "    \n"
            },
            "ctor1:check:OK");
    }

    // TPX-422
    // B.1.1-otjld-sh-23
    public void testB11_sh23() {
        runConformTest(
            new String[] {
		"TeamB11sh23.java",
			    "\n" +
			    "import base org.objectteams.TeamThreadManager;\n" +
			    "public team class TeamB11sh23 {\n" +
			    "    protected class R playedBy TeamThreadManager {\n" +
			    "        R(String[] args) {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            });
    }

    // occurred in Disposition on 07.07.2006
    // B.1.1-otjld-sh-24
    public void testB11_sh24() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh24.java",
			    "\n" +
			    "public team class TeamB11sh24 {\n" +
			    "    protected class R {\n" +
			    "        callin void m () {\n" +
			    "            return null;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "cannot return");
    }

    // reported by Bj�rn Lohrmann (OOSE group 06#G2)
    // B.1.1-otjld-sh-25
    public void testB11_sh25() {
        runTestExpectingWarnings(
            new String[] {
		"TeamB11sh25_2.java",
			    "\n" +
			    "public team class TeamB11sh25_2 {\n" +
			    "    public class R2 playedBy TeamB11sh25_1 {}\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh25_1.java",
			    "\n" +
			    "public team class TeamB11sh25_1 {\n" +
			    "    public class R1 playedBy TeamB11sh25_2 {\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. WARNING in TeamB11sh25_2.java (at line 3)\n" +
			"	public class R2 playedBy TeamB11sh25_1 {}\n" +
			"	                         ^^^^^^^^^^^^^\n" +
			"Base class/member type circularity via chain TeamB11sh25_2.R2->TeamB11sh25_1->TeamB11sh25_1.R1->TeamB11sh25_2;\n" +
			"please read the hints in the OT/J Language Definition (OTJLD 2.1.2(b)).\n" +
			"----------\n" +
			"----------\n" +
			"1. WARNING in TeamB11sh25_1.java (at line 3)\n" +
			"	public class R1 playedBy TeamB11sh25_2 {\n" +
			"	                         ^^^^^^^^^^^^^\n" +
			"Base class/member type circularity via chain TeamB11sh25_1.R1->TeamB11sh25_2->TeamB11sh25_2.R2->TeamB11sh25_1;\n" +
			"please read the hints in the OT/J Language Definition (OTJLD 2.1.2(b)).\n" +
			"----------\n");
    }

    // base predicate in team with syntax errors, TPX-448
    // B.1.1-otjld-sh-26
    public void testB11_sh26() {
        runNegativeTest(
            new String[] {
		"TeamB11sh26.java",
			    "\n" +
			    "public team class TeamB11sh26 {\n" +
			    "    protected class // unfinished\n" +
			    "    \n" +
			    "    protected class R playedBy TB11sh26 {\n" +
			    "        callin void foo() {}\n" +
			    "        void foo() <- replace String toUpperCase() \n" +
			    "            base when (\"a\".equals(\"b\"));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh26.java",
				"public class TB11sh26 {\n" +
				"    String toUpperCase() { return \"\"; }\n" +
				"}\n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh26.java (at line 3)\n" + 
    		"	protected class // unfinished\n" + 
    		"	          ^^^^^\n" + 
    		"Syntax error on token \"class\", callin expected\n" + 
    		"----------\n");
    }

    // predicate in team with syntax errors, similar to previous but not "base"
    // B.1.1-otjld-sh-27
    public void testB11_sh27() {
        runNegativeTest(
            new String[] {
		"TeamB11sh27.java",
			    "\n" +
			    "public team class TeamB11sh27 {\n" +
			    "    protected class // unfinished\n" +
			    "    \n" +
			    "    protected class R playedBy TB11sh27 {\n" +
			    "        callin void foo() {}\n" +
			    "        void foo() <- replace String toUpperCase() \n" +
			    "            when (\"a\".equals(\"b\"));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh27.java",
				"public class TB11sh27 {\n" +
				"    String toUpperCase() { return \"\"; }\n" +
				"}\n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh27.java (at line 3)\n" + 
    		"	protected class // unfinished\n" + 
    		"	          ^^^^^\n" + 
    		"Syntax error on token \"class\", callin expected\n" + 
    		"----------\n");
    }

    // TPX-451
    // B.1.1-otjld-sh-28
    public void testB11_sh28() {
        runConformTest(
            new String[] {
		"TeamB11sh28.java",
			    "\n" +
			    "public team class TeamB11sh28 {\n" +
			    "    public class R {}\n" +
			    "}    \n" +
			    "    \n",
		"TB11sh28.java",
			    "\n" +
			    "public class TB11sh28 {\n" +
			    "    final TeamB11sh28 _tm; \n" +
			    "    R<@_tm> _rl;\n" +
			    "    \n" +
			    "    TB11sh28(final TeamB11sh28 tm, R<@tm> rl) {\n" +
			    "        _tm = tm; \n" +
			    "        _rl = rl; \n" +
			    "     } \n" +
			    "}\n" +
			    "    \n"
            });
    }

    // A callin method has a name that is misinterpreted as a creator method - reported by kmeier on 07/12/06
    // B.1.1-otjld-sh-29
    public void testB11_sh29() {
       
       runConformTest(
            new String[] {
		"TeamB11sh29.java",
			    "\n" +
			    "public team class TeamB11sh29 {\n" +
			    "    protected class R playedBy TB11sh29 {\n" +
			    "        callin void createOutput () {\n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.createOutput();\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        createOutput <- replace test;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh29().activate();\n" +
			    "        new TB11sh29().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh29.java",
			    "\n" +
			    "public class TB11sh29 {\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // TPX-26, carp's comment 03/Aug/06
    // B.1.1-otjld-sh-30
    public void testB11_sh30() {
        runConformTest(
            new String[] {
		"TeamB11sh30_1.java",
			    "\n" +
			    "public team class TeamB11sh30_1 extends TeamB11sh30_2 {\n" +
			    "    protected class R1 {\n" +
			    "        int i;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh30_2.java",
			    "\n" +
			    "public team class TeamB11sh30_2 {\n" +
			    "    protected class R1 {\n" +
			    "        void foo() {}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            });
    }

    // TPX-458
    // B.1.1-otjld-sh-31
    public void testB11_sh31() {
       
       runConformTest(
            new String[] {
		"TeamB11sh31_3.java",
			    "\n" +
			    "public team class TeamB11sh31_3 extends TeamB11sh31_2 {\n" +
			    "    protected class R2 {\n" +
			    "        protected int getID() { return 7; }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh31_3();\n" +
			    "    }\n" +
			    "    TeamB11sh31_3() {\n" +
			    "        final TeamB11sh31_1 theTeam = new TeamB11sh31_1();\n" +
			    "        R<@theTeam> r = theTeam.getR(((R2)theRole).getID());\n" +
			    "        System.out.print(r.getV());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh31_1.java",
			    "\n" +
			    "public team class TeamB11sh31_1 {\n" +
			    "    public class R {\n" +
			    "        int val;\n" +
			    "        protected R(int i) {\n" +
			    "            this.val = i;\n" +
			    "        }\n" +
			    "        public int getV() { return val; }\n" +
			    "    }\n" +
			    "    public R getR(int v) {\n" +
			    "        return new R(v);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh31_2.java",
			    "\n" +
			    "public team class TeamB11sh31_2 {\n" +
			    "    R2 theRole = new R2();\n" +
			    "    protected class R2 {\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "7");
    }

    // The bug leading to the workaround leading to TPX-458
    // B.1.1-otjld-sh-32
    public void testB11_sh32() {
       
       runConformTest(
            new String[] {
		"TeamB11sh32_3.java",
			    "\n" +
			    "public team class TeamB11sh32_3 extends TeamB11sh32_2 {\n" +
			    "    protected class R2 {\n" +
			    "        public int getID() { return 7; }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh32_3();\n" +
			    "    }\n" +
			    "    TeamB11sh32_3() {\n" +
			    "        final TeamB11sh32_1 theTeam = new TeamB11sh32_1();\n" +
			    "        R<@theTeam> r = theTeam.getR(theRole.getID()); // no cast here, at some time caused a compile error?\n" +
			    "        System.out.print(r.getV());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh32_1.java",
			    "\n" +
			    "public team class TeamB11sh32_1 {\n" +
			    "    public class R {\n" +
			    "        int val;\n" +
			    "        protected R(int i) {\n" +
			    "            this.val = i;\n" +
			    "        }\n" +
			    "        public int getV() { return val; }\n" +
			    "    }\n" +
			    "    public R getR(int v) {\n" +
			    "        return new R(v);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh32_2.java",
			    "\n" +
			    "public team class TeamB11sh32_2 {\n" +
			    "    R2 theRole = new R2();\n" +
			    "    protected class R2 {\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "7");
    }


    // broken control flow in woven base, observed in CompletionAdaptor
    // B.1.1-otjld-sh-34
    public void testB11_sh34() {
       
       runConformTest(
            new String[] {
		"TeamB11sh34.java",
			    "\n" +
			    "public team class TeamB11sh34 {\n" +
			    "    protected class R playedBy TB11sh34 {\n" +
			    "        callin void realTest(int j) {\n" +
			    "            System.out.print(base.realTest(j));\n" +
			    "        }\n" +
			    "        realTest <- replace test;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) { TeamB11sh34 t = new TeamB11sh34();\n" +
			    "        t.activate();\n" +
			    "        new TB11sh34().test(3);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh34.java",
			    "\n" +
			    "public class TB11sh34 {\n" +
			    "    String test(int i) {\n" +
			    "        switch (i) {\n" +
			    "        case 1:\n" +
			    "        case 3:\n" +
			    "        case 5:\n" +
			    "        case 7:\n" +
			    "        case 15:\n" +
			    "        case 13:\n" +
			    "        case 6:\n" +
			    "            if (i == 3)\n" +
			    "                return \"OK\";\n" +
			    "            return \"NQOK\";\n" +
			    "        case 2:\n" +
			    "        case 18:\n" +
			    "        case 8:\n" +
			    "            return \"WRONG\";\n" +
			    "        default:\n" +
			    "            return \"NOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // broken anonymous class
    // B.1.1-otjld-sh-35
    public void testB11_sh35() {
        runNegativeTest(
            new String[] {
		"TeamB11sh35.java",
			    "\n" +
			    "public team class TeamB11sh35 {\n" +
			    "    protected class R playedBy TB11sh35 {\n" +
			    "        callin void bar() { \n" +
			    "            Object o = new Missing() {\n" +
			    "                void action() {\n" +
			    "                    new R(new TB11sh35());\n" +
			    "                }\n" +
			    "            };\n" +
			    "        }\n" +
			    "        bar <- replace foo;\n" +
			    "        int mumble() -> int mumble();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh35.java",
			    "\n" +
			    "public class TB11sh35 {\n" +
			    "        void foo() {}\n" +
			    "        int mumble() { return 32; }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh35.java (at line 5)\n" + 
    		"	Object o = new Missing() {\n" + 
    		"	               ^^^^^^^\n" + 
    		"Missing cannot be resolved to a type\n" + 
    		"----------\n");
    }

    // callin method after broken callout
    // B.1.1-otjld-sh-36
    public void testB11_sh36() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh36.java",
			    "\n" +
			    "public team class TeamB11sh36 {\n" +
			    "    protected class R playedBy Object {\n" +
			    "        toString =>  // incomplete\n" +
			    "        R() {}\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin void foo() { \n" +
			    "            // empty\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. WARNING in TeamB11sh36.java (at line 3)\n" + 
    		"	protected class R playedBy Object {\n" + 
    		"	                           ^^^^^^\n" + 
    		"Base class java.lang.Object appears to be a system class, which means that load time weaving could possibly fail\n" + 
    		"if this class is loaded from rt.jar/the bootstrap classpath.\n" + 
    		"----------\n" + 
    		"2. ERROR in TeamB11sh36.java (at line 4)\n" + 
    		"	toString =>  // incomplete\n" + 
    		"	         ^^\n" + 
    		"Syntax error on token \"=>\", delete this token\n" + 
    		"----------\n" + 
    		"3. ERROR in TeamB11sh36.java (at line 5)\n" + 
    		"	R() {}\n" + 
    		"	^\n" + 
    		"No method R found in type java.lang.Object to resolve method designator (OTJLD 3.1(c)).\n" + 
    		"----------\n");
    }

    // A lifted return of a callout is a role of a wrong team
    // B.1.1-otjld-sh-37
    public void testB11_sh37() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh37_2.java",
			    "\n" +
			    "import p1.TeamB11sh37_1;\n" +
			    "public team class TeamB11sh37_2 {\n" +
			    "    protected class R2 playedBy TB11sh37 {\n" +
			    "        TeamB11sh37_1.R1 getVal() -> get String val;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh37.java",
			    "\n" +
			    "public class TB11sh37 {\n" +
			    "    String val;\n" +
			    "}\n" +
			    "    \n",
		"p1/TeamB11sh37_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class TeamB11sh37_1 {\n" +
			    "    public class R1 playedBy String {}\n" +
			    "}\n" +
			    "    \n"
            },
            "1.2.2(b)");
    }

    // lowering-confusion reported by mehner in SportCourseManagement: tried to lower an exception, but only its ctor-arg should be lowered
    // B.1.1-otjld-sh-38
    public void testB11_sh38() {
       
       runConformTest(
            new String[] {
		"TeamB11sh38.java",
			    "\n" +
			    "public team class TeamB11sh38 {\n" +
			    "    protected class R playedBy B11sh38 {\n" +
			    "    }\n" +
			    "    TeamB11sh38 () {\n" +
			    "        R r = new R(new B11sh38());\n" +
			    "        try {\n" +
			    "            throw new EB11sh38(r); // lowers the argument\n" +
			    "        } catch (EB11sh38 ex) {\n" +
			    "            ex.print();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh38();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"B11sh38.java",
			    "\n" +
			    "public class B11sh38 {\n" +
			    "    public void print() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"EB11sh38.java",
			    "\n" +
			    "@SuppressWarnings(\"serial\")\n" +
			    "public class EB11sh38 extends Exception {\n" +
			    "    EB11sh38 (B11sh38 o) {\n" +
			    "        o.print();\n" +
			    "    }\n" +
			    "    void print() {\n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // copy inheritance for subclass of JFrame: coulnd't deal with type variables in signatures to copy
    // B.1.1-otjld-sh-39
    public void testB11_sh39() {
       
       runConformTest(
            new String[] {
		"TeamB11sh39_2.java",
			    "\n" +
			    "public team class TeamB11sh39_2 extends TeamB11sh39_1 {\n" +
			    "    @SuppressWarnings(\"serial\")\n" +
			    "    protected class R {\n" +
			    "        protected void print() {\n" +
			    "            tsuper.print();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh39_2().new R().print();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh39_1.java",
			    "\n" +
			    "import javax.swing.JFrame;\n" +
			    "public team class TeamB11sh39_1 {\n" +
			    "    @SuppressWarnings(\"serial\")\n" +
			    "    protected class R extends JFrame {\n" +
			    "        void print() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // multiple anonymous classes, implicitely inherited
    // B.1.1-otjld-sh-40
    public void testB11_sh40() {
       
       runConformTest(
            new String[] {
		"TeamB11sh40_2.java",
			    "\n" +
			    "public team class TeamB11sh40_2 extends TeamB11sh40_1 {\n" +
			    "    protected class R {\n" +
			    "        String getO() { return \"O\"; }\n" +
			    "        String getK() {\n" +
			    "            return new IB11sh40() {\n" +
			    "                public String getStr() { return \"K\"; }\n" +
			    "            }.getStr();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh40_2().new R().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IB11sh40.java",
			    "\n" +
			    "public interface IB11sh40 {\n" +
			    "    String getStr();\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh40_1.java",
			    "\n" +
			    "public team class TeamB11sh40_1 {\n" +
			    "    abstract protected class R {\n" +
			    "        protected void test() {\n" +
			    "            IB11sh40 i1 = new IB11sh40() {\n" +
			    "                public String getStr() { return getO(); }\n" +
			    "            };\n" +
			    "            IB11sh40 i2 = new IB11sh40() {\n" +
			    "                public String getStr() { return getK(); }\n" +
			    "            };\n" +
			    "            System.out.print(i1.getStr()+i2.getStr());\n" +
			    "        }\n" +
			    "        abstract String getO();\n" +
			    "        abstract String getK();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // multiple anonymous classes, implicitely inherited - reuse binary of local
    // B.1.1-otjld-sh-40f
    public void testB11_sh40f() {
       
       runConformTest(
            new String[] {
		"TeamB11sh40f_2.java",
			    "\n" +
			    "public team class TeamB11sh40f_2 extends TeamB11sh40f_1 {\n" +
			    "    protected class R {\n" +
			    "        String getO() { return \"O\"; }\n" +
			    "        String getK() {\n" +
			    "            return new IB11sh40f() {\n" +
			    "                public String getStr() { return \"K\"; }\n" +
			    "            }.getStr();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh40f_2().new R().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IB11sh40f.java",
			    "\n" +
			    "public interface IB11sh40f {\n" +
			    "    String getStr();\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh40f_1.java",
			    "\n" +
			    "public team class TeamB11sh40f_1 {\n" +
			    "    abstract protected class R {\n" +
			    "        protected void test() {\n" +
			    "            IB11sh40f i1 = new IB11sh40f() {\n" +
			    "                public String getStr() { return getO(); }\n" +
			    "            };\n" +
			    "            IB11sh40f i2 = new IB11sh40f() {\n" +
			    "                public String getStr() { return getK(); }\n" +
			    "            };\n" +
			    "            System.out.print(i1.getStr()+i2.getStr());\n" +
			    "        }\n" +
			    "        abstract String getO();\n" +
			    "        abstract String getK();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // reported by kmeier
    // B.1.1-otjld-sh-41
    public void testB11_sh41() {
       
       runConformTest(
            new String[] {
		"TeamB11sh41_3.java",
			    "\n" +
			    "public team class TeamB11sh41_3 {\n" +
			    "    final TeamB11sh41_1 t;\n" +
			    "\n" +
			    "    TeamB11sh41_3(TeamB11sh41_1 t) {\n" +
			    "        this.t = t;\n" +
			    "        activate();\n" +
			    "    }\n" +
			    "    protected class TheRole playedBy MyRole<@t> {\n" +
			    "        void afterTest() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        afterTest <- after test;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamB11sh41_2 target = new TeamB11sh41_2();\n" +
			    "        TeamB11sh41_3 thisTeam = new TeamB11sh41_3(target);\n" +
			    "        target.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh41_1.java",
			    "\n" +
			    "public team class TeamB11sh41_1 {\n" +
			    "    public class MyRole {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh41_2.java",
			    "\n" +
			    "public team class TeamB11sh41_2 extends TeamB11sh41_1 {\n" +
			    "    public void test() {\n" +
			    "        new MyRole().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // check added by resix
    // B.1.1-otjld-sh-41a
    public void testB11_sh41a() {
       
       runConformTest(
            new String[] {
		"TeamB11sh41a_3.java",
			    "\n" +
			    "public team class TeamB11sh41a_3 {\n" +
			    "    final TeamB11sh41a_1 t;\n" +
			    "\n" +
			    "    TeamB11sh41a_3(TeamB11sh41a_1 t) {\n" +
			    "        this.t = t;\n" +
			    "        activate();\n" +
			    "    }\n" +
			    "    protected class TheRole playedBy MyRole<@t> {\n" +
			    "        void afterTest() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        afterTest <- after test;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamB11sh41a_2 target = new TeamB11sh41a_2();\n" +
			    "        TeamB11sh41a_3 thisTeam = new TeamB11sh41a_3(target);\n" +
			    "        target.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh41a.java",
			    "\n" +
			    "public class TB11sh41a {}\n" +
			    "    \n",
		"TeamB11sh41a_1.java",
			    "\n" +
			    "public team class TeamB11sh41a_1 {\n" +
			    "    public class MyRole playedBy TB11sh41a {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh41a_2.java",
			    "\n" +
			    "public team class TeamB11sh41a_2 extends TeamB11sh41a_1 {\n" +
			    "    public void test() {\n" +
			    "        new MyRole(new TB11sh41a()).test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // reported by mehner in sport course management example
    // B.1.1-otjld-sh-42
    public void testB11_sh42() {
       Map customOptions = getCompilerOptions();
       
       runConformTest(
            new String[] {
		"TB11sh42Main.java",
			    "\n" +
			    "public class TB11sh42Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh42_2().activate();\n" +
			    "        TB11sh42_1 o1 = new TB11sh42_1();\n" +
			    "        TB11sh42_2 o2 = new TB11sh42_2();\n" +
			    "        o2.test(o1);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh42_1.java",
			    "\n" +
			    "public class TB11sh42_1 {\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh42_2.java",
			    "\n" +
			    "public class TB11sh42_2 {\n" +
			    "    void test(TB11sh42_1 other) {\n" +
			    "        other.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh42_1.java",
			    "\n" +
			    "public team class TeamB11sh42_1 {\n" +
			    "    TeamB11sh42_1() {\n" +
			    "        this.activate();\n" +
			    "    }\n" +
			    "    public class R1 playedBy TB11sh42_1 {\n" +
			    "    }\n" +
			    "    protected class R2 playedBy TB11sh42_2 {\n" +
			    "        callin void doA(R1 other) {\n" +
			    "            System.out.print(\">\");\n" +
			    "            base.doA(other);\n" +
			    "        }\n" +
			    "        doA <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh42_2.java",
			    "\n" +
			    "public team class TeamB11sh42_2 {\n" +
			    "    final TeamB11sh42_1 that = new TeamB11sh42_1();\n" +
			    "    @SuppressWarnings(\"decapsulation\") // baseclass and callin-to-callin\n" +
			    "    protected class R playedBy R2<@that> {\n" +
			    "        callin void doB(R1<@that> other) {\n" +
			    "            base.doB(other);\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        doB <- replace doA;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            ">OK!",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // saw an NPE while editing , fix is in rev 14985
    // B.1.1-otjld-sh-43
    public void testB11_sh43() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh43.java",
			    "\n" +
			    "public team class TeamB11sh43 {\n" +
			    "    protected class R playedBy Wrong { }\n" +
			    "    public TeamB11sh43 (String as R arg) {\n" +
			    "        System.out.print(arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "cannot be resolved");
    }

    // nested role shadows outer role - this rule is difficult to enforce since Confined et al actually violate it in every nested team
    // B.1.1-otjld_sh-44
    public void _sh_testB11_44() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh44.java",
			    "\n" +
			    "public team class TeamB11sh44 {\n" +
			    "    protected team class R1 {\n" +
			    "        protected class R11 {}\n" +
			    "        protected class R12 extends R11 {}\n" +
			    "    }\n" +
			    "    protected team class R2 extends R1 {\n" +
			    "        protected class R1 {}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1.4(a)");
    }

    // role shadows imported type
    // B.1.1-otjld-sh-44a
    public void testB11_sh44a() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh44a.java",
			    "\n" +
			    "import p1.TB11sh44a;\n" +
			    "public team class TeamB11sh44a {\n" +
			    "    protected class TB11sh44a {\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/TB11sh44a.java",
			    "\n" +
			    "package p1;\n" +
			    "public class TB11sh44a {\n" +
			    "}\n" +
			    "    \n"
            },
            "1.4(a)");
    }

    // conflicting package and field
    // B.1.1-otjld-sh-45
    public void testB11_sh45() {
       
       runConformTest(
            new String[] {
		"p2/TeamB11sh45_2.java",
			    "\n" +
			    "package p2;\n" +
			    "import p1.TeamB11sh45_1;\n" +
			    "public team class TeamB11sh45_2 {\n" +
			    "    final TeamB11sh45_1 p1;\n" +
			    "    @SuppressWarnings(\"bindingconventions\")\n" +
			    "    protected class R2 playedBy TeamB11sh45_1 {}\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh45_2();\n" +
			    "    }\n" +
			    "    TeamB11sh45_2() {\n" +
			    "        p1 = new TeamB11sh45_1();\n" +
			    "        R<@p1> r = p1.new R();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/TB11sh45.java",
			    "\n" +
			    "package p1;\n" +
			    "public class TB11sh45 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/TeamB11sh45_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class TeamB11sh45_1 {\n" +
			    "    public class R playedBy TB11sh45 {\n" +
			    "        public R () {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "        public void test() {\n" +
			    "            testBase();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void testBase() -> void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // load order problem with callout to field : ReferenceTeams forces to load TeamB11sh46_1 before TeamB11sh46_2 can be consulted regarding required decapsulation; occurred in OTPong
    // B.1.1-otjld-sh-46
    public void testB11_sh46() {
       myWriteFiles(
    		new String[] {
		"B11sh46teamconfig.txt",
			    "\n" +
			    "TeamB11sh46_2\n" +
			    "    \n"
    		});
       Map customOptions = getCompilerOptions();
       runConformTest(
            new String[] {
		"TB11sh46Main.java",
			    "\n" +
			    "public class TB11sh46Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh46_1().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh46_1.java",
			    "\n" +
			    "public team class TeamB11sh46_1 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private char getValue() { return 'K'; }\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh46_2.java",
			    "\n" +
			    "public team class TeamB11sh46_2 {\n" +
			    "    public TeamB11sh46_2() {\n" +
			    "        this.activate();\n" +
			    "    }\n" +
			    "    protected class R playedBy TeamB11sh46_1 {\n" +
			    "        void test() {\n" +
			    "            System.out.print(getValue());\n" +
			    "        }\n" +
			    "        test <- after test;\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        char getValue() -> char getValue();\n" +
			    "        void test() <- after char getValue() when (!isExecutingCallin());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            getTeamActivationVMArgs("B11sh46teamconfig.txt")/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // while preparing the previous test: guard was called with wrong parameters
    // B.1.1-otjld-sh-46f
    public void testB11_sh46f() {
        myWriteFiles(
        		new String[] {
    		"B11sh46fteamconfig.txt",
    			    "\n" +
    			    "TeamB11sh46f\n" +
    			    "    \n"
        		});
       Map customOptions = getCompilerOptions();
       runConformTest(
            new String[] {
		"TB11sh46fMain.java",
			    "\n" +
			    "public class TB11sh46fMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TB11sh46f().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh46f.java",
			    "\n" +
			    "public class TB11sh46f {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private char getValue() { return 'K'; }\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh46f.java",
			    "\n" +
			    "public team class TeamB11sh46f {\n" +
			    "    public TeamB11sh46f() {\n" +
			    "        this.activate();\n" +
			    "    }\n" +
			    "    protected class R playedBy TB11sh46f {\n" +
			    "        void test() {\n" +
			    "            System.out.print(getValue());\n" +
			    "        }\n" +
			    "        test <- after test;\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        char getValue() -> char getValue();\n" +
			    "        test <- after getValue when (!isExecutingCallin());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            getTeamActivationVMArgs("B11sh46fteamconfig.txt")/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // syntax error and local type in a role method - (note that exception was observed during UI operations only: reconsile or hovering the local type)
    // B.1.1-otjld-sh-47
    public void testB11_sh47() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
        runNegativeTest(
            new String[] {
		"TeamB11sh47.java",
			    "\n" +
			    "import base javax.swing.JFrame;\n" +
			    "import base java.lang.String;\n" +
			    "@SuppressWarnings(\"bindingtosystemclass\")\n" +
			    "public team class TeamB11sh47 {\n" +
			    "    protected class R1 playedBy JFrame {\n" +
			    "        callin void correct(int i) {\n" +
			    "            System.out.print(i);\n" +
			    "            base.correct(i);\n" +
			    "        }\n" +
			    "        correct <- replace show;\n" +
			    "        void unshow() -> void hide();\n" +
			    "    }\n" +
			    "    protected class Unfinished playedBy\n" +
			    "    protected class R3 playedBy String {\n" +
			    "        callin void shouldBeOK() {\n" +
			    "            class Runner implements Runnable {\n" +
			    "                public void run() {\n" +
			    "                    System.out.print(\"running\");\n" +
			    "                }\n" +
			    "            }\n" +
			    "            new Thread(new Runner()).run();\n" +
			    "        }\n" +
			    "        shouldBeOK <- replace length;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh47.java (at line 11)\n" + 
    		"	correct <- replace show;\n" + 
    		"	                   ^^^^\n" + 
    		"Method specifier \"show\" is ambiguous for the type javax.swing.JFrame. Please use the exact method signature to disambiguate (OTJLD 4.1(c)).\n" + 
    		"----------\n" + 
    		"2. ERROR in TeamB11sh47.java (at line 14)\n" + 
    		"	protected class Unfinished playedBy\n" + 
    		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
    		"Syntax error on token(s), misplaced construct(s)\n" + 
    		"----------\n" + 
    		"3. ERROR in TeamB11sh47.java (at line 15)\n" + 
    		"	protected class R3 playedBy String {\n" + 
    		"	                ^^\n" + 
    		"Member types not allowed in regular roles. Mark class TeamB11sh47.Unfinished as a team if R3 should be its role (OTJLD 1.5(a,b)). \n" + 
    		"----------\n"+ 
    		"4. WARNING in TeamB11sh47.java (at line 15)\n" + 
    		"	protected class R3 playedBy String {\n" + 
    		"	                            ^^^^^^\n" + 
    		"PlayedBy binding overrides finalness of base class java.lang.String (OTJLD 2.1.2(c)).\n" + 
    		"----------\n",
    		null/*classLibraries*/,
    		true/*shouldFlushOutputDirectory*/,
    		customOptions);
    }

    // team extends missing super team, stacked teams
    // B.1.1-otjld-sh-48
    public void testB11_sh48() {
        runNegativeTest(
            new String[] {
		"TeamB11sh48_2.java",
			    "\n" +
			    "public team class TeamB11sh48_2 {\n" +
			    "    protected team class Mid playedBy TeamB11sh48_1 {\n" +
			    "        protected class R2 playedBy R1<@base> {\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh48_1.java",
			    "\n" +
			    "import base java.util.Arrays;\n" +
			    "public team class TeamB11sh48_1 extends MissingTeam {\n" +
			    "    @SuppressWarnings(\"bindingtosystemclass\")\n" +
			    "    protected class R1 playedBy Arrays {\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. WARNING in TeamB11sh48_2.java (at line 4)\n" + 
    		"	protected class R2 playedBy R1<@base> {\n" + 
    		"	                            ^^\n" + 
    		"Overriding access restriction of base class R1<@base> (OTJLD 2.1.2(c)).\n" + 
    		"----------\n" + 
    		"----------\n" + 
    		"1. ERROR in TeamB11sh48_1.java (at line 3)\n" + 
    		"	public team class TeamB11sh48_1 extends MissingTeam {\n" + 
    		"	                                        ^^^^^^^^^^^\n" + 
    		"MissingTeam cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"2. ERROR in TeamB11sh48_1.java (at line 5)\n" + 
    		"	protected class R1 playedBy Arrays {\n" + 
    		"	                ^^\n" + 
    		"The hierarchy of the type R1 is inconsistent\n" + 
    		"----------\n");
    }

    // cast method of buggy binary team requested - wittness for "Required cast method not found in Team."
    // B.1.1-otjld-sh-49
    public void testB11_sh49() {
        runNegativeTest(
            new String[] {
		"TeamB11sh49.java",
			    "\n" +
			    "public team class TeamB11sh49 {\n" +
			    "    public class R {\n" +
			    "        void getString() -> toString;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"},
			    "----------\n" + 
				"1. ERROR in TeamB11sh49.java (at line 4)\n" + 
				"	void getString() -> toString;\n" + 
				"	     ^^^^^^^^^\n" + 
				"Method binding not allowed here, role R is not played by a base class (OTJLD 3.1(a)).\n" + 
				"----------\n" + 
				"2. ERROR in TeamB11sh49.java (at line 4)\n" + 
				"	void getString() -> toString;\n" + 
				"	                    ^^^^^^^^\n" + 
				"Syntax error on token \"toString\", invalid CallloutFieldSpec\n" + 
				"----------\n",
				null/*classLibraries*/,
				false/*shouldFlushOutputDirectory*/,
				null/*customOptions*/,
				true/*generateOutput*/,
				false/*showCategory*/,
				false/*showWarningToken*/);
        runConformTest(
                new String[] {
		"TB11sh49Main.java",
			    "\n" +
			    "public class TB11sh49Main {\n" +
			    "    void foo() {\n" +
			    "        final TeamB11sh49 t = new TeamB11sh49();\n" +
			    "        Object o = t.new R();\n" +
			    "        R<@t> r = (R<@t>)o;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            ""/*expectedOutputString*/,
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/);
    }

    // a role field access to a broken role - saw ICE("not exactly one getTeam method")
    // B.1.1-otjld-sh-50
    public void testB11_sh50() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh50.java",
			    "\n" +
			    "public team class TeamB11sh50 {\n" +
			    "    protected class R1 playedBy Missing {\n" +
			    "        public String val;\n" +
			    "    }\n" +
			    "    protected class R2 {\n" +
			    "        void foo(R1 other) {\n" +
			    "            other.val = \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh50.java (at line 3)\n" + 
    		"	protected class R1 playedBy Missing {\n" + 
    		"	                            ^^^^^^^\n" + 
    		"Missing cannot be resolved to a type\n" + 
    		"----------\n");
    }

    // curiosities if bound base method is covariantly redefined, reported by hsudof, base version: OK
    // B.1.1-otjld-sh-51
    public void testB11_sh51() {
       
       runConformTest(
            new String[] {
		"TB11sh51_1.java",
			    "\n" +
			    "public abstract class TB11sh51_1 {\n" +
			    "    abstract Object test();\n" +
			    "\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamB11sh51_1 tt = new TeamB11sh51_1();\n" +
			    "        tt.activate();\n" +
			    "        TB11sh51_2 t = new TB11sh51_2();\n" +
			    "        tt.m(t);\n" +
			    "        System.out.print(t.test());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh51_1.java",
			    "\n" +
			    "public team class TeamB11sh51_1 {\n" +
			    "    void m(TB11sh51_1 as Role r) {\n" +
			    "        System.out.print(\"DeclaredLifting \");\n" +
			    "    }\n" +
			    "    public class Role playedBy TB11sh51_1 {\n" +
			    "        void sayHi() {\n" +
			    "            System.out.print(\"before \");\n" +
			    "        }\n" +
			    "        // should not be called because binding to TB11sh51_2 is not type-safe:\n" +
			    "        callin Object test() { \n" +
			    "            System.out.print(\"replace \"); \n" +
			    "            base.test();\n" +
			    "            return  new Object() { public String toString() {return \"AnonObject\"; } };\n" +
			    "        }\n" +
			    "        test <- replace test; // implicitly locks the binding to \"Object test()\"\n" +
			    "        sayHi <- before test; // same as above\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"TB11sh51_2.java",
			    "\n" +
			    "public class TB11sh51_2 extends TB11sh51_1 {\n" +
			    "    @Override\n" +
			    "    public String test() {\n" +
			    "        return \"String\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "DeclaredLifting String");
    }

    // curiosities if bound base method is covariantly redefined, reported by hsudof, bound with "+", replace callin not compilable
    // B.1.1-otjld-sh-51p
    public void testB11_sh51p() {
        runNegativeTestMatching(
            new String[] {
		"TB11sh51p_1.java",
			    "\n" +
			    "public abstract class TB11sh51p_1 {\n" +
			    "    abstract Object test();\n" +
			    "\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamB11sh51p_1 tt = new TeamB11sh51p_1();\n" +
			    "        tt.activate();\n" +
			    "        TB11sh51p_2 t = new TB11sh51p_2();\n" +
			    "        tt.m(t);\n" +
			    "        System.out.print(t.test());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh51p_1.java",
			    "\n" +
			    "public team class TeamB11sh51p_1 {\n" +
			    "    void m(TB11sh51p_1 as Role r) {\n" +
			    "        System.out.print(\"DeclaredLifting \");\n" +
			    "    }\n" +
			    "    public class Role playedBy TB11sh51p_1 {\n" +
			    "        void sayHi() {\n" +
			    "            System.out.print(\"before \");\n" +
			    "        }\n" +
			    "        callin Object test() { \n" +
			    "            System.out.print(\"replace \"); \n" +
			    "            base.test();\n" +
			    "            return  new Object() { public String toString() {return \"AnonObject\"; } };\n" +
			    "        }\n" +
			    "        // incompatible: Object+ must be mapped to a type variable:\n" +
			    "        Object test() <- replace Object+ test();\n" +
			    "        void sayHi() <- before Object+ test();\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"TB11sh51p_2.java",
			    "\n" +
			    "public class TB11sh51p_2 extends TB11sh51p_1 {\n" +
			    "    @Override\n" +
			    "    public String test() {\n" +
			    "        return \"String\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.9.3(c)");
    }

    // curiosities if bound base method is covariantly redefined, reported by hsudof, static type changed the behaviour
    // B.1.1-otjld-sh-52
    public void testB11_sh52() {
       
       runConformTest(
            new String[] {
		"TB11sh52_1.java",
			    "\n" +
			    "public abstract class TB11sh52_1 {\n" +
			    "    abstract Object test();\n" +
			    "\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamB11sh52_1 tt = new TeamB11sh52_1();\n" +
			    "        tt.activate();\n" +
			    "        // using the super class, a generated method is called\n" +
			    "        // that is not overridden by TB11sh52_2.test().\n" +
			    "        TB11sh52_1 t = new TB11sh52_2();\n" +
			    "        tt.m(t);\n" +
			    "        System.out.print(t.test());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh52_1.java",
			    "\n" +
			    "public team class TeamB11sh52_1 {\n" +
			    "    void m(TB11sh52_1 as Role r) {\n" +
			    "        System.out.print(\"DeclaredLifting \");\n" +
			    "    }\n" +
			    "    public class Role playedBy TB11sh52_1 {\n" +
			    "        void sayHi() {\n" +
			    "            System.out.print(\"before \");\n" +
			    "        }\n" +
			    "        // should not be called because binding to TB11sh52_2 is not type-safe:\n" +
			    "        callin Object test() {\n" +
			    "            System.out.print(\"replace \");\n" +
			    "            base.test();\n" +
			    "            return  new Object() { public String toString() {return \"AnonObject\"; } };\n" +
			    "        }\n" +
			    "        test <- replace test;\n" +
			    "        sayHi <- before test;\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"TB11sh52_2.java",
			    "\n" +
			    "public class TB11sh52_2 extends TB11sh52_1 {\n" +
			    "    @Override\n" +
			    "    public String test() {\n" +
			    "        return \"String\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "DeclaredLifting String");
    }

    // curiosities if bound base method is covariantly redefined, reported by hsudof, referencing sub team changed behavior
    // B.1.1-otjld-sh-53
    public void testB11_sh53() {
       
       runConformTest(
            new String[] {
		"TB11sh53_1.java",
			    "\n" +
			    "public abstract class TB11sh53_1 {\n" +
			    "    abstract Object test();\n" +
			    "\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamB11sh53_1 tt = new TeamB11sh53_2();\n" +
			    "        tt.activate();\n" +
			    "        // Just referencing this team instance previously broke even the before binding\n" +
			    "        TeamB11sh53_2 tt2 = null;\n" +
			    "        TB11sh53_1 t = new TB11sh53_2();\n" +
			    "        tt.m(t);\n" +
			    "        System.out.print(t.test());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh53_2.java",
			    "\n" +
			    "public class TB11sh53_2 extends TB11sh53_1 {\n" +
			    "    @Override\n" +
			    "    public String test() {\n" +
			    "        return \"String\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh53_1.java",
			    "\n" +
			    "public team class TeamB11sh53_1 {\n" +
			    "    void m(TB11sh53_1 as Role r) {\n" +
			    "        System.out.print(\"DeclaredLifting \");\n" +
			    "    }\n" +
			    "    public class Role playedBy TB11sh53_1 {\n" +
			    "        void sayHi() {\n" +
			    "            System.out.print(\"before \");\n" +
			    "        }\n" +
			    "        // should not be called because binding to TB11sh53_2 is not type-safe:\n" +
			    "        callin Object test() {\n" +
			    "            System.out.print(\"replace \");\n" +
			    "            base.test();\n" +
			    "            return  new Object() { @Override public String toString() {return \"AnonObject\"; } };\n" +
			    "        }\n" +
			    "        test <- replace test;\n" +
			    "        void sayHi() <- before Object+ test(); // can capture covariance with no harm\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh53_2.java",
			    "\n" +
			    "public team class TeamB11sh53_2 extends TeamB11sh53_1 {\n" +
			    "    protected class R playedBy TB11sh53_2 {\n" +
			    "        // should be called, because this binding matches the precise base type:\n" +
			    "        callin String test() {\n" +
			    "            return base.test() + \">SubTeam\";\n" +
			    "        }\n" +
			    "        test <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "DeclaredLifting before String>SubTeam");
    }

    // syntax error in plain java should not trigger OT error message - witness for Trac ticket 28
    // B.1.1-otjld-sh-54
    public void testB11_sh54() {
        runNegativeTestMatching(
            new String[] {
		"TB11sh54.java",
			    "\n" +
			    "public class TB11sh54 {\n" +
			    "    void foo() {\n" +
			    "        String s = new // unfinished\n" +
			    "        s.substring(1,2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "resolved to a type");
    }

    // similar to previous but indeed an OT-specific problem
    // B.1.1-otjld-sh-55
    public void testB11_sh55() {
        runNegativeTestMatching(
            new String[] {
		"TB11sh55.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class TB11sh55 {\n" +
			    "    final Team t= new Team();\n" +
			    "    void foo() {\n" +
			    "        TB11sh55 me= this;\n" +
			    "        String s = new // need to analyze up-to the end, error is on 1st segment though:\n" +
			    "        me.t.ILowerable();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1.2.2(c)");
    }

    // very similar to previous but again not an OT-specific problem
    // B.1.1-otjld-sh-56
    public void testB11_sh56() {
        runNegativeTestMatching(
            new String[] {
		"TB11sh56.java",
			    "\n" +
			    "public class TB11sh56 {\n" +
			    "    final TB11sh56 t= new TB11sh56();\n" +
			    "    void foo() {\n" +
			    "        TB11sh56 me= this;\n" +
			    "        String s = new // cannot interpret as RoleType since last segment is not a type!\n" +
			    "        me.t.foo();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "resolved to a type");
    }

    // very similar to previous but again an OT-specific problem
    // B.1.1-otjld-sh-57
    public void testB11_sh57() {
        runNegativeTestMatching(
            new String[] {
		"TB11sh57.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class TB11sh57 {\n" +
			    "    class Inner {}\n" +
			    "    final TB11sh57 t= new TB11sh57();\n" +
			    "    void foo() {\n" +
			    "        TB11sh57 me= this;\n" +
			    "        String s = new // last element is a type, but anchor is not a team\n" +
			    "        me.t.Inner();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1.2.2(b)");
    }

    // partial recompile after removing bound base methods throws ICE (see Trac #89)
    // B.1.1-otjld-sh-58
    public void testB11_sh58() {
        runConformTest(
            new String[] {
		"TeamB11sh58_1.java",
			    "\n" +
			    "public team class TeamB11sh58_1 {\n" +
			    "    protected class R playedBy TB11sh58 {\n" +
			    "        void bar() {}\n" +
			    "        bar <- after foo;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh58.java",
			    "\n" +
			    "public class TB11sh58 { \n" +
			    "	public void foo () {}\n" +
			    "}\n" +
			    "    \n"});
        runConformTest(
        	new String[] {
		"TB11sh58.java",
			    "\n" +
			    "public class TB11sh58 { }\n" +
			    "    \n",
		"TeamB11sh58_2.java",
			    "\n" +
			    "public team class TeamB11sh58_2 extends TeamB11sh58_1 {}\n" +
			    "    \n"
            },
            null/*expectedOutput*/,
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/);
    }

    // generated methods should not complain about missing @Override - reported by Jan Marc Hoffmann
    // B.1.1-otjld-sh-59
    public void testB11_sh59() {
        runConformTest(
            new String[] {
		"TeamB11sh59.java",
			    "\n" +
			    "public team class TeamB11sh59 {}\n" +
			    "  \n"
            });
    }

    // constructor in a team with missing super class - after an example by Miguel Monteiro (errors incurred by missing super team)
    // B.1.1-otjld-sh-60
    public void testB11_sh60() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh60.java",
			    " // FIXME: -referenceInfo\n" +
			    "public team class TeamB11sh60 extends MissingTeam {\n" +
			    "    public class Role extends MissingSuperRole playedBy TB11sh60 { }\n" +
			    "    // implicit constructor\n" +
			    "}\n" +
			    "    \n",
		"TB11sh60.java",
			    "\n" +
			    "public class TB11sh60 {}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh60.java (at line 2)\n" + 
    		"	public team class TeamB11sh60 extends MissingTeam {\n" + 
    		"	                                      ^^^^^^^^^^^\n" + 
    		"MissingTeam cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"2. ERROR in TeamB11sh60.java (at line 3)\n" + 
    		"	public class Role extends MissingSuperRole playedBy TB11sh60 { }\n" + 
    		"	             ^^^^\n" + 
    		"The hierarchy of the type Role is inconsistent\n" + 
    		"----------\n" + 
    		"3. ERROR in TeamB11sh60.java (at line 3)\n" + 
    		"	public class Role extends MissingSuperRole playedBy TB11sh60 { }\n" + 
    		"	                          ^^^^^^^^^^^^^^^^\n" + 
    		"MissingSuperRole cannot be resolved to a type\n" + 
    		"----------\n");
    }

    // witness for problem with precedence in deeply nested team
    // B.1.1-otjld-sh-61
    public void testB11_sh61() {
        runNegativeTest(
            new String[] {
		"TeamB11sh61.java",
			    "\n" +
			    "public team class TeamB11sh61 {\n" +
			    "        protected team class Level1 playedBy TB11sh61\n" +
			    "        {\n" +
			    "                protected Level1(TB11sh61 e) { System.out.print(\"[I]\"); }\n" +
			    "                init <- after init;\n" +
			    "                void init() {\n" +
			    "                }\n" +
			    "                protected class Closed playedBy TB11sh61\n" +
			    "                                base when (Level1.this.hasRole(base, Closed.class))\n" +
			    "                {\n" +
			    "                        protected Closed(TB11sh61 e) { System.out.print(\"[Clo]\"); }\n" +
			    "                }\n" +
			    "                protected team class Dirty playedBy TB11sh61\n" +
			    "                                base when (Level1.this.hasRole(base, Dirty.class))\n" +
			    "                {\n" +
			    "                        protected Dirty(TB11sh61 e) { System.out.println(\"[D]\"); }\n" +
			    "\n" +
			    "                        askClose <- replace close;\n" +
			    "                        callin void askClose() {\n" +
			    "                        }\n" +
			    "\n" +
			    "                        save <- after save;\n" +
			    "                        @SuppressWarnings(\"roleinstantiation\") void save() {\n" +
			    "                                unregisterRole(this, Dirty.class);\n" +
			    "                                new Clean(this);\n" +
			    "                        }\n" +
			    "                        protected class ClosePending playedBy TB11sh61 {\n" +
			    "                                Runnable closer;\n" +
			    "                                ClosePending(TB11sh61 ed, Runnable closer) {\n" +
			    "                                        this(ed);\n" +
			    "                                        this.closer = closer;\n" +
			    "                                }\n" +
			    "                                doClose <- replace close;\n" +
			    "                                callin void doClose() {\n" +
			    "                                        closer.run();\n" +
			    "                                }\n" +
			    "                        }\n" +
			    "                }\n" +
			    "                protected class Clean playedBy TB11sh61\n" +
			    "                                base when (Level1.this.hasRole(base, Clean.class))\n" +
			    "                {\n" +
			    "                        protected Clean(TB11sh61 e) { System.out.println(\"[Cle]\"); }\n" +
			    "                }\n" +
			    "\n" +
			    "                boolean getAnswer() {\n" +
			    "                        return false;\n" +
			    "                }\n" +
			    "                \n" +
			    "                precedence Clean, Closed;\n" +
			    "                precedence Dirty, ClosePending;\n" +
			    "        }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh61.java",
			    "\n" +
			    "public class TB11sh61 {\n" +
			    "    void init() { System.out.print(\"i\"); }\n" +
			    "    void open() { System.out.print(\"o\"); }\n" +
			    "    void edit() { System.out.print(\"e\"); }\n" +
			    "    void save() { System.out.print(\"s\"); }\n" +
			    "    void close() { System.out.print(\"c\"); }\n" +
			    "}\n" +
			    "    \n"
            },
    		"----------\n" + 
    		"1. ERROR in TeamB11sh61.java (at line 51)\n" + 
    		"	precedence Dirty, ClosePending;\n" + 
    		"	                  ^^^^^^^^^^^^\n" + 
    		"Callin binding ClosePending not found in type TeamB11sh61.Level1 (OTJLD 4.8(b)).\n" + 
    		"----------\n");
    }

    // witness for NPE in CallinBindingManager
    // B.1.1-otjld-sh-62
    public void testB11_sh62() {
       
       runConformTest(
            new String[] {
		"TeamB11sh62.java",
			    "\n" +
			    "@SuppressWarnings(\"roleinstantiation\")\n" +
			    "public team class TeamB11sh62 {\n" +
			    "        protected team class Level1 playedBy TB11sh62\n" +
			    "        {\n" +
			    "                protected Level1(TB11sh62 e) { System.out.print(\"[I]\"); }\n" +
			    "\n" +
			    "                // initial transition: after init go to state Closed.\n" +
			    "                init <- after init;\n" +
			    "                void init() {\n" +
			    "                        // switch activation from outer team to this team:\n" +
			    "                        TeamB11sh62.this.deactivate();\n" +
			    "                        this.activate();\n" +
			    "                        // next state:\n" +
			    "                        new Closed(this);\n" +
			    "                }\n" +
			    "\n" +
			    "                /** State Closed, accepts event open, rejects event close (warning). */\n" +
			    "                protected class Closed playedBy TB11sh62\n" +
			    "                                base when (Level1.this.hasRole(base, Closed.class))\n" +
			    "                {\n" +
			    "                        protected Closed(TB11sh62 e) { System.out.print(\"[Clo]\"); }\n" +
			    "\n" +
			    "                        open <- after open;\n" +
			    "                        void open() {\n" +
			    "                                unregisterRole(this, Closed.class);\n" +
			    "                                new Clean(this);\n" +
			    "                        }\n" +
			    "\n" +
			    "                        warn <- after close;\n" +
			    "                        void warn() {\n" +
			    "                                System.out.print(\"[!c]\");\n" +
			    "                        }\n" +
			    "                }\n" +
			    "\n" +
			    "                /** State Dirty, accepts events close (conditionally) and save. */\n" +
			    "                @SuppressWarnings(\"basecall\")\n" +
			    "                protected team class Dirty playedBy TB11sh62\n" +
			    "                                base when (Level1.this.hasRole(base, Dirty.class))\n" +
			    "                {\n" +
			    "                        protected Dirty(TB11sh62 e) { System.out.print(\"[D]\"); }\n" +
			    "\n" +
			    "                        ask:\n" +
			    "                        askClose <- replace close;\n" +
			    "                        callin void askClose() {\n" +
			    "                                if (getAnswer()) {\n" +
			    "                                        base.askClose();\n" +
			    "                                        Level1.this.unregisterRole(this, Dirty.class);\n" +
			    "                                        new Closed(this);\n" +
			    "                                } else {\n" +
			    "                                        System.out.print(\"[!c2]\");\n" +
			    "                                }\n" +
			    "                        }\n" +
			    "\n" +
			    "                        save <- after save;\n" +
			    "                        void save() {\n" +
			    "                                Level1.this.unregisterRole(this, Dirty.class);\n" +
			    "                                new Clean(this);\n" +
			    "                        }\n" +
			    "                        protected class ClosePending playedBy TB11sh62 {\n" +
			    "                                Runnable closer;\n" +
			    "                                ClosePending(TB11sh62 ed, Runnable closer) {\n" +
			    "                                        this(ed);\n" +
			    "                                        this.closer = closer;\n" +
			    "                                }\n" +
			    "                                really:\n" +
			    "                                doClose <- replace close;\n" +
			    "                                callin void doClose() {\n" +
			    "                                        closer.run();\n" +
			    "                                }\n" +
			    "                        }\n" +
			    "                        precedence ask, ClosePending.really;\n" +
			    "                }\n" +
			    "\n" +
			    "                /** State Clean, accepts events edit and close, rejects event init (blocked). */\n" +
			    "                protected class Clean playedBy TB11sh62\n" +
			    "                                base when (Level1.this.hasRole(base, Clean.class))\n" +
			    "                {\n" +
			    "                        protected Clean(TB11sh62 e) { System.out.print(\"[Cle]\"); }\n" +
			    "\n" +
			    "                        edit <- after edit;\n" +
			    "                        void edit() {\n" +
			    "                                unregisterRole(this, Clean.class);\n" +
			    "                                new Dirty(this);\n" +
			    "                        }\n" +
			    "\n" +
			    "                        close <- after close;\n" +
			    "                        void close() {\n" +
			    "                                unregisterRole(this, Clean.class);\n" +
			    "                                new Closed(this);\n" +
			    "                        }\n" +
			    "\n" +
			    "                        nop <- replace init;\n" +
			    "                        @SuppressWarnings(\"basecall\")\n" +
			    "                        callin void nop()  {\n" +
			    "                                System.out.print(\"[!i]\");\n" +
			    "                        }\n" +
			    "                }\n" +
			    "\n" +
			    "                boolean getAnswer() {\n" +
			    "                        return false;\n" +
			    "                }\n" +
			    "\n" +
			    "                // class Clean and Closed both intercept 'close' => need to define precedence.\n" +
			    "                // Note: during transition Clean-close->Closed we don't want both roles to fire," +
			    "				 //       by giving Clean higher precedence its after-callin will trigger after Closed has checked its guard.\n" +
			    "                precedence Clean, Closed;\n" +
			    "\n" +
			    "        }\n" +
			    "        TeamB11sh62 (TB11sh62 b) {\n" +
			    "            new Level1(b);\n" +
			    "        }\n" +
			    "        public static void main(String[] args) {\n" +
			    "            TB11sh62 b = new TB11sh62();\n" +
			    "            new TeamB11sh62(b).activate();\n" +
			    "            b.init();\n" +
			    "            b.open();\n" +
			    "            b.edit();\n" +
			    "            b.close();\n" +
			    "            b.save();\n" +
			    "            b.close();\n" +
			    "            System.out.print(\"Fin\");\n" +
			    "        }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh62.java",
			    "\n" +
			    "public class TB11sh62 {\n" +
			    "    void init() { System.out.print(\"i\"); }\n" +
			    "    void open() { System.out.print(\"o\"); }\n" +
			    "    void edit() { System.out.print(\"e\"); }\n" +
			    "    void save() { System.out.print(\"s\"); }\n" +
			    "    void close() { System.out.print(\"c\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "[I]i[Clo]o[Cle]e[D][!c2]s[Cle]c[Clo]Fin");
    }

    // witness for base method tag overflow, old result was: "0190290390467nullnull"
    // B.1.1-otjld-sh-64
    public void testB11_sh64() {
       
       runConformTest(
            new String[] {
		"TeamB11sh64.java",
			    "\n" +
			    "public team class TeamB11sh64 {\n" +
			    "  public class R playedBy TB11sh64 {\n" +
			    "    callin String ci () {\n" +
			    "      String result = base.ci();\n" +
			    "      System.out.print(result);\n" +
			    "      return result;\n" +
			    "    }\n" +
			    "    ci <- replace m00, m01, m02, m03, m04, m05, m06, m07, m08, m09, \n" +
			    "		  m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, \n" +
			    "		  m20, m21, m22, m23, m24, m25, m26, m27, m28, m29, \n" +
			    "		  m30, m31, m32, m33, m34, m35, m36, m37, m38, m39, \n" +
			    "		  m40, m41, m42, m43, m44, m45, m46, m47, m48, m49, \n" +
			    "		  m50, m51, m52, m53, m54, m55, m56, m57, m58, m59, \n" +
			    "		  m60, m61, m62, m63, m64, m65, m66, m67, m68, m69, \n" +
			    "		  m70, m71, m72, m73, m74, m75, m76, m77, m78, m79, \n" +
			    "		  m80, m81, m82, m83, m84, m85, m86, m87, m88, m89, \n" +
			    "		  m90, m91, m92, m93, m94, m95, m96, m97, m98, m99, \n" +
			    "		  m100, m101, m102, m103, m104, m105, m106, m107, m108, m109, \n" +
			    "		  m110, m111, m112, m113, m114, m115, m116, m117, m118, m119, \n" +
			    "		  m120, m121, m122, m123, m124, m125, m126, m127, m128, m129;\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamB11sh64().activate();\n" +
			    "    TB11sh64 b = new TB11sh64();\n" +
			    "    b.m00(); b.m01(); b.m09();\n" +
			    "    b.m100(); b.m102(); b.m109();\n" +
			    "    b.m110(); b.m103(); b.m109();\n" +
			    "    b.m120(); b.m104(); b.m126(); b.m127(); b.m128(); b.m129();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TB11sh64.java",
			    "\n" +
			    "public class TB11sh64 {\n" +
			    "  String m00() { return \"0\"; } String m01() { return \"1\"; } String m02() { return \"2\"; } String m03 () { return \"3\"; } String m04() { return \"4\"; } String m05() { return \"5\"; } String m06 () { return \"6\"; } String m07() { return \"7\"; } String m08() { return \"8\"; } String m09 () { return \"9\"; }\n" +
			    "  String m10() { return \"0\"; } String m11() { return \"1\"; } String m12() { return \"2\"; } String m13 () { return \"3\"; } String m14() { return \"4\"; } String m15() { return \"5\"; } String m16 () { return \"6\"; } String m17() { return \"7\"; } String m18() { return \"8\"; } String m19 () { return \"9\"; }\n" +
			    "  String m20() { return \"0\"; } String m21() { return \"1\"; } String m22() { return \"2\"; } String m23 () { return \"3\"; } String m24() { return \"4\"; } String m25() { return \"5\"; } String m26 () { return \"6\"; } String m27() { return \"7\"; } String m28() { return \"8\"; } String m29 () { return \"9\"; }\n" +
			    "  String m30() { return \"0\"; } String m31() { return \"1\"; } String m32() { return \"2\"; } String m33 () { return \"3\"; } String m34() { return \"4\"; } String m35() { return \"5\"; } String m36 () { return \"6\"; } String m37() { return \"7\"; } String m38() { return \"8\"; } String m39 () { return \"9\"; }\n" +
			    "  String m40() { return \"0\"; } String m41() { return \"1\"; } String m42() { return \"2\"; } String m43 () { return \"3\"; } String m44() { return \"4\"; } String m45() { return \"5\"; } String m46 () { return \"6\"; } String m47() { return \"7\"; } String m48() { return \"8\"; } String m49 () { return \"9\"; }\n" +
			    "  String m50() { return \"0\"; } String m51() { return \"1\"; } String m52() { return \"2\"; } String m53 () { return \"3\"; } String m54() { return \"4\"; } String m55() { return \"5\"; } String m56 () { return \"6\"; } String m57() { return \"7\"; } String m58() { return \"8\"; } String m59 () { return \"9\"; }\n" +
			    "  String m60() { return \"0\"; } String m61() { return \"1\"; } String m62() { return \"2\"; } String m63 () { return \"3\"; } String m64() { return \"4\"; } String m65() { return \"5\"; } String m66 () { return \"6\"; } String m67() { return \"7\"; } String m68() { return \"8\"; } String m69 () { return \"9\"; }\n" +
			    "  String m70() { return \"0\"; } String m71() { return \"1\"; } String m72() { return \"2\"; } String m73 () { return \"3\"; } String m74() { return \"4\"; } String m75() { return \"5\"; } String m76 () { return \"6\"; } String m77() { return \"7\"; } String m78() { return \"8\"; } String m79 () { return \"9\"; }\n" +
			    "  String m80() { return \"0\"; } String m81() { return \"1\"; } String m82() { return \"2\"; } String m83 () { return \"3\"; } String m84() { return \"4\"; } String m85() { return \"5\"; } String m86 () { return \"6\"; } String m87() { return \"7\"; } String m88() { return \"8\"; } String m89 () { return \"9\"; }\n" +
			    "  String m90() { return \"0\"; } String m91() { return \"1\"; } String m92() { return \"2\"; } String m93 () { return \"3\"; } String m94() { return \"4\"; } String m95() { return \"5\"; } String m96 () { return \"6\"; } String m97() { return \"7\"; } String m98() { return \"8\"; } String m99 () { return \"9\"; }\n" +
			    "String m100() { return \"0\"; } String m101() { return \"1\"; } String m102() { return \"2\"; } String m103 () { return \"3\"; } String m104() { return \"4\"; } String m105() { return \"5\"; } String m106 () { return \"6\"; } String m107() { return \"7\"; } String m108() { return \"8\"; } String m109 () { return \"9\"; }\n" +
			    "  String m110() { return \"0\"; } String m111() { return \"1\"; } String m112() { return \"2\"; } String m113 () { return \"3\"; } String m114() { return \"4\"; } String m115() { return \"5\"; } String m116 () { return \"6\"; } String m117() { return \"7\"; } String m118() { return \"8\"; } String m119 () { return \"9\"; }\n" +
			    "  String m120() { return \"0\"; } String m121() { return \"1\"; } String m122() { return \"2\"; } String m123 () { return \"3\"; } String m124() { return \"4\"; } String m125() { return \"5\"; } String m126 () { return \"6\"; } String m127() { return \"7\"; } String m128() { return \"8\"; } String m129 () { return \"9\"; }\n" +
			    "}\n" +
			    "  \n"
            },
            "019029039046789");
    }

    // anonymous type in callin method - witness for scoping problem
    // B.1.1-otjld-sh-65
    public void testB11_sh65() {
       
       runConformTest(
            new String[] {
		"TeamB11sh65.java",
			    "\n" +
			    "public team class TeamB11sh65 {\n" +
			    "    protected class R playedBy B11sh65 {\n" +
			    "        test <- replace test;\n" +
			    "        callin void test(final IB11sh65 i) {\n" +
			    "            base.test(new IB11sh65() {\n" +
			    "                public String getOK() {\n" +
			    "                    return i.getOK();\n" +
			    "                }\n" +
			    "            });\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh65().activate();\n" +
			    "        new B11sh65().test(new IB11sh65() {\n" +
			    "            public String getOK() { return \"OK\"; }\n" +
			    "        });\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IB11sh65.java",
			    "\n" +
			    "public interface IB11sh65 {\n" +
			    "    String getOK();\n" +
			    "}\n" +
			    "    \n",
		"B11sh65.java",
			    "\n" +
			    "public class B11sh65 {\n" +
			    "    void test(IB11sh65 i) {\n" +
			    "        System.out.print(i.getOK());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // initializer in a role class
    // B.1.1-otjld-sh-66
    public void testB11_sh66() {
       
       runConformTest(
            new String[] {
		"TeamB11sh66.java",
			    "\n" +
			    "import java.util.HashMap;\n" +
			    "public team class TeamB11sh66 {\n" +
			    "    public class R {\n" +
			    "        HashMap<String,String> o2n = new HashMap<String,String>();\n" +
			    "        {\n" +
			    "            o2n.put(\"O\",\"N\");\n" +
			    "        }\n" +
			    "        protected void test(String o) {\n" +
			    "            System.out.print(o2n.get(o));\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R().test(\"O\");\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh66().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "N");
    }

    // warning irritant bit conflict - hint by Philippe Mulet
    // B.1.1-otjld-sh-67
    public void testB11_sh67() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownException, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedTypeArgumentsForMethodInvocation, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedLabel, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
        runTestExpectingWarnings(
            new String[] {
		"TB11sh67.java",
			    "\n" +
			    "public class TB11sh67 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private int secret;\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh67.java",
			    "\n" +
			    "@SuppressWarnings({\"unused\",\"basecall\"})\n" +
			    "public team class TeamB11sh67 {\n" +
			    "    void bar(int unused) {}\n" +
			    "    protected class R playedBy TB11sh67 {\n" +
			    "        int getSecret() -> get int secret; // <- decapsulation warning was masked by above @SuppressWarnings\n" +
			    "        callin void noBaseCall() {\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. WARNING in TeamB11sh67.java (at line 6)\n" +
			"	int getSecret() -> get int secret; // <- decapsulation warning was masked by above @SuppressWarnings\n" +
			"	                           ^^^^^^\n" +
			"Access restriction of private field secret in type TB11sh67 is overridden by this binding (OTJLD 3.5(e)).\n" +
			"----------\n",
            customOptions);
    }

    // warning irritant bit conflict - hint by Philippe Mulet
    // B.1.1-otjld-sh-68
	public void testB11_sh68() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownException, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedTypeArgumentsForMethodInvocation, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedLabel, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.WARNING);
       runTestExpectingWarnings(
            new String[] {
		"TB11sh68.java",
			    "\n" +
			    "public class TB11sh68 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private int secret;\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh68.java",
			    "\n" +
			    "@SuppressWarnings({\"unused\",\"basecall\",\"restriction\"}) // last one is unnecessary\n" +
			    "public team class TeamB11sh68 {\n" +
			    "    void bar(int unused) {}\n" +
			    "    @SuppressWarnings(\"basecall\") // unnesserary\n" +
			    "    protected class R playedBy TB11sh68 {\n" +
			    "        int getSecret() -> get int secret; // <- decapsulation warning was masked by above @SuppressWarnings\n" +
			    "        callin void noBaseCall() {\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. WARNING in TeamB11sh68.java (at line 2)\n" + 
    		"	@SuppressWarnings({\"unused\",\"basecall\",\"restriction\"}) // last one is unnecessary\n" + 
    		"	                            ^^^^^^^^^^\n" + 
    		"Unnecessary @SuppressWarnings(\"basecall\")\n" + 
    		"----------\n" + 
    		"2. WARNING in TeamB11sh68.java (at line 2)\n" + 
    		"	@SuppressWarnings({\"unused\",\"basecall\",\"restriction\"}) // last one is unnecessary\n" + 
    		"	                                       ^^^^^^^^^^^^^\n" + 
    		"Unnecessary @SuppressWarnings(\"restriction\")\n" + 
    		"----------\n" + 
    		"3. WARNING in TeamB11sh68.java (at line 7)\n" + 
    		"	int getSecret() -> get int secret; // <- decapsulation warning was masked by above @SuppressWarnings\n" + 
    		"	                           ^^^^^^\n" + 
    		"Access restriction of private field secret in type TB11sh68 is overridden by this binding (OTJLD 3.5(e)).\n" + 
    		"----------\n",
    		customOptions);
    }

    // issue with static initialization - user and generated conflicting, reported by Miguel Monteiro
    // B.1.1-otjld-sh-69
    public void testB11_sh69() {
       
       runConformTest(
            new String[] {
		"TB11sh69Main.java",
			    "\n" +
			    "public class TB11sh69Main {\n" +
			    "  public static void main(String[] args) {\n" +
			    "    TeamB11sh69 t = new TeamB11sh69();\n" +
			    "    TB11sh69 b = new TB11sh69();\n" +
			    "    t.activate();\n" +
			    "    b.yes();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TB11sh69.java",
			    "\n" +
			    "public class TB11sh69 {\n" +
			    "  static {\n" +
			    "    print(\"O\");\n" +
			    "  }\n" +
			    "  public static void print(String s) {\n" +
			    "    System.out.print(s);\n" +
			    "  }\n" +
			    "  public void yes() { print(\"!\"); }\n" +
			    "}\n" +
			    "  \n",
		"TeamB11sh69.java",
			    "\n" +
			    "public team class TeamB11sh69 {\n" +
			    "  protected class R playedBy TB11sh69 {\n" +
			    "    static void printK() { System.out.print(\"K\"); }\n" +
			    "    printK <- before print;\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK!");
    }

    // internal call should use custom ctor
    // B.1.1-otjld-sh-70
    public void testB11_sh70() {
       
       runConformTest(
            new String[] {
		"TeamB11sh70.java",
			    "\n" +
			    "public team class TeamB11sh70 {\n" +
			    "    protected class R playedBy TB11sh70 {\n" +
			    "\n" +
			    "        R(R other) { // this constructor is hidden by the more visible dflt lifting ctor!\n" +
			    "            base(\"K\");\n" +
			    "        }\n" +
			    "        protected R createOther() {\n" +
			    "            return new R(this); // within the role the better fitting ctor should be used\n" +
			    "        }\n" +
			    "    }\n" +
			    "    TeamB11sh70() {\n" +
			    "        new R(new TB11sh70(\"O\")).createOther();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh70();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh70.java",
			    "\n" +
			    "public class TB11sh70 {\n" +
			    "    TB11sh70(String s) { System.out.print(s); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // witness for ArrayStoreException found in the logs - custom ctor is never used
    // B.1.1-otjld-sh-70f
    public void testB11_sh70f() {
       
       runConformTest(
            new String[] {
		"TeamB11sh70f.java",
			    "\n" +
			    "public team class TeamB11sh70f {\n" +
			    "    protected class R playedBy TB11sh70f {\n" +
			    "\n" +
			    "        R(R other) { // this constructor is hidden by the more visible dflt lifting ctor!\n" +
			    "            base(\"K\");\n" +
			    "        }        \n" +
			    "    }\n" +
			    "    TeamB11sh70f() {\n" +
			    "        new R(new R(new TB11sh70f(\"O\")));\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        try {\n" +
			    "            new TeamB11sh70f();\n" +
			    "        } catch (org.objectteams.DuplicateRoleException e) {\n" +
			    "            System.out.print(\"caught\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh70f.java",
			    "\n" +
			    "public class TB11sh70f {\n" +
			    "    TB11sh70f(String s) { System.out.print(s); }\n" +
			    "}\n" +
			    "    \n"
            },
            "Ocaught");
    }

    // variant of above, right ctor resolved
    // B.1.1-otjld-sh-71
    public void testB11_sh71() {
       
       runConformTest(
            new String[] {
		"TeamB11sh71.java",
			    "\n" +
			    "public team class TeamB11sh71 {\n" +
			    "    protected class R playedBy TB11sh71 {\n" +
			    "        protected R(R other) {\n" +
			    "            base(\"K\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    TeamB11sh71() {\n" +
			    "        new R(new R(new TB11sh71(\"O\")));\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh71();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh71.java",
			    "\n" +
			    "public class TB11sh71 {\n" +
			    "    TB11sh71(String s) { System.out.print(s); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // witness for NegativeArraySizeException in StackMapFrame.duplicate (pre r19271
    // B.1.1-otjld-sh-72
    public void testB11_sh72() {
       
       runConformTest(
            new String[] {
		"TeamB11sh72.java",
			    "\n" +
			    "public team class TeamB11sh72 {\n" +
			    "    protected class MyRole playedBy B11sh72 {\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        static callin void foo() {}\n" +
			    "        foo <- replace foo\n" +
			    "             when (true);\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        if (new MyRole(new B11sh72()) instanceof MyRole) // force loading\n" +
			    "            System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "    public static void main (String[] args) {\n" +
			    "        new TeamB11sh72().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"B11sh72.java",
			    "\n" +
			    "public class B11sh72 {\n" +
			    "    public static void foo() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // witness for NPE in MethodModel.getReturnType from TSuperMessageSend.resolveType
    // B.1.1-otjld-sh-73
    public void testB11_sh73() {
        runNegativeTest(
            new String[] {
		"TeamB11sh73.java",
			    "\n" +
			    "public team class TeamB11sh73 {\n" +
			    "    protected class R {\n" +
			    "        callin void doit() {\n" +
			    "            tsuper.doit(); // no tsuper available\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh73.java (at line 5)\n" + 
    		"	tsuper.doit(); // no tsuper available\n" + 
    		"	^^^^^^^^^^^^^\n" + 
    		"Illegal tsuper call: role TeamB11sh73.R has no implicit super class (OTJLD 1.3.1(f)).\n" + 
    		"----------\n");
    }

    // Witness for ICE reported by MWSE0809 group
    // B.1.1-otjld-sh-74
    public void testB11_sh74() {
        runNegativeTest(
            new String[] {
		"TeamB11sh74_2.java",
			    "\n" +
			    "public team class TeamB11sh74_2 extends TeamB11sh74_1 {\n" +
			    "}\n" +
			    "    \n",
		"TB11sh74.java",
			    "\n" +
			    "public class TB11sh74 {\n" +
			    "    int id;\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh74_1.java",
			    "\n" +
			    "public team class TeamB11sh74_1 {\n" +
			    "    protected class R playedBy TB11sh74 {\n" +
			    "        String getId() -> get int id; // bug\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh74_1.java (at line 4)\n" + 
    		"	String getId() -> get int id; // bug\n" + 
    		"	^^^^^^^^^^^^^^\n" + 
    		"When binding field id via callout to role method getId():\n" + 
    		"Incompatible types: can\'t convert int to java.lang.String (OTJLD 3.5(b)).\n" + 
    		"----------\n");
    }

    // buggy callout should not create a CalloutMappingsAttribute-part - witness for ICE in CalloutMappingsAttribute.evaluateLateAttribute
    // B.1.1-otjld-sh-75
    public void testB11_sh75() {
       Map customOptions = getCompilerOptions();
       
       runTest(
            new String[] {
		"TeamB11sh75_2.java",
			    "\n" +
			    "public team class TeamB11sh75_2 extends TeamB11sh75_1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamB11sh75_1 t = new TeamB11sh75_1();\n" +
			    "        R<@t> r = t.getR();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh75.java",
			    "\n" +
			    "public class TB11sh75 {\n" +
			    "    int id;\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh75_1.java",
			    "\n" +
			    "public team class TeamB11sh75_1 {\n" +
			    "    public class R playedBy TB11sh75 {\n" +
			    "        String mismatch() -> get String id;\n" +
			    "        toString => toString;\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public R getR() {\n" +
			    "        return new R(new TB11sh75());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            true/*expectingCompilerErrors*/,
            "----------\n" + 
    		"1. ERROR in TeamB11sh75_1.java (at line 4)\n" + 
    		"	String mismatch() -> get String id;\n" + 
    		"	                         ^^^^^^\n" + 
    		"Field specifier \'id\' resolves to type int whereas type java.lang.String is specified (OTJLD 3.5(a)).\n" + 
    		"----------\n",
            "OK",
            ""/*expectedErrorOutput*/,
            true/*forceExecution*/,
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/,
            true/*skipJavac*/);
    }

    // buggy callout should not create a CalloutMappingsAttribute-part - witness for ICE in CalloutMappingsAttribute.evaluateLateAttribute
    // B.1.1-otjld-sh-75
    public void testB11_sh75a() {
       Map customOptions = getCompilerOptions();
       
       runTest(
            new String[] {
		"TeamB11sh75a_2.java",
			    "\n" +
			    "public team class TeamB11sh75a_2 extends TeamB11sh75a_1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamB11sh75a_1 t = new TeamB11sh75a_1();\n" +
			    "        R<@t> r = t.getR();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh75a.java",
			    "\n" +
			    "public class TB11sh75a {\n" +
			    "    int id;\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh75a_1.java",
			    "\n" +
			    "public team class TeamB11sh75a_1 {\n" +
			    "    public class R playedBy TB11sh75a {\n" +
			    "        String mismatch() -> get int id;\n" +
			    "        toString => toString;\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public R getR() {\n" +
			    "        return new R(new TB11sh75a());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            true/*expectingCompilerErrors*/,
            "----------\n" + 
			"1. ERROR in TeamB11sh75a_1.java (at line 4)\n" + 
			"	String mismatch() -> get int id;\n" + 
			"	^^^^^^^^^^^^^^^^^\n" + 
			"When binding field id via callout to role method mismatch():\n" + 
			"Incompatible types: can\'t convert int to java.lang.String (OTJLD 3.5(b)).\n" + 
			"----------\n",
            "OK",
            ""/*expectedErrorOutput*/,
            true/*forceExecution*/,
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/,
            true/*skipJavac*/);
    }

    // illegal qualified use of role in array type ref - witness for NPE in Scope.createArrayType
    // B.1.1-otjld-sh-76
    public void testB11_sh76() {
        runNegativeTestMatching(
            new String[] {
		"p1/TeamB11sh76.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class TeamB11sh76 {\n" +
			    "    protected class R { }\n" +
			    "    void foo (p1.TeamB11sh76.R[] roles) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in p1\\TeamB11sh76.java (at line 5)\n" + 
    		"	void foo (p1.TeamB11sh76.R[] roles) {}\n" + 
    		"	          ^^^^^^^^^^^^^^^^^^\n" + 
    		"Illegal qualified use of non-public role R (OTJLD 1.2.3(b)).\n" + 
    		"----------\n");
    }

    // attempted callout to constructor, CCE reported by Fabian Maack
    // B.1.1-otjld-sh-77
    public void testB11_sh77() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh77.java",
			    "\n" +
			    "public team class TeamB11sh77 {\n" +
			    "    protected class R playedBy TB11sh77 {\n" +
			    "        R getNew(String a) -> TB11sh77(String a);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh77.java",
			    "\n" +
			    "public class TB11sh77 {\n" +
			    "    public TB11sh77(String a) { }\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.2");
    }

    // NPE was thrown by LiftingEnvironment if late role was unbound after inline roles are bound
    // B.1.1-otjld-sh-78
    public void testB11_sh78() {
       
       runConformTest(
            new String[] {
		"TeamB11sh78.java",
			    "\n" +
			    "public team class TeamB11sh78 {\n" +
			    "    \n" +
			    "    protected class R2 playedBy TB11sh78 {\n" +
			    "        protected void test2() {\n" +
			    "            R1 r = new R1();\n" +
			    "            r.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void testt(TB11sh78 as R2 o) {\n" +
			    "        o.test2();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamB11sh78 t = new TeamB11sh78();\n" +
			    "        t.testt(new TB11sh78());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh78.java",
			    "\n" +
			    "public class TB11sh78 { }\n" +
			    "    \n",
		"TeamB11sh78/R1.java",
			    "\n" +
			    "team package TeamB11sh78;\n" +
			    "protected class R1 {\n" +
			    "    protected void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // wrong team package leaves role unresolved, NPE in RoleModel.hasBaseclassProblem() seen
    // B.1.1-otjld-sh-78f
    public void testB11_sh78f() {
        runNegativeTest(
            new String[] {
		"TeamB11sh78f.java",
			    "\n" +
			    "public team class TeamB11sh78f {\n" +
			    "\n" +
			    "    protected class R2 playedBy TB11sh78f {\n" +
			    "        protected void test2() {\n" +
			    "            R1 r = new R1();\n" +
			    "            r.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh78f.java",
			    "\n" +
			    "public class TB11sh78f { }\n" +
			    "    \n",
		"TeamB11sh78f/R1.java",
			    "\n" +
			    "team package Wrong;\n" +
			    "protected class R1 {\n" +
			    "    protected void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh78f.java (at line 6)\n" + 
    		"	R1 r = new R1();\n" + 
    		"	^^\n" + 
    		"R1 cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"2. ERROR in TeamB11sh78f.java (at line 6)\n" + 
    		"	R1 r = new R1();\n" + 
    		"	           ^^\n" + 
    		"R1 cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"----------\n" + 
    		"1. ERROR in TeamB11sh78f\\R1.java (at line 2)\n" + 
    		"	team package Wrong;\n" + 
    		"	             ^^^^^\n" + 
    		"Enclosing team Wrong not found for role file R1 (OTJLD 1.2.5(c)).\n" + 
    		"----------\n" + 
    		"2. ERROR in TeamB11sh78f\\R1.java (at line 3)\n" + 
    		"	protected class R1 {\n" + 
    		"	                ^^\n" + 
    		"Illegal modifier for the class R1; only public, abstract & final are permitted\n" + 
    		"----------\n");
    }

    // a callout tries to override a callin method - witness for AIOOBE in MethodMappingImplementaor.copyArguments
    // B.1.1-otjld-sh-79
    public void testB11_sh79() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh79.java",
			    "\n" +
			    "public team class TeamB11sh79 {\n" +
			    "    protected class R0 {\n" +
			    "        callin void simple(int j) {}\n" +
			    "    }\n" +
			    "    protected class R1 extends R0 playedBy TB11sh79 {\n" +
			    "        void simple(int k) => void simple(int j);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh79.java",
			    "\n" +
			    "public class TB11sh79 {\n" +
			    "    void simple(int j) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "callin");
    }

    // NPE observed while preparing ECOOP slides
    // B.1.1-otjld-sh-80
    public void testB11_sh80() {
        runNegativeTest(
            new String[] {
		"TeamB11sh80_2.java",
			    "\n" +
			    "import p1.TeamB11sh80_1;\n" +
			    "import p1.TeamB11sh80_1.R_1;\n" +
			    "public team class TeamB11sh80_2 {\n" +
			    "    @SuppressWarnings(\"bindingconventions\")\n" +
			    "    protected class R_2 playedBy R_1 {\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/TeamB11sh80_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class TeamB11sh80_1 {\n" +
			    "    public class R_1 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh80_2.java (at line 3)\n" + 
    		"	import p1.TeamB11sh80_1.R_1;\n" + 
    		"	       ^^^^^^^^^^^^^^^^^^^^\n" + 
    		"Illegal import for role type p1.TeamB11sh80_1.R_1: roles cannot be imported (OTJLD 1.2.2(i)).\n" + 
    		"----------\n" + 
    		"2. ERROR in TeamB11sh80_2.java (at line 6)\n" + 
    		"	protected class R_2 playedBy R_1 {\n" + 
    		"	                ^^^\n" + 
    		"Missing anchor (team instance) for role type p1.TeamB11sh80_1.R_1 outside its team context (OTJLD 1.2.2(b)).\n" + 
    		"----------\n" + 
    		"3. ERROR in TeamB11sh80_2.java (at line 6)\n" + 
    		"	protected class R_2 playedBy R_1 {\n" + 
    		"	                             ^^^\n" + 
    		"Missing anchor (team instance) for role type p1.TeamB11sh80_1.R_1 outside its team context (OTJLD 1.2.2(b)).\n" + 
    		"----------\n" + 
    		"4. ERROR in TeamB11sh80_2.java (at line 6)\n" + 
    		"	protected class R_2 playedBy R_1 {\n" + 
    		"	                             ^^^\n" + 
    		"Missing anchor (team instance) for role type p1.TeamB11sh80_1.R_1 outside its team context (OTJLD 1.2.2(b)).\n" + 
    		"----------\n");
    }

    // Various problems caused by unbalanced braces
    // B.1.1-otjld-sh-81
    public void testB11_sh81() {
        runNegativeTest(
            new String[] {
		"TeamB11sh81.java",
			    "\n" +
			    "public team class TeamB11sh81 {\n" +
			    "    protected class R1 {\n" +
			    "        void m1() { // unfinished }\n" +
			    "    }\n" +
			    "    protected class R2 {\n" +
			    "        callin void ci (Object arg) {\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected class R3 implements ILowerable playedBy TB11sh81 {\n" +
			    "        void t() {\n" +
			    "            this.lower().bm();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh81.java",
			    "\n" +
			    "public class TB11sh81 { public void bm() {} }\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh81.java (at line 6)\n" + 
    		"	protected class R2 {\n" + 
    		"	                ^^\n" + 
    		"Member types not allowed in regular roles. Mark class TeamB11sh81.R1 as a team if R2 should be its role (OTJLD 1.5(a,b)). \n" + 
    		"----------\n" + 
    		"2. ERROR in TeamB11sh81.java (at line 15)\n" + 
    		"	}\n" + 
    		"	^\n" + 
    		"Syntax error, insert \"}\" to complete ClassBody\n" + 
    		"----------\n");
    }

    // method in a nested team needs both a private-accessor and type wrapping
    // B.1.1-otjld-sh-82
    public void testB11_sh82() {
       
       runConformTest(
            new String[] {
		"TeamB11sh82.java",
			    "\n" +
			    "public team class TeamB11sh82 {\n" +
			    "    protected team class Mid {\n" +
			    "        public class Role1 {\n" +
			    "            protected void sayOK() {\n" +
			    "                System.out.print(\"OK\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class Role2 {\n" +
			    "            protected void go() {\n" +
			    "                getRole1(this).sayOK();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        private Role1 getRole1(Role2 r2) {\n" +
			    "            return new Role1();\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            new Role2().go();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected void test() {\n" +
			    "        new Mid().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh82().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // public field caused ctor with declared lifting to produce illegal byte code
    // B.1.1-otjld-sh-83
    public void testB11_sh83() {
       
       runConformTest(
            new String[] {
		"TeamB11sh83_2.java",
			    "\n" +
			    "public team class TeamB11sh83_2 extends TeamB11sh83_1 {\n" +
			    "    String s;\n" +
			    "    protected class R playedBy TB11sh83 {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(getVal());\n" +
			    "        }\n" +
			    "        String getVal() -> get String val;\n" +
			    "    }\n" +
			    "    public TeamB11sh83_2(TB11sh83 as R r) {     // ctor requesting copying of tsuper version\n" +
			    "        super(r);\n" +
			    "        s = TB11sh83.getString();\n" +
			    "    }\n" +
			    "    public void test() {\n" +
			    "        this.theR.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh83_2(new TB11sh83()).test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11sh83.java",
			    "\n" +
			    "public class TB11sh83 {\n" +
			    "    String val = \"OK\";\n" +
			    "    public static String getString() { return \"dontcare\"; }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh83_1.java",
			    "\n" +
			    "public abstract team class TeamB11sh83_1 {\n" +
			    "    R theR;\n" +
			    "    int status;\n" +
			    "    protected abstract class R {\n" +
			    "        public int id;                          // offending field\n" +
			    "        abstract protected void test();\n" +
			    "    }\n" +
			    "    public TeamB11sh83_1(R r) {                 // ctor needing copying\n" +
			    "        this.theR = r;\n" +
			    "        this.status = 1;\n" +
			    "        activate(ALL_THREADS);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // challenge copy inheritance of local type in buggy role method
    // B.1.1-otjld-sh-84
    public void testB11_sh84() {
        Map customOptions = getCompilerOptions();
        customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
        runNegativeTest(
            new String[] {
		"TeamB11sh84_2.java",
			    "\n" +
			    "public team class TeamB11sh84_2 extends TeamB11sh84_1 {\n" +
			    "    \n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh84_1.java",
			    "\n" +
			    "public team class TeamB11sh84_1 {\n" +
			    "    protected class R {\n" +
			    "        void test() {\n" +
			    "            unresolved();\n" +
			    "            new Runnable() { public void run() { System.out.println(\"would be ok\"); } }.run();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh84_1.java (at line 5)\n" + 
    		"	unresolved();\n" + 
    		"	^^^^^^^^^^\n" + 
    		"The method unresolved() is undefined for the type TeamB11sh84_1.R\n" + 
    		"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }

    // challenge copy inheritance of local type in role method of buggy team
    // B.1.1-otjld-sh-85
    public void testB11_sh85() {
        Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
        runNegativeTest(
            new String[] {
		"TeamB11sh85_2.java",
			    "\n" +
			    "public team class TeamB11sh85_2 extends TeamB11sh85_1 {\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh85_1.java",
			    "\n" +
			    "import not.existing.Import;\n" +
			    "public team class TeamB11sh85_1 {\n" +
			    "    protected class R {\n" +
			    "        void test() {\n" +
			    "            new Runnable() { public void run() { System.out.println(\"would be ok\"); } }.run();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh85_1.java (at line 2)\n" + 
    		"	import not.existing.Import;\n" + 
    		"	       ^^^\n" + 
    		"The import not cannot be resolved\n" + 
    		"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }

    // callout refers to missing type - example thanks to Eugene Hutorny
    // B.1.1-otjld-sh-86
    public void testB11_sh86() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
        runConformTest(
            new String[] {
		"TeamB11sh86_3.java",
			    "\n" +
			    "public team class TeamB11sh86_3 extends TeamB11sh86_2 { }\n" +
			    "    \n",
		"p1/TB11sh86_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class TB11sh86_1 {\n" +
			    "}\n" +
			    "    \n",
		"TB11sh86_2.java",
			    "\n" +
			    "public class TB11sh86_2 {\n" +
			    "    public p1.TB11sh86_1 bm(Object o) { return null; }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh86_1.java",
			    "\n" +
			    "import p1.TB11sh86_1; // the BUG\n" +
			    "public team class TeamB11sh86_1 {\n" +
			    "    abstract protected class R {\n" +
			    "        public abstract TB11sh86_1 getBase(Object o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11sh86_2.java",
			    "\n" +
			    "import p1.TB11sh86_1;\n" +
			    "public team class TeamB11sh86_2 extends TeamB11sh86_1 {\n" +
			    "    @Override\n" +
			    "    protected class R playedBy TB11sh86_2 {\n" +
			    "        getBase -> bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"},
                null,
                null/*classLibraries*/,
                false/*shouldFlushOutputDirectory*/,
                null/*vmArguments*/,
                customOptions,
                null/*requestor*/); 
        runNegativeTest(
            new String[] {
		"TeamB11sh86_1.java",
			    "\n" +
			    "//import p1.TB11sh86_1; // the BUG\n" +
			    "public team class TeamB11sh86_1 {\n" +
			    "    abstract protected class R {\n" +
			    "        public abstract TB11sh86_1 getBase(Object o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh86_1.java (at line 5)\n" + 
    		"	public abstract TB11sh86_1 getBase(Object o);\n" + 
    		"	                ^^^^^^^^^^\n" + 
    		"TB11sh86_1 cannot be resolved to a type\n" + 
    		"----------\n",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            customOptions,
            true/*generateOutput*/,
            false/*showCategory*/,
            false/*showWarningToken*/);
        runNegativeTest(
            new String[] {
        // same as above
		"TeamB11sh86_2.java",
			    "\n" +
			    "import p1.TB11sh86_1;\n" +
			    "public team class TeamB11sh86_2 extends TeamB11sh86_1 {\n" +
			    "    @Override\n" +
			    "    protected class R playedBy TB11sh86_2 {\n" +
			    "        getBase -> bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            // FIXME(SH): why these errors:?
            "----------\n" + 
    		"1. ERROR in TeamB11sh86_2.java (at line 6)\n" + 
    		"	getBase -> bm;\n" + 
    		"	^^^^^^^\n" + 
    		"A non-abstract role method exists for this callout-binding. Use callout-override (\'=>\') if you want to override it (OTJLD 3.1(e)).\n" + 
    		"----------\n" + 
    		"2. ERROR in TeamB11sh86_2.java (at line 6)\n" + 
    		"	getBase -> bm;\n" + 
    		"	^^^^^^^\n" + 
    		"The return type is incompatible with TeamB11sh86_1.R.getBase(Object)\n" + 
    		"----------\n",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            customOptions,
            true/*generateOutput*/,
            false/*showCategory*/,
            false/*showWarningToken*/);
        runNegativeTest(
            new String[] {
        // same as above
		"TeamB11sh86_3.java",
			    "\n" +
			    "public team class TeamB11sh86_3 extends TeamB11sh86_2 { }\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh86_3.java (at line 1)\n" + 
    		"	\n" + 
    		"public team class TeamB11sh86_3 extends TeamB11sh86_2 { }\n" + 
    		"	^\n" + 
    		"The type TB11sh86_1 cannot be resolved. It is indirectly referenced from required .class files\n" + 
    		"----------\n" + 
    		"2. ERROR in TeamB11sh86_3.java (at line 2)\n" + 
    		"	public team class TeamB11sh86_3 extends TeamB11sh86_2 { }\n" + 
    		"	                  ^^^^^^^^^^^^^\n" + 
    		"Role TeamB11sh86_3.R seems to refer to a stale tsuper role TeamB11sh86_1.R: tsuper method getBase(Ljava/lang/Object;)Lp1/TB11sh86_1; cannot be resolved. Please consider a full rebuild of the enclosing project.\n" + 
    		"----------\n",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            customOptions);
    }

    // challenging decapsulation an inherited method - exception reported by Eugene Hutorny
    // B.1.1-otjld-sh-87
    public void testB11_sh87() {
       
       runConformTest(
            new String[] {
		"pb/TB11sh87Main.java",
			    "\n" +
			    "package pb;\n" +
			    "public class TB11sh87Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        pb.TB11sh87.InnerBase o = new pb.TB11sh87().new InnerBase();\n" +
			    "        new TeamB11sh87_3().test(o);\n" +
			    "        System.out.print(o.toString());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"pt/TB11sh87_0.java",
			    "\n" +
			    "package pt;\n" +
			    "import java.math.BigDecimal;\n" +
			    "public abstract class TB11sh87_0 {\n" +
			    "    protected BigDecimal amount;\n" +
			    "    protected void setAmount(BigDecimal amount) {\n" +
			    "        this.amount = amount;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"pb/TB11sh87.java",
			    "\n" +
			    "package pb;\n" +
			    "public class TB11sh87 {\n" +
			    "    protected class InnerBase extends pt.TB11sh87_0 {\n" +
			    "        public String toString() {\n" +
			    "            return amount.toString();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"pt/TeamB11sh87_1.java",
			    "\n" +
			    "package pt;\n" +
			    "import java.math.BigDecimal;\n" +
			    "public team class TeamB11sh87_1 {\n" +
			    "    abstract protected class R {\n" +
			    "        abstract protected void setAmount(BigDecimal amount);\n" +
			    "    }\n" +
			    "    public void test(R o) {\n" +
			    "        o.setAmount(new BigDecimal(42));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"pb/TeamB11sh87_2.java",
			    "\n" +
			    "package pb;\n" +
			    "public team class TeamB11sh87_2 extends pt.TeamB11sh87_1 {\n" +
			    "    protected class R playedBy TB11sh87.InnerBase {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        setAmount -> setAmount;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"pb/TeamB11sh87_3.java",
			    "\n" +
			    "package pb;\n" +
			    "public team class TeamB11sh87_3 extends TeamB11sh87_2 {\n" +
			    "    public void test(TB11sh87.InnerBase as R o) {\n" +
			    "        super.test(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "42");
    }

    // Precedence declaration with unqualified names on team level (reported in TPX-315).
    // B.1.1-otjld-ph-1
    public void testB11_sh88() {
        runNegativeTest(
            new String[] {
		"TB11sh88.java",
			    "\n" +
			    "public class TB11sh88 {\n" +
			    "    void foo() {}\n" +
			    "}\n" +
			    "	\n",
		"TeamB11sh88.java",
			    "\n" +
			    "public team class TeamB11sh88 {\n" +
			    "    public class RB11sh881 playedBy TB11sh88 {\n" +
			    "		 callin1: bar <- after foo;\n" +
			    "       void bar() {}\n" +
			    "    }\n" +
			    "    public class RB11sh882 playedBy TB11sh88 {\n" +
			    "       callin2: bar <- after foo;\n" +
			    "       void bar() {}\n" +
			    "    }\n" +
			    "    precedence callin1, callin2;\n" +
			    "}\n" +
			    "	\n"
            },
            null);
    }

    // Callin binding w/ static base method (reported in TPX-318).
    // B.1.1-otjld-sh-89
    public void testB11_sh89() {
       
       runConformTest(
            new String[] {
		"TeamB11sh89Main.java",
			    "\n" +
			    "public team class TeamB11sh89Main {\n" +
			    "    public class RB11sh89 playedBy TB11sh89 {\n" +
			    "        foo <- after bar; \n" +
			    "        static void foo() {System.out.print(\"OK\");}\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamB11sh89Main t = new TeamB11sh89Main();\n" +
			    "        t.activate();\n" +
			    "        TB11sh89.bar();\n" +
			    "    }\n" +
			    "}\n" +
			    "	\n",
		"TB11sh89.java",
			    "\n" +
			    "public class TB11sh89 {\n" +
			    "	static void bar() {}\n" +
			    "}\n" +
			    "\n"
            },
            "OK");
    }

    // TPX-321: Duplicate role causes InternalCompilerError
    // B.1.1-otjld-sh-90
    public void testB11_sh90() {
    	String convertedOutputPath = new String(OUTPUT_DIR+'/').replace('/', '\\');
        myWriteFiles(
            new String[] {
		"TeamB11sh90.java",
			    "\n" +
			    "		public team class TeamB11sh90 {\n" +
			    "			public class RB11sh90 {}\n" +
			    "			void foo() { new RB11sh90(); }\n" +
			    "		}\n" +
			    "	\n",
		"TB11sh90.java",
			    "\n" +
			    "public class TB11sh90 {}\n"});
        runNegativeTest(
                new String[] {
		"TeamB11sh90/RB11sh90.java",
			    "\n" +
			    "team package TeamB11sh90;\n" +
			    "public class RB11sh90 playedBy TB11sh90 {}\n"
            },
            "----------\n" + 
    		"1. ERROR in "+convertedOutputPath+"TeamB11sh90.java (at line 3)\n" + 
    		"	public class RB11sh90 {}\n" + 
    		"	  ^^^^^^^^\n" + 
    		"The method put(TB11sh90, RB11sh90<@tthis[TeamB11sh90]>) in the type DoublyWeakHashMap<TB11sh90,RB11sh90<@tthis[TeamB11sh90]>> is not applicable for the arguments (TB11sh90, RB11sh90<@tthis[TeamB11sh90]>)\n" + 
    		"----------\n" + 
    		"----------\n" + 
    		"1. ERROR in TeamB11sh90\\RB11sh90.java (at line 3)\n" + 
    		"	public class RB11sh90 playedBy TB11sh90 {}\n" + 
    		"	             ^^^^^^^^\n" + 
    		"Duplicate nested type RB11sh90\n" + 
    		"----------\n",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            null/*customOptions*/);
    }

    // Incorrect precedence declaration causes NPE (reported in TPX-324).
    // B.1.1-otjld-sh-91
    public void testB11_sh91() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh91.java",
			    "\n" +
			    "public team class TeamB11sh91 {\n" +
			    "    public class RB11sh911 playedBy TB11sh91 {\n" +
			    "		 callin1: bar <- after foo;\n" +
			    "       void bar() {}\n" +
			    "    }\n" +
			    "    public class RB11sh912 playedBy TB11sh91 {\n" +
			    "       callin2: bar <- after foo;\n" +
			    "       void bar() {}\n" +
			    "    }\n" +
			    "    precedence RB11sh911.callin2, RB11sh912.callin2;\n" +
			    "}\n" +
			    "	\n",
		"TB11sh91.java",
			    "\n" +
			    "public class TB11sh91 {\n" +
			    "    void foo() {}\n" +
			    "}\n" +
			    "	\n"
            },
            "4.8(b)");
    }

    // this produces an NPE in Expression.computeConversion
    // B.1.1-otjld-sh-92
    public void testB11_sh92() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11sh92.java",
			    "\n" +
			    "import java.util.*;\n" +
			    "import base p.IB11sh92;\n" +
			    "import base p.B11sh92_2;\n" +
			    "public team class TeamB11sh92 {\n" +
			    "    protected class RBroken implements IB11sh92 playedBy IB11sh92 {\n" +
			    "        \n" +
			    "    }\n" +
			    "    public class ROK playedBy B11sh92_2 {\n" +
			    "        private Collection<RBroken> roles = new LinkedList<RBroken>();\n" +
			    "        void addRole(RBroken r) {\n" +
			    "            roles.add(r);\n" +
			    "        }\n" +
			    "        addRole <- after addBase;\n" +
			    "    }\n" +
			    "}\n" +
			    "     \n",
		"p/IB11sh92.java",
			    "\n" +
			    "package p;\n" +
			    "public interface IB11sh92 {\n" +
			    "}\n" +
			    "     \n",
		"p/B11sh92_1.java",
			    "\n" +
			    "package p;\n" +
			    "public class B11sh92_1 implements IB11sh92 {\n" +
			    "}\n" +
			    "     \n",
		"p/B11sh92_2.java",
			    "\n" +
			    "package p;\n" +
			    "public class B11sh92_2 {    \n" +
			    "    void addBase(B11sh92_1 b) {\n" +
			    "    }\n" +
			    "}\n" +
			    "     \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh92.java (at line 6)\n" + 
    		"	protected class RBroken implements IB11sh92 playedBy IB11sh92 {\n" + 
    		"	                                   ^^^^^^^^\n" + 
    		"IB11sh92 cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"2. WARNING in TeamB11sh92.java (at line 6)\n" + 
    		"	protected class RBroken implements IB11sh92 playedBy IB11sh92 {\n" + 
    		"	                                                     ^^^^^^^^\n" + 
    		"When binding interface IB11sh92 as base of RBroken:\n" + 
    		"Note that some features like callin bindings are not yet supported in this situation (OTJLD 2.1.1).\n" + 
    		"----------\n");
    }

    // callin w/ one-way parameter mapping
    // B.1.1-otjld-sh-93
    public void testB11_sh93() {
       
       runConformTest(
            new String[] {
		"TeamB11sh93.java",
			    "\n" +
			    "public team class TeamB11sh93 {\n" +
			    "    protected class R0 playedBy B11sh93 {\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin int rm(String s, int iR) {\n" +
			    "            System.out.print(s);\n" +
			    "            return iR*2;\n" +
			    "        }\n" +
			    "        int rm(String s, int iR) <- replace int bm0(int iB) with {\n" +
			    "            s <- \"test\",\n" +
			    "            iR <- iB\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11sh93().activate();\n" +
			    "        System.out.print(new B11sh93().bm0(3));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"B11sh93.java",
			    "\n" +
			    "public class B11sh93 {\n" +
			    "    public int bm0(int iB) {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "test6");
    }

    // syntax error caused ICE in PredicateGenerator.linkPredicates
    // B.1.1-otjld-sh-94
    public void testB11_sh94() {
        runNegativeTest(
            new String[] {
		"TeamB11sh94.java",
			    "\n" +
			    "public team class TeamB11sh94 {\n" +
			    "  protected class R playedBy TB11sh94 \n" +
			    "    base when activated // bug\n" +
			    "  {\n" +
			    "    callin void olleh() {\n" +
			    "      base.olleh();\n" +
			    "    }\n" +
			    "    olleh <- replace hello;\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TB11sh94.java",
			    "\n" +
			    "public class TB11sh94 {\n" +
			    "  void hello() {}\n" +
			    "}\n" +
			    "  \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh94.java (at line 4)\n" + 
    		"	base when activated // bug\n" + 
    		"	^^^^^^^^^^^^^^^^^^^\n" + 
    		"Syntax error on tokens, delete these tokens\n" + 
    		"----------\n");
    }
    
    // Bug 350318 - [compiler] Erroneous name clash error in @Override methods
    // originally reported by André Lehmann
    public void testB11_sh95() {
    	compileOrder = new String[][] { {"Visitor.java"}, {"Caller.java"}};
    	Map options = getCompilerOptions();
    	options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
        runTestExpectingWarnings(
            new String[] {
		"Caller.java",
			    "\n" + 
			    "public class Caller {\n" + 
			    "	private static class VisitorImpl extends VisitorAdapter<Object> {\n" + 
			    "		\n" + 
			    "		@Override\n" + 
			    "		public Parameter1 visit(Long r, Object a) {\n" + 
			    "			return null;\n" + 
			    "		}\n" + 
			    "		\n" + 
			    "		@Override\n" + 
			    "		public Parameter1 visit(Integer r, Object a) {\n" + 
			    "			return null;\n" + 
			    "		}\n" + 
			    "		\n" + 
			    "		@Override\n" + 
			    "		public Parameter1 visit(Number r, Object a) {\n" + 
			    "			return null;\n" + 
			    "		}\n" + 
			    "	}\n" + 
			    "\n" + 
			    "	private void accept(Visitor<Parameter1, Parameter2> v) {\n" + 
			    "		v.visit(1, new Parameter2());\n" + 
			    "	}\n" + 
			    "	\n" + 
			    "	public void start() {\n" + 
			    "	}\n" + 
			    "}\n",
		"Visitor.java",
			    "public interface Visitor<R, A> {\n" + 
			    "\n" + 
			    "	R visit(Long r, A a);\n" + 
			    "\n" + 
			    "	R visit(Integer r, A a);\n" + 
			    "\n" + 
			    "	R visit(Number r, A a);\n" + 
			    "}\n" + 
			    "class Parameter1 {\n" + 
			    "	\n" + 
			    "}\n" + 
			    "class Parameter2 {\n" + 
			    "	\n" + 
			    "}\n" +
			    "abstract class VisitorAdapter<A> implements Visitor<Parameter1, A> {\n" + 
			    "\n" + 
			    "	public Parameter1 visit(Long r, A a) {\n" + 
			    "		return null;\n" + 
			    "	}\n" + 
			    "\n" + 
			    "	public Parameter1 visit(Integer r, A a) {\n" + 
			    "		return null;\n" + 
			    "	}\n" + 
			    "\n" + 
			    "	public Parameter1 visit(Number r, A a) {\n" + 
			    "		return null;\n" + 
			    "	}\n" + 
			    "	\n" + 
			    "}\n"
            },
            "----------\n" + 
    		"1. WARNING in Caller.java (at line 3)\n" + 
    		"	private static class VisitorImpl extends VisitorAdapter<Object> {\n" + 
    		"	                     ^^^^^^^^^^^\n" + 
    		"The type Caller.VisitorImpl is never used locally\n" + 
    		"----------\n" + 
    		"2. WARNING in Caller.java (at line 21)\n" + 
    		"	private void accept(Visitor<Parameter1, Parameter2> v) {\n" + 
    		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
    		"The method accept(Visitor<Parameter1,Parameter2>) from the type Caller is never used locally\n" + 
    		"----------\n",
    		options);
    }

    // witness for NPE in AstGenerator.baseclassReference(TypeBinding, boolean) from SerializationGenerator.fillRestoreRole(TypeDeclaration, FieldDeclaration[])
    public void testB11_sh96() {
        Map customOptions = getCompilerOptions();
        customOptions.put(JavaCore.COMPILER_COMPLIANCE, "1.3");
        customOptions.put(JavaCore.COMPILER_SOURCE, "1.3");
        customOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "1.3");
        runNegativeTest(
            new String[] {
		"TB11sh4.java",
			    "\n" +
			    "public class TB11sh4 {}    \n" +
			    "    \n",
		"TeamB11sh4_1.java",
			    "\n" +
			    "public team class TeamB11sh4_1 implements java.io.Serializable {\n" +
			    "    protected TeamB11sh4_1 (Role1 r) { \n" +
			    "    } \n" +
			    "    public class Role1 playedBy TB11sh4 {} \n" +
			    "}\n"
            },
            "----------\n" + 
    		"1. ERROR in TeamB11sh4_1.java (at line 2)\n" + 
    		"	public team class TeamB11sh4_1 implements java.io.Serializable {\n" + 
    		"	                  ^^^^^^^^^^^\n" + 
    		"Name clash: The method restoreRole(Class<?>, Object) of type TeamB11sh4_1 has the same erasure as restoreRole(Class, Object) of type Team but does not override it\n" + 
    		"----------\n" + 
    		"2. WARNING in TeamB11sh4_1.java (at line 2)\n" + 
    		"	public team class TeamB11sh4_1 implements java.io.Serializable {\n" + 
    		"	                  ^^^^^^^^^^^^\n" + 
    		"The serializable class TeamB11sh4_1 does not declare a static final serialVersionUID field of type long\n" + 
    		"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions,
            true/*generateOutput*/,
            false/*showCategory*/,
            false/*showWarningToken*/);    
    }
    
    // Bug 366597 - [compiler] NPE with role ifc wrongly interpreted as a team
    public void testB11_sh97() {
    	runNegativeTest(new String[] {
    			"t/T1.java",
    			"package t;\n" +
    			"import base b.Base;\n" +
    			"public team class T1  {\n" +
    			"    protected interface IR\n" +
    			"    public class R2 playedBy Base {\n" +
    			"        void bar() {\n" +
    			"            this.foo();\n" +
    			"        }\n" +
    			"    }\n" +
    			"}\n",
    			"b/Base.java",
    			"package b;\n" +
    			"public class Base { void foo() {} }\n"
    	}, 
		"----------\n" +
		"1. ERROR in t\\T1.java (at line 4)\n" +
		"	protected interface IR\n" +
		"	                    ^^\n" +
		"Syntax error, insert \"InterfaceBody\" to complete ClassBodyDeclarations\n" +
		"----------\n" +
		"2. ERROR in t\\T1.java (at line 5)\n" +
		"	public class R2 playedBy Base {\n" +
		"	             ^^\n" +
		"Member types not allowed in regular roles. Mark class t.T1.IR as a team if R2 should be its role (OTJLD 1.5(a,b)). \n" +
		"----------\n" +
		"3. ERROR in t\\T1.java (at line 7)\n" +
		"	this.foo();\n" +
		"	^^^^\n" +
		"Missing anchor (team instance) for role type t.T1.IR.R2 outside its team context (OTJLD 1.2.2(b)).\n" +
		"----------\n" +
		"4. WARNING in t\\T1.java (at line 7)\n" +
		"	this.foo();\n" +
		"	^^^^^^^^^^\n" +
		"Access restriction of method foo() in type b.Base is overridden by this method binding (OTJLD 3.4(a)).\n" +
		"----------\n" +
		"5. ERROR in t\\T1.java (at line 7)\n" +
		"	this.foo();\n" +
		"	^^^^^^^^^^\n" +
		"Unresolved self call foo() is implicitly bound by an inferred callout (OTJLD 3.1(j)).\n" +
		"----------\n");
    }

    // Bug 366597 - [compiler] NPE with role ifc wrongly interpreted as a team
    public void testB11_sh98() {
    	runNegativeTest(new String[] {
    			"t/T1.java",
    			"package t;\n" +
    			"import base b.Base;\n" +
    			"public team class T1 extends T0 {\n" +
    			"    protected team interface IR {}\n" +
    			"    @Override\n" +
    			"    protected class R2 playedBy Base {\n" +
    			"        void bar() {\n" +
    			"            this.foo();\n" +
    			"        }\n" +
    			"    }\n" +
    			"}\n",
    			"t/T0.java",
    			"package t;\n" +
    			"public team class T0 {\n" +
    			"    protected class R2 {}" +
    			"}\n",
    			"b/Base.java",
    			"package b;\n" +
    			"public class Base { void foo() {} }\n"
    	}, 
		"----------\n" + 
		"1. ERROR in t\\T1.java (at line 4)\n" + 
		"	protected team interface IR {}\n" + 
		"	                         ^^\n" + 
		"Illegal modifier for the member interface IR; only public, protected, private, static & abstract are permitted\n" + 
		"----------\n" + 
		"2. WARNING in t\\T1.java (at line 8)\n" + 
		"	this.foo();\n" + 
		"	^^^^^^^^^^\n" + 
		"Access restriction of method foo() in type b.Base is overridden by this method binding (OTJLD 3.4(a)).\n" + 
		"----------\n" + 
		"3. ERROR in t\\T1.java (at line 8)\n" + 
		"	this.foo();\n" + 
		"	^^^^^^^^^^\n" + 
		"Unresolved self call foo() is implicitly bound by an inferred callout (OTJLD 3.1(j)).\n" + 
		"----------\n");
    }

    // reported by Christine Hundt
    // B.1.1-otjld-ju-1
    public void testB11_ju1() {
       
       runConformTest(
            new String[] {
		"TB11ju1Main.java",
			    "\n" +
			    "public class TB11ju1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TB11ju1 b = new TB11ju1();\n" +
			    "        TeamB11ju1 t = new TeamB11ju1();\n" +
			    "        t.activate();\n" +
			    "        System.out.println(b.bm());\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"TB11ju1.java",
			    "\n" +
			    "public class TB11ju1 {\n" +
			    "    public int bm() {\n" +
			    "        System.out.println(\"TB11ju1.bm()\");        \n" +
			    "        return 8;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11ju1.java",
			    "\n" +
			    "public team class TeamB11ju1 {\n" +
			    "    public class RB11ju1 playedBy TB11ju1 {\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin void rm() {\n" +
			    "            if (false)\n" +
			    "              base.rm();\n" +
			    "            System.out.println(\"MyTeam.MyRole.rm()\");\n" +
			    "        }\n" +
			    "        \n" +
			    "        void rm() <- replace int bm() with {\n" +
			    "            7 -> result\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "MyTeam.MyRole.rm()\n" +
            "7");
    }

    // TPX-318: Callin binding doesn't work with static base method. Callin-binding (after) w/ static method.
    // B.1.1-otjld-ju-2
    public void testB11_ju2() {
       
       runConformTest(
            new String[] {
		"TB11ju2Main.java",
			    "\n" +
			    "        public class TB11ju2Main {\n" +
			    "            public static void main(String[] args) {\n" +
			    "                TeamB11ju2 t = new TeamB11ju2();\n" +
			    "                t.activate();\n" +
			    "                //TB11ju2 b = new TB11ju2();\n" +
			    "                TB11ju2.sm();\n" +
			    "            }\n" +
			    "        }\n" +
			    "    \n",
		"TB11ju2.java",
			    "\n" +
			    "        public class TB11ju2 {\n" +
			    "            public static void sm(){} \n" +
			    "        }\n" +
			    "    \n",
		"TeamB11ju2.java",
			    "\n" +
			    "		public team class TeamB11ju2 {\n" +
			    "			public class RB11ju2 playedBy TB11ju2 {\n" +
			    "				public static void srm() {\n" +
			    "					System.out.print(\"OK\");\n" +
			    "				} \n" +
			    "				srm <- after sm;\n" +
			    "			}\n" +
			    "		}\n" +
			    "	\n"
            },
            "OK");
    }

    // TPX-318: Callin binding doesn't work with static base method. Callin-binding (before) w/ static method.
    // B.1.1-otjld-ju-3
    public void testB11_ju3() {
       
       runConformTest(
            new String[] {
		"TB11ju3Main.java",
			    "\n" +
			    "        public class TB11ju3Main {\n" +
			    "            public static void main(String[] args) {\n" +
			    "                TeamB11ju3 t = new TeamB11ju3();\n" +
			    "                t.activate();\n" +
			    "                TB11ju3 b = new TB11ju3();\n" +
			    "                TB11ju3.sm();\n" +
			    "            }\n" +
			    "        }\n" +
			    "    \n",
		"TB11ju3.java",
			    "\n" +
			    "        public class TB11ju3 {\n" +
			    "            public static void sm(){} \n" +
			    "        }\n" +
			    "    \n",
		"TeamB11ju3.java",
			    "\n" +
			    "		public team class TeamB11ju3 {\n" +
			    "			public class RB11ju3 playedBy TB11ju3 {\n" +
			    "				public static void srm() {\n" +
			    "					System.out.print(\"OK\");\n" +
			    "				} \n" +
			    "				srm <- before sm;\n" +
			    "			}\n" +
			    "		}\n" +
			    "	\n"
            },
            "OK");
    }

    // TPX-318: Callin binding doesn't work with static base method. Callin binding w/ static method with an argument.
    // B.1.1-otjld-ju-4
    public void testB11_ju4() {
       
       runConformTest(
            new String[] {
		"TB11ju4Main.java",
			    "\n" +
			    "        public class TB11ju4Main {\n" +
			    "            public static void main(String[] args) {\n" +
			    "                TeamB11ju4 t = new TeamB11ju4();\n" +
			    "                t.activate();\n" +
			    "                TB11ju4 b = new TB11ju4();\n" +
			    "                TB11ju4.sm(0);\n" +
			    "            }\n" +
			    "        }\n" +
			    "    \n",
		"TB11ju4.java",
			    "\n" +
			    "        public class TB11ju4 {\n" +
			    "            public static void sm(int a){} \n" +
			    "        }\n" +
			    "    \n",
		"TeamB11ju4.java",
			    "\n" +
			    "		public team class TeamB11ju4 {\n" +
			    "			public class RB11ju4 playedBy TB11ju4 {\n" +
			    "				public static void srm(int b) {\n" +
			    "					System.out.print(\"OK\");\n" +
			    "				} \n" +
			    "				srm <- after sm;\n" +
			    "			}\n" +
			    "		}\n" +
			    "	\n"
            },
            "OK");
    }

    // TPX-318: Callin binding doesn't work with static base method. Callin binding (replace) w/ static method with an argument.
    // B.1.1-otjld-ju-5
    public void testB11_ju5() {
       
       runConformTest(
            new String[] {
		"TB11ju5Main.java",
			    "\n" +
			    "        public class TB11ju5Main {\n" +
			    "            public static void main(String[] args) {\n" +
			    "                TeamB11ju5 t = new TeamB11ju5();\n" +
			    "                t.activate();\n" +
			    "                TB11ju5 b = new TB11ju5();\n" +
			    "                TB11ju5.sm(0);\n" +
			    "            }\n" +
			    "        }\n" +
			    "    \n",
		"TB11ju5.java",
			    "\n" +
			    "        public class TB11ju5 {\n" +
			    "            public static void sm(int a){} \n" +
			    "        }\n" +
			    "    \n",
		"TeamB11ju5.java",
			    "\n" +
			    "		public team class TeamB11ju5 {\n" +
			    "			public class RB11ju5 playedBy TB11ju5 {\n" +
			    "				static callin void srm(int b) {\n" +
			    "					System.out.print(\"OK\");\n" +
			    "					base.srm(b);\n" +
			    "				} \n" +
			    "				srm <- replace sm;\n" +
			    "			}\n" +
			    "		}\n" +
			    "	\n"
            },
            "OK");
    }

    // Generic type in the base method signature causes a ClassFormatError. (reported in TPX-476)
    // B.1.1-otjld-ju-6
    public void testB11_ju6() {
       Map customOptions = getCompilerOptions();
           customOptions.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
                                      customOptions.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.GENERATE);
                                      customOptions.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.GENERATE);        
       runConformTest(
            new String[] {
		"TB11ju6Main.java",
			    "\n" +
			    "        public class TB11ju6Main {\n" +
			    "            public static void main(String[] args) {\n" +
			    "                TeamB11ju6 t = new TeamB11ju6();\n" +
			    "				t.activate();\n" +
			    "				TB11ju6<String> b = new TB11ju6<String>();\n" +
			    "				b.baseMethod(\"nothing\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    \n",
		"TB11ju6.java",
			    "\n" +
			    "        public class TB11ju6<G> {\n" +
			    "            public void baseMethod(G param){\n" +
			    "				return;\n" +
			    "			}\n" +
			    "        }\n" +
			    "    \n",
		"TeamB11ju6.java",
			    "\n" +
			    "		public team class TeamB11ju6 {\n" +
			    "		        @SuppressWarnings(\"rawtypes\")\n" +
			    "			public class RB11ju6 playedBy TB11ju6 {\n" +
			    "				void roleMethod(){\n" +
			    "					System.out.print(\"OK\");\n" +
			    "				}		\n" +
			    "				roleMethod <- after baseMethod;\n" +
			    "			}\n" +
			    "		}\n" +
			    "	\n"
            },
            "OK",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // Wrong calculation of base field type
    // B.1.1-otjld-ch-1
    public void testB11_ch1() {
       
       runConformTest(
            new String[] {
		"TB11ch1Main.java",
			    "\n" +
			    "public class TB11ch1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11ch1SuperAdapt().activate();\n" +
			    "        new TeamB11ch1SubAdapt().activate();\n" +
			    "        \n" +
			    "        TB11ch1Sub b = new TB11ch1Sub();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11ch1Super.java",
			    "\n" +
			    "public class TB11ch1Super {\n" +
			    "    void bm() {\n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11ch1Sub.java",
			    "\n" +
			    "public class TB11ch1Sub extends TB11ch1Super {}\n" +
			    "    \n",
		"TeamB11ch1SuperAdapt.java",
			    "\n" +
			    "public team class TeamB11ch1SuperAdapt {\n" +
			    "    public class RB11ch11 playedBy TB11ch1Super {}\n" +
			    "}\n" +
			    "    \n",
		"TeamB11ch1SubAdapt.java",
			    "\n" +
			    "public team class TeamB11ch1SubAdapt {\n" +
			    "    public class RB11ch11 playedBy TB11ch1Sub {\n" +
			    "        callin void rm() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.rm();\n" +
			    "            \n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // Wrong calculation of base field type
    // B.1.1-otjld-ch-2
    public void testB11_ch2() {
       
       runConformTest(
            new String[] {
		"TB11ch2Main.java",
			    "\n" +
			    "public class TB11ch2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11ch2SuperAdapt().activate();\n" +
			    "        new TeamB11ch2SubAdapt().activate();\n" +
			    "        \n" +
			    "        TB11ch2Sub b = new TB11ch2Sub();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11ch2Super.java",
			    "\n" +
			    "public class TB11ch2Super {\n" +
			    "    void bm() {\n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TB11ch2.java",
			    "\n" +
			    "public class TB11ch2 extends TB11ch2Super {}\n" +
			    "    \n",
		"TB11ch2Sub.java",
			    "\n" +
			    "public class TB11ch2Sub extends TB11ch2 {}\n" +
			    "    \n",
		"TeamB11ch2SuperAdapt.java",
			    "\n" +
			    "public team class TeamB11ch2SuperAdapt {\n" +
			    "    public class RB11ch21 playedBy TB11ch2Super {}\n" +
			    "}\n" +
			    "    \n",
		"TeamB11ch2SubAdapt.java",
			    "\n" +
			    "public team class TeamB11ch2SubAdapt {\n" +
			    "    public class RB11ch21Super playedBy TB11ch2 {}\n" +
			    "    public class RB11ch21 extends RB11ch21Super playedBy TB11ch2Sub {\n" +
			    "        callin void rm() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.rm();\n" +
			    "            \n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // Shorthand notation of callin binding fails
    // B.1.1-otjld-ch-3
    public void testB11_ch3() {
        runConformTest(
            new String[] {
		"TB11ch3_2.java",
			    "\n" +
			    "public class TB11ch3_2 {}\n" +
			    "    \n",
		"TeamB11ch3_1.java",
			    "\n" +
			    "public team class TeamB11ch3_1 {\n" +
			    "    public class RB11ch3_1 {\n" +
			    "        callin void rm(RB11ch3_2 r2) {\n" +
			    "            base.rm(r2);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class RB11ch3_2 {}\n" +
			    "}\n" +
			    "    \n",
		"TeamB11ch3_2.java",
			    "\n" +
			    "public team class TeamB11ch3_2 extends TeamB11ch3_1 {\n" +
			    "    public class RB11ch3_1 playedBy TB11ch3_1 {\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "    public class RB11ch3_2 playedBy TB11ch3_2 {}\n" +
			    "}\n" +
			    "    \n",
		"TB11ch3_1.java",
			    "\n" +
			    "public class TB11ch3_1 {\n" +
			    "    void bm(TB11ch3_2 b2) {}\n" +
			    "}\n" +
			    "    \n"
            });
    }

    // Same as B.1.1-otjld-ch-3, but with detailed notation
    // B.1.1-otjld-ch-3a
    public void testB11_ch3a() {
        runConformTest(
            new String[] {
		"TB11ch3a_2.java",
			    "\n" +
			    "public class TB11ch3a_2 {}\n" +
			    "    \n",
		"TeamB11ch3a_1.java",
			    "\n" +
			    "public team class TeamB11ch3a_1 {\n" +
			    "    public class RB11ch3a_1 {\n" +
			    "        callin void rm(RB11ch3a_2 r2) {\n" +
			    "            base.rm(r2);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class RB11ch3a_2 {}\n" +
			    "}\n" +
			    "    \n",
		"TeamB11ch3a_2.java",
			    "\n" +
			    "public team class TeamB11ch3a_2 extends TeamB11ch3a_1 {\n" +
			    "    public class RB11ch3a_1 playedBy TB11ch3a_1 {\n" +
			    "        void rm(RB11ch3a_2 r2) <- replace void bm(TB11ch3a_2 b2);\n" +
			    "    }\n" +
			    "    public class RB11ch3a_2 playedBy TB11ch3a_2 {}\n" +
			    "}\n" +
			    "    \n",
		"TB11ch3a_1.java",
			    "\n" +
			    "public class TB11ch3a_1 {\n" +
			    "    void bm(TB11ch3a_2 b2) {}\n" +
			    "}\n" +
			    "    \n"
            });
    }

    // Problem with a static field in a role an inheritance
    // B.1.1-otjld-ch-4
    public void testB11_ch4() {
       
       runConformTest(
            new String[] {
		"TeamB11ch4_2.java",
			    "\n" +
			    "public team class TeamB11ch4_2 extends TeamB11ch4_1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11ch4_2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11ch4_1.java",
			    "\n" +
			    "public team class TeamB11ch4_1 {\n" +
			    "    public class RB11ch4_1 {\n" +
			    "        private static final int MY_STATIC = 0;\n" +
			    "        protected void rm(int i) {\n" +
			    "            if (MY_STATIC > i) {\n" +
			    "                System.out.print(\"Problem!\");\n" +
			    "            } else {\n" +
			    "                System.out.print(\"OK\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new RB11ch4_1().rm(0);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // No problem with a static field in a role without inheritance
    // B.1.1-otjld-ch-4a
    public void testB11_ch4a() {
        runConformTest(
            new String[] {
		"TeamB11ch4a_1.java",
			    "\n" +
			    "public team class TeamB11ch4a_1 {\n" +
			    "    public class RB11ch4a_1 {\n" +
			    "        private static final int MY_STATIC = 0;\n" +
			    "        // method just added to avoid unused warning:\n" +
			    "        void rm(int i) {\n" +
			    "            if (MY_STATIC > i) {\n" +
			    "                System.out.print(\"Problem!\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            });
    }

    // Problem with a static field in a role an inheritance - additional static field in the subrole
    // B.1.1-otjld-ch-5
    public void testB11_ch5() {
       
       runConformTest(
            new String[] {
		"TeamB11ch5_2.java",
			    "\n" +
			    "public team class TeamB11ch5_2 extends TeamB11ch5_1 {\n" +
			    "    public class RB11ch5_1 {\n" +
			    "        final static String ERROR_VALUE = \"\";\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamB11ch5_2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamB11ch5_1.java",
			    "\n" +
			    "public team class TeamB11ch5_1 {\n" +
			    "    public class RB11ch5_1 {\n" +
			    "        private static final int MY_STATIC = 0;\n" +
			    "        protected void rm() {\n" +
			    "            if (MY_STATIC > 0) {\n" +
			    "                System.out.print(\"Problem!\");\n" +
			    "            } else {\n" +
			    "                System.out.print(\"OK\");\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new RB11ch5_1().rm();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // Problem with a static field in a role and inheritance - non-constant initialization of static field
    // B.1.1-otjld-ch-5a
    public void testB11_ch5a() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11ch5a.java",
			    "\n" +
			    "public team class TeamB11ch5a {\n" +
			    "    public class RB11ch5a {\n" +
			    "        private static final int MY_STATIC = gv();\n" +
			    "        private static int gv() { return 3; }\n" +
			    "        static int access() { return MY_STATIC; }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1.2.1(g)");
    }

    // Problem with a static field in a role and inheritance - static initializer
    // B.1.1-otjld-ch-5b
    public void testB11_ch5b() {
        runNegativeTestMatching(
            new String[] {
		"TeamB11ch5b.java",
			    "\n" +
			    "public team class TeamB11ch5b {\n" +
			    "    public class RB11ch5b {\n" +
			    "        private static final int MY_STATIC;\n" +
			    "        static {\n" +
			    "            MY_STATIC = gv();\n" +
			    "        }\n" +
			    "        private static int gv() { return 3; }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "static initializer");
    }

    // variant reported by Andreas Werner: public fields
    // B.1.1-otjld-ch-5c
    public void testB11_ch5c() {
        runNegativeTestMatching(
            new String[] {
		"test/TeamB11ch5c.java",
			    "\n" +
			    "package test;\n" +
			    "public team class TeamB11ch5c {\n" +
			    "	public class RoleClass playedBy TB11ch5c {\n" +
			    "		/*\n" +
			    "		 * string is initialized either by \n" +
			    "		 * 	 getLocalString() - is a role member\n" +
			    "		 * or by\n" +
			    "		 *   getBaseString()  - is a base member\n" +
			    "		 */\n" +
			    "		public static final String string = getLocalString();\n" +
			    "//		public static final String string = getBaseString();\n" +
			    "		\n" +
			    "		callin static String getOriginal(){ \n" +
			    "			return string;\n" +
			    "		}\n" +
			    "		getOriginal <- replace getOriginal;\n" +
			    "		\n" +
			    "		abstract static String getBaseString();\n" +
			    "		getBaseString -> getAdaptation;\n" +
			    "		\n" +
			    "		private static String getLocalString(){\n" +
			    "			return \"Adaptation\";\n" +
			    "		}\n" +
			    "	}\n" +
			    "	\n" +
			    "	public TeamB11ch5c () {\n" +
			    "		activate(ALL_THREADS); \n" +
			    "	}\n" +
			    "}\n" +
			    "    \n",
		"test/TB11ch5c.java",
			    "\n" +
			    "package test;\n" +
			    "public class TB11ch5c {\n" +
			    "	public static void print(){\n" +
			    "		System.out.println(getOriginal());\n" +
			    "	}\n" +
			    "	\n" +
			    "	private static String getOriginal(){\n" +
			    "		return \"Original\";\n" +
			    "	}\n" +
			    "	@SuppressWarnings(\"unused\")\n" +
			    "	private static String getAdaptation(){\n" +
			    "		return \"Adaptation\";\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n"
            },
            "1.2.1(g)");
    }

    // variant reported by Andreas Werner, correct by using a team field
    // B.1.1-otjld-ch-5d
    public void testB11_ch5d() {
       
       runConformTest(
            new String[] {
		"TB11ch5dMain.java",
			    "\n" +
			    "public class TB11ch5dMain {\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new test.TeamB11ch5d();\n" +
			    "		test.TB11ch5d.print();\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n",
		"test/TB11ch5d.java",
			    "\n" +
			    "package test;\n" +
			    "public class TB11ch5d {\n" +
			    "	public static void print(){\n" +
			    "		System.out.print(getOriginal());\n" +
			    "	}\n" +
			    "	\n" +
			    "	private static String getOriginal(){\n" +
			    "		return \"Original\";\n" +
			    "	}\n" +
			    "	@SuppressWarnings(\"unused\")\n" +
			    "	private static String getAdaptation(){\n" +
			    "		return \"Adaptation\";\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n",
		"test/TeamB11ch5d.java",
			    "\n" +
			    "package test;\n" +
			    "public team class TeamB11ch5d {\n" +
			    "//	public final String string = RoleClass.getLocalString();\n" +
			    "	public final String string = RoleClass.getBaseString();\n" +
			    "	public class RoleClass playedBy TB11ch5d {\n" +
			    "		/*\n" +
			    "		 * string is initialized either by \n" +
			    "		 * 	 getLocalString() - is a role member\n" +
			    "		 * or by\n" +
			    "		 *   getBaseString()  - is a base member\n" +
			    "		 */\n" +
			    "		\n" +
			    "		callin static String getOriginal(){ \n" +
			    "			return string;\n" +
			    "		}\n" +
			    "		getOriginal <- replace getOriginal;\n" +
			    "		\n" +
			    "		protected abstract static String getBaseString();\n" +
			    "		getBaseString -> getAdaptation;\n" +
			    "		\n" +
			    "		private static String getLocalString(){\n" +
			    "			return \"Adaptation\";\n" +
			    "		}\n" +
			    "	}\n" +
			    "	\n" +
			    "	public TeamB11ch5d () {\n" +
			    "		activate(ALL_THREADS); \n" +
			    "	}\n" +
			    "}\n" +
			    "    \n"
            },
            "Adaptation");
    }
    public void testBug372786() {
    	runNegativeTest(
    		new String[] {
    	"TBug372786.java",
    			"import jarred.TeamBug372786;\n" +
    			"public class TBug372786 {\n" +
    			"	TeamBug372786 t;\n" +
    			"   void test() {\n" +
    			"       t.run();\n" +
    			"   }\n" +
    			"}\n"
    		},
    		"----------\n" + 
    		"1. ERROR in TBug372786.java (at line 1)\n" + 
    		"	import jarred.TeamBug372786;\n" + 
    		"	^\n" + 
    		"The type notjarred.Missing cannot be resolved. It is indirectly referenced from required .class files\n" + 
    		"----------\n",
    		getClassLibraries("bug372786.jar"),
    		false);
    }

    // Bug 391876 - false-report (nested class): interface cannot be implemented more than once
    // bogus error seen: the interface NonUniqueIndex cannot be implemented more than once[...]
    public void testBug391876() {
    	runConformTest(
    		new String[] {
    			"p/Entity.java",
    				"package p;\n" +
					"public interface Entity{}\n",
    			"p/Index.java",
    				"package p;\n" +
    				"public interface Index<T extends Entity> {}\n",
    			"p/NonUniqueIndex.java",
	    			"package p;\n" +
	    			"public interface NonUniqueIndex<V extends Entity, T> extends Index<V> {}\n",
    			"p/User.java",
    				"package p;\n" +
    				"public interface User extends Entity {}",
    			"p/UserByCustomerIndex.java",
    				"package p;\n" +
    				"public interface UserByCustomerIndex extends NonUniqueIndex<User, UserByCustomerIndex.UserByCustomerKey> {\n" + 
    				"	class UserByCustomerKey {}\n" +
    				"}",
    			"p3/NonUniqueIndexImpl.java",
    				"package p3;\n" +
    				"import p.*;\n" +
    				"public class NonUniqueIndexImpl<V extends Entity, T> implements NonUniqueIndex<V, T> {}\n",
    		},
    		"");
    	runConformTest(
			new String[] {
				"p2/UserByCustomerIndexImpl.java",
				"package p2;\n" +
						"import p.*;\n" +
						"import p3.NonUniqueIndexImpl;\n" +
						"public class UserByCustomerIndexImpl \n" +
						"		extends NonUniqueIndexImpl<User, UserByCustomerIndex.UserByCustomerKey>\n" +
						"		implements UserByCustomerIndex {}\n",
			},
			"", null, false/*flush*/, null);
    }
}
