/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2016 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.mappings;

import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccAbstract;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccNative;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPrivate;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccProtected;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPublic;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccStatic;

import java.util.ArrayList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration.WrapperKind;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions.WeavingScheme;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.compiler.InferenceKind;
import org.eclipse.objectteams.otdt.core.compiler.OTNameUtils;
import org.eclipse.objectteams.otdt.core.compiler.Pair;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.FieldAccessSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec.ImplementationStrategy;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.ParameterMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.PotentialLowerExpression;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.PrivateRoleMethodCall;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.ResultReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.TThisBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.WeakenedTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.FieldModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.AbstractStatementsGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstClone;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.IProtectable;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * This Class transforms all callout-mappings (aka calloutbindings)
 * for a given role. The callout-mappings remain in the role as AST
 * nodes. The role-methods that match these mappings are transformed
 * similar to the following example:
 * void myCallout()
 * {
 *   __OT__base.myBaseMethod();
 * }
 *
 * Special tricks:
 * ===============
 * What:  Linking bestnames of arguments (relevant for anchored types in signatures).
 * Why:   Three signatures exist: roleMethodSpec, baseMethodSpec and wrapper,
 *        Each has its own scope (a temporary scope for baseMethodSpec) and
 *        set of locals.
 * Where: createCallout links roleMethodSpec and wrapper
 *        getArgument links baseMethodSpec and wrapper (if not parameter mappings are given).
 *        
 * What:  Callout to private members of a role-as-base require access via two bridges
 * Why:   private methods are not exposed in the ifc-part, implicit inheritance needs
 *        redirection via the team instance, fields need accessors anyway
 * Where: see anonymous subclass of MessageSend in transformCalloutMethodBody
 *
 * @author haebor
 */
public class CalloutImplementor extends MethodMappingImplementor
{
	private static final int INTERFACE = 0;
	private static final int CLASS = 1;


	/**
	 * Generates a callout method for every callout mapping in the given RoleModel.
	 * @return false if errors had been reported during transformation, true else.
	 */
	public static boolean transformCallouts(RoleModel role) {
		boolean success = true;
		TypeDeclaration roleDecl = role.getAst();
		if (roleDecl != null && !roleDecl.isPurelyCopied && !roleDecl.binding.isSynthInterface()) { // no source level bindings present any way
	    	boolean needMethodBodies = Dependencies.needMethodBodies(roleDecl) && !role.hasBaseclassProblem() && !role.isIgnoreFurtherInvestigation();
    		// synth interfaces have no callouts anyway ;-)
            CalloutImplementor calloutImplementor = new CalloutImplementor(role);
            success &= calloutImplementor.transform(needMethodBodies);
		}
		return success;
	}

	public CalloutImplementor(RoleModel role)
	{
		super(role);
	    this.bindingDirection = TerminalTokens.TokenNameBINDOUT;
	}

	private boolean transform(boolean needMethodBodies)
	{
		AbstractMethodMappingDeclaration[] methodMappings =
			this._role.getAst().callinCallouts;

		boolean result = true;

		if (methodMappings != null && methodMappings.length > 0)
		{
			for (int idx = 0; idx < methodMappings.length; idx++)
			{
				AbstractMethodMappingDeclaration methodMapping = methodMappings[idx];
				if(methodMapping.isCallout())
				{
					boolean createStatements= needMethodBodies && !methodMapping.hasErrors();
					result &= (createCallout((CalloutMappingDeclaration) methodMapping, createStatements, false/*inferred*/) != null);
				}
			}
		}

		return result;
	}

