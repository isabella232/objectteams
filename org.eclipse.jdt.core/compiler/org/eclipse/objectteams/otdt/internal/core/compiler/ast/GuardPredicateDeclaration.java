/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: FakedBaseMessageSend.java 14480 2006-10-08 15:08:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.BaseScopeMarker;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.PredicateGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;

/**
 * Small extension of MethodDeclarations representing a guard predicate.
 *
 * Main purpose: avoid previous heuristics where different kinds of predicates where
 *               distinguished by their generated names.
 *
 * @author stephan
 * @since 0.9.25
 */
public class GuardPredicateDeclaration extends MethodDeclaration {

	public static final int UNKNOWN_PREDICATE = 0;
	public static final int TYPE_PREDICATE = 1;
	public static final int BINDING_PREDICATE = 2;
	public static final int METHOD_PREDICATE = 3;

	/** One of the above constants (UNKNOWN_, TYPE_, BINDING_, METHOD_PREDICATE). */
	public int kind;

	// direct access to the main statement, which will be burried within a try-catch:
	public ReturnStatement returnStatement;

	public boolean isBasePredicate;

	public GuardPredicateDeclaration(CompilationResult compilationResult,
									 char[] methodName,
									 boolean isBasePredicate,
									 int start, int end)
	{
		super(compilationResult);
		this.selector = methodName;
		this.isBasePredicate = isBasePredicate;
		this.sourceStart = start;
		this.sourceEnd   = end;
		this.declarationSourceStart = start;
		this.declarationSourceEnd   = end;
		this.isGenerated = true; // never show as a method
		this.isReusingSourceMethod= true; // although generated, our statement is a source statement/expression.
		this.returnType = new SingleTypeReference(TypeConstants.BOOLEAN, ((((long)start)<<32)+end));
	}

	public GuardPredicateDeclaration(CompilationResult compilationResult,
									 char[] methodName,
									 int kind, boolean typeIsRole, boolean isBasePredicate,
									 int start, int end)
	{
		this(compilationResult, methodName, isBasePredicate, start, end);
		this.kind = kind;
		this.modifiers = ClassFileConstants.AccSynchronized|ClassFileConstants.AccProtected;
		if (isBasePredicate && (typeIsRole || kind != GuardPredicateDeclaration.TYPE_PREDICATE))
			this.modifiers |= ClassFileConstants.AccStatic;
	}

	public void updatePredicateExpression(Expression expression, int declarationSourceEnd) {
		int s = expression.sourceStart;
		int e = expression.sourceEnd;
		AstGenerator gen = new AstGenerator(s,e);
		this.returnStatement = new ReturnStatement(expression, s, e);
		// handle OTJLD 5.4(c) by wrapping with a try-catch:
		this.statements = new Statement[]{
			tryCatch(this.returnStatement,
					 gen.qualifiedTypeReference(JAVA_LANG_THROWABLE),
					 gen.returnStatement(gen.booleanLiteral(false)))
		};
		this.hasParsedStatements = true;
		this.bodyStart = s;
		this.bodyEnd = e; // end of expression
		this.declarationSourceEnd = declarationSourceEnd; // behind bodyEnd
	}

	@Override
	protected void linkPredicates() {
		if ((this.modifiers & ClassFileConstants.AccAbstract) == 0)
			PredicateGenerator.linkPredicates(this);
	}

	public boolean isBasePredicate() {
		return this.isBasePredicate;
	}

	public Expression expression() {
		if (this.returnStatement == null)
			return null;
		return this.returnStatement.expression;
	}

	/**
	 * If an unresolvable name occurred in a predicate body, check whether this
	 * might be caused by missing signatures in the method binding.
	 *
	 * @param arguments prepared strings from the callin error-handling method.
	 * @param start
	 * @param end
	 * @return true if a specific error has been reported.
	 */
	public boolean handleMissingSignature(String[] arguments, int start, int end)
	{
		if (this.scope == null)
			return false;
		TypeDeclaration enclosingType = this.scope.referenceType();
		if (enclosingType.callinCallouts == null)
			return false;
		for (AbstractMethodMappingDeclaration mapping : enclosingType.callinCallouts) {
			if (mapping.isCallin() && !mapping.hasSignature) {
				this.scope.problemReporter().predicateHasNoArguments(arguments, start, end);
				return true;
			}
		}
		return false;
	}

	// construct a specialized try-catch for handling OTJLD 5.4(c)
	Statement tryCatch(Statement tryStatement, TypeReference exceptionType, Statement catchStatement)
	{
		TryStatement result = new TryStatement() {
			@Override
			protected ExceptionHandlingFlowContext createFlowContext(FlowContext flowContext, FlowInfo flowInfo) {
				return new ExceptionHandlingFlowContext(
							flowContext,
							this,
							Binding.NO_EXCEPTIONS, // treat all exceptions as undeclared, want to see the error/warning
							null, // initializationParent
							this.scope,
							flowInfo.unconditionalInits());
			}
		};
		result.sourceStart = this.sourceStart;
		result.sourceEnd   = this.sourceEnd;
		// fill sub-elements:
		AstGenerator gen = new AstGenerator(this.sourceStart, this.sourceEnd);
		result.tryBlock       = gen.block(new Statement[] {tryStatement});
		result.catchArguments = new Argument[] { gen.argument("exc".toCharArray(), exceptionType) }; //$NON-NLS-1$
		result.catchBlocks    = new Block[] { gen.block(new Statement[]{catchStatement}) };
		return result;
	}

	@Override
	public void resolveStatements() {
		if (this.returnStatement != null)
			// prepare for baseclass decapsulation:
			this.returnStatement.traverse(new BaseScopeMarker(), this.scope);
		super.resolveStatements();
	}
	@Override
	public StringBuffer printBody(int indent, StringBuffer output) {
		output.append(" {\n"); //$NON-NLS-1$

		// selective printing because we don't want to see the generated try-catch.
		this.returnStatement.printStatement(indent, output);

		output.append('\n');
		printIndent(indent == 0 ? 0 : indent - 1, output).append('}');
		return output;
	}
}
