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
 * $Id: CallinMappingDeclaration.java 23416 2010-02-03 19:59:31Z stephan $
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
 * Represents a callin binding of a role method with one ore more base methods (OTJLD §4).
 * Callin bindings must have one modifier (before, replace, after)
 * and may have parameter mappings. A replace callin may also have a result mapping.
 * e.g.:
 * 
 * ranges from:
 * <code>roleMethod <- after baseMethod;</code>
 * to:
 * <pre>
 * void roleMethod(MyObject obj) <-
 * after char[] baseMethod(String name, SomeClass cls) with 
 * {
 *     obj <- cls.foo(), 
 * }
 * </pre>
 * or:
 * <pre>
 * char[] roleMethod(MyObject obj) <-
 * replace char[] baseMethod(MyObject o, String name) with 
 * {
 *     obj <- o,
 *     result <- result 
 * }
 * </pre>
 * 
 * Callin bindings consist of
 * <ol>
 * <li>  a name as mentioned in source code or a generated name "&lt;File:Line,Col&gt;".
 * <li>  role_method_designator "<-" callin_modifier base_method_designator;
 *   <ol>
 *   <li> method designators may or may not contain parameters lists and return type
 *       but no modifiers (see §3.1(c)).
 *   <li> callin_modifiers are replace, before, after.
 *   </ol>
 * <li>  Parameter mappings
 *   <ol>
 *   <li> result mapping (only replace)
 *   </ol>
 * </ol>
 * 
 * Callin bindinds can be used in the following AST-nodes:
 * TypeDeclaration (compiler ast)
 * RoleTypeDeclaration (dom/ast)
 * 
 * @author mkr
 */
public class CallinMappingDeclaration extends AbstractMethodMappingDeclaration
{
	public static final String CALLIN = "<-"; //$NON-NLS-1$
    
