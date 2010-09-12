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
 * NEW for OTDT, built in analogy to WhileStatement.
 *
 * Represents a within statement (OTJLD §5.2(a)), e.g.
 * <code>
 * within(new MyTeam())
 * {
 *     foo();
 * }
 * </code>
 * or
 * <code>
 * within(myTeam)
 * {
 *     foo();
 * }
 * </code>
 *
 *  
 * Within Statements consist of a team expression and
 * a body element (org.eclipse.jdtd.core.dom.Block).
 * 
 * Within statements can be used in the following AST-nodes:
 * Block
 * 
 * @author mkr
 */
public class WithinStatement extends Statement 
{
    /**
     * The "team expression" structural property of this node type.
     */
    public static final ChildPropertyDescriptor TEAM_EXPRESSION_PROPERTY = 
        new ChildPropertyDescriptor(WithinStatement.class, "team expression", Expression.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$
    
	/**
	 * The "body" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor BODY_PROPERTY = 
		new ChildPropertyDescriptor(WithinStatement.class, "body", Statement.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	@SuppressWarnings("rawtypes")
	private static final List PROPERTY_DESCRIPTORS;
	
	static
	{
		List<StructuralPropertyDescriptor> propertyList = new ArrayList<StructuralPropertyDescriptor>(3);
		createPropertyList(WithinStatement.class, propertyList);
        addProperty(TEAM_EXPRESSION_PROPERTY, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
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

    
    private Expression teamExpression = null;
    
	/**
	 * The body statement; lazily initialized; defaults to an empty block 
	 * statement. This is an addition to the statements List.
	 */
	private Statement body = null;
	
	/**
	 * Creates a new withinStatement node owned by the given AST.
	 * By default, the withinStatement is empty.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
    WithinStatement(AST ast)
    {
        super(ast);
    }
    
    @SuppressWarnings("rawtypes")
	List internalStructuralPropertiesForType(int apiLevel)
    {
        return propertyDescriptors(apiLevel);
    }
    
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean isGet, ASTNode child)
    {
        if (property == BODY_PROPERTY)
        {
            if (isGet)
            {
                return getBody();
            }
            else
            {
                setBody((Statement) child);
                return null;
            }
        }
        if (property == TEAM_EXPRESSION_PROPERTY)
        {
            if (isGet)
            {
                return getTeamExpression();
            }
            else
            {
                setTeamExpression((Expression) child);
                return null;
            }
        
        }
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, isGet, child);
	}
	
    int getNodeType0()
    {
        return WITHIN_STATEMENT;
    }
    
    boolean subtreeMatch0(ASTMatcher matcher, Object other)
    {
		// dispatch to correct overloaded match method
        return matcher.match(this, other);
    }

	ASTNode clone0(AST target)
	{
		WithinStatement result = new WithinStatement(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.copyLeadingComment(this);
        result.setTeamExpression((Expression) getTeamExpression().clone(target));
		result.setBody((Statement) getBody().clone(target));
        
		return result;
	}

    void accept0(ASTVisitor visitor)
    {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren)
		{
            acceptChild(visitor, this.teamExpression);
		    acceptChild(visitor, this.body);
		}
		visitor.endVisit(this);
    }

	int memSize()
	{
	    // 3 * 4 == Number of single properties * 4
        return super.memSize() + 3 * 4 + this.body.memSize();
	}

    int treeSize()
    {
		return memSize() + this.body.treeSize();
    }
    
    /**
     * Returns the team exception of this within statement.
     * @return team exception
     */
    public Expression getTeamExpression()
    {
        if (this.teamExpression == null)
        {
            // lazy init must be thread-safe for readers
            synchronized (this) 
            {
                if (this.teamExpression == null) 
                {
                    preLazyInit();
                    this.teamExpression = new SimpleName(this.ast);
                    postLazyInit(this.teamExpression, TEAM_EXPRESSION_PROPERTY);
                }
            }
        }
        return this.teamExpression;
        
    }
    
    /**
     * Sets the expression of this while statement.
     * 
     * @param expression the expression node
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the node belongs to a different AST</li>
     * <li>the node already has a parent</li>
     * <li>a cycle in would be created</li>
     * </ul>
     */ 
    public void setTeamExpression(Expression expression)
    {
        if (expression == null)
        {
            throw new IllegalArgumentException();
        }
        ASTNode oldChild = this.teamExpression;
        preReplaceChild(oldChild, expression, TEAM_EXPRESSION_PROPERTY);
        this.teamExpression = expression;
        postReplaceChild(oldChild, expression, TEAM_EXPRESSION_PROPERTY);
    }

	/**
	 * Returns the body of this within statement.
	 * 
	 * @return the body statement node
	 */ 
	public Statement getBody()
    {
        if (this.body == null)
        {
            // lazy init must be thread-safe for readers
            synchronized (this)
            {
                if (this.body == null)
                {
                    preLazyInit();
                    this.body = new Block(this.ast);
                    postLazyInit(this.body, BODY_PROPERTY);
                }
            }
        }
        return this.body;
    }
    
	/**
	 * Sets the body of this within statement.
	 * <p>
	 * The Following is true for while statements (for within, too? (mkr)):<br />
	 * Special note: The Java language does not allow a local variable declaration
	 * to appear as the body of a while statement (they may only appear within a
	 * block). However, the AST will allow a <code>VariableDeclarationStatement</code>
	 * as the body of a <code>WithinStatement</code>. To get something that will
	 * compile, be sure to embed the <code>VariableDeclarationStatement</code>
	 * inside a <code>Block</code>.
	 * </p>
	 * 
	 * @param statement the body statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Statement statement) 
    {
		if (statement == null) 
        {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.body;
		preReplaceChild(oldChild, statement, BODY_PROPERTY);
		this.body = statement;
		postReplaceChild(oldChild, statement, BODY_PROPERTY);
	}
}
