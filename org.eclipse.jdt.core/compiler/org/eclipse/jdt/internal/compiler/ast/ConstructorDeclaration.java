/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ConstructorDeclaration.java 23404 2010-02-03 14:10:22Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.ArrayList;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.BaseAllocationExpression;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.BytecodeTransformer;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel.ProblemDetail;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.InsertTypeAdjustmentsVisitor;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * OTDT changes:
 *
 * What: analyze base constructor (exactly one) call.
 *
 * What: translate base() ctor calls into constructorCall (cf. BaseAllocationExpression)
 *
 * What: block some operations on generated/copied constructors.
 *
 * BYTE CODE:
 * What:
 * 		1) store the completed byte code in RoleModel
 * 		1b) mark erroneous methods to prevent byte code copy (have not byte code)
 * 		2) switch between actual generateCode and adjusting copy
 *
 * What: Refuse to generate field initialization for roles (see RoleInitializationMethod).
 * Why:  normal field initialization cannot be copy-inherited.
 *
 * What: generate code for valueParamSynthArgs.
 *
 * What: create statements for throwing an IllegalRoleCreationException at run-time
 * Why:  specializing an unbound role to a bound role renders existing new-expressions illegal.
 *
 * What: write OT-specific byte code attributes.
 *
 * @version $Id: ConstructorDeclaration.java 23404 2010-02-03 14:10:22Z stephan $
 */
public class ConstructorDeclaration extends AbstractMethodDeclaration {

	public ExplicitConstructorCall constructorCall;

