/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CalloutToFieldCompletionProposal.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.ui.assist.CompletionAdaptor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodBindingOperator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.swt.graphics.Image;

/**
 * 
 * @author stephan
 * @since 1.1.7
 */
@SuppressWarnings("restriction")
protected class CalloutToFieldCompletionProposal extends CreateMethodMappingCompletionProposal
{
	private String fFieldType;
	private String fFieldName;
	private boolean isSetter;
	
	protected CalloutToFieldCompletionProposal(IJavaProject 	  jProject, 
					  						   ICompilationUnit   cu,
					  						   CompletionProposal proposal,
					  						   String             fieldName,
					  						   String 		      fieldType,
					  						   boolean            isSetter,
					  						   boolean 			  isOverride,
					  						   int                length,
					  						   String             displayName,
					  						   Image              image)
    {
		super(jProject, cu, proposal, length, displayName, image);
		this.fFieldType= fieldType;
		this.fFieldName= fieldName;
		this.isSetter=   isSetter;
		this.fIsOverride= isOverride;
    }
	
	@Override
	boolean setupRewrite(ICompilationUnit                 iCU, 
			          ASTRewrite                       rewrite, 
			          ImportRewrite                    importRewrite,
			          ITypeBinding                     roleBinding,
			          ITypeBinding                     baseBinding,
			          ASTNode          				   node,
			          AbstractMethodMappingDeclaration partialMapping,
			          ChildListPropertyDescriptor      bodyProperty) 
			throws CoreException
	{
		// find base field:
		IVariableBinding field= findField(baseBinding, this.fFieldName, this.fFieldType);
		if (field == null)
			return false;
		CodeGenerationSettings settings= JavaPreferencesSettings.getCodeGenerationSettings(this.fJavaProject);
		// create callout:
		CalloutMappingDeclaration stub= StubUtility2.createCalloutToField(
											iCU, rewrite, importRewrite,
											this.fMethodName,
											field, this.isSetter, roleBinding.getName(),
											settings);
		if (stub != null) {
			stub.bindingOperator().setBindingKind(this.fIsOverride ? MethodBindingOperator.KIND_CALLOUT_OVERRIDE : MethodBindingOperator.KIND_CALLOUT);
			insertStub(rewrite, node, bodyProperty, stub);
		}
		return true;
	}
	
	IVariableBinding findField(ITypeBinding type, String selector, String typeName) {
		while (type != null) {
			IVariableBinding result= Bindings.findFieldInType(type, selector);
			if (result != null)
				return result;
			type= type.getSuperclass();
		}
		return null;
	}
}
