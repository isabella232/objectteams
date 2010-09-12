/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MessageSend.java 23405 2010-02-03 17:02:18Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nick Teryaev - fix for bug (https://bugs.eclipse.org/bugs/show_bug.cgi?id=40752)
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.CALLIN_FLAG_DEFINITELY_MISSING_BASECALL;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.CALLIN_FLAG_POTENTIALLY_MISSING_BASECALL;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.BaseCallMessageSend;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.BaseReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.AnchorMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.WeakenedTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CallinImplementorDyn;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CalloutImplementor;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.StandardElementGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

/**
 * OTDT changes:
 *
 * DECAPSULATION:
 * ==============
 * What: Decapsulation ignore private
 * How:  Avoid special treatment for private methods if decapsulation is involved:
 * 		+ generateCode() must not use invokespecial
 * 		+ manageSyntheticIfNecessary() must not create an access method
 *
 *
 * ROLE CLASS/INTERFACE:
 * =====================
 * What: Use invokeinterface for role methods
 * Why:  These methods are dispatched using the role interface of a RoleTypeBinding.
 *
 * Note: Each client generating a message send is responsible for providing a
 *       MethodBinding with declaringClass correctly set, since class/ifc adjustment
 *       is not performed for generated methods!
 *
 * FURTHER RESOLVE ISSUES:
 * =======================
 * What: Avoid double-resolving for receiver if this send is transformed during resolve.
 *
 * What: Check base calls: name&signature must match the enclosing method.
 *
 * What: Wrap return type, possibly using receiver to instantiate the type anchor.
 *
 *
 * Bracket lookup of the method binding with {before,after}MethodLookup
 * ------
 * What: Need to setup AnchorMapping
 * Why:  During method lookup, arguments may need to be instantiated using
 *       the actual receiver and arguments.
 *
 * What: May need to redirect static calls to the enclosing team.
 * How:  Invoke checkRedirectStatic.
 * Why:  Only after resolving receiver we know whether it is a type reference
 * 		 (and hence the method a static method).
 *
 * What: Redirect methods from predefined Team.Confined (afterMethodLookup)
 * Why:  Some methods are mentioned in Team.Confined but only for importing these
 * 	     from Object.
 *
 * What: Additional checking for anchored role type arguments
 * Why:  Cannot report errors during method lookup, because all potential
 *       matches are checked (not all of these should count for errors).
 *
 * What: Work against signature weakening.
 * How:  Insert casts for return value if needed.
 *
 * @version $Id: MessageSend.java 23405 2010-02-03 17:02:18Z stephan $
 */
public class MessageSend extends Expression implements InvocationSite {

	public Expression receiver;
	public char[] selector;
	public Expression[] arguments;
	public MethodBinding binding;							// exact binding resulting from lookup
	public MethodBinding syntheticAccessor;						// synthetic accessor for inner-emulation
	public TypeBinding expectedType;					// for generic method invocation (return type inference)

	public long nameSourcePosition ; //(start<<32)+end

	public TypeBinding actualReceiverType;
	public TypeBinding valueCast; // extra reference type cast to perform on method returned value
	public TypeReference[] typeArguments;
	public TypeBinding[] genericTypeArguments;

//{ObjectTeams:
	private boolean isDecapsulation = false;
	// special case: the role method call in a method pushed out to the enclosing team needs special resolving
	public boolean isPushedOutRoleMethodCall = false;
// SH}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	boolean nonStatic = !this.binding.isStatic();
	flowInfo = this.receiver.analyseCode(currentScope, flowContext, flowInfo, nonStatic).unconditionalInits();
	if (nonStatic) {
		this.receiver.checkNPE(currentScope, flowContext, flowInfo);
	}

	if (this.arguments != null) {
		int length = this.arguments.length;
		for (int i = 0; i < length; i++) {
			flowInfo = this.arguments[i].analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
		}
	}
	ReferenceBinding[] thrownExceptions;
	if ((thrownExceptions = this.binding.thrownExceptions) != Binding.NO_EXCEPTIONS) {
		if ((this.bits & ASTNode.Unchecked) != 0 && this.genericTypeArguments == null) {
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=277643, align with javac on JLS 15.12.2.6
			thrownExceptions = currentScope.environment().convertToRawTypes(this.binding.thrownExceptions, true, true);
		}
		// must verify that exceptions potentially thrown by this expression are caught in the method
		flowContext.checkExceptionHandlers(thrownExceptions, this, flowInfo.copy(), currentScope);
		// TODO (maxime) the copy above is needed because of a side effect into
		//               checkExceptionHandlers; consider protecting there instead of here;
		//               NullReferenceTest#test0510
	}
	manageSyntheticAccessIfNecessary(currentScope, flowInfo);
//{ObjectTeams: base calls via super:
	flowInfo = checkBaseCallsIfSuper(currentScope, flowInfo);
// SH}
	return flowInfo;
}
//{ObjectTeams: checkBaseCallsIfSuper
protected FlowInfo checkBaseCallsIfSuper(BlockScope currentScope, FlowInfo flowInfo) {
	MethodScope methodScope = currentScope.methodScope();
	if (methodScope == null) 
		return flowInfo;
	AbstractMethodDeclaration methodDecl = methodScope.referenceMethod();
	if (methodDecl == null || !methodDecl.isCallin()) 
		return flowInfo;
	if (!this.isSuperAccess())
		return flowInfo;
	MethodDeclaration callinMethod = (MethodDeclaration) methodDecl;
	if (MethodModel.hasCallinFlag(this.binding, CALLIN_FLAG_DEFINITELY_MISSING_BASECALL))
		return flowInfo; // no effect
	
	boolean definitelyViaSuper = !MethodModel.hasCallinFlag(this.binding, CALLIN_FLAG_POTENTIALLY_MISSING_BASECALL);
		
	LocalVariableBinding trackingVariable = callinMethod.baseCallTrackingVariable.binding;
	if (flowInfo.isDefinitelyAssigned(callinMethod.baseCallTrackingVariable)) {
		if (definitelyViaSuper)
			currentScope.problemReporter().definitelyDuplicateBasecall(this);
		else
			currentScope.problemReporter().potentiallyDuplicateBasecall(this);
	} else if (flowInfo.isPotentiallyAssigned(trackingVariable)) {
		currentScope.problemReporter().potentiallyDuplicateBasecall(this);
	} else {
		if (definitelyViaSuper) {
			flowInfo.markAsDefinitelyAssigned(trackingVariable);
		} else {
			FlowInfo potential = flowInfo.copy();
			potential.markAsDefinitelyAssigned(trackingVariable);
			flowInfo = FlowInfo.conditional(flowInfo.initsWhenTrue(), potential.initsWhenTrue());
		}
	}
	return flowInfo;
}
// SH}
/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#computeConversion(org.eclipse.jdt.internal.compiler.lookup.Scope, org.eclipse.jdt.internal.compiler.lookup.TypeBinding, org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
 */
public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
	if (runtimeTimeType == null || compileTimeType == null)
		return;
	// set the generic cast after the fact, once the type expectation is fully known (no need for strict cast)
	if (this.binding != null && this.binding.isValidBinding()) {
		MethodBinding originalBinding = this.binding.original();
		TypeBinding originalType = originalBinding.returnType;
	    // extra cast needed if method return type is type variable
		if (originalType.leafComponentType().isTypeVariable()) {
	    	TypeBinding targetType = (!compileTimeType.isBaseType() && runtimeTimeType.isBaseType())
	    		? compileTimeType  // unboxing: checkcast before conversion
	    		: runtimeTimeType;
	        this.valueCast = originalType.genericCast(targetType);
		} 	else if (this.binding == scope.environment().arrayClone
				&& runtimeTimeType.id != TypeIds.T_JavaLangObject
				&& scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5) {
					// from 1.5 source level on, array#clone() resolves to array type, but codegen to #clone()Object - thus require extra inserted cast
			this.valueCast = runtimeTimeType;
		}
        if (this.valueCast instanceof ReferenceBinding) {
			ReferenceBinding referenceCast = (ReferenceBinding) this.valueCast;
			if (!referenceCast.canBeSeenBy(scope)) {
//{ObjectTeams: Decapsulation:
			  if(!isDecapsulationAllowed(scope))
// SH}
	        	scope.problemReporter().invalidType(this,
	        			new ProblemReferenceBinding(
							CharOperation.splitOn('.', referenceCast.shortReadableName()),
							referenceCast,
							ProblemReasons.NotVisible));
			}
        }
	}
	super.computeConversion(scope, runtimeTimeType, compileTimeType);
}

