/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Assignment.java 23405 2010-02-03 17:02:18Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Genady Beriozkin - added support for reporting assignment with no effect
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lowering;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

/**
 * OTDT changes:
 *
 * What: wrap the rhs type if it is a role type
 *
 * What: record team anchor equivalence induced by the assignment
 *
 * @version $Id: Assignment.java 23405 2010-02-03 17:02:18Z stephan $
 */
public class Assignment extends Expression {

	public Expression lhs;
	public Expression expression;

public Assignment(Expression lhs, Expression expression, int sourceEnd) {
	//lhs is always a reference by construction ,
	//but is build as an expression ==> the checkcast cannot fail
	this.lhs = lhs;
	lhs.bits |= IsStrictlyAssigned; // tag lhs as assigned
	this.expression = expression;
	this.sourceStart = lhs.sourceStart;
	this.sourceEnd = sourceEnd;
}

//{ObjectTeams: special analysis for lowering conditional.
//   The following code actually uses the local:
//   	Base local;
//   	(local = role).baseMethod();
@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
	if (valueRequired && this.lhs instanceof SingleNameReference && Lowering.isLoweringConditional(this.expression)) {
		SingleNameReference ref = (SingleNameReference) this.lhs;
		if (ref.binding.kind() == Binding.LOCAL)
			((LocalVariableBinding)ref.binding).useFlag = LocalVariableBinding.USED;
	}
	return super.analyseCode(currentScope, flowContext, flowInfo, valueRequired);
}
// SH}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// record setting a variable: various scenarii are possible, setting an array reference,
// a field reference, a blank final field reference, a field of an enclosing instance or
// just a local variable.
	LocalVariableBinding local = this.lhs.localVariableBinding();
	int nullStatus = this.expression.nullStatus(flowInfo);
	if (local != null && (local.type.tagBits & TagBits.IsBaseType) == 0) {
		if (nullStatus == FlowInfo.NULL) {
			flowContext.recordUsingNullReference(currentScope, local, this.lhs,
				FlowContext.CAN_ONLY_NULL | FlowContext.IN_ASSIGNMENT, flowInfo);
		}
	}
	flowInfo = ((Reference) this.lhs)
		.analyseAssignment(currentScope, flowContext, flowInfo, this, false)
		.unconditionalInits();
	if (local != null && (local.type.tagBits & TagBits.IsBaseType) == 0) {
		switch(nullStatus) {
			case FlowInfo.NULL :
				flowInfo.markAsDefinitelyNull(local);
				break;
			case FlowInfo.NON_NULL :
				flowInfo.markAsDefinitelyNonNull(local);
				break;
			default:
				flowInfo.markAsDefinitelyUnknown(local);
		}
		if (flowContext.initsOnFinally != null) {
			switch(nullStatus) {
				case FlowInfo.NULL :
					flowContext.initsOnFinally.markAsDefinitelyNull(local);
					break;
				case FlowInfo.NON_NULL :
					flowContext.initsOnFinally.markAsDefinitelyNonNull(local);
					break;
				default:
					flowContext.initsOnFinally.markAsDefinitelyUnknown(local);
			}
		}
	}
	return flowInfo;
}

void checkAssignment(BlockScope scope, TypeBinding lhsType, TypeBinding rhsType) {
	FieldBinding leftField = getLastField(this.lhs);
	if (leftField != null &&  rhsType != TypeBinding.NULL && (lhsType.kind() == Binding.WILDCARD_TYPE) && ((WildcardBinding)lhsType).boundKind != Wildcard.SUPER) {
	    scope.problemReporter().wildcardAssignment(lhsType, rhsType, this.expression);
	} else if (leftField != null && !leftField.isStatic() && leftField.declaringClass != null /*length pseudo field*/&& leftField.declaringClass.isRawType()) {
	    scope.problemReporter().unsafeRawFieldAssignment(leftField, rhsType, this.lhs);
	} else if (rhsType.needsUncheckedConversion(lhsType)) {
	    scope.problemReporter().unsafeTypeConversion(this.expression, rhsType, lhsType);
	}
}

public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	// various scenarii are possible, setting an array reference,
	// a field reference, a blank final field reference, a field of an enclosing instance or
	// just a local variable.

	int pc = codeStream.position;
	 ((Reference) this.lhs).generateAssignment(currentScope, codeStream, this, valueRequired);
	// variable may have been optimized out
	// the lhs is responsible to perform the implicitConversion generation for the assignment since optimized for unused local assignment.
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

FieldBinding getLastField(Expression someExpression) {
    if (someExpression instanceof SingleNameReference) {
        if ((someExpression.bits & RestrictiveFlagMASK) == Binding.FIELD) {
            return (FieldBinding) ((SingleNameReference)someExpression).binding;
        }
    } else if (someExpression instanceof FieldReference) {
        return ((FieldReference)someExpression).binding;
    } else if (someExpression instanceof QualifiedNameReference) {
        QualifiedNameReference qName = (QualifiedNameReference) someExpression;
        if (qName.otherBindings == null) {
        	if ((someExpression.bits & RestrictiveFlagMASK) == Binding.FIELD) {
        		return (FieldBinding)qName.binding;
        	}
        } else {
            return qName.otherBindings[qName.otherBindings.length - 1];
        }
    }
    return null;
}

public int nullStatus(FlowInfo flowInfo) {
	return this.expression.nullStatus(flowInfo);
}

