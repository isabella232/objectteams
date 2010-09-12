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
 * $Id: TSuperMessageSend.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.CALLIN_FLAG_DEFINITELY_MISSING_BASECALL;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.CALLIN_FLAG_POTENTIALLY_MISSING_BASECALL;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.AnchorMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;

/**
 * NEW for OTDT.
 *
 * A messages send using tsuper has to perform additional checks,
 * because it is legal only if enclosing method and invoked method have
 * the same signature (except for the tsuper-mark argument).
 *
 * What: add appropriate marker argument
 * How:  before actual resolve, resolve the (possibly qualified) tsuper reference
 *       and determine from it the appropriate marker interface.
 *
 * @author stephan
 * @version $Id: TSuperMessageSend.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class TSuperMessageSend extends MessageSend {

	public  TsuperReference tsuperReference;

	private MethodBinding tsuperMethod;
	private boolean needReturnConversion= false;

	/** Check whether this message send contributes to base call analysis */
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		flowInfo = super.analyseCode(currentScope, flowContext, flowInfo);
		if (this.binding.isCallin())
		{
			if (!MethodModel.hasCallinFlag(this.tsuperMethod, CALLIN_FLAG_DEFINITELY_MISSING_BASECALL)) { // no contribution if no base call.
				MethodDeclaration callinMethod = (MethodDeclaration)currentScope.methodScope().referenceContext;
				LocalVariableBinding trackingVariable = callinMethod.baseCallTrackingVariable.binding;
				if (MethodModel.hasCallinFlag(this.tsuperMethod, CALLIN_FLAG_POTENTIALLY_MISSING_BASECALL)) {
					if (   flowInfo.isDefinitelyAssigned(trackingVariable)
						|| flowInfo.isPotentiallyAssigned(trackingVariable))
					{
						currentScope.problemReporter().potentiallyDuplicateBasecall(this);
					} else {
						FlowInfo potential = flowInfo.copy();
						potential.markAsDefinitelyAssigned(trackingVariable);
						flowInfo = FlowInfo.conditional(flowInfo.initsWhenTrue(), potential.initsWhenTrue());
					}
				} else { // tsuper definitely has a base call:
					if (flowInfo.isDefinitelyAssigned(trackingVariable))
						currentScope.problemReporter().definitelyDuplicateBasecall(this);
					else if (flowInfo.isPotentiallyAssigned(trackingVariable))
						currentScope.problemReporter().potentiallyDuplicateBasecall(this);
					if (!flowInfo.isDefinitelyAssigned(trackingVariable))
						flowInfo.markAsDefinitelyAssigned(trackingVariable);
				}
			}
		}
		return flowInfo;
	}

	public TypeBinding resolveType (BlockScope scope)
	{

		AbstractMethodDeclaration context = scope.methodScope().referenceMethod();
		if (   context != null
			&& CharOperation.equals(this.selector, context.selector))
		{
			// == Prepare marker arg:
			ReferenceBinding qualType = null;
			if (this.tsuperReference.qualification != null) {
				ReferenceBinding tsuperType = (ReferenceBinding)this.tsuperReference.resolveType(scope);
				if (tsuperType != null)
					qualType = tsuperType.enclosingType();
				// proceed even with missing qualType in order to find more errors (treat as unqualified then).
				// see 1.3.8-otjld-access-to-superrole-7
			}
	    	this.arguments = TSuperHelper.addMarkerArgument(
	    					qualType, this, this.arguments, scope);

	    	if (this.arguments == null && scope.methodScope().referenceMethod().ignoreFurtherInvestigation)
	    		return null; // various fatal errors in addMarkerArgument.

			super.resolveType(scope);

			// == find and store original tsuper method (as if bypassing its copy)
			if (   this.binding != null
				&& this.binding.isValidBinding())
			{
				this.tsuperMethod = this.binding.copyInheritanceSrc;
				if (this.binding.isCallin()) {
					// restore return type which has been generalized to 'Object':
					this.resolvedType= MethodModel.getReturnType(this.tsuperMethod);
					this.needReturnConversion= true;
				}
			}
			return this.resolvedType;
		}
		scope.problemReporter().tsuperCallsWrongMethod(this);
		return null;
	}

	protected TypeBinding afterMethodLookup(Scope scope, AnchorMapping mapping, TypeBinding[] argumentTypes, TypeBinding returnType)
	{
		if (this.binding.problemId() == ProblemReasons.NotFound) {
			// tsuper.m() may in fact refer to tsuper.super.m().
			// try to find the method as super.tsuper() instead:
			ReferenceBinding superRole = ((ReferenceBinding)this.receiver.resolvedType).superclass();
			if (superRole.isRole()) {
				// resolving includes evaluation of late attribute CopyInheritancSrc
				Dependencies.ensureBindingState(superRole, ITranslationStates.STATE_LATE_ATTRIBUTES_EVALUATED);
				int len = argumentTypes.length;
				boolean stripMarker = false;
				MethodBinding alternateMethod = getAlternateMethod(scope, superRole, argumentTypes);
				if (alternateMethod == null) {
					TypeBinding[] strippedArgs = new TypeBinding[len - 1];
					System.arraycopy(argumentTypes, 0, strippedArgs, 0, len-1);
					alternateMethod = getAlternateMethod(scope, superRole, strippedArgs);
					stripMarker = true;
				}
				if (alternateMethod != null)
				{
					this.binding = alternateMethod;
					if (stripMarker)
						System.arraycopy(this.arguments, 0, this.arguments = new Expression[len-1], 0, len-1);
					this.receiver = new SuperReference(this.receiver.sourceStart, this.receiver.sourceEnd);
					this.receiver.resolvedType = superRole;
					this.receiver.constant = Constant.NotAConstant;
					this.actualReceiverType = superRole;
					return this.binding.returnType; // updated
				}
			}
		}
		return returnType; // not updated
	}
	
	@Override @SuppressWarnings("hiding")
	public boolean checkInvocationArguments(BlockScope scope, Expression receiver, TypeBinding receiverType,
			MethodBinding method, Expression[] arguments, TypeBinding[] argumentTypes, boolean argsContainCast,
			InvocationSite invocationSite) 
	{
		if (arguments != null && argumentTypes.length == arguments.length + 1) {
			int len = arguments.length;
			System.arraycopy(argumentTypes, 0, argumentTypes = new TypeBinding[len], 0, len);
		}
		return super.checkInvocationArguments(scope, receiver, receiverType, method, arguments, argumentTypes, argsContainCast, invocationSite);
	}

	private MethodBinding getAlternateMethod(
			Scope scope, ReferenceBinding superRole, TypeBinding[] argumentTypes)
	{
		MethodBinding alternateMethod = scope.getMethod(superRole, this.selector, argumentTypes, this);
		if (alternateMethod.problemId() == ProblemReasons.NotVisible) {
			return alternateMethod; // want to see this error as IProblem.IndirectTSuperInvisible, cf. ProblemReporter.invalidMethod
		}
		MethodBinding alternateSrc = alternateMethod.copyInheritanceSrc;
		// TODO(SH): binary verbatim copies (no marker arg) are not recognized as copies!
		if (   alternateSrc != null
		    && isRoleOfSuperTeam(alternateSrc.declaringClass, scope))
			return alternateMethod;
		return null;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream,
			boolean valueRequired)
	{
		super.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired && this.needReturnConversion) {
			if (this.resolvedType != null && this.resolvedType.isValidBinding()) {
				if (this.resolvedType.isBaseType()) {
					// something like: ((Integer)result).intValue()
					char[][] boxtypeName= AstGenerator.boxTypeName((BaseTypeBinding)this.resolvedType);
					codeStream.checkcast(currentScope.getType(boxtypeName, 3));
					codeStream.generateUnboxingConversion(this.resolvedType.id);
				} else {
					// (RefType)result
					codeStream.checkcast(this.resolvedType);
				}
			}
		}
	}

	private boolean isRoleOfSuperTeam(ReferenceBinding roleClass, Scope scope) {
		ReferenceBinding site = scope.enclosingSourceType();
		if (!site.isRole())
			return false;
		return site.enclosingType().superclass().isCompatibleWith(roleClass.enclosingType());
	}

	protected boolean isAnySuperAccess() {
		return true;
	}

	public StringBuffer printExpression(int indent, StringBuffer output){
		if (this.tsuperReference != null && this.tsuperReference.qualification != null) {
			this.tsuperReference.qualification.printExpression(indent, output);
			output.append("."); //$NON-NLS-1$
		}
	    output.append("tsuper."); //$NON-NLS-1$
	    return super.printExpression(indent, output);
	}

}
