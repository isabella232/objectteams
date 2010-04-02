/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: GuardPredicateDeclaration.java 23417 2010-02-03 20:13:55Z stephan $
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
 * This class represents a declaration of a guard predicate in OT/J (OTJLD §5.4).
 * 
 * @author stephan
 */
public class GuardPredicateDeclaration extends ASTNode {

	/**
	 * Structural property for the boolean expression that represents the guard's implementation.
	 */
	public static final ChildPropertyDescriptor EXPRESSION_PROPERTY =
		new ChildPropertyDescriptor(GuardPredicateDeclaration.class, "expression", Expression.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

    /**
     * The "base" structural property of this node type, marking whether it is a "base guard" or not.
     */
    public static final SimplePropertyDescriptor BASE_PROPERTY = 
        new SimplePropertyDescriptor(GuardPredicateDeclaration.class, "base", boolean.class, MANDATORY); //$NON-NLS-1$

    
	private static final List PROPERTY_DESCRIPTORS_3_0;
	
	static {
		List propertyList = new ArrayList(3);
		createPropertyList(GuardPredicateDeclaration.class, propertyList);
		addProperty(BASE_PROPERTY, propertyList);
		addProperty(EXPRESSION_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
	}
	
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			throw new UnsupportedOperationException("JLS2 not supported"); //$NON-NLS-1$
		} else {
			return PROPERTY_DESCRIPTORS_3_0;
		}
	}
	
	private Expression expression = null;
	private boolean isBase = false;
	
	GuardPredicateDeclaration(AST ast)
	{
		super(ast);
	}
	
	@Override
    boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value)
    {
        if (property == BASE_PROPERTY) {
            if (get) {
                return isBase();
            } else {
                setBase(value);
                return false;
            }       
        }
        // allow default implementation to flag the error
        return super.internalGetSetBooleanProperty(property, get, value);
    }

	@Override
    ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child)
    {
		if (property == EXPRESSION_PROPERTY) {
			if (get) {
				return getExpression();
			} else {
				setExpression((Expression)child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
    }

	@Override
	int getNodeType0() {
		return GUARD_PREDICATE_DECLARATION;
	}
	
	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
				unsupportedIn2();
			}
			if (this.ast.apiLevel >= AST.JLS3) {
				acceptChild(visitor, this.expression);
			}
		}
		visitor.endVisit(this);
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
        // dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@Override
	ASTNode clone0(AST target) {
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) 
			unsupportedIn2();
		GuardPredicateDeclaration result = new GuardPredicateDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setBase(this.isBase());
		result.setExpression((Expression)ASTNode.copySubtree(target, getExpression()));
		return result;
	}
	
	public boolean isBase()
	{
		return isBase;
	}

	public void setBase(boolean isBase)
	{
		preValueChange(BASE_PROPERTY);
		this.isBase = isBase; 
		postValueChange(BASE_PROPERTY);
	}


	public ASTNode getExpression() {
		if (this.expression  == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.expression == null) {
					preLazyInit();
					this.expression = new SimpleName(this.ast);
					postLazyInit(this.expression, EXPRESSION_PROPERTY);
				}
			}
		}
		return this.expression;
	}
	
	public void setExpression(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.expression;
		preReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
		this.expression = expression;
		postReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
	}

	@Override
	List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	
	@Override
	void appendDebugString(StringBuffer buffer) {
		if (this.isBase)
			buffer.append("base "); //$NON-NLS-1$
		buffer.append("when ("); //$NON-NLS-1$
		this.expression.appendDebugString(buffer);
		buffer.append(")"); //$NON-NLS-1$
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE; // FIXME(SH) + plus some constant??
	}

	@Override
	int treeSize() {
		return memSize() + (this.expression == null ? 0 : getExpression().treeSize());
	}

}
