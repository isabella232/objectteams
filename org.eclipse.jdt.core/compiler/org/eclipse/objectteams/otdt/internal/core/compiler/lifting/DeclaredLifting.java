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
 * $Id: DeclaredLifting.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lifting;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.LiftingTypeReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.PotentialLiftExpression;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeContainerMethod;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.BytecodeTransformer;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.StateMemento;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.ReplaceSingleNameVisitor;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstClone;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstConverter;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;

/**
 * This class handles transformations relating to declared lifting (Base as Role o).
 * A considerable part of the complication results from team constructors which
 * require lifting: such constructors must be copied and adjusted in quite tricky ways.
 *
 * @author stephan
 * @version $Id: DeclaredLifting.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class DeclaredLifting implements IOTConstants {

	public static final char[] OT_LIFT_DYNAMIC = "_OT$lift_dynamic".toCharArray(); //$NON-NLS-1$

	/** do full transformation for Declared Lifting */
	public static void transformMethodsWithDeclaredLifting(TypeDeclaration teamDecl, boolean needMethodBodies)
	{
		if (teamDecl.methods != null)
			for (AbstractMethodDeclaration method : teamDecl.methods)
				transformMethodWithDeclaredLifting(method, teamDecl, needMethodBodies);
		redefineLiftDynamicMethods(teamDecl, needMethodBodies);
	}

	/*
	        public int myMethod(MyBase as MyRole obj)
	        {
	                int i=5;
	                return obj.getResult(i+1);
	        }

	        =>

	        public int myMethod(MyBase _OT$obj)
	        {
	                MyRole obj = _OT$liftToMyRole(_OT$obj);
	                int i=5;
	                return obj.getResult(i+1);
	        }
	*/
	public static void transformMethodWithDeclaredLifting(
			AbstractMethodDeclaration method, TypeDeclaration teamDecl, boolean needMethodBody)
	{
	    if(method.arguments==null)
	    	return; // No Declared Lifting

	    // gather and count DeclaredLifting Arguments
	    List<Argument> liftingTypeArguments = new LinkedList<Argument>();

	    for (int idx = 0; idx < method.arguments.length; idx++) {
	        Argument argument = method.arguments[idx];
	        if (   argument.type != null
	        	&& argument.type.isDeclaredLifting())
	        {
	      	    liftingTypeArguments.add(argument);
	        }
	    }
	    if(liftingTypeArguments.isEmpty())
	    	return; //No DeclaredLifting found in Arguments

	    if (method.isStatic()) {
	    	method.scope.problemReporter().declaredLiftingInStaticMethod(
	    			method,
					liftingTypeArguments.get(0));
	    	return;
	    }
	    // generate lift_dynamic method if <T base B> type parameter is declared
	    if (hasBaseBoundedTypeParameters(method))
		    for (Argument arg : liftingTypeArguments) {
		    	TypeBinding roleType = teamDecl.binding.getMemberType(((LiftingTypeReference)arg.type).roleToken);
		    	if (roleType != null)
		    		genLiftDynamicMethod(teamDecl, arg.type, roleType, needMethodBody);
		    }

	    if (!needMethodBody)
	    	return; // no more details needed

	    //create modified Statements
	    Statement[] statements = new Statement[liftingTypeArguments.size()];
		int position=0;
		for (Iterator<Argument> iter = liftingTypeArguments.iterator(); iter.hasNext();)
		{
			Argument argument = iter.next();
			char[] oldArgumentName = argument.name; // save before renaming argument
			LocalDeclaration declaration = createLiftingStatement(method.scope, argument);
			if (method instanceof ConstructorDeclaration) {
				// replace references in self-call:
				ConstructorDeclaration ctor = (ConstructorDeclaration)method;
				if (ctor.constructorCall != null) {
					// replace argn with _OT$argn in ctor call:
					ReplaceSingleNameVisitor.performReplacement(
							ctor.constructorCall,
							ctor.scope,
							oldArgumentName,
							argument.name);
				}
				ctor.needsLifting = true;
			}

	    	// collect:
			statements[position++] = declaration;
		}

		// add local variable declarations to the front of statements:
		Statement[] originalStatements = method.statements;
		if (originalStatements == null)
		{
			method.setStatements(statements);
		}
		else
		{
			Statement[] newStatements = new Statement[statements.length+method.statements.length];
			System.arraycopy(statements,0,newStatements,0,statements.length);
			System.arraycopy(originalStatements,0,newStatements,statements.length,originalStatements.length);
			method.setStatements(newStatements);
		}
	}

	static boolean hasBaseBoundedTypeParameters(AbstractMethodDeclaration method) {
		TypeParameter[] typeParameters = method.typeParameters();
		if (typeParameters == null)
			return false;
		for (TypeParameter typeParameter : typeParameters)
			if (typeParameter.hasBaseBound())
				return true;
		return false;
	}

	public static void transformCatch(Scope scope, Block block, Argument argument) {
		int len = block.statements.length;
		System.arraycopy(block.statements, 0,
						 block.statements = new Statement[len+1], 1,
						 len);
		block.statements[0] = createLiftingStatement(scope, argument);
	}

	/*
	 * Creates one statement for each argument:
	 *   MyRole obj = lift?(_OT$obj);
	 */
	private static LocalDeclaration createLiftingStatement(Scope scope, Argument argument) {
		LiftingTypeReference ltr = (LiftingTypeReference) argument.type;
		int start = argument.type.sourceStart;
		int end   = argument.type.sourceEnd;
		AstGenerator gen = new AstGenerator(start, end);

		// names:
		char[] oldName = argument.name;
		char[] newName = CharOperation.concat(OT_DOLLAR_NAME, oldName);
		argument.updateName(newName);

		// MyRole obj = _OT$liftToMyRole(_OT$obj); (via PotentialLiftExpression)
		TypeBinding roleType = scope.getType(ltr.roleToken);
		ReferenceBinding roleRef = (ReferenceBinding)roleType;
		//    using a PotentialLiftExpression allows us to defer type resolution.
		Expression liftCall = null;
		if (   roleRef.isValidBinding()
			&& roleRef.isRole()
			&& (roleRef.tagBits & (TagBits.HierarchyHasProblems | TagBits.BaseclassHasProblems)) == 0)
		{
			Expression receiverTeam = ThisReference.implicitThis();
			ReferenceBinding teamBinding = roleRef.enclosingType();
			if (teamBinding != scope.enclosingSourceType())
				receiverTeam = gen.qualifiedThisReference(teamBinding);
			if (roleRef.baseclass() == null) {
				// static adjustment (OTJLD 2.3.2(a)):
				ReferenceBinding baseType = (ReferenceBinding)scope.getType(ltr.baseTokens, ltr.baseTokens.length);
				roleRef = (ReferenceBinding)TeamModel.getRoleToLiftTo(scope, baseType, roleRef, true, ltr);
				if (baseType.isTypeVariable() && roleRef == null)
					roleRef = (ReferenceBinding)roleType; // fall back to the declared type
			}
			if (roleRef != null) {
				if (ltr.baseReference.dimensions() > 0)
					roleType = scope.createArrayType(roleRef, ltr.baseReference.dimensions());
				liftCall = new PotentialLiftExpression(
					receiverTeam,
					gen.singleNameReference(newName),
					ltr.roleReference);
			}
			// errors are reported by LiftingTypeReference.resolveType().
		}
		//    assemble the variable decl:
		LocalDeclaration declaration = gen.localVariable(
				oldName,
				AstClone.copyTypeReference(ltr.roleReference),
				liftCall);

		// store local declaration in lifting type,
		// which may need to cancel some generated AST after resolve.
		// (cf. LiftingTypeReference.invalidate())
		ltr.fakedArgument = declaration;

		return declaration;
	}

	/** If a team ctor uses role type args, prepare the ctor for
	 *  self calls involving declared lifting.
	 */
	public static void prepareArgLifting(TypeDeclaration teamDecl) {
		if (teamDecl.methods != null)
	    	for (int i = 0; i < teamDecl.methods.length; i++) {
				if (teamDecl.methods[i].isConstructor()) {
					ConstructorDeclaration ctor = (ConstructorDeclaration)teamDecl.methods[i];
					prepareArgLifting(teamDecl, ctor);
				}
			}
	}

	private static void prepareArgLifting(TypeDeclaration teamDecl, ConstructorDeclaration ctor)
	{
	    if(ctor.arguments==null || ctor.ignoreFurtherInvestigation)
	    	return; // nothing to do

	    List<LocalDeclaration> localVariables = new LinkedList<LocalDeclaration>();

	    boolean selfCallIsChanged = false;
	    boolean chainArgAdded = false;
	    for (int idx = 0; idx < ctor.arguments.length; idx++) {

	        Argument argument = ctor.arguments[idx];
	        TypeBinding param = ctor.binding.parameters[idx];

	        // TODO(SH): array of roles?
	        // don't use RoleTypeBinding.isRoleWithoutExplicitAnchor, because
	        // type wrapping in signature has not been done yet.
	        if (	isRoleOf(param, teamDecl.binding)
				&& !ctor.isTSuper)
	        {
				int start = argument.type.sourceStart;
				int end   = argument.type.sourceEnd;
	        	AstGenerator gen = new AstGenerator(start, end);

	        	if (!chainArgAdded) { // do this only once:
		        	// reserve space for a marker arg that could be added during copying:
		        	LocalDeclaration chainVar = gen.localVariable(
		        			"_OT$chainArg".toCharArray(), //$NON-NLS-1$
							teamDecl.scope.getJavaLangObject(),
							null);
		        	chainVar.isPlaceHolder = true;
		        	localVariables.add(chainVar);
		        	chainArgAdded = true;
	        	}

	        	// create local variable similar as for declared lifting
	        	char[] oldName = argument.name;
				char[] newName = CharOperation.concat(OT_DOLLAR_NAME, oldName);
				// cast might be needed if this collides with signature weakening
				// (cf. CopyInheritance.weakenSignature)
				Expression rhs = gen.castExpression(
						gen.singleNameReference(newName),
						gen.typeReference(((ReferenceBinding)param).getRealType()), // cast is safe here, but enclosing if is too strict!
						CastExpression.RAW);

	        	argument.updateName(newName);

	        	// only potentially need lifting in a constructor (once copied)
	        	LocalDeclaration declaration = gen.localVariable(
	        			oldName,
						AstClone.copyTypeReference(argument.type),
						rhs); // assign real argument
	        	declaration.isPreparingForLifting = true;

	        	localVariables.add(declaration);

	        	// replace references in super-call:
	        	if (ctor.constructorCall != null) {
	        		// replace argn with _OT$argn in ctor call:
	        		selfCallIsChanged = ReplaceSingleNameVisitor.performReplacement(
	        				ctor.constructorCall,
	    					ctor.scope,
	    					oldName,
	    					newName);
	        	}
	        }

	        if (selfCallIsChanged) {
	    		// add a marker argument to this call:
				ExplicitConstructorCall call = ctor.constructorCall;
				call.accessMode = ExplicitConstructorCall.This;
		        AstGenerator gen = new AstGenerator(call.sourceStart, call.sourceEnd);
		        int numArgs = 0;
				if (call.arguments == null) {
		        	call.arguments = new Expression[1];
		        } else {
		        	numArgs = call.arguments.length;
		        	System.arraycopy(
						call.arguments, 0,
						call.arguments = new Expression[numArgs+1], 0,
						numArgs);
		        }
	        	call.arguments[numArgs] = TSuperHelper.createMarkerArgExpr(
						teamDecl.binding.superclass, gen);
				MethodModel.getModel(ctor); // side effect.
	        }
		}
	    if (!localVariables.isEmpty()) {
	    	// add local variable declartions to the front of statements
	    	int statsLen = (ctor.statements == null) ? 0 : ctor.statements.length;
			Statement[] statements = new Statement[localVariables.size()+statsLen];
	    	int i=0;
	    	for (Iterator<LocalDeclaration> iter = localVariables.iterator(); iter.hasNext();) {
				statements[i++] = iter.next();
			}
	    	if (ctor.statements != null)
		    	System.arraycopy(
		    			ctor.statements, 0,
						statements, i,
						statsLen);
	    	ctor.setStatements(statements);
	    }
	}

	private static boolean isRoleOf(TypeBinding type, ReferenceBinding enclosingTeam) {
		if (type instanceof MemberTypeBinding)
			return ((MemberTypeBinding)type).enclosingType() == enclosingTeam;
		if (type instanceof RoleTypeBinding)
			return !((RoleTypeBinding)type).hasExplicitAnchor();
		return false;
	}
	/**
	 * Copy a team constructor such that the copy is suitable for chaining
	 * previous super calls through a chain of tsuper-calls. This enables
	 * a team to insert lifting calls at all required locations.
	 *
	 * In a subteam where the role is bound, the role parameter has to be replaced
	 * with the bound base class (signature & selfcall).
	 *
	 * @param teamDecl      this is where everything happens
	 * @param superTeamCtor constructor to be copied
	 * @param providedArgs  this are the types of arguments passed by the ExplicitConstructorCall
	 * @param needsLifting  has the context (initiating constructor decl) declared lifting?
	 */
	public static MethodBinding copyTeamConstructorForDeclaredLifting (
										Scope           scope,
										MethodBinding   superTeamCtor,
										TypeBinding[]   providedArgs,
										boolean         needsLifting)
	{
		TypeDeclaration teamDecl = scope.referenceType();
		AstGenerator gen = new AstGenerator(scope.methodScope().referenceMethod().sourceStart,
											scope.methodScope().referenceMethod().sourceEnd);

		if (scope.isOrgObjectteamsTeam(superTeamCtor.declaringClass))
			return maybeCreateTurningCtor(teamDecl, superTeamCtor, gen);

		// Parameter list with marker arg
		TypeBinding[] providedWithMarker = providedArgs;
		if (   providedArgs.length == 0
			|| !TSuperHelper.isMarkerInterface(providedArgs[providedArgs.length-1]))
		{
			// need to extend the param list
			TypeBinding lastParam = null;
			if (superTeamCtor.parameters.length > 0)
				lastParam = superTeamCtor.parameters[superTeamCtor.parameters.length-1];
			TypeBinding marker =
				(   lastParam != null
				 && TSuperHelper.isMarkerInterface(lastParam)) ? // prefer existing marker
					lastParam
				:
					TSuperHelper.getMarkerInterface(scope, superTeamCtor.declaringClass);

			providedWithMarker = AstEdit.extendTypeArray(providedArgs, marker);
		}


		// Inner self call:
		MethodBinding selfcall = null;
		AbstractMethodDeclaration src = superTeamCtor.sourceMethod();
		if (src != null) {
			if (src.ignoreFurtherInvestigation)
				return null; // can't create ctor

			// src means: look in the AST
			ConstructorDeclaration srcCtor = (ConstructorDeclaration)src;
			if (src.isCopied) {
				if (src.model != null)
					selfcall = src.model.adjustedSelfcall;					
			} else if (srcCtor.constructorCall != null) {
				Dependencies.ensureTeamState(superTeamCtor.declaringClass.getTeamModel(),
						                     ITranslationStates.STATE_RESOLVED);
				selfcall = srcCtor.constructorCall.binding;
			} else {
				if (!src.scope.compilationUnitScope().referenceContext.parseMethodBodies) {
					// that's why we have no constructorCall: CU is on diet (parse); use a fake (see Trac #142):
					MethodBinding result = new MethodBinding(ClassFileConstants.AccPublic, providedArgs, null, superTeamCtor.declaringClass);
					return result;
				}
				selfcall = superTeamCtor.declaringClass.superclass().getExactConstructor(Binding.NO_PARAMETERS);
			}
		} else if (superTeamCtor.bytecodeMissing || superTeamCtor.model == null) {
			return null; // can't create ctor.
		} else {
			// no src means: peek the byte code:
			selfcall = (new BytecodeTransformer()).peekConstructorCall(
				teamDecl.getTeamModel(),
				superTeamCtor.model,
				scope.environment());
		}
		MethodBinding adjustedSelfcall = null;
		if (selfcall == null) {
			if (!superTeamCtor.bytecodeMissing) // may have been set by peedConstructorCall above
				scope.problemReporter().unsupportedRoleDataflow(scope.methodScope().referenceMethod(), superTeamCtor);
			// be sure to continue below, because client needs the new ctor.
		} else {

			TypeBinding[] selfCallProvidedArgs = providedWithMarker;// TODO(SH): fetch argument order from bytecode

			adjustedSelfcall = maybeCopyCtorForSelfCall(scope, selfcall, selfCallProvidedArgs, needsLifting, gen);

			// Check for presence of desired constructor:
			MethodBinding existingConstructor = teamDecl.binding.getExactConstructor(providedWithMarker);
			if (existingConstructor != null) {
				if (adjustedSelfcall != null)
				{
					MethodModel model = MethodModel.getModel(existingConstructor);
					model.adjustSelfcall(selfcall, adjustedSelfcall);
				}
				return existingConstructor;
			}
		}

		// Create new constructor:
		ConstructorDeclaration newCtor = gen.constructor(
											teamDecl.compilationResult,
											ClassFileConstants.AccPublic,
											teamDecl.name,
											AstConverter.createArgumentsFromParameters(providedWithMarker, gen));
		
		// adjust argument-anchored types in this signature:
		for (int i=0; i<superTeamCtor.parameters.length; i++)
			if (RoleTypeBinding.isRoleWithExplicitAnchor(superTeamCtor.parameters[i])) {
				RoleTypeBinding requiredRTB = (RoleTypeBinding) superTeamCtor.parameters[i];
				if (requiredRTB._argumentPosition > -1) {
					// we have an arg-anchored type, adjust the anchor within this signature:
					Argument newArgument = newCtor.arguments[requiredRTB._argumentPosition];           // argument positions from the declared (super) ctor.
					newArgument.modifiers |= ClassFileConstants.AccFinal;
					newArgument.name = ((RoleTypeBinding) providedArgs[i])._teamAnchor.internalName(); // argument names from the provided types/arguments
				}
			}

		newCtor.isTSuper = true; // ??
		newCtor.isCopied = true;
		AstEdit.addMethod(teamDecl, newCtor);  // incl. resolve
	    MethodModel model = MethodModel.getModel(newCtor); // already connect binding.
	    if (needsLifting)
	    	model.liftedParams = superTeamCtor.parameters; // instructions for BytecodeTransformer
	    if (adjustedSelfcall != null)
	    	model.adjustSelfcall(selfcall, adjustedSelfcall); // be it only strengthening
	    newCtor.binding.setCopyInheritanceSrc(superTeamCtor);
	    newCtor.binding.copiedInContext = teamDecl.binding;
	    newCtor.sourceMethodBinding = superTeamCtor;

		return newCtor.binding;
	}

	/**
	 * A constructor being copied contains a self call which might require
	 * transitive copying of more team constructors.
	 * @param teamDecl
	 * @param selfcall
	 * @param providedArgs arguments provided to the selfcall
	 * @param needsLifting has the context required lifting of any args? pass this info down.
	 * @param gen
	 */
	private static MethodBinding maybeCopyCtorForSelfCall(
				Scope           scope,
				MethodBinding   selfcall,
				TypeBinding[]   providedArgs,
				boolean         needsLifting,
				AstGenerator    gen)
	{
		TypeBinding[] parameters = selfcall.parameters;
		assert providedArgs.length >= parameters.length;
		if (providedArgs.length > parameters.length)
			System.arraycopy (
					providedArgs, 0,
					providedArgs = new TypeBinding[parameters.length], 0,
					parameters.length);
		boolean hasProblematicArg = false;
		for (int i = 0; i < parameters.length; i++) {
			if (!providedArgs[i].isCompatibleWith(parameters[i])) {
				if (TSuperHelper.isMarkerInterface(parameters[i]))
					continue;
				hasProblematicArg = true;
				TypeBinding roleType = TeamModel.getRoleToLiftTo(
						scope, providedArgs[i], parameters[i], true, scope.referenceType().superclass);
				if (roleType != null)
				{
					// compatibility only through lifting, need to copy the constructor for this selfcall
					return copyTeamConstructorForDeclaredLifting(
							scope, selfcall, mergeSelfcallArgs(providedArgs, parameters), needsLifting);
				}
			} else if (RoleTypeBinding.isRoleWithoutExplicitAnchor(parameters[i])) {
				hasProblematicArg = true;
			}
		}
		if (selfcall.declaringClass.isTeam() && !hasProblematicArg) // don't copy from Object!
			return maybeCreateTurningCtor(scope.referenceType(), selfcall, gen);
		return null;
	}

	/**
	 * Prefer marker args from the super method to call,
	 * everything else should be taken from what is actually provided to the self call.
	 * @param thisProvidedArgs  provided here
	 * @param superExpectedArgs expected by the super method
	 * @return merged array of references to existing type bindings.
	 */
	private static TypeBinding[] mergeSelfcallArgs(
										TypeBinding[] thisProvidedArgs,
										TypeBinding[] superExpectedArgs)
	{
		TypeBinding[] result = new TypeBinding[thisProvidedArgs.length];
		for (int i = 0; i < result.length; i++) {
			if (TSuperHelper.isMarkerInterface(superExpectedArgs[i]))
				result[i] = superExpectedArgs[i];
			else
				result[i] = thisProvidedArgs[i];
		}
		return result;
	}

	/**
	 * At a specific point the constructor chain turns from this calls (w/ marker args)
	 * to real super calls. Create a constructor which does nothing but this turning.
	 * @param teamDecl
	 * @param superTeamCtor
	 * @param gen
	 *
	 * @return a binding if a new method has been generated, else null.
	 */
	public static MethodBinding maybeCreateTurningCtor(TypeDeclaration teamDecl,
														MethodBinding   superTeamCtor,
														AstGenerator    gen)
	{
		// constructor chains are created during resolve, so super team must be resolved.
		Dependencies.ensureTeamState(teamDecl.binding.superclass().getTeamModel(), ITranslationStates.STATE_RESOLVED);

		boolean hasMarkerArg =
			(   superTeamCtor.parameters.length > 0
			 && TSuperHelper.isMarkerInterface(
					superTeamCtor.parameters[superTeamCtor.parameters.length-1]));

		TypeBinding[] newParams = superTeamCtor.parameters;
		if (!hasMarkerArg) {
			TypeBinding markerType = TSuperHelper.getMarkerInterface(teamDecl.scope, superTeamCtor.declaringClass);
			newParams = AstEdit.extendTypeArray(superTeamCtor.parameters, markerType);
		}

		// do we need to work at all?
		MethodBinding existingCtor = teamDecl.binding.getExactConstructor(newParams);
		if (existingCtor != null)
			return existingCtor;

		// start creating:
		Argument[] arguments = AstConverter.createArgumentsFromParameters(superTeamCtor.parameters, gen);
		ConstructorDeclaration newCtor = gen.constructor(teamDecl.compilationResult,
														 ClassFileConstants.AccPublic,
														 teamDecl.name,
														 arguments);
		// arguments
		if (!hasMarkerArg) {
			TSuperHelper.addMarkerArg(newCtor, superTeamCtor.declaringClass);
			arguments = newCtor.arguments;
		}
		// constructor call
		newCtor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.Super);
		int length = superTeamCtor.parameters.length;// as many as the target (super) expects.
		Expression[] selfcallArgs = new Expression[length];
		for (int i = 0; i < length; i++) {
			selfcallArgs[i] = gen.singleNameReference(arguments[i].name);
		}
		newCtor.constructorCall.arguments = selfcallArgs;
		// empty body
		newCtor.setStatements(new Statement[0]);

		newCtor.isTSuper = true; // no matter if new or old marker arg
		AstEdit.addMethod(teamDecl, newCtor);

		return newCtor.binding;
	}

	/** If a super team has lift_dynamic methods, redefine them in the current team to match its roles. */
	public static void redefineLiftDynamicMethods(TypeDeclaration teamDecl, boolean needMethodBodies) {
		// if super team has a lift_dynamic method, re-generate it to match the roles of this team:
		ReferenceBinding superTeam = teamDecl.binding.superclass();
		if (superTeam != null && !CharOperation.equals(IOTConstants.ORG_OBJECTTEAMS_TEAM, superTeam.compoundName)) {
			// ensure super team has its lift_dynamic methods generated:
			Dependencies.ensureBindingState(superTeam, ITranslationStates.STATE_ROLE_HIERARCHY_ANALYZED);
			ASTNode location = teamDecl.superclass != null ? teamDecl.superclass : teamDecl;
			for (MethodBinding method : superTeam.methods())
				if (CharOperation.prefixEquals(OT_LIFT_DYNAMIC, method.selector)) {
					ReferenceBinding roleType = (ReferenceBinding) TeamModel.strengthenRoleType(teamDecl.binding, method.returnType);
					DeclaredLifting.genLiftDynamicMethod(teamDecl, location, roleType, needMethodBodies);
				}
		}
	}

	/**
	 * This method generates a lift method suitable for lifting an argument typed to a base-bounded type variable (B base R).
	 * The generated method will be added to the team type and resolved if suitable (team already in state >= resolving).
	 * @param teamDecl
	 * @param roleType
	 * @param gen
	 */
	public static void genLiftDynamicMethod(TypeDeclaration teamDecl, ASTNode ref, TypeBinding roleType, boolean needMethodBody)
	{
		char[] dynamicLiftingSelector = dynamicLiftSelector(roleType);
		if (teamDecl.binding.getMethods(dynamicLiftingSelector) != Binding.NO_METHODS)
			return; // already present
		
		if (roleType.isArrayType()) {
			teamDecl.scope.problemReporter().missingImplementation(ref, "Generic lifting of array not yet implemented.");
		} else if (roleType.isBaseType()) {
			teamDecl.scope.problemReporter().primitiveTypeNotAllowedForLifting(teamDecl, ref, roleType);
		} else { 
			// reference type
			AstGenerator gen = new AstGenerator(ref);
			MethodDeclaration dynLiftMeth = gen.method(teamDecl.compilationResult(),
													   ClassFileConstants.AccProtected,
													   roleType.erasure(),
													   dynamicLiftingSelector,
													   new Argument[] {
															gen.argument(IOTConstants.BASE, gen.qualifiedTypeReference(TypeContainerMethod.JAVA_LANG_OBJECT))
													   });
	
			dynLiftMeth.statements = new Statement[] { gen.emptyStatement() }; // to be replaced below
	
			dynLiftMeth.hasParsedStatements = true;
			AstEdit.addMethod(teamDecl, dynLiftMeth);
			dynLiftMeth.binding.returnType = ((ReferenceBinding)roleType).getRealType().erasure(); // force erased type after signature resolve
			if (needMethodBody) {
				dynLiftMeth.statements[0] = DeclaredLifting.generateDynamicSwitch(dynLiftMeth.scope, (ReferenceBinding)roleType, gen);
				if (StateMemento.hasMethodResolveStarted(teamDecl.binding))
					dynLiftMeth.statements[0].resolve(dynLiftMeth.scope);
			}
		}
	}

	/** Answer the selector for a dynamic lift method returning `roleType`. */
	public static char[] dynamicLiftSelector(TypeBinding roleType) {
		return CharOperation.concat(OT_LIFT_DYNAMIC, roleType.sourceName());
	}

	/* Generate the if-cascade for dynamic lifting depending on the argument's dynamic type. */
	static Statement generateDynamicSwitch(BlockScope scope, ReferenceBinding roleType, AstGenerator gen) {
		ReferenceBinding[] boundDescendants = roleType.roleModel.getBoundDescendants();
		if (boundDescendants.length == 0)
			return gen.throwStatement(
						gen.allocation(
							gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_ERROR),
							new Expression[] {
								gen.stringLiteral(
									CharOperation.concat(
										"Lifting impossible, role has no bound descendants: ".toCharArray(), //$NON-NLS-1$
										roleType.readableName()))
							}));
		IfStatement ifStat = null;
		IfStatement current = null;
		LookupEnvironment environment = scope.compilationUnitScope().environment;
		for (int i=0; i<boundDescendants.length; i++) {
			TypeBinding boundBase = boundDescendants[i].baseclass();
			if (RoleTypeBinding.isRoleWithExplicitAnchor(boundBase)) {
				if (boundBase.isParameterizedType()) {
					// tricky case: need to discard type parameters, but retain/recreate the role type wrapping:
					RoleTypeBinding baseRole = (RoleTypeBinding)boundBase;
					boundBase = environment.createParameterizedType(baseRole._declaredRoleType, 
																 	null, 					// erase type parameters 
																	baseRole._teamAnchor, 	// but retain anchor
																	-1,					// valueParamPosition 
																	baseRole.enclosingType());
				}
				// only RTB but not parameterized: leave unchanged.
			} else {
				boundBase = boundBase.erasure();
			}
			IfStatement newIf = gen.ifStatement(
									gen.instanceOfExpression(
										gen.singleNameReference(IOTConstants.BASE),
										gen.typeReference(boundBase)),
									gen.returnStatement(
										Lifting.liftCall(
											scope,
											gen.thisReference(),
											gen.castExpression(
												gen.singleNameReference(IOTConstants.BASE),
												gen.typeReference(boundBase),
												CastExpression.DO_WRAP),
											boundBase,
											boundDescendants[i],
											false/*needLowering*/)),
									null);
			if (ifStat == null)
				ifStat = newIf;					// toplevel "if"
			else
				current.elseStatement = newIf;  // nest within existing "if else { /* here */ }"
			current = newIf;
		}
		// final branch:
		current.elseStatement = Lifting.genLiftingFailedException(IOTConstants.BASE, roleType, gen);
		return ifStat;
	}
}
