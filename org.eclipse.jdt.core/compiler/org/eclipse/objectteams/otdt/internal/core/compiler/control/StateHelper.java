/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleModel.java 16291 2007-09-09 14:58:47Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.control;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;

import static org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates.*;

/**
 * Handle translation states of compilation units and types.
 *
 * @author stephan
 * @since OTDT 1.1.3
 */
public class StateHelper
{
	// === categorizing states: ===

	static boolean isRequiredState(int state) {
		if (state <= STATE_LENV_DONE_FIELDS_AND_METHODS)
			return true;
		switch (state) {
		case STATE_NONE:
		case STATE_ROLES_LINKED:
		case STATE_ROLE_HIERARCHY_ANALYZED:
		case STATE_FULL_LIFTING:
		case STATE_FAULT_IN_TYPES:
		case STATE_METHODS_CREATED:
		case STATE_STATEMENTS_TRANSFORMED: // detects and marks local types
		case STATE_CALLINS_TRANSFORMED:
		case STATE_METHODS_VERIFIED:
		case STATE_RESOLVED:
	    case STATE_CODE_ANALYZED:
		case STATE_BYTE_CODE_PREPARED:
		case STATE_BYTE_CODE_GENERATED:
			return true;
		default:
			return false;
		}
	}

	static boolean isCUDState(int state, int[] exceptions)
	{
		if (exceptions != null)
			for (int exception : exceptions)
				if (state == exception)
					return false;

		switch (state) {
		case STATE_BINDINGS_BUILT:
		case STATE_LENV_BUILD_TYPE_HIERARCHY:
		case STATE_LENV_CHECK_AND_SET_IMPORTS:
		case STATE_LENV_CONNECT_TYPE_HIERARCHY:
		case STATE_LENV_DONE_FIELDS_AND_METHODS:
		case STATE_ROLE_FILES_LINKED:
		case STATE_METHODS_PARSED:
		case STATE_METHODS_VERIFIED:
	    case STATE_RESOLVED:
	    case STATE_CODE_ANALYZED:
	    case STATE_BYTE_CODE_GENERATED:
	    	return true;
	    default: return false;
		}
	}

	// === query elements for their state: ===

	static boolean unitHasState(CompilationUnitDeclaration unit, int state)
	{
		if (unit.types == null)
			return false;
		for (TypeDeclaration type : unit.types) {
			if (!StateHelper.hasState(type.binding, state))
				return false;
		}
		unit.state.setState(state);
		return true;
	}

	/**
	 * does clazz have state (wrt its most appropriate model)?
	 */
	public static boolean hasState(ReferenceBinding clazz, int state) {
		if (clazz == null)
			return false;
	    return getState(clazz).getState() >= state;
	}

	/** Answer the minimal state among the given types. */
	public static int minimalState(ReferenceBinding[] types)
	{
		int s = ITranslationStates.STATE_FINAL;
		for (ReferenceBinding type : types) {
			int s1= getState(type).getState();
			if (s1 < s)
				s = s1;
		}
		return s;
	}

	// === update element's state: ===

	/** Recursively mark that `unit' has reached `state'.
	 *  Also trigger any further translation that has been requested during processing.
	 *
	 * @param unit
	 * @param state
	 * @param markRoleUnits if true also mark CUD of any member RoFi.
	 */
	public static void setStateRecursive(CompilationUnitDeclaration unit, int state, boolean markRoleUnits)
	{
		int requested= ITranslationStates.STATE_NONE;

		// update the unit:
		unit.state.setState(state);

		// descend to children:
		TypeDeclaration[] types = unit.types;
		if(types != null)
			for (int t=0; t < types.length; t++) {
				setStateRecursive(types[t], state, markRoleUnits);
				if (types[t].isRole()) {
					int roleRequest = types[t].getRoleModel()._state.fetchRequestedState();
					if (roleRequest > requested)
						requested= roleRequest;
				}
			}
		LocalTypeBinding[] localTypes = unit.localTypes;
		if(localTypes != null) {
			for (int t=0; t < unit.localTypeCount; t++) {
				if (localTypes[t].scope != null) {
					ClassScope scope = localTypes[t].scope;
					if (scope.referenceContext != null)
						setStateRecursive(scope.referenceContext, state, markRoleUnits);
				}
			}
		}

		// handle requests:
		unit.state.handleRequest(unit, requested);
	}

	/**
	 * record that decl and all contained types are in state `state'.
	 * make sure to reach all models of all types.
	 * @param markRoleUnit TODO
	 */
	static void setStateRecursive(TypeDeclaration decl, int state, boolean markRoleUnit)
	{
	    TypeModel model;
	    boolean processed = false;
	    if (decl.isTeam()) {
	        model = decl.getTeamModel();
	        model.setState(state);
	        processed = true;
	    }
	    if (decl.isRole()) {
	    	boolean handlingRoleFileImports= decl.isRoleFile()
						&& (state == STATE_LENV_CHECK_AND_SET_IMPORTS);
	    	if (!handlingRoleFileImports || markRoleUnit) {
	            model = decl.getRoleModel();
	            model.setState(state);
	            if (decl.compilationUnit != null)
	        		decl.compilationUnit.state.setState(state);
	    	}
	    	processed = true;
	    }
	    if (!processed) {
	        model = decl.getModel();
	        model.setState(state);
	    }
	    if (decl.memberTypes != null) {
	    	for (int i = 0; i < decl.memberTypes.length; i++) {
				setStateRecursive(decl.memberTypes[i], state, markRoleUnit);
			}
	    }
	}

	// === manage current processing and pending requests: ===

	/**
	 * Is the given type ready to process `state'?
	 * This is not the case if some processing already takes place
	 *  - either directly
	 *  - or via its enclosing team if state is a CUD state
	 * If current processing is less that `state'
	 * then schedule `state' as _requested.
	 */
	public static boolean isReadyToProcess(TypeModel model, int state) {
		if (!model.isReadyToProcess(state))
			return false;
		if (model.isRole() && isCUDState(state, null)) {
			ReferenceBinding role= model.getBinding();
			if (role != null &&  role.enclosingType() != null)
				return hasState(role.enclosingType(), state)
					|| isReadyToProcess(role.enclosingType().getTeamModel(), state);
		}
		return true;
	}

	public static boolean isReadyToProcess(SourceTypeBinding type, int state) {
		if (type.isRole() && !isReadyToProcess(type.roleModel, state))
			return false;
		if (type.isTeam() && !isReadyToProcess(type.getTeamModel(), state))
			return false;
		return isReadyToProcess(type.model, state);
	}

	/** Signal that processing for `state' has begun
	 *  (role- and team-models of type plus enclosing team).
	 * @param type
	 * @param state state according to ITranslationStates
	 * @param step sub-step according to LookupEnvironment
	 */
	public static boolean startProcessing(TypeDeclaration type, int state, int step)
	{
		if (type.isRole()) {
			RoleModel roleModel = type.getRoleModel();
			if (roleModel.getTeamModel()._state.isBlocking(roleModel.getTeamModel(), state, step))
				return false; // processing a late role file.
			roleModel._state.startProcessing(state, step);
		}
		if (type.isTeam())
			type.getTeamModel()._state.startProcessing(state, step);
		return true;
	}


	static StateMemento getState(ReferenceBinding type) {
	    TypeModel model = null;
	    if (type.isTeam())
	        model = type.getTeamModel();
	    else if (type.isRole())
	        model = type.roleModel;
	    if (model == null)
	        model = type.model;
	    return model._state;
	}
}
