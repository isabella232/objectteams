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
 * $Id: RoleTypeBinding.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import java.util.HashMap;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.compiler.OTNameUtils;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.RoleSplitter;


/**
 * NEW for OTDT.
 *
 * The type of a role is wrapped by a RoleTypeBinding.
 * This class takes care of anchoring each role type to an enclosing Team instance.
 * If the type anchor (a VariableBinding) is typed to a sub-type of
 * the Team containing the original role type,
 * we use the corresponding role from the sub-Team.
 *
 * What:  Signature weakening
 * Why:   Creation of a RTB might pass a role type from a super team together with an anchor
 *        from a sub-team due to signature weakening.
 * How:   Effecting the byte code generation:
 *        + Store weak type in _declaredRoleType as used for constantPoolName()
 *          also TypeAnalyzer.getMethodReturnTypeReference() uses _declaredRoleType() [should others, too?]
 *        + roleModel, _teamModel are kept consistent with _declaredRoleType,
 *          roleModel is used by TeamAnchor registerRoleType -> tell those types apart
 *          that will generate different byte code!
 *        For use by type checking:
 *        + Stronger type derived from team anchor is stored in _staticallyKnownRole{Class,Type}
 *
 * What:  Role types anchored to a method parameter
 * Why:   These require special substitution strategy, since argument names may not be available
 *        when processing message sends, so positions must be used instead.
 * How:   Store _argumentPosition of team anchor.
 *        Persist this position in AnchorListAttribute
 *        Map anchor in AnchorMapping (several locations)
 *        Setting _argumentPosition happens in
 *        + RoleTypeCreator.getParameterType()   (case of source type)
 *        + AnchorListAttribute.getWrappedType() (case of binary type)
 *
 * What:  Break circularity of org.objectteams.Team$__OT__Confined
 * Why:   Copies of __OT__Confined are the only classes, whose superclass-link
 *        actually uses the tsuper role from org.objectteams.Team.
 *        (so only this predefined class has a null-superclass).
 *        Function superclass() would loop for ever on these types, because
 *        using a TThis-anchor it would remap the super class Team.__OT__Confined
 *        to its copy in the current team => class and its superclass are the same!
 * How:   For confined classes preserve the actual enclosing type rather then
 *        deducing that from the team anchor (RoleTypeBinding(ITeamAnchor, ReferenceBinding))
 *
 * REGISTRIES:
 * -----------
 * What:  Instantiated types (with different team anchors)
 * How:   Register these in _instantiatedTypes(), managed by
 *        + maybeInstantiate()
 * 	      - registerAnchor() (for use by our constructors only)
 *
 * What:  Arrays (with different dimensions)
 * Where: _arrayBindings stores all known array types derived from a given RTB
 *        This field is encapsulated by getArrayType(dims)
 * Also:  maybeInstantiate(teamAnchor, dimensions), ITeamAnchor.getRoleTypeBinding(type, dims)
 *
 * FORWARDING:
 * -----------
 * What:  Forward API-functions to either _staticallyKnownRoleType or ..Class
 * How:   Type:   methods and constant fields
 *        Class:  fields, constructors, memberTypes
 *
 * TYPE CHECKING:
 * --------------
 * What:  Main entry is "isCompatibleWith()"
 *        This function has a side effect in possible calling
 *        Config.setCastRequired(), Config.setLoweringRequired()
 *
 * FIELD ACCESS REPLACING:
 * -----------------------
 * What:  When accessing a role field, the role expression has to be wrapped with a cast.
 * Why:   Role expressions are typed to the interface, fields however reside in the class.
 * Where: checkReplaceFieldAccess()
 *
 *
 * TODO(SH): This class manifests a current compiler limitation: an outer scope cannot
 * access a field of a contained role. This is so difficult, because:
 * - cast to role class locks to a concrete class, breaks implicit inheritance.
 * - cast method is dynamically bound does not return role _class_
 * - the need for access methods which could make this would sometimes be
 *   detected far too late.
 *
 * @author stephan
 */
