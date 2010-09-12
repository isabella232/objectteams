/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CallinMappingDeclaration.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameBINDIN;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameBINDOUT;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameafter;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamebefore;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNamereplace;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.CALLIN_FLAG_BASE_SUPER_CALL;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.CALLIN_FLAG_DEFINITELY_MISSING_BASECALL;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.CALLIN_FLAG_POTENTIALLY_MISSING_BASECALL;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

/**
 * NEW for OTDT.
 *
 * AST node for a callin method mapping.
 * Aside from its data structure, this class is responsible for type checking.
 *
 *
 * @author Markus Witte
 * @version $Id: CallinMappingDeclaration.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class CallinMappingDeclaration extends AbstractMethodMappingDeclaration
{
	// TerminalSymbols.TokenNamebefore,after,replace*
    public int callinModifier;

    /** This name is never null, either name as mentioned in source code
     *  or a generated name "<File:Line,Col>". */
    public char[] name;

	public MethodSpec[] baseMethodSpecs;
    public MethodSpec[] getBaseMethodSpecs () {
    	return this.baseMethodSpecs;
    }
	public int baseDeclarationSourceStart() {
		if (this.baseMethodSpecs == null || this.baseMethodSpecs.length == 0)
			return this.declarationSourceEnd+1;
		MethodSpec baseMethod = this.baseMethodSpecs[0];
		if (baseMethod.returnType != null)
			return baseMethod.returnType.sourceStart;
		return baseMethod.declarationSourceStart;
	}

	/** add a base method spec, iff none has been given yet. */
	public void checkAddBasemethodSpec(MethodSpec baseSpec) {
		if (this.baseMethodSpecs == null || this.baseMethodSpecs.length == 0)
			this.baseMethodSpecs = new MethodSpec[] {baseSpec};
	}

    /**
     * If the role method has a role return type, lifting should use this type:
	 * (Note, that this type may differ from the declared return type due to signature weakening!)
     */
    public TypeBinding realRoleReturn = null;
    /**
     * If realRoleReturn != null then this field keeps the method that the base call
     * should use for lifting
     */
    public MethodBinding liftMethod;

    /**
     * This method is only relevant for private inherited base methods.
     * Otherwise this method will never be called for callin mappings,
     * because MethodBinding.canBeSeenBy(..) implements a shortcut for
     * MessageSends within the scope of a  callin wrapper.
     */
    public boolean canAccessInvisibleBase () {
    	return true;
    }

    // one wrapper for each base method
    // (currently, will we optimize this? see AbstractMethodMappingDeclaration.resolveMethodSpecs())
    public MethodDeclaration[] wrappers;

    public GuardPredicateDeclaration predicate = null;

	// internally store here, whether the result from a base call
	// is needed because the binding does not provide the result.
    private MethodSpec baseMethodNeedingResultFromBasecall = null;

    // store here whether result is mapped in a parameter mapping.
    public boolean isResultMapped = false;

    public CallinMappingDeclaration(CompilationResult compilationResult)
    {
        super(compilationResult);
    }

	public void resolveMethodSpecs(RoleModel role,
								   ReferenceBinding baseType,
								   boolean resolveBaseMethods)
	{
		super.resolveMethodSpecs(role, baseType, resolveBaseMethods);
		if (this.roleMethodSpec.isValid() && this.roleMethodSpec.isStatic())
			if (this.predicate != null)
				makeMethodStatic(this.predicate);

		if (!resolveBaseMethods)
			return;

		MethodBinding[] baseMethods = new MethodBinding[this.baseMethodSpecs.length];
		for (int i = 0; i < this.baseMethodSpecs.length; i++) {
			if (this.baseMethodSpecs[i].resolvedMethod != null) {
				baseMethods[i] = this.baseMethodSpecs[i].resolvedMethod;
				if (isDangerousMethod(baseMethods[i]))
					this.scope.problemReporter().dangerousCallinBinding(this.baseMethodSpecs[i]);
			} else {
				MethodSpec spec = this.baseMethodSpecs[i];
				baseMethods[i] = new ProblemMethodBinding(spec.selector, null, baseType, 0);
			}
		}
		for (MethodBinding aBaseMethod : baseMethods) {
			if (aBaseMethod.isValidBinding() && aBaseMethod.returnType != TypeBinding.VOID) {
				if (   this.callinModifier == TerminalTokens.TokenNameafter
					&& this.roleMethodSpec.isValid()
					&& this.roleMethodSpec.resolvedType() != TypeBinding.VOID)
					this.scope.problemReporter().ignoringRoleMethodReturn(this.roleMethodSpec);
				break;
			}
		}
		this.binding._baseMethods = baseMethods;
	}
	boolean isDangerousMethod(MethodBinding method) {
		if (CharOperation.equals(method.selector, "hashCode".toCharArray())) //$NON-NLS-1$
			return method.parameters == Binding.NO_PARAMETERS;
		if (CharOperation.equals(method.selector, "equals".toCharArray())) //$NON-NLS-1$
			return (method.parameters.length == 1) && (method.parameters[0].id == TypeIds.T_JavaLangObject);
		return false;
	}
	/** For predicates the parser doesn't know if they are static,
	 *  update the method once we know the role method is indeed static.
	 *  Updates declaration, binding and scope for both class- and interface-part.
	 */
	private void makeMethodStatic(AbstractMethodDeclaration method) {
		method.modifiers |= ClassFileConstants.AccStatic;
		if (method.binding != null)
			method.binding.modifiers |= ClassFileConstants.AccStatic;
		if (method.scope != null)
			method.scope.isStatic = true;
		if (method.interfacePartMethod != null)
			makeMethodStatic(method.interfacePartMethod);
	}

	/**
     * In this case: check match of "replace" and "callin" flags, plus static-ness
     * @param haveBaseMethods have base methods been resolved?
     * @param baseClass       the role's bound base class
	 */
	@Override
	protected void checkModifiers(boolean haveBaseMethods, ReferenceBinding baseClass) {
	    if (this.ignoreFurtherInvestigation) // error was already flagged, i.e. missing replace
	        return;
	    // replace and callin matching:
		if (isReplaceCallin()) {
			if (!this.roleMethodSpec.resolvedMethod.isCallin()) {
				this.scope.problemReporter().replaceMappingToNonCallin(this.roleMethodSpec, this.roleMethodSpec.resolvedMethod);
				this.binding.tagBits |= TagBits.HasMappingIncompatibility;
				return;
			}
		} else {
			if (this.roleMethodSpec.resolvedMethod.isCallin()) {
				this.scope.problemReporter().callinMethodBoundNonReplace(this.roleMethodSpec, this);
				this.binding.tagBits |= TagBits.HasMappingIncompatibility;
				return;
			}
		}
		if (haveBaseMethods) {
			// static non-static consistency:
			if (!this.roleMethodSpec.resolvedMethod.isStatic()) {
				for (int i = 0; i < this.baseMethodSpecs.length; i++) {
					this.baseMethodSpecs[i].checkStaticness(this, false);
				}
			}
			if (isReplaceCallin()) {
				if (this.roleMethodSpec.resolvedMethod.isStatic()) {
					for (int i = 0; i < this.baseMethodSpecs.length; i++) {
						this.baseMethodSpecs[i].checkStaticness(this, true);
					}
				}
			}
			// callin-to-final? respect OTJLD 4.1(f)
			for (int i = 0; i < this.baseMethodSpecs.length; i++) {
				MethodBinding baseMethod = this.baseMethodSpecs[i].resolvedMethod;
				if (baseMethod != null && baseMethod.isFinal()) {
					if (baseMethod.declaringClass != baseClass) {
						this.scope.problemReporter().bindingToInheritedFinal(this.baseMethodSpecs[i], baseMethod, baseClass);
						this.binding.tagBits |= TagBits.HasMappingIncompatibility;
					}
				}
			}
		}
	}

	/**
	 * Check all parameters in methodSpec against the resolved role method.
	 * Also record which parameters (including result) need translation (lifting/lowering).
	 *
	 * Pre: not called if parameter mappings are present.
	 * @param methodSpec
	 */
	protected boolean internalCheckParametersCompatibility(
			MethodSpec methodSpec,
			TypeBinding[] roleParams,
			TypeBinding[] baseParams)
	{
		if (baseParams.length < roleParams.length) {
			this.scope.problemReporter().tooFewArgumentsInMethodMapping(this.roleMethodSpec, methodSpec, false/*callout*/);
			this.binding.tagBits |= TagBits.HasMappingIncompatibility;
			return false;
		} else {
			// before modifying the parameters array copy it:
			System.arraycopy(this.roleMethodSpec.parameters, 0, 
							 this.roleMethodSpec.parameters = new TypeBinding[roleParams.length], 0,
							 roleParams.length);
			for (int j = 0; j < roleParams.length; j++) {
				TypeBinding baseParam = baseParams[j];
				TypeBinding roleParam = roleParams[j];
				if (baseParam.dimensions() != roleParam.dimensions()) {
					this.scope.problemReporter().incompatibleMappedArgument(
							baseParam, roleParam, this.roleMethodSpec, j, /*callout*/false);
					this.binding.tagBits |= TagBits.HasMappingIncompatibility;
					continue; // no real type checking needed.
				}
				TypeBinding baseLeaf = baseParam.leafComponentType();
				TypeBinding roleLeaf = roleParam.leafComponentType();
				ASTNode location = (methodSpec.hasSignature) ? (ASTNode)methodSpec.arguments[j] : methodSpec;
				boolean compatibilityViaBaseAnchor= false;
				boolean hasReportedError= false;
				boolean isTypeVariable= false;
				try { // capture continue exits

					// unbound type variable matches everything:
					if (roleParam.isTypeVariable()) {
						TypeVariableBinding typeVariableBinding = (TypeVariableBinding)roleParam;
						if (typeVariableBinding.firstBound == null)
							continue;
						// use bound for type checking below, yet need not check two-way compatibility:
						isTypeVariable= true;
						roleLeaf= typeVariableBinding.firstBound.leafComponentType();
					}

					int dimensions = roleParam.dimensions();
					if (baseLeaf.isCompatibleWith(roleLeaf)) {
						this.roleMethodSpec.parameters[j]= roleParam;
						continue;
					}
					if (RoleTypeCreator.isCompatibleViaBaseAnchor(this.scope, baseLeaf, roleLeaf, TokenNameBINDIN))
					{
						this.roleMethodSpec.parameters[j]= roleParam;
						compatibilityViaBaseAnchor= true;
						continue;
					}

					TypeBinding roleToLiftTo = null;
					if (isReplaceCallin()) {
						TypeBinding roleSideType = roleLeaf;
						if (roleSideType.isRole()) {
							ReferenceBinding roleRef = (ReferenceBinding)roleSideType;
							roleRef = (ReferenceBinding)TeamModel.strengthenRoleType(this.scope.enclosingReceiverType(), roleRef);
							if (roleRef.baseclass() == baseLeaf) {
								if (dimensions > 0) {
									if (roleRef instanceof DependentTypeBinding)
										roleToLiftTo = ((DependentTypeBinding)roleRef).getArrayType(dimensions);
									else
										roleToLiftTo = this.scope.createArrayType(roleRef, dimensions); // FIXME(SH): is this OK?
								} else {
									roleToLiftTo = roleRef;
								}
							}
						}
					} else {
						// this uses OTJLD 2.3.3(a) adaptation which is not reversible, ie., not usable for replace:
						roleToLiftTo = TeamModel.getRoleToLiftTo(this.scope, baseParam, roleParam, false, location);
					}
					if (roleToLiftTo != null)
					{
						// success by translation
						methodSpec.argNeedsTranslation[j] = true;
						this.roleMethodSpec.argNeedsTranslation[j] = true;
						this.roleMethodSpec.parameters[j] = roleToLiftTo; // this applies to all bindings
						continue;
					}
					// check auto(un)boxing:
					if (this.scope.isBoxingCompatibleWith(baseLeaf, roleLeaf))
						continue;

					if (roleParam instanceof ReferenceBinding)
					{
						ReferenceBinding roleRef = (ReferenceBinding)roleParam;
						if (   roleRef.isRole()
							&& roleRef.baseclass() != null)
						{
							this.scope.problemReporter().typeMismatchErrorPotentialLift(
									location, baseParam, roleParam, roleRef.baseclass());
							hasReportedError= true;
							continue;
						}
					}
					// no compatibility detected:
					this.scope.problemReporter().incompatibleMappedArgument(
							baseParam, roleParam, this.roleMethodSpec, j, /*callout*/false);
					hasReportedError= true;
				} finally {
					if (hasReportedError)
						this.binding.tagBits |= TagBits.HasMappingIncompatibility;
					// regardless of continue, check this last because it is the least precise message:
					if (!hasReportedError && baseLeaf.isCompatibleWith(roleLeaf)) {
						if (isReplaceCallin() && !isTypeVariable) {
							boolean twowayCompatible =  compatibilityViaBaseAnchor
								? RoleTypeCreator.isCompatibleViaBaseAnchor(this.scope, baseLeaf, roleLeaf, TokenNameBINDOUT)
								: roleLeaf.isCompatibleWith(baseLeaf);
							if (!twowayCompatible) {
								// requires two-way compatibility (see additional paragraph in 4.5(d))
								this.scope.problemReporter().typesNotTwowayCompatibleInReplace(baseParam, roleParam, location, j);
							}
						}
					}
				}
			}
		}
		return true; // unused in the callin case
	}
	protected void checkReturnCompatibility(MethodSpec methodSpec)
	{
		Config.requireTypeAdjustment(); // reset flags
		// Note(SH): non-replace mappings have no return-dataflow (except for explicitly mapping 'result' in after)
		if (isReplaceCallin()) // return for after/before is ignored.
			checkResultForReplace(methodSpec);
	}

	@Override
	public boolean checkVisibility(MethodSpec spec, ReferenceBinding baseType)
	{
		if (!super.checkVisibility(spec, baseType))
			return false;
		if (isReplaceCallin()) {
			// create a faked invocationSite:
			MessageSend anticipatedBaseCall = new MessageSend();
			anticipatedBaseCall.receiver = new SingleNameReference("<fake>".toCharArray(), 0); //$NON-NLS-1$
			anticipatedBaseCall.receiver.resolvedType =
				     anticipatedBaseCall.actualReceiverType = baseType;
			if (   !spec.resolvedMethod.canBeSeenBy(baseType, anticipatedBaseCall, this.scope.classScope())
				&& (spec.resolvedMethod.modifiers & ClassFileConstants.AccProtected) == 0) // protected is not a warning
				this.scope.problemReporter().callinDecapsulation(spec, this.scope);
		}
		return true;
	}

	/** Check whether the baseSpec has a result compatible via replace. */
	public void checkResultForReplace(MethodSpec baseSpec) {
		boolean typeIdentityRequired= true; // default unless return is type variable
		// covariant return requires a fresh type parameter for the role's return type:
		if (baseSpec.covariantReturn && this.roleMethodSpec.returnType != null) {
			TypeBinding resolvedRoleReturn= this.roleMethodSpec.returnType.resolvedType;
			if (resolvedRoleReturn != null) {
				if (!resolvedRoleReturn.isTypeVariable()) {
					this.scope.problemReporter().covariantReturnRequiresTypeParameter(this.roleMethodSpec.returnType);
					this.binding.tagBits |= TagBits.HasMappingIncompatibility;
				} else  {
					// is the type parameter "fresh"?
					for (Argument arg : this.roleMethodSpec.arguments) {
						if (typeUsesTypeVariable(arg.type.resolvedType.leafComponentType(), resolvedRoleReturn)) {
							this.scope.problemReporter().duplicateUseOfTypeVariableInCallin(this.roleMethodSpec.returnType, resolvedRoleReturn);
							this.binding.tagBits |= TagBits.HasMappingIncompatibility;
							break;
						}
					}
				}				
			}
		}
		TypeVariableBinding returnVariable= MethodModel.checkedGetReturnTypeVariable(this.roleMethodSpec.resolvedMethod);
		if (returnVariable != null) {
			// unbounded type variable always matches:
			if (returnVariable.firstBound  == null)
				return;
			// in case of type variable only one-way compatibility is needed even for replace:
			typeIdentityRequired= false;
		}

		// now go for the actual type checking:
		TypeBinding baseReturn = baseSpec.resolvedMethod.returnType;
		TypeBinding roleReturn = MethodModel.getReturnType(this.roleMethodSpec.resolvedMethod);
		TypeBinding roleReturnLeaf = roleReturn != null ? roleReturn.leafComponentType() : null;
		if (   roleReturnLeaf instanceof ReferenceBinding
			&& ((ReferenceBinding)roleReturnLeaf).isRole())
		{
			// strengthen:
			roleReturnLeaf = TeamModel.strengthenRoleType(this.scope.enclosingSourceType(), roleReturnLeaf);
			if (roleReturnLeaf == null) {  // FIXME(SH): testcase and better handling
				String roleReturnName = roleReturn != null ? new String(roleReturn.readableName()) : "null return type"; //$NON-NLS-1$
				throw new InternalCompilerError("role strengthening for "+roleReturnName+" -> null"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// bound roles use their topmost bound super:
			if (((ReferenceBinding)roleReturnLeaf).baseclass() != null)
				roleReturnLeaf = RoleModel.getTopmostBoundRole(this.scope, (ReferenceBinding)roleReturnLeaf);

			// need the RTB:
			if (!DependentTypeBinding.isDependentType(roleReturnLeaf))
				roleReturnLeaf = RoleTypeCreator.maybeWrapUnqualifiedRoleType(roleReturnLeaf,this.scope.enclosingSourceType());

			// array?
			int dims = roleReturn != null ? roleReturn.dimensions() : 0;
			if (dims == 0) {
				roleReturn = roleReturnLeaf;
				this.realRoleReturn = roleReturnLeaf;
			} else {
				roleReturn = ((DependentTypeBinding)roleReturnLeaf).getArrayType(dims);
				this.realRoleReturn = ((DependentTypeBinding)roleReturnLeaf).getArrayType(dims);
			}
		}
		if (   baseReturn == null
		    || baseReturn == TypeBinding.VOID)
		{
			// OTJLD 4.4(b): "A callin method bound with replace
			//                to a base method returning void
			//                must not declare a non-void result."
			if (!(   roleReturn == null
				  || roleReturn == TypeBinding.VOID))
			{
				this.scope.problemReporter().callinIllegalRoleReturnReturn(
						baseSpec, this.roleMethodSpec);
				this.binding.tagBits |= TagBits.HasMappingIncompatibility;
			}
		} else {
			if (   roleReturn == null
				|| roleReturn == TypeBinding.VOID)
			{
				this.baseMethodNeedingResultFromBasecall = baseSpec;
				// will be reported in checkBaseResult().
				return;
			}

			TypeBinding baseLeaf = baseReturn.leafComponentType();
			if (baseLeaf instanceof DependentTypeBinding) {
				// instantiate relative to Role._OT$base:
				ReferenceBinding enclosingRole = this.scope.enclosingSourceType();
				FieldBinding baseField = enclosingRole.getField(IOTConstants._OT_BASE, true);
				if (baseField != null && baseField.isValidBinding())
					baseReturn = baseField.getRoleTypeBinding((ReferenceBinding)baseLeaf, baseReturn.dimensions());
			}

			// check auto(un)boxing:
			if (this.scope.isBoxingCompatibleWith(roleReturn, baseReturn))
				return;

			Config oldConfig = Config.createOrResetConfig(this);
			try {
				if (!roleReturn.isCompatibleWith(baseReturn)) {
					if (typeIdentityRequired) {
						this.scope.problemReporter().callinIncompatibleReturnType(
								baseSpec, this.roleMethodSpec);
						this.binding.tagBits |= TagBits.HasMappingIncompatibility;
						return;
					}
					// else we still needed the lowering test
				}
				// callin replace requires two way compatibility:
				baseSpec.returnNeedsTranslation = Config.getLoweringRequired();

			} finally {
				Config.removeOrRestore(oldConfig, this);
			}
			// from now on don't bother with arrays any more (dimensions have been checked):
			roleReturn = roleReturn.leafComponentType();
			baseReturn = baseReturn.leafComponentType();
			TypeBinding translatedReturn = baseSpec.returnNeedsTranslation ?
					((ReferenceBinding)roleReturn).baseclass() :
					roleReturn;
			if (translatedReturn.isTypeVariable()) {
				TypeBinding firstBound = ((TypeVariableBinding)translatedReturn).firstBound;
				if (firstBound != null)
					translatedReturn= firstBound;
			}
			if (!baseReturn.isCompatibleWith(translatedReturn)) {
				this.scope.problemReporter().callinIncompatibleReturnTypeBaseCall(
						baseSpec, this.roleMethodSpec);
				this.binding.tagBits |= TagBits.HasMappingIncompatibility;
			}
		}
	}

	/** Check OTJLD 4.4(b) "Callin parameter mapping / Restrictions for callin replace bindings" */
	public void checkResultMapping() {
		// for replace callins, a "result" mapping is not allowed,
		// unless an expected result is otherwise missing.

		for (MethodSpec baseSpec : this.baseMethodSpecs) {
			for (int i = 0; i < this.mappings.length; i++) {
				if (CharOperation.equals(this.mappings[i].ident.token, IOTConstants.RESULT))
				{
					this.isResultMapped = true;
					// OTJLD 4.4(b): "If the base method declares a result, then ...
					if (baseSpec.resolvedType() != TypeBinding.VOID) {
					//                * if the role method also declares a result,
						if (this.roleMethodSpec.resolvedType() != TypeBinding.VOID) {
							Expression resultExpr = this.mappings[i].expression;
					//                  => result must be mapped to itself
							if (! (resultExpr instanceof ResultReference))  {
								this.scope.problemReporter().nonResultExpressionInReplaceResult(resultExpr);
								this.binding.tagBits |= TagBits.HasMappingIncompatibility;
							}
						} // no else because:
					//                * if the role method does not declare a result,
				    //                  an arbitrary expression may be mapped to result
			    	} else {
			    		this.scope.problemReporter().resultMappingForVoidMethod(this, baseSpec, this.mappings[i]);
						this.binding.tagBits |= TagBits.HasMappingIncompatibility;
			    	}
				}
			}
		}
	}
	
	private boolean typeUsesTypeVariable(TypeBinding type, TypeBinding variable) {
		if (type.leafComponentType() == variable)
			return true;
		for (TypeVariableBinding t : type.typeVariables())
			if (typeUsesTypeVariable(t, variable))
				return true;
		if (type.isTypeVariable()) {
			if (typeUsesTypeVariable(((ReferenceBinding)type).superclass(), variable))
				return true;
			for (TypeBinding superIfc : ((ReferenceBinding)type).superInterfaces())
				if (typeUsesTypeVariable(superIfc, variable))
					return true;
		}
		return false;
	}

	protected void checkResult(MethodSpec baseSpec) {
		if (isReplaceCallin())
			checkResultForReplace(baseSpec);
	}

	protected void checkThrownExceptions(MethodSpec baseSpec) {
		checkThrownExceptions(this.roleMethodSpec.resolvedMethod, baseSpec.resolvedMethod);
		// transfer all exceptions of base methods to the role method (model):
		ReferenceBinding[] baseExceptions = baseSpec.resolvedMethod.thrownExceptions;
		if (baseExceptions != null && baseExceptions.length > 0) {
			MethodModel roleMethodModel = MethodModel.getModel(this.roleMethodSpec.resolvedMethod);
			roleMethodModel.addBaseExceptions(baseExceptions);
		}
	}

	/** Check details that require analyseCode() of methods to be finished: */
	public void analyseDetails(TypeDeclaration roleClass) 
	{		
		// check validity of base-super call
		if (   this.roleMethodSpec.isValid()
			&& MethodModel.hasCallinFlag(this.roleMethodSpec.resolvedMethod, CALLIN_FLAG_BASE_SUPER_CALL)) 
		{
			for (int i = 0; i < this.baseMethodSpecs.length; i++) {
				MethodBinding baseMethod = this.baseMethodSpecs[i].resolvedMethod;
				if (baseMethod != null) {
					if (!MethodModel.isOverriding(baseMethod, this.scope.compilationUnitScope()))
						this.scope.problemReporter().baseSuperCallToNonOverriding(this.baseMethodSpecs[i], this.roleMethodSpec);
					else
						// create the special access attribute that signals the need to handle super access:
						roleClass.getRoleModel().addMethodSuperAccess(baseMethod);
				}
			}
		}
		// check whether a base result is missing
		if (   this.baseMethodNeedingResultFromBasecall != null
			&& !this.isResultMapped) 
		{
			if (MethodModel.hasCallinFlag(this.roleMethodSpec.resolvedMethod, CALLIN_FLAG_DEFINITELY_MISSING_BASECALL))
			{
				this.scope.problemReporter().callinMappingMissingResult(
						this, this.baseMethodNeedingResultFromBasecall);
				this.binding.tagBits |= TagBits.HasMappingIncompatibility;
			}
			else if (MethodModel.hasCallinFlag(this.roleMethodSpec.resolvedMethod, CALLIN_FLAG_POTENTIALLY_MISSING_BASECALL))
			{
				this.scope.problemReporter().fragileCallinMapping(
						this, this.baseMethodNeedingResultFromBasecall);
			}
		}
	}

	public boolean isCallin()
	{
		return true;
	}

	public boolean isReplaceCallin() {
		return this.callinModifier == TokenNamereplace;
	}
	public boolean isStaticReplace() {
		return isReplaceCallin() && this.roleMethodSpec.resolvedMethod.isStatic();
	}

	public boolean isCallout()
	{
		return false;
	}

	public char[] getCallinModifier() {
        switch (this.callinModifier)
        {
        case TokenNamereplace :
            return IOTConstants.NAME_REPLACE;
        case TokenNameafter :
            return IOTConstants.NAME_AFTER;
        case TokenNamebefore :
            return IOTConstants.NAME_BEFORE;
        }
        return null;
    }

	/**
     * Returns whether at least one of the bound base method is captured including
     * overrides with covariant return types (marked as "RT+ bm()")
     */
	public boolean hasCovariantReturn() {
		for (MethodSpec spec : this.baseMethodSpecs)
			if (spec.covariantReturn)
				return true;
		return false;
	}
	/**
	 * For callins the role method is the implemented method to be invoked.
	 */
	public MethodSpec getImplementationMethodSpec() {
		return this.roleMethodSpec;
	}

	/**
	 * Get the expression that is being mapped to "result"
	 * Precondition: parameter mappings are present.
	 *
	 * @return expression (in this expression "result" should be a legal name).
	 */
	public Expression getResultExpression(MethodSpec baseMethodSpec, boolean needBoxing, AstGenerator gen)
	{
		if (baseMethodSpec.resolvedType() == TypeBinding.VOID)
			// binding non-void to void: just return "result" which will be ignored any way.
			return new SingleNameReference(
    				IOTConstants.RESULT,
    				(((long)this.roleMethodSpec.sourceStart)<<32)+ this.roleMethodSpec.sourceEnd);
		Expression resultExpr = null;
		for (int i = 0; i < this.mappings.length; i++) {
			if (this.mappings[i].isUsedFor(baseMethodSpec)) // already used mapping?
				continue;
			if (CharOperation.equals(this.mappings[i].ident.token, IOTConstants.RESULT)) {
				if (resultExpr != null) {
					this.scope.problemReporter().duplicateParamMapping(this.mappings[i], IOTConstants.RESULT, /*isCallout*/false);
					this.binding.tagBits |= TagBits.HasMappingIncompatibility;
				} else {
					resultExpr = this.mappings[i].expression;
				}
			}
		}
		if (resultExpr != null) {
			// check undefined 'result' expression:
        	if (   this.roleMethodSpec.resolvedType() == TypeBinding.VOID
            	&& (resultExpr instanceof ResultReference))
        	{
           		this.scope.problemReporter().resultNotDefinedForVoidMethod(resultExpr, this.roleMethodSpec.selector, false/*callout*/);
    			this.binding.tagBits |= TagBits.HasMappingIncompatibility;
        	}
		}
		if (   resultExpr != null
			&& !this.isResultMapped // in param-mapping role-side "result" already has boxed type
			&& needBoxing)
			resultExpr = gen.createBoxing(resultExpr, (BaseTypeBinding)baseMethodSpec.resolvedType());
		return resultExpr;
	}

	@Override
	Integer analyzeArgForReplace(MethodSpec sourceMethodSpec, int implIdx, Expression mappedArgExpr)
	{
		if (!isReplaceCallin()) return null;
		Expression currentExpression = mappedArgExpr;

		// the reverse of a cast expression is implicit widening, thus allow these
		if (currentExpression instanceof CastExpression) {
			CastExpression castExpression = (CastExpression)currentExpression;
			if (castExpression.type instanceof SingleNameReference) {
				// special case: SingleNameReference does not support base-import scope,
				// replace with SingleTypeReference:
				SingleNameReference type = (SingleNameReference)castExpression.type;
				AstGenerator gen = new AstGenerator(type.sourceStart, type.sourceEnd);
				castExpression.type = gen.baseTypeReference(type.token);
			}
			currentExpression = (castExpression).expression;
		}

		if (currentExpression instanceof SingleNameReference) {
			SingleNameReference arg = (SingleNameReference)currentExpression;
			return recordPosition(implIdx, arg.token, sourceMethodSpec);
		} else {
			SingleNameReference match = findBaseArgName(
						currentExpression, this.scope, sourceMethodSpec.arguments);
			if (match != null && this.scope != null) {
				this.scope.problemReporter()
					.baseArgInNonSimpleExpression(match);
				this.binding.tagBits |= TagBits.HasMappingIncompatibility;
			}
		}
		return null;
	}

	/** Get incoming arguments that are not used by the role side. */
	public int[] getUnmappedBasePositions(MethodSpec baseSpec) {
		int baseArgCount = baseSpec.resolvedParameters().length; // role-as-base enhancement filtered out
		int[] result = new int[baseArgCount];
		int idx = 0;
		for (int i=0; i<baseArgCount; i++) {
			if (!isMapped(i))
				result[idx++]=i;
		}
		if (idx < baseArgCount) {
			System.arraycopy(result, 0, result = new int[idx], 0, idx);
		}
		return result;
	}
	private boolean isMapped(int pos) {
		if (this.positions == null)
			return pos < this.roleMethodSpec.resolvedParameters().length;

		for (int i=0; i<this.positions.length; i++)
			if (this.positions[i] == pos+1)
				return true;
		return false;
	}

    public void traverse(ASTVisitor visitor, ClassScope classScope)
	{
		if(visitor.visit(this, classScope))
		{
			this.roleMethodSpec.traverse(visitor,this.scope);
			for (int idx = 0; idx < this.baseMethodSpecs.length; idx++)
			{
				this.baseMethodSpecs[idx].traverse(visitor,this.scope);
			}
            if (this.mappings != null)
            {
                for (int idy = 0; idy < this.mappings.length; idy++)
                {
                    ParameterMapping mapping = this.mappings[idy];
                        mapping.traverse(visitor,this.scope);
                }
            }
		}
		visitor.endVisit(this, classScope);
	}

    public String callinModifier() {
        return callinModifier(this.callinModifier);
    }

    public static String callinModifier(int callinModifier) {
        switch (callinModifier)
        {
            case TokenNamereplace :
            	return "replace"; //$NON-NLS-1$
            case TokenNameafter :
            	return "after"; //$NON-NLS-1$
            case TokenNamebefore :
            	return "before"; //$NON-NLS-1$
        }
        return "<unknown>"; //$NON-NLS-1$
    }

    public StringBuffer print(int indent, StringBuffer output)
    {
        printIndent(indent,output);

        if (this.name != null) {
        	output.append(this.name);
        	output.append(":\n");              //$NON-NLS-1$
        	printIndent(++indent,output);
        }
        this.roleMethodSpec.print(0,output);
        output.append(" <- ");                 //$NON-NLS-1$
        output.append(callinModifier()+" ");   //$NON-NLS-1$
        int length = this.baseMethodSpecs.length;
        if (length > 1)
        	output.append(" { ");              //$NON-NLS-1$
        for (int t = 0; t < length; t++)
        {
        	this.baseMethodSpecs[t].print(0,output);
            if (t < length - 1)
            	output.append(", ");           //$NON-NLS-1$
        }
        if (length > 1)
        	output.append(" } "); //$NON-NLS-1$
        if (this.predicate != null)
        {
        	output.append('\n');
        	printIndent(indent+1, output);
        	if (this.predicate.isBasePredicate)
        		output.append("base "); //$NON-NLS-1$
        	output.append("when ");     //$NON-NLS-1$
        	if (this.predicate.returnStatement != null)
        		this.predicate.returnStatement.expression.printExpression(indent, output);
        	else
        		output.append("<null expression>"); //$NON-NLS-1$
        }
        if (this.mappings != null)
        {
        	output.append(" with { ");  //$NON-NLS-1$
            length = this.mappings.length;
            for (int t = 0; t < length; t++)
            {
            	this.mappings[t].print(indent,output);
                if (t < length - 1)
                	output.append(", ");//$NON-NLS-1$
            }
            output.append(" } ");       //$NON-NLS-1$
        } else
        {
        	output.append(";");         //$NON-NLS-1$
        }
        return output;
    }

    public StringBuffer printShort(int indent, StringBuffer output, MethodSpec baseMethodSpec)
    {
    	printIndent(indent,output);
        this.roleMethodSpec.print(0,output);
        output.append(getCallinModifier());
        baseMethodSpec.print(0,output);
        return output;
    }

	/**
	 * @param baseMethodSpec
	 * @param wrapperMethod
	 */
	public void setWrapper(MethodSpec baseMethodSpec, MethodDeclaration wrapperMethod) {
		if (this.wrappers == null)
			this.wrappers = new MethodDeclaration[this.baseMethodSpecs.length];
		for (int i = 0; i < this.baseMethodSpecs.length; i++) {
			if (this.baseMethodSpecs[i] == baseMethodSpec) {
				this.wrappers[i] = wrapperMethod;
				return;
			}
		}
		this.scope.problemReporter().abortDueToInternalError("trying to set wrapper for non-existing baseMethodSpec"+baseMethodSpec); //$NON-NLS-1$
	}

	/**
	 * Answer the wrapper through which a given base method will invoke this binding.
	 */
	public MethodDeclaration getWrapper(MethodSpec baseMethodSpec) {
		for (int i = 0; i < this.baseMethodSpecs.length; i++) {
			if (this.baseMethodSpecs[i] == baseMethodSpec) {
				return this.wrappers[i];
			}
		}
		return null;
	}

	public boolean hasName() {
		return this.name != null && this.name[0] != '<';
	}
}