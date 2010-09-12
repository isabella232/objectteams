/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamModel.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.compiler.Pair;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.RoleFileCache;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.BoundClassesHierarchyAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.RoleBaseBindingsAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.WordValueAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.LiftingEnvironment;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.OTClassScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.TThisBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.Protections;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;

/**
 * This class models properties of Teams.
 * One instance of this class can be allocated for each ClassDeclaration
 * and/or ClassBinding.
 *
 * Tasks:
 * <ul>
 *   <li>Navigate to all roles.
 * </ul>
 *
 * @author stephan
 */
public class TeamModel extends TypeModel {


	/** The Marker interface created for this Team (_TSuper__OT__TeamName); */
	public TypeDeclaration markerInterface;

    private RoleBaseBindingsAttribute roleBaseBindings = null;

    // pairs of base-role classes for which lifting would be ambiguous
    public List<Pair<ReferenceBinding, ReferenceBinding>> ambigousLifting = new ArrayList<Pair<ReferenceBinding,ReferenceBinding>>();

    /* store all role caches managed by this team. */
    public FieldDeclaration[] caches = new FieldDeclaration[0];

    /* the unique tthis binding for this team */
    private TThisBinding tthis; // will be initialized once _binding is set.

    /* a synthetic class for storing known role files */
    private RoleFileCache knownRoleFiles = null;

    /* While loading a role file no further role file may pass the former role in translation. */
    public boolean _blockCatchup = false;

	public boolean _isCopyingLateRole = false;

	public LiftingEnvironment liftingEnv = null;

	public TeamModel(TypeDeclaration teamAst)
	{
		super(teamAst);
		if (teamAst.enclosingType == null)
			addAttribute(WordValueAttribute.compilerVersionAttribute());
		if (Config.clientIsBatchCompiler())
			this.knownRoleFiles = new RoleFileCache(teamAst);
	}
	public TeamModel(ReferenceBinding teamBinding)
	{
		super(teamBinding);
		if (!teamBinding.isSynthInterface())
			// no tthis for nested team ifc-parts
			this.tthis = new TThisBinding(teamBinding);
		// TODO (SH): else part should be a InternalCompilerError?
		// cf. getTThis().
	}

    public void setBinding (ReferenceBinding binding) {
        this._binding = binding;
        binding.setTeamModel(this);
		if (!binding.isSynthInterface())
			// no tthis for nested team ifc-parts
			this.tthis = new TThisBinding(binding);
    }

    public void addCache(FieldDeclaration cache) {
    	int len = this.caches.length;
    	System.arraycopy(
    			this.caches, 0,
				this.caches = new FieldDeclaration[len+1], 0,
				len);
    	this.caches[len] = cache;
    }

    /** Get all other base types bound by this team where the bound roles are:
     * (a) compatible to role and
     * (b) not contained in explicitRoles
     * @param role
     * @param explicitRoles
     * @return list of ReferenceBinding
     */
    public Set<ReferenceBinding> getSubBases (RoleModel role, RoleModel[] explicitRoles) {
    	Set<ReferenceBinding> result = new HashSet<ReferenceBinding>();
    	ReferenceBinding baseBinding = role.getBaseTypeBinding();
    	// search bases via their roles to ensure compatibility:
    	bases: for (ReferenceBinding currentRole : this._binding.memberTypes()) {
    		ReferenceBinding currentBase = currentRole.baseclass();
    		if (currentBase == null) continue;
    		// don't report base types with ambiguity: can't lift:
    		for (Pair<ReferenceBinding, ReferenceBinding> ambig: this.ambigousLifting)
    			if (ambig.equals(currentBase, role.getBinding()))
    				continue bases;
    		if (!currentRole.isCompatibleWith(role.getBinding())) {
    			// role is unrelated, inspect most specific super class bound to this hierarchy:
    			if (generalizesTo(currentBase, baseBinding, explicitRoles))
        			result.add(currentBase);
    			continue;
    		}
			if (currentBase.isCompatibleWith(baseBinding)) {
				for (int i = 0; i < explicitRoles.length; i++) {
					if (explicitRoles[i].getBaseTypeBinding() == currentBase)
						continue bases;
				}

				result.add(currentBase);
			}
		}
    	return result;
    }
    /* Climbing up the hierarchy of alienBase check if we find baseBinding
     * before hitting a direct base of any of the excludedRoles. */
    private boolean generalizesTo(ReferenceBinding alienBase, ReferenceBinding baseBinding, RoleModel[] excludedRoles) {
		ReferenceBinding currentBase = alienBase;
		while (currentBase != null) {
			for (RoleModel explicitRole : excludedRoles) {
				if (currentBase == explicitRole.getBaseTypeBinding())
					return false; // covered by a different role
			}
			currentBase = currentBase.superclass();
			if (currentBase == baseBinding) {
				return true;
			}
		}
		return false;
    }

