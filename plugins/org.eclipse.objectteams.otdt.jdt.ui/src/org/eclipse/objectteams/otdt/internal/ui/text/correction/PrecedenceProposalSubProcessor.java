/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PrecedenceProposalSubProcessor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.text.correction;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodMappingBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrecedenceDeclaration;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedCorrectionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.objectteams.otdt.internal.ui.assist.LinkedModeAdaptor;
import org.eclipse.objectteams.otdt.ui.ImageConstants;
import org.eclipse.objectteams.otdt.ui.ImageManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.TextEdit;

@SuppressWarnings("restriction")
public class PrecedenceProposalSubProcessor {

	/** This linked proposal mimics a menu: support swapping of the elements of a precedence decl. 
	 */
	class SwapPrecedencesProposal extends LinkedProposalPositionGroup.Proposal {

		public SwapPrecedencesProposal() {
			super(Messages.format(CorrectionMessages.OTQuickfix_swapprecedenceorder_label, null), 
				  null, 0);
		}

		@Override
		public String getAdditionalProposalInfo() {
			return Messages.format(CorrectionMessages.OTQuickfix_swapprecedenceorder_description, null);
		}
				
		@Override
		public TextEdit computeEdits(int offset, 
									 LinkedPosition position, 
									 char trigger, 
									 int stateMask, 
									 LinkedModeModel model)
				throws CoreException 		
		{
			// leave linked mode, since this rewrite would require re-wiring which is not possible.
			try {
				if (!LinkedModeAdaptor.instance.leaveLinkedMode())
					return null; // broken assumption, continuing would probably mess up positions.
			} catch (NullPointerException npe) {
				return null; // no adaptor installed
			}
			
			// get a fresh AST:
			ASTParser p = ASTParser.newParser(AST.JLS3);
			p.setSource(cu);
			p.setResolveBindings(false);
			p.setFocalPosition(offset);
			CompilationUnit astCU = (CompilationUnit) p.createAST(null);
			ast = astCU.getAST();
			
			// find precedence declaration at 'offset'
			ASTNode node = NodeFinder.perform(astCU, offset, 1);
			PrecedenceDeclaration prec = (PrecedenceDeclaration)node;

			try
			{
				// rewrite:
				rewrite = ASTRewrite.create(ast);
				ListRewrite listRewrite = rewrite.getListRewrite(prec, PrecedenceDeclaration.ELEMENTS_PROPERTY);
				SimpleName n1 = (SimpleName)prec.elements().get(0);
				listRewrite.remove(n1, null);
				listRewrite.insertLast(ast.newSimpleName(n1.getIdentifier()), null);
				return rewrite.rewriteAST();
			} catch (Throwable e) {
				// cannot log or: e.printStackTrace();
			}
			return null;
		}
	}
	
	// Make one more method visible:
	class MyLinkedCorrectionProposal extends LinkedCorrectionProposal {
		public MyLinkedCorrectionProposal(String name, ICompilationUnit cu,
				ASTRewrite rewrite, int relevance, Image image) {
			super(name, cu, rewrite, relevance, image);
		}

		@Override
		public LinkedProposalModel getLinkedProposalModel() {
			return super.getLinkedProposalModel();
		}
	}

	final static String KEY_PRECEDENCE = "precedence"; //$NON-NLS-1$
	final static String KEY_LABEL1 = "label1"; //$NON-NLS-1$
	final static String KEY_LABEL2 = "label2"; //$NON-NLS-1$

	ICompilationUnit cu;
	ASTNode focusNode;
	AST ast;
	ASTRewrite rewrite;
	public PrecedenceProposalSubProcessor(ICompilationUnit cu, ASTNode focusNode) {
		this.cu = cu;
		this.focusNode = focusNode;
	}
	
