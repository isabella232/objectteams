/** 
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CallinImplementorDyn.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors: Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.mappings;

import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPublic;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.compiler.Pair;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.PotentialLiftExpression;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.OTDynCallinBindingsAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.AbstractStatementsGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.MethodSignatureEnhancer;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstClone;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;

/**
 * This class translates callin binding to the dynamic weaving strategy.
 * This strategy is enabled by defining the property <code>ot.weaving=dynamic</code> 
 * 
 * @author stephan
 * @since 1.3.0M3
 */
public class CallinImplementorDyn extends MethodMappingImplementor {
	

	public static boolean DYNAMIC_WEAVING = "dynamic".equals(System.getProperty("ot.weaving")); //$NON-NLS-1$ $NON-NLS-2$

	//_OT$role
	static final char[] ROLE_VAR_NAME = CharOperation.concat(IOTConstants.OT_DOLLAR_NAME, IOTConstants.ROLE);

	// method names
	private static final char[] OT_CALL_BEFORE  = "_OT$callBefore".toCharArray(); //$NON-NLS-1$
	private static final char[] OT_CALL_AFTER   = "_OT$callAfter".toCharArray(); //$NON-NLS-1$
	private static final char[] OT_CALL_REPLACE = "_OT$callReplace".toCharArray(); //$NON-NLS-1$
	// used for base calls:
	public  static final char[] OT_CALL_NEXT    = "_OT$callNext".toCharArray(); //$NON-NLS-1$
	
	// variable names (arguments ...)
	private static final char[] TEAMS 			= "teams".toCharArray();
	private static final char[] INDEX			= "index".toCharArray();
	private static final char[] CALLIN_ID 		= "callinID".toCharArray();
	private static final char[] BOUND_METHOD_ID = "boundMethodID".toCharArray();
	private static final char[] ARGUMENTS 		= "arguments".toCharArray();
	private static final char[] _OT_RESULT		= "_OT$result".toCharArray();
	private static final char[] RESULT		 	= "result".toCharArray();
	private static final String LOCAL_ROLE 		= "local$";
	
	// for call next:
	private static final char[] BASE_CALL_ARGS  = "baseCallArguments".toCharArray();  


	
	private RoleModel _role;
	private ClassScope _roleScope;

	/**
	 * Main entry from Dependencies - roles.
	 * 
	 * Generates the byte code attributes.
	 */
	public void transformRole(RoleModel role)
	{
		this._role = role;
		this._roleScope = role.getAst().scope; // we definitely have an AST here
		this.bindingDirection = TerminalTokens.TokenNameBINDIN;
		
		AbstractMethodMappingDeclaration[] methodMappings =	this._role.getAst().callinCallouts;
		if(methodMappings == null || methodMappings.length == 0)
		{
			// there are no mappings in this role!
			return;
		}

		if (this._role._hasBindingAmbiguity) {
			for (int i = 0; i < methodMappings.length; i++) {
				this._roleScope.problemReporter().callinDespiteBindingAmbiguity(
									this._role.getBinding(), methodMappings[i]);
				methodMappings[i].tagAsHavingErrors();
			}
			return;
		}
        CallinMappingDeclaration[] callinMappings = new CallinMappingDeclaration[methodMappings.length];
        int num = 0;
//        LinkedList<CallinMappingDeclaration> staticReplaces = new LinkedList<CallinMappingDeclaration>();
		for (int idx = 0; idx < methodMappings.length; idx++)
		{
			AbstractMethodMappingDeclaration methodMapping = methodMappings[idx];
			if(!methodMapping.ignoreFurtherInvestigation && methodMapping.isCallin())
			{
// CRIPPLE:
// 				  result &= createCallin((CallinMappingDeclaration) methodMapping);
                callinMappings[num++] = (CallinMappingDeclaration)methodMapping;
//                if (methodMapping.isStaticReplace())
//                	staticReplaces.add((CallinMappingDeclaration)methodMapping);
			}
		}
        System.arraycopy(
                callinMappings, 0,
                callinMappings = new CallinMappingDeclaration[num], 0,
                num);
        OTDynCallinBindingsAttribute.createOrMerge(this._role.getTeamModel(), this._role.getBaseTypeBinding().constantPoolName(), callinMappings);
// CRIPPLE:
//        if (staticReplaces.size() > 0) {
//        	CallinMappingDeclaration[] callins = new CallinMappingDeclaration[staticReplaces.size()];
//        	staticReplaces.toArray(callins);
//        	this._role.getTeamModel().addOrMergeAttribute(new StaticReplaceBindingsAttribute(callins));
//        }
	}