    public void addBoundClassLink(ReferenceBinding subClass, ReferenceBinding superClass) {
    	ensureRoleBaseBindingsAttribute();
    	BoundClassesHierarchyAttribute attribute = null;
    	for (int i = 0; i < this._attributes.length; i++) {
			if (this._attributes[i].nameEquals(IOTConstants.BOUND_CLASSES_HIERARCHY)) {
				attribute = (BoundClassesHierarchyAttribute)this._attributes[i];
				break;
			}
		}
    	if (attribute == null)
    		addAttribute(attribute = new BoundClassesHierarchyAttribute());
    	attribute.add(subClass.attributeName(), superClass.attributeName());
    }

    /**
     * Add a base class binding ready to write out as attribute.
     * @param roleName
     * @param baseName
     */
    public void addRoleBaseBindingAttribute(char[] roleName, char[] baseName, boolean baseIsInterface) {
    	ensureRoleBaseBindingsAttribute();
    	this.roleBaseBindings.add(roleName, baseName, baseIsInterface);
    }

    private void ensureRoleBaseBindingsAttribute() {
        if (this.roleBaseBindings == null) {
            this.roleBaseBindings = new RoleBaseBindingsAttribute();
            addAttribute(this.roleBaseBindings);
        }
	}
	/**
     * Set the given state for all contained roles.
     */
    public void setMemberState(int state)
    {
        RoleModel[] roles = getRoles(true);
        if (roles != null)
        {
            for (int i=0; i<roles.length; i++)
            {
                RoleModel role = roles[i];
                role.setMemberState(state);
                role.setState(state);
                if (   (role.getBinding() != null)
					&& (role.getBinding().isTeam()))
                {
                    TeamModel teamModel = role.getBinding().getTeamModel();
                    teamModel.setState(state);
                    teamModel.setMemberState(state);
                }
            }
        }
    }

    /**
     * Set the given state for all contained roles, but not for roles-as-teams
     */
    public void setMemberStateShallow(int state)
    {
        RoleModel[] roles = getRoles(true);
        if (roles != null)
        {
            for (int i=0; i<roles.length; i++)
            {
                RoleModel role = roles[i];
                role.setMemberState(state);
                role.setState(state);
            }
        }
    }

	public TeamModel getSuperTeam()
	{
        TypeBinding superBinding = null;
        if (this._ast != null)
		{
            if (this._ast.superclass != null)
                superBinding = this._ast.superclass.resolveType(this._ast.scope);
		} else {
            superBinding = this._binding.superclass();
		}
	    if (superBinding == null)
            return null;

		assert( superBinding.isClass() ); // then we can cast to ReferenceBinding

		return ((ReferenceBinding)superBinding).getTeamModel();
	}

