/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MethodDeclaration.java 23404 2010-02-03 14:10:22Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.CALLIN_FLAG_DEFINITELY_MISSING_BASECALL;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.CALLIN_FLAG_POTENTIALLY_MISSING_BASECALL;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.BaseCallTrackingVariable;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.GuardPredicateDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;

/**
 * OTDT changes:
 *
 * What: Analyze base calls in the control flow within callin methods.
 * Why:  See OTJLD 4.3(b)
 * How:  Create a dummy variable "baseCallTrackingVariable"
 * 		 Each base call is treated like an assignment to that variable.
 *       Use definite assignment analysis regarding the tracking variable.
 * 		 (see also ReturnStatement.analyseCode()).
 *
 * 	     The analysis is combined with checking replace bindings in
 * 		 TypeDeclaration.internalAnalyseCode()
 *
 * What: Cope with copied methods, which have no statements to analyze.
 *
 * @version $Id: MethodDeclaration.java 23404 2010-02-03 14:10:22Z stephan $
 */
public class MethodDeclaration extends AbstractMethodDeclaration {

	public TypeReference returnType;
	public TypeParameter[] typeParameters;

//{ObjectTeams:
	// callin methods use this dummy variable to analyze presence of base calls:
	public BaseCallTrackingVariable baseCallTrackingVariable = null;
	// predicate:
	public GuardPredicateDeclaration predicate;
// SH}

	/**
	 * MethodDeclaration constructor comment.
	 */
	public MethodDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}

	public void analyseCode(ClassScope classScope, InitializationFlowContext initializationContext, FlowInfo flowInfo) {
		// starting of the code analysis for methods
		if (this.ignoreFurtherInvestigation)
			return;
		try {
			if (this.binding == null)
				return;
//{ObjectTeams: some reasons why there might be nothing to analyze:
			// still waiting for statements to be generated?
			if (   this.isGenerated && this.statements == null
				&& !this.scope.isOrgObjectteamsTeam(this.binding.declaringClass)) // o.o.T. methods are special
				return;
// SH}

			if (!this.binding.isUsed() && !this.binding.isAbstract()) {
				if (this.binding.isPrivate()
					|| (((this.binding.modifiers & (ExtraCompilerModifiers.AccOverriding|ExtraCompilerModifiers.AccImplementing)) == 0)
						&& this.binding.isOrEnclosedByPrivateType())) {
					if (!classScope.referenceCompilationUnit().compilationResult.hasSyntaxError) {
//{ObjectTeams: don't report against generated methods nor roles (could be accessed by tsub role):
					  if (   !CharOperation.prefixEquals(IOTConstants.OT_DOLLAR_NAME, this.selector) 
						  && (!classScope.referenceContext.isRole()
						  	  || (classScope.referenceContext.binding.isAnonymousType() && !classScope.referenceContext.isPurelyCopied))) // do report against non-copied anonymous types
// SH}
						this.scope.problemReporter().unusedPrivateMethod(this);
					}
				}
			}
//{ObjectTeams: different reasons why statements may be missing:
			if(   (this.isCopied && this.statements==null)             // really copied
				|| (areStatementsMissing() && !isAbstract())) // not parsed
				return;
// Markus Witte}

			// skip enum implicit methods
			if (this.binding.declaringClass.isEnum() && (this.selector == TypeConstants.VALUES || this.selector == TypeConstants.VALUEOF))
				return;

			// may be in a non necessary <clinit> for innerclass with static final constant fields
			if (this.binding.isAbstract() || this.binding.isNative())
				return;

			ExceptionHandlingFlowContext methodContext =
				new ExceptionHandlingFlowContext(
					initializationContext,
					this,
					this.binding.thrownExceptions,
					null,
					this.scope,
					FlowInfo.DEAD_END);

			// tag parameters as being set
			if (this.arguments != null) {
				for (int i = 0, count = this.arguments.length; i < count; i++) {
					flowInfo.markAsDefinitelyAssigned(this.arguments[i].binding);
				}
			}
			// propagate to statements
			if (this.statements != null) {
				int complaintLevel = (flowInfo.reachMode() & FlowInfo.UNREACHABLE) == 0 ? Statement.NOT_COMPLAINED : Statement.COMPLAINED_FAKE_REACHABLE;
				for (int i = 0, count = this.statements.length; i < count; i++) {
					Statement stat = this.statements[i];
					if ((complaintLevel = stat.complainIfUnreachable(flowInfo, this.scope, complaintLevel)) < Statement.COMPLAINED_UNREACHABLE) {
						flowInfo = stat.analyseCode(this.scope, methodContext, flowInfo);
					}
				}
			}
//{ObjectTeams: check for missing or duplicate base call:
			analyzeBaseCalls(flowInfo.mergedWith(methodContext.initsOnReturn));
// SH}
			// check for missing returning path
			TypeBinding returnTypeBinding = this.binding.returnType;
			if ((returnTypeBinding == TypeBinding.VOID) || isAbstract()) {
				if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0) {
					this.bits |= ASTNode.NeedFreeReturn;
				}
			} else {
				if (flowInfo != FlowInfo.DEAD_END) {
					this.scope.problemReporter().shouldReturn(returnTypeBinding, this);
				}
			}
			// check unreachable catch blocks
			methodContext.complainIfUnusedExceptionHandlers(this);
			// check unused parameters
			this.scope.checkUnusedParameters(this.binding);
		} catch (AbortMethod e) {
			this.ignoreFurtherInvestigation = true;
		}
	}