    /** This method drives the creation of a callout implementation for one callout mapping. */
    private MethodDeclaration createCallout(CalloutMappingDeclaration calloutMappingDeclaration, boolean needBody, boolean isInferred)
    {
		CallinCalloutScope calloutScope = calloutMappingDeclaration.scope;

		calloutMappingDeclaration.updateTSuperMethods();

		// This binding is part of the interface part of a role:
		MethodBinding roleMethodBinding = calloutMappingDeclaration.getRoleMethod();

        if(roleMethodBinding == null)
        {
        	// problemreporting already done in find-Base/Role-MethodBinding
            assert(calloutMappingDeclaration.ignoreFurtherInvestigation);
			return null;
        }
        if (!roleMethodBinding.isValidBinding()) {
        	if (roleMethodBinding.problemId() != ProblemReasons.NotFound) {
        		// CLOVER: never true in jacks suite
	        	calloutMappingDeclaration.tagAsHavingErrors();
	        	// hopefully error has been reported!
	        	return null;
	        } else { // shorthand style callout
	        	// help sourceMethod() below to find another callout method
	        	MethodBinding existingMethod = calloutScope.enclosingSourceType().getExactMethod(roleMethodBinding.selector, roleMethodBinding.parameters, calloutScope.compilationUnitScope());
	        	if (existingMethod != null)
	        		roleMethodBinding = existingMethod;
	        }
        }

    	MethodDeclaration roleMethodDeclaration = null;
    	if (TypeBinding.equalsEquals(roleMethodBinding.declaringClass, calloutScope.enclosingSourceType()))
    		roleMethodDeclaration = (MethodDeclaration)roleMethodBinding.sourceMethod();

        // have a binding but no declaration for method? -> requires creation of declaration
        boolean foundRoleDecl = roleMethodDeclaration != null;
        MethodBinding overriddenTSuper = null;
        // The following code allows to use tsuper in parameter mappings
        // (see 3.2.32-otjld-tsuper-access-1)
        if (   foundRoleDecl
        	&& roleMethodDeclaration.isCopied											// foundRoleDecl => (roleMethodDeclaration != null)
			&& (roleMethodDeclaration.modifiers & AccAbstract) == 0
			&& !TSuperHelper.isTSuper(roleMethodDeclaration.binding))
        {
        	// mapping conflicts with an implicitly inherited method.
        	// make the latter a tsuper version, now.
        	overriddenTSuper = roleMethodBinding;
        	// save a clone of the method binding before adding the marker arg, for use below.
        	roleMethodBinding = new MethodBinding(roleMethodBinding, roleMethodBinding.declaringClass); // clone

        	TSuperHelper.addMarkerArg(
        			roleMethodDeclaration,
					roleMethodDeclaration.binding.copyInheritanceSrc.declaringClass.enclosingType());
        	foundRoleDecl = false; // re-create the method;
        }

    	if (!foundRoleDecl) {
    		roleMethodDeclaration =
    			createAbstractRoleMethodDeclaration(roleMethodBinding,
    				calloutMappingDeclaration);
    		if (overriddenTSuper != null)
    			roleMethodDeclaration.binding.addOverriddenTSuper(overriddenTSuper);
    	} else {
    		roleMethodDeclaration.isReusingSourceMethod = true;							// foundRoleDecl => (roleMethodDeclaration != null)
    		// mark existing method as generated by the callout mapping:
    		roleMethodDeclaration.isMappingWrapper = WrapperKind.CALLOUT;

    		// match locator may want to know this for the interface part, too:
    		// (SH: unsure, if this is really needed, but it doesn't hurt either ;-)
    		if (roleMethodDeclaration.interfacePartMethod != null) {
    			roleMethodDeclaration.interfacePartMethod.isReusingSourceMethod = true;
        		roleMethodDeclaration.interfacePartMethod.isMappingWrapper = WrapperKind.CALLOUT;
    		}
    	}

		if (calloutMappingDeclaration.hasSignature)
    	{
			// Adjust arguments:
    		Argument[] args = calloutMappingDeclaration.roleMethodSpec.arguments;
    		if (args != null)
    		{
				for (int i=0;i<args.length;i++) {
		    		// if we already have a declaration and if we have signatures
		    		// in the mapping declaration, use the argument names from the
		    		// method mapping rather than those from the original declaration
		    		// (needed for parameter mapping!).
					if (foundRoleDecl)
						roleMethodDeclaration.arguments[i].updateName(args[i].name);

					// also link best names of arguments of roleMethodSpec and actual wrapper
					// ( requires wrapper argument to be bound, do this first.
					//   Note that all args must be bound in order!).
					roleMethodDeclaration.arguments[i].bind(roleMethodDeclaration.scope, args[i].binding.type, false);
					args[i].binding.setBestNameFromStat(roleMethodDeclaration.arguments[i]);
				}
    		}
    	}

    	if (roleMethodDeclaration != null) // try again
    	{
    		// Note: do not query the binding (as isAbstract() would do).
    		// Binding may have corrected modifiers, but take the raw modifiers.
            if (   !roleMethodDeclaration.isCopied
            	&& !roleMethodDeclaration.isGenerated
            	&& (roleMethodDeclaration.modifiers & AccAbstract) == 0)
            {
            	// bad overriding of existing / non-existant methods is handled in MethodMappingResolver already
            	roleMethodDeclaration.ignoreFurtherInvestigation = true; // don't throw "abstract method.. can only be defined by abstract class" error
            	calloutScope.problemReporter().calloutOverridesLocal(
	            		this._role.getAst(),
						calloutMappingDeclaration,
						roleMethodDeclaration.binding);
	            return null;
            }

            // fix flags (even if not needing body):
            roleMethodDeclaration.isCopied = false;
            int flagsToRemove = AccAbstract | ExtraCompilerModifiers.AccSemicolonBody | AccNative;
            roleMethodDeclaration.modifiers &= ~flagsToRemove;
            roleMethodDeclaration.binding.modifiers &= ~flagsToRemove;
            roleMethodDeclaration.isGenerated = true; // even if not generated via AstGenerator.
            if (needBody && calloutMappingDeclaration.binding.isValidBinding()) {
    	        // defer generation of statements:
    	        final CalloutMappingDeclaration mappingDeclaration = calloutMappingDeclaration;
            	MethodModel methodModel = MethodModel.getModel(roleMethodDeclaration);
            	if (isInferred)
            		methodModel._inferredCallout = mappingDeclaration;
				methodModel.setStatementsGenerator(
            		new AbstractStatementsGenerator() {
            			public boolean generateStatements(AbstractMethodDeclaration methodDecl) {
            				createCalloutMethodBody((MethodDeclaration)methodDecl, mappingDeclaration);
            				return true;
            			}
            		});
            } else if (calloutMappingDeclaration.ignoreFurtherInvestigation) {
            	roleMethodDeclaration.binding.bytecodeMissing = true; // will not be generated, so don't complain later.
            }
    	}
    	else // roleMethodDeclaration still null
    	{
    		// CLOVER: never reached in jacks suite ;-)
			throw new InternalCompilerError("OT-Compiler Error: couldn't create method declaration for callout! "  //$NON-NLS-1$
									+ calloutMappingDeclaration.toString());
    	}
        return roleMethodDeclaration;
    }

    private MethodDeclaration createAbstractRoleMethodDeclaration(
		MethodBinding templateBinding,
		CalloutMappingDeclaration calloutBindingDeclaration)
	{
		IProtectable baseFeature = null; 
		MethodSpec baseMethodSpec = calloutBindingDeclaration.baseMethodSpec;
		if (baseMethodSpec != null) // else syntax error?
			baseFeature = calloutBindingDeclaration.isCalloutToField()
									  ? ((FieldAccessSpec)baseMethodSpec).resolvedField
									  : baseMethodSpec.resolvedMethod;
		int modifiers = calloutBindingDeclaration.declaredModifiers;
		if (modifiers == 0) {
			// no modifiers declared in callout, look for base feature, last is the role method template
			modifiers = ((baseFeature != null) && (templateBinding.modifiers == 0))
							? baseFeature.modifiers()
							: templateBinding.modifiers;
			modifiers &= (ExtraCompilerModifiers.AccVisibilityMASK | AccStatic);
		}
		boolean isOverridingVisibility =    calloutBindingDeclaration.isCalloutOverride()
										 && (calloutBindingDeclaration.declaredModifiers != 0);
    	if (   templateBinding.isValidBinding() // short-hand callout has a Problem(NotFound) here
    		&& templateBinding.declaringClass.isRole())
    	{
    		// try to find the ifc-part for 'templateBinding',
    		// if found, use its visibility modifiers for the new method declaration,
    		// because class part visibility modifiers are actually useless (always public..)
    		ReferenceBinding ifcPart = templateBinding.declaringClass.roleModel.getInterfacePartBinding();
	    	if (ifcPart != null) {
	    		MethodBinding ifcMethod = TypeAnalyzer.findMethod(
	    				calloutBindingDeclaration.scope, ifcPart, templateBinding.selector, templateBinding.parameters);
	    		if (   ifcMethod != null && ifcMethod.isValidBinding())
	    		{
	    			if (!isOverridingVisibility) {
	    				// no modifiers in callout, use interface modifiers of found role method:
		    			modifiers &= ~ExtraCompilerModifiers.AccVisibilityMASK;
		    			modifiers |= ifcMethod.modifiers & ExtraCompilerModifiers.AccVisibilityMASK;
	    			}
	    		}
	    	}
    	}

    	// overriding an method explicitly inherited from non-role superclass? (see TPX-416)
    	boolean overridesExplicitNonRole = false;
    	ReferenceBinding superRole = null;
    	ReferenceBinding roleClass = this._role.getClassPartBinding();
    	if (roleClass != null) {
    		superRole = roleClass.superclass();
	    	if (   superRole != null									   // have a super class
	    		&& TypeBinding.notEquals(superRole.enclosingType(), roleClass.enclosingType())) // not a role from current team
	    	{
	    		MethodBinding superMethod = TypeAnalyzer.findMethod(
	    				calloutBindingDeclaration.scope, superRole, templateBinding.selector, templateBinding.parameters);
	    		if (superMethod != null && superMethod.isValidBinding())
	    			overridesExplicitNonRole = true; // TODO(SH): need compatibility checks? (a la MethodVerifier??)
	    	}
    	}

    	if (calloutBindingDeclaration.binding.inferred == InferenceKind.NONE) { // don't advertise inferred callout via the interface.
    		if (templateBinding.isStatic())		 // no real ifc part for static method, fake it! 
	    		createInterfaceFakeStatic(templateBinding, calloutBindingDeclaration);
    		else if (((modifiers & AccPrivate) == 0) && !overridesExplicitNonRole)  // also no ifc part for privates and methods from explicit non-role super
			    createAbstractRoleMethodDeclarationPart(templateBinding,
				    calloutBindingDeclaration,
				    modifiers,
				    INTERFACE);
    	}
		return createAbstractRoleMethodDeclarationPart(templateBinding,
			calloutBindingDeclaration,
			modifiers,
			CLASS);
	}

