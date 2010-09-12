/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ProblemReporter.java 23405 2010-02-03 17:02:18Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla - Contribution for bug 239066
 *     Stephan Herrmann  - Contribution for bug 236385
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.problem;

import java.io.CharConversionException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.util.Messages;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.compiler.OTNameUtils;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.BaseAllocationExpression;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.BaseCallMessageSend;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.FieldAccessSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.GuardPredicateDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.LiftingTypeReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.ParameterMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.PotentialLiftExpression;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.PrecedenceDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.ResultReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TSuperMessageSend;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeAnchorReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.WithinStatement;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.PrecedenceBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.MethodSignatureEnhancer;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.Protections;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * OTDT changes:
 * What: many new diagnostics!
 *
 */
public class ProblemReporter extends ProblemHandler {

	public ReferenceContext referenceContext;
	private Scanner positionScanner;
	private final static byte
	  // TYPE_ACCESS = 0x0,
	  FIELD_ACCESS = 0x4,
	  CONSTRUCTOR_ACCESS = 0x8,
	  METHOD_ACCESS = 0xC;

public ProblemReporter(IErrorHandlingPolicy policy, CompilerOptions options, IProblemFactory problemFactory) {
	super(policy, options, problemFactory);
}

//{ObjectTeams: support for passing a rechecker:
public ProblemReporter setRechecker(IProblemRechecker rechecker) {
	this.rechecker = rechecker;
	return this;
}
// SH}

private static int getElaborationId (int leadProblemId, byte elaborationVariant) {
	return leadProblemId << 8 | elaborationVariant; // leadProblemId comes into the higher order bytes
}
public static int getIrritant(int problemID) {
	switch(problemID){

		case IProblem.MaskedCatch :
			return CompilerOptions.MaskedCatchBlock;

		case IProblem.UnusedImport :
			return CompilerOptions.UnusedImport;

		case IProblem.MethodButWithConstructorName :
			return CompilerOptions.MethodWithConstructorName;

		case IProblem.OverridingNonVisibleMethod :
			return CompilerOptions.OverriddenPackageDefaultMethod;

		case IProblem.IncompatibleReturnTypeForNonInheritedInterfaceMethod :
		case IProblem.IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod :
			return CompilerOptions.IncompatibleNonInheritedInterfaceMethod;

		case IProblem.OverridingDeprecatedMethod :
		case IProblem.UsingDeprecatedType :
		case IProblem.UsingDeprecatedMethod :
		case IProblem.UsingDeprecatedConstructor :
		case IProblem.UsingDeprecatedField :
			return CompilerOptions.UsingDeprecatedAPI;

		case IProblem.LocalVariableIsNeverUsed :
			return CompilerOptions.UnusedLocalVariable;

		case IProblem.ArgumentIsNeverUsed :
			return CompilerOptions.UnusedArgument;

		case IProblem.NoImplicitStringConversionForCharArrayExpression :
			return CompilerOptions.NoImplicitStringConversion;

		case IProblem.NeedToEmulateFieldReadAccess :
		case IProblem.NeedToEmulateFieldWriteAccess :
		case IProblem.NeedToEmulateMethodAccess :
		case IProblem.NeedToEmulateConstructorAccess :
			return CompilerOptions.AccessEmulation;

		case IProblem.NonExternalizedStringLiteral :
		case IProblem.UnnecessaryNLSTag :
			return CompilerOptions.NonExternalizedString;

		case IProblem.UseAssertAsAnIdentifier :
			return CompilerOptions.AssertUsedAsAnIdentifier;

		case IProblem.UseEnumAsAnIdentifier :
			return CompilerOptions.EnumUsedAsAnIdentifier;

		case IProblem.NonStaticAccessToStaticMethod :
		case IProblem.NonStaticAccessToStaticField :
			return CompilerOptions.NonStaticAccessToStatic;

		case IProblem.IndirectAccessToStaticMethod :
		case IProblem.IndirectAccessToStaticField :
		case IProblem.IndirectAccessToStaticType :
			return CompilerOptions.IndirectStaticAccess;

		case IProblem.AssignmentHasNoEffect:
			return CompilerOptions.NoEffectAssignment;

		case IProblem.UnusedPrivateConstructor:
		case IProblem.UnusedPrivateMethod:
		case IProblem.UnusedPrivateField:
		case IProblem.UnusedPrivateType:
			return CompilerOptions.UnusedPrivateMember;

		case IProblem.LocalVariableHidingLocalVariable:
		case IProblem.LocalVariableHidingField:
		case IProblem.ArgumentHidingLocalVariable:
		case IProblem.ArgumentHidingField:
			return CompilerOptions.LocalVariableHiding;

		case IProblem.FieldHidingLocalVariable:
		case IProblem.FieldHidingField:
			return CompilerOptions.FieldHiding;

		case IProblem.TypeParameterHidingType:
		case IProblem.TypeHidingTypeParameterFromType:
		case IProblem.TypeHidingTypeParameterFromMethod:
		case IProblem.TypeHidingType:
			return CompilerOptions.TypeHiding;

		case IProblem.PossibleAccidentalBooleanAssignment:
			return CompilerOptions.AccidentalBooleanAssign;

		case IProblem.SuperfluousSemicolon:
		case IProblem.EmptyControlFlowStatement:
			return CompilerOptions.EmptyStatement;

		case IProblem.UndocumentedEmptyBlock:
			return CompilerOptions.UndocumentedEmptyBlock;

		case IProblem.UnnecessaryCast:
		case IProblem.UnnecessaryInstanceof:
			return CompilerOptions.UnnecessaryTypeCheck;

		case IProblem.FinallyMustCompleteNormally:
			return CompilerOptions.FinallyBlockNotCompleting;

		case IProblem.UnusedMethodDeclaredThrownException:
		case IProblem.UnusedConstructorDeclaredThrownException:
			return CompilerOptions.UnusedDeclaredThrownException;

		case IProblem.UnqualifiedFieldAccess:
			return CompilerOptions.UnqualifiedFieldAccess;

		case IProblem.UnnecessaryElse:
			return CompilerOptions.UnnecessaryElse;

		case IProblem.UnsafeRawConstructorInvocation:
		case IProblem.UnsafeRawMethodInvocation:
		case IProblem.UnsafeTypeConversion:
		case IProblem.UnsafeRawFieldAssignment:
		case IProblem.UnsafeGenericCast:
		case IProblem.UnsafeReturnTypeOverride:
		case IProblem.UnsafeRawGenericMethodInvocation:
		case IProblem.UnsafeRawGenericConstructorInvocation:
		case IProblem.UnsafeGenericArrayForVarargs:
			return CompilerOptions.UncheckedTypeOperation;

		case IProblem.RawTypeReference:
			return CompilerOptions.RawTypeReference;

		case IProblem.MissingOverrideAnnotation:
		case IProblem.MissingOverrideAnnotationForInterfaceMethodImplementation:
			return CompilerOptions.MissingOverrideAnnotation;

		case IProblem.FieldMissingDeprecatedAnnotation:
		case IProblem.MethodMissingDeprecatedAnnotation:
		case IProblem.TypeMissingDeprecatedAnnotation:
			return CompilerOptions.MissingDeprecatedAnnotation;

		case IProblem.FinalBoundForTypeVariable:
		    return CompilerOptions.FinalParameterBound;

		case IProblem.MissingSerialVersion:
			return CompilerOptions.MissingSerialVersion;

		case IProblem.ForbiddenReference:
			return CompilerOptions.ForbiddenReference;

		case IProblem.DiscouragedReference:
			return CompilerOptions.DiscouragedReference;

		case IProblem.MethodVarargsArgumentNeedCast :
		case IProblem.ConstructorVarargsArgumentNeedCast :
			return CompilerOptions.VarargsArgumentNeedCast;

		case IProblem.NullLocalVariableReference:
			return CompilerOptions.NullReference;

		case IProblem.PotentialNullLocalVariableReference:
			return CompilerOptions.PotentialNullReference;

		case IProblem.RedundantLocalVariableNullAssignment:
		case IProblem.RedundantNullCheckOnNonNullLocalVariable:
		case IProblem.RedundantNullCheckOnNullLocalVariable:
		case IProblem.NonNullLocalVariableComparisonYieldsFalse:
		case IProblem.NullLocalVariableComparisonYieldsFalse:
		case IProblem.NullLocalVariableInstanceofYieldsFalse:
			return CompilerOptions.RedundantNullCheck;

		case IProblem.BoxingConversion :
		case IProblem.UnboxingConversion :
			return CompilerOptions.AutoBoxing;

		case IProblem.MissingEnumConstantCase :
			return CompilerOptions.IncompleteEnumSwitch;

		case IProblem.AnnotationTypeUsedAsSuperInterface :
			return CompilerOptions.AnnotationSuperInterface;

		case IProblem.UnhandledWarningToken :
			return CompilerOptions.UnhandledWarningToken;

		case IProblem.UnusedWarningToken :
			return CompilerOptions.UnusedWarningToken;

		case IProblem.UnusedLabel :
			return CompilerOptions.UnusedLabel;

		case IProblem.JavadocUnexpectedTag:
		case IProblem.JavadocDuplicateTag:
		case IProblem.JavadocDuplicateReturnTag:
		case IProblem.JavadocInvalidThrowsClass:
		case IProblem.JavadocInvalidSeeReference:
		case IProblem.JavadocInvalidParamTagName:
		case IProblem.JavadocInvalidParamTagTypeParameter:
		case IProblem.JavadocMalformedSeeReference:
		case IProblem.JavadocInvalidSeeHref:
		case IProblem.JavadocInvalidSeeArgs:
		case IProblem.JavadocInvalidTag:
		case IProblem.JavadocUnterminatedInlineTag:
		case IProblem.JavadocMissingHashCharacter:
		case IProblem.JavadocEmptyReturnTag:
		case IProblem.JavadocUnexpectedText:
		case IProblem.JavadocInvalidParamName:
		case IProblem.JavadocDuplicateParamName:
		case IProblem.JavadocMissingParamName:
		case IProblem.JavadocMissingIdentifier:
		case IProblem.JavadocInvalidMemberTypeQualification:
		case IProblem.JavadocInvalidThrowsClassName:
		case IProblem.JavadocDuplicateThrowsClassName:
		case IProblem.JavadocMissingThrowsClassName:
		case IProblem.JavadocMissingSeeReference:
		case IProblem.JavadocInvalidValueReference:
		case IProblem.JavadocUndefinedField:
		case IProblem.JavadocAmbiguousField:
		case IProblem.JavadocUndefinedConstructor:
		case IProblem.JavadocAmbiguousConstructor:
		case IProblem.JavadocUndefinedMethod:
		case IProblem.JavadocAmbiguousMethod:
		case IProblem.JavadocAmbiguousMethodReference:
		case IProblem.JavadocParameterMismatch:
		case IProblem.JavadocUndefinedType:
		case IProblem.JavadocAmbiguousType:
		case IProblem.JavadocInternalTypeNameProvided:
		case IProblem.JavadocNoMessageSendOnArrayType:
		case IProblem.JavadocNoMessageSendOnBaseType:
		case IProblem.JavadocInheritedMethodHidesEnclosingName:
		case IProblem.JavadocInheritedFieldHidesEnclosingName:
		case IProblem.JavadocInheritedNameHidesEnclosingTypeName:
		case IProblem.JavadocNonStaticTypeFromStaticInvocation:
		case IProblem.JavadocGenericMethodTypeArgumentMismatch:
		case IProblem.JavadocNonGenericMethod:
		case IProblem.JavadocIncorrectArityForParameterizedMethod:
		case IProblem.JavadocParameterizedMethodArgumentTypeMismatch:
		case IProblem.JavadocTypeArgumentsForRawGenericMethod:
		case IProblem.JavadocGenericConstructorTypeArgumentMismatch:
		case IProblem.JavadocNonGenericConstructor:
		case IProblem.JavadocIncorrectArityForParameterizedConstructor:
		case IProblem.JavadocParameterizedConstructorArgumentTypeMismatch:
		case IProblem.JavadocTypeArgumentsForRawGenericConstructor:
		case IProblem.JavadocNotVisibleField:
		case IProblem.JavadocNotVisibleConstructor:
		case IProblem.JavadocNotVisibleMethod:
		case IProblem.JavadocNotVisibleType:
		case IProblem.JavadocUsingDeprecatedField:
		case IProblem.JavadocUsingDeprecatedConstructor:
		case IProblem.JavadocUsingDeprecatedMethod:
		case IProblem.JavadocUsingDeprecatedType:
		case IProblem.JavadocHiddenReference:
		case IProblem.JavadocMissingTagDescription:
		case IProblem.JavadocInvalidSeeUrlReference:
			return CompilerOptions.InvalidJavadoc;

		case IProblem.JavadocMissingParamTag:
		case IProblem.JavadocMissingReturnTag:
		case IProblem.JavadocMissingThrowsTag:
			return CompilerOptions.MissingJavadocTags;

		case IProblem.JavadocMissing:
			return CompilerOptions.MissingJavadocComments;

		case IProblem.ParameterAssignment:
			return CompilerOptions.ParameterAssignment;

		case IProblem.FallthroughCase:
			return CompilerOptions.FallthroughCase;

		case IProblem.OverridingMethodWithoutSuperInvocation:
			return CompilerOptions.OverridingMethodWithoutSuperInvocation;

		case IProblem.UnusedTypeArgumentsForMethodInvocation:
		case IProblem.UnusedTypeArgumentsForConstructorInvocation:
			return CompilerOptions.UnusedTypeArguments;

		case IProblem.RedundantSuperinterface:
			return CompilerOptions.RedundantSuperinterface;

		case IProblem.ComparingIdentical:
			return CompilerOptions.ComparingIdentical;
			
		case IProblem.MissingSynchronizedModifierInInheritedMethod:
			return CompilerOptions.MissingSynchronizedModifierInInheritedMethod;

		case IProblem.ShouldImplementHashcode:
			return CompilerOptions.ShouldImplementHashcode;
			
		case IProblem.DeadCode:
			return CompilerOptions.DeadCode;
			
		case IProblem.Task :
			return CompilerOptions.Tasks;

		case IProblem.UnusedObjectAllocation:
			return CompilerOptions.UnusedObjectAllocation;
//{ObjectTeams:
		case IProblem.DeprecatedBaseclass:
		case IProblem.CallinToDeprecated:
			return CompilerOptions.AdaptingDeprecated;
			
		case IProblem.DefinitelyMissingBaseCall:
		case IProblem.DefinitelyDuplicateBaseCall:
		case IProblem.PotentiallyMissingBaseCall:
		case IProblem.PotentiallyDuplicateBaseCall:
			return CompilerOptions.NotExactlyOneBasecall;
		case IProblem.BaseclassIsEnclosing:
		case IProblem.BaseclassCircularity:
			return CompilerOptions.BaseclassCycle;
		case IProblem.LiftCtorArgNotAllocation:
		case IProblem.InstantiatingSupercededRole:
		case IProblem.RoleConstructorHiddenByLiftingConstructor:
			return CompilerOptions.UnsafeRoleInstantiation;

		case IProblem.FragileCallinBindingReferenceType:
		case IProblem.FragileCallinBindingBaseType:
			return CompilerOptions.FragileCallin;

		case IProblem.RoleBindingPotentiallyAmbiguous:
			return CompilerOptions.PotentialAmbiguousPlayedBy;
		case IProblem.CallinDespiteBindingAmbiguity:
			return CompilerOptions.DefiniteBindingAmbiguity;
		case IProblem.AbstractPotentiallyRelevantRole:
			return CompilerOptions.AbstractPotentialRelevantRole;

		case IProblem.MissingOverrideAnnotationForRole:
			return CompilerOptions.MissingOverrideAnnotation;

		case IProblem.Decapsulation:
		case IProblem.DecapsulationShort:
		case IProblem.DecapsulationField:
		case IProblem.DecapsulationFieldReference:
		case IProblem.DecapsulationMessageSend:
		case IProblem.CallinDecapsulation:
		case IProblem.BaseclassDecapsulation:
		case IProblem.BaseclassDecapsulationFinal:
		case IProblem.BaseclassDecapsulationForcedExport:
		case IProblem.DecapsulationBaseCtor:
		case IProblem.AdaptedPluginAccess: // not a real error but shouldn't disturb processing
		case IProblem.BaseSuperCallDecapsulation:
			return CompilerOptions.Decapsulation;

		case IProblem.RegularlyImportedBaseclass:
		case IProblem.IllegalBaseImport:
		case IProblem.IllegalBaseImportNoAspectBinding:
		case IProblem.QualifiedReferenceToBaseclass:
			return CompilerOptions.BindingConventions;

		case IProblem.AddingInferredCalloutForInherited:
		case IProblem.UsingInferredCalloutForMessageSend:
		case IProblem.UsingCalloutToFieldForAssignment:
		case IProblem.UsingCalloutToFieldForFieldRead:
			return CompilerOptions.AddingInferredCallout;

		case IProblem.DeprecatedPathSyntax:
			return CompilerOptions.DeprecatedPathSyntax;

		case IProblem.TryingToWeaveIntoSystemClass:
			return CompilerOptions.WeaveIntoSystemClass;

		case IProblem.DangerousCallinBinding:
			return CompilerOptions.DangerousCallin;
			
		case IProblem.IgnoringRoleMethodReturn:
			return CompilerOptions.IgnoringRoleReturn;

		case IProblem.OverridingFinalRole:
			return CompilerOptions.OverridingFinalRole;

		case IProblem.CheckedExceptionInGuard:
			return CompilerOptions.ExceptionInGuard;

		case IProblem.AmbiguousUpcastOrLowering:
			return CompilerOptions.AmbiguousLowering;

		case IProblem.JavadocMissingRoleTag:
			return CompilerOptions.MissingJavadocTags;

		case IProblem.JavadocRoleTagNotRole:
		case IProblem.JavadocRoleTagInlineRole:
		case IProblem.JavadocRoleTagNotRoleFile:
		case IProblem.JavadocRoleTagInRegular:
			return CompilerOptions.InvalidJavadoc;
// SH}
	}
	return 0;
}
/**
 * Compute problem category ID based on problem ID
 * @param problemID
 * @return a category ID
 * @see CategorizedProblem
 */
public static int getProblemCategory(int severity, int problemID) {
	categorizeOnIrritant: {
		// fatal problems even if optional are all falling into same category (not irritant based)
		if ((severity & ProblemSeverities.Fatal) != 0)
			break categorizeOnIrritant;
		int irritant = getIrritant(problemID);
		switch (irritant) {
			case CompilerOptions.MethodWithConstructorName :
			case CompilerOptions.AccessEmulation :
			case CompilerOptions.AssertUsedAsAnIdentifier :
			case CompilerOptions.NonStaticAccessToStatic :
			case CompilerOptions.UnqualifiedFieldAccess :
			case CompilerOptions.UndocumentedEmptyBlock :
			case CompilerOptions.IndirectStaticAccess :
			case CompilerOptions.FinalParameterBound :
			case CompilerOptions.EnumUsedAsAnIdentifier :
			case CompilerOptions.AnnotationSuperInterface :
			case CompilerOptions.AutoBoxing :
			case CompilerOptions.MissingOverrideAnnotation :
			case CompilerOptions.MissingDeprecatedAnnotation :
			case CompilerOptions.ParameterAssignment :
				return CategorizedProblem.CAT_CODE_STYLE;

			case CompilerOptions.MaskedCatchBlock :
			case CompilerOptions.NoImplicitStringConversion :
			case CompilerOptions.NoEffectAssignment :
			case CompilerOptions.AccidentalBooleanAssign :
			case CompilerOptions.EmptyStatement :
			case CompilerOptions.FinallyBlockNotCompleting :
			case CompilerOptions.MissingSerialVersion :
			case CompilerOptions.VarargsArgumentNeedCast :
			case CompilerOptions.NullReference :
			case CompilerOptions.PotentialNullReference :
			case CompilerOptions.RedundantNullCheck :
			case CompilerOptions.IncompleteEnumSwitch :
			case CompilerOptions.FallthroughCase :
			case CompilerOptions.OverridingMethodWithoutSuperInvocation :
			case CompilerOptions.ComparingIdentical :
			case CompilerOptions.MissingSynchronizedModifierInInheritedMethod :
			case CompilerOptions.ShouldImplementHashcode :
			case CompilerOptions.DeadCode :
			case CompilerOptions.UnusedObjectAllocation :
				return CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM;
			
			case CompilerOptions.OverriddenPackageDefaultMethod :
			case CompilerOptions.IncompatibleNonInheritedInterfaceMethod :
			case CompilerOptions.LocalVariableHiding :
			case CompilerOptions.FieldHiding :
			case CompilerOptions.TypeHiding :
				return CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT;

			case CompilerOptions.UnusedLocalVariable :
			case CompilerOptions.UnusedArgument :
			case CompilerOptions.UnusedImport :
			case CompilerOptions.UnusedPrivateMember :
			case CompilerOptions.UnusedDeclaredThrownException :
			case CompilerOptions.UnnecessaryTypeCheck :
			case CompilerOptions.UnnecessaryElse :
			case CompilerOptions.UnhandledWarningToken :
			case CompilerOptions.UnusedWarningToken :
			case CompilerOptions.UnusedLabel :
			case CompilerOptions.RedundantSuperinterface :	
				return CategorizedProblem.CAT_UNNECESSARY_CODE;

			case CompilerOptions.UsingDeprecatedAPI :
				return CategorizedProblem.CAT_DEPRECATION;

			case CompilerOptions.NonExternalizedString :
				return CategorizedProblem.CAT_NLS;

			case CompilerOptions.Task :
				return CategorizedProblem.CAT_UNSPECIFIED; // TODO may want to improve
			
			case CompilerOptions.MissingJavadocComments :
			case CompilerOptions.MissingJavadocTags :
			case CompilerOptions.InvalidJavadoc :
			case CompilerOptions.InvalidJavadoc|CompilerOptions.UsingDeprecatedAPI :
				return CategorizedProblem.CAT_JAVADOC;

			case CompilerOptions.UncheckedTypeOperation :
			case CompilerOptions.RawTypeReference :
				return CategorizedProblem.CAT_UNCHECKED_RAW;
			
			case CompilerOptions.ForbiddenReference :
			case CompilerOptions.DiscouragedReference :
				return CategorizedProblem.CAT_RESTRICTION;
			
//{ObjectTeams:
			case CompilerOptions.NotExactlyOneBasecall:
			case CompilerOptions.BaseclassCycle:
			case CompilerOptions.UnsafeRoleInstantiation:
			case CompilerOptions.FragileCallin:
			case CompilerOptions.PotentialAmbiguousPlayedBy:
			case CompilerOptions.AbstractPotentialRelevantRole:
			case CompilerOptions.ExceptionInGuard:
			case CompilerOptions.AmbiguousLowering:
				return CategorizedProblem.CAT_CODE_STYLE;
			case CompilerOptions.Decapsulation:
			case CompilerOptions.OverridingFinalRole:
				return CategorizedProblem.CAT_RESTRICTION;
			case CompilerOptions.BindingConventions:
				return CategorizedProblem.CAT_CODE_STYLE;
			case CompilerOptions.AddingInferredCallout:
				return CategorizedProblem.CAT_CODE_STYLE;
			case CompilerOptions.DeprecatedPathSyntax:
				return CategorizedProblem.CAT_CODE_STYLE;
			case CompilerOptions.DefiniteBindingAmbiguity:
			case CompilerOptions.WeaveIntoSystemClass:
			case CompilerOptions.DangerousCallin:
			case CompilerOptions.IgnoringRoleReturn:
				return CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM;
			case CompilerOptions.AdaptingDeprecated :
				return CategorizedProblem.CAT_DEPRECATION;
// SH}
			default:
				break categorizeOnIrritant;
		}
	}
	// categorize fatal problems per ID
	switch (problemID) {
		case IProblem.IsClassPathCorrect :
		case IProblem.CorruptedSignature :
			return CategorizedProblem.CAT_BUILDPATH;
//{ObjectTeams:
		case IProblem.BaseImportFromSplitPackage :
		case IProblem.BaseImportFromSplitPackagePlural :
			return CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM;
// SH}

		default :
			if ((problemID & IProblem.Syntax) != 0)
				return CategorizedProblem.CAT_SYNTAX;
			if ((problemID & IProblem.ImportRelated) != 0)
				return CategorizedProblem.CAT_IMPORT;
			if ((problemID & IProblem.TypeRelated) != 0)
				return CategorizedProblem.CAT_TYPE;
			if ((problemID & (IProblem.FieldRelated|IProblem.MethodRelated|IProblem.ConstructorRelated)) != 0)
				return CategorizedProblem.CAT_MEMBER;
//{ObjectTeams:
			// more 'types':
			if (problemID >= IProblem.TEAM_RELATED && problemID < IProblem.CALLOUT_RELATED)
				return CategorizedProblem.CAT_TYPE;
			if (problemID >= IProblem.DEPENDENT_RELATED && problemID < IProblem.SYNTAX_RELATED)
				return CategorizedProblem.CAT_TYPE;
			// more 'members':
			if (problemID >= IProblem.CALLOUT_RELATED && problemID < IProblem.ACTIVATION_RELATED)
				return CategorizedProblem.CAT_MEMBER;
			// more syntax errors
			if (problemID >= IProblem.SYNTAX_RELATED && problemID < IProblem.LIMITATIONS)
				return CategorizedProblem.CAT_SYNTAX;
// SH}
	}
	return CategorizedProblem.CAT_INTERNAL;
}
public void abortDueToInternalError(String errorMessage) {
	this.abortDueToInternalError(errorMessage, null);
}
public void abortDueToInternalError(String errorMessage, ASTNode location) {
//{ObjectTeams: mark method as erroneous:
	if (location instanceof AbstractMethodDeclaration) {
		AbstractMethodDeclaration mdecl = (AbstractMethodDeclaration)location;
		if (mdecl.binding != null)
			mdecl.binding.bytecodeMissing = true;
	}
// SH}
	String[] arguments = new String[] {errorMessage};
	this.handle(
		IProblem.Unclassified,
		arguments,
		arguments,
		ProblemSeverities.Error | ProblemSeverities.Abort | ProblemSeverities.Fatal,
		location == null ? 0 : location.sourceStart,
		location == null ? 0 : location.sourceEnd);
}
public void abstractMethodCannotBeOverridden(SourceTypeBinding type, MethodBinding concreteMethod) {

	this.handle(
		// %1 must be abstract since it cannot override the inherited package-private abstract method %2
		IProblem.AbstractMethodCannotBeOverridden,
		new String[] {
			new String(type.sourceName()),
			new String(
					CharOperation.concat(
						concreteMethod.declaringClass.readableName(),
						concreteMethod.readableName(),
						'.'))},
		new String[] {
			new String(type.sourceName()),
			new String(
					CharOperation.concat(
						concreteMethod.declaringClass.shortReadableName(),
						concreteMethod.shortReadableName(),
						'.'))},
		type.sourceStart(),
		type.sourceEnd());
}
public void abstractMethodInAbstractClass(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {
	if (type.isEnum() && type.isLocalType()) {
		FieldBinding field = type.scope.enclosingMethodScope().initializedField;
		FieldDeclaration decl = field.sourceField();
		String[] arguments = new String[] {new String(decl.name), new String(methodDecl.selector)};
		this.handle(
			IProblem.AbstractMethodInEnum,
			arguments,
			arguments,
			methodDecl.sourceStart,
			methodDecl.sourceEnd);
	} else {
		String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
		this.handle(
			IProblem.AbstractMethodInAbstractClass,
			arguments,
			arguments,
			methodDecl.sourceStart,
			methodDecl.sourceEnd);
	}
}
public void abstractMethodInConcreteClass(SourceTypeBinding type) {
	if (type.isEnum() && type.isLocalType()) {
		FieldBinding field = type.scope.enclosingMethodScope().initializedField;
		FieldDeclaration decl = field.sourceField();
		String[] arguments = new String[] {new String(decl.name)};
		this.handle(
			IProblem.EnumConstantCannotDefineAbstractMethod,
			arguments,
			arguments,
			decl.sourceStart(),
			decl.sourceEnd());
	} else {
		String[] arguments = new String[] {new String(type.sourceName())};
		this.handle(
			IProblem.AbstractMethodsInConcreteClass,
			arguments,
			arguments,
			type.sourceStart(),
			type.sourceEnd());
	}
}
public void abstractMethodMustBeImplemented(SourceTypeBinding type, MethodBinding abstractMethod) {
	if (type.isEnum() && type.isLocalType()) {
		FieldBinding field = type.scope.enclosingMethodScope().initializedField;
		FieldDeclaration decl = field.sourceField();
		this.handle(
			// Must implement the inherited abstract method %1
			// 8.4.3 - Every non-abstract subclass of an abstract type, A, must provide a concrete implementation of all of A's methods.
			IProblem.EnumConstantMustImplementAbstractMethod,
			new String[] {
			        new String(abstractMethod.selector),
			        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, false),
			        new String(decl.name),
			},
			new String[] {
			        new String(abstractMethod.selector),
			        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, true),
			        new String(decl.name),
			},
			decl.sourceStart(),
			decl.sourceEnd());
	} else {
		this.handle(
			// Must implement the inherited abstract method %1
			// 8.4.3 - Every non-abstract subclass of an abstract type, A, must provide a concrete implementation of all of A's methods.
			IProblem.AbstractMethodMustBeImplemented,
			new String[] {
			        new String(abstractMethod.selector),
			        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, false),
			        new String(abstractMethod.declaringClass.readableName()),
			        new String(type.readableName()),
			},
			new String[] {
			        new String(abstractMethod.selector),
			        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, true),
			        new String(abstractMethod.declaringClass.shortReadableName()),
			        new String(type.shortReadableName()),
			},
			type.sourceStart(),
			type.sourceEnd());
	}
}
public void abstractMethodMustBeImplemented(SourceTypeBinding type, MethodBinding abstractMethod, MethodBinding concreteMethod) {
	this.handle(
		// Must implement the inherited abstract method %1
		// 8.4.3 - Every non-abstract subclass of an abstract type, A, must provide a concrete implementation of all of A's methods.
		IProblem.AbstractMethodMustBeImplementedOverConcreteMethod,
		new String[] {
		        new String(abstractMethod.selector),
		        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, false),
		        new String(abstractMethod.declaringClass.readableName()),
		        new String(type.readableName()),
		        new String(concreteMethod.selector),
		        typesAsString(concreteMethod.isVarargs(), concreteMethod.parameters, false),
		        new String(concreteMethod.declaringClass.readableName()),
		},
		new String[] {
		        new String(abstractMethod.selector),
		        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, true),
		        new String(abstractMethod.declaringClass.shortReadableName()),
		        new String(type.shortReadableName()),
		        new String(concreteMethod.selector),
		        typesAsString(concreteMethod.isVarargs(), concreteMethod.parameters, true),
		        new String(concreteMethod.declaringClass.shortReadableName()),
		},
		type.sourceStart(),
		type.sourceEnd());
}
public void abstractMethodNeedingNoBody(AbstractMethodDeclaration method) {
	this.handle(
		IProblem.BodyForAbstractMethod,
		NoArgument,
		NoArgument,
		method.sourceStart,
		method.sourceEnd,
		method,
		method.compilationResult());
}
public void alreadyDefinedLabel(char[] labelName, ASTNode location) {
	String[] arguments = new String[] {new String(labelName)};
	this.handle(
		IProblem.DuplicateLabel,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void annotationCannotOverrideMethod(MethodBinding overrideMethod, MethodBinding inheritedMethod) {
	ASTNode location = overrideMethod.sourceMethod();
	this.handle(
		IProblem.AnnotationCannotOverrideMethod,
		new String[] {
				new String(overrideMethod.declaringClass.readableName()),
				new String(inheritedMethod.declaringClass.readableName()),
				new String(inheritedMethod.selector),
				typesAsString(inheritedMethod.isVarargs(), inheritedMethod.parameters, false)},
		new String[] {
				new String(overrideMethod.declaringClass.shortReadableName()),
				new String(inheritedMethod.declaringClass.shortReadableName()),
				new String(inheritedMethod.selector),
				typesAsString(inheritedMethod.isVarargs(), inheritedMethod.parameters, true)},
		location.sourceStart,
		location.sourceEnd);
}
public void annotationCircularity(TypeBinding sourceType, TypeBinding otherType, TypeReference reference) {
	if (sourceType == otherType)
		this.handle(
			IProblem.AnnotationCircularitySelfReference,
			new String[] {new String(sourceType.readableName())},
			new String[] {new String(sourceType.shortReadableName())},
			reference.sourceStart,
			reference.sourceEnd);
	else
		this.handle(
			IProblem.AnnotationCircularity,
			new String[] {new String(sourceType.readableName()), new String(otherType.readableName())},
			new String[] {new String(sourceType.shortReadableName()), new String(otherType.shortReadableName())},
			reference.sourceStart,
			reference.sourceEnd);
}
public void annotationMembersCannotHaveParameters(AnnotationMethodDeclaration annotationMethodDeclaration) {
	this.handle(
		IProblem.AnnotationMembersCannotHaveParameters,
		NoArgument,
		NoArgument,
		annotationMethodDeclaration.sourceStart,
		annotationMethodDeclaration.sourceEnd);
}
public void annotationMembersCannotHaveTypeParameters(AnnotationMethodDeclaration annotationMethodDeclaration) {
	this.handle(
		IProblem.AnnotationMembersCannotHaveTypeParameters,
		NoArgument,
		NoArgument,
		annotationMethodDeclaration.sourceStart,
		annotationMethodDeclaration.sourceEnd);
}
public void annotationTypeDeclarationCannotHaveConstructor(ConstructorDeclaration constructorDeclaration) {
	this.handle(
		IProblem.AnnotationTypeDeclarationCannotHaveConstructor,
		NoArgument,
		NoArgument,
		constructorDeclaration.sourceStart,
		constructorDeclaration.sourceEnd);
}
public void annotationTypeDeclarationCannotHaveSuperclass(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.AnnotationTypeDeclarationCannotHaveSuperclass,
		NoArgument,
		NoArgument,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}
public void annotationTypeDeclarationCannotHaveSuperinterfaces(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.AnnotationTypeDeclarationCannotHaveSuperinterfaces,
		NoArgument,
		NoArgument,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}
public void annotationTypeUsedAsSuperinterface(SourceTypeBinding type, TypeReference superInterfaceRef, ReferenceBinding superType) {
	this.handle(
		IProblem.AnnotationTypeUsedAsSuperInterface,
		new String[] {new String(superType.readableName()), new String(type.sourceName())},
		new String[] {new String(superType.shortReadableName()), new String(type.sourceName())},
		superInterfaceRef.sourceStart,
		superInterfaceRef.sourceEnd);
}
public void annotationValueMustBeAnnotation(TypeBinding annotationType, char[] name, Expression value, TypeBinding expectedType) {
	String str = new String(name);
	this.handle(
		IProblem.AnnotationValueMustBeAnnotation,
		new String[] { new String(annotationType.readableName()), str, new String(expectedType.readableName()),  },
		new String[] { new String(annotationType.shortReadableName()), str, new String(expectedType.readableName()), },
		value.sourceStart,
		value.sourceEnd);
}
public void annotationValueMustBeArrayInitializer(TypeBinding annotationType, char[] name, Expression value) {
	String str = new String(name);
	this.handle(
    	IProblem.AnnotationValueMustBeArrayInitializer,
		new String[] { new String(annotationType.readableName()), str },
		new String[] { new String(annotationType.shortReadableName()), str},
    	value.sourceStart,
    	value.sourceEnd);
}
public void annotationValueMustBeClassLiteral(TypeBinding annotationType, char[] name, Expression value) {
	String str = new String(name);
	this.handle(
		IProblem.AnnotationValueMustBeClassLiteral,
		new String[] { new String(annotationType.readableName()), str },
		new String[] { new String(annotationType.shortReadableName()), str},
		value.sourceStart,
		value.sourceEnd);
}
public void annotationValueMustBeConstant(TypeBinding annotationType, char[] name, Expression value, boolean isEnum) {
	String str = new String(name);
	if (isEnum) {
    	this.handle(
    		IProblem.AnnotationValueMustBeAnEnumConstant,
    		new String[] { new String(annotationType.readableName()), str },
    		new String[] { new String(annotationType.shortReadableName()), str},
    		value.sourceStart,
    		value.sourceEnd);
	} else {
    	this.handle(
    		IProblem.AnnotationValueMustBeConstant,
    		new String[] { new String(annotationType.readableName()), str },
    		new String[] { new String(annotationType.shortReadableName()), str},
    		value.sourceStart,
    		value.sourceEnd);
    }
}
public void anonymousClassCannotExtendFinalClass(TypeReference reference, TypeBinding type) {
	this.handle(
		IProblem.AnonymousClassCannotExtendFinalClass,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		reference.sourceStart,
		reference.sourceEnd);
}
public void argumentTypeCannotBeVoid(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, Argument arg) {
	String[] arguments = new String[] {new String(methodDecl.selector), new String(arg.name)};
	this.handle(
		IProblem.ArgumentTypeCannotBeVoid,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void argumentTypeCannotBeVoidArray(Argument arg) {
	this.handle(
		IProblem.CannotAllocateVoidArray,
		NoArgument,
		NoArgument,
		arg.type.sourceStart,
		arg.type.sourceEnd);
}
public void arrayConstantsOnlyInArrayInitializers(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.ArrayConstantsOnlyInArrayInitializers,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);
}
public void assignmentHasNoEffect(AbstractVariableDeclaration location, char[] name){
	int severity = computeSeverity(IProblem.AssignmentHasNoEffect);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] { new String(name) };
	int start = location.sourceStart;
	int end = location.sourceEnd;
	if (location.initialization != null) {
		end = location.initialization.sourceEnd;
	}
	this.handle(
			IProblem.AssignmentHasNoEffect,
			arguments,
			arguments,
			severity,
			start,
			end);
}
public void assignmentHasNoEffect(Assignment location, char[] name){
	int severity = computeSeverity(IProblem.AssignmentHasNoEffect);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] { new String(name) };
	this.handle(
			IProblem.AssignmentHasNoEffect,
			arguments,
			arguments,
			severity,
			location.sourceStart,
			location.sourceEnd);
}

public void attemptToReturnNonVoidExpression(ReturnStatement returnStatement, TypeBinding expectedType) {
	this.handle(
		IProblem.VoidMethodReturnsValue,
		new String[] {new String(expectedType.readableName())},
		new String[] {new String(expectedType.shortReadableName())},
		returnStatement.sourceStart,
		returnStatement.sourceEnd);
}


public void attemptToReturnVoidValue(ReturnStatement returnStatement) {
	this.handle(
		IProblem.MethodReturnsVoid,
		NoArgument,
		NoArgument,
		returnStatement.sourceStart,
		returnStatement.sourceEnd);
}
public void autoboxing(Expression expression, TypeBinding originalType, TypeBinding convertedType) {
	if (this.options.getSeverity(CompilerOptions.AutoBoxing) == ProblemSeverities.Ignore) return;
	this.handle(
		originalType.isBaseType() ? IProblem.BoxingConversion : IProblem.UnboxingConversion,
		new String[] { new String(originalType.readableName()), new String(convertedType.readableName()), },
		new String[] { new String(originalType.shortReadableName()), new String(convertedType.shortReadableName()), },
		expression.sourceStart,
		expression.sourceEnd);
}
public void boundCannotBeArray(ASTNode location, TypeBinding type) {
	this.handle(
		IProblem.BoundCannotBeArray,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void boundMustBeAnInterface(ASTNode location, TypeBinding type) {
	this.handle(
		IProblem.BoundMustBeAnInterface,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void bytecodeExceeds64KLimit(AbstractMethodDeclaration location) {
	MethodBinding method = location.binding;
	if (location.isConstructor()) {
		this.handle(
			IProblem.BytecodeExceeds64KLimitForConstructor,
			new String[] {new String(location.selector), typesAsString(method.isVarargs(), method.parameters, false)},
			new String[] {new String(location.selector), typesAsString(method.isVarargs(), method.parameters, true)},
			ProblemSeverities.Error | ProblemSeverities.Abort | ProblemSeverities.Fatal,
			location.sourceStart,
			location.sourceEnd);
	} else {
		this.handle(
			IProblem.BytecodeExceeds64KLimit,
			new String[] {new String(location.selector), typesAsString(method.isVarargs(), method.parameters, false)},
			new String[] {new String(location.selector), typesAsString(method.isVarargs(), method.parameters, true)},
			ProblemSeverities.Error | ProblemSeverities.Abort | ProblemSeverities.Fatal,
			location.sourceStart,
			location.sourceEnd);
	}
}
public void bytecodeExceeds64KLimit(TypeDeclaration location) {
	this.handle(
		IProblem.BytecodeExceeds64KLimitForClinit,
		NoArgument,
		NoArgument,
		ProblemSeverities.Error | ProblemSeverities.Abort | ProblemSeverities.Fatal,
		location.sourceStart,
		location.sourceEnd);
}
public void cannotAllocateVoidArray(Expression expression) {
	this.handle(
		IProblem.CannotAllocateVoidArray,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void cannotAssignToFinalField(FieldBinding field, ASTNode location) {
	this.handle(
		IProblem.FinalFieldAssignment,
		new String[] {
			(field.declaringClass == null ? "array" : new String(field.declaringClass.readableName())), //$NON-NLS-1$
			new String(field.readableName())},
		new String[] {
			(field.declaringClass == null ? "array" : new String(field.declaringClass.shortReadableName())), //$NON-NLS-1$
			new String(field.shortReadableName())},
		nodeSourceStart(field, location),
		nodeSourceEnd(field, location));
}
public void cannotAssignToFinalLocal(LocalVariableBinding local, ASTNode location) {
	String[] arguments = new String[] { new String(local.readableName())};
	this.handle(
		IProblem.NonBlankFinalLocalAssignment,
		arguments,
		arguments,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}
public void cannotAssignToFinalOuterLocal(LocalVariableBinding local, ASTNode location) {
	String[] arguments = new String[] {new String(local.readableName())};
	this.handle(
		IProblem.FinalOuterLocalAssignment,
		arguments,
		arguments,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}
public void cannotDefineDimensionsAndInitializer(ArrayAllocationExpression expresssion) {
	this.handle(
		IProblem.CannotDefineDimensionExpressionsWithInit,
		NoArgument,
		NoArgument,
		expresssion.sourceStart,
		expresssion.sourceEnd);
}
public void cannotDireclyInvokeAbstractMethod(MessageSend messageSend, MethodBinding method) {
	this.handle(
		IProblem.DirectInvocationOfAbstractMethod,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void cannotExtendEnum(SourceTypeBinding type, TypeReference superclass, TypeBinding superTypeBinding) {
	String name = new String(type.sourceName());
	String superTypeFullName = new String(superTypeBinding.readableName());
	String superTypeShortName = new String(superTypeBinding.shortReadableName());
	if (superTypeShortName.equals(name)) superTypeShortName = superTypeFullName;
	this.handle(
		IProblem.CannotExtendEnum,
		new String[] {superTypeFullName, name},
		new String[] {superTypeShortName, name},
		superclass.sourceStart,
		superclass.sourceEnd);
}
public void cannotImportPackage(ImportReference importRef) {
	String[] arguments = new String[] {CharOperation.toString(importRef.tokens)};
	this.handle(
		IProblem.CannotImportPackage,
		arguments,
		arguments,
		importRef.sourceStart,
		importRef.sourceEnd);
}
public void cannotInstantiate(TypeReference typeRef, TypeBinding type) {
	this.handle(
		IProblem.InvalidClassInstantiation,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		typeRef.sourceStart,
		typeRef.sourceEnd);
}
public void cannotInvokeSuperConstructorInEnum(ExplicitConstructorCall constructorCall, MethodBinding enumConstructor) {
	this.handle(
		IProblem.CannotInvokeSuperConstructorInEnum,
		new String[] {
		        new String(enumConstructor.declaringClass.sourceName()),
		        typesAsString(enumConstructor.isVarargs(), enumConstructor.parameters, false),
		 },
		new String[] {
		        new String(enumConstructor.declaringClass.sourceName()),
		        typesAsString(enumConstructor.isVarargs(), enumConstructor.parameters, true),
		 },
		constructorCall.sourceStart,
		constructorCall.sourceEnd);
}
public void cannotReadSource(CompilationUnitDeclaration unit, AbortCompilationUnit abortException, boolean verbose) {
	String fileName = new String(unit.compilationResult.fileName);
	if (abortException.exception instanceof CharConversionException) {
		// specific encoding issue
		String encoding = abortException.encoding;
		if (encoding == null) {
			encoding = System.getProperty("file.encoding"); //$NON-NLS-1$
		}
		String[] arguments = new String[]{ fileName, encoding, };
		this.handle(
				IProblem.InvalidEncoding,
				arguments,
				arguments,
				0,
				0);
		return;
	}
	StringWriter stringWriter = new StringWriter();
	PrintWriter writer = new PrintWriter(stringWriter);
	if (verbose) {
		abortException.exception.printStackTrace(writer);
	} else {
		writer.print(abortException.exception.getClass().getName());
		writer.print(':');
		writer.print(abortException.exception.getMessage());
	}
	String exceptionTrace = stringWriter.toString();
	String[] arguments = new String[]{ fileName, exceptionTrace, };
	this.handle(
			IProblem.CannotReadSource,
			arguments,
			arguments,
			0,
			0);
}
public void cannotReferToNonFinalOuterLocal(LocalVariableBinding local, ASTNode location) {
	String[] arguments =new String[]{ new String(local.readableName())};
	this.handle(
		IProblem.OuterLocalMustBeFinal,
		arguments,
		arguments,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}
public void cannotReturnInInitializer(ASTNode location) {
	this.handle(
		IProblem.CannotReturnInInitializer,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void cannotThrowNull(ASTNode expression) {
	this.handle(
		IProblem.CannotThrowNull,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void cannotThrowType(ASTNode exception, TypeBinding expectedType) {
	this.handle(
		IProblem.CannotThrowType,
		new String[] {new String(expectedType.readableName())},
		new String[] {new String(expectedType.shortReadableName())},
		exception.sourceStart,
		exception.sourceEnd);
}
public void cannotUseQualifiedEnumConstantInCaseLabel(Reference location, FieldBinding field) {
	this.handle(
		IProblem.IllegalQualifiedEnumConstantLabel,
		new String[]{ String.valueOf(field.declaringClass.readableName()), String.valueOf(field.name) },
		new String[]{ String.valueOf(field.declaringClass.shortReadableName()), String.valueOf(field.name) },
		location.sourceStart(),
		location.sourceEnd());
}
public void cannotUseSuperInCodeSnippet(int start, int end) {
	this.handle(
		IProblem.CannotUseSuperInCodeSnippet,
		NoArgument,
		NoArgument,
		ProblemSeverities.Error | ProblemSeverities.Abort | ProblemSeverities.Fatal,
		start,
		end);
}
public void cannotUseSuperInJavaLangObject(ASTNode reference) {
	this.handle(
		IProblem.ObjectHasNoSuperclass,
		NoArgument,
		NoArgument,
		reference.sourceStart,
		reference.sourceEnd);
}
public void caseExpressionMustBeConstant(Expression expression) {
	this.handle(
		IProblem.NonConstantExpression,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void classExtendFinalClass(SourceTypeBinding type, TypeReference superclass, TypeBinding superTypeBinding) {
	String name = new String(type.sourceName());
	String superTypeFullName = new String(superTypeBinding.readableName());
	String superTypeShortName = new String(superTypeBinding.shortReadableName());
	if (superTypeShortName.equals(name)) superTypeShortName = superTypeFullName;
	this.handle(
		IProblem.ClassExtendFinalClass,
		new String[] {superTypeFullName, name},
		new String[] {superTypeShortName, name},
		superclass.sourceStart,
		superclass.sourceEnd);
}
public void codeSnippetMissingClass(String missing, int start, int end) {
	String[] arguments = new String[]{missing};
	this.handle(
		IProblem.CodeSnippetMissingClass,
		arguments,
		arguments,
		ProblemSeverities.Error | ProblemSeverities.Abort | ProblemSeverities.Fatal,
		start,
		end);
}
public void codeSnippetMissingMethod(String className, String missingMethod, String argumentTypes, int start, int end) {
	String[] arguments = new String[]{ className, missingMethod, argumentTypes };
	this.handle(
		IProblem.CodeSnippetMissingMethod,
		arguments,
		arguments,
		ProblemSeverities.Error | ProblemSeverities.Abort | ProblemSeverities.Fatal,
		start,
		end);
}
public void comparingIdenticalExpressions(Expression comparison){
	int severity = computeSeverity(IProblem.ComparingIdentical);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
			IProblem.ComparingIdentical,
			NoArgument,
			NoArgument,
			severity,
			comparison.sourceStart,
			comparison.sourceEnd);
}
/*
 * Given the current configuration, answers which category the problem
 * falls into:
 *		ProblemSeverities.Error | ProblemSeverities.Warning | ProblemSeverities.Ignore
 * when different from Ignore, severity can be coupled with ProblemSeverities.Optional
 * to indicate that this problem is configurable through options
 */
public int computeSeverity(int problemID){

	switch (problemID) {
		case IProblem.VarargsConflict :
			return ProblemSeverities.Warning;
 		case IProblem.TypeCollidesWithPackage :
			return ProblemSeverities.Warning;

		/*
		 * Javadoc tags resolved references errors
		 */
		case IProblem.JavadocInvalidParamName:
		case IProblem.JavadocDuplicateParamName:
		case IProblem.JavadocMissingParamName:
		case IProblem.JavadocInvalidMemberTypeQualification:
		case IProblem.JavadocInvalidThrowsClassName:
		case IProblem.JavadocDuplicateThrowsClassName:
		case IProblem.JavadocMissingThrowsClassName:
		case IProblem.JavadocMissingSeeReference:
		case IProblem.JavadocInvalidValueReference:
		case IProblem.JavadocUndefinedField:
		case IProblem.JavadocAmbiguousField:
		case IProblem.JavadocUndefinedConstructor:
		case IProblem.JavadocAmbiguousConstructor:
		case IProblem.JavadocUndefinedMethod:
		case IProblem.JavadocAmbiguousMethod:
		case IProblem.JavadocAmbiguousMethodReference:
		case IProblem.JavadocParameterMismatch:
		case IProblem.JavadocUndefinedType:
		case IProblem.JavadocAmbiguousType:
		case IProblem.JavadocInternalTypeNameProvided:
		case IProblem.JavadocNoMessageSendOnArrayType:
		case IProblem.JavadocNoMessageSendOnBaseType:
		case IProblem.JavadocInheritedMethodHidesEnclosingName:
		case IProblem.JavadocInheritedFieldHidesEnclosingName:
		case IProblem.JavadocInheritedNameHidesEnclosingTypeName:
		case IProblem.JavadocNonStaticTypeFromStaticInvocation:
		case IProblem.JavadocGenericMethodTypeArgumentMismatch:
		case IProblem.JavadocNonGenericMethod:
		case IProblem.JavadocIncorrectArityForParameterizedMethod:
		case IProblem.JavadocParameterizedMethodArgumentTypeMismatch:
		case IProblem.JavadocTypeArgumentsForRawGenericMethod:
		case IProblem.JavadocGenericConstructorTypeArgumentMismatch:
		case IProblem.JavadocNonGenericConstructor:
		case IProblem.JavadocIncorrectArityForParameterizedConstructor:
		case IProblem.JavadocParameterizedConstructorArgumentTypeMismatch:
		case IProblem.JavadocTypeArgumentsForRawGenericConstructor:
			if (!this.options.reportInvalidJavadocTags) {
				return ProblemSeverities.Ignore;
			}
			break;
		/*
		 * Javadoc invalid tags due to deprecated references
		 */
		case IProblem.JavadocUsingDeprecatedField:
		case IProblem.JavadocUsingDeprecatedConstructor:
		case IProblem.JavadocUsingDeprecatedMethod:
		case IProblem.JavadocUsingDeprecatedType:
			if (!(this.options.reportInvalidJavadocTags && this.options.reportInvalidJavadocTagsDeprecatedRef)) {
				return ProblemSeverities.Ignore;
			}
			break;
		/*
		 * Javadoc invalid tags due to non-visible references
		 */
		case IProblem.JavadocNotVisibleField:
		case IProblem.JavadocNotVisibleConstructor:
		case IProblem.JavadocNotVisibleMethod:
		case IProblem.JavadocNotVisibleType:
		case IProblem.JavadocHiddenReference:
			if (!(this.options.reportInvalidJavadocTags && this.options.reportInvalidJavadocTagsNotVisibleRef)) {
				return ProblemSeverities.Ignore;
			}
			break;
		/*
		 * Javadoc missing tag descriptions
		 */
		case IProblem.JavadocEmptyReturnTag:
			if (CompilerOptions.NO_TAG.equals(this.options.reportMissingJavadocTagDescription)) {
				return ProblemSeverities.Ignore;
			}
			break;
		case IProblem.JavadocMissingTagDescription:
			if (! CompilerOptions.ALL_STANDARD_TAGS.equals(this.options.reportMissingJavadocTagDescription)) {
				return ProblemSeverities.Ignore;
			}
			break;
	}
	int irritant = getIrritant(problemID);
	if (irritant != 0) {
		if ((problemID & IProblem.Javadoc) != 0 && !this.options.docCommentSupport)
			return ProblemSeverities.Ignore;
		return this.options.getSeverity(irritant);
	}
	return ProblemSeverities.Error | ProblemSeverities.Fatal;
}
public void conditionalArgumentsIncompatibleTypes(ConditionalExpression expression, TypeBinding trueType, TypeBinding falseType) {
	this.handle(
		IProblem.IncompatibleTypesInConditionalOperator,
		new String[] {new String(trueType.readableName()), new String(falseType.readableName())},
		new String[] {new String(trueType.sourceName()), new String(falseType.sourceName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void conflictingImport(ImportReference importRef) {
	String[] arguments = new String[] {CharOperation.toString(importRef.tokens)};
	this.handle(
		IProblem.ConflictingImport,
		arguments,
		arguments,
		importRef.sourceStart,
		importRef.sourceEnd);
}
public void constantOutOfRange(Literal literal, TypeBinding literalType) {
	String[] arguments = new String[] {new String(literalType.readableName()), new String(literal.source())};
	this.handle(
		IProblem.NumericValueOutOfRange,
		arguments,
		arguments,
		literal.sourceStart,
		literal.sourceEnd);
}
public void corruptedSignature(TypeBinding enclosingType, char[] signature, int position) {
	this.handle(
		IProblem.CorruptedSignature,
		new String[] { new String(enclosingType.readableName()), new String(signature), String.valueOf(position) },
		new String[] { new String(enclosingType.shortReadableName()), new String(signature), String.valueOf(position) },
		ProblemSeverities.Error | ProblemSeverities.Abort | ProblemSeverities.Fatal,
		0,
		0);
}
public void deprecatedField(FieldBinding field, ASTNode location) {
	int severity = computeSeverity(IProblem.UsingDeprecatedField);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		IProblem.UsingDeprecatedField,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		severity,
		nodeSourceStart(field, location),
		nodeSourceEnd(field, location));
}

public void deprecatedMethod(MethodBinding method, ASTNode location) {
	boolean isConstructor = method.isConstructor();
	int severity = computeSeverity(isConstructor ? IProblem.UsingDeprecatedConstructor : IProblem.UsingDeprecatedMethod);
	if (severity == ProblemSeverities.Ignore) return;
	if (isConstructor) {
		int start = -1;
		if(location instanceof AllocationExpression) {
			// omit the new keyword from the warning marker
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=300031
			AllocationExpression allocationExpression = (AllocationExpression) location;
			if (allocationExpression.enumConstant != null) {
				start = allocationExpression.enumConstant.sourceStart;
			}
			start = allocationExpression.type.sourceStart;
		}
		this.handle(
			IProblem.UsingDeprecatedConstructor,
			new String[] {new String(method.declaringClass.readableName()), typesAsString(method.isVarargs(), method.parameters, false)},
			new String[] {new String(method.declaringClass.shortReadableName()), typesAsString(method.isVarargs(), method.parameters, true)},
			severity,
			(start == -1) ? location.sourceStart : start,
			location.sourceEnd);
	} else {
		int start = -1;
		if (location instanceof MessageSend) {
			// start the warning marker from the location where the name of the method starts
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=300031
			start = (int) (((MessageSend)location).nameSourcePosition >>> 32);
		}
		this.handle(
			IProblem.UsingDeprecatedMethod,
			new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
			new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
			severity,
			(start == -1) ? location.sourceStart : start,
			location.sourceEnd);
	}
}
public void deprecatedType(TypeBinding type, ASTNode location) {
	deprecatedType(type, location, Integer.MAX_VALUE);
}
// The argument 'index' makes sure that we demarcate partial types correctly while marking off
// a deprecated type in a qualified reference (see bug 292510)
public void deprecatedType(TypeBinding type, ASTNode location, int index) {
	if (location == null) return; // 1G828DN - no type ref for synthetic arguments
	int severity = computeSeverity(IProblem.UsingDeprecatedType);
	if (severity == ProblemSeverities.Ignore) return;
	type = type.leafComponentType();
	int sourceStart = -1;
	if (location instanceof QualifiedTypeReference) { // https://bugs.eclipse.org/bugs/show_bug.cgi?id=300031
		QualifiedTypeReference ref = (QualifiedTypeReference) location;
		if (index < Integer.MAX_VALUE) {
			sourceStart = (int) (ref.sourcePositions[index] >> 32);
		}
	}
	this.handle(
		IProblem.UsingDeprecatedType,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		severity,
		(sourceStart == -1) ? location.sourceStart : sourceStart,
		nodeSourceEnd(null, location, index));
}
public void disallowedTargetForAnnotation(Annotation annotation) {
	this.handle(
		IProblem.DisallowedTargetForAnnotation,
		new String[] {new String(annotation.resolvedType.readableName())},
		new String[] {new String(annotation.resolvedType.shortReadableName())},
		annotation.sourceStart,
		annotation.sourceEnd);
}
public void duplicateAnnotation(Annotation annotation) {
	this.handle(
		IProblem.DuplicateAnnotation,
		new String[] {new String(annotation.resolvedType.readableName())},
		new String[] {new String(annotation.resolvedType.shortReadableName())},
		annotation.sourceStart,
		annotation.sourceEnd);
}
public void duplicateAnnotationValue(TypeBinding annotationType, MemberValuePair memberValuePair) {
	String name = 	new String(memberValuePair.name);
	this.handle(
		IProblem.DuplicateAnnotationMember,
		new String[] { name, new String(annotationType.readableName())},
		new String[] {	name, new String(annotationType.shortReadableName())},
		memberValuePair.sourceStart,
		memberValuePair.sourceEnd);
}
public void duplicateBounds(ASTNode location, TypeBinding type) {
	this.handle(
		IProblem.DuplicateBounds,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void duplicateCase(CaseStatement caseStatement) {
	this.handle(
		IProblem.DuplicateCase,
		NoArgument,
		NoArgument,
		caseStatement.sourceStart,
		caseStatement.sourceEnd);
}
public void duplicateDefaultCase(ASTNode statement) {
	this.handle(
		IProblem.DuplicateDefaultCase,
		NoArgument,
		NoArgument,
		statement.sourceStart,
		statement.sourceEnd);
}
public void duplicateEnumSpecialMethod(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {
    MethodBinding method = methodDecl.binding;
	this.handle(
		IProblem.CannotDeclareEnumSpecialMethod,
		new String[] {
	        new String(methodDecl.selector),
			new String(method.declaringClass.readableName()),
			typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {
			new String(methodDecl.selector),
			new String(method.declaringClass.shortReadableName()),
			typesAsString(method.isVarargs(), method.parameters, true)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}

public void duplicateFieldInType(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.DuplicateField,
		new String[] {new String(type.sourceName()), new String(fieldDecl.name)},
		new String[] {new String(type.shortReadableName()), new String(fieldDecl.name)},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void duplicateImport(ImportReference importRef) {
	String[] arguments = new String[] {CharOperation.toString(importRef.tokens)};
	this.handle(
		IProblem.DuplicateImport,
		arguments,
		arguments,
		importRef.sourceStart,
		importRef.sourceEnd);
}

public void duplicateInheritedMethods(SourceTypeBinding type, MethodBinding inheritedMethod1, MethodBinding inheritedMethod2) {
//{ObjectTeams: callin/regular conflict is reported elsewhere:
	if (inheritedMethod1.isCallin() != inheritedMethod2.isCallin())
		return;
// SH}
	this.handle(
		IProblem.DuplicateParameterizedMethods,
		new String[] {
	        new String(inheritedMethod1.selector),
			new String(inheritedMethod1.declaringClass.readableName()),
			typesAsString(inheritedMethod1.isVarargs(), inheritedMethod1.original().parameters, false),
			typesAsString(inheritedMethod2.isVarargs(), inheritedMethod2.original().parameters, false)},
		new String[] {
			new String(inheritedMethod1.selector),
			new String(inheritedMethod1.declaringClass.shortReadableName()),
			typesAsString(inheritedMethod1.isVarargs(), inheritedMethod1.original().parameters, true),
			typesAsString(inheritedMethod2.isVarargs(), inheritedMethod2.original().parameters, true)},
		type.sourceStart(),
		type.sourceEnd());
}
public void duplicateInitializationOfBlankFinalField(FieldBinding field, Reference reference) {
	String[] arguments = new String[]{ new String(field.readableName())};
	this.handle(
		IProblem.DuplicateBlankFinalFieldInitialization,
		arguments,
		arguments,
		nodeSourceStart(field, reference),
		nodeSourceEnd(field, reference));
}
public void duplicateInitializationOfFinalLocal(LocalVariableBinding local, ASTNode location) {
	String[] arguments = new String[] { new String(local.readableName())};
	this.handle(
		IProblem.DuplicateFinalLocalInitialization,
		arguments,
		arguments,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}
public void duplicateMethodInType(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, boolean equalParameters) {
    MethodBinding method = methodDecl.binding;
    if (equalParameters) {
		this.handle(
			IProblem.DuplicateMethod,
			new String[] {
		        new String(methodDecl.selector),
				new String(method.declaringClass.readableName()),
				typesAsString(method.isVarargs(), method.parameters, false)},
			new String[] {
				new String(methodDecl.selector),
				new String(method.declaringClass.shortReadableName()),
				typesAsString(method.isVarargs(), method.parameters, true)},
			methodDecl.sourceStart,
			methodDecl.sourceEnd);
    } else {
        int length = method.parameters.length;
        TypeBinding[] erasures = new TypeBinding[length];
        for (int i = 0; i < length; i++)  {
            erasures[i] = method.parameters[i].erasure();
        }
		this.handle(
			IProblem.DuplicateMethodErasure,
			new String[] {
		        new String(methodDecl.selector),
				new String(method.declaringClass.readableName()),
				typesAsString(method.isVarargs(), method.parameters, false),
				typesAsString(method.isVarargs(), erasures, false) } ,
			new String[] {
				new String(methodDecl.selector),
				new String(method.declaringClass.shortReadableName()),
				typesAsString(method.isVarargs(), method.parameters, true),
				typesAsString(method.isVarargs(), erasures, true) },
			methodDecl.sourceStart,
			methodDecl.sourceEnd);
    }
}

public void duplicateModifierForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
/* to highlight modifiers use:
	this.handle(
		new Problem(
			DuplicateModifierForField,
			new String[] {new String(fieldDecl.name)},
			fieldDecl.modifiers.sourceStart,
			fieldDecl.modifiers.sourceEnd));
*/
	String[] arguments = new String[] {new String(fieldDecl.name)};
	this.handle(
		IProblem.DuplicateModifierForField,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void duplicateModifierForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.DuplicateModifierForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		new String[] {new String(type.shortReadableName()), new String(methodDecl.selector)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void duplicateModifierForType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.DuplicateModifierForType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void duplicateModifierForVariable(LocalDeclaration localDecl, boolean complainForArgument) {
	String[] arguments = new String[] {new String(localDecl.name)};
	this.handle(
		complainForArgument
			? IProblem.DuplicateModifierForArgument
			: IProblem.DuplicateModifierForVariable,
		arguments,
		arguments,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void duplicateNestedType(TypeDeclaration typeDecl) {
//{ObjectTeams: behind the facade introduce a new signature.
	duplicateNestedType(typeDecl, typeDecl.sourceStart, typeDecl.sourceEnd);
}
public void duplicateNestedType(TypeDeclaration typeDecl, int sourceStart, int sourceEnd) {
// SH}
//{ObjectTeams: also tag the nested type:
	typeDecl.tagAsHavingErrors();
/* orig:
	String[] arguments = new String[] {new String(typeDecl.name)};
  :giro */
	String[] arguments = new String[] {new String(typeDecl.sourceName())};
// SH}
//{ObjectTeams: prevent some translations which cannot succeed:
	if (typeDecl.isRole()) {
		RoleModel roleModel = typeDecl.getRoleModel();
		if (roleModel.getClassPartBinding() != null)
			roleModel.getClassPartBinding().tagBits |= TagBits.BaseclassHasProblems;
		if (roleModel.getInterfacePartBinding() != null)
			roleModel.getInterfacePartBinding().tagBits |= TagBits.BaseclassHasProblems;
	}
// SH}
	this.handle(
		IProblem.DuplicateNestedType,
		arguments,
		arguments,
//{ObjectTeams: more flexible positsions
/* orig:
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
  :giro*/
		sourceStart,
		sourceEnd);
// SH}
}
public void duplicateSuperinterface(SourceTypeBinding type, TypeReference reference, ReferenceBinding superType) {
	this.handle(
		IProblem.DuplicateSuperInterface,
		new String[] {
			new String(superType.readableName()),
			new String(type.sourceName())},
		new String[] {
			new String(superType.shortReadableName()),
			new String(type.sourceName())},
		reference.sourceStart,
		reference.sourceEnd);
}
public void duplicateTargetInTargetAnnotation(TypeBinding annotationType, NameReference reference) {
	FieldBinding field = reference.fieldBinding();
	String name = 	new String(field.name);
	this.handle(
		IProblem.DuplicateTargetInTargetAnnotation,
		new String[] { name, new String(annotationType.readableName())},
		new String[] {	name, new String(annotationType.shortReadableName())},
		nodeSourceStart(field, reference),
		nodeSourceEnd(field, reference));
}
public void duplicateTypeParameterInType(TypeParameter typeParameter) {
	this.handle(
		IProblem.DuplicateTypeVariable,
		new String[] { new String(typeParameter.name)},
		new String[] { new String(typeParameter.name)},
		typeParameter.sourceStart,
		typeParameter.sourceEnd);
}
public void duplicateTypes(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	String[] arguments = new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)};
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	this.handle(
		IProblem.DuplicateTypes,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd,
		compUnitDecl.compilationResult);
}
public void emptyControlFlowStatement(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.EmptyControlFlowStatement,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);
}
public void enumAbstractMethodMustBeImplemented(AbstractMethodDeclaration method) {
	MethodBinding abstractMethod = method.binding;
	this.handle(
		// Must implement the inherited abstract method %1
		// 8.4.3 - Every non-abstract subclass of an abstract type, A, must provide a concrete implementation of all of A's methods.
		IProblem.EnumAbstractMethodMustBeImplemented,
		new String[] {
		        new String(abstractMethod.selector),
		        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, false),
		        new String(abstractMethod.declaringClass.readableName()),
		},
		new String[] {
		        new String(abstractMethod.selector),
		        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, true),
		        new String(abstractMethod.declaringClass.shortReadableName()),
		},
		method.sourceStart(),
		method.sourceEnd());
}
public void enumConstantMustImplementAbstractMethod(AbstractMethodDeclaration method, FieldDeclaration field) {
	MethodBinding abstractMethod = method.binding;
	this.handle(
		IProblem.EnumConstantMustImplementAbstractMethod,
		new String[] {
		        new String(abstractMethod.selector),
		        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, false),
		        new String(field.name),
		},
		new String[] {
		        new String(abstractMethod.selector),
		        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, true),
		        new String(field.name),
		},
		field.sourceStart(),
		field.sourceEnd());
}
public void enumConstantsCannotBeSurroundedByParenthesis(Expression expression) {
	this.handle(
		IProblem.EnumConstantsCannotBeSurroundedByParenthesis,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void enumStaticFieldUsedDuringInitialization(FieldBinding field, ASTNode location) {
	this.handle(
		IProblem.EnumStaticFieldInInInitializerContext,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		nodeSourceStart(field, location),
		nodeSourceEnd(field, location));
}
public void enumSwitchCannotTargetField(Reference reference, FieldBinding field) {
	this.handle(
			IProblem.EnumSwitchCannotTargetField,
			new String[]{ String.valueOf(field.declaringClass.readableName()), String.valueOf(field.name) },
			new String[]{ String.valueOf(field.declaringClass.shortReadableName()), String.valueOf(field.name) },
			nodeSourceStart(field, reference),
			nodeSourceEnd(field, reference));
}
public void errorNoMethodFor(MessageSend messageSend, TypeBinding recType, TypeBinding[] params) {
	StringBuffer buffer = new StringBuffer();
	StringBuffer shortBuffer = new StringBuffer();
	for (int i = 0, length = params.length; i < length; i++) {
		if (i != 0){
			buffer.append(", "); //$NON-NLS-1$
			shortBuffer.append(", "); //$NON-NLS-1$
		}
		buffer.append(new String(params[i].readableName()));
		shortBuffer.append(new String(params[i].shortReadableName()));
	}

	int id = recType.isArrayType() ? IProblem.NoMessageSendOnArrayType : IProblem.NoMessageSendOnBaseType;
	this.handle(
		id,
		new String[] {new String(recType.readableName()), new String(messageSend.selector), buffer.toString()},
		new String[] {new String(recType.shortReadableName()), new String(messageSend.selector), shortBuffer.toString()},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void errorThisSuperInStatic(ASTNode reference) {
	String[] arguments = new String[] {reference.isSuper() ? "super" : "this"}; //$NON-NLS-2$ //$NON-NLS-1$
	this.handle(
		IProblem.ThisInStaticContext,
		arguments,
		arguments,
		reference.sourceStart,
		reference.sourceEnd);
}
public void expressionShouldBeAVariable(Expression expression) {
	this.handle(
		IProblem.ExpressionShouldBeAVariable,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void fakeReachable(ASTNode location) {
	int sourceStart = location.sourceStart;
	int sourceEnd = location.sourceEnd;
	if (location instanceof LocalDeclaration) {
		LocalDeclaration declaration = (LocalDeclaration) location;
		sourceStart = declaration.declarationSourceStart;
		sourceEnd = declaration.declarationSourceEnd;
	}	
	this.handle(
		IProblem.DeadCode,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);
}
public void fieldHiding(FieldDeclaration fieldDecl, Binding hiddenVariable) {
	FieldBinding field = fieldDecl.binding;
	if (CharOperation.equals(TypeConstants.SERIALVERSIONUID, field.name)
			&& field.isStatic()
			&& field.isFinal()
			&& TypeBinding.LONG == field.type) {
				return; // do not report unused serialVersionUID field
	}
	if (CharOperation.equals(TypeConstants.SERIALPERSISTENTFIELDS, field.name)
			&& field.isStatic()
			&& field.isFinal()
			&& field.type.dimensions() == 1
			&& CharOperation.equals(TypeConstants.CharArray_JAVA_IO_OBJECTSTREAMFIELD, field.type.leafComponentType().readableName())) {
				return; // do not report unused serialPersistentFields field
	}
	boolean isLocal = hiddenVariable instanceof LocalVariableBinding;
	int severity = computeSeverity(isLocal ? IProblem.FieldHidingLocalVariable : IProblem.FieldHidingField);
	if (severity == ProblemSeverities.Ignore) return;
	if (isLocal) {
		this.handle(
			IProblem.FieldHidingLocalVariable,
			new String[] {new String(field.declaringClass.readableName()), new String(field.name) },
			new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name) },
			severity,
			nodeSourceStart(hiddenVariable, fieldDecl),
			nodeSourceEnd(hiddenVariable, fieldDecl));
	} else if (hiddenVariable instanceof FieldBinding) {
		FieldBinding hiddenField = (FieldBinding) hiddenVariable;
		this.handle(
			IProblem.FieldHidingField,
			new String[] {new String(field.declaringClass.readableName()), new String(field.name) , new String(hiddenField.declaringClass.readableName())  },
			new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name) , new String(hiddenField.declaringClass.shortReadableName()) },
			severity,
			nodeSourceStart(hiddenField, fieldDecl),
			nodeSourceEnd(hiddenField, fieldDecl));
	}
}
public void fieldsOrThisBeforeConstructorInvocation(ThisReference reference) {
	this.handle(
		IProblem.ThisSuperDuringConstructorInvocation,
		NoArgument,
		NoArgument,
		reference.sourceStart,
		reference.sourceEnd);
}
public void finallyMustCompleteNormally(Block finallyBlock) {
	this.handle(
		IProblem.FinallyMustCompleteNormally,
		NoArgument,
		NoArgument,
		finallyBlock.sourceStart,
		finallyBlock.sourceEnd);
}
public void finalMethodCannotBeOverridden(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		// Cannot override the final method from %1
		// 8.4.3.3 - Final methods cannot be overridden or hidden.
		IProblem.FinalMethodCannotBeOverridden,
		new String[] {new String(inheritedMethod.declaringClass.readableName())},
		new String[] {new String(inheritedMethod.declaringClass.shortReadableName())},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void finalVariableBound(TypeVariableBinding typeVariable, TypeReference typeRef) {
	int severity = computeSeverity(IProblem.FinalBoundForTypeVariable);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		IProblem.FinalBoundForTypeVariable,
		new String[] { new String(typeVariable.sourceName), new String(typeRef.resolvedType.readableName())},
		new String[] { new String(typeVariable.sourceName), new String(typeRef.resolvedType.shortReadableName())},
		severity,
		typeRef.sourceStart,
		typeRef.sourceEnd);
}
/** @param classpathEntryType one of {@link AccessRestriction#COMMAND_LINE},
 * {@link AccessRestriction#LIBRARY}, {@link AccessRestriction#PROJECT} */
public void forbiddenReference(FieldBinding field, ASTNode location,
//{ObjectTeams: caller provides more info via the last parameter:
/* orig:
		 byte classpathEntryType, String classpathEntryName, int problemId) {
  :giro */
		 byte classpathEntryType, AccessRestriction accessRestriction) {
	String classpathEntryName= accessRestriction.classpathEntryName;
	int problemId= accessRestriction.getProblemId();
// SH}
	int severity = computeSeverity(problemId);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		problemId,
		new String[] { new String(field.readableName()) }, // distinct from msg arg for quickfix purpose
		getElaborationId(IProblem.ForbiddenReference, (byte) (FIELD_ACCESS | classpathEntryType)),
		new String[] {
			classpathEntryName,
			new String(field.shortReadableName()),
	        new String(field.declaringClass.shortReadableName())},
	    severity,
		nodeSourceStart(field, location),
		nodeSourceEnd(field, location));
}
/** @param classpathEntryType one of {@link AccessRestriction#COMMAND_LINE},
 * {@link AccessRestriction#LIBRARY}, {@link AccessRestriction#PROJECT} */
public void forbiddenReference(MethodBinding method, ASTNode location,
//{ObjectTeams: caller provides more info via the last parameter:
/* orig:
		byte classpathEntryType, String classpathEntryName, int problemId) {
  :giro */
	    byte classpathEntryType, AccessRestriction accessRestriction) {
	String classpathEntryName= accessRestriction.classpathEntryName;
	int problemId= accessRestriction.getProblemId();
// SH}
	int severity = computeSeverity(problemId);
	if (severity == ProblemSeverities.Ignore) return;
	if (method.isConstructor())
		this.handle(
			problemId,
			new String[] { new String(method.readableName()) }, // distinct from msg arg for quickfix purpose
			getElaborationId(IProblem.ForbiddenReference, (byte) (CONSTRUCTOR_ACCESS | classpathEntryType)),
			new String[] {
				classpathEntryName,
				new String(method.shortReadableName())},
			severity,
			location.sourceStart,
			location.sourceEnd);
	else
		this.handle(
			problemId,
			new String[] { new String(method.readableName()) }, // distinct from msg arg for quickfix purpose
			getElaborationId(IProblem.ForbiddenReference, (byte) (METHOD_ACCESS | classpathEntryType)),
			new String[] {
				classpathEntryName,
				new String(method.shortReadableName()),
		        new String(method.declaringClass.shortReadableName())},
		    severity,
			location.sourceStart,
			location.sourceEnd);
}
/** @param classpathEntryType one of {@link AccessRestriction#COMMAND_LINE},
 * {@link AccessRestriction#LIBRARY}, {@link AccessRestriction#PROJECT} */
//{ObjectTeams: caller provides more info via the last parameter:
/* orig:
public void forbiddenReference(TypeBinding type, ASTNode location,
		byte classpathEntryType, String classpathEntryName, int problemId) {
  :giro */
public void forbiddenReference(TypeBinding type, ASTNode location, byte classpathEntryType, AccessRestriction restriction) {
	String classpathEntryName= restriction.classpathEntryName;
	int problemId= restriction.getProblemId();
// SH}
	if (location == null) return;
	int severity = computeSeverity(problemId);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		problemId,
		new String[] { new String(type.readableName()) }, // distinct from msg arg for quickfix purpose
		getElaborationId(IProblem.ForbiddenReference, /* TYPE_ACCESS | */ classpathEntryType), // TYPE_ACCESS values to 0
		new String[] {
			classpathEntryName,
			new String(type.shortReadableName())},
		severity,
		location.sourceStart,
		location.sourceEnd);
}
public void forwardReference(Reference reference, int indexInQualification, FieldBinding field) {
	this.handle(
		IProblem.ReferenceToForwardField,
		NoArgument,
		NoArgument,
		nodeSourceStart(field, reference, indexInQualification),
		nodeSourceEnd(field, reference, indexInQualification));
}
public void forwardTypeVariableReference(ASTNode location, TypeVariableBinding type) {
	this.handle(
		IProblem.ReferenceToForwardTypeVariable,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void genericTypeCannotExtendThrowable(TypeDeclaration typeDecl) {
	ASTNode location = typeDecl.binding.isAnonymousType() ? typeDecl.allocation.type : typeDecl.superclass;
	this.handle(
		IProblem.GenericTypeCannotExtendThrowable,
		new String[]{ new String(typeDecl.binding.readableName()) },
		new String[]{ new String(typeDecl.binding.shortReadableName()) },
		location.sourceStart,
		location.sourceEnd);
}
// use this private API when the compilation unit result can be found through the
// reference context. Otherwise, use the other API taking a problem and a compilation result
// as arguments
private void handle(
		int problemId,
		String[] problemArguments,
		int elaborationId,
		String[] messageArguments,
		int severity,
		int problemStartPosition,
		int problemEndPosition){
	this.handle(
			problemId,
			problemArguments,
			elaborationId,
			messageArguments,
			severity,
			problemStartPosition,
			problemEndPosition,
			this.referenceContext,
			this.referenceContext == null ? null : this.referenceContext.compilationResult());
	this.referenceContext = null;
}
// use this private API when the compilation unit result can be found through the
// reference context. Otherwise, use the other API taking a problem and a compilation result
// as arguments
private void handle(
	int problemId,
	String[] problemArguments,
	String[] messageArguments,
	int problemStartPosition,
	int problemEndPosition){

	this.handle(
			problemId,
			problemArguments,
			messageArguments,
			problemStartPosition,
			problemEndPosition,
			this.referenceContext,
			this.referenceContext == null ? null : this.referenceContext.compilationResult());
	this.referenceContext = null;
}
// use this private API when the compilation unit result cannot be found through the
// reference context.
private void handle(
	int problemId,
	String[] problemArguments,
	String[] messageArguments,
	int problemStartPosition,
	int problemEndPosition,
	CompilationResult unitResult){

	this.handle(
			problemId,
			problemArguments,
			messageArguments,
			problemStartPosition,
			problemEndPosition,
			this.referenceContext,
			unitResult);
	this.referenceContext = null;
}
// use this private API when the compilation unit result can be found through the
// reference context. Otherwise, use the other API taking a problem and a compilation result
// as arguments
private void handle(
	int problemId,
	String[] problemArguments,
	String[] messageArguments,
	int severity,
	int problemStartPosition,
	int problemEndPosition){

	this.handle(
			problemId,
			problemArguments,
			0, // no elaboration
			messageArguments,
			severity,
			problemStartPosition,
			problemEndPosition);
}

public void hiddenCatchBlock(ReferenceBinding exceptionType, ASTNode location) {
	this.handle(
		IProblem.MaskedCatch,
		new String[] {
			new String(exceptionType.readableName()),
		 },
		new String[] {
			new String(exceptionType.shortReadableName()),
		 },
		location.sourceStart,
		location.sourceEnd);
}
//{ObjectTeams: third parameter generalized:
/* orig:
public void hierarchyCircularity(SourceTypeBinding sourceType, ReferenceBinding superType, TypeReference reference) {
  :giro */
public void hierarchyCircularity(SourceTypeBinding sourceType, ReferenceBinding superType, ASTNode reference) {
// SH}	
	int start = 0;
	int end = 0;

	if (reference == null) {	// can only happen when java.lang.Object is busted
		start = sourceType.sourceStart();
		end = sourceType.sourceEnd();
	} else {
		start = reference.sourceStart;
		end = reference.sourceEnd;
	}

	if (sourceType == superType)
		this.handle(
			IProblem.HierarchyCircularitySelfReference,
			new String[] {new String(sourceType.readableName()) },
			new String[] {new String(sourceType.shortReadableName()) },
			start,
			end);
	else
		this.handle(
			IProblem.HierarchyCircularity,
			new String[] {new String(sourceType.readableName()), new String(superType.readableName())},
			new String[] {new String(sourceType.shortReadableName()), new String(superType.shortReadableName())},
			start,
			end);
}

public void hierarchyHasProblems(SourceTypeBinding type) {
//{ObjectTeams: ignore some internal types:
	if (type.isRole()) {
		RoleModel roleModel = type.roleModel;
		if (roleModel != null && roleModel.getAst() != null)
			if (roleModel.getAst().isPurelyCopied)
				return; // silently, not worth reporting
		if (TSuperHelper.isMarkerInterface(type))
			return;
		if (!type.isInterface())
			return; // report only once per role
	}
// SH}
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.HierarchyHasProblems,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalAbstractModifierCombinationForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.IllegalAbstractModifierCombinationForMethod,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalAccessFromTypeVariable(TypeVariableBinding variable, ASTNode location) {
	if ((location.bits & ASTNode.InsideJavadoc)!= 0) {
		javadocInvalidReference(location.sourceStart, location.sourceEnd);
	} else {
		String[] arguments = new String[] { new String(variable.sourceName) };
		this.handle(
				IProblem.IllegalAccessFromTypeVariable,
				arguments,
				arguments,
				location.sourceStart,
				location.sourceEnd);
	}
}
public void illegalClassLiteralForTypeVariable(TypeVariableBinding variable, ASTNode location) {
	String[] arguments = new String[] { new String(variable.sourceName) };
	this.handle(
		IProblem.IllegalClassLiteralForTypeVariable,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void illegalExtendedDimensions(AnnotationMethodDeclaration annotationTypeMemberDeclaration) {
	this.handle(
		IProblem.IllegalExtendedDimensions,
		NoArgument,
		NoArgument,
		annotationTypeMemberDeclaration.sourceStart,
		annotationTypeMemberDeclaration.sourceEnd);
}
public void illegalExtendedDimensions(Argument argument) {
	this.handle(
		IProblem.IllegalExtendedDimensionsForVarArgs,
		NoArgument,
		NoArgument,
		argument.sourceStart,
		argument.sourceEnd);
}
public void illegalGenericArray(TypeBinding leafComponentType, ASTNode location) {
	this.handle(
		IProblem.IllegalGenericArray,
		new String[]{ new String(leafComponentType.readableName())},
		new String[]{ new String(leafComponentType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void illegalInstanceOfGenericType(TypeBinding checkedType, ASTNode location) {
	TypeBinding erasedType = checkedType.leafComponentType().erasure();
	StringBuffer recommendedFormBuffer = new StringBuffer(10);
	if (erasedType instanceof ReferenceBinding) {
		ReferenceBinding referenceBinding = (ReferenceBinding) erasedType;
		recommendedFormBuffer.append(referenceBinding.qualifiedSourceName());
	} else {
		recommendedFormBuffer.append(erasedType.sourceName());
	}
	int count = erasedType.typeVariables().length;
	if (count > 0) {
		recommendedFormBuffer.append('<');
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				recommendedFormBuffer.append(',');
			}
			recommendedFormBuffer.append('?');
		}
		recommendedFormBuffer.append('>');
	}
	for (int i = 0, dim = checkedType.dimensions(); i < dim; i++) {
		recommendedFormBuffer.append("[]"); //$NON-NLS-1$
	}
	String recommendedForm = recommendedFormBuffer.toString();
	if (checkedType.leafComponentType().isTypeVariable()) {
		this.handle(
			IProblem.IllegalInstanceofTypeParameter,
			new String[] { new String(checkedType.readableName()), recommendedForm, },
			new String[] { new String(checkedType.shortReadableName()), recommendedForm, },
				location.sourceStart,
				location.sourceEnd);
		return;
	}
	this.handle(
		IProblem.IllegalInstanceofParameterizedType,
		new String[] { new String(checkedType.readableName()), recommendedForm, },
		new String[] { new String(checkedType.shortReadableName()), recommendedForm, },
		location.sourceStart,
		location.sourceEnd);
}
public void illegalLocalTypeDeclaration(TypeDeclaration typeDeclaration) {
	if (isRecoveredName(typeDeclaration.name)) return;

	int problemID = 0;
	if ((typeDeclaration.modifiers & ClassFileConstants.AccEnum) != 0) {
		problemID = IProblem.CannotDefineEnumInLocalType;
	} else if ((typeDeclaration.modifiers & ClassFileConstants.AccAnnotation) != 0) {
		problemID = IProblem.CannotDefineAnnotationInLocalType;
	} else if ((typeDeclaration.modifiers & ClassFileConstants.AccInterface) != 0) {
		problemID = IProblem.CannotDefineInterfaceInLocalType;
	}
	if (problemID != 0) {
		String[] arguments = new String[] {new String(typeDeclaration.name)};
		this.handle(
			problemID,
			arguments,
			arguments,
			typeDeclaration.sourceStart,
			typeDeclaration.sourceEnd);
	}
}
public void illegalModifierCombinationFinalAbstractForClass(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierCombinationFinalAbstractForClass,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierCombinationFinalVolatileForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {new String(fieldDecl.name)};

	this.handle(
		IProblem.IllegalModifierCombinationFinalVolatileForField,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalModifierForAnnotationField(FieldDeclaration fieldDecl) {
	String name = new String(fieldDecl.name);
	this.handle(
		IProblem.IllegalModifierForAnnotationField,
		new String[] {
			new String(fieldDecl.binding.declaringClass.readableName()),
			name,
		},
		new String[] {
			new String(fieldDecl.binding.declaringClass.shortReadableName()),
			name,
		},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalModifierForAnnotationMember(AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.IllegalModifierForAnnotationMethod,
		new String[] {
			new String(methodDecl.binding.declaringClass.readableName()),
			new String(methodDecl.selector),
		},
		new String[] {
			new String(methodDecl.binding.declaringClass.shortReadableName()),
			new String(methodDecl.selector),
		},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalModifierForAnnotationMemberType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForAnnotationMemberType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForAnnotationType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForAnnotationType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForClass(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForClass,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForEnum(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForEnum,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForEnumConstant(ReferenceBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {new String(fieldDecl.name)};
	this.handle(
		IProblem.IllegalModifierForEnumConstant,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}

public void illegalModifierForEnumConstructor(AbstractMethodDeclaration constructor) {
	this.handle(
		IProblem.IllegalModifierForEnumConstructor,
		NoArgument,
		NoArgument,
		constructor.sourceStart,
		constructor.sourceEnd);
}
public void illegalModifierForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {new String(fieldDecl.name)};
	this.handle(
		IProblem.IllegalModifierForField,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalModifierForInterface(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForInterface,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}

public void illegalModifierForInterfaceField(FieldDeclaration fieldDecl) {
	String name = new String(fieldDecl.name);
	this.handle(
		IProblem.IllegalModifierForInterfaceField,
		new String[] {
			new String(fieldDecl.binding.declaringClass.readableName()),
			name,
		},
		new String[] {
			new String(fieldDecl.binding.declaringClass.shortReadableName()),
			name,
		},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalModifierForInterfaceMethod(AbstractMethodDeclaration methodDecl) {
	// cannot include parameter types since they are not resolved yet
	// and the error message would be too long
	this.handle(
		IProblem.IllegalModifierForInterfaceMethod,
		new String[] {
			new String(methodDecl.selector)
		},
		new String[] {
			new String(methodDecl.selector)
		},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalModifierForLocalClass(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForLocalClass,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMemberClass(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForMemberClass,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMemberEnum(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForMemberEnum,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMemberInterface(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForMemberInterface,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMethod(AbstractMethodDeclaration methodDecl) {
	// cannot include parameter types since they are not resolved yet
	// and the error message would be too long
	this.handle(
		methodDecl.isConstructor() ? IProblem.IllegalModifierForConstructor : IProblem.IllegalModifierForMethod,
		new String[] {
			new String(methodDecl.selector)
		},
		new String[] {
			new String(methodDecl.selector)
		},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalModifierForVariable(LocalDeclaration localDecl, boolean complainAsArgument) {
	String[] arguments = new String[] {new String(localDecl.name)};
	this.handle(
		complainAsArgument
			? IProblem.IllegalModifierForArgument
			: IProblem.IllegalModifierForVariable,
		arguments,
		arguments,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void illegalPrimitiveOrArrayTypeForEnclosingInstance(TypeBinding enclosingType, ASTNode location) {
	this.handle(
		IProblem.IllegalPrimitiveOrArrayTypeForEnclosingInstance,
		new String[] {new String(enclosingType.readableName())},
		new String[] {new String(enclosingType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void illegalQualifiedParameterizedTypeAllocation(TypeReference qualifiedTypeReference, TypeBinding allocatedType) {
	this.handle(
		IProblem.IllegalQualifiedParameterizedTypeAllocation,
		new String[] { new String(allocatedType.readableName()), new String(allocatedType.enclosingType().readableName()), },
		new String[] { new String(allocatedType.shortReadableName()), new String(allocatedType.enclosingType().shortReadableName()), },
		qualifiedTypeReference.sourceStart,
		qualifiedTypeReference.sourceEnd);
}
public void illegalStaticModifierForMemberType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalStaticModifierForMemberType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalUsageOfQualifiedTypeReference(QualifiedTypeReference qualifiedTypeReference) {
	StringBuffer buffer = new StringBuffer();
	char[][] tokens = qualifiedTypeReference.tokens;
	for (int i = 0; i < tokens.length; i++) {
		if (i > 0) buffer.append('.');
		buffer.append(tokens[i]);
	}
	String[] arguments = new String[] { String.valueOf(buffer)};
	this.handle(
		IProblem.IllegalUsageOfQualifiedTypeReference,
		arguments,
		arguments,
		qualifiedTypeReference.sourceStart,
		qualifiedTypeReference.sourceEnd);
}
public void illegalUsageOfWildcard(TypeReference wildcard) {
	this.handle(
		IProblem.InvalidUsageOfWildcard,
		NoArgument,
		NoArgument,
		wildcard.sourceStart,
		wildcard.sourceEnd);
}
public void illegalVararg(Argument argType, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {CharOperation.toString(argType.type.getTypeName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.IllegalVararg,
		arguments,
		arguments,
		argType.sourceStart,
		argType.sourceEnd);
}
public void illegalVisibilityModifierCombinationForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {new String(fieldDecl.name)};
	this.handle(
		IProblem.IllegalVisibilityModifierCombinationForField,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalVisibilityModifierCombinationForMemberType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalVisibilityModifierCombinationForMemberType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalVisibilityModifierCombinationForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.IllegalVisibilityModifierCombinationForMethod,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalVisibilityModifierForInterfaceMemberType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalVisibilityModifierForInterfaceMemberType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalVoidExpression(ASTNode location) {
	this.handle(
		IProblem.InvalidVoidExpression,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void importProblem(ImportReference importRef, Binding expectedImport) {
	if (expectedImport instanceof FieldBinding) {
		int id = IProblem.UndefinedField;
		FieldBinding field = (FieldBinding) expectedImport;
		String[] readableArguments = null;
		String[] shortArguments = null;
		switch (expectedImport.problemId()) {
			case ProblemReasons.NotVisible :
				id = IProblem.NotVisibleField;
				readableArguments = new String[] {CharOperation.toString(importRef.tokens), new String(field.declaringClass.readableName())};
				shortArguments = new String[] {CharOperation.toString(importRef.tokens), new String(field.declaringClass.shortReadableName())};
				break;
			case ProblemReasons.Ambiguous :
				id = IProblem.AmbiguousField;
				readableArguments = new String[] {new String(field.readableName())};
				shortArguments = new String[] {new String(field.readableName())};
				break;
			case ProblemReasons.ReceiverTypeNotVisible :
				id = IProblem.NotVisibleType;
				readableArguments = new String[] {new String(field.declaringClass.leafComponentType().readableName())};
				shortArguments = new String[] {new String(field.declaringClass.leafComponentType().shortReadableName())};
				break;
		}
		this.handle(
			id,
			readableArguments,
			shortArguments,
			nodeSourceStart(field, importRef),
			nodeSourceEnd(field, importRef));
		return;
	}

	if (expectedImport.problemId() == ProblemReasons.NotFound) {
		char[][] tokens = expectedImport instanceof ProblemReferenceBinding
			? ((ProblemReferenceBinding) expectedImport).compoundName
			: importRef.tokens;
		String[] arguments = new String[]{CharOperation.toString(tokens)};
		this.handle(
		        IProblem.ImportNotFound,
		        arguments,
		        arguments,
		        importRef.sourceStart,
		        (int) importRef.sourcePositions[tokens.length - 1]);
		return;
	}
	if (expectedImport.problemId() == ProblemReasons.InvalidTypeForStaticImport) {
		char[][] tokens = importRef.tokens;
		String[] arguments = new String[]{CharOperation.toString(tokens)};
		this.handle(
		        IProblem.InvalidTypeForStaticImport,
		        arguments,
		        arguments,
		        importRef.sourceStart,
		        (int) importRef.sourcePositions[tokens.length - 1]);
		return;
	}
	invalidType(importRef, (TypeBinding)expectedImport);
}
public void incompatibleExceptionInThrowsClause(SourceTypeBinding type, MethodBinding currentMethod, MethodBinding inheritedMethod, ReferenceBinding exceptionType) {
	if (type == currentMethod.declaringClass) {
		int id;
		if (currentMethod.declaringClass.isInterface()
				&& !inheritedMethod.isPublic()){ // interface inheriting Object protected method
			id = IProblem.IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod;
		} else {
			id = IProblem.IncompatibleExceptionInThrowsClause;
		}
		this.handle(
			// Exception %1 is not compatible with throws clause in %2
			// 9.4.4 - The type of exception in the throws clause is incompatible.
			id,
			new String[] {
				new String(exceptionType.sourceName()),
				new String(
					CharOperation.concat(
						inheritedMethod.declaringClass.readableName(),
						inheritedMethod.readableName(),
						'.'))},
			new String[] {
				new String(exceptionType.sourceName()),
				new String(
					CharOperation.concat(
						inheritedMethod.declaringClass.shortReadableName(),
						inheritedMethod.shortReadableName(),
						'.'))},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
	} else
		this.handle(
			// Exception %1 in throws clause of %2 is not compatible with %3
			// 9.4.4 - The type of exception in the throws clause is incompatible.
			IProblem.IncompatibleExceptionInInheritedMethodThrowsClause,
			new String[] {
				new String(exceptionType.sourceName()),
				new String(
					CharOperation.concat(
						currentMethod.declaringClass.sourceName(),
						currentMethod.readableName(),
						'.')),
				new String(
					CharOperation.concat(
						inheritedMethod.declaringClass.readableName(),
						inheritedMethod.readableName(),
						'.'))},
			new String[] {
				new String(exceptionType.sourceName()),
				new String(
					CharOperation.concat(
						currentMethod.declaringClass.sourceName(),
						currentMethod.shortReadableName(),
						'.')),
				new String(
					CharOperation.concat(
						inheritedMethod.declaringClass.shortReadableName(),
						inheritedMethod.shortReadableName(),
						'.'))},
			type.sourceStart(),
			type.sourceEnd());
}
public void incompatibleReturnType(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	StringBuffer methodSignature = new StringBuffer();
	methodSignature
		.append(inheritedMethod.declaringClass.readableName())
		.append('.')
		.append(inheritedMethod.readableName());

	StringBuffer shortSignature = new StringBuffer();
	shortSignature
		.append(inheritedMethod.declaringClass.shortReadableName())
		.append('.')
		.append(inheritedMethod.shortReadableName());

	int id;
	final ReferenceBinding declaringClass = currentMethod.declaringClass;
	if (declaringClass.isInterface()
			&& !inheritedMethod.isPublic()){ // interface inheriting Object protected method
		id = IProblem.IncompatibleReturnTypeForNonInheritedInterfaceMethod;
	} else {
		id = IProblem.IncompatibleReturnType;
	}
	AbstractMethodDeclaration method = currentMethod.sourceMethod();
	int sourceStart = 0;
	int sourceEnd = 0;
	if (method == null) {
		if (declaringClass instanceof SourceTypeBinding) {
			SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) declaringClass;
			sourceStart = sourceTypeBinding.sourceStart();
			sourceEnd = sourceTypeBinding.sourceEnd();
		}
	} else if (method.isConstructor()){
		sourceStart = method.sourceStart;
		sourceEnd = method.sourceEnd;
	} else {
		TypeReference returnType = ((MethodDeclaration) method).returnType;
		sourceStart = returnType.sourceStart;
		if (returnType instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference typeReference = (ParameterizedSingleTypeReference) returnType;
			TypeReference[] typeArguments = typeReference.typeArguments;
			if (typeArguments[typeArguments.length - 1].sourceEnd > typeReference.sourceEnd) {
				sourceEnd = retrieveClosingAngleBracketPosition(typeReference.sourceEnd);
			} else {
				sourceEnd = returnType.sourceEnd;
			}
		} else if (returnType instanceof ParameterizedQualifiedTypeReference) {
			ParameterizedQualifiedTypeReference typeReference = (ParameterizedQualifiedTypeReference) returnType;
			sourceEnd = retrieveClosingAngleBracketPosition(typeReference.sourceEnd);
		} else {
			sourceEnd = returnType.sourceEnd;
		}
	}
//{ObjectTeams: if method is copied provide better message:
	if (method != null && method.isCopied) {
		ReferenceBinding originalClass = currentMethod.copyInheritanceSrc.declaringClass;
		StringBuffer copiedMethodSignature = new StringBuffer();
		copiedMethodSignature
			.append(originalClass.readableName())
			.append('.')
			.append(currentMethod.readableName());
		StringBuffer copiedMethodShortSignature = new StringBuffer();
		copiedMethodShortSignature
			.append(originalClass.shortReadableName())
			.append('.')
			.append(currentMethod.shortReadableName());

		this.handle(
				IProblem.IncompatibleReturnInCopiedMethod,
				new String[] {new String(copiedMethodSignature), methodSignature.toString()},
				new String[] {new String(copiedMethodShortSignature), shortSignature.toString()},
				sourceStart,
				sourceEnd);
		return;
	}
// SH}
	this.handle(
		id,
		new String[] {methodSignature.toString()},
		new String[] {shortSignature.toString()},
		sourceStart,
		sourceEnd);
}
public void incorrectArityForParameterizedType(ASTNode location, TypeBinding type, TypeBinding[] argumentTypes) {
	incorrectArityForParameterizedType(location, type, argumentTypes, Integer.MAX_VALUE);
}
public void incorrectArityForParameterizedType(ASTNode location, TypeBinding type, TypeBinding[] argumentTypes, int index) {
    if (location == null) {
		this.handle(
			IProblem.IncorrectArityForParameterizedType,
			new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false)},
			new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true)},
			ProblemSeverities.AbortCompilation | ProblemSeverities.Error | ProblemSeverities.Fatal,
			0,
			0);
		return; // not reached since aborted above
    }
	this.handle(
		IProblem.IncorrectArityForParameterizedType,
		new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false)},
		new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true)},
		location.sourceStart,
		nodeSourceEnd(null, location, index));
}
public void incorrectLocationForNonEmptyDimension(ArrayAllocationExpression expression, int index) {
	this.handle(
		IProblem.IllegalDimension,
		NoArgument,
		NoArgument,
		expression.dimensions[index].sourceStart,
		expression.dimensions[index].sourceEnd);
}
public void incorrectSwitchType(Expression expression, TypeBinding testType) {
	this.handle(
		IProblem.IncorrectSwitchType,
		new String[] {new String(testType.readableName())},
		new String[] {new String(testType.shortReadableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void indirectAccessToStaticField(ASTNode location, FieldBinding field){
	int severity = computeSeverity(IProblem.IndirectAccessToStaticField);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		IProblem.IndirectAccessToStaticField,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		severity,
		nodeSourceStart(field, location),
		nodeSourceEnd(field, location));
}
public void indirectAccessToStaticMethod(ASTNode location, MethodBinding method) {
	int severity = computeSeverity(IProblem.IndirectAccessToStaticMethod);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		IProblem.IndirectAccessToStaticMethod,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
		severity,
		location.sourceStart,
		location.sourceEnd);
}
public void inheritedMethodReducesVisibility(SourceTypeBinding type, MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
	StringBuffer concreteSignature = new StringBuffer();
	concreteSignature
		.append(concreteMethod.declaringClass.readableName())
		.append('.')
		.append(concreteMethod.readableName());
	StringBuffer shortSignature = new StringBuffer();
	shortSignature
		.append(concreteMethod.declaringClass.shortReadableName())
		.append('.')
		.append(concreteMethod.shortReadableName());
	this.handle(
		// The inherited method %1 cannot hide the public abstract method in %2
		IProblem.InheritedMethodReducesVisibility,
		new String[] {
			concreteSignature.toString(),
			new String(abstractMethods[0].declaringClass.readableName())},
		new String[] {
			shortSignature.toString(),
			new String(abstractMethods[0].declaringClass.shortReadableName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void inheritedMethodsHaveIncompatibleReturnTypes(ASTNode location, MethodBinding[] inheritedMethods, int length) {
	StringBuffer methodSignatures = new StringBuffer();
	StringBuffer shortSignatures = new StringBuffer();
	for (int i = length; --i >= 0;) {
		methodSignatures
			.append(inheritedMethods[i].declaringClass.readableName())
			.append('.')
			.append(inheritedMethods[i].readableName());
		shortSignatures
			.append(inheritedMethods[i].declaringClass.shortReadableName())
			.append('.')
			.append(inheritedMethods[i].shortReadableName());
		if (i != 0){
			methodSignatures.append(", "); //$NON-NLS-1$
			shortSignatures.append(", "); //$NON-NLS-1$
		}
	}

	this.handle(
		// Return type is incompatible with %1
		// 9.4.2 - The return type from the method is incompatible with the declaration.
		IProblem.InheritedIncompatibleReturnType,
		new String[] {methodSignatures.toString()},
		new String[] {shortSignatures.toString()},
		location.sourceStart,
		location.sourceEnd);
}
public void inheritedMethodsHaveIncompatibleReturnTypes(SourceTypeBinding type, MethodBinding[] inheritedMethods, int length) {
	StringBuffer methodSignatures = new StringBuffer();
	StringBuffer shortSignatures = new StringBuffer();
	for (int i = length; --i >= 0;) {
		methodSignatures
			.append(inheritedMethods[i].declaringClass.readableName())
			.append('.')
			.append(inheritedMethods[i].readableName());
		shortSignatures
			.append(inheritedMethods[i].declaringClass.shortReadableName())
			.append('.')
			.append(inheritedMethods[i].shortReadableName());
		if (i != 0){
			methodSignatures.append(", "); //$NON-NLS-1$
			shortSignatures.append(", "); //$NON-NLS-1$
		}
	}

	this.handle(
		// Return type is incompatible with %1
		// 9.4.2 - The return type from the method is incompatible with the declaration.
		IProblem.InheritedIncompatibleReturnType,
		new String[] {methodSignatures.toString()},
		new String[] {shortSignatures.toString()},
		type.sourceStart(),
		type.sourceEnd());
}
public void inheritedMethodsHaveNameClash(SourceTypeBinding type, MethodBinding oneMethod, MethodBinding twoMethod) {
	this.handle(
		IProblem.MethodNameClash,
		new String[] {
			new String(oneMethod.selector),
			typesAsString(oneMethod.original().isVarargs(), oneMethod.original().parameters, false),
			new String(oneMethod.declaringClass.readableName()),
			typesAsString(twoMethod.original().isVarargs(), twoMethod.original().parameters, false),
			new String(twoMethod.declaringClass.readableName()),
		 },
		new String[] {
			new String(oneMethod.selector),
			typesAsString(oneMethod.original().isVarargs(), oneMethod.original().parameters, true),
			new String(oneMethod.declaringClass.shortReadableName()),
			typesAsString(twoMethod.original().isVarargs(), twoMethod.original().parameters, true),
			new String(twoMethod.declaringClass.shortReadableName()),
		 },
		 type.sourceStart(),
		 type.sourceEnd());
}
public void initializerMustCompleteNormally(FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.InitializerMustCompleteNormally,
		NoArgument,
		NoArgument,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void innerTypesCannotDeclareStaticInitializers(ReferenceBinding innerType, Initializer initializer) {
	this.handle(
		IProblem.CannotDefineStaticInitializerInLocalType,
		new String[] {new String(innerType.readableName())},
		new String[] {new String(innerType.shortReadableName())},
		initializer.sourceStart,
		initializer.sourceStart);
}
public void interfaceCannotHaveConstructors(ConstructorDeclaration constructor) {
	this.handle(
		IProblem.InterfaceCannotHaveConstructors,
		NoArgument,
		NoArgument,
		constructor.sourceStart,
		constructor.sourceEnd,
		constructor,
		constructor.compilationResult());
}
public void interfaceCannotHaveInitializers(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {new String(type.sourceName())};

	this.handle(
		IProblem.InterfaceCannotHaveInitializers,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void invalidAnnotationMemberType(MethodDeclaration methodDecl) {
	this.handle(
		IProblem.InvalidAnnotationMemberType,
		new String[] {
			new String(methodDecl.binding.returnType.readableName()),
			new String(methodDecl.selector),
			new String(methodDecl.binding.declaringClass.readableName()),
		},
		new String[] {
			new String(methodDecl.binding.returnType.shortReadableName()),
			new String(methodDecl.selector),
			new String(methodDecl.binding.declaringClass.shortReadableName()),
		},
		methodDecl.returnType.sourceStart,
		methodDecl.returnType.sourceEnd);

}
public void invalidBreak(ASTNode location) {
	this.handle(
		IProblem.InvalidBreak,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void invalidConstructor(Statement statement, MethodBinding targetConstructor) {
	boolean insideDefaultConstructor =
		(this.referenceContext instanceof ConstructorDeclaration)
			&& ((ConstructorDeclaration)this.referenceContext).isDefaultConstructor();
	boolean insideImplicitConstructorCall =
		(statement instanceof ExplicitConstructorCall)
			&& (((ExplicitConstructorCall) statement).accessMode == ExplicitConstructorCall.ImplicitSuper);

	int sourceStart = statement.sourceStart;
	int sourceEnd = statement.sourceEnd;
	if (statement instanceof AllocationExpression) {
		AllocationExpression allocation = (AllocationExpression)statement;
		if (allocation.enumConstant != null) {
			sourceStart = allocation.enumConstant.sourceStart;
			sourceEnd = allocation.enumConstant.sourceEnd;
		}
	}

	int id = IProblem.UndefinedConstructor; //default...
    MethodBinding shownConstructor = targetConstructor;
	switch (targetConstructor.problemId()) {
		case ProblemReasons.NotFound :
			ProblemMethodBinding problemConstructor = (ProblemMethodBinding) targetConstructor;
			if (problemConstructor.closestMatch != null) {
		    	if ((problemConstructor.closestMatch.tagBits & TagBits.HasMissingType) != 0) {
					missingTypeInConstructor(statement, problemConstructor.closestMatch);
					return;
		    	}
		    }

			if (insideDefaultConstructor){
				id = IProblem.UndefinedConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				id = IProblem.UndefinedConstructorInImplicitConstructorCall;
			} else {
				id = IProblem.UndefinedConstructor;
			}
			break;
		case ProblemReasons.NotVisible :
//{ObjectTeams: more specific message?
		  if (shownConstructor.declaringClass.isRole()) {
			if (   statement instanceof TSuperMessageSend
				&& ((ReferenceBinding)((TSuperMessageSend)statement).actualReceiverType).getRealType() != shownConstructor.declaringClass.getRealType())
				id = IProblem.IndirectTSuperInvisible;
			else if (statement instanceof MessageSend && !((MessageSend)statement).receiver.isThis())
				id= IProblem.ExternalizedCallToNonPublicConstructor;
			else
				id = IProblem.NotVisibleRoleConstructor;
		  } else
// SH}
			if (insideDefaultConstructor){
				id = IProblem.NotVisibleConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				id = IProblem.NotVisibleConstructorInImplicitConstructorCall;
			} else {
				id = IProblem.NotVisibleConstructor;
			}
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			if (problemConstructor.closestMatch != null) {
			    shownConstructor = problemConstructor.closestMatch.original();
		    }
			break;
		case ProblemReasons.Ambiguous :
			if (insideDefaultConstructor){
				id = IProblem.AmbiguousConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				id = IProblem.AmbiguousConstructorInImplicitConstructorCall;
			} else {
				id = IProblem.AmbiguousConstructor;
			}
			break;
//{ObjectTeams: for roles we detect this earlier than JDT
		case ProblemReasons.NonStaticReferenceInConstructorInvocation:
			if (   targetConstructor.declaringClass != null
				&& targetConstructor.declaringClass.isRole())
			{
				noSuchEnclosingInstance(targetConstructor.declaringClass.enclosingType(), statement, true);
				return;
			}
			break;
// SH}
		case ProblemReasons.ParameterBoundMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			ParameterizedGenericMethodBinding substitutedConstructor = (ParameterizedGenericMethodBinding) problemConstructor.closestMatch;
			shownConstructor = substitutedConstructor.original();
			int augmentedLength = problemConstructor.parameters.length;
			TypeBinding inferredTypeArgument = problemConstructor.parameters[augmentedLength-2];
			TypeVariableBinding typeParameter = (TypeVariableBinding) problemConstructor.parameters[augmentedLength-1];
			TypeBinding[] invocationArguments = new TypeBinding[augmentedLength-2]; // remove extra info from the end
			System.arraycopy(problemConstructor.parameters, 0, invocationArguments, 0, augmentedLength-2);
			this.handle(
				IProblem.GenericConstructorTypeArgumentMismatch,
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false),
				        new String(shownConstructor.declaringClass.readableName()),
				        typesAsString(false, invocationArguments, false),
				        new String(inferredTypeArgument.readableName()),
				        new String(typeParameter.sourceName),
				        parameterBoundAsString(typeParameter, false) },
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true),
				        new String(shownConstructor.declaringClass.shortReadableName()),
				        typesAsString(false, invocationArguments, true),
				        new String(inferredTypeArgument.shortReadableName()),
				        new String(typeParameter.sourceName),
				        parameterBoundAsString(typeParameter, true) },
				sourceStart,
				sourceEnd);
			return;

		case ProblemReasons.TypeParameterArityMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			if (shownConstructor.typeVariables == Binding.NO_TYPE_VARIABLES) {
				this.handle(
					IProblem.NonGenericConstructor,
					new String[] {
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false),
					        new String(shownConstructor.declaringClass.readableName()),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
					new String[] {
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true),
					        new String(shownConstructor.declaringClass.shortReadableName()),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
					sourceStart,
					sourceEnd);
			} else {
				this.handle(
					IProblem.IncorrectArityForParameterizedConstructor  ,
					new String[] {
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false),
					        new String(shownConstructor.declaringClass.readableName()),
							typesAsString(false, shownConstructor.typeVariables, false),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
					new String[] {
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true),
					        new String(shownConstructor.declaringClass.shortReadableName()),
							typesAsString(false, shownConstructor.typeVariables, true),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
					sourceStart,
					sourceEnd);
			}
			return;
		case ProblemReasons.ParameterizedMethodTypeMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			this.handle(
				IProblem.ParameterizedConstructorArgumentTypeMismatch,
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false),
				        new String(shownConstructor.declaringClass.readableName()),
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownConstructor).typeArguments, false),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true),
				        new String(shownConstructor.declaringClass.shortReadableName()),
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownConstructor).typeArguments, true),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
				sourceStart,
				sourceEnd);
			return;
		case ProblemReasons.TypeArgumentsForRawGenericMethod :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			this.handle(
				IProblem.TypeArgumentsForRawGenericConstructor,
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false),
				        new String(shownConstructor.declaringClass.readableName()),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true),
				        new String(shownConstructor.declaringClass.shortReadableName()),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
				sourceStart,
				sourceEnd);
			return;
		case ProblemReasons.NoError : // 0
		default :
			needImplementation(statement); // want to fail to see why we were here...
			break;
	}

	this.handle(
		id,
		new String[] {new String(targetConstructor.declaringClass.readableName()), typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false)},
		new String[] {new String(targetConstructor.declaringClass.shortReadableName()), typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true)},
		sourceStart,
		sourceEnd);
}
public void invalidContinue(ASTNode location) {
	this.handle(
		IProblem.InvalidContinue,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void invalidEnclosingType(Expression expression, TypeBinding type, ReferenceBinding enclosingType) {

	if (enclosingType.isAnonymousType()) enclosingType = enclosingType.superclass();
	if (enclosingType.sourceName != null && enclosingType.sourceName.length == 0) return;

	int flag = IProblem.UndefinedType; // default
	switch (type.problemId()) {
		case ProblemReasons.NotFound : // 1
			flag = IProblem.UndefinedType;
			break;
		case ProblemReasons.NotVisible : // 2
			flag = IProblem.NotVisibleType;
			break;
		case ProblemReasons.Ambiguous : // 3
			flag = IProblem.AmbiguousType;
			break;
		case ProblemReasons.InternalNameProvided :
			flag = IProblem.InternalTypeNameProvided;
			break;
		case ProblemReasons.NoError : // 0
		default :
			needImplementation(expression); // want to fail to see why we were here...
			break;
	}

	this.handle(
		flag,
		new String[] {new String(enclosingType.readableName()) + "." + new String(type.readableName())}, //$NON-NLS-1$
		new String[] {new String(enclosingType.shortReadableName()) + "." + new String(type.shortReadableName())}, //$NON-NLS-1$
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidExplicitConstructorCall(ASTNode location) {

//{ObjectTeams
/*@ orig:
	this.handle(
		IProblem.InvalidExplicitConstructorCall,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
  :giro */
		int id = ((ExplicitConstructorCall)location).isTsuperAccess() ?
					IProblem.InvalidExplicitTSuperConstructorCall :
					IProblem.InvalidExplicitConstructorCall;
		this.handle(
			id,
			NoArgument,
			NoArgument,
			location.sourceStart,
			location.sourceEnd);
//	MW}
}
public void invalidExpressionAsStatement(Expression expression){
	this.handle(
		IProblem.InvalidExpressionAsStatement,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidField(FieldReference fieldRef, TypeBinding searchedType) {
	if(isRecoveredName(fieldRef.token)) return;

	int id = IProblem.UndefinedField;
	FieldBinding field = fieldRef.binding;
	switch (field.problemId()) {
		case ProblemReasons.NotFound :
			if ((searchedType.tagBits & TagBits.HasMissingType) != 0) {
				this.handle(
						IProblem.UndefinedType,
						new String[] {new String(searchedType.leafComponentType().readableName())},
						new String[] {new String(searchedType.leafComponentType().shortReadableName())},
						fieldRef.receiver.sourceStart,
						fieldRef.receiver.sourceEnd);
					return;
			}
			id = IProblem.UndefinedField;
/* also need to check that the searchedType is the receiver type
			if (searchedType.isHierarchyInconsistent())
				severity = SecondaryError;
*/
			break;
		case ProblemReasons.NotVisible :
//{ObjectTeams: report access via externalized role?
		  if (RoleTypeBinding.isRoleWithExplicitAnchor(searchedType)) {
			this.handle(
				IProblem.NonPublicFieldOfExternalizedRole,
				new String[] {new String(fieldRef.token), new String(searchedType.readableName())},
				new String[] {new String(fieldRef.token), new String(searchedType.shortReadableName())},
				nodeSourceStart(field, fieldRef),
				nodeSourceEnd(field, fieldRef));
		  } else
// SH}
			this.handle(
				IProblem.NotVisibleField,
				new String[] {new String(fieldRef.token), new String(field.declaringClass.readableName())},
				new String[] {new String(fieldRef.token), new String(field.declaringClass.shortReadableName())},
				nodeSourceStart(field, fieldRef),
				nodeSourceEnd(field, fieldRef));
			return;
		case ProblemReasons.Ambiguous :
			id = IProblem.AmbiguousField;
			break;
		case ProblemReasons.NonStaticReferenceInStaticContext :
			id = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case ProblemReasons.NonStaticReferenceInConstructorInvocation :
			id = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case ProblemReasons.InheritedNameHidesEnclosingName :
			id = IProblem.InheritedFieldHidesEnclosingName;
			break;
		case ProblemReasons.ReceiverTypeNotVisible :
			this.handle(
				IProblem.NotVisibleType, // cannot occur in javadoc comments
				new String[] {new String(searchedType.leafComponentType().readableName())},
				new String[] {new String(searchedType.leafComponentType().shortReadableName())},
				fieldRef.receiver.sourceStart,
				fieldRef.receiver.sourceEnd);
			return;

		case ProblemReasons.NoError : // 0
		default :
			needImplementation(fieldRef); // want to fail to see why we were here...
			break;
	}

	String[] arguments = new String[] {new String(field.readableName())};
	this.handle(
		id,
		arguments,
		arguments,
		nodeSourceStart(field, fieldRef),
		nodeSourceEnd(field, fieldRef));
}
public void invalidField(NameReference nameRef, FieldBinding field) {
	if (nameRef instanceof QualifiedNameReference) {
		QualifiedNameReference ref = (QualifiedNameReference) nameRef;
		if (isRecoveredName(ref.tokens)) return;
	} else {
		SingleNameReference ref = (SingleNameReference) nameRef;
		if (isRecoveredName(ref.token)) return;
	}
	int id = IProblem.UndefinedField;
	switch (field.problemId()) {
		case ProblemReasons.NotFound :
			TypeBinding declaringClass = field.declaringClass;
			if (declaringClass != null && (declaringClass.tagBits & TagBits.HasMissingType) != 0) {
				this.handle(
						IProblem.UndefinedType,
						new String[] {new String(field.declaringClass.readableName())},
						new String[] {new String(field.declaringClass.shortReadableName())},
						nameRef.sourceStart,
						nameRef.sourceEnd);
					return;
			}
			String[] arguments = new String[] {new String(field.readableName())};
			this.handle(
					id,
					arguments,
					arguments,
					nodeSourceStart(field, nameRef),
					nodeSourceEnd(field, nameRef));
			return;
		case ProblemReasons.NotVisible :
			char[] name = field.readableName();
			name = CharOperation.lastSegment(name, '.');
			this.handle(
				IProblem.NotVisibleField,
				new String[] {new String(name), new String(field.declaringClass.readableName())},
				new String[] {new String(name), new String(field.declaringClass.shortReadableName())},
				nodeSourceStart(field, nameRef),
				nodeSourceEnd(field, nameRef));
			return;
		case ProblemReasons.Ambiguous :
			id = IProblem.AmbiguousField;
			break;
		case ProblemReasons.NonStaticReferenceInStaticContext :
			id = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case ProblemReasons.NonStaticReferenceInConstructorInvocation :
			id = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case ProblemReasons.InheritedNameHidesEnclosingName :
			id = IProblem.InheritedFieldHidesEnclosingName;
			break;
		case ProblemReasons.ReceiverTypeNotVisible :
			this.handle(
				IProblem.NotVisibleType,
				new String[] {new String(field.declaringClass.readableName())},
				new String[] {new String(field.declaringClass.shortReadableName())},
				nameRef.sourceStart,
				nameRef.sourceEnd);
			return;
		case ProblemReasons.NoError : // 0
		default :
			needImplementation(nameRef); // want to fail to see why we were here...
			break;
	}
	String[] arguments = new String[] {new String(field.readableName())};
	this.handle(
		id,
		arguments,
		arguments,
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void invalidField(QualifiedNameReference nameRef, FieldBinding field, int index, TypeBinding searchedType) {
	//the resolution of the index-th field of qname failed
	//qname.otherBindings[index] is the binding that has produced the error

	//The different targetted errors should be :
	//UndefinedField
	//NotVisibleField
	//AmbiguousField

	if (isRecoveredName(nameRef.tokens)) return;

	if (searchedType.isBaseType()) {
		this.handle(
			IProblem.NoFieldOnBaseType,
			new String[] {
				new String(searchedType.readableName()),
				CharOperation.toString(CharOperation.subarray(nameRef.tokens, 0, index)),
				new String(nameRef.tokens[index])},
			new String[] {
				new String(searchedType.sourceName()),
				CharOperation.toString(CharOperation.subarray(nameRef.tokens, 0, index)),
				new String(nameRef.tokens[index])},
			nameRef.sourceStart,
			(int) nameRef.sourcePositions[index]);
		return;
	}

	int id = IProblem.UndefinedField;
	switch (field.problemId()) {
		case ProblemReasons.NotFound :
			if ((searchedType.tagBits & TagBits.HasMissingType) != 0) {
				this.handle(
						IProblem.UndefinedType,
						new String[] {new String(searchedType.leafComponentType().readableName())},
						new String[] {new String(searchedType.leafComponentType().shortReadableName())},
						nameRef.sourceStart,
						(int) nameRef.sourcePositions[index-1]);
					return;
			}
			String fieldName = new String(nameRef.tokens[index]);
			String[] arguments = new String[] {fieldName };
			this.handle(
					id,
					arguments,
					arguments,
					nodeSourceStart(field, nameRef),
					nodeSourceEnd(field, nameRef));
			return;
		case ProblemReasons.NotVisible :
			fieldName = new String(nameRef.tokens[index]);
//{ObjectTeams: report access via externalized role?
		  if (RoleTypeBinding.isRoleWithExplicitAnchor(searchedType)) {
			this.handle(
				IProblem.NonPublicFieldOfExternalizedRole,
				new String[] {fieldName, new String(searchedType.readableName())},
				new String[] {fieldName, new String(searchedType.shortReadableName())},
				nodeSourceStart(field, nameRef),
				nodeSourceEnd(field, nameRef));
		  } else
// SH}
			this.handle(
				IProblem.NotVisibleField,
				new String[] {fieldName, new String(field.declaringClass.readableName())},
				new String[] {fieldName, new String(field.declaringClass.shortReadableName())},
				nodeSourceStart(field, nameRef),
				nodeSourceEnd(field, nameRef));
			return;
		case ProblemReasons.Ambiguous :
			id = IProblem.AmbiguousField;
			break;
		case ProblemReasons.NonStaticReferenceInStaticContext :
			id = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case ProblemReasons.NonStaticReferenceInConstructorInvocation :
			id = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case ProblemReasons.InheritedNameHidesEnclosingName :
			id = IProblem.InheritedFieldHidesEnclosingName;
			break;
		case ProblemReasons.ReceiverTypeNotVisible :
			this.handle(
				IProblem.NotVisibleType,
				new String[] {new String(searchedType.leafComponentType().readableName())},
				new String[] {new String(searchedType.leafComponentType().shortReadableName())},
				nameRef.sourceStart,
				(int) nameRef.sourcePositions[index-1]);
			return;
		case ProblemReasons.NoError : // 0
		default :
			needImplementation(nameRef); // want to fail to see why we were here...
			break;
	}
	String[] arguments = new String[] {CharOperation.toString(CharOperation.subarray(nameRef.tokens, 0, index + 1))};
	this.handle(
		id,
		arguments,
		arguments,
		nameRef.sourceStart,
		(int) nameRef.sourcePositions[index]);
}

public void invalidFileNameForPackageAnnotations(Annotation annotation) {
	this.handle(
			IProblem.InvalidFileNameForPackageAnnotations,
			NoArgument,
			NoArgument,
			annotation.sourceStart,
			annotation.sourceEnd);
}

public void invalidMethod(MessageSend messageSend, MethodBinding method) {
	if (isRecoveredName(messageSend.selector)) return;

//{ObjectTeams: special messages:
	if (method.model != null && method.model.handleError(this, messageSend))
		return;
	// creator calls should be reported as constructors:
	if (CopyInheritance.isCreator(method)) {
		// must be handled in AllocationExpression:
		assert (method.problemId() != ProblemReasons.NonStaticReferenceInStaticContext);

		if (messageSend.actualReceiverType instanceof SourceTypeBinding) {
			TypeDeclaration receiverType = null;
			SourceTypeBinding stb = (SourceTypeBinding)messageSend.actualReceiverType;
			if (stb.scope != null)
				receiverType = stb.scope.referenceContext;
			if (receiverType != null && receiverType.isConverted) {
				this.referenceContext.tagAsHavingErrors();
				return; // ignore creation method not generated in converted type.
			}
		}
		MethodBinding closestMatch = ((ProblemMethodBinding)method).closestMatch;
		ReferenceBinding declaringClass;
		if (closestMatch != null) {
			declaringClass = ((ReferenceBinding)closestMatch.returnType).getRealType();
		} else {
			char[] className = CharOperation.subarray(method.selector, IOTConstants.CREATOR_PREFIX_NAME.length, -1);
			// can't use scope.findType() since we have no scope :(
			declaringClass = TeamModel.findMemberTypeInContext(method.declaringClass, className);
			assert (declaringClass != null); // ProblemReferenceBinding would be OK.
		}
		MethodBinding constructor = new MethodBinding(method, declaringClass);
		constructor.returnType = TypeBinding.VOID;
		char[] selector = declaringClass.sourceName();
		constructor.selector = selector;
		if (messageSend.arguments != null) {
			for (int i = 0; i < messageSend.arguments.length; i++) {
				constructor.parameters[i] = messageSend.arguments[i].resolvedType;
			}
		} else {
			constructor.parameters = Binding.NO_PARAMETERS;
		}
		if (   messageSend.arguments == null
			&& declaringClass.baseclass() != null
			&& method.problemId() == ProblemReasons.NotFound)
		{
			noDefaultCtorInBoundRole(messageSend, constructor);
		} else {
			ProblemMethodBinding problemMethod = new ProblemMethodBinding(
					constructor, selector, constructor.parameters, method.problemId());
			invalidConstructor(messageSend, problemMethod);
		}
		return;
	}
	// reverse the mapping from lower() to _OT$getBase():
	if (   (method.problemId() == ProblemReasons.NotFound)
		&& CharOperation.equals(messageSend.selector, IOTConstants._OT_GETBASE))
		method.selector = IOTConstants.LOWER;		
// SH}

	int id = IProblem.UndefinedMethod; //default...
    MethodBinding shownMethod = method;
	switch (method.problemId()) {
		case ProblemReasons.NotFound :
			if ((method.declaringClass.tagBits & TagBits.HasMissingType) != 0) {
				this.handle(
						IProblem.UndefinedType,
						new String[] {new String(method.declaringClass.readableName())},
						new String[] {new String(method.declaringClass.shortReadableName())},
						messageSend.receiver.sourceStart,
						messageSend.receiver.sourceEnd);
					return;
			}
			id = IProblem.UndefinedMethod;
			ProblemMethodBinding problemMethod = (ProblemMethodBinding) method;
			if (problemMethod.closestMatch != null) {
//{ObjectTeams: printing of tsuper message sends:
					boolean tsuperLookupDone = false;
					if (TSuperHelper.isTSuper(problemMethod.closestMatch)) {
						MethodBinding tsuperMethod = problemMethod.closestMatch;
						if (problemMethod.closestMatch.overriddenTSupers != null) {
							tsuperMethod = problemMethod.closestMatch.overriddenTSupers[0];
							tsuperLookupDone = true;
						}
						tsuperMethod = new MethodBinding(tsuperMethod, tsuperMethod.declaringClass);
						tsuperMethod.selector = CharOperation.concat("tsuper.".toCharArray(), tsuperMethod.selector); //$NON-NLS-1$
						int len = tsuperMethod.parameters.length;
						System.arraycopy(
								tsuperMethod.parameters, 0,
								tsuperMethod.parameters = new TypeBinding[len-1], 0,
								len -1);
						problemMethod.closestMatch = tsuperMethod;
					}
					if (   method.parameters != null
						&& method.parameters.length > 0
						&& TSuperHelper.isMarkerInterface(method.parameters[method.parameters.length-1]))
					{
						// cut off last arg (marker):
						int len = method.parameters.length;
						System.arraycopy(
								method.parameters, 0,
								method.parameters = new TypeBinding[len-1], 0,
								len -1);
						// find tsuper version:
						if (!tsuperLookupDone) {
							ReferenceBinding declaringClass = problemMethod.closestMatch.declaringClass;
							if (declaringClass.isRole()) {
								ReferenceBinding[] tsupers = declaringClass.roleModel.getTSuperRoleBindings();
								if (tsupers.length > 0) {
									problemMethod.closestMatch = new MethodBinding(
											problemMethod.closestMatch,
											tsupers[0]);
									// SH: might not be the perfect match, but as long as "diagonal"
									// tsuper calls are prohibited, the messages seems to be OK.
								}
							}
						}
					}
					// reporting of role-cast error:
					if (   CharOperation.prefixEquals(IOTConstants.CAST_PREFIX, method.selector)
						&& messageSend.arguments != null && messageSend.arguments.length > 0
						&& problemMethod.closestMatch.parameters.length > 0)
					{
						Expression arg = messageSend.arguments[0];
						String rightName      = new String(arg.resolvedType.readableName());
						String rightShortName = new String(arg.resolvedType.shortReadableName());
						TypeBinding param = problemMethod.closestMatch.parameters[0];
						String leftName      = new String(param.readableName());
						String leftShortName = new String(param.shortReadableName());
						this.handle(
								IProblem.IllegalCast,
								new String[] { rightName, leftName },
								new String[] { rightShortName, leftShortName },
								messageSend.sourceStart,
								messageSend.sourceEnd);
						return;
					}
					// illegal team anchor used?
					if (messageSend.arguments != null) {
						for (Expression arg : messageSend.arguments) {
							if (arg.resolvedType instanceof RoleTypeBinding) {
								RoleTypeBinding rtb= (RoleTypeBinding)arg.resolvedType;
								if (rtb._teamAnchor.problemId() == IProblem.AnchorNotFinal) {
									anchorPathNotFinal(arg, rtb._teamAnchor, rtb.sourceName());
									return;
								}
							}
						}
					}
// SH}
			    	shownMethod = problemMethod.closestMatch;
			    	if ((shownMethod.tagBits & TagBits.HasMissingType) != 0) {
						missingTypeInMethod(messageSend, shownMethod);
						return;
			    	}
					String closestParameterTypeNames = typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false);
					String parameterTypeNames = typesAsString(false, problemMethod.parameters, false);
					String closestParameterTypeShortNames = typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true);
					String parameterTypeShortNames = typesAsString(false, problemMethod.parameters, true);
					this.handle(
						IProblem.ParameterMismatch,
						new String[] {
							new String(shownMethod.declaringClass.readableName()),
							new String(shownMethod.selector),
							closestParameterTypeNames,
							parameterTypeNames
						},
						new String[] {
							new String(shownMethod.declaringClass.shortReadableName()),
							new String(shownMethod.selector),
							closestParameterTypeShortNames,
							parameterTypeShortNames
						},
						(int) (messageSend.nameSourcePosition >>> 32),
						(int) messageSend.nameSourcePosition);
					return;
			}
			break;
		case ProblemReasons.NotVisible :
			id = IProblem.NotVisibleMethod;
			problemMethod = (ProblemMethodBinding) method;
			if (problemMethod.closestMatch != null) {
			    shownMethod = problemMethod.closestMatch.original();
		    }
//{ObjectTeams: more specific message
			if (method.declaringClass.isRole()) {
				TypeBinding receiverType= messageSend.actualReceiverType;
				if (   messageSend instanceof TSuperMessageSend
					&& ((ReferenceBinding)receiverType).getRealType() != method.declaringClass.getRealType())
					id = IProblem.IndirectTSuperInvisible;
				else if (receiverType != null && RoleTypeBinding.isRoleWithExplicitAnchor(receiverType))
					id= IProblem.ExternalizedCallToNonPublicMethod;
				else
					id = IProblem.NotVisibleRoleMethod;
			}
// SH}
			break;
		case ProblemReasons.Ambiguous :
			id = IProblem.AmbiguousMethod;
			break;
		case ProblemReasons.InheritedNameHidesEnclosingName :
			id = IProblem.InheritedMethodHidesEnclosingName;
			break;
		case ProblemReasons.NonStaticReferenceInConstructorInvocation :
			id = IProblem.InstanceMethodDuringConstructorInvocation;
			break;
		case ProblemReasons.NonStaticReferenceInStaticContext :
			id = IProblem.StaticMethodRequested;
			break;
		case ProblemReasons.ReceiverTypeNotVisible :
			this.handle(
				IProblem.NotVisibleType,	// cannot occur in javadoc comments
				new String[] {new String(method.declaringClass.readableName())},
				new String[] {new String(method.declaringClass.shortReadableName())},
				messageSend.receiver.sourceStart,
				messageSend.receiver.sourceEnd);
			return;
		case ProblemReasons.ParameterBoundMismatch :
			problemMethod = (ProblemMethodBinding) method;
			ParameterizedGenericMethodBinding substitutedMethod = (ParameterizedGenericMethodBinding) problemMethod.closestMatch;
			shownMethod = substitutedMethod.original();
			int augmentedLength = problemMethod.parameters.length;
			TypeBinding inferredTypeArgument = problemMethod.parameters[augmentedLength-2];
			TypeVariableBinding typeParameter = (TypeVariableBinding) problemMethod.parameters[augmentedLength-1];
			TypeBinding[] invocationArguments = new TypeBinding[augmentedLength-2]; // remove extra info from the end
			System.arraycopy(problemMethod.parameters, 0, invocationArguments, 0, augmentedLength-2);
//{ObjectTeams: <B base R>:
			if (typeParameter.roletype != null) {
				this.handle(
					IProblem.GenericMethodTypeArgumentMismatchRoleBound,
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
					        new String(shownMethod.declaringClass.readableName()),
					        typesAsString(false, invocationArguments, false),
					        new String(inferredTypeArgument.readableName()),
					        new String(typeParameter.sourceName),
					        new String(typeParameter.roletype.readableName()) },
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
					        new String(shownMethod.declaringClass.shortReadableName()),
					        typesAsString(false, invocationArguments, true),
					        new String(inferredTypeArgument.shortReadableName()),
					        new String(typeParameter.sourceName),
					        new String(typeParameter.roletype.shortReadableName()) },
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);
				return;
			}
// SH}
			this.handle(
				IProblem.GenericMethodTypeArgumentMismatch,
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
				        new String(shownMethod.declaringClass.readableName()),
				        typesAsString(false, invocationArguments, false),
				        new String(inferredTypeArgument.readableName()),
				        new String(typeParameter.sourceName),
				        parameterBoundAsString(typeParameter, false) },
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
				        new String(shownMethod.declaringClass.shortReadableName()),
				        typesAsString(false, invocationArguments, true),
				        new String(inferredTypeArgument.shortReadableName()),
				        new String(typeParameter.sourceName),
				        parameterBoundAsString(typeParameter, true) },
				(int) (messageSend.nameSourcePosition >>> 32),
				(int) messageSend.nameSourcePosition);
			return;
		case ProblemReasons.TypeParameterArityMismatch :
			problemMethod = (ProblemMethodBinding) method;
			shownMethod = problemMethod.closestMatch;
			if (shownMethod.typeVariables == Binding.NO_TYPE_VARIABLES) {
				this.handle(
					IProblem.NonGenericMethod ,
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
					        new String(shownMethod.declaringClass.readableName()),
					        typesAsString(method.isVarargs(), method.parameters, false) },
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
					        new String(shownMethod.declaringClass.shortReadableName()),
					        typesAsString(method.isVarargs(), method.parameters, true) },
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);
			} else {
				this.handle(
					IProblem.IncorrectArityForParameterizedMethod  ,
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
					        new String(shownMethod.declaringClass.readableName()),
							typesAsString(false, shownMethod.typeVariables, false),
					        typesAsString(method.isVarargs(), method.parameters, false) },
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
					        new String(shownMethod.declaringClass.shortReadableName()),
							typesAsString(false, shownMethod.typeVariables, true),
					        typesAsString(method.isVarargs(), method.parameters, true) },
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);
			}
			return;
		case ProblemReasons.ParameterizedMethodTypeMismatch :
			problemMethod = (ProblemMethodBinding) method;
			shownMethod = problemMethod.closestMatch;
			this.handle(
				IProblem.ParameterizedMethodArgumentTypeMismatch,
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
				        new String(shownMethod.declaringClass.readableName()),
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownMethod).typeArguments, false),
				        typesAsString(method.isVarargs(), method.parameters, false) },
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
				        new String(shownMethod.declaringClass.shortReadableName()),
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownMethod).typeArguments, true),
				        typesAsString(method.isVarargs(), method.parameters, true) },
				(int) (messageSend.nameSourcePosition >>> 32),
				(int) messageSend.nameSourcePosition);
			return;
		case ProblemReasons.TypeArgumentsForRawGenericMethod :
			problemMethod = (ProblemMethodBinding) method;
			shownMethod = problemMethod.closestMatch;
			this.handle(
				IProblem.TypeArgumentsForRawGenericMethod ,
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
				        new String(shownMethod.declaringClass.readableName()),
				        typesAsString(method.isVarargs(), method.parameters, false) },
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
				        new String(shownMethod.declaringClass.shortReadableName()),
				        typesAsString(method.isVarargs(), method.parameters, true) },
				(int) (messageSend.nameSourcePosition >>> 32),
				(int) messageSend.nameSourcePosition);
			return;
//{ObjectTeams: more reasons:
		case ProblemReasons.ProblemAlreadyReported:
			return; // don't report again.
// SH}
		case ProblemReasons.NoError : // 0
		default :
			needImplementation(messageSend); // want to fail to see why we were here...
			break;
	}
//{ObjectTeams: undefined, because not in the interface part?
	if (id == IProblem.UndefinedMethod) {
		ReferenceBinding declaringClass = method.declaringClass;
		if (declaringClass.isRole())
		{
			ReferenceBinding roleClass = declaringClass.getRealClass();
			if (roleClass != null) {
				MethodBinding[] methods = roleClass.getMethods(method.selector);
				Scope scope = null;
				if (this.referenceContext instanceof AbstractMethodDeclaration) {
					scope = ((AbstractMethodDeclaration)this.referenceContext).scope;
				} else if (this.referenceContext instanceof AbstractMethodMappingDeclaration) {
					scope = ((AbstractMethodMappingDeclaration)this.referenceContext).scope;
				} else if (this.referenceContext instanceof TypeDeclaration) {
					scope = ((TypeDeclaration)this.referenceContext).scope;
				}
				if (scope != null) {
					for (int i = 0; i < methods.length; i++) {
						if (scope.parameterCompatibilityLevel(methods[i], method.parameters) == Scope.COMPATIBLE)
						{
							((ProblemMethodBinding)method).closestMatch = methods[i];
							id = IProblem.NotVisibleRoleMethod;
							break;
						}
					}
				}
			}
		}
	}
	// ignore problems regarding generated methods in converted types and types with errors
	boolean typeIsBogus;
	try {
		typeIsBogus= TypeModel.isConverted(method.declaringClass);
	} catch (Throwable t) { // resilience (saw NPE due to missing ifc-ast)
		typeIsBogus= true;
	}
	if (!typeIsBogus && this.referenceContext instanceof AbstractMethodDeclaration)
	{
		Scope scope = ((AbstractMethodDeclaration)this.referenceContext).scope;
		typeIsBogus = scope.referenceType().ignoreFurtherInvestigation;
	}
	if (   typeIsBogus
		&& CharOperation.prefixEquals(IOTConstants.OT_DOLLAR_NAME, method.selector))
	{
		this.referenceContext.tagAsHavingErrors(); // avoid generating bogus code
		return; // silently
	}
//SH}

	this.handle(
		id,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(shownMethod.selector), typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false)},
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(shownMethod.selector), typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true)},
		(int) (messageSend.nameSourcePosition >>> 32),
		(int) messageSend.nameSourcePosition);
}
public void invalidNullToSynchronize(Expression expression) {
	this.handle(
		IProblem.InvalidNullToSynchronized,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidOperator(BinaryExpression expression, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.InvalidOperator,
		new String[] {
			expression.operatorToString(),
			leftName + ", " + rightName}, //$NON-NLS-1$
		new String[] {
			expression.operatorToString(),
			leftShortName + ", " + rightShortName}, //$NON-NLS-1$
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidOperator(CompoundAssignment assign, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.InvalidOperator,
		new String[] {
			assign.operatorToString(),
			leftName + ", " + rightName}, //$NON-NLS-1$
		new String[] {
			assign.operatorToString(),
			leftShortName + ", " + rightShortName}, //$NON-NLS-1$
		assign.sourceStart,
		assign.sourceEnd);
}
public void invalidOperator(UnaryExpression expression, TypeBinding type) {
	this.handle(
		IProblem.InvalidOperator,
		new String[] {expression.operatorToString(), new String(type.readableName())},
		new String[] {expression.operatorToString(), new String(type.shortReadableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidParameterizedExceptionType(TypeBinding exceptionType, ASTNode location) {
	this.handle(
		IProblem.InvalidParameterizedExceptionType,
		new String[] {new String(exceptionType.readableName())},
		new String[] {new String(exceptionType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void invalidParenthesizedExpression(ASTNode reference) {
	this.handle(
		IProblem.InvalidParenthesizedExpression,
		NoArgument,
		NoArgument,
		reference.sourceStart,
		reference.sourceEnd);
}
public void invalidType(ASTNode location, TypeBinding type) {
	if (type instanceof ReferenceBinding) {
		if (isRecoveredName(((ReferenceBinding)type).compoundName)) return;
	}
	else if (type instanceof ArrayBinding) {
		TypeBinding leafType = ((ArrayBinding)type).leafComponentType;
		if (leafType instanceof ReferenceBinding) {
			if (isRecoveredName(((ReferenceBinding)leafType).compoundName)) return;
		}
	}

	if (type.isParameterizedType()) {
		List missingTypes = type.collectMissingTypes(null);
		if (missingTypes != null) {
			for (Iterator iterator = missingTypes.iterator(); iterator.hasNext(); ) {
				invalidType(location, (TypeBinding) iterator.next());
			}
			return;
		}
	}
	int id = IProblem.UndefinedType; // default
	switch (type.problemId()) {
		case ProblemReasons.NotFound :
			id = IProblem.UndefinedType;
			break;
		case ProblemReasons.NotVisible :
			id = IProblem.NotVisibleType;
//{ObjectTeams: externalized role?
			if (type instanceof ProblemReferenceBinding)
			{
				TypeBinding closestMatch = ((ProblemReferenceBinding)type).closestMatch();
				if (closestMatch instanceof RoleTypeBinding) {
					RoleTypeBinding rtb = (RoleTypeBinding)closestMatch;
					if (   rtb.hasExplicitAnchor()
						&& !rtb.isPublic())
					{
						if (location instanceof Expression) {
							Expression expr = (Expression)location;
							DecapsulationState decapsulation =
								expr.getBaseclassDecapsulation(rtb);
							if (decapsulation == DecapsulationState.CONFINED) {
								decapsulatingConfined(expr, rtb);
								if (expr instanceof TypeReference)
									((TypeReference)expr).setBaseclassDecapsulation(DecapsulationState.CONFINED);
								return;
							}
						}
						id = IProblem.ExternalizingNonPublicRole;
					}
				}
			}
// SH}
			break;
		case ProblemReasons.Ambiguous :
			id = IProblem.AmbiguousType;
			break;
		case ProblemReasons.InternalNameProvided :
			id = IProblem.InternalTypeNameProvided;
			break;
		case ProblemReasons.InheritedNameHidesEnclosingName :
			id = IProblem.InheritedTypeHidesEnclosingName;
			break;
		case ProblemReasons.NonStaticReferenceInStaticContext :
			id = IProblem.NonStaticTypeFromStaticInvocation;
		    break;
		case ProblemReasons.IllegalSuperTypeVariable :
		    id = IProblem.IllegalTypeVariableSuperReference;
		    break;
//{ObjectTeams:
		case ProblemReasons.AnchorNotFinal:
			ITeamAnchor anchor = null;
			if (type instanceof ProblemReferenceBinding)
				anchor = ((ProblemReferenceBinding)type).anchor;
			anchorPathNotFinal(location, anchor, type.readableName());
			return;
		case ProblemReasons.AnchorNotATeam:
			if (hasBaseQualifier(location))
				id= IProblem.BaseQualifiesNonTeam;
			else
				id = IProblem.IllegalTypeAnchorNotATeam;
			break;
		case ProblemReasons.AnchorNotFound:
			id = IProblem.UnresolvedTypeAnchor;
			break;
		case ProblemReasons.NoTeamContext:
			id = IProblem.NoTeamAnchorInScope;
			break;
		case ProblemReasons.ProblemAlreadyReported:
			return;
// SH}
		case ProblemReasons.NoError : // 0
		default :
			needImplementation(location); // want to fail to see why we were here...
			break;
	}

	int end = location.sourceEnd;
	if (location instanceof QualifiedNameReference) {
		QualifiedNameReference ref = (QualifiedNameReference) location;
		if (isRecoveredName(ref.tokens)) return;
		if (ref.indexOfFirstFieldBinding >= 1)
			end = (int) ref.sourcePositions[ref.indexOfFirstFieldBinding - 1];
	} else if (location instanceof ParameterizedQualifiedTypeReference) {
		// must be before instanceof ArrayQualifiedTypeReference
		ParameterizedQualifiedTypeReference ref = (ParameterizedQualifiedTypeReference) location;
		if (isRecoveredName(ref.tokens)) return;
		if (type instanceof ReferenceBinding) {
			char[][] name = ((ReferenceBinding) type).compoundName;
			end = (int) ref.sourcePositions[name.length - 1];
		}
	} else if (location instanceof ArrayQualifiedTypeReference) {
		ArrayQualifiedTypeReference arrayQualifiedTypeReference = (ArrayQualifiedTypeReference) location;
		if (isRecoveredName(arrayQualifiedTypeReference.tokens)) return;
		TypeBinding leafType = type.leafComponentType();
		if (leafType instanceof ReferenceBinding) {
			char[][] name = ((ReferenceBinding) leafType).compoundName; // problem type will tell how much got resolved
			end = (int) arrayQualifiedTypeReference.sourcePositions[name.length-1];
		} else {
			long[] positions = arrayQualifiedTypeReference.sourcePositions;
			end = (int) positions[positions.length - 1];
		}
	} else if (location instanceof QualifiedTypeReference) {
		QualifiedTypeReference ref = (QualifiedTypeReference) location;
		if (isRecoveredName(ref.tokens)) return;
		if (type instanceof ReferenceBinding) {
			char[][] name = ((ReferenceBinding) type).compoundName;
			if (name.length <= ref.sourcePositions.length)
				end = (int) ref.sourcePositions[name.length - 1];
		}
	} else if (location instanceof ImportReference) {
		ImportReference ref = (ImportReference) location;
		if (isRecoveredName(ref.tokens)) return;
		if (type instanceof ReferenceBinding) {
			char[][] name = ((ReferenceBinding) type).compoundName;
			end = (int) ref.sourcePositions[name.length - 1];
		}
	} else if (location instanceof ArrayTypeReference) {
		ArrayTypeReference arrayTypeReference = (ArrayTypeReference) location;
		if (isRecoveredName(arrayTypeReference.token)) return;
		end = arrayTypeReference.originalSourceEnd;
	}
	this.handle(
		id,
		new String[] {new String(type.leafComponentType().readableName()) },
		new String[] {new String(type.leafComponentType().shortReadableName())},
		location.sourceStart,
		end);
}
public void invalidTypeForCollection(Expression expression) {
	this.handle(
			IProblem.InvalidTypeForCollection,
			NoArgument,
			NoArgument,
			expression.sourceStart,
			expression.sourceEnd);
}
public void invalidTypeForCollectionTarget14(Expression expression) {
	this.handle(
			IProblem.InvalidTypeForCollectionTarget14,
			NoArgument,
			NoArgument,
			expression.sourceStart,
			expression.sourceEnd);
}
public void invalidTypeReference(Expression expression) {
	this.handle(
		IProblem.InvalidTypeExpression,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidTypeToSynchronize(Expression expression, TypeBinding type) {
	this.handle(
		IProblem.InvalidTypeToSynchronized,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidTypeVariableAsException(TypeBinding exceptionType, ASTNode location) {
	this.handle(
		IProblem.InvalidTypeVariableExceptionType,
		new String[] {new String(exceptionType.readableName())},
		new String[] {new String(exceptionType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void invalidUnaryExpression(Expression expression) {
	this.handle(
		IProblem.InvalidUnaryExpression,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidUsageOfAnnotation(Annotation annotation) {
	this.handle(
		IProblem.InvalidUsageOfAnnotations,
		NoArgument,
		NoArgument,
		annotation.sourceStart,
		annotation.sourceEnd);
}
public void invalidUsageOfAnnotationDeclarations(TypeDeclaration annotationTypeDeclaration) {
	this.handle(
		IProblem.InvalidUsageOfAnnotationDeclarations,
		NoArgument,
		NoArgument,
		annotationTypeDeclaration.sourceStart,
		annotationTypeDeclaration.sourceEnd);
}
public void invalidUsageOfEnumDeclarations(TypeDeclaration enumDeclaration) {
	this.handle(
		IProblem.InvalidUsageOfEnumDeclarations,
		NoArgument,
		NoArgument,
		enumDeclaration.sourceStart,
		enumDeclaration.sourceEnd);
}
public void invalidUsageOfForeachStatements(LocalDeclaration elementVariable, Expression collection) {
	this.handle(
		IProblem.InvalidUsageOfForeachStatements,
		NoArgument,
		NoArgument,
		elementVariable.declarationSourceStart,
		collection.sourceEnd);
}
public void invalidUsageOfStaticImports(ImportReference staticImport) {
	this.handle(
		IProblem.InvalidUsageOfStaticImports,
		NoArgument,
		NoArgument,
		staticImport.declarationSourceStart,
		staticImport.declarationSourceEnd);
}
public void invalidUsageOfTypeArguments(TypeReference firstTypeReference, TypeReference lastTypeReference) {
	this.handle(
		IProblem.InvalidUsageOfTypeArguments,
		NoArgument,
		NoArgument,
		firstTypeReference.sourceStart,
		lastTypeReference.sourceEnd);
}
public void invalidUsageOfTypeParameters(TypeParameter firstTypeParameter, TypeParameter lastTypeParameter) {
	this.handle(
		IProblem.InvalidUsageOfTypeParameters,
		NoArgument,
		NoArgument,
		firstTypeParameter.declarationSourceStart,
		lastTypeParameter.declarationSourceEnd);
}
public void invalidUsageOfTypeParametersForAnnotationDeclaration(TypeDeclaration annotationTypeDeclaration) {
	TypeParameter[] parameters = annotationTypeDeclaration.typeParameters;
	int length = parameters.length;
	this.handle(
			IProblem.InvalidUsageOfTypeParametersForAnnotationDeclaration,
			NoArgument,
			NoArgument,
			parameters[0].declarationSourceStart,
			parameters[length - 1].declarationSourceEnd);
}
public void invalidUsageOfTypeParametersForEnumDeclaration(TypeDeclaration annotationTypeDeclaration) {
	TypeParameter[] parameters = annotationTypeDeclaration.typeParameters;
	int length = parameters.length;
	this.handle(
			IProblem.InvalidUsageOfTypeParametersForEnumDeclaration,
			NoArgument,
			NoArgument,
			parameters[0].declarationSourceStart,
			parameters[length - 1].declarationSourceEnd);
}
public void invalidUsageOfVarargs(Argument argument) {
	this.handle(
		IProblem.InvalidUsageOfVarargs,
		NoArgument,
		NoArgument,
		argument.type.sourceStart,
		argument.sourceEnd);
}
public void isClassPathCorrect(char[][] wellKnownTypeName, CompilationUnitDeclaration compUnitDecl, Object location) {
	this.referenceContext = compUnitDecl;
	String[] arguments = new String[] {CharOperation.toString(wellKnownTypeName)};
	int start = 0, end = 0;
	if (location != null) {
		if (location instanceof InvocationSite) {
			InvocationSite site = (InvocationSite) location;
			start = site.sourceStart();
			end = site.sourceEnd();
		} else if (location instanceof ASTNode) {
			ASTNode node = (ASTNode) location;
			start = node.sourceStart();
			end = node.sourceEnd();
		}
	}
	this.handle(
		IProblem.IsClassPathCorrect,
		arguments,
		arguments,
		start,
		end);
}
private boolean isIdentifier(int token) {
	return token == TerminalTokens.TokenNameIdentifier;
}
private boolean isKeyword(int token) {
	switch(token) {
		case TerminalTokens.TokenNameabstract:
		case TerminalTokens.TokenNameassert:
		case TerminalTokens.TokenNamebyte:
		case TerminalTokens.TokenNamebreak:
		case TerminalTokens.TokenNameboolean:
		case TerminalTokens.TokenNamecase:
		case TerminalTokens.TokenNamechar:
		case TerminalTokens.TokenNamecatch:
		case TerminalTokens.TokenNameclass:
		case TerminalTokens.TokenNamecontinue:
		case TerminalTokens.TokenNamedo:
		case TerminalTokens.TokenNamedouble:
		case TerminalTokens.TokenNamedefault:
		case TerminalTokens.TokenNameelse:
		case TerminalTokens.TokenNameextends:
		case TerminalTokens.TokenNamefor:
		case TerminalTokens.TokenNamefinal:
		case TerminalTokens.TokenNamefloat:
		case TerminalTokens.TokenNamefalse:
		case TerminalTokens.TokenNamefinally:
		case TerminalTokens.TokenNameif:
		case TerminalTokens.TokenNameint:
		case TerminalTokens.TokenNameimport:
		case TerminalTokens.TokenNameinterface:
		case TerminalTokens.TokenNameimplements:
		case TerminalTokens.TokenNameinstanceof:
		case TerminalTokens.TokenNamelong:
		case TerminalTokens.TokenNamenew:
		case TerminalTokens.TokenNamenull:
		case TerminalTokens.TokenNamenative:
		case TerminalTokens.TokenNamepublic:
		case TerminalTokens.TokenNamepackage:
		case TerminalTokens.TokenNameprivate:
		case TerminalTokens.TokenNameprotected:
		case TerminalTokens.TokenNamereturn:
		case TerminalTokens.TokenNameshort:
		case TerminalTokens.TokenNamesuper:
		case TerminalTokens.TokenNamestatic:
		case TerminalTokens.TokenNameswitch:
		case TerminalTokens.TokenNamestrictfp:
		case TerminalTokens.TokenNamesynchronized:
		case TerminalTokens.TokenNametry:
		case TerminalTokens.TokenNamethis:
		case TerminalTokens.TokenNametrue:
		case TerminalTokens.TokenNamethrow:
		case TerminalTokens.TokenNamethrows:
		case TerminalTokens.TokenNametransient:
		case TerminalTokens.TokenNamevoid:
		case TerminalTokens.TokenNamevolatile:
		case TerminalTokens.TokenNamewhile:
			return true;
		default:
			return false;
	}
}
private boolean isLiteral(int token) {
	switch(token) {
		case TerminalTokens.TokenNameIntegerLiteral:
		case TerminalTokens.TokenNameLongLiteral:
		case TerminalTokens.TokenNameFloatingPointLiteral:
		case TerminalTokens.TokenNameDoubleLiteral:
		case TerminalTokens.TokenNameStringLiteral:
		case TerminalTokens.TokenNameCharacterLiteral:
			return true;
		default:
			return false;
	}
}

private boolean isRecoveredName(char[] simpleName) {
	return simpleName == RecoveryScanner.FAKE_IDENTIFIER;
}

private boolean isRecoveredName(char[][] qualifiedName) {
	if(qualifiedName == null) return false;
	for (int i = 0; i < qualifiedName.length; i++) {
		if(qualifiedName[i] == RecoveryScanner.FAKE_IDENTIFIER) return true;
	}
	return false;
}

public void javadocAmbiguousMethodReference(int sourceStart, int sourceEnd, Binding fieldBinding, int modifiers) {
	int severity = computeSeverity(IProblem.JavadocAmbiguousMethodReference);
	if (severity == ProblemSeverities.Ignore) return;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {new String(fieldBinding.readableName())};
		handle(
			IProblem.JavadocAmbiguousMethodReference,
			arguments,
			arguments,
			severity,
			sourceStart,
			sourceEnd);
	}
}

public void javadocDeprecatedField(FieldBinding field, ASTNode location, int modifiers) {
	int severity = computeSeverity(IProblem.JavadocUsingDeprecatedField);
	if (severity == ProblemSeverities.Ignore) return;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		this.handle(
			IProblem.JavadocUsingDeprecatedField,
			new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
			new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
			severity,
			nodeSourceStart(field, location),
			nodeSourceEnd(field, location));
	}
}

public void javadocDeprecatedMethod(MethodBinding method, ASTNode location, int modifiers) {
	boolean isConstructor = method.isConstructor();
	int severity = computeSeverity(isConstructor ? IProblem.JavadocUsingDeprecatedConstructor : IProblem.JavadocUsingDeprecatedMethod);
	if (severity == ProblemSeverities.Ignore) return;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		if (isConstructor) {
			this.handle(
				IProblem.JavadocUsingDeprecatedConstructor,
				new String[] {new String(method.declaringClass.readableName()), typesAsString(method.isVarargs(), method.parameters, false)},
				new String[] {new String(method.declaringClass.shortReadableName()), typesAsString(method.isVarargs(), method.parameters, true)},
				severity,
				location.sourceStart,
				location.sourceEnd);
		} else {
			this.handle(
				IProblem.JavadocUsingDeprecatedMethod,
				new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
				new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
				severity,
				location.sourceStart,
				location.sourceEnd);
		}
	}
}
public void javadocDeprecatedType(TypeBinding type, ASTNode location, int modifiers) {
	javadocDeprecatedType(type, location, modifiers, Integer.MAX_VALUE);
}
public void javadocDeprecatedType(TypeBinding type, ASTNode location, int modifiers, int index) {
	if (location == null) return; // 1G828DN - no type ref for synthetic arguments
	int severity = computeSeverity(IProblem.JavadocUsingDeprecatedType);
	if (severity == ProblemSeverities.Ignore) return;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		if (type.isMemberType() && type instanceof ReferenceBinding && !javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, ((ReferenceBinding)type).modifiers)) {
			this.handle(IProblem.JavadocHiddenReference, NoArgument, NoArgument, location.sourceStart, location.sourceEnd);
		} else {
			this.handle(
				IProblem.JavadocUsingDeprecatedType,
				new String[] {new String(type.readableName())},
				new String[] {new String(type.shortReadableName())},
				severity,
				location.sourceStart,
				nodeSourceEnd(null, location, index));
		}
	}
}
public void javadocDuplicatedParamTag(char[] token, int sourceStart, int sourceEnd, int modifiers) {
	int severity = computeSeverity(IProblem.JavadocDuplicateParamName);
	if (severity == ProblemSeverities.Ignore) return;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {String.valueOf(token)};
		this.handle(
			IProblem.JavadocDuplicateParamName,
			arguments,
			arguments,
			severity,
			sourceStart,
			sourceEnd);
	}
}
public void javadocDuplicatedReturnTag(int sourceStart, int sourceEnd){
	this.handle(IProblem.JavadocDuplicateReturnTag, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocDuplicatedTag(char[] tagName, int sourceStart, int sourceEnd){
	String[] arguments = new String[] { new String(tagName) };
	this.handle(
		IProblem.JavadocDuplicateTag,
		arguments,
		arguments,
		sourceStart,
		sourceEnd);
}
public void javadocDuplicatedThrowsClassName(TypeReference typeReference, int modifiers) {
	int severity = computeSeverity(IProblem.JavadocDuplicateThrowsClassName);
	if (severity == ProblemSeverities.Ignore) return;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {String.valueOf(typeReference.resolvedType.sourceName())};
		this.handle(
			IProblem.JavadocDuplicateThrowsClassName,
			arguments,
			arguments,
			severity,
			typeReference.sourceStart,
			typeReference.sourceEnd);
	}
}
public void javadocEmptyReturnTag(int sourceStart, int sourceEnd, int modifiers) {
	int severity = computeSeverity(IProblem.JavadocEmptyReturnTag);
	if (severity == ProblemSeverities.Ignore) return;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] { new String(JavadocTagConstants.TAG_RETURN) };
		this.handle(IProblem.JavadocEmptyReturnTag, arguments, arguments, sourceStart, sourceEnd);
	}
}
public void javadocErrorNoMethodFor(MessageSend messageSend, TypeBinding recType, TypeBinding[] params, int modifiers) {
	int id = recType.isArrayType() ? IProblem.JavadocNoMessageSendOnArrayType : IProblem.JavadocNoMessageSendOnBaseType;
	int severity = computeSeverity(id);
	if (severity == ProblemSeverities.Ignore) return;
	StringBuffer buffer = new StringBuffer();
	StringBuffer shortBuffer = new StringBuffer();
	for (int i = 0, length = params.length; i < length; i++) {
		if (i != 0){
			buffer.append(", "); //$NON-NLS-1$
			shortBuffer.append(", "); //$NON-NLS-1$
		}
		buffer.append(new String(params[i].readableName()));
		shortBuffer.append(new String(params[i].shortReadableName()));
	}
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		this.handle(
			id,
			new String[] {new String(recType.readableName()), new String(messageSend.selector), buffer.toString()},
			new String[] {new String(recType.shortReadableName()), new String(messageSend.selector), shortBuffer.toString()},
			severity,
			messageSend.sourceStart,
			messageSend.sourceEnd);
	}
}
public void javadocHiddenReference(int sourceStart, int sourceEnd, Scope scope, int modifiers) {
	Scope currentScope = scope;
	while (currentScope.parent.kind != Scope.COMPILATION_UNIT_SCOPE ) {
		if (!javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, currentScope.getDeclarationModifiers())) {
			return;
		}
		currentScope = currentScope.parent;
	}
	String[] arguments = new String[] { this.options.getVisibilityString(this.options.reportInvalidJavadocTagsVisibility), this.options.getVisibilityString(modifiers) };
	this.handle(IProblem.JavadocHiddenReference, arguments, arguments, sourceStart, sourceEnd);
}
public void javadocInvalidConstructor(Statement statement, MethodBinding targetConstructor, int modifiers) {

	if (!javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) return;
	int sourceStart = statement.sourceStart;
	int sourceEnd = statement.sourceEnd;
	if (statement instanceof AllocationExpression) {
		AllocationExpression allocation = (AllocationExpression)statement;
		if (allocation.enumConstant != null) {
			sourceStart = allocation.enumConstant.sourceStart;
			sourceEnd = allocation.enumConstant.sourceEnd;
		}
	}
	int id = IProblem.JavadocUndefinedConstructor; //default...
	ProblemMethodBinding problemConstructor = null;
	MethodBinding shownConstructor = null;
	switch (targetConstructor.problemId()) {
		case ProblemReasons.NotFound :
			id = IProblem.JavadocUndefinedConstructor;
			break;
		case ProblemReasons.NotVisible :
			id = IProblem.JavadocNotVisibleConstructor;
			break;
		case ProblemReasons.Ambiguous :
			id = IProblem.JavadocAmbiguousConstructor;
			break;
		case ProblemReasons.ParameterBoundMismatch :
			int severity = computeSeverity(IProblem.JavadocGenericConstructorTypeArgumentMismatch);
			if (severity == ProblemSeverities.Ignore) return;
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			ParameterizedGenericMethodBinding substitutedConstructor = (ParameterizedGenericMethodBinding) problemConstructor.closestMatch;
			shownConstructor = substitutedConstructor.original();

			int augmentedLength = problemConstructor.parameters.length;
			TypeBinding inferredTypeArgument = problemConstructor.parameters[augmentedLength-2];
			TypeVariableBinding typeParameter = (TypeVariableBinding) problemConstructor.parameters[augmentedLength-1];
			TypeBinding[] invocationArguments = new TypeBinding[augmentedLength-2]; // remove extra info from the end
			System.arraycopy(problemConstructor.parameters, 0, invocationArguments, 0, augmentedLength-2);

			this.handle(
				IProblem.JavadocGenericConstructorTypeArgumentMismatch,
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false),
				        new String(shownConstructor.declaringClass.readableName()),
				        typesAsString(false, invocationArguments, false),
				        new String(inferredTypeArgument.readableName()),
				        new String(typeParameter.sourceName),
				        parameterBoundAsString(typeParameter, false) },
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true),
				        new String(shownConstructor.declaringClass.shortReadableName()),
				        typesAsString(false, invocationArguments, true),
				        new String(inferredTypeArgument.shortReadableName()),
				        new String(typeParameter.sourceName),
				        parameterBoundAsString(typeParameter, true) },
				severity,
				sourceStart,
				sourceEnd);
			return;

		case ProblemReasons.TypeParameterArityMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			boolean noTypeVariables = shownConstructor.typeVariables == Binding.NO_TYPE_VARIABLES;
			severity = computeSeverity(noTypeVariables ? IProblem.JavadocNonGenericConstructor : IProblem.JavadocIncorrectArityForParameterizedConstructor);
			if (severity == ProblemSeverities.Ignore) return;
			if (noTypeVariables) {
				this.handle(
					IProblem.JavadocNonGenericConstructor,
					new String[] {
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false),
					        new String(shownConstructor.declaringClass.readableName()),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
					new String[] {
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true),
					        new String(shownConstructor.declaringClass.shortReadableName()),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
					severity,
					sourceStart,
					sourceEnd);
			} else {
				this.handle(
					IProblem.JavadocIncorrectArityForParameterizedConstructor,
					new String[] {
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false),
					        new String(shownConstructor.declaringClass.readableName()),
							typesAsString(false, shownConstructor.typeVariables, false),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
					new String[] {
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true),
					        new String(shownConstructor.declaringClass.shortReadableName()),
							typesAsString(false, shownConstructor.typeVariables, true),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
					severity,
					sourceStart,
					sourceEnd);
			}
			return;
		case ProblemReasons.ParameterizedMethodTypeMismatch :
			severity = computeSeverity(IProblem.JavadocParameterizedConstructorArgumentTypeMismatch);
			if (severity == ProblemSeverities.Ignore) return;
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			this.handle(
				IProblem.JavadocParameterizedConstructorArgumentTypeMismatch,
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false),
				        new String(shownConstructor.declaringClass.readableName()),
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownConstructor).typeArguments, false),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true),
				        new String(shownConstructor.declaringClass.shortReadableName()),
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownConstructor).typeArguments, true),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
				severity,
				sourceStart,
				sourceEnd);
			return;
		case ProblemReasons.TypeArgumentsForRawGenericMethod :
			severity = computeSeverity(IProblem.JavadocTypeArgumentsForRawGenericConstructor);
			if (severity == ProblemSeverities.Ignore) return;
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			this.handle(
				IProblem.JavadocTypeArgumentsForRawGenericConstructor,
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false),
				        new String(shownConstructor.declaringClass.readableName()),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
				new String[] {
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true),
				        new String(shownConstructor.declaringClass.shortReadableName()),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
				severity,
				sourceStart,
				sourceEnd);
			return;
		case ProblemReasons.NoError : // 0
		default :
			needImplementation(statement); // want to fail to see why we were here...
			break;
	}
	int severity = computeSeverity(id);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		id,
		new String[] {new String(targetConstructor.declaringClass.readableName()), typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false)},
		new String[] {new String(targetConstructor.declaringClass.shortReadableName()), typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true)},
		severity,
		statement.sourceStart,
		statement.sourceEnd);
}
/*
 * Similar implementation than invalidField(FieldReference...)
 * Note that following problem id cannot occur for Javadoc:
 * 	- NonStaticReferenceInStaticContext :
 * 	- NonStaticReferenceInConstructorInvocation :
 * 	- ReceiverTypeNotVisible :
 */
public void javadocInvalidField(FieldReference fieldRef, Binding fieldBinding, TypeBinding searchedType, int modifiers) {
	int id = IProblem.JavadocUndefinedField;
	switch (fieldBinding.problemId()) {
		case ProblemReasons.NotFound :
			id = IProblem.JavadocUndefinedField;
			break;
		case ProblemReasons.NotVisible :
			id = IProblem.JavadocNotVisibleField;
			break;
		case ProblemReasons.Ambiguous :
			id = IProblem.JavadocAmbiguousField;
			break;
		case ProblemReasons.NoError : // 0
		default :
			needImplementation(fieldRef); // want to fail to see why we were here...
			break;
	}
	int severity = computeSeverity(id);
	if (severity == ProblemSeverities.Ignore) return;
	// report issue
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {new String(fieldBinding.readableName())};
		handle(
			id,
			arguments,
			arguments,
			severity,
			fieldRef.sourceStart,
			fieldRef.sourceEnd);
	}
}
public void javadocInvalidMemberTypeQualification(int sourceStart, int sourceEnd, int modifiers){
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		this.handle(IProblem.JavadocInvalidMemberTypeQualification, NoArgument, NoArgument, sourceStart, sourceEnd);
	}
}
/*
 * Similar implementation than invalidMethod(MessageSend...)
 * Note that following problem id cannot occur for Javadoc:
 * 	- NonStaticReferenceInStaticContext :
 * 	- NonStaticReferenceInConstructorInvocation :
 * 	- ReceiverTypeNotVisible :
 */
public void javadocInvalidMethod(MessageSend messageSend, MethodBinding method, int modifiers) {
	if (!javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) return;
	// set problem id
	ProblemMethodBinding problemMethod = null;
	MethodBinding shownMethod = null;
	int id = IProblem.JavadocUndefinedMethod; //default...
	switch (method.problemId()) {
		case ProblemReasons.NotFound :
			id = IProblem.JavadocUndefinedMethod;
			problemMethod = (ProblemMethodBinding) method;
			if (problemMethod.closestMatch != null) {
				int severity = computeSeverity(IProblem.JavadocParameterMismatch);
				if (severity == ProblemSeverities.Ignore) return;
				String closestParameterTypeNames = typesAsString(problemMethod.closestMatch.isVarargs(), problemMethod.closestMatch.parameters, false);
				String parameterTypeNames = typesAsString(method.isVarargs(), method.parameters, false);
				String closestParameterTypeShortNames = typesAsString(problemMethod.closestMatch.isVarargs(), problemMethod.closestMatch.parameters, true);
				String parameterTypeShortNames = typesAsString(method.isVarargs(), method.parameters, true);
				if (closestParameterTypeShortNames.equals(parameterTypeShortNames)){
					closestParameterTypeShortNames = closestParameterTypeNames;
					parameterTypeShortNames = parameterTypeNames;
				}
				this.handle(
					IProblem.JavadocParameterMismatch,
					new String[] {
						new String(problemMethod.closestMatch.declaringClass.readableName()),
						new String(problemMethod.closestMatch.selector),
						closestParameterTypeNames,
						parameterTypeNames
					},
					new String[] {
						new String(problemMethod.closestMatch.declaringClass.shortReadableName()),
						new String(problemMethod.closestMatch.selector),
						closestParameterTypeShortNames,
						parameterTypeShortNames
					},
					severity,
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);
				return;
			}
			break;
		case ProblemReasons.NotVisible :
			id = IProblem.JavadocNotVisibleMethod;
			break;
		case ProblemReasons.Ambiguous :
			id = IProblem.JavadocAmbiguousMethod;
			break;
		case ProblemReasons.ParameterBoundMismatch :
			int severity = computeSeverity(IProblem.JavadocGenericMethodTypeArgumentMismatch);
			if (severity == ProblemSeverities.Ignore) return;
			problemMethod = (ProblemMethodBinding) method;
			ParameterizedGenericMethodBinding substitutedMethod = (ParameterizedGenericMethodBinding) problemMethod.closestMatch;
			shownMethod = substitutedMethod.original();
			int augmentedLength = problemMethod.parameters.length;
			TypeBinding inferredTypeArgument = problemMethod.parameters[augmentedLength-2];
			TypeVariableBinding typeParameter = (TypeVariableBinding) problemMethod.parameters[augmentedLength-1];
			TypeBinding[] invocationArguments = new TypeBinding[augmentedLength-2]; // remove extra info from the end
			System.arraycopy(problemMethod.parameters, 0, invocationArguments, 0, augmentedLength-2);
			this.handle(
				IProblem.JavadocGenericMethodTypeArgumentMismatch,
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
				        new String(shownMethod.declaringClass.readableName()),
				        typesAsString(false, invocationArguments, false),
				        new String(inferredTypeArgument.readableName()),
				        new String(typeParameter.sourceName),
				        parameterBoundAsString(typeParameter, false) },
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
				        new String(shownMethod.declaringClass.shortReadableName()),
				        typesAsString(false, invocationArguments, true),
				        new String(inferredTypeArgument.shortReadableName()),
				        new String(typeParameter.sourceName),
				        parameterBoundAsString(typeParameter, true) },
				severity,
				(int) (messageSend.nameSourcePosition >>> 32),
				(int) messageSend.nameSourcePosition);
			return;
		case ProblemReasons.TypeParameterArityMismatch :
			problemMethod = (ProblemMethodBinding) method;
			shownMethod = problemMethod.closestMatch;
			boolean noTypeVariables = shownMethod.typeVariables == Binding.NO_TYPE_VARIABLES;
			severity = computeSeverity(noTypeVariables ? IProblem.JavadocNonGenericMethod : IProblem.JavadocIncorrectArityForParameterizedMethod);
			if (severity == ProblemSeverities.Ignore) return;
			if (noTypeVariables) {
				this.handle(
					IProblem.JavadocNonGenericMethod,
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
					        new String(shownMethod.declaringClass.readableName()),
					        typesAsString(method.isVarargs(), method.parameters, false) },
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
					        new String(shownMethod.declaringClass.shortReadableName()),
					        typesAsString(method.isVarargs(), method.parameters, true) },
					severity,
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);
			} else {
				this.handle(
					IProblem.JavadocIncorrectArityForParameterizedMethod,
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
					        new String(shownMethod.declaringClass.readableName()),
							typesAsString(false, shownMethod.typeVariables, false),
					        typesAsString(method.isVarargs(), method.parameters, false) },
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
					        new String(shownMethod.declaringClass.shortReadableName()),
							typesAsString(false, shownMethod.typeVariables, true),
					        typesAsString(method.isVarargs(), method.parameters, true) },
					severity,
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);
			}
			return;
		case ProblemReasons.ParameterizedMethodTypeMismatch :
			severity = computeSeverity(IProblem.JavadocParameterizedMethodArgumentTypeMismatch);
			if (severity == ProblemSeverities.Ignore) return;
			problemMethod = (ProblemMethodBinding) method;
			shownMethod = problemMethod.closestMatch;
			this.handle(
				IProblem.JavadocParameterizedMethodArgumentTypeMismatch,
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
				        new String(shownMethod.declaringClass.readableName()),
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownMethod).typeArguments, false),
				        typesAsString(method.isVarargs(), method.parameters, false) },
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
				        new String(shownMethod.declaringClass.shortReadableName()),
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownMethod).typeArguments, true),
				        typesAsString(method.isVarargs(), method.parameters, true) },
				severity,
				(int) (messageSend.nameSourcePosition >>> 32),
				(int) messageSend.nameSourcePosition);
			return;
		case ProblemReasons.TypeArgumentsForRawGenericMethod :
			severity = computeSeverity(IProblem.JavadocTypeArgumentsForRawGenericMethod);
			if (severity == ProblemSeverities.Ignore) return;
			problemMethod = (ProblemMethodBinding) method;
			shownMethod = problemMethod.closestMatch;
			this.handle(
				IProblem.JavadocTypeArgumentsForRawGenericMethod,
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
				        new String(shownMethod.declaringClass.readableName()),
				        typesAsString(method.isVarargs(), method.parameters, false) },
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
				        new String(shownMethod.declaringClass.shortReadableName()),
				        typesAsString(method.isVarargs(), method.parameters, true) },
				severity,
				(int) (messageSend.nameSourcePosition >>> 32),
				(int) messageSend.nameSourcePosition);
			return;
		case ProblemReasons.NoError : // 0
		default :
			needImplementation(messageSend); // want to fail to see why we were here...
			break;
	}
	int severity = computeSeverity(id);
	if (severity == ProblemSeverities.Ignore) return;
	// report issue
	this.handle(
		id,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
		severity,
		(int) (messageSend.nameSourcePosition >>> 32),
		(int) messageSend.nameSourcePosition);
}
public void javadocInvalidParamTagName(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidParamTagName, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidParamTypeParameter(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidParamTagTypeParameter, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidReference(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidSeeReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
/**
 * Report an invalid reference that does not conform to the href syntax.
 * Valid syntax example: @see IProblem.JavadocInvalidSeeHref
 */
public void javadocInvalidSeeHref(int sourceStart, int sourceEnd) {
this.handle(IProblem.JavadocInvalidSeeHref, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidSeeReferenceArgs(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidSeeArgs, NoArgument, NoArgument, sourceStart, sourceEnd);
}
/**
 * Report a problem on an invalid URL reference.
 * Valid syntax example: @see IProblem.JavadocInvalidSeeUrlReference
 */
public void javadocInvalidSeeUrlReference(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidSeeUrlReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidTag(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidTag, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidThrowsClass(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidThrowsClass, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidThrowsClassName(TypeReference typeReference, int modifiers) {
	int severity = computeSeverity(IProblem.JavadocInvalidThrowsClassName);
	if (severity == ProblemSeverities.Ignore) return;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {String.valueOf(typeReference.resolvedType.sourceName())};
		this.handle(
			IProblem.JavadocInvalidThrowsClassName,
			arguments,
			arguments,
			severity,
			typeReference.sourceStart,
			typeReference.sourceEnd);
	}
}
public void javadocInvalidType(ASTNode location, TypeBinding type, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		int id = IProblem.JavadocUndefinedType; // default
		switch (type.problemId()) {
			case ProblemReasons.NotFound :
				id = IProblem.JavadocUndefinedType;
				break;
			case ProblemReasons.NotVisible :
				id = IProblem.JavadocNotVisibleType;
				break;
			case ProblemReasons.Ambiguous :
				id = IProblem.JavadocAmbiguousType;
				break;
			case ProblemReasons.InternalNameProvided :
				id = IProblem.JavadocInternalTypeNameProvided;
				break;
			case ProblemReasons.InheritedNameHidesEnclosingName :
				id = IProblem.JavadocInheritedNameHidesEnclosingTypeName;
				break;
			case ProblemReasons.NonStaticReferenceInStaticContext :
				id = IProblem.JavadocNonStaticTypeFromStaticInvocation;
			    break;
			case ProblemReasons.NoError : // 0
			default :
				needImplementation(location); // want to fail to see why we were here...
				break;
		}
		int severity = computeSeverity(id);
		if (severity == ProblemSeverities.Ignore) return;
		this.handle(
			id,
			new String[] {new String(type.readableName())},
			new String[] {new String(type.shortReadableName())},
			severity,
			location.sourceStart,
			location.sourceEnd);
	}
}
public void javadocInvalidValueReference(int sourceStart, int sourceEnd, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers))
		this.handle(IProblem.JavadocInvalidValueReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMalformedSeeReference(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocMalformedSeeReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissing(int sourceStart, int sourceEnd, int modifiers){
	int severity = computeSeverity(IProblem.JavadocMissing);
	this.javadocMissing(sourceStart, sourceEnd, severity, modifiers);
}
public void javadocMissing(int sourceStart, int sourceEnd, int severity, int modifiers){
	if (severity == ProblemSeverities.Ignore) return;
	boolean overriding = (modifiers & (ExtraCompilerModifiers.AccImplementing|ExtraCompilerModifiers.AccOverriding)) != 0;
	boolean report = (this.options.getSeverity(CompilerOptions.MissingJavadocComments) != ProblemSeverities.Ignore)
					&& (!overriding || this.options.reportMissingJavadocCommentsOverriding);
	if (report) {
		String arg = javadocVisibilityArgument(this.options.reportMissingJavadocCommentsVisibility, modifiers);
		if (arg != null) {
			String[] arguments = new String[] { arg };
			this.handle(
				IProblem.JavadocMissing,
				arguments,
				arguments,
				severity,
				sourceStart,
				sourceEnd);
		}
	}
}
public void javadocMissingHashCharacter(int sourceStart, int sourceEnd, String ref){
	int severity = computeSeverity(IProblem.JavadocMissingHashCharacter);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] { ref };
	this.handle(
		IProblem.JavadocMissingHashCharacter,
		arguments,
		arguments,
		severity,
		sourceStart,
		sourceEnd);
}
public void javadocMissingIdentifier(int sourceStart, int sourceEnd, int modifiers){
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers))
		this.handle(IProblem.JavadocMissingIdentifier, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissingParamName(int sourceStart, int sourceEnd, int modifiers){
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers))
		this.handle(IProblem.JavadocMissingParamName, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissingParamTag(char[] name, int sourceStart, int sourceEnd, int modifiers) {
	int severity = computeSeverity(IProblem.JavadocMissingParamTag);
	if (severity == ProblemSeverities.Ignore) return;
	boolean overriding = (modifiers & (ExtraCompilerModifiers.AccImplementing|ExtraCompilerModifiers.AccOverriding)) != 0;
	boolean report = (this.options.getSeverity(CompilerOptions.MissingJavadocTags) != ProblemSeverities.Ignore)
					&& (!overriding || this.options.reportMissingJavadocTagsOverriding);
	if (report && javadocVisibility(this.options.reportMissingJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] { String.valueOf(name) };
		this.handle(
			IProblem.JavadocMissingParamTag,
			arguments,
			arguments,
			severity,
			sourceStart,
			sourceEnd);
	}
}
public void javadocMissingReference(int sourceStart, int sourceEnd, int modifiers){
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers))
		this.handle(IProblem.JavadocMissingSeeReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissingReturnTag(int sourceStart, int sourceEnd, int modifiers){
	boolean overriding = (modifiers & (ExtraCompilerModifiers.AccImplementing|ExtraCompilerModifiers.AccOverriding)) != 0;
	boolean report = (this.options.getSeverity(CompilerOptions.MissingJavadocTags) != ProblemSeverities.Ignore)
					&& (!overriding || this.options.reportMissingJavadocTagsOverriding);
	if (report && javadocVisibility(this.options.reportMissingJavadocTagsVisibility, modifiers)) {
		this.handle(IProblem.JavadocMissingReturnTag, NoArgument, NoArgument, sourceStart, sourceEnd);
	}
}
public void javadocMissingTagDescription(char[] tokenName, int sourceStart, int sourceEnd, int modifiers) {
	int severity = computeSeverity(IProblem.JavadocMissingTagDescription);
	if (severity == ProblemSeverities.Ignore) return;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] { new String(tokenName) };
		// use IProblem.JavadocEmptyReturnTag for all identified tags
		this.handle(IProblem.JavadocEmptyReturnTag, arguments, arguments, sourceStart, sourceEnd);
	}
}
public void javadocMissingTagDescriptionAfterReference(int sourceStart, int sourceEnd, int modifiers){
	int severity = computeSeverity(IProblem.JavadocMissingTagDescription);
	if (severity == ProblemSeverities.Ignore) return;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		this.handle(IProblem.JavadocMissingTagDescription, NoArgument, NoArgument, severity, sourceStart, sourceEnd);
	}
}
public void javadocMissingThrowsClassName(int sourceStart, int sourceEnd, int modifiers){
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		this.handle(IProblem.JavadocMissingThrowsClassName, NoArgument, NoArgument, sourceStart, sourceEnd);
	}
}
public void javadocMissingThrowsTag(TypeReference typeRef, int modifiers){
	int severity = computeSeverity(IProblem.JavadocMissingThrowsTag);
	if (severity == ProblemSeverities.Ignore) return;
	boolean overriding = (modifiers & (ExtraCompilerModifiers.AccImplementing|ExtraCompilerModifiers.AccOverriding)) != 0;
	boolean report = (this.options.getSeverity(CompilerOptions.MissingJavadocTags) != ProblemSeverities.Ignore)
					&& (!overriding || this.options.reportMissingJavadocTagsOverriding);
	if (report && javadocVisibility(this.options.reportMissingJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] { String.valueOf(typeRef.resolvedType.sourceName()) };
		this.handle(
			IProblem.JavadocMissingThrowsTag,
			arguments,
			arguments,
			severity,
			typeRef.sourceStart,
			typeRef.sourceEnd);
	}
}
public void javadocUndeclaredParamTagName(char[] token, int sourceStart, int sourceEnd, int modifiers) {
	int severity = computeSeverity(IProblem.JavadocInvalidParamName);
	if (severity == ProblemSeverities.Ignore) return;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {String.valueOf(token)};
		this.handle(
			IProblem.JavadocInvalidParamName,
			arguments,
			arguments,
			severity,
			sourceStart,
			sourceEnd);
	}
}

public void javadocUnexpectedTag(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocUnexpectedTag, NoArgument, NoArgument, sourceStart, sourceEnd);
}

public void javadocUnexpectedText(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocUnexpectedText, NoArgument, NoArgument, sourceStart, sourceEnd);
}

public void javadocUnterminatedInlineTag(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocUnterminatedInlineTag, NoArgument, NoArgument, sourceStart, sourceEnd);
}

private boolean javadocVisibility(int visibility, int modifiers) {
	if (modifiers < 0) return true;
	switch (modifiers & ExtraCompilerModifiers.AccVisibilityMASK) {
		case ClassFileConstants.AccPublic :
			return true;
		case ClassFileConstants.AccProtected:
			return (visibility != ClassFileConstants.AccPublic);
		case ClassFileConstants.AccDefault:
			return (visibility == ClassFileConstants.AccDefault || visibility == ClassFileConstants.AccPrivate);
		case ClassFileConstants.AccPrivate:
			return (visibility == ClassFileConstants.AccPrivate);
	}
	return true;
}

private String javadocVisibilityArgument(int visibility, int modifiers) {
	String argument = null;
	switch (modifiers & ExtraCompilerModifiers.AccVisibilityMASK) {
		case ClassFileConstants.AccPublic :
			argument = CompilerOptions.PUBLIC;
			break;
		case ClassFileConstants.AccProtected:
			if (visibility != ClassFileConstants.AccPublic) {
				argument = CompilerOptions.PROTECTED;
			}
			break;
		case ClassFileConstants.AccDefault:
			if (visibility == ClassFileConstants.AccDefault || visibility == ClassFileConstants.AccPrivate) {
				argument = CompilerOptions.DEFAULT;
			}
			break;
		case ClassFileConstants.AccPrivate:
			if (visibility == ClassFileConstants.AccPrivate) {
				argument = CompilerOptions.PRIVATE;
			}
			break;
	}
	return argument;
}

public void localVariableHiding(LocalDeclaration local, Binding hiddenVariable, boolean  isSpecialArgHidingField) {
	if (hiddenVariable instanceof LocalVariableBinding) {
		int id = (local instanceof Argument)
				? IProblem.ArgumentHidingLocalVariable
				: IProblem.LocalVariableHidingLocalVariable;
		int severity = computeSeverity(id);
		if (severity == ProblemSeverities.Ignore) return;
		String[] arguments = new String[] {new String(local.name)  };
		this.handle(
			id,
			arguments,
			arguments,
			severity,
			nodeSourceStart(hiddenVariable, local),
			nodeSourceEnd(hiddenVariable, local));
	} else if (hiddenVariable instanceof FieldBinding) {
		if (isSpecialArgHidingField && !this.options.reportSpecialParameterHidingField){
			return;
		}
		int id = (local instanceof Argument)
				? IProblem.ArgumentHidingField
				: IProblem.LocalVariableHidingField;
		int severity = computeSeverity(id);
		if (severity == ProblemSeverities.Ignore) return;
		FieldBinding field = (FieldBinding) hiddenVariable;
		this.handle(
			id,
			new String[] {new String(local.name) , new String(field.declaringClass.readableName()) },
			new String[] {new String(local.name), new String(field.declaringClass.shortReadableName()) },
			severity,
			local.sourceStart,
			local.sourceEnd);
	}
}

public void localVariableNonNullComparedToNull(LocalVariableBinding local, ASTNode location) {
	int severity = computeSeverity(IProblem.NonNullLocalVariableComparisonYieldsFalse);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] {new String(local.name)  };
	this.handle(
		IProblem.NonNullLocalVariableComparisonYieldsFalse,
		arguments,
		arguments,
		severity,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}

public void localVariableNullComparedToNonNull(LocalVariableBinding local, ASTNode location) {
	int severity = computeSeverity(IProblem.NullLocalVariableComparisonYieldsFalse);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] {new String(local.name)  };
	this.handle(
		IProblem.NullLocalVariableComparisonYieldsFalse,
		arguments,
		arguments,
		severity,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}

public void localVariableNullInstanceof(LocalVariableBinding local, ASTNode location) {
	int severity = computeSeverity(IProblem.NullLocalVariableInstanceofYieldsFalse);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] {new String(local.name)  };
	this.handle(
		IProblem.NullLocalVariableInstanceofYieldsFalse,
		arguments,
		arguments,
		severity,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}

public void localVariableNullReference(LocalVariableBinding local, ASTNode location) {
	int severity = computeSeverity(IProblem.NullLocalVariableReference);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] {new String(local.name)  };
//{ObjectTeams: synthetic local variabel (in within statement)?
	if (location instanceof WithinStatement.SubstitutedReference) {
		WithinStatement.SubstitutedReference nameRef = (WithinStatement.SubstitutedReference) location;
		location = nameRef.getExpression();
		if (location instanceof SingleNameReference) // should be, since it resolved to a local variable
			arguments = new String[] { new String(((SingleNameReference)location).token) };
	}
// SH}
	this.handle(
		IProblem.NullLocalVariableReference,
		arguments,
		arguments,
		severity,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}

public void localVariablePotentialNullReference(LocalVariableBinding local, ASTNode location) {
	int severity = computeSeverity(IProblem.PotentialNullLocalVariableReference);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] {new String(local.name)};
//{ObjectTeams: synthetic local variabel (in within statement)?
	if (location instanceof WithinStatement.SubstitutedReference) {
		WithinStatement.SubstitutedReference nameRef = (WithinStatement.SubstitutedReference) location;
		location = nameRef.getExpression();
		if (location instanceof SingleNameReference) // should be, since it resolved to a local variable
			arguments = new String[] { new String(((SingleNameReference)location).token) };
	}
// SH}
	this.handle(
		IProblem.PotentialNullLocalVariableReference,
		arguments,
		arguments,
		severity,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}

public void localVariableRedundantCheckOnNonNull(LocalVariableBinding local, ASTNode location) {
	int severity = computeSeverity(IProblem.RedundantNullCheckOnNonNullLocalVariable);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] {new String(local.name)  };
	this.handle(
		IProblem.RedundantNullCheckOnNonNullLocalVariable,
		arguments,
		arguments,
		severity,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}

public void localVariableRedundantCheckOnNull(LocalVariableBinding local, ASTNode location) {
	int severity = computeSeverity(IProblem.RedundantNullCheckOnNullLocalVariable);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] {new String(local.name)  };
	this.handle(
		IProblem.RedundantNullCheckOnNullLocalVariable,
		arguments,
		arguments,
		severity,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}

public void localVariableRedundantNullAssignment(LocalVariableBinding local, ASTNode location) {
	int severity = computeSeverity(IProblem.RedundantLocalVariableNullAssignment);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] {new String(local.name)  };
	this.handle(
		IProblem.RedundantLocalVariableNullAssignment,
		arguments,
		arguments,
		severity,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}

public void methodMustOverride(AbstractMethodDeclaration method, long complianceLevel) {
	MethodBinding binding = method.binding;
	this.handle(
		complianceLevel == ClassFileConstants.JDK1_5 ? IProblem.MethodMustOverride : IProblem.MethodMustOverrideOrImplement,
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, false), new String(binding.declaringClass.readableName()), },
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, true), new String(binding.declaringClass.shortReadableName()),},
		method.sourceStart,
		method.sourceEnd);
}

public void methodNameClash(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		IProblem.MethodNameClash,
		new String[] {
			new String(currentMethod.selector),
			typesAsString(currentMethod.isVarargs(), currentMethod.parameters, false),
			new String(currentMethod.declaringClass.readableName()),
			typesAsString(inheritedMethod.isVarargs(), inheritedMethod.parameters, false),
			new String(inheritedMethod.declaringClass.readableName()),
		 },
		new String[] {
			new String(currentMethod.selector),
			typesAsString(currentMethod.isVarargs(), currentMethod.parameters, true),
			new String(currentMethod.declaringClass.shortReadableName()),
			typesAsString(inheritedMethod.isVarargs(), inheritedMethod.parameters, true),
			new String(inheritedMethod.declaringClass.shortReadableName()),
		 },
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}

public void methodNeedBody(AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.MethodRequiresBody,
		NoArgument,
		NoArgument,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}

public void methodNeedingNoBody(MethodDeclaration methodDecl) {
	this.handle(
		((methodDecl.modifiers & ClassFileConstants.AccNative) != 0) ? IProblem.BodyForNativeMethod : IProblem.BodyForAbstractMethod,
		NoArgument,
		NoArgument,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}

public void methodWithConstructorName(MethodDeclaration methodDecl) {
	this.handle(
		IProblem.MethodButWithConstructorName,
		NoArgument,
		NoArgument,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}

public void missingDeprecatedAnnotationForField(FieldDeclaration field) {
	int severity = computeSeverity(IProblem.FieldMissingDeprecatedAnnotation);
	if (severity == ProblemSeverities.Ignore) return;
	FieldBinding binding = field.binding;
	this.handle(
		IProblem.FieldMissingDeprecatedAnnotation,
		new String[] {new String(binding.declaringClass.readableName()), new String(binding.name), },
		new String[] {new String(binding.declaringClass.shortReadableName()), new String(binding.name), },
		severity,
		nodeSourceStart(binding, field),
		nodeSourceEnd(binding, field));
}

public void missingDeprecatedAnnotationForMethod(AbstractMethodDeclaration method) {
	int severity = computeSeverity(IProblem.MethodMissingDeprecatedAnnotation);
	if (severity == ProblemSeverities.Ignore) return;
	MethodBinding binding = method.binding;
	this.handle(
		IProblem.MethodMissingDeprecatedAnnotation,
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, false), new String(binding.declaringClass.readableName()), },
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, true), new String(binding.declaringClass.shortReadableName()),},
		severity,
		method.sourceStart,
		method.sourceEnd);
}

public void missingDeprecatedAnnotationForType(TypeDeclaration type) {
	int severity = computeSeverity(IProblem.TypeMissingDeprecatedAnnotation);
	if (severity == ProblemSeverities.Ignore) return;
	TypeBinding binding = type.binding;
	this.handle(
		IProblem.TypeMissingDeprecatedAnnotation,
		new String[] {new String(binding.readableName()), },
		new String[] {new String(binding.shortReadableName()),},
		severity,
		type.sourceStart,
		type.sourceEnd);
}
public void missingEnumConstantCase(SwitchStatement switchStatement, FieldBinding enumConstant) {
	this.handle(
		IProblem.MissingEnumConstantCase,
		new String[] {new String(enumConstant.declaringClass.readableName()), new String(enumConstant.name) },
		new String[] {new String(enumConstant.declaringClass.shortReadableName()), new String(enumConstant.name) },
		switchStatement.expression.sourceStart,
		switchStatement.expression.sourceEnd);
}
public void missingOverrideAnnotation(AbstractMethodDeclaration method) {
//{ObjectTeams: don't report for generated method:
	if (method.isGenerated)
		return;
// SH}
	int severity = computeSeverity(IProblem.MissingOverrideAnnotation);
	if (severity == ProblemSeverities.Ignore) return;
	MethodBinding binding = method.binding;
	this.handle(
		IProblem.MissingOverrideAnnotation,
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, false), new String(binding.declaringClass.readableName()), },
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, true), new String(binding.declaringClass.shortReadableName()),},
		severity,
		method.sourceStart,
		method.sourceEnd);
}
public void missingOverrideAnnotationForInterfaceMethodImplementation(AbstractMethodDeclaration method) {
//{ObjectTeams: don't report for generated method:
	if (method.isGenerated)
		return;
// SH}
	int severity = computeSeverity(IProblem.MissingOverrideAnnotationForInterfaceMethodImplementation);
	if (severity == ProblemSeverities.Ignore) return;
	MethodBinding binding = method.binding;
	this.handle(
		IProblem.MissingOverrideAnnotationForInterfaceMethodImplementation,
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, false), new String(binding.declaringClass.readableName()), },
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, true), new String(binding.declaringClass.shortReadableName()),},
		severity,
		method.sourceStart,
		method.sourceEnd);
}
public void missingReturnType(AbstractMethodDeclaration methodDecl) {
//{ObjectTeams: don't report?
	if (methodDecl.isGenerated) {
		methodDecl.tagAsHavingErrors();
		return;
	}
// SH}
	this.handle(
		IProblem.MissingReturnType,
		NoArgument,
		NoArgument,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void missingSemiColon(Expression expression){
	this.handle(
		IProblem.MissingSemiColon,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void missingSerialVersion(TypeDeclaration typeDecl) {
//{ObjectTeams: beatify role types:
/* orig:
	String[] arguments = new String[] {new String(typeDecl.name)};
  :giro */
	String[] arguments = new String[] {new String(typeDecl.sourceName())};
// SH}
	this.handle(
		IProblem.MissingSerialVersion,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void missingSynchronizedOnInheritedMethod(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
			IProblem.MissingSynchronizedModifierInInheritedMethod,
			new String[] {
					new String(currentMethod.declaringClass.readableName()),
					new String(currentMethod.selector),
					typesAsString(currentMethod.isVarargs(), currentMethod.parameters, false),
			},
			new String[] {
					new String(currentMethod.declaringClass.shortReadableName()),
					new String(currentMethod.selector),
					typesAsString(currentMethod.isVarargs(), currentMethod.parameters, true),
			},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
}
public void missingTypeInConstructor(ASTNode location, MethodBinding constructor) {
	List missingTypes = constructor.collectMissingTypes(null);
	TypeBinding missingType = (TypeBinding) missingTypes.get(0);
	int start = location.sourceStart;
	int end = location.sourceEnd;
	if (location instanceof QualifiedAllocationExpression) {
		QualifiedAllocationExpression qualifiedAllocation = (QualifiedAllocationExpression) location;
		if (qualifiedAllocation.anonymousType != null) {
			start = qualifiedAllocation.anonymousType.sourceStart;
			end = qualifiedAllocation.anonymousType.sourceEnd;
		}
	}
	this.handle(
			IProblem.MissingTypeInConstructor,
			new String[] {
			        new String(constructor.declaringClass.readableName()),
			        typesAsString(constructor.isVarargs(), constructor.parameters, false),
			       	new String(missingType.readableName()),
			},
			new String[] {
			        new String(constructor.declaringClass.shortReadableName()),
			        typesAsString(constructor.isVarargs(), constructor.parameters, true),
			       	new String(missingType.shortReadableName()),
			},
			start,
			end);
}

public void missingTypeInMethod(MessageSend messageSend, MethodBinding method) {
	List missingTypes = method.collectMissingTypes(null);
	TypeBinding missingType = (TypeBinding) missingTypes.get(0);
	this.handle(
			IProblem.MissingTypeInMethod,
			new String[] {
			        new String(method.declaringClass.readableName()),
			        new String(method.selector),
			        typesAsString(method.isVarargs(), method.parameters, false),
			       	new String(missingType.readableName()),
			},
			new String[] {
			        new String(method.declaringClass.shortReadableName()),
			        new String(method.selector),
			        typesAsString(method.isVarargs(), method.parameters, true),
			       	new String(missingType.shortReadableName()),
			},
			(int) (messageSend.nameSourcePosition >>> 32),
			(int) messageSend.nameSourcePosition);
}
public void missingValueForAnnotationMember(Annotation annotation, char[] memberName) {
	String memberString = new String(memberName);
	this.handle(
		IProblem.MissingValueForAnnotationMember,
		new String[] {new String(annotation.resolvedType.readableName()), memberString },
		new String[] {new String(annotation.resolvedType.shortReadableName()), memberString},
		annotation.sourceStart,
		annotation.sourceEnd);
}
public void mustDefineDimensionsOrInitializer(ArrayAllocationExpression expression) {
	this.handle(
		IProblem.MustDefineEitherDimensionExpressionsOrInitializer,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void mustSpecifyPackage(CompilationUnitDeclaration compUnitDecl) {
	String[] arguments = new String[] {new String(compUnitDecl.getFileName())};
	this.handle(
		IProblem.MustSpecifyPackage,
		arguments,
		arguments,
		compUnitDecl.sourceStart,
		compUnitDecl.sourceStart + 1);
}
public void mustUseAStaticMethod(MessageSend messageSend, MethodBinding method) {
	this.handle(
		IProblem.StaticMethodRequested,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void nativeMethodsCannotBeStrictfp(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.NativeMethodsCannotBeStrictfp,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void needImplementation(ASTNode location) {
	this.abortDueToInternalError(Messages.abort_missingCode, location);
}

public void needToEmulateFieldAccess(FieldBinding field, ASTNode location, boolean isReadAccess) {
	int id = isReadAccess
			? IProblem.NeedToEmulateFieldReadAccess
			: IProblem.NeedToEmulateFieldWriteAccess;
	int severity = computeSeverity(id);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		id,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		severity,
		nodeSourceStart(field, location),
		nodeSourceEnd(field, location));
}
public void needToEmulateMethodAccess(
	MethodBinding method,
	ASTNode location) {

	if (method.isConstructor()) {
		int severity = computeSeverity(IProblem.NeedToEmulateConstructorAccess);
		if (severity == ProblemSeverities.Ignore) return;
		if (method.declaringClass.isEnum())
			return; // tolerate emulation for enum constructors, which can only be made private
		this.handle(
			IProblem.NeedToEmulateConstructorAccess,
			new String[] {
				new String(method.declaringClass.readableName()),
				typesAsString(method.isVarargs(), method.parameters, false)
			 },
			new String[] {
				new String(method.declaringClass.shortReadableName()),
				typesAsString(method.isVarargs(), method.parameters, true)
			 },
			severity,
			location.sourceStart,
			location.sourceEnd);
		return;
	}
	int severity = computeSeverity(IProblem.NeedToEmulateMethodAccess);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		IProblem.NeedToEmulateMethodAccess,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(method.selector),
			typesAsString(method.isVarargs(), method.parameters, false)
		 },
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(method.selector),
			typesAsString(method.isVarargs(), method.parameters, true)
		 },
		 severity,
		location.sourceStart,
		location.sourceEnd);
}
public void noAdditionalBoundAfterTypeVariable(TypeReference boundReference) {
	this.handle(
		IProblem.NoAdditionalBoundAfterTypeVariable,
		new String[] { new String(boundReference.resolvedType.readableName()) },
		new String[] { new String(boundReference.resolvedType.shortReadableName()) },
		boundReference.sourceStart,
		boundReference.sourceEnd);
}
private int nodeSourceEnd(Binding field, ASTNode node) {
	return nodeSourceEnd(field, node, 0);
}
private int nodeSourceEnd(Binding field, ASTNode node, int index) {
	if (node instanceof ArrayTypeReference) {
		return ((ArrayTypeReference) node).originalSourceEnd;
	} else if (node instanceof QualifiedNameReference) {
		QualifiedNameReference ref = (QualifiedNameReference) node;
		if (ref.binding == field) {
			if (index == 0) {
				return (int) (ref.sourcePositions[ref.indexOfFirstFieldBinding-1]);
			} else {
				int length = ref.sourcePositions.length;
				if (index < length) {
					return (int) (ref.sourcePositions[index]);
				}
				return (int) (ref.sourcePositions[0]);
			}
		}
		FieldBinding[] otherFields = ref.otherBindings;
		if (otherFields != null) {
			int offset = ref.indexOfFirstFieldBinding;
			if (index != 0) {
				for (int i = 0, length = otherFields.length; i < length; i++) {
					if ((otherFields[i] == field) && (i + offset == index)) {
						return (int) (ref.sourcePositions[i + offset]);
					}
				}
			} else {
				for (int i = 0, length = otherFields.length; i < length; i++) {
					if (otherFields[i] == field)
						return (int) (ref.sourcePositions[i + offset]);
				}
			}
		}
	} else if (node instanceof ParameterizedQualifiedTypeReference) {
		ParameterizedQualifiedTypeReference reference = (ParameterizedQualifiedTypeReference) node;
		if (index < reference.sourcePositions.length) {
			return (int) reference.sourcePositions[index];
		}
	} else if (node instanceof ArrayQualifiedTypeReference) {
		ArrayQualifiedTypeReference reference = (ArrayQualifiedTypeReference) node;
		int length = reference.sourcePositions.length;
		if (index < length) {
			return (int) reference.sourcePositions[index];
		}
		return (int) reference.sourcePositions[length - 1];
	} else if (node instanceof QualifiedTypeReference) {
		QualifiedTypeReference reference = (QualifiedTypeReference) node;
		int length = reference.sourcePositions.length;
		if (index < length) {
			return (int) reference.sourcePositions[index];
		}
	}
	return node.sourceEnd;
}
private int nodeSourceStart(Binding field, ASTNode node) {
	return nodeSourceStart(field, node, 0);
}
private int nodeSourceStart(Binding field, ASTNode node, int index) {
	if (node instanceof FieldReference) {
		FieldReference fieldReference = (FieldReference) node;
		return (int) (fieldReference.nameSourcePosition >> 32);
	} else 	if (node instanceof QualifiedNameReference) {
		QualifiedNameReference ref = (QualifiedNameReference) node;
		if (ref.binding == field) {
			if (index == 0) {
				return (int) (ref.sourcePositions[ref.indexOfFirstFieldBinding-1] >> 32);
			} else {
				return (int) (ref.sourcePositions[index] >> 32);
			}
		}
		FieldBinding[] otherFields = ref.otherBindings;
		if (otherFields != null) {
			int offset = ref.indexOfFirstFieldBinding;
			if (index != 0) {
				for (int i = 0, length = otherFields.length; i < length; i++) {
					if ((otherFields[i] == field) && (i + offset == index)) {
						return (int) (ref.sourcePositions[i + offset] >> 32);
					}
				}
			} else {
				for (int i = 0, length = otherFields.length; i < length; i++) {
					if (otherFields[i] == field) {
						return (int) (ref.sourcePositions[i + offset] >> 32);
					}
				}
			}
		}
	} else if (node instanceof ParameterizedQualifiedTypeReference) {
		ParameterizedQualifiedTypeReference reference = (ParameterizedQualifiedTypeReference) node;
		return (int) (reference.sourcePositions[0]>>>32);
	}
	return node.sourceStart;
}
public void noMoreAvailableSpaceForArgument(LocalVariableBinding local, ASTNode location) {
	String[] arguments = new String[]{ new String(local.name) };
	this.handle(
		local instanceof SyntheticArgumentBinding
			? IProblem.TooManySyntheticArgumentSlots
			: IProblem.TooManyArgumentSlots,
		arguments,
		arguments,
		ProblemSeverities.Abort | ProblemSeverities.Error | ProblemSeverities.Fatal,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}
public void noMoreAvailableSpaceForConstant(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.TooManyBytesForStringConstant,
		new String[]{ new String(typeDeclaration.binding.readableName())},
		new String[]{ new String(typeDeclaration.binding.shortReadableName())},
		ProblemSeverities.Abort | ProblemSeverities.Error | ProblemSeverities.Fatal,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}

public void noMoreAvailableSpaceForLocal(LocalVariableBinding local, ASTNode location) {
	String[] arguments = new String[]{ new String(local.name) };
	this.handle(
		IProblem.TooManyLocalVariableSlots,
		arguments,
		arguments,
		ProblemSeverities.Abort | ProblemSeverities.Error | ProblemSeverities.Fatal,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location));
}
public void noMoreAvailableSpaceInConstantPool(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.TooManyConstantsInConstantPool,
		new String[]{ new String(typeDeclaration.binding.readableName())},
		new String[]{ new String(typeDeclaration.binding.shortReadableName())},
		ProblemSeverities.Abort | ProblemSeverities.Error | ProblemSeverities.Fatal,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}

public void nonExternalizedStringLiteral(ASTNode location) {
	this.handle(
		IProblem.NonExternalizedStringLiteral,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}

public void nonGenericTypeCannotBeParameterized(int index, ASTNode location, TypeBinding type, TypeBinding[] argumentTypes) {
	if (location == null) { // binary case
	    this.handle(
			IProblem.NonGenericType,
			new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false)},
			new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true)},
			ProblemSeverities.AbortCompilation | ProblemSeverities.Error | ProblemSeverities.Fatal,
			0,
			0);
	    return;
	}
    this.handle(
		IProblem.NonGenericType,
		new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false)},
		new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true)},
		nodeSourceStart(null, location),
		nodeSourceEnd(null, location, index));
}
public void nonStaticAccessToStaticField(ASTNode location, FieldBinding field) {
	nonStaticAccessToStaticField(location, field, -1);
}
public void nonStaticAccessToStaticField(ASTNode location, FieldBinding field, int index) {
	int severity = computeSeverity(IProblem.NonStaticAccessToStaticField);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		IProblem.NonStaticAccessToStaticField,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		severity,
		nodeSourceStart(field, location, index),
		nodeSourceEnd(field, location, index));
}
public void nonStaticAccessToStaticMethod(ASTNode location, MethodBinding method) {
	this.handle(
		IProblem.NonStaticAccessToStaticMethod,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
		location.sourceStart,
		location.sourceEnd);
}
public void nonStaticContextForEnumMemberType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.NonStaticContextForEnumMemberType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void noSuchEnclosingInstance(TypeBinding targetType, ASTNode location, boolean isConstructorCall) {

	int id;

	if (isConstructorCall) {
		//28 = No enclosing instance of type {0} is available due to some intermediate constructor invocation
		id = IProblem.EnclosingInstanceInConstructorCall;
	} else if ((location instanceof ExplicitConstructorCall)
				&& ((ExplicitConstructorCall) location).accessMode == ExplicitConstructorCall.ImplicitSuper) {
		//20 = No enclosing instance of type {0} is accessible to invoke the super constructor. Must define a constructor and explicitly qualify its super constructor invocation with an instance of {0} (e.g. x.super() where x is an instance of {0}).
		id = IProblem.MissingEnclosingInstanceForConstructorCall;
	} else if (location instanceof AllocationExpression
				&& (((AllocationExpression) location).binding.declaringClass.isMemberType()
					|| (((AllocationExpression) location).binding.declaringClass.isAnonymousType()
						&& ((AllocationExpression) location).binding.declaringClass.superclass().isMemberType()))) {
		//21 = No enclosing instance of type {0} is accessible. Must qualify the allocation with an enclosing instance of type {0} (e.g. x.new A() where x is an instance of {0}).
		id = IProblem.MissingEnclosingInstance;
	} else { // default
		//22 = No enclosing instance of the type {0} is accessible in scope
		id = IProblem.IncorrectEnclosingInstanceReference;
	}

	this.handle(
		id,
		new String[] { new String(targetType.readableName())},
		new String[] { new String(targetType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void notCompatibleTypesError(EqualExpression expression, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.IncompatibleTypesInEqualityOperator,
		new String[] {leftName, rightName },
		new String[] {leftShortName, rightShortName },
		expression.sourceStart,
		expression.sourceEnd);
}
public void notCompatibleTypesError(InstanceOfExpression expression, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.IncompatibleTypesInConditionalOperator,
		new String[] {leftName, rightName },
		new String[] {leftShortName, rightShortName },
		expression.sourceStart,
		expression.sourceEnd);
}
public void notCompatibleTypesErrorInForeach(Expression expression, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.IncompatibleTypesInForeach,
		new String[] {leftName, rightName },
		new String[] {leftShortName, rightShortName },
		expression.sourceStart,
		expression.sourceEnd);
}
public void objectCannotBeGeneric(TypeDeclaration typeDecl) {
	this.handle(
		IProblem.ObjectCannotBeGeneric,
		NoArgument,
		NoArgument,
		typeDecl.typeParameters[0].sourceStart,
		typeDecl.typeParameters[typeDecl.typeParameters.length-1].sourceEnd);
}
public void objectCannotHaveSuperTypes(SourceTypeBinding type) {
	this.handle(
		IProblem.ObjectCannotHaveSuperTypes,
		NoArgument,
		NoArgument,
		type.sourceStart(),
		type.sourceEnd());
}
public void objectMustBeClass(SourceTypeBinding type) {
	this.handle(
		IProblem.ObjectMustBeClass,
		NoArgument,
		NoArgument,
		type.sourceStart(),
		type.sourceEnd());
}
public void operatorOnlyValidOnNumericType(CompoundAssignment  assignment, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.TypeMismatch,
		new String[] {leftName, rightName },
		new String[] {leftShortName, rightShortName },
		assignment.sourceStart,
		assignment.sourceEnd);
}
public void overridesDeprecatedMethod(MethodBinding localMethod, MethodBinding inheritedMethod) {
	this.handle(
		IProblem.OverridingDeprecatedMethod,
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.readableName(),
						localMethod.readableName(),
						'.')),
			new String(inheritedMethod.declaringClass.readableName())},
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.shortReadableName(),
						localMethod.shortReadableName(),
						'.')),
			new String(inheritedMethod.declaringClass.shortReadableName())},
		localMethod.sourceStart(),
		localMethod.sourceEnd());
}
public void overridesMethodWithoutSuperInvocation(MethodBinding localMethod) {
	this.handle(
		IProblem.OverridingMethodWithoutSuperInvocation,
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.readableName(),
						localMethod.readableName(),
						'.'))
			},
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.shortReadableName(),
						localMethod.shortReadableName(),
						'.'))
			},
		localMethod.sourceStart(),
		localMethod.sourceEnd());
}
public void overridesPackageDefaultMethod(MethodBinding localMethod, MethodBinding inheritedMethod) {
	this.handle(
		IProblem.OverridingNonVisibleMethod,
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.readableName(),
						localMethod.readableName(),
						'.')),
			new String(inheritedMethod.declaringClass.readableName())},
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.shortReadableName(),
						localMethod.shortReadableName(),
						'.')),
			new String(inheritedMethod.declaringClass.shortReadableName())},
		localMethod.sourceStart(),
		localMethod.sourceEnd());
}
public void packageCollidesWithType(CompilationUnitDeclaration compUnitDecl) {
	String[] arguments = new String[] {CharOperation.toString(compUnitDecl.currentPackage.tokens)};
	this.handle(
		IProblem.PackageCollidesWithType,
		arguments,
		arguments,
		compUnitDecl.currentPackage.sourceStart,
		compUnitDecl.currentPackage.sourceEnd);
}
public void packageIsNotExpectedPackage(CompilationUnitDeclaration compUnitDecl) {
	String[] arguments = new String[] {
		CharOperation.toString(compUnitDecl.compilationResult.compilationUnit.getPackageName()),
		compUnitDecl.currentPackage == null ? "" : CharOperation.toString(compUnitDecl.currentPackage.tokens), //$NON-NLS-1$
	};
	this.handle(
		IProblem.PackageIsNotExpectedPackage,
		arguments,
		arguments,
		compUnitDecl.currentPackage == null ? 0 : compUnitDecl.currentPackage.sourceStart,
		compUnitDecl.currentPackage == null ? 0 : compUnitDecl.currentPackage.sourceEnd);
}
public void parameterAssignment(LocalVariableBinding local, ASTNode location) {
	int severity = computeSeverity(IProblem.ParameterAssignment);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] { new String(local.readableName())};
	this.handle(
		IProblem.ParameterAssignment,
		arguments,
		arguments,
		severity,
		nodeSourceStart(local, location),
		nodeSourceEnd(local, location)); // should never be a qualified name reference
}
private String parameterBoundAsString(TypeVariableBinding typeVariable, boolean makeShort) {
    StringBuffer nameBuffer = new StringBuffer(10);
    if (typeVariable.firstBound == typeVariable.superclass) {
        nameBuffer.append(makeShort ? typeVariable.superclass.shortReadableName() : typeVariable.superclass.readableName());
    }
    int length;
    if ((length = typeVariable.superInterfaces.length) > 0) {
	    for (int i = 0; i < length; i++) {
	        if (i > 0 || typeVariable.firstBound == typeVariable.superclass) nameBuffer.append(" & "); //$NON-NLS-1$
	        nameBuffer.append(makeShort ? typeVariable.superInterfaces[i].shortReadableName() : typeVariable.superInterfaces[i].readableName());
	    }
	}
	return nameBuffer.toString();
}
public void parameterizedMemberTypeMissingArguments(ASTNode location, TypeBinding type, int index) {
	if (location == null) { // binary case
	    this.handle(
			IProblem.MissingArgumentsForParameterizedMemberType,
			new String[] {new String(type.readableName())},
			new String[] {new String(type.shortReadableName())},
			ProblemSeverities.AbortCompilation | ProblemSeverities.Error | ProblemSeverities.Fatal,
			0,
			0);
	    return;
	}
    this.handle(
		IProblem.MissingArgumentsForParameterizedMemberType,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		nodeSourceEnd(null, location, index));
}
public void parseError(
	int startPosition,
	int endPosition,
	int currentToken,
	char[] currentTokenSource,
	String errorTokenName,
	String[] possibleTokens) {

	if (possibleTokens.length == 0) { //no suggestion available
		if (isKeyword(currentToken)) {
			String[] arguments = new String[] {new String(currentTokenSource)};
			this.handle(
				IProblem.ParsingErrorOnKeywordNoSuggestion,
				arguments,
				arguments,
				// this is the current -invalid- token position
				startPosition,
				endPosition);
			return;
		} else {
			String[] arguments = new String[] {errorTokenName};
			this.handle(
				IProblem.ParsingErrorNoSuggestion,
				arguments,
				arguments,
				// this is the current -invalid- token position
				startPosition,
				endPosition);
			return;
		}
	}

	//build a list of probable right tokens
	StringBuffer list = new StringBuffer(20);
	for (int i = 0, max = possibleTokens.length; i < max; i++) {
		if (i > 0)
			list.append(", "); //$NON-NLS-1$
		list.append('"');
		list.append(possibleTokens[i]);
		list.append('"');
	}

	if (isKeyword(currentToken)) {
		String[] arguments = new String[] {new String(currentTokenSource), list.toString()};
		this.handle(
			IProblem.ParsingErrorOnKeyword,
			arguments,
			arguments,
			// this is the current -invalid- token position
			startPosition,
			endPosition);
		return;
	}
	//extract the literal when it's a literal
	if (isLiteral(currentToken) ||
		isIdentifier(currentToken)) {
			errorTokenName = new String(currentTokenSource);
	}

	String[] arguments = new String[] {errorTokenName, list.toString()};
	this.handle(
		IProblem.ParsingError,
		arguments,
		arguments,
		// this is the current -invalid- token position
		startPosition,
		endPosition);
}
public void parseErrorDeleteToken(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName){
	syntaxError(
		IProblem.ParsingErrorDeleteToken,
		start,
		end,
		currentKind,
		errorTokenSource,
		errorTokenName,
		null);
}

public void parseErrorDeleteTokens(
	int start,
	int end){
	this.handle(
		IProblem.ParsingErrorDeleteTokens,
		NoArgument,
		NoArgument,
		start,
		end);
}
public void parseErrorInsertAfterToken(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName,
	String expectedToken){
	syntaxError(
		IProblem.ParsingErrorInsertTokenAfter,
		start,
		end,
		currentKind,
		errorTokenSource,
		errorTokenName,
		expectedToken);
}
public void parseErrorInsertBeforeToken(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName,
	String expectedToken){
	syntaxError(
		IProblem.ParsingErrorInsertTokenBefore,
		start,
		end,
		currentKind,
		errorTokenSource,
		errorTokenName,
		expectedToken);
}
public void parseErrorInsertToComplete(
	int start,
	int end,
	String inserted,
	String completed){
	String[] arguments = new String[] {inserted, completed};
	this.handle(
		IProblem.ParsingErrorInsertToComplete,
		arguments,
		arguments,
		start,
		end);
}

public void parseErrorInsertToCompletePhrase(
	int start,
	int end,
	String inserted){
	String[] arguments = new String[] {inserted};
	this.handle(
		IProblem.ParsingErrorInsertToCompletePhrase,
		arguments,
		arguments,
		start,
		end);
}
public void parseErrorInsertToCompleteScope(
	int start,
	int end,
	String inserted){
	String[] arguments = new String[] {inserted};
	this.handle(
		IProblem.ParsingErrorInsertToCompleteScope,
		arguments,
		arguments,
		start,
		end);
}
public void parseErrorInvalidToken(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName,
	String expectedToken){
	syntaxError(
		IProblem.ParsingErrorInvalidToken,
		start,
		end,
		currentKind,
		errorTokenSource,
		errorTokenName,
		expectedToken);
}
public void parseErrorMergeTokens(
	int start,
	int end,
	String expectedToken){
	String[] arguments = new String[] {expectedToken};
	this.handle(
		IProblem.ParsingErrorMergeTokens,
		arguments,
		arguments,
		start,
		end);
}
public void parseErrorMisplacedConstruct(
	int start,
	int end){
	this.handle(
		IProblem.ParsingErrorMisplacedConstruct,
		NoArgument,
		NoArgument,
		start,
		end);
}
public void parseErrorNoSuggestion(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName){
	syntaxError(
		IProblem.ParsingErrorNoSuggestion,
		start,
		end,
		currentKind,
		errorTokenSource,
		errorTokenName,
		null);
}
public void parseErrorNoSuggestionForTokens(
	int start,
	int end){
	this.handle(
		IProblem.ParsingErrorNoSuggestionForTokens,
		NoArgument,
		NoArgument,
		start,
		end);
}
public void parseErrorReplaceToken(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName,
	String expectedToken){
	syntaxError(
		IProblem.ParsingError,
		start,
		end,
		currentKind,
		errorTokenSource,
		errorTokenName,
		expectedToken);
}
public void parseErrorReplaceTokens(
	int start,
	int end,
	String expectedToken){
	String[] arguments = new String[] {expectedToken};
	this.handle(
		IProblem.ParsingErrorReplaceTokens,
		arguments,
		arguments,
		start,
		end);
}
public void parseErrorUnexpectedEnd(
	int start,
	int end){

	String[] arguments;
	if(this.referenceContext instanceof ConstructorDeclaration) {
		arguments = new String[] {Messages.parser_endOfConstructor};
	} else if(this.referenceContext instanceof MethodDeclaration) {
		arguments = new String[] {Messages.parser_endOfMethod};
	} else if(this.referenceContext instanceof TypeDeclaration) {
		arguments = new String[] {Messages.parser_endOfInitializer};
	} else {
		arguments = new String[] {Messages.parser_endOfFile};
	}
	this.handle(
		IProblem.ParsingErrorUnexpectedEOF,
		arguments,
		arguments,
		start,
		end);
}
public void possibleAccidentalBooleanAssignment(Assignment assignment) {
	this.handle(
		IProblem.PossibleAccidentalBooleanAssignment,
		NoArgument,
		NoArgument,
		assignment.sourceStart,
		assignment.sourceEnd);
}
public void possibleFallThroughCase(CaseStatement caseStatement) {
	// as long as we consider fake reachable as reachable, better keep 'possible' in the name
	this.handle(
		IProblem.FallthroughCase,
		NoArgument,
		NoArgument,
		caseStatement.sourceStart,
		caseStatement.sourceEnd);
}
public void publicClassMustMatchFileName(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	String[] arguments = new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)};
	this.handle(
		IProblem.PublicClassMustMatchFileName,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd,
		compUnitDecl.compilationResult);
}
public void rawMemberTypeCannotBeParameterized(ASTNode location, ReferenceBinding type, TypeBinding[] argumentTypes) {
	if (location == null) { // binary case
	    this.handle(
			IProblem.RawMemberTypeCannotBeParameterized,
			new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false), new String(type.enclosingType().readableName())},
			new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true), new String(type.enclosingType().shortReadableName())},
			ProblemSeverities.AbortCompilation | ProblemSeverities.Error | ProblemSeverities.Fatal,
			0,
			0);
	    return;
	}
    this.handle(
		IProblem.RawMemberTypeCannotBeParameterized,
		new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false), new String(type.enclosingType().readableName())},
		new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true), new String(type.enclosingType().shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void rawTypeReference(ASTNode location, TypeBinding type) {
//{ObjectTeams: don't report useless case:
	CompilationResult result = this.referenceContext.compilationResult();
	if (result.problems != null)
		for (IProblem problem : result.problems)
			if (problem != null && problem.getID() == IProblem.ParameterizedBaseclass)
				return; // class R playedBy C<T> has been re-written to .. playedBy C
	if (this.referenceContext instanceof AbstractMethodDeclaration)
		if (((AbstractMethodDeclaration)this.referenceContext).scope.isGeneratedScope())
			return;
// SH}
	type = type.leafComponentType();
    this.handle(
		IProblem.RawTypeReference,
		new String[] {new String(type.readableName()), new String(type.erasure().readableName()), },
		new String[] {new String(type.shortReadableName()),new String(type.erasure().shortReadableName()),},
		location.sourceStart,
		nodeSourceEnd(null, location, Integer.MAX_VALUE));
}
public void recursiveConstructorInvocation(ExplicitConstructorCall constructorCall) {
	this.handle(
		IProblem.RecursiveConstructorInvocation,
		new String[] {
			new String(constructorCall.binding.declaringClass.readableName()),
			typesAsString(constructorCall.binding.isVarargs(), constructorCall.binding.parameters, false)
		},
		new String[] {
			new String(constructorCall.binding.declaringClass.shortReadableName()),
			typesAsString(constructorCall.binding.isVarargs(), constructorCall.binding.parameters, true)
		},
		constructorCall.sourceStart,
		constructorCall.sourceEnd);
}
public void redefineArgument(Argument arg) {
	String[] arguments = new String[] {new String(arg.name)};
	this.handle(
		IProblem.RedefinedArgument,
		arguments,
		arguments,
		arg.sourceStart,
		arg.sourceEnd);
}
public void redefineLocal(LocalDeclaration localDecl) {
	String[] arguments = new String[] {new String(localDecl.name)};
	this.handle(
		IProblem.RedefinedLocal,
		arguments,
		arguments,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void redundantSuperInterface(SourceTypeBinding type, TypeReference reference, ReferenceBinding superinterface, ReferenceBinding declaringType) {
	int severity = computeSeverity(IProblem.RedundantSuperinterface);
	if (severity != ProblemSeverities.Ignore) {
		this.handle(
			IProblem.RedundantSuperinterface,
			new String[] {
				new String(superinterface.readableName()),
				new String(type.readableName()),
				new String(declaringType.readableName())},
			new String[] {
				new String(superinterface.shortReadableName()),
				new String(type.shortReadableName()),
				new String(declaringType.shortReadableName())},
			severity,
			reference.sourceStart,
			reference.sourceEnd);
	}
}
public void referenceMustBeArrayTypeAt(TypeBinding arrayType, ArrayReference arrayRef) {
	this.handle(
		IProblem.ArrayReferenceRequired,
		new String[] {new String(arrayType.readableName())},
		new String[] {new String(arrayType.shortReadableName())},
		arrayRef.sourceStart,
		arrayRef.sourceEnd);
}
public void reset() {
	this.positionScanner = null;
}
private int retrieveClosingAngleBracketPosition(int start) {
	if (this.referenceContext == null) return start;
	CompilationResult compilationResult = this.referenceContext.compilationResult();
	if (compilationResult == null) return start;
	ICompilationUnit compilationUnit = compilationResult.getCompilationUnit();
	if (compilationUnit == null) return start;
	char[] contents = compilationUnit.getContents();
	if (contents.length == 0) return start;
	if (this.positionScanner == null) {
		this.positionScanner = new Scanner(false, false, false, this.options.sourceLevel, this.options.complianceLevel, null, null, false);
		this.positionScanner.returnOnlyGreater = true;
	}
	this.positionScanner.setSource(contents);
	this.positionScanner.resetTo(start, contents.length);
	int end = start;
	int count = 0;
	try {
		int token;
		loop: while ((token = this.positionScanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
			switch(token) {
				case TerminalTokens.TokenNameLESS:
					count++;
					break;
				case TerminalTokens.TokenNameGREATER:
					count--;
					if (count == 0) {
						end = this.positionScanner.currentPosition - 1;
						break loop;
					}
					break;
				case TerminalTokens.TokenNameLBRACE :
					break loop;
			}
		}
	} catch(InvalidInputException e) {
		// ignore
	}
	return end;
}
private int retrieveEndingPositionAfterOpeningParenthesis(int sourceStart, int sourceEnd, int numberOfParen) {
	if (this.referenceContext == null) return sourceEnd;
	CompilationResult compilationResult = this.referenceContext.compilationResult();
	if (compilationResult == null) return sourceEnd;
	ICompilationUnit compilationUnit = compilationResult.getCompilationUnit();
	if (compilationUnit == null) return sourceEnd;
	char[] contents = compilationUnit.getContents();
	if (contents.length == 0) return sourceEnd;
	if (this.positionScanner == null) {
		this.positionScanner = new Scanner(false, false, false, this.options.sourceLevel, this.options.complianceLevel, null, null, false);
	}
	this.positionScanner.setSource(contents);
	this.positionScanner.resetTo(sourceStart, sourceEnd);
	try {
		int token;
		int previousSourceEnd = sourceEnd;
		while ((token = this.positionScanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
			switch(token) {
				case TerminalTokens.TokenNameRPAREN:
					return previousSourceEnd;
				default :
					previousSourceEnd = this.positionScanner.currentPosition - 1;
			}
		}
	} catch(InvalidInputException e) {
		// ignore
	}
	return sourceEnd;
}
private int retrieveStartingPositionAfterOpeningParenthesis(int sourceStart, int sourceEnd, int numberOfParen) {
	if (this.referenceContext == null) return sourceStart;
	CompilationResult compilationResult = this.referenceContext.compilationResult();
	if (compilationResult == null) return sourceStart;
	ICompilationUnit compilationUnit = compilationResult.getCompilationUnit();
	if (compilationUnit == null) return sourceStart;
	char[] contents = compilationUnit.getContents();
	if (contents.length == 0) return sourceStart;
	if (this.positionScanner == null) {
		this.positionScanner = new Scanner(false, false, false, this.options.sourceLevel, this.options.complianceLevel, null, null, false);
	}
	this.positionScanner.setSource(contents);
	this.positionScanner.resetTo(sourceStart, sourceEnd);
	int count = 0;
	try {
		int token;
		while ((token = this.positionScanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
			switch(token) {
				case TerminalTokens.TokenNameLPAREN:
					count++;
					if (count == numberOfParen) {
						this.positionScanner.getNextToken();
						return this.positionScanner.startPosition;
					}
			}
		}
	} catch(InvalidInputException e) {
		// ignore
	}
	return sourceStart;
}
public void returnTypeCannotBeVoidArray(MethodDeclaration methodDecl) {
	this.handle(
		IProblem.CannotAllocateVoidArray,
		NoArgument,
		NoArgument,
		methodDecl.returnType.sourceStart,
		methodDecl.returnType.sourceEnd);
}
public void scannerError(Parser parser, String errorTokenName) {
	Scanner scanner = parser.scanner;

	int flag = IProblem.ParsingErrorNoSuggestion;
	int startPos = scanner.startPosition;
	int endPos = scanner.currentPosition - 1;

	//special treatment for recognized errors....
	if (errorTokenName.equals(Scanner.END_OF_SOURCE))
		flag = IProblem.EndOfSource;
	else if (errorTokenName.equals(Scanner.INVALID_HEXA))
		flag = IProblem.InvalidHexa;
	else if (errorTokenName.equals(Scanner.INVALID_OCTAL))
		flag = IProblem.InvalidOctal;
	else if (errorTokenName.equals(Scanner.INVALID_CHARACTER_CONSTANT))
		flag = IProblem.InvalidCharacterConstant;
	else if (errorTokenName.equals(Scanner.INVALID_ESCAPE))
		flag = IProblem.InvalidEscape;
	else if (errorTokenName.equals(Scanner.INVALID_UNICODE_ESCAPE)){
		flag = IProblem.InvalidUnicodeEscape;
		// better locate the error message
		char[] source = scanner.source;
		int checkPos = scanner.currentPosition - 1;
		if (checkPos >= source.length) checkPos = source.length - 1;
		while (checkPos >= startPos){
			if (source[checkPos] == '\\') break;
			checkPos --;
		}
		startPos = checkPos;
	} else if (errorTokenName.equals(Scanner.INVALID_LOW_SURROGATE)) {
		flag = IProblem.InvalidLowSurrogate;
	} else if (errorTokenName.equals(Scanner.INVALID_HIGH_SURROGATE)) {
		flag = IProblem.InvalidHighSurrogate;
		// better locate the error message
		char[] source = scanner.source;
		int checkPos = scanner.startPosition + 1;
		while (checkPos <= endPos){
			if (source[checkPos] == '\\') break;
			checkPos ++;
		}
		endPos = checkPos - 1;
	} else if (errorTokenName.equals(Scanner.INVALID_FLOAT))
		flag = IProblem.InvalidFloat;
	else if (errorTokenName.equals(Scanner.UNTERMINATED_STRING))
		flag = IProblem.UnterminatedString;
	else if (errorTokenName.equals(Scanner.UNTERMINATED_COMMENT))
		flag = IProblem.UnterminatedComment;
	else if (errorTokenName.equals(Scanner.INVALID_CHAR_IN_STRING))
		flag = IProblem.UnterminatedString;
	else if (errorTokenName.equals(Scanner.INVALID_DIGIT))
		flag = IProblem.InvalidDigit;

	String[] arguments = flag == IProblem.ParsingErrorNoSuggestion
			? new String[] {errorTokenName}
			: NoArgument;
	this.handle(
		flag,
		arguments,
		arguments,
		// this is the current -invalid- token position
		startPos,
		endPos,
		parser.compilationUnit.compilationResult);
}
public void shouldImplementHashcode(SourceTypeBinding type) {	
	this.handle(
		IProblem.ShouldImplementHashcode,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void shouldReturn(TypeBinding returnType, ASTNode location) {
	this.handle(
		IProblem.ShouldReturnValue,
		new String[] { new String (returnType.readableName())},
		new String[] { new String (returnType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}

public void signalNoImplicitStringConversionForCharArrayExpression(Expression expression) {
	this.handle(
		IProblem.NoImplicitStringConversionForCharArrayExpression,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void staticAndInstanceConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	if (currentMethod.isStatic())
		this.handle(
			// This static method cannot hide the instance method from %1
			// 8.4.6.4 - If a class inherits more than one method with the same signature a static (non-abstract) method cannot hide an instance method.
			IProblem.CannotHideAnInstanceMethodWithAStaticMethod,
			new String[] {new String(inheritedMethod.declaringClass.readableName())},
			new String[] {new String(inheritedMethod.declaringClass.shortReadableName())},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
	else
		this.handle(
			// This instance method cannot override the static method from %1
			// 8.4.6.4 - If a class inherits more than one method with the same signature an instance (non-abstract) method cannot override a static method.
			IProblem.CannotOverrideAStaticMethodWithAnInstanceMethod,
			new String[] {new String(inheritedMethod.declaringClass.readableName())},
			new String[] {new String(inheritedMethod.declaringClass.shortReadableName())},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
}
public void staticFieldAccessToNonStaticVariable(ASTNode location, FieldBinding field) {
	String[] arguments = new String[] {new String(field.readableName())};
	this.handle(
		IProblem.NonStaticFieldFromStaticInvocation,
		arguments,
		arguments,
		nodeSourceStart(field,location),
		nodeSourceEnd(field, location));
}
public void staticInheritedMethodConflicts(SourceTypeBinding type, MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
//{ObjectTeams: if all abstract methods are static role-ifc methods, this is OK
	boolean ok = true;
	for (int i = 0; i < abstractMethods.length; i++) {
		if (abstractMethods[i].declaringClass.isSynthInterface() && abstractMethods[i].isStatic())
			continue;
		ok = false;
		break;
	}
	if (ok) return;
// SH}
	this.handle(
		// The static method %1 conflicts with the abstract method in %2
		// 8.4.6.4 - If a class inherits more than one method with the same signature it is an error for one to be static (non-abstract) and the other abstract.
		IProblem.StaticInheritedMethodConflicts,
		new String[] {
			new String(concreteMethod.readableName()),
			new String(abstractMethods[0].declaringClass.readableName())},
		new String[] {
//{ObjectTeams: bugfix? (was readableName, caused failure in regression tests)
			new String(concreteMethod.shortReadableName()),
// SH}
			new String(abstractMethods[0].declaringClass.shortReadableName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void staticMemberOfParameterizedType(ASTNode location, ReferenceBinding type, int index) {
	if (location == null) { // binary case
	    this.handle(
			IProblem.StaticMemberOfParameterizedType,
			new String[] {new String(type.readableName()), new String(type.enclosingType().readableName()), },
			new String[] {new String(type.shortReadableName()), new String(type.enclosingType().shortReadableName()), },
			ProblemSeverities.AbortCompilation | ProblemSeverities.Error | ProblemSeverities.Fatal,
			0,
			0);
	    return;
	}
	/*if (location instanceof ArrayTypeReference) {
		ArrayTypeReference arrayTypeReference = (ArrayTypeReference) location;
		if (arrayTypeReference.token != null && arrayTypeReference.token.length == 0) return;
		end = arrayTypeReference.originalSourceEnd;
	}*/
    this.handle(
		IProblem.StaticMemberOfParameterizedType,
		new String[] {new String(type.readableName()), new String(type.enclosingType().readableName()), },
		new String[] {new String(type.shortReadableName()), new String(type.enclosingType().shortReadableName()), },
		location.sourceStart,
		nodeSourceEnd(null, location, index));
}
public void stringConstantIsExceedingUtf8Limit(ASTNode location) {
	this.handle(
		IProblem.StringConstantIsExceedingUtf8Limit,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void superclassMustBeAClass(SourceTypeBinding type, TypeReference superclassRef, ReferenceBinding superType) {
	this.handle(
		IProblem.SuperclassMustBeAClass,
		new String[] {new String(superType.readableName()), new String(type.sourceName())},
		new String[] {new String(superType.shortReadableName()), new String(type.sourceName())},
		superclassRef.sourceStart,
		superclassRef.sourceEnd);
}
public void superfluousSemicolon(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.SuperfluousSemicolon,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);
}
public void superinterfaceMustBeAnInterface(SourceTypeBinding type, TypeReference superInterfaceRef, ReferenceBinding superType) {
	this.handle(
		IProblem.SuperInterfaceMustBeAnInterface,
		new String[] {new String(superType.readableName()), new String(type.sourceName())},
		new String[] {new String(superType.shortReadableName()), new String(type.sourceName())},
		superInterfaceRef.sourceStart,
		superInterfaceRef.sourceEnd);
}
public void superinterfacesCollide(TypeBinding type, ASTNode decl, TypeBinding superType, TypeBinding inheritedSuperType) {
	this.handle(
		IProblem.SuperInterfacesCollide,
		new String[] {new String(superType.readableName()), new String(inheritedSuperType.readableName()), new String(type.sourceName())},
		new String[] {new String(superType.shortReadableName()), new String(inheritedSuperType.shortReadableName()), new String(type.sourceName())},
		decl.sourceStart,
		decl.sourceEnd);
}
public void superTypeCannotUseWildcard(SourceTypeBinding type, TypeReference superclass, TypeBinding superTypeBinding) {
	String name = new String(type.sourceName());
	String superTypeFullName = new String(superTypeBinding.readableName());
	String superTypeShortName = new String(superTypeBinding.shortReadableName());
	if (superTypeShortName.equals(name)) superTypeShortName = superTypeFullName;
	this.handle(
		IProblem.SuperTypeUsingWildcard,
		new String[] {superTypeFullName, name},
		new String[] {superTypeShortName, name},
		superclass.sourceStart,
		superclass.sourceEnd);
}
private void syntaxError(
	int id,
	int startPosition,
	int endPosition,
	int currentKind,
	char[] currentTokenSource,
	String errorTokenName,
	String expectedToken) {

	String eTokenName;
	if (isKeyword(currentKind) ||
		isLiteral(currentKind) ||
		isIdentifier(currentKind)) {
			eTokenName = new String(currentTokenSource);
	} else {
		eTokenName = errorTokenName;
	}

	String[] arguments;
	if(expectedToken != null) {
		arguments = new String[] {eTokenName, expectedToken};
	} else {
		arguments = new String[] {eTokenName};
	}
	this.handle(
		id,
		arguments,
		arguments,
		startPosition,
		endPosition);
}
public void task(String tag, String message, String priority, int start, int end){
	this.handle(
		IProblem.Task,
		new String[] { tag, message, priority/*secret argument that is not surfaced in getMessage()*/},
		new String[] { tag, message, priority/*secret argument that is not surfaced in getMessage()*/},
		start,
		end);
}

public void tooManyDimensions(ASTNode expression) {
	this.handle(
		IProblem.TooManyArrayDimensions,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}

public void tooManyFields(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.TooManyFields,
		new String[]{ new String(typeDeclaration.binding.readableName())},
		new String[]{ new String(typeDeclaration.binding.shortReadableName())},
		ProblemSeverities.Abort | ProblemSeverities.Error | ProblemSeverities.Fatal,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}
public void tooManyMethods(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.TooManyMethods,
		new String[]{ new String(typeDeclaration.binding.readableName())},
		new String[]{ new String(typeDeclaration.binding.shortReadableName())},
		ProblemSeverities.Abort | ProblemSeverities.Error | ProblemSeverities.Fatal,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}
public void typeCastError(CastExpression expression, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.IllegalCast,
		new String[] { rightName, leftName },
		new String[] { rightShortName, leftShortName },
		expression.sourceStart,
		expression.sourceEnd);
}
public void typeCollidesWithEnclosingType(TypeDeclaration typeDecl) {
	String[] arguments = new String[] {new String(typeDecl.name)};
	this.handle(
		IProblem.HidingEnclosingType,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void typeCollidesWithPackage(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	String[] arguments = new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)};
	this.handle(
		IProblem.TypeCollidesWithPackage,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd,
		compUnitDecl.compilationResult);
}
public void typeHiding(TypeDeclaration typeDecl, TypeBinding hiddenType) {
	int severity = computeSeverity(IProblem.TypeHidingType);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		IProblem.TypeHidingType,
		new String[] { new String(typeDecl.name) , new String(hiddenType.shortReadableName()) },
		new String[] { new String(typeDecl.name) , new String(hiddenType.readableName()) },
		severity,
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void typeHiding(TypeDeclaration typeDecl, TypeVariableBinding hiddenTypeParameter) {
	int severity = computeSeverity(IProblem.TypeHidingTypeParameterFromType);
	if (severity == ProblemSeverities.Ignore) return;
	if (hiddenTypeParameter.declaringElement instanceof TypeBinding) {
		TypeBinding declaringType = (TypeBinding) hiddenTypeParameter.declaringElement;
		this.handle(
			IProblem.TypeHidingTypeParameterFromType,
			new String[] { new String(typeDecl.name) , new String(hiddenTypeParameter.readableName()), new String(declaringType.readableName())  },
			new String[] { new String(typeDecl.name) , new String(hiddenTypeParameter.shortReadableName()), new String(declaringType.shortReadableName()) },
			severity,
			typeDecl.sourceStart,
			typeDecl.sourceEnd);
	} else {
		// type parameter of generic method
		MethodBinding declaringMethod = (MethodBinding) hiddenTypeParameter.declaringElement;
		this.handle(
				IProblem.TypeHidingTypeParameterFromMethod,
				new String[] {
						new String(typeDecl.name),
						new String(hiddenTypeParameter.readableName()),
						new String(declaringMethod.selector),
						typesAsString(declaringMethod.isVarargs(), declaringMethod.parameters, false),
						new String(declaringMethod.declaringClass.readableName()),
				},
				new String[] {
						new String(typeDecl.name),
						new String(hiddenTypeParameter.shortReadableName()),
						new String(declaringMethod.selector),
						typesAsString(declaringMethod.isVarargs(), declaringMethod.parameters, true),
						new String(declaringMethod.declaringClass.shortReadableName()),
				},
				severity,
				typeDecl.sourceStart,
				typeDecl.sourceEnd);
	}
}
public void typeHiding(TypeParameter typeParam, Binding hidden) {
	int severity = computeSeverity(IProblem.TypeParameterHidingType);
	if (severity == ProblemSeverities.Ignore) return;
	TypeBinding hiddenType = (TypeBinding) hidden;
	this.handle(
		IProblem.TypeParameterHidingType,
		new String[] { new String(typeParam.name) , new String(hiddenType.readableName())  },
		new String[] { new String(typeParam.name) , new String(hiddenType.shortReadableName()) },
		severity,
		typeParam.sourceStart,
		typeParam.sourceEnd);
}
public void typeMismatchError(TypeBinding actualType, TypeBinding expectedType, ASTNode location, ASTNode expectingLocation) {
	if (actualType != null && (actualType.tagBits & TagBits.HasMissingType) != 0) { // improve secondary error
		this.handle(
				IProblem.UndefinedType,
				new String[] {new String(actualType.leafComponentType().readableName())},
				new String[] {new String(actualType.leafComponentType().shortReadableName())},
				location.sourceStart,
				location.sourceEnd);
			return;
	}
	if (expectingLocation != null && (expectedType.tagBits & TagBits.HasMissingType) != 0) { // improve secondary error
		this.handle(
				IProblem.UndefinedType,
				new String[] {new String(expectedType.leafComponentType().readableName())},
				new String[] {new String(expectedType.leafComponentType().shortReadableName())},
				expectingLocation.sourceStart,
				expectingLocation.sourceEnd);
			return;
	}
//{ObjectTeams: differentiate for role types:
	int problemId = IProblem.TypeMismatch; // default
	if (   RoleTypeBinding.isRoleType(expectedType)
		&& RoleTypeBinding.isRoleType(actualType))
	{
		DependentTypeBinding resultRole = (DependentTypeBinding)actualType;
		DependentTypeBinding expectedRole = (DependentTypeBinding)expectedType;
		if (resultRole.getRealType().isCompatibleWith(expectedRole.getRealType()))
		{
			if (resultRole._teamAnchor.problemId() != ProblemReasons.NoError) {
				problemId = resultRole._teamAnchor.problemId();
			} else if (resultRole._teamAnchor.hasSameTypeAs(expectedRole._teamAnchor)) {
				if (!resultRole._teamAnchor.hasSameBestNameAs(expectedRole._teamAnchor))
					problemId = IProblem.DifferentTeamInstance;
				// otherwise assume it is a non-OT type mismatch (e.g., mismatching type variables)
			} else {
				problemId = IProblem.DifferentTeamClass;
			}
		}
	}
	// special case 7.2(b): Object vs. Confined[].
	if (   expectedType.id == TypeIds.T_JavaLangObject
		&& actualType != null
		&& actualType.isArrayType()
		&& TypeAnalyzer.isConfined(actualType.leafComponentType()))
	{
			problemId= IProblem.ArrayOfConfinedNotConform;
	}
// SH}
	char[] actualShortReadableName = actualType.shortReadableName();
	char[] expectedShortReadableName = expectedType.shortReadableName();
	if (CharOperation.equals(actualShortReadableName, expectedShortReadableName)) {
		actualShortReadableName = actualType.readableName();
		expectedShortReadableName = expectedType.readableName();
	}
	this.handle(
/*OT:*/	problemId,
		new String[] {new String(actualType.readableName()), new String(expectedType.readableName())},
		new String[] {new String(actualShortReadableName), new String(expectedShortReadableName)},
		location.sourceStart,
		location.sourceEnd);
}
public void typeMismatchError(TypeBinding typeArgument, TypeVariableBinding typeParameter, ReferenceBinding genericType, ASTNode location) {
	if (location == null) { // binary case
		this.handle(
			IProblem.TypeArgumentMismatch,
			new String[] { new String(typeArgument.readableName()), new String(genericType.readableName()), new String(typeParameter.sourceName), parameterBoundAsString(typeParameter, false) },
			new String[] { new String(typeArgument.shortReadableName()), new String(genericType.shortReadableName()), new String(typeParameter.sourceName), parameterBoundAsString(typeParameter, true) },
			ProblemSeverities.AbortCompilation | ProblemSeverities.Error | ProblemSeverities.Fatal,
			0,
			0);
        return;
    }
//{ObjectTeams: include anchor in typeParameter name:
	char[] typeParamName;
	if (typeParameter.anchors == null)
		typeParamName = typeParameter.sourceName;
	else
		typeParamName = typeParameter.readableName();
	this.handle(
		IProblem.TypeArgumentMismatch,
/* orig:		
		new String[] { new String(typeArgument.readableName()), new String(genericType.readableName()), new String(typeParameter.sourceName), parameterBoundAsString(typeParameter, false) },
		new String[] { new String(typeArgument.shortReadableName()), new String(genericType.shortReadableName()), new String(typeParameter.sourceName), parameterBoundAsString(typeParameter, true) },
  :giro */
		new String[] { new String(typeArgument.readableName()), new String(genericType.readableName()), new String(typeParamName), parameterBoundAsString(typeParameter, false) },
		new String[] { new String(typeArgument.shortReadableName()), new String(genericType.shortReadableName()), new String(typeParamName), parameterBoundAsString(typeParameter, true) },
// SH}		
		location.sourceStart,
		location.sourceEnd);
}
private String typesAsString(boolean isVarargs, TypeBinding[] types, boolean makeShort) {
	StringBuffer buffer = new StringBuffer(10);
	for (int i = 0, length = types.length; i < length; i++) {
		if (i != 0)
			buffer.append(", "); //$NON-NLS-1$
		TypeBinding type = types[i];
		boolean isVarargType = isVarargs && i == length-1;
		if (isVarargType) type = ((ArrayBinding)type).elementsType();
		buffer.append(new String(makeShort ? type.shortReadableName() : type.readableName()));
		if (isVarargType) buffer.append("..."); //$NON-NLS-1$
	}
//{ObjectTeams: heuristically beautify:
	MethodSignatureEnhancer.beautifyTypesString(buffer, makeShort);
// SH}
	return buffer.toString();
}
public void undefinedAnnotationValue(TypeBinding annotationType, MemberValuePair memberValuePair) {
	if (isRecoveredName(memberValuePair.name)) return;
	String name = 	new String(memberValuePair.name);
	this.handle(
		IProblem.UndefinedAnnotationMember,
		new String[] { name, new String(annotationType.readableName())},
		new String[] {	name, new String(annotationType.shortReadableName())},
		memberValuePair.sourceStart,
		memberValuePair.sourceEnd);
}
public void undefinedLabel(BranchStatement statement) {
	if (isRecoveredName(statement.label)) return;
	String[] arguments = new String[] {new String(statement.label)};
	this.handle(
		IProblem.UndefinedLabel,
		arguments,
		arguments,
		statement.sourceStart,
		statement.sourceEnd);
}
// can only occur inside binaries
public void undefinedTypeVariableSignature(char[] variableName, ReferenceBinding binaryType) {
	this.handle(
		IProblem.UndefinedTypeVariable,
		new String[] {new String(variableName), new String(binaryType.readableName()) },
		new String[] {new String(variableName), new String(binaryType.shortReadableName())},
		ProblemSeverities.AbortCompilation | ProblemSeverities.Error | ProblemSeverities.Fatal,
		0,
		0);
}
public void undocumentedEmptyBlock(int blockStart, int blockEnd) {
	this.handle(
		IProblem.UndocumentedEmptyBlock,
		NoArgument,
		NoArgument,
		blockStart,
		blockEnd);
}
public void unexpectedStaticModifierForField(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {new String(fieldDecl.name)};
	this.handle(
		IProblem.UnexpectedStaticModifierForField,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void unexpectedStaticModifierForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.UnexpectedStaticModifierForMethod,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void unhandledException(TypeBinding exceptionType, ASTNode location) {
//{ObjectTeams: specific message:
	if (this.referenceContext instanceof GuardPredicateDeclaration) {
		checkedExceptionInGuard(exceptionType, location);
		return;
	}
// SH}

	boolean insideDefaultConstructor =
		(this.referenceContext instanceof ConstructorDeclaration)
			&& ((ConstructorDeclaration)this.referenceContext).isDefaultConstructor();
	boolean insideImplicitConstructorCall =
		(location instanceof ExplicitConstructorCall)
			&& (((ExplicitConstructorCall) location).accessMode == ExplicitConstructorCall.ImplicitSuper);

	this.handle(
		insideDefaultConstructor
			? IProblem.UnhandledExceptionInDefaultConstructor
			: (insideImplicitConstructorCall
					? IProblem.UndefinedConstructorInImplicitConstructorCall
					: IProblem.UnhandledException),
		new String[] {new String(exceptionType.readableName())},
		new String[] {new String(exceptionType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void unhandledWarningToken(Expression token) {
	String[] arguments = new String[] { token.constant.stringValue() };
	this.handle(
		IProblem.UnhandledWarningToken,
		arguments,
		arguments,
		token.sourceStart,
		token.sourceEnd);
}
public void uninitializedBlankFinalField(FieldBinding field, ASTNode location) {
//{ObjectTeams
	if ((field.tagBits & TagBits.IsFakedField) != 0)
		return;
// SH}
	String[] arguments = new String[] {new String(field.readableName())};
	this.handle(
		IProblem.UninitializedBlankFinalField,
		arguments,
		arguments,
		nodeSourceStart(field, location),
		nodeSourceEnd(field, location));
}
public void uninitializedLocalVariable(LocalVariableBinding binding, ASTNode location) {
	String[] arguments = new String[] {new String(binding.readableName())};
	this.handle(
		IProblem.UninitializedLocalVariable,
		arguments,
		arguments,
		nodeSourceStart(binding, location),
		nodeSourceEnd(binding, location));
}
public void unmatchedBracket(int position, ReferenceContext context, CompilationResult compilationResult) {
	this.handle(
		IProblem.UnmatchedBracket,
		NoArgument,
		NoArgument,
		position,
		position,
		context,
		compilationResult);
}
public void unnecessaryCast(CastExpression castExpression) {
//{ObjectTeams: NEVER report a generated cast as being unnecessary:
	if (castExpression.isGenerated)
		return;
// SH}
	int severity = computeSeverity(IProblem.UnnecessaryCast);
	if (severity == ProblemSeverities.Ignore) return;
	TypeBinding castedExpressionType = castExpression.expression.resolvedType;
	this.handle(
		IProblem.UnnecessaryCast,
		new String[]{ new String(castedExpressionType.readableName()), new String(castExpression.type.resolvedType.readableName())},
		new String[]{ new String(castedExpressionType.shortReadableName()), new String(castExpression.type.resolvedType.shortReadableName())},
		severity,
		castExpression.sourceStart,
		castExpression.sourceEnd);
}
public void unnecessaryElse(ASTNode location) {
	this.handle(
		IProblem.UnnecessaryElse,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void unnecessaryEnclosingInstanceSpecification(Expression expression, ReferenceBinding targetType) {
	this.handle(
		IProblem.IllegalEnclosingInstanceSpecification,
		new String[]{ new String(targetType.readableName())},
		new String[]{ new String(targetType.shortReadableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void unnecessaryInstanceof(InstanceOfExpression instanceofExpression, TypeBinding checkType) {
	int severity = computeSeverity(IProblem.UnnecessaryInstanceof);
	if (severity == ProblemSeverities.Ignore) return;
	TypeBinding expressionType = instanceofExpression.expression.resolvedType;
	this.handle(
		IProblem.UnnecessaryInstanceof,
		new String[]{ new String(expressionType.readableName()), new String(checkType.readableName())},
		new String[]{ new String(expressionType.shortReadableName()), new String(checkType.shortReadableName())},
		severity,
		instanceofExpression.sourceStart,
		instanceofExpression.sourceEnd);
}
public void unnecessaryNLSTags(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.UnnecessaryNLSTag,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);
}
public void unnecessaryTypeArgumentsForMethodInvocation(MethodBinding method, TypeBinding[] genericTypeArguments, TypeReference[] typeArguments) {
	String methodName = method.isConstructor()
		? new String(method.declaringClass.shortReadableName())
		: new String(method.selector);
	this.handle(
			method.isConstructor()
				? IProblem.UnusedTypeArgumentsForConstructorInvocation
				: IProblem.UnusedTypeArgumentsForMethodInvocation,
		new String[] {
				methodName,
		        typesAsString(method.isVarargs(), method.parameters, false),
		        new String(method.declaringClass.readableName()),
		        typesAsString(false, genericTypeArguments, false) },
		new String[] {
				methodName,
		        typesAsString(method.isVarargs(), method.parameters, true),
		        new String(method.declaringClass.shortReadableName()),
		        typesAsString(false, genericTypeArguments, true) },
		typeArguments[0].sourceStart,
		typeArguments[typeArguments.length-1].sourceEnd);
}
public void unqualifiedFieldAccess(NameReference reference, FieldBinding field) {
	int sourceStart = reference.sourceStart;
	int sourceEnd = reference.sourceEnd;
	if (reference instanceof SingleNameReference) {
		int numberOfParens = (reference.bits & ASTNode.ParenthesizedMASK) >> ASTNode.ParenthesizedSHIFT;
		if (numberOfParens != 0) {
			sourceStart = retrieveStartingPositionAfterOpeningParenthesis(sourceStart, sourceEnd, numberOfParens);
			sourceEnd = retrieveEndingPositionAfterOpeningParenthesis(sourceStart, sourceEnd, numberOfParens);
		} else {
			sourceStart = nodeSourceStart(field, reference);
			sourceEnd = nodeSourceEnd(field, reference);
		}
	} else {
		sourceStart = nodeSourceStart(field, reference);
		sourceEnd = nodeSourceEnd(field, reference);
	}
	this.handle(
		IProblem.UnqualifiedFieldAccess,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		sourceStart,
		sourceEnd);
}
public void unreachableCatchBlock(ReferenceBinding exceptionType, ASTNode location) {
	this.handle(
		IProblem.UnreachableCatch,
		new String[] {
			new String(exceptionType.readableName()),
		 },
		new String[] {
			new String(exceptionType.shortReadableName()),
		 },
		location.sourceStart,
		location.sourceEnd);
}
public void unreachableCode(Statement statement) {
	int sourceStart = statement.sourceStart;
	int sourceEnd = statement.sourceEnd;
	if (statement instanceof LocalDeclaration) {
		LocalDeclaration declaration = (LocalDeclaration) statement;
		sourceStart = declaration.declarationSourceStart;
		sourceEnd = declaration.declarationSourceEnd;
	} else if (statement instanceof Expression) {
		int statemendEnd = ((Expression) statement).statementEnd;
		if (statemendEnd != -1) sourceEnd = statemendEnd;
	}
	this.handle(
		IProblem.CodeCannotBeReached,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);
}
public void unresolvableReference(NameReference nameRef, Binding binding) {
/* also need to check that the searchedType is the receiver type
	if (binding instanceof ProblemBinding) {
		ProblemBinding problem = (ProblemBinding) binding;
		if (problem.searchType != null && problem.searchType.isHierarchyInconsistent())
			severity = SecondaryError;
	}
*/
	String[] arguments = new String[] {new String(binding.readableName())};
	int end = nameRef.sourceEnd;
	if (nameRef instanceof QualifiedNameReference) {
		QualifiedNameReference ref = (QualifiedNameReference) nameRef;
		if (isRecoveredName(ref.tokens)) return;
		if (ref.indexOfFirstFieldBinding >= 1)
			end = (int) ref.sourcePositions[ref.indexOfFirstFieldBinding - 1];
	} else {
		SingleNameReference ref = (SingleNameReference) nameRef;
		if (isRecoveredName(ref.token)) return;
	}
//{ObjectTeams: perhaps missing signature in callin mapping to pass args to predicate?
	if (this.referenceContext instanceof GuardPredicateDeclaration) {
		if (((GuardPredicateDeclaration)this.referenceContext).handleMissingSignature(arguments, nameRef.sourceStart, end))
			return;
	}
	if (nameRef.toString().startsWith(IOTConstants.OT_DOLLAR)) {
		this.referenceContext.tagAsHavingErrors();
		return; // don't report errors of generated elements
	}
// SH}
    int problemId = (nameRef.bits & Binding.VARIABLE) != 0 && (nameRef.bits & Binding.TYPE) == 0
    		? IProblem.UnresolvedVariable
    		: IProblem.UndefinedName;
	this.handle(
		problemId,
		arguments,
		arguments,
		nameRef.sourceStart,
		end);
}
public void unsafeCast(CastExpression castExpression, Scope scope) {
	int severity = computeSeverity(IProblem.UnsafeGenericCast);
	if (severity == ProblemSeverities.Ignore) return;
	TypeBinding castedExpressionType = castExpression.expression.resolvedType;
	TypeBinding castExpressionResolvedType = castExpression.resolvedType;
	this.handle(
		IProblem.UnsafeGenericCast,
		new String[]{
			new String(castedExpressionType.readableName()),
			new String(castExpressionResolvedType.readableName())
		},
		new String[]{
			new String(castedExpressionType.shortReadableName()),
			new String(castExpressionResolvedType.shortReadableName())
		},
		severity,
		castExpression.sourceStart,
		castExpression.sourceEnd);
}
public void unsafeGenericArrayForVarargs(TypeBinding leafComponentType, ASTNode location) {
	int severity = computeSeverity(IProblem.UnsafeGenericArrayForVarargs);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		IProblem.UnsafeGenericArrayForVarargs,
		new String[]{ new String(leafComponentType.readableName())},
		new String[]{ new String(leafComponentType.shortReadableName())},
		severity,
		location.sourceStart,
		location.sourceEnd);
}
public void unsafeRawFieldAssignment(FieldBinding field, TypeBinding expressionType, ASTNode location) {
	int severity = computeSeverity(IProblem.UnsafeRawFieldAssignment);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		IProblem.UnsafeRawFieldAssignment,
		new String[] {
		        new String(expressionType.readableName()), new String(field.name), new String(field.declaringClass.readableName()), new String(field.declaringClass.erasure().readableName()) },
		new String[] {
		        new String(expressionType.shortReadableName()), new String(field.name), new String(field.declaringClass.shortReadableName()), new String(field.declaringClass.erasure().shortReadableName()) },
		severity,
		nodeSourceStart(field,location),
		nodeSourceEnd(field, location));
}
public void unsafeRawGenericMethodInvocation(ASTNode location, MethodBinding rawMethod, TypeBinding[] argumentTypes) {
	boolean isConstructor = rawMethod.isConstructor();
	int severity = computeSeverity(isConstructor ? IProblem.UnsafeRawGenericConstructorInvocation : IProblem.UnsafeRawGenericMethodInvocation);
	if (severity == ProblemSeverities.Ignore) return;
    if (isConstructor) {
		this.handle(
			IProblem.UnsafeRawGenericConstructorInvocation, // The generic constructor {0}({1}) of type {2} is applied to non-parameterized type arguments ({3})
			new String[] {
				new String(rawMethod.declaringClass.sourceName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.original().parameters, false),
				new String(rawMethod.declaringClass.readableName()),
				typesAsString(false, argumentTypes, false),
			 },
			new String[] {
				new String(rawMethod.declaringClass.sourceName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.original().parameters, true),
				new String(rawMethod.declaringClass.shortReadableName()),
				typesAsString(false, argumentTypes, true),
			 },
			severity,
			location.sourceStart,
			location.sourceEnd);
    } else {
		this.handle(
			IProblem.UnsafeRawGenericMethodInvocation,
			new String[] {
				new String(rawMethod.selector),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.original().parameters, false),
				new String(rawMethod.declaringClass.readableName()),
				typesAsString(false, argumentTypes, false),
			 },
			new String[] {
				new String(rawMethod.selector),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.original().parameters, true),
				new String(rawMethod.declaringClass.shortReadableName()),
				typesAsString(false, argumentTypes, true),
			 },
			severity,
			location.sourceStart,
			location.sourceEnd);
    }
}
public void unsafeRawInvocation(ASTNode location, MethodBinding rawMethod) {
	boolean isConstructor = rawMethod.isConstructor();
	int severity = computeSeverity(isConstructor ? IProblem.UnsafeRawConstructorInvocation : IProblem.UnsafeRawMethodInvocation);
	if (severity == ProblemSeverities.Ignore) return;
    if (isConstructor) {
		this.handle(
			IProblem.UnsafeRawConstructorInvocation,
			new String[] {
				new String(rawMethod.declaringClass.readableName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, false),
				new String(rawMethod.declaringClass.erasure().readableName()),
			 },
			new String[] {
				new String(rawMethod.declaringClass.shortReadableName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, true),
				new String(rawMethod.declaringClass.erasure().shortReadableName()),
			 },
			severity,
			location.sourceStart,
			location.sourceEnd);
    } else {
		this.handle(
			IProblem.UnsafeRawMethodInvocation,
			new String[] {
				new String(rawMethod.selector),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, false),
				new String(rawMethod.declaringClass.readableName()),
				new String(rawMethod.declaringClass.erasure().readableName()),
			 },
			new String[] {
				new String(rawMethod.selector),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, true),
				new String(rawMethod.declaringClass.shortReadableName()),
				new String(rawMethod.declaringClass.erasure().shortReadableName()),
			 },
			severity,
			location.sourceStart,
			location.sourceEnd);
    }
}
public void unsafeReturnTypeOverride(MethodBinding currentMethod, MethodBinding inheritedMethod, SourceTypeBinding type) {
	int severity = computeSeverity(IProblem.UnsafeReturnTypeOverride);
	if (severity == ProblemSeverities.Ignore) return;
	int start = type.sourceStart();
	int end = type.sourceEnd();
	if (currentMethod.declaringClass == type) {
		ASTNode location = ((MethodDeclaration) currentMethod.sourceMethod()).returnType;
		start = location.sourceStart();
		end = location.sourceEnd();
	}
	this.handle(
			IProblem.UnsafeReturnTypeOverride,
			new String[] {
				new String(currentMethod.returnType.readableName()),
				new String(currentMethod.selector),
				typesAsString(currentMethod.original().isVarargs(), currentMethod.original().parameters, false),
				new String(currentMethod.declaringClass.readableName()),
				new String(inheritedMethod.returnType.readableName()),
				new String(inheritedMethod.declaringClass.readableName()),
				//new String(inheritedMethod.returnType.erasure().readableName()),
			 },
			new String[] {
				new String(currentMethod.returnType.shortReadableName()),
				new String(currentMethod.selector),
				typesAsString(currentMethod.original().isVarargs(), currentMethod.original().parameters, true),
				new String(currentMethod.declaringClass.shortReadableName()),
				new String(inheritedMethod.returnType.shortReadableName()),
				new String(inheritedMethod.declaringClass.shortReadableName()),
				//new String(inheritedMethod.returnType.erasure().shortReadableName()),
			 },
			severity,
			start,
			end);
}
public void unsafeTypeConversion(Expression expression, TypeBinding expressionType, TypeBinding expectedType) {
	int severity = computeSeverity(IProblem.UnsafeTypeConversion);
	if (severity == ProblemSeverities.Ignore) return;
	this.handle(
		IProblem.UnsafeTypeConversion,
		new String[] { new String(expressionType.readableName()), new String(expectedType.readableName()), new String(expectedType.erasure().readableName()) },
		new String[] { new String(expressionType.shortReadableName()), new String(expectedType.shortReadableName()), new String(expectedType.erasure().shortReadableName()) },
		severity,
		expression.sourceStart,
		expression.sourceEnd);
}
public void unusedArgument(LocalDeclaration localDecl) {
	int severity = computeSeverity(IProblem.ArgumentIsNeverUsed);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] {new String(localDecl.name)};
	this.handle(
		IProblem.ArgumentIsNeverUsed,
		arguments,
		arguments,
		severity,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void unusedDeclaredThrownException(ReferenceBinding exceptionType, AbstractMethodDeclaration method, ASTNode location) {
	boolean isConstructor = method.isConstructor();
	int severity = computeSeverity(isConstructor ? IProblem.UnusedConstructorDeclaredThrownException : IProblem.UnusedMethodDeclaredThrownException);
	if (severity == ProblemSeverities.Ignore) return;
	if (isConstructor) {
		this.handle(
			IProblem.UnusedConstructorDeclaredThrownException,
			new String[] {
				new String(method.binding.declaringClass.readableName()),
				typesAsString(method.binding.isVarargs(), method.binding.parameters, false),
				new String(exceptionType.readableName()),
			 },
			new String[] {
				new String(method.binding.declaringClass.shortReadableName()),
				typesAsString(method.binding.isVarargs(), method.binding.parameters, true),
				new String(exceptionType.shortReadableName()),
			 },
			severity,
			location.sourceStart,
			location.sourceEnd);
	} else {
		this.handle(
			IProblem.UnusedMethodDeclaredThrownException,
			new String[] {
				new String(method.binding.declaringClass.readableName()),
				new String(method.selector),
				typesAsString(method.binding.isVarargs(), method.binding.parameters, false),
				new String(exceptionType.readableName()),
			 },
			new String[] {
				new String(method.binding.declaringClass.shortReadableName()),
				new String(method.selector),
				typesAsString(method.binding.isVarargs(), method.binding.parameters, true),
				new String(exceptionType.shortReadableName()),
			 },
			severity,
			location.sourceStart,
			location.sourceEnd);
	}
}
public void unusedImport(ImportReference importRef) {
	int severity = computeSeverity(IProblem.UnusedImport);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] { CharOperation.toString(importRef.tokens) };
	this.handle(
		IProblem.UnusedImport,
		arguments,
		arguments,
		severity,
		importRef.sourceStart,
		importRef.sourceEnd);
}
public void unusedLabel(LabeledStatement statement) {
	int severity = computeSeverity(IProblem.UnusedLabel);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] {new String(statement.label)};
	this.handle(
		IProblem.UnusedLabel,
		arguments,
		arguments,
		severity,
		statement.sourceStart,
		statement.labelEnd);
}
public void unusedLocalVariable(LocalDeclaration localDecl) {
	int severity = computeSeverity(IProblem.LocalVariableIsNeverUsed);
	if (severity == ProblemSeverities.Ignore) return;
	String[] arguments = new String[] {new String(localDecl.name)};
	this.handle(
		IProblem.LocalVariableIsNeverUsed,
		arguments,
		arguments,
		severity,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void unusedObjectAllocation(AllocationExpression allocationExpression) {
	this.handle(
		IProblem.UnusedObjectAllocation, 
		NoArgument, 
		NoArgument, 
		allocationExpression.sourceStart, 
		allocationExpression.sourceEnd);
}
public void unusedPrivateConstructor(ConstructorDeclaration constructorDecl) {

	int severity = computeSeverity(IProblem.UnusedPrivateConstructor);
	if (severity == ProblemSeverities.Ignore) return;

	MethodBinding constructor = constructorDecl.binding;
	this.handle(
			IProblem.UnusedPrivateConstructor,
		new String[] {
			new String(constructor.declaringClass.readableName()),
			typesAsString(constructor.isVarargs(), constructor.parameters, false)
		 },
		new String[] {
			new String(constructor.declaringClass.shortReadableName()),
			typesAsString(constructor.isVarargs(), constructor.parameters, true)
		 },
		severity,
		constructorDecl.sourceStart,
		constructorDecl.sourceEnd);
}
public void unusedPrivateField(FieldDeclaration fieldDecl) {

	int severity = computeSeverity(IProblem.UnusedPrivateField);
	if (severity == ProblemSeverities.Ignore) return;

	FieldBinding field = fieldDecl.binding;

	if (CharOperation.equals(TypeConstants.SERIALVERSIONUID, field.name)
			&& field.isStatic()
			&& field.isFinal()
			&& TypeBinding.LONG == field.type) {
				return; // do not report unused serialVersionUID field
	}
	if (CharOperation.equals(TypeConstants.SERIALPERSISTENTFIELDS, field.name)
			&& field.isStatic()
			&& field.isFinal()
			&& field.type.dimensions() == 1
			&& CharOperation.equals(TypeConstants.CharArray_JAVA_IO_OBJECTSTREAMFIELD, field.type.leafComponentType().readableName())) {
				return; // do not report unused serialPersistentFields field
	}
	this.handle(
			IProblem.UnusedPrivateField,
		new String[] {
			new String(field.declaringClass.readableName()),
			new String(field.name),
		 },
		new String[] {
			new String(field.declaringClass.shortReadableName()),
			new String(field.name),
		 },
		severity,
		nodeSourceStart(field, fieldDecl),
		nodeSourceEnd(field, fieldDecl));
}
public void unusedPrivateMethod(AbstractMethodDeclaration methodDecl) {

	int severity = computeSeverity(IProblem.UnusedPrivateMethod);
	if (severity == ProblemSeverities.Ignore) return;

	MethodBinding method = methodDecl.binding;

	// no report for serialization support 'void readObject(ObjectInputStream)'
	if (!method.isStatic()
			&& TypeBinding.VOID == method.returnType
			&& method.parameters.length == 1
			&& method.parameters[0].dimensions() == 0
			&& CharOperation.equals(method.selector, TypeConstants.READOBJECT)
			&& CharOperation.equals(TypeConstants.CharArray_JAVA_IO_OBJECTINPUTSTREAM, method.parameters[0].readableName())) {
		return;
	}
	// no report for serialization support 'void writeObject(ObjectOutputStream)'
	if (!method.isStatic()
			&& TypeBinding.VOID == method.returnType
			&& method.parameters.length == 1
			&& method.parameters[0].dimensions() == 0
			&& CharOperation.equals(method.selector, TypeConstants.WRITEOBJECT)
			&& CharOperation.equals(TypeConstants.CharArray_JAVA_IO_OBJECTOUTPUTSTREAM, method.parameters[0].readableName())) {
		return;
	}
	// no report for serialization support 'Object readResolve()'
	if (!method.isStatic()
			&& TypeIds.T_JavaLangObject == method.returnType.id
			&& method.parameters.length == 0
			&& CharOperation.equals(method.selector, TypeConstants.READRESOLVE)) {
		return;
	}
	// no report for serialization support 'Object writeReplace()'
	if (!method.isStatic()
			&& TypeIds.T_JavaLangObject == method.returnType.id
			&& method.parameters.length == 0
			&& CharOperation.equals(method.selector, TypeConstants.WRITEREPLACE)) {
		return;
	}
	this.handle(
			IProblem.UnusedPrivateMethod,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(method.selector),
			typesAsString(method.isVarargs(), method.parameters, false)
		 },
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(method.selector),
			typesAsString(method.isVarargs(), method.parameters, true)
		 },
		severity,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void unusedPrivateType(TypeDeclaration typeDecl) {
//{ObjectTeams: don't bother with copied nor tsuper marker interface:
	// TODO(SH): could this diagnostic actually be used to remove marker interfaces before codegen??
	if (typeDecl.isPurelyCopied || TSuperHelper.isMarkerInterface(typeDecl.binding))
		return;
// SH}
	int severity = computeSeverity(IProblem.UnusedPrivateType);
	if (severity == ProblemSeverities.Ignore) return;

	ReferenceBinding type = typeDecl.binding;
	this.handle(
			IProblem.UnusedPrivateType,
		new String[] {
			new String(type.readableName()),
		 },
		new String[] {
			new String(type.shortReadableName()),
		 },
		severity,
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void unusedWarningToken(Expression token) {
	String[] arguments = new String[] { token.constant.stringValue() };
	this.handle(
		IProblem.UnusedWarningToken,
		arguments,
		arguments,
		token.sourceStart,
		token.sourceEnd);
}
public void useAssertAsAnIdentifier(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.UseAssertAsAnIdentifier,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);
}
public void useEnumAsAnIdentifier(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.UseEnumAsAnIdentifier,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);
}
public void varargsArgumentNeedCast(MethodBinding method, TypeBinding argumentType, InvocationSite location) {
	int severity = this.options.getSeverity(CompilerOptions.VarargsArgumentNeedCast);
	if (severity == ProblemSeverities.Ignore) return;
	ArrayBinding varargsType = (ArrayBinding)method.parameters[method.parameters.length-1];
	if (method.isConstructor()) {
		this.handle(
			IProblem.ConstructorVarargsArgumentNeedCast,
			new String[] {
					new String(argumentType.readableName()),
					new String(varargsType.readableName()),
					new String(method.declaringClass.readableName()),
					typesAsString(method.isVarargs(), method.parameters, false),
					new String(varargsType.elementsType().readableName()),
			},
			new String[] {
					new String(argumentType.shortReadableName()),
					new String(varargsType.shortReadableName()),
					new String(method.declaringClass.shortReadableName()),
					typesAsString(method.isVarargs(), method.parameters, true),
					new String(varargsType.elementsType().shortReadableName()),
			},
			severity,
			location.sourceStart(),
			location.sourceEnd());
	} else {
		this.handle(
			IProblem.MethodVarargsArgumentNeedCast,
			new String[] {
					new String(argumentType.readableName()),
					new String(varargsType.readableName()),
					new String(method.selector),
					typesAsString(method.isVarargs(), method.parameters, false),
					new String(method.declaringClass.readableName()),
					new String(varargsType.elementsType().readableName()),
			},
			new String[] {
					new String(argumentType.shortReadableName()),
					new String(varargsType.shortReadableName()),
					new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true),
					new String(method.declaringClass.shortReadableName()),
					new String(varargsType.elementsType().shortReadableName()),
			},
			severity,
			location.sourceStart(),
			location.sourceEnd());
	}
}
public void varargsConflict(MethodBinding method1, MethodBinding method2, SourceTypeBinding type) {
	this.handle(
		IProblem.VarargsConflict,
		new String[] {
		        new String(method1.selector),
		        typesAsString(method1.isVarargs(), method1.parameters, false),
		        new String(method1.declaringClass.readableName()),
		        typesAsString(method2.isVarargs(), method2.parameters, false),
		        new String(method2.declaringClass.readableName())
		},
		new String[] {
		        new String(method1.selector),
		        typesAsString(method1.isVarargs(), method1.parameters, true),
		        new String(method1.declaringClass.shortReadableName()),
		        typesAsString(method2.isVarargs(), method2.parameters, true),
		        new String(method2.declaringClass.shortReadableName())
		},
		method1.declaringClass == type ? method1.sourceStart() : type.sourceStart(),
		method1.declaringClass == type ? method1.sourceEnd() : type.sourceEnd());
}
public void variableTypeCannotBeVoid(AbstractVariableDeclaration varDecl) {
	String[] arguments = new String[] {new String(varDecl.name)};
	this.handle(
		IProblem.VariableTypeCannotBeVoid,
		arguments,
		arguments,
		varDecl.sourceStart,
		varDecl.sourceEnd);
}
public void variableTypeCannotBeVoidArray(AbstractVariableDeclaration varDecl) {
	this.handle(
		IProblem.CannotAllocateVoidArray,
		NoArgument,
		NoArgument,
		varDecl.type.sourceStart,
		varDecl.type.sourceEnd);
}
public void visibilityConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		//	Cannot reduce the visibility of the inherited method from %1
		// 8.4.6.3 - The access modifier of an hiding method must provide at least as much access as the hidden method.
		// 8.4.6.3 - The access modifier of an overiding method must provide at least as much access as the overriden method.
		IProblem.MethodReducesVisibility,
		new String[] {new String(inheritedMethod.declaringClass.readableName())},
		new String[] {new String(inheritedMethod.declaringClass.shortReadableName())},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void wildcardAssignment(TypeBinding variableType, TypeBinding expressionType, ASTNode location) {
	this.handle(
		IProblem.WildcardFieldAssignment,
		new String[] {
		        new String(expressionType.readableName()), new String(variableType.readableName()) },
		new String[] {
		        new String(expressionType.shortReadableName()), new String(variableType.shortReadableName()) },
		location.sourceStart,
		location.sourceEnd);
}
public void wildcardInvocation(ASTNode location, TypeBinding receiverType, MethodBinding method, TypeBinding[] arguments) {
	TypeBinding offendingArgument = null;
	TypeBinding offendingParameter = null;
	for (int i = 0, length = method.parameters.length; i < length; i++) {
		TypeBinding parameter = method.parameters[i];
		if (parameter.isWildcard() && (((WildcardBinding) parameter).boundKind != Wildcard.SUPER)) {
			offendingParameter = parameter;
			offendingArgument = arguments[i];
			break;
		}
	}

	if (method.isConstructor()) {
		this.handle(
			IProblem.WildcardConstructorInvocation,
			new String[] {
				new String(receiverType.sourceName()),
				typesAsString(method.isVarargs(), method.parameters, false),
				new String(receiverType.readableName()),
				typesAsString(false, arguments, false),
				new String(offendingArgument.readableName()),
				new String(offendingParameter.readableName()),
			 },
			new String[] {
				new String(receiverType.sourceName()),
				typesAsString(method.isVarargs(), method.parameters, true),
				new String(receiverType.shortReadableName()),
				typesAsString(false, arguments, true),
				new String(offendingArgument.shortReadableName()),
				new String(offendingParameter.shortReadableName()),
			 },
			location.sourceStart,
			location.sourceEnd);
    } else {
		this.handle(
			IProblem.WildcardMethodInvocation,
			new String[] {
				new String(method.selector),
				typesAsString(method.isVarargs(), method.parameters, false),
				new String(receiverType.readableName()),
				typesAsString(false, arguments, false),
				new String(offendingArgument.readableName()),
				new String(offendingParameter.readableName()),
			 },
			new String[] {
				new String(method.selector),
				typesAsString(method.isVarargs(), method.parameters, true),
				new String(receiverType.shortReadableName()),
				typesAsString(false, arguments, true),
				new String(offendingArgument.shortReadableName()),
				new String(offendingParameter.shortReadableName()),
			 },
			location.sourceStart,
			location.sourceEnd);
    }
}
public void wrongSequenceOfExceptionTypesError(TryStatement statement, TypeBinding exceptionType, int under, TypeBinding hidingExceptionType) {
	//the two catch block under and upper are in an incorrect order.
	//under should be define BEFORE upper in the source

	TypeReference typeRef = statement.catchArguments[under].type;
	this.handle(
		IProblem.InvalidCatchBlockSequence,
		new String[] {
			new String(exceptionType.readableName()),
			new String(hidingExceptionType.readableName()),
		 },
		new String[] {
			new String(exceptionType.shortReadableName()),
			new String(hidingExceptionType.shortReadableName()),
		 },
		typeRef.sourceStart,
		typeRef.sourceEnd);
}
//{ObjectTeams:
/** This class is used for sorting which we do to make messages more deterministic. */
private final class CharArrayComparator implements Comparator<char[]> {
	public int compare(char[] o1, char[] o2) {
		for (int i=0; i<o1.length && i<o2.length; i++) {
			if (o1[i] < o2[i]) return -1;
			if (o1[i] > o2[i]) return 1;
		}
		if (o1.length < o2.length) return -1;
		if (o1.length > o2.length) return 1;
		return 0;
	}
}
public void teamCannotHaveSuperTypes(SourceTypeBinding type) {
	this.handle(
		IProblem.TeamCannotHaveSuperTypes,
		NoArgument,
		NoArgument,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForTeam(TypeDeclaration type) {
    String[] arguments = new String[] {new String(type.sourceName())};
    this.handle(
        IProblem.IllegalModifierForTeam,
        arguments,
        arguments,
        type.sourceStart(),
        type.sourceEnd());
}
// ====== TEAMS AND ROLES: =====
// -- 1.2 --
public void illegalModifierForRole(SourceTypeBinding type) {
    String[] arguments = new String[] {new String(type.sourceName())};
    this.handle(
        IProblem.IllegalModifierForRole,
        arguments,
        arguments,
        type.sourceStart(),
        type.sourceEnd());
}
public void staticRole(SourceTypeBinding type) {
    String[] arguments = new String[] {new String(type.sourceName())};
    this.handle(
        IProblem.StaticRole,
        arguments,
        arguments,
        type.sourceStart(),
        type.sourceEnd());
}
public void roleCantInitializeStaticField(FieldDeclaration fieldDecl)
{
	this.handle(IProblem.RoleCantInitializeStaticField,
			NoArgument,
			NoArgument,
			fieldDecl.initialization.sourceStart,
			fieldDecl.initialization.sourceEnd);
}
public void externalizingNonPublicRole(ASTNode typedNode, ReferenceBinding roleType) {
    String[] arguments = new String[]{
        new String(roleType.sourceName())
    };
    this.handle(
        IProblem.ExternalizingNonPublicRole,
        arguments,
        arguments,
        typedNode.sourceStart,
        typedNode.sourceEnd);
}
public void qualifiedProtectedRole(ASTNode typedNode, ReferenceBinding roleType) {
    String[] arguments = new String[]{
        new String(roleType.sourceName())
    };
    this.handle(
        typedNode instanceof QualifiedTypeReference ?
        		IProblem.QualifiedProtectedRole :
        		IProblem.ParameterizedProtectedRole,
        arguments,
        arguments,
        typedNode.sourceStart,
        typedNode.sourceEnd);
}
public void qualifiedRole(TypeReference typedNode, ReferenceBinding roleType) {
    String[] arguments = new String[]{
        new String(roleType.sourceName())
    };
    this.handle(IProblem.QualifiedRole,
        arguments,
        arguments,
        typedNode.sourceStart,
        typedNode.sourceEnd);
}
public void missingTypeAnchor (ASTNode location, TypeBinding role)
{
	int start = location.sourceStart;
	if (location instanceof AbstractVariableDeclaration)
		start = ((AbstractVariableDeclaration)location).declarationSourceStart;
    String[] msgArgs = new String[] {
        new String(role.readableName()),
    };
    this.handle(
        IProblem.MissingAnchorForRoleType,
        NoArgument,
        msgArgs,
        start,
        location.sourceEnd);
}
public void typeAnchorNotEnclosingTeam(ASTNode typedNode, ReferenceBinding teamType, ReferenceBinding roleType) {
    String[] arguments = new String[]{
    	new String(teamType.readableName()),
        new String(roleType.sourceName())
    };
    this.handle(
        IProblem.TypeAnchorNotEnclosingTeam,
        arguments,
        arguments,
        typedNode.sourceStart,
        typedNode.sourceEnd);
}
public void illegalTypeAnchorNotATeam(ASTNode anchor) {
	String[] args = new String[] {
			anchor.toString()
	};
	this.handle(
			(hasBaseQualifier(anchor))
			  ? IProblem.BaseQualifiesNonTeam
			  : IProblem.IllegalTypeAnchorNotATeam,
			args,
			args,
			anchor.sourceStart,
			anchor.sourceEnd);
}
private boolean hasBaseQualifier(ASTNode location) {
	if (location instanceof QualifiedTypeReference) {
		QualifiedTypeReference qRef = (QualifiedTypeReference) location;
		if (CharOperation.equals(qRef.getTypeName()[0], IOTConstants._OT_BASE))
			return true;
	} else if (location instanceof ParameterizedSingleTypeReference) {
		ParameterizedSingleTypeReference pstr= (ParameterizedSingleTypeReference)location;
		TypeAnchorReference[] anchors= pstr.typeAnchors;
		if (anchors != null && anchors.length > 0) {
			Reference anchor= anchors[0].anchor;
			if (anchor instanceof SingleNameReference)
				return CharOperation.equals(((SingleNameReference)anchor).token, IOTConstants._OT_BASE);
			if (anchor instanceof QualifiedNameReference)
				return CharOperation.equals(((QualifiedNameReference)anchor).tokens[0], IOTConstants._OT_BASE);
			if (anchor instanceof FieldReference)
				return hasBaseQualifier(((FieldReference)anchor).receiver);
		}
	}
	return false;
}
public void noSuchRoleInTeam(TypeReference typeReference, ReferenceBinding teamBinding) {
	char[][] compoundTypeName = typeReference.getTypeName();
	String[] args = new String[] {
			new String(compoundTypeName[compoundTypeName.length-1]), // last component
			new String(teamBinding.readableName())
	};
	this.handle(
			IProblem.NoSuchRoleInTeam,
			args,
			args,
			typeReference.sourceStart,
			typeReference.sourceEnd);
}
public void anchorNotFinal(
        ASTNode   anchorExpr,
		char[]    roleName)
{
	int start = anchorExpr.sourceStart;
	int end   = anchorExpr.sourceEnd;
	if (roleName == null) {
		// possibly part of anchorExpr
		if (anchorExpr instanceof QualifiedTypeReference) {
			long[] positions = ((QualifiedTypeReference)anchorExpr).sourcePositions;
			end = (int)(positions[positions.length-2] & 0xFFFF); // everything but the last token
			roleName = ((QualifiedTypeReference)anchorExpr).tokens[positions.length-1]; // last token
		}
	}
    String[] msgArgs = new String[] {
        new String(roleName),
    };
    this.handle(
        IProblem.AnchorNotFinal,
        NoArgument,
        msgArgs,
        start,
        end);
}
public void anchorPathNotFinal(ASTNode location, ITeamAnchor anchor, char[] typeName)
{
	if (anchor == null) {
		anchorNotFinal(location, typeName);
		return;
	}
	int start = location.sourceStart;
	int end   = location.sourceEnd;
	String[] args = new String[] {
			new String(anchor.getBestName()),
			typeName != null ? new String(typeName) : "<unresolved type>"  //$NON-NLS-1$
	};
	this.handle (
			IProblem.AnchorPathNotFinal,
			args,
			args,
			start,
			end);
}
public void typeAnchorIsNotAVariable(Expression anchorExpr, char[] roleName)
{
    String[] arguments = new String[]{
        new String(roleName)
    };
    this.handle(
        IProblem.AnchorNotAVariable,
        arguments,
        arguments,
        anchorExpr.sourceStart,
        anchorExpr.sourceEnd);
}
public void roleCreationNotRelativeToEnclosingTeam(AllocationExpression allocation) {
	this.handle(
			IProblem.RoleAllocationNotRelativeToEnclosingTeam,
			NoArgument,
			NoArgument,
			allocation.sourceStart,
			allocation.sourceEnd);

}
public void noTeamAnchorInScope(ASTNode location, TypeBinding type) {
	String[] args = new String[] {
			new String(type.readableName())
	};
	this.handle(
			IProblem.NoTeamAnchorInScope,
			args,
			args,
			location.sourceStart,
			location.sourceEnd);
}
public void externalizedRoleNotAllowedHere(TypeReference typeReference, DependentTypeBinding roleType) {
	String[] args = new String[] { new String(roleType.readableName()) };
	this.handle(
			IProblem.ExternalizedRoleNotAllowedHere,
			args,
			args,
			typeReference.sourceStart,
			typeReference.sourceEnd);
}
public void extendingExternalizedRole(TypeBinding role, ASTNode location) {
	String[] args = new String[] {
			new String(role.readableName())
	};
	this.handle(
			IProblem.ExtendingExternalizedRole,
			args,
			args,
			location.sourceStart,
			location.sourceEnd);
}
public void extendingExternalizedRole(TypeReference reference) {
	this.handle(
			IProblem.ExtendingExternalizedRole,
			NoArgument,
			NoArgument,
			reference.sourceStart,
			reference.sourceEnd);
}

public void cycleInFieldAnchor(FieldBinding field) {
	String[] args = new String[] {
			new String(field.declaringClass.readableName()),
			new String(field.readableName())
	};
	this.handle(
			IProblem.CycleInFieldAnchor,
			args,
			args,
			0,0);
}
public void cannotImportRole(ImportReference importReference, Binding importBinding) {
	String[] args = {new String(importBinding.readableName()) };
	this.handle(
			IProblem.CannotImportRole,
			args, args,
			importReference.sourceStart,
			importReference.sourceEnd);
}
public void deprecatedPathSyntax(ASTNode path) {
	this.handle(IProblem.DeprecatedPathSyntax, NoArgument, NoArgument, path.sourceStart, path.sourceEnd);
}
public void roleFileMustDeclareOneType(CompilationUnitDeclaration roleUnit) {
	this.referenceContext = roleUnit;
	this.handle(
			IProblem.RoleFileMustDeclareOneType,
			NoArgument,
			NoArgument,
			roleUnit.sourceStart,
			roleUnit.sourceEnd);
}
public void roleFileMismatchingName(CompilationUnitDeclaration cud, TypeDeclaration roFiType) {
	String[] args = new String[] {
		new String(cud.getFileName()),
		new String(roFiType.name)	
	};
	this.handle(IProblem.RoleFileMismatchingName, args, args, roFiType.sourceStart, roFiType.sourceEnd);
}
public void mismatchingPackageForRole(char[][] packageName, char[] teamName, char[] roleName, int start, int end)
{
	String[] args = new String[] {
			new String(CharOperation.concatWith(packageName, '.')),
			new String(teamName),
			new String(roleName)
	};
	this.handle(
			IProblem.MismatchingPackageForRole,
			args,
			args,
			start,
			end);
}
public void nonTeamPackageForRole(char[][] tokens, char[] fileName, int start, int end) {
	String[] args = new String[] {
		new String(CharOperation.concatWith(tokens, '.')),
		new String(fileName)
	};
	this.handle(
			IProblem.NonTeamPackageForRole,
			args,
			args,
			start,
			end);
}
public void noEnclosingTeamForRoleFile(CompilationUnitDeclaration roleUnit, TypeDeclaration roleType) {
	this.referenceContext = roleType;
	String[] args = new String[]{
		new String(CharOperation.concatWith(roleUnit.currentPackage.tokens, '.')),
		new String(roleType.sourceName())
	};
	this.handle(
			IProblem.NoEnclosingTeamForRoleFile,
			args,
			args,
			roleUnit.currentPackage.sourceStart,
			roleUnit.currentPackage.sourceEnd);
}
// -- 1.3 --
public void regularExtendsTeam(SourceTypeBinding sourceType, TypeReference reference, ReferenceBinding superclass) {
    String[] args1 = new String[]{new String(superclass.readableName())};
    String[] args2 = new String[]{new String(superclass.readableName())};
    this.handle(
        IProblem.RegularExtendsTeam,
        args1,
        args2,
        reference.sourceStart,
        reference.sourceEnd);
}

public void overridingFinalRole(TypeDeclaration subRoleDecl, ReferenceBinding superRole) {
	String[] args = { new String(superRole.readableName()) };
	this.handle(
			IProblem.OverridingFinalRole,
			args,
			args,
			subRoleDecl.sourceStart,
			subRoleDecl.sourceEnd);
}

public void roleClassIfcConflict(TypeDeclaration ifcPart) {
	String[] args = new String[] {
			new String(ifcPart.name)
	};
	int problemId = ifcPart.isRegularInterface() ? IProblem.RoleInterfaceOverridesClass : IProblem.RoleClassOverridesInterface;
	this.handle(
			problemId,
			args,
			args,
			ifcPart.sourceStart,
			ifcPart.sourceEnd);
}

public void tsuperOutsideRole(
        AbstractMethodDeclaration context,
        Statement                 tsuperCall,
        ReferenceBinding          roleType)
{
    this.referenceContext = context;
    String[] args = new String[] {
        new String(context.binding.readableName()),
        new String(roleType.sourceName())
    };
    this.handle(
            IProblem.TSuperOutsideRole,
            args,
            args,
            tsuperCall.sourceStart,
            tsuperCall.sourceEnd);
}
public void tsuperCallWithoutTsuperRole(ReferenceBinding roleType, Statement tsuperCall) {
	String[] args = new String[]   {
			new String(roleType.readableName())
	};
	this.handle(
			IProblem.TSuperCallWithoutTSuperRole,
			args,
			args,
			tsuperCall.sourceStart,
			tsuperCall.sourceEnd);
}
public void invalidQualifiedTSuper(Statement tsuperCall,
		   ReferenceBinding qualifyingType,
		   ReferenceBinding roleType)
{
	String[] args = new String[] {
			new String(roleType.readableName()),
			qualifyingType != null ? new String(qualifyingType.readableName()) : "<no super class>" //$NON-NLS-1$
	};
	this.handle(
			IProblem.InvalidQualifiedTSuper,
			args,
			args,
			tsuperCall.sourceStart,
			tsuperCall.sourceEnd);
}
public void tsuperCallsWrongMethod(TSuperMessageSend send) {
	this.handle(
		IProblem.TSuperCallsWrongMethod,
		NoArgument,
		NoArgument,
		send.sourceStart,
		send.sourceEnd);
}
public void reducingRoleVisibility(
		TypeDeclaration roleInterfaceTypeDeclaration,
        int modifiers,
        int inheritedModifiers)
{
    String[] arguments = new String[]{
        new String(roleInterfaceTypeDeclaration.binding.sourceName()),
        Protections.toString(modifiers),
        Protections.toString(inheritedModifiers)
    };
    this.handle(
        IProblem.ReducingRoleVisibility,
        arguments,
        arguments,
        roleInterfaceTypeDeclaration.sourceStart,
        roleInterfaceTypeDeclaration.sourceEnd
    );
}
public void tsubMethodReducesVisibility(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		IProblem.TSubMethodReducesVisibility,
		new String[] {new String(inheritedMethod.declaringClass.readableName())},
		new String[] {new String(inheritedMethod.declaringClass.shortReadableName())},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void extendIncompatibleEnclosingTypes(
	       TypeDeclaration clazz,
	       ReferenceBinding other,
	       ReferenceBinding otherEnclosing)
{
    this.referenceContext = clazz;
    String[] args = new String[] {
       new String(other.sourceName()),
       new String(otherEnclosing.sourceName())
    };
    this.handle(
           IProblem.ExtendIncompatibleEnclosingTypes,
           args,
           args,
           clazz.superclass.sourceStart,
           clazz.superclass.sourceEnd);
}
public void incompatibleSuperclasses(
	       ASTNode location, ReferenceBinding newSuperclass, ReferenceBinding oldSuperclass, ReferenceBinding outerTSuper)
{
	 String outerTSuperName = outerTSuper == null ? "<unknown>" : String.valueOf(outerTSuper.readableName());
	 this.handle(
			 IProblem.IncompatibleSuperclasses,
			 new String[]{new String(newSuperclass.readableName()), new String(oldSuperclass.readableName()), outerTSuperName},
			 new String[]{new String(newSuperclass.shortReadableName()), new String(oldSuperclass.shortReadableName()), outerTSuperName},
			 location.sourceStart,
			 location.sourceEnd);
}
public void tsuperCtorDespiteRefinedExtends (Statement tsuperCall, ReferenceBinding superclass)
{
	String[] args = new String[] {
			new String(superclass.readableName())
	};
	this.handle(
			IProblem.TsuperCtorDespiteRefinedExtends,
			args,
			args,
			tsuperCall.sourceStart,
			tsuperCall.sourceEnd);
}
public void callToInheritedNonPublic(MessageSend messageSend, MethodBinding method) {
	String[] args = {
			new String(method.readableName()),
			new String(method.declaringClass.readableName()),
			new String(messageSend.actualReceiverType.readableName())
	};
	this.handle(IProblem.CallToInheritedNonPublic, args, args, messageSend.sourceStart, messageSend.sourceEnd);
}
// -- 1.4 --
public void roleShadowsVisibleType(TypeDeclaration roleType, TypeBinding otherType)
{
	// already reported plain-java version of this error?
	CompilationResult result = this.referenceContext.compilationResult();
	for (CategorizedProblem problem : result.getAllProblems())
		if (problem.getID() == IProblem.HidingEnclosingType)
			if (problem.getSourceStart() == roleType.sourceStart)
				return; // don't report again
	String[] arguments = new String[] {
			new String(roleType.name),
			new String(otherType.readableName())
		};
	this.handle(
			IProblem.RoleShadowsVisibleType,
			arguments,
			arguments,
			roleType.sourceStart,
			roleType.sourceEnd);
}
public void implicitlyHideField(FieldDeclaration field)
{
	String[] arguments = new String[]{
			new String(field.name)
	};
	this.handle(
		IProblem.ImplicitlyHidingField,
		arguments,
		arguments,
		field.sourceStart,
		field.sourceEnd);

}
// -- 1.5 --
public void regularOverridesTeam(TypeDeclaration type, ReferenceBinding tsuperclass) {
    String[] args1 = new String[]{new String(tsuperclass.readableName())};
    String[] args2 = new String[]{new String(tsuperclass.readableName())};
    this.handle(
        IProblem.RegularOverridesTeam,
        args1,
        args2,
        type.sourceStart,
        type.sourceEnd);
}
public void missingTeamForRoleWithMembers(SourceTypeBinding type, TypeDeclaration member) {
	this.referenceContext = member;
	String [] args = new String[] {
			new String(type.readableName()),
			new String(OTNameUtils.removeOTDelim(member.name))
	};
	this.handle(
		IProblem.MissingTeamForRoleWithMembers,
		args,
		args,
		member.sourceStart,
		member.sourceEnd);
}
public void teamExtendingEnclosing(TypeDeclaration clazz, ReferenceBinding superteam) {
	String[] args = new String[] {
			new String(clazz.binding.readableName()),
			new String(superteam.readableName())
	};
	this.handle(
			IProblem.TeamExtendingEnclosing,
			args,
			args,
			clazz.superclass.sourceStart,
	        clazz.superclass.sourceEnd);
}
public void incomparableTSupers(TypeDeclaration roleDecl, ReferenceBinding type1, ReferenceBinding type2)
{
	String[] args = new String[] {
		new String(roleDecl.sourceName()),
		new String(type1.readableName()),
		new String(type2.readableName())
	};

	int start;
	int end;
	if (roleDecl.isPurelyCopied) {
		TypeDeclaration enclosing = roleDecl.scope.parent.referenceType();
		start = enclosing.sourceStart;
		end = enclosing.sourceEnd;
	} else {
		start = roleDecl.sourceStart;
		end = roleDecl.sourceEnd;
	}
	this.handle(
			IProblem.IncomparableTSupers,
			args,
			args,
			start,
			end);
}
public void roleMustOverride(TypeDeclaration typeDecl) {
	String[] args = { new String(typeDecl.binding.readableName()) };
	this.handle(
			IProblem.RoleMustOverride,
			args, args,
			typeDecl.sourceStart,
			typeDecl.sourceEnd);
}
public void missingOverrideAnnotationForRole(TypeDeclaration typeDecl) {
	String[] args = { new String(typeDecl.binding.readableName()) };
	this.handle(
			IProblem.MissingOverrideAnnotationForRole,
			args, args,
			typeDecl.sourceStart,
			typeDecl.sourceEnd);
}
// ====== ROLE-BASE-BINDING: =====
// -- 2.1 --
public void overlappingRoleHierarchies(TypeDeclaration roleDecl, String msg) {
    String[] args = new String[]{msg};
    this.handle(
        IProblem.OverlappingRoleHierarchies,
        args,
        args,
        roleDecl.sourceStart,
        roleDecl.sourceEnd);
}
public void illegalPlayedByRedefinition(ASTNode location,
										ReferenceBinding declaredBase,
										ReferenceBinding superRole,
										ReferenceBinding inheritedBase)
{
	String[] args= { new String(declaredBase.readableName()),
			         new String(inheritedBase.readableName()),
			         new String(superRole.readableName())};
	this.handle(
			IProblem.IllegalPlayedByRedefinition,
			args, args,
			location.sourceStart, location.sourceEnd);
}
public void incompatibleBaseclasses(
	       TypeDeclaration type, int sStart, int sEnd, ReferenceBinding binding, ReferenceBinding next)
{
    this.referenceContext = type;
    this.handle(
       IProblem.IncompatibleBaseclasses,
       new String[]{new String(binding.readableName()), new String(next.readableName())},
       new String[]{new String(binding.shortReadableName()), new String(next.shortReadableName())},
       sStart,
       sEnd);
}
public void overridesPlayedBy (TypeDeclaration role, TypeBinding origBase)
{
    this.referenceContext = role;
    String[] msgArgs = new String[3];
    msgArgs[0] = new String(((ReferenceBinding)role.binding).sourceName());
    msgArgs[1] = role.baseclass.toString();
    msgArgs[2] = new String(origBase.shortReadableName());
    role.binding.tagBits |= TagBits.BaseclassHasProblems;
    this.handle(
        IProblem.OverridingPlayedBy,
        NoArgument,
        msgArgs,
        role.baseclass.sourceStart,
        role.baseclass.sourceEnd);
}
public void baseclassMustBeAClass(SourceTypeBinding type, TypeReference baseclassRef, ReferenceBinding baseType) {
	 this.handle(
		 IProblem.BaseclassMustBeAClass,
		 new String[] {new String(baseType.readableName()), new String(type.sourceName())},
		 new String[] {new String(baseType.shortReadableName()), new String(type.sourceName())},
		 ProblemSeverities.Warning,
		 baseclassRef.sourceStart,
		 baseclassRef.sourceEnd);
}
public void baseclassIsRoleOfTheSameTeam(SourceTypeBinding type, TypeReference baseclassRef, ReferenceBinding baseType) {
	this.handle(
		IProblem.BaseclassIsRoleOfTheSameTeam,
		new String[] {new String(baseType.readableName()), new String(type.sourceName())},
		new String[] {new String(baseType.shortReadableName()), new String(type.sourceName())},
		baseclassRef.sourceStart,
		baseclassRef.sourceEnd);
}
public void playedByEnclosing(SourceTypeBinding type, TypeReference baseclass, ReferenceBinding baseType) {
	this.handle(
			IProblem.BaseclassIsEnclosing,
			new String[] {new String(baseType.readableName()), new String(type.sourceName())},
			new String[] {new String(baseType.shortReadableName()), new String(type.sourceName())},
			baseclass.sourceStart,
			baseclass.sourceEnd);
}
public void baseclassIsMember(SourceTypeBinding type, TypeReference baseclassRef, ReferenceBinding baseclass) {
	baseclass = baseclass.getRealType();
	this.handle(
			IProblem.BaseclassIsMember,
			new String[] {new String(baseclass.readableName()), new String(type.sourceName())},
			new String[] {new String(baseclass.shortReadableName()), new String(type.sourceName())},
			baseclassRef.sourceStart,
			baseclassRef.sourceEnd);
}
public void baseImportInRegularClass(TypeDeclaration firstType, ImportReference reference) {
	String[] args = {
		(firstType != null) ? new String(firstType.name) : "<no type>"	 //$NON-NLS-1$
	};
	this.handle(
			IProblem.BaseImportInRegularClass,
			args,
			args,
			reference.sourceStart,
			reference.sourceEnd);
}
public void baseImportInRoleFile(ImportReference reference) {
	this.handle(
			IProblem.BaseImportInRoleFile,
			NoArgument,
			NoArgument,
			reference.sourceStart,
			reference.sourceEnd);
}

public void regularlyImportedBaseclass(TypeReference typeReference) {
	String[] args = {new String(CharOperation.concatWith(typeReference.getTypeName(), '.')) };
	this.handle(
			IProblem.RegularlyImportedBaseclass,
			args,
			args,
			typeReference.sourceStart,
			typeReference.sourceEnd);

}
public void qualifiedReferenceToBaseclass(TypeReference baseclassRef) {
	String[] args = {String.valueOf(CharOperation.concatWith(baseclassRef.getTypeName(), '.')) };
	this.handle(IProblem.QualifiedReferenceToBaseclass, args, args, baseclassRef.sourceStart, baseclassRef.sourceEnd);
}
public void parameterizedBaseclass(TypeReference baseclassRef, ReferenceBinding baseclass) {
	String[] args = {new String(baseclass.readableName())};
	this.handle(
		IProblem.ParameterizedBaseclass,
		args,
		args,
		baseclassRef.sourceStart,
		baseclassRef.sourceEnd);
}
public void nonParameterizedBaseclass(TypeReference baseclassRef, ReferenceBinding roleClass) {
	String[] args = {new String(roleClass.readableName())};
	this.handle(
		IProblem.NonParameterizedBaseclass,
		args,
		args,
		baseclassRef.sourceStart,
		baseclassRef.sourceEnd);
}
public void deprecatedBaseclass(TypeReference baseclassRef, TypeBinding baseclass) {
	String[] args = {new String(baseclass.readableName())};
	this.handle(
		IProblem.DeprecatedBaseclass,
		args,
		args,
		baseclassRef.sourceStart,
		baseclassRef.sourceEnd);
}
// triggered by the compiler.adaptor:
public void illegalBaseImport(ImportReference ref, String expectedBasePlugin, String actualBasePlugin) {
	String[] args;
	int problemId;
	if (actualBasePlugin == null) {
		args = new String[]{expectedBasePlugin};
		problemId = IProblem.IllegalBaseImport;
	} else {
		args = new String[]{expectedBasePlugin, actualBasePlugin};
		problemId = IProblem.IllegalBaseImportExpected;
	}
	this.handle(
			problemId,
			args,
			args,
			ref.sourceStart,
			ref.sourceEnd);
}
public void illegalBaseImportNoAspectBinding(ImportReference ref, String projectName) {
	String[] args = {projectName};
	this.handle(
			IProblem.IllegalBaseImportNoAspectBinding,
			args,
			args,
			ref.sourceStart,
			ref.sourceEnd);

}
public void baseImportFromSplitPackage(ImportReference ref, String expectedPlugin) {
	String[] args = { 
			new String(CharOperation.concatWith(ref.tokens, '.')),
			expectedPlugin 
	};
	this.handle(
			expectedPlugin.charAt(0) == '[' 
				? IProblem.BaseImportFromSplitPackagePlural 
				: IProblem.BaseImportFromSplitPackage,
			args, args, 
			ProblemSeverities.Warning,
			ref.sourceStart, ref.sourceEnd);
}
// -- 2.2 --
public void illegalImplicitLower(Expression expr, TypeBinding leftType, TypeBinding rightType)
{
    String[] args = new String[]{
        new String(leftType.readableName()),
        new String(rightType.readableName())
    };
    int problemId = IProblem.IllegalImplicitLower;
    if (expr instanceof CastExpression)
    	problemId = IProblem.OmitCastForLowering;
    else if (expr instanceof InstanceOfExpression)
    	problemId = IProblem.UseTmpForLoweringInstanceof;
    this.handle(
        problemId,
        args,
        args,
        expr.sourceStart,
        expr.sourceEnd);
}
public void ambiguousUpcastOrLowering(ASTNode location, TypeBinding expectedType, ReferenceBinding providedType) {
	String[] args  = { String.valueOf(providedType.readableName()),
					   String.valueOf(expectedType.readableName()),
					   String.valueOf(providedType.baseclass().readableName()) };
	this.handle(IProblem.AmbiguousUpcastOrLowering, args, args, location.sourceStart, location.sourceEnd);	
}
//-- 2.3 --
public void noDefaultCtorInBoundRole(MessageSend send, MethodBinding binding) {
	String[] arguments = new String[] {new String(binding.readableName())};
	this.handle(
			IProblem.NoDefaultCtorInBoundRole,
			arguments,
			arguments,
			send.sourceStart,
			send.sourceEnd);
}
public void missingEmptyCtorForLiftingCtor(
        TypeDeclaration roleDecl,
        ReferenceBinding parent)
{
	roleDecl.binding.tagBits |= TagBits.HasLiftingProblem;
    String[] arguments = new String[] {new String(parent.sourceName())};
    this.handle(
        IProblem.MissingEmptyCtorForLiftingCtor,
        arguments,
        arguments,
        roleDecl.sourceStart,
        roleDecl.sourceEnd);
}
public void explicitSuperInLiftConstructor(
        TypeDeclaration roleType,
        ConstructorDeclaration constructor)
{
    this.referenceContext = constructor;
    this.handle(
        IProblem.ExplicitSuperInLiftConstructor,
        NoArgument,
        NoArgument,
        constructor.constructorCall.sourceStart,
        constructor.constructorCall.sourceEnd);
}
public void declaredLiftingInStaticMethod(AbstractMethodDeclaration method, Argument argument) {
	String[] args = new String[] {new String(argument.name) };
	this.handle(
			IProblem.DeclaredLiftingInStaticMethod,
			args,
			args,
			argument.type.sourceStart,
			argument.type.sourceEnd);

}
public void qualifiedLiftingType(TypeReference roleReference, TypeBinding teamBinding) {
	String[] args = new String[] {new String(teamBinding.readableName())};
	this.handle(
			IProblem.QualifiedLiftingType,
			args,
			args,
			roleReference.sourceStart,
			roleReference.sourceEnd);
}
public void liftingTypeNotAllowedHere(ReferenceContext context, TypeReference reference) {
	this.referenceContext = context;
	this.handle(
			IProblem.LiftingTypeNotAllowedHere,
			NoArgument,
			NoArgument,
			reference.sourceStart,
			reference.sourceEnd);
}
public void primitiveTypeNotAllowedForLifting(
        TypeDeclaration declaration,
        ASTNode 	    ref,
        TypeBinding     baseType)
{
    this.referenceContext = declaration;
    String[] args = new String[] {
        new String(baseType.sourceName())
    };
    this.handle(
            IProblem.PrimitiveTypeNotAllowedForLifting,
            args,
            args,
            ref.sourceStart,
            ref.sourceEnd);
}
public void needRoleInLiftingType(
        TypeDeclaration declaration,
        TypeReference ref,
        TypeBinding type)
{
    this.referenceContext = declaration;
    String[] args = new String[] {
        new String(type.sourceName())
    };
    this.handle(
            IProblem.NeedRoleInLiftingType,
            args,
            args,
            ref.sourceStart,
            ref.sourceEnd);
}
public void roleNotBoundCantLift(
        TypeDeclaration declaration,
        TypeReference roleReference,
        TypeBinding   roleType)
{
    this.referenceContext = declaration;
    String[] args = new String[] {
        new String(roleType.sourceName())
    };
    this.handle(
            IProblem.RoleNotBoundCantLift,
            args,
            args,
            roleReference.sourceStart,
            roleReference.sourceEnd);
}
public void incompatibleBaseForRole(
        TypeDeclaration declaration,
        TypeReference reference,
        TypeBinding roleType,
        TypeBinding baseType)
{
    String[] args = new String[] {
        new String(baseType.sourceName()),
        new String(roleType.sourceName()),
		new String(((ReferenceBinding)roleType).baseclass().sourceName())
    };
    this.handle(
            IProblem.IncompatibleBaseForRole,
            args,
            args,
            reference.sourceStart,
            reference.sourceEnd);
}
public void syntaxErrorInDeclaredArrayLifting(LiftingTypeReference liftingType)
{
	this.handle(
		IProblem.SyntaxErrorInDeclaredArrayLifting,
		NoArgument,
		NoArgument,
		liftingType.sourceStart,
		liftingType.sourceEnd);
}

public void roleBoundIsNotRole(TypeReference reference, ReferenceBinding bound) {
	String[] args = new String[] {
        new String(bound.sourceName())
    };
    this.handle(
            IProblem.RoleBoundIsNotRole,
            args,
            args,
            reference.sourceStart,
            reference.sourceEnd);

}

public void potentiallyAmbiguousRoleBinding(TypeDeclaration teamDecl, HashSet<RoleModel> ambiguousRoles)
{
	this.referenceContext = teamDecl;

	String[] msgArgs = new String[2];
    char[][] details = new char[ambiguousRoles.size()][];

    Iterator<RoleModel> iter = ambiguousRoles.iterator();
    int i = 0;
    while (iter.hasNext()) {
        RoleModel role= iter.next();
        if (msgArgs[0] == null)
            msgArgs[0] = new String(role.getBaseTypeBinding().sourceName());
        details[i++] = role.getBinding().readableName();
    }
    Arrays.sort(details, new CharArrayComparator());
    msgArgs[1] = new String(CharOperation.concatWith(details, ','));

	this.handle(
		IProblem.RoleBindingPotentiallyAmbiguous,
	    NoArgument,
	    msgArgs,
		teamDecl.sourceStart,
		teamDecl.sourceEnd);
}
public void definiteLiftingAmbiguity(TypeBinding provided, TypeBinding required, ASTNode location)
{
	String[] args = new String[]{
		new String(provided.readableName()),
		new String(required.readableName())
	};
	this.handle(
			IProblem.DefiniteLiftingAmbiguity,
			args,
			args,
			location.sourceStart,
			location.sourceEnd);
}
// -- 2.4 --
public void qualifiedUseOfLiftingConstructor (MethodBinding ctor, Expression allocation)
{
	String[] args = new String[] {
			new String(ctor.readableName())
	};
	this.handle(
			IProblem.QualifiedUseOfLiftingConstructor,
			args,
			args,
			allocation.sourceStart,
			allocation.sourceEnd);
}
public void liftCtorArgNotAllocation(MethodBinding ctor, Expression allocation, ReferenceBinding baseclass)
{
	String[] args = new String[] {
			String.valueOf(ctor.readableName()),
			String.valueOf(baseclass.readableName())
	};
	this.handle(
			IProblem.LiftCtorArgNotAllocation,
			args,
			args,
			allocation.sourceStart,
			allocation.sourceEnd);
}
public void baseConstructorCallInWrongMethod(
        BaseAllocationExpression expression, ReferenceContext enclosingMethod)
{
    String[] arguments = new String[1];
    if (enclosingMethod instanceof AbstractMethodDeclaration)
    	if (((AbstractMethodDeclaration)enclosingMethod).binding != null)
    		arguments[0] = new String(
    				((AbstractMethodDeclaration)enclosingMethod).binding.readableName());
    	else
    		arguments[0] = new String(((AbstractMethodDeclaration)enclosingMethod).selector);
    else
        arguments[0] = "<clinit> of "+ //$NON-NLS-1$
                new String(((TypeDeclaration)this.referenceContext).binding.readableName());

    this.handle(
        IProblem.BaseCtorInWrongMethod,
        arguments,
        arguments,
        expression.sourceStart,
        expression.sourceEnd);
}
public void baseConstructorCallInLiftingConstructor(ConstructorDeclaration ctor) {
    String[] arguments = new String[0];
    this.handle(
        IProblem.BaseConstructorCallInLiftingConstructor,
        arguments,
        arguments,
        ctor.sourceStart,
        ctor.sourceEnd);
}
public void callsCtorWithMismatchingBaseCtor(
		ExplicitConstructorCall selfCall,
		ReferenceBinding declaringClass,
		ReferenceBinding requiredBase,
		ReferenceBinding createdBase)
{
	String[] args = new String[] {
		new String(declaringClass.readableName()),
		new String(requiredBase.readableName()),
		new String(createdBase.readableName())
	};
	this.handle(
		IProblem.CallsCtorWithMismatchingBaseCtor,
		args,
		args,
		selfCall.sourceStart,
		selfCall.sourceEnd);
}
public void baseConstructorCallIsNotFirst(BaseAllocationExpression expression) {
    String[] arguments = new String[0];
    this.handle(
        IProblem.BaseCtorCallIsNotFirst,
        arguments,
        arguments,
        expression.sourceStart,
        expression.sourceEnd);
}
public void baseConstructorExpressionOutsideCtorCall(BaseAllocationExpression expression) {
    String[] arguments = new String[0];
    this.handle(
        IProblem.BaseConstructorExpressionOutsideCtorCall,
        arguments,
        arguments,
        expression.sourceStart,
        expression.sourceEnd);
}
public void missingCallToBaseConstructor(
        ConstructorDeclaration ctor,
        ReferenceBinding       declaringClass)
{
    String[] arguments = new String[] {new String(declaringClass.sourceName())};
    this.handle(
        IProblem.MissingCallToBaseConstructor,
        arguments,
        arguments,
        ctor.sourceStart,
        ctor.sourceEnd);
}
public void tooManyCallsToBaseConstructor(
        ASTNode location,
        ExplicitConstructorCall ctorCall)
{
    String[] arguments = new String[] {ctorCall.toString()};
    this.handle(
        IProblem.TooManyCallsToBaseConstructor,
        arguments,
        arguments,
        location.sourceStart,
        location.sourceEnd);
}
public void roleConstructorHiddenByLiftingConstructor(ConstructorDeclaration constructor) {
	this.handle(IProblem.RoleConstructorHiddenByLiftingConstructor,
			    NoArgument, NoArgument, constructor.sourceStart, constructor.sourceEnd);
}
public void instantiatingSupercededRole(AllocationExpression expression, ReferenceBinding subRole) {
	String[] args = { new String(subRole.sourceName()) };
	this.handle(
			IProblem.InstantiatingSupercededRole,
			args,
			args,
			expression.sourceStart,
			expression.sourceEnd);
}
// -- 2.5 --
public void abstractPotentiallyRelevantRole(TypeDeclaration roleDeclaration, ReferenceBinding teamBinding) {
    this.referenceContext = roleDeclaration;
    teamBinding.tagBits |= TagBits.HasAbstractRelevantRole;
    String[] args = new String[] {
        new String(teamBinding.sourceName()),
        roleDeclaration.binding == null
	    ? new String(roleDeclaration.name)
	    : new String(roleDeclaration.binding.sourceName())
    };
    this.handle(
        IProblem.AbstractPotentiallyRelevantRole,
        args,
        args,
        roleDeclaration.sourceStart,
        roleDeclaration.sourceEnd);
}
public void abstractRelevantRole(TypeDeclaration roleDecl, ReferenceBinding teamBinding) {
	roleDecl.binding.tagBits |= TagBits.HasLiftingProblem;
    this.referenceContext = roleDecl;
    String[] args = new String[] {
        new String(teamBinding.sourceName()),
        roleDecl.binding == null
		    ? new String(roleDecl.name)
		    : new String(roleDecl.binding.sourceName())
    };
    this.handle(
        IProblem.AbstractRelevantRole,
        args,
        args,
        roleDecl.sourceStart,
        roleDecl.sourceEnd);
}
public void abstractRoleIsRelevant(Expression creation, TypeBinding roleBinding) {
	roleBinding.tagBits |= TagBits.HasLiftingProblem;
    String[] args = new String[] {
    	new String(((ReferenceBinding)roleBinding).enclosingType().sourceName),
        new String(roleBinding.sourceName())
    };
    this.handle(
        IProblem.AbstractRelevantRole,
        args,
        args,
        creation.sourceStart,
        creation.sourceEnd);
}
// -- 2.6 --
// BaseQualifiesNonTeam is reported through illegalTypeAnchorNotATeam and invalidType.
public void unboundQualifiedBase(ReferenceBinding prefix, ASTNode location) {
	String[] args = { new String(prefix.readableName()) };
	this.handle(
			IProblem.UnboundQualifiedBase,
			args,
			args,
			location.sourceStart,
			location.sourceEnd);
}
public void basecallInRegularMethod(ASTNode location, AbstractMethodDeclaration decl)
{
	char[] methodName = (decl.binding != null) ?
				decl.binding.readableName() :
				decl.selector;
	String[] arguments = new String[] { new String(methodName) };
	int problemId = decl.isMappingWrapper.any() ?
						IProblem.BasecallInMethodMapping :
						IProblem.BasecallInRegularMethod ;
	this.handle(
				problemId,
				arguments,
				arguments,
				location.sourceStart,
				location.sourceEnd
			);
}
public void baseCallOutsideMethod(ASTNode reference) {
	this.handle(
			IProblem.BaseCallOutsideMethod,
			NoArgument,
			NoArgument,
			reference.sourceStart,
			reference.sourceEnd);
}
public void baseAllocationDespiteBaseclassCycle(BaseAllocationExpression baseAllocation, TypeDeclaration roleDecl) {
	String[] arguments = { String.valueOf(roleDecl.binding.readableName())};
	this.handle(IProblem.BaseAllocationDespiteBaseclassCycle, arguments, arguments, baseAllocation.sourceStart, baseAllocation.sourceEnd);
}
// ====== METHOD_MAPPINGS (generic) =====
// -- 3.1 / 4.1 --
public void methodMappingNotInBoundRole(
        AbstractMethodMappingDeclaration mappingDeclaration,
        TypeDeclaration roleClass)
{
    this.referenceContext = roleClass;
    String[] args = new String[] {
        new String(roleClass.binding.sourceName())
    };
    this.handle(
    		roleClass.isRole() ?
    			mappingDeclaration.isCallout() ?
            		IProblem.CalloutMappingInUnboundRole: IProblem.CallinMappingInUnboundRole :
    			mappingDeclaration.isCallout() ?
                	IProblem.CalloutMappingInNonRole: IProblem.CallinMappingInNonRole,
            args,
            args,
            mappingDeclaration.sourceStart,
            mappingDeclaration.sourceEnd);
}
public void unresolvedMethodSpec(MethodSpec spec, ReferenceBinding type, boolean isCallout)
{
	String[] args = new String[] {
			spec.toString(),
			new String(type.readableName())
	};
	this.handle(
			isCallout? IProblem.UnresolvedCalloutMethodSpec : IProblem.UnresolvedCallinMethodSpec,
			args,
			args,
			spec.declarationSourceStart,
			spec.declarationSourceEnd);
}
public void ambiguousMethodMapping(MethodSpec methodSpec, ReferenceBinding type, boolean isCallout)
{
	String[] args = new String[] {
		new String(methodSpec.selector),
		new String(type.readableName())
	};
	this.handle(
			isCallout ? IProblem.AmbiguousCalloutMethodSpec : IProblem.AmbiguousCallinMethodSpec,
			args,
			args,
			methodSpec.sourceStart,
			methodSpec.declarationSourceEnd);

}
public void differentReturnInMethodSpec(MethodSpec spec, boolean isCallout) {
	String[] args = new String[] {
			new String(MethodModel.getReturnType(spec.resolvedMethod).shortReadableName()),
			new String(spec.returnType.resolvedType.shortReadableName())
	};
	this.handle(
			isCallout ? IProblem.DifferentReturnInCalloutMethodSpec : IProblem.DifferentReturnInCallinMethodSpec,
			args,
			args,
			spec.returnType.sourceStart,
			spec.returnType.sourceEnd);
}
public void differentParamInMethodSpec(MethodSpec spec, TypeReference specified, TypeBinding resolved, boolean isCallout)
{
	String[] args = new String[] {
			new String(resolved.shortReadableName()),
			new String(specified.resolvedType.shortReadableName())
	};
	this.handle(
			isCallout ? IProblem.DifferentParamInCalloutMethodSpec : IProblem.DifferentParamInCallinMethodSpec,
			args,
			args,
			specified.sourceStart,
			specified.sourceEnd);
}
public void callinCalloutUndeclaredException(
		ReferenceBinding exc,
		AbstractMethodMappingDeclaration bind)
{
	String[] args = new String[] {
			new String(exc.readableName())
	};
	this.handle(
			bind.isCallout() ?
					IProblem.CalloutUndeclaredException :
					IProblem.CallinUndeclaredException,
			args,
			args,
			bind.sourceStart,
			bind.sourceEnd);
}
//-- 3.2 / 4.4 --
public void tooFewArgumentsInMethodMapping(MethodSpec roleMethodSpec, MethodSpec baseMethodSpec, boolean isCallout)
{
	String[] args = new String[] {
			new String(roleMethodSpec.readableName()),
			new String(baseMethodSpec.readableName())
	};
	this.handle(
			isCallout ? IProblem.TooFewArgumentsInCallout : IProblem.TooFewArgumentsInCallin,
			args,
			args,
			baseMethodSpec.sourceStart,
			baseMethodSpec.declarationSourceEnd);
}
public void illegalDirectionForResult(Expression expr, boolean dirCallout)
{
    String[] args = new String[0];
    this.handle(
            dirCallout ? IProblem.IllegalDirectionForCalloutResult : IProblem.IllegalDirectionForCallinResult,
            args,
            args,
            expr.sourceStart,
            expr.sourceEnd);
}
public void duplicateParamMapping(
	    ParameterMapping paramMapping,
	    char[]           argName,
		boolean          isCallout)
{
    String[] arguments = new String[] { new String(argName) };
    this.handle(
        isCallout? IProblem.DuplicateCalloutParamMapping : IProblem.DuplicateCallinParamMapping,
        arguments,
        arguments,
        paramMapping.ident.sourceStart,
        paramMapping.ident.sourceEnd);
}
public void resultNotDefinedForVoidMethod(Expression resultExpr, char[] methodName, boolean isCallout) {
	String[] args = new String[] { new String(methodName) };
	this.handle(
			isCallout ? IProblem.ResultNotDefinedForVoidMethodCallout : IProblem.ResultNotDefinedForVoidMethodCallin,
			args,
			args,
			resultExpr.sourceStart,
			resultExpr.sourceEnd);
}
public void unmappedParameter(
        char[]     argName,
        MethodSpec spec,
		boolean    isCallout)
{
    String[] args = new String[]{
        new String(argName),
        spec.toString()
    };
    this.handle(
            isCallout? IProblem.UnmappedBaseParameter : IProblem.UnmappedRoleParameter,
            args,
            args,
            spec.declarationSourceStart,
            spec.declarationSourceEnd);
}
public void mappingResultToOther(
		CalloutMappingDeclaration calloutMappingDeclaration,
		ParameterMapping          mapping)
{
	this.handle(
			(calloutMappingDeclaration.isCallin())?
					IProblem.CallinMappingResultToOther:
					IProblem.CalloutMappingResultToOther,
			NoArgument,
			NoArgument,
			mapping.sourceStart,
			mapping.sourceEnd);

}
// -- 3.3 / 4.5 --
public void typeMismatchInMethodMapping(
		Expression expression,
		TypeBinding constantType,
		TypeBinding expectedType,
		boolean isCallout)
{
	String constantTypeName = new String(constantType.readableName());
	String expectedTypeName = new String(expectedType.readableName());
	String constantTypeShortName = new String(constantType.shortReadableName());
	String expectedTypeShortName = new String(expectedType.shortReadableName());
	if (constantTypeShortName.equals(expectedTypeShortName)){
		constantTypeShortName = constantTypeName;
		expectedTypeShortName = expectedTypeName;
	}
	this.handle(
		isCallout ? IProblem.CalloutTypeMismatch: IProblem.CallinTypeMismatch,
		new String[] {constantTypeName, expectedTypeName},
		new String[] {constantTypeShortName, expectedTypeShortName},
		expression.sourceStart,
		expression.sourceEnd);
}
public void incompatibleMappedArgument(
		TypeBinding providedType,
		TypeBinding expectedType,
		MethodSpec spec,
		int idx,
		boolean isCallout)
{
	String[] args = new String[] {
			Integer.toString(idx+1),
			new String(spec.readableName()),
			new String(providedType.readableName()),
			new String(expectedType.readableName())
	};
	int s, e;
	if (spec.hasSignature) {
		s = spec.arguments[idx].sourceStart;
		e = spec.arguments[idx].sourceEnd;
	} else {
		s = spec.declarationSourceStart;
		e = spec.declarationSourceEnd;
	}
	this.handle(
			isCallout? IProblem.IncompatibleMappedCalloutArgument : IProblem.IncompatibleMappedCallinArgument,
			args,
			args,
			s, e);
}
public void returnRequiredInMethodMapping(MethodSpec methodSpec, TypeBinding returnType, boolean isCallout)
{
	String[] args = new String[] {
			new String(methodSpec.readableName()),
			new String(returnType.readableName())
	};
	this.handle(
			isCallout ? IProblem.ReturnRequiredInCalloutMethodMapping : IProblem.ReturnRequiredInCallinMethodMapping,
			args,
			args,
			methodSpec.declarationSourceStart,
			methodSpec.declarationSourceEnd);
}
public void paramMapInInterface(AbstractMethodMappingDeclaration mappingDeclaration,
								ReferenceBinding binding)
{
	mappingDeclaration.ignoreFurtherInvestigation= true;
	String[] args = new String[] {new String(binding.readableName())};
	this.handle(
			IProblem.ParamMapInInterface,
			args,
			args,
			mappingDeclaration.bodyStart,
			mappingDeclaration.bodyEnd);
}
// -- general typing errors (no OTJLD paragraph) --
public void typeMismatchErrorPotentialLower(
        ASTNode     location,
        TypeBinding providedType,
        TypeBinding expectedType,
        TypeBinding baseType)
{
    typeMismatchErrorPotentialTranslation(location, providedType, expectedType, baseType, "lowering");     //$NON-NLS-1$
}
public void typeMismatchErrorPotentialLift(ASTNode location, TypeBinding providedType, TypeBinding expectedType, TypeBinding baseType)
{
    typeMismatchErrorPotentialTranslation(location, providedType, expectedType, baseType, "lifting");    	 //$NON-NLS-1$
}
private void typeMismatchErrorPotentialTranslation(ASTNode location, TypeBinding providedType, TypeBinding expectedType, TypeBinding baseType, String operator) {
	String providedTypeName = new String(providedType.readableName());
    String expectedTypeName = new String(expectedType.readableName());
    String baseTypeName     = new String(baseType.readableName());
    String providedTypeShortName = new String(providedType.shortReadableName());
    String expectedTypeShortName = new String(expectedType.shortReadableName());
    String baseTypeShortName     = new String(baseType.shortReadableName());
    if (providedTypeShortName.equals(expectedTypeShortName)){
        providedTypeShortName = providedTypeName;
        expectedTypeShortName = expectedTypeName;
    }
    this.handle(
        IProblem.TypeMismatch,
        new String[] {providedTypeName, expectedTypeName+" nor "+baseTypeName+" (using "+operator+")"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        new String[] {providedTypeShortName, expectedTypeShortName+" nor "+baseTypeShortName+" (using "+operator+")"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        location.sourceStart,
        location.sourceEnd);
}
/* unspecific API for a number of problems. */
public void boundMethodProblem (MethodSpec spec, ReferenceBinding type, boolean isCallout)
{
	String[] args = new String[] {
			new String(spec.readableName()),
			new String(type.readableName())
	};
	int code = -1;
	int problemId = spec.problemId();
	if (spec instanceof FieldAccessSpec)
	{
		// invisible/ambigous don't apply here!
		if (problemId == ProblemReasons.NotFound && isCallout)
			code = IProblem.UnresolvedFieldInCallout;
	} else {
		MethodBinding shownMethod;
		switch (problemId) {
		case ProblemReasons.Ambiguous:  code = isCallout?
				IProblem.AmbiguousCalloutMethodSpec :
				IProblem.AmbiguousCallinMethodSpec;
			break;
		case ProblemReasons.NotFound:   code = isCallout?
				IProblem.UnresolvedCalloutMethodSpec :
			    IProblem.UnresolvedCallinMethodSpec;
			if (spec.hasSignature && spec.returnType == null)
				return; // is the case with illegal ctor reference in callout
			break;
		case ProblemReasons.NotVisible: code = IProblem.InvisibleMethodSpec;
			break;
		case ProblemReasons.ParameterBoundMismatch:
			// from invalidMethod():
			ProblemMethodBinding problemMethod = (ProblemMethodBinding) spec.resolvedMethod;
			ParameterizedGenericMethodBinding substitutedMethod = (ParameterizedGenericMethodBinding) problemMethod.closestMatch;
			shownMethod = substitutedMethod.original();
			int augmentedLength = problemMethod.parameters.length;
			TypeBinding inferredTypeArgument = problemMethod.parameters[augmentedLength-2];
			TypeVariableBinding typeParameter = (TypeVariableBinding) problemMethod.parameters[augmentedLength-1];
			TypeBinding[] invocationArguments = new TypeBinding[augmentedLength-2]; // remove extra info from the end
			System.arraycopy(problemMethod.parameters, 0, invocationArguments, 0, augmentedLength-2);
//{ObjectTeams: <B base R>:
			if (typeParameter.roletype != null) {
				this.handle(
					IProblem.GenericMethodTypeArgumentMismatchRoleBound,
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
					        new String(shownMethod.declaringClass.readableName()),
					        typesAsString(false, invocationArguments, false),
					        new String(inferredTypeArgument.readableName()),
					        new String(typeParameter.sourceName),
					        new String(typeParameter.roletype.readableName()) },
					new String[] {
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
					        new String(shownMethod.declaringClass.shortReadableName()),
					        typesAsString(false, invocationArguments, true),
					        new String(inferredTypeArgument.shortReadableName()),
					        new String(typeParameter.sourceName),
					        new String(typeParameter.roletype.shortReadableName()) },
					spec.sourceStart,
					spec.sourceEnd);
				return;
			}
// SH}
			this.handle(
				IProblem.GenericMethodTypeArgumentMismatch,
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false),
				        new String(shownMethod.declaringClass.readableName()),
				        typesAsString(false, invocationArguments, false),
				        new String(inferredTypeArgument.readableName()),
				        new String(typeParameter.sourceName),
				        parameterBoundAsString(typeParameter, false) },
				new String[] {
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true),
				        new String(shownMethod.declaringClass.shortReadableName()),
				        typesAsString(false, invocationArguments, true),
				        new String(inferredTypeArgument.shortReadableName()),
				        new String(typeParameter.sourceName),
				        parameterBoundAsString(typeParameter, true) },
				spec.sourceStart,
				spec.sourceEnd);
			return;
		case ProblemReasons.ParameterizedMethodTypeMismatch: 
			code = IProblem.ParameterizedMethodArgumentTypeMismatch;
			ProblemMethodBinding method = (ProblemMethodBinding) spec.resolvedMethod;
			shownMethod = method.closestMatch; 
			args = new String[] {
					new String(shownMethod.selector),
			        typesAsString(shownMethod.isVarargs(), shownMethod.getSourceParameters(), false),
			        new String(shownMethod.declaringClass.readableName()),
					typesAsString(false, ((ParameterizedGenericMethodBinding)shownMethod).typeArguments, false),
			        typesAsString(method.isVarargs(), method.getSourceParameters(), false) 
			};
			break;
		}
	}
	if (code == -1) {
		String[] msgs = new String[] {"unexpected problem reason "+problemId };  //$NON-NLS-1$ 
		this.handle(IProblem.MissingImplementation, msgs, msgs, spec.declarationSourceStart, spec.declarationSourceEnd);
	} else {
		this.handle(code, args, args, spec.declarationSourceStart, spec.declarationSourceEnd);
	}
}
// ====== CALLOUT ======
// -- combined 3.1 / 3.5 --
public void regularCalloutOverrides(CalloutMappingDeclaration mapping) {
	int problemId = (mapping.baseMethodSpec instanceof FieldAccessSpec) ?
			IProblem.FieldCalloutOverrides :
			IProblem.RegularCalloutOverrides;
	this.handle(
		problemId,
		NoArgument,
		NoArgument,
		mapping.roleMethodSpec.declarationSourceStart,
		mapping.roleMethodSpec.declarationSourceEnd);
}
public void abstractMethodBoundAsOverrideCallout(CalloutMappingDeclaration calloutDecl)
{
	this.handle(
		IProblem.AbstractMethodBoundAsOverride,
		NoArgument,
		NoArgument,
		calloutDecl.roleMethodSpec.declarationSourceStart,
		calloutDecl.roleMethodSpec.declarationSourceEnd
	);
}
public void calloutOverridesLocal(
        TypeDeclaration context,
        AbstractMethodMappingDeclaration mapping,
        MethodBinding localMeth)
{
    this.referenceContext = context;
    String[] args = new String[] {
        new String(localMeth.shortReadableName())
    };
    this.handle(
        IProblem.CalloutOverridesLocal,
        args,
        args,
        mapping.roleMethodSpec.declarationSourceStart,
        mapping.roleMethodSpec.declarationSourceEnd);
}
public void regularCalloutOverridesCallout(CalloutMappingDeclaration mapping, MethodBinding inheritedMethod)
{
	ReferenceBinding declaringClass = inheritedMethod.declaringClass;
	if (inheritedMethod.copyInheritanceSrc != null)
		declaringClass = inheritedMethod.copyInheritanceSrc.declaringClass;
	String[] args = new String[] {
			new String(declaringClass.readableName())
	};
	int problemId = (mapping.baseMethodSpec instanceof FieldAccessSpec) ?
			IProblem.FieldCalloutOverridesCallout :
			IProblem.RegularCalloutOverridesCallout;
	this.handle(
		problemId,
		args,
		args,
		mapping.roleMethodSpec.declarationSourceStart,
		mapping.roleMethodSpec.declarationSourceEnd);
}
public void duplicateCalloutBinding(ReferenceContext context, MethodSpec methodSpec)
{
	String[] arguments = new String[] { methodSpec.toString() };
	this.referenceContext = context;
	this.handle(
		IProblem.DuplicateCalloutBinding,
		arguments,
		arguments,
		methodSpec.declarationSourceStart,
		methodSpec.declarationSourceEnd
	);
}
public void addingInferredCalloutForInherited(TypeDeclaration type, MethodBinding abstractMethod) {
	String[] args = { new String(abstractMethod.shortReadableName()) };
	this.handle(
			IProblem.AddingInferredCalloutForInherited,
			args,
			args,
			type.sourceStart,
			type.sourceEnd);
}
public void usingInferredCalloutForMessageSend(MessageSend send) {
	String[] args = { new String(send.binding.shortReadableName()) };
	this.handle(
			IProblem.UsingInferredCalloutForMessageSend,
			args,
			args,
			send.sourceStart,
			send.sourceEnd);
}
public void usingInferredCalloutToFieldForMessageSend(MessageSend send) {
	String[] args = { new String(send.binding.shortReadableName()) };
	this.handle(
			IProblem.UsingInferredCalloutToFieldForMessageSend,
			args,
			args,
			send.sourceStart,
			send.sourceEnd);
}
public void inferredCalloutInCompoundAssignment(ASTNode location, char[] fieldName) {
	String[] args = { String.valueOf(fieldName) };
	this.handle(
			IProblem.InferredCalloutInCompoundAssignment,
			args,
			args,
			location.sourceStart,
			location.sourceEnd);
}

public void calloutToEnclosing(CalloutMappingDeclaration mapping, RoleModel role) {
	String[] args = { String.valueOf(role.getBinding().readableName()),
					  String.valueOf(role.getBaseTypeBinding().readableName()) };
	this.handle(IProblem.CalloutToEnclosing, args, args, mapping.sourceStart, mapping.sourceEnd);	
}
// -- 3.2 --
public void unusedParamMap(
        AbstractMethodMappingDeclaration mappingDeclaration,
        ParameterMapping         mapping)
{
    this.referenceContext = mappingDeclaration;
    String[] args = new String[] {
        new String(mapping.ident.token)
    };
    this.handle(
            IProblem.UnusedParamMap,
            args,
            args,
            ProblemSeverities.Warning,
            mapping.sourceStart,
            mapping.sourceEnd);
}
public void calloutParameterMappingMissingSignatures(CalloutMappingDeclaration calloutBinding, int end)
{
	this.referenceContext = calloutBinding;
	this.handle(
			IProblem.CalloutParameterMappingMissingSignatures,
			NoArgument,
			NoArgument,
			calloutBinding.bodyStart,
			end);
}
@SuppressWarnings("nls")
public void wrongBindingDirection(
        CalloutMappingDeclaration mappingDeclaration,
        ParameterMapping         mapping)
{
    this.referenceContext = mappingDeclaration;
    String[] args;
    int problemId;
    if (CharOperation.equals(mapping.ident.token, IOTConstants.RESULT)) {
    	args = new String[] {
   	        (mappingDeclaration.isCallout()) ? "<-" : "->"
   	    };
   	    problemId = IProblem.WrongBindingDirectionResult;
    } else {
    	args = new String[] {
	        new String(mapping.ident.token),
	        (mappingDeclaration.isCallout()) ? "->" : "<-"
	    };
	    problemId = IProblem.WrongBindingDirection;
    }
    this.handle(
            problemId,
            args,
            args,
            mapping.sourceStart,
            mapping.sourceEnd);
}
public void resultMappingForVoidMethod(
        AbstractMethodMappingDeclaration mappingDeclaration,
		MethodSpec                       declaredMethod,
        ParameterMapping                 mapping)
{
    this.referenceContext = mappingDeclaration;
    String method = null;
    if (mappingDeclaration.isCallout()) {
        method = "role method "+new String(declaredMethod.selector); //$NON-NLS-1$
    } else {
        method = "base method "+new String(declaredMethod.selector); //$NON-NLS-1$
    }
    String[] args = new String[] {
        method
    };
    this.handle(
            IProblem.ResultMappingForVoidMethod,
            args,
            args,
            mapping.sourceStart,
            mapping.sourceEnd);
}
public void resultMappingForVoidMethod(
        AbstractMethodMappingDeclaration mappingDeclaration,
        ResultReference           result)
{
    String method = null;
    if (mappingDeclaration.isCallout()) {
        method = "role method "+((CalloutMappingDeclaration)mappingDeclaration).roleMethodSpec; //$NON-NLS-1$
    } else {
        method = "base methods"; //$NON-NLS-1$
    }
    String[] args = new String[] {
        method
    };
    this.handle(
            IProblem.ResultMappingForVoidMethod,
            args,
            args,
            result.sourceStart,
            result.sourceEnd);
}
public void calloutIncompatibleReturnType(MethodSpec roleMethodSpec, MethodSpec baseFeature) {
	assert (!(baseFeature instanceof FieldAccessSpec));

	MethodBinding baseMethod = baseFeature.resolvedMethod;
	String[] args = new String[] {
		new String(roleMethodSpec.resolvedMethod.readableName()),
		new String(baseMethod.readableName()),
		new String(roleMethodSpec.resolvedMethod.returnType.readableName()),
		new String(baseMethod.returnType.readableName())
	};
	int problem = IProblem.CalloutIncompatibleReturnType; // 3.3(d)
	if (   !baseFeature.hasSignature
		&& baseFeature.resolvedType() == TypeBinding.VOID)
		problem = IProblem.CalloutMissingReturnType;
	this.handle(
			problem,
			args,
			args,
			roleMethodSpec.declarationSourceStart,
			roleMethodSpec.declarationSourceEnd);
}

// -- 3.4 --
public void decapsulation(MessageSend send, Scope scope) {
	if (send.receiver instanceof SingleNameReference) {
		SingleNameReference receiver = (SingleNameReference)send.receiver;
		if (CharOperation.equals(receiver.token, IOTConstants.BASE)) {
			decapsulation(send.selector, send.binding.declaringClass, send, IProblem.DecapsulationMessageSend, scope);
			return;
		}
	}
	decapsulation(send.selector, send.binding.declaringClass, send, IProblem.Decapsulation, scope);
}
public void decapsulation(FieldAccessSpec spec, ReferenceBinding baseType, Scope scope) {
	decapsulation(spec.resolvedField.readableName(), baseType, spec, IProblem.DecapsulationField, scope);
}
public void decapsulation(QualifiedNameReference ref, FieldBinding field, Scope scope) {
	decapsulation(field.name, field.declaringClass, ref, IProblem.DecapsulationFieldReference, scope);
}
public void decapsulation(MethodSpec spec, ReferenceBinding baseType, Scope scope) {
	decapsulation(spec.resolvedMethod.readableName(), baseType, spec, IProblem.Decapsulation, scope);
}
void decapsulation(char[] baseFeatureName, ReferenceBinding baseType, ASTNode node, int problemID, Scope scope)
{
	int start= node.sourceStart;
	int end  = node.sourceEnd;
	String level = this.options.decapsulation;
	String[] args = null;
	if (level == CompilerOptions.REPORT_NONE)
		return;
	if (   (level == CompilerOptions.REPORT_CLASS)
		&& !scope.referenceCompilationUnit().isWarningSuppressedAt(
				IProblem.Decapsulation, start, end, null)) // handle suppressed warning below
	{
		this.referenceContext = scope.classScope().referenceContext;
		start = ((TypeDeclaration)this.referenceContext).sourceStart;
		end   = ((TypeDeclaration)this.referenceContext).sourceEnd;
		this.handle(
				IProblem.DecapsulationShort,
				NoArgument,
				NoArgument,
				start,
				end);
	} else { // REPORT_BINDING - or warning suppressed at the binding
		// need to report suppressed warning to avoid "Unnessary SuppressWarnings"
		args = new String[] {
				new String(baseFeatureName),
				new String(baseType.readableName())
		};
		this.handle(
				problemID,
				args,
				args,
				start,
				end);
	}
}
public void decapsulation(AllocationExpression alloc, Scope scope) {

	int start = alloc.sourceStart;
	int end   = alloc.statementEnd;
	String level = this.options.decapsulation;
	String[] args = null;
	if (level == CompilerOptions.REPORT_NONE)
		return;
	if (   (level == CompilerOptions.REPORT_CLASS)
		&& !scope.referenceCompilationUnit().isWarningSuppressedAt(
				IProblem.Decapsulation, start, end, null)) // handle suppressed warning below
	{
		this.referenceContext = scope.classScope().referenceContext;
		start = ((TypeDeclaration)this.referenceContext).sourceStart;
		end   = ((TypeDeclaration)this.referenceContext).sourceEnd;
		this.handle(
				IProblem.DecapsulationShort,
				NoArgument,
				NoArgument,
				start,
				end);
	} else { // REPORT_BINDING - or warning suppressed at the binding
		// need to report suppressed warning to avoid "Unnessary SuppressWarnings"
		args = new String[] {
				new String(alloc.binding.readableName())
		};
		this.handle(
				IProblem.DecapsulationBaseCtor,
				args,
				args,
				start,
				end);
	}
}
/** Ensures that each baseclass decapsulation is reported only once. */
public void decapsulation(Expression type) {
	decapsulation(type, type.resolvedType);
}
public void decapsulation(Expression type, TypeBinding resolvedType) {
	if (type.getBaseclassDecapsulation() == DecapsulationState.REPORTED)
		return;
	String[] args = new String[] {
			resolvedType != null ?
					new String(resolvedType.readableName()) :
					type.toString()
	};
	type.tagReportedBaseclassDecapsulation();
	this.handle(
			IProblem.BaseclassDecapsulation,
			args,
			args,
			type.sourceStart,
			type.sourceEnd);

}
public void decapsulationOfFinal(Expression type, TypeBinding resolvedType) {
	if (type.getBaseclassDecapsulation() == DecapsulationState.REPORTED)
		return;
	String[] args = new String[] {
			resolvedType != null ?
					new String(resolvedType.readableName()) :
					type.toString()
	};
	type.tagReportedBaseclassDecapsulation();
	this.handle(
			IProblem.BaseclassDecapsulationFinal,
			args,
			args,
			type.sourceStart,
			type.sourceEnd);

}
public void decapsulation(ImportReference type) {
	String[] args = { new String(CharOperation.concatWith(type.tokens, '.')) };
	this.handle(
			IProblem.BaseclassDecapsulation,
			args,
			args,
			type.sourceStart,
			type.sourceEnd);

}
public void decapsulationByForcedExport(ReferenceBinding type, ASTNode reference) {
	String[] args = { new String(type.readableName()) };
	this.handle(
			IProblem.BaseclassDecapsulationForcedExport,
			args,
			args,
			reference.sourceStart,
			reference.sourceEnd);
}
public void illegalUseOfForcedExport(ReferenceBinding type, ASTNode reference) {
	String[] args = { new String(type.readableName()) };
	this.handle(
			IProblem.IllegalUseOfForcedExport,
			args,
			args,
			reference.sourceStart,
			reference.sourceEnd);
}

public void decapsulatingConfined(Expression typeRef, ReferenceBinding typeBinding) {
	char[] name =  typeBinding.readableName();
	String[] args = new String[] { new String(name) };
	this.handle(
			IProblem.ConfinedDecapsulation,
			args,
			args,
			typeRef.sourceStart,
			typeRef.sourceEnd);

}
public void mappingToInvisiblePrivate(MethodSpec spec, ReferenceBinding site, boolean isCallin)
{
	String[] args = new String[] {
		new String(spec.readableName()),
		new String(spec.getDeclaringClass().readableName()),
		new String(site.readableName())
	};
	this.handle(
			isCallin?IProblem.CallinInvisiblePrivate:IProblem.CalloutInvisiblePrivate,
			args,
			args,
			spec.sourceStart,
			spec.sourceEnd);

}
// -- 3.5 (callout to field) --
public void differentTypeInFieldSpec(FieldAccessSpec spec) {
	String[] args = new String[] {
			new String(spec.resolvedField.name),
			new String(spec.resolvedField.type.readableName()),
			new String(spec.declaredType().resolvedType.readableName())
	};
	this.handle(
			IProblem.DifferentTypeInFieldSpec,
			args,
			args,
			spec.returnType.sourceStart,
			spec.returnType.sourceEnd);
}
public void fieldAccessHasNoEffect(MethodSpec roleMethodSpec, FieldAccessSpec fieldSpec) {
	String[] args = new String[] {
		new String(roleMethodSpec.resolvedMethod.shortReadableName()),
		new String(fieldSpec.resolvedField.name)
	};
	this.handle(
			IProblem.FieldAccessHasNoEffect,
			args,
			args,
			ProblemSeverities.Warning,
			roleMethodSpec.declarationSourceStart,
			fieldSpec.declarationSourceEnd);
}
public void calloutSetCantReturn(MethodSpec roleMethodSpec) {
	String[] args = new String[] {
			new String(roleMethodSpec.resolvedMethod.shortReadableName())
	};
	this.handle(
			IProblem.CalloutSetCantReturn,
			args,
			args,
			roleMethodSpec.declarationSourceStart,
			roleMethodSpec.declarationSourceEnd);
}
public void calloutToFieldMissingParameter (
		MethodSpec roleMethodSpec,
		FieldAccessSpec baseFieldSpec)
{
	FieldBinding baseField = baseFieldSpec.resolvedField;
	String[] args = new String[] {
		new String(roleMethodSpec.resolvedMethod.readableName()),
		new String(baseField.readableName()),
		new String(baseField.type.readableName())
	};
	this.handle(
			IProblem.CalloutToFieldMissingParameter,
			args,
			args,
			roleMethodSpec.declarationSourceStart,
			roleMethodSpec.declarationSourceEnd);
}
public void calloutIncompatibleFieldType(
		MethodSpec roleMethodSpec,
		FieldAccessSpec baseFieldSpec,
		TypeBinding requiredType,
		TypeBinding providedType)
{
	FieldBinding baseField = baseFieldSpec.resolvedField;
	String[] args = new String[] {
		new String(roleMethodSpec.resolvedMethod.readableName()),
		new String(baseField.readableName()),
		new String(requiredType.readableName()),
		new String(providedType.readableName())
	};
	int problem = baseFieldSpec.calloutModifier == TerminalTokens.TokenNameget ?
			IProblem.CalloutGetIncompatibleFieldType :
			IProblem.CalloutSetIncompatibleFieldType;
	this.handle(
			problem,
			args,
			args,
			roleMethodSpec.declarationSourceStart,
			roleMethodSpec.declarationSourceEnd);
}
public void inferredUseOfCalloutToField(boolean isSetter, Expression location, char[] token, FieldBinding baseField) {
	String[] args = { new String(token), baseField.toString() };
	this.handle(
			isSetter ?
				IProblem.UsingCalloutToFieldForAssignment :
				IProblem.UsingCalloutToFieldForFieldRead,
			args,
			args,
			location.sourceStart,
			location.sourceEnd);
}
// ====== CALLIN ======
// -- 4.1 --
public void callinDespiteBindingAmbiguity(
		ReferenceBinding binding,
		AbstractMethodMappingDeclaration methodMapping)
{
	String[] args = new String[] { new String(binding.readableName()) };
	this.handle(
			IProblem.CallinDespiteBindingAmbiguity,
			args,
			args,
			methodMapping.sourceStart,
			methodMapping.sourceEnd);
}
public void duplicateCallinName(CallinMappingDeclaration callinMapping) {
	String[] args = { String.valueOf(callinMapping.name) };
	this.handle(
			IProblem.DuplicateCallinName,
			args,
			args,
			callinMapping.sourceStart,
			callinMapping.sourceStart+callinMapping.name.length-1);
}
public void bindingToInheritedFinal(MethodSpec spec, MethodBinding method, ReferenceBinding baseClass) {
	String[] args = {
		new String(method.readableName()),
		new String(method.declaringClass.readableName()),
		new String(baseClass.readableName())
	};
	this.handle(
			IProblem.CallinToInheritedFinal,
			args,
			args,
			spec.sourceStart,
			spec.sourceEnd);
}
public void covariantReturnRequiresTypeParameter(TypeReference returnType) {
	String[] args = { returnType.toString() };
	this.handle(
			IProblem.CovariantReturnRequiresTypeParameter,
			args,
			args,
			returnType.sourceStart,
			returnType.sourceEnd);
}
// special callin diagnostic:
public void callinToDeprecated(MethodSpec spec, MethodBinding binding) {
	String[] args = { new String(binding.readableName()) };
	this.handle(IProblem.CallinToDeprecated, args, args, spec.sourceStart, spec.sourceEnd);
}
// -- 4.2 --
public void callinInNonRole(ReferenceBinding declaringClass, AbstractMethodDeclaration method) {
	String[] args = new String[] {
			new String(declaringClass.readableName())
	};
	this.handle(
			IProblem.CallinInNonRole,
			args,
			args,
			method.declarationSourceStart,
			method.sourceEnd);
}
public void callinWithVisibility(AbstractMethodDeclaration declaration, int modifiers)
{
	String[] args = new String[] {
			Protections.toString(modifiers),
			new String(declaration.binding.readableName())
		};
	this.handle(
			IProblem.CallinWithVisibility,
			args,
			args,
			declaration.sourceStart,
			declaration.sourceEnd);
}

public void callToCallin(MethodBinding method, MessageSend send) {
	String[] args = new String[] {
			new String(method.readableName())
	};
	this.handle(
			IProblem.CallToCallin,
			args,
			args,
			send.sourceStart,
			send.sourceEnd);
}
public void calloutBindingCallin(MethodSpec roleMethodSpec)
{
	String[] args = new String[] {
			new String(roleMethodSpec.readableName())
	};
	this.handle(
			IProblem.CalloutBindingCallin,
			args,
			args,
			roleMethodSpec.declarationSourceStart,
			roleMethodSpec.sourceEnd);
}
public void replaceMappingToNonCallin(MethodSpec methodSpec, MethodBinding resolvedMethod)
{
	String[] args = new String[] { new String(resolvedMethod.readableName()) };
	this.handle(
			IProblem.ReplaceMappingToNonCallin,
			args,
			args,
			methodSpec.declarationSourceStart,
			methodSpec.declarationSourceEnd);
}
public void callinMethodBoundNonReplace(MethodSpec methodSpec, CallinMappingDeclaration mappingDecl)
{
	String[] args = new String[] {
			new String(methodSpec.readableName()),
			mappingDecl.callinModifier()
	};
	this.handle(
			IProblem.CallinMethodBoundNonReplace,
			args,
			args,
			methodSpec.declarationSourceStart,
			methodSpec.declarationSourceEnd);
}
public void callinOverridesRegular(MethodBinding inheritedMethod, AbstractMethodDeclaration method)
{
	if (method.ignoreFurtherInvestigation)
		return; // maybe already reported?
	if (method.binding.copyInheritanceSrc != null) {
		conflictingCallinAndRegular(inheritedMethod, method.binding.copyInheritanceSrc);
		return;
	}
	this.referenceContext = method; // blame this method
	String[] args = new String[] {
			new String(inheritedMethod.declaringClass.readableName())
	};
	this.handle(
			IProblem.CallinOverridesRegular,
			args,
			args,
			method.declarationSourceStart,
			method.sourceEnd);
}
public void regularOverridesCallin(MethodBinding inheritedMethod, AbstractMethodDeclaration method)
{
	if (method.ignoreFurtherInvestigation)
		return; // maybe already reported?
	if (method.binding.copyInheritanceSrc != null) {
		conflictingCallinAndRegular(inheritedMethod, method.binding.copyInheritanceSrc);
		return;
	}
	this.referenceContext = method; // blame this method
	String[] args = new String[] {
			new String(inheritedMethod.declaringClass.readableName())
	};
	this.handle(
			IProblem.RegularOverridesCallin,
			args,
			args,
			method.declarationSourceStart,
			method.sourceEnd);
}
public void conflictingCallinAndRegular(MethodBinding inheritedMethod, MethodBinding method)
{
	String[] args = new String[] {
			new String(inheritedMethod.readableName()),
			new String(inheritedMethod.declaringClass.readableName()),
			new String(method.declaringClass.readableName())
	};
	int start = 0, end = 0;
	if (this.referenceContext instanceof TypeDeclaration) {
		TypeDeclaration typeDeclaration = (TypeDeclaration) this.referenceContext;
		start = typeDeclaration.sourceStart;
		end = typeDeclaration.sourceEnd;
		if (   typeDeclaration.getRoleModel() != null
			&& typeDeclaration.getRoleModel().getInterfacePartBinding() == inheritedMethod.declaringClass)
			return; // don't report against our own interface part
	}
	this.handle(
			IProblem.ConflictingCallinAndRegular,
			args,
			args,
			start,
			end);
}
// -- 4.3 (base calls) --
public void baseCallNotSameMethod(AbstractMethodDeclaration enclosingMethod, MessageSend send)
{
	char[] methodName = (enclosingMethod.binding != null) ?
			enclosingMethod.binding.readableName() :
			enclosingMethod.selector ;
	String[] args = new String[] {
			new String(methodName)
	};
	this.handle(
			IProblem.BaseCallNotSameMethod,
			args,
			args,
			send.sourceStart,
			send.sourceEnd);
}
public void baseCallDoesntMatchRoleMethodSignature(ASTNode baseCallMsgSend) {
	this.handle(
		IProblem.BaseCallDoesntMatchRoleMethodSignature,
		NoArgument,
		new String[] {baseCallMsgSend.toString()},
		baseCallMsgSend.sourceStart,
		baseCallMsgSend.sourceEnd);
}
public void potentiallyMissingBasecall (MethodDeclaration method) {
	this.handle(
			IProblem.PotentiallyMissingBaseCall,
			NoArgument,
			NoArgument,
			method.sourceStart,
			method.sourceEnd);
}
public void definitelyMissingBasecall (MethodDeclaration method) {
	this.handle(
			IProblem.DefinitelyMissingBaseCall,
			NoArgument,
			NoArgument,
			method.sourceStart,
			method.sourceEnd);
}
public void potentiallyDuplicateBasecall (Expression call) {
	this.handle(
			IProblem.PotentiallyDuplicateBaseCall,
			NoArgument,
			NoArgument,
			call.sourceStart,
			call.sourceEnd);
}
public void definitelyDuplicateBasecall (Expression call) {
	this.handle(
			IProblem.DefinitelyDuplicateBaseCall,
			NoArgument,
			NoArgument,
			call.sourceStart,
			call.sourceEnd);
}
public void callinMappingMissingResult(
		CallinMappingDeclaration mapping,
		MethodSpec baseMethodSpec)
{
	TypeBinding baseReturnType = baseMethodSpec.resolvedMethod.returnType;
	String[] args = new String[] {
			new String(baseReturnType.readableName())
	};
	// TODO (SH): shouldn't the positions point to the role method instead of the callin?
	this.handle(
			IProblem.MissingBaseCallResult,
			args,
			args,
			mapping.sourceStart,
			mapping.sourceEnd);
}
public void fragileCallinMapping(
		CallinMappingDeclaration mapping,
		MethodSpec baseMethodSpec)
{
	TypeBinding baseReturnType = baseMethodSpec.resolvedMethod.returnType;
	String[] args = new String[] {
			new String(baseReturnType.readableName())
	};
	boolean isBaseType = baseReturnType.isBaseType();
	// TODO (SH): shouldn't the positions point to the role method instead of the callin?
	this.handle(
			isBaseType ?
					IProblem.FragileCallinBindingBaseType :
					IProblem.FragileCallinBindingReferenceType,
			args,
			args,
			mapping.sourceStart,
			mapping.sourceEnd);
}
public void baseSuperCallToNonOverriding(MethodSpec baseMethodSpec, MethodSpec roleMethodSpec) {
	String[] args = {
		new String(baseMethodSpec.readableName()),
		new String(roleMethodSpec.readableName())
	};
	this.handle(
			IProblem.BaseSuperCallToNonOverriding,
			args, args,
			baseMethodSpec.sourceStart,
			baseMethodSpec.sourceStart);

}
public void baseSuperCallDecapsulation(BaseCallMessageSend send) {
	this.handle(
			IProblem.BaseSuperCallDecapsulation,
			NoArgument,
			NoArgument,
			send.sourceStart,
			send.sourceEnd);
}
// -- 4.4 --
public void callinParameterMappingMissingSignatures(CallinMappingDeclaration callinBinding) {
	this.handle(
			IProblem.CallinParameterMappingMissingSingatures,
			NoArgument,
			NoArgument,
			callinBinding.bodyStart,
			callinBinding.bodyEnd);
}

public void ignoringRoleMethodReturn(MethodSpec roleMethodSpec) {
	int start, end;
	if (roleMethodSpec.hasSignature) {
		start = roleMethodSpec.returnType.sourceStart;
		end =   roleMethodSpec.returnType.sourceEnd;
	} else {
		start = roleMethodSpec.sourceStart;
		end   = roleMethodSpec.sourceEnd;
	}
	String[] args = new String[] {
		new String(roleMethodSpec.resolvedType().readableName())
	};
	this.handle(
			IProblem.IgnoringRoleMethodReturn,
			args,
			args,
			start, 
			end);
}

public void nonResultExpressionInReplaceResult(Expression resultMapper) {
	this.handle(
			IProblem.NonReplaceExpressionInReplaceResult,
			NoArgument,
			NoArgument,
			resultMapper.sourceStart,
			resultMapper.sourceEnd);
}
public void callinIllegalRoleReturnReturn(MethodSpec baseMethodSpec, MethodSpec roleMethodSpec) {
	MethodBinding roleMethod = roleMethodSpec.resolvedMethod;
	MethodBinding baseMethod = baseMethodSpec.resolvedMethod;
	String[] args = new String[] {
		new String(roleMethodSpec.readableName()),
		new String(roleMethod.returnType.shortReadableName()),
		new String(roleMethod.returnType.readableName()),
		new String(baseMethod.readableName()),
		new String(baseMethod.returnType.shortReadableName()),
		new String(baseMethod.returnType.readableName()),
	};
	this.handle(
			IProblem.CallinIllegalRoleReturn,
			args,
			args,
			roleMethodSpec.declarationSourceStart,
			roleMethodSpec.declarationSourceEnd);
}
public void callinIncompatibleReturnType(MethodSpec baseMethodSpec, MethodSpec roleMethodSpec) {
	String[] args = new String[] {
		new String(baseMethodSpec.resolvedMethod.readableName()),
		new String(roleMethodSpec.readableName()),
		new String(baseMethodSpec.resolvedMethod.returnType.readableName()),
		new String(MethodModel.getReturnType(roleMethodSpec.resolvedMethod).readableName())
	};
	this.handle(
			IProblem.CallinIncompatibleReturnType,
			args,
			args,
			baseMethodSpec.declarationSourceStart,
			baseMethodSpec.declarationSourceEnd);
}
public void callinIncompatibleReturnTypeBaseCall(MethodSpec baseMethodSpec, MethodSpec roleMethodSpec) {
	TypeBinding roleReturn = MethodModel.getReturnType(roleMethodSpec.resolvedMethod);
	String[] args = new String[] {
		new String(roleMethodSpec.readableName()),
		new String(roleReturn.shortReadableName()),
		new String(roleReturn.readableName()),
		new String(baseMethodSpec.resolvedMethod.readableName()),
		new String(baseMethodSpec.resolvedMethod.returnType.shortReadableName()),
		new String(baseMethodSpec.resolvedMethod.returnType.readableName()),
	};
	this.handle(
			IProblem.CallinIncompatibleReturnTypeBaseCall,
			args,
			args,
			baseMethodSpec.declarationSourceStart,
			baseMethodSpec.declarationSourceEnd);
}
public void baseArgInNonSimpleExpression(SingleNameReference nameRef) {
	this.handle(
			IProblem.BaseArgInNonSimpleExpression,
			NoArgument,
			NoArgument,
			nameRef.sourceStart,
			nameRef.sourceEnd);
}
public void illegalBindingDirectionNonReplaceCallin(ParameterMapping mapping) {
	this.handle(IProblem.IllegalBindingDirectionNonReplaceCallin, 
				NoArgument, NoArgument, 
				mapping.sourceStart, 
				mapping.sourceEnd);
}
// -- 4.5 --
public void typesNotTwowayCompatibleInReplace(
		TypeBinding providedType,
		TypeBinding expectedType,
		ASTNode     location,
		int         argNum)
{
	String[] args = new String[] {
			new String(providedType.readableName()),
			new String(expectedType.readableName()),
			String.valueOf(argNum+1)
	};
	this.handle(
			IProblem.TypesNotTwowayCompatibleInReplace,
			args,
			args,
			location.sourceStart,
			location.sourceEnd);
}

public void duplicateUseOfTypeVariableInCallin(Expression type, TypeBinding typeVar) {
	String[] args = new String[] {
		new String(typeVar.readableName())
	};
	this.handle(IProblem.DuplicateUseOfTypeVariableInCallin,
			args,
			args,
			type.sourceStart,
			type.sourceEnd);
}
public void illegalMappingRHSTypeParameter(TypeParameter param) {
	this.handle(IProblem.IllegalMappingRHSTypeParameter,
			NoArgument, NoArgument,
			param.sourceStart,
			param.sourceEnd);
}
// -- 4.6 --
public void callinDecapsulation(MethodSpec baseSpec, Scope scope) {
	int start = baseSpec.sourceStart;
	int end   = baseSpec.sourceEnd;
	String level = this.options.decapsulation;
	String[] args = null;
	if (level == CompilerOptions.REPORT_NONE)
		return;
	if (level == CompilerOptions.REPORT_CLASS) {
		this.referenceContext = scope.classScope().referenceContext;
		start = ((TypeDeclaration)this.referenceContext).sourceStart;
		end   = ((TypeDeclaration)this.referenceContext).sourceEnd;
		this.handle(
				IProblem.DecapsulationShort,
				NoArgument,
				NoArgument,
				start,
				end);
	} else { // REPORT_BINDING
		args = new String[] {
				new String(baseSpec.resolvedMethod.readableName()),
				new String(baseSpec.resolvedMethod.declaringClass.readableName())
		};
		this.handle(
				IProblem.CallinDecapsulation,
				args,
				args,
				start,
				end);
	}
}

// -- 4.7 (static) --
public void callinIncompatibleStatic(CallinMappingDeclaration decl, MethodSpec baseMethod) {
	String[] args = new String[] {
			new String(decl.roleMethodSpec.readableName()),
			new String(baseMethod.resolvedMethod.readableName())
	};
	this.handle(
			IProblem.CallinIncompatibleStatic,
			args,
			args,
			baseMethod.declarationSourceStart,
			baseMethod.declarationSourceEnd);
}
public void replaceCallinIncompatibleStatic(CallinMappingDeclaration decl, MethodSpec baseMethod) {
	String[] args = new String[] {
			new String(decl.roleMethodSpec.readableName()),
			new String(baseMethod.resolvedMethod.readableName())
	};
	this.handle(
			IProblem.ReplaceCallinIncompatibleStatic,
			args,
			args,
			baseMethod.declarationSourceStart,
			baseMethod.declarationSourceEnd);
}
// -- 4.8 (precedence) --
public void unknownPrecedence(TypeDeclaration typeDecl, CallinCalloutBinding callin1, CallinCalloutBinding callin2)
{
	char[] class1, name1, class2, name2;
	CallinCalloutBinding m1, m2; // for position of reporting:
	if (new CharArrayComparator().compare(callin1.name, callin2.name)<0) {
		class1 = callin1._declaringRoleClass.readableName();
		class2 = callin2._declaringRoleClass.readableName();
		name1 = callin1.name;
		name2 = callin2.name;
		m1= callin1;
		m2= callin2;
	} else {
		class1 = callin2._declaringRoleClass.readableName();
		class2 = callin1._declaringRoleClass.readableName();
		name1 = callin2.name;
		name2 = callin1.name;
		m1= callin2;
		m2= callin1;
	}
	String[] args = new String[] {
			new String(class1), new String(name1),
			new String(class2), new String(name2),
			CallinMappingDeclaration.callinModifier(callin1.callinModifier)
	};
	int start = -1;
	int end   = -1;
	AbstractMethodMappingDeclaration matchingMapping;
	if ((matchingMapping = findMappingDeclaration(m1)) != null) {
		start = matchingMapping.sourceStart;
		end = matchingMapping.sourceEnd;
	} else
	if ((matchingMapping = findMappingDeclaration(m2)) != null) {
		start = matchingMapping.sourceStart;
		end = matchingMapping.sourceEnd;
	} else { // still nothing found??
		start = typeDecl.sourceStart;
		end = typeDecl.sourceEnd;
	}
	this.handle(
			IProblem.UnknownPrecedence,
			args,
			args,
			start,
			end);
}
// helper for the above:
private AbstractMethodMappingDeclaration findMappingDeclaration(CallinCalloutBinding callin)
{
	if (callin._declaringRoleClass.isBinaryBinding())
		return null;
	SourceTypeBinding declaringRole = (SourceTypeBinding)callin._declaringRoleClass;
	if (declaringRole != null && declaringRole.scope != null) {
		TypeDeclaration roleDecl = declaringRole.scope.referenceContext;
		if (roleDecl.callinCallouts != null)
			for (AbstractMethodMappingDeclaration mapping : roleDecl.callinCallouts)
				if (mapping.binding == callin)
					return mapping;
	}
	return null;
}
public void mismatchingAfterInPrecedence(NameReference name, boolean precedenceIsAfter) {
	if (precedenceIsAfter) {
		CallinCalloutBinding binding = (CallinCalloutBinding) name.binding;
		String[] args = new String[] { CallinMappingDeclaration.callinModifier(binding.callinModifier) };
		this.handle(
				IProblem.NonAfterCallinInAfterPrecedence, 
				args,
				args,
				name.sourceStart,
				name.sourceEnd);
	} else {
		this.handle(
				IProblem.AfterCallinInNonAfterPrecedence, 
				NoArgument,
				NoArgument,
				name.sourceStart,
				name.sourceEnd);
	}
}
public void precedenceInRegularClass(TypeDeclaration type, PrecedenceDeclaration[] precedences) {
	String[] args = new String[] {
		new String(type.binding.readableName())
	};
	this.handle(
			IProblem.PrecedenceInRegularClass,
			args,
			args,
			precedences[0].sourceStart,
			precedences[0].sourceEnd);
}
public void callinBindingNotFound(NameReference location, char[] name, ReferenceBinding type) {
	String[] args = new String[] {
		new String(name),
		new String(type.readableName())
	};
	this.handle(
			IProblem.CallinBindingNotFound,
			args,
			args,
			location.sourceStart,
			location.sourceEnd);
}
public void illegalEnclosingForCallinName(PrecedenceDeclaration precDecl,
		  ReferenceBinding type,
		  char[] name)
{
	String[] args = new String[] {
			new String(type.readableName()),
			new String(name)
	};
	this.handle(
			IProblem.IllegalEnclosingForCallinName,
			args,
			args,
			precDecl.sourceStart,
			precDecl.sourceEnd);
}
public void incompatiblePrecedenceLists(ASTNode location, TypeDeclaration declaration, PrecedenceBinding prec1, PrecedenceBinding prec2) {
	String[] args;
	int problemId;
	if (location instanceof PrecedenceDeclaration) {
		problemId = IProblem.IncompatiblePrecedenceListsOther;
		args = new String[] {
				new String(declaration.name),
				(((PrecedenceDeclaration)location).binding == prec1
						? prec2.toString()
								: prec1.toString())
		};
	} else { // prec decl not found, report both bindings in the details
		problemId = IProblem.IncompatiblePrecedenceListsSymmetric;
		args = new String[] {
				new String(declaration.name),
				prec1.toString(),
				prec2.toString()
		};
	}
	this.handle(
			problemId,
			args,
			args,
			location.sourceStart,
			location.sourceEnd);
}
public void precedenceForOverriding(NameReference ref_i, NameReference ref_j) {
	String[] args = new String[] {
			ref_i.toString(), ref_j.toString()
	};
	this.handle(
			IProblem.PrecedenceForOverriding,
			args,
			args,
			ref_j.sourceStart,
			ref_j.sourceEnd);
}
// ===== ACTIVATION/PREDICATES ======
public void withinStatementNeedsTeamInstance(Expression condition) {
	String[] arguments = new String[] {new String("\"within("+condition.toString()+")\"")}; //$NON-NLS-1$ //$NON-NLS-2$
		this.handle(
			IProblem.WithinStatementNeedsTeamInstance,
			arguments,
			arguments,
			condition.sourceStart,
			condition.sourceEnd);
}
public void checkedExceptionInGuard(TypeBinding exceptionType, ASTNode expression) {
	String[] args = { String.valueOf(exceptionType.readableName()) };
	this.handle(IProblem.CheckedExceptionInGuard,
			args, args,
			expression.sourceStart,
			expression.sourceEnd);
}
public void basePredicateInUnboundRole(AbstractMethodDeclaration predDecl, ReferenceBinding roleType)
{
	String[] arguments = new String[] {new String(roleType.readableName())};
	this.handle(
			IProblem.BasePredicateInUnboundRole,
			arguments,
			arguments,
			predDecl.declarationSourceStart, // include keyword "base".
			predDecl.sourceEnd);
}
public void predicateHasNoArguments(String[] arguments, int start, int end)
{
	this.handle(IProblem.PredicateHasNoArguments,
				arguments,
				arguments,
				start, end);
}
// ====== API ======
public void roleClassLiteralLacksTeamInstance(ClassLiteralAccess access, ReferenceBinding targetRef) {
	String[] args = new String[] {
			new String(targetRef.readableName())
		};
	this.handle(
			IProblem.RoleClassLiteralLacksTeamInstance,
			args,
			args,
			access.sourceStart,
			access.sourceEnd);
}
public void externalizedRoleClassLiteral(ClassLiteralAccess access, ReferenceBinding targetRef) {
	String[] args = new String[] {
		new String(targetRef.readableName())
	};
	this.handle(
			IProblem.ExternalizedRoleClassLiteral,
			args,
			args,
			access.sourceStart,
			access.sourceEnd);
}
public void overridingPredefined(AbstractMethodDeclaration foundMethod) {
	String[] args = new String[] {
			foundMethod.binding != null ?
					new String(foundMethod.binding.readableName()) :
					new String(foundMethod.selector)
	};
	this.handle(
			IProblem.OverridingPredefined,
			args,
			args,
			foundMethod.sourceStart,
			foundMethod.sourceEnd);
}
// ====== ENCAPSULATION ======
public void overridingConfined(TypeDeclaration roleDecl, String roleName) {
	String[] args = new String[] { roleName };
	this.handle(
			IProblem.OverridingConfined,
			args,
			args,
			ProblemSeverities.Error,
			roleDecl.sourceStart,
			roleDecl.sourceEnd);
}
// ====== DEPENDENT TYPES
public void typeAnchorReferenceNotAValue(Reference anchorExpr)
{
    this.handle(
        IProblem.AnchorReferenceNotAValue,
        NoArgument,
        NoArgument,
        anchorExpr.sourceStart,
        anchorExpr.sourceEnd);
}
public void typeAnchorReferenceNotAnObjectRef(int start, int end) {
    this.handle(
            IProblem.AnchorReferenceNotAnObjectRef,
            NoArgument,
            NoArgument,
            start, end);
}
public void typeAnchorNotFound(char[] token, int start, int end) {
	String[] args = new String[] { new String(token) };
    this.handle(
            IProblem.AnchorNotFound,
            args,
            args,
            start, end);
}
public void incompatibleValueParameter(TypeReference reference, VariableBinding currentParam) {
	String[] args = new String[] { new String(currentParam.type.shortReadableName()), new String(currentParam.name) };
	this.handle(
			IProblem.IncompatibleValueParameter,
			args,
			args,
			reference.sourceStart,
			reference.sourceEnd);
}
public void typeHasNoValueParamAt(TypeReference reference, ReferenceBinding refBinding, int typeParamPos) {
	String[] args = new String[] { new String(refBinding.readableName()), Integer.toString(typeParamPos+1) };
	this.handle(
			IProblem.TypeHasNoValueParamAt,
			args,
			args,
			reference.sourceStart,
			reference.sourceEnd);
}
public void rebindingTypeVariableAnchor(SingleTypeReference typeRef, TypeAnchorReference anchorRef, ITeamAnchor declaredAnchor) {
	String[] args = new String[] { new String(typeRef.token), new String(declaredAnchor.getBestName()) };
	this.handle(
			IProblem.RebindingTypeVariableAnchor,
			args,
			args,
			anchorRef.sourceStart,
			anchorRef.sourceEnd);
}

// ====== SYNTAX ======
public void otKeywordInRegularClass(int start, int end) {
	this.handle(
			IProblem.OTKeywordInRegularClass,
			NoArgument,
			NoArgument,
			start,
			end);
}
public void inheritedNameIsOTKeyword(MethodBinding m, int start, int end) {
	String[] args = new String[] {
			new String(m.selector),
			new String(m.declaringClass.readableName())
	};
	this.handle(
			IProblem.InheritedNameIsOTKeyword,
			args,
			args,
			start,
			end);

}
public void illegalOTIdentifier(char[] name, int start, int end) {
	String[] args = new String[] { new String(name) };
	this.handle(
			IProblem.IllegalOTIdentifier,
			args,
			args,
			start,
			end);
}

public void playedByInRegularClass(SourceTypeBinding type, TypeReference reference) {
    this.handle(
        IProblem.PlayedByInRegularClass,
        new String[] {new String(type.sourceName())},
        new String[] {new String(type.sourceName())},
        reference.sourceStart,
        reference.sourceEnd);
}

public void methodMappingNotInClass(AbstractMethodMappingDeclaration mapping) {
	int start = mapping.declarationSourceStart;
	int end = mapping.declarationSourceEnd;
	this.handle(
			IProblem.MethodMappingNotInClass,
			NoArgument, NoArgument,
			start,
			end);

}

public void illegalModifierInMethodMapping(
		AbstractMethodMappingDeclaration methodMapping, int modifiersStart, int modifiersEnd)
{
    this.referenceContext = methodMapping;
    int problemID = (methodMapping instanceof CallinMappingDeclaration) ?
    					IProblem.IllegalModifierInCallinBinding :
    					IProblem.IllegalModifierInCalloutShort;
    this.handle(
        problemID,
        NoArgument,
        NoArgument,
        modifiersStart,
        modifiersEnd);
}

public void wrongModifierInCalloutMapping(
		AbstractMethodMappingDeclaration methodMapping, int modifiersStart, int modifiersEnd)
{
    this.referenceContext = methodMapping;
    this.handle(
        IProblem.WrongModifierInCalloutBinding,
        NoArgument,
        NoArgument,
        modifiersStart,
        modifiersEnd);
}

public void syntaxErrorCtorMethodSpec(AbstractMethodDeclaration method) {
	this.handle(IProblem.SyntaxErrorCtorMethodSpec, NoArgument, NoArgument, method.sourceStart, method.sourceEnd);
}

public void syntaxErrorMethodSpecMissingReturnType(AbstractMethodDeclaration method) {
	this.handle(IProblem.SyntaxErrorMethodSpecMissingReturnType, NoArgument, NoArgument, method.sourceStart, method.sourceEnd);
}

public void illegalModifierInMethodSpecRight(
		AbstractMethodMappingDeclaration mapping, int modifiersStart, int modifiersEnd)
{
    this.referenceContext = mapping;
    int problemID = IProblem.IllegalModifierInMethodSpecRight;
    this.handle(
        problemID,
        NoArgument,
        NoArgument,
        modifiersStart,
        modifiersEnd);
}
public void syntaxErrorInCallinLabel(ReferenceContext rc, int start, TypeReference ref)
{
	this.referenceContext = rc;
	this.handle(
			IProblem.SyntaxErrorInCallinLabel,
			NoArgument,
			NoArgument,
			start,
			ref.sourceEnd);
}
public void callinReplaceKeywordNotOptional(
			CallinMappingDeclaration context, CallinMappingDeclaration callinDecl)
{
	this.referenceContext = context;
	String[] argument = new String[] { callinDecl.roleMethodSpec.toString() };

	// probably has a broken source-range
	int sourceStart = callinDecl.sourceStart;
	int sourceEnd = callinDecl.sourceEnd;
	if (callinDecl.sourceStart > 0 && callinDecl.sourceEnd < callinDecl.sourceStart)
	{
	    // valid start, broken end -> mark the end of the broken binding
	    if (callinDecl.roleMethodSpec != null)
	    {
	        sourceStart = callinDecl.roleMethodSpec.sourceEnd + 1;
	        sourceEnd = sourceStart + 3;
	    }
	    else
	        sourceEnd = callinDecl.sourceStart + 3;
	}

	this.handle(
		IProblem.CallinReplaceKeyWordNotOptional,
		argument,
		argument,
		sourceStart,
		sourceEnd);
}

public void missingPredicateExpression(GuardPredicateDeclaration method) {
	this.referenceContext = method;
	this.handle(IProblem.MissingPredicateExpression,
			NoArgument, NoArgument,
			method.sourceStart, method.sourceEnd);

}

public void syntaxErrorIllegalDeclaredLifting(ReferenceContext referenceContext2, LiftingTypeReference ltr) {
	this.handle(
			IProblem.SyntaxErrorIllegalDeclaredLifting,
			NoArgument,
			NoArgument,
			ltr.sourceStart,
			ltr.sourceEnd);
}
public void valueParamWrongPosition(TypeAnchorReference reference) {
	String[] args = { reference.toString() };
	this.handle(
			IProblem.ValueParamWrongPosition,
			args,
			args,
			reference.sourceStart,
			reference.sourceEnd);
}

public void syntaxErrorInRoleClassLiteral(Expression typeExpr) {
	this.handle(IProblem.SyntaxErrorSingleTypeReferenceExpected, 
			NoArgument, NoArgument, typeExpr.sourceStart, typeExpr.sourceEnd);
	
}
// ==== LIMITATIONS: ====
public void unsupportedUseOfGenerics(ASTNode location) {
	this.handle(
			IProblem.UnsupportedUseOfGenerics,
			NoArgument,
			NoArgument,
			location.sourceStart,
			location.sourceEnd);
}
public void unresolvedLifting(PotentialLiftExpression expression, TypeBinding baseType, TypeBinding roleType)
{
	String[] args  = new String[] {
		new String(baseType.readableName()),
		new String(roleType.readableName())
	};
	this.handle(
			IProblem.UnresolvedLifting,
			args,
			args,
			expression.sourceStart,
			expression.sourceEnd);
}
public void unsupportedRoleDataflow(AbstractMethodDeclaration site, MethodBinding selfcall) {
	String[] args = new String[] {
			new String(selfcall.readableName()),
			new String(selfcall.declaringClass.readableName())
	};
	this.handle(
			IProblem.UnsupportedRoleDataflow,
			args,
			args,
			site.sourceStart,
			site.sourceEnd);
}
public void roleFileForBinaryTeam(char[] roleFileName, TypeDeclaration enclosingTeam) {
	String[] args = new String[] {
			new String(roleFileName),
			new String(enclosingTeam.name)
	};
	this.handle(
			IProblem.RoleFileForBinaryTeam,
			args,
			args,
			ProblemSeverities.Error|ProblemSeverities.AbortType | ProblemSeverities.Fatal,
			0,
			0);
}
public void missingRoleInBinaryTeam(char[] roleName, ReferenceBinding enclosingTeam) {
	String[] args = new String[] {
			new String(roleName),
			new String(enclosingTeam.readableName())
	};
	this.handle(
			IProblem.MissingRoleInBinaryTeam,
			args,
			args,
			ProblemSeverities.Error|ProblemSeverities.AbortType | ProblemSeverities.Fatal,
			0,
			0);
}
public void missingCopiedRole(TypeBinding tsuperRole, ReferenceBinding enclosingTeam) {
	String[] args = new String[] {
			(tsuperRole.isClass() ? "class ":"interface ")+new String(tsuperRole.readableName()), //$NON-NLS-1$ //$NON-NLS-2$
			new String(enclosingTeam.readableName())
	};
	this.handle(
			IProblem.MissingCopiedRole,
			args,
			args,
			ProblemSeverities.Error | ProblemSeverities.AbortType | ProblemSeverities.Fatal,
			0,
			0);
}
public void corruptBytecode(MethodBinding dstMethod) {
	String[] arguments = new String[] {new String(dstMethod.readableName())};
	this.handle(
			IProblem.CorruptBytecode,
			arguments,
			arguments,
			dstMethod.sourceEnd(),
			dstMethod.sourceEnd());

}
public void incompatibleBytecode(char[] name, int problemId) {
	String[] argument = new String[] { new String(name) };
	this.handle(
			IProblem.IncompatibleJRE,
			argument,
			argument,
			0, 0);

}
public void incompleteDependentTypesImplementation(ASTNode node, String msg) {
	String[] msgs = new String[] {msg};
	this.handle(
			IProblem.IncompleteDependentTypesImplementation,
			msgs, msgs,
			node.sourceStart,
			node.sourceEnd);

}
public void experimentalFeature(ASTNode loc, String string) {
	String[] args = new String[] {string};
	this.handle(
			IProblem.ExprimentalFeature,
			args, args,
			ProblemSeverities.Warning,
			loc.sourceStart,
			loc.sourceEnd
	);
}
public void inconsistentlyResolvedRole(TypeDeclaration ast, char[] ifcName, char[] className) {
	String[] args = { new String(ifcName), new String(className) };
	this.handle(
			IProblem.InconsistentlyResolvedRole,
			args, args,
			ast.sourceStart,
			ast.sourceEnd);
}

public void javadocMissingRoleTag(TypeDeclaration type, ReferenceBinding role) {
	String[] arguments = new String[] { String.valueOf(role.sourceName()) };
	this.handle(
		IProblem.JavadocMissingRoleTag,
		arguments,
		arguments,
		type.sourceStart,
		type.sourceEnd);
}
public void javadocRoleTagNotRoleFile(JavadocSingleTypeReference roleRef, ReferenceBinding role) {
	String[] arguments = new String[] { String.valueOf(role.sourceName()) };
	this.handle(
		IProblem.JavadocRoleTagNotRoleFile,
		arguments,
		arguments,
		roleRef.sourceStart,
		roleRef.sourceEnd);
}
public void javadocRoleTagNotRole(JavadocSingleTypeReference docRef) {
	String[] arguments = new String[] { String.valueOf(docRef.token) };
	this.handle(
		IProblem.JavadocRoleTagNotRole,
		arguments,
		arguments,
		docRef.sourceStart,
		docRef.sourceEnd);
}
public void javadocRoleTagInlineRole(JavadocSingleTypeReference docRef) {
	String[] arguments = new String[] { String.valueOf(docRef.token) };
	this.handle(
		IProblem.JavadocRoleTagInlineRole,
		arguments,
		arguments,
		docRef.sourceStart,
		docRef.sourceEnd);
}
public void javadocRoleTagInRegular(JavadocSingleTypeReference docRef, ReferenceBinding type) {
	String[] arguments = new String[] { String.valueOf(type.sourceName()) };
	this.handle(
		IProblem.JavadocRoleTagInRegular,
		arguments,
		arguments,
		docRef.sourceStart,
		docRef.sourceEnd);
}

public void roleFileCantBeEnum(TypeDeclaration typeDecl, char[][] tokens) {
	this.referenceContext = typeDecl;
	String[] args = new String[] {new String(typeDecl.name), new String(CharOperation.concatWith(tokens, '.'))};
	this.handle(
			IProblem.RoleFileCantBeEnum,
			args, args,
			typeDecl.modifiersSourceStart,
			typeDecl.sourceEnd);

}
public void baseclassCircularity(String circle, TypeDeclaration roleDecl) {
	int s, e;
	if (roleDecl.baseclass != null) {
		s = roleDecl.baseclass.sourceStart;
		e = roleDecl.baseclass.sourceEnd;
	} else {
		s = roleDecl.sourceStart;
		e = roleDecl.sourceEnd;
	}
	String[] args = new String[] { circle };
	this.handle(IProblem.BaseclassCircularity, args, args, s, e);
}

public void staleTSuperRole(ReferenceBinding roleType, ReferenceBinding tsuperRole, String methodDesignator) {
	String[] args = new String[] {
			new String(roleType.readableName()),
			new String(tsuperRole.readableName()),
			methodDesignator
	};
	this.handle(
			IProblem.StaleTSuperRole,
			args,
			args,
			ProblemSeverities.Error | ProblemSeverities.AbortType | ProblemSeverities.Fatal, // Abort will be caught later by the compiler adaptor.
			0, 0);
}

public void staleSubRole(ReferenceBinding roleType, ReferenceBinding subRole) {
	String[] args = new String[] {
			new String(roleType.readableName()),
			new String(subRole.readableName())
	};
	this.handle(
			IProblem.StaleSubRole,
			args,
			args,
			ProblemSeverities.Error | ProblemSeverities.AbortType | ProblemSeverities.Fatal, // Abort will be caught later by the compiler adaptor.
			0, 0);
}

public void missingAccessorInBinary(SourceTypeBinding binding, FieldBinding targetField) {
	String[] args = { new String(binding.readableName()), new String(targetField.readableName()) };
	int start = binding.scope.referenceContext.sourceStart;
	int end   = binding.scope.referenceContext.sourceEnd;
	this.handle(
			IProblem.MissingAccessorInBinary,
			args,
			args,
			ProblemSeverities.Error | ProblemSeverities.AbortType | ProblemSeverities.Fatal, // Abort will be caught later by the compiler adaptor.
			start, end);
}

public void roleFileInBinaryTeam(TypeDeclaration roleType, ReferenceBinding teamBinding) {
	String[] args = { new String(roleType.name), new String(teamBinding.readableName())};
	this.handle(
			IProblem.RoleFileInBinaryTeam,
			args,
			args,
			ProblemSeverities.Error | ProblemSeverities.AbortType | ProblemSeverities.Fatal,
			roleType.sourceStart,
			roleType.sourceEnd);

}

public void mismatchingRoleParts(ReferenceBinding binding, TypeDeclaration type) {
	String[] args = { String.valueOf(binding.readableName())};
	this.handle(
			IProblem.MismatchingRoleParts,
			args,
			args,
			ProblemSeverities.Error | ProblemSeverities.AbortType | ProblemSeverities.Fatal,
			type.sourceStart,
			type.sourceEnd);
}
public void compilationOrderProblem(String msg, ASTNode node) {
	String[] args = { msg };
	this.handle(
			IProblem.CompilationOrderProblem,
			args,
			args,
			node.sourceStart,
			node.sourceEnd);
}

public void notGeneratingCallinBinding(TypeDeclaration teamDecl, RoleModel role) {
	String[] args = { new String(role.getBinding().sourceName()) };
	this.handle(
			IProblem.NotGeneratingCallinBinding,
			args,
			args,
			// abort helps the incremental builder to finish with fewer cycles:
			ProblemSeverities.Error|ProblemSeverities.AbortType|ProblemSeverities.Fatal,
			teamDecl.sourceStart,
			teamDecl.sourceEnd);
}

public void searchingBaseclassTooEarly(MemberTypeBinding binding) {
	TypeDeclaration src = binding.scope.referenceContext;
	int start = 0, end = 0;
	if (src != null) {
		start = src.sourceStart;
		end = src.sourceEnd;
	}
	String[] args = { new String(binding.sourceName()) };
	this.handle(
			IProblem.SearchingBaseclassTooEarly,
			args,
			args,
			start,
			end);

}

public void staticRoleFieldMustBeFinal(FieldDeclaration fieldDecl) {
	String [] args = { new String(fieldDecl.name) };
	this.handle(
			IProblem.StaticRoleFieldMustBeFinal,
			args,
			args,
			fieldDecl.sourceStart,
			fieldDecl.sourceEnd);
}

public void illegallyCopiedDefaultCtor(AbstractMethodDeclaration newMethodDecl, TypeDeclaration roleDecl) {
	String[] args = { new String(roleDecl.binding.readableName()) };
	this.handle(
			IProblem.IllegallyCopiedDefaultCtor,
			args,
			args,
			newMethodDecl.sourceStart,
			newMethodDecl.sourceEnd);
}

public void readonlyNotYetSupported(TypeDeclaration type) {
	this.handle(
			IProblem.ReadonlyNotYetSupported,
			NoArgument,
			NoArgument,
			type.sourceStart,
			type.sourceEnd);
}
public void missingImplementation(ASTNode node, String detail) {
	String[] args = new String[] {detail};
	this.handle(IProblem.MissingImplementation, args, args, node.sourceStart, node.sourceEnd);
}
public void tryingToWeaveIntoSystemClass(TypeReference baseclass, ReferenceBinding baseclassBinding) {
	String[] args = { new String(baseclassBinding.readableName()) };
	this.handle(
			IProblem.TryingToWeaveIntoSystemClass,
			args,
			args,
			baseclass.sourceStart,
			baseclass.sourceEnd);

}

public void dangerousCallinBinding(MethodSpec methodSpec) {
	String[] args = { String.valueOf(methodSpec.resolvedMethod.readableName()) };
	this.handle(IProblem.DangerousCallinBinding,
				args,
				args,
				methodSpec.sourceStart,
				methodSpec.sourceEnd);

}

public void unexpectedAnnotationStructure(char[][] annotationName, char[] fieldName, int start, int end) {
	String[] args = { String.valueOf(CharOperation.concatWith(annotationName, '.')),
					  String.valueOf(fieldName) };
	this.handle(IProblem.UnexpectedAnnotationStructure, args, args, start, end);
}
public void incompatibleOTJByteCodeVersion(char[] className, String versionString) {
	String[] argument = new String[] { new String(className), versionString };
	this.handle(
			IProblem.IncompatibleOTJByteCodeVersion,
			argument,
			argument,
			0, 0);
}
public void callinBindingToInterface(AbstractMethodMappingDeclaration callinMapping, ReferenceBinding baseIfc) {
	String[] args = { String.valueOf(baseIfc.readableName()) };
	this.handle(IProblem.CallinBindingToInterface, args, args, callinMapping.sourceStart, callinMapping.sourceEnd);	
}
// EXPERIMENTAL FEATURE:
public void migrateWithinNonFinalTeam(TypeReference superTypeRef, ReferenceBinding teamType) {
	String[] args = { String.valueOf(teamType.readableName()) };
	this.handle(IProblem.MigrateWithinNonFinalTeam, args, args, superTypeRef.sourceStart, superTypeRef.sourceEnd);
}
public void migrateNonRole(TypeReference superTypeRef, ReferenceBinding roleType) {
	String[] args = { String.valueOf(roleType.readableName()) };
	this.handle(IProblem.MigrateNonRole, args, args, superTypeRef.sourceStart, superTypeRef.sourceEnd);
}
public void migrateToNonTeam(Expression expression) {
	this.handle(IProblem.MigrateToNonTeam, NoArgument, NoArgument, expression.sourceStart, expression.sourceEnd);
}

public void migrateToWrongTeam(Expression expression, ReferenceBinding anchorType, ReferenceBinding roleType) {
	String[] args = { String.valueOf(anchorType.readableName()),
					  String.valueOf(roleType.sourceName()),
					  String.valueOf(roleType.enclosingType().readableName()) };
	this.handle(IProblem.MigrateToWrongTeam, args, args, expression.sourceStart, expression.sourceEnd);
}
public void migrateBoundRole(TypeReference superTypeRef, ReferenceBinding roleType) {
	String[] args = { String.valueOf(roleType.readableName()) };
	this.handle(IProblem.MigrateBoundRole, args, args, superTypeRef.sourceStart, superTypeRef.sourceEnd);
}
public void baseMigrateNonRole(TypeReference superTypeRef, ReferenceBinding roleType) {
	String[] args = { String.valueOf(roleType.readableName()) };
	this.handle(IProblem.BaseMigrateNonRole, args, args, superTypeRef.sourceStart, superTypeRef.sourceEnd);
}
public void baseMigrateUnboundRole(TypeReference superTypeRef, ReferenceBinding roleType) {
	String[] args = { String.valueOf(roleType.readableName()) };
	this.handle(IProblem.BaseMigrateUnboundRole, args, args, superTypeRef.sourceStart, superTypeRef.sourceEnd);
}

public void migrateToWrongBase(Expression expression, TypeBinding providedType, ReferenceBinding roleType, ReferenceBinding baseclass) {
	String[] args = { String.valueOf(providedType.readableName()),
					  String.valueOf(roleType.sourceName()),
					  String.valueOf(baseclass.readableName()) };
	this.handle(IProblem.MigrateToWrongBase, args, args, expression.sourceStart, expression.sourceEnd);
}
}
