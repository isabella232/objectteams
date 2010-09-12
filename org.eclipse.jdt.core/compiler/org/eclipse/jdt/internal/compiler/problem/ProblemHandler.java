/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration.WrapperKind;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.GuardPredicateDeclaration;

/*
 * Compiler error handler, responsible to determine whether
 * a problem is actually a warning or an error; also will
 * decide whether the compilation task can be processed further or not.
 *
 * Behavior : will request its current policy if need to stop on
 *	first error, and if should proceed (persist) with problems.
 */

public class ProblemHandler {

	public final static String[] NoArgument = CharOperation.NO_STRINGS;

	final public IErrorHandlingPolicy policy;
	public final IProblemFactory problemFactory;
	public final CompilerOptions options;

//{ObjectTeams: support for passing a rechecker:
	public IProblemRechecker rechecker;
// SH}

/*
 * Problem handler can be supplied with a policy to specify
 * its behavior in error handling. Also see static methods for
 * built-in policies.
 *
 */
public ProblemHandler(IErrorHandlingPolicy policy, CompilerOptions options, IProblemFactory problemFactory) {
	this.policy = policy;
	this.problemFactory = problemFactory;
	this.options = options;
}
/*
 * Given the current configuration, answers which category the problem
 * falls into:
 *		Error | Warning | Ignore
 */
public int computeSeverity(int problemId){

	return ProblemSeverities.Error; // by default all problems are errors
}
public CategorizedProblem createProblem(
	char[] fileName,
	int problemId,
	String[] problemArguments,
	String[] messageArguments,
	int severity,
	int problemStartPosition,
	int problemEndPosition,
	int lineNumber,
	int columnNumber) {

	return this.problemFactory.createProblem(
		fileName,
		problemId,
		problemArguments,
		messageArguments,
		severity,
		problemStartPosition,
		problemEndPosition,
		lineNumber,
		columnNumber);
}
public CategorizedProblem createProblem(
		char[] fileName,
		int problemId,
		String[] problemArguments,
		int elaborationId,
		String[] messageArguments,
		int severity,
		int problemStartPosition,
		int problemEndPosition,
		int lineNumber,
		int columnNumber) {
	return this.problemFactory.createProblem(
		fileName,
		problemId,
		problemArguments,
		elaborationId,
		messageArguments,
		severity,
		problemStartPosition,
		problemEndPosition,
		lineNumber,
		columnNumber);
}
public void handle(
	int problemId,
	String[] problemArguments,
	int elaborationId,
	String[] messageArguments,
	int severity,
	int problemStartPosition,
	int problemEndPosition,
	ReferenceContext referenceContext,
	CompilationResult unitResult) {
//{ObjectTeams: wrap once more to reset rechecker after handling:
	try {
		protectedHandle(problemId, problemArguments, elaborationId, messageArguments, severity, problemStartPosition, problemEndPosition, referenceContext, unitResult);
	} finally {
		this.rechecker = null;	
	}
}
private void protectedHandle(
		int problemId,
		String[] problemArguments,
		int elaborationId,
		String[] messageArguments,
		int severity,
		int problemStartPosition,
		int problemEndPosition,
		ReferenceContext referenceContext,
		CompilationResult unitResult)
{
// SH}
	if (severity == ProblemSeverities.Ignore)
		return;

//{ObjectTeams: several kinds of filtering:
	// some problems cannot be decided at the point of reporting, require recking:
	boolean requireRecheck = this.rechecker != null;
	// avoid unnecessary duplication of diagnostics:
	boolean markGenerated = false;
	// ignore errors in generated/copied methods if errors have already been reported
	if (referenceContext instanceof AbstractMethodDeclaration) {
		AbstractMethodDeclaration method = (AbstractMethodDeclaration)referenceContext;
		if (   method.isCopied
			|| (   method.isGenerated
				&& (method.isMappingWrapper == WrapperKind.NONE)
				&& !(method instanceof GuardPredicateDeclaration)))
		{
			// safer to ignore only if some error has already been reported
			boolean alreadyReported = ((severity & ProblemSeverities.Error) != 0)
										? method.compilationResult().hasErrors()
										: method.compilationResult().hasWarnings();
			if (alreadyReported) {
				if ((severity & ProblemSeverities.Fatal) != 0)
					if (!requireRecheck)
						referenceContext.tagAsHavingErrors();
				return;
			}
		} else if (method.binding != null) {
			ReferenceBinding clazz = method.binding.declaringClass;
			if (clazz != null && clazz.isRole() && !clazz.isRegularInterface()) {
				// has the same problem already been reported (re the other part; ifc/class)?
				if (unitResult.problemCount > 0 && unitResult.problems != null)
					for (CategorizedProblem prob : unitResult.problems)
						if (   prob != null
							&& prob.getID() == problemId
							&& prob.getSourceStart() == problemStartPosition
							&& prob.getSourceEnd()   == problemEndPosition)
						{
							if ((severity & ProblemSeverities.Error) != 0) {
								method.tagAsHavingErrors();
								return; // don't record error again.
							}
							// still need the warning to match against @SuppressWarnings,
							// but don't report it to the user
							markGenerated = true;
						}
			}
		}
	}
	// ignore most errors in purely copied classes:
	if (   referenceContext instanceof TypeDeclaration
		&& ((TypeDeclaration)referenceContext).isPurelyCopied)
	{
		switch (problemId) {
		// do report issues that could be caused by multiple inheritance:
		// super-type issues:
		case IProblem.RoleClassOverridesInterface:
		case IProblem.RoleInterfaceOverridesClass:
		case IProblem.OverridingFinalRole:
		case IProblem.IncompatibleSuperclasses:
		case IProblem.RoleShadowsVisibleType:
		case IProblem.RegularOverridesTeam:
		case IProblem.IncomparableTSupers:
		// playedBy issues:
		case IProblem.OverlappingRoleHierarchies:
		case IProblem.IllegalPlayedByRedefinition:
		case IProblem.IncompatibleBaseclasses:
		case IProblem.OverridingPlayedBy:
		case IProblem.BaseclassIsRoleOfTheSameTeam:
		case IProblem.RoleBindingPotentiallyAmbiguous:
		case IProblem.DefiniteLiftingAmbiguity:
		case IProblem.AbstractPotentiallyRelevantRole:
		case IProblem.AbstractRelevantRole:
			break;
		// also report build problems:
		case IProblem.MissingCopiedRole:
			break;
		default:
			return;
		}
	}
// SH}

	// if no reference context, we need to abort from the current compilation process
	if (referenceContext == null) {
		if ((severity & ProblemSeverities.Error) != 0) { // non reportable error is fatal
			CategorizedProblem problem = this.createProblem(null, problemId, problemArguments, elaborationId, messageArguments, severity, 0, 0, 0, 0);
			throw new AbortCompilation(null, problem);
		} else {
			return; // ignore non reportable warning
		}
	}

	int[] lineEnds;
	int lineNumber = problemStartPosition >= 0
			? Util.getLineNumber(problemStartPosition, lineEnds = unitResult.getLineSeparatorPositions(), 0, lineEnds.length-1)
			: 0;
	int columnNumber = problemStartPosition >= 0
			? Util.searchColumnNumber(unitResult.getLineSeparatorPositions(), lineNumber, problemStartPosition)
			: 0;
	CategorizedProblem problem =
		this.createProblem(
			unitResult.getFileName(),
			problemId,
			problemArguments,
			elaborationId,
			messageArguments,
			severity,
			problemStartPosition,
			problemEndPosition,
			lineNumber,
			columnNumber);
//{ObjectTeams: transfer more info to the problem:
	if (problem instanceof DefaultProblem) {
		DefaultProblem defaultProblem = (DefaultProblem)problem;
		defaultProblem.rechecker = this.rechecker;
		if (markGenerated)
			defaultProblem.markGenerated();
	}
// SH}

	if (problem == null) return; // problem couldn't be created, ignore

	switch (severity & ProblemSeverities.Error) {
		case ProblemSeverities.Error :
			record(problem, unitResult, referenceContext);
			if ((severity & ProblemSeverities.Fatal) != 0) {
//{ObjectTeams: problems with a rechecker are considered "optional", don't tag the reference context
			  if (!requireRecheck)
// SH}
				referenceContext.tagAsHavingErrors();
				// should abort ?
				int abortLevel;
				if ((abortLevel = this.policy.stopOnFirstError() ? ProblemSeverities.AbortCompilation : severity & ProblemSeverities.Abort) != 0) {
					referenceContext.abort(abortLevel, problem);
				}
			}
			break;
		case ProblemSeverities.Warning :
			record(problem, unitResult, referenceContext);
			break;
	}
}
/**
 * Standard problem handling API, the actual severity (warning/error/ignore) is deducted
 * from the problem ID and the current compiler options.
 */
public void handle(
	int problemId,
	String[] problemArguments,
	String[] messageArguments,
	int problemStartPosition,
	int problemEndPosition,
	ReferenceContext referenceContext,
	CompilationResult unitResult) {

	this.handle(
		problemId,
		problemArguments,
		0, // no message elaboration
		messageArguments,
		computeSeverity(problemId), // severity inferred using the ID
		problemStartPosition,
		problemEndPosition,
		referenceContext,
		unitResult);
}
public void record(CategorizedProblem problem, CompilationResult unitResult, ReferenceContext referenceContext) {
	unitResult.record(problem, referenceContext);
}
}
