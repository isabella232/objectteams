/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
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
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.test.builder;

import junit.framework.Test;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.util.Util;

import static org.eclipse.objectteams.otdt.tests.ClasspathUtil.OTRE_PATH;
/**
 * This test class tests incremental compilation of teams and roles.
 */
@SuppressWarnings("nls")  
public class IncrementalTests extends OTBuilderTests {
    	
    
	public IncrementalTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(IncrementalTests.class);
	}
	
	/*
	 */
	public void testRemoveTeamType() throws JavaModelException {
		System.out.println("***** testRemoveTeamType() *****");
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+ 
			"public team class AA {	}");
			

		IPath pathToAB = env.addClass(root, "p.AA", "R", 
			"team package p.AA;	\n"+ 
			"   protected class R {}");


		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove team AA */
		env.removeClass(env.getPackagePath(root, "p"), "AA");

		/* build must detect, that R - although unchanged - becomes invalid. */
		incrementalBuild(projectPath);
		expectingProblemsFor(pathToAB);
		expectingSpecificProblemFor(pathToAB, new Problem("", "Enclosing team p.AA not found for role file R (OTJLD 1.2.5(c)).", pathToAB, 13, 17, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); 
		
		/* Restore team AA */
		env.addClass(root, "p", "AA",
				"package p;	\n"+ 
				"public team class AA {}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	/*
	 */
	public void testRemoveRoleType() throws JavaModelException {
		System.out.println("***** testRemoveRoleType() *****");
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {	\n"+
			"   protected class RA {}}");

		IPath pathToAB = env.addClass(root, "p", "AB",
			"package p;	\n"+
			"public team class AB extends AA {"+
			"   protected class RB extends RA {} }");


		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove role AA.RA */
		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {}");

		/* build must detect, that AB - although unchanged - becomes invalid. */
		incrementalBuild(projectPath);
		expectingProblemsFor(pathToAB);
		expectingSpecificProblemFor(pathToAB, new Problem("", "RA cannot be resolved to a type", pathToAB, 75, 77, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); 
		
		/* Restore role AA.RA */
		env.addClass(root, "p", "AA",
				"package p;	\n"+
				"public team class AA {	\n"+
				"   public class RA {}}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	public void testRemoveRoleFile() throws JavaModelException {
		System.out.println("***** testRemoveRoleFile() *****");
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "Outer",
			"package p;	\n"+
			"public team class Outer {}");
		IPath pathToNested= env.addClass(root, "p/Outer", "Nested",
			"team package p.Outer;\n"+
			"protected team class Nested {}");

		IPath pathToOuterSub = env.addClass(root, "p", "OuterSub",
			"package p;	\n"+
			"public team class OuterSub extends Outer {\n"+
			"   Nested aRole;\n"+
			"}");

		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove role Outer.Nested */
		env.removeFile(pathToNested);

		/* build must detect, that AB - although unchanged - becomes invalid -- but no further errors/exceptions */
		incrementalBuild(projectPath);
		expectingProblemsFor(pathToOuterSub);
		expectingSpecificProblemFor(pathToOuterSub, new Problem("", "Nested cannot be resolved to a type", pathToOuterSub, 58, 64, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); 
		
	}
	/*
	 */
	public void testRemoveRoleMethod() throws JavaModelException {
		System.out.println("***** testRemoveRoleMethod() *****");
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {	\n"+
			"   public class RA { \n"+
			"       public void foo() {}\n"+
			"}}");

		env.addClass(root, "p", "AB",
			"package p;	\n"+
			"public team class AB extends AA {"+
			"   protected class RB extends RA {} }");


		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove role method AA.RA.foo */
		env.addClass(root, "p", "AA",
				"package p;	\n"+
				"public team class AA {	\n"+
				"   public class RA {}}");

		/* add a class referencing AB.RA */
		IPath pathToM = env.addClass(root, "p", "M",
				"package p;	\n"+
				"public class M {	\n"+
				"   void test() {\n"+
				"   new AB().new RA().foo();\n"+
				"}}");

		/* build must detect, that the use of AB - although AB is unchanged - becomes invalid. */
		incrementalBuild(projectPath);
		expectingProblemsFor(pathToM);
		expectingSpecificProblemFor(pathToM, new Problem("", "The method foo() is undefined for the type RA<@tthis[AB]>", pathToM, 68, 71, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); 

		/* Restore method AA.RA.foo */
		env.addClass(root, "p", "AA",
				"package p;	\n"+ 
				"public team class AA {	\n"+
				"   public class RA { \n"+
				"       public void foo() {}\n"+
				"}}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	
	/*
	 */
	public void testModifyTSuperRole1() throws JavaModelException {
		System.out.println("***** testModifyTSuperRole1() *****");
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); 

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); 
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {	\n"+
			"   protected class R {\n" +
			"       void bar() { }\n"+
			"}}");

		IPath pathToAB = env.addClass(root, "p", "AB",
			"package p;	\n"+
			"public team class AB extends AA {\n"+
			"   protected class R {\n"+
			"       void foo() { this.bar(); }\n"+
			"} }");


		fullBuild(projectPath);
		expectingNoProblems();

		/* Remove method AA.R.bar() */
		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {\n" +
			"   protected class R {}\n" +
			"}");

		/* build must detect, that AB - although unchanged - becomes invalid through a self-call. */
		incrementalBuild(projectPath);
		expectingProblemsFor(pathToAB);
		expectingSpecificProblemFor(pathToAB, new Problem("", "The method bar() is undefined for the type AB.R", pathToAB, 94, 97, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); 

		/* Restore method AA.R.bar */
		env.addClass(root, "p", "AA",
				"package p;	\n"+
				"public team class AA {	\n"+
				"   protected class R {\n" +
				"       void bar() { }\n"+
				"}}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	
	/*
	 * The body of a tsuper role is modified.
	 */
	public void testModifyTSuperRole2() throws Exception {
		System.out.println("***** testModifyTSuperRole2() *****");
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); 

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); 
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA", 
			"package p;	\n"+ 
			"public team class AA {	\n"+ 
			"   protected class R {\n" +
			"       String bar() { return \"NOK\"; }\n"+
			"}}");

		env.addClass(root, "p", "AB",
			"package p;	\n"+ 
			"public team class AB extends AA {\n"+
			"   protected class R {\n"+
			"       protected String foo() { return this.bar(); }\n"+ 
			"   }\n"+ 
			"   public static String test() {\n"+ 
			"      return new AB().new R().foo();\n"+
			"   }\n"+
			"}");


		fullBuild(projectPath);
		expectingNoProblems();

		/* Change method R.bar() */
		env.addClass(root, "p", "AA",
			"package p;	\n"+ 
			"public team class AA {\n" + 
			"   protected class R {\n" + 
			"       String bar() { return \"OK\"; }\n"+
			"}}");

		/* build must achieve that AB - although unchanged - behaves differently. */
		incrementalBuild(projectPath);
		expectingNoProblems();
		
		IJavaProject project = JavaCore.create(env.getProject("Project"));
		OTLaunchEnvironment launcher = new OTLaunchEnvironment(
				env.getWorkspaceRootPath(),
				project.getOutputLocation());
		Object result = launcher.launch("p.AB", "test");
		assertEquals("OK", result);
		// cleanup:
		launcher = null;
		result = null;
		System.gc();
	}
	
	/*
	 * The body of a tsuper role is modified -- three levels.
	 */
	public void testModifyTSuperRole3() throws Exception {
		System.out.println("***** testModifyTSuperRole3() *****");
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide 
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {	\n"+
			"   protected class R {\n" +
			"       String bar() { \n" +
			"          return \"NOK\";\n"+
			"       }\n"+
			"}}");

		env.addClass(root, "p", "AB", 
				"package p;	\n"+
				"public team class AB extends AA {}");
		
		env.addClass(root, "p", "AC",
			"package p;	\n"+
			"public team class AC extends AB {\n"+
			"   protected class R {\n"+
			"       protected String foo() { return this.bar(); }\n"+
			"   }\n"+
			"   public static String test() {\n"+
			"      return new AC().new R().foo();\n"+
			"   }\n"+
			"}");


		fullBuild(projectPath);
		expectingNoProblems();

		/* Change method R.bar() */
		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {\n" +
			"   protected class R {\n" +
			"       String bar() { return \"OK\"; }\n"+
			"}}");

		/* build must achieve that AB - although unchanged - behaves differently. */
		incrementalBuild(projectPath);
		expectingNoProblems();
		
		IJavaProject project = JavaCore.create(env.getProject("Project"));
		OTLaunchEnvironment launcher = new OTLaunchEnvironment(
				env.getWorkspaceRootPath(),
				project.getOutputLocation());
		Object result = launcher.launch("p.AC", "test");
		assertEquals("OK", result);
// cleanup:
		launcher = null;
		result = null;
		System.gc();
	}

	/** A playedBy declaration is added to a super role file.
	 *  Witness for NPE in RoleModel.getBaseclassAttributename. 
	 */
	public void testModifySuperRole1() throws JavaModelException {
		System.out.println("***** testModifySuperRole1() *****");
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); 

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); 
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {	\n"+
			"}");

		env.addClass(root, "p/AA", "R1",
			"team package p.AA;\n"+
			"protected class R1 {\n"+
			"       void foo() { }\n"+
			"}");

		env.addClass(root, "p/AA", "R2",
			"team package p.AA;\n"+
			"protected class R2 extends R1 {\n"+
			"       void bar() { this.foo(); }\n"+
			"}");

		env.addClass(root, "p", "B",
			"package p;\n"+
			"public class B {\n"+
			"       void snafu() {}\n"+
			"}");


		fullBuild(projectPath);
		expectingNoProblems();

		/* add a playedBy declaration */
		env.addClass(root, "p/AA", "R1",
			"team package p.AA;\n"+
			"protected class R1 playedBy B {\n"+
			"       void foo() { }\n"+
			"}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	
	/** 
	 * A sibling role is modified causing a binary role to fail generating callins. 
	 */
	public void testModifySiblingRole1() throws JavaModelException {
		System.out.println("***** testModifySiblingRole1() *****");
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); 

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); 
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "B",
			"package p;\n"+
			"public class B {\n"+
			"       void snafu() {}\n"+
		"}");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {	\n"+
			"}");

		env.addClass(root, "p/AA", "R1",
			"team package p.AA;\n"+
			"protected class R1 playedBy B {\n"+
			"       protected void foo() { }\n"+
			"       foo <- after snafu;\n"+
			"}");

		env.addClass(root, "p/AA", "R2",
			"team package p.AA;\n"+
			"protected class R2 {\n"+
			"       void bar() { new R1(new B()).foo(); }\n"+
			"}");



		fullBuild(projectPath);
		expectingNoProblems();

		/* change the method */
		env.addClass(root, "p/AA", "R2",
			"team package p.AA;\n"+
			"protected class R2 {\n"+
			"       void bar() {  }\n"+
			"}");

		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	
	/*
	 * The team with a role file is modified
	 */
	public void testModifyTeam1() throws Exception {
		System.out.println("***** testModifyTeam1() *****");
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); 

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "B", 
				"package p;	\n"+   
				"public class B {	\n"+  
				"    String bar() { return \"NOK\"; }\n"+
				"}");

		env.addClass(root, "p", "AA", 
			"package p;	\n"+   
			"public team class AA {	\n"+  
			"   public static String test() {\n"+
			"        new AA();\n"+
			"        return new B().bar();\n"+
			"   }\n"+
			"}");

		env.addClass(root, "p.AA", "R",
			"team package p.AA;	\n"+
			"protected class R playedBy B {\n"+
			"   @SuppressWarnings(\"basecall\")\n"+
			"   callin String foo() { return \"OK\"; }\n"+
			"   foo <- replace bar;\n"+  
			"}"); 


		fullBuild(projectPath);
		expectingNoProblems();

		/* add constructor */

		env.addClass(root, "p", "AA", 
			"package p;	\n"+   
			"public team class AA {	\n"+  
			"   public AA() { this.activate(); }\n"+
			"   public static String test() {\n"+
			"        new AA();\n"+
			"        return new B().bar();\n"+
			"   }\n"+
			"}");

		/* build must achieve that R - although unchanged - is compiled into AA. */
		incrementalBuild(projectPath);
		expectingNoProblems();
		
		IJavaProject project = JavaCore.create(env.getProject("Project"));
		OTLaunchEnvironment launcher = new OTLaunchEnvironment(
				env.getWorkspaceRootPath(),
				project.getOutputLocation(),
				true /* useTransformer */);
		Object result = launcher.launch("p.AA", "test");
		assertEquals("OK", result);
		// cleanup:
		launcher = null;
		result = null;
		System.gc();
	}
	
	public void testAddRoFiToCompiledTeam()
		throws JavaModelException
	{
		System.out.println("***** testAddRoFiToCompiledTeam() *****");
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide 
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {	\n"+
			"   protected class R {\n" +
			"       String bar() { \n" +
			"          return \"NOK\";\n"+
			"       }\n"+
			"}}");
		
		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p/AA", "ExtRole",
				"team package p.AA;	\n"+
				"public class ExtRole {	\n"+
				"    public String ok() { \n" +
				"       return \"NOK\";\n"+
				"    }\n"+
				"}");
		
		/* build must achieve that AA - although unchanged - is recompiled. */
		incrementalBuild(projectPath);
		expectingNoProblems();

		// same as above, just modify the contents:
		IPath packagePath = root.append("p/AA");
		env.addClass(packagePath, "ExtRole",
				"team package p.AA;	\n"+
				"public class ExtRole {	\n"+
				"    public String ok() { \n" +
				"       return \"OK\";\n"+
				"    }\n"+
				"}");
		
		/* build must(?) achieve that AA - although unchanged - is recompiled. */
		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	
	public void testAddRoFiToBrokenTeam()
		throws JavaModelException
	{
		System.out.println("***** testAddRoFiToBrokenTeam() *****");
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);
	
		// remove old package fragment root so that names don't collide 
		env.removePackageFragmentRoot(projectPath, "");
	
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
	
		// a broken team:
		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {	\n"+
			"   int abstract foo;// syntax error\n"+
			"}");
		
		fullBuild(projectPath);
		expectingProblemsFor(root);
	
		// add a role file:
		env.addClass(root, "p/AA", "ExtRole",
				"team package p.AA;	\n"+
				"public class ExtRole {	\n"+
				"    public String ok() { \n" +
				"       return \"NOK\";\n"+
				"    }\n"+
				"}");
		
		/* build must achieve that errors in AA are detected again. */
		incrementalBuild(projectPath);
		expectingProblemsFor(root);
		
		/* also break role file: */
		IPath rofi = env.addClass(root, "p/AA", "ExtRole",
				"team package p.AA;	\n"+
				"public class ExtRole {	\n"+
				"    public String ok { // error \n" +
				"       return \"NOK\";\n"+
				"    }\n"+
				"}");

		/* now everything is broken. */
		incrementalBuild(projectPath);
		expectingProblemsFor(root);
	
		// correct the syntax error:
		env.addClass(root, "p", "AA",
				"package p;	\n"+
				"public team class AA {	\n"+
				"   int foo() { return 0; }\n"+
				"}");

		/* build must detect that ExtRole still has an error. */
		incrementalBuild(projectPath);
		expectingProblemsFor(rofi);
	}
	
	public void testBreakTeamWithRoFi()
		throws JavaModelException
	{
		System.out.println("***** testBreakTeamWithRoFi() *****");
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);
	
		// remove old package fragment root so that names don't collide 
		env.removePackageFragmentRoot(projectPath, "");
	
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");
	
		// team and role w/o errors:
		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {	\n"+
			"    public void nop() { } \n" +
			"}");

		env.addClass(root, "p/AA", "ExtRole",
				"team package p.AA;	\n"+
				"public class ExtRole {	\n"+
				"    public String ok() { \n" +
				"       return \"OK\";\n"+
				"    }\n"+
				"}");
		
		fullBuild(projectPath);
		expectingNoProblems();
	
		// introduce an error to the team:
		env.addClass(root, "p", "AA",
				"package p;	\n"+
				"public team class AA {	\n"+
				"   int abstract nop();\n"+
				"}");

		/* build must achieve that AA - although unchanged - is recompiled. */
		incrementalBuild(projectPath);
		expectingProblemsFor(root);
	
	}

	public void testRoFiExtendsInline()
		throws JavaModelException
	{
		System.out.println("***** testRoFiExtendsInline() *****");
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide 
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "AA",
			"package p;	\n"+
			"public team class AA {	\n"+
			"   protected team class R {\n" +
			"       String bar() { \n" +
			"          return \"NOK\";\n"+
			"       }\n"+
			"}}");
		// Note: declaring R as a nested team requires to process the predifined roles,
		//       which caused a StackOverflowError previously.
		
		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p/AA", "ExtRole",
				"team package p.AA;	\n"+
				"public team class ExtRole extends R {\n"+
				"    public String ok() { \n" +
				"       return bar();\n"+
				"    }\n"+
				"}");
		
		// still OK.
		incrementalBuild(projectPath);
		expectingNoProblems();

		// same as above, just modify the contents:
		env.addClass(root, "p", "AA",
				"package p;	\n"+
				"public team class AA {	\n"+
				"   protected team class R {\n" +
				"       String bar() { \n" +
				"          return \"OK\";\n"+
				"       }\n"+
				"}}");
		
		// no reason why this should find problems.
		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	// see http://trac.objectteams.org/ot/ticket/89
	public void testRemoveBoundBaseMethod() 	
			throws JavaModelException
	{
		System.out.println("***** testRemoveBoundBaseMethod() *****");
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide 
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p0", "Base",
			"package p0;	\n"+
			"public class Base {	\n"+
			"   public void foo() {}\n"+
			"}");
		IPath pathToT1= env.addClass(root, "p", "T1",
				"package p;	\n"+
				"import base p0.Base;\n"+
				"public team class T1 {	\n"+
				"   protected class R playedBy Base {\n" +
				"       void bar() { } \n" +
				"       bar <- after foo;\n"+
				"   }\n"+
				"}");
		
		fullBuild(projectPath);
		expectingNoProblems();

		// remove foo():
		env.addClass(root, "p0", "Base",
				"package p0;	\n"+
				"public class Base {	\n"+
				"}");

		env.addClass(root, "p", "T2",
				"package p;	\n"+
				"public team class T2 extends T1 {}");
		
		// must detect, that callin binding in T1 is now invalid
		incrementalBuild(projectPath);
		expectingProblemsFor(pathToT1);
		expectingSpecificProblemFor(pathToT1, new Problem("", "No method foo found in type p0.Base to resolve method designator (OTJLD 4.1(c)).", pathToT1, 137, 140, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); 

		// restore:
		env.addClass(root, "p0", "Base",
				"package p0;	\n"+
				"public class Base {	\n"+
				"   public void foo() {}\n"+
				"}");
		fullBuild(projectPath);
		expectingNoProblems();
	}
	// This scenario was observed to throw a CCE, see https://bugs.eclipse.org/311885
	public void testPhantomRole() 	
			throws JavaModelException
	{
		System.out.println("***** testPhantomRole() *****");
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, OTRE_PATH);

		// remove old package fragment root so that names don't collide 
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "TeamBug311885_1",
				"package p;\n" +
				"public team class TeamBug311885_1 {\n" +
    			"	public team class T11 {\n" +
    			"		protected class R1 {\n" +
    			"			void m() {}\n" +
    			"		}\n" +
    			"		protected class R3 extends R1 {\n" +
    			"		}\n" +
    			"	}\n" +
    			"	public team class T12 extends T11 {\n" +
    			"	}\n" +
    			"}\n");
		env.addClass(root, "p", "TeamBug311885_2",
				"package p;	\n"+
				"public team class TeamBug311885_2 extends TeamBug311885_1 {\n" +
    			"	public team class T12 {\n" +
    			"		protected class R3 {\n" +
    			"           void m() {}\n" + 
    			" 		}\n" +
    			" 	}\n" +
    			"}\n");
		
		fullBuild(projectPath);
		expectingNoProblems();

		// change whitespace only:
		env.addClass(root, "p", "TeamBug311885_2",
				"package p;	\n"+
				"public team class TeamBug311885_2 extends TeamBug311885_1 {\n" +
    			"	public team class T12 {\n" +
    			"		protected class R3 {\n" +
    			"           void m() {	}\n" + 
    			" 		}\n" +
    			" 	}\n" +
    			"}\n");

		// must not throw CCE
		incrementalBuild(projectPath);
		expectingNoProblems();
	}

}
