/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added the following constants
 *								   NonStaticAccessToStaticField
 *								   NonStaticAccessToStaticMethod
 *								   Task
 *								   ExpressionShouldBeAVariable
 *								   AssignmentHasNoEffect
 *     IBM Corporation - added the following constants
 *								   TooManySyntheticArgumentSlots
 *								   TooManyArrayDimensions
 *								   TooManyBytesForStringConstant
 *								   TooManyMethods
 *								   TooManyFields
 *								   NonBlankFinalLocalAssignment
 *								   ObjectCannotHaveSuperTypes
 *								   MissingSemiColon
 *								   InvalidParenthesizedExpression
 *								   EnclosingInstanceInConstructorCall
 *								   BytecodeExceeds64KLimitForConstructor
 *								   IncompatibleReturnTypeForNonInheritedInterfaceMethod
 *								   UnusedPrivateMethod
 *								   UnusedPrivateConstructor
 *								   UnusedPrivateType
 *								   UnusedPrivateField
 *								   IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod
 *								   InvalidExplicitConstructorCall
 *     IBM Corporation - added the following constants
 *								   PossibleAccidentalBooleanAssignment
 *								   SuperfluousSemicolon
 *								   IndirectAccessToStaticField
 *								   IndirectAccessToStaticMethod
 *								   IndirectAccessToStaticType
 *								   BooleanMethodThrowingException
 *								   UnnecessaryCast
 *								   UnnecessaryArgumentCast
 *								   UnnecessaryInstanceof
 *								   FinallyMustCompleteNormally
 *								   UnusedMethodDeclaredThrownException
 *								   UnusedConstructorDeclaredThrownException
 *								   InvalidCatchBlockSequence
 *								   UnqualifiedFieldAccess
 *     IBM Corporation - added the following constants
 *								   Javadoc
 *								   JavadocUnexpectedTag
 *								   JavadocMissingParamTag
 *								   JavadocMissingParamName
 *								   JavadocDuplicateParamName
 *								   JavadocInvalidParamName
 *								   JavadocMissingReturnTag
 *								   JavadocDuplicateReturnTag
 *								   JavadocMissingThrowsTag
 *								   JavadocMissingThrowsClassName
 *								   JavadocInvalidThrowsClass
 *								   JavadocDuplicateThrowsClassName
 *								   JavadocInvalidThrowsClassName
 *								   JavadocMissingSeeReference
 *								   JavadocInvalidSeeReference
 *								   JavadocInvalidSeeHref
 *								   JavadocInvalidSeeArgs
 *								   JavadocMissing
 *								   JavadocInvalidTag
 *								   JavadocMessagePrefix
 *								   EmptyControlFlowStatement
 *     IBM Corporation - added the following constants
 *								   IllegalUsageOfQualifiedTypeReference
 *								   InvalidDigit
 *     IBM Corporation - added the following constants
 *								   ParameterAssignment
 *								   FallthroughCase
 *     IBM Corporation - added the following constants
 *                                 UnusedLabel
 *                                 UnnecessaryNLSTag
 *                                 LocalVariableMayBeNull
 *                                 EnumConstantsCannotBeSurroundedByParenthesis
 *                                 JavadocMissingIdentifier
 *                                 JavadocNonStaticTypeFromStaticInvocation
 *                                 RawTypeReference
 *                                 NoAdditionalBoundAfterTypeVariable
 *                                 UnsafeGenericArrayForVarargs
 *                                 IllegalAccessFromTypeVariable
 *                                 AnnotationValueMustBeArrayInitializer
 *                                 InvalidEncoding
 *                                 CannotReadSource
 *                                 EnumStaticFieldInInInitializerContext
 *                                 ExternalProblemNotFixable
 *                                 ExternalProblemFixable
 *     IBM Corporation - added the following constants
 *                                 AnnotationValueMustBeAnEnumConstant
 *                                 OverridingMethodWithoutSuperInvocation
 *                                 MethodMustOverrideOrImplement
 *                                 TypeHidingTypeParameterFromType
 *                                 TypeHidingTypeParameterFromMethod
 *                                 TypeHidingType
 *     IBM Corporation - added the following constants
 *								   NullLocalVariableReference
 *								   PotentialNullLocalVariableReference
 *								   RedundantNullCheckOnNullLocalVariable
 * 								   NullLocalVariableComparisonYieldsFalse
 * 								   RedundantLocalVariableNullAssignment
 * 								   NullLocalVariableInstanceofYieldsFalse
 * 								   RedundantNullCheckOnNonNullLocalVariable
 * 								   NonNullLocalVariableComparisonYieldsFalse
 *     IBM Corporation - added the following constants
 *                                 InvalidUsageOfTypeParametersForAnnotationDeclaration
 *                                 InvalidUsageOfTypeParametersForEnumDeclaration
 *     IBM Corporation - added the following constants
 *								   RedundantSuperinterface
 *		Benjamin Muskalla - added the following constants
 *									MissingSynchronizedModifierInInheritedMethod
 *		Fraunhofer FIRST - extended API and implementation
 *		Technical University Berlin - extended API and implementation
 *		Stephan Herrmann  - added the following constants
 *									UnusedObjectAllocation
 *									PotentiallyUnclosedCloseable
 *									PotentiallyUnclosedCloseableAtExit
 *									UnclosedCloseable
 *									UnclosedCloseableAtExit
 *									ExplicitlyClosedAutoCloseable
 * 								    RequiredNonNullButProvidedNull
 * 									RequiredNonNullButProvidedPotentialNull
 * 									RequiredNonNullButProvidedUnknown
 * 									NullAnnotationNameMustBeQualified
 * 									IllegalReturnNullityRedefinition
 * 									IllegalRedefinitionToNonNullParameter
 * 									IllegalDefinitionToNonNullParameter
 * 									ParameterLackingNonNullAnnotation
 * 									ParameterLackingNullableAnnotation
 * 									PotentialNullMessageSendReference
 * 									RedundantNullCheckOnNonNullMessageSend
 * 									CannotImplementIncompatibleNullness
 * 									RedundantNullAnnotation
 *									RedundantNullDefaultAnnotation
 *									RedundantNullDefaultAnnotationPackage
 *									RedundantNullDefaultAnnotationType
 *									RedundantNullDefaultAnnotationMethod
 *									ContradictoryNullAnnotations
 *									IllegalAnnotationForBaseType
 *									RedundantNullCheckOnSpecdNonNullLocalVariable
 *									SpecdNonNullLocalVariableComparisonYieldsFalse
 *									RequiredNonNullButProvidedSpecdNullable
 *									MissingDefaultCase
 *									MissingEnumConstantCaseDespiteDefault
 *									UninitializedLocalVariableHintMissingDefault
 *									UninitializedBlankFinalFieldHintMissingDefault
 *									ShouldReturnValueHintMissingDefault
 *									IllegalModifierForInterfaceDefaultMethod
 *									InheritedDefaultMethodConflictsWithOtherInherited
 *									ConflictingNullAnnotations
 *									ConflictingInheritedNullAnnotations
 *									UnsafeElementTypeConversion
 *									ArrayReferencePotentialNullReference
 *									DereferencingNullableExpression
 *									NullityMismatchingTypeAnnotation
 *									NullityMismatchingTypeAnnotationSuperHint
 *									NullityUncheckedTypeAnnotationDetail
 *									NullityUncheckedTypeAnnotationDetailSuperHint
 *									NullableFieldReference
 *									UninitializedNonNullField
 *									UninitializedNonNullFieldHintMissingDefault
 *									NonNullMessageSendComparisonYieldsFalse
 *									RedundantNullCheckOnNonNullSpecdField
 *									NonNullSpecdFieldComparisonYieldsFalse
 *									NonNullExpressionComparisonYieldsFalse
 *									RedundantNullCheckOnNonNullExpression
 *									ReferenceExpressionParameterNullityMismatch
 *									ReferenceExpressionParameterNullityUnchecked
 *									ReferenceExpressionReturnNullRedef
 *									ReferenceExpressionReturnNullRedefUnchecked
 *									DuplicateInheritedDefaultMethods
 *									SuperAccessCannotBypassDirectSuper
 *									SuperCallCannotBypassOverride
 *									ConflictingNullAnnotations
 *									ConflictingInheritedNullAnnotations
 *									UnsafeElementTypeConversion
 *									PotentialNullUnboxing
 *									NullUnboxing
 *									NullExpressionReference
 *									PotentialNullExpressionReference
 *									RedundantNullCheckAgainstNonNullType
 *									NullAnnotationUnsupportedLocation
 *									NullAnnotationUnsupportedLocationAtType
 *									NullityMismatchTypeArgument
 *									ContradictoryNullAnnotationsOnBound
 *									UnsafeNullnessCast
 *									ContradictoryNullAnnotationsInferred
 *									NonNullDefaultDetailIsNotEvaluated
 *									NullNotCompatibleToFreeTypeVariable
 *									NullityMismatchAgainstFreeTypeVariable
 *									ImplicitObjectBoundNoNullDefault
 *									IllegalParameterNullityRedefinition
 *									ContradictoryNullAnnotationsInferredFunctionType
 *									IllegalReturnNullityRedefinitionFreeTypeVariable
 *									UnlikelyCollectionMethodArgumentType
 *									UnlikelyEqualsArgumentType
 *      Jesper S Moller  - added the following constants
 *									TargetTypeNotAFunctionalInterface
 *									OuterLocalMustBeEffectivelyFinal
 *									IllegalModifiersForPackage
 *									DuplicateAnnotationNotMarkedRepeatable
 *									DisallowedTargetForContainerAnnotation
 *									RepeatedAnnotationWithContainerAnnotation
 *									ContainingAnnotationMustHaveValue
 *									ContainingAnnotationHasNonDefaultMembers
 *									ContainingAnnotationHasWrongValueType
 *								 	ContainingAnnotationHasShorterRetention
 *									RepeatableAnnotationHasTargets
 *									RepeatableAnnotationTargetMismatch
 *									RepeatableAnnotationIsDocumented
 *									RepeatableAnnotationIsInherited
 *									RepeatableAnnotationWithRepeatingContainerAnnotation
 *									VarLocalMultipleDeclarators
 *									VarLocalCannotBeArray
 *									VarLocalReferencesItself
 *									VarLocalWithoutInitizalier
 *									VarLocalInitializedToNull
 *									VarLocalCannotBeArrayInitalizers
 *									VarLocalCannotBeLambda
 *									VarLocalCannotBeMethodReference
 *									VarIsReserved
 *									VarIsReservedInFuture
 *									VarIsNotAllowedHere
******************************************************************************/
package org.eclipse.jdt.core.compiler;

import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;

/**
 * OTDT changes: many new problem IDs.
 *
 * Description of a Java problem, as detected by the compiler or some of the underlying
 * technology reusing the compiler.
 * A problem provides access to:
 * <ul>
 * <li> its location (originating source file name, source position, line number) </li>
 * <li> its message description </li>
 * <li> predicates to check its severity (error, warning, or info) </li>
 * <li> its ID : a number identifying the very nature of this problem. All possible IDs are listed
 * as constants on this interface. </li>
 * </ul>
 *
 * Note: the compiler produces IProblems internally, which are turned into markers by the JavaBuilder
 * so as to persist problem descriptions. This explains why there is no API allowing to reach IProblem detected
 * when compiling. However, the Java problem markers carry equivalent information to IProblem, in particular
 * their ID (attribute "id") is set to one of the IDs defined on this interface.
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IProblem {

/**
 * Answer back the original arguments recorded into the problem.
 * @return the original arguments recorded into the problem
 */
String[] getArguments();

/**
 * Returns the problem id
 *
 * @return the problem id
 */
int getID();

/**
 * Answer a localized, human-readable message string which describes the problem.
 *
 * @return a localized, human-readable message string which describes the problem
 */
String getMessage();

/**
 * Answer the file name in which the problem was found.
 *
 * @return the file name in which the problem was found
 */
char[] getOriginatingFileName();

/**
 * Answer the end position of the problem (inclusive), or -1 if unknown.
 *
 * @return the end position of the problem (inclusive), or -1 if unknown
 */
int getSourceEnd();

/**
 * Answer the line number in source where the problem begins.
 *
 * @return the line number in source where the problem begins
 */
int getSourceLineNumber();

/**
 * Answer the start position of the problem (inclusive), or -1 if unknown.
 *
 * @return the start position of the problem (inclusive), or -1 if unknown
 */
int getSourceStart();

/**
 * Returns whether the severity of this problem is 'Error'.
 *
 * @return true if the severity of this problem is 'Error', false otherwise
 */
boolean isError();

/**
 * Returns whether the severity of this problem is 'Warning'.
 *
 * @return true if the severity of this problem is 'Warning', false otherwise
 */
boolean isWarning();

/**
 * Returns whether the severity of this problem is 'Info'.
 *
 * @return true if the severity of this problem is 'Info', false otherwise
 * @since 3.12
 */
boolean isInfo();

/**
 * Set the end position of the problem (inclusive), or -1 if unknown.
 * Used for shifting problem positions.
 *
 * @param sourceEnd the given end position
 */
void setSourceEnd(int sourceEnd);

/**
 * Set the line number in source where the problem begins.
 *
 * @param lineNumber the given line number
 */
void setSourceLineNumber(int lineNumber);

/**
 * Set the start position of the problem (inclusive), or -1 if unknown.
 * Used for shifting problem positions.
 *
 * @param sourceStart the given start position
 */
