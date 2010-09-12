/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ReplaceSingleNameVisitor.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;

/**
 * MIGRATION_STATE: complete.
 *
 * Replace a given expression for all occurrences of a given SingleNameReference
 * @author stephan
 */
public class ReplaceSingleNameVisitor
    extends AbstractTransformStatementsVisitor
{
    private IExpressionProvider _provider;
    private char[]              _name;
    private boolean             _hasChanges;

    /**
     * Create a visitor that will search for each occurence of `name' and
     * replace it by an expression provided by `provider'.
     * @param name
     * @param provider
     */
    public ReplaceSingleNameVisitor(char[] name, IExpressionProvider provider)
    {
        this._name     = name;
        this._provider = provider;
    }

    /**
     *
     * @param node
     * @param scope
     * @param oldName
     * @param newName
     * @return        Has any replacement been performed?
     */
    public static boolean performReplacement(ASTNode node, BlockScope scope, char[] oldName, final char[] newName)
    {
    	final int start = node.sourceStart;
    	IExpressionProvider provider = new IExpressionProvider() {
			public Expression newExpression() {
                return new SingleNameReference(newName, start);
			}
    	};
    	ReplaceSingleNameVisitor replaceSingleNameVisitor = new ReplaceSingleNameVisitor(oldName, provider);
		node.traverse(replaceSingleNameVisitor, scope);
    	return replaceSingleNameVisitor._hasChanges;
    }
    /**
     * An expression must create a new Expression on each invocation, since
     * the AST must be a strict tree without sharing.
     * @author stephan
     */
    public static interface IExpressionProvider {
        public Expression newExpression();
    }
    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.AbstractTransformStatementsVisitor#pushExpression(org.eclipse.jdt.internal.compiler.ast.Expression, org.eclipse.jdt.internal.compiler.ast.Expression)
     */
    protected void enterExpression(Expression oldExpr, Expression newExpr, Statement node) {
        throw new InternalCompilerError("Method not applicable"); //$NON-NLS-1$
    }
    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.AbstractTransformStatementsVisitor#testExpression(org.eclipse.jdt.internal.compiler.ast.Statement)
     */
    protected boolean testExpression(Statement oldExpr) {
        if (!(oldExpr instanceof SingleNameReference))
            return false;
        return CharOperation.equals(((SingleNameReference)oldExpr).token, this._name);
    }

    protected void assertAllConsumed(ASTNode node) {/* replacement is not dynamically entered nor consumed */}

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.AbstractTransformStatementsVisitor#consumeExpression()
     */
    protected Expression consumeExpression() {
    	this._hasChanges = true;
        return this._provider.newExpression();
    }
    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.AbstractTransformStatementsVisitor#checkConsumeExpression(org.eclipse.jdt.internal.compiler.ast.Expression)
     */
    protected Expression checkConsumeExpression(Expression oldExpr, BlockScope scope) {
        if (testExpression(oldExpr)) {
        	this._hasChanges = true;
            return this._provider.newExpression();
        }
        return oldExpr;
    }
    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.AbstractTransformStatementsVisitor#checkConsumeStatement(org.eclipse.jdt.internal.compiler.ast.Statement)
     */
    protected Statement checkConsumeStatement(Statement oldStat, BlockScope scope) {
        if (testExpression(oldStat)) {
        	this._hasChanges = true;
            return this._provider.newExpression();
        }
        return oldStat;
    }


}
