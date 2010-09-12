/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 GK Software AG
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
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.refactoring.adaptor;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodMappingElement;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.ParameterInfo;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.rename.TempOccurrenceAnalyzer;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.text.edits.TextEditGroup;

import base org.eclipse.jdt.internal.corext.refactoring.structure.ChangeSignatureProcessor;
import base org.eclipse.jdt.internal.corext.refactoring.structure.ChangeSignatureProcessor.OccurrenceUpdate;
import base org.eclipse.jdt.internal.corext.refactoring.structure.ChangeSignatureProcessor.DeclarationUpdate;

/**
 * OT/J adaptations for the "Change Signature" Refactoring.
 * 
 * @author stephan
 */
@SuppressWarnings({"restriction", "decapsulation", "rawtypes"}) // using several DOM lists which are not generified
public team class ChangeSignatureAdaptor {
	
	/** 
	 * Intercept situations where a signature change affects a MethodSpec,
	 * and add some more changes to compensate.
	 */
	protected team class Processor playedBy ChangeSignatureProcessor {

		List getAddedInfos() 			-> List getAddedInfos();
		List getDeletedInfos()			-> List getDeletedInfos();
		boolean isOrderSameAsInitial() 	-> boolean isOrderSameAsInitial();
		List getFParameterInfos() 		-> get List fParameterInfos;


		// FIXME(SH): parameter name result -> error duplicate local variable result
		OccurrenceUpdate createOccurrenceUpdate(ASTNode node,
				CompilationUnitRewrite cuRewrite, RefactoringStatus refResult)
		<- replace
		OccurrenceUpdate createOccurrenceUpdate(ASTNode node,
				CompilationUnitRewrite cuRewrite, RefactoringStatus refResult);

		@SuppressWarnings("basecall")
		callin OccurrenceUpdate createOccurrenceUpdate(ASTNode node,
				CompilationUnitRewrite cuRewrite, RefactoringStatus refResult) 
		{
			// base method cannot handle method spec, so check this first:
			if (node.getNodeType() == ASTNode.METHOD_SPEC) {
				analyzeImpact(node, refResult);
				activate(); // ensure nested role MethodSpecUpdate receives callin triggers
				return new MethodSpecUpdate((MethodSpec)node, cuRewrite, refResult);
				// don't bother to deactivate(), this direct team is temporary itself.
			}
			return base.createOccurrenceUpdate(node, cuRewrite, refResult);
		}
		
		/* Detect situations that are not fully supported by this refactoring and create INFOs for reporting. */
		void analyzeImpact(ASTNode node, RefactoringStatus refResult) {
			StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
			boolean isSrcSide = false;
			if (locationInParent == CalloutMappingDeclaration.ROLE_MAPPING_ELEMENT_PROPERTY) {
				isSrcSide = true;
			} else if (locationInParent == CallinMappingDeclaration.BASE_MAPPING_ELEMENTS_PROPERTY) {
				isSrcSide = true;
			}
			boolean hasDelections = !getDeletedInfos().isEmpty();
			boolean hasAdditions = !getAddedInfos().isEmpty();
			if (!isSrcSide && hasAdditions) {
				if (node.getParent().getNodeType() == ASTNode.CALLIN_MAPPING_DECLARATION)
					refResult.merge(RefactoringStatus.createInfoStatus("Adding arguments at the role side of a callin binding is not fully supported. Please manually update transitive references."));
				else
					refResult.merge(RefactoringStatus.createInfoStatus("Adding arguments at the base side of a callout binding is not fully supported. Please manually update transitive references."));
			} else if (isSrcSide && hasDelections) {
				if (node.getParent().getNodeType() == ASTNode.CALLIN_MAPPING_DECLARATION)
					refResult.merge(RefactoringStatus.createInfoStatus("Deleting arguments at the base side of a callin binding is not fully supported. Please manually update transitive references."));
				else
					refResult.merge(RefactoringStatus.createInfoStatus("Deleting arguments at the role side of a callout binding is not fully supported. Please manually update transitive references."));
			}
		}
		
		/* pure gateway */
		protected class OccurrenceUpdate playedBy OccurrenceUpdate 
			when (this.fMethodSpec != null) // otherwise role was created by lifting, should not intercept anything
		{
			MethodSpec fMethodSpec;
			void addParamterMappings() <- after void reshuffleElements(); // final method cannot be bound from subrole
			void addParamterMappings() {
				// do nothing here, overrides will take over
			}			
		}
		
		/** 
		 * The source side of a method mapping behaves similar to a method declaration.
		 * In contrast to our baseclass which operates on a MethodDeclaration (fMethodDecl),
		 * this role operates on a MethodSpec (fMethodSpec).
		 * In addition to a few adaptations made necessary by the above difference,
		 * this role also intercepts "reshuffleElements()" in order to add parameter
		 * mappings that may absorb signature reordering. 
		 */
		@SuppressWarnings("basecall") // most callins truely replace the base behaviour
		protected class MethodSpecUpdate extends OccurrenceUpdate playedBy DeclarationUpdate
		{
			
			CompilationUnitRewrite getCompilationUnitRewrite() -> CompilationUnitRewrite getCompilationUnitRewrite();

			ASTRewrite getASTRewrite() -> ASTRewrite getASTRewrite();

			
			protected MethodSpecUpdate(MethodSpec node, CompilationUnitRewrite cuRewrite, RefactoringStatus refResult)
			{
				base(null/*MethodDeclaration*/, cuRewrite, refResult);
				this.fMethodSpec = node;
			}

			ListRewrite getParamgumentsRewrite() <- replace ListRewrite getParamgumentsRewrite();

			callin ListRewrite getParamgumentsRewrite() {
				return getASTRewrite().getListRewrite(fMethodSpec, MethodSpec.PARAMETERS_PROPERTY);
			}

			SimpleName getMethodNameNode() <- replace SimpleName getMethodNameNode();

			callin SimpleName getMethodNameNode() {
				return this.fMethodSpec.getName();
			}

			void changeJavadocTags() <- replace void changeJavadocTags();

			callin void changeJavadocTags() {
				// do nothing, no javadoc tags defined for method specs
			}

			void addDelegate() <- replace void addDelegate();

			callin void addDelegate() {
				// no delegates for method spec
			}

			void checkIfDeletedParametersUsed() <- replace void checkIfDeletedParametersUsed();
			
			@SuppressWarnings("inferredcallout") // almost verbatim copy from base
			callin void checkIfDeletedParametersUsed() {
				for (Iterator iter= getDeletedInfos().iterator(); iter.hasNext();) {
					ParameterInfo info= (ParameterInfo) iter.next();
					SingleVariableDeclaration paramDecl= (SingleVariableDeclaration) fMethodSpec.parameters().get(info.getOldIndex());
					TempOccurrenceAnalyzer analyzer= new TempOccurrenceAnalyzer(paramDecl, false);
					analyzer.perform();
					SimpleName[] paramRefs= analyzer.getReferenceNodes();

					if (paramRefs.length > 0){
						RefactoringStatusContext context= JavaStatusContext.create(fCuRewrite.getCu(), paramRefs[0]);
						String typeName= getFullTypeName(fMethodSpec);
						Object[] keys= new String[]{ BasicElementLabels.getJavaElementName(paramDecl.getName().getIdentifier()),
								BasicElementLabels.getJavaElementName(fMethodSpec.getName().getIdentifier()),
								BasicElementLabels.getJavaElementName(typeName)};
						String msg= Messages.format(RefactoringCoreMessages.ChangeSignatureRefactoring_parameter_used, keys);
						fResult.addError(msg, context);
					}
				}
			}
			
			/* similar but unrelated to DeclarationUpdate.getFullTypeName(MethodDeclaration). */
			private String getFullTypeName(MethodSpec decl) {
				ASTNode node= decl;
				while (node != null) {
					node= node.getParent();
					if (node instanceof AbstractTypeDeclaration) {
						return ((AbstractTypeDeclaration) node).getName().getIdentifier();
					}
				}
				return "MISSING"; //$NON-NLS-1$
			}
			
			@SuppressWarnings("inferredcallout")
			@Override
			void addParamterMappings() {
				if (isOrderSameAsInitial())
					return;
				AbstractMethodMappingDeclaration mapping = (AbstractMethodMappingDeclaration) this.fMethodSpec.getParent();
				if (mapping.hasParameterMapping()) {
					fResult.merge(RefactoringStatus.createWarningStatus("Cannot update existing parameter mapping"));
					return;
				}
				// collect these pieces of information:
				List otherSideArguments;	// arguments of the mapping side that is not affected by this refactoring
				boolean isRoleSide;			// whether the affect method spec is on the role side
				String parMapDirection;		// either of "->" or "<-"
				ChildListPropertyDescriptor parMapProp; // descriptor denoting the parameter mappings property within 'mapping'
				if (mapping.getNodeType() == ASTNode.CALLIN_MAPPING_DECLARATION) {
					parMapProp = CallinMappingDeclaration.PARAMETER_MAPPINGS_PROPERTY;
					isRoleSide = this.fMethodSpec.getLocationInParent() == CallinMappingDeclaration.ROLE_MAPPING_ELEMENT_PROPERTY;
					if (isRoleSide) {
						List baseMappingElements = ((CallinMappingDeclaration)mapping).getBaseMappingElements();
						if (baseMappingElements.size() > 1) {
							fResult.merge(RefactoringStatus.createWarningStatus("Need to create parameter mapping, which however is not yet implemented for callin to multiple bases"));
							return;
						}
						otherSideArguments = ((MethodSpec)baseMappingElements.get(0)).parameters();
					} else {
						otherSideArguments = ((MethodSpec)mapping.getRoleMappingElement()).parameters();
					}
					parMapDirection = "<-"; //$NON-NLS-1$
				} else {
					parMapProp = CalloutMappingDeclaration.PARAMETER_MAPPINGS_PROPERTY;
					isRoleSide = this.fMethodSpec.getLocationInParent() == CalloutMappingDeclaration.ROLE_MAPPING_ELEMENT_PROPERTY;
					if (isRoleSide) {
						MethodMappingElement baseMappingElement = ((CalloutMappingDeclaration)mapping).getBaseMappingElement();
						if (baseMappingElement.getNodeType() == ASTNode.FIELD_ACCESS_SPEC) {
							fResult.merge(RefactoringStatus.createWarningStatus("Need to create parameter mapping, which however is not yet implemented for callout-to-field"));
							return;
						}
						otherSideArguments = ((MethodSpec)baseMappingElement).parameters();
					} else {
						otherSideArguments = ((MethodSpec)mapping.getRoleMappingElement()).parameters();
					}
					parMapDirection = "->"; //$NON-NLS-1$
				}
				maybeAddParamMappings(mapping, parMapProp, otherSideArguments, getFParameterInfos(), parMapDirection);
			}
			
			void maybeAddParamMappings(AbstractMethodMappingDeclaration mapping,
									   ChildListPropertyDescriptor parMapProp,
									   List otherSideArguments,
									   List parameterInfos,
									   String parMapDirection) 
			{
				checkRelevance: {
					for (int i= 0; i < parameterInfos.size(); i++) {
						int oldIndex= ((ParameterInfo) parameterInfos.get(i)).getOldIndex();
						if (   oldIndex > -1 
							&& oldIndex < otherSideArguments.size()
							&& oldIndex != i)
						{
							break checkRelevance;
						}
					}
					return; // no change relevant for a parameter mapping
				}
				AST ast = this.fMethodSpec.getAST();
				TextEditGroup editGroup = new TextEditGroup("parametermappings"); //$NON-NLS-1$
				ListRewrite parMapRewrite = getASTRewrite().getListRewrite(mapping, parMapProp);
				for (int i= 0; i < parameterInfos.size(); i++) {
					ParameterInfo info= (ParameterInfo) parameterInfos.get(i);
					int oldIndex= info.getOldIndex();
					if (oldIndex > -1 && oldIndex < otherSideArguments.size()) {
						SingleVariableDeclaration arg = (SingleVariableDeclaration) otherSideArguments.get(oldIndex);
						ParameterMapping parMap = ast.newParameterMapping();
						parMap.setIdentifier((SimpleName) ASTNode.copySubtree(ast, arg.getName()));
						parMap.setExpression(ast.newSimpleName(info.getNewName()));
						parMap.setDirection(parMapDirection);
						parMapRewrite.insertLast(parMap, editGroup);
					}
				}				
			}
		}
	}	
}