public class RoleTypeBinding extends DependentTypeBinding
		implements IRoleTypeBinding, IOTConstants, ProblemReasons
{
    // used to signal that no valid anchor was found:
	public static final VariableBinding NoAnchor = new LocalVariableBinding(
	        "<no valid anchor>".toCharArray(), TypeBinding.NULL, ClassFileConstants.AccFinal, false);  //$NON-NLS-1$


    public ReferenceBinding  _declaredRoleType;         // as passed to the constructor FIXME(SH): fade out
    private ReferenceBinding _staticallyKnownRoleType;  // the interface                FIXME(SH): unify with DTB._type
    private ReferenceBinding _staticallyKnownRoleClass; // the class if we have one
    public ReferenceBinding  _staticallyKnownTeam;      // the static type of the team anchor
    private ReferenceBinding _superClass;               // caches the result of superclass()


    // ============= CREATION AND INSTANCE REGISTRY: ===================
    public RoleTypeBinding(ReferenceBinding genericType, TypeBinding[] typeArguments, ITeamAnchor teamAnchor, ReferenceBinding enclosingType, LookupEnvironment lookupEnvironment) 
    {
		super(genericType.getRealType(), typeArguments, enclosingType, lookupEnvironment);
		initialize(genericType, teamAnchor);
	}

    private void initialize(ReferenceBinding roleType, ITeamAnchor teamAnchor) 
    {
    	// FIXME(SH): is it OK to strip ParameterizedFields?
    	if (teamAnchor instanceof ParameterizedFieldBinding)
    		teamAnchor = ((ParameterizedFieldBinding)teamAnchor).original();

    	initializeDependentType(teamAnchor, -1); // role type bindings have no explicit value parameters

        this.tagBits |= TagBits.IsWrappedRole;

        // infer argument position.
        ITeamAnchor firstAnchorSegment = teamAnchor.getBestNamePath()[0];
        if (   firstAnchorSegment instanceof LocalVariableBinding
        	&& (((LocalVariableBinding)firstAnchorSegment).tagBits  & TagBits.IsArgument) != 0)
        {
        	LocalVariableBinding argumentBinding = (LocalVariableBinding)firstAnchorSegment;
			this._argumentPosition = argumentBinding.resolvedPosition;
        	final MethodScope methodScope = argumentBinding.declaringScope.methodScope();
        	if (methodScope != null)
        		// if scope is a callout, role method may not yet be resolved, defer:
        		this._declaringMethod = new IMethodProvider() {
        			private MethodBinding binding;
        			public MethodBinding getMethod() {
        				if (this.binding == null)
        					this.binding = methodScope.referenceMethodBinding();
        				return this.binding;
        			}
        		};
        }

        // compute the team:
        if (CharOperation.equals(roleType.compoundName, IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED))
        	// the following is needed in order to break the circularity
        	// of roles extending the predefined Team.__OT__Confined (see class comment)
        	this._staticallyKnownTeam = roleType.enclosingType();
        else if (teamAnchor == NoAnchor)
        	this._staticallyKnownTeam = roleType.enclosingType();
        else
        	teamAnchor.setStaticallyKnownTeam(this);

        assert(this._staticallyKnownTeam.isTeam());

        // compute role class and role interface (by name manipulation):
        if (RoleSplitter.isClassPartName(roleType.sourceName)) {
        	this._staticallyKnownRoleClass = roleType.getRealClass();
        	char[] typeName = RoleSplitter.getInterfacePartName(roleType.sourceName);
        	this._staticallyKnownRoleType = this._staticallyKnownTeam.getMemberType(typeName);
        } else {
	        this._staticallyKnownRoleType    = roleType.getRealType();
	        char[] className = CharOperation.concat(
	                OT_DELIM_NAME,
	                this._staticallyKnownRoleType.sourceName);
	        this._staticallyKnownRoleClass = this._staticallyKnownTeam.getMemberType(className);
	        this._staticallyKnownRoleClass = transferTypeArguments(this._staticallyKnownRoleClass);
        }
        this._staticallyKnownRoleType = transferTypeArguments(this._staticallyKnownRoleType);
        
        this._declaredRoleType = this._staticallyKnownRoleType;
		// keep these consistent with _declaredRoleType:
        this.roleModel        = this._declaredRoleType.roleModel;
        this._teamModel       = this._declaredRoleType.getTeamModel();

        assert this._staticallyKnownTeam.getRealClass() == roleType.enclosingType(): "weakening not using WeakenedTypeBinding"; //$NON-NLS-1$
        // some adjustments after all fields are known:
        if (this._staticallyKnownTeam != roleType.enclosingType())
            this._staticallyKnownRoleType = this._staticallyKnownTeam.getMemberType(roleType.sourceName);
        if (this._staticallyKnownRoleClass != null)
            this.modifiers = this._staticallyKnownRoleClass.modifiers;

		// record as known role type at teamAnchor and in our own cache
        registerAnchor();
    }

    // hook of maybeInstantiate
    TypeBinding forAnchor(ITeamAnchor anchor, int dimensions) {
    	return anchor.getRoleTypeBinding(this._staticallyKnownRoleType, dimensions);
    }

    // cache field for use only by the following method
    HashMap<ReferenceBinding, DependentTypeBinding> weakenedTypes;
    public ReferenceBinding weakenFrom(ReferenceBinding other) {
    	if (other instanceof RoleTypeBinding) {
    		if (this.weakenedTypes == null) 
    			this.weakenedTypes = new HashMap<ReferenceBinding, DependentTypeBinding>();
    		ReferenceBinding knownWeakened = this.weakenedTypes.get(other);
    		if (knownWeakened != null) 
    			return knownWeakened;
    		RoleTypeBinding otherRTB = (RoleTypeBinding)other;
   			DependentTypeBinding newWeakened = new WeakenedTypeBinding(this, otherRTB._declaredRoleType, this.environment);
   			this.weakenedTypes.put(other, newWeakened);
   			return newWeakened;
    	}
    	return other;
    }

    /** Get a RoleTypeBinding from given type, either by casting or by unwrapping a weakened type. */
    public static RoleTypeBinding getRoleTypeBinding(TypeBinding binding) {
    	if (binding instanceof WeakenedTypeBinding)
    		return getRoleTypeBinding(((WeakenedTypeBinding)binding).type);
    	return (RoleTypeBinding)binding;
    }

    // Are two given types basically the same type (not considering
    // role wrapping or different class/ifc view)?
    public static boolean type_eq(ReferenceBinding left, ReferenceBinding right) {
    	// null-checks allways first:
    	if (left == null) {
    		if (right == null) return true;
    		else return false;
    	}
    	if (right == null) return false;

    	if (left.erasure() == right.erasure())
    		return true;

    	if (left instanceof RoleTypeBinding) {
    		RoleTypeBinding leftRole = (RoleTypeBinding)left;
    		if (right instanceof RoleTypeBinding)
    			return left.getRealType() == ((RoleTypeBinding)right).getRealType();
    		if (leftRole.getRealType() == right)
    			return true;
    		if (leftRole.getRealClass() == right)
    			return true;
    	} else if (right instanceof RoleTypeBinding) {
    		RoleTypeBinding rightRole = (RoleTypeBinding)right;
    		if (rightRole.getRealType() == left)
    			return true;
    		if (rightRole.getRealClass() == left)
    			return true;
    	}
    	return false;
    }

    /**
     * Stricter comparison: almost as "==", allow different but equivalent type anchors.
     * Similar to isEquivalentTo, but symmetric.
     *
     * @param left
     * @param right
     * @return whether or not left and right are regarded as equal as defined above.
     */
    public static boolean eq(TypeBinding left, TypeBinding right) {
    	if (left == right) return true;
		DependentTypeBinding leftDep= null;
		DependentTypeBinding rightDep= null;
		// nesting occurs for WeakenedTypeBinding(RoleTypeBinding), e.g.
		while (DependentTypeBinding.isDependentType(left)) {
			leftDep= (DependentTypeBinding)left;
			left= leftDep.type;
		}
		while (DependentTypeBinding.isDependentType(right)) {
			rightDep= (DependentTypeBinding)right;
			right= rightDep.type;
		}
		return leftDep != null && rightDep != null
			&& leftDep._teamAnchor.hasSameBestNameAs(rightDep._teamAnchor)
			&& CharOperation.equals(left.internalName(), right.internalName());
    }

    public static boolean isRoleType(TypeBinding binding) {
    	if (binding == null) return false;
    	return (binding.tagBits & TagBits.IsWrappedRole) != 0;
    }

    public static boolean isRoleTypeOrArrayOfRole(TypeBinding binding) {
    	if ((binding.tagBits & TagBits.IsBaseType) != 0)
    		return false;
//    	if (binding.isParameterizedType()) {
//    		ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding)binding;
//			if (isRoleTypeOrArrayOfRole(parameterizedType.actualType()))
//    			return true;
//			for (TypeBinding typeArg : parameterizedType.arguments)
//				if (isRoleTypeOrArrayOfRole(typeArg)) return true;
//			return false;
//    	}
    	return (binding.leafComponentType().tagBits & TagBits.IsWrappedRole) != 0;
    }

	/**
	 * Is param a role type with explicit anchor?
	 *
	 * @param type any type binding.
	 * @return the answer
	 */
	public static boolean isRoleWithExplicitAnchor(TypeBinding type) {
		if (type == null)
			return false;
		if ((type.tagBits & TagBits.IsWrappedRole) == 0)
			return false;
		return ((DependentTypeBinding)type).hasExplicitAnchor();
	}

	/**
	 * Is param a role type with no explicit anchor?
	 *
	 * @param type any type binding.
	 * @return the answer
	 */
	public static boolean isRoleWithoutExplicitAnchor(TypeBinding type) {
		if (type == null)
			return false;
		if ((type.tagBits & TagBits.IsWrappedRole) == 0)
			return false;
		return !((DependentTypeBinding)type).hasExplicitAnchor();
	}

	/**
	 * Answer whether the method has a parameter of a type of a role such that
	 * <ul>
	 *   <li>the role is a role of the type declaring the method, and</li>
	 *   <li>the parameter type is used by a simple reference, i.e., not explicitly anchored.</li>
	 * </ul>
	 * @param method
	 * @return the answer
	 */
	public static boolean hasNonExternalizedRoleParameter(MethodBinding method) {
		ReferenceBinding declaringClass = method.declaringClass;
		TypeBinding[] parameters = method.parameters;
		for (int j = 0; j < parameters.length; j++) {
			if (   isRoleWithoutExplicitAnchor(parameters[j])
				&& ((ReferenceBinding)parameters[j]).enclosingType() == declaringClass)
				return true;
		}
		return false;
	}

    // ============= Begin Instance Methods ===============
	public ITeamAnchor[] getAnchorBestName() {
		return this._teamAnchor.getBestNamePath();
	}
    /**
     * For field lookup we give the real role class "__OT__Role".
     */
    public ReferenceBinding getRealClass()
    {
    	if (this._staticallyKnownRoleClass != null)
    		return this._staticallyKnownRoleClass;
    	return this._staticallyKnownRoleType.getRealClass();
    }


    /**
     * For type comparison we give the real role type "Role".
     */
    public ReferenceBinding getRealType()
    {
        return this._staticallyKnownRoleType;
    }

	/** Answer the type that will be used in the class file: */
    public TypeBinding erasure() {
    	return this.type.erasure();
    }
    
    @Override
    public ReferenceBinding genericType() {
//{ObjectTeams: role bindings are the original if no arguments defined:
		if (this.arguments == null)
			return this;
// SH}
    	return super.genericType();
    }

    /**
	 * Is type `other' a sibling role of current?
	 */
	public boolean isSiblingRole(SourceTypeBinding other) {
		return enclosingType() == other.enclosingType();
	}

	/**
	 * Would 'role' be the same type as 'this' if it had 'anchor'?
	 *
	 * @param role
	 * @param anchor
	 */
	public boolean isSameType(RoleTypeBinding role, ITeamAnchor anchor) {
		return
			   role._staticallyKnownRoleType == this._staticallyKnownRoleType
		    && role._staticallyKnownTeam     == this._staticallyKnownTeam
			&& this._teamAnchor.hasSameBestNameAs(anchor);
	}


    // ============== forward most queries to the _staticallyKnownRoleType. ====================

	// --------- Find fields in the class:
    public FieldBinding[] availableFields() {
    	if (this._staticallyKnownRoleClass == null)
    		return Binding.NO_FIELDS;
        return this._staticallyKnownRoleClass.availableFields();
    }
    public int fieldCount() {
    	if (this._staticallyKnownRoleClass == null)
    		return 0;
        return this._staticallyKnownRoleClass.fieldCount();
    }
    public FieldBinding[] fields() {
    	if (this._staticallyKnownRoleClass == null)
    		return Binding.NO_FIELDS;
        return this._staticallyKnownRoleClass.fields();
    }
    public FieldBinding getField(char[] fieldName, boolean needResolve) {
        // normal case first:
        FieldBinding result = null;
        if (this._staticallyKnownRoleClass != null) {
        	result = this._staticallyKnownRoleClass.getField(fieldName, needResolve);
	        if (result != null)
	        	return result;
        }
        // static final fields are found in the interface part:
        return this._staticallyKnownRoleType.getField(fieldName, needResolve);
    }

    // ------------- Find methods in the interface:
    @Override
    public MethodBinding[] getMethods(char[] selector) {
    	if (this.methods == null && !isParameterizedType())
    		return this.type.getMethods(selector);
    	return super.getMethods(selector);
    }
    
    public MethodBinding[] availableMethods() {
        return this._staticallyKnownRoleType.availableMethods();
    }
    /* well, constructors are actually in the class: */
    public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
    	if (this._staticallyKnownRoleClass == null) {
    		// try self-healing:
    		this._staticallyKnownRoleClass = this.roleModel.getClassPartBinding();
    		if (this._staticallyKnownRoleClass == null)
    			throw new InternalCompilerError("Searching for constructor in pure interface role " //$NON-NLS-1$
    				+new String(this._staticallyKnownRoleType.sourceName()));
    	}
        return this._staticallyKnownRoleClass.getExactConstructor(argumentTypes);
    }
    public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
        return this._staticallyKnownRoleType.getExactMethod(selector, argumentTypes, refScope);
    }

	/* Implement OT-specific addition to ReferenceBinding. */
	public void addMethod(MethodBinding method) {
		if (this._staticallyKnownRoleClass != null) {
			this._staticallyKnownRoleClass.addMethod(method);
		}
		MethodBinding ifcPart = new MethodBinding(method, this._staticallyKnownRoleType);
		ifcPart.modifiers |= ClassFileConstants.AccAbstract;
		this._staticallyKnownRoleType.addMethod(ifcPart);
	}


    public boolean canBeInstantiated() {
        return this._staticallyKnownRoleType.canBeInstantiated();
    }


    //  ===== can't redefine finals! =====