    /** In order to resolve static role methods via the interface create a method
     *  binding without a declaration.  */
    private void createInterfaceFakeStatic(MethodBinding template, CalloutMappingDeclaration calloutDecl)
    {
    	MethodBinding newMethod = new MethodBinding(template, this._role.getInterfacePartBinding());
    	this._role.getInterfacePartBinding().addMethod(newMethod);
    }

	/**
     * Creates method in interface or class-part of _role.
	 * @param templateBinding this method is used as a template for the method that
	 *        will be created.
	 * @param calloutBindingDeclaration
	 * @param part CLASS or INTERFACE
	 * @return an empty method declaration
	 */
	private MethodDeclaration createAbstractRoleMethodDeclarationPart(
		MethodBinding templateBinding,
		CalloutMappingDeclaration calloutBindingDeclaration,
		int modifiers,
		int part)
	{
		assert(templateBinding != null);

		AstGenerator gen = new AstGenerator(calloutBindingDeclaration.sourceStart, calloutBindingDeclaration.sourceEnd);
		TypeBinding returnType;
		if (calloutBindingDeclaration.roleMethodSpec.returnType != null)
			// if this one is given, it might be instantiated:
			returnType = calloutBindingDeclaration.roleMethodSpec.returnType.resolvedType;
		else
			// this one should exist in any case:
			returnType = calloutBindingDeclaration.roleMethodSpec.resolvedMethod.returnType;
		MethodDeclaration newMethod = gen.method(
				calloutBindingDeclaration.compilationResult,
				modifiers, // start from these, adapt below.
				returnType,
				templateBinding.selector,
				copyArguments(gen,
						calloutBindingDeclaration.scope,
						templateBinding.parameters,
						calloutBindingDeclaration.roleMethodSpec)
			);
		newMethod.typeParameters= getTypeParameters(calloutBindingDeclaration.hasSignature,
				                                    templateBinding,
				                                    calloutBindingDeclaration.roleMethodSpec,
				                                    gen);
		if (templateBinding.problemId() == ProblemReasons.NotFound) {
			// this is a short hand callout declaration:
			MethodSpec baseMethodSpec = calloutBindingDeclaration.baseMethodSpec;
			if (baseMethodSpec != null) { // null if missing in source code (e.g. during completion)
				if (baseMethodSpec.isStatic())
					newMethod.modifiers |= AccStatic;
				if (baseMethodSpec.resolvedMethod != null) {
					newMethod.thrownExceptions = AstClone.copyExceptions(baseMethodSpec.resolvedMethod, gen);
				}
			}
		} else {
			newMethod.thrownExceptions = AstClone.copyExceptions(templateBinding, gen);
		}
	    newMethod.isMappingWrapper = WrapperKind.CALLOUT;

		if (part == INTERFACE)
		{
			// generated callout method must also be added to the interface-part since
			// role-splitting already happened
			// Note: Interface part has the access modifiers!
            newMethod.modifiers |= ExtraCompilerModifiers.AccSemicolonBody|AccAbstract;
           	AstEdit.addMethod(this._role.getInterfaceAst(), newMethod);
		}
		else // == CLASS
		{
			if ((modifiers & AccPrivate) != 0) { // don't advertize in ifc
				// FIXME(SH): need to generate bridge methdods?
			} else if (calloutBindingDeclaration.binding.inferred.isAdvertisedInInterface()) { // only if actually advertised in the ifc-part
				// generated callout method must be public in the classPart.
				// access control is done only via the interface part.
				MethodModel.getModel(newMethod).storeModifiers(newMethod.modifiers);
				newMethod.modifiers &= ~(AccProtected);
				newMethod.modifiers |= AccPublic;
			}
			// abstract will be cleared once we are done.
            AstEdit.addMethod(this._role.getAst(), newMethod);
		}
		calloutBindingDeclaration.updateRoleMethod(newMethod.binding);
		return newMethod;
	}

	/**
	 * Create the methodbody for callouts
	 */
	void createCalloutMethodBody(
		MethodDeclaration         roleMethodDeclaration,
		CalloutMappingDeclaration calloutBindingDeclaration)
	{
		if (!transformCalloutMethodBody(
						roleMethodDeclaration,
						calloutBindingDeclaration))
			roleMethodDeclaration.tagAsHavingErrors();
	}