	public TypeParameter[] typeParameters;
//{ObjectTeams: more flags
	// we need to control the order of analyseCode, but prevent double analysis:
	private boolean isCodeAnalyzed = false;
	public boolean needsLifting = false;
// SH}

public ConstructorDeclaration(CompilationResult compilationResult){
	super(compilationResult);
}

/**
 * @see org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration#analyseCode(org.eclipse.jdt.internal.compiler.lookup.ClassScope, org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext, org.eclipse.jdt.internal.compiler.flow.FlowInfo)
 * @deprecated use instead {@link #analyseCode(ClassScope, InitializationFlowContext, FlowInfo, int)}
 */
public void analyseCode(ClassScope classScope, InitializationFlowContext initializerFlowContext, FlowInfo flowInfo) {
	analyseCode(classScope, initializerFlowContext, flowInfo, FlowInfo.REACHABLE);
}

/**
 * The flowInfo corresponds to non-static field initialization infos. It may be unreachable (155423), but still the explicit constructor call must be
 * analysed as reachable, since it will be generated in the end.
 */
public void analyseCode(ClassScope classScope, InitializationFlowContext initializerFlowContext, FlowInfo flowInfo, int initialReachMode) {
	if (this.ignoreFurtherInvestigation)
		return;

	int nonStaticFieldInfoReachMode = flowInfo.reachMode();
	flowInfo.setReachMode(initialReachMode);

//{ObjectTeams:
	// already processed? (see below "force called constructor to be analyzed")
	if (this.isCodeAnalyzed)
		return;
	this.isCodeAnalyzed = true;

	ReferenceBinding roleType = this.scope.enclosingSourceType();
	// don't analyze copied:
    if (this.isCopied) {
    	// ... except for base ctor issues:
    	if (MethodModel.callsBaseCtor(this.binding.copyInheritanceSrc))
    		MethodModel.setCallsBaseCtor(this);
    	if (   this.arguments == null 
    		&& !MethodModel.callsBaseCtor(this.binding)
    		&& roleType.baseclass() != null)
    	{
    		MethodModel.getModel(this).problemDetail = ProblemDetail.IllegalDefaultCtor;
    		this.isGenerated = true; // prevent conversion to DOM AST
    	}
        return;
    }
	// still waiting for statements to be generated?
	if (this.isGenerated && this.statements == null)
		return;
    // ctor of bound role needs exactly one base ctor call
    if (   roleType.baseclass() != null
    	&& this.constructorCall != null)
    {
    	// force called constructor to be analyzed:
    	MethodBinding selfCall = this.constructorCall.binding;
    	if (selfCall.declaringClass == this.binding.declaringClass)
    		selfCall.sourceMethod().analyseCode(classScope, initializerFlowContext, flowInfo.copy());

        boolean calledHere = MethodModel.callsBaseCtor(this.binding);
        boolean calledIndirectly = MethodModel.callsBaseCtor(selfCall);
		if (calledIndirectly) {
			ReferenceBinding requiredBase = roleType.baseclass();
			ReferenceBinding createdBase = selfCall.declaringClass.baseclass();
			if (requiredBase != createdBase)
				this.scope.problemReporter().
					callsCtorWithMismatchingBaseCtor(this.constructorCall,
													 selfCall.declaringClass,
													 requiredBase,
													 createdBase);
		}

        calledIndirectly |= (  !Lifting.isLiftToConstructor(this.binding, this.binding.declaringClass)
						    && Lifting.isLiftToConstructor(selfCall, selfCall.declaringClass));
                	       // if a non-lift ctor calls a lift-ctor this accounts for a base-ctor call.

        if (Lifting.isLiftToConstructor(this, this.binding.declaringClass)){
            if (calledHere || calledIndirectly)
                this.scope.problemReporter().baseConstructorCallInLiftingConstructor(this);
        } else {
            if (!calledHere && !calledIndirectly) {
            	if (!(isDefaultConstructor() && roleType.roleModel != null && roleType.roleModel.hasBaseclassProblem())) // ignore if wrong def.ctor was created 
            		this.scope.problemReporter().missingCallToBaseConstructor(
            				this, this.binding.declaringClass);
            } else if (calledHere && calledIndirectly) {
                this.scope.problemReporter().
                        tooManyCallsToBaseConstructor(this.statements[0], this.constructorCall);
            }
            if (!calledHere) {
            	// if this is an error, it is already reported. Don't complain again.
            	FieldBinding baseField = this.scope.enclosingSourceType().getField(IOTConstants._OT_BASE, true);
            	if (baseField != null)
					flowInfo.markAsDefinitelyAssigned(baseField);
            }
        }
        if (calledIndirectly)
        	MethodModel.setCallsBaseCtor(this);
    }
// SH}

//{ObjectTeams: don't report against role constructors (could be accessed by tsub role):
  if (   !classScope.referenceContext.isRole()
	  || (classScope.referenceContext.binding.isAnonymousType() && !classScope.referenceContext.isPurelyCopied)) // do report against non-copied anonymous types
//SH}
	checkUnused: {
		MethodBinding constructorBinding;
		if ((constructorBinding = this.binding) == null) break checkUnused;
		if ((this.bits & ASTNode.IsDefaultConstructor) != 0) break checkUnused;
		if (constructorBinding.isUsed()) break checkUnused;
		if (constructorBinding.isPrivate()) {
			if ((this.binding.declaringClass.tagBits & TagBits.HasNonPrivateConstructor) == 0)
				break checkUnused; // tolerate as known pattern to block instantiation
		} else if (!constructorBinding.isOrEnclosedByPrivateType()) {
			break checkUnused;
 		}
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=270446, When the AST built is an abridged version
		// we don't have all tree nodes we would otherwise expect. (see ASTParser.setFocalPosition)
		if (this.constructorCall == null)
			break checkUnused; 
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=264991, Don't complain about this
		// constructor being unused if the base class doesn't have a no-arg constructor.
		// See that a seemingly unused constructor that chains to another constructor with a
		// this(...) can be flagged as being unused without hesitation.
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=265142
		if (this.constructorCall.accessMode != ExplicitConstructorCall.This) {
			ReferenceBinding superClass = constructorBinding.declaringClass.superclass();
			if (superClass == null)
				break checkUnused;
			// see if there is a no-arg super constructor
			MethodBinding methodBinding = superClass.getExactConstructor(Binding.NO_PARAMETERS);
			if (methodBinding == null)
				break checkUnused;
			if (!methodBinding.canBeSeenBy(SuperReference.implicitSuperConstructorCall(), this.scope))
				break checkUnused;
			// otherwise default super constructor exists, so go ahead and complain unused.
		}
		// complain unused
		this.scope.problemReporter().unusedPrivateConstructor(this);
	}

	// check constructor recursion, once all constructor got resolved
	if (isRecursive(null /*lazy initialized visited list*/)) {
		this.scope.problemReporter().recursiveConstructorInvocation(this.constructorCall);
	}

	try {
		ExceptionHandlingFlowContext constructorContext =
			new ExceptionHandlingFlowContext(
				initializerFlowContext.parent,
				this,
				this.binding.thrownExceptions,
				initializerFlowContext,
				this.scope,
				FlowInfo.DEAD_END);
		initializerFlowContext.checkInitializerExceptions(
			this.scope,
			constructorContext,
			flowInfo);

		// anonymous constructor can gain extra thrown exceptions from unhandled ones
		if (this.binding.declaringClass.isAnonymousType()) {
			ArrayList computedExceptions = constructorContext.extendedExceptions;
			if (computedExceptions != null){
				int size;
				if ((size = computedExceptions.size()) > 0){
					ReferenceBinding[] actuallyThrownExceptions;
					computedExceptions.toArray(actuallyThrownExceptions = new ReferenceBinding[size]);
					this.binding.thrownExceptions = actuallyThrownExceptions;
				}
			}
		}

		// tag parameters as being set
		if (this.arguments != null) {
			for (int i = 0, count = this.arguments.length; i < count; i++) {
				flowInfo.markAsDefinitelyAssigned(this.arguments[i].binding);
			}
		}

		// propagate to constructor call
		if (this.constructorCall != null) {
			// if calling 'this(...)', then flag all non-static fields as definitely
			// set since they are supposed to be set inside other local constructor
			if (this.constructorCall.accessMode == ExplicitConstructorCall.This) {
				FieldBinding[] fields = this.binding.declaringClass.fields();
				for (int i = 0, count = fields.length; i < count; i++) {
					FieldBinding field;
					if (!(field = fields[i]).isStatic()) {
						flowInfo.markAsDefinitelyAssigned(field);
					}
				}
			}
			flowInfo = this.constructorCall.analyseCode(this.scope, constructorContext, flowInfo);
		}

		// reuse the reachMode from non static field info
		flowInfo.setReachMode(nonStaticFieldInfoReachMode);

		// propagate to statements
		if (this.statements != null) {
			int complaintLevel = (nonStaticFieldInfoReachMode & FlowInfo.UNREACHABLE) == 0 ? Statement.NOT_COMPLAINED : Statement.COMPLAINED_FAKE_REACHABLE;
			for (int i = 0, count = this.statements.length; i < count; i++) {
				Statement stat = this.statements[i];
				if ((complaintLevel = stat.complainIfUnreachable(flowInfo, this.scope, complaintLevel)) < Statement.COMPLAINED_UNREACHABLE) {
					flowInfo = stat.analyseCode(this.scope, constructorContext, flowInfo);
				}
			}
		}
		// check for missing returning path
		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0) {
			this.bits |= ASTNode.NeedFreeReturn;
		}

		// reuse the initial reach mode for diagnosing missing blank finals
		// no, we should use the updated reach mode for diagnosing uninitialized blank finals.
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=235781
		// flowInfo.setReachMode(initialReachMode);

		// check missing blank final field initializations
		if ((this.constructorCall != null)
//{ObjectTeams: no checking for some more cases:
			// don't need to check tsuper ctors:
			&& !TSuperHelper.isTSuper(this.binding)
			// tsuper() is implemented as this(), too (already containing field inits):
			&& (this.constructorCall.accessMode != ExplicitConstructorCall.Tsuper)
// SH}
			&& (this.constructorCall.accessMode != ExplicitConstructorCall.This)) {
			flowInfo = flowInfo.mergedWith(constructorContext.initsOnReturn);
			FieldBinding[] fields = this.binding.declaringClass.fields();
			for (int i = 0, count = fields.length; i < count; i++) {
				FieldBinding field;
				if ((!(field = fields[i]).isStatic())
					&& field.isFinal()
					&& (!flowInfo.isDefinitelyAssigned(fields[i]))) {
					this.scope.problemReporter().uninitializedBlankFinalField(
						field,
						((this.bits & ASTNode.IsDefaultConstructor) != 0) ? (ASTNode) this.scope.referenceType() : this);
				}
			}
		}
		// check unreachable catch blocks
		constructorContext.complainIfUnusedExceptionHandlers(this);
		// check unused parameters
		this.scope.checkUnusedParameters(this.binding);
	} catch (AbortMethod e) {
		this.ignoreFurtherInvestigation = true;
	}
}