public StringBuffer print(int indent, StringBuffer output) {
	//no () when used as a statement
	printIndent(indent, output);
	return printExpressionNoParenthesis(indent, output);
}
public StringBuffer printExpression(int indent, StringBuffer output) {
	//subclass redefine printExpressionNoParenthesis()
	output.append('(');
	return printExpressionNoParenthesis(0, output).append(')');
}

public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {
	this.lhs.printExpression(indent, output).append(" = "); //$NON-NLS-1$
	return this.expression.printExpression(0, output);
}

public StringBuffer printStatement(int indent, StringBuffer output) {
	//no () when used as a statement
	return print(indent, output).append(';');
}

public TypeBinding resolveType(BlockScope scope) {
	// due to syntax lhs may be only a NameReference, a FieldReference or an ArrayReference
	this.constant = Constant.NotAConstant;
	if (!(this.lhs instanceof Reference) || this.lhs.isThis()) {
		scope.problemReporter().expressionShouldBeAVariable(this.lhs);
		return null;
	}
	TypeBinding lhsType = this.lhs.resolveType(scope);
	this.expression.setExpectedType(lhsType); // needed in case of generic method invocation
	if (lhsType != null) {
		this.resolvedType = lhsType.capture(scope, this.sourceEnd);
	}
	TypeBinding rhsType = this.expression.resolveType(scope);
	if (lhsType == null || rhsType == null) {
		return null;
	}
	// check for assignment with no effect
	Binding left = getDirectBinding(this.lhs);
	if (left != null && left == getDirectBinding(this.expression)) {
		scope.problemReporter().assignmentHasNoEffect(this, left.shortReadableName());
	}
//{ObjectTeams: wrap rhs type:
	rhsType = RoleTypeCreator.maybeWrapUnqualifiedRoleType(rhsType, scope, this.expression);
// SH}

	// Compile-time conversion of base-types : implicit narrowing integer into byte/short/character
	// may require to widen the rhs expression at runtime
	if (lhsType != rhsType) { // must call before computeConversion() and typeMismatchError()
		scope.compilationUnitScope().recordTypeConversion(lhsType, rhsType);
	}
	if (this.expression.isConstantValueOfTypeAssignableToType(rhsType, lhsType)
			|| rhsType.isCompatibleWith(lhsType)) {
		this.expression.computeConversion(scope, lhsType, rhsType);
		checkAssignment(scope, lhsType, rhsType);
		if (this.expression instanceof CastExpression
				&& (this.expression.bits & ASTNode.UnnecessaryCast) == 0) {
			CastExpression.checkNeedForAssignedCast(scope, lhsType, (CastExpression) this.expression);
		}

//{ObjectTeams: additional check and record team anchor equivalence:
		if (Config.getLoweringPossible() && RoleTypeBinding.isRoleType(rhsType))
			((DependentTypeBinding)rhsType).recheckAmbiguousLowering(lhsType, this, scope, null);

		VariableBinding lhsBinding = null;
		if (this.lhs instanceof QualifiedNameReference) {
			QualifiedNameReference qnRef = (QualifiedNameReference)this.lhs;
			if (qnRef.otherBindings != null)
				lhsBinding = qnRef.otherBindings[qnRef.otherBindings.length-1];
			else
				lhsBinding = (VariableBinding)qnRef.binding;
		} else if (this.lhs instanceof NameReference) {
			lhsBinding = (VariableBinding)((NameReference)this.lhs).binding;
		} else if (this.lhs instanceof FieldReference) {
			lhsBinding = ((FieldReference)this.lhs).binding;
		}
		// could also be ArrayReference, which is not considered for anchor equivalence
		if (lhsBinding != null)
			lhsBinding.setBestNameFromStat(this.expression);
// SH}
		return this.resolvedType;
	} else if (isBoxingCompatible(rhsType, lhsType, this.expression, scope)) {
		this.expression.computeConversion(scope, lhsType, rhsType);
		if (this.expression instanceof CastExpression
				&& (this.expression.bits & ASTNode.UnnecessaryCast) == 0) {
			CastExpression.checkNeedForAssignedCast(scope, lhsType, (CastExpression) this.expression);
		}
		return this.resolvedType;
	}
	scope.problemReporter().typeMismatchError(rhsType, lhsType, this.expression, this.lhs);
	return lhsType;
}

/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#resolveTypeExpecting(org.eclipse.jdt.internal.compiler.lookup.BlockScope, org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
 */
public TypeBinding resolveTypeExpecting(BlockScope scope, TypeBinding expectedType) {

	TypeBinding type = super.resolveTypeExpecting(scope, expectedType);
	if (type == null) return null;
	TypeBinding lhsType = this.resolvedType;
	TypeBinding rhsType = this.expression.resolvedType;
	// signal possible accidental boolean assignment (instead of using '==' operator)
	if (expectedType == TypeBinding.BOOLEAN
			&& lhsType == TypeBinding.BOOLEAN
			&& (this.lhs.bits & IsStrictlyAssigned) != 0) {
		scope.problemReporter().possibleAccidentalBooleanAssignment(this);
	}
	checkAssignment(scope, lhsType, rhsType);
	return type;
}

public void traverse(ASTVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		this.lhs.traverse(visitor, scope);
		this.expression.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
public LocalVariableBinding localVariableBinding() {
	return this.lhs.localVariableBinding();
}
}
