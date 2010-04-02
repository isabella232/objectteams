/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PredicateGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import static org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants.STEP_OVER_SOURCEPOSITION_END;
import static org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants.STEP_OVER_SOURCEPOSITION_START;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.GuardPredicateDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * This class offers utilities for generating predicate checks
 * to be inserted in callin wrappers and liftTo methods.
 * It also extends existing predicate methods:
 * + set the type of the base arg after baseclass is resolved (incl. inherited playedBy)
 * + link each predicate to its tsuper, super and outer (logical AND).
 *
 * @author stephan
 * @version $Id: PredicateGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class PredicateGenerator extends SwitchOnBaseTypeGenerator
		implements IOTConstants
{
	/** The role holding the callin binding, we are currently working on. */
	private ReferenceBinding _currentRole;

	// == The following three fields are valid only during createSwitchStatement,
	//    ie., while generating a base predicate check other than team-level.
	//    They are used to tunnel values
	//    - from    internalCreateBasePredicateCheck
	//    - via     createSwitchStatement
	//    - down to genSingleBasePredicateCheck

	/** CallinMapping creating the callin wrapper that invokes the predicate check. */
	private CallinMappingDeclaration _callinMapping = null;
	/** The base method triggering the callin wrapper. */
	private MethodSpec _baseMethod = null;
	/** The name of a variable holding the base result for the case of after-callin bindings. */
	private char[] _resultName = null;

	/**
	 * Nested teams: when travelling out we may need different ways to access the appropriate
	 * base instance (see comment in createBasePredicateCheck()).
	 */
	private char[] _baseVarName = BASE; // per default use the predicate argument "base"

	private boolean _processingReplace = false;

	public PredicateGenerator(ReferenceBinding role, boolean processingReplace) {
		this._currentRole= role;
		this._processingReplace = processingReplace;
	}

	// ============== Base Predicates ================

	/**
	 * During parse the baseclass was not resolved, thus unknown if only inherited.
	 * This method adds the type to the base-arg of a base predicate.
	 *
	 * @param methodDecl
	 * @param arg
	 * @return          success?
	 */
	public static boolean createBaseArgType(AbstractMethodDeclaration methodDecl, Argument arg)
	{
		MethodBinding method = methodDecl.binding;
		AstGenerator gen = new AstGenerator(arg.sourceStart, arg.sourceEnd);
		ReferenceBinding roleType = method.declaringClass.isRole() ? method.declaringClass : null;
		ReferenceBinding baseType = null;
		if (   roleType == null && method.declaringClass.isTeam())
		{
			// outermost team receives unspecified base object
			baseType = methodDecl.scope.getJavaLangObject();
		} else {
			if (roleType == null) {
				// what exactly may cause this? (haven't seen this occurr)
				methodDecl.scope.problemReporter().abortDueToInternalError("predicate in non-role?"); //$NON-NLS-1$
				return false;
			}
			baseType = roleType.baseclass();
		}
		if (baseType == null) {
			methodDecl.scope.problemReporter().basePredicateInUnboundRole(methodDecl, roleType);
			return false;
		} else {
			arg.type = gen.baseclassReference(baseType);
		}
		return true;
	}

	/**
	 * Starting at a callin mapping find the most suitable base predicate check(s).
	 * + If a base predicate is found at role level or below,
	 *   create a switch statement (based on the base tag),
	 *   simulating dynamic binding of a role object which might not yet exist.
	 * + Otherwise only check a team-level base predicate if present.
	 * + If no matching base predicate is found return null.
	 * @param callinMapping CallinMapping creating the callin wrapper
	 *                      that invokes the predicate check
	 * @param baseMethod    The base method triggering the callin wrapper
	 * @param resultName    The name of a variable holding the base result
	 *                      for the case of after-callin bindings
	 * @param gen
	 * @return statement (which possibly throws LiftingVetoException) or null
	 */
	public Statement createBasePredicateCheck(
			CallinMappingDeclaration callinMapping,
			MethodSpec               baseMethod,
			char[]                   resultName,
			AstGenerator             gen)
	{
		ReferenceBinding   roleType  = callinMapping.scope.enclosingSourceType();
		Statement check = null;
		while (check == null && roleType.isRole()) { // travelling out
			check = internalCreateBasePredicateCheck(
						roleType, callinMapping, baseMethod, resultName, gen);
			// Within nested teams, when invoking the base predicate of an enclosing type
			// don't use the "base" argument (base of inner role)
			// but retrieve the base instance (_OT$base) of the next-enclosing role object:
			// (TODO(SH): does not handle far-outer role (would need FarOuterRole.this._OT$base)).
			this._baseVarName = _OT_BASE;
			roleType = roleType.enclosingType();
		}
		this._baseVarName = BASE; // reset to default.
		return check;
	}

	/** hook into createSwithStatement. */
	@Override
	char[] baseVarName() {
		return this._baseVarName;
	}

	private Statement internalCreateBasePredicateCheck(
			ReferenceBinding         roleType,
			CallinMappingDeclaration callinMapping,
			MethodSpec               baseMethod,
			char[]                   resultName,
			AstGenerator             gen)
	{
		CallinCalloutScope scope     = callinMapping.scope;
		ReferenceBinding   teamType  = roleType.enclosingType();
		TypeBinding        objParam  = scope.getJavaLangObject();

		while(roleType.isRole()) { // loop up the superclass chain

			char[][] methodNames = getBasePredicateNames(roleType, callinMapping);

			TypeBinding[] params = new TypeBinding[] {
					(roleType.baseclass() != null) ?
						roleType.baseclass() :
						objParam
					};

			for (int i = 0; i < methodNames.length; i++) {

				boolean valid;
				if (isBindingPredicateName(methodNames[i])) {
					// was found directly at the mapping declaration, no need to check further
					valid = true;
				} else {
		            // find a method or role level base predicate
					MethodBinding predicateMethod = TypeAnalyzer.findMethod(
							scope, roleType, methodNames[i], params);
					valid = predicateMethod.isValidBinding();
				}

				if (valid) {
					// if found create a full switch statement for this role
					Statement switchStat = null;
					try {
						this._callinMapping = callinMapping;
						this._baseMethod = baseMethod;
						this._resultName = resultName;

						if (baseMethod.isStatic()) // no lifting based on dynamic type
							switchStat = genSingleBasePredicateCheck(teamType, roleType, gen);
						else
							switchStat = createSwitchStatement(
								teamType, roleType, roleType.roleModel.getSubRoles(), gen);

					} finally {
						this._callinMapping = null;
						this._baseMethod = null;
						this._resultName = null;
					}
					return switchStat;
				}
			}

			roleType = roleType.superclass();
		}

		// find a team level base predicate
		MethodBinding predicateMethod = TypeAnalyzer.findMethod(
				scope, teamType, BASE_PREDICATE_PREFIX,
				new TypeBinding[]{objParam});
		if (predicateMethod.isValidBinding())
			return genSingleBasePredicateCheck(teamType, /*roleType*/null, gen);
		// nothing found
		return null;
	}

	/**
	 * Create a check regarding a specific base predicate.
     * More values are passed through the fields _callinMapping, _baseMethod, _resultName.
     *
	 * @param teamType
	 * @param roleType if given look for most specific predicate for this role type.
	 * @param gen for generating the statement
	 *
	 * @return a new statement or null
	 */
	private Statement genSingleBasePredicateCheck(
			ReferenceBinding teamType,
			ReferenceBinding roleType,
			AstGenerator     gen)
	{
		// generate something like
		// if (!<roleName>._OT$base_when$(base))
		//    throw new LiftingVetoException(this, base);
		// Instead of <roleName> any predicate name at any of the four levels is possible

		// find the most appropriate method name:
		char[] predicateMethodName = null;
		if (roleType != null) {
			// role level predicates:
			predicateMethodName = findBasePredicateName(teamType, roleType, this._callinMapping);
		}
		if (predicateMethodName == null) {
			// last resort: team level predicates:
			outerLoop: while (teamType != null) { // travel out.
				ReferenceBinding currentTeam = teamType;
				while(currentTeam.isTeam()) {     // travel up (super)
					MethodBinding[] candidates = teamType.getMethods(BASE_PREDICATE_PREFIX);
					if (candidates != null && candidates.length == 1) {
						predicateMethodName = BASE_PREDICATE_PREFIX;
						break outerLoop;
					}
					currentTeam= currentTeam.superclass();
				}
				teamType = teamType.enclosingType();
			}
		}
		if (predicateMethodName == null)
			return null;

		// create parameters:
		Expression[] predicateParameters;
		if (isBindingPredicateName(predicateMethodName)) {
			int numResult = (this._resultName == null) ? 0 : 1;

			// binding guard may need arguments
			if (this._baseMethod.hasSignature) {
				// pass all base arguments
				int nBaseArgs = this._baseMethod.parameters.length;
				predicateParameters = new Expression[nBaseArgs+1+numResult];
				for (int i = 0; i < nBaseArgs; i++) {
					predicateParameters[i+1] = gen.singleNameReference(this._baseMethod.arguments[i].name);
				}
			} else {
				// nothing declared, don't pass any method arguments.
				predicateParameters = new Expression[1];
			}
			predicateParameters[0] = baseReference(BASE, roleType, gen);
			if (this._resultName != null && this._baseMethod.hasSignature)
				predicateParameters[predicateParameters.length-1] = gen.singleNameReference(this._resultName);
		} else {
			// all method- and type level guards go here
			// (base method-guards receive no method arguments)
			predicateParameters = new Expression[] {baseReference(this._baseVarName, roleType, gen)};
		}
		Expression receiver = roleType != null ?
								gen.qualifiedNameReference(roleType) :
								gen.thisReference(); // the team in a team base predicate

		// assemble the statement:
		AstGenerator skipGen = new AstGenerator(STEP_OVER_SOURCEPOSITION_START, STEP_OVER_SOURCEPOSITION_END);
		return
			gen.stealthIfNotStatement(
				gen.messageSend(
					receiver,
					predicateMethodName,
					predicateParameters
				),
				genVetoStatement(skipGen, skipGen.singleNameReference(BASE))
			);
	}
	/** with a covariant base class, a base instance may need to be cast before
	 *  passing to a sub role's predicate. */
	private Expression baseReference(char[] baseVarName, ReferenceBinding roleType, AstGenerator gen) {
		Expression result= gen.singleNameReference(baseVarName);
		if (   roleType != null
			&& (roleType.baseclass() != this._currentRole.baseclass()))
			result= gen.castExpression(result, gen.typeReference(roleType.baseclass()), CastExpression.RAW);
		return result;
	}
	/* Generate the statement by which evaluation to false is signaled,
	 * either an exception (for replace bindings) or a simple return (before, after).
	 * target: target instance (role or base) or null for static settings
	 */
	private Statement genVetoStatement(AstGenerator gen, Expression target) {
		if (this._processingReplace)
			return gen.throwStatement(
				gen.allocation(
					gen.qualifiedTypeReference(ORG_OBJECTTEAMS_LIFTING_VETO),
					new Expression[] {
						gen.thisReference(),
						this._callinMapping.isStaticReplace() ? gen.nullLiteral() : target
					}
				)
			);
		else
			return gen.returnStatement(null);
	}

	private boolean isBindingPredicateName(char[] name) {
		// names have this format: _OT$[base_]when$rmeth${after,before,replace}$bmeth
		return CharOperation.occurencesOf('$', name) == 4;
	}

	/**
	 * Starting at callinMapping, search in two directions (super, outer)
	 * for a base predicate and return the name of its method.
	 * (tsuper should be covered by CopyInheritance!)
	 *
	 * @param teamType
	 * @param roleType
     * @param callinMapping
	 * @return name or null
	 */
	private char[] findBasePredicateName (
			ReferenceBinding         teamType,
			ReferenceBinding         roleType,
			CallinMappingDeclaration callinMapping)
	{
		while (roleType.isRole()) {
			char[][] predicateNames = getBasePredicateNames(roleType, callinMapping);
			for (int i = 0; i < predicateNames.length; i++) {
				MethodBinding[] candidates = roleType.getMethods(predicateNames[i]);
				if (candidates != null && candidates.length > 0) {
					boolean nonTSuperFound = false;
					for(int j = 0; j < candidates.length; j++) {
						if (TSuperHelper.isTSuper(candidates[j]))
							continue;
						if (nonTSuperFound)
							throw new InternalCompilerError("More than one type predicate??"); //$NON-NLS-1$
						nonTSuperFound = true;
					}
					if (nonTSuperFound)
						return predicateNames[i];
				}
			}
			roleType = roleType.superclass(); // = super
		}
		while (teamType != null) {
			char[][] predicateNames = getBasePredicateNames(roleType, callinMapping);
			for (int i = 0; i < predicateNames.length; i++) {
				MethodBinding[] candidates = roleType.getMethods(predicateNames[i]);
				if (candidates != null && candidates.length == 1)
					return predicateNames[i];
			}
			teamType = teamType.enclosingType(); // = outer
		}
		return null;
	}


	// ----------------- Hooks into createSwitchStatement: --------------------
	/**
	 * Add a base predicate check for the selected role:
	 */
	protected Statement createCaseStatement(RoleModel role, AstGenerator gen)
	{
		TypeDeclaration roleType = role.getClassPartAst();
		if (roleType != null) {
			// delegate and wrap the result:
			Statement predicateCheck = genSingleBasePredicateCheck(
						roleType.enclosingType.binding, roleType.binding, gen);
			if (predicateCheck != null) {
				return gen.block(new Statement[]{predicateCheck});
			}
		}
		return null;
	}

	/**
	 * If a team level base predicate exists, this is the default for unmatched base objects.
	 */
	protected Statement createDefaultStatement(ReferenceBinding staticRoleType, AstGenerator gen)
	{
		if (staticRoleType.isAbstract()) {
			// has not generated a case statement, do this instead of travelling out:
			Statement stat= genSingleBasePredicateCheck(staticRoleType.enclosingType(), staticRoleType, gen);
			if (stat != null) {
		        return stat;
			}
		}
		return genSingleBasePredicateCheck(
					staticRoleType.enclosingType(), /*roleType*/null, gen);
	}

	// ================= Regular Predicates: ======================

	/**
	 * Create a check regarding a regular predicate.
	 *
	 * @param mapping   the callin mapping to insert the generated check into.
	 * @param roleType
	 * @param argName   name of a variabe holding the callin target
	 * @param bindingArgs argument expressions provided from the callin binding (passed from the callin wrapper)
	 * @param messageArgs argument expressions as passed to the role method (after lifting/parameter mapping).
	 * @param gen       for generating the statement
	 * @return statement or null
	 */
	public Statement createPredicateCheck(
			CallinMappingDeclaration mapping,
			TypeDeclaration roleType,
			Expression      target,
			Expression[]    bindingArgs,
			Expression[]    messageArgs,
			AstGenerator    gen)
	{
		// if (!_OT$when(<roleVar>))
		//    throw new LiftingVetoException(this, <roleVar>/null);

		char[] predicateMethodName = null;
		try {
			this._callinMapping = mapping; // needed within genSinglePredicateCheck->genVetoStatement

			// 1. look at method mapping:
			if (mapping.predicate != null && !mapping.predicate.isBasePredicate)
				return genSinglePredicateCheck(mapping.predicate.selector, bindingArgs, target, false, gen);

			TypeBinding[] emptyParamTypes = new TypeBinding[0];
			Expression[]  emptyParamExprs = new Expression[0];

			// 2. look at role method:
			predicateMethodName = CharOperation.concatWith(
					new char[][] {
							PREDICATE_METHOD_NAME,
							mapping.roleMethodSpec.selector},
					'$');
			MethodBinding predicateMethod = TypeAnalyzer.findMethod(
							mapping.scope,
							roleType.binding,
							predicateMethodName,
							mapping.roleMethodSpec.parameters);
			if (predicateMethod.isValidBinding()) {
				// method guard directly operates on the messageArgs:
				int requiredArgs = predicateMethod.parameters.length;
				if (messageArgs != null && requiredArgs < messageArgs.length)
					System.arraycopy(
							messageArgs, messageArgs.length-requiredArgs, // copy tail, ie., minus enhanced args.
							messageArgs = new Expression[requiredArgs], 0,
							requiredArgs);
				return genSinglePredicateCheck(predicateMethodName, messageArgs, target, false, gen);
			}


			// 3. etc.: look at role class and all enclosing teams:
			ReferenceBinding currentType = roleType.binding;
			boolean outer = false;
			do {
				predicateMethod = TypeAnalyzer.findMethod(
						mapping.scope, currentType, PREDICATE_METHOD_NAME, emptyParamTypes);
				if (predicateMethod.isValidBinding())
					return genSinglePredicateCheck(PREDICATE_METHOD_NAME, emptyParamExprs, target, outer, gen);
				currentType = currentType.enclosingType();
				outer = true;
			} while (currentType != null && currentType.isTeam());
			return null;
		} finally {
			this._callinMapping = null;
		}
	}

	/**
	 * @param predicateMethodName
	 * @param params
	 * @param roleArgName
	 * @param gen
	 * @return an if-statement
	 */
	private Statement genSinglePredicateCheck(
			char[]       predicateMethodName,
			Expression[] params,
			Expression   target,
			boolean      outer,
			AstGenerator gen)
	{
		AstGenerator skipGen = new AstGenerator(STEP_OVER_SOURCEPOSITION_START, STEP_OVER_SOURCEPOSITION_END);
		return
			gen.stealthIfNotStatement(
				gen.messageSend(
					outer ? (Reference)gen.thisReference()
						  : target,
					predicateMethodName,
					params
				),
				genVetoStatement(skipGen, target.isTypeReference() ? skipGen.nullLiteral() : target)
			);
	}

	// ====== Link predicates, i.e., create a chain from specific to unspecific: ========
	/**
	 * Before resolving statements we might need to insert evaluations of
	 * additional predicates (outer, tsuper, super).
	 *
	 * @param predDecl the predicate method
	 */
	public static void linkPredicates(GuardPredicateDeclaration predDecl)
	{
		if (predDecl.ignoreFurtherInvestigation)
			return;
		if (predDecl.isCopied)
			return;

		if (predDecl.statements == null) {
			if (Config.getStrictDiet() || predDecl.scope.classScope().referenceContext.isConverted)
				return; // we know why there are no statements
			throw new InternalCompilerError("predicate should have statements in this mode"); //$NON-NLS-1$
		}

		ArrayList<Expression> otherPreds = new ArrayList<Expression>();
		AstGenerator gen = new AstGenerator(predDecl.bodyStart, predDecl.bodyEnd);
		// outer:
		Expression callOuterPredicate = getOuterPredicateCheck(predDecl, gen);
		if (callOuterPredicate != null)
			otherPreds.add(callOuterPredicate);

		//  tsuper
		Expression evalTSuperPredicate = getTSuperPredicateChecks(predDecl, gen);
		if (evalTSuperPredicate != null)
			otherPreds.add(evalTSuperPredicate);

		//  super:
		Expression callSuperPredicate = getSuperPredicateCheck(predDecl, gen);
		if (callSuperPredicate != null)
			otherPreds.add(callSuperPredicate);

		// combine all predicates with "&&":
		if (otherPreds.size() > 0)
		{
			ReturnStatement returnStat = predDecl.returnStatement;
			Expression expression = returnStat.expression;
			for (Iterator<Expression> predIter = otherPreds.iterator(); predIter.hasNext();) {
				Expression pred = predIter.next();
				expression = new AND_AND_Expression(
						expression, pred, OperatorIds.AND_AND);
			}
			returnStat.expression = expression;
		}
	}

	/** Find the next outer predicate to be invoked from `pred'. */
	private static Expression getOuterPredicateCheck(MethodDeclaration pred, AstGenerator gen)
	{
		char[]  predName        = pred.selector;
		boolean isBasePredicate = CharOperation.prefixEquals(BASE_PREDICATE_PREFIX, pred.selector);
		ReferenceBinding site        = pred.binding.declaringClass;
		ReferenceBinding currentType = site;

		while (true)
		{
			char[]        outerName   = null;
			TypeBinding[] outerParams = null;
			Expression[]  outerArgs   = null;
			int dollarCount = CharOperation.occurencesOf('$', predName);
			ReferenceBinding declaringClass = currentType; // assume this until we know better
			if (dollarCount == 4) {
				// binding predicate -> method predicate
				int dollarPos = CharOperation.lastIndexOf('$', predName);
				dollarPos = CharOperation.lastIndexOf('$', predName, 0, dollarPos-1);
				outerName = CharOperation.subarray(predName, 0, dollarPos);
				outerParams = getArgTypesFromBindingPredicate(pred);
				outerArgs = makeArgExpressions(pred, outerParams.length, null, gen);
			} else {
				// next outer is a type level predicate:
				if (dollarCount >= 2) {
					// {binding,method} predicate -> role level predicate
					int dollarPos = CharOperation.lastIndexOf('$', predName);
					outerName = CharOperation.subarray(predName, 0, dollarPos);
				} else {
					// type level guard can travel outwards without changing the name:
					outerName = predName;
					declaringClass = currentType.enclosingType();
					if (declaringClass == null || !declaringClass.isTeam())
						return null; // travelling beyond outermost team
				}
				if (isBasePredicate) {
					// base arg only, drop binding/method args.
					outerParams = new TypeBinding[] { pred.binding.parameters[0]    };
					outerArgs   = new Expression [] { gen.singleNameReference(BASE) };
				} else {
					// non-base type-level guard: need no arg
					outerParams = TypeBinding.NO_PARAMETERS;
				}
			}
			if (outerName == null)
				return null;
			MethodBinding outerMethod = TypeAnalyzer.findMethod(
					pred.scope, declaringClass, outerName, outerParams);
			if (outerMethod.isValidBinding())
				return gen.messageSend(
						genOuterReceiver(outerMethod.declaringClass, site, pred.isStatic(), gen),
						outerMethod.selector,
						outerArgs
				);
			currentType = declaringClass;
			predName = outerName;
		}
	}

	private static TypeBinding[] getArgTypesFromBindingPredicate(MethodDeclaration pred) {
		if (pred.arguments != null && pred.arguments.length > 0) {
			char[] lastArgName = pred.arguments[pred.arguments.length-1].name;
			if (CharOperation.equals(lastArgName, IOTConstants.RESULT)) {
				// the 'result' argument from a binding guard is not passed to the method guard.
				int len = pred.binding.parameters.length;
				TypeBinding[] filtered = new TypeBinding[len-1];
				System.arraycopy(pred.binding.parameters, 0, filtered, 0, len-1);
				return filtered;
			}
		}
		return pred.binding.parameters;
	}

	/** Generate the receiver for a call to an outer predicate. */
	private static Expression genOuterReceiver(ReferenceBinding receiverType,
											   ReferenceBinding site,
											   boolean 		    staticScope,
											   AstGenerator     gen)
	{
		if (staticScope && receiverType.isRole()) {
			return gen.qualifiedNameReference(receiverType);
		} else {
			if (receiverType == site)
				return gen.thisReference();
			else
				return gen.qualifiedThisReference(receiverType);
		}
	}
	/**
	 * Find relevant predicates in all tsuper-roles, create check expressions and join them with AND.
	 */
	private static Expression getTSuperPredicateChecks(MethodDeclaration pred, AstGenerator gen)
	{
		ReferenceBinding thisType = pred.binding.declaringClass;
		Expression accumulatedResult = null;
		if (thisType.isRole()) {
			ReferenceBinding[] tsuperRoles = thisType.roleModel.getTSuperRoleBindings();
			for (int i = 0; i < tsuperRoles.length; i++) {
				MethodBinding tsuperMethod = TypeAnalyzer.findMethod(
						pred.scope, tsuperRoles[i], pred.selector, pred.binding.parameters);
				if (!tsuperMethod.isValidBinding())
					continue;
				char[] markerIfcName = TSuperHelper.getTSuperMarkName(tsuperRoles[i].enclosingType());
				Expression current = gen.messageSend(
						    // 'implicit' prevents role type wrapping (tsuper pred is not in the ifc-part)
							ThisReference.implicitThis(),
							pred.selector,
							makeArgExpressions(
								pred,
								tsuperMethod.parameters.length,
								gen.castExpression(
									gen.nullLiteral(),
									gen.singleTypeReference(markerIfcName),
									CastExpression.RAW
								),
								gen
							));
				 if (accumulatedResult == null)
				 	accumulatedResult = current;
				 else
				 	accumulatedResult = new AND_AND_Expression(accumulatedResult, current, OperatorIds.AND_AND);
			}
		}
		return accumulatedResult;
	}
	private static Expression getSuperPredicateCheck(MethodDeclaration pred, AstGenerator gen)
	{
		MethodBinding superMethod = null;
		ReferenceBinding thisType = pred.binding.declaringClass;
		ReferenceBinding superType = thisType.superclass();
		while (superType != null) {
			superMethod = TypeAnalyzer.findMethod(
					pred.scope, superType, pred.selector, pred.binding.parameters);
			if (superMethod.isValidBinding()) {
				return gen.messageSend(
						pred.isStatic() ?
								gen.qualifiedNameReference(superType) :
								gen.superReference(),
						superMethod.selector,
						makeArgExpressions(pred, superMethod.parameters.length, null, gen)
					);
			}
			superType = superType.superclass();
		}
		return null;
	}

	/** Make expressions for passing arguments of pred up to the next predicate.
	 *
	 * @param pred             The predicate hosting the current method call
	 * @param parametersLength the target method needs this many parameters
	 * @param markerArg        if non-null append this as the last argument
	 * @param gen
	 * @return array of argument expressions
	 */
	private static Expression[] makeArgExpressions(
			MethodDeclaration pred,
			int               parametersLength,
			Expression        markerArg,
			AstGenerator      gen)
	{
		Expression[] args;
		if (markerArg != null) {
			args = new Expression[parametersLength+1];
			args[parametersLength] = markerArg;
		} else {
			args = new Expression[parametersLength];
		}
		for (int i = 0; i < parametersLength; i++) {
			args[i] = gen.singleNameReference(pred.arguments[i].name);
		}
		return args;
	}

	// =============== General LOOKUP: ====================


	/**
	 * Construct all names of the potential base predicates for a given role, method, and method mapping.
	 * @param roleType
	 * @param mapping
	 * @return non-null array of names (may still be empty..)
	 */
	private char[][] getBasePredicateNames(
			ReferenceBinding         roleType,
			CallinMappingDeclaration mapping)
	{
		ArrayList<char[]> names = new ArrayList<char[]>(3);
		if (mapping.predicate != null && mapping.predicate.isBasePredicate)
			// not inherited independently, access directly:
			names.add(mapping.predicate.selector);
		MethodBinding method = mapping.getRoleMethod();
		if (method != null)
			names.add(CharOperation.concatWith(
						new char[][]{BASE_PREDICATE_PREFIX, method.selector},
					 	'$'));
		if (roleType != null)
			names.add(BASE_PREDICATE_PREFIX);
		char[][] result = new char[names.size()][];
		names.toArray(result);
		return result;
	}

}
