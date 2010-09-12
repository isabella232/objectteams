/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTASTNodeSearchUtil.java 23473 2010-02-05 19:46:08Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.refactoring.adaptor.ReorgAdaptor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.TypeHelper;

/**
 * @author stephan
 *
 */
@SuppressWarnings("restriction")
public class OTASTNodeSearchUtil  playedBy ASTNodeSearchUtil 
{
	@SuppressWarnings("basecall")
	static callin ASTNode[] getDeclarationNodes(IJavaElement element, CompilationUnit cuNode) 
		throws JavaModelException 
	{
		switch(element.getElementType()){
		// consider OT-specific elements
        case IOTJavaElement.TEAM:
            return new ASTNode[] { getTypeDeclarationNode((IType)element, cuNode) };
        case IOTJavaElement.ROLE:
            IType roleType = (IType)element;
            if (TypeHelper.isTeam(roleType.getFlags()))
                return new ASTNode[] { getTypeDeclarationNode((IType)element, cuNode) };
            return new ASTNode[] { getRoleTypeDeclarationNode((IType)element, cuNode) };
        case IOTJavaElement.CALLOUT_MAPPING:
            return new ASTNode[] { getCalloutMappingDeclarationNode((ICalloutMapping)element, cuNode) };
        case IOTJavaElement.CALLOUT_TO_FIELD_MAPPING:
            return new ASTNode[] { getCalloutToFieldMappingDeclarationNode((ICalloutToFieldMapping)element, cuNode) };        
        case IOTJavaElement.CALLIN_MAPPING:
            return new ASTNode[] { getCallinMappingDeclarationNode((ICallinMapping)element, cuNode) };
        default:
        	return base.getDeclarationNodes(element, cuNode);
		}
	}
	getDeclarationNodes <- replace getDeclarationNodes;
	
	// === get OT-specific node types
    static ASTNode getRoleTypeDeclarationNode(
            IType type,
            CompilationUnit cuNode) throws JavaModelException
    {
        return ASTNodes.getParent(getNameNode(type, cuNode), RoleTypeDeclaration.class);
    }
    protected static ASTNode getCalloutMappingDeclarationNode(
            ICalloutMapping calloutMapping,
            CompilationUnit cuNode) throws JavaModelException
    {
        ASTNode nameNode = getNameNode(calloutMapping, cuNode);
        if (nameNode.getNodeType() == ASTNode.CALLOUT_MAPPING_DECLARATION)
        	return nameNode; // short mapping: nameRange is the whole mapping
		return ASTNodes.getParent(nameNode, CalloutMappingDeclaration.class);
    }
    protected static ASTNode getCalloutToFieldMappingDeclarationNode(
            ICalloutToFieldMapping calloutToFieldMapping,
            CompilationUnit cuNode) throws JavaModelException
    {
        ASTNode nameNode = getNameNode(calloutToFieldMapping, cuNode);
        if (nameNode.getNodeType() == ASTNode.CALLOUT_MAPPING_DECLARATION)
			return nameNode; // short mapping: nameRange is the whole mapping
        return ASTNodes.getParent(nameNode, CalloutMappingDeclaration.class);
    }
    protected static ASTNode getCallinMappingDeclarationNode(
            ICallinMapping callinMapping,
            CompilationUnit cuNode) throws JavaModelException
    {
        ASTNode nameNode = getNameNode(callinMapping, cuNode);
        if (nameNode.getNodeType() == ASTNode.CALLIN_MAPPING_DECLARATION)
        	return nameNode; // short mapping: nameRange is the whole mapping
		return ASTNodes.getParent(nameNode, CallinMappingDeclaration.class);
    }
    
    abstract static ASTNode getNameNode(IMember iMember, CompilationUnit cuNode) throws JavaModelException;
    @SuppressWarnings("decapsulation")
	getNameNode -> getNameNode;
	
	abstract static TypeDeclaration getTypeDeclarationNode(IType iType, CompilationUnit cuNode) throws JavaModelException;
	getTypeDeclarationNode -> getTypeDeclarationNode;
}
