/*******************************************************************************
 * Copyright (c) 2011 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation 
 *******************************************************************************/
package org.eclipse.objectteams.internal.jdt.nullity.quickfix;

import static org.eclipse.objectteams.internal.jdt.nullity.IConstants.IProblem.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix.CompilationUnitRewriteOperation;
import org.eclipse.jdt.internal.corext.fix.IProposableFix;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.internal.ui.text.correction.proposals.FixCorrectionProposal;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.objectteams.internal.jdt.nullity.NullCompilerOptions;
import org.eclipse.swt.graphics.Image;

import base org.eclipse.jdt.internal.ui.text.correction.QuickFixProcessor;

/**
 * Quickfixes for null-annotation related problems.
 * Hooks into JDT/UI's QuickFixProcessor.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class QuickFixes {

	protected class Processor playedBy QuickFixProcessor {

		hasCorrections <- replace hasCorrections;

		@SuppressWarnings("basecall")
		callin boolean hasCorrections(ICompilationUnit cu, int problemId) {
			switch (problemId) {
			case DefiniteNullFromNonNullMethod:
			case PotentialNullFromNonNullMethod:
			case DefiniteNullToNonNullParameter:
			case PotentialNullToNonNullParameter:
			case IllegalRedefinitionToNullableReturn:
			case IProblem.NonNullLocalVariableComparisonYieldsFalse:
			case IProblem.RedundantNullCheckOnNonNullLocalVariable:
					return true;
			default:
				return base.hasCorrections(cu, problemId);
			}
		}

		process <- after process;

		/**
		 * Add our proposals to the list assembled by the base class.
		 */
		void process(IInvocationContext context, IProblemLocation problem, @SuppressWarnings("rawtypes") Collection proposals) {
			int id= problem.getProblemId();
			if (id == 0) { // no proposals for none-problem locations
				return;
			}
			switch (id) {
			case DefiniteNullFromNonNullMethod:
			case PotentialNullFromNonNullMethod:
			case DefiniteNullToNonNullParameter:
			case PotentialNullToNonNullParameter:
			case IllegalRedefinitionToNullableReturn:
			case IllegalDefinitionToNonNullParameter:
			case IllegalRedefinitionToNonNullParameter:
				addNullAnnotationInSignatureProposal(context, problem, proposals);
				break;			
			case IProblem.NonNullLocalVariableComparisonYieldsFalse:
			case IProblem.RedundantNullCheckOnNonNullLocalVariable:
				if (isComplainingAboutArgument(context, problem))
					addNullAnnotationInSignatureProposal(context, problem, proposals);
				break;
			}			
		}		
	}
		
	@SuppressWarnings("unchecked")
	void addNullAnnotationInSignatureProposal(IInvocationContext context, IProblemLocation problem, @SuppressWarnings("rawtypes") Collection proposals)
	{
		IProposableFix fix= createNullAnnotationInSignatureFix(context.getASTRoot(), problem);
		
		if (fix != null) {
			Image image= JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
			Map<String, String> options= new Hashtable<String, String>();
			options.put(CleanUpConstants.ADD_MISSING_ANNOTATIONS, CleanUpOptions.TRUE);
			switch (problem.getProblemId()) {
			case DefiniteNullFromNonNullMethod:
			case IllegalRedefinitionToNullableReturn:
				options.put(CleanUpConstants.ADD_DEFINITELY_MISSING_RETURN_ANNOTATION_NULLABLE, CleanUpOptions.TRUE);
				break;
			case PotentialNullFromNonNullMethod:
				options.put(CleanUpConstants.ADD_POTENTIALLY_MISSING_RETURN_ANNOTATION_NULLABLE, CleanUpOptions.TRUE);
				break;
			case IProblem.NonNullLocalVariableComparisonYieldsFalse:
			case IProblem.RedundantNullCheckOnNonNullLocalVariable:
			    // may indicate a parameter null-check
			case DefiniteNullToNonNullParameter:
			case IllegalDefinitionToNonNullParameter:
			case IllegalRedefinitionToNonNullParameter:
				options.put(CleanUpConstants.ADD_DEFINITELY_MISSING_PARAMETER_ANNOTATION_NULLABLE, CleanUpOptions.TRUE);
				break;
			}
			FixCorrectionProposal proposal= new FixCorrectionProposal(fix, new NullAnnotationsCleanUp(options, this), 15, image, context);
			proposals.add(proposal);
		}		
	}
	
	boolean isComplainingAboutArgument(IInvocationContext context, IProblemLocation problem) {

		CompilationUnit astRoot= context.getASTRoot();
		ASTNode selectedNode= problem.getCoveringNode(astRoot);

		if (!(selectedNode instanceof SimpleName)) {
			return false;
		}
		SimpleName nameNode= (SimpleName) selectedNode;
		IBinding binding = nameNode.resolveBinding();
		if (binding.getKind() == IBinding.VARIABLE && ((IVariableBinding) binding).isParameter())
			return true;
		return false;
	}

	CompilationUnitRewriteOperationsFix createNullAnnotationInSignatureFix(CompilationUnit compilationUnit, IProblemLocation problem)
	{
		String nullableAnnotationName = getNullableAnnotationName(compilationUnit.getJavaElement(), false);
		String nonNullAnnotationName = getNonNullAnnotationName(compilationUnit.getJavaElement(), false);
		String annotationToAdd = nullableAnnotationName;
		String annotationToRemove = nonNullAnnotationName;
		
		switch (problem.getProblemId()) {
		case IllegalDefinitionToNonNullParameter:
		case IllegalRedefinitionToNonNullParameter:
			annotationToAdd = nonNullAnnotationName;
			annotationToRemove = nullableAnnotationName;
			break;
		// all others propose to add @Nullable
		}
		
		RewriteOperations.SignatureAnnotationRewriteOperation operation = 
			RewriteOperations.createAddAnnotationOperation(
				compilationUnit, problem, annotationToAdd, annotationToRemove, null, false/*thisUnitOnly*/, true/*allowRemove*/);
		if (operation == null)
			return null;
		
		return new CompilationUnitRewriteOperationsFix(operation.getMessage(),
													   operation.getCompilationUnit(), 
													   new CompilationUnitRewriteOperation[] {operation});
	}

	// Entry for NullAnnotationsCleanup:
	public ICleanUpFix createCleanUp(CompilationUnit compilationUnit, 
									 boolean addDefinitelyMissingReturnAnnotations, 
									 boolean addPotentiallyMissingReturnAnnotations, 
									 boolean addDefinitelyMissingParamAnnotations, 
									 IProblemLocation[] locations) 
	{
		
		ICompilationUnit cu= (ICompilationUnit)compilationUnit.getJavaElement();
		if (!JavaModelUtil.is50OrHigher(cu.getJavaProject()))
			return null;
		
		if (! (addDefinitelyMissingReturnAnnotations || addPotentiallyMissingReturnAnnotations || addDefinitelyMissingParamAnnotations))
			return null;
		
		List<CompilationUnitRewriteOperation> operations= new ArrayList<CompilationUnitRewriteOperation>();
		
		if (locations == null) {
			org.eclipse.jdt.core.compiler.IProblem[] problems= compilationUnit.getProblems();
			locations= new IProblemLocation[problems.length];
			for (int i= 0; i < problems.length; i++) {
				if (   (addDefinitelyMissingReturnAnnotations && (problems[i].getID() == DefiniteNullFromNonNullMethod))
					|| (addPotentiallyMissingReturnAnnotations && (problems[i].getID() == PotentialNullFromNonNullMethod))
					|| (addDefinitelyMissingParamAnnotations && mayIndicateParameterNullcheck(problems[i].getID())))
				locations[i]= new ProblemLocation(problems[i]);
			}
		}
		
		createAddNullAnnotationOperations(compilationUnit, locations, operations);
		
		if (operations.size() == 0)
			return null;
		
		CompilationUnitRewriteOperation[] operationsArray= operations.toArray(new CompilationUnitRewriteOperation[operations.size()]);
		return new CompilationUnitRewriteOperationsFix(FixMessages.QuickFixes_add_annotation_change_name, compilationUnit, operationsArray);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void createAddNullAnnotationOperations(CompilationUnit compilationUnit, IProblemLocation[] locations, List result) {
		String nullableAnnotationName = getNullableAnnotationName(compilationUnit.getJavaElement(), false);
		String nonNullAnnotationName = getNonNullAnnotationName(compilationUnit.getJavaElement(), false);
		Set<String> handledPositions = new HashSet<String>();
		for (int i= 0; i < locations.length; i++) {
			IProblemLocation problem= locations[i];
			if (problem == null) continue; // problem was filtered out by createCleanUp()
			String annotationToAdd = nullableAnnotationName;
			String annotationToRemove = nonNullAnnotationName;
			switch (problem.getProblemId()) {
			case IllegalDefinitionToNonNullParameter:
			case IllegalRedefinitionToNonNullParameter:
				annotationToAdd = nonNullAnnotationName;
				annotationToRemove = nullableAnnotationName;
			}
			CompilationUnitRewriteOperation fix = RewriteOperations.createAddAnnotationOperation(
						compilationUnit, problem, annotationToAdd, annotationToRemove, handledPositions, true, false);
			if (fix != null)
				result.add(fix);
		}
	}
	
	public static boolean isMissingNullAnnotationProblem(int id) {
		return id == DefiniteNullFromNonNullMethod || id == PotentialNullFromNonNullMethod 
				|| id == DefiniteNullToNonNullParameter || id == PotentialNullToNonNullParameter
				|| id == IllegalRedefinitionToNullableReturn
				|| mayIndicateParameterNullcheck(id);
	}
	
	public static boolean mayIndicateParameterNullcheck(int problemId) {
		return problemId == IProblem.NonNullLocalVariableComparisonYieldsFalse || problemId == IProblem.RedundantNullCheckOnNonNullLocalVariable;
	}
	
	public static boolean hasExplicitNullAnnotation(ICompilationUnit compilationUnit, int offset) {
		try {
			IJavaElement problemElement = compilationUnit.getElementAt(offset);
			if (problemElement.getElementType() == IJavaElement.METHOD) {
				IMethod method = (IMethod) problemElement;
				String nullable = getNullableAnnotationName(compilationUnit, true);
				String nonnull = getNonNullAnnotationName(compilationUnit, true);
				for (IAnnotation annotation : method.getAnnotations()) {
					if (   annotation.getElementName().equals(nonnull)
						|| annotation.getElementName().equals(nullable))
						return true;
				}
			}
		} catch (JavaModelException jme) {
			/* nop */
		}
		return false;
	}

	public static String getNullableAnnotationName(IJavaElement javaElement, boolean makeSimple) {
		String qualifiedName = javaElement.getJavaProject().getOption(NullCompilerOptions.OPTION_NullableAnnotationName, true);
		int lastDot;
		if (makeSimple && qualifiedName != null && (lastDot = qualifiedName.lastIndexOf('.')) != -1)
			return qualifiedName.substring(lastDot+1);
		return qualifiedName;
	}

	public static String getNonNullAnnotationName(IJavaElement javaElement, boolean makeSimple) {
		String qualifiedName = javaElement.getJavaProject().getOption(NullCompilerOptions.OPTION_NonNullAnnotationName, true);
		int lastDot;
		if (makeSimple && qualifiedName != null && (lastDot = qualifiedName.lastIndexOf('.')) != -1)
			return qualifiedName.substring(lastDot+1);
		return qualifiedName;
	}
}