//{ObjectTeams: check for missing base call:
	void analyzeBaseCalls(FlowInfo flowInfo) {
		if (flowInfo == FlowInfo.DEAD_END)
			return;
		if (isCallin())
		{
			if (!flowInfo.isDefinitelyAssigned(this.baseCallTrackingVariable)) {
				if (!flowInfo.isPotentiallyAssigned(this.baseCallTrackingVariable.binding)) {
					this.scope.problemReporter().definitelyMissingBasecall(this);
					MethodModel.addCallinFlag(this, CALLIN_FLAG_DEFINITELY_MISSING_BASECALL);
				} else {
					this.scope.problemReporter().potentiallyMissingBasecall(this);
					MethodModel.addCallinFlag(this, CALLIN_FLAG_POTENTIALLY_MISSING_BASECALL);
				}
			}
		}
	}
// SH}

	public boolean isMethod() {
		return true;
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		//fill up the method body with statement
		parser.parse(this, unit);
	}

	public StringBuffer printReturnType(int indent, StringBuffer output) {
		if (this.returnType == null) return output;
		return this.returnType.printExpression(0, output).append(' ');
	}

	public void resolveStatements() {
		// ========= abort on fatal error =============
		if (this.returnType != null && this.binding != null) {
			this.returnType.resolvedType = this.binding.returnType;
			// record the return type binding
		}
		// check if method with constructor name
		if (CharOperation.equals(this.scope.enclosingSourceType().sourceName, this.selector)) {
			this.scope.problemReporter().methodWithConstructorName(this);
		}
//{ObjectTeams: further analysis for non-abstract non-diet predicate methods:
		linkPredicates();
// SH}

		if (this.typeParameters != null) {
			for (int i = 0, length = this.typeParameters.length; i < length; i++) {
				this.typeParameters[i].resolve(this.scope);
			}
		}

		// check @Override annotation
		final CompilerOptions compilerOptions = this.scope.compilerOptions();
		checkOverride: {
			if (this.binding == null) break checkOverride;
			long complianceLevel = compilerOptions.complianceLevel;
			if (complianceLevel < ClassFileConstants.JDK1_5) break checkOverride;
			int bindingModifiers = this.binding.modifiers;
			boolean hasOverrideAnnotation = (this.binding.tagBits & TagBits.AnnotationOverride) != 0;
			boolean hasUnresolvedArguments = (this.binding.tagBits & TagBits.HasUnresolvedArguments) != 0;
//{ObjectTeams: re-check overloaded flag Bit10:
			if (hasUnresolvedArguments && this.binding.declaringClass.isRole()) {
				hasUnresolvedArguments = false;
				for (TypeBinding parameter : this.binding.parameters)
					if (!parameter.isValidBinding()) {
						hasUnresolvedArguments = true;
						break;
					}
			}
// SH}
			if (hasOverrideAnnotation  && !hasUnresolvedArguments) {
				// no static method is considered overriding
				if ((bindingModifiers & (ClassFileConstants.AccStatic|ExtraCompilerModifiers.AccOverriding)) == ExtraCompilerModifiers.AccOverriding)
					break checkOverride;
				//	in 1.5, strictly for overriding superclass method
				//	in 1.6 and above, also tolerate implementing interface method
				if (complianceLevel >= ClassFileConstants.JDK1_6
						&& ((bindingModifiers & (ClassFileConstants.AccStatic|ExtraCompilerModifiers.AccImplementing)) == ExtraCompilerModifiers.AccImplementing))
					break checkOverride;
				// claims to override, and doesn't actually do so
				this.scope.problemReporter().methodMustOverride(this, complianceLevel);
			} else {
				//In case of  a concrete class method, we have to check if it overrides(in 1.5 and above) OR implements a method(1.6 and above).
				//Also check if the method has a signature that is override-equivalent to that of any public method declared in Object.
//{ObjectTeams: don't report against generated/copied method:
			  if (!this.isGenerated && !this.isCopied)
// SH}
				if (!this.binding.declaringClass.isInterface()){
						if((bindingModifiers & (ClassFileConstants.AccStatic|ExtraCompilerModifiers.AccOverriding)) == ExtraCompilerModifiers.AccOverriding) {
							this.scope.problemReporter().missingOverrideAnnotation(this);
						} else {
							if(complianceLevel >= ClassFileConstants.JDK1_6
								&& compilerOptions.reportMissingOverrideAnnotationForInterfaceMethodImplementation
								&& this.binding.isImplementing()) {
									// actually overrides, but did not claim to do so

									this.scope.problemReporter().missingOverrideAnnotationForInterfaceMethodImplementation(this);
							}
							
						}
				}
				else {	//For 1.6 and above only
					//In case of a interface class method, we have to check if it overrides a method (isImplementing returns true in case it overrides)
					//Also check if the method has a signature that is override-equivalent to that of any public method declared in Object.
					if(complianceLevel >= ClassFileConstants.JDK1_6
							&& compilerOptions.reportMissingOverrideAnnotationForInterfaceMethodImplementation
							&& (((bindingModifiers & (ClassFileConstants.AccStatic|ExtraCompilerModifiers.AccOverriding)) == ExtraCompilerModifiers.AccOverriding) || this.binding.isImplementing())){
						// actually overrides, but did not claim to do so
						this.scope.problemReporter().missingOverrideAnnotationForInterfaceMethodImplementation(this);
					}
				}
			}
		}

		// by grammatical construction, interface methods are always abstract
		switch (TypeDeclaration.kind(this.scope.referenceType().modifiers)) {
			case TypeDeclaration.ENUM_DECL :
				if (this.selector == TypeConstants.VALUES) break;
				if (this.selector == TypeConstants.VALUEOF) break;
				//$FALL-THROUGH$
			case TypeDeclaration.CLASS_DECL :
				// if a method has an semicolon body and is not declared as abstract==>error
				// native methods may have a semicolon body
				if ((this.modifiers & ExtraCompilerModifiers.AccSemicolonBody) != 0) {
					if ((this.modifiers & ClassFileConstants.AccNative) == 0)
						if ((this.modifiers & ClassFileConstants.AccAbstract) == 0)
							this.scope.problemReporter().methodNeedBody(this);
				} else {
					// the method HAS a body --> abstract native modifiers are forbiden
					if (((this.modifiers & ClassFileConstants.AccNative) != 0) || ((this.modifiers & ClassFileConstants.AccAbstract) != 0))
						this.scope.problemReporter().methodNeedingNoBody(this);
				}
		}
		super.resolveStatements();

		// TagBits.OverridingMethodWithSupercall is set during the resolveStatements() call
		if (compilerOptions.getSeverity(CompilerOptions.OverridingMethodWithoutSuperInvocation) != ProblemSeverities.Ignore) {
			if (this.binding != null) {
        		int bindingModifiers = this.binding.modifiers;
        		if ((bindingModifiers & (ExtraCompilerModifiers.AccOverriding|ExtraCompilerModifiers.AccImplementing)) == ExtraCompilerModifiers.AccOverriding
        				&& (this.bits & ASTNode.OverridingMethodWithSupercall) == 0) {
        			this.scope.problemReporter().overridesMethodWithoutSuperInvocation(this.binding);
        		}
			}
		}

//{ObjectTeams: create a dummy local responsible for tracking base calls:
		if (isCallin() && !isAbstract()) {
			// do this last, so the variable doesn't mess up variable slots:
			this.baseCallTrackingVariable = new BaseCallTrackingVariable(this);
			this.baseCallTrackingVariable.resolve(this.scope);
	    }
// SH}
	}
