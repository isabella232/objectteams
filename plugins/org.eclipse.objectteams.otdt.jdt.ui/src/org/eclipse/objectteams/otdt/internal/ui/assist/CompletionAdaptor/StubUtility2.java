/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: StubUtility2.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	   Technical University Berlin - Initial API and implementation
 *     IBM Corporation - implementation of individual method bodies
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.ui.assist.CompletionAdaptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.FieldAccessSpec;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodBindingOperator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.ui.CodeGeneration;

/**
 * Add new functions to StubUtility2, accessing otherwise invisible helper functions.
 * @author stephan
 * @since 1.1.2
 */
@SuppressWarnings({ "unchecked", "rawtypes"/*parameter-less lists from DOM*/, "restriction", "decapsulation"/*final baseclass + callout-decapsulation*/ })
protected class StubUtility2 playedBy StubUtility2 
{

	// CALLOUT INTERFACE:
	List createParameters(IJavaProject project, ImportRewrite imports, ImportRewriteContext context, AST ast, IMethodBinding binding, MethodDeclaration decl) 
		-> List createParameters(IJavaProject project, ImportRewrite imports, ImportRewriteContext context, AST ast, IMethodBinding binding, MethodDeclaration decl);

	void findUnimplementedInterfaceMethods(ITypeBinding typeBinding, HashSet visited, ArrayList allMethods, IPackageBinding currPack, ArrayList toImplement) 
		-> void findUnimplementedInterfaceMethods(ITypeBinding typeBinding, HashSet visited, ArrayList allMethods, IPackageBinding currPack, ArrayList toImplement);
	IMethodBinding findMethodBinding(IMethodBinding method, List allMethods) 
		-> IMethodBinding findMethodBinding(IMethodBinding method, List allMethods);
	
	/** This method is inspired by createImplementationStub(..), but much leaner. */
	public static CalloutMappingDeclaration createCallout(ICompilationUnit unit, 
														  ASTRewrite rewrite, 
														  ImportRewrite imports, 
														  IMethodBinding binding, 
														  String type, 
														  CodeGenerationSettings settings) 
			throws CoreException
	{
		AST ast= rewrite.getAST();
		
		CalloutMappingDeclaration decl= ast.newCalloutMappingDeclaration();
		decl.setSignatureFlag(true);
		// no modifiers
		
		MethodSpec spec = ast.newMethodSpec();
		spec.setSignatureFlag(true);
		spec.setName(ast.newSimpleName(binding.getName()));

		// no type parameters
		
		spec.setReturnType2(imports.addImport(binding.getReturnType(), ast));

		// this helps to reuse some code regarding methods:
		MethodDeclaration method = ast.newMethodDeclaration();
		
		List parameters= createParameters(unit.getJavaProject(), imports, null, ast, binding, method);
		
		spec.parameters().addAll(ASTNode.copySubtrees(ast, parameters)); // copy to re-parent
		
		decl.setRoleMappingElement(spec);
		decl.setBaseMappingElement((MethodSpec)ASTNode.copySubtree(ast, spec));
		// no thrown exceptions
		
		String delimiter= StubUtility.getLineDelimiterUsed(unit);
		// no body
		
		if (settings.createComments) {
			// TODO(SH): this reuses the template for overridden methods, should actually define our own template.
			String string= CodeGeneration.getMethodComment(unit, type, method, binding, delimiter);
			if (string != null) {
				Javadoc javadoc= (Javadoc) rewrite.createStringPlaceholder(string, ASTNode.JAVADOC);
				decl.setJavadoc(javadoc);
			}
		}
		
		// no override annotation
		
		return decl;
	
	}
	/** This method is inspired by createImplementationStub(..), but much leaner. */
	public static CallinMappingDeclaration createCallin(ICompilationUnit unit, 
														ASTRewrite rewrite, 
														ImportRewrite imports, 
														IMethodBinding binding, 
														String type,
														ModifierKeyword callinModifier,
														CodeGenerationSettings settings) 
			throws CoreException
	{
		AST ast= rewrite.getAST();
		
		CallinMappingDeclaration decl= ast.newCallinMappingDeclaration();
		decl.setBindingOperator(ast.newMethodBindingOperator(callinModifier, MethodBindingOperator.KIND_CALLIN));
		// no regular modifiers
		
		MethodSpec spec = ast.newMethodSpec();
		spec.setSignatureFlag(true);
		spec.setName(ast.newSimpleName(binding.getName()));

		// no type parameters
		
		spec.setReturnType2(imports.addImport(binding.getReturnType(), ast));

		// this helps to reuse some code regarding methods:
		MethodDeclaration method = ast.newMethodDeclaration();
		
		List parameters= createParameters(unit.getJavaProject(), imports, null, ast, binding, method);
		
		spec.parameters().addAll(ASTNode.copySubtrees(ast, parameters)); // copy to re-parent
		
		decl.setRoleMappingElement(spec);
		decl.getBaseMappingElements().add(ASTNode.copySubtree(ast, spec));
		// no thrown exceptions
		
		String delimiter= StubUtility.getLineDelimiterUsed(unit);
		// no body
		
		if (settings.createComments) {
			// TODO(SH): this reuses the template for overridden methods, should actually define our own template.
			String string= CodeGeneration.getMethodComment(unit, type, method, binding, delimiter);
			if (string != null) {
				Javadoc javadoc= (Javadoc) rewrite.createStringPlaceholder(string, ASTNode.JAVADOC);
				decl.setJavadoc(javadoc);
			}
		}
		
		// no override annotation
		
		return decl;
	
	}
	/** Stripped down version of createCallout for use after '<-' : */
	public static MethodSpec createMethodSpec(ICompilationUnit unit, 
											  ASTRewrite rewrite, 
											  ImportRewrite imports, 
											  IMethodBinding binding,
											  boolean        hasSignature) 
			throws CoreException
	{
		AST ast= rewrite.getAST();
			
		MethodSpec spec = ast.newMethodSpec();
		spec.setSignatureFlag(hasSignature);
		spec.setName(ast.newSimpleName(binding.getName()));

		// no type parameters
		
		spec.setReturnType2(imports.addImport(binding.getReturnType(), ast));

		// this helps to reuse some code regarding methods:
		MethodDeclaration method = ast.newMethodDeclaration();
		
		List parameters= createParameters(unit.getJavaProject(), imports, null, ast, binding, method);
		
		spec.parameters().addAll(ASTNode.copySubtrees(ast, parameters)); // copy to re-parent
		
		return spec;		
	}
	