/**
 * Bytecode generation for a constructor
 *
 * @param classScope org.eclipse.jdt.internal.compiler.lookup.ClassScope
 * @param classFile org.eclipse.jdt.internal.compiler.codegen.ClassFile
 */
public void generateCode(ClassScope classScope, ClassFile classFile) {
//{ObjectTeams: if copied for implicit inheritance just adjust and write out
    if(isRelevantCopied())
    {
        new BytecodeTransformer().checkCopyMethodCode(classFile, this);
        if (this.binding.bytecodeMissing) { // copy did not succeed
			int problemsLength;
			CategorizedProblem[] problems =
				this.scope.referenceCompilationUnit().compilationResult.getProblems();
			CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
			System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
			classFile.addProblemConstructor(this, this.binding, problemsCopy);
        }
        return;
    }
    if (areStatementsMissing() && (this.bits & ASTNode.IsDefaultConstructor) == 0 ) {
    	this.binding.bytecodeMissing = true;
    	return;
    }
// SH}
	int problemResetPC = 0;
	if (this.ignoreFurtherInvestigation) {
		if (this.binding == null)
			return; // Handle methods with invalid signature or duplicates
		int problemsLength;
		CategorizedProblem[] problems =
			this.scope.referenceCompilationUnit().compilationResult.getProblems();
		CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
		System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
		classFile.addProblemConstructor(this, this.binding, problemsCopy);
//{ObjectTeams: mark this erroneous, so we don't try to copy the byte code later:
        this.binding.bytecodeMissing = true;
// SH}
		return;
	}
	try {
		problemResetPC = classFile.contentsOffset;
		internalGenerateCode(classScope, classFile);
	} catch (AbortMethod e) {
		if (e.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
			// a branch target required a goto_w, restart code gen in wide mode.
			try {
				classFile.contentsOffset = problemResetPC;
				classFile.methodCount--;
				classFile.codeStream.resetInWideMode(); // request wide mode
				internalGenerateCode(classScope, classFile); // restart method generation
			} catch (AbortMethod e2) {
				int problemsLength;
				CategorizedProblem[] problems =
					this.scope.referenceCompilationUnit().compilationResult.getAllProblems();
				CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
				System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
				classFile.addProblemConstructor(this, this.binding, problemsCopy, problemResetPC);
			}
		} else {
			int problemsLength;
			CategorizedProblem[] problems =
				this.scope.referenceCompilationUnit().compilationResult.getAllProblems();
			CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
			System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
			classFile.addProblemConstructor(this, this.binding, problemsCopy, problemResetPC);
		}
	}
}

