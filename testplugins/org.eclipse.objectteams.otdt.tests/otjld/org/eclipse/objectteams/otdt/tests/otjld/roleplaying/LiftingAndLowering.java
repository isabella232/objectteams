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
package org.eclipse.objectteams.otdt.tests.otjld.roleplaying;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

@SuppressWarnings("unchecked")
public class LiftingAndLowering extends AbstractOTJLDTest {
	
	public LiftingAndLowering(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test2232_liftingToAbstractRole1"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return LiftingAndLowering.class;
	}

    // a role is lowered to the base class that it is played by
    // 2.2.1-otjld-lowering-to-baseclass-1
    public void test221_loweringToBaseclass1() {
       
       runConformTest(
            new String[] {
		"T221ltb1Main.java",
			    "\n" +
			    "public class T221ltb1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team221ltb1 t = new Team221ltb1();\n" +
			    "        T221ltb1    o = new T221ltb1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb1.java",
			    "\n" +
			    "public class T221ltb1 {\n" +
			    "    String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team221ltb1.java",
			    "\n" +
			    "public team class Team221ltb1 {\n" +
			    "\n" +
			    "    public class Role221ltb1 playedBy T221ltb1 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValue() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T221ltb1 as Role221ltb1 obj) {\n" +
			    "        T221ltb1 o = obj;\n" +
			    "\n" +
			    "        return o.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is lowered to the base class that it is played by
    // 2.2.1-otjld-lowering-to-baseclass-2
    public void test221_loweringToBaseclass2() {
       
       runConformTest(
            new String[] {
		"T221ltb2Main.java",
			    "\n" +
			    "public class T221ltb2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team221ltb2 t = new Team221ltb2();\n" +
			    "        T221ltb2    o = new T221ltb2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb2.java",
			    "\n" +
			    "public class T221ltb2 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team221ltb2.java",
			    "\n" +
			    "public team class Team221ltb2 {\n" +
			    "\n" +
			    "    public class Role221ltb2 playedBy T221ltb2 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValue() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T221ltb2 as Role221ltb2 obj) {\n" +
			    "        final T221ltb2 o = obj;\n" +
			    "\n" +
			    "        return o.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is lowered to the base class that it is played by
    // 2.2.1-otjld-lowering-to-baseclass-3
    public void test221_loweringToBaseclass3() {
       
       runConformTest(
            new String[] {
		"T221ltb3Main.java",
			    "\n" +
			    "public class T221ltb3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team221ltb3 t = new Team221ltb3();\n" +
			    "        T221ltb3    o = new T221ltb3();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb3.java",
			    "\n" +
			    "public class T221ltb3 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team221ltb3.java",
			    "\n" +
			    "public team class Team221ltb3 {\n" +
			    "\n" +
			    "    public class Role221ltb3 playedBy T221ltb3 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValue() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T221ltb3 as Role221ltb3 obj) {\n" +
			    "        return getValueInternal(obj);\n" +
			    "    }\n" +
			    "\n" +
			    "    private String getValueInternal(T221ltb3 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is lowered to the base class that it is played by
    // 2.2.1-otjld-lowering-to-baseclass-4
    public void test221_loweringToBaseclass4() {
       
       runConformTest(
            new String[] {
		"T221ltb4Main.java",
			    "\n" +
			    "public class T221ltb4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team221ltb4_2 t = new Team221ltb4_2();\n" +
			    "        T221ltb4      o = new T221ltb4();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb4.java",
			    "\n" +
			    "public class T221ltb4 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team221ltb4_1.java",
			    "\n" +
			    "public team class Team221ltb4_1 {\n" +
			    "\n" +
			    "    public class Role221ltb4 playedBy T221ltb4 {}\n" +
			    "\n" +
			    "    public String getValue(T221ltb4 as Role221ltb4 obj) {\n" +
			    "        return getValueInternal(obj);\n" +
			    "    }\n" +
			    "\n" +
			    "    private String getValueInternal(final T221ltb4 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team221ltb4_2.java",
			    "\n" +
			    "public team class Team221ltb4_2 extends Team221ltb4_1 {\n" +
			    "\n" +
			    "    public class Role221ltb4 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValue() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is lowered via a field to the base class of its explicit superrole
    // 2.2.1-otjld-lowering-to-baseclass-5
    public void test221_loweringToBaseclass5() {
       
       runConformTest(
            new String[] {
		"T221ltb5Main.java",
			    "\n" +
			    "public class T221ltb5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team221ltb5 t = new Team221ltb5();\n" +
			    "        T221ltb5    o = new T221ltb5();\n" +
			    "\n" +
			    "        t.setValue(o);\n" +
			    "        System.out.print(t.getAttr());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb5.java",
			    "\n" +
			    "public class T221ltb5 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team221ltb5.java",
			    "\n" +
			    "public team class Team221ltb5 {\n" +
			    "    private T221ltb5 attr;\n" +
			    "\n" +
			    "    public class Role221ltb5_1 playedBy T221ltb5 {}\n" +
			    "\n" +
			    "    public class Role221ltb5_2 extends Role221ltb5_1 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValue() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public void setValue(T221ltb5 as Role221ltb5_2 obj) {\n" +
			    "        attr = obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getAttr() {\n" +
			    "        return attr.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is lowered in a return statement to the base class that it is played by
    // 2.2.1-otjld-lowering-to-baseclass-6
    public void test221_loweringToBaseclass6() {
       
       runConformTest(
            new String[] {
		"T221ltb6Main.java",
			    "\n" +
			    "public class T221ltb6Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team221ltb6 t = new Team221ltb6();\n" +
			    "        T221ltb6_2  o = new T221ltb6_2();\n" +
			    "\n" +
			    "        System.out.print(t.getBase(o).getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb6_1.java",
			    "\n" +
			    "public class T221ltb6_1 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb6_2.java",
			    "\n" +
			    "public class T221ltb6_2 extends T221ltb6_1 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team221ltb6.java",
			    "\n" +
			    "public team class Team221ltb6 {\n" +
			    "    public class Role221ltb6 playedBy T221ltb6_1 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValue() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public T221ltb6_1 getBase(T221ltb6_2 as Role221ltb6 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is lowered in a return statement to the base class that it is played by - call returns null!
    // 2.2.1-otjld-lowering-to-baseclass-7
    public void test221_loweringToBaseclass7() {
       
       runConformTest(
            new String[] {
		"T221ltb7Main.java",
			    "\n" +
			    "public class T221ltb7Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team221ltb7 t = new Team221ltb7();\n" +
			    "        if (t.getBase() == null)\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        else \n" +
			    "            System.out.print(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb7_1.java",
			    "\n" +
			    "public class T221ltb7_1 {}\n" +
			    "    \n",
		"Team221ltb7.java",
			    "\n" +
			    "public team class Team221ltb7 {\n" +
			    "    public class Role221ltb7 playedBy T221ltb7_1 {}\n" +
			    "    \n" +
			    "    private Role221ltb7 theRole = null;\n" +
			    "    public T221ltb7_1 getBase() {\n" +
			    "        return theRole;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is lowered in a return statement to the base class that it is played by - call returns null!
    // 2.2.1-otjld-lowering-to-baseclass-8
    public void test221_loweringToBaseclass8() {
       
       runConformTest(
            new String[] {
		"T221ltb8Main.java",
			    "\n" +
			    "public class T221ltb8Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team221ltb8 t = new Team221ltb8();\n" +
			    "        if (t.getR().getBase() == null)\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        else \n" +
			    "            System.out.print(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb8_1.java",
			    "\n" +
			    "public class T221ltb8_1 {}\n" +
			    "    \n",
		"Team221ltb8.java",
			    "\n" +
			    "public team class Team221ltb8 {\n" +
			    "    public class Role221ltb8 playedBy T221ltb8_1 {\n" +
			    "        private Role221ltb8 theRole = null;\n" +
			    "        public T221ltb8_1 getBase() {\n" +
			    "            return theRole;\n" +
			    "        }\n" +
			    "    }    \n" +
			    "    public Role221ltb8 getR() { \n" +
			    "        return new Role221ltb8(new T221ltb8_1());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is lowered in a return statement to the base class that it is played by - call returns null!
    // 2.2.1-otjld-lowering-to-baseclass-9
    public void test221_loweringToBaseclass9() {
       
       runConformTest(
            new String[] {
		"T221ltb9Main.java",
			    "\n" +
			    "public class T221ltb9Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team221ltb9 t = new Team221ltb9();\n" +
			    "        if (t.getR().getBases() == null)\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        else \n" +
			    "            System.out.print(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb9_1.java",
			    "\n" +
			    "public class T221ltb9_1 {}\n" +
			    "    \n",
		"Team221ltb9.java",
			    "\n" +
			    "public team class Team221ltb9 {\n" +
			    "    public class Role221ltb9 playedBy T221ltb9_1 {\n" +
			    "        private Role221ltb9[] theRoles = null;\n" +
			    "        public T221ltb9_1[] getBases() {\n" +
			    "            return theRoles;\n" +
			    "        }\n" +
			    "    }    \n" +
			    "    public Role221ltb9 getR() { \n" +
			    "        return new Role221ltb9(new T221ltb9_1());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is lowered in a return statement to the base class that it is played by - call returns null!
    // 2.2.1-otjld-lowering-to-baseclass-10
    public void test221_loweringToBaseclass10() {
       
       runConformTest(
            new String[] {
		"T221ltb10Main.java",
			    "\n" +
			    "public class T221ltb10Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team221ltb10 t = new Team221ltb10();\n" +
			    "        if (t.getR().getBases()[0] == null)\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        else \n" +
			    "            System.out.print(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb10_1.java",
			    "\n" +
			    "public class T221ltb10_1 {}\n" +
			    "    \n",
		"Team221ltb10.java",
			    "\n" +
			    "public team class Team221ltb10 {\n" +
			    "    public class Role221ltb10 playedBy T221ltb10_1 {\n" +
			    "        private Role221ltb10[] theRoles = new Role221ltb10[]{null};\n" +
			    "        public T221ltb10_1[] getBases() {\n" +
			    "            return theRoles;\n" +
			    "        }\n" +
			    "    }    \n" +
			    "    public Role221ltb10 getR() { \n" +
			    "        return new Role221ltb10(new T221ltb10_1());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is lowered in a return statement to the base class that it is played by - bound super-role exists
    // 2.2.1-otjld-lowering-to-baseclass-11
    public void test221_loweringToBaseclass11() {
       
       runConformTest(
            new String[] {
		"T221ltb11Main.java",
			    "\n" +
			    "public class T221ltb11Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team221ltb11 t = new Team221ltb11();\n" +
			    "        T221ltb11_2  o = new T221ltb11_2();\n" +
			    "\n" +
			    "        System.out.print(t.getBase(o).getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb11_1.java",
			    "\n" +
			    "public class T221ltb11_1 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb11_2.java",
			    "\n" +
			    "public class T221ltb11_2 extends T221ltb11_1 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team221ltb11.java",
			    "\n" +
			    "public team class Team221ltb11 {\n" +
			    "    public class Role221ltb11_1 playedBy T221ltb11_1 {\n" +
			    "    }\n" +
			    "    public class Role221ltb11_2 extends Role221ltb11_1 playedBy T221ltb11_2 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValue() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class Role221ltb11_3  {\n" +
			    "        protected T221ltb11_2 getBase(Role221ltb11_2 role) {\n" +
			    "            return role;\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public T221ltb11_1 getBase(T221ltb11_2 as Role221ltb11_2 obj) {\n" +
			    "        Role221ltb11_3 r = new Role221ltb11_3();\n" +
			    "        return r.getBase(obj);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // the enclosing nested team is lowered
    // 2.2.1-otjld-lowering-to-baseclass-12
    public void test221_loweringToBaseclass12() {
       
       runConformTest(
            new String[] {
		"Team221ltb12.java",
			    "\n" +
			    "public team class Team221ltb12 {\n" +
			    "    protected team class Mid playedBy T221ltb12 {\n" +
			    "        protected class Inner {\n" +
			    "            protected void test() {\n" +
			    "                doTest(Mid.this);\n" +
			    "            }\n" +
			    "            void doTest(T221ltb12 t) {\n" +
			    "                t.print();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            new Inner().test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new Mid(new T221ltb12()).test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team221ltb12().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb12.java",
			    "\n" +
			    "public class T221ltb12 {\n" +
			    "    void print() { System.out.print(\"OK\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // the enclosing nested team is lowered - via assignment
    // 2.2.1-otjld-lowering-to-baseclass-13
    public void test221_loweringToBaseclass13() {
       
       runConformTest(
            new String[] {
		"Team221ltb13.java",
			    "\n" +
			    "public team class Team221ltb13 {\n" +
			    "    protected team class Mid playedBy T221ltb13 {\n" +
			    "        protected class Inner {\n" +
			    "            protected void test() {\n" +
			    "                T221ltb13 that = Mid.this;\n" +
			    "                doTest(that);\n" +
			    "            }\n" +
			    "            void doTest(T221ltb13 t) {\n" +
			    "                t.print();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            new Inner().test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new Mid(new T221ltb13()).test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team221ltb13().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T221ltb13.java",
			    "\n" +
			    "public class T221ltb13 {\n" +
			    "    void print() { System.out.print(\"OK\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }
    
    // a foreach loop over an iterable needs to lower elements
    public void test221_loweringToBaseclass14() {
    	runConformTest(
    		new String[] {
    	"Team221ltb14.java",
    			"import java.util.*;\n" +
    			"public team class Team221ltb14 {\n" +
    			"	protected class R playedBy T221ltb14 {\n" +
    			"		protected R() { base(); }\n" +
    			"	}\n" +
    			"	void test() {\n" +
    			"		List<R> roles = new ArrayList<R>();\n" +
    			"		roles.add(new R());\n" +
    			"		for (T221ltb14 b : roles)\n" +
    			"			b.foo();\n" +
    			"	}\n" +
    			"	public static void main(String... args) {\n" +
    			"		new Team221ltb14().test();\n" +
    			"	}\n" +
    			"}\n",
    	"T221ltb14.java",
    			"public class T221ltb14 {\n" +
    			"	void foo() {\n" +
    			"		System.out.println(\"OK\");\n" +
    			"	}\n" +
    			"}\n",
    		},
    		"OK");
    }
    
    // a foreach loop over an array needs to lower elements
    public void test221_loweringToBaseclass15() {
    	runConformTest(
    		new String[] {
    	"Team221ltb15.java",
    			"import java.util.*;\n" +
    			"public team class Team221ltb15 {\n" +
    			"	protected class R playedBy T221ltb15 {\n" +
    			"		protected R() { base(); }\n" +
    			"	}\n" +
    			"	void test() {\n" +
    			"		R[] roles = new R[] {new R()};\n" +
    			"		for (T221ltb15 b : roles)\n" +
    			"			b.foo();\n" +
    			"	}\n" +
    			"	public static void main(String... args) {\n" +
    			"		new Team221ltb15().test();\n" +
    			"	}\n" +
    			"}\n",
    	"T221ltb15.java",
    			"public class T221ltb15 {\n" +
    			"	void foo() {\n" +
    			"		System.out.println(\"OK\");\n" +
    			"	}\n" +
    			"}\n",
    		},
    		"OK");
    }
    
    // a foreach loop over an iterable needs to lower elements (role-of-role)
    public void test221_loweringToBaseclass16() {
    	runConformTest(
    		new String[] {
    	"Team221ltb16_2.java",
    			"import java.util.*;\n" +
    			"public team class Team221ltb16_2 {\n" +
    			"	final Team221ltb16_1 t = new Team221ltb16_1();\n" +
    			"	protected class R playedBy R1<@t> {\n" +
    			"		protected R() { base(); }\n" +
    			"	}\n" +
    			"	void test1() {\n" +
    			"		R r = new R();\n" +
    			"		test2(r);\n" +
    			"	}\n" +
    			"	void test2(R1<@t> b) {\n" +
    			"		b.foo();\n" +
    			"	}\n" +
    			"	public static void main(String... args) {\n" +
    			"		new Team221ltb16_2().test1();\n" +
    			"	}\n" +
    			"}\n",
    	"Team221ltb16_1.java",
    			"public team class Team221ltb16_1 {\n" +
    			"	public class R1 {\n" +
    			"		public void foo() {\n" +
    			"			System.out.println(\"OK\");\n" +
    			"		}\n" +
    			"	}\n" +
    			"}\n",
    		},
    		"OK");
    }
    
    // Bug 335628 - [compiler] lowering in array initializer
    public void test221_loweringToBaseclass17 () {
    	runConformTest(
    		new String[] {
    	"Team221ltb17.java",
    			"public team class Team221ltb17 {\n" + 
    			"	protected class R playedBy T221ltb17 {\n" + 
    			"		protected R() { base(); }\n" + 
    			"	}\n" + 
    			"	void test() {\n" + 
    			"		R r = new R();\n" + 
    			"		T221ltb17[] bases = new T221ltb17[] { r };\n" + 
    			"		for (int i = 0; i < bases.length; i++) {\n" + 
    			"			bases[i].print();\n" + 
    			"		}\n" + 
    			"	}\n" + 
    			"	public static void main(String[] args) {\n" + 
    			"		new Team221ltb17().test();\n" + 
    			"	}\n" + 
    			"}\n",
    	"T221ltb17.java",
		    	"public class T221ltb17 {\n" + 
		    	"	public void print() {\n" + 
		    	"		System.out.println(\"OK\");\n" + 
		    	"	}\n" +
		    	"}\n"
    		},
    		"OK");
    }
    
    // Bug 335628 - [compiler] lowering in array initializer
    public void test221_loweringToBaseclass18 () {
    	runConformTest(
    		new String[] {
    	"Team221ltb18.java",
    			"public team class Team221ltb18 {\n" + 
    			"	protected class R playedBy T221ltb18 {\n" + 
    			"		protected R() { base(); }\n" + 
    			"	}\n" + 
    			"	void test() {\n" + 
    			"		R r = new R();\n" + 
    			"		T221ltb18[][] bases = new T221ltb18[][] { { r } };\n" + 
    			"		for (int i = 0; i < bases.length; i++)\n" + 
    			"			for (int k = 0; k < bases[i].length; k++)\n" + 
    			"				bases[i][k].print();\n" + 
    			"	}\n" + 
    			"	public static void main(String[] args) {\n" + 
    			"		new Team221ltb18().test();\n" + 
    			"	}\n" + 
    			"}\n",
    	"T221ltb18.java",
		    	"public class T221ltb18 {\n" + 
		    	"	public void print() {\n" + 
		    	"		System.out.println(\"OK\");\n" + 
		    	"	}\n" +
		    	"}\n"
    		},
    		"OK");
    }

    // Bug 355253 - warnings regarding synthetic variables _OT$unlowerd$123
    // syntactically known to be non-null
    public void test221_loweringToBaseclass19 () {
    	Map options = getCompilerOptions();
    	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
    	runConformTest(
    		true /* flushOutputDir*/,
    		new String[] {
    	"Team221ltb19.java",
    			"public team class Team221ltb19 {\n" + 
    			"	protected class R playedBy T221ltb19 {\n" + 
    			"		protected R() { base(); }\n" +
    			"       protected void test() {\n" +
    			"           T221ltb19.accept(this);\n" +
    			"       }\n" + 
    			"	}\n" + 
    			"	T221ltb19 aBase;\n" + 
    			"	void test() {\n" + 
    			"		new R().test();\n" + 
    			"	}\n" + 
    			"	public static void main(String[] args) {\n" + 
    			"		new Team221ltb19().test();\n" + 
    			"	}\n" + 
    			"}\n",
    	"T221ltb19.java",
		    	"public class T221ltb19 {\n" + 
		    	"	public static void accept(T221ltb19 inst) {\n" + 
		    	"		System.out.println(\"OK\");\n" + 
		    	"	}\n" +
		    	"}\n"
    		},
    		null, // libs
    		options,
    		"", // compiler log
    		"OK",
    		"",
    		JavacTestOptions.EclipseJustification.EclipseWarningConfiguredAsError);
    }

    // Bug 355253 - warnings regarding synthetic variables _OT$unlowerd$123
    // known to be non-null by analysis
    public void test221_loweringToBaseclass20 () {
    	Map options = getCompilerOptions();
    	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
    	runConformTest(
    		true /* flushOutputDir*/,
    		new String[] {
    	"Team221ltb20.java",
    			"public team class Team221ltb20 {\n" + 
    			"	protected class R playedBy T221ltb20 {\n" + 
    			"		protected R() { base(); }\n" +
    			"       protected void test() {\n" +
    			"           R r = this;" +
    			"           T221ltb20.accept(r);\n" +
    			"       }\n" + 
    			"	}\n" + 
    			"	T221ltb20 aBase;\n" + 
    			"	void test() {\n" + 
    			"		new R().test();\n" + 
    			"	}\n" + 
    			"	public static void main(String[] args) {\n" + 
    			"		new Team221ltb20().test();\n" + 
    			"	}\n" + 
    			"}\n",
    	"T221ltb20.java",
		    	"public class T221ltb20 {\n" + 
		    	"	public static void accept(T221ltb20 inst) {\n" + 
		    	"		System.out.println(\"OK\");\n" + 
		    	"	}\n" +
		    	"}\n"
    		},
    		null, // libs
    		options,
    		"", // compiler log
    		"OK",
    		"",
    		JavacTestOptions.EclipseJustification.EclipseWarningConfiguredAsError);
    }

    // a role is lowered to a class that is not its base class
    // 2.2.2-otjld-lowering-to-non-baseclass-1
    public void test222_loweringToNonBaseclass1() {
        runNegativeTest(
            new String[] {
		"T222ltnb1_1.java",
			    "\n" +
			    "public class T222ltnb1_1 {}\n" +
			    "    \n",
		"T222ltnb1_2.java",
			    "\n" +
			    "public class T222ltnb1_2 {}\n" +
			    "    \n",
		"Team222ltnb1.java",
			    "\n" +
			    "public team class Team222ltnb1 {\n" +
			    "\n" +
			    "    public class Role222ltnb1 playedBy T222ltnb1_1 {}\n" +
			    "\n" +
			    "    public String getValue(T222ltnb1_1 as Role222ltnb1 obj) {\n" +
			    "        T222ltnb1_2 o = obj;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role is lowered to a class that is a subclass of its base class
    // 2.2.2-otjld-lowering-to-non-baseclass-2
    public void test222_loweringToNonBaseclass2() {
        runNegativeTest(
            new String[] {
		"T222ltnb2_1.java",
			    "\n" +
			    "public class T222ltnb2_1 {}\n" +
			    "    \n",
		"T222ltnb2_2.java",
			    "\n" +
			    "public class T222ltnb2_2 extends T222ltnb2_1 {}\n" +
			    "    \n",
		"Team222ltnb2.java",
			    "\n" +
			    "public team class Team222ltnb2 {\n" +
			    "\n" +
			    "    public class Role222ltnb2 playedBy T222ltnb2_1 {}\n" +
			    "\n" +
			    "    public T222ltnb2_2 getBase(T222ltnb2_1 as Role222ltnb2 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // lowering of a role object for the same base object produces the same base class object
    // 2.2.3-otjld-lowering-produces-same-object-1
    public void test223_loweringProducesSameObject1() {
       
       runConformTest(
            new String[] {
		"T223lpso1Main.java",
			    "\n" +
			    "public class T223lpso1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team223lpso1 t = new Team223lpso1();\n" +
			    "        T223lpso1_2  o = new T223lpso1_2();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T223lpso1_1.java",
			    "\n" +
			    "public interface T223lpso1_1 {}\n" +
			    "    \n",
		"T223lpso1_2.java",
			    "\n" +
			    "public class T223lpso1_2 implements T223lpso1_1 {}\n" +
			    "    \n",
		"Team223lpso1.java",
			    "\n" +
			    "public team class Team223lpso1 {\n" +
			    "\n" +
			    "    public class Role223lpso1 playedBy T223lpso1_2 {}\n" +
			    "\n" +
			    "    public String test(T223lpso1_2 as Role223lpso1 obj) {\n" +
			    "        T223lpso1_1 loweredA = obj;\n" +
			    "        T223lpso1_2 loweredB = obj;\n" +
			    "\n" +
			    "        if ((loweredA == loweredB) &&\n" +
			    "            loweredA.equals(loweredB) &&\n" +
			    "            (loweredA.hashCode() == loweredB.hashCode())) {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // lowering of a role object for the same base object produces the same base class object
    // 2.2.3-otjld-lowering-produces-same-object-2
    public void test223_loweringProducesSameObject2() {
       
       runConformTest(
            new String[] {
		"T223lpso2Main.java",
			    "\n" +
			    "public class T223lpso2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team223lpso2 t = new Team223lpso2();\n" +
			    "        T223lpso2_2  o = new T223lpso2_2();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T223lpso2_1.java",
			    "\n" +
			    "public class T223lpso2_1 {}\n" +
			    "    \n",
		"T223lpso2_2.java",
			    "\n" +
			    "public class T223lpso2_2 extends T223lpso2_1 {}\n" +
			    "    \n",
		"Team223lpso2.java",
			    "\n" +
			    "public team class Team223lpso2 {\n" +
			    "\n" +
			    "    public class Role223lpso2_1 playedBy T223lpso2_1 {}\n" +
			    "    public class Role223lpso2_2 extends Role223lpso2_1 playedBy T223lpso2_2 {}\n" +
			    "\n" +
			    "    private T223lpso2_1 liftAndLower1(T223lpso2_1 as Role223lpso2_1 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    private T223lpso2_1 liftAndLower2(T223lpso2_2 as Role223lpso2_2 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T223lpso2_2 obj) {\n" +
			    "        T223lpso2_1 loweredA = liftAndLower1(obj);\n" +
			    "        T223lpso2_1 loweredB = liftAndLower2(obj);\n" +
			    "\n" +
			    "        if ((loweredA == loweredB) &&\n" +
			    "            loweredA.equals(loweredB) &&\n" +
			    "            (loweredA.hashCode() == loweredB.hashCode())) {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role cannot be compared to its lowered self
    // 2.2.4-otjld-role-not-equal-to-baseobject-1
    public void test224_roleNotEqualToBaseobject1() {
        runNegativeTest(
            new String[] {
		"T224rnetb1.java",
			    "\n" +
			    "public class T224rnetb1 {}\n" +
			    "    \n",
		"Team224rnetb1.java",
			    "\n" +
			    "public team class Team224rnetb1 {\n" +
			    "\n" +
			    "    public class Role224rnetb1 playedBy T224rnetb1 {}\n" +
			    "\n" +
			    "    public boolean test(T224rnetb1 as Role224rnetb1 obj) {\n" +
			    "        T224rnetb1 lowered = obj;\n" +
			    "\n" +
			    "        return lowered == obj;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role cannot be compared to its lowered self (instanceof)
    // 2.2.4-otjld-role-not-equal-to-baseobject-1a
    public void test224_roleNotEqualToBaseobject1a() {
        runNegativeTest(
            new String[] {
		"T224rnetb1a.java",
			    "\n" +
			    "public class T224rnetb1a {}\n" +
			    "    \n",
		"Team224rnetb1a.java",
			    "\n" +
			    "public team class Team224rnetb1a {\n" +
			    "\n" +
			    "    public class Role224rnetb1a playedBy T224rnetb1a {}\n" +
			    "\n" +
			    "    public boolean test(T224rnetb1a as Role224rnetb1a obj) {\n" +
			    "        return obj instanceof T224rnetb1a;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role cannot be casted to its lowered self
    // 2.2.4-otjld-role-not-equal-to-baseobject-1b
    public void test224_roleNotEqualToBaseobject1b() {
        runNegativeTest(
            new String[] {
		"T224rnetb1b.java",
			    "\n" +
			    "public class T224rnetb1b {}\n" +
			    "    \n",
		"Team224rnetb1b.java",
			    "\n" +
			    "public team class Team224rnetb1b {\n" +
			    "\n" +
			    "    public class Role224rnetb1b playedBy T224rnetb1b {}\n" +
			    "\n" +
			    "    public T224rnetb1b test(T224rnetb1b as Role224rnetb1b obj) {\n" +
			    "         return (T224rnetb1b)obj;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role is not equal to its lowered self
    // 2.2.4-otjld-role-not-equal-to-baseobject-2
    public void test224_roleNotEqualToBaseobject2() {
       
       runConformTest(
            new String[] {
		"T224rnetb2Main.java",
			    "\n" +
			    "public class T224rnetb2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team224rnetb2 t = new Team224rnetb2();\n" +
			    "        T224rnetb2    o = new T224rnetb2();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T224rnetb2.java",
			    "\n" +
			    "public class T224rnetb2 {}\n" +
			    "    \n",
		"Team224rnetb2.java",
			    "\n" +
			    "public team class Team224rnetb2 {\n" +
			    "\n" +
			    "    public class Role224rnetb2 playedBy T224rnetb2 {}\n" +
			    "\n" +
			    "    public String test(T224rnetb2 as Role224rnetb2 obj) {\n" +
			    "        T224rnetb2 lowered = obj;\n" +
			    "\n" +
			    "        if (lowered.equals(obj) ||\n" +
			    "            (lowered.hashCode() == obj.hashCode())) {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is not equal to its lowered self
    // 2.2.4-otjld-role-not-equal-to-baseobject-3
    public void test224_roleNotEqualToBaseobject3() {
       
       runConformTest(
            new String[] {
		"T224rnetb3Main.java",
			    "\n" +
			    "public class T224rnetb3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team224rnetb3_2 t = new Team224rnetb3_2();\n" +
			    "        T224rnetb3_2    o = new T224rnetb3_2();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T224rnetb3_1.java",
			    "\n" +
			    "public class T224rnetb3_1 {}\n" +
			    "    \n",
		"T224rnetb3_2.java",
			    "\n" +
			    "public class T224rnetb3_2 extends T224rnetb3_1 {}\n" +
			    "    \n",
		"Team224rnetb3_1.java",
			    "\n" +
			    "public team class Team224rnetb3_1 {\n" +
			    "\n" +
			    "    protected class Role224rnetb3_1 playedBy T224rnetb3_1 {}\n" +
			    "}\n" +
			    "    \n",
		"Team224rnetb3_2.java",
			    "\n" +
			    "public team class Team224rnetb3_2 extends Team224rnetb3_1 {\n" +
			    "\n" +
			    "    public class Role224rnetb3_2 extends Role224rnetb3_1 playedBy T224rnetb3_2 {}\n" +
			    "\n" +
			    "    public String test(T224rnetb3_2 as Role224rnetb3_2 obj) {\n" +
			    "        T224rnetb3_1 lowered = obj;\n" +
			    "\n" +
			    "        if (lowered.equals(obj) ||\n" +
			    "            (lowered.hashCode() == obj.hashCode())) {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // the type of a lowering assignment expression is the base class
    // 2.2.5-otjld-type-of-lowering-assignment-1
    public void test225_typeOfLoweringAssignment1() {
       
       runConformTest(
            new String[] {
		"T225tola1Main.java",
			    "\n" +
			    "public class T225tola1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team225tola1 t = new Team225tola1();\n" +
			    "        T225tola1    o = new T225tola1();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T225tola1.java",
			    "\n" +
			    "public class T225tola1 {}\n" +
			    "    \n",
		"Team225tola1.java",
			    "\n" +
			    "public team class Team225tola1 {\n" +
			    "\n" +
			    "    public class Role225tola1 playedBy T225tola1 {}\n" +
			    "\n" +
			    "    public String test(T225tola1 as Role225tola1 obj) {\n" +
			    "        T225tola1 lowered;\n" +
			    "\n" +
			    "        if ((lowered = obj).getClass() == T225tola1.class) {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // the type of a lowering assignment expression is the base class
    // 2.2.5-otjld-type-of-lowering-assignment-2
    public void test225_typeOfLoweringAssignment2() {
       
       runConformTest(
            new String[] {
		"T225tola2Main.java",
			    "\n" +
			    "public class T225tola2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team225tola2_2 t = new Team225tola2_2();\n" +
			    "        T225tola2_2    o = new T225tola2_2();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T225tola2_1.java",
			    "\n" +
			    "public abstract class T225tola2_1 {}\n" +
			    "    \n",
		"T225tola2_2.java",
			    "\n" +
			    "public class T225tola2_2 extends T225tola2_1 {}\n" +
			    "    \n",
		"Team225tola2_1.java",
			    "\n" +
			    "public team class Team225tola2_1 {\n" +
			    "\n" +
			    "    public class Role225tola2_1 playedBy T225tola2_1 {}\n" +
			    "}\n" +
			    "    \n",
		"Team225tola2_2.java",
			    "\n" +
			    "public team class Team225tola2_2 extends Team225tola2_1 {\n" +
			    "\n" +
			    "    public class Role225tola2_2 extends Role225tola2_1 playedBy T225tola2_2 {}\n" +
			    "\n" +
			    "    public String test(T225tola2_2 as Role225tola2_2 obj) {\n" +
			    "        Role225tola2_1 r = obj;\n" +
			    "        T225tola2_1    lowered;\n" +
			    "\n" +
			    "        if ((lowered = r).getClass() == T225tola2_2.class) {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a base class object is lifted to a role via a method parameter
    // 2.2.6-otjld-lifting-1
    public void test226_lifting1() {
       
       runConformTest(
            new String[] {
		"T226l1Main.java",
			    "\n" +
			    "public class T226l1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team226l1 t = new Team226l1();\n" +
			    "        T226l1    o = new T226l1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T226l1.java",
			    "\n" +
			    "public class T226l1 {\n" +
			    "    public String getValue() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team226l1.java",
			    "\n" +
			    "public team class Team226l1 {\n" +
			    "\n" +
			    "    public class Role226l1 playedBy T226l1 {\n" +
			    "        public abstract String getValue();\n" +
			    "        String getValue() -> String getValue() with {\n" +
			    "            result <- \"OK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T226l1 as Role226l1 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a base class object is lifted to a role via a final method parameter
    // 2.2.6-otjld-lifting-2
    public void test226_lifting2() {
       
       runConformTest(
            new String[] {
		"T226l2Main.java",
			    "\n" +
			    "public class T226l2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team226l2 t = new Team226l2();\n" +
			    "        T226l2_1  o = new T226l2_2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T226l2_1.java",
			    "\n" +
			    "public abstract class T226l2_1 {\n" +
			    "    public abstract String getValue();\n" +
			    "}\n" +
			    "    \n",
		"T226l2_2.java",
			    "\n" +
			    "public class T226l2_2 extends T226l2_1 {\n" +
			    "    public String getValue() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team226l2.java",
			    "\n" +
			    "public team class Team226l2 {\n" +
			    "\n" +
			    "    public class Role226l2_1 playedBy T226l2_1 {\n" +
			    "        public abstract String getValue();\n" +
			    "        String getValue() -> String getValue() with {\n" +
			    "            result <- \"OK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role226l2_2 extends Role226l2_1 {}\n" +
			    "\n" +
			    "    public String getValue(final T226l2_1 as Role226l2_2 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a base class object is lifted to a role via a return statement
    // 2.2.6-otjld-lifting-3
    public void test226_lifting3() {
       
       runConformTest(
            new String[] {
		"T226l3Main.java",
			    "\n" +
			    "public class T226l3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team226l3_2 t = new Team226l3_2();\n" +
			    "        T226l3      o = new T226l3();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T226l3.java",
			    "\n" +
			    "public class T226l3 {\n" +
			    "    public T226l3 getInstance() {\n" +
			    "        return new T226l3();\n" +
			    "    }\n" +
			    "    public String getValue() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team226l3_1.java",
			    "\n" +
			    "public team class Team226l3_1 {\n" +
			    "\n" +
			    "    public class Role226l3 playedBy T226l3 {\n" +
			    "        public abstract String getValue();\n" +
			    "        public abstract Role226l3 getInstance();\n" +
			    "\n" +
			    "        String getValue() -> String getValue() with {\n" +
			    "            result <- \"OK\"\n" +
			    "        }\n" +
			    "        Role226l3 getInstance() -> T226l3 getInstance();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team226l3_2.java",
			    "\n" +
			    "public team class Team226l3_2 extends Team226l3_1 {\n" +
			    "    public String getValue(T226l3 as Role226l3 obj) {\n" +
			    "        return obj.getInstance().getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a base class object is lifted to a role that it does not play
    // 2.2.7-otjld-lifting-to-unplaying-role-1
    public void test227_liftingToUnplayingRole1() {
        runNegativeTest(
            new String[] {
		"T227ltur1_1.java",
			    "\n" +
			    "public class T227ltur1_1 {}\n" +
			    "    \n",
		"T227ltur1_2.java",
			    "\n" +
			    "public class T227ltur1_2 {}\n" +
			    "    \n",
		"Team227ltur1.java",
			    "\n" +
			    "public team class Team227ltur1 {\n" +
			    "\n" +
			    "    public class Role227ltur1 playedBy T227ltur1_1 {}\n" +
			    "\n" +
			    "    public void test (T227ltur1_2 as Role227ltur1 obj) {}\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a base class object is lifted to a role that it does not play
    // 2.2.7-otjld-lifting-to-unplaying-role-2
    public void test227_liftingToUnplayingRole2() {
        runNegativeTest(
            new String[] {
		"T227ltur2_1.java",
			    "\n" +
			    "public class T227ltur2_1 {}\n" +
			    "    \n",
		"T227ltur2_2.java",
			    "\n" +
			    "public class T227ltur2_2 extends T227ltur2_1 {}\n" +
			    "    \n",
		"Team227ltur2.java",
			    "\n" +
			    "public team class Team227ltur2 {\n" +
			    "\n" +
			    "    public class Role227ltur2 playedBy T227ltur2_2 {}\n" +
			    "\n" +
			    "    public void test (T227ltur2_1 as Role227ltur2 obj) {}\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // subsequent lifting of the same base class object produces the same role object
    // 2.2.8-otjld-lifting-produces-same-role-1
    public void test228_liftingProducesSameRole1() {
       
       runConformTest(
            new String[] {
		"T228lpsr1Main.java",
			    "\n" +
			    "public class T228lpsr1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team228lpsr1_2 t = new Team228lpsr1_2();\n" +
			    "        T228lpsr1_2    o = new T228lpsr1_2();\n" +
			    "\n" +
			    "        System.out.print(t.test(o, o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T228lpsr1_1.java",
			    "\n" +
			    "public class T228lpsr1_1 {}\n" +
			    "    \n",
		"T228lpsr1_2.java",
			    "\n" +
			    "public class T228lpsr1_2 extends T228lpsr1_1 {}\n" +
			    "    \n",
		"Team228lpsr1_1.java",
			    "\n" +
			    "public team class Team228lpsr1_1 {\n" +
			    "    public class Role228lpsr1_1 playedBy T228lpsr1_1 {}\n" +
			    "}\n" +
			    "    \n",
		"Team228lpsr1_2.java",
			    "\n" +
			    "public team class Team228lpsr1_2 extends Team228lpsr1_1 {\n" +
			    "\n" +
			    "    public class Role228lpsr1_2 extends Role228lpsr1_1 playedBy T228lpsr1_2 {}\n" +
			    "\n" +
			    "    public String test(T228lpsr1_1 as Role228lpsr1_1 objA, T228lpsr1_2 as Role228lpsr1_2 objB) {\n" +
			    "        if ((objA == objB) &&\n" +
			    "            objA.equals(objB) &&\n" +
			    "            (objA.hashCode() == objB.hashCode())) {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // subsequent lifting of the same base class object produces the same role object
    // 2.2.8-otjld-lifting-produces-same-role-2
    public void test228_liftingProducesSameRole2() {
       
       runConformTest(
            new String[] {
		"T228lpsr2Main.java",
			    "\n" +
			    "public class T228lpsr2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team228lpsr2 t = new Team228lpsr2();\n" +
			    "        T228lpsr2_2  o = new T228lpsr2_2();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T228lpsr2_1.java",
			    "\n" +
			    "public abstract class T228lpsr2_1 {}\n" +
			    "    \n",
		"T228lpsr2_2.java",
			    "\n" +
			    "public class T228lpsr2_2 extends T228lpsr2_1 {\n" +
			    "    protected T228lpsr2_2 self() {\n" +
			    "        return this;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team228lpsr2.java",
			    "\n" +
			    "public team class Team228lpsr2 {\n" +
			    "\n" +
			    "    public class Role228lpsr2_1 playedBy T228lpsr2_1 {}\n" +
			    "    public class Role228lpsr2_2 playedBy T228lpsr2_2 {\n" +
			    "        public abstract Role228lpsr2_1 toRole1();\n" +
			    "        Role228lpsr2_1 toRole1() -> T228lpsr2_2 self();\n" +
			    "    }\n" +
			    "\n" +
			    "    private Role228lpsr2_1 toRole1(T228lpsr2_1 as Role228lpsr2_1 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    private Role228lpsr2_2 toRole2(T228lpsr2_2 as Role228lpsr2_2 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T228lpsr2_2 obj) {\n" +
			    "        Role228lpsr2_1 roleA = toRole1(obj);\n" +
			    "        Role228lpsr2_1 roleB = toRole2(obj).toRole1();\n" +
			    "\n" +
			    "        if ((roleA == roleB) &&\n" +
			    "            roleA.equals(roleB) &&\n" +
			    "            (roleA.hashCode() == roleB.hashCode())) {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // lifting of the same base class object to roles related by implicit inheritance does produce different role objcts (as of their hashCode)
    // 2.2.9-otjld-different-context-lifting-1
    public void test229_differentContextLifting1() {
       
       runConformTest(
            new String[] {
		"T229dcl1Main.java",
			    "\n" +
			    "public class T229dcl1Main {\n" +
			    "    @SuppressWarnings(\"ambiguouslowering\") // equals could eat either: role or base\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team229dcl1_1 t1 = new Team229dcl1_1();\n" +
			    "        final Team229dcl1_2 t2 = new Team229dcl1_2();\n" +
			    "        T229dcl1            o  = new T229dcl1();\n" +
			    "        Role229dcl1<@t1>    r1 = t1.toRole(o);\n" +
			    "        Role229dcl1<@t2>    r2 = t2.toRole(o);\n" +
			    "\n" +
			    "        if (/* statically wrong; (r1 == r2) || */\n" +
			    "            r1.equals(r2) ||\n" +
			    "            (r1.hashCode() == r2.hashCode())) {\n" +
			    "            System.out.print(\"NOTOK\");\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T229dcl1.java",
			    "\n" +
			    "public class T229dcl1 {}\n" +
			    "    \n",
		"Team229dcl1_1.java",
			    "\n" +
			    "public team class Team229dcl1_1 {\n" +
			    "    public class Role229dcl1 playedBy T229dcl1 {}\n" +
			    "\n" +
			    "    public Role229dcl1 toRole(T229dcl1 as Role229dcl1 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team229dcl1_2.java",
			    "\n" +
			    "public team class Team229dcl1_2 extends Team229dcl1_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // lifting of the same base class object to unrelated roles produces different role objcts
    // 2.2.9-otjld-different-context-lifting-2
    public void test229_differentContextLifting2() {
       
       runConformTest(
            new String[] {
		"T229lpsr2Main.java",
			    "\n" +
			    "public class T229lpsr2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team229dcl2 t = new Team229dcl2();\n" +
			    "        T229dcl2    o = new T229dcl2();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T229dcl2.java",
			    "\n" +
			    "public class T229dcl2 {}\n" +
			    "    \n",
		"Team229dcl2.java",
			    "\n" +
			    "public team class Team229dcl2 {\n" +
			    "    public class Role229dcl2_1 playedBy T229dcl2 {}\n" +
			    "    public class Role229dcl2_2 playedBy T229dcl2 {}\n" +
			    "\n" +
			    "    private Role229dcl2_1 toRole1(T229dcl2 as Role229dcl2_1 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    private Role229dcl2_2 toRole2(T229dcl2 as Role229dcl2_2 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T229dcl2 obj) {\n" +
			    "        Role229dcl2_1 r1 = toRole1(obj);\n" +
			    "        Role229dcl2_2 r2 = toRole2(obj);\n" +
			    "\n" +
			    "        if (r1.equals(r2) ||\n" +
			    "            (r1.hashCode() == r2.hashCode())) {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // lifting of the same base class object to related roles produces the same role objct
    // 2.2.9-otjld-different-context-lifting-3
    public void test229_differentContextLifting3() {
       
       runConformTest(
            new String[] {
		"T229lpsr3Main.java",
			    "\n" +
			    "public class T229lpsr3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team229dcl3 t = new Team229dcl3();\n" +
			    "        T229dcl3    o = new T229dcl3();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T229dcl3.java",
			    "\n" +
			    "public class T229dcl3 {}\n" +
			    "    \n",
		"Team229dcl3.java",
			    "\n" +
			    "public team class Team229dcl3 {\n" +
			    "    public class Role229dcl3_1 playedBy T229dcl3 {}\n" +
			    "    public class Role229dcl3_2 extends Role229dcl3_1 {}\n" +
			    "\n" +
			    "    private Role229dcl3_1 toRole1(T229dcl3 as Role229dcl3_1 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    private Role229dcl3_2 toRole2(T229dcl3 as Role229dcl3_2 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T229dcl3 obj) {\n" +
			    "        Role229dcl3_1 r1 = toRole1(obj);\n" +
			    "        Role229dcl3_2 r2 = toRole2(obj);\n" +
			    "\n" +
			    "        if (r1 == r2) {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }


    // compare instances of different teams
    // 2.2.9-otjld-different-context-lifting-5
    public void test229_differentContextLifting5() {
       
       runConformTest(
            new String[] {
		"T229lpsr1Main.java",
			    "\n" +
			    "public class T229lpsr1Main {\n" +
			    "    @SuppressWarnings(\"ambiguouslowering\") // equals could eat either: role or base\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team229dcl5_1 t1 = new Team229dcl5_1();\n" +
			    "        final Team229dcl5_2 t2 = new Team229dcl5_2();\n" +
			    "        T229dcl5            o  = new T229dcl5();\n" +
			    "        Role229dcl5<@t1>    r1 = t1.toRole(o);\n" +
			    "        Role229dcl5<@t2>    r2 = t2.toRole(o);\n" +
			    "\n" +
			    "        if ((r1 == r2) || \n" +
			    "            r1.equals(r2) ||\n" +
			    "            (r1.hashCode() == r2.hashCode())) {\n" +
			    "            System.out.print(\"NOTOK\");\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T229dcl5.java",
			    "\n" +
			    "public class T229dcl5 {}\n" +
			    "    \n",
		"Team229dcl5_1.java",
			    "\n" +
			    "public team class Team229dcl5_1 {\n" +
			    "    public class Role229dcl5 playedBy T229dcl5 {}\n" +
			    "\n" +
			    "    public Role229dcl5 toRole(T229dcl5 as Role229dcl5 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team229dcl5_2.java",
			    "\n" +
			    "public team class Team229dcl5_2 extends Team229dcl5_1 {\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a base object is not equal to its lifted self
    // 2.2.10-otjld-baseobject-not-equal-to-roleobject-1
    public void test2210_baseobjectNotEqualToRoleobject1() {
       
       runConformTest(
            new String[] {
		"T2210bnetr1Main.java",
			    "\n" +
			    "public class T2210bnetr1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2210bnetr1 t = new Team2210bnetr1();\n" +
			    "        T2210bnetr1    o = new T2210bnetr1();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2210bnetr1.java",
			    "\n" +
			    "public class T2210bnetr1 {}\n" +
			    "    \n",
		"Team2210bnetr1.java",
			    "\n" +
			    "public team class Team2210bnetr1 {\n" +
			    "    public class Role2210bnetr1 playedBy T2210bnetr1 {}\n" +
			    "\n" +
			    "    private Role2210bnetr1 toRole(T2210bnetr1 as Role2210bnetr1 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T2210bnetr1 obj) {\n" +
			    "        Role2210bnetr1 r = toRole(obj);\n" +
			    "\n" +
			    "        if (r.equals(obj) ||\n" +
			    "            (r.hashCode() == obj.hashCode())) {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a base object is not equal to its lifted self
    // 2.2.10-otjld-baseobject-not-equal-to-roleobject-2
    public void test2210_baseobjectNotEqualToRoleobject2() {
       
       runConformTest(
            new String[] {
		"T2210bnetr2Main.java",
			    "\n" +
			    "public class T2210bnetr2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2210bnetr2 t = new Team2210bnetr2();\n" +
			    "        T2210bnetr2    o = new T2210bnetr2();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2210bnetr2.java",
			    "\n" +
			    "public class T2210bnetr2 {}\n" +
			    "    \n",
		"Team2210bnetr2.java",
			    "\n" +
			    "public team class Team2210bnetr2 {\n" +
			    "    public class Role2210bnetr2_1 playedBy T2210bnetr2 {}\n" +
			    "    public class Role2210bnetr2_2 extends Role2210bnetr2_1 {}\n" +
			    "\n" +
			    "    private Role2210bnetr2_2 toRole(T2210bnetr2 as Role2210bnetr2_2 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T2210bnetr2 obj) {\n" +
			    "        Role2210bnetr2_2 r = toRole(obj);\n" +
			    "\n" +
			    "        if (r.equals(obj) ||\n" +
			    "            (r.hashCode() == obj.hashCode())) {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a base object is not equal to its lifted self
    // 2.2.10-otjld-baseobject-not-equal-to-roleobject-3
    public void test2210_baseobjectNotEqualToRoleobject3() {
       
       runConformTest(
            new String[] {
		"T2210bnetr3Main.java",
			    "\n" +
			    "public class T2210bnetr3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2210bnetr3_2 t = new Team2210bnetr3_2();\n" +
			    "        T2210bnetr3      o = new T2210bnetr3();\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2210bnetr3.java",
			    "\n" +
			    "public class T2210bnetr3 {}\n" +
			    "    \n",
		"Team2210bnetr3_1.java",
			    "\n" +
			    "public team class Team2210bnetr3_1 {\n" +
			    "    public class Role2210bnetr3 playedBy T2210bnetr3 {}\n" +
			    "}\n" +
			    "    \n",
		"Team2210bnetr3_2.java",
			    "\n" +
			    "public team class Team2210bnetr3_2 extends Team2210bnetr3_1 {\n" +
			    "    private Role2210bnetr3 toRole(T2210bnetr3 as Role2210bnetr3 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T2210bnetr3 obj) {\n" +
			    "        Role2210bnetr3 r = toRole(obj);\n" +
			    "\n" +
			    "        if (r.equals(obj) ||\n" +
			    "            (r.hashCode() == obj.hashCode())) {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a base object is tried to lift via an assignment expression
    // 2.2.11-otjld-lifting-via-assignment-1
    public void test2211_liftingViaAssignment1() {
        runNegativeTest(
            new String[] {
		"T2211lva1.java",
			    "\n" +
			    "public class T2211lva1 {}\n" +
			    "    \n",
		"Team2211lva1.java",
			    "\n" +
			    "public team class Team2211lva1 {\n" +
			    "    public class Role2211lva1 playedBy T2211lva1 {}\n" +
			    "\n" +
			    "    public void test(T2211lva1 obj) {\n" +
			    "        Role2211lva1 r = obj;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a base object is tried to lift via an assignment expression
    // 2.2.11-otjld-lifting-via-assignment-2
    public void test2211_liftingViaAssignment2() {
        runNegativeTest(
            new String[] {
		"T2211lva2.java",
			    "\n" +
			    "public class T2211lva2 {}\n" +
			    "    \n",
		"Team2211lva2.java",
			    "\n" +
			    "public team class Team2211lva2 {\n" +
			    "    public class Role2211lva2_1 playedBy T2211lva2 {}\n" +
			    "    public class Role2211lva2_2 extends Role2211lva2_1 {}\n" +
			    "\n" +
			    "    private Role2211lva2_2 r = new T2211lva2();\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // an array of role objects is lowered to an array of the base class that it is played by
    // 2.2.12-otjld-array-lowering-to-baseclass-1a
    public void test2212_arrayLoweringToBaseclass1a() {
       
       runConformTest(
            new String[] {
		"T2212altb1aMain.java",
			    "\n" +
			    "public class T2212altb1aMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2212altb1a t = new Team2212altb1a();\n" +
			    "        T2212altb1a    o = new T2212altb1a();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(new T2212altb1a[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2212altb1a.java",
			    "\n" +
			    "public class T2212altb1a {\n" +
			    "    public String toString() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2212altb1a.java",
			    "\n" +
			    "public team class Team2212altb1a {\n" +
			    "\n" +
			    "    public class Role2212altb1a playedBy T2212altb1a {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    // alternate (newer) syntax\n" +
			    "    public String getValue(T2212altb1a[] as Role2212altb1a[] objs) {\n" +
			    "        T2212altb1a[] o = objs;\n" +
			    "\n" +
			    "        return o[0].toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of role objects is lowered to an array of the base class that it is played by
    // 2.2.12-otjld-array-lowering-to-baseclass-1
    public void test2212_arrayLoweringToBaseclass1() {
       
       runConformTest(
            new String[] {
		"T2212altb1Main.java",
			    "\n" +
			    "public class T2212altb1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2212altb1 t = new Team2212altb1();\n" +
			    "        T2212altb1    o = new T2212altb1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(new T2212altb1[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2212altb1.java",
			    "\n" +
			    "public class T2212altb1 {\n" +
			    "    public String toString() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2212altb1.java",
			    "\n" +
			    "public team class Team2212altb1 {\n" +
			    "\n" +
			    "    public class Role2212altb1 playedBy T2212altb1 {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T2212altb1 as Role2212altb1 objs[]) {\n" +
			    "        T2212altb1[] o = objs;\n" +
			    "\n" +
			    "        return o[0].toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of role objects is lowered to an array of the base class that it is played by
    // 2.2.12-otjld-array-lowering-to-baseclass-2
    public void test2212_arrayLoweringToBaseclass2() {
       
       runConformTest(
            new String[] {
		"T2212tb2Main.java",
			    "\n" +
			    "public class T2212tb2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2212altb2 t = new Team2212altb2();\n" +
			    "        T2212altb2    o = new T2212altb2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(new T2212altb2[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2212altb2.java",
			    "\n" +
			    "public class T2212altb2 {\n" +
			    "    public String toString() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2212altb2.java",
			    "\n" +
			    "public team class Team2212altb2 {\n" +
			    "\n" +
			    "    public class Role2212altb2 playedBy T2212altb2 {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T2212altb2 as Role2212altb2 objs[]) {\n" +
			    "        final T2212altb2[] o = objs;\n" +
			    "\n" +
			    "        return o[0].toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of role objects is lowered to an array of the base class that it is played by
    // 2.2.12-otjld-array-lowering-to-baseclass-3
    public void test2212_arrayLoweringToBaseclass3() {
       
       runConformTest(
            new String[] {
		"T2212tb3Main.java",
			    "\n" +
			    "public class T2212tb3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2212altb3 t = new Team2212altb3();\n" +
			    "        T2212altb3_2  o = new T2212altb3_2();\n" +
			    "\n" +
			    "        t.setValues(new T2212altb3_2[]{o});\n" +
			    "        System.out.print(t);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2212altb3_1.java",
			    "\n" +
			    "public class T2212altb3_1 {\n" +
			    "    public String toString() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2212altb3_2.java",
			    "\n" +
			    "public class T2212altb3_2 extends T2212altb3_1 {\n" +
			    "    public String toString() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2212altb3.java",
			    "\n" +
			    "public team class Team2212altb3 {\n" +
			    "    private T2212altb3_1[] o;\n" +
			    "\n" +
			    "    public class Role2212altb3 playedBy T2212altb3_2 {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public void setValues(T2212altb3_2 as Role2212altb3 objs[]) {\n" +
			    "        o = objs;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String toString()\n" +
			    "    {\n" +
			    "        return o[0].toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of role objects is lowered to an array of the base class that it is played by
    // 2.2.12-otjld-array-lowering-to-baseclass-4
    public void test2212_arrayLoweringToBaseclass4() {
       
       runConformTest(
            new String[] {
		"T2212tb4Main.java",
			    "\n" +
			    "public class T2212tb4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2212altb4_2 t = new Team2212altb4_2();\n" +
			    "        T2212altb4      o = new T2212altb4();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(new T2212altb4[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2212altb4.java",
			    "\n" +
			    "public class T2212altb4 {\n" +
			    "    public String toString() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2212altb4_1.java",
			    "\n" +
			    "public team class Team2212altb4_1 {\n" +
			    "\n" +
			    "    public class Role2212altb4 playedBy T2212altb4 {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2212altb4_2.java",
			    "\n" +
			    "public team class Team2212altb4_2 extends Team2212altb4_1 {\n" +
			    "\n" +
			    "    public String getValue(T2212altb4 as Role2212altb4 objs[]) {\n" +
			    "        return getValueInternal(objs);\n" +
			    "    }\n" +
			    "\n" +
			    "    private String getValueInternal(T2212altb4[] objs) {\n" +
			    "        return objs[0].toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of role objects is lowered to an array of the base class that it is played by
    // 2.2.12-otjld-array-lowering-to-baseclass-5
    public void test2212_arrayLoweringToBaseclass5() {
       
       runConformTest(
            new String[] {
		"T2212tb5Main.java",
			    "\n" +
			    "public class T2212tb5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2212altb5_2 t = new Team2212altb5_2();\n" +
			    "        T2212altb5      o = new T2212altb5();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(new T2212altb5[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2212altb5.java",
			    "\n" +
			    "public class T2212altb5 {\n" +
			    "    public String toString() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2212altb5_1.java",
			    "\n" +
			    "public team class Team2212altb5_1 {\n" +
			    "\n" +
			    "    public class Role2212altb5_1 playedBy T2212altb5 {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2212altb5_2.java",
			    "\n" +
			    "public team class Team2212altb5_2 extends Team2212altb5_1 {\n" +
			    "\n" +
			    "    public class Role2212altb5_2 extends Role2212altb5_1 {}\n" +
			    "\n" +
			    "    public String getValue(T2212altb5 as Role2212altb5_2 objs[]) {\n" +
			    "        return getValueInternal(objs);\n" +
			    "    }\n" +
			    "\n" +
			    "    private String getValueInternal(final T2212altb5[] objs) {\n" +
			    "        return objs[0].toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of role objects is lowered to an array of the base class that it is played by
    // 2.2.12-otjld-array-lowering-to-baseclass-6
    public void test2212_arrayLoweringToBaseclass6() {
       
       runConformTest(
            new String[] {
		"T2212tb6Main.java",
			    "\n" +
			    "public class T2212tb6Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2212altb6 t = new Team2212altb6();\n" +
			    "        T2212altb6    o = new T2212altb6();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(new T2212altb6[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2212altb6.java",
			    "\n" +
			    "public class T2212altb6 {\n" +
			    "    public String toString() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2212altb6.java",
			    "\n" +
			    "public team class Team2212altb6 {\n" +
			    "\n" +
			    "    public class Role2212altb6 playedBy T2212altb6 {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"NOTOK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T2212altb6 as Role2212altb6 objs[]) {\n" +
			    "        return convert(objs)[0].toString();\n" +
			    "    }\n" +
			    "\n" +
			    "    private T2212altb6[] convert(Role2212altb6[] objs) {\n" +
			    "        return objs;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of role objects is lowered to an array of the base class that it is played by -- base call
    // 2.2.12-otjld-array-lowering-to-baseclass-7
    public void test2212_arrayLoweringToBaseclass7() {
       
       runConformTest(
            new String[] {
		"Team2212altb7.java",
			    "\n" +
			    "public team class Team2212altb7 {\n" +
			    "    protected class R playedBy T2212altb7 {\n" +
			    "        callin void intercept(R[] rs) {\n" +
			    "            rs[0].print();\n" +
			    "            base.intercept(rs);\n" +
			    "        }\n" +
			    "        void intercept(R[] rs) <- replace void test(T2212altb7[] ts) \n" +
			    "            with { rs <- ts }\n" +
			    "        void print() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2212altb7().activate();\n" +
			    "        T2212altb7 b = new T2212altb7();\n" +
			    "        b.test(new T2212altb7[]{b});\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2212altb7.java",
			    "\n" +
			    "public class T2212altb7 {\n" +
			    "    void test(T2212altb7[] ts) {\n" +
			    "        ts[0].print();\n" +
			    "    }\n" +
			    "    void print() {\n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }
    // Bug 329374 -  Implicit Lowering of a Role Array as return results in compiler error
    public void test2212_arrayLoweringToBaseclass8() {
        compileOrder = new String[][] {{"T2212altb8_1.java", "T2212altb8_2.java", "Team2212altb8.java"}, {"T2212altb8Main.java"}};
        runConformTest(
                new String[] {
            "T2212altb8Main.java",
            		"public class T2212altb8Main {\n"+
            		"    public static void main(String[] args){\n"+
            		"        Team2212altb8 t = new Team2212altb8();\n"+
            		"        T2212altb8_2 b = new T2212altb8_2();\n"+
            		"        T2212altb8_1[] as = t.getAs(b);\n"+
            		"    }\n" +
            		"}\n",
            "T2212altb8_1.java",
            		"public class T2212altb8_1 {}\n",
            "T2212altb8_2.java",
                	"public class T2212altb8_2 {}\n",
            "Team2212altb8.java",
            		"public team class Team2212altb8 {\n" +
            		"	 protected class RoleA playedBy T2212altb8_1 {\n" +
            		"    }\n" +
            		"    protected class RoleB playedBy T2212altb8_2 {\n" +
            		"        protected RoleA[] getAs(){\n" +
            		"            return new RoleA[0];\n" +
            		"        }\n" +
            		"    }\n" +
            		"    public RoleA[] getAs(T2212altb8_2 as RoleB rb) {\n" +
            		"        return rb.getAs();\n" +
            		"    }\n" +
            		"}\n"
                });
    }
    
    // lowering of a role object array for the same base objects produces the an equal object array
    // 2.2.14-otjld-array-lowering-produces-equal-object-1
    public void test2214_arrayLoweringProducesEqualObject1() {
       
       runConformTest(
            new String[] {
		"T2214alpeo1Main.java",
			    "\n" +
			    "public class T2214alpeo1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2214alpeo1 t = new Team2214alpeo1();\n" +
			    "        T2214alpeo1    o = new T2214alpeo1();\n" +
			    "\n" +
			    "        System.out.print(t.test(new T2214alpeo1[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2214alpeo1.java",
			    "\n" +
			    "public class T2214alpeo1 {}\n" +
			    "    \n",
		"Team2214alpeo1.java",
			    "\n" +
			    "public team class Team2214alpeo1 {\n" +
			    "\n" +
			    "    public class Role2214alpeo1 playedBy T2214alpeo1 {}\n" +
			    "\n" +
			    "    public String test(T2214alpeo1 as Role2214alpeo1 obj[]) {\n" +
			    "        T2214alpeo1[] loweredA = obj;\n" +
			    "        T2214alpeo1[] loweredB = obj;\n" +
			    "\n" +
			    "        if (loweredA == loweredB) {\n" +
			    "            return \"NOTOK 1\";\n" +
			    "        }\n" +
			    "        else if (!java.util.Arrays.equals(loweredA, loweredB)) {\n" +
			    "            return \"NOTOK 1\";\n" +
			    "        }\n" +
			    "        else if (loweredA.hashCode() == loweredB.hashCode()) {\n" +
			    "            return \"NOTOK 3\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // lowering of a role object array for the same base objects produces an equal object array
    // 2.2.14-otjld-array-lowering-produces-equal-object-2
    public void test2214_arrayLoweringProducesEqualObject2() {
       
       runConformTest(
            new String[] {
		"T2214alpeo2Main.java",
			    "\n" +
			    "public class T2214alpeo2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2214alpeo2_2 t = new Team2214alpeo2_2();\n" +
			    "        T2214alpeo2_2    o = new T2214alpeo2_2();\n" +
			    "\n" +
			    "        System.out.print(t.test(new T2214alpeo2_2[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2214alpeo2_1.java",
			    "\n" +
			    "public class T2214alpeo2_1 {}\n" +
			    "    \n",
		"T2214alpeo2_2.java",
			    "\n" +
			    "public class T2214alpeo2_2 extends T2214alpeo2_1 {}\n" +
			    "    \n",
		"Team2214alpeo2_1.java",
			    "\n" +
			    "public team class Team2214alpeo2_1 {\n" +
			    "\n" +
			    "    public class Role2214alpeo2_1 playedBy T2214alpeo2_1 {}\n" +
			    "}\n" +
			    "    \n",
		"Team2214alpeo2_2.java",
			    "\n" +
			    "public team class Team2214alpeo2_2 extends Team2214alpeo2_1 {\n" +
			    "\n" +
			    "    public class Role2214alpeo2_2 extends Role2214alpeo2_1 playedBy T2214alpeo2_2 {}\n" +
			    "\n" +
			    "    public String test(T2214alpeo2_2 as Role2214alpeo2_2 obj[]) {\n" +
			    "        T2214alpeo2_1[] loweredA = obj;\n" +
			    "        T2214alpeo2_2[] loweredB = obj;\n" +
			    "\n" +
			    "        if (loweredA == loweredB) {\n" +
			    "            return \"NOTOK 1\";\n" +
			    "        }\n" +
			    "        else if (!java.util.Arrays.equals(loweredA, loweredB)) {\n" +
			    "            return \"NOTOK 1\";\n" +
			    "        }\n" +
			    "        else if (loweredA.hashCode() == loweredB.hashCode()) {\n" +
			    "            return \"NOTOK 3\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of base class objects is lifted to a role array
    // 2.2.15-otjld-array-lifting-1
    public void test2215_arrayLifting1() {
       
       runConformTest(
            new String[] {
		"T2215al1Main.java",
			    "\n" +
			    "public class T2215al1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2215al1 t = new Team2215al1();\n" +
			    "        T2215al1    o = new T2215al1();\n" +
			    "\n" +
			    "        System.out.print(t.test(new T2215al1[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2215al1.java",
			    "\n" +
			    "public class T2215al1 {\n" +
			    "    public String toString() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2215al1.java",
			    "\n" +
			    "public team class Team2215al1 {\n" +
			    "\n" +
			    "    public class Role2215al1 playedBy T2215al1 {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"OK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T2215al1 as Role2215al1 obj[]) {\n" +
			    "        return obj[0].toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of base class objects is lifted to a role array
    // 2.2.15-otjld-array-lifting-2
    public void test2215_arrayLifting2() {
       
       runConformTest(
            new String[] {
		"T2215al2Main.java",
			    "\n" +
			    "public class T2215al2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2215al2_2 t = new Team2215al2_2();\n" +
			    "        T2215al2      o = new T2215al2();\n" +
			    "\n" +
			    "        System.out.print(t.test(new T2215al2[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2215al2.java",
			    "\n" +
			    "public class T2215al2 {\n" +
			    "    public String toString() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2215al2_1.java",
			    "\n" +
			    "public team class Team2215al2_1 {\n" +
			    "    public class Role2215al2_1 playedBy T2215al2 {}\n" +
			    "}\n" +
			    "    \n",
		"Team2215al2_2.java",
			    "\n" +
			    "public team class Team2215al2_2 extends Team2215al2_1 {\n" +
			    "\n" +
			    "    public class Role2215al2_2 extends Role2215al2_1 {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"OK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T2215al2 as Role2215al2_2 obj[]) {\n" +
			    "        return obj[0].toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of base class objects is lifted to a role array
    // 2.2.15-otjld-array-lifting-3
    public void test2215_arrayLifting3() {
       
       runConformTest(
            new String[] {
		"T2215al3Main.java",
			    "\n" +
			    "public class T2215al3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2215al3 t = new Team2215al3();\n" +
			    "        T2215al3    o = new T2215al3();\n" +
			    "\n" +
			    "        System.out.print(t.test(new T2215al3[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2215al3.java",
			    "\n" +
			    "public class T2215al3 {\n" +
			    "    public String toString() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2215al3.java",
			    "\n" +
			    "public team class Team2215al3 {\n" +
			    "    public class Role2215al3_1 playedBy T2215al3 {}\n" +
			    "\n" +
			    "    public class Role2215al3_2 playedBy T2215al3 {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"OK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T2215al3 as Role2215al3_1 obj[]) {\n" +
			    "        return getValue(obj);\n" +
			    "    }\n" +
			    "\n" +
			    "    private String getValue(T2215al3 as Role2215al3_2 obj[]) {\n" +
			    "        return obj[0].toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of base class objects (inner class) is lifted to a role array
    // 2.2.15-otjld-array-lifting-4
    public void test2215_arrayLifting4() {
       
       runConformTest(
            new String[] {
		"T2215al4Main.java",
			    "\n" +
			    "public class T2215al4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2215al4 t = new Team2215al4();\n" +
			    "        T2215al4    o = new T2215al4();\n" +
			    "        T2215al4.Inner i= o.new Inner();\n" +
			    "\n" +
			    "        System.out.print(t.test(new T2215al4.Inner[]{i}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2215al4.java",
			    "\n" +
			    "public class T2215al4 {\n" +
			    "    public class Inner  {\n" +
			    "        public String toString() {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2215al4.java",
			    "\n" +
			    "public team class Team2215al4 {\n" +
			    "\n" +
			    "    public class Role2215al4 playedBy T2215al4.Inner {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"OK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T2215al4.Inner as Role2215al4 obj[]) {\n" +
			    "        return obj[0].toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }
    

    // an array of base class objects is lifted to an array of nested roles
    public void test2215_arrayLifting5() {
       
       runConformTest(
            new String[] {
		"T2215al5Main.java",
			    "\n" +
			    "public class T2215al5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team2215al5 t = new Team2215al5();\n" +
			    "        T2215al5    o = new T2215al5();\n" +
			    "        Mid<@t> mid = new Mid<@t>();\n" +
			    "        System.out.print(mid.test(new T2215al5[]{o}));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2215al5.java",
			    "\n" +
			    "public class T2215al5 {\n" +
			    "    public String toString() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2215al5.java",
			    "\n" +
			    "public team class Team2215al5 {\n" +
			    "  public team class Mid {\n" +
			    "    public class Role2215al5 playedBy T2215al5 {\n" +
			    "        public abstract String toString();\n" +
			    "        String toString() -> String toString() with {\n" +
			    "            result <- \"OK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public String test(T2215al5 as Role2215al5 obj[]) {\n" +
			    "        return obj[0].toString();\n" +
			    "    }\n" +
			    "  }\n" +
			    "\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of base class objects is lifted to an array of nested roles - location: callout return
    // witness for broken bytecode: ifc-part of array lifter was not found, but silently generated a problem method for the caller
    public void test2215_arrayLifting6() {
       
       runConformTest(
            new String[] {
		"T2215al6Main.java",
			    "\n" +
			    "public class T2215al6Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team2215al6 t = new Team2215al6();\n" +
			    "        T2215al6_2    o = new T2215al6_2();\n" +
			    "        t.test(o);\n" +
			    "    }\n" +
			    "}\n",
		"T2215al6_1.java",
			    "\n" +
			    "public class T2215al6_1 {\n" +
			    "    @Override\n" +
			    "    public String toString() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n",
		"T2215al6_2.java",
			    "\n" +
			    "public class T2215al6_2 {\n" +
			    "    public T2215al6_1[] getOthers() { return new T2215al6_1[]{ new T2215al6_1()}; }\n" +
			    "}\n",
		"Team2215al6.java",
			    "\n" +
			    "public team class Team2215al6 {\n" +
			    "  public team class Mid playedBy T2215al6_2 {\n" +
			    "    public class Role2215al6 playedBy T2215al6_1 {\n" +
			    "        String toString() => String toString() with {\n" +
			    "            result <- \"OK\"\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Role2215al6[] getOthers() -> T2215al6_1[] getOthers();\n" +
			    "    protected void test() {\n" +
			    "      System.out.print(getOthers()[0].toString());\n" +
			    "    }\n" +
			    "  }\n" +
			    "  public void test(T2215al6_2 as Mid m) {\n" +
			    "    m.test();\n" +
			    "  }\n" +
			    "}\n"
            },
            "OK");
    }


    // an array of base class objects is lifted to an array of nested roles - location: callin return
    public void test2215_arrayLifting7() {
       
       runConformTest(
            new String[] {
		"T2215al7Main.java",
			    "\n" +
			    "public class T2215al7Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team2215al7 t = new Team2215al7();\n" +
			    "        t.activate();\n" +
			    "        T2215al7_2    o = new T2215al7_2();\n" +
			    "        System.out.print(o.getOthers()[0]);\n" +
			    "    }\n" +
			    "}\n",
		"T2215al7_1.java",
			    "\n" +
			    "public class T2215al7_1 {\n" +
			    "    @Override\n" +
			    "    public String toString() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n",
		"T2215al7_2.java",
			    "\n" +
			    "public class T2215al7_2 {\n" +
			    "    public T2215al7_1[] getOthers() { return new T2215al7_1[0]; }\n" +
			    "}\n",
		"Team2215al7.java",
			    "\n" +
			    "public team class Team2215al7 {\n" +
			    "  public team class Mid playedBy T2215al7_2 {\n" +
			    "    public class Role2215al7 playedBy T2215al7_1 {\n" +
			    "        protected Role2215al7() {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Role2215al7[] getOthers() <- replace T2215al7_1[] getOthers();\n" +
			    "    callin Role2215al7[] getOthers() {\n" +
			    "      System.out.print(base.getOthers().length);\n" +
			    "      return new Role2215al7[]{ new Role2215al7() };\n" +
			    "    }\n" +
			    "  }\n" +
			    "}\n"
            },
            "0OK");
    }

    // an array of base class objects is lifted to an array of a role class that is not played by that base class
    // 2.2.16-otjld-array-lifting-to-unrelated-role-1
    public void test2216_arrayLiftingToUnrelatedRole1() {
        runNegativeTest(
            new String[] {
		"T2216altur1.java",
			    "\n" +
			    "public class T2216altur1 {}\n" +
			    "    \n",
		"Team2216altur1.java",
			    "\n" +
			    "public team class Team2216altur1 {\n" +
			    "    public class Role2216altur1 {}\n" +
			    "\n" +
			    "    public void test(T2216altur1 as Role2216altur1 obj[]) {}\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // an array of base class objects is lifted to an array of a role class that is not played by that base class
    // 2.2.16-otjld-array-lifting-to-unrelated-role-2
    public void test2216_arrayLiftingToUnrelatedRole2() {
        runNegativeTest(
            new String[] {
		"p1/T2216altur2.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T2216altur2 {}\n" +
			    "    \n",
		"p1/Team2216altur2_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team2216altur2_1 {\n" +
			    "    // shadows the class within this package\n" +
			    "    protected class T2216altur2 {}\n" +
			    "    public class Role2216altur2 playedBy T2216altur2 {}\n" +
			    "}\n" +
			    "    \n",
		"p2/Team2216altur2_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team2216altur2_2 extends p1.Team2216altur2_1 {\n" +
			    "    public void test(T2216altur2 as Role2216altur2 obj[]) {}\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // the lifting of an array of base objects produces equal role object arrays (but not the same)
    // 2.2.17-otjld-array-lifting-produces-equal-object-1
    public void test2217_arrayLiftingProducesEqualObject1() {
       
       runConformTest(
            new String[] {
		"T2217alpeo1Main.java",
			    "\n" +
			    "public class T2217alpeo1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2217alpeo1 t = new Team2217alpeo1();\n" +
			    "        T2217alpeo1    o = new T2217alpeo1();\n" +
			    "\n" +
			    "        System.out.print(t.test(o, o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2217alpeo1.java",
			    "\n" +
			    "public class T2217alpeo1 {\n" +
			    "    public T2217alpeo1[] getObjects() {\n" +
			    "        return new T2217alpeo1[]{this};\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2217alpeo1.java",
			    "\n" +
			    "public team class Team2217alpeo1 {\n" +
			    "    public class Role2217alpeo1_1 playedBy T2217alpeo1 {}\n" +
			    "\n" +
			    "    public class Role2217alpeo1_2 playedBy T2217alpeo1 {\n" +
			    "        public abstract Role2217alpeo1_1[] getObjects();\n" +
			    "        Role2217alpeo1_1[] getObjects() -> T2217alpeo1[] getObjects();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test(T2217alpeo1 as Role2217alpeo1_2 objA, T2217alpeo1 as Role2217alpeo1_2 objB) {\n" +
			    "        Role2217alpeo1_1[] liftedA = objA.getObjects();\n" +
			    "        Role2217alpeo1_1[] liftedB = objB.getObjects();\n" +
			    "\n" +
			    "        if (liftedA == liftedB) {\n" +
			    "            return \"NOTOK 1\";\n" +
			    "        }\n" +
			    "        else if (!liftedA.equals(liftedB)) {\n" +
			    "	    if (liftedA.length != liftedB.length)\n" +
			    "	    	return \"NOTOK 2a\";\n" +
			    "	    for (int i=0; i<liftedA.length; i++){\n" +
			    "	    	if (liftedA[i] != liftedB[i])\n" +
			    "            		return \"NOTOK 2b\";\n" +
			    "	    }\n" +
			    "	    return \"OK\";\n" +
			    "        }\n" +
			    "        else if (liftedA.hashCode() != liftedB.hashCode()) {\n" +
			    "            return \"NOTOK 3\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // the lifting of an array of base objects produces equal role object arrays (but not the same)
    // 2.2.17-otjld-array-lifting-produces-equal-object-2
    public void test2217_arrayLiftingProducesEqualObject2() {
       
       runConformTest(
            new String[] {
		"T2217alpeo2Main.java",
			    "\n" +
			    "public class T2217alpeo2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2217alpeo2  t = new Team2217alpeo2();\n" +
			    "        T2217alpeo2_2[] o = { new T2217alpeo2_2() };\n" +
			    "\n" +
			    "        System.out.print(t.test(o, o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2217alpeo2_1.java",
			    "\n" +
			    "public class T2217alpeo2_1 {}\n" +
			    "    \n",
		"T2217alpeo2_2.java",
			    "\n" +
			    "public class T2217alpeo2_2 extends T2217alpeo2_1 {}\n" +
			    "    \n",
		"Team2217alpeo2.java",
			    "\n" +
			    "public team class Team2217alpeo2 {\n" +
			    "    public class Role2217alpeo2_1 playedBy T2217alpeo2_1 {}\n" +
			    "\n" +
			    "    public class Role2217alpeo2_2 extends Role2217alpeo2_1 {}\n" +
			    "\n" +
			    "    public String test(T2217alpeo2_1 as Role2217alpeo2_2 objA[], T2217alpeo2_2 as Role2217alpeo2_2 objB[]) {\n" +
			    "\n" +
			    "        if (objA == objB) {\n" +
			    "            return \"NOTOK 1\";\n" +
			    "        }\n" +
			    "        else if (!objA.equals(objB)) {\n" +
			    "	    if (objA.length != objB.length)\n" +
			    "	    	return \"NOTOK 2a\";\n" +
			    "	    for (int i=0; i<objA.length; i++){\n" +
			    "	    	if (objA[i] != objB[i])\n" +
			    "            		return \"NOTOK 2b\";\n" +
			    "	    }\n" +
			    "	    if (java.util.Arrays.equals(objA, objB))\n" +
			    "	    	return \"OK\";\n" +
			    "	    return \"NOTOK 2c\";\n" +
			    "        }\n" +
			    "        else if (objA.hashCode() != objB.hashCode()) {\n" +
			    "            return \"NOTOK 3\";\n" +
			    "        }\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"NOTOK 4\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of base objects is not equal to its lifted self
    // 2.2.18-otjld-basearray-not-equal-to-rolearray-1
    public void test2218_basearrayNotEqualToRolearray1() {
       
       runConformTest(
            new String[] {
		"T2218bnetr1Main.java",
			    "\n" +
			    "public class T2218bnetr1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2218bnetr1 t = new Team2218bnetr1();\n" +
			    "\n" +
			    "        System.out.print(t.test());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2218bnetr1.java",
			    "\n" +
			    "public class T2218bnetr1 {}\n" +
			    "    \n",
		"Team2218bnetr1.java",
			    "\n" +
			    "public team class Team2218bnetr1 {\n" +
			    "    public class Role2218bnetr1_1 playedBy T2218bnetr1 {}\n" +
			    "\n" +
			    "    public class Role2218bnetr1_2 extends Role2218bnetr1_1 {}\n" +
			    "\n" +
			    "    private Role2218bnetr1_2[] convert(T2218bnetr1 as Role2218bnetr1_2 obj[]) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test() {\n" +
			    "        T2218bnetr1[]      obj    = { new T2218bnetr1() };\n" +
			    "        Role2218bnetr1_2[] lifted = convert(obj);\n" +
			    "\n" +
			    "        if (obj.equals(lifted)) {\n" +
			    "            return \"NOTOK 1\";\n" +
			    "        }\n" +
			    "        else if (obj.hashCode() == lifted.hashCode()) {\n" +
			    "            return \"NOTOK 2\";\n" +
			    "        }\n" +
			    "	else if (java.util.Arrays.equals(obj, lifted)) {\n" +
			    "	    return \"NOTOK 3\";\n" +
			    "	}\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of base objects is not equal to its lifted self
    // 2.2.18-otjld-basearray-not-equal-to-rolearray-2
    public void test2218_basearrayNotEqualToRolearray2() {
       
       runConformTest(
            new String[] {
		"T2218bnetr2Main.java",
			    "\n" +
			    "public class T2218bnetr2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2218bnetr2 t = new Team2218bnetr2();\n" +
			    "\n" +
			    "        System.out.print(t.test());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2218bnetr2_1.java",
			    "\n" +
			    "public class T2218bnetr2_1 {}\n" +
			    "    \n",
		"T2218bnetr2_2.java",
			    "\n" +
			    "public class T2218bnetr2_2 extends T2218bnetr2_1 {}\n" +
			    "    \n",
		"Team2218bnetr2.java",
			    "\n" +
			    "public team class Team2218bnetr2 {\n" +
			    "    public class Role2218bnetr2_1 playedBy T2218bnetr2_1 {}\n" +
			    "\n" +
			    "    public class Role2218bnetr2_2 playedBy T2218bnetr2_2 {}\n" +
			    "\n" +
			    "    private Role2218bnetr2_1[] toRole1(T2218bnetr2_1 as Role2218bnetr2_1 obj[]) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    private Role2218bnetr2_2[] toRole2(T2218bnetr2_2 as Role2218bnetr2_2 obj[]) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test() {\n" +
			    "        T2218bnetr2_1[]    obj    = toRole2(new T2218bnetr2_2[]{ new T2218bnetr2_2() });\n" +
			    "        Role2218bnetr2_1[] lifted = toRole1(obj);\n" +
			    "\n" +
			    "        if (obj.equals(lifted)) {\n" +
			    "            return \"NOTOK 1\";\n" +
			    "        }\n" +
			    "        else if (obj.hashCode() == lifted.hashCode()) {\n" +
			    "            return \"NOTOK 2\";\n" +
			    "        }\n" +
			    "	else if (java.util.Arrays.equals(obj, lifted)) {\n" +
			    "	    return \"NOTOK 3\";\n" +
			    "	}\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of role objects is not equal to its lowered self
    // 2.2.19-otjld-rolearray-equal-to-lowered-1
    public void test2219_rolearrayEqualToLowered1() {
       
       runConformTest(
            new String[] {
		"T2219bnetr1Main.java",
			    "\n" +
			    "public class T2219bnetr1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2219bnetr1 t = new Team2219bnetr1();\n" +
			    "\n" +
			    "        System.out.print(t.test());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2219bnetr1.java",
			    "\n" +
			    "public class T2219bnetr1 {}\n" +
			    "    \n",
		"Team2219bnetr1.java",
			    "\n" +
			    "public team class Team2219bnetr1 {\n" +
			    "    public class Role2219bnetr1 playedBy T2219bnetr1 {}\n" +
			    "\n" +
			    "    private Role2219bnetr1[] toRole(T2219bnetr1 as Role2219bnetr1 obj[]) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test() {\n" +
			    "        Role2219bnetr1[] obj     = toRole(new T2219bnetr1[]{ new T2219bnetr1() });\n" +
			    "        T2219bnetr1[]    lowered = obj;\n" +
			    "\n" +
			    "        if (obj.equals(lowered)) {\n" +
			    "            return \"NOTOK 1\";\n" +
			    "        }\n" +
			    "        else if (obj.hashCode() == lowered.hashCode()) {\n" +
			    "            return \"NOTOK 2\";\n" +
			    "        }\n" +
			    "	else if (java.util.Arrays.equals(obj, lowered)) {\n" +
			    "	    return \"NOTOK 3\";\n" +
			    "	}\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an array of role objects is not equal to its lowered self
    // 2.2.19-otjld-rolearray-equal-to-lowered-2
    public void test2219_rolearrayEqualToLowered2() {
       
       runConformTest(
            new String[] {
		"T2219bnetr2Main.java",
			    "\n" +
			    "public class T2219bnetr2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2219bnetr2_2 t = new Team2219bnetr2_2();\n" +
			    "\n" +
			    "        System.out.print(t.test());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2219bnetr2_1.java",
			    "\n" +
			    "public class T2219bnetr2_1 {}\n" +
			    "    \n",
		"T2219bnetr2_2.java",
			    "\n" +
			    "public class T2219bnetr2_2 extends T2219bnetr2_1 {}\n" +
			    "    \n",
		"Team2219bnetr2_1.java",
			    "\n" +
			    "public team class Team2219bnetr2_1 {\n" +
			    "    public class Role2219bnetr2 playedBy T2219bnetr2_2 {}\n" +
			    "}\n" +
			    "    \n",
		"Team2219bnetr2_2.java",
			    "\n" +
			    "public team class Team2219bnetr2_2 extends Team2219bnetr2_1 {\n" +
			    "    private Role2219bnetr2[] obj;\n" +
			    "\n" +
			    "    private void init(T2219bnetr2_2 as Role2219bnetr2 obj[]) {\n" +
			    "        this.obj = obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String test() {\n" +
			    "        init(new T2219bnetr2_2[]{ new T2219bnetr2_2() });\n" +
			    "\n" +
			    "        T2219bnetr2_1[] lowered = obj;\n" +
			    "\n" +
			    "        if (obj.equals(lowered)) {\n" +
			    "            return \"NOTOK 1\";\n" +
			    "        }\n" +
			    "        else if (obj.hashCode() == lowered.hashCode()) {\n" +
			    "            return \"NOTOK 2\";\n" +
			    "        }\n" +
			    "	else if (java.util.Arrays.equals(obj, lowered)) {\n" +
			    "	    return \"NOTOK 3\";\n" +
			    "	}\n" +
			    "        else\n" +
			    "        {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class has an initializeRole method with a signature that is not as required 2.2h
    // 2.2.20-otjld_obsolete-initializeRole-with-different-signature-1
    public void _obsolete_test2220_initializeRoleWithDifferentSignature1() {
       
       runConformTest(
            new String[] {
		"T2220iwds1Main.java",
			    "\n" +
			    "public class T2220iwds1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2220iwds1 t = new Team2220iwds1();\n" +
			    "        T2220iwds1    o = new T2220iwds1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2220iwds1.java",
			    "\n" +
			    "public class T2220iwds1 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2220iwds1.java",
			    "\n" +
			    "public team class Team2220iwds1 {\n" +
			    "    public class Role2220iwds1 playedBy T2220iwds1 {\n" +
			    "        private int value = 0;\n" +
			    "\n" +
			    "        public void initializeRole() {\n" +
			    "            value++;\n" +
			    "        }\n" +
			    "\n" +
			    "        public abstract int getValue();\n" +
			    "        int getValue() -> int getValue() with {\n" +
			    "            result <- value\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public int getValue(T2220iwds1 as Role2220iwds1 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "0");
    }

    // a role class has an initializeRole method with a signature that is not as required 2.2h
    // 2.2.20-otjld_obsolete-initializeRole-with-different-signature-2
    public void _obsolete_test2220_initializeRoleWithDifferentSignature2() {
       
       runConformTest(
            new String[] {
		"T2220iwds2Main.java",
			    "\n" +
			    "public class T2220iwds2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2220iwds2 t = new Team2220iwds2();\n" +
			    "        T2220iwds2    o = new T2220iwds2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2220iwds2.java",
			    "\n" +
			    "public class T2220iwds2 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2220iwds2.java",
			    "\n" +
			    "public team class Team2220iwds2 {\n" +
			    "    public class Role2220iwds2 playedBy T2220iwds2 {\n" +
			    "        private int value = 0;\n" +
			    "\n" +
			    "        void initializeRole() {\n" +
			    "            value++;\n" +
			    "        }\n" +
			    "\n" +
			    "        public abstract int getValue();\n" +
			    "        int getValue() -> int getValue() with {\n" +
			    "            result <- value\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public int getValue(T2220iwds2 as Role2220iwds2 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "0");
    }

    // a role class has an initializeRole method with a signature that is not as required 2.2h
    // 2.2.20-otjld_obsolete-initializeRole-with-different-signature-3
    public void _obsolete_test2220_initializeRoleWithDifferentSignature3() {
       
       runConformTest(
            new String[] {
		"T2220iwds3Main.java",
			    "\n" +
			    "public class T2220iwds3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2220iwds3 t = new Team2220iwds3();\n" +
			    "        T2220iwds3    o = new T2220iwds3();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2220iwds3.java",
			    "\n" +
			    "public class T2220iwds3 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2220iwds3.java",
			    "\n" +
			    "public team class Team2220iwds3 {\n" +
			    "    public class Role2220iwds3 playedBy T2220iwds3 {\n" +
			    "        private int value = 0;\n" +
			    "\n" +
			    "        private void initializeRole() {\n" +
			    "            value++;\n" +
			    "        }\n" +
			    "\n" +
			    "        public abstract int getValue();\n" +
			    "        int getValue() -> int getValue() with {\n" +
			    "            result <- value\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public int getValue(T2220iwds3 as Role2220iwds3 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "0");
    }

    // a role class has an initializeRole method with a signature that is not as required 2.2h
    // 2.2.20-otjld_obsolete-initializeRole-with-different-signature-4
    public void _obsolete_test2220_initializeRoleWithDifferentSignature4() {
       
       runConformTest(
            new String[] {
		"T2220iwds4Main.java",
			    "\n" +
			    "public class T2220iwds4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2220iwds4 t = new Team2220iwds4();\n" +
			    "        T2220iwds4    o = new T2220iwds4();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2220iwds4.java",
			    "\n" +
			    "public class T2220iwds4 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2220iwds4.java",
			    "\n" +
			    "public team class Team2220iwds4 {\n" +
			    "    public class Role2220iwds4 playedBy T2220iwds4 {\n" +
			    "        private int value = 0;\n" +
			    "\n" +
			    "        protected int initializeRole() {\n" +
			    "            value++;\n" +
			    "            return value;\n" +
			    "        }\n" +
			    "\n" +
			    "        public abstract int getValue();\n" +
			    "        int getValue() -> int getValue() with {\n" +
			    "            result <- value\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public int getValue(T2220iwds4 as Role2220iwds4 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "0");
    }

    // a role class has an initializeRole method with a signature that is not as required 2.2h
    // 2.2.20-otjld_obsolete-initializeRole-with-different-signature-5
    public void _obsolete_test2220_initializeRoleWithDifferentSignature5() {
       
       runConformTest(
            new String[] {
		"T2220iwds5Main.java",
			    "\n" +
			    "public class T2220iwds5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2220iwds5 t = new Team2220iwds5();\n" +
			    "        T2220iwds5    o = new T2220iwds5();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2220iwds5.java",
			    "\n" +
			    "public class T2220iwds5 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2220iwds5.java",
			    "\n" +
			    "public team class Team2220iwds5 {\n" +
			    "    public class Role2220iwds5 playedBy T2220iwds5 {\n" +
			    "        private int value = 0;\n" +
			    "\n" +
			    "        protected void initializeRole(int arg) {\n" +
			    "            value++;\n" +
			    "        }\n" +
			    "\n" +
			    "        public abstract int getValue();\n" +
			    "        int getValue() -> int getValue() with {\n" +
			    "            result <- value\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public int getValue(T2220iwds5 as Role2220iwds5 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "0");
    }

    // a role class has an initializeRole method that is called exactly once during subsequent lifting of the same base object in the same team
    // 2.2.22-otjld-role-with-initializeRole-method-1
    public void test2222_roleWithInitializeRoleMethod1() {
       
       runConformTest(
            new String[] {
		"T2222rwim1Main.java",
			    "\n" +
			    "public class T2222rwim1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2222rwim1 t = new Team2222rwim1();\n" +
			    "        T2222rwim1    o = new T2222rwim1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2222rwim1.java",
			    "\n" +
			    "public class T2222rwim1 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2222rwim1.java",
			    "\n" +
			    "public team class Team2222rwim1 {\n" +
			    "    private int value = 0;\n" +
			    "    public class Role2222rwim1 playedBy T2222rwim1 {\n" +
			    "\n" +
			    "        public Role2222rwim1(T2222rwim1 b) {\n" +
			    "            Team2222rwim1.this.value++;\n" +
			    "        }\n" +
			    "\n" +
			    "        public abstract int getValue();\n" +
			    "        int getValue() -> int getValue() with {\n" +
			    "            result <- Team2222rwim1.this.value\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    private Role2222rwim1 toRole(T2222rwim1 as Role2222rwim1 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public int getValue(T2222rwim1 obj) {\n" +
			    "        final Role2222rwim1 lifted1 = toRole(obj);\n" +
			    "        T2222rwim1          lowered = lifted1;\n" +
			    "        Role2222rwim1       lifted2 = toRole(lowered);\n" +
			    "\n" +
			    "        return lifted2.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1");
    }

    // a role class has an initializeRole method that is called twice during subsequent lifting of the same base object to different roles in the same team
    // 2.2.22-otjld-role-with-initializeRole-method-2
    public void test2222_roleWithInitializeRoleMethod2() {
       
       runConformTest(
            new String[] {
		"T2222rwim2Main.java",
			    "\n" +
			    "public class T2222rwim2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2222rwim2 t = new Team2222rwim2();\n" +
			    "        T2222rwim2    o = new T2222rwim2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2222rwim2.java",
			    "\n" +
			    "public class T2222rwim2 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2222rwim2.java",
			    "\n" +
			    "public team class Team2222rwim2 {\n" +
			    "    private int value = 0;\n" +
			    "\n" +
			    "    public class Role2222rwim2_1 playedBy T2222rwim2 {\n" +
			    "\n" +
			    "        Role2222rwim2_1(T2222rwim2 b) {\n" +
			    "            Team2222rwim2.this.value++;\n" +
			    "        }\n" +
			    "\n" +
			    "        public abstract int getValue();\n" +
			    "        int getValue() -> int getValue() with {\n" +
			    "            result <- Team2222rwim2.this.value\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role2222rwim2_2 playedBy T2222rwim2 {\n" +
			    "\n" +
			    "        Role2222rwim2_2(T2222rwim2 b) {\n" +
			    "            Team2222rwim2.this.value++;\n" +
			    "        }\n" +
			    "	}\n" +
			    "\n" +
			    "    private Role2222rwim2_1 role;\n" +
			    "\n" +
			    "    private Role2222rwim2_1 toRole(T2222rwim2 as Role2222rwim2_1 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public int getValue(T2222rwim2 as Role2222rwim2_2 obj) {\n" +
			    "        T2222rwim2 lowered = obj;\n" +
			    "\n" +
			    "        // this should produce a different role object (of type Role2222rwim2_1) -> two calls to initializeRole\n" +
			    "        role = toRole(obj);        \n" +
			    "        return role.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "2");
    }

    // each class in a role hierarchy has its own default constructor, all of which are invoked
    // 2.2.23-otjld-role-hierarchy-with-initializeRole-methods
    public void test2223_roleHierarchyWithInitializeRoleMethods() {
       
       runConformTest(
            new String[] {
		"T2223rhwimMain.java",
			    "\n" +
			    "public class T2223rhwimMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2223rhwim t = new Team2223rhwim();\n" +
			    "        T2223rhwim_2  o = new T2223rhwim_2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2223rhwim_1.java",
			    "\n" +
			    "public class T2223rhwim_1 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2223rhwim_2.java",
			    "\n" +
			    "public class T2223rhwim_2 extends T2223rhwim_1 {}\n" +
			    "    \n",
		"Team2223rhwim.java",
			    "\n" +
			    "public team class Team2223rhwim {\n" +
			    "    protected int value = 0;\n" +
			    "\n" +
			    "    public abstract class Role2223rhwim_1  {\n" +
			    "        Role2223rhwim_1() {\n" +
			    "            Team2223rhwim.this.value++;\n" +
			    "        }\n" +
			    "\n" +
			    "        public abstract int getValue();\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role2223rhwim_2 extends Role2223rhwim_1 playedBy T2223rhwim_2 {\n" +
			    "        Role2223rhwim_2(T2223rhwim_2 b) {\n" +
			    "            Team2223rhwim.this.value++;\n" +
			    "        }\n" +
			    "        int getValue() -> int getValue() with {\n" +
			    "            result <- Team2223rhwim.this.value\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role2223rhwim_3 extends Role2223rhwim_2 {\n" +
			    "        Role2223rhwim_3(T2223rhwim_2 b) {\n" +
			    "            Team2223rhwim.this.value++;\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public int getValue(T2223rhwim_2 as Role2223rhwim_3 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "3");
    }

    // a role hierarchy with initializeRole methods containing super-calls
    // 2.2.24-otjld-role-hierarchy-with-initializeRole-methods
    public void test2224_roleHierarchyWithInitializeRoleMethods() {
       
       runConformTest(
            new String[] {
		"T2224rhwimMain.java",
			    "\n" +
			    "public class T2224rhwimMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2224rhwim_2 t = new Team2224rhwim_2();\n" +
			    "        T2224rhwim      o = new T2224rhwim();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2224rhwim.java",
			    "\n" +
			    "public class T2224rhwim {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2224rhwim_1.java",
			    "\n" +
			    "public team class Team2224rhwim_1 {\n" +
			    "    protected int value = 0;\n" +
			    "\n" +
			    "    public class Role2224rhwim_1 playedBy T2224rhwim {\n" +
			    "        Role2224rhwim_1(T2224rhwim b) {\n" +
			    "            Team2224rhwim_1.this.value++;\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role2224rhwim_2 extends Role2224rhwim_1 {\n" +
			    "        // does not inherit lifting constructor\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2224rhwim_2.java",
			    "\n" +
			    "public team class Team2224rhwim_2 extends Team2224rhwim_1 {\n" +
			    "    public class Role2224rhwim_2 {\n" +
			    "		// overrides public default constructor\n" +
			    "        public Role2224rhwim_2(T2224rhwim b) {\n" +
			    "            tsuper(b);\n" +
			    "            Team2224rhwim_2.this.value++;\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role2224rhwim_3 extends Role2224rhwim_2 {\n" +
			    "		// overrides public constructor\n" +
			    "        public Role2224rhwim_3(T2224rhwim b) {\n" +
			    "            super(b);\n" +
			    "            Team2224rhwim_2.this.value++;\n" +
			    "        }\n" +
			    "\n" +
			    "        public abstract int getValue();\n" +
			    "        int getValue() -> int getValue() with {\n" +
			    "            result <- Team2224rhwim_2.this.value\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public int getValue(T2224rhwim as Role2224rhwim_3 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "3");
    }

    // the default lifting constructor is public so must all overrides
    // 2.2.24-otjld-restricting-constructor-visibility
    public void test2224_restrictingConstructorVisibility() {
        runNegativeTest(
            new String[] {
		"T2224rhwim2.java",
			    "\n" +
			    "public class T2224rhwim2 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2224rhwim2_1.java",
			    "\n" +
			    "public team class Team2224rhwim2_1 {\n" +
			    "    protected int value = 0;\n" +
			    "\n" +
			    "    public class Role2224rhwim2_1 playedBy T2224rhwim2 {\n" +
			    "        Role2224rhwim2_1(T2224rhwim2 b) {\n" +
			    "            Team2224rhwim2_1.this.value++;\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role2224rhwim2_2 extends Role2224rhwim2_1 {\n" +
			    "        // should inherit the initializeRole method\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2224rhwim2_2.java",
			    "\n" +
			    "public team class Team2224rhwim2_2 extends Team2224rhwim2_1 {\n" +
			    "    public class Role2224rhwim2_2 {\n" +
			    "		// try to override public default constructor\n" +
			    "        Role2224rhwim2_2(T2224rhwim2 b) {\n" +
			    "            tsuper(b);\n" +
			    "            Team2224rhwim2_2.this.value++;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "	\n"
            },
            null);
    }

    // a role class inherited the initializeRole method which is called exactly once during lifting
    // 2.2.25-otjld-inherited-initializeRole-method
    public void test2225_inheritedInitializeRoleMethod() {
       
       runConformTest(
            new String[] {
		"T2225iimMain.java",
			    "\n" +
			    "public class T2225iimMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2225iim_2 t = new Team2225iim_2();\n" +
			    "        T2225iim      o = new T2225iim();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2225iim.java",
			    "\n" +
			    "public class T2225iim {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2225iim_1.java",
			    "\n" +
			    "public team class Team2225iim_1 {\n" +
			    "    protected int value = 0;\n" +
			    "\n" +
			    "    public class Role2225iim_1 playedBy T2225iim {\n" +
			    "        Role2225iim_1(T2225iim b) {\n" +
			    "            Team2225iim_1.this.value++;\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role2225iim_2 extends Role2225iim_1 {}\n" +
			    "}\n" +
			    "    \n",
		"Team2225iim_2.java",
			    "\n" +
			    "public team class Team2225iim_2 extends Team2225iim_1 {\n" +
			    "    public class Role2225iim_3 extends Role2225iim_2 {\n" +
			    "        public abstract int getValue();\n" +
			    "        int getValue() -> int getValue() with {\n" +
			    "            result <- Team2225iim_2.this.value\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public int getValue(T2225iim as Role2225iim_3 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1");
    }

    // an array of base class objects is lifted to a role array, and for each lifted base object the initializeRole method is called
    // 2.2.27-otjld-array-lifting-calls-initializeRole-1
    public void test2227_arrayLiftingCallsInitializeRole1() {
       
       runConformTest(
            new String[] {
		"T2227alci1Main.java",
			    "\n" +
			    "public class T2227alci1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2227alci1 t = new Team2227alci1();\n" +
			    "        T2227alci1[]  o = { new T2227alci1(), new T2227alci1(), new T2227alci1() };\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2227alci1.java",
			    "\n" +
			    "public class T2227alci1 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2227alci1.java",
			    "\n" +
			    "public team class Team2227alci1 {\n" +
			    "    protected int value = 0;\n" +
			    "\n" +
			    "    public class Role2227alci1_1 playedBy T2227alci1 {\n" +
			    "        Role2227alci1_1(T2227alci1 b) {\n" +
			    "            Team2227alci1.this.value++;\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role2227alci1_2 extends Role2227alci1_1 {}\n" +
			    "\n" +
			    "    private Role2227alci1_2[] toRoles(T2227alci1 as Role2227alci1_2 obj[]) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "\n" +
			    "    public int test(T2227alci1 as Role2227alci1_2 obj[]) {\n" +
			    "        T2227alci1[]      lowered = obj;\n" +
			    "        Role2227alci1_2[] lifted  = toRoles(lowered);\n" +
			    "\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "3");
    }

    // an array of base class objects is lifted to a role array, and for each lifted base object the initializeRole method is called
    // 2.2.27-otjld-array-lifting-calls-initializeRole-2
    public void test2227_arrayLiftingCallsInitializeRole2() {
       
       runConformTest(
            new String[] {
		"T2227alci2Main.java",
			    "\n" +
			    "public class T2227alci2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2227alci2_2 t = new Team2227alci2_2();\n" +
			    "        T2227alci2_1[]  o = { new T2227alci2_1(), new T2227alci2_2(), new T2227alci2_1() };\n" +
			    "\n" +
			    "        System.out.print(t.test(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2227alci2_1.java",
			    "\n" +
			    "public class T2227alci2_1 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2227alci2_2.java",
			    "\n" +
			    "public class T2227alci2_2 extends T2227alci2_1 {}\n" +
			    "    \n",
		"Team2227alci2_1.java",
			    "\n" +
			    "public team class Team2227alci2_1 {\n" +
			    "    protected int value = 0;\n" +
			    "\n" +
			    "    public class Role2227alci2_1 playedBy T2227alci2_1 {\n" +
			    "        Role2227alci2_1(T2227alci2_1 b) {\n" +
			    "            Team2227alci2_1.this.value++;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2227alci2_2.java",
			    "\n" +
			    "public team class Team2227alci2_2 extends Team2227alci2_1 {\n" +
			    "    public class Role2227alci2_2 extends Role2227alci2_1 playedBy T2227alci2_2 {}\n" +
			    "\n" +
			    "    public int test(T2227alci2_1 as Role2227alci2_1 obj[]) {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "3");
    }

    // a constructor declares a parameter with lifting
    // 2.2.28-otjld-declared-lifting-in-construtor-1
    public void test2228_declaredLiftingInConstrutor1() {
       
       runConformTest(
            new String[] {
		"Team2228dlic1.java",
			    "\n" +
			    "public team class Team2228dlic1 {\n" +
			    "	String val=\"O\";\n" +
			    "	protected class R playedBy T2228dlic1 {\n" +
			    "		protected String getVa() {\n" +
			    "			return val;\n" +
			    "		}\n" +
			    "		protected abstract String getLue();\n" +
			    "		getLue -> getter;\n" +
			    "	}\n" +
			    "	public Team2228dlic1 (T2228dlic1 as R r) {\n" +
			    "		System.out.print(r.getVa()+r.getLue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team2228dlic1(new T2228dlic1());\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T2228dlic1.java",
			    "\n" +
			    "public class T2228dlic1 {\n" +
			    "	public String getter() { return \"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a constructor declares a parameter with lifting
    // 2.2.28-otjld-declared-lifting-in-construtor-2
    public void test2228_declaredLiftingInConstrutor2() {
       
       runConformTest(
            new String[] {
		"Team2228dlic2_2.java",
			    "\n" +
			    "public team class Team2228dlic2_2 extends Team2228dlic2_1 {\n" +
			    "	public Team2228dlic2_2 (T2228dlic2 as R r) {\n" +
			    "		super(r);\n" +
			    "		System.out.print(r.getLue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team2228dlic2_2(new T2228dlic2());\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T2228dlic2.java",
			    "\n" +
			    "public class T2228dlic2 {\n" +
			    "	public String getter() { return \"K\"; }\n" +
			    "}\n" +
			    "	\n",
		"Team2228dlic2_1.java",
			    "\n" +
			    "public team class Team2228dlic2_1 {\n" +
			    "	String val=\"O\";\n" +
			    "	protected class R playedBy T2228dlic2 {\n" +
			    "		protected String getVa() {\n" +
			    "			return val;\n" +
			    "		}\n" +
			    "		abstract public String getLue();\n" +
			    "		getLue -> getter;\n" +
			    "	}\n" +
			    "	public Team2228dlic2_1 (T2228dlic2 as R r) {\n" +
			    "		System.out.print(r.getVa());\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a constructor declares a parameter with lifting - invoking super with role type
    // 2.2.28-otjld-declared-lifting-in-construtor-3
    public void test2228_declaredLiftingInConstrutor3() {
       
       runConformTest(
            new String[] {
		"Team2228dlic3_2.java",
			    "\n" +
			    "public team class Team2228dlic3_2 extends Team2228dlic3_1 {\n" +
			    "	public Team2228dlic3_2 (T2228dlic3 as R r) {\n" +
			    "		super(r);\n" +
			    "		System.out.print(r.getLue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team2228dlic3_2(new T2228dlic3());\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T2228dlic3.java",
			    "\n" +
			    "public class T2228dlic3 {\n" +
			    "	public String getter() { return \"K\"; }\n" +
			    "}\n" +
			    "	\n",
		"Team2228dlic3_1.java",
			    "\n" +
			    "public team class Team2228dlic3_1 {\n" +
			    "	String val=\"O\";\n" +
			    "	protected class R playedBy T2228dlic3 {\n" +
			    "		protected String getVa() {\n" +
			    "			return val;\n" +
			    "		}\n" +
			    "		abstract public String getLue();\n" +
			    "		getLue -> getter;\n" +
			    "	}\n" +
			    "	public Team2228dlic3_1 (R r) {\n" +
			    "		System.out.print(r.getVa());\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a constructor uses a parameter with declared lifting, but the base type is invalid 
    // WITNESS for error reported in TPX-204 (fixed somewhere between 0.7.5 and 0.7.11)
    // 2.2.28-otjld-declared-lifting-in-construtor-4
    public void test2228_declaredLiftingInConstrutor4() {
        runNegativeTest(
            new String[] {
		"T2228dlic4.java",
			    " public class T2228dlic4 {} \n",
		"Team2228dlic4_2.java",
			    "\n" +
			    "public team class Team2228dlic4_2 {\n" +
			    "    protected class R playedBy T2228dlic4 {}\n" +
			    "    private Team2228dlic4_2(Erroneous as R o) { }\n" +
			    "}    \n" +
			    "    \n"
            },
            null);
    }

    // a constructor with declared lifting calls super with a role across several levels
    // 2.2.28-otjld-declared-lifting-in-constructor-5
    // Bug 326052 [compiler] problems with team ctors with declared lifting in specific inheritance situations 
    public void test2228_declaredLiftingInConstructor5() {
       
       runConformTest(
            new String[] {
		"Team2228dlic5_3.java",
			    "\n" +
			    "public team class Team2228dlic5_3 extends Team2228dlic5_2 {\n" +
			    "    public class R2 extends R playedBy T2228dlic5 {}\n" +
			    "    public Team2228dlic5_3(T2228dlic5 as R2 r) {\n" +
			    "        super(r);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2228dlic5_3(new T2228dlic5());\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T2228dlic5.java",
			    "\n" +
			    "public class T2228dlic5 {}    \n" +
			    "    \n",
		"Team2228dlic5_1.java",
			    "\n" +
			    "public abstract team class Team2228dlic5_1 {\n" +
			    "    protected abstract class R {\n" +
			    "        protected abstract void test();\n" +
			    "    }\n" +
			    "    protected Team2228dlic5_1(R r) {\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team2228dlic5_2.java",
			    "\n" +
			    "public team class Team2228dlic5_2 extends Team2228dlic5_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected Team2228dlic5_2(R r) {\n" +
			    "        super(r);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a constructor with declared lifting calls super with a role across several levels
    // witness for AssertionError during bytecode copy
    // 2.2.28-otjld-declared-lifting-in-constructor-5f
    public void test2228_declaredLiftingInConstructor5f() {
        runNegativeTest(
            new String[] {
		"Team2228dlic5f_1.java",
			    "\n" +
			    "public abstract team class Team2228dlic5f_1 {\n" +
			    "    protected abstract class R {\n" +
			    "        abstract void test();\n" +
			    "    }\n" +
			    "    protected Team2228dlic5f_1(R r) {\n" +
			    "        r.test(); // not visible, causes this ctor and all clients to fail\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2228dlic5f.java",
			    "\n" +
			    "public class T2228dlic5f {}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. ERROR in Team2228dlic5f_1.java (at line 7)\n" +
			"	r.test(); // not visible, causes this ctor and all clients to fail\n" +
			"	  ^^^^\n" +
			"The method test() from the role type Team2228dlic5f_1.R is not visible (OTJLD 1.2.1(e)).\n" +
			"----------\n",
            null/*classLibs*/,
            false/*shouldFlushOutputDirectory*/,
            null/*customOptions*/,
            true/*generateOutput*/,
            false/*showCategory*/,
            false/*showWarningToken*/);
        runConformTest(
        	new String[] {
		"Team2228dlic5f_2.java",
			    "\n" +
			    "public team class Team2228dlic5f_2 extends Team2228dlic5f_1 {\n" +
			    "	 @Override\n" +
			    "    protected class R {\n" +
			    "	     @Override\n" +
			    "        void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected Team2228dlic5f_2(R r) {\n" +
			    "        super(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2228dlic5f_3.java",
			    "\n" +
			    "public team class Team2228dlic5f_3 extends Team2228dlic5f_2 {\n" +
			    "    public class R2 extends R playedBy T2228dlic5f {}\n" +
			    "    public Team2228dlic5f_3(T2228dlic5f as R2 r) {\n" +
			    "        super(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            ""/*expectedCompilerOutput*/,
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/);
    }

    // a constructor with declared lifting calls super with a role across several levels - compiling in one go should not trigger compiler limitation (can't find super call)
    // 2.2.28-otjld-declared-lifting-in-constructor-5g
    public void test2228_declaredLiftingInConstructor5g() {
       
       runConformTest(
            new String[] {
		"Team2228dlic5g_3.java",
			    "\n" +
			    "public team class Team2228dlic5g_3 extends Team2228dlic5g_2 {\n" +
			    "    public class R2 extends R playedBy T2228dlic5g {}\n" +
			    "    public Team2228dlic5g_3(T2228dlic5g as R2 r) {\n" +
			    "        super(r);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2228dlic5g_3(new T2228dlic5g());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2228dlic5g.java",
			    "\n" +
			    "public class T2228dlic5g {}\n" +
			    "    \n",
		"Team2228dlic5g_1.java",
			    "\n" +
			    "public abstract team class Team2228dlic5g_1 {\n" +
			    "    protected abstract class R {\n" +
			    "        abstract protected void test();\n" +
			    "    }\n" +
			    "    protected Team2228dlic5g_1(R r) {\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2228dlic5g_2.java",
			    "\n" +
			    "public team class Team2228dlic5g_2 extends Team2228dlic5g_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected Team2228dlic5g_2(R r) {\n" +
			    "        super(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a constructor with declared lifting calls super with a role across several levels -- unsupported data flow
    // 2.2.28-otjld-declared-lifting-in-constructor-6
    // Bug 326052 [compiler] problems with team ctors with declared lifting in specific inheritance situations 
    public void _test2228_declaredLiftingInConstructor6() {
        runConformTest(
            new String[] {
		"Team2228dlic6_3.java",
			    "\n" +
			    "public team class Team2228dlic6_3 extends Team2228dlic6_2 {\n" +
			    "    public class R2 extends R playedBy T2228dlic6 {}\n" +
			    "    public Team2228dlic6_3(T2228dlic6 as R2 r) {\n" +
			    "        super(r);\n" +
			    "    }\n" +
			    "    public static void main(String... args) {\n" +
			    "		new Team2228dlic6_3(new T2228dlic6());\n" +
			    "	 }\n" +
			    "}    \n" +
			    "    \n",
		"T2228dlic6.java",
			    "\n" +
			    "public class T2228dlic6 {}    \n" +
			    "    \n",
		"Team2228dlic6_1.java",
			    "\n" +
			    "public abstract team class Team2228dlic6_1 {\n" +
			    "    protected abstract class R {\n" +
			    "        protected abstract void test();\n" +
			    "    }\n" +
			    "    protected Team2228dlic6_1(R r) {\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team2228dlic6_2.java",
			    "\n" +
			    "public team class Team2228dlic6_2 extends Team2228dlic6_1 {\n" +
			    "    @Override\n" +
			    "    protected class R {\n" +
			    "        @Override\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        protected R id() { return this; }\n" +
			    "    }\n" +
			    "    protected Team2228dlic6_2(R r) {\n" +
			    "        super(r.id());\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a constructor with declared lifting calls super with a role across several levels -- unsupported data flow
    // 2.2.28-otjld-declared-lifting-in-constructor-6l
    public void test2228_declaredLiftingInConstructor6l() {
        runConformTest(
                new String[] {
		"T2228dlic6l.java",
			    "\n" +
			    "public class T2228dlic6l {}    \n" +
			    "    \n",
		"Team2228dlic6l_1.java",
			    "\n" +
			    "public abstract team class Team2228dlic6l_1 {\n" +
			    "    protected abstract class R {\n" +
			    "        protected abstract void test();\n" +
			    "    }\n" +
			    "    protected Team2228dlic6l_1(R r) {\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team2228dlic6l_2.java",
			    "\n" +
			    "public team class Team2228dlic6l_2 extends Team2228dlic6l_1 {\n" +
			    "    @Override\n" +
			    "    protected class R {\n" +
			    "        @Override\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        protected R id() { return this; }\n" +
			    "    }\n" +
			    "    protected Team2228dlic6l_2(R r) {\n" +
			    "        super(r.id());\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            });
        runNegativeTest(
            new String[] {
		"Team2228dlic6l_3.java",
			    "\n" +
			    "public team class Team2228dlic6l_3 extends Team2228dlic6l_2 {\n" +
			    "    public class R2 extends R playedBy T2228dlic6l {}\n" +
			    "    public Team2228dlic6l_3(T2228dlic6l as R2 r) {\n" +
			    "        super(r);\n" +
			    "    }\n" +
			    "}\n"
            },
            "----------\n" + 
    		"1. ERROR in Team2228dlic6l_3.java (at line 4)\n" + 
    		"	public Team2228dlic6l_3(T2228dlic6l as R2 r) {\n" + 
    		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
    		"Compiler limitation: This constructor seems to request lifting of an argument. However, the self call in Team2228dlic6l_2(R) of class Team2228dlic6l_2 passes its arguments in an unexpected way. Cannot perform required transitive byte-code translation.\n" + 
    		"----------\n",
        	null/*classLibraries*/,
        	false/*shouldFlushOutputDirectory*/);
    }

    // a constructor with declared lifting calls super with a role across several levels -- 
    // this version generated ICE("Binary method has no code attribute")
    // 2.2.28-otjld-declared-lifting-in-constructor-6f
    public void test2228_declaredLiftingInConstructor6f() {
        runNegativeTestMatching(
            new String[] {
		"Team2228dlic6f_3.java",
			    "\n" +
			    "public team class Team2228dlic6f_3 extends Team2228dlic6f_2 {\n" +
			    "    public class R2 extends R playedBy T2228dlic6f {}\n" +
			    "    public Team2228dlic6f_3(T2228dlic6f as R2 r) {\n" +
			    "        super(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2228dlic6f.java",
			    "\n" +
			    "public class T2228dlic6f {}\n" +
			    "    \n",
		"Team2228dlic6f_1.java",
			    "\n" +
			    "public abstract team class Team2228dlic6f_1 {\n" +
			    "    protected abstract class R {\n" +
			    "        abstract void test();\n" +
			    "    }\n" +
			    "    protected Team2228dlic6f_1(R r) {\n" +
			    "        r.test(); // not visible \n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2228dlic6f_2.java",
			    "\n" +
			    "public team class Team2228dlic6f_2 extends Team2228dlic6f_1 {\n" +
			    "    @Override\n" +
			    "    protected class R {\n" +
			    "       @Override\n" +
			    "        void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        R id() { return this; }\n" +
			    "    }\n" +
			    "    protected Team2228dlic6f_2(R r) {\n" +
			    "        super(r.id()); // not visible\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team2228dlic6f_1.java (at line 7)\n" + 
    		"	r.test(); // not visible \n" + 
    		"	  ^^^^\n" + 
    		"The method test() from the role type Team2228dlic6f_1.R is not visible (OTJLD 1.2.1(e)).\n" + 
    		"----------\n" + 
    		"----------\n" + 
    		"1. ERROR in Team2228dlic6f_2.java (at line 12)\n" + 
    		"	super(r.id()); // not visible\n" + 
    		"	        ^^\n" + 
    		"The method id() from the role type Team2228dlic6f_2.R is not visible (OTJLD 1.2.1(e)).\n" + 
    		"----------\n");
    }

    // a constructor with declared lifting calls super with a role across several levels  -- unsupported data flow
    // 2.2.28-otjld-declared-lifting-in-constructor-7
    // Bug 326052 [compiler] problems with team ctors with declared lifting in specific inheritance situations 
    public void _test2228_declaredLiftingInConstructor7() {
        runConformTest(
            new String[] {
		"Team2228dlic7_3.java",
			    "\n" +
			    "public team class Team2228dlic7_3 extends Team2228dlic7_2 {\n" +
			    "    public class R2 extends R playedBy T2228dlic7 {}\n" +
			    "    public Team2228dlic7_3(T2228dlic7 as R2 r) {\n" +
			    "        super(r);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2228dlic7_3(new T2228dlic7());\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T2228dlic7.java",
			    "\n" +
			    "public class T2228dlic7 {}    \n" +
			    "    \n",
		"Team2228dlic7_1.java",
			    "\n" +
			    "public abstract team class Team2228dlic7_1 {\n" +
			    "    protected abstract class R {\n" +
			    "        protected abstract void test();\n" +
			    "    }\n" +
			    "    protected Team2228dlic7_1(int dummy, R r) {\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team2228dlic7_2.java",
			    "\n" +
			    "public team class Team2228dlic7_2 extends Team2228dlic7_1 {\n" +
			    "    @Override\n" +
			    "    protected class R {\n" +
			    "    	 @Override\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected Team2228dlic7_2(R r) {\n" +
			    "        super(1, r);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }
    
    // a constructor with declared lifting calls super with a role across several levels  -- unsupported data flow
    // 2.2.28-otjld-declared-lifting-in-constructor-7l
    public void test2228_declaredLiftingInConstructor7l() {
        runConformTest(
            new String[] {
		"T2228dlic7l.java",
			    "\n" +
			    "public class T2228dlic7l {}    \n" +
			    "    \n",
		"Team2228dlic7l_1.java",
			    "\n" +
			    "public abstract team class Team2228dlic7l_1 {\n" +
			    "    protected abstract class R {\n" +
			    "        protected abstract void test();\n" +
			    "    }\n" +
			    "    protected Team2228dlic7l_1(int dummy, R r) {\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team2228dlic7l_2.java",
			    "\n" +
			    "public team class Team2228dlic7l_2 extends Team2228dlic7l_1 {\n" +
			    "    @Override\n" +
			    "    protected class R {\n" +
			    "    	 @Override\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        R id() { return this; }\n" +
			    "    }\n" +
			    "    protected Team2228dlic7l_2(R r) {\n" +
			    "        super(1, r);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/);
        
        runNegativeTest(
            new String[] {
		"Team2228dlic7l_3.java",
			    "\n" +
			    "public team class Team2228dlic7l_3 extends Team2228dlic7l_2 {\n" +
			    "    public class R2 extends R playedBy T2228dlic7l {}\n" +
			    "    public Team2228dlic7l_3(T2228dlic7l as R2 r) {\n" +
			    "        super(r);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2228dlic7l_3(new T2228dlic7l());\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team2228dlic7l_3.java (at line 4)\n" + 
    		"	public Team2228dlic7l_3(T2228dlic7l as R2 r) {\n" + 
    		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
    		"Compiler limitation: This constructor seems to request lifting of an argument. However, the self call in Team2228dlic7l_2(R) of class Team2228dlic7l_2 passes its arguments in an unexpected way. Cannot perform required transitive byte-code translation.\n" + 
    		"----------\n",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/);
    }
    // two roles are lifted, witness for a former AIOOBE in AnchorMapping
    // 2.2.28-otjld-declared-lifting-in-constructor-8
    public void test2228_declaredLiftingInConstructor8() {
       
       runConformTest(
            new String[] {
		"Team2228dlic8_2.java",
			    "\n" +
			    "public team class Team2228dlic8_2 extends Team2228dlic8_1 {\n" +
			    "    protected class R1 playedBy T2228dlic8_1 {\n" +
			    "        test1 -> test1;\n" +
			    "    }\n" +
			    "    protected class R2 playedBy T2228dlic8_2 {\n" +
			    "        test2 -> test2;\n" +
			    "    }\n" +
			    "    public Team2228dlic8_2(T2228dlic8_1 as R1 r1, T2228dlic8_2 as R2 r2) {\n" +
			    "        super(r1, r2);\n" +
			    "    }\n" +
			    "    // shorter ctor just to confuse the compiler:\n" +
			    "    public Team2228dlic8_2(T2228dlic8_1 as R1 r1) {\n" +
			    "        super(r1);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2228dlic8_2(new T2228dlic8_1(), new T2228dlic8_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2228dlic8_1.java",
			    "\n" +
			    "public class T2228dlic8_1 {\n" +
			    "    void test1() { System.out.print(\"OKI\"); }\n" +
			    "}\n" +
			    "    \n",
		"T2228dlic8_2.java",
			    "\n" +
			    "public class T2228dlic8_2 {\n" +
			    "    void test2() { System.out.print(\"DOKI\"); }\n" +
			    "}\n" +
			    "    \n",
		"Team2228dlic8_1.java",
			    "\n" +
			    "public team class Team2228dlic8_1 {\n" +
			    "    abstract protected class R1 {\n" +
			    "        abstract protected void test1();\n" +
			    "    }\n" +
			    "    abstract protected class R2 {\n" +
			    "        abstract protected void test2();\n" +
			    "    }\n" +
			    "    public Team2228dlic8_1(R1 r1, R2 r2) {\n" +
			    "        r1.test1();\n" +
			    "        r2.test2();\n" +
			    "    }\n" +
			    "    // shorter ctor just to confuse the compiler:\n" +
			    "    public Team2228dlic8_1(R1 r1) {\n" +
			    "        r1.test1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OKIDOKI");
    }

    // a role is explicitly lowered
    // 2.2.29-otjld-explicit-lowering-1
    public void test2229_explicitLowering1() {
       
       runConformTest(
            new String[] {
		"Team2229el1.java",
			    "\n" +
			    "public team class Team2229el1 {\n" +
			    "	public class R implements ILowerable playedBy T2229el1 {\n" +
			    "	}\n" +
			    "	public Team2229el1 () {\n" +
			    "		R r = new R(new T2229el1(\"OK\"));\n" +
			    "		T2229el1 t = (T2229el1)r.lower();\n" +
			    "		System.out.print(t.getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team2229el1();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T2229el1.java",
			    "\n" +
			    "public class T2229el1 {\n" +
			    "	private String val = null;\n" +
			    "	public T2229el1(String v) { val = v; }\n" +
			    "	String getValue() { return val; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // explicit lowering has the type of the bound base class
    // 2.2.29-otjld-explicit-lowering-2
    public void test2229_explicitLowering2() {
       
       runConformTest(
            new String[] {
		"Team2229el2.java",
			    "\n" +
			    "public team class Team2229el2 {\n" +
			    "    protected class R1 implements ILowerable playedBy T2229el2_1 {\n" +
			    "        protected R1(String v) {\n" +
			    "            base(v);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected class R2 implements ILowerable playedBy T2229el2_1 {\n" +
			    "        protected R2(String v) {\n" +
			    "            base(v);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test(T2229el2_2 target) {\n" +
			    "        R1 r1 = new R1(\"O\");\n" +
			    "        R2 r2 = new R2(\"K\");\n" +
			    "        target.test(r1.lower());\n" +
			    "        target.test(r2.lower());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2229el2().test(new T2229el2_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2229el2_1.java",
			    "\n" +
			    "public class T2229el2_1 {\n" +
			    "    String val;\n" +
			    "    public T2229el2_1(String v) { this.val = v; }\n" +
			    "    public void print() { System.out.print(this.val); }\n" +
			    "}\n" +
			    "    \n",
		"T2229el2_2.java",
			    "\n" +
			    "public class T2229el2_2 {\n" +
			    "    public void test(T2229el2_1 arg) {\n" +
			    "        arg.print();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an abstract bound role implements ILowerable
    // 2.2.29-otjld-explicit-lowering-3
    public void test2229_explicitLowering3() {
       
       runConformTest(
            new String[] {
		"Team2229el3.java",
			    "\n" +
			    "public team class Team2229el3  {\n" +
			    "    protected abstract class R0 implements ILowerable { }\n" +
			    "    protected class R1 extends R0 implements ILowerable playedBy T2229el3 {\n" +
			    "        public String toString() { return \"Wrong\"; }\n" +
			    "    }\n" +
			    "    public void test(T2229el3 as R0 r) {\n" +
			    "        System.out.print(r.lower());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2229el3 t = new Team2229el3();\n" +
			    "        t.test(new T2229el3());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2229el3.java",
			    "\n" +
			    "public class T2229el3 {\n" +
			    "    public String toString() { return \"OK\"; }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // Bug 391290 - Internal compiler error/Corrupt byte code
    // witness for "tsuper has corrupt byte code" regarding call to _OT$getBase()
    public void test2229_explicitLowering4() {
    	runConformTest(
    		new String[] {
    			"Team2229el4_2.java",
    			"public team class Team2229el4_2 extends Team2229el4_1 {\n" +
    			"	public static void main(String[] args) {\n" +
    			"		new Team2229el4_2().test(new T2229el4());\n" +
    			"	}\n" +
    			"}\n",
    			"Team2229el4_1.java",
    			"public team class Team2229el4_1 {\n" +
    			"	protected class R implements ILowerable playedBy T2229el4 {\n" +
    			"		protected void test() {\n" +
    			"			this.lower().bar();\n" +
    			"		}\n" +
    			"	}\n" +
    			"	void test(T2229el4 as R r) { r.test(); }\n" +
    			"}\n",
    			"T2229el4.java",
    			"public class T2229el4 {\n" +
    			"	void bar() { System.out.print(\"OK\"); }\n" +
    			"}\n"
    		},
    		"OK");
    }

    // Bug 391290 - Internal compiler error/Corrupt byte code
    // positive case involving copy-inherited call to _OT$getBase()
    public void test2229_explicitLowering4b() {
    	runConformTest(
    		new String[] {
				"Team2229el4b_2.java",
				"public team class Team2229el4b_2 extends Team2229el4b_1 {\n" +
				"	public static void main(String[] args) {\n" +
				"		new Team2229el4b_2().test(new T2229el4b());\n" +
				"	}\n" +
				"}\n",
    			"Team2229el4b_1.java",
    			"public team class Team2229el4b_1 {" +
    			"	protected team class Mid {\n" +
    			"		protected class R playedBy T2229el4b {\n" +
    			"		}\n" +
    			"		protected void test(T2229el4b as R r) { " +
    			"			T2229el4b b = r;\n" +
    			"			b.bar();\n" +
    			"		}\n" +
    			"	}\n" +
    			"	public void test(T2229el4b b) {\n" +
    			"		new Mid().test(b);\n" +
    			"	}\n" +
    			"}\n",
    			"T2229el4b.java",
    			"public class T2229el4b {\n" +
    			"	void bar() { System.out.print(\"OK\"); }\n" +
    			"}\n",
    		},
    		"OK");
    }

    // a static team method tries to lift its parameter
    // 2.2.30-otjld-declared-lifting-in-static-method
    public void test2230_declaredLiftingInStaticMethod() {
        runNegativeTestMatching(
            new String[] {
		"Team2230dlism.java",
			    "\n" +
			    "public team class Team2230dlism {\n" +
			    "	protected class R playedBy T2230dlism {}\n" +
			    "	static void erroneous(T2230dlism as R o) {}\n" +
			    "}\n" +
			    "	\n",
		"T2230dlism.java",
			    "\n" +
			    "public class T2230dlism {\n" +
			    "}	\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team2230dlism.java (at line 4)\n" + 
    		"	static void erroneous(T2230dlism as R o) {}\n" + 
    		"	                      ^^^^^^^^^^^^^^^\n" + 
    		"Illegal type for argument o: declared lifting not allowed in static methods (OTJLD 2.3.2(a)).\n" + 
    		"----------\n");
    }

    // a base object should be lifting before its ctor has finished (was TPX-487)
    // 2.2.31-otjld-lifting-uninitialized-object-1
    public void test2231_liftingUninitializedObject1() {
       
       runConformTest(
            new String[] {
		"Team2231luo1.java",
			    "\n" +
			    "public team class Team2231luo1 {\n" +
			    "    protected class R playedBy T2231luo1_2 {\n" +
			    "        void test() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        test <- after test;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2231luo1 t = new Team2231luo1();\n" +
			    "        t.activate();\n" +
			    "        new T2231luo1_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2231luo1_1.java",
			    "\n" +
			    "public class T2231luo1_1 {\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "    T2231luo1_1() {\n" +
			    "        test(); // dynamic call within ctor!\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2231luo1_2.java",
			    "\n" +
			    "public class T2231luo1_2 extends T2231luo1_1 {}\n" +
			    "    \n"
            },
            "OK");
    }

    // a base object should be lifting before its ctor has finished - replace
    // 2.2.31-otjld-lifting-uninitialized-object-2
    public void test2231_liftingUninitializedObject2() {
       
       runConformTest(
            new String[] {
		"Team2231luo2.java",
			    "\n" +
			    "public team class Team2231luo2 {\n" +
			    "    protected class R playedBy T2231luo2_2 {\n" +
			    "        callin void test() {\n" +
			    "            base.test();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        test <- replace test;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2231luo2 t = new Team2231luo2();\n" +
			    "        t.activate();\n" +
			    "        new T2231luo2_2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2231luo2_1.java",
			    "\n" +
			    "public class T2231luo2_1 {\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "    T2231luo2_1() {\n" +
			    "        test(); // dynamic call within ctor!\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2231luo2_2.java",
			    "\n" +
			    "public class T2231luo2_2 extends T2231luo2_1 {}\n" +
			    "    \n"
            },
            "OKOK");
    }

    // an abstract role class is bound to an abstract base class
    // 2.2.32-otjld-lifting-to-abstract-role-1
    public void test2232_liftingToAbstractRole1() {
       
       runConformTest(
            new String[] {
		"Team2232ltar1.java",
			    "import org.objectteams.LiftingFailedException;\n" +
			    "@SuppressWarnings(\"abstractrelevantrole\")\n" +
			    "public team class Team2232ltar1 {\n" +
			    "    protected abstract class R1 playedBy T2232ltar1_1 {\n" +
			    "        void test() -> void test();\n" +
			    "    }\n" +
			    "    protected class R2 extends R1 playedBy T2232ltar1_2 { }\n" +
			    "    Team2232ltar1(T2232ltar1_1 as R1 r) throws LiftingFailedException {\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) throws LiftingFailedException {\n" +
			    "        new Team2232ltar1(new T2232ltar1_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2232ltar1_1.java",
			    "\n" +
			    "public abstract class T2232ltar1_1 {\n" +
			    "    public abstract void test();\n" +
			    "}\n" +
			    "    \n",
		"T2232ltar1_2.java",
			    "\n" +
			    "public class T2232ltar1_2 extends T2232ltar1_1 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is explicitly created to a class that could not be produced by smart lifting, subsequent lifting callin fails
    // 2.2.33-otjld-explicit-creation-breaks-lifting-1
    public void test2233_explicitCreationBreaksLifting1() {
       
       runConformTest(
            new String[] {
		"Team2233ecbl1.java",
			    "\n" +
			    "public team class Team2233ecbl1 {\n" +
			    "    protected class R0 playedBy T2233ecbl1 {\n" +
			    "    }\n" +
			    "    protected class R1 extends R0 {\n" +
			    "        void rm() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        rm <- after bm;\n" +
			    "    }\n" +
			    "    Team2233ecbl1 (T2233ecbl1 b) {\n" +
			    "        this.activate();\n" +
			    "        new R0(b); // only effect: insert role into cache!\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T2233ecbl1 b = new T2233ecbl1();\n" +
			    "        new Team2233ecbl1(b);\n" +
			    "        try {\n" +
			    "            b.bm();\n" +
			    "        } catch (org.objectteams.WrongRoleException wre) {\n" +
			    "            System.out.print(\"Caught\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2233ecbl1.java",
			    "\n" +
			    "public class T2233ecbl1 {\n" +
			    "    void bm() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OCaught");
    }

    // a nested team uses declared lifting, regular base class
    // 2.2.34-otjld-declared-lifting-in-nested-team-1
    public void test2234_declaredLiftingInNestedTeam1() {
       
       runConformTest(
            new String[] {
		"Team2234dlint1.java",
			    "\n" +
			    "public team class Team2234dlint1 {\n" +
			    "	public team class Inner {\n" +
			    "		protected class R playedBy T2234dlint1 {\n" +
			    "			void test() -> void test();\n" +
			    "		}\n" +
			    "		public void test(T2234dlint1 as R o) {\n" +
			    "			o.test();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	void test() {\n" +
			    "		new Inner().test(new T2234dlint1());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team2234dlint1().test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T2234dlint1.java",
			    "\n" +
			    "public class T2234dlint1 {\n" +
			    "	public void test() {\n" +
			    "		System.out.print(\"OK\");\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a nested team uses declared lifting, base class is role of outer team
    // 2.2.34-otjld-declared-lifting-in-nested-team-2
    public void test2234_declaredLiftingInNestedTeam2() {
       
       runConformTest(
            new String[] {
		"Team2234dlint2.java",
			    "\n" +
			    "public team class Team2234dlint2 {\n" +
			    "	protected class OuterRole {\n" +
			    "		public void test() {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public team class Inner {\n" +
			    "		protected class R playedBy OuterRole {\n" +
			    "			void test() -> void test();\n" +
			    "		}\n" +
			    "		public void test(OuterRole as R o) {\n" +
			    "			o.test();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	void test() {\n" +
			    "		new Inner().test(new OuterRole());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team2234dlint2().test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a method call could be resolved both with and without lowering - role type is more specific than lowered base
    // 2.2.35-otjld-ambiguous-lowering-1
    public void test2235_ambiguousLowering1() {
       
       runConformTest(
            new String[] {
		"Team2235al1.java",
			    "\n" +
			    "public team class Team2235al1 {\n" +
			    "    protected class R playedBy T2235al1 { }\n" +
			    "    void test() {\n" +
			    "        m1(new R(new T2235al1()));\n" +
			    "    }\n" +
			    "    void m1(T2235al1 b) {\n" +
			    "        System.out.print(\"with lowering\");\n" +
			    "    }\n" +
			    "    void m1(R r) {\n" +
			    "        System.out.print(\"without lowering\");\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2235al1 t = new Team2235al1();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2235al1.java",
			    "\n" +
			    "public class T2235al1 {}\n" +
			    "    \n"
            },
            "without lowering");
    }

    // a lifting participant creates roles using a custom constructor
    // 2.2.36-otjld-lifting-participant-1
    public void test2236_liftingParticipant1() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
       runConformTest(
            new String[] {
		"Team2236lp1.java",
			    "\n" +
			    "public team class Team2236lp1 {\n" +
			    "    protected class R playedBy T2236lp1_1 {\n" +
			    "        int n;\n" +
			    "        public R(T2236lp1_1 b, int n) {\n" +
			    "            this(b);\n" +
			    "            this.n = n;\n" +
			    "        }\n" +
			    "        public void identify() {\n" +
			    "            System.out.print(this.n);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected class R2 extends R playedBy T2236lp1_2 {\n" +
			    "    }\n" +
			    "    void test(T2236lp1_1 as R o) {\n" +
			    "        o.identify();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team2236lp1 t = new Team2236lp1();\n" +
			    "        t.test(new T2236lp1_1());\n" +
			    "        t.test(new T2236lp1_1());\n" +
			    "        T2236lp1_1 b = new T2236lp1_1();\n" +
			    "        t.test(b);\n" +
			    "        t.test(b);\n" +
			    "        t.test(new T2236lp1_1());\n" +
			    "        t.test(new T2236lp1_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2236lp1_1.java",
			    "\n" +
			    "public class T2236lp1_1 {\n" +
			    "}\n" +
			    "    \n",
		"T2236lp1_2.java",
			    "\n" +
			    "public class T2236lp1_2 extends T2236lp1_1 {\n" +
			    "}\n" +
			    "    \n",
		"lifter/T2236lp1Lifter.java",
			    "\n" +
			    "package lifter;\n" +
			    "import org.objectteams.ITeam;\n" +
			    "import java.lang.reflect.Constructor;\n" +
			    "public class T2236lp1Lifter implements org.objectteams.ILiftingParticipant {\n" +
			    "    int count = 1;\n" +
			    "    public Object createRole(ITeam teamInstance, Object base, String roleClassName) {\n" +
			    "        try {\n" +
			    "            Class<?> rc = Class.forName(roleClassName);\n" +
			    "            Constructor<?>[] ctors = rc.getDeclaredConstructors();\n" +
			    "            for (Constructor<?> ctor : ctors)\n" +
			    "                if (ctor.getParameterTypes().length == 3)\n" +
			    "                    return ctor.newInstance(new Object[]{teamInstance, base, count++});\n" +
			    "\n" +
			    "        } catch (Exception e) {\n" +
			    "            e.printStackTrace();\n" +
			    "        }\n" +
			    "        return null;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "123340",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            new String[] { "-Dot.lifting.participant=lifter.T2236lp1Lifter"}/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a lifting participant creates a layered role using a custom constructor
    // 2.2.36-otjld-lifting-participant-2
    public void test2236_liftingParticipant2() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
       runConformTest(
            new String[] {
		"Team2236lp2_2.java",
			    "\n" +
			    "public team class Team2236lp2_2 {\n" +
			    "    final Team2236lp2_1 otherTeam = new Team2236lp2_1();\n" +
			    "    \n" +
			    "    protected class R playedBy RLower1<@otherTeam> {\n" +
			    "        int n;\n" +
			    "        public R(RLower1<@otherTeam> b, int n) {\n" +
			    "            this(b);\n" +
			    "            this.n = n;\n" +
			    "        }\n" +
			    "        public void identify() {\n" +
			    "            System.out.print(this.n);\n" +
			    "        }\n" +
			    "        identify <- after bm;\n" +
			    "    }\n" +
			    "    protected class R2 extends R playedBy RLower2<@otherTeam> {\n" +
			    "    }\n" +
			    "    protected void test(RLower1<@otherTeam> as R o) {\n" +
			    "        o.identify();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2236lp2_2().runTests();\n" +
			    "    }\n" +
			    "    public void runTests() {\n" +
			    "        test(new RLower1<@otherTeam>());\n" +
			    "        test(new RLower1<@otherTeam>());\n" +
			    "        RLower1<@otherTeam> b = new RLower1<@otherTeam>();\n" +
			    "        test(b);\n" +
			    "        test(b);\n" +
			    "        test(new RLower1<@otherTeam>());\n" +
			    "        test(new RLower2<@otherTeam>());\n" +
			    "        this.activate();\n" +
			    "        new Team2236lp2_1().new RLower1().bm(); // LiftingVetoException prevents callin\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2236lp2_1.java",
			    "\n" +
			    "public team class Team2236lp2_1 {\n" +
			    "    public class RLower1 { public void bm() {} }\n" +
			    "    public class RLower2 extends RLower1 {}\n" +
			    "}\n" +
			    "    \n",
		"lifter/T2236lp2Lifter.java",
			    "\n" +
			    "package lifter;\n" +
			    "import org.objectteams.ITeam;\n" +
			    "import java.lang.reflect.Constructor;\n" +
			    "public class T2236lp2Lifter implements org.objectteams.ILiftingParticipant {\n" +
			    "    int count = 1;\n" +
			    "    public Object createRole(ITeam teamInstance, Object base, String roleClassName) {\n" +
			    "        if (!roleClassName.startsWith(\"Team2236lp2_2$__OT__R\"))\n" +
			    "            System.err.println(\"wrong role class name \"+roleClassName);\n" +
			    "        try {\n" +
			    "            Class<?> rc = Class.forName(roleClassName);\n" +
			    "            Constructor<?>[] ctors = rc.getDeclaredConstructors();\n" +
			    "            for (Constructor<?> ctor : ctors)\n" +
			    "                if (ctor.getParameterTypes().length == 3)\n" +
			    "                    return ctor.newInstance(new Object[]{teamInstance, base, count++});\n" +
			    "\n" +
			    "        } catch (Exception e) {\n" +
			    "            e.printStackTrace();\n" +
			    "        }\n" +
			    "        return null;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "123340",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            new String[]{ "-Dot.lifting.participant=lifter.T2236lp2Lifter" }/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }
    public void test2237_instantiationPolicy1() {
    	runConformTest(
        	true/*flushOutputDir*/,
    		new String[] {
		"Team2237ip1.java",
				"import static org.objectteams.InstantiationPolicy.*;\n" +
				"import org.objectteams.Instantiation;\n" +
				"public team class Team2237ip1 {\n" +
				"    @Instantiation(ALWAYS)\n" +
				"    protected class R playedBy T2237ip1 {\n" +
				"        protected void test() -> void test();\n" +
				"		 equals => equals;\n" +
				"        hashCode => hashCode;\n" +
				"    }\n" +
				"    public void test(T2237ip1 as R o) {\n" +
				"        o.test();\n" +
				"    }\n" +
				"    public static void main(String... args) {\n" +
				"        Team2237ip1 t = new Team2237ip1();\n" +
				"        T2237ip1 b = new T2237ip1();\n" +
				"        t.test(b);\n" +
				"        t.test(b);\n" +
				"        System.out.print(t.getAllRoles().length);\n" +
				"    }\n" +
				"}\n",  
    	"T2237ip1.java",
    			"public class T2237ip1 {\n" +
    			"    void test() { System.out.print(\"OK\"); }\n" +
    			"}\n",
    		},
    		"", // explicitly check no warnings
    		"OKOK0",
    		"",
    		null/*no excuce*/);
    }
    public void test2237_instantiationPolicy2() {
    	runConformTest(
    		true/*flushOutputDir*/,
    		new String[] {
		"Team2237ip2.java",
				"import static org.objectteams.InstantiationPolicy.*;\n" +
				"import org.objectteams.Instantiation;\n" +
				"public team class Team2237ip2 {\n" +
				"    @Instantiation(ONDEMAND)\n" +
				"    protected class R playedBy T2237ip2 {\n" +
				"        protected void test() -> void test();\n" +
				"    }\n" +
				"    public void test(T2237ip2 as R o) {\n" +
				"        o.test();\n" +
				"    }\n" +
				"    public static void main(String... args) {\n" +
				"        Team2237ip2 t = new Team2237ip2();\n" +
				"        T2237ip2 b = new T2237ip2();\n" +
				"        t.test(b);\n" +
				"        t.test(b);\n" +
				"        System.out.print(t.getAllRoles().length);\n" +
				"    }\n" +
				"}\n",  
    	"T2237ip2.java",
    			"public class T2237ip2 {\n" +
    			"    void test() { System.out.print(\"OK\"); }\n" +
    			"}\n",
    		},
    		"",
    		"OKOK1",
    		"",
    		null/*no excuce*/);
    }

    public void test2237_instantiationPolicy3() {
    	runNegativeTest(
    		new String[] {
		"NotATeam2237ip3.java",
				"import static org.objectteams.InstantiationPolicy.*;\n" +
				"import org.objectteams.Instantiation;\n" +
				"public class NotATeam2237ip3 {\n" +
				"    @Instantiation(ONDEMAND)\n" +
				"    protected class R {\n" +
				"    }\n" +
				"}\n",  
    		},
    		"----------\n" + 
    		"1. ERROR in NotATeam2237ip3.java (at line 4)\n" + 
    		"	@Instantiation(ONDEMAND)\n" + 
    		"	^^^^^^^^^^^^^^\n" + 
    		"Annotation \'@Instantiation\' can only be applied to role classes (OTJLD 2.3.1(d)).\n" + 
    		"----------\n");
    }
    
    public void test2237_instantiationPolicy4() {
    	runNegativeTest(
    		new String[] {
		"Team2237ip4.java",
				"import static org.objectteams.InstantiationPolicy.*;\n" +
				"import org.objectteams.Instantiation;\n" +
				"public team class Team2237ip4 {\n" +
				"    @Instantiation(ALWAYS)\n" +
				"    protected class R {\n" +
				"        int val = 0;\n" +
				"    }\n" +
				"}\n",  
    		},
    		"----------\n" + 
    		"1. WARNING in Team2237ip4.java (at line 5)\n" + 
    		"	protected class R {\n" + 
    		"	                ^\n" + 
    		"Roles with InstantiationPolicy \'ALWAYS\' should define equals() and hashCode() methods (OTJLD 2.3.1(d)).\n" + 
    		"----------\n" + 
    		"2. WARNING in Team2237ip4.java (at line 6)\n" + 
    		"	int val = 0;\n" + 
    		"	    ^^^\n" + 
    		"Fields are discouraged in roles with InstantiationPolicy \'ALWAYS\' (OTJLD 2.3.1(d)).\n" + 
    		"----------\n");
    }

}
