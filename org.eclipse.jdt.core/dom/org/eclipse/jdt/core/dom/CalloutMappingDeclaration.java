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
 * $Id: CalloutMappingDeclaration.java 23416 2010-02-03 19:59:31Z stephan $
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
 * Represents DOM-ASTNode for Callout Bindings (OTJLD §2.4.2), 
 * which has to handle code from e.g. :
 *    foo => bar;
 * to e.g. :
 *		Integer absoluteValue(Integer integer) -> int abs(int i) with { 
 *        integer.intValue() -> i,
 *        result <- new Integer(result)
 *    	}
 * and also the callout to field binding:
 * - without value mapping:
 * 	
 *     	setValue -> set value;
 * 
 * 	   	int getValue() -> get int value;
 * 
 * 	- with value mappings:
 *  	
 * 		Integer getValue()           -> get int val
 *   		with { result             <-    new Integer(result) }
 * 
 *		void setValue(Integer i)     -> set int val
 *   		with { integer.intValue() ->    val }
 *
 * 
 * This class consists of one MethodSpec for bound role method and one MethodSpec for base method
 * or FieldAccessSpec for access to a field of the base class. Also it consists of
 * a callout kind and an optionally mapping of parameters.
 * 
 * This node is used in TypeDeclaration, particulary in RoleTypeDeclaration.
 * 
 * @author ike
 */
public class CalloutMappingDeclaration extends AbstractMethodMappingDeclaration
{
	public static final String CALLOUT = "->"; //$NON-NLS-1$
	public static final String CALLOUT_OVERRIDE = "=>"; //$NON-NLS-1$
	
	/**
	 * Creates a new AST node for a callout mapping declaration owned 
	 * by the given AST. By default, the declaration is for a callout mapping
	 * of an unspecified, but legal, name;
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */    
	CalloutMappingDeclaration(AST ast)
	{
		super(ast);
	}

	/**
	 * The "javadoc" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor JAVADOC_PROPERTY = 
		internalJavadocPropertyFactory(CalloutMappingDeclaration.class);
    
	/**
	 * The left "methodSpec" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor ROLE_MAPPING_ELEMENT_PROPERTY = 
		new ChildPropertyDescriptor(CalloutMappingDeclaration.class, "roleMappingElement", MethodMappingElement.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The binding operator structural property ("<- modifier")
	 * @since 1.3.1
	 */
	public static final ChildPropertyDescriptor BINDING_OPERATOR_PROPERTY =
		new ChildPropertyDescriptor(CalloutMappingDeclaration.class, "bindingOperator", MethodBindingOperator.class, MANDATORY, NO_CYCLE_RISK);

	/**
	 * The right "methodSpec" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor BASE_MAPPING_ELEMENT_PROPERTY = 
		new ChildPropertyDescriptor(CalloutMappingDeclaration.class, "baseMappingElement", MethodMappingElement.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$
    
    /**
	 * The "signature" structural property of this node type.
	 */
	public static final SimplePropertyDescriptor SIGNATURE_PROPERTY = 
		new SimplePropertyDescriptor(CalloutMappingDeclaration.class, "signature", boolean.class, MANDATORY); //$NON-NLS-1$
    
	/**
	 * The "parameterMappings" structural property of this node type.
	 */
	public static final ChildListPropertyDescriptor PARAMETER_MAPPINGS_PROPERTY = 
		internalParameterMappingPropertyFactory(CalloutMappingDeclaration.class);
	
	/**
	 * The "modifiers" structural property of this node type.
	 */
	public static final SimplePropertyDescriptor MODIFIERS_PROPERTY = 
		internalModifiersPropertyFactory(CalloutMappingDeclaration.class);
	
	/**
	 * The "modifiers2" structural property of this node type.
	 */
	public static final ChildListPropertyDescriptor MODIFIERS2_PROPERTY = 
		internalModifiers2PropertyFactory(CalloutMappingDeclaration.class);
	
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
	
	private MethodMappingElement _baseMappingElement = null;
	private boolean _baseMappingInitialized= false;
	private boolean _hasSignature = false;
	
