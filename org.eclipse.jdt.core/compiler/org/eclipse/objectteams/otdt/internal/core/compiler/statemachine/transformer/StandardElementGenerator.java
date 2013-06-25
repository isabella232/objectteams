/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2013 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.SingleValueAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.StateMemento;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.TreeNode;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.TThisBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.FieldModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.*;
import static org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers.*;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.*;

/**
 * This class inserts standard elements into role and team classes.
 *
 * For bound and unbound roles:
 *  	Team _OT$getTeam();
 * 		protected <role> _OT$castTo$<role> (Object _OT$arg1)
 *		protected <role>[].. _OT$castTo$<role>$<dims> (Object[].. _OT$arg1)  // OPTIONALLY
 *
 * For bound roles only:
 *      <baseclass> _OT$base;      // field
 *      <baseclass> _OT$getBase(); // method (abstract or implemented)
 *		"PlayedBy" Attribute (byte code)
 *
 * Some more elements have been (partly) handled here in earlier versions:
 *   role caches -> LiftingEnvironment
 *   role class literal -> RoleClassLiteralAccess
 *
 * Special treatment of _OT$base when refining inherited playedBy:
 * (see 1.1.26-otjld-nesting-and-layering-2):
 * + do NOT create a new field declaration to avoid duplicates
 * + BUT create a new field binding mark with TagBits.IsFakedField
 *   This field binding allows to correctly type-check <@base> types but it ...
 *   - is filtered out in ReferenceBinding.fieldCount()
 *   - does not report uninitializedBlankFinalField
 *   - is not copied in CopyInheritance.copyField
 *   - generates getfield of real inherited field plus a checkcast to compensate for weakening
 *
 * @author stephan
 */
public class StandardElementGenerator {

//    // need a fully-split (no $compound) name:
//	private static final char[][] ORG_OBJECTTEAMS_TEAM_DOT_CONFINED = new char[][]{
//									"org".toCharArray(), //$NON-NLS-1$
//									"objectteams".toCharArray(), //$NON-NLS-1$
//									"Team".toCharArray(), //$NON-NLS-1$
//									"Confined".toCharArray()  //$NON-NLS-1$
//							};
	private static final String OUTER_THIS_PREFIX = "this$"; //$NON-NLS-1$
	private static final char[] LENGTH = "length".toCharArray(); //$NON-NLS-1$

	 /**
	 * For each role (bound or unbound) generate
	 * 		public org.objectteams.Team _OT$getTeam()
	 * If roleType is an interface generate the abstract interface method.
	 *
	 * @param roleType the role to operate on
	 * @return the binding of the created method or null
	 */
	public static @Nullable MethodBinding createGetTeamMethod(TypeDeclaration roleType)
	{
		MethodBinding existingMethod = TypeAnalyzer.findMethod(
				roleType.initializerScope,
				roleType.binding,
				_OT_GETTEAM,
				Binding.NO_PARAMETERS);
		if (existingMethod != null && existingMethod.isValidBinding()) {
			// is already covered by inheritance (copy or extends)
			if (roleType.isInterface() == existingMethod.isAbstract())
				return null;
		}


		AstGenerator gen = roleType.baseclass != null ?
			new AstGenerator(roleType.baseclass.sourceStart, roleType.baseclass.sourceEnd) :
			new AstGenerator(roleType.sourceStart, roleType.sourceEnd);

		ReferenceBinding teamType = null;
		LookupEnvironment environment = roleType.scope.environment();
		try {
			environment.missingClassFileLocation = roleType;
			teamType = roleType.scope.getOrgObjectteamsITeam();
		} finally {
			environment.missingClassFileLocation = null;
		}

        int flags = AccPublic;
        if (roleType.isInterface())
        	flags |= AccAbstract|AccSemicolonBody;

		MethodDeclaration getTeam = gen.method(roleType.compilationResult, flags, teamType, _OT_GETTEAM, null);
		AstEdit.addMethod(roleType, getTeam);

		if ((flags & AccSemicolonBody) == 0)
		{
			int depth = roleType.binding.depth() - 1;
			getTeam.setStatements(new Statement[]{
				gen.returnStatement(
					gen.fieldReference(
						gen.thisReference(),
						(OUTER_THIS_PREFIX+depth).toCharArray()
					))
			});
			if (StateMemento.hasMethodResolveStarted(roleType.binding))
				getTeam.resolveStatements();
		}
		return getTeam.binding;
	}


