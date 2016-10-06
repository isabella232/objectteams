/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 IT Service Omikron GmbH and others.
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
 * 	  Thomas Dudziak - Initial API and implementation
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otjld.teamactivation;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

@SuppressWarnings("unchecked")
public class ExplicitTeamActivation extends AbstractOTJLDTest {
	
	public ExplicitTeamActivation(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test5215_explicitActivationForAllThreads"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return ExplicitTeamActivation.class;
	}

    // a team is explicitly activated and a callin binding is called in the activation context
    // 5.2.1-otjld-callin-invocation-1
    public void test521_callinInvocation1() {
       
       runConformTest(
            new String[] {
		"T521ci1Main.java",
			    "\n" +
			    "public class T521ci1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team521ci1 t = new Team521ci1();\n" +
			    "        T521ci1_2  o = new T521ci1_2();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            o.test();\n" +
			    "        }\n" +
			    "        o.test();\n" +
			    "        System.out.print(T521ci1_1.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T521ci1_1.java",
			    "\n" +
			    "public class T521ci1_1 {\n" +
			    "    private static String value = \"\";\n" +
			    "    public static void addValue(String txt) {\n" +
			    "        value += txt;\n" +
			    "    }\n" +
			    "    public static String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T521ci1_2.java",
			    "\n" +
			    "public class T521ci1_2 {\n" +
			    "    public void test() {\n" +
			    "        T521ci1_1.addValue(\"a\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team521ci1.java",
			    "\n" +
			    "public team class Team521ci1 {\n" +
			    "    public class Role521ci1_1 playedBy T521ci1_2 {\n" +
			    "        public void test() {\n" +
			    "            T521ci1_1.addValue(\"b\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role521ci1_2 extends Role521ci1_1 {\n" +
			    "        test <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "baa");
    }

    // a team is explicitly activated and a callin binding is called in the activation context
    // 5.2.1-otjld-callin-invocation-2
    public void test521_callinInvocation2() {
       
       runConformTest(
            new String[] {
		"T521ci2Main.java",
			    "\n" +
			    "public class T521ci2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team521ci2_2 t = new Team521ci2_2();\n" +
			    "        T521ci2_2    o = new T521ci2_2();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        o.test();\n" +
			    "        t.deactivate();\n" +
			    "        o.test();\n" +
			    "        System.out.print(T521ci2_1.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T521ci2_1.java",
			    "\n" +
			    "public class T521ci2_1 {\n" +
			    "    private static String value = \"\";\n" +
			    "    public static void addValue(String txt) {\n" +
			    "        value += txt;\n" +
			    "    }\n" +
			    "    public static String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T521ci2_2.java",
			    "\n" +
			    "public class T521ci2_2 {\n" +
			    "    public void test() {\n" +
			    "        T521ci2_1.addValue(\"a\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team521ci2_1.java",
			    "\n" +
			    "public team class Team521ci2_1 {\n" +
			    "    public class Role521ci2 playedBy T521ci2_2 {\n" +
			    "        public String addValue(String arg) {\n" +
			    "            T521ci2_1.addValue(arg);\n" +
			    "            return T521ci2_1.getValue();\n" +
			    "        }\n" +
			    "        String addValue(String arg) <- after void test() with {\n" +
			    "            arg <- \"b\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"Team521ci2_2.java",
			    "\n" +
			    "public team class Team521ci2_2 extends Team521ci2_1 {}\n" +
			    "    \n"
            },
            "aba");
    }

    // a team is explicitly activated and a callin binding is called in the activation context
    // 5.2.1-otjld-callin-invocation-3
    public void test521_callinInvocation3() {
       
       runConformTest(
            new String[] {
		"T521ci3Main.java",
			    "\n" +
			    "public class T521ci3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team521ci3 t = new Team521ci3();\n" +
			    "        T521ci3    o = new T521ci3();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        System.out.print(o);\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T521ci3.java",
			    "\n" +
			    "public class T521ci3 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team521ci3.java",
			    "\n" +
			    "public team class Team521ci3 {\n" +
			    "    public class Role521ci3 playedBy T521ci3 {\n" +
			    "        callin String getValue() {\n" +
			    "            base.getValue();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        String getValue() <- replace String toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "ba");
    }

    // a team is explicitly activated and a callin binding is called in the activation context
    // 5.2.1-otjld-callin-invocation-4
    public void test521_callinInvocation4() {
       
       runConformTest(
            new String[] {
		"T521ci4Main.java",
			    "\n" +
			    "public class T521ci4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team521ci4_2 t = new Team521ci4_2();\n" +
			    "        T521ci4      o = new T521ci4();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            System.out.print(o.test());\n" +
			    "        }\n" +
			    "        System.out.print(o.test());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T521ci4.java",
			    "\n" +
			    "public class T521ci4 {\n" +
			    "    public String test() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team521ci4_1.java",
			    "\n" +
			    "public abstract team class Team521ci4_1 {\n" +
			    "    public abstract class Role521ci4_1 playedBy T521ci4 {\n" +
			    "        callin String test() {\n" +
			    "            base.test();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        test <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team521ci4_2.java",
			    "\n" +
			    "public team class Team521ci4_2 extends Team521ci4_1 {\n" +
			    "    public class Role521ci4_2 extends Role521ci4_1 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "ba");
    }

    // a team is explicitly activated and a callin binding is called in the activation context
    // Bug 326416 -  [compiler] within() not working for team extending non-team
    public void test521_callinInvocation5() {
       
       runConformTest(
            new String[] {
		"T521ci5Main.java",
			    "\n" +
			    "public class T521ci5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team521ci5 t = new Team521ci5();\n" +
			    "        T521ci5_2  o = new T521ci5_2();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            o.test();\n" +
			    "        }\n" +
			    "        o.test();\n" +
			    "        System.out.print(T521ci5_1.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T521ci5_1.java",
			    "\n" +
			    "public class T521ci5_1 {\n" +
			    "    private static String value = \"\";\n" +
			    "    public static void addValue(String txt) {\n" +
			    "        value += txt;\n" +
			    "    }\n" +
			    "    public static String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T521ci5_2.java",
			    "\n" +
			    "public class T521ci5_2 {\n" +
			    "    public void test() {\n" +
			    "        T521ci5_1.addValue(\"a\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TSuper521ci5.java",
				"public class TSuper521ci5 {}\n",
		"Team521ci5.java",
			    "\n" +
			    "public team class Team521ci5 extends TSuper521ci5 {\n" +
			    "    public class Role521ci5_1 playedBy T521ci5_2 {\n" +
			    "        public void test() {\n" +
			    "            T521ci5_1.addValue(\"b\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role521ci5_2 extends Role521ci5_1 {\n" +
			    "        test <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "baa");
    }

    // invoking a role method activates the team temporarily
    // 5.2.2-otjld-role-method-invocation-activates-team
    public void test522_roleMethodInvocationActivatesTeam() {
       
       runConformTest(
            new String[] {
		"T522rmiatMain.java",
			    "\n" +
			    "public class T522rmiatMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team522rmiat t = new Team522rmiat();\n" +
			    "        Role522rmiat_1<@t> r = t.new Role522rmiat_1();\n" +
			    "        T522rmiat          o = new T522rmiat();\n" +
			    "\n" +
			    "        System.out.print(r.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T522rmiat.java",
			    "\n" +
			    "public class T522rmiat {\n" +
			    "    public String test() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team522rmiat.java",
			    "\n" +
			    "import org.objectteams.ImplicitTeamActivation;\n" +
			    "public team class Team522rmiat {\n" +
			    "    @ImplicitTeamActivation\n" +
			    "    public class Role522rmiat_1 {\n" +
			    "        public String test(T522rmiat obj) {\n" +
			    "            // should invoke the callin replace binding as the team is currently active\n" +
			    "            return obj.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role522rmiat_2 playedBy T522rmiat {\n" +
			    "        callin String test() {\n" +
			    "            base.test();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        test <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "b");
    }

    // a callin binding is not called when an unrelated team is active
    // 5.2.3-otjld-callin-in-other-team-context-1
    public void test523_callinInOtherTeamContext1() {
       
       runConformTest(
            new String[] {
		"T523ciotc1Main.java",
			    "\n" +
			    "public class T523ciotc1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team523ciotc1_2 t = new Team523ciotc1_2();\n" +
			    "        T523ciotc1      o = new T523ciotc1();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T523ciotc1.java",
			    "\n" +
			    "public class T523ciotc1 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team523ciotc1_1.java",
			    "\n" +
			    "public team class Team523ciotc1_1 {\n" +
			    "    public class Role523ciotc1 playedBy T523ciotc1 {\n" +
			    "        callin String getValue() {\n" +
			    "            base.getValue();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        getValue <- replace toString;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team523ciotc1_2.java",
			    "\n" +
			    "public team class Team523ciotc1_2 {}\n" +
			    "    \n"
            },
            "a");
    }

    // a callin binding is not called when a base team is active
    // 5.2.3-otjld-callin-in-other-team-context-2
    public void test523_callinInOtherTeamContext2() {
       
       runConformTest(
            new String[] {
		"T523ciotc2Main.java",
			    "\n" +
			    "public class T523ciotc2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team523ciotc2_1 t = new Team523ciotc2_1();\n" +
			    "        T523ciotc2      o = new T523ciotc2();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            System.out.print(o.test());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T523ciotc2.java",
			    "\n" +
			    "public class T523ciotc2 {\n" +
			    "    public String test() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team523ciotc2_1.java",
			    "\n" +
			    "public team class Team523ciotc2_1 {\n" +
			    "    public class Role523ciotc2 playedBy T523ciotc2 {\n" +
			    "        callin String test() {\n" +
			    "            base.test();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team523ciotc2_2.java",
			    "\n" +
			    "public team class Team523ciotc2_2 extends Team523ciotc2_1 {\n" +
			    "    public class Role523ciotc2 {\n" +
			    "        String test() <- replace String test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "a");
    }

    // a callin binding is not called when not within a team activation context
    // 5.2.4-otjld-callin-when-team-is-inactive-1
    public void test524_callinWhenTeamIsInactive1() {
       
       runConformTest(
            new String[] {
		"T524cwtii1Main.java",
			    "\n" +
			    "public class T524cwtii1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team524cwtii1 t = new Team524cwtii1();\n" +
			    "        T524cwtii1    o = new T524cwtii1();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T524cwtii1.java",
			    "\n" +
			    "public class T524cwtii1 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team524cwtii1.java",
			    "\n" +
			    "public team class Team524cwtii1 {\n" +
			    "    public class Role524cwtii1 playedBy T524cwtii1 {\n" +
			    "        callin String getValue() {\n" +
			    "            base.getValue();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        String getValue() <- replace String toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "a");
    }

    // a callin binding is not called when not within a team activation context
    // 5.2.4-otjld-callin-when-team-is-inactive-2
    public void test524_callinWhenTeamIsInactive2() {
       
       runConformTest(
            new String[] {
		"T524cwtii2Main.java",
			    "\n" +
			    "public class T524cwtii2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team524cwtii2 t = new Team524cwtii2();\n" +
			    "        T524cwtii2_2  o = new T524cwtii2_2();\n" +
			    "\n" +
			    "        within (t) {}\n" +
			    "        o.test();\n" +
			    "        System.out.print(T524cwtii2_1.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T524cwtii2_1.java",
			    "\n" +
			    "public class T524cwtii2_1 {\n" +
			    "    private static String value = \"\";\n" +
			    "    public static void addValue(String txt) {\n" +
			    "        value += txt;\n" +
			    "    }\n" +
			    "    public static String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T524cwtii2_2.java",
			    "\n" +
			    "public class T524cwtii2_2 {\n" +
			    "    public void test() {\n" +
			    "        T524cwtii2_1.addValue(\"a\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team524cwtii2.java",
			    "\n" +
			    "public team class Team524cwtii2 {\n" +
			    "    public class Role524cwtii2 playedBy T524cwtii2_2 {\n" +
			    "        public void addValue() {\n" +
			    "            T524cwtii2_1.addValue(\"b\");\n" +
			    "        }\n" +
			    "        addValue <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "a");
    }

    // a within statement uses an invalid expression
    // 5.2.5-otjld-illegal-within-statement-1
    public void test525_illegalWithinStatement1() {
        runNegativeTest(
            new String[] {
		"T525iws1.java",
			    "\n" +
			    "public class T525iws1 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team525iws1.java",
			    "\n" +
			    "public team class Team525iws1 {\n" +
			    "    public class Role525iws1 playedBy T525iws1 {\n" +
			    "        public void test() {}\n" +
			    "        test <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T525iws1Main.java",
			    "\n" +
			    "public class T525iws1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team525iws1 t = new Team525iws1();\n" +
			    "        T525iws1    o = new T525iws1();\n" +
			    "\n" +
			    "        within (o) {\n" +
			    "            o.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a within statement uses an invalid expression
    // 5.2.5-otjld-illegal-within-statement-2
    public void test525_illegalWithinStatement2() {
        runNegativeTest(
            new String[] {
		"T525iws2.java",
			    "\n" +
			    "public class T525iws2 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team525iws2.java",
			    "\n" +
			    "public team class Team525iws2 {\n" +
			    "    public class Role525iws2 playedBy T525iws2 {\n" +
			    "        public void test() {}\n" +
			    "        test <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T525iws2Main.java",
			    "\n" +
			    "public class T525iws2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team525iws2 t = new Team525iws2();\n" +
			    "        T525iws2    o = new T525iws2();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        within ((Object)t) {\n" +
			    "            o.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a within statement uses an invalid expression
    // 5.2.5-otjld-illegal-within-statement-3
    public void test525_illegalWithinStatement3() {
        runNegativeTest(
            new String[] {
		"T525iws3.java",
			    "\n" +
			    "public class T525iws3 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team525iws3.java",
			    "\n" +
			    "public team class Team525iws3 {\n" +
			    "    public class Role525iws3 playedBy T525iws3 {\n" +
			    "        public void test() {}\n" +
			    "        test <- after test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T525iws3Main.java",
			    "\n" +
			    "public class T525iws3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team525iws3 t = new Team525iws3();\n" +
			    "        Role525iws3<@t>   r = t.new Role525iws3();\n" +
			    "        T525iws3          o = new T525iws3();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        within (r) {\n" +
			    "            o.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a within statement uses an invalid expression
    // 5.2.5-otjld-illegal-within-statement-4
    public void test525_illegalWithinStatement4() {
        runNegativeTest(
            new String[] {
		"T525iws4.java",
			    "\n" +
			    "public class T525iws4 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team525iws4.java",
			    "\n" +
			    "public team class Team525iws4 {\n" +
			    "    public class Role525iws4 playedBy T525iws4 {\n" +
			    "        public void test() {}\n" +
			    "        test <- after test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T525iws4Main.java",
			    "\n" +
			    "public class T525iws4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team525iws4 t = new Team525iws4();\n" +
			    "        T525iws4    o = new T525iws4();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        within (null) {\n" +
			    "            o.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a within statement uses an invalid expression
    // 5.2.5-otjld-illegal-within-statement-5
    public void test525_illegalWithinStatement5() {
        runNegativeTest(
            new String[] {
		"T525iws5.java",
			    "\n" +
			    "public class T525iws5 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team525iws5.java",
			    "\n" +
			    "public team class Team525iws5 {\n" +
			    "    public class Role525iws5 playedBy T525iws5 {\n" +
			    "        public void test() {}\n" +
			    "        test <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T525iws5Main.java",
			    "\n" +
			    "public class T525iws5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team525iws5 t = new Team525iws5();\n" +
			    "        T525iws5    o = new T525iws5();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        within (0) {\n" +
			    "            o.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // team expression can only be null
    // 5.2.5-otjld-illegal-within-statement-6
    public void test525_illegalWithinStatement6() {
        runTestExpectingWarnings(
            new String[] {
		"T525iws6.java",
			    "\n" +
			    "public class T525iws6 {\n" +
			    "    void foo() {\n" +
			    "        org.objectteams.Team nullTeam = null;\n" +
			    "        within (nullTeam) {\n" +
			    "            System.out.println(\"OUCH\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. WARNING in T525iws6.java (at line 5)\n" +
			"	within (nullTeam) {\n" +
			"	        ^^^^^^^^\n" +
			"Null pointer access: The variable nullTeam can only be null at this location\n" +
			"----------\n");
    }

    // team expression may be null
    // 5.2.5-otjld_disabled_illegal-within-statement-7
    // disabled due to https://bugs.eclipse.org/bugs/292478  -  Report potentially null across variable assignment
    public void _disabled_test525_illegalWithinStatement7() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
        runTestExpectingWarnings(
            new String[] {
		"T525iws7.java",
			    "\n" +
			    "public class T525iws7 {\n" +
			    "    void foo(org.objectteams.Team nullTeam) {\n" +
			    "        if (nullTeam == null) { }\n" +
			    "        within (nullTeam) {\n" +
			    "            System.out.println(\"OUCH\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "Potential null pointer access: The variable nullTeam may be null at this location");
    }

    // within a team/role hierarchy multiple roles callin-bind to the same base method as 'before'/'after'; the order of activation of the teams defines the invocation order of these bindings
    // NOTE: still need to define callin overriding along 'extends'
    // 5.2.6-otjld-activation-order
    public void test526_activationOrder() {
       
       runConformTest(
            new String[] {
		"T526aoMain.java",
			    "\n" +
			    "public class T526aoMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team526ao_2 t2 = new Team526ao_2();\n" +
			    "        Team526ao_3 t3 = new Team526ao_3();\n" +
			    "        T526ao_2    o  = new T526ao_2();\n" +
			    "\n" +
			    "        t3.activate();\n" +
			    "        within (t2) {\n" +
			    "            o.test();\n" +
			    "        }\n" +
			    "        o.test();\n" +
			    "        System.out.print(T526ao_1.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T526ao_1.java",
			    "\n" +
			    "public class T526ao_1 {\n" +
			    "    private static String value = \"\";\n" +
			    "    public static void addValue(String txt) {\n" +
			    "        value += txt;\n" +
			    "    }\n" +
			    "    public static String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T526ao_2.java",
			    "\n" +
			    "public class T526ao_2 {\n" +
			    "    public void test() {\n" +
			    "        T526ao_1.addValue(\"-\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team526ao_1.java",
			    "\n" +
			    "public team class Team526ao_1 {\n" +
			    "    public class Role526ao_1 playedBy T526ao_2 {\n" +
			    "        public void test1() {\n" +
			    "            T526ao_1.addValue(\"|1b|\");\n" +
			    "        }\n" +
			    "        public void test2() {\n" +
			    "            T526ao_1.addValue(\"|1a|\");\n" +
			    "        }\n" +
			    "        at:\n" +
			    "        test2 <- after  test;\n" +
			    "        bt:\n" +
			    "        test1 <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team526ao_2.java",
			    "\n" +
			    "public team class Team526ao_2 extends Team526ao_1 {\n" +
			    "    public class Role526ao_2 extends Role526ao_1 {\n" +
			    "        public void test3() {\n" +
			    "            T526ao_1.addValue(\"|2b|\");\n" +
			    "        }\n" +
			    "        public void test4() {\n" +
			    "            T526ao_1.addValue(\"|2a|\");\n" +
			    "        }\n" +
			    "        bt:\n" +
			    "        test3 <- before test;\n" +
			    "        at:\n" +
			    "        test4 <- after  test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team526ao_3.java",
			    "\n" +
			    "public team class Team526ao_3  {\n" +
			    "    public class Role526ao_2 playedBy T526ao_2 {\n" +
			    "        public void test1() {\n" +
			    "            T526ao_1.addValue(\"|3b|\");\n" +
			    "        }\n" +
			    "        public void test2() {\n" +
			    "            T526ao_1.addValue(\"|3a|\");\n" +
			    "        }\n" +
			    "        bt:\n" +
			    "        test1 <- before test;\n" +
			    "        at:\n" +
			    "        test2 <- after  test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "|2b||3b|-|3a||2a||3b|-|3a|");
    }

    // within a team/role hierarchy multiple roles callin-bind to the same base method as 'before'/'after'; the order of activation of the teams defines the invocation order of these bindings,
    // NOTE: still need to define callin overriding along 'extends'
    // 5.2.7-otjld-activation-order
    public void test527_activationOrder() {
       
       runConformTest(
            new String[] {
		"T527aoMain.java",
			    "\n" +
			    "public class T527aoMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team527ao_1 t1 = new Team527ao_1();\n" +
			    "        Team527ao_2 t2 = new Team527ao_2();\n" +
			    "        Team527ao_3 t3 = new Team527ao_3();\n" +
			    "        T527ao_2    o  = new T527ao_2();\n" +
			    "\n" +
			    "        t3.activate();\n" +
			    "        t1.activate();\n" +
			    "        within (t2) {\n" +
			    "            t3.deactivate();\n" +
			    "            o.test();\n" +
			    "        }\n" +
			    "        o.test();\n" +
			    "        System.out.print(T527ao_1.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T527ao_1.java",
			    "\n" +
			    "public class T527ao_1 {\n" +
			    "    private static String value = \"\";\n" +
			    "    public static void addValue(String txt) {\n" +
			    "        value += txt;\n" +
			    "    }\n" +
			    "    public static String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T527ao_2.java",
			    "\n" +
			    "public class T527ao_2 {\n" +
			    "    public void test() {\n" +
			    "        T527ao_1.addValue(\"-\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team527ao_1.java",
			    "\n" +
			    "public team class Team527ao_1 {\n" +
			    "    public class Role527ao_1 playedBy T527ao_2 {\n" +
			    "        public void test1() {\n" +
			    "            T527ao_1.addValue(\"|1b|\");\n" +
			    "        }\n" +
			    "        public void test2() {\n" +
			    "            T527ao_1.addValue(\"|1a|\");\n" +
			    "        }\n" +
			    "        at:\n" +
			    "        test2 <- after  test;\n" +
			    "        bt:\n" +
			    "        test1 <- before test;\n" +
			    "    }\n" +
			    "    public class Role527ao_2 extends Role527ao_1 {\n" +
			    "        public void test3() {\n" +
			    "            T527ao_1.addValue(\"|2b|\");\n" +
			    "        }\n" +
			    "        public void test4() {\n" +
			    "            T527ao_1.addValue(\"|2a|\");\n" +
			    "        }\n" +
			    "        bt:\n" +
			    "        test3 <- before test;\n" +
			    "        at:\n" +
			    "        test4 <- after  test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team527ao_2.java",
			    "\n" +
			    "public team class Team527ao_2 extends Team527ao_1 {\n" +
			    "    public class Role527ao_2 {\n" +
			    "        public void test3() {\n" +
			    "            T527ao_1.addValue(\"|3b|\");\n" +
			    "        }\n" +
			    "        public void test4() {\n" +
			    "            T527ao_1.addValue(\"|3a|\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team527ao_3.java",
			    "\n" +
			    "public team class Team527ao_3  {\n" +
			    "    public class Role527ao_2 playedBy T527ao_2 {\n" +
			    "        public void test1() {\n" +
			    "            T527ao_1.addValue(\"|4b|\");\n" +
			    "        }\n" +
			    "        public void test2() {\n" +
			    "            T527ao_1.addValue(\"|4a|\");\n" +
			    "        }\n" +
			    "        bt:\n" +
			    "        test1 <- before test;\n" +
			    "        at:\n" +
			    "        test2 <- after  test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "|3b||2b|-|2a||3a||2b|-|2a|");
    }

    // in nested activation contexts of the same team instance, deactivating an inner context does not make the team inactive
    // 5.2.8-otjld-nested-activation-contexts-1
    public void test528_nestedActivationContexts1() {
       
       runConformTest(
            new String[] {
		"T528nac1Main.java",
			    "\n" +
			    "public class T528nac1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team528nac1 t = new Team528nac1();\n" +
			    "        T528nac1    o = new T528nac1();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            System.out.print(o);\n" +
			    "            boolean wasActive = t.isActive();\n" +
			    "	    t.activate();\n" +
			    "            System.out.print(o);\n" +
			    "	    if (!wasActive)\n" +
			    "            	t.deactivate();\n" +
			    "            System.out.print(o);\n" +
			    "        }\n" +
			    "            System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T528nac1.java",
			    "\n" +
			    "public class T528nac1 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team528nac1.java",
			    "\n" +
			    "public team class Team528nac1 {\n" +
			    "    public class Role528nac1 playedBy T528nac1 {\n" +
			    "        callin String getValue() {\n" +
			    "            base.getValue();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        String getValue() <- replace String toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "bbba");
    }

    // in nested activation contexts of the same team instance, explicitly deactivating an inner context restores the previous activation state
    // 5.2.8-otjld-nested-activation-contexts-2
    public void test528_nestedActivationContexts2() {
       
       runConformTest(
            new String[] {
		"T528nac2Main.java",
			    "\n" +
			    "public class T528nac2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team528nac2 t = new Team528nac2();\n" +
			    "        T528nac2    o = new T528nac2();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        System.out.print(o);\n" +
			    "        within (t) {\n" +
			    "            System.out.print(o);\n" +
			    "        }\n" +
			    "        System.out.print(o);\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T528nac2.java",
			    "\n" +
			    "public class T528nac2 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team528nac2.java",
			    "\n" +
			    "public team class Team528nac2 {\n" +
			    "    public class Role528nac2 playedBy T528nac2 {\n" +
			    "        callin String getValue() {\n" +
			    "            base.getValue();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        String getValue() <- replace String toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "bbba");
    }

    // in nested activation contexts of the same team instance, deactivating an inner context makes the team inactive
    // 5.2.8-otjld-nested-activation-contexts-3
    public void test528_nestedActivationContexts3() {
       
       runConformTest(
            new String[] {
		"T528nac3Main.java",
			    "\n" +
			    "public class T528nac3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team528nac3 t = new Team528nac3();\n" +
			    "        T528nac3    o = new T528nac3();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        System.out.print(o);\n" +
			    "        t.activate();\n" +
			    "        System.out.print(o);\n" +
			    "        t.deactivate();\n" +
			    "        // this should have deactivated the team\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T528nac3.java",
			    "\n" +
			    "public class T528nac3 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team528nac3.java",
			    "\n" +
			    "public team class Team528nac3 {\n" +
			    "    public class Role528nac3 playedBy T528nac3 {\n" +
			    "        callin String getValue() {\n" +
			    "            base.getValue();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        String getValue() <- replace String toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "bba");
    }

    // in nested activation contexts of the same team instance, deactivating an inner context does not make the team inactive
    // 5.2.8-otjld-nested-activation-contexts-4
    public void test528_nestedActivationContexts4() {
       
       runConformTest(
            new String[] {
		"T528nac4Main.java",
			    "\n" +
			    "public class T528nac4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team528nac4 t = new Team528nac4();\n" +
			    "        T528nac4    o = new T528nac4();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            System.out.print(o);\n" +
			    "            within (t) {\n" +
			    "                System.out.print(o);\n" +
			    "            }\n" +
			    "            System.out.print(o);\n" +
			    "        }\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T528nac4.java",
			    "\n" +
			    "public class T528nac4 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team528nac4.java",
			    "\n" +
			    "public team class Team528nac4 {\n" +
			    "    public class Role528nac4 playedBy T528nac4 {\n" +
			    "        callin String getValue() {\n" +
			    "            base.getValue();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        String getValue() <- replace String toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "bbba");
    }

    // an exception is thrown in a within activation context that is catched outside of the context
    // 5.2.9-otjld-exception-from-within-activation-context-1
    public void test529_exceptionFromWithinActivationContext1() {
       
       runConformTest(
            new String[] {
		"T529efwac1Main.java",
			    "\n" +
			    "public class T529efwac1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team529efwac1 t = new Team529efwac1();\n" +
			    "        T529efwac1    o = new T529efwac1();\n" +
			    "\n" +
			    "        try\n" +
			    "        {\n" +
			    "            within (t) {\n" +
			    "                System.out.print(o);\n" +
			    "            }\n" +
			    "        }\n" +
			    "        catch (Exception ex) {\n" +
			    "            System.out.print(ex.getMessage());\n" +
			    "            // t should not be active anymore\n" +
			    "            System.out.print(o);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T529efwac1.java",
			    "\n" +
			    "public class T529efwac1 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team529efwac1.java",
			    "\n" +
			    "public team class Team529efwac1 {\n" +
			    "    public class Role529efwac1 playedBy T529efwac1 {\n" +
			    "        public String toString() {\n" +
			    "            throw new RuntimeException(\"b\");\n" +
			    "        }\n" +
			    "        toString <- after toString;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "ba");
    }

    // an exception is thrown in a activate/deactivate activation context that is catched outside of the context
    // 5.2.9-otjld-exception-from-within-activation-context-2
    public void test529_exceptionFromWithinActivationContext2() {
       
       runConformTest(
            new String[] {
		"T529efwac2Main.java",
			    "\n" +
			    "public class T529efwac2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team529efwac2 t = new Team529efwac2();\n" +
			    "        T529efwac2    o = new T529efwac2();\n" +
			    "\n" +
			    "        try\n" +
			    "        {\n" +
			    "            t.activate();\n" +
			    "            System.out.print(o.getValue2());\n" +
			    "            t.deactivate();\n" +
			    "        }\n" +
			    "        catch (Exception ex) {\n" +
			    "            // t should still be active as activate/deactivate are not safe wrt. to exceptions\n" +
			    "            System.out.print(o.getValue1());\n" +
			    "            t.deactivate();\n" +
			    "        }\n" +
			    "        System.out.print(o.getValue2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T529efwac2.java",
			    "\n" +
			    "public class T529efwac2 {\n" +
			    "    public String getValue1() {\n" +
			    "        return \"1\";\n" +
			    "    }\n" +
			    "    public String getValue2() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team529efwac2.java",
			    "\n" +
			    "public team class Team529efwac2 {\n" +
			    "    public class Role529efwac2 playedBy T529efwac2 {\n" +
			    "        callin String getValue1() {\n" +
			    "            base.getValue1();\n" +
			    "            return \"2\";\n" +
			    "        }\n" +
			    "        callin String getValue2() {\n" +
			    "            base.getValue2();\n" +
			    "            throw new RuntimeException();\n" +
			    "        }\n" +
			    "        getValue1 <- replace getValue1;\n" +
			    "        getValue2 <- replace getValue2;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "2a");
    }

    // deactivate is called for an inactive team
    // 5.2.10-otjld-deactivate-for-inactive-team-1
    public void test5210_deactivateForInactiveTeam1() {
       
       runConformTest(
            new String[] {
		"T5210dfit1Main.java",
			    "\n" +
			    "public class T5210dfit1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team5210dfit1 t = new Team5210dfit1();\n" +
			    "        T5210dfit1    o = new T5210dfit1();\n" +
			    "\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5210dfit1.java",
			    "\n" +
			    "public class T5210dfit1 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5210dfit1.java",
			    "\n" +
			    "public team class Team5210dfit1 {\n" +
			    "    public class Role5210dfit1 playedBy T5210dfit1 {\n" +
			    "        callin String getValue() {\n" +
			    "            base.getValue();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        getValue <- replace toString;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "a");
    }

    // deactivate is called for an inactive team
    // 5.2.10-otjld-deactivate-for-inactive-team-2
    public void test5210_deactivateForInactiveTeam2() {
       
       runConformTest(
            new String[] {
		"T5210dfit2Main.java",
			    "\n" +
			    "public class T5210dfit2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team5210dfit2 t = new Team5210dfit2();\n" +
			    "        T5210dfit2    o = new T5210dfit2();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        System.out.print(o);\n" +
			    "        t.deactivate();\n" +
			    "\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5210dfit2.java",
			    "\n" +
			    "public class T5210dfit2 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5210dfit2.java",
			    "\n" +
			    "public team class Team5210dfit2 {\n" +
			    "    public class Role5210dfit2 playedBy T5210dfit2 {\n" +
			    "        callin String getValue() {\n" +
			    "            base.getValue();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        getValue <- replace toString;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "ba");
    }

    // deactivate is called for an inactive team
    // 5.2.10-otjld-deactivate-for-inactive-team-3
    public void test5210_deactivateForInactiveTeam3() {
       
       runConformTest(
            new String[] {
		"T5210dfit3Main.java",
			    "\n" +
			    "public class T5210dfit3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team5210dfit3 t = new Team5210dfit3();\n" +
			    "        T5210dfit3    o = new T5210dfit3();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            System.out.print(o);\n" +
			    "        }\n" +
			    "\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5210dfit3.java",
			    "\n" +
			    "public class T5210dfit3 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5210dfit3.java",
			    "\n" +
			    "public team class Team5210dfit3 {\n" +
			    "    public class Role5210dfit3 playedBy T5210dfit3 {\n" +
			    "        callin String getValue() {\n" +
			    "            base.getValue();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        getValue <- replace toString;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "ba");
    }

    // an activation context is closed in the presense of another thread that also works with the object
    // 5.2.11-otjld-activation-context-and-thread
    public void test5211_activationContextAndThread() {
       
       runConformTest(
            new String[] {
		"T5211acatMain.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class T5211acatMain {\n" +
			    "    public static void main(String[] args) throws InterruptedException {\n" +
			    "        Team5211acat   t = new Team5211acat();\n" +
			    "        T5211acat_2    o = new T5211acat_2();\n" +
			    "        Thread5210acat r = new Thread5210acat(o);\n" +
			    "\n" +
			    "        System.out.print(o);\n" +
			    "        t.activate(Team.ALL_THREADS);\n" +
			    "        System.out.print(o);\n" +
			    "        r.start();\n" +
			    "        while (!T5211acat_1.isAllowedToReleaseContext()) {\n" +
			    "            Thread.sleep(10);\n" +
			    "        }\n" +
			    "        t.deactivate(Team.ALL_THREADS);\n" +
			    "        T5211acat_1.markContextReleased();\n" +
			    "        Thread.sleep(15);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5211acat_1.java",
			    "\n" +
			    "public class T5211acat_1 {\n" +
			    "    private static int contextStatus = 0;\n" +
			    "    public synchronized static void allowReleaseOfContext() {\n" +
			    "        contextStatus = 1;\n" +
			    "    }\n" +
			    "    public synchronized static boolean isAllowedToReleaseContext() {\n" +
			    "        return contextStatus == 1;\n" +
			    "    }\n" +
			    "    public synchronized static void markContextReleased() {\n" +
			    "        contextStatus = 2;\n" +
			    "    }\n" +
			    "    public synchronized static boolean isContextReleased() {\n" +
			    "        return contextStatus == 2;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5211acat_2.java",
			    "\n" +
			    "public class T5211acat_2 {\n" +
			    "    public String toString() {\n" +
			    "        return \"a\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Thread5210acat.java",
			    "\n" +
			    "public class Thread5210acat extends Thread {\n" +
			    "    private T5211acat_2 obj;\n" +
			    "    public Thread5210acat(T5211acat_2 obj) {\n" +
			    "        super(\"\");\n" +
			    "        this.obj = obj;\n" +
			    "    }\n" +
			    "    public void run() {\n" +
			    "        System.out.print(obj);\n" +
			    "        T5211acat_1.allowReleaseOfContext();\n" +
			    "        while (!T5211acat_1.isContextReleased()) {\n" +
			    "            try\n" +
			    "            {\n" +
			    "                sleep(5);\n" +
			    "            }\n" +
			    "            catch (InterruptedException ex)\n" +
			    "            {}\n" +
			    "        }\n" +
			    "        System.out.print(obj);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5211acat.java",
			    "\n" +
			    "public team class Team5211acat {\n" +
			    "    public class Role5211acat playedBy T5211acat_2 {\n" +
			    "        callin String getValue() {\n" +
			    "            base.getValue();\n" +
			    "            return \"b\";\n" +
			    "        }\n" +
			    "        getValue <- replace toString;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "abba");
    }

    // a within statement is executed while the team is explicitly active
    // 5.2.12-otjld-within-and-other-activation-1
    public void test5212_withinAndOtherActivation1() {
       
       runConformTest(
            new String[] {
		"T5212waoa1Main.java",
			    "\n" +
			    "public class T5212waoa1Main {\n" +
			    "	public static void main (String[] args) {\n" +
			    "		T5212waoa1 b = new T5212waoa1();\n" +
			    "		b.t();\n" +
			    "		Team5212waoa1 t = new Team5212waoa1();\n" +
			    "		t.activate();\n" +
			    "		b.t();\n" +
			    "		within (t) {\n" +
			    "			b.t();\n" +
			    "		}\n" +
			    "		b.t();\n" +
			    "		within (t) {\n" +
			    "			t.deactivate();\n" +
			    "			b.t();\n" +
			    "		}\n" +
			    "		b.t();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T5212waoa1.java",
			    "\n" +
			    "public class T5212waoa1 {\n" +
			    "	void t() {System.out.print(\"i\"); } // inactive\n" +
			    "}	\n" +
			    "	\n",
		"Team5212waoa1.java",
			    " \n" +
			    "public team class Team5212waoa1 {\n" +
			    "	protected class R playedBy T5212waoa1 {\n" +
			    "		callin void rm () {\n" +
			    "			System.out.print(\"a\"); // active\n" +
			    "		}\n" +
			    "		rm <- replace t;\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "iaaaia");
    }

    // a within statement is executed while the team is implicitly active
    // 5.2.12-otjld-within-and-other-activation-2
    public void test5212_withinAndOtherActivation2() {
       
       runConformTest(
            new String[] {
		"T5212waoa2Main.java",
			    "\n" +
			    "public class T5212waoa2Main {\n" +
			    "	public static void main (String[] args) {\n" +
			    "		T5212waoa2 b = new T5212waoa2();\n" +
			    "		b.t();\n" +
			    "		Team5212waoa2 t = new Team5212waoa2();\n" +
			    "		t.test(b);\n" +
			    "		b.t();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T5212waoa2.java",
			    "\n" +
			    "public class T5212waoa2 {\n" +
			    "	void t() {System.out.print(\"i\"); }\n" +
			    "}	\n" +
			    "	\n",
		"Team5212waoa2.java",
			    "\n" +
			    "import org.objectteams.ImplicitTeamActivation;\n" +
			    "public team class Team5212waoa2 {\n" +
			    "	protected class R playedBy T5212waoa2 {\n" +
			    "		callin void rm () {\n" +
			    "			System.out.print(\"a\");\n" +
			    "		}\n" +
			    "		rm <- replace t;\n" +
			    "	}\n" +
			    "        @ImplicitTeamActivation\n" +
			    "	public void test(T5212waoa2 b) {\n" +
			    "		b.t();\n" +
			    "		within (this) {\n" +
			    "			b.t();\n" +
			    "		}\n" +
			    "		b.t();\n" +
			    "		within (this) {\n" +
			    "			this.deactivate();\n" +
			    "			b.t();\n" +
			    "		}\n" +
			    "		b.t();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "iaaaiai");
    }

    // a within statement is executed while the team is implicitly active and calls another team method
    // 5.2.12-otjld-within-and-other-activation-3
    public void test5212_withinAndOtherActivation3() {
       
       runConformTest(
            new String[] {
		"T5212waoa3Main.java",
			    "\n" +
			    "public class T5212waoa3Main {\n" +
			    "	public static void main (String[] args) {\n" +
			    "		T5212waoa3 b = new T5212waoa3();\n" +
			    "		b.t();\n" +
			    "		Team5212waoa3 t = new Team5212waoa3();\n" +
			    "		t.test(b);\n" +
			    "		b.t();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T5212waoa3.java",
			    "\n" +
			    "public class T5212waoa3 {\n" +
			    "	void t() {System.out.print(\"i\"); }\n" +
			    "}	\n" +
			    "	\n",
		"Team5212waoa3.java",
			    "\n" +
			    "import org.objectteams.ImplicitTeamActivation;\n" +
			    "@ImplicitTeamActivation\n" +
			    "public team class Team5212waoa3 {\n" +
			    "	protected class R playedBy T5212waoa3 {\n" +
			    "		callin void rm () {\n" +
			    "			System.out.print(\"a\");\n" +
			    "		}\n" +
			    "		rm <- replace t;\n" +
			    "	}\n" +
			    "	public void test(T5212waoa3 b) {\n" +
			    "		b.t();\n" +
			    "		within (this) {\n" +
			    "			b.t();\n" +
			    "			test2(b);\n" +
			    "		}\n" +
			    "		b.t();\n" +
			    "	}\n" +
			    "	public void test2(T5212waoa3 b) {\n" +
			    "		b.t();\n" +
			    "		within (this) {\n" +
			    "			b.t();\n" +
			    "		}\n" +
			    "		b.t();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "iaaaaaai");
    }

    // a within block only activates the team for the current thread
    // 5.2.13-otjld-within-activation-and-threads-1
    public void test5213_withinActivationAndThreads1() {
       
       runConformTest(
            new String[] {
		"T5213waat1Main.java",
			    "\n" +
			    "public class T5213waat1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team5213waat1 t = new Team5213waat1();\n" +
			    "        T5213waat1 b = new T5213waat1();\n" +
			    "        within(t) {\n" +
			    "            // call b.bm() from another thread: ----->\n" +
			    "            Runnable bmc = new Runnable() {\n" +
			    "                public void run() {\n" +
			    "                    new T5213waat1().bm();\n" +
			    "                }\n" +
			    "            };\n" +
			    "            Thread baseMethodCallThread = new Thread(bmc);\n" +
			    "            baseMethodCallThread.start();\n" +
			    "            try {\n" +
			    "                baseMethodCallThread.join();\n" +
			    "            } catch (InterruptedException ie) {\n" +
			    "                ie.printStackTrace();\n" +
			    "            }\n" +
			    "            // <------\n" +
			    "            // call b.bm() from this thread:\n" +
			    "            b.bm();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5213waat1.java",
			    "\n" +
			    "public class T5213waat1 {\n" +
			    "    public void bm() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5213waat1.java",
			    "\n" +
			    "public team class Team5213waat1 {\n" +
			    "    public class MyRole playedBy T5213waat1 {\n" +
			    "        callin void rm() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // entering a within block while the team has been activated for ALL_THREADS (lazy)
    // 5.2.13-otjld-within-activation-and-threads-2
    public void test5213_withinActivationAndThreads2() {
       
       runConformTest(
            new String[] {
		"T5213waat2Main.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class T5213waat2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team5213waat2 t = new Team5213waat2();\n" +
			    "        T5213waat2 b = new T5213waat2();\n" +
			    "        t.activate(Team.ALL_THREADS);\n" +
			    "\n" +
			    "        within(t) {\n" +
			    "            // call b.bm() from another thread: ----->\n" +
			    "            Runnable bmc = new Runnable() {\n" +
			    "                public void run() {\n" +
			    "                    new T5213waat2().bm();\n" +
			    "                }\n" +
			    "            };\n" +
			    "            Thread baseMethodCallThread = new Thread(bmc);\n" +
			    "            baseMethodCallThread.start();\n" +
			    "            try {\n" +
			    "                baseMethodCallThread.join();\n" +
			    "            } catch (InterruptedException ie) {\n" +
			    "                ie.printStackTrace();\n" +
			    "            }\n" +
			    "            // <------\n" +
			    "            // call b.bm() from this thread:\n" +
			    "            b.bm();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5213waat2.java",
			    "\n" +
			    "public class T5213waat2 {\n" +
			    "    public void bm() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5213waat2.java",
			    "\n" +
			    "public team class Team5213waat2 {\n" +
			    "    public class MyRole playedBy T5213waat2 {\n" +
			    "        callin void rm() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "KK");
    }

    // global team deactivation permanently 'overrides' thread local activation
    // 5.2.14-otjld-explicit-per-thread-activation-1
    public void test5214_explicitPerThreadActivation1() {
       
       runConformTest(
            new String[] {
		"T5214epta1Main.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class T5214epta1Main {\n" +
			    "    static Team5214epta1 t;\n" +
			    "    static T5214epta1 b;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        t = new Team5214epta1();\n" +
			    "        b = new T5214epta1();\n" +
			    "        b.bm();\n" +
			    "        t.activate();\n" +
			    "        b.bm();\n" +
			    "        System.out.print(\"|\");\n" +
			    "        // call activate(Team.ALL_THREADS) from another thread: ----->\n" +
			    "        Runnable otherThread1 = new Runnable() {\n" +
			    "            public void run() {\n" +
			    "                b.bm();\n" +
			    "                t.activate(Team.ALL_THREADS);\n" +
			    "                b.bm();\n" +
			    "                System.out.print(\"|\");\n" +
			    "            }\n" +
			    "        };\n" +
			    "        Thread ot1 = new Thread(otherThread1);\n" +
			    "        ot1.start();\n" +
			    "        try {\n" +
			    "            ot1.join();\n" +
			    "        } catch (InterruptedException ie) {\n" +
			    "            ie.printStackTrace();\n" +
			    "        }\n" +
			    "        // <------\n" +
			    "        t.deactivate(Team.ALL_THREADS);\n" +
			    "        b.bm();\n" +
			    "        // call activate() from another thread: ----->\n" +
			    "        Runnable otherThread2 = new Runnable() {\n" +
			    "            public void run() {\n" +
			    "                t.activate();\n" +
			    "            }\n" +
			    "        };\n" +
			    "        Thread ot2 = new Thread(otherThread2);\n" +
			    "        ot2.start();\n" +
			    "        try {\n" +
			    "            ot2.join();\n" +
			    "        } catch (InterruptedException ie) {\n" +
			    "            ie.printStackTrace();\n" +
			    "        }\n" +
			    "        // <------\n" +
			    "        // call b.bm() from this thread:\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5214epta1.java",
			    "\n" +
			    "public class T5214epta1 {\n" +
			    "    public void bm() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5214epta1.java",
			    "\n" +
			    "public team class Team5214epta1 {\n" +
			    "    public class MyRole playedBy T5214epta1 {\n" +
			    "        callin void rm() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK|OK|OO");
    }

    // global team activation applies also for later started threads
    // 5.2.15-otjld-explicit-activation-for-all-threads-1
    public void test5215_explicitActivationForAllThreads1() {
       
       runConformTest(
            new String[] {
		"T5215eaat1Main.java",
			    "\n" +
			    "import static org.objectteams.Team.*;\n" +
			    "public class T5215eaat1Main {\n" +
			    "    static Team5215eaat1 t;\n" +
			    "    static T5215eaat1 b;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        t = new Team5215eaat1();\n" +
			    "        b = new T5215eaat1();\n" +
			    "        b.bm();\n" +
			    "        t.activate(ALL_THREADS);\n" +
			    "        b.bm();\n" +
			    "        System.out.print(\"|\");\n" +
			    "        t.deactivate(); // deactivation for this thread only!\n" +
			    "        b.bm();\n" +
			    "        // call bm() from another thread (team is still global activ): ----->\n" +
			    "        Runnable otherThread1 = new Runnable() {\n" +
			    "            public void run() {\n" +
			    "                b.bm();\n" +
			    "            }\n" +
			    "        };\n" +
			    "        Thread ot1 = new Thread(otherThread1);\n" +
			    "        ot1.start();\n" +
			    "        try {\n" +
			    "            ot1.join();\n" +
			    "        } catch (InterruptedException ie) {\n" +
			    "            ie.printStackTrace();\n" +
			    "        }\n" +
			    "        // <------\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5215eaat1.java",
			    "\n" +
			    "public class T5215eaat1 {\n" +
			    "    public void bm() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5215eaat1.java",
			    "\n" +
			    "public team class Team5215eaat1 {\n" +
			    "    public class MyRole playedBy T5215eaat1 {\n" +
			    "        callin void rm() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK|OK");
    }

    // global team activation applies also for later started threads - SUPER class of started thread implements 'Runnable'
    // 5.2.15-otjld-explicit-activation-for-all-threads-1a
    public void test5215_explicitActivationForAllThreads1a() {
       
       runConformTest(
            new String[] {
		"T5215eaat1aMain.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class T5215eaat1aMain {\n" +
			    "    static Team5215eaat1a t;\n" +
			    "    static T5215eaat1a b;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        t = new Team5215eaat1a();\n" +
			    "        b = new T5215eaat1a();\n" +
			    "        b.bm();\n" +
			    "        t.activate(Team.ALL_THREADS);\n" +
			    "        b.bm();\n" +
			    "        System.out.print(\"|\");\n" +
			    "        t.deactivate(); // deactivation for this thread only!\n" +
			    "        b.bm();\n" +
			    "        // call bm() from another thread (team is still global activ): ----->\n" +
			    "        T5215eaat1a_Runnable2 otherThread1 = new T5215eaat1a_Runnable2(b);\n" +
			    "        Thread ot1 = new Thread(otherThread1);\n" +
			    "        ot1.start();\n" +
			    "        try {\n" +
			    "            ot1.join();\n" +
			    "        } catch (InterruptedException ie) {\n" +
			    "            ie.printStackTrace();\n" +
			    "        }\n" +
			    "        // <------\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5215eaat1a.java",
			    "\n" +
			    "public class T5215eaat1a {\n" +
			    "    public void bm() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5215eaat1a.java",
			    "\n" +
			    "public team class Team5215eaat1a {\n" +
			    "    public class MyRole playedBy T5215eaat1a {\n" +
			    "        callin void rm() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5215eaat1a_Runnable1.java",
			    "\n" +
			    "public team class T5215eaat1a_Runnable1 implements Runnable {\n" +
			    "    \n" +
			    "    protected T5215eaat1a b;\n" +
			    "\n" +
			    "    public T5215eaat1a_Runnable1(T5215eaat1a _b) {\n" +
			    "        b = _b;\n" +
			    "    }\n" +
			    "\n" +
			    "    public void run() {\n" +
			    "\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5215eaat1a_Runnable2.java",
			    "\n" +
			    "public team class T5215eaat1a_Runnable2 extends T5215eaat1a_Runnable1 {\n" +
			    "    public T5215eaat1a_Runnable2(T5215eaat1a _b) {\n" +
			    "        super(_b);\n" +
			    "    }\n" +
			    "    public void run() {\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK|OK");
    }

    // deactivating a (lazy) global active team for a thread will activate it for all other threads
    // 5.2.15-otjld-explicit-activation-for-all-threads-2
    public void test5215_explicitActivationForAllThreads2() {
       
       runConformTest(
            new String[] {
		"T5215eaat2Main.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class T5215eaat2Main {\n" +
			    "    static Team5215eaat2 t;\n" +
			    "    static T5215eaat2 b;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        t = new Team5215eaat2();\n" +
			    "        b = new T5215eaat2();\n" +
			    "        b.bm();\n" +
			    "        t.activate(Team.ALL_THREADS);\n" +
			    "        b.bm();\n" +
			    "        System.out.print(\"|\");\n" +
			    "        // call bm() from another thread: ----->\n" +
			    "        Runnable otherThread1 = new Runnable() {\n" +
			    "            public void run() {\n" +
			    "                b.bm();\n" +
			    "            }\n" +
			    "        };\n" +
			    "        Thread ot1 = new Thread(otherThread1);\n" +
			    "        ot1.start();\n" +
			    "        try {\n" +
			    "            ot1.join();\n" +
			    "        } catch (InterruptedException ie) {\n" +
			    "            ie.printStackTrace();\n" +
			    "        }\n" +
			    "        // <------\n" +
			    "        // call bm() from yet another thread: ----->\n" +
			    "        Runnable otherThread2 = new Runnable() {\n" +
			    "            public void run() {\n" +
			    "                t.deactivate(); // deactivation for this thread only!\n" +
			    "                b.bm();\n" +
			    "            }\n" +
			    "        };\n" +
			    "        Thread ot2 = new Thread(otherThread2);\n" +
			    "        ot2.start();\n" +
			    "        try {\n" +
			    "            ot2.join();\n" +
			    "        } catch (InterruptedException ie) {\n" +
			    "            ie.printStackTrace();\n" +
			    "        }\n" +
			    "        System.out.print(\"|\");\n" +
			    "        try {\n" +
			    "            if (t.isActive(ot1) && !t.isActive(ot2))\n" +
			    "                System.out.print(\"NOK\");\n" +
			    "            else\n" +
			    "                System.out.print(\"NOTOK\");\n" +
			    "        } catch (IllegalThreadStateException itse) {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        // <------\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5215eaat2.java",
			    "\n" +
			    "public class T5215eaat2 {\n" +
			    "    public void bm() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5215eaat2.java",
			    "\n" +
			    "public team class Team5215eaat2 {\n" +
			    "    public class MyRole playedBy T5215eaat2 {\n" +
			    "        callin void rm() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK|KO|OK");
    }

    // deactivating a (lazy) global active team for a thread will activate it for all other threads
    // 5.2.15-otjld-explicit-activation-for-all-threads-2a
    public void test5215_explicitActivationForAllThreads2a() {
       
       runConformTest(
            new String[] {
		"T5215eaat2aMain.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class T5215eaat2aMain {\n" +
			    "    static Team5215eaat2a t;\n" +
			    "    static T5215eaat2a b;\n" +
			    "    static Thread ot1 = null;\n" +
			    "    static Thread ot2 = null;\n" +
			    "    static boolean finished = false;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        t = new Team5215eaat2a();\n" +
			    "        b = new T5215eaat2a();\n" +
			    "        b.bm();\n" +
			    "        t.activate(Team.ALL_THREADS);\n" +
			    "        b.bm();\n" +
			    "        System.out.print(\"|\");\n" +
			    "        // call bm() from another thread: ----->\n" +
			    "        Runnable otherThread1 = new Runnable() {\n" +
			    "            public void run() {\n" +
			    "                b.bm();\n" +
			    "                if (t.isActive(ot1))\n" +
			    "                    System.out.print(\"ACT\");\n" +
			    "                else\n" +
			    "                    System.out.print(\"NOTOK\");\n" +
			    "            }\n" +
			    "        };\n" +
			    "        ot1 = new Thread(otherThread1);\n" +
			    "        ot1.start();\n" +
			    "        try {\n" +
			    "            ot1.join();\n" +
			    "        } catch (InterruptedException ie) {\n" +
			    "            ie.printStackTrace();\n" +
			    "        }\n" +
			    "        // <------\n" +
			    "        // call bm() from yet another thread: ----->\n" +
			    "        Runnable otherThread2 = new Runnable() {\n" +
			    "            public void run() {\n" +
			    "                t.deactivate(); // deactivation for this thread only!\n" +
			    "                b.bm();\n" +
			    "                System.out.print(\"|\");\n" +
			    "                if (!t.isActive(ot2))\n" +
			    "                    System.out.print(\"NOTACT\");\n" +
			    "                else\n" +
			    "                    System.out.print(\"NOTOK\");\n" +
			    "            }\n" +
			    "        };\n" +
			    "        ot2 = new Thread(otherThread2);\n" +
			    "        ot2.start();\n" +
			    "        try {\n" +
			    "            ot2.join();\n" +
			    "        } catch (InterruptedException ie) {\n" +
			    "            ie.printStackTrace();\n" +
			    "        }\n" +
			    "        finished = true;\n" +
			    "        // <------\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5215eaat2a.java",
			    "\n" +
			    "public class T5215eaat2a {\n" +
			    "    public void bm() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5215eaat2a.java",
			    "\n" +
			    "public team class Team5215eaat2a {\n" +
			    "    public class MyRole playedBy T5215eaat2a {\n" +
			    "        callin void rm() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK|KACTO|NOTACT");
    }

    // deactivating a (lazy) global active team for a thread will activate it for all other threads INCLUDING the main thread
    // 5.2.15-otjld-explicit-activation-for-all-threads-3
    public void test5215_explicitActivationForAllThreads3() {
       
       runConformTest(
            new String[] {
		"T5215eaat3Main.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class T5215eaat3Main {\n" +
			    "    static Team5215eaat3 t;\n" +
			    "    static T5215eaat3 b;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        t = new Team5215eaat3();\n" +
			    "        b = new T5215eaat3();\n" +
			    "        b.bm();\n" +
			    "        t.activate(Team.ALL_THREADS);\n" +
			    "        b.bm();\n" +
			    "        System.out.print(\"|\");\n" +
			    "        // call bm() from yet another thread: ----->\n" +
			    "        Runnable otherThread1 = new Runnable() {\n" +
			    "            public void run() {\n" +
			    "                b.bm();\n" +
			    "                t.deactivate(); // deactivation for this thread only!\n" +
			    "                b.bm();\n" +
			    "            }\n" +
			    "        };\n" +
			    "        Thread ot1 = new Thread(otherThread1);\n" +
			    "        ot1.start();\n" +
			    "        try {\n" +
			    "            ot1.join();\n" +
			    "        } catch (InterruptedException ie) {\n" +
			    "            ie.printStackTrace();\n" +
			    "        }\n" +
			    "        // <------\n" +
			    "        System.out.print(\"|\");\n" +
			    "        // t has to be active for the main thread, too:\n" +
			    "        if (t.isActive()) \n" +
			    "            System.out.print(\"OK\");\n" +
			    "        else\n" +
			    "            System.out.print(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5215eaat3.java",
			    "\n" +
			    "public class T5215eaat3 {\n" +
			    "    public void bm() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5215eaat3.java",
			    "\n" +
			    "public team class Team5215eaat3 {\n" +
			    "    public class MyRole playedBy T5215eaat3 {\n" +
			    "        callin void rm() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK|KO|OK");
    }

    // using a Runnable.run() a thread creation is faked [see also  Bug 316696 -  [otre] OTRE doesn't know about all threads].
    public void test5215_explicitActivationForAllThreads3a() {
       
       runConformTest(
            new String[] {
		"T5215eaat3aMain.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class T5215eaat3aMain {\n" +
			    "    static Team5215eaat3a t;\n" +
			    "    static T5215eaat3a b;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        t = new Team5215eaat3a();\n" +
			    "        b = new T5215eaat3a();\n" +
			    "        b.bm();\n" + 									// not active
			    "        t.activate(Team.ALL_THREADS);\n" +
			    "        b.bm();\n" +									// active
			    "        System.out.print(\"|\");\n" +
			    "        // call bm() from a runnable: ----->\n" +
			    "        Runnable runnable = new Runnable() {\n" +
			    "            public void run() {\n" +
			    "                b.bm();\n" +
			    "            }\n" +
			    "        };\n" +
			    "		 t.deactivate();\n" +
			    "        b.bm();\n" + 									// not active for this thread
			    "		 runnable.run();\n" +							// bm() during run(): not active for this thread
			    "        t.activate();\n" +
			    "        Thread ot1 = new Thread(runnable);\n" +
			    "        ot1.start();\n" +								// bm() during run(): inherit activation from current thread 
			    "        try {\n" +
			    "            ot1.join();\n" +
			    "        } catch (InterruptedException ie) {\n" +
			    "            ie.printStackTrace();\n" +
			    "        }\n" +
			    "        // <------\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5215eaat3a.java",
			    "\n" +
			    "public class T5215eaat3a {\n" +
			    "    public void bm() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5215eaat3a.java",
			    "\n" +
			    "public team class Team5215eaat3a {\n" +
			    "    public class MyRole playedBy T5215eaat3a {\n" +
			    "        callin void rm() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK|OOK");
    }

    // isActive(ALL_THREADS) returns true if the theam has been activated for all threads
    // 5.2.15-otjld-explicit-activation-for-all-threads-4
    public void test5215_explicitActivationForAllThreads4() {
       
       runConformTest(
            new String[] {
		"T5215eaat4Main.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class T5215eaat4Main {\n" +
			    "    static Team5215eaat4 t;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        t = new Team5215eaat4();\n" +
			    "        t.activate();\n" +
			    "        System.out.print(t.isActive());\n" +
			    "        System.out.print(\"|\");\n" +
			    "        System.out.print(t.isActive(Team.ALL_THREADS));\n" +
			    "        System.out.print(\"|\");\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(t.isActive());\n" +
			    "        System.out.print(\"|\");\n" +
			    "        t.activate(Team.ALL_THREADS);\n" +
			    "        System.out.print(t.isActive(Team.ALL_THREADS));\n" +
			    "        System.out.print(\"|\");\n" +
			    "        // another thread: ----->\n" +
			    "        Runnable otherThread1 = new Runnable() {\n" +
			    "            public void run() {\n" +
			    "                t.deactivate(); // deactivation for this thread only!\n" +
			    "            }\n" +
			    "        };\n" +
			    "        Thread ot1 = new Thread(otherThread1);\n" +
			    "        ot1.start();\n" +
			    "        try {\n" +
			    "            ot1.join();\n" +
			    "        } catch (InterruptedException ie) {\n" +
			    "            ie.printStackTrace();\n" +
			    "        }\n" +
			    "        // <------\n" +
			    "        System.out.print(t.isActive(Team.ALL_THREADS));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5215eaat4.java",
			    "\n" +
			    "public team class Team5215eaat4 {\n" +
			    "    public class MyRole {}\n" +
			    "}\n" +
			    "    \n"
            },
            "true|false|false|true|true");
    }

    // a team registers twice at a super class which is bound by another team - seen in OTPong
    // 5.2.16-otjld-activation-bug-1
    public void test5216_activationBug1() {
       
       runConformTest(
            new String[] {
		"Team5216_2.java",
			    "\n" +
			    "public team class Team5216_2 {\n" +
			    "    protected class R2 playedBy T5216_2 {\n" +
			    "        void test() { \n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        test <- after test;\n" +
			    "    }\n" +
			    "    protected class R3 playedBy T5216_3 {\n" +
			    "    }\n" +
			    "    Team5216_1 t = null;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team5216_2 t = new Team5216_2();\n" +
			    "        t.activate();\n" +
			    "        T5216_2 b = new T5216_2();\n" +
			    "        b.test();\n" +
			    "        t.deactivate();\n" +
			    "        b.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5216_1.java",
			    "\n" +
			    "public class T5216_1 {\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5216_2.java",
			    "\n" +
			    "public class T5216_2 extends T5216_1 {\n" +
			    "}\n" +
			    "    \n",
		"T5216_3.java",
			    "\n" +
			    "public class T5216_3 extends T5216_1 {\n" +
			    "}\n" +
			    "    \n",
		"Team5216_1.java",
			    "\n" +
			    "public team class Team5216_1 {\n" +
			    "    protected class R1 playedBy T5216_1 {\n" +
			    "        void test() { \n" +
			    "            System.out.print(\"!!\");\n" +
			    "        }\n" +
			    "        test <- after test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OKO");
    }

    // internally duplicate attempt to deactivate a team caused an AIOOBE "0" - reported by MWSE students
    // 5.2.16-otjld-activation-bug-2
    public void test5216_activationBug2() {
       
       runConformTest(
            new String[] {
		"Team5216ab2_2.java",
			    "\n" +
			    "public team class Team5216ab2_2 {\n" +
			    "    protected class R2 playedBy T5216ab2_1 {\n" +
			    "        void sayR2() {\n" +
			    "            System.out.print(\"R2.\");\n" +
			    "        }\n" +
			    "        sayR2 <- after test;\n" +
			    "    }\n" +
			    "    protected class R3 playedBy T5216ab2_2 {\n" +
			    "        void sayR3() {\n" +
			    "            System.out.print(\"R3.\");\n" +
			    "        }\n" +
			    "        sayR3 <- after test;\n" +
			    "    }\n" +
			    "    precedence R2, R3;\n" +
			    "    \n" +
			    "    public static void main(String[] args) {\n" +
			    "        T5216ab2_0 b1= new T5216ab2_1();\n" +
			    "        T5216ab2_0 b2= new T5216ab2_2();\n" +
			    "        Team5216ab2_1 t1= new Team5216ab2_1();\n" +
			    "        Team5216ab2_2 t2= new Team5216ab2_2();\n" +
			    "        b1.test();\n" +
			    "        b2.test();\n" +
			    "        t1.activate();\n" +
			    "        t2.activate();\n" +
			    "        b1.test();\n" +
			    "        b2.test();\n" +
			    "        t2.deactivate();\n" +
			    "        b1.test();\n" +
			    "        b2.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5216ab2_0.java",
			    "\n" +
			    "public class T5216ab2_0 {\n" +
			    "    void test() {\n" +
			    "       System.out.print(\"B0.\"); \n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5216ab2_1.java",
			    "\n" +
			    "public class T5216ab2_1 extends T5216ab2_0 {\n" +
			    "    void test() {\n" +
			    "       System.out.print(\"B1.\"); \n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5216ab2_2.java",
			    "\n" +
			    "public class T5216ab2_2 extends T5216ab2_0 {\n" +
			    "    void test() {\n" +
			    "       System.out.print(\"B2.\"); \n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team5216ab2_1.java",
			    "\n" +
			    "public team class Team5216ab2_1 {\n" +
			    "    protected class R1 playedBy T5216ab2_0 {\n" +
			    "        void sayR1() {\n" +
			    "            System.out.print(\"R1.\");\n" +
			    "        }\n" +
			    "        sayR1 <- after test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "B1.B2.B1.R1.R2.B2.R1.R3.B1.R1.B2.R1.");
    }

    // witness for AIOOBE
    // 5.2.17-otjld-concurrent-activation-1
    public void _test5217_concurrentActivation1() {
       
       runConformTest(
            new String[] {
		"Team5217ca1.java",
			    "\n" +
			    "import java.util.Random;\n" +
			    "public team class Team5217ca1 {\n" +
			    "    R r;\n" +
			    "    protected class R playedBy T5217ca1 {\n" +
			    "        int rcount;\n" +
			    "        callin void up() {\n" +
			    "            rcount++;\n" +
			    "            base.up();\n" +
			    "        }\n" +
			    "        up <- replace test;\n" +
			    "    }\n" +
			    "    Team5217ca1() { this.activate(ALL_THREADS); }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team5217ca1[] teams= new Team5217ca1[100];\n" +
			    "        for(int i=0;i<100;i++)\n" +
			    "            teams[i]= new Team5217ca1();\n" +
			    "        Thread switcher= new Thread() {\n" +
			    "            public void run() {\n" +
			    "                Random r= new Random(5217);\n" +
			    "                while (true) {\n" +
			    "                    int i= r.nextInt(100);\n" +
			    "                    if (teams[i].isActive())\n" +
			    "                        teams[i].deactivate(ALL_THREADS);\n" +
			    "                    else\n" +
			    "                        teams[i].activate(ALL_THREADS);\n" +
			    "                }\n" +
			    "            }\n" +
			    "        };\n" +
			    "        T5217ca1 b= new T5217ca1();\n" +
			    "        switcher.start();\n" +
			    "        for (int i=0; i<50000; i++)\n" +
			    "            try {\n" +
			    "                b.test();\n" +
			    "            } catch (ArrayIndexOutOfBoundsException aioobe) {\n" +
			    "                System.out.print(\"NOK\");\n" +
			    "                System.exit(0);\n" +
			    "            } catch (Throwable t) {\n" +
			    "                t.printStackTrace();\n" +
			    "            }\n" +
			    "        System.out.print(\"OK\"); // no exception.\n" +
			    "        System.exit(0);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5217ca1.java",
			    "\n" +
			    "public class T5217ca1 {\n" +
			    "    public int count;\n" +
			    "    void test() {\n" +
			    "        this.count++;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // witness for deadlock
    // 5.2.17-otjld-concurrent-activation-2
    public void test5217_concurrentActivation2() {
       
       runConformTest(
            new String[] {
		"Team5217ca2.java",
			    "\n" +
			    "public team class Team5217ca2 {\n" +
			    "    R r;\n" +
			    "    static boolean worked=false;\n" +
			    "    static int i;\n" +
			    "    protected class R playedBy T5217ca2 {\n" +
			    "        int rcount;\n" +
			    "        callin void up() {\n" +
			    "            rcount++;\n" +
			    "            base.up();\n" +
			    "        }\n" +
			    "        up <- replace test;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        // some teams so initial wrapper needs some time:\n" +
			    "        final Team5217ca2[] teams= new Team5217ca2[100];\n" +
			    "        for(int j=0;j<100;j++)\n" +
			    "            teams[j]= new Team5217ca2();\n" +
			    "        Thread switcher= new Thread() {\n" +
			    "            public void run() {\n" +
			    "                T5217ca2 b= new T5217ca2();\n" +
			    "                while (true) {\n" +
			    "                    within (teams[50]) {\n" +
			    "                        worked= true; // mark that we are still alive\n" +
			    "                    } // the end of this block calls _OT$restoreActivationState()\n" +
			    "                }\n" +
			    "            }\n" +
			    "        };\n" +
			    "        Thread guardian= new Thread() {\n" +
			    "            public void run() {\n" +
			    "                while (true) {\n" +
			    "                    worked= false;\n" +
			    "                    try {\n" +
			    "                        sleep(1000);\n" +
			    "                    } catch(InterruptedException ie) {\n" +
			    "                        System.out.print(\"INTERRUPTED at \"+i);\n" +
			    "                        System.exit(0);\n" +
			    "                    }\n" +
			    "                    if (!worked) {\n" +
			    "                        System.out.print(\"DEAD at \"+i);\n" +
			    "                        System.exit(0);\n" +
			    "                    }\n" +
			    "                }\n" +
			    "            }\n" +
			    "        };\n" +
			    "        T5217ca2 b= new T5217ca2();\n" +
			    "        switcher.start();\n" +
			    "        guardian.start();\n" +
			    "        for (i=0; i<500; i++)\n" +
			    "            b.test();\n" +
			    "        System.out.print(\"OK\"); // no deadlock\n" +
			    "        System.exit(0);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T5217ca2.java",
			    "\n" +
			    "public class T5217ca2 {\n" +
			    "    int m=0;\n" +
			    "    void test() {\n" +
			    "        m++;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a per-thread team inherits this activation to a child thread, see Trac #228
    // 5.2.18-otjld-inheritable-team-activation-1
    public void test5218_inheritableTeamActivation1() {
       
       runConformTest(
            new String[] {
		"Team5218ita1.java",
			    "\n" +
			    "public team class Team5218ita1 {\n" +
			    "    protected class R playedBy T5218ita1 {\n" +
			    "		void k () { System.out.print(\"K\"); }\n" +
			    "		k <- after o;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) throws InterruptedException {\n" +
			    "		org.objectteams.Team t = new Team5218ita1();\n" +
			    "		t.setInheritableActivation(true);\n" +
			    "		t.activate();\n" +
			    "		Thread th = new Thread(new Runnable() { \n" +
			    "	    	public void run() {\n" +
			    "				new T5218ita1().o();\n" +
			    "	    	}\n" +
			    "		});\n" +
			    "		th.start();\n" +
			    "       th.join();\n" +
			    "    }\n" +
			    "}\n" +
			    "  \n",
		"T5218ita1.java",
			    "\n" +
			    "public class T5218ita1 {\n" +
			    "    void o() {\n" +
			    "		System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a per-thread team does not inherit this activation to a child thread, see Trac #228
    // 5.2.18-otjld-inheritable-team-activation-2
    public void test5218_inheritableTeamActivation2() {
       
       runConformTest(
            new String[] {
		"Team5218ita2.java",
			    "\n" +
			    "public team class Team5218ita2 {\n" +
			    "    protected class R playedBy T5218ita2 {\n" +
			    "	void k () { System.out.print(\"K\"); }\n" +
			    "	k <- after o;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) throws InterruptedException {\n" +
			    "		org.objectteams.Team t = new Team5218ita2();\n" +
			    "		t.activate();\n" +
			    "		Thread th = new Thread(new Runnable() { \n" +
			    "	    	public void run() {\n" +
			    "				new T5218ita2().o();\n" +
			    "	    	}\n" +
			    "		});\n" +
			    "		th.start();\n" +
			    "   	th.join();\n" +
			    "    }\n" +
			    "}\n" +
			    "  \n",
		"T5218ita2.java",
			    "\n" +
			    "public class T5218ita2 {\n" +
			    "    void o() {\n" +
			    "	System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "O");
    }
}