void setSourceStart(int sourceStart);


	/**
	 * Problem Categories
	 * The high bits of a problem ID contains information about the category of a problem.
	 * For example, (problemID & TypeRelated) != 0, indicates that this problem is type related.
	 *
	 * A problem category can help to implement custom problem filters. Indeed, when numerous problems
	 * are listed, focusing on import related problems first might be relevant.
	 *
	 * When a problem is tagged as Internal, it means that no change other than a local source code change
	 * can  fix the corresponding problem. A type related problem could be addressed by changing the type
	 * involved in it.
	 */
	int TypeRelated = 0x01000000;
	int FieldRelated = 0x02000000;
	int MethodRelated = 0x04000000;
	int ConstructorRelated = 0x08000000;
	int ImportRelated = 0x10000000;
	int Internal = 0x20000000;
	int Syntax = 0x40000000;
	/** @since 3.0 */
	int Javadoc = 0x80000000;
	/** @since 3.14 */
	int ModuleRelated = 0x00800000;
	/** @since 3.18 */
	int Compliance = 0x00400000;
	/** @since 3.20 */
	int PreviewRelated = 0x00200000;

	/**
	 * Mask to use in order to filter out the category portion of the problem ID.
	 */
	int IgnoreCategoriesMask = 0x1FFFFF;

	/*
	 * Below are listed all available problem IDs. Note that this list could be augmented in the future,
	 * as new features are added to the Java core implementation.
	 *
	 * Problem IDs must be kept unique even when their mask is stripped, since
	 * the bare integer literal is used for message lookup in
	 * /org.eclipse.jdt.core/compiler/org/eclipse/jdt/internal/compiler/problem/messages.properties.
	 * Use this regex to find duplicates: (?s)(\+ \d+)\b.*\1\b
	 */

	/**
	 * ID reserved for referencing an internal error inside the JavaCore implementation which
	 * may be surfaced as a problem associated with the compilation unit which caused it to occur.
	 */
	int Unclassified = 0;

	/**
	 * General type related problems
	 */
	int ObjectHasNoSuperclass = TypeRelated + 1;
	int UndefinedType = TypeRelated + 2;
	int NotVisibleType = TypeRelated + 3;
	int AmbiguousType = TypeRelated + 4;
	int UsingDeprecatedType = TypeRelated + 5;
	int InternalTypeNameProvided = TypeRelated + 6;
	/** @since 2.1 */
	int UnusedPrivateType = Internal + TypeRelated + 7;

	int IncompatibleTypesInEqualityOperator = TypeRelated + 15;
	int IncompatibleTypesInConditionalOperator = TypeRelated + 16;
	int TypeMismatch = TypeRelated + 17;
	/** @since 3.0 */
	int IndirectAccessToStaticType = Internal + TypeRelated + 18;

	/** @since 3.10 */
	int ReturnTypeMismatch = TypeRelated + 19;

	/**
	 * Inner types related problems
	 */
	int MissingEnclosingInstanceForConstructorCall = TypeRelated + 20;
	int MissingEnclosingInstance = TypeRelated + 21;
	int IncorrectEnclosingInstanceReference = TypeRelated + 22;
	int IllegalEnclosingInstanceSpecification = TypeRelated + 23;
	int CannotDefineStaticInitializerInLocalType = Internal + 24;
	int OuterLocalMustBeFinal = Internal + 25;
	int CannotDefineInterfaceInLocalType = Internal + 26;
	int IllegalPrimitiveOrArrayTypeForEnclosingInstance = TypeRelated + 27;
	/** @since 2.1 */
	int EnclosingInstanceInConstructorCall = Internal + 28;
	int AnonymousClassCannotExtendFinalClass = TypeRelated + 29;
	/** @since 3.1 */
	int CannotDefineAnnotationInLocalType = Internal + 30;
	/** @since 3.1 */
	int CannotDefineEnumInLocalType = Internal + 31;
	/** @since 3.1 */
	int NonStaticContextForEnumMemberType = Internal + 32;
	/** @since 3.3 */
	int TypeHidingType = TypeRelated + 33;
	/** @since 3.11 */
	int NotAnnotationType = TypeRelated + 34;

	// variables
	int UndefinedName = Internal + FieldRelated + 50;
	int UninitializedLocalVariable = Internal + 51;
	int VariableTypeCannotBeVoid = Internal + 52;
	/** @deprecated - problem is no longer generated, use {@link #CannotAllocateVoidArray} instead */
	int VariableTypeCannotBeVoidArray = Internal + 53;
	int CannotAllocateVoidArray = Internal + 54;
	// local variables
	int RedefinedLocal = Internal + 55;
	int RedefinedArgument = Internal + 56;
	// final local variables
	int DuplicateFinalLocalInitialization = Internal + 57;
	/** @since 2.1 */
	int NonBlankFinalLocalAssignment = Internal + 58;
	/** @since 3.2 */
	int ParameterAssignment = Internal + 59;
	int FinalOuterLocalAssignment = Internal + 60;
	int LocalVariableIsNeverUsed = Internal + 61;
	int ArgumentIsNeverUsed = Internal + 62;
	int BytecodeExceeds64KLimit = Internal + 63;
	int BytecodeExceeds64KLimitForClinit = Internal + 64;
	int TooManyArgumentSlots = Internal + 65;
	int TooManyLocalVariableSlots = Internal + 66;
	/** @since 2.1 */
	int TooManySyntheticArgumentSlots = Internal + 67;
	/** @since 2.1 */
	int TooManyArrayDimensions = Internal + 68;
	/** @since 2.1 */
	int BytecodeExceeds64KLimitForConstructor = Internal + 69;

	// fields
	int UndefinedField = FieldRelated + 70;
	int NotVisibleField = FieldRelated + 71;
	int AmbiguousField = FieldRelated + 72;
	int UsingDeprecatedField = FieldRelated + 73;
	int NonStaticFieldFromStaticInvocation = FieldRelated + 74;
	int ReferenceToForwardField = FieldRelated + Internal + 75;
	/** @since 2.1 */
	int NonStaticAccessToStaticField = Internal + FieldRelated + 76;
	/** @since 2.1 */
	int UnusedPrivateField = Internal + FieldRelated + 77;
	/** @since 3.0 */
	int IndirectAccessToStaticField = Internal + FieldRelated + 78;
	/** @since 3.0 */
	int UnqualifiedFieldAccess = Internal + FieldRelated + 79;
	int FinalFieldAssignment = FieldRelated + 80;
	int UninitializedBlankFinalField = FieldRelated + 81;
	int DuplicateBlankFinalFieldInitialization = FieldRelated + 82;
	/** @since 3.6 */
	int UnresolvedVariable = FieldRelated + 83;
	/** @since 3.10 */
	int NonStaticOrAlienTypeReceiver = MethodRelated + 84;

	/** @since 3.11 */
	int ExceptionParameterIsNeverUsed = Internal + 85;
	/** @since 3.17 */
	int BytecodeExceeds64KLimitForSwitchTable = Internal + 86;

	// variable hiding
	/** @since 3.0 */
	int LocalVariableHidingLocalVariable = Internal + 90;
	/** @since 3.0 */
	int LocalVariableHidingField = Internal + FieldRelated + 91;
	/** @since 3.0 */
	int FieldHidingLocalVariable = Internal + FieldRelated + 92;
	/** @since 3.0 */
	int FieldHidingField = Internal + FieldRelated + 93;
	/** @since 3.0 */
	int ArgumentHidingLocalVariable = Internal + 94;
	/** @since 3.0 */
	int ArgumentHidingField = Internal + 95;
	/** @since 3.1 */
	int MissingSerialVersion = Internal + 96;
	/** @since 3.10 */
	int LambdaRedeclaresArgument = Internal + 97;
	/** @since 3.10 */
	int LambdaRedeclaresLocal = Internal + 98;
	/** @since 3.10 */
	int LambdaDescriptorMentionsUnmentionable = 99;

	// methods
	int UndefinedMethod = MethodRelated + 100;
	int NotVisibleMethod = MethodRelated + 101;
	int AmbiguousMethod = MethodRelated + 102;
	int UsingDeprecatedMethod = MethodRelated + 103;
	int DirectInvocationOfAbstractMethod = MethodRelated + 104;
	int VoidMethodReturnsValue = MethodRelated + 105;
	int MethodReturnsVoid = MethodRelated + 106;
	int MethodRequiresBody = Internal + MethodRelated + 107;
	int ShouldReturnValue = Internal + MethodRelated + 108;
	int MethodButWithConstructorName = MethodRelated + 110;
	int MissingReturnType = TypeRelated + 111;
	int BodyForNativeMethod = Internal + MethodRelated + 112;
	int BodyForAbstractMethod = Internal + MethodRelated + 113;
	int NoMessageSendOnBaseType = MethodRelated + 114;
	int ParameterMismatch = MethodRelated + 115;
	int NoMessageSendOnArrayType = MethodRelated + 116;
	/** @since 2.1 */
    int NonStaticAccessToStaticMethod = Internal + MethodRelated + 117;
	/** @since 2.1 */
	int UnusedPrivateMethod = Internal + MethodRelated + 118;
	/** @since 3.0 */
	int IndirectAccessToStaticMethod = Internal + MethodRelated + 119;
	/** @since 3.4 */
	int MissingTypeInMethod = MethodRelated + 120;
	/** @since 3.7 */
	int MethodCanBeStatic = Internal + MethodRelated + 121;
	/** @since 3.7 */
	int MethodCanBePotentiallyStatic = Internal + MethodRelated + 122;
	/** @since 3.10 */
	int MethodReferenceSwingsBothWays = Internal + MethodRelated + 123;
	/** @since 3.10 */
	int StaticMethodShouldBeAccessedStatically = Internal + MethodRelated + 124;
	/** @since 3.10 */
	int InvalidArrayConstructorReference = Internal + MethodRelated + 125;
	/** @since 3.10 */
	int ConstructedArrayIncompatible = Internal + MethodRelated + 126;
	/** @since 3.10 */
	int DanglingReference = Internal + MethodRelated + 127;
	/** @since 3.10 */
	int IncompatibleMethodReference = Internal + MethodRelated + 128;

	// constructors
	/** @since 3.4 */
	int MissingTypeInConstructor = ConstructorRelated + 129;
	int UndefinedConstructor = ConstructorRelated + 130;
	int NotVisibleConstructor = ConstructorRelated + 131;
	int AmbiguousConstructor = ConstructorRelated + 132;
	int UsingDeprecatedConstructor = ConstructorRelated + 133;
	/** @since 2.1 */
	int UnusedPrivateConstructor = Internal + MethodRelated + 134;
	// explicit constructor calls
	int InstanceFieldDuringConstructorInvocation = ConstructorRelated + 135;
	int InstanceMethodDuringConstructorInvocation = ConstructorRelated + 136;
	int RecursiveConstructorInvocation = ConstructorRelated + 137;
	int ThisSuperDuringConstructorInvocation = ConstructorRelated + 138;
	/** @since 3.0 */
	int InvalidExplicitConstructorCall = ConstructorRelated + Syntax + 139;
	// implicit constructor calls
	int UndefinedConstructorInDefaultConstructor = ConstructorRelated + 140;
	int NotVisibleConstructorInDefaultConstructor = ConstructorRelated + 141;
	int AmbiguousConstructorInDefaultConstructor = ConstructorRelated + 142;
	int UndefinedConstructorInImplicitConstructorCall = ConstructorRelated + 143;
	int NotVisibleConstructorInImplicitConstructorCall = ConstructorRelated + 144;
	int AmbiguousConstructorInImplicitConstructorCall = ConstructorRelated + 145;
	int UnhandledExceptionInDefaultConstructor = TypeRelated + 146;
	int UnhandledExceptionInImplicitConstructorCall = TypeRelated + 147;

	// expressions
	/** @since 3.6 */
	int UnusedObjectAllocation = Internal + 148;
	/** @since 3.5 */
	int DeadCode = Internal + 149;
	int ArrayReferenceRequired = Internal + 150;
	int NoImplicitStringConversionForCharArrayExpression = Internal + 151;
	// constant expressions
	int StringConstantIsExceedingUtf8Limit = Internal + 152;
	int NonConstantExpression = Internal + 153;
	int NumericValueOutOfRange = Internal + 154;
	// cast expressions
	int IllegalCast = TypeRelated + 156;
	// allocations
	int InvalidClassInstantiation = TypeRelated + 157;
	int CannotDefineDimensionExpressionsWithInit = Internal + 158;
	int MustDefineEitherDimensionExpressionsOrInitializer = Internal + 159;
	// operators
	int InvalidOperator = Internal + 160;
	// statements
	int CodeCannotBeReached = Internal + 161;
	int CannotReturnInInitializer = Internal + 162;
	int InitializerMustCompleteNormally = Internal + 163;
	// assert
	int InvalidVoidExpression = Internal + 164;
	// try
	int MaskedCatch = TypeRelated + 165;
	int DuplicateDefaultCase = Internal + 166;
	int UnreachableCatch = TypeRelated + MethodRelated + 167;
	int UnhandledException = TypeRelated + 168;
	// switch
	int IncorrectSwitchType = TypeRelated + 169;
	int DuplicateCase = FieldRelated + 170;

	// labelled
	int DuplicateLabel = Internal + 171;
	int InvalidBreak = Internal + 172;
	int InvalidContinue = Internal + 173;
	int UndefinedLabel = Internal + 174;
	//synchronized
	int InvalidTypeToSynchronized = Internal + 175;
	int InvalidNullToSynchronized = Internal + 176;
	// throw
	int CannotThrowNull = Internal + 177;
	// assignment
	/** @since 2.1 */
	int AssignmentHasNoEffect = Internal + 178;
	/** @since 3.0 */
	int PossibleAccidentalBooleanAssignment = Internal + 179;
	/** @since 3.0 */
	int SuperfluousSemicolon = Internal + 180;
	/** @since 3.0 */
	int UnnecessaryCast = Internal + TypeRelated + 181;
	/** @deprecated - no longer generated, use {@link #UnnecessaryCast} instead
	 *   @since 3.0 */
	int UnnecessaryArgumentCast = Internal + TypeRelated + 182;
	/** @since 3.0 */
	int UnnecessaryInstanceof = Internal + TypeRelated + 183;
	/** @since 3.0 */
	int FinallyMustCompleteNormally = Internal + 184;
	/** @since 3.0 */
	int UnusedMethodDeclaredThrownException = Internal + 185;
	/** @since 3.0 */
	int UnusedConstructorDeclaredThrownException = Internal + 186;
	/** @since 3.0 */
	int InvalidCatchBlockSequence = Internal + TypeRelated + 187;
	/** @since 3.0 */
	int EmptyControlFlowStatement = Internal + TypeRelated + 188;
	/** @since 3.0 */
	int UnnecessaryElse = Internal + 189;

	// inner emulation
	int NeedToEmulateFieldReadAccess = FieldRelated + 190;
	int NeedToEmulateFieldWriteAccess = FieldRelated + 191;
	int NeedToEmulateMethodAccess = MethodRelated + 192;
	int NeedToEmulateConstructorAccess = MethodRelated + 193;

	/** @since 3.2 */
	int FallthroughCase = Internal + 194;

	//inherited name hides enclosing name (sort of ambiguous)
	int InheritedMethodHidesEnclosingName = MethodRelated + 195;
	int InheritedFieldHidesEnclosingName = FieldRelated + 196;
	int InheritedTypeHidesEnclosingName = TypeRelated + 197;

	/** @since 3.1 */
	int IllegalUsageOfQualifiedTypeReference = Internal + Syntax + 198;

	// miscellaneous
	/** @since 3.2 */
	int UnusedLabel = Internal + 199;
	int ThisInStaticContext = Internal + 200;
	int StaticMethodRequested = Internal + MethodRelated + 201;
	int IllegalDimension = Internal + 202;
	/** @deprecated - problem is no longer generated */
	int InvalidTypeExpression = Internal + 203;
	int ParsingError = Syntax + Internal + 204;
	int ParsingErrorNoSuggestion = Syntax + Internal + 205;
	int InvalidUnaryExpression = Syntax + Internal + 206;

	// syntax errors
	int InterfaceCannotHaveConstructors = Syntax + Internal + 207;
	int ArrayConstantsOnlyInArrayInitializers = Syntax + Internal + 208;
	int ParsingErrorOnKeyword = Syntax + Internal + 209;
	int ParsingErrorOnKeywordNoSuggestion = Syntax + Internal + 210;

	/** @since 3.5 */
	int ComparingIdentical = Internal + 211;

	/** @since 3.22
	 * @noreference preview feature error */
	int UnsafeCast = TypeRelated + 212;

	int UnmatchedBracket = Syntax + Internal + 220;
	int NoFieldOnBaseType = FieldRelated + 221;
	int InvalidExpressionAsStatement = Syntax + Internal + 222;
	/** @since 2.1 */
	int ExpressionShouldBeAVariable = Syntax + Internal + 223;
	/** @since 2.1 */
	int MissingSemiColon = Syntax + Internal + 224;
	/** @since 2.1 */
	int InvalidParenthesizedExpression = Syntax + Internal + 225;

	/** @since 3.10 */
	int NoSuperInInterfaceContext = Syntax + Internal + 226;

	/** @since 3.0 */
	int ParsingErrorInsertTokenBefore = Syntax + Internal + 230;
	/** @since 3.0 */
	int ParsingErrorInsertTokenAfter = Syntax + Internal + 231;
	/** @since 3.0 */
    int ParsingErrorDeleteToken = Syntax + Internal + 232;
    /** @since 3.0 */
    int ParsingErrorDeleteTokens = Syntax + Internal + 233;
    /** @since 3.0 */
    int ParsingErrorMergeTokens = Syntax + Internal + 234;
    /** @since 3.0 */
    int ParsingErrorInvalidToken = Syntax + Internal + 235;
    /** @since 3.0 */
    int ParsingErrorMisplacedConstruct = Syntax + Internal + 236;
    /** @since 3.0 */
    int ParsingErrorReplaceTokens = Syntax + Internal + 237;
    /** @since 3.0 */
    int ParsingErrorNoSuggestionForTokens = Syntax + Internal + 238;
    /** @since 3.0 */
    int ParsingErrorUnexpectedEOF = Syntax + Internal + 239;
    /** @since 3.0 */
    int ParsingErrorInsertToComplete = Syntax + Internal + 240;
    /** @since 3.0 */
    int ParsingErrorInsertToCompleteScope = Syntax + Internal + 241;
    /** @since 3.0 */
    int ParsingErrorInsertToCompletePhrase = Syntax + Internal + 242;

	// scanner errors
	int EndOfSource = Syntax + Internal + 250;
	int InvalidHexa = Syntax + Internal + 251;
	int InvalidOctal = Syntax + Internal + 252;
	int InvalidCharacterConstant = Syntax + Internal + 253;
	int InvalidEscape = Syntax + Internal + 254;
	int InvalidInput = Syntax + Internal + 255;
	int InvalidUnicodeEscape = Syntax + Internal + 256;
	int InvalidFloat = Syntax + Internal + 257;
	int NullSourceString = Syntax + Internal + 258;
	int UnterminatedString = Syntax + Internal + 259;
	int UnterminatedComment = Syntax + Internal + 260;
	int NonExternalizedStringLiteral = Internal + 261;
	/** @since 3.1 */
	int InvalidDigit = Syntax + Internal + 262;
	/** @since 3.1 */
	int InvalidLowSurrogate = Syntax + Internal + 263;
	/** @since 3.1 */
	int InvalidHighSurrogate = Syntax + Internal + 264;
	/** @since 3.2 */
	int UnnecessaryNLSTag = Internal + 265;
	/** @since 3.7.1 */
	int InvalidBinary = Syntax + Internal + 266;
	/** @since 3.7.1 */
	int BinaryLiteralNotBelow17 = Syntax + Internal + 267;
	/** @since 3.7.1 */
	int IllegalUnderscorePosition = Syntax + Internal + 268;
	/** @since 3.7.1 */
	int UnderscoresInLiteralsNotBelow17 = Syntax + Internal + 269;
	/** @since 3.7.1 */
	int IllegalHexaLiteral = Syntax + Internal + 270;

	/** @since 3.10 */
	int MissingTypeInLambda = MethodRelated + 271;
	/** @since 3.23  */
	int UnterminatedTextBlock = PreviewRelated + 272;
	// type related problems
	/** @since 3.1 */
	int DiscouragedReference = TypeRelated + 280;

	int InterfaceCannotHaveInitializers = TypeRelated + 300;
	int DuplicateModifierForType = TypeRelated + 301;
	int IllegalModifierForClass = TypeRelated + 302;
	int IllegalModifierForInterface = TypeRelated + 303;
	int IllegalModifierForMemberClass = TypeRelated + 304;
	int IllegalModifierForMemberInterface = TypeRelated + 305;
	int IllegalModifierForLocalClass = TypeRelated + 306;
	/** @since 3.1 */
	int ForbiddenReference = TypeRelated + 307;
	int IllegalModifierCombinationFinalAbstractForClass = TypeRelated + 308;
	int IllegalVisibilityModifierForInterfaceMemberType = TypeRelated + 309;
	int IllegalVisibilityModifierCombinationForMemberType = TypeRelated + 310;
	int IllegalStaticModifierForMemberType = TypeRelated + 311;
	int SuperclassMustBeAClass = TypeRelated + 312;
	int ClassExtendFinalClass = TypeRelated + 313;
	int DuplicateSuperInterface = TypeRelated + 314;
	int SuperInterfaceMustBeAnInterface = TypeRelated + 315;
	int HierarchyCircularitySelfReference = TypeRelated + 316;
	int HierarchyCircularity = TypeRelated + 317;
	int HidingEnclosingType = TypeRelated + 318;
	int DuplicateNestedType = TypeRelated + 319;
	int CannotThrowType = TypeRelated + 320;
	int PackageCollidesWithType = TypeRelated + 321;
	int TypeCollidesWithPackage = TypeRelated + 322;
	int DuplicateTypes = TypeRelated + 323;
	int IsClassPathCorrect = TypeRelated + 324;
	int PublicClassMustMatchFileName = TypeRelated + 325;
	/** @deprecated - problem is no longer generated */
	int MustSpecifyPackage = Internal + 326;
	int HierarchyHasProblems = TypeRelated + 327;
	int PackageIsNotExpectedPackage = Internal + 328;
	/** @since 2.1 */
	int ObjectCannotHaveSuperTypes = Internal + 329;
	/** @since 3.1 */
	int ObjectMustBeClass = Internal + 330;
	/** @since 3.4 */
	int RedundantSuperinterface = TypeRelated + 331;
	/** @since 3.5 */
	int ShouldImplementHashcode = TypeRelated + 332;
	/** @since 3.5 */
	int AbstractMethodsInConcreteClass = TypeRelated + 333;

	/** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
	int SuperclassNotFound =  TypeRelated + 329 + ProblemReasons.NotFound; // TypeRelated + 330
	/** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
	int SuperclassNotVisible =  TypeRelated + 329 + ProblemReasons.NotVisible; // TypeRelated + 331
	/** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
	int SuperclassAmbiguous =  TypeRelated + 329 + ProblemReasons.Ambiguous; // TypeRelated + 332
	/** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
	int SuperclassInternalNameProvided =  TypeRelated + 329 + ProblemReasons.InternalNameProvided; // TypeRelated + 333
	/** @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName} instead */
	int SuperclassInheritedNameHidesEnclosingName =  TypeRelated + 329 + ProblemReasons.InheritedNameHidesEnclosingName; // TypeRelated + 334

	/** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
	int InterfaceNotFound =  TypeRelated + 334 + ProblemReasons.NotFound; // TypeRelated + 335
	/** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
	int InterfaceNotVisible =  TypeRelated + 334 + ProblemReasons.NotVisible; // TypeRelated + 336
	/** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
	int InterfaceAmbiguous =  TypeRelated + 334 + ProblemReasons.Ambiguous; // TypeRelated + 337
	/** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
	int InterfaceInternalNameProvided =  TypeRelated + 334 + ProblemReasons.InternalNameProvided; // TypeRelated + 338
	/** @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName} instead */
	int InterfaceInheritedNameHidesEnclosingName =  TypeRelated + 334 + ProblemReasons.InheritedNameHidesEnclosingName; // TypeRelated + 339

	// field related problems
	int DuplicateField = FieldRelated + 340;
	int DuplicateModifierForField = FieldRelated + 341;
	int IllegalModifierForField = FieldRelated + 342;
	int IllegalModifierForInterfaceField = FieldRelated + 343;
	int IllegalVisibilityModifierCombinationForField = FieldRelated + 344;
	int IllegalModifierCombinationFinalVolatileForField = FieldRelated + 345;
	int UnexpectedStaticModifierForField = FieldRelated + 346;

	/** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
	int FieldTypeNotFound =  FieldRelated + 349 + ProblemReasons.NotFound; // FieldRelated + 350
	/** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
	int FieldTypeNotVisible =  FieldRelated + 349 + ProblemReasons.NotVisible; // FieldRelated + 351
	/** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
	int FieldTypeAmbiguous =  FieldRelated + 349 + ProblemReasons.Ambiguous; // FieldRelated + 352
	/** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
	int FieldTypeInternalNameProvided =  FieldRelated + 349 + ProblemReasons.InternalNameProvided; // FieldRelated + 353
	/** @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName} instead */
	int FieldTypeInheritedNameHidesEnclosingName =  FieldRelated + 349 + ProblemReasons.InheritedNameHidesEnclosingName; // FieldRelated + 354

	// method related problems
	int DuplicateMethod = MethodRelated + 355;
	int IllegalModifierForArgument = MethodRelated + 356;
	int DuplicateModifierForMethod = MethodRelated + 357;
	int IllegalModifierForMethod = MethodRelated + 358;
	int IllegalModifierForInterfaceMethod = MethodRelated + 359;
	int IllegalVisibilityModifierCombinationForMethod = MethodRelated + 360;
	int UnexpectedStaticModifierForMethod = MethodRelated + 361;
	int IllegalAbstractModifierCombinationForMethod = MethodRelated + 362;
	int AbstractMethodInAbstractClass = MethodRelated + 363;
	int ArgumentTypeCannotBeVoid = MethodRelated + 364;
	/** @deprecated - problem is no longer generated, use {@link #CannotAllocateVoidArray} instead */
	int ArgumentTypeCannotBeVoidArray = MethodRelated + 365;
	/** @deprecated - problem is no longer generated, use {@link #CannotAllocateVoidArray} instead */
	int ReturnTypeCannotBeVoidArray = MethodRelated + 366;
	int NativeMethodsCannotBeStrictfp = MethodRelated + 367;
	int DuplicateModifierForArgument = MethodRelated + 368;
	/** @since 3.5 */
	int IllegalModifierForConstructor = MethodRelated + 369;

	/** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
	int ArgumentTypeNotFound =  MethodRelated + 369 + ProblemReasons.NotFound; // MethodRelated + 370
	/** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
	int ArgumentTypeNotVisible =  MethodRelated + 369 + ProblemReasons.NotVisible; // MethodRelated + 371
	/** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
	int ArgumentTypeAmbiguous =  MethodRelated + 369 + ProblemReasons.Ambiguous; // MethodRelated + 372
	/** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
	int ArgumentTypeInternalNameProvided =  MethodRelated + 369 + ProblemReasons.InternalNameProvided; // MethodRelated + 373
	/** @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName} instead */
	int ArgumentTypeInheritedNameHidesEnclosingName =  MethodRelated + 369 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 374

	/** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
	int ExceptionTypeNotFound =  MethodRelated + 374 + ProblemReasons.NotFound; // MethodRelated + 375
	/** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
	int ExceptionTypeNotVisible =  MethodRelated + 374 + ProblemReasons.NotVisible; // MethodRelated + 376
	/** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
	int ExceptionTypeAmbiguous =  MethodRelated + 374 + ProblemReasons.Ambiguous; // MethodRelated + 377
	/** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
	int ExceptionTypeInternalNameProvided =  MethodRelated + 374 + ProblemReasons.InternalNameProvided; // MethodRelated + 378
	/** @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName} instead */
	int ExceptionTypeInheritedNameHidesEnclosingName =  MethodRelated + 374 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 379

	/** @deprecated - problem is no longer generated, use {@link #UndefinedType} instead */
	int ReturnTypeNotFound =  MethodRelated + 379 + ProblemReasons.NotFound; // MethodRelated + 380
	/** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
	int ReturnTypeNotVisible =  MethodRelated + 379 + ProblemReasons.NotVisible; // MethodRelated + 381
	/** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
	int ReturnTypeAmbiguous =  MethodRelated + 379 + ProblemReasons.Ambiguous; // MethodRelated + 382
	/** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
	int ReturnTypeInternalNameProvided =  MethodRelated + 379 + ProblemReasons.InternalNameProvided; // MethodRelated + 383
	/** @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName} instead */
	int ReturnTypeInheritedNameHidesEnclosingName =  MethodRelated + 379 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 384

	// import related problems
	int ConflictingImport = ImportRelated + 385;
	int DuplicateImport = ImportRelated + 386;
	int CannotImportPackage = ImportRelated + 387;
	int UnusedImport = ImportRelated + 388;

	int ImportNotFound =  ImportRelated + 389 + ProblemReasons.NotFound; // ImportRelated + 390
	/** @deprecated - problem is no longer generated, use {@link #NotVisibleType} instead */
	int ImportNotVisible =  ImportRelated + 389 + ProblemReasons.NotVisible; // ImportRelated + 391
	/** @deprecated - problem is no longer generated, use {@link #AmbiguousType} instead */
	int ImportAmbiguous =  ImportRelated + 389 + ProblemReasons.Ambiguous; // ImportRelated + 392
	/** @deprecated - problem is no longer generated, use {@link #InternalTypeNameProvided} instead */
	int ImportInternalNameProvided =  ImportRelated + 389 + ProblemReasons.InternalNameProvided; // ImportRelated + 393
	/** @deprecated - problem is no longer generated, use {@link #InheritedTypeHidesEnclosingName} instead */
	int ImportInheritedNameHidesEnclosingName =  ImportRelated + 389 + ProblemReasons.InheritedNameHidesEnclosingName; // ImportRelated + 394

	/** @since 3.1 */
	int InvalidTypeForStaticImport =  ImportRelated + 391;

	// local variable related problems
	int DuplicateModifierForVariable = MethodRelated + 395;
	int IllegalModifierForVariable = MethodRelated + 396;
	/** @deprecated - problem is no longer generated, use {@link #RedundantNullCheckOnNonNullLocalVariable} instead */
	int LocalVariableCannotBeNull = Internal + 397; // since 3.3: semantics are LocalVariableRedundantCheckOnNonNull
	/** @deprecated - problem is no longer generated, use {@link #NullLocalVariableReference}, {@link #RedundantNullCheckOnNullLocalVariable} or {@link #RedundantLocalVariableNullAssignment} instead */
	int LocalVariableCanOnlyBeNull = Internal + 398; // since 3.3: split with LocalVariableRedundantCheckOnNull depending on context
	/** @deprecated - problem is no longer generated, use {@link #PotentialNullLocalVariableReference} instead */
	int LocalVariableMayBeNull = Internal + 399;

	// method verifier problems
	int AbstractMethodMustBeImplemented = MethodRelated + 400;
	int FinalMethodCannotBeOverridden = MethodRelated + 401;
	int IncompatibleExceptionInThrowsClause = MethodRelated + 402;
	int IncompatibleExceptionInInheritedMethodThrowsClause = MethodRelated + 403;
	int IncompatibleReturnType = MethodRelated + 404;
	int InheritedMethodReducesVisibility = MethodRelated + 405;
	int CannotOverrideAStaticMethodWithAnInstanceMethod = MethodRelated + 406;
	int CannotHideAnInstanceMethodWithAStaticMethod = MethodRelated + 407;
	int StaticInheritedMethodConflicts = MethodRelated + 408;
	int MethodReducesVisibility = MethodRelated + 409;
	int OverridingNonVisibleMethod = MethodRelated + 410;
	int AbstractMethodCannotBeOverridden = MethodRelated + 411;
	int OverridingDeprecatedMethod = MethodRelated + 412;
	/** @since 2.1 */
	int IncompatibleReturnTypeForNonInheritedInterfaceMethod = MethodRelated + 413;
	/** @since 2.1 */
	int IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod = MethodRelated + 414;
	/** @since 3.1 */
	int IllegalVararg = MethodRelated + 415;
	/** @since 3.3 */
	int OverridingMethodWithoutSuperInvocation = MethodRelated + 416;
	/** @since 3.5 */
	int MissingSynchronizedModifierInInheritedMethod= MethodRelated + 417;
	/** @since 3.5 */
	int AbstractMethodMustBeImplementedOverConcreteMethod = MethodRelated + 418;
	/** @since 3.5 */
	int InheritedIncompatibleReturnType = MethodRelated + 419;

	// code snippet support
	int CodeSnippetMissingClass = Internal + 420;
	int CodeSnippetMissingMethod = Internal + 421;
	int CannotUseSuperInCodeSnippet = Internal + 422;

	//constant pool
	int TooManyConstantsInConstantPool = Internal + 430;
	/** @since 2.1 */
	int TooManyBytesForStringConstant = Internal + 431;

	// static constraints
	/** @since 2.1 */
	int TooManyFields = Internal + 432;
	/** @since 2.1 */
	int TooManyMethods = Internal + 433;
	/** @since 3.7 */
	int TooManyParametersForSyntheticMethod = Internal + 434;

	// 1.4 features
	// assertion warning
	int UseAssertAsAnIdentifier = Internal + 440;

	// 1.5 features
	int UseEnumAsAnIdentifier = Internal + 441;
	/** @since 3.2 */
	int EnumConstantsCannotBeSurroundedByParenthesis = Syntax + Internal + 442;

	/** @since 3.10 */
	int IllegalUseOfUnderscoreAsAnIdentifier = Syntax + Internal + 443;
	 /** @since 3.10 */
	int UninternedIdentityComparison = Syntax + Internal + 444;
	 /** @since 3.24 */
	int ErrorUseOfUnderscoreAsAnIdentifier = Syntax + Internal + 445;

	// detected task
	/** @since 2.1 */
	int Task = Internal + 450;

	// local variables related problems, cont'd
	/** @since 3.3 */
	int NullLocalVariableReference = Internal + 451;
	/** @since 3.3 */
	int PotentialNullLocalVariableReference = Internal + 452;
	/** @since 3.3 */
	int RedundantNullCheckOnNullLocalVariable = Internal + 453;
	/** @since 3.3 */
	int NullLocalVariableComparisonYieldsFalse = Internal + 454;
	/** @since 3.3 */
	int RedundantLocalVariableNullAssignment = Internal + 455;
	/** @since 3.3 */
	int NullLocalVariableInstanceofYieldsFalse = Internal + 456;
	/** @since 3.3 */
	int RedundantNullCheckOnNonNullLocalVariable = Internal + 457;
	/** @since 3.3 */
	int NonNullLocalVariableComparisonYieldsFalse = Internal + 458;
	/** @since 3.9 */
	int PotentialNullUnboxing = Internal + 459;
	/** @since 3.9 */
	int NullUnboxing = Internal + 461;

	// block
	/** @since 3.0 */
	int UndocumentedEmptyBlock = Internal + 460;

	/*
	 * Javadoc comments
	 */
	/**
	 * Problem signaled on an invalid URL reference.
	 * Valid syntax example: @see "http://www.eclipse.org/"
	 * @since 3.4
	 */
	int JavadocInvalidSeeUrlReference = Javadoc + Internal + 462;
	/**
	 * Problem warned on missing tag description.
	 * @since 3.4
	 */
	int JavadocMissingTagDescription = Javadoc + Internal + 463;
	/**
	 * Problem warned on duplicated tag.
	 * @since 3.3
	 */
	int JavadocDuplicateTag = Javadoc + Internal + 464;
	/**
	 * Problem signaled on an hidden reference due to a too low visibility level.
	 * @since 3.3
	 */
	int JavadocHiddenReference = Javadoc + Internal + 465;
	/**
	 * Problem signaled on an invalid qualification for member type reference.
	 * @since 3.3
	 */
	int JavadocInvalidMemberTypeQualification = Javadoc + Internal + 466;
	/** @since 3.2 */
	int JavadocMissingIdentifier = Javadoc + Internal + 467;
	/** @since 3.2 */
	int JavadocNonStaticTypeFromStaticInvocation = Javadoc + Internal + 468;
	/** @since 3.1 */
	int JavadocInvalidParamTagTypeParameter = Javadoc + Internal + 469;
	/** @since 3.0 */
	int JavadocUnexpectedTag = Javadoc + Internal + 470;
	/** @since 3.0 */
	int JavadocMissingParamTag = Javadoc + Internal + 471;
	/** @since 3.0 */
	int JavadocMissingParamName = Javadoc + Internal + 472;
	/** @since 3.0 */
	int JavadocDuplicateParamName = Javadoc + Internal + 473;
	/** @since 3.0 */
	int JavadocInvalidParamName = Javadoc + Internal + 474;
	/** @since 3.0 */
	int JavadocMissingReturnTag = Javadoc + Internal + 475;
	/** @since 3.0 */
	int JavadocDuplicateReturnTag = Javadoc + Internal + 476;
	/** @since 3.0 */
	int JavadocMissingThrowsTag = Javadoc + Internal + 477;
	/** @since 3.0 */
	int JavadocMissingThrowsClassName = Javadoc + Internal + 478;
	/** @since 3.0 */
	int JavadocInvalidThrowsClass = Javadoc + Internal + 479;
	/** @since 3.0 */
	int JavadocDuplicateThrowsClassName = Javadoc + Internal + 480;
	/** @since 3.0 */
	int JavadocInvalidThrowsClassName = Javadoc + Internal + 481;
	/** @since 3.0 */
	int JavadocMissingSeeReference = Javadoc + Internal + 482;
	/** @since 3.0 */
	int JavadocInvalidSeeReference = Javadoc + Internal + 483;
	/**
	 * Problem signaled on an invalid URL reference that does not conform to the href syntax.
	 * Valid syntax example: @see <a href="http://www.eclipse.org/">Eclipse Home Page</a>
	 * @since 3.0
	 */
	int JavadocInvalidSeeHref = Javadoc + Internal + 484;
	/** @since 3.0 */
	int JavadocInvalidSeeArgs = Javadoc + Internal + 485;
	/** @since 3.0 */
	int JavadocMissing = Javadoc + Internal + 486;
	/** @since 3.0 */
	int JavadocInvalidTag = Javadoc + Internal + 487;
	/*
	 * ID for field errors in Javadoc
	 */
	/** @since 3.0 */
	int JavadocUndefinedField = Javadoc + Internal + 488;
	/** @since 3.0 */
	int JavadocNotVisibleField = Javadoc + Internal + 489;
	/** @since 3.0 */
	int JavadocAmbiguousField = Javadoc + Internal + 490;
	/** @since 3.0 */
	int JavadocUsingDeprecatedField = Javadoc + Internal + 491;
	/*
	 * IDs for constructor errors in Javadoc
	 */
	/** @since 3.0 */
	int JavadocUndefinedConstructor = Javadoc + Internal + 492;
	/** @since 3.0 */
	int JavadocNotVisibleConstructor = Javadoc + Internal + 493;
	/** @since 3.0 */
	int JavadocAmbiguousConstructor = Javadoc + Internal + 494;
	/** @since 3.0 */
	int JavadocUsingDeprecatedConstructor = Javadoc + Internal + 495;
	/*
	 * IDs for method errors in Javadoc
	 */
	/** @since 3.0 */
	int JavadocUndefinedMethod = Javadoc + Internal + 496;
	/** @since 3.0 */
	int JavadocNotVisibleMethod = Javadoc + Internal + 497;
	/** @since 3.0 */
	int JavadocAmbiguousMethod = Javadoc + Internal + 498;
	/** @since 3.0 */
	int JavadocUsingDeprecatedMethod = Javadoc + Internal + 499;
	/** @since 3.0 */
	int JavadocNoMessageSendOnBaseType = Javadoc + Internal + 500;
	/** @since 3.0 */
	int JavadocParameterMismatch = Javadoc + Internal + 501;
	/** @since 3.0 */
	int JavadocNoMessageSendOnArrayType = Javadoc + Internal + 502;
	/*
	 * IDs for type errors in Javadoc
	 */
	/** @since 3.0 */
	int JavadocUndefinedType = Javadoc + Internal + 503;
	/** @since 3.0 */
	int JavadocNotVisibleType = Javadoc + Internal + 504;
	/** @since 3.0 */
	int JavadocAmbiguousType = Javadoc + Internal + 505;
	/** @since 3.0 */
	int JavadocUsingDeprecatedType = Javadoc + Internal + 506;
	/** @since 3.0 */
	int JavadocInternalTypeNameProvided = Javadoc + Internal + 507;
	/** @since 3.0 */
	int JavadocInheritedMethodHidesEnclosingName = Javadoc + Internal + 508;
	/** @since 3.0 */
	int JavadocInheritedFieldHidesEnclosingName = Javadoc + Internal + 509;
	/** @since 3.0 */
	int JavadocInheritedNameHidesEnclosingTypeName = Javadoc + Internal + 510;
	/** @since 3.0 */
	int JavadocAmbiguousMethodReference = Javadoc + Internal + 511;
	/** @since 3.0 */
	int JavadocUnterminatedInlineTag = Javadoc + Internal + 512;
	/** @since 3.0 */
	int JavadocMalformedSeeReference = Javadoc + Internal + 513;
	/** @since 3.0 */
	int JavadocMessagePrefix = Internal + 514;

	/** @since 3.1 */
	int JavadocMissingHashCharacter = Javadoc + Internal + 515;
	/** @since 3.1 */
	int JavadocEmptyReturnTag = Javadoc + Internal + 516;
	/** @since 3.1 */
	int JavadocInvalidValueReference = Javadoc + Internal + 517;
	/** @since 3.1 */
	int JavadocUnexpectedText = Javadoc + Internal + 518;
	/** @since 3.1 */
	int JavadocInvalidParamTagName = Javadoc + Internal + 519;

	// see also JavadocNotAccessibleType below

	/*
	 * IDs for module errors in Javadoc
	 */
	/** @since 3.20 */
	int JavadocMissingUsesTag = Javadoc + Internal + 1800;
	/** @since 3.20 */
	int JavadocDuplicateUsesTag = Javadoc + Internal + 1801;
	/** @since 3.20 */
	int JavadocMissingUsesClassName = Javadoc + Internal + 1802;
	/** @since 3.20 */
	int JavadocInvalidUsesClassName = Javadoc + Internal + 1803;
	/** @since 3.20 */
	int JavadocInvalidUsesClass = Javadoc + Internal + 1804;
	/** @since 3.20 */
	int JavadocMissingProvidesTag = Javadoc + Internal + 1805;
	/** @since 3.20 */
	int JavadocDuplicateProvidesTag = Javadoc + Internal + 1806;
	/** @since 3.20 */
	int JavadocMissingProvidesClassName = Javadoc + Internal + 1807;
	/** @since 3.20 */
	int JavadocInvalidProvidesClassName = Javadoc + Internal + 1808;
	/** @since 3.20 */
	int JavadocInvalidProvidesClass = Javadoc + Internal + 1809;
	/** @since 3.24*/
	int JavadocInvalidModuleQualification = Javadoc + Internal + 1810;

	/**
	 * Generics
	 */
	/** @since 3.1 */
	int DuplicateTypeVariable = Internal + 520;
	/** @since 3.1 */
	int IllegalTypeVariableSuperReference = Internal + 521;
	/** @since 3.1 */
	int NonStaticTypeFromStaticInvocation = Internal + 522;
	/** @since 3.1 */
	int ObjectCannotBeGeneric = Internal + 523;
	/** @since 3.1 */
	int NonGenericType = TypeRelated + 524;
	/** @since 3.1 */
	int IncorrectArityForParameterizedType = TypeRelated + 525;
	/** @since 3.1 */
	int TypeArgumentMismatch = TypeRelated + 526;
	/** @since 3.1 */
	int DuplicateMethodErasure = TypeRelated + 527;
	/** @since 3.1 */
	int ReferenceToForwardTypeVariable = TypeRelated + 528;
    /** @since 3.1 */
	int BoundMustBeAnInterface = TypeRelated + 529;
    /** @since 3.1 */
	int UnsafeRawConstructorInvocation = TypeRelated + 530;
    /** @since 3.1 */
	int UnsafeRawMethodInvocation = TypeRelated + 531;
    /** @since 3.1 */
	int UnsafeTypeConversion = TypeRelated + 532;
    /** @since 3.1 */
	int InvalidTypeVariableExceptionType = TypeRelated + 533;
	/** @since 3.1 */
	int InvalidParameterizedExceptionType = TypeRelated + 534;
	/** @since 3.1 */
	int IllegalGenericArray = TypeRelated + 535;
	/** @since 3.1 */
	int UnsafeRawFieldAssignment = TypeRelated + 536;
	/** @since 3.1 */
	int FinalBoundForTypeVariable = TypeRelated + 537;
	/** @since 3.1 */
	int UndefinedTypeVariable = Internal + 538;
	/** @since 3.1 */
	int SuperInterfacesCollide = TypeRelated + 539;
	/** @since 3.1 */
	int WildcardConstructorInvocation = TypeRelated + 540;
	/** @since 3.1 */
	int WildcardMethodInvocation = TypeRelated + 541;
	/** @since 3.1 */
	int WildcardFieldAssignment = TypeRelated + 542;
	/** @since 3.1 */
	int GenericMethodTypeArgumentMismatch = TypeRelated + 543;
	/** @since 3.1 */
	int GenericConstructorTypeArgumentMismatch = TypeRelated + 544;
	/** @since 3.1 */
	int UnsafeGenericCast = TypeRelated + 545;
	/** @since 3.1 */
	int IllegalInstanceofParameterizedType = Internal + 546;
	/** @since 3.1 */
	int IllegalInstanceofTypeParameter = Internal + 547;
	/** @since 3.1 */
	int NonGenericMethod = TypeRelated + 548;
	/** @since 3.1 */
	int IncorrectArityForParameterizedMethod = TypeRelated + 549;
	/** @since 3.1 */
	int ParameterizedMethodArgumentTypeMismatch = TypeRelated + 550;
	/** @since 3.1 */
	int NonGenericConstructor = TypeRelated + 551;
	/** @since 3.1 */
	int IncorrectArityForParameterizedConstructor = TypeRelated + 552;
	/** @since 3.1 */
	int ParameterizedConstructorArgumentTypeMismatch = TypeRelated + 553;
	/** @since 3.1 */
	int TypeArgumentsForRawGenericMethod = TypeRelated + 554;
	/** @since 3.1 */
	int TypeArgumentsForRawGenericConstructor = TypeRelated + 555;
	/** @since 3.1 */
	int SuperTypeUsingWildcard = TypeRelated + 556;
	/** @since 3.1 */
	int GenericTypeCannotExtendThrowable = TypeRelated + 557;
	/** @since 3.1 */
	int IllegalClassLiteralForTypeVariable = TypeRelated + 558;
	/** @since 3.1 */
	int UnsafeReturnTypeOverride = MethodRelated + 559;
	/** @since 3.1 */
	int MethodNameClash = MethodRelated + 560;
	/** @since 3.1 */
	int RawMemberTypeCannotBeParameterized = TypeRelated + 561;
	/** @since 3.1 */
	int MissingArgumentsForParameterizedMemberType = TypeRelated + 562;
	/** @since 3.1 */
	int StaticMemberOfParameterizedType = TypeRelated + 563;
    /** @since 3.1 */
	int BoundHasConflictingArguments = TypeRelated + 564;
    /** @since 3.1 */
	int DuplicateParameterizedMethods = MethodRelated + 565;
	/** @since 3.1 */
	int IllegalQualifiedParameterizedTypeAllocation = TypeRelated + 566;
	/** @since 3.1 */
	int DuplicateBounds = TypeRelated + 567;
	/** @since 3.1 */
	int BoundCannotBeArray = TypeRelated + 568;
    /** @since 3.1 */
	int UnsafeRawGenericConstructorInvocation = TypeRelated + 569;
    /** @since 3.1 */
	int UnsafeRawGenericMethodInvocation = TypeRelated + 570;
	/** @since 3.1 */
	int TypeParameterHidingType = TypeRelated + 571;
	/** @since 3.2 */
	int RawTypeReference = TypeRelated + 572;
	/** @since 3.2 */
	int NoAdditionalBoundAfterTypeVariable = TypeRelated + 573;
	/** @since 3.2 */
	int UnsafeGenericArrayForVarargs = MethodRelated + 574;
	/** @since 3.2 */
	int IllegalAccessFromTypeVariable = TypeRelated + 575;
	/** @since 3.3 */
	int TypeHidingTypeParameterFromType = TypeRelated + 576;
	/** @since 3.3 */
	int TypeHidingTypeParameterFromMethod = TypeRelated + 577;
    /** @since 3.3 */
    int InvalidUsageOfWildcard = Syntax + Internal + 578;
    /** @since 3.4 */
    int UnusedTypeArgumentsForMethodInvocation = MethodRelated + 579;

	/**
	 * Foreach
	 */
	/** @since 3.1 */
	int IncompatibleTypesInForeach = TypeRelated + 580;
	/** @since 3.1 */
	int InvalidTypeForCollection = Internal + 581;
	/** @since 3.6*/
	int InvalidTypeForCollectionTarget14 = Internal + 582;

	/** @since 3.7.1 */
	int DuplicateInheritedMethods = MethodRelated + 583;
	/** @since 3.8 */
	int MethodNameClashHidden = MethodRelated + 584;

	/** @since 3.9 */
	int UnsafeElementTypeConversion = TypeRelated + 585;
	/** @since 3.11 */
    int InvalidTypeArguments = MethodRelated + TypeRelated + 586;

	/**
	 * 1.5 Syntax errors (when source level < 1.5)
	 */
	/** @since 3.1 */
    int InvalidUsageOfTypeParameters = Syntax + Internal + 590;
    /** @since 3.1 */
    int InvalidUsageOfStaticImports = Syntax + Internal + 591;
    /** @since 3.1 */
    int InvalidUsageOfForeachStatements = Syntax + Internal + 592;
    /** @since 3.1 */
    int InvalidUsageOfTypeArguments = Syntax + Internal + 593;
    /** @since 3.1 */
    int InvalidUsageOfEnumDeclarations = Syntax + Internal + 594;
    /** @since 3.1 */
    int InvalidUsageOfVarargs = Syntax + Internal + 595;
    /** @since 3.1 */
    int InvalidUsageOfAnnotations = Syntax + Internal + 596;
    /** @since 3.1 */
    int InvalidUsageOfAnnotationDeclarations = Syntax + Internal + 597;
    /** @since 3.4 */
    int InvalidUsageOfTypeParametersForAnnotationDeclaration = Syntax + Internal + 598;
    /** @since 3.4 */
    int InvalidUsageOfTypeParametersForEnumDeclaration = Syntax + Internal + 599;
    /**
     * Annotation
     */
	/** @since 3.1 */
	int IllegalModifierForAnnotationMethod = MethodRelated + 600;
    /** @since 3.1 */
    int IllegalExtendedDimensions = MethodRelated + 601;
    /** @since 3.1 */
	int InvalidFileNameForPackageAnnotations = Syntax + Internal + 602;
    /** @since 3.1 */
	int IllegalModifierForAnnotationType = TypeRelated + 603;
    /** @since 3.1 */
	int IllegalModifierForAnnotationMemberType = TypeRelated + 604;
    /** @since 3.1 */
	int InvalidAnnotationMemberType = TypeRelated + 605;
    /** @since 3.1 */
	int AnnotationCircularitySelfReference = TypeRelated + 606;
    /** @since 3.1 */
	int AnnotationCircularity = TypeRelated + 607;
	/** @since 3.1 */
	int DuplicateAnnotation = TypeRelated + 608;
	/** @since 3.1 */
	int MissingValueForAnnotationMember = TypeRelated + 609;
	/** @since 3.1 */
	int DuplicateAnnotationMember = Internal + 610;
	/** @since 3.1 */
	int UndefinedAnnotationMember = MethodRelated + 611;
	/** @since 3.1 */
	int AnnotationValueMustBeClassLiteral = Internal + 612;
	/** @since 3.1 */
	int AnnotationValueMustBeConstant = Internal + 613;
	/** @deprecated - problem is no longer generated (code is legite)
	 *   @since 3.1 */
	int AnnotationFieldNeedConstantInitialization = Internal + 614;
	/** @since 3.1 */
	int IllegalModifierForAnnotationField = Internal + 615;
	/** @since 3.1 */
	int AnnotationCannotOverrideMethod = MethodRelated + 616;
	/** @since 3.1 */
	int AnnotationMembersCannotHaveParameters = Syntax + Internal + 617;
	/** @since 3.1 */
	int AnnotationMembersCannotHaveTypeParameters = Syntax + Internal + 618;
	/** @since 3.1 */
	int AnnotationTypeDeclarationCannotHaveSuperclass = Syntax + Internal + 619;
	/** @since 3.1 */
	int AnnotationTypeDeclarationCannotHaveSuperinterfaces = Syntax + Internal + 620;
	/** @since 3.1 */
	int DuplicateTargetInTargetAnnotation = Internal + 621;
	/** @since 3.1 */
	int DisallowedTargetForAnnotation = TypeRelated + 622;
	/** @since 3.1 */
	int MethodMustOverride = MethodRelated + 623;
	/** @since 3.1 */
	int AnnotationTypeDeclarationCannotHaveConstructor = Syntax + Internal + 624;
	/** @since 3.1 */
	int AnnotationValueMustBeAnnotation = Internal + 625;
	/** @since 3.1 */
	int AnnotationTypeUsedAsSuperInterface = TypeRelated + 626;
	/** @since 3.1 */
	int MissingOverrideAnnotation = MethodRelated + 627;
	/** @since 3.1 */
	int FieldMissingDeprecatedAnnotation = Internal + 628;
	/** @since 3.1 */
	int MethodMissingDeprecatedAnnotation = Internal + 629;
	/** @since 3.1 */
	int TypeMissingDeprecatedAnnotation = Internal + 630;
	/** @since 3.1 */
	int UnhandledWarningToken = Internal + 631;
	/** @since 3.2 */
	int AnnotationValueMustBeArrayInitializer = Internal + 632;
	/** @since 3.3 */
	int AnnotationValueMustBeAnEnumConstant = Internal + 633;
	/** @since 3.3 */
	int MethodMustOverrideOrImplement = MethodRelated + 634;
	/** @since 3.4 */
	int UnusedWarningToken = Internal + 635;
	/** @since 3.6 */
	int MissingOverrideAnnotationForInterfaceMethodImplementation = MethodRelated + 636;
	/** @since 3.10 */
    int InvalidUsageOfTypeAnnotations = Syntax + Internal + 637;
    /** @since 3.10 */
    int DisallowedExplicitThisParameter = Syntax + Internal + 638;
    /** @since 3.10 */
    int MisplacedTypeAnnotations = Syntax + Internal + 639;
    /** @since 3.10 */
    int IllegalTypeAnnotationsInStaticMemberAccess = Internal + Syntax + 640;
    /** @since 3.10 */
    int IllegalUsageOfTypeAnnotations = Internal + Syntax + 641;
    /** @since 3.10 */
    int IllegalDeclarationOfThisParameter = Internal + Syntax + 642;
    /** @since 3.10 */
    int ExplicitThisParameterNotBelow18 = Internal + Syntax + 643;
    /** @since 3.10 */
    int DefaultMethodNotBelow18 = Internal + Syntax + 644;
    /** @since 3.10 */
    int LambdaExpressionNotBelow18 = Internal + Syntax + 645;
    /** @since 3.10 */
    int MethodReferenceNotBelow18 = Internal + Syntax + 646;
    /** @since 3.10 */
    int ConstructorReferenceNotBelow18 = Internal + Syntax + 647;
    /** @since 3.10 */
    int ExplicitThisParameterNotInLambda = Internal + Syntax + 648;
    /**
     * @since 3.10
     * @deprecated Per https://bugs.openjdk.java.net/browse/JDK-8231435 this problem is no longer raised
     */
    @Deprecated
    int ExplicitAnnotationTargetRequired = TypeRelated + 649;
    /** @since 3.10 */
    int IllegalTypeForExplicitThis = Internal + Syntax + 650;
    /** @since 3.10 */
    int IllegalQualifierForExplicitThis = Internal + Syntax + 651;
    /** @since 3.10 */
    int IllegalQualifierForExplicitThis2 = Internal + Syntax + 652;
    /** @since 3.10 */
    int TargetTypeNotAFunctionalInterface = Internal + TypeRelated + 653;
    /** @since 3.10 */
    int IllegalVarargInLambda = Internal + TypeRelated + 654;
    /** @since 3.10 */
    int illFormedParameterizationOfFunctionalInterface = Internal + TypeRelated + 655;
    /** @since 3.10 */
    int lambdaSignatureMismatched = Internal + TypeRelated + 656;
    /** @since 3.10 */
    int lambdaParameterTypeMismatched = Internal + TypeRelated + 657;
    /** @since 3.10 */
    int IncompatibleLambdaParameterType = Internal + TypeRelated + 658;
    /** @since 3.10 */
    int NoGenericLambda = Internal + TypeRelated + 659;
    /**
	 * More problems in generics
	 */
    /** @since 3.4 */
    int UnusedTypeArgumentsForConstructorInvocation = MethodRelated + 660;
	/** @since 3.9 */
	int UnusedTypeParameter = TypeRelated + 661;
	/** @since 3.9 */
	int IllegalArrayOfUnionType = TypeRelated + 662;
	/** @since 3.10 */
	int OuterLocalMustBeEffectivelyFinal = Internal + 663;
	/** @since 3.10 */
	int InterfaceNotFunctionalInterface = Internal + TypeRelated + 664;
	/** @since 3.10 */
	int ConstructionTypeMismatch = Internal + TypeRelated + 665;
    /** @since 3.10 */
    int ToleratedMisplacedTypeAnnotations = Syntax + Internal + 666;
    /** @since 3.13*/
    int InterfaceSuperInvocationNotBelow18 = Internal + Syntax + 667;
    /** @since 3.13*/
    int InterfaceStaticMethodInvocationNotBelow18 = Internal + Syntax + 668;
	/** @since 3.14 */
	int FieldMustBeFinal = Internal + 669;


	/**
	 * Null analysis for other kinds of expressions, syntactically nonnull
	 */
	/** @since 3.9 */
	int NonNullExpressionComparisonYieldsFalse = Internal + 670;
	/** @since 3.9 */
	int RedundantNullCheckOnNonNullExpression = Internal + 671;
	/** @since 3.9 */
	int NullExpressionReference = Internal + 672;
	/** @since 3.9 */
	int PotentialNullExpressionReference = Internal + 673;

	/**
	 * Corrupted binaries
	 */
	/** @since 3.1 */
	int CorruptedSignature = Internal + 700;
	/**
	 * Corrupted source
	 */
	/** @since 3.2 */
	int InvalidEncoding = Internal + 701;
	/** @since 3.2 */
	int CannotReadSource = Internal + 702;

	/**
	 * Autoboxing
	 */
	/** @since 3.1 */
	int BoxingConversion = Internal + 720;
	/** @since 3.1 */
	int UnboxingConversion = Internal + 721;

	/**
	 * Enum
	 */
	/** @since 3.1 */
	int IllegalModifierForEnum = TypeRelated + 750;
	/** @since 3.1 */
	int IllegalModifierForEnumConstant = FieldRelated + 751;
	/** @deprecated - problem could not be reported, enums cannot be local takes precedence
	 *   @since 3.1 */
	int IllegalModifierForLocalEnum = TypeRelated + 752;
	/** @since 3.1 */
	int IllegalModifierForMemberEnum = TypeRelated + 753;
	/** @since 3.1 */
	int CannotDeclareEnumSpecialMethod = MethodRelated + 754;
	/** @since 3.1 */
	int IllegalQualifiedEnumConstantLabel = FieldRelated + 755;
	/** @since 3.1 */
	int CannotExtendEnum = TypeRelated + 756;
	/** @since 3.1 */
	int CannotInvokeSuperConstructorInEnum = MethodRelated + 757;
	/** @since 3.1 */
	int EnumAbstractMethodMustBeImplemented = MethodRelated + 758;
	/** @since 3.1 */
	int EnumSwitchCannotTargetField = FieldRelated + 759;
	/** @since 3.1 */
	int IllegalModifierForEnumConstructor = MethodRelated + 760;
	/** @since 3.1 */
	int MissingEnumConstantCase = FieldRelated + 761;
	/** @since 3.2 */ // TODO need to fix 3.1.1 contribution (inline this constant on client side)
	int EnumStaticFieldInInInitializerContext = FieldRelated + 762;
	/** @since 3.4 */
	int EnumConstantMustImplementAbstractMethod = MethodRelated + 763;
	/** @since 3.5 */
	int EnumConstantCannotDefineAbstractMethod = MethodRelated + 764;
	/** @since 3.5 */
	int AbstractMethodInEnum = MethodRelated + 765;
	/** @since 3.8 */
	int MissingEnumDefaultCase = Internal + 766;
	/** @since 3.8 */
	int MissingDefaultCase = Internal + 767;
	/** @since 3.8 */
	int MissingEnumConstantCaseDespiteDefault = FieldRelated + 768;
	/** @since 3.8 */
	int UninitializedLocalVariableHintMissingDefault = Internal + 769;
	/** @since 3.8 */
	int UninitializedBlankFinalFieldHintMissingDefault = FieldRelated + 770;
	/** @since 3.8 */
	int ShouldReturnValueHintMissingDefault = MethodRelated + 771;

	/**
	 * Var args
	 */
	/** @since 3.1 */
	int IllegalExtendedDimensionsForVarArgs = Syntax + Internal + 800;
	/** @since 3.1 */
	int MethodVarargsArgumentNeedCast = MethodRelated + 801;
	/** @since 3.1 */
	int ConstructorVarargsArgumentNeedCast = ConstructorRelated + 802;
	/** @since 3.1 */
	int VarargsConflict = MethodRelated + 803;
	/** @since 3.7.1 */
	int SafeVarargsOnFixedArityMethod = MethodRelated + 804;
	/** @since 3.7.1 */
	int SafeVarargsOnNonFinalInstanceMethod = MethodRelated + 805;
	/** @since 3.7.1 */
	int PotentialHeapPollutionFromVararg = MethodRelated + 806;
	/** @since 3.8 */
	int VarargsElementTypeNotVisible = MethodRelated + 807;
	/** @since 3.8 */
	int VarargsElementTypeNotVisibleForConstructor = ConstructorRelated + 808;
	/** @since 3.10 */
	int ApplicableMethodOverriddenByInapplicable = MethodRelated + 809;

	/**
	 * Javadoc Generic
	 */
	/** @since 3.1 */
	int JavadocGenericMethodTypeArgumentMismatch = Javadoc + Internal + 850;
	/** @since 3.1 */
	int JavadocNonGenericMethod = Javadoc + Internal + 851;
	/** @since 3.1 */
	int JavadocIncorrectArityForParameterizedMethod = Javadoc + Internal + 852;
	/** @since 3.1 */
	int JavadocParameterizedMethodArgumentTypeMismatch = Javadoc + Internal + 853;
	/** @since 3.1 */
	int JavadocTypeArgumentsForRawGenericMethod = Javadoc + Internal + 854;
	/** @since 3.1 */
	int JavadocGenericConstructorTypeArgumentMismatch = Javadoc + Internal + 855;
	/** @since 3.1 */
	int JavadocNonGenericConstructor = Javadoc + Internal + 856;
	/** @since 3.1 */
	int JavadocIncorrectArityForParameterizedConstructor = Javadoc + Internal + 857;
	/** @since 3.1 */
	int JavadocParameterizedConstructorArgumentTypeMismatch = Javadoc + Internal + 858;
	/** @since 3.1 */
	int JavadocTypeArgumentsForRawGenericConstructor = Javadoc + Internal + 859;

	/**
	 * Java 7 errors
	 */
	/** @since 3.7.1 */
	int AssignmentToMultiCatchParameter = Internal + 870;
	/** @since 3.7.1 */
	int ResourceHasToImplementAutoCloseable = TypeRelated + 871;
	/** @since 3.7.1 */
	int AssignmentToResource = Internal + 872;
	/** @since 3.7.1 */
	int InvalidUnionTypeReferenceSequence = Internal + TypeRelated + 873;
	/** @since 3.7.1 */
	int AutoManagedResourceNotBelow17 = Syntax + Internal + 874;
	/** @since 3.7.1 */
	int MultiCatchNotBelow17 =  Syntax + Internal + 875;
	/** @since 3.7.1 */
	int PolymorphicMethodNotBelow17 = MethodRelated + 876;
	/** @since 3.7.1 */
	int IncorrectSwitchType17 = TypeRelated + 877;
	/** @since 3.7.1 */
	int CannotInferElidedTypes = TypeRelated + 878;
	/** @since 3.7.1 */
	int CannotUseDiamondWithExplicitTypeArguments = TypeRelated + 879;
	/** @since 3.7.1 */
	int CannotUseDiamondWithAnonymousClasses = TypeRelated + 880;
	/** @since 3.7.1 */
	int SwitchOnStringsNotBelow17 = TypeRelated + 881;	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348492
	/** @since 3.7.1 */
	int UnhandledExceptionOnAutoClose =  TypeRelated + 882;
	/** @since 3.7.1 */
	int DiamondNotBelow17 =  TypeRelated + 883;
	/** @since 3.7.1 */
	int RedundantSpecificationOfTypeArguments = TypeRelated + 884;
	/** @since 3.8 */
	int PotentiallyUnclosedCloseable = Internal + 885;
	/** @since 3.8 */
	int PotentiallyUnclosedCloseableAtExit = Internal + 886;
	/** @since 3.8 */
	int UnclosedCloseable = Internal + 887;
	/** @since 3.8 */
	int UnclosedCloseableAtExit = Internal + 888;
	/** @since 3.8 */
	int ExplicitlyClosedAutoCloseable = Internal + 889;
	/** @since 3.8 */
	int SwitchOnEnumNotBelow15 = TypeRelated + 890;	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=360317
	/** @since 3.10 */
	int IntersectionCastNotBelow18 = TypeRelated + 891;
	/** @since 3.10 */
	int IllegalBasetypeInIntersectionCast = TypeRelated + 892;
	/** @since 3.10 */
	int IllegalArrayTypeInIntersectionCast = TypeRelated + 893;
	/** @since 3.10 */
	int DuplicateBoundInIntersectionCast = TypeRelated + 894;
	/** @deprecated This problem is no longer reported; number Of functional interface is not an issue, number of abstract methods is.
	 * @since 3.10 */
	int MultipleFunctionalInterfaces = TypeRelated + 895;
	/** @since 3.10 */
	int StaticInterfaceMethodNotBelow18 = Internal + Syntax + 896;
	/** @since 3.10 */
	int DuplicateAnnotationNotMarkedRepeatable = TypeRelated + 897;
	/** @since 3.10 */
	int DisallowedTargetForContainerAnnotationType = TypeRelated + 898;
	/** @since 3.10 */
	int RepeatedAnnotationWithContainerAnnotation = TypeRelated + 899;

	/** @since 3.14 */
	int AutoManagedVariableResourceNotBelow9 = Syntax + Internal + 1351;
	/**
	 * External problems -- These are problems defined by other plugins
	 */

	/** @since 3.2 */
	int ExternalProblemNotFixable = 900;

	// indicates an externally defined problem that has a quick-assist processor
	// associated with it
	/** @since 3.2 */
	int ExternalProblemFixable = 901;

	/** @since 3.10 */
	int ContainerAnnotationTypeHasWrongValueType = TypeRelated + 902;
	/** @since 3.10 */
	int ContainerAnnotationTypeMustHaveValue = TypeRelated + 903;
	/** @since 3.10 */
	int ContainerAnnotationTypeHasNonDefaultMembers = TypeRelated + 904;
	/** @since 3.10 */
	int ContainerAnnotationTypeHasShorterRetention = TypeRelated + 905;
	/** @since 3.10 */
	int RepeatableAnnotationTypeTargetMismatch = TypeRelated + 906;
	/** @since 3.10 */
	int RepeatableAnnotationTypeIsDocumented = TypeRelated + 907;
	/** @since 3.10 */
	int RepeatableAnnotationTypeIsInherited = TypeRelated + 908;
	/** @since 3.10 */
	int RepeatableAnnotationWithRepeatingContainerAnnotation = TypeRelated + 909;

	/**
	 * Errors/warnings from annotation based null analysis
	 */
	/** @since 3.8 */
	int RequiredNonNullButProvidedNull = TypeRelated + 910;
	/** @since 3.8 */
	int RequiredNonNullButProvidedPotentialNull = TypeRelated + 911;
	/** @since 3.8 */
	int RequiredNonNullButProvidedUnknown = TypeRelated + 912;
	/** @since 3.8 */
	int MissingNonNullByDefaultAnnotationOnPackage = Internal + 913; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
	/** @since 3.8 */
	int IllegalReturnNullityRedefinition = MethodRelated + 914;
	/** @since 3.8 */
	int IllegalRedefinitionToNonNullParameter = MethodRelated + 915;
	/** @since 3.8 */
	int IllegalDefinitionToNonNullParameter = MethodRelated + 916;
	/** @since 3.8 */
	int ParameterLackingNonNullAnnotation = MethodRelated + 917;
	/** @since 3.8 */
	int ParameterLackingNullableAnnotation = MethodRelated + 918;
	/** @since 3.8 */
	int PotentialNullMessageSendReference = Internal + 919;
	/** @since 3.8 */
	int RedundantNullCheckOnNonNullMessageSend = Internal + 920;
	/** @since 3.8 */
	int CannotImplementIncompatibleNullness = Internal + 921;
	/** @since 3.8 */
	int RedundantNullAnnotation = MethodRelated + 922;
	/** @since 3.8 */
	int IllegalAnnotationForBaseType = TypeRelated + 923;
	/** @since 3.9 */
	int NullableFieldReference = FieldRelated + 924;
	/** @since 3.8 */
	int RedundantNullDefaultAnnotation = Internal + 925; // shouldn't actually occur any more after bug 366063
	/** @since 3.8 */
	int RedundantNullDefaultAnnotationPackage = Internal + 926;
	/** @since 3.8 */
	int RedundantNullDefaultAnnotationType = Internal + 927;
	/** @since 3.8 */
	int RedundantNullDefaultAnnotationMethod = Internal + 928;
	/** @since 3.8 */
	int ContradictoryNullAnnotations = Internal + 929;
	/** @since 3.8 */
	int MissingNonNullByDefaultAnnotationOnType = Internal + 930; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
	/** @since 3.8 */
	int RedundantNullCheckOnSpecdNonNullLocalVariable = Internal + 931;
	/** @since 3.8 */
	int SpecdNonNullLocalVariableComparisonYieldsFalse = Internal + 932;
	/** @since 3.8 */
	int RequiredNonNullButProvidedSpecdNullable = Internal + 933;
	/** @since 3.9 */
	int UninitializedNonNullField = FieldRelated + 934;
	/** @since 3.9 */
	int UninitializedNonNullFieldHintMissingDefault = FieldRelated + 935;
	/** @since 3.9 */
	int NonNullMessageSendComparisonYieldsFalse = Internal + 936;
	/** @since 3.9 */
	int RedundantNullCheckOnNonNullSpecdField = Internal + 937;
	/** @since 3.9 */
	int NonNullSpecdFieldComparisonYieldsFalse = Internal + 938;
	/** @since 3.9 */
	int ConflictingNullAnnotations = MethodRelated + 939;
	/** @since 3.9 */
	int ConflictingInheritedNullAnnotations = MethodRelated + 940;
	/** @since 3.10 */
	int RedundantNullCheckOnField = Internal + 941;
	/** @since 3.10 */
	int FieldComparisonYieldsFalse = Internal + 942;
	/** @since 3.14 */
	int RedundantNullDefaultAnnotationModule = Internal + 943;
	/** @since 3.19 */
	int RedundantNullCheckOnConstNonNullField = Internal + 944;
	/** @since 3.20 */
	int ConstNonNullFieldComparisonYieldsFalse = Internal + 945;
	/** @since 3.21 */
	int InheritedParameterLackingNonNullAnnotation = MethodRelated + 946;

	/** @since 3.10 */
	int ArrayReferencePotentialNullReference = Internal + 951;
	/** @since 3.10 */
	int DereferencingNullableExpression = Internal + 952;
	/** @since 3.10 */
	int NullityMismatchingTypeAnnotation = Internal + 953;
	/** @since 3.10 */
	int NullityMismatchingTypeAnnotationSuperHint = Internal + 954;
	/** @since 3.10 */
	int NullityUncheckedTypeAnnotationDetail = Internal + 955;
	/** @since 3.10 */
	int NullityUncheckedTypeAnnotationDetailSuperHint = Internal + 956;
	/** @since 3.10 */
	int ReferenceExpressionParameterNullityMismatch = MethodRelated + 957;
	/** @since 3.10 */
	int ReferenceExpressionParameterNullityUnchecked = MethodRelated + 958;
	/** @since 3.10 */
	int ReferenceExpressionReturnNullRedef = MethodRelated + 959;
	/** @since 3.10 */
	int ReferenceExpressionReturnNullRedefUnchecked = MethodRelated + 960;
	/** @since 3.10 */
	int RedundantNullCheckAgainstNonNullType = Internal + 961;
	/** @since 3.10 */
	int NullAnnotationUnsupportedLocation = Internal + 962;
	/** @since 3.10 */
	int NullAnnotationUnsupportedLocationAtType = Internal + 963;
	/** @since 3.10 */
	int NullityMismatchTypeArgument = Internal + 964;
	/** @since 3.10 */
	int ContradictoryNullAnnotationsOnBound = Internal + 965;
	/** @since 3.10 */
	int ContradictoryNullAnnotationsInferred = Internal + 966;
	/** @since 3.10 */
	int UnsafeNullnessCast = Internal + 967;
	/** @since 3.10 */
	int NonNullDefaultDetailIsNotEvaluated = 968; // no longer reported
	/** @since 3.10 */
	int NullNotCompatibleToFreeTypeVariable = 969;
	/** @since 3.10 */
	int NullityMismatchAgainstFreeTypeVariable = 970;
	/** @since 3.11 */
	int ImplicitObjectBoundNoNullDefault = 971;
	/** @since 3.11 */
	int IllegalParameterNullityRedefinition = MethodRelated + 972;
	/** @since 3.11 */
	int ContradictoryNullAnnotationsInferredFunctionType = MethodRelated + 973;
	/** @since 3.11 */
	int IllegalReturnNullityRedefinitionFreeTypeVariable = MethodRelated + 974;
	/** @since 3.12 */
	int IllegalRedefinitionOfTypeVariable = 975;
	/** @since 3.12 */
	int UncheckedAccessOfValueOfFreeTypeVariable = 976;
	/** @since 3.12 */
	int UninitializedFreeTypeVariableField = 977;
	/** @since 3.12 */
	int UninitializedFreeTypeVariableFieldHintMissingDefault = 978;
	/** @since 3.12 */
	int RequiredNonNullButProvidedFreeTypeVariable = TypeRelated + 979;
	/** @since 3.12 */
	int NonNullTypeVariableFromLegacyMethod = TypeRelated + 980;
	/** @since 3.12 */
	int NonNullMethodTypeVariableFromLegacyMethod = TypeRelated + 981;
	/** @since 3.21 */
	int MissingNullAnnotationImplicitlyUsed = Internal + 982;
	/** @since 3.21 */
	int AnnotatedTypeArgumentToUnannotated = Internal + 983;
	/** @since 3.21 */
	int AnnotatedTypeArgumentToUnannotatedSuperHint = Internal + 984;


	// Java 8 work
	/** @since 3.10 */
	int IllegalModifiersForElidedType = Internal + 1001;
	/** @since 3.10 */
	int IllegalModifiers = Internal + 1002;

	/** @since 3.10 */
	int IllegalTypeArgumentsInRawConstructorReference = TypeRelated + 1003;

	// more on lambdas:
	/** @since 3.18 */
	int MissingValueFromLambda = Internal + 1004;

	// default methods:
	/** @since 3.10 */
	int IllegalModifierForInterfaceMethod18 = MethodRelated + 1050;

	/** @since 3.10 */
	int DefaultMethodOverridesObjectMethod = MethodRelated + 1051;

	/** @since 3.10 */
	int InheritedDefaultMethodConflictsWithOtherInherited = MethodRelated + 1052;

	/** @since 3.10 */
	int DuplicateInheritedDefaultMethods = MethodRelated + 1053;

	/** @since 3.10 */
	int SuperAccessCannotBypassDirectSuper = TypeRelated + 1054;
	/** @since 3.10 */
	int SuperCallCannotBypassOverride = MethodRelated + 1055;
	/** @since 3.10 */
	int IllegalModifierCombinationForInterfaceMethod = MethodRelated + 1056;
	/** @since 3.10 */
	int IllegalStrictfpForAbstractInterfaceMethod = MethodRelated + 1057;
	/** @since 3.10 */
	int IllegalDefaultModifierSpecification = MethodRelated + 1058;
	/** @since 3.13 */
	int CannotInferInvocationType = TypeRelated + 1059;


	/** @since 3.13 */
	int TypeAnnotationAtQualifiedName = Internal + Syntax + 1060;

	/** @since 3.13 */
	int NullAnnotationAtQualifyingType = Internal + Syntax + 1061;

	/** @since 3.14*/
	int IllegalModifierForInterfaceMethod9 = MethodRelated + 1071;
	/** @since 3.14*/
	int IllegalModifierCombinationForPrivateInterfaceMethod9 = MethodRelated + 1070;
	/** @since 3.14 */
	int UndefinedModule = ModuleRelated + 1300;
	/** @since 3.14 */
	int DuplicateRequires = ModuleRelated + 1301;
	/** @since 3.14 */
	int DuplicateExports = ModuleRelated + 1302;
	/** @since 3.14 */
	int DuplicateUses = ModuleRelated + 1303;
	/** @since 3.14 */
	int DuplicateServices = ModuleRelated + 1304;
	/** @since 3.14 */
	int CyclicModuleDependency = ModuleRelated + 1305;
	/** @since 3.14 */
	int AbstractServiceImplementation = TypeRelated + 1306;
	/** @since 3.14 */
	int ProviderMethodOrConstructorRequiredForServiceImpl = TypeRelated + 1307;
	/** @since 3.14 */
	int ServiceImplDefaultConstructorNotPublic = TypeRelated + 1308;
	/** @since 3.14 */
	int NestedServiceImpl = TypeRelated + 1309;
	/** @since 3.14 */
	int ServiceImplNotDefinedByModule = TypeRelated + 1310;
	/** @since 3.14 */
	int PackageDoesNotExistOrIsEmpty = ModuleRelated + 1311;
	/** @since 3.14 */
	int NonDenotableTypeArgumentForAnonymousDiamond = TypeRelated + 1312;
	/** @since 3.14 */
	int DuplicateOpens = ModuleRelated + 1313;
	/** @since 3.14 */
	int DuplicateModuleRef = ModuleRelated + 1314;
	/** @since 3.14 */
	int InvalidOpensStatement = ModuleRelated + 1315;
	/** @since 3.14 */
	int InvalidServiceIntfType = ModuleRelated + 1316;
	/** @since 3.14 */
	int InvalidServiceImplType = ModuleRelated + 1317;
	/** @since 3.14 */
	int IllegalModifierForModule = ModuleRelated + 1318;
	/** @since 3.18 */
	int UndefinedModuleAddReads = ModuleRelated + 1319;
	/** @since 3.20 */
	int ExportingForeignPackage = ModuleRelated + 1320;


	/** @since 3.14 */
	int DuplicateResource = Internal + 1251;

	// terminally
	/** @since 3.14 */
	int UsingTerminallyDeprecatedType = TypeRelated + 1400;
	/** @since 3.14 */
	int UsingTerminallyDeprecatedMethod = MethodRelated + 1401;
	/** @since 3.14 */
	int UsingTerminallyDeprecatedConstructor = MethodRelated + 1402;
	/** @since 3.14 */
	int UsingTerminallyDeprecatedField = FieldRelated + 1403;
	/** @since 3.14 */
	int OverridingTerminallyDeprecatedMethod = MethodRelated + 1404;
	// with since
	/** @since 3.14 */
	int UsingDeprecatedSinceVersionType = TypeRelated + 1405;
	/** @since 3.14 */
	int UsingDeprecatedSinceVersionMethod = MethodRelated + 1406;
	/** @since 3.14 */
	int UsingDeprecatedSinceVersionConstructor = MethodRelated + 1407;
	/** @since 3.14 */
	int UsingDeprecatedSinceVersionField = FieldRelated + 1408;
	/** @since 3.14 */
	int OverridingDeprecatedSinceVersionMethod = MethodRelated + 1409;
	// terminally with since
	/** @since 3.14 */
	int UsingTerminallyDeprecatedSinceVersionType = TypeRelated + 1410;
	/** @since 3.14 */
	int UsingTerminallyDeprecatedSinceVersionMethod = MethodRelated + 1411;
	/** @since 3.14 */
	int UsingTerminallyDeprecatedSinceVersionConstructor = MethodRelated + 1412;
	/** @since 3.14 */
	int UsingTerminallyDeprecatedSinceVersionField = FieldRelated + 1413;
	/** @since 3.14 */
	int OverridingTerminallyDeprecatedSinceVersionMethod = MethodRelated + 1414;

	// unused constants:
	/** @since 3.14 */
	int UsingDeprecatedPackage = ModuleRelated + 1425;
	/** @since 3.14 */
	int UsingDeprecatedSinceVersionPackage = ModuleRelated + 1426;
	/** @since 3.14 */
	int UsingTerminallyDeprecatedPackage = ModuleRelated + 1427;
	/** @since 3.14 */
	int UsingTerminallyDeprecatedSinceVersionPackage = ModuleRelated + 1428;
	// deprecation of modules:
	/** @since 3.14 */
	int UsingDeprecatedModule = ModuleRelated + 1429;
	/** @since 3.14 */
	int UsingDeprecatedSinceVersionModule = ModuleRelated + 1430;
	/** @since 3.14 */
	int UsingTerminallyDeprecatedModule = ModuleRelated + 1431;
	/** @since 3.14 */
	int UsingTerminallyDeprecatedSinceVersionModule = ModuleRelated + 1432;

	/** @since 3.14 */
	int NotAccessibleType = TypeRelated + 1450;
	/** @since 3.14 */
	int NotAccessibleField = FieldRelated + 1451;
	/** @since 3.14 */
	int NotAccessibleMethod = MethodRelated + 1452;
	/** @since 3.14 */
	int NotAccessibleConstructor = MethodRelated + 1453;
	/** @since 3.14 */
	int NotAccessiblePackage = ImportRelated + 1454;
	/** @since 3.14 */
	int ConflictingPackageFromModules = ModuleRelated + 1455;
	/** @since 3.14 */
	int ConflictingPackageFromOtherModules = ModuleRelated + 1456;
	/** @since 3.14 */
	int NonPublicTypeInAPI = ModuleRelated + 1457;
	/** @since 3.14 */
	int NotExportedTypeInAPI = ModuleRelated + 1458;
	/** @since 3.14 */
	int MissingRequiresTransitiveForTypeInAPI = ModuleRelated + 1459;
	/** @since  3.14 */
	int UnnamedPackageInNamedModule = ModuleRelated + 1460;
	/** @since  3.14 */
	int UnstableAutoModuleName = ModuleRelated + 1461;
	/** @since  3.24 */
	int ConflictingPackageInModules = ModuleRelated + 1462;

	// doc variant of an above constant:
	/** @since 3.22 */
	int JavadocNotAccessibleType = Javadoc + NotAccessibleType;

	/** @since 3.13 */
	int RedundantNullDefaultAnnotationLocal = Internal + 1062;

	/** @since 3.13 */
	int RedundantNullDefaultAnnotationField = Internal + 1063;

	/** @since 3.10 */
	int GenericInferenceError = 1100; 	// FIXME: This is just a stop-gap measure, be more specific via https://bugs.eclipse.org/404675

	/** @deprecated - problem is no longer generated (implementation issue has been resolved)
	 * @since 3.10 */
	int LambdaShapeComputationError = 1101;
	/** @since 3.13 */
	int ProblemNotAnalysed = 1102;
	/** @since 3.18 */
	int PreviewFeatureDisabled = Compliance + 1103;
	/** @since 3.18 */
	int PreviewFeatureUsed = Compliance + 1104;
	/** @since 3.18 */
	int PreviewFeatureNotSupported = Compliance + 1105;
	/** @since 3.20*/
	int PreviewFeaturesNotAllowed = PreviewRelated + 1106;
	/** @since 3.24*/
	int FeatureNotSupported = Compliance + 1107;

	/** @since 3.13 */
	int UnlikelyCollectionMethodArgumentType = 1200;
	/** @since 3.13 */
	int UnlikelyEqualsArgumentType = 1201;