	/**
	 * API for Dependencies:
	 * Create a role cast method.
	 */
	public static void createCastMethod(TeamModel teamModel, TypeDeclaration roleType, int dimensions) {
		getCastMethod(teamModel, roleType.binding, roleType.scope, dimensions, false, roleType.sourceStart, roleType.sourceEnd);
	}

	/**
	 * Retreive (or create) a team level method used for casting an expression to a role.
	 * After casting also check the containingInstance against the current team.
	 * @param teamModel
	 * @param roleType
	 * @param scope (used only for lookup of j.l.Object)
	 * @param searchSuper  should super classes of teamClass be search, too?
	 * @param sourceStart
	 * @param sourceEnd
	 * @return the method
	 */
	public static MethodBinding getCastMethod(
			TeamModel        teamModel,
			ReferenceBinding roleType,
			Scope            scope,
			int              dimensions,
			boolean          searchSuper,
			int sourceStart,
			int sourceEnd)
	{
		/*
		 * protected <role> _OT$castTo$<role> (Object _OT$arg1) {
		 *    if (_OT$arg1 == null) return null;
		 * 	  __OT__<role> role = (__OT__<role>) _OT$arg1;
		 *    if (role._OT$getTeam() != this)
		 * 		  throw new RuntimeException();
		 *    return role;
		 * }
		 *OR FOR ARRAY:
		 * protected <role>[].. _OT$castTo$<role>$<dims> (Object[].. _OT$arg1) {
		 *    if (_OT$arg1 == null) return null;
		 * 	  <role>[].. role = (<role>[]..) _OT$arg1;
		 *    if (role.length > 0 && ((__OT__<role>)role[0])._OT$getTeam() != this) // TODO(SH): extract through several dims
		 * 		  throw new RuntimeException();
		 *    return role;
		 * }
		 * NOTE(SH): it suffices to check team equivalence for one element, since at this point it
		 *           must already be a role-array, which cannot mix roles from different teams ;-)
		 */
		boolean shouldWeaken = (teamModel.getState() >= ITranslationStates.STATE_TYPES_ADJUSTED); // weakening for other methods already done?
		MethodBinding superMethod = null;
		
		roleType = roleType.getRealType();
		char[] methodName = CharOperation.concat(CAST_PREFIX, roleType.sourceName());
		if (dimensions > 0)
			methodName = CharOperation.concat(methodName, String.valueOf(dimensions).toCharArray(), '$');
		ReferenceBinding teamBinding = teamModel.getBinding();
		while (teamBinding != null) {
			MethodBinding[] methods = teamBinding.getMethods(methodName);
			if (methods != null && methods.length == 1) {
				if (methods[0].declaringClass == teamModel.getBinding() || searchSuper)
					return methods[0];
				// go ahead and generate a new method, but use superMethod for weakening after generating:
				superMethod = methods[0];
				break;
			}
			if (!searchSuper && !shouldWeaken)
				break;
			teamBinding = teamBinding.superclass();
		}

		TypeDeclaration teamClass = teamModel.getAst();
		if (teamClass == null) {
			if (true) {// FIXME(SH): team has error?
				MethodBinding castMethod = new MethodBinding(AccPublic,
										 methodName,
										 roleType,
										 new TypeBinding[]{scope.getJavaLangObject()},
										 null,
										 teamModel.getBinding());
				teamModel.getBinding().addMethod(castMethod);
				return castMethod;
			}
			throw new InternalCompilerError("Required cast method not found."); //$NON-NLS-1$
		}

		AstGenerator gen = new AstGenerator(sourceStart, sourceEnd);

		// --- method header ---
		int modifiers = 0;
		boolean clearPrivateModifier = false;
		if (roleType.isPublic()) {
			modifiers = AccPublic;
		} else {
			// this weird combination allows to return a non-public role and will
			// grant access across packages in the byte code.
			modifiers = AccProtected;
			clearPrivateModifier = true;
			// See also BinaryTypeBinding.resolveTypesFor(MethodBinding) where the Protected flag is restored.
		}
		// args
		char[] argName = OT_DOLLAR_ARG.toCharArray();

		// find the appropriate top-level-super-type:
		ReferenceBinding exprType = teamClass.scope.getJavaLangObject();
//		if (!roleType.isStrictlyCompatibleWith(exprType)) {
//			exprType = (ReferenceBinding)teamClass.scope.getType(ORG_OBJECTTEAMS_ICONFINED, 3);
//			if (!roleType.isCompatibleWith(exprType))
//				exprType = (ReferenceBinding)teamClass.scope.getType(
//						ORG_OBJECTTEAMS_TEAM_DOT_CONFINED,
//						4);
//		}
		TypeReference exprTypeRef = gen.typeReference(exprType);

		MethodDeclaration castMethod = gen.method(teamClass.compilationResult(),
						modifiers,
						gen.createArrayTypeReference(roleType, dimensions),
						methodName,
						new Argument[]{
							gen.argument(argName, exprTypeRef)
						});
		// see org.eclipse.objectteams.otdt.tests.otjld.regression.ReportedBugs.testB11_sh15():
		// pre-set return type to prevent problems with resolving lateron
		TypeBinding returnType = dimensions == 0 ? roleType : scope.environment().createArrayType(roleType, dimensions);
		castMethod.returnType.resolvedType = RoleTypeCreator.maybeWrapUnqualifiedRoleType(returnType, teamBinding);

		// <role> role = (<role>)_OT$arg;
		TypeReference arrayCastType = gen.createArrayTypeReference(roleType, dimensions);
		LocalDeclaration castedLocalVar = gen.localVariable(
				ROLE,
				arrayCastType,
				gen.castExpression(
						gen.singleNameReference(argName),
						arrayCastType,
						CastExpression.RAW));

		//STATEMENTS:
		// if (_OT$arg1 == null) return null;
		//AND
		//   if (role._OT$getTeam() != this)
		//      throw new RuntimeException();
		//  OR
		//   if (role.length > 0 && ((<roleClass>)role[0])._OT$getTeam() != this)
		//      throw new RuntimeException();

		Statement nullCheck = gen.ifStatement(
									gen.nullCheck(gen.singleNameReference(argName)),
									gen.returnStatement(gen.nullLiteral()));

		Expression teamCheckCondition;
		teamCheckCondition = genTeamCheck(gen,
										  OperatorIds.NOT_EQUAL,
										  gen.singleNameReference(ROLE),
										  gen.thisReference(),
										  dimensions);

		if (dimensions > 0)
			teamCheckCondition = gen.setPos(new AND_AND_Expression(
					gen.equalExpression(
							gen.qualifiedNameReference(
									new char[][]{ROLE, LENGTH}),
							gen.intLiteral(0),
							OperatorIds.GREATER),
					teamCheckCondition,
					OperatorIds.AND_AND));

		// here we go:
		castMethod.setStatements(new Statement[] {
			nullCheck,
			castedLocalVar,
			gen.ifStatement(
				teamCheckCondition,
			    gen.throwStatement(
			    	gen.allocation(
				        gen.qualifiedTypeReference(ROLE_CAST_EXCEPTION),
						new Expression[0]))),
			// return role;
			gen.returnStatement(
					gen.singleNameReference(ROLE))
		});
		castMethod.isGenerated = true;
		AstEdit.addGeneratedMethod(teamClass, castMethod);
		if (clearPrivateModifier)
			castMethod.binding.tagBits = TagBits.ClearPrivateModifier;
		
		if (superMethod != null)
			CopyInheritance.weakenSignature(castMethod, superMethod);
		return castMethod.binding;
	}

