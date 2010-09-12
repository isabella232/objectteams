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
 * $Id: CopyInheritance.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration.WrapperKind;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodVerifier;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.RoleClassLiteralAccess;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeContainerMethod;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ConstantPoolObjectMapper;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.CopyInheritanceSourceAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.InheritedRolesAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.OTSpecialAccessAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.StateHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config.NotConfiguredException;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticBaseCallSurrogate;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticRoleBridgeMethodBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticRoleFieldAccess;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.WeakenedTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.ReflectionGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.ReplaceSingleNameVisitor;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.SerializationGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.StandardElementGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstClone;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstConverter;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.Protections;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * This class implements all that is needed for implicit inheritance by
 * means of copying features from a tsuper-role to a current role.
 *
 * When the methods from this transformer are invoked, role classes are split
 * and all classes have their type bindings set and connected.
 * Method bodies have not been handled at all.
 *
 * Objects of translations:
 * + Some translations are performed on AST
 *   (using AstEdit, which also initializes bindings of newly created elements):
 *   - creation of new classes, method declarations, fields
 *     as copies of tsuper versions.
 *   - creation methods (full code).
 * + Typelevel translations only concern bindings
 *   - TypeLevel.connectRoleClasses
 *
 * Tasks:
 * + Copy declarations of classes, methods and fields, and establish
 *   superclass/superinterface linkage.
 * + Signature weakening: ensure that overrides use the role types from the
 *   team that first introduced a given method.
 *   - class methods are weakened when copyMethod finds a match between a
 *     tsuper version and an overriding method.
 *   - when the super role (extends) is in place, additional weakening might
 *     be required (weakenSignaturesFromExtends)
 *   - interface methods are weakened during types adjustment
 *     (here weakening simply means to remove the more specific version).
 *   Special story for signature weakening of creators and liftTo methods:
 *   - Always create AST with singleTypeReference -> resolve locally.
 *     Locations:
 *     + CopyInheritance.internalCreateCreationMethod
 *     + Lifting.createLiftToMethodDeclaration
 *     + AstConverter.createMethod
 *   - Explicitly create WeakenedTypeBinding if the affected role is inherited
 *     + RoleTypeCreator.wrapTypesInMethodDeclSignature
 *
 *
 * Entries:
 * + Most methods are invoked by Dependencies:
 *   - copyRolesFromTeam, addMarkerInterface
 *        for STATE_ROLE_INHERITANCE
 *   - copyFeatures, checkAllImplemented
 * 		  for STATE_ROLE_FEATURES_COPIED
 *   - createCreationMethod, weakenInterfaceSignatures, weakenSignaturesFromExtends, weakenTeamMethodSignatures
 *        for STATE_TYPES_ADJUSTED
 *   - copyGeneratedFeatures
 * 		  for STATE_MAPPINGS_TRANSFORMED
 *   - copyCastToMethods, copyLocalTypes, copyAttribute
 *        for STATE_LATE_ELEMENTS_COPIED
 * + copySyntheticFieldsAndMethods() is called by
 *        TypeDeclaration.generateCode()
 * + createConstructorMethodInvocationExpression is called by
 * 		  resolveType() in AllocationExpression and QualifiedAllocationExpression
 *
 *
 * @author Markus Witte
 * @version $Id: CopyInheritance.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class CopyInheritance implements IOTConstants, ClassFileConstants, ExtraCompilerModifiers, ITranslationStates
{

	/**
	 * Entry for ClassScope: copy into `teamBinding' all roles of its super and tsuper teams.
	 * Also connect roles with OT-specific links.
     */
	public static void copyRoles(SourceTypeBinding teamBinding) {
		if ((teamBinding.tagBits & TagBits.BeginCopyRoles) != 0)
			return;
		teamBinding.tagBits |= TagBits.BeginCopyRoles;

		TeamModel teamModel= teamBinding.getTeamModel();
		ReferenceBinding superTeam= teamBinding.superclass;
		if (superTeam == null) {
			assert (teamBinding.tagBits & TagBits.HierarchyHasProblems) != 0 : "Only broken teams can have null superclass"; //$NON-NLS-1$
			return;
		}

		// super team:
		TSuperHelper.addMarkerInterface(teamModel, superTeam);
		copyRolesFromTeam(superTeam, teamModel, false/*isTsuperTeam*/);

		// tsuper teams:
		if (teamBinding.isRole()) {
			RoleModel teamAsRole = teamBinding.roleModel;
			for (ReferenceBinding tsuperTeam: teamAsRole.getTSuperRoleBindings()) {
				TSuperHelper.addMarkerInterface(teamModel, tsuperTeam);
				copyRolesFromTeam(tsuperTeam, teamModel, true/*isTsuperTeam*/);
			}
		}

		teamModel.addAttribute(new InheritedRolesAttribute(teamModel.getBinding()));
	}

	/**
	 * Copy role from one (t)super team to the target team.
	 * @param sourceTeam either super or tsuper team of the target team
	 * @param targetTeamModel
	 * @param isTsuperTeam is the source team a tsuper team of the target (regular super otherwise)?
	 */
	private static void copyRolesFromTeam(
			ReferenceBinding sourceTeam,
			TeamModel        targetTeamModel,
			boolean          isTsuperTeam)
	{
		if (!sourceTeam.isTeam())
			return; // error which is detected elsewhere (see ProblemReporter.regularOverridesTeam())
		loadRoleFiles(sourceTeam, targetTeamModel.getBinding().memberTypes());
		loadRoleFiles(targetTeamModel.getBinding(), sourceTeam.memberTypes());

	    // 1. copy all roles (NO recursion for nested teams).
		doCopyRoles(sourceTeam, targetTeamModel, isTsuperTeam);
		connectRolesFromTeam(sourceTeam, targetTeamModel.getAst(), isTsuperTeam);
	}

	private static void connectRolesFromTeam(
			ReferenceBinding sourceTeam,
			TypeDeclaration  teamDeclaration,
			boolean          isTsuperTeam)
	{
	    // 2. copyClassProperties
	    //    connect synth-ifc to implicit super-ifc,
	    //    connect ifc-part to model
		TypeDeclaration[] subRoles = teamDeclaration.memberTypes;
	    if (subRoles != null) {
	    	//  map  < String name->TypeDeclaration > for copied role interfaces
	    	HashMap<String, TypeDeclaration> roleIfcs = new HashMap<String, TypeDeclaration>();
	    	// collect all synthetic interfaces:
	    	for (TypeDeclaration typeDeclaration : subRoles)
	    		if(typeDeclaration.isInterface() && !typeDeclaration.isRegularInterface())
	    			roleIfcs.put(new String(typeDeclaration.name), typeDeclaration);

	    	SupertypeObligation[][] obligations = new SupertypeObligation[subRoles.length][];
	        for (int i=0; i<subRoles.length; i++)
	        {
	            TypeDeclaration subRoleDecl = subRoles[i];
	            if ((subRoleDecl.modifiers & ClassFileConstants.AccEnum) != 0)
	                continue;
	            RoleModel       subRole     = subRoleDecl.getRoleModel();
	            obligations[i] =
	            	TypeLevel.connectRoleClasses(sourceTeam, subRoleDecl);
	            subRole.recordIfcPart(roleIfcs);

	            // additional checking for 1.4(a):
	            if (subRoleDecl.binding != null && subRoleDecl.binding.isSynthInterface())
					checkRoleShadows(teamDeclaration.scope, teamDeclaration.binding, subRoleDecl);
	        }
	        for (int i = 0; i < subRoles.length; i++) {
	            if ((subRoles[i].modifiers & ClassFileConstants.AccEnum) != 0)
	                continue;
	            verifyTSupers(subRoles[i].getRoleModel());
				SupertypeObligation[] obligs = obligations[i];
				for (int j = 0; j < obligs.length; j++) {
					obligs[j].check(subRoles[i]);
				}
			}
	    }
	}

	// additional check (OTJLD 1.4(a)):
	private static void checkRoleShadows(ClassScope scope, ReferenceBinding sourceType, TypeDeclaration memberDecl) {
		ReferenceBinding currentType = sourceType.enclosingType();
		char[] internalName= memberDecl.name;
		ReferenceBinding otherType;
		while (currentType != null) {
			otherType = scope.findMemberType(internalName, currentType);
			if (otherType != null && otherType.isValidBinding()) {
				if (TypeAnalyzer.isPredefinedRole(otherType))
					return;
				if (TSuperHelper.isMarkerInterface(otherType))
					return;
				scope.problemReporter().roleShadowsVisibleType(memberDecl, otherType);
				break;
			}
			currentType = currentType.enclosingType();
		}
		TypeBinding otherType2 = scope.compilationUnitScope().getType(internalName);
		if (otherType2 != null && otherType2.isValidBinding())
			scope.problemReporter().roleShadowsVisibleType(memberDecl, otherType2);
	}

	/** A binary team need not copy roles from super, but must connect roles to the
	 *  teamModel and to their tsupers.
	 */
	public static void connectBinaryTSupers(TeamModel subTeamModel) {
		ReferenceBinding subTeam = subTeamModel.getBinding();
		ReferenceBinding superTeam = subTeam.superclass();
		if (superTeam != null && superTeam.isTeam()) {
			Dependencies.ensureBindingState(superTeam, STATE_LENV_CONNECT_TYPE_HIERARCHY);
			for (ReferenceBinding tsuperRole : superTeam.memberTypes()) {
				connectToTSuperRole(subTeamModel, subTeam, tsuperRole);
			}
		}
	}

	/** Find a role within subTeam corresponding to tsuperRole and connect it to its tsuper. */
	private static void connectToTSuperRole(TeamModel subTeamModel, ReferenceBinding subTeam, ReferenceBinding tsuperRole)
	{
		ReferenceBinding subRole = subTeam.getMemberType(tsuperRole.internalName());
		if (subRole != null)
			if (subRole.roleModel == null)
				assert CharOperation.equals(subRole.internalName(), IOTConstants.ROFI_CACHE);
			else
				subRole.roleModel.connect(subTeamModel, tsuperRole);
	}

	/** Mark a state for a role and its members, but not nested roles. */
	private static void setRoleState(RoleModel subRole, int state) {
		subRole.setState(state);
		if (subRole.isTeam())
			// done this only for one level (not roles-as-teams):
			subRole.getTeamModelOfThis().setMemberStateShallow(state);
		else
			subRole.setMemberState(state);
	}


	/**
	 * Try to load all role types for a given team.
	 * (a) try a tsuper role for each role in tsubRoles
	 * (b) try a role type for each simple type reference in field and method sigs.
	 *
	 * @param sourceTeam
	 * @param suggestedRoles
	 */
	private static void loadRoleFiles(ReferenceBinding sourceTeam, ReferenceBinding[] suggestedRoles) {
		if (sourceTeam instanceof SourceTypeBinding) {
			// fetch scope  and AST:
			ClassScope scope = ((SourceTypeBinding)sourceTeam).scope;
			if (scope == null)
				return; // too late to add members :-((
			TypeDeclaration teamDecl = scope.referenceContext;

			HashSet<String> processed = new HashSet<String>();

			// load roles from super/sub team:
			for (int i = 0; i < suggestedRoles.length; i++) {
				char[] roleName = suggestedRoles[i].internalName();
				sourceTeam.getMemberType(roleName);
				processed.add(new String(roleName));
			}

			// scan fields:
			if (teamDecl.fields != null)
				for (int i = 0; i < teamDecl.fields.length; i++) {
					loadRoFiFromType(sourceTeam, teamDecl.fields[i].type, processed);
				}
			// scan methods:
			if (teamDecl.methods != null)
				for (int i = 0; i < teamDecl.methods.length; i++) {
					AbstractMethodDeclaration method = teamDecl.methods[i];
					if (method.arguments != null)
						for (int j = 0; j < method.arguments.length; j++) {
							loadRoFiFromType(sourceTeam, method.arguments[j].type, processed);
						}
					if (method instanceof MethodDeclaration)
						loadRoFiFromType(
								sourceTeam,
								((MethodDeclaration)method).returnType,
								processed);
				}
		}
	}

	private static void loadRoFiFromType (ReferenceBinding sourceTeam, TypeReference type, HashSet<String> processed) {
		if (type instanceof SingleTypeReference) {
			char[] typeName = ((SingleTypeReference)type).token;
			String typeString = new String(typeName);
			if (!processed.contains(typeString)) {
				ReferenceBinding rofi= sourceTeam.getMemberType(typeName);
				while (rofi != null && !rofi.isBinaryBinding()) {
					// if a rofi actually entered the compilation process it needs
					// to have its super types connected:
					SourceTypeBinding sourceRole= (SourceTypeBinding)rofi;
					if (   sourceRole.scope != null
						&& (sourceRole.tagBits & TagBits.BeginHierarchyCheck) == 0)
						sourceRole.scope.connectTypeHierarchyWithoutMembers();
					// do the same for the class part, too?
					if (sourceRole.isSynthInterface()) {
						rofi= sourceRole.getRealClass();
						if (rofi != null && rofi.isInterface())
							// observed an infinite loop, probably caused by rofi remaining unchanged by getRealClass above. Want to see why.
							throw new InternalCompilerError("Role has no class-part");   //$NON-NLS-1$
					} else {
						rofi= null; // terminate loop
					}
				}

				processed.add(typeString);
			}
		}
	}


	/**
     * @param srcTeam where to copy roles from
	 * @param targetTeam destination team
	 * @param isTsuperTeam is the super team inherited implicitly?
	 */
	private static void doCopyRoles(
			ReferenceBinding srcTeam,
			TeamModel targetTeam,
			boolean isTsuperTeam)
	{
		ReferenceBinding[] tsuperRoles = srcTeam.getTeamModel().getKnownRoles();
		if (tsuperRoles == null)
			return;

		TypeDeclaration teamDeclaration = targetTeam.getAst();
		for(int i=0;i<tsuperRoles.length;i++)
		{
		    ReferenceBinding tsuperRoleBinding = tsuperRoles[i];
	        TypeDeclaration subRoleType = copyRole (
	                tsuperRoleBinding,
					false,
	                teamDeclaration,
					isTsuperTeam);

	        if (subRoleType != null) {
	            RoleModel subRoleModel = subRoleType.getRoleModel();
	            subRoleModel.connect(targetTeam, tsuperRoleBinding);
	        } else {
				// subrole already existed, connect it now:
	        	connectToTSuperRole(targetTeam, teamDeclaration.binding, tsuperRoleBinding);
	        }
		}
	}
 	/** Check for OTJLD 1.5(d) */
	private static void verifyTSupers(RoleModel role) {
		if (!role.getAst().isInterface())
			return; // analyzing interfaces suffices, they have all tsuper information.
		ReferenceBinding[] tsupers = role.getTSuperRoleBindings();
		if (tsupers.length > 1) {
			for (int i = 0; i < tsupers.length; i++) {
				for (int j = 0; j < tsupers.length; j++) {
					if (i!=j) {
						ReferenceBinding common = findCommonSuper(tsupers[i], tsupers[j]);
						if (common == null) {
							TypeDeclaration roleDecl = role.getAst();
							roleDecl.scope.problemReporter().incomparableTSupers(roleDecl, tsupers[i], tsupers[j]);
							return;
						}
					}
				}
			}
		}
	}
	// helper for verifyTSupers() above.
	private static ReferenceBinding findCommonSuper(ReferenceBinding type1, ReferenceBinding type2)
	{
		ReferenceBinding current = type1;
		while(current != null && current.depth() == type1.depth()) // don't accept super class from outer scope
		{
			if (type2.isCompatibleWith(current))
				return current;
			current = current.superclass();
		}
		ReferenceBinding[] superInterfaces = type1.superInterfaces();
		for (int i = 0; i < superInterfaces.length; i++) {
			ReferenceBinding common = findCommonSuper(superInterfaces[i], type2);
			if (common != null)
				return common;
		}
		return null;
	}

	public static boolean copyLocalTypes(RoleModel roleModel) {
		// TODO (SH): ensuring equals constantPoolName does probably not yet work:
		//            first assign indices to COPIED locals, *then* to locally defined ones!.
		// copy local types from tsuper role:
	    ReferenceBinding[] tsuperRoles = roleModel.getTSuperRoleBindings();
	    TeamModel teamModel = roleModel.getTeamModel();
	    for (int i = 0; i < tsuperRoles.length; i++) {
	    	ReferenceBinding tsuperRole = tsuperRoles[i];
		    if (tsuperRole != null) {
			    ReferenceBinding superTeam = tsuperRole.enclosingType();
			    RoleModel tsuperModel = tsuperRole.roleModel;
			    // require recursive copying from direct and indirect tsuper roles:
			    Dependencies.ensureRoleState(tsuperModel, STATE_LATE_ELEMENTS_COPIED);
			    Iterator<RoleModel> tsuperLocals = tsuperModel.localTypes();
			    while (tsuperLocals.hasNext()) {
			    	RoleModel tsuperLocal = tsuperLocals.next();
			    	Dependencies.ensureRoleState(tsuperLocal, STATE_RESOLVED);
					ReferenceBinding tsuperNestedBinding = tsuperLocal.getBinding();
			    	TypeDeclaration localDecl = copyRoleNestedInternal(teamModel, roleModel, tsuperNestedBinding);
			    	if (localDecl != null) {
			    		TypeLevel.mergeSuperinterfaces(superTeam, tsuperNestedBinding, localDecl.binding);
			    		TypeLevel.copyAdjustSuperclass(tsuperNestedBinding, localDecl, null);
			    	}
			    }
		    }
		}
		return true;
	}
    /**
	 * @param destTeamModel
	 * @param destRole
	 * @param tsuperNestedBinding
	 */
	private static TypeDeclaration copyRoleNestedInternal(
			TeamModel         destTeamModel,
			RoleModel         destRole,
			ReferenceBinding  tsuperNestedBinding)
	{
		TypeDeclaration subRoleType = copyRole (tsuperNestedBinding, true, destTeamModel.getAst(), false);
		if (subRoleType != null) {
			RoleModel subRoleModel = subRoleType.getRoleModel();
			subRoleModel.connect(destTeamModel, tsuperNestedBinding);
			if (tsuperNestedBinding.isLocalType())
				destRole.addLocalType(tsuperNestedBinding.constantPoolName(), subRoleModel);
		}
		return subRoleType;
	}

	/**
	 * If a tsuper role is not overriden in the current team,
	 * create a fresh new role type declaration.
	 * Always copy all inheritance information to the tsub role
	 * (extends, implements, playedBy).
	 * No difference, whether we are looking at a role class or interface.
	 *
	 * @param superRole
	 * @param subTeamDecl this declaration is being edited.
	 * @return the new or modified type declaration
	 */
	private static TypeDeclaration copyRole (
	        ReferenceBinding superRole,
			boolean          isNestedType,
	        TypeDeclaration  subTeamDecl,
			boolean          isTsuperTeam)
	{
		subTeamDecl.getTeamModel()._isCopyingLateRole = true;
		try {
			if (superRole instanceof MissingTypeBinding)
				return null; // don't copy missing type!
			if (superRole.sourceName == null)
				return null; // local types have null name
		    String name = new String(superRole.sourceName);
		    if((name.startsWith(TSUPER_OT) || CharOperation.equals(superRole.sourceName, ROFI_CACHE)))
		        return null; // don't copy these special roles

		    if (   subTeamDecl.isRole()
		    	&& superRole.roleModel.equals(subTeamDecl.getRoleModel()))
		    	return null; // can happen in case of a role extending its enclosing

		    TypeDeclaration subRoleDecl =
		        findMemberType(subTeamDecl, superRole.sourceName);

	    	ReferenceBinding subRoleBinding = subTeamDecl.binding.getMemberType(superRole.internalName());
	    	if (subRoleBinding != null) {
	    	    // don't copy binary tsuper, if a binary exists already here.
	    		if (shouldPreserveBinaryRole(subRoleBinding, subTeamDecl.compilationResult)) {
	    			if (isNestedType)
	    				subRoleBinding.roleModel.maybeAddLocalToEnclosing();
	    			// no further processing needed except for connecting tsuper and copyInheritanceSrc:
	    			connectBinaryNested(superRole, subTeamDecl, subRoleBinding);
	    			return null;
	    		}
	    		// try again, memberType lookup might have introduced a role file to subTeamDecl:
	    		if (subRoleDecl == null)
	    			subRoleDecl = findMemberType(subTeamDecl, superRole.sourceName);
	    		if (subRoleDecl != null && (subRoleDecl.binding.tagBits & TagBits.BeginHierarchyCheck) == 0)
	    			subRoleDecl.scope.connectTypeHierarchyWithoutMembers();
	    		if (subRoleDecl == null) // still?
	    			return null; // assume recompile has been scheduled
	    	}

		    // If type doesn't exist, create now
		    if(subRoleDecl == null)
		    {
		    	char[] superRoleName = superRole.internalName();
	   			if (superRole.isLocalType()) {
	   		    	if (!superRole.isBinaryBinding())
	   		    		 ((LocalTypeBinding)superRole).computeConstantPoolName();
		    		int lastDollar = CharOperation.lastIndexOf('$', superRole.sourceName);
		    		if (lastDollar >= 0) {
		    			superRoleName = CharOperation.subarray(superRole.sourceName, lastDollar+1, -1);
		    		} else {
		    			char[] superConstantPoolName = superRole.constantPoolName();
						lastDollar = CharOperation.lastIndexOf('$', superConstantPoolName);
		    			if (lastDollar >= 0)
		    				superRoleName = CharOperation.subarray(superConstantPoolName, lastDollar+1, -1);
		    		}
		    	}
		        subRoleDecl = AstConverter.createNestedType(
		                superRoleName,
		                superRole.modifiers,
						isNestedType,
		                true, // purely copied
						subTeamDecl,
						superRole);
		        if (subRoleDecl.isInterface()) {
		        	// purely copied interface now copies superinterfaces (not handled in connectRolesFromTeam()):
			        ReferenceBinding[] tsuperSupers = superRole.superInterfaces();
			        subRoleDecl.binding.superInterfaces = new ReferenceBinding[tsuperSupers.length];
			        int j=0;
			        for (int i = 0; i < tsuperSupers.length; i++) {
						char[] tsuperSuperName = tsuperSupers[i].internalName();
						if (!CharOperation.equals(tsuperSuperName, superRoleName)) {
							ReferenceBinding tsubRole = subTeamDecl.binding.getMemberType(tsuperSuperName);
							if (tsubRole != null)
								subRoleDecl.binding.superInterfaces[j++] = tsubRole;
						}
					}
					if (j<tsuperSupers.length)
						System.arraycopy(subRoleDecl.binding.superInterfaces, 0, subRoleDecl.binding.superInterfaces = new ReferenceBinding[j], 0, j);
		        }
		    } else {
		    	if (subRoleDecl.isRegularInterface() != superRole.isRegularInterface()) {
					subRoleDecl.scope.problemReporter().roleClassIfcConflict(subRoleDecl);
					// overwrite existing type with tsuper copy:
					subRoleDecl.isGenerated = true;
					subRoleDecl.isPurelyCopied = true;
					subRoleDecl.modifiers = superRole.modifiers;
					subRoleDecl.fields = null;
					subRoleDecl.methods = null;
					subRoleDecl.superclass = null;
					subRoleDecl.superInterfaces = null;
			        SourceTypeBinding roleBinding  = subRoleDecl.binding;
			        roleBinding.modifiers = superRole.modifiers;
			        roleBinding.setFields(Binding.NO_FIELDS);
			        roleBinding.setMethods(Binding.NO_METHODS);
			        roleBinding.baseclass =  null;
			        roleBinding.superclass = subTeamDecl.scope.getJavaLangObject();
			        roleBinding.superInterfaces = Binding.NO_SUPERINTERFACES;
					return subRoleDecl;
		    	}
		    	if (    superRole.isTeam()
		    		&& !subRoleDecl.isTeam())
		    	{
		    		if (!Protections.hasClassKindProblem(subRoleDecl.binding))
		    			subRoleDecl.scope.problemReporter().regularOverridesTeam(subRoleDecl, superRole);
		    		subRoleDecl.modifiers |= ClassFileConstants.AccTeam;
		    		if (subRoleBinding != null)
		    			subRoleBinding.modifiers |= ClassFileConstants.AccTeam;
		    	}
		    	if (!isTsuperTeam) {
		    		if (CharOperation.equals(subRoleDecl.name, OTCONFINED)) {
		        		subRoleDecl.scope.problemReporter().overridingConfined(subRoleDecl, "Confined"); //$NON-NLS-1$
		        		return null;
		    		}
		    		if (CharOperation.equals(subRoleDecl.name, ICONFINED)) {
		    			subRoleDecl.scope.problemReporter().overridingConfined(subRoleDecl, "IConfined"); //$NON-NLS-1$
		    			return null;
		    		}
		    		if (superRole.isFinal()) {
		    			subRoleDecl.scope.problemReporter().overridingFinalRole(subRoleDecl, superRole);
		    			return null;
		    		}
		    	}
		    }

		    if (superRole.roleModel.hasBaseclassProblem())
		    {
		        subRoleDecl.binding.tagBits |= TagBits.BaseclassHasProblems;
		    }
	//		if (subRoleBinding != null && subRoleBinding.isBinaryBinding())
	//			subRoleDecl.scope.problemReporter().mismatchingRoleParts(subRoleBinding, subRoleDecl);
		    return subRoleDecl;
		} finally {
			subTeamDecl.getTeamModel()._isCopyingLateRole = false;
		}
	}

	public static void connectBinaryNested(ReferenceBinding superRole, TypeDeclaration subTeam, ReferenceBinding subRole)
	{
		subRole.roleModel.connect(subTeam.getTeamModel(), superRole);
		Dependencies.ensureBindingState(subRole, ITranslationStates.STATE_LATE_ATTRIBUTES_EVALUATED);
		subTeam.scope.compilationUnitScope().registerBinaryNested(subRole);
	}

	public static ReferenceBinding copyLateRole(TypeDeclaration teamDecl, ReferenceBinding tsuperRole) {
		TypeDeclaration roleType = null;
		ReferenceBinding ifcPart = null;
		char[] tsuperName = tsuperRole.internalName();
		if (CharOperation.prefixEquals(IOTConstants.OT_DELIM_NAME, tsuperName)) {
			char[] ifcName = CharOperation.subarray(tsuperName, IOTConstants.OT_DELIM_LEN, -1);
			ifcPart = teamDecl.binding.getMemberType(ifcName);
		}
		roleType = copyLateRolePart(teamDecl, tsuperRole);
		if (ifcPart != null) {
			// FIXME(SH): improve catch up: connecting bindings etc.
			Dependencies.ensureBindingState(tsuperRole.enclosingType(), ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY);
			if (StateHelper.hasState(tsuperRole, ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY)) {
	            RoleModel subRole = roleType.getRoleModel();
	           	TypeLevel.connectRoleClasses(tsuperRole.enclosingType(), roleType);
	//            subRole.set = ifcPart;
	//            ifcPart.roleModel._classPart = roleType.binding;
	            setRoleState(subRole, STATE_LENV_CONNECT_TYPE_HIERARCHY);
			}
		}
		if (roleType == null) return null;
		Dependencies.lateRolesCatchup(teamDecl.getTeamModel());
		return roleType.binding;
	}
	private static TypeDeclaration copyLateRolePart(TypeDeclaration teamDecl, ReferenceBinding tsuperRole) {
		TypeDeclaration roleDecl = copyRole(tsuperRole, false, teamDecl, false);
        if (roleDecl != null) {
        	TeamModel teamModel = teamDecl.getTeamModel();
            RoleModel subRoleModel = roleDecl.getRoleModel();
            subRoleModel.connect(teamModel, tsuperRole);
            return roleDecl;
        }
        return null;
	}
	/** Hook for the BuildManager. */
	private static boolean shouldPreserveBinaryRole(ReferenceBinding subRoleBinding,
													CompilationResult result)
	{
		return subRoleBinding.isBinaryBinding();
	}

    /**
     * Copy a byte code attribute from tsuper role.
     * Currently only supported: CallinMethodMappingsAttribute
     *
     * @param subRole
     */
    public static void copyAttribute(RoleModel subRole)
    {
    	if (subRole != null) {
	    	ReferenceBinding[] tsuperRoles = subRole.getTSuperRoleBindings();
	    	for (int i = 0; i < tsuperRoles.length; i++) {
	    		Dependencies.ensureBindingState(tsuperRoles[i], ITranslationStates.STATE_CALLINS_TRANSFORMED);
	    		subRole.copyAttributeFrom(tsuperRoles[i].roleModel, IOTConstants.CALLIN_METHOD_MAPPINGS);
			}
    	}
    }
    /**
     * Copy a byte code attribute from super team.
     * Currently only supported: StaticReplaceBindingsAttribute
     *
     * @param subTeam
     */
    public static void copyAttribute(TeamModel subTeam)
    {
    	if (   subTeam != null
    		&& !TypeAnalyzer.isOrgObjectteamsTeam(subTeam.getBinding()))
    	{
	    	ReferenceBinding superTeam = subTeam.getBinding().superclass();
	    	if (   superTeam != null
	    		&& superTeam.isValidBinding()
	    		&& superTeam.isTeam()
	    		&& !TypeAnalyzer.isOrgObjectteamsTeam(superTeam))
	    	{
	            Dependencies.ensureBindingState(superTeam, STATE_CALLINS_TRANSFORMED);
	    		subTeam.copyAttributeFrom(superTeam.getTeamModel(), IOTConstants.STATIC_REPLACE_BINDINGS);
	    		//subTeam.copyAttributeFrom(superTeam.getTeamModel(), OTDynCallinBindingsAttribute.ATTRIBUTE_NAME);
	    		OTSpecialAccessAttribute attrib = (OTSpecialAccessAttribute)superTeam.getTeamModel().getAttribute(IOTConstants.OTSPECIAL_ACCESS);
	    		if (attrib != null)
	    			attrib.addFieldAccessesTo(subTeam);
	    	}
    	}
    }
   	/**
	 * If adjustSuperinterfaces updates a superinterface, we will check compatibility,
	 * ie., the new superinterface must be a subtype from the originally resolved version.
	 * Reasons why this could fail should be sought in the field of nested teams,
	 * where independent super-teams may lead to name-clashes?!
	 */
    static class SupertypeObligation {
    	ReferenceBinding thatSuperType;
    	ReferenceBinding thisSuperType;
    	ASTNode location;
    	ReferenceBinding outerTSuper; // where the outer extends was inherited from
		SupertypeObligation(ReferenceBinding thisSupertype, ReferenceBinding thatSupertype, ASTNode location, ReferenceBinding outerTSuper)
		{
			this.thisSuperType = thisSupertype;
			this.thatSuperType = thatSupertype;
			this.location = location;
			this.outerTSuper = outerTSuper;
		}
		void check(TypeDeclaration subRole) {
			if (!this.thisSuperType.isCompatibleWith(this.thatSuperType))
				// this message is not precise (could be superinterfaces),
				// and I'm not sure whether the parameters actually make sense..
				subRole.scope.problemReporter().incompatibleSuperclasses(
						(this.location != null) ? this.location : subRole,
						this.thisSuperType,
						this.thatSuperType,
						this.outerTSuper);
		}
    }

    // ============ FEATURE LEVEL ============

	/**
	 * Copy all fields and methods from tsuperRoleBinding to role
	 *
	 * @param role
	 * @param tsuperRoleBinding
	 */
	public static void copyFeatures(RoleModel role, ReferenceBinding tsuperRoleBinding) {
		TypeDeclaration rt = role.getAst();
		// fields first (may be needed by synthetic methods below).
		FieldBinding[] fields = tsuperRoleBinding.fields();
		if(fields != null)
		    for(int j=0; j<fields.length; j++)
		        copyField(fields[j],rt);

		// regular methods.
		MethodBinding[] methods = tsuperRoleBinding.methods();
		if(methods != null)
		    for(int j=0; j<methods.length; j++)
		        copyMethod(methods[j],rt);
	}

	/**
	 * Copy features that have been generated after STATE_ROLE_FEATURES_COPIED.
	 *
	 * @param model
	 */
	public static void copyGeneratedFeatures(RoleModel model) {
		TypeDeclaration roleType = model.getAst();
		ReferenceBinding[] tsuperRoles = model.getTSuperRoleBindings();
		for (int i = 0; i < tsuperRoles.length; i++) {
			if (tsuperRoles[i].isInterface())
				continue;
			FieldBinding[] fields = tsuperRoles[i].fields();
			for (int j = 0; j < fields.length; j++) {
				if (!model.hasAlreadyBeenCopied(fields[j]))
					copyField(fields[j], roleType);
			}
			MethodBinding[] methods = tsuperRoles[i].methods();
			for (int j = 0; j < methods.length; j++) {
				if (model.hasAlreadyBeenCopied(methods[j])) {
					if (!methods[j].isAbstract()) {
						// implemented previously abstract method?
						MethodBinding[] existingMethod = model.getBinding().methods();
						for (int k = 0; k < existingMethod.length; k++) {
							if (   existingMethod[k].isCopiedFrom(methods[j])
								&& existingMethod[k].isAbstract())
							{
								// keep methods and update modifiers:
								existingMethod[k].modifiers &= ~(AccAbstract|AccSemicolonBody);
								AbstractMethodDeclaration decl = existingMethod[k].sourceMethod();
								if (decl != null)
									decl.modifiers &= ~(AccAbstract|AccSemicolonBody);
								break;
							}
						}
					}
				} else {
					copyMethod(methods[j], roleType);
				}
			}
		}
	}

	/**
	 * Copy a given method to the tsub-role.
	 * Adds the new method to AST and performs initial creation of bindings.
	 * @param method  method to copy
	 * @param targetRoleDecl target role class
	 */
	private static void copyMethod(
	        MethodBinding 	method,
	        TypeDeclaration targetRoleDecl)
	{
		boolean wasSynthetic = false;
		ReferenceBinding site = null;


	    if ((method.modifiers & AccSynthetic) != 0) {
			wasSynthetic = true;
			// some, but not all, synthetics shall be generated with strong signatures as indicated by 'site':
			if (!SyntheticBaseCallSurrogate.isBaseCallSurrogateName(method.selector))
				site = targetRoleDecl.binding;
			if (SyntheticRoleBridgeMethodBinding.isPrivateBridgeSelector(method.selector))
				return; // will be generated anew
		}

		if (TypeContainerMethod.isTypeContainer(method))
			return;  // don't copy these dummy methods

	    if (   isCreator(method)
	    	|| CharOperation.equals(IOTConstants._OT_GETBASE, method.selector)
	    	|| CharOperation.prefixEquals(IOTConstants.CAST_PREFIX, method.selector)
	    	|| CharOperation.prefixEquals(IOTConstants.GET_CLASS_PREFIX, method.selector))
	        return; // create/getBase/cast/getClass-methods are generated anew in each team.
	    	// (can only happen in a team that is a role of an outer team.)
	        // Note: we don't use AccSynthetic on creators, because
	        // Synthetic methods are not read from byte code.
	    	// FIXME(SH): this last note is not true any more.

	    if (   targetRoleDecl.isTeam() 
	    	&& 
			   (   ReflectionGenerator.isReflectionMethod(method) 
				|| SerializationGenerator.isSerializationMethod(method)))
	    	return;
	    if (MethodModel.isFakedMethod(method))
	    	return; // will be generated anew (basecall-surrogate, rolefield-bridge)

	    if (CharOperation.equals(IOTConstants.MIGRATE_TO_TEAM, method.selector))
	    	return; // can only be used from the exact team

	    // avoid copying twice (see copyGeneratedFeatures()):
	    targetRoleDecl.getRoleModel().recordCopiedFeature(method);

	    // some useful objects:
	    ReferenceBinding srcRole = method.declaringClass;
	    TypeDeclaration targetTeamDecl = targetRoleDecl.enclosingType;
	    ReferenceBinding srcTeam = TeamModel.getEnclosingTeam(srcRole);
	    ReferenceBinding tgtTeam = targetTeamDecl.binding;

	    MethodBinding origin = (method.copyInheritanceSrc != null) ?
									method.copyInheritanceSrc :
									method;

	    AbstractMethodDeclaration methodFound = findMethod(
	            srcTeam, method,
	            tgtTeam, targetRoleDecl);

	    if (method.isConstructor()) {
	    	if (CharOperation.equals(srcRole.compoundName, IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED))
	        {
	        	// must add default constructor explicitly,
	        	// since if we would copy the one from Team.__OT__Confined,
	        	// it would invoke the wrong super().
	        	ConstructorDeclaration ctor =
	        		targetRoleDecl.createDefaultConstructor(true, true);
	       		targetRoleDecl.binding.resolveGeneratedMethod(ctor, false);
	        	return;
	        }
	    	else if (targetRoleDecl.getRoleModel()._refinesExtends)
	        {
	        	// even if we can't copy the ctor, we need to mark
	        	// the current ctor as overriding the tsuper version.
	    		if (methodFound != null)
	    			methodFound.binding.addOverriddenTSuper(method);
	        	return; // extends is covariantly redefined, don't copy ctor!
	        	// attempts to invoke this tsuper ctor are caught in ExplicitConstructorCall.resolve().
	        }
	    }

	    // If method already exists in subteam,
	    // + adjust method in subteam to the more general signature (here)
	    // + extend copy with marker arg (below).
	    // else give an exact copy.
	    if(methodFound != null)
	    {
	        // do not touch broken methods:
	    	// (methodFound might have no binding,
	        //  e.g., due to a missing import of return-type)
	    	if (methodFound.binding == null)
	    		return;

	    	if (methodFound.binding.copyInheritanceSrc == origin)
	    		return; // diamond inheritance: copying the same method from two different sources

	    	// do this in any case so that ConstantPoolObjectMapper won't fail:
	    	methodFound.binding.addOverriddenTSuper(method);

	    	// abstract methods have no byte code to copy;
	    	// new method silently replaces abstract version:
	        if (method.isAbstract()) {
		        weakenSignature(methodFound, method);
	        	return;
	        }

			if (method.isFinal()) {
				methodFound.scope.problemReporter().finalMethodCannotBeOverridden(methodFound.binding, method);
				return;
			}
	        weakenSignature(methodFound, method);
	        MethodBinding foundSrc = methodFound.binding.copyInheritanceSrc;
	        if (   foundSrc != null
	        	&& foundSrc != origin)
	        {
	        	// found a copied method which has a different origin, choose the more specific:
        		if (TeamModel.isMoreSpecificThan(origin.declaringClass,foundSrc.declaringClass))
				{
	        		// more specific method overwrites previous linkage:
		        	methodFound.binding.setCopyInheritanceSrc(origin);
		        	methodFound.sourceMethodBinding = origin;
		        	methodFound.isTSuper = TSuperHelper.isTSuper(method);
		        	// TODO(SH): also update CopyInheritanceSrc-Attribute!
		        	if (!method.isAbstract()) {
		        		// not abstract any more, joining abstract and implemented methods:
		    			methodFound.modifiers &= ~(AccAbstract|AccSemicolonBody);
		    			methodFound.binding.modifiers &= ~(AccAbstract|AccSemicolonBody);
		    			// TODO(SH): might need multiple copyInheritanceSrc!
		    			// TODO(SH) need to adjust copiedInContext?
		        	}
	        	}
	        	return;
	        }
	    }
	    AstGenerator gen = new AstGenerator(targetRoleDecl.sourceStart, targetRoleDecl.sourceEnd);
	    gen.replaceableEnclosingClass = tgtTeam;
	    AbstractMethodDeclaration newMethodDecl =
	    		AstConverter.createMethod(method, site, targetRoleDecl.compilationResult, DecapsulationState.REPORTED, gen);

	    if (methodFound != null)
	        TSuperHelper.addMarkerArg(newMethodDecl, srcTeam);

	    if(newMethodDecl.isConstructor()){
	        // comments (SH):
	        // other phases may depend on this field (constructorCall) being set,
	        // although it carries no real information.
	        ConstructorDeclaration cd = (ConstructorDeclaration)newMethodDecl;
	        cd.constructorCall = SuperReference.implicitSuperConstructorCall();

	        if (   Lifting.isLiftingCtor(method)
		    	&& method.parameters[0].isRole())
		    {
	        	// if baseclass is implicitely redefined use the strong type:
	        	ReferenceBinding newBase= targetRoleDecl.binding.baseclass();
	        	if (newBase != method.parameters[0])
	        		newMethodDecl.arguments[0].type= gen.baseclassReference(newBase);
		    }
	    }

    	AstEdit.addMethod(targetRoleDecl, newMethodDecl, wasSynthetic, false/*addToFront*/);
    	if (method.isPrivate())
    		newMethodDecl.binding.modifiers |= ExtraCompilerModifiers.AccLocallyUsed; // don't warn unused copied method

	    newMethodDecl.binding.setCopyInheritanceSrc(origin);
	    newMethodDecl.binding.copiedInContext = tgtTeam.enclosingType();
	    MethodModel newModel = MethodModel.getModel(newMethodDecl);
	    newModel.addAttribute(CopyInheritanceSourceAttribute.copyInherSrcAttribute(origin, newModel));
    	if (wasSynthetic)
    		targetRoleDecl.getRoleModel().addSyntheticMethodMapping(method, newMethodDecl.binding);

	    if (method.isAnyCallin() && !method.isCallin()) // callin wrapper
	    	newMethodDecl.isMappingWrapper = WrapperKind.CALLIN;

	    if (methodFound == null) {
	    	// copy down some more properties:
	    	if (TSuperHelper.isTSuper(method))
	    		newMethodDecl.isTSuper = true;
	    	if (method.model != null && method.model.callinFlags != 0)
	    		MethodModel.addCallinFlag(newMethodDecl, method.model.callinFlags);
	    	if (method.isAnyCallin()) {
	    		TypeBinding inheritedSrcReturn = MethodModel.getReturnType(method);
	    		if (inheritedSrcReturn.isRole())
	    			inheritedSrcReturn = RoleTypeCreator.maybeWrapUnqualifiedRoleType(inheritedSrcReturn, targetRoleDecl.binding);
				MethodModel.saveReturnType(newMethodDecl.binding, inheritedSrcReturn);
	    	} else {
			    if (   !method.isPublic()                  // non-public source-level class method?
			    	&& !method.isConstructor()
			    	&& !targetRoleDecl.isInterface()
			    	&& !CharOperation.prefixEquals(IOTConstants.OT_DOLLAR_NAME, method.selector))
			    {
			    	MethodModel.getModel(newMethodDecl).storeModifiers(newMethodDecl.modifiers);
			    }
	    	}
	    }
	}


	/**
	 * Copy all synthetic access methods to this role from its tsuper role.
	 * Also re-create synthetic fields.
	 *
	 * @param roleType
	 */
	public static void copySyntheticRoleFieldsAndMethods(TypeDeclaration roleType) {
		ReferenceBinding[] tsuperRoleBindings = roleType.getRoleModel().getTSuperRoleBindings();
		for (int i = 0; i < tsuperRoleBindings.length; i++) {
			if (tsuperRoleBindings[i] instanceof SourceTypeBinding) {
				// binary types have their synthetic methods already during regular copy inheritance.
				// TODO (SH): using RoleModel.addSyntheticMethodMapping would still be required for
				// full robustness, if several synthetic methods would otherwise conflict.
				SyntheticMethodBinding[] synthMethods =
					((SourceTypeBinding)tsuperRoleBindings[i]).syntheticMethods();
				if (synthMethods != null) {
					for (int j=0; j<synthMethods.length; j++)
						copySyntheticMethod(synthMethods[j], roleType, roleType.getRoleModel(), roleType.enclosingType);
				}

				FieldBinding[] synthFields =
					((SourceTypeBinding)tsuperRoleBindings[i]).syntheticFields();
				if (synthFields != null) {
					for (int j = 0; j < synthFields.length; j++)
						roleType.binding.addCopiedSyntheticFied(synthFields[j]);
				}
			}
		}
	}

	/**
	 * Copy all inherited SyntheticRoleFieldAccess-methods to teamType.
	 * Variant for source super-team (need those super-synthetics to be generated).
	 *
	 * @param teamType
	 */
	public static void copySyntheticTeamMethods(TypeDeclaration teamType) {
		if (teamType.binding == null)
			return; // no chance
		ReferenceBinding superTeam = teamType.binding.superclass();
		if (superTeam instanceof SourceTypeBinding && superTeam.isTeam()) {
			Dependencies.ensureTeamState(superTeam.getTeamModel(), ITranslationStates.STATE_BYTE_CODE_GENERATED);
			SyntheticMethodBinding[] synthMethods =
				((SourceTypeBinding)superTeam).syntheticMethods();
			if (synthMethods != null) {
				for (int j=0; j<synthMethods.length; j++) {
					if (synthMethods[j] instanceof SyntheticRoleFieldAccess)
						copySyntheticMethod(synthMethods[j], teamType, teamType);
				}
			}
		}
	}

	/**
	 * Copy all SyntheticRoleFieldAccess-methods from superTeam to teamModel.
	 * Variant for binary super-team.
	 *
	 * @param teamModel
	 * @param superTeam
	 */
	public static void copySyntheticTeamMethods(TeamModel teamModel, BinaryTypeBinding superTeam)
	{
		// Note: the following methods are helping to map this synth methods from tsuper to tsub:
		//       ConstantPoolObjectReader.findMethodBinding()
		//       RoleModel.mapSyntheticMethod()
		TypeDeclaration tgtTeamDecl = teamModel.getAst();
		for (MethodBinding method : superTeam.methods()) {
			if (method instanceof SyntheticRoleFieldAccess) {
				SyntheticRoleFieldAccess srcMethod = (SyntheticRoleFieldAccess)method;
				srcMethod.resolveTypes();
				srcMethod.resolvedField();
				copySyntheticMethod(srcMethod, tgtTeamDecl, tgtTeamDecl);
			}
		}
	}

    private static void copySyntheticMethod (
    			SyntheticMethodBinding srcMethod,
				TypeDeclaration        tgtTypeDecl,
				RoleModel              tgtRoleModel,
				TypeDeclaration        targetTeamDecl)
    {
    	MethodBinding dstMethod = copySyntheticMethod(srcMethod, tgtTypeDecl, targetTeamDecl);
		if (dstMethod != null)
			tgtRoleModel.addSyntheticMethodMapping(srcMethod, dstMethod);
    }

	/**
     * Copy a synthetic access method to `tgtTypeDecl'.
     *
     * For SyntheticRoleFieldAccess the purpose of copying is:
     * - override the inherited method (requires sign.-weakening)
     * - update the contained cast to the suitable role class.
     *
     * @param srcMethod
     * @param tgtTypeDecl either role or team.
     * @param targetTeamDecl equal to tgtTypeDecl or its enclosing
     */
    private static MethodBinding copySyntheticMethod (
			SyntheticMethodBinding srcMethod,
			TypeDeclaration        tgtTypeDecl,
			TypeDeclaration        targetTeamDecl)
    {
    	MethodBinding dstMethod = null;
    	boolean isSuperAccess = false;
    	if ((srcMethod.modifiers & AccSynthetic) != 0) {
    		FieldBinding srcField, dstField;
    		switch (srcMethod.purpose) {
    		case SyntheticMethodBinding.SuperFieldReadAccess:
    			isSuperAccess = true;
    			//$FALL-THROUGH$
    		case SyntheticMethodBinding.FieldReadAccess:
    			if (srcMethod instanceof SyntheticRoleFieldAccess)
    				srcField = ((SyntheticRoleFieldAccess) srcMethod).resolvedField(); // ensure binding is initialized.
    			else
    				srcField = srcMethod.targetReadField;
    			dstField = ConstantPoolObjectMapper.mapField(srcMethod, srcField, targetTeamDecl.binding);
    			dstMethod = tgtTypeDecl.binding.addSyntheticMethod(dstField, true /* read */, isSuperAccess, false);
    			if (!srcField.isStatic() && (srcMethod instanceof SyntheticRoleFieldAccess))
    				dstMethod.parameters[0] = srcMethod.parameters[0]; // manual weakening
    			break;
    		case SyntheticMethodBinding.SuperFieldWriteAccess:
    			isSuperAccess = true;
    			//$FALL-THROUGH$
    		case SyntheticMethodBinding.FieldWriteAccess:
    			if (srcMethod instanceof SyntheticRoleFieldAccess)
    				srcField = ((SyntheticRoleFieldAccess) srcMethod).resolvedField(); // ensure binding is initialized.
    			else
    				srcField = srcMethod.targetWriteField;
    			dstField = ConstantPoolObjectMapper.mapField(srcMethod, srcField, targetTeamDecl.binding);
    			dstMethod = tgtTypeDecl.binding.addSyntheticMethod(dstField, false /* write */, isSuperAccess, false);
    			if (!srcField.isStatic() && (srcMethod instanceof SyntheticRoleFieldAccess))
    				dstMethod.parameters[0] = srcMethod.parameters[0]; // manual weakening
    			break;
    		case SyntheticMethodBinding.SuperMethodAccess:
    			isSuperAccess = true;
    			//$FALL-THROUGH$
    		case SyntheticMethodBinding.MethodAccess:
    			MethodBinding dstOrigMethod = ConstantPoolObjectMapper.mapMethod(srcMethod, srcMethod.targetMethod, null, targetTeamDecl.binding);
    			if (dstOrigMethod.isCallin())
    				dstMethod = tgtTypeDecl.binding.addSyntheticBaseCallSurrogate(dstOrigMethod);
    			else 
    				dstMethod = tgtTypeDecl.binding.addSyntheticMethod(dstOrigMethod, isSuperAccess);
    			break;
    		case SyntheticMethodBinding.BridgeMethod:
    			dstMethod= ConstantPoolObjectMapper.mapMethod(srcMethod, srcMethod, null, targetTeamDecl.binding);
    			if (dstMethod == null)
    				tgtTypeDecl.scope.problemReporter().abortDueToInternalError("Expected synthetic bridge method does not exist: "+new String(srcMethod.readableName())); //$NON-NLS-1$
    			break;
    		case SyntheticMethodBinding.RoleMethodBridgeInner:
    		case SyntheticMethodBinding.RoleMethodBridgeOuter:
    			return null; // not copied but generated anew
    		default:
    			tgtTypeDecl.scope.problemReporter().abortDueToInternalError("Synthetic methods only partially supported"); //$NON-NLS-1$
    		}
    	}
    	return dstMethod;
    }

    /**
     * Default role constructors are not copied but created anew for each role
     * (see the caller of this method).
     * This method records the new ctor as overriding all its tsuper versions (if any).
     */
    public static void connectDefaultCtor(RoleModel clazz, MethodBinding binding) {
            ReferenceBinding[] tsupers = clazz.getTSuperRoleBindings();
            if (tsupers.length == 0) return;
            MethodBinding[] tsuperCtors = new MethodBinding[tsupers.length];
            int j=0;
            for (int i=0; i<tsupers.length; i++) {
                    MethodBinding tsuperCtor = tsupers[i].getExactConstructor(Binding.NO_PARAMETERS);
                    if (tsuperCtor != null)
                            tsuperCtors[j++] = tsuperCtor;
            }
            if (j>0) {
                    if (j == tsupers.length)
                            binding.overriddenTSupers = tsuperCtors;
                    else
                            System.arraycopy(tsuperCtors, 0, binding.overriddenTSupers=new MethodBinding[j], 0, j);
            }
    }

    /**
     * Nothing exciting here, just create a new field declaration.
     * @param field
     * @param roleDeclaration
     */
    private static void copyField(
            FieldBinding    field,
            TypeDeclaration roleDeclaration)
    {
	    // avoid copying twice (see copyGeneratedFeatures()):
	    roleDeclaration.getRoleModel().recordCopiedFeature(field);

        if ((field.modifiers & AccSynthetic) != 0) {
        	roleDeclaration.binding.addCopiedSyntheticFied(field);
        	return;
        }
        if ((field.tagBits & TagBits.IsFakedField) != 0)
        	return; // don't copy fakes.

        if (roleDeclaration.fields != null) {
			for (int i=0;i<roleDeclaration.fields.length;i++) {
				FieldDeclaration currentField = roleDeclaration.fields[i];
				if (CharOperation.equals(currentField.name, field.name))
				{
					if (   currentField.binding != null
						&& currentField.binding.copyInheritanceSrc != null
						&& currentField.binding.copyInheritanceSrc == field.copyInheritanceSrc)
						return; // not a problem: repeated inheritance of the same field!

					ProblemReporter problemReporter = currentField.isStatic() ?
							roleDeclaration.staticInitializerScope.problemReporter() :
							roleDeclaration.initializerScope.problemReporter();
					problemReporter.implicitlyHideField(currentField);
					return;
				}
			}
        }
        AstGenerator gen = new AstGenerator(roleDeclaration.sourceStart, roleDeclaration.sourceEnd);
        FieldDeclaration fieldDeclaration =
                AstConverter.createField(field, roleDeclaration, gen);
        AstEdit.addField(roleDeclaration,fieldDeclaration, true, false/*typeProblem*/);
        if (fieldDeclaration.binding != null) {
        	fieldDeclaration.binding.modifiers |= ExtraCompilerModifiers.AccLocallyUsed; 
        	fieldDeclaration.binding.modifiers |= (field.modifiers&ExtraCompilerModifiers.AccBlankFinal); // BlankFinal was omitted on the fieldDecl
        }

    }

    /**
	 * Generate creation methods for a given role constructor.
	 * for concrete R:
	 *   R _OT$createR (mySignature) { return new __OT__R(myArgs); }
	 * for abstract R:
	 *    abstract R _OT$createR (mySignature);
	 * If role is inherited from super-Team, repeat creation method with
	 * same signature but creating an instance of the local role type.
	 *
	 * @param teamDeclaration	the team class to hold the creation method
	 * @param roleModel 		the role to instantiate
     * @param constructor 		use this as a template to create the creation method, may be null.
     * @param constructorBinding non-null representation of constructor
     * @param needMethodBody used to signal whether generating statements is required
	 * @return the creation method.
	 */
	public static MethodDeclaration createCreationMethod (
	        TypeDeclaration        teamDeclaration,
	        RoleModel              roleModel,
	        ConstructorDeclaration constructor,
			MethodBinding 		   constructorBinding,
			boolean                needMethodBody)
	{

	    int start, end;
	    int modifiers;
	    boolean hasError = false;
	    if (constructor != null) {
		    if (constructor.isTSuper || constructor.hasErrors())
		        return null;
		    if (constructor.isDefaultConstructor() && roleModel.hasBaseclassProblem())
		    	hasError = true;

	    	start = constructor.sourceStart;
	    	end = constructor.sourceEnd;
	    	modifiers = constructor.modifiers;
	    	constructorBinding= constructor.binding;
	    } else {
		    if (TSuperHelper.isTSuper(constructorBinding))
		        return null;
	    	start = teamDeclaration.sourceStart;
	    	end = teamDeclaration.sourceEnd;
	    	modifiers = constructorBinding.modifiers;
	    }
	    int originalModifiers= -1;
		// creation method must access constructor, make it at least protected:
	    if (constructorBinding != null && (constructorBinding.isDefault() || constructorBinding.isPrivate())) {
	    	originalModifiers = constructorBinding.modifiers;
	    	MethodModel.getModel(constructorBinding).storeModifiers(originalModifiers);
	    }

	    AstGenerator gen = new AstGenerator(start, end);

	    Argument[] newArguments = null;
	    // Arguments (construct from bindings, using dummy names):
	    TypeBinding[] srcParams = constructorBinding != null ? constructorBinding.parameters : null;
	    if (srcParams != null) {
	        newArguments = AstConverter.createArgumentsFromParameters(srcParams, gen);
	        if (srcParams.length == 1 && srcParams[0] == roleModel.getInterfacePartBinding()) {     // single argument of type of this role itself?
	        	if (constructorBinding.isPrivate() || constructorBinding.isDefault())				// srcParams != null => constructorBinding != null
	        		roleModel.getAst().scope.problemReporter().roleConstructorHiddenByLiftingConstructor(constructor);
	        }
	    }
	    if (newArguments != null && constructorBinding != null && Lifting.isLiftingCtor(constructorBinding))
	    	newArguments[0].type.setBaseclassDecapsulation(DecapsulationState.REPORTED);

	    // if we have source arguments, improve: use correct argument names:
	    if (newArguments != null && constructor != null) {
		    Argument[] srcArguments = constructor.arguments;
		    if (srcArguments != null && srcArguments.length == newArguments.length)
		    	for (int i = 0; i < srcArguments.length; i++)
					newArguments[i].name = srcArguments[i].name;
	    }
	    TypeReference[] exceptions = null;
	    if (constructor != null) {
		    if (constructor.thrownExceptions != null)
		    	exceptions = AstClone.copyTypeArray(constructor.thrownExceptions);
	    } else {
	    	if (constructorBinding.thrownExceptions != Binding.NO_EXCEPTIONS)
	    		exceptions = AstClone.copyExceptions(constructorBinding, gen);
	    }

	    MethodDeclaration newMethod = internalCreateCreationMethod(
	    		teamDeclaration,
				roleModel,
				modifiers,
				newArguments,
				exceptions,
				needMethodBody && !hasError,
				start, end);
	    if (hasError)
	    	newMethod.tagAsHavingErrors();

	    if (newMethod != null) {
		    MethodModel model = MethodModel.getModel(newMethod);
		    model._srcCtor = constructorBinding;
		    if (originalModifiers != -1)
		    	model.storeModifiers(originalModifiers);
	    }

	    if (newMethod != null && constructor != null)
	    	// faked source locations (binaries have no source poss, but compiled constructor has no errors.)
	    	AstClone.copySrcLocation(constructor, newMethod);

	    return newMethod;
	}

	/**
     * Common implementation for creating a creation method from
     * either AST or BinaryTypeBinding.
     *
	 * @param teamDeclaration target of method generation
	 * @param roleModel       the role to be instantiated
	 * @param ctorModifiers   access modifiers of the constructor
	 * @param newArguments    pre-assembled arguments
	 * @param thrownExceptions exceptions declared by the constructor
	 * @param needMethodBody
	 * @param start			  pretended position of creation method
	 * @param end             pretended position of creation method
	 * @return the creation method declaration
	 */
	private static MethodDeclaration internalCreateCreationMethod(
			TypeDeclaration teamDeclaration,
			RoleModel       roleModel,
			int             ctorModifiers,
			Argument[]      newArguments,
			TypeReference[] thrownExceptions,
			boolean         needMethodBody,
			int start, int end)
	{

		if ((ctorModifiers & AccPrivate) != 0) // not accessible from outside
	        return null;

	    char[] typeName = roleModel.getBinding().internalName();
	    AstGenerator gen= new AstGenerator(start, end);

	    // Modifiers:
	    int typeModifiers = roleModel.getBinding().modifiers;
	    // Selector:
	    char[] roleName = roleModel.getName();

	    // If role type is generic:
	    TypeVariableBinding[] typeVars = roleModel.getBinding().typeVariables();

	    // Let's start:
	    MethodDeclaration newMethod = gen.method(
	    								teamDeclaration.compilationResult,
	    								Protections.combine(typeModifiers, ctorModifiers),
	    								createRoleTypeRef(roleName, typeVars, gen),
	    								CharOperation.concat(CREATOR_PREFIX_NAME, roleName),
	    								newArguments);
	    newMethod.thrownExceptions = thrownExceptions;

	    // If role is generic so must be the creation method: <T> R<T> _OT$create$R()
        if (typeVars != Binding.NO_TYPE_VARIABLES) {
        	newMethod.typeParameters = new TypeParameter[typeVars.length];
        	for (int i = 0; i < typeVars.length; i++)
        		newMethod.typeParameters[i] = gen.typeParameter(typeVars[i]);
        }

	    // adjust modifiers if type is abstract:
	    if ((typeModifiers & AccAbstract) != 0)
	    {
	        if ((teamDeclaration.modifiers & AccAbstract) != 0) {
	            // abstract R createR (mySignature);
	            newMethod.modifiers |= AccSemicolonBody;
	        } else {
	            // if team is not abstract, we ensure that the
	            // abstract creation method is never called directly.
	            MethodModel.getModel(newMethod).markAsForbiddenCreationMethod();
	            newMethod.modifiers &= ~(AccAbstract|AccSemicolonBody);
	            // Then MessageSend.resolve will detect invocation of this method.
	        }
	    }

	    // Body (if requested and non-abstract)
	    if (needMethodBody) {
		    Expression returnExpr;
		    if ((typeModifiers & AccAbstract) == 0)
		    {   // { return new __OT__R(myArgs); }
		    	// convert all arguments to expressions:
		    	Expression[] expressions= null;
		    	if (newArguments != null) {
		    		int argumentlength = newArguments.length;
		    		expressions = new Expression[argumentlength];
		    		for (int i = 0; i < argumentlength; i++)
		    			expressions[i]=  gen.singleNameReference(newArguments[i].name);
		    	}
				returnExpr = gen.allocation(createRoleTypeRef(typeName, typeVars, gen),
		        							expressions);
		        returnExpr.bits |= ASTNode.IsGenerated;
		    } else {
		        returnExpr = gen.nullLiteral();
		    }
		    newMethod.setStatements(new Statement[] {
		            gen.returnStatement(returnExpr)
		    });
	    }

	    newMethod.isGenerated = true;
	    AstEdit.addMethod(teamDeclaration, newMethod);

		return newMethod;
	}

	static TypeReference createRoleTypeRef(char[] typeName, TypeVariableBinding[] typeVars, AstGenerator gen) {
        if (typeVars != Binding.NO_TYPE_VARIABLES) {
        	TypeReference[] typeArgs = new TypeReference[typeVars.length];
        	for (int i = 0; i < typeArgs.length; i++)
				typeArgs[i] = gen.singleTypeReference(typeVars[i].sourceName);
        	return new ParameterizedSingleTypeReference(typeName, typeArgs, 0, gen.pos);
        } else {
        	return gen.singleTypeReference(typeName);
        }
	}

	/**
	 * Create the interface part of a creation method and add it to the team-role (ifc part).
	 *
	 * @param teamBinding
	 * @param creatorDecl
	 */
	public static void createCreatorIfcPart(
			SourceTypeBinding teamBinding,
			MethodDeclaration creatorDecl)
	{
		MethodDeclaration ifcCreator = null;
		if (teamBinding.isRole()) {
			ReferenceBinding ifcBinding = teamBinding.roleModel.getInterfacePartBinding();
			MethodBinding foundMethod = TypeAnalyzer.findMethod(creatorDecl.scope, ifcBinding, creatorDecl.selector, creatorDecl.binding.parameters);
			if (foundMethod == null || foundMethod.problemId() == ProblemReasons.NotFound) {
		    	TypeDeclaration enclTeamDecl = teamBinding.roleModel.getTeamModel().getAst();
		    	ifcCreator = AstConverter.genRoleIfcMethod(enclTeamDecl, creatorDecl);
		    	AstEdit.addMethod(teamBinding.roleModel.getInterfaceAst(), ifcCreator);
		    	MethodModel.getModel(ifcCreator)._srcCtor = creatorDecl.model._srcCtor;
			}
		}
	}

	/**
	 * After the super team has been resolved, we need to scan all generated castTo methods,
	 * and override each one with another one that internally uses the roles of the
	 * current team. Of course the return type has to be weakened in this case.
	 *
	 * @param teamDecl
	 */
	public static void copyCastToAndGetClassMethods(TypeDeclaration teamDecl) {
		if (teamDecl.superclass == null)
			return; // nothing to copy
	    // re-create castTo methods to adjust the type:
		MethodBinding[] superMethods = teamDecl.binding.superclass.availableMethods();
	    for (int i = 0; i < superMethods.length; i++) {
			MethodBinding superMethod = superMethods[i];
			if (CharOperation.prefixEquals(IOTConstants.CAST_PREFIX, superMethod.selector))
			{
				ReferenceBinding roleBinding = (ReferenceBinding)superMethod.returnType;
				RoleTypeBinding roleType = (RoleTypeBinding)RoleTypeCreator.maybeWrapUnqualifiedRoleType(
						teamDecl.scope,
						teamDecl.binding.getMemberType(roleBinding.sourceName()),
						null);
				if (roleType == null)
					continue;
				int dimensions = 0;
				int thirdDollar = CharOperation.indexOf('$', superMethod.selector, IOTConstants.CAST_PREFIX.length);
				if (thirdDollar > -1) {
					char[] dimString = CharOperation.subarray(superMethod.selector, thirdDollar+1, -1);
					dimensions = Integer.parseInt(new String(dimString));
				}
				MethodBinding castMethod = StandardElementGenerator.getCastMethod(
						teamDecl.getTeamModel(),
						roleType,
						teamDecl.scope,
						dimensions,
						false, // don't search super teams
						teamDecl.superclass.sourceStart,
						teamDecl.superclass.sourceEnd);
				ReferenceBinding refType = (ReferenceBinding) castMethod.returnType;
				castMethod.returnType = refType.weakenFrom((ReferenceBinding)superMethod.returnType);
			}
			else if (CharOperation.prefixEquals(GET_CLASS_PREFIX, superMethods[i].selector))
			{
				char[] roleName = CharOperation.subarray(
						superMethods[i].selector, GET_CLASS_PREFIX.length, -1);
				ReferenceBinding roleBinding = teamDecl.binding.getMemberType(roleName);
				if (!roleBinding.isBinaryBinding())
					RoleClassLiteralAccess.ensureGetClassMethod(
							teamDecl.getTeamModel(),
							roleBinding.roleModel);
			}
		}
	}
    /**
     * Create an invocation of a creation method, which will replace a given
     * allocation expression, i.e.,
     *      new R(args) ==> _OT$createR(args)
     *
     * PRE: allocationExpr.type has a simple name.
     *
     * @param scope
     * @param allocationExpr
     * @return the fully assembled expression.
     */
    public static MessageSend createConstructorMethodInvocationExpression(
            Scope scope, AllocationExpression allocationExpr)
    {
        // some elements from allocationExpression can be reused, since that
        // expression will be discarded (replaced by the result of this method).
        // These parts will only be used one more time by the current
        // TransformStatementsVisitor, so don't delete yet.


        if (!(allocationExpr.type instanceof SingleTypeReference)) {
        	scope.problemReporter().roleCreationNotRelativeToEnclosingTeam(allocationExpr);
        	allocationExpr.resolvedType = allocationExpr.type.resolvedType;
        	return null;
        }

        AstGenerator gen = new AstGenerator(allocationExpr.sourceStart, allocationExpr.sourceEnd);
        char[] typeName = allocationExpr.type.getTypeName()[0];
        char[] selector = CharOperation.concat(CREATOR_PREFIX_NAME, typeName);

        Expression receiver = null;
        if (allocationExpr instanceof QualifiedAllocationExpression) {
            // Sharing 'receiver' breaks tree structure, but MessageSend and
            // RoleTypeReference avoid double resolving.
        	receiver = ((QualifiedAllocationExpression)allocationExpr).enclosingInstance;
        }
        if (   receiver == null
        	&& allocationExpr.type instanceof ParameterizedSingleTypeReference)
        {
        	ParameterizedSingleTypeReference psType = (ParameterizedSingleTypeReference)allocationExpr.type;
        	if (psType.typeAnchors != null) {
        		assert psType.typeAnchors.length == 1 : "Currently only one value parameter supported."; //$NON-NLS-1$
        		receiver = psType.typeAnchors[0].anchor;
        	}
        }
        if (receiver != null) {
        	ReferenceBinding receiverType = (ReferenceBinding)receiver.resolvedType;
        	// TransformStatementsVisitor calls us before resolve, but that transformer
        	// has an identifier-based strategy to ensure that the role exists.
			if (receiverType != null) {
				receiverType = receiverType.getRealClass();
				Dependencies.ensureBindingState(receiverType, STATE_LENV_CONNECT_TYPE_HIERARCHY);
				if (receiverType.getMemberType(typeName) == null)
				{
					scope.problemReporter().noSuchRoleInTeam(allocationExpr.type, receiverType);
					return null;
				}
        	}
        } else {
        	receiver = ThisReference.implicitThis();
        }
        MessageSend message = new MessageSend() {
        	// specialized MessageSend to perform analysis for unsafe role instantiation:
        	@Override
        	public TypeBinding resolveType(BlockScope blockScope) {
        		TypeBinding result = super.resolveType(blockScope);
        		ReferenceBinding roleType = (ReferenceBinding)this.resolvedType;
        		if (roleType != null && roleType.isValidBinding()) {
	        		MethodBinding ctor = roleType.getExactConstructor(this.binding.parameters);
	        		if (Lifting.isLiftToConstructor(ctor, roleType)) // implies role is bound
	        		{
						if (this.arguments != null && this.arguments.length > 0) {
							Expression currentArg = this.arguments[0];
							while (currentArg instanceof CastExpression)
								currentArg = ((CastExpression)currentArg).expression; // unwrap
							if (   (currentArg instanceof AllocationExpression)
								|| (   (currentArg instanceof MessageSend)
									 && isCreator(((MessageSend)currentArg).binding)))
							{
								// need to check if arg is compatible without lowering
								TypeBinding allocType = currentArg.resolvedType;
								Config confBak = Config.createOrResetConfig(this);
								try {
									if (   allocType.isCompatibleWith(roleType.baseclass())
										&& !Config.getLoweringRequired()) // TODO(SH): could potentially ask the expression if conversions were involved?
										return result; // we're clean
								} finally {
									Config.removeOrRestore(confBak, this);
								}
							}
							blockScope.problemReporter().liftCtorArgNotAllocation(ctor, this, roleType.baseclass());
							this.binding.roleCreatorRequiringRuntimeCheck = true;
						}
	        		}
        		}
        		return result;
        	}
        };
        gen.setPos(message);
        message.nameSourcePosition = gen.pos;
        message.selector = selector;
        message.receiver = receiver;
        message.arguments = allocationExpr.arguments;
        message.constant = Constant.NotAConstant;

        // remove left-overs (if any):
        allocationExpr.resolvedType = null;

        return message;
    }

    /**
     * This method should ensure, that an interface does not provide the same
     * method as an implicitly inherited method but with a more specific signature
     * Since role interfaces are created by the RoleSplitter, at that time nothing
     * was known about method signatures of tsuper roles.
     * Also check whether all methods of roleType are compatible with implicitly
     * inherited versions from tsuperRole (visibility, exceptions etc.).
     * Perform these checks on the interface parts only, since class versions have
     * their visibilities twisted.
     * @param superRoleIfc   lookup methods here, to match ours against.
     * @param subTeam
     * @param subRoleIfcDecl this type declaration is edited.
     */
    public static void weakenInterfaceSignatures(
            ReferenceBinding superRoleIfc,
            ReferenceBinding subTeam,
            TypeDeclaration  subRoleIfcDecl)
    {
        ReferenceBinding superTeam = superRoleIfc.enclosingType();
        MethodBinding[] superMethods = superRoleIfc.methods();

    	checkPrivateMethods(superTeam, superMethods, subTeam, subRoleIfcDecl);

        AbstractMethodDeclaration[] oldMethods = subRoleIfcDecl.methods;
        if (oldMethods == null)
            return;

        MethodVerifier verifier = subRoleIfcDecl.scope.environment().methodVerifier();

        // collect methods that match an implicitly inherited method
        HashSet<AbstractMethodDeclaration> foundMethodDecls = new HashSet<AbstractMethodDeclaration>();
        HashSet<MethodBinding> foundMethodBinds = new HashSet<MethodBinding>();
        for (int i=0; i<superMethods.length; i++)
        {
            MethodBinding inheritedMethod = superMethods[i];
            AbstractMethodDeclaration methodFound = findMethod(
                    superTeam, superMethods[i],
                    subTeam,   subRoleIfcDecl);
            if (  (methodFound != null)
            	&&(methodFound.binding != null)) // do not touch broken methods
			{
                // check compatibility before removing:
                if (inheritedMethod.isValidBinding()) {
                    verifier.checkAgainstImplicitlyInherited(
                            subRoleIfcDecl.binding,
                            subTeam,   methodFound.binding,
                            superTeam, inheritedMethod);
                }
                if (   !methodFound.isGenerated  // don't remove generated methods
                	|| methodFound.hasErrors())  // unless erroneous
                {
                	foundMethodDecls.add(methodFound);
                	foundMethodBinds.add(methodFound.binding);
                }
            }
        }
        int declarationLen = oldMethods.length - foundMethodDecls.size();

        // remove matching declarations:
        subRoleIfcDecl.methods = new AbstractMethodDeclaration[declarationLen];
        reduceArray(oldMethods, subRoleIfcDecl.methods, foundMethodDecls);

    	//invalid method-bindings has been removed from methods-binding-array in faultInTypes
		int bindingLen = subRoleIfcDecl.binding.methods().length - foundMethodBinds.size();


        // remove matching bindings
        SourceTypeBinding subRoleBind     = subRoleIfcDecl.binding;
        subRoleBind.reduceMethods(foundMethodBinds, bindingLen);

        // recurse to interfaces (including tsuper):
        ReferenceBinding[] superInterfaces = superRoleIfc.superInterfaces();
        for (int i=0; i<superInterfaces.length; i++) {
            ReferenceBinding superIfc = superInterfaces[i];
            if (superIfc.isSynthInterface())
                weakenInterfaceSignatures(
                        superIfc, subTeam, subRoleIfcDecl);
        }
    }

    /** Specifically check for private methods overriding non-private ones,
     *  since private methods are not included in the analysis of weakenInterfaceSignatures
     *  (private methods are _not_ included in the interface part).
     * @param superTeam
     * @param superMethods
     * @param subTeam
     * @param subRoleIfcDecl
     */
    private static void checkPrivateMethods(
    		ReferenceBinding superTeam, MethodBinding[] superMethods,
    		ReferenceBinding subTeam, TypeDeclaration subRoleIfcDecl)
    {
		RoleModel roleModel = subRoleIfcDecl.getRoleModel();
		if (roleModel == null) return;
		TypeDeclaration classPart = roleModel._classPart;
		if (classPart == null) return;

		// implementation mainly as above:
		MethodVerifier verifier = subRoleIfcDecl.scope.environment().methodVerifier();

		for (MethodBinding inheritedMethod : superMethods) {
			AbstractMethodDeclaration methodFound = findMethod(
					superTeam, inheritedMethod, subTeam, classPart);
            if (  (methodFound != null)
            	&&((methodFound.modifiers & ClassFileConstants.AccPrivate) != 0) // only private ones.
            	&&(methodFound.binding != null)) // do not touch broken methods
			{
                // check compatibility (reducing visibility?):
                if (inheritedMethod.isValidBinding()) {
                    verifier.checkAgainstImplicitlyInherited(
                            classPart.binding,
                            subTeam,   methodFound.binding,
                            superTeam, inheritedMethod);
                }
			}
		}
	}

	/**
     * Traverse all supers (extends,implements) to check whether
     * any overriding method should weaken its signature to match
     * the inherited version.
     * Perform operation both on role class and interface.
     * Inclucde recursion on super class and super interfaces (as long a they are roles).
     * @param currentRole   role to investigate
     * @param roleClassDecl the class decl to edit (constant during recursion)
     * @param roleIfcDecl   the ifc decl to edit  (constant during recursion)
     */
	public static void weakenSignaturesFromSupers(ReferenceBinding currentRole, TypeDeclaration roleClassDecl, TypeDeclaration roleIfcDecl) {
		weakenSignaturesFromExtends(
		        roleClassDecl,
		        roleIfcDecl,
		        currentRole.superclass());
		ReferenceBinding[] superIfcs = currentRole.superInterfaces();
		for (int i=0; i<superIfcs.length; i++)
		    weakenSignaturesFromExtends(
		    	roleClassDecl,
		    	roleIfcDecl,
		    	superIfcs[i]);
	}

	/**
     * Given that the super role (extends or implements) is in place with all its features,
     * check whether any overriding method should weaken its signature to match
     * the inherited version.
     * Perform operation both on role class and interface.
     * Inclucde recursion on super class and super interfaces (as long a they are roles).
     * @param roleClassDecl
     * @param roleIfcDecl
     * @param superRole     the role to which our signatures must be adjusted
     */
    private static void weakenSignaturesFromExtends(
            TypeDeclaration  roleClassDecl,
            TypeDeclaration  roleIfcDecl,
            ReferenceBinding superRole)
    {
        if (superRole == null || !superRole.isDirectRole())
            return;
        // workaround for mixed binary/source roles (cause for this situation unknown):
        if (roleIfcDecl == null)
        	return;
        if (roleIfcDecl.binding.erasure() != superRole.erasure()) // no need to treat our ifc-part
        {
	        Dependencies.ensureBindingState(superRole, STATE_TYPES_ADJUSTED);
	        MethodBinding[] superMethods = superRole.methods();
	        if (superMethods != null)
	        {
	            ReferenceBinding thisTeam  = roleClassDecl.binding.enclosingType();
	            for (int i=0; i<superMethods.length; i++) {
	                AbstractMethodDeclaration methodDecl;
	                methodDecl = findMethod(thisTeam, superMethods[i], thisTeam, roleClassDecl);
	                if (methodDecl != null)
	                    weakenSignature(methodDecl, superMethods[i]);
	                methodDecl = findMethod(thisTeam, superMethods[i], thisTeam, roleIfcDecl);
	                if (methodDecl != null)
	                    weakenSignature(methodDecl, superMethods[i]);
	                    // Note (SH): removing does not work here, because other roles,
	                    // may extend this role and have no other chance than by this
	                    // weakened method to learn about their need to weaken, too.
	            }
	        }
        }
        // recurse to all superclass/interfaces
        weakenSignaturesFromSupers(superRole, roleClassDecl, roleIfcDecl);
    }


    /** Tries to find a role class within a Team
     * @param where the Team class to look into
     * @param name name of the role class to search for
     * @return interface or class found
     */
    private static TypeDeclaration findMemberType(TypeDeclaration where, char[] name){

        //Alle im Team definierten Rollen durchiterieren
        if (where.memberTypes != null) {
            for (int i = 0; i < where.memberTypes.length; i++) {
                if (where.memberTypes[i] != null) {
                    TypeDeclaration memberType = where.memberTypes[i];

                    if(CharOperation.equals(memberType.name, name))
                        return memberType;
                }
            }
        }
        return null;
    }

    /**
     * Search a method within a role or team declaration.
     * Does a comparison based on signatures, where the need of signature weakening
     * is taken into account.
     * @param givenTeam   enclosing team of givenMethod
	 * @param givenMethod find a method matching this one's signature
	 * @param targetTeam     enclosing team of targetRoleDecl
     * @param targetClassDecl where to look.
	 * @return the matching method, if any. Might have no binding (i.e. ignoreFurtherInvestigation == true)
	 */
	private static AbstractMethodDeclaration findMethod(
            ReferenceBinding givenTeam,
            MethodBinding    givenMethod,
            ReferenceBinding targetTeam,
            TypeDeclaration  targetClassDecl)
    {

		if (targetClassDecl.methods == null)
			return null;

		//Signature-bindings of methods are inserted on demand!
		targetClassDecl.binding.getMethods(givenMethod.selector);

		for (int i = 0; i < targetClassDecl.methods.length; i++) {
			AbstractMethodDeclaration targetMethDecl = targetClassDecl.methods[i];
			if (targetMethDecl != null && !targetMethDecl.isClinit()) {
				if (targetMethDecl.isConstructor() == givenMethod.isConstructor())
                {
                    if (!targetMethDecl.isConstructor()){
                        MethodDeclaration md = (MethodDeclaration) targetMethDecl;
                        // methods - other than constructors - have a name:
                        if (!CharOperation.equals(givenMethod.selector, md.selector))
                            continue;
                    }

                    if (targetMethDecl.binding == null)
                    	return targetMethDecl;

					if (TypeAnalyzer.isEqualMethodSignature(
                                givenTeam,  givenMethod,
                                targetTeam, targetMethDecl.binding,
                                TypeAnalyzer.ANY_MATCH))
                    {
						return targetMethDecl;
					}
				}
			}
		}
		return null;
	}


    /**
     * For a given role method adjust its signature according to the
     * tsuper-role version which is provided by its binding.
     * For each argument type:
     * <ul>
     * <li> if it is a role type, make it refer explicitly to the type in the super team
     * <li> for each parameter whose type is adjusted insert a new local with the desired
     *   type and cast the provided argument value to this local.
     * <li> replace arguments in super calls of a constructor by the renamed and
     *   and casted argument.
     * </ul>
     * E.g., the constructor
     * <pre> Role (Role2 r) {
     *     super(r);
     *     print(r);
     * }</pre>
     * becomes:
     * <pre> Role (SuperTeam.Role2 __OT__r) {
     *     super((Role2)__OT__r);
     *     Role2 r = (Role2)__OT__r;
     *     print(r);
     * </pre>
     * @param method method to adjust
     * @param template the tsuper version which should be taken as master for adjustment.
     * @return has weakening been performed?
     */
    public static boolean weakenSignature(
            AbstractMethodDeclaration method,
            MethodBinding             template)
    {
        MethodBinding binding = method.binding;
        // no weakening for constructors:
        if (method.isConstructor())
        	return false;
        // do not touch broken methods:
        if (binding == null || method.hasErrors())
        	return false;
        if (MethodModel.hasProblem(template)) {
			method.tagAsHavingErrors(); // propagate error
        	return false;
        }

        MethodScope   scope   = method.scope;
        boolean changed = false;


        // true weakening (with weakened type binding) avoids bridge methods
        if (method.binding.returnType != template.returnType && DependentTypeBinding.isDependentType(method.binding.returnType))
        	method.binding.returnType = WeakenedTypeBinding.makeWeakenedTypeBinding((DependentTypeBinding)method.binding.returnType.leafComponentType(),
        																	   	    (ReferenceBinding) template.returnType.leafComponentType(),
        																	   	    template.returnType.dimensions());

        // liftTo methods have no role arguments
        if (Lifting.isLiftToMethod(method.binding))
        	return changed;

        // Method parameters
        int paramLen = binding.parameters.length;
        assert (paramLen == template.parameters.length);
        if (paramLen == 0)
            return changed;

        if (method.isConstructor() && binding.declaringClass.isTeam())
        	return changed; // already processed by Lifting.prepareArgLifting() (includes a cast).

        // selectively share type bindings, but not the array:
        for(int i=0; i<paramLen; i++) {
        	TypeBinding param= binding.parameters[i];
        	TypeBinding tmplParam= template.parameters[i];
        	if (param.isRole()) {
        		binding.parameters[i]= template.parameters[i];
        		if (param.isParameterizedType() && tmplParam.isParameterizedType()) {
        			// keep old type arguments, just replace the type itself
        			TypeBinding[] args= ((ParameterizedTypeBinding)param).arguments;
        			ReferenceBinding tmplType= ((ParameterizedTypeBinding)tmplParam).actualType();
        			try {
						LookupEnvironment env = Config.getLookupEnvironment();
						binding.parameters[i]= new ParameterizedTypeBinding(tmplType, args, tmplType.enclosingType(), env);
					} catch (NotConfiguredException e) {
						e.logWarning("Cannot create parameterized type"); //$NON-NLS-1$
					}
        		}
        	}
        }

        // below: treat statements.
        if (method.isAbstract())
        	return changed;

        if (method.isCopied)
        	return changed; // no statements

        ArrayList<Statement> newLocalStats = new ArrayList<Statement>();
        for (int i=0; i<method.arguments.length; i++)
        {
            final Argument argument = method.arguments[i];
            char[]         oldName  = argument.name;
            if (RoleTypeBinding.isRoleWithExplicitAnchor(argument.type.resolvedType))
            	continue;
            TypeReference  newType  = TypeAnalyzer.weakenTypeReferenceFromBinding(
                        scope, argument.type, argument.binding.type, binding.parameters[i]);
            if (newType != argument.type)
            {
                changed = true;
                
                newType.setBaseclassDecapsulation(argument.type.getBaseclassDecapsulation());

                // local variable:
                newLocalStats.add(generateCastedLocal(argument, newType));

                // replace arguments in super-constructor call:
                if (method instanceof ConstructorDeclaration)
                {
                    ConstructorDeclaration ctor = (ConstructorDeclaration)method;
                    ReplaceSingleNameVisitor.IExpressionProvider provider =
                        new ReplaceSingleNameVisitor.IExpressionProvider()
                    {
                        public Expression newExpression() {
                            return new SingleNameReference(argument.name, argument.sourceStart);
                        }
                    };
                    if (ctor.constructorCall != null)
                    	ctor.constructorCall.traverse(
	                            new ReplaceSingleNameVisitor(oldName, provider),
	                            null); // no scope
                }
            }
        }
        if (    !binding.declaringClass.isDirectRole()
        	&& CharOperation.equals(binding.selector, IOTConstants.INIT_METHOD_NAME))
        	return changed; // no statements, not casted locals.
        if (!newLocalStats.isEmpty())
        {
        	if (StateHelper.hasState(binding.declaringClass, ITranslationStates.STATE_RESOLVED))
        		for (Statement localDeclaration : newLocalStats)
					localDeclaration.resolve(method.scope);

			// add the local variable declaration statements:
            MethodModel.prependStatements(method, newLocalStats);
        }
        return changed;
    }

    // utility for signature weakening.
    public static <T>void reduceArray(
            T[] origArray,
            T[] newArray,
            HashSet<T> filter)
    {
        int j=0;
        for (int i=0; i<origArray.length; i++)
        {
            if (!filter.contains(origArray[i]))
                newArray[j++] = origArray[i];
        }
    }

    /**
     * Generate a local declaration from an argument which is to be renamed and retyped.
     * @param argument the original declaration
     * @param newType
     * @return a new LocalDeclaration
     */
    private static LocalDeclaration generateCastedLocal(
            Argument         argument,
            TypeReference    newType)
    {
        char[]        oldArgName = argument.name;
        TypeReference oldType    = argument.type;

        // change argument from "T.R arg" to "SuperT.R __OT__arg"
        char[] newArgName;
        newArgName    = CharOperation.concat(OT_DELIM_NAME, oldArgName);
        argument.updateName(newArgName);
        argument.type = newType;

        AstGenerator gen = new AstGenerator(argument.sourceStart, argument.sourceEnd);
        // create the variable "T.R arg = (T.R)__OT__arg"
        LocalDeclaration localVar = gen.localVariable(
        		oldArgName,
				AstClone.copyTypeReference(oldType),
				gen.castExpression(
						gen.singleNameReference(newArgName),
						oldType,
						CastExpression.DO_WRAP));
        localVar.modifiers = argument.modifiers;
        return localVar;
    }


	/**
	 * @param teamDecl
	 */
	public static void weakenTeamMethodSignatures(TypeDeclaration teamDecl) {
		HashSet<AbstractMethodDeclaration> weakenedMethods = new HashSet<AbstractMethodDeclaration>();
		ReferenceBinding superTeam = teamDecl.binding.superclass();
		// Note(SH): no need to process tsuper teams, because their methods
		// are already processed as role methods of the appropriate level.
		while (superTeam != null) {
			MethodBinding[] superMethods = superTeam.methods();
			for (int i = 0; i < superMethods.length; i++) {
				AbstractMethodDeclaration tgtMethod = findMethod(
						superTeam,        superMethods[i],
						teamDecl.binding, teamDecl);
				if (tgtMethod != null && !weakenedMethods.contains(tgtMethod)) {
					weakenSignature(tgtMethod, superMethods[i]);
					weakenedMethods.add(tgtMethod); // don't weaken more than once
				}
			}
			superTeam = superTeam.superclass();
		}
	}

	/**
	 * Is the given method a generated creation method?
	 * @param method
	 * @return true or false ;-)
	 */
	public static boolean isCreator(MethodBinding method) {
	    return isCreator(method.selector);
	}

	public static boolean isCreator(AbstractMethodDeclaration method) {
	    return isCreator(method.selector);
	}

	public static boolean isCreator(char[] selector) {
		return CharOperation.prefixEquals(CREATOR_PREFIX_NAME, selector);
	}

}