//{ObjectTeams:
	int OTJ_RELATED = 100000;
	int OTCHAP = 10000;
	int TeamCannotHaveSuperTypes                 = OTJ_RELATED +    1;
	int IllegalModifierForTeam                   = OTJ_RELATED +    2;

//  ==== TEAMS AND ROLES: ====
	int TEAM_RELATED = OTJ_RELATED + 1*OTCHAP;
	int StaticRole                               = TEAM_RELATED + 2101; // 1.2.1
	int IllegalModifierForRole                   = TEAM_RELATED + 2102; // 1.2.1(a)
	int NotVisibleRoleMethod                     = TEAM_RELATED + 2103; // 1.2.1(e)
	int NotVisibleRoleConstructor 				 = TEAM_RELATED + 2104; // 1.2.1(e)
	int DifferentTeamInstance 					 = TEAM_RELATED + 2105; // 1.2.1(e)
	int NonPublicFieldOfExternalizedRole         = TEAM_RELATED + 2106; // 1.2.1(e)
	int ExternalizedCallToNonPublicMethod		 = TEAM_RELATED + 2107; // 1.2.1(e)
	int ExternalizedCallToNonPublicConstructor 	 = TEAM_RELATED + 2108; // 1.2.1(e)
	int IndirectTSuperInvisible					 = TEAM_RELATED + 2109; // 1.2.1(e)
	int RoleCantInitializeStaticField            = TEAM_RELATED + 2110; // 1.2.1(g)

	int ExternalizingNonPublicRole               = TEAM_RELATED + 2200; // 1.2.2(a)
	// unused: TEAM_RELATED + 2201
	int MissingAnchorForRoleType                 = TEAM_RELATED + 2202; // 1.2.2(b)
	int TypeAnchorNotEnclosingTeam               = TEAM_RELATED + 2203; // 1.2.2(b)
	int IllegalTypeAnchorNotATeam                = TEAM_RELATED + 2204; // 1.2.2(b)
	int UnresolvedTypeAnchor                     = TEAM_RELATED + 2205; // 1.2.2(b)
	int NoSuchRoleInTeam                         = TEAM_RELATED + 2206; // 1.2.2(b)
	int DeprecatedPathSyntax 					 = TEAM_RELATED + 2207; // 1.2.2(b)
	int QualifiedRole							 = TEAM_RELATED + 2208; // 1.2.2(b)
	int AnchorNotFinal                           = TEAM_RELATED + 2209; // 1.2.2(c)
	int AnchorPathNotFinal                       = TEAM_RELATED + 2210; // 1.2.2(c)
	int AnchorNotAVariable                       = TEAM_RELATED + 2211; // 1.2.2(c)
	int RoleAllocationNotRelativeToEnclosingTeam = TEAM_RELATED + 2212; // 1.2.2(c)
	int NoTeamAnchorInScope                      = TEAM_RELATED + 2213; // 1.2.2(f)
	int ExternalizedRoleNotAllowedHere           = TEAM_RELATED + 2214; // 1.2.2(g)
	int ExtendingExternalizedRole                = TEAM_RELATED + 2215; // 1.2.2(g)
	int CycleInFieldAnchor                       = TEAM_RELATED + 2216; // 1.2.2
	int CannotImportRole 						 = TEAM_RELATED + 2217; // 1.2.2(i)
	int QualifiedProtectedRole                   = TEAM_RELATED + 2300; // 1.2.3(b)
	int ParameterizedProtectedRole               = TEAM_RELATED + 2301; // 1.2.3(b)

	int RoleFileMustDeclareOneType               = TEAM_RELATED + 2501; // 1.2.5(b)
	int RoleFileMismatchingName 				 = TEAM_RELATED + 2502; // 1.2.5(b)
	int MismatchingPackageForRole                = TEAM_RELATED + 2503; // 1.2.5(c)
	int NonTeamPackageForRole 			         = TEAM_RELATED + 2504; // 1.2.5(c)
	int NoEnclosingTeamForRoleFile 				 = TEAM_RELATED + 2505; // 1.2.5(c)
	int JavadocMissingRoleTag 					 = TEAM_RELATED + 2506; // 1.2.5(d)
	int JavadocRoleTagNotRoleFile				 = TEAM_RELATED + 2507; // 1.2.5(d)
	int JavadocRoleTagNotRole					 = TEAM_RELATED + 2508; // 1.2.5(d)
	int JavadocRoleTagInlineRole				 = TEAM_RELATED + 2509; // 1.2.5(d)
	int JavadocRoleTagInRegular					 = TEAM_RELATED + 2510; // 1.2.5(d)
	int RoleFileCantBeEnum                       = TEAM_RELATED + 2511; // 1.2.5(e)


	int RegularExtendsTeam 			             = TEAM_RELATED + 3001; // 1.3
	int RoleClassOverridesInterface 	         = TEAM_RELATED + 3101; // 1.3.1(c)
	int RoleInterfaceOverridesClass 	         = TEAM_RELATED + 3102; // 1.3.1(c)
	int OverridingFinalRole						 = TEAM_RELATED + 3103; // 1.3.1(c)
	int RoleMustOverride						 = TEAM_RELATED + 3104; // 1.3.1(c)
	int MissingOverrideAnnotationForRole		 = TEAM_RELATED + 3105; // 1.3.1(c)
	int DifferentTeamClass 						 = TEAM_RELATED + 3106; // 1.3.1(d)
    int TSuperOutsideRole                        = TEAM_RELATED + 3107; // 1.3.1(f)
	int TSuperCallWithoutTSuperRole              = TEAM_RELATED + 3108; // 1.3.1(f)
	int InvalidQualifiedTSuper                   = TEAM_RELATED + 3109; // 1.3.1(f)
	int TSuperCallsWrongMethod                   = TEAM_RELATED + 3110; // 1.3.1(f)
	int ReducingRoleVisibility                   = TEAM_RELATED + 3111; // 1.3.1(h)
	int TSubMethodReducesVisibility              = TEAM_RELATED + 3112; // 1.3.1(h)
	int IncompatibleReturnInCopiedMethod		 = TEAM_RELATED + 3113; // 1.3.1(k)
	int ExtendIncompatibleEnclosingTypes         = TEAM_RELATED + 3201; // 1.3.2(a)
	int IncompatibleSuperclasses                 = TEAM_RELATED + 3202; // 1.3.2(b)
	int TsuperCtorDespiteRefinedExtends          = TEAM_RELATED + 3203; // 1.3.2(c)
	int CallToInheritedNonPublic				 = TEAM_RELATED + 3204; // 1.3.2(e)

	int RoleShadowsVisibleType 					 = TEAM_RELATED + 4001; // 1.4(a)
	int ImplicitlyHidingField                    = TEAM_RELATED + 4002; // 1.4(b)

	int RegularOverridesTeam                     = TEAM_RELATED + 5001; // 1.5(a)
	int MissingTeamForRoleWithMembers            = TEAM_RELATED + 5002; // 1.5(a,b)
	int TeamExtendingEnclosing                   = TEAM_RELATED + 5003; // 1.5(c)
	int IncomparableTSupers                      = TEAM_RELATED + 5004; // 1.5(d)

