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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.objectteams.otdt.tests.ClasspathUtil;


public class CompilationOrderTests extends OTBuilderTests {

	public static Test suite() {
		return buildTestSuite(CompilationOrderTests.class);
	}

	public CompilationOrderTests(String name) {
		super(name);
	}
	
	/* In batch-mode this causes a problem, 
	 * compilation order is different in workbench mode, though
     * (depends on class names?!).
	 * Original jacks test is removed.
	 */
	@SuppressWarnings("nls")
	public void test177otjd5f()  throws JavaModelException {
		System.out.println("***** test177otjd5f() *****");
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addExternalJar(projectPath, ClasspathUtil.OTRE_PATH);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "ATeam",
				"package p;	\n"+ 
		"public team class ATeam {	protected class R implements IConfined {} public IConfined getR() { return new R(); } }");

		env.addClass(root, "p", "M", 
			"package p;	\n"+ 
			"public class M {\n"+
			"   @SuppressWarnings(\"unused\")\n"+
			"   void foo() {\n"+
			"       final ATeam t = new ATeam();\n"+
			"       IConfined<@t> ic = t.getR();\n"+
			"   }\n"+
			"}");

		fullBuild(projectPath);
		expectingNoProblems();
	}

	/* (TODO(SH): test doesn't really fit into this class).
	 * Witness for an NPE in ExplicitConstructorCall.resolve():
	 * If o.o.Team cannot be found, receiverType could be null!
	 */
	@SuppressWarnings("nls")
	public void testMissingOTRE() throws JavaModelException {
		System.out.println("***** testMissingOTRE() *****");
		IPath projectPath = env.addProject("Project", "1.5"); 
		// don't abort when detecting the build path error
		// (otherwise other errors would be expunged).
		env.getJavaProject(projectPath).setOption(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.ERROR);
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		// don't: env.addExternalJar(projectPath, OTRE_JAR_PATH);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath ateam = env.addClass(root, "p", "ATeam",
				"package p;	\n"+ 
		"public team class ATeam {	ATeam() { super(); } }");

		fullBuild(projectPath);
		
		expectingProblemsFor(ateam);
		expectingOnlySpecificProblemsFor(ateam, 
				new Problem[] {
					new Problem("", "The type org.objectteams.Team cannot be resolved. It is indirectly referenced from required .class files", ateam, 30, 35, CategorizedProblem.CAT_BUILDPATH, IMarker.SEVERITY_ERROR),
					new Problem("", "The constructor Team() is undefined", ateam, 48,56, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)
				});
	}
}