	private boolean transformCalloutMethodBody(
            MethodDeclaration         roleMethodDeclaration,
            CalloutMappingDeclaration calloutDecl)
	{
		/* Type myCallout(int r)
		 * {
		 *   return __OT__base.myBaseMethod(r);
		 * }
		 */

        // NOTE (SH): Do not manually set any resolvedType fields.
        // If this field is set, we assume resolve has completed for that element.
        // So we rather rely on resolveType() to fill in the details later.

		// Note(SH): do not use roleMethodBinding.returnType as a short-cut:
		//           the method spec holds more specific type information (weakening!)
        TypeBinding returnType =
                calloutDecl.roleMethodSpec.returnType.resolvedType;

        int sStart ;
        int sEnd;

        // the generated method implements the callout declaration,
        // so use its source location for the wrapper method:
        sStart = calloutDecl.sourceStart;
        sEnd   = calloutDecl.sourceEnd;
        // these two are needed to generate correct line numbers:
        roleMethodDeclaration.bodyStart   = sStart;
        roleMethodDeclaration.bodyEnd     = sEnd;
        if (!roleMethodDeclaration.isReusingSourceMethod) {
	        roleMethodDeclaration.sourceStart = sStart;
	        roleMethodDeclaration.sourceEnd   = sEnd;
			roleMethodDeclaration.declarationSourceStart = sStart;
			roleMethodDeclaration.declarationSourceEnd   = sEnd;
        } // Else we have a source method in the same class.
		  // => Keep its source positions, because otherwise search et al.
		  // can't distinguish the abstract declaration and the callout

		Expression[] arguments;
        if(calloutDecl.hasSignature)
		{
        	arguments = makeWrapperCallArguments(
                    		calloutDecl,
							roleMethodDeclaration,
							calloutDecl.roleMethodSpec,
							(calloutDecl.baseMethodSpec instanceof FieldAccessSpec) ? ((FieldAccessSpec)calloutDecl.baseMethodSpec) : null,
							false /*hasResultArg*/);
			if (   arguments == null
                || hasParamMappingProblems(calloutDecl, returnType, roleMethodDeclaration.scope.problemReporter()))
			{
                return false;
			}
		} else {
			arguments = makeArguments(calloutDecl, roleMethodDeclaration, calloutDecl.baseMethodSpec);
		}

        // From this point use the source location of its base method spec,
        // because the method body is a call to the method specific by the spec.
        sStart = calloutDecl.baseMethodSpec.sourceStart;
        sEnd   = calloutDecl.baseMethodSpec.sourceEnd;
        final AstGenerator gen = new AstGenerator(sStart, sEnd);

        char[] selector = calloutDecl.baseMethodSpec.selector;
        ReferenceBinding baseType = this._role.getBaseTypeBinding();
		Expression receiver = gen.castExpression(
							gen.baseNameReference(IOTConstants._OT_BASE),
							gen.baseclassReference(baseType),
							CastExpression.DO_WRAP);

		Expression baseAccess = null;
		if (calloutDecl.baseMethodSpec.isPrivate() && baseType.isRole()
				&& calloutDecl.baseMethodSpec.implementationStrategy != ImplementationStrategy.DYN_ACCESS)
		{
    		// tricky case: callout to a private role method (base-side)
    		// requires the indirection via two wrapper methods (privateBridgeMethod)

    		// compensate weakening:
    		if (baseType instanceof WeakenedTypeBinding)
    			baseType = ((WeakenedTypeBinding)baseType).getStrongType();

    		// generated message send refers to public bridge, report decapsulation now:
    		calloutDecl.scope.problemReporter().decapsulation(calloutDecl.baseMethodSpec, baseType, calloutDecl.scope);

    		boolean isCalloutToField = calloutDecl.isCalloutToField();
    		MethodBinding targetMethod = calloutDecl.baseMethodSpec.resolvedMethod;
	    	MessageSend baseSend = new PrivateRoleMethodCall(receiver, selector, arguments, isCalloutToField,
	    										   calloutDecl.scope, baseType, targetMethod, gen);
    		baseSend.accessId = calloutDecl.baseMethodSpec.accessId;
    		baseAccess = baseSend;
    	} else {
    		if (calloutDecl.baseMethodSpec.isStatic())
    			// we thought we should use an instance
    			// but callout-to-static is sent to the base *class*
    			receiver = gen.baseTypeReference(baseType);
    		switch (calloutDecl.baseMethodSpec.implementationStrategy) {
				case DIRECT:
					if (calloutDecl.isCalloutToField()) {
						// not using a decapsulation accessor but direct field access:
						FieldAccessSpec fieldSpec = (FieldAccessSpec) calloutDecl.baseMethodSpec;
						FieldBinding baseField = fieldSpec.resolvedField;
						if (baseField.isStatic())
							baseAccess = gen.qualifiedNameReference(baseField);
						else
							baseAccess = gen.qualifiedBaseNameReference(new char[][] {IOTConstants._OT_BASE, baseField.name });
						if (fieldSpec.isSetter()) {
							int pos = (fieldSpec.isStatic() | this._role.getWeavingScheme() == WeavingScheme.OTDRE) ? 0 : 1;
							baseAccess = gen.assignment((NameReference)baseAccess, arguments[pos]);
							returnType = TypeBinding.VOID; // signal that no result processing is necessary
						}
					} else {
		    			baseAccess = gen.messageSend(receiver, selector, arguments);
					}
					break;
				case DECAPS_WRAPPER:
					if (!calloutDecl.baseMethodSpec.isStatic() && calloutDecl.isCalloutToField())
						// we thought we should use an instance
						// but decapsulating c-t-f (non-role-base) is sent to the base *class*
						receiver = gen.baseNameReference(baseType.getRealClass());
		    		MessageSend baseSend = gen.messageSend(receiver, selector, arguments);
		    		baseSend.accessId = calloutDecl.baseMethodSpec.accessId;
		    		baseAccess = baseSend;
		    		break;
				case DYN_ACCESS:
					baseAccess = CalloutImplementorDyn.baseAccessExpression(calloutDecl.scope, this._role, baseType, receiver, calloutDecl.baseMethodSpec, arguments, gen);
					break;
    		}			
		}	 

        boolean success = true;
        ArrayList<Statement> statements = new ArrayList<Statement>(3);
        if(returnType == TypeBinding.VOID)
		{
       		// just the method call
        	statements.add(baseAccess);

        	// generate empty return statement so that it gets a proper source position
        	statements.add(gen.returnStatement(null));
		} else {
			if (calloutDecl.mappings == null)
			{
				// return the result of the method call
				statements.add(
						gen.returnStatement(
								gen.potentialLift(
										null,     // use default receiver
										baseAccess,
										returnType,
										false))); // no reversing required
			}
			else
			{
				// return result with parameter mapping
				success = transformCalloutMethodBodyResultMapping(
					statements,
					baseAccess,
					calloutDecl,
					roleMethodDeclaration);
			}
		}
        if (success) {
        	roleMethodDeclaration.setStatements(
        			statements.toArray(new Statement[statements.size()]));
        } else {
        	roleMethodDeclaration.statements = new Statement[0];
        }
		return success;
	}

	/**
     * Make the arguments of this message send.
     * Note: it is not guaranteed that the parameters match!
     * @param baseMethodSpec spec for the base method (must be resolved).
     */
    private Expression[] makeArguments(
    			CalloutMappingDeclaration methodMapping,
				AbstractMethodDeclaration roleMethodDecl,
				MethodSpec baseMethodSpec)
    {
        assert (!methodMapping.hasSignature);
        Argument[]    roleArgs   = roleMethodDecl.arguments;

        MethodSpec roleMethodSpec = methodMapping.roleMethodSpec;
		AstGenerator gen = new AstGenerator(roleMethodSpec.sourceStart, roleMethodSpec.sourceEnd);
        int minArguments;
        Expression[] arguments = null;
		int offset=0;
        if (baseMethodSpec instanceof FieldAccessSpec) {
        	// field access is mapped to static method with additional first parameter _OT$base:
        	if (((FieldAccessSpec) baseMethodSpec).isSetter())
				minArguments = 1;
			else
				minArguments = 0;
        	if (!baseMethodSpec.isStatic() && this._role.getWeavingScheme() == WeavingScheme.OTRE) { // OTREDyn uses non-static accessor for non-static fields
        		arguments = new Expression[minArguments+1];
	        	// cast needed against weakened _OT$base reference
        		//   and if base is a role, to go to the class type (FIXME)
	        	gen.retargetFrom(baseMethodSpec);
	    		arguments[0] = gen.castExpression(
						gen.singleNameReference(IOTConstants._OT_BASE),
						gen.baseclassReference(this._role.getBaseTypeBinding().getRealClass()),
						this._role.getBaseTypeBinding().isRole() ? CastExpression.NEED_CLASS : CastExpression.RAW); // FIXME(SH): change to RAW and let OTRE do the cast?
	    		gen.retargetFrom(roleMethodSpec);
	    		offset = 1;
        	} else {
        		arguments = new Expression[minArguments];
        	}
        	if (((FieldAccessSpec) baseMethodSpec).isSetter())
        		arguments[offset] = new PotentialLowerExpression(
						gen.singleNameReference(roleArgs[0].name),
						adjustBaseSideType(baseMethodSpec.resolvedType()));
        	return arguments;
        } else {
        	if (roleArgs == null)
        		return new Expression[0];
        	TypeBinding[] baseParams =  baseMethodSpec.resolvedMethod.parameters;
            minArguments = Math.min(baseParams.length, roleArgs.length);
            assert(minArguments == baseParams.length);
        	arguments = new Expression[minArguments];
        	for(int i=0; i<minArguments; i++)
        	{
        		arguments[i+offset] = new PotentialLowerExpression(
        				gen.singleNameReference(roleArgs[i].name),
        				adjustBaseSideType(baseParams[i+offset]));
        	}
        	return arguments;
        }
    }

