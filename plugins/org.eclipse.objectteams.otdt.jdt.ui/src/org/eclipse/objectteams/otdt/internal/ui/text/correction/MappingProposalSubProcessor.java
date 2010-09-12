/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MappingProposalSubProcessor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.text.correction;

import static org.eclipse.jdt.core.dom.TypeDeclaration.BODY_DECLARATIONS_PROPERTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldAccessSpec;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IMethodMappingBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.rewrite.ASTNodeCreator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.UnresolvedElementsSubProcessor;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.NewMethodCorrectionProposal;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.compiler.InferenceKind;
import org.eclipse.objectteams.otdt.core.compiler.OTNameUtils;
import org.eclipse.objectteams.otdt.core.compiler.Pair;
import org.eclipse.objectteams.otdt.internal.ui.assist.OTQuickFixes;

/**
 * Quick-fix proposals for method mappings.
 * 
 * @author stephan
 * @since 1.1.2
 */
@SuppressWarnings("restriction")
public class MappingProposalSubProcessor {

	public static final String FAKETHIS = "$fakethis$"; //$NON-NLS-1$

	@SuppressWarnings("unchecked") // arguments.add()
	static IJavaCompletionProposal getChangeAssignmentToCalloutCallProposal(
				ICompilationUnit cu, 
				TypeDeclaration  enclosingType,
				ASTNode 	     selectedNode)
	{
		// find (fieldName x useThis)
		Pair<SimpleName,Boolean> answer = findFieldName(selectedNode);
		if (answer == null) return null;
		char[] accessorName = OTNameUtils.accessorName(/*isSetter*/true, 
													   answer.first.getIdentifier().toCharArray());
		
		// find parent-assignment
		ASTNode parent = selectedNode.getParent();
		if (parent.getNodeType() != ASTNode.ASSIGNMENT)
			return null;
		Assignment assignment = (Assignment)parent;
		
		// start rewriting
		AST ast = selectedNode.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);
		
		// create the call:
		MethodInvocation send = ast.newMethodInvocation();
		if (answer.second)
			send.setExpression(ast.newThisExpression());
		send.setName(ast.newSimpleName(String.valueOf(accessorName)));
		send.arguments().add(ASTNode.copySubtree(ast, assignment.getRightHandSide()));
		
		makeReplacement(rewrite, assignment, send);
		
		// try to also materialize the inferred callout (using the same rewrite as to combine modifications):
		IJavaCompletionProposal proposal = getMaterializeInferredCalloutToFieldProposal(cu, rewrite, selectedNode, enclosingType, InferenceKind.FIELDSET);
		if (proposal != null)
			return proposal;
		
		// not materializing, propose the replacement, only:
		return new ASTRewriteCorrectionProposal(
				CorrectionMessages.OTQuickfix_change_assignment_to_settercall,
				cu,
				rewrite,
				10000, // TODO(SH)
				JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
	}
	
	static IJavaCompletionProposal getChangeFieldReadToCalloutCallProposal(
				ICompilationUnit cu, 
				TypeDeclaration  enclosingType,
				ASTNode 		 selectedNode) 
	{
		// find (fieldName x useThis)
		Pair<SimpleName,Boolean> answer = findFieldName(selectedNode);
		if (answer == null) return null;
		char[] accessorName = OTNameUtils.accessorName(/*isSetter*/false, 
													   answer.first.getIdentifier().toCharArray());

		// start rewriting
		AST ast = selectedNode.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);
		
		// create the call:
		MethodInvocation send = ast.newMethodInvocation();
		if (answer.second)
			send.setExpression(ast.newThisExpression());
		send.setName(ast.newSimpleName(String.valueOf(accessorName)));
		
		makeReplacement(rewrite, selectedNode, send);
		
		// try to also materialize the inferred callout (using the same rewrite as to combine modifications):
		IJavaCompletionProposal proposal = getMaterializeInferredCalloutToFieldProposal(cu, rewrite, selectedNode, enclosingType, InferenceKind.FIELDGET);
		if (proposal != null)
			return proposal;
		