	/**
	 * Creates a new AST node for a callin mapping declaration owned 
	 * by the given AST. By default, the declaration is for a callin mapping
	 * of an unspecified, but legal, name;
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	CallinMappingDeclaration(AST ast)
	{
		super(ast);
	}

    /**
     * The "names" structural property of this node type.
     */
    public static final ChildPropertyDescriptor NAME_PROPERTY = 
        new ChildPropertyDescriptor(CallinMappingDeclaration.class, "name", SimpleName.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$
    
	/**
	 * The "modifiers" structural property of this node type (added in JLS3 API).
	 * @since 3.1
	 */
	public static final ChildListPropertyDescriptor MODIFIERS2_PROPERTY = 
		internalModifiers2PropertyFactory(CallinMappingDeclaration.class);

	
	/**
	 * The "javadoc" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor JAVADOC_PROPERTY = 
		internalJavadocPropertyFactory(CallinMappingDeclaration.class);

	/**
	 * The "roleMappingElement" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor ROLE_MAPPING_ELEMENT_PROPERTY = 
		new ChildPropertyDescriptor(CallinMappingDeclaration.class, "roleMappingElement", MethodMappingElement.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The binding operator structural property ("<- modifier")
	 * @since 1.3.1
	 */
	public static final ChildPropertyDescriptor BINDING_OPERATOR_PROPERTY =
		new ChildPropertyDescriptor(CallinMappingDeclaration.class, "bindingOperator", MethodBindingOperator.class, MANDATORY, NO_CYCLE_RISK);

	/**
	 * The "baseMappingElements" structural property of this node type.
	 */
	public static final ChildListPropertyDescriptor BASE_MAPPING_ELEMENTS_PROPERTY = 
		new ChildListPropertyDescriptor(CallinMappingDeclaration.class, "baseMappingElements", MethodMappingElement.class, NO_CYCLE_RISK); //$NON-NLS-1$
	
    /**
     * The "guardPredicate" structural property of this node type.
     * @since 0.9.25
     */
    public static final ChildPropertyDescriptor GUARD_PROPERTY = 
        new ChildPropertyDescriptor(CallinMappingDeclaration.class, "guardPredicate", GuardPredicateDeclaration.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
     * The "parameter mappings" structural property of this node type.
     */
    public static final ChildListPropertyDescriptor PARAMETER_MAPPINGS_PROPERTY = 
        internalParameterMappingPropertyFactory(CallinMappingDeclaration.class);
	    
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
	
    private SimpleName _labelName = null;
    
	private MethodMappingElement _roleMappingElement = null; // FIXME(SH): should be MethodSpec??
	ASTNode.NodeList _baseMappingElements = new ASTNode.NodeList(BASE_MAPPING_ELEMENTS_PROPERTY);
    
	GuardPredicateDeclaration _optionalGuardPredicate = null;
	
	static
	{
		List propertyList = new ArrayList(7);
		createPropertyList(CallinMappingDeclaration.class, propertyList);
        addProperty(NAME_PROPERTY, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(ROLE_MAPPING_ELEMENT_PROPERTY, propertyList);
		addProperty(BINDING_OPERATOR_PROPERTY, propertyList);
		addProperty(BASE_MAPPING_ELEMENTS_PROPERTY, propertyList);
        addProperty(PARAMETER_MAPPINGS_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);

		propertyList = new ArrayList(9);
		createPropertyList(CallinMappingDeclaration.class, propertyList);
        addProperty(NAME_PROPERTY, propertyList);
		addProperty(MODIFIERS2_PROPERTY, propertyList); // for annotations
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(ROLE_MAPPING_ELEMENT_PROPERTY, propertyList);
		addProperty(BINDING_OPERATOR_PROPERTY, propertyList);
		addProperty(BASE_MAPPING_ELEMENTS_PROPERTY, propertyList);
        addProperty(GUARD_PROPERTY, propertyList);		
        addProperty(PARAMETER_MAPPINGS_PROPERTY, propertyList);
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
		throw new UnsupportedOperationException("JLS2 not supported"); //$NON-NLS-1$
	}

	final ChildListPropertyDescriptor internalModifiers2Property() 
	{
		return MODIFIERS2_PROPERTY;
	}

	public ChildPropertyDescriptor getRoleElementProperty() {
		return ROLE_MAPPING_ELEMENT_PROPERTY;
	}

	protected ChildPropertyDescriptor internalGetBindingOperatorProperty() {
		return BINDING_OPERATOR_PROPERTY;
	}

	final ChildListPropertyDescriptor internalParameterMappingsProperty()
	{
	    return PARAMETER_MAPPINGS_PROPERTY;
	}

	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child)
	{
		if (property == JAVADOC_PROPERTY)
		{
			if (get) {
				return getJavadoc();
			} else {
				setJavadoc((Javadoc) child);
				return null;
			}
		}
        if (property == NAME_PROPERTY) {
            if (get) {
                return getName();
            } else {
                setName((SimpleName)child);
                return null;
            }
        }
		// name is not a child (SimpleName) but a direct String property
		// callin modifier is not a child either but a direct int property
		if (property == ROLE_MAPPING_ELEMENT_PROPERTY)
		{
		    if (get) {
		        return getRoleMappingElement();
			} else  {
		        setRoleMappingElement((MethodSpec) child);
		        return null;
		    }
		}
        if (property == GUARD_PROPERTY)
        {
            if (get) {
                return getGuardPredicate();
            } else {
                setGuardPredicate((GuardPredicateDeclaration) child);
                return null;
            }
        }

		// allow default implementation to flag the error (incl. handling of elements common to all method mappings):
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	final List internalGetChildListProperty(ChildListPropertyDescriptor property)
	{
        if(property == MODIFIERS2_PROPERTY)
        	return modifiers();

		if (property == BASE_MAPPING_ELEMENTS_PROPERTY)
	        return getBaseMappingElements();

        if (property == PARAMETER_MAPPINGS_PROPERTY)
            return  getParameterMappings();
        
        // allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}
       
	ChildPropertyDescriptor internalJavadocProperty()
    {
		return JAVADOC_PROPERTY;
    }

    List internalStructuralPropertiesForType(int apiLevel)
    {
		return propertyDescriptors(apiLevel);
    }

    int getNodeType0()
    {
        return CALLIN_MAPPING_DECLARATION;
    }

    @SuppressWarnings("unchecked")
	ASTNode clone0(AST target)
    {
		CallinMappingDeclaration result = new CallinMappingDeclaration(target);
        result.setName(this.getName());
		if (this.ast.apiLevel >= AST.JLS3) 
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers())); // annotations
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.setRoleMappingElement(
				(MethodSpec) ASTNode.copySubtree(target, getRoleMappingElement()));
		result.setBindingOperator((MethodBindingOperator)bindingOperator().clone(target));
		result.getBaseMappingElements().addAll(
				ASTNode.copySubtrees(target, getBaseMappingElements()));
        result.setGuardPredicate((GuardPredicateDeclaration)ASTNode.copySubtree(target, getGuardPredicate()));
        result.getParameterMappings().addAll(
                ASTNode.copySubtrees(target, getParameterMappings()));
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
			acceptChild(visitor, _labelName);
			acceptChild(visitor, _roleMappingElement);
			if (this.ast.apiLevel >= AST.JLS3)
				acceptChildren(visitor, modifiers);
			acceptChild(visitor, bindingOperator);
			acceptChildren(visitor, _baseMappingElements);
			acceptChild(visitor, this.getGuardPredicate());
            acceptChildren(visitor, _parameterMappings);
		}
		visitor.endVisit(this);
    }
    
    void appendDebugString(StringBuffer buffer) {
    	if (getName() != null) {
	        buffer.append(getName().getIdentifier());
	        buffer.append(':');
    	}
        super.appendDebugString(buffer);
    }
    
    int treeSize()
    {
		return memSize() + (super.optionalDocComment == null 
                                ? 0 
                                : getJavadoc().treeSize());
    }

	/**
	 * Returns the method spec left of the callin arrow.
	 * @return the left method spec, i.e. the declaring role method
	 */ 
    @Override
	public MethodMappingElement getRoleMappingElement()
	{
        if (_roleMappingElement == null)
        {
            // lazy init must be thread-safe for readers
            synchronized (this)
            {
                if (_roleMappingElement == null)
                {
                    preLazyInit();
                    _roleMappingElement = new MethodSpec(this.ast);
                    postLazyInit(_roleMappingElement, ROLE_MAPPING_ELEMENT_PROPERTY);
                }
            }
        }
        return _roleMappingElement;
    }
	
	/**
	 * Sets the left method spec (role method spec) declared in this callin
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
		ASTNode oldChild = _roleMappingElement;
		preReplaceChild(oldChild, roleMappingElement, ROLE_MAPPING_ELEMENT_PROPERTY);
		_roleMappingElement = roleMappingElement;
		postReplaceChild(oldChild, roleMappingElement, ROLE_MAPPING_ELEMENT_PROPERTY);
    }

	/**
	 * Returns the live ordered list of base method specs for this
	 * callin mapping declaration.
	 * 
	 * @return the live list of base method specs
	 *    (element type: <code>IExtendedModifier</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 */ 
	public List getBaseMappingElements()
	{
		return _baseMappingElements;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeSignatures() {
		super.removeSignatures();
		for (Object baseElement : getBaseMappingElements())
			removeSignatureFrom((MethodMappingElement) baseElement);
	}

	/**
	 * Returns the callin modifiers explicitly specified on this declaration.
	 * 
	 * @return exactly one of before, after, replace using constants from Modifier
	 * @see Modifier
	 */ 
	public int getCallinModifier()
	{
		return bindingOperator.getBindingModifier();
	}
	
	/**
	 * Sets the callin modifier explicitly specified on this declaration.
	 * 
	 * @param modifiers the given modifiers (bit-wise or of <code>Modifier</code> constants)
	 * @see Modifier
	 * @deprecated use setBindingOperator instead
	 */
	public void setCallinModifier(int modifiers)
	{
		setCallinModifier(this.ast.newModifier(Modifier.ModifierKeyword.fromFlagValue(modifiers)));
	}
	
	/**
	 * @deprecated use setBindingOperator
	 */
	public void setCallinModifier(Modifier modifier) {
		if (this.bindingOperator == null) {
			MethodBindingOperator op = new MethodBindingOperator(this.ast);
			op.setBindingKind(MethodBindingOperator.KIND_CALLIN);
			op.setBindingModifier(modifier);
			this.setBindingOperator(op);
		} else {
			this.bindingOperator.setBindingModifier(modifier);
		}
	}
	
	/**
	 * Returns the callin modifier for this callin mapping declaration.
	 * 
     * @see Modifier
	 * @return one of before, after, replace
	 */ 
	public Modifier callinModifier()
	{
		return this.bindingOperator.bindingModifier();
	}

	public boolean hasName() {
		return _labelName != null && _labelName.getIdentifier().charAt(0) != '<';
	}
    public void setName(SimpleName name)
    {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this._labelName;
		preReplaceChild(oldChild, name, NAME_PROPERTY);
		this._labelName = name;
		postReplaceChild(oldChild, name, NAME_PROPERTY);

    }
 
    public SimpleName getName() {
    	return this._labelName;
    }
    
    public void setGuardPredicate(GuardPredicateDeclaration predicate) {
        ASTNode oldChild = this._optionalGuardPredicate;
        preReplaceChild(oldChild, predicate, GUARD_PROPERTY);
        this._optionalGuardPredicate = predicate;
        postReplaceChild(oldChild, predicate, GUARD_PROPERTY);
	}

	public GuardPredicateDeclaration getGuardPredicate() {
		return _optionalGuardPredicate;
	}

	/** 
	 * Return whether a static base method is bound such that
	 * no instance will be passed through this binding 
	 * (one static base method suffices to determine staticness).
	 */
	public boolean isStatic() {
		for (Object baseElem : this.getBaseMappingElements()) {
			IMethodBinding baseMethod = ((MethodSpec)baseElem).resolveBinding();
			if (baseMethod != null && Modifier.isStatic(baseMethod.getModifiers()))
				return true;
		}
		return false;
	}
}
