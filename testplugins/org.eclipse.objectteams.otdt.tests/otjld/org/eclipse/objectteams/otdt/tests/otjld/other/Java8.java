/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2012 Stephan Herrmann
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

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.core.ext.WeavingScheme;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

public class Java8 extends AbstractOTJLDTest {

	public Java8(String name) {
		super(name);
	}

// Static initializer to specify tests subset using TESTS_* static variables
// All tests which do not belong to the subset are skipped...
	static {
//		TESTS_NAMES = new String[] { "testA11_lambdaExpression02"};
//		TESTS_NUMBERS = new int[] { 1459 };
//		TESTS_RANGE = new int[] { 1097, -1 };
	}

	public static Test suite() {
		Test suite = buildMinimalComplianceTestSuite(Java8.class, F_1_8);
		TESTS_COUNTERS.put(Java8.class.getName(), new Integer(suite.countTestCases()));
		return suite;
	}

	public static Class<Java8> testClass() {
		return Java8.class;
	}
	
	@Override
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
		if (this.weavingScheme == WeavingScheme.OTRE)
			System.err.println("Running Java8 tests for OTRE weaver will skip most tests");
	}

	private void runNegativeTestNoFlush(String[] testFiles, Map<String, String> compilerOptions, String error) {
		runNegativeTest(false, testFiles, null, compilerOptions, error, DEFAULT_TEST_OPTIONS);
	}

	private void runConformTestNoFlush(String[] testFiles, Map<String, String> compilerOptions) {
		runConformTest(false, testFiles, null, compilerOptions, "", "", "", DEFAULT_TEST_OPTIONS);
	}

	// A lambda expression appears in a parameter mapping
	// - empty param list
	public void testA11_lambdaExpression01() {
		if (this.weavingScheme == WeavingScheme.OTRE) return;
		runConformTest(
			new String[] {
		"p1/TeamA11le01.java",
			"package p1;\n" +
			"public team class TeamA11le01 {\n" +
			"    protected class Role playedBy TA11le01 {\n" +
			"        protected String v = \"OK\";\n" +
			"        void outside() -> void run(Runnable r) with {\n" +
			"            () -> System.out.println(v) -> r\n" +
			"        }\n" +
			"    }\n" +
			"    void test(TA11le01 as Role r) {\n" +
			"        r.outside();\n" +
			"    } \n" +
			"    public static void main(String[] args) throws Exception {\n" +
			"        new TeamA11le01().test(new TA11le01());\n" +
			"    }\n" +
			"}",
	"p1/TA11le01.java",
			"package p1;\n" +
			"public class TA11le01 {\n" +
			"    public void run(Runnable r) {\n" +
			"      try {\n" +
			"        r.run();\n" +
			"      } catch (NullPointerException npe) {System.out.print(\"caught\"); }\n" +
			"    }\n" +
			"}",
			},
			"OK"); // FIXME real execution of lambda
	}

	// A lambda expression appears in a parameter mapping
	// - single ident param
	// - lambda is 2nd mapping
	public void testA11_lambdaExpression02() {
		if (this.weavingScheme == WeavingScheme.OTRE) return;
		runConformTest(
			new String[] {
		"p1/TeamA11le02.java",
			"package p1;\n" +
			"public team class TeamA11le02 {\n" +
			"    protected class Role playedBy TA11le02 {\n" +
			"        protected String v;\n" +
			"        void outside() -> void run(String s, RunWithArg<String> r) with {\n" +
			"            \"prefix.\" -> s,\n" +
			"            x -> System.out.println(Role.this.v) -> r\n" +
			"        }\n" +
			"    }\n" +
			"    void test(TA11le02 as Role r) {\n" +
			"        r.v = \"OK\";" +
			"        r.outside();\n" +
			"    } \n" +
			"    public static void main(String[] args) throws Exception {\n" +
			"        new TeamA11le02().test(new TA11le02());\n" +
			"    }\n" +
			"}",
	"p1/RunWithArg.java",
			"package p1;\n" +
			"public interface RunWithArg<T> {\n" +
			"	void run(T a);\n" +
			"}\n",
	"p1/TA11le02.java",
			"package p1;\n" +
			"public class TA11le02 {\n" +
			"    public void run(String s, RunWithArg<String> r) {\n" +
			"        System.out.print(s);\n" +
			"      try {\n" +
			"        r.run(s);\n" +
			"      } catch (NullPointerException npe) {System.out.print(\"caught\"); }\n" +
			"    }\n" +
			"}",
			},
			"prefix.OK");
	}

	// A lambda expression appears in a parameter mapping
	// - two type elided params
	public void testA11_lambdaExpression03() {
		if (this.weavingScheme == WeavingScheme.OTRE) return;
		runConformTest(
			new String[] {
		"p1/TeamA11le03.java",
			"package p1;\n" +
			"public team class TeamA11le03 {\n" +
			"    protected class Role playedBy TA11le03 {\n" +
			"        void outside() -> void run(IA11le03 r) with {\n" +
			"            (x,y) -> System.out.println(\"\"+x+y) -> r\n" +
			"        }\n" +
			"    }\n" +
			"    void test(TA11le03 as Role r) {\n" +
			"        r.outside();\n" +
			"    } \n" +
			"    public static void main(String[] args) throws Exception {\n" +
			"        new TeamA11le03().test(new TA11le03());\n" +
			"    }\n" +
			"}",
		"p1/IA11le03.java",
			"package p1;\n" +
			"public interface IA11le03 {\n" +
			"    void invoke(String s1, String s2);\n" +
			"}",
		"p1/TA11le03.java",
			"package p1;\n" +
			"public class TA11le03 {\n" +
			"    public void run(IA11le03 r) {\n" +
			"      try {\n" +
			"        r.invoke(\"O\", \"K\");\n" +
			"      } catch (NullPointerException npe) {System.out.print(\"caught\"); }\n" +
			"    }\n" +
			"}",
			},
			"OK");
	}

	// A lambda expression appears in a parameter mapping
	// - two typed params
	public void testA11_lambdaExpression04() {
		if (this.weavingScheme == WeavingScheme.OTRE) return;
		runConformTest(
			new String[] {
		"p1/TeamA11le04.java",
			"package p1;\n" +
			"public team class TeamA11le04 {\n" +
			"    protected class Role playedBy TA11le04 {\n" +
			"        void outside() -> void run(IA11le04 r) with {\n" +
			"            (String x, String y) -> System.out.println(\"\"+x+y) -> r\n" +
			"        }\n" +
			"    }\n" +
			"    void test(TA11le04 as Role r) {\n" +
			"        r.outside();\n" +
			"    } \n" +
			"    public static void main(String[] args) throws Exception {\n" +
			"        new TeamA11le04().test(new TA11le04());\n" +
			"    }\n" +
			"}",
		"p1/IA11le04.java",
			"package p1;\n" +
			"public interface IA11le04 {\n" +
			"    void invoke(String s1, String s2);\n" +
			"}",
		"p1/TA11le04.java",
			"package p1;\n" +
			"public class TA11le04 {\n" +
			"    public void run(IA11le04 r) {\n" +
			"      try {\n" +
			"        r.invoke(\"O\", \"K\");\n" +
			"      } catch (NullPointerException npe) {System.out.print(\"caught\"); }\n" +
			"    }\n" +
			"}",
			},
			"OK");
	}
	
	public void testTypeAnnotationAndTypeAnchor_1() {
		if (this.weavingScheme == WeavingScheme.OTRE) return;
		runConformTest(
			new String[] {
		"Marker.java",
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" + 
			"@Target({ ElementType.TYPE_USE })\n" + 
			"public @interface Marker {}\n",
		"T1.java,",
			"public team class T1 {\n" +
			"	public class R {}\n" +
			"}\n",
		"C1.java",
			"public abstract class C1 {\n" +
			"	abstract void test1(final T1 o, R<@o> c);\n" +
			"	abstract void test2(final Object o, C2<@Marker Object> c);\n" +
			"}\n" +
			"class C2<T> {}\n"
			});
	}
	
	public void testTypeAnnotationAndTypeAnchor_2() {
		if (this.weavingScheme == WeavingScheme.OTRE) return;
		runConformTest(
			new String[] {
		"p1/Marker.java",
				"package p1;\n" +
				"import java.lang.annotation.*;\n" +
				"@Retention(RetentionPolicy.CLASS)\n" + 
				"@Target({ ElementType.TYPE_USE })\n" + 
				"public @interface Marker {}\n",
		"p1/Marker2.java",
				"package p1;\n" +
				"import java.lang.annotation.*;\n" +
				"@Retention(RetentionPolicy.CLASS)\n" + 
				"@Target({ ElementType.TYPE_USE })\n" + 
				"public @interface Marker2 {}\n",
		"T1.java,",
			"public team class T1 {\n" +
			"	public class R {}\n" +
			"}\n",
		"C1.java",
			"public abstract team class C1 {\n" +
			"	final T1 t = new T1();\n" +
			"	abstract void test1(final C1 a, R<@a.t> c);\n" +
			"	abstract void test2(final Object o, C2<java.lang.@p1.Marker Object> c);\n" + // bug, should be java.lang. @p1.Marker Object
			"	abstract void test3(final Object o, C2<@p1.Marker @p1.Marker2 Object> c);\n" + // bug, should be java.lang. @p1.Marker Object
			"}\n" +
			"class C2<T> {}\n"
			});
	}

	public void testOtreWarning() {
		runConformTest(
			new String[] {
				"B.java",
				"public class B {}\n"
			});
		Map<String, String> compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportOtreWeavingIntoJava8, CompilerOptions.ERROR);
		String[] teamSource = new String[] { "MyTeam.java",
				"public team class MyTeam {\n" +
				"	protected class R playedBy B {}\n" +
				"}\n" };
		if (this.weavingScheme == WeavingScheme.OTRE)
			runNegativeTestNoFlush(
				teamSource,
				compilerOptions,
				"----------\n" + 
				"1. WARNING in MyTeam.java (at line 2)\n" + 
				"	protected class R playedBy B {}\n" + 
				"	                           ^\n" + 
				"Base class B has class file version 52 which cannot be handled by the traditional OTRE based on BCEL. Please consider using the ASM based OTDRE instead.\n" + 
				"----------\n");
		else
			runConformTestNoFlush(teamSource, compilerOptions);
	}


    public void testImplicitInheritanceWithDefaultMethods1() {
		if (this.weavingScheme == WeavingScheme.OTRE) return;
    	runConformTest(
    		new String[]{
    			"SubTeam.java",
    			"public team class SubTeam extends SuperTeam {\n" +
    			"	public interface I2  {\n" + 
    			"		default void bar() { System.out.println(\"subbar\"); }\n" + 
    			"	}\n" + 
    			"	protected class R {\n" + 
    			"		protected void foo() {\n" + 
    			"			System.out.println(\"foosub\");\n" + 
    			"		}\n" + 
    			"	}\n" + 
    			"	public static void main(String[] args) {\n" + 
    			"		new SubTeam().test();\n" + 
    			"	}\n" + 
    			"}\n",
    			"SuperTeam.java",
    			"public team class SuperTeam {\n" +
    			"	public interface I2 {\n" + 
    			"		default void bar() { System.out.println(\"bar\"); }\n" + 
    			"	}\n" + 
    			"	protected class R implements I2 {\n" + 
    			"		protected void foo() {\n" + 
    			"			System.out.println(\"foo\");\n" + 
    			"		}\n" + 
    			"	}\n" + 
    			"	protected void test() {\n" + 
    			"		R r = new R();\n" + 
    			"		r.foo();\n" + 
    			"		r.bar();\n" + 
    			"	}\n" + 
    			"}\n"
    		},
    		"foosub\n" +
    		"subbar");
    }

    // Bug475635
    public void testBaseCallsInLambdas() {
    	runConformTest(
    		new String[] {
	    		"b/Base.java",
	    		"package b;\n" +
	    		"public class Base {\n" +
	    		"	public void bm() { System.out.print(\"b\"); }\n" +
	    		"	public static void main(String... args) {\n" +
	    		"		new t.MyT().activate();\n" +
	    		"		new Base().bm();\n" +
	    		"	}\n" + 
	    		"}\n",
	    		"t/MyT.java",
	    		"package t;\n" +
	    		"import base b.Base;\n" + 
	    		"\n" + 
	    		"public team class MyT {\n" + 
	    		"\n" + 
	    		"	protected class R playedBy Base {\n" + 
	    		"		void perform(Runnable r) { r.run(); }\n" +
	    		"		@SuppressWarnings(\"basecall\")\n" + 
	    		"		callin void test() {\n" + 
	    		"			perform(() -> {\n" + 
	    		"				System.out.print(0);\n" + 
	    		"				base.test();\n" +
	    		"				System.out.print(1);\n" + 
	    		"				perform(() -> base.test());\n" + 
	    		"			});\n" + 
	    		"			new Runnable() {\n" + 
	    		"				@Override\n" + 
	    		"				public void run() {\n" + 
	    		"					perform(() -> {\n" +
	    		"						System.out.print(2);\n" + 
	    		"						base.test(); \n" + 
	    		"					});\n" +
	    		"					System.out.print(3);\n" + 
	    		"					base.test();\n" + 
	    		"					System.out.println(4);\n" + 
	    		"				}\n" + 
	    		"			}.run();\n" + 
	    		"		}\n" + 
	    		"		test <- replace bm;\n" + 
	    		"	}\n" +
	    		"}\n"
    		},
    		"0b1b2b3b4");
    }
    
    public void testBug506747() {
    	Map<String,String> options = getCompilerOptions();
    	options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED); // enable AnnotatableTypeSystem
    	runNegativeTest(
    		new String[] {
    			"p/X.java",
    			"package p;\n" +
    			"import java.lang.annotation.*;\n" +
    			"@Target(ElementType.TYPE_USE)\n" +
    			"@interface Ann {}\n" +
    			"public class X {\n" +
    			"	void m(final @Ann X X) {\n" +
    			"		X.Inner i;\n" +
    			"	}\n" +
    			"}\n"
    		},
    		"----------\n" + 
			"1. ERROR in p\\X.java (at line 7)\n" + 
			"	X.Inner i;\n" + 
			"	^^^^^^^\n" + 
			"X.Inner cannot be resolved to a type\n" + 
			"----------\n",
			null, // libs
			true, // flush
    		options);
    }

    public void testBug506749a() {
    	runNegativeTest(
    		new String[] {
    			"Bug506749.java",
    			"public team class Bug506749 {\n" +
    			"	protected class R {\n" +
    			"		void test() {\n" +
    			"			Runnable r = () -> base();\n" +
    			"		}\n" +
    			"	}\n" +
    			"}\n"
    		},
    		"----------\n" + 
			"1. ERROR in Bug506749.java (at line 4)\n" + 
			"	Runnable r = () -> base();\n" + 
			"	                   ^^^^\n" + 
			"Illegal base constructor call: enclosing '() -> <no expression yet>' is not a constructor of a bound role (OTJLD 2.4.2).\n" + 
			"----------\n");
    }

    public void testBug506749b() {
    	runNegativeTest(
    		new String[] {
    			"Bug506749.java",
    			"public team class Bug506749 {\n" +
    			"	protected class R {\n" +
    			"		void test() {\n" +
    			"			Runnable r = () -> { base(); };\n" +
    			"		}\n" +
    			"	}\n" +
    			"}\n"
    		},
    		"----------\n" + 
			"1. ERROR in Bug506749.java (at line 4)\n" + 
			"	Runnable r = () -> { base(); };\n" + 
			"	                     ^^^^\n" + 
			"Illegal base constructor call: enclosing \'() -> {\n" + 
			"  <no expression yet>;\n" + 
			"}\' is not a constructor of a bound role (OTJLD 2.4.2).\n" + 
			"----------\n");
    }
}
