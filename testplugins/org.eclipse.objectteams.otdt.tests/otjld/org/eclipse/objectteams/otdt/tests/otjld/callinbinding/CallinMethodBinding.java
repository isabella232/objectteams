/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2013 IT Service Omikron GmbH and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Thomas Dudziak - Initial API and implementation
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otjld.callinbinding;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

@SuppressWarnings("unchecked")
public class CallinMethodBinding extends AbstractOTJLDTest {
	
	public CallinMethodBinding(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test4143_callinToTeamMethod" };
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return CallinMethodBinding.class;
	}

    // a role method is callin-bound as 'before' to a method in the direct base class
    // 4.1.1-otjld-before-callin-binding-1
    public void test411_beforeCallinBinding1() {
       
       runConformTest(
            new String[] {
		"T411bcb1Main.java",
			    "\n" +
			    "public class T411bcb1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team411bcb1 t = new Team411bcb1();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "\n" +
			    "        T411bcb1_2 o = new T411bcb1_2();\n" +
			    "\n" +
			    "        System.out.print(o.getValue(2));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T411bcb1_1.java",
			    "\n" +
			    "public class T411bcb1_1 {\n" +
			    "    private static int value = 0;\n" +
			    "    public static void setValue(int arg) {\n" +
			    "        value = 1;\n" +
			    "    }\n" +
			    "    public static int getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T411bcb1_2.java",
			    "\n" +
			    "public class T411bcb1_2 {\n" +
			    "    public int getValue(int arg) {\n" +
			    "        return T411bcb1_1.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team411bcb1.java",
			    "\n" +
			    "public team class Team411bcb1 {\n" +
			    "    public class Role411bcb1 extends T411bcb1_1 playedBy T411bcb1_2 {\n" +
			    "        setValue <- before getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1");
    }

    // a role method is callin-bound as 'after' to a method in the base class of the implicit superrole
    // 4.1.1-otjld-before-callin-binding-2
    public void test411_beforeCallinBinding2() {
       
       runConformTest(
            new String[] {
		"T411bcb2Main.java",
			    "\n" +
			    "public class T411bcb2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team411bcb2_2 t = new Team411bcb2_2();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "\n" +
			    "        T411bcb2_2 o = new T411bcb2_2();\n" +
			    "\n" +
			    "        System.out.print(o.getValue(\"OK\")+\"|\"+t.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T411bcb2_1.java",
			    "\n" +
			    "public class T411bcb2_1 {\n" +
			    "    String getValue(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T411bcb2_2.java",
			    "\n" +
			    "public class T411bcb2_2 extends T411bcb2_1 {}\n" +
			    "    \n",
		"Team411bcb2_1.java",
			    "\n" +
			    "public team class Team411bcb2_1 {\n" +
			    "    private String value = \"NOTOK\";\n" +
			    "\n" +
			    "    public class Role411bcb2 playedBy T411bcb2_2 {\n" +
			    "        protected void setValueInternal(String arg) {\n" +
			    "            Team411bcb2_1.this.value = arg;\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team411bcb2_2.java",
			    "\n" +
			    "public team class Team411bcb2_2 extends Team411bcb2_1 {\n" +
			    "    public class Role411bcb2 {\n" +
			    "        setValueInternal <- after getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK|OK");
    }

    // a role method is callin-bound as 'replace' to a method in the base class of the explicit superrole
    // 4.1.1-otjld-before-callin-binding-3
    public void test411_beforeCallinBinding3() {
       
       runConformTest(
            new String[] {
		"T411bcb3Main.java",
			    "\n" +
			    "public class T411bcb3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team411bcb3 t = new Team411bcb3();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "\n" +
			    "        T411bcb3 o = new T411bcb3();\n" +
			    "\n" +
			    "        System.out.print(o.getValue(\"NOTOK\")+\"|\"+t.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T411bcb3.java",
			    "\n" +
			    "public class T411bcb3 {\n" +
			    "    public String getValue(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team411bcb3.java",
			    "\n" +
			    "public team class Team411bcb3 {\n" +
			    "    private String value = \"NOTOK\";\n" +
			    "\n" +
			    "    public class Role411bcb3_1 playedBy T411bcb3 {\n" +
			    "        callin String test() {\n" +
			    "            base.test();\n" +
			    "            Team411bcb3.this.value = \"OK\";\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role411bcb3_2 extends Role411bcb3_1 {\n" +
			    "        String test() <- replace String getValue(String arg);\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK|OK");
    }

    // a replace binding mentions a buggy callin method
    // 4.1.1-otjld-before-callin-binding-3f
    public void test411_beforeCallinBinding3f() {
        
        runNegativeTest(
            new String[] {
		"p1/T411bcb3f.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T411bcb3f {\n" +
			    "    public void foo() {\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team411bcb3f_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team411bcb3f_1 {\n" +
			    "    protected R r;\n" +
			    "    protected class R playedBy T411bcb3f {\n" +
			    "        callin void broken () {\n" +
			    "            base.broken();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/Team411bcb3f_2.java",
			    "\n" +
			    "package p2;\n" +
			    "import p1.Team411bcb3f_1;\n" +
			    "public team class Team411bcb3f_2 extends Team411bcb3f_1 {\n" +
			    "    @Override\n" +
			    "    public class R  {\n" +
			    "        public R() { base(); }\n" +
			    "        public void blub() { }\n" +
			    "        @Override\n" +
			    "        callin void broken () {\n" +
			    "            Team411bcb3f_1.this.r= this;\n" +
			    "            base.broken();\n" +
			    "            Team411bcb3f_1.this.r= null;\n" +
			    "        }\n" +
			    "        broken <- replace foo;\n" +
			    "    }\n" +
			    "}\n",
            },
            "----------\n" + 
    		"1. ERROR in p2\\Team411bcb3f_2.java (at line 11)\n" + 
    		"	Team411bcb3f_1.this.r= this;\n" + 
    		"	^^^^^^^^^^^^^^^^^^^\n" + 
    		"No enclosing instance of the type Team411bcb3f_1 is accessible in scope\n" + 
    		"----------\n" + 
    		"2. ERROR in p2\\Team411bcb3f_2.java (at line 11)\n" + 
    		"	Team411bcb3f_1.this.r= this;\n" + 
    		"	                    ^\n" + 
    		"The field Team411bcb3f_1.r is not visible\n" + 
    		"----------\n" + 
    		"3. ERROR in p2\\Team411bcb3f_2.java (at line 13)\n" + 
    		"	Team411bcb3f_1.this.r= null;\n" + 
    		"	^^^^^^^^^^^^^^^^^^^\n" + 
    		"No enclosing instance of the type Team411bcb3f_1 is accessible in scope\n" + 
    		"----------\n" + 
    		"4. ERROR in p2\\Team411bcb3f_2.java (at line 13)\n" + 
    		"	Team411bcb3f_1.this.r= null;\n" + 
    		"	                    ^\n" + 
    		"The field Team411bcb3f_1.r is not visible\n" + 
    		"----------\n",
    		null/*classLibraries*/,
    		true/*shouldFlushOutputDirectory*/,
    		null/*customOptions*/,
    		true/*generateOutput*/,
    		false/*showCategory*/,
    		false);
        runConformTest(
            new String[] {
		"T411bcb3f_2.java",
	    		"import p2.Team411bcb3f_2;\n" +
	    		"public class T411bcb3f_2 {\n" +
	    		"    final Team411bcb3f_2 t= new Team411bcb3f_2();\n" +
	    		"    R<@t> r= t.new R();\n" +
	    		"    void bar() {\n" +
	    		"        r.blub();\n" +
	    		"    }\n" +
	    		"}\n" +
	    		"    \n"
            },
            null/*expectedOutput*/,
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/);
    }

    // a role method is callin-bound as 'replace' to a method in the base class of a remote superrole
    // 4.1.1-otjld-before-callin-binding-4
    public void test411_beforeCallinBinding4() {
       
       runConformTest(
            new String[] {
		"T411bcb4Main.java",
			    "\n" +
			    "public class T411bcb4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team411bcb4_2 t = new Team411bcb4_2();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "\n" +
			    "        T411bcb4_2 o = new T411bcb4_2();\n" +
			    "\n" +
			    "        System.out.print(o.getValue(\"4\", \"5\")+\"|\"+t.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T411bcb4_1.java",
			    "\n" +
			    "public class T411bcb4_1 {\n" +
			    "    protected String getValue(String arg1, String arg2) {\n" +
			    "        return \"1\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T411bcb4_2.java",
			    "\n" +
			    "public class T411bcb4_2 extends T411bcb4_1 {}\n" +
			    "    \n",
		"Team411bcb4_1.java",
			    "\n" +
			    "public team class Team411bcb4_1 {\n" +
			    "    private String value = \"2\";\n" +
			    "\n" +
			    "    public class Role411bcb4_1 playedBy T411bcb4_1 {\n" +
			    "        callin String test(String arg) {\n" +
			    "            Team411bcb4_1.this.setValue(\"3\");\n" +
			    "            return base.test(arg);\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    protected void setValue(String value) {\n" +
			    "        this.value = value;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team411bcb4_2.java",
			    "\n" +
			    "public team class Team411bcb4_2 extends Team411bcb4_1 {\n" +
			    "\n" +
			    "    public class Role411bcb4_2 extends Role411bcb4_1 playedBy T411bcb4_2 {\n" +
			    "        String test(String arg) <- replace String getValue(String arg1, String arg2) with {\n" +
			    "            arg <- arg2,\n" +
			    "            result -> result\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1|3");
    }

    // a role method is callin-bound as 'replace' to a method in the base class of a remote superrole
    // 4.1.1-otjld-before-callin-binding-5
    public void test411_beforeCallinBinding5() {
       
       runConformTest(
            new String[] {
		"T411bcb5Main.java",
			    "\n" +
			    "public class T411bcb5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team411bcb5_2 t = new Team411bcb5_2();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "\n" +
			    "        T411bcb5_2 o = new T411bcb5_2();\n" +
			    "\n" +
			    "        System.out.print(o.getValue(\"1\", \"2\")+\"|\"+t.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T411bcb5_1.java",
			    "\n" +
			    "public class T411bcb5_1 {\n" +
			    "    protected String getValue(String arg1, String arg2) {\n" +
			    "        return arg1+arg2;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T411bcb5_2.java",
			    "\n" +
			    "public class T411bcb5_2 extends T411bcb5_1 {}\n" +
			    "    \n",
		"Team411bcb5_1.java",
			    "\n" +
			    "public team class Team411bcb5_1 {\n" +
			    "    private String value = \"NOTOK\";\n" +
			    "\n" +
			    "    public class Role411bcb5_1 playedBy T411bcb5_1 {\n" +
			    "        callin String test(String arg) {\n" +
			    "            Team411bcb5_1.this.setValue(arg);\n" +
			    "            return base.test(\"3\")+\"4\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    protected void setValue(String value) {\n" +
			    "        this.value = value;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team411bcb5_2.java",
			    "\n" +
			    "public team class Team411bcb5_2 extends Team411bcb5_1 {\n" +
			    "\n" +
			    "    public class Role411bcb5_2 extends Role411bcb5_1 playedBy T411bcb5_2 {\n" +
			    "        String test(String arg) <- replace String getValue(String arg1, String arg2) with {\n" +
			    "            arg <- arg2,\n" +
			    "            result -> result\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "134|2");
    }

    // non simple expression in replace param mapping
    // 4.1.1-otjld-before-callin-binding-6
    public void test411_beforeCallinBinding6() {
        runNegativeTest(
            new String[] {
		"T411bcb6_1.java",
			    "\n" +
			    "public class T411bcb6_1 {\n" +
			    "    protected String getValue(String arg1, String arg2) {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T411bcb6_2.java",
			    "\n" +
			    "public class T411bcb6_2 extends T411bcb6_1 {}\n" +
			    "    \n",
		"Team411bcb6_1.java",
			    "\n" +
			    "public team class Team411bcb6_1 {\n" +
			    "    private String value = \"NOTOK\";\n" +
			    "\n" +
			    "    public class Role411bcb6_1 playedBy T411bcb6_1 {\n" +
			    "        callin String test(String arg) {\n" +
			    "            Team411bcb6_1.this.setValue(\"OK\");\n" +
			    "            return base.test(arg);\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    protected void setValue(String value) {\n" +
			    "        this.value = value;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team411bcb6_2.java",
			    "\n" +
			    "public team class Team411bcb6_2 extends Team411bcb6_1 {\n" +
			    "\n" +
			    "    public class Role411bcb6_2 extends Role411bcb6_1 playedBy T411bcb6_2 {\n" +
			    "        String test(String arg) <- replace String getValue(String arg1, String arg2) with {\n" +
			    "            arg <- arg2+\"X\",\n" +
			    "            result -> result\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a callin binding has a signature without parameter names
    // 4.1.2-otjld-callin-binding-without-parameter-names-1
    public void test412_callinBindingWithoutParameterNames1() {
        runNegativeTest(
            new String[] {
		"T412cbwpn1.java",
			    "\n" +
			    "public class T412cbwpn1 {\n" +
			    "    public String getValue(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team412cbwpn1.java",
			    "\n" +
			    "public team class Team412cbwpn1 {\n" +
			    "\n" +
			    "    public class Role412cbwpn1 playedBy T412cbwpn1 {\n" +
			    "        callin String getValue(String arg) {\n" +
			    "            return base.getValue(arg);\n" +
			    "        }\n" +
			    "        String getValue(String) <- replace String getValue(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a callin binding has a signature without parameter names
    // 4.1.2-otjld-callin-binding-without-parameter-names-2
    public void test412_callinBindingWithoutParameterNames2() {
        runNegativeTest(
            new String[] {
		"T412cbwpn2.java",
			    "\n" +
			    "public class T412cbwpn2 {\n" +
			    "    public String getValue(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team412cbwpn2.java",
			    "\n" +
			    "public team class Team412cbwpn2 {\n" +
			    "\n" +
			    "    public class Role412cbwpn2 playedBy T412cbwpn2 {\n" +
			    "        public String getValue(String arg) {\n" +
			    "            return arg;\n" +
			    "        }\n" +
			    "        String getValue(String arg) <- after String getValue(String);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role method is callin-bound as 'replace' and overwrites a replace callin-binding in the superrole, named callin
    // 4.1.3-otjld-overwriting-inherited-callin-binding-1
    public void test413_overwritingInheritedCallinBinding1() {
       
       runConformTest(
            new String[] {
		"T413oicb1Main.java",
			    "\n" +
			    "public class T413oicb1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team413oicb1_2 t = new Team413oicb1_2();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            T413oicb1 o = new T413oicb1();\n" +
			    "\n" +
			    "            System.out.print(o.getValue());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb1.java",
			    "\n" +
			    "public class T413oicb1 {\n" +
			    "    public String getValue() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team413oicb1_1.java",
			    "\n" +
			    "public team class Team413oicb1_1 {\n" +
			    "    public class Role413oicb1 playedBy T413oicb1 {\n" +
			    "        callin String test() {\n" +
			    "            base.test();\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        testGetValue:\n" +
			    "        test <- replace getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team413oicb1_2.java",
			    "\n" +
			    "public team class Team413oicb1_2 extends Team413oicb1_1 {\n" +
			    "    public class Role413oicb1 {\n" +
			    "        callin String test2() {\n" +
			    "            base.test2();\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        testGetValue:\n" +
			    "        String test2() <- replace String getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role method is callin-bound as 'replace' and adds to a replace callin-binding in the superrole, precedence used
    // 4.1.3-otjld-overwriting-inherited-callin-binding-1a
    public void test413_overwritingInheritedCallinBinding1a() {
       
       runConformTest(
            new String[] {
		"T413oicb1aMain.java",
			    "\n" +
			    "public class T413oicb1aMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team413oicb1a_2 t = new Team413oicb1a_2();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            T413oicb1a o = new T413oicb1a();\n" +
			    "\n" +
			    "            System.out.print(o.getValue());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb1a.java",
			    "\n" +
			    "public class T413oicb1a {\n" +
			    "    public String getValue() {\n" +
			    "        return \"O\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team413oicb1a_1.java",
			    "\n" +
			    "public team class Team413oicb1a_1 {\n" +
			    "    public class Role413oicb1a playedBy T413oicb1a {\n" +
			    "        callin String test() {\n" +
			    "            return base.test()+\"!\";\n" +
			    "        }\n" +
			    "        testGetValue:\n" +
			    "        test <- replace getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team413oicb1a_2.java",
			    "\n" +
			    "public team class Team413oicb1a_2 extends Team413oicb1a_1 {\n" +
			    "    public class Role413oicb1a {\n" +
			    "        callin String test2() {\n" +
			    "            return base.test2()+\"K\";\n" +
			    "        }\n" +
			    "        test2GetValue:\n" +
			    "        String test2() <- replace String getValue();\n" +
			    "    }\n" +
			    "    precedence Role413oicb1a.testGetValue, Role413oicb1a.test2GetValue;\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // a role method is callin-bound as 'replace' and overwrites a replace callin-binding in the superrole, callins have no name
    // 4.1.3-otjld-overwriting-inherited-callin-binding-1f
    public void test413_overwritingInheritedCallinBinding1f() {
        runNegativeTestMatching(
            new String[] {
		"Team413oicb1f_2.java",
			    "\n" +
			    "public team class Team413oicb1f_2 extends Team413oicb1f_1 {\n" +
			    "    public class Role413oicb1f {\n" +
			    "        callin String test2() {\n" +
			    "            base.test2();\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        String test2() <- replace String getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb1f.java",
			    "\n" +
			    "public class T413oicb1f {\n" +
			    "    public String getValue() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team413oicb1f_1.java",
			    "\n" +
			    "public team class Team413oicb1f_1 {\n" +
			    "    public class Role413oicb1f playedBy T413oicb1f {\n" +
			    "        callin String test() {\n" +
			    "            base.test();\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        test <- replace getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OTJLD 4.8");
    }

    // named callin bindings in two _independent_ roles, precedence missing
    // 4.1.3-otjld-overwriting-inherited-callin-binding-1g
    public void test413_overwritingInheritedCallinBinding1g() {
        runNegativeTestMatching(
            new String[] {
		"Team413oicb1g_2.java",
			    "\n" +
			    "public team class Team413oicb1g_2 extends Team413oicb1g_1 {\n" +
			    "    public class Role413oicb1g_2 playedBy T413oicb1g {\n" +
			    "        callin String test2() {\n" +
			    "            return base.test2()+\"K\";\n" +
			    "        }\n" +
			    "        testGetValue:\n" +
			    "        String test2() <- replace String getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb1g.java",
			    "\n" +
			    "public class T413oicb1g {\n" +
			    "    public String getValue() {\n" +
			    "        return \"O\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team413oicb1g_1.java",
			    "\n" +
			    "public team class Team413oicb1g_1 {\n" +
			    "    public class Role413oicb1g_1 playedBy T413oicb1g {\n" +
			    "        callin String test() {\n" +
			    "            return base.test()+\"!\";\n" +
			    "        }\n" +
			    "        testGetValue:\n" +
			    "        test <- replace getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.8");
    }

    // named callin bindings in two _independent_ roles, precedence missing, re-use purely copied binary role
    // 4.1.3-otjld-overwriting-inherited-callin-binding-1h
    public void test413_overwritingInheritedCallinBinding1h() {
        runNegativeTestMatching(
            new String[] {
		"Team413oicb1h_2.java",
			    "\n" +
			    "public team class Team413oicb1h_2 extends Team413oicb1h_1 {\n" +
			    "    public class Role413oicb1h_2 playedBy T413oicb1h {\n" +
			    "        callin String test2() {\n" +
			    "            return base.test2()+\"K\";\n" +
			    "        }\n" +
			    "        testGetValue:\n" +
			    "        String test2() <- replace String getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb1h.java",
			    "\n" +
			    "public class T413oicb1h {\n" +
			    "    public String getValue() {\n" +
			    "        return \"O\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team413oicb1h_1.java",
			    "\n" +
			    "public team class Team413oicb1h_1 {\n" +
			    "    public class Role413oicb1h_1 playedBy T413oicb1h {\n" +
			    "        callin String test() {\n" +
			    "            return base.test()+\"!\";\n" +
			    "        }\n" +
			    "        testGetValue:\n" +
			    "        test <- replace getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.8");
    }

    // a role method is callin-bound as 'replace' and overwrites a name replace callin-binding in the superrole, 
    // NOTE: mapping overriding along 'extends' is not yet defined.
    // Is problematic because both bindings will exist within the same team!
    // 4.1.3-otjld-overwriting-inherited-callin-binding-2
    public void test413_overwritingInheritedCallinBinding2() {
       
       runConformTest(
            new String[] {
		"T413oicb2Main.java",
			    "\n" +
			    "public class T413oicb2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team413oicb2 t = new Team413oicb2();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            T413oicb2_2 o = new T413oicb2_2();\n" +
			    "\n" +
			    "            System.out.print(o.getValue());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb2_1.java",
			    "\n" +
			    "public class T413oicb2_1 {\n" +
			    "    public String getValue() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb2_2.java",
			    "\n" +
			    "public class T413oicb2_2 extends T413oicb2_1 {}\n" +
			    "    \n",
		"Team413oicb2.java",
			    "\n" +
			    "public team class Team413oicb2 {\n" +
			    "    public class Role413oicb2_1 playedBy T413oicb2_1 {\n" +
			    "        callin String test() {\n" +
			    "            base.test();\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        testGetValue:\n" +
			    "        String test() <- replace String getValue();\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role413oicb2_2 extends Role413oicb2_1 playedBy T413oicb2_2 {\n" +
			    "        callin String test() {\n" +
			    "            base.test();\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        testGetValue:\n" +
			    "        test <- replace getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role method is callin-bound as 'replace' and overwrites a replace callin-binding in the superrole
    // 4.1.3-otjld-overwriting-inherited-callin-binding-2f
    public void test413_overwritingInheritedCallinBinding2f() {
        runNegativeTestMatching(
            new String[] {
		"Team413oicb2f.java",
			    "\n" +
			    "public team class Team413oicb2f {\n" +
			    "    public class Role413oicb2f_1 playedBy T413oicb2f_1 {\n" +
			    "        callin String test() {\n" +
			    "            base.test();\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        String test() <- replace String getValue();\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role413oicb2f_2 extends Role413oicb2f_1 playedBy T413oicb2f_2 {\n" +
			    "        callin String test() {\n" +
			    "            base.test();\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        test <- replace getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb2f_1.java",
			    "\n" +
			    "public class T413oicb2f_1 {\n" +
			    "    public String getValue() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb2f_2.java",
			    "\n" +
			    "public class T413oicb2f_2 extends T413oicb2f_1 {}\n" +
			    "    \n"
            },
            "4.8");
    }

    // a role method is callin-bound as 'replace' and overwrites a replace callin-binding in the superrole - smart lifting selects the appropriate binding - independent role methods
    // 4.1.3-otjld-overwriting-inherited-callin-binding-2g
    public void test413_overwritingInheritedCallinBinding2g() {
        runNegativeTestMatching(
            new String[] {
		"Team413oicb2g.java",
			    "\n" +
			    "public team class Team413oicb2g {\n" +
			    "    public class Role413oicb2g_1 playedBy T413oicb2g_1 {\n" +
			    "        callin String test1() {\n" +
			    "            base.test1();\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        String test1() <- replace String getValue();\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role413oicb2g_2 extends Role413oicb2g_1 playedBy T413oicb2g_2 {\n" +
			    "        callin String test2() {\n" +
			    "            base.test2();\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        test2 <- replace getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb2g_1.java",
			    "\n" +
			    "public class T413oicb2g_1 {\n" +
			    "    public String getValue() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb2g_2.java",
			    "\n" +
			    "public class T413oicb2g_2 extends T413oicb2g_1 {}\n" +
			    "    \n"
            },
            "4.8");
    }

    // a role method is callin-bound as 'replace' and overwrites a before callin-binding in the remote superrole
    // 4.1.3-otjld-overwriting-inherited-callin-binding-3
    public void test413_overwritingInheritedCallinBinding3() {
       
       runConformTest(
            new String[] {
		"T413oicb3Main.java",
			    "\n" +
			    "public class T413oicb3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team413oicb3_2 t = new Team413oicb3_2();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            T413oicb3 o = new T413oicb3();\n" +
			    "\n" +
			    "            System.out.print(o.getValue());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T413oicb3.java",
			    "\n" +
			    "public class T413oicb3 {\n" +
			    "    public static String value = null;\n" +
			    "    public String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team413oicb3_1.java",
			    "\n" +
			    "public team class Team413oicb3_1 {\n" +
			    "    public class Role413oicb3_1 playedBy T413oicb3 {\n" +
			    "        public void test() {\n" +
			    "            T413oicb3.value = \"NOTOK\";\n" +
			    "        }\n" +
			    "        test <- before getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team413oicb3_2.java",
			    "\n" +
			    "public team class Team413oicb3_2 extends Team413oicb3_1 {\n" +
			    "    public class Role413oicb3_2 extends Role413oicb3_1 {\n" +
			    "        callin String getValue() {\n" +
			    "            // we get a warning here but we do not want to call the base method\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        getValue <- replace getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
          //FIXME: expect this warning
    }

    // an unbound role class has a callin binding
    // 4.1.4-otjld-callin-binding-in-unbound-role-1
    public void test414_callinBindingInUnboundRole1() {
        runNegativeTest(
            new String[] {
		"T414cbiur1.java",
			    "\n" +
			    "public class T414cbiur1 {\n" +
			    "    public String getValue(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team414cbiur1.java",
			    "\n" +
			    "public team class Team414cbiur1 {\n" +
			    "\n" +
			    "    public class Role414cbiur1 extends T414cbiur1 {\n" +
			    "        public String getValue(String arg) {\n" +
			    "            return arg;\n" +
			    "        }\n" +
			    "        getValue <- after getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // an unbound role class has a callin binding
    // 4.1.4-otjld-callin-binding-in-unbound-role-2
    public void test414_callinBindingInUnboundRole2() {
        runNegativeTest(
            new String[] {
		"T414cbiur2.java",
			    "\n" +
			    "public class T414cbiur2 {\n" +
			    "    public String getValue(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team414cbiur2.java",
			    "\n" +
			    "public team class Team414cbiur2 {\n" +
			    "\n" +
			    "    public class Role414cbiur2_1 {\n" +
			    "        callin String getValue(String arg) {\n" +
			    "            return base.getValue(arg);\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role414cbiur2_2 extends Role414cbiur2_1 {\n" +
			    "        getValue <- replace getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds to a non-existing base method
    // 4.1.5-otjld-nonexisting-base-method-1
    public void test415_nonexistingBaseMethod1() {
        runNegativeTest(
            new String[] {
		"T415nbm1.java",
			    "\n" +
			    "public class T415nbm1 {}\n" +
			    "    \n",
		"Team415nbm1.java",
			    "\n" +
			    "public team class Team415nbm1 {\n" +
			    "\n" +
			    "    public class Role415nbm1 playedBy T415nbm1 {\n" +
			    "        public String getValue(String arg) {\n" +
			    "            return arg;\n" +
			    "        }\n" +
			    "        String getValue(String arg) <- after String getValue(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds to a non-existing base method
    // 4.1.5-otjld-nonexisting-base-method-2
    public void test415_nonexistingBaseMethod2() {
        runNegativeTest(
            new String[] {
		"T415nbm2.java",
			    "\n" +
			    "public class T415nbm2 {\n" +
			    "    public String getValue(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm2.java",
			    "\n" +
			    "public team class Team415nbm2 {\n" +
			    "\n" +
			    "    public class Role415nbm2 playedBy T415nbm2 {\n" +
			    "        public String getValue(String arg) {\n" +
			    "            return arg;\n" +
			    "        }\n" +
			    "        getValue <- after getValue1;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds to a not-accessible base method
    // 4.1.5-otjld-nonexisting-base-method-3a
    public void test415_nonexistingBaseMethod3a() {
       
       runConformTest(
            new String[] {
		"Team415nbm3a.java",
			    "\n" +
			    "public team class Team415nbm3a {\n" +
			    "\n" +
			    "    public class Role415nbm3a playedBy T415nbm3a {\n" +
			    "        public void test() {\n" +
			    "	    System.out.print(\"O\");\n" +
			    "	}\n" +
			    "        void test() <- before void test(String arg);\n" +
			    "    }\n" +
			    "    Team415nbm3a () {\n" +
			    "    	activate();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "    	new Team415nbm3a();\n" +
			    "	(new T415nbm3a()).run();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T415nbm3a.java",
			    "\n" +
			    "public class T415nbm3a {\n" +
			    "    private void test(String arg) {\n" +
			    "    	System.out.print(arg);\n" +
			    "    }\n" +
			    "    public void run() {\n" +
			    "    	test(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class callin-binds to a not-accessible base method - with base call
    // 4.1.5-otjld-nonexisting-base-method-3b
    public void test415_nonexistingBaseMethod3b() {
       runConformTest(
            new String[] {
		"T415nbm3bMain.java",
			    "\n" +
			    "public class T415nbm3bMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "    	new Team415nbm3b();\n" +
			    "	(new T415nbm3b()).run();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T415nbm3b.java",
			    "\n" +
			    "public class T415nbm3b {\n" +
			    "    private void test(String arg) {\n" +
			    "    	System.out.print(arg);\n" +
			    "    }\n" +
			    "    public void run() {\n" +
			    "    	test(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm3b.java",
			    "\n" +
			    "public team class Team415nbm3b {\n" +
			    "\n" +
			    "    public class Role415nbm3b playedBy T415nbm3b {\n" +
			    "        callin void test() {\n" +
			    "	    System.out.print(\"O\");\n" +
			    "	    base.test();\n" +
			    "	}\n" +
			    "        void test() <- replace void test(String arg);\n" +
			    "    }\n" +
			    "    Team415nbm3b () {\n" +
			    "    	activate();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class callin-binds to a not-accessible base method - with base call - double compile
    // 4.1.5-otjld-nonexisting-base-method-3f
    public void test415_nonexistingBaseMethod3f() {
       
       runConformTest(
            new String[] {
		"T415nbm3fMain.java",
			    "\n" +
			    "public class T415nbm3fMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team415nbm3f();\n" +
			    "    (new T415nbm3f()).run();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T415nbm3f.java",
			    "\n" +
			    "public class T415nbm3f {\n" +
			    "    private void test(String arg) {\n" +
			    "        System.out.print(arg);\n" +
			    "    }\n" +
			    "    public void run() {\n" +
			    "        test(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm3f.java",
			    "\n" +
			    "public team class Team415nbm3f {\n" +
			    "\n" +
			    "    public class Role415nbm3f playedBy T415nbm3f {\n" +
			    "        callin void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "        base.test();\n" +
			    "    }\n" +
			    "        void test() <- replace void test(String arg);\n" +
			    "    }\n" +
			    "    Team415nbm3f () {\n" +
			    "        activate();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class CALLOUT-binds to a not-accessible inherited base method - error case
    // 4.1.5-otjld-nonexisting-base-method-3co
    public void test415_nonexistingBaseMethod3co() {
        runNegativeTestMatching(
            new String[] {
		"Team415nbm3co.java",
			    "\n" +
			    "public team class Team415nbm3co {\n" +
			    "\n" +
			    "    public class Role415nbm3co playedBy T415nbm3co_2 {\n" +
			    "        void test(String arg) -> void test(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T415nbm3co_1.java",
			    "\n" +
			    "public class T415nbm3co_1 {\n" +
			    "    private void test(String arg) {\n" +
			    "    	System.out.print(arg);\n" +
			    "    }\n" +
			    "    public void run() {\n" +
			    "    	test(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T415nbm3co_2.java",
			    "\n" +
			    "public class T415nbm3co_2 extends T415nbm3co_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "3.4(d)");
    }

    // a role class callin-binds to a not-accessible inherited base method - error case
    // 4.1.5-otjld-nonexisting-base-method-3h
    public void test415_nonexistingBaseMethod3h() {
        runNegativeTestMatching(
            new String[] {
		"Team415nbm3h.java",
			    "\n" +
			    "public team class Team415nbm3h {\n" +
			    "\n" +
			    "    public class Role415nbm3h playedBy T415nbm3h_2 {\n" +
			    "        public void test() {\n" +
			    "    	    System.out.print(\"O\");\n" +
			    "        }\n" +
			    "        void test() <- before void test(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T415nbm3h_1.java",
			    "\n" +
			    "public class T415nbm3h_1 {\n" +
			    "    private void test(String arg) {\n" +
			    "    	System.out.print(arg);\n" +
			    "    }\n" +
			    "    public void run() {\n" +
			    "    	test(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T415nbm3h_2.java",
			    "\n" +
			    "public class T415nbm3h_2 extends T415nbm3h_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "4.6(a)");
    }

    // a role class callin-binds to a not-accessible inherited base method - corrected
    // 4.1.5-otjld-nonexisting-base-method-3i
    public void test415_nonexistingBaseMethod3i() {
        Map options = getCompilerOptions();
        options.put(CompilerOptions.OPTION_ReportHiddenLiftingProblem, CompilerOptions.WARNING);
       runConformTest(
            new String[] {
		"Team415nbm3i.java",
			    "import org.objectteams.LiftingFailedException;\n" +
			    "public team class Team415nbm3i {\n" +
			    "    @SuppressWarnings(\"abstractrelevantrole\")\n" +
			    "    protected abstract class Role415nbm3i_1 playedBy T415nbm3i_1 {\n" +
			    "        public abstract void test();\n" +
			    "    	 @SuppressWarnings(\"hidden-lifting-problem\")\n" +
			    "        void test() <- before void test(String arg);\n" +
			    "    }\n" +
			    "    public class Role415nbm3i_2 extends Role415nbm3i_1 playedBy T415nbm3i_2 {\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team415nbm3i () {\n" +
			    "    	activate();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "    	new Team415nbm3i();\n" +
			    "	(new T415nbm3i_2()).run();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T415nbm3i_1.java",
			    "\n" +
			    "public abstract class T415nbm3i_1 {\n" +
			    "    private void test(String arg) {\n" +
			    "    	System.out.print(arg);\n" +
			    "    }\n" +
			    "    public void run() {\n" +
			    "    	test(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T415nbm3i_2.java",
			    "\n" +
			    "public class T415nbm3i_2 extends T415nbm3i_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK",
            null/*classLibs*/,
            true/*shouldFlush*/,
            null/*vmArguments*/,
            options,
            null/*requester*/);
    }

    // a role class callin-binds to a not-accessible base method
    // 4.1.5-otjld-nonexisting-base-method-4
    public void test415_nonexistingBaseMethod4() {
       
       runConformTest(
            new String[] {
		"p2/Team415nbm4.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team415nbm4 {\n" +
			    "    @SuppressWarnings(\"bindingconventions\") // re base import vs. fqn\n" +
			    "    public class Role415nbm4 playedBy p1.T415nbm4 {\n" +
			    "        public void check() {\n" +
			    "		System.out.print(\"O\");\n" +
			    "	}\n" +
			    "        check <- before test;\n" +
			    "    }\n" +
			    "    Team415nbm4 () {\n" +
			    "    	activate();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "    	new Team415nbm4();\n" +
			    "	(new p1.T415nbm4()).run();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T415nbm4.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T415nbm4 {\n" +
			    "    void test(String arg) {\n" +
			    "    	System.out.print(arg);\n" +
			    "    }\n" +
			    "    public void run() {\n" +
			    "    	test(\"K\");\n" +
			    "    }	\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class callin-binds to a base method with a signature that has no return type whereas the actual base method has one
    // 4.1.5-otjld-nonexisting-base-method-5
    public void test415_nonexistingBaseMethod5() {
        runNegativeTest(
            new String[] {
		"T415nbm5.java",
			    "\n" +
			    "public class T415nbm5 {\n" +
			    "    public String getValue() {\n" +
			    "        return null;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm5.java",
			    "\n" +
			    "public team class Team415nbm5 {\n" +
			    "\n" +
			    "    public class Role415nbm5 playedBy T415nbm5 {\n" +
			    "        public void test() {}\n" +
			    "        void test() <- before void getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds as 'replace' a role method with no return type whereas the base method has one
    // 4.1.5-otjld-nonexisting-base-method-6
    public void test415_nonexistingBaseMethod6() {
       
       runConformTest(
            new String[] {
		"Team415nbm6.java",
			    "\n" +
			    "public team class Team415nbm6 {\n" +
			    "\n" +
			    "    public class Role415nbm6 playedBy T415nbm6 {\n" +
			    "        callin void test() {\n" +
			    "            setValue(\"O\");\n" +
			    "            base.test();\n" +
			    "        }\n" +
			    "	abstract void setValue(String s);\n" +
			    "        test <- replace test;\n" +
			    "	setValue -> setValue;\n" +
			    "    }\n" +
			    "    public Team415nbm6() {\n" +
			    "        activate();\n" +
			    "    	T415nbm6 o = new T415nbm6();\n" +
			    "	System.out.print(o.test());\n" +
			    "    }\n" +
			    "    public static void main(String [] args) {\n" +
			    "	new Team415nbm6();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T415nbm6.java",
			    "\n" +
			    "public class T415nbm6 {\n" +
			    "    private String value=\"\";\n" +
			    "    public String test() {\n" +
			    "        return value+\"K\";\n" +
			    "    }\n" +
			    "    public String setValue(String s) {\n" +
			    "    	value = s;\n" +
			    "	return value;\n" +
			    "   }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class callin-binds to a base method with a signature that has a return type whereas the actual base method has none
    // 4.1.5-otjld-nonexisting-base-method-7
    public void test415_nonexistingBaseMethod7() {
        runNegativeTest(
            new String[] {
		"T415nbm7.java",
			    "\n" +
			    "public class T415nbm7 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm7.java",
			    "\n" +
			    "public team class Team415nbm7 {\n" +
			    "\n" +
			    "    public class Role415nbm7 playedBy T415nbm7 {\n" +
			    "        protected String test() {\n" +
			    "            return null;\n" +
			    "        }\n" +
			    "        String test() <- after String test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds as 'replace' a role method with a return type whereas the base method has none
    // 4.1.5-otjld-nonexisting-base-method-8
    public void test415_nonexistingBaseMethod8() {
        runNegativeTest(
            new String[] {
		"T415nbm8.java",
			    "\n" +
			    "public class T415nbm8 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm8.java",
			    "\n" +
			    "public team class Team415nbm8 {\n" +
			    "\n" +
			    "    public class Role415nbm8 playedBy T415nbm8 {\n" +
			    "        callin String getValue() {\n" +
			    "            return base.getValue();\n" +
			    "        }\n" +
			    "        getValue <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds as 'replace' to a base method with a signature that is different from the signature of the actual base method
    // 4.1.5-otjld-nonexisting-base-method-9
    public void test415_nonexistingBaseMethod9() {
        runNegativeTest(
            new String[] {
		"T415nbm9.java",
			    "\n" +
			    "public class T415nbm9 {\n" +
			    "    public void test(String arg1, int arg2) {}\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm9.java",
			    "\n" +
			    "public team class Team415nbm9 {\n" +
			    "\n" +
			    "    public class Role415nbm9 playedBy T415nbm9 {\n" +
			    "        callin void test() {\n" +
			    "            base.test();\n" +
			    "        }\n" +
			    "        void test() <- replace void test(String arg1, short arg2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds as 'replace' to a base method with a signature that is different from the signature of the actual base method
    // 4.1.5-otjld-nonexisting-base-method-10
    public void test415_nonexistingBaseMethod10() {
        runNegativeTest(
            new String[] {
		"T415nbm10.java",
			    "\n" +
			    "public class T415nbm10 {\n" +
			    "    public void test(int arg1, int arg2) {}\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm10.java",
			    "\n" +
			    "public team class Team415nbm10 {\n" +
			    "\n" +
			    "    public class Role415nbm10 playedBy T415nbm10 {\n" +
			    "        callin void test1(long arg1, short arg2) {\n" +
			    "            base.test1(arg1, arg2);\n" +
			    "        }\n" +
			    "        test1 <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds to a base method with a signature that has less parameters than the real base method
    // 4.1.5-otjld-nonexisting-base-method-11
    public void test415_nonexistingBaseMethod11() {
        runNegativeTest(
            new String[] {
		"T415nbm11.java",
			    "\n" +
			    "public class T415nbm11 {\n" +
			    "    public void test(String arg) {}\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm11.java",
			    "\n" +
			    "public team class Team415nbm11 {\n" +
			    "\n" +
			    "    public class Role415nbm11 playedBy T415nbm11 {\n" +
			    "        public void test() {}\n" +
			    "        void test() <- after void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds to a base method with a signature that has more parameters than the real base method
    // 4.1.5-otjld-nonexisting-base-method-12
    public void test415_nonexistingBaseMethod12() {
        runNegativeTest(
            new String[] {
		"T415nbm12.java",
			    "\n" +
			    "public class T415nbm12 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm12.java",
			    "\n" +
			    "public team class Team415nbm12 {\n" +
			    "\n" +
			    "    public class Role415nbm12 playedBy T415nbm12 {\n" +
			    "        public void test1(String arg) {}\n" +
			    "        void test1(String arg) <- after void test(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds a role method with more parameters than the bound base method
    // 4.1.5-otjld-nonexisting-base-method-13
    public void test415_nonexistingBaseMethod13() {
        runNegativeTest(
            new String[] {
		"T415nbm13.java",
			    "\n" +
			    "public class T415nbm13 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm13.java",
			    "\n" +
			    "public team class Team415nbm13 {\n" +
			    "\n" +
			    "    public class Role415nbm13 playedBy T415nbm13 {\n" +
			    "        public void test1(String arg) {}\n" +
			    "        test1 <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds as 'replace' a role method with more parameters than the bound base method
    // 4.1.5-otjld-nonexisting-base-method-14
    public void test415_nonexistingBaseMethod14() {
        runNegativeTest(
            new String[] {
		"T415nbm14.java",
			    "\n" +
			    "public class T415nbm14 {\n" +
			    "    public void test(String arg) {}\n" +
			    "}\n" +
			    "    \n",
		"Team415nbm14.java",
			    "\n" +
			    "public team class Team415nbm14 {\n" +
			    "\n" +
			    "    public class Role415nbm14 playedBy T415nbm14 {\n" +
			    "        callin void test(String arg1, String arg2) {\n" +
			    "            base.test(arg1, arg2);\n" +
			    "        }\n" +
			    "        test <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds as 'replace' to a base method while omitting the return type
    // 4.1.5-otjld-nonexisting-base-method-15
    public void test415_nonexistingBaseMethod15() {
        runNegativeTest(
            new String[] {
		"Team415nbm15.java",
			    "\n" +
			    "public team class Team415nbm15 {\n" +
			    "\n" +
			    "    public class Role415nbm15 playedBy T415nbm15 {\n" +
			    "        test <- replace test(String a);\n" +
			    "        callin void test(String arg1) {\n" +
			    "            base.test(arg1);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T415nbm15.java",
			    "\n" +
			    "public class T415nbm15 {\n" +
			    "    public int test(String arg) { return 3; }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team415nbm15.java (at line 5)\n" + 
    		"	test <- replace test(String a);\n" + 
    		"	        ^^^^^^^\n" + 
    		"Syntax error on token \"replace\", invalid MethodSpecsShort\n" + 
    		"----------\n");
    }

    // a callin-binding states a modifier
    // 4.1.6-otjld-callin-binding-states-modifier-1
    public void test416_callinBindingStatesModifier1() {
        runNegativeTestMatching(
            new String[] {
		"Team416bsm1.java",
			    "\n" +
			    "public team class Team416bsm1 {\n" +
			    "\n" +
			    "    public class Role416bsm1 playedBy T416bsm1 {\n" +
			    "        public void test() {}\n" +
			    "        public void test() <- before void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T416bsm1.java",
			    "\n" +
			    "public class T416bsm1 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.3");
    }

    // a callin-binding states a modifier
    // 4.1.6-otjld-callin-binding-states-modifier-2
    public void test416_callinBindingStatesModifier2() {
        runNegativeTestMatching(
            new String[] {
		"Team416bsm2.java",
			    "\n" +
			    "public team class Team416bsm2 {\n" +
			    "\n" +
			    "    public class Role416bsm2 playedBy T416bsm2 {\n" +
			    "        public void doSomething() {}\n" +
			    "        void doSomething() <- after private void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T416bsm2.java",
			    "\n" +
			    "public class T416bsm2 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.5");
    }

    // a callin-binding states a modifier
    // 4.1.6-otjld-callin-binding-states-modifier-3
    public void test416_callinBindingStatesModifier3() {
        runNegativeTestMatching(
            new String[] {
		"Team416bsm3.java",
			    "\n" +
			    "public team class Team416bsm3 {\n" +
			    "\n" +
			    "    public class Role416bsm3 playedBy T416bsm3 {\n" +
			    "        callin void test(String arg) {\n" +
			    "            base.test(arg);\n" +
			    "        }\n" +
			    "        void test(String arg) <- replace abstract void test(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T416bsm3.java",
			    "\n" +
			    "public abstract class T416bsm3 {\n" +
			    "    public abstract void test(String arg);\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.5");
    }

    // a callin-binding states a modifier
    // 4.1.6-otjld-callin-binding-states-modifier-4
    public void test416_callinBindingStatesModifier4() {
        runNegativeTestMatching(
            new String[] {
		"Team416bsm4.java",
			    "\n" +
			    "public team class Team416bsm4 {\n" +
			    "\n" +
			    "    public class Role416bsm4 playedBy T416bsm4 {\n" +
			    "        callin void doSomething(String arg) {\n" +
			    "            base.doSomething(arg);\n" +
			    "        }\n" +
			    "        static void doSomething(String arg) <- replace void test(String arg1, String arg2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T416bsm4.java",
			    "\n" +
			    "public class T416bsm4 {\n" +
			    "    public void test(String arg1, String arg2) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.3");
    }

    // a callin-binding states a modifier
    // 4.1.6-otjld-callin-binding-states-modifier-5
    public void test416_callinBindingStatesModifier5() {
        runNegativeTestMatching(
            new String[] {
		"Team416bsm5.java",
			    "\n" +
			    "public team class Team416bsm5 {\n" +
			    "\n" +
			    "    public class Role416bsm5 playedBy T416bsm5 {\n" +
			    "        public void doSomething(String arg) {}\n" +
			    "        void doSomething(String arg) <- before synchronized void test(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T416bsm5.java",
			    "\n" +
			    "public class T416bsm5 {\n" +
			    "    public synchronized void test(String arg) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.5");
    }

    // a callin-binding states a modifier
    // 4.1.6-otjld-callin-binding-states-modifier-6
    public void test416_callinBindingStatesModifier6() {
        runNegativeTestMatching(
            new String[] {
		"Team416bsm6.java",
			    "\n" +
			    "public team class Team416bsm6 {\n" +
			    "\n" +
			    "    public class Role416bsm6 playedBy T416bsm6 {\n" +
			    "        protected void test() {}\n" +
			    "        protected test <- after test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T416bsm6.java",
			    "\n" +
			    "public class T416bsm6 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.3");
    }

    // a callin-binding states a modifier
    // 4.1.6-otjld-callin-binding-states-modifier-7
    public void test416_callinBindingStatesModifier7() {
        runNegativeTestMatching(
            new String[] {
		"Team416bsm7.java",
			    "\n" +
			    "public team class Team416bsm7 {\n" +
			    "\n" +
			    "    public class Role416bsm7 playedBy T416bsm7 {\n" +
			    "        callin void doSomething(String arg) {\n" +
			    "            base.doSomething(arg);\n" +
			    "        }\n" +
			    "        doSomething <- replace abstract test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T416bsm7.java",
			    "\n" +
			    "public class T416bsm7 {\n" +
			    "    public void test(String arg) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "Syntax");
    }

    // a callin-binding uses <= instead of <-
    // 4.1.7-otjld-wrong-callin-binding-operator
    public void test417_wrongCallinBindingOperator() {
        runNegativeTest(
            new String[] {
		"T417wcbo.java",
			    "\n" +
			    "public class T417wcbo {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team417wcbo.java",
			    "\n" +
			    "public team class Team417wcbo {\n" +
			    "\n" +
			    "    public class Role417wcbo playedBy T417wcbo {\n" +
			    "        protected void test() {}\n" +
			    "        test <= after test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a regular method is replace-bound
    // 4.1.8-otjld-wrong-callin-combination-1
    public void test418_wrongCallinCombination1() {
        runNegativeTestMatching(
            new String[] {
		"Team418wcc1.java",
			    "\n" +
			    "import base java.util.ArrayList;\n" +
			    "public team class Team418wcc1 {\n" +
			    "    @SuppressWarnings(\"unchecked\")\n" +
			    "    protected class R playedBy ArrayList {\n" +
			    "        void wrong() {}\n" +
			    "        wrong <- replace size;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.2(d)");
    }

    // a role class callin-binds a non-existing role method
    // 4.1.9-otjld-nonexisting-role-method-1
    public void test419_nonexistingRoleMethod1() {
        runNegativeTest(
            new String[] {
		"T419nrm1.java",
			    "\n" +
			    "public class T419nrm1 {\n" +
			    "    public String getValue(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team419nrm1.java",
			    "\n" +
			    "public team class Team419nrm1 {\n" +
			    "    public class Role419nrm1 playedBy T419nrm1 {\n" +
			    "        String getValue(String arg) <- after String getValue(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds a non-existing role method
    // 4.1.9-otjld-nonexisting-role-method-2
    public void test419_nonexistingRoleMethod2() {
        runNegativeTest(
            new String[] {
		"T419nrm2.java",
			    "\n" +
			    "public class T419nrm2 {\n" +
			    "    public String getValue(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team419nrm2.java",
			    "\n" +
			    "public team class Team419nrm2 {\n" +
			    "\n" +
			    "    public class Role419nrm2 playedBy T419nrm2 {\n" +
			    "        public String getValue(String arg) {\n" +
			    "            return arg;\n" +
			    "        }\n" +
			    "        getValue1 <- after getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds a not-accessible role method
    // 4.1.9-otjld-nonexisting-role-method-3
    public void test419_nonexistingRoleMethod3() {
        runNegativeTest(
            new String[] {
		"T419nrm3.java",
			    "\n" +
			    "public class T419nrm3 {\n" +
			    "    public void test(String arg) {}\n" +
			    "}\n" +
			    "    \n",
		"Team419nrm3.java",
			    "\n" +
			    "public team class Team419nrm3 {\n" +
			    "    public class Role419nrm3_1 {\n" +
			    "        private void test() {}\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role419nrm3_2 extends Role419nrm3_1 playedBy T419nrm3 {\n" +
			    "        void test() <- before void test(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds a non-protected role method
    // 4.1.9-otjld-nonexisting-role-method-4
    public void test419_nonexistingRoleMethod4() {
       
       runConformTest(
            new String[] {
		"p2/Team419nrm4_2.java",
			    "\n" +
			    "package p2;\n" +
			    "import base p1.T419nrm4;\n" +
			    "public team class Team419nrm4_2 extends p1.Team419nrm4_1 {\n" +
			    "    public class Role419nrm4 playedBy T419nrm4 {\n" +
			    "    	// check is indeed visible along implicit inheritance\n" +
			    "        check <- before test;\n" +
			    "    }\n" +
			    "    Team419nrm4_2() { activate(); }\n" +
			    "    public static void main(String[] args) {\n" +
			    "    	new Team419nrm4_2();\n" +
			    "	(new p1.T419nrm4()).test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T419nrm4.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T419nrm4 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team419nrm4_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team419nrm4_1 {\n" +
			    "    public class Role419nrm4 {\n" +
			    "        void check() {\n" +
			    "	    System.out.print(\"O\");\n" +
			    "	}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class callin-binds a role method with a signature that has no return type whereas the actual role method has one
    // 4.1.9-otjld-nonexisting-role-method-5
    public void test419_nonexistingRoleMethod5() {
        runNegativeTest(
            new String[] {
		"T419nrm5.java",
			    "\n" +
			    "public class T419nrm5 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team419nrm5.java",
			    "\n" +
			    "public team class Team419nrm5 {\n" +
			    "\n" +
			    "    public class Role419nrm5 playedBy T419nrm5 {\n" +
			    "        public String getValue() {\n" +
			    "            return null;\n" +
			    "        }\n" +
			    "        void getValue() <- before void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds a role method with a signature that has a return type whereas the actual role method has none
    // 4.1.9-otjld-nonexisting-role-method-6
    public void test419_nonexistingRoleMethod6() {
        runNegativeTest(
            new String[] {
		"T419nrm6.java",
			    "\n" +
			    "public class T419nrm6 {\n" +
			    "    public String test() {\n" +
			    "        return null;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team419nrm6.java",
			    "\n" +
			    "public team class Team419nrm6 {\n" +
			    "\n" +
			    "    public class Role419nrm6 playedBy T419nrm6 {\n" +
			    "        protected void test() {}\n" +
			    "        String test() <- after String test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds as 'replace' a role method with a signature that is different from the signature of the actual role method
    // 4.1.9-otjld-nonexisting-role-method-7
    public void test419_nonexistingRoleMethod7() {
        runNegativeTest(
            new String[] {
		"T419nrm7.java",
			    "\n" +
			    "public class T419nrm7 {\n" +
			    "    public void test(String arg1, int arg2) {}\n" +
			    "}\n" +
			    "    \n",
		"Team419nrm7.java",
			    "\n" +
			    "public team class Team419nrm7 {\n" +
			    "\n" +
			    "    public class Role419nrm7 playedBy T419nrm7 {\n" +
			    "        callin void test(String arg1, int arg2) {\n" +
			    "            base.test(arg1, arg2);\n" +
			    "        }\n" +
			    "        void test(String arg1, short arg2) <- replace void test(String arg1, int arg2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds a role method with a signature that has less parameters than the real role method
    // 4.1.9-otjld-nonexisting-role-method-8
    public void test419_nonexistingRoleMethod8() {
        runNegativeTest(
            new String[] {
		"T419nrm8.java",
			    "\n" +
			    "public class T419nrm8 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team419nrm8.java",
			    "\n" +
			    "public team class Team419nrm8 {\n" +
			    "\n" +
			    "    public class Role419nrm8 playedBy T419nrm8 {\n" +
			    "        public void test(String arg) {}\n" +
			    "        void test() <- after void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role class callin-binds a role method with a signature that has more parameters than the real role method
    // 4.1.9-otjld-nonexisting-role-method-9
    public void test419_nonexistingRoleMethod9() {
        runNegativeTest(
            new String[] {
		"T419nrm9.java",
			    "\n" +
			    "public class T419nrm9 {\n" +
			    "    public void test(String arg) {}\n" +
			    "}\n" +
			    "    \n",
		"Team419nrm9.java",
			    "\n" +
			    "public team class Team419nrm9 {\n" +
			    "\n" +
			    "    public class Role419nrm9 playedBy T419nrm9 {\n" +
			    "        public void test1() {}\n" +
			    "        void test1(String arg) <- after void test(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a callin-binding refers to a base method instead of a role method
    // 4.1.10-otjld-callin-binding-between-base-methods
    public void test4110_callinBindingBetweenBaseMethods() {
        runNegativeTest(
            new String[] {
		"T4110cbbbm.java",
			    "\n" +
			    "public class T4110cbbbm {\n" +
			    "    public void test() {}\n" +
			    "    public void doSomething() {}\n" +
			    "}\n" +
			    "    \n",
		"Team4110cbbbm.java",
			    "\n" +
			    "public team class Team4110cbbbm {\n" +
			    "\n" +
			    "    public class Role4110cbbbm playedBy T4110cbbbm {\n" +
			    "        public void test() {}\n" +
			    "        doSomething <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role method is callin-bound as 'before' to multiple base methods
    // 4.1.12-otjld-callin-binding-multiple-base-methods-1
    public void test4112_callinBindingMultipleBaseMethods1() {
       
       runConformTest(
            new String[] {
		"T4112cbmbm1Main.java",
			    "\n" +
			    "public class T4112cbmbm1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4112cbmbm1 t = new Team4112cbmbm1();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "\n" +
			    "        T4112cbmbm1_3 o = new T4112cbmbm1_3();\n" +
			    "\n" +
			    "        T4112cbmbm1_1.addValue(o.getValue(\"a\"));\n" +
			    "        o.test(\"b\");\n" +
			    "\n" +
			    "        System.out.print(T4112cbmbm1_1.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm1_1.java",
			    "\n" +
			    "public class T4112cbmbm1_1 {\n" +
			    "    private static String value = \"\";\n" +
			    "    public static void addValue(String arg) {\n" +
			    "        value += \"|\"+arg+\"|\";\n" +
			    "    }\n" +
			    "    public static String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm1_2.java",
			    "\n" +
			    "public class T4112cbmbm1_2 {\n" +
			    "    public String getValue(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm1_3.java",
			    "\n" +
			    "public class T4112cbmbm1_3 extends T4112cbmbm1_2 {\n" +
			    "    public void test(String arg) {\n" +
			    "        T4112cbmbm1_1.addValue(arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4112cbmbm1.java",
			    "\n" +
			    "public team class Team4112cbmbm1 {\n" +
			    "    public class Role4112cbmbm1_1 playedBy T4112cbmbm1_2 {\n" +
			    "        private int counter = 0;\n" +
			    "        protected void test(String arg) {\n" +
			    "            T4112cbmbm1_1.addValue(String.valueOf(++counter)+\":\"+arg);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class Role4112cbmbm1_2 extends Role4112cbmbm1_1 playedBy T4112cbmbm1_3 {\n" +
			    "        test <- before test, getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "|1:a||a||2:b||b|");
    }

    // a role method is callin-bound as 'after' to multiple base methods
    // 4.1.12-otjld-callin-binding-multiple-base-methods-2
    public void test4112_callinBindingMultipleBaseMethods2() {
       
       runConformTest(
            new String[] {
		"T4112cbmbm2Main.java",
			    "\n" +
			    "public class T4112cbmbm2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4112cbmbm2_2 t = new Team4112cbmbm2_2();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            T4112cbmbm2_4 o = new T4112cbmbm2_4();\n" +
			    "    \n" +
			    "            T4112cbmbm2_1.addValue(o.getValue(\"a\").toString());\n" +
			    "            T4112cbmbm2_1.addValue(o.test2(\"c\", \"d\").toString());\n" +
			    "            T4112cbmbm2_1.addValue(o.test1(\"b\").toString());\n" +
			    "        }\n" +
			    "\n" +
			    "        System.out.print(T4112cbmbm2_1.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm2_1.java",
			    "\n" +
			    "public class T4112cbmbm2_1 {\n" +
			    "    private static String value = \"\";\n" +
			    "    public static void addValue(String arg) {\n" +
			    "        value += \"|\"+arg+\"|\";\n" +
			    "    }\n" +
			    "    public static String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm2_2.java",
			    "\n" +
			    "public class T4112cbmbm2_2 {\n" +
			    "    public Object getValue(String arg) {\n" +
			    "        T4112cbmbm2_1.addValue(arg);\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm2_3.java",
			    "\n" +
			    "public class T4112cbmbm2_3 extends T4112cbmbm2_2 {}\n" +
			    "    \n",
		"T4112cbmbm2_4.java",
			    "\n" +
			    "public class T4112cbmbm2_4 extends T4112cbmbm2_3 {\n" +
			    "    public String test1(String arg) {\n" +
			    "        T4112cbmbm2_1.addValue(arg);\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "    public Object test2(String arg1, String arg2) {\n" +
			    "        T4112cbmbm2_1.addValue(arg1 + arg2);\n" +
			    "        return arg1 + arg2;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4112cbmbm2_1.java",
			    "\n" +
			    "public team class Team4112cbmbm2_1 {\n" +
			    "    public class Role4112cbmbm2 {\n" +
			    "        private int counter = 0;\n" +
			    "        Object test(String arg) {\n" +
			    "            T4112cbmbm2_1.addValue(String.valueOf(++counter)+\":\"+arg);\n" +
			    "            return null;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4112cbmbm2_2.java",
			    "\n" +
			    "public team class Team4112cbmbm2_2 extends Team4112cbmbm2_1 {\n" +
			    "    public class Role4112cbmbm2 playedBy T4112cbmbm2_4 {\n" +
			    "        Object test(String arg) <- after String test1(String arg),\n" +
			    "                                         Object test2(String arg1, String arg2),\n" +
			    "                                         Object getValue(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "|a||1:a||a||cd||2:c||cd||b||3:b||b|");
    }

    // a role method is callin-bound as 'replace' to multiple base methods
    // 4.1.12-otjld-callin-binding-multiple-base-methods-3
    public void test4112_callinBindingMultipleBaseMethods3() {
       
       runConformTest(
            new String[] {
		"T4112cbmbm3Main.java",
			    "\n" +
			    "public class T4112cbmbm3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4112cbmbm3 t = new Team4112cbmbm3();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "\n" +
			    "        T4112cbmbm3_2 o = new T4112cbmbm3_2();\n" +
			    "\n" +
			    "        T4112cbmbm3_1.addValue(o.test1(1));\n" +
			    "        o.test2(2, 3);\n" +
			    "\n" +
			    "        System.out.print(T4112cbmbm3_1.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm3_1.java",
			    "\n" +
			    "public class T4112cbmbm3_1 {\n" +
			    "    private static String value = \"\";\n" +
			    "    public static void addValue(long arg) {\n" +
			    "        value += \"|\"+arg+\"|\";\n" +
			    "    }\n" +
			    "    public static void addValue(String arg) {\n" +
			    "        value += \"|\"+arg+\"|\";\n" +
			    "    }\n" +
			    "    public static String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm3_2.java",
			    "\n" +
			    "public class T4112cbmbm3_2 {\n" +
			    "    public int test1(int arg) {\n" +
			    "        return arg - 1;\n" +
			    "    }\n" +
			    "    public void test2(int arg1, int arg2) {\n" +
			    "        T4112cbmbm3_1.addValue(arg1 * arg2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4112cbmbm3.java",
			    "\n" +
			    "public team class Team4112cbmbm3 {\n" +
			    "    public class Role4112cbmbm3 playedBy T4112cbmbm3_2 {\n" +
			    "        private int counter = 0;\n" +
			    "        callin void test(int arg) {\n" +
			    "            T4112cbmbm3_1.addValue(String.valueOf(++counter)+\":\"+arg);\n" +
			    "            base.test(0);\n" +
			    "        }\n" +
			    "        // fragile binding -> warning\n" +
			    "        void test(int arg) <- replace int test1(int arg), void test2(int arg1, int arg2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "|1:1||-1||2:2||0|");
       // FIXME  expect this warning
    }


    // a role method is callin-bound as 'replace' to multiple base methods - this requires exact signature matches
    // 4.1.12-otjld-callin-binding-multiple-base-methods-5
    public void test4112_callinBindingMultipleBaseMethods5() {
       
       runConformTest(
            new String[] {
		"T4112cbmbm5Main.java",
			    "\n" +
			    "public class T4112cbmbm5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4112cbmbm5_2 t = new Team4112cbmbm5_2();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            T4112cbmbm5_4 o = new T4112cbmbm5_4();\n" +
			    "\n" +
			    "            T4112cbmbm5_1.addValue(o.test3(\"a\",\"X\").toString());\n" +
			    "            T4112cbmbm5_1.addValue(o.test2(\"c\",\"Y\").toString());\n" +
			    "            T4112cbmbm5_1.addValue(o.test1(\"b\").toString());\n" +
			    "        }\n" +
			    "\n" +
			    "        System.out.print(T4112cbmbm5_1.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm5_1.java",
			    "\n" +
			    "public class T4112cbmbm5_1 {\n" +
			    "    private static String value = \"\";\n" +
			    "    public static void addValues(String arg1, String arg2) {\n" +
			    "        value += \"|\"+arg1+\"#\"+arg2+\"|\";\n" +
			    "    }\n" +
			    "    public static void addValue(String arg) {\n" +
			    "        value += \"|\"+arg+\"|\";\n" +
			    "    }\n" +
			    "    public static String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm5_2.java",
			    "\n" +
			    "public class T4112cbmbm5_2 {\n" +
			    "    public Object test1(Object arg) {\n" +
			    "        T4112cbmbm5_1.addValue(arg.toString());\n" +
			    "        return arg.toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm5_3.java",
			    "\n" +
			    "public class T4112cbmbm5_3 extends T4112cbmbm5_2 {\n" +
			    "    Object test2(Object arg, String str) {\n" +
			    "        T4112cbmbm5_1.addValues(arg.toString(), \"->\"+str);\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm5_4.java",
			    "\n" +
			    "public class T4112cbmbm5_4 extends T4112cbmbm5_3 {\n" +
			    "    Object test3(String arg, Object o) {\n" +
			    "        T4112cbmbm5_1.addValues(arg, o.toString());\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4112cbmbm5_1.java",
			    "\n" +
			    "public team class Team4112cbmbm5_1 {\n" +
			    "    public class Role4112cbmbm5_1 playedBy T4112cbmbm5_2 {\n" +
			    "        private int counter = 0;\n" +
			    "        callin Object test(Object arg) {\n" +
			    "            T4112cbmbm5_1.addValue(String.valueOf(++counter)+\":\"+arg.toString());\n" +
			    "            return base.test(\"-\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"Team4112cbmbm5_2.java",
			    "\n" +
			    "public team class Team4112cbmbm5_2 extends Team4112cbmbm5_1 {\n" +
			    "    public class Role4112cbmbm5_2 extends Role4112cbmbm5_1 playedBy T4112cbmbm5_4 {\n" +
			    "	test <- replace test1, test2;\n" +
			    "	Object test(Object o1) <- replace Object test3(String s1, Object o2) with {\n" +
			    "		o1 <- o2,\n" +
			    "		result -> result\n" +
			    "	}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "|1:X||a#-||a||2:c||-#->Y||-||3:b||-||-|");
       // FIXME  expect this warning
    }

    // a role method is callin-bound as 'replace'  where matching would require (two-way) polymorphism
    // 4.1.12-otjld-callin-binding-multiple-base-methods-6
    public void test4112_callinBindingMultipleBaseMethods6() {
        runNegativeTest(
            new String[] {
		"T4112cbmbm6_1.java",
			    "\n" +
			    "public class T4112cbmbm6_1 {\n" +
			    "    private static String value = \"\";\n" +
			    "    public static void addValue(String arg) {\n" +
			    "        value += \"|\"+arg+\"|\";\n" +
			    "    }\n" +
			    "    public static String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm6_2.java",
			    "\n" +
			    "public class T4112cbmbm6_2 {\n" +
			    "    public String test1(Object arg) {\n" +
			    "        T4112cbmbm6_1.addValue(arg.toString());\n" +
			    "        return arg.toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4112cbmbm6_3.java",
			    "\n" +
			    "public class T4112cbmbm6_3 extends T4112cbmbm6_2 {\n" +
			    "    Object test2(Object arg) {\n" +
			    "        T4112cbmbm6_1.addValue(arg.toString());\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4112cbmbm6_1.java",
			    "\n" +
			    "public team class Team4112cbmbm6_1 {\n" +
			    "    public class Role4112cbmbm6_1 playedBy T4112cbmbm6_2 {\n" +
			    "        private int counter = 0;\n" +
			    "        // no base call\n" +
			    "        callin String test(Object arg) {\n" +
			    "            T4112cbmbm6_1.addValue(String.valueOf(++counter)+\":\"+arg.toString());\n" +
			    "            return \"-\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"Team4112cbmbm6_2.java",
			    "\n" +
			    "public team class Team4112cbmbm6_2 extends Team4112cbmbm6_1 {\n" +
			    "    public class Role4112cbmbm6_2 extends Role4112cbmbm6_1 playedBy T4112cbmbm6_3 {\n" +
			    "        test <- replace test2;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
        // FIXME  expect this warning
    }

    // a role method without a return type is callin-bound as 'before' to a base method with a return type
    // 4.1.13-otjld-base-return-value-1
    public void test4113_baseReturnValue1() {
       
       runConformTest(
            new String[] {
		"T4113brv1Main.java",
			    "\n" +
			    "public class T4113brv1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4113brv1 t = new Team4113brv1();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            T4113brv1 o = new T4113brv1();\n" +
			    "\n" +
			    "            System.out.print(o.getValue());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4113brv1.java",
			    "\n" +
			    "public class T4113brv1 {\n" +
			    "    public static String value = null;\n" +
			    "    public String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4113brv1.java",
			    "\n" +
			    "public team class Team4113brv1 {\n" +
			    "\n" +
			    "    public class Role4113brv1 playedBy T4113brv1 {\n" +
			    "        public void test() {\n" +
			    "            T4113brv1.value = \"OK\";\n" +
			    "        }\n" +
			    "        void test() <- before String getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role method without a return type is callin-bound as 'after' to a base method with a return type
    // 4.1.13-otjld-base-return-value-2
    public void test4113_baseReturnValue2() {
       
       runConformTest(
            new String[] {
		"T4113brv2Main.java",
			    "\n" +
			    "public class T4113brv2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4113brv2 t = new Team4113brv2();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            T4113brv2 o = new T4113brv2();\n" +
			    "\n" +
			    "            System.out.print(o.getValue());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4113brv2.java",
			    "\n" +
			    "public class T4113brv2 {\n" +
			    "    public static String value = \"OK\";\n" +
			    "    public String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4113brv2.java",
			    "\n" +
			    "public team class Team4113brv2 {\n" +
			    "\n" +
			    "    public class Role4113brv2 playedBy T4113brv2 {\n" +
			    "        public void test() {\n" +
			    "            T4113brv2.value = \"NOTOK\";\n" +
			    "        }\n" +
			    "        test <- after getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role method without a return type is callin-bound as 'replace' to a base method with a return type , "return" must be translated to return null
    // 4.1.13-otjld-base-return-value-3
    public void test4113_baseReturnValue3() {
       
       runConformTest(
            new String[] {
		"T4113brv3Main.java",
			    "\n" +
			    "public class T4113brv3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4113brv3 t = new Team4113brv3();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            T4113brv3 o = new T4113brv3();\n" +
			    "\n" +
			    "            System.out.print(o.getValue());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4113brv3.java",
			    "\n" +
			    "public class T4113brv3 {\n" +
			    "    public static String value = \"WRONG\";\n" +
			    "    public String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4113brv3.java",
			    "\n" +
			    "public team class Team4113brv3 {\n" +
			    "\n" +
			    "    public class Role4113brv3 playedBy T4113brv3 {\n" +
			    "        callin void test(String val) {\n" +
			    "            T4113brv3.value = \"OK\";\n" +
			    "            if (\"NOTOK\".equals(val)) {\n" +
			    "                base.test(val);\n" +
			    "                return;\n" +
			    "            } else {\n" +
			    "                base.test(\"even worse\");\n" +
			    "            }\n" +
			    "            System.out.print(\"shouldn't reach here\");\n" +
			    "        }\n" +
			    "        void test(String v) <- replace String getValue()\n" +
			    "            with { v <- \"NOTOK\" }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role method without a return type is callin-bound as 'replace' to a base method with a base return type
    // 4.1.13-otjld-base-return-value-4
    public void test4113_baseReturnValue4() {
       
       runConformTest(
            new String[] {
		"T4113brv4Main.java",
			    "\n" +
			    "public class T4113brv4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4113brv4 t = new Team4113brv4();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            T4113brv4 o = new T4113brv4();\n" +
			    "\n" +
			    "            System.out.print(o.getValue());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4113brv4.java",
			    "\n" +
			    "public class T4113brv4 {\n" +
			    "    public static char value = '?';\n" +
			    "    public char getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4113brv4.java",
			    "\n" +
			    "public team class Team4113brv4 {\n" +
			    "\n" +
			    "    public class Role4113brv4 playedBy T4113brv4 {\n" +
			    "        callin void test(char val) {\n" +
			    "            T4113brv4.value = '!';\n" +
			    "            if (val == '#') {\n" +
			    "                base.test(val);\n" +
			    "                return;\n" +
			    "            } else {\n" +
			    "                base.test('@');\n" +
			    "            }\n" +
			    "            System.out.print(\"shouldn't reach here\");\n" +
			    "        }\n" +
			    "        void test(char v) <- replace char getValue()\n" +
			    "            with { v <- '#' }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "!");
    }

    // a role method a base return type is callin-bound as 'replace', testing boxing/unboxing in translated code
    // 4.1.13-otjld-base-return-value-5
    public void test4113_baseReturnValue5() {
       
       runConformTest(
            new String[] {
		"T4113brv5Main.java",
			    "\n" +
			    "public class T4113brv5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4113brv5 t = new Team4113brv5();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            T4113brv5 o = new T4113brv5();\n" +
			    "\n" +
			    "            System.out.print(o.getValue());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4113brv5.java",
			    "\n" +
			    "public class T4113brv5 {\n" +
			    "    public static char value = '?';\n" +
			    "    public char getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4113brv5.java",
			    "\n" +
			    "public team class Team4113brv5 {\n" +
			    "\n" +
			    "    public class Role4113brv5 playedBy T4113brv5 {\n" +
			    "        callin char test(char val) {\n" +
			    "            T4113brv5.value = '!';\n" +
			    "            if (val == '#') {\n" +
			    "                char ignore= base.test(val);\n" +
			    "                return '$';\n" +
			    "            } else {\n" +
			    "                char ignore2= base.test('@');\n" +
			    "            }\n" +
			    "            System.out.print(\"shouldn't reach here\");\n" +
			    "            return '-';\n" +
			    "        }\n" +
			    "        char test(char v) <- replace char getValue()\n" +
			    "            with { v <- '#' }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "$");
    }

    // a role method a base return type is callin-bound as 'replace', testing boxing/unboxing in translated code: type error
    // 4.1.13-otjld-base-return-value-6
    public void test4113_baseReturnValue6() {
        runNegativeTestMatching(
            new String[] {
		"Team4113brv6.java",
			    "\n" +
			    "public team class Team4113brv6 {\n" +
			    "\n" +
			    "    public class Role4113brv6 playedBy T4113brv6 {\n" +
			    "        callin char test(char val) {\n" +
			    "            T4113brv6.value = '!';\n" +
			    "            if (val == '#') {\n" +
			    "                char ignore= base.test(val);\n" +
			    "                return 3;\n" +
			    "            } else {\n" +
			    "                char ignore2= base.test('@');\n" +
			    "            }\n" +
			    "            System.out.print(\"shouldn't reach here\");\n" +
			    "            return '-';\n" +
			    "        }\n" +
			    "        char test(char v) <- replace char getValue()\n" +
			    "            with { v <- '#' }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4113brv6.java",
			    "\n" +
			    "public class T4113brv6 {\n" +
			    "    public static char value = '?';\n" +
			    "    public char getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "cannot convert");
    }

    // a role method with a return type is callin-bound as 'before' to a base method without a return type
    // 4.1.15-otjld-ignore-role-method-return-value-1
    public void test4115_ignoreRoleMethodReturnValue1() {
       
       runConformTest(
            new String[] {
		"T4115irmrv1Main.java",
			    "\n" +
			    "public class T4115irmrv1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4115irmrv1 t = new Team4115irmrv1();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "\n" +
			    "        T4115irmrv1 o = new T4115irmrv1();\n" +
			    "\n" +
			    "        o.test();\n" +
			    "        System.out.print(T4115irmrv1.value);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4115irmrv1.java",
			    "\n" +
			    "public class T4115irmrv1 {\n" +
			    "    public static int value = 1;\n" +
			    "    public void test() {\n" +
			    "        value += 10;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4115irmrv1.java",
			    "\n" +
			    "public team class Team4115irmrv1 {\n" +
			    "\n" +
			    "    public class Role4115irmrv1 playedBy T4115irmrv1 {\n" +
			    "        public int test() {\n" +
			    "            return T4115irmrv1.value *= 2;\n" +
			    "        }\n" +
			    "        test <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "12");
    }

    // a role method with a return type is callin-bound as 'after' to a base method without a return type
    // 4.1.15-otjld-ignore-role-method-return-value-2
    public void test4115_ignoreRoleMethodReturnValue2() {
       
       runConformTest(
            new String[] {
		"T4115irmrv2Main.java",
			    "\n" +
			    "public class T4115irmrv2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4115irmrv2 t = new Team4115irmrv2();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "\n" +
			    "        T4115irmrv2 o = new T4115irmrv2();\n" +
			    "\n" +
			    "        o.test();\n" +
			    "        System.out.print(T4115irmrv2.value);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4115irmrv2.java",
			    "\n" +
			    "public class T4115irmrv2 {\n" +
			    "    public static int value = 1;\n" +
			    "    public void test() {\n" +
			    "        value += 10;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4115irmrv2.java",
			    "\n" +
			    "public team class Team4115irmrv2 {\n" +
			    "\n" +
			    "    public class Role4115irmrv2 playedBy T4115irmrv2 {\n" +
			    "        public int getValue() {\n" +
			    "            return T4115irmrv2.value *= 2;\n" +
			    "        }\n" +
			    "        int getValue() <- after void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "22");
    }

    // a role method with a return type is callin-bound as 'after' to a base method with a matching return type
    // expect warning
    public void test4115_ignoreRoleMethodReturnValue3() {
       
       runTestExpectingWarnings(
            new String[] {
		"T4115irmrv3.java",
			    "\n" +
			    "public class T4115irmrv3 {\n" +
			    "    public static int value = 1;\n" +
			    "    public int test() {\n" +
			    "        value += 10;\n" +
			    "		 return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4115irmrv3.java",
			    "\n" +
			    "public team class Team4115irmrv3 {\n" +
			    "\n" +
			    "    public class Role4115irmrv3 playedBy T4115irmrv3 {\n" +
			    "        public int test() {\n" +
			    "            return T4115irmrv3.value *= 2;\n" +
			    "        }\n" +
			    "        test <- after test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. WARNING in Team4115irmrv3.java (at line 8)\n" + 
    		"	test <- after test;\n" + 
    		"	^^^^\n" + 
    		"Callin after binding cannot return a value to the base caller, role method return value of type int will be ignored (OTJLD 4.4(a)).\n" + 
    		"----------\n");
    }

    // a role method with a return type is callin-bound as 'before' to a base method with a matching return type
    // expect no warning, method specs have signatures
    public void test4115_ignoreRoleMethodReturnValue3b() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportIgnoringRoleMethodReturn, CompilerOptions.ERROR);

       runConformTest(
            new String[] {
		"T4115irmrv3bMain.java",
			    "\n" +
			    "public class T4115irmrv3bMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4115irmrv3b t = new Team4115irmrv3b();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "\n" +
			    "        T4115irmrv3b o = new T4115irmrv3b();\n" +
			    "\n" +
			    "        System.out.print(o.test());\n" +
			    "        System.out.print(T4115irmrv3b.value);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4115irmrv3b.java",
			    "\n" +
			    "public class T4115irmrv3b {\n" +
			    "    public static int value = 1;\n" +
			    "    public int test() {\n" +
			    "        value += 10;\n" +
			    "		 return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4115irmrv3b.java",
			    "\n" +
			    "public team class Team4115irmrv3b {\n" +
			    "\n" +
			    "    public class Role4115irmrv3b playedBy T4115irmrv3b {\n" +
			    "        public int test() {\n" +
			    "            return T4115irmrv3b.value *= 2;\n" +
			    "        }\n" +
			    "        int test() <- before int test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1212",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a role method with a return type is callin-bound as 'after' to a base method with a matching return type
    // expect warning, method specs have signatures
    public void test4115_ignoreRoleMethodReturnValue3s() {
        Map customOptions = getCompilerOptions();
        customOptions.put(CompilerOptions.OPTION_ReportIgnoringRoleMethodReturn, CompilerOptions.ERROR);

        runNegativeTest(
            new String[] {
		"T4115irmrv3s.java",
			    "\n" +
			    "public class T4115irmrv3s {\n" +
			    "    public static int value = 1;\n" +
			    "    public int test() {\n" +
			    "        value += 10;\n" +
			    "		 return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4115irmrv3s.java",
			    "\n" +
			    "public team class Team4115irmrv3s {\n" +
			    "\n" +
			    "    public class Role4115irmrv3s playedBy T4115irmrv3s {\n" +
			    "        public int test() {\n" +
			    "            return T4115irmrv3s.value *= 2;\n" +
			    "        }\n" +
			    "        int test() <- after int test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team4115irmrv3s.java (at line 8)\n" + 
    		"	int test() <- after int test();\n" + 
    		"	^^^\n" + 
    		"Callin after binding cannot return a value to the base caller, role method return value of type int will be ignored (OTJLD 4.4(a)).\n" + 
    		"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }

    // a role method with a return type is callin-bound as 'after' to a base method without a return type
    // warning suppressed
    public void test4115_ignoreRoleMethodReturnValue4() {
       
       runConformTest(
            new String[] {
		"T4115irmrv4Main.java",
			    "\n" +
			    "public class T4115irmrv4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4115irmrv4 t = new Team4115irmrv4();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "\n" +
			    "        T4115irmrv4 o = new T4115irmrv4();\n" +
			    "\n" +
			    "        System.out.print(o.test());\n" +
			    "        System.out.print(T4115irmrv4.value);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4115irmrv4.java",
			    "\n" +
			    "public class T4115irmrv4 {\n" +
			    "    public static int value = 1;\n" +
			    "    public int test() {\n" +
			    "        value += 10;\n" +
			    "		 return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4115irmrv4.java",
			    "\n" +
			    "public team class Team4115irmrv4 {\n" +
			    "\n" +
			    "    public class Role4115irmrv4 playedBy T4115irmrv4 {\n" +
			    "        public int getValue() {\n" +
			    "            return T4115irmrv4.value *= 2;\n" +
			    "        }\n" +
			    "		 @SuppressWarnings(\"ignoredresult\")\n" +
			    "        int getValue() <- after int test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1122");
    }

    // a role method is callin-bound as 'replace' to a base method that has a non-conforming return type
    // 4.1.16-otjld-non-conforming-return-types-1
    public void test4116_nonConformingReturnTypes1() {
        runNegativeTest(
            new String[] {
		"T4116ncrt1.java",
			    "\n" +
			    "public class T4116ncrt1 {\n" +
			    "    public int test() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4116ncrt1.java",
			    "\n" +
			    "public team class Team4116ncrt1 {\n" +
			    "\n" +
			    "    public class Role4116ncrt1 playedBy T4116ncrt1 {\n" +
			    "        callin short test() {\n" +
			    "            // it would work if not for the base call\n" +
			    "            return base.test();\n" +
			    "        }\n" +
			    "        short test() <- replace int test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role method is callin-bound as 'replace' to a base method that has a non-conforming return type
    // 4.1.16-otjld-non-conforming-return-types-2
    public void test4116_nonConformingReturnTypes2() {
        runNegativeTest(
            new String[] {
		"T4116ncrt2.java",
			    "\n" +
			    "public class T4116ncrt2 {\n" +
			    "    public short test() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4116ncrt2.java",
			    "\n" +
			    "public team class Team4116ncrt2 {\n" +
			    "\n" +
			    "    public class Role4116ncrt2 playedBy T4116ncrt2 {\n" +
			    "        callin int test() {\n" +
			    "            return base.test();\n" +
			    "        }\n" +
			    "        int test() <- replace short test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role method is callin-bound as 'replace' to a base method that has a non-conforming return type
    // 4.1.16-otjld-non-conforming-return-types-3
    public void test4116_nonConformingReturnTypes3() {
        runNegativeTestMatching(
            new String[] {
		"Team4116ncrt3.java",
			    "\n" +
			    "public team class Team4116ncrt3 {\n" +
			    "\n" +
			    "    public class Role4116ncrt3 playedBy T4116ncrt3 {\n" +
			    "        // warning because we do not call the base method\n" +
			    "        callin String test(String arg) {\n" +
			    "            return arg;\n" +
			    "        }\n" +
			    "        test <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4116ncrt3.java",
			    "\n" +
			    "public class T4116ncrt3 {\n" +
			    "    public Object[] test(String arg) {\n" +
			    "        return null;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.4(b)");
    }

    // a role method with a callin modifier is callin-bound as 'before'
    // 4.1.17-otjld-illegal-use-of-callin-modifier-1
    public void test4117_illegalUseOfCallinModifier1() {
        runNegativeTest(
            new String[] {
		"T4117iuocm1.java",
			    "\n" +
			    "public class T4117iuocm1 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team4117iuocm1.java",
			    "\n" +
			    "public team class Team4117iuocm1 {\n" +
			    "\n" +
			    "    public class Role4117iuocm1 playedBy T4117iuocm1 {\n" +
			    "        callin void test() {\n" +
			    "            base.test();\n" +
			    "        }\n" +
			    "        test <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role method with a callin modifier is callin-bound as 'after'
    // 4.1.17-otjld-illegal-use-of-callin-modifier-2
    public void test4117_illegalUseOfCallinModifier2() {
        runNegativeTest(
            new String[] {
		"Team4117iuocm2.java",
			    "\n" +
			    "public team class Team4117iuocm2 {\n" +
			    "\n" +
			    "    public class Role4117iuocm2 playedBy T4117iuocm2 {\n" +
			    "        callin void test1() {\n" +
			    "            base.test1();\n" +
			    "        }\n" +
			    "        void test1() <- after void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4117iuocm2.java",
			    "\n" +
			    "public class T4117iuocm2 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team4117iuocm2.java (at line 8)\n" + 
    		"	void test1() <- after void test();\n" + 
    		"	^^^^^^^^^^^^\n" + 
    		"Cannot bind callin method test1() using \"after\" (OTJLD 4.2(d)).\n" + 
    		"----------\n");
    }

    // a role method without a callin modifier is callin-bound as 'replace'
    // 4.1.18-otjld-missing-callin-modifier-1
    public void test4118_missingCallinModifier1() {
        runNegativeTest(
            new String[] {
		"T4118mcm1.java",
			    "\n" +
			    "public class T4118mcm1 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team4118mcm1.java",
			    "\n" +
			    "public team class Team4118mcm1 {\n" +
			    "\n" +
			    "    public class Role4118mcm1 playedBy T4118mcm1 {\n" +
			    "        public void test() {}\n" +
			    "        test <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role method without a callin modifier is callin-bound as 'replace'
    // 4.1.18-otjld-missing-callin-modifier-2
    public void test4118_missingCallinModifier2() {
        runNegativeTest(
            new String[] {
		"T4118mcm2.java",
			    "\n" +
			    "public class T4118mcm2 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team4118mcm2.java",
			    "\n" +
			    "public team class Team4118mcm2 {\n" +
			    "\n" +
			    "    public class Role4118mcm2 playedBy T4118mcm2 {\n" +
			    "        public void test() {}\n" +
			    "        void test() <- replace void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a replace-callin binding has no replace binding
    // 4.1.20-otjld-missing-replace-modifier
    public void test4120_missingReplaceModifier() {
        runNegativeTest(
            new String[] {
		"T4120mrm.java",
			    "\n" +
			    "public class T4120mrm {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team4120mrm.java",
			    "\n" +
			    "public team class Team4120mrm {\n" +
			    "\n" +
			    "    public class Role4120mrm playedBy T4120mrm {\n" +
			    "        callin void test() { base.test(); }\n" +
			    "        void test() <- void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a callin binding to multiple methods, each with a binding modifier
    // 4.1.21-otjld-multiple-bound-methods-with-modifiers-1
    public void test4121_multipleBoundMethodsWithModifiers1() {
        runNegativeTest(
            new String[] {
		"T4121mbmwm1.java",
			    "\n" +
			    "public class T4121mbmwm1 {\n" +
			    "    public void test1() {}\n" +
			    "    public void test2() {}\n" +
			    "}\n" +
			    "    \n",
		"Team4121mbmwm1.java",
			    "\n" +
			    "public team class Team4121mbmwm1 {\n" +
			    "\n" +
			    "    public class Role4121mbmwm1 playedBy T4121mbmwm1 {\n" +
			    "        public void test() {}\n" +
			    "        test <- before test1, after test2;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a callin binding to multiple methods, each with a binding modifier
    // 4.1.21-otjld-multiple-bound-methods-with-modifiers-2
    public void test4121_multipleBoundMethodsWithModifiers2() {
        runNegativeTest(
            new String[] {
		"T4121mbmwm2.java",
			    "\n" +
			    "public class T4121mbmwm2 {\n" +
			    "    public void test1() {}\n" +
			    "    public void test2() {}\n" +
			    "}\n" +
			    "    \n",
		"Team4121mbmwm2.java",
			    "\n" +
			    "public team class Team4121mbmwm2 {\n" +
			    "\n" +
			    "    public class Role4121mbmwm2 playedBy T4121mbmwm2 {\n" +
			    "        callin void test() {}\n" +
			    "        test <- before test1, replace test2;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a callin binding binds an implicitly inherted method which has a role parameter
    // 4.1.22-otjld-binding-tsuper-method-with-signature
    public void test4122_bindingTsuperMethodWithSignature() {
       
       runConformTest(
            new String[] {
		"Team4122btmws_2.java",
			    "\n" +
			    "public team class Team4122btmws_2 extends Team4122btmws_1 {\n" +
			    "	protected class R1 {\n" +
			    "		void bar(R2 r) <- after void foo(T4122btmws b);\n" +
			    "	}\n" +
			    "	protected class R2 {\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(\"K\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	Team4122btmws_2 () {\n" +
			    "		activate();\n" +
			    "		T4122btmws b1 = new T4122btmws(\"foo\");\n" +
			    "		b1.foo(new T4122btmws(\"O\"));\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team4122btmws_2();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T4122btmws.java",
			    "\n" +
			    "public class T4122btmws {\n" +
			    "	private String val;\n" +
			    "	T4122btmws(String v) {\n" +
			    "		val = v;\n" +
			    "	}\n" +
			    "	void foo(T4122btmws other) {\n" +
			    "	   other.test();\n" +
			    "	}\n" +
			    "	private void test() { \n" +
			    "		System.out.print(val); \n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team4122btmws_1.java",
			    "\n" +
			    "public team class Team4122btmws_1 {\n" +
			    "	protected class R1 playedBy T4122btmws {\n" +
			    "		void bar(R2 r) {\n" +
			    "			r.test();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	protected class R2 playedBy T4122btmws {\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(\"NOK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a callin method is bound to an inaccessible base method and has a base call
    // 4.1.23-otjld-callin-with-basecall-to-inaccessible-1
    public void test4123_callinWithBasecallToInaccessible1() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_Decapsulation, CompilerOptions.REPORT_NONE);
       runConformTest(
            new String[] {
		"Team4123cwbti1.java",
			    "\n" +
			    "public team class Team4123cwbti1 {\n" +
			    "    protected class R playedBy T4123cwbti1 {\n" +
			    "        callin void test() {\n" +
			    "            base.test();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void test() <- replace void test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4123cwbti1 t = new Team4123cwbti1();\n" +
			    "        t.activate();\n" +
			    "        T4123cwbti1 b = new T4123cwbti1();\n" +
			    "        b.go();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4123cwbti1.java",
			    "\n" +
			    "public class T4123cwbti1 {\n" +
			    "    private void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "    public void go() {\n" +
			    "        test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a callin method is directly called
    // 4.1.24-otjld-directly-call-callin-method-1
    public void test4124_directlyCallCallinMethod1() {
        runNegativeTestMatching(
            new String[] {
		"Team4124dccm1.java",
			    " \n" +
			    "public team class Team4124dccm1 {\n" +
			    "    public class RoleA { \n" +
			    "        int x = someMethod(); \n" +
			    " \n" +
			    "        callin int someMethod() { \n" +
			    "            return 1979; \n" +
			    "        } \n" +
			    "    } \n" +
			    "}    \n" +
			    "    \n"
            },
            "4.2(d)");
    }

    // a after callin binding maps "result" to an argument
    // 4.1.25-otjld-result-in-after-binding-1
    public void test4125_resultInAfterBinding1() {
       
       runConformTest(
            new String[] {
		"Team4125riab1.java",
			    "\n" +
			    "public team class Team4125riab1 {\n" +
			    "    public class R playedBy T4125riab1 {\n" +
			    "        void log(String v) {\n" +
			    "            System.out.print(v);\n" +
			    "        }\n" +
			    "        void log(String v) <- after String getVal() with {\n" +
			    "            v <- result\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        (new Team4125riab1()).activate();\n" +
			    "        (new T4125riab1()).getVal();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4125riab1.java",
			    "\n" +
			    "public class T4125riab1 {    \n" +
			    "        String getVal() { return \"OK\"; }\n" +
			    "}        \n" +
			    "    \n"
            },
            "OK");
    }

    // a before callin binding maps "result" to an argument
    // 4.1.25-otjld-result-in-before-binding-2
    public void test4125_resultInBeforeBinding2() {
        runNegativeTest(
            new String[] {
		"T4125ribb2.java",
			    "\n" +
			    "public class T4125ribb2 {    \n" +
			    "        String getVal() { return \"OK\"; }\n" +
			    "}        \n" +
			    "    \n",
		"Team4125ribb2.java",
			    "\n" +
			    "public team class Team4125ribb2 {\n" +
			    "    public class R playedBy T4125ribb2 {\n" +
			    "        void log(String v) {\n" +
			    "            System.out.print(v);\n" +
			    "        }\n" +
			    "        void log(String v) <- before String getVal() with {\n" +
			    "            v <- result\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        (new Team4125ribb2()).activate();\n" +
			    "        (new T4125ribb2()).getVal();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // the name "result" is used in a method spec of a parameter-mapping-les callin (could work)
    public void test4125_resultInMethodSpec3() {
    	runConformTest(
    		new String[] {
    	"Team4125rims3.java",
		    	"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"\n" + 
				"public team class Team4125rims3 {\n" + 
				"\n" + 
				"	protected class R playedBy T4125rims3 {\n" + 
				"		<T> void safeAppend(T el, List<T> result) <- replace void append(T el, List<T> result);\n" + 
				"		callin <T> void safeAppend(T el, List<T> result) {\n" + 
				"			if (el != null)\n" + 
				"				base.safeAppend(el, result);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new Team4125rims3().activate();\n" + 
				"		List<String> strings = new ArrayList<String>();\n" + 
				"		strings.add(\"O\");\n" + 
				"		T4125rims3 appender = new T4125rims3();\n" + 
				"		appender.append(null, strings);\n" + 
				"		appender.append(\"K\", strings);\n" + 
				"		for (String string : strings) {\n" + 
				"			System.out.print(string);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
   		"T4125rims3.java",
    			"import java.util.List;\n" + 
    			"\n" + 
    			"\n" + 
    			"public class T4125rims3 {\n" + 
    			"	<T> void append(T el, List<T> result) {\n" + 
    			"		result.add(el);\n" + 
    			"	}\n" + 
    			"}\n",
    		},
    		"OK");
    }
    
    // a callin binding has a label
    // 4.1.26-otjld-name-callin-binding-1
    public void test4126_nameCallinBinding1() {
        runConformTest(
            new String[] {
		"Team4126ncb1.java",
			    "\n" +
			    "import base p.Team4126ncb1_0;\n" +
			    "public team class Team4126ncb1 {\n" +
			    "    public class R playedBy Team4126ncb1_0 {\n" +
			    "        void rm() {};\n" +
			    "        \n" +
			    "        logActivate:\n" +
			    "        void rm() <- after void activate();\n" +
			    "    }\n" +
			    "}\n",
		"p/Team4126ncb1_0.java",
				"package p;\n" +
				"public team class Team4126ncb1_0 { }\n"
            });
    }

    // a callin binding has a label with syntax error
    // 4.1.26-otjld-name-callin-binding-2
    public void test4126_nameCallinBinding2() {
        runNegativeTestMatching(
            new String[] {
		"Team4126ncb2.java",
			    "\n" +
			    "import base p1.Team4126ncb2_0;\n" +
			    "public team class Team4126ncb2 {\n" +
			    "    public class R playedBy Team4126ncb2_0 {\n" +
			    "        void rm() {};\n" +
			    "        \n" +
			    "        public logActivate:\n" +
			    "        void rm() <- after void activate();\n" +
			    "    }\n" +
			    "}\n",
		"p/Team4126ncb2_0.java",
				"package p;\n" +
				"public team class Team4126ncb2_0 { }\n"
            },
            "Syntax error");
    }

    // a callin binding has a label with syntax error
    // 4.1.26-otjld-name-callin-binding-3
    public void test4126_nameCallinBinding3() {
        runNegativeTestMatching(
            new String[] {
		"Team4126ncb3.java",
			    "\n" +
			    "import base p1.Team4126ncb3_0;\n" +
			    "public team class Team4126ncb3 {\n" +
			    "    public class R playedBy Team4126ncb3_0 {\n" +
			    "        void rm() {};\n" +
			    "        \n" +
			    "        logActivate[]:\n" +
			    "        void rm() <- after void activate();\n" +
			    "    }\n" +
			    "}\n",
		"p/Team4126ncb3_0.java",
				"package p;\n" +
				"public team class Team4126ncb3_0 { }\n"
            },
            "Syntax error");
    }

    // a callin binding has a label with syntax error
    // 4.1.26-otjld-name-callin-binding-4
    public void test4126_nameCallinBinding4() {
        runNegativeTestMatching(
            new String[] {
		"Team4126ncb4.java",
			    "\n" +
			    "import base p.Team4126ncb4_0;\n" +
			    "public team class Team4126ncb4 {\n" +
			    "    public class R playedBy Team4126ncb4_0 {\n" +
			    "        void rm() {};\n" +
			    "        \n" +
			    "        private synchronized logActivate[]:\n" +
			    "        void rm() <- after void activate();\n" +
			    "    }\n" +
			    "}\n",
		"p/Team4126ncb4_0.java",
				"package p;\n" +
				"public team class Team4126ncb4_0 { }\n"
            },
            "Syntax error");
    }

    // a callin binding has a label with syntax error
    // 4.1.26-otjld-name-callin-binding-5
    public void test4126_nameCallinBinding5() {
        runNegativeTestMatching(
            new String[] {
		"Team4126ncb5.java",
			    "\n" +
			    "import base p.Team4126ncb5_0;\n" +
			    "public team class Team4126ncb5 {\n" +
			    "    public class R playedBy Team4126ncb5_0 {\n" +
			    "        void rm() {};\n" +
			    "        \n" +
			    "        logActivate as bar:\n" +
			    "        void rm() <- after void activate();\n" +
			    "    }\n" +
			    "}\n",
		"p/Team4126ncb5_0.java",
				"package p;\n" +
				"public team class Team4126ncb5_0 { }\n"
            },
            "Syntax error");
    }

    // a long callin binding has a label
    // 4.1.26-otjld-name-callin-binding-6
    public void test4126_nameCallinBinding6() {
        runConformTest(
            new String[] {
		"Team4126ncb6.java",
			    "\n" +
			    "public team class Team4126ncb6 {\n" +
			    "    @SuppressWarnings(\"bindingconventions\") // fqn base class\n" +
			    "    public class R playedBy p.Team4126ncb6_0 {\n" +
			    "        void rm() {};\n" +
			    "        \n" +
			    "        logActivate:\n" +
			    "        void rm() <- after void activate();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"p/Team4126ncb6_0.java",
				"package p;\n" +
				"public team class Team4126ncb6_0 { }\n"
            });
    }

    // a team has a precedence declaration
    // 4.1.27-otjld-precedence-declaration-1
    public void test4127_precedenceDeclaration1() {
       
       runConformTest(
            new String[] {
		"Team4127pd1.java",
			    "\n" +
			    "public team class Team4127pd1 {\n" +
			    "    public class R playedBy T4127pd1 {\n" +
			    "        void rm1() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void rm2() { \n" +
			    "            System.out.print(\"O\"); \n" +
			    "        }\n" +
			    "        b1: rm1 <- after bm;\n" +
			    "        b2: rm2 <- after bm;\n" +
			    "    }\n" +
			    "    precedence after R.b1, R.b2;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd1 t = new Team4127pd1();\n" +
			    "        t.activate();\n" +
			    "        T4127pd1 b = new T4127pd1();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4127pd1.java",
			    "\n" +
			    "public class T4127pd1 {\n" +
			    "    void bm() { }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }
    // a team has a class-level precedence declaration
    public void test4127_precedenceDeclaration1c() {
       
       runConformTest(
            new String[] {
		"Team4127pd1c.java",
			    "\n" +
			    "public team class Team4127pd1c {\n" +
			    "    public class R1 playedBy T4127pd1c {\n" +
			    "        void rm1() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm1 <- after bm;\n" +
			    "    }\n" +
			    "    public class R2 playedBy T4127pd1c {\n" +
			    "        void rm2() { \n" +
			    "            System.out.print(\"O\"); \n" +
			    "        }\n" +
			    "        rm2 <- after bm;\n" +
			    "    }\n" +
			    "    precedence after R1, R2;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd1c t = new Team4127pd1c();\n" +
			    "        t.activate();\n" +
			    "        T4127pd1c b = new T4127pd1c();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4127pd1c.java",
			    "\n" +
			    "public class T4127pd1c {\n" +
			    "    void bm() { }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a team has two precedence declarations
    // 4.1.27-otjld-precedence-declaration-2
    public void test4127_precedenceDeclaration2() {
       
       runConformTest(
            new String[] {
		"Team4127pd2.java",
			    "\n" +
			    "public team class Team4127pd2 {\n" +
			    "    public class R playedBy T4127pd2 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"O\"); \n" +
			    "        }\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void rm3() {\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- after bm;\n" +
			    "        b2: rm2 <- after bm;\n" +
			    "        b3: rm3 <- after bm;\n" +
			    "    }\n" +
			    "    precedence after R.b2, R.b1;\n" +
			    "    precedence after R.b3, R.b2;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd2 t = new Team4127pd2();\n" +
			    "        t.activate();\n" +
			    "        T4127pd2 b = new T4127pd2();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4127pd2.java",
			    "\n" +
			    "public class T4127pd2 {\n" +
			    "    void bm() { }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a role has a precedence declaration
    // 4.1.27-otjld-precedence-declaration-3
    public void test4127_precedenceDeclaration3() {
       
       runConformTest(
            new String[] {
		"Team4127pd3.java",
			    "\n" +
			    "public team class Team4127pd3 {\n" +
			    "    public class R playedBy T4127pd3 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"O\"); \n" +
			    "        }\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- after bm;\n" +
			    "        b2: rm2 <- after bm;\n" +
			    "        precedence after b2, b1;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd3 t = new Team4127pd3();\n" +
			    "        t.activate();\n" +
			    "        T4127pd3 b = new T4127pd3();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4127pd3.java",
			    "\n" +
			    "public class T4127pd3 {\n" +
			    "    void bm() { }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a role has two precedence declarations
    // 4.1.27-otjld-precedence-declaration-4
    public void test4127_precedenceDeclaration4() {
       
       runConformTest(
            new String[] {
		"Team4127pd4.java",
			    "\n" +
			    "public team class Team4127pd4 {\n" +
			    "    public class R playedBy T4127pd4 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"O\"); \n" +
			    "        }\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void rm3() {\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- after bm;\n" +
			    "        b2: rm2 <- after bm;\n" +
			    "        b3: rm3 <- after bm;\n" +
			    "        precedence after b2, b1;\n" +
			    "        precedence after b3, b2;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd4 t = new Team4127pd4();\n" +
			    "        t.activate();\n" +
			    "        T4127pd4 b = new T4127pd4();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4127pd4.java",
			    "\n" +
			    "public class T4127pd4 {\n" +
			    "    void bm() { }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a team has a precedence declaration for replace bindings
    // 4.1.27-otjld-precedence-declaration-5
    public void test4127_precedenceDeclaration5() {
       
       runConformTest(
            new String[] {
		"Team4127pd5.java",
			    "\n" +
			    "public team class Team4127pd5 {\n" +
			    "    public class R playedBy T4127pd5 {\n" +
			    "        callin void rm1() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "            base.rm1();\n" +
			    "            System.out.print(\"Y\");\n" +
			    "        }\n" +
			    "        callin void rm2() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.rm2();\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- replace bm;\n" +
			    "        b2: rm2 <- replace bm;\n" +
			    "    }\n" +
			    "    precedence R.b2, R.b1;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd5 t = new Team4127pd5();\n" +
			    "        t.activate();\n" +
			    "        T4127pd5 b = new T4127pd5();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4127pd5.java",
			    "\n" +
			    "public class T4127pd5 {\n" +
			    "    void bm() { System.out.print(\"A\");}\n" +
			    "}    \n" +
			    "    \n"
            },
            "OKAY!");
    }

    // a team has a precedence declaration for replace bindings - base call omitted
    // 4.1.27-otjld-precedence-declaration-6
    public void test4127_precedenceDeclaration6() {
       
       runConformTest(
            new String[] {
		"T4127pd6Main.java",
			    "\n" +
			    "public class T4127pd6Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd6 t = new Team4127pd6();\n" +
			    "        t.activate();\n" +
			    "        T4127pd6 b = new T4127pd6();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4127pd6.java",
			    "\n" +
			    "public class T4127pd6 {\n" +
			    "    void bm() { System.out.print(\"A\");}\n" +
			    "}    \n" +
			    "    \n",
		"Team4127pd6.java",
			    "\n" +
			    "public team class Team4127pd6 {\n" +
			    "    public class R playedBy T4127pd6 {\n" +
			    "        callin void rm1() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        callin void rm2() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.rm2();\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- replace bm;\n" +
			    "        b2: rm2 <- replace bm;\n" +
			    "    }\n" +
			    "    precedence R.b2, R.b1;\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a team has a precedence declaration for replace bindings - explicitly thrown LiftingVetoException
    // 4.1.27-otjld-precedence-declaration-7
    public void test4127_precedenceDeclaration7() {
       
       runConformTest(
            new String[] {
		"Team4127pd7.java",
			    "\n" +
			    "public team class Team4127pd7 {\n" +
			    "    public class R playedBy T4127pd7 {\n" +
			    "        int count = 0;\n" +
			    "        R(T4127pd7 b) {\n" +
			    "            if (count%2==0) {\n" +
			    "                count++;\n" +
			    "                throw new org.objectteams.LiftingVetoException();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        callin void rm1() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "            base.rm1();\n" +
			    "            System.out.print(\"Y\");\n" +
			    "        }\n" +
			    "        callin void rm2() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.rm2();\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- replace bm;\n" +
			    "        b2: rm2 <- replace bm;\n" +
			    "    }\n" +
			    "    precedence R.b2, R.b1;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd7 t = new Team4127pd7();\n" +
			    "        t.activate();\n" +
			    "        T4127pd7 b = new T4127pd7();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4127pd7.java",
			    "\n" +
			    "public class T4127pd7 {\n" +
			    "    void bm() { System.out.print(\"A\");}\n" +
			    "}    \n" +
			    "    \n"
            },
            "KAY");
    }

    // a team has a precedence declaration for replace bindings - guard forbids lifting
    // 4.1.27-otjld-precedence-declaration-8
    public void test4127_precedenceDeclaration8() {
       
       runConformTest(
            new String[] {
		"Team4127pd8.java",
			    "\n" +
			    "public team class Team4127pd8 {\n" +
			    "    public class R playedBy T4127pd8 {\n" +
			    "        callin void rm1() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "            base.rm1();\n" +
			    "            System.out.print(\"Y\");\n" +
			    "        }\n" +
			    "        callin void rm2() base when (false) { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.rm2();\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- replace bm;\n" +
			    "        b2: rm2 <- replace bm;\n" +
			    "    }\n" +
			    "    precedence R.b2, R.b1;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd8 t = new Team4127pd8();\n" +
			    "        t.activate();\n" +
			    "        T4127pd8 b = new T4127pd8();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4127pd8.java",
			    "\n" +
			    "public class T4127pd8 {\n" +
			    "    void bm() { System.out.print(\"A\");}\n" +
			    "}    \n" +
			    "    \n"
            },
            "KAY");
    }

    // two teams have precedence declarations for replace bindings - guard forbids lifting
    // 4.1.27-otjld-precedence-declaration-9
    public void test4127_precedenceDeclaration9() {
       
       runConformTest(
            new String[] {
		"Team4127pd9.java",
			    "\n" +
			    "public team class Team4127pd9 {\n" +
			    "    public class R playedBy T4127pd9 {\n" +
			    "        callin void rm1() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "            base.rm1();\n" +
			    "            System.out.print(\"Y\");\n" +
			    "        }\n" +
			    "        callin void rm2() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.rm2();\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- replace bm;\n" +
			    "        b2: rm2 <- replace bm;\n" +
			    "    }\n" +
			    "    precedence R.b2, R.b1;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd9 t = new Team4127pd9();\n" +
			    "        t.activate();\n" +
			    "        Team4127pd9_2 t2 = new Team4127pd9_2();\n" +
			    "        t2.activate();\n" +
			    "        T4127pd9 b = new T4127pd9();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4127pd9.java",
			    "\n" +
			    "public class T4127pd9 {\n" +
			    "    void bm() { System.out.print(\"A\");}\n" +
			    "}    \n" +
			    "    \n",
		"Team4127pd9_2.java",
			    "\n" +
			    "public team class Team4127pd9_2 {\n" +
			    "    public class R playedBy T4127pd9 base when (false) {\n" +
			    "        callin void rm1() {\n" +
			    "            System.out.print(\"NOT\");\n" +
			    "            base.rm1();\n" +
			    "        }\n" +
			    "        callin void rm2() { \n" +
			    "            base.rm2();\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- replace bm;\n" +
			    "        b2: rm2 <- replace bm;\n" +
			    "    }\n" +
			    "    precedence R.b2, R.b1;\n" +
			    "}    \n" +
			    "    \n"
            },
            "OKAY!");
    }

    // a team has a precedence declaration - subteam adds another one
    // 4.1.27-otjld-precedence-declaration-10
    public void test4127_precedenceDeclaration10() {
       
       runConformTest(
            new String[] {
		"Team4127pd10_2.java",
			    "\n" +
			    "public team class Team4127pd10_2 extends Team4127pd10_1 {\n" +
			    "    public class R {\n" +
			    "    	void rm3() {\n" +
			    "	   System.out.print(\"!\");\n" +
			    "	}\n" +
			    "	b3: rm3 <- after bm;\n" +
			    "    }\n" +
			    "    precedence after R.b3, R.b1;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd10_2 t = new Team4127pd10_2();\n" +
			    "        t.activate();\n" +
			    "        T4127pd10 b = new T4127pd10();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4127pd10.java",
			    "\n" +
			    "public class T4127pd10 {\n" +
			    "    void bm() { }\n" +
			    "}    \n" +
			    "    \n",
		"Team4127pd10_1.java",
			    "\n" +
			    "public team class Team4127pd10_1 {\n" +
			    "    public class R playedBy T4127pd10 {\n" +
			    "        void rm1() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void rm2() { \n" +
			    "            System.out.print(\"O\"); \n" +
			    "        }\n" +
			    "        b1: rm1 <- after bm;\n" +
			    "        b2: rm2 <- after bm;\n" +
			    "    }\n" +
			    "    precedence after R.b1, R.b2;\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // a team has a precedence declaration - subteam adds another one - within packages
    // 4.1.27-otjld-precedence-declaration-10p
    public void test4127_precedenceDeclaration10p() {
       
       runConformTest(
            new String[] {
		"p2/Team4127pd10p_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team4127pd10p_2 extends p1.Team4127pd10p_1 {\n" +
			    "    public class R {\n" +
			    "        void rm3() {\n" +
			    "       System.out.print(\"!\");\n" +
			    "    }\n" +
			    "    b3: rm3 <- after bm;\n" +
			    "    }\n" +
			    "    precedence after R.b3, R.b1;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4127pd10p_2 t = new Team4127pd10p_2();\n" +
			    "        t.activate();\n" +
			    "        p1.T4127pd10p b = new p1.T4127pd10p();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"p1/T4127pd10p.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T4127pd10p {\n" +
			    "    public void bm() { }\n" +
			    "}    \n" +
			    "    \n",
		"p1/Team4127pd10p_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team4127pd10p_1 {\n" +
			    "    public class R playedBy T4127pd10p {\n" +
			    "        void rm1() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void rm2() { \n" +
			    "            System.out.print(\"O\"); \n" +
			    "        }\n" +
			    "        b1: rm1 <- after bm;\n" +
			    "        b2: rm2 <- after bm;\n" +
			    "    }\n" +
			    "    precedence after R.b1, R.b2;\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // a nested team has a precedence declaration
    // 4.1.27-otjld-precedence-declaration-11
    public void test4127_precedenceDeclaration11() {
       
       runConformTest(
            new String[] {
		"Team4127pd11_2.java",
			    "\n" +
			    "public team class Team4127pd11_2 extends Team4127pd11_1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4127pd11_1();\n" +
			    "        T4127pd11 b = new T4127pd11();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4127pd11.java",
			    "\n" +
			    "public class T4127pd11 {\n" +
			    "    void bm() { }\n" +
			    "}    \n" +
			    "    \n",
		"Team4127pd11_1.java",
			    "\n" +
			    "public team class Team4127pd11_1 {\n" +
			    "    public team class InnerT {\n" +
			    "        public class R playedBy T4127pd11 {\n" +
			    "            void rm1() {\n" +
			    "                System.out.print(\"K\");\n" +
			    "            }\n" +
			    "            void rm2() { \n" +
			    "                System.out.print(\"O\"); \n" +
			    "            }\n" +
			    "            b1: rm1 <- after bm;\n" +
			    "            b2: rm2 <- after bm;\n" +
			    "        }\n" +
			    "        precedence after R.b1, R.b2;\n" +
			    "        \n" +
			    "    }\n" +
			    "    public Team4127pd11_1 () {\n" +
			    "        InnerT it = new InnerT();\n" +
			    "        it.activate();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a nested team has a precedence declaration - in a package
    // 4.1.27-otjld-precedence-declaration-11p
    public void test4127_precedenceDeclaration11p() {
       
       runConformTest(
            new String[] {
		"p2/Team4127pd11p_2.java",
			    "\n" +
			    "package p2;\n" +
			    "import p1.Team4127pd11p_1;\n" +
			    "public team class Team4127pd11p_2 extends Team4127pd11p_1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new p1.Team4127pd11p_1();\n" +
			    "        p1.T4127pd11p b = new p1.T4127pd11p();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"p1/T4127pd11p.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T4127pd11p {\n" +
			    "    public void bm() { }\n" +
			    "}    \n" +
			    "    \n",
		"p1/Team4127pd11p_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team4127pd11p_1 {\n" +
			    "    protected team class InnerT {\n" +
			    "        public class R playedBy T4127pd11p {\n" +
			    "            void rm1() {\n" +
			    "                System.out.print(\"K\");\n" +
			    "            }\n" +
			    "            void rm2() { \n" +
			    "                System.out.print(\"O\"); \n" +
			    "            }\n" +
			    "            b1: rm1 <- after bm;\n" +
			    "            b2: rm2 <- after bm;\n" +
			    "        }\n" +
			    "        precedence after R.b1, R.b2;\n" +
			    "        \n" +
			    "    }\n" +
			    "    public Team4127pd11p_1 () {\n" +
			    "        InnerT it = new InnerT();\n" +
			    "        it.activate();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a nested team has a precedence declaration - in a package - compile in one go
    // 4.1.27-otjld-precedence-declaration-11P
    public void test4127_precedenceDeclaration11P() {
       
       runConformTest(
            new String[] {
		"p2/Team4127pd11P_2.java",
			    "\n" +
			    "package p2;\n" +
			    "import p1.Team4127pd11P_1;\n" +
			    "public team class Team4127pd11P_2 extends Team4127pd11P_1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new p1.Team4127pd11P_1();\n" +
			    "        p1.T4127pd11P b = new p1.T4127pd11P();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T4127pd11P.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T4127pd11P {\n" +
			    "    public void bm() { }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team4127pd11P_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team4127pd11P_1 {\n" +
			    "    protected team class InnerT {\n" +
			    "        public class R playedBy T4127pd11P {\n" +
			    "            void rm1() {\n" +
			    "                System.out.print(\"K\");\n" +
			    "            }\n" +
			    "            void rm2() {\n" +
			    "                System.out.print(\"O\");\n" +
			    "            }\n" +
			    "            b1: rm1 <- after bm;\n" +
			    "            b2: rm2 <- after bm;\n" +
			    "        }\n" +
			    "        precedence after R.b1, R.b2;\n" +
			    "\n" +
			    "    }\n" +
			    "    public Team4127pd11P_1 () {\n" +
			    "        InnerT it = new InnerT();\n" +
			    "        it.activate();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a precedence is not needed because lifting has no overlap (witness for TPX-418 comment 2)
    // 4.1.27-otjld-precedence-declaration-12
    public void test4127_precedenceDeclaration12() {
       
       runConformTest(
            new String[] {
		"Team4127pd12.java",
			    "\n" +
			    "public team class Team4127pd12 {\n" +
			    "    protected class R1 playedBy T4127pd12_1 {\n" +
			    "        void rm() { System.out.print(\"?\"); }\n" +
			    "        rm <- after bm;\n" +
			    "    }\n" +
			    "    protected class R2 playedBy T4127pd12_2 {\n" +
			    "        void rm() { System.out.print(\"!\"); }\n" +
			    "        rm <- after bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4127pd12().activate();\n" +
			    "        new T4127pd12_1().bm();\n" +
			    "        new T4127pd12_2().bm();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4127pd12_0.java",
			    "\n" +
			    "public class T4127pd12_0 {\n" +
			    "    void bm() { System.out.print(\"O\"); }\n" +
			    "}\n" +
			    "    \n",
		"T4127pd12_1.java",
			    "\n" +
			    "public class T4127pd12_1 extends T4127pd12_0 {\n" +
			    "}\n" +
			    "    \n",
		"T4127pd12_2.java",
			    "\n" +
			    "public class T4127pd12_2 extends T4127pd12_0 {\n" +
			    "    void bm() { System.out.print(\"K\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "O?K!");
    }
        
    // precedence for two after callin bindings, missing "after" modifier
    public void test4127_precedenceDeclaration13() {
    	runNegativeTest(
    		new String[] {
    	"Team4127pd13.java",
    		"public team class Team4127pd13 {\n" +
    		"	protected class R playedBy T4127pd13 {\n" +
    		"		c1: k <- after test;\n" +
    		"		c2: bang <- after test;\n" +
    		"		void k() { System.out.print(\"K\"); }\n" +
    		"		void bang() { System.out.print(\"!\"); }\n" +
    		"		precedence c2, c1;\n" +
    		"	}\n" +
    		"}\n",
    	"T4127pd13.java",
    		"public class T4127pd13 {\n" +
    		"	void test() { System.out.print(\"O\"); }\n" +
    		"}\n"
    		}, 
    		"----------\n" + 
    		"1. ERROR in Team4127pd13.java (at line 7)\n" + 
    		"	precedence c2, c1;\n" + 
    		"	           ^^\n" + 
    		"\'precedence\' declaration for \'after\' binding must be specified as \'precedence after\' (OTJLD 4.8(a)).\n" + 
    		"----------\n" + 
    		"2. ERROR in Team4127pd13.java (at line 7)\n" + 
    		"	precedence c2, c1;\n" + 
    		"	               ^^\n" + 
    		"\'precedence\' declaration for \'after\' binding must be specified as \'precedence after\' (OTJLD 4.8(a)).\n" + 
    		"----------\n");
    }
    
    // precedence after for two callin bindings, one without the "after" modifier
    public void test4127_precedenceDeclaration14() {
    	runNegativeTest(
    		new String[] {
    	"Team4127pd14.java",
    		"public team class Team4127pd14 {\n" +
    		"	protected class R playedBy T4127pd14 {\n" +
    		"		c1: k <- after test;\n" +
    		"		c2: bang <- before test;\n" +
    		"		void k() { System.out.print(\"K\"); }\n" +
    		"		void bang() { System.out.print(\"!\"); }\n" +
    		"		precedence after c2, c1;\n" +
    		"	}\n" +
    		"}\n",
    	"T4127pd14.java",
    		"public class T4127pd14 {\n" +
    		"	void test() { System.out.print(\"O\"); }\n" +
    		"}\n"
    		}, 
    		"----------\n" + 
    		"1. ERROR in Team4127pd14.java (at line 7)\n" + 
    		"	precedence after c2, c1;\n" + 
    		"	                 ^^\n" + 
    		"\'precedence after\' declaration cannot refer to \'before\' bindings (OTJLD 4.8(a)).\n" + 
    		"----------\n");
    }
    
    // merging precedence declarations from two nesting levels:
    public void test4127_precedenceDeclaration15() {
    	runConformTest(
			new String[] {
		"Team4127pd15.java",
				"public team class Team4127pd15 {\n" +
				"	precedence MyRoleB.bl1, MyRoleA;\n" +
				" 	protected class MyRoleB playedBy T4127pd15 {\n" +
				"		precedence bl1, bl2;\n" +
				"		void rm() {\n" +
				"			System.out.print(\"1\");\n" +
				"		}\n" +
				"		bl1: rm <- before bm;\n" +
				"		void rm2() {\n" +
				"			System.out.print(\"2\");\n" +
				"		}\n" +
				"		bl2: rm2 <- before bm;\n" +
				"	}\n" +
				"	protected class MyRoleA playedBy T4127pd15 {\n" +
				"		void rm() {\n" +
				"			System.out.print(\"3\");\n" +
				"		}\n" +
				"		rm <- before bm;\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		Team4127pd15 t = new Team4127pd15();\n" +
                "		t.activate();\n" +
                "		T4127pd15 b = new T4127pd15();\n" +
                "		b.bm();\n" +
				"	}\n" +
				"}\n",
		"T4127pd15.java",
				"public class T4127pd15 {\n" +
				"	void bm() {\n" +
				"		System.out.print(\"!\");\n" +
				"	}\n" +
				"}\n"
			},
			"123!");
    }

    // merging precedence declarations from the same nesting levels:
    public void test4127_precedenceDeclaration16() {
    	runConformTest(
			new String[] {
		"Team4127pd16.java",
				"public team class Team4127pd16 {\n" +
				" 	protected class MyRoleB playedBy T4127pd16 {\n" +
				"		precedence bl1, bl2;\n" +
				"		precedence bl1, bl3;\n" +
				"		void rm() {\n" +
				"			System.out.print(\"1\");\n" +
				"		}\n" +
				"		bl1: rm <- before bm;\n" +
				"		void rm3() {\n" +
				"			System.out.print(\"3\");\n" +
				"		}\n" +
				"		bl3: rm3 <- before bm;\n" +
				"		void rm2() {\n" +
				"			System.out.print(\"2\");\n" +
				"		}\n" +
				"		bl2: rm2 <- before bm;\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		Team4127pd16 t = new Team4127pd16();\n" +
                "		t.activate();\n" +
                "		T4127pd16 b = new T4127pd16();\n" +
                "		b.bm();\n" +
				"	}\n" +
				"}\n",
		"T4127pd16.java",
				"public class T4127pd16 {\n" +
				"	void bm() {\n" +
				"		System.out.print(\"!\");\n" +
				"	}\n" +
				"}\n"
			},
			"123!");
    }

    // merging precedence declarations from two nesting levels
    // Bug 316659 -  [compiler] NPE in PrecedenceBinding.hasCommonBaseMethod
    public void test4127_precedenceDeclaration17() {
    	runNegativeTest(
			new String[] {
		"Team4127pd17.java",
				"public team class Team4127pd17 {\n" +
				"	precedence MyRoleB.bl1, MyRoleA;\n" +
				" 	protected class MyRoleB playedBy T4127pd17 {\n" +
				"       precedence MyRoleB.bl1, MyRoleB.bl2;\n" +
				"		void rm() {\n" +
				"			System.out.print(\"1\");\n" +
				"		}\n" +
				"		bl1: rm <- before bm;\n" +
				"		void rm2() {\n" +
				"			System.out.print(\"2\");\n" +
				"		}\n" +
				"		bl2: rm2 <- before bm;\n" +
				"	}\n" +
				"	protected class MyRoleA playedBy T4127pd17 {\n" +
				"		void rm() {\n" +
				"			System.out.print(\"3\");\n" +
				"		}\n" +
				"		rm <- before bm;\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		Team4127pd17 t = new Team4127pd17();\n" +
                "		t.activate();\n" +
                "		T4127pd17 b = new T4127pd17();\n" +
                "		b.bm();\n" +
				"	}\n" +
				"}\n",
		"T4127pd17.java",
				"public class T4127pd17 {\n" +
				"	void bm() {\n" +
				"		System.out.print(\"!\");\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in Team4127pd17.java (at line 4)\n" + 
			"	precedence MyRoleB.bl1, MyRoleB.bl2;\n" + 
			"	           ^^^^^^^^^^^\n" + 
			"MyRoleB cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in Team4127pd17.java (at line 4)\n" + 
			"	precedence MyRoleB.bl1, MyRoleB.bl2;\n" + 
			"	                        ^^^^^^^^^^^\n" + 
			"MyRoleB cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in Team4127pd17.java (at line 8)\n" + 
			"	bl1: rm <- before bm;\n" + 
			"	^^^\n" + 
			"\'before\' callin bindings Team4127pd17.MyRoleB.bl1 and Team4127pd17.MyRoleB.bl2 refer to the same base method; must declare precedence of these bindings (OTJLD 4.8).\n" + 
			"----------\n" + 
			"4. ERROR in Team4127pd17.java (at line 18)\n" + 
			"	rm <- before bm;\n" + 
			"	^^\n" + 
			"\'before\' callin bindings Team4127pd17.MyRoleA.<Team4127pd17:18,2> and Team4127pd17.MyRoleB.bl2 refer to the same base method; must declare precedence of these bindings (OTJLD 4.8).\n" + 
			"----------\n");
    }
    
    // Bug 332893 - Class Precedence not working between Role callin and SubRole callin
    public void test4127_precedenceDeclaration18 () {
    	runNegativeTest(
    		new String[] {
    			"PrecBug.java",
    			"public team class PrecBug {\n" + 
    			"    precedence after RA.RB, RA;\n" + 
    			"    protected team class RA playedBy A {\n" + 
    			"        void some(String v) <- after void myMethod2()\n" +
    			"            with { v <- \"RA\" }\n" + 
    			"        void some(String v) {\n" +
    			"            System.out.print(\"some\"+v);\n" + 
    			"        }\n" + 
    			"        protected class RB playedBy B {\n" + 
    			"            void some(String v) <- after void myMethod2()" +
    			"                with { v <- \"RB\" }\n" + 
    			"        }\n" + 
    			"    }\n" +
    			"    public PrecBug(A as RA a) {\n" +
    			"        a.activate();\n" +
    			"    }\n" +
    			"    public static void main(String... args) {\n" +
    			"        B b = new B();\n" +
    			"        new PrecBug(b).activate();\n" +
    			"        new B().myMethod2();\n" +
    			"    }\n" + 
    			"}\n",
    			"A.java",
    			"public class A {\n" +
    			"    void myMethod2() {}\n" +
    			"}\n",
    			"B.java",
    			"public class B extends A {\n" +
    			"}\n"
    		}, 
    		"----------\n" + 
    		"1. ERROR in PrecBug.java (at line 2)\n" + 
    		"	precedence after RA.RB, RA;\n" + 
    		"	                 ^^^^^^^^^^\n" + 
    		"\'precedence\' declaration can only refer to direct role classes, however PrecBug.RA.RB is a nested role of team PrecBug.RA (OTJLD 4.8).\n" + 
    		"----------\n");
    }
    
    // Bug 335777 - [compiler] don't flag missing precedence if different enclosing teams
    public void test4127_precedenceDeclaration19 () {
    	runConformTest(
    		new String[] {
    			"PrecBug19.java",
    			"public team class PrecBug19 {\n" + 
    			"    protected team class RA playedBy A {\n" + 
    			"        void some(String v) <- before void myMethod2()\n" +
    			"            with { v <- \"RA\" }\n" + 
    			"        void some(String v) <- after void myMethod2()\n" +
    			"            with { v <- \"RA\" }\n" + 
    			"        void some(String v) {\n" +
    			"            System.out.print(\"some\"+v);\n" + 
    			"        }\n" + 
    			"        protected class RB playedBy B {\n" + 
    			"            void some(String v) <- before void myMethod2()" +
    			"                with { v <- \"RB\" }\n" +
				"            void some(String v) <- after void myMethod2()" +
    			"                with { v <- \"RB\" }\n" +
    			"        }\n" + 
    			"    }\n" +
    			"    public PrecBug19(A as RA a) {\n" +
    			"        a.activate();\n" +
    			"    }\n" +
    			"    public static void main(String... args) {\n" +
    			"        B b = new B();\n" +
    			"        new PrecBug19(b).activate();\n" +
    			"        new B().myMethod2();\n" +
    			"    }\n" + 
    			"}\n",
    			"A.java",
    			"public class A {\n" +
    			"    void myMethod2() { System.out.print(\"-\"); }\n" +
    			"}\n",
    			"B.java",
    			"public class B extends A {\n" +
    			"}\n"
    		}, 
    		"someRAsomeRB-someRBsomeRA");
    }

    // a regular class has a precedence declaration
    // 4.1.28-otjld-invalid-precedence-declaration-1
    public void test4128_invalidPrecedenceDeclaration1() {
        runNegativeTestMatching(
            new String[] {
		"T4128ipd1.java",
			    "\n" +
			    "public class T4128ipd1 {\n" +
			    "    precedence R.b1, R.b2;\n" +
			    "}    \n" +
			    "    \n"
            },
            "A.0.1");
    }

    // a team has a precedence declaration - no such callin binding
    // 4.1.28-otjld-invalid-precedence-declaration-2
    public void test4128_invalidPrecedenceDeclaration2() {
        runNegativeTestMatching(
            new String[] {
		"Team4128ipd2.java",
			    "\n" +
			    "public team class Team4128ipd2 {\n" +
			    "    public class R {\n" +
			    "    }\n" +
			    "    precedence R.b1;\n" +
			    "}    \n" +
			    "    \n"
            },
            "4.8(b)");
    }

    // a team has a precedence declaration - no such role
    // 4.1.28-otjld-invalid-precedence-declaration-3
    public void test4128_invalidPrecedenceDeclaration3() {
        runNegativeTestMatching(
            new String[] {
		"Team4128ipd3.java",
			    "\n" +
			    "public team class Team4128ipd3 {\n" +
			    "    precedence R.b1;\n" +
			    "}    \n" +
			    "    \n"
            },
            "type");
    }

    // a team has two contradicting precedence declarations
    // 4.1.28-otjld-invalid-precedence-declaration-4
    public void test4128_invalidPrecedenceDeclaration4() {
        runNegativeTestMatching(
            new String[] {
		"Team4128ipd4.java",
			    "\n" +
			    "public team class Team4128ipd4 {\n" +
			    "    public class R playedBy T4128ipd4 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"O\"); \n" +
			    "        }\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void rm3() {\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- after bm;\n" +
			    "        b2: rm2 <- after bm;\n" +
			    "        b3: rm3 <- after bm;\n" +
			    "        precedence after b2, b1;\n" +
			    "    }\n" +
			    "    precedence after R.b1, R.b2, R.b3;\n" +
			    "}    \n" +
			    "    \n",
		"T4128ipd4.java",
			    "\n" +
			    "public class T4128ipd4 {\n" +
			    "    void bm() { }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OTJLD 4.8(d)");
    }

    // a role has a precedence declaration, so has its super role
    // 4.1.29-otjld-inheritance-of-precedence-declaration-1
    public void test4129_inheritanceOfPrecedenceDeclaration1() {
       
       runConformTest(
            new String[] {
		"Team4129iopd1.java",
			    "\n" +
			    "public team class Team4129iopd1 {\n" +
			    "    public class R1 playedBy T4129iopd1 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"O\"); \n" +
			    "        }\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- after bm;\n" +
			    "        b2: rm2 <- after bm;\n" +
			    "        precedence after b2, b1;\n" +
			    "    }\n" +
			    "    public class R2 extends R1 {\n" +
			    "        void rm3() {\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        b3: rm3 <- after bm;\n" +
			    "        precedence after b3, b2;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4129iopd1 t = new Team4129iopd1();\n" +
			    "        t.activate();\n" +
			    "        T4129iopd1 b = new T4129iopd1();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4129iopd1.java",
			    "\n" +
			    "public class T4129iopd1 {\n" +
			    "    void bm() { }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a team has a precedence declaration by role class
    // 4.1.30-otjld-class-based-precedence-declaration-1
    public void test4130_classBasedPrecedenceDeclaration1() {
       
       runConformTest(
            new String[] {
		"Team4130cbpd1.java",
			    "\n" +
			    "public team class Team4130cbpd1 {\n" +
			    "    public class R playedBy T4130cbpd1 {\n" +
			    "        callin void rm1() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "            base.rm1();\n" +
			    "            System.out.print(\"Y\");\n" +
			    "        }\n" +
			    "        rm1 <- replace bm;\n" +
			    "    }\n" +
			    "    public class R2 extends R {\n" +
			    "        callin void rm2() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            base.rm2();\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        rm2 <- replace bm;\n" +
			    "    }\n" +
			    "    precedence R2, R;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4130cbpd1 t = new Team4130cbpd1();\n" +
			    "        t.activate();\n" +
			    "        T4130cbpd1 b = new T4130cbpd1();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4130cbpd1.java",
			    "\n" +
			    "public class T4130cbpd1 {\n" +
			    "    void bm() { System.out.print(\"A\");}\n" +
			    "}    \n" +
			    "    \n"
            },
            "OKAY!");
    }

    // a role defines a callin binding despite a binding ambiguity
    // 4.1.31-otjld-illegal-lifting-binding-ambiguity-1
    public void test4131_illegalLiftingBindingAmbiguity1() {
        runNegativeTest(
            new String[] {
		"Team4131ilba1.java",
			    "\n" +
			    "public team class Team4131ilba1 {\n" +
			    "	protected class R0 playedBy T4131ilba1 {\n" +
			    "		void rm() {};\n" +
			    "		rm <- after test;\n" +
			    "	}\n" +
			    "	protected class R1 extends R0 {}\n" +
			    "	protected class R2 extends R0 {}\n" +
			    "}	\n" +
			    "	\n",
		"T4131ilba1.java",
			    "\n" +
			    "public class T4131ilba1 {\n" +
			    "	public void test() {};\n" +
			    "}	\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. WARNING in Team4131ilba1.java (at line 2)\n" + 
    		"	public team class Team4131ilba1 {\n" + 
    		"	                  ^^^^^^^^^^^^^\n" + 
    		"Potential ambiguity in role binding. The base \'T4131ilba1\' is bound to the following roles: Team4131ilba1.R1,Team4131ilba1.R2 (OTJLD 2.3.4(a)).\n" + 
    		"----------\n" + 
    		"2. ERROR in Team4131ilba1.java (at line 5)\n" + 
    		"	rm <- after test;\n" + 
    		"	^^\n" + 
    		"Unsafe callin binding, because lifting to role Team4131ilba1.R0 may fail due to a reported binding ambiguity (OTJLD 2.3.5(c)).\n" + 
    		"----------\n" + 
    		"3. ERROR in Team4131ilba1.java (at line 5)\n" + 
    		"	rm <- after test;\n" + 
    		"	^^\n" + 
    		"Unsafe callin binding, because lifting to role Team4131ilba1.R0 may fail due to a reported binding ambiguity (OTJLD 2.3.5(c)).\n" + 
    		"----------\n");
    }

    // a definite binding ambiguity is reported (as the need to handle LiftingFailedException)
    // 4.1.31-otjld-illegal-lifting-binding-ambiguity-2d
    public void test4131_illegalLiftingBindingAmbiguity2d() {
        runNegativeTestMatching(
            new String[] {
		"Team4131ilba2d.java",
			    "\n" +
			    "public team class Team4131ilba2d {\n" +
			    "	protected class R0 playedBy T4131ilba2d {\n" +
			    "	}\n" +
			    "	protected class R1 extends R0 {}\n" +
			    "	protected class R2 extends R0 {}\n" +
			    "	Team4131ilba2d (T4131ilba2d as R0 o) {}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team4131ilba2d(new T4131ilba2d());\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T4131ilba2d.java",
			    "\n" +
			    "public class T4131ilba2d {\n" +
			    "	public void test() {};\n" +
			    "}	\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. WARNING in Team4131ilba2d.java (at line 2)\n" + 
    		"	public team class Team4131ilba2d {\n" + 
    		"	                  ^^^^^^^^^^^^^^\n" + 
    		"Potential ambiguity in role binding. The base \'T4131ilba2d\' is bound to the following roles: Team4131ilba2d.R1,Team4131ilba2d.R2 (OTJLD 2.3.4(a)).\n" + 
    		"----------\n" + 
    		"2. ERROR in Team4131ilba2d.java (at line 7)\n" + 
    		"	Team4131ilba2d (T4131ilba2d as R0 o) {}\n" + 
    		"	                ^^^^^^^^^^^^^^^^^\n" + 
    		"Unhandled exception type LiftingFailedException, caused by an unsafe lifting request (OTJLD 2.3.5).\n" + 
    		"----------\n");
    }

    // a team uses declared lifting despite a binding ambiguity -> runtime exception
    // 4.1.31-otjld-illegal-lifting-binding-ambiguity-2
    public void test4131_illegalLiftingBindingAmbiguity2() {

       runConformTest(
            new String[] {
		"Team4131ilba2.java",
			    "import org.objectteams.LiftingFailedException;\n" +
			    "public team class Team4131ilba2 {\n" +
			    "	protected class R0 playedBy T4131ilba2_1 {\n" +
			    "	}\n" +
			    "	protected class R1 extends R0 playedBy T4131ilba2_2 {}\n" +
			    "	protected class R2 extends R0 playedBy T4131ilba2_2 {}\n" +
			    "	Team4131ilba2 (T4131ilba2_1 as R0 o) throws LiftingFailedException {}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		try {\n" +
			    "			new Team4131ilba2(new T4131ilba2_2());\n" +
			    "		} catch (LiftingFailedException e) {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T4131ilba2_1.java",
			    "\n" +
			    "public class T4131ilba2_1 {\n" +
			    "	public void test() {};\n" +
			    "}	\n" +
			    "	\n",
		"T4131ilba2_2.java",
			    "\n" +
			    "public class T4131ilba2_2 extends T4131ilba2_1 {\n" +
			    "}	\n" +
			    "	\n"
            },
            "OK");
    }

    // a named callin binding is overridden by an implicit sub role
    // 4.1.32-otjld-overriding-of-callin-binding-1
    public void test4132_overridingOfCallinBinding1() {
       
       runConformTest(
            new String[] {
		"Team4132oocb1_2.java",
			    "\n" +
			    "public team class Team4132oocb1_2 extends Team4132oocb1 {\n" +
			    "    public class R1 {\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        b1: rm2 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb1_2 t = new Team4132oocb1_2();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb1 b = new T4132oocb1();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb1.java",
			    "\n" +
			    "public class T4132oocb1 {\n" +
			    "    void bm() {\n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team4132oocb1.java",
			    "\n" +
			    "public team class Team4132oocb1 {\n" +
			    "    public class R1 playedBy T4132oocb1 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOTOK\"); \n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a named callin binding is overridden by an implicit sub role  - inherited precedence declaration
    // 4.1.32-otjld-overriding-of-callin-binding-1a
    public void test4132_overridingOfCallinBinding1a() {
       
       runConformTest(
            new String[] {
		"Team4132oocb1a_2.java",
			    "\n" +
			    "public team class Team4132oocb1a_2 extends Team4132oocb1a {\n" +
			    "    public class R1 {\n" +
			    "        void rm3() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "        b1: rm3 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb1a_2 t = new Team4132oocb1a_2();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb1a b = new T4132oocb1a();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb1a.java",
			    "\n" +
			    "public class T4132oocb1a {\n" +
			    "    void bm() {\n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team4132oocb1a.java",
			    "\n" +
			    "public team class Team4132oocb1a {\n" +
			    "    public class R1 playedBy T4132oocb1a {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOTO\"); \n" +
			    "        }\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "        b2: rm2 <- before bm;\n" +
			    "        precedence b1, b2;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a named callin binding is overridden by an explicit sub role in the same team
    // 4.1.32-otjld-overriding-of-callin-binding-2
    public void test4132_overridingOfCallinBinding2() {
       
       runConformTest(
            new String[] {
		"Team4132oocb2.java",
			    "\n" +
			    "public team class Team4132oocb2 {\n" +
			    "    public class R1 playedBy T4132oocb2 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOTOK\"); \n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "    }\n" +
			    "    public class R2 extends R1 {\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        b1: rm2 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb2 t = new Team4132oocb2();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb2 b = new T4132oocb2();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb2.java",
			    "\n" +
			    "public class T4132oocb2 {\n" +
			    "    void bm() { \n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a named callin binding is overridden by an explicit sub role in the same team - inherited precedence declaration
    // 4.1.32-otjld-overriding-of-callin-binding-2a
    public void test4132_overridingOfCallinBinding2a() {
       
       runConformTest(
            new String[] {
		"Team4132oocb2a.java",
			    "\n" +
			    "public team class Team4132oocb2a {\n" +
			    "    public class R1 playedBy T4132oocb2a {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOTO\"); \n" +
			    "        }\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "        b2: rm2 <- before bm;\n" +
			    "        precedence b1, b2;\n" +
			    "    }\n" +
			    "    public class R2 extends R1 {\n" +
			    "        void rm3() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "        b1: rm3 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb2a t = new Team4132oocb2a();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb2a b = new T4132oocb2a();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb2a.java",
			    "\n" +
			    "public class T4132oocb2a {\n" +
			    "    void bm() {\n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a named callin binding is overridden by an explicit sub role in a sub team
    // 4.1.32-otjld-overriding-of-callin-binding-3
    public void test4132_overridingOfCallinBinding3() {
       
       runConformTest(
            new String[] {
		"Team4132oocb3_2.java",
			    "\n" +
			    "public team class Team4132oocb3_2 extends Team4132oocb3 {\n" +
			    "    public class R2 extends R1 {\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        b1: rm2 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb3_2 t = new Team4132oocb3_2();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb3 b = new T4132oocb3();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb3.java",
			    "\n" +
			    "public class T4132oocb3 {\n" +
			    "    void bm() { \n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team4132oocb3.java",
			    "\n" +
			    "public team class Team4132oocb3 {\n" +
			    "    public class R1 playedBy T4132oocb3 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOTOK\"); \n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a named callin binding is overridden by an explicit sub role in a sub team - inherited precedence declaration
    // 4.1.32-otjld-overriding-of-callin-binding-3a
    public void test4132_overridingOfCallinBinding3a() {
       
       runConformTest(
            new String[] {
		"Team4132oocb3a_2.java",
			    "\n" +
			    "public team class Team4132oocb3a_2 extends Team4132oocb3a {\n" +
			    "    public class R2 extends R1 {\n" +
			    "        void rm3() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "        b1: rm3 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb3a_2 t = new Team4132oocb3a_2();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb3a b = new T4132oocb3a();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb3a.java",
			    "\n" +
			    "public class T4132oocb3a {\n" +
			    "    void bm() { \n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team4132oocb3a.java",
			    "\n" +
			    "public team class Team4132oocb3a {\n" +
			    "    public class R1 playedBy T4132oocb3a {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOTO\"); \n" +
			    "        }\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "        b2: rm2 <- before bm;\n" +
			    "        precedence b1, b2;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a named callin binding is overridden by an explicit subsub role in a sub team
    // 4.1.32-otjld-overriding-of-callin-binding-4
    public void test4132_overridingOfCallinBinding4() {
       
       runConformTest(
            new String[] {
		"Team4132oocb4_2.java",
			    "\n" +
			    "public team class Team4132oocb4_2 extends Team4132oocb4 {\n" +
			    "    public class R2 extends R1 { }\n" +
			    "    public class R3 extends R2 {\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        b1: rm2 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb4_2 t = new Team4132oocb4_2();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb4 b = new T4132oocb4();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb4.java",
			    "\n" +
			    "public class T4132oocb4 {\n" +
			    "    void bm() { \n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team4132oocb4.java",
			    "\n" +
			    "public team class Team4132oocb4 {\n" +
			    "    public class R1 playedBy T4132oocb4 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOTOK\"); \n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a named callin binding is overridden by an explicit subsub role in a sub team - inherited precedence declaration
    // 4.1.32-otjld-overriding-of-callin-binding-4a
    public void test4132_overridingOfCallinBinding4a() {
       
       runConformTest(
            new String[] {
		"Team4132oocb4a_2.java",
			    "\n" +
			    "public team class Team4132oocb4a_2 extends Team4132oocb4a {\n" +
			    "    public class R2 extends R1 { }\n" +
			    "    public class R3 extends R2 {\n" +
			    "        void rm3() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "        b1: rm3 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb4a_2 t = new Team4132oocb4a_2();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb4a b = new T4132oocb4a();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb4a.java",
			    "\n" +
			    "public class T4132oocb4a {\n" +
			    "    void bm() { \n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team4132oocb4a.java",
			    "\n" +
			    "public team class Team4132oocb4a {\n" +
			    "    public class R1 playedBy T4132oocb4a {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOTO\"); \n" +
			    "        }\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "        b2: rm2 <- before bm;\n" +
			    "        precedence b1, b2;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a named callin binding is overridden by an explicit subsub role in the same team
    // 4.1.32-otjld-overriding-of-callin-binding-5
    public void test4132_overridingOfCallinBinding5() {
       
       runConformTest(
            new String[] {
		"Team4132oocb5.java",
			    "\n" +
			    "public team class Team4132oocb5 {\n" +
			    "    public class R1 playedBy T4132oocb5 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOTOK\"); \n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "    }\n" +
			    "    public class R2 extends R1 {}\n" +
			    "    public class R3 extends R2 {\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        b1: rm2 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb5 t = new Team4132oocb5();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb5 b = new T4132oocb5();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb5.java",
			    "\n" +
			    "public class T4132oocb5 {\n" +
			    "    void bm() { \n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a callin binding overrides another, but additionally there is a precedence declaration between them
    // 4.1.32-otjld-overriding-of-callin-binding-6
    public void test4132_overridingOfCallinBinding6() {
       
       runConformTest(
            new String[] {
		"Team4132oocb6.java",
			    "\n" +
			    "public team class Team4132oocb6 {\n" +
			    "    precedence R2, R1;\n" +
			    "\n" +
			    "    public class R1 playedBy T4132oocb6 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOT\"); \n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "    }\n" +
			    "    public class R2 extends R1 {\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        b1: rm2 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb6 t = new Team4132oocb6();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb6 b = new T4132oocb6();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb6.java",
			    "\n" +
			    "public class T4132oocb6 {\n" +
			    "    void bm() { \n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a callin binding overrides another, but additionally there is a precedence declaration between them - different order
    // 4.1.32-otjld-overriding-of-callin-binding-6a
    public void test4132_overridingOfCallinBinding6a() {
       
       runConformTest(
            new String[] {
		"Team4132oocb6a.java",
			    "\n" +
			    "public team class Team4132oocb6a {\n" +
			    "    precedence R1, R2;\n" +
			    "\n" +
			    "    public class R1 playedBy T4132oocb6a {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOT\"); \n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "    }\n" +
			    "    public class R2 extends R1 {\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        b1: rm2 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb6a t = new Team4132oocb6a();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb6a b = new T4132oocb6a();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb6a.java",
			    "\n" +
			    "public class T4132oocb6a {\n" +
			    "    void bm() { \n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a callin binding overrides another, but additionally there is a precedence declaration between them
    // 4.1.32-otjld-overriding-of-callin-binding-7
    public void test4132_overridingOfCallinBinding7() {
       
       runConformTest(
            new String[] {
		"Team4132oocb7.java",
			    "\n" +
			    "public team class Team4132oocb7 {\n" +
			    "    precedence R2, R1;\n" +
			    "\n" +
			    "    public class R1 playedBy T4132oocb7 {\n" +
			    "        void rm1() { \n" +
			    "            System.out.print(\"NOT\"); \n" +
			    "        }\n" +
			    "        void rm3() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        b1: rm1 <- before bm;\n" +
			    "        b2: rm3 <- before bm;\n" +
			    "    }\n" +
			    "    public class R2 extends R1 {\n" +
			    "        void rm2() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "        b1: rm2 <- before bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb7 t = new Team4132oocb7();\n" +
			    "        t.activate();\n" +
			    "        T4132oocb7 b = new T4132oocb7();\n" +
			    "        b.bm();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4132oocb7.java",
			    "\n" +
			    "public class T4132oocb7 {\n" +
			    "    void bm() { \n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK!");
    }

    // a precedence list mentions to callin bindings overriding each other
    // 4.1.32-otjld-overriding-of-callin-binding-8
    public void test4132_overridingOfCallinBinding8() {
        runNegativeTestMatching(
            new String[] {
		"Team4132oocb8.java",
			    "\n" +
			    "public team class Team4132oocb8 {\n" +
			    "	protected class R1 playedBy T4132oocb8 {\n" +
			    "		void rm() {}\n" +
			    "		b1: rm <- after bm;\n" +
			    "	}\n" +
			    "	protected class R2 extends R1 {\n" +
			    "		void rm2() {}\n" +
			    "		b1: rm2 <- after bm;\n" +
			    "	}\n" +
			    "	precedence R2.b1, R1.b1;\n" +
			    "}\n" +
			    "	\n",
		"T4132oocb8.java",
			    "\n" +
			    "public class T4132oocb8 {\n" +
			    "	void bm() {}\n" +
			    "}\n" +
			    "	\n"
            },
            "4.8(e)");
    }

    // a callin label is used twice within the same role class
    // 4.1.32-otjld-overriding-of-callin-binding-9
    public void test4132_overridingOfCallinBinding9() {
        runNegativeTestMatching(
            new String[] {
		"Team4132oocb9.java",
			    "\n" +
			    "public team class Team4132oocb9 {\n" +
			    "    protected class R playedBy T4132oocb9 {\n" +
			    "        void test1() { }\n" +
			    "        void test2() { }\n" +
			    "        label: test1 <- after bm;\n" +
			    "        label: test2 <- after bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team4132oocb9 t = new Team4132oocb9();\n" +
			    "        t.activate();\n" +
			    "        new T4132oocb9().bm();\n" +
			    "    }\n" +
			    "}\n" +
			    "	\n",
		"T4132oocb9.java",
			    "\n" +
			    "public class T4132oocb9 {\n" +
			    "	void bm() {}\n" +
			    "}\n" +
			    "	\n"
            },
            "4.1(e)");
    }

    // a callin method is marked private
    // 4.1.33-otjld-callin-to-private-1
    public void test4133_callinToPrivate1() {
        runNegativeTestMatching(
            new String[] {
		"Team4133ctp1.java",
			    "\n" +
			    "public team class Team4133ctp1 {\n" +
			    "	protected class R playedBy T4133ctp1 {\n" +
			    "		private callin void rm() {\n" +
			    "			System.out.print(\"O\");\n" +
			    "			base.rm();\n" +
			    "		}\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T4133ctp1.java",
			    "\n" +
			    "public team class T4133ctp1 {\n" +
			    "	void bm() {\n" +
			    "		System.out.print(\"K\");\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "4.2(d)");
    }

    // a private role method is before bound
    // 4.1.33-otjld-callin-to-private-2
    public void test4133_callinToPrivate2() {
    	//FIXME HACK:
    	//this.verifier = getTestVerifier(true);
       
       runConformTest(
            new String[] {
		"Team4133ctp2.java",
			    "\n" +
			    "public team class Team4133ctp2 {\n" +
			    "	protected class R playedBy T4133ctp2 {\n" +
			    "		private void rm() {\n" +
			    "			System.out.print(\"O\");\n" +
			    "		}\n" +
			    "		rm <- before bm;\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team4133ctp2().activate();\n" +
			    "		new T4133ctp2().bm();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T4133ctp2.java",
			    "\n" +
			    "public team class T4133ctp2 {\n" +
			    "	void bm() {\n" +
			    "		System.out.print(\"K\");\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a private static role method is before bound
    // 4.1.33-otjld-callin-to-private-2s
    public void test4133_callinToPrivate2s() {
       
       runConformTest(
            new String[] {
		"Team4133ctp2s.java",
			    "\n" +
			    "public team class Team4133ctp2s {\n" +
			    "        protected class R playedBy T4133ctp2s {\n" +
			    "                private static void rm(Object o) {\n" +
			    "                        System.out.print(o);\n" +
			    "                }\n" +
			    "                void rm(Object o) <- before void bm()\n" +
			    "                    with { o <- \"O\" }\n" +
			    "        }\n" +
			    "        public static void main(String[] args) {\n" +
			    "                new Team4133ctp2s().activate();\n" +
			    "                new T4133ctp2s().bm();\n" +
			    "        }\n" +
			    "}\n" +
			    "        \n",
		"T4133ctp2s.java",
			    "\n" +
			    "public team class T4133ctp2s {\n" +
			    "        void bm() {\n" +
			    "                System.out.print(\"K\");\n" +
			    "        }\n" +
			    "}\n" +
			    "        \n"
            },
            "OK");
    }

    // a callin method has several modifiers
    // 4.1.33-otjld-callin-to-private-3
    public void test4133_callinToPrivate3() {
        runNegativeTestMatching(
            new String[] {
		"Team4133ctp3.java",
			    "\n" +
			    "public team class Team4133ctp3 {\n" +
			    "	protected class R playedBy T4133ctp3 {\n" +
			    "		synchronized protected callin void rm() {\n" +
			    "			System.out.print(\"O\");\n" +
			    "			base.rm();\n" +
			    "		}\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T4133ctp3.java",
			    "\n" +
			    "public team class T4133ctp3 {\n" +
			    "	void bm() {\n" +
			    "		System.out.print(\"K\");\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "4.2(d)");
    }

    // a try-finally block (->'ret' instruction) is used in a callin method
    // 4.1.34-otjld-local-variables-in-callin-methods-1
    public void test4134_localVariablesInCallinMethods1() {
       
       runConformTest(
            new String[] {
		"Team4134lvicm1.java",
			    "\n" +
			    "public team class Team4134lvicm1 {\n" +
			    "    protected class R playedBy T4134lvicm1 {\n" +
			    "        callin String rm() {\n" +
			    "            String b = base.rm();\n" +
			    "            try {\n" +
			    "                b = b + \"\";\n" +
			    "            } finally {\n" +
			    "                b = \"O\"+b;\n" +
			    "            }   \n" +
			    "            return b;\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T4134lvicm1 b = new T4134lvicm1();\n" +
			    "        Team4134lvicm1 t = new Team4134lvicm1();\n" +
			    "        t.activate();\n" +
			    "        System.out.print(b.bm());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4134lvicm1.java",
			    "\n" +
			    "public team class T4134lvicm1 {\n" +
			    "    String bm() {\n" +
			    "        return \"K\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a local variable in a callin method is incremented
    // 4.1.34-otjld-local-variables-in-callin-methods-2
    public void test4134_localVariablesInCallinMethods2() {
       
       runConformTest(
            new String[] {
		"Team4134lvicm2.java",
			    "\n" +
			    "public team class Team4134lvicm2 {\n" +
			    "    protected class R playedBy T4134lvicm2 {\n" +
			    "        callin void rm(int i) {\n" +
			    "            int my_i = i;\n" +
			    "            base.rm(my_i);\n" +
			    "            for (int my_i2=i+1; my_i2<4; my_i2++) {\n" +
			    "                System.out.print(my_i2);\n" +
			    "            }\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T4134lvicm2 b = new T4134lvicm2();\n" +
			    "        Team4134lvicm2 t = new Team4134lvicm2();\n" +
			    "        t.activate();\n" +
			    "        b.bm(1);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4134lvicm2.java",
			    "\n" +
			    "public team class T4134lvicm2 {\n" +
			    "    void bm(int i) {\n" +
			    "        System.out.print(i);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "123");
    }

    // an exception handler is used in a callin method
    // 4.1.34-otjld-local-variables-in-callin-methods-3
    public void test4134_localVariablesInCallinMethods3() {
       
       runConformTest(
            new String[] {
		"T4134lvicmMain3.java",
			    "\n" +
			    "public class T4134lvicmMain3 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T4134lvicm3 b = new T4134lvicm3();\n" +
			    "        Team4134lvicm3 t = new Team4134lvicm3();\n" +
			    "        t.activate();\n" +
			    "        b.bm(2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4134lvicm3.java",
			    "\n" +
			    "public team class T4134lvicm3 {\n" +
			    "    void bm(int i) {\n" +
			    "        System.out.print(10/i);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4134lvicm3.java",
			    "\n" +
			    "public team class Team4134lvicm3 {\n" +
			    "    protected class R playedBy T4134lvicm3 {\n" +
			    "        callin void rm(int i) {\n" +
			    "            try {\n" +
			    "                base.rm(i-2);\n" +
			    "            } catch(Exception e) {\n" +
			    "                base.rm(i);\n" +
			    "            }\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n"
            },
            "5");
       // FIXME  expect this warning
    }

    // basic type arguments are unused in the role method signaturen
    // 4.1.35-otjld-basic-type-unused-args-1
    public void test4135_basicTypeUnusedArgs1() {
       
       runConformTest(
            new String[] {
		"Team4135btua1.java",
			    "\n" +
			    "public team class Team4135btua1 {\n" +
			    "    protected class R playedBy T4135btua1 {\n" +
			    "        callin void rm() {\n" +
			    "            base.rm();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm <- replace bm;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T4135btua1 b = new T4135btua1();\n" +
			    "        Team4135btua1 t = new Team4135btua1();\n" +
			    "        t.activate();\n" +
			    "        b.bm(true, 1, (float)2.0, (double)3.0, (short)4, (byte)5, 'c', (long)7);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4135btua1.java",
			    "\n" +
			    "public team class T4135btua1 {\n" +
			    "    void bm(boolean b, int i, float f, double d, short s, byte by, char c, long l) {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin method calls its explicit super method
    // 4.1.36-otjld-callin-method-with-super-1
    public void test4136_callinMethodWithSuper1() {
       
       runConformTest(
            new String[] {
		"Team4136cmws1.java",
			    "\n" +
			    "public team class Team4136cmws1 {\n" +
			    "    protected class R1 playedBy T4136cmws1 {\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin void ci () {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        ci <- replace fubar;\n" +
			    "    }\n" +
			    "    protected class R2 extends R1 {\n" +
			    "        callin void ci () {\n" +
			    "            System.out.print(\"O\");\n" +
			    "            super.ci();\n" +
			    "            base.ci();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4136cmws1().activate();\n" +
			    "        new T4136cmws1().fubar();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4136cmws1.java",
			    "\n" +
			    "public class T4136cmws1 {\n" +
			    "    void fubar() {}\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a callin method calls its implicit super method
    // 4.1.36-otjld-callin-method-with-super-2
    public void test4136_callinMethodWithSuper2() {
       
       runConformTest(
            new String[] {
		"Team4136cmws2_2.java",
			    "        \n" +
			    "public team class Team4136cmws2_2 extends Team4136cmws2_1 {\n" +
			    "    protected class R1 {\n" +
			    "        callin void ci () {\n" +
			    "            System.out.print(\"O\");\n" +
			    "            tsuper.ci();\n" +
			    "            base.ci();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4136cmws2_2().activate();\n" +
			    "        new T4136cmws2().fubar();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4136cmws2.java",
			    "\n" +
			    "public class T4136cmws2 {\n" +
			    "    void fubar() {}\n" +
			    "}    \n" +
			    "    \n",
		"Team4136cmws2_1.java",
			    "\n" +
			    "public team class Team4136cmws2_1 {\n" +
			    "    protected class R1 playedBy T4136cmws2 {\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin void ci () {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        ci <- replace fubar;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin method calls its implicit super method which contains a base-call
    // 4.1.36-otjld-callin-method-with-super-3
    public void test4136_callinMethodWithSuper3() {
       
       runConformTest(
            new String[] {
		"Team4136cmws3_2.java",
			    "        \n" +
			    "public team class Team4136cmws3_2 extends Team4136cmws3_1 {\n" +
			    "    protected class R1 {\n" +
			    "        callin void ci () {\n" +
			    "            System.out.print(\"O\");\n" +
			    "            tsuper.ci();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4136cmws3_2().activate();\n" +
			    "        new T4136cmws3().fubar();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T4136cmws3.java",
			    "\n" +
			    "public class T4136cmws3 {\n" +
			    "    void fubar() {\n" +
			    "        System.out.print(\"!\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team4136cmws3_1.java",
			    "\n" +
			    "public team class Team4136cmws3_1 {\n" +
			    "    protected class R1 playedBy T4136cmws3 {\n" +
			    "        callin void ci () {\n" +
			    "            System.out.print(\"K\");\n" +
			    "            base.ci();\n" +
			    "        }\n" +
			    "        ci <- replace fubar;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // a replace callin requires OTJLD 2.3.3(a) for compatibility, which is, however, not reversible
    // 4.1.37-otjld-incompatible-callin-binding-1
    public void test4137_incompatibleCallinBinding1() {
        runNegativeTestMatching(
            new String[] {
		"Team4137icb1.java",
			    "\n" +
			    "public team class Team4137icb1 {\n" +
			    "    protected class R1 { \n" +
			    "        callin void bar(R1 r){\n" +
			    "            base.bar(r); // OTRE generates access to inexistent r._OT$base (when seen through the binding below)\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected class R2 extends R1 playedBy T4137icb1 {\n" +
			    "        bar <- replace foo;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4137icb1().activate();\n" +
			    "        T4137icb1 b = new T4137icb1();\n" +
			    "        b.foo(b);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4137icb1.java",
			    "\n" +
			    "public class T4137icb1 {\n" +
			    "    void foo(T4137icb1 b) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "4.5(d)");
    }

    // a callin binding to an inherited final method would require to override the final method
    // 4.1.38-otjld-callin-to-final-1
    public void test4138_callinToFinal1() {
        runNegativeTestMatching(
            new String[] {
		"Team4138ctf1.java",
			    "\n" +
			    "public team class Team4138ctf1 {\n" +
			    "    protected class R playedBy T4138ctf1_2 {\n" +
			    "        void k() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        k <- after test;\n" +
			    "    }\n" +
			    "    // for running the class and observing:\n" +
			    "    // \"VerifyError: class T4138ctf1_2 overrides final method\"\n" +
			    "    public static void main(String[] args) {} \n" +
			    "}\n" +
			    "    \n",
		"T4138ctf1_1.java",
			    "\n" +
			    "public class T4138ctf1_1 {\n" +
			    "    final void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4138ctf1_2.java",
			    "\n" +
			    "public class T4138ctf1_2 extends T4138ctf1_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "4.1(f)");
    }

    // a callin binding to an inherited final method; correctly adapting the super-base
    // 4.1.38-otjld-callin-to-final-2
    public void test4138_callinToFinal2() {
       
       runConformTest(
            new String[] {
		"Team4138ctf2.java",
			    "\n" +
			    "public team class Team4138ctf2 {\n" +
			    "    protected class R0 playedBy T4138ctf2_1 {\n" +
			    "        void k() {}; // cannot declare abstract because R0 is relevant!\n" +
			    "        k <- after test;\n" +
			    "    }\n" +
			    "    protected class R1 extends R0 playedBy T4138ctf2_2 {\n" +
			    "        void k() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4138ctf2().activate();\n" +
			    "        new T4138ctf2_2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4138ctf2_1.java",
			    "\n" +
			    "public class T4138ctf2_1 {\n" +
			    "    final void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4138ctf2_2.java",
			    "\n" +
			    "public class T4138ctf2_2 extends T4138ctf2_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // multiple different callins from different teams to the same base method
    // 4.1.38-otjld-multiple-callin-1
    public void test4138_multipleCallin1() {
       
       runConformTest(
            new String[] {
		"T4138mc1Main.java",
			    "\n" +
			    "public class T4138mc1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4138mc1_1().activate();\n" +
			    "        new Team4138mc1_2().activate();\n" +
			    "        new T4138mc1().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4138mc1.java",
			    "\n" +
			    "public class T4138mc1 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4138mc1_1.java",
			    "\n" +
			    "public team class Team4138mc1_1 {\n" +
			    "    protected class R playedBy T4138mc1 {\n" +
			    "        callin void t() {\n" +
			    "            base.t();\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        t <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4138mc1_2.java",
			    "\n" +
			    "public team class Team4138mc1_2 {\n" +
			    "    protected class R playedBy T4138mc1 {\n" +
			    "        void u() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "        u <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // multiple different callins from different teams to the same base method - base inheritance
    // 4.1.38-otjld-multiple-callin-2
    public void test4138_multipleCallin2() {
       
       runConformTest(
            new String[] {
		"T4138mc2Main.java",
			    "\n" +
			    "public class T4138mc2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4138mc2_1().activate();\n" +
			    "        new Team4138mc2_2().activate();\n" +
			    "        new T4138mc2_2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4138mc2_1.java",
			    "\n" +
			    "public class T4138mc2_1 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4138mc2_2.java",
			    "\n" +
			    "public class T4138mc2_2 extends T4138mc2_1 {}\n" +
			    "    \n",
		"Team4138mc2_1.java",
			    "\n" +
			    "public team class Team4138mc2_1 {\n" +
			    "    protected class R playedBy T4138mc2_1 {\n" +
			    "        callin void t() {\n" +
			    "            base.t();\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        t <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4138mc2_2.java",
			    "\n" +
			    "public team class Team4138mc2_2 {\n" +
			    "    protected class R playedBy T4138mc2_2 {\n" +
			    "        void u() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "        u <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // multiple different callins from different teams to the same base method - base inheritance
    // simplified load order: load base before teams.
    public void test4138_multipleCallin2l() {
       
       runConformTest(
            new String[] {
		"T4138mc2lMain.java",
			    "\n" +
			    "public class T4138mc2lMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T4138mc2l_2 t = new T4138mc2l_2();" +
			    "        new Team4138mc2l_1().activate();\n" +
			    "        new Team4138mc2l_2().activate();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4138mc2l_1.java",
			    "\n" +
			    "public class T4138mc2l_1 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4138mc2l_2.java",
			    "\n" +
			    "public class T4138mc2l_2 extends T4138mc2l_1 {}\n" +
			    "    \n",
		"Team4138mc2l_1.java",
			    "\n" +
			    "public team class Team4138mc2l_1 {\n" +
			    "    protected class R playedBy T4138mc2l_1 {\n" +
			    "        callin void t() {\n" +
			    "            base.t();\n" +
			    "            System.out.print(\"!\");\n" +
			    "        }\n" +
			    "        t <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4138mc2l_2.java",
			    "\n" +
			    "public team class Team4138mc2l_2 {\n" +
			    "    protected class R playedBy T4138mc2l_2 {\n" +
			    "        void u() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "        u <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // multiple replaces ordered by precedence plus before/after - reported by ofrank
    // 4.1.38-otjld-multiple-callin-3
    public void test4138_multipleCallin3() {
       
       runConformTest(
            new String[] {
		"Team4138mc3.java",
			    "\n" +
			    "public team class Team4138mc3 {\n" +
			    "        static String indent = \"\";\n" +
			    "        String name;\n" +
			    "        Team4138mc3 (String name) {\n" +
			    "                this.name = name;\n" +
			    "        }\n" +
			    "        precedence R1, R0;\n" +
			    "        public class R0 playedBy T4138mc3 {\n" +
			    "                public void beforeMethod() {\n" +
			    "                        System.out.println(name+indent+\"R0 before\");\n" +
			    "                }\n" +
			    "\n" +
			    "                callin void replaceMethod() {\n" +
			    "                        System.out.println(name+indent+\">>>R0 replace\");\n" +
			    "                        String ind = indent;\n" +
			    "                        indent = indent + \"   \";\n" +
			    "                        base.replaceMethod();\n" +
			    "                        indent = ind;\n" +
			    "                        System.out.println(name+indent+\"<<<R0 replace\");\n" +
			    "                }\n" +
			    "\n" +
			    "                public void afterMethod() {\n" +
			    "                        System.out.println(name+indent+\"R0 after\");\n" +
			    "                }\n" +
			    "\n" +
			    "                replaceMethod <- replace abc;\n" +
			    "\n" +
			    "                beforeMethod <- before abc;\n" +
			    "\n" +
			    "                afterMethod <- after abc;\n" +
			    "\n" +
			    "        }\n" +
			    "\n" +
			    "        protected class R1 playedBy T4138mc3 {\n" +
			    "                callin void replaceMethod() {\n" +
			    "                        System.out.println(name+indent+\">>>R1 replace\");\n" +
			    "                        String ind = indent;\n" +
			    "                        indent = indent + \"   \";\n" +
			    "                        base.replaceMethod();\n" +
			    "                        indent = ind;\n" +
			    "                        System.out.println(name+indent+\"<<<R1 replace\");\n" +
			    "                }\n" +
			    "\n" +
			    "                replaceMethod <- replace abc;\n" +
			    "        }\n" +
			    "        public static void main(String[] args) {\n" +
			    "                new Team4138mc3(\"XXX\").activate();\n" +
			    "                new Team4138mc3(\"YYY\").activate();\n" +
			    "                new T4138mc3().abc();\n" +
			    "        }\n" +
			    "}\n" +
			    "    \n",
		"T4138mc3.java",
			    "\n" +
			    "public class T4138mc3 {\n" +
			    "    public void abc () { System.out.println(\"base method\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "YYYR0 before\n" +
			"YYY>>>R1 replace\n" +
			"YYY   >>>R0 replace\n" +
			"XXX      R0 before\n" +
			"XXX      >>>R1 replace\n" +
			"XXX         >>>R0 replace\n" +
			"base method\n" +
			"XXX         <<<R0 replace\n" +
			"XXX      <<<R1 replace\n" +
			"XXX      R0 after\n" +
			"YYY   <<<R0 replace\n" +
			"YYY<<<R1 replace\n" +
			"YYYR0 after");
    }

    // a callin binding intercepts a method that has an override with covariant return - static type is super-base
    // 4.1.39-otjld-callin-binding-with-plus-1
    public void test4139_callinBindingWithPlus1() {
       
       runConformTest(
            new String[] {
		"Team4139cbwp1.java",
			    "\n" +
			    "public team class Team4139cbwp1 {\n" +
			    "    protected class R playedBy T4139cbwp1_1 {\n" +
			    "        void k() { System.out.print(\"O\"); }\n" +
			    "        void k() <- after T4139cbwp1_1+ getOther();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4139cbwp1().activate();\n" +
			    "        T4139cbwp1_1 b = new T4139cbwp1_2();\n" +
			    "        b = b.getOther();\n" +
			    "        b.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp1_1.java",
			    "\n" +
			    "public class T4139cbwp1_1 {\n" +
			    "    T4139cbwp1_1 getOther() { return this; }\n" +
			    "    void test() { System.out.print(\"X\"); }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp1_2.java",
			    "\n" +
			    "public class T4139cbwp1_2 extends T4139cbwp1_1 {\n" +
			    "    @Override\n" +
			    "    T4139cbwp1_2 getOther() { return this; }\n" +
			    "    void test() { System.out.print(\"K\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin binding intercepts a method that has an override with covariant return - static type is super-base - no plus used
    public void test4139_callinBindingWithPlus1a() {
       
       runConformTest(
            new String[] {
		"Team4139cbwp1a.java",
			    "\n" +
			    "public team class Team4139cbwp1a {\n" +
			    "    protected class R playedBy T4139cbwp1a_1 {\n" +
			    "        void k() { System.out.print(\"O\"); }\n" +
			    "        void k() <- after T4139cbwp1a_1 getOther();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4139cbwp1a().activate();\n" +
			    "        T4139cbwp1a_1 b = new T4139cbwp1a_2();\n" +
			    "        b = b.getOther();\n" +
			    "        b.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp1a_1.java",
			    "\n" +
			    "public class T4139cbwp1a_1 {\n" +
			    "    T4139cbwp1a_1 getOther() { return this; }\n" +
			    "    void test() { System.out.print(\"X\"); }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp1a_2.java",
			    "\n" +
			    "public class T4139cbwp1a_2 extends T4139cbwp1a_1 {\n" +
			    "    @Override\n" +
			    "    T4139cbwp1a_2 getOther() { return this; }\n" +
			    "    void test() { System.out.print(\"K\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "K");
    }

    // a callin binding intercepts a method that has an override with covariant return - static type is sub-base
    // 4.1.39-otjld-callin-binding-with-plus-2
    public void test4139_callinBindingWithPlus2() {
       
       runConformTest(
            new String[] {
		"Team4139cbwp2.java",
			    "\n" +
			    "public team class Team4139cbwp2 {\n" +
			    "    protected class R playedBy T4139cbwp2_1 {\n" +
			    "        void k() { System.out.print(\"O\"); }\n" +
			    "        void k() <- after T4139cbwp2_1+ getOther();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4139cbwp2().activate();\n" +
			    "        T4139cbwp2_2 b = new T4139cbwp2_2();\n" +
			    "        b = b.getOther();\n" +
			    "        b.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp2_1.java",
			    "\n" +
			    "public class T4139cbwp2_1 {\n" +
			    "    T4139cbwp2_1 getOther() { return this; }\n" +
			    "    void test() { System.out.print(\"X\"); }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp2_2.java",
			    "\n" +
			    "public class T4139cbwp2_2 extends T4139cbwp2_1 {\n" +
			    "    @Override\n" +
			    "    T4139cbwp2_2 getOther() { return this; }\n" +
			    "    void test() { System.out.print(\"K\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // replace callin to base methods with covariant return types
    // 4.1.39-otjld-callin-binding-with-plus-3
    public void test4139_callinBindingWithPlus3() {
       
       runConformTest(
            new String[] {
		"Team4139cbwp3.java",
			    "\n" +
			    "public team class Team4139cbwp3 {\n" +
			    "    protected class R playedBy T4139cbwp3_1 {\n" +
			    "        callin <T extends T4139cbwp3_1> T k() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            return base.k();\n" +
			    "        }\n" +
			    "        <T extends T4139cbwp3_1> T k() <- replace T4139cbwp3_1+ getOther();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4139cbwp3().activate();\n" +
			    "        T4139cbwp3_2 b = new T4139cbwp3_2();\n" +
			    "        b = b.getOther();\n" +
			    "        b.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp3_1.java",
			    "\n" +
			    "public class T4139cbwp3_1 {\n" +
			    "    T4139cbwp3_1 getOther() { return this; }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp3_2.java",
			    "\n" +
			    "public class T4139cbwp3_2 extends T4139cbwp3_1 {\n" +
			    "    @Override\n" +
			    "    T4139cbwp3_2 getOther() { return this; }\n" +
			    "    void test() { System.out.print(\"K\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // replace callin to base methods with covariant return types, invoked on super-base as the static type
    // 4.1.39-otjld-callin-binding-with-plus-4
    public void test4139_callinBindingWithPlus4() {
       
       runConformTest(
            new String[] {
		"Team4139cbwp4.java",
			    "\n" +
			    "public team class Team4139cbwp4 {\n" +
			    "    protected class R playedBy T4139cbwp4_1 {\n" +
			    "        callin <T extends T4139cbwp4_1> T k() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            return base.k();\n" +
			    "        }\n" +
			    "        <T extends T4139cbwp4_1> T k() <- replace T4139cbwp4_1+ getOther();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4139cbwp4().activate();\n" +
			    "        T4139cbwp4_1 b = new T4139cbwp4_2();\n" +
			    "        b = b.getOther();\n" +
			    "        ((T4139cbwp4_2)b).test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp4_1.java",
			    "\n" +
			    "public class T4139cbwp4_1 {\n" +
			    "    T4139cbwp4_1 getOther() { return this; }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp4_2.java",
			    "\n" +
			    "public class T4139cbwp4_2 extends T4139cbwp4_1 {\n" +
			    "    @Override\n" +
			    "    T4139cbwp4_2 getOther() { return this; }\n" +
			    "    void test() { System.out.print(\"K\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // replace callin to base methods with covariant return types, incompatible return in callin method
    // 4.1.39-otjld-callin-binding-with-plus-5
    public void test4139_callinBindingWithPlus5() {
        runNegativeTestMatching(
            new String[] {
		"Team4139cbwp5.java",
			    "\n" +
			    "public team class Team4139cbwp5 {\n" +
			    "    protected class R playedBy T4139cbwp5_1 {\n" +
			    "        callin <T extends T4139cbwp5_1> T k() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            return new T4139cbwp5_1();\n" +
			    "        }\n" +
			    "        <T extends T4139cbwp5_1> T k() <- replace T4139cbwp5_1+ getOther();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp5_1.java",
			    "\n" +
			    "public class T4139cbwp5_1 {\n" +
			    "    T4139cbwp5_1 getOther() { return this; }\n" +
			    "}\n" +
			    "    \n"
            },
            "cannot convert");
    }

    // replace callin to base methods with covariant return types, same as -5 but not using generics
    // 4.1.39-otjld-callin-binding-with-plus-6
    public void test4139_callinBindingWithPlus6() {
        runNegativeTestMatching(
            new String[] {
		"Team4139cbwp6.java",
			    "\n" +
			    "public team class Team4139cbwp6 {\n" +
			    "    protected class R playedBy T4139cbwp6_1 {\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin T4139cbwp6_1 k() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            return new T4139cbwp6_1();\n" +
			    "        }\n" +
			    "        T4139cbwp6_1 k() <- replace T4139cbwp6_1+ getOther();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp6_1.java",
			    "\n" +
			    "public class T4139cbwp6_1 {\n" +
			    "    T4139cbwp6_1 getOther() { return this; }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.9.3(c)");
    }

    // replace callin to base methods with covariant return types, same as -5 but role methods is not using generics (callin binding however is)
    // 4.1.39-otjld-callin-binding-with-plus-7
    public void test4139_callinBindingWithPlus7() {
        runNegativeTestMatching(
            new String[] {
		"Team4139cbwp7.java",
			    "\n" +
			    "public team class Team4139cbwp7 {\n" +
			    "    protected class R playedBy T4139cbwp7_1 {\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin T4139cbwp7_1 k() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            return new T4139cbwp7_1();\n" +
			    "        }\n" +
			    "        <T extends T4139cbwp7_1> T k() <- replace T4139cbwp7_1+ getOther();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp7_1.java",
			    "\n" +
			    "public class T4139cbwp7_1 {\n" +
			    "    T4139cbwp7_1 getOther() { return this; }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.1(c)");
    }

    // replace callin to base methods with covariant return types,  base call result is stored and modified
    // 4.1.39-otjld-callin-binding-with-plus-8
    public void test4139_callinBindingWithPlus8() {
       
       runConformTest(
            new String[] {
		"Team4139cbwp8.java",
			    "\n" +
			    "public team class Team4139cbwp8 {\n" +
			    "    protected class R playedBy T4139cbwp8_1 {\n" +
			    "        callin <T extends T4139cbwp8_1> T k() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            T result= base.k();\n" +
			    "            result.setVal(\"K\");\n" +
			    "            return result;\n" +
			    "        }\n" +
			    "        <T extends T4139cbwp8_1> T k() <- replace T4139cbwp8_1+ getOther();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4139cbwp8().activate();\n" +
			    "        T4139cbwp8_1 b = new T4139cbwp8_2();\n" +
			    "        b = b.getOther();\n" +
			    "        ((T4139cbwp8_2)b).test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp8_1.java",
			    "\n" +
			    "public abstract class T4139cbwp8_1 {\n" +
			    "    T4139cbwp8_1 getOther() { return this; }\n" +
			    "    abstract void setVal(String val);\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp8_2.java",
			    "\n" +
			    "public class T4139cbwp8_2 extends T4139cbwp8_1 {\n" +
			    "    String val;\n" +
			    "    void setVal(String val) {\n" +
			    "        this.val= val;\n" +
			    "    }\n" +
			    "    @Override\n" +
			    "    T4139cbwp8_2 getOther() { return this; }\n" +
			    "    void test() { System.out.print(val); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // replace callin to base methods with covariant return types - unbounded type variable
    // 4.1.39-otjld-callin-binding-with-plus-9
    public void test4139_callinBindingWithPlus9() {
       
       runConformTest(
            new String[] {
		"Team4139cbwp9.java",
			    "\n" +
			    "public team class Team4139cbwp9 {\n" +
			    "    protected class R playedBy T4139cbwp9_1 {\n" +
			    "        callin <T> T k() { \n" +
			    "            System.out.print(\"O\");\n" +
			    "            return base.k();\n" +
			    "        }\n" +
			    "        <T> T k() <- replace T4139cbwp9_1+ getOther();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4139cbwp9().activate();\n" +
			    "        T4139cbwp9_2 b = new T4139cbwp9_2();\n" +
			    "        b = b.getOther();\n" +
			    "        b.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp9_1.java",
			    "\n" +
			    "public class T4139cbwp9_1 {\n" +
			    "    T4139cbwp9_1 getOther() { return this; }\n" +
			    "}\n" +
			    "    \n",
		"T4139cbwp9_2.java",
			    "\n" +
			    "public class T4139cbwp9_2 extends T4139cbwp9_1 {\n" +
			    "    @Override\n" +
			    "    T4139cbwp9_2 getOther() { return this; }\n" +
			    "    void test() { System.out.print(\"K\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // base call result is lifting-compatible to type bound
    // 4.1.40-otjld-callin-replace-compatibility-5
    public void test4140_callinReplaceCompatibility5() {
       
       runConformTest(
            new String[] {
		"Team4140crc5.java",
			    "\n" +
			    "public team class Team4140crc5 {\n" +
			    "    protected class R playedBy T4140crc5 {\n" +
			    "        protected void print() { System.out.print(\"O\"); }\n" +
			    "        callin <T extends R> T mingle() {\n" +
			    "			T t= base.mingle();\n" +
			    "			t.print();\n" +
			    "			return t;\n" +
			    "		}\n" +
			    "		<T extends R> T mingle() <- replace T4140crc5 m();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        within(new Team4140crc5()) {\n" +
			    "            System.out.print(new T4140crc5().m());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc5.java",
			    "\n" +
			    "public class T4140crc5 {\n" +
			    "	public T4140crc5 m() {\n" +
			    "	   return new T4140crc5();\n" +
			    "	}\n" +
			    "	public String toString() {\n" +
			    "		return \"K\";\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // base call result is lifting-compatible to exact type -- reference for previous test
    // 4.1.40-otjld-callin-replace-compatibility-6
    public void test4140_callinReplaceCompatibility6() {
       
       runConformTest(
            new String[] {
		"Team4140crc6.java",
			    "\n" +
			    "public team class Team4140crc6 {\n" +
			    "    protected class R playedBy T4140crc6 {\n" +
			    "        void print() { System.out.print(\"O\"); }\n" +
			    "        callin R mingle() {\n" +
			    "			R t= base.mingle();\n" +
			    "			t.print();\n" +
			    "			return t;\n" +
			    "		}\n" +
			    "		R mingle() <- replace T4140crc6 m();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        within(new Team4140crc6()) {\n" +
			    "            System.out.print(new T4140crc6().m());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc6.java",
			    "\n" +
			    "public class T4140crc6 {\n" +
			    "	public T4140crc6 m() {\n" +
			    "	   return new T4140crc6();\n" +
			    "	}\n" +
			    "	public String toString() {\n" +
			    "		return \"K\";\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a parameter in a replace binding is lifting compatible
    // 4.1.40-otjld-callin-replace-compatibility-7
    public void test4140_callinReplaceCompatibility7() {
       
       runConformTest(
            new String[] {
		"Team4140crc7.java",
			    "\n" +
			    "public team class Team4140crc7 {\n" +
			    "    protected class R playedBy T4140crc7 {\n" +
			    "        void k() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        callin void otherK(R other) {\n" +
			    "            base.otherK(other);\n" +
			    "            other.k();\n" +
			    "        }\n" +
			    "        otherK <- replace otherO;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        within (new Team4140crc7()) {\n" +
			    "            new T4140crc7().otherO(new T4140crc7());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc7.java",
			    "\n" +
			    "public class T4140crc7 {\n" +
			    "    public void o() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "    public void otherO(T4140crc7 other) {\n" +
			    "        other.o();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // the return of a callin method is an unbound thus incompatible role
    // 4.1.40-otjld-callin-replace-compatibility-8
    public void test4140_callinReplaceCompatibility8() {
        runNegativeTestMatching(
            new String[] {
		"Team4140crc8.java",
			    "\n" +
			    "public team class Team4140crc8 {\n" +
			    "    protected class R0 {}\n" +
			    "    protected class R playedBy T4140crc8 {\n" +
			    "        void k() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        callin R0 otherK(R other) {\n" +
			    "            R0 r= base.otherK(other);\n" +
			    "            other.k();\n" +
			    "            return r;\n" +
			    "        }\n" +
			    "        otherK <- replace otherO;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        within (new Team4140crc8()) {\n" +
			    "            new T4140crc8().otherO(new T4140crc8());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc8.java",
			    "\n" +
			    "public class T4140crc8 {\n" +
			    "    public void o() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "    public T4140crc8 otherO(T4140crc8 other) {\n" +
			    "        other.o();\n" +
			    "        return other;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.4(b)");
    }

    // two different base methods have return types that are compatible to the bound of a type parameter
    // 4.1.40-otjld-callin-replace-compatibility-9
    public void test4140_callinReplaceCompatibility9() {
       
       runConformTest(
            new String[] {
		"Team4140crc9.java",
			    "\n" +
			    "public team class Team4140crc9 {\n" +
			    "    protected class R playedBy T4140crc9 {\n" +
			    "        callin <T extends Object> T zork() {\n" +
			    "            T o= base.zork();\n" +
			    "            System.out.print(o.toString());\n" +
			    "            return o;\n" +
			    "        }\n" +
			    "        //zork <- replace foo, bar;\n" +
			    "        <T extends Object> T zork() <- replace String foo(), Object bar();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4140crc9().activate();\n" +
			    "        T4140crc9 b= new T4140crc9();\n" +
			    "        String s= b.foo();\n" +
			    "        System.out.print(s.toUpperCase());\n" +
			    "        System.out.print(b.bar());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc9.java",
			    "\n" +
			    "public team class T4140crc9 {\n" +
			    "    String foo () { return \"foo\"; }\n" +
			    "    Object bar () { return new Object() { public String toString() { return \"bar\"; } }; }\n" +
			    "}\n" +
			    "    \n"
            },
            "fooFOObarbar");
    }

    // two different base methods have return types that are compatible to the bound of a type parameter -- signatureless binding
    // 4.1.40-otjld-callin-replace-compatibility-9s
    public void test4140_callinReplaceCompatibility9s() {
       
       runConformTest(
            new String[] {
		"Team4140crc9s.java",
			    "\n" +
			    "public team class Team4140crc9s {\n" +
			    "    protected class R playedBy T4140crc9s {\n" +
			    "        callin <T extends Object> T zork() {\n" +
			    "            T o= base.zork();\n" +
			    "            System.out.print(o.toString());\n" +
			    "            return o;\n" +
			    "        }\n" +
			    "        zork <- replace foo, bar;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4140crc9s().activate();\n" +
			    "        T4140crc9s b= new T4140crc9s();\n" +
			    "        String s= b.foo();\n" +
			    "        System.out.print(s.toUpperCase());\n" +
			    "        System.out.print(b.bar());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc9s.java",
			    "\n" +
			    "public team class T4140crc9s {\n" +
			    "    String foo () { return \"foo\"; }\n" +
			    "    Object bar () { return new Object() { public String toString() { return \"bar\"; } }; }\n" +
			    "}\n" +
			    "    \n"
            },
            "fooFOObarbar");
    }

    // two different base methods have parameter types that are compatible to the bound of a type parameter
    // 4.1.40-otjld-callin-replace-compatibility-10
    public void test4140_callinReplaceCompatibility10() {
       
       runConformTest(
            new String[] {
		"Team4140crc10.java",
			    "\n" +
			    "public team class Team4140crc10 {\n" +
			    "    protected class R playedBy T4140crc10 {\n" +
			    "        callin <T extends T4140crc10> void fred(T o) {\n" +
			    "            o.fub();\n" +
			    "            base.fred(o);\n" +
			    "        }\n" +
			    "        <T extends T4140crc10> void fred(T o) <- replace void process(T4140crc10 o), void process2(T4140crc10.T4140crc10_2  o);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4140crc10().activate();\n" +
			    "        T4140crc10 b= new T4140crc10();\n" +
			    "        b.process(b);\n" +
			    "        b.process2(new T4140crc10.T4140crc10_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc10.java",
			    "\n" +
			    "public class T4140crc10 {\n" +
			    "    static class T4140crc10_2 extends T4140crc10 {\n" +
			    "        void zork() { System.out.print(\"zork\"); }\n" +
			    "    }\n" +
			    "    public String toString() { return \"T4140crc10\"; }\n" +
			    "    void fub() { System.out.print(\"fub\"); }\n" +
			    "    void process(T4140crc10 o) { System.out.print(o); }\n" +
			    "    void process2(T4140crc10_2  o) { o.zork(); }\n" +
			    "}\n" +
			    "    \n"
            },
            "fubT4140crc10fubzork");
    }

    // two different base methods have parameter types that are compatible to the bound of a type parameter -- signatureless binding
    // 4.1.40-otjld-callin-replace-compatibility-10s
    public void test4140_callinReplaceCompatibility10s() {
       
       runConformTest(
            new String[] {
		"Team4140crc10s.java",
			    "\n" +
			    "public team class Team4140crc10s {\n" +
			    "    protected class R playedBy T4140crc10s {\n" +
			    "        callin <T extends T4140crc10s> void fred(T o) {\n" +
			    "            o.fub();\n" +
			    "            base.fred(o);\n" +
			    "        }\n" +
			    "        fred <- replace process, process2;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4140crc10s().activate();\n" +
			    "        T4140crc10s b= new T4140crc10s();\n" +
			    "        b.process(b);\n" +
			    "        b.process2(new T4140crc10s.T4140crc10s_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc10s.java",
			    "\n" +
			    "public class T4140crc10s {\n" +
			    "    static class T4140crc10s_2 extends T4140crc10s {\n" +
			    "        void zork() { System.out.print(\"zork\"); }\n" +
			    "    }\n" +
			    "    public String toString() { return \"T4140crc10s\"; }\n" +
			    "    void fub() { System.out.print(\"fub\"); }\n" +
			    "    void process(T4140crc10s o) { System.out.print(o); }\n" +
			    "    void process2(T4140crc10s_2  o) { o.zork(); }\n" +
			    "}\n" +
			    "    \n"
            },
            "fubT4140crc10sfubzork");
    }

    // a base method has parameter types that are compatible to the bound of a type parameter, 2 params, with mapping (swap)
    // 4.1.40-otjld-callin-replace-compatibility-11
    public void test4140_callinReplaceCompatibility11() {
       
       runConformTest(
            new String[] {
		"Team4140crc11.java",
			    "\n" +
			    "public team class Team4140crc11 {\n" +
			    "    protected class R playedBy T4140crc11_2 {\n" +
			    "        callin <T1 extends T4140crc11_1, T2 extends T4140crc11_2> void fred(T1 o1, T2 o2) {\n" +
			    "            o1.fub1();\n" +
			    "            o2.fub2();\n" +
			    "            base.fred(o1, o2);\n" +
			    "        }\n" +
			    "        <T1 extends T4140crc11_1, T2 extends T4140crc11_2> void fred(T1 o1, T2 o2) <- replace void process(T4140crc11_2 b2, T4140crc11_1.Sub1 b1)\n" +
			    "            with {\n" +
			    "                o1 <- b1,\n" +
			    "                o2 <- b2\n" +
			    "            }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4140crc11().activate();\n" +
			    "        T4140crc11_2 b= new T4140crc11_2.Sub2();\n" +
			    "        b.process(b, new T4140crc11_1.Sub1());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc11_1.java",
			    "\n" +
			    "public class T4140crc11_1 {\n" +
			    "    static class Sub1 extends T4140crc11_1 {\n" +
			    "        void zork1() { System.out.print(\"zork1\"); }\n" +
			    "    }\n" +
			    "    public String toString() { return \"T4140crc11_1\"; }\n" +
			    "    void fub1() { System.out.print(\"fub1\"); }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc11_2.java",
			    "\n" +
			    "public class T4140crc11_2 {\n" +
			    "    static class Sub2 extends T4140crc11_2 {\n" +
			    "        public String toString() { return \"Sub2\"; }\n" +
			    "    }\n" +
			    "    public String toString() { return \"T4140crc11_2\"; }\n" +
			    "    void fub2() { System.out.print(\"fub2\"); }\n" +
			    "    void process(T4140crc11_2 b2, T4140crc11_1.Sub1 b1) {\n" +
			    "        System.out.print(b2);\n" +
			    "        b1.zork1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "fub1fub2Sub2zork1");
    }

    // a base method has parameter types that are compatible to the bound of a type parameter, 2 params, with partial mapping
    // 4.1.40-otjld-callin-replace-compatibility-12
    public void test4140_callinReplaceCompatibility12() {
       
       runConformTest(
            new String[] {
		"Team4140crc12.java",
			    "\n" +
			    "public team class Team4140crc12 {\n" +
			    "    protected class R playedBy T4140crc12_2 {\n" +
			    "        callin <T1 extends T4140crc12_1> void fred(T1 o1) {\n" +
			    "            o1.fub1();\n" +
			    "            base.fred(o1);\n" +
			    "        }\n" +
			    "        <T1 extends T4140crc12_1> void fred(T1 o1) <- replace void process(T4140crc12_2 b2, T4140crc12_1.Sub1 b1)\n" +
			    "            with {\n" +
			    "                o1 <- b1\n" +
			    "            }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4140crc12().activate();\n" +
			    "        T4140crc12_2 b= new T4140crc12_2.Sub2();\n" +
			    "        b.process(b, new T4140crc12_1.Sub1());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc12_1.java",
			    "\n" +
			    "public class T4140crc12_1 {\n" +
			    "    static class Sub1 extends T4140crc12_1 {\n" +
			    "        void zork1() { System.out.print(\"zork1\"); }\n" +
			    "    }\n" +
			    "    public String toString() { return \"T4140crc12_1\"; }\n" +
			    "    void fub1() { System.out.print(\"fub1\"); }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc12_2.java",
			    "\n" +
			    "public class T4140crc12_2 {\n" +
			    "    static class Sub2 extends T4140crc12_2 {\n" +
			    "        public String toString() { return \"Sub2\"; }\n" +
			    "    }\n" +
			    "    public String toString() { return \"T4140crc12_2\"; }\n" +
			    "    void fub2() { System.out.print(\"fub2\"); }\n" +
			    "    void process(T4140crc12_2 b2, T4140crc12_1.Sub1 b1) {\n" +
			    "        System.out.print(b2);\n" +
			    "        b1.zork1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "fub1Sub2zork1");
    }

    // a role method uses the same type parameter twice - not used for covariant return
    // 4.1.40-otjld-callin-replace-compatibility-13
    public void test4140_callinReplaceCompatibility13() {
        runConformTest(
            new String[] {
		"Team4140crc13.java",
			    "\n" +
			    "public team class Team4140crc13 {\n" +
			    "    protected class R {\n" +
			    "        callin <T> void ci (T a1, T a2) {\n" +
			    "            base.ci(a1, a2);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            });
    }

    // a role method uses the same type parameter twice - trying to capture covariant return
    // 4.1.40-otjld-callin-replace-compatibility-14
    public void test4140_callinReplaceCompatibility14() {
        runNegativeTestMatching(
            new String[] {
		"Team4140crc14.java",
			    "\n" +
			    "public team class Team4140crc14 {\n" +
			    "    protected class R playedBy T4140crc14 {\n" +
			    "        callin <T> T ci (Object a1, T a2) {\n" +
			    "            return base.ci(a1, a2);\n" +
			    "        }\n" +
			    "        <T> T ci(Object a1, T a2) <- replace Number+ bm(Object a1, Number a2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4140crc14.java",
			    "\n" +
			    "public class T4140crc14 {\n" +
			    "    Number bm(Object a1, Number a2) { return null; }\n" +
			    "}\n" +
			    "    \n"
            },
            "4.10(a)");
    }

    // a callin binds to method hashCode
    // 4.1.41-otjld-dangerous-callin-binding-1
    public void test4141_dangerousCallinBinding1() {
        runTestExpectingWarnings(
            new String[] {
		"T4141dcb1.java",
			    "\n" +
			    "public class T4141dcb1 {}\n" +
			    "    \n",
		"Team4141dcb1.java",
			    "\n" +
			    "public team class Team4141dcb1 {\n" +
			    "    protected class R playedBy T4141dcb1 {\n" +
			    "		 @SuppressWarnings(\"basecall\")\n" +
			    "        callin int fourtytwo () { return 42; }\n" +
			    "        fourtytwo <- replace hashCode;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. WARNING in Team4141dcb1.java (at line 6)\n" +
			"	fourtytwo <- replace hashCode;\n" +
			"	                     ^^^^^^^^\n" +
			"Dangerous callin binding: hashCode() is used internally during lifting, which could create infinite recursion.\n" +
			"----------\n");
    }

    // a callin binds to method equals - signature included, after callin
    // 4.1.41-otjld-dangerous-callin-binding-2
    public void test4141_dangerousCallinBinding2() {
        runTestExpectingWarnings(
            new String[] {
		"T4141dcb2.java",
			    "\n" +
			    "public class T4141dcb2 {}\n" +
			    "    \n",
		"Team4141dcb2.java",
			    "\n" +
			    "public team class Team4141dcb2 {\n" +
			    "    protected class R playedBy T4141dcb2 {\n" +
			    "        void nothing() { }\n" +
			    "        void nothing() <- after boolean equals(Object other);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. WARNING in Team4141dcb2.java (at line 5)\n" +
			"	void nothing() <- after boolean equals(Object other);\n" +
			"	                                ^^^^^^\n" +
			"Dangerous callin binding: equals(Object) is used internally during lifting, which could create infinite recursion.\n" +
			"----------\n");
    }

    // a callin binds to method equals - warning suppressed
    // 4.1.41-otjld-dangerous-callin-binding-3
    public void test4141_dangerousCallinBinding3() {
       
       runConformTest(
            new String[] {
		"Team4141dcb3.java",
			    "\n" +
			    "public team class Team4141dcb3 {\n" +
			    "    protected class R playedBy T4141dcb3 {\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin boolean always() { return true; }\n" +
			    "        @SuppressWarnings(\"dangerouscallin\")\n" +
			    "        boolean always() <- replace boolean equals(Object other)\n" +
			    "            base when (!isExecutingCallin());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4141dcb3().activate();\n" +
			    "        System.out.print(new T4141dcb3().equals(new T4141dcb3()));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4141dcb3.java",
			    "\n" +
			    "public class T4141dcb3 {}\n" +
			    "    \n"
            },
            "true");
    }

    // a callin binding is inherited, read from binary - witness for NPE in SingleValueAttribute.evaluate reported by Marco
    // 4.1.42-otjld-inheritance-of-callin-binding-1
    public void test4142_inheritanceOfCallinBinding1() {
       
       runConformTest(
            new String[] {
		"T4142iocb1Main.java",
			    "\n" +
			    "public class T4142iocb1Main {\n" +
			    "  public static void main(String[] args) {\n" +
			    "    final Team4142iocb1_2 t = new Team4142iocb1_2();\n" +
			    "    t.activate();\n" +
			    "    new T4142iocb1().foo();\n" +
			    "    R<@t> r = t.getR(); // force reading class R.\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"T4142iocb1.java",
			    "\n" +
			    "public class T4142iocb1 {\n" +
			    "  void foo() {}\n" +
			    "}\n" +
			    "  \n",
		"Team4142iocb1_1.java",
			    "\n" +
			    "public team class Team4142iocb1_1 {\n" +
			    "  R r;\n" +
			    "  public class R playedBy T4142iocb1 {\n" +
			    "    private void test() {\n" +
			    "      System.out.print(\"OK\");\n" +
			    "      r=this;\n" +
			    "    }\n" +
			    "    test <- after foo; // callin to private role method requires copy-inh of wrapper\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"Team4142iocb1_2.java",
			    "\n" +
			    "public team class Team4142iocb1_2 extends Team4142iocb1_1 {\n" +
			    "  public R getR() { return this.r; }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // a bound base method is deprecated
    // 4.1.43-otjld-callin-to-deprecated-1
    public void test4143_callinToDeprecated1() {
        runNegativeTestMatching(
            new String[] {
		"Team4143ctd1.java",
			    "\n" +
			    "public team class Team4143ctd1 {\n" +
			    "    protected class R playedBy T4143ctd1 {\n" +
			    "        void rm() {}\n" +
			    "        rm <- after test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4143ctd1.java",
			    "\n" +
			    "public class T4143ctd1 {\n" +
			    "    /** @deprecated don't use. */\n" +
			    "    @Deprecated\n" +
			    "    void test() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team4143ctd1.java (at line 5)\n" + 
    		"	rm <- after test;\n" + 
    		"	            ^^^^\n" + 
    		"Bound base method test() is deprecated.\n" + 
    		"----------\n");
    }

    // a bound base method is deprecated - configured to warning
    // 4.1.43-otjld-callin-to-deprecated-1w
    public void test4143_callinToDeprecated1w() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportAdaptingDeprecated, CompilerOptions.WARNING);
        runTestExpectingWarnings(
            new String[] {
		"T4143ctd1w.java",
			    "\n" +
			    "public class T4143ctd1w {\n" +
			    "    /** @deprecated don't use. */\n" +
			    "    @Deprecated\n" +
			    "    void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team4143ctd1w.java",
			    "\n" +
			    "public team class Team4143ctd1w {\n" +
			    "    protected class R playedBy T4143ctd1w {\n" +
			    "        void rm() {}\n" +
			    "        rm <- after test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. WARNING in Team4143ctd1w.java (at line 5)\n" + 
    		"	rm <- after test;\n" + 
    		"	            ^^^^\n" + 
    		"Bound base method test() is deprecated.\n" + 
    		"----------\n",
    		customOptions);
    }

    // an after callin binding refers to a team method instead of a role method
    // 4.1.43-otjld-callin-to-team-method-1
    public void test4143_callinToTeamMethod1() {
       
       runConformTest(
            new String[] {
		"Team4143cttm1.java",
			    "\n" +
			    "public team class Team4143cttm1 {\n" +
			    "    void k(String s) {\n" +
			    "        System.out.print(s);\n" +
			    "    }\n" +
			    "    protected class R playedBy T4143cttm1 {\n" +
			    "        void k(String s) <- after void test()\n" +
			    "            with { s <- \"K\" }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4143cttm1().activate();\n" +
			    "        new T4143cttm1().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4143cttm1.java",
			    "\n" +
			    "public class T4143cttm1 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an after callin binding refers to an inherited team method instead of a role method
    // 4.1.43-otjld-callin-to-team-method-2
    public void test4143_callinToTeamMethod2() {
       
       runConformTest(
            new String[] {
		"Team4143cttm2_2.java",
			    "\n" +
			    "public team class Team4143cttm2_2 extends Team4143cttm2_1 {\n" +
			    "    protected class R playedBy T4143cttm2 {\n" +
			    "        void k(String s) <- after void test()\n" +
			    "            with { s <- \"K\" }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4143cttm2_2().activate();\n" +
			    "        new T4143cttm2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4143cttm2.java",
			    "\n" +
			    "public class T4143cttm2 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4143cttm2_1.java",
			    "\n" +
			    "public team class Team4143cttm2_1 {\n" +
			    "    void k(String s) {\n" +
			    "        System.out.print(s);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a short after callin binding refers to a team method instead of a role method
    // 4.1.43-otjld-callin-to-team-method-3
    public void test4143_callinToTeamMethod3() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
       customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team4143cttm3.java",
			    "\n" +
			    "/**\n" +
			    " * @role R\n" +
			    " */\n" +
			    "public team class Team4143cttm3 {\n" +
			    "    private void k(String s) {\n" + // extra difficulty: team method is private
			    "        System.out.print(s);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team4143cttm3().activate();\n" +
			    "        new T4143cttm3().test(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T4143cttm3.java",
			    "\n" +
			    "public class T4143cttm3 {\n" +
			    "    public void test(String s) {\n" +
			    "        // nop\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team4143cttm3/R.java",
			    "\n" +
			    "team package Team4143cttm3;\n" +
			    "protected class R playedBy T4143cttm3 {\n" +
			    "    k <- after test;\n" +
			    "}\n" +
			    "    \n"
            },
            "OK",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // an after callin binding refers to a package-private method of a super team
    // https://bugs.eclipse.org/397867 - [compiler] illegal access to invisible method via callin binding not detected
    public void test4143_callinToTeamMethod4() {
       runNegativeTest(
            new String[] {
		"p2/Team4143cttm4_2.java",
			    "package p2;\n" +
			    "import base p0.T4143cttm4;\n" +
			    "public team class Team4143cttm4_2 extends p1.Team4143cttm4_1 {\n" +
			    "    protected class R playedBy T4143cttm4 {\n" +
			    "        void k(String s) <- after void test()\n" +
			    "            with { s <- \"K\" }\n" +
			    "    }\n" +
			    "}\n",
		"p1/Team4143cttm4_1.java",
			    "package p1;\n" +
			    "public team class Team4143cttm4_1 {\n" +
			    "    void k(String s) {\n" +
			    "        System.out.print(s);\n" +
			    "    }\n" +
			    "}\n",
		"p0/T4143cttm4.java",
			    "package p0;\n" +
			    "public class T4143cttm4 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n"
            },
            "----------\n" + 
    		"1. ERROR in p2\\Team4143cttm4_2.java (at line 5)\n" + 
    		"	void k(String s) <- after void test()\n" + 
    		"	     ^\n" + 
    		"The method k(String) from the type Team4143cttm4_1 is not visible\n" + 
    		"----------\n");
    }

    // a before callin binding refers to a package-private method of a super team
    // https://bugs.eclipse.org/411625 - [compiler] unreportable compile error in the context of bug 397867
    public void test4143_callinToTeamMethod5() {
       runNegativeTest(
            new String[] {
		"p2/Team4143cttm5_2.java",
			    "package p2;\n" +
			    "import base p0.T4143cttm5;\n" +
			    "public team class Team4143cttm5_2 extends p1.Team4143cttm5_1 {\n" +
			    "    protected class R playedBy T4143cttm5 {\n" +
			    "        k <- before test;\n" +
			    "    }\n" +
			    "}\n",
		"p1/Team4143cttm5_1.java",
			    "package p1;\n" +
			    "public team class Team4143cttm5_1 {\n" +
			    "    void k() {\n" +
			    "        System.out.println();\n" +
			    "    }\n" +
			    "}\n",
		"p0/T4143cttm5.java",
			    "package p0;\n" +
			    "public class T4143cttm5 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n"
            },
            "----------\n" + 
    		"1. ERROR in p2\\Team4143cttm5_2.java (at line 5)\n" + 
    		"	k <- before test;\n" + 
    		"	^\n" + 
    		"The method k() from the type Team4143cttm5_1 is not visible\n" + 
    		"----------\n");
    }

    // implicitly inherited role method requires weakening
    public void test4144_callinToInherited1() {
    	runConformTest(
    		new String[] {
    	"Team4144cti1_2.java",
    		"public team class Team4144cti1_2 extends Team4144cti1_1 {\n" +
    		"	@Override\n" +
    		"	protected class R1 playedBy T4144cti1_1 {\n" +
    		"		test <- after test\n;" +
    		"	}\n" +
    		"	@Override\n" +
    		"	protected class R2 playedBy T4144cti1_2 {}\n" +
    		"	public static void main(String... args) {\n" +
    		"		new Team4144cti1_2().activate();\n" +
    		"		new T4144cti1_1().test(new T4144cti1_2());\n" +
    		"	}\n" +
    		"}\n",
    	"Team4144cti1_1.java",
	    	"public team class  Team4144cti1_1 {\n" +
			"	protected class R1 {\n" +
			"		void test(R2 other) {\n" +
			"			other.print();\n" +
			"		}\n" +
			"	}\n" +
			"	protected class R2 {\n" +
			"		protected void print() {\n" +
			"			System.out.print(\"K\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		"T4144cti1_1.java",
			"public class T4144cti1_1 {\n" +
			"	public void test(T4144cti1_2 other) {\n" +
			"		System.out.print(\"O\");\n" +
			"	}\n" +
			"}\n",
		"T4144cti1_2.java",
			"public class T4144cti1_2 {}\n"
    		},
    		"OK");
    }
    
    // Bug 318309 -  [compiler] warnings re unused exceptions from generated code
    // a role method does not declare the expection that it base method may throw
    public void test4145_baseMethodWithException1() {
        Map customOptions = getCompilerOptions();
        customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownException, CompilerOptions.ERROR);
        customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable, CompilerOptions.DISABLED);

    	runConformTest(
    		new String[] {
    	"Team4145bmwe1.java",
        			"public team class Team4145bmwe1 {\n" +
        			"	protected class R playedBy T4145bmwe1 {\n" +
        			"		@SuppressWarnings(\"basecall\")\n" +
        			"		callin void foo() {\n" +
        			"			System.out.print(\"OK\");\n" +
        			"		}\n" +
        			"		foo <- replace bar;\n" +
        			"	}\n" +
        			"	public static void main(String... args) throws Exception {\n" +
        			"		new Team4145bmwe1().activate();\n" +
        			"		new T4145bmwe1().bar();\n" +
        			"	}\n" +
        			"}\n",
    	"T4145bmwe1.java",
    			"public class T4145bmwe1 {\n" +
    			"	void bar() throws Exception { throw new Exception(\"Not Implemented\"); }\n" +
    			"}\n"
    		},
            "OK",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    public void test4146_callinToConstructor1() {
    	runConformTest(
    		new String[] {
    			"Team4146ctc1.java",
    			"public team class Team4146ctc1 {\n" +
    			"	protected class R playedBy T4146ctc1 {\n" +
    			"		void print() { System.out.print('K'); }\n" +
    			"		void print() <- after T4146ctc1();\n" +
    			"	}\n" +
    			"	public static void main(String... args) {\n" +
    			"		new Team4146ctc1().activate();\n" +
    			"		new T4146ctc1().test();\n" +
    			"	}\n" +
    			"}\n",
    			"T4146ctc1.java",
    			"public class T4146ctc1 {\n" +
    			"	T4146ctc1() { System.out.print('O'); }\n" +
    			"	public void test() { System.out.print('!'); }\n" +
    			"}\n"
    		},
    		"OK!");
    }

    // before-ctor callin is illegal
    public void test4146_callinToConstructor2() {
    	runNegativeTest(
    		new String[] {
    			"Team4146ctc1.java",
    			"public team class Team4146ctc1 {\n" +
    			"	protected class R playedBy T4146ctc1 {\n" +
    			"		void print() { System.out.print('K'); }\n" +
    			"		print <- before T4146ctc1;\n" +
    			"	}\n" +
    			"	public static void main(String... args) {\n" +
    			"		new Team4146ctc1().activate();\n" +
    			"		new T4146ctc1().test();\n" +
    			"	}\n" +
    			"}\n",
    			"T4146ctc1.java",
    			"public class T4146ctc1 {\n" +
    			"	T4146ctc1() { System.out.print('O'); }\n" +
    			"	public void test() { System.out.print('!'); }\n" +
    			"}\n"
    		},
    		"----------\n" + 
			"1. ERROR in Team4146ctc1.java (at line 4)\n" + 
			"	print <- before T4146ctc1;\n" + 
			"	                ^^^^^^^^^\n" + 
			"Callin binding to constructor \'T4146ctc1()\' must use the callin modifier \"after\" (OTJLD 4.1(i)). \n" + 
			"----------\n");
    }

    // passing ctor arguments into the callin
    public void test4146_callinToConstructor3() {
    	runConformTest(
    		new String[] {
    			"Team4146ctc1.java",
    			"public team class Team4146ctc1 {\n" +
    			"	protected class R playedBy T4146ctc1 {\n" +
    			"		void print(String prefix, int n) {\n" +
    			"			for (int i=0; i<n; i++)\n" +
    			"				System.out.print(prefix);\n" +
    			"		}" +
    			"		print <- after T4146ctc1;\n" +
    			"	}\n" +
    			"	public static void main(String... args) {\n" +
    			"		new Team4146ctc1().activate();\n" +
    			"		new T4146ctc1(\"_\", 3).test();\n" +
    			"	}\n" +
    			"}\n",
    			"T4146ctc1.java",
    			"public class T4146ctc1 {\n" +
    			"	T4146ctc1(String prefix, int n) { System.out.print('O'); }\n" +
    			"	public void test() { System.out.print('!'); }\n" +
    			"}\n"
    		},
    		"O___!");
    }

    // ctor with early return
    public void test4146_callinToConstructor4() {
    	runConformTest(
    		new String[] {
    			"Team4146ctc1.java",
    			"public team class Team4146ctc1 {\n" +
    			"	protected class R playedBy T4146ctc1 {\n" +
    			"		void print(String prefix, int n) {\n" +
    			"			for (int i=0; i<n; i++)\n" +
    			"				System.out.print(prefix);\n" +
    			"		}" +
    			"		print <- after T4146ctc1;\n" +
    			"	}\n" +
    			"	public static void main(String... args) {\n" +
    			"		new Team4146ctc1().activate();\n" +
    			"		new T4146ctc1(\"_\", 3).test();\n" +
    			"	}\n" +
    			"}\n",
    			"T4146ctc1.java",
    			"public class T4146ctc1 {\n" +
    			"	T4146ctc1(String prefix, int n) {\n" +
    			"		if (n == 3) {\n" +
    			"			System.out.print('O');\n" +
    			"			return;\n" +
    			"		}\n" +
    			"		System.out.print(\"NotOK\");\n" +
    			"	}\n" +
    			"	public void test() { System.out.print('!'); }\n" +
    			"}\n"
    		},
    		"O___!");
    }

    // ==== from binding-of-abstract-and-concrete-methods: ====


    // a callin binding binds to an abstract method though the team is not marked abstract
    // 7.2.4-otjld-callin-bound-abstract-method-1
    public void test724_callinBoundAbstractMethod1() {
        runNegativeTestMatching(
            new String[] {
		"Team724cbam1.java",
			    "\n" +
			    "public team class Team724cbam1 {\n" +
			    "    public abstract class Role724cbam1 playedBy T724cbam1 {\n" +
			    "        abstract callin String test();\n" +
			    "        test <- replace toString;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T724cbam1.java",
			    "\n" +
			    "public class T724cbam1 {}\n" +
			    "    \n"
            },
            "2.5(b)");
    }

    // a callin binding binds to an abstract method though the team is not marked abstract
    // 7.2.4-otjld-callin-bound-abstract-method-2
    public void test724_callinBoundAbstractMethod2() {
        runNegativeTest(
            new String[] {
		"Team724cbam2_2.java",
			    "\n" +
			    "public team class Team724cbam2_2 extends Team724cbam2_1 {\n" +
			    "    @Override\n" +
			    "    public class Role724cbam2 playedBy T724cbam2 {\n" +
			    "        test <- replace toString;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T724cbam2.java",
			    "\n" +
			    "public class T724cbam2 {}\n" +
			    "    \n",
		"Team724cbam2_1.java",
			    "\n" +
			    "public abstract team class Team724cbam2_1 {\n" +
			    "    public abstract class Role724cbam2 {\n" +
			    "        abstract callin String test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team724cbam2_2.java (at line 4)\n" + 
    		"	public class Role724cbam2 playedBy T724cbam2 {\n" + 
    		"	             ^^^^^^^^^^^^\n" + 
    		"The abstract method test in type Role724cbam2 can only be defined by an abstract class\n" + 
    		"----------\n" +
    		"2. ERROR in Team724cbam2_2.java (at line 4)\n" +
    		"	public class Role724cbam2 playedBy T724cbam2 {\n" +
    		"	             ^^^^^^^^^^^^\n" +
    		"The type Team724cbam2_2.Role724cbam2 must implement the inherited abstract method Team724cbam2_2.Role724cbam2.test()\n" + 
    		"----------\n");
    }

    // a callin binding binds to an abstract method where the explicit superclass has a concrete implementation, and the team is not marked abstract
    // 7.2.5-otjld-callin-bound-abstract-method-1
    public void test725_callinBoundAbstractMethod1() {
        runNegativeTest(
            new String[] {
		"Team725cbam1_2.java",
			    "\n" +
			    "public team class Team725cbam1_2 extends Team725cbam1_1 {\n" +
			    "    public class Role725cbam1_1 playedBy T725cbam1 {\n" +
			    "        callin void test() {\n" +
			    "            base.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    @Override\n" +
			    "    public class Role725cbam1_2 extends Role725cbam1_1 playedBy T725cbam1 {\n" +
			    "        test <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T725cbam1.java",
			    "\n" +
			    "public class T725cbam1 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team725cbam1_1.java",
			    "\n" +
			    "public abstract team class Team725cbam1_1 {\n" +
			    "    public abstract class Role725cbam1_2 {\n" +
			    "        abstract callin void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team725cbam1_2.java (at line 10)\n" + 
    		"	public class Role725cbam1_2 extends Role725cbam1_1 playedBy T725cbam1 {\n" + 
    		"	             ^^^^^^^^^^^^^^\n" + 
    		"The abstract method test in type Role725cbam1_2 can only be defined by an abstract class\n" + 
    		"----------\n" +
    		"2. ERROR in Team725cbam1_2.java (at line 10)\n" + 
    		"	public class Role725cbam1_2 extends Role725cbam1_1 playedBy T725cbam1 {\n" + 
    		"	             ^^^^^^^^^^^^^^\n" + 
    		"The type Team725cbam1_2.Role725cbam1_2 must implement the inherited abstract method Team725cbam1_2.Role725cbam1_2.test()\n" + 
    		"----------\n");
    }

    // same structure as above but without abstractness - witness for incompatible return of inherited base call surrogate
    // 7.2.5-otjld-callin-bound-abstract-method-1a
    public void test725_callinBoundAbstractMethod1a() {
        runConformTest(
            new String[] {
		"T725cbam1a.java",
			    "\n" +
			    "public class T725cbam1a {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team725cbam1a_1.java",
			    "\n" +
			    "public team class Team725cbam1a_1 {\n" +
			    "    public class Role725cbam1a_2 {\n" +
			    "        callin void test() { base.test(); }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team725cbam1a_2.java",
			    "\n" +
			    "public team class Team725cbam1a_2 extends Team725cbam1a_1 {\n" +
			    "    public class Role725cbam1a_1 playedBy T725cbam1a {\n" +
			    "        callin void test() {\n" +
			    "            base.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role725cbam1a_2 extends Role725cbam1a_1 playedBy T725cbam1a {\n" +
			    "        test <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            });
    }

    // a callin binding binds to an abstract method where the explicit superclass has a concrete implementation, and the team is not marked abstract
    // 7.2.5-otjld-callin-bound-abstract-method-2
    public void test725_callinBoundAbstractMethod2() {
        runNegativeTestMatching(
            new String[] {
		"Team725cbam2_2.java",
			    "\n" +
			    "public team class Team725cbam2_2 extends Team725cbam2_1 {\n" +
			    "    @Override\n" +
			    "    public class Role725cbam2_2 extends Role725cbam2_1 {}\n" +
			    "}\n" +
			    "    \n",
		"T725cbam2.java",
			    "\n" +
			    "public class T725cbam2 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team725cbam2_1.java",
			    "\n" +
			    "public abstract team class Team725cbam2_1 {\n" +
			    "    public class Role725cbam2_1 playedBy T725cbam2 {\n" +
			    "        callin void test() {\n" +
			    "            base.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public abstract class Role725cbam2_2 playedBy T725cbam2 {\n" +
			    "        abstract callin void test();\n" +
			    "        test <- replace test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team725cbam2_2.java (at line 4)\n" + 
    		"	public class Role725cbam2_2 extends Role725cbam2_1 {}\n" + 
    		"	             ^^^^^^^^^^^^^^\n" + 
    		"The abstract method test in type Role725cbam2_2 can only be defined by an abstract class\n" + 
    		"----------\n");
    }
    // Bug 337413 - [otjld][compiler] consider changing LiftingFailedException to a checked exception
    // need to declare LiftingFailedException but luckily the program still works :)
    public void test726_callinWithHiddenLiftingProblem1() {
        Map options = getCompilerOptions();
        options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
        options.put(CompilerOptions.OPTION_ReportUnsafeRoleInstantiation, CompilerOptions.IGNORE); // see new R2(b);
    	runConformTest(
    		new String[] {
    	"Team726cwhlp1.java",
    			"@SuppressWarnings(\"ambiguousbinding\")\n" +
    			"public team class Team726cwhlp1 {\n" +
    			"   protected abstract class R0 playedBy T726cwhlp1 {\n" +
    			"		@SuppressWarnings(\"hidden-lifting-problem\")\n" +
    			"		bar <- after foo;\n" +
    			"		abstract void bar();\n" +
    			"	}\n" +
    			"	protected class R1 extends R0 {\n" +
    			"		void bar() { System.out.print(\"NOTOK\"); }\n" +
    			"	}\n" +
    			"	protected class R2 extends R0 {\n" +
    			"		void bar() {\n" +
    			"			System.out.print(\"OK\");\n" +
    			"		}\n" +
    			"	}\n" +
    			"   void doLift(T726cwhlp1 as R2 r) throws org.objectteams.LiftingFailedException {}\n" +
    			"	public static void main(String[] args) throws org.objectteams.LiftingFailedException {\n" +
    			"		Team726cwhlp1 t = new Team726cwhlp1();\n" +
    			"		t.activate();\n" +
    			"		T726cwhlp1 b = new T726cwhlp1();\n" +
    			"		t.doLift(b);\n" +
    			"		b.foo();\n" +
    			"	}\n" +
    			"}\n",
    	"T726cwhlp1.java",
    			"public class T726cwhlp1 {\n" +
    			"	public void foo() { }\n" +
    			"}\n"
    		},
    		"OK",
            null/*classLibs*/,
            true/*shouldFlush*/,
            null/*vmArguments*/,
            options,
            null/*requester*/);
    }
    // Bug 337413 - [otjld][compiler] consider changing LiftingFailedException to a checked exception
    // callin doesn't fire due to LiftingFailedException behind the scenes (call target lifting)
    public void test726_callinWithHiddenLiftingProblem2() {
        Map options = getCompilerOptions();
        options.put(CompilerOptions.OPTION_ReportHiddenLiftingProblem, CompilerOptions.WARNING);
    	runConformTest(
    		new String[] {
    	"Team726cwhlp2.java",
    			"@SuppressWarnings(\"ambiguousbinding\")\n" +
    			"public team class Team726cwhlp2 {\n" +
    			"   protected class R0 playedBy T726cwhlp2 {\n" +
    			"		@SuppressWarnings(\"hidden-lifting-problem\")\n" +
    			"		bar <- after foo;\n" +
    			"		void bar() {\n" +
    			"			System.out.print(\"OK\");\n" +
    			"		}\n" +
    			"	}\n" +
    			"	protected class R1 extends R0 {}\n" +
    			"	protected class R2 extends R0 {}\n" +
    			"	public static void main(String[] args) {\n" +
    			"		Team726cwhlp2 t = new Team726cwhlp2();\n" +
    			"		t.activate();\n" +
    			"		T726cwhlp2 b = new T726cwhlp2();\n" +
    			"		b.foo();\n" +
    			"	}\n" +
    			"}\n",
    	"T726cwhlp2.java",
    			"public class T726cwhlp2 {\n" +
    			"	public void foo() { }\n" +
    			"}\n"
    		},
    		"",
            null/*classLibs*/,
            true/*shouldFlush*/,
            null/*vmArguments*/,
            options,
            null/*requester*/);
    }
    // Bug 337413 - [otjld][compiler] consider changing LiftingFailedException to a checked exception
    // callin doesn't fire due to LiftingFailedException behind the scenes (arg lifting in after-callin)
    public void test726_callinWithHiddenLiftingProblem3() {
        Map options = getCompilerOptions();
        options.put(CompilerOptions.OPTION_ReportHiddenLiftingProblem, CompilerOptions.WARNING);
    	runConformTest(
    		new String[] {
    	"Team726cwhlp3.java",
    			"@SuppressWarnings(\"ambiguousbinding\")\n" +
    			"public team class Team726cwhlp3 {\n" +
    			"	protected class ROK playedBy T726cwhlp3 {\n" +
    			"		@SuppressWarnings(\"hidden-lifting-problem\")\n" +
    			"		bar <- after foo;\n" + // argument lifting going bad
    			"		void bar(R0 r) {\n" +
    			"			System.out.print(\"OK\");\n" +
    			"		}\n" +
    			"   }\n" +
    			"   protected class R0 playedBy T726cwhlp3 {}\n" +
    			"	protected class R1 extends R0 {}\n" +
    			"	protected class R2 extends R0 {}\n" +
    			"	public static void main(String[] args) {\n" +
    			"		Team726cwhlp3 t = new Team726cwhlp3();\n" +
    			"		t.activate();\n" +
    			"		T726cwhlp3 b = new T726cwhlp3();\n" +
    			"		b.foo(b);\n" +
    			"	}\n" +
    			"}\n",
    	"T726cwhlp3.java",
    			"public class T726cwhlp3 {\n" +
    			"	public void foo(T726cwhlp3 b) { }\n" +
    			"}\n"
    		},
    		"",
            null/*classLibs*/,
            true/*shouldFlush*/,
            null/*vmArguments*/,
            options,
            null/*requester*/);
    }
    // Bug 337413 - [otjld][compiler] consider changing LiftingFailedException to a checked exception
    // callin doesn't fire due to LiftingFailedException behind the scenes (arg lifting in replace-callin)
    public void test726_callinWithHiddenLiftingProblem4() {
        Map options = getCompilerOptions();
        options.put(CompilerOptions.OPTION_ReportHiddenLiftingProblem, CompilerOptions.ERROR);
        options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
    	runConformTest(
    		new String[] {
    	"Team726cwhlp4.java",
    			"@SuppressWarnings(\"ambiguousbinding\")\n" +
    			"public team class Team726cwhlp4 {\n" +
    			"	protected class ROK playedBy T726cwhlp4 {\n" +
    			"       mycallin:\n" +
    			"		@SuppressWarnings(\"hidden-lifting-problem\")\n" +
    			"		bar <- replace foo;\n" + // argument lifting going bad
    			"       @SuppressWarnings(\"basecall\")\n" +
    			"		callin void bar(R0 r) {\n" +
    			"			System.out.print(\"NOK\");\n" +
    			"		}\n" +
    			"   }\n" +
    			"   protected class R0 playedBy T726cwhlp4 {}\n" +
    			"	protected class R1 extends R0 {}\n" +
    			"	protected class R2 extends R0 {}\n" +
    			"	public static void main(String[] args) {\n" +
    			"		Team726cwhlp4 t = new Team726cwhlp4();\n" +
    			"		t.activate();\n" +
    			"		T726cwhlp4 b = new T726cwhlp4();\n" +
    			"		b.foo(b);\n" +
    			"	}\n" +
    			"}\n",
    	"T726cwhlp4.java",
    			"public class T726cwhlp4 {\n" +
    			"	public void foo(T726cwhlp4 b) { System.out.print(\"OK\"); }\n" +
    			"}\n"
    		},
    		"OK",
            null/*classLibs*/,
            true/*shouldFlush*/,
            null/*vmArguments*/,
            options,
            null/*requester*/);
    }
    
    // Bug 387996 - "T cannot be resolved or is not a field" error due to field named like package
    public void testBug387996a() {
    	runConformTest(
    		new String[] {
    			"test/T1.java",
    				"package test;\n" +
    				"public team class T1 {\n" +
    				"    private Object test;  // = name of package\n" +
    				"    protected class R playedBy B base when (true) {\n" +
    				"        void bar() {}\n" +
    				"        bar <- after foo;  // <- warning!\n" +
    				"    }\n" +
    				"}",
    			"test/B.java",
    				"package test;\n" +
    				"public class B {    \n" +
    				"    public void foo() {};\n" +
    				"}"
    		});
    }
}
