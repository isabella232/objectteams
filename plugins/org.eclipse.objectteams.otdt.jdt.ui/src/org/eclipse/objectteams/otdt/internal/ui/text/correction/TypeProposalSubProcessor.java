/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TypeProposalSubProcessor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeAnchor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.QualifiedTypeNameHistory;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.AddImportCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedCorrectionProposal;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

/**
 * Process quick fixes related to types.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public class TypeProposalSubProcessor {

	private static final String ANCHOR_GROUP_ID = "AnchorNameID"; //$NON-NLS-1$

	/**
	 * Change a simple type role reference to a an anchored type reference.
	 * Infer possible anchors from the context.  
	 * 
	 * @param cu            	where everything happens 
	 * @param fullName      	the qualified name of a role type
	 * @param type         		the node specifying the role type (to be replaced)
	 * @param enclosingTeamName the team containing the role type to change.
	 * 
	 * all parameters: non-null!
	 * @return the proposal (always)
	 */
	@SuppressWarnings("unchecked")
	public static LinkedCorrectionProposal changeTypeToAnchored(ICompilationUnit cu,
																String           fullName, 
																Name             type, 
																String           enclosingTeamName) 
	{
		AST ast = type.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);
		
		// search candidates for an anchor:		
		String[] variables = matchingVariables(type, enclosingTeamName);
		String firstAnchor = (variables.length>0) ? variables[0] : "aTeam"; //$NON-NLS-1$ 
		
		// construct and replace 'Type<@anchor>'
		SimpleName anchorName = (SimpleName)rewrite.createStringPlaceholder(firstAnchor, ASTNode.SIMPLE_NAME);
		TypeAnchor anchor = ast.newTypeAnchor(anchorName);
		Name newTypeName= type;
		if (type.isQualifiedName())
			newTypeName= ((QualifiedName)type).getName();
		newTypeName= (Name)ASTNode.copySubtree(ast, newTypeName);
		ParameterizedType newType = ast.newParameterizedType(ast.newSimpleType(newTypeName));
		newType.typeArguments().add(anchor);
		rewrite.replace(type, newType, null);
		
		// assemble a proposal:
		Image image= JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
		int relevance = 13; // FIXME
		String label = CorrectionMessages.OTQuickFix_Type_change_type_to_anchored; 
		LinkedCorrectionProposal proposal = new LinkedCorrectionProposal(label, cu, rewrite, relevance, image);

		// setup linked mode:
		proposal.addLinkedPosition(rewrite.track(anchorName), true, ANCHOR_GROUP_ID);
		if (variables.length > 1) // should we present choices?
			for (int i=0; i<variables.length; i++) 
				proposal.addLinkedPositionProposal(ANCHOR_GROUP_ID, variables[i], null);
		return proposal;
	}

	/** Find the names of all variables in scope that match typeToMatch. */ 
	static private String[] matchingVariables (ASTNode node, String enclosingTeamName) {
		ArrayList<String> list = new ArrayList<String>();
		ASTNode previous = null;
		while (node != null && node.getNodeType() != ASTNode.COMPILATION_UNIT) {
			switch (node.getNodeType()) {
			case ASTNode.BLOCK:
				list.addAll(getMatchingLocalsPriorTo((Block)node, enclosingTeamName, previous));
				break;
			case ASTNode.METHOD_DECLARATION:
				for (Object fieldObj : ((MethodDeclaration)node).parameters()) {
					SingleVariableDeclaration param= (SingleVariableDeclaration)fieldObj;
					addSingleVarIfMatch(param.getType(), enclosingTeamName, param, list);
				}
				break;
			case ASTNode.ROLE_TYPE_DECLARATION:
			case ASTNode.TYPE_DECLARATION:
				for (FieldDeclaration field : ((TypeDeclaration)node).getFields()) 
					addFragmentsIfMatch(field.getType(), enclosingTeamName, field.fragments(), list);
				break;
			}
			previous = node;
			node = node.getParent();
		}
		return list.toArray(new String[list.size()]);
	}
	
	/** 
	 * Find all local variables within block prior to current that match typeToMatch.
	 * Return the variables' names. 
	 */
	@SuppressWarnings("rawtypes") // block.statements() is raw type
	private static ArrayList<String> getMatchingLocalsPriorTo(Block   block,
															  String  enclosingTeamName, 
															  ASTNode current) 
	{
		ArrayList<String> result = new ArrayList<String>();
		if (current != null) {
			List statements = block.statements();
			for (int i=0; i<statements.size(); i++) {
				Object statement = statements.get(i);
				if (statement instanceof VariableDeclarationStatement) {
					VariableDeclarationStatement var = (VariableDeclarationStatement)statement;
					addFragmentsIfMatch(var.getType(), enclosingTeamName, var.fragments(), result);
				}
				if (statement == current)
					break;
			}
		}
		return result;
	}

	/** If typeNode matches typeToMatch add the names of all fragments to result. */
	@SuppressWarnings("rawtypes")
	private static void addFragmentsIfMatch(Type typeNode, String enclosingTeamName, List fragments, List<String> result) 
	{
		if (   typeNode.isSimpleType()
			&& nameEquals(((SimpleType)typeNode).getName(), enclosingTeamName)) 
		{
			for (int i=0; i<fragments.size(); i++)
				result.add(((VariableDeclarationFragment)fragments.get(i)).getName().getIdentifier());
		}
	}
	private static void addSingleVarIfMatch(Type typeNode, String enclosingTeamName, SingleVariableDeclaration var, List<String> result) 
	{
		if (   typeNode.isSimpleType()
			&& nameEquals(((SimpleType)typeNode).getName(), enclosingTeamName)) 
		{
			result.add(var.getName().getIdentifier());
		}
	}
	
	/** Does name match the type to match, either by simple or by qualified name? */
	private static boolean nameEquals(Name name, String enclosingTeamName) {
		String nameString = name.getFullyQualifiedName();
		return enclosingTeamName.equals(nameString) 
			|| enclosingTeamName.endsWith("."+nameString); //$NON-NLS-1$
	}
	
	/** 
	 * Proposal for adding an "import base" statement to the current role file's enclosing team.
	 * @param cu			where everything happens 
	 * @param fullName 		name of type to import
	 * @param node 			node requiring the import
	 * @param relevance 	relevance
	 * @param maxProposals  how many proposals should be accepted?
	 * @return  the proposal or null
	 */
	public static CUCorrectionProposal createImportInRoFisTeamProposal(ICompilationUnit cu, String fullName, Name node, int relevance, int maxProposals) 
	{	
		// get the new CU:
		try {
			IType[] types = cu.getTypes();
			IOTType otType = OTModelManager.getOTElement(types[0]);
			if (otType == null || !otType.isRole())
				return null; // cu is not a rofi
			IType teamType = ((IRoleType)otType).getTeamJavaType();
			cu = teamType.getCompilationUnit();
			if (cu == null)
				return null;

		} catch (JavaModelException e) {
			return null;
		}
		
		// get a fresh AST:
		ASTParser p = ASTParser.newParser(AST.JLS3);
		p.setSource(cu);
		p.setResolveBindings(false);
		CompilationUnit astCU = (CompilationUnit) p.createAST(null);
		
		for (Object anImport : astCU.imports()) 
			if (((ImportDeclaration)anImport).getName().getFullyQualifiedName().equals(fullName))
				return null; // no change needed
		
		// create the proposal -- starting from here code is inspired by 
		// org.eclipse.jdt.internal.ui.text.correction.UnresolvedElementsSubProcessor.createTypeRefChangeProposal(ICompilationUnit, String, Name, int, int)
		ImportRewrite importRewrite= null;
		String simpleName= fullName;
		String packName= Signature.getQualifier(fullName);
		if (packName.length() > 0) { // no imports for primitive types, type variables
			
			importRewrite= StubUtility.createImportRewrite(astCU, true); // OT-modified(SH)
			simpleName= importRewrite.addImport(fullName);
		}

		if (!isLikelyTypeName(simpleName)) {
			relevance -= 2;
		}

		ASTRewriteCorrectionProposal proposal = null;
		SimpleName simpleNameNode = 
			node.isQualifiedName()
			? ((QualifiedName)node).getName()
			: (SimpleName)node;
		if (importRewrite != null && simpleName.equals(simpleNameNode.getIdentifier())) { // import only
			// import only
			String[] arg= { BasicElementLabels.getJavaElementName(simpleName), BasicElementLabels.getJavaElementName(packName) };
			String label= Messages.format(CorrectionMessages.OTQuickFix_Type_add_base_import_to_enclosing_team, arg);
			Image image= JavaPluginImages.get(JavaPluginImages.IMG_OBJS_IMPDECL);
			int boost= QualifiedTypeNameHistory.getBoost(fullName, 0, maxProposals);
			proposal= new AddImportCorrectionProposal(label, cu, relevance + 100 + boost, image, packName, simpleName, simpleNameNode);
			proposal.setCommandId(ADD_IMPORT_ID);
			proposal.setImportRewrite(importRewrite);
		}
		// "else" dropped for OT
		return proposal;
	}
	private static final String ADD_IMPORT_ID= "org.eclipse.jdt.ui.correction.addImport"; //$NON-NLS-1$
	private static boolean isLikelyTypeName(String name) {
		return name.length() > 0 && Character.isUpperCase(name.charAt(0));
	}

	/**
	 * handle IProblem.RegularlyImportedBaseclass:
	 * "change import to "import base ...";
	 * OR (if cu is a role file):
	 * add a new "import base" to the enclosing team.
	 * 
	 * @param selectedNode reference to the type being imported
	 * @param cu where everything happens
	 * @return the proposal or null
	 * @throws JavaModelException exception in one of the various model operations
	 */
	public static IJavaCompletionProposal getMakeImportBase(ASTNode selectedNode, ICompilationUnit cu) 
			throws JavaModelException
	{
	
		if (selectedNode.getNodeType() != ASTNode.SIMPLE_NAME) 
			return null;
		
		SimpleName name = (SimpleName)selectedNode;
		
		ITypeBinding type = name.resolveTypeBinding();
		if (type == null) 
			return null;
		
		int relevance = 1000; // TODO(SH): compute
		
		IType[] types = cu.getTypes();
		for (IType toplevelType : types) {
			IOTType ottype = OTModelManager.getOTElement(toplevelType);
			if (ottype != null && ottype.isRole())
				// no import base in role file, create a new one in the team instead:
				return createImportInRoFisTeamProposal(cu, type.getQualifiedName(), name, relevance, 1);
		}
		
		ImportRewrite importRewrite = StubUtility.createImportRewrite(cu, true);
		if (!importRewrite.setImportBase(type))
			return null; // failure
		
		String simpleName = type.getName();
		String pack1 = type.getPackage().getNameComponents()[0];
		String displayName = pack1+"..."+simpleName; //$NON-NLS-1$
		
		ASTRewriteCorrectionProposal rewriteProposal =
					new ASTRewriteCorrectionProposal(
							Messages.format(CorrectionMessages.OTQuickFix_Type_convertimporttobase_description, 
											new String[] {displayName}),
							cu, 
							ASTRewrite.create(selectedNode.getAST()), 
							relevance, 
							JavaPluginImages.get(JavaPluginImages.IMG_OBJS_IMPDECL));
		rewriteProposal.setImportRewrite(importRewrite);
		return rewriteProposal;
	}

	/**
	 * Import a base class so that a qualified reference can be replaced by a simple one.
	 * 
	 * @param selectedNode the qualified reference
	 * @param cu where everything happens
	 * @return the proposal or null
	 * @throws JavaModelException exception in one of the various model operations
	 */
	public static IJavaCompletionProposal getImportBaseclass(ASTNode selectedNode, ICompilationUnit cu) throws JavaModelException 
	{
		if (selectedNode.getNodeType() != ASTNode.QUALIFIED_NAME) 
			return null;
		
		QualifiedName typeRef = (QualifiedName)selectedNode;

		ITypeBinding type = typeRef.resolveTypeBinding();
		if (type == null) 
			return null;
		
		int relevance = 1000; // TODO(SH): compute
		
		IType[] types = cu.getTypes();
		boolean isRoleFile = false;
		for (IType toplevelType : types) {
			IOTType ottype = OTModelManager.getOTElement(toplevelType);
			if (ottype != null && ottype.isRole()) {
				isRoleFile = true;
				break;
			}
		}
		// no import base in role file, create a new one in the team instead?
		final IJavaCompletionProposal importProposal = isRoleFile 
			? createImportInRoFisTeamProposal(cu, type.getQualifiedName(), typeRef, relevance, 1)
			: null;
		
		// create the base import
		ImportRewrite importRewrite = StubUtility.createImportRewrite(cu, true);
		if (!isRoleFile) {
			importRewrite.addImport(type);
			if (!importRewrite.setImportBase(type))
				return null; // failure
		}
		
		AST ast = selectedNode.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);
		
		// replace the type reference with a simple name:
		Name simpleTypeRef = ast.newName(typeRef.getName().getIdentifier());
		rewrite.replace(selectedNode, simpleTypeRef, null);

		// assemble the proposal:
		String simpleName = type.getName();
		String pack1 = type.getPackage().getNameComponents()[0];
		String displayName = pack1+"..."+simpleName; //$NON-NLS-1$
		ASTRewriteCorrectionProposal rewriteProposal =
					new ASTRewriteCorrectionProposal(
							Messages.format(CorrectionMessages.OTQuickFix_Type_convert_fqn_to_importtobase_description, 
											new String[] {displayName}),
							cu, 
							rewrite, 
							relevance, 
							JavaPluginImages.get(JavaPluginImages.IMG_OBJS_IMPDECL))
					{
						@Override
						public void apply(IDocument document) {
							super.apply(document);
							// propagate to the import proposal which refers to a different CU (that's why be build two separate proposals):
							if (importProposal != null)
								importProposal.apply(document);
						}
					};
		rewriteProposal.setImportRewrite(importRewrite);
		return rewriteProposal;
	}
}
