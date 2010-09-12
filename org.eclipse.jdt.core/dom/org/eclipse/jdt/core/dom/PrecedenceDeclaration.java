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
@SuppressWarnings("rawtypes")
public class PrecedenceDeclaration extends ASTNode {

	@SuppressWarnings("nls")
	public static final ChildListPropertyDescriptor ELEMENTS_PROPERTY =
		new ChildListPropertyDescriptor(PrecedenceDeclaration.class, "elements", Name.class, NO_CYCLE_RISK);

	/**
	 * The "after" structural property of this node type.
	 * @since 0.7.0
	 */
	@SuppressWarnings("nls")
	public static final SimplePropertyDescriptor AFTER_PROPERTY = 
		new SimplePropertyDescriptor(PrecedenceDeclaration.class, "after", boolean.class, MANDATORY);

	private static final List PROPERTY_DESCRIPTORS_3_0;
	
	static {
		List propertyList = new ArrayList(3);
		createPropertyList(PrecedenceDeclaration.class, propertyList);
		addProperty(ELEMENTS_PROPERTY, propertyList);
		addProperty(AFTER_PROPERTY, propertyList);
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

	/**
	 * <code>true</code> for <code>precedence after</code>, else <code>false</code>.
	 */
	boolean isAfter = false;
	
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
     boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
    	 if (property == AFTER_PROPERTY) {
    		 if (get) {
    			 return isAfter();
    		 } else {
    			 setAfter(value);
    			 return false;
    		 }
    	 }
    	 // allow default implementation to flag the error
    	 return super.internalGetSetBooleanProperty(property, get, value);
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
		result.setAfter(isAfter());
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

	/**
	 * Mark whether this is a <code>precedence after</code> declaration. 
	 */
    public void setAfter(boolean isAfter) {
		preValueChange(AFTER_PROPERTY);
		this.isAfter = isAfter;
		postValueChange(AFTER_PROPERTY);		
    }

	/**
	 * Answer whether this is a <code>precedence after</code> declaration. 
	 */
    public boolean isAfter() {
		return this.isAfter;
    }

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 1;
	}

	@Override
	int treeSize() {
		return memSize() + this._elements.listSize();
	}

}