	/**
	 * Add a proposal in add callin labels as needed and a binding based precedence declaration
	 * to the enclosing role.
	 * 
	 * @param roleType the enclosing role
	 * @param problemArguments elements 0 and 2 contain the names of two roles, 
	 * 			elements 1 and 3 contain names of callin bindings.
	 * @return
	 */
	IJavaCompletionProposal getAddBindingPrecedenceProposal(TypeDeclaration roleType, 
											 				String[] problemArguments) 
	{
		this.ast = focusNode.getAST();
		this.rewrite = ASTRewrite.create(ast);

		Name name1 = null;
		Name name2 = null;
		String callin1 = problemArguments[1];
		String callin2 = problemArguments[3];
		CallinMappingDeclaration mapping1 = findCallinMapping(roleType, callin1);
		CallinMappingDeclaration mapping2 = findCallinMapping(roleType, callin2);
		
		if (callin1.charAt(0) == '<') {
			if (mapping1 != null) { // TODO(SH):lookup across CUs? 
				callin1 = "callin1"; //$NON-NLS-1$
				name1 = this.ast.newSimpleName(callin1);
			}
		}	
		if (callin2.charAt(0) == '<') {
			if (mapping2 != null) { // TODO(SH):lookup across CUs? 
				callin2 = "callin2"; //$NON-NLS-1$
				name2 = this.ast.newSimpleName(callin2);
			}
		}
		if (callin1.charAt(0) == '<' || callin2.charAt(0) == '<')
			return null; // inserting names doesn't succeed
		
		if (!problemArguments[0].equals(problemArguments[2]))
			return null; // different roles, need to insert precedence into the team.
	
		LinkedCorrectionProposal proposal = getPrecedenceProposal(roleType, 
				Messages.format(CorrectionMessages.OTQuickfix_addbindingprecedence_description,
						new String[]{roleType.getName().getIdentifier()}), 
				callin1, callin2,
			 	name1 != null, name2 != null);

		// create new, editable labels (linked to their mentioning within the precedence declaration):
		if (name2 != null) { // add to front:
			this.rewrite.set(mapping2, CallinMappingDeclaration.NAME_PROPERTY, name2, null);
			proposal.addLinkedPosition(this.rewrite.track(name2), true, KEY_LABEL2);
		}
		if (name1 != null) { // even more to front:
			this.rewrite.set(mapping1, CallinMappingDeclaration.NAME_PROPERTY, name1, null);
			proposal.addLinkedPosition(this.rewrite.track(name1), true, KEY_LABEL1);
		}

		return proposal;
	}

	/**
	 * Create a proposal to add a binding-based precedence declaration to the enclosing team.
	 *  
	 * @param teamType the enclosing team.
	 * @param problemArguments elements 0 and 2 contain the names of two roles, 
	 * 			elements 1 and 3 contain names of callin bindings.
	 * @return
	 */
	IJavaCompletionProposal getAddBindingPrecedenceToTeamProposal(TypeDeclaration teamType, 
																  String[] problemArguments) 
	{

		String callin1 = problemArguments[1];
		String callin2 = problemArguments[3];
		if (callin1.charAt(0) == '<' || callin2.charAt(0) == '<')
			return null; // unnamed callin -- would need to insert names first.
		
		this.ast = focusNode.getAST();
		this.rewrite = ASTRewrite.create(ast);

		return getPrecedenceProposal(teamType, 
					Messages.format(CorrectionMessages.OTQuickfix_addbindingprecedence_description,
							new String[]{teamType.getName().getIdentifier()}), 
					Signature.getSimpleName(problemArguments[0])+"."+callin1, //$NON-NLS-1$
					Signature.getSimpleName(problemArguments[2])+"."+callin2, //$NON-NLS-1$
					false, false /* don't link labels */);
	}

	/**
	 * Create a proposal to add a class-based precedence declaration to the enclosing team.
	 * 
	 * @param teamType the enclosing team
	 * @param problemArguments elements 0 and 2 contain the names of two roles
	 * @return the new proposal
	 */
	IJavaCompletionProposal getAddRolePrecedenceToTeamProposal(TypeDeclaration teamType, 
															   String[] problemArguments) 
	{
		if (problemArguments[0].equals(problemArguments[2]))
			return null; // same role, can't use role to discriminate
	
		this.ast = focusNode.getAST();
		this.rewrite = ASTRewrite.create(ast);

		return getPrecedenceProposal(teamType, 
					Messages.format(CorrectionMessages.OTQuickfix_addroleprecedence_description,
							new String[]{teamType.getName().getIdentifier()}), 
					Signature.getSimpleName(problemArguments[0]), 
					Signature.getSimpleName(problemArguments[2]),
					false, false /* don't link labels */);
	}

