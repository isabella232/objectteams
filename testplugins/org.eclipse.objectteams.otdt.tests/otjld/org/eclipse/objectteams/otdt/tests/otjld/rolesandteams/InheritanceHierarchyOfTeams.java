/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2015 IT Service Omikron GmbH and others.
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
package org.eclipse.objectteams.otdt.tests.otjld.rolesandteams;

import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

import junit.framework.Test;

/**
 * Former jacks tests from section implicit-hierarchy-of-teams (1.2.*)
 * @author stephan 
 */
public class InheritanceHierarchyOfTeams extends AbstractOTJLDTest {

	public InheritanceHierarchyOfTeams(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test127_nonTeamSuperclass1" };
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return InheritanceHierarchyOfTeams.class;
	}

    // a team has an explicit org.objectteams.Team superclass
    // 1.2.1-otjld-explicit-team-superclass
    public void test121_explicitTeamSuperclass() {
        runConformTest(
            new String[] {
		"Team121ets.java",
			    "\n" +
			    "public team class Team121ets extends org.objectteams.Team {}\n" +
			    "    \n"
            });
    }

    // a class that is not a team has an explicit org.objectteams.Team superclass
    // 1.2.2-otjld-team-superclass-not-allowed
    public void test122_teamSuperclassNotAllowed() {
        runNegativeTest(
            new String[] {
		"Team122tsna.java",
			    "\n" +
			    "public class Team122tsna extends org.objectteams.Team {}\n" +
			    "    \n"
            },
            null);
    }