	// copied and slightly adjusted from old CallinImplementor:
	Expression getArgument(
			AbstractMethodMappingDeclaration methodMapping,
			MethodDeclaration       		 wrapperDeclaration,
			TypeBinding[]                    implParameters,
			int		      					 idx,
			final MethodSpec				 sourceMethodSpec)
	{
		final MethodSpec implementationMethodSpec = methodMapping.getImplementationMethodSpec();

	    Expression mappedArgExpr = null;
	    int        pos           = -1;
	    char[]     targetArgName = null;

		int generatedArgsLen = methodMapping.isReplaceCallin() ?
									MethodSignatureEnhancer.ENHANCING_ARG_LEN:
									0;
		final int srcIdx = idx-generatedArgsLen; // index into source-code signatures.

    	targetArgName = implementationMethodSpec.arguments[srcIdx].name;
    	// retrieve info collected during analyzeParameterMappings:
    	Pair<Expression,Integer> mapper = methodMapping.mappingExpressions[srcIdx];
		mappedArgExpr = mapper.first;
		if (mapper.second != null)
			pos = mapper.second.intValue();

	    if (mappedArgExpr != null) {
	    	SourceTypeBinding roleType = methodMapping.scope.enclosingSourceType();

	    	if (idx >= implParameters.length) // CLOVER: never true in jacks suite
	    		return mappedArgExpr; // arg is invisible to receiver, don't lift
			TypeBinding expectedType = implParameters[idx];

			// arg might have been weakened:
			if (   expectedType.isRole()
				&& expectedType.enclosingType() != roleType.enclosingType())
						expectedType = TeamModel.strengthenRoleType(roleType, expectedType);

			AstGenerator gen = new AstGenerator(mappedArgExpr.sourceStart, mappedArgExpr.sourceEnd);
			Expression receiver = null;
			if (   RoleTypeBinding.isRoleWithoutExplicitAnchor(expectedType)
				&& roleType.getRealClass() == ((ReferenceBinding)expectedType).enclosingType())
			{
				// expectedType is a role of the current role(=team),
				// use the role as the receiver for the lift call:
				receiver = gen.singleNameReference(ROLE_VAR_NAME);
			}
			if (sourceMethodSpec.hasSignature) {
				if (sourceMethodSpec.argNeedsTranslation(srcIdx)) {
					return Lifting.liftCall(methodMapping.scope,
											receiver != null ? receiver : ThisReference.implicitThis(),
											mappedArgExpr,
											sourceMethodSpec.resolvedParameters()[srcIdx],
											expectedType,
											methodMapping.isReplaceCallin()/*needLowering*/);
				}
				if (methodMapping.mappings == null)
					// we have signatures and no parameter mapping.
					// if no need for translation has been recorded, it IS not needed.
					return mappedArgExpr;
			}
			// don't know yet whether lifting is actually needed (=>potentially)
			Expression liftExpr =
				gen.potentialLift(receiver, mappedArgExpr, expectedType, methodMapping.isReplaceCallin());  // reversible?

			// if param mappings are present, connect the PLE to the method spec for propagating translation flag
			// (CallinMethodMappingsAttribute needs to know whether lifting is actually needed.)
			if (methodMapping.mappings != null && pos != -1 && liftExpr instanceof PotentialLiftExpression) {
				final int srcPos = pos;
				((PotentialLiftExpression)liftExpr).onLiftingRequired(new Runnable() {public void run() {
					sourceMethodSpec.argNeedsTranslation[srcPos] = true;
					implementationMethodSpec.argNeedsTranslation[srcIdx] = true; 
				}});
			}

			return liftExpr;
	    }
	    wrapperDeclaration.scope.problemReporter().unmappedParameter(
	            targetArgName,
	            implementationMethodSpec,
				methodMapping.isCallout());
	    return null;
	}

