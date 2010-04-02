/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: QuickFixProcessor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.objectteams.otdt.internal.ui.assist.OTQuickFixes;

/**
 * Processor for OT/J-specific quickfixes, which mostly delegates to specific sub-processors:
 * - ChangeModifierProposalSubProcessor
 * - PrecedenceProposalSubProcessor
 * 
 * Some structures borrow ideas from org.eclipse.jdt.internal.ui.text.corrections.QuickFixProcessor
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public class QuickFixProcessor implements IQuickFixProcessor {
	

	
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		switch (problemId) {
// import:		
		case IProblem.RegularlyImportedBaseclass:
		case IProblem.QualifiedReferenceToBaseclass:
// roles:			
		case IProblem.MissingTeamForRoleWithMembers:
		case IProblem.IllegalModifierForRole:
		case IProblem.ReducingRoleVisibility:
		case IProblem.AbstractRelevantRole:
		case IProblem.AbstractPotentiallyRelevantRole:
		case IProblem.MissingAnchorForRoleType:
		case IProblem.MissingOverrideAnnotationForRole:
// general method mappings:
		case IProblem.DifferentParamInCallinMethodSpec:
		case IProblem.UnresolvedCallinMethodSpec:
		case IProblem.UnresolvedCalloutMethodSpec:
// callout:
		case IProblem.RegularCalloutOverrides:
		case IProblem.AbstractMethodBoundAsOverride:
// callins:			
		case IProblem.ReplaceMappingToNonCallin:
		case IProblem.CallinMethodBoundNonReplace:
		case IProblem.RegularOverridesCallin:
		case IProblem.CallinOverridesRegular:
		case IProblem.CallinIncompatibleStatic:
		case IProblem.ReplaceCallinIncompatibleStatic:
		case IProblem.CallinReplaceKeyWordNotOptional:
		case IProblem.UnknownPrecedence:
		case IProblem.CovariantReturnRequiresTypeParameter:
// externalized roles and visibility:
		case IProblem.DeprecatedPathSyntax:
		case IProblem.AnchorNotFinal:
		case IProblem.AnchorPathNotFinal:
		case IProblem.ExternalizedCallToNonPublicConstructor:
		case IProblem.ExternalizedCallToNonPublicMethod:
		case IProblem.NotVisibleRoleMethod:
		case IProblem.TSubMethodReducesVisibility:
// inferred callouts:
		case IProblem.UsingCalloutToFieldForFieldRead:
		case IProblem.UsingCalloutToFieldForAssignment:
		case IProblem.UsingInferredCalloutForMessageSend:
		case IProblem.AddingInferredCalloutForInherited:
			return true;
// javadoc:
		case IProblem.JavadocMissingRoleTag:
			return true;
		}
		return false;
	}

	public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
													IProblemLocation[] locations)
			throws CoreException 
	{
		if (locations == null || locations.length == 0) {
			return null;
		}

		HashSet<Integer> handledProblems= new HashSet<Integer>(locations.length);
		ArrayList<IJavaCompletionProposal> resultingCollections= new ArrayList<IJavaCompletionProposal>();
		for (int i= 0; i < locations.length; i++) {
			IProblemLocation curr= locations[i];
			Integer id= new Integer(curr.getProblemId());
			if (handledProblems.add(id)) {
				process(context, curr, resultingCollections);
			}
		}
		return resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);

	}
	
	private void process(IInvocationContext context, IProblemLocation problem, Collection<IJavaCompletionProposal> proposals) throws CoreException {
		int id= problem.getProblemId();
		if (id == 0) { // no proposals for none-problem locations
			return;
		}
		ICompilationUnit cu= context.getCompilationUnit();
		ASTNode selectedNode= problem.getCoveringNode(context.getASTRoot());
		if (selectedNode == null) 
			return;
		
		ASTNode enclosingType= ASTResolving.findParentType(selectedNode);
		TypeDeclaration parentType= null;
		try { 
			if (enclosingType != null)
				parentType = (TypeDeclaration)ASTResolving.findParentType(enclosingType.getParent());
		} catch (ClassCastException cce) { /* just no success */ }
		CallinMappingDeclaration callinMapping;
		CalloutMappingDeclaration calloutMapping;
		IJavaCompletionProposal javaProposal= null;
		try {
			switch (id) {
// imports:		
			case IProblem.RegularlyImportedBaseclass:
	
				javaProposal = TypeProposalSubProcessor.getMakeImportBase(selectedNode, cu);
				break;
			case IProblem.QualifiedReferenceToBaseclass:
				javaProposal = TypeProposalSubProcessor.getImportBaseclass(selectedNode, cu);
				break;
// team modifiers:			
			case IProblem.MissingTeamForRoleWithMembers:
				if (parentType != null && parentType instanceof RoleTypeDeclaration)
					proposals.add(ChangeModifierProposalSubProcessor
										.getMakeTypeTeamProposal(cu, (RoleTypeDeclaration) parentType, 5));
				break;
			case IProblem.AbstractRelevantRole:
			case IProblem.AbstractPotentiallyRelevantRole:
				if (parentType != null)
					proposals.add(ChangeModifierProposalSubProcessor
										.getMakeTypeAbstractProposal(cu, parentType, 8));
				break;
// role modifiers:
			case IProblem.IllegalModifierForRole:
				if (enclosingType instanceof RoleTypeDeclaration) {
					RoleTypeDeclaration roleTypeDecl = (RoleTypeDeclaration)enclosingType;
					proposals.add(ChangeModifierProposalSubProcessor
										.getChangeRoleVisibilityProposal(cu, roleTypeDecl, Modifier.PROTECTED));
					proposals.add(ChangeModifierProposalSubProcessor
										.getChangeRoleVisibilityProposal(cu, roleTypeDecl, Modifier.PUBLIC));
				}
				break;
			case IProblem.ReducingRoleVisibility:
				if (enclosingType instanceof RoleTypeDeclaration) {
					RoleTypeDeclaration roleTypeDecl = (RoleTypeDeclaration)enclosingType;
					ITypeBinding[] tsupers = roleTypeDecl.resolveBinding().getSuperRoles();
					if (tsupers == null || tsupers.length == 0)
						break;
					int inheritedModifiers = tsupers[0].getModifiers() & ChangeModifierProposalSubProcessor.VISIBILITY_MASK;
					proposals.add(ChangeModifierProposalSubProcessor
										.getChangeRoleVisibilityProposal(cu, roleTypeDecl, inheritedModifiers));
				}
				break;
			case IProblem.MissingOverrideAnnotationForRole:
				// use original from ModifierCorrectionSubProcessor with adaptations:
				OTQuickFixes.instance().addOverrideAnnotationProposal(context, problem, proposals);
				break;
// general method mappings:
			case IProblem.DifferentParamInCallinMethodSpec:
			case IProblem.UnresolvedCallinMethodSpec:
			case IProblem.UnresolvedCalloutMethodSpec:
				MappingProposalSubProcessor.addUnresolvedMethodSpecProposals(selectedNode,
																			 (TypeDeclaration)enclosingType,
																			 context,
																			 problem,
																			 proposals);
				break;
// callout related:
			case IProblem.RegularCalloutOverrides:
				calloutMapping = (CalloutMappingDeclaration)getEnclosingMethodMapping(selectedNode);
				if (calloutMapping != null)
					proposals.add(ChangeModifierProposalSubProcessor.getChangeCalloutKindProposal(cu, calloutMapping, true/*toOverride*/));
				break;
			case IProblem.AbstractMethodBoundAsOverride:
				calloutMapping = (CalloutMappingDeclaration)getEnclosingMethodMapping(selectedNode);
				if (calloutMapping != null)
					proposals.add(ChangeModifierProposalSubProcessor.getChangeCalloutKindProposal(cu, calloutMapping, false/*toOverride*/));
				break;
// callin related:
			case IProblem.ReplaceMappingToNonCallin:
				callinMapping = (CallinMappingDeclaration)getEnclosingMethodMapping(selectedNode);
				if (callinMapping != null) {
					IMethodBinding roleMethod = ((MethodSpec)callinMapping.getRoleMappingElement()).resolveBinding();
					proposals.add(ChangeModifierProposalSubProcessor
										.getChangeMethodModifierProposal(context, null, roleMethod, 
																		 Modifier.OT_CALLIN, true));
					proposals.add(ChangeModifierProposalSubProcessor
										.getAddOrChangeCallinModifierProposal(cu, callinMapping));
				}
				break;
			case IProblem.CallinMethodBoundNonReplace:
				callinMapping = (CallinMappingDeclaration)getEnclosingMethodMapping(selectedNode);
				if (callinMapping != null) {
					IMethodBinding roleMethod = ((MethodSpec)callinMapping.getRoleMappingElement()).resolveBinding();
					proposals.add(ChangeModifierProposalSubProcessor
										.getChangeMethodModifierProposal(context, null, roleMethod, 
																		 Modifier.OT_CALLIN, false));
					proposals.add(ChangeModifierProposalSubProcessor
										.getAddOrChangeCallinModifierProposal(cu, callinMapping));
				}
				break;
			case IProblem.RegularOverridesCallin:
				if (selectedNode instanceof MethodDeclaration)
					proposals.add(ChangeModifierProposalSubProcessor
										.getChangeMethodModifierProposal(context, (MethodDeclaration)selectedNode, null, 
																  		 Modifier.OT_CALLIN, true));
				break;
			case IProblem.CallinOverridesRegular:
				if (selectedNode instanceof MethodDeclaration)
					proposals.add(ChangeModifierProposalSubProcessor
										.getChangeMethodModifierProposal(context, (MethodDeclaration)selectedNode, null, 
																  		 Modifier.OT_CALLIN, false));
				break;
			case IProblem.CallinIncompatibleStatic:
				callinMapping = (CallinMappingDeclaration)getEnclosingMethodMapping(selectedNode);
				if (callinMapping != null) {
					MethodSpec roleMSpec = (MethodSpec)callinMapping.getRoleMappingElement();
					proposals.add(ChangeModifierProposalSubProcessor
										.getChangeMethodModifierProposal(context, null, roleMSpec.resolveBinding(), 
																  		 Modifier.STATIC, true));
				}
				break;
			case IProblem.ReplaceCallinIncompatibleStatic:
				callinMapping = (CallinMappingDeclaration)getEnclosingMethodMapping(selectedNode);
				if (callinMapping != null) {
					MethodSpec roleMSpec = (MethodSpec)callinMapping.getRoleMappingElement();
					proposals.add(ChangeModifierProposalSubProcessor
										.getChangeMethodModifierProposal(context, null, roleMSpec.resolveBinding(),
																  	 	 Modifier.STATIC, false));
				}
				break;
			case IProblem.CallinReplaceKeyWordNotOptional:
				callinMapping = (CallinMappingDeclaration)getEnclosingMethodMapping(selectedNode);
				if (callinMapping != null) {
					proposals.add(ChangeModifierProposalSubProcessor
										.getAddOrChangeCallinModifierProposal(cu, callinMapping));
				}				
				break;
			case IProblem.UnknownPrecedence:
				if (enclosingType != null) {
					IJavaCompletionProposal proposal;
					PrecedenceProposalSubProcessor processor = new PrecedenceProposalSubProcessor(cu, enclosingType);
					proposal = processor.getAddBindingPrecedenceProposal((TypeDeclaration)enclosingType, problem.getProblemArguments());
					if (proposal != null)
						proposals.add(proposal);
					if (parentType != null) {
						proposal = processor.getAddBindingPrecedenceToTeamProposal(parentType, problem.getProblemArguments());
						if (proposal != null)
							proposals.add(proposal);
						proposal = processor.getAddRolePrecedenceToTeamProposal(parentType, problem.getProblemArguments());
						if (proposal != null)
							proposals.add(proposal);
					}
				}
				break;
			case IProblem.CovariantReturnRequiresTypeParameter:
				javaProposal=  MappingProposalSubProcessor.addTypeParameterToCallin(cu, selectedNode, (TypeDeclaration)enclosingType);
				break;
// externalized roles:
			case IProblem.DeprecatedPathSyntax:
				javaProposal= getMigratePathSyntaxProposal(cu, selectedNode);
				break;
			case IProblem.AnchorNotFinal:
			case IProblem.AnchorPathNotFinal:
				javaProposal= ChangeModifierProposalSubProcessor.changeAnchorToFinalProposal(cu, selectedNode);
				break;
			case IProblem.ExternalizedCallToNonPublicConstructor:
			case IProblem.ExternalizedCallToNonPublicMethod:
				// this processor is orig jdt, but it is adapted by OTQuickFixes/QuickFixCoreAdaptor:
				try {
					OTQuickFixes.publicRequested = true;
					ModifierCorrectionSubProcessor.addNonAccessibleReferenceProposal(context, problem, proposals, ModifierCorrectionSubProcessor.TO_VISIBLE, 10);
				} finally {
					OTQuickFixes.publicRequested = false;
				}
				break;
			case IProblem.NotVisibleRoleMethod:
				// this processor is orig jdt, but it is adapted by OTQuickFixes/QuickFixCoreAdaptor:
				ModifierCorrectionSubProcessor.addNonAccessibleReferenceProposal(context, problem, proposals, ModifierCorrectionSubProcessor.TO_VISIBLE, 10);
				break;
			case IProblem.TSubMethodReducesVisibility:
				// this processor is orig jdt, adaptations needed? 
				ModifierCorrectionSubProcessor.addChangeOverriddenModfierProposal(context, problem, proposals, ModifierCorrectionSubProcessor.TO_VISIBLE);
				break;
			case IProblem.MissingAnchorForRoleType:
				if (selectedNode instanceof VariableDeclarationStatement) {
					VariableDeclarationStatement variable = (VariableDeclarationStatement) selectedNode;
					selectedNode= variable.getType();
					if (selectedNode instanceof SimpleType) {
						SimpleType typeNode = (SimpleType) selectedNode;
						selectedNode= typeNode.getName();
					}
				}
				if (selectedNode instanceof Name) {
					Name typeName= (Name) selectedNode;
					ITypeBinding roleType= typeName.resolveTypeBinding();
					if (roleType == null) break;
					ITypeBinding teamType= roleType.getDeclaringClass();
					if (teamType == null) break;
					proposals.add(TypeProposalSubProcessor.changeTypeToAnchored(cu, 
																				roleType.getQualifiedName(), 
																				typeName,
																				teamType.getQualifiedName()));
				}
				break;
// inferred callout:
			case IProblem.UsingCalloutToFieldForAssignment:
				javaProposal = MappingProposalSubProcessor.getChangeAssignmentToCalloutCallProposal(
											cu, (TypeDeclaration) enclosingType, selectedNode);
				// add to proposals below
				break;
			case IProblem.UsingCalloutToFieldForFieldRead:
				javaProposal = MappingProposalSubProcessor.getChangeFieldReadToCalloutCallProposal(
											cu, (TypeDeclaration) enclosingType, selectedNode);
				// add to proposals below
				break;
			case IProblem.UsingInferredCalloutForMessageSend:
				javaProposal=  MappingProposalSubProcessor.getMaterializeInferredCalloutSelfCallProposal(
											cu, (TypeDeclaration)enclosingType, (MethodInvocation) selectedNode);
				// add to proposals below
				break;
			case IProblem.AddingInferredCalloutForInherited:
				javaProposal=  MappingProposalSubProcessor.getMaterializeInferredCalloutsInheritedProposal(
											cu, (TypeDeclaration)enclosingType, selectedNode);
				// add to proposals below
				break;
			// javadoc:
			case IProblem.JavadocMissingRoleTag:
				javaProposal= JavadocProposalSubProcessor.addRoleTag(cu, problem.getProblemArguments(), (TypeDeclaration)enclosingType);
				break;
			}
		} catch (ClassCastException cce) { /* could not find an expected node */ }
		if (javaProposal != null)
			proposals.add(javaProposal);
	}
	
	static AbstractMethodMappingDeclaration getEnclosingMethodMapping(ASTNode current) {
		while (current != null) {
			switch (current.getNodeType()) {
			case ASTNode.CALLIN_MAPPING_DECLARATION:
			case ASTNode.CALLOUT_MAPPING_DECLARATION:
				return (AbstractMethodMappingDeclaration)current;
			case ASTNode.ROLE_TYPE_DECLARATION:
			case ASTNode.TYPE_DECLARATION:
			case ASTNode.COMPILATION_UNIT:
				return null;
			}
			current = current.getParent();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private ASTRewriteCorrectionProposal getMigratePathSyntaxProposal(ICompilationUnit cu, 
																	  ASTNode selectedNode) 
	{
		if (selectedNode instanceof SingleVariableDeclaration)
			selectedNode = ((SingleVariableDeclaration)selectedNode).getType();
		ASTNode oldType = selectedNode; // saved for replacing be rewriter
		AST ast = oldType.getAST();
		// elements for the new type:
		String     prefixPath = null; 
		SimpleName typeName   = null;
		int        dimensions = 0;
		
		// strip off dims:
		if (selectedNode.getNodeType() == ASTNode.ARRAY_TYPE) {
			ArrayType type = (ArrayType)selectedNode;
			dimensions = type.getDimensions();
			oldType = type.getElementType();
		}
		// strip off simple type possibly wrapping qualified name: 
		if (oldType.getNodeType() == ASTNode.SIMPLE_TYPE) {
			SimpleType type = (SimpleType)oldType;
			oldType = type.getName();
		}
		// what remains should be a qualified something: 
		if (oldType.getNodeType() == ASTNode.QUALIFIED_NAME) {
			QualifiedName name = (QualifiedName)oldType;
			prefixPath = name.getQualifier().toString();
			typeName = name.getName();
		} else if (oldType.getNodeType() == ASTNode.QUALIFIED_TYPE) {
			QualifiedType type = (QualifiedType)oldType;
			prefixPath = type.getQualifier().toString();
			typeName = type.getName();
		}
		if (prefixPath != null) {// assemble:
			if (prefixPath.equals("base"))  //$NON-NLS-1$
				prefixPath = baseQualifier(selectedNode, typeName)+prefixPath;

			// T
			Type simpleType = ast.newSimpleType((Name)ASTNode.copySubtree(ast, typeName));
			// T<>
			ParameterizedType paramType = ast.newParameterizedType(simpleType);
			// T<@prefix.path>
			paramType.typeArguments().add(ast.newTypeAnchor(ast.newName(prefixPath)));
			// T<@prefix.path>[]..
			Type newType = (dimensions == 0) ? 
								paramType :
								ast.newArrayType(paramType, dimensions); 
			ASTRewrite rewrite = ASTRewrite.create(ast);
			rewrite.replace(selectedNode, newType, null);
			return new ASTRewriteCorrectionProposal(
					Messages.format(CorrectionMessages.OTQuickfix_migrateroletypesyntax_description, null),
					cu,
					rewrite,
					17, // TODO(SH): ;-)
					JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
		}
		return null;
	}

	private String baseQualifier(ASTNode selectedNode, 
									SimpleName typeName) 
	{
		// find enclosing role type:
		RoleTypeDeclaration enclType = null;
		ASTNode current = selectedNode;
		while (enclType == null && current != null) {
			current = current.getParent();
			if (current instanceof RoleTypeDeclaration)
				enclType = (RoleTypeDeclaration)current;
		}
		
		// find base class containing 'typeName':
		RoleTypeDeclaration currentType = enclType;
		int depth = 0;
		while(currentType != null) {
			// find 'typeName' in baseclass:
			ITypeBinding baseclass = currentType.getBaseClassType().resolveBinding();
			while (baseclass != null) {
				for (ITypeBinding member : baseclass.getDeclaredTypes()) {
					if (member.getName().equals(typeName.getIdentifier())) {
						if (depth > 0)
							return currentType.getName()+"."; //$NON-NLS-1$
						break; // success, but no need for a qualifier
					}
				}
				// loop up:
				baseclass = baseclass.getSuperclass();
			}
			if (currentType.getParent() instanceof RoleTypeDeclaration)
				// loop out:
				currentType = (RoleTypeDeclaration)currentType.getParent();
			else
				break; // no success, try anyway
			depth++;
		}
		return ""; //$NON-NLS-1$
	}
}
