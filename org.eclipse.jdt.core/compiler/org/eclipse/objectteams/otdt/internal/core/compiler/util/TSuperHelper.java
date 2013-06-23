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
 * $Id: TSuperHelper.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccProtected;
import static org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers.AccRole;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.AccSynthIfc;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.MARKER_ARG_NAME;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.OT_DELIM_NAME;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.TSUPER_OT_NAME;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.OTNameUtils;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TSuperMessageSend;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.StateHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * This class is repsonsible for so-called marker interfaces by which copied methods
 * are marked as tsuper-version.
 *
 * Note that this class is not intended for instantiation, but as a library of static methods.
 *
 * @author stephan
 * @version $Id: TSuperHelper.java 23416 2010-02-03 19:59:31Z stephan $
 *
 */
public class TSuperHelper {


	/**
	 * Get the name of the interface marking methods inherited from `superTeam'
	 * @param superTeam
	 * @return marker ifc name.
	 */
	public static char[] getTSuperMarkName(ReferenceBinding superTeam) {
		char[] name = superTeam.sourceName();
		while ((superTeam = superTeam.enclosingType()) != null)
			name = CharOperation.concat(superTeam.sourceName(), OT_DELIM_NAME, name);
		return CharOperation.concat(TSUPER_OT_NAME, name);
	}

	// ==== Actual TSuper Marker Interfaces ===

	/**
	 * @param teamModel
	 * @param superTeam
	 */
	public static void addMarkerInterface(TeamModel teamModel, ReferenceBinding superTeam) {
		TypeDeclaration teamDeclaration = teamModel.getAst();
		TypeDeclaration markerInterface = createMarkerInterface(teamDeclaration, superTeam);
		teamModel.markerInterface = markerInterface;
	    markerInterface.getRoleModel().setState(ITranslationStates.STATE_LENV_DONE_FIELDS_AND_METHODS);
	}

	/**
	 * Only create one marker interface per team during copy inheritance.
	 * Other marker types should be retrieved using scope.getType(name);
	 */
	private static TypeDeclaration createMarkerInterface(TypeDeclaration teamTypeDeclaration, ReferenceBinding superteam) {
	    char[] superMarkName = TSuperHelper.getTSuperMarkName(superteam);
	    if (teamTypeDeclaration.memberTypes != null)
	        for (int i = 0; i < teamTypeDeclaration.memberTypes.length; i++) {
	        	if (CharOperation.equals(teamTypeDeclaration.memberTypes[i].name, superMarkName))
	        		return teamTypeDeclaration.memberTypes[i];
			}
	    return AstConverter.createNestedType(
	        superMarkName,
	        AccSynthIfc | AccRole | AccProtected,
			false, /* not a nested type */
	        false, /* not purely copied */
			teamTypeDeclaration,
			null /* no tsuper role */);
	}

	public static TypeBinding getMarkerInterface(Scope scope, ReferenceBinding superteam) {
		return scope.getType(TSuperHelper.getTSuperMarkName(superteam));
	}

	private static TypeReference markerTypeRef(ReferenceBinding superTeam, AstGenerator gen) {
		return gen.singleTypeReference(TSuperHelper.getTSuperMarkName(superTeam));
	}

	public static boolean isMarkerInterface(TypeBinding type) {
		if (type.isWildcard()) // wildcards don't even have a name!
			return false;
	    if (   type instanceof ReferenceBinding
	    	&& type.isValidBinding())
	        return OTNameUtils.isTSuperMarkerInterface(((ReferenceBinding)type).internalName());
	    return false;
	}

	// ==== Marker Arguments: ====

	/**
	 * Is the expression the casted null of a marker arg?
	 * @param expression
	 * @return the answer
	 */
	public static boolean isMarkerArg(Expression expression) {
		if (!(expression instanceof CastExpression))
			return false;
		CastExpression cast = (CastExpression)expression;
		if (!(cast.type instanceof SingleTypeReference))
			return false;
		return OTNameUtils.isTSuperMarkerInterface(((SingleTypeReference)cast.type).token);
	}

	/**
	 * Is the given method a tsuper-version (recognized by having
	 * a marker arg at last position) ?
	 *
	 * @param method
	 * @return the answer
	 */
	public static boolean isTSuper(MethodBinding method) {
		if (method == null) return false;
	    TypeBinding[] params = method.parameters;
	    if (params.length > 0) {
	        TypeBinding lastParam = params[params.length-1];
	        if (lastParam != null)
	            return isMarkerInterface(lastParam);
	    }
	    return false;
	}

