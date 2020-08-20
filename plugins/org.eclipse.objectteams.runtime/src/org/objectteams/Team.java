/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2007 Berlin Institute of Technology, Germany.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.objectteams;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;

import org.eclipse.jdt.annotation.Nullable;

/**
 *  This is the root class of all team definitions.
 *  Any class with the <tt>team</tt> modifier implicitly inherits from this class.
 */
public /* team */ class Team implements ITeam {
	// Technical note: comments starting with //$Debug are intended
	// for a standalone tool that generates an interface
	// which the debugger needs in order to know about line numbers in this class.

	/*
	 *  Synchronization: This class supports two levels of synchronization:
	 *  <ul>
	 *  <li>Fields of <code>Team</code> are synchronized
	 *      using <code>this</code> as the monitor.
	 *  <li>Those calls that (un)register a team at its base classes are synchronized
	 *      via <code>registrationLock</code>
	 *  </ul>
	 *  This allows releasing the lock on <code>this</code> before calling to the base class.
	 *  Note, that the synchronized portion of each initial-wrapper is also synchronized
	 *  (against <code>_OT$addTeam/_OT$removeTeam</code>) and that it calls back to
	 *  the registered teams (method <code>isActive()</code>).
	 *  Without <code>registrationLock</code> this situation could easily deadlock:
	 *  Thread1: <pre>t.activate() -> Base._OT$addTeam()</pre>: owns t, waits for Base.
	 *  Thread2: <pre>b.bm() (initial wrapper) -> t.isActive()</pre>: owns Base, waits for t.
	 */

	/**
	 * Internal field used by the runtime to install a lifting participant if one is configured.
	 */
	public static ILiftingParticipant _OT$liftingParticipant = null;
	
	public static ITeamManager _OT$teamManager; // callback into OTDRE, if set

	/**
	 * Default constructor for debugging purpose.
	 */
	public Team() {} //$Debug(TeamConstructor)

	/*
	 * manual copy-inheritance of a role interface from ITeam.
	 */
	public interface ILowerable extends ITeam.ILowerable {}

	/*
	 * manual copy-inheritance of a role interface from ITeam.
	 */
	public interface IConfined extends ITeam.IConfined {}

	/**
	 *  Special role type that<ul>
	 * <li> does not extend java.lang.Object
	 * <li> may not leak references outside the team.
	 * </ul>
	 */
	protected interface Confined {
		/* internal method needed for cast and instanceof
		 * (this method will be generated for role classes) */
		ITeam _OT$getTeam();
	}

    /**
     * This class would have been generated by the OT-compiler.
     * Don't explicitly use it in client code!
     */
    protected class __OT__Confined implements Confined {
		// internal method needed for cast and instanceof
    	public ITeam _OT$getTeam() {
    		return Team.this; //$Debug(ConfinedGetTeam)
    	}
    }

	/**
	 *  Internal function for identifying a Team.
	 *  Should not be called by client code.
	 */
	public int _OT$getID () {return -1;}

    /**
	 * The constant <code>ALL_THREADS</code> is used for global team (de-)activation.
	 */
	public static final Thread ALL_THREADS = new Thread();

	private static final int _OT$UNREGISTERED = 0;
	private static final int _OT$REGISTERED = 1;
	private  int _OT$registrationState = _OT$UNREGISTERED;

	private boolean _OT$globalActive = false;

	private ThreadLocal<Integer> _OT$implicitActivationsPerThread = new ThreadLocal<Integer>() {
		@Override
		protected synchronized Integer initialValue() {
			return Integer.valueOf(0);
		}
	};

	private boolean _OT$lazyGlobalActiveFlag = false;

	/**
	 * <code>_OT$activatedThreads</code> contains all threads for which this team instance is active.
	 * key		= activated thread
	 * value 	= Boolean(true) for explicit activation | Boolean(false) for implicit activation.
	 */
	private WeakHashMap<Thread, Boolean> _OT$activatedThreads = new WeakHashMap<Thread, Boolean>();

	/** This lock is used to protect activate/deactivate methods <strong>including</strong>
	 *  the calls to doRegistration/doUnregistration.
	 */
	private Object _OT$registrationLock= new Object();

	/**
	 * {@inheritDoc}
	 */
	public void activate() {
		activate(Thread.currentThread());
	}

	/**
	 * {@inheritDoc}
	 */
	public void deactivate() {
		deactivate(Thread.currentThread());
	}

	/**
	 * {@inheritDoc}
	 */
	public void activate(Thread thread) {
		// acquire both locks to avoid incomplete execution:
		synchronized (this._OT$registrationLock) {
			synchronized (this) {
				if (thread.equals(ALL_THREADS)) {
					_OT$globalActive = true;
					_OT$lazyGlobalActiveFlag = true;
					TeamThreadManager.addGlobalActiveTeam(this);
				} else { // activation only for 'thread':
					// register 'thread' as active:
					_OT$activatedThreads.put(thread, Boolean.TRUE);
				}
			} // release this before calling synchronized base class methods
			doRegistration(); //$Debug(ActivateMethod)
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deactivate(Thread thread) {
		// acquire both locks to avoid incomplete execution:
		synchronized (this._OT$registrationLock) {
			boolean shouldUnregister= false;
			synchronized(this) {
				if (thread.equals(ALL_THREADS)) {
					_OT$globalActive = false;
					TeamThreadManager.removeGlobalActiveTeam(this);
					// unregister all threads:
					_OT$activatedThreads.clear();
					shouldUnregister= true;
				} else { // deactivation only for 'thread':
					if (_OT$lazyGlobalActiveFlag) {
						// be eager now: activate for all (other) threads:
						_OT$activateForAllThreads();
					}
					// deactivate for 'thread', no longer active:
					_OT$activatedThreads.remove(thread);
					if (!_OT$lazyGlobalActiveFlag  && _OT$activatedThreads.isEmpty()) {
						shouldUnregister= true;
					}
				}
				_OT$lazyGlobalActiveFlag = false;
			} // release this before calling synchronized base class methods
			if (shouldUnregister) 		//$Debug(DeactivateMethod)
				doUnregistration();
		}
	}

	public void deactivateForEndedThread(Thread thread) {
		synchronized (_OT$registrationLock) {
			boolean shouldUnregister= false;
			synchronized (this) {
				_OT$activatedThreads.remove(thread);
				if (!_OT$lazyGlobalActiveFlag  && _OT$activatedThreads.isEmpty())
					shouldUnregister= true;
			}
			if (shouldUnregister)
				doUnregistration();
		}
	}

	private void _OT$activateForAllThreads() {
		HashSet<Thread> threads = TeamThreadManager.getExistingThreads();
		Iterator<Thread> it = threads.iterator();
		while (it.hasNext()) {
			Thread a_thread = it.next();
			activate(a_thread); // use smaller activate version (no ALL_THREADS, no registerAtBases,...
		}
	}

	/**
	 * This method is used for implicit activation in team-level methods.
	 * Implicit activation only applies to the current thread.
	 * Don't call it from client code.
	 */
	public void _OT$implicitlyActivate() {
		synchronized (this._OT$registrationLock) {
			boolean shouldRegister= false;
			synchronized (this) {
				// this method is used for debugging purpose (team monitor)
				Thread currentThread = Thread.currentThread();
				if (!_OT$activatedThreads.containsKey(currentThread)) {
					// register 'thread' as active:
					_OT$activatedThreads.put(currentThread, Boolean.FALSE);
					shouldRegister= true;
				}
				//	increment thread local implicit activation counter:
				int implActCount = (_OT$implicitActivationsPerThread.get()).intValue();
				_OT$implicitActivationsPerThread.set(Integer.valueOf(implActCount + 1 ));
			}
			if (shouldRegister) //$Debug(ImplicitActivateMethod)
				doRegistration();
		}
	}

	/**
	 * This method is used for implicitly deactivation in team-level methods.
	 * It respects explicit activation changes and nested calls to team-level methods.
	 * Implicit deactivation only applies to the current thread.
	 * Don't call it from client code.
	 */
	public void _OT$implicitlyDeactivate() {
		synchronized (this._OT$registrationLock) {
			boolean shouldUnregister= false;
			synchronized(this) {
				// this method is used for debugging purpose (team monitor)
				Thread currentThread = Thread.currentThread();
				boolean explicitlyActivated = false;
				if (_OT$activatedThreads.containsKey(currentThread)) {
					explicitlyActivated = ((Boolean) _OT$activatedThreads.get(currentThread)).booleanValue();
				}
				if (!explicitlyActivated
						&& !_OT$lazyGlobalActiveFlag // no explicit activation overriding the implicit one
						&& ((_OT$implicitActivationsPerThread.get()).intValue() == 1))  // this is the last implicit activation
				{
					_OT$activatedThreads.remove(currentThread);
					if (_OT$activatedThreads.isEmpty()) // there are not other threads for which this theam is active
					{
						shouldUnregister= true;
					}
				}
				// decrement thread local implicit activaion counter:
				int implActCount = (_OT$implicitActivationsPerThread.get()).intValue();
				_OT$implicitActivationsPerThread.set(Integer.valueOf(implActCount - 1));
			}
			if (shouldUnregister) //$Debug(ImplicitDeactivateMethod)
				doUnregistration();
		}
	}

	/**
	 * Define whether per-thread activation of this team should be inheritable
	 * such that the team will be activated automatically for any new threads
	 * that are spawned from a thread for which the team is already active at that time.
	 *
	 * @param inheritable whether or not activation should be inheritable to new threads
	 */
	public void setInheritableActivation(boolean inheritable) {
		if (inheritable)
			TeamThreadManager.registerTeamForActivationInheritance(this);
		else
			TeamThreadManager.unRegisterTeamForActivationInheritance(this);
	}

	// not API (for use by the TeamThreadManager)
	public boolean internalIsActiveSpecificallyFor(Thread t) {
		return this._OT$activatedThreads.containsKey(t);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isActive() {
		return isActive(Thread.currentThread());
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isActive(Thread thread) {
		if (thread.equals(ALL_THREADS)) {
			return _OT$globalActive;
		}
		if (_OT$lazyGlobalActiveFlag) {
				return true;
		} else {
			//if (!TeamThreadManager.getExistingThreads().contains(thread)) { // this thread is already finished!
			if (!thread.isAlive()) { // this thread is already finished!
				throw new IllegalThreadStateException("Called 'isActive(...)' for a thread which is no longer running!");
			}
			return _OT$activatedThreads.containsKey(thread);
		}
	}

// ***** for restoring the activation state after a within block:	---->*****
	private static final int _OT$INACTIVE = 0;
	private static final int _OT$IMPLICIT_ACTIVE = 1;
	private static final int _OT$EXPLICIT_ACTIVE = 2;

	/**
	 * {@inheritDoc}
	 */
	public synchronized int _OT$saveActivationState() {
		int old_state = _OT$INACTIVE;
		if (_OT$lazyGlobalActiveFlag) {
			old_state = _OT$EXPLICIT_ACTIVE;
		} else {
			Thread current_thread = Thread.currentThread();
			if (_OT$activatedThreads.containsKey(current_thread)) {
				old_state = _OT$IMPLICIT_ACTIVE;
				if (((Boolean)_OT$activatedThreads.get(current_thread)).booleanValue()) {
					old_state = _OT$EXPLICIT_ACTIVE;
				}
			}
		}
		return old_state;
	}

	/**
	 * {@inheritDoc}
	 */
	public void _OT$restoreActivationState(int old_state) {
		synchronized (_OT$registrationLock) {
			if (old_state == _OT$INACTIVE) // team was inactive before:
				deactivate();
			else { // team was active before: has to be reactivated:
				boolean explicit = (old_state == _OT$EXPLICIT_ACTIVE);
				synchronized (this) {
					_OT$activatedThreads.put(Thread.currentThread(), Boolean.valueOf(explicit));
				}
				doRegistration();
			}
		}
	}
//	 ***** <----for restoring the activation state after a within block.	*****


	private void doRegistration() {
		if (_OT$registrationState == _OT$UNREGISTERED) {
			if (_OT$teamManager != null)
				_OT$teamManager.handleTeamStateChange(this, ITeamManager.TeamStateChange.REGISTER);
			else
				_OT$registerAtBases();
			_OT$registrationState = _OT$REGISTERED;
		}
	}

	private void doUnregistration() {
		if (_OT$registrationState == _OT$REGISTERED) {
			if (_OT$teamManager != null)
				_OT$teamManager.handleTeamStateChange(this, ITeamManager.TeamStateChange.UNREGISTER);
			else
				_OT$unregisterFromBases();
			_OT$registrationState = _OT$UNREGISTERED;
		}
	}

	/**
	 * This method will be implemented by generated code in subteams.
	 * It registers the team at every base playing one of its roles.
	 * Don't call it from client code.
	 */
	public void _OT$registerAtBases() {}

	/**
	 * This method will be implemented by generated code in subteams.
	 * It unregisters the team from every base playing one of its roles.
	 * Don't call it from client code.
	 */
	public void _OT$unregisterFromBases() {}

	//public int _OT$activationState = -1; // TODO: remove usage of  this from generated code


	/**
	 * {@inheritDoc}
	 */
	public boolean hasRole(Object aBase) {
		// overriding method to be generated by the compiler for each team with bound roles.
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasRole(Object aBase, Class<?> roleType) throws IllegalArgumentException {
		// overriding method to be generated by the compiler for each team with bound roles.
		throw new IllegalArgumentException("No such bound role type in this team: "+roleType.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getRole(Object aBase) {
		// overriding method to be generated by the compiler for each team with bound roles.
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> @Nullable T getRole(Object aBase, Class<T> roleType) throws IllegalArgumentException {
		// overriding method to be generated by the compiler for each team with bound roles.
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getAllRoles() {
		// overriding method to be generated by the compiler for each team with bound roles.
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T[] getAllRoles(Class<T> roleType) throws IllegalArgumentException {
		// overriding method to be generated by the compiler for each team with bound roles.
		throw new IllegalArgumentException("Class org.objectteams.Team has no bound roles.");
	}

	/** Internal variable to be set from generated code. */
	private ThreadLocal<Boolean> _OT$isExecutingCallin = new ThreadLocal<>();

	/**
	 * Method only for internal use by generated code.
	 */
	public boolean _OT$setExecutingCallin(boolean newFlag) {
		Boolean oldVal = _OT$isExecutingCallin.get();
		_OT$isExecutingCallin.set(newFlag);
		return Boolean.TRUE == oldVal;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isExecutingCallin() {
		return _OT$isExecutingCallin.get() == Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterRole(Object aRole) {
		// overriding method to be generated by the compiler for each team with bound roles.
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterRole(Object aRole, Class<?> roleType) throws IllegalArgumentException {
		// overriding method to be generated by the compiler for each team with bound roles.
	}

	@Override
	protected void finalize() throws Throwable {
		// nop, hook for the debugger
		@SuppressWarnings("unused")
		int i= 2+3; // Note: body must not be empty for debugger to be able to stop.
	} // $Debug(FinalizeMethod)

	/**
	 * If a serializable team wishes to persist its global activation status it must
	 * call this method from its writeObject() method and correspondingly call
	 * {@link #readGlobalActivationState(ObjectInputStream)} from its readObject().
	 */
	protected void writeGlobalActivationState(ObjectOutputStream out) throws IOException {
		out.writeBoolean(this._OT$globalActive);
	}
	/**
	 * If a serializable team wishes to persist its global activation status it must
	 * call this method from its readObject() method and correspondingly call
	 * {@link #writeGlobalActivationState(ObjectOutputStream)} from its writeObject().
	 * If a team is restored that was globally active when serialized, it will be activated
	 * correspondingly during deserialization when this method is called.
	 */
	protected void readGlobalActivationState(ObjectInputStream in) throws IOException {
		this._OT$globalActive = in.readBoolean();
		if (this._OT$globalActive) {
			this._OT$lazyGlobalActiveFlag = true;
			this.doRegistration();
		}
	}
	/**
	 * Serializable teams must invoke this method once from their readObject() method
	 * in order to re-initialize internal data structures.
	 */
	protected void restore() { /* empty; implementation will be generated for each serializable sub-class. */ }
	/**
	 * Serializable teams must invoke this method from their readObject() method
	 * for each role that has been retrieved and shall be re-registered for this team.
	 */
	protected void restoreRole(Class<?> clazz, Object role) { /* empty; implementation will be generated for each serializable sub-class. */ }
	
	// === BELOW THIS POINT: Methods used by the Object Teams Dynamic Runtime Environment, NOT API ===
	
	public static void registerTeamManager(ITeamManager teamManager) {
		if (Team._OT$teamManager != null) throw new IllegalStateException("team manager already defined.");
		Team._OT$teamManager = teamManager;
	}

	/**
	 * This method is the first part of the new chaining wrapper.
	 * It should be called from the generated client code.
	 *
	 * @param baze the current base object
	 * @param teams the current team objects
	 * @param idx the index of the current team in teams
	 * @param callinIds an array of ids, that are unique in the team
	 *                  for a base method in a base class
	 * @param boundMethodId an unique id for a base method in the base class.
	 *                      This id is needed for a base call.
	 * @param args packed arguments.
	 * @return possibly boxed result
	 */
	public Object _OT$callAllBindings(IBoundBase2 baze, ITeam[] teams,int idx,int[] callinIds, int boundMethodId, Object[] args)
	{
		Object res = null;

		if ((boundMethodId & 0x80000000) == 0) { // bit 0x80000000 signals ctor (which has no before/replace bindings)
			this._OT$callBefore(baze, callinIds[idx], boundMethodId, args);

			res = this._OT$callReplace(baze, teams, idx, callinIds, boundMethodId, args);
		}

		this._OT$callAfter(baze, callinIds[idx], boundMethodId, args, res); // make result available to param mappings!

		return res;
	}
	/**
	 * This method calls the next team or a base method,
	 * if there are no more active teams for a joinpoint
	 *
	 * @param baze the current base object
	 * @param teams the current base object
	 * @param idx the index of the current team in teams, also points into callinIds, i.e., both lists run synchroneously.
	 * @param callinIds an array of ids, that are unique in the team
	 *                  for a base method in a base class
	 * @param boundMethodId an unique id for a base method in the base class.
	 *                      This id is needed for a base call.
	 * @param args original packed arguments.
	 * @param baseCallArgs packed arguments as provided to the base call.
	 * @param baseCallFlags flags to signal a base call / a base super call
	 * @return possibly boxed result
	 */
	public Object _OT$callNext(IBoundBase2 baze, ITeam[] teams, int idx, int[] callinIds, int boundMethodId, Object[] args, Object[] baseCallArgs, int baseCallFlags)
	{
		return _OT$terminalCallNext(baze, teams, idx, callinIds, boundMethodId, args, baseCallArgs, baseCallFlags);
	}
	public static Object _OT$terminalCallNext(IBoundBase2 baze, ITeam[] teams, int idx, int[] callinIds, int boundMethodId, Object[] args, Object[] baseCallArgs, int baseCallFlags)
	{
		// Are there still active teams?
		if (idx+1 < teams.length) {
			// Yes, so call the next team/callin
			return teams[idx+1]._OT$callAllBindings(baze, teams, idx+1, callinIds, boundMethodId, args);
		} else {
			//No, call the base method
			if (baze == null) {
				//handle base call to a static base method
				return teams[idx]._OT$callOrigStatic(callinIds[idx], boundMethodId, args);
			} else {
				if (baseCallFlags == 2)
					boundMethodId++;
				return baze._OT$callOrig(boundMethodId, args);
			}
		}
	}

	/**
	 * Executes all before callins for a given callin id.
	 * Must be overridden by a team, if the team gets before callins.
	 *
	 * @param baze the current base object
	 * @param callinId the current callin id
	 * @param boundMethodId an unique id for a base method in the base class.
	 *                      This id is needed for a base call.
	 * @param args packed arguments.
	 * @return possibly boxed result
	 */
	public void _OT$callBefore(IBoundBase2 baze, int callinId, int boundMethodId, Object[] args) {
		// nop; override with code from before callin bindings.
	}

	/**
	 * Executes all after callins for a given callin id.
	 * Must be overridden by a team, if the team gets after callins.
	 *
	 * @param baze the current base object
	 * @param callinId the current callin id
	 * @param boundMethodId an unique id for a base method in the base class.
	 *                      This id is needed for a base call.
	 * @param args packed arguments.
	 * @param result the result of the base method. Could be used by after callins
	 */
	public void _OT$callAfter(IBoundBase2 baze, int callinId, int boundMethodId, Object[] args, Object result) {
		// nop; override with code from after callin bindings.
	}

	/**
	 * Execute replace callins of the team for the current callin id.
	 * Must be overridden by a team, if the team has got replace callins.
	 *
	 * @param baze the current base object
	 * @param teams the current base object
	 * @param idx the index of the current team in teams
	 * @param callinIds an array of ids, that are unique in the team
	 *                  for a base method in a base class
	 * @param boundMethodId an unique id for a base method in the base class.
	 *                      This id is needed for a base call.
	 * @param args packed arguments.
	 * @return possibly boxed result
	 */
	public Object _OT$callReplace(IBoundBase2 baze, ITeam[] teams, int idx, int[] callinIds, int boundMethodId, Object[] args) {
		// default; override with code from replace callin bindings.
		return _OT$callNext(baze, teams, idx, callinIds, boundMethodId, args, null, 0);
	}

	/**
	 * Calls the method callOrigStatic of a concrete class dependent on the
	 * given callin id.
	 * Must be overridden in a team, if the team has got base calls
	 * to static base methods
	 * @param callinId
	 * @param boundMethodId
	 * @param args
	 * @return
	 */
	public Object _OT$callOrigStatic(int callinId, int boundMethodId, Object[] args) {
		return null;
	}
}
