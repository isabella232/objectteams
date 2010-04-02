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
 * $Id: OTReconcilerTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.tests.model.ReconcilerTests;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;

/**
 * Tests for errors shown only in the gutter (CompilationUnitProblemFinder),
 * but not reported by the ImageBuilder.
 * 
 * @author stephan
 * @since 1.2.1
 */
public class OTReconcilerTests extends ReconcilerTests {

	public static Test suite() {
		return buildModelTestSuite(OTReconcilerTests.class);
	}
	
	public OTReconcilerTests(String name) {
		super(name);
	}
	
	static {
//		TESTS_NAMES = new String[] { "testAnchoredType01", "testAnchoredType02", "testAnchoredType03" };
	}
// ===== Copied all our modifications from AbstractJavaModelTests ===== 
	/*
	 * Returns the OS path to the external directory that contains external jar files.
	 * This path ends with a File.separatorChar.
	 */
	protected String getExternalPath() {
		if (EXTERNAL_JAR_DIR_PATH == null)
			try {
				String path = getWorkspaceRoot().getLocation().toFile().getParentFile().getCanonicalPath();
				if (path.charAt(path.length()-1) != File.separatorChar)
					path += File.separatorChar;
				EXTERNAL_JAR_DIR_PATH = path;
//{ObjectTeams: use separate dir for our JCL since we use a modified version which must not conflict with the std version:
				EXTERNAL_JAR_DIR_PATH += "OTJCLDir"+File.separatorChar;
// SH}
			} catch (IOException e) {
				e.printStackTrace();
			}
		return EXTERNAL_JAR_DIR_PATH;
	}
	/**
	 * Returns the OS path to the directory that contains this plugin.
	 */
	protected String getPluginDirectoryPath() {
		try {
//{ObjectTeams: adjust plugin id from org.eclipse.jdt.core.tests.model to org.eclipse.objectteams.otdt.tests 
			URL platformURL = Platform.getBundle("org.eclipse.objectteams.otdt.tests").getEntry("/");
//carp}
			return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	protected IJavaProject setUpJavaProject(final String projectName) throws CoreException, IOException {
//{ObjectTeams: OT/J needs at least 1.5 (was 1.4):
		IJavaProject javaProject = setUpJavaProject(projectName, "1.5");
		return javaProject;
// SH}
	}
//{ObjectTeams: copy jcl at least once per test run:
	private static boolean forceCopyJCL= true; 
// SH}
	/**
	 * Check locally for the required JCL files, <jclName>.jar and <jclName>src.zip.
	 * If not available, copy from the project resources.
	 */
	public void setupExternalJCL(String jclName) throws IOException {
		String externalPath = getExternalPath();
		String separator = java.io.File.separator;
		String resourceJCLDir = getPluginDirectoryPath() + separator + "JCL";
		java.io.File jclDir = new java.io.File(externalPath);
		java.io.File jclMin =
			new java.io.File(externalPath + jclName + ".jar");
		java.io.File jclMinsrc = new java.io.File(externalPath + jclName + "src.zip");
		if (!jclDir.exists()) {
			if (!jclDir.mkdir()) {
				//mkdir failed
				throw new IOException("Could not create the directory " + jclDir);
			}
			//copy the two files to the JCL directory
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + jclName + ".jar");
			copy(resourceJCLMin, jclMin);
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + jclName + "src.zip");
			copy(resourceJCLMinsrc, jclMinsrc);
		} else {
			//check that the two files, jclMin.jar and jclMinsrc.zip are present
			//copy either file that is missing or less recent than the one in workspace
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + jclName + ".jar");
//{ObjectTeams: added option forceCopyJCL
			if (forceCopyJCL ||
				(jclMin.lastModified() < resourceJCLMin.lastModified())
                    || (jclMin.length() != resourceJCLMin.length())) {
				copy(resourceJCLMin, jclMin);
			}
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + jclName + "src.zip");
			if (forceCopyJCL ||
				(jclMinsrc.lastModified() < resourceJCLMinsrc.lastModified())
                    || (jclMinsrc.length() != resourceJCLMinsrc.length())) {
				copy(resourceJCLMinsrc, jclMinsrc);
			}
			if (forceCopyJCL)
				System.out.println("Test "+this.getClass().getName()+" has copied jclMin.");
			forceCopyJCL= false; // done
// SH}
		}
	}
	
