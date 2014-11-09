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
 * $Id: AbstractMethodMappingDeclaration.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameBINDIN;
import static org.eclipse.jdt.internal.compiler.parser.TerminalTokens.TokenNameBINDOUT;

import java.util.Arrays;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
import org.eclipse.jdt.internal.compiler.problem.AbortType;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.objectteams.otdt.core.compiler.Pair;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.AnchorMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.BaseScopeMarker;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

/**
 * NEW for OTDT
 *
 * Generalizes structure and resolving for Callin/CallountMethodMappings.
 * Subclasses have to provide specific strategies for type checking.
 *
 * @author Markus Witte
 * @version $Id: AbstractMethodMappingDeclaration.java 23401 2010-02-02 23:56:05Z stephan $
 */
public abstract class AbstractMethodMappingDeclaration
	extends ASTNode
	implements ProblemSeverities, ReferenceContext
{
	public int declarationSourceStart; // includes javadoc
	public int declarationSourceEnd; // end of everything including even trailing comment.

	public int bindingTokenStart; // start of '->', '=>' or '<-'
    public int modifierStart;     // either callin modifier or c-t-f modifier or not used
    public int modifierEnd;

	public int bodyStart; // body = parameter mappings ;-)
	public int bodyEnd;
	// sourceStart and sourceEnd point to leftMethodSpec.sourceStart to rightMostMethodSpec.declarationSourceEnd


	public boolean hasSignature;
	public MethodSpec roleMethodSpec;
	// not defined here: baseMethodSpec (different multiplicities for callout (1) vs. callin (n)).
    public ParameterMapping[] mappings;
    // marker for parameter mappings which have been skipped during diet parsing:
    public static final ParameterMapping[] PENDING_MAPPINGS= new ParameterMapping[0];
    /** indices into source signature: */
    public int[] positions; // ordered by target method signature, see comment of recordPosition
    public Pair<Expression, Integer>[] mappingExpressions= null; // ordered by implementing method's signature

    public CallinCalloutScope scope;
	public CallinCalloutBinding binding;

	public CompilationResult compilationResult;
	public boolean ignoreFurtherInvestigation = false;

    public LocalDeclaration resultVar=null;

	public Javadoc javadoc;
	public Annotation[] annotations;
	public boolean hasParsedParamMappings= false;

	public AbstractMethodMappingDeclaration(CompilationResult compilationResult)
	{
		super();
		this.compilationResult=compilationResult;
	}

	/** add a base method spec, iff none has been given yet. */
	public abstract void checkAddBasemethodSpec(MethodSpec baseSpec);

	public abstract boolean isCallin();
	public abstract boolean isCallout();
	public boolean isReplaceCallin()   { return false; }
	public boolean isStaticReplace()   { return false; }
	public boolean isCalloutOverride() { return false; }

	/**
	 * Can an otherwise invisible base method be accessed?
	 * Callout: only if decapsulation is enabled.
	 * Callin:  switch ALLOW_CALLIN_FROM_INVISIBLE.
	 */
	public abstract boolean canAccessInvisibleBase ();

	public CompilationUnitDeclaration getCompilationUnitDeclaration() {
		if (this.scope != null) {
			return this.scope.compilationUnitScope().referenceContext;
		}
		return null;
	}

	/**
	 * @return resolved binding from role-method-spec, or null if unresolved
	 */
	public MethodBinding getRoleMethod() {
		return this.roleMethodSpec.resolvedMethod;
	}

	/**
	 * Provide uniform access to baseMethodSpec(s).
	 */
	public abstract MethodSpec[] getBaseMethodSpecs ();

	/**
	 * Answer the method specification at the implementation (callee) side.
	 */
	public abstract MethodSpec getImplementationMethodSpec();

	protected abstract void checkModifiers (boolean haveBaseMethods, ReferenceBinding baseType);

	// ==== implement ReferenceContext: ====

	public CompilationResult compilationResult()
	{
		return this.compilationResult;
	}

	public boolean hasErrors()
	{
		return this.ignoreFurtherInvestigation;
	}

	public void tagAsHavingErrors()
	{
		this.ignoreFurtherInvestigation = true;
	}
	
	public void tagAsHavingIgnoredMandatoryErrors(int problemId) {
		// nothing here
	}

	public void resetErrorFlag() {
		this.ignoreFurtherInvestigation = false;
	}

	public void abort(int abortLevel, CategorizedProblem problem)
	{
		switch (abortLevel)
		{
			case AbortCompilation :
				throw new AbortCompilation(this.compilationResult, problem);
			case AbortCompilationUnit :
				throw new AbortCompilationUnit(this.compilationResult, problem);
			case AbortType :
				throw new AbortType(this.compilationResult, problem);
			default :
				throw new AbortMethod(this.compilationResult, problem);
		}
	}

	// ==== End(implement ReferenceContext) ====


	/**
	 * Record the fact, that argument argName is mapped to position idx.
	 * Look up the argument in the signature of sourceMethodSpec and store
	 * that position into slot idx of positions.
	 * (Attention: positions in sourceMethodSpec are 1-based, 0 meaning result).
	 * Example:
	 * <pre>
	 * 		void foo(int a1, int a2) <- replace void bar(int b1, int b2, int b3)
	 *      with {
	 * 						 a2 <- b1,
	 * 			             a1 <- b3
	 *      }
	 * </pre>
	 * In this case recordPosition is called with (2, b1) and (1, b3).
	 * As a result, positions will be set to {3,1}.
	 *
	 * @param implIdx target side position to which argName is mapped
	 * @param argName identifier used in this parameter mapping
	 * @param sourceMethodSpec signature at the caller side
	 */
	public Integer recordPosition(int implIdx, char[] argName, MethodSpec sourceMethodSpec) {
		Argument[] sourceArgs = sourceMethodSpec.arguments;
		if (sourceArgs == null) return null; // incomplete mapping
		for (int i = 0; i < sourceArgs.length; i++) {
			if (CharOperation.equals(sourceArgs[i].name, argName)) {
				this.positions[implIdx] = i+1; // 1 based positions for arguments
				return new Integer(i); // TODO (SH): handle double mapping.
			}
		}
		return null;
	}


	public abstract void traverse(ASTVisitor visitor, ClassScope classScope);

	public void resolveMethodSpecs(RoleModel role,
								   ReferenceBinding   baseType,
								   boolean            resolveBaseMethods)
	{
		/*
		 * CallinImplementor will generate one wrapper for each base method.
		 *     (optimize: one for each signature)
		 */

		// 1. resolve types in signatures
		MethodSpec[] baseMethodSpecs = getBaseMethodSpecs();
	    if (this.roleMethodSpec != null) {
	        this.roleMethodSpec.resolveTypes(this.scope, false/*isBaseSide*/);
			// notify parameter mappings:
			if (isCallin() && this.mappings != null)
				for (ParameterMapping map : this.mappings)
					map.argumentsResolved(this.roleMethodSpec);
	    }
		for (int i = 0; i < baseMethodSpecs.length; i++) {
			MethodSpec spec = baseMethodSpecs[i];
			// don't fill scope (would lead to clashes!), but create a new one:
			CallinCalloutScope tmpScope = new CallinCalloutScope(this.scope.classScope(), this.scope.referenceContext);
			spec.resolveTypes(tmpScope, true/*isBaseSide*/);
			// notify parameter mappings:
			if (isCallout() && this.mappings != null)
				for (ParameterMapping map : this.mappings)
					map.argumentsResolved(spec);
		}

		// 2. find role methods ..
		boolean roleOK = true;
		if (! this.hasSignature) {
			// 2a. .. by name only
			// Note(SH): we need the class part to detect overriding
			//           but than we have difficulties finding the correct modifiers :(
			ReferenceBinding receiverType = role.isRegularInterface()
											? role.getBinding()
											: role.getClassPartBinding();
			this.roleMethodSpec.resolveFeature(receiverType, this.scope, isReplaceCallin(), /*isBaseSide*/false, /*allowEnclosing*/(isCallin() && !isReplaceCallin()));
			this.roleMethodSpec.checkResolutionSuccess(role.getBinding(), this.scope);
		} else {
			// 2b. .. by name and signature
			for (int i = 0; i < this.roleMethodSpec.parameters.length; i++) {
				this.roleMethodSpec.parameters[i] = RoleTypeCreator.maybeWrapUnqualifiedRoleType(
						this.scope,
						this.roleMethodSpec.parameters[i],
						this.roleMethodSpec.arguments[i]);
			}
			boolean hasEarlyError = this.ignoreFurtherInvestigation;
			this.roleMethodSpec.resolveFeatureWithArgMapping(role.getBinding(), this.scope, /*isBaseSide*/false, isReplaceCallin(), /*allowEnclosing*/(isCallin() && !isReplaceCallin()));
			boolean reportedRoleMethodError = !hasEarlyError && this.ignoreFurtherInvestigation;

			boolean calloutMissingRoleMethod = false;
			if (isCallout() && this.roleMethodSpec.problemId() == ProblemReasons.NotFound){
				calloutMissingRoleMethod = true;
				// we know we have a signature, repair the return type in the problem binding:
				this.roleMethodSpec.resolvedMethod.returnType = this.roleMethodSpec.returnType.resolvedType;
			}

			if (   !this.roleMethodSpec.isValid()
				&& !reportedRoleMethodError
				&& !calloutMissingRoleMethod)
			{
				this.scope.problemReporter().boundMethodProblem(this.roleMethodSpec, role.getBinding(), isCallout());
				roleOK = false;
			} else {
				// TODO (SH): should we wrap roleMethodSpec.returnType here??
				// (checkReturnType falsely fails in GebitDispo).
				roleOK =    !reportedRoleMethodError
					   	 && this.roleMethodSpec.checkParameterTypes(this.scope, false)
					   	 && this.roleMethodSpec.checkRoleReturnType(this.scope, isCallout());
			}
		}
		// 3. find base methods:
		if (resolveBaseMethods) {
			if (getBaseMethodSpecs().length == 0) {
				// need to recover missing base method spec:
				Parser parser = Config.getParser();
				// this could dispatch to the CompletionParser:
				MethodSpec baseMethodSpec = parser.recoverMissingBaseMethodSpec(this, role);
				if (baseMethodSpec.selector.length == 0)
					resolveBaseMethods = false; // prevent further analysis below.
				if (isCallout())
					((CalloutMappingDeclaration)this).baseMethodSpec = baseMethodSpec;
				else
					((CallinMappingDeclaration)this).baseMethodSpecs = new MethodSpec[]{baseMethodSpec};
			}
			if (baseType != null) {
				if (! this.hasSignature) {
					for (int i = 0; i < baseMethodSpecs.length; i++) {
						MethodSpec methodSpec = baseMethodSpecs[i];
						methodSpec.resolveFeature(baseType, this.scope, /*expectingCallin*/false, /*isBaseSide*/true, /*allowEnclosing*/false);
						methodSpec.checkResolutionSuccess(baseType, this.scope);
						if (   this.roleMethodSpec.isValid()
							&& methodSpec.isValid())
						{
							if (methodSpec instanceof FieldAccessSpec) {
								((CalloutMappingDeclaration)this)
									.checkTypeCompatibility((FieldAccessSpec)methodSpec);
							} else {
								checkParametersCompatibility(methodSpec);
								checkReturnCompatibility(methodSpec);
							}
							checkVisibility(methodSpec, baseType);
							checkThrownExceptions(methodSpec);
						}
					}
				} else {
					// for now pretend all base classes are public/visible:
					int baseTypeModifiers = baseType.modifiers;
		    		baseType.modifiers &= ~ExtraCompilerModifiers.AccVisibilityMASK;
		    		baseType.modifiers |= ClassFileConstants.AccPublic;
		    		try {
						for (int i = 0; i < baseMethodSpecs.length; i++) {
							MethodSpec methodSpec = baseMethodSpecs[i];
		
							methodSpec.resolveFeatureWithArgMapping(baseType, this.scope, /*isBaseSide*/true, /*callinExpected*/false, /*allowEnclosing*/false);
		
							if (!methodSpec.isValid()) {
								if (   methodSpec.problemId() == ProblemReasons.NotVisible
									&& canAccessInvisibleBase())
								{
									methodSpec.resolvedMethod = ((ProblemMethodBinding)methodSpec.resolvedMethod).closestMatch;
								} else {
									this.scope.problemReporter().boundMethodProblem(methodSpec, baseType, isCallout());
									continue;
								}
							}
							if (   !methodSpec.checkBaseReturnType(this.scope, isCallin()?TokenNameBINDOUT:TokenNameBINDIN)
								|| !methodSpec.checkParameterTypes(this.scope, true))
								continue;
							// translation bits are already initialized in the constructor of MethodSpec.
							if (roleOK) {
								if (this.mappings == null && this.hasParsedParamMappings) {
									if (methodSpec instanceof FieldAccessSpec) {
										((CalloutMappingDeclaration)this)
										.checkTypeCompatibility((FieldAccessSpec)methodSpec);
									} else {
										checkParametersCompatibility(methodSpec);
										checkReturnCompatibility(methodSpec);
									}
									// if we have mappings, type checking needs to be deferred..
									// or if we have problems or haven't parsed mappings skip these checks altogether
								} else {
									checkResult(methodSpec);
								}
							}
							// check these in both cases (?)
							checkVisibility(methodSpec, baseType);
							checkThrownExceptions(methodSpec);
						}
		    		} finally {
		    			baseType.modifiers = baseTypeModifiers;
		    		}
				}
			}
		}
		// 4. generally:
		checkModifiers(resolveBaseMethods, baseType);
//{OTDTUI : SelectionOnMethodSpec needs to be informed! Is this the right location ?
		this.roleMethodSpec.resolveFinished();
		for(int idx = 0; idx < baseMethodSpecs.length; idx++)
		{
			if (isCallin())
				if (baseMethodSpecs[idx].resolvedMethod != null && baseMethodSpecs[idx].resolvedMethod.isDeprecated())
					this.scope.problemReporter().callinToDeprecated(baseMethodSpecs[idx], baseMethodSpecs[idx].resolvedMethod);
			baseMethodSpecs[idx].resolveFinished();
		}
//haebor}
		if (this.hasSignature && this.mappings != null)
		{
			// Make simple param mappings a->x, b<-y propagate best names.
			// Currently no bindings exist to propagate,
			// so wrap the SimpleNameReference to do the propagation after it has been resolved.

			MethodSpec implSpec= getImplementationMethodSpec();
			if (implSpec.arguments != null)
			{
				// check each parameter mapping:
				for (ParameterMapping mapping : this.mappings)
					if (mapping.expression instanceof SingleNameReference)
					{
						// for each simple mappling find the corresponding argument:
						char[] token= ((SingleNameReference)mapping.expression).token;
						long pos= (((long)mapping.expression.sourceStart)<<32)+mapping.expression.sourceEnd;
						for (final Argument arg : implSpec.arguments)
							if (CharOperation.equals(arg.name, token))
							{
								// found; is it possibly a team anchor?
								if (arg.binding != null && arg.binding.couldBeTeamAnchor()) {

									// yes, so we need a wrapper:
									Expression wrappedExpr= new SingleNameReference(token,pos) {
										@Override
										public TypeBinding resolveType(BlockScope blockScope) {
											TypeBinding result= super.resolveType(blockScope);
											if (this.binding instanceof ITeamAnchor)
												if (((ITeamAnchor)this.binding).isValidAnchor())
													arg.binding.shareBestName((ITeamAnchor)this.binding);
											return result;
										}
									};
									if (this.mappingExpressions != null) {
										// also update in mappingExpressions (have different order there):
										for (int i = 0; i < this.mappingExpressions.length; i++) {
											Pair<Expression,Integer> pair= this.mappingExpressions[i];
											if (pair.first == mapping.expression) {
												pair.first= wrappedExpr;
												break;
											}
										}
									}
									mapping.expression= wrappedExpr;
								}
								break; // done with this mapping
							}
					}
			}
		}
	}

	public boolean checkVisibility (MethodSpec spec, ReferenceBinding baseType)
	{
		if (spec.isPrivate()) {
			if (TypeBinding.notEquals(baseType.getRealClass(), spec.getDeclaringClass())) {
				this.scope.problemReporter().mappingToInvisiblePrivate(spec, baseType, isCallin());
				return false; // don't report decapsulation if this error is detected.
			}
		}
		spec.checkDecapsulation(baseType, this.scope);
		return true;
	}

	protected void checkResult(MethodSpec baseSpec) { /* default: NOOP */ }


	/** Helper for type checking: are types compatible if auto(un)boxing is used? */
	protected boolean isCompatibleViaBoxing(
							TypeBinding requiredType,
							TypeBinding providedType,
							char[] argName,
							AstGenerator gen)
	{
		SingleNameReference fakeExpr = gen.singleNameReference(argName);
		fakeExpr.computeConversion(this.scope, requiredType, providedType);
		return PotentialTranslationExpression.usesAutoboxing(fakeExpr);
	}

	/**
	 * After method specs have been resolved check whether parameters/return are compatible,
	 * possibly recording the need of translations (lift/lower).
	 *
	 * This method applies only if no parameter mappings are present.
	 * @param methodSpec
	 */
	private void checkParametersCompatibility(MethodSpec methodSpec) {
		TypeBinding[] roleParams = this.roleMethodSpec.resolvedParameters();
		TypeBinding[] baseParams = methodSpec.resolvedParameters();
		AnchorMapping anchorMapping = null;
		try {
			if (this.hasSignature)
			{
				if (isCallout()) {
					// instantiate base params using role spec arguments
					anchorMapping = AnchorMapping.setupNewMapping(null, this.roleMethodSpec.arguments, this.scope);
					baseParams = AnchorMapping.instantiateParameters(this.scope, baseParams, null);
				} else {
					// instantiate role params using base spec arguments
					anchorMapping = AnchorMapping.setupNewMapping(null, methodSpec.arguments, this.scope);
					roleParams = AnchorMapping.instantiateParameters(this.scope, roleParams, null);
				}
			}
			internalCheckParametersCompatibility(methodSpec, roleParams, baseParams);
		} finally {
			if (anchorMapping != null)
				AnchorMapping.removeCurrentMapping(anchorMapping);
		}
	}

	/**
	 * Hook method. Compare role and base side arguments given that no param-mappings are given.
	 *
     * @param methodSpec the base method spec that produced baseParams
	 * @param roleParams the parameters of the resolved role method
	 * @param baseParams the parameters of the resolved base method
	 */
	abstract boolean internalCheckParametersCompatibility(MethodSpec methodSpec, TypeBinding[] roleParams, TypeBinding[] baseParams);

	protected abstract void checkReturnCompatibility(MethodSpec methodSpec);
	protected abstract void checkThrownExceptions (MethodSpec methodSpec);

	protected void checkThrownExceptions(MethodBinding provided, MethodBinding expected) {
		if (provided != null && provided.thrownExceptions != null) {
			if (expected.thrownExceptions != null) {
				outer: for (int i = 0; i < provided.thrownExceptions.length; i++) {
					ReferenceBinding thrown = provided.thrownExceptions[i];
					if (thrown.isUncheckedException(false))
						continue;
					for (int j = 0; j < expected.thrownExceptions.length; j++) {
						ReferenceBinding declared = expected.thrownExceptions[j];
						if (thrown.isCompatibleWith(declared)) {
							continue outer;
						}
					}
					this.scope.problemReporter().callinCalloutUndeclaredException(thrown, this);
				}
			}
		}
	}

	/**
	 * Callout creation might have turned a method into a tsuper version.
	 * Update all affected method specs to the overriding callout method.
	 */
	public void updateTSuperMethods() {
		if (this.ignoreFurtherInvestigation)
			return;
		SourceTypeBinding enclosingSourceType = this.scope.enclosingSourceType();
		this.roleMethodSpec.updateTSuperMethod(enclosingSourceType);
		MethodSpec[] baseMethodSpecs = getBaseMethodSpecs();
		for (int i = 0; i < baseMethodSpecs.length; i++) {
			baseMethodSpecs[i].updateTSuperMethod(enclosingSourceType);
		}
	}

	/**
	 * API for parsing the details (param mappings) of this method mapping.
	 * This method ensures that (this.mappings != null implies this.mappingExpressions != null)
	 * @param parser
	 * @param unit
	 */
	public void parseParamMappings(Parser parser, CompilationUnitDeclaration unit) {

		//fill up the mapping with parameter mappings
		if (this.ignoreFurtherInvestigation)
			return;
		parser.parse(this, unit);
		analyzeParameterMappings(parser);
	}


	/**
	 * After parameter mappings are parsed perform AST-based analysis in order to
	 * create the two arrays positions and mappingExpressions.
	 */
	@SuppressWarnings("unchecked") // array of generic (Pair<Expression,Integer>[]) cannot be specified :(
	public void analyzeParameterMappings(Parser parser)
	{
		if (this.mappings != null)
		{
			for (ParameterMapping mapping : this.mappings)
				if (mapping.direction == TerminalTokens.TokenNameBINDIN) // ident <- expr
					mapping.expression.traverse(new BaseScopeMarker(), this.scope);
			
			// check 4.4(c) - sentence 2:
			if (isCallin() && !isReplaceCallin())
				for (ParameterMapping mapping : this.mappings)
					if (mapping.direction == TerminalTokens.TokenNameBINDOUT) // expr -> ident
						parser.problemReporter().illegalBindingDirectionNonReplaceCallin(mapping);

			{ // prepare positions:
				MethodSpec targetSpec= isCallin()
											? this.roleMethodSpec
											: ((CalloutMappingDeclaration)this).baseMethodSpec;
				Argument[] targetArguments = targetSpec.arguments;
				this.positions= targetArguments != null
											? new int[targetArguments.length]
											: new int[0];
				Arrays.fill(this.positions, -1); // mark as invalid
			}

			{ // compute mappingExpressions:
				MethodSpec implementationMethodSpec = getImplementationMethodSpec();
				int implParamsLength= 0;
				if (implementationMethodSpec.arguments != null)
					implParamsLength= implementationMethodSpec.arguments.length;
				this.mappingExpressions= new Pair[implParamsLength];

				MethodSpec sourceSpec= isCallin()
											? getBaseMethodSpecs()[0]
											: this.roleMethodSpec;
				for (int idx= 0; idx < implParamsLength; idx++) {
			    	char[] targetArgName = implementationMethodSpec.arguments[idx].name;
					Pair<Expression,Integer> mapper = getMappedArgument(sourceSpec, idx, targetArgName);
					this.mappingExpressions[idx]= mapper;
				}
			}
			if (isCallin()) {
				MethodSpec[] allSpecs= getBaseMethodSpecs();
				if (allSpecs.length>1) {
					// FIXME(SH): check equal arguments
				}
			}
		}
	}


	/**
	 * The method mapping contains parameter mappings and the index is within the
	 * range of source code arguments, so let's lookup the argument expression.
	 *
	 * @param sourceMethodSpec
	 * @param implIdx		   index into arguments of the implementation(target) method
	 * @param targetArgName
	 * @return Pair:{the argument expression, the src arg position}
	 */
	Pair<Expression, Integer> getMappedArgument(MethodSpec  sourceMethodSpec,
												int         implIdx,
												char[]      targetArgName)
	{
		Expression mappedArgExpr = null;
		Integer basePos = null;
		int bindingDirection= isCallin() ? TerminalTokens.TokenNameBINDIN : TerminalTokens.TokenNameBINDOUT;
		// look for a mapping:
		for (int i=0; i<this.mappings.length; i++)
		{
			if (   !this.mappings[i].isUsedFor(sourceMethodSpec)
		        && (this.mappings[i].direction == bindingDirection))
		    {
		        if (CharOperation.equals(this.mappings[i].ident.token, targetArgName))
		        {
		            if (mappedArgExpr != null) {
		            	if (this.scope != null)
		            		this.scope.problemReporter().duplicateParamMapping(
										                       this.mappings[i],
										                       targetArgName,
															   isCallout());
		                break;
		            }

		            // TODO(SH) for the case of multiple base methods we actually need to
		            //          clone this expression! (a generic clone is not available!)
		            // we got it, store it!
		            mappedArgExpr = this.mappings[i].expression;

		            // replace requires reversibility:
	            	basePos = analyzeArgForReplace(sourceMethodSpec, implIdx, mappedArgExpr);
		            this.mappings[i].setUsedFor(sourceMethodSpec); // don't use again + check all used.
		        }
		    }
		}
		return new Pair<Expression, Integer>(mappedArgExpr, basePos);
	}

	// record positions, callin does more:
	Integer analyzeArgForReplace(MethodSpec sourceMethodSpec, int idx, Expression mappedArgExpr) {
		if (mappedArgExpr instanceof SingleNameReference) {
			SingleNameReference arg = (SingleNameReference)mappedArgExpr;
			recordPosition(idx, arg.token, sourceMethodSpec);
		}
		return null;
	}

	/**
	 * Look for any single names in the given expression that refer to an argument of the base method.
	 * For error reporting only, thus only the first occurrence is of interest.
	 *
	 * @param mappedArgExpr expression to investigate
	 * @param blockScope    just to please the visitor
	 * @param arguments     base arguments to match against
	 * @return a found reference or null.
	 */
	SingleNameReference findBaseArgName(Expression mappedArgExpr, BlockScope blockScope, final Argument[] arguments) {
		@SuppressWarnings("serial")
		class FoundException extends RuntimeException {
			SingleNameReference name;
			FoundException(SingleNameReference name) {this.name = name;}
		}

		ASTVisitor visitor = new ASTVisitor() {
			public void endVisit(SingleNameReference nameRef, BlockScope blockScope2) {
				for (int i = 0; i < arguments.length; i++) {
					if (CharOperation.equals(arguments[i].name, nameRef.token))
						throw new FoundException(nameRef);
				}
			}
		};
		try {
			mappedArgExpr.traverse(visitor, blockScope);
		} catch (FoundException e) {
			return e.name;
		}
		return null;
	}

	public void resolveAnnotations() {
		resolveAnnotations(this.scope, this.annotations, this.binding);
	}

}
