/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ChangeModifierProposalSubProcessor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.text.correction;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodBindingOperator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ModifierChangeCorrectionProposal;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.ui.ImageConstants;
import org.eclipse.objectteams.otdt.ui.ImageManager;

/**
 * Compute OT-specific proposals for changing various modifiers.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public class ChangeModifierProposalSubProcessor 
{
	static final int VISIBILITY_MASK = (Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC);
	
	/**
	 * handle IProblem.MissingTeamForRoleWithMembers
	 */
	static ASTRewriteCorrectionProposal getMakeTypeTeamProposal(ICompilationUnit cu, RoleTypeDeclaration typeDeclaration, int relevance) {
		AST ast= typeDeclaration.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);
		rewrite.set(typeDeclaration, RoleTypeDeclaration.TEAM_PROPERTY, true, null);

		String label= Messages.format(CorrectionMessages.OTQuickfix_addteam_description, typeDeclaration.getName().getIdentifier());
		Image image= JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
		return new ASTRewriteCorrectionProposal(label, cu, rewrite, 5, image);
	}
	
	// taken from ModifierCorrectionSubProcessor, but also considering RoleTypeDeclarationn.
	static ASTRewriteCorrectionProposal getMakeTypeAbstractProposal(ICompilationUnit cu, TypeDeclaration typeDeclaration, int relevance) {
		AST ast= typeDeclaration.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);
		Modifier newModifier= ast.newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
		ChildListPropertyDescriptor modifiersProperty = typeDeclaration instanceof RoleTypeDeclaration ?
										RoleTypeDeclaration.MODIFIERS2_PROPERTY :
										TypeDeclaration.MODIFIERS2_PROPERTY;
		ListRewrite listRewrite = rewrite.getListRewrite(typeDeclaration, modifiersProperty);
		if (typeDeclaration.isTeam())
			listRewrite.insertAt(newModifier, findTeamModifierIndex(typeDeclaration), null);
		else
			listRewrite.insertLast(newModifier, null);

		String label= Messages.format(CorrectionMessages.OTQuickfix_addabstract_description, typeDeclaration.getName().getIdentifier());
		Image image= JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
		LinkedCorrectionProposal proposal= new LinkedCorrectionProposal(label, cu, rewrite, relevance, image);
		proposal.addLinkedPosition(rewrite.track(newModifier), true, "modifier"); //$NON-NLS-1$
		return proposal;
	}
	
	/* Helper */
	@SuppressWarnings("unchecked") // AST does not declare type parameters
	private static int findTeamModifierIndex(TypeDeclaration typeDeclaration) {
		List modifiers = typeDeclaration.modifiers();
		if (modifiers == null)
			throw new IllegalArgumentException("team without team modifier?"); //$NON-NLS-1$
		for (int i=0; i < modifiers.size(); i++) {
			if (((Modifier)modifiers.get(i)).getKeyword() == ModifierKeyword.TEAM_KEYWORD) 
				return i;
		}
		throw new IllegalArgumentException("team without team modifier?");		 //$NON-NLS-1$
	}

	static IJavaCompletionProposal getChangeRoleVisibilityProposal(
			ICompilationUnit cu, RoleTypeDeclaration roleType, int modifier) 
	{
		return new ModifierChangeCorrectionProposal(
						Messages.format(
							CorrectionMessages.OTQuickfix_changerolevisibility_description, 
							new String[] { roleType.getName().getIdentifier(), 
										   Modifier.isPublic(modifier)?"public":"protected" }), //$NON-NLS-1$ //$NON-NLS-2$ 
						cu, 
						roleType.resolveBinding(), 
						roleType, 
						modifier, 
						VISIBILITY_MASK, 
						8, 
						JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));		
	}

	static IJavaCompletionProposal getChangeMethodModifierProposal(IInvocationContext context, 
																   MethodDeclaration  methodDecl, 
																   IMethodBinding 	  method, 
																   int 				  modifier, 
																   boolean 			  isAdding) 
	{
		if (methodDecl == null)
			methodDecl = (MethodDeclaration)context.getASTRoot().findDeclaringNode(method);
		else if (method == null)
			method = methodDecl.resolveBinding();
		if (methodDecl==null || method == null)
			throw new InternalCompilerError("incomplete quickfix implementation"); //$NON-NLS-1$
		String label = null;
		String[] values = null;
		if (modifier == Modifier.OT_CALLIN) {
			label = isAdding? CorrectionMessages.OTQuickfix_addcallinmodifier_description:
                			  CorrectionMessages.OTQuickfix_removecallinmodifier_description;
			values = new String[] { method.getName() };
		} else {
			label = isAdding? CorrectionMessages.OTQuickfix_addmethodmodifier_description:
                		      CorrectionMessages.OTQuickfix_removemethodmodifier_description;
            values = new String[] { getModifierString(modifier), method.getName() };			
		}
			
		return new ModifierChangeCorrectionProposal(
						Messages.format(label, values), 
						context.getCompilationUnit(), 
						method, 
						methodDecl, 
						isAdding ? modifier : 0, 
						modifier, 
						8, 
						JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));		
	}

	final static String KEY_CALLIN_MODIFIER = "callin_modifier"; //$NON-NLS-1$
	
	public static IJavaCompletionProposal getAddOrChangeCallinModifierProposal(
								ICompilationUnit         cu,
								CallinMappingDeclaration callinMapping) 
	{
		AST ast = callinMapping.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);
		IMethodBinding roleMethod = ((MethodSpec)callinMapping.getRoleMappingElement()).resolveBinding();
		boolean replaceRequired = roleMethod != null && Modifier.isCallin(roleMethod.getModifiers());
		
		ModifierKeyword keyword = ModifierKeyword.REPLACE_KEYWORD;
		if (!replaceRequired) {
			if (roleMethod != null) 
				keyword = ModifierKeyword.AFTER_KEYWORD;
		}
		Modifier modifier = ast.newModifier(keyword);
		rewrite.set(callinMapping.bindingOperator(), MethodBindingOperator.BINDING_MODIFIER_PROPERTY, modifier, null);
		LinkedCorrectionProposal proposal = new LinkedCorrectionProposal(
				callinMapping.getCallinModifier() == Modifier.OT_MISSING_MODIFIER ?
						CorrectionMessages.OTQuickfix_AddCallinModifier :
						CorrectionMessages.OTQuickfix_ChangeCallinModifier ,
				cu,
				rewrite,
				10,
				ImageManager.getSharedInstance().get(ImageConstants.CALLINBINDING_REPLACE_IMG));
		if (!replaceRequired) {
			// setup two alternatives
			proposal.addLinkedPosition(rewrite.track(modifier), false, KEY_CALLIN_MODIFIER);
			proposal.addLinkedPositionProposal(KEY_CALLIN_MODIFIER, "after", null); //$NON-NLS-1$
			proposal.addLinkedPositionProposal(KEY_CALLIN_MODIFIER, "before", null); //$NON-NLS-1$
			if (roleMethod == null) 
				proposal.addLinkedPositionProposal(KEY_CALLIN_MODIFIER, "replace", null); //$NON-NLS-1$
		}
		return proposal;
	}

	public static IJavaCompletionProposal getChangeCalloutKindProposal(ICompilationUnit cu, CalloutMappingDeclaration calloutMapping, boolean toOverride) 
	{
		AST ast = calloutMapping.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		rewrite.set(calloutMapping.bindingOperator(),
					MethodBindingOperator.BINDING_KIND_PROPERTY,
					toOverride ? MethodBindingOperator.KIND_CALLOUT_OVERRIDE : MethodBindingOperator.KIND_CALLOUT, 
					null);
		return new ASTRewriteCorrectionProposal(
					toOverride ? CorrectionMessages.OTQuickfix_ChangeCalloutToOverride
							   : CorrectionMessages.OTQuickfix_ChangeCalloutToRegular,
					cu, rewrite, 10, ImageManager.getSharedInstance().get(
					ImageConstants.CALLOUTBINDING_IMG));
	}
	
	private static String getModifierString(int modifier) {
		switch (modifier) {
		case Modifier.PUBLIC:    return "public";    //$NON-NLS-1$
		case Modifier.PROTECTED: return "protected"; //$NON-NLS-1$
		case Modifier.PRIVATE:   return "private";   //$NON-NLS-1$
		case Modifier.STATIC:    return "static";    //$NON-NLS-1$
		}
		return "<unexpected modifier>"; //$NON-NLS-1$
	}

	/**
	 * Add a missing "final" modifier to an assumed type anchor.
	 * @param cu     where things happen 
	 * @param anchor anchor expression that lacks the final modifier.
	 * @return a proposal or null if not applicable.
	 */
	public static ModifierChangeCorrectionProposal changeAnchorToFinalProposal(ICompilationUnit cu, ASTNode anchor) 
	{
		VariableDeclarationFragment var = null;
		String anchorName = null;
		if (anchor instanceof SimpleName) {
			anchorName = ((SimpleName)anchor).getIdentifier();
			IBinding variable = ((SimpleName)anchor).resolveBinding();
			if (variable instanceof IVariableBinding) {
				ASTNode outer = anchor;
				while (!(outer instanceof CompilationUnit)) {
					outer = outer.getParent();
					if (outer == null)
						return null;
				}
				ASTNode declaringNode = ((CompilationUnit)outer).findDeclaringNode(variable);
				if (declaringNode instanceof VariableDeclarationFragment)
					var = (VariableDeclarationFragment)declaringNode;
			}
		}
		if (var != null)
			return new ModifierChangeCorrectionProposal(Messages.format(
															CorrectionMessages.OTQuickfix_makeanchorfinal_description,
															new String[]{anchorName}),
														cu,
														var.resolveBinding(),
														var,
														Modifier.FINAL,
														0,
														13, // TODO(SH)
														JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
		return null;
	}

}