	public static boolean isCastToMethod(char[] selector) {
		return CharOperation.prefixEquals(CAST_PREFIX, selector);
	}
	/**
	 * Create the code that combines team anchor comparison and a regulare instanceof.
	 * @param exprType
	 * @param castType
	 */
	public static Expression createRoleInstanceOfCheck(
			BlockScope scope, InstanceOfExpression expr, ReferenceBinding exprType, DependentTypeBinding castType) {

		AstGenerator gen = new AstGenerator(expr.sourceStart, expr.sourceEnd);
		Expression teamInstanceComparison;
		if (RoleTypeBinding.isRoleWithExplicitAnchor(exprType)) {
			teamInstanceComparison = createAnchorEqualCheck
				(scope, (RoleTypeBinding)exprType, castType, expr.sourceStart, expr.sourceEnd);
		} else {
			 BinaryExpression teamCheck = genTeamCheck(gen,
					OperatorIds.EQUAL_EQUAL,
					gen.resolvedCastExpression(
							expr.expression,  // FIXME(SH): avoid double evaluation of expression!
											  // but how can we store a value without a statement?
											  // use a byte-code hack (cf. Lowering.{Pushing,Pop}Expression!)
							castType.getRealType(),
							CastExpression.RAW),
					createTeamAnchorReference(
							scope,
							castType,
							gen),
					expr.type.dimensions());

			// manually resolve:
			MessageSend msg = (MessageSend)teamCheck.left;
			msg.binding = castType.getMethod(scope, IOTConstants._OT_GETTEAM);
			if (msg.binding == null) {
				// add a fake method, assuming it was not created due to errors:
				msg.binding = new MethodBinding(AccPublic,
												IOTConstants._OT_GETTEAM,
												scope.getOrgObjectteamsITeam(),
												Binding.NO_PARAMETERS,
												Binding.NO_EXCEPTIONS,
												castType);
				assert castType.roleModel.isIgnoreFurtherInvestigation();
			}
			msg.actualReceiverType = castType.getRealType();
			msg.constant = Constant.NotAConstant;
			teamCheck.right.constant = Constant.NotAConstant;
			teamCheck.constant = Constant.NotAConstant;
			teamCheck.resolvedType = TypeBinding.BOOLEAN;

			teamInstanceComparison = teamCheck;
		}


		TypeReference castTypeRef = gen.typeReference(castType.getRealType());
		castTypeRef.resolvedType = castType.getRealType();
		castTypeRef.constant = Constant.NotAConstant;
		expr.expression.resolvedType = exprType.getRealClass();
		expr.expression.constant = Constant.NotAConstant;
		InstanceOfExpression origCheckClone = gen.setPos(new InstanceOfExpression(expr.expression, castTypeRef));
		origCheckClone.bits = expr.bits; // includes operator
		origCheckClone.resolvedType = TypeBinding.BOOLEAN;
		origCheckClone.constant = Constant.NotAConstant;

		AND_AND_Expression andAnd = gen.setPos(new AND_AND_Expression(origCheckClone, teamInstanceComparison, OperatorIds.AND_AND));
		andAnd.resolvedType = TypeBinding.BOOLEAN;
		andAnd.constant = Constant.NotAConstant;
		return andAnd;
	}

