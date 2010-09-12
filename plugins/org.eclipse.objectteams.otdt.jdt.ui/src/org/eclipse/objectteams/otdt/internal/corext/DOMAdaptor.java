/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: DOMAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.corext;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import base org.eclipse.jdt.internal.corext.dom.ModifierRewrite;

/**
 * This team adapts classes from org.eclipse.jdt.internal.corext.dom.
 * 
 * Issues:
 * - hard coded switch missing OT-cases.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class DOMAdaptor 
{
	protected class ModifierRewrite playedBy ModifierRewrite 
	{
		@SuppressWarnings("decapsulation")
		evaluateListRewrite <- replace evaluateListRewrite;
		
		/** Add missing switch case. */
		@SuppressWarnings("basecall")
		callin ListRewrite evaluateListRewrite(ASTRewrite rewrite, ASTNode declNode) 
		{
			switch (declNode.getNodeType()) {
			case ASTNode.ROLE_TYPE_DECLARATION:
				return rewrite.getListRewrite(declNode, RoleTypeDeclaration.MODIFIERS2_PROPERTY);
			case ASTNode.CALLOUT_MAPPING_DECLARATION:
				return rewrite.getListRewrite(declNode, CalloutMappingDeclaration.MODIFIERS2_PROPERTY);
			}
			return base.evaluateListRewrite(rewrite, declNode);
		}
	}
}
