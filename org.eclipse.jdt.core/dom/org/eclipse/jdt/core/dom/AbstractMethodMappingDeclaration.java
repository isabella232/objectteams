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
 * $Id: AbstractMethodMappingDeclaration.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.List;

/**
 * NEW for OTDT.
 * 
 * Super class for callin/callout method mappings
 * @author brcan
 *
 * $Id: AbstractMethodMappingDeclaration.java 23416 2010-02-03 19:59:31Z stephan $
 */
public abstract class AbstractMethodMappingDeclaration extends BodyDeclaration
{
	
	protected MethodMappingElement roleMappingElement = null;

	// includes info about binding kind and binding modifiers
	protected MethodBindingOperator bindingOperator = null;

	/**
	 * Creates a new AST node for an abstract method mapping declaration node owned by
	 * the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	AbstractMethodMappingDeclaration(AST ast)
	{
		super(ast);
	}
	
	/**
	 * The list of parameter mappings (element type: <code>ParameterMapping</code>). 
	 * Defaults to an empty list.
	 */
	ASTNode.NodeList _parameterMappings = 
		new ASTNode.NodeList(internalParameterMappingsProperty());

	/** Return the structural property descriptor for the roleMappingElement property of this node. */
	public abstract ChildPropertyDescriptor getRoleElementProperty();

	/** Return the structural property descriptor for the bindingOperator property of this node. */ 
	abstract ChildPropertyDescriptor internalGetBindingOperatorProperty();
	
	/**
	 * Returns structural property descriptor for the "parameterMappings" property
	 * of this node.
	 * 
	 * @return the property descriptor
	 */
	abstract ChildListPropertyDescriptor internalParameterMappingsProperty();
	
	/**
	 * Creates and returns a structural property descriptor for the
	 * "parameterMappings" property declared on the given concrete node type.
	 * 
	 * @return the property descriptor
	 */
	static final ChildListPropertyDescriptor internalParameterMappingPropertyFactory(Class nodeClass)
    {
		return new ChildListPropertyDescriptor(nodeClass, "parameterMappings", ParameterMapping.class, CYCLE_RISK); //$NON-NLS-1$
	}
	
	ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child)
	{
   	    if (property == getRoleElementProperty())
		{
			if (get) {
				return getRoleMappingElement();
			} else {
				setRoleMappingElement((MethodSpec) child);
				return null;
			}
		}
		if (property == internalGetBindingOperatorProperty())
		{
			if (get) {
				return bindingOperator();
			} else	{
				setBindingOperator((MethodBindingOperator)child);
				return null;
			}
		}
        // allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	

	/**
	 * Returns the method spec left of the callout/in arrow.
	 * @return the left method spec, i.e. the declaring role method
	 */ 
	public MethodMappingElement getRoleMappingElement()
	{
		if (this.roleMappingElement == null) {
			// lazy init must be thread-safe for readers
			synchronized (this)	{
				if (this.roleMappingElement == null) {
					preLazyInit();
					this.roleMappingElement = new MethodSpec(this.ast);
					postLazyInit(this.roleMappingElement, getRoleElementProperty());
				}
			}
		}		
		return this.roleMappingElement;
	}

	/**
	 * Sets the left method spec (role method spec) declared in this callout
	 * mapping declaration to the given method spec.
	 * 
	 * @param roleMappingElement
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
    public void setRoleMappingElement(MethodMappingElement roleMappingElement)
    {
		if (roleMappingElement == null)
		{
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = roleMappingElement;
		preReplaceChild(oldChild, roleMappingElement, getRoleElementProperty());
		this.roleMappingElement = roleMappingElement;
		postReplaceChild(oldChild, roleMappingElement, getRoleElementProperty());
    }

	public void setBindingOperator(MethodBindingOperator bindingOp) {
		ChildPropertyDescriptor propertyDescriptor = internalGetBindingOperatorProperty();
		MethodBindingOperator oldOperator = this.bindingOperator;
		preReplaceChild(oldOperator, bindingOp, propertyDescriptor);
		this.bindingOperator = bindingOp;
		postReplaceChild(oldOperator, bindingOp, propertyDescriptor);		
	}
	
	public MethodBindingOperator bindingOperator() {
        if (this.bindingOperator == null)
        {
            // lazy init must be thread-safe for readers
            synchronized (this)
            {
                if (this.bindingOperator == null)
                {
                    preLazyInit();
                    this.bindingOperator = new MethodBindingOperator(this.ast);
                    postLazyInit(this.bindingOperator, internalGetBindingOperatorProperty());
                }
            }
        }
		return this.bindingOperator;
	}

	/**
     * Returns the live ordered list of parameter mappings for this
     * callin mapping declaration.
     * 
     * @return the live list of parameter mappings
     *    (element type: <code>ParameterMapping</code>)
     */ 
    public List getParameterMappings() 
    {
        return _parameterMappings;
    }

    public boolean hasParameterMapping()
    {
        return (! _parameterMappings.isEmpty());
    }

	public boolean hasSignature() {
		return getRoleMappingElement().hasSignature();
	}
	
	/** 
	 * Mark this method mapping as not having signatures and actually
	 * remove signatures from all method specs.
	 */
	public void removeSignatures() {
		removeSignatureFrom(getRoleMappingElement());
	}
	
    void removeSignatureFrom(MethodMappingElement mappingElement) {
		mappingElement.setSignatureFlag(false);
		switch (mappingElement.getNodeType()) {
			case ASTNode.METHOD_SPEC:
				MethodSpec spec = (MethodSpec) mappingElement;
				spec.setReturnType2(null);
				@SuppressWarnings("rawtypes")
				List typeParams = spec.typeParameters();
				while (typeParams.size() > 0)
					typeParams.remove(0);
				@SuppressWarnings("rawtypes")
				List params = spec.parameters();
				while (params.size() > 0)
					params.remove(0);
				break;
			case ASTNode.FIELD_ACCESS_SPEC:
				FieldAccessSpec fieldSpec = (FieldAccessSpec) mappingElement;
				fieldSpec.setFieldType(null);
				break;
		}
	}

	public IMethodMappingBinding resolveBinding()
    {
        return this.ast.getBindingResolver().resolveMethodMapping(this);
    }
}