	/**
	 * Create and add a marker arg which marks a tsuper message send
	 * 		"Qualification.tsuper.m(args)" -> "args, TSuper_OT_QualifyingTeam"
	 *
	 * @param qualifyingType may be null.
	 * @param tsuperCall
	 * @param arguments
	 * @param scope
	 * @return argument expressions, either new, or unchanged or null (error).
	 */
	public static Expression[] addMarkerArgument(ReferenceBinding qualifyingType, Statement        tsuperCall, Expression[]     arguments, BlockScope       scope) {
		if(scope == null)
			return null; // see 2.3.20-otjld-illegal-constructor-call-10

		int sStart = tsuperCall.sourceStart;
		int sEnd   = tsuperCall.sourceEnd;

		SourceTypeBinding roleType = scope.enclosingSourceType();
	    if (!roleType.isSourceRole()) {

	        scope.problemReporter().tsuperOutsideRole(
	            (AbstractMethodDeclaration)scope.methodScope().referenceContext,
	            tsuperCall,
	            roleType);
	        return arguments; // unchanged.
	    }
	    ReferenceBinding[] tsuperRoleBindings = roleType.roleModel.getTSuperRoleBindings();
	    if (tsuperRoleBindings.length == 0) {
	    	scope.problemReporter().tsuperCallWithoutTsuperRole(roleType, tsuperCall);
	    	return null;
	    }
		ReferenceBinding superTeam;
		if (qualifyingType == null) {
			superTeam = tsuperRoleBindings[0].enclosingType();
		} else {
			TSuperMessageSend tsuperMsg = (TSuperMessageSend)tsuperCall;
			ReferenceBinding superRole = (ReferenceBinding)tsuperMsg.tsuperReference.resolveType(scope);
			superTeam = superRole.enclosingType();
			if (superTeam == null)
				return null;
		}

		AstGenerator gen = new AstGenerator(sStart, sEnd);

		CastExpression cast = createMarkerArgExpr(superTeam, gen);

		Expression[] newArgs;
		if(arguments == null){
			newArgs = new Expression[] {cast};
		} else {
			newArgs = new Expression[arguments.length+1];
			System.arraycopy(arguments,0, newArgs,0, arguments.length);
			newArgs[newArgs.length-1] = cast;
		}
		return newArgs;
	}

	public static CastExpression createMarkerArgExpr(ReferenceBinding superTeam, AstGenerator gen) {
		return gen.castExpression(
				gen.nullLiteral(),
				markerTypeRef(superTeam, gen),
				CastExpression.RAW);
	}

	/**
	 * Augments the method signature with a special parameter
	 * (adds an additional parameter at end of parameter list).
	 * The type of the new parameter is the MarkerInterface-Type.
	 * <code>public method(String str)</code>
	 * will be augmented to
	 * <code>public method(String str, TSuper__OT__XYZ __OT__marker )</code>
	 * where XYZ is the name of the direct superteam.
	 * @param methodDeclaration the method of which Parameters will be augmented
	 * @param origin superTeam used for part of the marker type's name
	 */
	public static void addMarkerArg(AbstractMethodDeclaration methodDeclaration, ReferenceBinding origin) {
		AstGenerator gen = new AstGenerator(methodDeclaration.sourceStart, methodDeclaration.sourceEnd);
	    TypeReference marker = gen.singleTypeReference(TSuperHelper.getTSuperMarkName(origin));

	    int argPos = 0;
		if(methodDeclaration.arguments==null){
			methodDeclaration.arguments = new Argument[1];
		} else{
			int argumentlength = methodDeclaration.arguments.length;
			if(argumentlength == 0){
				methodDeclaration.arguments = new Argument[1];
			} else{
				Argument[] arguments = new Argument[argumentlength+1];
				System.arraycopy(methodDeclaration.arguments,0, arguments,0, argumentlength);
				methodDeclaration.arguments=arguments;
	            argPos = argumentlength;
			}
		}
	    methodDeclaration.arguments[argPos] =
	        new Argument(MARKER_ARG_NAME,0,marker,0);
	    methodDeclaration.isTSuper = true;
	    if (methodDeclaration.binding != null) {
	    	TypeBinding[] oldParams = methodDeclaration.binding.parameters;
	    	TypeBinding[] newParams = new TypeBinding[oldParams.length+1];
	    	System.arraycopy(oldParams, 0, newParams, 0, oldParams.length);
	    	newParams[oldParams.length] = marker.resolveType(methodDeclaration.scope);
	    	methodDeclaration.binding.parameters = newParams;
	    	methodDeclaration.binding.resetSignature();
	    }
	}