/**
 * MessageSend code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	// generate receiver/enclosing instance access
	MethodBinding codegenBinding = this.binding.original();
	boolean isStatic = codegenBinding.isStatic();
	if (isStatic) {
		this.receiver.generateCode(currentScope, codeStream, false);
	} else if ((this.bits & ASTNode.DepthMASK) != 0 && this.receiver.isImplicitThis()) { // outer access ?
		// outer method can be reached through emulation if implicit access
		ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT);
		Object[] path = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
		codeStream.generateOuterAccess(path, this, targetType, currentScope);
	} else {
		this.receiver.generateCode(currentScope, codeStream, true);
		if ((this.bits & NeedReceiverGenericCast) != 0) {
			codeStream.checkcast(this.actualReceiverType);
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
//{ObjectTeams: calling a static role method requiring an enclosing team instance?
	// cf. AbstractQualifiedAllocationExpression.generateCode()
	if (codegenBinding.needsSyntheticEnclosingTeamInstance())
	{
		codeStream.iconst_0(); // dummy
		codeStream.generateSyntheticEnclosingInstanceValues(
				currentScope,
				(ReferenceBinding)this.actualReceiverType,
				null, /*enclosing instance*/
				this);
	}
// SH}
	// generate arguments
	generateArguments(this.binding, this.arguments, currentScope, codeStream);
	pc = codeStream.position;
	// actual message invocation
	if (this.syntheticAccessor == null){
		TypeBinding constantPoolDeclaringClass = CodeStream.getConstantPoolDeclaringClass(currentScope, codegenBinding, this.actualReceiverType, this.receiver.isImplicitThis());
		if (isStatic){
			codeStream.invoke(Opcodes.OPC_invokestatic, codegenBinding, constantPoolDeclaringClass);
//{ObjectTeams: decapsulated methods will not be private in the JVM any more:
/* orig:
		} else if((this.receiver.isSuper()) || codegenBinding.isPrivate()){
  :giro */
		} else if((this.receiver.isSuper()) || (codegenBinding.isPrivate() && !this.isDecapsulation)) 
		{
// SH}
			codeStream.invoke(Opcodes.OPC_invokespecial, codegenBinding, constantPoolDeclaringClass);
//{ObjectTeams: always use interface methods of role type binding:
/* orig:
		} else if (constantPoolDeclaringClass.isInterface()) { // interface or annotation type
  :giro */
		} else if (constantPoolDeclaringClass.isInterface()  // interface or annotation type
				   || constantPoolDeclaringClass instanceof RoleTypeBinding)
		{
// SH}
			codeStream.invoke(Opcodes.OPC_invokeinterface, codegenBinding, constantPoolDeclaringClass);
		} else {
			codeStream.invoke(Opcodes.OPC_invokevirtual, codegenBinding, constantPoolDeclaringClass);
		}
	} else {
		codeStream.invoke(Opcodes.OPC_invokestatic, this.syntheticAccessor, null /* default declaringClass */);
	}
	// required cast must occur even if no value is required
	if (this.valueCast != null) codeStream.checkcast(this.valueCast);
	if (valueRequired){
		// implicit conversion if necessary
		codeStream.generateImplicitConversion(this.implicitConversion);
	} else {
		boolean isUnboxing = (this.implicitConversion & TypeIds.UNBOXING) != 0;
		// conversion only generated if unboxing
		if (isUnboxing) codeStream.generateImplicitConversion(this.implicitConversion);
		switch (isUnboxing ? postConversionType(currentScope).id : codegenBinding.returnType.id) {
			case T_long :
			case T_double :
				codeStream.pop2();
				break;
			case T_void :
				break;
			default :
				codeStream.pop();
		}
	}
	codeStream.recordPositionsFrom(pc, (int)(this.nameSourcePosition >>> 32)); // highlight selector
}
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
 */