    private boolean transformCalloutMethodBodyResultMapping(
    	ArrayList<Statement>      statements,
    	Expression                resultExpr,
		CalloutMappingDeclaration calloutDecl,
		MethodDeclaration         roleMethodDeclaration)
    {
		Expression resultMapper = null;
		ParameterMapping[] mappings = calloutDecl.mappings;
		boolean resultFound = false;
		int sStart = 0;
		int sEnd = 0;
		int resultStart = 0;
		int resultEnd = 0;

		for (int i=0; i<mappings.length; i++)
		{
			if (!mappings[i].isUsedFor(calloutDecl.roleMethodSpec))
			{
				if (CharOperation.equals(mappings[i].ident.token, IOTConstants.RESULT))
				{
					if (resultFound)
					{
						roleMethodDeclaration.scope.problemReporter().duplicateParamMapping(
							   mappings[i],
							   IOTConstants.RESULT,
							   /*isCallout*/true);
						return false;
					}
					resultMapper = mappings[i].expression;
					sStart = mappings[i].sourceStart;
					sEnd   = mappings[i].sourceEnd;
					resultStart = mappings[i].ident.sourceStart;
					resultEnd   = mappings[i].ident.sourceEnd;
					resultFound = true;
				}
			}
		}
		if (!resultFound) // CLOVER: never true in jacks suite
		{
			roleMethodDeclaration.scope.problemReporter().unmappedParameter(
					IOTConstants.RESULT,
					calloutDecl.roleMethodSpec,
					/*isCallout*/true);
			return false;
		}
		assert(resultMapper != null);
		assert (calloutDecl.baseMethodSpec.hasSignature);

		AstGenerator gen = new AstGenerator(resultStart, resultEnd);

		Statement callStatement = resultExpr;

		TypeBinding baseReturnType = calloutDecl.baseMethodSpec.returnType.resolvedType;
		if (baseReturnType != TypeBinding.VOID) {
			char[] localName = IOTConstants.RESULT;
			if (calloutDecl.baseMethodSpec instanceof FieldAccessSpec)
				localName = ((FieldAccessSpec)calloutDecl.baseMethodSpec).getFieldName();
			calloutDecl.resultVar = gen.localVariable(
				localName,
				calloutDecl.baseMethodSpec.returnType.resolvedType,
				resultExpr);
			calloutDecl.resultVar.type.setBaseclassDecapsulation(DecapsulationState.REPORTED);
			callStatement = calloutDecl.resultVar;
		}

		gen.sourceStart = sStart;
		gen.sourceEnd   = sEnd;

		statements.add(callStatement);
		if (!roleMethodDeclaration.isStatic()) {
			// for "base" in parameter mappings append: BaseType base = _OT$base;
			// do this _after_ the actual forwarding so that 'base' will not shadow anything (see 3.3.21-otjld-callout-to-field-anchored-type-3)
			FieldBinding baseField = TypeAnalyzer.findField(this._role.getBinding(), IOTConstants._OT_BASE, /*static*/false, false);
			if (baseField != null) // CLOVER: never false in jacks suite
				statements.add(
						gen.localVariable(IOTConstants.BASE,
								gen.baseclassReference(baseField.type),
								gen.singleNameReference(IOTConstants._OT_BASE)));
		}
		statements.add(gen.returnStatement(
						gen.potentialLift(
							null, 	   // use default receiver
							resultMapper,
							calloutDecl.roleMethodSpec.returnType.resolvedType,
							false)));  // no reversing required
		return true;
    }

    /**
     * After building mapped arguments the parameter mapping should be empty
     * except for a result mapping. Check if this is so. Also check for
     * result mapping in void method.
     * @param calloutMappingDeclaration
     * @param returnType
     * @param problemReporter
     * @return the answer
     */
    private static boolean hasParamMappingProblems(
            CalloutMappingDeclaration calloutMappingDeclaration,
            TypeBinding returnType,
            ProblemReporter problemReporter)
    {
        boolean hasArgError = false;
        ParameterMapping[] mappings = calloutMappingDeclaration.mappings;
        if (mappings != null) {
            for (int i = 0; i < mappings.length; i++) {
            	if (CharOperation.equals(mappings[i].ident.token, IOTConstants.RESULT))
            	{
            		if (mappings[i].direction == TerminalTokens.TokenNameBINDOUT)
            		{
            			problemReporter.wrongBindingDirection(
            					calloutMappingDeclaration, mappings[i]);
            			hasArgError = true;
            		}
                    else if (returnType == TypeBinding.VOID)
                    {
                        problemReporter.resultMappingForVoidMethod(
                                calloutMappingDeclaration,
								calloutMappingDeclaration.roleMethodSpec,
                                mappings[i]);
                        hasArgError = true;
                    }
            	}
            	else if (mappings[i].expression instanceof ResultReference)
            	{
                	problemReporter.mappingResultToOther(
                			calloutMappingDeclaration, mappings[i]);
            	}
            	else
            	{
            		if (!mappings[i].isUsedFor(calloutMappingDeclaration.roleMethodSpec))
            		{
	                    if (mappings[i].direction == TerminalTokens.TokenNameBINDOUT)
	                    {
	                    	// TODO(SH): can this actually happen? Clover says: YES.
	                    	// would have to be an inexistent param ident?
	                        problemReporter.unusedParamMap(
	                                calloutMappingDeclaration,
	                                mappings[i]);
	                        // warning only
	                    }
	                    else
	                    {
                    		problemReporter.wrongBindingDirection(
		                                calloutMappingDeclaration, mappings[i]);
	                        hasArgError = true;
	                    }
                    }
                }
            }
        }
        return hasArgError;
    }
	// =======================================================