	static
	{
		List propertyList = new ArrayList(8);
		createPropertyList(CalloutMappingDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(ROLE_MAPPING_ELEMENT_PROPERTY, propertyList);
		addProperty(BINDING_OPERATOR_PROPERTY, propertyList);
		addProperty(BASE_MAPPING_ELEMENT_PROPERTY, propertyList);
		addProperty(SIGNATURE_PROPERTY, propertyList);
		addProperty(PARAMETER_MAPPINGS_PROPERTY, propertyList);
		addProperty(MODIFIERS_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);
		
		propertyList = new ArrayList(8);
		createPropertyList(CalloutMappingDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(ROLE_MAPPING_ELEMENT_PROPERTY, propertyList);
		addProperty(BINDING_OPERATOR_PROPERTY, propertyList);
		addProperty(BASE_MAPPING_ELEMENT_PROPERTY, propertyList);
		addProperty(SIGNATURE_PROPERTY, propertyList);
		addProperty(PARAMETER_MAPPINGS_PROPERTY, propertyList);
		addProperty(MODIFIERS2_PROPERTY, propertyList);
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

	final SimplePropertyDescriptor internalModifiersProperty()
	{
		return MODIFIERS_PROPERTY;
	}

    final ChildListPropertyDescriptor internalModifiers2Property()
    {
        return MODIFIERS2_PROPERTY;
    }
    
    final ChildListPropertyDescriptor internalParameterMappingsProperty()
    {
        return PARAMETER_MAPPINGS_PROPERTY;
    }

	ChildPropertyDescriptor internalJavadocProperty()
    {
		return JAVADOC_PROPERTY;
    }

	ChildPropertyDescriptor internalGetRoleElementProperty() {
		return ROLE_MAPPING_ELEMENT_PROPERTY;
	}
	
	ChildPropertyDescriptor internalGetBindingOperatorProperty() {
		return BINDING_OPERATOR_PROPERTY;
	}
	
    final int internalGetSetIntProperty(SimplePropertyDescriptor property, boolean isGetRequest, int value)
	{
		if (property == MODIFIERS_PROPERTY)
		{
			if (isGetRequest)
			{
				return getModifiers();
			}
			else
			{
				setModifiers(value);
				return 0;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetIntProperty(property, isGetRequest, value);
	}
    
    final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean isGet, ASTNode child)
	{
   	    if (property == JAVADOC_PROPERTY)
		{
			if (isGet)
			{
				return getJavadoc();
			}
			else
			{
				setJavadoc((Javadoc) child);
				return null;
			}
		}

   	    if (property == BASE_MAPPING_ELEMENT_PROPERTY)
		{
			if (isGet)
			{
				return getBaseMappingElement();
			}
			else
			{
				setBaseMappingElement((MethodSpec) child);
				return null;
			}
		}		
		// allow default implementation to flag the error (incl. handling of elements common to all method mappings):
		return super.internalGetSetChildProperty(property, isGet, child);
	}
    
    final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value)
	{
		if (property == SIGNATURE_PROPERTY)
		{
			if (get)
			{
				return hasSignature();
			}
			else
			{
				setSignatureFlag(value);
				return false;
			}		
		}
		return super.internalGetSetBooleanProperty(property, get, value);
	}
    
    final List internalGetChildListProperty(ChildListPropertyDescriptor property)
	{
		if (property == PARAMETER_MAPPINGS_PROPERTY)
		{
			return getParameterMappings();
		}
		if (property == MODIFIERS2_PROPERTY)
		{
			return modifiers();
		}
		
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

    List internalStructuralPropertiesForType(int apiLevel)
    {
		return propertyDescriptors(apiLevel);
    }

    int getNodeType0()
    {
        return CALLOUT_MAPPING_DECLARATION;
    }

    @SuppressWarnings("unchecked")
	ASTNode clone0(AST target)
    {
        CalloutMappingDeclaration result = new CalloutMappingDeclaration(target);
		if (this.ast.apiLevel >= AST.JLS3) 
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers())); // annotations        
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.setRoleMappingElement(
				(MethodMappingElement) ASTNode.copySubtree(target, getRoleMappingElement()));
		result.setBindingOperator((MethodBindingOperator)bindingOperator().clone(target));
		result.setBaseMappingElement(
				(MethodMappingElement) ASTNode.copySubtree(target, getBaseMappingElement()));
		result.setSignatureFlag(this.hasSignature());
		result.getParameterMappings().addAll(
				ASTNode.copySubtrees(target, this.getParameterMappings()));
		
		return result;
    }

    boolean subtreeMatch0(ASTMatcher matcher, Object other)
    {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
    }

    void accept0(ASTVisitor visitor)
    {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren)
		{
			// visit children in normal left to right reading order
			acceptChild(visitor, getJavadoc());
			if (this.ast.apiLevel >= AST.JLS3)
				acceptChildren(visitor, modifiers);			
			acceptChild(visitor, roleMappingElement);
			acceptChild(visitor, bindingOperator);
			acceptChild(visitor, _baseMappingElement);
			acceptChildren(visitor,_parameterMappings);
		}
		visitor.endVisit(this);
    }

    int treeSize()
    {
		return memSize() + (super.optionalDocComment == null 
                ? 0 
                : getJavadoc().treeSize());
    }

	/**
	 * Returns the method spec right of the callout arrow.
	 * @return the right method spec, i.e. the referenced base method
	 * @see Modifier
	 */ 
	public MethodMappingElement getBaseMappingElement()
	{
		if (_baseMappingElement == null && !this._baseMappingInitialized)
		{
			// lazy init must be thread-safe for readers
			synchronized (this)
			{
				if (_baseMappingElement == null)
				{
					preLazyInit();
					_baseMappingElement = new MethodSpec(this.ast);
					this._baseMappingInitialized= true;
					postLazyInit(_baseMappingElement, BASE_MAPPING_ELEMENT_PROPERTY);
				}
			}
		}
		return _baseMappingElement;	    
	}

	/**
	 * Sets the right method spec (base method spec) declared in this callout
	 * mapping declaration to the given method spec.
	 * 
	 * @param baseMappingElement
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
    public void setBaseMappingElement(MethodMappingElement baseMappingElement)
    {
		this._baseMappingInitialized= true;
		ASTNode oldChild = _baseMappingElement;
		preReplaceChild(oldChild, baseMappingElement, BASE_MAPPING_ELEMENT_PROPERTY);
		_baseMappingElement = baseMappingElement;
		postReplaceChild(oldChild, baseMappingElement, BASE_MAPPING_ELEMENT_PROPERTY);        
    }

	/**
	 * 
	 * @return the flag, whether callout is a callout override or a simple callout
	 * 	true, if an override;  
	 */ 
	public boolean isCalloutOverride()
	{
		return this.bindingOperator().getBindingKind() == MethodBindingOperator.KIND_CALLOUT_OVERRIDE;
	}

	public boolean hasSignature() 
	{
		return _hasSignature;
	}

	public void setSignatureFlag(boolean hasSignature) 
	{
		preValueChange(SIGNATURE_PROPERTY);
		_hasSignature = hasSignature;
		postValueChange(SIGNATURE_PROPERTY);
	}
}
