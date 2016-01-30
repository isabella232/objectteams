/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2016 IT Service Omikron GmbH and others.
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
package org.eclipse.objectteams.otdt.tests.otjld.calloutbinding;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.core.ext.WeavingScheme;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

@SuppressWarnings("unchecked")
public class CalloutMethodBinding extends AbstractOTJLDTest {
	
	public CalloutMethodBinding(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test3117_inferredCallout14"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return CalloutMethodBinding.class;
	}
    // an abstract role method is callout-bound via -> to a method in the direct base class
    // 3.1.1-otjld-abstract-callout-binding-1
    public void test311_abstractCalloutBinding1() {
       
       runConformTest(
            new String[] {
		"T311acb1Main.java",
			    "\n" +
			    "public class T311acb1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team311acb1 t = new Team311acb1();\n" +
			    "        T311acb1_1  o = new T311acb1_1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T311acb1_1.java",
			    "\n" +
			    "public class T311acb1_1 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T311acb1_2.java",
			    "\n" +
			    "public interface T311acb1_2 {\n" +
			    "    public String getValue(String arg);\n" +
			    "}\n" +
			    "    \n",
		"Team311acb1.java",
			    "\n" +
			    "public team class Team311acb1 {\n" +
			    "\n" +
			    "    public class Role311acb1 implements T311acb1_2 playedBy T311acb1_1 {\n" +
			    "        public abstract String getValue(String arg);\n" +
			    "        getValue -> getValueInternal;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T311acb1_1 as Role311acb1 obj) {\n" +
			    "        return obj.getValue(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // the same but without the callout binding - make sure we don't suppress too many errors: must be flagged as abstract
    // 3.1.1-otjld-abstract-callout-binding-1a
    public void test311_abstractCalloutBinding1a() {
        runNegativeTestMatching(
            new String[] {
		"Team311acb1a.java",
			    "\n" +
			    "public team class Team311acb1a {\n" +
			    "\n" +
			    "    public class Role311acb1a implements T311acb1a_2 playedBy T311acb1a_1 {\n" +
			    "        public abstract String getValue(String arg);\n" +
			    "        // getValue -> getValueInternal; <- missing\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T311acb1a_1 as Role311acb1a obj) {\n" +
			    "        return obj.getValue(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T311acb1a_1.java",
			    "\n" +
			    "public class T311acb1a_1 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T311acb1a_2.java",
			    "\n" +
			    "public interface T311acb1a_2 {\n" +
			    "    public String getValue(String arg);\n" +
			    "}\n" +
			    "    \n"
            },
            "abstract");
    }

    // an abstract role method is callout-bound via -> to a method in the base class of the implicit superclass
    // 3.1.1-otjld-abstract-callout-binding-2
    public void test311_abstractCalloutBinding2() {
       
       runConformTest(
            new String[] {
		"T311acb2Main.java",
			    "\n" +
			    "public class T311acb2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team311acb2_2 t = new Team311acb2_2();\n" +
			    "        T311acb2_2    o = new T311acb2_2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T311acb2_1.java",
			    "\n" +
			    "public class T311acb2_1 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T311acb2_2.java",
			    "\n" +
			    "public class T311acb2_2 extends T311acb2_1 {}\n" +
			    "    \n",
		"Team311acb2_1.java",
			    "\n" +
			    "public team class Team311acb2_1 {\n" +
			    "\n" +
			    "    public class Role311acb2 playedBy T311acb2_2 {}\n" +
			    "}\n" +
			    "    \n",
		"Team311acb2_2.java",
			    "\n" +
			    "public team class Team311acb2_2 extends Team311acb2_1 {\n" +
			    "\n" +
			    "    public class Role311acb2 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValue();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T311acb2_2 as Role311acb2 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an abstract role method is callout-bound via -> to a method in the base class of the explicit superclass
    // 3.1.1-otjld-abstract-callout-binding-3
    public void test311_abstractCalloutBinding3() {
       
       runConformTest(
            new String[] {
		"T311acb3Main.java",
			    "\n" +
			    "public class T311acb3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team311acb3 t = new Team311acb3();\n" +
			    "        T311acb3    o = new T311acb3();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T311acb3.java",
			    "\n" +
			    "public class T311acb3 {\n" +
			    "    String getValueInternal(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team311acb3.java",
			    "\n" +
			    "public team class Team311acb3 {\n" +
			    "\n" +
			    "    public class Role311acb3_1 playedBy T311acb3 {}\n" +
			    "\n" +
			    "    public class Role311acb3_2 extends Role311acb3_1 {\n" +
			    "        public abstract String getValue(String arg);\n" +
			    "        String getValue(String arg) -> String getValueInternal(String arg);\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T311acb3 as Role311acb3_2 obj) {\n" +
			    "        return obj.getValue(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an abstract role method is callout-bound via -> to a method in the indirect base class of the explicit superclass
    // 3.1.1-otjld-abstract-callout-binding-4
    public void test311_abstractCalloutBinding4() {
       
       runConformTest(
            new String[] {
		"T311acb4Main.java",
			    "\n" +
			    "public class T311acb4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        p2.Team311acb4_2 t = new p2.Team311acb4_2();\n" +
			    "        p1.T311acb4      o = new p1.T311acb4();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T311acb4.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T311acb4 {\n" +
			    "    public String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/Team311acb4_1.java",
			    "\n" +
			    "package p2;\n" +
			    "import base p1.T311acb4;\n" +
			    "public team class Team311acb4_1 {\n" +
			    "\n" +
			    "    public class Role311acb4_1 playedBy T311acb4 {}\n" +
			    "}\n" +
			    "    \n",
		"p2/Team311acb4_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team311acb4_2 extends Team311acb4_1 {\n" +
			    "\n" +
			    "    public class Role311acb4_2 extends Role311acb4_1 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        getValue -> getValue;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(p1.T311acb4 as Role311acb4_2 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an abstract role method is callout-bound (via -> and the name only) to a base method with an assignment-compatible return type
    // 3.1.1-otjld-abstract-callout-binding-5
    public void test311_abstractCalloutBinding5() {
       
       runConformTest(
            new String[] {
		"T311acb5Main.java",
			    "\n" +
			    "public class T311acb5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team311acb5 t = new Team311acb5();\n" +
			    "        T311acb5    o = new T311acb5();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T311acb5.java",
			    "\n" +
			    "public class T311acb5 {\n" +
			    "    public int getValue() {\n" +
			    "        return 1;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team311acb5.java",
			    "\n" +
			    "public team class Team311acb5 {\n" +
			    "\n" +
			    "    public class Role311acb5 playedBy T311acb5 {\n" +
			    "        public abstract long getValue();\n" +
			    "        getValue -> getValue;\n" +
			    "    }\n" +
			    "\n" +
			    "    public long getValue(T311acb5 as Role311acb5 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1");
    }

    // an abstract role method is callout-bound (via -> and the name only) to a base method with an assignment-compatible signature
    // 3.1.1-otjld-abstract-callout-binding-6
    public void test311_abstractCalloutBinding6() {
       
       runConformTest(
            new String[] {
		"T311acb6Main.java",
			    "\n" +
			    "public class T311acb6Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team311acb6 t = new Team311acb6();\n" +
			    "        T311acb6    o = new T311acb6();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T311acb6.java",
			    "\n" +
			    "public class T311acb6 {\n" +
			    "    public double getValue(double arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team311acb6.java",
			    "\n" +
			    "public team class Team311acb6 {\n" +
			    "\n" +
			    "    public class Role311acb6 playedBy T311acb6 {\n" +
			    "        public abstract double getValue(float arg);\n" +
			    "        getValue -> getValue;\n" +
			    "    }\n" +
			    "\n" +
			    "    public double getValue(T311acb6 as Role311acb6 obj) {\n" +
			    "        return obj.getValue(1.0f);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1.0");
    }

    // a role class callout-binds a role method via -> to a base method that has a signature that is compatible via lowering
    // 3.1.1-otjld-abstract-callout-binding-7
    public void test311_abstractCalloutBinding7() {
       
       runConformTest(
            new String[] {
		"T311acb7Main.java",
			    "\n" +
			    "public class T311acb7Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team311acb7 t = new Team311acb7();\n" +
			    "        T311acb7_1  o = new T311acb7_1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T311acb7_2.java",
			    "\n" +
			    "public class T311acb7_2 {\n" +
			    "    public String toString() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T311acb7_1.java",
			    "\n" +
			    "public class T311acb7_1 {\n" +
			    "    public T311acb7_2 getObject() {\n" +
			    "        return new T311acb7_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team311acb7.java",
			    "\n" +
			    "public team class Team311acb7 {\n" +
			    "\n" +
			    "    public class Role311acb7_1 playedBy T311acb7_1 {\n" +
			    "        public abstract Role311acb7_2 test();\n" +
			    "        test -> getObject;\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role311acb7_2 playedBy T311acb7_2 {\n" +
			    "        public String toString() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T311acb7_1 as Role311acb7_1 obj) {\n" +
			    "        return obj.test().toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }
    // Bug 355314 - abstract method error may be masked by callout binding
    public void test311_abstractCalloutBinding8() {
        
        runNegativeTest(
             new String[] {
 		"T311acb8Main.java",
 			    "\n" +
 			    "public class T311acb8Main {\n" +
 			    "    public static void main(String[] args) {\n" +
 			    "        Team311acb8_2 t = new Team311acb8_2();\n" +
 			    "        T311acb8_1  o = new T311acb8_1();\n" +
 			    "\n" +
 			    "        System.out.print(t.getValue(o));\n" +
 			    "    }\n" +
 			    "}\n" +
 			    "    \n",
 		"T311acb8_1.java",
 			    "\n" +
 			    "public class T311acb8_1 {\n" +
 			    "    public String getValue() {\n" +
 			    "        return getValueInternal();\n" +
 			    "    }\n" +
 			    "    public String getValueInternal() {\n" +
 			    "        return \"OK\";\n" +
 			    "    }\n" +
 			    "}\n" +
 			    "    \n",
	    "Team311acb8_2.java",
 			    "\n" +
 			    "public team class Team311acb8_2 extends Team311acb8_1 {\n" +
 			    "    @Override\n" +
 			    "    public class Role311acb8 playedBy T311acb8_1 {\n" +
 			    "        getValue -> getValue;\n" +
 			    "    }\n" +
 			    "\n" +
 			    "    public String getValue(T311acb8_1 as Role311acb8 obj) {\n" +
 			    "        return obj.getValueInternal();\n" +
 			    "    }\n" +
 			    "}\n",
 		"Team311acb8_1.java",
 			    "\n" +
 			    "public abstract team class Team311acb8_1 {\n" +
 			    "\n" +
 			    "    public abstract class Role311acb8 {\n" +
 			    "        public abstract String getValue();\n" +
 			    "        public abstract String getValueInternal();\n" +
 			    "    }\n" +
 			    "\n" +
 			    "}\n",
             },
            "----------\n" + 
			"1. ERROR in Team311acb8_2.java (at line 4)\n" + 
			"	public class Role311acb8 playedBy T311acb8_1 {\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Inherited abstract method getValueInternal() is implicitly bound as an inferred callout (OTJLD 3.1(j)).\n" + 
			"----------\n");
     }
    // Bug 355314 - abstract method error may be masked by callout binding
    public void test311_abstractCalloutBinding8a() {
        
        runNegativeTest(
             new String[] {
 		"T311acb8aMain.java",
 			    "\n" +
 			    "public class T311acb8aMain {\n" +
 			    "    public static void main(String[] args) {\n" +
 			    "        Team311acb8a_2 t = new Team311acb8a_2();\n" +
 			    "        T311acb8a_1  o = new T311acb8a_1();\n" +
 			    "\n" +
 			    "        System.out.print(t.getValue(o));\n" +
 			    "    }\n" +
 			    "}\n" +
 			    "    \n",
 		"T311acb8a_1.java",
 			    "\n" +
 			    "public class T311acb8a_1 {\n" +
 			    "    public String getValue() {\n" +
 			    "        return \"OK\";\n" +
 			    "    }\n" +
 			    "}\n" +
 			    "    \n",
	    "Team311acb8a_2.java",
 			    "\n" +
 			    "public team class Team311acb8a_2 extends Team311acb8a_1 {\n" +
 			    "    @Override\n" +
 			    "    public class Role311acb8a playedBy T311acb8a_1 {\n" +
 			    "        getValue -> getValue;\n" +
 			    "    }\n" +
 			    "\n" +
 			    "    public String getValue(T311acb8a_1 as Role311acb8a obj) {\n" +
 			    "        return obj.getValueInternal();\n" +
 			    "    }\n" +
 			    "}\n",
 		"Team311acb8a_1.java",
 			    "\n" +
 			    "public abstract team class Team311acb8a_1 {\n" +
 			    "\n" +
 			    "    public abstract class Role311acb8a {\n" +
 			    "        public abstract String getValue();\n" +
 			    "        public abstract String getValueInternal();\n" +
 			    "    }\n" +
 			    "\n" +
 			    "}\n",
             },
            "----------\n" + 
			"1. ERROR in Team311acb8a_2.java (at line 4)\n" + 
			"	public class Role311acb8a playedBy T311acb8a_1 {\n" + 
			"	             ^^^^^^^^^^^^\n" + 
			"The abstract method getValueInternal in type Role311acb8a can only be defined by an abstract class\n" + 
			"----------\n" +
			"2. ERROR in Team311acb8a_2.java (at line 4)\n" + 
			"	public class Role311acb8a playedBy T311acb8a_1 {\n" + 
			"	             ^^^^^^^^^^^^\n" + 
			"The type Team311acb8a_2.Role311acb8a must implement the inherited abstract method Team311acb8a_2.Role311acb8a.getValueInternal()\n" + 
			"----------\n");
     }

    // an abstract role method is callout-bound via -> and the signature to a method but the parameter names are missing
    // 3.1.2-otjld-missing-parameter-names-1
    public void test312_missingParameterNames1() {
        runNegativeTestMatching(
            new String[] {
		"Team312mpn1.java",
			    "\n" +
			    "public team class Team312mpn1 {\n" +
			    "\n" +
			    "    public class Role312mpn1 playedBy T312mpn1 {\n" +
			    "        protected abstract void getValue(String arg);\n" +
			    "        void getValue(String) -> void getValueInternal(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T312mpn1.java",
			    "\n" +
			    "public class T312mpn1 {\n" +
			    "    public void getValueInternal(String arg) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "Syntax error");
    }

    // an abstract role method is callout-bound via -> and the signature to a method but the parameter names are missing
    // 3.1.2-otjld-missing-parameter-names-2
    public void test312_missingParameterNames2() {
        runNegativeTestMatching(
            new String[] {
		"Team312mpn2.java",
			    "\n" +
			    "public team class Team312mpn2 {\n" +
			    "\n" +
			    "    public class Role312mpn2 playedBy T312mpn2 {\n" +
			    "        protected abstract void getValue(String arg);\n" +
			    "        void getValue(String arg) -> void getValueInternal(String, String arg2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T312mpn2.java",
			    "\n" +
			    "public class T312mpn2 {\n" +
			    "    public void getValueInternal(String arg1, String arg2) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "Syntax error");
    }

    // a concrete role method is callout-bound via => to a method in the direct base class
    // 3.1.3-otjld-concrete-callout-binding-1
    public void test313_concreteCalloutBinding1() {
       
       runConformTest(
            new String[] {
		"T313ccb1Main.java",
			    "\n" +
			    "public class T313ccb1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team313ccb1 t = new Team313ccb1();\n" +
			    "        T313ccb1    o = new T313ccb1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T313ccb1.java",
			    "\n" +
			    "public class T313ccb1 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team313ccb1.java",
			    "\n" +
			    "public team class Team313ccb1 {\n" +
			    "    public class Role313ccb0 {\n" +
			    "        String getValue(String arg) {\n" +
			    "            return arg;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class Role313ccb1 extends Role313ccb0 playedBy T313ccb1 {\n" +
			    "        protected String getValue(String arg) => String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T313ccb1 as Role313ccb1 obj) {\n" +
			    "        return obj.getValue(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout-override binding tries to reduce the visibility of an inherited method
    // 3.1.3-otjld-concrete-callout-binding-1f
    public void test313_concreteCalloutBinding1f() {
        runNegativeTestMatching(
            new String[] {
		"Team313ccb1f.java",
			    "\n" +
			    "public team class Team313ccb1f {\n" +
			    "    public class Role313ccb0 {\n" +
			    "        public String getValue(String arg) {\n" +
			    "            return arg;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class Role313ccb1f extends Role313ccb0 playedBy T313ccb1f {\n" +
			    "        protected String getValue(String arg) => String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T313ccb1f as Role313ccb1f obj) {\n" +
			    "        return obj.getValue(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T313ccb1f.java",
			    "\n" +
			    "public class T313ccb1f {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "Cannot reduce the visibility");
    }

    // a concrete role method is callout-bound via => to a method in the implicit base class
    // 3.1.3-otjld-concrete-callout-binding-2
    public void test313_concreteCalloutBinding2() {
       
       runConformTest(
            new String[] {
		"T313ccb2Main.java",
			    "\n" +
			    "public class T313ccb2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team313ccb2_2 t = new Team313ccb2_2();\n" +
			    "        T313ccb2_2    o = new T313ccb2_2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T313ccb2_1.java",
			    "\n" +
			    "public class T313ccb2_1 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T313ccb2_2.java",
			    "\n" +
			    "public class T313ccb2_2 extends T313ccb2_1 {}\n" +
			    "    \n",
		"Team313ccb2_1.java",
			    "\n" +
			    "public team class Team313ccb2_1 {\n" +
			    "\n" +
			    "    public class Role313ccb2 playedBy T313ccb2_2 {\n" +
			    "        public String getValue(String arg) {\n" +
			    "            return arg;\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team313ccb2_2.java",
			    "\n" +
			    "public team class Team313ccb2_2 extends Team313ccb2_1 {\n" +
			    "\n" +
			    "    public class Role313ccb2 {\n" +
			    "        getValue => getValue;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T313ccb2_2 as Role313ccb2 obj) {\n" +
			    "        return obj.getValue(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a concrete role method is callout-bound via => to a method in the base class of the explicit superclass
    // 3.1.3-otjld-concrete-callout-binding-3
    public void test313_concreteCalloutBinding3() {
       
       runConformTest(
            new String[] {
		"T313ccb3Main.java",
			    "\n" +
			    "public class T313ccb3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team313ccb3_2 t = new Team313ccb3_2();\n" +
			    "        T313ccb3    o = new T313ccb3();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T313ccb3.java",
			    "\n" +
			    "public class T313ccb3 {\n" +
			    "    String getValueInternal(String arg) {\n" +
			    "        return arg;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team313ccb3_1.java",
			    "\n" +
			    "public abstract team class Team313ccb3_1 {\n" +
			    "	public abstract class Role313ccb3_1 {}\n" +
			    "	public abstract class Role313ccb3_2 {\n" +
			    "	        public String getValue(String arg) {\n" +
			    "        	    return \"NOTOK\";\n" +
			    "        	}\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n",
		"Team313ccb3_2.java",
			    "\n" +
			    "public team class Team313ccb3_2 extends Team313ccb3_1 {\n" +
			    "\n" +
			    "    public class Role313ccb3_1 playedBy T313ccb3 {}\n" +
			    "\n" +
			    "    public class Role313ccb3_2 extends Role313ccb3_1 {\n" +
			    "    	public Role313ccb3_2() {\n" +
			    "		base();\n" +
			    "	}\n" +
			    "        getValue => getValueInternal;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T313ccb3 as Role313ccb3_2 obj) {\n" +
			    "        return obj.getValue(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a concrete role method is callout-bound via => to a method in the indirect base class of the explicit superclass
    // 3.1.3-otjld-concrete-callout-binding-4
    public void test313_concreteCalloutBinding4() {
       
       runConformTest(
            new String[] {
		"T313ccb4Main.java",
			    "\n" +
			    "public class T313ccb4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        p2.Team313ccb4_2 t = new p2.Team313ccb4_2();\n" +
			    "        p1.T313ccb4      o = new p1.T313ccb4();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T313ccb4.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T313ccb4 {\n" +
			    "    public String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/Team313ccb4_1.java",
			    "\n" +
			    "package p2;\n" +
			    "import base p1.T313ccb4;\n" +
			    "public team class Team313ccb4_1 {\n" +
			    "\n" +
			    "    public class Role313ccb4_1 playedBy T313ccb4 {}\n" +
			    "}\n" +
			    "    \n",
		"p2/Team313ccb4_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team313ccb4_2 extends Team313ccb4_1 {\n" +
			    "    public class Role313ccb4_1 {\n" +
			    "        protected String getValue(String arg) {\n" +
			    "            return arg;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class Role313ccb4_2 extends Role313ccb4_1 {\n" +
			    "        protected String getValue(String arg) => String getValue();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(p1.T313ccb4 as Role313ccb4_2 obj) {\n" +
			    "        return obj.getValue(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a concrete role method is callout-bound via => to a method in the direct base class - overriding protected native clone
    // 3.1.3-otjld-concrete-callout-binding-5
    public void test313_concreteCalloutBinding5() {
       
       runConformTest(
            new String[] {
		"T313ccb5Main.java",
			    "\n" +
			    "public class T313ccb5Main {\n" +
			    "    public static void main(String[] args)  throws CloneNotSupportedException  {\n" +
			    "        Team313ccb5 t = new Team313ccb5();\n" +
			    "        T313ccb5    o = new T313ccb5(\"OK\");\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T313ccb5.java",
			    "\n" +
			    "public class T313ccb5 {\n" +
			    "    String val;\n" +
			    "    public T313ccb5(String v) { this.val= v; }\n" +
			    "    public String getValueInternal() {\n" +
			    "        return this.val;\n" +
			    "    }\n" +
			    "    public Object clone() throws CloneNotSupportedException { return new T313ccb5(this.val); }\n" +
			    "}\n" +
			    "    \n",
		"Team313ccb5.java",
			    "\n" +
			    "public team class Team313ccb5 {\n" +
			    "    public class Role313ccb0 {\n" +
			    "        protected String getValue(String arg) {\n" +
			    "            return arg;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class Role313ccb5 extends Role313ccb0 playedBy T313ccb5 {\n" +
			    "        String getValue(String arg) => String getValueInternal();\n" +
			    "        Object clone() => Object clone();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T313ccb5 as Role313ccb5 obj) throws CloneNotSupportedException {\n" +
			    "        obj= new Role313ccb5((T313ccb5)obj.clone());\n" +
			    "        return obj.getValue(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a concrete role method is callout-bound via => and the signature to a method but the parameter names are missing
    // 3.1.4-otjld-missing-parameter-names-1
    public void test314_missingParameterNames1() {
        runNegativeTestMatching(
            new String[] {
		"Team314mpn1.java",
			    "\n" +
			    "public team class Team314mpn1 {\n" +
			    "\n" +
			    "    public class Role314mpn1 playedBy T314mpn1 {\n" +
			    "        protected abstract void getValue(String arg);\n" +
			    "        void getValue(String arg) -> void getValueInternal(String);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T314mpn1.java",
			    "\n" +
			    "public class T314mpn1 {\n" +
			    "    public void getValueInternal(String arg) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "Syntax error");
    }

    // a concrete role method is callout-bound via => and the signature to a method but the parameter names are missing
    // 3.1.4-otjld-missing-parameter-names-2
    public void test314_missingParameterNames2() {
        runNegativeTestMatching(
            new String[] {
		"Team314mpn2.java",
			    "\n" +
			    "public team class Team314mpn2 {\n" +
			    "\n" +
			    "    public class Role314mpn2 playedBy T314mpn2 {\n" +
			    "        protected abstract void getValue(String arg);\n" +
			    "        void getValue(String) -> void getValueInternal(String arg1, String arg2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T314mpn2.java",
			    "\n" +
			    "public class T314mpn2 {\n" +
			    "    public void getValueInternal(String arg1, String arg2) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "Syntax error");
    }

    // a role class overwrites an inherited callout binding from its implicit superrole via => to a different base method
    // 3.1.5-otjld-overwritten-callout-binding-1
    public void test315_overwrittenCalloutBinding1() {
       
       runConformTest(
            new String[] {
		"T315ocb1Main.java",
			    "\n" +
			    "public class T315ocb1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team315ocb1_2 t = new Team315ocb1_2();\n" +
			    "        T315ocb1      o = new T315ocb1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T315ocb1.java",
			    "\n" +
			    "public class T315ocb1 {\n" +
			    "    public String getValue1() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "    protected String getValue2() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team315ocb1_1.java",
			    "\n" +
			    "public team class Team315ocb1_1 {\n" +
			    "\n" +
			    "    public class Role315ocb1 playedBy T315ocb1 {\n" +
			    "        protected String getValue() -> String getValue1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team315ocb1_2.java",
			    "\n" +
			    "public team class Team315ocb1_2 extends Team315ocb1_1 {\n" +
			    "\n" +
			    "    public class Role315ocb1 {\n" +
			    "        getValue => getValue2;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T315ocb1 as Role315ocb1 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class overwrites an inherited callout binding from its explicit superrole via => to the overwritten base method in a subclass
    // 3.1.5-otjld-overwritten-callout-binding-2
    public void test315_overwrittenCalloutBinding2() {
       
       runConformTest(
            new String[] {
		"T315ocb2Main.java",
			    "\n" +
			    "public class T315ocb2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team315ocb2 t = new Team315ocb2();\n" +
			    "        T315ocb2_2  o = new T315ocb2_2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T315ocb2_1.java",
			    "\n" +
			    "public class T315ocb2_1 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T315ocb2_2.java",
			    "\n" +
			    "public class T315ocb2_2 extends T315ocb2_1 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team315ocb2.java",
			    "\n" +
			    "public team class Team315ocb2 {\n" +
			    "\n" +
			    "    public class Role315ocb2_1 playedBy T315ocb2_1 {\n" +
			    "        public abstract String getValue(String val);\n" +
			    "        getValue -> getValue;\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role315ocb2_2 extends Role315ocb2_1 playedBy T315ocb2_2 {\n" +
			    "        String getValue(String arg) => String getValue();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T315ocb2_2 as Role315ocb2_2 obj) {\n" +
			    "        return obj.getValue(\"NOTOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class callout-binds a concrete role method via ->
    // 3.1.6-otjld-illegal-concrete-role-method-binding
    public void test316_illegalConcreteRoleMethodBinding() {
        runNegativeTest(
            new String[] {
		"Team316icrmb_2.java",
			    "\n" +
			    "public team class Team316icrmb_2 extends Team316icrmb_1 {\n" +
			    "\n" +
			    "	 @Override\n" +
			    "    public class Role316icrmb playedBy T316icrmb {\n" +
			    "        test -> test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T316icrmb.java",
			    "\n" +
			    "public class T316icrmb {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team316icrmb_1.java",
			    "\n" +
			    "public team class Team316icrmb_1 {\n" +
			    "\n" +
			    "    public class Role316icrmb  {\n" +
			    "        protected void test() {}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team316icrmb_2.java (at line 6)\n" + 
    		"	test -> test;\n" + 
    		"	^^^^\n" + 
    		"A non-abstract role method exists for this callout-binding. Use callout-override (\'=>\') if you want to override it (OTJLD 3.1(e)).\n" + 
    		"----------\n");
    }

    // a role class callout-binds an abstract role method via =>
    // 3.1.7-otjld-illegal-abstract-role-method-binding
    public void test317_illegalAbstractRoleMethodBinding() {
        runNegativeTest(
            new String[] {
		"Team317iarmb.java",
			    "\n" +
			    "public team class Team317iarmb {\n" +
			    "\n" +
			    "    public class Role317iarmb_1 playedBy T317iarmb {}\n" +
			    "\n" +
			    "    public class Role317iarmb_2 extends Role317iarmb_1 {\n" +
			    "        abstract void test(String arg);\n" +
			    "        test => test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T317iarmb.java",
			    "\n" +
			    "public class T317iarmb {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team317iarmb.java (at line 8)\n" + 
    		"	test => test;\n" + 
    		"	^^^^\n" + 
    		"Trying to bind an abstract method as callout override (OTJLD 3.1(e)).\n" + 
    		"----------\n");
    }

    // an unbound role class has a callout binding bia ->
    // 3.1.8-otjld-callout-binding-in-unbound-role-1
    public void test318_calloutBindingInUnboundRole1() {
        runNegativeTestMatching(
            new String[] {
		"Team318cbiur1.java",
			    "\n" +
			    "public team class Team318cbiur1 {\n" +
			    "\n" +
			    "    public class Role318cbiur1 {\n" +
			    "        public abstract void test();\n" +
			    "        test -> test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "3.1(a)");
    }

    // an unbound role class has a callout binding bia =>
    // 3.1.8-otjld-callout-binding-in-unbound-role-2
    public void test318_calloutBindingInUnboundRole2() {
        runNegativeTestMatching(
            new String[] {
		"Team318cbiur2.java",
			    "\n" +
			    "public team class Team318cbiur2 {\n" +
			    "\n" +
			    "    public class Role318cbiur2 {\n" +
			    "        public abstract void test();\n" +
			    "        test => test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "3.1(a)");
    }

    // a role class callout-binds a role method via -> to a non-existing base method
    // 3.1.9-otjld-nonexisting-base-method-1
    public void test319_nonexistingBaseMethod1() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm1.java",
			    "\n" +
			    "public team class Team319nbm1 {\n" +
			    "\n" +
			    "    public class Role319nbm1 playedBy T319nbm1 {\n" +
			    "        public abstract void test(String arg);\n" +
			    "        test -> test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm1.java",
			    "\n" +
			    "public class T319nbm1 {}\n" +
			    "    \n"
            },
            "3.1(c)");
    }

    // a role class callout-binds a role method via => to a base method that has a different return type
    // 3.1.9-otjld-nonexisting-base-method-2
    public void test319_nonexistingBaseMethod2() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm2.java",
			    "\n" +
			    "public team class Team319nbm2 {\n" +
			    "\n" +
			    "    public class Role319nbm2 playedBy T319nbm2 {\n" +
			    "        public void test() {}\n" +
			    "        void test() => long getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm2.java",
			    "\n" +
			    "public class T319nbm2 {\n" +
			    "    public int getValue() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "3.1(c)");
    }

    // a role class callout-binds a role method via -> to a base method that has a return type (corrected version)
    // 3.1.9-otjld-nonexisting-base-method-3
    public void test319_nonexistingBaseMethod3() {
       
       runConformTest(
            new String[] {
		"T319nbm3Main.java",
			    "\n" +
			    "public class  T319nbm3Main {\n" +
			    "	public static void main(String[] args) {\n" +
			    "		Team319nbm3 t = new Team319nbm3();\n" +
			    "		t.test(new T319nbm3());\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n",
		"T319nbm3.java",
			    "\n" +
			    "public class T319nbm3 {\n" +
			    "    public long test() {\n" +
			    "    	System.out.print(\"OK\");\n" +
			    "	return 0L;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team319nbm3.java",
			    "\n" +
			    "public team class Team319nbm3 {\n" +
			    "\n" +
			    "    public class Role319nbm3 playedBy T319nbm3 {\n" +
			    "        public void test() -> long test();\n" +
			    "    }\n" +
			    "    public void test(T319nbm3 as Role319nbm3 r) {\n" +
			    "    	r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class callout-binds a role method via -> to a base method that has a return type but stated signature has one
    // 3.1.9-otjld-nonexisting-base-method-3f
    public void test319_nonexistingBaseMethod3f() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm3f.java",
			    "\n" +
			    "public team class Team319nbm3f {\n" +
			    "\n" +
			    "    public class Role319nbm3f playedBy T319nbm3f {\n" +
			    "        public abstract void test();\n" +
			    "        void test() -> long test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm3f.java",
			    "\n" +
			    "public class T319nbm3f {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "3.1(c)");
    }

    // a role class callout-binds a role method via => to a base method that has a no return type whereas the binding would require one
    // 3.1.9-otjld-nonexisting-base-method-4
    public void test319_nonexistingBaseMethod4() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm4.java",
			    "\n" +
			    "public team class Team319nbm4 {\n" +
			    "\n" +
			    "    public class Role319nbm4 playedBy T319nbm4 {\n" +
			    "        public int test() { return -3; }\n" +
			    "        test => test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm4.java",
			    "\n" +
			    "public class T319nbm4 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "3.2(e)");
    }

    // a role class callout-binds a role method via => to a base method that has an argument whereas the binding provides none
    // 3.1.9-otjld-nonexisting-base-method-4f
    public void test319_nonexistingBaseMethod4f() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm4f.java",
			    "\n" +
			    "public team class Team319nbm4f {\n" +
			    "\n" +
			    "    public class Role319nbm4f playedBy T319nbm4f {\n" +
			    "        public void test() {}\n" +
			    "        test => test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm4f.java",
			    "\n" +
			    "public class T319nbm4f {\n" +
			    "    public void test(int i) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "3.2(e)");
    }

    // a role class callout-binds a role method via -> to a base method that has an not-assignment compatible signature
    // 3.1.9-otjld-nonexisting-base-method-5
    public void test319_nonexistingBaseMethod5() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm5.java",
			    "\n" +
			    "public team class Team319nbm5 {\n" +
			    "\n" +
			    "    public class Role319nbm5 playedBy T319nbm5 {\n" +
			    "        public abstract int test();\n" +
			    "        test -> test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm5.java",
			    "\n" +
			    "public class T319nbm5 {\n" +
			    "    public long test() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "3.3(d)");
    }

    // a role class callout-binds a role method via -> to a base method that has an not-assignment compatible signature
    // 3.1.9-otjld-nonexisting-base-method-6
    public void test319_nonexistingBaseMethod6() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm6.java",
			    "\n" +
			    "public team class Team319nbm6 {\n" +
			    "\n" +
			    "    public class Role319nbm6 playedBy T319nbm6 {\n" +
			    "        public abstract int test();\n" +
			    "        test -> test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm6.java",
			    "\n" +
			    "public class T319nbm6 {\n" +
			    "    public String test() {\n" +
			    "        return null;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "3.3(d)");
    }

    // a role class callout-binds a role method via => to a base method that has an not-assignment compatible signature
    // 3.1.9-otjld-nonexisting-base-method-7
    public void test319_nonexistingBaseMethod7() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm7.java",
			    "\n" +
			    "public team class Team319nbm7 {\n" +
			    "\n" +
			    "    public class Role319nbm7 playedBy T319nbm7 {\n" +
			    "        public abstract void test(int arg1, int arg2);\n" +
			    "        test -> test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm7.java",
			    "\n" +
			    "public class T319nbm7 {\n" +
			    "    public void test(short arg1, long arg2) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "3.3(d)");
    }

    // a role class callout-binds a role method via -> to a base method that has an not-assignment compatible signature
    // 3.1.9-otjld-nonexisting-base-method-8
    public void test319_nonexistingBaseMethod8() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm8.java",
			    "\n" +
			    "public team class Team319nbm8 {\n" +
			    "\n" +
			    "    public class Role319nbm8 playedBy T319nbm8 {\n" +
			    "        public abstract int test();\n" +
			    "        int test() -> int test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm8.java",
			    "\n" +
			    "public class T319nbm8 {\n" +
			    "    public long test() {\n" +
			    "        return 0;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "3.1(c)");
    }

    // a role class callout-binds a role method via => to a base method that has an not-assignment compatible signature
    // 3.1.9-otjld-nonexisting-base-method-9
    public void test319_nonexistingBaseMethod9() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm9.java",
			    "\n" +
			    "public team class Team319nbm9 {\n" +
			    "\n" +
			    "    public class Role319nbm9 playedBy T319nbm9 {\n" +
			    "        public void test(int arg1, int arg2) {}\n" +
			    "        void test(int arg1, int arg2) => void test(int arg1, int arg2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm9.java",
			    "\n" +
			    "public class T319nbm9 {\n" +
			    "    public void test(short arg1, long arg2) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "3.1(c)");
    }

    // a role class callout-binds a role method via => to a base method that has more parameters than specified
    // 3.1.9-otjld-nonexisting-base-method-10
    public void test319_nonexistingBaseMethod10() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm10.java",
			    "\n" +
			    "public team class Team319nbm10 {\n" +
			    "\n" +
			    "    public class Role319nbm10 playedBy T319nbm10 {\n" +
			    "        public void test(int arg) {}\n" +
			    "        void test(int arg) => void test(int arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm10.java",
			    "\n" +
			    "public class T319nbm10 {\n" +
			    "    public void test(int arg1, int arg2) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "3.1(c)");
    }

    // a role class callout-binds a role method via -> to a base method that has a result that is compatible via lifting
    // 3.1.9-otjld-nonexisting-base-method-11
    public void test319_nonexistingBaseMethod11() {
       
       runConformTest(
            new String[] {
		"Team319nbm11.java",
			    "\n" +
			    "public team class Team319nbm11 {\n" +
			    "\n" +
			    "    public class Role319nbm11_2 playedBy T319nbm11_2 {\n" +
			    "		protected void print() { System.out.print(\"OK\"); }\n" +
			    "	}\n" +
			    "\n" +
			    "    public class Role319nbm11_1 playedBy T319nbm11_1 {\n" +
			    "        public Role319nbm11_2 test() -> T319nbm11_2 getObject();\n" +
			    "    }\n" +
			    "	public Team319nbm11() {\n" +
			    "		Role319nbm11_1 r1 = new Role319nbm11_1(new T319nbm11_1());\n" +
			    "		Role319nbm11_2 r2 = r1.test();\n" +
			    "		r2.print();\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team319nbm11();\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n",
		"T319nbm11_2.java",
			    "\n" +
			    "public class T319nbm11_2 {}\n" +
			    "    \n",
		"T319nbm11_1.java",
			    "\n" +
			    "public class T319nbm11_1 {\n" +
			    "    public T319nbm11_2 getObject() {\n" +
			    "        return new T319nbm11_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class callout-binds a role method via -> to a base method that has an argument that is compatible only via lifting
    // 3.1.9-otjld-nonexisting-base-method-12
    public void test319_nonexistingBaseMethod12() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm12_2.java",
			    "\n" +
			    "public team class Team319nbm12_2 {\n" +
			    "\n" +
			    "    public class Role319nbm12_1 playedBy Team319nbm12_1 {\n" +
			    "        public abstract void doSomething(T319nbm12 obj) {}\n" +
			    "        doSomething -> test;\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"T319nbm12.java",
			    "\n" +
			    "public class T319nbm12 {}    \n" +
			    "    \n",
		"Team319nbm12_1.java",
			    "\n" +
			    "public team class Team319nbm12_1 {\n" +
			    "        public class R playedBy T319nbm12 {}\n" +
			    "        protected void test(R obj) {}    \n" +
			    "}\n" +
			    "    \n"
            },
            "3.3(d)");
    }

    // a role class callout-binds a role method via -> to a base method that has an argument that is compatible only via lifting , playedBy is already illegal (role of the same team)
    // 3.1.9-otjld-nonexisting-base-method-12f
    public void test319_nonexistingBaseMethod12f() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm12f.java",
			    "\n" +
			    "public team class Team319nbm12f {\n" +
			    "\n" +
			    "    public class Role319nbm12f_1 playedBy Role319nbm12f_3 {\n" +
			    "        public void doSomething(T319nbm12f obj) {}\n" +
			    "        doSomething => test;\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role319nbm12f_2 playedBy T319nbm12f {}\n" +
			    "\n" +
			    "    public class Role319nbm12f_3 {\n" +
			    "        protected void test(Role319nbm12f_2 obj) {}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm12f.java",
			    "\n" +
			    "public class T319nbm12f {}\n" +
			    "    \n"
            },
            "2.1.2(a)");
    }

    // a role class callout-binds a role method via => to a base method that has an argument that is compatible via lowering
    // 3.1.9-otjld-nonexisting-base-method-13
    public void test319_nonexistingBaseMethod13() {
       
       runConformTest(
            new String[] {
		"T319nbm13Main.java",
			    "\n" +
			    "public class T319nbm13Main {\n" +
			    "	public static void main(String[] args) {\n" +
			    "		Team319nbm13 t = new Team319nbm13();\n" +
			    "		t.test(new T319nbm13_1(), new T319nbm13_2(\"OK\"));\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n",
		"T319nbm13_2.java",
			    "\n" +
			    "public class T319nbm13_2 {\n" +
			    "	private String value;\n" +
			    "	public T319nbm13_2 (String val) {\n" +
			    "		this.value = val;\n" +
			    "	}\n" +
			    "	public String getValue() { \n" +
			    "		return value;\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n",
		"T319nbm13_1.java",
			    "\n" +
			    "public class T319nbm13_1 {\n" +
			    "    public void test(T319nbm13_2 obj) {\n" +
			    "    	System.out.print(obj.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team319nbm13.java",
			    "\n" +
			    "public team class Team319nbm13 {\n" +
			    "\n" +
			    "    public class Role319nbm13_2 playedBy T319nbm13_2 {}\n" +
			    "\n" +
			    "    public class Role319nbm13_1 playedBy T319nbm13_1 {\n" +
			    "        public void test(Role319nbm13_2 obj) -> void test(T319nbm13_2 obj);\n" +
			    "    }\n" +
			    "    public void test(T319nbm13_1 as Role319nbm13_1 r1, T319nbm13_2 as Role319nbm13_2 r2) \n" +
			    "    {\n" +
			    "    	r1.test(r2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class callout-binds a role method via -> to a base method that has a result that is compatible via lowering
    // 3.1.9-otjld-nonexisting-base-method-14
    public void test319_nonexistingBaseMethod14() {
        runNegativeTestMatching(
            new String[] {
		"Team319nbm14_2.java",
			    "\n" +
			    "public team class Team319nbm14_2 {\n" +
			    "\n" +
			    "    public class Role319nbm14_1 playedBy Team319nbm14_1 {\n" +
			    "        public abstract T319nbm14 test();\n" +
			    "        T319nbm14 test() -> R<@base> test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T319nbm14.java",
			    "\n" +
			    "public class T319nbm14 {}\n" +
			    "    \n",
		"Team319nbm14_1.java",
			    "\n" +
			    "public team class Team319nbm14_1 {\n" +
			    "    public class R playedBy T319nbm14 {}\n" +
			    "    public R test() { return null;}\n" +
			    "}    \n" +
			    "    \n"
            },
            "2.2(b)");
    }

    // a role class callout-binds a role method via -> but states a modifier in the binding
    // 3.1.10-otjld-binding-states-modifier-1
    public void test3110_bindingStatesModifier1() {
        runNegativeTestMatching(
            new String[] {
		"Team3110bsm1.java",
			    "\n" +
			    "public team class Team3110bsm1 {\n" +
			    "\n" +
			    "    public class Role3110bsm1 playedBy T3110bsm1 {\n" +
			    "        public abstract void test(String arg);\n" +
			    "        public test -> test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3110bsm1.java",
			    "\n" +
			    "public class T3110bsm1 {\n" +
			    "    public void test(String arg) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.2");
    }

    // a role class callout-binds a role method  but states a modifier in the binding
    // 3.1.10-otjld-binding-states-modifier-2
    public void test3110_bindingStatesModifier2() {
        runNegativeTestMatching(
            new String[] {
		"Team3110bsm2.java",
			    "\n" +
			    "public team class Team3110bsm2 {\n" +
			    "\n" +
			    "    public class Role3110bsm2 playedBy T3110bsm2 {\n" +
			    "        void test(String arg) -> public void test(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3110bsm2.java",
			    "\n" +
			    "public class T3110bsm2 {\n" +
			    "    protected void test(String arg) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.5");
    }

    // a role class callout-binds a role method via => but states a modifier in the binding
    // 3.1.10-otjld-binding-states-modifier-3
    public void test3110_bindingStatesModifier3() {
        runNegativeTestMatching(
            new String[] {
		"Team3110bsm3.java",
			    "\n" +
			    "public team class Team3110bsm3 {\n" +
			    "\n" +
			    "    protected class R {\n" +
			    "        protected void test(String arg) {}\n" +
			    "    }\n" +
			    "    \n" +
			    "    public class Role3110bsm3 extends R playedBy T3110bsm3 {\n" +
			    "        final void test(String arg) => void test(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3110bsm3.java",
			    "\n" +
			    "public class T3110bsm3 {\n" +
			    "    protected void test(String arg) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.2");
    }

    // a role class callout-binds a role method via -> but states a modifier in the binding
    // 3.1.10-otjld-binding-states-modifier-4
    public void test3110_bindingStatesModifier4() {
        runNegativeTestMatching(
            new String[] {
		"Team3110bsm4.java",
			    "\n" +
			    "public team class Team3110bsm4 {\n" +
			    "\n" +
			    "    public class Role3110bsm4 playedBy T3110bsm4 {\n" +
			    "        protected abstract void test(String arg);\n" +
			    "        abstract void test(String arg) -> void test(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3110bsm4.java",
			    "\n" +
			    "public class T3110bsm4 {\n" +
			    "    protected void test(String arg) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.2");
    }

    // a role class callout-binds a role method but states a modifier in the binding
    // 3.1.10-otjld-binding-states-modifier-5
    public void test3110_bindingStatesModifier5() {
        runNegativeTestMatching(
            new String[] {
		"Team3110bsm5.java",
			    "\n" +
			    "public team class Team3110bsm5 {\n" +
			    "\n" +
			    "    public class Role3110bsm5 playedBy T3110bsm5 {\n" +
			    "        void test(String arg) -> static void test(String arg);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3110bsm5.java",
			    "\n" +
			    "public class T3110bsm5 {\n" +
			    "    protected void test(String arg) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "A.3.5");
    }

    // a callout long with modifier follows a callin short, witness for bogus error message
    // 3.1.10-otjld-binding-states-modifier-6
    public void test3110_bindingStatesModifier6() {
        runConformTest(
            new String[] {
		"T3110bsm6.java",
			    "\n" +
			    "public class T3110bsm6 {\n" +
			    "    public void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team3110bsm6.java",
			    "\n" +
			    "public team class Team3110bsm6 {\n" +
			    "    protected class R playedBy T3110bsm6 {\n" +
			    "        void bar() {}\n" +
			    "        bar <- after test;\n" +
			    "        protected void myTest() -> void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            });
    }

    // a role overwites an explicitly inherited callout-binding via ->
    // 3.1.11-otjld-illegal-overwriting-inherited-binding-1
    public void test3111_illegalOverwritingInheritedBinding1() {
        runNegativeTest(
            new String[] {
		"Team3111ioib1.java",
			    "\n" +
			    "public team class Team3111ioib1 {\n" +
			    "\n" +
			    "    public class Role3111ioib1_1 playedBy T3111ioib1 {\n" +
			    "        public abstract String getValue();\n" +
			    "        String getValue() -> String getValue1();\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role3111ioib1_2 extends Role3111ioib1_1 {\n" +
			    "        String getValue() -> String getValue2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3111ioib1.java",
			    "\n" +
			    "public class T3111ioib1 {\n" +
			    "    protected String getValue1() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "    protected String getValue2() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team3111ioib1.java (at line 10)\n" + 
    		"	String getValue() -> String getValue2();\n" + 
    		"	^^^^^^^^^^^^^^^^^\n" + 
    		"Callout binding conflicts with inherited callout binding from class Team3111ioib1.Role3111ioib1_1. Use \'=>\' if you want to override it (OTJLD 3.1(f)).\n" + 
    		"----------\n");
    }

    // a role overwites an implicitly inherited callout-binding via ->
    // 3.1.11-otjld-illegal-overwriting-inherited-binding-2
    public void test3111_illegalOverwritingInheritedBinding2() {
        runNegativeTest(
            new String[] {
		"Team3111ioib2_2.java",
			    "\n" +
			    "public team class Team3111ioib2_2 extends Team3111ioib2_1 {\n" +
			    "	 @Override\n" +
			    "    public class Role3111ioib2 {\n" +
			    "        test1 -> test2;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3111ioib2.java",
			    "\n" +
			    "public class T3111ioib2 {\n" +
			    "    protected void test1() {}\n" +
			    "    protected void test2() {}\n" +
			    "}\n" +
			    "    \n",
		"Team3111ioib2_1.java",
			    "\n" +
			    "public team class Team3111ioib2_1 {\n" +
			    "    public class Role3111ioib2 playedBy T3111ioib2 {\n" +
			    "        void test1() -> void test1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team3111ioib2_2.java (at line 5)\n" + 
    		"	test1 -> test2;\n" + 
    		"	^^^^^\n" + 
    		"Callout binding conflicts with inherited callout binding from class Team3111ioib2_1.Role3111ioib2. Use \'=>\' if you want to override it (OTJLD 3.1(f)).\n" + 
    		"----------\n");
    }

    // a role overwites an inherited callout-binding via ->
    // 3.1.11-otjld-illegal-overwriting-inherited-binding-3
    public void test3111_illegalOverwritingInheritedBinding3() {
        runNegativeTest(
            new String[] {
		"Team3111ioib3_2.java",
			    "\n" +
			    "public team class Team3111ioib3_2 extends Team3111ioib3_1 {\n" +
			    "    public class Role3111ioib3_2 extends Role3111ioib3_1 {\n" +
			    "        test -> test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3111ioib3.java",
			    "\n" +
			    "public class T3111ioib3 {\n" +
			    "    protected void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team3111ioib3_1.java",
			    "\n" +
			    "public team class Team3111ioib3_1 {\n" +
			    "    public class Role3111ioib3_1 playedBy T3111ioib3 {\n" +
			    "        void test() -> void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team3111ioib3_2.java (at line 4)\n" + 
    		"	test -> test;\n" + 
    		"	^^^^\n" + 
    		"Callout binding conflicts with inherited callout binding from class Team3111ioib3_1.Role3111ioib3_1. Use \'=>\' if you want to override it (OTJLD 3.1(f)).\n" + 
    		"----------\n");
    }

    // a role binds to a base method via the name but there is more than one method of that name
    // 3.1.12-otjld-ambiguous-base-method-name-1
    public void test3112_ambiguousBaseMethodName1() {
        runNegativeTestMatching(
            new String[] {
		"Team3112abmn1.java",
			    "\n" +
			    "public team class Team3112abmn1 {\n" +
			    "    public class Role3112abmn1 playedBy T3112abmn1 {\n" +
			    "        public void test() {}\n" +
			    "        test => test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3112abmn1.java",
			    "\n" +
			    "public class T3112abmn1 {\n" +
			    "    protected void test() {}\n" +
			    "    public void test(String arg) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "3.1(c)");
    }

    // a role binds to a base method via the name but there is more than one method of that name
    // 3.1.12-otjld-ambiguous-base-method-name-2
    public void test3112_ambiguousBaseMethodName2() {
        runNegativeTestMatching(
            new String[] {
		"Team3112abmn2_2.java",
			    "\n" +
			    "public team class Team3112abmn2_2 extends Team3112abmn2_1 {\n" +
			    "    public class Role3112abmn2 {\n" +
			    "        test => test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3112abmn2.java",
			    "\n" +
			    "public class T3112abmn2 {\n" +
			    "    protected void test(short arg) {}\n" +
			    "    public void test(long arg) {}\n" +
			    "}\n" +
			    "    \n",
		"Team3112abmn2_1.java",
			    "\n" +
			    "public team class Team3112abmn2_1 {\n" +
			    "    public class Role3112abmn2 playedBy T3112abmn2 {\n" +
			    "        public void test(int arg) {}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "3.1(c)");
    }

    // a role binds to a base method via the name but there is more than one method of that name
    // 3.1.12-otjld-ambiguous-base-method-name-3
    public void test3112_ambiguousBaseMethodName3() {
        runNegativeTestMatching(
            new String[] {
		"Team3112abmn3_2.java",
			    "\n" +
			    "public team class Team3112abmn3_2 extends Team3112abmn3_1 {\n" +
			    "    public class Role3112abmn3 extends Role3112abmn3_1 playedBy T3112abmn3_2 {\n" +
			    "        public abstract void test(String arg);\n" +
			    "        test -> test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3112abmn3_1.java",
			    "\n" +
			    "public class T3112abmn3_1 {\n" +
			    "    protected void test() {}\n" +
			    "}\n" +
			    "    \n",
		"T3112abmn3_2.java",
			    "\n" +
			    "public class T3112abmn3_2 extends T3112abmn3_1 {\n" +
			    "    public void test(String arg) {}\n" +
			    "}\n" +
			    "    \n",
		"Team3112abmn3_1.java",
			    "\n" +
			    "public team class Team3112abmn3_1 {\n" +
			    "    public class Role3112abmn3_1 playedBy T3112abmn3_1 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "3.1(c)");
    }

    // a static base method is bound via callout
    // 3.1.13-otjld-callout-to-static-1
    public void test3113_calloutToStatic1() {
       
       runConformTest(
            new String[] {
		"T3113cts1Main.java",
			    "\n" +
			    "public class T3113cts1Main {\n" +
			    "	public static void main(String[] args) {\n" +
			    "		Team3113cts1 t = new Team3113cts1();\n" +
			    "		t.test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T3113cts1.java",
			    "\n" +
			    "public class T3113cts1 {\n" +
			    "	public static String getValue() {\n" +
			    "		return \"OK\";\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team3113cts1.java",
			    "\n" +
			    "public team class Team3113cts1 {\n" +
			    "	protected class R3113cts1 playedBy T3113cts1 {\n" +
			    "		protected abstract static String getV();\n" +
			    "		getV -> getValue;\n" +
			    "	}\n" +
			    "	public void test() {\n" +
			    "		System.out.print(R3113cts1.getV());\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }
    
    // missing abstract modifier for role with abstract static method
    public void test3113_calloutToStatic2() {
        runNegativeTest(
             new String[] {
 		"Team3113cts2.java",
 			    "\n" +
 			    "public team class Team3113cts2 {\n" +
 			    "	protected class R3113cts2 {\n" +
 			    "		protected abstract static String getV();\n" +
 			    "	}\n" +
 			    "	public void test() {\n" +
 			    "		System.out.print(R3113cts2.getV());\n" +
 			    "	}\n" +
 			    "}\n"
             },
             "----------\n" + 
     		 "1. ERROR in Team3113cts2.java (at line 3)\n" + 
     		 "	protected class R3113cts2 {\n" + 
     		 "	                ^^^^^^^^^\n" + 
     		 "The type R3113cts2 must be an abstract class to define abstract methods\n" + 
             "----------\n" + 
     		 "2. ERROR in Team3113cts2.java (at line 4)\n" + 
     		 "	protected abstract static String getV();\n" + 
     		 "	                                 ^^^^^^\n" + 
     		 "The abstract method getV in type R3113cts2 can only be defined by an abstract class\n" + 
     		 "----------\n");
     }
    
    // abstract static method bound by callout in implicit sub-role
    // Bug 336394 - [compiler] ClassFormatError caused by abstract static method 
    public void test3113_calloutToStatic3() {
        runConformTest(
             new String[] {
 		"T3113cts3Main.java",
 			    "\n" +
 			    "public class T3113cts3Main {\n" +
 			    "	public static void main(String[] args) {\n" +
 			    "		Team3113cts3_1 t = new Team3113cts3_2();\n" +
 			    "		t.test();\n" +
 			    "	}\n" +
 			    "}\n" +
 			    "	\n",
 		"T3113cts3.java",
 			    "\n" +
 			    "public class T3113cts3 {\n" +
 			    "	public static String getValue() {\n" +
 			    "		return \"OK\";\n" +
 			    "	}\n" +
 			    "}\n" +
 			    "	\n",
 		"Team3113cts3_1.java",
 			    "\n" +
 			    "public team class Team3113cts3_1 {\n" +
 			    "	protected abstract class R3113cts3 {\n" +
 			    "		protected abstract static String getV();\n" +
 			    "	}\n" +
 			    "	public void test() {\n" +
 			    "		System.out.print(R3113cts3.getV());\n" + // illegal call unless we copy this team method to subteams
 			    "	}\n" +
 			    "}\n",
 		"Team3113cts3_2.java",
 			    "\n" +
 			    "public team class Team3113cts3_2 extends Team3113cts3_1 {\n" +
 			    "	protected class R3113cts3 playedBy T3113cts3 {\n" +
			    "		getV -> getValue;\n" +
 			    "	}\n" +
 			    "}\n"
             },
             "OK");
     }
    
    // abstract static method bound by callout in implicit sub-role
    // Bug 336394 - [compiler] ClassFormatError caused by abstract static method 
    public void test3113_calloutToStatic4() {
        runConformTest(
             new String[] {
 		"T3113cts4Main.java",
 			    "\n" +
 			    "public class T3113cts4Main {\n" +
 			    "	public static void main(String[] args) {\n" +
 			    "		Team3113cts4_1 t = new Team3113cts4_2();\n" +
 			    "		t.test();\n" +
 			    "	}\n" +
 			    "}\n" +
 			    "	\n",
 		"T3113cts4.java",
 			    "\n" +
 			    "public class T3113cts4 {\n" +
 			    "	public static String getValue() {\n" +
 			    "		return \"OK\";\n" +
 			    "	}\n" +
 			    "}\n" +
 			    "	\n",
 		"Team3113cts4_1.java",
 			    "\n" +
 			    "public team class Team3113cts4_1 {\n" +
 			    "	protected abstract class R3113cts4_1 {\n" +
 			    "		protected abstract static String getV();\n" +
 			    "	}\n" +
 			    "	protected class R3113cts4_2 {\n" +
 			    "       protected String test() { return R3113cts4_1.getV(); }\n" + // static role method called from a role instance
 			    "	}\n" +
 			    "	public void test() {\n" +
 			    "		System.out.print(new R3113cts4_2().test());\n" +
 			    "	}\n" +
 			    "}\n",
 		"Team3113cts4_2.java",
 			    "\n" +
 			    "public team class Team3113cts4_2 extends Team3113cts4_1 {\n" +
 			    "	protected class R3113cts4_1 playedBy T3113cts4 {\n" +
			    "		getV -> getValue;\n" +
 			    "	}\n" +
 			    "}\n"
             },
             "OK");
     }

    // Bug 397235 - [compiler] cannot bind to a static role method
    // anchored access to static method - static anchor
    public void test3113_calloutToStatic5() {
    	runConformTest(new String[] {
    		"Team3113cts5_2.java",
    			"public team class Team3113cts5_2 {\n" +
				"    final static Team3113cts5_1 other = new Team3113cts5_1();\n" +
    			"    protected class R playedBy R<@other> {\n" +
    			"        void test() -> void test();\n" +
    			"    }\n" +
    			"    void test() {\n" +
    			"        R.test();\n" +
    			"    }\n" +
    			"    public static void main(String[] args) {\n" +
    			"        other.val = \"OK\";\n" +
    			"        new Team3113cts5_2().test();\n" +
    			"    }\n" +
    			"}\n",
    		"Team3113cts5_1.java",
    			"public team class Team3113cts5_1 {\n" +
    			"public String val;\n" +
    			"public class R {\n" +
    			"    public static void test() {\n" +
    			"        System.out.print(val);\n" +
    			"    }\n" +
    			"}\n" +
    			"public R getR() { return new R(); }\n" +
    			"}\n"
    	},
    	"OK");
    }


    // Bug 397235 - [compiler] cannot bind to a static role method
    // anchored access to static method - non-static anchor, two segments
    public void test3113_calloutToStatic6() {
    	runConformTest(new String[] {
    		"Team3113cts6_2.java",
    			"public team class Team3113cts6_2 {\n" +
				"    final Team3113cts6_1 other = new Team3113cts6_1();\n" +
    			"    protected class R playedBy R<@other.self> {\n" +
    			"        void test() -> void test();\n" +
    			"    }\n" +
    			"    void test() {\n" +
    			"        R.test();\n" +
    			"    }\n" +
    			"    public static void main(String[] args) {\n" + 
    			"        Team3113cts6_2 t = new Team3113cts6_2();" +
    			"        t.other.val = \"OK\";\n" +
    			"        t.test();\n" +
    			"    }\n" +
    			"}\n",
    		"Team3113cts6_1.java",
    			"public team class Team3113cts6_1 {\n" +
    			"    final public Team3113cts6_1 self = this;"+
    			"    public String val;\n" +
    			"    public class R {\n" +
    			"        public static void test() {\n" +
    			"            System.out.print(val);\n" +
    			"        }\n" +
    			"    }\n" +
    			"    public R getR() { return new R(); }\n" +
    			"}\n"
    	},
    	"OK");
    }


    // A callout binding creates a role method
    // 3.1.14-otjld-callout-without-role-method-1
    public void test3114_calloutWithoutRoleMethod1() {
       
       runConformTest(
            new String[] {
		"Team3114cwrm1.java",
			    "\n" +
			    "public team class Team3114cwrm1 {\n" +
			    "	protected class R playedBy T3114cwrm1 {\n" +
			    "		void test(String v) -> void test (String val);\n" +
			    "	}\n" +
			    "	Team3114cwrm1(T3114cwrm1 as R r) {\n" +
			    "		r.test(\"OK\");\n" +
			    "	}\n" +
			    "	public static void main (String[] args) {\n" +
			    "		new Team3114cwrm1(new T3114cwrm1());\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"T3114cwrm1.java",
			    "\n" +
			    "public class T3114cwrm1 {\n" +
			    "	protected void test(String val) {\n" +
			    "		System.out.print(val);\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // public callout method in protected role - should not report warning
    // 3.1.14-otjld-callout-without-role-method-2
    public void test3114_calloutWithoutRoleMethod2() {
       
       runConformTest(
            new String[] {
		"Team3114cwrm2.java",
			    "\n" +
			    "public team class Team3114cwrm2 {\n" +
			    "        protected class R playedBy T3114cwrm2 {\n" +
			    "                R getMe() -> T3114cwrm2 getMe();\n" +
			    "                protected void ok() { System.out.print(\"OK\"); }\n" +
			    "        }\n" +
			    "        Team3114cwrm2(T3114cwrm2 as R r) {\n" +
			    "                r.ok();\n" +
			    "        }\n" +
			    "        public static void main (String[] args) {\n" +
			    "                new Team3114cwrm2(new T3114cwrm2());\n" +
			    "        }\n" +
			    "}\n" +
			    "        \n",
		"T3114cwrm2.java",
			    "\n" +
			    "public class T3114cwrm2 {\n" +
			    "        public T3114cwrm2 getMe() { return this; }\n" +
			    "}\n" +
			    "        \n"
            },
            "OK");
    }

    // a local class calls a callout of the enclosing role
    // 3.1.15-otjld-callout-called-within-localtype-1
    public void test3115_calloutCalledWithinLocaltype1() {
       
       runConformTest(
            new String[] {
		"Team3115ccwl1.java",
			    "\n" +
			    "public team class Team3115ccwl1 {\n" +
			    "    protected class R playedBy T3115ccwl1 {\n" +
			    "        protected R() {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "        void test() -> void test();\n" +
			    "        protected void runit() { \n" +
			    "            new Runnable() {\n" +
			    "                public void run() {\n" +
			    "                    test();\n" +
			    "                }\n" +
			    "            }.run();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team3115ccwl1() {\n" +
			    "        new R().runit();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3115ccwl1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3115ccwl1.java",
			    "\n" +
			    "public class T3115ccwl1 {\n" +
			    "    void test() { System.out.print(\"O\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a local class calls a callout of the enclosing role - broken other callout binding
    // 3.1.15-otjld-callout-called-within-localtype-2
    public void test3115_calloutCalledWithinLocaltype2() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
        runNegativeTest(
            new String[] {
		"Team3115ccwl2.java",
			    "\n" +
			    "public team class Team3115ccwl2 {\n" +
			    "    protected class R playedBy T3115ccwl2 {\n" +
			    "        R() {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "        void test() -> void test();\n" +
			    "        void broken() -> void inexistent();\n" +
			    "        void runit() { \n" +
			    "            new Runnable() {\n" +
			    "                public void run() {\n" +
			    "                    test();\n" +
			    "                }\n" +
			    "            }.run();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3115ccwl2.java",
			    "\n" +
			    "public class T3115ccwl2 {\n" +
			    "    void test() { System.out.print(\"O\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team3115ccwl2.java (at line 8)\n" + 
    		"	void broken() -> void inexistent();\n" + 
    		"	                 ^^^^^^^^^^^^^^^^^\n" + 
    		"No method inexistent() found in type T3115ccwl2 to resolve method designator (OTJLD 3.1(c)).\n" + 
    		"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }

    // two callouts have the same name and signature [reported by Marco]
    // 3.1.16-otjld-duplicate-callout-1
    public void test3116_duplicateCallout1() {
        runNegativeTestMatching(
            new String[] {
		"Team3116dc1.java",
			    "\n" +
			    "public team class Team3116dc1 {\n" +
			    "    protected class R playedBy T3116dc1 {\n" +
			    "        void rm() -> void bm1();\n" +
			    "        void rm() -> void bm2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3116dc1.java",
			    "\n" +
			    "public class T3116dc1 {\n" +
			    "    void bm1() {}\n" +
			    "    void bm2() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "3.1(g)");
    }

    // a callout binding is inferred from a declared interface
    // 3.1.17-otjld-inferred-callout-1
    public void test3117_inferredCallout1() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3117ic1.java",
			    "\n" +
			    "public team class Team3117ic1 {\n" +
			    "    @SuppressWarnings(\"inferredcallout\")\n" +
			    "    protected class R implements I3117ic1 playedBy T3117ic1 {\n" +
			    "    }\n" +
			    "    Team3117ic1() {\n" +
			    "        new R(new T3117ic1()).test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3117ic1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"I3117ic1.java",
			    "\n" +
			    "public interface I3117ic1 {\n" +
			    "    void test();\n" +
			    "}\n" +
			    "    \n",
		"T3117ic1.java",
			    "\n" +
			    "public class T3117ic1 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
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

    // a callout binding is inferred from a declared interface - parameter with lowering
    // 3.1.17-otjld-inferred-callout-2
    public void test3117_inferredCallout2() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3117ic2.java",
			    "\n" +
			    "public team class Team3117ic2 {\n" +
			    "    protected interface I {\n" +
			    "        void test(R other);\n" +
			    "    }\n" +
			    "    protected class R implements I playedBy T3117ic2 {\n" +
			    "    }\n" +
			    "    Team3117ic2() {\n" +
			    "        R r1 = new R(new T3117ic2());\n" +
			    "        I r2 = new R(new T3117ic2());\n" +
			    "        r2.test(r1);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3117ic2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3117ic2.java",
			    "\n" +
			    "public class T3117ic2 {\n" +
			    "    public void test(T3117ic2 other) {\n" +
			    "        other.print();\n" +
			    "    }\n" +
			    "    private void print() {\n" +
			    "        System.out.print(\"OK\");\n" +
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

    // a callout binding is inferred from an unresolved self-call
    // 3.1.17-otjld-inferred-callout-3
    public void test3117_inferredCallout3() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3117ic3.java",
			    "\n" +
			    "public team class Team3117ic3 {\n" +
			    "    protected class R playedBy T3117ic3 {\n" +
			    "        @SuppressWarnings(\"inferredcallout\")\n" +
			    "        protected int run() {\n" +
			    "            return test(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team3117ic3() {\n" +
			    "        System.out.print(new R(new T3117ic3()).run());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3117ic3();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3117ic3.java",
			    "\n" +
			    "public class T3117ic3 {\n" +
			    "    public int test(String a) {\n" +
			    "        System.out.print(a);\n" +
			    "        return 32;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK32",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a callout binding is inferred from an unresolved self-call, not overridable
    // 3.1.17-otjld-inferred-callout-4
    public void test3117_inferredCallout4() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.IGNORE);
       
       runConformTest(
            new String[] {
		"Team3117ic4.java",
			    "\n" +
			    "public team class Team3117ic4 {\n" +
			    "    protected class R0 playedBy T3117ic4 {\n" +
			    "        protected int run() {\n" +
			    "            return test(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected class R extends R0 {\n" +
			    "        protected int test(String a) {\n" +
			    "            System.out.print(\"NOK\");\n" +
			    "            return -1;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team3117ic4() {\n" +
			    "        System.out.print(new R(new T3117ic4()).run());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3117ic4();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3117ic4.java",
			    "\n" +
			    "public class T3117ic4 {\n" +
			    "    public int test(String a) {\n" +
			    "        System.out.print(a);\n" +
			    "        return 32;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK32",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a callout binding is inferred from an unresolved self-call - used in boolean expression
    // 3.1.17-otjld-inferred-callout-5
    public void test3117_inferredCallout5() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3117ic5.java",
			    "\n" +
			    "public team class Team3117ic5 {\n" +
			    "    protected class R playedBy T3117ic5 {\n" +
			    "        @SuppressWarnings(\"inferredcallout\")\n" +
			    "        protected int run() {\n" +
			    "            if (test(\"OK\") && true)\n" +
			    "                return 32;\n" +
			    "            return -1;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team3117ic5() {\n" +
			    "        System.out.print(new R(new T3117ic5()).run());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3117ic5();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3117ic5.java",
			    "\n" +
			    "public class T3117ic5 {\n" +
			    "    public boolean test(String a) {\n" +
			    "        System.out.print(a);\n" +
			    "        return \"OK\".equals(a);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK32",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // an inferred callout tries to refer to an inherited private method (see Trac #96)
    // 3.1.17-otjld-inferred-callout-6
    public void test3117_inferredCallout6() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
        runNegativeTest(
            new String[] {
		"Team3117ic6.java",
			    "\n" +
			    "public team class Team3117ic6 {\n" +
			    "    @SuppressWarnings(\"decapsulation\")\n" +
			    "    protected class R playedBy T3117ic6_2 {\n" +
			    "        @SuppressWarnings(\"inferredcallout\")\n" +
			    "        void thief() {\n" +
			    "            secret();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3117ic6_1.java",
			    "\n" +
			    "public class T3117ic6_1 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private void secret() {}\n" +
			    "}\n" +
			    "    \n",
		"T3117ic6_2.java",
			    "\n" +
			    "public class T3117ic6_2 extends T3117ic6_1 {}\n" +
			    "    \n"
            },
            "----------\n" +
            "1. ERROR in Team3117ic6.java (at line 7)\n" +
			"	secret();\n" +
			"	^^^^^^^^\n" +
			"The private base feature secret() from type T3117ic6_1 is not available via the base type T3117ic6_2 (OTJLD 3.4(d)). \n" +
			"----------\n" +
			"2. ERROR in Team3117ic6.java (at line 7)\n" +
			"	secret();\n" +
			"	^^^^^^\n" +
			"The method secret() is undefined for the type Team3117ic6.R\n" +
			"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }

    // an inferred callout tries to refer to an inherited private method - trying to execute buggy class (see Trac #96)
    // 3.1.17-otjld-inferred-callout-7
    public void test3117_inferredCallout7() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
        runTest(
            new String[] {
		"Team3117ic7.java",
			    "\n" +
			    "public team class Team3117ic7 {\n" +
			    "    @SuppressWarnings(\"decapsulation\")\n" +
			    "    protected class R playedBy T3117ic7_2 {\n" +
			    "        @SuppressWarnings(\"inferredcallout\")\n" +
			    "        protected void thief() {\n" +
			    "            secret();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team3117ic7() {\n" +
			    "        R r= new R(new T3117ic7_2());\n" +
			    "        r.thief();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3117ic7();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3117ic7_1.java",
			    "\n" +
			    "public class T3117ic7_1 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private void secret() { System.out.print(\"SECRET\");}\n" +
			    "}\n" +
			    "    \n",
		"T3117ic7_2.java",
			    "\n" +
			    "public class T3117ic7_2 extends T3117ic7_1 {}\n" +
			    "    \n"
            },
            true/*expectingCompilerErrors*/,
            "----------\n" + 
            "1. ERROR in Team3117ic7.java (at line 7)\n" + 
            "	secret();\n" + 
            "	^^^^^^^^\n" + 
            "The private base feature secret() from type T3117ic7_1 is not available via the base type T3117ic7_2 (OTJLD 3.4(d)). \n" + 
            "----------\n" + 
            "2. ERROR in Team3117ic7.java (at line 7)\n" + 
            "	secret();\n" + 
            "	^^^^^^\n" + 
            "The method secret() is undefined for the type Team3117ic7.R\n" + 
            "----------\n",
            ""/*expectedOutputString*/,
            "java.lang.Error: Unresolved compilation problems: \n" + 
    		"	The private base feature secret() from type T3117ic7_1 is not available via the base type T3117ic7_2 (OTJLD 3.4(d)). \n" + 
    		"	The method secret() is undefined for the type Team3117ic7.R\n" + 
    		"\n" + 
    		"	at Team3117ic7$__OT__R.thief(Team3117ic7.java:7)\n" + 
    		"	at Team3117ic7.<init>(Team3117ic7.java:12)\n" + 
    		"	at Team3117ic7.main(Team3117ic7.java:15)\n" + 
//    		"	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" + 
//    		"	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n" + 
//    		"	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n" + 
//    		"	at java.lang.reflect.Method.invoke(Method.java:597)\n" + 
    		"	at"/*expectedErrorString*/,
            true/*forceExecution*/,
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,     		
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/,
        	true/*skipJavac*/);
    }

    // inferred callout to a static method, called in static context
    // 3.1.17-otjld-inferred-callout-8
    public void test3117_inferredCallout8() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.IGNORE);
       
       runConformTest(
            new String[] {
		"Team3117ic8.java",
			    "\n" +
			    "public team class Team3117ic8 {\n" +
			    "    protected class R playedBy T3117ic8 {\n" +
			    "        static protected void test() {\n" +
			    "            System.out.print(getOK());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    Team3117ic8() {\n" +
			    "        R.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3117ic8();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3117ic8.java",
			    "\n" +
			    "public class T3117ic8 {\n" +
			    "    public static String getOK() { return \"OK\"; }\n" +
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

    // multiple uses of inferred callouts should produce multiple warnings
    // 3.1.17-otjld-inferred-callout-9
    public void test3117_inferredCallout9() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
        runTestExpectingWarnings(
            new String[] {
		"T3117ic9.java",
			    "\n" +
			    "public class T3117ic9 {\n" +
			    "    double aField;\n" +
			    "    char aMethod(int in) { return (char)in; }\n" +
			    "}\n" +
			    "    \n",
		"Team3117ic9.java",
			    "\n" +
			    "public team class Team3117ic9 {\n" +
			    "    protected class R playedBy T3117ic9 {\n" +
			    "        void doit() {\n" +
			    "            System.out.print(aField);\n" +
			    "            System.out.print(aField);\n" +
			    "            System.out.print(aMethod(13));\n" +
			    "            System.out.print(aMethod(14));\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
			"1. WARNING in Team3117ic9.java (at line 5)\n" + 
			"	System.out.print(aField);\n" + 
			"	                 ^^^^^^\n" + 
			"Reference double aField implicitly uses callout getter to base field \'double aField\' (OTJLD 3.5(h)).\n" + 
			"----------\n" + 
			"2. WARNING in Team3117ic9.java (at line 6)\n" + 
			"	System.out.print(aField);\n" + 
			"	                 ^^^^^^\n" + 
			"Reference double aField implicitly uses callout getter to base field \'double aField\' (OTJLD 3.5(h)).\n" + 
			"----------\n" + 
			"3. WARNING in Team3117ic9.java (at line 7)\n" + 
			"	System.out.print(aMethod(13));\n" + 
			"	                 ^^^^^^^^^^^\n" + 
			"Unresolved self call aMethod(int) is implicitly bound by an inferred callout (OTJLD 3.1(j)).\n" + 
			"----------\n" + 
			"4. WARNING in Team3117ic9.java (at line 8)\n" + 
			"	System.out.print(aMethod(14));\n" + 
			"	                 ^^^^^^^^^^^\n" + 
			"Unresolved self call aMethod(int) is implicitly bound by an inferred callout (OTJLD 3.1(j)).\n" + 
			"----------\n",
            customOptions);
    }

    // Bug 326689 -  [compiler] inferred callout not working with overloads and same-named callin method
    public void test3117_inferredCallout10() {
        Map customOptions = getCompilerOptions();
        customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);

    	runConformTest(
    		new String[]{
    	"Team3117ic10.java",
    			"public team class Team3117ic10 {\n" + 
				"\n" + 
				"	protected class R playedBy T3117ic10 {\n" + 
				"\n" + 
				"		String foo(String s, int i) <- replace String foo(String s, int i);\n" + 
				"\n" + 
				"		@SuppressWarnings({ \"inferredcallout\", \"basecall\" })\n" + 
				"		callin String foo(String s, int i) {\n" + 
				"			return foo(s+\"(\"+i+\")\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new Team3117ic10().activate();\n" + 
				"		System.out.println(new T3117ic10().foo(\"OK\", 23));\n" + 
				"	}\n" + 
				"}\n",
    	"T3117ic10.java",
    			"public class T3117ic10 {\n" + 
    			"	String foo(String s, int i) {\n" + 
    			"		return s+\":\"+i;\n" + 
    			"	}\n" + 
    			"	String foo(String s) {\n" + 
    			"		return s;\n" + 
    			"	}\n" + 
    			"}\n",
    		}, "OK(23)",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // Bug 355315 - callout inferred to implement protected method causes IllegalAccessError
    public void test3117_inferredCallout11() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3117ic11.java",
			    "\n" +
			    "public team class Team3117ic11 {\n" +
			    "    @SuppressWarnings(\"inferredcallout\")\n" +
			    "    protected class R1 extends R0 playedBy T3117ic11 {\n" +
			    "    }\n" +
			    "    protected abstract class R0 {\n" +
			    "        abstract protected void test();\n" +
			    "    }\n" +
			    "    Team3117ic11() {\n" +
			    "        new R1(new T3117ic11()).test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3117ic11();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3117ic11.java",
			    "\n" +
			    "public class T3117ic11 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
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

    // Bug 355313 - inferred callout not working for implicitly inherited method
    public void test3117_inferredCallout12() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"Team3117ic12_2.java",
			    "\n" +
			    "public team class Team3117ic12_2 extends Team3117ic12_1 {\n" +
			    "    @Override\n" +
			    "    @SuppressWarnings(\"inferredcallout\")\n" +
			    "    protected class R playedBy T3117ic12 {\n" +
			    "    }\n" +
			    "    Team3117ic12_2() {\n" +
			    "        new R(new T3117ic12()).test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team3117ic12_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team3117ic12_1.java",
			    "\n" +
			    "public abstract team class Team3117ic12_1 {\n" +
			    "    protected abstract class R {\n" +
			    "        abstract protected void test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3117ic12.java",
			    "\n" +
			    "public class T3117ic12 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
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

    // Bug 416776 - [compiler] callout inference from interface fails during incremental build
    public void test3117_inferredCallout13() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"b/IBase.java",
			    "\n" +
			    "package b;\n" + 
			    "\n" + 
			    "public interface IBase {\n" + 
			    "\n" + 
			    "	void setS(String s);\n" + 
			    "\n" +
			    "	void setL(java.util.List<String> l);\n" + 
			    "}\n",
		"b/BaseClass.java",
			    "package b;\n" + 
			    "\n" + 
			    "public class BaseClass implements IBase {\n" + 
			    "\n" + 
			    "	String s;\n" + 
			    "	java.util.List<String> l;\n" + 
			    "\n" + 
			    "	public void setS(String s) {\n" + 
			    "		this.s = s;\n" + 
			    "	}\n" + 
			    "\n" + 
			    "	public void setL(java.util.List<String> l) {\n" + 
			    "		this.l = l;\n" + 
			    "	}\n" + 
			    "}\n" + 
			    "\n"
            },
            "",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
       runConformTest(
               new String[] {
		"t/Team3117ic13.java",
			    "package t;\n" + 
			    "\n" + 
			    "import b.IBase;\n" + 
			    "\n" + 
			    "import base b.BaseClass;\n" + 
			    "\n" + 
			    "/**\n" + 
			    " * @author stephan\n" + 
			    " *\n" + 
			    " */\n" + 
			    "public team class Team3117ic13 {\n" + 
			    "\n" + 
			    "	@SuppressWarnings(\"inferredcallout\")\n" + 
			    "	protected class Role1 implements IBase playedBy BaseClass {\n" + 
			    "		String extra;\n" + 
			    "	}\n" + 
			    "}\n"
            },
            "",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // Bug 416938 - [compiler] inferred callouts are not consistently available in a tsub role
    // ensure subteam doesn't have to repeat callout inference (expect no warning!)
    public void test3117_inferredCallout14() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"b/IBase.java",
			    "\n" +
			    "package b;\n" + 
			    "\n" + 
			    "public interface IBase {\n" + 
			    "\n" + 
			    "	void setS(String s);\n" + 
			    "\n" +
			    "	void setL(java.util.List<String> l);\n" + 
			    "}\n",
		"b/BaseClass.java",
			    "package b;\n" + 
			    "\n" + 
			    "public class BaseClass implements IBase {\n" + 
			    "\n" + 
			    "	String s;\n" + 
			    "	java.util.List<String> l;\n" + 
			    "\n" + 
			    "	public void setS(String s) {\n" + 
			    "		this.s = s;\n" + 
			    "	}\n" + 
			    "\n" + 
			    "	public void setL(java.util.List<String> l) {\n" + 
			    "		this.l = l;\n" + 
			    "	}\n" + 
			    "}\n" + 
			    "\n"
            },
            "",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
       runNegativeTest(
               new String[] {
		"t/Team3117ic14_2.java",
			    "package t;\n" + 
			    "\n" + 
			    "import b.IBase;\n" + 
			    "\n" + 
			    "public team class Team3117ic14_2 extends Team3117ic14_1 {\n" + 
			    "   @Override\n" + 
			    "	protected class Role1 {\n" +
			    "       @Override public String toString() { return \"R\"; }\n" + 
			    "		Zork extra2;\n" + 
			    "	}\n" + 
			    "}\n",
		"t/Team3117ic14_1.java",
			    "package t;\n" + 
			    "\n" + 
			    "import b.IBase;\n" + 
			    "\n" + 
			    "import base b.BaseClass;\n" + 
			    "\n" + 
			    "public team class Team3117ic14_1 {\n" + 
			    "\n" + 
			    "	@SuppressWarnings(\"inferredcallout\")\n" + 
			    "	protected class Role1 implements IBase playedBy BaseClass {\n" +
			    "		String extra;\n" + 
			    "	}\n" + 
			    "}\n",
            },
            (this.weavingScheme == WeavingScheme.OTDRE || this.complianceLevel < ClassFileConstants.JDK1_8
            ?
            "----------\n" + 
            "1. ERROR in t\\Team3117ic14_2.java (at line 9)\n" + 
            "	Zork extra2;\n" + 
            "	^^^^\n" + 
            "Zork cannot be resolved to a type\n" + 
            "----------\n"
            :
        	"----------\n" + 
        	"1. WARNING in t\\Team3117ic14_1.java (at line 10)\n" + 
        	"	protected class Role1 implements IBase playedBy BaseClass {\n" + 
        	"	                                                ^^^^^^^^^\n" + 
        	"Base class b.BaseClass has class file version 52 which cannot be handled by the traditional OTRE based on BCEL. Please consider using the ASM based OTDRE instead.\n" + 
        	"----------\n" +
    		"----------\n" + 
    		"1. WARNING in t\\Team3117ic14_2.java (at line 7)\n" + 
    		"	protected class Role1 {\n" + 
    		"	                ^^^^^\n" + 
    		"Base class b.BaseClass has class file version 52 which cannot be handled by the traditional OTRE based on BCEL. Please consider using the ASM based OTDRE instead.\n" + 
    		"----------\n" + 
    		"2. ERROR in t\\Team3117ic14_2.java (at line 9)\n" + 
    		"	Zork extra2;\n" + 
    		"	^^^^\n" + 
    		"Zork cannot be resolved to a type\n" + 
    		"----------\n"
    		),
            null,//libs
            false,//flush
            customOptions);
    }

    // Bug 416938 - [compiler] inferred callouts are not consistently available in a tsub role
    // ensure subteam doesn't have to repeat callout inference (expect no warning!)
    // positive variant with execution
    public void test3117_inferredCallout14b() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportInferredCallout, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"b/IBase.java",
			    "\n" +
			    "package b;\n" + 
			    "\n" + 
			    "public interface IBase {\n" + 
			    "\n" + 
			    "	void setS(String s);\n" + 
			    "\n" +
			    "	void setL(java.util.List<String> l);\n" + 
			    "}\n",
		"b/BaseClass.java",
			    "package b;\n" + 
			    "\n" + 
			    "public class BaseClass implements IBase {\n" + 
			    "\n" + 
			    "	String s = \"OK\";\n" + 
			    "	java.util.List<String> l;\n" + 
			    "\n" + 
			    "	public void setS(String s) {\n" + 
			    "		this.s = s;\n" + 
			    "	}\n" + 
			    "\n" + 
			    "	public void setL(java.util.List<String> l) {\n" + 
			    "		this.l = l;\n" + 
			    "	}\n" +
			    "	public void print() {\n" +
			    "		System.out.print(s);\n" +
			    "		System.out.print(l.size());\n" +
			    "	}\n" + 
			    "}\n" + 
			    "\n"
            },
            "",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
       runConformTest(
               new String[] {
        "T3117ic14bMain.java",
        		"public class T3117ic14bMain{\n" +
        		"	public static void main(String[] args) {\n" +
        		"		b.BaseClass b = new b.BaseClass();\n" +
        		"		new t.Team3117ic14_2().test(b);\n" +
        		"		b.print();\n" +
        		"	}\n" +
        		"}\n",
		"t/Team3117ic14_2.java",
			    "package t;\n" + 
			    "\n" + 
			    "import base b.BaseClass;\n" + 
			    "\n" + 
			    "public team class Team3117ic14_2 extends Team3117ic14_1 {\n" + 
			    "   @Override\n" + 
			    "	protected class Role1 {\n" +
			    "       @Override public String toString() { return \"R\"; }\n" + 
			    "	}\n" +
			    "	public void test(BaseClass as Role1 r) {\n" +
			    "		r.setS(\"NOK\");\n" +
			    "		r.setL(new java.util.ArrayList<String>());\n" +
			    "	}\n" + 
			    "}\n",
		"t/Team3117ic14_1.java",
			    "package t;\n" + 
			    "\n" + 
			    "import b.IBase;\n" + 
			    "\n" + 
			    "import base b.BaseClass;\n" + 
			    "\n" + 
			    "public team class Team3117ic14_1 {\n" + 
			    "\n" + 
			    "	@SuppressWarnings(\"inferredcallout\")\n" + 
			    "	protected class Role1 implements IBase playedBy BaseClass {\n" +
			    "		public void setS(String s){}\n" + 
			    "		String extra;\n" + 
			    "	}\n" + 
			    "}\n",
            },
            "OK0",
            null/*classLibraries*/,
            false/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a short callout binding lacks a rhs
    // 3.1.18-otjld-incomplete-callout-binding-1
    public void test3118_incompleteCalloutBinding1() {
        runNegativeTest(
            new String[] {
		"Team3118icb1.java",
			    "\n" +
			    "public team class Team3118icb1 {\n" +
			    "    protected class R playedBy T3118icb1 {\n" +
			    "        abstract void test();\n" +
			    "        test -> ;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3118icb1.java",
			    "\n" +
			    "public class T3118icb1 {}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team3118icb1.java (at line 5)\n" + 
    		"	test -> ;\n" + 
    		"	     ^^\n" + 
    		"Syntax error on token \"->\", MethodSpecShort expected after this token\n" + 
    		"----------\n");
    }

    // a long callout binding lacks a return type
    // 3.1.18-otjld-incomplete-callout-binding-2
    public void test3118_incompleteCalloutBinding2() {
        runNegativeTest(
            new String[] {
		"Team3118icb2.java",
			    "\n" +
			    "public team class Team3118icb2 {\n" +
			    "    protected class R playedBy T3118icb2 {\n" +
			    "        void test() -> test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3118icb2.java",
			    "\n" +
			    "public class T3118icb2 {}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team3118icb2.java (at line 4)\n" + 
    		"	void test() -> test();\n" + 
    		"	               ^^^^^^\n" + 
    		"Syntax error: missing return type for method designator (OTJLD A.3.2). \n" + 
    		"----------\n" + 
    		"2. ERROR in Team3118icb2.java (at line 4)\n" + 
    		"	void test() -> test();\n" + 
    		"	               ^^^^^^\n" + 
    		"No method test() found in type T3118icb2 to resolve method designator (OTJLD 3.1(c)).\n" + 
    		"----------\n");
    }

    // a long callout binding is unfinished
    // 3.1.18-otjld-incomplete-callout-binding-3
    public void test3118_incompleteCalloutBinding3() {
        runNegativeTest(
            new String[] {
		"Team3118icb3.java",
			    "\n" +
			    "public team class Team3118icb3 {\n" +
			    "    protected class R playedBy T3118icb3 {\n" +
			    "        void test() -> void t\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3118icb3.java",
			    "\n" +
			    "public class T3118icb3 {}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team3118icb3.java (at line 4)\n" + 
    		"	void test() -> void t\n" + 
    		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
    		"Syntax error on token(s), misplaced construct(s)\n" + 
    		"----------\n");
    }

    // a long callout binding is unfinished
    // 3.1.18-otjld-incomplete-callout-binding-4
    public void test3118_incompleteCalloutBinding4() {
        runNegativeTestMatching(
            new String[] {
		"Team3118icb4.java",
			    "\n" +
			    "public team class Team3118icb4 {\n" +
			    "    protected class R playedBy T3118icb4 {\n" +
			    "        void test() -> \n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3118icb4.java",
			    "\n" +
			    "public class T3118icb4 {}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team3118icb4.java (at line 4)\n" + 
    		"	void test() -> \n" + 
    		"	            ^^\n" + 
    		"Syntax error, insert \"CallloutFieldSpec\" to complete CalloutHeader\n" + 
    		"----------\n" + 
    		"2. ERROR in Team3118icb4.java (at line 4)\n" + 
    		"	void test() -> \n" + 
    		"	            ^^\n" + 
    		"Syntax error, insert \"EmptyParameterMappings\" to complete ClassBodyDeclarations\n" + 
    		"----------\n");
    }

    // a callout binding has to select between overloaded base methods
    // 3.1.19-otjld-callout-with-overloading-1
    public void test3119_calloutWithOverloading1() {
       
       runConformTest(
            new String[] {
		"Team3119cwo1.java",
			    "\n" +
			    "public team class Team3119cwo1 {\n" +
			    "    protected class R playedBy T3119cwo1_3 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        protected int foo(T3119cwo1_2 b) -> int foo(T3119cwo1_2 b);\n" +
			    "    }\n" +
			    "    void test(T3119cwo1_3 as R o) {\n" +
			    "        System.out.print(o.foo(new T3119cwo1_2()));\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team3119cwo1 t = new Team3119cwo1();\n" +
			    "        t.test(new T3119cwo1_3());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T3119cwo1_1.java",
			    "\n" +
			    "public class T3119cwo1_1 { }\n" +
			    "    \n",
		"T3119cwo1_2.java",
			    "\n" +
			    "public class T3119cwo1_2 extends T3119cwo1_1 { }\n" +
			    "    \n",
		"T3119cwo1_3.java",
			    "\n" +
			    "public class T3119cwo1_3 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private int foo(T3119cwo1_2 b) { return 42; }\n" +
			    "    protected int foo(T3119cwo1_1 b) { return 23; }\n" +
			    "}\n" +
			    "    \n"
            },
            "42");
    }
    
    // ==== from binding-of-abstract-and-concrete-methods: ====
    
    // a callout binding to an abstract role method is invoked
    // 7.2.1-otjld-callout-bound-abstract-method-invocation-1
    public void test721_calloutBoundAbstractMethodInvocation1() {
       
       runConformTest(
            new String[] {
		"T721cbami1Main.java",
			    "\n" +
			    "public class T721cbami1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team721cbami1 t = new Team721cbami1();\n" +
			    "        T721cbami1_2  o = new T721cbami1_2();\n" +
			    "\n" +
			    "        System.out.print(t.toObject(o).test());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T721cbami1_1.java",
			    "\n" +
			    "public interface T721cbami1_1 {\n" +
			    "    String test();\n" +
			    "}\n" +
			    "    \n",
		"T721cbami1_2.java",
			    "\n" +
			    "public class T721cbami1_2 {\n" +
			    "    public String toString() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team721cbami1.java",
			    "\n" +
			    "public team class Team721cbami1 {\n" +
			    "    public class Role721cbami1 implements T721cbami1_1 playedBy T721cbami1_2 {\n" +
			    "        test -> toString;\n" +
			    "    }\n" +
			    "\n" +
			    "    public T721cbami1_1 toObject(T721cbami1_2 as Role721cbami1 obj) {\n" +
			    "        return obj;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout binding to an abstract role method is invoked
    // 7.2.1-otjld-callout-bound-abstract-method-invocation-2
    public void test721_calloutBoundAbstractMethodInvocation2() {
       
       runConformTest(
            new String[] {
		"T721cbami2Main.java",
			    "\n" +
			    "public class T721cbami2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team721cbami2_2 t = new Team721cbami2_2();\n" +
			    "        T721cbami2_2    o = new T721cbami2_2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T721cbami2_1.java",
			    "\n" +
			    "public abstract class T721cbami2_1 {\n" +
			    "    protected abstract String test();\n" +
			    "}\n" +
			    "    \n",
		"T721cbami2_2.java",
			    "\n" +
			    "public class T721cbami2_2 {\n" +
			    "    @Override\n" +
			    "    public String toString() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team721cbami2_1.java",
			    "\n" +
			    "public abstract team class Team721cbami2_1 {\n" +
			    "    public abstract class Role721cbami2_1 extends T721cbami2_1 playedBy T721cbami2_2 {}\n" +
			    "}\n" +
			    "    \n",
		"Team721cbami2_2.java",
			    "\n" +
			    "public team class Team721cbami2_2 extends Team721cbami2_1 {\n" +
			    "    public class Role721cbami2_2 extends Role721cbami2_1 {\n" +
			    "        test -> toString;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T721cbami2_2 as Role721cbami2_2 obj) {\n" +
			    "        return obj.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    public void testCalloutVisibility() {
    	runNegativeTest(
    		new String[] {
    			"b/B.java",
    			"package b;\n" +
    			"@SuppressWarnings(\"unused\")\n" +
    			"public class B {\n" +
    			"	int f0;\n" +
    			"	private int f1;\n" +
    			"	private int f2;\n" +
    			"	private int f3;\n" +
    			"}\n",
    			"p1/Team1.java",
    			"package p1;\n" +
    			"import base b.B;\n" +
    			"@SuppressWarnings(\"decapsulation\")\n" +
    			"public team class Team1 {\n" +
    			"	protected class R playedBy B {\n" +
    			"		int getF0() -> get int f0;\n" + // package vis
    			"		int getF1() -> get int f1;\n" + // ERR: private from private base field
    			"		private int getF2() -> get int f2;\n" + // ERR: private
    			"		protected int getF3() -> get int f3;\n" + // protected
    			"\n" +
    			"		int m1() { return 1; }\n" +
    			"		private int m2() { return 2; }\n" +
    			"		protected int m3() { return 3; }\n" +
    			"	}\n" +
    			"}\n",
    			"p2/Team2.java",
    			"package p2;\n" +
    			"public team class Team2 extends p1.Team1 {\n" +
    			"	@Override\n" +
    			"	protected class R {\n" + // no probs in implicit inheritance
    			"		int test1() {\n" +
    			"			return getF0() +\n" +
    			"					getF1() +\n" +
    			"					getF2() +\n" +
    			"					getF3();\n" +
    			"		}\n" +
    			"		int test2() {\n" +
    			"			return m1() +\n" +
    			"					m2() +\n" +
    			"					m3();\n" +
    			"		}\n" +
    			"	}\n" +
    			"	protected class R2 extends R {\n" +
    			"		int test3() {\n" +
    			"			return getF1() +\n" +
    			"					getF2() +\n" +
    			"					getF3();\n" +
    			"		}\n" +
    			"		int test4() {\n" +
    			"			return m1() +\n" +
    			"					m2() +\n" +
    			"					m3();\n" +
    			"		}\n" +
    			"	}\n" +
    			"}\n"
    		},
    		"----------\n" + 
			"1. ERROR in p2\\Team2.java (at line 19)\n" + 
			"	return getF1() +\n" + 
			"	       ^^^^^\n" + 
			"The method getF1() from the role type Team2.R is not visible (OTJLD 1.2.1(e)).\n" + 
			"----------\n" + 
			"2. ERROR in p2\\Team2.java (at line 20)\n" + 
			"	getF2() +\n" + 
			"	^^^^^\n" + 
			"The method getF2() from the role type Team2.R is not visible (OTJLD 1.2.1(e)).\n" + 
			"----------\n" + 
			"3. ERROR in p2\\Team2.java (at line 25)\n" + 
			"	m2() +\n" + 
			"	^^\n" + 
			"The method m2() from the role type Team2.R is not visible (OTJLD 1.2.1(e)).\n" + 
			"----------\n");
    }
}