//  ==== ROLE-BASE-BINDING ====
	int ROLE_RELATED = OTJ_RELATED + 2*OTCHAP;
	int OverlappingRoleHierarchies               = ROLE_RELATED + 1001; // 2.1(c)
	int IllegalPlayedByRedefinition              = ROLE_RELATED + 1002; // 2.1(c)
	int IncompatibleBaseclasses                  = ROLE_RELATED + 1003; // 2.1(c)
	int OverridingPlayedBy                       = ROLE_RELATED + 1004; // 2.1(d)
	int BaseclassMustBeAClass                    = ROLE_RELATED + 1101; // 2.1.1
	int BaseclassIsMember                        = ROLE_RELATED + 1201; // 2.1.2
	int BaseclassIsRoleOfTheSameTeam             = ROLE_RELATED + 1202; // 2.1.2(a)
	int BaseclassIsEnclosing 			         = ROLE_RELATED + 1203; // 2.1.2(b)
	int BaseclassCircularity                     = ROLE_RELATED + 1204; // 2.1.2(b)
	int BaseclassDecapsulation 				     = ROLE_RELATED + 1205; // 2.1.2(c)
	int BaseclassDecapsulationFinal			     = ROLE_RELATED + 1206; // 2.1.2(c)
	int ConfinedDecapsulation 				     = ROLE_RELATED + 1207; // 2.1.2(c)
	int BaseImportInRegularClass 				 = ROLE_RELATED + 1208; // 2.1.2(d)
	int BaseImportInRoleFile                     = ROLE_RELATED + 1209; // 2.1.2(d)
	int RegularlyImportedBaseclass				 = ROLE_RELATED + 1210; // 2.1.2(d)
	int QualifiedReferenceToBaseclass			 = ROLE_RELATED + 1211; // 2.1.2(d)
	int ParameterizedBaseclass                   = ROLE_RELATED + 1212; // 2.1.2(e)
	int NonParameterizedBaseclass                = ROLE_RELATED + 1213; // 2.1.2(e)

	int DeprecatedBaseclass						 = ROLE_RELATED + 1299; // no OTJLD

	// ==>> do not change these numbers: they are used from persisted state to determine if base imports are legal:
	int IllegalBaseImport 						 = ROLE_RELATED + 1308; // base import vs. aspectBinding
	int IllegalBaseImportExpected				 = ROLE_RELATED + 1309; // base import vs. aspectBinding
	int IllegalBaseImportNoAspectBinding         = ROLE_RELATED + 1310; // base import vs. aspectBinding
	int BaseclassDecapsulationForcedExport	     = ROLE_RELATED + 1311; // 2.1.2(c) + OT/Equinox
	int AdaptedPluginAccess                      = ROLE_RELATED + 1312; // not an error but signal presence of an aspectBinding (OT/Equinox)
	int IllegalUseOfForcedExport				 = ROLE_RELATED + 1313; // OT/Equinox
	// <<==
	int BaseImportFromSplitPackage				 = ROLE_RELATED + 1314; // OT/Equinox
	int BaseImportFromSplitPackagePlural		 = ROLE_RELATED + 1315; // OT/Equinox

	int IllegalImplicitLower 					 = ROLE_RELATED + 2001; // 2.2(b)
	int OmitCastForLowering 					 = ROLE_RELATED + 2002; // 2.2(b)
	int UseTmpForLoweringInstanceof              = ROLE_RELATED + 2003; // 2.2(b)
	int AmbiguousUpcastOrLowering 				 = ROLE_RELATED + 2004; // 2.2(f)

	int NoDefaultCtorInBoundRole                 = ROLE_RELATED + 3101; // 2.3.1(b)
	int MissingEmptyCtorForLiftingCtor           = ROLE_RELATED + 3102; // 2.3.1(c)
	int ExplicitSuperInLiftConstructor           = ROLE_RELATED + 3103; // 2.3.1(c)
	/** @since 3.7 (OT 2.0) */
	int InstantiationAnnotationInNonRole		 = ROLE_RELATED + 3104; // 2.3.1(d)
	/** @since 3.7 (OT 2.0) */
	int FieldInRoleWithInstantiationPolicy		 = ROLE_RELATED + 3105; // 2.3.1(d)
	/** @since 3.7 (OT 2.0) */
	int MissingEqualsHashCodeWithInstantation	 = ROLE_RELATED + 3106; // 2.3.1(d)
	int RoleConstructorNeedingTSuperCall		 = ROLE_RELATED + 3107; // FIXME

	int DeclaredLiftingInStaticMethod            = ROLE_RELATED + 3201; // 2.3.2(a)
	int QualifiedLiftingType 					 = ROLE_RELATED + 3202; // 2.3.2(a)
	int LiftingTypeNotAllowedHere 				 = ROLE_RELATED + 3203; // 2.3.2(a)
	int PrimitiveTypeNotAllowedForLifting		 = ROLE_RELATED + 3204; // 2.3.2(a)
	int NeedRoleInLiftingType                    = ROLE_RELATED + 3205; // 2.3.2(a)
	int RoleNotBoundCantLift 					 = ROLE_RELATED + 3206; // 2.3.2(a)
	int IncompatibleBaseForRole 				 = ROLE_RELATED + 3207; // 2.3.2(a)
	int SyntaxErrorInDeclaredArrayLifting        = ROLE_RELATED + 3208; // 2.3.2(a)
	int RoleBoundIsNotRole					     = ROLE_RELATED + 3209; // 2.3.2(e)
	int GenericMethodTypeArgumentMismatchRoleBound=ROLE_RELATED + 3210; // 2.3.2(e)

	int RoleBindingPotentiallyAmbiguous          = ROLE_RELATED + 3401; // 2.3.4(a)
	/** @since 3.7 OT 2.0 */
	int AmbiguousLiftingMayBreakClients			 = ROLE_RELATED + 3402; // 2.3.5(d)
	/** @since 3.7 OT 2.0 */
	int UnhandledLiftingFailedException			 = ROLE_RELATED + 3403; // 2.3.5

	int QualifiedUseOfLiftingConstructor         = ROLE_RELATED + 4101; // 2.4.1(a)
	int LiftCtorArgNotAllocation                 = ROLE_RELATED + 4102; // 2.4.1(c)
	int BaseCtorInWrongMethod                    = ROLE_RELATED + 4201; // 2.4.2
	int BaseConstructorCallInLiftingConstructor  = ROLE_RELATED + 4202; // 2.4.2
	int CallsCtorWithMismatchingBaseCtor         = ROLE_RELATED + 4203; // 2.4.2
	int InvalidExplicitTSuperConstructorCall     = ROLE_RELATED + 4204; // 2.4.2
	int BaseAllocationDespiteBaseclassCycle      = ROLE_RELATED + 4205; // 2.4.2
	int BaseCtorCallIsNotFirst                   = ROLE_RELATED + 4206; // 2.4.2(b)
	int MissingCallToBaseConstructor             = ROLE_RELATED + 4207; // 2.4.2(b)
	int TooManyCallsToBaseConstructor            = ROLE_RELATED + 4208; // 2.4.2(b)
	int DecapsulationBaseCtor                    = ROLE_RELATED + 4209; // 2.4.2(b)
	int BaseConstructorExpressionOutsideCtorCall = ROLE_RELATED + 4210; // 2.4.2(c)
	int RoleConstructorHiddenByLiftingConstructor= ROLE_RELATED + 4211; // 2.4.2(d)
	int InstantiatingSupercededRole 			 = ROLE_RELATED + 4301; // 2.4.3


	int AbstractPotentiallyRelevantRole          = ROLE_RELATED + 5001; // 2.5(b)
	int AbstractRelevantRole                     = ROLE_RELATED + 5002; // 2.5(b)

	int BaseQualifiesNonTeam 					 = ROLE_RELATED + 6001; // 2.6(a)
	int UnboundQualifiedBase 					 = ROLE_RELATED + 6002; // 2.6(a)
	int BasecallInRegularMethod                  = ROLE_RELATED + 6003; // 2.6
	int BasecallInMethodMapping                  = ROLE_RELATED + 6004; // 2.6
	int BaseCallOutsideMethod                    = ROLE_RELATED + 6005; // 2.6
	int DecapsulationFieldReference              = ROLE_RELATED + 6006; // 2.6(g)
	int DecapsulationMessageSend 				 = ROLE_RELATED + 6007; // 2.6(g)


