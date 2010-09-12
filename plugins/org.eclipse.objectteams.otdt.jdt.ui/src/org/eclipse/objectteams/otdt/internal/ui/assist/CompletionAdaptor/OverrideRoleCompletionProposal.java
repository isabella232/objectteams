/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann
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
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.ui.assist.CompletionAdaptor;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.text.java.JavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.TypeProposalInfo;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;

/**
 * Completion proposal to override a role from a (t)super team.
 */
@SuppressWarnings("restriction")
protected class OverrideRoleCompletionProposal extends JavaTypeCompletionProposal
{
	private static final int R_TYPE=18; // raise relevance by this factor (slightly more than overriding methods)
	
	private IJavaProject fJavaProject;
	private String fRoleName;
	private int fReplaceStart;
	private int fModifiers;
	private StyledString fDisplayString; 

	public OverrideRoleCompletionProposal( 
		       ICompilationUnit   cu,  
		       CompletionProposal proposal,
			   int                length,
			   StyledString       displayName,
			   Image              image) 
	{
		super(String.valueOf(proposal.getCompletion()), cu, proposal.getReplaceStart(), length, image, displayName, proposal.getRelevance()*R_TYPE);
		fJavaProject=   cu.getJavaProject();
		fDisplayString= displayName;
		fRoleName=      String.valueOf(proposal.getName());
		fModifiers=     proposal.getFlags();
		fReplaceStart=  proposal.getReplaceStart();
		setProposalInfo(new TypeProposalInfo(fJavaProject, proposal));
	}
	
	public StyledString getStyledDisplayString() {
		return this.fDisplayString;
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getPrefixCompletionText(org.eclipse.jface.text.IDocument,int)
	 */
	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		return this.fRoleName;
	}

	private CompilationUnit getRecoveredAST(IDocument document, int offset, Document recoveredDocument) {
		CompilationUnit ast= SharedASTProvider.getAST(fCompilationUnit, SharedASTProvider.WAIT_ACTIVE_ONLY, null);
		if (ast != null) {
			recoveredDocument.set(document.get());
			return ast;
		}

		char[] content= document.get().toCharArray();

		// clear prefix to avoid compile errors
		int index= offset - 1;
		while (index >= 0 && Character.isJavaIdentifierPart(content[index])) {
			content[index]= ' ';
			index--;
		}

		recoveredDocument.set(new String(content));

		final ASTParser parser= ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setSource(content);
		parser.setUnitName(fCompilationUnit.getElementName());
		parser.setProject(fCompilationUnit.getJavaProject());
		return (CompilationUnit) parser.createAST(new NullProgressMonitor());
	}

	/*
	 * @see JavaTypeCompletionProposal#updateReplacementString(IDocument,char,int,ImportRewrite)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected boolean updateReplacementString(IDocument document, char trigger, int offset, ImportRewrite importRewrite) throws CoreException, BadLocationException 
	{
		Document recoveredDocument= new Document();
		CompilationUnit unit= getRecoveredAST(document, offset, recoveredDocument);

		// find enclosing team type:
		ASTNode node= NodeFinder.perform(unit, fReplaceStart, 0);
		while(node != null && !(node instanceof AbstractTypeDeclaration)) {
			node= node.getParent();
		}
		
		if (node != null) {
			AbstractTypeDeclaration teamDecl= ((AbstractTypeDeclaration) node);
			
			// create and setup the rewrite:
			AST ast = unit.getAST();
			ASTRewrite rewrite= ASTRewrite.create(ast);
			rewrite.setToOTJ();
			
			// create type
			TypeDeclaration newType = ast.newTypeDeclaration();
			newType.setName(ast.newSimpleName(this.fRoleName));
			newType.setInterface(Flags.isInterface(this.fModifiers));
			newType.setTeam(Flags.isTeam(this.fModifiers));
			// add @Override:
			Annotation overrideAnnotation = ast.newMarkerAnnotation();
			overrideAnnotation.setTypeName(ast.newSimpleName("Override")); //$NON-NLS-1$
			List modifiers = newType.modifiers();
			modifiers.add(overrideAnnotation);
			// add protected or public
			modifiers.add(ast.newModifier(Flags.isPublic(this.fModifiers) 
							? ModifierKeyword.PUBLIC_KEYWORD 
							: ModifierKeyword.PROTECTED_KEYWORD));
			// add team keyword?
			if (Flags.isTeam(this.fModifiers))
				modifiers.add(ast.newModifier(ModifierKeyword.TEAM_KEYWORD));
			
			insertStub(rewrite, teamDecl, teamDecl.getBodyDeclarationsProperty(), this.fReplaceStart, newType);

			// create the replacementString from the rewrite:
			ITrackedNodePosition position= rewrite.track(newType);
			try {
				rewrite.rewriteAST(recoveredDocument, fJavaProject.getOptions(true)).apply(recoveredDocument);

				String generatedCode= recoveredDocument.get(position.getStartPosition(), position.getLength());
				CodeGenerationSettings settings= JavaPreferencesSettings.getCodeGenerationSettings(fJavaProject);
				int generatedIndent= IndentManipulation.measureIndentUnits(getIndentAt(recoveredDocument, position.getStartPosition(), settings), settings.tabWidth, settings.indentWidth);

				String indent= getIndentAt(document, getReplacementOffset(), settings);
				setReplacementString(IndentManipulation.changeIndent(generatedCode, generatedIndent, settings.tabWidth, settings.indentWidth, indent, TextUtilities.getDefaultLineDelimiter(document)));

			} catch (MalformedTreeException exception) {
				JavaPlugin.log(exception);
			} catch (BadLocationException exception) {
				JavaPlugin.log(exception);
			}
		}
		return true;
	}
	

	private static String getIndentAt(IDocument document, int offset, CodeGenerationSettings settings) {
		try {
			IRegion region= document.getLineInformationOfOffset(offset);
			return IndentManipulation.extractIndentString(document.get(region.getOffset(), region.getLength()), settings.tabWidth, settings.indentWidth);
		} catch (BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}
}
