/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ExplicitConstructorCall.java 23405 2010-02-03 17:02:18Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TsuperReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.BytecodeTransformer;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.DeclaredLifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.AnchorMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;

/**
 * OTDT changes:
 *
 * What: Additional kind Tsuper. (Note, that Base is handled by BaseAllocationExpression).
 * How:  A tsuper() call is treated mostly like a this call.
 * 		 The flag Tsuper signals, that a marker arg has to be added to select tsuper versions.
 *
 *
 * What: Manage super calls in constructors with declared lifting.
 * Why:  A self call in such a constructor could pass the lifted version of the argument.
 * How:  See checkLiftingTeamConstructor() and class comment in class Lifting.
 *
 *
 * What: Management of chained tsuper argument.
 * Why:  If a constructor is copied, an additional argument is added to its signature to
 * 	     allow explicit selection for tsuper calls.
 *       Also any constructor to be invoked via this() will be augmented with a marker arg.
 * 		 Therefor in addition to the signatures, the this() call in a copied constructor
 * 		 will need to be augmented by a marker arg.
 * 		 A value for this marker arg can be passed through from the provided argument.
 * Example:
 * <pre>
 * 		class T1.R {
 * 			R(int i) {..}
 * 			R() {
 * 				this(42);
 * 				moreStatements();
 * 			}
 * 		}
 * </pre>
 * when copied to T2 extends T1 becomes:
 * <pre>
 * 		class T2.R {
 * 			R(int i, TSuper_OT$T1 _OT$marker) {..}
 * 			R(TSuper_OT$T1 _OT$marker) {
 * 				this(42, _OT$marker);
 * 				moreStatements();
 * 			}
 * 		}
 * </pre>
 * Participants:
 *      chainTSuperMarkerArgPos:
 * 			mediate between resolve(), where the situation is recognized and generateCode().
 * 		in class BytecodeTransformer:
 * 		+ addChainingPlaceholder:
 * 			prepare the byte code so it can easily be changed.
 * 		+ adjustCode(), replaceChainArg():
 * 			recognized the characteristic byte code sequence produced before, and patch it.
 *
 * @version $Id: ExplicitConstructorCall.java 23405 2010-02-03 17:02:18Z stephan $
 */
public class ExplicitConstructorCall extends Statement implements InvocationSite {

	public Expression[] arguments;
	public Expression qualification;
	public MethodBinding binding;							// exact binding resulting from lookup
	MethodBinding syntheticAccessor;						// synthetic accessor for inner-emulation
	public int accessMode;
	public TypeReference[] typeArguments;
	public TypeBinding[] genericTypeArguments;

	public final static int ImplicitSuper = 1;
	public final static int Super = 2;
	public final static int This = 3;
//{ObjectTeams: new constant for TSuper
	public static final int Tsuper = 4;
// SH}

//{ObjectTeams:
//	 if this is a super() or this() call within a constructor record the position
//	 that an additional tsuper-mark-arg of this constructor will receive
//	 when being copied.
	private int chainTSuperMarkArgPos = -1;
// SH}

	public VariableBinding[][] implicitArguments;

	// TODO Remove once DOMParser is activated
	public int typeArgumentsSourceStart;

	public ExplicitConstructorCall(int accessMode) {
		this.accessMode = accessMode;
	}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		// must verify that exceptions potentially thrown by this expression are caught in the method.