	/** New factory method: */
	public static CalloutMappingDeclaration createCalloutToField(ICompilationUnit unit, 
																 ASTRewrite rewrite, 
																 ImportRewrite imports, 
																 String accessorName,
																 IVariableBinding field, 
																 boolean isSetter,
																 String declaringType, 
																 CodeGenerationSettings settings) 
			throws CoreException
	{
		AST ast= rewrite.getAST();
		
		CalloutMappingDeclaration decl= ast.newCalloutMappingDeclaration();
		decl.setSignatureFlag(true);
		
		// role side:
		MethodSpec spec = ast.newMethodSpec();
		spec.setSignatureFlag(true);
		spec.setName(ast.newSimpleName(accessorName));
		if (!isSetter) {
			spec.setReturnType2(imports.addImport(field.getType(), ast));
			// no parameters
		} else {
			spec.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
			createSetterParameter(unit, imports, ast, field, spec);
		}
		decl.setRoleMappingElement(spec);
		
		// base side:
		FieldAccessSpec fSpec= ast.newFieldAccessSpec();
		fSpec.setSignatureFlag(true);
		fSpec.setName(ast.newSimpleName(field.getName()));
		fSpec.setFieldType(imports.addImport(field.getType(), ast));
		decl.bindingOperator().setBindingModifier(isSetter ? Modifier.OT_SET_CALLOUT : Modifier.OT_GET_CALLOUT);
		decl.setBaseMappingElement(fSpec);
		
		if (settings.createComments) {
			String delimiter= StubUtility.getLineDelimiterUsed(unit);
			String string= CodeGeneration.getFieldComment(unit, declaringType, field.getName(), delimiter);
			if (string != null) {
				Javadoc javadoc= (Javadoc) rewrite.createStringPlaceholder(string, ASTNode.JAVADOC);
				decl.setJavadoc(javadoc);
			}
		}	
		return decl;
	}
	
	/** This method is inspired by createImplementationStub(..), but much leaner. */
	public static MethodSpec createMethodSpec(ICompilationUnit unit, 
											  ASTRewrite rewrite, 
											  ImportRewrite imports,
											  ImportRewriteContext context,
											  AST ast, 
											  IMethodBinding binding,
											  boolean hasSignature,
											  String type, 
											  CodeGenerationSettings settings) 
			throws CoreException
	{
		MethodSpec spec = ast.newMethodSpec();
		spec.setSignatureFlag(hasSignature);
		spec.setName(ast.newSimpleName(binding.getName()));
		if (!hasSignature)
			return spec; // nothing more to do
		
		// no type parameters
		
		spec.setReturnType2(imports.addImport(binding.getReturnType(), ast));

		// this helps to reuse some code regarding methods:
		MethodDeclaration method = ast.newMethodDeclaration();
		
		List parameters= createParameters(unit.getJavaProject(), imports, context, ast, binding, method);
		
		spec.parameters().addAll(ASTNode.copySubtrees(ast, parameters)); // copy to re-parent

		return spec;
	}
	
