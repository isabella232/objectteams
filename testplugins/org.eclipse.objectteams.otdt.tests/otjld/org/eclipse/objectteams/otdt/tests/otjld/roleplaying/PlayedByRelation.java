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

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

import junit.framework.Test;

@SuppressWarnings("unchecked")
public class PlayedByRelation extends AbstractOTJLDTest {
	
	public PlayedByRelation(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test2124_internalRole1"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return PlayedByRelation.class;
	}

    // a role is bound to an unrelated class
    // 2.1.1-otjld-bound-to-unrelated-class-1
    public void test211_boundToUnrelatedClass1() {
       
       runConformTest(
            new String[] {
		"T211btuc1Main.java",
			    "\n" +
			    "public class T211btuc1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team211btuc1 t = new Team211btuc1();\n" +
			    "        T211btuc1    o = new T211btuc1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T211btuc1.java",
			    "\n" +
			    "class T211btuc1 {\n" +
			    "    String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team211btuc1.java",
			    "\n" +
			    "public team class Team211btuc1 {\n" +
			    "\n" +
			    "    public class Role211btuc1 playedBy T211btuc1 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T211btuc1 as Role211btuc1 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to an inner class of the enclosing normal class
    // 2.1.1-otjld-bound-to-unrelated-class-2
    public void test211_boundToUnrelatedClass2() {
       
       runConformTest(
            new String[] {
		"T211btuc2Main.java",
			    "\n" +
			    "public class T211btuc2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T211btuc2.Team211btuc2  t = new T211btuc2().new Team211btuc2();\n" +
			    "        T211btuc2.Inner211btuc2 o = new T211btuc2().new Inner211btuc2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T211btuc2.java",
			    "\n" +
			    "public class T211btuc2 {\n" +
			    "    public class Inner211btuc2 {\n" +
			    "        public String getValueInternal() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public team class Team211btuc2 {\n" +
			    "        public class Role211btuc2 playedBy Inner211btuc2 {\n" +
			    "            protected abstract String getValue();\n" +
			    "            String getValue() -> String getValueInternal();\n" +
			    "        }\n" +
			    "    \n" +
			    "        public String getValue(Inner211btuc2 as Role211btuc2 obj) {\n" +
			    "            return obj.getValue();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to a nested class of some other normal class
    // 2.1.1-otjld-bound-to-unrelated-class-3
    public void test211_boundToUnrelatedClass3() {
       
       runConformTest(
            new String[] {
		"T211btuc3Main.java",
			    "\n" +
			    "public class T211btuc3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team211btuc3             t = new Team211btuc3();\n" +
			    "        T211btuc3.Nested211btuc3 o = new T211btuc3.Nested211btuc3();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T211btuc3.java",
			    "\n" +
			    "public class T211btuc3 {\n" +
			    "    static class Nested211btuc3 {\n" +
			    "        String getValueInternal() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team211btuc3.java",
			    "\n" +
			    "public team class Team211btuc3 {\n" +
			    "\n" +
			    "    public class Role211btuc3 playedBy T211btuc3.Nested211btuc3 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T211btuc3.Nested211btuc3 as Role211btuc3 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to another team class
    // 2.1.1-otjld-bound-to-unrelated-class-4
    public void test211_boundToUnrelatedClass4() {
       
       runConformTest(
            new String[] {
		"T211btuc4Main.java",
			    "\n" +
			    "public class T211btuc4Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team211btuc4_1 t1 = new Team211btuc4_1();\n" +
			    "        Team211btuc4_2 t2 = new Team211btuc4_2();\n" +
			    "\n" +
			    "        System.out.print(t2.getValue(t1));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team211btuc4_1.java",
			    "\n" +
			    "public team class Team211btuc4_1 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team211btuc4_2.java",
			    "\n" +
			    "public team class Team211btuc4_2 {\n" +
			    "\n" +
			    "    public class Role211btuc4 playedBy Team211btuc4_1 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(Team211btuc4_1 as Role211btuc4 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a base class (same package) is deprecated, reported as error
    // 2.1.1-otjld-bound-to-unrelated-class-5
    public void test211_boundToUnrelatedClass5() {
        runNegativeTest(
            new String[] {
		"Team211btuc5.java",
			    "\n" +
			    "public team class Team211btuc5 {\n" +
			    "    protected class R playedBy T211btuc5 { }\n" +
			    "}\n" +
			    "    \n",
		"T211btuc5.java",
			    "\n" +
			    "/** @deprecated don't use this any more */\n" +
			    "@Deprecated\n" +
			    "public class T211btuc5 { }\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team211btuc5.java (at line 3)\n" + 
    		"	protected class R playedBy T211btuc5 { }\n" + 
    		"	                           ^^^^^^^^^\n" + 
    		"Base class T211btuc5 is deprecated.\n" + 
    		"----------\n");
    }

    // a role is bound to its direct superclass
    // 2.1.2-otjld-bound-to-related-class-1
    public void test212_boundToRelatedClass1() {
       
       runConformTest(
            new String[] {
		"T212btrc1Main.java",
			    "\n" +
			    "public class T212btrc1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team212btrc1 t = new Team212btrc1();\n" +
			    "        T212btrc1    o = new T212btrc1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T212btrc1.java",
			    "\n" +
			    "public class T212btrc1 {\n" +
			    "    protected String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team212btrc1.java",
			    "\n" +
			    "public team class Team212btrc1 {\n" +
			    "\n" +
			    "    public class Role212btrc1 extends T212btrc1 playedBy T212btrc1 {\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "        public abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T212btrc1 as Role212btrc1 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to its indirect superclass
    // 2.1.2-otjld-bound-to-related-class-2
    public void test212_boundToRelatedClass2() {
       
       runConformTest(
            new String[] {
		"T212btrc2Main.java",
			    "\n" +
			    "public class T212btrc2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team212btrc2_2 t = new Team212btrc2_2();\n" +
			    "        T212btrc2      o = new T212btrc2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T212btrc2.java",
			    "\n" +
			    "public class T212btrc2 {\n" +
			    "    protected String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team212btrc2_1.java",
			    "\n" +
			    "public team class Team212btrc2_1 {\n" +
			    "\n" +
			    "    public class Role212btrc2 extends T212btrc2 {\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team212btrc2_2.java",
			    "\n" +
			    "public team class Team212btrc2_2 extends Team212btrc2_1 {\n" +
			    "\n" +
			    "    public class Role212btrc2 playedBy T212btrc2 {\n" +
			    "        public abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T212btrc2 as Role212btrc2 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to enclosing normal class, illegal callout
    // 2.1.2-otjld-bound-to-related-class-3
    // Bug 318815 -  [otjld] [compiler] Support the "Internal Role" pattern
    public void test212_boundToRelatedClass3() {
    	Map options = getCompilerOptions();
    	options.put(CompilerOptions.OPTION_ReportBaseclassCycle, CompilerOptions.ERROR);
        runNegativeTest(
            new String[] {
		"T212btrc3.java",
			    "\n" +
			    "public class T212btrc3 {\n" +
			    "    @SuppressWarnings(\"unused\") /*callout access*/private String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "    public team class Team212btrc3 {\n" +
			    "        public class Role212btrc3 playedBy T212btrc3 {\n" +
			    "            public String getValue() -> String getValueInternal();\n" +
			    "        }\n" +
			    "    \n" +
			    "        public String getValue(T212btrc3 as Role212btrc3 obj) {\n" +
			    "            return obj.getValue();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in T212btrc3.java (at line 7)\n" + 
    		"	public class Role212btrc3 playedBy T212btrc3 {\n" + 
    		"	                                   ^^^^^^^^^\n" + 
    		"Base class T212btrc3 is an enclosing type of Role212btrc3; please read the hints in the OT/J Language Definition (OTJLD 2.1.2(b)).\n" + 
    		"----------\n" + 
    		"2. ERROR in T212btrc3.java (at line 8)\n" + 
    		"	public String getValue() -> String getValueInternal();\n" + 
    		"	              ^^^^^^^^\n" + 
    		"Role T212btrc3.Team212btrc3.Role212btrc3 cannot declare callout bindings because it is playedBy enclosing type T212btrc3 (OTJLD 3.1(a)); please directly access the target method.\n" + 
    		"----------\n" + 
    		"3. ERROR in T212btrc3.java (at line 12)\n" + 
    		"	return obj.getValue();\n" + 
    		"	           ^^^^^^^^\n" + 
    		"The method getValue() is undefined for the type Role212btrc3<@tthis[Team212btrc3]>\n" + 
    		"----------\n",
    		null/*classLibraries*/,
    		true/*shouldFlushOutputDirectory*/,
    		options);
    }

    // a role is bound to its team - legal callin
    // 2.1.2-otjld-bound-to-related-class-4
    // Bug 318815 -  [otjld] [compiler] Support the "Internal Role" pattern
    public void test212_boundToRelatedClass4() {
        runConformTest(
            new String[] {
		"Team212btrc4.java",
			    "\n" +
			    "public team class Team212btrc4 {\n" +
			    "    private String getValueInternal() {\n" +
			    "        return \"O\";\n" +
			    "    }\n" +
			    "\n" +
			    "	 @SuppressWarnings(\"baseclasscycle\")\n" +
			    "    protected class Role212btrc4 playedBy Team212btrc4 {\n" + 
			    "        String getValue() <- replace String getValueInternal();\n" +
			    "		 callin String getValue() {\n" +
			    "			return base.getValue() + \"K\";\n" +
			    "		 }\n" +
			    "    }\n" +
			    "\n" +
			    "    public static void main(String ... args) {\n" +
			    "        Team212btrc4 t = new Team212btrc4();\n" +
			    "		 t.activate();\n" +
			    "		 System.out.print(t.getValueInternal());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to its superteam
    // 2.1.2-otjld-bound-to-related-class-5
    // Bug 318815 -  [otjld] [compiler] Support the "Internal Role" pattern
    public void test212_boundToRelatedClass5() {
       
       runNegativeTest(
            new String[] {
		"T212btrc5Main.java",
			    "\n" +
			    "public class T212btrc5Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team212btrc5_1 t1 = new Team212btrc5_1();\n" +
			    "        Team212btrc5_2 t2 = new Team212btrc5_2();\n" +
			    "\n" +
			    "        System.out.print(t2.getValue(t1));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team212btrc5_1.java",
			    "\n" +
			    "public team class Team212btrc5_1 {\n" +
			    "    protected String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team212btrc5_2.java",
			    "\n" +
			    "public team class Team212btrc5_2 extends Team212btrc5_1 {\n" +
			    "\n" +
			    "	 @SuppressWarnings(\"baseclasscycle\")\n" +
			    "    public class Role212btrc5 playedBy Team212btrc5_1 {\n" +
			    "        public String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(Team212btrc5_1 as Role212btrc5 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team212btrc5_2.java (at line 6)\n" + 
    		"	public String getValue() -> String getValueInternal();\n" + 
    		"	              ^^^^^^^^\n" + 
    		"Role Team212btrc5_2.Role212btrc5 cannot declare callout bindings because it is playedBy enclosing type Team212btrc5_1 (OTJLD 3.1(a)); please directly access the target method.\n" + 
    		"----------\n" + 
    		"2. ERROR in Team212btrc5_2.java (at line 10)\n" + 
    		"	return obj.getValue();\n" + 
    		"	           ^^^^^^^^\n" + 
    		"The method getValue() is undefined for the type Role212btrc5<@tthis[Team212btrc5_2]>\n" + 
    		"----------\n");
    }

    // a role is indirectly bound to its team - rejected as error
    // Bug 318815 -  [otjld] [compiler] Support the "Internal Role" pattern
    public void test212_boundToRelatedClass6() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportBaseclassCycle, CompilerOptions.ERROR);
        runNegativeTest(
            new String[] {
		"Team212btrc6_2.java",
			    "\n" +
			    "public team class Team212btrc6_2 {\n" +
			    "    protected class Role212btrc6_2 playedBy Team212btrc6_1 {\n" + 
			    "    }\n" +
			    "}\n",
		"Team212btrc6_1.java",
				"public team class Team212btrc6_1 {\n" +
				"	protected class Role212btrc6_1 playedBy Team212btrc6_2 {} \n" +
				"}\n"
            },
            "----------\n" + 
    		"1. ERROR in Team212btrc6_2.java (at line 3)\n" + 
    		"	protected class Role212btrc6_2 playedBy Team212btrc6_1 {\n" + 
    		"	                                        ^^^^^^^^^^^^^^\n" + 
    		"Base class/member type circularity via chain Team212btrc6_2.Role212btrc6_2->Team212btrc6_1->Team212btrc6_1.Role212btrc6_1->Team212btrc6_2;\n" + 
    		"please read the hints in the OT/J Language Definition (OTJLD 2.1.2(b)).\n" + 
    		"----------\n" + 
    		"----------\n" + 
    		"1. ERROR in Team212btrc6_1.java (at line 2)\n" + 
    		"	protected class Role212btrc6_1 playedBy Team212btrc6_2 {} \n" + 
    		"	                                        ^^^^^^^^^^^^^^\n" + 
    		"Base class/member type circularity via chain Team212btrc6_1.Role212btrc6_1->Team212btrc6_2->Team212btrc6_2.Role212btrc6_2->Team212btrc6_1;\n" + 
    		"please read the hints in the OT/J Language Definition (OTJLD 2.1.2(b)).\n" + 
    		"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }
    
    // a role is bound to an interface
    // 2.1.3-otjld-bound-to-interface
    public void test213_boundToInterface_1() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
        runNegativeTest(
            new String[] {
		"Team213bti1.java",
			    "\n" +
			    "public team class Team213bti1 {\n" +
			    "    public class Role213bti1 playedBy I213bti1 {\n" +
			    "        public abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "    public String getValue(T213bti1 as Role213bti1 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n",
		"I213bti1.java",
			    "\n" +
			    "public interface I213bti1 {\n" +
			    "    String getValueInternal();\n" +
			    "}\n" +
			    "    \n",
		"T213bti1.java",
			    "\n" +
			    "public class T213bti1 implements I213bti1 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n"
            },
            "----------\n" + 
    		"1. WARNING in Team213bti1.java (at line 3)\n" + 
    		"	public class Role213bti1 playedBy I213bti1 {\n" + 
    		"	                                  ^^^^^^^^\n" + 
    		"When binding interface I213bti1 as base of Role213bti1:\n" +
    		"Note that some features like callin bindings are not yet supported in this situation (OTJLD 2.1.1).\n" + 
    		"----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }
    
    // a role is bound to an interface
    // this could work (no method bindings here)
    public void test213_boundToInterface_2() {
        runConformTest(
            new String[] {
		"Team213bti2.java",
			    "import org.objectteams.LiftingFailedException;\n" +
			    "public team class Team213bti2 {\n" +
			    "    protected abstract class Role213bti2_0 playedBy I213bti2 {\n" +
			    "        public abstract String getValue();\n" +
			    "    }\n" +
			    "    public class Role213bti2_1 extends Role213bti2_0 playedBy T213bti2 {\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "    public String getValue(I213bti2 as Role213bti2_0 obj) throws LiftingFailedException {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "    public static void main(String... args) throws LiftingFailedException {\n" +
			    "        Team213bti2 t = new Team213bti2();\n" +
			    "        String v = t.getValue(new T213bti2());\n" +
			    "        System.out.println(v);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"I213bti2.java",
			    "\n" +
			    "public interface I213bti2 {\n" +
			    "    String getValueInternal();\n" +
			    "}\n",
		"T213bti2.java",
			    "\n" +
			    "public class T213bti2 implements I213bti2 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n"
            },
            "OK");
    }

    // a role is bound to an interface - with callout
    // 2.1.3-otjld-bound-to-interface 
    public void test213_boundToInterface_3() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
        runConformTest(
            new String[] {
		"Team213bti3.java",
			    "\n" +
			    "public team class Team213bti3 {\n" +
			    "    public class Role213bti3 playedBy I213bti3 {\n" +
			    "        public abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "    public class Role213bti3_1 extends Role213bti3 playedBy T213bti3 {}\n" +
			    "    public String getValue(T213bti3 as Role213bti3 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "    public static void main(String... args) {\n" +
			    "        Team213bti3 t = new Team213bti3();\n" +
			    "        String v = t.getValue(new T213bti3());\n" +
			    "        System.out.println(v);\n" +
			    "    }\n" +
			    "}\n",
		"I213bti3.java",
			    "\n" +
			    "public interface I213bti3 {\n" +
			    "    String getValueInternal();\n" +
			    "}\n",
		"T213bti3.java",
			    "\n" +
			    "public class T213bti3 implements I213bti3 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n"
            },
            "OK");
    }

    // a role is bound to an interface - with callin in sub-role
    public void test213_boundToInterface_4() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
        runConformTest(
            new String[] {
		"Team213bti4.java",
			    "\n" +
			    "public team class Team213bti4 {\n" +
			    "    public class Role213bti4 playedBy I213bti4 {\n" +
			    "        String getValue() -> String getOK();\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin String getOK() { return getValue(); }\n" +
			    "    }\n" +
			    "    public class Role213bti4_1 extends Role213bti4 playedBy T213bti4 {\n" +
			    "        getOK <- replace getNOTOK;\n" +
			    "    }\n" +
			    "    public static void main(String... args) {\n" +
			    "        Team213bti4 t = new Team213bti4();\n" +
			    "        t.activate();\n" +
			    "        T213bti4 b = new T213bti4();\n" +
			    "        System.out.print(b.getNOTOK());\n" +
			    "    }\n" +
			    "}\n",
		"I213bti4.java",
			    "\n" +
			    "public interface I213bti4 {\n" +
			    "    String getOK();\n" +
			    "}\n",
		"T213bti4.java",
			    "\n" +
			    "public class T213bti4 implements I213bti4 {\n" +
			    "    public String getOK() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "    public String getNOTOK() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n"
            },
            "OK");
    }

    // a role is bound to an interface - with callin => error
    public void test213_boundToInterface_4e() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
        runNegativeTest(
            new String[] {
		"Team213bti4e.java",
			    "\n" +
			    "public team class Team213bti4e {\n" +
			    "    public class Role213bti4e playedBy I213bti4e {\n" +
			    "        String getValue() -> String getOK();\n" +
			    "        @SuppressWarnings(\"basecall\")\n" +
			    "        callin String getOK() { return getValue(); }\n" +
			    "        getOK <- replace getNOTOK;\n" +
			    "    }\n" +
			    "    public class Role213bti4e_1 extends Role213bti4e playedBy T213bti4e {\n" +
			    "    }\n" +
			    "    public static void main(String... args) {\n" +
			    "        Team213bti4e t = new Team213bti4e();\n" +
			    "        t.activate();\n" +
			    "        T213bti4e b = new T213bti4e();\n" +
			    "        System.out.print(b.getNOTOK());\n" +
			    "    }\n" +
			    "}\n",
		"I213bti4e.java",
			    "\n" +
			    "public interface I213bti4e {\n" +
			    "    String getOK();\n" +
			    "    String getNOTOK();\n" +
			    "}\n",
		"T213bti4e.java",
			    "\n" +
			    "public class T213bti4e implements I213bti4e {\n" +
			    "    public String getOK() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "    public String getNOTOK() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n"
            },
            "----------\n" +
            "1. WARNING in Team213bti4e.java (at line 3)\n" +
            "	public class Role213bti4e playedBy I213bti4e {\n" +
            "	                                   ^^^^^^^^^\n" +
            "When binding interface I213bti4e as base of Role213bti4e:\nNote that some features like callin bindings are not yet supported in this situation (OTJLD 2.1.1).\n" +
            "----------\n" +
            "2. ERROR in Team213bti4e.java (at line 7)\n" +
            "	getOK <- replace getNOTOK;\n" +
            "	^^^^^\n" +
            "Implementation limitation: Callin binding to an interface (I213bti4e) is not supported.\n" +
            "----------\n",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            customOptions);
    }
    
    // a role is bound to an abstract class
    // 2.1.3-otjld-bound-to-abstract-class
    public void test213_boundToAbstractClass() {
       
       runConformTest(
            new String[] {
		"T213btacMain.java",
			    "\n" +
			    "public class T213btacMain {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team213btac t = new Team213btac();\n" +
			    "        T213btac1   o = new T213btac2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T213btac1.java",
			    "\n" +
			    "public abstract class T213btac1 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T213btac2.java",
			    "\n" +
			    "public class T213btac2 extends T213btac1 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team213btac.java",
			    "\n" +
			    "public team class Team213btac {\n" +
			    "    public class Role213btac playedBy T213btac1 {\n" +
			    "        public abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "    public String getValue(T213btac1 as Role213btac obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to its team, and issues an illegal base constructor call
    // 2.1.4-otjld-bound-to-related-team-1
    public void test214_boundToRelatedTeam1() {
        runNegativeTest(
            new String[] {
		"Team214btrt1.java",
			    "\n" +
			    "public team class Team214btrt1 {\n" +
			    "    protected class Role214btrt1 playedBy Team214btrt1 {\n" +
			    "        Role214btrt1() {\n" +
			    "			base();\n" +
			    "		 }\n" +
			    "    }\n" +
			    "}\n"
            },
            "----------\n" + 
    		"1. WARNING in Team214btrt1.java (at line 3)\n" + 
    		"	protected class Role214btrt1 playedBy Team214btrt1 {\n" + 
    		"	                                      ^^^^^^^^^^^^\n" + 
    		"Base class Team214btrt1 is an enclosing type of Role214btrt1; please read the hints in the OT/J Language Definition (OTJLD 2.1.2(b)).\n" + 
    		"----------\n" + 
    		"2. ERROR in Team214btrt1.java (at line 5)\n" + 
    		"	base();\n" + 
    		"	^^^^\n" + 
    		"Cannot invoke a base constructor because enclosing role Team214btrt1.Role214btrt1 is involved in baseclass circularity (OTJLD 2.4.2). \n" + 
    		"----------\n");
    }

    // a role is bound to its superteam (from which it is implicitly inherited), invokes method of enclosing team that recursively invokes the role method
    // 2.1.4-otjld-bound-to-related-team-2
    public void test214_boundToRelatedTeam2() {
       
       runConformTest(
            new String[] {
		"T214btrt2Main.java",
			    "\n" +
			    "public class T214btrt2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team214btrt2_2 t = new Team214btrt2_2();\n" +
			    "\n" +
			    "	try {\n" +
			    "        	System.out.print(t.getValue(t));\n" +
			    "	} catch (StackOverflowError soe) {\n" +
			    "		System.out.print(\"Caught\");\n" +
			    "	}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team214btrt2_1.java",
			    "\n" +
			    "public team class Team214btrt2_1 {\n" +
			    "    protected String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "\n" +
			    "    protected abstract class Role214btrt2 {\n" +
			    "        public abstract String getValue();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(Role214btrt2 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team214btrt2_2.java",
			    "\n" +
			    "public team class Team214btrt2_2 extends Team214btrt2_1 {\n" +
			    "    @SuppressWarnings(\"baseclasscycle\")\n" +
			    "    protected class Role214btrt2 playedBy Team214btrt2_1 {\n" +
			    "        public String getValue() { return getValueInternal(); }\n" +
			    "    }\n" +
			    "    \n" +
			    "    public String getValue(Team214btrt2_1 as Role214btrt2 obj) {\n" +
			    "        return super.getValue(obj);\n" +
			    "    }\n" +
			    "    protected String getValueInternal() {\n" +
			    "        return getValue(this);\n" +
			    "    }\n" +
			    "\n" +
			    "}\n" +
			    "    \n"
            },
            "Caught");
    }

    // a role is bound to itself
    // 2.1.5-otjld-bound-to-role-1
    public void test215_boundToRole1() {
        runNegativeTest(
            new String[] {
		"Team215btr1.java",
			    "\n" +
			    "public team class Team215btr1 {\n" +
			    "    public class Role215btr1 playedBy Role215btr1 {\n" +
			    "        private String getValueInternal() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "        public abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue() {\n" +
			    "        return new Role215btr1().getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role is bound to its explicit superrole
    // 2.1.5-otjld-bound-to-role-2
    public void test215_boundToRole2() {
        runNegativeTest(
            new String[] {
		"Team215btr2.java",
			    "\n" +
			    "public team class Team215btr2 {\n" +
			    "    public class Role215btr2_1 {\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class Role215btr2_2 extends Role215btr2_1 playedBy Role215btr2_1 {\n" +
			    "        public abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue() {\n" +
			    "        Role215btr2_2 r = new Role215btr2_1();\n" +
			    "\n" +
			    "        return new Role215btr2_2().getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role is bound to another role in the same team
    // 2.1.5-otjld-bound-to-role-3
    public void test215_boundToRole3() {
        runNegativeTest(
            new String[] {
		"Team215btr3.java",
			    "\n" +
			    "public team class Team215btr3 {\n" +
			    "    public class Role215btr3_1 {\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class Role215btr3_2 playedBy Role215btr3_1 {\n" +
			    "        public abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue() {\n" +
			    "        Role215btr3_2 r = new Role215btr3_2();\n" +
			    "\n" +
			    "        return r.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role is bound to another role in a different team using an anchored type
    // 2.1.5-otjld-bound-to-role-4
    public void test215_boundToRole4() {
       
       runConformTest(
            new String[] {
		"Team215btr4_2.java",
			    "\n" +
			    "public team class Team215btr4_2 {\n" +
			    "    private final Team215btr4_1 t = new Team215btr4_1();\n" +
			    "\n" +
			    "    public class Role215btr4_2 playedBy Role215btr4_1<@t> {\n" +
			    "        public abstract String getValue();\n" +
			    "	@SuppressWarnings(\"decapsulation\")\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "    public String getValue(Role215btr4_1<@t> as Role215btr4_2 r) {\n" +
			    "        return r.getValue();\n" +
			    "    }\n" +
			    "    public void test() {\n" +
			    "        System.out.print(getValue(t.new Role215btr4_1()));\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team215btr4_2 t = new Team215btr4_2();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team215btr4_1.java",
			    "\n" +
			    "public team class Team215btr4_1 {\n" +
			    "    public class Role215btr4_1 {\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to another role in a different team using an anchored type (with package structure)
    // 2.1.5-otjld-bound-to-role-4a
    public void test215_boundToRole4a() {
       
       runConformTest(
            new String[] {
		"mypackage/Team215btr4a_2.java",
			    "\n" +
			    "package mypackage;\n" +
			    "public team class Team215btr4a_2 {\n" +
			    "    private final Team215btr4a_1 t = new Team215btr4a_1();\n" +
			    "\n" +
			    "    public class Role215btr4a_2 playedBy Role215btr4a_1<@t> {\n" +
			    "        public abstract String getValue();\n" +
			    "	@SuppressWarnings(\"decapsulation\")\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "    public String getValue(Role215btr4a_1<@t> as Role215btr4a_2 r) {\n" +
			    "        return r.getValue();\n" +
			    "    }\n" +
			    "    public void test() {\n" +
			    "        System.out.print(getValue(t.new Role215btr4a_1()));\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team215btr4a_2 t = new Team215btr4a_2();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"mypackage/Team215btr4a_1.java",
			    "\n" +
			    "package mypackage;\n" +
			    "public team class Team215btr4a_1 {\n" +
			    "    public class Role215btr4a_1 {\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to another role in a different team using an anchored type - after callin
    // 2.1.5-otjld-bound-to-role-5
    public void test215_boundToRole5() {
       
       runConformTest(
            new String[] {
		"Team215btr5_2.java",
			    "\n" +
			    "public team class Team215btr5_2 {\n" +
			    "    private final Team215btr5_1 t = new Team215btr5_1();\n" +
			    "\n" +
			    "    public class Role215btr5_2 playedBy Role215btr5_1<@t> {\n" +
			    "        public abstract String getValue();\n" +
			    "	@SuppressWarnings(\"decapsulation\")\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "		protected void logSetting() {\n" +
			    "			System.out.print(getValue());\n" +
			    "		}\n" +
			    "    	logSetting <- after setValueInternal;\n" +
			    "	}\n" +
			    "    public void test() {\n" +
			    "        t.new Role215btr5_1().doIt();\n" +
			    "		\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "		Team215btr5_2 t2 = new Team215btr5_2();\n" +
			    "        t2.activate();\n" +
			    "		t2.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team215btr5_1.java",
			    "\n" +
			    "public team class Team215btr5_1 {\n" +
			    "	public Role215btr5_1 getRole() {\n" +
			    "		return new Role215btr5_1();\n" +
			    "	}\n" +
			    "    public class Role215btr5_1 {\n" +
			    "		private String valueInternal = \"XK\";\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return valueInternal;\n" +
			    "        }\n" +
			    "		protected void setValueInternal(String value) {\n" +
			    "			valueInternal = value;\n" +
			    "		}\n" +
			    "		public void doIt() {\n" +
			    "	 		setValueInternal(\"OK\");\n" +
			    "		}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to another role in a different team using an anchored type - replace callin
    // 2.1.5-otjld-bound-to-role-6
    public void test215_boundToRole6() {
       
       runConformTest(
            new String[] {
		"Team215btr6_2.java",
			    "\n" +
			    "public team class Team215btr6_2 {\n" +
			    "    private final Team215btr6_1 t = new Team215btr6_1();\n" +
			    "\n" +
			    "    @SuppressWarnings(\"roletypesyntax\")\n" +
			    "    public class Role215btr6_2 playedBy t.Role215btr6_1 {\n" +
			    "        public abstract String getValue();\n" +
			    "	@SuppressWarnings(\"decapsulation\")\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "		callin void logSetting() {\n" +
			    "			base.logSetting();\n" +
			    "			System.out.print(getValue());\n" +
			    "		}\n" +
			    "    	logSetting <- replace setValueInternal;\n" +
			    "	}\n" +
			    "    public void test() {\n" +
			    "        t.new Role215btr6_1().doIt();\n" +
			    "		\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "		Team215btr6_2 t2 = new Team215btr6_2();\n" +
			    "        t2.activate();\n" +
			    "		t2.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team215btr6_1.java",
			    "\n" +
			    "public team class Team215btr6_1 {\n" +
			    "	public Role215btr6_1 getRole() {\n" +
			    "		return new Role215btr6_1();\n" +
			    "	}\n" +
			    "    public class Role215btr6_1 {\n" +
			    "		private String valueInternal = \"XK\";\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return valueInternal;\n" +
			    "        }\n" +
			    "		protected void setValueInternal(String value) {\n" +
			    "			valueInternal = value;\n" +
			    "		}\n" +
			    "		public void doIt() {\n" +
			    "	 		setValueInternal(\"OK\");\n" +
			    "		}\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to another role in a different team using an anchored type - after callin to callin method
    // 2.1.5-otjld-bound-to-role-7
    public void test215_boundToRole7() {
       
       runConformTest(
            new String[] {
		"Team215btr7_2.java",
			    "\n" +
			    "public team class Team215btr7_2 {\n" +
			    "    private final Team215btr7_1 t = new Team215btr7_1();\n" +
			    "\n" +
			    "    public class Role215btr7_2 playedBy Role215btr7_1<@t> {\n" +
			    "        public abstract String getValue();\n" +
			    "	@SuppressWarnings(\"decapsulation\")\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "		protected void logSetting() {\n" +
			    "			System.out.print(\"old: \"+getValue());\n" +
			    "		}\n" +
			    "    	logSetting <- after/*before*/ setValueInternal;\n" +
			    "	}\n" +
			    "    public void test() {\n" +
			    "		t.activate();\n" +
			    "		T215btr7 b = new T215btr7();\n" +
			    "		b.setState(\"NOTOK\");\n" +
			    "		System.out.print(\"new: \"+b.getState());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "		Team215btr7_2 t2 = new Team215btr7_2();\n" +
			    "        t2.activate();\n" +
			    "		t2.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T215btr7.java",
			    "\n" +
			    "public class T215btr7 {\n" +
			    "	private String state=\"\";\n" +
			    "	public String getState() {\n" +
			    "		return state;\n" +
			    "	}\n" +
			    "	public void setState(String _state) {\n" +
			    "		state=_state;\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n",
		"Team215btr7_1.java",
			    "\n" +
			    "public team class Team215btr7_1 {\n" +
			    "    public class Role215btr7_1 playedBy T215btr7 {\n" +
			    "	\n" +
			    "	private String valueInternal = \"\";\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return valueInternal;\n" +
			    "        }\n" +
			    "		callin void setValueInternal(String value) {\n" +
			    "			valueInternal = value;\n" +
			    "			base.setValueInternal(\"OK\");\n" +
			    "		}\n" +
			    "		setValueInternal <- replace setState;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "old: NOTOKnew: OK");
    }

    // a role is bound to another role in a different team using an anchored type - before callin to after callin
    // 2.1.5-otjld-bound-to-role-8
    public void test215_boundToRole8() {
       
       runConformTest(
            new String[] {
		"Team215btr8_2.java",
			    "\n" +
			    "public team class Team215btr8_2 {\n" +
			    "    private final Team215btr8_1 t = new Team215btr8_1();\n" +
			    "\n" +
			    "    public class Role215btr8_2 playedBy Role215btr8_1<@t> {\n" +
			    "   		protected void printO() {\n" +
			    "			System.out.print(\"O\");\n" +
			    "		}\n" +
			    "    	printO <- before printK;\n" +
			    "	}\n" +
			    "    public void test() {\n" +
			    "		t.activate();\n" +
			    "		T215btr8 b = new T215btr8();\n" +
			    "		b.doIt();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "		Team215btr8_2 t2 = new Team215btr8_2();\n" +
			    "        t2.activate();\n" +
			    "		t2.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T215btr8.java",
			    "\n" +
			    "public class T215btr8 {\n" +
			    "	public void doIt() {\n" +
			    "	    System.out.print(\"Everything is \");\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n",
		"Team215btr8_1.java",
			    "\n" +
			    "public team class Team215btr8_1 {\n" +
			    "    public class Role215btr8_1 playedBy T215btr8 {\n" +
			    "		public void printK() {\n" +
			    "		    System.out.print(\"K\");\n" +
			    "		}\n" +
			    "		printK <- after doIt;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "Everything is OK");
    }

    // a role is bound to another role in a different team using an anchored type - replace callin to callin method
    // 2.1.5-otjld-bound-to-role-9
    public void test215_boundToRole9() {
       Map customOptions = getCompilerOptions();
       
       runConformTest(
            new String[] {
		"Team215btr9_2.java",
			    "\n" +
			    "public team class Team215btr9_2 {\n" +
			    "    private final Team215btr9_1 t = new Team215btr9_1();\n" +
			    "\n" +
			    "    public class Role215btr9_2 playedBy Role215btr9_1<@t> {\n" +
			    "        public abstract String getValue();\n" +
			    "	@SuppressWarnings(\"decapsulation\")\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "        callin void logSetting() {\n" +
			    "                base.logSetting();\n" +
			    "                System.out.print(\"old: \"+getValue());\n" +
			    "        }\n" +
			    "        @SuppressWarnings(\"decapsulation\")\n" +
			    "    	logSetting <- replace setValueInternal;\n" +
			    "	}\n" +
			    "    public void test() {\n" +
			    "		t.activate();\n" +
			    "		T215btr9 b = new T215btr9();\n" +
			    "		b.setState(\"NOTOK\");\n" +
			    "		System.out.print(\"new: \"+b.getState());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "		Team215btr9_2 t2 = new Team215btr9_2();\n" +
			    "        t2.activate();\n" +
			    "		t2.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T215btr9.java",
			    "\n" +
			    "public class T215btr9 {\n" +
			    "	private String state=\"\";\n" +
			    "	public String getState() {\n" +
			    "		return state;\n" +
			    "	}\n" +
			    "	public void setState(String _state) {\n" +
			    "		state=_state;\n" +
			    "	}\n" +
			    "}\n" +
			    "    \n",
		"Team215btr9_1.java",
			    "\n" +
			    "public team class Team215btr9_1 {\n" +
			    "    public class Role215btr9_1 playedBy T215btr9 {\n" +
			    "	\n" +
			    "	private String valueInternal = \"\";\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return valueInternal;\n" +
			    "        }\n" +
			    "		callin void setValueInternal(String value) {\n" +
			    "			valueInternal = value;\n" +
			    "			base.setValueInternal(\"OK\");\n" +
			    "		}\n" +
			    "		setValueInternal <- replace setState;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "old: NOTOKnew: OK",
            null/*classLibraries*/,
            true/*shouldFlushOutputDirectory*/,
            null/*vmArguments*/,
            customOptions,
            null/*no custom requestor*/);
    }

    // two instances of lower team exist, only one is adapted by an active team
    // 2.1.5-otjld-bound-to-role-10
    public void test215_boundToRole10() {
       
       runConformTest(
            new String[] {
		"T215btr10Main.java",
			    "\n" +
			    "public class T215btr10Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team215btr10_1 t1 = new Team215btr10_1();\n" +
			    "        //Team215btr10_1 t2 = new Team215btr10_1();\n" +
			    "        new Team215btr10_2(t1); // this one adapts t1\n" +
			    "        new Team215btr10_2(t1); // this one adapts t1, too\n" +
			    "        new Team215btr10_2(null);// this one has no effect\n" +
			    "        t1.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team215btr10_1.java",
			    "\n" +
			    "public team class Team215btr10_1 { \n" +
			    "    protected class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team215btr10_2.java",
			    "\n" +
			    "public team class Team215btr10_2 {\n" +
			    "    final Team215btr10_1 other;\n" +
			    "    Team215btr10_2(Team215btr10_1 other) {\n" +
			    "        this.other = other;\n" +
			    "        this.activate();\n" +
			    "    }\n" +
			    "    @SuppressWarnings({\"roletypesyntax\",\"decapsulation\"}) // protected base class (role)\n" +
			    "    protected class R2 playedBy other.R {\n" +
			    "        void k() { \n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "        k <- after test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OKK");
    }

    // two instances of lower team exist, only one is adapted by an active team -- playedBy from binary
    // 2.1.5-otjld_bound-to-role-11
    public void _bound_test215_toRole11() {
       
       runConformTest(
            new String[] {
		"T215btr11Main.java",
			    "\n" +
			    "public class T215btr11Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team215btr11_1 t1 = new Team215btr11_1();\n" +
			    "        //Team215btr11_1 t2 = new Team215btr11_1();\n" +
			    "        new Team215btr11_3(t1); // this one adapts t1\n" +
			    "        new Team215btr11_3(t1); // this one adapts t1, too\n" +
			    "        new Team215btr11_3(null);// this one has no effect\n" +
			    "        t1.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team215btr11_1.java",
			    "\n" +
			    "public team class Team215btr11_1 {\n" +
			    "    protected class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team215btr11_2.java",
			    "\n" +
			    "public team class Team215btr11_2 {\n" +
			    "    final Team215btr11_1 other;\n" +
			    "    Team215btr11_2(Team215btr11_1 other) {\n" +
			    "        this.other = other;\n" +
			    "        this.activate();\n" +
			    "    }\n" +
			    "    protected class R2 playedBy R<@other> {\n" +
			    "        // need to store in class file the type of this role's cache:\n" +
			    "        // DoublyWeakHashmap<R<@other>, R2<@tthis>> ! record as s.t. like @NoAnchor<@other,@tthis> ?\n" +
			    "        void k() { \n" +
			    "            System.out.print(\"K\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team215btr11_3.java",
			    "\n" +
			    "public team class Team215btr11_3 extends Team215btr11_2 {\n" +
			    "    Team215btr11_3(Team215btr11_1 other) {\n" +
			    "        super(other);\n" +
			    "    }\n" +
			    "    protected class R2 {\n" +
			    "        public R2(R<@other> myBase) { /* help the compiler. */ }\n" +
			    "        k <- after test;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OKK");
    }

    // multilevel baseclass refinement + accumulating base imported bases
    // 2.1.5-otjld_bound-to-role-12
    public void _bound_test215_toRole12() {
       
       runConformTest(
            new String[] {
		"Team215btr12_4.java",
			    "\n" +
			    "import base p2.Team215btr12_2;\n" +
			    "public team class Team215btr12_4 extends Team215btr12_3 {\n" +
			    "    public team class Mid2 extends Mid1 playedBy Team215btr12_2 {\n" +
			    "        //public class Role1 playedBy Role215btr12_1<@base> {}\n" +
			    "        public class Role2 extends Role1 playedBy Role215btr12_2<@base> {\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            String getValue() => String getValueInternal();\n" +
			    "        }\n" +
			    "        public void test() {\n" +
			    "            System.out.print(getValue(new Role215btr12_2<@base>()));\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test() {\n" +
			    "        new Mid2(new Team215btr12_2()).test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team215btr12_2 t = new Team215btr12_2();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team215btr12_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team215btr12_1 {\n" +
			    "    public class Role215btr12_1 {\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return \"OK\";\n" +
			    "       }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/Team215btr12_2.java",
			    "\n" +
			    "package p2;\n" +
			    "import p1.Team215btr12_1;\n" +
			    "public team class Team215btr12_2 extends Team215btr12_1 {\n" +
			    "    public class Role215btr12_1 {\n" +
			    "    }\n" +
			    "    public class Role215btr12_2 extends Role215btr12_1 {\n" +
			    "        protected String getValueInternal() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team215btr12_3.java",
			    "\n" +
			    "import base p1.Team215btr12_1;\n" +
			    "public team class Team215btr12_3 {\n" +
			    "    public team class Mid1 playedBy Team215btr12_1 {\n" +
			    "        public class Role1 playedBy Role215btr12_1<@base> {\n" +
			    "            public String getValue() { return \"NOK\"; }\n" +
			    "        }\n" +
			    "        public String getValue(Role215btr12_1<@base> as Role1 r) {\n" +
			    "            return r.getValue();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to the same base class as its implicit super role
    // 2.1.6-otjld-same-player-as-superrole-1
    public void test216_samePlayerAsSuperrole1() {
       
       runConformTest(
            new String[] {
		"T216spas1Main.java",
			    "\n" +
			    "public class T216spas1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team216spas1_2 t = new Team216spas1_2();\n" +
			    "        T216spas1      o = new T216spas1();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T216spas1.java",
			    "\n" +
			    "public class T216spas1 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team216spas1_1.java",
			    "\n" +
			    "public team class Team216spas1_1 {\n" +
			    "    public class Role216spas1 playedBy T216spas1 {\n" +
			    "        public String getValue() {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team216spas1_2.java",
			    "\n" +
			    "public team class Team216spas1_2 extends Team216spas1_1 {\n" +
			    "    public class Role216spas1 playedBy T216spas1 {\n" +
			    "        String getValue() => String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue(T216spas1 as Role216spas1 r) {\n" +
			    "        return r.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is implicitly bound to the same base class (a friendly nested class) as its implicit super role
    // 2.1.6-otjld-same-player-as-superrole-2
    public void test216_samePlayerAsSuperrole2() {
       myWriteFiles(
    		new String[] {
		"p1/T216spas2.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T216spas2 {\n" +
			    "    public static class Nested216spas2 {\n" +
			    "        public String getValueInternal() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n"    		   
       		});
       runConformTest(
            new String[] {
		"p1/T216spas2Main.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T216spas2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        p2.Team216spas2_2        t = new p2.Team216spas2_2();\n" +
			    "        T216spas2.Nested216spas2 o = new T216spas2.Nested216spas2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/Team216spas2_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team216spas2_1 {\n" +
			    "    public class Role216spas2 playedBy T216spas2.Nested216spas2 {\n" +
			    "        public String getValue() {\n" +
			    "            return \"NOTOK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public String getValue(T216spas2.Nested216spas2 as Role216spas2 r) {\n" +
			    "        return r.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p2/Team216spas2_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team216spas2_2 extends p1.Team216spas2_1 {\n" +
			    "    @Override\n" +
			    "    public class Role216spas2 {\n" +
			    "        String getValue() => String getValueInternal();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK",
            null,/*class libraries*/
            false,/*flushOutputDirectory*/
            null/*vmarguments*/);
    }

    // a role is bound to the same base class (an inner class in the same enclosing normal class) as its explicit super role
    // 2.1.6-otjld-same-player-as-superrole-3
    public void test216_samePlayerAsSuperrole3() {
       
       runConformTest(
            new String[] {
		"T216spas3Main.java",
			    "\n" +
			    "public class T216spas3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        T216spas3 o = new T216spas3();\n" +
			    "\n" +
			    "        System.out.print(o.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T216spas3.java",
			    "\n" +
			    "public class T216spas3 {\n" +
			    "    private class Inner216spas3 {\n" +
			    "        public String getValueInternal() {\n" +
			    "            return \"OK\";\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public team class Team216spas3 {\n" +
			    "        public class Role216spas3_1 playedBy Inner216spas3 {\n" +
			    "            public String getValue() {\n" +
			    "                return \"NOTOK\";\n" +
			    "            }\n" +
			    "        }\n" +
			    "        public class Role216spas3_2 extends Role216spas3_1 playedBy Inner216spas3 {\n" +
			    "            String getValue() => String getValueInternal();\n" +
			    "        }\n" +
			    "    \n" +
			    "        public String getValue(Inner216spas3 as Role216spas3_2 r) {\n" +
			    "            return r.getValue();\n" +
			    "        }\n" +
			    "    }\n" +
			    "\n" +
			    "    public String getValue() {\n" +
			    "        Team216spas3  t = new Team216spas3();\n" +
			    "        Inner216spas3 o = new Inner216spas3();\n" +
			    "\n" +
			    "        return t.getValue(o);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to the subclass of the base class of its explicit superrole
    // 2.1.7-otjld-bound-to-subclass-1
    public void test217_boundToSubclass1() {
       
       runConformTest(
            new String[] {
		"T217bts1Main.java",
			    "\n" +
			    "public class T217bts1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team217bts1 t = new Team217bts1();\n" +
			    "        T217bts1_2  o = new T217bts1_2();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T217bts1_1.java",
			    "\n" +
			    "public class T217bts1_1 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T217bts1_2.java",
			    "\n" +
			    "public class T217bts1_2 extends T217bts1_1 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team217bts1.java",
			    "\n" +
			    "public team class Team217bts1 {\n" +
			    "    public class Role217bts1_1 playedBy T217bts1_1 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "\n" +
			    "    public class Role217bts1_2 extends Role217bts1_1 playedBy T217bts1_2 {}\n" +
			    "\n" +
			    "    public String getValue(T217bts1_2 as Role217bts1_2 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to the subclass of the base class of its explicit superrole
    // 2.1.7-otjld-bound-to-subclass-2
    public void test217_boundToSubclass2() {
       
       runConformTest(
            new String[] {
		"T217bts2Main.java",
			    "\n" +
			    "public class T217bts2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team217bts2_2 t = new Team217bts2_2();\n" +
			    "        T217bts2_4    o = new T217bts2_4();\n" +
			    "\n" +
			    "        System.out.print(t.getValue(o));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T217bts2_1.java",
			    "\n" +
			    "public class T217bts2_1 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"NOTOK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T217bts2_2.java",
			    "\n" +
			    "public class T217bts2_2 extends T217bts2_1 {\n" +
			    "}\n" +
			    "    \n",
		"T217bts2_3.java",
			    "\n" +
			    "public class T217bts2_3 extends T217bts2_2 {}\n" +
			    "    \n",
		"T217bts2_4.java",
			    "\n" +
			    "public class T217bts2_4 extends T217bts2_3 {\n" +
			    "    public String getValueInternal() {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team217bts2_1.java",
			    "\n" +
			    "public team class Team217bts2_1 {\n" +
			    "    public class Role217bts2_1 playedBy T217bts2_1 {\n" +
			    "        protected abstract String getValue();\n" +
			    "        String getValue() -> String getValueInternal();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team217bts2_2.java",
			    "\n" +
			    "public team class Team217bts2_2 extends Team217bts2_1 {\n" +
			    "    public class Role217bts2_2 extends Role217bts2_1 playedBy T217bts2_3 {}\n" +
			    "\n" +
			    "    public String getValue(T217bts2_3 as Role217bts2_2 obj) {\n" +
			    "        return obj.getValue();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a role is bound to a class that is unrelated to the base class of its explicit superrole
    // 2.1.8-otjld-not-bound-to-subclass-1
    public void test218_notBoundToSubclass1() {
        runNegativeTest(
            new String[] {
		"T218nbts1_1.java",
			    "\n" +
			    "public class T218nbts1_1 {}\n" +
			    "    \n",
		"T218nbts1_2.java",
			    "\n" +
			    "public class T218nbts1_2 {}\n" +
			    "    \n",
		"Team218nbts1.java",
			    "\n" +
			    "public team class Team218nbts1 {\n" +
			    "    public class Role218nbts1_1 playedBy T218nbts1_1 {}\n" +
			    "    public class Role218nbts1_2 extends Role218nbts1_1 playedBy T218nbts1_2 {}\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role is bound to a superclass of the base class of its explicit superrole
    // 2.1.8-otjld-not-bound-to-subclass-2
    public void test218_notBoundToSubclass2() {
        runNegativeTest(
            new String[] {
		"T218nbts2_1.java",
			    "\n" +
			    "public class T218nbts2_1 {}\n" +
			    "    \n",
		"T218nbts2_2.java",
			    "\n" +
			    "public class T218nbts2_2 extends T218nbts2_1 {}\n" +
			    "    \n",
		"Team218nbts2.java",
			    "\n" +
			    "public team class Team218nbts2 {\n" +
			    "    public class Role218nbts2_1 playedBy T218nbts2_2 {}\n" +
			    "    public class Role218nbts2_2 extends Role218nbts2_1 playedBy T218nbts2_1 {}\n" +
			    "}\n" +
			    "    \n"
            },
            null);
    }

    // a role is bound to class that has the same short name as the base class of its explicit superrole, but is unrelated to it
    // 2.1.8-otjld-not-bound-to-subclass-3
    public void test218_notBoundToSubclass3() {
        runNegativeTestMatching(
            new String[] {
		"p2/Team218nbts3_2.java",
			    "\n" +
			    "package p2;\n" +
			    "public team class Team218nbts3_2 extends p1.Team218nbts3_1 {\n" +
			    "    public class Role218nbts3_2 extends Role218nbts3_1 playedBy T218nbts3 {}\n" +
			    "}\n" +
			    "    \n",
		"p1/T218nbts3.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T218nbts3 {}\n" +
			    "    \n",
		"p2/T218nbts3.java",
			    "\n" +
			    "package p2;\n" +
			    "public class T218nbts3 {}\n" +
			    "    \n",
		"p1/Team218nbts3_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public team class Team218nbts3_1 {\n" +
			    "    public class Role218nbts3_1 playedBy T218nbts3 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "2.1(c)");
    }

    // a role binds to a subclass of the base class of its implicit superrole
    // 2.1.9-otjld-playedby-inheritance-1
    public void test219_playedbyInheritance1() {
        runNegativeTestMatching(
            new String[] {
		"Team219pi1_2.java",
			    "\n" +
			    "public team class Team219pi1_2 extends Team219pi1_1  {\n" +
			    "    @Override\n" +
			    "    public class Role219pi1 playedBy T219pi1_2 {}\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"T219pi1_1.java",
			    "\n" +
			    "public class T219pi1_1 {}\n" +
			    "    \n",
		"T219pi1_2.java",
			    "\n" +
			    "public class T219pi1_2 extends T219pi1_1 {}\n" +
			    "    \n",
		"Team219pi1_1.java",
			    "\n" +
			    "public team class Team219pi1_1 {\n" +
			    "    public class Role219pi1 playedBy T219pi1_1 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "2.1(d)");
    }

    // a role binds to another class than the base class of its implicit superrole
    // 2.1.9-otjld-playedby-inheritance-2
    public void test219_playedbyInheritance2() {
        runNegativeTestMatching(
            new String[] {
		"Team219pi2_2.java",
			    "\n" +
			    "public team class Team219pi2_2 extends Team219pi2_1  {\n" +
			    "    public class Role219pi2_2 playedBy T219pi2_2 {}\n" +
			    "\n" +
			    "}\n" +
			    "    \n",
		"T219pi2_1.java",
			    "\n" +
			    "public class T219pi2_1 {}\n" +
			    "    \n",
		"T219pi2_2.java",
			    "\n" +
			    "public class T219pi2_2 {}\n" +
			    "    \n",
		"Team219pi2_1.java",
			    "\n" +
			    "public team class Team219pi2_1 {\n" +
			    "    public class Role219pi2_1 playedBy T219pi2_1 {}\n" +
			    "    public class Role219pi2_2 extends Role219pi2_1 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "2.1(c)");
    }

    // a role inherits different incompatible base classes
    // 2.1.9-otjld-playedby-inheritance-3
    public void test219_playedbyInheritance3() {
        runNegativeTestMatching(
            new String[] {
		"Team219pi3.java",
			    "\n" +
			    "public abstract team class Team219pi3 {\n" +
			    "    protected interface IR playedBy T219pi3_1 {}\n" +
			    "    protected class R0 playedBy T219pi3_2 {}\n" +
			    "    protected class R1 extends R0 implements IR {\n" +
			    "        R1() { \n" +
			    "            super(new T219pi3_2());\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T219pi3_1.java",
			    "\n" +
			    "public class T219pi3_1 {}\n" +
			    "    \n",
		"T219pi3_2.java",
			    "\n" +
			    "public class T219pi3_2 {}\n" +
			    "    \n"
            },
            "2.1(c)");
    }

    // a role inherits different, but compatible base classes
    // 2.1.9-otjld-playedby-inheritance-4
    public void test219_playedbyInheritance4() {
       
       runConformTest(
            new String[] {
		"Team219pi4.java",
			    "\n" +
			    "@SuppressWarnings(\"abstractrelevantrole\")\n" +
			    "public team class Team219pi4 {\n" +
			    "    // diamond root:\n" +
			    "    protected interface IR0 playedBy T219pi4_1 {}\n" +
			    "    // diamond branches:\n" +
			    "    protected class R0  implements IR0 playedBy T219pi4_2 {}\n" +
			    "    protected class IR1 implements IR0 playedBy T219pi4_3 {}\n" +
			    "    // merge:\n" +
			    "    protected class R1 extends R0 implements IR1 {\n" +
			    "        protected R1() { \n" +
			    "            super(new T219pi4_3());\n" +
			    "        }\n" +
			    "        protected void test() -> void say();\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        new R1().test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team219pi4().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T219pi4_1.java",
			    "\n" +
			    "public abstract class T219pi4_1 {\n" +
			    "    void say() {\n" +
			    "        System.out.print(\"nay\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T219pi4_2.java",
			    "\n" +
			    "public class T219pi4_2 extends T219pi4_1 {}\n" +
			    "    \n",
		"T219pi4_3.java",
			    "\n" +
			    "public class T219pi4_3 extends T219pi4_2 {\n" +
			    "    void say() {\n" +
			    "        System.out.print(\"yes\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "yes");
    }

    // a role implicitly inherits and uses a playedby relation unchanged
    // 2.1.9-otjld-playedby-inheritance-5
    public void test219_playedbyInheritance5() {
       
       runConformTest(
            new String[] {
		"Team219pi5_2.java",
			    "\n" +
			    "public team class Team219pi5_2 extends Team219pi5_1 {\n" +
			    "    protected class R {\n" +
			    "        public void test() -> void test();\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        R r= new R(new T219pi5());\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team219pi5_2 t= new Team219pi5_2();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T219pi5.java",
			    "\n" +
			    "public class T219pi5 {\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team219pi5_1.java",
			    "\n" +
			    "public team class Team219pi5_1 {\n" +
			    "    protected class R playedBy T219pi5 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a team imports a base class using a base-import
    // 2.1.10-otjld-base-import-1
    public void test2110_baseImport1() {
       
       runConformTest(
            new String[] {
		"Team2110bi1.java",
			    "\n" +
			    "import base p1.T2110bi1;\n" +
			    "public team class Team2110bi1 {\n" +
			    "    protected class R playedBy T2110bi1 {\n" +
			    "        void t() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "        t <- before test;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2110bi1().activate();\n" +
			    "        new p1.T2110bi1().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T2110bi1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T2110bi1 {\n" +
			    "    public void test() { \n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a team imports a base class using a base-import - base class decapsulation
    // 2.1.10-otjld-base-import-1d
    public void test2110_baseImport1d() {
       
       runConformTest(
            new String[] {
		"Team2110bi1d.java",
			    "\n" +
			    "import base p1.T2110bi1d_2;\n" +
			    "@SuppressWarnings(\"decapsulation\")\n" +
			    "public team class Team2110bi1d {\n" +
			    "    protected class R playedBy T2110bi1d_2 {\n" +
			    "        void t() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "        }\n" +
			    "        t <- before test;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2110bi1d().activate();\n" +
			    "        new p1.T2110bi1d_1().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T2110bi1d_1.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T2110bi1d_1 {\n" +
			    "    public void test() { \n" +
			    "        new T2110bi1d_2().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "class T2110bi1d_2 {\n" +
			    "    public void test() { \n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a team imports a class using a base-import - used in non-base position
    // 2.1.10-otjld-base-import-2
    public void test2110_baseImport2() {
        runNegativeTestMatching(
            new String[] {
		"Team2110bi2.java",
			    "\n" +
			    "import base p1.T2110bi2;\n" +
			    "public team class Team2110bi2 {\n" +
			    "    void wrong(T2110bi2 arg) { }\n" +
			    "}\n" +
			    "    \n",
		"p1/T2110bi2.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T2110bi2 {\n" +
			    "    public void test() { \n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "cannot be resolved to a type");
    }

    // a team imports a base class using a base-import -- using in declared lifting
    // 2.1.10-otjld-base-import-3
    public void test2110_baseImport3() {
       
       runConformTest(
            new String[] {
		"Team2110bi3.java",
			    "\n" +
			    "import base p1.T2110bi3;\n" +
			    "public team class Team2110bi3 {\n" +
			    "    protected class R playedBy T2110bi3 {\n" +
			    "        protected void t() {\n" +
			    "            System.out.print(\"O\");\n" +
			    "            test();\n" +
			    "        }\n" +
			    "        void test() -> void test();\n" +
			    "    }\n" +
			    "    Team2110bi3(T2110bi3 as R o) {\n" +
			    "        o.t();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        p1.T2110bi3 b = new p1.T2110bi3();\n" +
			    "        new Team2110bi3(b);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"p1/T2110bi3.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T2110bi3 {\n" +
			    "    public void test() { \n" +
			    "        System.out.print(\"K\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a regular class uses a base import
    // 2.1.10-otjld-base-import-4
    public void test2110_baseImport4() {
        runNegativeTestMatching(
            new String[] {
		"T2110bi4.java",
			    "\n" +
			    "import base javax.swing.JFrame;\n" +
			    "public class T2110bi4 {\n" +
			    "    JFrame frame;\n" +
			    "}\n" +
			    "    \n"
            },
            "2.1.2(d)");
    }

    // a base class is imported without "base"
    // 2.1.10-otjld-base-import-5
    public void test2110_baseImport5() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportBindingConventions, CompilerOptions.WARNING);
       customOptions.put(CompilerOptions.OPTION_ReportWeaveIntoSystemClass, CompilerOptions.WARNING);
        runTestExpectingWarnings(
            new String[] {
		"Team2110bi5.java",
			    "\n" +
			    "import javax.swing.JFrame;\n" +
			    "public team class Team2110bi5 {\n" +
			    "    @SuppressWarnings(\"bindingtosystemclass\")\n" +
			    "    protected class Role playedBy JFrame {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. WARNING in Team2110bi5.java (at line 5)\n" + 
			"	protected class Role playedBy JFrame {}\n" +
			"	                              ^^^^^^\n" +
			"It is recommended that base class JFrame be imported with the modifier \"base\" (OTJLD 2.1.2(d)).\n" +
			"----------\n");
    }

    // a base class is imported without "base", ignored
    // 2.1.10-otjld-base-import-6
    public void test2110_baseImport6() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportBindingConventions, CompilerOptions.IGNORE);
       customOptions.put(CompilerOptions.OPTION_ReportWeaveIntoSystemClass, CompilerOptions.WARNING);
        runTestExpectingWarnings(
            new String[] {
		"Team2110bi6.java",
			    "\n" +
			    "import javax.swing.JFrame;\n" +
			    "public team class Team2110bi6 {\n" +
			    "    protected class Role playedBy JFrame {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. WARNING in Team2110bi6.java (at line 4)\n" + 
    		"	protected class Role playedBy JFrame {}\n" + 
    		"	                              ^^^^^^\n" + 
    		"Base class javax.swing.JFrame appears to be a system class, which means that load time weaving could possibly fail\n" + 
    		"if this class is loaded from rt.jar/the bootstrap classpath.\n" + 
    		"----------\n",
    		customOptions);
    }

    // an unused base import should produce a warning
    // 2.1.10-otjld-base-import-7
    public void test2110_baseImport7() {
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
		"p1/T2110bi7.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T2110bi7 {}\n" +
			    "    \n",
		"Team2110bi7.java",
			    "\n" +
			    "import base p1.T2110bi7;\n" +
			    "import base java.util.List;\n" +
			    "public team class Team2110bi7 {\n" +
			    "    protected class R playedBy T2110bi7 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. WARNING in Team2110bi7.java (at line 3)\n" + 
    		"	import base java.util.List;\n" + 
    		"	            ^^^^^^^^^^^^^^\n" + 
    		"The import java.util.List is never used\n" + 
    		"----------\n",
    		customOptions);
    }

    // a base class is used by FQN instead of base import
    // 2.1.10-otjld-base-import-8
    public void test2110_baseImport8() {
       Map customOptions = getCompilerOptions();
       customOptions.put(CompilerOptions.OPTION_ReportBindingConventions, CompilerOptions.WARNING);
        runTestExpectingWarnings(
            new String[] {
		"p1/T2110bi8.java",
			    "\n" +
			    "package p1;\n" +
			    "public class T2110bi8 {}\n" +
			    "    \n",
		"Team2110bi8.java",
			    "\n" +
			    "public team class Team2110bi8 {\n" +
			    "    protected class R playedBy p1.T2110bi8 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" +
			"1. WARNING in Team2110bi8.java (at line 3)\n" +
			"	protected class R playedBy p1.T2110bi8 {}\n" +
			"	                           ^^^^^^^^^^^\n" +
			"Qualified reference to base class p1.T2110bi8 is deprecated, should use a base import instead (OTJLD 2.1.2(d)).\n" +
			"----------\n",
            customOptions);
    }

    // a role is playedBy a role of outer enclosing team - undefined constructor
    // 2.1.11-otjld-playedBy-within-team-1f
    public void test2111_playedByWithinTeam1f() {
        runNegativeTest(
            new String[] {
		"Team2111pwt1f.java",
			    "\n" +
			    "public team class Team2111pwt1f {\n" +
			    "    protected class Base {\n" +
			    "        void test() { System.out.print(\"O\"); }\n" +
			    "    }\n" +
			    "    protected team class Mid {\n" +
			    "        protected class R playedBy Base {\n" +
			    "            protected void test() {\n" +
			    "                tust();\n" +
			    "                System.out.print(\"K\");\n" +
			    "            }\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            void tust() -> void test();\n" +
			    "        }\n" +
			    "        Mid(Base b) {\n" +
			    "            @SuppressWarnings(\"roleinstantiation\")\n" +
			    "            R r = new R(b);\n" +
			    "            r.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2111pwt1f().new Mid();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team2111pwt1f.java (at line 22)\n" + 
    		"	new Team2111pwt1f().new Mid();\n" + 
    		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
    		"The constructor Team2111pwt1f.Mid() is undefined\n" + 
    		"----------\n");
    }

    // a role is playedBy a role of outer enclosing team
    // 2.1.11-otjld-playedBy-within-team-1
    public void test2111_playedByWithinTeam1() {
       
       runConformTest(
            new String[] {
		"Team2111pwt1.java",
			    "\n" +
			    "public team class Team2111pwt1 {\n" +
			    "    protected class Base {\n" +
			    "        void test() { System.out.print(\"O\"); }\n" +
			    "    }\n" +
			    "    protected team class Mid {\n" +
			    "        protected class R playedBy Base {\n" +
			    "            protected void test() {\n" +
			    "                tust();\n" +
			    "                System.out.print(\"K\");\n" +
			    "            }\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            void tust() -> void test();\n" +
			    "        }\n" +
			    "        public Mid() {\n" +
			    "            R r = new R(new Base());\n" +
			    "            r.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2111pwt1().new Mid();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a playedBy is refined by implicit inheritence, base is role of same enclosing
    // 2.1.12-otjld-implicitly-refined-playedBy-1
    public void test2112_implicitlyRefinedPlayedBy1() {
       
       runConformTest(
            new String[] {
		"Team2112irp1_2.java",
			    "\n" +
			    "public team class Team2112irp1_2 extends Team2112irp1_1 {\n" +
			    "    protected class Base {\n" +
			    "        void test() { \n" +
			    "            tsuper.test();\n" +
			    "            System.out.print(\"K\"); \n" +
			    "        }\n" +
			    "    }\n" +
			    "    protected team class Mid {\n" +
			    "        protected class R playedBy Base {\n" +
			    "            protected void test() {\n" +
			    "                tust();\n" +
			    "                System.out.print(\"!\");\n" +
			    "            }\n" +
			    "            @SuppressWarnings(\"decapsulation\")\n" +
			    "            void tust() -> void test();\n" +
			    "        }\n" +
			    "        public Mid() {\n" +
			    "            tsuper(new Base());\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2112irp1_2().new Mid();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2112irp1_1.java",
			    "\n" +
			    "public team class Team2112irp1_1 {\n" +
			    "    protected class Base {\n" +
			    "        void test() { System.out.print(\"O\"); }\n" +
			    "    }\n" +
			    "    protected team class Mid {\n" +
			    "        protected class R playedBy Base {\n" +
			    "            protected void test() -> void test();\n" +
			    "        }\n" +
			    "        Mid(Base b) {\n" +
			    "            R r = new R(b);\n" +
			    "            r.test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // a playedBy is refined by implicit inheritence, two levels of nesting
    // 2.1.12-otjld-implicitly-refined-playedBy-2
    public void test2112_implicitlyRefinedPlayedBy2() {
       
       runConformTest(
            new String[] {
		"Team2112irp2_2.java",
			    "\n" +
			    "public team class Team2112irp2_2 extends Team2112irp2_1 {\n" +
			    "    protected team class Outer {\n" +
			    "        protected class Base {\n" +
			    "            public void test() { \n" +
			    "                tsuper.test();\n" +
			    "                System.out.print(\"K\"); \n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected team class Mid {\n" +
			    "            protected class R playedBy Base {\n" +
			    "                public void test() {\n" +
			    "                    tust();\n" +
			    "                    System.out.print(\"!\");\n" +
			    "                }\n" +
			    "                void tust() -> void test();\n" +
			    "            }\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2112irp2_2().new Outer().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team2112irp2_1.java",
			    "\n" +
			    "public team class Team2112irp2_1 {\n" +
			    "    protected team class Outer {\n" +
			    "        protected class Base {\n" +
			    "            void test() { System.out.print(\"O\"); }\n" +
			    "        }\n" +
			    "        protected team class Mid {\n" +
			    "            protected class R playedBy Base {\n" +
			    "                @SuppressWarnings(\"decapsulation\")\n" +
			    "                protected void test() -> void test();\n" +
			    "            }\n" +
			    "            protected void test() {\n" +
			    "                R r = new R(new Base());\n" +
			    "                r.test();\n" +
			    "            }\n" +
			    "        }\n" +
			    "        protected void test() {\n" +
			    "            new Mid().test();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK!");
    }

    // a role migrates to a different base object, same type
    // 2.1.13-otjld-base-migration-1
    public void test2113_baseMigration1() {
       
       runConformTest(
            new String[] {
		"Team2113bm1.java",
			    "\n" +
			    "import org.objectteams.*;\n" +
			    "public team class Team2113bm1 {\n" +
			    "    protected class R implements IBaseMigratable playedBy T2113bm1 {\n" +
			    "        String getVal() -> get String val;\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"roleinstantiation\")\n" +
			    "    void test(T2113bm1 wrong, T2113bm1 ok) {\n" +
			    "        R r = new R(wrong);\n" +
			    "        r.migrateToBase(ok);\n" +
			    "        System.out.print(r.getVal());                           // OK (r bound to ok)\n" +
			    "        System.out.print(this.hasRole(ok));                     // true\n" +
			    "        System.out.print(this.getRole(ok, R.class).getVal());   // OK (can retrieve r)\n" +
			    "        System.out.print(this.hasRole(wrong));                  // false\n" +
			    "        r = null;\n" +
			    "        System.gc();\n" +
			    "        System.out.print(this.getAllRoles().length);            // 1: ok still claims role\n" +
			    "        ok = null;\n" +
			    "        System.gc();\n" +
			    "        System.out.print(this.getAllRoles().length);            // 0: no more reference\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2113bm1().test(new T2113bm1(\"wrong\"), new T2113bm1(\"OK\"));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2113bm1.java",
			    "\n" +
			    "public class T2113bm1 {\n" +
			    "    public String val;\n" +
			    "    T2113bm1(String v) { this.val = v; }\n" +
			    "}\n" +
			    "    \n"
            },
            "OKtrueOKfalse10");
    }

    // a role migrates to a different base object, unbound base type
    // 2.1.13-otjld-base-migration-2
    public void test2113_baseMigration2() {
        runNegativeTestMatching(
            new String[] {
		"Team2113bm2.java",
			    "\n" +
			    "import org.objectteams.*;\n" +
			    "public team class Team2113bm2 {\n" +
			    "    protected class R implements IBaseMigratable playedBy T2113bm2_1 {\n" +
			    "        String getVal() -> get String val;\n" +
			    "    }\n" +
			    "    @SuppressWarnings(\"roleinstantiation\")\n" +
			    "    void test(T2113bm2_1 wrong, T2113bm2_2 worse) {\n" +
			    "        R r = new R(wrong);\n" +
			    "        r.migrateToBase(worse);\n" +
			    "        System.out.print(r.getVal());\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team2113bm2().test(new T2113bm2_1(\"wrong\"), new T2113bm2_2(\"worse\"));\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T2113bm2_1.java",
			    "\n" +
			    "public class T2113bm2_1 {\n" +
			    "    public String val;\n" +
			    "    T2113bm2_1(String v) { this.val = v; }\n" +
			    "}\n" +
			    "    \n",
		"T2113bm2_2.java",
			    "\n" +
			    "public class T2113bm2_2 {\n" +
			    "    public String val;\n" +
			    "    T2113bm2_2(String v) { this.val = v; }\n" +
			    "}\n" +
			    "    \n"
            },
            "not applicable");
    }
    
    // see  https://bugs.eclipse.org/348082
    // [compiler] Internal Role pattern with deeply nested team gives compile error in generated code
    public void test2124_internalRole1() {
    	runConformTest(
    		new String[]{
    	"p2124/Team2124ir1.java",
    			"package p2124;\n" +
    			"public team class Team2124ir1 {\n" +
    			"    protected team class Mid {\n" + 
    			"         protected team class Inner playedBy Mid {\n" +
    			"\n" + 
    			"         }\n" + 
    			"   }\n" + 
    			"" +
    			"}\n"
    		});
    }

    // access to private static inner of an inaccessible outer via playedBy
    public void test2125_roleOfNestedClass1() {
    	runConformTest(
    		new String[] {
    	"p2125/base/T2125ronc1.java",
    			"package p2125.base;\n" +
    			"class T2125ronc1 {\n" +
    			"    private static class Inner {\n" +
    			"        void foo() {}\n" +
    			"    }\n" +
    			"    void bar(Inner i) { i.foo(); }\n" +
    			"}\n",
    	"p2125/teams/Team2125ronc1.java",
    			"package p2125.teams;\n" +
    			"import base p2125.base.T2125ronc1.Inner;\n" +
    			"@SuppressWarnings(\"decapsulation\")\n" +
    			"public team class Team2125ronc1 {\n" +
    			"    protected class R playedBy Inner {\n" +
    			"        void foo() -> void foo();\n" +
    			"    }\n" +
    			"}\n"
    		});
    }
}