public void generateSyntheticFieldInitializationsIfNecessary(MethodScope methodScope, CodeStream codeStream, ReferenceBinding declaringClass) {
//{ObjectTeams: new kind of synthetics, may occur in non-nested types, too:
	SyntheticArgumentBinding[] syntheticArgs = declaringClass.valueParamSynthArgs();
	for (int i = 0, max = syntheticArgs == null ? 0 : syntheticArgs.length; i < max; i++) {
		SyntheticArgumentBinding syntheticArg;
		if ((syntheticArg = syntheticArgs[i]).matchingField != null) {
			codeStream.aload_0();
			codeStream.load(syntheticArg);
			codeStream.fieldAccess(Opcodes.OPC_putfield, syntheticArg.matchingField, declaringClass);
		}
	}
// SH}
	if (!declaringClass.isNestedType()) return;

	NestedTypeBinding nestedType = (NestedTypeBinding) declaringClass;

//{ObjectTeams: variable already declared above.
/* orig:
	SyntheticArgumentBinding[] syntheticArgs = nestedType.syntheticEnclosingInstances();
  :giro */
	syntheticArgs = nestedType.syntheticEnclosingInstances();
//SH}
	for (int i = 0, max = syntheticArgs == null ? 0 : syntheticArgs.length; i < max; i++) {
		SyntheticArgumentBinding syntheticArg;
		if ((syntheticArg = syntheticArgs[i]).matchingField != null) {
			codeStream.aload_0();
			codeStream.load(syntheticArg);
			codeStream.fieldAccess(Opcodes.OPC_putfield, syntheticArg.matchingField, null /* default declaringClass */);
		}
	}
	syntheticArgs = nestedType.syntheticOuterLocalVariables();
	for (int i = 0, max = syntheticArgs == null ? 0 : syntheticArgs.length; i < max; i++) {
		SyntheticArgumentBinding syntheticArg;
		if ((syntheticArg = syntheticArgs[i]).matchingField != null) {
			codeStream.aload_0();
			codeStream.load(syntheticArg);
			codeStream.fieldAccess(Opcodes.OPC_putfield, syntheticArg.matchingField, null /* default declaringClass */);
		}
	}
}

