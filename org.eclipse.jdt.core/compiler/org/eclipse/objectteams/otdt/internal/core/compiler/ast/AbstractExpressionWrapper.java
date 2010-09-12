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
 * $Id: AbstractExpressionWrapper.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.problem.IProblemReporterWrapperFactory;


/**
 * NEW for OTDT
 *
 * @author gis
 * @version $Id: AbstractExpressionWrapper.java 23401 2010-02-02 23:56:05Z stephan $
 */
public abstract class AbstractExpressionWrapper extends Expression implements IProblemReporterWrapperFactory
{
	public Expression _wrappee;

	public AbstractExpressionWrapper()
	{
		this.constant = Constant.NotAConstant;
	}

    public AbstractExpressionWrapper(Expression wrappee, int sStart, int sEnd)
    {
        super();
        this._wrappee = wrappee;
        this._wrappee.sourceStart = sStart;
        this._wrappee.sourceEnd = sEnd;

		this.sourceStart = sStart;
		this.sourceEnd   = sEnd;

		this.constant = Constant.NotAConstant;
	}

	public StringBuffer printExpression(int indent, StringBuffer output)
	{
		return this._wrappee.printExpression(indent, output);
	}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo)
	{
		return this._wrappee.analyseCode(currentScope, flowContext, flowInfo);
	}

	public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType)
	{
		super.computeConversion(scope, runtimeTimeType, compileTimeType);
		// pass the flag on to the wrapped expression
		this._wrappee.implicitConversion = this.implicitConversion;
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired)
	{
		this._wrappee.generateCode(currentScope, codeStream, valueRequired);
	}

	public TypeBinding resolveType(BlockScope scope)
	{
		if (this.constant == null)
			this.constant = Constant.NotAConstant;
		return this.resolvedType = this._wrappee.resolveType(scope);
	}

	/**
	 * Implement this using your concrete _wrappee.
	 */
	public abstract void traverse(ASTVisitor visitor, BlockScope scope);
}
