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
 * $Id: LiftingType.java 14417 2006-09-23 11:18:42Z stephan $
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
 * NEW for OTDT
 * 
 * This class represents the anchor "@anchor" of a dependent type T<@anchor>.
 * For the compiler this is a value reference but syntactically we treat it
 * as a type reference so the whole type can mimic as a regular generic type.
 * 
 * @author stephan
 * @version $Id: LiftingType.java 14417 2006-09-23 11:18:42Z stephan $
 */
public class TypeAnchor extends Type {
	
	/**
	 * The "path" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor PATH_PROPERTY = 
		new ChildPropertyDescriptor(TypeAnchor.class, "path", Name.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	@SuppressWarnings("rawtypes")
	private static final List PROPERTY_DESCRIPTORS;
	
	static
	{
		List<StructuralPropertyDescriptor> propertyList = new ArrayList<StructuralPropertyDescriptor>(2);
		createPropertyList(TypeAnchor.class, propertyList);
		addProperty(PATH_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS&ast;</code> constants
	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 */
	@SuppressWarnings("rawtypes")
	public static List propertyDescriptors(int apiLevel)
	{
		return PROPERTY_DESCRIPTORS;
	}
			
	/** 
	 * The path node; lazily initialized; defaults to a unspecified, but legal, path.
	 */
	private Name path = null;
	
	
	/**
	 * Creates a new unparented node for a type anchor owned by the given AST.
	 * By default, an unspecified, but legal, expression.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	TypeAnchor(AST ast)
	{
		super(ast);
	}

	@SuppressWarnings("rawtypes")
	@Override
	final List internalStructuralPropertiesForType(int apiLevel)
	{
		return propertyDescriptors(apiLevel);
	}
	
	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child)
	{
		if (property == PATH_PROPERTY)
		{
			if (get)
			{
				return getPath();
			}
			else
			{
				setPath((Name) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	/**
	 * Returns the path of this type anchor.
	 * 
	 * @return the path node
	 */ 
	public Name getPath() {
		if (this.path == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.path == null) {
					preLazyInit();
					this.path = new SimpleName(this.ast);
					postLazyInit(this.path, PATH_PROPERTY);
				}
			}
		}
		return this.path;
	}
		
	/**
	 * Sets the path of this type anchor.
	 * 
	 * @param path the new path node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setPath(Name path) {
		if (path == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.path;
		preReplaceChild(oldChild, path, PATH_PROPERTY);
		this.path = path;
		postReplaceChild(oldChild, path, PATH_PROPERTY);
	}

	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChild(visitor, getPath());
		}
		visitor.endVisit(this);
	}

	@Override
	ASTNode clone0(AST target) {
		TypeAnchor result = new TypeAnchor(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setPath((Name) getPath().clone(target));
		return result;
	}

	@Override
	int getNodeType0() {
		return TYPE_ANCHOR;
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 1 * 4;
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@Override
	int treeSize() {
		return memSize() 
		+ (this.path == null ? 0 : getPath().treeSize());
	}

}