//  ==== CALLOUT ====
	int CALLOUT_RELATED = OTJ_RELATED + 3*OTCHAP;
	int CalloutMappingInNonRole                  = CALLOUT_RELATED + 1001; // 3.1(a)
	int CalloutMappingInUnboundRole              = CALLOUT_RELATED + 1002; // 3.1(a)
	int CalloutToEnclosing						 = CALLOUT_RELATED + 1003; // 3.1(a)
	int UnresolvedCalloutMethodSpec 			 = CALLOUT_RELATED + 1004; // 3.1(c)
	int AmbiguousCalloutMethodSpec               = CALLOUT_RELATED + 1005; // 3.1(c)
	int DifferentReturnInCalloutMethodSpec 		 = CALLOUT_RELATED + 1006; // 3.1(c)
	int DifferentParamInCalloutMethodSpec 	     = CALLOUT_RELATED + 1007; // 3.1(c)
	int RegularCalloutOverrides 				 = CALLOUT_RELATED + 1008; // 3.1(e)
	int AbstractMethodBoundAsOverride            = CALLOUT_RELATED + 1009; // 3.1(e)
	int CalloutOverridesLocal 					 = CALLOUT_RELATED + 1010; // 3.1(e)
	int RegularCalloutOverridesCallout 			 = CALLOUT_RELATED + 1011; // 3.1(f)
	int DuplicateCalloutBinding 				 = CALLOUT_RELATED + 1012; // 3.1(g)
	int CalloutUndeclaredException               = CALLOUT_RELATED + 1013; // 3.1(h)

	int AddingInferredCalloutForInherited 		 = CALLOUT_RELATED + 1014; // 3.1(j)
	int UsingInferredCalloutForMessageSend		 = CALLOUT_RELATED + 1015; // 3.1(j)
	int InferredCalloutInCompoundAssignment		 = CALLOUT_RELATED + 1016; // 3.1(j)

	int UnusedParamMap 							 = CALLOUT_RELATED + 2001; // 3.2
	int CalloutParameterMappingMissingSignatures = CALLOUT_RELATED + 2002; // 3.2(a)
	int DuplicateCalloutParamMapping 			 = CALLOUT_RELATED + 2003; // 3.2(b)
	int UnmappedBaseParameter 					 = CALLOUT_RELATED + 2004; // 3.2(b)
	int WrongBindingDirection 					 = CALLOUT_RELATED + 2005; // 3.2(b)
	int WrongBindingDirectionResult				 = CALLOUT_RELATED + 2006; // 3.2(c)
	int ResultMappingForVoidMethod               = CALLOUT_RELATED + 2007; // 3.2(c)
	int IllegalDirectionForCalloutResult         = CALLOUT_RELATED + 2008; // 3.2(c)
	int ResultNotDefinedForVoidMethodCallout     = CALLOUT_RELATED + 2009; // 3.2(c)
	int CalloutMappingResultToOther				 = CALLOUT_RELATED + 2010; // 3.2(c)
	int TooFewArgumentsInCallout	 			 = CALLOUT_RELATED + 2011; // 3.2(e)
	int CalloutMissingReturnType                 = CALLOUT_RELATED + 2012; // 3.2(e)
	int ParamMapInInterface						 = CALLOUT_RELATED + 2013; // 3.2(a)

	int CalloutTypeMismatch                      = CALLOUT_RELATED + 3001; // 3.3(d)
	int IncompatibleMappedCalloutArgument        = CALLOUT_RELATED + 3002; // 3.3(d)
	int ReturnRequiredInCalloutMethodMapping 	 = CALLOUT_RELATED + 3003; // 3.3(d)
	int CalloutIncompatibleReturnType            = CALLOUT_RELATED + 3004; // 3.3(d)

	int DecapsulationShort						 = CALLOUT_RELATED + 4001; // 3.4(a)
	int Decapsulation                            = CALLOUT_RELATED + 4002; // 3.4(a)
	int InvisibleMethodSpec                      = CALLOUT_RELATED + 4003; // 3.4 (TODO(SH): is it ever raised?)
	int CalloutInvisiblePrivate 				 = CALLOUT_RELATED + 4004; // 3.4(d)


	int UnresolvedFieldInCallout 				 = CALLOUT_RELATED + 5001; // 3.5
	int DifferentTypeInFieldSpec				 = CALLOUT_RELATED + 5002; // 3.5(a)
	int FieldAccessHasNoEffect 					 = CALLOUT_RELATED + 5003; // 3.5(b)
	int CalloutSetCantReturn                     = CALLOUT_RELATED + 5004; // 3.5(b)
	int CalloutToFieldMissingParameter 			 = CALLOUT_RELATED + 5005; // 3.5(b)
	int CalloutGetIncompatibleFieldType			 = CALLOUT_RELATED + 5006; // 3.5(b)
	int CalloutSetIncompatibleFieldType			 = CALLOUT_RELATED + 5007; // 3.5(b)
	int DecapsulationField                       = CALLOUT_RELATED + 5008; // 3.5(e)
	int DecapsulationFieldWrite                  = CALLOUT_RELATED + 5009; // 3.5(e)
	int FieldCalloutOverrides 					 = CALLOUT_RELATED + 5010; // 3.5(g)
	int FieldCalloutOverridesCallout 			 = CALLOUT_RELATED + 5011; // 3.5(g)

	int UsingCalloutToFieldForAssignment         = CALLOUT_RELATED + 5012; // 3.5(h)
	int UsingCalloutToFieldForFieldRead          = CALLOUT_RELATED + 5013; // 3.5(h)
	int UsingInferredCalloutToFieldForMessageSend= CALLOUT_RELATED + 5014; // 3.5(h)


