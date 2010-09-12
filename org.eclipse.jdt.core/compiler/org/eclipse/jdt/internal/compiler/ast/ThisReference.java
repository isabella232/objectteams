/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ThisReference.java 23404 2010-02-03 14:10:22Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

/**
 * OTDT changes:
 *
 * What: if it's not an ImplicitThis, wrap the type if its a role.
 *
 * @version $Id: ThisReference.java 23404 2010-02-03 14:10:22Z stephan $
 */
public class ThisReference extends Reference {

	public static ThisReference implicitThis(){

		ThisReference implicitThis = new ThisReference(0, 0);
		implicitThis.bits |= IsImplicitThis;
		return implicitThis;
	}

	public ThisReference(int sourceStart, int sourceEnd) {

		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}

	/*
	 * @see Reference#analyseAssignment(...)
	 */
	public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound) {

		return flowInfo; // this cannot be assigned
	}

	public boolean checkAccess(MethodScope methodScope) {

		// this/super cannot be used in constructor call
		if (methodScope.isConstructorCall) {
			methodScope.problemReporter().fieldsOrThisBeforeConstructorInvocation(this);
			return false;
		}

		// static may not refer to this/super
		if (methodScope.isStatic) {
			methodScope.problemReporter().errorThisSuperInStatic(this);
			return false;
		}
		return true;
	}

	/*
	 * @see Reference#generateAssignment(...)
	 */
	public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {

		 // this cannot be assigned
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

		int pc = codeStream.position;
		if (valueRequired)
			codeStream.aload_0();
		if ((this.bits & IsImplicitThis) == 0) codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/*
	 * @see Reference#generateCompoundAssignment(...)
	 */
	public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion,  boolean valueRequired) {

		 // this cannot be assigned
	}

	/*
	 * @see org.eclipse.jdt.internal.compiler.ast.Reference#generatePostIncrement()
	 */
	public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {

		 // this cannot be assigned
	}

	public boolean isImplicitThis() {

		return (this.bits & IsImplicitThis) != 0;
	}

	public boolean isThis() {

		return true ;
	}

	public int nullStatus(FlowInfo flowInfo) {
		return FlowInfo.NON_NULL;
	}

	public StringBuffer printExpression(int indent, StringBuffer output){

		if (isImplicitThis()) return output;
		return output.append("this"); //$NON-NLS-1$
	}

	public TypeBinding resolveType(BlockScope scope) {

		this.constant = Constant.NotAConstant;
		if (!isImplicitThis() &&!checkAccess(scope.methodScope())) {
			return null;
		}
//{ObjectTeams: only implicit this returned directly:
	  if (isImplicitThis())
// orig:
		return this.resolvedType = scope.enclosingReceiverType();
// : giro
		this.resolvedType = RoleTypeCreator.maybeWrapUnqualifiedRoleType(
                scope.enclosingReceiverType(),
                scope,
                this);
        return this.resolvedType;
// SH}
	}

	public void traverse(ASTVisitor visitor, BlockScope blockScope) {

		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}
	public void traverse(ASTVisitor visitor, ClassScope blockScope) {

		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}
}
