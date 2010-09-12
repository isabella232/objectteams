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
 * $Id: ParameterMapping.java 22581 2009-09-24 10:26:48Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/

package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import java.util.HashSet;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * NEW for OTDT:
 *
 * Mapping between an expression and an identifier (either direction).
 *
 * @author Markus Witte
 * @version $Id: ParameterMapping.java 22581 2009-09-24 10:26:48Z stephan $
 */
public class ParameterMapping extends Expression {

	public int direction;
	public Expression expression;
	public SingleNameReference ident;
	public HashSet<MethodSpec> isUsedForMethodSpec = new HashSet<MethodSpec>();

	public ParameterMapping(int direction, Expression expression, SingleNameReference ident) {
		this.direction=direction;
		this.expression=expression;
		this.ident=ident;
		if (direction == TerminalTokens.TokenNameBINDIN) {
			this.sourceStart= ident.sourceStart;
			this.sourceEnd=   expression.sourceEnd;
		} else {
			this.sourceStart= expression.sourceStart;
			this.sourceEnd=   ident.sourceEnd;
		}
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {
		if((this.direction==TerminalTokens.TokenNameBINDIN))// == CharOperation.equals(ident, IOTConstants.RESULT))
		{
			output.append(this.ident.token);
			output.append(" <- "); //$NON-NLS-1$
			this.expression.print(0,output);
		}
		else
		{
    		this.expression.print(indent,output);
			output.append(" -> "); //$NON-NLS-1$
    		output.append(this.ident.token);
		}

		return output;
	}

	public boolean isUsedFor(MethodSpec sourceMethodSpec) {
		return this.isUsedForMethodSpec.contains(sourceMethodSpec);
	}

	public void setUsedFor(MethodSpec sourceMethodSpec) {
		this.isUsedForMethodSpec.add(sourceMethodSpec);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#traverse(org.eclipse.jdt.internal.compiler.ASTVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(
		ASTVisitor visitor,
		BlockScope scope)
    {
        if (visitor.visit(this, scope))
            this.expression.traverse(visitor, scope);
        visitor.endVisit(this, scope);
	}

	// store resolved argment to link with ident, also hook for assist
	protected void argumentsResolved(MethodSpec spec) {
		if (spec instanceof FieldAccessSpec) {
			if (CharOperation.equals(((FieldAccessSpec)spec).selector, this.ident.token))
				this.ident.binding = ((FieldAccessSpec)spec).resolvedField;
		} else if (spec.arguments != null) {
			for (Argument arg : spec.arguments)
				if (CharOperation.equals(arg.name, this.ident.token))
					this.ident.binding = arg.binding;
		}
	}
}