//  ==== CALLIN ====
	int CALLIN_RELATED = OTJ_RELATED + 4*OTCHAP;
	int CallinMappingInNonRole                   = CALLIN_RELATED + 1001; // 4.1(b)
	int CallinMappingInUnboundRole               = CALLIN_RELATED + 1002; // 4.1(b)
	int CallinDespiteBindingAmbiguity 			 = CALLIN_RELATED + 1003; // 2.3.5(c)
	/** @since 3.7 OTDT 2.0.0 */
	int CallinDespiteAbstractRole				 = CALLIN_RELATED + 1004; // 2.3.5(c)
	int UnresolvedCallinMethodSpec 				 = CALLIN_RELATED + 1005; // 4.1(c)
	int AmbiguousCallinMethodSpec                = CALLIN_RELATED + 1006; // 4.1(c)
	int DifferentReturnInCallinMethodSpec        = CALLIN_RELATED + 1007; // 4.1(c)
	int DifferentParamInCallinMethodSpec 	     = CALLIN_RELATED + 1008; // 4.1(c)
	int DuplicateCallinName 					 = CALLIN_RELATED + 1009; // 4.1(e)
	int CallinToInheritedFinal 					 = CALLIN_RELATED + 1010; // 4.1(f)
	int CallinUndeclaredException                = CALLIN_RELATED + 1011; // 4.1(g)
	/** @since 3.9 OTDT 2.2 */
	int CallinToConstructorMustUseAfter			 = CALLIN_RELATED + 1012; // 4.1(h)
	/** @since 3.17 OTDT 2.7.3 */
	int CallinOverriddenInTeam					 = CALLIN_RELATED + 1013; // 4.1(e)

	int CallinToDeprecated						 = CALLIN_RELATED + 1099; // no OTJLD

	int CallinInNonRole                          = CALLIN_RELATED + 2001; // 4.2(d)
	int CallinWithVisibility 	            	 = CALLIN_RELATED + 2002; // 4.2(d)
	int CallToCallin                             = CALLIN_RELATED + 2003; // 4.2(d)
	int CalloutBindingCallin 					 = CALLIN_RELATED + 2004; // 4.2(d)
	int ReplaceMappingToNonCallin 				 = CALLIN_RELATED + 2005; // 4.2(d)
	int CallinMethodBoundNonReplace 			 = CALLIN_RELATED + 2006; // 4.2(d)
	int CallinOverridesRegular 					 = CALLIN_RELATED + 2007; // 4.2(d)
	int RegularOverridesCallin 					 = CALLIN_RELATED + 2008; // 4.2(d)
	int ConflictingCallinAndRegular 			 = CALLIN_RELATED + 2009; // 4.2(d)


	int BaseCallNotSameMethod                    = CALLIN_RELATED + 3001; // 4.3(a)
	int BaseCallDoesntMatchRoleMethodSignature   = CALLIN_RELATED + 3002; // 4.3(a)
	int PotentiallyMissingBaseCall               = CALLIN_RELATED + 3003; // 4.3(b)
	int DefinitelyMissingBaseCall                = CALLIN_RELATED + 3004; // 4.3(b)
	int PotentiallyDuplicateBaseCall             = CALLIN_RELATED + 3005; // 4.3(c)
	int DefinitelyDuplicateBaseCall              = CALLIN_RELATED + 3006; // 4.3(c)
	int MissingBaseCallResult                    = CALLIN_RELATED + 3007; // 4.3(e)
	int FragileCallinBindingBaseType             = CALLIN_RELATED + 3008; // 4.3(e)
	int FragileCallinBindingReferenceType        = CALLIN_RELATED + 3009; // 4.3(e)
	int BaseSuperCallToNonOverriding             = CALLIN_RELATED + 3010; // 4.3(f)
	int BaseSuperCallDecapsulation               = CALLIN_RELATED + 3011; // 4.3(f)
	/** @since 3.12 OT 2.5 */
	int BaseCallArgumentMismatch				 = CALLIN_RELATED + 3012; // "regular" type checking


	int CallinParameterMappingMissingSingatures  = CALLIN_RELATED + 4000; // 4.4(a)
	int IllegalDirectionForCallinResult          = CALLIN_RELATED + 4001; // 4.4(a)
	int TooFewArgumentsInCallin 				 = CALLIN_RELATED + 4002; // 4.4(a)
	int DuplicateCallinParamMapping 			 = CALLIN_RELATED + 4003; // 4.4(a)
	int ResultNotDefinedForVoidMethodCallin      = CALLIN_RELATED + 4004; // 4.4(a)
	int UnmappedRoleParameter 					 = CALLIN_RELATED + 4005; // 4.4(a)
	int IgnoringRoleMethodReturn				 = CALLIN_RELATED + 4006; // 4.4(a)
	int NonReplaceExpressionInReplaceResult      = CALLIN_RELATED + 4007; // 4.4(b)
	int CallinIllegalRoleReturn 				 = CALLIN_RELATED + 4008; // 4.4(b)
	int CallinIncompatibleReturnType			 = CALLIN_RELATED + 4009; // 4.4(b)
	int CallinIncompatibleReturnTypeBaseCall	 = CALLIN_RELATED + 4010; // 4.4(b)
	int CallinMappingResultToOther 				 = CALLIN_RELATED + 4011; // 4.4(b)
	int BaseArgInNonSimpleExpression 			 = CALLIN_RELATED + 4012; // 4.4(b)
	int IllegalBindingDirectionNonReplaceCallin  = CALLIN_RELATED + 4013; // 4.4(c)

	int CallinTypeMismatch                       = CALLIN_RELATED + 5001; // 4.5(d)
	int IncompatibleMappedCallinArgument		 = CALLIN_RELATED + 5002; // 4.5(d)
	int ReturnRequiredInCallinMethodMapping	 	 = CALLIN_RELATED + 5003; // 4.5(d)
	int TypesNotTwowayCompatibleInReplace        = CALLIN_RELATED + 5004; // 4.5(d)


	int CallinDecapsulation 					 = CALLIN_RELATED + 6001; // 4.6
	int CallinInvisiblePrivate                   = CALLIN_RELATED + 6002; // 4.6(??)


	int CallinIncompatibleStatic 				 = CALLIN_RELATED + 7001; // 4.7(b)
	int ReplaceCallinIncompatibleStatic 		 = CALLIN_RELATED + 7002; // 4.7(d)

	int UnknownPrecedence                        = CALLIN_RELATED + 8001; // 4.8
	int IllegalDeepRoleReferenceInPrecedence     = CALLIN_RELATED + 8002; // 4.8
	int PrecedenceInRegularClass                 = CALLIN_RELATED + 8003; // 4.8(a)
	int AfterCallinInNonAfterPrecedence			 = CALLIN_RELATED + 8004; // 4.8(a)
	int NonAfterCallinInAfterPrecedence			 = CALLIN_RELATED + 8005; // 4.8(a)
	int CallinBindingNotFound 					 = CALLIN_RELATED + 8006; // 4.8(b)
	int IllegalEnclosingForCallinName 			 = CALLIN_RELATED + 8007; // 4.8(b)
	int IncompatiblePrecedenceListsOther         = CALLIN_RELATED + 8008; // 4.8(d)
	int IncompatiblePrecedenceListsSymmetric     = CALLIN_RELATED + 8009; // 4.8(d)
	int PrecedenceForOverriding 				 = CALLIN_RELATED + 8010; // 4.8(e)

	int CovariantReturnRequiresTypeParameter     = CALLIN_RELATED + 9001; // 4.9.3(c)

	int DuplicateUseOfTypeVariableInCallin		 = CALLIN_RELATED + 9501; // 4.10(a)