private void internalGenerateCode(ClassScope classScope, ClassFile classFile) {
	classFile.generateMethodInfoHeader(this.binding);
	int methodAttributeOffset = classFile.contentsOffset;
	int attributeNumber = classFile.generateMethodInfoAttribute(this.binding);
//{ObjectTeams: write OT-specific byte code attributes
    if (this.model != null)
        attributeNumber += this.model.writeAttributes(classFile);
// SH}
	if ((!this.binding.isNative()) && (!this.binding.isAbstract())) {

		TypeDeclaration declaringType = classScope.referenceContext;
		int codeAttributeOffset = classFile.contentsOffset;
		classFile.generateCodeAttributeHeader();
		CodeStream codeStream = classFile.codeStream;
		codeStream.reset(this, classFile);

		// initialize local positions - including initializer scope.
		ReferenceBinding declaringClass = this.binding.declaringClass;

		int enumOffset = declaringClass.isEnum() ? 2 : 0; // String name, int ordinal
		int argSlotSize = 1 + enumOffset; // this==aload0

		if (declaringClass.isNestedType()){
			this.scope.extraSyntheticArguments = declaringClass.syntheticOuterLocalVariables();
			this.scope.computeLocalVariablePositions(// consider synthetic arguments if any
					declaringClass.getEnclosingInstancesSlotSize() + 1 + enumOffset,
				codeStream);
			argSlotSize += declaringClass.getEnclosingInstancesSlotSize();
			argSlotSize += declaringClass.getOuterLocalVariablesSlotSize();
		} else {
//{ObjectTeams:	treat value parameters similar to enclosing instances:
			VariableBinding[] syntheticArguments = declaringClass.valueParamSynthArgs();
			argSlotSize += syntheticArguments.length;
/* orig:			
			this.scope.computeLocalVariablePositions(1 + enumOffset,  codeStream);
  :giro */
			this.scope.computeLocalVariablePositions(1 + enumOffset + syntheticArguments.length,  codeStream);
// SH}
		}

		if (this.arguments != null) {
			for (int i = 0, max = this.arguments.length; i < max; i++) {
				// arguments initialization for local variable debug attributes
				LocalVariableBinding argBinding;
				codeStream.addVisibleLocalVariable(argBinding = this.arguments[i].binding);
				argBinding.recordInitializationStartPC(0);
				switch(argBinding.type.id) {
					case TypeIds.T_long :
					case TypeIds.T_double :
						argSlotSize += 2;
						break;
					default :
						argSlotSize++;
						break;
				}
			}
		}

		MethodScope initializerScope = declaringType.initializerScope;
		initializerScope.computeLocalVariablePositions(argSlotSize, codeStream); // offset by the argument size (since not linked to method scope)

		boolean needFieldInitializations = this.constructorCall == null || this.constructorCall.accessMode != ExplicitConstructorCall.This;
//{ObjectTeams: some more constructors do not initialize fields:
		// if constructorCall is tsuper() the called ctor contains field initializations already.
		if (this.constructorCall != null)
			needFieldInitializations &= this.constructorCall.accessMode != ExplicitConstructorCall.Tsuper;
		// copied team constructors (due to arg lifting) do not initialize fields
		if (   !needFieldInitializations
			&& this.constructorCall != null
			&& this.constructorCall.binding.model != null
			&& this.constructorCall.binding.model.liftedParams != null)
		{
			needFieldInitializations = true;
		}
		// top confined types have no fields to initialize nor have they initFields methods:
		if (needFieldInitializations && TypeAnalyzer.isTopConfined(this.scope.enclosingReceiverType()))
			needFieldInitializations= false;
// SH}

		// post 1.4 target level, synthetic initializations occur prior to explicit constructor call
		boolean preInitSyntheticFields = this.scope.compilerOptions().targetJDK >= ClassFileConstants.JDK1_4;

		if (needFieldInitializations && preInitSyntheticFields){
			generateSyntheticFieldInitializationsIfNecessary(this.scope, codeStream, declaringClass);
		}
		// generate constructor call
		if (this.constructorCall != null) {
			this.constructorCall.generateCode(this.scope, codeStream);
		}
		// generate field initialization - only if not invoking another constructor call of the same class
		if (needFieldInitializations) {
			if (!preInitSyntheticFields){
				generateSyntheticFieldInitializationsIfNecessary(this.scope, codeStream, declaringClass);
			}
//{ObjectTeams: only non-role types have standard field initialization:
		  if (   !declaringType.isRole()
			  || (   declaringClass.enclosingType() != null // also accept roles of o.o.Team
				  && declaringClass.enclosingType().id == IOTConstants.T_OrgObjectTeamsTeam))
		  {
// orig:
			// generate user field initialization
			if (declaringType.fields != null) {
				for (int i = 0, max = declaringType.fields.length; i < max; i++) {
					FieldDeclaration fieldDecl;
					if (!(fieldDecl = declaringType.fields[i]).isStatic()) {
						fieldDecl.generateCode(initializerScope, codeStream);
					}
				}
			}
// :giro
		  } else
			callInit: if (!Lifting.isLiftingCtor(this.binding))
		  {
				// lifting ctor already contains the invoke statement
				MethodBinding[] initMethods = declaringType.binding.getMethods(IOTConstants.INIT_METHOD_NAME);
				if (initMethods.length >= 1)
				{
					int argCount = TSuperHelper.isTSuper(this.binding) ?  1 : 0;
					for (int i = 0; i < initMethods.length; i++) {
						if (initMethods[i].parameters.length == argCount) {
							codeStream.aload_0(); // this
							codeStream.invoke(Opcodes.OPC_invokevirtual, initMethods[i], declaringType.binding);
							break callInit;
						}
					}
				}
				// no matching role init method should mean we had errors.
				assert    TypeModel.isIgnoreFurtherInvestigation(classScope.referenceContext)
				       || (declaringClass.tagBits & TagBits.BaseclassHasProblems) != 0
				       || declaringClass.isTeam(); // might be the "turning constructor" of a nested team (see 2.1.11-otjld-*-1f)
		  }
// SH}
		}
		// generate statements
		if (this.statements != null) {
			for (int i = 0, max = this.statements.length; i < max; i++) {
				this.statements[i].generateCode(this.scope, codeStream);
			}
		}
		// if a problem got reported during code gen, then trigger problem method creation
		if (this.ignoreFurtherInvestigation) {
			throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
		}
		if ((this.bits & ASTNode.NeedFreeReturn) != 0) {
			codeStream.return_();
		}
		// local variable attributes
		codeStream.exitUserScope(this.scope);
		codeStream.recordPositionsFrom(0, this.bodyEnd);
		try {
			classFile.completeCodeAttribute(codeAttributeOffset);
		} catch (NegativeArraySizeException e) {
			throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
		}
//{ObjectTeams
		maybeRecordByteCode(classFile, methodAttributeOffset-6); // include method header
// SH}
		attributeNumber++;
		if ((codeStream instanceof StackMapFrameCodeStream)
				&& needFieldInitializations
				&& declaringType.fields != null) {
			((StackMapFrameCodeStream) codeStream).resetSecretLocals();
		}
	}
	classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);
}

