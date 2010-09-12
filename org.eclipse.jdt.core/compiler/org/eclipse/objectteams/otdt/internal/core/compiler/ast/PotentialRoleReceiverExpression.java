/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2009 Stephan Herrmann.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.CompilationResult.CheckPoint;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;

/**
 * An expression wrapper providing two options for resolving:
 *  - direct
 *  - using a role reference as the implicit receiver
 * Used to wrap RHS expressions in callin parameter mappings.
 * 
 * @author stephan
 * @since 1.3.1
 */
public class PotentialRoleReceiverExpression extends Expression {

	Expression expression;
	char[] roleVarName;
	TypeReference roleClassRef;
	
	private Expression altExpression;
	
	public PotentialRoleReceiverExpression(Expression expression, char[] roleName, TypeReference roleClassRef) {
		super();
		this.expression = expression;
		this.roleVarName = roleName;
		this.roleClassRef = roleClassRef;
		this.sourceStart = expression.sourceStart;
		this.sourceEnd = expression.sourceEnd;
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo) {
		if (this.altExpression != null)
			return this.altExpression.analyseCode(currentScope, flowContext, flowInfo);
		else
			return this.expression.analyseCode(currentScope, flowContext, flowInfo);
	}
	@Override
	public void computeConversion(Scope scope, TypeBinding runtimeType, TypeBinding compileTimeType) {
		if (this.altExpression != null)
			this.altExpression.computeConversion(scope, runtimeType, compileTimeType);
		else
			this.expression.computeConversion(scope, runtimeType, compileTimeType);
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		if (this.altExpression != null)
			this.altExpression.generateCode(currentScope, codeStream, valueRequired);
		else
			this.expression.generateCode(currentScope, codeStream, valueRequired);
	}
	
	@Override
	public TypeBinding resolveType(BlockScope scope) {
		ReferenceContext referenceContext = scope.referenceContext();
		CompilationResult compilationResult = referenceContext.compilationResult();
		CheckPoint cp = compilationResult.getCheckPoint(referenceContext);
		
		// try normal:
		this.resolvedType = this.expression.resolveType(scope);
		if (this.resolvedType != null && this.resolvedType.isValidBinding())
			return this.resolvedType;
		
		// try alternative:
		TypeBinding altResult = null;
		AstGenerator gen = new AstGenerator(this.expression);
		if (this.expression instanceof PotentialLiftExpression) // that didn't help, only use one of lifting/role-scoping
			this.expression = ((PotentialLiftExpression)this.expression).expression;
		if (this.expression instanceof SingleNameReference) {
			this.altExpression = gen.fieldReference(
										gen.castExpression(
												gen.singleNameReference(this.roleVarName), 
												this.roleClassRef,
												CastExpression.NEED_CLASS),
										((SingleNameReference)this.expression).token);
			altResult = this.altExpression.resolveType(scope);
			// share resolved binding (helps the match locator)
			((NameReference)this.expression).binding = ((FieldReference)this.altExpression).binding;
		} else if (this.expression instanceof MessageSend) {
			MessageSend send = (MessageSend) this.expression;
			if (send.receiver.isThis()) {
				this.altExpression = gen.messageSend(gen.singleNameReference(this.roleVarName),
													 send.selector,
													 send.arguments);
				altResult = this.altExpression.resolveType(scope);
				// share resolved bindings (helps the match locator)
				((MessageSend)this.expression).binding = ((MessageSend)this.altExpression).binding;
				((MessageSend)this.expression).resolvedType = ((MessageSend)this.altExpression).resolvedType;
				((MessageSend)this.expression).actualReceiverType = ((MessageSend)this.altExpression).actualReceiverType;
			}
		}
		
		// evaluate results:
		if (altResult != null && altResult.isValidBinding()) {
			compilationResult.rollBack(cp);
			return this.resolvedType = altResult;
		}
		this.altExpression = null; // discard unsuccessful indirection
		return this.resolvedType;
	}
	
	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		return this.expression.printExpression(indent, output);
	}

}
