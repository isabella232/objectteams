/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CallinRHSCompletionProposal.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.ui.assist.CompletionAdaptor;

import static org.eclipse.jdt.core.dom.CallinMappingDeclaration.BASE_MAPPING_ELEMENTS_PROPERTY;
import static org.eclipse.jdt.core.dom.MethodBindingOperator.BINDING_MODIFIER_PROPERTY;
import static org.eclipse.objectteams.otdt.ui.ImageConstants.CALLINBINDING_AFTER_IMG;
import static org.eclipse.objectteams.otdt.ui.ImageConstants.CALLINBINDING_BEFORE_IMG;
import static org.eclipse.objectteams.otdt.ui.ImageConstants.CALLINBINDING_REPLACE_IMG;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IMethodMappingBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.objectteams.otdt.internal.ui.util.Images;
import org.eclipse.swt.graphics.Image;

/**
 * Rewrite-based completion proposal for the RHS of an incomplete callin mapping.
 * If the mapping does not yet specify a callin modifier (before,replace,after)
 * one is inferred in accordance with the bound role method.
 * After insertion into the editor the callin modifier can be changed using linked-mode editing.
 *  
 * @author stephan
 * @since 1.1.8
 */
@SuppressWarnings("restriction")
protected class CallinRHSCompletionProposal extends MethodMappingCompletionProposal  {

	protected CallinRHSCompletionProposal(IJavaProject 	     jProject,
										  ICompilationUnit   cu,
										  CompletionProposal proposal,
										  String[]           paramTypes,
										  int                length,
										  String             displayName,
										  Image              image)
	{
		super(jProject, cu,  proposal, paramTypes, length, displayName, image);
	}
	
	@SuppressWarnings("unchecked") // list of method specs from dom.
	@Override
	boolean setupRewrite(ICompilationUnit                 iCU,
			          	 ASTRewrite                       rewrite,
			          	 ImportRewrite                    importRewrite,
			          	 ITypeBinding                     roleBinding,
			          	 ITypeBinding					  baseBinding,
			          	 ASTNode                          type,
			          	 AbstractMethodMappingDeclaration partialMapping,
			          	 ChildListPropertyDescriptor      bodyProperty) 
			throws CoreException
	{
		if (partialMapping == null || !(partialMapping instanceof CallinMappingDeclaration))
			return false;
		// find base method:
		IMethodBinding method= findMethod(baseBinding, fMethodName, fParamTypes);
		if (method == null)
			return false;
		CallinMappingDeclaration callinMapping= (CallinMappingDeclaration)partialMapping;

		ListRewrite baseSpecsRewrite = rewrite.getListRewrite(partialMapping, BASE_MAPPING_ELEMENTS_PROPERTY);
		
		int insertPosition= 0;
		if (fLength > 0) {
			// need to remove partial method spec:
			List<MethodSpec> baseSpecs= callinMapping.getBaseMappingElements();
			for (int i = 0; i < baseSpecs.size(); i++) {
				MethodSpec spec= baseSpecs.get(i);
				if (   spec.getStartPosition() == fReplaceStart
					&& spec.getLength()        == fLength)
				{
					baseSpecsRewrite.remove(spec, null);
					insertPosition= i+1;
					break;
				}
			}
		}

		// create and insert:
		boolean hasSignature= callinMapping.getRoleMappingElement().hasSignature();
		MethodSpec spec= StubUtility2
					.createMethodSpec(iCU, rewrite, importRewrite, method, hasSignature);
		baseSpecsRewrite.insertAt(spec, insertPosition, null);
		
		int existingMod= ((CallinMappingDeclaration)partialMapping).getCallinModifier();
		if (existingMod == Modifier.OT_MISSING_MODIFIER)
		{
			// initial modifier (should match the role method):
			ModifierKeyword defaultKeyword = ModifierKeyword.BEFORE_KEYWORD;
			IMethodMappingBinding mappingBinding= partialMapping.resolveBinding();
			if (mappingBinding != null) {
				IMethodBinding roleMethod= mappingBinding.getRoleMethod();
				if (   roleMethod != null
					&& (roleMethod.getModifiers() & ExtraCompilerModifiers.AccCallin) != 0)
					defaultKeyword= ModifierKeyword.REPLACE_KEYWORD;
				else
					defaultKeyword= ModifierKeyword.BEFORE_KEYWORD;
			}
			Modifier afterMod= rewrite.getAST().newModifier(defaultKeyword);
			rewrite.set(partialMapping.bindingOperator(), BINDING_MODIFIER_PROPERTY, afterMod, null);

			// other modifiers:
			final ITrackedNodePosition position = rewrite.track(afterMod);
			this.addLinkedPosition(position, false, BINDINGKIND_KEY);
			LinkedProposalPositionGroup group=
				getLinkedProposalModel().getPositionGroup(BINDINGKIND_KEY, true);
			group.addProposal("before",  Images.getImage(CALLINBINDING_BEFORE_IMG), 13);  //$NON-NLS-1$
			group.addProposal("replace", Images.getImage(CALLINBINDING_REPLACE_IMG), 13); //$NON-NLS-1$
			group.addProposal("after",   Images.getImage(CALLINBINDING_AFTER_IMG), 13);   //$NON-NLS-1$
		}
		return true;	
	}
	
}
