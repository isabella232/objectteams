/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *     Stephan Herrmann - Contributions for
 *     						bug 236385 - [compiler] Warn for potential programming problem if an object is created but not used
 *     						bug 319201 - [null] no warning when unboxing SingleNameReference causes NPE
 *     						bug 349326 - [1.7] new warning for missing try-with-resources
 * 							bug 186342 - [compiler][null] Using annotations for null checking
 *							bug 358903 - Filter practically unimportant resource leak warnings
 *							bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
 *							bug 370639 - [compiler][resource] restore the default for resource leak warnings
 *							bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *							bug 388996 - [compiler][resource] Incorrect 'potential resource leak'
 *							bug 403147 - [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
 *     Jesper S Moller <jesper@selskabet.org> - Contributions for
 *							bug 378674 - "The method can be declared as static" is wrong
 *     Till Brychcy - Contributions for
 *     						bug 413460 - NonNullByDefault is not inherited to Constructors when accessed via Class File
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.StateMemento;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.AnchorMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * OTDT changes:
 *
 * What: wrap resolved type if role, may need to instantiate parameters of constructor call.
 * Why:  this expression can be seen as a variant of MessageSend
 *       (invokes the constructor and returns a value).
 *
 * What: replace "new Role()" statements with a call to the creation method.
 * Why:  do it during resolve, because only now we will find the role if it is in a role file.
 *
 * What: record need to check duplicate roles
 *       if this expression creates a role using the lifting constructor
 * See:  Class comment in MethodBinding
 *
 * @version $Id: AllocationExpression.java 23405 2010-02-03 17:02:18Z stephan $
 */
public class AllocationExpression extends Expression implements InvocationSite {

	public TypeReference type;
	public Expression[] arguments;
	public MethodBinding binding;							// exact binding resulting from lookup
	MethodBinding syntheticAccessor;						// synthetic accessor for inner-emulation
	public TypeReference[] typeArguments;
	public TypeBinding[] genericTypeArguments;
	public FieldDeclaration enumConstant; // for enum constant initializations
	protected TypeBinding typeExpected;	  // for <> inference
	public boolean inferredReturnType;

	public FakedTrackingVariable closeTracker;	// when allocation a Closeable store a pre-liminary tracking variable here

//{ObjectTeams: alternate AST in case the creation needs to be redirected through a creator call:
	private MessageSend roleCreatorCall = null;
	private NameReference valueParam; 						// if allocation type has value parameter: synthesized argument for ctor call
// SH}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
//{ObjectTeams: redirect?
	if (this.roleCreatorCall != null)
		return this.roleCreatorCall.analyseCode(currentScope, flowContext, flowInfo);
// SH}
	// check captured variables are initialized in current context (26134)
	checkCapturedLocalInitializationIfNecessary((ReferenceBinding)this.binding.declaringClass.erasure(), currentScope, flowInfo);

	// process arguments
	if (this.arguments != null) {
		boolean analyseResources = currentScope.compilerOptions().analyseResourceLeaks;
		boolean hasResourceWrapperType = analyseResources 
				&& this.resolvedType instanceof ReferenceBinding 
				&& ((ReferenceBinding)this.resolvedType).hasTypeBit(TypeIds.BitWrapperCloseable);
		for (int i = 0, count = this.arguments.length; i < count; i++) {
			flowInfo =
				this.arguments[i]
					.analyseCode(currentScope, flowContext, flowInfo)
					.unconditionalInits();
			// if argument is an AutoCloseable insert info that it *may* be closed (by the target method, i.e.)
			if (analyseResources && !hasResourceWrapperType) { // allocation of wrapped closeables is analyzed specially
				flowInfo = FakedTrackingVariable.markPassedToOutside(currentScope, this.arguments[i], flowInfo, flowContext, false);
			}
			this.arguments[i].checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
		}
		analyseArguments(currentScope, flowContext, flowInfo, this.binding, this.arguments);
	}

	// record some dependency information for exception types
	ReferenceBinding[] thrownExceptions;
	if (((thrownExceptions = this.binding.thrownExceptions).length) != 0) {
		if ((this.bits & ASTNode.Unchecked) != 0 && this.genericTypeArguments == null) {
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=277643, align with javac on JLS 15.12.2.6
			thrownExceptions = currentScope.environment().convertToRawTypes(this.binding.thrownExceptions, true, true);
		}		
		// check exception handling
		flowContext.checkExceptionHandlers(
			thrownExceptions,
			this,
			flowInfo.unconditionalCopy(),
			currentScope);
	}

	// after having analysed exceptions above start tracking newly allocated resource:
	if (currentScope.compilerOptions().analyseResourceLeaks && FakedTrackingVariable.isAnyCloseable(this.resolvedType))
		FakedTrackingVariable.analyseCloseableAllocation(currentScope, flowInfo, this);

	if (this.binding.declaringClass.isMemberType() && !this.binding.declaringClass.isStatic()) {
		// allocating a non-static member type without an enclosing instance of parent type
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
		currentScope.resetDeclaringClassMethodStaticFlag(this.binding.declaringClass.enclosingType());
		// Reviewed for https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674 :
		// The corresponding problem (when called from static) is not produced until during code generation
	}
	manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
	manageSyntheticAccessIfNecessary(currentScope, flowInfo);

	// account for possible exceptions thrown by the constructor
	flowContext.recordAbruptExit(); // TODO whitelist of ctors that cannot throw any exc.??

	return flowInfo;
}

public void checkCapturedLocalInitializationIfNecessary(ReferenceBinding checkedType, BlockScope currentScope, FlowInfo flowInfo) {
	if (((checkedType.tagBits & ( TagBits.AnonymousTypeMask|TagBits.LocalTypeMask)) == TagBits.LocalTypeMask)
			&& !currentScope.isDefinedInType(checkedType)) { // only check external allocations
		NestedTypeBinding nestedType = (NestedTypeBinding) checkedType;
		SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticOuterLocalVariables();
		if (syntheticArguments != null)
			for (int i = 0, count = syntheticArguments.length; i < count; i++){
				SyntheticArgumentBinding syntheticArgument = syntheticArguments[i];
				LocalVariableBinding targetLocal;
				if ((targetLocal = syntheticArgument.actualOuterLocalVariable) == null) continue;
				if (targetLocal.declaration != null && !flowInfo.isDefinitelyAssigned(targetLocal)){
					currentScope.problemReporter().uninitializedLocalVariable(targetLocal, this);
				}
			}
	}
}

public Expression enclosingInstance() {
	return null;
}

public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	if (!valueRequired)
		currentScope.problemReporter().unusedObjectAllocation(this);
	
//{ObjectTeams: redirect?
	if (this.roleCreatorCall != null) {
		this.roleCreatorCall.generateCode(currentScope, codeStream, valueRequired);
		return;
	}
// SH}
	int pc = codeStream.position;
	MethodBinding codegenBinding = this.binding.original();
	ReferenceBinding allocatedType = codegenBinding.declaringClass;

	codeStream.new_(allocatedType);
	boolean isUnboxing = (this.implicitConversion & TypeIds.UNBOXING) != 0;
	if (valueRequired || isUnboxing) {
		codeStream.dup();
	}
	// better highlight for allocation: display the type individually
	if (this.type != null) { // null for enum constant body
		codeStream.recordPositionsFrom(pc, this.type.sourceStart);
	} else {
		// push enum constant name and ordinal
		codeStream.ldc(String.valueOf(this.enumConstant.name));
		codeStream.generateInlinedValue(this.enumConstant.binding.id);
	}

	// handling innerclass instance allocation - enclosing instance arguments
	if (allocatedType.isNestedType()) {
		codeStream.generateSyntheticEnclosingInstanceValues(
			currentScope,
			allocatedType,
			enclosingInstance(),
			this);
	}
//{ObjectTeams: pass value parameter:
	if (this.valueParam != null)
		this.valueParam.generateCode(currentScope, codeStream, true);
// SH}
	// generate the arguments for constructor
	generateArguments(this.binding, this.arguments, currentScope, codeStream);
	// handling innerclass instance allocation - outer local arguments
	if (allocatedType.isNestedType()) {
		codeStream.generateSyntheticOuterArgumentValues(
			currentScope,
			allocatedType,
			this);
	}
	// invoke constructor
	if (this.syntheticAccessor == null) {
		codeStream.invoke(Opcodes.OPC_invokespecial, codegenBinding, null /* default declaringClass */);
	} else {
		// synthetic accessor got some extra arguments appended to its signature, which need values
		for (int i = 0,
			max = this.syntheticAccessor.parameters.length - codegenBinding.parameters.length;
			i < max;
			i++) {
			codeStream.aconst_null();
		}
		codeStream.invoke(Opcodes.OPC_invokespecial, this.syntheticAccessor, null /* default declaringClass */);
	}
	if (valueRequired) {
		codeStream.generateImplicitConversion(this.implicitConversion);
	} else if (isUnboxing) {
		// conversion only generated if unboxing
		codeStream.generateImplicitConversion(this.implicitConversion);
		switch (postConversionType(currentScope).id) {
			case T_long :
			case T_double :
				codeStream.pop2();
				break;
			default :
				codeStream.pop();
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
 */
public TypeBinding[] genericTypeArguments() {
	return this.genericTypeArguments;
}

public boolean isSuperAccess() {
//{ObjectTeams: within a creation method fake visibility:
	if (isGenerated())
		return true;
// SH}
	return false;
}

public boolean isTypeAccess() {
	return true;
}

/* Inner emulation consists in either recording a dependency
 * link only, or performing one level of propagation.
 *
 * Dependency mechanism is used whenever dealing with source target
 * types, since by the time we reach them, we might not yet know their
 * exact need.
 */
public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) != 0) return;
	ReferenceBinding allocatedTypeErasure = (ReferenceBinding) this.binding.declaringClass.erasure();

	// perform some emulation work in case there is some and we are inside a local type only
	if (allocatedTypeErasure.isNestedType()
		&& currentScope.enclosingSourceType().isLocalType()) {

		if (allocatedTypeErasure.isLocalType()) {
			((LocalTypeBinding) allocatedTypeErasure).addInnerEmulationDependent(currentScope, false);
			// request cascade of accesses
		} else {
			// locally propagate, since we already now the desired shape for sure
			currentScope.propagateInnerEmulation(allocatedTypeErasure, false);
			// request cascade of accesses
		}
	}
}

