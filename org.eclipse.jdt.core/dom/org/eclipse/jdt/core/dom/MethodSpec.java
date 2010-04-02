/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * NEW for OTDT, built in analogy to MethodDeclaration.
 *
 * Represents DOM-ASTNode for callout binding to a method of the corresponding
 * base class, which has to handle code 
 * from e.g. :
 * 		baseMethod
 * to e.g. :
 *		String roleGetString(int b, String str)
 *
 * This class has following properties:
 * 	parameters,
 *  returnType,
 * 	name,
 * 	signature
 * 
 * This AST node has no modifier.
 * 
 * This node can be used in CalloutMethodDeclaration and 
 * CallinMappingDeclaration
 * 
 * @author jsv
 */
public class MethodSpec extends MethodMappingElement
{
	/**
	 * The "signature" structural property of this node type.
	 */
	public static final SimplePropertyDescriptor SIGNATURE_PROPERTY = 
		internalSignaturePropertyFactory(MethodSpec.class);
		
	/**
	 * The "covariantReturnType" property, flagging if "+" has been specified in the source.
	 * @since OTDT 1.1.3
	 */
	public static final SimplePropertyDescriptor COVARIANT_RETURN_PROPERTY =
		new SimplePropertyDescriptor(MethodSpec.class, "covariantReturn", boolean.class, MANDATORY); //$NON-NLS-1$
	