public TypeBinding[] genericTypeArguments() {
	return this.genericTypeArguments;
}

public boolean isSuperAccess() {
	return this.receiver.isSuper();
}
//{ObjectTeams: super OR tsuper?
protected boolean isAnySuperAccess() {
	return this.receiver.isSuper();
}
// SH}
public boolean isTypeAccess() {
	return this.receiver != null && this.receiver.isTypeReference();
}
public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo){

	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0)	return;

	// if method from parameterized type got found, use the original method at codegen time
	MethodBinding codegenBinding = this.binding.original();
	if (this.binding.isPrivate()
//{ObjectTeams:
		&& !this.isDecapsulation)
// SH}
	{

		// depth is set for both implicit and explicit access (see MethodBinding#canBeSeenBy)
		if (currentScope.enclosingSourceType() != codegenBinding.declaringClass){
			this.syntheticAccessor = ((SourceTypeBinding)codegenBinding.declaringClass).addSyntheticMethod(codegenBinding, false /* not super access there */);
			currentScope.problemReporter().needToEmulateMethodAccess(codegenBinding, this);
			return;
		}

	} else if (this.receiver instanceof QualifiedSuperReference){ // qualified super

		// qualified super need emulation always
		SourceTypeBinding destinationType = (SourceTypeBinding)(((QualifiedSuperReference)this.receiver).currentCompatibleType);
		this.syntheticAccessor = destinationType.addSyntheticMethod(codegenBinding, isSuperAccess());
		currentScope.problemReporter().needToEmulateMethodAccess(codegenBinding, this);
		return;

	} else if (this.binding.isProtected()){

//{ObjectTeams:
/* orig:
		SourceTypeBinding enclosingSourceType;
		if (((this.bits & ASTNode.DepthMASK) != 0)
				&& codegenBinding.declaringClass.getPackage()
					!= (enclosingSourceType = currentScope.enclosingSourceType()).getPackage()){

			SourceTypeBinding currentCompatibleType = (SourceTypeBinding)enclosingSourceType.enclosingTypeAt((this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT);
  :giro */
		SourceTypeBinding enclosingSourceType = currentScope.enclosingSourceType();
		int depth = getDepthForSynthMethodAccess(this.binding, enclosingSourceType);
		if (depth > 0)
		{
			SourceTypeBinding currentCompatibleType =
				(SourceTypeBinding)enclosingSourceType.enclosingTypeAt(depth);
// SH}
			this.syntheticAccessor = currentCompatibleType.addSyntheticMethod(codegenBinding, isSuperAccess());
			currentScope.problemReporter().needToEmulateMethodAccess(codegenBinding, this);
			return;
		}
	}
}
public int nullStatus(FlowInfo flowInfo) {
	return FlowInfo.UNKNOWN;
}

/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#postConversionType(Scope)
 */
