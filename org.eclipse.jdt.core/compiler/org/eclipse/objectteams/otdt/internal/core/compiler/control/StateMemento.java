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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config.NotConfiguredException;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * Each object of StateMemento describes the translation state of a CUD or a type.
 * + actual state (_state)
 * + state being processed (_currentlyProcessingState)
 * + state requested for processing (_requestedState)
 * + has method resolving started? (_methodResolveStarted) (TODO(SH): replace with currentlyProcessing..?)
 *
 * FIXME(SH): _completingBindingStep should be obsolete now.
 *
 * @author stephan
 * @since OTDT 1.1.3
 */
public class StateMemento
{
    /** actual current ITranslationState. */
    private int _state = ITranslationStates.STATE_NONE;

    private boolean _methodResolveStarted = false;

    /**
     * State that has started processing.
     * Note that this field is set by Dependencies.ensurce{Team,Role}State as well
     * as two functions in ClassScope, which bypass Dependencies.
     */
    private int _currentlyProcessingState = ITranslationStates.STATE_NONE;
    /** Details of STATE_BINDINGS_COMPLETED, see _currentlyProcessingState. */
    private int _completingBindingsStep = 0;
    /** A state that should be established after an ongoing processing has finished. */
    private int _requestedState = ITranslationStates.STATE_NONE;

    /** map states to job-lists. */
	private HashMap<Integer, List<Runnable>> _pendingJobs = new HashMap<Integer, List<Runnable>>();


    /** Answer the last state that has either completed or at least started. */
    public int getProcessingState() {
    	return Math.max(this._state, this._currentlyProcessingState);
    }

    public int getState() {
		return this._state;
	}

	/**
	 * Is the given element ready to process `state'?
	 * This is not the case if some processing already takes place.
	 * If current processing is less that `state'
	 * then schedule `state' as _requested.
	 */
    public boolean isReadyToProcess(int state) {
    	if (this._currentlyProcessingState == 0 || this._state >= state)
    		return true;
    	if (this._currentlyProcessingState < state)
    		this._requestedState = Math.max(this._requestedState, state);
    	return false;
    }
	/** If state is still unset it to `initState'. */
	public void inititalize(int initState) {
        if (this._state == 0)
        	this._state = initState;
	}

	/** Set translation state to `state' but refusing downgrades,
	 *  possibly resetting _currentlyProcessingState.
	 *  @return old state value.
	 */
    public int setState(int state) {
    	int oldState= this._state;
    	if (state >= this._currentlyProcessingState)
			this._currentlyProcessingState = ITranslationStates.STATE_NONE;
    	if (state >= this._requestedState)
			this._requestedState = ITranslationStates.STATE_NONE;
        if (state > this._state) {
        	this._state = state;
// Debugging the statemachine:
//    		 System.out.println(this);
        }
        return oldState;
	}

    /** Signal that processing for `state' has begun. */
    void startProcessing(int state, int step) {
    	this._currentlyProcessingState = Math.max(this._currentlyProcessingState, state);
    	// FIXME(SH): is this needed after both state-models have been integrated?
    	this._completingBindingsStep = Math.max(this._completingBindingsStep, step);
    }

	/** Is this team blocking processing the requested step for its roles?
	 * @param teamModel
	 * @param state state according to ITranslationStates
	 * @param step sub-step according to LookupEnvironment
	 */
	public boolean isBlocking(TeamModel teamModel, int state, int step) {
		if (! teamModel._blockCatchup)
			return false;
		if (this._currentlyProcessingState == ITranslationStates.STATE_NONE)
			return false;
		if (state < this._currentlyProcessingState)
			return false;
		// FIXME(SH): obsolete after integrating both state models?
		if (state >= ITranslationStates.STATE_LENV_BUILD_TYPE_HIERARCHY && state <= ITranslationStates.STATE_ROLES_LINKED)
			return step < this._completingBindingsStep;
		return true;
	}

    /** While processing is blocked, schedule a processing request.  */
    public void requestState(TypeDeclaration typeDecl, int state) {
    	if (typeDecl != null && typeDecl.compilationUnit != null)
    	{
    		StateMemento unitState = typeDecl.compilationUnit.state;
			if (state > unitState._currentlyProcessingState)
    		{
				unitState._requestedState= Math.max(state,
									unitState._requestedState);
				return; // requesting at the CUD should suffice, will descend to the type.
    		}
    	}
    	if (state > this._currentlyProcessingState)
    		this._requestedState = state;
    }

