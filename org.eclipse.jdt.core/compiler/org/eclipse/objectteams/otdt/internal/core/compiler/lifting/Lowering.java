/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Lowering.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lifting;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.StandardElementGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;

/**
 * MIGRATION_STATE: complete, fixmes remain.
 * moved here from org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.
 *
 * Helper that generates the lowering statements necessary for the callout compiler-feature.
 *
 * Nested classes PushingExpression and PopExpression are used to store a value on
 * the stack that must be evaluated only once but is first used for a null-check
 * and later the same value is used for the actual translation.
 *
 * @author haebor
 * @version $Id: Lowering.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class Lowering implements IOTConstants {

	/**
	 * This method generates a lowering translation for a given expression.
	 * We have three distinct alternatives:
	 * (1) arrays are translated via a method call (see ArrayLowering).
	 * (2) field access (applicable for same and sibling roles).
	 * (3) _OT$getBase() method (else).
	 *
	 * @param scope for lookup
	 * @param expression original
	 * @param unloweredType either a RoleTypeBinding or an ArrayBinding thereof.
	 * @param requiredType  the base type (or ArrayBinding thereof).
	 * @param needNullCheck if expression maybe null, a null-check is required at runtime.
	 * @return translation expression
	 */
	public Expression lowerExpression(
              BlockScope  scope,
              final Expression  expression,
		      TypeBinding unloweredType,
			  TypeBinding requiredType,
			  boolean     needNullCheck)
	{
        // Note, this method is responsible for 'resolving' all AST nodes it generates!

        int sourceStart = expression.sourceStart;
        int sourceEnd   = expression.sourceEnd;
    	AstGenerator gen = new AstGenerator(sourceStart, sourceEnd);

    	unloweredType = TeamModel.strengthenRoleType(scope.enclosingSourceType(), unloweredType);
        Expression loweringExpr = null;

        TypeBinding expressionType = expression.resolvedType;
        // this assertion needed for pushing/casting using unchecked one-byte opcodes.
        assert expressionType == null || expressionType.leafComponentType() instanceof ReferenceBinding;

    	PushingExpression provider = new PushingExpression(expression);

        if(unloweredType.isArrayType())
		{
        	// (1) array translation:
			ArrayLowering trans = new ArrayLowering();
			loweringExpr =  trans.lowerArray(
								scope,
								needNullCheck ?
										new PopExpression(expressionType, unloweredType, provider) :
										expression,
								unloweredType,
								requiredType);
		}
		else
		{
            RoleTypeBinding roleType = (RoleTypeBinding)unloweredType;
			boolean needMethod =   roleType.isRegularInterface()
								|| !roleType.isSiblingRole(scope.enclosingSourceType());
            if (needMethod) {
            	// (3) translation using _OT$.getBase() method:
				MessageSend callLower = gen.messageSend(
                		needNullCheck ?
                				new PopExpression(expressionType, unloweredType, provider) :
                				expression,
                		IOTConstants._OT_GETBASE,
						new Expression[0]);

                // manual resolving:
                callLower.actualReceiverType = unloweredType;
                callLower.constant       = Constant.NotAConstant;
                callLower.resolvedType   = roleType.baseclass();

                callLower.binding =
                            StandardElementGenerator.getGetBaseMethod(
                            		scope,
                                    roleType.roleModel,
                                    roleType.baseclass());
                loweringExpr = callLower;
            } else {
                // (2) translation using field _OT$base:
                FieldReference invokeBaseOnRole =
                    new FieldReference(IOTConstants._OT_BASE, (((long)sourceStart) << 32) + sourceEnd);
                ReferenceBinding roleClass = roleType.roleModel.getClassPartBinding();

                if (needNullCheck) {
                    invokeBaseOnRole.receiver  =
							new PopExpression(expressionType, roleClass, provider); // this includes the cast.
                } else {
	                TypeReference classRef = gen.typeReference(roleClass);
	                if (classRef != null) {
	                	// field access needs cast to the role-class.
	                	// FIXME(SH): use synthetic role field accessor instead!
	                	CastExpression unloweredExpr;
	                    unloweredExpr = new CastExpression(expression, classRef, CastExpression.NEED_CLASS);
	                    unloweredExpr.constant     = Constant.NotAConstant;
	                    unloweredExpr.resolvedType = roleClass;
						unloweredExpr.bits        |= ASTNode.GenerateCheckcast;
		                invokeBaseOnRole.receiver  = unloweredExpr;
	                } else {
	                	invokeBaseOnRole.receiver  = expression;
	                }
                }

                invokeBaseOnRole.actualReceiverType = roleClass;
                invokeBaseOnRole.resolvedType       = roleClass.baseclass();
                invokeBaseOnRole.binding = scope.findField(
                                        			  roleClass,
                                        			  IOTConstants._OT_BASE,
                                        			  invokeBaseOnRole,
                                        			  true);
                invokeBaseOnRole.constant           = Constant.NotAConstant;
               	loweringExpr = invokeBaseOnRole;
            }
		}
        if (needNullCheck) {
        	// ((expression)<pushed> == null) ? (RequiredType)<pop> : lower(<pop>).
			loweringExpr = new LoweringConditional(
					expression,
					gen.nullCheck(provider),
					new PopExpression(expressionType, requiredType, provider), // casted null instead of lowering
					loweringExpr); // contains a PopExpression as receiver
			loweringExpr.constant = Constant.NotAConstant;
			loweringExpr.resolvedType = requiredType;
        }
        return loweringExpr;
	}
	
	/**
     * Special conditional expression that redirects code-gen (bypass all push/pop business) if no value is required.
     */
	class LoweringConditional extends ConditionalExpression {
		private Expression origExpression;
		LoweringConditional (Expression origExpression, Expression condition, Expression valueIfTrue, Expression valueIfFalse)
		{
			super(condition, valueIfTrue, valueIfFalse);
			this.origExpression = origExpression;
		}
		
		public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
			if (!valueRequired) 
				// avoid fiddling with the stack for unused values, only create the side-effects of expression:
				origExpression.generateCode(currentScope, codeStream, valueRequired);
			else
				super.generateCode(currentScope, codeStream, valueRequired);
		}	
	}
	
	public static boolean isLoweringConditional(Expression expr) {
		return expr instanceof LoweringConditional;
	}

	/**
	 * A wrapper for an expression that needs to be duplicated on the stack for later reuse.
	 */
	class PushingExpression extends Expression {
		// the wrapped expression
		Expression expression;
		// stack level where this value resides (even after consuming this expression).
		int stackDepth = -1;

		/**
		 * @param expression real expression to wrap.
		 */
		PushingExpression (Expression expression) {
			this.expression = expression;
			this.constant = Constant.NotAConstant;
			this.sourceStart = expression.sourceStart;
			this.sourceEnd   = expression.sourceEnd;
		}
		/** Simply forward. */
		public void traverse(ASTVisitor visitor, BlockScope scope) {
		    this.expression.traverse(visitor, scope);
		}

		/** Simply forward. */
		public FlowInfo analyseCode(BlockScope  currentScope, FlowContext flowContext, FlowInfo    flowInfo) {
		    return this.expression.analyseCode(currentScope, flowContext, flowInfo);
		}

		/** Simply forward. */
		public TypeBinding resolveType(BlockScope scope) {
			if (this.constant == null)
				this.constant = Constant.NotAConstant;
			return this.resolvedType = this.expression.resolveType(scope);
		}

		/** Forward and duplicate. */
		public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired)
		{
			this.expression.generateCode(currentScope, codeStream, valueRequired);

			this.stackDepth = codeStream.stackDepth; // remember stack level

			// The following line is the actual purpose of this class:
			codeStream.dup();
		}

		/** Mainly forward. */
		@SuppressWarnings("nls")
		public StringBuffer printExpression(int indent, StringBuffer output) {
			output.append("(");
			this.expression.printExpression(indent, output);
			output.append(")<pushed>");
			return output;
		}

	}

	/** A placeholder for a value that is not computed but already exists on the stack.
	 *  Only a type cast might be needed to reuse that value.
	 *  However, the needed value might be burried by another value,
	 *  in that case retrieve it using a swap bytecode.
	 */
	static class PopExpression extends Expression {

		// a previously pushed null may need to be re-interpreted by a cast:
		private boolean castRequired;

		// the expression that has produced (dup-ed) the value of this expression.
		PushingExpression provider;

		/**
		 * @param pushedType This type was pushed before.
		 * @param popType    This type is needed now.
		 * @param provider   The expression that has produced (dup-ed) the value of this expression.
		 */
		PopExpression (TypeBinding pushedType, TypeBinding popType, PushingExpression provider)
		{
			this.resolvedType = popType;
			if (popType == null)
				this.resolvedType = pushedType;
			else
				this.castRequired = pushedType != popType;
			this.provider = provider;
			this.constant = Constant.NotAConstant;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jdt.internal.compiler.ast.Expression#printExpression(int, java.lang.StringBuffer)
		 */
		@SuppressWarnings("nls")
		public StringBuffer printExpression(int indent, StringBuffer output) {
			output.append("<pop>");
			return output;
		}
		public TypeBinding resolveType(BlockScope scope) {
			return this.resolvedType;
		}

		public void generateCode(
				BlockScope currentScope,
				CodeStream codeStream,
				boolean valueRequired)
		{
			// don't create a value, operand is already on stack.

			// just look where exactly the value resides:
			if (this.provider.stackDepth + 1 == codeStream.stackDepth) // maximum difference is 1.
			{
				// value is burried, swap now:
				codeStream.swap();
			} else {
				assert this.provider.stackDepth == codeStream.stackDepth; // value is already in position.
			}

			if (this.castRequired) // might only need to be casted.
				codeStream.checkcast(this.resolvedType);
		}
	}

	public static boolean isPopExpression (Expression expression) {
		return expression instanceof PopExpression;
	}

	/**
	 * Extract the actual value, considering wrapping by PushingExpression/PopExpression.
	 *
	 * @param expression
	 * @return the actual value to be lowered
	 */
	public static Expression unwrapExpression(Expression expression) {
		if (expression instanceof PushingExpression)
			return ((PushingExpression)expression).expression;
		if (expression instanceof PopExpression)
			return ((PopExpression)expression).provider.expression;
		return null;
	}
}
