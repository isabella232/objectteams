/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010, 2014 Stephan Herrmann
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
package org.eclipse.objectteams.otdt.tests.otjld.other;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.core.ext.WeavingScheme;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

import junit.framework.Test;

@SuppressWarnings("unchecked")
public class Java5 extends AbstractOTJLDTest {
	
	public Java5(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "testA122_genericsRegression1"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return Java5.class;
	}
    // a base field has a generic type and is bound via callout
    // A.1.1-otjld-generic-feature-in-base-1
    public void testA11_genericFeatureInBase1() {
       
       runConformTest(
            new String[] {
		"TeamA11gfib1.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA11gfib1 {\n" +
			    "    protected class R playedBy TA11gfib1 {\n" +
			    "        Integer getInteger(int pos) -> get LinkedList<Integer> numbers\n" +
			    "            with {           result <- result.get(pos) }\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(getInteger(0));\n" +
			    "        }\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"roleinstantiation\")\n" +
			    "    TeamA11gfib1() {\n" +
			    "        TA11gfib1 b = new TA11gfib1();\n" +
			    "        b.numbers.add(new Integer(3));\n" +
			    "        R r = new R(b);\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA11gfib1();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA11gfib1.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public class TA11gfib1 {\n" +
			    "    public LinkedList<Integer> numbers = new LinkedList<Integer>();\n" +
			    "}\n" +
			    "    \n"
            },
            "3");
    }

