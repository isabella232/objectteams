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
 * $Id: PotentialTranslationExpression.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;

/**
 * NEW for OTDT.
 *
 * Wrapper nodes for expression that might (or might not) need translation for conformance
 * (lifting or lowering).
 *
 * Why: Whether lifting/lowring is needed can only be decided during resolve.
 *
 * @author stephan
 * @version $Id: PotentialTranslationExpression.java 23401 2010-02-02 23:56:05Z stephan $
 */
public abstract class PotentialTranslationExpression extends Expression  implements IOTConstants{


	public Expression expression;
	protected TypeBinding expectedType;
	protected boolean checked = false;
	protected Expression rawExpression = null; // just for pretty-printing
	protected String operator;                 // just for pretty-printing

    public PotentialTranslationExpression(Expression  expression, TypeBinding expectedType)
    {
        super(); // does nothing ;-)
        this.expression   = expression;
        this.expectedType = expectedType;
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
	@Override
	public int nullStatus(FlowInfo flowInfo) {
		return this.expression.nullStatus(flowInfo);
	}

	/** Simply forward. */
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	    this.expression.generateCode(currentScope, codeStream, valueRequired);
	}

	/**
	 * Check whether rawType is already compatible perhaps using basic type conversion
	 * @param scope
	 * @param rawType
	 * @return the compatible type
	 */
	protected TypeBinding compatibleType(BlockScope scope, TypeBinding rawType) {
		// save and reset flags:
    	Config oldConfig = Config.createOrResetConfig(this);

		try {
		    if (areTypesCompatible(rawType, this.expectedType)) {
		        if (!Config.requireTypeAdjustment()) {
		            // TODO (SH) is conversion of arrays of base type allowed?
	            	TypeBinding resultType = this.resolvedType; // default
		            if (this.resolvedType.isBaseType()) {
		                if (rawType != this.expectedType) {
		                    this.rawExpression = this.expression;
		                    this.rawExpression.computeConversion(scope, rawType, rawType); // null conversion.

		                    this.expression = new CastExpression(
		                            this.expression,
		                            TypeReference.baseTypeReference(this.expectedType.id,0));
		                    this.expression.constant = Constant.NotAConstant;
		                    ((CastExpression)this.expression).checkCastTypesCompatibility(
		                            scope,
		                            this.expectedType,
		                            rawType,
		                            this.expression);
		                    this.operator = "(convert to "+new String(this.expectedType.readableName())+")"; //$NON-NLS-1$ //$NON-NLS-2$
		                    resultType = this.expectedType;
		                }
		            }
	                if (   BaseTypeBinding.isWidening(this.expectedType.id, rawType.id)
	                    && this.expression.constant != Constant.NotAConstant)
	                	this.expression.computeConversion(scope, this.expectedType, rawType);
		            return resultType;
		        }
		    }
		} finally {
		    // restore on any exit:
	    	Config.removeOrRestore(oldConfig, this);
		}
	    return null;
	}

	private boolean areTypesCompatible(TypeBinding thisType, TypeBinding thatType) {
	    if (    RoleTypeBinding.isRoleType(thisType)
	         && RoleTypeBinding.isRoleType(thatType))
	    {
	        // ignore anchors for now:
	    	thisType = ((ReferenceBinding)thisType).getRealClass();
	        thatType = ((ReferenceBinding)thatType).getRealClass();
	    }
	    return thisType.isCompatibleWith(thatType);
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {
	    if (this.checked && this.rawExpression != null)
	    {
	        output.append(this.operator+"("); //$NON-NLS-1$
	    	this.rawExpression.printExpression(indent,output);
	    	output.append(")"); //$NON-NLS-1$
	    }
	    else
	    {
	    	output.append(this.operator+"?("); //$NON-NLS-1$
	    	this.expression.printExpression(indent,output);
	    	output.append(")"); //$NON-NLS-1$
	    }
	    return output;
	}

	protected TypeBinding reportIncompatibility(BlockScope scope, TypeBinding rawType) {
		AbstractMethodDeclaration enclosingMethod = scope.methodScope().referenceMethod();
		if (enclosingMethod != null && enclosingMethod.isMappingWrapper.any())
		    scope.problemReporter().typeMismatchInMethodMapping(
		    		this.expression, rawType, this.expectedType,
		    		enclosingMethod.isMappingWrapper.callout());
		else
			scope.problemReporter().typeMismatchError(rawType, this.expectedType, this.expression, null);
		this.resolvedType = null;
		return null;
	}

	protected void checkOtherConversions(BlockScope scope, TypeBinding requiredType, TypeBinding providedType)
	{
        // copied and adjusted from ReturnStatement.resolve()
		this.expression.computeConversion(scope, requiredType, providedType);
		if (providedType.needsUncheckedConversion(requiredType)) {
		    scope.problemReporter().unsafeTypeConversion(this.expression, requiredType, providedType);
		}
		if (this.expression instanceof CastExpression
				&& (this.expression.bits & (ASTNode.UnnecessaryCast|ASTNode.DisableUnnecessaryCastCheck)) == 0) {
			CastExpression.checkNeedForAssignedCast(scope, providedType, (CastExpression) this.expression);
		}
	}

	public static boolean usesAutoboxing(Expression expression) {
		return (expression.implicitConversion & (ASTNode.BOXING|ASTNode.UNBOXING)) != 0;
	}
}