//  ==== ACTIVATION/PREDICATES ====
	int ACTIVATION_RELATED = OTJ_RELATED + 5*OTCHAP;
	int WithinStatementNeedsTeamInstance         = ACTIVATION_RELATED + 2001; // 5.2(a)

	int CheckedExceptionInGuard				     = ACTIVATION_RELATED + 4001; // 5.4(c)
	int PredicateHasNoArguments                  = ACTIVATION_RELATED + 4101; // 5.4.1(a)
	int BasePredicateInUnboundRole               = ACTIVATION_RELATED + 4201; // 5.4.2(f)

	//  ==== API ====
	int API_RELATED = OTJ_RELATED + 6*OTCHAP;
	int RoleClassLiteralLacksTeamInstance        = API_RELATED + 1001; // 6.1(c)
	int ExternalizedRoleClassLiteral             = API_RELATED + 1002; // 6.1(c)

	int OverridingPredefined                     = API_RELATED + 2001; // 6.2

//  ==== ENCAPSULATION ====
	int ENCAPS_RELATED = OTJ_RELATED + 7*OTCHAP;
	int OverridingConfined                       = ENCAPS_RELATED + 2001; // 7.2(a)
	int ArrayOfConfinedNotConform 				 = ENCAPS_RELATED + 2002; // 7.2(b)

//  ==== Joinpoint Queries: ====
	int JP_RELATED = OTJ_RELATED + 8*OTCHAP;
	// Chapter currently not in use

//  ==== Dependent Types: ====
	int DEPENDENT_RELATED = OTJ_RELATED + 9*OTCHAP;
	int AnchorReferenceNotAValue	             = DEPENDENT_RELATED +  210; // 9.2.1
	int AnchorReferenceNotAnObjectRef			 = DEPENDENT_RELATED +  211; // 9.2.1
	int AnchorNotFound                           = DEPENDENT_RELATED +  212; // 9.2.1
	int IncompatibleValueParameter               = DEPENDENT_RELATED +  213; // 9.2.1
	int TypeHasNoValueParamAt                    = DEPENDENT_RELATED +  214; // 9.2.1
	int RebindingTypeVariableAnchor				 = DEPENDENT_RELATED +  215; // 9.2.1(a)
	/**
	 * @since 3.7
	 */
	int ValueTypeArgumentMismatch 				 = DEPENDENT_RELATED +  220; // 9.2.2