public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) != 0) return;
	// if constructor from parameterized type got found, use the original constructor at codegen time
	MethodBinding codegenBinding = this.binding.original();
//{ObjectTeams: baseclass decapsulation:
	if (   codegenBinding.isPrivate()
		&& this.type != null
		&& this.type.getBaseclassDecapsulation().isAllowed())
	{
			return; // avoid tweaking below, which might result in CCE if declaringClass is binary.
	}
// SH}

	ReferenceBinding declaringClass;
	if (codegenBinding.isPrivate() && currentScope.enclosingSourceType() != (declaringClass = codegenBinding.declaringClass)) {

		// from 1.4 on, local type constructor can lose their private flag to ease emulation
		if ((declaringClass.tagBits & TagBits.IsLocalType) != 0 && currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4) {
			// constructor will not be dumped as private, no emulation required thus
			codegenBinding.tagBits |= TagBits.ClearPrivateModifier;
		} else {
			this.syntheticAccessor = ((SourceTypeBinding) declaringClass).addSyntheticMethod(codegenBinding, isSuperAccess());
			currentScope.problemReporter().needToEmulateMethodAccess(codegenBinding, this);
		}
	}
}

public StringBuffer printExpression(int indent, StringBuffer output) {
	if (this.type != null) { // type null for enum constant initializations
		output.append("new "); //$NON-NLS-1$
	}
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
	if (this.type != null) { // type null for enum constant initializations
		this.type.printExpression(0, output);
	}
	output.append('(');
	if (this.arguments != null) {
		for (int i = 0; i < this.arguments.length; i++) {
			if (i > 0) output.append(", "); //$NON-NLS-1$
			this.arguments[i].printExpression(0, output);
		}
	}
	return output.append(')');
}