// ===== End COPY_AND_PASTE
	
	protected IJavaProject createOTJavaProject(String projectName, String[] sourceFolders, String[] libraries, String output) throws CoreException {
		IJavaProject javaProject = createJavaProject(projectName, sourceFolders, libraries, output, "1.5");
		IProjectDescription description = javaProject.getProject().getDescription();
		description.setNatureIds(OTDTPlugin.createProjectNatures(description));
		javaProject.getProject().setDescription(description, null);
		javaProject.setOption(CompilerOptions.OPTION_ReportWeaveIntoSystemClass, CompilerOptions.IGNORE);
		return javaProject;
	}

	// http://trac.objectteams.org/ot/ticket/142
	public void testTrac142() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p = createOTJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "bin");
			IProject project = p.getProject();
			IProjectDescription prjDesc = project.getDescription();
			//prjDesc.setNatureIds(OTDTPlugin.createProjectNatures(prjDesc));
			prjDesc.setBuildSpec(OTDTPlugin.createProjectBuildCommands(prjDesc));
			project.setDescription(prjDesc, null);

			OTREContainer.initializeOTJProject(project);

			this.createFile(
				"/P/SuperFoo.java",
				"public team class SuperFoo {\n" +
				"	public class Role {}\n" +
				"	public SuperFoo(Role r) {\n" +
				"	}\n" +
				"}\n"
			);
			String sourceFoo = "public team class Foo extends SuperFoo {\n" +
			"	public class Role playedBy String {}\n" +
			"	public Foo(String as Role r) {\n" +
			"       super(r);\n" +
			"	}\n" +
			"}\n";
			this.createFile(
					"/P/Foo.java",
					sourceFoo
			);

			char[] sourceChars = sourceFoo.toCharArray();
			this.problemRequestor.initialize(sourceChars);
		
			
			getCompilationUnit("/P/Foo.java").getWorkingCopy(this.wcOwner, null);
			
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n"
			);
		} finally {
			deleteProject("P");
		}
	}
	
	// http://trac.objectteams.org/ot/ticket/143 (comment1)
	public void testTrac143b() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p = createOTJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "bin");
			IProject project = p.getProject();
			IProjectDescription prjDesc = project.getDescription();
			prjDesc.setNatureIds(OTDTPlugin.createProjectNatures(prjDesc));
			prjDesc.setBuildSpec(OTDTPlugin.createProjectBuildCommands(prjDesc));
			project.setDescription(prjDesc, null);

			OTREContainer.initializeOTJProject(project);

			this.createFile(
				"/P/SuperFoo.java",
				"public team class SuperFoo {\n" +
				"	public abstract class Role {\n" +
				"		abstract void foo();\n" +
				"	}\n" +
				"}\n"
			);
			String sourceFoo = "public team class Foo extends SuperFoo {\n" +
			"	public class Role playedBy String {\n" +
			"		void foo() -> int length();\n" +
			"	}\n" +
			"}\n";
			this.createFile(
					"/P/Foo.java",
					sourceFoo
			);
			String sourceSubFoo = "public team class SubFoo extends Foo {\n" +
			"	public class Role {}\n" +
			"}\n";
			this.createFile(
					"/P/SubFoo.java",
					sourceSubFoo
			);

			char[] sourceChars = sourceSubFoo.toCharArray();
			this.problemRequestor.initialize(sourceChars);
		
			
			getCompilationUnit("/P/SubFoo.java").getWorkingCopy(this.wcOwner, null);
			
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n"
			);
		} finally {
			deleteProject("P");
		}
	}
	

	// http://trac.objectteams.org/ot/ticket/248
	public void testTrac248() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p = createOTJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
			IProject project = p.getProject();
			IProjectDescription prjDesc = project.getDescription();
			prjDesc.setNatureIds(OTDTPlugin.createProjectNatures(prjDesc));
			prjDesc.setBuildSpec(OTDTPlugin.createProjectBuildCommands(prjDesc));
			project.setDescription(prjDesc, null);

			OTREContainer.initializeOTJProject(project);

			// ROFI will be pulled in via CompilationUnitProblemFinder.accept -> SourceTypeConverter.buildCompilationUnit
			this.createFolder("/P/src/FooTeam");
			this.createFile(
					"/P/src/FooTeam/R.java",
					"team package FooTeam;\n" +
					"protected class R {\n" +
					"    private static int getI() {\n" + 
					"        return 3;\n" + 
					"    }\n" + 
					"    protected int test() {\n" + 
					"        return getI();\n" + 
					"    }\n" +
					"}\n");
			
			String sourceTeam = 
				"public team class FooTeam {\n" +
				"   R r;\n" +
				"}\n";
			this.createFile(
					"/P/src/FooTeam.java",
					sourceTeam
			);

			char[] sourceChars = sourceTeam.toCharArray();
			this.problemRequestor.initialize(sourceChars);
		
			
			getCompilationUnit("/P/src/FooTeam.java").getWorkingCopy(this.wcOwner, null);
			
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n"
			);
		} finally {
			deleteProject("P");
		}
	}	

	// http://trac.objectteams.org/ot/ticket/259
	public void testTrac259() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p = createOTJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "bin");
			IProject project = p.getProject();
			IProjectDescription prjDesc = project.getDescription();
			prjDesc.setNatureIds(OTDTPlugin.createProjectNatures(prjDesc));
			prjDesc.setBuildSpec(OTDTPlugin.createProjectBuildCommands(prjDesc));
			project.setDescription(prjDesc, null);

			OTREContainer.initializeOTJProject(project);

			this.createFile(
				"/P/MyBase.java",
				"public class MyBase {\n" +
				"	private void secretDo() {\n" +
				"	}\n" +
				"}\n"
			);
			this.createFolder("/P/Foo");
			this.createFile("/P/Foo/Role.java", 
					"team package Foo;\n" +
					"protected class Role playedBy MyBase {\n" +
					"	protected void doit() -> void secretDo();\n" +
					"}\n");
			String sourceFoo = "public team class Foo {\n" +
			"	protected void test(Role r) {\n" +
			" 		r.doit();\n" +
			"	}\n" +
			"}\n";
			this.createFile(
					"/P/Foo.java",
					sourceFoo
			);

			char[] sourceChars = sourceFoo.toCharArray();
			this.problemRequestor.initialize(sourceChars);
		
			
			getCompilationUnit("/P/Foo.java").getWorkingCopy(this.wcOwner, null);
			
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n"
			);
		} finally {
			deleteProject("P");
		}
	}
	
	// previously a syntax error could cause an IllegalArgumentException, see r19238 ff.
	public void testSyntaxError() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p = createOTJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "bin");
			IProject project = p.getProject();
			IProjectDescription prjDesc = project.getDescription();
			prjDesc.setNatureIds(OTDTPlugin.createProjectNatures(prjDesc));
			prjDesc.setBuildSpec(OTDTPlugin.createProjectBuildCommands(prjDesc));
			project.setDescription(prjDesc, null);

			OTREContainer.initializeOTJProject(project);

			this.createFile(
				"/P/Bar.java",
				"public class Bar {\n" +
				"   String baz() { return null;  }\n" +
				"}\n"
			);
			String sourceFoo = "public team class Foo  {\n" +
			"	public class Role playedBy String \n" +
			"       base when (base.){\n" +
			"       String getFoo() -> get String foo\n" +
			"         with { result <- (String)foo }\n" +
			"   }\n" +
			"   void doodle() {}" +
			"}\n";
			this.createFile(
					"/P/Foo.java",
					sourceFoo
			);
			String sourceMain = "public class Main {\n" +
					"void zork(Foo f) {\n" +
					"    f.doodle();\n" +
					"}\n" +
					"}";
			this.createFile(
					"/P/Main.java",
					sourceMain
			);

			char[] sourceChars = sourceMain.toCharArray();
			this.problemRequestor.initialize(sourceChars);
		
			
			getCompilationUnit("/P/Main.java").getWorkingCopy(this.wcOwner, null);
			
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n"
			);
		} finally {
			deleteProject("P");
		}
	}
	
	@SuppressWarnings("unchecked") // options in a raw map
	public void testLocalInRoFi() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p = createOTJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "bin");
			// set compliance to 1.6 to force generated of a stack map attribute.
			Map options = p.getOptions(true);
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
			p.setOptions(options);
			IProject project = p.getProject();
			IProjectDescription prjDesc = project.getDescription();
			prjDesc.setNatureIds(OTDTPlugin.createProjectNatures(prjDesc));
			prjDesc.setBuildSpec(OTDTPlugin.createProjectBuildCommands(prjDesc));
			project.setDescription(prjDesc, null);

			OTREContainer.initializeOTJProject(project);

			this.createFile(
				"/P/Plain.java",
				"public class Plain {\n" +
				"    public Object getVal() {\n" + 
				"        return \"Plain\";\n" + 
				"    }\n" +
				"}\n"
			);
			String sourceTeam = "public team class Foo {\n" +
				"    String foo, blub, bar, dings, wurgs, zork;\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new Foo().new Role().test();\n" + 
				"    }\n" +
				"}\n";
			this.createFile(
					"/P/Foo.java",
					sourceTeam
			);
			this.createFolder("/P/Foo");
			
			String sourceRole1= "team package Foo;\n" +
				"@SuppressWarnings(\"unused\")\n" +
				"public class Role1 {\n" +
				"	String s1, s2, s3, s4, s5, s6;\n" +
				"}\n";
			this.createFile(
					"/P/Foo/Role1.java",
					sourceRole1);

			String sourceRole2 = "team package Foo;\n" +
				"@SuppressWarnings(\"unused\")\n" +
				"public class Role2 extends Role1 {\n" +
				"        protected void test() {\n" + 
				"            Plain isub = new Plain() {\n" + 
				"                public Object getVal() {\n" + 
				"                    Object edits = super.getVal();\n" + 
				"                    if (edits instanceof String) {\n" + 
				"                        String string = (String)edits;\n" + 
				"                        int l = string.length();\n" + 
				"                    }\n" + 
				"                    return edits;\n" + 
				"                }\n" + 
				"            };\n" + 
				"            Object v = isub.getVal();\n" + 
				"        }\n" +
				"}\n";
			this.createFile(
					"/P/Foo/Role2.java",
					sourceRole2
			);

			char[] sourceChars = sourceRole2.toCharArray();
			this.problemRequestor.initialize(sourceChars);
		
			
			ICompilationUnit wc = getCompilationUnit("/P/Foo/Role2.java").getWorkingCopy(this.wcOwner, null);
			wc.reconcile(AST.JLS3, 
						 ICompilationUnit.FORCE_PROBLEM_DETECTION|ICompilationUnit.ENABLE_STATEMENTS_RECOVERY|ICompilationUnit.ENABLE_BINDINGS_RECOVERY,
						 wc.getOwner(), null);
			
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n" +
				"----------\n" +
				"----------\n"
			);
		} finally {
			deleteProject("P");
		}
	}
	

	@SuppressWarnings("unchecked") // options in a raw map
	public void testRemoveRole() throws CoreException, InterruptedException, UnsupportedEncodingException {
		try {
			// Resources creation
			IJavaProject p = createOTJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "bin");
			// set compliance to 1.6 to force generated of a stack map attribute.
			Map options = p.getOptions(true);
			options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
			options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
			options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
			p.setOptions(options);
			IProject project = p.getProject();
			IProjectDescription prjDesc = project.getDescription();
			prjDesc.setNatureIds(OTDTPlugin.createProjectNatures(prjDesc));
			prjDesc.setBuildSpec(OTDTPlugin.createProjectBuildCommands(prjDesc));
			project.setDescription(prjDesc, null);

			OTREContainer.initializeOTJProject(project);


			String sourceTeam = "public team class Foo {\n" +
				"	public class Role1 {\n" +
				"		String s1, s2, s3, s4, s5, s6;\n" +
				"	}\n" +
				"   String foo, blub, bar, dings, wurgs, zork;\n" + 
				"   public static void main(String[] args) {\n" + 
				"       new Foo().new Role1().test();\n" + 
				"   }\n" +
				"}\n";
			this.createFile(
					"/P/Foo.java",
					sourceTeam
			);
					
			this.problemRequestor.initialize(sourceTeam.toCharArray());
			
			ICompilationUnit wc = getCompilationUnit("/P/Foo.java").getWorkingCopy(this.wcOwner, null);
			IType itype = wc.getType("Foo");
			IOTType ottype = OTModelManager.getOTElement(itype);
			IType rt = ottype.getRoleType("Role1");
			IOTType roleType = OTModelManager.getOTElement(rt); // this caches the role type
			assertTrue ("Role should initially exist", rt.exists());
			assertTrue ("RoleType should initially exist", roleType.exists());
			
			// delete Role1
			wc.applyTextEdit(new DeleteEdit(
					("public team class Foo {\n").length(), 
					("	public class Role1 {\n" +
							"		String s1, s2, s3, s4, s5, s6;\n" +
					"	}\n").length()), 
					null);
			wc.reconcile(AST.JLS3, 
					ICompilationUnit.FORCE_PROBLEM_DETECTION|ICompilationUnit.ENABLE_STATEMENTS_RECOVERY|ICompilationUnit.ENABLE_BINDINGS_RECOVERY,
					wc.getOwner(), null);
			
			// watch that
			// ... the JavaElement no longer exists:
			rt = itype.getType("Role1");
			assertFalse ("Role should no longer exist", rt.exists());
			// ... and OTModel no longer has a RoleType:
			ottype = OTModelManager.getOTElement(itype);
			rt = ottype.getRoleType("Role1");
			assertNull("Role should be null", rt);			
		} finally {
			deleteProject("P");
		}
	}

	public void testAnchoredType01() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p = createOTJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "bin");
			IProject project = p.getProject();
			IProjectDescription prjDesc = project.getDescription();
			//prjDesc.setNatureIds(OTDTPlugin.createProjectNatures(prjDesc));
			prjDesc.setBuildSpec(OTDTPlugin.createProjectBuildCommands(prjDesc));
			project.setDescription(prjDesc, null);

			OTREContainer.initializeOTJProject(project);

			this.createFile(
				"/P/BaseTeam.java",
				"public team class BaseTeam {\n" +
				"	public class Role {}\n" +
				"}\n"
			);
			
			String sourceFoo = "public team class Foo {\n" +
					"   protected team class Mid playedBy BaseTeam {\n" +
					"		public class Inner1 playedBy Role<@Mid.base> {}\n" +
					"		protected class Inner2 playedBy Role<@base> {}\n" +
					"		@SuppressWarnings(\"roletypesyntax\")\n" +
					"		protected class Inner3 playedBy base.Role {}\n" +
					"	}\n" +
					"}\n";
			this.createFile(
					"/P/Foo.java",
					sourceFoo
			);

			char[] sourceChars = sourceFoo.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			
			ICompilationUnit fooWC = getCompilationUnit("/P/Foo.java").getWorkingCopy(this.wcOwner, null);
			IType foo =  fooWC.getType("Foo");
			
			CompilerOptions compilerOptions = new CompilerOptions(p.getOptions(true));
			ProblemReporter problemReporter = new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					compilerOptions,
					new DefaultProblemFactory());
			
			// force usage of type converter:
			CompilationUnitDeclaration parsedUnit =
				SourceTypeConverter.buildCompilationUnit(
						new ISourceType[] {(ISourceType) ((SourceType)foo).getElementInfo()},
						SourceTypeConverter.FIELD_AND_METHOD | SourceTypeConverter.MEMBER_TYPE,
						problemReporter,
						new CompilationResult("Foo.java".toCharArray(), 1, 1, 90));
			
			// force resolving:
			process(parsedUnit, p, compilerOptions, problemReporter, ITranslationStates.STATE_RESOLVED);
			
			// evaluate result:
			String result = "";
			CategorizedProblem[] problems = parsedUnit.compilationResult().problems;
			assertNotNull(problems);
			for (IProblem problem : problems)
				if (problem != null && problem.isError())
					result += problem;
			assertEquals("", result);
		} finally {
			deleteProject("P");
		}
	}
	
	public void testAnchoredType02() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p = createOTJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "bin");
			IProject project = p.getProject();
			IProjectDescription prjDesc = project.getDescription();
			//prjDesc.setNatureIds(OTDTPlugin.createProjectNatures(prjDesc));
			prjDesc.setBuildSpec(OTDTPlugin.createProjectBuildCommands(prjDesc));
			project.setDescription(prjDesc, null);

			OTREContainer.initializeOTJProject(project);

			this.createFile(
				"/P/BaseTeam.java",
				"public team class BaseTeam {\n" +
				"	public class Role {}\n" +
				"   Role baseField;" +
				"}\n"
			);
			
			String sourceFoo = "public team class Foo {\n" +
					"   protected team class Mid playedBy BaseTeam {\n" +
					"		public class Inner playedBy Role<@Mid.base> {}\n" +
					"       Inner get1() -> get Role<@Mid.base> baseField;\n" +
					"       Inner get2() -> get Role<@base> baseField;\n" +
					"		@SuppressWarnings(\"roletypesyntax\")\n" +
					"       Inner get3() -> get base.Role baseField;\n" +
					"	}\n" +
					"}\n";
			this.createFile(
					"/P/Foo.java",
					sourceFoo
			);

			char[] sourceChars = sourceFoo.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			
			ICompilationUnit fooWC = getCompilationUnit("/P/Foo.java").getWorkingCopy(this.wcOwner, null);
			IType foo =  fooWC.getType("Foo");
			
			CompilerOptions compilerOptions = new CompilerOptions(p.getOptions(true));
			ProblemReporter problemReporter = new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					compilerOptions,
					new DefaultProblemFactory());
			
			// force usage of type converter:
			CompilationUnitDeclaration parsedUnit =
				SourceTypeConverter.buildCompilationUnit(
						new ISourceType[] {(ISourceType) ((SourceType)foo).getElementInfo()},
						SourceTypeConverter.FIELD_AND_METHOD | SourceTypeConverter.MEMBER_TYPE,
						problemReporter,
						new CompilationResult("Foo.java".toCharArray(), 1, 1, 90));
			
			// force resolving:
			process(parsedUnit, p, compilerOptions, problemReporter, ITranslationStates.STATE_RESOLVED);
			
			// evaluate result:
			String result = "";
			CategorizedProblem[] problems = parsedUnit.compilationResult().problems;
			assertNotNull(problems);
			for (IProblem problem : problems)
				if (problem != null && problem.isError())
					result += problem;
			assertEquals("", result);
		} finally {
			deleteProject("P");
		}
	}

	class SourceTypeCompiler extends Compiler {
		public SourceTypeCompiler(INameEnvironment nameEnvironment,
				IErrorHandlingPolicy policy,
				CompilerOptions compilerOptions,
				IProblemFactory problemFactory) 
		{
			super(nameEnvironment, policy, compilerOptions, 
				  new ICompilerRequestor() { public void acceptResult(CompilationResult result) { } },
				  problemFactory);
		}

		// from CompilationUnitProblemFinder:
		public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
			// ensure to jump back to toplevel type for first one (could be a member)
//				while (sourceTypes[0].getEnclosingType() != null)
//					sourceTypes[0] = sourceTypes[0].getEnclosingType();

			CompilationResult result =
				new CompilationResult(sourceTypes[0].getFileName(), 1, 1, this.options.maxProblemsPerUnit);

			// need to hold onto this
			CompilationUnitDeclaration unit =
				SourceTypeConverter.buildCompilationUnit(
					sourceTypes,//sourceTypes[0] is always toplevel here
					SourceTypeConverter.FIELD_AND_METHOD // need field and methods
					| SourceTypeConverter.MEMBER_TYPE // need member types
					| SourceTypeConverter.FIELD_INITIALIZATION, // need field initialization
					this.lookupEnvironment.problemReporter,
					result);

			if (unit != null) {
	//{ObjectTeams: controlled by Dependencies:
			  boolean newDependencySetup= false;
			  try {
				if (!Dependencies.isSetup()) {
					newDependencySetup= true;
					Dependencies.setup(this, this.parser, this.lookupEnvironment, true, false);
				}
				// Note(SH): this will redirect:
				this.lookupEnvironment.buildTypeBindings(unit, accessRestriction);
				this.lookupEnvironment.completeTypeBindings(unit);
			  } finally {
				if (newDependencySetup)
					Dependencies.release(this);
			  }
	// SH}
			}
		}
	}
	private void process(CompilationUnitDeclaration parsedUnit, IJavaProject p,
			CompilerOptions compilerOptions, ProblemReporter problemReporter, int state)
			throws JavaModelException {
		Parser parser = new Parser(problemReporter, false);
		INameEnvironment nameEnvironment = new SearchableEnvironment((JavaProject)p, this.wcOwner);
		Compiler compiler = new SourceTypeCompiler(
					nameEnvironment, 
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					problemReporter.problemFactory);
		
		Dependencies.setup(this, parser, compiler.lookupEnvironment, true, false);
		Dependencies.ensureState(parsedUnit, state);
	}
	
	public void testAnchoredType03() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p = createOTJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "bin");
			IProject project = p.getProject();
			IProjectDescription prjDesc = project.getDescription();
			//prjDesc.setNatureIds(OTDTPlugin.createProjectNatures(prjDesc));
			prjDesc.setBuildSpec(OTDTPlugin.createProjectBuildCommands(prjDesc));
			project.setDescription(prjDesc, null);

			OTREContainer.initializeOTJProject(project);

			this.createFile(
				"/P/BaseTeam.java",
				"public team class BaseTeam {\n" +
				"	public class Role {}\n" +
				"}\n"
			);
			String sourceFoo = "public team class Foo {\n" +
					"   protected team class Mid playedBy BaseTeam {\n" +
					"		public class Inner1 playedBy Role<@Mid.base> {}\n" +
					"		protected class Inner2 playedBy Role<@base> {}\n" +
					"		@SuppressWarnings(\"roletypesyntax\")\n" +
					"		protected class Inner3 playedBy base.Role {}\n" +
					"	}\n" +
					"}\n";
			this.createFile(
					"/P/Foo.java",
					sourceFoo
			);

			char[] sourceChars = sourceFoo.toCharArray();
			this.problemRequestor.initialize(sourceChars);
		
			this.createFile(
					"/P/Main.java",
					"public team class Main extends Foo {\n" +
					"	public team class Mid {\n" +
					"		public class Inner1 {}\n" +
					"		public class Inner2 {}\n" +
					"		public class Inner3 {}\n" +
					"	}\n" +
					"}\n"
			);
			
			getCompilationUnit("/P/Main.java").getWorkingCopy(this.wcOwner, null);
			
			assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n"
			);
		} finally {
			deleteProject("P");
		}
	}
	
}
