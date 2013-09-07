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
 * $Id: CalloutMappingDeclaration.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameBINDOUT;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameCALLOUT_OVERRIDE;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameget;


/**
 * NEW for OTDT.
 *
 * Main responsibility: type checking
 *
 * @author Markus Witte
 * @version $Id: CalloutMappingDeclaration.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class CalloutMappingDeclaration extends AbstractMethodMappingDeclaration
{
	public int calloutKind;
	public int declaredModifiers = 0;
	public int modifiersSourceStart; // start of declared modifiers (visibility)
	public MethodSpec baseMethodSpec;


	// might want to make this configurable via compiler options, but
	// note that currently this flag affects only bindings with signatures!
	private static final boolean ALLOW_DECAPSULATION = true;

	public CalloutMappingDeclaration(CompilationResult compilationResult)
	{
		super(compilationResult);
	}

	public void setCalloutKind(boolean isOverride) {
		this.calloutKind = isOverride ?
								TokenNameCALLOUT_OVERRIDE :
								TokenNameBINDOUT;

	}


	/** add a base method spec, iff none has been given yet. */
	@Override
	public void checkAddBasemethodSpec(MethodSpec baseSpec) {
		if (this.baseMethodSpec == null)
			this.baseMethodSpec = baseSpec;
	}

	/**
	 * Check all parameters in methodSpec against the resolved role method.
	 * Also record which parameters (including result) need translation (lifting/lowering).
	 *
	 * Pre: not called if parameter mappings are present.
     *
	 * @param methodSpec maybe null, signaling we are just inferring an implicit callout.
	 * @param roleParams
	 * @param baseParams
	 */
	public boolean internalCheckParametersCompatibility(
			MethodSpec methodSpec,
			TypeBinding[] roleParams,
			TypeBinding[] baseParams)
	{
		if (roleParams.length < baseParams.length) {
			if (methodSpec != null) {// don't report in infer-mode
				this.scope.problemReporter().tooFewArgumentsInMethodMapping(this.roleMethodSpec, methodSpec, true/*callout*/);
				this.binding.tagBits |= TagBits.HasMappingIncompatibility;
			}
			return false;
		} else {
			for (int j = 0; j < baseParams.length; j++) {
				Config oldConfig = Config.createOrResetConfig(this);
				try {
					TypeBinding roleParam = roleParams[j];
					if (roleParam instanceof UnresolvedReferenceBinding)
						roleParam = ((UnresolvedReferenceBinding) roleParam).resolve(this.scope.environment(), false);
					TypeBinding roleParamLeaf = roleParam.leafComponentType();
					TypeBinding roleBaseLeaf = null;
					if (   roleParamLeaf instanceof ReferenceBinding
						&& ((ReferenceBinding)roleParamLeaf).isRole())
					{
						roleParam = TeamModel.strengthenRoleType(
								this.scope.enclosingSourceType(), roleParam);
						roleBaseLeaf = ((ReferenceBinding)roleParam.leafComponentType()).baseclass();
					}
					TypeBinding baseParam = baseParams[j];
					if (!roleParam.isCompatibleWith(baseParam)) {
						if (!RoleTypeCreator.isCompatibleViaBaseAnchor(this.scope, baseParam, roleParam, TokenNameBINDOUT))
						{
							// try auto(un)boxing:
							if (this.scope.isBoxingCompatibleWith(roleParam, baseParam))
								continue; // success through (un)boxing

							if (methodSpec == null)
								return false; // when inferring one error suffices to abort.

							if (methodSpec.hasSignature && roleBaseLeaf != null)
								this.scope.problemReporter().typeMismatchErrorPotentialLower(
										methodSpec.arguments[j], roleParam, baseParam, roleBaseLeaf);
							else
								this.scope.problemReporter().incompatibleMappedArgument(
										roleParam, baseParam, methodSpec, j, /*callout*/true);
							this.binding.tagBits |= TagBits.HasMappingIncompatibility;
						}
					} else {
						this.roleMethodSpec.argNeedsTranslation[j] = Config.getLoweringRequired();
					}
				} finally {
					Config.removeOrRestore(oldConfig, this);
				}
			}
		}
		return true;
	}


	public void checkReturnCompatibility(MethodSpec methodSpec)
	{
		TypeBinding roleReturn = this.roleMethodSpec.resolvedType();
		TypeBinding baseReturn = methodSpec.resolvedType();
		if (roleReturn != null) {
			if (baseReturn == null) {
				this.scope.problemReporter().returnRequiredInMethodMapping(
						methodSpec, roleReturn, true/*callout*/);
			} else {
				// build a receiver (_OT$base):
				AstGenerator gen = new AstGenerator(methodSpec.sourceStart, methodSpec.sourceEnd);
				SingleNameReference baseRef = gen.singleNameReference(IOTConstants._OT_BASE);
				baseRef.binding = RoleTypeCreator.findResolvedVariable(this.scope.classScope(), IOTConstants._OT_BASE);
				// maybe wrap return type relative to _OT$base
				baseReturn = RoleTypeCreator.maybeWrapQualifiedRoleType(
						this.scope,
						baseRef,
						baseReturn,
						methodSpec.returnType);
				if (this.roleMethodSpec.returnType == null)
					this.roleMethodSpec.returnType = gen.typeReference(roleReturn);
				if (   roleReturn == TypeBinding.VOID
				    || baseReturn.isCompatibleWith(roleReturn))
				{
					this.roleMethodSpec.returnType.resolvedType = roleReturn;
				} else {
					TypeBinding roleToLiftTo = TeamModel.getRoleToLiftTo(this.scope, baseReturn, roleReturn, false, methodSpec);
					if (roleToLiftTo != null)
					{
						// success by translation
						this.roleMethodSpec.returnNeedsTranslation = true;
						this.roleMethodSpec.returnType.resolvedType = roleToLiftTo; // instantiated.
						return; // if successful we're done.
					}
					// try auto(un)boxing:
					if (this.scope.isBoxingCompatibleWith(baseReturn, roleReturn)) {
						this.roleMethodSpec.returnType.resolvedType = roleReturn;
						return; // success by (un)boxing
					}

					this.scope.problemReporter().calloutIncompatibleReturnType(
							this.roleMethodSpec, methodSpec);
					this.binding.tagBits |= TagBits.HasMappingIncompatibility;
				}
			}
		}
	}


	/**
	 * Only one check needs to be performed for callout-to-field:
	 * @param fieldSpec
	 */
	protected void checkTypeCompatibility(FieldAccessSpec fieldSpec)
	{
		// make sure roleMethodSpec has a returnType, because that is going to be updated below:
		if (this.roleMethodSpec.returnType == null) {
			AstGenerator gen = new AstGenerator(this.roleMethodSpec.sourceStart, this.roleMethodSpec.sourceEnd);
			this.roleMethodSpec.returnType = gen.typeReference(this.roleMethodSpec.resolvedType());
		}

		TypeBinding requiredType = null;
		TypeBinding providedType = null;

		if (fieldSpec.calloutModifier == TokenNameget) {
			requiredType = this.roleMethodSpec.resolvedType();
			providedType = fieldSpec.resolvedType();
			if (providedType.isCompatibleWith(requiredType)) {
				if (this.roleMethodSpec.returnType.resolvedType == null)
                    this.roleMethodSpec.returnType.resolvedType = requiredType; // need a valid type here
				return; // OK => done
			} else {
				TypeBinding roleToLiftTo = TeamModel.getRoleToLiftTo(this.scope, providedType, requiredType, false, fieldSpec);
				if (roleToLiftTo != null)
				{
					// success by translation
					this.roleMethodSpec.returnNeedsTranslation = true;
					this.roleMethodSpec.returnType.resolvedType = roleToLiftTo; // instantiated.
					return; // OK => done.
				}
				if (requiredType == TypeBinding.VOID) {
					this.scope.problemReporter().fieldAccessHasNoEffect(this.roleMethodSpec, fieldSpec);
					this.roleMethodSpec.returnType.resolvedType = requiredType; // keep going..
					return; // warned
				}
				if (this.roleMethodSpec.returnType.resolvedType == null) {
					// https://bugs.eclipse.org/387236
					// if returnType was added late (above) and if type mismatch exists, we still need a resolved type here:
					this.roleMethodSpec.returnType.resolve(this.scope);
				}
			}
		} else { // 'set'
			if (this.roleMethodSpec.resolvedMethod.returnType != TypeBinding.VOID) {
				this.scope.problemReporter().calloutSetCantReturn(this.roleMethodSpec);
				this.binding.tagBits |= TagBits.HasMappingIncompatibility;
			}
			this.roleMethodSpec.returnType.resolvedType = TypeBinding.VOID;
			TypeBinding[] params = this.roleMethodSpec.resolvedMethod.parameters;
			if (params == null || params.length == 0) {
				this.scope.problemReporter().calloutToFieldMissingParameter(this.roleMethodSpec, fieldSpec);
				this.binding.tagBits |= TagBits.HasMappingIncompatibility;
				return; // don't report more problems
			}
			providedType = params[0];
			requiredType = fieldSpec.resolvedType();
			if (providedType.isCompatibleWith(requiredType))
				return;
		}
		// fall through in any case of incompatibility:
		this.scope.problemReporter().calloutIncompatibleFieldType(
				this.roleMethodSpec, fieldSpec, requiredType, providedType);
		this.binding.tagBits |= TagBits.HasMappingIncompatibility;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractMethodMappingDeclaration#checkModifiers(org.eclipse.jdt.internal.compiler.lookup.CallinCalloutScope)
	 */
	@Override
	protected void checkModifiers(boolean haveBaseMethods, ReferenceBinding baseType) {
		boolean roleHasImplementation = false;
		MethodBinding roleMethod = this.roleMethodSpec.resolvedMethod;
		if (!roleMethod.isValidBinding())
			return;
		// update modifiers after tsuper has generated callout methods (perhaps giving implementation to abstract decl)
		if (roleMethod.isAbstract() && roleMethod.copyInheritanceSrc != null && !roleMethod.copyInheritanceSrc.isAbstract())
			roleMethod.modifiers &= ~ClassFileConstants.AccAbstract;
		roleHasImplementation = !roleMethod.isAbstract();

		if (roleHasImplementation != isCalloutOverride())
		{
			if (roleHasImplementation)
			{
				if (isCalloutMethod(roleMethod)) {
					if (   roleMethod.declaringClass != this.scope.enclosingSourceType()
						|| roleMethod.copyInheritanceSrc != null)  // "local" callouts (not copied) are treated in
					{											   // MethodMappingResolver.checkForDuplicateMethodMappings()
						this.scope.problemReporter().regularCalloutOverridesCallout(this, roleMethod);
					}
				} else {
					this.scope.problemReporter().regularCalloutOverrides(this);
				}
			}
			else // isCalloutOverride() but not really overriding
			{
				this.scope.problemReporter().abstractMethodBoundAsOverrideCallout(this);
				AbstractMethodDeclaration roleMethodDeclaration = roleMethod.sourceMethod();
				if(roleMethodDeclaration != null)
				{
					roleMethodDeclaration.ignoreFurtherInvestigation = true;
					this.ignoreFurtherInvestigation = true;
				}

			}
		}
		if (roleMethod.isCallin()) {
			this.scope.problemReporter().calloutBindingCallin(this.roleMethodSpec);
		}
		if (hasErrors()) {
			// unsuccessful attempt to implement role method as callout,
			// mark the method as erroneous:
			if (this.roleMethodSpec.resolvedMethod != null && this.roleMethodSpec.resolvedMethod.isAbstract())
			{
				AbstractMethodDeclaration methodDecl = this.roleMethodSpec.resolvedMethod.sourceMethod();
				if (methodDecl != null)
					methodDecl.tagAsHavingErrors(); // prevent abstract-error
			}
		}
	}
	private boolean isCalloutMethod(MethodBinding method) {
		if (method.copyInheritanceSrc != null)
			method = method.copyInheritanceSrc;
		if (method.declaringClass.isBinaryBinding()) {
			// fault in types adds callinCallouts for binary types (late attribute)
			Dependencies.ensureBindingState(method.declaringClass, ITranslationStates.STATE_FAULT_IN_TYPES);
		}
		CallinCalloutBinding[] bindings = method.declaringClass.callinCallouts;
		if (bindings != null)
		{
			for (int i = 0; i < bindings.length; i++) {
				if (bindings[i]._roleMethodBinding == method)
					return true;
			}
		}
		return false;
	}

	protected void checkThrownExceptions(MethodSpec baseSpec) {
		if (   this.hasSignature
			&& !this.roleMethodSpec.resolvedMethod.isValidBinding()
			&& this.roleMethodSpec.problemId() == ProblemReasons.NotFound)
			return; // not checking for shorthand declarations

		checkThrownExceptions(baseSpec.resolvedMethod, this.roleMethodSpec.resolvedMethod);
	}

	public StringBuffer print(int indent, StringBuffer output)
	{
		printIndent(indent,output);
		if (this.declaredModifiers != 0)
			printModifiers(this.declaredModifiers, output);
		printShort(0,output);
		if(this.mappings!=null)
		{
			output.append(" with {\n"); //$NON-NLS-1$
			int length = this.mappings.length;
			for(int t=0;t<length;t++)
			{
				printIndent(indent+1,output);
                this.mappings[t].print(0,output);
				if(t<length-1)
					output.append(",\n"); //$NON-NLS-1$
				else
					output.append("\n"); //$NON-NLS-1$
			}
			printIndent(indent, output);
			output.append("}"); //$NON-NLS-1$
		} else {
			output.append(";"); //$NON-NLS-1$
		}

		return output;
	}

    public StringBuffer printShort(int indent, StringBuffer output)
    {
    	printIndent(indent,output);
        this.roleMethodSpec.print(0,output);
        if(this.calloutKind==TokenNameCALLOUT_OVERRIDE)
            output.append(" => "); //$NON-NLS-1$
        else
            output.append(" -> "); //$NON-NLS-1$

        if (this.baseMethodSpec != null)
        	this.baseMethodSpec.print(0,output);
        else
        	output.append(" <nullBaseMethod>"); //$NON-NLS-1$
        return output;
    }

	public void traverse(ASTVisitor visitor, ClassScope classScope)
	{
		if(visitor.visit(this, classScope))
		{
			if (this.roleMethodSpec != null)
				this.roleMethodSpec.traverse(visitor,this.scope);
			if (this.baseMethodSpec != null) // null happens on "void foo() -> ;"
				this.baseMethodSpec.traverse(visitor,this.scope);
			if(this.mappings != null)
			{
				for (int i = 0; i < this.mappings.length; i++)
				{
					ParameterMapping mapping = this.mappings[i];
					mapping.traverse(visitor,this.scope);
				}
			}
		}
		visitor.endVisit(this, classScope);
	}

	public boolean isCallin()
	{
		return false;
	}

	public boolean isCallout()
	{
		return true;
	}

	public boolean isCalloutOverride()
	{
		return this.calloutKind==TokenNameCALLOUT_OVERRIDE;
	}

    public boolean isCalloutToField()
    {
        return this.baseMethodSpec instanceof FieldAccessSpec;
    }

	public boolean canAccessInvisibleBase() {
		return ALLOW_DECAPSULATION;
	}
	/**
     * @return the start position of arrow token
     */
    public int arrowSourceStart() {
        return this.roleMethodSpec.sourceEnd + 1;
    }

    /**
     * @return the end position of arrow token
     */
    public int arrowSourceEnd() {
        return this.baseMethodSpec.sourceStart - 1;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractMethodMappingDeclaration#getImpementationMethodSpec()
	 */
	public MethodSpec getImplementationMethodSpec() {
		return this.baseMethodSpec;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractMethodMappingDeclaration#getBaseMethodSpecs()
	 */
	public MethodSpec[] getBaseMethodSpecs() {
		if (this.baseMethodSpec != null) {
		return new MethodSpec[] { this.baseMethodSpec };
		} else {
			tagAsHavingErrors(); // caused by a syntax error after the "->" or "=>" token.
			return new MethodSpec[0];
		}
	}

	/**
     * After a callout-rolemethod has been generated from a shorthand style callout,
     * a ProblemMethodBinding(NotFound) must be replaced with a valid method binding.
     *
	 * @param method
	 */
	public void updateRoleMethod(MethodBinding method) {
		this.roleMethodSpec.resolvedMethod = method;
		this.binding._roleMethodBinding = method;
	}
}