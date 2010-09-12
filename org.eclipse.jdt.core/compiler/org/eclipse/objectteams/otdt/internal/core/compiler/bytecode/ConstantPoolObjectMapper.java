/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ConstantPoolObjectMapper.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
/**
 * ObjectTeams Eclipse source extensions
 *
 * @author Markus Witte
 *
 * @date 29.09.2003
 */
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;


import java.util.Arrays;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config.NotConfiguredException;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * MIGRATION_STATE: complete.
 *
 * @author Markus Witte
 * @version $Id: ConstantPoolObjectMapper.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class ConstantPoolObjectMapper implements ClassFileConstants{

	private MethodBinding _srcMethod; //The source-Method which bytecode ist to copy
	private MethodBinding _dstMethod; //The destination-Method for bytecode-copy will receive transformed bytecode from source-Method

	/**
	 * @param srcMethodBinding where bytes are copied from
	 * @param dstMethodBinding where bytes are copied to
	 */
	public ConstantPoolObjectMapper(
			MethodBinding srcMethodBinding,
			MethodBinding dstMethodBinding)
	{
		this._srcMethod   = srcMethodBinding;
		this._dstMethod   = dstMethodBinding;
	}

	/**
	 * map a src-ConstantPoolObject into an dst-ConstantPoolObject
	 * @param src_cpo
	 * @return destination object
	 */
	public ConstantPoolObject mapConstantPoolObject(ConstantPoolObject src_cpo)
	{
		return mapConstantPoolObject(src_cpo, false/*addMarkerArgAllowed*/);
	}
	/**
	 * map a src-ConstantPoolObject into an dst-ConstantPoolObject
	 * @param src_cpo
	 * @param addMarkerArgAllowed whether copying is allowed to add a marker arg
	 * @return destination object
	 */
	public ConstantPoolObject mapConstantPoolObject(ConstantPoolObject src_cpo, boolean addMarkerArgAllowed)
	{
		int type=src_cpo.getType();
		ReferenceBinding dstTeam = getTeam(this._dstMethod);

		// first investigate the declaring class if appropriate:
		ReferenceBinding declaringClass = null;
		switch (type) {
		case MethodRefTag:
		case InterfaceMethodRefTag:
			declaringClass = src_cpo.getMethodRef().declaringClass;
			break;
		case FieldRefTag:
			declaringClass = src_cpo.getFieldRef().declaringClass;
			break;
		}
		if (   declaringClass != null
			&& declaringClass instanceof RoleTypeBinding
			&& ((RoleTypeBinding)declaringClass).hasExplicitAnchor())
		{
			return src_cpo; // don't map features of externalized roles!
		}

		// perform the mapping:
		switch(type){
			case FieldRefTag:
				return new ConstantPoolObject(
						FieldRefTag,
						mapField(this._srcMethod, src_cpo.getFieldRef(), dstTeam)) ;
			case MethodRefTag:
				return new ConstantPoolObject(
						MethodRefTag,
						mapMethod(this._srcMethod, src_cpo.getMethodRef(), this._dstMethod, dstTeam, addMarkerArgAllowed));
			case InterfaceMethodRefTag:
				return new ConstantPoolObject(
						InterfaceMethodRefTag,
						mapMethod(this._srcMethod, src_cpo.getMethodRef(), this._dstMethod, dstTeam, addMarkerArgAllowed));
			case ClassTag:
				return new ConstantPoolObject(
						ClassTag,
						mapClass(this._srcMethod, src_cpo.getClassObject(), dstTeam));
		}
		//if no mapping is needed, return original ConstantPoolObject
		return src_cpo;
	}

    /**
     * Map a type binding and convert it to a UTF8-Object.
     * @param typeBinding
     * @param useGenerics should type parameters be respected (else use erasure)
     * @return ConstantPoolObject of type Utf8Tag
     */
    public ConstantPoolObject mapTypeUtf8(TypeBinding typeBinding, boolean useGenerics)
    {
        //System.out.println("Sign of "+typeBinding+"="+new String(typeBinding.signature()));
        char[] prefix    = null;
        if (typeBinding.isArrayType())
        {
            // need to disassemble arrays, because we want to analyze the element type:
            ArrayBinding  array = (ArrayBinding)typeBinding;
            prefix = new char[array.dimensions()];
            Arrays.fill(prefix, '[');
            typeBinding = array.leafComponentType;
        }
        if (typeBinding.isClass() || typeBinding.isInterface())
        {
            ConstantPoolObject clazzCPO = new ConstantPoolObject(
            			ClassTag,
						mapClass(this._srcMethod, typeBinding, getTeam(this._dstMethod)));
            typeBinding = clazzCPO.getClassObject();
        }
        char[] signature = useGenerics ? typeBinding.genericTypeSignature() : typeBinding.signature();

        if (prefix != null)
            signature = CharOperation.concat(prefix, signature);
        return new ConstantPoolObject(Utf8Tag, signature);
    }

	/**
	 * this method realizes the logic of the mapping
	 * @return destination type
	 */
	private static TypeBinding mapClass(MethodBinding srcMethod, TypeBinding typeBinding, ReferenceBinding dstTeam) {
		return mapClass(getTeam(srcMethod), typeBinding, dstTeam);
	}

	public static TypeBinding mapClass(ReferenceBinding srcTeamBinding, TypeBinding typeBinding, ReferenceBinding dstTeam)
	{
		boolean isArrayBinding = typeBinding instanceof ArrayBinding;
		int dimension = 0;
		TypeBinding originalType = typeBinding;
		if(isArrayBinding)
		{
			ArrayBinding formerType = (ArrayBinding) typeBinding;
			typeBinding = formerType.leafComponentType();
			dimension = formerType.dimensions;

			if (typeBinding.isBaseType())
				return formerType; // no need to map array of basetype
		}
		ReferenceBinding refTypeBinding = (ReferenceBinding)typeBinding;
		// if Binding points at Role-Field of Superteamclass, then mapping must be done
		if(isMappableClass(refTypeBinding))
		{
			ReferenceBinding refTeamBinding=getTeam(refTypeBinding);
			if(refTeamBinding != null)
			{
				if(srcTeamBinding != null)
				{
					TypeBinding newBinding = null;
					ReferenceBinding currentSrcTeam = srcTeamBinding;
					ReferenceBinding currentDstTeam = dstTeam;
					while (   currentSrcTeam != null
						   && currentDstTeam != null) {
						if(refTeamBinding == currentSrcTeam)	{
							// mapping the enclosing team which is nested in an outer team?
							if (typeBinding == refTeamBinding && typeBinding.isRole())
								newBinding = currentDstTeam;
							else
								newBinding = searchRoleClass(refTypeBinding, currentDstTeam);
							break;
						}
						// try dependent refinement of base classes:
						ReferenceBinding srcBase = currentSrcTeam.baseclass();
						if (srcBase != null && (srcBase.isTeam() || srcBase.isRole())) {
							if (srcBase == refTypeBinding)
								newBinding = currentDstTeam.baseclass();
							else
								newBinding = searchRoleClass(refTypeBinding, currentDstTeam.baseclass());
							if (newBinding != null)
								break;
						}
						// the common team to start searching might be an enclosing:
						if (!currentSrcTeam.isRole())
							break;
						currentSrcTeam= currentSrcTeam.enclosingType();
						currentDstTeam= currentDstTeam.enclosingType();
					}
					if (newBinding != null) {
						if(isArrayBinding)
						{
							// have no scope so can't use Scope.createArray(),
							// which otherwise should be used throughout.
							try {
								newBinding = new ArrayBinding(newBinding, dimension, Config.getLookupEnvironment());
							} catch (NotConfiguredException e) {
								e.logWarning("Cannot create array binding"); //$NON-NLS-1$
							}
						}
						return newBinding;
					}
				}
			}
		}
		return originalType;
	}

	/**
	 * This method realizes the logic of the mapping for fields.
	 * @param srcMethod        where to copy from
	 * @param refFieldBinding  what to copy/remap
	 * @param dstTeam          where to copy to
	 * @return destination field
	 */
	public static FieldBinding mapField(MethodBinding srcMethod, FieldBinding refFieldBinding, ReferenceBinding dstTeam)
	{
		// if Binding points at Role-Field of Superteamclass, then mapping must be done
		if(dstTeam!=null){
			if(isMappableField(refFieldBinding)){
				if (refFieldBinding.isSynthetic()) {
					RoleModel role = ((ReferenceBinding)mapClass(srcMethod, refFieldBinding.declaringClass, dstTeam)).roleModel;
					if (role != null) {
						FieldBinding dstField= role.mapSyntheticField(refFieldBinding);
						if (dstField!= null)
							return dstField;
					}
				}
				ReferenceBinding refTeamBinding=getTeam(refFieldBinding);
				if(refTeamBinding!=null){
					ReferenceBinding srcTeamBinding = getTeam(srcMethod);
					if(srcTeamBinding!=null){
						if(refTeamBinding==srcTeamBinding){
							FieldBinding newBinding=searchRoleField(refFieldBinding, dstTeam);
							if (   newBinding != null
								&& newBinding.declaringClass != dstTeam
								&& !TeamModel.isTeamContainingRole(dstTeam, newBinding.declaringClass))
							{
								// field is declared neither in dstTeam nor one of its roles.
								// find the class, that corresponds to the field's declaring class:
								ReferenceBinding updatedClass =
									(ReferenceBinding)mapClass(srcMethod, newBinding.declaringClass, dstTeam);
								// update field binding to new declaring class?
								if (newBinding.declaringClass != updatedClass)
									newBinding = new FieldBinding(newBinding, updatedClass);
							}
							return newBinding;
						}
					}
				}
			}
		}
		return refFieldBinding;
	}
	/**
	 * This method realizes the logic of the mapping for methods.
	 * @param srcMethod         where to copy from
	 * @param refMethodBinding  what to copy/remap
	 * @param dstTeam           where to copy to
	 * @return destination method
	 */
	public static MethodBinding mapMethod(MethodBinding srcMethod,
			                   			  MethodBinding refMethodBinding,
			                   			  MethodBinding dstMethod,
			                   			  ReferenceBinding dstTeam)
	{
		return mapMethod(srcMethod, refMethodBinding, dstMethod, dstTeam, false/*addMarkerAllowed*/);
	}
	/**
	 * This method realizes the logic of the mapping for methods.
	 * @param srcMethod         where to copy from
	 * @param refMethodBinding  what to copy/remap
	 * @param dstTeam           where to copy to
	 * @param addMarkerArgAllowed whether copying is allowed to add a marker arg
	 * @return destination method
	 */
	public static MethodBinding mapMethod(MethodBinding srcMethod,
			                   			  MethodBinding refMethodBinding,
			                   			  MethodBinding dstMethod,
			                   			  ReferenceBinding dstTeam,
			                   			  boolean addMarkerArgAllowed)
	{
		if (dstMethod != null) {
			if (dstMethod.model != null) {
				if (dstMethod.model.oldSelfcall == refMethodBinding)
					return dstMethod.model.adjustedSelfcall;
			}
			if (isConfinedSuperCtor(srcMethod, refMethodBinding))
				return getConfinedSuperCtor(dstMethod);
		}
		// if Binding points at Role-Method of Superteamclass, then mapping must be done
		if(isMappableMethod(refMethodBinding)){
			if (refMethodBinding.isSynthetic()) {
				RoleModel role = ((ReferenceBinding)mapClass(srcMethod, refMethodBinding.declaringClass, dstTeam)).roleModel;
				if (role != null) {
					MethodBinding foundMethod = role.mapSyntheticMethod(refMethodBinding);
					if (foundMethod != null)
						return foundMethod;
				}
			}
			boolean isDecapsAccessor = false;
			if (CharOperation.prefixEquals(IOTConstants.OT_DECAPS, refMethodBinding.selector)) {
				// to find a decapsulated method, first strip off the accessor's prefix, then search and ...
				refMethodBinding = new MethodBinding(refMethodBinding, refMethodBinding.declaringClass);
				refMethodBinding.selector = CharOperation.subarray(refMethodBinding.selector, IOTConstants.OT_DECAPS.length, -1);
				isDecapsAccessor = true;
			}
			MethodBinding foundMethod = doMapMethod(srcMethod, refMethodBinding, dstMethod, dstTeam, addMarkerArgAllowed);
			if (foundMethod != null && isDecapsAccessor) {
				// .. append the stripped prefix after finding
				foundMethod = new MethodBinding(foundMethod, foundMethod.declaringClass);
				foundMethod.selector = CharOperation.concat(IOTConstants.OT_DECAPS, foundMethod.selector);
			}
			return foundMethod;
		}
		return refMethodBinding;
	}
	static MethodBinding doMapMethod(MethodBinding srcMethod,
			                   			  MethodBinding refMethodBinding,
			                   			  MethodBinding dstMethod,
			                   			  ReferenceBinding dstTeam,
			                   			  boolean addMarkerArgAllowed)
	{
		ReferenceBinding refTeamBinding=getTeam(refMethodBinding);
		if(refTeamBinding!=null){
			ReferenceBinding srcTeamBinding = getTeam(srcMethod);
			if(srcTeamBinding!=null){
				// same team (direct tsuper) or compatible (indirect tsuper)
				if(srcTeamBinding.isCompatibleWith(refTeamBinding)){
					if(dstTeam!=null){
						MethodBinding newBinding=searchRoleMethodInTeam(dstTeam, refMethodBinding, addMarkerArgAllowed);
						if (newBinding == null) {
							// indirect tsuper may not be copied to this class:
							if (srcTeamBinding != refTeamBinding)
								return refMethodBinding;
							// is the method (inherited from a non role and) adjusted by getUpdatedMethodBinding?
							if (!hasMethod(refMethodBinding.declaringClass, refMethodBinding))
								return refMethodBinding;
						}
						return newBinding;
					}
				}
			}
		}
		return refMethodBinding;
	}

	/**
	 * Is given method a super-call to the constructor of an __OT__Confined role?
	 */
	private static boolean isConfinedSuperCtor(MethodBinding srcMethod, MethodBinding refMethodBinding) {
		// constructor?
		if (!refMethodBinding.isConstructor())
			return false;
		// of class __OT__Confined?
		if (!CharOperation.equals(refMethodBinding.declaringClass.compoundName, IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED))
			return false;
		// is it the superclass of the current src class?
		if (refMethodBinding.declaringClass == srcMethod.declaringClass.superclass())
			return true;
		// current src class may have no super class which is OK if it is Team.__OT__Confined
		return
			   srcMethod.declaringClass.superclass() == null
			&& CharOperation.equals(IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED, refMethodBinding.declaringClass.compoundName);
	}

	private static MethodBinding getConfinedSuperCtor(MethodBinding dstMethod) {
		ReferenceBinding newSuperclass = dstMethod.declaringClass.superclass();
		if (newSuperclass == null)
			throw new InternalCompilerError("copying byte code to class without superclass?"); //$NON-NLS-1$
		return newSuperclass.getExactConstructor(new TypeBinding[0]);
	}

	private static boolean hasMethod(ReferenceBinding clazz, MethodBinding meth) {
		MethodBinding[] methods = clazz.methods();
		for (int i = 0; i < methods.length; i++) {
			if (meth == methods[i])
				return true;
		}
		return false;
	}
	/**
	 * searches for a matching field in the destination Team
	 * For this the method will compare all roles from destination Team with
	 * the role of the referenced Field. if one matching role ist found,
	 * search inside this role for the referenced Field.
	 * The Field exists in this Role, because we have copied it before.
	 * @param refFieldBinding the referenced FieldBinding which must be a Role-Field of the dstTeamBinding-Superclass
	 * @param dstTeamBinding the destination Team
	 * @return the found FieldBinding or null if no matching Field is found
	 */
	private static FieldBinding searchRoleField(FieldBinding refFieldBinding, ReferenceBinding dstTeamBinding) {
		ReferenceBinding refRoleBinding = refFieldBinding.declaringClass;
		if(isMappableClass(refRoleBinding)) {
			//search for matching Role in dstTeamBinding
			ReferenceBinding dstRefRoleBinding = searchRoleClass(refRoleBinding, dstTeamBinding);
			//search for matching field in destination Role
			return ConstantPoolObjectReader.findFieldByBinding(dstRefRoleBinding, refFieldBinding);
		}
		//never should be here
		return null;
	}

	/**
	 * searches for a matching Method in the destination Team
	 * For this the method will compare all roles from destination Team with
	 * the role of the referenced Method. if one matching role ist found,
	 * search inside this role for the referenced Method.
	 * The Method exists in this Role, because we have copied it before.
	 * @param dstTeam the destination Team
	 * @param refMethod the referenced MethodBinding which must be a Role-Method of the dstTeamBinding-Superclass
	 * @return the found MethodBinding or null if no matching Method is found
	 */
	private static MethodBinding searchRoleMethodInTeam(ReferenceBinding dstTeam, MethodBinding refMethod, boolean addMarkerArgAllowed)
	{
		ReferenceBinding refRoleBinding = refMethod.declaringClass;
		if(isMappableClass(refRoleBinding)){
			//search for matching Role in dstTeamBinding
			ReferenceBinding dstRefRoleBinding = searchRoleClass(refRoleBinding, dstTeam);
			//search for matching method in destination Role
			MethodBinding roleMethod = searchRoleMethodInRole(dstRefRoleBinding, refMethod, addMarkerArgAllowed);
			if (roleMethod != null)
				return roleMethod;
			if (dstRefRoleBinding.isCompatibleWith(refRoleBinding)) {
				return new MethodBinding(refMethod, dstRefRoleBinding);
			}
//			System.out.println("method "+new String(refMethod.declaringClass.qualifiedSourceName())+"."+refMethod+" not found in "+new String(dstRefRoleBinding.qualifiedSourceName()));
		} else if (isStaticBasecallSurrogate(refMethod)) {
			// basecall surrogates for static role methods are team methods, but need adaptation, too.
			MethodBinding[] methods = dstTeam.getMethods(refMethod.selector);
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].parameters.length != refMethod.parameters.length)
					continue;
				for (int j = 0; j < methods[i].parameters.length; j++) {
					if (methods[i].parameters[j] != refMethod.parameters[j]) // non-variant
						continue;
				}
				return methods[i];
			}
			// FIXME(SH): should byte code store info whether callin method is bound?
			return refMethod; // don't change method reference, method might be a dummy (throwing OTREInternalError).
		}
		return null;
	}

	private static MethodBinding searchRoleMethodInRole(ReferenceBinding role, MethodBinding refMethod, boolean addMarkerArgAllowed)
	{
		MethodBinding[] methods;
		if (CopyInheritance.isCreator(refMethod)) {
			methods = role.getMethods(refMethod.selector);
			assert(methods.length == 1);
			return methods[0];
		}
		if (   role.isLocalType()
			&& CharOperation.equals(refMethod.selector, IOTConstants.INIT_METHOD_NAME))
		{
			// have no overriding along implicit inheritance due to scoping of locals,
			// so just retrieve the method by its name:
			return role.getMethods(IOTConstants.INIT_METHOD_NAME)[0];
		}
		int bestRank = Integer.MAX_VALUE;
		MethodBinding bestMethod = null;
		methods = role.getMethods(refMethod.selector);
		for (int i=0; i<methods.length; i++) {
			if (   refMethod.parameters.length == methods[i].parameters.length
				|| (   addMarkerArgAllowed
					&& refMethod.parameters.length + 1 == methods[i].parameters.length))
			{
				int rank = rankMethod(refMethod, methods[i], 0);
				if (rank < bestRank) {
					bestMethod = methods[i];
					bestRank = rank;
				}
			}
		}
		if (bestMethod != null)
			return bestMethod;
		if (    role.superclass() != null
			&& !CharOperation.equals(role.superclass().compoundName, TypeConstants.JAVA_LANG_OBJECT)
			&&  haveCommonEnclosingType(role.superclass(), role))
		{
			MethodBinding result = searchRoleMethodInRole(role.superclass(), refMethod, addMarkerArgAllowed);
			if (result != null)
				return result;
		}
		ReferenceBinding [] superInterfaces = role.superInterfaces();
		if (superInterfaces != null) {
			for (int i=0; i<superInterfaces.length; i++) {
				if (haveCommonEnclosingType(superInterfaces[i], role.enclosingType())) {
					MethodBinding binding = searchRoleMethodInRole(superInterfaces[i], refMethod, addMarkerArgAllowed);
					if (binding != null)
						return binding;
				}
			}
		}
		return null;
	}

	/**
	 * Do left and right have a common enclosing type at the same distance?
	 * @param left
	 * @param right
	 * @return the answer
	 */
	private static boolean haveCommonEnclosingType(ReferenceBinding left, ReferenceBinding right)
	{
		ReferenceBinding leftEnclosing = left.enclosingType();
		ReferenceBinding rightEnclosing = right.enclosingType();
		if (leftEnclosing == null || rightEnclosing == null)
			return false;
		if (leftEnclosing == rightEnclosing)
			return true;
		return haveCommonEnclosingType(leftEnclosing, rightEnclosing);
	}

	/**
	 * Test whether candidate is a suitable adjustment for toLookFor.
	 * Traversal uses copyInheritanceSrc and overriddenTSuper.
	 * Rank is determined by depth of traversal needed to find a connection.
	 *
	 * PRE: length of parameterlists has been checked.
	 *
	 * @return a small positiv integer value if suitable. Integer.MAX_VALUE if candidate is not suitable.
	 */
	private static int rankMethod(MethodBinding toLookFor, MethodBinding candidate, int currentRank)
	{
		currentRank++;
		if (candidate == null || toLookFor == null)
			return Integer.MAX_VALUE;
		if (candidate == toLookFor)
			return currentRank;
		if (   currentRank > 0 // at top level we search all methods by this name any way.
		    && (candidate.modifiers & ClassFileConstants.AccBridge) != 0)
		{
			// bridge method: there must be a matching regular method: find it:
			MethodBinding[] otherMethods= candidate.declaringClass.getMethods(candidate.selector);
			methods:
			for (MethodBinding other: otherMethods) {
				if (other != candidate && other.parameters.length == candidate.parameters.length) {
					for(int i=0; i<other.parameters.length; i++)
						if (other.parameters[i] != candidate.parameters[i])
							continue methods;
					if (other == toLookFor)
						return currentRank;
					break;
				}
			}
		}
		int rank = rankMethod(toLookFor, candidate.copyInheritanceSrc, currentRank);
		if (rank < Integer.MAX_VALUE)
			return rank;
		rank = rankMethod(toLookFor.copyInheritanceSrc, candidate, currentRank);
		if (rank < Integer.MAX_VALUE)
			return rank;
		if (candidate.overriddenTSupers != null)
			for (int i=0; i < candidate.overriddenTSupers.length; i++) {
				rank = rankMethod(toLookFor, candidate.overriddenTSupers[i], currentRank);
				if (rank < Integer.MAX_VALUE)
					return rank;
			}
		if (toLookFor.overriddenTSupers != null)
			for (int i=0; i< toLookFor.overriddenTSupers.length; i++) {
				rank = rankMethod(toLookFor.overriddenTSupers[i], candidate, currentRank);
				if (rank < Integer.MAX_VALUE)
					return rank;
			}
		return Integer.MAX_VALUE;
	}
	/**
	 * Searches for enclosing Team of binding
	 * @param binding
	 * @return Team if available, null otherhwise
	 */
	private static ReferenceBinding getTeam(MethodBinding binding) {
		return getTeam(binding.declaringClass);
	}

	/**
	 * Searches for enclosing Team of binding
	 * @param binding
	 * @return Team if available, null otherhwise
	 */
	private static ReferenceBinding getTeam(FieldBinding binding) {
		return getTeam(binding.declaringClass);
	}

	/**
	 * Searches for enclosing Team of binding, including binding itself.
	 * @param binding
	 * @return Team if available, null otherhwise
	 */
	private static ReferenceBinding getTeam(ReferenceBinding binding) {
		if (binding.isTeam())
			return binding;
		return TeamModel.getEnclosingTeam(binding);
	}

	private static boolean isMappableMethod(MethodBinding refMethodBinding){
		if (isMappableClass(refMethodBinding.declaringClass))
			return true;
		if (isStaticBasecallSurrogate(refMethodBinding))
			return true;
		return false;
	}

	private static boolean isStaticBasecallSurrogate(MethodBinding refMethodBinding) {
		return    CharOperation.endsWith(refMethodBinding.selector, "$base".toCharArray()) //$NON-NLS-1$
		       && !refMethodBinding.declaringClass.isRole(); // regular base call surrogates are role methods.
															 // if it's not a role method, the callin must be static.
	}

	private static boolean isMappableField(FieldBinding refFieldBinding){
		return isMappableClass(refFieldBinding.declaringClass);
	}

	public static boolean isMappableClass(ReferenceBinding type) {
		if (type.isInterface())
			return false; // tsuper interface is good enough
		return TeamModel.getEnclosingTeam(type) != null;
	}

	/**
	 * Searches for a matching Roleclass in the destination Team which is given by the site of reference.
	 * For this the method will compare all roles from destination Team with the referenced Role.
	 * If one matching role ist found, this role will be returned
	 * The Role exists in this Team, because we have copied it before.
	 *
	 * @param template the referenced TypeBinding which must be a Roleclass of the dstTeamBinding-Superclass
	 * @param site the site where the new type reference is needed, either the team or one of its nested types.
	 * @return the found TypeBinding or null if no matching Role is found
	 */
	public static ReferenceBinding searchRoleClass(ReferenceBinding template, ReferenceBinding site)
	{
		if (!template.isRole()) // possible on external calls.
			return template;
		ReferenceBinding srcTeam = TeamModel.getEnclosingTeam(template);
		// different names are used depending on the kind of type:
		if (template.isLocalType())
			return searchRoleClass(template.constantPoolName(), srcTeam, site);
		else
			return searchRoleClass(template.internalName(), srcTeam, site);
	}

	/**
	 * Implementation of the above.
	 *
	 * TODO (SH): searching by simple name is not exact for nested teams!
	 *
	 * @return null ProblemReferenceBinding or a valid type in the context of 'site'
	 */
	private static ReferenceBinding searchRoleClass(char[] roleName, ReferenceBinding srcTeam, ReferenceBinding site)
	{
		// check the site type
		if (   site.isLocalType()
			&& CharOperation.equals(site.constantPoolName(), roleName))
			return site;
		// check its member types
		ReferenceBinding[] members = site.memberTypes();
		// TODO(SH): We observe binary and source of the same type.
		//           As a workaround prefer source over binary here:
		ReferenceBinding candidate = null;
		for (int i=0; i<members.length; i++) {
			if (CharOperation.equals(members[i].internalName(), roleName))
				if (members[i].isBinaryBinding() && candidate == null)
					candidate = members[i];
				else
					return members[i];
		}
		if (candidate != null)
			return candidate;
		for (int i=0; i<members.length; i++) {
			ReferenceBinding result = searchRoleClass(roleName, srcTeam, members[i]);
			if (result != null && result.isValidBinding())
				return result;
		}
		// delegate to role model, which can handle local types, too.
		if (site.isRole())
			return site.roleModel.findTypeRelative(
					TypeAnalyzer.constantPoolNameRelativeToTeam(srcTeam, roleName));
		return null; // caller in recursion may still find the type.
	}
}
//Markus Witte}

