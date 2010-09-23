/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007, 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CreateMethodMappingCompletionProposal.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.ui.assist.CompletionAdaptor;

import static org.eclipse.objectteams.otdt.ui.ImageConstants.CALLINBINDING_AFTER_IMG;
import static org.eclipse.objectteams.otdt.ui.ImageConstants.CALLINBINDING_BEFORE_IMG;
import static org.eclipse.objectteams.otdt.ui.ImageConstants.CALLINBINDING_REPLACE_IMG;
import static org.eclipse.objectteams.otdt.ui.ImageConstants.CALLOUTBINDING_IMG;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodBindingOperator;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.objectteams.otdt.internal.ui.util.Images;
import org.eclipse.objectteams.otdt.internal.ui.util.OTStubUtility;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/** 
 * [unbound role: extends rather than playedBy.]
 * 
 * Rewrite-based completion for creating full callout mappings, 
 * which afterwards use linked mode editing for choosing the method binding kind.
 * Also the role method name is offered for editing in linked mode.
 *
 * @author stephan
 * @since 1.1.2
 */ 
@SuppressWarnings("restriction")
protected team class CreateMethodMappingCompletionProposal extends MethodMappingCompletionProposal 
{

	/* gateway to private final base class. */
	@SuppressWarnings("decapsulation")
	protected class MyJavaLinkedModeProposal playedBy JavaLinkedModeProposal  {

		public MyJavaLinkedModeProposal(ICompilationUnit unit, ITypeBinding typeProposal, int relevance) {
			base(unit, typeProposal, relevance);
		}

		TextEdit computeEdits(int offset, LinkedPosition position, char trigger, int stateMask, LinkedModeModel model) 
		-> TextEdit computeEdits(int offset, LinkedPosition position, char trigger, int stateMask, LinkedModeModel model);
	}



	boolean fIsOverride = false;
	boolean fIsOnlyCallin = false; 
	
	protected CreateMethodMappingCompletionProposal(IJavaProject 	   jProject, 
												    ICompilationUnit   cu,
												    CompletionProposal proposal,
												    String[]           paramTypes,
												    boolean 		   isOverride,
												    boolean 		   isOnlyCallin,
												    int                length,
												    String             displayName, 
												    Image              image)
	{
		super(jProject, cu, proposal, paramTypes, length, displayName, image);
		this.fIsOverride= isOverride;
		this.fIsOnlyCallin = isOnlyCallin;
	}
	protected CreateMethodMappingCompletionProposal(IJavaProject 	   jProject, 
									    ICompilationUnit   cu,  
									    CompletionProposal proposal,
									    int                length,
									    String             displayName,
									    Image              image) 
	{
		super(jProject, cu, proposal, length, displayName, image);
	}
	
	/** Create a rewrite that additionally removes typed fragment if needed. 
     *  That fragment will not be represented by an AST-node, that could be removed.
     */
	ASTRewrite createRewrite(AST ast) 
	{
		if (fLength == 0)
			return ASTRewrite.create(ast);
		
		// the typed prefix will have to be deleted:
		final TextEdit delete= new DeleteEdit(fReplaceStart, fLength);
		
		// return a custom rewrite that additionally deletes typed fragment
		return new ASTRewrite(ast) {
			@Override
			public TextEdit rewriteAST() 
					throws JavaModelException, IllegalArgumentException 
			{
				TextEdit edits = super.rewriteAST();
				if (edits instanceof MultiTextEdit) {
					MultiTextEdit multi = (MultiTextEdit) edits;
					multi.addChild(delete);
				}
				return edits;
			}
		};
	}
	
	/** Overridable, see CalloutToFieldCompletionProposal.
	 *  At least baseBinding must be set, roleBinding is optional.
	 */
	boolean setupRewrite(ICompilationUnit                 iCU, 
			          ASTRewrite       				   rewrite, 
			          ImportRewrite   			       importRewrite,
			          ITypeBinding					   roleBinding,
			          ITypeBinding					   baseBinding,
			          ASTNode                          type,
			          AbstractMethodMappingDeclaration partialMapping,
			          ChildListPropertyDescriptor      bodyProperty) 
			throws CoreException
	{
		// find base method:
		IMethodBinding method= findMethod(baseBinding, fMethodName, fParamTypes);
		if (method == null)
			return false;
		
		CodeGenerationSettings settings= JavaPreferencesSettings.getCodeGenerationSettings(fJavaProject);
		// create callout:
		AbstractMethodMappingDeclaration stub= this.fIsOnlyCallin 
				? OTStubUtility.createCallin(iCU, rewrite, importRewrite,
						 				    method, baseBinding.getName(), ModifierKeyword.BEFORE_KEYWORD, settings)
				: OTStubUtility.createCallout(iCU, rewrite, importRewrite,
											 method, baseBinding.getName(), settings);
		if (stub != null) {
			insertStub(rewrite, type, bodyProperty, fReplaceStart, stub);
			
			MethodSpec roleMethodSpec = (MethodSpec)stub.getRoleMappingElement();
			
			// return type:
			ITrackedNodePosition returnTypePosition = null;
			ITypeBinding returnType = method.getReturnType();
			if (!(returnType.isPrimitive() && "void".equals(returnType.getName()))) { //$NON-NLS-1$
				returnTypePosition = rewrite.track(roleMethodSpec.getReturnType2());
				addLinkedPosition(returnTypePosition, true, ROLEMETHODRETURN_KEY);
				LinkedProposalPositionGroup group1 = getLinkedProposalModel().getPositionGroup(ROLEMETHODRETURN_KEY, true);
				group1.addProposal(new MyJavaLinkedModeProposal(iCU, method.getReturnType(), 13));
				group1.addProposal("void", null, 13);  //$NON-NLS-1$
			}
			
			// role method name:
			addLinkedPosition(rewrite.track(roleMethodSpec.getName()), false, ROLEMETHODNAME_KEY);
			
			// argument lifting?
			if (roleBinding != null)
				addLiftingProposals(roleBinding, method, stub, rewrite);
			
			// binding operator:
			addLinkedPosition(rewrite.track(stub.bindingOperator()), false, BINDINGKIND_KEY);
			LinkedProposalPositionGroup group2= getLinkedProposalModel().getPositionGroup(BINDINGKIND_KEY, true);
			if (!this.fIsOnlyCallin) {
				String calloutToken = "->"; //$NON-NLS-1$
				if (this.fIsOverride) {
					calloutToken = "=>"; //$NON-NLS-1$
					stub.bindingOperator().setBindingKind(MethodBindingOperator.KIND_CALLOUT_OVERRIDE);
				}
				group2.addProposal(calloutToken, Images.getImage(CALLOUTBINDING_IMG), 13);
			}
			group2.addProposal(makeBeforeAfterBindingProposal("<- before", Images.getImage(CALLINBINDING_BEFORE_IMG), returnTypePosition));  //$NON-NLS-1$
			group2.addProposal("<- replace", Images.getImage(CALLINBINDING_REPLACE_IMG), 13); //$NON-NLS-1$
			group2.addProposal(makeBeforeAfterBindingProposal("<- after",  Images.getImage(CALLINBINDING_AFTER_IMG), returnTypePosition));   //$NON-NLS-1$
		}
		return true;	
	}
	/** Create a method-binding proposal that, when applied, will change the role-returntype to "void": */
	Proposal makeBeforeAfterBindingProposal(String displayString, Image image, final ITrackedNodePosition returnTypePosition) {
		return new Proposal(displayString, image, 13) {
			@Override
			public TextEdit computeEdits(int offset, LinkedPosition position, char trigger, int stateMask, LinkedModeModel model)
					throws CoreException 
			{
				MultiTextEdit edits = new MultiTextEdit();
				if (returnTypePosition != null)
					edits.addChild(new ReplaceEdit(returnTypePosition.getStartPosition(), returnTypePosition.getLength(), "void")); //$NON-NLS-1$
				edits.addChild(super.computeEdits(offset, position, trigger, stateMask, model));
				return edits;
			}
		};
	}
	
	/** Check if any parameters or the return type are candidates for lifting/lowering. */
	@SuppressWarnings("rawtypes")
	private void addLiftingProposals(ITypeBinding roleTypeBinding, IMethodBinding methodBinding,
			AbstractMethodMappingDeclaration stub, ASTRewrite rewrite) 
	{
		ITypeBinding[] roles= roleTypeBinding.getDeclaringClass().getDeclaredTypes();
		MethodSpec roleSpec= (MethodSpec)stub.getRoleMappingElement();
		List params= roleSpec.parameters();
		ITypeBinding[] paramTypes = methodBinding.getParameterTypes();
		for (int i= 0; i<params.size(); i++)
			addLiftingProposalGroup(rewrite, ROLEPARAM_KEY+i, roles, 
							        ((SingleVariableDeclaration)params.get(i)).getType(), paramTypes[i]);
		addLiftingProposalGroup(rewrite, ROLEPARAM_KEY+"return", roles,  //$NON-NLS-1$
									roleSpec.getReturnType2(), methodBinding.getReturnType());
	}
	/**
	 * check whether a given type is played by a role from a given array and create a proposal group containing base and role type. 
	 * @param rewrite
	 * @param positionGroupID 
	 * @param roles       available roles in the enclosing team
	 * @param type        AST node to investigate
	 * @param typeBinding type binding of AST node to investigate
	 */
	void addLiftingProposalGroup(ASTRewrite rewrite, String positionGroupID, ITypeBinding[] roles, Type type, ITypeBinding typeBinding)
	{
		for (ITypeBinding roleBinding : roles) {
			if (roleBinding.isSynthRoleIfc()) continue; // synth ifcs would otherwise cause dupes
			if (typeBinding.equals(roleBinding.getBaseClass())) {
				ITrackedNodePosition argTypePos= rewrite.track(type);
				addLinkedPosition(argTypePos, true, positionGroupID);
				LinkedProposalPositionGroup group=
					getLinkedProposalModel().getPositionGroup(positionGroupID, true);
				group.addProposal(type.toString(), null, 13);
				group.addProposal(roleBinding.getName(), null, 13);
				break;
			}
		}		
	}
}