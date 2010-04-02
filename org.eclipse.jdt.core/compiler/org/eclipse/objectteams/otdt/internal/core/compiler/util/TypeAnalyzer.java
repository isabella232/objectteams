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
 * $Id: TypeAnalyzer.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;


import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.LiftingTypeReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * Utility class for analyzing, and generating types.
 * Handles bindings, names and references.
 *
 * @author stephan <i>stephan@cs.tu-berlin.de</i>
 * @version $Id: TypeAnalyzer.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class TypeAnalyzer  {

    public static final int EXACT_MATCH_ONLY        = 1;
    public static final int NEEDING_ADJUSTMENT_ONLY = 2;
    public static final int ANY_MATCH               = 3;


    /**
     * Given a method in a super Team, check wether the method declaration
     * from the sub Team is "the same method", which means, that role type
     * adjustments are taken into consideration.
     * These kinds of comparison are supported:
     * <dt>
     * <dt>EXACT_MATCH_ONLY
     *     <dd> these mathods are the same even in plain Java
     * <dt>NEEDING_ADJUSTMENT_ONLY
     *     <dd> only answer "true" if any parameter needs weakening
     * <dt>ANY_MATCH
     *     <dd> answer "true" if either kind of match is encountered.
     * </dt>
     * @param superTeam
     * @param superMeth
     * @param subTeam
     * @param subMeth
     * @param matchKind one of the above constants.
     * @return the answer
     */
    public static boolean isEqualMethodSignature(
            ReferenceBinding superTeam,
            MethodBinding    superMeth,
            ReferenceBinding subTeam,
            MethodBinding    subMeth,
            int              matchKind)
    {

        if(subMeth.parameters.length != superMeth.parameters.length)
            return false;
        TypeBinding[] params = subMeth.parameters;
        boolean needAdjustment = false;
        for (int i = 0; i < params.length; i++) {
            TypeBinding param = params[i];
            TypeBinding superTypeBind = superMeth.parameters[i];
            if (param == superTypeBind) continue;
            if (areTypesMatchable(param, subTeam, superTypeBind, superTeam, matchKind))
            {
                needAdjustment = true;
            } else {
                return false;
            }

        }
        if (matchKind == NEEDING_ADJUSTMENT_ONLY)
            return needAdjustment;
        return true;
    }

    /**
     * Answer, whether two reference bindings are views of the same role
     * possibly seen from different teams.
     */
    public static boolean isSameRole(ReferenceBinding r1, ReferenceBinding r2)
    {
        if (r1 == r2)
            return true;
        if (r1 == null || r2 == null)
        	return false;
        if (! (r1.isRole() && r2.isRole()))
            return false;
        ReferenceBinding t1 = r1.enclosingType();
        ReferenceBinding t2 = r2.enclosingType();
        if (t1 == t2 || t1 == null || t2 == null)
            return false; // not two different teams: no other chance..
        if (! (t1.isCompatibleWith(t2) || t2.isCompatibleWith(t1)))
            return false; // not sub/super teams.
        return CharOperation.equals(r1.internalName(), r2.internalName());
    }

    /** does role refine the extends clause of its tsuper role? */
    public static boolean refinesExtends (ReferenceBinding role, ReferenceBinding tsuperRole)
    {
    	return !isSameRole(
				role.superclass(),
				tsuperRole.superclass());
    }
    /**
     * Answer, whether two type are the same, when regarding a class and an
     * interface part of a role as identical.
     */
    public static boolean isSameType(ReferenceBinding site, TypeBinding t1, TypeBinding t2)
    {
        if (t1 == t2) return true;
        if (t1 == null || t2 == null) 
        	return false;
        if (t1.isArrayType()) {
            if (   !t2.isArrayType()
                || (t1.dimensions() != t2.dimensions()))
                return false;
            t1 = t1.leafComponentType();
            t2 = t2.leafComponentType();
        }
        else
        if (t2.isArrayType())
        {
        	return false;
        }

        if (t1.isBaseType())
            return t1 == t2;
        if (t2.isBaseType())
            return false;
        ReferenceBinding r1 = (ReferenceBinding)t1;
        ReferenceBinding r2 = (ReferenceBinding)t2;
        if (r1.isDirectRole() && r2.isDirectRole()) {
        	r2 = (ReferenceBinding)TeamModel.strengthenRoleType(site, r2);
            return r1.roleModel.getInterfacePartBinding() ==
                   r2.roleModel.getInterfacePartBinding();
        }
        return false;
    }
    /**
     * Are type compatible possibly performing role type adjustment?
     * @param currentType
     * @param subTeam
     * @param tsuperType
     * @param superTeam
     * @param matchKind
     * @return the answer
     */
    public static boolean areTypesMatchable(
            TypeBinding      currentType,
            ReferenceBinding subTeam,
            TypeBinding      tsuperType,
            ReferenceBinding superTeam,
            int              matchKind)
    {
    	// be very defensive:
    	if (currentType instanceof ProblemReferenceBinding) {
    		TypeBinding closestMatch = ((ProblemReferenceBinding)currentType).closestMatch();
    		if (closestMatch == null)
    			return CharOperation.equals(currentType.sourceName(), tsuperType.sourceName());
			currentType = closestMatch;
    	}
    	if (tsuperType instanceof ProblemReferenceBinding) {
    		TypeBinding closestMatch = ((ProblemReferenceBinding)tsuperType).closestMatch();
    		if (closestMatch == null)
    			return CharOperation.equals(currentType.sourceName(), tsuperType.sourceName());
			tsuperType = closestMatch;
    	}

        // if it's array-types, compare dimension and extract leafComponentType
        if ((currentType instanceof ArrayBinding) || (tsuperType instanceof ArrayBinding))
        {
            if (!(tsuperType  instanceof ArrayBinding) ||
                !(currentType instanceof ArrayBinding))
                return false;
            ArrayBinding currentArray = (ArrayBinding)currentType;
            ArrayBinding superArray   = (ArrayBinding)tsuperType;
            if (currentArray.dimensions() != superArray.dimensions())
                return false;
            currentType = currentArray.leafComponentType();
            tsuperType  = superArray.leafComponentType();
        }

        // guaranteed to be scalar now
        if (currentType instanceof ReferenceBinding)
        {
            if (DependentTypeBinding.isDependentType(currentType))
                currentType = ((DependentTypeBinding)currentType).getRealType();
            if (! (tsuperType instanceof ReferenceBinding))
                return false;
            if (tsuperType instanceof DependentTypeBinding)
                tsuperType = ((DependentTypeBinding)tsuperType).getRealType();
            if (currentType.isParameterizedType() || tsuperType.isParameterizedType()) {
            	// at least one type parameterized: get erasure(s)
            	if (currentType.isParameterizedType()) {
	        		if (tsuperType.isParameterizedType()) {
    					// both parameterized: check parameters:
		            	ParameterizedTypeBinding currentParameterized = ((ParameterizedTypeBinding)currentType);
	                	ParameterizedTypeBinding tsuperParameterized = ((ParameterizedTypeBinding)tsuperType);
		            	if (!CharOperation.equals(currentParameterized.genericTypeSignature,
		            							  tsuperParameterized.genericTypeSignature))
		            		return false;
            		} else if (!tsuperType.isRawType()) {
            			return false; // mismatch generic vs. non-generic
            		}

            	} else if (!currentType.isRawType()) {
            		return false; // mismatch non-generic vs. generic
            	}
				currentType = currentType.erasure();
				tsuperType  = tsuperType.erasure();
            }
            if (currentType.isTypeVariable() && tsuperType.isTypeVariable())
            	return currentType == tsuperType;

            char[][] tname1 = compoundNameOfReferenceType((ReferenceBinding)tsuperType, true, true);
            char[][] tname2 = compoundNameOfReferenceType((ReferenceBinding)currentType, true, true);
            if (CharOperation.equals(tname1, tname2) && (tsuperType != currentType))
                throw new InternalCompilerError("different bindings for the same type??"+currentType+':'+tsuperType); //$NON-NLS-1$

            if (CharOperation.equals(tname1,tname2))
                return true;
            if  (matchKind == EXACT_MATCH_ONLY)
            {
                return false;
            } else {
                tname1 = splitTypeNameRelativeToTeam((ReferenceBinding)tsuperType, superTeam);
                tname2 = splitTypeNameRelativeToTeam((ReferenceBinding)currentType, subTeam);
                if (!CharOperation.equals(tname1, tname2))
                    return false;
            }
        } else if (currentType instanceof BaseTypeBinding) {
            if (currentType != tsuperType)
                return false;
        } else {
            throw new InternalCompilerError("matching of unexpected type kind: "+currentType); //$NON-NLS-1$
        }
        return true;
    }


    /**
     * Extract a compound type name relativ to a given Team.
     * This means, if tb is a (transitiv) member type of team,
     * cut off the Team and return only remaining part.
     * If tb is not a (transitiv) member of team, return the
     * full compound name of outer/inner classes but not containing
     * the package.
     *
     * For features inherited from an indirect super team allow
     * the team prefix to relate to that super team.
     *
     * @param tb the type to represent
     * @param teamBinding the Team
     * @return non-null array of at least one component
     */
    public static char[][] splitTypeNameRelativeToTeam(ReferenceBinding tb, TypeBinding teamBinding) {
        char [][] qname = new char[][] {tb.internalName()};
        // move out until we find the source team:
        while (tb instanceof MemberTypeBinding) {
            tb = ((MemberTypeBinding)tb).enclosingType;
            if (tb.isTeam()) {
            	if (tb == teamBinding)
            		return qname;
            	else if (teamBinding.isCompatibleWith(tb))
            		return qname;
            } else {
                // prepend one component to qname:
                char[][] qn2 = new char[qname.length+1][];
                System.arraycopy(qname, 0, qn2, 1, qname.length);
                qn2[0] = tb.internalName();
                qname = qn2;
            }
        }

        return qname;
    }

    /**
     * Split the qualified name of a member type into a compound name.
     * (the field compoundName merges Outer$Inner into one element
     *  of the compound, which is not what we want here).
     * @param tb
     * @param includePackage should the package name(s) be included?
     * @param createTeamAnchor should a possible team anchor be included in the compound name?
     *        TODO(SH) saying yes here causes as to create old syntax AST.
     * @return non-null array of at least one component
     */
    public static char[][] compoundNameOfReferenceType(
            ReferenceBinding tb,
            boolean includePackage,
            boolean createTeamAnchor)
    {
    	if (tb instanceof ProblemReferenceBinding) {
    		ReferenceBinding closestMatch = (ReferenceBinding) tb.closestMatch();
    		if (closestMatch != null)
    			tb = closestMatch;
    	}
    	if (!tb.isValidBinding()) { // no further processing possible
    		if (includePackage)
    			return tb.compoundName;
    		int l = tb.compoundName.length;
    		return new char[][]{tb.compoundName[l-1]};
    	}
        if (   createTeamAnchor
        	&& DependentTypeBinding.isDependentType(tb)
           	&& ((DependentTypeBinding)tb).hasExplicitAnchor())
        {
        	DependentTypeBinding roleTypeBinding = (DependentTypeBinding)tb;

        	// for role types the prefix is a variable not a type:
        	ITeamAnchor[] path = roleTypeBinding._teamAnchor.getBestNamePath();

        	// If anchor is a field, repend a team anchor with "Outer.this"
        	// for the type containing the anchor field.
        	char[] declaringClass = null;
        	int prefixLen = 0;
        	if (roleTypeBinding._teamAnchor instanceof FieldBinding) {
        		declaringClass = ((FieldBinding)(roleTypeBinding)._teamAnchor).declaringClass.internalName();
        			prefixLen = 2;
        	}
        	char[][] names = new char[path.length+1+prefixLen][];
        	if (declaringClass != null) {
        			// do prepend
	        	names[0] = declaringClass;
        		names[1] = "this".toCharArray(); //$NON-NLS-1$
        	}
        	for (int i = 0; i < path.length; i++) {
				names[i+prefixLen] = path[i].internalName();
			}
        	names[path.length+prefixLen] = tb.internalName();

        	return names;
        }
        char[][] packName = includePackage && (tb.getPackage() != null) ?
        			tb.getPackage().compoundName :
                    new char[0][];
        char[][] outerName = (tb.enclosingType() != null) ?
        			compoundNameOfReferenceType(tb.enclosingType(), false, createTeamAnchor) :
        	        new char[0][];
        char[][] result = new char[packName.length+outerName.length+1][];
        System.arraycopy(packName, 0, result, 0, packName.length);
        System.arraycopy(outerName, 0, result, packName.length, outerName.length);
        result[result.length-1] = tb.internalName();
        return result;
    }

    /**
     * Try to interpret a type name as a local type of a role.
     * Take the constantPoolName and chop off the team name which prefixes this name.
     *
     * @param teamBinding
     * @param compoundName
     * @return non-null char-array
     */
    public static char[] constantPoolNameRelativeToTeam(ReferenceBinding teamBinding, char[]compoundName) {
    	char[] teamName = teamBinding.constantPoolName();
    	if (!CharOperation.prefixEquals(teamName, compoundName))
    		return compoundName;
    	assert (compoundName[teamName.length] == '$');
    	return CharOperation.subarray(compoundName, teamName.length+1, -1);
    }

    /**
     * Compare two types trying to interpret type names as local types of roles, where
     * the constantPoolName must be investigated but the team name prefix chopped off.
     *
     * @param teamBinding enclosing team of the first type
     * @param role the first type itself
     * @param typeName relative name of the second type (ie., team prefix is already copped off).
     * @return the answer
     */
    public static boolean equalRoleLocal(ReferenceBinding teamBinding, ReferenceBinding role, char[] typeName)
    {
    	// TODO (SH) during CopyInheritanc constantPoolName is still null.
    	//           but do we need to map names of local types in accessor signatures??
    	if (role.constantPoolName() == null)
    		return false;
    	char[] relativeName = constantPoolNameRelativeToTeam(teamBinding, role.constantPoolName());
    	return CharOperation.equals(typeName, relativeName);
    }
    /**
     * Given a reference (read from source code) and a binding (read from
     * a resolved super-role) create a weakened type reference, i.e., if
     * the type is a role-type, create a new reference from the binding.
     * Precondition: both types are identical except for implicit inheritance.
     * @param origRef This reference may or may not need to be changed.
     *  In any case use its source positions.
     * @param binding this specifies the type we need to refer to.
     * @return either origRef or a fresh reference
     */
    public static TypeReference weakenTypeReferenceFromBinding(
            MethodScope scope,
            TypeReference origRef,
            TypeBinding binding)
    {
        if (!(   (binding instanceof ReferenceBinding)
        	  || (binding instanceof ArrayBinding)))
            return origRef;
        TypeBinding origBind = getType(origRef, scope).erasure();
        binding = binding.erasure();
        if (binding instanceof RoleTypeBinding)
        	if (origBind == ((RoleTypeBinding)binding).getRealType())
        		return origRef;
        if (origBind == binding)
            return origRef;
        AstGenerator gen = new AstGenerator(origRef.sourceStart, origRef.sourceEnd);
        return gen.typeReference(binding);
    }

    /**
     * Find a method in a super role
     * @param subTeam the Team containing subMethod
     * @param subMethod a method to match
     * @param superRole where to look
     * @param superTeam enclosing Team of superRole
     * @return a method or null
     */
    public static MethodBinding findMethodInSuperRole(
            ReferenceBinding subTeam,   MethodBinding subMethod,
            ReferenceBinding superRole, ReferenceBinding superTeam,
            int matchKind)
    {
        MethodBinding[] superMethods =superRole.methods();
        for (int i=0; i<superMethods.length; i++)
        {
            if (isEqualMethodSignature(superTeam, superMethods[i], subTeam, subMethod, matchKind))
                return superMethods[i];
        }
        return null;
    }

    // helper for weakenTypeReferenceFromBinding
    private static TypeBinding getType (TypeReference ref, MethodScope scope)
    {
    	TypeBinding result;
        if (ref instanceof SingleTypeReference)
            result = scope.getType(((SingleTypeReference)ref).token);
        else if (ref instanceof QualifiedTypeReference) {
            char[][] tokens = ((QualifiedTypeReference)ref).tokens;
			result = scope.getType(tokens, tokens.length);
        } else if (ref instanceof LiftingTypeReference)
        	return ref.resolvedType; // this is the base type, no need for weakening ;-)
        else
        	throw new InternalCompilerError("Unexpected type reference "+ref.toString()); //$NON-NLS-1$
        if (ref.dimensions() > 0)
        	result = scope.createArrayType(result, ref.dimensions());
        return result;
    }

    public static boolean isOrgObjectteamsTeam(ReferenceBinding type) {
    	if (type == null)
    		return false;
        return CharOperation.equals(type.compoundName, IOTConstants.ORG_OBJECTTEAMS_TEAM);
    }

    public static boolean isOrgObjectteamsTeam(CompilationUnitDeclaration unit) {
    	if (   unit != null
    		&& unit.currentPackage != null
    		&& unit.types != null
    		&& unit.types.length > 0) {
    		return     CharOperation.equals(IOTConstants.ORG_OBJECTTEAMS, unit.currentPackage.tokens)
    				&& CharOperation.equals(IOTConstants.TEAM, unit.types[0].name);
    	}

    	return false;
    }

    public static boolean isVariableRef (Expression e) {
        return (e.bits & Binding.VARIABLE) != 0;
    }

	/**
	 * Find a fied in a type searching superclasses and enclosing classes.
	 * Note, that this method indeed finds private fields even via a super-class of type.
	 *
	 * @param type where to look
	 * @param token name of the field to look for
	 * @param isStaticScope is the field reference within a static scope?
	 * @param allowOuter is it legal to find the field in an enclosing type?
	 * @param requiredState when navigating to supertypes, is a certain state required for those?
	 *        (this does not apply to `type' itself and its enclosing).
	 * @return a field or null
	 */
	public static FieldBinding findField(
			ReferenceBinding type,
			char[]           token,
			boolean  		 isStaticScope,
			boolean          allowOuter,
			int              requiredState)
	{
		while (type != null) {           // loop inner->outer
			ReferenceBinding currentType = type;
			while (currentType != null) { // loop sub->super
				FieldBinding foundVar = currentType.getField(token, /*resolve*/true);
				if (foundVar != null) {
					if (isStaticScope && ! foundVar.isStatic())
						return new ProblemFieldBinding(foundVar, currentType, token, ProblemReasons.NonStaticReferenceInStaticContext);
					return foundVar;
				}
				currentType = currentType.superclass();
				if (currentType != null && requiredState != -1)
					Dependencies.ensureBindingState(currentType, requiredState);
			}
			if (!allowOuter)
				return null;
			isStaticScope = type.isStatic(); 
			type = type.enclosingType();
		}
		return null;
	}

	public static FieldBinding findField(ReferenceBinding type,
										 char[]           token,
										 boolean 		  isStaticScope,
										 boolean          allowOuter)
	{
		return findField(type, token, isStaticScope, allowOuter, -1);
	}
	/**
	 * Find a method in type or one of its super types.
	 * @param scope where the method shall be invoked from
	 * @param type  where to search
	 * @param selector name of the method
	 * @param params params
	 */
	public static MethodBinding findMethod(
			Scope scope,
			ReferenceBinding type,
			char[] selector,
			TypeBinding[] params)
	{
		return findMethod(scope, type, selector, params, /*decapsulationAllowed*/false);
	}
	/**
	 * Find a method in type or one of its super types.
	 * @param scope where the method shall be invoked from
	 * @param type  where to search
	 * @param selector name of the method
	 * @param params params
	 * @param decapsulationAllowed whether or not invisible methods should be found, too
	 */
	public static MethodBinding findMethod(
			Scope 			 scope,
			ReferenceBinding type,
			char[] 			 selector,
			TypeBinding[] 	 params,
			boolean          decapsulationAllowed)
	{
		if (type == null || scope == null)
			return null;
		FieldReference invocationSite = new FieldReference("dummy".toCharArray(), 0); //$NON-NLS-1$
		if (decapsulationAllowed)
			invocationSite.setBaseclassDecapsulation(DecapsulationState.ALLOWED);
		invocationSite.receiver = new ThisReference(0,0);
		invocationSite.actualReceiverType = invocationSite.receiver.resolveType(scope.classScope()); // method scope could be static context!
		return scope.getMethod(type, selector, params, invocationSite);
	}

	/** Find a method with identical parameters after erasing. */
	public static MethodBinding findCompatibleMethod(ReferenceBinding site, MethodBinding template)
	{
		methods:
		for (MethodBinding existingMethod : site.getMethods(template.selector))
		{
			if (existingMethod.parameters.length != template.parameters.length)
				continue;
			for (int j = 0; j < template.parameters.length; j++) {
				if (existingMethod.parameters[j].erasure() != template.parameters[j].erasure())
					continue methods;
			}
			return existingMethod;
		}
		return null;
	}
	/**
	 * Look for the method declartion matching the given selector and length of argument list.
	 * @param typeDeclaration
	 * @param selector
	 * @param nArgs
	 * @return a method or null
	 */
	public static AbstractMethodDeclaration findMethodDecl(
			TypeDeclaration typeDeclaration,
			char[]          selector,
			int             nArgs)
	{
		if (typeDeclaration.methods == null)
			return null;
		AbstractMethodDeclaration[] methods = typeDeclaration.methods;
		for (int i=0; i<methods.length; i++) {
			if (CharOperation.equals(methods[i].selector, selector))
			{
				if (methods[i].arguments == null) {
					if (nArgs == 0)
						return methods[i];
				} else if (methods[i].arguments.length == nArgs) {
					return methods[i];
				}
			}
		}
		return null;
	}

	/**
	 * Find a method just by its selector.
	 * Looks up the superclass/interfaces hierarchy.
	 *
	 * @param typeBinding
	 * @param selector
	 * @return first matching method
	 */
	public static MethodBinding getMethod(ReferenceBinding typeBinding, char[] selector) {
		if (typeBinding == null)
			return null;
		MethodBinding[] foundMethods = typeBinding.getMethods(selector);
		if (foundMethods != Binding.NO_METHODS)
			return foundMethods[0];
		MethodBinding foundMethod = getMethod(typeBinding.superclass(), selector);
		if (foundMethod != null)
			return foundMethod;
		ReferenceBinding[] superInterfaces = typeBinding.superInterfaces();
		for (int i = 0; i < superInterfaces.length; i++) {
			foundMethod = getMethod(superInterfaces[i], selector);
			if (foundMethod != null)
				return foundMethod;
		}
		return null;
	}

	/**
	 * When comparing two role types, allow the teams to be super/sub teams
	 * in either direction.
	 *
	 * TODO (SH): check:
	 * (1) does this method hide relevant incompatibilities?
	 * (2) do we need to apply this method in other places, too?
	 *
	 * @param one
	 * @param two
	 * @return the answer
	 */
	public static boolean areRoleTypesEqual(
			RoleTypeBinding one,
			RoleTypeBinding two)
	{
		if (!CharOperation.equals(one.sourceName(), two.sourceName()))
			return false;
		ReferenceBinding teamOne = one._staticallyKnownTeam;
		ReferenceBinding teamTwo = two._staticallyKnownTeam;
		if (teamOne.isRole() && teamTwo.isRole()) {
			ReferenceBinding outerOne = teamOne.enclosingType();
			ReferenceBinding outerTwo = teamTwo.enclosingType();
			if (outerOne != outerTwo) {
				// raise nested teams to same level.
				if (outerOne.isCompatibleWith(outerTwo)) {
					teamTwo = (ReferenceBinding)TeamModel.strengthenRoleType(outerOne, teamTwo);
				} else if (outerTwo.isCompatibleWith(outerOne)) {
					teamOne = (ReferenceBinding)TeamModel.strengthenRoleType(outerTwo, teamOne);
				}
			}
			// in order to compare two team-as-role types take their interface parts:
			teamOne = teamOne.roleModel.getInterfacePartBinding();
			teamTwo = teamTwo.roleModel.getInterfacePartBinding();
		}
		if (teamOne.isCompatibleWith(teamTwo))
			return true;
		if (teamTwo.isCompatibleWith(teamOne))
			return true;
		return false;
	}
	/*
	 * This is the structure of confined types as seen by the compiler:
	 *
	 * predefined:
	 * IConfined          .superclass = null
	 * Team.IConfined     .superclass = null, superinterface = IConfined
	 * Team.Confined      .superclass = null
	 * Team.__OT__Confined.superclass = null, superinterface = Team.Confined
	 *
	 * client code:
	 * TX.IConfined       .superclass = OTC,  superinterface = Team.IConfined
	 * TX.Confined        .superclass = OTC,  superinterface = Team.Confined
	 * TX.__OT__Confined  .superclass = OTC,  superinterface = TX.Confined
	 * TX.R               .superclass = OTC,  superinterface = TX.IMyIFC
	 * TX.__OT__R         .superclass = OTC,  superinterface = TX.R
	 * (OTX = __OT__Confined, resolving to TX.__OT__Confined except for itself)
	 *
	 * Class-files have to differ from this view:
	 * For JVM-compatibility all superinterfaces must store Object as their superclass.
     * These interfaces are marked using OTClassFlags attribute.
	 *
	 */

	public static boolean isTopConfined(ReferenceBinding type) {
		char[][] compoundName= type.compoundName;
		if (    compoundName.length == 3
			&& (   CharOperation.equals(compoundName, IOTConstants.ORG_OBJECTTEAMS_ICONFINED)
				|| CharOperation.equals(compoundName, IOTConstants.ORG_OBJECTTEAMS_ITEAM_ICONFINED)
				|| CharOperation.equals(compoundName, IOTConstants.ORG_OBJECTTEAMS_TEAM_CONFINED)
				|| CharOperation.equals(compoundName, IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED)))
		{
			return true;
		}
		char[] name= type.internalName();
		return type.isRole()
		    && (   CharOperation.equals(name, IOTConstants.ICONFINED)
				|| CharOperation.equals(name, IOTConstants.OTCONFINED)
				|| CharOperation.equals(name, IOTConstants.CONFINED));
	}

	public static boolean isConfined(TypeBinding type) {
		if (type == null || !type.isRole()) return false;
		ReferenceBinding currentType= (ReferenceBinding)type;
		while (currentType != null) {
			if (currentType.id == TypeIds.T_JavaLangObject)
				return false;
			currentType = currentType.superclass();
		}
		return true;
	}
	public static boolean isPredefinedRole(ReferenceBinding type) {
		char[] name = type.internalName();
		if (CharOperation.equals(name, IOTConstants.ROFI_CACHE))
			return true;
		if (!type.isRole())
			return false;
		return     CharOperation.equals(name, IOTConstants.ICONFINED)
				|| CharOperation.equals(name, IOTConstants.OTCONFINED)
				|| CharOperation.equals(name, IOTConstants.CONFINED)
				|| CharOperation.equals(name, IOTConstants.ILOWERABLE)
				|| CharOperation.equals(name, IOTConstants.IBOUNDBASE);
	}

	public static boolean extendsOTConfined(TypeDeclaration type) {
		if (type.superclass instanceof QualifiedTypeReference)
			return CharOperation.equals(((QualifiedTypeReference)type.superclass).tokens,
									    IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED);
		if (type.superclass instanceof SingleTypeReference)
			return CharOperation.equals(((SingleTypeReference)type.superclass).token,
									    IOTConstants.OTCONFINED);
		return false;
	}

	/**
	 * When asking for a field should synthetic fields be searched?
	 * @param scope        site of usage, if this is a generated methods, searching synthetics is OK
	 * @param receiverType class containing the field
	 * @param fieldName
	 * @return the answer
	 */
	public static boolean isSearchingForSyntheticField(MethodScope scope, TypeBinding receiverType, char[] fieldName) {
		return CharOperation.prefixEquals(TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX, fieldName)
		    && scope != null
			&& ((AbstractMethodDeclaration)scope.referenceContext).isGenerated
			&& receiverType instanceof ReferenceBinding
			&& ((ReferenceBinding)receiverType).isRole();
	}

	public static boolean isSourceTypeWithErrors (ReferenceBinding type) {
		if (type instanceof SourceTypeBinding) {
			SourceTypeBinding sourceType = (SourceTypeBinding)type;
			if (sourceType.scope != null)
				return sourceType.scope.referenceContext.hasErrors();
		}
		return false;
	}

	// _OT$base.Types need stripping:
	public static char[] stripTypeName(char[] typeName) {
		if (CharOperation.prefixEquals(IOTConstants._OT_BASE, typeName))
			typeName = CharOperation.subarray(typeName, IOTConstants.OT_DOLLAR_LEN, -1);
		return typeName;
	}

	public static boolean sameOrContained(ReferenceBinding binding, ReferenceBinding targetEnclosingType)
	{
		while (binding != null) {
			if (binding == targetEnclosingType)
				return true;
			binding = binding.enclosingType();
		}
		return false;
	}
}
