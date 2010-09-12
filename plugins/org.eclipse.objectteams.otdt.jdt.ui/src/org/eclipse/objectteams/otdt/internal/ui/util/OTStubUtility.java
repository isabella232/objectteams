/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann.
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
 * Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.util;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.FieldAccessSpec;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodBindingOperator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.ui.CodeGeneration;

import base org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2;

/**
 * OT-Extension for {@link org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2}.
 * @since 0.7.0 (previously these methdds resided in {@link org.eclipse.objectteams.otdt.internal.ui.assist.CompletionAdaptor.StubUtility2})
 */
@SuppressWarnings("restriction")
public team class OTStubUtility {

	private static OTStubUtility instance;

	@SuppressWarnings("decapsulation")
	protected class StubUtility2 playedBy StubUtility2 {

		@SuppressWarnings("rawtypes")
		protected List createParameters(IJavaProject project, ImportRewrite imports,
				ImportRewriteContext context, AST ast, IMethodBinding binding,
				MethodDeclaration decl)
		->
		List createParameters(IJavaProject project, ImportRewrite imports,
				ImportRewriteContext context, AST ast, IMethodBinding binding,
				MethodDeclaration decl);	
	}
	
	static OTStubUtility getDefault() {
		if (instance == null)
			instance = new OTStubUtility();
		return instance;
	}
	@SuppressWarnings("rawtypes")
	List createParameters(IJavaProject project, ImportRewrite imports,
			ImportRewriteContext context, AST ast, IMethodBinding binding,
			MethodDeclaration decl)
	{
		return StubUtility2.createParameters(project, imports, context, ast, binding, decl);
	}
	
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
		
		List parameters= getDefault().createParameters(unit.getJavaProject(), imports, null, ast, binding, method);
		
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
		
		List parameters= getDefault().createParameters(unit.getJavaProject(), imports, null, ast, binding, method);
		
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
		
		List parameters= getDefault().createParameters(unit.getJavaProject(), imports, null, ast, binding, method);
		
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
		FieldAccessSpec fSpec = createFieldSpec(ast, imports, field, true);
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
	
	public static FieldAccessSpec createFieldSpec(AST ast, ImportRewrite imports, IVariableBinding field, boolean hasSignature) 
	{
		FieldAccessSpec fSpec= ast.newFieldAccessSpec();
		fSpec.setSignatureFlag(hasSignature);
		fSpec.setName(ast.newSimpleName(field.getName()));
		fSpec.setFieldType(imports.addImport(field.getType(), ast));
		return fSpec;
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
		
		List parameters= getDefault().createParameters(unit.getJavaProject(), imports, context, ast, binding, method);
		
		spec.parameters().addAll(ASTNode.copySubtrees(ast, parameters)); // copy to re-parent
	
		return spec;
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