	public static boolean isTSubOf(ReferenceBinding class1, ReferenceBinding other) {
		if (!class1.isRole() || !other.isRole())
			return false;
		if (   StateHelper.hasState(other, ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY)
			&& StateHelper.hasState(class1, ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY)
			&& StateHelper.hasState(class1.enclosingType(), ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY))
		{
			return class1.roleModel.hasTSuperRole(other);
		}
		if (!CharOperation.equals(class1.sourceName(), other.sourceName()))
			return false;
		return class1.enclosingType().superclass().erasure() == other.enclosingType().erasure()
			|| isTSubOf(class1.enclosingType(), other.enclosingType());
	}

	/** Do two type anchors basically refer to the same field (modulo impl/expl. inheritance)? */
	public static boolean isEquivalentField(ITeamAnchor currentAnchor, ITeamAnchor tsuperAnchor) {
		if (currentAnchor == tsuperAnchor)
			return true;
		if ((currentAnchor == null) || (tsuperAnchor == null))
			return false;
		if (!(currentAnchor instanceof FieldBinding))
			return false;
		if (!(tsuperAnchor instanceof FieldBinding))
			return false;
		FieldBinding currentField = (FieldBinding)currentAnchor;
		FieldBinding tsuperField = (FieldBinding)tsuperAnchor;
		if (!CharOperation.equals(currentField.name, tsuperField.name))
			return false;
		if (currentField.declaringClass.isCompatibleWith(tsuperField.declaringClass))
			// different fields means one should be a faked strong copy of the other:
			return (currentField.tagBits & TagBits.IsFakedField) != 0;
		if (isTSubOf(currentField.declaringClass, tsuperField.declaringClass))
			return true;
		return false;
	}

// UNUSED but commit this code at least once to facility retrieval should it be needed later.
//	/**
//	 * Convert a marker interface back to the team being represented.
//	 * @param marker
//	 * @param environment
//	 * @return team reference binding
//	 */
//	public static ReferenceBinding markerToTeam(ReferenceBinding site, ReferenceBinding marker) {
//		int compoundLen = marker.compoundName.length;
//		// this will hold the compound name of the outermost type:
//		char[][] compoundName = new char[compoundLen][];
//		System.arraycopy(marker.compoundName, 0, compoundName, 0, compoundLen);
//
//		// extract the pure name of the marker interface:
//		char[][] splitName = CharOperation.splitOn('$', compoundName[compoundLen-1]);
//		String name = new String(splitName[splitName.length-1]);
//
//		// parse the name:
//		int start = TSUPER_OT_LEN;
//		ReferenceBinding clazz = null;
//		int end = 0;
//		while (end != -1) {
//			end = name.indexOf(OT_DELIM, start); // additional delim-separator?
//			String element = null;
//			if (end == -1)
//				element = name.substring(start); // get the last element
//			else
//				element = name.substring(start, end);
//			if (clazz == null) {
//				// retrieve the outermost type:
//				compoundName[compoundLen-1] = element.toCharArray();
//				//clazz = environment.getType(compoundName);
//				clazz = findType(site, element.toCharArray());
//			} else {
//				// retrieve member types:
//				clazz = clazz.getMemberType(element.toCharArray());
//				if (clazz.isSynthInterface())
//					clazz = clazz.roleModel.getClassPartBinding();
//			}
//			start=end+OT_DELIM_LEN;
//		}
//		return clazz;
//	}
//
//	private static ReferenceBinding findType(ReferenceBinding site, char[] name) {
//		ReferenceBinding currentLevel = site;
//		do {
//			ReferenceBinding currentSuper = currentLevel;
//			do {
//				if (CharOperation.equals(currentSuper.internalName(), name))
//					return currentSuper;
//				currentSuper = currentSuper.superclass();
//			} while (currentSuper != null);
//			currentLevel = currentLevel.enclosingType();
//		} while (currentLevel != null);
//		return null;
//	}

}