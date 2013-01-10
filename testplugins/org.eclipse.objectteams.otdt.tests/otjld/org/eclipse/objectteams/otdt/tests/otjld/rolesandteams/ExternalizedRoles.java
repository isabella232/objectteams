/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010, 2013 Stephan Herrmann
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
package org.eclipse.objectteams.otdt.tests.otjld.rolesandteams;

import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

import junit.framework.Test;

public class ExternalizedRoles extends AbstractOTJLDTest {
	
	public ExternalizedRoles(String name) {
		super(name);
	}
	
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test166_typeAnchorIsPath9"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}
	
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return ExternalizedRoles.class;
	}
    // a type of a field is anchored to a simple field reference
    // 1.6.1-otjld-simple-anchored-type-1
    public void test161_simpleAnchoredType1() {
       
       runConformTest(
            new String[] {
		"T161sat1Main.java",
			    "\n" +
			    "public class T161sat1Main {\n" +
			    "	final Team161sat1 other = new Team161sat1();\n" +
			    "	Role<@other> role = other.getRole();\n" +
			    "	public void test () {\n" +
			    "		System.out.print(role.getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new T161sat1Main().test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team161sat1.java",
			    "\n" +
			    "public team class Team161sat1 {\n" +
			    "	public class Role {\n" +
			    "        public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role getRole() {\n" +
			    "		return new Role();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type of a field is anchored to a simple field reference - otherwise unused private field
    // 1.6.1-otjld-simple-anchored-type-1a
    public void test161_simpleAnchoredType1a() {
       
       runConformTest(
            new String[] {
		"T161sat1aMain.java",
			    "\n" +
			    "public class T161sat1aMain {\n" +
			    "	private final Team161sat1a other;\n" +
			    "	Role<@other> role;\n" +
			    "	public T161sat1aMain(final Team161sat1a t, Role<@t> r) {\n" +
			    "            other = t;\n" +
			    "            role = r;\n" +
			    "        }\n" +
			    "	public void test () {\n" +
			    "		System.out.print(role.getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "	       final Team161sat1a t = new Team161sat1a();\n" +
			    "	       new T161sat1aMain(t, t.getRole()).test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team161sat1a.java",
			    "\n" +
			    "public team class Team161sat1a {\n" +
			    "	public class Role {\n" +
			    "        public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role getRole() {\n" +
			    "		return new Role();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type of a field is anchored to a simple field reference - overloading plus varargs plus ambiguous lowering confuses AnchorMapping
    // 1.6.1-otjld-simple-anchored-type-1f
    public void test161_simpleAnchoredType1f() {
       
       runConformTest(
            new String[] {
		"T161sat1fMain.java",
			    "\n" +
			    "public class T161sat1fMain {\n" +
			    "	public void test(final Team161sat1f t, Role<@t> r) {\n" +
			    "	    final Team161sat1f other = t;\n" +
			    "	    Role<@other> role = r;\n" +
			    "            System.out.print(role.getValue());\n" +
			    "        }\n" +
			    "	public void test(final Team161sat1f t, Object r, String ... strings) {\n" +
			    "	    throw new RuntimeException(\"wrong method\");\n" +
			    "        }\n" +
			    "	public static void main(String[] args) {\n" +
			    "	       final Team161sat1f t = new Team161sat1f();\n" +
			    "	       T161sat1fMain m = new T161sat1fMain();\n" +
			    "	       m.test(t, t.getRole());\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T161sat1f.java",
			    "\n" +
			    "public class T161sat1f {}\n" +
			    "	\n",
		"Team161sat1f.java",
			    "\n" +
			    "public team class Team161sat1f {\n" +
			    "	public class Role playedBy T161sat1f {\n" +
			    "	    public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public class Role2 extends Role {\n" +
			    "	    public Role2() {\n" +
			    "		base();\n" +
			    "	    }\n" +
			    "	}\n" +
			    "	public Role2 getRole() {\n" +
			    "		return new Role2();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type of a local variable is anchored to a simple field reference
    // 1.6.1-otjld-simple-anchored-type-2
    public void test161_simpleAnchoredType2() {
       
       runConformTest(
            new String[] {
		"T161sat2Main.java",
			    "\n" +
			    "public class T161sat2Main {\n" +
			    "	final Team161sat2 other = new Team161sat2();\n" +
			    "	public void test () {\n" +
			    "		Role<@other> role = other.getRole();\n" +
			    "		System.out.print(role.getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new T161sat2Main().test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team161sat2.java",
			    "\n" +
			    "public team class Team161sat2 {\n" +
			    "	public class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role getRole() {\n" +
			    "		return new Role();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type of a parameter is anchored to a simple field reference
    // 1.6.1-otjld-simple-anchored-type-3
    public void test161_simpleAnchoredType3() {
       
       runConformTest(
            new String[] {
		"T161sat3Main.java",
			    "\n" +
			    "public class T161sat3Main {\n" +
			    "	static final Team161sat3 other = new Team161sat3();\n" +
			    "	public void test (Role<@other> role) {\n" +
			    "		System.out.print(role.getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new T161sat3Main().test(other.getRole());\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team161sat3.java",
			    "\n" +
			    "public team class Team161sat3 {\n" +
			    "	public class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role getRole() {\n" +
			    "		return new Role();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // qualified field reference has type of externalized role
    // 1.6.1-otjld-simple-anchored-type-4
    public void test161_simpleAnchoredType4() {
       
       runConformTest(
            new String[] {
		"Team161sat4_2.java",
			    "\n" +
			    "public team class Team161sat4_2 {\n" +
			    "    final Team161sat4_1 otherTeam;\n" +
			    "    public class R2 {\n" +
			    "        public R1<@otherTeam> r1;\n" +
			    "    }\n" +
			    "    public void test(R2 r2, R1<@otherTeam> r1) {\n" +
			    "        if (r2.r1 == r1)\n" +
			    "            r2.r1.print();\n" +
			    "    }\n" +
			    "    public Team161sat4_2(final Team161sat4_1 otherTeam) {\n" +
			    "        this.otherTeam = otherTeam;\n" +
			    "        R1<@otherTeam> r1 = otherTeam.new R1(); // R1<@this.otherTeam>\n" +
			    "        R2 r2 = new R2();\n" +
			    "        r2.r1 = r1;\n" +
			    "        test(r2, r1);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team161sat4_2(new Team161sat4_1());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team161sat4_1.java",
			    "\n" +
			    "public team class Team161sat4_1 {\n" +
			    "    public class R1 {\n" +
			    "        public void print() { System.out.print(\"OK\"); }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a type of an array field is anchored to a simple field reference
    // 1.6.2-otjld-anchored-array-type-1
    public void test162_anchoredArrayType1() {
       
       runConformTest(
            new String[] {
		"T162aat1Main.java",
			    "\n" +
			    "public class T162aat1Main {\n" +
			    "	final Team162aat1 other = new Team162aat1();\n" +
			    "	Role<@other>[] role = other.getRoles();\n" +
			    "	public void test () {\n" +
			    "		System.out.print(role[0].getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new T162aat1Main().test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team162aat1.java",
			    "\n" +
			    "public team class Team162aat1 {\n" +
			    "	public class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role[] getRoles() {\n" +
			    "		return new Role[]{new Role()};\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type of a local array variable is anchored to a simple field reference
    // 1.6.2-otjld-anchored-array-type-2
    public void test162_anchoredArrayType2() {
       
       runConformTest(
            new String[] {
		"T162aat2Main.java",
			    "\n" +
			    "public class T162aat2Main {\n" +
			    "	final Team162aat2 other = new Team162aat2();\n" +
			    "	public void test () {\n" +
			    "		Role<@other>[] roles = other.getRoles();\n" +
			    "		System.out.print(roles[0].getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new T162aat2Main().test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team162aat2.java",
			    "\n" +
			    "public team class Team162aat2 {\n" +
			    "	public class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role[] getRoles() {\n" +
			    "		return new Role[]{new Role()};\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type of a array parameter is anchored to a simple field reference
    // 1.6.2-otjld-anchored-array-type-3
    public void test162_anchoredArrayType3() {
       
       runConformTest(
            new String[] {
		"T162aat3Main.java",
			    "\n" +
			    "public class T162aat3Main {\n" +
			    "	static final Team162aat3 other = new Team162aat3();\n" +
			    "	public void test (Role<@other>[] roles) {\n" +
			    "		System.out.print(roles[0].getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new T162aat3Main().test(other.getRoles());\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team162aat3.java",
			    "\n" +
			    "public team class Team162aat3 {\n" +
			    "	public class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role[] getRoles() {\n" +
			    "		return new Role[]{new Role()};\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type of an array field is anchored to a simple field reference, reading array element
    // 1.6.2-otjld-anchored-array-type-4
    public void test162_anchoredArrayType4() {
       
       runConformTest(
            new String[] {
		"T162aat4Main.java",
			    "\n" +
			    "public class T162aat4Main {\n" +
			    "	final Team162aat4 other = new Team162aat4();\n" +
			    "	Role<@other>[] role = other.getRoles();\n" +
			    "	public void test () {\n" +
			    "		testOne(role[0]);\n" +
			    "	}\n" +
			    "	void testOne(Role<@other> r) {\n" +
			    "	   System.out.print(r.getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new T162aat4Main().test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team162aat4.java",
			    "\n" +
			    "public team class Team162aat4 {\n" +
			    "	public class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role[] getRoles() {\n" +
			    "		return new Role[]{new Role()};\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type of an array field is anchored to a simple field reference, reading array element, mismatching anchor
    // 1.6.2-otjld-anchored-array-type-4f
    public void test162_anchoredArrayType4f() {
        runNegativeTestMatching(
            new String[] {
		"T162aat4fMain.java",
			    "\n" +
			    "public class T162aat4fMain {\n" +
			    "	final Team162aat4f other = new Team162aat4f();\n" +
			    "	final Team162aat4f wrong = new Team162aat4f();\n" +
			    "	Role<@other>[] role = other.getRoles();\n" +
			    "	public void test () {\n" +
			    "		Role<@wrong> r= role[0];\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team162aat4f.java",
			    "\n" +
			    "public team class Team162aat4f {\n" +
			    "	public class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role[] getRoles() {\n" +
			    "		return new Role[]{new Role()};\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "1.2.2(e)");
    }

    // a type of an array field is anchored to a simple field reference, assigning array element
    // 1.6.2-otjld-anchored-array-type-5
    public void test162_anchoredArrayType5() {
       
       runConformTest(
            new String[] {
		"T162aat5Main.java",
			    "\n" +
			    "public class T162aat5Main {\n" +
			    "	final Team162aat5 other = new Team162aat5();\n" +
			    "	Role<@other>[] role = other.getRoles();\n" +
			    "	public void test () {\n" +
			    "	    Role<@other>[] otherRoles= new Role<@other>[1];\n" +
			    "	    otherRoles[0]= role[0];\n" +
			    "		testOne(otherRoles);\n" +
			    "	}\n" +
			    "	void testOne(Role<@other>[] r) {\n" +
			    "	   System.out.print(r[0].getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new T162aat5Main().test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team162aat5.java",
			    "\n" +
			    "public team class Team162aat5 {\n" +
			    "	public class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role[] getRoles() {\n" +
			    "		return new Role[]{new Role()};\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type of an array field is anchored to a simple field reference, assigning array element, mismatching anchor
    // 1.6.2-otjld-anchored-array-type-5f
    public void test162_anchoredArrayType5f() {
        runNegativeTestMatching(
            new String[] {
		"T162aat5fMain.java",
			    "\n" +
			    "public class T162aat5fMain {\n" +
			    "	final Team162aat5f other = new Team162aat5f();\n" +
			    "	final Team162aat5f wrong = new Team162aat5f();\n" +
			    "	Role<@other>[] role = other.getRoles();\n" +
			    "	public void test () {\n" +
			    "		role[0]= new Role<@wrong>();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team162aat5f.java",
			    "\n" +
			    "public team class Team162aat5f {\n" +
			    "	public class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role[] getRoles() {\n" +
			    "		return new Role[]{new Role()};\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "1.2.2(e)");
    }

    // a role type is anchored to a static variable given by its qualified name
    // 1.6.3-otjld-type-anchored-to-qualified-static-1
    public void test163_typeAnchoredToQualifiedStatic1() {
       
       runConformTest(
            new String[] {
		"T163tatqs1Main.java",
			    "\n" +
			    "public class T163tatqs1Main {\n" +
			    "	Role<@Team163tatqs1.instance> role = Team163tatqs1.instance.getRole();\n" +
			    "	public void test () {\n" +
			    "		System.out.print(role.getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new T163tatqs1Main().test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team163tatqs1.java",
			    "\n" +
			    "public team class Team163tatqs1 {\n" +
			    "	public static final Team163tatqs1 instance = new Team163tatqs1();\n" +
			    "	public class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role getRole() {\n" +
			    "		return new Role();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a role array type is anchored to a static variable given by its qualified name
    // 1.6.3-otjld-type-anchored-to-qualified-static-2
    public void test163_typeAnchoredToQualifiedStatic2() {
       
       runConformTest(
            new String[] {
		"T163tatqs2Main.java",
			    "\n" +
			    "public class T163tatqs2Main {\n" +
			    "	Role<@Team163tatqs2.instance>[] roles = Team163tatqs2.instance.getRoles();\n" +
			    "	public void test () {\n" +
			    "		System.out.print(roles[0].getValue());\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new T163tatqs2Main().test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team163tatqs2.java",
			    "\n" +
			    "public team class Team163tatqs2 {\n" +
			    "	public static final Team163tatqs2 instance = new Team163tatqs2();\n" +
			    "	public class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public Role[] getRoles() {\n" +
			    "		return new Role[]{new Role()};\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type anchor is assigned in its initialization
    // 1.6.4-otjld-type-anchor-initialization-1
    public void test164_typeAnchorInitialization1() {
       
       runConformTest(
            new String[] {
		"T164tai1Main.java",
			    "\n" +
			    "public class T164tai1Main {\n" +
			    "	public static void main(String[] args) {\n" +
			    "		final Team164tai1 t1 = new Team164tai1();\n" +
			    "		final Team164tai1 t2 = t1;\n" +
			    "		R<@t1> r = t1.getR();\n" +
			    "		t2.consumeR(r);\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"Team164tai1.java",
			    "\n" +
			    "public team class Team164tai1 {\n" +
			    "	public class R {\n" +
			    "		public String getValue () { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public R getR() { return new R(); }\n" +
			    "	public void consumeR(R r) { System.out.print(r.getValue()); }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // two different instances are used as anchors
    // 1.6.4-otjld-type-anchor-initialization-2
    public void test164_typeAnchorInitialization2() {
        runNegativeTest(
            new String[] {
		"Team164tai2.java",
			    "\n" +
			    "public team class Team164tai2 {\n" +
			    "	public class R {\n" +
			    "		public String getValue () { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public R getR() { return new R(); }\n" +
			    "	public void consumeR(R r) { System.out.print(r.getValue()); }\n" +
			    "}\n" +
			    "	\n",
		"T164tai2Main.java",
			    "\n" +
			    "public class T164tai2Main {\n" +
			    "	public static void main(String[] args) {\n" +
			    "		final Team164tai2 t1 = new Team164tai2();\n" +
			    "		final Team164tai2 t2 = new Team164tai2();\n" +
			    "		R<@t1> r = t1.getR();\n" +
			    "		t2.consumeR(r);\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            null);
    }

    // a type anchor is assigned
    // 1.6.5-otjld-type-anchor-assignment-1
    public void test165_typeAnchorAssignment1() {
       
       runConformTest(
            new String[] {
		"T165taa1Main.java",
			    "\n" +
			    "public class T165taa1Main {\n" +
			    "	public static void main(String[] args) {\n" +
			    "		final Team165taa1 t1 = new Team165taa1();\n" +
			    "		final Team165taa1 t2 ;\n" +
			    "		R<@t1> r = t1.getR();\n" +
			    "		t2 = t1;\n" +
			    "		t2.consumeR(r);\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"Team165taa1.java",
			    "\n" +
			    "public team class Team165taa1 {\n" +
			    "	public class R {\n" +
			    "		public String getValue () { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public R getR() { return new R(); }\n" +
			    "	public void consumeR(R r) { System.out.print(r.getValue()); }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type anchor is assigned, but final is missing
    // 1.6.5-otjld-type-anchor-assignment-2
    public void test165_typeAnchorAssignment2() {
        runNegativeTest(
            new String[] {
		"Team165taa2.java",
			    "\n" +
			    "public team class Team165taa2 {\n" +
			    "	public class R {\n" +
			    "		public String getValue () { return \"OK\"; }\n" +
			    "	}\n" +
			    "	public R getR() { return new R(); }\n" +
			    "	public void consumeR(R r) { System.out.print(r.getValue()); }\n" +
			    "}\n" +
			    "	\n",
		"T165taa2Main.java",
			    "\n" +
			    "public class T165taa2Main {\n" +
			    "	public static void main(String[] args) {\n" +
			    "		Team165taa2 t1 = new Team165taa2();\n" +
			    "		final Team165taa2 t2 ;\n" +
			    "		t2 = t1;\n" +
			    "		t2.consumeR(t1.getR());\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            null);
    }

    // a type anchor is a 3 element path
    // 1.6.6-otjld-type-anchor-is-path-1
    public void test166_typeAnchorIsPath1() {
       
       runConformTest(
            new String[] {
		"T166taia1Main.java",
			    "\n" +
			    "public class T166taia1Main {\n" +
			    "	public static void main (String[] args) {\n" +
			    "		final T166taia1_1 t = new T166taia1_1();\n" +
			    "		R<@t.a1.a2> r = t.a1.a2.getR();\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team166taia1.java",
			    "\n" +
			    "public team class Team166taia1 {\n" +
			    "	public class R { \n" +
			    "		public void test() {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R getR() { \n" +
			    "		return new R();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T166taia1_2.java",
			    "\n" +
			    "public class T166taia1_2 {\n" +
			    "	public final Team166taia1 a2 = new Team166taia1();\n" +
			    "}\n" +
			    "	\n",
		"T166taia1_1.java",
			    "\n" +
			    "public class T166taia1_1 {\n" +
			    "	public final T166taia1_2 a1 = new T166taia1_2();\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type anchor is a 3 element path - non final element in path
    // 1.6.6-otjld-type-anchor-is-path-1f
    public void test166_typeAnchorIsPath1f() {
        runNegativeTestMatching(
            new String[] {
		"T166taia1fMain.java",
			    "\n" +
			    "public class T166taia1fMain {\n" +
			    "	public static void main (String[] args) {\n" +
			    "		final T166taia1f_1 t = new T166taia1f_1();\n" +
			    "		R<@t.a1.a2> r = t.a1.a2.getR();\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team166taia1f.java",
			    "\n" +
			    "public team class Team166taia1f {\n" +
			    "	public class R { \n" +
			    "		public void test() {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R getR() { \n" +
			    "		return new R();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"T166taia1f_2.java",
			    "\n" +
			    "public class T166taia1f_2 {\n" +
			    "	public final Team166taia1f a2 = new Team166taia1f();\n" +
			    "}\n" +
			    "	\n",
		"T166taia1f_1.java",
			    "\n" +
			    "public class T166taia1f_1 {\n" +
			    "	public T166taia1f_2 a1 = new T166taia1f_2();\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in T166taia1fMain.java (at line 5)\n" + 
    		"	R<@t.a1.a2> r = t.a1.a2.getR();\n" + 
    		"	  ^^^^^^^^\n" + 
    		"Anchor T166taia1f_1.a1 for the role type R is not final (OTJLD 1.2.2(c)).\n" + 
    		"----------\n" + 
    		"2. ERROR in T166taia1fMain.java (at line 5)\n" + 
    		"	R<@t.a1.a2> r = t.a1.a2.getR();\n" + 
    		"	                ^^^^\n" + 
    		"Anchor t.a1 for the role type R<@tthis[Team166taia1f]> is not final (OTJLD 1.2.2(c)).\n" + 
    		"----------\n" + 
    		"3. ERROR in T166taia1fMain.java (at line 5)\n" + 
    		"	R<@t.a1.a2> r = t.a1.a2.getR();\n" + 
    		"	                ^^^^^^^^^^^^^^\n" + 
    		"Type mismatch: cannot convert from R<@tthis[Team166taia1f]> to R<@t.a1.a2> because type anchors could not be proven to be the same instance (OTJLD 1.2.2(e)).\n" + 
    		"----------\n");
    }

    // a type anchor is a 3 element path - trying to implicitly use enclosing instance
    // 1.6.6-otjld-type-anchor-is-path-2
    public void test166_typeAnchorIsPath2() {
        runNegativeTestMatching(
            new String[] {
		"T166taia2Main.java",
			    "\n" +
			    "public class T166taia2Main {\n" +
			    "	public static void main (String[] args) {\n" +
			    "		final T166taia2_1 t = new T166taia2_1();\n" +
			    "		R<@t.a1.a2.theR> r = t.a1.a2.getR();\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team166taia2.java",
			    "\n" +
			    "public team class Team166taia2 {\n" +
			    "	public class R { \n" +
			    "		public void test() {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R getR() { \n" +
			    "		return new R();\n" +
			    "	}\n" +
			    "	final R theR = new R();\n" +
			    "}\n" +
			    "	\n",
		"T166taia2_2.java",
			    "\n" +
			    "public class T166taia2_2 {\n" +
			    "	public final Team166taia2 a2 = new Team166taia2();\n" +
			    "}\n" +
			    "	\n",
		"T166taia2_1.java",
			    "\n" +
			    "public class T166taia2_1 {\n" +
			    "	public final T166taia2_2 a1 = new T166taia2_2();\n" +
			    "}\n" +
			    "	\n"
            },
            "resolved to a type");
    }

    // a type anchor is a 3 element path - trying to implicitly use enclosing instance
    // 1.6.6-otjld-type-anchor-is-path-3
    public void test166_typeAnchorIsPath3() {
       
       runConformTest(
            new String[] {
		"T166taia3Main.java",
			    "\n" +
			    "public class T166taia3Main {\n" +
			    "	public static void main (String[] args) {\n" +
			    "		final T166taia3_1 t = new T166taia3_1();\n" +
			    "		R<@t.a1.a2> r = t.a1.a2.theR.getR();\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team166taia3.java",
			    "\n" +
			    "public team class Team166taia3 {\n" +
			    "	public class R { \n" +
			    "		public void test() {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "		public R getR() { \n" +
			    "			return new R();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	final R theR = new R();\n" +
			    "}\n" +
			    "	\n",
		"T166taia3_2.java",
			    "\n" +
			    "public class T166taia3_2 {\n" +
			    "	public final Team166taia3 a2 = new Team166taia3();\n" +
			    "}\n" +
			    "	\n",
		"T166taia3_1.java",
			    "\n" +
			    "public class T166taia3_1 {\n" +
			    "	public final T166taia3_2 a1 = new T166taia3_2();\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type anchor is a 3 element path - role is accessed by a field
    // 1.6.6-otjld-type-anchor-is-path-4
    public void test166_typeAnchorIsPath4() {
       
       runConformTest(
            new String[] {
		"T166taia4Main.java",
			    "\n" +
			    "public class T166taia4Main {\n" +
			    "	public static void main (String[] args) {\n" +
			    "		final T166taia4_1 t = new T166taia4_1();\n" +
			    "		R<@t.a1.a2> r = t.a1.a2.myR;\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team166taia4.java",
			    "\n" +
			    "public team class Team166taia4 {\n" +
			    "	public class R { \n" +
			    "		public void test() {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R myR = new R();\n" +
			    "}\n" +
			    "	\n",
		"T166taia4_2.java",
			    "\n" +
			    "public class T166taia4_2 {\n" +
			    "	public final Team166taia4 a2 = new Team166taia4();\n" +
			    "}\n" +
			    "	\n",
		"T166taia4_1.java",
			    "\n" +
			    "public class T166taia4_1 {\n" +
			    "	public final T166taia4_2 a1 = new T166taia4_2();\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a type anchor is a 3 element path - role is accessed by a field - non final element in path
    // 1.6.6-otjld-type-anchor-is-path-4f
    public void test166_typeAnchorIsPath4f() {
        runNegativeTestMatching(
            new String[] {
		"T166taia4fMain.java",
			    "\n" +
			    "public class T166taia4fMain {\n" +
			    "	public static void main (String[] args) {\n" +
			    "		final T166taia4f_1 t = new T166taia4f_1();\n" +
			    "		R<@t.a1.a2> r = t.a1.a2.myR;\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team166taia4f.java",
			    "\n" +
			    "public team class Team166taia4f {\n" +
			    "	public class R { \n" +
			    "		public void test() {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R myR = new R();\n" +
			    "}\n" +
			    "	\n",
		"T166taia4f_2.java",
			    "\n" +
			    "public class T166taia4f_2 {\n" +
			    "	public final Team166taia4f a2 = new Team166taia4f();\n" +
			    "}\n" +
			    "	\n",
		"T166taia4f_1.java",
			    "\n" +
			    "public class T166taia4f_1 {\n" +
			    "	public T166taia4f_2 a1 = new T166taia4f_2();\n" +
			    "}\n" +
			    "	\n"
            },
            "----------\n" + 
    		"1. ERROR in T166taia4fMain.java (at line 5)\n" + 
    		"	R<@t.a1.a2> r = t.a1.a2.myR;\n" + 
    		"	  ^^^^^^^^\n" + 
    		"Anchor T166taia4f_1.a1 for the role type R is not final (OTJLD 1.2.2(c)).\n" + 
    		"----------\n" + 
    		"2. ERROR in T166taia4fMain.java (at line 5)\n" + 
    		"	R<@t.a1.a2> r = t.a1.a2.myR;\n" + 
    		"	                ^^^^\n" + 
    		"Anchor t.a1 for the role type R<@tthis[Team166taia4f]> is not final (OTJLD 1.2.2(c)).\n" + 
    		"----------\n");
    }

    // mixed test - positive part
    // 1.6.6-otjld-type-anchor-is-path-5
    public void test166_typeAnchorIsPath5() {
        runConformTest(
            new String[] {
		"Team166taip5.java",
			    "\n" +
			    "public team class Team166taip5 {\n" +
			    "    public final Team166taip5 x = null;\n" +
			    "    public final Y<@x> yx1 = x.new Y();\n" +
			    "    public Y<@x> yx2;\n" +
			    "    public final Z<@yx1> zx1 = null;\n" +
			    "    public Z<@yx1> zx2;\n" +
			    "\n" +
			    "    public team class Y {\n" +
			    "        Y yy;\n" +
			    "        public Y getYY() { return yy; }\n" +
			    "        public team class Z {\n" +
			    "            Y<@x> yz1;\n" +
			    "            public void setYz1(Y<@x> arg) {\n" +
			    "                yz1 = arg; \n" +
			    "            }\n" +
			    "            Y yz2;\n" +
			    "        }\n" +
			    "   }   \n" +
			    "}        \n" +
			    "    \n",
		"T166taip5.java",
			    "\n" +
			    "public class T166taip5 {\n" +
			    "   final Team166taip5 xt =null;\n" +
			    "   final Y<@xt.x> myY = null;\n" +
			    "\n" +
			    "   final Y<@xt> y1 = null;\n" +
			    "   final Z<@y1> z  = null;\n" +
			    "   Y<@xt> y2;\n" +
			    "   \n" +
			    "   void foo() {\n" +
			    "        xt.yx2 = myY;  // this should be ok\n" +
			    "        xt.zx2 = null; // this should be ok (I think)\n" +
			    "        xt.zx2 = xt.zx1; // this should be ok as well\n" +
			    "        z.setYz1(xt.yx1);  // this should be ok and is ok\n" +
			    "  \n" +
			    "        Y<@xt> yf = xt.new Y(); // this works\n" +
			    "        \n" +
			    "        Z<@y1> zf = y1.new Z(); // this should probably work (?)\n" +
			    "        \n" +
			    "        yf = xt.new Y().getYY(); \n" +
			    "   }    \n" +
			    "}    \n" +
			    "    \n"
            });
    }

    // mixed test - negative part 1
    // 1.6.6-otjld-type-anchor-is-path-6
    public void test166_typeAnchorIsPath6() {
        runNegativeTestMatching(
            new String[] {
		"T166taip6.java",
			    "\n" +
			    "public class T166taip6 {\n" +
			    "   final Team166taip6 xt =null;\n" +
			    "   final Y<@xt.x> myY = null;\n" +
			    "\n" +
			    "   final Y<@xt> y1 = null;\n" +
			    "   final Z<@y1> z  = null;\n" +
			    "   Y<@xt> y2;\n" +
			    "   void foo() {\n" +
			    "        y2 = z.yz1;     // this should not be ok\n" +
			    "   }    \n" +
			    "}    \n" +
			    "    \n",
		"Team166taip6.java",
			    "\n" +
			    "public team class Team166taip6 {\n" +
			    "    public final Team166taip6 x = null;\n" +
			    "    public final Y<@x> yx1 = x.new Y();\n" +
			    "    public Y<@x> yx2;\n" +
			    "    public final Z<@yx1> zx1 = null;\n" +
			    "    public Z<@yx1> zx2;\n" +
			    "\n" +
			    "    public team class Y {\n" +
			    "        Y yy;\n" +
			    "        public team class Z {\n" +
			    "            public Y<@x> yz1;\n" +
			    "            Y yz2;\n" +
			    "        }\n" +
			    "   }   \n" +
			    "}        \n" +
			    "    \n"
            },
            "Type mismatch");
    }

    // mixed test - negative part 2
    // 1.6.6-otjld-type-anchor-is-path-7
    public void test166_typeAnchorIsPath7() {
        runNegativeTestMatching(
            new String[] {
		"T166taip7.java",
			    "\n" +
			    "public class T166taip7 {\n" +
			    "   final Team166taip7 xt =null;\n" +
			    "   final Y<@xt.x> myY = null;\n" +
			    "\n" +
			    "   final Y<@xt> y1 = null;\n" +
			    "   final Z<@y1> z  = null;\n" +
			    "   Y<@xt> y2;\n" +
			    "   void foo() {\n" +
			    "        xt.yx1 = z.yz1; // this should not be ok b/c x.y is final\n" +
			    "   }    \n" +
			    "}    \n" +
			    "    \n",
		"Team166taip7.java",
			    "\n" +
			    "public team class Team166taip7 {\n" +
			    "    public final Team166taip7 x = null;\n" +
			    "    public final Y<@x> yx1 = x.new Y();\n" +
			    "    public Y<@x> yx2;\n" +
			    "    public final Z<@yx1> zx1 = null;\n" +
			    "    public Z<@yx1> zx2;\n" +
			    "\n" +
			    "    public team class Y {\n" +
			    "        Y yy;\n" +
			    "        public team class Z {\n" +
			    "            public Y<@x> yz1;\n" +
			    "            Y yz2;\n" +
			    "        }\n" +
			    "   }   \n" +
			    "}        \n" +
			    "    \n"
            },
            "final");
    }

    // mixed test - negative part 3
    // 1.6.6-otjld-type-anchor-is-path-8
    public void test166_typeAnchorIsPath8() {
        runNegativeTestMatching(
            new String[] {
		"T166taip8.java",
			    "\n" +
			    "public class T166taip8 {\n" +
			    "   final Team166taip8 xt =null;\n" +
			    "   final Y<@xt.x> myY = null;\n" +
			    "\n" +
			    "   final Y<@xt> y1 = null;\n" +
			    "   final Z<@y1> z  = null;\n" +
			    "   Y<@xt> y2;\n" +
			    "   void foo() {\n" +
			    "        y1.yy = z.yz1;  // this should not be ok\n" +
			    "   }    \n" +
			    "}    \n" +
			    "    \n",
		"Team166taip8.java",
			    "\n" +
			    "public team class Team166taip8 {\n" +
			    "    public final Team166taip8 x = null;\n" +
			    "    public final Y<@x> yx1 = x.new Y();\n" +
			    "    public Y<@x> yx2;\n" +
			    "    public final Z<@yx1> zx1 = null;\n" +
			    "    public Z<@yx1> zx2;\n" +
			    "\n" +
			    "    public team class Y {\n" +
			    "        public Y yy;\n" +
			    "        public team class Z {\n" +
			    "            public Y<@x> yz1;\n" +
			    "            Y yz2;\n" +
			    "        }\n" +
			    "   }   \n" +
			    "}        \n" +
			    "    \n"
            },
            "Type mismatch");
    }

    // See https://bugs.eclipse.org/397897 [compiler] In three layers of teams anchor equivalence is not recognized
    public void test166_typeAnchorIsPath9() {
    	runConformTest(
    		new String[] {
    	"p166taip9/Main.java",
    			"package p166taip9;\n" +
    			"public team class Main {\n" + 
    			"	private final Display display;\n" + 
    			"\n" + 
    			"	public Main(Display display) {\n" + 
    			"		this.display = display;\n" + 
    			"	}\n" + 
    			"	protected class C playedBy Connect<@display> {\n" + 
    			"		final Shapes sw = display.a;\n" + 
    			"		protected void test() {\n" + 
    			"			sw.new Connection(getNode().getShape());\n" + 
    			"		}\n" + 
    			"		Node<@display> getNode() -> Node<@display> getNode();\n" + 
    			"	}\n" +
    			"   void test2(Connect<@display> as C c) {\n" +
    			"       c.test();\n" +
    			"   }\n" +
    			"   void test() {\n" +
    			"		test2(display.new Connect());\n" +
    			"	}\n" +
    			"   public static void main(String[] args) {\n" +
    			"		final Display d = new Display();\n" +
    			"       new Main(d).test();\n" +
    			"   }\n" + 
    			"}",
    	"p166taip9/Shapes.java",
    			"package p166taip9;\n" +
    			"public team class Shapes {\n" + 
    			"	public class Shape {\n" +
    			"		protected void print() {\n" +
    			"			System.out.print(\"OK\");\n" +
    			"		}\n" +
    			"	}\n" + 
    			"	public class Connection {\n" + 
    			"		public Connection(Shape s) {" +
    			"			s.print();\n" +
    			"		}\n" + 
    			"	}\n" + 
    			"}\n",
    	"p166taip9/Display.java",
    			"package p166taip9;\n" +
    			"public team class Display {\n" + 
    			"	public final Shapes a = new Shapes();\n" + 
    			"	public class Connect {\n" + 
    			"		public Node getNode() {\n" + 
    			"			return new Node();\n" + 
    			"		}\n" + 
    			"	}\n" + 
    			"	public class Node {\n" + 
    			"		public Shape<@a> getShape() {\n" + 
    			"			return a.new Shape();\n" + 
    			"		}\n" + 
    			"	}\n" + 
    			"}\n"
    		},
    		"OK");
    }

    // a role is externalized relative to the base reference
    // 1.6.7-otjld-role-anchored-to-base-1
    public void test167_roleAnchoredToBase1() {
       
       runConformTest(
            new String[] {
		"Team167ratb1_2.java",
			    "\n" +
			    "public team class Team167ratb1_2 {\n" +
			    "	public class R2 playedBy Team167ratb1_1 { \n" +
			    "		protected R2() {\n" +
			    "			base();\n" +
			    "		}\n" +
			    "		protected void test() {\n" +
			    "			R<@base> r = getR();\n" +
			    "			r.test();\n" +
			    "		}\n" +
			    "		abstract R<@base> getR();\n" +
			    "		getR -> getR;\n" +
			    "	}\n" +
			    "	public Team167ratb1_2 () {\n" +
			    "		R2 r = new R2();\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team167ratb1_2();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"Team167ratb1_1.java",
			    "\n" +
			    "public team class Team167ratb1_1 {\n" +
			    "	public class R {\n" +
			    "		public void test () {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R getR() {\n" +
			    "		return new R();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "OK");
    }

    // a role is externalized relative to the base reference - passed as parameter
    // 1.6.7-otjld-role-anchored-to-base-2
    public void test167_roleAnchoredToBase2() {
       
       runConformTest(
            new String[] {
		"Team167ratb2_2.java",
			    "\n" +
			    "public team class Team167ratb2_2 {\n" +
			    "	public class R2 playedBy Team167ratb2_1 { \n" +
			    "		protected R2() {\n" +
			    "			base();\n" +
			    "		}\n" +
			    "		protected void test() {\n" +
			    "			R<@base> r = getR();\n" +
			    "			testR(r);\n" +
			    "		}\n" +
			    "		void testR(R<@base> r) {\n" +
			    "			r.test();\n" +
			    "		}\n" +
			    "		abstract R<@base> getR();\n" +
			    "		R<@base> getR() -> R<@base> getR();\n" +
			    "	}\n" +
			    "	public Team167ratb2_2 () {\n" +
			    "		R2 r = new R2();\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team167ratb2_2();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"Team167ratb2_1.java",
			    "\n" +
			    "public team class Team167ratb2_1 {\n" +
			    "	public class R {\n" +
			    "		public void test () {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R getR() {\n" +
			    "		return new R();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "OK");
    }

    // a role is externalized relative to the base reference and used in the playedBy clause of a nested role!
    // 1.6.7-otjld-role-anchored-to-base-3
    public void test167_roleAnchoredToBase3() {
       
       runConformTest(
            new String[] {
		"Team167ratb3_2.java",
			    "\n" +
			    "public team class Team167ratb3_2 {\n" +
			    "	public team class R2 playedBy Team167ratb3_1 { \n" +
			    "		protected R2() {\n" +
			    "			base();\n" +
			    "		}\n" +
			    "		protected void test() {\n" +
			    "			R3 r = getR();\n" +
			    "			r.test();\n" +
			    "		}\n" +
			    "		abstract R3 getR();\n" +
			    "		getR -> getR;\n" +
			    "		protected class R3 playedBy R<@base> {\n" +
			    "			protected void test() -> void test();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public Team167ratb3_2 () {\n" +
			    "		R2 r = new R2();\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team167ratb3_2();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"Team167ratb3_1.java",
			    "\n" +
			    "public team class Team167ratb3_1 {\n" +
			    "	public class R {\n" +
			    "		public void test () {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R getR() {\n" +
			    "		return new R();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "OK");
    }

    // a role array is externalized relative to the base reference
    // 1.6.7-otjld-role-anchored-to-base-4
    public void test167_roleAnchoredToBase4() {
       
       runConformTest(
            new String[] {
		"Team167ratb4_2.java",
			    "\n" +
			    "public team class Team167ratb4_2 {\n" +
			    "	public class R2 playedBy Team167ratb4_1 { \n" +
			    "		protected R2() {\n" +
			    "			base();\n" +
			    "		}\n" +
			    "		protected void test() {\n" +
			    "			R<@base>[] rs = getRs();\n" +
			    "			for (int i=0; i<rs.length; i++) \n" +
			    "			   rs[i].test();\n" +
			    "		}\n" +
			    "		abstract R<@base>[] getRs();\n" +
			    "		getRs -> getRs;\n" +
			    "	}\n" +
			    "	public Team167ratb4_2 () {\n" +
			    "		R2 r = new R2();\n" +
			    "		r.test();\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team167ratb4_2();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"Team167ratb4_1.java",
			    "\n" +
			    "public team class Team167ratb4_1 {\n" +
			    "	public class R {\n" +
			    "		public void test () {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R[] getRs() {\n" +
			    "		return new R[]{new R(), new R()};\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "OKOK");
    }

    // a role is externalized relative to the base reference, used in a callout signature
    // 1.6.7-otjld-role-anchored-to-base-5
    public void test167_roleAnchoredToBase5() {
       
       runConformTest(
            new String[] {
		"Team167ratb5_2.java",
			    "\n" +
			    "public team class Team167ratb5_2 {\n" +
			    "        public class R playedBy Team167ratb5_1 {\n" +
			    "                protected R() {\n" +
			    "                        base();\n" +
			    "                }\n" +
			    "                protected void test() {\n" +
			    "                        R2<@base> r = getR2(getR1());\n" +
			    "                        r.test();\n" +
			    "                }\n" +
			    "                abstract R1<@base> getR1();\n" +
			    "                R1<@base> getR1() -> R1<@base> getR1();\n" +
			    "                abstract R2<@base> getR2(R1<@base> in);\n" +
			    "                R2<@base> getR2(R1<@base> in) -> R2<@base> getR2(R1<@base> in);\n" +
			    "        }\n" +
			    "        public Team167ratb5_2 () {\n" +
			    "                R r = new R();\n" +
			    "                r.test();\n" +
			    "        }\n" +
			    "        public static void main(String[] args) {\n" +
			    "                new Team167ratb5_2();\n" +
			    "        }\n" +
			    "}\n" +
			    "        \n",
		"Team167ratb5_1.java",
			    "\n" +
			    "public team class Team167ratb5_1 {\n" +
			    "        public class R1 {\n" +
			    "                public R2 getR2() {\n" +
			    "                      return new R2();\n" +
			    "                }\n" +
			    "        }\n" +
			    "        public class R2 {\n" +
			    "                public void test () {\n" +
			    "                        System.out.print(\"OK\");\n" +
			    "                }\n" +
			    "        }\n" +
			    "        public R1 getR1() { return new R1(); }\n" +
			    "        public R2 getR2(R1 in) {\n" +
			    "                return in.getR2();\n" +
			    "        }\n" +
			    "}\n" +
			    "        \n"
            },
            "OK");
    }

    // a syntax error in previous test triggered an NPE
    // 1.6.7-otjld-role-anchored-to-base-5f
    public void test167_roleAnchoredToBase5f() {
        runNegativeTestMatching(
            new String[] {
		"Team167ratb5f_2.java",
			    "\n" +
			    "public team class Team167ratb5f_2 {\n" +
			    "        public class R playedBy Team167ratb5f_1 {\n" +
			    "                protected R() {\n" +
			    "                        base();\n" +
			    "                }\n" +
			    "                protected void test() {\n" +
			    "                        R<@base> r = getR2(getR1());\n" +
			    "                        r.test();\n" +
			    "                }\n" +
			    "                abstract R<@base> getR1();\n" +
			    "                R<@base> getR1() -> R<@base> getR1();\n" +
			    "                abstract R<@base> getR2(R<@base> in);\n" +
			    "                R<@base> getR2(R<@base> in) -> R<@base> getR2(R<@base>1 in); // here\n" +
			    "        }\n" +
			    "}\n" +
			    "        \n",
		"Team167ratb5f_1.java",
			    "\n" +
			    "public team class Team167ratb5f_1 {\n" +
			    "        public class R1 {\n" +
			    "                public R2 getR2() {\n" +
			    "                      return new R2();\n" +
			    "                }\n" +
			    "        }\n" +
			    "        public class R2 {\n" +
			    "                public void test () {\n" +
			    "                        System.out.print(\"OK\");\n" +
			    "                }\n" +
			    "        }\n" +
			    "        public R1 getR1() { return new R1(); }\n" +
			    "        public R2 getR2(R1 in) {\n" +
			    "                return in.getR2();\n" +
			    "        }\n" +
			    "}\n" +
			    "        \n"
            },
    		"----------\n" +
			"1. ERROR in Team167ratb5f_2.java (at line 3)\n" +
			"	public class R playedBy Team167ratb5f_1 {\n" +
			"	             ^\n" +
			"The type R must be an abstract class to define abstract methods\n" +
			"----------\n" +
			"2. ERROR in Team167ratb5f_2.java (at line 8)\n" +
			"	R<@base> r = getR2(getR1());\n" +
			"	^\n" +
			"Illegal use of value parameter: type Team167ratb5f_2.R does not declare a value parameter at position 1 (OTJLD 9.2.1).\n" +
			"----------\n" +
			"3. ERROR in Team167ratb5f_2.java (at line 8)\n" +
			"	R<@base> r = getR2(getR1());\n" +
			"	             ^^^^^\n" +
			"The method getR2(R<@tthis[Team167ratb5f_2]>) in the type Team167ratb5f_2.R is not applicable for the arguments (R)\n" +
			"----------\n" +
			"4. ERROR in Team167ratb5f_2.java (at line 9)\n" +
			"	r.test();\n" +
			"	^\n" +
			"The type R is not visible\n" +
			"----------\n" +
			"5. ERROR in Team167ratb5f_2.java (at line 11)\n" +
			"	abstract R<@base> getR1();\n" +
			"	         ^\n" +
			"Illegal use of value parameter: type Team167ratb5f_2.R does not declare a value parameter at position 1 (OTJLD 9.2.1).\n" +
			"----------\n" +
			"6. ERROR in Team167ratb5f_2.java (at line 11)\n" +
			"	abstract R<@base> getR1();\n" +
			"	         ^^^^^^^^\n" +
			"The return type is incompatible with Team167ratb5f_2.R.getR1()\n" +
			"----------\n" +
			"7. ERROR in Team167ratb5f_2.java (at line 12)\n" +
			"	R<@base> getR1() -> R<@base> getR1();\n" +
			"	^\n" +
			"Illegal use of value parameter: type Team167ratb5f_2.R does not declare a value parameter at position 1 (OTJLD 9.2.1).\n" +
			"----------\n" +
			"8. ERROR in Team167ratb5f_2.java (at line 12)\n" +
			"	R<@base> getR1() -> R<@base> getR1();\n" +
			"	^\n" +
			"Method designator binds to a method returning R whereas return type R is specified (OTJLD 3.1(c)).\n" +
			"----------\n" +
			"9. ERROR in Team167ratb5f_2.java (at line 12)\n" +
			"	R<@base> getR1() -> R<@base> getR1();\n" +
			"	                    ^\n" +
			"Illegal use of value parameter: type Team167ratb5f_2.R does not declare a value parameter at position 1 (OTJLD 9.2.1).\n" +
			"----------\n" +
			"10. ERROR in Team167ratb5f_2.java (at line 12)\n" +
			"	R<@base> getR1() -> R<@base> getR1();\n" +
			"	                    ^\n" +
			"Method designator binds to a method returning R1<@tthis[Team167ratb5f_1]> whereas return type R is specified (OTJLD 3.1(c)).\n" +
			"----------\n" +
			"11. ERROR in Team167ratb5f_2.java (at line 13)\n" +
			"	abstract R<@base> getR2(R<@base> in);\n" +
			"	         ^\n" +
			"Illegal use of value parameter: type Team167ratb5f_2.R does not declare a value parameter at position 1 (OTJLD 9.2.1).\n" +
			"----------\n" +
			"12. ERROR in Team167ratb5f_2.java (at line 13)\n" +
			"	abstract R<@base> getR2(R<@base> in);\n" +
			"	                  ^^^^^^^^^^^^^^^^^^\n" +
			"The abstract method getR2 in type R can only be defined by an abstract class\n" +
			"----------\n" +
			"13. ERROR in Team167ratb5f_2.java (at line 13)\n" +
			"	abstract R<@base> getR2(R<@base> in);\n" +
			"	                        ^\n" +
			"Illegal use of value parameter: type Team167ratb5f_2.R does not declare a value parameter at position 1 (OTJLD 9.2.1).\n" +
			"----------\n" +
			"14. ERROR in Team167ratb5f_2.java (at line 14)\n" +
			"	R<@base> getR2(R<@base> in) -> R<@base> getR2(R<@base>1 in); // here\n" +
			"	^\n" +
			"Illegal use of value parameter: type Team167ratb5f_2.R does not declare a value parameter at position 1 (OTJLD 9.2.1).\n" +
			"----------\n" +
			"15. ERROR in Team167ratb5f_2.java (at line 14)\n" +
			"	R<@base> getR2(R<@base> in) -> R<@base> getR2(R<@base>1 in); // here\n" +
			"	               ^\n" +
			"Illegal use of value parameter: type Team167ratb5f_2.R does not declare a value parameter at position 1 (OTJLD 9.2.1).\n" +
			"----------\n" +
			"16. ERROR in Team167ratb5f_2.java (at line 14)\n" +
			"	R<@base> getR2(R<@base> in) -> R<@base> getR2(R<@base>1 in); // here\n" +
			"	                               ^\n" +
			"Illegal use of value parameter: type Team167ratb5f_2.R does not declare a value parameter at position 1 (OTJLD 9.2.1).\n" +
			"----------\n" +
			"17. ERROR in Team167ratb5f_2.java (at line 14)\n" +
			"	R<@base> getR2(R<@base> in) -> R<@base> getR2(R<@base>1 in); // here\n" +
			"	                               ^^^^^^^^^^^^^^^\n" +
			"No method getR2() found in type Team167ratb5f_1 to resolve method designator (OTJLD 3.1(c)).\n" +
			"----------\n" +
			"18. ERROR in Team167ratb5f_2.java (at line 14)\n" +
			"	R<@base> getR2(R<@base> in) -> R<@base> getR2(R<@base>1 in); // here\n" +
			"	                                                      ^\n" +
			"Syntax error on token \"1\", delete this token\n" +
			"----------\n");
    }

    // a role is externalized relative to the base reference - used in callin signature
    // 1.6.7-otjld-role-anchored-to-base-6
    public void test167_roleAnchoredToBase6() {
       
       runConformTest(
            new String[] {
		"Team167ratb6_2.java",
			    "\n" +
			    "public team class Team167ratb6_2 {\n" +
			    "	public class R2 playedBy Team167ratb6_1 { \n" +
			    "		callin R<@base> printR() {\n" +
			    "			R<@base> r = base.printR();\n" +
			    "			r.test();\n" +
			    "			return r;\n" +
			    "		}\n" +
			    "		printR <- replace getR;\n" +
			    "	}\n" +
			    "	public Team167ratb6_2 () {\n" +
			    "		activate();\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		new Team167ratb6_2();\n" +
			    "		final Team167ratb6_1 t = new Team167ratb6_1();\n" +
			    "		t.getR();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"Team167ratb6_1.java",
			    "\n" +
			    "public team class Team167ratb6_1 {\n" +
			    "	public class R {\n" +
			    "		public void test () {\n" +
			    "			System.out.print(\"OK\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R getR() {\n" +
			    "		return new R();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "OK");
    }

    // base is qualified with a non-role type
    // 1.6.7-otjld-role-anchored-to-base-7
    public void test167_roleAnchoredToBase7() {
        runNegativeTestMatching(
            new String[] {
		"Team167ratb7.java",
			    "\n" +
			    "public team class Team167ratb7 {\n" +
			    "    public class R { }\n" +
			    "    R<@Team167ratb7.base> role;\n" +
			    "}\n" +
			    "    \n"
            },
            "2.6(a)");
    }

    // an anchored type is read from a class file , navigate along MessageSend-receiver
    // 1.6.8-otjld-anchored-type-from-classfile-1
    public void test168_anchoredTypeFromClassfile1() {
       
       runConformTest(
            new String[] {
		"T168atfc1_2.java",
			    "\n" +
			    "public class T168atfc1_2 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final T168atfc1_1 other = new T168atfc1_1();\n" +
			    "        R<@other.t> r = other.getR();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team168atfc1.java",
			    "\n" +
			    "public team class Team168atfc1 {\n" +
			    "    public class R {\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public R getR() { return new R(); }\n" +
			    "}\n" +
			    "    \n",
		"T168atfc1_1.java",
			    "\n" +
			    "public class T168atfc1_1 {\n" +
			    "    public final Team168atfc1 t = new Team168atfc1();\n" +
			    "    public R<@t> getR() { return t.getR(); }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // an anchored type is read from a class file , navigate along outer of MessageSend-receiver
    // 1.6.8-otjld-anchored-type-from-classfile-2
    public void test168_anchoredTypeFromClassfile2() {
       
       runConformTest(
            new String[] {
		"T168atfc2_2.java",
			    "\n" +
			    "public class T168atfc2_2 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final T168atfc2_1 other = new T168atfc2_1();\n" +
			    "        R<@other.t> r = other.inner.getR();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team168atfc2.java",
			    "\n" +
			    "public team class Team168atfc2 {\n" +
			    "    public class R {\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public R getR() { return new R(); }\n" +
			    "}\n" +
			    "    \n",
		"T168atfc2_1.java",
			    "\n" +
			    "public class T168atfc2_1 {\n" +
			    "    public final Team168atfc2 t = new Team168atfc2();\n" +
			    "    public class Inner {\n" +
			    "        public R<@t> getR() { return t.getR(); }\n" +
			    "    }\n" +
			    "    public final Inner inner = new Inner();\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // an anchored type is read from a class file , navigate along field
    // 1.6.8-otjld-anchored-type-from-classfile-3
    public void test168_anchoredTypeFromClassfile3() {
       
       runConformTest(
            new String[] {
		"T168atfc3_2.java",
			    "\n" +
			    "public class T168atfc3_2 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final T168atfc3_1 other = new T168atfc3_1();\n" +
			    "        R<@other.t> r = other.r;\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team168atfc3.java",
			    "\n" +
			    "public team class Team168atfc3 {\n" +
			    "    public class R {\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public R getR () { return new R(); };\n" +
			    "}\n" +
			    "    \n",
		"T168atfc3_1.java",
			    "\n" +
			    "public class T168atfc3_1 {\n" +
			    "    public final Team168atfc3 t = new Team168atfc3();\n" +
			    "    public R<@t> r = t.getR();\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // an anchored type is read from a class file, navigate along field - compile two files in one go
    // 1.6.8-otjld-anchored-type-from-classfile-3a
    public void test168_anchoredTypeFromClassfile3a() {
       
       runConformTest(
            new String[] {
		"T168atfc3a_2.java",
			    "\n" +
			    "public class T168atfc3a_2 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final T168atfc3a_1 other = new T168atfc3a_1();\n" +
			    "        R<@other.t> r = other.r;\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team168atfc3a.java",
			    "\n" +
			    "public team class Team168atfc3a {\n" +
			    "    public class R {\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public R getR () { return new R(); };\n" +
			    "}\n" +
			    "    \n",
		"T168atfc3a_1.java",
			    "\n" +
			    "public class T168atfc3a_1 {\n" +
			    "    public final Team168atfc3a t = new Team168atfc3a();\n" +
			    "    public R<@t> r = t.getR();\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a callout binding has an arg that is anchored to another argument
    // 1.6.9-otjld-callout-with-anchored-type-1
    public void test169_calloutWithAnchoredType1() {
       
       runConformTest(
            new String[] {
		"Team169cwat1.java",
			    "\n" +
			    "public team class Team169cwat1 {\n" +
			    "    public class R playedBy T169cwat1 {\n" +
			    "        protected void test1(final Team169cwat1 tr, R<@tr> r) -> void test1(final Team169cwat1 tb, R<@tb> r);\n" +
			    "        \n" +
			    "        public abstract void test2();\n" +
			    "        test2 -> test2;\n" +
			    "    }\n" +
			    "    public Team169cwat1() {\n" +
			    "        R r1 = new R(new T169cwat1(\"NOTOK\"));\n" +
			    "        R r2 = new R(new T169cwat1(\"OK\"));\n" +
			    "        r1.test1(this, r2);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team169cwat1();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T169cwat1.java",
			    "\n" +
			    "public team class T169cwat1 {\n" +
			    "    private String val;\n" +
			    "    public T169cwat1(String v) {\n" +
			    "        val = v;\n" +
			    "    }\n" +
			    "    public void test2() {\n" +
			    "        System.out.print(val);\n" +
			    "    }\n" +
			    "    public void test1(final Team169cwat1 t, R<@t> r) {\n" +
			    "        r.test2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a callout binding has an arg that is anchored to another argument
    // 1.6.9-otjld-callout-with-anchored-type-2
    public void test169_calloutWithAnchoredType2() {
       
       runConformTest(
            new String[] {
		"Team169cwat2.java",
			    "\n" +
			    "public team class Team169cwat2 {\n" +
			    "    public class R playedBy T169cwat2 {\n" +
			    "        protected void test1(int dummy, final Team169cwat2 tr, R<@tr> r) -> void test1(final Team169cwat2 tb, R<@tb> r)\n" +
			    "            with { tr -> tb, r -> r }\n" +
			    "\n" +
			    "        public abstract void test2();\n" +
			    "        test2 -> test2;\n" +
			    "    }\n" +
			    "    public Team169cwat2() {\n" +
			    "        R r1 = new R(new T169cwat2(\"NOTOK\"));\n" +
			    "        R r2 = new R(new T169cwat2(\"OK\"));\n" +
			    "        r1.test1(-3, this, r2);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team169cwat2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T169cwat2.java",
			    "\n" +
			    "public team class T169cwat2 {\n" +
			    "    private String val;\n" +
			    "    public T169cwat2(String v) {\n" +
			    "        val = v;\n" +
			    "    }\n" +
			    "    public void test2() {\n" +
			    "        System.out.print(val);\n" +
			    "    }\n" +
			    "    public void test1(final Team169cwat2 t, R<@t> r) {\n" +
			    "        r.test2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a method return type is anchored to a non final variable
    // 1.6.10-otjld-method-return-illegal-anchor-1
    public void test1610_methodReturnIllegalAnchor1() {
        runNegativeTestMatching(
            new String[] {
		"Team1610mria1.java",
			    "\n" +
			    "public team class Team1610mria1 {\n" +
			    "    public class R {}\n" +
			    "    Team1610mria1 t;\n" +
			    "    public R<@t> test() {\n" +
			    "        return null; \n" +
			    "    };\n" +
			    "}\n" +
			    "    \n"
            },
            "1.2.2(c)");
    }

    // a method return type is anchored to a non team variable - NotFound
    // 1.6.10-otjld-method-return-illegal-anchor-2
    public void test1610_methodReturnIllegalAnchor2() {
        runNegativeTestMatching(
            new String[] {
		"Team1610mria2.java",
			    "\n" +
			    "public team class Team1610mria2 {\n" +
			    "    public class R {}\n" +
			    "    final Object t = null;\n" +
			    "    public R<@t> test() {\n" +
			    "        return null; \n" +
			    "    };\n" +
			    "}\n" +
			    "    \n"
            },
            "9.2.1");
    }

    // a method return type is anchored to a non team variable
    // 1.6.10-otjld-method-return-illegal-anchor-2a
    public void test1610_methodReturnIllegalAnchor2a() {
        runNegativeTestMatching(
            new String[] {
		"Team1610mria2a.java",
			    "\n" +
			    "import java.util.Map;\n" +
			    "import java.util.Map.Entry;\n" +
			    "public team class Team1610mria2a {\n" +
			    "    public class R {}\n" +
			    "    final Map<String,String> t = null;\n" +
			    "    public Entry<@t> test() {\n" +
			    "        return null; \n" +
			    "    };\n" +
			    "}\n" +
			    "    \n"
            },
            "9.2.1");
    }

    // a method return type is anchored to an unresolvable variable (1. path element)
    // 1.6.10-otjld-method-return-illegal-anchor-3
    public void test1610_methodReturnIllegalAnchor3() {
        runNegativeTestMatching(
            new String[] {
		"Team1610mria3.java",
			    "\n" +
			    "public team class Team1610mria3 {\n" +
			    "    public class R {}\n" +
			    "    public R<@t> test() {\n" +
			    "        return null; \n" +
			    "    };\n" +
			    "}\n" +
			    "    \n"
            },
            "resolve");
    }

    // a method return type is anchored to an unresolvable variable (2. path element)
    // 1.6.10-otjld-method-return-illegal-anchor-4
    public void test1610_methodReturnIllegalAnchor4() {
        runNegativeTestMatching(
            new String[] {
		"Team1610mria4.java",
			    "\n" +
			    "public team class Team1610mria4 {\n" +
			    "    public class R {}\n" +
			    "    final Team1610mria4 t = null;\n" +
			    "    public R<@t.f> test() {\n" +
			    "        return null; \n" +
			    "    };\n" +
			    "}\n" +
			    "    \n"
            },
            "----------\n" + 
    		"1. ERROR in Team1610mria4.java (at line 5)\n" + 
    		"	public R<@t.f> test() {\n" + 
    		"	            ^\n" + 
    		"Parameter \'f\' cannot be resolved (OTJLD 9.2.1).\n" + 
    		"----------\n");
    }

    // a parameter type is defined relative to tthis, in a message send this is updated to the team of the receiver - a role
    // 1.6.11-otjld-parameter-type-anchored-updated-to-receiver-1
    public void test1611_parameterTypeAnchoredUpdatedToReceiver1() {
       
       runConformTest(
            new String[] {
		"Team1611ptautr1.java",
			    "\n" +
			    "public team class Team1611ptautr1 {\n" +
			    "	public class R {\n" +
			    "		String val;\n" +
			    "		public R(String v) {\n" +
			    "			val = v;\n" +
			    "		}\n" +
			    "		void print() {\n" +
			    "			System.out.print(val);\n" +
			    "		}\n" +
			    "		public void test(R other) {\n" +
			    "			other.print();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		final Team1611ptautr1 t = new Team1611ptautr1();\n" +
			    "		R<@t> r1 = t.new R(\"NOTOK\");\n" +
			    "		R<@t> r2 = t.new R(\"OK\");\n" +
			    "		r1.test(r2);\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // a parameter type is defined relative to tthis, in a message send this is updated to the receiver - a array-reference of role
    // 1.6.11-otjld-parameter-type-anchored-updated-to-receiver-2
    public void test1611_parameterTypeAnchoredUpdatedToReceiver2() {
       
       runConformTest(
            new String[] {
		"Team1611ptautr2.java",
			    "\n" +
			    "public team class Team1611ptautr2 {\n" +
			    "	public class R {\n" +
			    "		String val;\n" +
			    "		public R(String v) {\n" +
			    "			val = v;\n" +
			    "		}\n" +
			    "		void print() {\n" +
			    "			System.out.print(val);\n" +
			    "		}\n" +
			    "		public void test(R other) {\n" +
			    "			other.print();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		final Team1611ptautr2 t = new Team1611ptautr2();\n" +
			    "		R<@t>[] r1 = new R<@t>[]{t.new R(\"NOTOK\")};\n" +
			    "		R<@t> r2 = t.new R(\"OK\");\n" +
			    "		r1[0].test(r2);\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // an externalized role via static anchor is used as receiver and argument
    // 1.6.11-otjld-parameter-type-anchored-updated-to-receiver-3
    public void test1611_parameterTypeAnchoredUpdatedToReceiver3() {
       
       runConformTest(
            new String[] {
		"T1611ptautr3Main.java",
			    "\n" +
			    "public class T1611ptautr3Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        R<@Team1611ptautr3.INSTANCE> r= Team1611ptautr3.INSTANCE.getR();\n" +
			    "        r.test(r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1611ptautr3.java",
			    "\n" +
			    "public team class Team1611ptautr3 {\n" +
			    "    public static final Team1611ptautr3 INSTANCE= new Team1611ptautr3();\n" +
			    "    public class R {\n" +
			    "        public void test(R r) {\n" +
			    "            if (r == this)\n" +
			    "                System.out.print(\"OK\");\n" +
			    "            else\n" +
			    "                System.out.print(\"NOK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public R getR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // on an anchor path, an implitly reachable shortcut is taken, which is dropped from the path
    // 1.6.12-otjld-path-drops-implicitly-reachable-1
    public void test1612_pathDropsImplicitlyReachable1() {
       
       runConformTest(
            new String[] {
		"Team1612pdir1_2.java",
			    "\n" +
			    "public team class Team1612pdir1_2 {\n" +
			    "	public final Team1612pdir1_1 _anchor;\n" +
			    "	public R1 getR1() { return new R1(); }\n" +
			    "	Team1612pdir1_2 () {\n" +
			    "		_anchor = new Team1612pdir1_1();\n" +
			    "	}\n" +
			    "	public void test() {\n" +
			    "		R2 r2 = new R2(new R1());\n" +
			    "		r2.test();\n" +
			    "	}\n" +
			    "	\n" +
			    "	public class R1 {\n" +
			    "		public R0<@_anchor> getR0() {\n" +
			    "			System.out.print(\"O\");\n" +
			    "			return _anchor.getR0();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	\n" +
			    "	protected class R2 {\n" +
			    "		R1 _sibling;\n" +
			    "		public R2(R1 sibling) { _sibling = sibling; }\n" +
			    "		protected void test() {\n" +
			    "			R0<@_anchor> r0 = _sibling.getR0();\n" +
			    "			r0.test();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		Team1612pdir1_2 t = new Team1612pdir1_2();\n" +
			    "		t.test();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"Team1612pdir1_1.java",
			    "\n" +
			    "public team class Team1612pdir1_1 {\n" +
			    "	public class R0 {\n" +
			    "		public void test() {\n" +
			    "			System.out.print(\"K\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R0 getR0() { return new R0(); }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // on an anchor path, an implitly reachable shortcut is taken, which is dropped from the path - tested in team method
    // 1.6.12-otjld-path-drops-implicitly-reachable-2
    public void test1612_pathDropsImplicitlyReachable2() {
       
       runConformTest(
            new String[] {
		"Team1612pdir2_2.java",
			    "\n" +
			    "public team class Team1612pdir2_2 {\n" +
			    "	public final Team1612pdir2_1 _anchor;\n" +
			    "	public R1 getR1() { return new R1(); }\n" +
			    "	Team1612pdir2_2 () {\n" +
			    "		_anchor = new Team1612pdir2_1();\n" +
			    "	}\n" +
			    "	public void test() {\n" +
			    "		R2 r2 = new R2(new R1());\n" +
			    "		R0<@_anchor> r0 = r2.getR0();\n" +
			    "		r0.test();\n" +
			    "	}\n" +
			    "	\n" +
			    "	public class R1 {\n" +
			    "		public R0<@_anchor> getR0() {\n" +
			    "			System.out.print(\"O\");\n" +
			    "			return _anchor.getR0();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	\n" +
			    "	protected class R2 {\n" +
			    "		R1 _sibling;\n" +
			    "		protected R2(R1 sibling) { _sibling = sibling; }\n" +
			    "		protected  R0<@_anchor> getR0() {\n" +
			    "			return _sibling.getR0();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		Team1612pdir2_2 t = new Team1612pdir2_2();\n" +
			    "		t.test();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n",
		"Team1612pdir2_1.java",
			    "\n" +
			    "public team class Team1612pdir2_1 {\n" +
			    "	public class R0 {\n" +
			    "		public void test() {\n" +
			    "			System.out.print(\"K\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public R0 getR0() { return new R0(); }\n" +
			    "}\n" +
			    "	\n"
            },
            "OK");
    }

    // on an anchor path, an implitly reachable shortcut is taken, which is dropped from the path - same team
    // 1.6.12-otjld-path-drops-implicitly-reachable-3
    public void test1612_pathDropsImplicitlyReachable3() {
       
       runConformTest(
            new String[] {
		"Team1612pdir3.java",
			    "\n" +
			    "public team class Team1612pdir3 {\n" +
			    "	public final Team1612pdir3 _anchor;\n" +
			    "	public R1 getR1() { return new R1(); }\n" +
			    "	Team1612pdir3 () {\n" +
			    "		_anchor = this;\n" +
			    "	}\n" +
			    "	public void test() {\n" +
			    "		R2 r2 = new R2(new R1());\n" +
			    "		R1<@_anchor> r1 = r2.getR1();\n" +
			    "		r1.test();\n" +
			    "	}\n" +
			    "	\n" +
			    "	public class R1 {\n" +
			    "		public R1<@_anchor> getR1() {\n" +
			    "			System.out.print(\"O\");\n" +
			    "			return _anchor.getR1();\n" +
			    "		}\n" +
			    "		public void test() {\n" +
			    "			System.out.print(\"K\");\n" +
			    "		}\n" +
			    "	}\n" +
			    "	\n" +
			    "	protected class R2 {\n" +
			    "		R1 _sibling;\n" +
			    "		protected R2(R1 sibling) { _sibling = sibling; }\n" +
			    "		protected R1<@_anchor> getR1() {\n" +
			    "			return _sibling.getR1();\n" +
			    "		}\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		Team1612pdir3 t = new Team1612pdir3();\n" +
			    "		t.test();\n" +
			    "	}\n" +
			    "}	\n" +
			    "	\n"
            },
            "OK");
    }

    // a field is accessed qualified with a interface name, WITNESS for TPX-256 (fixed between 0.8.1 and 0.8.2)
    // 1.6.13-otjld-static-field-accessed-qualified
    public void test1613_staticFieldAccessedQualified() {
       
       runConformTest(
            new String[] {
		"T1613.java",
			    "\n" +
			    "public class T1613 {\n" +
			    "    static final Team1613 t = I1613.t;\n" +
			    "    public static void main(String[] args) {\n" +
			    "        t.getR().test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1613.java",
			    "\n" +
			    "public team class Team1613 {\n" +
			    "    String val;\n" +
			    "    public Team1613(String v) { val = v; }\n" +
			    "    public class R {\n" +
			    "        public void test() { System.out.print(val); }\n" +
			    "    }\n" +
			    "    public R getR() { return new R(); }\n" +
			    "}    \n" +
			    "    \n",
		"I1613.java",
			    "\n" +
			    "public interface I1613 {\n" +
			    "    final static Team1613 t = new Team1613(\"OK\");\n" +
			    "}        \n" +
			    "    \n"
            },
            "OK");
    }

    // A role played by a non-existing anchored type unsuccessful attempt to WITNESS TPX-249
    // 1.6.14-otjld-playedby-non-existing
    public void test1614_playedbyNonExisting() {
        runNegativeTestMatching(
            new String[] {
		"Team1614pne_3.java",
			    "\n" +
			    " public team class Team1614pne_3 { \n" +
			    "    final Team1614pne_1 t1 = new Team1614pne_1(); \n" +
			    "    public class Role3 playedBy Role1<@t1> {}\n" +
			    " } \n" +
			    "    \n",
		"Team1614pne_2.java",
			    "\n" +
			    " public team class Team1614pne_2 { \n" +
			    "    public class Role2 { \n" +
			    "       Foo f; \n" +
			    "    } \n" +
			    " } \n" +
			    "    \n",
		"Team1614pne_1.java",
			    " \n" +
			    " public team class Team1614pne_1 extends Team1614pne_2 {} \n" +
			    "    \n"
            },
            "resolve");
    }

    // assigning a role which is created as externalized but the anchor has a non-final prefix
    // 1.6.15-otjld-assigning-externalized-creation-1
    public void test1615_assigningExternalizedCreation1() {
        runNegativeTestMatching(
            new String[] {
		"Team1615aec1.java",
			    "\n" +
			    "public team class Team1615aec1 {\n" +
			    "    public class R {}\n" +
			    "    public void test(R r) {}\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team1615aec1 t = new Team1615aec1();\n" +
			    "        t.test(t.new R());\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "1.2.2(c)");
    }

    // a role is casted to its externalized type
    // 1.6.16-otjld-casting-to-externalized-1
    public void test1616_castingToExternalized1() {
       
       runConformTest(
            new String[] {
		"Team1616cte1.java",
			    "\n" +
			    "public team class Team1616cte1 {\n" +
			    "    public class R { \n" +
			    "        public void test() { \n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1616cte1 t = new Team1616cte1();\n" +
			    "        Object o = t.new R();\n" +
			    "        R<@t> r = (R<@t>)o;\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a role is casted to its externalized type - array type
    // 1.6.16-otjld-casting-to-externalized-1a
    public void test1616_castingToExternalized1a() {
       
       runConformTest(
            new String[] {
		"Team1616cte1a.java",
			    "\n" +
			    "public team class Team1616cte1a {\n" +
			    "    public class R { \n" +
			    "        public void test() { \n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1616cte1a t = new Team1616cte1a();\n" +
			    "        Object[] o = new R<@t>[]{t.new R()};\n" +
			    "        R<@t>[] r = (R<@t>[])o;\n" +
			    "        r[0].test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a role is casted to its externalized type - array type , cast error
    // 1.6.16-otjld-casting-to-externalized-1f
    public void test1616_castingToExternalized1f() {
       
       runConformTest(
            new String[] {
		"Team1616cte1f.java",
			    "\n" +
			    "public team class Team1616cte1f {\n" +
			    "    public class R { \n" +
			    "        public void test() { \n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1616cte1f t = new Team1616cte1f();\n" +
			    "        Object[] o = new Object[]{t.new R()};\n" +
			    "        try {\n" +
			    "            R<@t>[] r = (R<@t>[])o;\n" +
			    "            r[0].test();\n" +
			    "        } catch (org.objectteams.RoleCastException e) {\n" +
			    "            System.out.print(\"NOK\");\n" +
			    "        } catch (ClassCastException e) {\n" +
			    "            System.out.print(\"Caught\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "Caught");
    }

    // a role is casted to its externalized type - array type , team instance mismatch
    // 1.6.16-otjld-casting-to-externalized-1g
    public void test1616_castingToExternalized1g() {
       
       runConformTest(
            new String[] {
		"Team1616cte1g.java",
			    "\n" +
			    "public team class Team1616cte1g {\n" +
			    "    public class R { \n" +
			    "        public void test() { \n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1616cte1g t = new Team1616cte1g();\n" +
			    "        final Team1616cte1g t2 = new Team1616cte1g();\n" +
			    "        Object[] o = new R<@t>[]{t.new R()};\n" +
			    "        try {        \n" +
			    "            R<@t2>[] r = (R<@t2>[])o;\n" +
			    "            r[0].test();\n" +
			    "        } catch (org.objectteams.RoleCastException rce) {\n" +
			    "            System.out.print(\"Caught\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "Caught");
    }

    // a role is casted to its externalized type, wrong team instance
    // 1.6.16-otjld-casting-to-externalized-2
    public void test1616_castingToExternalized2() {
       
       runConformTest(
            new String[] {
		"Team1616cte2.java",
			    "\n" +
			    "public team class Team1616cte2 {\n" +
			    "    public class R { \n" +
			    "        public void test() { \n" +
			    "            System.out.print(\"NOK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1616cte2 t = new Team1616cte2();\n" +
			    "        final Team1616cte2 t2 = new Team1616cte2();\n" +
			    "        Object o = t.new R();\n" +
			    "        try {\n" +
			    "            R<@t2> r = (R<@t2>)o;\n" +
			    "            r.test();\n" +
			    "        } catch (org.objectteams.RoleCastException ex) {\n" +
			    "            System.out.print(\"Caught\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "Caught");
    }

    // a role is casted to its externalized type - wrong role type
    // 1.6.16-otjld-casting-to-externalized-3
    public void test1616_castingToExternalized3() {
       
       runConformTest(
            new String[] {
		"Team1616cte3.java",
			    "\n" +
			    "public team class Team1616cte3 {\n" +
			    "    public class R { \n" +
			    "        public void test() { \n" +
			    "            System.out.print(\"NOK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public class R2 {}\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1616cte3 t = new Team1616cte3();\n" +
			    "        Object o = t.new R();\n" +
			    "        try {\n" +
			    "            R2<@t> r = (R2<@t>)o;\n" +
			    "            System.out.print(r);\n" +
			    "        } catch (ClassCastException ex) {\n" +
			    "            System.out.print(\"Caught\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "Caught");
    }

    // a role type return is anchored to a parameter of the same method
    // 1.6.17-otjld-return-anchored-to-parameter-1
    public void test1617_returnAnchoredToParameter1() {
       
       runConformTest(
            new String[] {
		"Team1617ratp1.java",
			    "\n" +
			    "public team class Team1617ratp1 {\n" +
			    "    public class R {\n" +
			    "        public void test() {\n" +
			    "            Team1617ratp1.this.print();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    String val;\n" +
			    "    void print() {\n" +
			    "        System.out.print(val);\n" +
			    "    }\n" +
			    "    Team1617ratp1(String v) { \n" +
			    "        val = v;\n" +
			    "    }\n" +
			    "    R<@t> test(final Team1617ratp1 t) {\n" +
			    "        return t.new R();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1617ratp1 tok = new Team1617ratp1(\"OK\");\n" +
			    "        Team1617ratp1 t = new Team1617ratp1(\"NOK\");\n" +
			    "        R<@tok> r = t.test(tok);\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a role type return is anchored to a parameter of the same method
    // 1.6.17-otjld-return-anchored-to-parameter-2
    public void test1617_returnAnchoredToParameter2() {
       
       runConformTest(
            new String[] {
		"Team1617ratp2.java",
			    "\n" +
			    "public team class Team1617ratp2 {\n" +
			    "    public class R {\n" +
			    "        public void test() {\n" +
			    "            Team1617ratp2.this.print();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    String val;\n" +
			    "    void print() {\n" +
			    "        System.out.print(val);\n" +
			    "    }\n" +
			    "    Team1617ratp2(String v) { \n" +
			    "        val = v;\n" +
			    "    }\n" +
			    "    R<@t> test (final Team1617ratp2 t, R<@t> r) {\n" +
			    "        return r;\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1617ratp2 tok = new Team1617ratp2(\"OK\");\n" +
			    "        R<@tok> r1 = tok.new R();\n" +
			    "        Team1617ratp2 t = new Team1617ratp2(\"NOK\");\n" +
			    "        R<@tok> r = t.test(tok, r1);\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a role type return (array) is anchored to a parameter of the same method
    // 1.6.17-otjld-return-anchored-to-parameter-3
    public void test1617_returnAnchoredToParameter3() {
       
       runConformTest(
            new String[] {
		"Team1617ratp3.java",
			    "\n" +
			    "public team class Team1617ratp3 {\n" +
			    "    public class R {\n" +
			    "        public void test() {\n" +
			    "            Team1617ratp3.this.print();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    String val;\n" +
			    "    void print() {\n" +
			    "        System.out.print(val);\n" +
			    "    }\n" +
			    "    Team1617ratp3(String v) { \n" +
			    "        val = v;\n" +
			    "    }\n" +
			    "    R<@t>[] test(final Team1617ratp3 t) {\n" +
			    "        return new R<@t>[]{ t.new R()};\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1617ratp3 tok = new Team1617ratp3(\"OK\");\n" +
			    "        Team1617ratp3 t = new Team1617ratp3(\"NOK\");\n" +
			    "        R<@tok>[] rs = t.test(tok);\n" +
			    "        rs[0].test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // a role type return is anchored indirectly to a parameter of the same method
    // 1.6.17-otjld-return-anchored-to-parameter-4
    public void test1617_returnAnchoredToParameter4() {
       
       runConformTest(
            new String[] {
		"Team1617ratp4.java",
			    "\n" +
			    "public team class Team1617ratp4 {\n" +
			    "    public final Team1617ratp4 same = this;\n" +
			    "    public class R {\n" +
			    "        public void test() {\n" +
			    "            Team1617ratp4.this.print();\n" +
			    "        }\n" +
			    "    }\n" +
			    "    String val;\n" +
			    "    void print() {\n" +
			    "        System.out.print(val);\n" +
			    "    }\n" +
			    "    Team1617ratp4(String v) { \n" +
			    "        val = v;\n" +
			    "    }\n" +
			    "    R<@t.same> test(final Team1617ratp4 t) {\n" +
			    "        return t.same.new R();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1617ratp4 tok = new Team1617ratp4(\"OK\");\n" +
			    "        Team1617ratp4 t = new Team1617ratp4(\"NOK\");\n" +
			    "        R<@tok> r = t.test(tok);\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
            },
            "OK");
    }

    // an anchor needs a static qualifier to disambiguate
    // 1.6.18-otjld-anchor-problems-1
    public void test1618_anchorProblems1() {
       
       runConformTest(
            new String[] {
		"T1618ap1.java",
			    "\n" +
			    "public class T1618ap1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        R<@Team1618ap1.f> r = Team1618ap1.f.new R();\n" +
			    "        r.createR();\n" +
			    "        r.r.test(r.f);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1618ap1.java",
			    "\n" +
			    "public team class Team1618ap1 {\n" +
			    "    public static final Team1618ap1 f = new Team1618ap1();\n" +
			    "    public class R {\n" +
			    "        public int f;\n" +
			    "        public R<@Team1618ap1.f> r;\n" +
			    "        public void test(int i) {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        public void createR() {\n" +
			    "            r = Team1618ap1.f.new R();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // two anchored fields create a recursion between two types
    // 1.6.18-otjld-anchor-problems-2
    public void test1618_anchorProblems2() {
       this.compileOrder = new String[][]{{"Team1618ap2_1.java","Team1618ap2_2.java"}, {"T1618ap2.java"}};
       runConformTest(
            new String[] {
		"T1618ap2.java",
			    "\n" +
			    "public class T1618ap2 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        new Team1618ap2_2().r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1618ap2_1.java",
			    "\n" +
			    "public team class Team1618ap2_1 {\n" +
			    "    public static final Team1618ap2_2 f = new Team1618ap2_2();\n" +
			    "    public R2<@f> r;\n" +
			    "    public class R1 {\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1618ap2_2.java",
			    "\n" +
			    "public team class Team1618ap2_2 {\n" +
			    "    public final Team1618ap2_1 f = new Team1618ap2_1();\n" +
			    "    public R1<@f> r;\n" +
			    "    public Team1618ap2_2() {\n" +
			    "        r = f.new R1();\n" +
			    "    }\n" +
			    "    public class R2 {}\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an anchor needs a static qualifier to disambiguate - misinterpretation leads to recursion
    // 1.6.18-otjld-anchor-problems-3
    public void test1618_anchorProblems3() {
       
       runConformTest(
            new String[] {
		"T1618ap3.java",
			    "\n" +
			    "public class T1618ap3 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        R<@Team1618ap3.f> r = Team1618ap3.f.new R();\n" +
			    "        r.createR();\n" +
			    "        r.r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1618ap3.java",
			    "\n" +
			    "public team class Team1618ap3 {\n" +
			    "    public static final Team1618ap3 f = new Team1618ap3();\n" +
			    "    public class R {\n" +
			    "        public R<@Team1618ap3.f> f; // should not be used at all\n" +
			    "        public R<@Team1618ap3.f> r;\n" +
			    "        public void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        public void createR() {\n" +
			    "            r = Team1618ap3.f.new R();\n" +
			    "        }        \n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a non-static anchor field is referenced from a static context
    // 1.6.18-otjld-anchor-problems-4
    public void test1618_anchorProblems4() {
        runNegativeTestMatching(
            new String[] {
		"T1618ap4Main.java",
			    "\n" +
			    "public class T1618ap4Main {\n" +
			    "    final Team1618ap4 other = new Team1618ap4();\n" +
			    "    static void test() {\n" +
			    "        @SuppressWarnings(\"unused\")\n" +
			    "        Role<@other> r = new Role<@other>();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1618ap4.java",
			    "\n" +
			    "public team class Team1618ap4 {\n" +
			    "    public class Role {}\n" +
			    "}\n" +
			    "    \n"
            },
            "static reference to the non-static field");
    }

    // a non-static anchor field is referenced from a static context which, however is enclosed in a non-static context
    // 1.6.18-otjld-anchor-problems-5
    public void test1618_anchorProblems5() {
        runConformTest(
            new String[] {
		"Team1618ap5.java",
			    "\n" +
			    "public team class Team1618ap5 {\n" +
			    "    public class Role {}\n" +
			    "}\n" +
			    "    \n",
		"T1618ap5Main.java",
			    "\n" +
			    "public team class T1618ap5Main {\n" +
			    "    final Team1618ap5 other = new Team1618ap5();\n" +
			    "    protected class Inner {\n" +
			    "        static void test() {\n" +
			    "            Role<@other> r = new Role<@other>();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            });
    }

    // a non-static anchor field is referenced from a static context
    // 1.6.18-otjld-anchor-problems-6
    public void test1618_anchorProblems6() {
        runNegativeTestMatching(
            new String[] {
		"T1618ap6Main.java",
			    "\n" +
			    "public class T1618ap6Main {\n" +
			    "    final Team1618ap6 other = new Team1618ap6();\n" +
			    "    static class Inner {\n" +
			    "        static void test() {\n" +
			    "            @SuppressWarnings(\"unused\")\n" +
			    "            Role<@other> r = new Role<@other>();\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1618ap6.java",
			    "\n" +
			    "public team class Team1618ap6 {\n" +
			    "    public class Role {}\n" +
			    "}\n" +
			    "    \n"
            },
            "static reference to the non-static field");
    }

    // the argument of a constructor is anchored
    // 1.6.19-otjld-anchored-constructor-arg-1
    public void test1619_anchoredConstructorArg1() {
       
       runConformTest(
            new String[] {
		"T1619Main.java",
			    "\n" +
			    "public class T1619Main {\n" +
			    "    T1619Main(final Team1619 t, R<@t> r) {\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1619 t = new Team1619();\n" +
			    "        new T1619Main(t, t.new R());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1619.java",
			    "\n" +
			    "public team class Team1619 {\n" +
			    "    public class R {\n" +
			    "        public void test () {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // the example from the otjld 1.2.2
    // 1.6.20-otjld-doc-example-1
    public void test1620_docExample1() {
       
       runConformTest(
            new String[] {
		"T1620de1.java",
			    "\n" +
			    "public class T1620de1 {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final FlightBonus fb = new FlightBonus();\n" +
			    "        ClearAction ca = new ClearAction(fb, fb.getSubscriber());\n" +
			    "        ca.actionPerformed();\n" +
			    "        ca = null;\n" +
			    "        System.gc();\n" +
			    "		 System.runFinalization();\n" + // hopefully triggers: ca.finalize();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"FlightBonus.java",
			    "\n" +
			    "team class FlightBonus {\n" +
			    "    public class Subscriber {\n" +
			    "        public int credits = 5000;\n" +
			    "        public void clearCredits() { \n" +
			    "            credits = -1;\n" +
			    "        };\n" +
			    "    }\n" +
			    "    Subscriber getSubscriber() {\n" +
			    "        return new Subscriber();\n" +
			    "    }\n" +
			    "    void unshow(Subscriber subscr) {\n" +
			    "        System.out.print(\"no longer showing subscr with \"+subscr.credits+\" credits\");\n" +
			    "    }\n" +
			    " }\n" +
			    "    \n",
		"ClearAction.java",
			    "\n" +
			    "class ClearAction {\n" +
			    "    final FlightBonus context;\n" +
			    "    Subscriber<@context> subscriber;\n" +
			    "    ClearAction (final FlightBonus bonus,\n" +
			    "                Subscriber<@bonus> subscr) {\n" +
			    "        context    = bonus;   // unique assignemt to 'context'\n" +
			    "        subscriber = subscr;\n" +
			    "    }\n" +
			    "    void actionPerformed () {\n" +
			    "        subscriber.clearCredits();\n" +
			    "    }\n" +
			    "    public void finalize () {\n" +
			    "        context.unshow(subscriber);\n" +
			    "    }\n" +
			    " }\n" +
			    "    \n"
            },
            "no longer showing subscr with -1 credits");
    }

    // 
    // 1.6.21-otjld-constructor-receives-externalized-role-1
    public void test1621_constructorReceivesExternalizedRole1() {
       
       runConformTest(
            new String[] {
		"T1621crer1Main.java",
			    "\n" +
			    "public class T1621crer1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team1621crer1 aTeam = new Team1621crer1();\n" +
			    "        T1621crer1_2 t = aTeam.getT();\n" +
			    "        t.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T1621crer1_0.java",
			    "\n" +
			    "public class T1621crer1_0 {\n" +
			    "    int i = 42;\n" +
			    "    String s = \"fish\";\n" +
			    "}\n" +
			    "    \n",
		"T1621crer1_1.java",
			    "\n" +
			    "public class T1621crer1_1 {\n" +
			    "    T1621crer1_1(int i, String s) {\n" +
			    "        System.out.print(i);\n" +
			    "        System.out.print(s);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T1621crer1_2.java",
			    "\n" +
			    "public class T1621crer1_2 extends T1621crer1_1 {\n" +
			    "    final Team1621crer1 anchor;\n" +
			    "    R<@anchor> r;\n" +
			    "    public T1621crer1_2(final Team1621crer1 anchor, R<@anchor> r) {\n" +
			    "        super(getI(anchor,r), getS(r));\n" +
			    "        this.anchor = anchor;\n" +
			    "        this.r = r;\n" +
			    "    }\n" +
			    "    void test() {\n" +
			    "        this.r.test();\n" +
			    "    }\n" +
			    "    static int getI(final Team1621crer1 anchor, R<@anchor> r) {\n" +
			    "        return r.getI();\n" +
			    "    }\n" +
			    "    static String getS(T1621crer1_0 t) {\n" +
			    "        return t.s;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1621crer1.java",
			    "\n" +
			    "public team class Team1621crer1 {\n" +
			    "    public class R playedBy T1621crer1_0 {\n" +
			    "        protected R() { base(); }\n" +
			    "        public void test() { \n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "        public abstract int getI();\n" +
			    "        int getI() -> get int i;\n" +
			    "    }\n" +
			    "    T1621crer1_2 getT() {\n" +
			    "        return new T1621crer1_2(this, new R());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "42fishOK");
    }

    // a shorthand defined callout method is called on an externalized role
    // 1.6.22-otjld-call-to-callout-of-externalized-1
    public void test1622_callToCalloutOfExternalized1() {
       
       runConformTest(
            new String[] {
		"T1622ctcoe1Main.java",
			    "\n" +
			    "public class T1622ctcoe1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        R<@Team1622ctcoe1.INSTANCE> r= Team1622ctcoe1.INSTANCE.getR(new T1622ctcoe1());\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"T1622ctcoe1.java",
			    "\n" +
			    "public class T1622ctcoe1 {\n" +
			    "    void test() {\n" +
			    "        System.out.print(\"OK\");\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1622ctcoe1.java",
			    "\n" +
			    "public team class Team1622ctcoe1 {\n" +
			    "    public static final Team1622ctcoe1 INSTANCE= new Team1622ctcoe1();\n" +
			    "    public class R playedBy T1622ctcoe1 {\n" +
			    "        public abstract void test();\n" +
			    "        void test() -> void test();\n" +
			    "    }\n" +
			    "    public R getR(T1622ctcoe1 as R o) {\n" +
			    "        return o;\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a method parameter has a type anchored to another paramter
    // 1.6.23-otjld-type-anchored-to-parameter-1
    public void test1623_typeAnchoredToParameter1() {
       
       runConformTest(
            new String[] {
		"T1623tatp1Main.java",
			    "\n" +
			    "public class T1623tatp1Main {\n" +
			    "    void test(final Team1623tatp1 t, R<@t> r) {\n" +
			    "        t.test(r);\n" +
			    "    }\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1623tatp1 t1 = new Team1623tatp1();\n" +
			    "        new T1623tatp1Main().test(t1, t1.new R());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1623tatp1.java",
			    "\n" +
			    "public team class Team1623tatp1 {\n" +
			    "    public class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test(R r) {\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // a method parameter has a type anchored to another paramter, inside method uses role for other method call
    // 1.6.23-otjld-type-anchored-to-parameter-2
    public void test1623_typeAnchoredToParameter2() {
        runNegativeTestMatching(
            new String[] {
		"T1623tatp2Main.java",
			    "\n" +
			    "public class T1623tatp2Main {\n" +
			    "    void test(final Team1623tatp2 t, R<@t> r) {\n" +
			    "        t.test(r);\n" +
			    "        Team1623tatp2 tt = new Team1623tatp2();\n" +
			    "        this.test(tt, r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1623tatp2.java",
			    "\n" +
			    "public team class Team1623tatp2 {\n" +
			    "    public class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test(R r) {\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "not applicable");
    }

    // a method parameter has a type anchored to another paramter, inside method uses role for other method call - team is fresh instance
    // 1.6.23-otjld-type-anchored-to-parameter-3
    public void test1623_typeAnchoredToParameter3() {
        runNegativeTestMatching(
            new String[] {
		"T1623tatp3Main.java",
			    "\n" +
			    "public class T1623tatp3Main {\n" +
			    "    void test(final Team1623tatp3 t, R<@t> r) {\n" +
			    "        t.test(r);\n" +
			    "        this.test(new Team1623tatp3(), r);\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1623tatp3.java",
			    "\n" +
			    "public team class Team1623tatp3 {\n" +
			    "    public class R {\n" +
			    "        protected void test() {\n" +
			    "            System.out.print(\"OK\");\n" +
			    "        }\n" +
			    "    }\n" +
			    "    public void test(R r) {\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "not applicable");
    }

    // a method with externalized role parameter is called on a new team expression
    // 1.6.24-otjld-new-team-instance-1
    public void test1624_newTeamInstance1() {
        runNegativeTestMatching(
            new String[] {
		"T1624nti1Main.java",
			    "\n" +
			    "public class T1624nti1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        final Team1624nti1 t = new Team1624nti1();\n" +
			    "        new Team1624nti1().doR(new R<@t>());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1624nti1.java",
			    "\n" +
			    "public team class Team1624nti1 {\n" +
			    "    public class R {}\n" +
			    "    public void doR(R r) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "not applicable");
    }

    // a method call argument is a fresh role anchored to a fresh team instance
    // 1.6.24-otjld-new-team-instance-2
    public void test1624_newTeamInstance2() {
        runNegativeTestMatching(
            new String[] {
		"T1624nti2Main.java",
			    "\n" +
			    "public class T1624nti2Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Team1624nti2 t = new Team1624nti2();\n" +
			    "        t.doR(new Team1624nti2().new R());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1624nti2.java",
			    "\n" +
			    "public team class Team1624nti2 {\n" +
			    "    public class R {}\n" +
			    "    public void doR(R r) {}\n" +
			    "}\n" +
			    "    \n"
            },
            "not applicable");
    }

    // a team method returns a role type - the caller is not allowed to see the role type but uses Object
    // 1.6.25-otjld-return-is-non-externalizable-role-1
    public void test1625_returnIsNonExternalizableRole1() {
       
       runConformTest(
            new String[] {
		"T1625riner1Main.java",
			    "\n" +
			    "public class T1625riner1Main {\n" +
			    "    public static void main(String[] args) {\n" +
			    "        Object o = new Team1625riner1().getR();\n" +
			    "        System.out.print(o.toString());\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team1625riner1.java",
			    "\n" +
			    "public team class Team1625riner1 {\n" +
			    "    protected class R {\n" +
			    "        public String toString() { return \"OK\"; }\n" +
			    "    }\n" +
			    "    public R getR() {\n" +
			    "        return new R();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
            },
            "OK");
    }

    // an enhanced for loop iterates over an Iterable of externalized roles
    public void test1626_loopingOverExternalized1() {
    	runConformTest(
    		new String[] {
    	"T1626loe1Main.java",
    		"public class T1626loe1Main {\n" +
    		"	public static void main(String... args) {\n" +
    		"		final Team1626loe1 t = new Team1626loe1();\n" +
    		"		R1<@t> r1 = new R1<@t>();\n" +
    		"		for (R2<@t> r2 : r1)\n" +
    		"			System.out.print(r2);\n" +
    		"	}\n" +
    		"}\n",
    	"Team1626loe1.java",
    		"import java.util.ArrayList;\n" +
    		"import java.util.Iterator;\n" +
    		"public team class Team1626loe1 {\n" +
    		"	public class R1 implements Iterable<R2> {\n" +
    		"		public Iterator<R2> iterator() {\n" +
    		"			ArrayList<R2> list = new ArrayList<R2>();\n" +
    		"			list.add(new R2(\"Ra\"));\n" +
    		"			list.add(new R2(\"Rb\"));\n" +
    		"			return list.iterator();\n" +
    		"		}\n" +
    		"	}\n" +
    		"	public class R2 {\n" +
    		"		String val;\n" +
    		"		public R2(String v) { this.val=v;}\n" +
    		"		@Override public String toString() { return this.val; }\n" +
    		"	}\n" +
    		"}\n"
    		},
    		"RaRb");
    }

    // an enhanced for loop iterates over an array of externalized roles
    public void test1626_loopingOverExternalized1a() {
    	runConformTest(
    		new String[] {
    	"T1626loe1aMain.java",
    		"public class T1626loe1aMain {\n" +
    		"	public static void main(String... args) {\n" +
    		"		final Team1626loe1a t = new Team1626loe1a();\n" +
    		"		for (R<@t> r : t.roles)\n" +
    		"			System.out.print(r);\n" +
    		"	}\n" +
    		"}\n",
    	"Team1626loe1a.java",
    		"import java.util.ArrayList;\n" +
    		"import java.util.Iterator;\n" +
    		"public team class Team1626loe1a {\n" +
    		"	public class R {\n" +
    		"		String val;\n" +
    		"		public R(String v) { this.val=v;}\n" +
    		"		@Override public String toString() { return this.val; }\n" +
    		"	}\n" +
    		"	public R[] roles = \n" +
    		"		new R[] {\n" +
    		"			new R(\"Ra\"),\n" +
    		"			new R(\"Rb\")\n" +
    		"		};\n" +
    		"}\n"
    		},
    		"RaRb");
    }

    // an enhanced for loop iterates over an Iterable of externalized roles
    // involves implicit inheritance from binary and adding of a playedBy decl.
    public void test1626_loopingOverExternalized2() {
    	runConformTest(
        		new String[] {
    	"Team1626loe2_1.java",
	    		"import java.util.ArrayList;\n" +
	    		"import java.util.Iterator;\n" +
	    		"public team class Team1626loe2_1 {\n" +
	    		"	public class R1 implements Iterable<R0> {\n" +
	    		"		public Iterator<R0> iterator() {\n" +
	    		"			ArrayList<R0> list = new ArrayList<R0>();\n" +
	    		"			list.add(new R0(\"Ra\"));\n" +
	    		"			list.add(new R0(\"Rb\"));\n" +
	    		"			return list.iterator();\n" +
	    		"		}\n" +
	    		"	}\n" +
	    		"	public class R0 {\n" +
	    		"		String val;\n" +
	    		"		public R0(String v) { this.val=v;}\n" +
	    		"		@Override public String toString() { return this.val; }\n" +
	    		"	}\n" +
	    		"}\n"
        		});
    	runConformTest(
    		new String[] {
    	"T1626loe2Main.java",
	    		"public class T1626loe2Main {\n" +
	    		"	public static void main(String... args) {\n" +
	    		"		final Team1626loe2_2 t2 = new Team1626loe2_2();\n" +
	    		"		final Team1626loe2_3<@t2> t3 = new Team1626loe2_3<@t2>();\n" +
	    		"		R1<@t3> r1 = new R1<@t3>(new T1626loe2());\n" +
	    		"		for (R0<@t3> r2 : r1)\n" +
	    		"			System.out.print(r2);\n" +
	    		"	}\n" +
	    		"}\n",
    	"Team1626loe2_2.java",
    			"public team class Team1626loe2_2 {\n" +
    			"	public team class Team1626loe2_3 extends Team1626loe2_1 {\n" +
    			"		@Override\n" +
    			"		public class R0 {}\n" +
    			"		@Override\n" +
    			"		public class R1 playedBy T1626loe2 {}\n" +
    			"	}\n" +
    			"}\n",
    	"T1626loe2.java",
    			"public class T1626loe2 {} \n"
    		},
    		"RaRb",
    		null/*classLibraries*/,
    		false/*shouldFlushOutputDirectory*/,
    		null/*vmArguments*/);
    }

    // an enhanced for loop iterates over an Iterable of externalized roles
    // involves implicit inheritance from binary and adding of a playedBy decl.
    // using generic super class instead of superinterface
    public void test1626_loopingOverExternalized3() {
    	runConformTest(
        		new String[] {
    	"Team1626loe2_1.java",
	    		"import java.util.ArrayList;\n" +
	    		"import java.util.Iterator;\n" +
	    		"public team class Team1626loe2_1 {\n" +
	    		"	public class R1 extends MyIterable<R0> {\n" +
	    		"		public Iterator<R0> iterator() {\n" +
	    		"			ArrayList<R0> list = new ArrayList<R0>();\n" +
	    		"			list.add(new R0(\"Ra\"));\n" +
	    		"			list.add(new R0(\"Rb\"));\n" +
	    		"			return list.iterator();\n" +
	    		"		}\n" +
	    		"	}\n" +
	    		"	public class R0 {\n" +
	    		"		String val;\n" +
	    		"		public R0(String v) { this.val=v;}\n" +
	    		"		@Override public String toString() { return this.val; }\n" +
	    		"	}\n" +
	    		"}\n",
	    "MyIterable.java",
	    		"public abstract class MyIterable<E> implements Iterable<E>{}\n"
        		});
    	runConformTest(
    		new String[] {
    	"T1626loe2Main.java",
	    		"public class T1626loe2Main {\n" +
	    		"	public static void main(String... args) {\n" +
	    		"		final Team1626loe2_2 t2 = new Team1626loe2_2();\n" +
	    		"		final Team1626loe2_3<@t2> t3 = new Team1626loe2_3<@t2>();\n" +
	    		"		R1<@t3> r1 = new R1<@t3>(new T1626loe2());\n" +
	    		"		for (R0<@t3> r2 : r1)\n" +
	    		"			System.out.print(r2);\n" +
	    		"	}\n" +
	    		"}\n",
    	"Team1626loe2_2.java",
    			"public team class Team1626loe2_2 {\n" +
    			"	public team class Team1626loe2_3 extends Team1626loe2_1 {\n" +
    			"		@Override\n" +
    			"		public class R0 {}\n" +
    			"		@Override\n" +
    			"		public class R1 playedBy T1626loe2 {}\n" +
    			"	}\n" +
    			"}\n",
    	"T1626loe2.java",
    			"public class T1626loe2 {} \n"
    		},
    		"RaRb",
    		null/*classLibraries*/,
    		false/*shouldFlushOutputDirectory*/,
    		null/*vmArguments*/);
    }

}