    /** Destructively read _requestedState. */
    int fetchRequestedState() {
    	int oldRequest= this._requestedState;
    	this._requestedState= 0;
    	return oldRequest;
    }

    /** Given that `request' has been requested, decide if now is the time
     *  to actually perform the request. */
	public void handleRequest(CompilationUnitDeclaration unit, int requested) {
		if (Config.getBundledCompleteTypeBindingsMode()) {
			// in this mode don't process past the last step finished in LookupEnvironment:
			LookupEnvironment env = null;
			if (unit.scope != null)
				env = unit.scope.environment;
			if (env == null) {
				try {
					env = Config.getLookupEnvironment();
				} catch (NotConfiguredException e) {
					e.logWarning("Cannot handle processing request"); //$NON-NLS-1$
				}
			}
			if (env != null) {
				int envStateCompleted = env.getDependenciesStateCompleted();
				if (envStateCompleted < requested) {
					// just record the original request:
					this._requestedState = Math.max(requested, this._requestedState);
					// but perform that part that is safe:
					if (envStateCompleted > this._state)
						Dependencies.ensureState(unit, envStateCompleted);
					return;
				}
			}
		}
		if (this._currentlyProcessingState == 0) {
			if (this._requestedState > this._state) {
				requested= Math.max(requested, this._requestedState);
				if (isBlocked(unit, requested))
					return;
				this._requestedState= 0;
			}
			if (requested > this._state)
				Dependencies.ensureState(unit, requested);
		} else { // not ready, postpone again.
			this._requestedState= Math.max(this._requestedState, requested);
		}
	}

    private boolean isBlocked(CompilationUnitDeclaration unit, int requested) {
    	if (unit.isRoleUnit() && unit.types != null && unit.types.length > 0) {
    		TypeDeclaration enclosingType = unit.types[0].enclosingType;
    		if (enclosingType != null && enclosingType.isTeam())
    			return enclosingType.getTeamModel()._blockCatchup;
    	}
    	return false;
	}

	/** Add a job to be performed as soon as this element reaches `state'. */
    public void addJob(int state, Runnable job) {
    	if (state <= this._state) {
    		job.run();
    		return;
    	}
    	List<Runnable> jobs = this._pendingJobs.get(state);
    	if (jobs == null)
    		this._pendingJobs.put(state, jobs = new ArrayList<Runnable>());
    	jobs.add(job);
    }

	public void runPendingJobs(int state) {
    	List<Runnable> jobs;
    	synchronized (this) {
    		jobs = this._pendingJobs.remove(state);
		}
    	if (jobs != null)
    		for (Runnable job : jobs)
    			job.run();
	}

	@Override
	@SuppressWarnings("nls")
	public String toString() {
		String result= ITranslationStates.STATE_NAMES[this._state];
		if (this._currentlyProcessingState > 0)
			result += " c:"+this._currentlyProcessingState;
		if (this._requestedState > 0)
			result += " r:"+this._requestedState;
		return result;
	}

	public void assertState(int state, String msg) {
		if (this._state < state)
			throw new InternalCompilerError(msg);
	}

	/** Signal that processing for `state' has begun.
	 * @return the previous processing state
	 */
	public int startProcessing(int state) {
		int previous = this._currentlyProcessingState;
		this._currentlyProcessingState = Math.max(this._currentlyProcessingState, state);
		return previous;
	}


	/** See methodResolveStart() */
	public static boolean hasMethodResolveStarted(ReferenceBinding type) {
	    return StateHelper.getState(type)._methodResolveStarted;
	}

	/**
	 * Using this method, TypeDeclaration.resolve() signals when it is about
	 * to resolve methods. Any methods added to the class later than this need
	 * to be resolved manually.
	 *
	 * @param type
	 */
	public static void methodResolveStart(ReferenceBinding type) {
	    StateHelper.getState(type)._methodResolveStarted = true;
	}

	public boolean hasMethodResolveStarted() {
		return this._methodResolveStarted;
	}
}