	/**
     * Get all roles represented by their RoleModel.
     * Considers Ast or Bindings, whatever is more appropriate.
     * SH: used to ensure STATE_ROLE_INHERITANCE for this team, which is now
     *     moved to CalloutImplementor.getRoleModelForType() to make this
     *     method reusable.
 	 * Does not take external roles into account, yet.
     *
     * @returns all direct roles for this Team.
     */
	public RoleModel[] getRoles(boolean includeSynthInterfaces)
	{
		List<RoleModel> list = new LinkedList<RoleModel>();
		if(this._binding != null)
		{
			// if binding exists, it may contain reused binary roles
			// which are not present in _ast, so prefer binding.
			ReferenceBinding[] roleBindings = this._binding.memberTypes();
			for (int i = 0; i < roleBindings.length; i++)
            {
                ReferenceBinding binding = roleBindings[i];
                if (binding.isEnum())
                	continue;
                if(includeSynthInterfaces || binding.isDirectRole())
                {
                	if (binding.roleModel != null)
                		list.add(binding.roleModel);
                }
            }
		}
		else
		{

			TypeDeclaration[] roles = this._ast.memberTypes;
			if (roles != null)
			{
				for (int idx = 0; idx<roles.length; idx++)
				{
			  		if ((roles[idx].modifiers & ClassFileConstants.AccEnum) != 0)
			  			continue; // enums are not roles
					if (includeSynthInterfaces || roles[idx].isSourceRole())
					{
                		RoleModel roleModel = roles[idx].getRoleModel(this);
                		if (roleModel != null)
                			list.add(roleModel);
					}
				}
			}
		}
		return list.toArray(new RoleModel[list.size()]);
	}

	/** Answer the number of roles, binding(preferred) or ast. */
	public int getNumRoles() {
		if (this._binding != null)
			return this._binding.memberTypes().length;
		if (this._ast.memberTypes == null)
			return 0;
		return this._ast.memberTypes.length;
	}

	/** Try to re-interpret this team type as a role and answer its role model. */
	public RoleModel getRoleModelOfThis() {
		if (this._ast != null)
			return this._ast.getRoleModel();
		return this._binding.roleModel;
	}

    public boolean isTeam() {
        return true;
    }

    /** This Method includes the interface part of nested teams. */
    public static boolean isAnyTeam(ReferenceBinding type) {
    	if (type.isTeam())
    		return true;
		if (type.isRole()) {
	    	ReferenceBinding classPartBinding = type.roleModel.getClassPartBinding();
	    	if (classPartBinding != null)
	    		return classPartBinding.isTeam();
		}
    	return false;
    }

    /** This Method includes the interface part of nested teams.*/
	public static boolean isAnyTeam(TypeDeclaration typeDecl) {
		if (typeDecl.isTeam())
			return true;
		if (typeDecl.isRole() && typeDecl.isInterface())
		{
			TypeDeclaration classPart = typeDecl.getRoleModel().getClassPartAst();
			if (classPart != null)
				return classPart.isTeam();
		}
		return false;
	}

    protected String getKindString()
    {
        return "Team"; //$NON-NLS-1$
    }
	public TThisBinding getTThis() {
		if (this.tthis == null)
			throw new InternalCompilerError("no tthis for "+new String(getBinding().readableName())); //$NON-NLS-1$
		return this.tthis;
	}
	/**
	 * Find an enclosing type of nestedType that is a team.
	 * @param nestedType
	 * @return team or null
	 */
	public static ReferenceBinding getEnclosingTeam(ReferenceBinding nestedType) {
		ReferenceBinding outer = nestedType.enclosingType();
		while (outer != null) {
			if (outer.isTeam())
				return outer;
			outer = outer.enclosingType();
		}
		return null;
	}

