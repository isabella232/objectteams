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
 * $Id: BaseCallMessageSend.java 23416 2010-02-03 19:59:31Z stephan $
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
 * BaseCallMessageSend represents a 'base call'
 * inside a role method with 'callin' modifier (OTJLD §4.3),
 * e.g.
 * <code>base.myRoleMethod(arg1, arg2)</code>
 *
 * Contained AST elements:
 * a selector name (<code>SimpleName</code>), e.g. <code>myRoleMethod<code>.
 * a List of argument expressions (<code>Expression</code>).
 * 
 * @author mkr
 * @version $Id: BaseCallMessageSend.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class BaseCallMessageSend extends Expression
{
    /**
     * The "name" structural property of this node type.
     * @since 3.0
     */
    public static final ChildPropertyDescriptor NAME_PROPERTY = 
        new ChildPropertyDescriptor(BaseCallMessageSend.class,
                                    "name",//$NON-NLS-1$
                                    SimpleName.class,
                                    MANDATORY,
                                    NO_CYCLE_RISK); 

    /**
     * The "arguments" structural property of this node type.
     * @since 3.0
     */
    public static final ChildListPropertyDescriptor ARGUMENTS_PROPERTY = 
        new ChildListPropertyDescriptor(BaseCallMessageSend.class,
                                        "arguments",//$NON-NLS-1$
                                        Expression.class,
                                        CYCLE_RISK); 

    
    
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static
	{
		List propertyList = new ArrayList(3);
		createPropertyList(BaseCallMessageSend.class, propertyList);
        addProperty(NAME_PROPERTY, propertyList);
        addProperty(ARGUMENTS_PROPERTY, propertyList);
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
     * The method name; lazily initialized;
     */
    private SimpleName _methodName = null;
    
    /**
     * The list of argument expressions (element type: 
     * <code>Expression</code>). Defaults to an empty list.
     */
    private ASTNode.NodeList _arguments =
        new ASTNode.NodeList(ARGUMENTS_PROPERTY);

	/**
	 * Creates a new AST node for a tsuper expression owned by the given AST.
	 * By default, there is no qualifier.
	 * 
	 * @param ast the AST that is to own this node
	 */
	BaseCallMessageSend(AST ast)
	{
		super(ast);
	}

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
        }
        
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
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
		return BASE_CALL_MESSAGE_SEND;
	}

	@SuppressWarnings("unchecked")
	ASTNode clone0(AST target)
	{
		BaseCallMessageSend result = new BaseCallMessageSend(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
        result.setName((SimpleName)_methodName.clone(target));
        result.getArguments().addAll(ASTNode.copySubtrees(target, _arguments));
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
        if (visitChildren) {
            // visit children in normal left to right reading order
            acceptChild(visitor, _methodName);
            acceptChildren(visitor, _arguments);
        }
        visitor.endVisit(this);
	}
	
	
    /**
     * Returns the name of the method invoked in this expression.
     * 
     * @return the method name node
     */ 
    public SimpleName getName()
    { 
        if (_methodName == null)
        {
            // lazy init must be thread-safe for readers
            synchronized (this)
            {
                if (_methodName == null)
                {
                    preLazyInit();
                    _methodName = new SimpleName(this.ast);
                    postLazyInit(_methodName, NAME_PROPERTY);
                }
            }
        }
        return _methodName;
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
        ASTNode oldChild = _methodName;
        preReplaceChild(oldChild, name, NAME_PROPERTY);
        _methodName = name;
        postReplaceChild(oldChild, name, NAME_PROPERTY);
    }
    
    /**
     * Returns the ordered list of argument expressions in this 
     * base call method send expression.
     * 
     * @return the live list of argument expressions 
     *    (element type: <code>Expression</code>)
     */ 
    public List getArguments() 
    {
        return _arguments;
    }

    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    int memSize() 
    {
        return BASE_NODE_SIZE + 4 * 4; 
    }
    
    /* (omit javadoc for this method)
     * Method declared on ASTNode.
     */
    int treeSize() 
    {
        return 
            memSize()
            + (_methodName == null ? 0 : _methodName.treeSize())
            + (_arguments  == null ? 0 : _arguments.listSize());
    }

    public IMethodBinding resolveMethodBinding()
    {
        return this.ast.getBindingResolver().resolveMethod(this);
    }
    
    
    
}
