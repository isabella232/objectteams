/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2011 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Dependencies.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.control;


import java.util.Iterator;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.compiler.OTNameUtils;
import org.eclipse.objectteams.otdt.core.compiler.Pair;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.RoleClassLiteralAccess;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.RoleInitializationMethod;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.DeclaredLifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lifting.InstantiationPolicy;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.LiftingEnvironment;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.OTClassScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CallinImplementor;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CallinImplementorDyn;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CalloutImplementor;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.MethodMappingResolver;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.ModelElement;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.RecordLocalTypesVisitor;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.RoleSplitter;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.StandardElementGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.TransformStatementsVisitor;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleFileHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * This class manages the dependencies between different translation steps.
 *
 * There are a few cross-cutting dependencies that complicate the process:
 *
 * Local Types of Roles
 * --------------------
 * + These are added to the process via RoleModel.addLocalType triggered from:
 * 		- TransformStatementsVisitor
 *      - CopyInheritance.copyRoleNestedInternal
 *   (binary types use addUnresolvedLocalType)
 * + Bindings for local types are created only during resolve:
 *		- QualifiedAllocationExpression.resolveType()->BlockScope.addAnonymousType()
 *    	- {various statements}.resolve(BlockScope)->TypeDeclaration.resolve(BlockScope)->BlockScope.addLocalType
 * + While processing states below STATE_RESOLVED, local types are skipped
 * 	  (see  ensureRoleState.'loop over localTypes' and RoleModel.setMemberState()).
 * + During unit.resolve() local types catch up:
 * 		ClassScope.{buildAnonymousTypeBinding(),buildLocalTypeBinding()} trigger ensureRoleState().
 *
 * Role files:
 * + STATE_METHODS_PARSED needs to travel up (to team-unit) and down (to role-unit)
 *
 *
 * @author stephan
 * @version $Id: Dependencies.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class Dependencies implements ITranslationStates {

	/**
     * Configure the Dependencies module for use by a specific client.
	 * @param client  the object invoking setup
	 * @param parser
	 * @param lookupEnvironment
	 * @param verifyMethods
	 * @param analyzeCode
	 * @param generateCode
	 * @param buildFieldsAndMethods
	 * @param bundledCompleteTypeBindings
	 * @param strictDiet
     */
    public static Config setup(
    		Object            client,
			Parser			  parser,
			LookupEnvironment lookupEnvironment,
			boolean	          verifyMethods,
			boolean           analyzeCode,
			boolean           generateCode,
			boolean           buildFieldsAndMethods,
			boolean           bundledCompleteTypeBindings,
			boolean           strictDiet)
    {
    	Config config = new Config();
    	config.client = client;
    	config.parser = parser;
    	config.lookupEnvironment = lookupEnvironment;
    	config.verifyMethods = verifyMethods;
    	config.analyzeCode = analyzeCode;
    	config.generateCode = generateCode;
    	config.buildFieldsAndMethods = buildFieldsAndMethods;
    	config.bundledCompleteTypeBindings = bundledCompleteTypeBindings;
    	config.strictDiet = strictDiet;

    	// Debugging
//    	config.ex = new Exception("Dependencies.setup()");
//    	config.ex.getStackTrace();

    	Config.addConfig(config);
    	return config;
    }

    /**
     * Configure the Dependencies module for use by a specific client.
	 * @param client  the object invoking setup
     * @param parser
     * @param lookupEnvironment
     * @param buildFieldsAndMethods should bindings for fields and methods be built?
     * @param strictDiet are statements ever parsed or are we on a strict diet?
     */
    public static void setup(
    		Object            client,
			Parser			  parser,
			LookupEnvironment lookupEnvironment,
			boolean           buildFieldsAndMethods,
			boolean           strictDiet)
    {
    	Dependencies.setup(client, parser, lookupEnvironment, true, true, true,
    			buildFieldsAndMethods, false, strictDiet);
    }

    /**
     * Configure the Dependencies module for use by a specific client.
	 * @param client  the object invoking setup
     * @param parser
     * @param lookupEnvironment
     * @param buildFieldsAndMethods should bindings for fields and methods be built?
     * @param bundledCompleteTypeBindings should completeTypeBindings try to treat all units at once?
     * @param strictDiet are statements ever parsed or are we on a strict diet?
     */
    public static void setup(
    		Object            client,
			Parser			  parser,
			LookupEnvironment lookupEnvironment,
			boolean           buildFieldsAndMethods,
			boolean           bundledCompleteTypeBindings,
			boolean           strictDiet)
    {
    	Dependencies.setup(client, parser, lookupEnvironment, true, true, true,
    			buildFieldsAndMethods, bundledCompleteTypeBindings, strictDiet);
    }


	/**
     * Release Dependencies configuration from a specific client.
	 * @param client ensure that client actually configured the
	 *   module from the same thread as currently executing.
	 */
	public static void release(Object client) {
	    Config.removeConfig(client);
	}

	public static boolean isSetup() {
	    return Config.hasConfig();
	}

	enum Success {
		Fail, NotReady, OK;
	}

	/**
     * All roles and Teams contained in `unit' are required to be in `state'.
     * Do the necessary translations, if any.
     *
     * When used: MAIN ENTRY of the statemachine.
     *
     * @param unit
     * @param state required state
     * @return success?
     */
	public static boolean ensureState (CompilationUnitDeclaration unit, int state) {
		return ensureState(unit, null, state) == Success.OK;
	}

	/**
     * All roles and Teams contained in `unit' are required to be in `state'.
     * Do the necessary translations, if any.
     *
     * When used: MAIN ENTRY of the statemachine.
     *
     * @param unit
     * @param accessRestriction (optional) access restriction for STATE_BINDINGS_BUILT
     * @param state required state
     * @return success?
     */
	public static Success ensureState (CompilationUnitDeclaration unit, AccessRestriction accessRestriction, int state)
	{
        boolean done = true;
        Success success = Success.OK;
        int oldState = unit.state.getState();

        if (unit.ignoreFurtherInvestigation)
			success = Success.Fail; // but KEEPGOING to generate also problem class files.

		if (oldState >= STATE_BINDINGS_BUILT && unit.types == null)
			return Success.OK; // nothing to do for empty CUD after building bindings (which analyzes some package issues)

		if (oldState >= STATE_BINDINGS_BUILT && unit.scope == null)
			return Success.Fail; // no chance

        if (oldState >= state) {
        	return Success.OK;
        }
        else
        {
        	unit.state.startProcessing(state, 0);

        	if (state > 1)
                success = ensureState(unit, null, state -1);

    		if (success == Success.NotReady)
    			return success;

    		if (   (success == Success.OK)
   				|| StateHelper.isRequiredState(state)) // KEEPGOING generate problem class in any case
   			{
    			Pair<Boolean,Success> result = establishUnitState(unit, state, success,	accessRestriction);
    			done = result.first;
    			success = result.second;
    		}
        }

        if (done) {
        	if (success != Success.NotReady)
        		StateHelper.setStateRecursive(unit, state, true);
        } else {
			TypeDeclaration[] types = unit.types;
			if(types != null) {
				int nextState = oldState;
				while (++nextState <= state)
					for (int t=0; /* KEEPGOING success &&*/ t < types.length; t++)
						if (!ensureAstState(types[t], nextState))
							success = Success.Fail;
			}
			unit.state.setState(state);
		}

        return success;
	}

	/* Core of above method without the recursion. */
	private static Pair<Boolean,Success> establishUnitState(CompilationUnitDeclaration unit,
			int state, Success success, AccessRestriction accessRestriction)
	{
		boolean done = true;
		if (StateHelper.unitHasState(unit, state))
			return new Pair<Boolean,Success>(done, Success.OK);

	    // original calls stolen from Compiler.process are handled here:
		try {
		    switch (state)
		    {
		    	case STATE_ROLE_FILES_LINKED:
		    		establishRoleFilesLinked(unit, Config.getLookupEnvironment());
		    		break;
		    	case STATE_BINDINGS_BUILT:
		    		Config.getLookupEnvironment().internalBuildTypeBindings(unit, accessRestriction);
		    		break;
		    	case STATE_LENV_BUILD_TYPE_HIERARCHY:
		    	case STATE_LENV_CHECK_AND_SET_IMPORTS:
		    	case STATE_LENV_CONNECT_TYPE_HIERARCHY:
		    	case STATE_LENV_DONE_FIELDS_AND_METHODS:
		    		checkReadKnownRoles(unit);
		    		LookupEnvironment lookupEnvironment= Config.getLookupEnvironment();
		    		if (Config.getBundledCompleteTypeBindingsMode()) {
		            	Config.getLookupEnvironment().internalCompleteTypeBindings(unit);
		            	int stateReached = lookupEnvironment.getDependenciesStateCompleted();
		            	StateHelper.setStateRecursive(unit, stateReached, true);
		            	if (stateReached >= state)
		            		return new Pair<Boolean,Success>(done,
		            									 (stateReached >= state) ? Success.OK : Success.NotReady);
						// if not successful try directly:
		    		}
	    			// completely under our control:
	    			switch (state) {
	            	case STATE_LENV_BUILD_TYPE_HIERARCHY:
	            		break; // no-op
	            	case STATE_LENV_CHECK_AND_SET_IMPORTS:
	            		unit.scope.checkAndSetImports();
	            		break;
	            	case STATE_LENV_CONNECT_TYPE_HIERARCHY:
	            		CompilationUnitDeclaration previousUnit = lookupEnvironment.unitBeingCompleted;
	            		lookupEnvironment.unitBeingCompleted = unit;
	            		try {
	            			unit.scope.connectTypeHierarchy();
	            		} finally {
	            			lookupEnvironment.unitBeingCompleted = previousUnit;
	            		}
	            		break;
	            	case STATE_LENV_DONE_FIELDS_AND_METHODS:
	            		unit.scope.checkParameterizedTypes();
	            		if (Config.getBuildFieldsAndMethods())
	            			unit.scope.buildFieldsAndMethods();
	            		break;
	    			}
		    		break;
		        case STATE_METHODS_PARSED:
		            if (success == Success.OK && unit.parseMethodBodies)
		                Config.delegateGetMethodBodies(unit);
		            // special case: mark the unit but also descend into types (for role units)
		            unit.state.setState(STATE_METHODS_PARSED);
		            done = false;
		            break;
		        case STATE_METHODS_VERIFIED:
					if (Config.getVerifyMethods())
		            	unit.scope.verifyMethods(Config.getLookupEnvironment().methodVerifier());
		            break;
		        case STATE_RESOLVED:
		           	unit.resolve();
		            break;
		        case STATE_CODE_ANALYZED:
					if (Config.getAnalyzeCode())
		            	unit.analyseCode();
		            break;
		        case STATE_BYTE_CODE_GENERATED:
					if (Config.getGenerateCode())
		            	unit.generateCode();
						// descending into already generated types is
						// prevented by TypeDeclaration.hasBeenGenerated
		            break;
		        default:
		        	done = false;
		    }
		} catch (Config.NotConfiguredException e) {
			e.logWarning("Processing CU "+String.valueOf(unit.getFileName())+" failed"); //$NON-NLS-1$ //$NON-NLS-2$
			success = Success.Fail;
		}
		
		return new Pair<Boolean,Success>(done, success);
	}

	private static CompilationUnitDeclaration getCUD(TypeModel model) {
        TypeDeclaration type = model.getAst();
        if (type == null)
        	return null; // it was read from class file.

        // local types tend to have no scope if something went bad (incl. utterly broken types)
        // so travel the AST instead, as long as possible:
        while (type.enclosingType != null) {
        	type = type.enclosingType;
        }

    	if (type.scope == null) {
            if (type.compilationResult.problemCount > 0)
                return null;
            throw new InternalCompilerError("scope should not be null!"); //$NON-NLS-1$
    	}

        return type.scope.referenceCompilationUnit();
	}

    /**
     * Start translation for this state at the top (CompilationUnit).
     * This method works for those states only that operate on the AST.
     *
     * When used: delegate up from ensure{Type,Team,Role}State
     */
    private static boolean ensureUnitState(TypeModel model, int state)
    {
        TypeDeclaration type = model.getAst();
        if (type != null) // otherwise it was read from class file.
        {
            CompilationUnitDeclaration unit = getCUD(model);
            if (unit == null)
            	return false;
            boolean success = (ensureState(unit, null, state) == Success.OK);
            if (success)
            	unit.state.assertState(state, "Inconsistent unit state, expected "+state); //$NON-NLS-1$
            	// ... but might still need to descend into types for role units.
            return success;
        } else { // nothing to to for binary types, just mark the state:
            model.setState(state);
            model.setMemberState(state);
        }
        return true;
    }

    /**
     * If `type' is either a Team or a role, ensure that it has at least
     * `state' translation status.
     *
     * When used: dispatching down from ensureState(CompilationUnitDeclaration,int)
     *
     * @param type
     * @param state
     * @return success
     */
	private static boolean ensureAstState (TypeDeclaration type, int state)
	{
		if (   TypeModel.isIgnoreFurtherInvestigation(type)
			&& !StateHelper.isRequiredState(state)) // KEEPGOING
			return false;

        boolean processed = false;
		if (type.isRole())
		{
			// this includes the case of "team as role"
            if (!ensureRoleState(type.getRoleModel(), state))
                return false;
            processed = true;
		} else if (type.isTeam())
		{
			if (!ensureTeamState(type.getTeamModel(), state))
				return false;
            processed = true;
		}
        if (!processed)
        {
            return ensureTypeState(type.getModel(), state);
        }
		return processed;
	}

    /**
     * If `type' is either a Team or a role, ensure that it has at least
     * `state' translation status.
     *
     * When used: EXTERNAL ENTRY from resolve et al.
     *
     * @param type
     * @param state
     * @return success
     */
    public static boolean ensureBindingState (ReferenceBinding type, int state)
    {
        boolean processed = false;
        if (type instanceof ProblemReferenceBinding)
        	return false;
        if (type.isRole())
        {
        	// this includes the case of "team as role"
        	ReferenceBinding ifcPart = type.getRealType();
        	ReferenceBinding clsPart = type.getRealClass();
            boolean success = true;
            if (ifcPart != null)
            	success = ensureRoleState(ifcPart.roleModel, state);
            if (   clsPart != null
            	&& (ifcPart == null || clsPart.roleModel != ifcPart.roleModel))
            	success &= ensureRoleState(clsPart.roleModel, state);
            if (!success)
                return false;
            processed = true;
        } else if (type.isTeam())
        {
            if (!ensureTeamState(type.getTeamModel(), state))
                return false;
            processed = true;
        }
        if (!processed)
        {
            return ensureTypeState(type.model, state);
        }
        return processed;
    }

	/**
	 * @param model
	 */
	public static boolean ensureTeamState(TeamModel model, int state) {
		boolean success = true;

    	ReferenceBinding teamBinding = model.getBinding();
		TypeDeclaration teamDeclaration = model.getAst();
        if (teamDeclaration != null) {
            if ((teamDeclaration.binding == null) &&
                (model.getState() > STATE_ROLES_SPLIT))
            {
            	if (teamDeclaration.ignoreFurtherInvestigation)
            		return false; // irrecoverable
                throw new InternalCompilerError("binding needed"); //$NON-NLS-1$
            }
        }
		while (model.getState() < state)
		{
			int numMemberTypes = model.getNumRoles();

			int oldState  = model.getState();
            int nextState = oldState+1;

            if (   model.isIgnoreFurtherInvestigation()
				&& !StateHelper.isRequiredState(nextState)) // KEEPGOING
            {
            	StateHelper.setStateRecursive(model.getAst(), nextState, true);
				continue;
            }

            final int NONE         = 0;
            final int ROLES        = 1;
            final int NESTED_TEAMS = 2;
            int done = NONE;

            if (StateHelper.isCUDState(nextState, new int[]{STATE_METHODS_PARSED})) {
				// special case team-as-role:
				if (   model.isRole()
					&& model.getRoleModelOfThis().getState() >= nextState) {
						// already processed.
						model.setState(nextState);
						continue;
				}
            }

            if (!model._state.isReadyToProcess(nextState))
            	return false;
            model._state.startProcessing(nextState);

            switch (nextState) // what should be done next?
			{
				case STATE_ROLES_SPLIT:
					fixEnclosingType(model);
					success = establishRolesSplit(model);
                    done = NESTED_TEAMS;
					break;
                case STATE_ROLE_HIERARCHY_ANALYZED:
               		success = establishRoleHierarchy(model);
                    done = ROLES;
                    break;
                case STATE_FULL_LIFTING:
            		success = establishFullLifting(model);
                    done = ROLES;
                    break;
                case STATE_TYPES_ADJUSTED:
                    success = establishTypesAdjusted(model);
                    done = NONE; // also process roles!
                    break;
                case STATE_STATEMENTS_TRANSFORMED:
                    success = establishStatementsTransformed(model);
                    done = NESTED_TEAMS;
                    break;
                case STATE_CALLINS_TRANSFORMED:
                	success = establishCallinsTransformed(model);
                	done = NONE;
                	break;
                case STATE_LATE_ELEMENTS_COPIED:
                	success = establishLateElementsCopied(model);
                	done = NONE;
                	break;
                case STATE_FAULT_IN_TYPES:
                	success = establishFaultInTypes(model);
                	done = NESTED_TEAMS;
                	break;
                case STATE_ROLE_INIT_METHODS:
					if (teamBinding.isRole())
						ensureRoleState(teamBinding.roleModel, nextState);
					done = NONE;
                	break;
                case STATE_METHODS_CREATED:
                	success = establishMethodsCreated(model);
                	done = NONE;
                	break;
                // Delegate up:
                case STATE_METHODS_PARSED:
                	// special case: delegate up to unit and also seek role units:

                	// Note(SH): don't use ensureUnitState which needs to recurse into ensureAstState,
                	//           and then we can't easily decide how much processing has already happened.
                	CompilationUnitDeclaration unit = getCUD(model);
                	if (unit != null && unit.parseMethodBodies)
                        Config.delegateGetMethodBodies(unit);

                	// redundantly mark: we still need to traverse members:
                    done = NONE;
                    break;
                case STATE_LATE_ATTRIBUTES_EVALUATED:
        			// more attributes after fields and methods are in place:
        			// (notably precedence must be evaluated before resolving)
        			model.evaluateLateAttributes(STATE_LATE_ATTRIBUTES_EVALUATED);
        			done = NONE;
        			break;
        		// ==== CUD-states except STATE_METHODS_PARSED:
                case STATE_LENV_CONNECT_TYPE_HIERARCHY:
                	if (teamBinding.isBinaryBinding()) {
                    	CopyInheritance.connectBinaryTSupers(model);
                		done= ROLES;
                		break;
                	}
                	//$FALL-THROUGH$ (for source types)
                case STATE_RESOLVED:
                case STATE_ROLE_FILES_LINKED:
                case STATE_BINDINGS_BUILT:
				case STATE_LENV_BUILD_TYPE_HIERARCHY:
				case STATE_LENV_CHECK_AND_SET_IMPORTS:
				case STATE_LENV_DONE_FIELDS_AND_METHODS:
                case STATE_METHODS_VERIFIED:
                case STATE_CODE_ANALYZED:
                case STATE_BYTE_CODE_GENERATED:
                    // always start from the unit
                    success = ensureUnitState(model, nextState);
                    done = NESTED_TEAMS; // recursion is included.
                    break;
			}
			if (!success)
				return false;
            if (done < NESTED_TEAMS)
            {
                // pass some states down to our roles.
                // This currently affects
            	// + ROLES_LINKED,
                // + ROLE_FEATURES_COPIED
            	// + ROLE_HIERARCHY_ANALYZED
            	// + TYPES_ADJUSTED
                // + STATEMENTS_TRANSFORMED
                // + MAPPINGS_RESOLVED
                // + MAPPINGS_TRANSFORMED
            	// + LATE_ELEMENTS_COPIED
                // + BYTE_CODE_PREPARED

                RoleModel[] roles = model.getRoles(true); // includeSynthIfcs
                if(roles != null) {
                    for (int idx = 0; idx<roles.length; idx++) {
                    	if (done == NONE) {
                    		success &= ensureRoleState(roles[idx], nextState);
                    	} else { // did roles, but not as nested teams.
                    		if (roles[idx].isTeam()) {
                    			success &= ensureTeamState(roles[idx].getTeamModelOfThis(), nextState);
                    			// only now this class if fully processed, mark it:
                    			roles[idx].setState(nextState);
                    		}
                    	}
                    }
                }
                model.setState(nextState);
            }

            if (model.getNumRoles() > numMemberTypes)
            	lateRolesCatchup(model);

            if (model.getState() == oldState)
                throw new InternalCompilerError("Translation (team) does not advance, fails to establish state "+ITranslationStates.STATE_NAMES[oldState+1]); //$NON-NLS-1$
		}
		return true;
	}

	/**
	 * @param model
	 */
	public static boolean ensureRoleState(RoleModel model, int state) {
		boolean success = true;
		while (model.getState() < state)
		{
            int oldState  = model.getState();
            int nextState = oldState+1;

            if (   model.isIgnoreFurtherInvestigation()
            	&& !StateHelper.isRequiredState(nextState)) // KEEPGOING
			{
            	StateHelper.setStateRecursive(model.getAst(), nextState, true);
            	continue;
            }

            model._state.startProcessing(nextState);

            if (model._isLocalType)
            {
    			// some transformations are not needed for role nested types:
    			switch (nextState) {
    			case STATE_ROLE_FILES_LINKED:
    			case STATE_ROLES_SPLIT:
    			case STATE_BINDINGS_BUILT:
    			case STATE_LENV_BUILD_TYPE_HIERARCHY:
    			case STATE_LENV_CHECK_AND_SET_IMPORTS:
    			case STATE_LENV_CONNECT_TYPE_HIERARCHY:
    			case STATE_METHODS_PARSED:
    			//case STATE_ROLE_INIT_METHODS:
    			case STATE_ROLE_HIERARCHY_ANALYZED:
    			case STATE_FULL_LIFTING:
    			case STATE_CALLINS_TRANSFORMED:
                case STATE_METHODS_VERIFIED:
    			case STATE_RESOLVED:
    				model.setState(nextState);
    				success &= true;
    				continue; // go to next state
    			case STATE_LENV_DONE_FIELDS_AND_METHODS:
	    			{
	    				// FIXME(SH): treat other states like this, too??
	    				//            use ensureLateRoleUnitState here, too? (if isLateRole(model)).
                		TypeDeclaration roleDecl = model.getAst();
                		if (roleDecl != null && roleDecl.scope != null)
		    				roleDecl.scope.buildFieldsAndMethodsForLateRole();
	    				model.setState(nextState);
	    				continue;
	    			}
    			case STATE_STATEMENTS_TRANSFORMED:
    				success &= establishStatementsTransformed(model);
    				continue;
    			}
    		}

			switch (nextState) // what should be done next?
			{
				case STATE_ROLES_SPLIT:
					success &= establishRoleSplit(model);
					break;
				case STATE_ROLES_LINKED:
					RoleSplitter.linkScopes(model);
					RoleSplitter.linkSuperAndBaseInIfc(model);
                    success &= true; // redundant
                    model.setState(STATE_ROLES_LINKED);
					break;
				case STATE_ROLE_INIT_METHODS:
					success &= establishRoleInitializationMethod(model);
					break;
                case STATE_ROLE_FEATURES_COPIED:
                    success &= establishRoleFeaturesCopied(model);
                    break;
                case STATE_FAULT_IN_TYPES:
                	success &= establishFaultInTypes(model);
                	break;
                case STATE_METHODS_CREATED:
                    success &= establishMethodsCreated(model);
                    break;
                case STATE_TYPES_ADJUSTED:
                    success &= establishTypesAdjusted(model);
                    break;
                case STATE_CALLINS_TRANSFORMED:
                	success &= establishCallinsTransformed(model);
                	break;
                case STATE_LATE_ELEMENTS_COPIED:
                	success &= establishLateElementsCopied(model);
                	break;
                case STATE_BYTE_CODE_PREPARED:
                    success &= establishByteCodePrepared(model);
                    break;
                case STATE_ROLE_HIERARCHY_ANALYZED:
                	if (isLateRole(model, nextState))
                		success &= establishRoleHierarchy(model);
                	else
                    	success &= ensureTeamState(model.getTeamModel(), nextState);
                	break;
                case STATE_FULL_LIFTING:
                	if (isLateRole(model, nextState))
                		success &= establishFullLifting(model);
                	else
                    	success &= ensureTeamState(model.getTeamModel(), nextState);
                	break;
                case STATE_STATEMENTS_TRANSFORMED:
                	if (isLateRole(model, nextState))
                		success &= establishStatementsTransformed(model);
                	else
                    	success &= ensureTeamState(model.getTeamModel(), nextState);
                    break;
                case STATE_LATE_ATTRIBUTES_EVALUATED:
        			// more attributes after fields and methods are in place:
        			// (notably copyInheritanceSrc must be evaluated before resolving)
        			model.evaluateLateAttributes(STATE_LATE_ATTRIBUTES_EVALUATED);
        			break;
        		// ==== CUD-states (one special case up-front):
                case STATE_BYTE_CODE_GENERATED:
            		{
                		TypeDeclaration roleDecl = model.getAst();
                		if (roleDecl != null && roleDecl.isRoleFile()) {
                			// generate code from role file requires enclosing class file
                			success &= ensureAstState(roleDecl.enclosingType, nextState);
                			if (success && model.getState() == STATE_BYTE_CODE_GENERATED)
                				break;
                			// else fall through: try via the role unit.
                		}
                	}
            		//$FALL-THROUGH$
                case STATE_ROLE_FILES_LINKED:
				case STATE_BINDINGS_BUILT:
				case STATE_LENV_BUILD_TYPE_HIERARCHY:
				case STATE_LENV_CHECK_AND_SET_IMPORTS:
				case STATE_LENV_CONNECT_TYPE_HIERARCHY:
				case STATE_LENV_DONE_FIELDS_AND_METHODS:
				case STATE_METHODS_PARSED:
                case STATE_METHODS_VERIFIED:
                case STATE_RESOLVED:
                case STATE_CODE_ANALYZED:
                	if (   isLateRole(model, nextState)
                		&& !model.isLocalType())
                	{
                		success &= ensureLateRoleUnitState(model, nextState);
                	} else {
                		// always start from the unit (possibly: role unit):
                		TypeDeclaration roleDecl = model.getAst();
                    	if (roleDecl != null && roleDecl.isRoleFile()) {
                    		success &= (ensureState(roleDecl.compilationUnit, null, nextState) == Success.OK);
                    		model.setState(nextState);
                    		model.setMemberState(nextState);
                    		break;
                    	} else if (nextState == STATE_METHODS_PARSED) {
                    		// special case: mark this as done but also delegate up to unit:
                    		model.setState(nextState);
                    	}
                    	success &= ensureUnitState(model, nextState);
                	}
                    break;
			}
			if (   nextState <= STATE_ROLES_SPLIT
				|| nextState >= STATE_RESOLVED) {
				// don't process local types before resolve()!
				Iterator<RoleModel> localTypes = model.localTypes();
				while (localTypes.hasNext()) {
					RoleModel local = localTypes.next();
					if (!ensureRoleState(local, nextState))
						return false; // TODO(SH): KEEPGOING?
				}
			}
			if (model.isTeam())
				ensureTeamState(model.getTeamModelOfThis(), nextState);
            if (model.getState() == oldState) {
            	if (!model._state.isReadyToProcess(oldState+1))
            		return false; // no success yet, but via _currentlyProcessingState we still have hope ;-)
                throw new InternalCompilerError("translation (role) does not advance, fails to establish state "+ITranslationStates.STATE_NAMES[oldState+1]); //$NON-NLS-1$
            }
		}
		return true;
	}

	private static boolean ensureLateRoleUnitState(RoleModel model, int nextState) {
		// a unit-state has been requested for a late role
		TypeDeclaration roleDecl = model.getAst();
		if (roleDecl != null) {
			TypeDeclaration teamDecl = roleDecl.enclosingType;
			switch (nextState) {
			case STATE_ROLE_FILES_LINKED: // handled explicitly when role files are found
				break;
			case STATE_LENV_DONE_FIELDS_AND_METHODS:
				if (roleDecl.scope != null) {
					roleDecl.scope.checkParameterizedTypeBounds();
					roleDecl.scope.checkParameterizedSuperTypeCollisions();
            		if (Config.getBuildFieldsAndMethods())
            			roleDecl.scope.buildFieldsAndMethodsForLateRole();
				}
				break;
			case STATE_RESOLVED:
				if (teamDecl != null && teamDecl.scope != null)
					roleDecl.resolve(teamDecl.scope);
				break;
			case STATE_CODE_ANALYZED:
				if (teamDecl != null && teamDecl.scope != null)
					if (Config.getAnalyzeCode())
						roleDecl.analyseCode(teamDecl.scope);
				break;
			default:
				if (roleDecl.isRoleFile()) {
					CompilationUnitDeclaration unit = roleDecl.compilationUnit;
					Pair<Boolean,Success> result = establishUnitState(unit, nextState, Success.OK, null);
					Success success = result.second;
					if (success != Success.NotReady)
						StateHelper.setStateRecursive(unit, nextState, false);
					return success == Success.OK;
				}
			}
		}
		model.setState(nextState);
		model.setMemberState(nextState);
		return true;
	}

	private static boolean isLateRole(RoleModel model, int nextState) {
		if (   model.getTeamModel().getState() >= nextState
			&& model.getState() < nextState)
		{
       		return true;
    	}
		return false;
	}

	/** Process all late roles of teamModel, to have the same state as teamModel. */
	public static void lateRolesCatchup(TeamModel teamModel) {
		if (teamModel.getBinding() == null)
			return; // too early to catch up.

// TODO(SH): enable when needed:
//		CopyInheritance.sortRoles(teamModel.getBinding());
		ReferenceBinding[] memberTypes = teamModel.getBinding().memberTypes();
		int startState = StateHelper.minimalState(memberTypes)+1;
		// loop over all relevant states, to ensure consistent processing of class and ifc part:
		for (int nextState = startState; nextState<=teamModel._state.getProcessingState(); nextState++) {
			if (nextState == ITranslationStates.STATE_FINAL)
				break; // don't try to reach STATE_FINAL via ensureRoleState.
			for (int i = 0; i < memberTypes.length; i++) {
				ReferenceBinding memberType = memberTypes[i];
				if (memberType.isRole()) {// could be enum
					RoleModel roleModel = memberType.roleModel;
						// only process, if no processing is in progress:
						if (StateHelper.isReadyToProcess(roleModel, nextState))
							ensureRoleState(roleModel, nextState);
						else
							roleModel._state.requestState(roleModel.getAst(), nextState);
				}
			}
		}
		// has this team already resolved its regular memberTypes? catch up:
		if (   teamModel.getState() == ITranslationStates.STATE_RESOLVED-1
			&& teamModel._state.hasMethodResolveStarted())
		{
			ClassScope scope = teamModel.getAst().scope;
			for (int i = 0; i < memberTypes.length; i++) {
				ReferenceBinding memberType = memberTypes[i];
				if (memberType.isRole() && memberType.isClass()) {// could be enum
					RoleModel classModel = memberType.roleModel;
					if (classModel != null && classModel.getAst() != null && classModel.getAst().isRoleFile())
						if (classModel._state.getProcessingState() < ITranslationStates.STATE_RESOLVED)
							classModel.getAst().resolve(scope);
				}
			}
		}
	}

	public static boolean needMethodBodies(TypeDeclaration type) {
		if (type.scope == null)
			return false;
		if (type.binding != null && (type.binding.tagBits & TagBits.HierarchyHasProblems) != 0)
			return false; // when type hierarchy is inconsistent expect grave errors, don't generate bodies.
		return type.scope.referenceCompilationUnit().parseMethodBodies;
	}
    /**
     * Process a regular class.
     * @param model
     */
    private static boolean ensureTypeState(TypeModel model, int state) {
		boolean success = true;
        while (model.getState() < state)
        {
            int oldState  = model.getState();
            int nextState = oldState+1;

            if (   model.isIgnoreFurtherInvestigation()
                	&& !StateHelper.isRequiredState(nextState)) // KEEPGOING
    		{
               	StateHelper.setStateRecursive(model.getAst(), nextState, true);
               	continue;
            }

            switch (nextState) // what should be done next?
            {
                case STATE_STATEMENTS_TRANSFORMED:
                    establishStatementsTransformed(model);
                    // currently only reason for invoking this for all types:
                    // wrap QualifiedAllocationExpression for potential
                    // replacement by creator-method-invocation.
                    success = ensureMembersState(model, state);
                    break;
                case STATE_FAULT_IN_TYPES:
                	success = establishFaultInTypes(model);
                	break;
                case STATE_NONE:
                case STATE_ROLES_SPLIT:
                case STATE_ROLES_LINKED:
                case STATE_ROLE_INIT_METHODS:
                case STATE_ROLE_FEATURES_COPIED:
                case STATE_ROLE_HIERARCHY_ANALYZED:
                case STATE_FULL_LIFTING:
                case STATE_METHODS_CREATED:
                case STATE_TYPES_ADJUSTED:
				case STATE_CALLINS_TRANSFORMED:
				case STATE_LATE_ATTRIBUTES_EVALUATED:
				case STATE_LATE_ELEMENTS_COPIED:
                case STATE_BYTE_CODE_PREPARED:
                    success = ensureMembersState(model, state);
                    model.setState(nextState);
                    break;
                // ==== CUD-states: delegate up:
                case STATE_ROLE_FILES_LINKED:
                case STATE_BINDINGS_BUILT:
				case STATE_LENV_BUILD_TYPE_HIERARCHY:
				case STATE_LENV_CHECK_AND_SET_IMPORTS:
				case STATE_LENV_CONNECT_TYPE_HIERARCHY:
				case STATE_LENV_DONE_FIELDS_AND_METHODS:
				case STATE_METHODS_PARSED:
                case STATE_METHODS_VERIFIED:
                case STATE_RESOLVED:
                case STATE_CODE_ANALYZED:
                case STATE_BYTE_CODE_GENERATED:
                    success = ensureUnitState(model, nextState);
                    // special case (two way navigation up&down): mark as visited:
                    if (nextState == STATE_METHODS_PARSED)
                    	model.setState(STATE_METHODS_PARSED);
                    // local types need to catch up:
                    if (nextState <= STATE_RESOLVED && model.getBinding().isLocalType())
                    	model.setState(nextState);
                    break;
            }
            if (!success)
                return false;
            if (model.getState() <= oldState)
                throw new InternalCompilerError("Translation (type) does not advance past "+ITranslationStates.STATE_NAMES[oldState]); //$NON-NLS-1$
        }
        return true;
    }

    /**
     * If model has members, ensure that all of them have state 'state'.
     */
    private static boolean ensureMembersState(TypeModel model, int state) {
        TypeModel[] members = model.getMembers();
        boolean success = true;
        for (int i=0; i<members.length; i++) {
            if (members[i].isTeam())
                success &= ensureTeamState((TeamModel)members[i], state);
            else
                success &= ensureTypeState(members[i], state);
            // member cannot be a role since model is not a team.
        }
        return success;
    }

    /**
	 * @param teamModel traverse members of this team and link them to the correct enclosing
	 */
	private static void fixEnclosingType(TeamModel teamModel)
	{
		TypeDeclaration teamDeclaration = teamModel.getAst();
		if (teamDeclaration == null)
			return;

		// bugfix: enclosingType is null in case of erroneous sourcecode
		// Comment(SH): what kind of error?
		// Candidate locations for setting enclosing type are:
		//   + Parser.dispatchDeclarationInto() -- regular location
		//   + Parser.parseRoles()              -- regular location
		//   + SourceTypeConverter.convert()    -- currently asserting enclosingType != null

		TypeDeclaration[] roles = teamDeclaration.memberTypes;
		if (roles != null)
		{
			for (int i = 0; i<roles.length; i++)
			{
			    roles[i].enclosingType = teamDeclaration;
			}
		}
	}

    /**
     * ROFI link teams and their roles.
     */
	private static void establishRoleFilesLinked(
		CompilationUnitDeclaration unit,
		LookupEnvironment          environment)
	{
	    if (unit.types == null)
	        return;
		for (int j = 0; j < unit.types.length; j++) {
			if (unit.types[j].isRole()) {
				if (unit.types.length != 1) {
					if (Config.getConfig().client.getClass() == Compiler.class) // exact match.
					{
						environment.problemReporter.roleFileMustDeclareOneType(unit);
						return;
					}
				}
				if (unit.types[j].enclosingType == null)
					RoleFileHelper.getTeamOfRoleFile(unit, unit.types[j], environment);
			}
		}
		unit.compilationResult.roleFileDepth = unit.types[0].getRoleFileDepth();
	}

	/* **** STATE_ROLES_SPLIT (OT/J) ****
	 * Split roles into a classpart and an interface part.
	 * - No bindings yet.
	 * - RoleModels are created and connected to the TeamModel and to both AST-parts.
	 *
	 * GENERATES:
	 * - interface parts and some links
	 */

	/**
     * This method only operates on AST.
     * It combines
     * - role splitting with
     * - connecting a role to its team.
     *
     * If we only have bindings,
     * - roles are already split
     * - the team is retrieved via getTeamModel().
     */
	private static boolean establishRolesSplit(TeamModel teamModel)
	{
		boolean success = true;
        RoleModel[] roles = teamModel.getRoles(true);
        if (teamModel.getAst() != null && roles != null)
        {
            for (int i=0; i<roles.length; i++)
            {
                TypeDeclaration role = roles[i].getAst();
                assert (role != null);
                role.getRoleModel(teamModel); // register team
                if (!establishRoleSplit(roles[i], teamModel, role)) {
                	success = false;
                	break;
                }

                if (role.isTeam())
                	ensureTeamState(role.getTeamModel(), STATE_ROLES_SPLIT);
            }
        }
		teamModel.setState(STATE_ROLES_SPLIT);
		return success;
	}

    /**
     * Split this role into interface part and class part.
     * BinaryTypes: Nothing to do.
     */
	private static boolean establishRoleSplit(RoleModel model)
	{
		TypeDeclaration roleDecl = model.getAst();
        if (roleDecl != null) {
            assert (roleDecl.enclosingType != null); // once we had a bug with unset enclosingType

            return establishRoleSplit(model, model.getTeamModel(), roleDecl);
        }
        model.setState(STATE_ROLES_SPLIT);
        return true;
	}

    /**
     * Split this role into interface part and class part.
     * PRE: only called on AST-based RoleModel, i.e., (roleDecl != null)
     */
	private static boolean establishRoleSplit(
			RoleModel       model,
			TeamModel       teamModel,
			TypeDeclaration roleDecl)
	{
		if (TypeAnalyzer.isOrgObjectteamsTeam(teamModel.getBinding()))
		{
			model.setState(STATE_ROLES_SPLIT);
			return true;
		}
		// TODO(SH): check whether role splitting should be disabled for ALL local types.
		if (   !roleDecl.isGenerated
			&& roleDecl.name.length != 0) // don't split anonymous type
		{ // else nothing to do
			TypeDeclaration teamType = teamModel.getAst();
			TypeDeclaration interfacePart;
	        RoleModel partModel;

	        if (!roleDecl.isInterface()) { // nothing to split for interfaces
	            interfacePart  = RoleSplitter.createInterfacePart(teamType, roleDecl);
	    		partModel = interfacePart.getRoleModel(teamModel);
	            partModel.setState(STATE_ROLES_SPLIT);
	            partModel._interfacePart = interfacePart;
	            partModel._classPart = roleDecl;

	        	RoleSplitter.transformClassPart(teamType, roleDecl);
	            partModel = roleDecl.getRoleModel(teamModel);
	            partModel.setState(STATE_ROLES_SPLIT);
	            partModel._interfacePart = interfacePart;
	            partModel._classPart = roleDecl;

	            partModel.checkClassAndIfcParts();
	        }
        }
		model.setState(STATE_ROLES_SPLIT);
		return true;
	}

	/* **** STATE_BINDINGS_BUILD (JAVA) ****
	 * => LookupEnvironment.internalBuildTypeBindings()
	 * + many changes in CompilationUnitScope (ROFI), ClassScope (callinCallouts, baseclass)
	 *
	 * **** STATE_BINDINGS_COMPLETED (JAVA) ****
	 * => LookupEnvironment.internalCompleteTypeBindings()
	 *
	 * OT/J hooked in:
	 * + checkReadKnownRoles(..)
	 *   Read role files according to the role file cache, either directly from here
	 *   or hooked into LookupEnvironment.completeTypeBindings()
	 *
	 * Attention: STATE_BINDINGS_COMPLETE gives no guarantee before _all_ CUDs have
	 *            been processed!
	 */

	public static void checkReadKnownRoles(CompilationUnitDeclaration unit) {
		if (unit.types != null)
			for (int i = 0; i < unit.types.length; i++)
				checkReadKnownRoles(unit.types[i]);
	}

	private static void checkReadKnownRoles(TypeDeclaration type) {
		if (type.isTeam())
			type.getTeamModel().readKnownRoleFiles();
		if (type.memberTypes != null)
			for (int i = 0; i < type.memberTypes.length; i++)
				checkReadKnownRoles(type.memberTypes[i]);
	}

	 /*
	 * **** STATE_ROLES_LINKED (OT/J) ****
	 * ifcPart.scope.parent = classPart.scope in order to share nested types in particular:
	 * => RoleSplitter.linkScopes()
	 * Transfer playedBy and extends links to the interface part:
	 * => RoleSplitter.linkSuperAndBaseInIfc()
	 *
	 * **** STATE_METHODS_PARSED (JAVA) ****
	 * => Parser.getMethodBodies()
	 */

	/* **** STATE_ROLE_INIT_METHODS (OT/J) ****
	 * Create a method that holds all field initializations for a role.
	 *
	 * GENERATES:
	 * - _OT$InitFields()
	 */

	/**
     * BinaryTypes: nothing to do.
     */
	private static boolean establishRoleInitializationMethod(RoleModel role)
	{
		// must be done before role features copy
		TypeDeclaration roleClassDeclaration = role.getAst();
		if (   roleClassDeclaration != null
			&& !roleClassDeclaration.isInterface()
			&& needMethodBodies(roleClassDeclaration))
		{
			RoleInitializationMethod.setupRoleInitializationMethod(role);
		}

		role.setState(STATE_ROLE_INIT_METHODS);
		return true;
	}

	/* **** STATE_ROLE_FEATURES_COPIED (OT/J) ****
     * Copy all direct features (methods/ctors/fields/) from tsuper roles.
     * Operate on AST and bindings. Link bindings of copied methods to
     * their copyInheritanceSrc.
	 *
	 * GENERATES:
	 * - fields from tsuper
	 * - place-holders (empty AST) for methods from tsuper.
	 */

    /**
     * After copying remember how many fields/methods have been copied,
     * in order to add copies of generated features later.
     *
     * Note: Local types will be copied for STATE_LATE_ELEMENTS_COPIED
     *
     * BinaryTypes and marker interfaces: nothing to do.
     */
    private static boolean establishRoleFeaturesCopied(RoleModel role)
    {
        TypeDeclaration roleType = role.getAst();
        if (   roleType != null
        	&& needMethodBodies(roleType))
        {
            if (OTNameUtils.isTSuperMarkerInterface(roleType.name))
            {
                role.setState(STATE_ROLE_FEATURES_COPIED);
                return true;
            }
            ReferenceBinding superRole = roleType.binding.superclass;
            if (   superRole != null
            	&& superRole.id != TypeIds.T_JavaLangObject)
            {
                // for signature weakening, as performed by copyMethod,
                // we must first process the super role (extends):
                if (superRole.isRole()) {
                    if (!ensureRoleState(superRole.roleModel, STATE_ROLE_FEATURES_COPIED))
                    {
                        role.setState(STATE_ROLE_FEATURES_COPIED);
                        return false;
                    }
                }
            }

            ReferenceBinding[] tsupers = role.getTSuperRoleBindings();
            for (int i = 0; i < tsupers.length; i++) {
	            ReferenceBinding tsuperRoleBinding = tsupers[i];
	            if((tsuperRoleBinding != null)
	                && (!tsuperRoleBinding.isInterface()))
	            {
	            	// prepare for transitive copying:
	            	// (later added features are copied via RoleModel.setState()->CopyInheritance.copyGeneratedFeatures())
	                if (!ensureTeamState(tsuperRoleBinding.roleModel.getTeamModel(), STATE_ROLE_FEATURES_COPIED))
	                {
	                    role.setState(STATE_ROLE_FEATURES_COPIED);
	                    roleType.tagAsHavingErrors(); // missing methods from tsuper.
	                    return false;
	                }
	                // to use transitive copyInheritanceSrc tsuper must have that attribute evaluated.
	                ModelElement.evaluateLateAttributes(tsuperRoleBinding, STATE_ROLE_FEATURES_COPIED);
	                if (role.getState() < STATE_ROLE_FEATURES_COPIED)
	                	CopyInheritance.copyFeatures(role, tsuperRoleBinding);
	            }
			}
        }

        role.setState(STATE_ROLE_FEATURES_COPIED);
        return true;
    }

    /* **** STATE_ROLE_HIERARCHY_ANALYZED (OT/J) ****
     * - Create role hierarchy for LiftingEnvironment
     * - translate declared lifting
     * - check overriding of blaseclass
     *
     * GENERATES:
     * - field: _OT$base
     * - methods: _OT$getBase, lower, liftTo ctor
     * REMOVE:
     * - default ctor for bound role
     */

    /**
     * BinaryTypes: nothing to do.
     *
     * Recurse: ROLES (no NESTED_TEAMS)
     */
    private static boolean establishRoleHierarchy(TeamModel model) {
        TypeDeclaration teamDeclaration = model.getAst();
        if (   teamDeclaration != null
        	&& teamDeclaration.memberTypes != null)
        {
        	// LiftingEnvironment.filterBoundRootRoles() and
            // checkRefineBaseFromSuperInterfaces() need baseclass to be resolved,
            // which may not happen before role inheritance:
            ReferenceBinding superTeam = model.getBinding().superclass();
            if (superTeam != null)
            	ensureBindingState(superTeam, STATE_LENV_CONNECT_TYPE_HIERARCHY);

            boolean needMethodBodies = needMethodBodies(teamDeclaration);
            // Order: this one is likely to load role files ...
            DeclaredLifting.transformMethodsWithDeclaredLifting(teamDeclaration, needMethodBodies); // includes gen lift_dynamic

            ReferenceBinding[] memberTypes = model.getBinding().memberTypes();
            for (int i = 0; i < memberTypes.length; i++)
				memberTypes[i].methods(); // methods are needed during createLiftToCtor, can also trigger ROFI loading

            // remember memberTypes now to set state only for those that
            // have undergone the translation below.
            int numMembers = model.getBinding().memberTypes().length;

            // after role inheritance is established, check whether baseclass is illegally overridden.
            memberTypes = model.getBinding().memberTypes();
            for (int i = 0; i < memberTypes.length; i++) {
            	if (!memberTypes[i].isBinaryBinding() && !memberTypes[i].isEnum())
            		((MemberTypeBinding)memberTypes[i]).checkRefineBaseFromSuperInterfaces();
            }

            // ... while these ones transform all roles known to this point.
            model.liftingEnv = new LiftingEnvironment(teamDeclaration); // creates the hierarchy
            model.liftingEnv.createRoleBaseLinkage(/*lateRole*/null, needMethodBodies);

            memberTypes = model.getBinding().memberTypes();
            if (numMembers < memberTypes.length)
            	throw new InternalCompilerError("Role file found at unexpected point: "+new String(memberTypes[numMembers].readableName())); //$NON-NLS-1$

            model.setState(STATE_ROLE_HIERARCHY_ANALYZED);
			for (int i = 0; i < memberTypes.length; i++) {
				if (!memberTypes[i].isTeam() && !memberTypes[i].isEnum())
					memberTypes[i].roleModel.setState(STATE_ROLE_HIERARCHY_ANALYZED);
                // Instead of setting member state for nested teams,
                // wait for ensureTeamState to descend into nested teams.
                // Each nested team then flags its roleModel, too.
			}
        } else {
        	// nothing to do for binary types and role-less teams.
        	model.setState(STATE_ROLE_HIERARCHY_ANALYZED);
        	model.setMemberState(STATE_ROLE_HIERARCHY_ANALYZED);
        }
        return true;
    }

    private static boolean establishRoleHierarchy(RoleModel model) {
        boolean success = true;
        TypeDeclaration roleDecl = model.getAst();
    	if (   roleDecl == null
    		|| !roleDecl.isDirectRole())
    	{
        	model.setState(STATE_ROLE_HIERARCHY_ANALYZED);
        	model.setMemberState(STATE_ROLE_HIERARCHY_ANALYZED);
        	return true;
    	}
    	// after role inheritance is established, check whether baseclass is illegally overridden.
    	if (model.isBound()) {
    		MemberTypeBinding roleBinding = (MemberTypeBinding)model.getBinding();
    		roleBinding.checkRefineBaseFromSuperInterfaces();
    	}

    	TeamModel teamModel = model.getTeamModel();
    	if (teamModel.liftingEnv == null)
    		teamModel.liftingEnv = new LiftingEnvironment(roleDecl.enclosingType);
    	else
    		teamModel.liftingEnv.init(roleDecl.enclosingType);
    	if (model.isBound())
    		teamModel.liftingEnv.createRoleBaseLinkage(model, needMethodBodies(roleDecl));


        model.setState(STATE_ROLE_HIERARCHY_ANALYZED);
        if (roleDecl.isTeam()) {
        	TeamModel teamModelOfThis = model.getTeamModelOfThis();
//        	if (teamModelOfThis.getState() == STATE_ROLE_HIERARCHY_ANALYZED-1)
        		success = establishRoleHierarchy(teamModelOfThis);
        }

        model.setMemberState(STATE_ROLE_HIERARCHY_ANALYZED);
        return success;
    }

    /* **** STATE_FULL_LIFTING (OT/J) ****
     * - Re-create role hierarchy for LiftingEnvironment (late roles only)
     * - Create lifting infrastructure (caches, methods, constructors)
     *
     * GENERATES:
     * - field: _OT$cache$R
     * - methods: _OT$liftToR, role-query-methods (reflection)
     */
    static boolean establishFullLifting(TeamModel model) {
        TypeDeclaration teamDeclaration = model.getAst();
        if (   teamDeclaration != null
        	&& teamDeclaration.memberTypes != null)
        {
            model.liftingEnv.createLiftingInfrastructure(null, needMethodBodies(teamDeclaration));
	       	model.setState(STATE_FULL_LIFTING);
	       	// shallow traversal only:
	       	for (RoleModel roleModel : model.getRoles(true))
				roleModel.setState(STATE_FULL_LIFTING);
        } else {
	       	// nothing to do for binary types and role-less teams.
	       	model.setState(STATE_FULL_LIFTING);
	       	model.setMemberState(STATE_FULL_LIFTING);
        }
        return true;
    }
    static boolean establishFullLifting(RoleModel model) {
    	boolean success = true;
        TypeDeclaration roleDecl = model.getAst();
    	if (   roleDecl == null
    		|| !roleDecl.isDirectRole())
    	{
        	model.setState(STATE_FULL_LIFTING);
        	model.setMemberState(STATE_FULL_LIFTING);
        	return true;
    	}
    	TeamModel teamModel = model.getTeamModel();
    	teamModel.liftingEnv.createLiftingInfrastructure(model, needMethodBodies(roleDecl));
        model.setState(STATE_FULL_LIFTING);
        return success;
    }
    /* **** STATE_FAULT_IN_TYPES (JAVA) ****
     * Finish resolving signatures, check for duplicates.
     * => CompilationUnitScope.faultInImports()
     * => SourceTypeBinding.faultInTypesForFieldsAndMethods()
     */

	/**
     * Doing fault in types here and not via CompilationUnitScope
     * allows us to process single types without processing the whole
     * compilation unit!
     * We also add some OT-specific transformantions:
     * - faultInRoleImports
     * - evaluateLateAttributes
	 */
	private static boolean establishFaultInTypes(TypeModel clazz) {
		TypeDeclaration ast = clazz.getAst();
		if (ast != null && ast.scope != null) {
			ast.scope.compilationUnitScope().faultInImports();
			SourceTypeBinding typeBinding = ast.binding;
			typeBinding.faultInTypesForFieldsAndMethods();
			faultInRoleImports(ast);
		}
		// more attributes after fields and methods are in place (CallXXMethodMappingAttribute)
		if (clazz.getBinding() != null) {
			ModelElement.evaluateLateAttributes(clazz.getBinding(), STATE_FAULT_IN_TYPES);
		} else {
			assert ast != null && ast.hasErrors();
		}

		clazz.setState(STATE_FAULT_IN_TYPES);
		clazz.setMemberState(STATE_FAULT_IN_TYPES);
		return true;
	}

	private static void faultInRoleImports(TypeDeclaration teamType) {
		if (!teamType.isTeam() || teamType.memberTypes == null)
			return;
		for (TypeDeclaration roleType: teamType.memberTypes) {
			if (roleType.scope instanceof OTClassScope)
				((OTClassScope)roleType.scope).faultInRoleFileImports();
			faultInRoleImports(roleType);
		}
	}
	
//        	if (teamDecl.isRole()) {
//        		ensureRoleState(teamDecl.getRoleModel(), STATE_TYPES_ADJUSTED);
//        	}

//        	TeamModel superTeam = teamModel.getSuperTeam();
//	        if (superTeam != null) {
//	            if (!ensureTeamState(superTeam, STATE_TYPES_ADJUSTED))
//	            {
//	            	teamDecl.tagAsHavingErrors();
//	                return;
//	            }
//	        }

//	        CopyInheritance.weakenTeamMethodSignatures(teamDecl);

	/* **** STATE_METHODS_CREATED (OT/J) ****
	 * - generate methods relating to roles and implicit inheritance.
	 *
	 * GENERATES:
	 * For team:
	 * 1. copy synthetic access methods
	 * For role:
	 * 0. Binary roles: bindings for methods to be created by the OTRE 
	 * 1. creation methods
     * 2. getTeam methods
	 * 3. add method from non-role superclasses to the interface part.
	 * 4. cast methods (calls getTeam method)
	 * 5. abstract _OT$getBase() method for unbound lowerable role
	 * 6. callout methods
	 */
	private static boolean establishMethodsCreated(TeamModel teamModel) {
		if (teamModel.getBinding().isRole())
			ensureRoleState(teamModel.getRoleModelOfThis(), STATE_METHODS_CREATED);
		ReferenceBinding superTeam = teamModel.getBinding().superclass();
        // synthetics from binary super team (binary has synthetics right from the beginning):
		// Note: for source super team this is done in TypeDeclaration.generateCode().
        if (teamModel.getAst() != null &&  superTeam.isBinaryBinding())
        	// (requires role features copied of our roles in order to map the fields)
        	CopyInheritance.copySyntheticTeamMethods(teamModel, (BinaryTypeBinding)superTeam);
        if (teamModel.getAst() != null) {
			if (   !TypeAnalyzer.isOrgObjectteamsTeam(teamModel.getBinding())
				&& !teamModel.getBinding().superclass().isTeam())
			{
				LookupEnvironment env = teamModel.getAst().scope.environment();
				env.getTeamMethodGenerator().addMethodsAndFields(teamModel.getAst());
			}
        }
		return true;
	}

    private static boolean establishMethodsCreated(RoleModel clazz)
    {
        TypeDeclaration  teamType        = clazz.getTeamModel().getAst();
        ReferenceBinding subRole         = clazz.getBinding();
        TypeDeclaration  subRoleDecl     = clazz.getAst();

        if (subRole == null) { // extra caution, none of the code below would work
            clazz.setState(STATE_METHODS_CREATED);
        	return false;
        }

        if (   OTNameUtils.isTSuperMarkerInterface(clazz.getInternalName())
        	|| teamType == null)
        {
        	// 0. binary only: create OTRE-generated methods:
        	if (subRole.isBinaryBinding())
       			((BinaryTypeBinding)subRole).createOTREMethods(clazz);
            clazz.setState(STATE_METHODS_CREATED);
            return true; // nothing to do
        }

        SourceTypeBinding subTeam = teamType.binding;


        if (subRoleDecl == null)
        {
        	// 1. create creation methods from constructors (binding version):
        	//     TODO (SH): currently, this is only invoked for roles that are
        	//                explicitly mentioned in classes being compiled.
        	//                Need a place to store a list of previously compiled roles!
            if (   subRole.isClass()       // interfaces have not constructores, don't bother.
            	&& !subRole.isLocalType()) // local types need no creators, are only copied along with their containing method
            {
            	createCreators(clazz, teamType, subRole, subTeam);
            }
            // no steps 2.+3.
        }
        // Next translations only for source types (no role nested types)
        if (   subRoleDecl != null
        	&& subRole.isDirectRole())
        {
            // 1. create creation methods from constructors (AST version)
            if (subRole.isClass())  // interfaces have not constructores, don't bother.
            	createCtorsAndCreators(clazz, teamType, subRoleDecl, subTeam);

            // 2. create getTeam methods:
           	if (subRoleDecl.isInterface()) {
                	// process class and ifc at the same time, because otherwise an error
                	// somewhere along the line may cause inconsistency between them two.
                	StandardElementGenerator.createGetTeamMethod(subRoleDecl);
                	TypeDeclaration classPart = clazz.getClassPartAst();
                	if (classPart != null)
                    	StandardElementGenerator.createGetTeamMethod(classPart);
            }
            // 3. add methods from non-role super-class to the ifc-part
            if (!subRole.isInterface())
                RoleSplitter.setupInterfaceForExtends(clazz.getTeamModel().getAst(), subRoleDecl, clazz.getInterfaceAst());
        }

        // 4. cast and getClass methods (independent of the source/binary difference):
        if (   subRole.isInterface()
        		&& (subRoleDecl == null || needMethodBodies(subRoleDecl)))
        {
        	TypeDeclaration sourceType = subRoleDecl != null ? subRoleDecl : teamType;
        	TypeDeclaration classPart = clazz.getClassPartAst();
        	if (classPart != null)
        		StandardElementGenerator.createCastMethod(clazz.getTeamModel(), classPart, /*dims*/ 0); // FIXME dimensions
        	else // difference is only in source positions used.
        		StandardElementGenerator.getCastMethod(clazz.getTeamModel(), subRole, teamType.scope, /*dims*/ 0, false, sourceType.sourceStart, sourceType.sourceEnd); // FIXME dimensions
        	if (subRole.isPublic())
        		RoleClassLiteralAccess.ensureGetClassMethod(teamType.getTeamModel(), clazz);
        }

        // 5. special case roles which need an abstract _OT$getBase() method:
		StandardElementGenerator.createGetBaseForUnboundLowerable(clazz);
        
		// 6. resolve method mappings and create callout methods:
		MethodMappingResolver resolver = resolveCalloutMappings(clazz);
		CalloutImplementor.transformCallouts(clazz);
		if (resolver != null)
			resolver.resolve(false/*doCallout*/); // callins last so all methods incl. callout are already in place
		
		if (subRoleDecl != null)
			checkMissingMethods(subRole, subRoleDecl);

        clazz.setState(STATE_METHODS_CREATED);
        return true;
    }

    // detail of STATE_METHODS_CREATED (binary case):
	private static void createCreators(RoleModel clazz, TypeDeclaration teamType, ReferenceBinding subRole, SourceTypeBinding subTeam) 
	{
		boolean needMethodBodies = needMethodBodies(teamType);
		MethodBinding[] methodBindings = subRole.methods();
		if (methodBindings != null)
		{
		    for (int i=0; i<methodBindings.length; i++)
		    {
		        if (methodBindings[i].isConstructor()) {
		            AbstractMethodDeclaration creator =
		                CopyInheritance.createCreationMethod(
		                        teamType,
		                        clazz,
		                        null, // ConstructorDeclaration
		                        methodBindings[i],
								needMethodBodies);
		            if (   creator != null
		            	&& !creator.ignoreFurtherInvestigation)
		            {
		                subTeam.resolveGeneratedMethod(creator.binding);
		                // this also wraps the signature using wrapTypesInMethodDeclSignature
		            }
		        }
		    }
		}
	}

	// detail of STATE_METHODS_CREATED (AST case):
	private static void createCtorsAndCreators(RoleModel clazz, TypeDeclaration teamType, TypeDeclaration subRoleDecl, SourceTypeBinding subTeam) 
	{
		// ensure we have all constructors from tsuper (incl. default ctor)
		for (ReferenceBinding tsuperRole : clazz.getTSuperRoleBindings())
			ensureBindingState(tsuperRole, ITranslationStates.STATE_METHODS_CREATED);
		CopyInheritance.copyGeneratedFeatures(clazz);

		boolean needMethodBodies = needMethodBodies(subRoleDecl);
		AbstractMethodDeclaration[] methodDeclarations = subRoleDecl.methods;
		// may need to create default constructor first:
		boolean hasConstructor = false;
		if (methodDeclarations != null)
		    for (int i=0; i<methodDeclarations.length; i++)
		    	if (methodDeclarations[i].isConstructor() && !TSuperHelper.isTSuper(methodDeclarations[i].binding)) {
		    		hasConstructor = true; 
		    		break; 
		    	}
		if (!hasConstructor) {
			ConstructorDeclaration defCtor = subRoleDecl.createDefaultConstructor(needMethodBodies, false);
			AstEdit.addMethod(subRoleDecl, defCtor);
			CopyInheritance.connectDefaultCtor(clazz, defCtor.binding);
			methodDeclarations = subRoleDecl.methods;
		}
		// now the creation methods for all constructors:
		if (methodDeclarations != null)
		{
		    for (int i=0; i<methodDeclarations.length; i++)
		    {
		        if (methodDeclarations[i].isConstructor()) {
		            ConstructorDeclaration constructor =
		                (ConstructorDeclaration)methodDeclarations[i];
		            MethodDeclaration creator =
		                CopyInheritance.createCreationMethod(
		                        teamType,
		                        clazz,
		                        constructor,
								constructor.binding, needMethodBodies);
		            if (   creator != null
		            	&& !creator.ignoreFurtherInvestigation)
		            {
		            	CopyInheritance.createCreatorIfcPart(subTeam, creator);
		                subTeam.resolveGeneratedMethod(creator.binding);
		                // this also wraps the signature using wrapTypesInMethodDeclSignature
		            }
		        }
		    }
		}
	}

	// detail of STATE_METHODS_CREATED:
	private static MethodMappingResolver resolveCalloutMappings(RoleModel role) 
	{
		ReferenceBinding roleBinding = role.getBinding();

		TypeDeclaration roleDecl = role.getAst();
		if (   roleDecl != null
			&& Config.getConfig().verifyMethods)
		{
			boolean hasBaseclassProblem = role.hasBaseclassProblem();
			
			if (!hasBaseclassProblem) {
				ReferenceBinding baseclass = role.getBaseTypeBinding();
				if (baseclass != null && !role._playedByEnclosing)
					// some methods accessible to callout/callin might be added during
					// callout transformation.
					ensureBindingState(baseclass, STATE_METHODS_CREATED);
			}
			ReferenceBinding[] tsuperRoles = role.getTSuperRoleBindings();
			for (int i = 0; i < tsuperRoles.length; i++) {
				// need the generated wrappers to determine abstractness of methods
				ensureBindingState(tsuperRoles[i], STATE_METHODS_CREATED);
			}
			// same reason as for tsuper roles:
			if (roleBinding.superclass() != null)
				ensureBindingState(roleBinding.superclass(), STATE_METHODS_CREATED);

			// make sure tsuper wrappers are copied to current role:
			CopyInheritance.copyGeneratedFeatures(role);
			
			// actually need to proceed even with no base class, because
			// method mappings without baseclass are reported within resolve() below:
			MethodMappingResolver resolver = new MethodMappingResolver(role, !hasBaseclassProblem && needMethodBodies(roleDecl));
			resolver.resolve(true/*doCallout*/);
			return resolver; // pass this resolver so establishMethodsCreated can continue with resolving callins
		}
		return null;
	}

	// detail of STATE_METHODS_CREATED (can only check this after callouts have been transformed):
	private static void checkMissingMethods(ReferenceBinding roleBinding, TypeDeclaration roleDecl) {
		if (roleBinding.isClass() && ((roleBinding.tagBits & TagBits.AnnotationInstantiation) != 0)) {
			InstantiationPolicy instantiationPolicy = RoleModel.getInstantiationPolicy(roleBinding);
			if (!instantiationPolicy.isAlways())
				return;
			ClassScope scope = roleDecl.scope;
			boolean missing = false;
			MethodBinding equals = roleBinding.getExactMethod(TypeConstants.EQUALS, 
															  new TypeBinding[] {scope.getJavaLangObject()}, scope.compilationUnitScope());
			if (equals == null || !equals.isValidBinding() || equals.declaringClass != roleBinding) {
				missing = true;
			} else {
				MethodBinding hashCode = roleBinding.getExactMethod(TypeConstants.HASHCODE, 
						  										  	Binding.NO_PARAMETERS, scope.compilationUnitScope());
				if (hashCode == null || !hashCode.isValidBinding() || hashCode.declaringClass != roleBinding)
					missing = true;
			}
			if (missing) {
				scope.problemReporter().missingEqualsHashCodeWithInstantation(scope.referenceContext, instantiationPolicy);
			}
		}
		// copied abstract methods are not yet checked for callout inference (which is normally triggered from checkMethods()) 
		if (roleBinding.isClass() && roleDecl.methods != null) {
			for (AbstractMethodDeclaration method : roleDecl.methods) {
				if (((method.modifiers & ClassFileConstants.AccAbstract) != 0) && method.isCopied) {
		    		// inheriting abstract method in non-abstract role may require callout inference:
					CalloutImplementor coi = new CalloutImplementor(roleDecl.getRoleModel());
					if (coi.generateInferredCallout(roleDecl, method.binding))
						roleDecl.scope.problemReporter().addingInferredCalloutForInherited(roleDecl, method.binding);
				}
			}
		}
	}

	/* **** STATE_TYPES_ADJUSTED (OT/J) ****
	 * - wrap types in signatures using RoleTypeBinding
	 * - signature weakening
	 * - adjustments for declared lifting in team ctors
	 *
	 * GENERATES:
	 * - nothing
	 */
    /**
     * Signature weakening and adjustments for declared lifting in team ctors.
     * Type wrapping in methods.
     *
     * Recurse: NONE.
     */
    private static boolean establishTypesAdjusted(TeamModel teamModel)
    {
    	TypeDeclaration teamDecl = teamModel.getAst();
		if (teamDecl != null) {
			if (needMethodBodies(teamDecl))
				DeclaredLifting.prepareArgLifting(teamDecl);
        	if (teamDecl.isRole()) {
        		ensureRoleState(teamDecl.getRoleModel(), STATE_TYPES_ADJUSTED);
        	}

        	TeamModel superTeam = teamModel.getSuperTeam();
	        if (superTeam != null) {
	            if (!ensureTeamState(superTeam, STATE_TYPES_ADJUSTED))
	            {
	            	teamDecl.tagAsHavingErrors();
	                teamModel.setState(STATE_TYPES_ADJUSTED);
	                return false;
	            }
	        }

	        CopyInheritance.weakenTeamMethodSignatures(teamDecl);
	        
		}
		teamModel.setState(STATE_TYPES_ADJUSTED);
		return true;
    }

    /**
     * BinaryTypes: Steps 1 & 6 (signature wrapping & cast methods) only.
     */
    private static boolean establishTypesAdjusted(RoleModel clazz)
    {
        TypeDeclaration  teamType        = clazz.getTeamModel().getAst();
        ReferenceBinding subRole         = clazz.getBinding();
        TypeDeclaration  subRoleDecl     = clazz.getAst();

        if (   OTNameUtils.isTSuperMarkerInterface(clazz.getInternalName())
        	|| teamType == null)
        {
            clazz.setState(STATE_TYPES_ADJUSTED);
            return true; // nothing to do
        }

        SourceTypeBinding subTeam = teamType.binding;

        // Remaining translations only for source types (no role nested types)
        if (   subRoleDecl != null
        	&& subRole.isDirectRole())
        {

			// 2. Signature weakening
            if (subRole.isInterface())
            {
                ReferenceBinding tsuperRole = clazz.getTSuperRoleBinding();
                if (tsuperRole != null)
                {
                    CopyInheritance.weakenInterfaceSignatures(
                            tsuperRole,
                            subTeam,
                            subRoleDecl);
                }
            } else {
                CopyInheritance.weakenSignaturesFromSupers(
                			subRole,
                			subRoleDecl,
                			clazz.getInterfaceAst());
            }


        }
        clazz.setState(STATE_TYPES_ADJUSTED);
        return true;
    }

    /* **** STATE_STATEMENTS_TRANSFORMED (OT/J) ****
     * Translate:
     * - t.new R()
     * - tsuper()
     * - "result" within parameter mapping expressions
     * Link local types to their "enclosingType".
     *
     * GENERATES:
     * (no new elements, only changes at the statement level)
     */

	/**
     * Perform several substitutions in method bodies.
     * (See TransformStatementsVisitor).
     *
     * BinaryTypes: nothing to do.
     *
     * Recursion: NESTED_TEAMS.
     */
    private static boolean establishStatementsTransformed(TypeModel clazz)
    {
        TypeDeclaration type = clazz.getAst();
        if (type != null) {
        	if (needMethodBodies(type)) {
        		TransformStatementsVisitor transformer = new TransformStatementsVisitor();
        		if ((type.bits & ASTNode.IsLocalType) != 0) {
        			MethodScope methodScope = type.scope.methodScope();
        			if (methodScope != null)
        				transformer.checkPushCallinMethod(methodScope.referenceMethod());
        		}
        		type.traverse(transformer, type.scope.compilationUnitScope());
        	} else if (clazz.isTeam()) {
        		if (type.memberTypes != null) {
        			for (int i = 0; i < type.memberTypes.length; i++) {
						establishStatementsTransformed(type.memberTypes[i].getRoleModel());
					}
        		}
        	}
        	if (   needMethodBodies(type)
        		|| type.isConverted) // converted types may contain local types but no other statements!
        	{
        		RecordLocalTypesVisitor recorder = new RecordLocalTypesVisitor();
        		recorder.recordLocalTypesFor(type);
        	}
        }
        clazz.setState(ITranslationStates.STATE_STATEMENTS_TRANSFORMED);
        clazz.setMemberState(ITranslationStates.STATE_STATEMENTS_TRANSFORMED);
        return true;
    }

    /* **** STATE_CALLINS_TRANSFORMED (OT/J) ***
     * Generate the wrappers for callin bindings.
     *
     * GENERATES:
     * - callin wrappers incl. corresponding attributes
     * COPIES from super/tsuper:
     * - CallinMethodMappingsAttribute and StaticReplaceBindingsAttribute
     */
	private static boolean establishCallinsTransformed(TeamModel aTeam)
	{
		CopyInheritance.copyAttribute(aTeam);
//{OTDyn:
    	if (CallinImplementorDyn.DYNAMIC_WEAVING) {
    		CallinImplementorDyn callinImplementor = new CallinImplementorDyn();
    		callinImplementor.transformTeam(aTeam);
    	}
// SH}
		aTeam.setState(STATE_CALLINS_TRANSFORMED);
		return true;
	}

	private static boolean establishCallinsTransformed(RoleModel role)
	{
        boolean success = true;
        TypeDeclaration roleDecl = role.getAst();
		if (Config.getConfig().verifyMethods)
        { 
			if (roleDecl == null) {
				TeamModel teamModel = role.getTeamModel();
				if (teamModel != null && teamModel.getAst() != null) {
					TypeDeclaration teamDecl = teamModel.getAst();
					if (!teamDecl.isConverted && role.hasCallins()) {
						teamDecl.scope.problemReporter().notGeneratingCallinBinding(teamDecl, role);
						success = false;
					}
				}
			} else if (!roleDecl.isPurelyCopied) { // no source level bindings present any way
	        	boolean needMethodBodies = needMethodBodies(roleDecl) && !role.hasBaseclassProblem() && !role.isIgnoreFurtherInvestigation();
	        	if (!roleDecl.binding.isSynthInterface()) {
	        		// synth interfaces have no callins anyway ;-)
		            if (needMethodBodies) {  // not translating callin bindings will cause no secondary errors -> skip if no body needed
//{OTDyn:
		            	if (CallinImplementorDyn.DYNAMIC_WEAVING) {
		            		CallinImplementorDyn callinImplementor = new CallinImplementorDyn();
		            		callinImplementor.transformRole(role);
		            	} else {
// SH}
		            		CallinImplementor callinImplementor = new CallinImplementor(role);
		            		success &= callinImplementor.transform();
		            	}
		            }
	        	}
			}
        }
		// less preconditions for copying these attributes:
        CopyInheritance.copyAttribute(role);
		role.setState(STATE_CALLINS_TRANSFORMED);
		return success;
	}

	/* **** STATE_METHODS_VERIFIED (JAVA) ****
	 * Check visibility, correct overriding etc.
	 * => CompilationUnitScope.verifyMethods()
	 *   OT/J changes mostly in MethodVerifier
	 *
	 * **** STATE_RESOLVED (JAVA) ****
	 * Resolve everything, type-Checking.
	 * => CompilationUnitDeclaration.resolve()
	 *
	 * + Many OT/J changes throughout!
	 */

	/* **** STATE_LATE_ELEMENTS_COPIED ****
	 * Copy elements that were not available during regular copy inheritance.
	 * Includes copying of the CALLIN_METHOD_MAPPINGS attribute
	 *
	 * GENERATES copies of:
	 * - cast methods
	 * - get class methods (RoleClassLiteralAccess)
	 * - local types
	 */

    private static boolean establishLateElementsCopied(TeamModel model) {
    	// also copy and adjust castTo Methods and getClass methods
    	ensureBindingState(model.getBinding().superclass(), STATE_RESOLVED);
    	TypeDeclaration teamDecl = model.getAst();
		if (   teamDecl != null
			&& needMethodBodies(teamDecl))
		{
    		CopyInheritance.copyCastToAndGetClassMethods(teamDecl);
    		LiftingEnvironment.fillGeneratedMethods(teamDecl);
    		for (AbstractMethodDeclaration method : teamDecl.methods)
				if (method.isGenerated && method.model != null)
					method.model.generateStatements();
		}
    	model.setState(STATE_LATE_ELEMENTS_COPIED);
    	return true;
    }

    private static boolean establishLateElementsCopied(RoleModel model) {
    	boolean success = true;
    	TypeDeclaration roleDecl = model.getAst();
    	if (roleDecl != null) {
	    	success = CopyInheritance.copyLocalTypes(model);
	    	if (!roleDecl.isInterface())
	    	{
	    		if (roleDecl.methods != null)
	    			for (AbstractMethodDeclaration method : roleDecl.methods)
	    				if (method.isGenerated && method.model != null)
	    					method.model.generateStatements();

	    		if (model.isBound()) {
		    		MethodBinding unimplementedGetBase = model.getInheritedUnimplementedGetBase();
		    		if (unimplementedGetBase != null) {
		    			ReferenceBinding classBinding = model.getClassPartBinding();
		    			if (classBinding != null && !classBinding.isBinaryBinding()) {
		    				MethodBinding getBaseMethod = classBinding.getExactMethod(IOTConstants._OT_GETBASE, Binding.NO_PARAMETERS, null);
		    				SourceTypeBinding classSourceType = (SourceTypeBinding) classBinding;
		    				classSourceType.addSyntheticBridgeMethod(unimplementedGetBase, getBaseMethod);
		    			}
		    		}
	    		}
	    	}
    	}
    	model.setState(STATE_LATE_ELEMENTS_COPIED);
    	return success;
    }

    /* **** STATE_CODE_ANALYZED ****
     * CompilationUnitDeclaration.analyseCode() and children
     *
     * with these OT-activities hooked into:
     * - check for replace callin bindings with missing base call result (TypeDeclaration)
     * - merge precedences and check for duplicates (TypeDeclaration)
     *   (requires super-team to be resolved)
     * - (transitively) analyze call to base ctor (ConstructorDeclaration)
     * - analyze base calls in callin method (MethodDeclaration)
     */

    /* **** STATE_BYTE_CODE_PREPARED (OT/J) ****
     * Generate byte code for tsuper role.
     */

    /**
     * Prepare the byte code of methods for copying.
     * Actual byte code copy (incl. adjustment) is performed during generateCode.
     */
    private static boolean establishByteCodePrepared(RoleModel role)
    {
        boolean success = true;
        TypeDeclaration roleDecl = role.getAst();
		if (   roleDecl != null
			&& !roleDecl.isInterface()
			&& needMethodBodies(roleDecl))
        {
            ReferenceBinding tsuperRoleBinding = role.getTSuperRoleBinding();

            if(tsuperRoleBinding != null)
            {
                RoleModel tsuperRoleModel = tsuperRoleBinding.roleModel;
                // TODO(SH): if tsuper role is from the same compilation unit (nested team),
                //           we cannot really separate byte code generation!
                success = ensureBindingState(tsuperRoleModel.getBinding(), STATE_BYTE_CODE_GENERATED);
            }
			CopyInheritance.copySyntheticRoleFieldsAndMethods(roleDecl);
        }
        role.setState(STATE_BYTE_CODE_PREPARED);
        return success;
    }

    /* **** STATE_BYTE_CODE_GENERATED (JAVA) ****
     * Actually produce the byte code.
     * CompilationUnitDeclaration.generateCode() and children
     *
     * + CopyInheritance.copySyntheticFieldsAndMethods() (from TypeDeclaration)
     * + store ROFI-cache (TypeDeclaration)
     * + BytecodeTransformer.checkCopyMethodCode() (from AbstractMethodDeclaration)
     * + write attributes (AbstractMethodDeclaration & ClassFile)
     *
     */

    /* **** STATE_FINAL ****
     * Do some cleanup to give back memory.
     *
     * Don't require previous state to be set, because this state may
     * bypass all intermediate states, e.g., in the case of exceptions,
     * or when only resolve is requested.
     *
     * Processing happens in CUD.cleanup(TypeDeclaration) -> TypeDeclaration.cleanupModels()
     */
}