	/**
	 * Create the edits for a new precedence declaration.
	 * Also add the option to swap the order.
	 * If 'useFullStringAlternatives' is true, the two alternatives
	 * are provided as two strings comprising a full precedence declaration each.
	 * Otherwise the keyword 'precedence' is used as an anchor for a "menu"
	 * showing one option: "> Swap Order". 
	 * 
	 * @param targetType
	 * @param label   display label for this proposal.
	 * @param callin1 name of one callin binding
	 * @param callin2 name of the other callin binding
	 * @param useFullStringAlternatives who to support setting the precedence order.
	 * @return the proposal
	 */
	@SuppressWarnings("unchecked")
	private LinkedCorrectionProposal getPrecedenceProposal(TypeDeclaration targetType,
			 									  		   String label,
			 									  		   String callin1,
			 									  		   String callin2,
			 									  		   boolean linkLabel1,
			 									  		   boolean linkLabel2) 
	{
		ChildListPropertyDescriptor precedenceProperty;
		if (targetType instanceof RoleTypeDeclaration)
			precedenceProperty = RoleTypeDeclaration.PRECEDENCE_PROPERTY;
		else
			precedenceProperty = TypeDeclaration.PRECEDENCE_PROPERTY;
		ListRewrite listRewrite = this.rewrite.getListRewrite(targetType, precedenceProperty);
		PrecedenceDeclaration newPrecedence = ast.newPrecedenceDeclaration();
		Name element1 = ast.newName(callin1);
		Name element2 = ast.newName(callin2);
		newPrecedence.elements().add(element1);
		newPrecedence.elements().add(element2);
		listRewrite.insertLast(newPrecedence, null);
		MyLinkedCorrectionProposal proposal = new MyLinkedCorrectionProposal(
						label,
						this.cu,
						this.rewrite,
						10,
						ImageManager.getSharedInstance().get(ImageConstants.CALLINBINDING_REPLACE_IMG));
	
		if (callin1 != null && callin2 != null) 
		{
			if (!linkLabel1 && !linkLabel2) 
			{
				// setup two alternatives (different order):
				proposal.addLinkedPosition(this.rewrite.track(newPrecedence), false, KEY_PRECEDENCE);
				proposal.addLinkedPositionProposal(KEY_PRECEDENCE, 
												   "precedence "+callin1+", "+callin2+";",//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												   null); 
				proposal.addLinkedPositionProposal(KEY_PRECEDENCE, 
												   "precedence "+callin2+", "+callin1+";", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												   null);
			}
			else 
			{
				// setup a "menu":
				final ITrackedNodePosition precedencePosition = this.rewrite.track(newPrecedence);
				ITrackedNodePosition swapPosition = new ITrackedNodePosition() {
						public int getLength() {
							return "precedence".length(); //$NON-NLS-1$
						}
						public int getStartPosition() {
							return precedencePosition.getStartPosition();
						}
					};
				proposal.addLinkedPosition(swapPosition, false, KEY_PRECEDENCE);
				LinkedProposalPositionGroup positionGroup=  proposal.getLinkedProposalModel().getPositionGroup(KEY_PRECEDENCE, true);
				positionGroup.addProposal(new StringLinkedModeProposal(
									CorrectionMessages.OTQuickfix_swapordermenu_label,
									CorrectionMessages.OTQuickfix_swapordermenu_description));
				positionGroup.addProposal(new SwapPrecedencesProposal());
	
				// prepare elements of this precedence to be linked with the (editable) callin labels.
				if (linkLabel1)
					proposal.addLinkedPosition(this.rewrite.track(element1), false, KEY_LABEL1);
				if (linkLabel2)
					proposal.addLinkedPosition(this.rewrite.track(element2), false, KEY_LABEL2);
			}
		}
		return proposal;
	}
	

	@SuppressWarnings("unchecked") // uses parameterless list of DOM AST.
	private static CallinMappingDeclaration findCallinMapping(TypeDeclaration roleType, String callinName)
	{
		boolean isAnonymous = callinName.charAt(0) == '<';
		List members = roleType.bodyDeclarations();
		if (members != null) { 
			for (Object object : members) {
				if (object instanceof CallinMappingDeclaration) {
					CallinMappingDeclaration mapping = (CallinMappingDeclaration)object;
					if (mapping.getName() != null) { 
						if (mapping.getName().getIdentifier().equals(callinName))
							return mapping;
					} else if (isAnonymous) {
						IMethodMappingBinding binding = mapping.resolveBinding();
						String currentName = binding.getName();
						if (currentName.startsWith(callinName)) // binding name comprises the full declaration
							return mapping;
					}
				}
			}
		}
		return null;
	}
}
