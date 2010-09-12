/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: QualifiedThisReference.java 23404 2010-02-03 14:10:22Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

/**
 * OTDT changes:
 *
 * What: If qualification is a role, use the class part.
 * Why:  Only the class contains nested roles.
 *
 * What: Qualified this within a static role method requires two tricks.
 * Where:
 *   + AbstractMethodDeclaration.generateCode() generates 2 synthetic arguments,
 *     to ensure that aload_1 will fetch the enclosing team instance
 *     similar to constructors of regular inner classes.
 *   + BlockScope.getEmulationPath() finds the synthetic argument as the
 *     implementation of this reference.
 *
 * @version $Id: QualifiedThisReference.java 23404 2010-02-03 14:10:22Z stephan $
 */
public class QualifiedThisReference extends ThisReference {

	public TypeReference qualification;
//{ObjectTeams: accessible for sub-class:
	protected
// SH}
	ReferenceBinding currentCompatibleType;

	public QualifiedThisReference(TypeReference name, int sourceStart, int sourceEnd) {
		super(sourceStart, sourceEnd);
		this.qualification = name;
		name.bits |= IgnoreRawTypeCheck; // no need to worry about raw type usage
		this.sourceStart = name.sourceStart;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return flowInfo;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		boolean valueRequired) {

		return flowInfo;
	}

	/**
	 * Code generation for QualifiedThisReference
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		if (valueRequired) {
			if ((this.bits & DepthMASK) != 0) {
				Object[] emulationPath =
					currentScope.getEmulationPath(this.currentCompatibleType, true /*only exact match*/, false/*consider enclosing arg*/);
				codeStream.generateOuterAccess(emulationPath, this, this.currentCompatibleType, currentScope);
			} else {
				// nothing particular after all
				codeStream.aload_0();
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public TypeBinding resolveType(BlockScope scope) {

		this.constant = Constant.NotAConstant;
		// X.this is not a param/raw type as denoting enclosing instance
		TypeBinding type = this.qualification.resolveType(scope, true /* check bounds*/);
		if (type == null || !type.isValidBinding()) return null;
		// X.this is not a param/raw type as denoting enclosing instance
		type = type.erasure();

		// resolvedType needs to be converted to parameterized
		if (type instanceof ReferenceBinding) {
			this.resolvedType = scope.environment().convertToParameterizedType((ReferenceBinding) type);
		} else {
			// error case
			this.resolvedType = type;
		}

//{ObjectTeams: redirect to the class part of a role:
        if (((ReferenceBinding)this.resolvedType).isDirectRole())
            this.resolvedType =
            			 type =
                ((ReferenceBinding)this.resolvedType).roleModel.getClassPartBinding();
// SH}

		// the qualification MUST exactly match some enclosing type name
		// It is possible to qualify 'this' by the name of the current class
		int depth = 0;
		this.currentCompatibleType = scope.referenceType().binding;
//{ObjectTeams: use class part to avoid roles to be reported as static:
		this.currentCompatibleType = this.currentCompatibleType.getRealClass();
// SH}
		while (this.currentCompatibleType != null && this.currentCompatibleType != type) {
			depth++;
			this.currentCompatibleType = this.currentCompatibleType.isStatic() ? null : this.currentCompatibleType.enclosingType();
		}
		this.bits &= ~DepthMASK; // flush previous depth if any
		this.bits |= (depth & 0xFF) << DepthSHIFT; // encoded depth into 8 bits

		if (this.currentCompatibleType == null) {
			scope.problemReporter().noSuchEnclosingInstance(type, this, false);
			return this.resolvedType;
		}

		// Ensure one cannot write code like: B() { super(B.this); }
		if (depth == 0) {
			checkAccess(scope.methodScope());
		} // if depth>0, path emulation will diagnose bad scenarii

//{ObjectTeams: wrap role type:
		this.resolvedType = RoleTypeCreator.maybeWrapUnqualifiedRoleType(
	                scope,
	                this.resolvedType,
	                this);
// SH}
		return this.resolvedType;
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		return this.qualification.print(0, output).append(".this"); //$NON-NLS-1$
	}

	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			this.qualification.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}

	public void traverse(
			ASTVisitor visitor,
			ClassScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			this.qualification.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}
