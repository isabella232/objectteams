/*******************************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CorrectionAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 *     Technical University Berlin - Initial API and implementation
 *     IBM Corporation - copies of individual methods from bound base classes.
 *******************************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TSuperConstructorInvocation;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.proposals.NewMethodCorrectionProposal;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.swt.graphics.Image;

import base org.eclipse.jdt.internal.ui.text.correction.UnresolvedElementsSubProcessor;

/**
 * Individual method bodies are copied from their base version.
 * 
 * @author stephan
 * @since 1.1.2
 */
@SuppressWarnings("restriction")
public team class CorrectionAdaptor {
	protected class UnresolvedElementsSubProcessor playedBy UnresolvedElementsSubProcessor
	{
		/** This callin adjusts the compilation unit, when a new method should be inserted
		 *  into the outer type: which for a role file is NOT within the current CU.
		 */	
		@SuppressWarnings({ "decapsulation", "rawtypes" })
		void addNewMethodProposals(ICompilationUnit cu, CompilationUnit astRoot, Expression sender, List arguments, boolean isSuperInvocation, ASTNode invocationNode, String methodName, Collection proposals)
		<- replace void addNewMethodProposals(ICompilationUnit cu, CompilationUnit astRoot, Expression sender, List arguments, boolean isSuperInvocation, ASTNode invocationNode, String methodName, Collection proposals);
		
		@SuppressWarnings({ "basecall", "rawtypes", "unchecked" })
		callin static void addNewMethodProposals(ICompilationUnit cu, CompilationUnit astRoot, Expression sender, List arguments, boolean isSuperInvocation, ASTNode invocationNode, String methodName, Collection proposals) 
				throws JavaModelException 
		{
			// OT_COPY_PASTE:
			ITypeBinding nodeParentType= Bindings.getBindingOfParentType(invocationNode);
			ITypeBinding binding= null;
			if (sender != null) {
				binding= sender.resolveTypeBinding();
			} else {
				binding= nodeParentType;
				if (isSuperInvocation && binding != null) {
					binding= binding.getSuperclass();
				}
			}
			if (binding != null && binding.isFromSource()) {
				ITypeBinding senderDeclBinding= binding.getTypeDeclaration();

				ICompilationUnit targetCU= ASTResolving.findCompilationUnitForBinding(cu, astRoot, senderDeclBinding);
				if (targetCU != null) {
					String label;
					Image image;
					ITypeBinding[] parameterTypes= getParameterTypes(arguments);
					if (parameterTypes != null) {
						String sig= ASTResolving.getMethodSignature(methodName, parameterTypes, false);
		
						if (ASTResolving.isUseableTypeInContext(parameterTypes, senderDeclBinding, false)) {
							if (nodeParentType == senderDeclBinding) {
								label= Messages.format(CorrectionMessages.UnresolvedElementsSubProcessor_createmethod_description, sig);
								image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PRIVATE);
							} else {
								label= Messages.format(CorrectionMessages.UnresolvedElementsSubProcessor_createmethod_other_description, new Object[] { sig, BasicElementLabels.getJavaElementName(senderDeclBinding.getName()) } );
								image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
							}
							proposals.add(new NewMethodCorrectionProposal(label, targetCU, invocationNode, arguments, senderDeclBinding, 5, image));
						}
						if (senderDeclBinding.isNested() && cu.equals(targetCU) && sender == null && Bindings.findMethodInHierarchy(senderDeclBinding, methodName, (ITypeBinding[]) null) == null) { // no covering method
							ASTNode anonymDecl= astRoot.findDeclaringNode(senderDeclBinding);
							if (anonymDecl != null) {
								senderDeclBinding= Bindings.getBindingOfParentType(anonymDecl.getParent());
								if (!senderDeclBinding.isAnonymous() && ASTResolving.isUseableTypeInContext(parameterTypes, senderDeclBinding, false)) {
									String[] args= new String[] { sig, ASTResolving.getTypeSignature(senderDeclBinding) };
									label= Messages.format(CorrectionMessages.UnresolvedElementsSubProcessor_createmethod_other_description, args);
									image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PROTECTED);
//{ObjectTeams: the pay-load: when traveling out of a role file, search for the new targetCU:									
									if (binding.isRole() && astRoot.findDeclaringNode(senderDeclBinding) == null)
										targetCU= ASTResolving.findCompilationUnitForBinding(cu, astRoot, senderDeclBinding);
// SH}
									proposals.add(new NewMethodCorrectionProposal(label, targetCU, invocationNode, arguments, senderDeclBinding, 5, image));
								}
							}
						}
					}
				}
			}
		}

		@SuppressWarnings("rawtypes")
		void getConstructorProposals(IInvocationContext context, IProblemLocation problem, Collection proposals) 
			<- after void getConstructorProposals(IInvocationContext context, IProblemLocation problem, Collection proposals);
		
		/* Copied from base method and adapted for tsuper ctor calls. */
		@SuppressWarnings({"rawtypes","unchecked"})
		static void getConstructorProposals(IInvocationContext context, IProblemLocation problem, Collection proposals)
				throws CoreException
		{
			ICompilationUnit cu= context.getCompilationUnit();
	
			CompilationUnit astRoot= context.getASTRoot();
			ASTNode selectedNode= problem.getCoveringNode(astRoot);
			if (selectedNode == null) {
				return;
			}
	
			ITypeBinding targetBinding= null;
			List arguments= null;
/*{ObjectTeams: not used: orig:
			IMethodBinding recursiveConstructor= null;
   SH} */
	
			int type= selectedNode.getNodeType();
			if (type == ASTNode.TSUPER_CONSTRUCTOR_INVOCATION) {
				ITypeBinding typeBinding= Bindings.getBindingOfParentType(selectedNode);
				if (typeBinding != null && !typeBinding.isAnonymous()) {
//{ObjectTeams: different super class computation:
/* orig:
					targetBinding= typeBinding.getSuperclass();
  :giro */
					targetBinding= getTSuperClass(typeBinding);
// SH}
					arguments= ((TSuperConstructorInvocation) selectedNode).getArguments();
				}
// other cases already handled in the base method				
			}
			if (targetBinding == null) {
				return;
			}
			IMethodBinding[] methods= targetBinding.getDeclaredMethods();
			ArrayList similarElements= new ArrayList();
			for (int i= 0; i < methods.length; i++) {
				IMethodBinding curr= methods[i];
//{ObjectTeams: not using recursiveConstructor:
/* orig:
				if (curr.isConstructor() && recursiveConstructor != curr) {
  :giro */
				if (curr.isConstructor()) {
// SH}
					similarElements.add(curr); // similar elements can contain a implicit default constructor
				}
			}
	
			addParameterMissmatchProposals(context, problem, similarElements, selectedNode, arguments, proposals);
	
			if (targetBinding.isFromSource()) {
				ITypeBinding targetDecl= targetBinding.getTypeDeclaration();
	
				ICompilationUnit targetCU= ASTResolving.findCompilationUnitForBinding(cu, astRoot, targetDecl);
				if (targetCU != null) {
					String[] args= new String[] { ASTResolving.getMethodSignature( ASTResolving.getTypeSignature(targetDecl), getParameterTypes(arguments), false) };
					String label= Messages.format(CorrectionMessages.UnresolvedElementsSubProcessor_createconstructor_description, args);
					Image image= JavaElementImageProvider.getDecoratedImage(JavaPluginImages.DESC_MISC_PUBLIC, JavaElementImageDescriptor.CONSTRUCTOR, JavaElementImageProvider.SMALL_SIZE);
					proposals.add(new NewMethodCorrectionProposal(label, targetCU, selectedNode, arguments, targetDecl, 5, image));
				}
			}
		}
		
		static ITypeBinding getTSuperClass(ITypeBinding type) { // FIXME(SH): currently only first-level tsuper is used
			ITypeBinding[] superRoles = type.getSuperRoles();
			if (superRoles != null && superRoles.length > 0)
				return superRoles[0];
			return null;
		}
		
		
		@SuppressWarnings({ "rawtypes", "decapsulation" })
		ITypeBinding[] getParameterTypes(List args) -> ITypeBinding[] getParameterTypes(List args);

		@SuppressWarnings({ "rawtypes", "decapsulation" })
		void addParameterMissmatchProposals(IInvocationContext arg0, IProblemLocation arg1, List arg2, ASTNode arg3, List arg4, Collection arg5) 
			-> void addParameterMissmatchProposals(IInvocationContext arg0, IProblemLocation arg1, List arg2, ASTNode arg3, List arg4, Collection arg5);
	}
}