//{ObjectTeams: hook for subclass:
	protected void linkPredicates() { /* noop */ }
// SH}

	public void traverse(
		ASTVisitor visitor,
		ClassScope classScope) {

		if (visitor.visit(this, classScope)) {
			if (this.javadoc != null) {
				this.javadoc.traverse(visitor, this.scope);
			}
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, this.scope);
			}
			if (this.typeParameters != null) {
				int typeParametersLength = this.typeParameters.length;
				for (int i = 0; i < typeParametersLength; i++) {
					this.typeParameters[i].traverse(visitor, this.scope);
				}
			}
			if (this.returnType != null)
				this.returnType.traverse(visitor, this.scope);
			if (this.arguments != null) {
				int argumentLength = this.arguments.length;
				for (int i = 0; i < argumentLength; i++)
					this.arguments[i].traverse(visitor, this.scope);
			}
			if (this.thrownExceptions != null) {
				int thrownExceptionsLength = this.thrownExceptions.length;
				for (int i = 0; i < thrownExceptionsLength; i++)
					this.thrownExceptions[i].traverse(visitor, this.scope);
			}
			if (this.statements != null) {
				int statementsLength = this.statements.length;
				for (int i = 0; i < statementsLength; i++)
					this.statements[i].traverse(visitor, this.scope);
			}
		}
		visitor.endVisit(this, classScope);
	}
	public TypeParameter[] typeParameters() {
	    return this.typeParameters;
	}
}