//{ObjectTeams: ctors are copied for roles and teams:
@Override
public void maybeRecordByteCode(ClassFile classFile, int methodAttributeOffset) {
	RoleModel.maybeRecordByteCode(
	            this,
	            classFile,
	            methodAttributeOffset);
	if (this.binding.declaringClass.isTeam()) {
	   	// TODO (SH): could we optimize this, e.g., not copy the whole
	   	// class' byte code, but 'only' method and constant pool?
	   	MethodModel methodModel = MethodModel.getModel(this);
	   	methodModel.recordByteCode(classFile, methodAttributeOffset);
	}
}
// SH}

public boolean isConstructor() {
	return true;
}

public boolean isDefaultConstructor() {
	return (this.bits & ASTNode.IsDefaultConstructor) != 0;
}

public boolean isInitializationMethod() {
	return true;
}

/*
 * Returns true if the constructor is directly involved in a cycle.
 * Given most constructors aren't, we only allocate the visited list
 * lazily.
 */
public boolean isRecursive(ArrayList visited) {
	if (this.binding == null
			|| this.constructorCall == null
			|| this.constructorCall.binding == null
			|| this.constructorCall.isSuperAccess()
// {ObjectTeams
			|| this.constructorCall.isTsuperAccess()
// Markus Witte}
			|| !this.constructorCall.binding.isValidBinding()) {
		return false;
	}

	ConstructorDeclaration targetConstructor =
		((ConstructorDeclaration)this.scope.referenceType().declarationOf(this.constructorCall.binding.original()));
	if (this == targetConstructor) return true; // direct case

	if (visited == null) { // lazy allocation
		visited = new ArrayList(1);
	} else {
		int index = visited.indexOf(this);
		if (index >= 0) return index == 0; // only blame if directly part of the cycle
	}
	visited.add(this);

	return targetConstructor.isRecursive(visited);
}