	/**
	 * Main entry from Dependencies - teams.
	 * 
	 * Creates the dispatch methods.
	 */
	public void transformTeam(TeamModel aTeam) 
	{
		List<CallinMappingDeclaration> beforeMappings = new ArrayList<CallinMappingDeclaration>();
		List<CallinMappingDeclaration> replaceMappings = new ArrayList<CallinMappingDeclaration>();
		List<CallinMappingDeclaration> afterMappings = new ArrayList<CallinMappingDeclaration>();
		
		for (RoleModel role : aTeam.getRoles(false)) {
			TypeDeclaration roleDecl = role.getAst(); // FIXME(SH): this breaks incremental compilation: all roles must be present as AST!!
			if (roleDecl == null) continue; // FIXME(SH): check if this is OK
			if (roleDecl.callinCallouts != null) {
				for (AbstractMethodMappingDeclaration mappingDecl : roleDecl.callinCallouts) {
					if (mappingDecl.isCallin()) {
						CallinMappingDeclaration callinDecl = (CallinMappingDeclaration) mappingDecl;
						switch (callinDecl.callinModifier) {
							case TerminalTokens.TokenNamebefore: 	beforeMappings.add(callinDecl); 	break;
							case TerminalTokens.TokenNamereplace: 	replaceMappings.add(callinDecl); 	break;
							case TerminalTokens.TokenNameafter: 	afterMappings.add(callinDecl); 		break;
						}
					}
				}
			}
		}
		if (beforeMappings.size() > 0)
			generateDispatchMethod(OT_CALL_BEFORE,  false, false, beforeMappings, aTeam);
		if (afterMappings.size() > 0)
			generateDispatchMethod(OT_CALL_AFTER,   false, true, afterMappings, aTeam);
		if (replaceMappings.size() > 0) {
			generateDispatchMethod(OT_CALL_REPLACE, true,  false, replaceMappings, aTeam);
			generateCallNext(replaceMappings, aTeam);
		}
	}

	private void generateDispatchMethod(char[] methodName, final boolean isReplace, boolean isAfter, final List<CallinMappingDeclaration> callinDecls, final TeamModel aTeam) 
	{
		// FIXME(SH): once we know that Team has empty implementations (and checked cases involving team inheritance)
		// we probably want to avoid generating empty methods here.
		final TypeDeclaration teamDecl = aTeam.getAst();
		if (teamDecl == null) return;
		final AstGenerator gen = new AstGenerator(teamDecl);
		
		// public void _OT$callBefore   (IBoundBase base, 							int boundMethodId, int callinId, 	Object[] args)
		// public void _OT$callAfter	(IBoundBase base, 							int boundMethodId, int callinId, 	Object[] args, Object result)
		// public void _OT$callReplace	(IBoundBase base, Team[] teams, int index, 	int boundMethodId, int[] callinIds, Object[] args)
		int length = 4;
		if (isReplace)
			length = 6;
		else if (isAfter)
			length = 5;
		Argument[] arguments = new Argument[length]; 
		int a = 0;
		arguments[a++] 		= gen.argument(IOTConstants.BASE, gen.qualifiedTypeReference(IOTConstants.ORG_OBJECTTEAMS_IBOUNDBASE));
		if (isReplace)
			arguments[a++] 	= gen.argument(TEAMS, gen.qualifiedArrayTypeReference(IOTConstants.ORG_OBJECTTEAMS_ITEAM, 1));
		if (isReplace)
			arguments[a++] 	= gen.argument(INDEX, gen.typeReference(TypeBinding.INT));
		arguments[a++] 		=
			isReplace  	?	  gen.argument(CALLIN_ID, gen.createArrayTypeReference(TypeBinding.INT, 1))
						:	  gen.argument(CALLIN_ID, gen.typeReference(TypeBinding.INT));
		arguments[a++] 		= gen.argument(BOUND_METHOD_ID, gen.typeReference(TypeBinding.INT));
		arguments[a++] 		= gen.argument(ARGUMENTS, gen.qualifiedArrayTypeReference(TypeConstants.JAVA_LANG_OBJECT, 1));
		if (isAfter)
			arguments[a++]	= gen.argument(_OT_RESULT, gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT));
		
