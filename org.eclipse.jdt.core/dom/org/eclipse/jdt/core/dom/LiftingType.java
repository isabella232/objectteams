/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: LiftingType.java 23416 2010-02-03 19:59:31Z stephan $
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
 * Represents DOM-ASTNode for declared lifting (OTJLD §2.3.2)
 * which has to handle code like:
 * 		(MyClass as MyRole role) 
 * 
 * This class has following properties:
 * 	Name
 *  baseType
 *	roleType
 * 
 * This node represents a Type.
 * 
 * This node inherits from Type and is handled like other types. 
 * - typically used in team level methods (declared lifting) 
 *
 * @author jsv
 */

public class LiftingType extends Type
{
	
	/**
	 * The "BaseType" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor BASE_TYPE_PROPERTY = 
		new ChildPropertyDescriptor(LiftingType.class, "baseType", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "roleType" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor ROLE_TYPE_PROPERTY = 
		new ChildPropertyDescriptor(LiftingType.class, "roleType", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "name" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		new ChildPropertyDescriptor(LiftingType.class, "name", Name.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static
	{
		List propertyList = new ArrayList(4);
		createPropertyList(LiftingType.class, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(BASE_TYPE_PROPERTY, propertyList);
		addProperty(ROLE_TYPE_PROPERTY, propertyList);
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
	public static List propertyDescriptors(int apiLevel)
	{
		return PROPERTY_DESCRIPTORS;
	}
			
	/** 
	 * The type name node; lazily initialized; defaults to a type with
	 * an unspecified, but legal, name.
	 */
	private Name typeName = null;
	
	/**
	 * The base type.
	 * JLS2 behevior: lazily initialized; defaults to void.
	 * Note that this field is ignored for constructor declarations.
	 */
	private Type _baseType = null;
	
	/**
	 * The role type.
	 * JLS2 behevior: lazily initialized; defaults to void.
	 * Note that this field is ignored for constructor declarations.
	 */
	private Type _roleType = null;
	
	/**
	 * Creates a new unparented node for a lifting type owned by the given AST.
	 * By default, an unspecified, but legal, name.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	LiftingType(AST ast)
	{
		super(ast);
	}

	final List internalStructuralPropertiesForType(int apiLevel)
	{
		return propertyDescriptors(apiLevel);
	}
	
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child)
	{
		if (property == NAME_PROPERTY)
		{
			if (get)
			{
				return getName();
			}
			else
			{
				setName((Name) child);
				return null;
			}
		}
		
		if (property == BASE_TYPE_PROPERTY)
		{
			if (get)
			{
				return getBaseType();
			}
			else
			{
				setBaseType((Type) child);
				return null;
			}
		}
		
		if (property == ROLE_TYPE_PROPERTY)
		{
			if (get)
			{
				return getRoleType();
			}
			else
			{
				setRoleType((Type) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	final int getNodeType0()
	{
		return LIFTING_TYPE;
	}

	ASTNode clone0(AST target)
	{
		LiftingType result = new LiftingType(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setName((Name) (getName()).clone(target));
		result.setBaseType((Type) ASTNode.copySubtree(target, getBaseType()));
		result.setRoleType((Type) ASTNode.copySubtree(target, getRoleType()));
		
		return result;
	}

	final boolean subtreeMatch0(ASTMatcher matcher, Object other)
	{
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	void accept0(ASTVisitor visitor)
	{
		boolean visitChildren = visitor.visit(this);
		if (visitChildren)
		{
			acceptChild(visitor, getName());
			acceptChild(visitor, getBaseType());
			acceptChild(visitor, getRoleType());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the name of this lifting type.
	 * 
	 * @return the name of this lifting type
	 */ 
	public Name getName()
	{
		if (this.typeName == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.typeName == null) {
					preLazyInit();
					this.typeName = new SimpleName(this.ast);
					postLazyInit(this.typeName, NAME_PROPERTY);
				}
			}
		}
		return this.typeName;
	}
	
	/**
	 * Sets the name of this lifting type to the given name.
	 * 
	 * @param typeName the new name of this lifting type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(Name typeName)
	{
		if (typeName == null)
		{
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.typeName;
		preReplaceChild(oldChild, typeName, NAME_PROPERTY);
		this.typeName = typeName;
		postReplaceChild(oldChild, typeName, NAME_PROPERTY);
	}

	/**
	 * Returns the base type in this LiftingType,
	 */
	public Type getBaseType()
	{
		if (this._baseType == null)
		{
			// lazy init must be thread-safe for readers
			synchronized (this)
			{
				if (this._baseType == null)
				{
					preLazyInit();
					this._baseType = this.ast.newPrimitiveType(PrimitiveType.VOID);
					postLazyInit(this._baseType, BASE_TYPE_PROPERTY);
				}
			}
		}
		return this._baseType;
	}
	
	/**
	 * Returns the role type in this LiftingType,
	 */
	public Type getRoleType()
	{
		if (this._roleType == null)
		{
			// lazy init must be thread-safe for readers
			synchronized (this)
			{
				if (this._roleType == null)
				{
					preLazyInit();
					this._roleType = this.ast.newPrimitiveType(PrimitiveType.VOID);
					postLazyInit(this._roleType, ROLE_TYPE_PROPERTY);
				}
			}
		}
		return this._roleType;
	}
	
	/**
	 * Sets the base type of in this LiftingType
	 */
	public void setBaseType(Type type)
	{
		if (type == null)
		{
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this._baseType;
		preReplaceChild(oldChild, type, BASE_TYPE_PROPERTY);
		this._baseType = type;
		postReplaceChild(oldChild, type, BASE_TYPE_PROPERTY);
	}
	
	/**
	 * Sets the role type of in this LiftingType
	 */
	public void setRoleType(Type type)
	{
		if (type == null)
		{
			throw new IllegalArgumentException();
		}		
		ASTNode oldChild = this._roleType;
		preReplaceChild(oldChild, type, ROLE_TYPE_PROPERTY);
		this._roleType = type;
		postReplaceChild(oldChild, type, ROLE_TYPE_PROPERTY);
	}
	
	int memSize()
	{
		// treat Code as free
		return BASE_NODE_SIZE + 3 * 4;
	}
	
	int treeSize()
	{
		return memSize() 
			+ (this.typeName == null ? 0 : getName().treeSize())
			+ (this.getBaseType() == null ? 0 : getBaseType().treeSize())
			+ (this.getRoleType() == null ? 0 : getRoleType().treeSize());
	}
}
