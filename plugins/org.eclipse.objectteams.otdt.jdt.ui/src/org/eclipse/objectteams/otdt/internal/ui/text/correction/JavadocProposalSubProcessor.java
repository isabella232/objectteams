/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JavadocProposalSubProcessor.java 23438 2010-02-04 20:05:24Z stephan $
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
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal;

/**
 * Compute quick fix proposals for OT-specific Javadoc.
 * 
 * @author stephan
 * @since 1.2.5
 */
@SuppressWarnings("restriction")
public class JavadocProposalSubProcessor 
{
	private static final String TAG_ROLE = "@role"; //$NON-NLS-1$

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static CUCorrectionProposal addRoleTag(ICompilationUnit cu,
												  String[]         problemArguments, 
												  TypeDeclaration  teamType) 
	{
		if (problemArguments == null || problemArguments.length != 1)
			return null;
		AST ast = teamType.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);
		
		// create the tag
		TagElement newTag= ast.newTagElement();
		newTag.setTagName(TAG_ROLE);
		
		// add the role name
		List fragments= newTag.fragments();
		String name= problemArguments[0];
		fragments.add(ast.newSimpleName(name));
		
		// add the tag to the javadoc
		Javadoc javadoc = teamType.getJavadoc();
		ListRewrite tagsRewriter= rewrite.getListRewrite(javadoc, Javadoc.TAGS_PROPERTY);
		tagsRewriter.insertLast(newTag, null);
		
		String label= Messages.format(CorrectionMessages.OTQuickfix_add_missing_role_tag, name);
		int relevance = 1;
		return new ASTRewriteCorrectionProposal(label, cu, rewrite, relevance, JavaPluginImages.get(JavaPluginImages.IMG_OBJS_JAVADOCTAG));
	}

}
