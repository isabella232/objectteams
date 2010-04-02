/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TagBits.java 23137 2009-12-04 20:03:06Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;

/**
 * OTDT changes: several more bits. See their individual documentation.
 */
public interface TagBits {

	// Tag bits in the tagBits int of every TypeBinding
	long IsArrayType = ASTNode.Bit1;
	long IsBaseType = ASTNode.Bit2;
	long IsNestedType = ASTNode.Bit3;
	long IsMemberType = ASTNode.Bit4;
	long ContainsNestedTypeReferences = ASTNode.Bit12; // method/parameterized type binding
	long MemberTypeMask = IsNestedType | IsMemberType | ContainsNestedTypeReferences;
	long IsLocalType = ASTNode.Bit5;
	long LocalTypeMask = IsNestedType | IsLocalType | ContainsNestedTypeReferences;
	long IsAnonymousType = ASTNode.Bit6;
	long AnonymousTypeMask = LocalTypeMask | IsAnonymousType | ContainsNestedTypeReferences;
	long IsBinaryBinding = ASTNode.Bit7;

	// set for all bindings either representing a missing type (type), or directly referencing a missing type (field/method/variable)
	long HasMissingType = ASTNode.Bit8;

	// for method
	long HasUncheckedTypeArgumentForBoundCheck = ASTNode.Bit9;
	
	// set when method has argument(s) that couldn't be resolved
	long HasUnresolvedArguments = ASTNode.Bit10;
	
	// for the type cycle hierarchy check used by ClassScope
	long BeginHierarchyCheck = ASTNode.Bit9;  // type
	long EndHierarchyCheck = ASTNode.Bit10; // type
	long HasParameterAnnotations = ASTNode.Bit11; // method/constructor


	// test bit to see if default abstract methods were computed
	long KnowsDefaultAbstractMethods = ASTNode.Bit11; // type

	long IsArgument = ASTNode.Bit11; // local
	long ClearPrivateModifier = ASTNode.Bit10; // constructor binding

	// test bits to see if parts of binary types are faulted
	long AreFieldsSorted = ASTNode.Bit13;
	long AreFieldsComplete = ASTNode.Bit14; // sorted and all resolved
	long AreMethodsSorted = ASTNode.Bit15;
	long AreMethodsComplete = ASTNode.Bit16; // sorted and all resolved

	// test bit to avoid asking a type for a member type (includes inherited member types)
	long HasNoMemberTypes = ASTNode.Bit17;

	// test bit to identify if the type's hierarchy is inconsistent
	long HierarchyHasProblems = ASTNode.Bit18;

	// test bit to identify if the type's type variables have been connected
	long TypeVariablesAreConnected = ASTNode.Bit19;

	// set for parameterized type with successful bound check
	long PassedBoundCheck = ASTNode.Bit23;

	// set for parameterized type NOT of the form X<?,?>
	long IsBoundParameterizedType = ASTNode.Bit24;

	// used by BinaryTypeBinding
	long HasUnresolvedTypeVariables = ASTNode.Bit25;
	long HasUnresolvedSuperclass = ASTNode.Bit26;
	long HasUnresolvedSuperinterfaces = ASTNode.Bit27;
	long HasUnresolvedEnclosingType = ASTNode.Bit28;
	long HasUnresolvedMemberTypes = ASTNode.Bit29;

	long HasTypeVariable = ASTNode.Bit30; // set either for type variables (direct) or parameterized types indirectly referencing type variables
	long HasDirectWildcard = ASTNode.Bit31; // set for parameterized types directly referencing wildcards

	// for the annotation cycle hierarchy check used by ClassScope
	long BeginAnnotationCheck = ASTNode.Bit32L;
	long EndAnnotationCheck = ASTNode.Bit33L;

	// standard annotations
	// 9-bits for targets
	long AnnotationResolved = ASTNode.Bit34L;
	long DeprecatedAnnotationResolved = ASTNode.Bit35L;
	long AnnotationTarget = ASTNode.Bit36L; // @Target({}) only sets this bit
	long AnnotationForType = ASTNode.Bit37L;
	long AnnotationForField = ASTNode.Bit38L;
	long AnnotationForMethod = ASTNode.Bit39L;
	long AnnotationForParameter = ASTNode.Bit40L;
	long AnnotationForConstructor = ASTNode.Bit41L;
	long AnnotationForLocalVariable = ASTNode.Bit42L;
	long AnnotationForAnnotationType = ASTNode.Bit43L;
	long AnnotationForPackage = ASTNode.Bit44L;
	long AnnotationTargetMASK = AnnotationTarget
				| AnnotationForType | AnnotationForField
				| AnnotationForMethod | AnnotationForParameter
				| AnnotationForConstructor | AnnotationForLocalVariable
				| AnnotationForAnnotationType | AnnotationForPackage;
	// 2-bits for retention (should check (tagBits & RetentionMask) == RuntimeRetention
	long AnnotationSourceRetention = ASTNode.Bit45L;
	long AnnotationClassRetention = ASTNode.Bit46L;
	long AnnotationRuntimeRetention = AnnotationSourceRetention | AnnotationClassRetention;
	long AnnotationRetentionMASK = AnnotationSourceRetention | AnnotationClassRetention | AnnotationRuntimeRetention;
	// marker annotations
	long AnnotationDeprecated = ASTNode.Bit47L;
	long AnnotationDocumented = ASTNode.Bit48L;
	long AnnotationInherited = ASTNode.Bit49L;
	long AnnotationOverride = ASTNode.Bit50L;
	long AnnotationSuppressWarnings = ASTNode.Bit51L;
	long AllStandardAnnotationsMask = AnnotationTargetMASK | AnnotationRetentionMASK | AnnotationDeprecated | AnnotationDocumented | AnnotationInherited |  AnnotationOverride | AnnotationSuppressWarnings;

	long DefaultValueResolved = ASTNode.Bit52L;

	// set when type contains non-private constructor(s)
	long HasNonPrivateConstructor = ASTNode.Bit53L;

//{ObjectTeams:
    // is it a wrapped role type?
    long IsWrappedRole = ASTNode.Bit58L;					// for role type bindings
    // have types been wrapped in the signature
    long HasWrappedSignature = ASTNode.Bit58L;				// for method bindings    
    // "new SomeTeam()" expression, unmatchable anchor
    long IsFreshTeamInstance = ASTNode.Bit58L; 				// for LocalVariableBinding
    
    // is it a dependent type?
    long IsDependentType = ASTNode.Bit59L;					// for type bindings

    // had the baseclass field/playedBy problems?
    long BaseclassHasProblems = ASTNode.Bit60L;				// for type bindings (role)
    
    // several issues making lifting impossible
    long HasLiftingProblem = ASTNode.Bit61L;    			// for type bindings (role class)
    // parameter/return incompatibility btw base-role?
    long HasMappingIncompatibility = ASTNode.Bit61L; 		// for method mapping bindings

    // can lifting fail due to role abstractness?
    long HasAbstractRelevantRole = ASTNode.Bit62L;			// for type bindings (team)
    // is a _OT$base field faked to compensate for weakening?
    long IsFakedField = ASTNode.Bit62L;						// for field binding

    // details of completeTypeBindings:
    long BeginCopyRoles = ASTNode.Bit63L;					// for type binding (team)

    // did a nested team require correction of team/role flags?
    long HasClassKindProblem = ASTNode.Bit64L;				// for type binding (team&role)
    // is a role field needing a cast to the role class:
    long IsRoleClassField = ASTNode.Bit64L;					// for field binding (role)
    
// SH}
}