	public static TypeDeclaration getOutermostTeam(TypeDeclaration type) {
		if (!type.isTeam())
			return getOutermostTeam(type.enclosingType);
		TypeDeclaration result = type;
		while (type != null && type.isTeam()) {
			result = type;
			type = type.enclosingType;
		}
		return result;
	}
	private static ReferenceBinding normalizeTeam(ReferenceBinding teamBinding) {
		if (teamBinding == null)
			return null;
	    if (teamBinding.isRole() && teamBinding.isSynthInterface())
	    	return teamBinding.roleModel.getClassPartBinding();
		return teamBinding;
	}
	/**
	 * Starting a site and moving outwards find a team that contains
	 * roleType or a tsub-role (ie., the resulting team may be a
	 * subteam of roleType's enclosing team.
	 *
	 * @param site
	 * @param roleType
	 * @return a team type.
	 */
	public static ReferenceBinding findEnclosingTeamContainingRole(
	        ReferenceBinding site,
	        ReferenceBinding roleType)
	{
		site = normalizeTeam(site);
	    while (site != null) {
	        if (TeamModel.isTeamContainingRole(site, roleType))
	            return site;
	        site = site.enclosingType();
	    }
	    return null;
	}

	/**
	 * Answer whether a team contains a given role (team may be more
	 * specific than the role's enclosing team).
	 *
	 * @param teamBinding the team containing the role or a subteam
	 * @param roleBinding
	 * @return the answer
	 */
	public static boolean isTeamContainingRole (
	        ReferenceBinding teamBinding,
	        ReferenceBinding roleBinding)
	{
	    return levelFromEnclosingTeam(teamBinding, roleBinding) != 0;
	}

	/**
	 * Try to interpret teamCandidate as an enclosing team of roleType.
	 *
	 * @param teamCandidate
	 * @param roleType
	 * @return the number of nesting levels that role type lies within teamCandidate, 0 if no match.
	 */
	public static int levelFromEnclosingTeam(
			ReferenceBinding teamCandidate,
			ReferenceBinding roleType)
	{
		int l = 1;
	    if (teamCandidate == null)
	        return 0;
	    teamCandidate = normalizeTeam(teamCandidate);
	    if (!teamCandidate.isTeam())
	    	return 0;
	    if (!roleType.isRole())
	    	return 0;
	    if (roleType.isParameterizedType())
	    	roleType = ((ParameterizedTypeBinding)roleType).genericType();
    	ReferenceBinding roleOuter = roleType.enclosingType();
	    if (teamCandidate.isRole()) {
	    	ReferenceBinding outerTeam = teamCandidate.enclosingType();
	    	int l2 = levelFromEnclosingTeam(outerTeam, roleOuter);
	    	if (l2 > 0) {
	    		if (l2 == 1)
	    			roleOuter = outerTeam.getMemberType(roleOuter.internalName());
	    		l = l2;
	    	}
	    	// else nested teamCandidate might extend a non-nested team.
	    }
	    ReferenceBinding member = teamCandidate.getMemberType(roleType.internalName());
	    if (member == null) {
	    	// several levels away, e.g., role nested (anonymous?):
	    	if (roleOuter.isRole()) {
	    		int l2 = levelFromEnclosingTeam(teamCandidate, roleOuter);
	    		if (l2 > 0)
	    			return l2 + 1;
	    	}
	    	return 0;
	    }
	    if (areTypesCompatible(teamCandidate, roleOuter))
	    	return l;
		return 0;
	}
	public static boolean areTypesCompatible(ReferenceBinding type1, ReferenceBinding type2)
	{
		if (type1.isRole() && type2.isRole()) {
			if (!type1.isInterface())
				type1 = type1.roleModel.getInterfacePartBinding();
			if (!type2.isInterface())
				type2 = type2.roleModel.getInterfacePartBinding();
		}
		return type1.isCompatibleWith(type2);
	}
	/**
	 * Are (direct role?) types comparable disregarding for the moment that their team instances
	 * need to be compared, too?
	 * Role nested types are not handled here, since they are not wrapped.
	 *
	 * (Used for checking whether a cast is legal and requires instance comparison).
	 * @param expressionType
	 * @param castType
	 * @return the answer
	 */
	public static boolean isComparableToRole(ReferenceBinding expressionType, ReferenceBinding castType) {
		if (!castType.isDirectRole())
			return false;
		return
			   castType.getRealType().isCompatibleWith(expressionType.getRealType())
			|| expressionType.getRealType().isCompatibleWith(castType.getRealType());
	}

