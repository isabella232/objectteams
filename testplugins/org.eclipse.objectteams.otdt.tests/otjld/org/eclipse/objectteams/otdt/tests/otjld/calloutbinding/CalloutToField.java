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
package org.eclipse.objectteams.otdt.tests.otjld.calloutbinding;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

@SuppressWarnings("unchecked")
public class CalloutToField extends AbstractOTJLDTest {
	
	public CalloutToField(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test3319_calloutToArrayField1f"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return CalloutToField.class;
	}


    // a getter callout
    // 3.3.1-otjld-callout-get-1
    public void test331_calloutGet1() {
       
       runConformTest(
            new String[] {
		"Team331cg1.java",
			    "\n" +
			    "public team class Team331cg1 {\n" +
			    "	protected class Role playedBy T331cg1 {\n" +
			    "		abstract String getValue();\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(getValue()+getMore());\n" +
			    "		}\n" +
			    "		getValue -> get value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team331cg1 () {\n" +
			    "		Role r = new Role(new T331cg1());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg1();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg1.java",
			    "\n" +
			    "public class T331cg1 {\n" +
			    "	String value = \"O\";\n" +
			    "	public String getMore() { return \"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a getter callout to a static field
    // 3.3.1-otjld-callout-get-2
    public void test331_calloutGet2() {
       
       runConformTest(
            new String[] {
		"Team331cg2.java",
			    "\n" +
			    "public team class Team331cg2 {\n" +
			    "	protected class Role playedBy T331cg2 {\n" +
			    "		abstract String getValue();\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(getValue()+getMore());\n" +
			    "		}\n" +
			    "		getValue -> get value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team331cg2 () {\n" +
			    "		Role r = new Role(new T331cg2());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg2();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg2.java",
			    "\n" +
			    "public class T331cg2 {\n" +
			    "	static String value = \"O\";\n" +
			    "	public String getMore() { return \"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a static getter callout to a static field
    // 3.3.1-otjld-callout-get-2s
    public void test331_calloutGet2s() {
       
       runConformTest(
            new String[] {
		"Team331cg2s.java",
			    "\n" +
			    "public team class Team331cg2s {\n" +
			    "	protected class Role playedBy T331cg2s {\n" +
			    "		static abstract String getValue();\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(getValue()+getMore());\n" +
			    "		}\n" +
			    "		getValue -> get value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team331cg2s () {\n" +
			    "		Role r = new Role(new T331cg2s());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg2s();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg2s.java",
			    "\n" +
			    "public class T331cg2s {\n" +
			    "	static String value = \"O\";\n" +
			    "	public String getMore() { return \"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a getter callout to an inherited field
    // 3.3.1-otjld-callout-get-3
    public void test331_calloutGet3() {
       
       runConformTest(
            new String[] {
		"Team331cg3.java",
			    "\n" +
			    "public team class Team331cg3 {\n" +
			    "	protected class Role playedBy T331cg3 {\n" +
			    "		abstract String getValue();\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(getValue()+getMore());\n" +
			    "		}\n" +
			    "		getValue -> get value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team331cg3 () {\n" +
			    "		Role r = new Role(new T331cg3());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg3();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg3super.java",
			    "\n" +
			    "public class T331cg3super {\n" +
			    "	String value = \"O\";\n" +
			    "}\n" +
			    "	\n",
		"T331cg3.java",
			    "\n" +
			    "public class T331cg3 extends T331cg3super {\n" +
			    "	public String getMore() { return \"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a getter callout to a non existing field
    // 3.3.1-otjld-callout-get-4
    public void test331_calloutGet4() {
        runNegativeTest(
            new String[] {
		"Team331cg4.java",
			    "\n" +
			    "public team class Team331cg4 {\n" +
			    "	protected class Role playedBy T331cg4 {\n" +
			    "		abstract String getValue();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(getValue());\n" +
			    "		}\n" +
			    "		getValue -> get value;\n" +
			    "	}\n" +
			    "	public Team331cg4 () {\n" +
			    "		Role r = new Role(new T331cg4());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg4();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg4.java",
			    "\n" +
			    "public class T331cg4 {\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
			"1. ERROR in Team331cg4.java (at line 8)\n" + 
			"	getValue -> get value;\n" + 
			"	                ^^^^^\n" + 
			"Callout binding cannot resolve field value in type T331cg4 (OTJLD 3.5).\n" + 
			"----------\n");
    }

    // a getter callout with wrong role method return type
    // 3.3.1-otjld-callout-get-5
    public void test331_calloutGet5() {
        runNegativeTest(
            new String[] {
		"Team331cg5.java",
			    "\n" +
			    "public team class Team331cg5 {\n" +
			    "	protected class Role playedBy T331cg5 {\n" +
			    "		abstract int getValue();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(getValue());\n" +
			    "		}\n" +
			    "		getValue -> get value;\n" +
			    "	}\n" +
			    "	public Team331cg5 () {\n" +
			    "		Role r = new Role(new T331cg5());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg5();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg5.java",
			    "\n" +
			    "public class T331cg5 {\n" +
			    "	String value = \"O\";\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
			"1. ERROR in Team331cg5.java (at line 8)\n" + 
			"	getValue -> get value;\n" + 
			"	^^^^^^^^\n" + 
			"When binding field value via callout to role method getValue():\n" + 
			"Incompatible types: can\'t convert java.lang.String to int (OTJLD 3.5(b)).\n" + 
			"----------\n");
    }

    // a getter callout with wrong role method return type
    // 3.3.1-otjld-callout-get-5b
    public void test331_calloutGet5b() {
        runNegativeTest(
            new String[] {
		"Team331cg5b.java",
			    "\n" +
			    "public team class Team331cg5b {\n" +
			    "	protected class Role playedBy T331cg5b {\n" +
			    "		abstract int getValue();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(getValue());\n" +
			    "		}\n" +
			    "		int getValue() -> get String value;\n" +
			    "	}\n" +
			    "	public Team331cg5b () {\n" +
			    "		Role r = new Role(new T331cg5b());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg5b();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg5b.java",
			    "\n" +
			    "public class T331cg5b {\n" +
			    "	String value = \"O\";\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team331cg5b.java (at line 8)\n" + 
    		"	int getValue() -> get String value;\n" + 
    		"	^^^^^^^^^^^^^^\n" + 
    		"When binding field value via callout to role method getValue():\n" + 
    		"Incompatible types: can\'t convert java.lang.String to int (OTJLD 3.5(b)).\n" + 
    		"----------\n");
    }

    // a getter callout with wrong role method return type and wrong field spec type
    public void test331_calloutGet5c() {
        runNegativeTest(
            new String[] {
		"Team331cg5c.java",
			    "\n" +
			    "public team class Team331cg5c {\n" +
			    "	protected class Role playedBy T331cg5c {\n" +
			    "		abstract int getValue();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(getValue());\n" +
			    "		}\n" +
			    "		int getValue() -> get int value;\n" +
			    "	}\n" +
			    "	public Team331cg5c () {\n" +
			    "		Role r = new Role(new T331cg5c());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg5c();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg5c.java",
			    "\n" +
			    "public class T331cg5c {\n" +
			    "	String value = \"O\";\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team331cg5c.java (at line 8)\n" + 
    		"	int getValue() -> get int value;\n" + 
    		"	                      ^^^\n" + 
    		"Field specifier \'value\' resolves to type java.lang.String whereas type int is specified (OTJLD 3.5(a)).\n" + 
    		"----------\n");
    }

    // a getter callout with role method return type void
    // 3.3.1-otjld-callout-get-6
    public void test331_calloutGet6() {
       
       runConformTest(
            new String[] {
		"T331cg6Main.java",
			    "\n" +
			    "public class T331cg6Main {\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team331cg6();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg6.java",
			    "\n" +
			    "public class T331cg6 {\n" +
			    "	String value = \"O\";\n" +
			    "}\n" +
			    "	\n",
		"Team331cg6.java",
			    "\n" +
			    "public team class Team331cg6 {\n" +
			    "	protected class Role playedBy T331cg6 {\n" +
			    "		abstract void getValue();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(\"Calling effectless method...\");\n" +
			    "			getValue();\n" +
			    "		}\n" +
			    "		getValue -> get value;\n" +
			    "	}\n" +
			    "	public Team331cg6 () {\n" +
			    "		Role r = new Role(new T331cg6());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "Calling effectless method...");
    }

    // a getter callout with parameter mappings
    // 3.3.1-otjld-callout-get-7
    public void test331_calloutGet7() {
       
       runConformTest(
            new String[] {
		"Team331cg7.java",
			    "\n" +
			    "public team class Team331cg7 {\n" +
			    "	protected class Role playedBy T331cg7 {\n" +
			    "		abstract Integer getValue();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(getValue());\n" +
			    "		}\n" +
			    "		Integer getValue() -> get int value with {\n" +
			    "			result <- new Integer(value)\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public Team331cg7 () {\n" +
			    "		Role r = new Role(new T331cg7());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg7();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg7.java",
			    "\n" +
			    "public class T331cg7 {\n" +
			    "	int value = 77;\n" +
			    "}\n" +
			    "	\n"
            },
            "77");
    }

    // a getter callout to a basic type field
    // 3.3.1-otjld-callout-get-8
    public void test331_calloutGet8() {
       
       runConformTest(
            new String[] {
		"Team331cg8.java",
			    "\n" +
			    "public team class Team331cg8 {\n" +
			    "	protected class Role playedBy T331cg8 {\n" +
			    "		abstract char getValue();\n" +
			    "		abstract char getMore();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(new Character(getValue()).toString()\n" +
			    "			                +new Character(getMore()).toString());\n" +
			    "		}\n" +
			    "		getValue -> get value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team331cg8 () {\n" +
			    "		Role r = new Role(new T331cg8());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg8();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg8.java",
			    "\n" +
			    "public class T331cg8 {\n" +
			    "	char value = 'O';\n" +
			    "	public char getMore() { return 'K'; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a getter callout to a base type with a role in this team (role is NOT public)
    // 3.3.1-otjld-callout-get-9
    public void test331_calloutGet9() {
       
       runConformTest(
            new String[] {
		"Team331cg9.java",
			    "\n" +
			    "public team class Team331cg9 {\n" +
			    "	protected class Role playedBy T331cg9 {\n" +
			    "		abstract Role getRole();\n" +
			    "		@SuppressWarnings(\"ambiguouslowering\")\n" +
			    "		protected void test() {\n" +
			    "			if (getRole().equals(this))\n" +
			    "				System.out.print(\"OK\");\n" +
			    "			else \n" +
			    "				System.out.print(\"NOTOK\");\n" +
			    "		}\n" +
			    "		getRole -> get me;\n" +
			    "	}\n" +
			    "	public Team331cg9 () {\n" +
			    "		Role r = new Role(new T331cg9());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg9();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg9.java",
			    "\n" +
			    "public class T331cg9 {\n" +
			    "	T331cg9 me;\n" +
			    "\n" +
			    "	public T331cg9() {\n" +
			    "		me = this;\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a normal callout to a base type with a role in this team
    // 3.3.1-otjld-callout-get-9a
    public void test331_calloutGet9a() {
       
       runConformTest(
            new String[] {
		"Team331cg9a.java",
			    "\n" +
			    "public team class Team331cg9a {\n" +
			    "	public class Role playedBy T331cg9a {\n" +
			    "		abstract Role getRole();\n" +
			    "		@SuppressWarnings(\"ambiguouslowering\")\n" +
			    "		protected void test() {\n" +
			    "			if (getRole().equals(this))\n" +
			    "				System.out.print(\"OK\");\n" +
			    "			else \n" +
			    "				System.out.print(\"NOTOK\");\n" +
			    "		}\n" +
			    "		getRole -> getMe;\n" +
			    "	}\n" +
			    "	public Team331cg9a () {\n" +
			    "		Role r = new Role(new T331cg9a());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg9a();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg9a.java",
			    "\n" +
			    "public class T331cg9a {\n" +
			    "	T331cg9a me;\n" +
			    "	\n" +
			    "	public T331cg9a() {\n" +
			    "		me = this;\n" +
			    "	}\n" +
			    "	public T331cg9a getMe() {\n" +
			    "		return me;\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a getter callout to a base type with a role in this team (role is public)
    // 3.3.1-otjld-callout-get-9b
    public void test331_calloutGet9b() {
       
       runConformTest(
            new String[] {
		"Team331cg9b.java",
			    "\n" +
			    "public team class Team331cg9b {\n" +
			    "	public class Role playedBy T331cg9b {\n" +
			    "		abstract Role getRole();\n" +
			    "		\n" +
			    "		protected void test() {\n" +
			    "			if (getRole().equals((Object)this)) // signal that we don't want lowering\n" +
			    "				System.out.print(\"OK\");\n" +
			    "			else \n" +
			    "				System.out.print(\"NOTOK\");\n" +
			    "		}\n" +
			    "		getRole -> get me;\n" +
			    "	}\n" +
			    "	public Team331cg9b () {\n" +
			    "		Role r = new Role(new T331cg9b());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg9b();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg9b.java",
			    "\n" +
			    "public class T331cg9b {\n" +
			    "	T331cg9b me;\n" +
			    "\n" +
			    "	public T331cg9b() {\n" +
			    "		me = this;\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // two getter callouts from one role
    // 3.3.1-otjld-callout-get-10
    public void test331_calloutGet10() {
       
       runConformTest(
            new String[] {
		"Team331cg10.java",
			    "\n" +
			    "public team class Team331cg10 {\n" +
			    "	protected class Role playedBy T331cg10 {\n" +
			    "		abstract String getValue();\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(getValue()+getMore());\n" +
			    "		}\n" +
			    "		getValue -> get value;\n" +
			    "		getMore -> get more;\n" +
			    "	}\n" +
			    "	public Team331cg10 () {\n" +
			    "		Role r = new Role(new T331cg10());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg10();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg10.java",
			    "\n" +
			    "public class T331cg10 {\n" +
			    "	String value = \"O\";\n" +
			    "	String more = \"K\"; \n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a getter callout with wrong signature
    // 3.3.1-otjld-callout-get-11
    public void test331_calloutGet11() {
       
       runConformTest(
            new String[] {
		"Team331cg11.java",
			    "\n" +
			    "public team class Team331cg11 {\n" +
			    "	protected class Role playedBy T331cg11 {\n" +
			    "		abstract String getValue(int i);\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			System.out.print(getValue(1)+getMore());\n" +
			    "		}\n" +
			    "		getValue -> get value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team331cg11 () {\n" +
			    "		Role r = new Role(new T331cg11());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team331cg11();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T331cg11.java",
			    "\n" +
			    "public class T331cg11 {\n" +
			    "	String value = \"O\";\n" +
			    "	public String getMore() { return \"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a getter callout with broken parameter mapping (saw NPE in MessageSend:593)
    // 3.3.2-otjld-callout-get-12
    public void test332_calloutGet12() {
        runNegativeTestMatching(
            new String[] {
		"Team332cg12.java",
			    "\n" +
			    "public team class Team332cg12 {\n" +
			    "    protected class R playedBy T332cg12 {\n" +
			    "        boolean getFlag() -> get char[] name\n" +
			    "            with { result <- name.startsWith(\"bla\".toCharArray()) }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T332cg12.java",
			    "\n" +
			    "public class T332cg12 {\n" +
			    "    char[] name;\n" +
			    "}\n" +
			    "    \n"
            },
            "Cannot invoke");
    }

    // a getter callout to a static field uses abstract declaration and binding with signatures - witness for bogus ambiguous method message
    // 3.3.2-otjld-callout-get-13
    public void test332_calloutGet13() {
       
       runConformTest(
            new String[] {
		"Team332cg13.java",
			    "\n" +
			    "public team class Team332cg13 {\n" +
			    "    protected class R playedBy T332cg13 {\n" +
			    "        protected abstract static String getOk();\n" +
			    "        @SuppressWarnings(\"decapsulation\") String getOk() -> get String ok;\n" +
			    "    }\n" +
			    "    Team332cg13() {\n" +
			    "        System.out.print(R.getOk());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team332cg13();\n" +
			    "    }    \n" +
			    "}\n" +
			    "    \n",
		"T332cg13.java",
			    "\n" +
			    "public class T332cg13 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    static private String ok = \"OK\";\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a getter callout to a static field uses only a binding with signatures
    // 3.3.2-otjld-callout-get-13s
    public void test332_calloutGet13s() {
       
       runConformTest(
            new String[] {
		"Team332cg13s.java",
			    "\n" +
			    "public team class Team332cg13s {\n" +
			    "    protected class R playedBy T332cg13s {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        protected\n" +
			    "        String getOk() -> get String ok;\n" +
			    "    }\n" +
			    "    Team332cg13s() {\n" +
			    "        System.out.print(R.getOk());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team332cg13s();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T332cg13s.java",
			    "\n" +
			    "public class T332cg13s {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    static private String ok = \"OK\";\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a getter callout to a static field uses only a binding with signatures, field is visible, but class is not
    public void test332_calloutGet13sc() {
       
       runConformTest(
            new String[] {
		"p2/Team332cg13sc.java",
			    "package p2;\n" +
			    "import base p1.T332cg13sc;\n" +
			    "public team class Team332cg13sc {\n" +
			    "    @SuppressWarnings(\"decapsulation\")\n" +
			    "    protected class R playedBy T332cg13sc {\n" +
			    "        String getOk() -> get String ok;\n" +
			    "    }\n" +
			    "    Team332cg13sc() {\n" +
			    "        System.out.print(R.getOk());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team332cg13sc();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T332cg13sc.java",
			    "package p1;\n" +
			    "class T332cg13sc {\n" +
			    "    public static String ok = \"OK\";\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a getter callout to a static field uses only a binding with signatures, field is visible
    public void test332_calloutGet13sv() {
       
       runConformTest(
            new String[] {
		"Team332cg13sv.java",
			    "\n" +
			    "public team class Team332cg13sv {\n" +
			    "    protected class R playedBy T332cg13sv {\n" +
			    "        protected\n" +
			    "        String getOk() -> get String ok;\n" +
			    "    }\n" +
			    "    Team332cg13sv() {\n" +
			    "        System.out.print(R.getOk());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team332cg13sv();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T332cg13sv.java",
			    "\n" +
			    "public class T332cg13sv {\n" +
			    "    protected static String ok = \"OK\";\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a getter callout to a static field uses only a binding with signatures - not visible
    // 3.3.2-otjld-callout-get-13v
    public void test332_calloutGet13v() {
        runNegativeTestMatching(
            new String[] {
		"Team332cg13v.java",
			    "\n" +
			    "public team class Team332cg13v {\n" +
			    "    protected class R playedBy T332cg13v {\n" +
			    "        @SuppressWarnings(\"decapsulation\") String getOk() -> get String ok; // inherits private visibility\n" +
			    "    }\n" +
			    "    Team332cg13v() {\n" +
			    "        System.out.print(R.getOk()); // not visible\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T332cg13v.java",
			    "\n" +
			    "public class T332cg13v {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    static private String ok = \"OK\";\n" +
			    "}\n" +
			    "    \n"
            },
            "1.2.1(e)");
    }

    // a field spec incorrectly uses "void" as the field'd type
    // Bug 329888 -  Missing compiler error when using wrong field specifier in Callout to field (long)
    public void test332_calloutGet14() {
    	runNegativeTest(
    		new String[]{
    	"Team332cg14.java",
    			"public team class Team332cg14 {\n" +
    			"    protected class R playedBy T332cg14 {\n" +
    			"        String getF() -> get void f;\n" +
    			"    }\n" +
    			"}\n",
    	"T332cg14.java",
    			"public class T332cg14 {\n" +
    			"    String f;\n" +
    			"}\n"
    		}, 
    		"----------\n" + 
    		"1. ERROR in Team332cg14.java (at line 3)\n" + 
    		"	String getF() -> get void f;\n" + 
    		"	                     ^^^^\n" + 
    		"Field specifier \'f\' resolves to type java.lang.String whereas type void is specified (OTJLD 3.5(a)).\n" + 
    		"----------\n");
    }
    
    // a setter callout
    // 3.3.2-otjld-callout-set-1
    public void test332_calloutSet1() {
       
       runConformTest(
            new String[] {
		"Team332cs1.java",
			    "\n" +
			    "public team class Team332cs1 {\n" +
			    "	protected class Role playedBy T332cs1 {\n" +
			    "		abstract void setValue(String val);\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			setValue(\"O\");\n" +
			    "			System.out.print(getMore());\n" +
			    "		}\n" +
			    "		setValue -> set value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team332cs1 () {\n" +
			    "		Role r = new Role(new T332cs1());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs1();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs1.java",
			    "\n" +
			    "public class T332cs1 {\n" +
			    "	String value = \"X\";\n" +
			    "	public String getMore() { return value+\"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a setter callout to a static field
    // 3.3.2-otjld-callout-set-2
    public void test332_calloutSet2() {
       
       runConformTest(
            new String[] {
		"Team332cs2.java",
			    "\n" +
			    "public team class Team332cs2 {\n" +
			    "	protected class Role playedBy T332cs2 {\n" +
			    "		abstract void setValue(String val);\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			setValue(\"O\");\n" +
			    "			System.out.print(getMore());\n" +
			    "		}\n" +
			    "		setValue -> set value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team332cs2 () {\n" +
			    "		Role r = new Role(new T332cs2());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs2();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs2.java",
			    "\n" +
			    "public class T332cs2 {\n" +
			    "	static String value = \"X\";\n" +
			    "	public String getMore() { return value+\"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a static setter callout to a static field
    // 3.3.2-otjld-callout-set-2s
    public void test332_calloutSet2s() {
       
       runConformTest(
            new String[] {
		"Team332cs2s.java",
			    "\n" +
			    "public team class Team332cs2s {\n" +
			    "	protected class Role playedBy T332cs2s {\n" +
			    "		static abstract void setValue(String val);\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			setValue(\"O\");\n" +
			    "			System.out.print(getMore());\n" +
			    "		}\n" +
			    "		setValue -> set value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team332cs2s () {\n" +
			    "		Role r = new Role(new T332cs2s());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs2s();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs2s.java",
			    "\n" +
			    "public class T332cs2s {\n" +
			    "	static String value = \"X\";\n" +
			    "	public String getMore() { return value+\"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a static setter callout to a static field (shorthand)
    // 3.3.2-otjld-callout-set-2s2
    public void test332_calloutSet2s2() {
       
       runConformTest(
            new String[] {
		"Team332cs2s2.java",
			    "\n" +
			    "public team class Team332cs2s2 {\n" +
			    "	protected class Role playedBy T332cs2s2 {\n" +
			    "		static abstract String getMore();\n" +
			    "		protected static void test() {\n" +
			    "			setValue(\"O\");\n" +
			    "			System.out.print(getMore());\n" +
			    "		}\n" +
			    "		void setValue(String value) -> set String value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team332cs2s2 () {\n" +
			    "		Role.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs2s2();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs2s2.java",
			    "\n" +
			    "public class T332cs2s2 {\n" +
			    "	static String value = \"X\";\n" +
			    "	public static String getMore() { return value+\"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a setter callout to an inherited field
    // 3.3.2-otjld-callout-set-3
    public void test332_calloutSet3() {
       
       runConformTest(
            new String[] {
		"Team332cs3.java",
			    "\n" +
			    "public team class Team332cs3 {\n" +
			    "	protected class Role playedBy T332cs3 {\n" +
			    "		abstract void setValue(String val);\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			setValue(\"O\");\n" +
			    "			System.out.print(getMore());\n" +
			    "		}\n" +
			    "		setValue -> set value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team332cs3 () {\n" +
			    "		Role r = new Role(new T332cs3());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs3();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs3super.java",
			    "\n" +
			    "public class T332cs3super {\n" +
			    "	String value = \"X\";\n" +
			    "}\n" +
			    "	\n",
		"T332cs3.java",
			    "\n" +
			    "public class T332cs3 extends T332cs3super {\n" +
			    "	public String getMore() { return value+\"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a setter callout to an inherited private field
    // 3.3.2-otjld-callout-set-3f
    public void test332_calloutSet3f() {
        runNegativeTestMatching(
            new String[] {
		"Team332sc3f.java",
			    "\n" +
			    "public team class Team332sc3f {\n" +
			    "	protected class Role playedBy T332sc3f {\n" +
			    "		abstract void setValue(String val);\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			setValue(\"O\");\n" +
			    "			System.out.print(getMore());\n" +
			    "		}\n" +
			    "		setValue -> set value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team332sc3f () {\n" +
			    "		Role r = new Role(new T332sc3f());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332sc3f();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332sc3fsuper.java",
			    "\n" +
			    "public class T332sc3fsuper {\n" +
			    "	private String value = \"X\";\n" +
			    "        @SuppressWarnings(\"unused\")\n" +
			    "	private String getMore() { return value+\"K\"; }\n" +
			    "}\n" +
			    "	\n",
		"T332sc3f.java",
			    "\n" +
			    "public class T332sc3f extends T332sc3fsuper {\n" +
			    "}\n" +
			    "	\n"
            },
            "3.4(d)");
    }

    // a setter callout to a non existing field
    // 3.3.2-otjld-callout-set-4
    public void test332_calloutSet4() {
        runNegativeTest(
            new String[] {
		"Team332cs4.java",
			    "\n" +
			    "public team class Team332cs4 {\n" +
			    "	protected class Role playedBy T332cs4 {\n" +
			    "		abstract void setValue(String val);\n" +
			    "		protected void test() {\n" +
			    "			setValue(\"OK\");\n" +
			    "		}\n" +
			    "		setValue -> set value;\n" +
			    "	}\n" +
			    "	public Team332cs4 () {\n" +
			    "		Role r = new Role(new T332cs4());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs4();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs4.java",
			    "\n" +
			    "public class T332cs4 {\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team332cs4.java (at line 8)\n" + 
    		"	setValue -> set value;\n" + 
    		"	                ^^^^^\n" + 
    		"Callout binding cannot resolve field value in type T332cs4 (OTJLD 3.5).\n" + 
    		"----------\n");
    }

    // a setter callout with wrong role method argument type
    // 3.3.2-otjld-callout-set-5
    public void test332_calloutSet5() {
        runNegativeTest(
            new String[] {
		"Team332cs5.java",
			    "\n" +
			    "public team class Team332cs5 {\n" +
			    "	protected class Role playedBy T332cs5 {\n" +
			    "		abstract void setValue(int val);\n" +
			    "		protected void test() {\n" +
			    "			setValue(77);\n" +
			    "		}\n" +
			    "		setValue -> set value;\n" +
			    "	}\n" +
			    "	public Team332cs5 () {\n" +
			    "		Role r = new Role(new T332cs5());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs5();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs5.java",
			    "\n" +
			    "public class T332cs5 {\n" +
			    "	String value = \"X\";\n" +
			    "	public String getMore() { return value+\"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team332cs5.java (at line 8)\n" + 
    		"	setValue -> set value;\n" + 
    		"	^^^^^^^^\n" + 
    		"When binding role method setValue(int) via callout to field value:\n" + 
    		"Incompatible types: can\'t convert int to java.lang.String (OTJLD 3.5(b)).\n" + 
    		"----------\n");
    }

    // a setter callout with wrong role method argument type
    // 3.3.2-otjld-callout-set-5b
    public void test332_calloutSet5b() {
        runNegativeTest(
            new String[] {
		"Team332cs5b.java",
			    "\n" +
			    "public team class Team332cs5b {\n" +
			    "	protected class Role playedBy T332cs5b {\n" +
			    "		abstract void setValue(int val);\n" +
			    "		protected void test() {\n" +
			    "			setValue(77);\n" +
			    "		}\n" +
			    "		void setValue(int val) -> set int value;\n" +
			    "	}\n" +
			    "	public Team332cs5b () {\n" +
			    "		Role r = new Role(new T332cs5b());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs5b();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs5b.java",
			    "\n" +
			    "public class T332cs5b {\n" +
			    "	String value = \"X\";\n" +
			    "	public String getMore() { return value+\"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team332cs5b.java (at line 8)\n" + 
    		"	void setValue(int val) -> set int value;\n" + 
    		"	                                  ^^^^^\n" + 
    		"Field specifier \'value\' resolves to type java.lang.String whereas type int is specified (OTJLD 3.5(a)).\n" + 
    		"----------\n");
    }

    // a setter callout with no parameters
    // 3.3.2-otjld-callout-set-6
    public void test332_calloutSet6() {
        runNegativeTest(
            new String[] {
		"Team332cs6.java",
			    "\n" +
			    "public team class Team332cs6 {\n" +
			    "	protected class Role playedBy T332cs6 {\n" +
			    "		abstract void setValue();\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			setValue();\n" +
			    "			System.out.print(getMore());\n" +
			    "		}\n" +
			    "       @SuppressWarnings(\"decapsulation\")\n" +
			    "		setValue -> set value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team332cs6 () {\n" +
			    "		Role r = new Role(new T332cs6());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs6();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs6.java",
			    "\n" +
			    "public class T332cs6 {\n" +
			    "	String value = \"X\";\n" +
			    "	public String getMore() { return value+\"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team332cs6.java (at line 11)\n" + 
    		"	setValue -> set value;\n" + 
    		"	^^^^^^^^\n" + 
    		"Cannot bind role method setValue() to field value:\n" + 
    		"Missing parameter of type java.lang.String (OTJLD 3.5(b)).\n" + 
    		"----------\n");
    }

    // a setter callout with parameter mappings
    // 3.3.2-otjld-callout-set-7
    public void test332_calloutSet7() {
       
       runConformTest(
            new String[] {
		"Team332cs7.java",
			    "\n" +
			    "public team class Team332cs7 {\n" +
			    "	protected class Role playedBy T332cs7 {\n" +
			    "		abstract void setValue(Integer val);\n" +
			    "		abstract String getValue();\n" +
			    "		protected void test() {\n" +
			    "			setValue(new Integer(77));\n" +
			    "			System.out.print(getValue());\n" +
			    "		}\n" +
			    "		void setValue(Integer val) -> set int value with {\n" +
			    "			val.intValue() -> value\n" +
			    "		}\n" +
			    "		getValue -> getValue;\n" +
			    "	}\n" +
			    "	public Team332cs7 () {\n" +
			    "		Role r = new Role(new T332cs7());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs7();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs7.java",
			    "\n" +
			    "public class T332cs7 {\n" +
			    "	int value = 88;\n" +
			    "	public String getValue() { return Integer.toString(value); }\n" +
			    "}\n" +
			    "	\n"
            },
            "77");
    }

    // a setter callout to a basic type field
    // 3.3.2-otjld-callout-set-8
    public void test332_calloutSet8() {
       
       runConformTest(
            new String[] {
		"Team332cs8.java",
			    "\n" +
			    "public team class Team332cs8 {\n" +
			    "	protected class Role playedBy T332cs8 {\n" +
			    "		abstract void setValue(char val);\n" +
			    "		abstract String getMore();\n" +
			    "		protected void test() {\n" +
			    "			setValue('O');\n" +
			    "			System.out.print(getMore());\n" +
			    "		}\n" +
			    "		setValue -> set value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "	public Team332cs8 () {\n" +
			    "		Role r = new Role(new T332cs8());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs8();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs8.java",
			    "\n" +
			    "public class T332cs8 {\n" +
			    "	char value = 'X';\n" +
			    "	public String getMore() { return new Character(value).toString()+\"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a setter callout to a base type with a role in this team
    // 3.3.2-otjld-callout-set-9
    public void test332_calloutSet9() {
       
       runConformTest(
            new String[] {
		"Team332cs9.java",
			    "\n" +
			    "public team class Team332cs9 {\n" +
			    "	protected class Role playedBy T332cs9 {\n" +
			    "		abstract void setRole(Role r);\n" +
			    "		abstract Role getRole();\n" +
			    "		\n" +
			    "		protected void test(Role r) {\n" +
			    "			setRole(r);\n" +
			    "			System.out.print(getRole().toString());\n" +
			    "		}\n" +
			    "		setRole -> set me;\n" +
			    "		getRole -> getMe;\n" +
			    "		toString => toString;\n" +
			    "	}\n" +
			    "	public Team332cs9 () {\n" +
			    "		Role r1 = new Role(new T332cs9(\"b1\"));\n" +
			    "		Role r2 = new Role(new T332cs9(\"b2\"));\n" +
			    "		r1.test(r2);\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team332cs9();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs9.java",
			    "\n" +
			    "public class T332cs9 {\n" +
			    "	String name;\n" +
			    "	T332cs9 me;\n" +
			    "	public T332cs9(String n) {\n" +
			    "		name = n;\n" +
			    "		me = this;\n" +
			    "	}\n" +
			    "	public T332cs9 getMe() {\n" +
			    "		return me;\n" +
			    "	}\n" +
			    "	public String toString() {\n" +
			    "		return name;\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "b2");
    }

    // a setter callout with a return value
    // 3.3.2-otjld-callout-set-11
    public void test332_calloutSet11() {
        runNegativeTestMatching(
            new String[] {
		"Team332cs11.java",
			    "\n" +
			    "public team class Team332cs11 {\n" +
			    "	protected class Role playedBy T332cs11 {\n" +
			    "		abstract int setValue(String val);\n" +
			    "		abstract String getMore();\n" +
			    "		void test() {\n" +
			    "			setValue(\"O\");\n" +
			    "			System.out.print(getMore());\n" +
			    "		}\n" +
			    "		setValue -> set value;\n" +
			    "		getMore -> getMore;\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T332cs11.java",
			    "\n" +
			    "public class T332cs11 {\n" +
			    "	String value = \"X\";\n" +
			    "	public String getMore() { return value+\"K\"; }\n" +
			    "}\n" +
			    "	\n"
            },
            "3.5");
    }

    // a getter and a setter callout to the same field
    // 3.3.12-otjld-callout-get-and-set-1
    public void test3312_calloutGetAndSet1() {
       
       runConformTest(
            new String[] {
		"Team3312cgs1.java",
			    "\n" +
			    "public team class Team3312cgs1 {\n" +
			    "	protected class Role playedBy T3312cgs1 {\n" +
			    "		abstract void setValue(String val);\n" +
			    "		abstract String getValue();\n" +
			    "		protected void test() {\n" +
			    "			setValue(\"OK\");\n" +
			    "			System.out.print(getValue());\n" +
			    "		}\n" +
			    "		setValue -> set value;\n" +
			    "		getValue -> get value;\n" +
			    "	}\n" +
			    "	public Team3312cgs1 () {\n" +
			    "		Role r = new Role(new T3312cgs1());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team3312cgs1();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T3312cgs1.java",
			    "\n" +
			    "public class T3312cgs1 {\n" +
			    "	String value = \"XK\";\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a getter and a setter callout to different fields
    // 3.3.12-otjld-callout-get-and-set-2
    public void test3312_calloutGetAndSet2() {
       
       runConformTest(
            new String[] {
		"Team3312cgs2.java",
			    "\n" +
			    "public team class Team3312cgs2 {\n" +
			    "	protected class Role playedBy T3312cgs2 {\n" +
			    "		abstract void setFirst(String val);\n" +
			    "		abstract String getFirst();\n" +
			    "		abstract String getSecond();\n" +
			    "		protected void test() {\n" +
			    "			setFirst(\"O\");\n" +
			    "			System.out.print(getFirst()+getSecond());\n" +
			    "		}\n" +
			    "		setFirst -> set first;\n" +
			    "		getFirst -> getFirst;\n" +
			    "		getSecond -> get second;\n" +
			    "	}\n" +
			    "	public Team3312cgs2 () {\n" +
			    "		Role r = new Role(new T3312cgs2());\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team3312cgs2();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T3312cgs2.java",
			    "\n" +
			    "public class T3312cgs2 {\n" +
			    "	String first = \"X\";\n" +
			    "	String second = \"K\";\n" +
			    "	public String getFirst() { return first; } \n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a role is played by an anchored type, using callout to field
    // 3.3.13-otjld-callout-to-field-of-anchored-1
    public void test3313_calloutToFieldOfAnchored1() {
       
       runConformTest(
            new String[] {
		"Team3313ctfoa1_2.java",
			    "\n" +
			    "public team class Team3313ctfoa1_2 {\n" +
			    "    final Team3313ctfoa1_1 a = new Team3313ctfoa1_1(); \n" +
			    "    public class RoleB playedBy RoleA<@a> {\n" +
			    "       protected abstract String getVal();\n" +
			    "       @SuppressWarnings(\"decapsulation\")\n" +
			    "       getVal -> get val; \n" +
			    "    } \n" +
			    "    public Team3313ctfoa1_2() {\n" +
			    "        test(a.getRole());\n" +
			    "    }\n" +
			    "    public void test(RoleA<@a> as RoleB arg) {\n" +
			    "        System.out.print(arg.getVal());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3313ctfoa1_2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team3313ctfoa1_1.java",
			    "\n" +
			    "public team class Team3313ctfoa1_1 {\n" +
			    "    public class RoleA {\n" +
			    "        String val = \"OK\";\n" +
			    "    }\n" +
			    "    public RoleA getRole() {\n" +
			    "        return new RoleA();\n" +
			    "    }\n" +
			    "}        \n" +
			    "    \n"
            },
            "OK");
    }

    // a role is played by an anchored type, using callout to field
    // 3.3.13-otjld-callout-to-field-of-anchored-1b
    public void test3313_calloutToFieldOfAnchored1b() {
       
       runConformTest(
            new String[] {
		"Team3313ctfoa1b_2.java",
			    "\n" +
			    "public team class Team3313ctfoa1b_2 {\n" +
			    "    final Team3313ctfoa1b_1 a = new Team3313ctfoa1b_1(); \n" +
			    "    public class RoleB playedBy RoleA<@a> { \n" +
			    "       protected abstract String getVal(); \n" +
			    "       @SuppressWarnings(\"decapsulation\")\n" +
			    "       getVal -> get val; \n" +
			    "    } \n" +
			    "    public Team3313ctfoa1b_2() {\n" +
			    "        test(a.getRole());\n" +
			    "    }\n" +
			    "    public void test(RoleA<@a> as RoleB arg) {\n" +
			    "        System.out.print(arg.getVal());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3313ctfoa1b_2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team3313ctfoa1b_1.java",
			    "\n" +
			    "public team class Team3313ctfoa1b_1 {\n" +
			    "    public class RoleA {\n" +
			    "        String val = \"OK\";\n" +
			    "    }\n" +
			    "    public RoleA getRole() {\n" +
			    "        return new RoleA();\n" +
			    "    }\n" +
			    "}        \n" +
			    "    \n"
            },
            "OK");
    }

    // the anchor used in a callout to field is read from class file 
    // WITNESS for TPX-251 - fixed between 0.8.0 and 0.8.1
    // 3.3.13-otjld-callout-to-field-of-anchored-2
    public void test3313_calloutToFieldOfAnchored2() {
       
       runConformTest(
            new String[] {
		"Team3313ctfoa2_4.java",
			    "\n" +
			    " public team class Team3313ctfoa2_4 { \n" +
			    "    final Team3313ctfoa2_1 t1 = new Team3313ctfoa2_1(); \n" +
			    "    public class Role4 playedBy Role2<@t1> {\n" +
			    "       final Team3313ctfoa2_3 t43 = t1.t23; \n" +
			    "       protected abstract void m(Role3<@t43> r3);\n" +
			    "       @SuppressWarnings(\"decapsulation\")\n" +
			    "       m -> set field; \n" +
			    "       void test() -> void test();\n" +
			    "       public void setAndTest() {\n" +
			    "            Role3<@t43> r3 = t43.new Role3();\n" +
			    "            m(r3);\n" +
			    "            test();\n" +
			    "       }\n" +
			    "    } \n" +
			    "    public Team3313ctfoa2_4() {\n" +
			    "        test(t1.getRole());\n" +
			    "    }\n" +
			    "    public void test(Role2<@t1> as Role4 r) {\n" +
			    "        r.setAndTest();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3313ctfoa2_4();\n" +
			    "    }\n" +
			    " } \n" +
			    "    \n",
		"Team3313ctfoa2_3.java",
			    "\n" +
			    " public team class Team3313ctfoa2_3 { \n" +
			    "    public class Role3 {} \n" +
			    " } \n" +
			    "    \n",
		"Team3313ctfoa2_2.java",
			    "\n" +
			    " public team class Team3313ctfoa2_2 { \n" +
			    "    final Team3313ctfoa2_3 t23 = new Team3313ctfoa2_3(); \n" +
			    "    public class Role2 { \n" +
			    "       Role3<@t23> field;\n" +
			    "       public void test() {\n" +
			    "           if (field != null) \n" +
			    "               System.out.print(\"OK\");\n" +
			    "           else\n" +
			    "               System.out.print(\"NOK\");\n" +
			    "       }\n" +
			    "    } \n" +
			    "    public Role2 getRole() { return new Role2(); }\n" +
			    " }\n" +
			    "    \n",
		"Team3313ctfoa2_1.java",
			    "\n" +
			    " public team class Team3313ctfoa2_1 extends Team3313ctfoa2_2 { } \n" +
			    "    \n"
            },
            "OK");
    }

    // the anchor used in a callout to field is read from class file, same as above but new role type syntax
    // 3.3.13-otjld-callout-to-field-of-anchored-2s
    public void test3313_calloutToFieldOfAnchored2s() {
       
       runConformTest(
            new String[] {
		"Team3313ctfoa2s_4.java",
			    "\n" +
			    " public team class Team3313ctfoa2s_4 { \n" +
			    "    final Team3313ctfoa2s_1 t1 = new Team3313ctfoa2s_1(); \n" +
			    "    public class Role4 playedBy Role2<@t1> { \n" +
			    "       final Team3313ctfoa2s_3 t43 = t1.t23; \n" +
			    "       abstract void m(Role3<@t43> r3);\n" +
			    "       @SuppressWarnings(\"decapsulation\")\n" +
			    "       m -> set field; \n" +
			    "       void test() -> void test();\n" +
			    "       public void setAndTest() {\n" +
			    "            Role3<@t43> r3 = t43.new Role3();\n" +
			    "            m(r3);\n" +
			    "            test();\n" +
			    "       }\n" +
			    "    } \n" +
			    "    public Team3313ctfoa2s_4() {\n" +
			    "        test(t1.getRole());\n" +
			    "    }\n" +
			    "    public void test(Role2<@t1> as Role4 r) {\n" +
			    "        r.setAndTest();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3313ctfoa2s_4();\n" +
			    "    }\n" +
			    " } \n" +
			    "    \n",
		"Team3313ctfoa2s_3.java",
			    "\n" +
			    " public team class Team3313ctfoa2s_3 { \n" +
			    "    public class Role3 {} \n" +
			    " } \n" +
			    "    \n",
		"Team3313ctfoa2s_2.java",
			    "\n" +
			    " public team class Team3313ctfoa2s_2 { \n" +
			    "    final Team3313ctfoa2s_3 t23 = new Team3313ctfoa2s_3(); \n" +
			    "    public class Role2 { \n" +
			    "       protected Role3<@t23> field; \n" +
			    "       public void test() {\n" +
			    "           if (field != null) \n" +
			    "               System.out.print(\"OK\");\n" +
			    "           else\n" +
			    "               System.out.print(\"NOK\");\n" +
			    "       }\n" +
			    "    } \n" +
			    "    public Role2 getRole() { return new Role2(); }\n" +
			    " }\n" +
			    "    \n",
		"Team3313ctfoa2s_1.java",
			    "\n" +
			    " public team class Team3313ctfoa2s_1 extends Team3313ctfoa2s_2 { } \n" +
			    "    \n"
            },
            "OK");
    }

    // the type in a callout to field is an externalized role 
    // WITNESS for TPX-257
    // 3.3.14-otjld-callout-to-field-type-externalized-1
    public void test3314_calloutToFieldTypeExternalized1() {
       
       runConformTest(
            new String[] {
		"T3314ctfte1Main.java",
			    "\n" +
			    "public class T3314ctfte1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3314ctfte1_1();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team3314ctfte1_3.java",
			    "     \n" +
			    " public team class Team3314ctfte1_3 { \n" +
			    "    public class Role3 {\n" +
			    "        String val;\n" +
			    "        public String getVal() { return val; }\n" +
			    "        public Role3(String v) { val = v; }\n" +
			    "    } \n" +
			    " } \n" +
			    "    \n",
		"Team3314ctfte1_2.java",
			    "\n" +
			    " public team class Team3314ctfte1_2 { \n" +
			    "    protected final Team3314ctfte1_3 t23 = new Team3314ctfte1_3(); \n" +
			    "    public class Role2 { \n" +
			    "       public Role3<@t23> role;\n" +
			    "       public void test() { System.out.print(role.getVal()); }\n" +
			    "    } \n" +
			    "    public Role2 getR2() { return new Role2(); }\n" +
			    " } \n" +
			    "    \n",
		"Team3314ctfte1_1.java",
			    "\n" +
			    " public team class Team3314ctfte1_1 { \n" +
			    "    final Team3314ctfte1_2 t2 = new Team3314ctfte1_2(); \n" +
			    "    public class Role1 playedBy Role2<@t2> {\n" +
			    "          final Team3314ctfte1_3 t3 = t2.t23; \n" +
			    "          protected void foo(Role3<@t3> r3) -> set Role3<@t3> role;\n" +
			    "          protected Role3<@t3> newRole3(String v) {\n" +
			    "              return t3.new Role3(v);\n" +
			    "          }\n" +
			    "    } \n" +
			    "    public Team3314ctfte1_1() {\n" +
			    "        Role2<@t2> r2 = t2.getR2();\n" +
			    "        final Role1 r1 = new Role1(r2);\n" +
			    "        r1.foo(r1.newRole3(\"OK\"));\n" +
			    "        r2.test();\n" +
			    "    }\n" +
			    " } \n" +
			    "    \n"
            },
            "OK");
    }

    // the type in a callout to field is an externalized role - missing field decl
    // WITNESS for TPX-257(2)
    // 3.3.14-otjld-callout-to-field-type-externalized-2
    public void test3314_calloutToFieldTypeExternalized2() {
        runNegativeTestMatching(
            new String[] {
		"Team3314ctfte2_1.java",
			    "\n" +
			    " public team class Team3314ctfte2_1 { \n" +
			    "    final Team3314ctfte2_2 t2 = new Team3314ctfte2_2(); \n" +
			    "    public class Role1 playedBy Role2<@t2> {\n" +
			    "          protected final Team3314ctfte2_3 t3 = t2.getT3(); \n" +
			    "          abstract void foo(Role3<@t3> r3);\n" +
			    "          void foo(Role3<@t3> r3) -> set Role3<@t3> role;\n" +
			    "    } \n" +
			    "    @SuppressWarnings(\"roleinstantiation\")\n" +
			    "    public Team3314ctfte2_1() {\n" +
			    "        Role2<@t2> r2 = t2.getR2();\n" +
			    "        final Role1 r1 = new Role1(r2);\n" +
			    "        r1.foo(r1.t3.new Role3(\"OK\"));\n" +
			    "        r2.test();\n" +
			    "    }\n" +
			    " } \n" +
			    "    \n",
		"Team3314ctfte2_3.java",
			    "     \n" +
			    " public team class Team3314ctfte2_3 { \n" +
			    "    public class Role3 {\n" +
			    "        public String val;\n" +
			    "        public Role3(String v) { val = v; }\n" +
			    "    } \n" +
			    " } \n" +
			    "    \n",
		"Team3314ctfte2_2.java",
			    "\n" +
			    " public team class Team3314ctfte2_2 { \n" +
			    "    protected final Team3314ctfte2_3 t3 = new Team3314ctfte2_3(); \n" +
			    "    public class Role2 { \n" +
			    "       // protected Role3<@t3> role;\n" +
			    "       public void test() { System.out.print(role.val); }\n" +
			    "    } \n" +
			    "    public Role2 getR2() { return new Role2(); }\n" +
			    "    public Team3314ctfte2_3 getT3() { \n" +
			    "       return t3; \n" +
			    "    } \n" +
			    " } \n" +
			    "    \n"
            },
            "3.5");
    }

    // a callout set to field returns an externalized role (illegal base class decapsulation)
    // 3.3.14-otjld-callout-to-field-type-externalized-3f
    public void test3314_calloutToFieldTypeExternalized3f() {
        runNegativeTestMatching(
            new String[] {
		"Team3314ctfte3f_2.java",
			    "\n" +
			    "public team class Team3314ctfte3f_2 {\n" +
			    "    protected class R2 playedBy Team3314ctfte3f_1 {\n" +
			    "        abstract R1<@base> getR1();\n" +
			    "        getR1 -> get other;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team3314ctfte3f_1.java",
			    "\n" +
			    "public team class Team3314ctfte3f_1 { \n" +
			    "    R1 other = new R1();\n" +
			    "    protected class R1 {\n" +
			    "        public String toString() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1.2.3(b)");
    }

    // a callout set to field returns an externalized role (base class decapsulation)
    // 3.3.14-otjld-callout-to-field-type-externalized-3
    public void test3314_calloutToFieldTypeExternalized3() {
       
       runConformTest(
            new String[] {
		"Team3314ctfte3_2.java",
			    "\n" +
			    "public team class Team3314ctfte3_2 {\n" +
			    "    protected team class R3 playedBy Team3314ctfte3_1 {\n" +
			    "        protected R3(Team3314ctfte3_1 b) {\n" +
			    "            this.activate();\n" +
			    "        }\n" +
			    "        protected class R3_1 playedBy R1<@base> {\n" +
			    "            abstract R2<@R3.base> getR2();\n" +
			    "            getR2 -> get other;\n" +
			    "            void test() {\n" +
			    "                System.out.print(getR2());\n" +
			    "            }\n" +
			    "            test <- after test;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"roleinstantiation\")\n" +
			    "    Team3314ctfte3_2() {\n" +
			    "        this.activate();\n" +
			    "        Team3314ctfte3_1 b = new Team3314ctfte3_1();\n" +
			    "        new R3(b);\n" +
			    "        b.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3314ctfte3_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team3314ctfte3_1.java",
			    "\n" +
			    "public team class Team3314ctfte3_1 {\n" +
			    "    protected class R1 {\n" +
			    "        protected R2 other = new R2();\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class R2 {\n" +
			    "        public String toString() {\n" +
			    "            return \"K\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R1().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout set to field returns an externalized role
    // 3.3.14-otjld-callout-to-field-type-externalized-4
    public void test3314_calloutToFieldTypeExternalized4() {
       
       runConformTest(
            new String[] {
		"Team3314ctfte4_2.java",
			    "\n" +
			    "public team class Team3314ctfte4_2 {\n" +
			    "    protected team class R3 playedBy Team3314ctfte4_1 {\n" +
			    "        protected R3(Team3314ctfte4_1 b) {\n" +
			    "            this.activate();\n" +
			    "        }\n" +
			    "        protected class R3_1 playedBy R1<@base> {\n" +
			    "            abstract R2<@R3.base> getR2();\n" +
			    "	    @SuppressWarnings(\"decapsulation\")\n" +
			    "            getR2 -> get other;\n" +
			    "            void test() {\n" +
			    "                System.out.print(getR2());\n" +
			    "            }\n" +
			    "            test <- after test;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"roleinstantiation\")\n" +
			    "    Team3314ctfte4_2() {\n" +
			    "        this.activate();\n" +
			    "        Team3314ctfte4_1 b = new Team3314ctfte4_1();\n" +
			    "        new R3(b);\n" +
			    "        b.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3314ctfte4_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team3314ctfte4_1.java",
			    "\n" +
			    "public team class Team3314ctfte4_1 {\n" +
			    "    public class R1 {\n" +
			    "        R2 other = new R2();\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class R2 {\n" +
			    "        public String toString() {\n" +
			    "            return \"K\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R1().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout set to field returns an externalized role, field is implicitly inherited - saw compile error in OrderSystem
    // 3.3.14-otjld-callout-to-field-type-externalized-5
    public void test3314_calloutToFieldTypeExternalized5() {
       
       runConformTest(
            new String[] {
		"Team3314ctfte5_3.java",
			    "\n" +
			    "public team class Team3314ctfte5_3 {\n" +
			    "    protected team class R3 playedBy Team3314ctfte5_2 {\n" +
			    "        protected R3(Team3314ctfte5_2 b) {\n" +
			    "            this.activate();\n" +
			    "        }\n" +
			    "        protected class R3_1 playedBy R1<@base> {\n" +
			    "            abstract R2<@R3.base> getR2();\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            getR2 -> get other;\n" +
			    "            void test() {\n" +
			    "                System.out.print(getR2());\n" +
			    "            }\n" +
			    "            test <- after test;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"roleinstantiation\")\n" +
			    "    Team3314ctfte5_3() {\n" +
			    "        this.activate();\n" +
			    "        Team3314ctfte5_2 b = new Team3314ctfte5_2();\n" +
			    "        new R3(b);\n" +
			    "        b.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3314ctfte5_3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team3314ctfte5_1.java",
			    "\n" +
			    "public abstract team class Team3314ctfte5_1 {\n" +
			    "    public class R1 {\n" +
			    "        R2 other = new R2();\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public abstract class R2 {}\n" +
			    "    void test() {\n" +
			    "        new R1().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team3314ctfte5_2.java",
			    "\n" +
			    "public team class Team3314ctfte5_2 extends Team3314ctfte5_1 {\n" +
			    "    public class R2 {\n" +
			    "        public String toString() {\n" +
			    "            return \"K\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout to field overrides an (implicitly) inherited callout to field
    // 3.3.15-otjld-callout-to-field-overrides-1
    public void test3315_calloutToFieldOverrides1() {
       
       runConformTest(
            new String[] {
		"Team3315ctfo1_2.java",
			    "\n" +
			    "public team class Team3315ctfo1_2 extends Team3315ctfo1_1 {	\n" +
			    "	protected class R1 {\n" +
			    "		String getIt() => get String right;\n" +
			    "	}\n" +
			    "	Team3315ctfo1_2() {\n" +
			    "		R1 r = new R1(new T3315ctfo1());\n" +
			    "		System.out.print(r.getIt());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team3315ctfo1_2();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T3315ctfo1.java",
			    "\n" +
			    "public class T3315ctfo1 {\n" +
			    "	String wrong = \"wrong\";\n" +
			    "	String right = \"OK\";\n" +
			    "}	\n" +
			    "	\n",
		"Team3315ctfo1_1.java",
			    "\n" +
			    "public team class Team3315ctfo1_1 {\n" +
			    "	protected class R1 playedBy T3315ctfo1 {\n" +
			    "		protected abstract String getIt();\n" +
			    "		String getIt() -> get String wrong;\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // - same team
    // 3.3.15-otjld-callout-to-field-overrides-2
    public void test3315_calloutToFieldOverrides2() {
       
       runConformTest(
            new String[] {
		"Team3315ctfo2.java",
			    "\n" +
			    "public team class Team3315ctfo2 {\n" +
			    "	protected class R1 playedBy T3315ctfo2 {\n" +
			    "		protected abstract String getIt();\n" +
			    "		String getIt() -> get String wrong;\n" +
			    "	}\n" +
			    "	protected class R2 extends R1 {\n" +
			    "		String getIt() => get String right;\n" +
			    "	}\n" +
			    "	Team3315ctfo2() {\n" +
			    "		R2 r = new R2(new T3315ctfo2());\n" +
			    "		System.out.print(r.getIt());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team3315ctfo2();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T3315ctfo2.java",
			    "\n" +
			    "public class T3315ctfo2 {\n" +
			    "	String wrong = \"wrong\";\n" +
			    "	String right = \"OK\";\n" +
			    "}	\n" +
			    "	\n"
            },
            "OK");
    }

    // set - same team
    // 3.3.15-otjld-callout-to-field-overrides-3
    public void test3315_calloutToFieldOverrides3() {
       
       runConformTest(
            new String[] {
		"Team3315ctfo3.java",
			    "\n" +
			    "public team class Team3315ctfo3 {\n" +
			    "	protected class R1 playedBy T3315ctfo3 {\n" +
			    "		protected abstract void setIt(String v);\n" +
			    "		void setIt(String v) -> set String wrong;\n" +
			    "	}\n" +
			    "	protected class R2 extends R1 {\n" +
			    "		void setIt(String v) => set String right;\n" +
			    "	}\n" +
			    "	Team3315ctfo3(T3315ctfo3 as R2 r) {\n" +
			    "		r.setIt(\"OK\");\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		T3315ctfo3 b = new T3315ctfo3();\n" +
			    "		new Team3315ctfo3(b);\n" +
			    "		System.out.print(b.right);\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T3315ctfo3.java",
			    "\n" +
			    "public class T3315ctfo3 {\n" +
			    "	String wrong;\n" +
			    "	public String right = \"Wrong\";\n" +
			    "}	\n" +
			    "	\n"
            },
            "OK");
    }

    // wrong callout kind (should declare override)
    // 3.3.15-otjld-callout-to-field-overrides-4
    public void test3315_calloutToFieldOverrides4() {
        runNegativeTestMatching(
            new String[] {
		"Team3315ctfo4.java",
			    "\n" +
			    "public team class Team3315ctfo4 {\n" +
			    "	protected class R1 playedBy T3315ctfo4 {\n" +
			    "       @SuppressWarnings(\"decapsulation\")\n" +
			    "		void setIt(String v) -> set String wrong;\n" +
			    "	}\n" +
			    "	protected class R2 extends R1 {\n" +
			    "		void setIt(String v) -> set String right;\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T3315ctfo4.java",
			    "\n" +
			    "public class T3315ctfo4 {\n" +
			    "	String wrong;\n" +
			    "	public String right = \"Wrong\";\n" +
			    "}	\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team3315ctfo4.java (at line 8)\n" + 
    		"	void setIt(String v) -> set String right;\n" + 
    		"	^^^^^^^^^^^^^^^^^^^^\n" + 
    		"Callout binding conflicts with inherited callout binding from class Team3315ctfo4.R1. Use \'=>\' if you want to override it (OTJLD 3.5(g)).\n" + 
    		"----------\n");
    }

    // wrong callout kind (should declare override)
    // 3.3.15-otjld-callout-to-field-overrides-5
    public void test3315_calloutToFieldOverrides5() {
        runNegativeTestMatching(
            new String[] {
		"Team3315ctfo5.java",
			    "\n" +
			    "public team class Team3315ctfo5 {\n" +
			    "	protected class R1 playedBy T3315ctfo5 {\n" +
			    "		void setIt(String v) {}\n" +
			    "	}\n" +
			    "	protected class R2 extends R1 {\n" +
			    "		void setIt(String v) -> set String right;\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T3315ctfo5.java",
			    "\n" +
			    "public class T3315ctfo5 {\n" +
			    "	String wrong;\n" +
			    "	public String right = \"Wrong\";\n" +
			    "}	\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in Team3315ctfo5.java (at line 7)\n" + 
    		"	void setIt(String v) -> set String right;\n" + 
    		"	^^^^^^^^^^^^^^^^^^^^\n" + 
    		"A non-abstract role method exists for this callout-binding. Use callout-override (\'=>\') if you want to override it (OTJLD 3.5(g)).\n" + 
    		"----------\n");
    }

    // a callout to field is implicitly inherited
    // 3.3.16-otjld-implicitly-inheriting-callout-to-field-1
    public void test3316_implicitlyInheritingCalloutToField1() {
       
       runConformTest(
            new String[] {
		"Team3316iictf1_2.java",
			    "\n" +
			    "public team class Team3316iictf1_2 extends Team3316iictf1_1 {	\n" +
			    "	Team3316iictf1_2() {\n" +
			    "		R1 r = new R1(new T3316iictf1());\n" +
			    "		System.out.print(r.getIt());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team3316iictf1_2();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T3316iictf1.java",
			    "\n" +
			    "public class T3316iictf1 {\n" +
			    "	String right = \"OK\";\n" +
			    "}	\n" +
			    "	\n",
		"Team3316iictf1_1.java",
			    "\n" +
			    "public team class Team3316iictf1_1 {\n" +
			    "	protected class R1 playedBy T3316iictf1 {\n" +
			    "		protected abstract String getIt();\n" +
			    "		String getIt() -> get String right;\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // several teams call out to the same field (occurred in DispositionWorkflow.java)
    // 3.3.17-otjld-multiple-callout-to-same-field-1
    public void test3317_multipleCalloutToSameField1() {
       
       runConformTest(
            new String[] {
		"Team3317mctsf1_2.java",
			    "\n" +
			    "public team class Team3317mctsf1_2 {\n" +
			    "    protected team class R2 playedBy T3317mctsf1  {\n" +
			    "        R3 getOther() -> get BR<@base> r;\n" +
			    "        protected class R3 playedBy BR<@base> {\n" +
			    "	    public abstract String getV();\n" +
			    "	    @SuppressWarnings(\"decapsulation\")\n" +
			    "            String getV() -> get String val \n" +
			    "                with { result <- val.toUpperCase() }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            R3 other = getOther();\n" +
			    "            System.out.print(other.getV());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        R2 r = new R2(new T3317mctsf1(\"k\"));\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3317mctsf1_1().test();\n" +
			    "        new Team3317mctsf1_2().test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T3317mctsf1.java",
			    "\n" +
			    "public team class T3317mctsf1 {\n" +
			    "    BR r;\n" +
			    "    T3317mctsf1(String v) {\n" +
			    "        r = new BR(v);\n" +
			    "    }\n" +
			    "    public class BR {\n" +
			    "        protected String val;\n" +
			    "        public BR (String v) {\n" +
			    "            val = v;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team3317mctsf1_1.java",
			    "\n" +
			    "public team class Team3317mctsf1_1 {\n" +
			    "    protected team class R playedBy T3317mctsf1 {\n" +
			    "        protected abstract O getOther();\n" +
			    "        getOther -> get r;\n" +
			    "        public class O playedBy BR<@base> {\n" +
			    "	    public abstract String getV();\n" +
			    "	    @SuppressWarnings(\"decapsulation\")\n" +
			    "            String getV() -> get String val;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        final R r = new R(new T3317mctsf1(\"O\"));\n" +
			    "        System.out.print(r.getOther().getV());\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // several teams call out to the same field (occurred in DispositionWorkflow.java)
    // 3.3.17-otjld-multiple-callout-to-same-field-2
    public void test3317_multipleCalloutToSameField2() {
       
       runConformTest(
            new String[] {
		"Team3317mctsf2_2.java",
			    "\n" +
			    "public team class Team3317mctsf2_2 {\n" +
			    "    protected team class R2 playedBy T3317mctsf2  {\n" +
			    "        void setOther(R3 r) -> set BR<@base> r;\n" +
			    "        protected class R3 playedBy BR<@base> {\n" +
			    "            protected R3 (String v) {\n" +
			    "                base(v.toUpperCase());\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            setOther(new R3(\"k\"));\n" +
			    "            System.out.print(getVal());            \n" +
			    "        }\n" +
			    "        String getVal() -> String getVal();\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        R2 r = new R2(new T3317mctsf2());\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3317mctsf2_1().test();\n" +
			    "        new Team3317mctsf2_2().test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T3317mctsf2.java",
			    "\n" +
			    "public team class T3317mctsf2 {\n" +
			    "    BR r;\n" +
			    "    public class BR {\n" +
			    "        String val;\n" +
			    "        public BR (String v) {\n" +
			    "            val = v;\n" +
			    "        }\n" +
			    "        protected String getVal() { return val; }\n" +
			    "    }\n" +
			    "    public String getVal() {\n" +
			    "        return r.getVal();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team3317mctsf2_1.java",
			    "\n" +
			    "public team class Team3317mctsf2_1 {\n" +
			    "    protected team class R playedBy T3317mctsf2 {\n" +
			    "        protected abstract void setOther(O other);\n" +
			    "        setOther -> set r;\n" +
			    "        public class O playedBy BR<@base> {\n" +
			    "            public O(String v) {\n" +
			    "                base(v);\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"roleinstantiation\")\n" +
			    "    void test() {\n" +
			    "        T3317mctsf2 o = new T3317mctsf2();\n" +
			    "        final R r = new R(o); // arg is not a new expression, suppressed warning\n" +
			    "        r.setOther(r.new O(\"O\"));\n" +
			    "        System.out.print(o.getVal());\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a callout to field binds to a field whose type is more specific than the declared role return type
    // 3.3.18-otjld-callout-to-field-compatible-type-1
    public void test3318_calloutToFieldCompatibleType1() {
       
       runConformTest(
            new String[] {
		"Team3318ctfct1.java",
			    "\n" +
			    "public team class Team3318ctfct1 {\n" +
			    "    protected class R playedBy T3318ctfct1 {\n" +
			    "        protected abstract Object getIt();\n" +
			    "        getIt -> get f;\n" +
			    "    }\n" +
			    "    Team3318ctfct1() {\n" +
			    "        R r = new R(new T3318ctfct1());\n" +
			    "        System.out.print(r.getIt());\n" +
			    "    }\n" +
			    "    public static void main (String[] args) {\n" +
			    "        new Team3318ctfct1();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T3318ctfct1.java",
			    "\n" +
			    "public class T3318ctfct1 {\n" +
			    "    T3318ctfct1 f = this;\n" +
			    "    public String toString() { return \"OK\"; }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // callout set to array field
    // 3.3.19-otjld-callout-to-array-field-1
    public void test3319_calloutToArrayField1() {
       
       runConformTest(
            new String[] {
		"Team3319ctaf1.java",
			    "\n" +
			    "public team class Team3319ctaf1 {\n" +
			    "    protected class R playedBy T3319ctaf1 {\n" +
			    "        protected R() { base(); }\n" +
			    "        void setVals(String[] vals) -> set String[] vals;\n" +
			    "        protected abstract void test();\n" +
			    "        void test() -> void test();\n" +
			    "    }\n" +
			    "    Team3319ctaf1() {\n" +
			    "        R r = new R();\n" +
			    "        r.setVals(new String[]{\"O\",\"K\"});\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3319ctaf1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3319ctaf1.java",
			    "\n" +
			    "public class T3319ctaf1 {\n" +
			    "    protected String[] vals;\n" +
			    "    void test() {\n" +
			    "        System.out.print(vals[0]+vals[1]);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // callout set to array field - type name confusion (observed in TypeHierarchyViewAdaptor)
    // 3.3.19-otjld-callout-to-array-field-1f
    public void test3319_calloutToArrayField1f() {
        runNegativeTestMatching(
            new String[] {
		"p/Team3319ctaf1f.java",
			    "\n" +
			    "package p;\n" +
			    "import base p0.T3319ctaf1f;\n" +
			    "public team class Team3319ctaf1f {\n" +
			    "    protected class R playedBy T3319ctaf1f {\n" +
			    "        protected R() { base(); }\n" +
			    "        // the RHS using base import scope prefers p.String over java.lang.String:\n" +
			    "        void setVals(String[] vals) -> set String[] vals;\n" +
			    "        void test() -> void test();\n" +
			    "    }\n" +
			    "    Team3319ctaf1f() {\n" +
			    "        R r = new R();\n" +
			    "        r.setVals(new String[]{\"O\",\"K\"});\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3319ctaf1f();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p0/T3319ctaf1f.java",
			    "\n" +
			    "package p0;\n" +
			    "public class T3319ctaf1f {\n" +
			    "    public String[] vals;\n" +
			    "    public void test() {\n" +
			    "        System.out.print(vals[0]+vals[1]);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p/String.java",
			    "\n" +
			    "package p;\n" +
			    "public class String {}\n" +
			    "    \n"
            },
            "3.5(a)");
    }

    // an assignment is inferred to use a declared callout to field - assignment to field reference
    // 3.3.20-otjld-inferred-callout-to-field-1
    public void test3320_inferredCalloutToField1() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf1.java",
			    "\n" +
			    "public team class Team3320ictf1 {\n" +
			    "    protected class R playedBy T3320ictf1 {\n" +
			    "        void setVal(String v) -> set String val;\n" +
			    "        protected R () {\n" +
			    "            base();\n" +
			    "            this.val= \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    T3320ictf1 getBase() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T3320ictf1 b= new Team3320ictf1().getBase();\n" +
			    "        System.out.print(b.val);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf1.java",
			    "\n" +
			    "public class T3320ictf1 {\n" +
			    "    protected String val;\n" +
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

    // an assignment is inferred to use a declared callout to field - assignment to static field reference (deprecated but legal code)
    // 3.3.20-otjld-inferred-callout-to-field-1s
    public void test3320_inferredCalloutToField1s() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf1s.java",
			    "\n" +
			    "public team class Team3320ictf1s {\n" +
			    "    protected class R playedBy T3320ictf1s {\n" +
			    "        void setVal(String v) -> set String val;\n" +
			    "        protected R () {\n" +
			    "            base();\n" +
			    "            this.val= \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    T3320ictf1s getBase() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T3320ictf1s b= new Team3320ictf1s().getBase();\n" +
			    "        System.out.print(b.val);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf1s.java",
			    "\n" +
			    "public class T3320ictf1s {\n" +
			    "    protected static String val;\n" +
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

    // an assignment is inferred to use a declared callout to field - assignment to static field reference (unqualified field access)
    // 3.3.20-otjld-inferred-callout-to-field-1s2
    public void test3320_inferredCalloutToField1s2() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf1s2.java",
			    "\n" +
			    "public team class Team3320ictf1s2 {\n" +
			    "    protected class R playedBy T3320ictf1s2 {\n" +
			    "        void setVal(String v) -> set String val;\n" +
			    "        protected R () {\n" +
			    "            base();\n" +
			    "            val= \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    T3320ictf1s2 getBase() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T3320ictf1s2 b= new Team3320ictf1s2().getBase();\n" +
			    "        System.out.print(b.val);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf1s2.java",
			    "\n" +
			    "public class T3320ictf1s2 {\n" +
			    "    protected static String val;\n" +
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

    // an assignment is inferred to use a declared callout to field - assignment to single name reference
    // 3.3.20-otjld-inferred-callout-to-field-2
    public void test3320_inferredCalloutToField2() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf2.java",
			    "\n" +
			    "public team class Team3320ictf2 {\n" +
			    "    protected class R playedBy T3320ictf2 {\n" +
			    "	protected abstract void setStringVal(String v);\n" +
			    "        void setStringVal(String v) -> set String stringVal;\n" +
			    "        protected R () {\n" +
			    "            base();\n" +
			    "            stringVal= \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    T3320ictf2 getBase() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T3320ictf2 b= new Team3320ictf2().getBase();\n" +
			    "        System.out.print(b.stringVal);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf2.java",
			    "\n" +
			    "public class T3320ictf2 {\n" +
			    "    String stringVal;\n" +
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

    // a name reference inferred to use a declared callout to field - simple name expression
    // 3.3.20-otjld-inferred-callout-to-field-3
    public void test3320_inferredCalloutToField3() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf3.java",
			    "\n" +
			    "public team class Team3320ictf3 {\n" +
			    "    protected class R playedBy T3320ictf3 {\n" +
			    "        boolean getBoolVal() -> get boolean boolVal;\n" +
			    "        protected R () {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(boolVal);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf3().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf3.java",
			    "\n" +
			    "public class T3320ictf3 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private static boolean boolVal= true;\n" +
			    "}\n" +
			    "    \n"
            },
            "true",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a name reference inferred to use an inferred callout to field - simple name expression
    // 3.3.20-otjld-inferred-callout-to-field-3b
    public void test3320_inferredCalloutToField3b() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf3b.java",
			    "\n" +
			    "public team class Team3320ictf3b {\n" +
			    "    protected class R playedBy T3320ictf3b {\n" +
			    "        //boolean getBoolVal() -> get boolean boolVal;\n" +
			    "        protected R () {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            if(boolVal)\n" +
			    "                System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf3b().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf3b.java",
			    "\n" +
			    "public class T3320ictf3b {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private static boolean boolVal= true;\n" +
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

    // a name reference inferred to use a declared callout to field - this.f expression
    // 3.3.20-otjld-inferred-callout-to-field-4
    public void test3320_inferredCalloutToField4() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf4.java",
			    "\n" +
			    "public team class Team3320ictf4 {\n" +
			    "    protected class R playedBy T3320ictf4 {\n" +
			    "        String getStringVal() -> get String stringVal;\n" +
			    "        protected R () {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(this.stringVal);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf4().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf4.java",
			    "\n" +
			    "public class T3320ictf4 {\n" +
			    "    String stringVal= \"OK\";\n" +
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

    // an assignment is inferred to use an inferred callout to field - assignment to field reference
    // 3.3.20-otjld-inferred-callout-to-field-5
    public void test3320_inferredCalloutToField5() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf5.java",
			    "\n" +
			    "public team class Team3320ictf5 {\n" +
			    "    protected class R playedBy T3320ictf5 {\n" +
			    "        protected R () {\n" +
			    "            base();\n" +
			    "            this.val= \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    T3320ictf5 getBase() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T3320ictf5 b= new Team3320ictf5().getBase();\n" +
			    "        System.out.print(b.val);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf5.java",
			    "\n" +
			    "public class T3320ictf5 {\n" +
			    "    protected String val;\n" +
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

    // an assignment is inferred to use an inferred callout to field - reading a simple name
    // 3.3.20-otjld-inferred-callout-to-field-6
    public void test3320_inferredCalloutToField6() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf6.java",
			    "\n" +
			    "public team class Team3320ictf6 {\n" +
			    "    protected class R playedBy T3320ictf6 {\n" +
			    "        protected R () {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(stringVal);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf6().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf6.java",
			    "\n" +
			    "public class T3320ictf6 {\n" +
			    "    String stringVal= \"OK\";\n" +
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

    // an inferred callout to field, warning is suppressed
    // 3.3.20-otjld-inferred-callout-to-field-7
    public void test3320_inferredCalloutToField7() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf7.java",
			    "\n" +
			    "public team class Team3320ictf7 {\n" +
			    "    protected interface IR {\n" +
			    "        void forwarded();\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"inferredcallout\")\n" +
			    "    protected class R implements IR playedBy T3320ictf7 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        protected void test() {\n" +
			    "            i= 3;\n" +
			    "            int j= i + add(i, 4);\n" +
			    "            System.out.print(j);\n" +
			    "        }\n" +
			    "        int add(int i1, int i2) {\n" +
			    "            return i1+i2;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R(new T3320ictf7()).test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf7().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf7.java",
			    "\n" +
			    "public class T3320ictf7 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private int i;\n" +
			    "    void forwarded() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "10",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // an inferred callout to field, warning is suppressed - witness for wrong overload of StringBuilder.append
    // 3.3.20-otjld-inferred-callout-to-field-7f
    public void test3320_inferredCalloutToField7f() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf7f.java",
			    "\n" +
			    "public team class Team3320ictf7f {\n" +
			    "    protected interface IR {\n" +
			    "        void forwarded();\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"inferredcallout\")\n" +
			    "    protected class R implements IR playedBy T3320ictf7f {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        protected void test() {\n" +
			    "            i= 3;\n" +
			    "            System.out.print(\"\"+i);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R(new T3320ictf7f()).test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf7f().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf7f.java",
			    "\n" +
			    "public class T3320ictf7f {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private int i;\n" +
			    "    void forwarded() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "3",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // an inferred callout to inherited field (see Trac #95), warning is suppressed
    // 3.3.20-otjld-inferred-callout-to-field-8
    public void test3320_inferredCalloutToField8() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf8.java",
			    "\n" +
			    "public team class Team3320ictf8 {\n" +
			    "    protected interface IR {\n" +
			    "        void forwarded();\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"inferredcallout\")\n" +
			    "    protected class R implements IR playedBy T3320ictf8_2 {\n" +
			    "        protected void test() {\n" +
			    "            i= 3;\n" +
			    "            int j= i + add(i, 4);\n" +
			    "            System.out.print(j);\n" +
			    "        }\n" +
			    "        int add(int i1, int i2) {\n" +
			    "            return i1+i2;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R(new T3320ictf8_2()).test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf8().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf8_1.java",
			    "\n" +
			    "public class T3320ictf8_1 {\n" +
			    "    int i;\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf8_2.java",
			    "\n" +
			    "public class T3320ictf8_2 extends T3320ictf8_1 {\n" +
			    "    void forwarded() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "10",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // attempted inferred callout to "inherited" private field (see Trac #95 and #96), warning is suppressed
    // 3.3.20-otjld-inferred-callout-to-field-9
    public void test3320_inferredCalloutToField9() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       runNegativeTest(
            new String[] {
		"Team3320ictf9.java",
			    "\n" +
			    "public team class Team3320ictf9 {\n" +
			    "    protected interface IR {\n" +
			    "        void forwarded();\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"inferredcallout\")\n" +
			    "    protected class R implements IR playedBy T3320ictf9_2 {\n" +
			    "        void test() {\n" +
			    "            i= 3;\n" +
			    "            int j= i + add(i, 4);\n" +
			    "            System.out.print(j);\n" +
			    "        }\n" +
			    "        int add(int i1, int i2) {\n" +
			    "            return i1+i2;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf9_1.java",
			    "\n" +
			    "public class T3320ictf9_1 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private int i;\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf9_2.java",
			    "\n" +
			    "public class T3320ictf9_2 extends T3320ictf9_1 {\n" +
			    "    void forwarded() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. ERROR in Team3320ictf9.java (at line 9)\n" +
			"	i= 3;\n" +
			"	^\n" +
			"The private base feature i from type T3320ictf9_1 is not available via the base type T3320ictf9_2 (OTJLD 3.4(d)). \n" +
			"----------\n" +
			"2. ERROR in Team3320ictf9.java (at line 10)\n" +
			"	int j= i + add(i, 4);\n" +
			"	       ^\n" +
			"The private base feature i from type T3320ictf9_1 is not available via the base type T3320ictf9_2 (OTJLD 3.4(d)). \n" +
			"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }

    // the receiver of a method call is an inferred c-t-f - no .this prefix
    // 3.3.20-otjld-inferred-callout-to-field-10
    public void test3320_inferredCalloutToField10() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.IGNORE);
       
       runConformTest(
            new String[] {
		"Team3320ictf10.java",
			    "\n" +
			    "public team class Team3320ictf10 {\n" +
			    "    protected class R playedBy T3320ictf10 {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(val.toUpperCase());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public Team3320ictf10() {\n" +
			    "        R r = new R(new T3320ictf10(\"ok\"));\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf10();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf10.java",
			    "\n" +
			    "public class T3320ictf10 {\n" +
			    "    String val;\n" +
			    "    T3320ictf10 (String v) { this.val = v; }\n" +
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

    // the receiver of a method call is an inferred c-t-f - with .this prefix
    // also: reference to private field from binary
    // 3.3.20-otjld-inferred-callout-to-field-11
    public void test3320_inferredCalloutToField11() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.IGNORE);
       
       runConformTest(
    		new String[] {
		"T3320ictf11.java",
			    "\n" +
			    "public class T3320ictf11 {\n" +
			    "    private String val;\n" +
			    "    T3320ictf11 (String v) { this.val = v; }\n" +
			    "}\n" +
			    "    \n"    		
			    });
       runConformTest(
            new String[] {
		"Team3320ictf11.java",
			    "\n" +
			    "public team class Team3320ictf11 {\n" +
			    "    protected class R playedBy T3320ictf11 {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(this.val.toUpperCase());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public Team3320ictf11() {\n" +
			    "        R r = new R(new T3320ictf11(\"ok\"));\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf11();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",

            },
            "OK",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // an inferred c-t-f is a prefix in a qualified name reference
    // 3.3.20-otjld-inferred-callout-to-field-12
    public void test3320_inferredCalloutToField12() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.IGNORE);
       
       runConformTest(
            new String[] {
		"Team3320ictf12.java",
			    "\n" +
			    "public team class Team3320ictf12 {\n" +
			    "    protected class R playedBy T3320ictf12 {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(other.val);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public Team3320ictf12() {\n" +
			    "        R r = new R(new T3320ictf12(\"ok\"));\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf12();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf12.java",
			    "\n" +
			    "public class T3320ictf12 {\n" +
			    "    T3320ictf12 other;\n" +
			    "    public String val;\n" +
			    "    T3320ictf12 (String v) { this.val = v; this.other = this; }\n" +
			    "}\n" +
			    "    \n"
            },
            "ok",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a method call reuses the inferred getter for a base field
    // 3.3.20-otjld-inferred-callout-to-field-13
    public void test3320_inferredCalloutToField13() {
        Map customOptions = getCompilerOptions();
        customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
        runNegativeTest(
            new String[] {
		"Team3320ictf13.java",
			    "\n" +
			    "public team class Team3320ictf13 {\n" +
			    "    public class R playedBy T3320ictf13 {\n" +
			    "        void doing() {\n" +
			    "            if (f == getF())\n" +
			    "                System.out.print(\"==\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf13.java",
			    "\n" +
			    "public class T3320ictf13 {\n" +
			    "    byte f;\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. WARNING in Team3320ictf13.java (at line 5)\n" +
			"	if (f == getF())\n" +
			"	    ^\n" +
			"Reference byte f implicitly uses callout getter to base field 'byte f' (OTJLD 3.5(h)).\n" +
			"----------\n" +
			"2. ERROR in Team3320ictf13.java (at line 5)\n" +
			"	if (f == getF())\n" +
			"	         ^^^^^^\n" +
			"Method call getF() tries to re-use an inferred callout to field (OTJLD 3.5(h)). \n" +
			"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }

    // a method call reuses the inferred getter for a base field from its tsuper role
    // 3.3.20-otjld-inferred-callout-to-field-14
    public void test3320_inferredCalloutToField14() {
        Map customOptions = getCompilerOptions();
        customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
        runNegativeTest(
            new String[] {
		"Team3320ictf14_2.java",
			    "\n" +
			    "public team class Team3320ictf14_2 extends Team3320ictf14_1 {\n" +
			    "    public class R playedBy T3320ictf14 {\n" +
			    "        void doing() {\n" +
			    "            if (f == getF())\n" +
			    "                System.out.print(\"==\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf14.java",
			    "\n" +
			    "public class T3320ictf14 {\n" +
			    "    byte f;\n" +
			    "}\n" +
			    "    \n",
		"Team3320ictf14_1.java",
			    "@SuppressWarnings(\"inferredcallout\")\n" +
			    "public team class Team3320ictf14_1 {\n" +
			    "    public class R playedBy T3320ictf14 {\n" +
			    "        void doOther() {\n" +
			    "            System.out.print(f);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. WARNING in Team3320ictf14_2.java (at line 3)\n" +
			"	public class R playedBy T3320ictf14 {\n" +
			"	             ^\n" +
			"Role Team3320ictf14_2.R should be tagged with @Override since it actually overrides a superteam role (OTJLD 1.3.1(c)).\n" +
			"----------\n" +
			"2. WARNING in Team3320ictf14_2.java (at line 5)\n" +
			"	if (f == getF())\n" +
			"	    ^\n" +
			"Reference byte f implicitly uses callout getter to base field 'byte f' (OTJLD 3.5(h)).\n" +
			"----------\n" +
			"3. ERROR in Team3320ictf14_2.java (at line 5)\n" +
			"	if (f == getF())\n" +
			"	         ^^^^^^\n" +
			"Method call getF() tries to re-use an inferred callout to field (OTJLD 3.5(h)). \n" +
			"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }

    // an inferred callout to field has to apply lifting - extracted from CallHierarchyAdapter.accectSearchMatch
    // 3.3.20-otjld-inferred-callout-to-field-15
    public void test3320_inferredCalloutToField15() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf15.java",
			    "\n" +
			    "public team class Team3320ictf15 {\n" +
			    "    @SuppressWarnings(\"inferredcallout\")\n" +
			    "    protected class R playedBy T3320ictf15 {\n" +
			    "        protected R() { base(); }\n" +
			    "        protected void test() {\n" +
			    "            R other = me;\n" +
			    "            other.print();\n" +
			    "        }\n" +
			    "        void print() { System.out.print(\"OK\"); }\n" +
			    "    }\n" +
			    "    public void test() {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf15().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf15.java",
			    "\n" +
			    "public class T3320ictf15 {\n" +
			    "    public T3320ictf15 me = this;\n" +
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

    // an inferred callout to field has to apply lifting
    // like before but field reference (this.me)
    // Bug 339807 - [compiler] inferred callout to field doesn't support lifting
    public void test3320_inferredCalloutToField15f() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3320ictf15f.java",
			    "\n" +
			    "public team class Team3320ictf15f {\n" +
			    "    @SuppressWarnings(\"inferredcallout\")\n" +
			    "    protected class R playedBy T3320ictf15f {\n" +
			    "        protected R() { base(); }\n" +
			    "        protected void test() {\n" +
			    "            R other = this.me;\n" +
			    "            other.print();\n" +
			    "        }\n" +
			    "        void print() { System.out.print(\"OK\"); }\n" +
			    "    }\n" +
			    "    public void test() {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3320ictf15f().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf15f.java",
			    "\n" +
			    "public class T3320ictf15f {\n" +
			    "    public T3320ictf15f me = this;\n" +
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

    // an assignment is inferred to use a declared callout to field - compound assignment to field reference
    public void test3320_inferredCalloutToField16() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runNegativeTest(
            new String[] {
		"Team3320ictf16.java",
			    "\n" +
			    "public team class Team3320ictf16 {\n" +
			    "    protected class R playedBy T3320ictf16 {\n" +
			    "        void setVal(int v) -> set int val;\n" +
			    "        protected R () {\n" +
			    "            base();\n" +
			    "            this.val += 13;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    T3320ictf16 getBase() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T3320ictf16 b= new Team3320ictf16().getBase();\n" +
			    "        System.out.print(b.val);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3320ictf16.java",
			    "\n" +
			    "public class T3320ictf16 {\n" +
			    "    protected int val = 4;\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team3320ictf16.java (at line 7)\n" + 
    		"	this.val += 13;\n" + 
    		"	^^^^^^^^\n" + 
    		"Attempting to infer callout to base field val in a compound assignment (OTJLD 3.1(j)).\n" + 
    		"----------\n" + 
    		"2. ERROR in Team3320ictf16.java (at line 7)\n" + 
    		"	this.val += 13;\n" + 
    		"	     ^^^\n" + 
    		"val cannot be resolved or is not a field\n" + 
    		"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }


    // a callout accesses a field in a base-anchored base role (using special syntax new R<@base>()]
    // 3.3.21-otjld_disabled_callout-to-field-anchored-type-1
    public void test3321_calloutToFieldAnchoredType1() {
       
       runConformTest(
            new String[] {
		"Team3321ctfat1_2.java",
			    "\n" +
			    "public team class Team3321ctfat1_2 {\n" +
			    "    protected team class Mid playedBy Team3321ctfat1_1 {\n" +
			    "        protected class Inner playedBy base.R {\n" +
			    "            protected String getVal() -> get String val;\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            base.R r= new R<@base>();\n" +
			    "            Inner i= new Inner(r);\n" +
			    "            System.out.print(i.getVal());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team3321ctfat1_2() {\n" +
			    "        Mid m= new Mid(new Team3321ctfat1_1());\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3321ctfat1_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team3321ctfat1_1.java",
			    "\n" +
			    "public team class Team3321ctfat1_1 {\n" +
			    "    public class R  {\n" +
			    "        String val= \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout accesses a field in a base-anchored base role
    // 3.3.21-otjld-callout-to-field-anchored-type-2
    public void test3321_calloutToFieldAnchoredType2() {
       
       runConformTest(
            new String[] {
		"Team3321ctfat2_2.java",
			    "\n" +
			    "public team class Team3321ctfat2_2 {\n" +
			    "    protected team class Mid playedBy Team3321ctfat2_1 {\n" +
			    "        protected class Inner playedBy R<@base> {\n" +
			    "	    @SuppressWarnings(\"decapsulation\")\n" +
			    "            protected String getVal() -> get String val;\n" +
			    "        }\n" +
			    "        protected Inner getNewInner() -> R<@base> getNewR();\n" +
			    "        protected void test() {\n" +
			    "            Inner i= getNewInner();\n" +
			    "            System.out.print(i.getVal());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team3321ctfat2_2() {\n" +
			    "        Mid m= new Mid(new Team3321ctfat2_1());\n" +
			    "        m.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3321ctfat2_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team3321ctfat2_1.java",
			    "\n" +
			    "public team class Team3321ctfat2_1 {\n" +
			    "    public class R  {\n" +
			    "        String val= \"OK\";\n" +
			    "    }\n" +
			    "    public R getNewR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // looks similar to above, but base actually refers to a package
    // 3.3.21-otjld-callout-to-field-anchored-type-3
    public void test3321_calloutToFieldAnchoredType3() {
       
       runConformTest(
            new String[] {
		"Team3321ctfat3.java",
			    "\n" +
			    "import base base.T3321ctfat3;    \n" +
			    "public team class Team3321ctfat3 {\n" +
			    "    protected class R playedBy T3321ctfat3 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        protected int getVal() -> get String val\n" +
			    "            with { result <- val.length() }\n" +
			    "        protected R() { base(); }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        R r= new R();\n" +
			    "        System.out.print(r.getVal());\n" +
			    "    }\n" +
			    "    Team3321ctfat3() {\n" +
			    "        test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3321ctfat3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"base/T3321ctfat3.java",
			    "\n" +
			    "package base;\n" +
			    "public class T3321ctfat3 {\n" +
			    "    String val= \"OK\";\n" +
			    "}\n" +
			    "    \n"
            },
            "2");
    }
    
    // Bug 354480 - VerifyError due to bogus lowering in inferred callout-to-field
    public void testBug354480() {
    	Map options = getCompilerOptions();
    	options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
    	runConformTest(
    		new String[] {
    			"Main.java",
	    			"import b.Return;\n" + 
	    			"import b.Scope;\n" + 
	    			"import t.Team1;\n" + 
	    			"public class Main {\n" + 
	    			"	public static void main(String[] args) {\n" + 
	    			"		new Team1().activate();\n" + 
	    			"		new Return().analyse(new Scope());\n" + 
	    			"	}\n" + 
	    			"}\n",
    			"b/Expr.java",
	    			"package b;\n" +
	    			"public class Expr  {\n" + 
	    			"	public void analyse(Scope scope) {\n" +
	    			"		System.out.print(\"OK\");\n" +
	    			"	}\n" + 
	    			"}\n",
    			"b/Scope.java",
	    			"package b;\n" +
	    			"public class Scope {}\n",
    			"b/Return.java",
	    			"package b;\n" +
	    			"public class Return  {\n" + 
	    			"	public Expr expr = new Expr();\n" + 
	    			"	public void analyse(Scope scope) {\n" + 
	    			"	}\n" + 
	    			"}\n",
	    		"t/Team1.java",
	    		"package t;\n" +
	    		"import b.Expr;\n" + 
	    		"import base b.Return;\n" + 
	    		"import base b.Scope;\n" + 
	    		"\n" + 
	    		"public team class Team1 {\n" + 
	    		"	protected class Scope playedBy Scope {}\n" + 
	    		"	protected class Return playedBy Return {\n" + 
	    		"		Expr getExpr() -> get Expr expr;\n" + 
	    		"		void analyse(Scope scope) <- replace void analyse(Scope scope);\n" + 
	    		"		@SuppressWarnings({\"inferredcallout\", \"basecall\"})\n" + 
	    		"		callin void analyse(Scope scope) {\n" + 
	    		"			this.expr.analyse(scope);\n" + // expr via c-t-f, scope needs lowering
	    		"		}\n" + 
	    		"	}\n" + 
	    		"}\n"    			
    		},
    		"OK",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            options,
            null/*no custom requestor*/);
    }
}