	/**
	 * Generates the comparison of team anchors as needed for cast and instanceof.
	 *
	 * @param gen
	 * @param operator either "==" or "!="
	 * @param providedExpression
	 * @param expectedTeamExpr
	 * @param dimensions
	 * @return a boolean expression
	 */
	private static BinaryExpression genTeamCheck(AstGenerator     gen,
												 int 			  operator,
												 Expression       providedExpression,
												 Expression       expectedTeamExpr, int              dimensions)
	{
		BinaryExpression teamCheckCondition;
		Expression teamProvidedRef;
		Expression providedLeafExpression;

		if (dimensions == 0)
			providedLeafExpression = providedExpression;
		else if (dimensions == 1)
			providedLeafExpression = gen.arrayReference(providedExpression, 0);
		else
			throw new InternalCompilerError("Multidimensional array of roles not supported here"); // FIXME: implement this case, too //$NON-NLS-1$
		teamProvidedRef = gen.messageSend(providedLeafExpression, _OT_GETTEAM, null);


		teamCheckCondition = gen.equalExpression(
					teamProvidedRef,
					expectedTeamExpr,
					operator);
		return teamCheckCondition;
	}

    /**
	 * Create the code needed for checking at runtime the identity of type anchors of two role types.
	 * The result already has all information relating to the resolve phase.
	 * @param left
	 * @param right
	 * @param start start of source code position (fake).
	 * @param end end of source code position (fake).
	 * @return a new EqualExpression
	 */
	private static EqualExpression createAnchorEqualCheck(BlockScope scope, DependentTypeBinding left, DependentTypeBinding right, int start, int end)
	{
		AstGenerator gen = new AstGenerator(start,end);
		Expression exprTeam = createTeamAnchorReference(scope, left, gen);
		Expression castTeam = createTeamAnchorReference(scope, right, gen);

		EqualExpression teamInstanceComparison = gen.equalExpression(exprTeam, castTeam, OperatorIds.EQUAL_EQUAL);
		teamInstanceComparison.constant = Constant.NotAConstant;
		return teamInstanceComparison;
	}

