/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2012 IT Service Omikron GmbH and others.
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
package org.eclipse.objectteams.otdt.tests.otjld.calloutbinding;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

@SuppressWarnings("unchecked")
public class OverridingAccessRestrictions extends AbstractOTJLDTest {
	
	public OverridingAccessRestrictions(String name) {
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
		return OverridingAccessRestrictions.class;
	}

    // a callout binding access a hidden base method
    // 7.4.1-otjld-callout-access-to-hidden-method-1
    public void test741_calloutAccessToHiddenMethod1() {
       
       runConformTest(
            new String[] {
		"T741cathm1Main.java",
			    "\n" +
			    "public class T741cathm1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        p2.Team741cathm1_2 t = new p2.Team741cathm1_2();\n" +
			    "        p1.T741cathm1      o = new p1.T741cathm1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T741cathm1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T741cathm1 {\n" +
			    "    protected String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team741cathm1_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team741cathm1_1 {\n" +
			    "    public class Role741cathm1 playedBy T741cathm1 {}\n" +
			    "}\n" +
			    "    \n",
		"p2/Team741cathm1_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team741cathm1_2 extends p1.Team741cathm1_1 {\n" +
			    "    public class Role741cathm1 {\n" +
			    "        public abstract String test();\n" +
			    "        test -> getValue;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(p1.T741cathm1 as Role741cathm1 obj) {\n" +
			    "        return obj.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout binding access a hidden base method
    // 7.4.1-otjld-callout-access-to-hidden-method-2
    public void test741_calloutAccessToHiddenMethod2() {
       Map customOptions = getCompilerOptions();
       
       runConformTest(
            new String[] {
		"T741cathm2Main.java",
			    "\n" +
			    "public class T741cathm2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        p2.Team741cathm2 t = new p2.Team741cathm2();\n" +
			    "        p1.T741cathm2    o = new p1.T741cathm2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T741cathm2.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T741cathm2 {\n" +
			    "    String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/Team741cathm2.java",
			    "\n" +
			    "package p2;\n" +
			    "import base p1.T741cathm2;\n" +
			    "public team class Team741cathm2 {\n" +
			    "    public class Role741cathm2_1 playedBy T741cathm2 {}\n" +
			    "\n" +
			    "    public class Role741cathm2_2 extends Role741cathm2_1 {\n" +
			    "        public abstract String test();\n" +
			    "        test -> getValue;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(p1.T741cathm2 as Role741cathm2_2 obj) {\n" +
			    "        return obj.test();\n" +
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

    // a callout binding access a hidden base method -- suppressed warning
    // 7.4.1-otjld-callout-access-to-hidden-method-2b
    public void test741_calloutAccessToHiddenMethod2b() {
        runConformTest(
            new String[] {
		"p1/T741cathm2b.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T741cathm2b {\n" +
			    "    String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/Team741cathm2b.java",
			    "\n" +
			    "package p2;\n" +
			    "import base p1.T741cathm2b;\n" +
			    "public team class Team741cathm2b {\n" +
			    "    public class Role741cathm2b_1 playedBy T741cathm2b {}\n" +
			    "\n" +
			    "    public class Role741cathm2b_2 extends Role741cathm2b_1 {\n" +
			    "        public abstract String test();\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        test -> getValue;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(p1.T741cathm2b as Role741cathm2b_2 obj) {\n" +
			    "        return obj.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            });
    }

    // a callout binding access a hidden base method
    // 7.4.1-otjld-callout-access-to-hidden-method-3
    public void test741_calloutAccessToHiddenMethod3() {
       Map customOptions = getCompilerOptions();
       
       runConformTest(
            new String[] {
		"T741cathm3Main.java",
			    "\n" +
			    "public class T741cathm3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team741cathm3 t = new Team741cathm3();\n" +
			    "        T741cathm3    o = new T741cathm3();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T741cathm3.java",
			    "\n" +
			    "public class T741cathm3 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team741cathm3.java",
			    "\n" +
			    "public team class Team741cathm3 {\n" +
			    "    public class Role741cathm3 playedBy T741cathm3 {\n" +
			    "        public abstract String test();\n" +
			    "        test -> getValue;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T741cathm3 as Role741cathm3 obj) {\n" +
			    "        return obj.test();\n" +
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

    // a callout binding access a hidden static base method
    // 7.4.1-otjld-callout-access-to-hidden-method-4
    public void test741_calloutAccessToHiddenMethod4() {
       
       runConformTest(
            new String[] {
		"T741cathm4Main.java",
			    "\n" +
			    "public class T741cathm4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        p2.Team741cathm4_2 t = new p2.Team741cathm4_2();\n" +
			    "        p1.T741cathm4      o = new p1.T741cathm4();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T741cathm4.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T741cathm4 {\n" +
			    "    protected static String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team741cathm4_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team741cathm4_1 {\n" +
			    "    public class Role741cathm4 playedBy T741cathm4 {}\n" +
			    "}\n" +
			    "    \n",
		"p2/Team741cathm4_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team741cathm4_2 extends p1.Team741cathm4_1 {\n" +
			    "    public class Role741cathm4 {\n" +
			    "        public abstract String test();\n" +
			    "        test -> getValue;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(p1.T741cathm4 as Role741cathm4 obj) {\n" +
			    "        return obj.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout binding access a hidden method in a indirect base class - private not visible
    // 7.4.2-otjld-callout-access-to-hidden-method-1f
    public void test742_calloutAccessToHiddenMethod1f() {
        runNegativeTestMatching(
            new String[] {
		"Team742cathm1f.java",
			    "\n" +
			    "public team class Team742cathm1f {\n" +
			    "    public class Role742cathm1f playedBy T742cathm1f_2 {\n" +
			    "        public abstract String test();\n" +
			    "        test -> getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T742cathm1f_1.java",
			    "\n" +
			    "public class T742cathm1f_1 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T742cathm1f_2.java",
			    "\n" +
			    "public class T742cathm1f_2 extends T742cathm1f_1 {}\n" +
			    "    \n"
            },
            "3.1(c)");
    }

    // a callout binding access a hidden method in a indirect base class
    // 7.4.2-otjld-callout-access-to-hidden-method-1
    public void test742_calloutAccessToHiddenMethod1() {
       Map customOptions = getCompilerOptions();
       
       runConformTest(
            new String[] {
		"p1/T742cathm1_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T742cathm1_1 {\n" +
			    "    String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/T742cathm1_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public class T742cathm1_2 extends p1.T742cathm1_1 {}\n" +
			    "    \n",
		"p3/Team742cathm1.java",
			    "\n" +
			    "package p3;\n" +
			    "import base p2.T742cathm1_2;\n" +
			    "public team class Team742cathm1 {\n" +
			    "    public class Role742cathm1 playedBy T742cathm1_2 {\n" +
			    "        public abstract String test();\n" +
			    "        test -> getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a callout binding access a hidden method in a indirect base class
    // 7.4.2-otjld-callout-access-to-hidden-method-2
    public void test742_calloutAccessToHiddenMethod2() {
       Map customOptions = getCompilerOptions();
       
       runConformTest(
            new String[] {
		"p1/T742cathm2_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T742cathm2_1 {\n" +
			    "    String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/T742cathm2_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public class T742cathm2_2 extends p1.T742cathm2_1 {}\n" +
			    "    \n",
		"p1/Team742cathm2.java",
			    "\n" +
			    "package p1;\n" +
			    "import base p2.T742cathm2_2;\n" +
			    "public team class Team742cathm2 {\n" +
			    "    public class Role742cathm2 playedBy T742cathm2_2 {\n" +
			    "        public abstract String test();\n" +
			    "        test -> getValue;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a callout binding accesses a private method of a base class that is a role itself
    // 7.4.2-otjld-callout-access-to-hidden-method-3
    public void test742_calloutAccessToHiddenMethod3() {
       
       runConformTest(
            new String[] {
		"Team742cathm3_2.java",
			    "\n" +
			    "public team class Team742cathm3_2 {\n" +
			    "    final Team742cathm3_1 theT = new Team742cathm3_1();\n" +
			    "    public class Role742cathm3 playedBy R<@theT> {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        public String test() -> String getValue();\n" +
			    "    }\n" +
			    "    Team742cathm3_2() {\n" +
			    "        System.out.print(new Role742cathm3(theT.new R()).test());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team742cathm3_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team742cathm3_1.java",
			    "\n" +
			    "public team class Team742cathm3_1 {\n" +
			    "    public class R {\n" +
			    "        private String getValue() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout binding accesses a private method of a base class that is a role itself -- externalized creation uses new syntax
    // 7.4.2-otjld-callout-access-to-hidden-method-3f
    public void test742_calloutAccessToHiddenMethod3f() {
       
       runConformTest(
            new String[] {
		"Team742cathm3f_2.java",
			    "\n" +
			    "public team class Team742cathm3f_2 {\n" +
			    "    final Team742cathm3f_1 theT = new Team742cathm3f_1();\n" +
			    "    public class Role742cathm3f playedBy R<@theT> {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        public String test() -> String getValue();\n" +
			    "    }\n" +
			    "    Team742cathm3f_2() {\n" +
			    "        System.out.print(new Role742cathm3f(new R<@theT>()).test());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team742cathm3f_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team742cathm3f_1.java",
			    "\n" +
			    "public team class Team742cathm3f_1 {\n" +
			    "    public class R {\n" +
			    "        private String getValue() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout accesses a protected member which its base inherits, sub-base overrides
    // 7.4.2-otjld-callout-access-to-hidden-method-4
    public void test742_calloutAccessToHiddenMethod4() {
       
       runConformTest(
            new String[] {
		"Team742cathm4.java",
			    "\n" +
			    "import base p2.T742cathm4_2;\n" +
			    "public team class Team742cathm4 {\n" +
			    "    protected class R playedBy T742cathm4_2 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        protected void tr() -> void tb();\n" +
			    "    }\n" +
			    "    <B base R> void test(B as R o) {\n" +
			    "        o.tr();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team742cathm4 t = new Team742cathm4();\n" +
			    "        t.test(new p3.T742cathm4_3());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T742cathm4_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T742cathm4_1 {\n" +
			    "    protected void tb() {\n" +
			    "        System.out.print(\"NOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/T742cathm4_2.java",
			    "\n" +
			    "package p2;\n" +
			    "import p1.T742cathm4_1;\n" +
			    "public class T742cathm4_2 extends T742cathm4_1 { }\n" +
			    "    \n",
		"p3/T742cathm4_3.java",
			    "\n" +
			    "package p3;\n" +
			    "import p2.T742cathm4_2;\n" +
			    "public class T742cathm4_3 extends T742cathm4_2 {\n" +
			    "    protected void tb() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout accesses a private method in its direct base - sub base re-declares
    // 7.4.2-otjld-callout-access-to-hidden-method-5
    public void test742_calloutAccessToHiddenMethod5() {
       
       runConformTest(
            new String[] {
		"Team742cathm5.java",
			    "\n" +
			    "public team class Team742cathm5 {\n" +
			    "    protected class R playedBy T742cathm5_1 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        protected void tr() -> void test();\n" +
			    "        protected void bar() -> void bar();\n" +
			    "    }\n" +
			    "    void test (T742cathm5_1 as R o) {\n" +
			    "        o.bar();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team742cathm5 t = new Team742cathm5();\n" +
			    "        t.test(new T742cathm5_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T742cathm5_1.java",
			    "\n" +
			    "public class T742cathm5_1 {\n" +
			    "    private void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "    protected void bar() {\n" +
			    "        test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T742cathm5_2.java",
			    "\n" +
			    "public class T742cathm5_2 extends T742cathm5_1 {\n" +
			    "    protected void test() {\n" +
			    "        System.out.print(\"NOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout accesses a private method in its direct base - sub base re-declares
    // 7.4.2-otjld-callout-access-to-hidden-method-6
    public void test742_calloutAccessToHiddenMethod6() {
       
       runConformTest(
            new String[] {
		"Team742cathm6.java",
			    "\n" +
			    "public team class Team742cathm6 {\n" +
			    "    protected class R playedBy T742cathm6_1 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        protected void tr() -> void test();\n" +
			    "    }\n" +
			    "    void test (T742cathm6_1 as R o) {\n" +
			    "        o.tr();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team742cathm6 t = new Team742cathm6();\n" +
			    "        t.test(new T742cathm6_2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T742cathm6_1.java",
			    "\n" +
			    "public class T742cathm6_1 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T742cathm6_2.java",
			    "\n" +
			    "public class T742cathm6_2 extends T742cathm6_1 {\n" +
			    "    protected void test() {\n" +
			    "        System.out.print(\"NOK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin binding access a hidden base method
    // 7.4.3-otjld-callin-access-to-hidden-method-1
    public void test743_callinAccessToHiddenMethod1() {
       
       runConformTest(
            new String[] {
		"T743cathm1Main.java",
			    "\n" +
			    "public class T743cathm1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        p2.Team743cathm1_2 t = new p2.Team743cathm1_2();\n" +
			    "        p1.T743cathm1      o = new p1.T743cathm1();\n" +
			    "\n" +
			    "        within (t) {\n" +
			    "            o.test();\n" +
			    "        }\n" +
			    "        System.out.print(t.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T743cathm1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T743cathm1 {\n" +
			    "    protected void testInternal() {}\n" +
			    "    public void test() {\n" +
			    "        testInternal();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team743cathm1_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team743cathm1_1 {\n" +
			    "    public class Role743cathm1 playedBy T743cathm1 {}\n" +
			    "}\n" +
			    "    \n",
		"p2/Team743cathm1_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team743cathm1_2 extends p1.Team743cathm1_1 {\n" +
			    "    private String value = \"NOTOK\";\n" +
			    "\n" +
			    "    public class Role743cathm1 {\n" +
			    "        public void test() {\n" +
			    "            Team743cathm1_2.this.value = \"OK\";\n" +
			    "        }\n" +
			    "        test <- before testInternal;\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin binding access a hidden base method
    // 7.4.3-otjld-callin-access-to-hidden-method-2
    public void test743_callinAccessToHiddenMethod2() {
       
       runConformTest(
            new String[] {
		"T743cathm2Main.java",
			    "\n" +
			    "public class T743cathm2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        p2.Team743cathm2 t = new p2.Team743cathm2();\n" +
			    "        p1.T743cathm2    o = new p1.T743cathm2();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        o.test();\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(t.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T743cathm2.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T743cathm2 {\n" +
			    "    void testInternal() {}\n" +
			    "    public void test() {\n" +
			    "        testInternal();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/Team743cathm2.java",
			    "\n" +
			    "package p2;\n" +
			    "import base p1.T743cathm2;\n" +
			    "public team class Team743cathm2 {\n" +
			    "    private String value = \"NOTOK\";\n" +
			    "\n" +
			    "    public class Role743cathm2_1 playedBy T743cathm2 {}\n" +
			    "\n" +
			    "    public class Role743cathm2_2 extends Role743cathm2_1 {\n" +
			    "        public void test() {\n" +
			    "            Team743cathm2.this.value = \"OK\";\n" +
			    "        }\n" +
			    "        void test() <- after void testInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue() {\n" +
			    "        return value;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin binding access a hidden base method
    // 7.4.3-otjld-callin-access-to-hidden-method-3
    public void test743_callinAccessToHiddenMethod3() {
       
       runConformTest(
            new String[] {
		"T743cathm3Main.java",
			    "\n" +
			    "public class T743cathm3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team743cathm3 t = new Team743cathm3();\n" +
			    "        T743cathm3    o = new T743cathm3();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T743cathm3.java",
			    "\n" +
			    "public class T743cathm3 {\n" +
			    "    private String test() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "\n" +
			    "    public String toString() {\n" +
			    "        return test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team743cathm3.java",
			    "\n" +
			    "import org.objectteams.ImplicitTeamActivation;\n" +
			    "public team class Team743cathm3 {\n" +
			    "    public class Role743cathm3 playedBy T743cathm3 {\n" +
			    "        callin String test() {\n" +
			    "            base.test();\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        test <- replace test;\n" +
			    "    }\n" +
			    "    @ImplicitTeamActivation\n" +
			    "    public String getValue(T743cathm3 obj) {\n" +
			    "        return obj.toString();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callin binding access a hidden method in a indirect base class  private not visible
    // 7.4.4-otjld-callin-access-to-hidden-method-1f
    public void test744_callinAccessToHiddenMethod1f() {
        runNegativeTestMatching(
            new String[] {
		"Team744cathm1f.java",
			    "\n" +
			    "public team class Team744cathm1f {\n" +
			    "    public class Role744cathm1f playedBy T744cathm1f_2 {\n" +
			    "        public void test() {}\n" +
			    "        test <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T744cathm1f_1.java",
			    "\n" +
			    "public class T744cathm1f_1 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private void test() {}\n" +
			    "}\n" +
			    "    \n",
		"T744cathm1f_2.java",
			    "\n" +
			    "public class T744cathm1f_2 extends T744cathm1f_1 {}\n" +
			    "    \n"
            },
            "4.1(c)");
    }

    // a callin binding access a hidden method in a indirect base class
    // 7.4.4-otjld-callin-access-to-hidden-method-1
    public void test744_callinAccessToHiddenMethod1() {
       
       runConformTest(
            new String[] {
		"p1/T744cathm1_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T744cathm1_1 {\n" +
			    "    void test() {}\n" +
			    "}\n" +
			    "    \n",
		"p2/T744cathm1_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public class T744cathm1_2 extends p1.T744cathm1_1 {}\n" +
			    "    \n",
		"p3/Team744cathm1.java",
			    "\n" +
			    "package p3;\n" +
			    "import base p2.T744cathm1_2;\n" +
			    "public team class Team744cathm1 {\n" +
			    "    public class Role744cathm1 playedBy T744cathm1_2 {\n" +
			    "        public void test() {}\n" +
			    "        test <- before test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "");
    }

    // a callin binding access a hidden method in a indirect base class
    // 7.4.4-otjld-callin-access-to-hidden-method-2
    public void test744_callinAccessToHiddenMethod2() {
       
       runConformTest(
            new String[] {
		"p1/T744cathm2_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T744cathm2_1 {\n" +
			    "    String getValue() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/T744cathm2_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public class T744cathm2_2 extends p1.T744cathm2_1 {}\n" +
			    "    \n",
		"p1/Team744cathm2.java",
			    "\n" +
			    "package p1;\n" +
			    "import base p2.T744cathm2_2;\n" +
			    "public team class Team744cathm2 {\n" +
			    "    public class Role744cathm2 playedBy T744cathm2_2 {\n" +
			    "        callin String test() {\n" +
			    "            return base.test();\n" +
			    "        }\n" +
			    "        String test() <- replace String getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "");
    }

    // a normal role method access a hidden method in a indirect base class
    // 7.4.5-otjld-role-method-accesses-hidden-method-1
    public void test745_roleMethodAccessesHiddenMethod1() {
        runNegativeTest(
            new String[] {
		"p1/T745cathm1_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T745cathm1_1 {\n" +
			    "    protected void test() {}\n" +
			    "}\n" +
			    "    \n",
		"p2/T745cathm1_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public class T745cathm1_2 extends p1.T745cathm1_1 {}\n" +
			    "    \n",
		"p2/Team745cathm1.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team745cathm1 {\n" +
			    "    public class Role745cathm1 playedBy T745cathm1_2 {\n" +
			    "        public void test(T745cathm1_2 obj) {\n" +
			    "            obj.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a normal role method access a hidden method in a direct base class
    // 7.4.5-otjld-role-method-accesses-hidden-method-2
    public void test745_roleMethodAccessesHiddenMethod2() {
        runNegativeTest(
            new String[] {
		"p1/T745cathm2.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T745cathm2 {\n" +
			    "    void test() {}\n" +
			    "}\n" +
			    "    \n",
		"p1/Team745cathm2_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team745cathm2_1 {\n" +
			    "    public class Role745cathm2 playedBy T745cathm2 {}\n" +
			    "}\n" +
			    "    \n",
		"p2/Team745cathm2_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team745cathm2_2 extends p1.Team745cathm2_1 {\n" +
			    "    public class Role745cathm2 {\n" +
			    "        public void test(p1.T745cathm2 obj) {\n" +
			    "            obj.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a normal role method access a hidden method in a direct base class
    // 7.4.5-otjld-role-method-accesses-hidden-method-3
    public void test745_roleMethodAccessesHiddenMethod3() {
        runNegativeTest(
            new String[] {
		"T745cathm3.java",
			    "\n" +
			    "public class T745cathm3 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private void test() {}\n" +
			    "}\n" +
			    "    \n",
		"Team745cathm3.java",
			    "\n" +
			    "public team class Team745cathm3 {\n" +
			    "    public class Role745cathm3_1 playedBy T745cathm3 {}\n" +
			    "\n" +
			    "    public class Role745cathm3_2 extends Role745cathm3_1 {\n" +
			    "        public void test(T745cathm3 obj) {\n" +
			    "            obj.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a playedBy declaration overrides access restrictions
    // 7.4.6-otjld-baseclass-is-invisible-1
    public void test746_baseclassIsInvisible1() {
       
       runConformTest(
            new String[] {
		"p1/T746biv1Main.java",
			    "\n" +
			    "package p1;    \n" +
			    "public class T746biv1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new p2.Team746biv1().activate();\n" +
			    "        new T746biv1().tust();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T746biv1.java",
			    "\n" +
			    "package p1;\n" +
			    "class T746biv1 {\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "    void tust() {}\n" +
			    "}\n" +
			    "    \n",
		"p2/Team746biv1.java",
			    "\n" +
			    "package p2;\n" +
			    "import base p1.T746biv1;\n" +
			    "public team class Team746biv1 {\n" +
			    "    protected class R playedBy T746biv1 {\n" +
			    "        void tost() {\n" +
			    "            tast();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void tast() -> void test();\n" +
			    "        tost <- after tust;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a playedBy declaration overrides access restrictions -- warnings suppressed
    // 7.4.6-otjld-baseclass-is-invisible-2
    public void test746_baseclassIsInvisible2() {
       
       runConformTest(
            new String[] {
		"p1/T746biv2Main.java",
			    "\n" +
			    "package p1;    \n" +
			    "public class T746biv2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new p2.Team746biv2().activate();\n" +
			    "        new T746biv2().tust();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T746biv2.java",
			    "\n" +
			    "package p1;\n" +
			    "class T746biv2 {\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "    void tust() {}\n" +
			    "}\n" +
			    "    \n",
		"p2/Team746biv2.java",
			    "\n" +
			    "package p2;\n" +
			    "@SuppressWarnings({\"decapsulation\",\"bindingconventions\"})\n" +
			    "public team class Team746biv2 {\n" +
			    "    protected class R playedBy p1.T746biv2 {\n" +
			    "        void tost() {\n" +
			    "            tast();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void tast() -> void test();\n" +
			    "        tost <- after tust;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a playedBy declaration overrides access restrictions -- static callout  (3 warnings: 2 decaps + 1 fqn base class
    // 7.4.6-otjld-baseclass-is-invisible-3
    public void test746_baseclassIsInvisible3() {
       Map customOptions = getCompilerOptions();
       
       runConformTest(
            new String[] {
		"p1/T746biv3Main.java",
			    "\n" +
			    "package p1;    \n" +
			    "public class T746biv3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new p2.Team746biv3().activate();\n" +
			    "        new T746biv3().tust();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T746biv3.java",
			    "\n" +
			    "package p1;\n" +
			    "class T746biv3 {\n" +
			    "    static void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "    void tust() {}\n" +
			    "}\n" +
			    "    \n",
		"p2/Team746biv3.java",
			    "\n" +
			    "package p2;    \n" +
			    "public team class Team746biv3 {\n" +
			    "    protected class R playedBy p1.T746biv3 {\n" +
			    "        void tost() {\n" +
			    "            tast();\n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        void tast() -> void test();\n" +
			    "        tost <- after tust;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a playedBy declaration overrides access restrictions - mentioning directly, too
    // 7.4.6-otjld-baseclass-is-invisible-4
    public void test746_baseclassIsInvisible4() {
        runNegativeTestMatching(
            new String[] {
		"p2/Team746biv4.java",
			    "\n" +
			    "public team class Team746biv4 {\n" +
			    "    @SuppressWarnings(\"bindingconventions\")\n" +
			    "    protected class R playedBy p1.T746biv4 { // OK\n" +
			    "    }\n" +
			    "    p1.T746biv4 aBase; // NOK\n" +
			    "}\n" +
			    "    \n",
		"p1/T746biv4.java",
			    "\n" +
			    "package p1;\n" +
			    "class T746biv4 {\n" +
			    "}\n" +
			    "    \n"
            },
            "not visible");
    }

    // using a base constructor
    // 7.4.6-otjld-baseclass-is-invisible-5
    public void test746_baseclassIsInvisible5() {
       
       runConformTest(
            new String[] {
		"p2/Team746biv5.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team746biv5 {\n" +
			    "    @SuppressWarnings({\"decapsulation\",\"bindingconventions\"})\n" +
			    "    protected class R playedBy p1.T746biv5 {\n" +
			    "        protected R() {\n" +
			    "            base(25);\n" +
			    "        }\n" +
			    "        public void test() {\n" +
			    "            System.out.print(getI());\n" +
			    "        }\n" +
			    "        int getI() -> get int i;\n" +
			    "    }\n" +
			    "    Team746biv5() {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team746biv5();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T746biv5.java",
			    "\n" +
			    "package p1;\n" +
			    "class T746biv5 {\n" +
			    "    int i;\n" +
			    "    T746biv5 (int i) {\n" +
			    "        this.i = i;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "25");
    }

    // a playedBy declaration overrides access restrictions, used in a signature + copy inheritance (4 warns: 3 decaps + 1 fqn base class
    // 7.4.6-otjld-baseclass-is-invisible-6
    public void test746_baseclassIsInvisible6() {
       Map customOptions = getCompilerOptions();
       
       runConformTest(
            new String[] {
		"p1/T746biv6Main.java",
			    "\n" +
			    "package p1;    \n" +
			    "public class T746biv6Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new p3.Team746biv6_2().activate();\n" +
			    "        new T746biv6().tust();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T746biv6.java",
			    "\n" +
			    "package p1;\n" +
			    "class T746biv6 {\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "    void tust() {}\n" +
			    "}\n" +
			    "    \n",
		"p2/Team746biv6.java",
			    "\n" +
			    "package p2;    \n" +
			    "public team class Team746biv6 {\n" +
			    "    protected class R playedBy p1.T746biv6 {\n" +
			    "        String val;\n" +
			    "        R(p1.T746biv6 aBase) {\n" +
			    "            val = \"K\";\n" +
			    "        }\n" +
			    "        void tost() {\n" +
			    "            tast();\n" +
			    "            System.out.print(val);\n" +
			    "        }\n" +
			    "        void tast() -> void test();\n" +
			    "        tost <- after tust;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"p3/Team746biv6_2.java",
			    "\n" +
			    "package p3;\n" +
			    "public team class Team746biv6_2 extends p2.Team746biv6 {}\n" +
			    "    \n"
            },
            "OK",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // a playedBy declaration overrides access restrictions, used in the signature of a callin-bound base method
    // 7.4.6-otjld-baseclass-is-invisible-7
    public void test746_baseclassIsInvisible7() {
       
       runConformTest(
            new String[] {
		"p2/Team746biv7.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team746biv7 {\n" +
			    "    @SuppressWarnings({\"decapsulation\",\"bindingconventions\"})\n" +
			    "    protected class R playedBy p1.T746biv7_1 {\n" +
			    "        callin R[] tist(R other) {\n" +
			    "            base.tist(other);\n" +
			    "            System.out.print(\"K\");\n" +
			    "            return null;\n" +
			    "        }\n" +
			    "        tist <- replace tust;\n" +
			    "    }\n" +
			    "    Team746biv7() {\n" +
			    "        activate();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team746biv7();\n" +
			    "        new p1.T746biv7_2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T746biv7_1.java",
			    "\n" +
			    "package p1;\n" +
			    "class T746biv7_1 {\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"O\");\n" +
			    "    }\n" +
			    "    T746biv7_1[] tust(T746biv7_1 other) { other.test(); return null; }\n" +
			    "}\n" +
			    "    \n",
		"p1/T746biv7_2.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T746biv7_2 {\n" +
			    "    public void test() {\n" +
			    "        new T746biv7_1().tust(new T746biv7_1());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a bound base class is final - expect decapsulation warning
    // 7.4.6-otjld-baseclass-is-invisible-8
    public void test746_baseclassIsInvisible8() {
       Map options = getCompilerOptions();
       options.put(OTDTPlugin.OT_COMPILER_DECAPSULATION, JavaCore.ERROR);
       runNegativeTest(
            new String[] {
		"Team746bii8.java",
			    "\n" +
			    "public team class Team746bii8 {\n" +
			    "    public class R playedBy T746bii8 {\n" +
			    "        void ok() { System.out.print(\"OK\"); }\n" +
			    "        ok <- after test;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team746bii8().activate();\n" +
			    "        new T746bii8().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T746bii8.java",
			    "\n" +
			    "public final class T746bii8 {\n" +
			    "    void test() {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team746bii8.java (at line 3)\n" + 
    		"	public class R playedBy T746bii8 {\n" + 
    		"	                        ^^^^^^^^\n" + 
    		"PlayedBy binding overrides finalness of base class T746bii8 (OTJLD 2.1.2(c)).\n" + 
    		"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            options);
    }

    // a base imported base class is invisible, suppressing at the role suffices
    // 7.4.6-otjld-baseclass-is-invisible-9
    public void test746_baseclassIsInvisible9() {
       
       runConformTest(
            new String[] {
		"Team746bii9.java",
			    "\n" +
			    "import base p1.T746bii9;\n" +
			    "public team class Team746bii9 {\n" +
			    "    @SuppressWarnings(\"decapsulation\")\n" +
			    "    protected class R playedBy T746bii9 {\n" +
			    "        protected void test() -> void test();\n" +
			    "        protected R() {\n" +
			    "            base();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test() {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team746bii9().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T746bii9.java",
			    "\n" +
			    "package p1;\n" +
			    "class T746bii9 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private void test() { System.out.print(\"OK\"); }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // despite decapsulation a private feature of a super class is not accessible
    // 7.4.7-otjld-private-feature-is-not-inherited-1
    public void test747_privateFeatureIsNotInherited1() {
        runNegativeTestMatching(
            new String[] {
		"Team747pfini1.java",
			    "\n" +
			    "public team class Team747pfini1 {\n" +
			    "    protected class R playedBy T747pfini1_2 {\n" +
			    "        int steal() -> get int secret;\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T747pfini1_1.java",
			    "\n" +
			    "public class T747pfini1_1 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private int secret;\n" +
			    "}    \n" +
			    "    \n",
		"T747pfini1_2.java",
			    "\n" +
			    "public class T747pfini1_2 extends T747pfini1_1 {\n" +
			    "    // nothing\n" +
			    "}\n" +
			    "    \n"
            },
            "3.4(d)");
    }

    // despite decapsulation a private feature of a super class is not accessible
    // 7.4.7-otjld-private-feature-is-not-inherited-2
    public void test747_privateFeatureIsNotInherited2() {
        runNegativeTestMatching(
            new String[] {
		"Team747pfini2.java",
			    "\n" +
			    "public team class Team747pfini2 {\n" +
			    "    protected class R playedBy T747pfini2_2 {\n" +
			    "        int steal() -> int secret();\n" +
			    "    }\n" +
			    "}\n",
		"T747pfini2_1.java",
			    "\n" +
			    "public class T747pfini2_1 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private int secret() { return 17; };\n" +
			    "}    \n" +
			    "    \n",
		"T747pfini2_2.java",
			    "\n" +
			    "public class T747pfini2_2 extends T747pfini2_1 {\n" +
			    "    // nothing\n" +
			    "}\n" +
			    "    \n"
            },
            "3.4(d)");
    }

    // despite decapsulation a private feature of a super class is not accessible (see TPX-442)
    // 7.4.7-otjld-private-feature-is-not-inherited-3
    public void test747_privateFeatureIsNotInherited3() {
       
       runConformTest(
            new String[] {
		"Team747pfini3.java",
			    "\n" +
			    "public team class Team747pfini3 {\n" +
			    "    protected class R1 playedBy T747pfini3_1 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        protected String steal() -> get String secret;\n" +
			    "        protected void test() {};\n" +
			    "    }\n" +
			    "    protected class R2 extends R1 playedBy T747pfini3_2 {\n" +
			    "        @Override\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(steal());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public Team747pfini3(T747pfini3_1 as R1 o) {\n" +
			    "        o.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team747pfini3(new T747pfini3_2());\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"T747pfini3_1.java",
			    "\n" +
			    "public class T747pfini3_1 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private String secret = \"secret\";\n" +
			    "}    \n" +
			    "    \n",
		"T747pfini3_2.java",
			    "\n" +
			    "public class T747pfini3_2 extends T747pfini3_1 {\n" +
			    "    // nothing\n" +
			    "}\n" +
			    "    \n"
            },
            "secret");
    }

    // a callout to field causes decapsulation: watch for the warning
    // 7.4.8-otjld-field-decapsulation-1
    public void test748_fieldDecapsulation1() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportDecapsulationWrite, CompilerOptions.ERROR);
        runNegativeTest(
            new String[] {
		"T748fd1.java",
			    "\n" +
			    "public class T748fd1 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private int secret;\n" +
			    "}\n" +
			    "    \n",
		"Team748fd1.java",
			    "\n" +
			    "public team class Team748fd1 {\n" +
			    "    protected class R playedBy T748fd1 {\n" +
			    "        int steal() -> get int secret;\n" +
			    "        void setSecret(int val) -> set int secret;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. WARNING in Team748fd1.java (at line 4)\n" +
			"	int steal() -> get int secret;\n" +
			"	                       ^^^^^^\n" +
			"Access restriction of private field secret in type T748fd1 is overridden by this binding (OTJLD 3.5(e)).\n" +
            "----------\n" +
			"2. ERROR in Team748fd1.java (at line 5)\n" +
			"	void setSecret(int val) -> set int secret;\n" +
			"	                                   ^^^^^^\n" +
			"Write access to the private field secret in type T748fd1 overrides access restriction (OTJLD 3.5(e)).\n" +
			"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }

    // a callout to field causes decapsulation: watch for the effect
    // 7.4.8-otjld-field-decapsulation-2
    public void test748_fieldDecapsulation2() {
       
       runConformTest(
            new String[] {
		"Team748fd2.java",
			    "\n" +
			    "public team class Team748fd2 {\n" +
			    "    protected class R playedBy T748fd2 {\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "        public int steal() -> get int secret;\n" +
			    "    }\n" +
			    "    Team748fd2(T748fd2 as R r) {\n" +
			    "        System.out.print(r.steal());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team748fd2(new T748fd2());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T748fd2.java",
			    "\n" +
			    "public class T748fd2 {\n" +
			    "    @SuppressWarnings(\"unused\")\n" +
			    "    private int secret = 13;\n" +
			    "}\n" +
			    "    \n"
            },
            "13");
    }

    // a callout to field causes decapsulation, multiple declarations in the inheritance hierarchy - see Trac #232
    // 7.4.8-otjld-field-decapsulation-3
    public void test748_fieldDecapsulation3() {
       
       runConformTest(
            new String[] {
		"Team748fd3.java",
			    "\n" +
			    "public team class Team748fd3 {\n" +
			    "        public class R1 playedBy T748fd3_1 {\n" +
			    "                // should use private str field from T748fd3_1:\n" +
			    "                @SuppressWarnings(\"decapsulation\")\n" +
			    "                private String getStr() -> get String str;\n" +
			    "                printStr <- after printStr1;\n" +
			    "                void printStr() {\n" +
			    "                        System.out.println(\"T1.R1::getStr1(): \" + getStr());\n" +
			    "                }\n" +
			    "        }\n" +
			    "\n" +
			    "        public class R2 /* extends R1 */ playedBy T748fd3_2 {\n" +
			    "                // should use private str field from T748fd3_2:\n" +
			    "                @SuppressWarnings(\"decapsulation\")\n" +
			    "                private String getStr() -> get String str;\n" +
			    "                printStr <- after printStr2;\n" +
			    "                void printStr() {\n" +
			    "                        System.out.println(\"T1.R2::getStr2(): \" + getStr());\n" +
			    "                }\n" +
			    "        }\n" +
			    "        public static void main(String[] args) {\n" +
			    "                new Team748fd3().activate();\n" +
			    "                T748fd3_1 c1 = new T748fd3_1();\n" +
			    "                T748fd3_2 c2 = new T748fd3_2();\n" +
			    "                System.out.println(\"c1.printStr1():\");\n" +
			    "                c1.printStr1();\n" +
			    "                System.out.println(\"c2.printStr1():\");\n" +
			    "                c2.printStr1();\n" +
			    "                System.out.println(\"c2.printStr2():\");\n" +
			    "                c2.printStr2();\n" +
			    "                System.out.println(\"((T748fd3_1)c2).printStr1():\");\n" +
			    "                ((T748fd3_1)c2).printStr1();\n" +
			    "        }\n" +
			    "}\n" +
			    "    \n",
		"T748fd3_1.java",
			    "\n" +
			    "public class T748fd3_1 {\n" +
			    "        private String str = \"C1.str\";\n" +
			    "        public void printStr1() {\n" +
			    "                System.out.println(\"C1::printStr1(): \" + str);\n" +
			    "        }\n" +
			    "}\n" +
			    "    \n",
		"T748fd3_2.java",
			    "\n" +
			    "public class T748fd3_2 extends T748fd3_1 {\n" +
			    "        private String str = \"C2.str\";\n" +
			    "        public void printStr2() {\n" +
			    "                System.out.println(\"C2::printStr2(): \" + str);\n" +
			    "        }    \n" +
			    "}\n" +
			    "    \n"
            },
            "c1.printStr1():\n" +
			"C1::printStr1(): C1.str\n" +
			"T1.R1::getStr1(): C1.str\n" +
			"c2.printStr1():\n" +
			"C1::printStr1(): C1.str\n" +
			"T1.R1::getStr1(): C1.str\n" +
			"c2.printStr2():\n" +
			"C2::printStr2(): C2.str\n" +
			"T1.R2::getStr2(): C2.str\n" +
			"((T748fd3_1)c2).printStr1():\n" +
			"C1::printStr1(): C1.str\n" +
			"T1.R1::getStr1(): C1.str");
    }
}
