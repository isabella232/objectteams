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
package org.eclipse.objectteams.otdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccessSpec;
import org.eclipse.jdt.core.dom.GuardPredicateDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodMappingElement;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.objectteams.otdt.internal.ui.util.OTStubUtility;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.TextEditGroup;

/**
 * OT/J specific quick assists.
 * @since 0.7.0
 */
@SuppressWarnings("restriction")
public class QuickAssistProcessor implements IQuickAssistProcessor {

	public boolean hasAssists(IInvocationContext context) throws CoreException {
		ASTNode coveringNode= context.getCoveringNode();
		if (coveringNode != null)
			return hasToggleSignaturesOfMethodMappingProposal(coveringNode);
		return false;
	}

	private boolean hasToggleSignaturesOfMethodMappingProposal(ASTNode coveringNode) {
		while (coveringNode != null) {
			switch (coveringNode.getNodeType()) {
			case ASTNode.CALLIN_MAPPING_DECLARATION:
			case ASTNode.CALLOUT_MAPPING_DECLARATION:
				AbstractMethodMappingDeclaration methodMapping = (AbstractMethodMappingDeclaration)coveringNode;
				if (methodMapping.hasParameterMapping())
					return false;
				return true;
			case ASTNode.TYPE_DECLARATION:
			case ASTNode.ROLE_TYPE_DECLARATION:
			case ASTNode.COMPILATION_UNIT:
				return false;
			}
			coveringNode = coveringNode.getParent();
		}
		return false;
	}

	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException
	{
		ASTNode coveringNode= context.getCoveringNode();
		if (coveringNode != null) {
			ArrayList<ASTRewriteCorrectionProposal> resultingCollections= new ArrayList<ASTRewriteCorrectionProposal>();
			boolean noErrorsAtLocation= noErrorsAtLocation(locations);

			// quick assists that show up also if there is an error/warning
			getRemoveMethodMappingSignaturesProposal(context.getCompilationUnit(), coveringNode, resultingCollections);
			if (noErrorsAtLocation) {
				getAddMethodMappingSignaturesProposal(context.getCompilationUnit(), coveringNode, resultingCollections);
			}
			return resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
		}
		return null;
	}
	