		try {
			((MethodScope) currentScope).isConstructorCall = true;

			// process enclosing instance
			if (this.qualification != null) {
				flowInfo =
					this.qualification
						.analyseCode(currentScope, flowContext, flowInfo)
						.unconditionalInits();
			}
			// process arguments
			if (this.arguments != null) {
				for (int i = 0, max = this.arguments.length; i < max; i++) {
					flowInfo =
						this.arguments[i]
							.analyseCode(currentScope, flowContext, flowInfo)
							.unconditionalInits();
				}
			}

			ReferenceBinding[] thrownExceptions;
			if ((thrownExceptions = this.binding.thrownExceptions) != Binding.NO_EXCEPTIONS) {
				if ((this.bits & ASTNode.Unchecked) != 0 && this.genericTypeArguments == null) {
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=277643, align with javac on JLS 15.12.2.6
					thrownExceptions = currentScope.environment().convertToRawTypes(this.binding.thrownExceptions, true, true);
				}				
				// check exceptions
				flowContext.checkExceptionHandlers(
					thrownExceptions,
					(this.accessMode == ExplicitConstructorCall.ImplicitSuper)
						? (ASTNode) currentScope.methodScope().referenceContext
						: (ASTNode) this,
					flowInfo,
					currentScope);
			}
			manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
			manageSyntheticAccessIfNecessary(currentScope, flowInfo);
			return flowInfo;
		} finally {
			((MethodScope) currentScope).isConstructorCall = false;
		}
	}

	/**
	 * Constructor call code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if ((this.bits & ASTNode.IsReachable) == 0) {
			return;
		}
		try {
			((MethodScope) currentScope).isConstructorCall = true;

			int pc = codeStream.position;
			codeStream.aload_0();

			MethodBinding codegenBinding = this.binding.original();
			ReferenceBinding targetType = codegenBinding.declaringClass;

			// special name&ordinal argument generation for enum constructors
			if (targetType.erasure().id == TypeIds.T_JavaLangEnum || targetType.isEnum()) {
				codeStream.aload_1(); // pass along name param as name arg
				codeStream.iload_2(); // pass along ordinal param as ordinal arg
			}
			// handling innerclass constructor invocation
			// handling innerclass instance allocation - enclosing instance arguments
			if (targetType.isNestedType()) {
				codeStream.generateSyntheticEnclosingInstanceValues(
					currentScope,
					targetType,
					(this.bits & ASTNode.DiscardEnclosingInstance) != 0 ? null : this.qualification,
					this);
			}
//FIXME(SH): pass value parameters?!
			// generate arguments
			generateArguments(this.binding, this.arguments, currentScope, codeStream);

//{ObjectTeams: prepare chaining of tsuper marker arg:
			if (this.chainTSuperMarkArgPos > -1)
				BytecodeTransformer.addChainingPlaceholder(currentScope, codeStream, this.chainTSuperMarkArgPos);
// SH}

			// handling innerclass instance allocation - outer local arguments
			if (targetType.isNestedType()) {
				codeStream.generateSyntheticOuterArgumentValues(
					currentScope,
					targetType,
					this);
			}
			if (this.syntheticAccessor != null) {
				// synthetic accessor got some extra arguments appended to its signature, which need values
				for (int i = 0,
					max = this.syntheticAccessor.parameters.length - codegenBinding.parameters.length;
					i < max;
					i++) {
					codeStream.aconst_null();
				}
				codeStream.invoke(Opcodes.OPC_invokespecial, this.syntheticAccessor, null /* default declaringClass */);
			} else {
				codeStream.invoke(Opcodes.OPC_invokespecial, codegenBinding, null /* default declaringClass */);
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
		} finally {
			((MethodScope) currentScope).isConstructorCall = false;
		}
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
	 */
	public TypeBinding[] genericTypeArguments() {
		return this.genericTypeArguments;
	}

	public boolean isImplicitSuper() {
		return (this.accessMode == ExplicitConstructorCall.ImplicitSuper);
	}

//{ObjectTeams
	public boolean isTsuperAccess() {
		return this.accessMode == Tsuper;
	}