public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
	//fill up the constructor body with its statements
	if (((this.bits & ASTNode.IsDefaultConstructor) != 0) && this.constructorCall == null){
		this.constructorCall = SuperReference.implicitSuperConstructorCall();
		this.constructorCall.sourceStart = this.sourceStart;
		this.constructorCall.sourceEnd = this.sourceEnd;
		return;
	}
//{ObjectTeams: generated don't have source code:
	if (this.isCopied || this.isGenerated)
		return;
// SH}
	parser.parse(this, unit, false);

}

//{ObjectTeams: create statements for throwing an IllegalRoleCreationException at run-time:
void createExceptionStatements() {
	AstGenerator gen = new AstGenerator(this.sourceStart, this.sourceEnd);
	this.constructorCall = gen.explicitConstructorCall(ExplicitConstructorCall.Super);
	this.statements = new Statement[] {
		gen.throwStatement(
				gen.allocation(
						gen.qualifiedTypeReference(IOTConstants.ILLEGAL_ROLE_CREATION_EXCEPTION),
						null))
	};
	this.hasParsedStatements = true;
	this.isCopied = false;
	resolve(this.scope.classScope());
}
// SH}

public StringBuffer printBody(int indent, StringBuffer output) {
	output.append(" {"); //$NON-NLS-1$
	if (this.constructorCall != null) {
		output.append('\n');
		this.constructorCall.printStatement(indent, output);
	}
	if (this.statements != null) {
		for (int i = 0; i < this.statements.length; i++) {
			output.append('\n');
			this.statements[i].printStatement(indent, output);
		}
	}
//{ObjectTeams: signal missing body for copy inherited methods
	else
	{
		if(this.isCopied==true && this.statements == null){
			output.append("\n"); //$NON-NLS-1$
			printIndent(indent == 0 ? 0 : indent - 1,output);
			output.append("     /* CopyInheritance */"); //$NON-NLS-1$
		}
	}
//Markus Witte}
	output.append('\n');
	printIndent(indent == 0 ? 0 : indent - 1, output).append('}');
	return output;
}

public void resolveJavadoc() {
	if (this.binding == null || this.javadoc != null) {
		super.resolveJavadoc();
	} else if ((this.bits & ASTNode.IsDefaultConstructor) == 0) {
		if (this.binding.declaringClass != null && !this.binding.declaringClass.isLocalType()) {
			// Set javadoc visibility
			int javadocVisibility = this.binding.modifiers & ExtraCompilerModifiers.AccVisibilityMASK;
			ClassScope classScope = this.scope.classScope();
			ProblemReporter reporter = this.scope.problemReporter();
			int severity = reporter.computeSeverity(IProblem.JavadocMissing);
			if (severity != ProblemSeverities.Ignore) {
				if (classScope != null) {
					javadocVisibility = Util.computeOuterMostVisibility(classScope.referenceType(), javadocVisibility);
				}
				int javadocModifiers = (this.binding.modifiers & ~ExtraCompilerModifiers.AccVisibilityMASK) | javadocVisibility;
				reporter.javadocMissing(this.sourceStart, this.sourceEnd, severity, javadocModifiers);
			}
		}
	}
}