	/**
	 * @param scope use this for resolving a newly generated team expression
	 * @param roleType retrieve the anchor from this type.
	 * @param gen represents faked source code position.
	 * @return a new reference
	 */
	private static Expression createTeamAnchorReference(BlockScope scope, DependentTypeBinding roleType, AstGenerator gen) {
		Expression teamExpr = createTeamExpression(roleType, gen);
		teamExpr.resolveType(scope);
		return teamExpr;
	}

	/** Create an expression evaluating to the team instance inherent to the given role type. */
	public static Expression createTeamExpression(DependentTypeBinding roleType, AstGenerator gen)
	{
		return (roleType._teamAnchor instanceof TThisBinding) ?
				gen.qualifiedThisReference((ReferenceBinding)roleType._teamAnchor.getResolvedType())
			:   gen.singleNameReference(roleType._teamAnchor.internalName());
	}

	/**
     * Create the binding between a role and its base:
     * For lowering:
     * + create the _OT$base field.
     * and/or
     * + create the _OT$getBase method (abstract or implemented)
     *
     * Bytecode:
     * + Add the "PlayedBy" Attribute.
     */
    public static void generatePlayedByElements(
            RoleModel role,
            TreeNode  boundParentRole)
    {
        TypeDeclaration roleType = role.getAst();
    	ReferenceBinding baseclass = roleType.binding.baseclass();
        if (baseclass != null)
        {
            if (boundParentRole == null)
            {
                // this is a topmost bound role, create the base link method

                // synthetic interfaces don't reach here.
                if (roleType.isInterface()) {
                    createGetBaseMethod(roleType, baseclass, _OT_GETBASE, AccPublic|AccAbstract|AccSemicolonBody);
                } else {
                    checkCreateBaseField(roleType, baseclass, true);
                    
					// all actual base fields are accessible by a corresponding method:
                    // for role classes create two versions (ifc/class)
                    createGetBaseMethod(role.getInterfaceAst(), baseclass, _OT_GETBASE, AccPublic|AccAbstract|AccSemicolonBody);
                	createGetBaseMethod(roleType, baseclass, _OT_GETBASE, AccPublic);
                }
            } else {
            	// potentially create covariant _OT$getBase() method:
            	ReferenceBinding superBase= boundParentRole.getTreeObject().getBaseTypeBinding();
            	if (!superBase.isCompatibleWith(baseclass)) {
            		// optimization: only need the covariant method, if a superinterface declared
            		// the more specific return type:
            		ReferenceBinding[] superInterfaces= roleType.binding.superInterfaces();
            		if (superInterfaces != null)
            			for (ReferenceBinding superIfc : superInterfaces)
							if (superIfc.baseclass() != null && !superBase.isCompatibleWith(superIfc.baseclass())) {
								createGetBaseMethod(roleType, baseclass, _OT_GETBASE, AccPublic);
								break;
							}
            	}
            }
            ReferenceBinding interfacePartBinding = role.getInterfacePartBinding();
            char[] baseclassName = role.getBaseclassAttributename(true); // with anchor
            role.addAttribute(SingleValueAttribute.playedByAttribute(baseclassName));
            if (!role.getBinding().isInterface() && interfacePartBinding != null)
            	interfacePartBinding.roleModel.addAttribute(
            			SingleValueAttribute.playedByAttribute(baseclassName));
        }
    }


	private static void checkCreateFakedStrongBaseField(
			ReferenceBinding superRole, ReferenceBinding roleClass,
			ReferenceBinding baseTypeBinding)
	{
		if (superRole.baseclass == baseTypeBinding)
			return; // not strengthening
		ReferenceBinding nextSuper= superRole.superclass();
		while (nextSuper.isRole() && nextSuper.roleModel.isBound()) {
			superRole= nextSuper;
			nextSuper= nextSuper.superclass();
		}
		// create faked base field with exact type information:
		FieldBinding fakeStrongBaseField = new FieldBinding(_OT_BASE,
													baseTypeBinding,
													AccPublic|AccFinal|AccSynthetic,
													roleClass,
													Constant.NotAConstant);
		fakeStrongBaseField.tagBits |= TagBits.IsFakedField;
		SourceTypeBinding roleSourceClass = (SourceTypeBinding) roleClass;
		FieldModel model = FieldModel.getModel(fakeStrongBaseField);
		model.actualDeclaringClass = superRole;
		FieldBinding superBaseField= superRole.getField(IOTConstants._OT_BASE, false);
		if (superBaseField != null)
			fakeStrongBaseField.shareBestName(superBaseField);
		roleSourceClass.addField(fakeStrongBaseField);
	}