	/**
	 * The "name" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		internalNamePropertyFactory(MethodSpec.class);
	
	/**
	 * The "returnType" structural property of this node type (JLS2 API only).
	 */
	// TODO (jeem) When JLS3 support is complete (post 3.0) - deprecated Replaced by {@link #RETURN_TYPE2_PROPERTY} in the JLS3 API.
	public static final ChildPropertyDescriptor RETURN_TYPE_PROPERTY = 
		new ChildPropertyDescriptor(MethodSpec.class, "returnType", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "returnType2" structural property of this node type (added in JLS3 API).
	 * @since 3.1
	 */
	public static final ChildPropertyDescriptor RETURN_TYPE2_PROPERTY = 
		new ChildPropertyDescriptor(MethodSpec.class, "returnType2", Type.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "typeParameters" structural property of this node type (added in JLS3 API).
	 * @since OTDT 1.1.3
	 */
	public static final ChildListPropertyDescriptor TYPE_PARAMETERS_PROPERTY = 
		new ChildListPropertyDescriptor(MethodSpec.class, "typeParameters", TypeParameter.class, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "parameters" structural property of this node type).
	 */
	public static final ChildListPropertyDescriptor PARAMETERS_PROPERTY = 
		new ChildListPropertyDescriptor(MethodSpec.class, "parameters", SingleVariableDeclaration.class, CYCLE_RISK); //$NON-NLS-1$
		
	
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
	 * @since 3.1
	 */
	private static final List PROPERTY_DESCRIPTORS_3_0;

		
	static
	{
		List propertyList = new ArrayList(6);
		createPropertyList(MethodSpec.class, propertyList);
		addProperty(RETURN_TYPE_PROPERTY, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(PARAMETERS_PROPERTY, propertyList);
		addProperty(SIGNATURE_PROPERTY, propertyList);
		addProperty(COVARIANT_RETURN_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);
		
		propertyList = new ArrayList(7);
		createPropertyList(MethodSpec.class, propertyList);
		addProperty(RETURN_TYPE2_PROPERTY, propertyList);
		addProperty(TYPE_PARAMETERS_PROPERTY, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(PARAMETERS_PROPERTY, propertyList);
		addProperty(SIGNATURE_PROPERTY, propertyList);
		addProperty(COVARIANT_RETURN_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);

	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the AST.JLS* constants
	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			return PROPERTY_DESCRIPTORS_2_0;
		} else {
			return PROPERTY_DESCRIPTORS_3_0;
		}
	}
	
	/**
	 * The parameter declarations 
	 * (element type: <code>SingleVariableDeclaration</code>).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList parameters =
		new ASTNode.NodeList(PARAMETERS_PROPERTY);
	
	/**
	 * The return type.
	 * JLS2 behevior: lazily initialized; defaults to void.
	 * Note that this field is ignored for constructor declarations.
	 */
	private Type returnType = null;
	
	/**
	 * Indicated whether the return type has been initialized.
	 * @since 3.1
	 */
	private boolean returnType2Initialized = false;

	/**
	 * The type paramters (element type: <code>TypeParameter</code>). 
	 * (see constructor).
	 * @since OTDT 1.1.3
	 */
	private ASTNode.NodeList typeParameters = null;

	/**
	 * Whether the return type was specified with "+" to match covariant return types, too.
	 * @since OTDT 1.1.3
	 */
	private boolean _hasCovariantReturn;

	/**
	 * Creates a new AST node for a method spec declaration owned 
	 * by the given AST. By default, the declaration is for a method spec
	 * of an unspecified, but legal, name; no modifiers; no javadoc; no type 
	 * parameters; void return type; no parameters; no array dimensions after 
	 * the parameters; no thrown exceptions; and no body (as opposed to an
	 * empty body).
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	MethodSpec(AST ast)
	{
		super(ast);
		if (ast.apiLevel >= AST.JLS3)
		    this.typeParameters = new ASTNode.NodeList(TYPE_PARAMETERS_PROPERTY);
	}

	/**
	 * Returns the covariantReturn flag
	 * @since OTDT 1.1.3
	 */ 
	public boolean hasCovariantReturn()	{
		return _hasCovariantReturn;
	}
	
	/**
	 * Sets the covariantReturn flag.
	 * @since OTDT 1.1.3
	 */ 
	public void setCovariantReturnFlag(boolean hasCovariantReturn)
	{
		preValueChange(COVARIANT_RETURN_PROPERTY);
		this._hasCovariantReturn = hasCovariantReturn;
		postValueChange(COVARIANT_RETURN_PROPERTY);
	}
	
	final List internalStructuralPropertiesForType(int apiLevel)
	{
		return propertyDescriptors(apiLevel);
	}
	
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value)
	{
		if (property == SIGNATURE_PROPERTY)
		{
			if (get) {
				return hasSignature();
			} else {
				setSignatureFlag(value);
				return false;
			}		
		}
		if (property == COVARIANT_RETURN_PROPERTY)
		{
			if (get) {
				return hasCovariantReturn();
			} else {
				setCovariantReturnFlag(value);
				return false;
			}		
		}
		
		return super.internalGetSetBooleanProperty(property, get, value);
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
				setName((SimpleName) child);
				return null;
			}
		}
		if (property == RETURN_TYPE_PROPERTY)
		{
			if (get)
			{
				return getReturnType();
			}
			else
			{
				setReturnType((Type) child);
				return null;
			}
		}
		if(property == RETURN_TYPE2_PROPERTY) {
			if(get)
				return getReturnType2();
			else {
				setReturnType2((Type) child);
				return null;
			}
		}
			

		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	final List internalGetChildListProperty(ChildListPropertyDescriptor property)
	{
		if (property == TYPE_PARAMETERS_PROPERTY) {
			return typeParameters();
		}
		if (property == PARAMETERS_PROPERTY) {
			return parameters();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

		
    final ChildListPropertyDescriptor internalModifiers2Property()
    {
        return null;
    }
    
    SimplePropertyDescriptor internalSignatureProperty() {
		return SIGNATURE_PROPERTY;
	}
	
	ChildPropertyDescriptor internalNameProperty() {
		return NAME_PROPERTY;
	}

	final int getNodeType0()
	{
		return METHOD_SPEC;
	}

	@SuppressWarnings("unchecked")
	ASTNode clone0(AST target)
	{
		MethodSpec result = new MethodSpec(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		if (this.ast.apiLevel == AST.JLS2)
		{
			result.setReturnType(
					(Type) ASTNode.copySubtree(target, getReturnType()));
		}
		if (this.ast.apiLevel >= AST.JLS3)
		{
			result.setReturnType2(
					(Type) ASTNode.copySubtree(target, getReturnType2()));
			result.typeParameters().addAll(
				ASTNode.copySubtrees(target, typeParameters()));
		}
		result.setName((SimpleName) getName().clone(target));
		result.parameters().addAll(
			ASTNode.copySubtrees(target, parameters()));
		result.setSignatureFlag(this.hasSignature());
		result.setCovariantReturnFlag(this.hasCovariantReturn());
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
			// visit children in normal left to right reading order
			if (this.ast.apiLevel == AST.JLS2)
			{
				acceptChild(visitor, getReturnType());
			}
			else
			{
//				acceptChildren(visitor, this.modifiers);
				acceptChild(visitor, getReturnType2());
			}
			acceptChild(visitor, getName());
			acceptChildren(visitor, this.typeParameters);
			acceptChildren(visitor, this.parameters);
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the live ordered list of method parameter declarations for this
	 * method spec.
	 * 
	 * @return the live list of method parameter declarations
	 *    (element type: <code>SingleVariableDeclaration</code>)
	 */
	public List parameters() {
		return this.parameters;
	}

	/**
	 * Returns the live ordered list of type parameters of this method
	 * declaration (added in JLS3 API). This list is non-empty for parameterized methods.
	 * 
	 * @return the live list of type parameters
	 *    (element type: <code>TypeParameter</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since OTDT 1.1.3
	 */ 
	public List typeParameters() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.typeParameters == null) {
			unsupportedIn2();
		}
		return this.typeParameters;
	}

    public IMethodBinding resolveBinding() {
        return this.ast.getBindingResolver().resolveMethod(this);
    }
    
	/**
	 * Returns the return type of the method declared in this method spec,
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
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than JLS2
	 */
	// TODO (jeem) When JLS3 support is complete (post 3.0) - deprecated In the JLS3 API, this method is replaced by <code>getReturnType2</code>, which may return <code>null</code>.
	public Type getReturnType()
	{
	    supportedOnlyIn2();
		if (this.returnType == null)
		{
			// lazy init must be thread-safe for readers
			synchronized (this)
			{
				if (this.returnType == null)
				{
					preLazyInit();
					this.returnType = this.ast.newPrimitiveType(PrimitiveType.VOID);
					postLazyInit(this.returnType, RETURN_TYPE_PROPERTY);
				}
			}
		}
		return this.returnType;
	}

	/**
	 * Sets the return type of the method declared in this method spec
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
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than JLS2
	 */
	// TODO (jeem) When JLS3 support is complete (post 3.0) - deprecated In the JLS3 API, this method is replaced by <code>setReturnType2</code>, which accepts <code>null</code>.
	public void setReturnType(Type type)
	{
	    supportedOnlyIn2();
		if (type == null)
		{
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.returnType;
		preReplaceChild(oldChild, type, RETURN_TYPE_PROPERTY);
		this.returnType = type;
		postReplaceChild(oldChild, type, RETURN_TYPE_PROPERTY);
	}
	
	/**
	 * Returns the return type of the method declared in this method 
	 * declaration, exclusive of any extra array dimensions (added in JLS3 API). 
	 * This is one of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although, if present, it does still figure in subtree equality comparisons
	 * and visits), and is devoid of the binding information ordinarily
	 * available. In the JLS2 API, the return type is mandatory. 
	 * In the JLS3 API, the return type is optional.
	 * </p>
	 * 
	 * @return the return type, possibly the void primitive type,
	 * or <code>null</code> if none
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @since 3.1
	 */ 
	public Type getReturnType2() {
	    unsupportedIn2();
		if (this.returnType == null && !this.returnType2Initialized) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.returnType == null && !this.returnType2Initialized) {
					preLazyInit();
					this.returnType = this.ast.newPrimitiveType(PrimitiveType.VOID);
					this.returnType2Initialized = true;
					postLazyInit(this.returnType, RETURN_TYPE2_PROPERTY);
				}
			}
		}
		return this.returnType;
	}
	
	/**
	 * Sets the return type of the method declared in this method declaration
	 * to the given type, exclusive of any extra array dimensions (added in JLS3 API).
	 * This is one of the few places where the void type is meaningful.
	 * <p>
	 * Note that this child is not relevant for constructor declarations
	 * (although it does still figure in subtree equality comparisons and visits).
	 * In the JLS2 API, the return type is mandatory. 
	 * In the JLS3 API, the return type is optional.
	 * </p>
	 * 
	 * @param type the new return type, possibly the void primitive type,
	 * or <code>null</code> if none
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @since 3.1
	 */ 
	public void setReturnType2(Type type) {
	    unsupportedIn2();
		this.returnType2Initialized = true;
		ASTNode oldChild = this.returnType;
		preReplaceChild(oldChild, type, RETURN_TYPE2_PROPERTY);
		this.returnType = type;
		postReplaceChild(oldChild, type, RETURN_TYPE2_PROPERTY);
	}


	int memSize()
	{
		return BASE_NODE_SIZE + 3 * 4;
	}
	
	int treeSize()
	{
		return memSize()
					+ (this.getName() == null ? 0 : getName().treeSize())
					+ (this.returnType == null ? 0 : this.returnType.treeSize())
					+ this.parameters.listSize()
					+ this.typeParameters.listSize();
	}

}
