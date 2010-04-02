/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PrecedenceDeclaration.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a precedence declaration in OT/J (OTJLD §4.8).
 * A precedence declaration contains a list of names 
 * referring either to classes or to callin bindings.
 *  
 * @author stephan
 */
public class PrecedenceDeclaration extends ASTNode {

	@SuppressWarnings("nls")
	public static final ChildListPropertyDescriptor ELEMENTS_PROPERTY =
		new ChildListPropertyDescriptor(PrecedenceDeclaration.class, "elements", Name.class, NO_CYCLE_RISK);

	private static final List PROPERTY_DESCRIPTORS_3_0;
	
	static {
		List propertyList = new ArrayList(2);
		createPropertyList(PrecedenceDeclaration.class, propertyList);
		addProperty(ELEMENTS_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
	}
	
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			throw new UnsupportedOperationException("JLS2 not supported"); //$NON-NLS-1$
		} else {
			return PROPERTY_DESCRIPTORS_3_0;
		}
	}
	
	ASTNode.NodeList _elements = new ASTNode.NodeList(ELEMENTS_PROPERTY);
	
	PrecedenceDeclaration(AST ast)
	{
		super(ast);
	}
	
     List internalGetChildListProperty(ChildListPropertyDescriptor property)
    {
		if (property == ELEMENTS_PROPERTY) {
			return this._elements;
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
    }
     
	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
				unsupportedIn2();
			}
			if (this.ast.apiLevel >= AST.JLS3) {
				acceptChildren(visitor, this._elements);
			}
		}
		visitor.endVisit(this);
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
        // dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@SuppressWarnings("unchecked")
	@Override
	ASTNode clone0(AST target) {
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) 
			unsupportedIn2();
		PrecedenceDeclaration result = new PrecedenceDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.elements().addAll(ASTNode.copySubtrees(target, elements()));
		return result;
	}

	@Override
	int getNodeType0() {
		return PRECEDENCE_DECLARATION;
	}

	@Override
	List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	
	public List elements() {
		return this._elements;
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE;
	}

	@Override
	int treeSize() {
		return memSize() + this._elements.listSize();
	}

}
