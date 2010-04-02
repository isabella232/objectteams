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
 * $Id: TSuperMessageSend.java 23416 2010-02-03 19:59:31Z stephan $
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
 * TSuperMessageSend represents a super call along the implicit
 * inheritance of roles (OTJLD §1.3.1(f)), 
 * e.g. <code>tsuper.m(arg1, arg2)</code> (only inside method <code>m</code> of a role method)
 * or <code>tsuper(arg1, arg2)</code> (only in a constructor of a role class).
 * 
 * Contained AST elements:
 * a selector name (<code>SimpleName</code>), e.g. <code>myRoleMethod</code>.
 * a List of argument expressions (<code>Expression</code>).
 * 
 * Locations in source code:
 * This node can only be used within role methods.
 * 
 * @author mkr
 */
public class TSuperMessageSend extends Expression
{
       /**
     * The "name" structural property of this node type.
     * @since 3.0
     */
    public static final ChildPropertyDescriptor NAME_PROPERTY = 
        new ChildPropertyDescriptor(TSuperMessageSend.class,
                                    "name",//$NON-NLS-1$
                                    SimpleName.class,
                                    MANDATORY,
                                    NO_CYCLE_RISK); 

    /**
     * The "arguments" structural property of this node type.
     * @since 3.0
     */
    public static final ChildListPropertyDescriptor ARGUMENTS_PROPERTY = 
        new ChildListPropertyDescriptor(TSuperMessageSend.class,
                                        "arguments",//$NON-NLS-1$
                                        Expression.class,
                                        CYCLE_RISK); 

	/**
	 * The "qualifier" structural property of this node type.
	 */
	public static final ChildPropertyDescriptor QUALIFIER_PROPERTY = 
		new ChildPropertyDescriptor(TSuperMessageSend.class, 
				                    "qualifier", //$NON-NLS-1$
									Name.class, 
									OPTIONAL, NO_CYCLE_RISK); 

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	@SuppressWarnings("rawtypes")
	private static final List PROPERTY_DESCRIPTORS;
	
	static
	{
        ArrayList<StructuralPropertyDescriptor> propertyList = new ArrayList<StructuralPropertyDescriptor>(4);
		createPropertyList(TSuperMessageSend.class, propertyList);
        addProperty(NAME_PROPERTY, propertyList);
        addProperty(ARGUMENTS_PROPERTY, propertyList);
		addProperty(QUALIFIER_PROPERTY, propertyList);
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
     * The method name; lazily initialized;
     */
    private SimpleName methodName = null;
    
    /**
     * The list of argument expressions (element type: 
     * <code>Expression</code>). Defaults to an empty list.
     */
    private ASTNode.NodeList arguments =
        new ASTNode.NodeList(ARGUMENTS_PROPERTY);

    /**
     * The tsuper-qualification; lazily initialized;
     */
    private Name optionalQualification = null;
    
	/**
	 * Creates a new AST node for a tsuper expression owned by the given AST.
	 * By default, there is no qualifier.
	 * 
	 * @param ast the AST that is to own this node
	 */
	TSuperMessageSend(AST ast)
	{
		super(ast);
	}

	@SuppressWarnings("rawtypes")
	final List internalStructuralPropertiesForType(int apiLevel)
	{
		return propertyDescriptors(apiLevel);
	}
	
    final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property,
                                              boolean get,
                                              ASTNode child)
    {
        if (property == NAME_PROPERTY)
        {
            if (get) {
                return getName();
            }
            else
            {
                setName((SimpleName) child);
                return null;
            }
        } else
        if (property == QUALIFIER_PROPERTY)
        {
            if (get) {
                return getQualifier();
            }
            else
            {
                setQualification((SimpleName) child);
                return null;
            }
        }
        
        // allow default implementation to flag the error
        return super.internalGetSetChildProperty(property, get, child);        
    }

    @SuppressWarnings("rawtypes")
	final List internalGetChildListProperty(ChildListPropertyDescriptor property)
    {
        if (property == ARGUMENTS_PROPERTY)
        {
            return getArguments();
        }
    
        // allow default implementation to flag the error
        return super.internalGetChildListProperty(property);
    }

	final int getNodeType0()
	{
		return TSUPER_MESSAGE_SEND;
	}

	@SuppressWarnings("unchecked")
	ASTNode clone0(AST target)
	{
		TSuperMessageSend result = new TSuperMessageSend(target);        
        result.setSourceRange(this.getStartPosition(), this.getLength());
        result.setName((SimpleName)this.methodName.clone(target));
        result.getArguments().addAll(ASTNode.copySubtrees(target, this.arguments));
        result.setQualification((Name)this.optionalQualification.clone(target));
        return result;        
    }
    
    /**
     * Returns the name of the method invoked in this expression.
     * 
     * @return the method name node
     */ 
    public SimpleName getName() 
    {
        if (this.methodName == null)
        {
            // lazy init must be thread-safe for readers
            synchronized (this)
            {
                if (this.methodName == null)
                {
                    preLazyInit();
                    this.methodName = new SimpleName(this.ast);
                    postLazyInit(this.methodName, NAME_PROPERTY);
                }
            }
        }
        return this.methodName;
    }
    
    /**
     * Sets the name of the method invoked in this expression to the
     * given name.
     * 
     * @param name the new method name
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the node belongs to a different AST</li>
     * <li>the node already has a parent</li>
     * </ul>
     */ 
    public void setName(SimpleName name) 
    {
        if (name == null)
        {
            throw new IllegalArgumentException();
        }
        ASTNode oldChild = this.methodName;
        preReplaceChild(oldChild, name, NAME_PROPERTY);
        this.methodName = name;
        postReplaceChild(oldChild, name, NAME_PROPERTY);
    }
    
    /**
     * Returns the ordered list of argument expressions in this 
     * base call method send expression.
     * 
     * @return the live list of argument expressions 
     *    (element type: <code>Expression</code>)
     */ 
    @SuppressWarnings("rawtypes")
	public List getArguments() 
    {
        return this.arguments;
    }

	/**
	 * Returns the qualifier of this qualified creation expression, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the qualifier name node, or <code>null</code> if there is none
	 */ 
	public Name getQualifier()
	{
		return this.optionalQualification;
	}

    
    /**
     * Sets the tsuper qualification
     * 
     * @param qualification the new qualification
     * @exception IllegalArgumentException if:
     * <ul>
     * <li>the node belongs to a different AST</li>
     * <li>the node already has a parent</li>
     * </ul>
     */ 
    public void setQualification(Name qualification) 
    {
        if (qualification == null)
        {
            throw new IllegalArgumentException();
        }
        ASTNode oldChild = this.optionalQualification;
        preReplaceChild(oldChild, qualification, QUALIFIER_PROPERTY);
        this.optionalQualification = qualification;
        postReplaceChild(oldChild, qualification, QUALIFIER_PROPERTY);
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
            acceptChild(visitor, this.optionalQualification);
            acceptChild(visitor, this.methodName);
            acceptChildren(visitor, this.arguments);
        }
        visitor.endVisit(this);
    }

	int memSize()
	{
		// treat Operator as free
		return BASE_NODE_SIZE + 4 * 4;
	}
	
    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    int treeSize() 
    {
        return 
            memSize()
            + (this.methodName == null ? 0 : this.methodName.treeSize())
            + (this.arguments  == null ? 0 : this.arguments.listSize());
    }
    
    public IMethodBinding resolveMethodBinding() {
        return this.ast.getBindingResolver().resolveMethod(this);
    }
    
}