//    public final boolean canBeSeenBy(PackageBinding invocationPackage) {
//    public final boolean canBeSeenBy(ReferenceBinding receiverType, SourceTypeBinding invocationType) {
//    public final boolean canBeSeenBy(Scope scope) {
//    public final int depth() {
//    public final boolean isViewedAsDeprecated() {
//    public final ReferenceBinding enclosingTypeAt(int relativeDepth) {

// these simply operate on this.modifiers:
//    public final int getAccessFlags() {
//    public final boolean isAbstract() {
//    public final boolean isAnonymousType() {
//    public final boolean isBinaryBinding() {

//    public final boolean isTeam() {
//    public final boolean isRole() {
//    public final boolean isDefault() {
//    public final boolean isDeprecated() {
//    public final boolean isFinal() {
//    public final boolean isInterface() {
//    public final boolean isLocalType() {
//    public final boolean isMemberType() {
//    public final boolean isNestedType() {
//    public final boolean isPrivate() {
//    public final boolean isPrivateUsed() {
//    public final boolean isProtected() {
//    public final boolean isPublic() {
//    public final boolean isStatic() {
//    public final boolean isStrictfp() {

    public TeamModel getTeamModel() {
    	if (this._teamModel != null)
    		return this._teamModel;
    	if (this._staticallyKnownRoleClass != null)
    		this._teamModel = this._staticallyKnownRoleClass.getTeamModel();
    	if (this._teamModel != null)
    		return this._teamModel;
    	this._teamModel = this._staticallyKnownRoleType.getTeamModel();
    	return this._teamModel;

    }
    public final boolean isClass() {
        return this._staticallyKnownRoleClass != null;
    }
    public boolean isRole() {
    	return true;
    }
    public boolean isSourceRole() {
    	return true;
    }
    public boolean isDirectRole() {
    	return true;
    }

    public void computeId() {
        this._staticallyKnownRoleType.computeId();
    }

    public char[] getFileName() {
        return this._staticallyKnownRoleType.getFileName();
    }

    public ReferenceBinding[] memberTypes() {
        return this._staticallyKnownRoleType.memberTypes();
    }
    public ReferenceBinding getMemberType(char[] typeName) {
        ReferenceBinding result = this._staticallyKnownRoleType.getMemberType(typeName);
        if (result != null)
        	return result;
        if (this._staticallyKnownRoleClass == null)
        	return null;
        return this._staticallyKnownRoleClass.getMemberType(typeName);
    }

    @Override
	public SyntheticArgumentBinding[] valueParamSynthArgs() {
    	if (this._staticallyKnownRoleClass != null)
    		return this._staticallyKnownRoleClass.valueParamSynthArgs();
    	return NO_SYNTH_ARGUMENTS;
	}

    public PackageBinding getPackage() {
        return this._staticallyKnownRoleType.getPackage();
    }

    public ReferenceBinding superclass() {
    	// 0. a stored superclass:
        if (this._superClass != null)
            return this._superClass;

        // 1. a direct super-role is found as the superclass of this role's class:
        if (this._staticallyKnownRoleClass != null) {
	        ReferenceBinding superClass = this._staticallyKnownRoleClass.superclass();
	        if ((superClass != null) && (superClass.isDirectRole()))
	        {
	        	// 1.a: a confined type "as-is": don't instantiate/strengthen
	        	if (OTNameUtils.isPredefinedConfined(superClass.compoundName))
	        		return this._superClass = superClass;

	        	// 1.b: instantiate superclass to the current team anchor:
	        	superClass = (ReferenceBinding)this._teamAnchor.getRoleTypeBinding(
	                    superClass.roleModel.getInterfacePartBinding(),
						0); // dimensions=0 => the above cast is safe.

	        	// 1.c: strengthen weakened type:
	        	if (superClass instanceof WeakenedTypeBinding) {
	        		// TODO(SH): comment: how come this is possible? why is depth relevant?
	        		if (superClass.depth() >= depth()) {
	        			return null; // FIXME(SH): find a witness or delete this branch
	        		} else {
	        			// using "extends" accross team border:
	        			superClass = ((WeakenedTypeBinding)superClass).type;
	        		}
	        	}
	            return this._superClass = superClass;
	        }
        }

        // 2. non-role superclass as the superclass of the role's interfaces:
        if (this._staticallyKnownRoleType != null)
        	return this._superClass = this._staticallyKnownRoleType.superclass();

        return null;
    }
    
    @Override
    public boolean isHierarchyConnected() {
    	return this.type.isHierarchyConnected();
    }

    public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
        return this._staticallyKnownRoleType.syntheticEnclosingInstanceTypes();
    }
    public SyntheticArgumentBinding[] syntheticOuterLocalVariables() {
        return this._staticallyKnownRoleType.syntheticOuterLocalVariables();
    }


    /* To check whether a role type implements a given role interface,
     * we need to take our role CLASS and the other side's INTERFACE,
     * extracting this from a RoleTypeBinding if needed.
     */
    public boolean implementsInterface(ReferenceBinding anInterface, boolean searchHierarchy) {
        if (anInterface instanceof RoleTypeBinding)
        {
            // TODO (SH): check Team equivalence
            anInterface = ((RoleTypeBinding)anInterface)._staticallyKnownRoleType;
        }
        return this._staticallyKnownRoleType.implementsInterface(anInterface, searchHierarchy);
    }
    public boolean implementsMethod(MethodBinding method) {
        return ((AbstractOTReferenceBinding)this._staticallyKnownRoleType).implementsMethod(method);
    }

    /*
     * For superclass test, take the role CLASSES on both sides.
     */
    public boolean isSuperclassOf(ReferenceBinding otherType) {
        if (otherType instanceof RoleTypeBinding)
        {
            // TODO (SH): check Team equivalence?
            otherType = ((RoleTypeBinding)otherType)._staticallyKnownRoleType;
        }
        return this._staticallyKnownRoleType.isSuperclassOf(otherType);
    }

    /* mainly forward: */
    @Override
    public TypeBinding findSuperTypeOriginatingFrom(TypeBinding otherType) {
    	if (otherType instanceof ReferenceBinding) {
    		ReferenceBinding otherRef = (ReferenceBinding)otherType;
    		// interface part first:
    		ReferenceBinding ifcBinding = this.transferTypeArguments(this._staticallyKnownRoleType);
    		TypeBinding superRef = ifcBinding.findSuperTypeOriginatingFrom(otherRef.getRealType());
    		// for regular superclass also search class part:
    		if (superRef == null && this._staticallyKnownRoleClass != null) {
    			ReferenceBinding classBinding = this.transferTypeArguments(this._staticallyKnownRoleClass);
    			superRef = classBinding.findSuperTypeOriginatingFrom(otherRef.getRealClass());
    			if (superRef != null && (Config.getCastRequired() == null)) {
    				// compatible via role class only with a cast,
    				// use the actually expected type to mark this.
    				Config.setCastRequired((ReferenceBinding)otherType);
    			}
    		}
    		if (superRef != null) {
    			if (superRef.isRole())
    				return this._teamAnchor.getRoleTypeBinding((ReferenceBinding) superRef, 0);
    			else
    				return superRef;
    		}
    	}
    	return null;
    }

    /** Forward to either part: */
    @Override
    public boolean isProvablyDistinct(TypeBinding otherType) {
    	if (this._staticallyKnownRoleType != null && !this._staticallyKnownRoleType.isProvablyDistinct(otherType))
    		return false;
    	if (this._staticallyKnownRoleClass != null && !this._staticallyKnownRoleClass.isProvablyDistinct(otherType))
    		return false;
    	return true;
    }

    @Override
    public void setIsBoundBase(ReferenceBinding roleType) {
    	if (this._staticallyKnownRoleClass != null)
    		this._staticallyKnownRoleClass.setIsBoundBase(roleType);
    	if (this._staticallyKnownRoleType != null)
    		this._staticallyKnownRoleType.setIsBoundBase(roleType);
    }
    /**
     * MAIN ENTRY FOR TYPE CHECKING.
     *
     * Answer true if the receiver type can be assigned to the argument type (right).
     * Compare team-anchor and role-type.
     * Note, that the type of _teamAnchor and _staticallyKnownTeam may differ.
     * The former is relevant for type-checking. the latter serves mainly for code generation
     * and for determining overriding.
     */
    public boolean isCompatibleWith(TypeBinding right) {
        if (right == this)
            return true;
        if (!(right instanceof ReferenceBinding))
            return false;

        ReferenceBinding referenceBinding = (ReferenceBinding) right;
        if (isRoleType(referenceBinding))
        {
        	RoleTypeBinding rightRole = getRoleTypeBinding(referenceBinding);

        	// compare teams:
        	if (!this._teamAnchor.hasSameBestNameAs(rightRole._teamAnchor))
        	{ // different anchors, not both tthis: not compatible!
        		return isCompatibleViaLowering(rightRole);
        	}

    		// compensate weakened signature:
    		if (rightRole._staticallyKnownTeam != this._staticallyKnownTeam) {
    			if (TeamModel.areTypesCompatible(
    					rightRole._staticallyKnownTeam,
						this._staticallyKnownTeam))
    			{
    				ReferenceBinding leftStrengthened = this._teamAnchor.getMemberTypeOfType(internalName());
    				if (leftStrengthened != this)
    					return leftStrengthened.isCompatibleWith(right);
    			}
    			else if (TeamModel.areTypesCompatible(
    					this._staticallyKnownTeam,
    					rightRole._staticallyKnownTeam))
    			{
    				rightRole = (RoleTypeBinding)this._teamAnchor.getMemberTypeOfType(rightRole.internalName());

    			} else {
    				return false;
    			}
    		}

    		// check the role types:
			if (this._staticallyKnownRoleType.
                    isCompatibleWith(rightRole._staticallyKnownRoleType))
				return true;
        }
        if (   referenceBinding.isInterface()
            && implementsInterface(referenceBinding, true))
                return true;

        if (   this._staticallyKnownRoleClass == null
        	&& this._staticallyKnownRoleType.isCompatibleWith(referenceBinding, false))
        {
        	checkAmbiguousObjectLower(referenceBinding);
        	return true; // this case is wittnessed by: "this=RoleIfc", right="Object"; other examples?
        }

        // do we need the class part instead of the interface part?
        if (   (this._staticallyKnownRoleClass != null)
            && this._staticallyKnownRoleClass.isStrictlyCompatibleWith(referenceBinding)
            && !TeamModel.isTeamContainingRole(this._staticallyKnownTeam, referenceBinding))
        {
        	// Cast from a role to its non-role superclass
        	// (Interfaces do not reflect this compatibility, thus we need to help here).
            Config.setCastRequired(referenceBinding);
            checkAmbiguousObjectLower(referenceBinding);
            return true;
        }

        // after everything else has failed try lowering:
        return isCompatibleViaLowering(referenceBinding);
    }

    // check if upcast to java.lang.Object bypasses a potentially desired lowering:
    private void checkAmbiguousObjectLower(ReferenceBinding otherType) {
    	if (   otherType.id == TypeIds.T_JavaLangObject
    		&& baseclass() != null)
    		Config.setLoweringPossible(true);
    }

    public boolean isCompatibleViaLowering(ReferenceBinding otherType) {
		// <B base R>?
		if (otherType instanceof TypeVariableBinding) {
			ReferenceBinding otherRole = ((TypeVariableBinding)otherType).roletype;
			if (this.isCompatibleWith(otherRole)) {
				Config.setLoweringRequired(true);
				return true;
			}
		}
		ReferenceBinding baseType = null;
		// needed for base-class lookup:
		Dependencies.ensureRoleState(this.roleModel, ITranslationStates.STATE_LENV_DONE_FIELDS_AND_METHODS);

        RoleModel strengthened = this._teamAnchor.getStrengthenedRole(this);
        if ((strengthened.getInterfacePartBinding().modifiers & AccSynthIfc) == ClassFileConstants.AccInterface)
            baseType = strengthened.getInterfacePartBinding().baseclass();
        else
        {
            if(strengthened.getClassPartBinding() != null)
            {
            	baseType = strengthened.getClassPartBinding().baseclass();
            }
        }
        if (  (baseType != null)
            && baseType.isCompatibleWith(otherType))
        {
            Config.setLoweringRequired(true);
            return true;
        }
        return false;
	}

    public boolean isStrictlyCompatibleWith(TypeBinding right) {
    	return isCompatibleWith(right);
    }

    // see DependentTypeBinding.isEquivalentTo()
	@Override
	public boolean isEquivalentTo(TypeBinding otherType) {
		if (! (otherType instanceof RoleTypeBinding))
			return this._staticallyKnownRoleType.isEquivalentTo(otherType); // don't use DTB._type
		RoleTypeBinding otherDep = getRoleTypeBinding(otherType);
		// same best name implies that checking of simple names suffices (thanks to OTJLD 1.4(c)):
		return otherDep._teamAnchor.hasSameBestNameAs(this._teamAnchor)
			&& CharOperation.equals(otherDep.internalName(), internalName());
	}

	/** ignoring the role part, check if anchors are provably identical. */
	public boolean hasEquivalentAnchorTo(TypeBinding otherType) {
		if (otherType instanceof RoleTypeBinding) {
			RoleTypeBinding otherRoleType = (RoleTypeBinding)otherType;
			if (this._teamAnchor.hasSameBestNameAs(otherRoleType._teamAnchor))
				return true;
		}
		return false;
	}
    // =========== VARIOUS NAMES: =============

    public char[] qualifiedSourceName() {
        return this._staticallyKnownRoleType.qualifiedSourceName();
    }

    public char[] optimalName() /*var.RoleType or java.lang.Object */
    {
    	if (this._teamAnchor instanceof TThisBinding)
    		return super.readableName();

    	char[] anchorName = new char[0];

    	ITeamAnchor[] bestNamePath = this._teamAnchor.getBestNamePath();
		for (int i = 0; i < bestNamePath.length; i++) {
    		char[] readableName = bestNamePath[i].readableName();
    		if (CharOperation.equals(readableName, IOTConstants._OT_BASE))
    			readableName = "base".toCharArray(); //$NON-NLS-1$
			anchorName = CharOperation.concatWith(
    				new char[][]{anchorName, readableName},
					'.');
		}

        return CharOperation.concat(
            anchorName,
            strippedName(this._staticallyKnownRoleType),
            '.');
    }

   	private char[] strippedName(ReferenceBinding role) {
	    if (OTNameUtils.isTSuperMarkerInterface(role.sourceName))
	    {
	        return "<tsuper-mark>".toCharArray(); //$NON-NLS-1$
	    }
	    else if (RoleSplitter.isClassPartName(role.sourceName))
	    {
	        return RoleSplitter.getInterfacePartName(role.sourceName);
	    }
	    return role.sourceName;
	}

    // don't override: need original version in classfile:
    // public char[] signature() /* Ljava/lang/Object; */ {
    public char[] sourceName() {
        return this._staticallyKnownRoleType.sourceName();
    }

    // Note: this uses the original name (respects signature weakening):
    public char[] constantPoolName() /* java/lang/Object */ {
        return this._declaredRoleType.constantPoolName();
    }
    @Override
    public char[] attributeName() {
    	if (this._staticallyKnownRoleClass != null)
    		return this._staticallyKnownRoleClass.attributeName();
    	return super.attributeName();
    }
    public String toString() {
    	String anchorStr = ""; //$NON-NLS-1$
    	ITeamAnchor[] bestNamePath = this._teamAnchor.getBestNamePath(false);
		for (int i = 0; i < bestNamePath.length; i++) {
    		if (i>0)
    			anchorStr += '.';
			anchorStr += new String(bestNamePath[i].readableName());
		}
    	String staticTeam = ""; //$NON-NLS-1$
    	if (!(this._teamAnchor instanceof TThisBinding))
    		staticTeam = '['+new String(this._staticallyKnownTeam.sourceName())+']';
        return
            anchorStr
            +staticTeam
            +'.'
            +new String(sourceName());
    }
}
