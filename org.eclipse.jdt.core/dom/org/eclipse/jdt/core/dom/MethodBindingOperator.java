/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann
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
 * Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents all information concerning HOW a method mapping connects
 * its role/base features:
 * <ul>
 * <li>The binding kind as marked by one of the tokens <code>-&gt;,=&gt;,&lt;-</code>
 * <li>An optional modifier, one of <code>before,replace,after</code> (callin) or <code>get,set</code> (callout-to-field).
 * </ul>
 * @author stephan
 * @since 1.3.1
 */
@SuppressWarnings("unchecked")
public class MethodBindingOperator extends ASTNode {

	public static final int KIND_CALLOUT 		  = 1;
	public static final int KIND_CALLOUT_OVERRIDE = 2;
	public static final int KIND_CALLIN 		  = 3;
	
	/**
	 * The "binding-kind" property of this node type.
	 */
	public static final SimplePropertyDescriptor BINDING_KIND_PROPERTY =
		new SimplePropertyDescriptor(MethodBindingOperator.class, "bindingKind", int.class, MANDATORY);

	/**
	 * The "binding-modifier" structural property of this node type, none if regular callout.
	 */
    public static final ChildPropertyDescriptor BINDING_MODIFIER_PROPERTY = 
		new ChildPropertyDescriptor(MethodBindingOperator.class, "bindingModifier", Modifier.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

    
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS_2_0;
		
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS_3_0;

	static
	{
		List propertyList = new ArrayList(1);
		createPropertyList(MethodBindingOperator.class, propertyList);
		addProperty(BINDING_KIND_PROPERTY, propertyList);
		addProperty(BINDING_MODIFIER_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);

		propertyList = new ArrayList(1);
		createPropertyList(MethodBindingOperator.class, propertyList);
		addProperty(BINDING_KIND_PROPERTY, propertyList);
		addProperty(BINDING_MODIFIER_PROPERTY, propertyList); // one flag, not a bitset
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
		if(apiLevel == AST.JLS3)
			return PROPERTY_DESCRIPTORS_3_0;
		else
			return PROPERTY_DESCRIPTORS_2_0;
	}
	
	private int bindingKind = 0; // one of KIND_CALLIN, KIND_CALLOUT, KIND_CALLOUT_OVERRIDE;
	/**
	 * The modifier flags; exactly one of before, after, replace.
	 * No default.
	 */
	private int bindingModifierFlag = 0;
	private Modifier bindingModifier;

	public MethodBindingOperator(AST ast) {
		super(ast);
	}


	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child)
	{
		if (property == BINDING_MODIFIER_PROPERTY)
		{
			if (get) {
				return bindingModifier();
			} else	{
				setBindingModifier((Modifier)child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	@Override
	int internalGetSetIntProperty(SimplePropertyDescriptor property, boolean get, int value) {
		if (property == BINDING_KIND_PROPERTY) {
			if (get) {
				return getBindingKind();
			}
			else
			{
				setBindingKind(value);
				return 0;
			}
		}

		// default impl to signal errors
		return super.internalGetSetIntProperty(property, get, value);
	}

    public int getBindingKind() {
		return this.bindingKind;
	}

	public void setBindingKind(int bindingKind) {
		if (bindingKind < 0) {
			throw new IllegalArgumentException();
		}
		preValueChange(BINDING_KIND_PROPERTY);
		this.bindingKind = bindingKind;
		postValueChange(BINDING_KIND_PROPERTY);
	}


	/**
	 * Returns the callin modifiers explicitly specified on this declaration.
	 * 
	 * @return exactly one of before, after, replace using constants from Modifier
	 * @see Modifier
	 */ 
	public int getBindingModifier()
	{
		if (this.bindingModifierFlag == 0 && this.bindingModifier != null) 
		{
			this.bindingModifierFlag = this.bindingModifier.getKeyword().toFlagValue();
		}
		return bindingModifierFlag;
	}
	
	/**
	 * Sets the callin or c-t-f modifier explicitly specified on this declaration.
	 * 
	 * @param modifiers the given modifiers (bit-wise or of <code>Modifier</code> constants)
	 * @see Modifier
	 */
	public void setBindingModifier(int modifiers)
	{
		setBindingModifier(this.ast.newModifier(Modifier.ModifierKeyword.fromFlagValue(modifiers)));
	}
	
	public void setBindingModifier(Modifier modifier) {
		ChildPropertyDescriptor propertyDescriptor = BINDING_MODIFIER_PROPERTY;
		Modifier oldModifier = this.bindingModifier;
		preReplaceChild(oldModifier, modifier, propertyDescriptor);
		this.bindingModifierFlag = 0; // clear cached flags
		this.bindingModifier = modifier;
		postReplaceChild(oldModifier, modifier, propertyDescriptor);		
	}
	
	/**
	 * Returns the callin or c-t-f modifier for this mapping declaration.
	 * 
     * @see Modifier
	 * @return one of before, after, replace
	 */ 
	public Modifier bindingModifier()
	{
		return bindingModifier;
	}

	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChild(visitor, this.bindingModifier);
		}
		visitor.endVisit(this);
	}

	ASTNode clone0(AST target) {
		MethodBindingOperator result = new MethodBindingOperator(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setBindingModifier(this.bindingModifier);
		result.setBindingKind(this.bindingKind);
		return result;
	}

	int getNodeType0() {
		return METHOD_BINDING_OPERATOR;
	}

	List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	int memSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		if (!(other instanceof MethodBindingOperator))
			return false;
		MethodBindingOperator otherOp = (MethodBindingOperator) other;
		return    (otherOp.bindingKind == this.bindingKind)
		       && (otherOp.getBindingModifier() == this.getBindingModifier());
	}

	int treeSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