		// not materializing, propose the replacement, only:
		return new ASTRewriteCorrectionProposal(
				CorrectionMessages.OTQuickfix_change_fieldaccess_to_gettercall,
				cu,
				rewrite,
				10000, // TODO(SH)
				JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
	}
	
	// answer: fieldName x useThis
	private static Pair<SimpleName,Boolean> findFieldName(ASTNode selectedNode) {
		switch (selectedNode.getNodeType()) {
		case  ASTNode.SIMPLE_NAME: 
			return new Pair<SimpleName,Boolean>((SimpleName)selectedNode, false);
		case ASTNode.FIELD_ACCESS:
			FieldAccess access = (FieldAccess) selectedNode;
			if (access.getExpression().getNodeType() != ASTNode.THIS_EXPRESSION)
				return null;
			return new Pair<SimpleName,Boolean>(access.getName(), true);
		default:
			return null;
		}
	}
	// add a replacement oldNode -> newNode to rewrite
	private static void makeReplacement(ASTRewrite rewrite, ASTNode oldNode, ASTNode newNode) 
	{
		ASTNode parent= oldNode.getParent();
		StructuralPropertyDescriptor desc= oldNode.getLocationInParent();
		if (desc.isChildListProperty()) {
			ListRewrite listRewrite= rewrite.getListRewrite(parent, (ChildListPropertyDescriptor) desc);
			listRewrite.replace(oldNode, newNode, null);
		} else {
			rewrite.set(parent, desc, newNode, null);
		}
	}

	/** Abstract over different ways for identifying a role method. */
	interface IMethodMemento {
		boolean isEqualTo(IMethodBinding other);
	}
	
	// callout inferred from self-call
	static ASTRewriteCorrectionProposal getMaterializeInferredCalloutSelfCallProposal(
				ICompilationUnit cu, 
				TypeDeclaration  enclosingType, 
				MethodInvocation selectedNode) 
			throws JavaModelException 
	{
			final IMethodBinding resolvedMethod = selectedNode.resolveMethodBinding();
			if (resolvedMethod == null) return null;
			IMethodMemento memento = new IMethodMemento() {
				public boolean isEqualTo(IMethodBinding other) {
					return resolvedMethod.equals(other);
				}
			};
			return getMaterializeInferredCalloutProposal(cu, null, enclosingType, memento, InferenceKind.SELFCALL);
	}
	// callout inferred from field access (get or set)
	static ASTRewriteCorrectionProposal getMaterializeInferredCalloutToFieldProposal(
				ICompilationUnit 	cu,
				ASTRewrite		    rewrite,
				ASTNode			 	selectedNode, 
				TypeDeclaration  	enclosingType,
				final InferenceKind kind) 
	{
			// find (fieldName x useThis)
			Pair<SimpleName,Boolean> answer = findFieldName(selectedNode);
			if (answer == null) return null;
			final String accessorName = String.valueOf(OTNameUtils.accessorName(kind == InferenceKind.FIELDSET, answer.first.getIdentifier().toCharArray()));
			final ITypeBinding fieldType = answer.first.resolveTypeBinding();
			IMethodMemento memento = new IMethodMemento() {
				public boolean isEqualTo(IMethodBinding other) {
					if (!accessorName.equals(other.getName()))
						return false;
					ITypeBinding returnType = other.getReturnType();
					ITypeBinding[] params = other.getParameterTypes();
					if (kind == InferenceKind.FIELDSET) {
						// no return:
						if (!Bindings.isVoidType(returnType)) return false;
						// matching param type:
						if (params.length != 1) return false;
						return fieldType == null || params[0].equals(fieldType);
					} else {
						// matching return:
						if (Bindings.isVoidType(returnType)) return false;
						if (fieldType != null && !fieldType.equals(returnType)) return false;
						// no params:
						return params.length == 0;
					}
				}
			};
			return getMaterializeInferredCalloutProposal(cu, rewrite, enclosingType, memento, kind);
	}	
	// callout inferred from self-call or field access - search for resolved callout binding.
	static ASTRewriteCorrectionProposal getMaterializeInferredCalloutProposal(
				ICompilationUnit cu,
				ASTRewrite		 rewrite,
				TypeDeclaration  enclosingType, 
				IMethodMemento   roleMethodMemento,
				InferenceKind    kind) 
	{
		ITypeBinding declaringClass = enclosingType.resolveBinding(); //method.getDeclaringClass();
		for (IMethodMappingBinding mapping : declaringClass.getResolvedMethodMappings())
			if (   mapping != null                        // null if problem exists
				&& mapping.getInferenceKind() == kind
				&& roleMethodMemento.isEqualTo(mapping.getRoleMethod()))
				return createMaterializeInferredCalloutProposal(
									cu, rewrite, enclosingType, mapping, kind);
		
		return null;
	}

