/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MethodMappingCompletionProposal.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.ui.assist.CompletionAdaptor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedCorrectionProposal;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * This abstract class is a convenience for its subclasses:
 * rewrite-based completion proposals relating to method mappings.
 *  
 * @author stephan
 * @since 1.1.8
 */
@SuppressWarnings("restriction")
protected abstract class MethodMappingCompletionProposal extends LinkedCorrectionProposal 
{
	
	static final String ROLEMETHODRETURN_KEY= "rolemethodreturn"; //$NON-NLS-1$ 
	static final String ROLEMETHODNAME_KEY= "rolemethodname";  //$NON-NLS-1$
	static final String ROLEPARAM_KEY= "roleparam";  //$NON-NLS-1$
	static final String BINDINGKIND_KEY = "bindingkind"; //$NON-NLS-1$
	
	String[] 	 fParamTypes;
	String 		 fMethodName;
	
	IJavaProject fJavaProject;
	
	// info from the CompletionProposal
	int 		 fReplaceStart;
	int          fLength;
	String	     fReplacementString;

	public MethodMappingCompletionProposal(IJavaProject 	  jProject, 
									       ICompilationUnit   cu,  
									       CompletionProposal proposal,  
										   int                length,
										   String             displayName,
										   Image              image) 
	{
		super(displayName, cu, null, computeRelevance(proposal), image);
		fMethodName=   String.valueOf(proposal.getName());
		fJavaProject=  jProject;
		fReplaceStart= proposal.getReplaceStart();
		fLength=       length;
		fReplacementString= new String(proposal.getCompletion());
	}
	public MethodMappingCompletionProposal(IJavaProject 	  jProject, 
									       ICompilationUnit   cu,  
									       CompletionProposal proposal,
									       String[]			  paramTypes,
										   int                length,
										   String             displayName,
										   Image              image) 
	{
		this(jProject, cu, proposal, length, displayName, image);
		fParamTypes=   paramTypes;
	}

	/*
	 */
	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		ICompilationUnit iCU= getCompilationUnit();
		CompilationUnit unit= ASTResolving.createQuickFixAST(iCU, null);
		ImportRewrite importRewrite= createImportRewrite(unit);

		// find enclosing mapping and type:
		AbstractMethodMappingDeclaration partialMapping=null;
		ASTNode node= NodeFinder.perform(unit, fReplaceStart, 0);
		while(node != null && !(node instanceof AbstractTypeDeclaration)) 
		{
			if (partialMapping == null && (node instanceof AbstractMethodMappingDeclaration))
				partialMapping= (AbstractMethodMappingDeclaration)node;
			node= node.getParent();
		}
		
		if (node != null) {
			AbstractTypeDeclaration declaration= ((AbstractTypeDeclaration) node);
			ChildListPropertyDescriptor bodyProperty= declaration.getBodyDeclarationsProperty();

			// find role and base type bindings:
			ITypeBinding roleBinding = declaration.resolveBinding();
			ITypeBinding baseBinding = null;
			if (roleBinding != null) {
				baseBinding = roleBinding.getBaseClass();
			} else if (declaration instanceof RoleTypeDeclaration) {
				baseBinding = ((RoleTypeDeclaration)declaration).getBaseClassType().resolveBinding();
			}
			if (baseBinding == null) {
				OTDTUIPlugin.getDefault().getLog().log(new Status(Status.ERROR, "org.eclipse.objectteams.otdt.jdt.ui", "could not resolve type bindings")); //$NON-NLS-1$ //$NON-NLS-2$
				return null;
			}
			
			// create and setup the rewrite:
			ASTRewrite rewrite= createRewrite(unit.getAST());
			rewrite.setToOTJ();
			if (setupRewrite(iCU, rewrite, importRewrite, roleBinding, baseBinding, node, partialMapping, bodyProperty))
				return rewrite;

			// rewriting was not successful, use the original replace string from the CompletionProposal:
			return new ASTRewrite(unit.getAST()) {
				@Override
				public TextEdit rewriteAST() {
					return new ReplaceEdit(fReplaceStart, fLength, fReplacementString);
				}
			};
		}
		return null;
	}

	/** Create a fresh rewrite, by default nothing exciting, but overridable.. */
	ASTRewrite createRewrite(AST ast) {
		return ASTRewrite.create(ast);
	}
	
	abstract boolean setupRewrite(ICompilationUnit                 iCU,
							      ASTRewrite                       rewrite,
								  ImportRewrite                    importRewrite,
							      ITypeBinding					   roleBinding,
							      ITypeBinding					   baseBinding,
							      ASTNode                          type,
							      AbstractMethodMappingDeclaration partialMapping,
							      ChildListPropertyDescriptor      bodyProperty) 
		throws CoreException;
	
	/** replace getOverridableMethods with a lookup of our own. */
	IMethodBinding findMethod(ITypeBinding type, String selector, String[] paramTypeNames) {
		while (type != null) {
			IMethodBinding result= Bindings.findMethodInType(type, selector, paramTypeNames);
			if (result != null)
				return result;
			type= type.getSuperclass();
		}
		return null;
	}
}