	/** 
	 * This callin adds treatment of implicit inheritance including phantom roles
	 * to it's base version.
	 */
	IMethodBinding[] getUnimplementedMethods(ITypeBinding typeBinding, boolean implementAbstractsOfInput) 
		<- replace IMethodBinding[] getUnimplementedMethods(ITypeBinding typeBinding, boolean implementAbstractsOfInput);
	@SuppressWarnings("basecall")
	callin static IMethodBinding[] getUnimplementedMethods(ITypeBinding typeBinding, boolean implementAbstractsOfInput) {
		// COPY&PASTE from base version:
		ArrayList allMethods= new ArrayList();
		ArrayList toImplement= new ArrayList();

		IMethodBinding[] typeMethods= typeBinding.getDeclaredMethods();
		for (int i= 0; i < typeMethods.length; i++) {
			IMethodBinding curr= typeMethods[i];
			int modifiers= curr.getModifiers();
			if (!curr.isConstructor() && !Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers)) {
//{ObjectTeams: never enter copied methods (have no java element)
			  if (!curr.isCopied())
// SH}
				allMethods.add(curr);
			}
		}
//{ObjectTeams: direct tsuper roles:
		ITypeBinding[] tsupers= typeBinding.getSuperRoles();
		  if (tsupers != null)
			  for (ITypeBinding tsuperRole : tsupers) 
				  findVisibleVirtualMethods(tsuperRole, allMethods);
// SH}
		
		ITypeBinding superClass= typeBinding.getSuperclass();
		while (superClass != null) {
//{ObjectTeams: watch for phantom roles (which don't exist):
		  if (superClass.getJavaElement().exists()) {
			  // extracted orig:
			  findVisibleVirtualMethods(superClass, allMethods);
		  } else {
			  // proceed to the existing super of a phantom role:
			  tsupers= superClass.getSuperRoles();
			  if (tsupers != null)
				  for (ITypeBinding tsuperRole : tsupers) 
					  findVisibleVirtualMethods(tsuperRole, allMethods);
		  }
// SH}
		  superClass= superClass.getSuperclass();
		}

		for (int i= 0; i < allMethods.size(); i++) {
			IMethodBinding curr= (IMethodBinding) allMethods.get(i);
			int modifiers= curr.getModifiers();
			if ((Modifier.isAbstract(modifiers) || curr.getDeclaringClass().isInterface()) && (implementAbstractsOfInput || typeBinding != curr.getDeclaringClass())) {
				// implement all abstract methods
				toImplement.add(curr);
			}
		}

		HashSet visited= new HashSet();
		ITypeBinding curr= typeBinding;
		while (curr != null) {
			ITypeBinding[] superInterfaces= curr.getInterfaces();
			for (int i= 0; i < superInterfaces.length; i++) {
//{ObjectTeams: consider that a role class part finds its super interfaces in the interface part:
				if (curr.isClassPartOf(superInterfaces[i])) {
					// do consider transitively inherited methods:
					ITypeBinding[] superSuperInterfaces = superInterfaces[i].getInterfaces();
					for (int j = 0; j < superSuperInterfaces.length; j++)
						findUnimplementedInterfaceMethods(superSuperInterfaces[j], visited, allMethods, typeBinding.getPackage(), toImplement);
					// but don't add methods from our own ifc-part (occurs when role extends non-role).
					continue; 
				}
// SH}
				findUnimplementedInterfaceMethods(superInterfaces[i], visited, allMethods, typeBinding.getPackage(), toImplement);
			}
			curr= curr.getSuperclass();
		}

		return (IMethodBinding[]) toImplement.toArray(new IMethodBinding[toImplement.size()]);
	}
	// COPY&PASTE: orig extracted as helper from base of above:
	private static void findVisibleVirtualMethods(ITypeBinding typeBinding, ArrayList allMethods) {
		IMethodBinding[] typeMethods= typeBinding.getDeclaredMethods();
		for (int i= 0; i < typeMethods.length; i++) {
			IMethodBinding curr= typeMethods[i];
			int modifiers= curr.getModifiers();
			if (!curr.isConstructor() && !Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers)) {
			  if (!curr.isCopied()) // new check for OT
				if (findMethodBinding(curr, allMethods) == null) {
					allMethods.add(curr);
				}
			}
		}		
	}

	static void createSetterParameter(ICompilationUnit unit, ImportRewrite imports, AST ast, IVariableBinding field, MethodSpec decl) 
	{
		List parameters= decl.parameters();
		SingleVariableDeclaration var= ast.newSingleVariableDeclaration();
		var.setType(imports.addImport(field.getType(), ast));
		var.setName(ast.newSimpleName(field.getName()));
		parameters.add(var);
	}

}