public TypeBinding postConversionType(Scope scope) {
	TypeBinding convertedType = this.resolvedType;
	if (this.valueCast != null)
		convertedType = this.valueCast;
	int runtimeType = (this.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
	switch (runtimeType) {
		case T_boolean :
			convertedType = TypeBinding.BOOLEAN;
			break;
		case T_byte :
			convertedType = TypeBinding.BYTE;
			break;
		case T_short :
			convertedType = TypeBinding.SHORT;
			break;
		case T_char :
			convertedType = TypeBinding.CHAR;
			break;
		case T_int :
			convertedType = TypeBinding.INT;
			break;
		case T_float :
			convertedType = TypeBinding.FLOAT;
			break;
		case T_long :
			convertedType = TypeBinding.LONG;
			break;
		case T_double :
			convertedType = TypeBinding.DOUBLE;
			break;
		default :
	}
	if ((this.implicitConversion & TypeIds.BOXING) != 0) {
		convertedType = scope.environment().computeBoxingType(convertedType);
	}
	return convertedType;
}

public StringBuffer printExpression(int indent, StringBuffer output){

	if (!this.receiver.isImplicitThis()) this.receiver.printExpression(0, output).append('.');
	if (this.typeArguments != null) {
		output.append('<');
		int max = this.typeArguments.length - 1;
		for (int j = 0; j < max; j++) {
			this.typeArguments[j].print(0, output);
			output.append(", ");//$NON-NLS-1$
		}
		this.typeArguments[max].print(0, output);
		output.append('>');
	}
	output.append(this.selector).append('(') ;
	if (this.arguments != null) {
		for (int i = 0; i < this.arguments.length ; i ++) {
			if (i > 0) output.append(", "); //$NON-NLS-1$
			this.arguments[i].printExpression(0, output);
		}
	}
	return output.append(')');
}

public TypeBinding resolveType(BlockScope scope) {
	// Answer the signature return type
	// Base type promotion

	this.constant = Constant.NotAConstant;
	boolean receiverCast = false, argsContainCast = false;
	if (this.receiver instanceof CastExpression) {
		this.receiver.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
		receiverCast = true;
	}
//{ObjectTeams: receiver may already be resolved, keep that result:
  if (this.receiver.resolvedType != null)
	this.actualReceiverType = this.receiver.resolvedType;
  else
// orig: Note: result from resolveType() may be null while resolvedType is MissingTypeBinding
	this.actualReceiverType = this.receiver.resolveType(scope);
// :giro
  /*orig:
	boolean receiverIsType = this.receiver instanceof NameReference && (((NameReference) this.receiver).bits & Binding.TYPE) != 0;
   */
    // don't only expect NameReference, BaseReference can be static, too.
	boolean receiverIsType = (   (this.receiver instanceof NameReference)
							  || (this.receiver instanceof BaseReference))
							 && (this.receiver.bits & Binding.TYPE) != 0;
// MW,JH,SH}
	if (receiverCast && this.actualReceiverType != null) {
		 // due to change of declaring class with receiver type, only identity cast should be notified
		if (((CastExpression)this.receiver).expression.resolvedType == this.actualReceiverType) {
			scope.problemReporter().unnecessaryCast((CastExpression)this.receiver);
		}
	}
	// resolve type arguments (for generic constructor call)
	if (this.typeArguments != null) {
		int length = this.typeArguments.length;
		boolean argHasError = scope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_5; // typeChecks all arguments
		this.genericTypeArguments = new TypeBinding[length];
		for (int i = 0; i < length; i++) {
			TypeReference typeReference = this.typeArguments[i];
			if ((this.genericTypeArguments[i] = typeReference.resolveType(scope, true /* check bounds*/)) == null) {
				argHasError = true;
			}
			if (argHasError && typeReference instanceof Wildcard) {
				scope.problemReporter().illegalUsageOfWildcard(typeReference);
			}
		}
		if (argHasError) {
			if (this.arguments != null) { // still attempt to resolve arguments
				for (int i = 0, max = this.arguments.length; i < max; i++) {
					this.arguments[i].resolveType(scope);
				}
			}
			return null;
		}
	}
	// will check for null after args are resolved
	TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
	if (this.arguments != null) {
		boolean argHasError = false; // typeChecks all arguments
		int length = this.arguments.length;
		argumentTypes = new TypeBinding[length];
		for (int i = 0; i < length; i++){
			Expression argument = this.arguments[i];
			if (argument instanceof CastExpression) {
				argument.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
				argsContainCast = true;
			}
//{ObjectTeams: arguments might already be resolved, see e.g. CastExpression.createRoleCheck
		  if (argument.resolvedType != null) {
			argumentTypes[i] = argument.resolvedType;
		  } else {
// orig:
			if ((argumentTypes[i] = argument.resolveType(scope)) == null){
				argHasError = true;
			}
// :giro
		  }
// SH}
		}
		if (argHasError) {
			if (this.actualReceiverType instanceof ReferenceBinding) {
				//  record a best guess, for clients who need hint about possible method match
				TypeBinding[] pseudoArgs = new TypeBinding[length];
				for (int i = length; --i >= 0;)
					pseudoArgs[i] = argumentTypes[i] == null ? TypeBinding.NULL : argumentTypes[i]; // replace args with errors with null type
				this.binding =
					this.receiver.isImplicitThis()
						? scope.getImplicitMethod(this.selector, pseudoArgs, this)
						: scope.findMethod((ReferenceBinding) this.actualReceiverType, this.selector, pseudoArgs, this);
				if (this.binding != null && !this.binding.isValidBinding()) {
					MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
					// record the closest match, for clients who may still need hint about possible method match
					if (closestMatch != null) {
						if (closestMatch.original().typeVariables != Binding.NO_TYPE_VARIABLES) { // generic method
							// shouldn't return generic method outside its context, rather convert it to raw method (175409)
							closestMatch = scope.environment().createParameterizedGenericMethod(closestMatch.original(), (RawTypeBinding)null);
						}
						this.binding = closestMatch;
						MethodBinding closestMatchOriginal = closestMatch.original();
						if (closestMatchOriginal.isOrEnclosedByPrivateType() && !scope.isDefinedInMethod(closestMatchOriginal)) {
							// ignore cases where method is used from within inside itself (e.g. direct recursions)
							closestMatchOriginal.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
						}
					}
				}
//{ObjectTeams: more tagging as used:
				else if (this.binding != null && this.binding.isOrEnclosedByPrivateType() && !scope.isDefinedInMethod(this.binding)) {
					// ignore cases where method is used from within inside itself (e.g. direct recursions)
					this.binding.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
				}
// SH}
			}
			return null;
		}
	}
	if (this.actualReceiverType == null) {
		return null;
	}
	// base type cannot receive any message
	if (this.actualReceiverType.isBaseType()) {
		scope.problemReporter().errorNoMethodFor(this, this.actualReceiverType, argumentTypes);
		return null;
	}
//{ObjectTeams: revert receiver weakening & setup anchorMapping:
	if (this.actualReceiverType instanceof WeakenedTypeBinding) {
		// this happens if receiver is a role-type field from a super team, see B.1.1-otjld-sh-32.
		this.actualReceiverType = ((WeakenedTypeBinding)this.actualReceiverType).getStrongType();
		this.bits |= NeedReceiverGenericCast; // addes a cast to this.actualReceiverType;
	}
	if (this.receiver.isThis() && !receiverIsQualifiedThis() && this.actualReceiverType.isRole()) {
		this.actualReceiverType = ((ReferenceBinding)this.actualReceiverType).getRealClass();
		if (this.actualReceiverType == null) // happens with true role ifc
			this.actualReceiverType = ((ReferenceBinding)this.receiver.resolvedType).getRealType();
	}
	AnchorMapping anchorMapping = null;
	int baseTypeModifiers = -1;
	ReferenceBinding receiverReferenceType = (this.actualReceiverType instanceof ReferenceBinding) ?
												(ReferenceBinding)this.actualReceiverType :
												null;
    // forward declaration of original local used below:
    TypeBinding returnType = null;
  try {
	anchorMapping = beforeMethodLookup(argumentTypes, scope);
	if (isDecapsulationAllowed(scope) && receiverReferenceType != null) {
	    // for now pretend all base classes are public/visible (avoid ReceiverTypeNotVisible)
		baseTypeModifiers = receiverReferenceType.modifiers;
    	receiverReferenceType.modifiers &= ~ExtraCompilerModifiers.AccVisibilityMASK;
    	receiverReferenceType.modifiers |= ClassFileConstants.AccPublic;
	}
// SH}
//{ObjectTeams: for calls to lower() resolve using internal selector _OT$getBase:
	char[] realSelector = this.selector;
	if (   CharOperation.equals(this.selector, IOTConstants.LOWER)
		&& this.actualReceiverType.isRole()
		&& this.arguments == null
		&& this.actualReceiverType.isCompatibleWith(scope.getType(
				new char[][] {IOTConstants.ORG, IOTConstants.OBJECTTEAMS, IOTConstants.ITEAM, IOTConstants.ILOWERABLE},
				4)))
		this.selector=IOTConstants._OT_GETBASE;
// orig:
	this.binding = this.receiver.isImplicitThis()
			? scope.getImplicitMethod(this.selector, argumentTypes, this)
			: scope.getMethod(this.actualReceiverType, this.selector, argumentTypes, this);
// :giro			
	this.selector = realSelector;
// SH}
//{ObjectTeams: postprocessing
    // wrap return type with knowledge of anchor mapping
	returnType = RoleTypeCreator.maybeWrapQualifiedRoleType(this, scope);
  } finally {
	// cleanup (required)
    AnchorMapping.removeCurrentMapping(anchorMapping);
	if (baseTypeModifiers != -1)
	   	receiverReferenceType.modifiers = baseTypeModifiers; // not-null if baseTypeModifiers != -1
  }
  // more checks requiring the binding to be known (possibly updating returnType):
  returnType = afterMethodLookup(scope, anchorMapping, argumentTypes, returnType);
// SH}
//{ObjectTeams illegal call to callin? (do this before reporting mismatching args - where callin args are in fact enhanced!)
	int bindingModifiers = this.binding.modifiers;
	if (!this.binding.isValidBinding()) {
		ProblemMethodBinding problem = (ProblemMethodBinding)this.binding;
		if (problem.closestMatch != null)
			bindingModifiers = problem.closestMatch.modifiers;
	}
	if ((bindingModifiers & ExtraCompilerModifiers.AccCallin) != 0) {
		AbstractMethodDeclaration enclosingMethod = scope.methodScope().referenceMethod();
		boolean callinAllowed;
		if (enclosingMethod == null)
			callinAllowed = false;
		else if (isAnySuperAccess())
			callinAllowed = ((enclosingMethod.modifiers & ExtraCompilerModifiers.AccCallin) != 0);
		else
			callinAllowed = enclosingMethod.isGenerated;
		if (!callinAllowed) {
			// Note that the problem reporter wrapper of a base call ignores this error:
			scope.problemReporter().callToCallin(this.binding, this);
			return this.resolvedType = null;
		}
	}
	// can we infer a callout to implement the missing method?
	if (this.binding.problemId() == ProblemReasons.NotFound) {
		if (CharOperation.prefixEquals(IOTConstants.OT_DOLLAR_NAME, this.selector) && scope.referenceType().ignoreFurtherInvestigation)
			return null; // type already reported error, this message send is obviously generated, don't bother any more
		if (CalloutImplementor.inferMappingFromCall(scope.referenceType(), this, argumentTypes)) {
			scope.problemReporter().usingInferredCalloutForMessageSend(this);
			returnType = this.binding.returnType;
		}
	} else {
		CalloutMappingDeclaration callout = MethodModel.getImplementingInferredCallout(this.binding); // reusing previously inferred callout?
		if (callout != null) {
			if (callout.isCalloutToField())
				scope.problemReporter().usingInferredCalloutToFieldForMessageSend(this); // error
			else
				scope.problemReporter().usingInferredCalloutForMessageSend(this);		 // warning
		}
	}
// SH}
	if (!this.binding.isValidBinding()) {
		if (this.binding.declaringClass == null) {
			if (this.actualReceiverType instanceof ReferenceBinding) {
				this.binding.declaringClass = (ReferenceBinding) this.actualReceiverType;
			} else {
				scope.problemReporter().errorNoMethodFor(this, this.actualReceiverType, argumentTypes);
				return null;
			}
		}
//{ObjectTeams: Decapsulation:
		if(   this.binding.problemId() == ProblemReasons.NotVisible
		   && isDecapsulationAllowed(scope))
		{
			this.binding = ((ProblemMethodBinding)this.binding).closestMatch;
			if (!this.binding.declaringClass.isRole()) { // access via interface is possible anyway, no access wrapper needed.
				// instruct the OTRE to generate an accessor method:
				scope.enclosingSourceType().roleModel.addInaccessibleBaseMethod(this.binding);
				// pretend that accessor method were already there:
				this.binding = new MethodBinding(this.binding, this.binding.declaringClass.getRealClass());
				this.binding.selector = CharOperation.concat(IOTConstants.OT_DECAPS, this.selector);
			}
			this.isDecapsulation = true;
			scope.problemReporter().decapsulation(this, scope);
			try {
				anchorMapping = AnchorMapping.setupNewMapping(receiverForAnchorMapping(scope), this.arguments, scope);
				returnType = RoleTypeCreator.maybeWrapQualifiedRoleType(this, scope); // update
			} finally {
				AnchorMapping.removeCurrentMapping(anchorMapping);
			}
		} else {
// orig:
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=245007 avoid secondary errors in case of
		// missing super type for anonymous classes ... 
		ReferenceBinding declaringClass = this.binding.declaringClass;
		boolean avoidSecondary = declaringClass != null &&
								 declaringClass.isAnonymousType() &&
								 declaringClass.superclass() instanceof MissingTypeBinding;
		if (!avoidSecondary)
			scope.problemReporter().invalidMethod(this, this.binding);
		MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
		switch (this.binding.problemId()) {
			case ProblemReasons.Ambiguous :
				break; // no resilience on ambiguous
			case ProblemReasons.NotVisible :
			case ProblemReasons.NonStaticReferenceInConstructorInvocation :
			case ProblemReasons.NonStaticReferenceInStaticContext :
			case ProblemReasons.ReceiverTypeNotVisible :
			case ProblemReasons.ParameterBoundMismatch :
				// only steal returnType in cases listed above
				if (closestMatch != null) this.resolvedType = closestMatch.returnType;
				break;
		}
		// record the closest match, for clients who may still need hint about possible method match
		if (closestMatch != null) {
			this.binding = closestMatch;
			MethodBinding closestMatchOriginal = closestMatch.original();
			if (closestMatchOriginal.isOrEnclosedByPrivateType() && !scope.isDefinedInMethod(closestMatchOriginal)) {
				// ignore cases where method is used from within inside itself (e.g. direct recursions)
				closestMatchOriginal.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
			}
		}
		return (this.resolvedType != null && (this.resolvedType.tagBits & TagBits.HasMissingType) == 0)
						? this.resolvedType
						: null;
// :giro
		}
// SH}
	}
//{ObjectTeams: new check (base.m() within m() ?)
    if (this.receiver instanceof BaseReference)
    {
    	if (!CallinImplementorDyn.DYNAMIC_WEAVING) { // FIXME(OTDYN): need a new strategy to check this for dyn weaving.
	        AbstractMethodDeclaration enclosingMethod = scope.methodScope().referenceMethod();
	        if (!enclosingMethod.isCallin())
	        	enclosingMethod = BaseCallMessageSend.getOuterCallinMethod(scope.methodScope());
	        MethodBinding basecallSurrogate = null;
	        if (enclosingMethod.model != null)
	        	basecallSurrogate = enclosingMethod.model.getBaseCallSurrogate();
	        if (this.binding != basecallSurrogate)
	        	scope.problemReporter().baseCallNotSameMethod(enclosingMethod, this);
    	}
    }
// SH}
	if ((this.binding.tagBits & TagBits.HasMissingType) != 0) {
		scope.problemReporter().missingTypeInMethod(this, this.binding);
	}
	final CompilerOptions compilerOptions = scope.compilerOptions();
	if (!this.binding.isStatic()) {
		// the "receiver" must not be a type
		if (receiverIsType) {
			scope.problemReporter().mustUseAStaticMethod(this, this.binding);
			if (this.actualReceiverType.isRawType()
					&& (this.receiver.bits & ASTNode.IgnoreRawTypeCheck) == 0
					&& compilerOptions.getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore) {
				scope.problemReporter().rawTypeReference(this.receiver, this.actualReceiverType);
			}
		} else {
			// handle indirect inheritance thru variable secondary bound
			// receiver may receive generic cast, as part of implicit conversion
			TypeBinding oldReceiverType = this.actualReceiverType;
			this.actualReceiverType = this.actualReceiverType.getErasureCompatibleType(this.binding.declaringClass);
			this.receiver.computeConversion(scope, this.actualReceiverType, this.actualReceiverType);
			if (this.actualReceiverType != oldReceiverType && this.receiver.postConversionType(scope) != this.actualReceiverType) { // record need for explicit cast at codegen since receiver could not handle it
				this.bits |= NeedReceiverGenericCast;
			}
		}
	} else {
		// static message invoked through receiver? legal but unoptimal (optional warning).
		if (!(this.receiver.isImplicitThis() || this.receiver.isSuper() || receiverIsType)) {
			scope.problemReporter().nonStaticAccessToStaticMethod(this, this.binding);
		}
		if (!this.receiver.isImplicitThis() && this.binding.declaringClass != this.actualReceiverType) {
			scope.problemReporter().indirectAccessToStaticMethod(this, this.binding);
		}
	}
	if (checkInvocationArguments(scope, this.receiver, this.actualReceiverType, this.binding, this.arguments, argumentTypes, argsContainCast, this)) {
		this.bits |= ASTNode.Unchecked;
	}

	//-------message send that are known to fail at compile time-----------
	if (this.binding.isAbstract()) {
		if (this.receiver.isSuper()) {
			scope.problemReporter().cannotDireclyInvokeAbstractMethod(this, this.binding);
		}
		// abstract private methods cannot occur nor abstract static............
	}
//{ObjectTeams: use copy inheritance src if possible (scope analysis and error reporting):
/* orig:
	if (isMethodUseDeprecated(this.binding, scope, true))
		scope.problemReporter().deprecatedMethod(this.binding, this);
  :giro */
  {
	MethodBinding origMethod = this.binding.copyInheritanceSrc != null ? this.binding.copyInheritanceSrc : this.binding;
	if (isMethodUseDeprecated(origMethod, scope, true))
		scope.problemReporter().deprecatedMethod(origMethod, this);
  }
// SH}

//{ObjectTeams: forbidden creation method?
	if (   this.binding.model != null
			&& this.binding.model.isForbiddenCreationMethod())
	{
		scope.problemReporter().abstractRoleIsRelevant(
				this, this.binding.returnType);
	}
// SH}
	// from 1.5 source level on, array#clone() returns the array type (but binding still shows Object)
	if (this.binding == scope.environment().arrayClone && compilerOptions.sourceLevel >= ClassFileConstants.JDK1_5) {
		this.resolvedType = this.actualReceiverType;
	} else {
//{ObjectTeams: use (adjusted) type from above and further adjust
/* orig:
		TypeBinding returnType;
 */
		if ((this.bits & ASTNode.Unchecked) != 0 && this.genericTypeArguments == null) {
			// TODO(SH): 3.5: should this branch be affected by the checks below, too?
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=277643, align with javac on JLS 15.12.2.6
			returnType = this.binding.returnType;
			if (returnType != null) {
				returnType = scope.environment().convertToRawType(returnType.erasure(), true);
			}
		} else {
/*
			returnType = this.binding.returnType;
 :giro */
			// check role-type return type in non-generated methods:
			if (  !scope.isGeneratedScope()
				&& returnType instanceof ReferenceBinding)
			{
				if (StandardElementGenerator.isCastToMethod(this.selector)) {
					this.resolvedType = this.binding.returnType; // pre-set to avoid wrapping this type // FIXME(SH): redundant?!
				} else {
					// signature weakening might have produced the wrong returnType.
					// check if we must cast this expression to the strengthened version:
					returnType = checkStrengthenReturnType(returnType, scope);
				}
			}
// orig:
			if (returnType != null) {
				returnType = returnType.capture(scope, this.sourceEnd);
			}
// :giro
		}
// SH}
		this.resolvedType = returnType;
	}
	if (this.receiver.isSuper() && compilerOptions.getSeverity(CompilerOptions.OverridingMethodWithoutSuperInvocation) != ProblemSeverities.Ignore) {
		final ReferenceContext referenceContext = scope.methodScope().referenceContext;
		if (referenceContext instanceof AbstractMethodDeclaration) {
			final AbstractMethodDeclaration abstractMethodDeclaration = (AbstractMethodDeclaration) referenceContext;
			MethodBinding enclosingMethodBinding = abstractMethodDeclaration.binding;
			if (enclosingMethodBinding.isOverriding()
					&& CharOperation.equals(this.binding.selector, enclosingMethodBinding.selector)
					&& this.binding.areParametersEqual(enclosingMethodBinding)) {
				abstractMethodDeclaration.bits |= ASTNode.OverridingMethodWithSupercall;
			}
		}
	}
	if (this.typeArguments != null && this.binding.original().typeVariables == Binding.NO_TYPE_VARIABLES) {
		scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(this.binding, this.genericTypeArguments, this.typeArguments);
	}
	return (this.resolvedType.tagBits & TagBits.HasMissingType) == 0
				? this.resolvedType
				: null;
}

//{ObjectTeams: utils:
private boolean receiverIsQualifiedThis() {
	if (this.receiver instanceof QualifiedThisReference)
		return true;
	if (this.receiver instanceof BaseReference)
		return (((BaseReference)this.receiver).isQualified());
	return false;
}
public boolean isDecapsulationAllowed(Scope scope) {
	MethodScope methodScope = scope.methodScope();
	// Note: methodScope() may return initializerScope, which has no Method, but the Type as refcontext.
	if (methodScope != null && methodScope.referenceContext instanceof AbstractMethodDeclaration) {
		AbstractMethodDeclaration method = (AbstractMethodDeclaration) methodScope.referenceContext;
		return method.isCalloutWrapper() || method.isBasePredicate();
	}
	return false;
}
// SH}
//{ObjectTeams: Hooks around method lookup (using getMethod/getImplicitMethod):
protected AnchorMapping beforeMethodLookup(
		TypeBinding[] argumentTypes, Scope scope)
{
	AnchorMapping result = null;
	try {
		if (this.actualReceiverType instanceof ReferenceBinding) // funny thing: receiver could be array..
			Dependencies.ensureBindingState(
	            (ReferenceBinding)this.actualReceiverType,
	            ITranslationStates.STATE_METHODS_CREATED); // creates bindings for possible target methods (incl. shorthand callout) 
	} finally {
		// it is essential that we setup an anchor mapping in any case.
		result = AnchorMapping.setupNewMapping(receiverForAnchorMapping(scope), this.arguments, scope);
	}
	return result;
}
/* The role method call in a callin wrapper should not set the receiver,
 * because it appears in the role context, but needs an explicit role receiver.
 */
private Expression receiverForAnchorMapping(Scope scope) {
	MethodScope methodScope = scope.methodScope();
	if (methodScope != null && methodScope.isCallinWrapper() && this.isPushedOutRoleMethodCall)
    	return null; // pretend the call target is already "this" = the role.

	return this.receiver;
}

protected TypeBinding afterMethodLookup(Scope scope, AnchorMapping anchorMapping, TypeBinding[] argumentTypes, TypeBinding returnType)
{
	// tweak methods of predefined confined types:
	if (   this.binding.isValidBinding()
		&& CharOperation.equals(this.binding.declaringClass.compoundName, IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED)
		&& !CharOperation.equals(this.selector, IOTConstants._OT_GETTEAM))
	{
		// methods found in a predefined confined type are actually methods of Object
		// (except for _OT$getTeam() which is generated for each role):
		ReferenceBinding object = scope.getJavaLangObject();
		this.binding = scope.findMethod(object, this.binding.selector, this.binding.parameters, this);
	}

	// check whether all anchors in the signature are final
	// (this step was skipped during instantiate parameters).
	if (this.binding.isValidBinding() && anchorMapping != null)
		if (!anchorMapping.checkInstantiatedParameters(this, scope)) {
			// store a problem binding with the instantiated arguments for further error reporting:
			MethodBinding instantiatedMethod = new MethodBinding(this.binding, (ReferenceBinding)this.actualReceiverType);
			instantiatedMethod.parameters = anchorMapping.getInstantiatedParameters(this.binding);
			this.binding = new ProblemMethodBinding(instantiatedMethod, this.selector, this.binding.parameters, ProblemReasons.NotFound);
		}

	// check access to static role method via the interface
	if (this.binding.isStatic() && this.actualReceiverType.isInterface()) {
		// static method in an interface can only occur in a role.
		ReferenceBinding receiverType = (ReferenceBinding)this.actualReceiverType;
		assert receiverType.isRole();
		ReferenceBinding classPart = receiverType.roleModel.getClassPartBinding();
		this.actualReceiverType = classPart; // used when calling getUpdatedMethodBinding() and during codeGen
	}

	// check role-type return type in non-generated methods:
	if (  !scope.isGeneratedScope()
		&& this.binding.returnType instanceof ReferenceBinding)
	{
		if (StandardElementGenerator.isCastToMethod(this.selector)) {
			this.resolvedType = this.binding.returnType; // pre-set to avoid wrapping this type
		}
	}
	return returnType; // not changed
}
/** Is this message send legally referring to a role's baseclass? */
@Override
public DecapsulationState getBaseclassDecapsulation() {
	if (CharOperation.equals(this.selector, IOTConstants._OT_GETBASE))
//		return DecapsulationState.REPORTED;
//	if (CopyInheritance.isCreator(this.selector))
		return DecapsulationState.REPORTED;
	return this.receiver.getBaseclassDecapsulation();
}
/* if a cast for the return value is needed (due to weakening) perform necessary changes
 * on resolvedType and valueCast, return the strengthenedType. */
private TypeBinding checkStrengthenReturnType(TypeBinding returnType, Scope scope)
{
	ReferenceBinding currentType = (ReferenceBinding) returnType.leafComponentType();
	if (currentType.isTypeVariable()) {
		TypeBinding typeBound= ((TypeVariableBinding)currentType).firstBound;
		if (typeBound instanceof ReferenceBinding)
			currentType= (ReferenceBinding)typeBound;
	}
	if (!currentType.isRole())
		return returnType; // unchanged

	ReferenceBinding strengthenedReturnType = null;

	if (currentType instanceof WeakenedTypeBinding) {
		WeakenedTypeBinding weakenedTypeBinding = (WeakenedTypeBinding)currentType;
		if (!weakenedTypeBinding.isSignificantlyWeakened())
			return returnType; // unchanged

		strengthenedReturnType = weakenedTypeBinding.getStrongType();

	} else {
		ReferenceBinding site = (ReferenceBinding)this.actualReceiverType;
		if (this.receiver.isSuper())
			site = scope.enclosingSourceType();
		strengthenedReturnType = (ReferenceBinding)TeamModel.strengthenRoleType(site, currentType);
		if (strengthenedReturnType == currentType)
			return returnType; //unchanged
		
		// if strengthenRoleType actually worked it returned a dependent type
		this.resolvedType = WeakenedTypeBinding.makeWeakenedTypeBinding((DependentTypeBinding) strengthenedReturnType, currentType, returnType.dimensions());
	}
	// schedule for generating a cast
	this.valueCast = strengthenedReturnType;
	return strengthenedReturnType; // this is what clients should see
}
// SH}
//{ObjectTeams: references to the enclosing team need synthetic accessors.
//              see also: Reference.getDepthForSynthFieldAccess(..)
/**
 * @param methodBinding
 * @param enclosingSourceType
 * @return depth of field's declaring class as seen from enclosingSourceTypes or -1.
 */
protected int getDepthForSynthMethodAccess(MethodBinding methodBinding, SourceTypeBinding enclosingSourceType)
{
	int depth = (this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT;

	if (methodBinding.isPrivate())
		return depth;
	if (!methodBinding.isProtected())
		return -1;
	if (methodBinding.declaringClass.getPackage() == enclosingSourceType.getPackage()) {
		depth = TeamModel.levelFromEnclosingTeam(methodBinding.declaringClass, enclosingSourceType);
		// through copy inheritance this code could be executed within a different package!
		if (depth == 0)
			return -1; // neither a team field, nor an access across packages
	}
	return depth;
}
// SH}

public void setActualReceiverType(ReferenceBinding receiverType) {
	if (receiverType == null) return; // error scenario only
	this.actualReceiverType = receiverType;
}
public void setDepth(int depth) {
	this.bits &= ~ASTNode.DepthMASK; // flush previous depth if any
	if (depth > 0) {
		this.bits |= (depth & 0xFF) << ASTNode.DepthSHIFT; // encoded on 8 bits
	}
}

/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#setExpectedType(org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
 */
public void setExpectedType(TypeBinding expectedType) {
    this.expectedType = expectedType;
}
public void setFieldIndex(int depth) {
	// ignore for here
}
public TypeBinding expectedType() {
	return this.expectedType;
}

public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		this.receiver.traverse(visitor, blockScope);
		if (this.typeArguments != null) {
			for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
				this.typeArguments[i].traverse(visitor, blockScope);
			}
		}
		if (this.arguments != null) {
			int argumentsLength = this.arguments.length;
			for (int i = 0; i < argumentsLength; i++)
				this.arguments[i].traverse(visitor, blockScope);
		}
	}
	visitor.endVisit(this, blockScope);
}
}