    /**
	 * Note that as a side effect, this method modifies methodMapping.mappings!
	 * @param methodMapping    lookup method spec and parameter mapping here
	 * @param wrapperDeclaration      use these if no mapping is involved
	 * @param implParameters   parameters of the implemented method to invoke
	 * @param idx              argument position on the target side
	 * @param hasResultArgument (ignored)
	 * @param sourceMethodSpec this signature defines the provided args
	 * @return a mapped argument expression or null
	 */
	Expression getArgument(
					AbstractMethodMappingDeclaration methodMapping,
					MethodDeclaration                wrapperDeclaration,
					TypeBinding[]                    implParameters,
					int                              idx,
					boolean                          hasResultArgument,
					MethodSpec                       sourceMethodSpec)
	{
		MethodSpec implementationMethodSpec = methodMapping.getImplementationMethodSpec();
		Argument[] specifiedArgs = implementationMethodSpec.arguments;

		ParameterMapping[] parameterMappings = methodMapping.mappings;
	    Expression mappedArgExpr = null;
	    char[] targetArgName = null;
		if (parameterMappings == null)
	    {
	        targetArgName = wrapperDeclaration.arguments[idx].name;
	        mappedArgExpr = genSimpleArgExpr(
	        					targetArgName,
								((CalloutMappingDeclaration)methodMapping).baseMethodSpec);
	        // if parameters are not mapped, identify provided and expected args.
	        // Note that expected args means those of the baseMethodSpec
	        // which are otherwise unreachable.
	        // (stored in a temporary scope, see AbstractMethodMappingDeclaration.resolveMethodSpecs)
	        if (idx < specifiedArgs.length) // CLOVER: never false in jacks suite
	        	specifiedArgs[idx].binding.setBestNameFromStat(wrapperDeclaration.arguments[idx]);
	    }
		else
	    {
	    	if (methodMapping.mappingExpressions == null) {
	    		assert !methodMapping.hasParsedParamMappings : "expect lack of parsing as cause for missing expressions"; //$NON-NLS-1$
		    	return null; // this indicates an error (required param mappings have not been parsed)
	    	}
	    	targetArgName = implementationMethodSpec.arguments[idx].name;
	    	Pair<Expression, Integer> mapper = methodMapping.mappingExpressions[idx];
	    	mappedArgExpr = mapper.first;
	    }
	    if (mappedArgExpr != null) {
	    	if (idx >= implParameters.length) // CLOVER: never true in jacks suite
	    		return mappedArgExpr; // arg is invisible to receiver, don't lower
			TypeBinding expectedType = implParameters[idx];
			
			// if type is anchored to another arg of the same binding, we have to translate:
			// 1.) is it a param-anchored type?
			if (expectedType.leafComponentType() instanceof DependentTypeBinding) {
				DependentTypeBinding dependentExpectedLeaf = (DependentTypeBinding)expectedType.leafComponentType();
				int anchorArgPos = dependentExpectedLeaf._argumentPosition;
				if (anchorArgPos != -1) {
					// 2.) need to translate the position using the param mapping?
					if (methodMapping.positions != null)
						anchorArgPos = methodMapping.positions[anchorArgPos]-1;
					// 3.) with this position retrieve the source argument and re-anchor the type to this anchor:
					ITeamAnchor mappedAnchor = sourceMethodSpec.arguments[anchorArgPos].binding;
					expectedType = mappedAnchor.getRoleTypeBinding(dependentExpectedLeaf, expectedType.dimensions());
				}
			}

			return new PotentialLowerExpression(mappedArgExpr, adjustBaseSideType(expectedType));
	    }

	    wrapperDeclaration.scope.problemReporter()
				.unmappedParameter(
		            targetArgName,
		            implementationMethodSpec,
					methodMapping.isCallout());
	    return null;
	}

	TypeBinding adjustBaseSideType(TypeBinding givenType) {
		ReferenceBinding baseBinding = this._role.getBaseTypeBinding();
		if (baseBinding == null) return givenType;
		if (givenType.leafComponentType().isBaseType()) return givenType;
		ReferenceBinding givenLeaf = (ReferenceBinding)givenType.leafComponentType();
		int dimensions = givenType.dimensions();
		TypeVariableBinding[] arguments = null;
		if (givenType.isParameterizedType())
			arguments = ((ParameterizedTypeBinding)givenType).typeVariables();
		if (baseBinding instanceof DependentTypeBinding) {
			ITeamAnchor anchor = ((DependentTypeBinding)baseBinding).getAnchor();
			if (anchor.isTeamContainingRole(givenLeaf)) {
				if (TypeBinding.notEquals(anchor.getResolvedType(), givenLeaf.enclosingType()))
					givenLeaf = (ReferenceBinding) TeamModel.strengthenRoleType((ReferenceBinding) anchor.getResolvedType(), givenLeaf);
				return anchor.getDependentTypeBinding(givenLeaf, -1, arguments, dimensions);
			}
		}
		return givenType;
	}

	@Override
	TypeBinding[] getImplementationParamters(AbstractMethodMappingDeclaration methodMapping, MethodDeclaration wrapperMethod)
	{
		TypeBinding[] result= super.getImplementationParamters(methodMapping, wrapperMethod);
		// if we have type variables replace them with the version of the wrapper method:
		int l= result.length;
		if (result == Binding.NO_PARAMETERS || l == 0)
			return result;
		TypeVariableBinding[] variables= wrapperMethod.binding.typeVariables();
		if (variables != Binding.NO_TYPE_VARIABLES) {
			System.arraycopy(result, 0, result= new TypeBinding[l], 0, l);
			for (int i = 0; i < result.length; i++)
				result[i] = substituteVariables(result[i], variables);
		}
		return result;
	}

	/** Given a binding of a callout mapping as inherited from a super interface,
	 *  create the wrapper method for the current role class.
	 */
	public void generateFromBinding(CallinCalloutBinding mapping)
	{
		TypeDeclaration roleClass = this._role.getClassPartAst();
		boolean needBody = Dependencies.needMethodBodies(roleClass);
		AstGenerator gen = new AstGenerator(roleClass.sourceStart, roleClass.sourceEnd);
		CalloutMappingDeclaration callout = gen.calloutMappingDeclaration(roleClass.compilationResult);
		callout.roleMethodSpec = fromMethodBinding(mapping._roleMethodBinding, gen);
		if (mapping._baseMethods.length > 0)
			callout.baseMethodSpec = fromMethodBinding(mapping._baseMethods[0], gen);
		else
			assert !needBody : "Role needing method bodies should have base method set"; //$NON-NLS-1$
		// fake resolving:
		callout.scope = new CallinCalloutScope(roleClass.scope, callout);
		callout.scope.createBinding(callout);
		// these create a return type reference from the return type binding:
		callout.checkReturnCompatibility(callout.roleMethodSpec);
		if (callout.baseMethodSpec != null)
			callout.checkReturnCompatibility(callout.baseMethodSpec);
		// translate to method declaration:
		createCallout(callout, needBody, MethodModel.getImplementingInferredCallout(mapping._roleMethodBinding)!=null/*isInferred*/);
	}

	/**
	 * Try to infer a callout binding that implements a given abstract method.
	 *
	 * @param roleClass      type into which the callout might be generated (guaranteed to be a role)
	 * @param abstractMethod inherited abstract method
	 * @return the generated wrapper method or null
	 */
	public MethodDeclaration generateInferredCallout(TypeDeclaration roleClass, MethodBinding abstractMethod) {
		ReferenceBinding baseType = this._role.getBaseTypeBinding();
		if (baseType == null || !baseType.isValidBinding())
			return null;
		AstGenerator gen = new AstGenerator(roleClass.sourceStart, roleClass.sourceEnd);
		MethodSpec roleMethod = fromMethodBinding(abstractMethod, gen);
		TypeBinding[] roleParams = abstractMethod.parameters;

		return internalGenerateInferredCallout(roleClass, baseType,
											   roleMethod, roleParams, InferenceKind.INTERFACE,
											   gen);
	}

