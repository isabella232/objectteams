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
 * $Id: ParameterMapping.java 23416 2010-02-03 19:59:31Z stephan $
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
 * NEW for OTDT.
 *
 * Represents the DOM-ASTNode for parameterMapping, which has to handle mappings for callin (OTJLD §4.4) and callout (OTJLD §3.2) bindings.
 * 
 * Possible parameter mappings are:
 *   "integer.intValue() -> i" or "result <- new Integer(result)" for callout Bindings or
 *     "what <- uid" or  "new Integer(i) -> result" for callin Bindings
 *    
 * Contained AST elements: 
 * This class consists of one expression, one identifier, one direction and a flag, whether it is
 * a return-parameter mapping or not. The direction is necessary for determination of callout and callin.
 * 
 * Locations in source code:
 * This node is used in CalloutMappingDeclartion, CallinMappingDeclaration
 * 
 * @author ike
 */
public class ParameterMapping extends Expression
{
	/**
	 * Creates a new AST node for a ParameterMapping declaration owned 
	 * by the given AST. By default, the declaration is for a ParameterMapping
	 * of an unspecified, but legal, name;
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */    
	ParameterMapping(AST ast)
	{
		super(ast);
	}
	
	
	/**
	 * The expression structural property of this node type.
	 */
	public static final ChildPropertyDescriptor EXPRESSION_PROPERTY = 
		new ChildPropertyDescriptor(ParameterMapping.class, "expression", Expression.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The identifier structural property of this node type.
	 */
	public static final ChildPropertyDescriptor IDENTIFIER_PROPERTY = 
		new ChildPropertyDescriptor(ParameterMapping.class, "identifier", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The direction structural property of this node type.
	 */
	public static final SimplePropertyDescriptor DIRECTION_PROPERTY = 
		new SimplePropertyDescriptor(ParameterMapping.class, "direction", String.class, MANDATORY);	 //$NON-NLS-1$
	
	/**
	 * The is_Result structural property of this node type.
	 */
    private static final SimplePropertyDescriptor IS_RESULT_PROPERTY =
        new SimplePropertyDescriptor(ParameterMapping.class, "isResult", boolean.class, MANDATORY); //$NON-NLS-1$

	
    /**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS_2_0;
	
	private Expression _expression = null;
	private SimpleName _identifier = null;
	private String _direction = null;
	private boolean _isResult = false;
    
	static
	{
		List propertyList = new ArrayList(5);
		createPropertyList(ParameterMapping.class, propertyList);
		addProperty(EXPRESSION_PROPERTY, propertyList);
		addProperty(IDENTIFIER_PROPERTY, propertyList);
		addProperty(DIRECTION_PROPERTY, propertyList);
		addProperty(IS_RESULT_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);
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
		return PROPERTY_DESCRIPTORS_2_0;
	}
	
	
	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the AST.JLS* constants
	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 */
	List internalStructuralPropertiesForType(int apiLevel) 
	{
		return PROPERTY_DESCRIPTORS_2_0;
	}

    final ASTNode internalGetSetChildProperty(
        				ChildPropertyDescriptor property, 
        				boolean isGet, 
        				ASTNode child)
	{
		if (property == IDENTIFIER_PROPERTY)
		{
			if (isGet)
			{
				return getIdentifier();
			}
			else
			{
				setIdentifier((SimpleName) child);
				return null;
			}
		}
		else if (property == EXPRESSION_PROPERTY)
		{
			if (isGet)
			{
				return getExpression();
			}
			else
			{
				setExpression((Expression) child);
				return null;
			}
		}

		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, isGet, child);
	}
	
	final boolean internalGetSetBooleanProperty(
	    				SimplePropertyDescriptor property, 
	    				boolean isGet, 
	    				boolean value)
	{
		if (property == IS_RESULT_PROPERTY)
		{
			if (isGet)
			{
				return hasResultFlag();
			}
			else
			{
				setResultFlag(value);
				return false;
			}		
		}
		return super.internalGetSetBooleanProperty(property, isGet, value);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTNode#internalGetSetObjectProperty(org.eclipse.jdt.core.dom.SimplePropertyDescriptor, boolean, java.lang.Object)
	 */
	final Object internalGetSetObjectProperty(
	        			SimplePropertyDescriptor property,
	        			boolean isGet, 
	        			Object value) 
	{

		if (property == DIRECTION_PROPERTY)
		{
			if (isGet)
			{
				return getDirection();
			}
			else
			{
				setDirection((String) value);
				return null;
			}
		}
		return super.internalGetSetObjectProperty(property, isGet, value);
	}
	
	public boolean hasResultFlag() 
	{
		return _isResult;
	}

	public void setResultFlag(boolean resultFlag)
    {
		preValueChange(IS_RESULT_PROPERTY);
		_isResult = resultFlag;
		postValueChange(IS_RESULT_PROPERTY);
	}

	public SimpleName getIdentifier()
	{
		if (_identifier == null)
		{
			// lazy init must be thread-safe for readers
			synchronized (this)
			{
				if (_identifier == null)
				{
					preLazyInit();
					_identifier = new SimpleName(super.ast);
					postLazyInit(_identifier, IDENTIFIER_PROPERTY);
				}
			}
		}	return _identifier;
	}

	public void setIdentifier(SimpleName identifier)
	{
		if (identifier == null)
		{
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = _identifier;
		preReplaceChild(oldChild, identifier, IDENTIFIER_PROPERTY);
		_identifier = identifier;
		postReplaceChild(oldChild, identifier, IDENTIFIER_PROPERTY);
	}	

	public String getDirection()
	{
		if (_direction == null)
		{
			// lazy init must be thread-safe for readers
			synchronized (this)
			{
				if (_direction == null)
				{
					_direction = new String(""); //$NON-NLS-1$
				}
			}
		}
		return _direction;
	}
	
	public boolean isBindIN() {
		return "<-".equals(getDirection());
	}
	
	public void setDirection(String direction)
	{
		if (direction == null)
		{
			throw new IllegalArgumentException();
		}
		_direction = direction;
	}


	public ASTNode getExpression()
	{
		if (_expression == null)
		{
			// lazy init must be thread-safe for readers
			synchronized (this)
			{
				if (_expression == null)
				{
					preLazyInit();
					_expression = new SimpleName(super.ast);
					postLazyInit(_expression, EXPRESSION_PROPERTY);
				}
			}
		}	return _expression;
	}

	public void setExpression(Expression expression)
	{
		if (expression == null)
		{
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = _expression;
		preReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
		_expression = expression;
		postReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTNode#getNodeType0()
	 */
	int getNodeType0()
	{
		return ASTNode.PARAMETER_MAPPING;
	}
 
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTNode#subtreeMatch0(org.eclipse.jdt.core.dom.ASTMatcher, java.lang.Object)
	 */
	boolean subtreeMatch0(ASTMatcher matcher, Object other)
	{
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTNode#clone0(org.eclipse.jdt.core.dom.AST)
	 */
	ASTNode clone0(AST target)
	{
		ParameterMapping result = new ParameterMapping(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setExpression((Expression) ASTNode.copySubtree(target,getExpression()));
		result.setIdentifier((SimpleName) ASTNode.copySubtree(target,getIdentifier()));
		result.setDirection(this.getDirection());
		result.setResultFlag(this.hasResultFlag());

		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTNode#accept0(org.eclipse.jdt.core.dom.ASTVisitor)
	 */
	void accept0(ASTVisitor visitor)
	{
		boolean visitChildren = visitor.visit(this);
		if (visitChildren)
		{
			// visit children in normal left to right reading order
			if (getDirection().equals("->")) { 	// expr -> id
				acceptChild(visitor, getExpression());
				acceptChild(visitor, getIdentifier());
			} else {							// id <- expr
				acceptChild(visitor, getIdentifier());
				acceptChild(visitor, getExpression());
			}
		}
		visitor.endVisit(this);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTNode#treeSize()
	 */
	int treeSize()
	{
		return memSize() + getExpression().treeSize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTNode#memSize()
	 */
	int memSize()
	{
		return BASE_NODE_SIZE + 4 * 4;
	}
}
