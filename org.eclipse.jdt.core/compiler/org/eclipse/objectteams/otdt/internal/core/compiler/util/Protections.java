/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
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
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TSuperMessageSend;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel.ProblemDetail;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * Code dealing with modifiers and protection of team/role related elements.
 *
 * TODO (OTJLD): implement import restriction (1.2.3.(a))
 *
 * @author stephan
 * @version $Id: Protections.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class Protections implements ClassFileConstants, ExtraCompilerModifiers {

    private static final int AccSynthIfc = (AccInterface|AccSynthetic);

    /**
     * Role classes cannot be private
     * @param modifiers
     * @return adjusted modifiers
     */
    public static int checkRoleModifiers(int modifiers, TypeDeclaration type, Scope scope)
    {
        if (!type.isSourceRole())
            return modifiers;
        if ((modifiers & AccPrivate) != 0)
        {
            modifiers ^= AccPrivate;
            if (!hasClassKindProblem(type.binding))
            	scope.problemReporter().illegalModifierForRole(type.binding);

        } else if ((modifiers & (AccPublic | AccProtected)) == 0)
        {
            // default
            modifiers |= AccProtected;
            if (!hasClassKindProblem(type.binding))
            	scope.problemReporter().illegalModifierForRole(type.binding);
        }
        // TODO (SH): can we avoid AccTeam here?
        if ((modifiers & AccTeam) == 0)
        {
        	if (   type.memberTypes != null
        		&& type.memberTypes.length > 0)
        	{
                if (!hasClassKindProblem(type.binding))
                	scope.problemReporter().missingTeamForRoleWithMembers(type.binding, type.memberTypes[0]);
        		// avoid secondary errors
                if (!type.isInterface()) { // setting AccTeam for interface would aggravate the situation
                	modifiers |= AccTeam;
                	type.modifiers |= AccTeam;
                }
        		type.binding.tagBits |= TagBits.HasClassKindProblem;
        		type.getTeamModel(); // initialize
        		for (int i = 0; i < type.memberTypes.length; i++) {
					type.memberTypes[i].modifiers |= AccRole;
					type.memberTypes[i].enclosingType = type;
					RoleModel roleModel = type.memberTypes[i].getRoleModel();
					if (!type.memberTypes[i].isRoleFile())
						roleModel.setState(ITranslationStates.STATE_ROLE_FILES_LINKED); // manually catch up this one CUD state, nothing to be done.
					Dependencies.ensureRoleState(roleModel, ITranslationStates.STATE_ROLES_SPLIT);
        		}
        	}
        }
        if ((modifiers & AccStatic) != 0 && (modifiers & AccRole) != 0)
        {
            if (!hasClassKindProblem(type.binding))
            	scope.problemReporter().staticRole(type.binding);
        }
        return modifiers;
    }

    public static boolean hasClassKindProblem(ReferenceBinding binding) {
    	if ((binding.tagBits & TagBits.HasClassKindProblem) != 0)
    		return true;
    	if (binding.enclosingType() != null)
    		return hasClassKindProblem(binding.enclosingType());
    	return false;
    }

    public static boolean isRoleInterfaceMethod(MethodBinding inheritedMethod) {
        int classModifiers = inheritedMethod.declaringClass.modifiers;
        return ((classModifiers & AccSynthIfc) == AccSynthIfc);
    }

    /**
     * @param modifiers
     * @param abstractMethods
     * @return are all abstract methods element of a synthetic role interface?
     */
    public static boolean checkRoleIfcVisibility(int modifiers, MethodBinding[] abstractMethods) {
        for (int i=0; i<abstractMethods.length; i++)
        {
        	MethodModel model = abstractMethods[i].model;
        	if (model != null && model.problemDetail == ProblemDetail.RoleInheritsNonPublic)
        		continue; // these methods are not public either
            ReferenceBinding ifc = abstractMethods[i].declaringClass;
            if ((ifc.modifiers & AccSynthIfc) != AccSynthIfc)
                return false;
        }
        return true;
    }

    public static boolean isAsVisible(int modifiers, int inheritedModifiers) {
    // mainly stolen from MethodVerifier.isAsVisible
        if ((inheritedModifiers & AccVisibilityMASK) ==
            (modifiers & AccVisibilityMASK))
        {
            return true;
        }

        if ((modifiers & AccPublic) != 0) return true;      // Covers everything
        if ((inheritedModifiers & AccPublic) != 0) return false;

        if ((modifiers & AccProtected) != 0) return true;
        if ((inheritedModifiers & AccProtected) != 0) return false;

        return (modifiers & AccPrivate) == 0; // The inherited thing cannot be private since it would not be visible
    }

    /**
     * Printable representation of visibility modifiers
     * @param modifiers
     * @return the string representation of modifiers
     */
    @SuppressWarnings("nls")
	public static String toString(int modifiers) {
        if ((modifiers & AccPublic)    != 0) return "public";
        if ((modifiers & AccProtected) != 0) return "protected";
        if ((modifiers & AccPrivate)   != 0) return "private";
        return "<default visibility>";
    }

    /**
     * Compute visibility of a feature from outside its class.
     * Handles visibility and abstractness.
     * @param classMod
     * @param featureMod must not be private
     * @return the combined bitset
     */
    public static int combine(int classMod, int featureMod) {
        int abstractness = (classMod & AccAbstract) | (featureMod & AccAbstract);
        int visibility = 0;

        if ((featureMod & AccPrivate) != 0) throw new InternalCompilerError("precond violated"); //$NON-NLS-1$
        if ((classMod & AccPrivate) != 0) {
            visibility = classMod & AccVisibilityMASK;
        } else if ((classMod & (AccPublic|AccProtected|AccPrivate)) == 0) {
            visibility = 0;
        } else if ((classMod & AccProtected) != 0) {
            if ((featureMod & (AccProtected|AccPublic)) != 0)
                visibility = AccProtected;
            else
                visibility = 0;
        } else {  // class is public
            visibility = featureMod & AccVisibilityMASK;
        }
        return visibility | abstractness;
    }

    /**
     * For checking default visibility try to identify requiredType as a
     * super-interface of startType, but never leaving requiredPackage.
     * @param startType
     * @param requiredType
     * @param requiredPackage
     * @return whether or not a supertype path could be found
     */
    public static boolean findSuperIfcInPackage(
            ReferenceBinding startType,
            ReferenceBinding requiredType,
            PackageBinding   requiredPackage)
    {
        if (startType.fPackage != requiredPackage) return false;
        if (startType == requiredType) return true;
        ReferenceBinding[] superIfcs = startType.superInterfaces();
        if (superIfcs == null) return false;
        for (int i=0; i<superIfcs.length; i++) {
            if (findSuperIfcInPackage(superIfcs[i], requiredType, requiredPackage))
                return true;
        }
        return false;
    }

    /**
     * TODO (SH): find a better place: will we have a class Validity?
     *
     * Check whether other has no more enclosing types than binding.
     * @param binding
     * @param other
     */
    public static boolean checkCompatibleEnclosingForRoles(
            Scope             scope,
            TypeDeclaration   clazz,
            SourceTypeBinding binding,
            ReferenceBinding  other)
    {
        if (!binding.isRole())
            return true;
        if (binding.isTeam()) {
        	ReferenceBinding enclosing = binding.enclosingType();
        	while (enclosing != null) {
        		if (enclosing == other) {
        			scope.problemReporter().teamExtendingEnclosing(clazz, other);
        			return false;
        		}
        		enclosing = enclosing.enclosingType();
        	}
        }
        ReferenceBinding otherEnclosing = other.original().enclosingType();
        if (otherEnclosing == null)
            return true;
        ReferenceBinding thisEnclosing = binding.enclosingType();
        while (thisEnclosing != null) {
            if (thisEnclosing.isCompatibleWith(otherEnclosing))
                return true; // good!
            thisEnclosing = thisEnclosing.enclosingType();
        }
        scope.problemReporter().extendIncompatibleEnclosingTypes(
                clazz, other, otherEnclosing);
        return false;
    }

	/**
	 * Visibility control for role fields/methods. The public case is already handled outside.
	 */
	public static boolean canBeSeenBy(IProtectable binding, TypeBinding receiverType, InvocationSite invocationSite, Scope scope)
	{
		// never legal for non-public features:
		if (RoleTypeBinding.isRoleWithExplicitAnchor(receiverType))
			return false;

		// callin methods are not subject to visibility control:
		if ((binding.modifiers() & ExtraCompilerModifiers.AccCallin) != 0)
			return true;

		ReferenceBinding declaringClass = (ReferenceBinding) binding.getDeclaringClass().getRealClass().erasure();
		ReferenceBinding invocationType = scope.enclosingSourceType();

		// special privilege for tsuper calls:
		if (invocationSite instanceof TSuperMessageSend && invocationType.isRole()) {
			// check all tsupers, but not mixing with explicit supers
			if (invocationType.roleModel.hasTSuperRole(declaringClass))
				return true;
		}
		
		if (binding.isProtected()) {
			// answer true if the invocationType is the declaringClass or they are in the same package
			// OR the invocationType is a subclass of the declaringClass
			//    AND the receiverType is the invocationType or its subclass
			//    OR the method is a static method accessed directly through a type
			//    OR previous assertions are true for one of the enclosing type
			if (invocationType == declaringClass) return true;

			// instead of package investigate the enclosing team.

			if (receiverType instanceof ReferenceBinding) {
				// strengthen all role types relative to the receiver type.
				ReferenceBinding receiver = (ReferenceBinding)receiverType;
				declaringClass = (ReferenceBinding)TeamModel.strengthenRoleType(receiver, declaringClass);
				if (invocationType.isRole())
					invocationType = (ReferenceBinding)TeamModel.strengthenRoleType(receiver, invocationType);
			}

// START orig code from FieldBinding:
			ReferenceBinding currentType = invocationType;
			int depth = 0;
			do {
/*OT:*/			if (TeamModel.isTeamContainingRole(currentType, declaringClass))
/*OT:*/				return true;
				if (declaringClass.isSuperclassOf(currentType)) {
					if (invocationSite.isSuperAccess()){
						return true;
					}
					// receiverType can be an array binding in one case... see if you can change it
					if (receiverType instanceof ArrayBinding){
						return false;
					}
					if (binding.isStatic()){
						if (depth > 0) invocationSite.setDepth(depth);
						return true; // see 1FMEPDL - return invocationSite.isTypeAccess();
					}
					if (currentType == receiverType || currentType.isSuperclassOf((ReferenceBinding)receiverType)){
						if (depth > 0) invocationSite.setDepth(depth);
						return true;
					}
				}
				depth++;
				currentType = currentType.enclosingType();
			} while (currentType != null);
			return false;
		}
// END orig code

		// default and private can be accessed from any nested class:

		// only invocationType is allowed to be nested within declaringClass,
		// not vice versa:
		ReferenceBinding currentInvocationType = invocationType;
		while(currentInvocationType != null) {
			if (currentInvocationType == declaringClass)
				return true;
			currentInvocationType = currentInvocationType.enclosingType();
		}

		// no more hope for private
		if (binding.isPrivate())
			return false;

		// default visibility (similar to FieldBinding.canBeSeenBy, whith less emphasis on packages):
		// receiverType can be an array binding in one case... see if you can change it
		if (receiverType instanceof ArrayBinding)
			return false;

		// have access to all super features:
		// (implicit inheritance is already dealt with by copy inheritance ;-) )
		if (invocationSite.isSuperAccess())
			invocationType = invocationType.superclass();

		ReferenceBinding currentType = (ReferenceBinding) (invocationType.isInterface()
										?((ReferenceBinding)receiverType).getRealType().erasure()
										:((ReferenceBinding)receiverType).getRealClass().erasure());
		PackageBinding declaringPackage = declaringClass.fPackage;
		do {
			if (invocationType == currentType) return true;
			if (   !currentType.isRole()						// when leaving team contexts ...
				&& declaringPackage != currentType.fPackage)    // ... compare the packages instead.
				return false;
		} while ((currentType = currentType.superclass()) != null);
		return false;
	}
}