    // a team class without super team implements multiple interfaces
    // 1.2.3-otjld-multiple-interfaces-1
    public void test123_multipleInterfaces1() {
        runConformTest(
            new String[] {
		"T123mi1Main.java",
			    "\n" +
			    "public class T123mi1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team123mi1 t  = new Team123mi1();\n" +
			    "        T123mi1_1  i1 = t;\n" +
			    "        T123mi1_2  i2 = (T123mi1_2)i1;\n" +
			    "\n" +
			    "        System.out.print(i2.getValue());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T123mi1_1.java",
			    "\n" +
			    "public interface T123mi1_1 {\n" +
			    "    void doSomething();\n" +
			    "}\n" +
			    "    \n",
		"T123mi1_2.java",
			    "\n" +
			    "public interface T123mi1_2 {\n" +
			    "    String getValue();\n" +
			    "}\n" +
			    "    \n",
		"Team123mi1.java",
			    "\n" +
			    "public team class Team123mi1 implements T123mi1_1, T123mi1_2 {\n" +
			    "    public void doSomething()\n" +
			    "    {}\n" +
			    "    public String getValue()\n" +
			    "    {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a team class with super team implements multiple interfaces
    // 1.2.3-otjld-multiple-interfaces-2
    public void test123_multipleInterfaces2() {
        runConformTest(
            new String[] {
		"T123mi2Main.java",
			    "\n" +
			    "public class T123mi2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team123mi2_2 t = new Team123mi2_2();\n" +
			    "\n" +
			    "        System.out.print(t.test());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T123mi2_1.java",
			    "\n" +
			    "public interface T123mi2_1 {\n" +
			    "    String getValue1();\n" +
			    "}\n" +
			    "    \n",
		"T123mi2_2.java",
			    "\n" +
			    "public interface T123mi2_2 {\n" +
			    "    String getValue2(T123mi2_1 obj);\n" +
			    "}\n" +
			    "    \n",
		"Team123mi2_1.java",
			    "\n" +
			    "public team class Team123mi2_1 implements T123mi2_2 {\n" +
			    "    public String getValue2(T123mi2_1 obj)\n" +
			    "    {\n" +
			    "        return obj.getValue1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team123mi2_2.java",
			    "\n" +
			    "public team class Team123mi2_2 extends Team123mi2_1 implements T123mi2_1 {\n" +
			    "    public String getValue1()\n" +
			    "    {\n" +
			    "        return \"OK\";\n" +
			    "    }\n" +
			    "    public String test()\n" +
			    "    {\n" +
			    "        return getValue2(this);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a team class without super team is cast to org.objectteams.Team
    // 1.2.4-otjld-cast-to-Team-1
    public void test124_castToTeam1() {
        runConformTest(
            new String[] {
		"T124ctT1Main.java",
			    "\n" +
			    "public class T124ctT1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        org.objectteams.Team t = new Team124ctT1();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team124ctT1.java",
			    "\n" +
			    "public team class Team124ctT1 {}\n" +
			    "    \n"
            },
            "OK");
    }

    // a team class with super team is cast to org.objectteams.Team
    // 1.2.4-otjld-cast-to-Team-2
    public void test124_castToTeam2() {
        runConformTest(
            new String[] {
		"T124ctT2Main.java",
			    "\n" +
			    "public class T124ctT2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        org.objectteams.Team t = new Team124ctT2_2();\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team124ctT2_1.java",
			    "\n" +
			    "public team class Team124ctT2_1 {}\n" +
			    "    \n",
		"Team124ctT2_2.java",
			    "\n" +
			    "public team class Team124ctT2_2 extends Team124ctT2_1 {}\n" +
			    "    \n"
            },
            "OK");
    }

    // a team class is casted to its direct super team
    // 1.2.5-otjld-cast-to-supertam-1
    public void test125_castToSupertam1() {
        runConformTest(
            new String[] {
		"T125cts1Main.java",
			    "\n" +
			    "public class T125cts1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team125cts1_1 t1 = new Team125cts1_2();\n" +
			    "        Team125cts1_2 t2 = (Team125cts1_2)t1;\n" +
			    "\n" +
			    "        t2.activate();\n" +
			    "        t2.deactivate();\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team125cts1_1.java",
			    "\n" +
			    "public team class Team125cts1_1 {}\n" +
			    "    \n",
		"Team125cts1_2.java",
			    "\n" +
			    "public team class Team125cts1_2 extends Team125cts1_1 {}\n" +
			    "    \n"
            },
            "OK");
    }

    // a team class is casted to an indirect super team
    // 1.2.5-otjld-cast-to-supertam-2
    public void test125_castToSupertam2() {
        runConformTest(
            new String[] {
		"T125cts2Main.java",
			    "\n" +
			    "public class T125cts2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team125cts2_1 t1 = new Team125cts2_4();\n" +
			    "        Team125cts2_3 t2 = (Team125cts2_3)t1;\n" +
			    "\n" +
			    "        t2.activate();\n" +
			    "        t2.deactivate();\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team125cts2_1.java",
			    "\n" +
			    "public team class Team125cts2_1 {}\n" +
			    "    \n",
		"Team125cts2_2.java",
			    "\n" +
			    "public team class Team125cts2_2 extends Team125cts2_1 {}\n" +
			    "    \n",
		"Team125cts2_3.java",
			    "\n" +
			    "public team class Team125cts2_3 extends Team125cts2_2 {}\n" +
			    "    \n",
		"Team125cts2_4.java",
			    "\n" +
			    "public team class Team125cts2_4 extends Team125cts2_3 {}\n" +
			    "    \n"
            },
            "OK");
    }

    // a variable of a non-team type containg a team instance is cast to the team type
    // 1.2.6-otjld-cast-to-teamtype-1
    public void test126_castToTeamtype1() {
        runConformTest(
            new String[] {
		"T126ctt1Main.java",
			    "\n" +
			    "public class T126ctt1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Object      o = new Team126ctt1();\n" +
			    "        Team126ctt1 t = (Team126ctt1)o;\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team126ctt1.java",
			    "\n" +
			    "public team class Team126ctt1 {}\n" +
			    "    \n"
            },
            "OK");
    }

    // a variable of a non-team type containg a team instance is cast to org.objectteams.Team
    // 1.2.6-otjld-cast-to-teamtype-2
    public void test126_castToTeamtype2() {
        runConformTest(
            new String[] {
		"T126ctt2Main.java",
			    "\n" +
			    "public class T126ctt2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Cloneable            c = new Team126ctt2();\n" +
			    "        org.objectteams.Team t = (org.objectteams.Team)c;\n" +
			    "\n" +
			    "        t.activate();\n" +
			    "        t.deactivate();\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team126ctt2.java",
			    "\n" +
			    "public team class Team126ctt2 implements Cloneable {}\n" +
			    "    \n"
            },
            "OK");
    }

    // a team class has a regular class as its explicit superclass - this class is also bound as a role's base class - two level team hierarchy
    // 1.2.7-otjld-non-team-superclass-1
    public void test127_nonTeamSuperclass1a() {
        runConformTest(
            new String[] {
		"Team127nts1a_2.java",
			    "\n" +
			    "public team class Team127nts1a_2 extends Team127nts1a_1 {\n" +
			    "    public void hello() {\n" +
			    "        System.out.print(\"hello\");\n" +
			    "    }\n" +
			    "    public void world() {\n" +
			    "        System.out.print(\"world\");\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team127nts1a_2 t = new Team127nts1a_2();\n" +
			    "        t.activate();\n" +
			    "        t.hello();\n" +
			    "        t.world();\n" +
			    "        System.out.print(t.getAllRoles().length);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T127nts1a.java",
			    "\n" +
			    "public class T127nts1a {\n" +
			    "    public void hello() {\n" +
			    "        System.out.print(\"wrong\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team127nts1a_1.java",
			    "\n" +
			    "public team class Team127nts1a_1 extends T127nts1a {\n" +
			    "    protected class R playedBy T127nts1a {\n" +
			    "        void blank() {\n" +
			    "            System.out.print(\" \");\n" +
			    "        }\n" +
			    "        blank <- after hello;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "hello world1");
    }

    // replace callin:
    public void test127_nonTeamSuperclass1b() {
        runConformTest(
            new String[] {
		"Team127nts1b_2.java",
			    "\n" +
			    "public team class Team127nts1b_2 extends Team127nts1b_1 {\n" +
			    "    public void hello() {\n" +
			    "        System.out.print(\"hello\");\n" +
			    "    }\n" +
			    "    public void world() {\n" +
			    "        System.out.print(\"world\");\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team127nts1b_2 t = new Team127nts1b_2();\n" +
			    "        t.activate();\n" +
			    "        t.hello();\n" +
			    "        t.world();\n" +
			    "        System.out.print(t.getAllRoles().length);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T127nts1b.java",
			    "\n" +
			    "public class T127nts1b {\n" +
			    "    public void hello() {\n" +
			    "        System.out.print(\"wrong\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team127nts1b_1.java",
			    "\n" +
			    "public team class Team127nts1b_1 extends T127nts1b {\n" +
			    "    protected class R playedBy T127nts1b {\n" +
			    "        callin void blank() {\n" +
			    "            base.blank();\n" +
			    "            System.out.print(\" \");\n" +
			    "        }\n" +
			    "        blank <- replace hello;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "hello world1");
    }

    // before static
    public void test127_nonTeamSuperclass1c() {
        runConformTest(
            new String[] {
		"Team127nts1c_2.java",
			    "\n" +
			    "public team class Team127nts1c_2 extends Team127nts1c_1 {\n" +
			    "    public void hello() {\n" +
			    "        System.out.print(\"hello\");\n" +
			    "    }\n" +
			    "    public static void world() {\n" +
			    "        System.out.print(\"wrong\");\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team127nts1c_2 t = new Team127nts1c_2();\n" +
			    "        t.activate();\n" +
			    "        t.hello();\n" +
			    "        T127nts1c.world();\n" +
			    "        System.out.print(t.getAllRoles().length);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T127nts1c.java",
			    "\n" +
			    "public class T127nts1c {\n" +
			    "    public static void world() {\n" +
			    "        System.out.print(\"world\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team127nts1c_1.java",
			    "\n" +
			    "public team class Team127nts1c_1 extends T127nts1c {\n" +
			    "    protected class R playedBy T127nts1c {\n" +
			    "        static void blank() {\n" +
			    "            System.out.print(\" \");\n" +
			    "        }\n" +
			    "        blank <- before world;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "hello world0");
    }

    // a team class has a superclass that is not a team
    // 1.2.7-otjld-non-team-superclass-2
    public void test127_nonTeamSuperclass2() {
        runConformTest(
            new String[] {
		"T127nts2.java",
			    "\n" +
			    "public class T127nts2 {}\n" +
			    "    \n",
		"Team127nts2.java",
			    "\n" +
			    "public team class Team127nts2 extends T127nts2 {}\n" +
			    "    \n"
            });
    }
    
    // a nested team has a non-team superclass
    public void test127_nonTeamSuperclass3() {
    	runConformTest(
    		new String[] {
    	"Team127nts3.java",
    			"public team class Team127nts3 {\n" +
    			"	protected team class Inner extends T127nts3 {}\n" +
    			"	void test() {\n" +
    			"		new Inner().test();\n" +
    			"	}\n" +
    			"	public static void main(String... args) {\n" +
    			"		new Team127nts3().test();\n" +
    			"	}\n" +
    			"}\n",
    	"T127nts3.java",
    			"public class T127nts3 {\n" +
    			"	public void test() {\n" +
    			"		System.out.println(\"OK\");\n" +
    			"	}\n" +
    			"}\n"
    		},
    		"OK");
    }

    // a non-abstract team class has an interface that is does not implement
    // 1.2.8-otjld-interface-not-implemented
    public void test128_interfaceNotImplemented() {
        runNegativeTest(
            new String[] {
		"T128ini.java",
			    "\n" +
			    "public interface T128ini {\n" +
			    "    void doSomething();\n" +
			    "}\n" +
			    "    \n",
		"Team128ini.java",
			    "\n" +
			    "public team class Team128ini implements T128ini {}\n" +
			    "    \n"
            },
            null);
    }

    // role inheritance from super team and sub team together yields a circle - StackOverflowError reported by jogeb
    // 1.2.9-otjld-circular-role-inheritance-1
    public void test129_circularRoleInheritance1() {
        runNegativeTestMatching(
            new String[] {
		"Team129cri1_1.java",
			    "\n" +
			    "public team class Team129cri1_1 {\n" +
			    "    protected class R1 {}\n" +
			    "    protected class R2 extends R1 {}\n" +
			    "}\n" +
			    "    \n",
		"Team129cri1_2.java",
			    "\n" +
			    "public team class Team129cri1_2 extends Team129cri1_1 {\n" +
			    "    protected class R1 extends R2 {}\n" +
			    "    protected class R2 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "cycle");
    }

    public void testBug506740() {
    	runConformTest(
    		new String[] {
    			"b/Base.java",
    			"package b;\n" +
    			"public class Base {\n" +
    			"	private String f1 = \"f1\";\n" +
    			"	private String f2 = \"f2\";\n" +
    			"	private String f3 = \"f3\";\n" +
    			"	private String f4 = \"f4\";\n" +
    			"	private String f5 = \"f5\";\n" +
    			"}\n",
    			"t/Team1.java",
    			"package t;\n" +
    			"public team class Team1 {\n" +
    			"	@SuppressWarnings(\"decapsulation\")\n" +
    			"	protected class R playedBy b.Base {\n" +
    			"		protected R() { base(); }\n" +
    			"		protected String getF1() -> get String f1;\n" +
    			"		protected String getF2() -> get String f2;\n" +
    			"	}\n" +
    			"}\n"
    		});
    	runConformTest(
    		false, // no flush
    		new String[] {
    			"t/Team2.java",
    			"package t;\n" +
    			"public team class Team2 extends Team1 {\n" +
    			"	@Override\n" +
    			"	@SuppressWarnings(\"decapsulation\")\n" +
    			"	protected class R {\n" +
    			"		protected String getF3() -> get String f3;\n" +
    			"		protected String getF4() -> get String f4;\n" +
    			"	}\n" +
    			"	void test() {\n" +
    			"		R r = new R();\n" +
    			"		System.out.print(r.getF1()+r.getF2()+r.getF3()+r.getF4());\n" +
    			"	}\n" +
    			"	public static void main(String... args) {\n" +
    			"		new Team2().test();\n" +
    			"	}\n" +
    			"}\n"
    		},
    		"",
    		"f1f2f3f4",
    		"",
    		null /* javacOptions */);
    	runConformTest(
    		false, // no flush
    		new String[] {
    			"t/Team3.java",
    			"package t;\n" +
    			"public team class Team3 extends Team2 {\n" +
    			"	@Override\n" +
    			"	@SuppressWarnings(\"decapsulation\")\n" +
    			"	protected class R {\n" +
    			"		protected String getF5() -> get String f5;\n" +
    			"	}\n" +
    			"	void test() {\n" +
    			"		R r = new R();\n" +
    			"		System.out.print(r.getF1()+r.getF2()+r.getF3()+r.getF4()+r.getF5());\n" +
    			"	}\n" +
    			"	public static void main(String... args) {\n" +
    			"		new Team3().test();\n" +
    			"	}\n" +
    			"}\n"
    		},
    		"",
    		"f1f2f3f4f5",
    		"",
    		null /* javacOptions */);
    }
}
