/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Fraunhofer FIRST - Initial API and implementation
 * 	   Technical University Berlin - Initial API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.core;

import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedMethodsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jdt.testplugin.TestOptions;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.tests.core.ProjectTestSetup;

/** Structure is OT_COPY_PASTE from {@link org.eclipse.jdt.ui.tests.core.source.AddUnimplementedMethodsTest} 
 * @since 1.2.1 
 */
public class AddUnimplementedMethodsTest extends TestCase {
	
	private static final Class THIS= AddUnimplementedMethodsTest.class;
	
	private IJavaProject fJavaProject;
	private IPackageFragment fPackage;
	
	public AddUnimplementedMethodsTest(String name) {
		super(name);
	}
		
	public static Test allTests() {
		return new ProjectTestSetup(new TestSuite(THIS));
	}

	public static Test suite() {
		if (true) {
			return allTests();
		} else {
			TestSuite suite= new TestSuite();
			suite.addTest(new AddUnimplementedMethodsTest("test1"));
			return new ProjectTestSetup(suite);
		}	
	}
	
	protected void setUp() throws Exception {
		fJavaProject= JavaProjectHelper.createJavaProject("DummyProject", "bin");
		assertNotNull(JavaProjectHelper.addRTJar(fJavaProject));
		
		Hashtable options= TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "999");
		fJavaProject.setOptions(options);
		
		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "${body_statement}\n// TODO", null);
		
		IPackageFragmentRoot root= JavaProjectHelper.addSourceContainer(fJavaProject, "src");
		fPackage= root.createPackageFragment("org.eclispe.objectteams.util", true, null);
		
		IEclipsePreferences node= new ProjectScope(fJavaProject.getProject()).getNode(JavaUI.ID_PLUGIN);
		node.putBoolean(PreferenceConstants.CODEGEN_USE_OVERRIDE_ANNOTATION, false);
		node.putBoolean(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);
		node.flush();
	}


	protected void tearDown () throws Exception {
		JavaProjectHelper.delete(fJavaProject);
		fJavaProject= null;
		fPackage= null;
	}

	public void testTrac143() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("package org.eclipse.objectteams.util;\n");
		buf.append("import java.util.Properties;\n");
		buf.append("public team class F {\n");
		buf.append("  public class R {\n");
		buf.append("    public abstract void b(Properties p);\n");
		buf.append("  }\n");
		buf.append("}\n");
		fPackage.createCompilationUnit("F.java", buf.toString(), false, null);
		
		buf= new StringBuffer();
		buf.append("package org.eclipse.objectteams.util;\n");
		ICompilationUnit cu= fPackage.createCompilationUnit("F2.java", buf.toString(), false, null);
		
		IType testClass= cu.createType("public team class F2 extends F {\n\n}\n", null, true, null);
		testClass = testClass.createType("public class R playedBy Object {\n\n}\n", null, true, null);
		
		testHelper(testClass);
		
		IMethod[] methods= testClass.getMethods();
		checkMethods(new String[] { "b", "equals", "clone", "toString", "finalize", "hashCode" }, methods);
		
		IImportDeclaration[] imports= cu.getImports();
		checkImports(new String[]{"java.util.Properties"}, imports);
	}
	
	
	private void testHelper(IType testClass) throws JavaModelException, CoreException {
		testHelper(testClass, -1, true);
	}
	
	private void testHelper(IType testClass, int insertionPos, boolean implementAllOverridable) throws JavaModelException, CoreException {
		RefactoringASTParser parser= new RefactoringASTParser(AST.JLS3);
		CompilationUnit unit= parser.parse(testClass.getCompilationUnit(), true);
		AbstractTypeDeclaration declaration= (AbstractTypeDeclaration) ASTNodes.getParent(NodeFinder.perform(unit, testClass.getNameRange()), AbstractTypeDeclaration.class);
		assertNotNull("Could not find type declaration node", declaration);
		ITypeBinding binding= declaration.resolveBinding();
		assertNotNull("Binding for type declaration could not be resolved", binding);
		
		IMethodBinding[] overridableMethods= implementAllOverridable ? StubUtility2.getOverridableMethods(unit.getAST(), binding, false) : null;
		
		AddUnimplementedMethodsOperation op= new AddUnimplementedMethodsOperation(unit, binding, overridableMethods, insertionPos, true, true, true);
		op.run(new NullProgressMonitor());
		JavaModelUtil.reconcile(testClass.getCompilationUnit());
	}
	
	private void checkMethods(String[] expected, IMethod[] methods) {
		int nMethods= methods.length;
		int nExpected= expected.length;
		assertTrue("" + nExpected + " methods expected, is " + nMethods, nMethods == nExpected);
		for (int i= 0; i < nExpected; i++) {
			String methName= expected[i];
			assertTrue("method " + methName + " expected", nameContained(methName, methods));
		}
	}			
	
	private void checkImports(String[] expected, IImportDeclaration[] imports) {
		int nImports= imports.length;
		int nExpected= expected.length;
		if (nExpected != nImports) {
			StringBuffer buf= new StringBuffer();
			buf.append(nExpected).append(" imports expected, is ").append(nImports).append("\n");
			buf.append("expected:\n");
			for (int i= 0; i < expected.length; i++) {
				buf.append(expected[i]).append("\n");
			}
			buf.append("actual:\n");
			for (int i= 0; i < imports.length; i++) {
				buf.append(imports[i]).append("\n");
			}
			assertTrue(buf.toString(), false);
		}
		for (int i= 0; i < nExpected; i++) {
			String impName= expected[i];
			assertTrue("import " + impName + " expected", nameContained(impName, imports));
		}
	}

	private boolean nameContained(String methName, IJavaElement[] methods) {
		for (int i= 0; i < methods.length; i++) {
			if (methods[i].getElementName().equals(methName)) {
				return true;
			}
		}
		return false;
	}	
}
