/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 367203 - [compiler][null] detect assigning null to nonnull argument
 *								bug 365519 - editorial cleanup after bug 186342 and bug 365387
 *								bug 365662 - [compiler][null] warn on contradictory and redundant null annotations
 *								bug 365531 - [compiler][null] investigate alternative strategy for internally encoding nullness defaults
 *								bug 388281 - [compiler][null] inheritance of null annotations as an option
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 417295 - [1.8[[null] Massage type annotated null analysis to gel well with deep encoded type bindings.
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 425152 - [1.8] [compiler] Lambda Expression not resolved but flow analyzed leading to NPE.
 *								Bug 423505 - [1.8] Implement "18.5.4 More Specific Method Inference"
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *								Bug 438012 - [1.8][null] Bogus Warning: The nullness annotation is redundant with a default that applies to this location
 *								Bug 440759 - [1.8][null] @NonNullByDefault should never affect wildcards and uses of a type variable
 *								Bug 443347 - [1.8][null] @NonNullByDefault should not affect constructor arguments of an anonymous instantiation
 *								Bug 435805 - [1.8][compiler][null] Java 8 compiler does not recognize declaration style null annotations
 *								Bug 466713 - Null Annotations: NullPointerException using <int @Nullable []> as Type Param
 *								Bug 456584 - [1.8][null] Bogus warning for return type variable's @NonNull annotation being 'redundant'
 *								Bug 471611 - Error on hover on call to generic method with null annotation
 *     Jesper Steen Moller - Contributions for
 *								Bug 412150 [1.8] [compiler] Enable reflected parameter names during annotation processing
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.RecordComponent;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationPosition;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions.WeavingScheme;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AnchorListAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticRoleBridgeMethodBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.MethodSignatureEnhancer;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.IProtectable;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.Protections;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;

/**
 * OTDT changes:
 *
 * STRUCTURE:
 * ==========
 * What: Additional, OT-specific info is in a MethodModel instance, which
 *       is associated to a method binding, as needed.
 * Note: This MethodModel has to be passed along every time a new MethodBinding
 *       is created either from AST or from another MethodBinding.
 *
 * What: New flags copyInheritanceSrc and overriddenTSuper
 *       Both flags link to a tsuper version, the first for purely
 *       copied methods, the latter for overriding methods.
 * Why:  Mainly, ConstantPoolObjectMapper needs these links to find
 *       tsub-versions of method bindings retrieved from the source method.
 * Further notes on copyInheritanceSrc:
 *       + this field is furthermore used as a flag to signal that
 *         this method is a pure copy.
 *       + during CopyInheritance it is transitively followed, to actually
 *         enter the original version of a method that is copied along
 *         several levels.
 *       + While resolving types for binary methods, this field is set
 * 	       similarily as during CopyInheritance.
 *       + It is used during error reporting to mention the source-method
 *         not any copy.
 *
 *
 *
 * What: Analysis for calls to a base-constructor.
 * Why:  Constructors of bound roles (other than the lifting constructor)
 *       must directly or indirectly invoke base().
 *
 *
 * Runtime checks in role creation methods are scattered over these classes:
 * + AllocationExpression.resolveType
 *   detect unsafe use of lifting constructor
 *   -> record in MethodBinding.roleCreatorRequiringRuntimeCheck
 * + AbstractMethodDeclaration.generateCode(ClassFile)
 *   if roleCreatorRequiringRuntimeCheck is set:
 *   -> call Lifting.createDuplicateRoleCheck to create the byte code sequence
 */
//{ObjectTeams: added IProtectable
/* orig:
public class MethodBinding extends Binding {
  :giro */
public class MethodBinding extends Binding implements IProtectable {
// SH}

	public int modifiers;
	public char[] selector;
	public TypeBinding returnType;
	public TypeBinding[] parameters;
	public TypeBinding receiver;  // JSR308 - explicit this parameter
	public ReferenceBinding[] thrownExceptions;
	public ReferenceBinding declaringClass;
	public TypeVariableBinding[] typeVariables = Binding.NO_TYPE_VARIABLES;
	char[] signature;
	public long tagBits;
	// Used only for constructors
	protected AnnotationBinding [] typeAnnotations = Binding.NO_ANNOTATIONS;

//{ObjectTeams:
	public int otBits;
	public MethodModel model = null;

    // back references for byte code adjustment
    // Note, that this is always the real source, possibly several levels away from this method.
    public MethodBinding copyInheritanceSrc = null;
    public void setCopyInheritanceSrc(MethodBinding tsuperMethod) {
    	this.copyInheritanceSrc = tsuperMethod;
    	if (isCallin()) {
    		MethodBinding surrogate = MethodModel.getModel(this).getBaseCallSurrogate();
    		if (surrogate != null) {
	    		if (tsuperMethod == null)
	    			surrogate.copyInheritanceSrc = null;
	    		else
	    			surrogate.copyInheritanceSrc = MethodModel.getModel(tsuperMethod).getBaseCallSurrogate();
    		}
    	}
    	if (tsuperMethod != null && tsuperMethod.declaringClass.isRole() && tsuperMethod instanceof ParameterizedMethodBinding)
    		this.otBits |= IOTConstants.IsCopyOfParameterized;
    }

    // set of tsuper methods being overridden by this method
    public MethodBinding[] overriddenTSupers = null;
    public void addOverriddenTSuper(MethodBinding tsuperBinding) {
    	this.modifiers |= ExtraCompilerModifiers.AccOverriding;
    	if (this.overriddenTSupers == null) {
    		this.overriddenTSupers = new MethodBinding[]{tsuperBinding};
    	} else {
    		int length = this.overriddenTSupers.length;
    		for (int i = 0; i < length; i++) {
				if (this.overriddenTSupers[i] == tsuperBinding)
					return;
			}
    		System.arraycopy(
    				this.overriddenTSupers, 0,
    				this.overriddenTSupers=new MethodBinding[length+1], 1,
					length);
    		this.overriddenTSupers[0] = tsuperBinding;
    	}
		if (tsuperBinding.isCallin()) {
			MethodBinding basecallSurrogate = MethodModel.getModel(this).getBaseCallSurrogate();
			if (basecallSurrogate != null) { // might be reusing an inherited surrogate
				MethodBinding tsuperBasecallSurrogate = MethodModel.getModel(tsuperBinding).getBaseCallSurrogate();
				basecallSurrogate.addOverriddenTSuper(tsuperBasecallSurrogate);
			}
  		}
    }
    public boolean overridesTSuper(MethodBinding tsuperBinding) {
    	if (this.overriddenTSupers != null) {
    		for (int i = 0; i < this.overriddenTSupers.length; i++) {
				if (this.overriddenTSupers[i] == tsuperBinding)
					return true;
			}
    	}
		return false;
    }

    // Copied methods store the context of copying (enclosing type of the target team),
    // so we don't copy the same method again in a different outer team context:
    // TODO (SH): need to store this in a byte code attribute, too.
    public ReferenceBinding copiedInContext = null;
    // when reading binary methods store anchor information here:
    public AnchorListAttribute anchorList = null;

    // is byte code missing due to compile errors?
    public boolean bytecodeMissing = false;

    // is this a role creation method required runtime check for DuplicateRoleException?
    public boolean roleCreatorRequiringRuntimeCheck = false;

    // additional queries:
    /** Callin method or replace callin wrapper */
    public boolean isAnyCallin() {
    	return (this.modifiers & ExtraCompilerModifiers.AccCallin) != 0;
    }
    /** Source level callin method only. */
    public boolean isCallin() {
    	return (this.modifiers & ExtraCompilerModifiers.AccCallin) != 0 && !CharOperation.contains('$', this.selector);
    }
    /** Has this method been (indirectly) copied from other? */
    public boolean isCopiedFrom(MethodBinding other) {
    	if (this.copyInheritanceSrc == null)
    		return false;
    	if (this.copyInheritanceSrc == other)
    		return true;
    	return this.copyInheritanceSrc == other.copyInheritanceSrc;
    }
    /** Static role methods require synthetic arguments.
     *  Note: the actual values are produced by MessageSend.generateCode().
     */
    public boolean needsSyntheticEnclosingTeamInstance() {
    	if (this.declaringClass == null)
    		return false;
    	if (needsSyntheticEnclosingTeamInstance(this.modifiers, this.declaringClass, this.selector))
    		return !MethodModel.isFakedMethod(this, MethodModel.FakeKind.BASECALL_SURROGATE);
    	return false;
    }
    static boolean needsSyntheticEnclosingTeamInstance(int modifiers, ReferenceBinding declaringClass, char[] selector)
    {
    	int EXPECTED_MODIFIERS = ClassFileConstants.AccStatic;
    	int UNEXPECTED_MODIFIERS = ClassFileConstants.AccSynthetic;
    	int filteredModifiers = modifiers & (EXPECTED_MODIFIERS | UNEXPECTED_MODIFIERS);
    	if (filteredModifiers != EXPECTED_MODIFIERS)
    		return false;
    	if (CharOperation.prefixEquals(IOTConstants.OT_GETFIELD, selector))
    		return false;
    	if (CharOperation.prefixEquals(IOTConstants.OT_SETFIELD, selector))
    		return false;
    	if (SyntheticRoleBridgeMethodBinding.isPrivateBridgeSelector(selector))
    		return false;
    	return declaringClass.isRole();
    }
    /** Implement {@link IProtectable#getDeclaringClass} */
	@Override
	public ReferenceBinding getDeclaringClass() {
		return this.declaringClass;
	}
	/** Implement {@link IProtectable#modifiers} */
	@Override
	public int modifiers() {
		return this.modifiers;
	}
// SH}

	/** Store nullness information from annotation (incl. applicable default). */
	public Boolean[] parameterNonNullness;  // TRUE means @NonNull declared, FALSE means @Nullable declared, null means nothing declared
	public int defaultNullness; // for null *type* annotations

	/** Store parameter names from MethodParameters attribute (incl. applicable default). */
	public char[][] parameterNames = Binding.NO_PARAMETER_NAMES;

protected MethodBinding() {
	// for creating problem or synthetic method
}
public MethodBinding(int modifiers, char[] selector, TypeBinding returnType, TypeBinding[] parameters, ReferenceBinding[] thrownExceptions, ReferenceBinding declaringClass) {
	this.modifiers = modifiers;
	this.selector = selector;
	this.returnType = returnType;
	this.parameters = (parameters == null || parameters.length == 0) ? Binding.NO_PARAMETERS : parameters;
	this.thrownExceptions = (thrownExceptions == null || thrownExceptions.length == 0) ? Binding.NO_EXCEPTIONS : thrownExceptions;
	this.declaringClass = declaringClass;

	// propagate the strictfp & deprecated modifiers
	if (this.declaringClass != null) {
		if (this.declaringClass.isStrictfp())
			if (!(isNative() || isAbstract()))
				this.modifiers |= ClassFileConstants.AccStrictfp;
	}
}
public MethodBinding(int modifiers, TypeBinding[] parameters, ReferenceBinding[] thrownExceptions, ReferenceBinding declaringClass) {
	this(modifiers, TypeConstants.INIT, TypeBinding.VOID, parameters, thrownExceptions, declaringClass);
}
// special API used to change method declaring class for runtime visibility check
public MethodBinding(MethodBinding initialMethodBinding, ReferenceBinding declaringClass) {
	this.modifiers = initialMethodBinding.modifiers;
	this.selector = initialMethodBinding.selector;
	this.returnType = initialMethodBinding.returnType;
	this.parameters = initialMethodBinding.parameters;
	this.thrownExceptions = initialMethodBinding.thrownExceptions;
	this.declaringClass = declaringClass;
	declaringClass.storeAnnotationHolder(this, initialMethodBinding.declaringClass.retrieveAnnotationHolder(initialMethodBinding, true));
//{ObjectTeams: add new field:
	this.typeVariables = initialMethodBinding.typeVariables;
// SH}
}
/* Answer true if the argument types & the receiver's parameters have the same erasure
*/
public final boolean areParameterErasuresEqual(MethodBinding method) {
	TypeBinding[] args = method.parameters;
//{ObjectTeams: retrench callin methods (avoids undue creation of bridge method)
	TypeBinding[] realParameters = this.parameters;
	if (isCallin() && method.isCallin()) { // comparing two callin methods?
		args = method.getSourceParameters();
		this.parameters = getSourceParameters();
	}
  try {
// orig:
	if (this.parameters == args)
		return true;

	int length = this.parameters.length;
	if (length != args.length)
		return false;

	for (int i = 0; i < length; i++)
		if (TypeBinding.notEquals(this.parameters[i], args[i]) && TypeBinding.notEquals(this.parameters[i].erasure(), args[i].erasure()))
			return false;
// :giro
  } finally {
	  this.parameters = realParameters;
  }
// SH}
	return true;
}
/*
 * Returns true if given parameters are compatible with this method parameters.
 * Callers to this method should first check that the number of TypeBindings
 * passed as argument matches this MethodBinding number of parameters
 */
public final boolean areParametersCompatibleWith(TypeBinding[] arguments) {
	int paramLength = this.parameters.length;
	int argLength = arguments.length;
	int lastIndex = argLength;
	if (isVarargs()) {
		lastIndex = paramLength - 1;
		if (paramLength == argLength) { // accept X[] but not X or X[][]
			TypeBinding varArgType = this.parameters[lastIndex]; // is an ArrayBinding by definition
			TypeBinding lastArgument = arguments[lastIndex];
			if (TypeBinding.notEquals(varArgType, lastArgument) && !lastArgument.isCompatibleWith(varArgType))
				return false;
		} else if (paramLength < argLength) { // all remainig argument types must be compatible with the elementsType of varArgType
			TypeBinding varArgType = ((ArrayBinding) this.parameters[lastIndex]).elementsType();
			for (int i = lastIndex; i < argLength; i++)
				if (TypeBinding.notEquals(varArgType, arguments[i]) && !arguments[i].isCompatibleWith(varArgType))
					return false;
		} else if (lastIndex != argLength) { // can call foo(int i, X ... x) with foo(1) but NOT foo();
			return false;
		}
		// now compare standard arguments from 0 to lastIndex
	}
	for (int i = 0; i < lastIndex; i++)
		if (TypeBinding.notEquals(this.parameters[i], arguments[i]) && !arguments[i].isCompatibleWith(this.parameters[i]))
			return false;
	return true;
}
/* Answer true if the argument types & the receiver's parameters are equal
*/
public final boolean areParametersEqual(MethodBinding method) {
	TypeBinding[] args = method.parameters;
	if (this.parameters == args)
		return true;

	int length = this.parameters.length;
	if (length != args.length)
		return false;

	for (int i = 0; i < length; i++)
		if (TypeBinding.notEquals(this.parameters[i], args[i]))
			return false;
	return true;
}

/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

/* Answer true if the type variables have the same erasure
*/
public final boolean areTypeVariableErasuresEqual(MethodBinding method) {
	TypeVariableBinding[] vars = method.typeVariables;
	if (this.typeVariables == vars)
		return true;

	int length = this.typeVariables.length;
	if (length != vars.length)
		return false;

	for (int i = 0; i < length; i++)
		if (TypeBinding.notEquals(this.typeVariables[i], vars[i]) && TypeBinding.notEquals(this.typeVariables[i].erasure(), vars[i].erasure()))
			return false;
	return true;
}
public MethodBinding asRawMethod(LookupEnvironment env) {
	if (this.typeVariables == Binding.NO_TYPE_VARIABLES) return this;

	// substitute type arguments with raw types
	int length = this.typeVariables.length;
	TypeBinding[] arguments = new TypeBinding[length];
	for (int i = 0; i < length; i++) {
		arguments[i] = makeRawArgument(env, this.typeVariables[i]);
	}
	return env.createParameterizedGenericMethod(this, arguments);
}
private TypeBinding makeRawArgument(LookupEnvironment env, TypeVariableBinding var) {
	if (var.boundsCount() <= 1) {
		TypeBinding upperBound = var.upperBound();
		if (upperBound.isTypeVariable())
			return makeRawArgument(env, (TypeVariableBinding) upperBound);
		return env.convertToRawType(upperBound, false /*do not force conversion of enclosing types*/);
	} else {
		// use an intersection type to retain full bound information if more than 1 bound
		TypeBinding[] itsSuperinterfaces = var.superInterfaces();
		int superLength = itsSuperinterfaces.length;
		TypeBinding rawFirstBound = null;
		TypeBinding[] rawOtherBounds = null;
		if (var.boundsCount() == superLength) {
			rawFirstBound = env.convertToRawType(itsSuperinterfaces[0], false);
			rawOtherBounds = new TypeBinding[superLength - 1];
			for (int s = 1; s < superLength; s++)
				rawOtherBounds[s - 1] = env.convertToRawType(itsSuperinterfaces[s], false);
		} else {
			rawFirstBound = env.convertToRawType(var.superclass(), false);
			rawOtherBounds = new TypeBinding[superLength];
			for (int s = 0; s < superLength; s++)
				rawOtherBounds[s] = env.convertToRawType(itsSuperinterfaces[s], false);
		}
		return env.createWildcard(null, 0, rawFirstBound, rawOtherBounds, org.eclipse.jdt.internal.compiler.ast.Wildcard.EXTENDS);
	}
}

/* Answer true if the receiver is visible to the type provided by the scope.
* InvocationSite implements isSuperAccess() to provide additional information
* if the receiver is protected.
*
* NOTE: This method should ONLY be sent if the receiver is a constructor.
*
* NOTE: Cannot invoke this method with a compilation unit scope.
*/

public final boolean canBeSeenBy(InvocationSite invocationSite, Scope scope) {
	if (isPublic()) return true;

	SourceTypeBinding invocationType = scope.enclosingSourceType();
	if (TypeBinding.equalsEquals(invocationType, this.declaringClass)) return true;

	if (isProtected()) {
//{ObjectTeams: for role check enclosing team rather than package:
		if (this.declaringClass.isDirectRole())
			return TeamModel.findEnclosingTeamContainingRole(invocationType, this.declaringClass) != null;
// SH}
		// answer true if the receiver is in the same package as the invocationType
		if (invocationType.fPackage == this.declaringClass.fPackage) return true;
		return invocationSite.isSuperAccess();
	}

	if (isPrivate()) {
		// answer true if the invocationType and the declaringClass have a common enclosingType
		// already know they are not the identical type

//{ObjectTeams: private role features are not visible outside the role
//              (role nested types are not affected by this rule).
		if (this.declaringClass.isDirectRole())
			return false;
//SH}

		ReferenceBinding outerInvocationType = invocationType;
		ReferenceBinding temp = outerInvocationType.enclosingType();
		while (temp != null) {
			outerInvocationType = temp;
			temp = temp.enclosingType();
		}

		ReferenceBinding outerDeclaringClass = (ReferenceBinding)this.declaringClass.erasure();
		temp = outerDeclaringClass.enclosingType();
		while (temp != null) {
			outerDeclaringClass = temp;
			temp = temp.enclosingType();
		}
		return TypeBinding.equalsEquals(outerInvocationType, outerDeclaringClass);
	}

	// isDefault()
//{ObjectTeams: default visible role features are not visible outside the role hierarchy
//              (role nested types are not affected by this rule).
	if (this.declaringClass.isDirectRole()) {
		MethodScope methodScope = scope.methodScope();
		if (methodScope != null && methodScope.isCallinWrapper())
			return true;
		return invocationSite.isSuperAccess();
	}
//SH}
	return invocationType.fPackage == this.declaringClass.fPackage;
}
public final boolean canBeSeenBy(PackageBinding invocationPackage) {
	if (isPublic()) return true;
	if (isPrivate()) return false;

	// isProtected() or isDefault()
	return invocationPackage == this.declaringClass.getPackage();
}

//{ObjectTeams: variant to respect original scope for creator methods:
public final boolean canBeSeenBy(Scope origImplicitScope, TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
	if (   origImplicitScope != null
		&& CharOperation.prefixEquals(IOTConstants.CREATOR_PREFIX_NAME, this.selector))
	{
		// use the scope and recieverType where the search started:
		return canBeSeenBy(origImplicitScope.enclosingReceiverType(), invocationSite, origImplicitScope);
	}
	return canBeSeenBy(receiverType, invocationSite, scope);
}
// SH}
/* Answer true if the receiver is visible to the type provided by the scope.
* InvocationSite implements isSuperAccess() to provide additional information
* if the receiver is protected.
*
* NOTE: Cannot invoke this method with a compilation unit scope.
*/
public final boolean canBeSeenBy(TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {

	SourceTypeBinding invocationType = scope.enclosingSourceType();
//{ObjectTeams: staticness is no problem in synthetic role interfaces:
  if (!this.declaringClass.isSynthInterface())
// SH}
	if (this.declaringClass.isInterface() && isStatic() && !isPrivate()) {
		// Static interface methods can be explicitly invoked only through the type reference of the declaring interface or implicitly in the interface itself or via static import.
		if (scope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_8)
			return false;
		if ((invocationSite.isTypeAccess() || invocationSite.receiverIsImplicitThis()) && TypeBinding.equalsEquals(receiverType, this.declaringClass))
			return true;
		return false;
	}

	if (isPublic()) return true;


//{ObjectTeams:
	// creator methods conceptually belong to the role, check for stored orig ctor:
	if (this.model != null && this.model._srcCtor != null) {
		// combine a dummy method: only declaring class and modifiers count:
		MethodBinding fake = new MethodBinding();
		fake.declaringClass = this.model._srcCtor.declaringClass;
		fake.modifiers = this.modifiers;
		return fake.canBeSeenBy(invocationSite, scope);
	}
	TypeBinding origReceiver = receiverType;
    receiverType = receiverType.leafComponentType();

    // strip off RoleTypeBinding for determining visibility
    ReferenceBinding receiverClass = null;
    if (receiverType.isRole()) {
    	receiverClass = ((ReferenceBinding)receiverType).getRealClass();
    	receiverType = ((ReferenceBinding)receiverType).getRealType();
    }

    // short-cut for generated methods (here: callin wrappers)
	if (scope.methodScope() != null && scope.methodScope().isCallinWrapper())
		if (   !isPrivate()
			|| TypeBinding.equalsEquals(receiverType, this.declaringClass)
			|| TypeBinding.equalsEquals(receiverClass, this.declaringClass))
		{
			return true;
		}
// SH}
//{ObjectTeams: use receiverClass, if available
/* orig:
	if (TypeBinding.equalsEquals(invocationType, this.declaringClass) && TypeBinding.equalsEquals(invocationType, receiverType)) return true;
  :giro */
	if (TypeBinding.equalsEquals(invocationType, this.declaringClass) && TypeBinding.equalsEquals(invocationType, receiverClass != null ? receiverClass : receiverType)) return true;
// SH}

	if (invocationType == null) // static import call
		return !isPrivate() && scope.getCurrentPackage() == this.declaringClass.fPackage;

//{ObjectTeams: different rules for role methods:
	if (this.declaringClass.isDirectRole())
		return Protections.canBeSeenBy(this, origReceiver, invocationSite, scope);
// SH}

	if (isProtected()) {
		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the receiverType is the invocationType or its subclass
		//    OR the method is a static method accessed directly through a type
		//    OR previous assertions are true for one of the enclosing type
		if (TypeBinding.equalsEquals(invocationType, this.declaringClass)) return true;
		if (invocationType.fPackage == this.declaringClass.fPackage) return true;

		ReferenceBinding currentType = invocationType;
//{ObjectTeams: use class here:
/* orig:
		TypeBinding receiverErasure = receiverType.erasure();
  :giro*/
		TypeBinding receiverErasure = receiverClass != null ? receiverClass.erasure()
									: receiverType.erasure();
// SH}
		ReferenceBinding declaringErasure = (ReferenceBinding) this.declaringClass.erasure();
		int depth = 0;
		do {
			if (currentType.findSuperTypeOriginatingFrom(declaringErasure) != null) {
				if (invocationSite.isSuperAccess())
					return true;
				// receiverType can be an array binding in one case... see if you can change it
				if (receiverType instanceof ArrayBinding)
					return false;
				if (isStatic()) {
					if (depth > 0) invocationSite.setDepth(depth);
					return true; // see 1FMEPDL - return invocationSite.isTypeAccess();
				}
				if (TypeBinding.equalsEquals(currentType, receiverErasure) || receiverErasure.findSuperTypeOriginatingFrom(currentType) != null) {
					if (depth > 0) invocationSite.setDepth(depth);
					return true;
				}
			}
			depth++;
			currentType = currentType.enclosingType();
		} while (currentType != null);
		return false;
	}

	if (isPrivate()) {
//{ObjectTeams: temporarily switch 'declaringClass'
//              (make comparable to adjusted receiverType).
	  ReferenceBinding declaringClassOrig = this.declaringClass;
	  this.declaringClass = this.declaringClass.getRealType();
	  try {
// SH}

		// answer true if the receiverType is the declaringClass
		// AND the invocationType and the declaringClass have a common enclosingType
		receiverCheck: {
			if (TypeBinding.notEquals(receiverType, this.declaringClass)) {
				// special tolerance for type variable direct bounds, but only if compliance <= 1.6, see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=334622
				if (scope.compilerOptions().complianceLevel <= ClassFileConstants.JDK1_6 && receiverType.isTypeVariable() && ((TypeVariableBinding) receiverType).isErasureBoundTo(this.declaringClass.erasure()))
					break receiverCheck;
				return false;
			}
		}

		if (TypeBinding.notEquals(invocationType, this.declaringClass)) {
			ReferenceBinding outerInvocationType = invocationType;
			ReferenceBinding temp = outerInvocationType.enclosingType();
			while (temp != null) {
				outerInvocationType = temp;
				temp = temp.enclosingType();
			}

			ReferenceBinding outerDeclaringClass = (ReferenceBinding)this.declaringClass.erasure();
			temp = outerDeclaringClass.enclosingType();
			while (temp != null) {
				outerDeclaringClass = temp;
				temp = temp.enclosingType();
			}
			if (TypeBinding.notEquals(outerInvocationType, outerDeclaringClass)) return false;
		}
		return true;
//{ObjectTeams: reset:
	  } finally {
		  this.declaringClass = declaringClassOrig;
	  }
// SH}
	}

	// isDefault()
	PackageBinding declaringPackage = this.declaringClass.fPackage;
	if (invocationType.fPackage != declaringPackage) return false;

	// receiverType can be an array binding in one case... see if you can change it
	if (receiverType instanceof ArrayBinding)
		return false;
	TypeBinding originalDeclaringClass = this.declaringClass.original();
//{ObjectTeams: changed receiverType to receiverClass:
/* orig:
	ReferenceBinding currentType = (ReferenceBinding) (receiverType);
  :giro */
	ReferenceBinding currentType = receiverClass != null ? receiverClass
									: (ReferenceBinding) receiverType;
// SH}
	do {
		if (currentType.isCapture()) { // https://bugs.eclipse.org/bugs/show_bug.cgi?id=285002
			if (TypeBinding.equalsEquals(originalDeclaringClass, currentType.erasure().original())) return true;
		} else {
			if (TypeBinding.equalsEquals(originalDeclaringClass, currentType.original())) return true;
		}
		PackageBinding currentPackage = currentType.fPackage;
		// package could be null for wildcards/intersection types, ignore and recurse in superclass
//{ObjectTeams: if leaving the package, we still have an alternate plan (below)
    /* orig
		if (!currentType.isCapture() && currentPackage != null && currentPackage != declaringPackage) return false;
     */
		if (!currentType.isCapture() && currentPackage != null && currentPackage != declaringPackage) break;
// orig:
	} while ((currentType = currentType.superclass()) != null);
// :giro
    currentType = (ReferenceBinding)receiverType;
    if (currentType.isSynthInterface()) {
        if (Protections.findSuperIfcInPackage(currentType, this.declaringClass, declaringPackage))
            return true;
    }
// SH}
	return false;
}

public List<TypeBinding> collectMissingTypes(List<TypeBinding> missingTypes) {
	if ((this.tagBits & TagBits.HasMissingType) != 0) {
		missingTypes = this.returnType.collectMissingTypes(missingTypes);
		for (int i = 0, max = this.parameters.length; i < max; i++) {
			missingTypes = this.parameters[i].collectMissingTypes(missingTypes);
		}
		for (int i = 0, max = this.thrownExceptions.length; i < max; i++) {
			missingTypes = this.thrownExceptions[i].collectMissingTypes(missingTypes);
		}
		for (int i = 0, max = this.typeVariables.length; i < max; i++) {
			TypeVariableBinding variable = this.typeVariables[i];
			missingTypes = variable.superclass().collectMissingTypes(missingTypes);
			ReferenceBinding[] interfaces = variable.superInterfaces();
			for (int j = 0, length = interfaces.length; j < length; j++) {
				missingTypes = interfaces[j].collectMissingTypes(missingTypes);
			}
		}
	}
	return missingTypes;
}

public MethodBinding computeSubstitutedMethod(MethodBinding method, LookupEnvironment env) {
	int length = this.typeVariables.length;
	TypeVariableBinding[] vars = method.typeVariables;
	if (length != vars.length)
		return null;

	// must substitute to detect cases like:
	//   <T1 extends X<T1>> void dup() {}
	//   <T2 extends X<T2>> Object dup() {return null;}
	ParameterizedGenericMethodBinding substitute =
		env.createParameterizedGenericMethod(method, this.typeVariables);
	for (int i = 0; i < length; i++)
		if (!this.typeVariables[i].isInterchangeableWith(vars[i], substitute))
			return null;
	return substitute;
}

/*
 * declaringUniqueKey dot selector genericSignature
 * p.X { <T> void bar(X<T> t) } --> Lp/X;.bar<T:Ljava/lang/Object;>(LX<TT;>;)V
 */
@Override
public char[] computeUniqueKey(boolean isLeaf) {
	// declaring class
	char[] declaringKey = this.declaringClass.computeUniqueKey(false/*not a leaf*/);
	int declaringLength = declaringKey.length;

	// selector
	int selectorLength = this.selector == TypeConstants.INIT ? 0 : this.selector.length;

	// generic signature
	char[] sig = genericSignature();
	boolean isGeneric = sig != null;
	if (!isGeneric) sig = signature();
	int signatureLength = sig.length;

	// thrown exceptions
	int thrownExceptionsLength = this.thrownExceptions.length;
	int thrownExceptionsSignatureLength = 0;
	char[][] thrownExceptionsSignatures = null;
	boolean addThrownExceptions = thrownExceptionsLength > 0 && (!isGeneric || CharOperation.lastIndexOf('^', sig) < 0);
	if (addThrownExceptions) {
		thrownExceptionsSignatures = new char[thrownExceptionsLength][];
		for (int i = 0; i < thrownExceptionsLength; i++) {
			if (this.thrownExceptions[i] != null) {
				thrownExceptionsSignatures[i] = this.thrownExceptions[i].signature();
				thrownExceptionsSignatureLength += thrownExceptionsSignatures[i].length + 1;	// add one char for separator
			}
		}
	}

	char[] uniqueKey = new char[declaringLength + 1 + selectorLength + signatureLength + thrownExceptionsSignatureLength];
	int index = 0;
	System.arraycopy(declaringKey, 0, uniqueKey, index, declaringLength);
	index = declaringLength;
	uniqueKey[index++] = '.';
	System.arraycopy(this.selector, 0, uniqueKey, index, selectorLength);
	index += selectorLength;
	System.arraycopy(sig, 0, uniqueKey, index, signatureLength);
	if (thrownExceptionsSignatureLength > 0) {
		index += signatureLength;
		for (int i = 0; i < thrownExceptionsLength; i++) {
			char[] thrownExceptionSignature = thrownExceptionsSignatures[i];
			if (thrownExceptionSignature != null) {
				uniqueKey[index++] = '|';
				int length = thrownExceptionSignature.length;
				System.arraycopy(thrownExceptionSignature, 0, uniqueKey, index, length);
				index += length;
			}
		}
	}
	return uniqueKey;
}

//{ObjectTeams
/**
 * @return true if method has a marker arg
 */
public boolean hasMarkerArg()
{
	TypeBinding[] params = this.parameters;

	if (   params == null
		|| params.length == 0)
			return false;
	TypeBinding lastParam = params[params.length-1];
	return TSuperHelper.isMarkerInterface(lastParam);
}
// support two views of parameters/returnType: actual enhanced/generalized vs. source-level
protected int switchCount = 0;
protected TypeBinding[] enhancedParameters = null;
protected TypeBinding generalizedReturnType = null; // only set if return and parameters are temporarily retrenched.
/**
 * Revert signature enhancement and return only those parameters defined in the source code.
 * @return a nonnull array of parameter types without the enhancing args
 */
public TypeBinding[] getSourceParameters() {
	TypeBinding[] allParameters = this.parameters;
	if ((this.switchCount == 0) && isCallin()) {
		int numGeneratedArgs = getMethodSignatureEnhancer().ENHANCING_ARG_LEN;
		TypeBinding[] sourceParameters = new TypeBinding[allParameters.length - numGeneratedArgs];
		System.arraycopy(allParameters, numGeneratedArgs,
				         sourceParameters, 0, sourceParameters.length);
		return sourceParameters;
	} else {
		return allParameters;
	}
}
/** How many source-level parameters does this method have? */
public int getSourceParamLength() {
	if ((this.switchCount == 0) && isCallin())
		return this.parameters.length - getMethodSignatureEnhancer().ENHANCING_ARG_LEN;

	return this.parameters.length;
}
public MethodSignatureEnhancer getMethodSignatureEnhancer() {
	WeavingScheme weavingScheme = WeavingScheme.OTRE;
	if (this.declaringClass instanceof BinaryTypeBinding)
		weavingScheme = ((BinaryTypeBinding)this.declaringClass).environment.globalOptions.weavingScheme;
	else if (this.declaringClass instanceof SourceTypeBinding) {
		Scope scope = ((SourceTypeBinding)this.declaringClass).scope;
		if (scope != null)
			weavingScheme = scope.compilerOptions().weavingScheme;
	}
	return MethodSignatureEnhancer.variants[weavingScheme.ordinal()];
}
/**
 * Temporarily set the source parameters as this method's parameters.
 * The same for the return type.
 */
public void switchToSourceParamters() {
	// only store resolved types:
	if (this.declaringClass instanceof BinaryTypeBinding)
		((BinaryTypeBinding)this.declaringClass).resolveTypesFor(this);

	// store:
	this.enhancedParameters = this.parameters;
	this.generalizedReturnType = this.returnType;
	// switch:
	this.parameters = getSourceParameters();
	this.returnType = MethodModel.getReturnType(this);
	// mark:
	this.switchCount++;
}
/**
 * Restore the (enhanced?) paremeters and the generalized return type.
 */
public void resetParameters() {
	if (--this.switchCount == 0) {
		// restore:
		this.parameters = this.enhancedParameters;
		this.returnType = this.generalizedReturnType;
		// clean:
		this.enhancedParameters = null;
		this.generalizedReturnType = null;
	}
}
/**
 * Get type from this method's signature as it shall be used for code generation.
 * The purpose is to weaken any parameter substitutions that have been applied
 * along implicit inheritance.
 * @param pos either a parameter position or -1 designating the return type.
 * @return the 'weakened' type at the specified position.
 */
public TypeBinding getCodeGenType(int pos) {
	MethodBinding tsuperOriginal = (this.otBits & IOTConstants.IsCopyOfParameterized) != 0 ? this.copyInheritanceSrc.original() : null;
	TypeBinding currentType, tsuperType;
	checkTsuper: {
		if (pos == -1) {
			currentType = this.returnType;
			if (tsuperOriginal == null)
				break checkTsuper;
			tsuperType = tsuperOriginal.returnType;
		} else {
			currentType = this.parameters[pos];
			if (tsuperOriginal == null || pos >= tsuperOriginal.parameters.length)
				break checkTsuper;
			tsuperType = tsuperOriginal.parameters[pos];
		}
		if (   tsuperType.isTypeVariable()
			&& (((TypeVariableBinding)tsuperType).declaringElement.kind() & Binding.TYPE) != 0
			&& !currentType.isTypeVariable())
			return tsuperType.erasure();
	}
	if (   currentType instanceof TypeVariableBinding
		&& !(currentType instanceof CaptureBinding)
		&& ((TypeVariableBinding)currentType).declaringElement.kind() == Binding.BINDING
		&& this instanceof ParameterizedGenericMethodBinding)
		return ((ParameterizedGenericMethodBinding)this).reverseSubstitute((TypeVariableBinding) currentType).erasure();
	return currentType.erasure();
}
//MW+SH}

/* Answer the receiver's constant pool name.
*
* <init> for constructors
* <clinit> for clinit methods
* or the source name of the method
*/
public final char[] constantPoolName() {
	return this.selector;
}

/**
 * After method verifier has finished, fill in missing @NonNull specification from the applicable default.
 * @param needToApplyParameterNonNullDefault
 * @param needToApplyReturnNonNullDefault
 */
protected void fillInDefaultNonNullness(AbstractMethodDeclaration sourceMethod, boolean needToApplyReturnNonNullDefault, ParameterNonNullDefaultProvider needToApplyParameterNonNullDefault) {
	if (this.parameterNonNullness == null)
		this.parameterNonNullness = new Boolean[this.parameters.length];
	boolean added = false;
	int length = this.parameterNonNullness.length;
	for (int i = 0; i < length; i++) {
		if(!needToApplyParameterNonNullDefault.hasNonNullDefaultForParam(i)) {
			continue;
		}
		if (this.parameters[i].isBaseType())
			continue;
		if (this.parameterNonNullness[i] == null) {
			added = true;
			this.parameterNonNullness[i] = Boolean.TRUE;
			if (sourceMethod != null) {
				sourceMethod.arguments[i].binding.tagBits |= TagBits.AnnotationNonNull;
			}
		} else if (sourceMethod != null && this.parameterNonNullness[i].booleanValue()) {
			sourceMethod.scope.problemReporter().nullAnnotationIsRedundant(sourceMethod, i);
		}
	}
	if (added)
		this.tagBits |= TagBits.HasParameterAnnotations;
	if(!needToApplyReturnNonNullDefault)
		return;
	if (   this.returnType != null
		&& !this.returnType.isBaseType()
		&& (this.tagBits & TagBits.AnnotationNullMASK) == 0)
	{
		this.tagBits |= TagBits.AnnotationNonNull;
	} else if (sourceMethod != null && (this.tagBits & TagBits.AnnotationNonNull) != 0) {
		sourceMethod.scope.problemReporter().nullAnnotationIsRedundant(sourceMethod, -1/*signifies method return*/);
	}
}

//pre: null annotation analysis is enabled
protected void fillInDefaultNonNullness18(AbstractMethodDeclaration sourceMethod, LookupEnvironment env) {
	MethodBinding original = original();
	if(original == null) {
		return;
	}
	ParameterNonNullDefaultProvider hasNonNullDefaultForParameter = hasNonNullDefaultForParameter(sourceMethod);
	if (hasNonNullDefaultForParameter.hasAnyNonNullDefault()) {
		boolean added = false;
		int length = this.parameters.length;
		for (int i = 0; i < length; i++) {
			if (!hasNonNullDefaultForParameter.hasNonNullDefaultForParam(i))
				continue;
			TypeBinding parameter = this.parameters[i];
			if (!original.parameters[i].acceptsNonNullDefault())
				continue;
			long existing = parameter.tagBits & TagBits.AnnotationNullMASK;
			if (existing == 0L) {
				added = true;
				if (!parameter.isBaseType()) {
					this.parameters[i] = env.createAnnotatedType(parameter, new AnnotationBinding[]{env.getNonNullAnnotation()});
					if (sourceMethod != null)
						sourceMethod.arguments[i].binding.type = this.parameters[i];
				}
			} else if (sourceMethod != null && (parameter.tagBits & TagBits.AnnotationNonNull) != 0
							&& sourceMethod.arguments[i].hasNullTypeAnnotation(AnnotationPosition.MAIN_TYPE)) {
				sourceMethod.scope.problemReporter().nullAnnotationIsRedundant(sourceMethod, i);
			}
		}
		if (added)
			this.tagBits |= TagBits.HasParameterAnnotations;
	}
	if (original.returnType != null && hasNonNullDefaultForReturnType(sourceMethod) && original.returnType.acceptsNonNullDefault()) {
		if ((this.returnType.tagBits & TagBits.AnnotationNullMASK) == 0) {
			this.returnType = env.createAnnotatedType(this.returnType, new AnnotationBinding[]{env.getNonNullAnnotation()});
		} else if (sourceMethod instanceof MethodDeclaration && (this.returnType.tagBits & TagBits.AnnotationNonNull) != 0
						&& ((MethodDeclaration)sourceMethod).hasNullTypeAnnotation(AnnotationPosition.MAIN_TYPE)) {
			sourceMethod.scope.problemReporter().nullAnnotationIsRedundant(sourceMethod, -1/*signifies method return*/);
		}
	}
}

public MethodBinding findOriginalInheritedMethod(MethodBinding inheritedMethod) {
	MethodBinding inheritedOriginal = inheritedMethod.original();
	TypeBinding superType = this.declaringClass.findSuperTypeOriginatingFrom(inheritedOriginal.declaringClass);
	if (superType == null || !(superType instanceof ReferenceBinding)) return null;

	if (TypeBinding.notEquals(inheritedOriginal.declaringClass, superType)) {
		// must find inherited method with the same substituted variables
		MethodBinding[] superMethods = ((ReferenceBinding) superType).getMethods(inheritedOriginal.selector, inheritedOriginal.parameters.length);
		for (int m = 0, l = superMethods.length; m < l; m++)
			if (superMethods[m].original() == inheritedOriginal)
				return superMethods[m];
	}
	return inheritedOriginal;
}

/**
 * <pre>
 *<typeParam1 ... typeParamM>(param1 ... paramN)returnType thrownException1 ... thrownExceptionP
 * T foo(T t) throws X<T>   --->   (TT;)TT;LX<TT;>;
 * void bar(X<T> t)   -->   (LX<TT;>;)V
 * <T> void bar(X<T> t)   -->  <T:Ljava.lang.Object;>(LX<TT;>;)V
 * </pre>
 */
public char[] genericSignature() {
//{ObjectTeams: retrench for completion?:
	return genericSignature(false);
}
public char[] genericSignature(boolean retrenchCallin) {
// SH}
	if ((this.modifiers & ExtraCompilerModifiers.AccGenericSignature) == 0) return null;
	StringBuffer sig = new StringBuffer(10);
	if (this.typeVariables != Binding.NO_TYPE_VARIABLES) {
		sig.append('<');
		for (int i = 0, length = this.typeVariables.length; i < length; i++) {
			sig.append(this.typeVariables[i].genericSignature());
		}
		sig.append('>');
	}
	sig.append('(');
//{ObjectTeams: retrench:
/* orig:
	for (int i = 0, length = this.parameters.length; i < length; i++) {
		sig.append(this.parameters[i].genericTypeSignature());
	}
  :giro */
	TypeBinding[] visibleParameters = retrenchCallin
								? getSourceParameters()
								: this.parameters;
	for (int i = 0, length = visibleParameters.length; i < length; i++) {
		sig.append(visibleParameters[i].genericTypeSignature());
	}
// SH}
	sig.append(')');
	if (this.returnType != null)
		sig.append(this.returnType.genericTypeSignature());

	// only append thrown exceptions if any is generic/parameterized
	boolean needExceptionSignatures = false;
	int length = this.thrownExceptions.length;
	for (int i = 0; i < length; i++) {
		if((this.thrownExceptions[i].modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0) {
			needExceptionSignatures = true;
			break;
		}
	}
	if (needExceptionSignatures) {
		for (int i = 0; i < length; i++) {
			sig.append('^');
			sig.append(this.thrownExceptions[i].genericTypeSignature());
		}
	}
	int sigLength = sig.length();
	char[] genericSignature = new char[sigLength];
	sig.getChars(0, sigLength, genericSignature, 0);
	return genericSignature;
}

//{ObjectTeams: if method signature has to be changed (e.g., conversion to tsuper) reset the computed signature:
public void resetSignature() {
	this.signature = null;
}
// SH}

public final int getAccessFlags() {
//{ObjectTeams: also allow callin flag
/* orig:
	return this.modifiers & (ExtraCompilerModifiers.AccJustFlag | ExtraCompilerModifiers.AccDefaultMethod);
  :giro */
	return this.modifiers & (ExtraCompilerModifiers.AccJustFlag | ExtraCompilerModifiers.AccDefaultMethod | ExtraCompilerModifiers.AccCallin);
// SH}
}

@Override
public AnnotationBinding[] getAnnotations() {
	MethodBinding originalMethod = original();
	return originalMethod.declaringClass.retrieveAnnotations(originalMethod);
}

/**
 * Compute the tagbits for standard annotations. For source types, these could require
 * lazily resolving corresponding annotation nodes, in case of forward references.
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#getAnnotationTagBits()
 */
@Override
public long getAnnotationTagBits() {
	MethodBinding originalMethod = original();
	if ((originalMethod.tagBits & TagBits.AnnotationResolved) == 0 && originalMethod.declaringClass instanceof SourceTypeBinding) {
		ClassScope scope = ((SourceTypeBinding) originalMethod.declaringClass).scope;
		if (scope != null) {
			TypeDeclaration typeDecl = scope.referenceContext;
			AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(originalMethod);
			if (methodDecl != null)
				ASTNode.resolveAnnotations(methodDecl.scope, methodDecl.annotations, originalMethod);
			CompilerOptions options = scope.compilerOptions();
			if (options.isAnnotationBasedNullAnalysisEnabled) {
				long nullDefaultBits = this.defaultNullness;
				if (nullDefaultBits != 0 && this.declaringClass instanceof SourceTypeBinding) {
					Binding target = scope.checkRedundantDefaultNullness(this.defaultNullness, typeDecl.declarationSourceStart);
					if (target != null) {
						methodDecl.scope.problemReporter().nullDefaultAnnotationIsRedundant(methodDecl, methodDecl.annotations, target);
					}
				}
			}
		}
	}
	return originalMethod.tagBits;
}

/**
 * @return the default value for this annotation method or <code>null</code> if there is no default value
 */
public Object getDefaultValue() {
	MethodBinding originalMethod = original();
	if ((originalMethod.tagBits & TagBits.DefaultValueResolved) == 0) {
		//The method has not been resolved nor has its class been resolved.
		//It can only be from a source type within compilation units to process.
		if (originalMethod.declaringClass instanceof SourceTypeBinding) {
			SourceTypeBinding sourceType = (SourceTypeBinding) originalMethod.declaringClass;
			if (sourceType.scope != null) {
				AbstractMethodDeclaration methodDeclaration = originalMethod.sourceMethod();
				if (methodDeclaration != null && methodDeclaration.isAnnotationMethod()) {
					methodDeclaration.resolve(sourceType.scope);
				}
			}
		}
		originalMethod.tagBits |= TagBits.DefaultValueResolved;
	}
	AnnotationHolder holder = originalMethod.declaringClass.retrieveAnnotationHolder(originalMethod, true);
	return holder == null ? null : holder.getDefaultValue();
}

/**
 * @return the annotations for each of the method parameters or <code>null></code>
 * 	if there's no parameter or no annotation at all.
 */
public AnnotationBinding[][] getParameterAnnotations() {
	int length;
	if ((length = this.parameters.length) == 0) {
		return null;
	}
	MethodBinding originalMethod = original();
	AnnotationHolder holder = originalMethod.declaringClass.retrieveAnnotationHolder(originalMethod, true);
	AnnotationBinding[][] allParameterAnnotations = holder == null ? null : holder.getParameterAnnotations();
	if (allParameterAnnotations == null && (this.tagBits & TagBits.HasParameterAnnotations) != 0) {
		allParameterAnnotations = new AnnotationBinding[length][];
		// forward reference to method, where param annotations have not yet been associated to method
		if (this.declaringClass instanceof SourceTypeBinding) {
			SourceTypeBinding sourceType = (SourceTypeBinding) this.declaringClass;
			if (sourceType.scope != null) {
				AbstractMethodDeclaration methodDecl = sourceType.scope.referenceType().declarationOf(originalMethod);
				for (int i = 0; i < length; i++) {
					Argument argument = methodDecl.arguments[i];
					if (argument.annotations != null) {
						ASTNode.resolveAnnotations(methodDecl.scope, argument.annotations, argument.binding);
						allParameterAnnotations[i] = argument.binding.getAnnotations();
					} else {
						allParameterAnnotations[i] = Binding.NO_ANNOTATIONS;
					}
				}
			} else {
				for (int i = 0; i < length; i++) {
					allParameterAnnotations[i] = Binding.NO_ANNOTATIONS;
				}
			}
		} else {
			for (int i = 0; i < length; i++) {
				allParameterAnnotations[i] = Binding.NO_ANNOTATIONS;
			}
		}
		setParameterAnnotations(allParameterAnnotations);
	}
	return allParameterAnnotations;
}

public TypeVariableBinding getTypeVariable(char[] variableName) {
	for (int i = this.typeVariables.length; --i >= 0;)
		if (CharOperation.equals(this.typeVariables[i].sourceName, variableName))
			return this.typeVariables[i];
	return null;
}

public TypeVariableBinding[] getAllTypeVariables(boolean isDiamond) {
	TypeVariableBinding[] allTypeVariables = this.typeVariables;
	if (isDiamond) {
		TypeVariableBinding[] classTypeVariables = this.declaringClass.typeVariables();
		int l1 = allTypeVariables.length;
		int l2 = classTypeVariables.length;
		if (l1 == 0) {
			allTypeVariables = classTypeVariables;
		} else if (l2 != 0) {
			System.arraycopy(allTypeVariables, 0, allTypeVariables=new TypeVariableBinding[l1+l2], 0, l1);
			System.arraycopy(classTypeVariables, 0, allTypeVariables, l1, l2);
		}
	}
	return allTypeVariables;
}

/**
 * Returns true if method got substituted parameter types
 * (see ParameterizedMethodBinding)
 */
public boolean hasSubstitutedParameters() {
	return false;
}

/* Answer true if the return type got substituted.
 */
public boolean hasSubstitutedReturnType() {
	return false;
}

/* Answer true if the receiver is an abstract method
*/
public final boolean isAbstract() {
	return (this.modifiers & ClassFileConstants.AccAbstract) != 0;
}

/* Answer true if the receiver is a bridge method
*/
public final boolean isBridge() {
	return (this.modifiers & ClassFileConstants.AccBridge) != 0;
}

/* Answer true if the receiver is a constructor
*/
public final boolean isConstructor() {
	return this.selector == TypeConstants.INIT;
}

/* Answer true if the receiver is a compact constructor
*/
public final boolean isCompactConstructor() {
	return (this.modifiers &  ExtraCompilerModifiers.AccCompactConstructor) != 0;
}

/* Answer true if the receiver has default visibility
*/
@Override
public final boolean isDefault() {
	return !isPublic() && !isProtected() && !isPrivate();
}

/* Answer true if the receiver is a system generated default abstract method
*/
public final boolean isDefaultAbstract() {
	return (this.modifiers & ExtraCompilerModifiers.AccDefaultAbstract) != 0;
}

/* Answer true if the receiver is a default method (Java 8 feature) */
public boolean isDefaultMethod() {
	return (this.modifiers & ExtraCompilerModifiers.AccDefaultMethod) != 0;
}

/* Answer true if the receiver is a deprecated method
*/
public final boolean isDeprecated() {
	return (this.modifiers & ClassFileConstants.AccDeprecated) != 0;
}

/* Answer true if the receiver is final and cannot be overridden
*/
public final boolean isFinal() {
	return (this.modifiers & ClassFileConstants.AccFinal) != 0;
}

/* Answer true if the receiver is implementing another method
 * in other words, it is overriding and concrete, and overriden method is abstract
 * Only set for source methods
*/
public final boolean isImplementing() {
	return (this.modifiers & ExtraCompilerModifiers.AccImplementing) != 0;
}

/*
 * Answer true if the receiver is a "public static void main(String[])" method
 */
public final boolean isMain() {
	if (this.selector.length == 4 && CharOperation.equals(this.selector, TypeConstants.MAIN)
			&& ((this.modifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccStatic)) != 0)
			&& TypeBinding.VOID == this.returnType
			&& this.parameters.length == 1) {
		TypeBinding paramType = this.parameters[0];
		if (paramType.dimensions() == 1 && paramType.leafComponentType().id == TypeIds.T_JavaLangString) {
			return true;
		}
	}
	return false;
}

/* Answer true if the receiver is a native method
*/
public final boolean isNative() {
	return (this.modifiers & ClassFileConstants.AccNative) != 0;
}

/* Answer true if the receiver is overriding another method
 * Only set for source methods
*/
public final boolean isOverriding() {
	return (this.modifiers & ExtraCompilerModifiers.AccOverriding) != 0;
}
/* Answer true if the receiver has private visibility
*/
@Override
public final boolean isPrivate() {
	return (this.modifiers & ClassFileConstants.AccPrivate) != 0;
}

/* Answer true if the receiver has private visibility or if any of its enclosing types do.
*/
public final boolean isOrEnclosedByPrivateType() {
	if ((this.modifiers & ClassFileConstants.AccPrivate) != 0)
		return true;
	return this.declaringClass != null && this.declaringClass.isOrEnclosedByPrivateType();
}

/* Answer true if the receiver has protected visibility
*/
@Override
public final boolean isProtected() {
	return (this.modifiers & ClassFileConstants.AccProtected) != 0;
}

/* Answer true if the receiver has public visibility
*/
@Override
public final boolean isPublic() {
	return (this.modifiers & ClassFileConstants.AccPublic) != 0;
}

/* Answer true if the receiver is a static method
*/
@Override
public final boolean isStatic() {
	return (this.modifiers & ClassFileConstants.AccStatic) != 0;
}

/* Answer true if all float operations must adher to IEEE 754 float/double rules
*/
public final boolean isStrictfp() {
	return (this.modifiers & ClassFileConstants.AccStrictfp) != 0;
}

/* Answer true if the receiver is a synchronized method
*/
public final boolean isSynchronized() {
	return (this.modifiers & ClassFileConstants.AccSynchronized) != 0;
}

/* Answer true if the receiver has public visibility
*/
public final boolean isSynthetic() {
	return (this.modifiers & ClassFileConstants.AccSynthetic) != 0;
}

/* Answer true if the receiver has private visibility and is used locally
*/
public final boolean isUsed() {
	return (this.modifiers & ExtraCompilerModifiers.AccLocallyUsed) != 0;
}

/* Answer true if the receiver method has varargs
*/
public boolean isVarargs() {
	return (this.modifiers & ClassFileConstants.AccVarargs) != 0;
}
public boolean isParameterizedGeneric() {
	return false;
}
public boolean isPolymorphic() {
	return false;
}
/* Answer true if the receiver's declaring type is deprecated (or any of its enclosing types)
*/
public final boolean isViewedAsDeprecated() {
	return (this.modifiers & (ClassFileConstants.AccDeprecated | ExtraCompilerModifiers.AccDeprecatedImplicitly)) != 0;
}

@Override
public final int kind() {
	return Binding.METHOD;
}
/* Answer true if the receiver is visible to the invocationPackage.
*/

/**
 * Returns the original method (as opposed to parameterized/polymorphic instances)
 */
public MethodBinding original() {
	return this;
}

/**
 * Strips one level of parameterization, so if both class & method are parameterized,
 * leave the class parameters in place.
 */
public MethodBinding shallowOriginal() {
	return original();
}

public MethodBinding genericMethod() {
	return this;
}

@Override
public char[] readableName() /* foo(int, Thread) */ {
	StringBuffer buffer = new StringBuffer(this.parameters.length + 1 * 20);
	if (isConstructor())
		buffer.append(this.declaringClass.sourceName());
//{ObjectTeams: hide creator method:
	else if (CopyInheritance.isCreator(this))
		buffer.append(this.returnType.sourceName());
// SH}
	else
		buffer.append(this.selector);
	buffer.append('(');
	if (this.parameters != Binding.NO_PARAMETERS) {
//{ObjectTeams: hide marker arg and enhancing args:
		int firstParam = 0;
		int lastParam = this.parameters.length-1;
		if (TSuperHelper.isTSuper(this)) {
			lastParam --;
			buffer.insert(0, "tsuper."); //$NON-NLS-1$
		}
		if (isCallin())
			firstParam = getMethodSignatureEnhancer().ENHANCING_ARG_LEN;
/* orig:
		for (int i = 0, length = this.parameters.length; i < length; i++) {
			if (i > 0)
  :giro */
		for (int i = firstParam; i <= lastParam; i++) {
			if (i > firstParam)
// SH}
				buffer.append(", "); //$NON-NLS-1$
			buffer.append(this.parameters[i].sourceName());
		}
	}
	buffer.append(')');
	return buffer.toString().toCharArray();
}
final public AnnotationBinding[] getTypeAnnotations() {
	return this.typeAnnotations;
}

public void setTypeAnnotations(AnnotationBinding[] annotations) {
	this.typeAnnotations = annotations;
}
@Override
public void setAnnotations(AnnotationBinding[] annotations, boolean forceStore) {
	this.declaringClass.storeAnnotations(this, annotations, forceStore);
}
public void setAnnotations(AnnotationBinding[] annotations, AnnotationBinding[][] parameterAnnotations, Object defaultValue, LookupEnvironment optionalEnv) {
	this.declaringClass.storeAnnotationHolder(this,  AnnotationHolder.storeAnnotations(annotations, parameterAnnotations, defaultValue, optionalEnv));
}
public void setDefaultValue(Object defaultValue) {
	MethodBinding originalMethod = original();
	originalMethod.tagBits |= TagBits.DefaultValueResolved;

	AnnotationHolder holder = this.declaringClass.retrieveAnnotationHolder(this, false);
	if (holder == null)
		setAnnotations(null, null, defaultValue, null);
	else
		setAnnotations(holder.getAnnotations(), holder.getParameterAnnotations(), defaultValue, null);
}
public void setParameterAnnotations(AnnotationBinding[][] parameterAnnotations) {
	AnnotationHolder holder = this.declaringClass.retrieveAnnotationHolder(this, false);
	if (holder == null)
		setAnnotations(null, parameterAnnotations, null, null);
	else
		setAnnotations(holder.getAnnotations(), parameterAnnotations, holder.getDefaultValue(), null);
}
protected final void setSelector(char[] selector) {
	this.selector = selector;
	this.signature = null;
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#shortReadableName()
 */
@Override
public char[] shortReadableName() {
	StringBuffer buffer = new StringBuffer(this.parameters.length + 1 * 20);
	if (isConstructor())
		buffer.append(this.declaringClass.shortReadableName());
//{ObjectTeams: hide creator method:
	else if (CopyInheritance.isCreator(this))
		buffer.append(this.returnType.sourceName());
// SH}
	else
		buffer.append(this.selector);
	buffer.append('(');
	if (this.parameters != Binding.NO_PARAMETERS) {
//{ObjectTeams: hide marker arg and enhancing args:
		int firstParam = 0;
		int lastParam = this.parameters.length-1;
		if (TSuperHelper.isTSuper(this)) {
			lastParam --;
			buffer.insert(0, "tsuper."); //$NON-NLS-1$
		}
		if (isCallin())
			firstParam = getMethodSignatureEnhancer().ENHANCING_ARG_LEN;
/* orig:
		for (int i = 0, length = this.parameters.length; i < length; i++) {
			if (i > 0)
  :giro */
		for (int i = firstParam; i <= lastParam; i++) {
			if (i > firstParam)
// SH}
				buffer.append(", "); //$NON-NLS-1$
//{ObjectTeams; more robust, was sourceName(), which doesn't work for ProblemReferenceBinding
			buffer.append(this.parameters[i].shortReadableName());
// SH}
		}
	}
	buffer.append(')');
	int nameLength = buffer.length();
	char[] shortReadableName = new char[nameLength];
	buffer.getChars(0, nameLength, shortReadableName, 0);
	return shortReadableName;
}

//{ObjectTeams
public char[] selector()
{
    if (CharOperation.compareWith(this.selector, IOTConstants.CREATOR_PREFIX_NAME) == 0)
    {	if(this.returnType==null)
    		return this.selector;
    	else
			return this.returnType.shortReadableName();
    }
    else
    {
        return this.selector;
    }
}
// SH}

//{ObjectTeams:
/** How many synthetic arguments does this method expect, counting synth args, but no enhancement. */
public int syntheticArgsCount() {
	if (this.needsSyntheticEnclosingTeamInstance())
		return 1 + this.declaringClass.syntheticEnclosingInstanceTypes().length;
	return 0;
}
// SH}

/* Answer the receiver's signature.
*
* NOTE: This method should only be used during/after code gen.
* The signature is cached so if the signature of the return type or any parameter
* type changes, the cached state is invalid.
*/
public final char[] signature() /* (ILjava/lang/Thread;)Ljava/lang/Object; */ {
//{ObjectTeams: retrench callin for completion?:
	return signature(false); // normal case: direct signature;
}
public final char[] signature(boolean retrenchRoleMethod) {
  if (!retrenchRoleMethod)
// SH}
	if (this.signature != null)
		return this.signature;

	StringBuffer buffer = new StringBuffer(this.parameters.length + 1 * 20);
	buffer.append('(');

//{ObjectTeams: respect callin-enhancement:
/* orig:
	TypeBinding[] targetParameters = this.parameters;
  :giro */
	TypeBinding[] targetParameters = retrenchRoleMethod ? getSourceParameters(): this.parameters;
// SH}
	boolean isConstructor = isConstructor();
	if (isConstructor && this.declaringClass.isEnum()) { // insert String name,int ordinal
		buffer.append(ConstantPool.JavaLangStringSignature);
		buffer.append(TypeBinding.INT.signature());
	}
	boolean needSynthetics = isConstructor && this.declaringClass.isNestedType();
//{ObjectTeams: also need synthetics for static role methods:
	if (this.needsSyntheticEnclosingTeamInstance() && !retrenchRoleMethod) {
		needSynthetics = true;
		buffer.append(TypeBinding.INT.signature()); // dummy arg instead of this=aload0
	}
// SH}
	if (needSynthetics) {
		// take into account the synthetic argument type signatures as well
		ReferenceBinding[] syntheticArgumentTypes = this.declaringClass.syntheticEnclosingInstanceTypes();
//{ObjectTeams: manual weakening of synthetic enclosing team arg:
		if (this.copyInheritanceSrc != null && this.isStatic()) {
			syntheticArgumentTypes = this.copyInheritanceSrc.declaringClass.syntheticEnclosingInstanceTypes();
			if (syntheticArgumentTypes != null && syntheticArgumentTypes.length > 0 && syntheticArgumentTypes[0].isRole())
				syntheticArgumentTypes[0] = syntheticArgumentTypes[0].getRealType();
		}
// SH}
		if (syntheticArgumentTypes != null) {
			for (int i = 0, count = syntheticArgumentTypes.length; i < count; i++) {
				buffer.append(syntheticArgumentTypes[i].signature());
			}
		}

		if (this instanceof SyntheticMethodBinding) {
			targetParameters = ((SyntheticMethodBinding)this).targetMethod.parameters;
		}
	}
//{ObjectTeams: similar for value parameters:
	if (isConstructor) {
		VariableBinding[] syntheticArguments = this.declaringClass.valueParamSynthArgs();
		if (syntheticArguments != Binding.NO_SYNTH_ARGUMENTS)
			for (int i = 0, count = syntheticArguments.length; i < count; i++)
				buffer.append(syntheticArguments[i].type.signature());
	}
	if (   this.copyInheritanceSrc != null
		&& !(this.copyInheritanceSrc.declaringClass instanceof LocalTypeBinding)) // copied from binary?
		needSynthetics = false; // don't add outer locals, are contained in targetParameters
// SH}

	if (targetParameters != Binding.NO_PARAMETERS) {
		for (int i = 0; i < targetParameters.length; i++) {
			buffer.append(targetParameters[i].signature());
		}
	}
	if (needSynthetics) {
		SyntheticArgumentBinding[] syntheticOuterArguments = this.declaringClass.syntheticOuterLocalVariables();
		int count = syntheticOuterArguments == null ? 0 : syntheticOuterArguments.length;
		for (int i = 0; i < count; i++) {
			buffer.append(syntheticOuterArguments[i].type.signature());
		}
		// move the extra padding arguments of the synthetic constructor invocation to the end
		for (int i = targetParameters.length, extraLength = this.parameters.length; i < extraLength; i++) {
			buffer.append(this.parameters[i].signature());
		}
	}
	buffer.append(')');
	if (this.returnType != null)
		buffer.append(this.returnType.signature());
//{ObjectTeams: don't store retrenched version:
	if (retrenchRoleMethod)
		return buffer.toString().toCharArray();
// SH}
	int nameLength = buffer.length();
	this.signature = new char[nameLength];
	buffer.getChars(0, nameLength, this.signature, 0);

	return this.signature;
}
/*
 * This method is used to record references to nested types inside the method signature.
 * This is the one that must be used during code generation.
 *
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=171184
 */
public char[] signature(ClassFile classFile) {
//Note(SH): this method is not used by completion et al, therefor we don't need
//          the retrenchRoleMethod arg here.
	if (this.signature != null) {
		if ((this.tagBits & TagBits.ContainsNestedTypeReferences) != 0) {
			// we need to record inner classes references
			boolean isConstructor = isConstructor();
			TypeBinding[] targetParameters = this.parameters;
			boolean needSynthetics = isConstructor && this.declaringClass.isNestedType();
//{ObjectTeams: also need synthetics for static role methods:
			if (this.needsSyntheticEnclosingTeamInstance())
				needSynthetics = true;
// SH}
			if (needSynthetics) {
				// take into account the synthetic argument type signatures as well
				ReferenceBinding[] syntheticArgumentTypes = this.declaringClass.syntheticEnclosingInstanceTypes();
				if (syntheticArgumentTypes != null) {
					for (int i = 0, count = syntheticArgumentTypes.length; i < count; i++) {
						ReferenceBinding syntheticArgumentType = syntheticArgumentTypes[i];
						if ((syntheticArgumentType.tagBits & TagBits.ContainsNestedTypeReferences) != 0) {
							Util.recordNestedType(classFile, syntheticArgumentType);
						}
					}
				}
				if (this instanceof SyntheticMethodBinding) {
					targetParameters = ((SyntheticMethodBinding)this).targetMethod.parameters;
				}
			}

			if (targetParameters != Binding.NO_PARAMETERS) {
				for (int i = 0, max = targetParameters.length; i < max; i++) {
					TypeBinding targetParameter = targetParameters[i];
					TypeBinding leafTargetParameterType = targetParameter.leafComponentType();
					if ((leafTargetParameterType.tagBits & TagBits.ContainsNestedTypeReferences) != 0) {
						Util.recordNestedType(classFile, leafTargetParameterType);
					}
				}
			}
			if (needSynthetics) {
				// move the extra padding arguments of the synthetic constructor invocation to the end
				for (int i = targetParameters.length, extraLength = this.parameters.length; i < extraLength; i++) {
					TypeBinding parameter = this.parameters[i];
					TypeBinding leafParameterType = parameter.leafComponentType();
					if ((leafParameterType.tagBits & TagBits.ContainsNestedTypeReferences) != 0) {
						Util.recordNestedType(classFile, leafParameterType);
					}
				}
			}
			if (this.returnType != null) {
				TypeBinding ret = this.returnType.leafComponentType();
				if ((ret.tagBits & TagBits.ContainsNestedTypeReferences) != 0) {
					Util.recordNestedType(classFile, ret);
				}
			}
		}
		return this.signature;
	}

	StringBuffer buffer = new StringBuffer((this.parameters.length + 1) * 20);
	buffer.append('(');

	TypeBinding[] targetParameters = this.parameters;
	boolean isConstructor = isConstructor();
	if (isConstructor && this.declaringClass.isEnum()) { // insert String name,int ordinal
		buffer.append(ConstantPool.JavaLangStringSignature);
		buffer.append(TypeBinding.INT.signature());
	}
	boolean needSynthetics = isConstructor && this.declaringClass.isNestedType();
//{ObjectTeams: also need synthetics for static role methods:
	if (this.needsSyntheticEnclosingTeamInstance()) {
		needSynthetics = true;
		buffer.append(TypeBinding.INT.signature()); // dummy arg instead of this=aload0
	}
// SH}
	if (needSynthetics) {
		// take into account the synthetic argument type signatures as well
		ReferenceBinding[] syntheticArgumentTypes = this.declaringClass.syntheticEnclosingInstanceTypes();
//{ObjectTeams: manual weakening of synthetic enclosing team arg (last segment only):
		if (this.copyInheritanceSrc != null && this.isStatic()) {
			syntheticArgumentTypes = this.copyInheritanceSrc.declaringClass.syntheticEnclosingInstanceTypes();
			if (syntheticArgumentTypes != null && syntheticArgumentTypes.length > 0 && syntheticArgumentTypes[0].isRole())
				syntheticArgumentTypes[0] = syntheticArgumentTypes[0].getRealType();
		}
// SH}
		if (syntheticArgumentTypes != null) {
			for (int i = 0, count = syntheticArgumentTypes.length; i < count; i++) {
				ReferenceBinding syntheticArgumentType = syntheticArgumentTypes[i];
				if ((syntheticArgumentType.tagBits & TagBits.ContainsNestedTypeReferences) != 0) {
					this.tagBits |= TagBits.ContainsNestedTypeReferences;
					Util.recordNestedType(classFile, syntheticArgumentType);
				}
				buffer.append(syntheticArgumentType.signature());
			}
		}

		if (this instanceof SyntheticMethodBinding) {
			targetParameters = ((SyntheticMethodBinding)this).targetMethod.parameters;
		}
	}
//{ObjectTeams: similar for value parameters:
	if (isConstructor) {
		VariableBinding[] syntheticArguments = this.declaringClass.valueParamSynthArgs();
		if (syntheticArguments != Binding.NO_SYNTH_ARGUMENTS)
			for (int i = 0, count = syntheticArguments.length; i < count; i++)
				buffer.append(syntheticArguments[i].type.signature());
	}
	if (   this.copyInheritanceSrc != null
		&& !(this.copyInheritanceSrc.declaringClass instanceof LocalTypeBinding)) // copied from binary?
		needSynthetics = false; // don't add outer locals, are contained in targetParameters
// SH}

	if (targetParameters != Binding.NO_PARAMETERS) {
		for (int i = 0, max = targetParameters.length; i < max; i++) {
			TypeBinding targetParameter = targetParameters[i];
			TypeBinding leafTargetParameterType = targetParameter.leafComponentType();
			if ((leafTargetParameterType.tagBits & TagBits.ContainsNestedTypeReferences) != 0) {
				this.tagBits |= TagBits.ContainsNestedTypeReferences;
				Util.recordNestedType(classFile, leafTargetParameterType);
			}
//{ObjectTeams: 'weaken' to that erasure that was used in the tsuper version:
			targetParameter = getCodeGenType(i);
//SH}
			buffer.append(targetParameter.signature());
		}
	}
	if (needSynthetics) {
		SyntheticArgumentBinding[] syntheticOuterArguments = this.declaringClass.syntheticOuterLocalVariables();
		int count = syntheticOuterArguments == null ? 0 : syntheticOuterArguments.length;
		for (int i = 0; i < count; i++) {
			buffer.append(syntheticOuterArguments[i].type.signature());
		}
		// move the extra padding arguments of the synthetic constructor invocation to the end
		for (int i = targetParameters.length, extraLength = this.parameters.length; i < extraLength; i++) {
			TypeBinding parameter = this.parameters[i];
			TypeBinding leafParameterType = parameter.leafComponentType();
			if ((leafParameterType.tagBits & TagBits.ContainsNestedTypeReferences) != 0) {
				this.tagBits |= TagBits.ContainsNestedTypeReferences;
				Util.recordNestedType(classFile, leafParameterType);
			}
			buffer.append(parameter.signature());
		}
	}
	buffer.append(')');
	if (this.returnType != null) {
		TypeBinding ret = this.returnType.leafComponentType();
		if ((ret.tagBits & TagBits.ContainsNestedTypeReferences) != 0) {
			this.tagBits |= TagBits.ContainsNestedTypeReferences;
			Util.recordNestedType(classFile, ret);
		}
//{ObjectTeams: 'weaken' to that erasure that was used in the tsuper version:
/* orig:
		buffer.append(this.returnType.signature());
  :giro */
		buffer.append(getCodeGenType(-1).signature());
// SH}
	}
	int nameLength = buffer.length();
	this.signature = new char[nameLength];
	buffer.getChars(0, nameLength, this.signature, 0);

	return this.signature;
}
public final int sourceEnd() {
	AbstractMethodDeclaration method = sourceMethod();
	if (method == null) {
		if (this.declaringClass instanceof SourceTypeBinding)
			return ((SourceTypeBinding) this.declaringClass).sourceEnd();
		return 0;
	}
	return method.sourceEnd;
}
public AbstractMethodDeclaration sourceMethod() {
	if (isSynthetic()) {
		return null;
	}
	SourceTypeBinding sourceType;
	try {
		sourceType = (SourceTypeBinding) this.declaringClass;
	} catch (ClassCastException e) {
		return null;
	}

	AbstractMethodDeclaration[] methods = sourceType.scope != null ? sourceType.scope.referenceContext.methods : null;
	if (methods != null) {
		for (int i = methods.length; --i >= 0;)
			if (this == methods[i].binding)
				return methods[i];
	}
	return null;
}
public LambdaExpression sourceLambda() {
	return null;
}
public RecordComponent sourceRecordComponent() {
	return null;
}
public final int sourceStart() {
	AbstractMethodDeclaration method = sourceMethod();
	if (method == null) {
		if (this.declaringClass instanceof SourceTypeBinding)
			return ((SourceTypeBinding) this.declaringClass).sourceStart();
		return 0;
	}
	return method.sourceStart;
}

/**
 * Returns the method to use during tiebreak (usually the method itself).
 * For generic method invocations, tiebreak needs to use generic method with erasure substitutes.
 */
public MethodBinding tiebreakMethod() {
	return this;
}
//{ObjectTeams: for printing method mappings allow to suppress the modifiers:
@Override
public String toString() {
	return toString(true);
}
public String toString(boolean showModifiers) {
// SH}
	StringBuffer output = new StringBuffer(10);
	if ((this.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
		output.append("[unresolved] "); //$NON-NLS-1$
	}
//{ObjectTeams: print only conditionally:
  if (showModifiers)
// SH}
	ASTNode.printModifiers(this.modifiers, output);
	output.append(this.returnType != null ? this.returnType.debugName() : "<no type>"); //$NON-NLS-1$
	output.append(" "); //$NON-NLS-1$
	output.append(this.selector != null ? new String(this.selector) : "<no selector>"); //$NON-NLS-1$
	output.append("("); //$NON-NLS-1$
	if (this.parameters != null) {
		if (this.parameters != Binding.NO_PARAMETERS) {
			for (int i = 0, length = this.parameters.length; i < length; i++) {
				if (i  > 0)
					output.append(", "); //$NON-NLS-1$
				output.append(this.parameters[i] != null ? this.parameters[i].debugName() : "<no argument type>"); //$NON-NLS-1$
			}
		}
	} else {
		output.append("<no argument types>"); //$NON-NLS-1$
	}
	output.append(") "); //$NON-NLS-1$

	if (this.thrownExceptions != null) {
		if (this.thrownExceptions != Binding.NO_EXCEPTIONS) {
			output.append("throws "); //$NON-NLS-1$
			for (int i = 0, length = this.thrownExceptions.length; i < length; i++) {
				if (i  > 0)
					output.append(", "); //$NON-NLS-1$
				output.append((this.thrownExceptions[i] != null) ? this.thrownExceptions[i].debugName() : "<no exception type>"); //$NON-NLS-1$
			}
		}
	} else {
		output.append("<no exception types>"); //$NON-NLS-1$
	}
	return output.toString();
}
public TypeVariableBinding[] typeVariables() {
	return this.typeVariables;
}
//pre: null annotation analysis is enabled
public boolean hasNonNullDefaultForReturnType(AbstractMethodDeclaration srcMethod) {
	return hasNonNullDefaultFor(Binding.DefaultLocationReturnType, srcMethod, srcMethod == null ? -1 : srcMethod.declarationSourceStart);
}

static int getNonNullByDefaultValue(AnnotationBinding annotation) {
	ElementValuePair[] elementValuePairs = annotation.getElementValuePairs();
	if (elementValuePairs == null || elementValuePairs.length == 0 ) {
		// no argument: apply default default
		ReferenceBinding annotationType = annotation.getAnnotationType();
		if (annotationType == null) return 0;
		MethodBinding[] annotationMethods = annotationType.methods();
		if (annotationMethods != null && annotationMethods.length == 1) {
			Object value = annotationMethods[0].getDefaultValue();
			return Annotation.nullLocationBitsFromAnnotationValue(value);
		}
		return DefaultLocationsForTrueValue; // custom unconfigurable NNBD
	} else if (elementValuePairs.length > 0) {
		// evaluate the contained EnumConstantSignatures:
		int nullness = 0;
		for (int i = 0; i < elementValuePairs.length; i++)
			nullness |= Annotation.nullLocationBitsFromAnnotationValue(elementValuePairs[i].getValue());
		return nullness;
	} else {
		// empty argument: cancel all defaults from enclosing scopes
		return NULL_UNSPECIFIED_BY_DEFAULT;
	}
}


//pre: null annotation analysis is enabled
public ParameterNonNullDefaultProvider hasNonNullDefaultForParameter(AbstractMethodDeclaration srcMethod) {
	int len = this.parameters.length;
	boolean[] result = new boolean[len];
	boolean trueFound = false;
	boolean falseFound = false;
	for (int i = 0; i < len; i++) {
		int start = srcMethod == null || srcMethod.arguments == null || srcMethod.arguments.length == 0 ? -1
				: srcMethod.arguments[i].declarationSourceStart;
		int nonNullByDefaultValue = srcMethod != null && start >= 0
				? srcMethod.scope.localNonNullByDefaultValue(start)
				: 0;
		if (nonNullByDefaultValue == 0) {
			AnnotationBinding[][] parameterAnnotations = getParameterAnnotations();
			if (parameterAnnotations != null) {
				AnnotationBinding[] annotationBindings = parameterAnnotations[i];
				for (AnnotationBinding annotationBinding : annotationBindings) {
					ReferenceBinding annotationType = annotationBinding.getAnnotationType();
					if (!annotationType.hasNullBit(TypeIds.BitNonNullByDefaultAnnotation)) {
						continue;
					}
					nonNullByDefaultValue |= getNonNullByDefaultValue(annotationBinding);
				}
			}
		}
		boolean b;
		if (nonNullByDefaultValue != 0) {
			// parameter specific NNBD found
			b = (nonNullByDefaultValue & Binding.DefaultLocationParameter) != 0;
		} else {
			b = hasNonNullDefaultFor(Binding.DefaultLocationParameter, srcMethod, start);
		}
		if (b) {
			trueFound = true;
		} else {
			falseFound = true;
		}
		result[i] = b;
	}
		if (trueFound && falseFound) {
			return new ParameterNonNullDefaultProvider.MixedProvider(result);
		}
		return trueFound ? ParameterNonNullDefaultProvider.TRUE_PROVIDER : ParameterNonNullDefaultProvider.FALSE_PROVIDER;
	}
//pre: null annotation analysis is enabled
private boolean hasNonNullDefaultFor(int location, AbstractMethodDeclaration srcMethod, int start) {
	if ((this.modifiers & ExtraCompilerModifiers.AccIsDefaultConstructor) != 0)
		return false;
	if (this.defaultNullness != 0)
		return (this.defaultNullness & location) != 0;
	return this.declaringClass.hasNonNullDefaultFor(location, start);
}

public boolean redeclaresPublicObjectMethod(Scope scope) {
	ReferenceBinding javaLangObject = scope.getJavaLangObject();
	MethodBinding [] methods = javaLangObject.getMethods(this.selector);
	for (int i = 0, length = methods == null ? 0 : methods.length; i < length; i++) {
		final MethodBinding method = methods[i];
		if (!method.isPublic() || method.isStatic() || method.parameters.length != this.parameters.length)
			continue;
		if (MethodVerifier.doesMethodOverride(this, method, scope.environment()))
			return true;
	}
	return false;
}
public boolean isVoidMethod() {
	return this.returnType == TypeBinding.VOID;
}
public boolean doesParameterLengthMatch(int suggestedParameterLength) {
	int len = this.parameters.length;
	return len <= suggestedParameterLength || (isVarargs() && len == suggestedParameterLength + 1);
}
public void updateTypeVariableBinding(TypeVariableBinding previousBinding, TypeVariableBinding updatedBinding) {
	TypeVariableBinding[] bindings = this.typeVariables;
	if (bindings != null) {
		for (int i = 0; i < bindings.length; i++) {
			if (bindings[i] == previousBinding) { //$IDENTITY-COMPARISON$
				bindings[i] = updatedBinding;
			}
		}
	}
}
}
