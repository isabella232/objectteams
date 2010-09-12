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
 * $Id: BaseCallMessageSend.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticBaseCallSurrogate;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CallinImplementorDyn;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.problem.BaseCallProblemReporterWrapper;
import org.eclipse.objectteams.otdt.internal.core.compiler.problem.BlockScopeWrapper;
import org.eclipse.objectteams.otdt.internal.core.compiler.problem.ProblemReporterWrapper;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.MethodSignatureEnhancer;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;

/**
 * NEW for OTDT:
 *
 * This class encodes a base call in a callin method.
 *
 * What: Intercept error ParameterMismatch
 * Why:  @see BaseCallProblemReporterWrapper
 *
 * What: Help to analyse number of base calls
 * How:  see  analyseCode and  MethodDeclaration.baseCallTrackingVariable
 *
 * What: re-wire the message send to the base call surrogate
 * How:  resolveType adapts receiver, selector and return type,
 *		 may even insert an assignment to _OT$result for base return tunneling.
 *       TransformStatementsVisitor will take care of argument enhancing.
 *
 * What: create a method binding faking the base call surrogate to be created by the OTRE
 *
 * TODO(SH): pretty printing, mainly for error reporting! (hide enhancements!)
 *
 * @author gis
 * @version $Id: BaseCallMessageSend.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class BaseCallMessageSend extends AbstractExpressionWrapper
{
	public char[] sourceSelector;
	Expression[] sourceArgs;
	// keep a reference to the original send in case the _wrappee is replaced by some transformation
	// (e.g., CollectedReplacementsTransformer wrapping _wrappee with a cast).
	protected MessageSend _sendOrig;
	BaseReference _receiver; // avoid casts like (BaseReference)wrappee.receiver
	public boolean isSuperAccess; // flag for base.super.m() calls

    public BaseCallMessageSend(MessageSend wrappee, int baseEndPos)
    {
        super(wrappee, wrappee.sourceStart, wrappee.sourceEnd);
    	wrappee.receiver =
          this._receiver = new BaseReference(wrappee.sourceStart, baseEndPos);
		this._sendOrig = wrappee;
		this.sourceSelector = wrappee.selector;
		this.sourceArgs = wrappee.arguments;
    }
	/** Signal whether this base call is actually a base.super-call. */
	public void setSuperAccess(boolean isSuperAccess, ProblemReporter problemReporter) {
		this.isSuperAccess = isSuperAccess;
		if (isSuperAccess)
			problemReporter.baseSuperCallDecapsulation(this);
		// add a further boolean argument to pass this flag to the runtime.
		// insert it at front of regular arguments to it will end up between normal enhancement args and regular args.
		// (note that this arg may be removed again if current callin method is static,
		//       see TransformStatementsVisitor#visit(MessageSend,BlockScope))
		Expression[] args = this._sendOrig.arguments;
		this.sourceArgs = args;
		int len = 0;
		int extra = CallinImplementorDyn.DYNAMIC_WEAVING ? 0 : 1;
		if (args == null) {
			args = new Expression[extra];
		} else {
			len = args.length;
			System.arraycopy(args, 0, args=new Expression[len+extra], extra, len);
		}
		// insert before regular args:
		if (!CallinImplementorDyn.DYNAMIC_WEAVING) // FIXME(OTDYN): base.super calls not supported by dynamic weaver.
			args[0] = this.isSuperAccess ? new TrueLiteral(this.sourceStart, this.sourceEnd) : new FalseLiteral(this.sourceStart, this.sourceEnd);
		this._sendOrig.arguments = args;
	}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo)
	{
		flowInfo = super.analyseCode(currentScope, flowContext, flowInfo);
		MethodDeclaration callinMethod = getEnclosingCallinMethod(currentScope);
		LocalVariableBinding trackingVariable = callinMethod.baseCallTrackingVariable.binding;
		if (flowInfo.isDefinitelyAssigned(callinMethod.baseCallTrackingVariable))
			currentScope.problemReporter().definitelyDuplicateBasecall(this._wrappee);
		else if (flowInfo.isPotentiallyAssigned(trackingVariable))
			currentScope.problemReporter().potentiallyDuplicateBasecall(this._wrappee);
		else
			flowInfo.markAsDefinitelyAssigned(trackingVariable);

		// check exceptions thrown by any bound base method:
		MethodScope methodScope = currentScope.methodScope();
		if (methodScope != null) {
			MethodModel methodModel = methodScope.referenceMethod().binding.model;
			if (   methodModel != null
				&& methodModel._baseExceptions != null)
			{
				for (ReferenceBinding exceptionType : methodModel._baseExceptions)
					flowContext.checkExceptionHandlers(exceptionType, this, flowInfo, currentScope);
			}
		}

		if (this.isSuperAccess)
			// 	signal that the base call surrogate needs to handle super-access:
			MethodModel.addCallinFlag(currentScope.methodScope().referenceMethod(), IOTConstants.CALLIN_FLAG_BASE_SUPER_CALL);

		return flowInfo;
	}

	@Override
	public int nullStatus(FlowInfo flowInfo) {
		return FlowInfo.UNKNOWN;
	}

	/*
	 * Return the innermost callin method enclosing this basecall.
	 */
	private MethodDeclaration getEnclosingCallinMethod(BlockScope currentScope) {
		MethodDeclaration candidate = (MethodDeclaration)currentScope.methodScope().referenceContext;
		if (candidate.isCallin())
			return candidate;
		assert BaseCallMessageSend.nestedInCallin(currentScope.methodScope()) : "Not nested in a callin method"; //$NON-NLS-1$
		return (MethodDeclaration)currentScope.methodScope().parent.methodScope().referenceMethod();
	}

	/**
	 * If this base call resides in a local type retrieve the callin method enclosing that type.
	 */
	public static MethodDeclaration getOuterCallinMethod(MethodScope scope) {
		Scope parent = scope.parent;
		if (parent != null) {
			MethodScope outerMethodScope = parent.methodScope();
			if (outerMethodScope == null)
				return null;
			AbstractMethodDeclaration outerMethod = outerMethodScope.referenceMethod();
			if (outerMethod.isCallin())
				return (MethodDeclaration)outerMethod;
		}
		return null;
	}

	/**
	 * Are we in the situation of a base call within a local type within a callin method?
	 */
	public static boolean nestedInCallin(MethodScope scope) {
		return getOuterCallinMethod(scope) != null;
	}

	public TypeBinding resolveType(BlockScope scope)
	{
		AstGenerator gen = new AstGenerator(this._wrappee.sourceStart, this._wrappee.sourceEnd);
		MessageSend wrappedSend = this._sendOrig;
		AbstractMethodDeclaration referenceMethod = scope.methodScope().referenceMethod();
		boolean isStatic = scope.methodScope().isStatic;

		// === re-wire the message send to the base call surrogate
    	// receiver:
		MethodDeclaration outerCallinMethod = getOuterCallinMethod(scope.methodScope());
		MethodBinding enclosingCallinMethod = outerCallinMethod != null ? outerCallinMethod.binding : scope.methodScope().referenceMethodBinding();
		if (outerCallinMethod != null && outerCallinMethod.binding == null)
			return null; // no hope
		ReferenceBinding roleType = scope.enclosingSourceType();
		this._receiver.adjustReceiver(roleType, isStatic, outerCallinMethod, gen);
		
		// empty base call surrogate is handled using a synthetic base call surrogate:
		boolean isCallinBound = false;
		if (enclosingCallinMethod != null) {
			isCallinBound = SyntheticBaseCallSurrogate.isCallinMethodBoundIn(enclosingCallinMethod, enclosingCallinMethod.declaringClass);
		} else {
			isCallinBound = roleType.roleModel.isBound();
		}

		// who should work, compiler or OTRE?
		if (!isCallinBound && !CallinImplementorDyn.DYNAMIC_WEAVING) {
			resolveSyntheticBaseCallSurrogate(outerCallinMethod, scope);
			return this.resolvedType;
		}
		

		// selector:
		if (CallinImplementorDyn.DYNAMIC_WEAVING) {
			wrappedSend.selector = CallinImplementorDyn.OT_CALL_NEXT;
		} else {
			wrappedSend.selector = SyntheticBaseCallSurrogate.genSurrogateName(wrappedSend.selector, roleType.sourceName(), isStatic);
		}

		// arguments are enhanced by the TransformStatementsVisitor

    	// return type:
		TypeBinding returnType = null;
    	if (referenceMethod != null && ! referenceMethod.ignoreFurtherInvestigation) {
    		returnType = MethodModel.getReturnType(referenceMethod.binding);
			if (returnType.isBaseType()) {
				if (returnType != TypeBinding.VOID)
					this._wrappee = gen.createUnboxing(this._wrappee, (BaseTypeBinding)returnType);
				else if (outerCallinMethod == null)
					// store value which is not used locally but has to be chained to the caller.
					// (cannot set result in outer callin method!)
					this._wrappee = gen.assignment(
							gen.singleNameReference(IOTConstants.OT_RESULT), this._wrappee);
			}
    	}

    	// check context:
		if (outerCallinMethod==null) // already found appropriate context?
		{
			if (!checkContext(scope))
					return null;
		}

		BlockScopeWrapper baseCallScope = new BlockScopeWrapper(scope, this);
		super.resolveType(baseCallScope);
		if (CallinImplementorDyn.DYNAMIC_WEAVING) {
			// convert Object result from callNext
			if (returnType != null && !returnType.isBaseType()) {
				this.resolvedType = returnType;
				this._sendOrig.valueCast = returnType;
			}
		}
		return this.resolvedType;
	}
	/* manual resolve for an empty base call surrogate, which is indeed generated by the compiler, not the OTRE. */
	private void resolveSyntheticBaseCallSurrogate(MethodDeclaration outerCallinMethod, BlockScope scope) 
	{		
		// find the method:
		AbstractMethodDeclaration callinMethodDecl = outerCallinMethod;
		if (callinMethodDecl == null) {
			if (checkContext(scope)) {
				callinMethodDecl = scope.methodScope().referenceMethod();
			} else {
				return; // error reported by checkContext
			}
		}
		MethodBinding callinMethod = callinMethodDecl.binding;
		// check name match:
		if (!CharOperation.equals(this._sendOrig.selector, callinMethod.selector))
			scope.problemReporter().baseCallNotSameMethod(callinMethodDecl, this._sendOrig);

		// find the receiver type:
		this._receiver.resolve(scope);
		int depth = 0;
		while (this._receiver.resolvedType.isLocalType()) {			
			this._receiver.resolvedType = this._receiver.resolvedType.enclosingType();
			depth++;
		}
		this._receiver.bits |= depth << DepthSHIFT;
		if (this._receiver.resolvedType instanceof ReferenceBinding) {
			ReferenceBinding receiverType = (ReferenceBinding) this._receiver.resolvedType;
			this._receiver.resolvedType = receiverType.getRealClass();
		}

		// resolve arguments:
		TypeBinding[] sendparams = new TypeBinding[this._sendOrig.arguments.length];
		for (int i=0; i<sendparams.length; i++)
			sendparams[i] = this._sendOrig.arguments[i].resolveType(scope);
		
		// check arguments:
		int sourceArgsLen = 0;
		if (this.sourceArgs != null)
			sourceArgsLen = this.sourceArgs.length;
		TypeBinding[] methodParams = callinMethod.getSourceParameters();
		if (sourceArgsLen != methodParams.length) {
			scope.problemReporter().baseCallDoesntMatchRoleMethodSignature(this);
		} else {
			for (int i=0; i<sourceArgsLen; i++) {
				TypeBinding srcArgType = this.sourceArgs[i].resolvedType;
				if (srcArgType == null && callinMethodDecl.hasErrors())
					continue;
				if (!srcArgType.isCompatibleWith(methodParams[i])) {
					if (isBoxingCompatible(srcArgType, methodParams[i], this.sourceArgs[i], scope)) {
						int enhancedArgIdx = i+MethodSignatureEnhancer.ENHANCING_ARG_LEN+1; // normal enhancement plus isSuperAccess flag
						this._sendOrig.arguments[enhancedArgIdx].computeConversion(scope, methodParams[i], srcArgType);
					} else {
						scope.problemReporter().baseCallDoesntMatchRoleMethodSignature(this);
						break;
					}
				}
			}
		}
		
		// create and link the synthetic method binding:
		MethodBinding surrogate = null; 	
		MethodModel model = callinMethod.model;
		if (model != null)
			surrogate = model.getBaseCallSurrogate();
		if (surrogate == null) {
			SourceTypeBinding receiverClass = ((SourceTypeBinding)((ReferenceBinding)this._receiver.resolvedType).getRealClass());
			if (SyntheticBaseCallSurrogate.isBindingForCallinMethodInherited(callinMethod)) {
				ReferenceBinding currentRole = callinMethod.declaringClass;
				while (surrogate == null && ((currentRole = currentRole.superclass()) != null)) {
					surrogate = receiverClass.getExactMethod(SyntheticBaseCallSurrogate.genSurrogateName(this.sourceSelector, currentRole.sourceName(), callinMethod.isStatic()), sendparams, null);
				}
			} else {
				surrogate = receiverClass.addSyntheticBaseCallSurrogate(callinMethod);
			}
		}
		this._sendOrig.binding = surrogate;
		this._sendOrig.actualReceiverType = this._receiver.resolvedType;
		this._sendOrig.constant = Constant.NotAConstant;
		this.resolvedType = this._sendOrig.resolvedType = MethodModel.getReturnType(this._sendOrig.binding);
	}

	@Override
	public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
		if (   this._sendOrig.binding instanceof SyntheticBaseCallSurrogate 
			&& this._sendOrig == this._wrappee        // otherwise wrappee contains conversion
			&& this.resolvedType.isBaseType()
			&& this.resolvedType != TypeBinding.VOID) 
		{
			ReferenceBinding boxType = (ReferenceBinding) scope.getType(AstGenerator.boxTypeName((BaseTypeBinding) this.resolvedType), 3);
			this._sendOrig.valueCast =  boxType; // triggers insertion of checkcast to boxType
			compileTimeType = boxType;           // triggers unboxing conversion
		}
		super.computeConversion(scope, runtimeTimeType, compileTimeType);
	}
	
	private boolean checkContext(BlockScope scope) {
		if (	scope.methodScope() == null
			|| !(scope.methodScope().referenceContext instanceof AbstractMethodDeclaration)) // initializer
			{
				scope.problemReporter().baseCallOutsideMethod(this);
				return false;
			}

        AbstractMethodDeclaration methodDecl = (AbstractMethodDeclaration)scope.methodScope().referenceContext;
        // base call only allowed in callin methods:
        if ((methodDecl.modifiers & ExtraCompilerModifiers.AccCallin) == 0) {
       		scope.problemReporter().basecallInRegularMethod(this, methodDecl);
       		return false;
        }
		return true;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope)
	{
		if(visitor.visit(this, scope))
		{
			this._wrappee.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

	public MessageSend getMessageSend()
	{
		return this._sendOrig;
	}

	public ProblemReporterWrapper create(ProblemReporter wrappee)
	{
		return new BaseCallProblemReporterWrapper(wrappee, this);
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		char[] selectorSave = "<missing>".toCharArray(); //$NON-NLS-1$
		Expression[] argsSave= null;
		try {
			if (this._sendOrig != null) {
				selectorSave = this._sendOrig.selector;
				this._sendOrig.selector = this.sourceSelector;
				argsSave = this._sendOrig.arguments;
				boolean isEnhanced = (this._sendOrig.bits & ASTNode.HasBeenTransformed) != 0;
				this._sendOrig.arguments = MethodSignatureEnhancer.retrenchBasecallArguments(argsSave, isEnhanced);
				return this._sendOrig.printExpression(indent, output);
			}
		} finally {
			if (selectorSave != null)
				this._sendOrig.selector = selectorSave;
			if (argsSave != null)
				this._sendOrig.arguments = argsSave;
		}
		return super.printExpression(indent, output);
	}
}