	/**
	 * Try to infer a callout binding from an otherwise unresolved message send.
	 *
	 * @param roleClass    unchecked
	 * @param messageSend
	 * @param roleParams
	 * @return whether or not inference succeeded
	 */
	public static boolean inferMappingFromCall(TypeDeclaration roleClass,
											   MessageSend     messageSend,
											   TypeBinding[]   roleParams)
	{
		if (roleClass == null || !roleClass.isRole())
			return false;

		if (!messageSend.receiver.isThis())
			return false;

		ReferenceBinding baseType = roleClass.binding.baseclass();
		if (baseType == null)
			return false;

		AstGenerator gen = new AstGenerator(messageSend.sourceStart, messageSend.sourceEnd);
		MethodSpec roleMethod = fromMessageSend(roleClass, messageSend, roleParams, gen);

		CalloutImplementor coi = new CalloutImplementor(roleClass.getRoleModel());
		if (coi.internalGenerateInferredCallout(roleClass, baseType,
												roleMethod, roleParams, InferenceKind.SELFCALL,
												gen) != null)
		{
			messageSend.binding = roleMethod.resolvedMethod;
			return true;
		}
		return false;
	}

	/**
	 * After a role method spec has been created perform remaining tasks of
	 * inferring a base method and creating the callout mapping.
	 * (not used for inferred callout-to-field).
	 *
	 * @param roleClass      guaranteed to be a bound role
	 * @param baseType       non-null
	 * @param roleMethodSpec freshly created and manually resolved
	 * @param roleParams
	 * @param kind 			 infer from superInterface or from an unresolved self-call?
	 * @param gen            positioned AstGenerator
	 * @return the generated wrapper method or null if no success
	 */
	private MethodDeclaration internalGenerateInferredCallout(TypeDeclaration  roleClass,
													ReferenceBinding baseType,
													MethodSpec 		 roleMethodSpec,
													TypeBinding[]    roleParams,
													InferenceKind 	 kind,
													AstGenerator     gen)
	{
		CalloutMappingDeclaration callout;
		callout= gen.calloutMappingDeclaration(roleClass.compilationResult);
		callout.roleMethodSpec= roleMethodSpec;
		// fake resolving:
		callout.scope= new CallinCalloutScope(roleClass.scope, callout);

		// find matching candidate:
		MethodBinding candidate;
		candidate= inferBaseMethod(callout, baseType, roleMethodSpec.selector, roleParams);
		if (candidate == null)
			return null;

		// modifiers adjustment:
		if (kind == InferenceKind.SELFCALL && candidate.isStatic())
			// no problem to adjust method binding which is generated in #fromMessageSend()
			roleMethodSpec.resolvedMethod.modifiers |= AccStatic;

		// return type adjustments:
		TypeBinding expectedType= roleMethodSpec.resolvedType();
		if (expectedType instanceof DependentTypeBinding) {
			DependentTypeBinding roleBinding = (DependentTypeBinding)expectedType;
			if (roleBinding.getAnchor() instanceof TThisBinding)
				expectedType= roleBinding.baseclass();
		}

		// adjust return type from base method:
		if (   expectedType != null
			&& !candidate.returnType.isCompatibleWith(expectedType))
		{
			roleMethodSpec.returnType.resolvedType = candidate.returnType;
			roleMethodSpec.resolvedMethod.returnType = candidate.returnType;
		}

		// add thrown exceptions:
		roleMethodSpec.resolvedMethod.thrownExceptions = candidate.thrownExceptions;

		if (kind == InferenceKind.SELFCALL) {
			// adjust parameters from base method:
			for (int i = 0; i < roleParams.length; i++) {
				if (TypeBinding.notEquals(roleParams[i], candidate.parameters[i])) {
					Config.requireTypeAdjustment(); // reset
					if (   roleParams[i].isCompatibleWith(candidate.parameters[i])
						&& !Config.getLoweringRequired())
					{
						roleMethodSpec.resolvedMethod.parameters[i] = candidate.parameters[i];
					}
				}
			}
		}

		// continue generating:
		callout.baseMethodSpec = fromMethodBinding(candidate, gen);

		// now we have all information for checking 3.4(d) (see Trac #96):
		if (!callout.checkVisibility(callout.baseMethodSpec, baseType))
			return null; // don't generate illegal access.

		CallinCalloutBinding calloutBinding;
		calloutBinding= callout.scope.createBinding(callout);
		calloutBinding.inferred= kind;
		calloutBinding._baseMethods = new MethodBinding[] {candidate};
		roleClass.binding.addCallinCallouts(new CallinCalloutBinding[]{calloutBinding});
		// these create a return type reference from the return type binding:
		callout.checkReturnCompatibility(callout.roleMethodSpec);
		callout.checkReturnCompatibility(callout.baseMethodSpec);
		// translate to method declaration:
		return createCallout(callout, true/*needBody*/, true/*isInferred*/);
	}

	/**
	 *
	 * @param callout    role method spec is set, base is unset.
	 * @param baseType   non-null
	 * @param selector
	 * @param roleParams
	 * @return whether or not inference succeeded
	 */
	private MethodBinding inferBaseMethod(CalloutMappingDeclaration callout,
										  ReferenceBinding 			baseType,
										  char[] 	       			selector,
										  TypeBinding[]    			roleParams)
	{
		ArrayList<MethodBinding> candidates = new ArrayList<MethodBinding>();
		while (baseType != null) {
			try {
				for (MethodBinding method : baseType.methods())
					if (   CharOperation.equals(method.selector, selector)
						&& method.parameters.length == roleParams.length)
					{
						candidates.add(method);
					}
			} catch (AbortCompilation ac) {
				if (baseType.isBinaryBinding() && ac.problem.getID() == IProblem.IsClassPathCorrect)
					return null; // binary type with missing dependencies, nothing we can infer
				throw ac; // unknown reason, don't silently swallow
			}

			for (MethodBinding candidate : candidates)
				if (callout.internalCheckParametersCompatibility(null, roleParams, candidate.parameters))
					return candidate;
			baseType= baseType.superclass();
		}
		return null;
	}

	private MethodSpec fromMethodBinding(MethodBinding method, AstGenerator gen) {
		MethodSpec result = gen.methodSpec(method.selector);
		result.resolvedMethod = method;
		setInferredReturnType(result, method.returnType, gen);
		result.initTranslationBits(); // needed for internalCheckParametersCompatibility()
		return result;
	}

	private static MethodSpec fromMessageSend(TypeDeclaration roleClass,
											  MessageSend     send,
											  TypeBinding[]   roleParams,
											  AstGenerator 	  gen)
	{
		MethodSpec result = gen.methodSpec(send.selector);
		TypeBinding expectedType = send.expectedType;
		result.resolvedMethod = new MethodBinding(AccPrivate, send.selector, expectedType, roleParams, null, roleClass.binding);
		setInferredReturnType(result, expectedType, gen);
		result.initTranslationBits(); // needed for internalCheckParametersCompatibility()
		return result;
	}
	private static void setInferredReturnType(MethodSpec methodSpec, TypeBinding expectedType, AstGenerator gen)
	{
		methodSpec.returnType = gen.singleTypeReference("<inferredType>".toCharArray()); //$NON-NLS-1$
		if (expectedType != null)
			methodSpec.returnType.resolvedType = expectedType;
		else
			methodSpec.returnType.resolvedType = TypeBinding.VOID;
	}

