/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Reference.java 23404 2010-02-03 14:10:22Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * OTDT changes:
 * What: Utility for determining the nesting depth of a referred field as seen from a particular type.
 *
 * What: support notion of expected type
 * Why:  inferred callout to field must be created with proper type, possibly involving lifting.
 * 
 * @author stephan
 * @version $Id: Reference.java 23404 2010-02-03 14:10:22Z stephan $
 */
public abstract class Reference extends Expression  {
//{ObjectTeams: support expected type
	public TypeBinding expectedType;
	@Override
	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}
// SH}
/**
 * BaseLevelReference constructor comment.
 */
public Reference() {
	super();
}
public abstract FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound);

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return flowInfo;
}

public FieldBinding fieldBinding() {
	//this method should be sent one FIELD-tagged references
	//  (ref.bits & BindingIds.FIELD != 0)()
	return null ;
}

public void fieldStore(Scope currentScope, CodeStream codeStream, FieldBinding fieldBinding, MethodBinding syntheticWriteAccessor, TypeBinding receiverType, boolean isImplicitThisReceiver, boolean valueRequired) {
	int pc = codeStream.position;
	if (fieldBinding.isStatic()) {
		if (valueRequired) {
			switch (fieldBinding.type.id) {
				case TypeIds.T_long :
				case TypeIds.T_double :
					codeStream.dup2();
					break;
				default : 
					codeStream.dup();
					break;
			}
		}
		if (syntheticWriteAccessor == null) {
			TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(currentScope, fieldBinding, receiverType, isImplicitThisReceiver);
			codeStream.fieldAccess(Opcodes.OPC_putstatic, fieldBinding, constantPoolDeclaringClass);
		} else {
			codeStream.invoke(Opcodes.OPC_invokestatic, syntheticWriteAccessor, null /* default declaringClass */);
		}
	} else { // Stack:  [owner][new field value]  ---> [new field value][owner][new field value]
		if (valueRequired) {
			switch (fieldBinding.type.id) {
				case TypeIds.T_long :
				case TypeIds.T_double :
					codeStream.dup2_x1();
					break;
				default : 
					codeStream.dup_x1();
					break;
			}
		}
		if (syntheticWriteAccessor == null) {
			TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(currentScope, fieldBinding, receiverType, isImplicitThisReceiver);
			codeStream.fieldAccess(Opcodes.OPC_putfield, fieldBinding, constantPoolDeclaringClass);
		} else {
			codeStream.invoke(Opcodes.OPC_invokestatic, syntheticWriteAccessor, null /* default declaringClass */);
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

public abstract void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired);

public abstract void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired);

public abstract void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired);

//{ObjectTeams: references to the enclosing team need synthetic accessors.
/**
 * @param fieldBinding
 * @param enclosingSourceType
 * @return depth of field's declaring class as seen from enclosingSourceType or -1.
 */
protected int getDepthForSynthFieldAccess(FieldBinding fieldBinding, SourceTypeBinding enclosingSourceType) {
	int depth = (this.bits & DepthMASK) >> DepthSHIFT;

	if (fieldBinding.isPrivate())
		return depth;
	if (fieldBinding.isPublic())
		return -1;
	if (fieldBinding.declaringClass.getPackage() == enclosingSourceType.getPackage()) {
		depth = TeamModel.levelFromEnclosingTeam(fieldBinding.declaringClass, enclosingSourceType);
		// through copy inheritance this code could be executed within a different package!
		if (depth == 0)
			return -1; // neither a team field, nor an access across packages
	}
	return depth;
}
/** 
 * If this reference is an inferred call to a c-t-f to static, synthetic args (int,Team) must be generated. 
 * @return true if synthetic args have been generated
 */
protected boolean checkGeneratedSynthArgsForFieldAccess(MethodBinding[] accessors, CodeStream codeStream, BlockScope scope) {
	if (accessors != null && SyntheticMethodBinding.isCalloutToStaticField(accessors[SingleNameReference.WRITE]))
	{
		SyntheticMethodBinding syntheticMethodBinding = (SyntheticMethodBinding)accessors[SingleNameReference.WRITE];
		syntheticMethodBinding.generateStaticCTFArgs(codeStream, scope, this, (this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT);
		return true;
	}
	return false; // nothing generated
}
// SH}
}