public TypeBinding resolveType(BlockScope scope) {
	// Propagate the type checking to the arguments, and check if the constructor is defined.
	this.constant = Constant.NotAConstant;
	if (this.type == null) {
		// initialization of an enum constant
		this.resolvedType = scope.enclosingReceiverType();
	} else {
//{ObjectTeams: support detection of new path.R():
		this.type.bits |= IsAllocationType;
// SH}
		this.resolvedType = this.type.resolveType(scope, true /* check bounds*/);
		checkParameterizedAllocation: {
			if (this.type instanceof ParameterizedQualifiedTypeReference) { // disallow new X<String>.Y<Integer>()
				ReferenceBinding currentType = (ReferenceBinding)this.resolvedType;
				if (currentType == null) return currentType;
				do {
					// isStatic() is answering true for toplevel types
					if ((currentType.modifiers & ClassFileConstants.AccStatic) != 0) break checkParameterizedAllocation;
					if (currentType.isRawType()) break checkParameterizedAllocation;
				} while ((currentType = currentType.enclosingType())!= null);
				ParameterizedQualifiedTypeReference qRef = (ParameterizedQualifiedTypeReference) this.type;
				for (int i = qRef.typeArguments.length - 2; i >= 0; i--) {
					if (qRef.typeArguments[i] != null) {
						scope.problemReporter().illegalQualifiedParameterizedTypeAllocation(this.type, this.resolvedType);
						break;
					}
				}
			}
		}
	}
	// will check for null after args are resolved

//{ObjectTeams: replace role allocations:
	if (!scope.isGeneratedScope()) {
		// also accept type.resolvedType if a problem was detected during type.resolveType().
		TypeBinding typeBinding = this.resolvedType;
		if (typeBinding != null && (typeBinding instanceof ProblemReferenceBinding))
			typeBinding = ((ProblemReferenceBinding)typeBinding).closestMatch();
		if (   typeBinding instanceof ReferenceBinding
			&& ((ReferenceBinding)typeBinding).isDirectRole())
	        return resolveAsRoleCreationExpression((ReferenceBinding) typeBinding, scope);
	}
// SH}

	final boolean isDiamond = this.type != null && (this.type.bits & ASTNode.IsDiamond) != 0;
	// resolve type arguments (for generic constructor call)
	if (this.typeArguments != null) {
		int length = this.typeArguments.length;
		boolean argHasError = scope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_5;
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
		if (isDiamond) {
			scope.problemReporter().diamondNotWithExplicitTypeArguments(this.typeArguments);
			return null;
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

	// buffering the arguments' types
	boolean argsContainCast = false;
	TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
	if (this.arguments != null) {
		boolean argHasError = false;
		int length = this.arguments.length;
		argumentTypes = new TypeBinding[length];
		for (int i = 0; i < length; i++) {
			Expression argument = this.arguments[i];
			if (argument instanceof CastExpression) {
				argument.bits |= DisableUnnecessaryCastCheck; // will check later on
				argsContainCast = true;
			}
			if ((argumentTypes[i] = argument.resolveType(scope)) == null) {
				argHasError = true;
			}
		}
		if (argHasError) {
			/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=345359, if arguments have errors, completely bail out in the <> case.
			   No meaningful type resolution is possible since inference of the elided types is fully tied to argument types. Do
			   not return the partially resolved type.
			 */
			if (isDiamond) {
				return null; // not the partially cooked this.resolvedType
			}
			if (this.resolvedType instanceof ReferenceBinding) {
				// record a best guess, for clients who need hint about possible constructor match
				TypeBinding[] pseudoArgs = new TypeBinding[length];
				for (int i = length; --i >= 0;) {
					pseudoArgs[i] = argumentTypes[i] == null ? TypeBinding.NULL : argumentTypes[i]; // replace args with errors with null type
				}
				this.binding = scope.findMethod((ReferenceBinding) this.resolvedType, TypeConstants.INIT, pseudoArgs, this);
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
			}
			return this.resolvedType;
		}
	}
	if (this.resolvedType == null || !this.resolvedType.isValidBinding()) {
		return null;
	}

	// null type denotes fake allocation for enum constant inits
	if (this.type != null && !this.resolvedType.canBeInstantiated()) {
		scope.problemReporter().cannotInstantiate(this.type, this.resolvedType);
		return this.resolvedType;
	}
	if (isDiamond) {
		TypeBinding [] inferredTypes = inferElidedTypes(((ParameterizedTypeBinding) this.resolvedType).genericType(), null, argumentTypes, scope);
		if (inferredTypes == null) {
			scope.problemReporter().cannotInferElidedTypes(this);
			return this.resolvedType = null;
		}
		this.resolvedType = this.type.resolvedType = scope.environment().createParameterizedType(((ParameterizedTypeBinding) this.resolvedType).genericType(), inferredTypes, ((ParameterizedTypeBinding) this.resolvedType).enclosingType());
 	}
	ReferenceBinding allocationType = (ReferenceBinding) this.resolvedType;
//{ObjectTeams: may need to instantiate parameters of constructor
    AnchorMapping anchorMapping = AnchorMapping.setupNewMapping(null, this.arguments, scope);
  try {
	  // ensure allocation type has methods:
	  Dependencies.ensureBindingState(allocationType, ITranslationStates.STATE_LENV_DONE_FIELDS_AND_METHODS);
// SH}
	if (!(this.binding = scope.getConstructor(allocationType, argumentTypes, this)).isValidBinding()) {
//{ObjectTeams: baseclass decapsulation?
	  boolean baseclassDecapsulationAllowed =
						   this.type != null // null happens for enum constants
						&& this.type.getBaseclassDecapsulation(allocationType).isAllowed();
	  if (   this.binding.problemId() == ProblemReasons.NotVisible
		  && (   baseclassDecapsulationAllowed
		      || scope.isGeneratedScope()))
	  {
		this.binding = ((ProblemMethodBinding)this.binding).closestMatch;
		if (baseclassDecapsulationAllowed) {
			scope.enclosingSourceType().roleModel.addInaccessibleBaseMethod(this.binding);
			scope.problemReporter().decapsulation(this, scope);
		}
	  } else {
//  orig:
		if (this.binding.declaringClass == null) {
			this.binding.declaringClass = allocationType;
		}
		if (this.type != null && !this.type.resolvedType.isValidBinding()) {
			return null;
		}
		scope.problemReporter().invalidConstructor(this, this.binding);
		return this.resolvedType;
// :giro
	  }
// SH}
	}
	if ((this.binding.tagBits & TagBits.HasMissingType) != 0) {
		scope.problemReporter().missingTypeInConstructor(this, this.binding);
	}
	if (isMethodUseDeprecated(this.binding, scope, true))
		scope.problemReporter().deprecatedMethod(this.binding, this);
	if (checkInvocationArguments(scope, null, allocationType, this.binding, this.arguments, argumentTypes, argsContainCast, this)) {
		this.bits |= ASTNode.Unchecked;
	}
	if (this.typeArguments != null && this.binding.original().typeVariables == Binding.NO_TYPE_VARIABLES) {
		scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(this.binding, this.genericTypeArguments, this.typeArguments);
	}
	if (!isDiamond && this.resolvedType.isParameterizedTypeWithActualArguments()) {
 		checkTypeArgumentRedundancy((ParameterizedTypeBinding) this.resolvedType, null, argumentTypes, scope);
 	}
	final CompilerOptions compilerOptions = scope.compilerOptions();
	if (compilerOptions.isAnnotationBasedNullAnalysisEnabled && (this.binding.tagBits & TagBits.IsNullnessKnown) == 0) {
//{ObjectTeams: added 2nd arg:
/* orig:
		new ImplicitNullAnnotationVerifier(compilerOptions.inheritNullAnnotations)
 */
		new ImplicitNullAnnotationVerifier(compilerOptions.inheritNullAnnotations, scope.environment())
// SH}
				.checkImplicitNullAnnotations(this.binding, null/*srcMethod*/, false, scope);
	}
//{ObjectTeams: may need to wrap the resolved type
    this.resolvedType = allocationType =
    	(ReferenceBinding)RoleTypeCreator.maybeWrapUnqualifiedRoleType(allocationType, scope, this);
	DependentTypeBinding dependentTypeBinding = this.resolvedType.asPlainDependentType();
	if (dependentTypeBinding != null) {
		ITeamAnchor[] anchorPath = dependentTypeBinding._teamAnchor.getBestNamePath();
		int len = anchorPath.length;
		int prefixLen=0;
		char[][] tokens;
		if (anchorPath[0] instanceof FieldBinding && ((FieldBinding)anchorPath[0]).isStatic()) {
			char[][] qname = TypeAnalyzer.compoundNameOfReferenceType(((FieldBinding)anchorPath[0]).declaringClass, true, false);
			prefixLen = qname.length;
			len += prefixLen;
			tokens = new char[len][];
			System.arraycopy(qname, 0, tokens, 0, qname.length);
		} else {
			tokens = new char[len][];
		}
		for (int i=0; i+prefixLen < len; i++)
			tokens[i+prefixLen] = anchorPath[i].internalName();
		AstGenerator gen = new AstGenerator(this);
		if (len > 1)
			this.valueParam = gen.qualifiedNameReference(tokens);
		else
			this.valueParam = gen.singleNameReference(tokens[0]);
		this.valueParam.resolve(scope);
	}
  } finally {
      AnchorMapping.removeCurrentMapping(anchorMapping);
  }
// SH}
	return allocationType;
}

public TypeBinding[] inferElidedTypes(ReferenceBinding allocationType, ReferenceBinding enclosingType, TypeBinding[] argumentTypes, final BlockScope scope) {
	/* Given the allocation type and the arguments to the constructor, see if we can synthesize a generic static factory
	   method that would, given the argument types and the invocation site, manufacture a parameterized object of type allocationType.
	   If we are successful then by design and construction, the parameterization of the return type of the factory method is identical
	   to the types elided in the <>.
	 */   
	MethodBinding factory = scope.getStaticFactory(allocationType, enclosingType, argumentTypes, this);
	if (factory instanceof ParameterizedGenericMethodBinding && factory.isValidBinding()) {
		ParameterizedGenericMethodBinding genericFactory = (ParameterizedGenericMethodBinding) factory;
		this.inferredReturnType = genericFactory.inferredReturnType;
		return ((ParameterizedTypeBinding)factory.returnType).arguments;
	}
	return null;
}

public void checkTypeArgumentRedundancy(ParameterizedTypeBinding allocationType, ReferenceBinding enclosingType, TypeBinding[] argumentTypes, final BlockScope scope) {
	ProblemReporter reporter = scope.problemReporter();
	if ((reporter.computeSeverity(IProblem.RedundantSpecificationOfTypeArguments) == ProblemSeverities.Ignore) || scope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_7) return;
	if (allocationType.arguments == null) return;  // raw binding
	if (this.genericTypeArguments != null) return; // diamond can't occur with explicit type args for constructor
	if (argumentTypes == Binding.NO_PARAMETERS && this.typeExpected instanceof ParameterizedTypeBinding) {
		ParameterizedTypeBinding expected = (ParameterizedTypeBinding) this.typeExpected;
		if (expected.arguments != null && allocationType.arguments.length == expected.arguments.length) {
			// check the case when no ctor takes no params and inference uses the expected type directly
			// eg. X<String> x = new X<String>()
			int i;
			for (i = 0; i < allocationType.arguments.length; i++) {
				if (allocationType.arguments[i] != expected.arguments[i])
					break;
			}
			if (i == allocationType.arguments.length) {
				reporter.redundantSpecificationOfTypeArguments(this.type, allocationType.arguments);
				return;
			}	
		}
	}
	TypeBinding [] inferredTypes = inferElidedTypes(allocationType.genericType(), enclosingType, argumentTypes, scope);
	if (inferredTypes == null) {
		return;
	}
	for (int i = 0; i < inferredTypes.length; i++) {
		if (inferredTypes[i] != allocationType.arguments[i])
			return;
	}
	reporter.redundantSpecificationOfTypeArguments(this.type, allocationType.arguments);
}

//{ObjectTeams: replace with and resolved role creation expression:
private TypeBinding resolveAsRoleCreationExpression(ReferenceBinding typeBinding, BlockScope scope) {
	RoleModel roleModel = typeBinding.roleModel;
	ReferenceBinding roleClass = roleModel.getClassPartBinding();
	// creating a role that should not be instantiated (OTJLD 2.4.3)?
	if (typeBinding.isRole()) {
		ReferenceBinding subRole = roleClass.roleModel._supercededBy;
		if (subRole != null)
			scope.problemReporter().instantiatingSupercededRole(this, subRole);
	}

	if (   !StateMemento.hasMethodResolveStarted(typeBinding)
		&& roleClass != null)
	{
		Dependencies.ensureRoleState(
			roleClass.roleModel,
			ITranslationStates.STATE_METHODS_CREATED); // forces creation method to be created
	}

	if (!RoleTypeBinding.isRoleWithExplicitAnchor(typeBinding)) {
		ReferenceBinding enclosingType = scope.enclosingReceiverType();
		if (scope.methodScope().isStatic)
			enclosingType = enclosingType.enclosingType();
		ReferenceBinding targetEnclosing = roleClass != null
											? roleClass.enclosingType()
											: roleModel.getTeamModel().getBinding();
		while (enclosingType != null && enclosingType.original() != targetEnclosing) {
			enclosingType = enclosingType.enclosingType();
		}
		if (enclosingType == null) {
			// create a dummy binding for error reporting (just need declaringClass):
			this.binding = new MethodBinding(0, Binding.NO_PARAMETERS, Binding.NO_EXCEPTIONS, typeBinding);
			scope.problemReporter().noSuchEnclosingInstance(
					typeBinding.enclosingType(), this, false);
			return null;
		}
	}

	if (roleModel.hasBaseclassProblem()) {
		scope.methodScope().referenceContext.tagAsHavingErrors();
		return null; // creator is not generated.
	}

	this.roleCreatorCall = CopyInheritance.createConstructorMethodInvocationExpression(scope, this);
	if (this.roleCreatorCall == null)
		return null;

	this.resolvedType = this.roleCreatorCall.resolveType(scope);
	if (typeBinding.isParameterizedType()) {
		this.resolvedType =	 scope.environment().createParameterizedType(
								 (ReferenceBinding)this.resolvedType,
								 ((ParameterizedTypeBinding)typeBinding).arguments,
								 this.resolvedType.enclosingType());
	}

	// UI needs to find the method in this.binding:
	MethodBinding origBinding = this.roleCreatorCall.binding;
	if (origBinding != null && origBinding.model != null && origBinding.model._srcCtor != null)
		this.binding = origBinding.model._srcCtor;
	else
		this.binding = origBinding;

	return this.resolvedType;
}
// SH}

public void setActualReceiverType(ReferenceBinding receiverType) {
	// ignored
}

public void setDepth(int i) {
	// ignored
}

public void setFieldIndex(int i) {
	// ignored
}

public void traverse(ASTVisitor visitor, BlockScope scope) {
//{ObjectTeams: alternate ast
	if (this.roleCreatorCall != null) {
		this.roleCreatorCall.traverse(visitor, scope);
		return;
	}
// SH}

	if (visitor.visit(this, scope)) {
		if (this.typeArguments != null) {
			for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
				this.typeArguments[i].traverse(visitor, scope);
			}
		}
		if (this.type != null) { // enum constant scenario
			this.type.traverse(visitor, scope);
		}
		if (this.arguments != null) {
			for (int i = 0, argumentsLength = this.arguments.length; i < argumentsLength; i++)
				this.arguments[i].traverse(visitor, scope);
		}
	}
	visitor.endVisit(this, scope);
}
/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#setExpectedType(org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
 */
public void setExpectedType(TypeBinding expectedType) {
	this.typeExpected = expectedType;
}
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#expectedType()
 */
public TypeBinding expectedType() {
	return this.typeExpected;
}

}