	/* Proposals for removing signatures from method mappings. */
	private void getRemoveMethodMappingSignaturesProposal(ICompilationUnit cu,
														  ASTNode coveringNode, 
														  ArrayList<ASTRewriteCorrectionProposal> resultingCollections) 
	{
		AbstractMethodMappingDeclaration mapping = getMethodMapping(coveringNode);
		if (mapping != null && mapping.hasSignature()) {
			
			if (mapping.getNodeType() == ASTNode.CALLIN_MAPPING_DECLARATION)
				if (predicateUsesMethodSpecArgument((CallinMappingDeclaration) mapping))
					return; // don't propose to remove signatures, since an arg is used in the predicate
			
			AST ast= mapping.getAST();
			ASTRewrite rewrite= ASTRewrite.create(ast);

			// remove type parameters (only LHS):
			ListRewrite typeParamters = rewrite.getListRewrite(mapping.getRoleMappingElement(), MethodSpec.TYPE_PARAMETERS_PROPERTY);
			for (Object typeParamObject : ((MethodSpec)mapping.getRoleMappingElement()).typeParameters())
				typeParamters.remove((ASTNode) typeParamObject, null);
			
			// remove role signature:
			removeSignature(rewrite, mapping.getRoleMappingElement());
			
			// remove base signature(s):
			if (mapping instanceof CalloutMappingDeclaration) {
				removeSignature(rewrite, ((CalloutMappingDeclaration)mapping).getBaseMappingElement());									
			} else {
				for (Object baseElement : ((CallinMappingDeclaration)mapping).getBaseMappingElements()) {
					removeSignature(rewrite, (MethodMappingElement)baseElement);					
				}
			}
			
			String label= "Remove signatures from method binding";
			Image image= JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_REMOVE);
			ASTRewriteCorrectionProposal proposal= new ASTRewriteCorrectionProposal(label, cu, rewrite, 1, image);
			resultingCollections.add(proposal);
		}
	}

	// helper: detect whether a guard predicate uses an argument of the corresponding method spec.
	private boolean predicateUsesMethodSpecArgument(CallinMappingDeclaration callinMapping) 
	{
		GuardPredicateDeclaration predicate = callinMapping.getGuardPredicate();
		if (predicate == null)
			return false;
		final List<String> argNames = new ArrayList<String>();
		if (predicate.isBase())
			for (Object baseMethodObject : callinMapping.getBaseMappingElements()) 
				for (Object baseArgObj : ((MethodSpec)baseMethodObject).parameters())
					argNames.add(((SingleVariableDeclaration) baseArgObj).getName().getIdentifier());
		else
			for (Object roleArgObj : ((MethodSpec)callinMapping.getRoleMappingElement()).parameters())
				argNames.add(((SingleVariableDeclaration) roleArgObj).getName().getIdentifier());
		
		try {
			predicate.getExpression().accept(new ASTVisitor() {
				@Override
				public boolean visit(SimpleName node) {
					for (String argName : argNames)
						if (argName.equals(node.getIdentifier()))
							throw new RuntimeException();
					return false;
				}
			});
		} catch (RuntimeException re) {
			return true; 
		}
		return false;
	}
	
	// helper: remove the signature from one mapping element (method spec or field access spec).
	private void removeSignature(ASTRewrite rewrite, MethodMappingElement mappingElement) {
		rewrite.set(mappingElement, mappingElement.signatureProperty(), Boolean.FALSE, null);
		switch (mappingElement.getNodeType()) {
		case ASTNode.FIELD_ACCESS_SPEC:
			rewrite.set(mappingElement, FieldAccessSpec.FIELD_TYPE_PROPERTY, null, null);
			break;
		case ASTNode.METHOD_SPEC:
			// return type:
			rewrite.set(mappingElement, MethodSpec.RETURN_TYPE2_PROPERTY, null, null);
			// type parameters:
			ListRewrite listRewrite = rewrite.getListRewrite(mappingElement, MethodSpec.TYPE_PARAMETERS_PROPERTY);
			for (Object toRemove : ((MethodSpec)mappingElement).typeParameters())
				listRewrite.remove((ASTNode) toRemove, null);
			// parameters:
			listRewrite = rewrite.getListRewrite(mappingElement, MethodSpec.PARAMETERS_PROPERTY);
			for (Object toRemove : ((MethodSpec)mappingElement).parameters())
				listRewrite.remove((ASTNode) toRemove, null);
			break;
		}
	}

	/* Proposals for adding signatures to method mappings. */
	private void getAddMethodMappingSignaturesProposal(final ICompilationUnit cu,
			  										   ASTNode coveringNode,
												       ArrayList<ASTRewriteCorrectionProposal> resultingCollections) 
	{
		final AbstractMethodMappingDeclaration mapping = getMethodMapping(coveringNode);
		if (mapping != null && !mapping.hasSignature()) {
			final AST ast= mapping.getAST();
			final ASTRewrite rewrite= ASTRewrite.create(ast);
			
			String label= "Add signatures to method binding";
			Image image= JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_ADD);
			ASTRewriteCorrectionProposal proposal= new ASTRewriteCorrectionProposal(label, cu, rewrite, 1, image) {
				@Override
				protected ASTRewrite getRewrite() throws CoreException 
				{
					TextEditGroup editGroup = new TextEditGroup("Add Signatures");
					ImportRewrite imports = createImportRewrite((CompilationUnit) ASTNodes.getParent(mapping, ASTNode.COMPILATION_UNIT));
					// role method:
					IMethodBinding roleMethod = mapping.resolveBinding().getRoleMethod();
					MethodSpec newSpec = OTStubUtility.createMethodSpec(cu, rewrite, imports, roleMethod, true);
					convertTypeParameters(ast, roleMethod, newSpec);
					rewrite.set(mapping, mapping.getRoleElementProperty(), newSpec, editGroup);
					
					// base method(s):
					if (mapping.getNodeType() == ASTNode.CALLIN_MAPPING_DECLARATION)
						addSignatureToCallinBases(cu, rewrite, imports, (CallinMappingDeclaration) mapping, editGroup);
					else
						addSignatureToCalloutBase(cu, rewrite, imports, (CalloutMappingDeclaration) mapping, editGroup);
					return rewrite;
				}

				@SuppressWarnings("unchecked")
				private void convertTypeParameters(final AST ast, IMethodBinding roleMethod, MethodSpec destMethodSpec) 
				{
					for (ITypeBinding typeParameter : roleMethod.getTypeParameters()) {
						TypeParameter newTypeParameter = ast.newTypeParameter();
						newTypeParameter.setName(ast.newSimpleName(typeParameter.getName()));
						for (ITypeBinding typeBound : typeParameter.getTypeBounds())
							newTypeParameter.typeBounds().add(convertType(ast, typeBound));
						destMethodSpec.typeParameters().add(newTypeParameter);
					}
				}
			};
			resultingCollections.add(proposal);
		}		
	}

	// helper: add signatures to all base specs of a callin mapping:
	private void addSignatureToCallinBases(ICompilationUnit cu,
										   ASTRewrite rewrite, ImportRewrite imports,
										   CallinMappingDeclaration mapping, 
										   TextEditGroup editGroup) 
	{
		@SuppressWarnings("rawtypes")
		List oldBaseSpecs = mapping.getBaseMappingElements();
		ListRewrite baseMethods = rewrite.getListRewrite(mapping, CallinMappingDeclaration.BASE_MAPPING_ELEMENTS_PROPERTY);
		int i=0;
		for (IMethodBinding baseMethod : mapping.resolveBinding().getBaseMethods()) {
			try {
				MethodSpec newSpec = OTStubUtility.createMethodSpec(cu, rewrite, imports, baseMethod, true);
				baseMethods.replace((ASTNode) oldBaseSpecs.get(i), newSpec, editGroup);
			} catch (CoreException e) {
				OTDTUIPlugin.log(e);
			} finally {
				i++;
			}
		}		
	}

	// helper: add signature to a callout base spec (method or field)
	private void addSignatureToCalloutBase(ICompilationUnit cu,
										   ASTRewrite rewrite, 
										   ImportRewrite imports,
										   CalloutMappingDeclaration mapping, 
										   TextEditGroup editGroup) 
	{
		MethodMappingElement baseElement;
		try {
			if (mapping.bindingOperator().isCalloutToField()) {
				IVariableBinding baseField = ((FieldAccessSpec)mapping.getBaseMappingElement()).resolveBinding();
				baseElement = OTStubUtility.createFieldSpec(mapping.getAST(), imports, baseField, true);
			} else {
				IMethodBinding baseMethod = mapping.resolveBinding().getBaseMethods()[0];
				baseElement = OTStubUtility.createMethodSpec(cu, rewrite, imports, baseMethod, true);
			}
			rewrite.set(mapping, CalloutMappingDeclaration.BASE_MAPPING_ELEMENT_PROPERTY, baseElement, editGroup);
		} catch (CoreException e) {
			OTDTUIPlugin.log(e);
		}
	}

	AbstractMethodMappingDeclaration getMethodMapping (ASTNode coveringNode) {
		if (coveringNode instanceof AbstractMethodMappingDeclaration)
			return (AbstractMethodMappingDeclaration) coveringNode;
		return (AbstractMethodMappingDeclaration) ASTNodes.getParent(coveringNode, AbstractMethodMappingDeclaration.class);
	}

	@SuppressWarnings("unchecked")
	private Type convertType(AST ast, ITypeBinding typeBinding) {
		if (typeBinding.isTypeVariable())
			return ast.newSimpleType(ast.newSimpleName(typeBinding.getName()));
		String typeName = typeBinding.getErasure().getQualifiedName();
		Type type = (typeName.indexOf('.') > -1)
			? ast.newSimpleType(ast.newName(typeName))
			: ast.newSimpleType(ast.newSimpleName(typeName));
		ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
		if (typeArguments.length > 0) {
			ParameterizedType parameterizedType = ast.newParameterizedType(type);
			for (ITypeBinding bound : typeArguments)
				parameterizedType.typeArguments().add(convertType(ast, bound));
			return parameterizedType;
		}
		return type;
	}

	static boolean noErrorsAtLocation(IProblemLocation[] locations) {
		if (locations != null) {
			for (int i= 0; i < locations.length; i++) {
				IProblemLocation location= locations[i];
				if (location.isError()) {
					if (IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER.equals(location.getMarkerType())
							&& JavaCore.getOptionForConfigurableSeverity(location.getProblemId()) != null) {
						// continue (only drop out for severe (non-optional) errors)
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}
}