    // a base field has a generic type and is bound via callout
    // A.1.1-otjld-generic-feature-in-base-2
    public void testA11_genericFeatureInBase2() {
       
       runConformTest(
            new String[] {
		"TeamA11gfib2.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA11gfib2 {\n" +
			    "    protected class R playedBy TA11gfib2 {\n" +
			    "        protected R getSibling(int pos) -> get LinkedList<TA11gfib2> siblings\n" +
			    "            with {     result <- result.get(pos) }\n" +
			    "        String getVal() -> get String val;\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(getSibling(0).getVal());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"roleinstantiation\")\n" +
			    "    TeamA11gfib2() {\n" +
			    "        TA11gfib2 b1 = new TA11gfib2();\n" +
			    "        b1.val = \"OK\";\n" +
			    "        TA11gfib2 b2 = new TA11gfib2();\n" +
			    "        b2.siblings.add(b1);\n" +
			    "        R r = new R(b2);\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA11gfib2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA11gfib2.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public class TA11gfib2 {\n" +
			    "    public String val;\n" +
			    "    public LinkedList<TA11gfib2> siblings = new LinkedList<TA11gfib2>();\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a base has a parameterized method  which is bound via callout
    // A.1.1-otjld-generic-feature-in-base-3
    public void testA11_genericFeatureInBase3() {
       
       runConformTest(
            new String[] {
		"TeamA11gfib3.java",
			    "\n" +
			    "public team class TeamA11gfib3 {\n" +
			    "  protected class R playedBy TA11gfib3 {\n" +
			    "    <T> T getIt(T it) -> T getIt(T it);\n" +
			    "    protected void test() {\n" +
			    "      String ok= getIt(\"OK\");\n" +
			    "      System.out.print(ok);\n" +
			    "    }\n" +
			    "  }\n" +
			    "  TeamA11gfib3() {\n" +
			    "    new R(new TA11gfib3()).test();\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA11gfib3();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib3.java",
			    "\n" +
			    "public class TA11gfib3 {\n" +
			    "  <T> T getIt(T it) { return it; }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // a base has a parameterized method  which is bound via callout with substitution
    // A.1.1-otjld-generic-feature-in-base-3s
    public void testA11_genericFeatureInBase3s() {
       
       runConformTest(
            new String[] {
		"TeamA11gfib3s.java",
			    "\n" +
			    "public team class TeamA11gfib3s {\n" +
			    "  protected class R playedBy TA11gfib3s {\n" +
			    "    String getIt(String it) -> String getIt(String it);\n" +
			    "    protected void test() {\n" +
			    "      String ok= getIt(\"OK\");\n" +
			    "      System.out.print(ok);\n" +
			    "    }\n" +
			    "  }\n" +
			    "  TeamA11gfib3s() {\n" +
			    "    new R(new TA11gfib3s()).test();\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA11gfib3s();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib3s.java",
			    "\n" +
			    "public class TA11gfib3s {\n" +
			    "  <T> T getIt(T it) { return it; }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // a base has a parameterized method  which is bound via callin
    // A.1.1-otjld-generic-feature-in-base-3ci
    public void testA11_genericFeatureInBase3ci() {
       
       runConformTest(
            new String[] {
		"TeamA11gfib3ci.java",
			    "\n" +
			    "public team class TeamA11gfib3ci {\n" +
			    "  protected class R playedBy TA11gfib3ci {\n" +
			    "    <T> T getIt(T it) <- replace T getIt(T it);\n" +
			    "    callin <T> T getIt(T it) {\n" +
			    "      System.out.print(\">>\");\n" +
			    "      return base.getIt(it);\n" +
			    "    }\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA11gfib3ci().activate();\n" +
			    "    System.out.print(new TA11gfib3ci().getIt(\"OK\"));\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib3ci.java",
			    "\n" +
			    "public class TA11gfib3ci {\n" +
			    "  <T> T getIt(T it) { return it; }\n" +
			    "}\n" +
			    "  \n"
            },
            ">>OK");
    }

    // a base has a parameterized method with type bound which is bound via callin
    // A.1.1-otjld-generic-feature-in-base-3cib
    public void testA11_genericFeatureInBase3cib() {
       
       runConformTest(
            new String[] {
		"TeamA11gfib3cib.java",
			    "\n" +
			    "public team class TeamA11gfib3cib {\n" +
			    "  protected class R playedBy TA11gfib3cib {\n" +
			    "    <T extends Number> T getIt(T it) <- replace T getIt(T it);\n" +
			    "    callin <T extends Number> T getIt(T it) {\n" +
			    "      System.out.print(\">>\"+(it.intValue()+1));\n" +
			    "      return base.getIt(it);\n" +
			    "    }\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA11gfib3cib().activate();\n" +
			    "    System.out.print(new TA11gfib3cib().getIt(new Integer(3)));\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib3cib.java",
			    "\n" +
			    "public class TA11gfib3cib {\n" +
			    "  <T extends Number> T getIt(T it) { return it; }\n" +
			    "}\n" +
			    "  \n"
            },
            ">>43");
    }

    // a base has a parameterized method with type bound which is bound via callin - compatible type variable declaration
    // A.1.1-otjld-generic-feature-in-base-3cib2
    public void testA11_genericFeatureInBase3cib2() {
       
       runConformTest(
            new String[] {
		"TeamA11gfib3cib2.java",
			    "\n" +
			    "public team class TeamA11gfib3cib2 {\n" +
			    "  protected class R playedBy TA11gfib3cib2 {\n" +
			    "    <T extends Number> T getIt(T it) <- replace T getIt(T it);\n" +
			    "    callin <T> T getIt(T it) {\n" +
			    "      System.out.print(\">>\");\n" +
			    "      return base.getIt(it);\n" +
			    "    }\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA11gfib3cib2().activate();\n" +
			    "    System.out.print(new TA11gfib3cib2().getIt(new Integer(3)));\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib3cib2.java",
			    "\n" +
			    "public class TA11gfib3cib2 {\n" +
			    "  <T extends Number> T getIt(T it) { return it; }\n" +
			    "}\n" +
			    "  \n"
            },
            ">>3");
    }

    // a base has a parameterized method with type bound which is bound via callin - incompatible type variable declaration
    // A.1.1-otjld-generic-feature-in-base-3cib3
    public void testA11_genericFeatureInBase3cib3() {
        runNegativeTestMatching(
            new String[] {
		"TeamA11gfib3cib3.java",
			    "\n" +
			    "public team class TeamA11gfib3cib3 {\n" +
			    "  protected class R playedBy TA11gfib3cib3 {\n" +
			    "    <T> T getIt(T it) <- replace T getIt(T it);\n" +
			    "    callin <T extends Number> T getIt(T it) {\n" +
			    "      System.out.print(\">>\"+(it.intValue()+1));\n" +
			    "      return base.getIt(it);\n" +
			    "    }\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA11gfib3cib3().activate();\n" +
			    "    System.out.print(new TA11gfib3cib3().getIt(new Integer(3)));\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib3cib3.java",
			    "\n" +
			    "public class TA11gfib3cib3 {\n" +
			    "  <T> T getIt(T it) { return it; }\n" +
			    "}\n" +
			    "  \n"
            },
            (this.complianceLevel < ClassFileConstants.JDK1_8 ?
	    		"----------\n" + 
	    		"1. ERROR in TeamA11gfib3cib3.java (at line 4)\n" + 
	    		"	<T> T getIt(T it) <- replace T getIt(T it);\n" + 
	    		"	      ^^^^^\n" + 
	    		"Bound mismatch: The generic method getIt(T) of type TeamA11gfib3cib3.R is not applicable for the arguments (T). The inferred type T is not a valid substitute for the bounded parameter <T extends Number>\n" + 
	    		"----------\n"
	    	:
	    		"----------\n" + 
	    		"1. ERROR in TeamA11gfib3cib3.java (at line 4)\n" + 
	    		"	<T> T getIt(T it) <- replace T getIt(T it);\n" + 
	    		"	^^^^^^^^^^^^^^^^^\n" + 
	    		"No method getIt(T) found in type TeamA11gfib3cib3.R to resolve method designator (OTJLD 4.1(c)).\n" + 
	    		"----------\n"));
    }

    // a base has a parameterized method  which is bound via callin - illegally declared type parameter
    // A.1.1-otjld-generic-feature-in-base-3cif
    public void testA11_genericFeatureInBase3cif() {
        runNegativeTestMatching(
            new String[] {
		"TeamA11gfib3cif.java",
			    "\n" +
			    "public team class TeamA11gfib3cif {\n" +
			    "  protected class R playedBy TA11gfib3cif {\n" +
			    "    <X> X getIt(X it) <- replace <T> T getIt(T it);\n" +
			    "    callin <X> X getIt(X it) {\n" +
			    "      System.out.print(\">>\");\n" +
			    "      return base.getIt(it);\n" +
			    "    }\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib3cif.java",
			    "\n" +
			    "public class TA11gfib3cif {\n" +
			    "  <T> T getIt(T it) { return it; }\n" +
			    "}\n" +
			    "  \n"
            },
            "A.3.3");
    }

    // a base has a parameterized method  which is bound via callin - unresolved type parameter
    // A.1.1-otjld-generic-feature-in-base-3cig
    public void testA11_genericFeatureInBase3cig() {
        runNegativeTestMatching(
            new String[] {
		"TeamA11gfib3cig.java",
			    "\n" +
			    "public team class TeamA11gfib3cig {\n" +
			    "  protected class R playedBy TA11gfib3cig {\n" +
			    "    <X> X getIt(X it) <- replace T getIt(T it);\n" +
			    "    callin <X> X getIt(X it) {\n" +
			    "      System.out.print(\">>\");\n" +
			    "      return base.getIt(it);\n" +
			    "    }\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib3cig.java",
			    "\n" +
			    "public class TA11gfib3cig {\n" +
			    "  <T> T getIt(T it) { return it; }\n" +
			    "}\n" +
			    "  \n"
            },
            "cannot be resolved");
    }

    // a base has a parameterized method, type variable used as type argument -  which is bound via callin
    public void testA11_genericFeatureInBase3cih() {
       
       runConformTest(
            new String[] {
		"TeamA11gfib3ci.java",
			    "\n" +
			    "public team class TeamA11gfib3ci {\n" +
			    "  protected class R playedBy TA11gfib3ci {\n" +
			    "    <T> T getIt(Object it, Class<T> c) <- replace T getIt(Object it, Class<T> c)\n" +
			    "			base when (c.isInstance(it));" +
			    "    callin <T> T getIt(Object it, Class<T> c) {\n" +
			    "      System.out.print(\">>\");\n" +
			    "      return base.getIt(it, c);\n" +
			    "    }\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA11gfib3ci().activate();\n" +
			    "    System.out.print(new TA11gfib3ci().getIt(\"OK\", String.class));\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib3ci.java",
			    "\n" +
			    "public class TA11gfib3ci {\n" +
			    "  <T> T getIt(Object it, Class<T> c) { return c.cast(it); }\n" +
			    "}\n" +
			    "  \n"
            },
            ">>OK");
    }

    // a base has a parameterized method, base's type parameter omitted -  which is bound via callin
    public void testA11_genericFeatureInBase3cii() {
       
       runNegativeTest(
            new String[] {
		"TeamA11gfib3ci.java",
			    "\n" +
			    "public team class TeamA11gfib3ci {\n" +
			    "  protected class R playedBy TA11gfib3ci {\n" +
			    "    <T> T getIt(Object it, Class<T> c) <- replace T getIt(Object it, Class c)\n" +
			    "			base when (c.isInstance(it));" +
			    "    callin <T> T getIt(Object it, Class<T> c) {\n" +
			    "      System.out.print(\">>\");\n" +
			    "      return base.getIt(it, c);\n" +
			    "    }\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA11gfib3ci().activate();\n" +
			    "    System.out.print(new TA11gfib3ci().getIt(\"OK\", String.class));\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib3ci.java",
			    "\n" +
			    "public class TA11gfib3ci {\n" +
			    "  <T> T getIt(Object it, Class<T> c) { return c.cast(it); }\n" +
			    "}\n" +
			    "  \n"
            },
            "----------\n" + 
    		"1. WARNING in TeamA11gfib3ci.java (at line 4)\n" + 
    		"	<T> T getIt(Object it, Class<T> c) <- replace T getIt(Object it, Class c)\n" + 
    		"	                                                                 ^^^^^\n" + 
    		"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
    		"----------\n" + 
    		"2. ERROR in TeamA11gfib3ci.java (at line 4)\n" + 
    		"	<T> T getIt(Object it, Class<T> c) <- replace T getIt(Object it, Class c)\n" + 
    		"	                                                                 ^^^^^\n" + 
    		"Method specifier finds no direct match, argument of closest match is Class<Object> instead of Class (OTJLD 4.1(c)).\n" + 
    		"----------\n");
    }

    // a base has a parameterized method, base's type parameter erased to Object -  which is bound via callin
    public void testA11_genericFeatureInBase3cij() {
       
       runNegativeTest(
            new String[] {
		"TeamA11gfib3ci.java",
			    "\n" +
			    "public team class TeamA11gfib3ci {\n" +
			    "  protected class R playedBy TA11gfib3ci {\n" +
			    "    <T> T getIt(Object it, Class<T> c) <- replace T getIt(Object it, Class<Object> c)\n" +
			    "			base when (c.isInstance(it));" +
			    "    callin <T> T getIt(Object it, Class<T> c) {\n" +
			    "      System.out.print(\">>\");\n" +
			    "      return base.getIt(it, c);\n" +
			    "    }\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA11gfib3ci().activate();\n" +
			    "    System.out.print(new TA11gfib3ci().getIt(\"OK\", String.class));\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib3ci.java",
			    "\n" +
			    "public class TA11gfib3ci {\n" +
			    "  <T> T getIt(Object it, Class<T> c) { return c.cast(it); }\n" +
			    "}\n" +
			    "  \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamA11gfib3ci.java (at line 4)\n" + 
    		"	<T> T getIt(Object it, Class<T> c) <- replace T getIt(Object it, Class<Object> c)\n" + 
    		"	                                ^\n" + 
    		"When mapping the 2. argument of getIt(Object, Class): cannot convert from java.lang.Class<java.lang.Object> to java.lang.Class<T> (OTJLD 4.5(d)).\n" + 
    		"----------\n");
    }

    // a base has a parameterized method  which is bound via callout
    // A.1.1-otjld-generic-feature-in-base-4
    public void testA11_genericFeatureInBase4() {
       
       runConformTest(
            new String[] {
		"TeamA11gfib4.java",
			    "\n" +
			    "import java.util.List;\n" +
			    "import java.util.ArrayList;\n" +
			    "public team class TeamA11gfib4 {\n" +
			    "  protected class R playedBy TA11gfib4 {\n" +
			    "    <X, T extends List<X>> T create(Class<T> c, X e) -> T create(Class<T> c, X e);\n" +
			    "    @SuppressWarnings({\"rawtypes\",\"unchecked\"})\n" +
			    "    Class<ArrayList<String>> check(Class<? extends ArrayList> c) {\n" +
			    "      return (Class<ArrayList<String>>)c; // ugly hack to achieve nested parameterization\n" +
			    "    }\n" +
			    "    protected void test() {\n" +
			    "      List<String> l= create(check(ArrayList.class), \"OK\");\n" +
			    "      System.out.print(l.get(0));\n" +
			    "    }\n" +
			    "  }\n" +
			    "  TeamA11gfib4() {\n" +
			    "    new R(new TA11gfib4()).test();\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA11gfib4();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib4.java",
			    "\n" +
			    "public class TA11gfib4 {\n" +
			    "  <X, T extends java.util.List<X>> T create(Class<T> c, X e) {\n" +
			    "    try {\n" +
			    "      T result= c.newInstance();\n" +
			    "      result.add(e);\n" +
			    "      System.out.print(result.size());\n" +
			    "      return result;\n" +
			    "    } catch (Exception ex) {\n" +
			    "      return null;\n" +
			    "    }\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n"
            },
            "1OK");
    }

    // mimiced after a variation of the DeclarationImplAdaptor suggested to svenk
    // A.1.1-otjld-generic-feature-in-base-5
    public void testA11_genericFeatureInBase5() {
       
       runConformTest(
            new String[] {
		"TeamA11gfib5.java",
			    "\n" +
			    "public team class TeamA11gfib5 {\n" +
			    "  protected class R implements IA11gfib5_2 playedBy TA11gfib5_2 {\n" +
			    "    protected void test() {\n" +
			    "      TA11gfib5_1 o= getAnnotation(TA11gfib5_1.class);\n" +
			    "      System.out.print(o.getVal());\n" +
			    "    }\n" +
			    "    <A extends TA11gfib5_1> A getAnnotation(Class<A> annotationType)\n" +
			    "      -> A getAnnotation(Class<A> annotationType);\n" +
			    "  }\n" +
			    "  TeamA11gfib5() {\n" +
			    "    R r= new R(new TA11gfib5_2());\n" +
			    "    r.test();\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA11gfib5();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib5_1.java",
			    "\n" +
			    "public interface TA11gfib5_1 {\n" +
			    "  String getVal();\n" +
			    "}\n" +
			    "  \n",
		"TA11gfib5_2.java",
			    "\n" +
			    "public class TA11gfib5_2 implements IA11gfib5_2 {\n" +
			    "  @SuppressWarnings(\"unchecked\")\n" +
			    "  public <A extends TA11gfib5_1> A getAnnotation(Class<A> annotationType) {\n" +
			    "    return (A) new TA11gfib5_1() {\n" +
			    "      public String getVal() { return \"OK\"; }\n" +
			    "    };\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"IA11gfib5_2.java",
			    "\n" +
			    "public interface IA11gfib5_2 {\n" +
			    "  public <A extends TA11gfib5_1> A getAnnotation(Class<A> annotationType);\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // a role method has a generic parameter and is bound via callout
    // A.1.2-otjld-generic-role-feature-1
    public void testA12_genericRoleFeature1() {
       
       runConformTest(
            new String[] {
		"TeamA12grf1.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA12grf1 {\n" +
			    "    protected class R playedBy TA12grf1 {\n" +
			    "        protected void testMany(LinkedList<String> l) -> void test2(String s1, String s2)\n" +
			    "            with {\n" +
			    "                l.get(0) -> s1,\n" +
			    "                l.get(1) -> s2\n" +
			    "            }\n" +
			    "    }\n" +
			    "    TeamA12grf1() {\n" +
			    "        R r = new R(new TA12grf1());\n" +
			    "        LinkedList<String> l = new LinkedList<String>();\n" +
			    "        l.add(\"O\");\n" +
			    "        l.add(\"K\");\n" +
			    "        r.testMany(l);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf1();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA12grf1.java",
			    "\n" +
			    "public class TA12grf1 {\n" +
			    "    void test2(String s1, String s2) {\n" +
			    "        System.out.print(s1+s2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role method has a generic parameter and is bound via callout - lowering required
    // A.1.2-otjld-generic-role-feature-2
    public void testA12_genericRoleFeature2() {
       
       runConformTest(
            new String[] {
		"TeamA12grf2.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA12grf2 {\n" +
			    "    protected class R playedBy TA12grf2 {\n" +
			    "        protected void testOne(LinkedList<R> l, int i) -> void test(TA12grf2 b)\n" +
			    "            with {\n" +
			    "                l.get(i) -> b\n" +
			    "            }\n" +
			    "    }\n" +
			    "    TeamA12grf2() {\n" +
			    "        LinkedList<R> l = new LinkedList<R>();\n" +
			    "        l.add(new R(new TA12grf2(\"O\")));\n" +
			    "        l.add(new R(new TA12grf2(\"K\")));\n" +
			    "        l.get(0).testOne(l, 0);\n" +
			    "        l.get(0).testOne(l, 1);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA12grf2.java",
			    "\n" +
			    "public class TA12grf2 {\n" +
			    "    String val;\n" +
			    "    TA12grf2 (String v) { this.val = v; }\n" +
			    "    void test(TA12grf2 b) {\n" +
			    "        System.out.print(b.val);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a team has a parameterized collection of bases, lowering required
    // A.1.2-otjld-generic-role-feature-2a
    public void testA12_genericRoleFeature2a() {
       
       runConformTest(
            new String[] {
		"TeamA12grf2a.java",
			    "\n" +
			    "import java.util.ArrayList;\n" +
			    "public team class TeamA12grf2a {\n" +
			    "    protected class R playedBy TA12grf2a {\n" +
			    "        protected R () { base(); }\n" +
			    "    }\n" +
			    "    ArrayList<TA12grf2a> list= new ArrayList<TA12grf2a>();\n" +
			    "    void test() {\n" +
			    "        R r= new R();\n" +
			    "        list.add(r);\n" +
			    "        System.out.print(list.size());\n" +
			    "        TA12grf2a b= r; // need to force lowering (see http://trac.objectteams.org/ot/wiki/Caveats)\n" +
			    "        list.remove(b);\n" +
			    "        System.out.print(list.size());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf2a().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA12grf2a.java",
			    "\n" +
			    "public class TA12grf2a { }\n" +
			    "    \n"
            },
            "10");
    }

    // a team has a parameterized collection of bases, lowering ambiguous
    // A.1.2-otjld-generic-role-feature-2w
    public void testA12_genericRoleFeature2w() {
       
       runConformTest(
            new String[] {
		"TeamA12grf2w.java",
			    "\n" +
			    "import java.util.ArrayList;\n" +
			    "public team class TeamA12grf2w {\n" +
			    "    protected class R playedBy TA12grf2w {\n" +
			    "        protected R () { base(); }\n" +
			    "    }\n" +
			    "    ArrayList<TA12grf2w> list= new ArrayList<TA12grf2w>();\n" +
			    "    void test() {\n" +
			    "        R r= new R();\n" +
			    "        list.add(r);\n" +
			    "        System.out.print(list.size());\n" +
			    "        list.remove(r);\n" +
			    "        System.out.print(list.size());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf2w().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA12grf2w.java",
			    "\n" +
			    "public class TA12grf2w { }\n" +
			    "    \n"
            },
            "11");
    }

    // a team has a parameterized collection of bases, lowering ambiguous
    // A.1.2-otjld-generic-role-feature-2w2
    public void testA12_genericRoleFeature2w2() {
       
       runConformTest(
            new String[] {
		"TeamA12grf2w2.java",
			    "\n" +
			    "import java.util.ArrayList;\n" +
			    "public team class TeamA12grf2w2 {\n" +
			    "    protected class R playedBy TA12grf2w2 {\n" +
			    "        protected R () { base(); }\n" +
			    "    }\n" +
			    "    ArrayList<TA12grf2w2> list= new ArrayList<TA12grf2w2>();\n" +
			    "    void test() {\n" +
			    "        R r= new R();\n" +
			    "        list.add(r);\n" +
			    "        System.out.print(list.size());\n" +
			    "        Object o = r;\n" +
			    "        list.remove(o);\n" +
			    "        System.out.print(list.size());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf2w2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA12grf2w2.java",
			    "\n" +
			    "public class TA12grf2w2 { }\n" +
			    "    \n"
            },
            "11");
    }

    // a base method has a generic parameter and is bound via callin
    // A.1.2-otjld-generic-role-feature-3
    public void testA12_genericRoleFeature3() {
       
       runConformTest(
            new String[] {
		"TeamA12grf3.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA12grf3 {\n" +
			    "    protected class R playedBy TA12grf3 {\n" +
			    "         void testMany(String s) <- after void test2(LinkedList<String> l)\n" +
			    "            with {\n" +
			    "                s <- l.get(0)\n" +
			    "            }\n" +
			    "         void testMany(String s) {\n" +
			    "            System.out.print(s);\n" +
			    "         }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf3().activate();\n" +
			    "        TA12grf3 b = new TA12grf3();\n" +
			    "        LinkedList<String> li = new LinkedList<String>();\n" +
			    "        li.add(\"K\");\n" +
			    "        li.add(\"O\");\n" +
			    "        b.test2(li);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA12grf3.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public class TA12grf3 {\n" +
			    "    void test2(LinkedList<String> l) {\n" +
			    "        System.out.print(l.get(1));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role method has a generic parameter and is bound via callin - lifting required
    // A.1.2-otjld-generic-role-feature-4
    public void testA12_genericRoleFeature4() {
       
       runConformTest(
            new String[] {
		"TeamA12grf4.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA12grf4 {\n" +
			    "    protected class R playedBy TA12grf4 {\n" +
			    "         void testMany(TA12grf4 s) <- after void test2(LinkedList<TA12grf4> l)\n" +
			    "            with {\n" +
			    "                s <- l.get(0)\n" +
			    "            }\n" +
			    "         void testMany(TA12grf4 s) {\n" +
			    "            System.out.print(s.val);\n" +
			    "         }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf4().activate();\n" +
			    "        TA12grf4 b = new TA12grf4(\"?\");\n" +
			    "        LinkedList<TA12grf4> li = new LinkedList<TA12grf4>();\n" +
			    "        li.add(new TA12grf4(\"K\"));\n" +
			    "        li.add(new TA12grf4(\"O\"));\n" +
			    "        b.test2(li);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA12grf4.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public class TA12grf4 {\n" +
			    "    String val = null;\n" +
			    "    TA12grf4(String v) { this.val = v; }\n" +
			    "    void test2(LinkedList<TA12grf4> l) {\n" +
			    "        System.out.print(l.get(1).val);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }


    // a role is used as type parameter, implicit inheritance involved
    // A.1.2-otjld-generic-role-feature-6
    public void testA12_genericRoleFeature6() {
       
       runConformTest(
            new String[] {
		"TeamA12grf6_2.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA12grf6_2 extends TeamA12grf6_1 {\n" +
			    "    protected class R {\n" +
			    "        public void print() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    TeamA12grf6_2() {\n" +
			    "        this.l= new LinkedList<R>();\n" +
			    "        this.l.add(new R());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf6_2().testl();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA12grf6_1.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA12grf6_1 {\n" +
			    "    abstract protected class R {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    LinkedList<R> l;\n" +
			    "    void testl() {\n" +
			    "        for(R r : l)\n" +
			    "            r.print();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is used as type parameter, implicit inheritance involved - class LinkedList vs. interface List
    // A.1.2-otjld-generic-role-feature-7
    public void testA12_genericRoleFeature7() {
       
       runConformTest(
            new String[] {
		"TeamA12grf7_2.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA12grf7_2 extends TeamA12grf7_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void print() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    TeamA12grf7_2() {\n" +
			    "        this.l= new LinkedList<R>();\n" +
			    "        this.l.add(new R());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf7_2().testl();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA12grf7_1.java",
			    "\n" +
			    "import java.util.List;\n" +
			    "public team class TeamA12grf7_1 {\n" +
			    "    abstract protected class R {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    List<R> l;\n" +
			    "    void testl() {\n" +
			    "        for(R r : l)\n" +
			    "            r.print();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is used as type parameter, implicit inheritance involved - used in return statement
    // A.1.2-otjld-generic-role-feature-8
    public void testA12_genericRoleFeature8() {
       
       runConformTest(
            new String[] {
		"TeamA12grf8_2.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA12grf8_2 extends TeamA12grf8_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void print() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    LinkedList<R> getL() {\n" +
			    "        return this.l;\n" +
			    "    }\n" +
			    "    TeamA12grf8_2() {\n" +
			    "        this.l= getL();\n" +
			    "        this.l.add(new R());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf8_2().testl();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA12grf8_1.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA12grf8_1 {\n" +
			    "    abstract protected class R {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    LinkedList<R> l= new LinkedList<R>();\n" +
			    "    void testl() {\n" +
			    "        for(R r : l)\n" +
			    "            r.print();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is used as type parameter, implicit inheritance involved - used within abstract role
    // A.1.2-otjld-generic-role-feature-9
    public void testA12_genericRoleFeature9() {
       
       runConformTest(
            new String[] {
		"TeamA12grf9_2.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public team class TeamA12grf9_2 extends TeamA12grf9_1 {\n" +
			    "    protected class R {\n" +
			    "        void print() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            LinkedList<R> list= getL();\n" +
			    "            for (R r : list)\n" +
			    "                r.print();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf9_2().testl();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA12grf9_1.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public abstract team class TeamA12grf9_1 {\n" +
			    "    abstract protected class R {\n" +
			    "        private LinkedList<R> l= new LinkedList<R>();\n" +
			    "        LinkedList<R> getL() {\n" +
			    "            l.add(this);\n" +
			    "            return l;\n" +
			    "        }\n" +
			    "        abstract void print();\n" +
			    "        protected abstract void test();\n" +
			    "    }\n" +
			    "    void testl() {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin method has a generic return type, unsafely overridden with a raw type (witness for an observed NPE in ProblemReporter.unsafeReturnTypeOverride)
    // A.1.2-otjld-generic-role-feature-10
    public void testA12_genericRoleFeature10() {
		runTestExpectingWarnings(
			new String[] {
				"TeamA12grf10.java",
				    "\n" +
				    "import java.util.List;\n" +
				    "public team class TeamA12grf10 {\n" +
				    "    abstract protected class R1 {\n" +
				    "        abstract callin List<String>  ci();\n" +
				    "    }\n" +
				    "    protected class R2 extends R1 {\n" +
				    "		  @Override\n" +
				    "         callin List ci() { return base.ci(); }\n" +
				    "    }\n" +
				    "}\n" +
				    "    \n"
			},
			"----------\n" + 
    		"1. WARNING in TeamA12grf10.java (at line 9)\n" + 
    		"	callin List ci() { return base.ci(); }\n" + 
    		"	       ^^^^\n" + 
    		"List is a raw type. References to generic type List<E> should be parameterized\n" + 
    		"----------\n" + 
    		"2. WARNING in TeamA12grf10.java (at line 9)\n" + 
    		"	callin List ci() { return base.ci(); }\n" + 
    		"	       ^^^^\n" + 
    		"Type safety: The return type List for ci() from the type TeamA12grf10.R2 needs unchecked conversion to conform to List<String> from the type TeamA12grf10.R1\n" + 
    		"----------\n");
    }

    // a role method has a return type with wildcard  - reported by Olaf Otto
    // A.1.2-otjld-generic-role-feature-11
    public void testA12_genericRoleFeature11() {
       
       runConformTest(
            new String[] {
		"TeamA12grf11.java",
			    "\n" +
			    "public team class TeamA12grf11 {\n" +
			    "    public class R {\n" +
			    "        public Class<?> getMyClass() {\n" +
			    "            return this.getClass();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    TeamA12grf11() {}\n" +
			    "    TeamA12grf11(final TeamA12grf11 other) {\n" +
			    "        R<@other> r = new R<@other>();\n" +
			    "        if (r instanceof R) // dependent types\n" +
			    "            System.out.print(\"Not\");\n" +
			    "        if (R.class.isAssignableFrom(r.getMyClass())) // raw types (r is __OT__R)\n" +
			    "            System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA12grf11(new TeamA12grf11());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an externalized role is used as a type argument
    // A.1.2-otjld-generic-role-feature-12
    public void testA12_genericRoleFeature12() {
       
       runConformTest(
            new String[] {
		"TA12grf12Main.java",
			    "\n" +
			    "import java.util.List;\n" +
			    "import java.util.ArrayList;\n" +
			    "public class TA12grf12Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA12grf12 t = new TeamA12grf12();\n" +
			    "        List<R<@t>> rs =  new ArrayList<R<@t>>();\n" +
			    "        rs.add(t.newR());\n" +
			    "        rs.get(0).test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA12grf12.java",
			    "\n" +
			    "public team class TeamA12grf12 {\n" +
			    "    public class R {\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public R newR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an externalized role is used as a type argument - wrong team instance
    // A.1.2-otjld-generic-role-feature-12f
    public void testA12_genericRoleFeature12f() {
        runNegativeTestMatching(
            new String[] {
		"TA12grf12fMain.java",
			    "\n" +
			    "import java.util.List;\n" +
			    "import java.util.ArrayList;\n" +
			    "public class TA12grf12fMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA12grf12f t = new TeamA12grf12f();\n" +
			    "        List<R<@t>> rs =  new ArrayList<R<@t>>();\n" +
			    "        final TeamA12grf12f t2 = new TeamA12grf12f();\n" +
			    "        rs.add(t2.newR());\n" +
			    "        rs.get(0).test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA12grf12f.java",
			    "\n" +
			    "public team class TeamA12grf12f {\n" +
			    "    public class R {\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public R newR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "not applicable");
    }

    // an externalized role is used as a type argument - wrong team instance
    // A.1.2-otjld-generic-role-feature-12f2
    public void testA12_genericRoleFeature12f2() {
        runNegativeTestMatching(
            new String[] {
		"TA12grf12f2Main.java",
			    "\n" +
			    "import java.util.List;\n" +
			    "import java.util.ArrayList;\n" +
			    "public class TA12grf12f2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA12grf12f2 t = new TeamA12grf12f2();\n" +
			    "        List<R<@t>> rs =  new ArrayList<R<@t>>();\n" +
			    "        rs.add(t.newR());\n" +
			    "        final TeamA12grf12f2 t2 = new TeamA12grf12f2();\n" +
			    "        R<@t2> r2 = rs.get(0);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA12grf12f2.java",
			    "\n" +
			    "public team class TeamA12grf12f2 {\n" +
			    "    public class R {\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public R newR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1.2.2(e)");
    }

    // a role method uses a type parameter of its enclosing team
    public void testA12_genericRoleFeature13() {
    	runConformTest(
    		new String[] {
    	"TeamA12grf13_2.java",
    			"public team class TeamA12grf13_2 extends TeamA12grf13_1<String> {\n" +
    			"   @Override\n" +
    			"	public class R {\n" +
    			"        @Override\n" +
    			"        protected void test(String u) {\n" +
    			"            System.out.print(u);\n" +
    			"        }\n" +
    			"   }\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf13_2().test(\"OK\");\n" +
    			"   }\n" +
    			"}\n",
    	"TeamA12grf13_1.java",
    			"public team class TeamA12grf13_1<U> {\n" +
    			"	public class R {\n" +
    			"        protected void test(U u){}" +
    			"   }\n" +
    			"   void test(U u) {\n" +
    			"       R r = new R();\n" +
    			"       r.test(u);\n" +
    			"   }\n" +
    			"}\n"
    		},
    		"OK");
    }

    // a role method uses a type parameter of its enclosing team - two ways of role creation
    public void testA12_genericRoleFeature14() {
    	runConformTest(
    		new String[] {
    	"TeamA12grf14_2.java",
    			"public team class TeamA12grf14_2 extends TeamA12grf14_1<String> {\n" +
    			"   @Override\n" +
    			"	public class R {\n" +
    			"        protected R() { tsuper(); System.out.print(2); }\n" +
    			"        @Override\n" +
    			"        protected void test(String u) {\n" +
    			"            System.out.print(u);\n" +
    			"        }\n" +
    			"   }\n" +
    			"   R foo() { System.out.print(5); return new R();}\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf14_2().test(\"OK\");\n" +
    			"   }\n" +
    			"}\n",
    	"TeamA12grf14_1.java",
    			"public team class TeamA12grf14_1<U> {\n" +
    			"	public class R {\n" +
    			"        protected R() { System.out.print(1); }\n" +
    			"        protected void test(U u){}" +
    			"   }\n" +
    			"   R foo() { System.out.print(4); return new R(); }\n" +
    			"   void test(U u) {\n" +
    			"       System.out.print(\"-\"+foo().getClass().getName()+\"-\");\n" +
    			"       R r = new R();\n" +
    			"       System.out.print(\"-\"+r.getClass().getName()+\"-\");\n" +
    			"       r.test(u);\n" +
    			"   }\n" +
    			"}\n"
    		},
    		"512-TeamA12grf14_2$__OT__R-12-TeamA12grf14_2$__OT__R-OK");
    }

    // a role method uses a type parameter of its enclosing team in a callin method & binding
    public void testA12_genericRoleFeature15() {
    	runConformTest(
    		new String[] {
    	"TeamA12grf15_2.java",
    			"public team class TeamA12grf15_2 extends TeamA12grf15_1<String> {\n" +
    			"   @Override\n" +
    			"	public class R playedBy TA12grf15 {\n" +
    			"        @Override\n" +
    			"        callin String test(String u) {\n" +
    			"            return \"O\"+base.test(u);\n" +
    			"        }\n" +
    			"		 test <- replace test;\n" +
    			"   }\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf15_2().activate();\n" +
    			"       System.out.print(new TA12grf15().test(\"K\"));" +
    			"   }\n" +
    			"}\n",
    	"TeamA12grf15_1.java",
    			"public team class TeamA12grf15_1<U> {\n" +
    			"	public class R {\n" +
    			"        @SuppressWarnings(\"basecall\")\n" +
    			"        callin U test(U u){ return u; }\n" +
    			"   }\n" +
    			"}\n",
    	"TA12grf15.java",
    			"public class TA12grf15 {\n" +
    			"   protected String test(String u){ return u;}\n" +
    			"}\n"
    		},
    		"OK");
    }

    // a role method uses a type parameter of its enclosing team in a static callin method & binding
    // other role is phantom
    public void testA12_genericRoleFeature15s() {
    	runConformTest(
    		new String[] {
    	"TeamA12grf15s_2.java",
    			"public team class TeamA12grf15s_2 extends TeamA12grf15s_1<String> {\n" +
    			"   @Override\n" +
    			"	public class R playedBy TA12grf15s {\n" +
//    			"        @Override\n" +
    			"        static callin String test(String u) {\n" +
    			"            return \"O\"+base.test(u);\n" +
    			"        }\n" +
    			"		 test <- replace test;\n" +
    			"   }\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf15s_2().activate();\n" +
    			"       System.out.print(TA12grf15s.test(\"K\"));" +
    			"   }\n" +
    			"}\n",
    	"TeamA12grf15s_1.java",
    			"public team class TeamA12grf15s_1<U> {\n" +
    			"	public class R {\n" +
    			"        @SuppressWarnings(\"basecall\")\n" +
    			"        static callin U test(U u){ return new OtherRole().idem(u); }\n" +
    			"   }\n" +
    			"   public class OtherRole {\n" +
    			"        protected U idem(U u) { return u; }\n" +
    			"   }\n" +
    			"}\n",
    	"TA12grf15s.java",
    			"public class TA12grf15s {\n" +
    			"   static protected String test(String u){ return u;}\n" +
    			"}\n"
    		},
    		"OK");
    }

    // a role method uses a type parameter of its enclosing team in a inherited static callin method & local binding
    public void testA12_genericRoleFeature15si() {
    	runConformTest(
    		new String[] {
    	"TeamA12grf15si_2.java",
    			"public team class TeamA12grf15si_2 extends TeamA12grf15si_1<String> {\n" +
    			"   @Override\n" +
    			"	public class R playedBy TA12grf15si {\n" +
    			"		 test <- replace test;\n" +
    			"   }\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf15si_2().activate();\n" +
    			"       System.out.print(TA12grf15si.test(\"K\"));" +
    			"   }\n" +
    			"}\n",
    	"TeamA12grf15si_1.java",
    			"public team class TeamA12grf15si_1<U> {\n" +
    			"	public class R {\n" +
    			"        static callin U test(U u){\n" +
    			"            System.out.print(\"O\");\n" +
    			"            return base.test(u);\n" +
    			"        }\n" +
    			"   }\n" +
    			"}\n",
    	"TA12grf15si.java",
    			"public class TA12grf15si {\n" +
    			"   static protected String test(String u){ return u+\"!\";}\n" +
    			"}\n"
    		},
    		"OK!");
    }


    // a role method uses a type parameter of its enclosing team, callin to private with inheritance
    // middle team instantiates parameter
    public void testA12_genericRoleFeature16() {
    	runConformTest(
    		new String[] {
    	"TeamA12grf16_3.java",
    			"public team class TeamA12grf16_3 extends TeamA12grf16_2 {\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf16_3().activate();\n" +
    			"       System.out.print(new TA12grf16().test(\"K\"));" +
    			"   }\n" +
    			"}\n",
    	"TeamA12grf16_2.java",
				"public team class TeamA12grf16_2 extends TeamA12grf16_1<String> {\n" +
				"   @Override\n" +
				"	public class R playedBy TA12grf16 {\n" +
				"		 test <- before test;\n" +
				"   }\n" +
				"}\n",
    	"TeamA12grf16_1.java",
    			"public team class TeamA12grf16_1<U> {\n" +
    			"	public class R {\n" +
				"        private U test(U u) {\n" +
				"            System.out.print(\"O\");\n" +
				"            return u;\n" +
				"        }\n" +
    			"   }\n" +
    			"}\n",
    	"TA12grf16.java",
    			"public class TA12grf16 {\n" +
    			"   protected String test(String u){ return u;}" +
    			"}\n"
    		},
    		"OK");
    }

    // a role method uses a type parameter of its enclosing team, callin to private with inheritance
    // subteam has callin binding
    public void testA12_genericRoleFeature16a() {
    	runConformTest(
    		new String[] {
    	"TeamA12grf16a_3.java",
    			"public team class TeamA12grf16a_3 extends TeamA12grf16a_2<String> {\n" +
				"   @Override\n" +
				"	public class R playedBy TA12grf16a {\n" +
				"		 test <- before test;\n" +
				"   }\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf16a_3().activate();\n" +
    			"       System.out.print(new TA12grf16a().test(\"K\"));" +
    			"   }\n" +
    			"}\n",
    	"TeamA12grf16a_2.java",
				"public team class TeamA12grf16a_2<U> extends TeamA12grf16a_1<U> {\n" +
				"}\n",
    	"TeamA12grf16a_1.java",
    			"public team class TeamA12grf16a_1<U> {\n" +
    			"	public class R {\n" +
				"        private U test(U u) {\n" +
				"            System.out.print(\"O\");\n" +
				"            return u;\n" +
				"        }\n" +
    			"   }\n" +
    			"}\n",
    	"TA12grf16a.java",
    			"public class TA12grf16a {\n" +
    			"   protected String test(String u){ return u;}" +
    			"}\n"
    		},
    		"OK");
    }


    // a role method uses a type parameter of its enclosing team, callin to private with inheritance-
    // type mismatch in callin binding (U <- String)
    public void testA12_genericRoleFeature16f() {
    	runNegativeTest(
    		new String[] {
    	"TeamA12grf16f_3.java",
    			"public team class TeamA12grf16f_3 extends TeamA12grf16f_2<String> {\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf16f_3().activate();\n" +
    			"       System.out.print(new TA12grf16f().test(\"K\"));" +
    			"   }\n" +
    			"}\n",
    	"TeamA12grf16f_2.java",
				"public team class TeamA12grf16f_2<U> extends TeamA12grf16f_1<U> {\n" +
				"   @Override\n" +
				"	public class R playedBy TA12grf16f {\n" +
				"		 test <- before test;\n" +
				"   }\n" +
				"}\n",
    	"TeamA12grf16f_1.java",
    			"public team class TeamA12grf16f_1<U> {\n" +
    			"	public class R {\n" +
				"        private U test(U u) {\n" +
				"            return u;\n" +
				"        }\n" +
    			"   }\n" +
    			"}\n",
    	"TA12grf16f.java",
    			"public class TA12grf16f {\n" +
    			"   protected String test(String u){ return u;}" +
    			"}\n"
    		},
    		"----------\n" + 
			"1. ERROR in TeamA12grf16f_2.java (at line 4)\n" + 
			"	test <- before test;\n" + 
			"	^^^^\n" + 
			(this.weavingScheme == WeavingScheme.OTRE
			? "Type mismatch: cannot convert from String to U\n"
			: "The method test(U) in the type TeamA12grf16f_2<U>.R is not applicable for the arguments (String)\n"
			) +
			"----------\n");
    }

    // a role does not use the type parameter of its enclosing team, has callin binding
    public void testA12_genericRoleFeature17() {
    	runConformTest(
    		new String[] {
    	"TeamA12grf17.java",
    			"public team class TeamA12grf17<U> {\n" +
    			"	public class R playedBy TA12grf17 {\n" +
    			"        callin String test(String u){ return base.test(u)+\"K\"; }\n" +
    			"		 test <- replace test;\n" +
    			"   }\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf17<String>().activate();\n" +
    			"       System.out.print(new TA12grf17().test(\"O\"));" +
    			"   }\n" +
    			"}\n",
    	"TA12grf17.java",
    			"public class TA12grf17 {\n" +
    			"   protected String test(String u){ return u;}\n" +
    			"}\n"
    		},
    		"OK");
    }

    // a role does not use the type parameter of its enclosing team, has callin binding, copied as phantom role
    // witness for "The return type is incompatible with TeamA12grf18_1<U>._OT$getClass$ILowerable()"
    public void testA12_genericRoleFeature18() {
    	runConformTest(
    		new String[] {
    	"TeamA12grf18_2.java",
    			"public team class TeamA12grf18_2<U> extends TeamA12grf18_1<U> {\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf18_2<String>().activate();\n" +
    			"       System.out.print(new TA12grf18().test(\"I\"));" +
    			"   }\n" +
    			"}\n",
    	"TeamA12grf18_1.java",
    			"public team class TeamA12grf18_1<U> {\n" +
    			"	public class R playedBy TA12grf18 {\n" +
    			"        callin String test(String u){ return base.test(u)+\"K\"; }\n" +
    			"		 test <- replace test;\n" +
    			"   }\n" +
    			"}\n",
    	"TA12grf18.java",
    			"public class TA12grf18 {\n" +
    			"   protected String test(String u){ return u;}\n" +
    			"}\n"
    		});
    	runConformTest(
    		new String[] {
    	"TeamA12grf18_2.java",
    			"public team class TeamA12grf18_2<U> extends TeamA12grf18_1<U> {\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf18_2<String>().activate();\n" +
    			"       System.out.print(new TA12grf18().test(\"O\"));" +
    			"   }\n" +
    			"}\n",
    		},
    		"OK",
    		null,
    		false/*shouldFlushOutputDirectory*/,
    		new String[] {"-DA12grf18_2=2"}); // force new vm launch to accept the new class version
    }

    // a role does not use the type parameter of its enclosing team, has callin binding, copied as phantom role
    public void testA12_genericRoleFeature19() {
    	runConformTest(
    		new String[] {
    	"TeamA12grf19_2.java",
    			"public team class TeamA12grf19_2 extends TeamA12grf19_1<String> {\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf19_2().activate();\n" +
    			"       System.out.print(new TA12grf19().test(\"I\"));" +
    			"   }\n" +
    			"}\n",
    	"TeamA12grf19_1.java",
    			"public team class TeamA12grf19_1<U> {\n" +
    			"	public class R playedBy TA12grf19 {\n" +
    			"        callin String test(String u){ return base.test(u)+\"K\"; }\n" +
    			"		 test <- replace test;\n" +
    			"   }\n" +
    			"}\n",
    	"TA12grf19.java",
    			"public class TA12grf19 {\n" +
    			"   protected String test(String u){ return u;}\n" +
    			"}\n"
    		});
    	runConformTest(
    		new String[] {
    	"TeamA12grf19_2.java",
    			"public team class TeamA12grf19_2 extends TeamA12grf19_1<String> {\n" +
    			"   public static void main(String... args) {\n" +
    			"       new TeamA12grf19_2().activate();\n" +
    			"       System.out.print(new TA12grf19().test(\"O\"));" +
    			"   }\n" +
    			"}\n",
    		},
    		"OK",
    		null,
    		false/*shouldFlushOutputDirectory*/,
    		new String[] {"-DA12grf19_2=2"}); // force new vm launch to accept the new class version
    }

    // a parameter of a callout binding requires autoboxing - explicit mapping
    // A.1.3-otjld-autoboxing-in-method-mapping-1
    public void testA13_autoboxingInMethodMapping1() {
       
       runConformTest(
            new String[] {
		"TeamA13aimm1.java",
			    "\n" +
			    "public team class TeamA13aimm1 {\n" +
			    "    protected class R playedBy TA13aimm1 {\n" +
			    "        protected R() { base(); }\n" +
			    "        protected void test(int i) -> void test(Integer i)\n" +
			    "            with {    i  ->                   i }\n" +
			    "    }\n" +
			    "    TeamA13aimm1 () {\n" +
			    "        new R().test(1);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA13aimm1();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA13aimm1.java",
			    "\n" +
			    "public class TA13aimm1 {\n" +
			    "    void test(Integer i) {\n" +
			    "        System.out.print(i);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "1");
    }

    // a parameter of a callout binding requires autoboxing - implicit mapping
    // A.1.3-otjld-autoboxing-in-method-mapping-2
    public void testA13_autoboxingInMethodMapping2() {
       
       runConformTest(
            new String[] {
		"TeamA13aimm2.java",
			    "\n" +
			    "public team class TeamA13aimm2 {\n" +
			    "    protected class R playedBy TA13aimm2 {\n" +
			    "        protected R() { base(); }\n" +
			    "        protected void test(int i) -> void test(Integer i);\n" +
			    "    }\n" +
			    "    TeamA13aimm2 () {\n" +
			    "        new R().test(1);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA13aimm2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA13aimm2.java",
			    "\n" +
			    "public class TA13aimm2 {\n" +
			    "    void test(Integer i) {\n" +
			    "        System.out.print(i);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "1");
    }

    // a parameter of a callout binding requires autounboxing - explicit mapping
    // A.1.3-otjld-autoboxing-in-method-mapping-3
    public void testA13_autoboxingInMethodMapping3() {
       
       runConformTest(
            new String[] {
		"TeamA13aimm3.java",
			    "\n" +
			    "public team class TeamA13aimm3 {\n" +
			    "    protected class R playedBy TA13aimm3 {\n" +
			    "        protected R() { base(); }\n" +
			    "        protected void test(Integer i) -> void test(int i)\n" +
			    "            with {        i  ->               i }\n" +
			    "    }\n" +
			    "    TeamA13aimm3 () {\n" +
			    "        new R().test(new Integer(1));\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA13aimm3();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA13aimm3.java",
			    "\n" +
			    "public class TA13aimm3 {\n" +
			    "    void test(int i) {\n" +
			    "        System.out.print(i);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "1");
    }

    // a parameter of a callout binding requires autounboxing - implicit mapping
    // A.1.3-otjld-autoboxing-in-method-mapping-4
    public void testA13_autoboxingInMethodMapping4() {
       
       runConformTest(
            new String[] {
		"TeamA13aimm4.java",
			    "\n" +
			    "public team class TeamA13aimm4 {\n" +
			    "    protected class R playedBy TA13aimm4 {\n" +
			    "        protected R() { base(); }\n" +
			    "        protected void test(Integer i) -> void test(int i);\n" +
			    "    }\n" +
			    "    TeamA13aimm4 () {\n" +
			    "        new R().test(new Integer(1));\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA13aimm4();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA13aimm4.java",
			    "\n" +
			    "public class TA13aimm4 {\n" +
			    "    void test(int i) {\n" +
			    "        System.out.print(i);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "1");
    }

    // a parameter of a callout binding requires autounboxing - explicit mapping - lifting direction
    // A.1.3-otjld-autoboxing-in-method-mapping-5
    public void testA13_autoboxingInMethodMapping5() {
       
       runConformTest(
            new String[] {
		"TeamA13aimm5.java",
			    "\n" +
			    "public team class TeamA13aimm5 {\n" +
			    "    protected class R playedBy TA13aimm5 {\n" +
			    "        protected R() { base(); }\n" +
			    "        protected Integer test() -> int test()\n" +
			    "         with { result <- result}\n" +
			    "    }\n" +
			    "    TeamA13aimm5 () {\n" +
			    "        System.out.print(new R().test());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA13aimm5();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA13aimm5.java",
			    "\n" +
			    "public class TA13aimm5 {\n" +
			    "    int test() {\n" +
			    "        return 1;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "1");
    }

    // a parameter of a callout binding requires autounboxing - implicit mapping - lifting direction
    // A.1.3-otjld-autoboxing-in-method-mapping-6
    public void testA13_autoboxingInMethodMapping6() {
       
       runConformTest(
            new String[] {
		"TeamA13aimm6.java",
			    "\n" +
			    "public team class TeamA13aimm6 {\n" +
			    "    protected class R playedBy TA13aimm6 {\n" +
			    "        protected R() { base(); }\n" +
			    "        protected Integer test() -> int test();\n" +
			    "    }\n" +
			    "    TeamA13aimm6 () {\n" +
			    "        System.out.print(new R().test());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA13aimm6();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA13aimm6.java",
			    "\n" +
			    "public class TA13aimm6 {\n" +
			    "    int test() {\n" +
			    "        return 1;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "1");
    }

    // the return of a callin binding requires unboxing - implicit mapping
    // A.1.3-otjld-autoboxing-in-method-mapping-7
    public void testA13_autoboxingInMethodMapping7() {
       
       runConformTest(
            new String[] {
		"TeamA13aimm7.java",
			    "\n" +
			    "public team class TeamA13aimm7 {\n" +
			    "    protected class R playedBy TA13aimm7 {\n" +
			    "        void test(int i) {\n" +
			    "            System.out.print(i+1);\n" +
			    "        }\n" +
			    "        void test(int i) <- after void test(Integer i);\n" +
			    "    }\n" +
			    "    TeamA13aimm7 () {\n" +
			    "        this.activate();\n" +
			    "        new TA13aimm7().test(new Integer(1));\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA13aimm7();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA13aimm7.java",
			    "\n" +
			    "public class TA13aimm7 {\n" +
			    "    void test(Integer i) {\n" +
			    "        System.out.print(i);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "12");
    }

    // the return of a callin binding requires autounboxing - implicit mapping
    // A.1.3-otjld-autoboxing-in-method-mapping-8
    public void testA13_autoboxingInMethodMapping8() {
       
       runConformTest(
            new String[] {
		"TeamA13aimm8.java",
			    "\n" +
			    "public team class TeamA13aimm8 {\n" +
			    "    protected class R playedBy TA13aimm8 {\n" +
			    "        callin int test() {\n" +
			    "            return base.test()+1;\n" +
			    "        }\n" +
			    "        int test() <- replace Integer test();\n" +
			    "    }\n" +
			    "    TeamA13aimm8 () {\n" +
			    "        this.activate();\n" +
			    "        System.out.print(new TA13aimm8().test());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA13aimm8();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"TA13aimm8.java",
			    "\n" +
			    "public class TA13aimm8 {\n" +
			    "    Integer test() {\n" +
			    "        return new Integer(1);\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "2");
    }

    // the argument of a callin binding requires autoboxing - implicit mapping - signature less binding
    // A.1.3-otjld-autoboxing-in-method-mapping-9
    public void testA13_autoboxingInMethodMapping9() {
       
       runConformTest(
            new String[] {
		"TeamA13aimm9.java",
			    "\n" +
			    "public team class TeamA13aimm9 {\n" +
			    "    protected class R playedBy TA13aimm9 {\n" +
			    "        callin void logInteger(Integer arg) {\n" +
			    "            System.out.print(\"-> log \" + arg+\":\");\n" +
			    "            base.logInteger(arg+1);\n" +
			    "        }\n" +
			    "        logInteger <- replace foo;\n" +
			    "    }\n" +
			    "    TeamA13aimm9 () {\n" +
			    "        this.activate();\n" +
			    "        new TA13aimm9().foo(3);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA13aimm9();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA13aimm9.java",
			    "\n" +
			    "public class TA13aimm9 {\n" +
			    "    public void foo(int i) {\n" +
			    "        System.out.print(\"foo \" + i);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "-> log 3:foo 4");
    }

    // the argument of a callin binding requires autoboxing - callin unbound at site of decleration
    // A.1.3-otjld-autoboxing-in-method-mapping-10
    public void testA13_autoboxingInMethodMapping10() {
       
       runConformTest(
            new String[] {
		"TeamA13aimm10.java",
			    "\n" +
			    "public team class TeamA13aimm10 {\n" +
			    "    protected class R0 {\n" +
			    "        callin void logInteger(Integer arg) {\n" +
			    "            System.out.print(\"-> log \" + arg+\":\");\n" +
			    "            base.logInteger(arg+1);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected class R1 extends R0 playedBy TA13aimm10 {\n" +
			    "        logInteger <- replace foo;\n" +
			    "    }\n" +
			    "    TeamA13aimm10 () {\n" +
			    "        this.activate();\n" +
			    "        new TA13aimm10().foo(3);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA13aimm10();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA13aimm10.java",
			    "\n" +
			    "public class TA13aimm10 {\n" +
			    "    public void foo(int i) {\n" +
			    "        System.out.print(\"foo \" + i);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "-> log 3:foo 4");
    }

    // reported by Dehla, see TPX-468
    // A.1.4-otjld-autounboxing-in-method-mapping-1
    public void testA14_autounboxingInMethodMapping1() {
       
       runConformTest(
            new String[] {
		"TeamA14aimm1.java",
			    "\n" +
			    "public team class TeamA14aimm1 {\n" +
			    "    public class R playedBy TA14aimm1 {\n" +
			    "        protected R(int i) {\n" +
			    "            base(i); // autoboxing\n" +
			    "        }\n" +
			    "        public abstract int getId();\n" +
			    "        getId -> getId;\n" +
			    "    }\n" +
			    "    public TeamA14aimm1 () {\n" +
			    "        R r = new R(5);\n" +
			    "        System.out.print(r.getId());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA14aimm1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA14aimm1.java",
			    "\n" +
			    "public class TA14aimm1 {\n" +
			    "   Integer id;\n" +
			    "    public TA14aimm1(Integer anId) {\n" +
			    "        id = anId;\n" +
			    "    }\n" +
			    "    public Integer getId() {\n" +
			    "        return id;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "5");
    }

    // a role class literal is used in a static team method
    // A.1.5-otjld-role-class-literal-1
    public void testA15_roleClassLiteral1() {
        runNegativeTestMatching(
            new String[] {
		"TeamA15rcl1.java",
			    "\n" +
			    "public team class TeamA15rcl1 {\n" +
			    "    protected class R {}\n" +
			    "    static void wrong () {\n" +
			    "        System.out.print(R.class);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "6.1(c)");
    }

    // a role class literal is used in a static role method
    // A.1.5-otjld-role-class-literal-2
    public void testA15_roleClassLiteral2() {
       
       runConformTest(
            new String[] {
		"TeamA15rcl2.java",
			    "\n" +
			    "public team class TeamA15rcl2 {\n" +
			    "    protected class R {\n" +
			    "        protected static void print () {\n" +
			    "            System.out.print(R.class.getName());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    TeamA15rcl2() {\n" +
			    "        R.print();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA15rcl2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "TeamA15rcl2$R");
    }

    // syntax error in a role class literal
    // A.1.5-otjld-role-class-literal-3
    public void testA15_roleClassLiteral3() {
        runNegativeTestMatching(
            new String[] {
		"TA15rcl3.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class TA15rcl3 {\n" +
			    "    void test(final Team t) {\n" +
			    "        Object o = (3+4)<@t>.class;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1.2.4(c)");
    }

    // 
    // A.1.5-otjld-role-class-literal-4
    public void testA15_roleClassLiteral4() {
       
       runConformTest(
            new String[] {
		"TeamA15rcl4_2.java",
			    "\n" +
			    "public team class TeamA15rcl4_2 extends TeamA15rcl4_1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA15rcl4_1 t1 = new TeamA15rcl4_1();\n" +
			    "        final TeamA15rcl4_2 t2 = new TeamA15rcl4_2();\n" +
			    "        if (t1.isMyRole(R<@t2>.class))\n" +
			    "            System.out.print(\"Y\");\n" +
			    "        else\n" +
			    "            System.out.print(\"N\");\n" +
			    "        if (t1.isMyRole(R<@t1>.class))\n" +
			    "            System.out.print(\"Y\");\n" +
			    "        else\n" +
			    "            System.out.print(\"N\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA15rcl4_1.java",
			    "\n" +
			    "public team class TeamA15rcl4_1 {\n" +
			    "    public class R {}\n" +
			    "    public boolean isMyRole(Class<?> c) {\n" +
			    "        return c == R.class;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "NY");
    }

    // an invisible type is mentioned as a type parameter in a callout RHS
    // A.1.6-otjld-decapsulation-for-type-parameter-1
    public void testA16_decapsulationForTypeParameter1() {
       
       runConformTest(
            new String[] {
		"TeamA16dftp1.java",
			    "\n" +
			    "import java.util.ArrayList;\n" +
			    "import base p1.TA16dftp1;\n" +
			    "@SuppressWarnings(\"decapsulation\")\n" +
			    "public team class TeamA16dftp1 {\n" +
			    "    protected class R playedBy TA16dftp1 {\n" +
			    "        static abstract R getInstance();\n" +
			    "        R getInstance() -> get ArrayList<TA16dftp1> instances\n" +
			    "            with { result <- instances.get(0) }\n" +
			    "        String getVal() -> get String val;\n" +
			    "        protected static void test() {\n" +
			    "            System.out.print(getInstance().getVal());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    TeamA16dftp1() {\n" +
			    "        R.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA16dftp1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/TA16dftp1.java",
			    "\n" +
			    "package p1;\n" +
			    "import java.util.ArrayList;\n" +
			    "class TA16dftp1 {\n" +
			    "    String val;\n" +
			    "    TA16dftp1 (String v) { this.val = v; }\n" +
			    "    static ArrayList<TA16dftp1> instances = new ArrayList<TA16dftp1>();\n" +
			    "    static {\n" +
			    "        instances.add(new TA16dftp1(\"OK\"));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is played by the raw type of a generic base class
    // A.1.7-otjld-generic-baseclass-1
    public void testA17_genericBaseclass1() {
       
       runConformTest(
            new String[] {
		"TeamA17gb1.java",
			    "\n" +
			    "public team class TeamA17gb1 {\n" +
			    "    @SuppressWarnings(\"rawtypes\")\n" +
			    "    protected class R playedBy TA17gb1 {\n" +
			    "        void bef(String s) {\n" +
			    "            System.out.print(s+\"K\");\n" +
			    "        }\n" +
			    "        void bef(String s) <- before void test(Object o)\n" +
			    "            with { s <- (String)o }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA17gb1().activate();\n" +
			    "        new TA17gb1<String>().test(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA17gb1.java",
			    "\n" +
			    "public class TA17gb1<T> {\n" +
			    "    void test(T o) {\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OKO");
    }

    // a role is played by a parameterized base class
    // A.1.7-otjld-generic-baseclass-2
    public void testA17_genericBaseclass2() {
        runNegativeTestMatching(
            new String[] {
		"TeamA17gb2.java",
			    "\n" +
			    "public team class TeamA17gb2 {\n" +
			    "    protected class R playedBy TA17gb2<String> {\n" +
			    "        void bef(String s) {\n" +
			    "            System.out.print(s+\"K\");\n" +
			    "        }\n" +
			    "        void bef(String s) <- before void test(String o);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA17gb2().activate();\n" +
			    "        new TA17gb2<String>().test(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA17gb2.java",
			    "\n" +
			    "public class TA17gb2<T> {\n" +
			    "    void test(T o) {\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "2.1.2(e)");
    }

    // a parameterized role is played by a parameterized base class
    // A.1.7-otjld-generic-baseclass-3
    public void testA17_genericBaseclass3() {
       
       runConformTest(
            new String[] {
		"TeamA17gb3.java",
			    "\n" +
			    "public team class TeamA17gb3 {\n" +
			    "    protected class R<U> playedBy TA17gb3<U> {\n" +
			    "        void bef(U s) {\n" +
			    "            print(s);\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void bef(U s) <- before void test1(U o);\n" +
			    "        void print(U s) -> void test2(U o);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA17gb3().activate();\n" +
			    "        new TA17gb3<String>().test1(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA17gb3.java",
			    "\n" +
			    "public class TA17gb3<T> {\n" +
			    "    void test1(T o) {\n" +
			    "        System.out.print(o);\n" +
			    "    }\n" +
			    "    void test2(T o) {\n" +
			    "        System.out.print(\"(\"+o+\")\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "(O)KO");
    }

    // a generic list is modified from its role
    // A.1.7-otjld-generic-baseclass-4
    public void testA17_genericBaseclass4() {
       
       runConformTest(
            new String[] {
		"TA17gb4Main.java",
			    "\n" +
			    "public class TA17gb4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA17gb4 t = new TeamA17gb4();\n" +
			    "        TA17gb4List<String> strings = new TA17gb4List<String>();\n" +
			    "        strings.addLast(\"O\");\n" +
			    "        strings.addLast(\"K\");\n" +
			    "        t.manipulate(strings, \"!\");\n" +
			    "        for(String s : strings)\n" +
			    "            System.out.print(s);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA17gb4.java",
			    "\n" +
			    "public team class TeamA17gb4 {\n" +
			    "    protected class R<U> playedBy TA17gb4List<U> {\n" +
			    "        protected void put(U elem) -> boolean add(U elem);\n" +
			    "        protected void duplicate() {\n" +
			    "            U elem = getLast();\n" +
			    "            put(elem);\n" +
			    "        }\n" +
			    "        U getLast() -> U getLast();\n" +
			    "    }\n" +
			    "    public <X> void manipulate(TA17gb4List<X> as R<X> r, X e) {\n" +
			    "        r.duplicate();\n" +
			    "        r.put(e);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA17gb4List.java",
			    "\n" +
			    "@SuppressWarnings(\"serial\")\n" +
			    "public class TA17gb4List<T> extends java.util.LinkedList<T> {}\n" +
			    "    \n"
            },
            "OKK!");
    }

    // a generic list is modified from its role
    // A.1.7-otjld-generic-baseclass-5
    public void testA17_genericBaseclass5() {
       
       runConformTest(
            new String[] {
		"TA17gb5Main.java",
			    "\n" +
			    "public class TA17gb5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA17gb5 t = new TeamA17gb5();\n" +
			    "        TA17gb5List<String> strings = new TA17gb5List<String>();\n" +
			    "        strings.addLast(\"O\");\n" +
			    "        strings.addLast(\"K\");\n" +
			    "        String s = t.manipulate(strings, \"!\");\n" +
			    "        System.out.print(s);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA17gb5.java",
			    "\n" +
			    "import java.util.Iterator;\n" +
			    "public team class TeamA17gb5 {\n" +
			    "    protected class R<U> implements Iterable<U> playedBy TA17gb5List<U> {\n" +
			    "        protected void put(U elem) -> boolean add(U elem);\n" +
			    "        protected void duplicate() {\n" +
			    "            U elem = getLast();\n" +
			    "            put(elem);\n" +
			    "        }\n" +
			    "        U getLast() -> U getLast();\n" +
			    "        Iterator<U> iterator() -> Iterator<U> iterator();\n" +
			    foreach("U") +
			    spliteratorCallout() +
			    "    }\n" +
			    "    public String manipulate(TA17gb5List<String> as R<String> r, String e) {\n" +
			    "        r.duplicate();\n" +
			    "        for(String elem : r)\n" +
			    "            if (!elem.equals(e))\n" +
			    "                return elem;\n" +
			    "        return null;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA17gb5List.java",
			    "\n" +
			    "@SuppressWarnings(\"serial\")\n" +
			    "public class TA17gb5List<T> extends java.util.LinkedList<T> {}\n" +
			    "    \n"
            },
            "O");
    }

    // a generic list is modified from its role - iterate over role/base as an Iterable
    // A.1.7-otjld-generic-baseclass-6
    public void testA17_genericBaseclass6() {
       
       runConformTest(
            new String[] {
		"TA17gb6Main.java",
			    "\n" +
			    "public class TA17gb6Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA17gb6 t = new TeamA17gb6();\n" +
			    "        TA17gb6List<String> strings = new TA17gb6List<String>();\n" +
			    "        strings.addLast(\"O\");\n" +
			    "        strings.addLast(\"K\");\n" +
			    "        String s = t.manipulate(strings, \"!\");\n" +
			    "        System.out.print(s);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA17gb6List.java",
			    "\n" +
			    "@SuppressWarnings(\"serial\")\n" +
			    "public class TA17gb6List<T> extends java.util.LinkedList<T> { }\n" +
			    "    \n",
		"TeamA17gb6.java",
			    "\n" +
			    "import java.util.Iterator;\n" +
			    "public team class TeamA17gb6 {\n" +
			    "    protected class R<U> implements Iterable<U> playedBy TA17gb6List<U> {\n" +
			    "        protected void put(U elem) -> boolean add(U elem);\n" +
			    "        protected void duplicate() {\n" +
			    "            U elem = getLast();\n" +
			    "            put(elem);\n" +
			    "        }\n" +
			    "        U getLast() -> U getLast();\n" +
			    "        Iterator<U> iterator() -> Iterator<U> iterator();\n" +
			    foreach("U") +
			    spliteratorCallout() +
			    "    }\n" +
			    "    public <X> X manipulate(TA17gb6List<X> as R<X> r, X e) {\n" +
			    "        r.duplicate();\n" +
			    "        for(X elem : r)\n" +
			    "            if (!elem.equals(e))\n" +
			    "                return elem;\n" +
			    "        return null;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "O");
    }

    // a callout binding has a type parameter whereas its super-role-method uses the raw type
    // A.1.8-otjld-callout-redefines-inherited-1
    public void testA18_calloutRedefinesInherited1() {
       
       runConformTest(
            new String[] {
		"TeamA18cri1.java",
			    "\n" +
			    "import java.util.Vector;\n" +
			    "@SuppressWarnings(\"rawtypes\")\n" +
			    "public team class TeamA18cri1 {\n" +
			    "    protected abstract class R0 {\n" +
			    "        Vector getElements() { return null; }\n" +
			    "    }\n" +
			    "    protected class R1 extends R0 playedBy TA18cri1 {\n" +
			    "        protected Vector<String> getElements() => Vector<String> getStrings();\n" +
			    "    }\n" +
			    "    TeamA18cri1(TA18cri1 as R1 r) {\n" +
			    "        for (String s: r.getElements())\n" +
			    "            System.out.print(s);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA18cri1(new TA18cri1());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA18cri1.java",
			    "\n" +
			    "import java.util.Vector;\n" +
			    "public class TA18cri1 {\n" +
			    "        Vector<String> getStrings() {\n" +
			    "            Vector<String> result= new Vector<String>();\n" +
			    "            result.add(\"O\");\n" +
			    "            result.add(\"k\");\n" +
			    "            return result;\n" +
			    "        }\n" +
			    "}\n" +
			    "    \n"
            },
            "Ok");
    }

    // a callout binding covariantly redefines the return type of the abstract bound role method
    // A.1.8-otjld-callout-redefines-inherited-2
    public void testA18_calloutRedefinesInherited2() {
       
       runConformTest(
            new String[] {
		"TeamA18cri2.java",
			    "\n" +
			    "public team class TeamA18cri2 {\n" +
			    "    protected abstract class R0 {\n" +
			    "        protected abstract Object getElement();\n" +
			    "    }\n" +
			    "    protected class R1 extends R0 playedBy TA18cri2 {\n" +
			    "        protected String getElement() -> String getString();\n" +
			    "    }\n" +
			    "    TeamA18cri2(TA18cri2 as R1 r) {\n" +
			    "        System.out.print(r.getElement());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA18cri2(new TA18cri2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA18cri2.java",
			    "\n" +
			    "public class TA18cri2 {\n" +
			    "        String getString() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin method has a generic argument
    // A.1.8-otjld-callin-method-with-generic-1
    public void testA18_callinMethodWithGeneric1() {
       
       runConformTest(
            new String[] {
		"TA18cmwg1Main.java",
			    "\n" +
			    "import java.util.ArrayList;\n" +
			    "public class TA18cmwg1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA18cmwg1().activate();\n" +
			    "        ArrayList<String> ls= new ArrayList<String>();\n" +
			    "        ls.add(\"O\");\n" +
			    "        ls.add(\"K\");\n" +
			    "        new TA18cmwg1().bar(ls);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA18cmwg1.java",
			    "\n" +
			    "import java.util.List;\n" +
			    "public team class TeamA18cmwg1 {\n" +
			    "    protected class R playedBy TA18cmwg1 {\n" +
			    "        callin void foos(List<String> ls) {\n" +
			    "            System.out.print(ls.get(0));\n" +
			    "            base.foos(ls);\n" +
			    "        }\n" +
			    "        void foos(List<String> ls) <- replace void bar(List<String> ls);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA18cmwg1.java",
			    "\n" +
			    "import java.util.List;\n" +
			    "public class TA18cmwg1 {\n" +
			    "    void bar(List<String> ls) {\n" +
			    "        System.out.print(ls.get(1));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a method has a type parameter constrained by "base"
    // A.1.9-otjld-basetype-parameter-1
    public void testA19_basetypeParameter1() {
       
       runConformTest(
            new String[] {
		"TeamA19bp1.java",
			    "\n" +
			    "public team class TeamA19bp1 {\n" +
			    "    protected abstract class Role0 {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    protected class Role1 extends Role0 playedBy TA19bp1_1 {\n" +
			    "        print -> print1;\n" +
			    "    }\n" +
			    "    protected class Role2 extends Role0 playedBy TA19bp1_2 {\n" +
			    "        print -> print2;\n" +
			    "    }\n" +
			    "    public <B base Role0> void test(B as Role0 o) {\n" +
			    "        o.print();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA19bp1 t= new TeamA19bp1();\n" +
			    "        t.test(new TA19bp1_1());\n" +
			    "        t.test(new TA19bp1_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp1_1.java",
			    "\n" +
			    "public class TA19bp1_1 {\n" +
			    "    void print1() {\n" +
			    "        System.out.print(\"Base1\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp1_2.java",
			    "\n" +
			    "public class TA19bp1_2 {\n" +
			    "    void print2() {\n" +
			    "        System.out.print(\"Base2\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "Base1Base2");
    }

    // a method has a type parameter constrained by "base" - bound is not a role
    // A.1.9-otjld-basetype-parameter-2
    public void testA19_basetypeParameter2() {
        runNegativeTestMatching(
            new String[] {
		"TeamA19bp2.java",
			    "\n" +
			    "public team class TeamA19bp2 {\n" +
			    "    protected abstract class Role0 {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    public <B base String> void test(B as Role0 o) {\n" +
			    "        o.print();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "2.3.2(e)");
    }

    // a method has a type parameter constrained by "base" - ambiguous lifting
    // A.1.9-otjld-basetype-parameter-3
    public void testA19_basetypeParameter3() {
        runNegativeTest(
            new String[] {
		"TeamA19bp3.java",
			    "\n" +
			    "public team class TeamA19bp3 {\n" +
			    "    protected abstract class Role0 {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    protected class Role1 extends Role0 playedBy TA19bp3 {\n" +
			    "        print -> print1;\n" +
			    "    }\n" +
			    "    protected class Role2 extends Role0 playedBy TA19bp3 {\n" +
			    "        print -> print1;\n" +
			    "    }\n" +
			    "    public <B base Role0> void test(B as Role0 o) {\n" +
			    "        o.print();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp3.java",
			    "\n" +
			    "public class TA19bp3 {\n" +
			    "    void print1() {\n" +
			    "        System.out.print(\"Base1\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamA19bp3.java (at line 12)\n" + 
    		"	public <B base Role0> void test(B as Role0 o) {\n" + 
    		"	                                ^^^^^^^^^^\n" + 
    		"Unhandled exception type LiftingFailedException, caused by an unsafe lifting request (OTJLD 2.3.5).\n" + 
    		"----------\n");
    }

    // a method has a type parameter constrained by "base" - ambiguous lifting
    // make sure secondary occurrences of LiftingFailedException are reported w/o OTJLD reference.
    public void testA19_basetypeParameter3a() {
        runNegativeTest(
            new String[] {
		"TeamA19bp3a.java",
			    "import org.objectteams.LiftingFailedException;\n" +
			    "public team class TeamA19bp3a {\n" +
			    "    protected abstract class Role0 {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    protected class Role1 extends Role0 playedBy TA19bp3a {\n" +
			    "        print -> print1;\n" +
			    "    }\n" +
			    "    protected class Role2 extends Role0 playedBy TA19bp3a {\n" +
			    "        print -> print1;\n" +
			    "    }\n" +
			    "    public <B base Role0> void test(B as Role0 o) throws LiftingFailedException {\n" +
			    "        o.print();\n" +
			    "    }\n" +
			    "    public void client(TA19bp3a b) {\n" +
			    "        test(b);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp3a.java",
			    "\n" +
			    "public class TA19bp3a {\n" +
			    "    void print1() {\n" +
			    "        System.out.print(\"Base1\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamA19bp3a.java (at line 16)\n" + 
    		"	test(b);\n" + 
    		"	^^^^^^^\n" + 
    		"Unhandled exception type LiftingFailedException\n" + 
    		"----------\n");
    }

    // a method has a type parameter constrained by "base" - incompatible invocation
    // A.1.9-otjld-basetype-parameter-4
    public void testA19_basetypeParameter4() {
        runNegativeTestMatching(
            new String[] {
		"TeamA19bp4.java",
			    "\n" +
			    "public team class TeamA19bp4 {\n" +
			    "    protected abstract class Role0 {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    protected class Role1 extends Role0 playedBy TA19bp4_1 {\n" +
			    "        print -> print1;\n" +
			    "    }\n" +
			    "    protected class Role2 playedBy TA19bp4_2 {\n" +
			    "        void print() -> void print1();\n" +
			    "    }\n" +
			    "    public <B base Role0> void test(B as Role0 o) {\n" +
			    "        o.print();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA19bp4 t= new TeamA19bp4();\n" +
			    "        t.test(new TA19bp4_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp4_1.java",
			    "\n" +
			    "public class TA19bp4_1 {\n" +
			    "    void print1() {\n" +
			    "        System.out.print(\"Base1\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp4_2.java",
			    "\n" +
			    "public class TA19bp4_2 {\n" +
			    "    void print1() {\n" +
			    "        System.out.print(\"Base1\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TeamA19bp4.java (at line 17)\n" + 
    		"	t.test(new TA19bp4_2());\n" + 
    		"	  ^^^^\n" + 
    		"Bound mismatch: The generic method test(B) of type TeamA19bp4 is not applicable for the arguments (TA19bp4_2). The inferred type TA19bp4_2 is not a valid substitute for the bounded parameter <B base TeamA19bp4.Role0> (OTJLD 2.3.2(e)).\n" + 
    		"----------\n");
    }

    // a method has a type parameter constrained by "base" - nested team
    // A.1.9-otjld-basetype-parameter-5
    public void testA19_basetypeParameter5() {
       
       runConformTest(
            new String[] {
		"TeamA19bp5.java",
			    "\n" +
			    "public team class TeamA19bp5 {\n" +
			    "    public class TA19bp5_2 {\n" +
			    "        void print2() {\n" +
			    "            System.out.print(\"Base2\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class InnerTeam {\n" +
			    "        protected abstract class Role0 {\n" +
			    "            protected abstract void print();\n" +
			    "        }\n" +
			    "        protected class Role1 extends Role0 playedBy TA19bp5_1 {\n" +
			    "            print -> print1;\n" +
			    "        }\n" +
			    "        protected class Role2 extends Role0 playedBy TA19bp5_2 {\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            print -> print2;\n" +
			    "        }\n" +
			    "        public <B base Role0> void test(B as Role0 o) {\n" +
			    "            o.print();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        InnerTeam inner = new InnerTeam();\n" +
			    "        inner.test(new TA19bp5_1());\n" +
			    "        inner.test(new TA19bp5_2());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA19bp5 t= new TeamA19bp5();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp5_1.java",
			    "\n" +
			    "public class TA19bp5_1 {\n" +
			    "    void print1() {\n" +
			    "        System.out.print(\"Base1\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "Base1Base2");
    }

    // a method has a type parameter constrained by "base" - some inheritance used
    // A.1.9-otjld-basetype-parameter-6
    public void testA19_basetypeParameter6() {
       
       runConformTest(
            new String[] {
		"TeamA19bp6.java",
			    "\n" +
			    "public team class TeamA19bp6 {\n" +
			    "    protected abstract class Role0 {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    protected abstract class Role0a extends Role0 {}\n" +
			    "    protected class Role1 extends Role0a playedBy TA19bp6_1 {\n" +
			    "        print -> print1;\n" +
			    "    }\n" +
			    "    protected class Role2 extends Role0 playedBy TA19bp6_2 {\n" +
			    "        print -> print2;\n" +
			    "    }\n" +
			    "    public <B base Role0> void test(B as Role0 o) {\n" +
			    "        o.print();\n" +
			    "    }\n" +
			    "    public <B base Role0a> void testa(B as Role0a o) {\n" +
			    "        o.print();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA19bp6 t= new TeamA19bp6();\n" +
			    "        t.test(new TA19bp6_1());\n" +
			    "        t.test(new TA19bp6_2a());\n" +
			    "        t.testa(new TA19bp6_1());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp6_2.java",
			    "\n" +
			    "public class TA19bp6_2 {\n" +
			    "    void print2() {\n" +
			    "        System.out.print(\"Base2\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp6_2a.java",
			    "\n" +
			    "public class TA19bp6_2a extends TA19bp6_2 {\n" +
			    "    void print2() {\n" +
			    "        System.out.print(\"Base2a\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp6_1.java",
			    "\n" +
			    "public class TA19bp6_1 {\n" +
			    "    void print1() {\n" +
			    "        System.out.print(\"Base1\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "Base1Base2aBase1");
    }

    // a method has a type parameter constrained by "base" , using the type variable
    // A.1.9-otjld-basetype-parameter-7
    public void testA19_basetypeParameter7() {
       
       runConformTest(
            new String[] {
		"TeamA19bp7.java",
			    "\n" +
			    "public team class TeamA19bp7 {\n" +
			    "    protected abstract class Role0 {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    protected class Role1 extends Role0 playedBy TA19bp7_1 {\n" +
			    "        print -> print1;\n" +
			    "        public String toString() { return \":Role1\"; }\n" +
			    "    }\n" +
			    "    protected class Role2 extends Role0 playedBy TA19bp7_2 {\n" +
			    "        print -> print2;\n" +
			    "        public String toString() { return \":Role2\"; }\n" +
			    "    }\n" +
			    "    public <B base Role0> void test(B as Role0 o) {\n" +
			    "        o.print();\n" +
			    "        System.out.print(o);\n" +
			    "        B b = o;            // <- here: lowering to a statically unknown type\n" +
			    "        System.out.print(b);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA19bp7 t= new TeamA19bp7();\n" +
			    "        t.test(new TA19bp7_1());\n" +
			    "        System.out.print(\"-\");\n" +
			    "        t.test(new TA19bp7_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp7_1.java",
			    "\n" +
			    "public class TA19bp7_1 {\n" +
			    "    void print1() {\n" +
			    "        System.out.print(\"base1\");\n" +
			    "    }\n" +
			    "    public String toString() {\n" +
			    "        return \":Base1\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp7_2.java",
			    "\n" +
			    "public class TA19bp7_2 {\n" +
			    "    void print2() {\n" +
			    "        System.out.print(\"base2\");\n" +
			    "    }\n" +
			    "    public String toString() {\n" +
			    "        return \":Base2\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "base1:Role1:Base1-base2:Role2:Base2");
    }

    // a method has a type parameter constrained by "base" - deferred lifting
    // A.1.9-otjld-basetype-parameter-8
    public void testA19_basetypeParameter8() {
       
       runConformTest(
            new String[] {
		"TeamA19bp8.java",
			    "\n" +
			    "public team class TeamA19bp8 {\n" +
			    "    protected abstract class Role0 {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    protected class Role1 extends Role0 playedBy TA19bp8_1 {\n" +
			    "        print -> print1;\n" +
			    "    }\n" +
			    "    protected class Role2 extends Role0 playedBy TA19bp8_2 {\n" +
			    "        print -> print2;\n" +
			    "    }\n" +
			    "    public <B base Role0> void guarded(B b) { // <- accepting base without lifting\n" +
			    "        if (b != null)\n" +
			    "            test(b); // <- passing object of statically unknown type B\n" +
			    "    }\n" +
			    "    public <B base Role0> void test(B as Role0 o) {\n" +
			    "        o.print();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA19bp8 t= new TeamA19bp8();\n" +
			    "        t.guarded(new TA19bp8_1());\n" +
			    "        t.guarded((TA19bp8_1)null); // <- not invoking test() here\n" +
			    "        t.guarded(new TA19bp8_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp8_1.java",
			    "\n" +
			    "public class TA19bp8_1 {\n" +
			    "    void print1() {\n" +
			    "        System.out.print(\"Base1\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp8_2.java",
			    "\n" +
			    "public class TA19bp8_2 {\n" +
			    "    void print2() {\n" +
			    "        System.out.print(\"Base2\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "Base1Base2");
    }

    // a method has a type parameter constrained by "base" - team inheritance involved
    // A.1.9-otjld-basetype-parameter-9
    public void testA19_basetypeParameter9() {
       
       runConformTest(
            new String[] {
		"TeamA19bp9_2.java",
			    "\n" +
			    "public team class TeamA19bp9_2 extends TeamA19bp9_1 {\n" +
			    "    protected class R2 extends R playedBy TA19bp9 {\n" +
			    "        perform -> test;\n" +
			    "        protected void confirm() {\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA19bp9_2 t = new TeamA19bp9_2();\n" +
			    "        TA19bp9 b = new TA19bp9();\n" +
			    "        t.test(b);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA19bp9_1.java",
			    "\n" +
			    "public team class TeamA19bp9_1 {\n" +
			    "    abstract protected class R {\n" +
			    "        protected abstract void perform();\n" +
			    "        protected abstract void confirm();\n" +
			    "    }\n" +
			    "    public <B base R> void test(B as R o) {\n" +
			    "        o.perform();\n" +
			    "        o.confirm();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp9.java",
			    "\n" +
			    "public class TA19bp9 {\n" +
			    "    public void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a method has two type parameters constrained by "base"
    // A.1.9-otjld-basetype-parameter-10
    public void testA19_basetypeParameter10() {
       
       runConformTest(
            new String[] {
		"TeamA19bp10.java",
			    "\n" +
			    "public team class TeamA19bp10 {\n" +
			    "    protected abstract class Role0 {\n" +
			    "        protected abstract void print();\n" +
			    "    }\n" +
			    "    protected class Role1 extends Role0 playedBy TA19bp10_1 {\n" +
			    "        print -> print1;\n" +
			    "    }\n" +
			    "    protected class Role2 extends Role0 playedBy TA19bp10_2 {\n" +
			    "        print -> print2;\n" +
			    "    }\n" +
			    "    public <B1 base Role0, B2 base Role0> void test(B1 as Role0 o1, B2 as Role0 o2) {\n" +
			    "        o1.print();\n" +
			    "        o2.print();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA19bp10 t= new TeamA19bp10();\n" +
			    "        t.test(new TA19bp10_1(), new TA19bp10_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp10_1.java",
			    "\n" +
			    "public class TA19bp10_1 {\n" +
			    "    void print1() {\n" +
			    "        System.out.print(\"Base1\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA19bp10_2.java",
			    "\n" +
			    "public class TA19bp10_2 {\n" +
			    "    void print2() {\n" +
			    "        System.out.print(\"Base2\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "Base1Base2");
    }

    // an implicitly inherited method becomes invalid because its explicit super is implicitly redefined with covariant return type
    // A.1.10-otjld-covariance-and-implicit-inheritance-1
    public void testA110_covarianceAndImplicitInheritance1() {
        runNegativeTestMatching(
            new String[] {
		"TeamA110caii1_2.java",
			    "\n" +
			    "public team class TeamA110caii1_2 extends TeamA110caii1_1 {\n" +
			    "  protected class R0 {\n" +
			    "    @Override\n" +
			    "    String getO() {\n" +
			    "      return \"OK\";\n" +
			    "    }\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TeamA110caii1_1.java",
			    "\n" +
			    "public team class TeamA110caii1_1 {\n" +
			    "  protected abstract class R0 {\n" +
			    "    abstract Object getO();\n" +
			    "  }\n" +
			    "  protected class R1 extends R0 {\n" +
			    "    Object getO() {\n" +
			    "      return new Object();\n" +
			    "    }\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n"
            },
            "1.3.1(k)");
    }

    // methods are consistently redefined with covariant return type
    // A.1.10-otjld-covariance-and-implicit-inheritance-2
    public void testA110_covarianceAndImplicitInheritance2() {
       
       runConformTest(
            new String[] {
		"TeamA110caii2_2.java",
			    "\n" +
			    "public team class TeamA110caii2_2 extends TeamA110caii2_1 {\n" +
			    "  protected class R0 {\n" +
			    "    protected String getO() {\n" +
			    "      return \"OK\";\n" +
			    "    }\n" +
			    "  }\n" +
			    "  protected class R1 {\n" +
			    "    protected String getO() {\n" +
			    "      return super.getO();\n" +
			    "    }\n" +
			    "  }\n" +
			    "  void test() {\n" +
			    "    R0 r = new R1();\n" +
			    "    String s = r.getO();\n" +
			    "    System.out.print(s);\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    new TeamA110caii2_2().test();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TeamA110caii2_1.java",
			    "\n" +
			    "public team class TeamA110caii2_1 {\n" +
			    "  protected class R0 {\n" +
			    "    Object getO() {\n" +
			    "      return new Object();\n" +
			    "    }\n" +
			    "  }\n" +
			    "  protected class R1 extends R0 {\n" +
			    "    Object getO() {\n" +
			    "      return super.getO();\n" +
			    "    }\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // a callin method is covariantly overridden
    // A.1.11-otjld-covariance-1
    public void testA111_covariance1() {
       
       runConformTest(
            new String[] {
		"TeamA111c1.java",
			    "\n" +
			    "public team class TeamA111c1 {\n" +
			    "    protected class R0 {\n" +
			    "        callin Object ci() {\n" +
			    "            return base.ci();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected class R1 extends R0 playedBy TA111c1 {\n" +
			    "        callin String ci() {\n" +
			    "            return base.ci()+\"K\";\n" +
			    "        }\n" +
			    "        ci <- replace foo;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA111c1().activate();\n" +
			    "        System.out.print(new TA111c1().foo());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA111c1.java",
			    "\n" +
			    "public class TA111c1 {\n" +
			    "    String foo() { return \"O\"; }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a team has a type parameter
    // A.1.12-otjld-generic-team-1
    public void testA112_genericTeam1() {
       
       runConformTest(
            new String[] {
		"TeamA112gt1.java",
			    "\n" +
			    "public team class TeamA112gt1<E> {\n" +
			    "    public void test(E e) {\n" +
			    "        System.out.print(e);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA112gt1<String> t = new TeamA112gt1<String>();\n" +
			    "        t.test(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }
    // a team has a type parameter that is used in a static role method
    public void testA112_genericTeam2() {
       
       runConformTest(
            new String[] {
		"TeamA112gt2.java",
			    "\n" +
			    "public team class TeamA112gt2<E> {\n" +
			    "    protected class R {\n" +
			    "        protected static void test(E e) {\n" +
			    "            System.out.print(e);\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test(E e) {\n" +
			    "        R.test(e);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        TeamA112gt2<String> t = new TeamA112gt2<String>();\n" +
			    "        t.test(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }
    
    // Bug 332795 - [compiler][generics] import issues with inherited playedBy binding inside parameterized team
    // Bug 335774 - [compiler] don't flag "bindingconventions" against import in superteam
    public void testA112_genericTeam3() {
    	runTestExpectingWarnings(
    		new String[] {
    	"TeamA112gt3_2.java",
    			"public team class TeamA112gt3_2 extends TeamA112gt3_1<String> {\n" +
    			"    @Override protected class R {}\n" +
    			"    void test() {\n" +
    			"		 new R().print();\n" +
    			"    }\n" +
    			"    public static void main(String... args) {\n" +
    			"        new TeamA112gt3_2().test();\n" +
    			"    }\n" +
    			"}\n",
    	"TeamA112gt3_1.java",
    			"import mypack.B1;\n" +
    			"public team class TeamA112gt3_1<U> {\n" +
    			"	protected class R playedBy B1 {\n" +
    			"        protected R () { base(); }\n" +
    			"        protected void print() { System.out.print(\"OK\"); }\n" +
    			"   }\n" +
    			"}\n",
    	"mypack/B1.java",
    			"package mypack;\n" +
    			"public class B1 {}\n"
    		},
    		"----------\n" + 
    		"1. WARNING in TeamA112gt3_1.java (at line 3)\n" + 
    		"	protected class R playedBy B1 {\n" + 
    		"	                           ^^\n" + 
    		"It is recommended that base class B1 be imported with the modifier \"base\" (OTJLD 2.1.2(d)).\n" + 
    		"----------\n",
    		"OK");
    }
    
    // Bug 394263 - Team with generic type parameter causes role inheritance error 
    public void testA112_genericTeam4() {
    	runConformTest(
    		new String[] {
    	"MyTeam.java",
    			"public team class MyTeam<T> {	\n" + 
    			"	protected class MyRole {}	\n" + 
    			"	protected class MySubRole extends MyRole {}\n" + 
    			"}"
    		}
    	);
    }

    // a static method in a role file suppresses an nls warning
    // Bug 321352 -  [compiler][reconciler] reporting of non-externalized string constants in role files 
    public void testA113_suppressWarnings1() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
    	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
        runConformTest(
            new String[] {
		"TeamA113sw1.java",
			    "\n" +
			    "public team class TeamA113sw1 {}\n" +
			    "    \n",
		"TeamA113sw1/R.java",
			    "\n" +
			    "team package TeamA113sw1;\n" +
			    "protected class R {\n" +
			    "    @SuppressWarnings(\"nls\")\n" +
			    "    public static void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "",
            null/*classLibs*/,
            true/*shouldFlush*/,
            null/*vmArgs*/,
            customOptions,
            null/*requestor*/);
    }

    // a static method in a role file reports an nls warning
    // Bug 321352 -  [compiler][reconciler] reporting of non-externalized string constants in role files 
    public void testA113_suppressWarnings1w() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
        runNegativeTest(
            new String[] {
		"TeamA113sw1.java",
			    "\n" +
			    "public team class TeamA113sw1 {}\n" +
			    "    \n",
		"TeamA113sw1/R.java",
			    "\n" +
			    "team package TeamA113sw1;\n" +
			    "protected class R {\n" +
			    "    public static void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
            "1. ERROR in TeamA113sw1\\R.java (at line 5)\n" +
            "	System.out.print(\"OK\");\n" +
            "	                 ^^^^\n" +
            "Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
            "----------\n",
            null/*classLibs*/,
            true/*shouldFlush*/,
            customOptions,
            null/*requestor*/);
    }

    // a static method in a role file suppresses an nls warning
    // A.1.13-otjld-suppress-warnings-1
    public void testA113_suppressWarnings2() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
        runConformTest(
            new String[] {
		"TeamA113sw1.java",
			    "\n" +
			    "public team class TeamA113sw1 {}\n" +
			    "    \n",
		"TeamA113sw1/R.java",
			    "\n" +
			    "team package TeamA113sw1;\n" +
			    "protected class R {\n" +
			    "    public static void test() {\n" +
			    "        System.out.print(\"OK\"); //$NON-NLS-1$\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "",
            null/*classLibs*/,
            true/*shouldFlush*/,
            null/*vmArgs*/,
            customOptions,
            null/*requestor*/);
    }

    // A role class is generic - ICE reported by Ivica Loncar
    // A.1.14-otjld-generic-role-1
    public void testA114_genericRole1() {
       
       runConformTest(
            new String[] {
		"TeamA114gr1.java",
			    "\n" +
			    "public team class TeamA114gr1 {\n" +
			    "    protected class R<T> {\n" +
			    "      private T val;\n" +
			    "      protected R(T v) { this.val = v; }\n" +
			    "      protected void print() {\n" +
			    "	System.out.print(this.val);\n" +
			    "      }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "      new R<String>(\"OK\").print();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "      new TeamA114gr1().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // A bound role class is generic - explicitly instantiated
    // A.1.14-otjld-generic-role-2
    public void testA114_genericRole2() {
        runNegativeTestMatching(
            new String[] {
		"TeamA114gr2.java",
			    "\n" +
			    "public team class TeamA114gr2 {\n" +
			    "    protected class R<T> playedBy TA114gr2 {\n" +
			    "      private T val;\n" +
			    "      protected R(T v) {\n" +
			    "	base();\n" +
			    "	this.val = v; \n" +
			    "      }\n" +
			    "      protected void print() -> void print(String v)\n" +
			    "	with { this.val.toString() -> v }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "      new R<String>(\"OK\").print();\n" +
			    "    }\n" +
			    "}\n" +
			    "  \n",
		"TA114gr2.java",
			    "\n" +
			    "public class TA114gr2 {\n" +
			    "  void print(String v) { System.out.print(v); }\n" +
			    "}\n" +
			    "  \n"
            },
            "2.1.2(e)");
    }

    // A bound role class is generic - created using declared lifting
    // A.1.14-otjld_illegal_generic-role-3
    public void _illegal_testA114_genericRole3() {
       
       runConformTest(
            new String[] {
		"TeamA114gr3.java",
			    "\n" +
			    "public team class TeamA114gr3 {\n" +
			    "    protected class R<T> playedBy TA114gr3 {\n" +
			    "      void print(T o) -> void print();\n" +
			    "    }\n" +
			    "    void test(TA114gr3 as R<String> r) {\n" +
			    "      r.print(\"?\");\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "      new TeamA114gr3().test(new TA114gr3(\"OK\"));\n" +
			    "    }\n" +
			    "}\n" +
			    "  \n",
		"TA114gr3.java",
			    "\n" +
			    "public class TA114gr3 {\n" +
			    "  private String val;\n" +
			    "  public TA114gr3(String v) { this.val = v; }\n" +
			    "  void print() { System.out.print(this.val); }\n" +
			    "}\n" +
			    "  \n"
            },
            "OK");
    }

    // a generic role is a parameter in a replace callin - modeled after a situation in OrderSystemPersistenceTeam
    // A.1.14-otjld-generic-role-4
    public void testA114_genericRole4() {
       
       runConformTest(
            new String[] {
		"TeamA114gr4.java",
			    "\n" +
			    "public team class TeamA114gr4 {\n" +
			    "    protected team class R playedBy TA114gr4 {\n" +
			    "        abstract protected class G<E> {\n" +
			    "            abstract protected void test(E e);\n" +
			    "        }\n" +
			    "        protected class C extends G<String> {\n" +
			    "            protected void test(String e) { System.out.print(e); }\n" +
			    "        }\n" +
			    "        protected C getC() {\n" +
			    "            return new C();\n" +
			    "        }\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin <U> void process(G<U> g, U e) {\n" +
			    "            g.test(e);\n" +
			    "        }\n" +
			    "        void process(G<String> g, String e) <- replace void empty()\n" +
			    "            with { g <- getC(), e <- \"OK\" }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA114gr4().activate();\n" +
			    "        new TA114gr4().empty();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA114gr4.java",
			    "\n" +
			    "public class TA114gr4 {\n" +
			    "    void test() { System.out.print(\"OK\"); }\n" +
			    "    void empty() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a generic role is a parameter in a replace callin - modeled after a situation in OrderSystemPersistenceTeam - more difficult type inference
    // A.1.14-otjld-generic-role-5
    public void testA114_genericRole5() {
       
       runConformTest(
            new String[] {
		"TeamA114gr5.java",
			    "\n" +
			    "public team class TeamA114gr5 {\n" +
			    "    protected team class R playedBy TA114gr5 {\n" +
			    "        abstract protected class G<E> {\n" +
			    "            abstract protected void test(E e);\n" +
			    "        }\n" +
			    "        protected class C extends G<String> {\n" +
			    "            protected void test(String e) { System.out.print(e); }\n" +
			    "        }\n" +
			    "        protected C getC() {\n" +
			    "            return new C();\n" +
			    "        }\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin <U> void process(G<U> g, String desc) {\n" +
			    "            U val = getX(desc);\n" +
			    "            g.test(val);\n" +
			    "        }\n" +
			    "        void process(G<String> g, String desc) <- replace void empty()\n" +
			    "            with { g <- getC(), desc <- \"OK\" }\n" +
			    "\n" +
			    "        @SuppressWarnings(\"unchecked\")\n" +
			    "        <X> X getX(String desc) {\n" +
			    "            return (X)desc;\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA114gr5().activate();\n" +
			    "        new TA114gr5().empty();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA114gr5.java",
			    "\n" +
			    "public class TA114gr5 {\n" +
			    "    void empty() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a generic role is a parameter in a replace callin - modeled after a situation in OrderSystemPersistenceTeam - more difficult type inference
    // A.1.14-otjld-generic-role-6
    public void testA114_genericRole6() {
       
       runConformTest(
            new String[] {
		"TeamA114gr6.java",
			    "\n" +
			    "import java.util.Collection;\n" +
			    "import java.util.ArrayList;\n" +
			    "public team class TeamA114gr6 {\n" +
			    "    protected team class R playedBy TA114gr6 {\n" +
			    "        abstract protected class G<E> {\n" +
			    "            abstract protected Collection<E> test();\n" +
			    "        }\n" +
			    "        protected class C extends G<String> {\n" +
			    "            protected Collection<String> test() {\n" +
			    "                ArrayList<String> result = new ArrayList<String>();\n" +
			    "                result.add(\"OK\");\n" +
			    "                return result;\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected C getC() {\n" +
			    "            return new C();\n" +
			    "        }\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin <U> void process(G<U> g) {\n" +
			    "            Collection<U> elements = g.test();\n" +
			    "            for (U elem : elements)\n" +
			    "                System.out.print(elem);\n" +
			    "        }\n" +
			    "        void process(G<String> g) <- replace void empty()\n" +
			    "            with { g <- getC() }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA114gr6().activate();\n" +
			    "        new TA114gr6().empty();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA114gr6.java",
			    "\n" +
			    "public class TA114gr6 {\n" +
			    "    void empty() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }
    
    // Bug 332801 - [compiler][generics] instantiating a generic role cannot be type-checked
    public void testA114_genericRole7() {
    	runConformTest(
    		new String[] {
    	"Canonicalization.java",
    			"public team class Canonicalization {\n" + 
    			"  protected class Cache<K,V> {\n" +
    			"      protected V getV(K k, V v1, V v2) { return v1; }\n" +
    			"  }\n" + 
    			"  Cache<Integer,String> stringCache = new Cache<Integer, String>();\n" +
    			"  void test() {\n" +
    			"      System.out.print(stringCache.getV(2, \"OK\", \"NOTOK\"));\n" +
    			"  }\n" + 
    			"  public static void main(String... args) {\n" +
    			"     new Canonicalization().test();\n" +
    			"  }\n" + 
    			"}\n"
    		},
    		"OK");
    }

    // a decapsulating expression in a base guard requires a generic cast, witness for NPE since 3.4.2
    // A.1.15-otjld-generic-cast-in-baseaccess-1
    public void testA115_genericCastInBaseaccess1() {
       
       runConformTest(
            new String[] {
		"TeamA115gcib1.java",
			    "\n" +
			    "public team class TeamA115gcib1 {\n" +
			    "  @SuppressWarnings(\"decapsulation\")\n" +
			    "  protected class R playedBy TA115gcib1 \n" +
			    "    base when (base.strings.get(0).toLowerCase().equals(\"thisone\"))\n" +
			    "  {\n" +
			    "    void ok() { System.out.print(\"OK\"); }\n" +
			    "    ok <- after test;\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    TeamA115gcib1 t = new TeamA115gcib1();\n" +
			    "    t.activate();\n" +
			    "    new TA115gcib1(\"ThisOneNot\").test();\n" +
			    "    new TA115gcib1(\"ThisOne\").test();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA115gcib1.java",
			    "\n" +
			    "import java.util.ArrayList;\n" +
			    "public class TA115gcib1 {\n" +
			    "  private ArrayList<String> strings = new ArrayList<String>();\n" +
			    "  public TA115gcib1(String val) {\n" +
			    "    strings.add(val);\n" +
			    "  }\n" +
			    "  void test() { System.out.print(\"test:\"); }\n" +
			    "}\n" +
			    "  \n"
            },
            "test:test:OK");
    }

    // a decapsulating expression in a base guard requires a generic cast, witness for NPE since 3.4.2
    // A.1.15-otjld-generic-cast-in-baseaccess-2
    public void testA115_genericCastInBaseaccess2() {
       
       runConformTest(
            new String[] {
		"TeamA115gcib2.java",
			    "\n" +
			    "public team class TeamA115gcib2 {\n" +
			    "  @SuppressWarnings(\"decapsulation\")\n" +
			    "  protected class R playedBy TA115gcib2 \n" +
			    "    base when (base.cont.element.toLowerCase().equals(\"thisone\"))\n" +
			    "  {\n" +
			    "    void ok() { System.out.print(\"OK\"); }\n" +
			    "    ok <- after test;\n" +
			    "  }\n" +
			    "  public static void main(String[] args) {\n" +
			    "    TeamA115gcib2 t = new TeamA115gcib2();\n" +
			    "    t.activate();\n" +
			    "    new TA115gcib2(\"ThisOneNot\").test();\n" +
			    "    new TA115gcib2(\"ThisOne\").test();\n" +
			    "  }\n" +
			    "}\n" +
			    "  \n",
		"TA115gcib2Container.java",
			    "\n" +
			    "public class TA115gcib2Container<T> {\n" +
			    "  public T element;\n" +
			    "}\n" +
			    "  \n",
		"TA115gcib2.java",
			    "\n" +
			    "public class TA115gcib2 {\n" +
			    "  private TA115gcib2Container<String> cont = new TA115gcib2Container<String>();\n" +
			    "  public TA115gcib2(String val) {\n" +
			    "    this.cont.element = val;\n" +
			    "  }\n" +
			    "  void test() { System.out.print(\"test:\"); }\n" +
			    "}\n" +
			    "  \n"
            },
            "test:test:OK");
    }

    // an overriding role has an override annotation
    // A.1.16-otjld-override-annotation-1
    public void testA116_overrideAnnotation1() {
       
       runConformTest(
            new String[] {
		"TeamA116on1_2.java",
			    "\n" +
			    "public team class TeamA116on1_2 extends TeamA116on1_1 {\n" +
			    "    @Override protected class R {\n" +
			    "        @Override protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    TeamA116on1_2 () {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA116on1_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA116on1_1.java",
			    "\n" +
			    "public team class TeamA116on1_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"Nope\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role has an override annotation - not overriding
    // A.1.16-otjld-override-annotation-2
    public void testA116_overrideAnnotation2() {
        runNegativeTestMatching(
            new String[] {
		"TeamA116on2.java",
			    "\n" +
			    "public team class TeamA116on2 {\n" +
			    "    @Override\n" +
			    "    protected class R {\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "1.3.1(c)");
    }

    // an overriding role has no override annotation
    // A.1.16-otjld-override-annotation-3
    public void testA116_overrideAnnotation3() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.WARNING);
       
       runConformTest(
            new String[] {
		"TeamA116on3_2.java",
			    "\n" +
			    "public team class TeamA116on3_2 extends TeamA116on3_1 {\n" +
			    "    protected class R {\n" +
			    "        @Override protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    TeamA116on3_2 () {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA116on3_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA116on3_1.java",
			    "\n" +
			    "public team class TeamA116on3_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"Nope\");\n" +
			    "        }\n" +
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

    // Bug 381790 - [compiler] support @Override for role method implementing an interface method
    // With @Override Annotations
    // R: role class implementing regular interface 
    // R1: role class extending role class
    // R2: role extending regular class
    public void testA116_overrideAnnotation4() {
    	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
    	String[] sources = new String [] {
    		"TeamA117oi4.java",
    		"public team class TeamA117oi4 {\n" +
    		"	protected class R implements IA117oi4 {\n" +
    		"		@Override public void bar() {}\n" +
    		"	}\n" +
    		"	protected class R1 extends R {\n" +
    		"		@Override public void bar() {}\n" +
    		"	}\n" +
			"	protected class R2 extends TA117oi4 {\n" +
			"		@Override public void foo() {}\n" +
			"	}\n" +
    		"}\n",
			"TA117oi4.java",
			"public class TA117oi4 {\n" +
			"	public void foo() {}\n" +
			"}\n",
    		"IA117oi4.java",
    		"public interface IA117oi4 {\n" +
    		"	public void bar();\n" +
    		"}\n"
    	};
    	if (this.complianceLevel == ClassFileConstants.JDK1_5)
    		runNegativeTest(sources,
				"----------\n" + 
				"1. ERROR in TeamA117oi4.java (at line 3)\n" + 
				"	@Override public void bar() {}\n" + 
				"	                      ^^^^^\n" + 
				"The method bar() of type TeamA117oi4.R must override a superclass method\n" + 
				"----------\n");
    	else
    		runConformTest(sources, "");
    }

    // Bug 381790 - [compiler] support @Override for role method implementing an interface method
    // Missing @Override Annotations:
    // R: role implements regular interface
    // R1: role extends other role
    // R2: role extending regular class
    public void testA116_overrideAnnotation5() {
    	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
    	Map options = getCompilerOptions();
    	options.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.ERROR);
    	String[] sources = new String [] {
			"TeamA117oi5.java",
			"public team class TeamA117oi5 {\n" +
			"	protected class R implements IA117oi5 {\n" +
			"		public void bar() {}\n" +
			"	}\n" +
			"	protected class R1 extends R {\n" +
			"		public void bar() {}\n" +
			"	}\n" +
			"	protected class R2 extends TA117oi5 {\n" +
			"		public void foo() {}\n" +
			"	}\n" +
			"}\n",
			"TA117oi5.java",
			"public class TA117oi5 {\n" +
			"	public void foo() {}\n" +
			"}\n",
			"IA117oi5.java",
			"public interface IA117oi5 {\n" +
			"	public void bar();\n" +
			"}\n"
		};
    	if (this.complianceLevel == ClassFileConstants.JDK1_5)
			runNegativeTest(
	    		sources,
	    		"----------\n" + 
				"1. ERROR in TeamA117oi5.java (at line 6)\n" + 
				"	public void bar() {}\n" + 
				"	            ^^^^^\n" + 
				"The method bar() of type TeamA117oi5.R1 should be tagged with @Override since it actually overrides a superclass method\n" + 
				"----------\n" + 
				"2. ERROR in TeamA117oi5.java (at line 9)\n" + 
				"	public void foo() {}\n" + 
				"	            ^^^^^\n" + 
				"The method foo() of type TeamA117oi5.R2 should be tagged with @Override since it actually overrides a superclass method\n" + 
				"----------\n",
				null,
				true,
				options);
    	else
			runNegativeTest(
	    		sources,
				"----------\n" + 
				"1. ERROR in TeamA117oi5.java (at line 3)\n" + 
				"	public void bar() {}\n" + 
				"	            ^^^^^\n" + 
				"The method bar() of type TeamA117oi5.R should be tagged with @Override since it actually overrides a superinterface method\n" + 
				"----------\n" + 
				"2. ERROR in TeamA117oi5.java (at line 6)\n" + 
				"	public void bar() {}\n" + 
				"	            ^^^^^\n" + 
				"The method bar() of type TeamA117oi5.R1 should be tagged with @Override since it actually overrides a superclass method\n" + 
				"----------\n" + 
				"3. ERROR in TeamA117oi5.java (at line 9)\n" + 
				"	public void foo() {}\n" + 
				"	            ^^^^^\n" + 
				"The method foo() of type TeamA117oi5.R2 should be tagged with @Override since it actually overrides a superclass method\n" + 
				"----------\n",
				null,
				true,
				options);
    }

    // a role method is deprecated, so should be its tsub
    // A.1.17-otjld-copyinheritance-for-annotation-1
    public void testA117_copyinheritanceForAnnotation1() {
    	runTestExpectingWarnings(
			new String[] {
				"TeamA117cfa1_1.java",
				    "\n" +
				    "public team class TeamA117cfa1_1 {\n" +
				    "    protected class R {\n" +
				    "        @Deprecated\n" +
				    "        void dep() { }\n" +
				    "    }\n" +
				    "}\n" +
				    "    \n",
			    "TeamA117cfa1_2.java",
				    "\n" +
				    "public team class TeamA117cfa1_2 extends TeamA117cfa1_1 {\n" +
				    "    protected class R {\n" +
				    "        void test() {\n" +
				    "            dep();\n" +
				    "        }\n" +
				    "    }\n" +
				    "}\n" +
				    "    \n"
			},
			"----------\n" + 
			"1. WARNING in TeamA117cfa1_2.java (at line 3)\n" + 
			"	protected class R {\n" + 
			"	                ^\n" + 
			"Role TeamA117cfa1_2.R should be tagged with @Override since it actually overrides a superteam role (OTJLD 1.3.1(c)).\n" + 
			"----------\n" + 
			"2. WARNING in TeamA117cfa1_2.java (at line 5)\n" + 
			"	dep();\n" + 
			"	^^^^^\n" + 
			"The method dep() from the type TeamA117cfa1_1.R is deprecated\n" + 
			"----------\n");
    }

    // a role method is deprecated, so should be its tsub  -  testing at runtime via reflection
    // A.1.17-otjld-copyinheritance-for-annotation-2
    public void _testA117_copyinheritanceForAnnotation2() {
       
       runConformTest(
            new String[] {
		"TeamA117cfa2_2.java",
			    "\n" +
			    "import java.lang.reflect.*;\n" +
			    "import java.lang.annotation.Annotation;\n" +
			    "public team class TeamA117cfa2_2 extends TeamA117cfa2_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() throws NoSuchMethodException {\n" +
			    "            Method m = this.getClass().getMethod(\"dep\", new Class<?>[0]);\n" +
			    "            Annotation[] annots = m.getDeclaredAnnotations();\n" +
			    "            if (annots != null)\n" +
			    "                for (Annotation annot : annots)\n" +
			    "                    System.out.print(annot.toString());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() throws NoSuchMethodException {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) throws NoSuchMethodException {\n" +
			    "        new TeamA117cfa2_2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA117cfa2_1.java",
			    "\n" +
			    "public team class TeamA117cfa2_1 {\n" +
			    "    protected class R {\n" +
			    "        @Deprecated\n" +
			    "        void dep() { }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "@java.lang.Deprecated()");
    }

    // a role method has a custom annotation, so should its tsub  -  testing at runtime via reflection
    // A.1.17-otjld-copyinheritance-for-annotation-3
    public void _testA117_copyinheritanceForAnnotation3() {
       
       runConformTest(
            new String[] {
		"TeamA117cfa3_2.java",
			    "\n" +
			    "import java.lang.reflect.*;\n" +
			    "import java.lang.annotation.Annotation;\n" +
			    "public team class TeamA117cfa3_2 extends TeamA117cfa3_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() throws NoSuchMethodException {\n" +
			    "            Method m = this.getClass().getMethod(\"ann\", new Class<?>[0]);\n" +
			    "            Annotation[] annots = m.getDeclaredAnnotations();\n" +
			    "            if (annots != null)\n" +
			    "                for (Annotation annot : annots)\n" +
			    "                    System.out.print(annot.toString());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() throws NoSuchMethodException {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) throws NoSuchMethodException {\n" +
			    "        new TeamA117cfa3_2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IA117cfa3.java",
			    "\n" +
			    "import java.lang.annotation.*;\n" +
			    "@Retention(RetentionPolicy.RUNTIME)\n" +
			    "public @interface IA117cfa3 {\n" +
			    "    String[] value();\n" +
			    "}\n" +
			    "    \n",
		"TeamA117cfa3_1.java",
			    "\n" +
			    "public team class TeamA117cfa3_1 {\n" +
			    "    protected class R {\n" +
			    "        @IA117cfa3({\"one\", \"two\"})\n" +
			    "        void ann() { }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "@IA117cfa3(value=[one, two])");
    }

    // a role method has a custom annotation, so should its tsub  -  testing at runtime via reflection
    // A.1.17-otjld-copyinheritance-for-annotation-4
    public void testA117_copyinheritanceForAnnotation4() {
       if (this.weavingScheme == WeavingScheme.OTRE) {
    	   // bytecode at 1.8 contains a method handle, cannot be read by BCEL
    	   System.err.println("BCEL cannot handle this byte code, uses method handles.");
    	   return;
       }
       runConformTest(
            new String[] {
		"TeamA117cfa4_2.java",
			    "\n" +
			    "import java.lang.reflect.*;\n" +
			    "import java.lang.annotation.Annotation;\n" +
			    "public team class TeamA117cfa4_2 extends TeamA117cfa4_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() throws NoSuchMethodException {\n" +
			    "            Method m = this.getClass().getMethod(\"ann\", new Class<?>[0]);\n" +
			    "            Annotation[] annots = m.getDeclaredAnnotations();\n" +
			    "            if (annots != null)\n" +
			    "                for (Annotation annot : annots)\n" +
			    "                    System.out.print(annot.toString());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() throws NoSuchMethodException {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) throws NoSuchMethodException {\n" +
			    "        new TeamA117cfa4_2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IA117cfa4.java",
			    "\n" +
			    "import java.lang.annotation.*;\n" +
			    "@Retention(RetentionPolicy.RUNTIME)\n" +
			    "public @interface IA117cfa4 {\n" +
			    "    String left();\n" +
			    "    String right();\n" +
			    "}\n" +
			    "    \n",
		"TeamA117cfa4_1.java",
			    "\n" +
			    "public team class TeamA117cfa4_1 {\n" +
			    "    protected class R {\n" +
			    "        @IA117cfa4(left=\"one\",right=\"two\")\n" +
			    "        void ann() { }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            isJRE9Plus ? "@IA117cfa4(left=\"one\", right=\"two\")" : "@IA117cfa4(left=one, right=two)");
    }

    // a role method has a nested custom annotation, so should its tsub  -  testing at runtime via reflection
    // A.1.17-otjld-copyinheritance-for-annotation-5
    public void testA117_copyinheritanceForAnnotation5() {
       
       runConformTest(
            new String[] {
		"TeamA117cfa5_2.java",
			    "\n" +
			    "import java.lang.reflect.*;\n" +
			    "import java.lang.annotation.Annotation;\n" +
			    "public team class TeamA117cfa5_2 extends TeamA117cfa5_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() throws NoSuchMethodException {\n" +
			    "            Method m = this.getClass().getMethod(\"ann\", new Class<?>[0]);\n" +
			    "            Annotation[] annots = m.getDeclaredAnnotations();\n" +
			    "            if (annots != null)\n" +
			    "                for (Annotation annot : annots)\n" +
			    "                    System.out.print(annot.toString());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() throws NoSuchMethodException {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    (this.weavingScheme == WeavingScheme.OTRE
			    ? "" // skip execution
			    :
			    "    public static void main(String[] args) throws NoSuchMethodException {\n" +
			    "        new TeamA117cfa5_2().test();\n" +
			    "    }\n"
			    ) +
			    "}\n" +
			    "    \n",
		"TeamA117cfa5_1.java",
			    "\n" +
			    "public team class TeamA117cfa5_1 {\n" +
			    "    protected class R {\n" +
			    "        @IA117cfa5_2(left=\"one\",right=@IA117cfa5_1(\"two\"))\n" +
			    "        void ann() { }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IA117cfa5_1.java",
			    "\n" +
			    "import java.lang.annotation.*;\n" +
			    "@Retention(RetentionPolicy.RUNTIME)\n" +
			    "public @interface IA117cfa5_1 {\n" +
			    "    String value();\n" +
			    "}\n" +
			    "    \n",
		"IA117cfa5_2.java",
			    "\n" +
			    "import java.lang.annotation.*;\n" +
			    "@Retention(RetentionPolicy.RUNTIME)\n" +
			    "public @interface IA117cfa5_2 {\n" +
			    "    String left();\n" +
			    "    IA117cfa5_1 right();\n" +
			    "}\n" +
			    "    \n"
            },
		    (this.weavingScheme == WeavingScheme.OTRE
		    ? ""
		    : (isJRE9Plus ? "@IA117cfa5_2(left=\"one\", right=@IA117cfa5_1(value=\"two\"))": "@IA117cfa5_2(left=one, right=@IA117cfa5_1(value=two))")));
    }

    // a role method has a custom annotation with enum values, annotation was in conflict with implicit activation annotation
    // A.1.17-otjld-copyinheritance-for-annotation-5e
    public void testA117_copyinheritanceForAnnotation5e() {
       
       runConformTest(
            new String[] {
		"TeamA117cfa5e_2.java",
			    "\n" +
			    "import java.lang.reflect.*;\n" +
			    "import java.lang.annotation.Annotation;\n" +
			    "public team class TeamA117cfa5e_2 extends TeamA117cfa5e_1 {\n" +
			    "    public class R {\n" +
			    "        protected void test() throws NoSuchMethodException {\n" +
			    "            Method m = this.getClass().getMethod(\"ann\", new Class<?>[0]);\n" +
			    "            Annotation[] annots = m.getDeclaredAnnotations();\n" +
			    "            if (annots != null)\n" +
			    "                for (Annotation annot : annots)\n" +
			    "                    System.out.print(annot.toString());\n" +
			    "            ann();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() throws NoSuchMethodException {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    (this.weavingScheme == WeavingScheme.OTRE && IS_JRE_8
			    ? "" // skip execution
			    :
			    "    public static void main(String[] args) throws NoSuchMethodException {\n" +
			    "        new TeamA117cfa5e_2().test();\n" +
			    "    }\n"
			    ) +
			    "}\n" +
			    "    \n",
		"EA117cfa5e_2.java",
			    "\n" +
			    "public enum EA117cfa5e_2 {\n" +
			    "    A, B, C\n" +
			    "}\n" +
			    "    \n",
		"TeamA117cfa5e_1.java",
			    "\n" +
			    "public team class TeamA117cfa5e_1 {\n" +
			    "    public class R {\n" +
			    "        @IA117cfa5e(val1=EA117cfa5e_1.TWO, val2=EA117cfa5e_2.C)\n" +
			    "        @org.objectteams.ImplicitTeamActivation\n" +
			    "        public void ann() {\n" +
			    "            System.out.print(TeamA117cfa5e_1.this.isActive() ? \"Active\" : \"Inactive\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IA117cfa5e.java",
			    "\n" +
			    "import java.lang.annotation.*;\n" +
			    "@Retention(RetentionPolicy.RUNTIME)\n" +
			    "public @interface IA117cfa5e {\n" +
			    "    EA117cfa5e_1 val1();\n" +
			    "    EA117cfa5e_2 val2();\n" +
			    "}\n" +
			    "    \n",
		"EA117cfa5e_1.java",
			    "\n" +
			    "public enum EA117cfa5e_1 {\n" +
			    "    ONE, TWO, THREE\n" +
			    "}\n" +
			    "    \n"
            },
		    (this.weavingScheme == WeavingScheme.OTRE && IS_JRE_8
		    ? ""
		    : "@IA117cfa5e(val1=TWO, val2=C)@org.objectteams.ImplicitTeamActivation()Active"));
    }

    // a role field is deprecated, so should be its tsub
    // A.1.17-otjld-copyinheritance-for-annotation-6
    public void testA117_copyinheritanceForAnnotation6() {
        runTestExpectingWarnings(
            new String[] {
		"TeamA117cfa6_1.java",
			    "\n" +
			    "public team class TeamA117cfa6_1 {\n" +
			    "    protected class R {\n" +
			    "        @Deprecated\n" +
			    "        public String dep;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA117cfa6_2.java",
			    "\n" +
			    "public team class TeamA117cfa6_2 extends TeamA117cfa6_1 {\n" +
			    "    @Override\n" +
			    "    public class R  { } // force recompilation if class file exists\n" +
			    "}\n" +
			    "    \n",
		"TA117cfa6Main.java",
			    "\n" +
			    "public class TA117cfa6Main {\n" +
			    "    void test(final TeamA117cfa6_2 t, R<@t> r) {\n" +
			    "        System.out.print(r.dep);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
            "1. WARNING in TA117cfa6Main.java (at line 4)\n"+
            "	System.out.print(r.dep);\n"+
            "	                   ^^^\n"+
            "The field TeamA117cfa6_2.R.dep is deprecated\n"+
            "----------\n");
    }

    // a role field has a custom annotation, so should its tsub  -  testing at runtime via reflection
    // A.1.17-otjld-copyinheritance-for-annotation-7
    public void testA117_copyinheritanceForAnnotation7() {
       
       runConformTest(
            new String[] {
		"TeamA117cfa7_2.java",
			    "\n" +
			    "import java.lang.reflect.*;\n" +
			    "import java.lang.annotation.Annotation;\n" +
			    "public team class TeamA117cfa7_2 extends TeamA117cfa7_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() throws NoSuchFieldException {\n" +
			    "            Field m = this.getClass().getField(\"field\");\n" +
			    "            Annotation[] annots = m.getDeclaredAnnotations();\n" +
			    "            if (annots != null)\n" +
			    "                for (Annotation annot : annots)\n" +
			    "                    System.out.print(annot.toString());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() throws NoSuchFieldException {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    (this.weavingScheme == WeavingScheme.OTRE && IS_JRE_8
			    ? "" // skip execution
			    :
			    "    public static void main(String[] args) throws NoSuchFieldException {\n" +
			    "        new TeamA117cfa7_2().test();\n" +
			    "    }\n"
			    ) +
			    "}\n" +
			    "    \n",
		"IA117cfa7.java",
			    "\n" +
			    "import java.lang.annotation.*;\n" +
			    "@Retention(RetentionPolicy.RUNTIME)\n" +
			    "public @interface IA117cfa7 {\n" +
			    "    String[] value();\n" +
			    "}\n" +
			    "    \n",
		"TeamA117cfa7_1.java",
			    "\n" +
			    "public team class TeamA117cfa7_1 {\n" +
			    "    protected class R {\n" +
			    "        @IA117cfa7({\"one\", \"two\"})\n" +
			    "        public int field;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
		    (this.weavingScheme == WeavingScheme.OTRE && IS_JRE_8
		    ? ""
		    : isJRE9Plus ? "@IA117cfa7(value={\"one\", \"two\"})" : "@IA117cfa7(value=[one, two])"));
    }

    // a role field has a custom annotation (scalar arg), so should its tsub  -  testing at runtime via reflection
    // A.1.17-otjld-copyinheritance-for-annotation-8
    public void testA117_copyinheritanceForAnnotation8() {
       
       runConformTest(
            new String[] {
		"TeamA117cfa8_2.java",
			    "\n" +
			    "import java.lang.reflect.*;\n" +
			    "import java.lang.annotation.Annotation;\n" +
			    "public team class TeamA117cfa8_2 extends TeamA117cfa8_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() throws NoSuchFieldException {\n" +
			    "            Field m = this.getClass().getField(\"field\");\n" +
			    "            Annotation[] annots = m.getDeclaredAnnotations();\n" +
			    "            if (annots != null)\n" +
			    "                for (Annotation annot : annots)\n" +
			    "                    System.out.print(annot.toString());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() throws NoSuchFieldException {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    (this.weavingScheme == WeavingScheme.OTRE && IS_JRE_8
			    ? ""
	    		:
			    "    public static void main(String[] args) throws NoSuchFieldException {\n" +
			    "        new TeamA117cfa8_2().test();\n" +
			    "    }\n"
			    ) +
			    "}\n" +
			    "    \n",
		"IA117cfa8.java",
			    "\n" +
			    "import java.lang.annotation.*;\n" +
			    "@Retention(RetentionPolicy.RUNTIME)\n" +
			    "public @interface IA117cfa8 {\n" +
			    "    String value();\n" +
			    "}\n" +
			    "    \n",
		"TeamA117cfa8_1.java",
			    "\n" +
			    "public team class TeamA117cfa8_1 {\n" +
			    "    protected class R {\n" +
			    "        @IA117cfa8(\"val\")\n" +
			    "        public int field;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
		    (this.weavingScheme == WeavingScheme.OTRE && IS_JRE_8
		    ? ""
    		: isJRE9Plus ? "@IA117cfa8(value=\"val\")" : "@IA117cfa8(value=val)"));
    }

    // a role field has a custom annotation (enum typed arg), so should its tsub  -  testing at runtime via reflection
    // A.1.17-otjld-copyinheritance-for-annotation-8e
    public void testA117_copyinheritanceForAnnotation8e() {
       
       runConformTest(
            new String[] {
		"TeamA117cfa8e_2.java",
			    "\n" +
			    "import java.lang.reflect.*;\n" +
			    "import java.lang.annotation.Annotation;\n" +
			    "public team class TeamA117cfa8e_2 extends TeamA117cfa8e_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() throws NoSuchFieldException {\n" +
			    "            Field m = this.getClass().getField(\"field\");\n" +
			    "            Annotation[] annots = m.getDeclaredAnnotations();\n" +
			    "            if (annots != null)\n" +
			    "                for (Annotation annot : annots)\n" +
			    "                    System.out.print(annot.toString());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() throws NoSuchFieldException {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    (this.weavingScheme == WeavingScheme.OTRE && IS_JRE_8
			    ? ""
	    		:
			    "    public static void main(String[] args) throws NoSuchFieldException {\n" +
			    "        new TeamA117cfa8e_2().test();\n" +
			    "    }\n"
			    ) +
			    "}\n" +
			    "    \n",
		"TeamA117cfa8e_1.java",
			    "\n" +
			    "public team class TeamA117cfa8e_1 {\n" +
			    "    protected class R {\n" +
			    "        @IA117cfa8e(theVal=IA117cfa8e.Value.TWO)\n" +
			    "        public int field;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IA117cfa8e.java",
			    "\n" +
			    "import java.lang.annotation.*;\n" +
			    "@Retention(RetentionPolicy.RUNTIME)\n" +
			    "public @interface IA117cfa8e {\n" +
			    "    public enum Value { ONE, TWO, NONE }\n" +
			    "    Value theVal();\n" +
			    "}\n" +
			    "    \n"
            },
		    (this.weavingScheme == WeavingScheme.OTRE && IS_JRE_8
		    ? ""
		    : "@IA117cfa8e(theVal=TWO)"));
    }

    // a role field has a custom annotation, so should its tsub  -  testing at runtime via reflection
    // A.1.17-otjld-copyinheritance-for-annotation-9
    public void testA117_copyinheritanceForAnnotation9() {
       
       runConformTest(
            new String[] {
		"TeamA117cfa9_2.java",
			    "\n" +
			    "import java.lang.reflect.*;\n" +
			    "import java.lang.annotation.Annotation;\n" +
			    "public team class TeamA117cfa9_2 extends TeamA117cfa9_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() throws NoSuchFieldException {\n" +
			    "            Field m = this.getClass().getField(\"field\");\n" +
			    "            Annotation[] annots = m.getDeclaredAnnotations();\n" +
			    "            if (annots != null)\n" +
			    "                for (Annotation annot : annots)\n" +
			    "                    System.out.print(annot.toString());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() throws NoSuchFieldException {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    (this.weavingScheme == WeavingScheme.OTRE && IS_JRE_8
			    ? ""
	    		:
			    "    public static void main(String[] args) throws NoSuchFieldException {\n" +
			    "        new TeamA117cfa9_2().test();\n" +
			    "    }\n"
			    ) +
			    "}\n" +
			    "    \n",
		"TeamA117cfa9_1.java",
			    "\n" +
			    "public team class TeamA117cfa9_1 {\n" +
			    "    protected class R {\n" +
			    "        @IA117cfa9({1, 2})\n" +
			    "        public int field;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IA117cfa9.java",
			    "\n" +
			    "import java.lang.annotation.*;\n" +
			    "@Retention(RetentionPolicy.RUNTIME)\n" +
			    "public @interface IA117cfa9 {\n" +
			    "    int[] value();\n" +
			    "}\n" +
			    "    \n"
            },
		    (this.weavingScheme == WeavingScheme.OTRE && IS_JRE_8
		    ? ""
    		: isJRE9Plus ? "@IA117cfa9(value={1, 2})" : "@IA117cfa9(value=[1, 2])"));
    }

    // a role class extends a generic class providing type parameters
    // A.1.18-otjld-role-extends-parameterized-1
    public void testA118_roleExtendsParameterized1() {
       
       runConformTest(
            new String[] {
		"TeamA118rep1.java",
			    "\n" +
			    "import java.util.ArrayList;\n" +
			    "public team class TeamA118rep1 {\n" +
			    "    @SuppressWarnings(\"serial\")\n" +
			    "    protected class RList extends ArrayList<String> {\n" +
			    "        protected void test() {\n" +
			    "            add(\"OK\");\n" +
			    "            System.out.print(get(0));\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        RList rlist = new RList();\n" +
			    "        rlist.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA118rep1().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class extends a generic role providing type parameters
    // A.1.18-otjld-role-extends-parameterized-2
    public void testA118_roleExtendsParameterized2() {
       
       runConformTest(
            new String[] {
		"TeamA118rep2.java",
			    "\n" +
			    "public team class TeamA118rep2 {\n" +
			    "    abstract protected class RGeneric<E> {\n" +
			    "        abstract protected E getVal();\n" +
			    "    }\n" +
			    "    protected class RConcrete extends RGeneric<String> {\n" +
			    "        protected String getVal() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        RGeneric<String> r = new RConcrete();\n" +
			    "        System.out.print(r.getVal());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA118rep2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role class extends a generic regular class with type bounds
    // A.1.18-otjld-role-extends-parameterized-3
    public void testA118_roleExtendsParameterized3() {
       
       runConformTest(
            new String[] {
		"TeamA118rep3.java",
			    "\n" +
			    "public team class TeamA118rep3 {\n" +
			    "    public class R1 extends TA118rep3<R1,R2> {\n" +
			    "    }\n" +
			    "    public class R2 extends TA118rep3<R2,R1> {\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        R1 r1 = new R1();\n" +
			    "        R2 r2 = new R2();\n" +
			    "        r1.add(r2);\n" +
			    "        System.out.print(r2.size());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new TeamA118rep3().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA118rep3.java",
			    "\n" +
			    "import java.util.HashSet;\n" +
			    "public class TA118rep3 <U extends TA118rep3<U,V>, V extends TA118rep3<V,U>> {\n" +
			    "    HashSet<V> others = new HashSet<V>();\n" +
			    "    public void add(V other) {\n" +
			    "        others.add(other);\n" +
			    "        other.others.add((U)this);\n" +
			    "    }\n" +
			    "    public int size() { return others.size(); }\n" +
			    "}\n" +
			    "    \n"
            },
            "1");
    }

    // a type parameter has a nested value parameter
    // A.1.19-otjld-nested-value-parameter-1
    public void testA119_nestedValueParameter1() {
       
       runConformTest(
            new String[] {
		"TeamA119nvp1.java",
			    "\n" +
			    "public team class TeamA119nvp1 {\n" +
			    "    public class Role playedBy TA119nvp1_2 {\n" +
			    "        public Role() { base(); }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA119nvp1 t1 = new TeamA119nvp1();\n" +
			    "        Role<@t1> r = new Role<@t1>();\n" +
			    "        TA119nvp1_1<@t1,Role<@t1>> c = new TA119nvp1_1<@t1,Role<@t1>>();\n" +
			    "        c.test(r);\n" +
			    "        System.out.print(t1.getAllRoles().length);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp1_1.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class TA119nvp1_1<Team t, R<@t>> {\n" +
			    "    public void test(R<@t> r) {\n" +
			    "        t.unregisterRole(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp1_2.java",
			    "\n" +
			    "public class TA119nvp1_2 {}\n" +
			    "    \n"
            },
            "0");
    }

    // a type parameter has a nested value parameter
    // A.1.19-otjld-nested-value-parameter-2
    public void testA119_nestedValueParameter2() {
        runNegativeTest(
            new String[] {
		"TeamA119nvp2.java",
			    "\n" +
			    "public team class TeamA119nvp2 {\n" +
			    "    public class Role playedBy TA119nvp2_2 {\n" +
			    "        public Role() { base(); }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA119nvp2 t1 = new TeamA119nvp2();\n" +
			    "        Role<@t1> r = new Role<@t1>();\n" +
			    "        final TeamA119nvp2 t2 = new TeamA119nvp2();\n" +
			    "        TA119nvp2_1<@t2,Role<@t1>> c = new TA119nvp2_1<@t2,Role<@t1>>();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp2_1.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class TA119nvp2_1<Team t, R<@t>> {\n" +
			    "    public void test(R<@t> r) {\n" +
			    "        t.unregisterRole(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp2_2.java",
			    "\n" +
			    "public class TA119nvp2_2 {}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. WARNING in TeamA119nvp2.java (at line 10)\n" + 
    		"	TA119nvp2_1<@t2,Role<@t1>> c = new TA119nvp2_1<@t2,Role<@t1>>();\n" + 
    		"	^^^^^^^^^^^\n" + 
    		"Using an experimental feature: Implementation for mixed type and value parameters is experimental..\n" + 
    		"----------\n" + 
    		"2. WARNING in TeamA119nvp2.java (at line 10)\n" + 
    		"	TA119nvp2_1<@t2,Role<@t1>> c = new TA119nvp2_1<@t2,Role<@t1>>();\n" + 
    		"	                                   ^^^^^^^^^^^\n" + 
    		"Using an experimental feature: Implementation for mixed type and value parameters is experimental..\n" + 
    		"----------\n");
    }

    // inconsisted usage of value-dependent type variable
    // A.1.19-otjld-nested-value-parameter-3
    public void testA119_nestedValueParameter3() {
        runNegativeTestMatching(
            new String[] {
		"TA119nvp3.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class TA119nvp3<Team t, R<@t>> {\n" +
			    "    public void test(final Team t2, R<@t2> r) {\n" +
			    "        t.unregisterRole(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "9.2.1(a)");
    }

    // a type parameter has a nested value parameter with additional type bound (super role)
    // A.1.19-otjld-nested-value-parameter-4
    public void testA119_nestedValueParameter4() {
       // FIXME(SH): make it work with compilation in one go, too!
       this.compileOrder = new String[][]{{"TA119nvp4_2.java"}, {"TeamA119nvp4.java"}, {"TA119nvp4_1.java"}, {"TA119nvp4Main.java"}};
       runConformTest(
            new String[] {
		"TA119nvp4Main.java",
			    "\n" +
			    "public class TA119nvp4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA119nvp4 t1 = new TeamA119nvp4();\n" +
			    "        Role<@t1> r = new Role<@t1>();\n" +
			    "        TA119nvp4_1<@t1,Role<@t1>> c = new TA119nvp4_1<@t1,Role<@t1>>();\n" +
			    "        c.test(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp4_1.java",
			    "\n" +
			    "public class TA119nvp4_1<TeamA119nvp4 t, R<@t> extends Showable<@t>> {\n" +
			    "    public void test(R<@t> r) {\n" +
			    "        t.show(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp4_2.java",
			    "\n" +
			    "public class TA119nvp4_2 {}\n" +
			    "    \n",
		"TeamA119nvp4.java",
			    "\n" +
			    "public team class TeamA119nvp4 {\n" +
			    "    public abstract class Showable {\n" +
			    "        public abstract void show();\n" +
			    "    }\n" +
			    "    public class Role extends Showable playedBy TA119nvp4_2 {\n" +
			    "        public Role() { base(); }\n" +
			    "		 @Override\n" +
			    "        public void show() { System.out.print(\"OK\"); }\n" +
			    "    }\n" +
			    "    public void show(Showable s) {\n" +
			    "        s.show();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a type parameter has a nested value parameter with additional type bound (super role)
    // like previous but illegally use qualified type reference
    public void testA119_nestedValueParameter4b() {
       this.compileOrder = new String[][]{{"pb/TA119nvp4_2.java"}, {"pt/TeamA119nvp4.java"}, {"pb/TA119nvp4_1.java"}, {"TA119nvp4Main.java"}};
       runNegativeTest(
            new String[] {
		"TA119nvp4Main.java",
			    "\n" +
			    "public class TA119nvp4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final pt.TeamA119nvp4 t1 = new pt.TeamA119nvp4();\n" +
			    "        Role<@t1> r = new Role<@t1>();\n" +
			    "        pb.TA119nvp4_1<@t1,Role<@t1>> c = new pb.TA119nvp4_1<@t1,Role<@t1>>();\n" +
			    "        c.test(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"pb/TA119nvp4_1.java",
			    "package pb;\n" +
			    "import pt.TeamA119nvp4;\n" +
			    "public class TA119nvp4_1<TeamA119nvp4 t, R<@t> extends Showable<@t>> {\n" +
			    "    public void test(R<@t> r) {\n" +
			    "        t.show(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"pb/TA119nvp4_2.java",
			    "package pb;\n" +
			    "public class TA119nvp4_2 {}\n" +
			    "    \n",
		"pt/TeamA119nvp4.java",
			    "package pt;\n" +
			    "public team class TeamA119nvp4 {\n" +
			    "    public abstract class Showable {\n" +
			    "        public abstract void show();\n" +
			    "    }\n" +
			    "    public class Role extends Showable playedBy pb.TA119nvp4_2 {\n" +
			    "        public Role() { base(); }\n" +
			    "		 @Override\n" +
			    "        public void show() { System.out.print(\"OK\"); }\n" +
			    "    }\n" +
			    "    public void show(Showable s) {\n" +
			    "        s.show();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. WARNING in pt\\TeamA119nvp4.java (at line 6)\n" + 
    		"	public class Role extends Showable playedBy pb.TA119nvp4_2 {\n" + 
    		"	                                            ^^^^^^^^^^^^^^\n" + 
    		"Qualified reference to base class pb.TA119nvp4_2 is deprecated, should use a base import instead (OTJLD 2.1.2(d)).\n" + 
    		"----------\n" + 
    		(this.weavingScheme == WeavingScheme.OTRE && this.complianceLevel >= ClassFileConstants.JDK1_8
    		? 
			"2. WARNING in pt\\TeamA119nvp4.java (at line 6)\n" + 
			"	public class Role extends Showable playedBy pb.TA119nvp4_2 {\n" + 
			"	                                            ^^^^^^^^^^^^^^\n" + 
			"Base class pb.TA119nvp4_2 has class file version 52 which cannot be handled by the traditional OTRE based on BCEL. Please consider using the ASM based OTDRE instead.\n" + 
			"----------\n"
    		: ""
			) +
    		"----------\n" + 
    		"1. ERROR in TA119nvp4Main.java (at line 6)\n" + 
    		"	pb.TA119nvp4_1<@t1,Role<@t1>> c = new pb.TA119nvp4_1<@t1,Role<@t1>>();\n" + 
    		"	               ^^^\n" + 
    		"Illegal position for value parameter @t1: must be a parameter of a single name type reference(OTJLD A.9(a)).\n" + 
    		"----------\n" + 
    		"2. ERROR in TA119nvp4Main.java (at line 6)\n" + 
    		"	pb.TA119nvp4_1<@t1,Role<@t1>> c = new pb.TA119nvp4_1<@t1,Role<@t1>>();\n" + 
    		"	                                                     ^^^\n" + 
    		"Illegal position for value parameter @t1: must be a parameter of a single name type reference(OTJLD A.9(a)).\n" + 
    		"----------\n");
    }

    // a type parameter has a nested value parameter with additional type bound - role not subtype of bound
    // A.1.19-otjld-nested-value-parameter-5
    public void testA119_nestedValueParameter5() {
        runNegativeTestMatching(
            new String[] {
		"TA119nvp5Main.java",
			    "\n" +
			    "public class TA119nvp5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA119nvp5 t1 = new TeamA119nvp5();\n" +
			    "        Role<@t1> r = new Role<@t1>();\n" +
			    "        TA119nvp5_1<@t1,Role<@t1>> c = new TA119nvp5_1<@t1,Role<@t1>>();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA119nvp5.java",
			    "\n" +
			    "public team class TeamA119nvp5 {\n" +
			    "    public abstract class Showable {\n" +
			    "        public abstract void show();\n" +
			    "    }\n" +
			    "    public class Role playedBy TA119nvp5_2 {\n" +
			    "        public Role() { base(); }\n" +
			    "        public void show() { System.out.print(\"OK\"); }\n" +
			    "    }\n" +
			    "    public void show(Showable s) {\n" +
			    "        s.show();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp5_1.java",
			    "\n" +
			    "public class TA119nvp5_1<TeamA119nvp5 t, R<@t> extends Showable<@t>> {\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp5_2.java",
			    "\n" +
			    "public class TA119nvp5_2 {}\n" +
			    "    \n"
            },
            "Bound mismatch");
    }

    // a type parameter has a nested value parameter with additional type bound - wrong argument passed
    // A.1.19-otjld-nested-value-parameter-6
    public void testA119_nestedValueParameter6() {
        runNegativeTestMatching(
            new String[] {
		"TA119nvp6Main.java",
			    "\n" +
			    "public class TA119nvp6Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA119nvp6 t1 = new TeamA119nvp6();\n" +
			    "        TA119nvp6_1<@t1,Role<@t1>> c = new TA119nvp6_1<@t1,Role<@t1>>();\n" +
			    "        OtherRole<@t1> r = new OtherRole<@t1>();        \n" +
			    "        c.test(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA119nvp6.java",
			    "\n" +
			    "public team class TeamA119nvp6 {\n" +
			    "    public abstract class Showable {\n" +
			    "        public abstract void show();\n" +
			    "    }\n" +
			    "    public class Role extends Showable playedBy TA119nvp6_2 {\n" +
			    "        public Role() { base(); }\n" +
			    "        public void show() { System.out.print(\"OK\"); }\n" +
			    "    }\n" +
			    "    public class OtherRole extends Showable {\n" +
			    "        public void show() { System.out.print(\"NOK\"); }\n" +
			    "    }\n" +
			    "    public void show(Showable s) {\n" +
			    "        s.show();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp6_1.java",
			    "\n" +
			    "public class TA119nvp6_1<TeamA119nvp6 t, R<@t> extends Showable<@t>> {\n" +
			    "    public void test(R<@t> r) {\n" +
			    "        t.show(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp6_2.java",
			    "\n" +
			    "public class TA119nvp6_2 {}\n" +
			    "    \n"
            },
            "err");
    }

    // a type parameter has a nested value parameter with additional type bound (plain class)
    // A.1.19-otjld-nested-value-parameter-7
    public void testA119_nestedValueParameter7() {
       
       runConformTest(
            new String[] {
		"TA119nvp7Main.java",
			    "\n" +
			    "public class TA119nvp7Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA119nvp7 t1 = new TeamA119nvp7();\n" +
			    "        Role<@t1> r = new Role<@t1>();\n" +
			    "        TA119nvp7_1<@t1,Role<@t1>> c = new TA119nvp7_1<@t1,Role<@t1>>();\n" +
			    "        c.test(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp7_2.java",
			    "\n" +
			    "public class TA119nvp7_2 {}\n" +
			    "    \n",
		"TA119nvp7_3.java",
			    "\n" +
			    "public abstract class TA119nvp7_3 {\n" +
			    "    public abstract void show();\n" +
			    "}\n" +
			    "    \n",
		"TeamA119nvp7.java",
			    "\n" +
			    "public team class TeamA119nvp7 {\n" +
			    "    public class Role extends TA119nvp7_3 playedBy TA119nvp7_2 {\n" +
			    "        public Role() { base(); }\n" +
			    "        public void show() { System.out.print(\"OK\"); }\n" +
			    "    }\n" +
			    "    public void show(TA119nvp7_3 s) {\n" +
			    "        s.show();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119nvp7_1.java",
			    "\n" +
			    "public class TA119nvp7_1<TeamA119nvp7 t, R<@t> extends TA119nvp7_3> {\n" +
			    "    public void test(R<@t> r) {\n" +
			    "        t.show(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a method has a nested value paramter
    // A.1.19-otjld-nested-value-parameter-8
    public void testA119_nestedValueParameter8() {
       
       runConformTest(
            new String[] {
		"TA119nvp8.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class TA119nvp8 {\n" +
			    "    static\n" +
			    "    <R<@t>>\n" +
			    "    void unregister(int dummy, R<@t> r, final Team t) {\n" +
			    "        t.unregisterRole(r);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA119nvp8 t1 = new TeamA119nvp8();\n" +
			    "        Role<@t1> r = t1.new Role();\n" +
			    "        unregister(3, r, t1);\n" +
			    "        System.out.print(t1.getAllRoles().length);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA119nvp8.java",
			    "\n" +
			    "public team class TeamA119nvp8 {\n" +
			    "    public class Role { }\n" +
			    "}\n" +
			    "    \n"
            },
            "0");
    }

    // a method has a nested value paramter - mismatch within signature
    // A.1.19-otjld-nested-value-parameter-9
    public void testA119_nestedValueParameter9() {
        runNegativeTestMatching(
            new String[] {
		"TA119nvp9.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class TA119nvp9 {\n" +
			    "    static\n" +
			    "    <R<@t>>\n" +
			    "    void unregister(int dummy, R<@t2> r, final Team t, final Team t2) {\n" +
			    "        t.unregisterRole(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA119nvp9.java",
			    "\n" +
			    "public team class TeamA119nvp9 {\n" +
			    "    public class Role { }\n" +
			    "}\n" +
			    "    \n"
            },
            "9.2.1(a)");
    }

    // a method has a nested value paramter - illegal application
    // A.1.19-otjld-nested-value-parameter-10
    public void testA119_nestedValueParameter10() {
        runNegativeTestMatching(
            new String[] {
		"TA119nvp10.java",
			    "\n" +
			    "import org.objectteams.Team;\n" +
			    "public class TA119nvp10 {\n" +
			    "    static\n" +
			    "    <R<@t>>\n" +
			    "    void unregister(int dummy, R<@t> r, final Team t, final Team t2) {\n" +
			    "        t.unregisterRole(r);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA119nvp10 t1 = new TeamA119nvp10();\n" +
			    "        final TeamA119nvp10 t2 = new TeamA119nvp10();\n" +
			    "        Role<@t2> r = new Role<@t2>();\n" +
			    "        unregister(3, r, t1, t2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA119nvp10.java",
			    "\n" +
			    "public team class TeamA119nvp10 {\n" +
			    "    public class Role { }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in TA119nvp10.java (at line 13)\n" + 
    		"	unregister(3, r, t1, t2);\n" + 
    		"	^^^^^^^^^^\n" + 
    		"The method unregister(int, R<@t1>, Team, Team) in the type TA119nvp10 is not applicable for the arguments (int, Role<@t2>, TeamA119nvp10, TeamA119nvp10)\n" + 
    		"----------\n");
    }

    // a plain generic type is parameterized by an externalized role - positive case
    // A.1.19-otjld-parameterized-by-role-1
    public void testA119_parameterizedByRole1() {
       
       runConformTest(
            new String[] {
		"TA119pbr1Main.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public class TA119pbr1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "	final TeamA119pbr1 t = new TeamA119pbr1();\n" +
			    "	R<@t> r1 = t.new R();\n" +
			    "	R<@t> r2 = t.new R();\n" +
			    "	LinkedList<R<@t>> l = new LinkedList<R<@t>>();\n" +
			    "	l.add(r1);\n" +
			    "	l.add(r2);\n" +
			    "	l.get(0).printOther(l.get(1));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA119pbr1.java",
			    "\n" +
			    "public team class TeamA119pbr1 {\n" +
			    "    public class R  {\n" +
			    "	public void printOther(R other) {\n" +
			    "	    other.print();\n" +
			    "	}\n" +
			    "	void print() {\n" +
			    "	    System.out.print(\"OK\");\n" +
			    "	}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a plain generic type is parameterized by an externalized role - different teams
    // A.1.19-otjld-parameterized-by-role-2
    public void testA119_parameterizedByRole2() {
        runNegativeTestMatching(
            new String[] {
		"TA119pbr2Main.java",
			    "\n" +
			    "import java.util.LinkedList;\n" +
			    "public class TA119pbr2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "	final TeamA119pbr2 t1 = new TeamA119pbr2();\n" +
			    "	final TeamA119pbr2 t2 = new TeamA119pbr2();\n" +
			    "	R<@t1> r1 = t1.new R();\n" +
			    "	R<@t2> r2 = t2.new R();\n" +
			    "	LinkedList<R<@t1>> l = new LinkedList<R<@t1>>();\n" +
			    "	l.add(r1);\n" +
			    "	l.add(r2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA119pbr2.java",
			    "\n" +
			    "public team class TeamA119pbr2 {\n" +
			    "    public class R  {\n" +
			    "	public void printOther(R other) {\n" +
			    "	    other.print();\n" +
			    "	}\n" +
			    "	void print() {\n" +
			    "	    System.out.print(\"OK\");\n" +
			    "	}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "not applicable");
    }

    // a plain generic type is parameterized by two externalized roles
    // A.1.19-otjld-parameterized-by-role-3
    public void testA119_parameterizedByRole3() {
       
       runConformTest(
            new String[] {
		"TA119pbr3Main.java",
			    "\n" +
			    "import java.util.HashMap;\n" +
			    "public class TA119pbr3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "	final TeamA119pbr3 t1 = new TeamA119pbr3();\n" +
			    "	final TeamA119pbr3 t2 = new TeamA119pbr3();\n" +
			    "	R<@t1> r1 = t1.new R();\n" +
			    "	R<@t2> r2 = t2.new R();\n" +
			    "	HashMap<R<@t1>,R<@t2>> m = new HashMap<R<@t1>,R<@t2>>();\n" +
			    "	m.put(r1,r2);\n" +
			    "	m.get(r1).print();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA119pbr3.java",
			    "\n" +
			    "public team class TeamA119pbr3 {\n" +
			    "    public class R  {\n" +
			    "	public void print() {\n" +
			    "	    System.out.print(\"OK\");\n" +
			    "	}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a plain generic type is parameterized by two externalized roles - wrong instance
    // A.1.19-otjld-parameterized-by-role-4
    public void testA119_parameterizedByRole4() {
        runNegativeTestMatching(
            new String[] {
		"TA119pbr4Main.java",
			    "\n" +
			    "import java.util.HashMap;\n" +
			    "public class TA119pbr4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "	final TeamA119pbr4 t1 = new TeamA119pbr4();\n" +
			    "	final TeamA119pbr4 t2 = new TeamA119pbr4();\n" +
			    "	R<@t1> r1 = t1.new R();\n" +
			    "	R<@t1> r2 = t1.new R();\n" +
			    "	HashMap<R<@t1>,R<@t2>> m = new HashMap<R<@t1>,R<@t2>>();\n" +
			    "	m.put(r1,r2);\n" +
			    "	m.get(r1).print();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA119pbr4.java",
			    "\n" +
			    "public team class TeamA119pbr4 {\n" +
			    "    public class R  {\n" +
			    "	public void print() {\n" +
			    "	    System.out.print(\"OK\");\n" +
			    "	}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "not applicable");
    }

    // a role type is used as a type bound
    // A.1.19-otjld-parameterized-by-role-5
    public void testA119_parameterizedByRole5() {
       
       runConformTest(
            new String[] {
		"TA119pbr5Main.java",
			    "\n" +
			    "public class TA119pbr5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA119pbr5 t = new TeamA119pbr5();\n" +
			    "        R<@t> r = t.new R();\n" +
			    "        TA119pbr5 b = r;\n" +
			    "        R<@t> r2 = t.getRole(b, t.getRClass());\n" +
			    "        System.out.print(r == r2);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA119pbr5.java",
			    "\n" +
			    "public team class TeamA119pbr5 {\n" +
			    "    public class R playedBy TA119pbr5 {\n" +
			    "        public R() { base(); }\n" +
			    "    }\n" +
			    "    public Class<? extends R> getRClass() {\n" +
			    "        return R.class;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TA119pbr5.java",
			    "\n" +
			    "public class TA119pbr5 {}\n" +
			    "    \n"
            },
            "true");
    }
    
    // similar to above but not using OT specifics
    public void testA119_plainJava() {
    	runConformTest(
    		new String[] {
    			"GetRole.java",
    			"\n" + 
    			"class R<Z> {}\n" + 
    			"\n" + 
    			"public class GetRole {\n" + 
    			"	public <T> T getRole(Object o, Class<T> clazz) {\n" + 
    			"		return null;\n" + 
    			"	}\n" + 
    			"	public Class<? extends R> getRClass() { return R.class; }\n" + 
    			"	static void test(GetRole gr, Object o) {\n" + 
    			"		R<String> rs = gr.getRole(o, gr.getRClass());\n" + 
    			"	}\n" + 
    			"}\n"
    		},
    		"");
    }

    // a collection is parameterized by a role, used in foreach outside the team
    // A.1.19-otjld-parameterized-by-role-6
    public void testA119_parameterizedByRole6() {
       
       runConformTest(
            new String[] {
		"TA119pbr6Main.java",
			    "\n" +
			    "public class TA119pbr6Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA119pbr6 t = new TeamA119pbr6();\n" +
			    "        t.addR(\"O\");\n" +
			    "        t.addR(\"K\");\n" +
			    "        for(R<@t> r : t.roles)\n" +
			    "            r.print();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"TeamA119pbr6.java",
			    "\n" +
			    "import java.util.List;\n" +
			    "import java.util.ArrayList;\n" +
			    "public team class TeamA119pbr6 {\n" +
			    "    public List<R> roles = new ArrayList<R>();\n" +
			    "    public void addR(String v) {\n" +
			    "        this.roles.add(new R(v));\n" +
			    "    }\n" +
			    "    public class R {\n" +
			    "        String v;\n" +
			    "        protected R(String v) { this.v = v; }\n" +
			    "        public void print() {\n" +
			    "            System.out.print(this.v);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // method call through an externalized role takes as argument a type parameterized by a role
    // A.1.19-otjld-parameterized-by-role-7
    public void testA119_parameterizedByRole7() {
       
       runConformTest(
            new String[] {
		"TA119pbr7Main.java",
			    "\n" +
			    "public class TA119pbr7Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final TeamA119pbr7 t = new TeamA119pbr7();\n" +
			    "        final R<@t> r = new R<@t>();\n" +
			    "        System.out.print(r.isSimilar(new IA119pbr7<R<@t>>() {\n" +
			    "                public boolean check(R<@t> arg)  {\n" +
			    "                    return arg == r;\n" +
			    "                }\n" +
			    "            }));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"IA119pbr7.java",
			    "\n" +
			    "public interface IA119pbr7<T> {\n" +
			    "    boolean check(T arg);\n" +
			    "}\n" +
			    "    \n",
		"TeamA119pbr7.java",
			    "\n" +
			    "public team class TeamA119pbr7 {\n" +
			    "    public class R {\n" +
			    "        public boolean isSimilar(IA119pbr7<R> otherCheck) {\n" +
			    "            return otherCheck.check(this);\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "true");
    }

    // A team contains an enum
    public void testA120_enumInTeam1() {
    	runConformTest(
    		new String[] {
    	"TeamA120eit1.java",
    		"public team class TeamA120eit1 {\n" +
    		"	protected enum Values { V1, V2 };\n" +
    		"	public static void main(String[] args) {\n" +
    		"		for (Values v : Values.values())\n" +
    		"			System.out.print(v);\n" +
    		"	}\n" +
    		"}"
    		},
    		"V1V2");
    }

    // A team contains an enum, team contains syntax error
    public void testA120_enumInTeam2() {
    	runNegativeTest(
    		new String[] {
    	"TeamA120eit2.java",
    		"public team class TeamA120eit2 {\n" +
    		"	public enum Values { V1, V2 };\n" +
    		"	protected class R playedBy B base when(unfinished.) {}\n" +
    		"	public static void main(String[] args) {\n" +
    		"		for (Values v : Values.values())\n" +
    		"			System.out.print(v);\n" +
    		"	}\n" +
    		"}",
    	"B.java",
    		"public class B {}\n"
    		},
    		"----------\n" + 
    		"1. ERROR in TeamA120eit2.java (at line 3)\n" + 
    		"	protected class R playedBy B base when(unfinished.) {}\n" + 
    		"	                                                 ^\n" + 
    		"Syntax error on token \".\", this expected after this token\n" + 
    		"----------\n");
    }

    // A team contains an enum - with team inheritance
    // Bug 336395 - [compiler] enum inside team class may cause NPE
    public void testA120_enumInTeam3() {
    	runConformTest(
    		new String[] {
    	"TeamA120eit3_2.java",
	    		"public team class TeamA120eit3_2 extends TeamA120eit3_1 {\n" +
	    		"	public static void main(String[] args) {\n" +
	    		"		for (Values v : Values.values())\n" +
	    		"			System.out.print(v);\n" +
	    		"	}\n" +
	    		"}",
    	"TeamA120eit3_1.java",
	    		"public team class TeamA120eit3_1 {\n" +
	    		"	protected enum Values { V1, V2 };\n" +
	    		"}"
    		},
    		"V1V2");
    }

    // A team contains an enum - with team inheritance - incremental compilation
    // Bug 348570 - [compiler] missing team anchor reported against enum
    public void testA120_enumInTeam4() {
    	runConformTest(
    		new String[] {
    	"TeamA120eit4_1.java",
	    		"public team class TeamA120eit4_1 {\n" +
	    		"	protected enum Values { V1, V2 };\n" +
	    		"}"
    		},
    		"");
    	runConformTest(
    		new String[] {
    	"TeamA120eit4_2.java",
	    		"public team class TeamA120eit4_2 extends TeamA120eit4_1 {\n" +
	    		"	public static void main(String[] args) {\n" +
	    		"		for (Values v : Values.values())\n" +
	    		"			System.out.print(v);\n" +
	    		"	}\n" +
	    		"}"
    		},
    		"V1V2",
            null/*classLibs*/,
            false/*shouldFlush*/,
            null/*vmArgs*/,
            null/*customOptions*/,
            null/*requestor*/);
    }

    // An enum-in-a-team tries to refer to a role
    // Bug 355259 - Cannot declare role-typed field in an enum-as-team-member
    public void testA120_enumInTeam5() {
    	runNegativeTest(
    		new String[] {
    	"TeamA120eit5.java",
	    		"public team class TeamA120eit5 {\n" +
	    		"   protected class R {}\n" +
	    		"   protected enum Values {\n" +
	    		"       V1, V2;\n" +
	    		"       public R r = new R();\n" +
	    		"   }\n" +
	    		"	public static void main(String[] args) {\n" +
	    		"		for (Values v : Values.values())\n" +
	    		"			System.out.print(v);\n" +
	    		"	}\n" +
	    		"}"
    		},
    		"----------\n" + 
			"1. ERROR in TeamA120eit5.java (at line 5)\n" + 
			"	public R r = new R();\n" + 
			"	             ^^^^^^^\n" + 
			"No enclosing instance of type TeamA120eit5 is accessible. Must qualify the allocation with an enclosing instance of type TeamA120eit5 (e.g. x.new A() where x is an instance of TeamA120eit5).\n" + 
			"----------\n");
    }

    public void testA121_genericNestedTeam1() {
    	runConformTest(
    		new String[] {
    	"TeamA121gnt1.java",
	    	"public team class TeamA121gnt1 {\n" +
			"    public team class Mid<A1> {\n" +
			"        protected class Inner<A> {}\n" +
			"    }\n" +
			"    public team class Mid2<A2> extends Mid<A2> {\n" +
			"    }\n" +
			"}\n"
    		});
    }
    
    public void testA122_genericsRegression1() {
    	runConformTest(
    		new String[] {
    	"TA122gr1.java",
    		"import java.util.*;\n" +
    		"public class TA122gr1 {\n" +
    		"	HashMap<String,Class<?>> map = new HashMap<String,Class<?>>();\n" +
    		"}\n"
    		});
    }

}
