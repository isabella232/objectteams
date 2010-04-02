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
 * $Id: FieldAccessSpec.java 23416 2010-02-03 19:59:31Z stephan $
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
 *
 * Represents DOM-ASTNode for callout binding to a field of the corresponding base class (OTJLD §3.5),
 * which has to handle code from e.g. :
 * 		get value
 * 		set value
 * to e.g. :
 *		get int value
 *		set String value
 *
 * This class has following properties:
 * 	modifiers (JLS2) or isSetter (JLS3)
 *  fieldType,
 * 	name,
 * 	signature
 * 
 * This node can be used only in CalloutMethodDeclaration.
 * 
 * @author jsv
 */
public class FieldAccessSpec extends MethodMappingElement 
{
	/**
	 * The "signature" structural property of this node type.
	 */
	public static final SimplePropertyDescriptor SIGNATURE_PROPERTY = 
		internalSignaturePropertyFactory(FieldAccessSpec.class);
		
	/**
	 * The "name" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		internalNamePropertyFactory(FieldAccessSpec.class);
	
	/**
	 * The "fieldType" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor FIELD_TYPE_PROPERTY = 
		new ChildPropertyDescriptor(FieldAccessSpec.class, "fieldType", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS_2_0;

	private static final List PROPERTY_DESCRIPTORS_3_0;

	static
	{
		List propertyList = new ArrayList(4);
		createPropertyList(FieldAccessSpec.class, propertyList);
		addProperty(SIGNATURE_PROPERTY, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(FIELD_TYPE_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);

		propertyList = new ArrayList(4);
		createPropertyList(FieldAccessSpec.class, propertyList);
		addProperty(SIGNATURE_PROPERTY, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(FIELD_TYPE_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the AST.JLS* constants
	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 */
	public static List propertyDescriptors(int apiLevel)
	{
		if (apiLevel == AST.JLS2_INTERNAL)
			return PROPERTY_DESCRIPTORS_2_0;
		else
			return PROPERTY_DESCRIPTORS_3_0;
	}
	
	boolean isSetter = false;
	
	/**
	 * The field type.
	 * JLS2 behevior: lazily initialized; defaults to void.
	 * Note that this field is ignored for constructor declarations.
	 */
	private Type _fieldType = null;
	
	FieldAccessSpec(AST ast)
	{
		super(ast);
	}
	
	final List internalStructuralPropertiesForType(int apiLevel)
	{
		return propertyDescriptors(apiLevel);
	}
	
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean isGetRequest, boolean value)
	{
		if (property == SIGNATURE_PROPERTY)
		{
			if (isGetRequest)
			{
				return hasSignature();
			}
			else
			{
				setSignatureFlag(value);
				return false;
			}		
		}		
		return super.internalGetSetBooleanProperty(property, isGetRequest, value);
	}
	
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean isGetRequest, ASTNode child)
	{
		if (property == NAME_PROPERTY)
		{
			if (isGetRequest)
			{
				return getName();
			}
			else
			{
				setName((SimpleName) child);
				return null;
			}
		}
		if (property == FIELD_TYPE_PROPERTY)
		{
			if (isGetRequest)
			{
				return getFieldType();
			}
			else
			{
				setFieldType((Type) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, isGetRequest, child);
	}
	
	SimplePropertyDescriptor internalSignatureProperty() {
		return SIGNATURE_PROPERTY;
	}
	
	ChildPropertyDescriptor internalNameProperty() {
		return NAME_PROPERTY;
	}
	
	final int getNodeType0()
	{
		return FIELD_ACCESS_SPEC;
	}
	
	ASTNode clone0(AST target)
	{
		FieldAccessSpec result = new FieldAccessSpec(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setSignatureFlag(this.hasSignature());
		result.setName((SimpleName) this.getName().clone(target));
		result.setFieldType(
				(Type) ASTNode.copySubtree(target, getFieldType()));
		return result;
	}
	
	void accept0(ASTVisitor visitor)
	{
		boolean visitChildren = visitor.visit(this);
		if (visitChildren)
		{
			// visit children in normal left to right reading order
			acceptChild(visitor, getFieldType());
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}
	
	final boolean subtreeMatch0(ASTMatcher matcher, Object other)
	{
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}
	
	int treeSize()
	{
		return memSize()
					+ (this.getName() == null ? 0 : getName().treeSize())
					+ (this.getFieldType() == null ? 0 : getFieldType().treeSize());
	}
	
	int memSize()
	{
		return BASE_NODE_SIZE + 3 * 4;
	}
	
	/**
	 * Returns the field type of the field in this FieldAccessSpec,
	 * exclusive of any extra array dimensions (JLS2 API only). 
	 * This is one of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although, it does still figure in subtree equality comparisons
	 * and visits), and is devoid of the binding information ordinarily
	 * available.
	 * </p>
	 * 
	 * @return the return type, possibly the void primitive type
	 */
	public Type getFieldType()
	{
		if (this._fieldType == null)
		{
			// lazy init must be thread-safe for readers
			synchronized (this)
			{
				if (this._fieldType == null)
				{
					preLazyInit();
					this._fieldType = this.ast.newPrimitiveType(PrimitiveType.VOID);
					postLazyInit(this._fieldType, FIELD_TYPE_PROPERTY);
				}
			}
		}
		return this._fieldType;
	}

	/**
	 * Sets the field type of the field declared in this FieldAccessSpec
	 * declaration to the given type, exclusive of any extra array dimensions
	 * (JLS2 API only). This is one of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although it does still figure in subtree equality comparisons and visits).
	 * </p>
	 * 
	 * @param type the new return type, possibly the void primitive type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public void setFieldType(Type type)
	{
		if (type == null)
		{
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this._fieldType;
		preReplaceChild(oldChild, type, FIELD_TYPE_PROPERTY);
		this._fieldType = type;
		postReplaceChild(oldChild, type, FIELD_TYPE_PROPERTY);
	}
	   
    public IVariableBinding resolveBinding() {
        return this.ast.getBindingResolver().resolveVariable(this);
    }
    
}