		TypeReference returnTypeRef = isReplace ? gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT) : gen.typeReference(TypeBinding.VOID);
		
		final MethodDeclaration callMethod = gen.method(teamDecl.compilationResult, AccPublic, returnTypeRef, methodName, arguments);
		callMethod.isMappingWrapper = AbstractMethodDeclaration.WrapperKind.CALLIN;
		
		AstEdit.addMethod(teamDecl, callMethod);
		
		MethodModel.getModel(callMethod).setStatementsGenerator(new AbstractStatementsGenerator() {
			final char[][] ARG_NAMES = new char[][]{IOTConstants.BASE, TEAMS, INDEX, CALLIN_ID, BOUND_METHOD_ID, ARGUMENTS};

			protected boolean generateStatements(AbstractMethodDeclaration methodDecl) {
				List<Statement> statements = new ArrayList<Statement>();
				SwitchStatement switchStat = new SwitchStatement();
				switchStat.expression =
					isReplace 
					? gen.arrayReference(gen.singleNameReference(CALLIN_ID), gen.singleNameReference(INDEX))
					: gen.singleNameReference(CALLIN_ID);
				
				// one case block per callin mapping:
				for (CallinMappingDeclaration callinDecl : callinDecls) 
				{
					if (callinDecl.ignoreFurtherInvestigation)
						continue;
					// one case label per bound base method:
					for (MethodSpec baseMethodSpec : callinDecl.baseMethodSpecs)
						statements.add(gen.caseStatement(gen.intLiteral(baseMethodSpec.callinID)));
					
					
					MethodSpec baseSpec = callinDecl.baseMethodSpecs[0];  // TODO(SH): check base method multiplicity
					TypeBinding baseReturn = baseSpec.resolvedType();
					boolean mayUseResultArgument =    callinDecl.callinModifier == TerminalTokens.TokenNameafter
												   && callinDecl.mappings != null
												   && baseReturn != TypeBinding.VOID;
					
					boolean isStaticRoleMethod = callinDecl.getRoleMethod().isStatic();
					ReferenceBinding roleType = callinDecl.scope.enclosingReceiverType();

					
					int nStats = isReplace ? 1 : 2; // "return rm();" or "rm(); break;" 
					if (!isStaticRoleMethod)
						nStats++; // local role 
					if (callinDecl.mappings != null)
						nStats += baseSpec.arguments.length; // one local per base arg
					if (mayUseResultArgument)
						nStats++; // local var "result"
					Statement[] blockStatements = new Statement[nStats];
					int statIdx = 0;
					
					if (mayUseResultArgument)
						// BaseReturnType result = (BaseReturnType)_OT$result;
						blockStatements[statIdx++] = gen.localVariable(RESULT, baseReturn, 
																	   gen.castExpression(gen.singleNameReference(_OT_RESULT),
																			  			  gen.typeReference(baseReturn),
																			  			  CastExpression.RAW));
					Expression receiver;
					if (!isStaticRoleMethod) {
						// RoleType local$n = this._OT$liftToRoleType((BaseType)base); 
						char[] roleVar = (LOCAL_ROLE+statements.size()).toCharArray();
						blockStatements[statIdx++] = gen.localVariable(roleVar, roleType.sourceName(),
								Lifting.liftCall(callMethod.scope,
												 gen.thisReference(),
												 gen.castExpression(gen.singleNameReference(IOTConstants.BASE), gen.typeReference(roleType.baseclass()), CastExpression.RAW),
												 callMethod.scope.getType(IOTConstants.ORG_OBJECTTEAMS_IBOUNDBASE, 3),
												 roleType,
												 false,
												 gen));
						receiver = gen.singleNameReference(roleVar);
						// private receiver needs to be casted to the class.
						if (callinDecl.getRoleMethod().isPrivate())
							receiver = gen.castExpression(receiver, gen.typeReference(roleType.getRealClass()), CastExpression.RAW);
					} else {
						receiver = gen.singleNameReference(callinDecl.getRoleMethod().declaringClass.sourceName());
					}
					
					// unpack arguments to be used by parameter mappings:
					// ArgTypeN argn = args[n]
					if (callinDecl.mappings != null) {
						TypeBinding[] baseParams = baseSpec.resolvedParameters();
						for (int i=0; i<baseSpec.arguments.length; i++) {
							Argument baseArg = baseSpec.arguments[i];
							Expression rawArg = gen.arrayReference(gen.singleNameReference(ARGUMENTS), i);
							Expression init = (baseParams[i].isBaseType())
									? gen.createUnboxing(rawArg, (BaseTypeBinding)baseParams[i]) // includes intermediate cast to boxed type
									: gen.castExpression(rawArg, gen.typeReference(baseParams[i]), CastExpression.RAW);
							blockStatements[statIdx++] = gen.localVariable(baseArg.name, AstClone.copyTypeReference(baseArg.type), init);
						}
					}
					
					// local$n.roleMethod((ArgType0)args[0], .. (ArgTypeN)args[n]);
					// local$n.roleMethod(base, teams, boundMethodId, callinIds, index, args, (ArgType0)args[0], .. (ArgTypeN)args[n]);
					// -- assemble arguments:
					TypeBinding[] roleParams = callinDecl.roleMethodSpec.resolvedParameters();
					Expression[] callArgs = new Expression [roleParams.length + (isReplace ? MethodSignatureEnhancer.ENHANCING_ARG_LEN : 0)];
					int idx = 0;
					if (isReplace)
						for (char[] argName : ARG_NAMES)
							callArgs[idx++] = gen.singleNameReference(argName);

					for (int i=0; i<roleParams.length; i++) {
						if (callinDecl.mappings == null) {
							Expression arg = gen.arrayReference(gen.singleNameReference(ARGUMENTS), i);
							if (roleParams[i].isBaseType()) {
								arg = gen.createUnboxing(arg, (BaseTypeBinding)roleParams[i]); // includes intermediate cast to boxed type
							} else {
								// Object -> MyBaseClass
								arg = gen.castExpression(arg, gen.typeReference(baseSpec.resolvedParameters()[i]), CastExpression.DO_WRAP);
								// lift?(MyBaseClass) 
								arg = gen.potentialLift(gen.thisReference(), arg, roleParams[i], isReplace/*reversible*/);
							}
							callArgs[i+idx] = arg;
						} else {
							callArgs[i+idx] = getArgument(callinDecl, (MethodDeclaration) methodDecl, callinDecl.getRoleMethod().parameters, 
											  			  i+idx, baseSpec);
						}
					}
					// -- assemble the method call:
					MessageSend roleMethodCall = gen.messageSend(receiver, callinDecl.roleMethodSpec.selector, callArgs);
					roleMethodCall.isPushedOutRoleMethodCall = true;
					if (isReplace) {
						blockStatements[statIdx] = gen.returnStatement(roleMethodCall);
					} else {
						blockStatements[statIdx++] = roleMethodCall;
						blockStatements[statIdx] = gen.breakStatement();
					}
					statements.add(gen.block(blockStatements));
				}
				if (isReplace) { 
					// default: callNext:
					Expression[] callArgs = new Expression[ARG_NAMES.length+1];
					for (int idx=0; idx < ARG_NAMES.length; idx++)
						callArgs[idx] = gen.singleNameReference(ARG_NAMES[idx]);
					callArgs[callArgs.length-1] = gen.nullLiteral(); // baseCallArguments
					statements.add(gen.caseStatement(null)); // = default
					statements.add(gen.returnStatement(
										gen.messageSend(
											gen.qualifiedThisReference(aTeam.getBinding()), 
											OT_CALL_NEXT, 
											callArgs)));
				}
				switchStat.statements = statements.toArray(new Statement[statements.size()]);
				methodDecl.statements = new Statement[] { switchStat };
				methodDecl.hasParsedStatements = true;
				// DEBUGGING:
				System.out.println("Method added: "+callMethod);
				return true;
			}
		});
	}
	
	private void generateCallNext(final List<CallinMappingDeclaration> callinDecls, final TeamModel aTeam) {
		// public Object _OT$callNext(IBoundBase baze, Team[] teams, int idx, int[] callinIds, int boundMethodId, Object[] args, Object[] baseCallArgs) 
		final TypeDeclaration teamDecl = aTeam.getAst();
		if (teamDecl == null) return;
		final AstGenerator gen = new AstGenerator(teamDecl);
		Argument[] args = new Argument[] {
				gen.argument(IOTConstants.BASE, gen.qualifiedTypeReference(IOTConstants.ORG_OBJECTTEAMS_IBOUNDBASE)),
				gen.argument(TEAMS, 			gen.qualifiedArrayTypeReference(IOTConstants.ORG_OBJECTTEAMS_ITEAM, 1)),
				gen.argument(INDEX, 			gen.typeReference(TypeBinding.INT)),
				gen.argument(CALLIN_ID, 		gen.createArrayTypeReference(TypeBinding.INT, 1)),
				gen.argument(BOUND_METHOD_ID, 	gen.typeReference(TypeBinding.INT)),
				gen.argument(ARGUMENTS, 		gen.qualifiedArrayTypeReference(TypeConstants.JAVA_LANG_OBJECT, 1)),
				gen.argument(BASE_CALL_ARGS, 	gen.qualifiedArrayTypeReference(TypeConstants.JAVA_LANG_OBJECT, 1))
		};
		// super call directly passes all these args through:
		Expression[] superArgs = new Expression[args.length];
		for (int i=0; i<args.length;i++)
			superArgs[i] = gen.singleNameReference(args[i].name);
		
		MethodDeclaration decl = gen.method(teamDecl.compilationResult, AccPublic, gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT), OT_CALL_NEXT, args);
		
		// the payload: reverse parameter mappings:
		SwitchStatement swStat = new SwitchStatement();
		swStat.expression = gen.arrayReference(gen.singleNameReference(CALLIN_ID), gen.singleNameReference(INDEX));
		List<Statement> swStatements = new ArrayList<Statement>(); 
		for (CallinMappingDeclaration mapping : callinDecls) {
			List<Statement> caseBlockStats = new ArrayList<Statement>();
			int nLabels = 0;
			for (MethodSpec baseSpec : mapping.baseMethodSpecs) 
				caseBlockStats.add(gen.caseStatement(gen.intLiteral(baseSpec.getCallinId(aTeam))));
			int nRoleArgs = mapping.getRoleMethod().getSourceParamLength();
			if (mapping.positions != null) {
				int[] poss = mapping.positions;
				nLabels = caseBlockStats.size();
				for (int i=0; i<poss.length; i++)
					// arguments[basepos] = baseCallArguments[i]
					if (poss[i] > 0)
						caseBlockStats.add(gen.assignment(gen.arrayReference(gen.singleNameReference(ARGUMENTS), poss[i]-1), // 0 represents result
														  gen.arrayReference(gen.singleNameReference(BASE_CALL_ARGS), i)));
			} else if (nRoleArgs > 0) {
				for (int i=0; i<nRoleArgs; i++) // FIXME(SH): no more args than expected by baseSpec (which determines the array size!)
					// arguments[i] = baseCallArguments[i]
					caseBlockStats.add(gen.assignment(gen.arrayReference(gen.singleNameReference(ARGUMENTS), i),
							  		   				  gen.arrayReference(gen.singleNameReference(BASE_CALL_ARGS), i)));
			}
			if (caseBlockStats.size() > nLabels) { // any action added ?
				swStatements.addAll(caseBlockStats);
				swStatements.add(gen.breakStatement());
			}
		}
		if (swStatements.size() == 0)
			return; // don't add useless method
		
		swStat.statements = swStatements.toArray(new Statement[swStatements.size()]);
		decl.statements = new Statement[] {
			swStat,
			gen.returnStatement(gen.messageSend(gen.superReference(), OT_CALL_NEXT, superArgs))
		};
		decl.hasParsedStatements = true;
		AstEdit.addMethod(teamDecl, decl);
	}
}