    /**
     * If roleType is a class-part that does not inherit a base field,
     * create one here.
     *
     * @param roleType  the type to hold the new _OT$base field
     * @param baseclass the type of the field
     * @param createBinding should a FieldBinding be created now?
     */
	public static void checkCreateBaseField(TypeDeclaration roleType,
										    ReferenceBinding baseclass,
										    boolean createBinding)
	{
		if (roleType.isInterface())
			return;

        if (roleType.fields != null)
        	for (FieldDeclaration field : roleType.fields)
				if (CharOperation.equals(field.name, _OT_BASE))
					return;

		AstGenerator gen= roleType.baseclass != null
							 ? new AstGenerator(roleType.baseclass)
							 : new AstGenerator(roleType);
		gen.replaceableEnclosingClass = roleType.binding.enclosingType();
		ReferenceBinding superRole = roleType.binding.superclass;
		if (   superRole != null              // has parent
			&& superRole.isRole()             // parent is role
			&& superRole.roleModel.isBound()) // parent is bound
		{
			checkCreateFakedStrongBaseField(superRole, roleType.binding, baseclass);
			return; // don't try to actually override a field
		}

		int modifiers = AccSynthetic | AccPublic | AccFinal;
		if (roleType.binding.isCompatibleWith(roleType.scope.getOrgObjectteamsIBaseMigratable())) {
			modifiers &= ~ClassFileConstants.AccFinal;
			// migrate method is added from LiftingEnvironment.createOneRoleBaseLinkage()
		}
		FieldDeclaration baseField = gen.field(modifiers,
											   gen.baseclassReference(baseclass),
											   _OT_BASE,
											   null);
		boolean hasTypeProblem = baseclass instanceof MissingTypeBinding;
		AstEdit.addField(roleType, baseField, createBinding, hasTypeProblem, false);
		if (hasTypeProblem && createBinding) {
			// faked resolving for missing type
			baseField.binding.type= baseclass;
			baseField.binding.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
		}
	}