//  ==== SYNTAX ====
	int SYNTAX_RELATED = OTJ_RELATED + 10*OTCHAP;
	int OTKeywordInRegularClass                  = SYNTAX_RELATED +  101; // A.0.1
	int InheritedNameIsOTKeyword                 = SYNTAX_RELATED +  201; // A.0.2
	int IllegalOTIdentifier                      = SYNTAX_RELATED +  301; // A.0.3

	int PlayedByInRegularClass                   = SYNTAX_RELATED + 1101; // A.1.1(a)

	int MethodMappingNotInClass					 = SYNTAX_RELATED + 3101; // A.3.1
	int IllegalModifierInCalloutShort            = SYNTAX_RELATED + 3201; // A.3.2
	int WrongModifierInCalloutBinding            = SYNTAX_RELATED + 3202; // A.3.2
	int SyntaxErrorCtorMethodSpec				 = SYNTAX_RELATED + 3203; // A.3.2
	int SyntaxErrorMethodSpecMissingReturnType	 = SYNTAX_RELATED + 3204; // A.3.2
	int IllegalModifierInCallinBinding           = SYNTAX_RELATED + 3301; // A.3.3
	int SyntaxErrorInCallinLabel                 = SYNTAX_RELATED + 3302; // A.3.3
	int CallinReplaceKeyWordNotOptional          = SYNTAX_RELATED + 3303; // A.3.3
	int IllegalMappingRHSTypeParameter   		 = SYNTAX_RELATED + 3304; // 4.3.3
	/**
	 * @since 3.8
	 */
	int IllegalModifierBeforeCallinLabel 		 = SYNTAX_RELATED + 3305; // A.3.3 // FIXME: annotations are still missing from OTJLD A.3.3

	int IllegalModifierInMethodSpecRight         = SYNTAX_RELATED + 3501; // A.3.5

	int SyntaxErrorIllegalDeclaredLifting        = SYNTAX_RELATED + 6001; // A.6(a)

	int MissingPredicateExpression				 = SYNTAX_RELATED + 7001; // A.7.1

	int ValueParamWrongPosition                  = SYNTAX_RELATED + 9001; // A.9(a)

	int SyntaxErrorSingleTypeReferenceExpected   = SYNTAX_RELATED + 9501; // 1.2.4(c) NOTE(SH): don't yet have a section "Expression" in §A.

//  ==== LIMITATIONS: ====
	int LIMITATIONS = OTJ_RELATED + 11*OTCHAP;
	int UnsupportedUseOfGenerics                 = LIMITATIONS +      1;
	int UnresolvedLifting                        = LIMITATIONS +      2;
	int UnsupportedRoleDataflow                  = LIMITATIONS +      3;
	int RoleFileForBinaryTeam                    = LIMITATIONS +      6;
	int MissingRoleInBinaryTeam                  = LIMITATIONS +      7;
	int MissingCopiedRole                        = LIMITATIONS +      8;
	int CorruptBytecode 						 = LIMITATIONS +      9;
	int IncompatibleJRE 						 = LIMITATIONS +     10;
	int IncompleteDependentTypesImplementation   = LIMITATIONS +     11;
	int ExprimentalFeature                       = LIMITATIONS +     12;
	int StaleTSuperRole                          = LIMITATIONS +     13;
	int StaleSubRole	                         = LIMITATIONS +     14;
	int MissingAccessorInBinary                  = LIMITATIONS +     15;
	int RoleFileInBinaryTeam                     = LIMITATIONS +     16;
	int MismatchingRoleParts 					 = LIMITATIONS +     17;
	int InconsistentlyResolvedRole               = LIMITATIONS +     18;
	int CompilationOrderProblem                  = LIMITATIONS +     19;
	int NotGeneratingCallinBinding 				 = LIMITATIONS +     20;
	int SearchingBaseclassTooEarly 				 = LIMITATIONS +     21;
	int StaticRoleFieldMustBeFinal               = LIMITATIONS +     22;
	// move to regular errors:?
	int IllegallyCopiedDefaultCtor 				 = LIMITATIONS +     23;
	/**
	 * @deprecated no longer raised, since 'readonly' is no longer recognized by the grammar
	 */
	@Deprecated
	int ReadonlyNotYetSupported                  = LIMITATIONS +     24;

	int MissingImplementation 					 = LIMITATIONS +     25;
	int TryingToWeaveIntoSystemClass			 = LIMITATIONS +     26;

	int DangerousCallinBinding 					 = LIMITATIONS +     27;

	int UnexpectedAnnotationStructure			 = LIMITATIONS + 	 28;

	int IncompatibleOTJByteCodeVersion			 = LIMITATIONS + 	 29;

	int CallinBindingToInterface				 = LIMITATIONS + 	 30;

	int AbstractStaticMethodCalled 				 = LIMITATIONS +     31; // actually a sign of corrupt byte code

	/** @since 4.3 OT 2.2 */
	int RoleFileMissingTeamDeclaration			 = LIMITATIONS + 	 32;

	/** @since 3.10 OT 2.3 */
	int OtreCannotWeaveIntoJava8				 = LIMITATIONS + 	 33;

	/** @since 3.10 OT 2.3 */
	int IncompatibleWeavingScheme				 = LIMITATIONS + 	 34;

// ==== EXPERIMENTAL: ====
	int EXPERIMENTAL = OTJ_RELATED + 12*OTCHAP;
	int MigrateNonRole 							 = EXPERIMENTAL +     1;
	int MigrateWithinNonFinalTeam 				 = EXPERIMENTAL +     2;
	int MigrateToNonTeam 						 = EXPERIMENTAL +     3;
	int MigrateToWrongTeam 						 = EXPERIMENTAL +     4;
	int MigrateBoundRole                         = EXPERIMENTAL +     5;
	int BaseMigrateNonRole 						 = EXPERIMENTAL +    10;
	int BaseMigrateUnboundRole					 = EXPERIMENTAL +    11;
	int MigrateToWrongBase 						 = EXPERIMENTAL +    12;
//SH}

	/* Local-Variable Type Inference */
	/** @since 3.14 */
	int VarLocalMultipleDeclarators = Syntax + 1500; // ''var'' is not allowed in a compound declaration
	/** @since 3.14 */
	int VarLocalCannotBeArray = Syntax + 1501; // ''var'' is not allowed as an element type of an array
	/** @since 3.14 */
	int VarLocalReferencesItself = Syntax + 1502; // Declaration using ''var'' may not contin references to itself
	/** @since 3.14 */
	int VarLocalWithoutInitizalier = Syntax + 1503; // Cannot use ''var'' on variable without initializer
	/** @since 3.14 */
	int VarLocalInitializedToNull = TypeRelated + 1504; // Variable initialized to ''null'' needs an explicit target-type
	/** @since 3.14 */
	int VarLocalInitializedToVoid = TypeRelated + 1505; // Variable initializer is ''void'' -- cannot infer variable type
	/** @since 3.14 */
	int VarLocalCannotBeArrayInitalizers = TypeRelated + 1506; // Array initializer needs an explicit target-type
	/** @since 3.14 */
	int VarLocalCannotBeLambda = TypeRelated + 1507; // Lambda expression needs an explicit target-type
	/** @since 3.14 */
	int VarLocalCannotBeMethodReference = TypeRelated + 1508; // Method reference needs an explicit target-type
	/** @since 3.14 */
	int VarIsReserved = Syntax + 1509; // ''var'' is not a valid type name
	/** @since 3.14 */
	int VarIsReservedInFuture = Syntax + 1510; // ''var'' should not be used as an type name, since it is a reserved word from source level 10 on
	/** @since 3.14 */
	int VarIsNotAllowedHere = Syntax + 1511; // ''var'' is not allowed here
	/** @since 3.16 */
	int VarCannotBeMixedWithNonVarParams = Syntax + 1512; // ''var'' cannot be mixed with explicit or implicit parameters
	/** @since 3.18
	 * @deprecated preview related error - will be removed
	 * @noreference preview related error */
	int SwitchExpressionsIncompatibleResultExpressionTypes = TypeRelated + 1600;
	/** @since 3.18
	 * @deprecated preview related error - will be removed
	 * @noreference preview related error */
	int SwitchExpressionsEmptySwitchBlock = Internal + 1601;
	/** @since 3.18
	 * @deprecated preview related error - will be removed
	 * @noreference preview related error */
	int SwitchExpressionsNoResultExpression = TypeRelated + 1602;
	/** @since 3.18
	 * @deprecated preview related error - will be removed
	 * @noreference preview related error */
	int SwitchExpressionSwitchLabeledBlockCompletesNormally = Internal + 1603;
	/** @since 3.18
	 * @deprecated preview related error - will be removed
	 * @noreference preview related error */
	int SwitchExpressionLastStatementCompletesNormally = Internal + 1604;
	/** @since 3.18
	 * @deprecated preview related error - will be removed
	 * @noreference preview related error */
	int SwitchExpressionTrailingSwitchLabels = Internal + 1605;
	/** @since 3.18
	 * @deprecated preview related error - will be removed
	 * @noreference preview related error */
	int switchMixedCase = Syntax + 1606;
	/** @since 3.18
	 * @deprecated preview related error - will be removed
	 * @noreference preview related error */
	int SwitchExpressionMissingDefaultCase = Internal + 1607;
	/** @since 3.18
	 * @deprecated preview related error - will be removed
	 * @noreference preview related error */
	int SwitchExpressionBreakMissingValue = Internal + 1610;
	/** @since 3.18
	 * @deprecated preview related error - will be removed
	 * @noreference preview related error */
	int SwitchExpressionMissingEnumConstantCase = Internal + 1611;
	/** @since 3.18
	 * @deprecated preview related error - will be removed
	 * @noreference preview related error */
	int SwitchExpressionIllegalLastStatement = Internal + 1612;

	/* Java14 errors - begin */
	/** @since 3.21  */
	int SwitchExpressionsYieldIncompatibleResultExpressionTypes = TypeRelated + 1700;
	/** @since 3.21  */
	int SwitchExpressionsYieldEmptySwitchBlock = Syntax + 1701;
	/** @since 3.21  */
	int SwitchExpressionsYieldNoResultExpression = Internal + 1702;
	/** @since 3.21  */
	int SwitchExpressionaYieldSwitchLabeledBlockCompletesNormally = Internal + 1703;
	/** @since 3.21  */
	int SwitchExpressionsYieldLastStatementCompletesNormally = Internal + 1704;
	/** @since 3.21  */
	int SwitchExpressionsYieldTrailingSwitchLabels = Internal + 1705;
	/** @since 3.21  */
	int SwitchPreviewMixedCase = Syntax + 1706;
	/** @since 3.21  */
	int SwitchExpressionsYieldMissingDefaultCase = Syntax + 1707;
	/** @since 3.21  */
	int SwitchExpressionsYieldMissingValue = Syntax + 1708;
	/** @since 3.21  */
	int SwitchExpressionsYieldMissingEnumConstantCase = Syntax + 1709;
	/** @since 3.21  */
	int SwitchExpressionsYieldIllegalLastStatement = Internal + 1710;
	/** @since 3.21  */
	int SwitchExpressionsYieldBreakNotAllowed = Syntax + 1711;
	/** @since 3.21  */
	int SwitchExpressionsYieldUnqualifiedMethodWarning = Syntax + 1712;
	/** @since 3.21  */
	int SwitchExpressionsYieldUnqualifiedMethodError = Syntax + 1713;
	/** @since 3.21  */
	int SwitchExpressionsYieldOutsideSwitchExpression = Syntax + 1714;
	/** @since 3.21  */
	int SwitchExpressionsYieldRestrictedGeneralWarning = Internal + 1715;
	/** @since 3.21  */
	int SwitchExpressionsYieldIllegalStatement = Internal + 1716;
	/** @since 3.21  */
	int SwitchExpressionsYieldTypeDeclarationWarning = Internal + 1717;
	/** @since 3.21  */
	int SwitchExpressionsYieldTypeDeclarationError = Internal + 1718;
	/** @since 3.22 */
	int MultiConstantCaseLabelsNotSupported = Syntax + 1719;
	/** @since 3.22*/
	int ArrowInCaseStatementsNotSupported = Syntax + 1720;
	/** @since 3.22 */
	int SwitchExpressionsNotSupported = Syntax + 1721;
	/** @since 3.22 */
	int SwitchExpressionsBreakOutOfSwitchExpression  = Syntax + 1722;
	/** @since 3.22 */
	int SwitchExpressionsContinueOutOfSwitchExpression  = Syntax + 1723;
	/** @since 3.22 */
	int SwitchExpressionsReturnWithinSwitchExpression  = Syntax + 1724;

	/* Java 14 errors end */
	/* Java 15 errors begin */
	/* records - begin */

	/** @since 3.26 */
	int RecordIllegalModifierForInnerRecord = TypeRelated + 1730;
	/** @since 3.26 */
	int RecordIllegalModifierForRecord = TypeRelated + 1731;
	/** @since 3.26
	 * JLS 14 Sec 8.10.1
	 * it is always a compile-time error for a record header to declare a record component with the name
	 * finalize, getClass, hashCode, notify, notifyAll, or toString. */
	int RecordIllegalComponentNameInRecord = TypeRelated + 1732;
	/** @since 3.26
	 */
	int RecordNonStaticFieldDeclarationInRecord = TypeRelated + 1733;
	/** @since 3.26
	 */
	int RecordAccessorMethodHasThrowsClause = TypeRelated + 1734;
	/** @since 3.26
	 */
	int RecordCanonicalConstructorHasThrowsClause = TypeRelated + 1735;
	/** @since 3.26
	 */
	int RecordCanonicalConstructorVisibilityReduced = TypeRelated + 1736;
	/** @since 3.26
	 */
	int RecordMultipleCanonicalConstructors = TypeRelated + 1737;
	/** @since 3.26
	 */
	int RecordCompactConstructorHasReturnStatement = TypeRelated + 1738;
	/** @since 3.26
	 */
	int RecordDuplicateComponent = TypeRelated + 1739;
	/** @since 3.26
	 */
	int RecordIllegalNativeModifierInRecord = TypeRelated + 1740;
	/** @since 3.26
	 */
	int RecordInstanceInitializerBlockInRecord = TypeRelated + 1741;
	/** @since 3.26
	 */
	int RestrictedTypeName = TypeRelated + 1742;
	/** @since 3.26
	 */
	int RecordIllegalAccessorReturnType = TypeRelated + 1743;
	/** @since 3.26
	 */
	int RecordAccessorMethodShouldNotBeGeneric = TypeRelated + 1744;
	/** @since 3.26
	 */
	int RecordAccessorMethodShouldBePublic = TypeRelated + 1745;
	/** @since 3.26
	 */
	int RecordCanonicalConstructorShouldNotBeGeneric = TypeRelated + 1746;
	/** @since 3.26
	 */
	int RecordCanonicalConstructorHasReturnStatement = TypeRelated + 1747;
	/** @since 3.26
	 */
	int RecordCanonicalConstructorHasExplicitConstructorCall = TypeRelated + 1748;
	/** @since 3.26
	 */
	int RecordCompactConstructorHasExplicitConstructorCall = TypeRelated + 1749;
	/** @since 3.26
	 */
	int RecordNestedRecordInherentlyStatic = TypeRelated + 1750;
	/** @since 3.26
	 */
	int RecordAccessorMethodShouldNotBeStatic= TypeRelated + 1751;
	/** @since 3.26
	 */
	int RecordCannotExtendRecord= TypeRelated + 1752;
	/** @since 3.26
	 */
	int RecordComponentCannotBeVoid= TypeRelated + 1753;
	/** @since 3.26
	 */
	int RecordIllegalVararg= TypeRelated + 1754;
	/** @since 3.26
	 */
	int RecordStaticReferenceToOuterLocalVariable= TypeRelated + 1755;
	/** @since 3.26
	 */
	int RecordCannotDefineRecordInLocalType= TypeRelated + 1756;
	/** @since 3.26
	 */
	int RecordComponentsCannotHaveModifiers= TypeRelated + 1757;
	/** @since 3.26
	 */
	int RecordIllegalParameterNameInCanonicalConstructor = TypeRelated + 1758;
	/** @since 3.26
	 */
	int RecordIllegalExplicitFinalFieldAssignInCompactConstructor = TypeRelated + 1759;
	/** @since 3.26
	 */
	int RecordMissingExplicitConstructorCallInNonCanonicalConstructor= TypeRelated + 1760;
	/** @since 3.26
	 */
	int RecordIllegalStaticModifierForLocalClassOrInterface = TypeRelated + 1761;
	/** @since 3.26
	 */
	int RecordIllegalModifierForLocalRecord = TypeRelated + 1762;
	/** @since 3.26
	 */
	int RecordIllegalExtendedDimensionsForRecordComponent = Syntax + Internal + 1763;

	/* records - end */
	/* Local and Nested Static Declarations - Begin */
	/** @since 3.24
	 * @noreference preview feature error */
	int LocalStaticsIllegalVisibilityModifierForInterfaceLocalType = PreviewRelated + 1765;
	/** @since 3.24
	 * @noreference preview feature error */
	int IllegalModifierForLocalEnumDeclaration = PreviewRelated + 1766;
	/* records - end */


	/* instanceof pattern: */
	/** @since 3.22
	 * @deprecated problem no longer generated */
	int PatternVariableNotInScope = PreviewRelated + 1780;
	/** @since 3.26
	 */
	int PatternVariableRedefined = Internal + 1781;
	/** @since 3.26
	 */
	int PatternSubtypeOfExpression = Internal + 1782;
	/** @since 3.26
	 */
	int IllegalModifierForPatternVariable = Internal + 1783;
	/** @since 3.26
	 */
	int PatternVariableRedeclared = Internal + 1784;


	/** @since 3.24
	 * @noreference preview feature error */
	int SealedMissingClassModifier = PreviewRelated + 1850;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedDisAllowedNonSealedModifierInClass = PreviewRelated + 1851;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedSuperClassDoesNotPermit = PreviewRelated + 1852;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedSuperInterfaceDoesNotPermit = PreviewRelated + 1853;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedMissingSealedModifier = PreviewRelated + 1854;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedMissingInterfaceModifier = PreviewRelated + 1855;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedDuplicateTypeInPermits = PreviewRelated + 1856;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedNotDirectSuperClass = PreviewRelated + 1857;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedPermittedTypeOutsideOfModule = PreviewRelated + 1858;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedPermittedTypeOutsideOfPackage = PreviewRelated + 1859;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedSealedTypeMissingPermits = PreviewRelated + 1860;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedInterfaceIsSealedAndNonSealed = PreviewRelated + 1861;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedDisAllowedNonSealedModifierInInterface = PreviewRelated + 1862;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedNotDirectSuperInterface = PreviewRelated + 1863;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedLocalDirectSuperTypeSealed = PreviewRelated + 1864;
	/** @since 3.24
	 * @noreference preview feature error */
	int SealedAnonymousClassCannotExtendSealedType = PreviewRelated + 1865;
	/* Java15 errors - end */

	}