// Markus Witte}
	public boolean isSuperAccess() {
// {ObjectTeams
/* orig:
		return this.accessMode != ExplicitConstructorCall.This;
  :giro */
		return (this.accessMode == ExplicitConstructorCall.Super) || (this.accessMode == ExplicitConstructorCall.ImplicitSuper);
// Markus Witte}
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
	void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
		ReferenceBinding superTypeErasure = (ReferenceBinding) this.binding.declaringClass.erasure();

		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0)	{
		// perform some emulation work in case there is some and we are inside a local type only
		if (superTypeErasure.isNestedType()
			&& currentScope.enclosingSourceType().isLocalType()) {

			if (superTypeErasure.isLocalType()) {
				((LocalTypeBinding) superTypeErasure).addInnerEmulationDependent(currentScope, this.qualification != null);
			} else {
				// locally propagate, since we already now the desired shape for sure
				currentScope.propagateInnerEmulation(superTypeErasure, this.qualification != null);
			}
		}
		}
	}

	public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0)	{
			// if constructor from parameterized type got found, use the original constructor at codegen time
			MethodBinding codegenBinding = this.binding.original();

			// perform some emulation work in case there is some and we are inside a local type only
			if (this.binding.isPrivate() && this.accessMode != ExplicitConstructorCall.This) {
				ReferenceBinding declaringClass = codegenBinding.declaringClass;
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
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {
		printIndent(indent, output);
		if (this.qualification != null) this.qualification.printExpression(0, output).append('.');
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
//{ObjectTeams
		if (this.accessMode == ExplicitConstructorCall.Tsuper) {
			 output.append("tsuper("); //$NON-NLS-1$
		} else
// Markus Witte}
		if (this.accessMode == ExplicitConstructorCall.This) {
			output.append("this("); //$NON-NLS-1$
		} else {
			output.append("super("); //$NON-NLS-1$
		}
		if (this.arguments != null) {
//{ObjectTeams: hide marker arg:
			int len = this.arguments.length;
			if (   this.accessMode == ExplicitConstructorCall.Tsuper
			    && TSuperHelper.isMarkerArg(this.arguments[len-1]))
				len--;
/* orig:
			for (int i = 0; i < this.arguments.length; i++) {
  :giro */
			for (int i = 0; i < len; i++) {
// SH}
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.arguments[i].printExpression(0, output);
			}
		}
		return output.append(");"); //$NON-NLS-1$
	}

	public void resolve(BlockScope scope) {
		// the return type should be void for a constructor.
		// the test is made into getConstructor

		// mark the fact that we are in a constructor call.....
		// unmark at all returns
		MethodScope methodScope = scope.methodScope();
		try {
			AbstractMethodDeclaration methodDeclaration = methodScope.referenceMethod();
			if (methodDeclaration == null
					|| !methodDeclaration.isConstructor()
					|| ((ConstructorDeclaration) methodDeclaration).constructorCall != this) {
				scope.problemReporter().invalidExplicitConstructorCall(this);
				// fault-tolerance
				if (this.qualification != null) {
					this.qualification.resolveType(scope);
				}
				if (this.typeArguments != null) {
					for (int i = 0, max = this.typeArguments.length; i < max; i++) {
						this.typeArguments[i].resolveType(scope, true /* check bounds*/);
					}
				}
				if (this.arguments != null) {
					for (int i = 0, max = this.arguments.length; i < max; i++) {
						this.arguments[i].resolveType(scope);
					}
				}
				return;
			}
			methodScope.isConstructorCall = true;
			ReferenceBinding receiverType = scope.enclosingReceiverType();
			boolean rcvHasError = false;
//{ObjectTeams: treat tsuper like this calls:
			/* @original
			if (this.accessMode != ExplicitConstructorCall.This) {
			*/
			if (isSuperAccess()) {
// Markus Witte}
			  	receiverType = receiverType.superclass();
			  	TypeReference superclassRef = scope.referenceType().superclass;
				if (superclassRef != null && superclassRef.resolvedType != null && !superclassRef.resolvedType.isValidBinding()) {
					rcvHasError = true;
				}
			}
//{ObjectTeams: compiling org.objectteams.Team$__OT__Confined ?
			if (receiverType == null) {
				if (CharOperation.equals(scope.enclosingSourceType().compoundName,
							IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED))
					receiverType = scope.getJavaLangObject(); // use this supertype only this one time!
				else // testharness for checking a hypothesis:
					scope.problemReporter().missingImplementation(this, "Detected null receiverType"); //$NON-NLS-1$
//				throw new InternalCompilerError("caught you");
			}
// SH}
			if (receiverType != null) {
				// prevent (explicit) super constructor invocation from within enum
				if (this.accessMode == ExplicitConstructorCall.Super && receiverType.erasure().id == TypeIds.T_JavaLangEnum) {
					scope.problemReporter().cannotInvokeSuperConstructorInEnum(this, methodScope.referenceMethod().binding);
				}
				// qualification should be from the type of the enclosingType
				if (this.qualification != null) {
					if (this.accessMode != ExplicitConstructorCall.Super) {
						scope.problemReporter().unnecessaryEnclosingInstanceSpecification(
							this.qualification,
							receiverType);
					}
					if (!rcvHasError) {
						ReferenceBinding enclosingType = receiverType.enclosingType();
						if (enclosingType == null) {
							scope.problemReporter().unnecessaryEnclosingInstanceSpecification(this.qualification, receiverType);
							this.bits |= ASTNode.DiscardEnclosingInstance;
						} else {
							TypeBinding qTb = this.qualification.resolveTypeExpecting(scope, enclosingType);
							this.qualification.computeConversion(scope, qTb, qTb);
						}
					}
				}
			}
			// resolve type arguments (for generic constructor call)
			if (this.typeArguments != null) {
				boolean argHasError = scope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_5;
				int length = this.typeArguments.length;
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
					return;
				}
			}
			// arguments buffering for the method lookup
			TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
			boolean argsContainCast = false;
			if (this.arguments != null) {
				boolean argHasError = false; // typeChecks all arguments
				int length = this.arguments.length;
				argumentTypes = new TypeBinding[length];
				for (int i = 0; i < length; i++) {
					Expression argument = this.arguments[i];
					if (argument instanceof CastExpression) {
						argument.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
						argsContainCast = true;
					}
					if ((argumentTypes[i] = argument.resolveType(scope)) == null) {
						argHasError = true;
					}
				}
				if (argHasError) {
					if (receiverType == null) {
						return;
					}
					// record a best guess, for clients who need hint about possible contructor match
					TypeBinding[] pseudoArgs = new TypeBinding[length];
					for (int i = length; --i >= 0;) {
						pseudoArgs[i] = argumentTypes[i] == null ? TypeBinding.NULL : argumentTypes[i]; // replace args with errors with null type
					}
					this.binding = scope.findMethod(receiverType, TypeConstants.INIT, pseudoArgs, this);
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
					return;
				}
			} else if (receiverType.erasure().id == TypeIds.T_JavaLangEnum) {
				// TODO (philippe) get rid of once well-known binding is available
				argumentTypes = new TypeBinding[] { scope.getJavaLangString(), TypeBinding.INT };
			}
			if (receiverType == null) {
				return;
			}
//{ObjectTeams: type wrapping for arguments and error handling for tsuper calls.
/* @original:
			if ((this.binding = scope.getConstructor(receiverType, argumentTypes, this)).isValidBinding()) {
*/
			// ensure that types in signatures of teams and roles are wrapped:
			if (receiverType.isTeam() || receiverType.isRole())
	            Dependencies.ensureBindingState(receiverType, ITranslationStates.STATE_TYPES_ADJUSTED);

            // additional error handling for tsuper calls:
            if (this.accessMode == Tsuper) {
            	if (!receiverType.isRole()) {
            		scope.problemReporter().tsuperOutsideRole(
            				methodScope.referenceMethod(),
							this,
							receiverType);
            		return;
            	}
            	RoleModel role = receiverType.roleModel;
            	if (role.getImplicitSuperRole() == null) {
            		scope.problemReporter().tsuperCallWithoutTsuperRole(
            				receiverType,
							this);
            		return;
            	}
            	if (role._refinesExtends) {
            		scope.problemReporter().tsuperCtorDespiteRefinedExtends(this, receiverType.superclass());
            		return;
            	}
            }

            Expression receiver = null;
            switch (this.accessMode) {
            case This  : receiver = new ThisReference(this.sourceStart,this.sourceEnd); break;
            case ImplicitSuper:
            case Super : receiver = new SuperReference(this.sourceStart,this.sourceEnd); break;
            case Tsuper: receiver = new TsuperReference(this.sourceStart,this.sourceEnd); break;
            }

            // find the constructor:
            AnchorMapping anchorMapping = null;
            try {
                anchorMapping = AnchorMapping.setupNewMapping(
	                    receiver, this.arguments, scope);
	/*~orig~*/  this.binding = scope.getConstructor(receiverType, argumentTypes, this);

				// check different reasons for changing the accessMode

				MethodBinding tsuperMethod = null;
				TypeBinding[] tsuperArgs = null;

				if (!this.binding.isValidBinding())
				{
					if (this.accessMode == Tsuper) {
						// tsuper version not found. Try without marker arg:
						// (marker arg is only added when overriding a tsuper version!)
						int len = argumentTypes.length-1;
						TypeBinding[] strippedArgumentTypes = new TypeBinding[len];
						System.arraycopy(argumentTypes, 0, strippedArgumentTypes, 0, len);
						MethodBinding strippedMethod = scope.getConstructor(receiverType, strippedArgumentTypes, this);
						if (   strippedMethod.isValidBinding()
							&& strippedMethod.copyInheritanceSrc != null)
						{
							this.binding = strippedMethod;
							argumentTypes = strippedArgumentTypes;
							System.arraycopy (
									this.arguments, 0,
									this.arguments = new Expression[len], 0, len);
						}
					} else if (accessingSuperteam(scope))
					{
						// is a copied version present (maybe we just copied it)?
						// invoke it as tsuper rather than super.
						TeamModel teamModel = scope.enclosingSourceType().getTeamModel();

						tsuperArgs = AstEdit.extendTypeArray(argumentTypes, teamModel.getMarkerInterfaceBinding(scope));
						tsuperMethod = scope.getConstructor(receiverType, tsuperArgs, this);
					}
				} else if (   accessingSuperteam(scope)
						   && methodDeclaration.binding != null
						   && RoleTypeBinding.hasNonExternalizedRoleParameter(methodDeclaration.binding))
				{
					// all constructors with role parameters need to be copied,
					// because subteams with declared lifting require control over all code
					// in the super-call chain.
					if (RoleTypeBinding.hasNonExternalizedRoleParameter(this.binding))
					{
						boolean needsLifting = ((ConstructorDeclaration)methodDeclaration).needsLifting;
						tsuperMethod = DeclaredLifting.copyTeamConstructorForDeclaredLifting(
														scope, this.binding, argumentTypes, needsLifting);
					} else {
						tsuperMethod = DeclaredLifting.maybeCreateTurningCtor(
								scope.referenceType(),
								this.binding,
								new AstGenerator(this.sourceStart, this.sourceEnd));
					}
					if (tsuperMethod != null)
						tsuperArgs = AstEdit.extendTypeArray(
											argumentTypes,
											tsuperMethod.parameters[tsuperMethod.parameters.length-1]);
				}
				if (   tsuperMethod != null
					&& tsuperMethod.isValidBinding()
					&& tsuperMethod != this.binding
					&& tsuperMethod != methodDeclaration.binding)
				{
					updateFromTSuper(scope.enclosingSourceType().superclass(),
									 tsuperArgs[tsuperArgs.length-1]);
					this.binding       = tsuperMethod;
					argumentTypes = tsuperArgs;
				}
				
				// perform any adjustments needed for declared lifting in team constructors:
				if (this.binding.problemId() == ProblemReasons.NotFound)
					argumentTypes = checkLiftingTeamCtor(scope, argumentTypes, (ConstructorDeclaration)methodDeclaration);
            } finally {
                AnchorMapping.removeCurrentMapping(anchorMapping);
            }


/*~orig~*/  if (this.binding.isValidBinding()) {

                // record chainTSuperMarkArgPos if needed
                if (   scope.enclosingSourceType().isRole()
                	&& !TSuperHelper.isTSuper(this.binding)
                	&& this.binding.declaringClass == scope.enclosingSourceType())
            	{
                	MethodBinding enclosingMethod = methodDeclaration.binding;
                	if (enclosingMethod != null)
                		this.chainTSuperMarkArgPos = enclosingMethod.parameters.length+enclosingMethod.declaringClass.depth()+1;
                }
// SH}
				if ((this.binding.tagBits & TagBits.HasMissingType) != 0) {
					if (!methodScope.enclosingSourceType().isAnonymousType()) {
						scope.problemReporter().missingTypeInConstructor(this, this.binding);
					}
				}
				if (isMethodUseDeprecated(this.binding, scope, this.accessMode != ExplicitConstructorCall.ImplicitSuper)) {
					scope.problemReporter().deprecatedMethod(this.binding, this);
				}
				if (checkInvocationArguments(scope, null, receiverType, this.binding, this.arguments, argumentTypes, argsContainCast, this)) {
					this.bits |= ASTNode.Unchecked;
				}
				if (this.binding.isOrEnclosedByPrivateType()) {
					this.binding.original().modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
				}
				if (this.typeArguments != null
						&& this.binding.original().typeVariables == Binding.NO_TYPE_VARIABLES) {
					scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(this.binding, this.genericTypeArguments, this.typeArguments);
				}
			} else {
				if (this.binding.declaringClass == null) {
					this.binding.declaringClass = receiverType;
				}
				if (rcvHasError)
					return;
//{ObjectTeams:	revert augmented signature for error reporting:
				if (this.accessMode == Tsuper) {
					int len = this.binding.parameters.length;
					System.arraycopy(
							this.binding.parameters, 0,
							this.binding.parameters = new TypeBinding[len-1], 0,
							len-1);
					this.binding.declaringClass = receiverType.roleModel.getImplicitSuperRole().getBinding();
				}
// SH}
				scope.problemReporter().invalidConstructor(this, this.binding);
			}
		} finally {
			methodScope.isConstructorCall = false;
		}
	}

//{ObjectTeams: helper functions for lifting in constructors:
	/**
	 * Is this ExplicitConstructorCall an access to a constructor from a super team?
	 */
	private boolean accessingSuperteam(BlockScope scope) {
		return   isSuperAccess()
			   && scope.enclosingSourceType().isTeam();
	}

	/**
	 * Given that regular lookup did not find a suitable ctor to call, try:
	 * 1.: are we just providing an additional marker arg,
	 *     which should be added to an existing constructor?
	 * 2.: would an existing constructor match if we lifted our arguments?
	 *
	 * @param scope enclosing method scope
	 * @param argumentTypes types of argument expressions of this self call
	 * @return possibly adjusted version of argumentTypes
	 */
	private TypeBinding[] checkLiftingTeamCtor(BlockScope scope, TypeBinding[] argumentTypes, ConstructorDeclaration ctorDecl)
	{
		if (!scope.referenceType().isTeam())
			return argumentTypes;
		ReferenceBinding superTeam = scope.enclosingSourceType().superclass();
		MethodBinding[] ctors = superTeam.getMethods(TypeConstants.INIT);
		if (ctors != null) {
			for (int i = 0; i < ctors.length; i++) {
				MethodBinding ctor = ctors[i];
				if (isChainingMatch(this.arguments, ctor.parameters)) {
					if (this.accessMode == This) {
						// At this spot we know: Lifting.prepareLiftingArg had added a marker arg.
						// Now create a chaining ctor for this self call:
						// (only now we have the types of all actual arguments).
						MethodBinding newBinding;
						newBinding = DeclaredLifting.copyTeamConstructorForDeclaredLifting(
													scope, ctor, argumentTypes, ctorDecl.needsLifting);
						if (newBinding != null)
							this.binding = newBinding;
						break;
					}
				} else {
					if (isSuperAccess()) {
						TypeBinding[] adjustedArgtypes = getAdjustedArgTypes(
															scope.referenceType().getTeamModel(),
															scope,
															this.arguments,
															ctor.parameters,
															ctor);
						if (adjustedArgtypes != null) {
							// lifting must be performed. Cannot do it before self-calling,
							// so let the called ctor do the lifting:
							// ie., create a copy of the super ctor with lifting:

							MethodBinding newBinding;
							newBinding = DeclaredLifting.copyTeamConstructorForDeclaredLifting(
													scope, ctor, adjustedArgtypes, ctorDecl.needsLifting);
							if (newBinding != null) {
								this.binding = newBinding;
								TypeBinding markerType = this.binding.parameters[this.binding.parameters.length-1];
								updateFromTSuper(superTeam, markerType);
								argumentTypes = AstEdit.extendTypeArray(argumentTypes, markerType);
							}
							break;
						}
					}
				}
			}
		}
		return argumentTypes;
	}

	/** Is this self call requesting a ctor with added marker arg? */
	private boolean isChainingMatch(Expression[] provided, TypeBinding[] expected) {
		if (provided == null)
			return expected.length == 0;
		if (provided.length != expected.length+1)
			return false;
		for (int i = 0; i < expected.length; i++) {
			if (!provided[i].resolvedType.isCompatibleWith(expected[i]))
				return false;
		}
		return TSuperHelper.isMarkerArg(provided[provided.length-1]);
	}
	/** Would types match if we use lifting?
	 * If so, also add a marker arg to the parameters.
	 */
	private TypeBinding[] getAdjustedArgTypes(
			TeamModel teamModel, BlockScope scope, Expression[] provided, TypeBinding[] expected, MethodBinding method)
	{
		if (provided == null)
		{
			if (expected.length == 0)
				return new TypeBinding[0];
			else
				return null;
		}
		if (provided.length != expected.length)
			return null;
		TypeBinding[] adjustedArgtypes = new TypeBinding[expected.length+1];
		for (int i = 0; i < expected.length; i++) {
			adjustedArgtypes[i] = this.arguments[i].resolvedType;

			TypeBinding param = expected[i];
			TypeBinding arg = provided[i].resolvedType;
			if (AnchorMapping.areTypesEqual(param, arg, method) || arg.isCompatibleWith(param)) // Note: param-anchored types must be exact matches here (limitation)
				continue;
			if (TSuperHelper.isMarkerInterface(param))
				return null;
			TypeBinding roleType = TeamModel.getRoleToLiftTo(scope, arg, param, false, /*location*/provided[i]);
			if (roleType == null)
				return null;
			// FIXME(SH): should roleType be stored in adjustedArgtypes??
		}
		adjustedArgtypes[expected.length] = teamModel.markerInterface.binding;
		return adjustedArgtypes;
	}
	/**
	 * The target method exists in the current team class, discriminated by a proper marker arg.
	 * Update this self call to use the local version as tsuper (arguments and accessMode).
	 */
	private void updateFromTSuper(ReferenceBinding superTeam,
								  TypeBinding      markerType)
	{
		Expression markerArg = TSuperHelper.createMarkerArgExpr(
									superTeam,
									new AstGenerator(this.sourceStart, this.sourceEnd));
		markerArg.resolvedType = markerType;
		this.arguments =  AstEdit.extendExpressionArray(this.arguments, markerArg);
		this.accessMode = Tsuper; // is a tsuper call that was recognized late.
	}
// SH}

	public void setActualReceiverType(ReferenceBinding receiverType) {
		// ignored
	}

	public void setDepth(int depth) {
		// ignore for here
	}

	public void setFieldIndex(int depth) {
		// ignore for here
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.qualification != null) {
				this.qualification.traverse(visitor, scope);
			}
			if (this.typeArguments != null) {
				for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
					this.typeArguments[i].traverse(visitor, scope);
				}
			}
			if (this.arguments != null) {
				for (int i = 0, argumentLength = this.arguments.length; i < argumentLength; i++)
					this.arguments[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}