    /**
     * Answer the _OT$getBase() method, create it if needed.
     * We return the get base method of the direct type of roleModel.
     * If a second AST exists (class/ifc) that one will get a get base method, too.
     *
     * @param roleModel
     * @param baseType
     * @return method binding for _OT$getBase().
     */
    public static MethodBinding getGetBaseMethod(
    		BlockScope 		 scope,
            RoleModel        roleModel,
            ReferenceBinding baseType)
    {
		MethodBinding binding = TypeAnalyzer.findMethod(
				scope,       roleModel.getBinding(),
				_OT_GETBASE, Binding.NO_PARAMETERS);
		if (binding.isValidBinding())
			return binding;
		// CHECK(SH): the code below should be obsolete:
		// if no suitable getBase method is found, we need to create it
		if (roleModel.getBinding().isBinaryBinding()) {
			scope.problemReporter().abortDueToInternalError(
					"missing internal _OT$getBase() method"); //$NON-NLS-1$
			return binding; // the problem binding.
		}

		// create both version (ifc/class):
        MethodBinding ifcMethod = createGetBaseMethod(
        			roleModel.getInterfaceAst(), baseType, _OT_GETBASE, AccPublic |AccAbstract|AccSemicolonBody);
        if (roleModel.getClassPartAst() != null) {
        	MethodBinding classMethod = createGetBaseMethod(
        			roleModel.getClassPartAst(), baseType, _OT_GETBASE, AccPublic);
        	if (!roleModel.getAst().isInterface())
        		return classMethod;
        }
        return ifcMethod;
    }
    /**
     * @return either a newly created method or a valid, existing one.
     */
	private static MethodBinding createGetBaseMethod(
			TypeDeclaration roleType,
			ReferenceBinding baseType,
			char[] methodName,
			int flags)
	{
		if (roleType  == null)
			return null; // sanity check, null can be caused by binary ifc-part in a source role.
		MethodBinding existingMethod = null;
		AbstractMethodDeclaration decl = TypeAnalyzer.findMethodDecl(roleType,	methodName, 0);
		if (decl != null)
			existingMethod = decl.binding;
		if (existingMethod == null)
			existingMethod = TypeAnalyzer.findMethod(
				roleType.initializerScope,
				roleType.binding.superclass(),
				methodName,
				Binding.NO_PARAMETERS);
		if (existingMethod != null && existingMethod.isValidBinding()) {     // valid method exists
			if (existingMethod.isAbstract() == roleType.isInterface())       // abstractness is correct
				if (   existingMethod.declaringClass == roleType.binding     // declared here
					|| existingMethod.returnType.isCompatibleWith(baseType)) // inherited but compatible
					return existingMethod;
		}

		AstGenerator gen = roleType.baseclass != null ?
			new AstGenerator(roleType.baseclass.sourceStart, roleType.baseclass.sourceEnd) :
			new AstGenerator(roleType.sourceStart, roleType.sourceEnd);
		gen.replaceableEnclosingClass = roleType.binding.enclosingType();

		TypeReference baseclassReference; // must set in if or else
		TypeParameter methodParam = null;
		Statement bodyStatement;		  // must set in if or else
		if (baseType != null) {
			baseclassReference = gen.baseclassReference(baseType);
			bodyStatement = gen.returnStatement(
									gen.castExpression(
											gen.singleNameReference(_OT_BASE),
											baseclassReference,
											CastExpression.DO_WRAP
									));
		} else {
			// this role is not bound but create a generic getBase method for use via a <B base R> type:
			final char[] paramName = "_OT$AnyBase".toCharArray(); //$NON-NLS-1$ used only for this one declaration.
			methodParam = gen.baseBoundedTypeParameter(paramName, roleType.binding);
			baseclassReference = gen.singleTypeReference(paramName);
			final char[][] ABSTRACT_METHOD = new char[][] {"java".toCharArray(), 				 //$NON-NLS-1$
													       "lang".toCharArray(), 				 //$NON-NLS-1$
													       "AbstractMethodError".toCharArray()}; //$NON-NLS-1$
			bodyStatement = gen.throwStatement(
									gen.allocation(
											gen.qualifiedTypeReference(ABSTRACT_METHOD),
											null));
		}
		MethodDeclaration getBase = gen.method(roleType.compilationResult, flags,
										 	   baseclassReference, methodName, null);

		if (methodParam != null)
			getBase.typeParameters = new TypeParameter[]{methodParam};

		AstEdit.addMethod(roleType, getBase);
		for (ReferenceBinding tsuperRole : roleType.getRoleModel().getTSuperRoleBindings()) {
			for (MethodBinding tsuperMethod : tsuperRole.getMethods(_OT_GETBASE))
				getBase.binding.addOverriddenTSuper(tsuperMethod);
		}

		if (methodParam != null)
			roleType.getRoleModel().unimplementedGetBase = getBase.binding;

		if ((flags & AccSemicolonBody) == 0)
		{
			getBase.setStatements(new Statement[]{bodyStatement});
			if (StateMemento.hasMethodResolveStarted(roleType.binding))
				getBase.resolveStatements();
		}
		return getBase.binding;
	}


	/** If an unbound role declares to implement ILowerable an abstract getBase method must be added. */
	public static void createGetBaseForUnboundLowerable(RoleModel model) {
		TypeDeclaration interfaceAst = model.getInterfaceAst();
		if (interfaceAst == null || interfaceAst.scope == null)
			return;
		ClassScope scope = interfaceAst.scope;
		TypeBinding iLowerable = scope.getType(
				new char[][] {IOTConstants.ORG, IOTConstants.OBJECTTEAMS, IOTConstants.ITEAM, IOTConstants.ILOWERABLE},
				4);
		if (!model.isBound() && model.getBinding().isCompatibleWith(iLowerable))
			createGetBaseMethod(interfaceAst, scope.getJavaLangObject(), _OT_GETBASE, AccPublic|AccAbstract|AccSemicolonBody);
	}
}