	/**
	 * Is left compatible to right, or are both corresponding roles
	 * of compatible enclosing teams (recursively).
	 * (Used for checking compatible refinement of a role's superclass).
	 */
	public static boolean areCompatibleEnclosings(ReferenceBinding left, ReferenceBinding right)
	{
		if (areTypesCompatible(left, right))
			return true;
		if (!CharOperation.equals(left.sourceName(), right.sourceName()))
			return false;
		ReferenceBinding leftEnclosing = left.enclosingType();
		ReferenceBinding rightEnclosing = right.enclosingType();
		if (leftEnclosing == null || rightEnclosing == null)
			return false;
		if (leftEnclosing.isTeam() && rightEnclosing.isTeam())
			return areCompatibleEnclosings(leftEnclosing, rightEnclosing);
		return false;
	}

	/**
	 * Get the most suitable RoleTypeBinding for roleType in a tthis context defined by scope.
	 * Strengthening reverses the effect of signature weakening.
	 *
	 * (Used for determining the statically known role type for lifting)
	 *
	 * @param site     (guaranteed to be within the context of a team)
	 * @param roleType (guaranteed to be a role or an array thereof)
	 * @return found role - need not be a RoleTypeBinding
	 */
	public static TypeBinding strengthenRoleType (
			ReferenceBinding site,
			TypeBinding roleType)
	{
		ReferenceBinding enclosingTeam = site;
	    enclosingTeam = normalizeTeam(enclosingTeam);
		if (!enclosingTeam.isTeam())
			enclosingTeam = getEnclosingTeam(site);
		if (enclosingTeam == null)
			return roleType; // this site cannot strengthen the role type.
		if (roleType.isLocalType())
			return roleType;
		int dimensions = roleType.dimensions();
		ReferenceBinding roleRefType = (ReferenceBinding)roleType.leafComponentType();
		ReferenceBinding roleEnclosing = roleRefType.enclosingType();
		if (roleEnclosing.isRole() && roleEnclosing != site) {
			// first strengthen enclosing team if it is nested:
			ReferenceBinding strengthenedEnclosing = null;
			if (roleEnclosing.enclosingType() != enclosingTeam.enclosingType())
				strengthenedEnclosing = (ReferenceBinding)strengthenRoleType(site, roleEnclosing);
			if (strengthenedEnclosing != null && strengthenedEnclosing != site) {
				// we indeed found a better site, so start over:
				return strengthenRoleType(strengthenedEnclosing, roleType);
			}
		}
		// check success:
		if (!(   roleRefType.isRole()         							 // need a role
			  && areCompatibleEnclosings(enclosingTeam, roleEnclosing))) // teams must be compatible
		{
			if (enclosingTeam.isRole()) // try via outer team:
				return strengthenRoleType(enclosingTeam.enclosingType(), roleType);
			return roleType;
		}
		if (roleRefType instanceof RoleTypeBinding) {
			RoleTypeBinding rtb = (RoleTypeBinding)roleRefType;

			if (! (rtb._teamAnchor instanceof TThisBinding))
				return roleType; // don't instantiate explicit team anchor.
		}
		// lookup adjusted role type:
		roleRefType = enclosingTeam.getMemberType(roleRefType.internalName());
		if (roleRefType == null) {
			if (enclosingTeam.isBinaryBinding()) {
				ReferenceBinding current= enclosingTeam;
				// search a role type to report against (for aborting):
				while (current != null && current.isBinaryBinding())
					current= current.enclosingType();
				if (current != null) {
					Scope scope= ((SourceTypeBinding)current).scope;
					if (scope != null) {
						scope.problemReporter().missingRoleInBinaryTeam(roleType.constantPoolName(), enclosingTeam);
						return null;
					}
				}
			}
			if (Protections.hasClassKindProblem(enclosingTeam))
				return roleType; // can't do better..
			if (!enclosingTeam.isBinaryBinding()) {
				Scope scope= ((SourceTypeBinding)enclosingTeam.getRealType()).scope;
				scope.problemReporter().missingCopiedRole(roleType, enclosingTeam);
			} else if (!site.isBinaryBinding()) {
				Scope scope= ((SourceTypeBinding)site.getRealType()).scope;
				scope.problemReporter().missingCopiedRole(roleType, enclosingTeam);
			} else {
				throw new InternalCompilerError("could not find role "+String.valueOf(roleType.constantPoolName()) //$NON-NLS-1$
												+" in "+String.valueOf(site.constantPoolName()) +" and could not report regularly"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return roleType; // can't do better, but shouldn't reach here, because missingCopiedRole triggers AbortType.
		}
		VariableBinding anchor = enclosingTeam.getTeamModel().getTThis();
		if (roleType.isParameterizedType()) {
			// consult original role type for type arguments:
			ParameterizedTypeBinding ptb = (ParameterizedTypeBinding)roleType;
			TypeBinding parameterized = ptb.environment.createParameterizedType(roleRefType, ptb.arguments, anchor, -1, roleRefType.enclosingType());
			if (dimensions > 0)
				return ptb.environment.createArrayType(parameterized, dimensions);
			return parameterized;
		}
		return anchor.getRoleTypeBinding(roleRefType, dimensions);
	}
	/**
	 * Starting at site look for an enclosing type that is compatible to targetEnclosing. 
	 */
	public static ReferenceBinding strengthenEnclosing(ReferenceBinding site, ReferenceBinding targetEnclosing) {
		ReferenceBinding currentEnclosing = site;
		while (targetEnclosing != currentEnclosing) {
			if (currentEnclosing.isCompatibleWith(targetEnclosing)) {
				return currentEnclosing;
			}
			currentEnclosing = currentEnclosing.enclosingType();
			if (currentEnclosing == null)
				throw new InternalCompilerError("Target class for callin binding not found: "+String.valueOf(targetEnclosing.readableName())); //$NON-NLS-1$
		}
		return targetEnclosing;
	}

	/**
	 * Find a role type from a given reference binding.
	 * Should only be used if no scope is available!
	 *
	 * @param site
	 * @param className
	 * @return role or null
	 */
	public static ReferenceBinding findMemberTypeInContext(ReferenceBinding site, char[] className) {
		while (site != null) {
			ReferenceBinding roleType = site.getMemberType(className);
			if (roleType != null)
				return roleType;
			site = site.enclosingType();
		}
		return null;
	}

	public static boolean isMoreSpecificThan(ReferenceBinding type1, ReferenceBinding type2)
	{
		if (type1.isCompatibleWith(type2))
			return true;
		ReferenceBinding enclosingType1 = type1.enclosingType();
		ReferenceBinding enclosingType2 = type2.enclosingType();
		if (enclosingType1 != null && enclosingType2 != null)
			return isMoreSpecificThan(enclosingType1, enclosingType2);
		return false;
	}
	/**
	 * performs role type strengthening, preforms static adjustment (OTJLD 2.3.3(a).
	 * Respects arrays and checks compatibility.
	 * @param scope client context, guaranteed to be a team or a role
	 * @param provided
	 * @param required
	 * @param doAdjust should the adjusted role be returned (as opposed to just the strengthened)?
	 * @return an exact role or null
	 */
	public static TypeBinding getRoleToLiftTo (
			Scope scope,
			TypeBinding provided,
			TypeBinding required,
			boolean doAdjust,
			ASTNode location)
	{
		ReferenceBinding requiredRef = null;
		if (   required.isArrayType()
			&& (required.leafComponentType() instanceof ReferenceBinding))
		{
			requiredRef = (ReferenceBinding)required.leafComponentType();
		} else if (required instanceof ReferenceBinding) {
			requiredRef = (ReferenceBinding)required;
		}
		if (   requiredRef != null
			&& requiredRef.isRole())
		{
			TeamModel enclosingTeam = requiredRef.enclosingType().getTeamModel();
			if (enclosingTeam != null && !provided.leafComponentType().isBaseType()) {
				for (Pair<ReferenceBinding,ReferenceBinding> ambig : enclosingTeam.ambigousLifting)
					if (ambig.equals((ReferenceBinding)provided.leafComponentType(), requiredRef))
					{
						scope.problemReporter().definiteLiftingAmbiguity(provided, required, location);
						return null;
					}
			}

			requiredRef = (ReferenceBinding)strengthenRoleType(
					scope.enclosingSourceType(),
					requiredRef);
			ReferenceBinding foundRole = null;
			if (requiredRef.baseclass() == null) {
				foundRole = adjustRoleToLiftTo(scope, provided, requiredRef, location);
				if (foundRole != null && !doAdjust)
					foundRole = requiredRef; // successful but revert to unadjusted
			} else {
				if (!provided.leafComponentType().isBaseType()) {
					ReferenceBinding providedLeaf = (ReferenceBinding)provided.leafComponentType();
					providedLeaf = RoleTypeCreator.maybeInstantiateFromPlayedBy(scope, providedLeaf);
					if (   providedLeaf.isCompatibleWith(requiredRef.baseclass())
						&& required.dimensions() == provided.dimensions())
					{
						foundRole = requiredRef;
					}
				}
// FIXME(SH): unneeded?
//				// just check definite binding ambiguity:
//				adjustRoleToLiftTo(scope, provided, requiredRef, location);
			}
			if (foundRole != null) {
				// success by translation
				if (required.dimensions() == 0)
					return foundRole;
				else
					return scope.createArrayType(foundRole, required.dimensions());
			}
		}
		return null;
	}

	/**
	 * Perform static adjustment according to OTJLD 2.3.3(a).
	 */
	private static ReferenceBinding adjustRoleToLiftTo(
			Scope scope,
			TypeBinding provided,
			ReferenceBinding required,
			ASTNode location)
	{
		ReferenceBinding mostGeneralFound = null;
		ReferenceBinding mostSpecificFound = null;

		ReferenceBinding enclosingTeam = required.enclosingType();
		ReferenceBinding[] roleTypes = enclosingTeam.memberTypes();
		ReferenceBinding requiredLeaf = (ReferenceBinding)required.leafComponentType();
		requiredLeaf = requiredLeaf.getRealType();
		for (int i = 0; i < roleTypes.length; i++) {
			if (TSuperHelper.isMarkerInterface(roleTypes[i]))
				continue;
			RoleModel currentRole = roleTypes[i].roleModel;
			ReferenceBinding currentRoleIfc = currentRole.getInterfacePartBinding();
			ReferenceBinding currentBase = currentRole.getBaseTypeBinding();

			if (mostGeneralFound == currentRoleIfc)
				continue; // already seen (happens because class/ifc part show the same role)

			if (   currentBase != null
				&& provided.leafComponentType().isCompatibleWith(currentBase)
				&& currentRoleIfc.isCompatibleWith(requiredLeaf))
			{
				if (mostGeneralFound == null) {
					mostGeneralFound = currentRoleIfc;
					mostSpecificFound = currentRoleIfc;
				} else {
					if (mostGeneralFound.isCompatibleWith(currentRoleIfc)) {
						mostGeneralFound = currentRoleIfc; // new type is more general
					} else if (   currentRoleIfc.isCompatibleWith(mostSpecificFound)) {
						mostSpecificFound = currentRoleIfc;// new type is more specific
					} else { // non-linear relation between different candidates.
						scope.problemReporter().definiteLiftingAmbiguity(provided, required, location);
						return null;
					}
				}
			}
		}
		return mostGeneralFound;
	}


	// ==== Facade for RoleFileCache: ====

	/**
	 *  Setup a structure for persistent storage of a list of know.
	 *  role files.
	 * @param scope TODO
	 * @param environment lookup new types here.
	 */
	public void setupRoFiCache(Scope scope, LookupEnvironment environment) {
		if (this.knownRoleFiles != null)
			this.knownRoleFiles.createTypeAndBinding(scope, environment);
	}

	/**
	 * Has the role files cache been initialized properly for 'receiverType'?
	 */
	public static boolean hasRoFiCache(ReferenceBinding receiverType) {
		if (!receiverType.isTeam())
			return false;
		TeamModel model = receiverType.getTeamModel();
		if (model.knownRoleFiles == null)
			return false;
		return model.knownRoleFiles.isValid;
	}

	/**
	 * Ensure all known roles (role files) are loaded.
	 */
	public void readKnownRoleFiles() {
		if (this.knownRoleFiles != null)
			this.knownRoleFiles.readKnownRoles();
	}
	/**
	 * Generate the byte code to store the cache of known role files.
	 *
	 * @param classFile class file of the enclosing team.
	 */
	public void generateRoFiCache(ClassFile classFile) {
		if (this.knownRoleFiles != null && !Protections.hasClassKindProblem(this._binding))
			this.knownRoleFiles.generateCode(classFile);
	}
	/**
	 * Register a role file by its (relative) type name.
	 *
	 * @param name
	 */
	public void addKnownRoleFile(char[] name, ReferenceBinding role) {
		if (this.knownRoleFiles != null)
			this.knownRoleFiles.addRoleFile(name);
		if (this._ast != null && (this._ast.scope instanceof OTClassScope))
			((OTClassScope) this._ast.scope).recordBaseClassUse(role.baseclass); // avoid evaluating baseclass()
	}

	/**
	 * Get all known roles, those that are listed in memberTypes as well
	 * as those only recorded in knownRoleFiles (which might be binary
	 * even if the team is a SourceTypeBinding), but exclude the RoFi cache itself.
	 * @return intended to be a non-null array.
	 */
	public ReferenceBinding[] getKnownRoles() {
		ReferenceBinding[] members = this._binding.memberTypes();
		if (this.knownRoleFiles == null)
			return members;
		HashSet<String> roleNames = new HashSet<String>();
		for (int i = 0; i < members.length; i++) {
			if (!RoleFileCache.isRoFiCache(members[i]))
				roleNames.add(new String(members[i].internalName()));
		}
		char[][] roleFileNames = this.knownRoleFiles.getNames();
		for (int i = 0; i < roleFileNames.length; i++) {
			roleNames.add(new String(roleFileNames[i]));
		}
		int len = roleNames.size();
		int i=0;
		ReferenceBinding[] result = new ReferenceBinding[len];
		for (Iterator<String> iter = roleNames.iterator(); iter.hasNext();) {
			String name= iter.next();
			result[i++] = this._binding.getMemberType(name.toCharArray());
		}
		return result;
	}

	public TypeBinding getMarkerInterfaceBinding(Scope scope) {
		if (this.markerInterface != null)
			return this.markerInterface.binding;
		if (   this._binding != null
			&& this._binding.superclass() != null)
		{
			// has not been generated yet, but perhaps this can indeed by repaired now:
			TSuperHelper.addMarkerInterface(this, this._binding.superclass());
			return this.markerInterface.binding;
		}
		PackageBinding pkgBinding = scope.compilationUnitScope().fPackage;
		char[] markerName = "MissingTSuperMarker".toCharArray(); //$NON-NLS-1$
		char[][] compoundName = this._binding != null ?
									CharOperation.arrayConcat(this._binding.compoundName, markerName) :
									new char[][] { markerName };
		return scope.compilationUnitScope().environment.createMissingType(pkgBinding, compoundName);
	}
//{OTDyn:
	private int nextCallinID = 0;
	public int getNewCallinId(MethodSpec baseMethodSpec) {
		int callinID = this.nextCallinID++;
		baseMethodSpec.callinID = callinID;
		return callinID;
	}
// SH}
}