	// callout inferred from self-call or field access:
	static ASTRewriteCorrectionProposal createMaterializeInferredCalloutProposal(
				ICompilationUnit 	  cu,
				ASTRewrite            rewrite,
				TypeDeclaration       enclosingType,	
				IMethodMappingBinding mapping,
				InferenceKind         kind) 
	{
		AST ast = enclosingType.getAST();
		ImportRewrite imports;
		try {
			imports = StubUtility.createImportRewrite(cu, true);
		} catch (JavaModelException jme) {
			return null;
		}
		CalloutMappingDeclaration callout = createCalloutFromInferred(mapping, ast, imports, kind);
		
		if (rewrite == null)  // only provided by some clients
			rewrite = ASTRewrite.create(ast);
		rewrite.getListRewrite(enclosingType, BODY_DECLARATIONS_PROPERTY)
					.insertFirst(callout, null);
		String label = (kind == InferenceKind.SELFCALL) 
			? CorrectionMessages.OTQuickfix_materialize_inferred_callout
			: CorrectionMessages.OTQuickfix_materialize_inferred_callout_to_field;
		ASTRewriteCorrectionProposal proposal = new ASTRewriteCorrectionProposal(
												label,
												cu,
												rewrite,
												10000,  // TODO(SH)
												JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
		proposal.setImportRewrite(imports);
		return proposal;
	}

	// callouts inferred from inherited abstract methods:
	static ASTRewriteCorrectionProposal getMaterializeInferredCalloutsInheritedProposal(
			ICompilationUnit cu, TypeDeclaration enclosingType, ASTNode selectedNode) 
		throws JavaModelException 
	{
		AST ast = enclosingType.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);
		ListRewrite listRewrite= rewrite.getListRewrite(enclosingType, BODY_DECLARATIONS_PROPERTY);
		ImportRewrite imports = StubUtility.createImportRewrite(cu, true);
		boolean generated= false;
	
		ITypeBinding type= (ITypeBinding)((SimpleName)selectedNode).resolveBinding();
		for(IMethodMappingBinding mapping : type.getResolvedMethodMappings()) {
			if (!mapping.isCallin()) {
				if (mapping.getInferenceKind() == InferenceKind.INTERFACE) {
					CalloutMappingDeclaration callout;
					callout= createCalloutFromInferred(mapping, ast, imports, InferenceKind.INTERFACE);
					listRewrite.insertFirst(callout, null);
					generated= true;
				}	
			}
		}
		if (generated) {
			ASTRewriteCorrectionProposal proposal;
			proposal= new ASTRewriteCorrectionProposal(CorrectionMessages.OTQuickfix_materialize_inferred_callouts,
							cu,
							rewrite,
							10000,  // TODO(SH)
							JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
			proposal.setImportRewrite(imports);
			return proposal;
		}
		return null;
	}

	// generate a callout declaration from a given binding:
	static CalloutMappingDeclaration createCalloutFromInferred(
			IMethodMappingBinding mapping, AST ast, ImportRewrite imports, InferenceKind kind) 
	{
		CalloutMappingDeclaration callout = ast.newCalloutMappingDeclaration();
		String[] argNames = mapping.getBaseArgumentNames(); // possibly adjusted for fieldset
		IMethodBinding baseMethod = mapping.getBaseMethods()[0];
		switch (kind) {
		case INTERFACE:
		case SELFCALL:
			callout.setBaseMappingElement(
					createMethodSpec(ast, imports, baseMethod, argNames));
			break;
		case FIELDSET:
			{
				String baseMethodName = baseMethod.getName();
				int pos= baseMethodName.lastIndexOf('$');
				String fieldName= baseMethodName.substring(pos+1);
				ITypeBinding fieldType= baseMethod.getParameterTypes()[1];
				callout.setBaseMappingElement(createFieldSpec(ast, imports, fieldName, fieldType));
				callout.bindingOperator().setBindingModifier(Modifier.OT_SET_CALLOUT);
				argNames= new String[] { fieldName };
				break;
			}
		case FIELDGET:
			{
				String baseMethodName = baseMethod.getName();
				int pos= baseMethodName.lastIndexOf('$');
				String fieldName= baseMethodName.substring(pos+1);
				ITypeBinding fieldType= baseMethod.getReturnType();
				callout.setBaseMappingElement(createFieldSpec(ast, imports, fieldName, fieldType));
				callout.bindingOperator().setBindingModifier(Modifier.OT_GET_CALLOUT);
				break;
			}
		}
		
		callout.setRoleMappingElement(
				createMethodSpec(ast, imports, mapping.getRoleMethod(), argNames));
		
		callout.setSignatureFlag(true);
		return callout;
	}

	static MethodSpec createMethodSpec(AST ast, ImportRewrite imports, IMethodBinding methodBinding, String[] argNames) 
	{
		List<SingleVariableDeclaration> args = new ArrayList<SingleVariableDeclaration>();
		for (int i = 0; i < methodBinding.getParameterTypes().length; i++) {
			ITypeBinding paramType = methodBinding.getParameterTypes()[i];
			args.add(
				ASTNodeCreator.createArgument(ast, 0/*modifiers*/, 
						imports.addImport(paramType, ast), 
						argNames[i],
						0 /*extraDimensions*/,
						null));
		}
		ITypeBinding providedReturnType = methodBinding.getReturnType();
		Type returnType = imports.addImport(providedReturnType, ast);
		return ASTNodeCreator.createMethodSpec(ast, 
						methodBinding.getName(), 
						returnType,
						args, true);
	}

	static FieldAccessSpec createFieldSpec(AST ast, ImportRewrite imports, String fieldName, ITypeBinding fieldType) 
	{
		return ASTNodeCreator.createFieldAccSpec(ast, 
						fieldName, 
						imports.addImport(fieldType, ast));
	}

	@SuppressWarnings("unchecked") // handling AST-Lists 
	public static IJavaCompletionProposal addTypeParameterToCallin(ICompilationUnit cu, 
																   ASTNode selectedNode,
																   TypeDeclaration enclosingType) 
	{
		final String TYPE_VAR_NAME = "E"; //$NON-NLS-1$
		
		if (selectedNode instanceof Name) {
			MethodSpec roleSpec= (MethodSpec)ASTNodes.getParent(selectedNode, ASTNode.METHOD_SPEC);
			ASTNode oldType = selectedNode.getParent();
			
			// find the role method to perform the same change on it, too.
			IMethodBinding roleMethod= roleSpec.resolveBinding();
			MethodDeclaration roleMethodDecl= null;
			for (MethodDeclaration method : enclosingType.getMethods()) {
				if (method.resolveBinding() == roleMethod) {
					Type returnType = method.getReturnType2();
					if (returnType == null)
						break;
					if (returnType.isSimpleType()) { 
						Name typeName = ((SimpleType)returnType).getName();
						if ("void".equals(typeName.getFullyQualifiedName())) //$NON-NLS-1$
						break;
					}
					roleMethodDecl= method;
					break;
				}
			}
			
			AST ast = enclosingType.getAST();
			ASTRewrite rewrite = ASTRewrite.create(ast);
			TextEditGroup group= new TextEditGroup("adding parameter"); //$NON-NLS-1$
			// create type parameter <E extends OriginalType>
			TypeParameter typeParameter= ast.newTypeParameter();
			typeParameter.setName(ast.newSimpleName(TYPE_VAR_NAME)); 
			typeParameter.typeBounds().add(ASTNode.copySubtree(ast, oldType));
			// add type parameter to role method spec
			rewrite.getListRewrite(roleSpec, MethodSpec.TYPE_PARAMETERS_PROPERTY)
							.insertFirst(typeParameter, group);
			// change return type to type variable
			rewrite.set(roleSpec, MethodSpec.RETURN_TYPE2_PROPERTY, 
						ast.newSimpleType(ast.newSimpleName(TYPE_VAR_NAME)),
						group);
			
			// the same changes also against the method declaration:
			if (roleMethodDecl != null) {
				rewrite.getListRewrite(roleMethodDecl, MethodDeclaration.TYPE_PARAMETERS_PROPERTY)
					.insertFirst(ASTNode.copySubtree(ast, typeParameter), group);
				rewrite.set(roleMethodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, 
						ast.newSimpleType(ast.newSimpleName(TYPE_VAR_NAME)),
						group);
			}
			
			return new ASTRewriteCorrectionProposal(CorrectionMessages.OTQuickfix_addtypeparametertocallin_label,
					cu,
					rewrite,
					10000,  // TODO(SH)
					JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
		}
		return null;
	}

	private static final String FAKED_METHOD = "$$faked$$"; //$NON-NLS-1$
	
	/**
	 * Add proposals to create method for unresolved method spec.
	 * @param selectedNode    the unresolved method spec
	 * @param enclosingType   the enclosing role
	 * @param context 		  invocation context as needed to find covering nodes
	 * @param problem 		  the problem that triggered the assist
	 * @param proposals 	  list of proposals to which to add the new proposal
	 * @throws CoreException  if some compilation unit could not be found
	 */
	@SuppressWarnings("unchecked")
	public static void addUnresolvedMethodSpecProposals(ASTNode selectedNode, 
														TypeDeclaration enclosingType, 
														IInvocationContext context, 
														final IProblemLocation problem, 
														Collection proposals) 
			throws CoreException 
	{
		// Note: this completion proposal needs to modify the ast in order to 
		// provide a MethodInvocation serving as a template for the proposal.
		AST ast= selectedNode.getAST();
		
		// left-over from previous attempt?
		if (selectedNode instanceof MethodDeclaration) {
			MethodDeclaration method= (MethodDeclaration)selectedNode;
			if (method.getName().getIdentifier().equals(FAKED_METHOD)) {
				// yes, remove it:
				int flags= enclosingType.getFlags();
				if ((flags & ASTNode.PROTECT) != 0)
					enclosingType.setFlags(flags & ~ASTNode.PROTECT);
				enclosingType.bodyDeclarations().remove(method);
				selectedNode= problem.getCoveringNode(context.getASTRoot());
			}
		} 
		while (selectedNode.getNodeType() != ASTNode.METHOD_SPEC) {
			selectedNode = selectedNode.getParent();
			if (selectedNode == null)
				return;
		}
		
		MethodSpec methodSpec= (MethodSpec)selectedNode;
		// construct a faked problem location:
		// a new method:
		MethodDeclaration fakeMethod= ast.newMethodDeclaration();
		fakeMethod.setName(ast.newSimpleName(FAKED_METHOD));
		{
			ASTNode mapping= methodSpec.getParent();
			fakeMethod.setSourceRange(mapping.getStartPosition(), mapping.getLength());
		}
		// a new method invocation:
		MethodInvocation invoc= ast.newMethodInvocation();
		invoc.setSourceRange(methodSpec.getStartPosition(), methodSpec.getLength());
		// receiver:
		StructuralPropertyDescriptor locationInParent = methodSpec.getLocationInParent();
		if (   locationInParent == CalloutMappingDeclaration.BASE_MAPPING_ELEMENT_PROPERTY
			|| locationInParent == CallinMappingDeclaration.BASE_MAPPING_ELEMENTS_PROPERTY)
		{
			if (enclosingType.getNodeType() == ASTNode.ROLE_TYPE_DECLARATION) {
				// create a receiver of the baseclass type:
				Type baseType = ((RoleTypeDeclaration)enclosingType).getBaseClassType();			
				invoc.setExpression(ast.newResolvedVariableName(new String(IOTConstants._OT_BASE_ARG), baseType));
			}
		} else {
			// using an explicit this-typed receiver avoids proposal to define method in the enclosing team:
			invoc.setExpression(ast.newResolvedVariableName(FAKETHIS, enclosingType));
		}
		// final for the IProblemLocation-adaptor below:
		final SimpleName selector = (SimpleName)ASTNode.copySubtree(ast, methodSpec.getName());
		selector.setSourceRange(methodSpec.getName().getStartPosition(), methodSpec.getName().getLength());
		invoc.setName(selector);
		// set args/parameters for both:
		for (Object elem : methodSpec.parameters()) {
			SingleVariableDeclaration param= (SingleVariableDeclaration)elem;
			fakeMethod.parameters().add(ASTNode.copySubtree(ast, param));
			invoc.arguments().add(ast.newSimpleName(param.getName().getIdentifier()));
		}
		// assemble and add to type:
		fakeMethod.setBody(ast.newBlock());
		fakeMethod.getBody().statements().add(ast.newExpressionStatement(invoc));
		int flags= enclosingType.getFlags();
		if ((flags & ASTNode.PROTECT) != 0)
			enclosingType.setFlags(flags & ~ASTNode.PROTECT);
		enclosingType.bodyDeclarations().add(fakeMethod);
		enclosingType.setFlags(flags);
		
		// wrap the problem:
		IProblemLocation newProblem= new IProblemLocation() {
			public ASTNode getCoveredNode(CompilationUnit astRoot)  { return selector; }
			public ASTNode getCoveringNode(CompilationUnit astRoot) { return selector; }
			public int getLength() 					{ return selector.getLength(); }
			public String getMarkerType()         	{ return problem.getMarkerType(); }
			public int getOffset()                	{ return selector.getStartPosition(); }
			public String[] getProblemArguments() 	{ return problem.getProblemArguments(); }
			public int getProblemId() 				{ return problem.getProblemId(); }
			public boolean isError() { return true;}			
		};
		
		UnresolvedElementsSubProcessor.getMethodProposals(context, 
								  newProblem, 
								  problem.getProblemId() == IProblem.DifferentParamInCallinMethodSpec, 
								  proposals);
		
		// parameters may have been set to Object, because types could not be resolved.
		// adjust these from the original method spec now:
		for (Object proposal : proposals) {
			if (proposal instanceof NewMethodCorrectionProposal) {
				NewMethodCorrectionProposal methodProposal = (NewMethodCorrectionProposal)proposal;
				OTQuickFixes.instance().registerNewMethodCorrectionProposal(methodSpec, methodProposal);
				methodProposal.setDisplayName(updateDisplayName(methodSpec, methodProposal.getDisplayString()));
			}
		}
	}
	/** Replace the parameter part in a display name with types from the MethodSpec */
	@SuppressWarnings("unchecked") // methodSpec.parameters is raw type 
	private static String updateDisplayName(MethodSpec methodSpec, String displayString) {
		String head= displayString.substring(0, displayString.indexOf('(')+1);
		String tail= displayString.substring(displayString.indexOf(')')); 
		StringBuffer buf= new StringBuffer(head);
		List parameters= methodSpec.parameters();
		String sep=""; //$NON-NLS-1$
		for (int i=0; i<parameters.size(); i++) {
			buf.append(sep); 
			SingleVariableDeclaration arg= (SingleVariableDeclaration)parameters.get(i);
			buf.append(arg.getType().toString());
			sep= ", "; //$NON-NLS-1$
		}
		buf.append(tail);
		return buf.toString();
	}
}