/*
 * Type checking for constructor, just another method, except for special check
 * for recursive constructor invocations.
 */
public void resolveStatements() {
	SourceTypeBinding sourceType = this.scope.enclosingSourceType();
	if (!CharOperation.equals(sourceType.sourceName, this.selector)){
		this.scope.problemReporter().missingReturnType(this);
	}
//{ObjectTeams: don't resolve any thing for copied (not even generated super().
    if (this.isCopied)
        return;
// SH}
	if (this.typeParameters != null) {
		for (int i = 0, length = this.typeParameters.length; i < length; i++) {
			this.typeParameters[i].resolve(this.scope);
		}
	}
	if (this.binding != null && !this.binding.isPrivate()) {
		sourceType.tagBits |= TagBits.HasNonPrivateConstructor;
	}
	// if null ==> an error has occurs at parsing time ....
	if (this.constructorCall != null) {
		if (sourceType.id == TypeIds.T_JavaLangObject
				&& this.constructorCall.accessMode != ExplicitConstructorCall.This) {
			// cannot use super() in java.lang.Object
			if (this.constructorCall.accessMode == ExplicitConstructorCall.Super) {
				this.scope.problemReporter().cannotUseSuperInJavaLangObject(this.constructorCall);
			}
			this.constructorCall = null;
		} else {
//{ObjectTeams: some transformations:
		  // base() might replace existing constructorCall:
		  if (   this.statements != null
			  && this.statements.length > 0
			  && (this.statements[0] instanceof BaseAllocationExpression))
		  {
			 ((BaseAllocationExpression)this.statements[0]).checkGenerate(this.scope);
		  }
		  // prepare for resolving a special statement: constructorCall.
	      Config oldConfig = Config.createOrResetConfig(this);
		  try {
// orig:
			this.constructorCall.resolve(this.scope);
// :giro
			// order in this condition is relevant, first part resets flags!
			if (   Config.requireTypeAdjustment()
			    && !this.scope.problemReporter().referenceContext.hasErrors())
			{
				this.constructorCall.traverse(new InsertTypeAdjustmentsVisitor(), this.scope);
			}
		  } finally {
		    Config.removeOrRestore(oldConfig, this);
		  }
// SH}
		}
	}
	if ((this.modifiers & ExtraCompilerModifiers.AccSemicolonBody) != 0) {
		this.scope.problemReporter().methodNeedBody(this);
	}
	super.resolveStatements();
}

public void traverse(ASTVisitor visitor, ClassScope classScope) {
	if (visitor.visit(this, classScope)) {
		if (this.javadoc != null) {
			this.javadoc.traverse(visitor, this.scope);
		}
		if (this.annotations != null) {
			int annotationsLength = this.annotations.length;
			for (int i = 0; i < annotationsLength; i++)
				this.annotations[i].traverse(visitor, this.scope);
		}
		if (this.typeParameters != null) {
			int typeParametersLength = this.typeParameters.length;
			for (int i = 0; i < typeParametersLength; i++) {
				this.typeParameters[i].traverse(visitor, this.scope);
			}
		}
		if (this.arguments != null) {
			int argumentLength = this.arguments.length;
			for (int i = 0; i < argumentLength; i++)
				this.arguments[i].traverse(visitor, this.scope);
		}
		if (this.thrownExceptions != null) {
			int thrownExceptionsLength = this.thrownExceptions.length;
			for (int i = 0; i < thrownExceptionsLength; i++)
				this.thrownExceptions[i].traverse(visitor, this.scope);
		}
		if (this.constructorCall != null)
			this.constructorCall.traverse(visitor, this.scope);
		if (this.statements != null) {
			int statementsLength = this.statements.length;
			for (int i = 0; i < statementsLength; i++)
				this.statements[i].traverse(visitor, this.scope);
		}
	}
	visitor.endVisit(this, classScope);
}
public TypeParameter[] typeParameters() {
    return this.typeParameters;
}
}