	/**
	 * API: If an expression failed to resolve a field,
	 * try whether using a callout-to-field could be substituted
	 * (potentially also inferring the callout itself).
	 *
	 * @param scope
	 * @param receiver  can be null
	 * @param location
	 * @param fieldName
	 * @param isSetter
	 * @param expectedType what type does the caller of this inferred callout expect? May be null.
	 * @return whether or not inference succeeded
	 */
	public static CalloutMappingDeclaration inferCalloutAccess(Scope scope, Expression receiver, Expression location, char[] fieldName, boolean isSetter, TypeBinding expectedType) {
		if (receiver != null && !(receiver instanceof ThisReference))
			return null;
		TypeDeclaration type= scope.referenceType();
		if (!type.isRole() || !type.getRoleModel().isBound())
			return null;
		char[] accessorName = OTNameUtils.accessorName(isSetter, fieldName);

		// search callout-to-field:
		CalloutMappingDeclaration callout = null;
		if (type.callinCallouts != null) {
			for (AbstractMethodMappingDeclaration mapping : type.callinCallouts) {
				if (!mapping.isCallout())
					continue;
				CalloutMappingDeclaration candidate = (CalloutMappingDeclaration)mapping;
				if (!candidate.isCalloutToField())
					continue;
				FieldAccessSpec fieldAccessSpec = (FieldAccessSpec)candidate.baseMethodSpec;
				if (fieldAccessSpec.isSetter() != isSetter)
					continue;
				FieldBinding baseField= fieldAccessSpec.resolvedField;
				if (baseField == null || !baseField.isValidBinding())
					continue;
				if (   CharOperation.equals(baseField.name, fieldName)
					&& CharOperation.equals(candidate.roleMethodSpec.selector, accessorName))
				{
					// found
					callout = candidate;
					break;
				}
			}
		}
		if (callout == null) { // second chance: infer the callout binding, too:
			AstGenerator gen= new AstGenerator(location.sourceStart, location.sourceEnd);
			callout = inferCalloutToField(type, fieldName, accessorName, isSetter, expectedType, gen);
		}

		if (callout != null) {
			if ((location.bits & ASTNode.IsCompoundAssigned) != 0) {
				// not legal in this context
				scope.problemReporter().inferredCalloutInCompoundAssignment(location, fieldName);
				return null;
			}
			scope.problemReporter().inferredUseOfCalloutToField(isSetter, location, fieldName, ((FieldAccessSpec)callout.baseMethodSpec).resolvedField);
		}
		return callout;
	}

	private static CalloutMappingDeclaration inferCalloutToField(TypeDeclaration roleClass,
														   		 char[] 		 fieldName,
														   		 char[]          accessorName,
														   		 boolean 		 isSetter,
														   		 TypeBinding     expectedType,
														   		 AstGenerator    gen)
	{
		ReferenceBinding baseclass= roleClass.binding.baseclass();
		if (baseclass == null || !baseclass.isValidBinding())
			return null;

		// find matching candidate:
		FieldBinding baseField= TypeAnalyzer.findField(baseclass, fieldName, /*static*/false, false);
		if (baseField == null)
			return null;

		FieldModel fieldModel= FieldModel.getModel(baseField);
		CalloutMappingDeclaration callout= isSetter ?
									fieldModel._setterCallout : fieldModel._getterCallout;
		if (callout != null)
			return callout; // already generated

			// generate:
		callout= gen.calloutMappingDeclaration(roleClass.compilationResult);
		callout.hasSignature= true;
		if (isSetter) {
			fieldModel._setterCallout= callout;
			fillInferredCalloutSetToField(callout, accessorName, baseField, gen);
		} else {
			fieldModel._getterCallout= callout;
			fillInferredCalloutGetToField(callout, accessorName, baseField, expectedType, gen);
		}

		// resolve:
		callout.scope= new CallinCalloutScope(roleClass.scope, callout);
		CallinCalloutBinding calloutBinding= callout.scope.createBinding(callout);
		callout.resolveMethodSpecs(roleClass.getRoleModel(), baseclass, true);
		
		calloutBinding.inferred= isSetter ? InferenceKind.FIELDSET : InferenceKind.FIELDGET;
		if (callout.baseMethodSpec.resolvedMethod != null) {			
			calloutBinding._baseMethods = new MethodBinding[] {callout.baseMethodSpec.resolvedMethod};
		} else {
			calloutBinding._baseField = ((FieldAccessSpec)callout.baseMethodSpec).resolvedField;
		}
		roleClass.binding.addCallinCallouts(new CallinCalloutBinding[]{calloutBinding});

		// translate to method declaration:
		new CalloutImplementor(roleClass.getRoleModel()).createCallout(callout, true/*needBody*/, true/*isInferred*/);

		return callout;
	}

	/* Create method specs for a callout "get" to field. */
	private static void fillInferredCalloutGetToField(CalloutMappingDeclaration callout,
												      char[] 					accessorName,
												      FieldBinding 				baseField,
												      TypeBinding 			    expectedType,
												      AstGenerator 			 	gen)
	{
		MethodSpec roleMethodSpec;
		roleMethodSpec = gen.methodSpec(accessorName);
		roleMethodSpec.hasSignature= true;
		callout.roleMethodSpec= roleMethodSpec;
		roleMethodSpec.returnType= gen.typeReference(expectedType != null ? expectedType : baseField.type);
		roleMethodSpec.arguments= new Argument[0];

		FieldAccessSpec fieldAccessSpec;
		fieldAccessSpec = gen.fieldAccessSpec(baseField.name, baseField.type, false);
		fieldAccessSpec.hasSignature= true;
		callout.baseMethodSpec= fieldAccessSpec;
	}

	/* Create method specs for a callout "set" to field. */
	private static void fillInferredCalloutSetToField(CalloutMappingDeclaration callout,
													  char[] 					accessorName,
													  FieldBinding 				baseField,
													  AstGenerator 				gen)
	{
		MethodSpec  roleMethodSpec;
		roleMethodSpec = gen.methodSpec(accessorName);
		roleMethodSpec.hasSignature= true;
		callout.roleMethodSpec= roleMethodSpec;
		roleMethodSpec.returnType= gen.singleTypeReference(TypeConstants.VOID);
		roleMethodSpec.arguments= new Argument[] {
			gen.argument(baseField.name, gen.typeReference(baseField.type))
		};

		FieldAccessSpec fieldAccessSpec;
		fieldAccessSpec = gen.fieldAccessSpec(baseField.name, baseField.type, true);
		fieldAccessSpec.hasSignature= true;
		callout.baseMethodSpec= fieldAccessSpec;
	}
}
