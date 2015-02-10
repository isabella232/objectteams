/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2013, 2015 GK Software AG
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 	Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.internal.osgi.weaving;

import static org.eclipse.objectteams.otequinox.TransformerPlugin.log;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.objectteams.internal.osgi.weaving.AspectBinding.BaseBundle;
import org.eclipse.objectteams.internal.osgi.weaving.AspectBinding.TeamBinding;
import org.eclipse.objectteams.internal.osgi.weaving.Util.ProfileKind;
import org.eclipse.objectteams.otequinox.ActivationKind;
import org.eclipse.objectteams.otequinox.TransformerPlugin;
import org.eclipse.objectteams.otredyn.runtime.TeamManager;
import org.objectteams.Team;
import org.osgi.framework.Bundle;
import org.osgi.framework.hooks.weaving.WovenClass;

/**
 * This class triggers the actual loading/instantiation/activation of teams.
 * <p>
 * It implements a strategy of deferring those teams that are not ready for
 * instantiating / activating, which we check by comparing the sets of
 * bound base classes of the team vs. the set of classes currently being
 * processed by class loading & weaving.
 * This reflects that fact that classes for which loading has already been
 * started would otherwise trigger an irrecoverable NoClassDefFoundError. 
 * </p><p>
 * Which teams participate in deferred instantiation is communicated via the 
 * shared list {@link #deferredTeams}.
 * </p>
 */
@NonNullByDefault
public class TeamLoader {

	/** Collect here any teams that cannot yet be handled and should be scheduled later. */
	private List<WaitingTeamRecord> deferredTeams;

	private Set<String> beingDefined;
	
	boolean useDynamicWeaving;

	public TeamLoader(List<WaitingTeamRecord> deferredTeams, Set<String> beingDefined, boolean useDynamicWeaving) {
		this.deferredTeams = deferredTeams;
		this.beingDefined = beingDefined;
		this.useDynamicWeaving = useDynamicWeaving;
	}

	/**
	 * Team loading, 1st attempt before the base class is even loaded
	 * Trying to do these phases: load (now) instantiate/activate (if ready),
	 */
	public void loadTeamsForBase(BaseBundle baseBundle, WovenClass baseClass, AspectPermissionManager permissionManager) {
		@SuppressWarnings("null")@NonNull String className = baseClass.getClassName();
		Collection<TeamBinding> teamsForBase = baseBundle.teamsPerBase.get(className);
		if (teamsForBase == null) 
			return; // not done

		// permission checking can be performed now or later, depending on readiness:
		boolean permissionManagerReady = permissionManager.isReady();

		// ==== check permissions before we start activating:
		if (permissionManagerReady) { // otherwise we will register pending obligations below.
			Set<TeamBinding> deniedTeams = permissionManager.checkAspectPermissionDenial(teamsForBase);
			if (!deniedTeams.isEmpty()){
				for (WaitingTeamRecord rec : new ArrayList<>(this.deferredTeams))
					if (deniedTeams.contains(rec.team))
						this.deferredTeams.remove(rec);
			}
		}
		
		List<Team> teamInstances = new ArrayList<>();
		for (TeamBinding teamForBase : teamsForBase) {
			if (teamForBase.isActivated) continue;
			if (teamForBase.hasBeenDenied()) {
				log(IStatus.WARNING, "Not activating team "+teamForBase.teamName+" due to denied permissions.");
				continue;
			}
			// Load:
			Class<? extends Team> teamClass;
			teamClass = teamForBase.loadTeamClass();
			if (teamClass == null) {
				log(new ClassNotFoundException("Not found: "+teamForBase), "Failed to load team "+teamForBase);
				continue;
			}
			// Try to instantiate & activate, failures are recorded in deferredTeams
			ActivationKind activationKind = teamForBase.getActivation();
			if (activationKind == ActivationKind.NONE) {
				teamForBase = teamForBase.getOtherTeamToActivate();
				if (teamForBase != null) {
					if (teamForBase.isActivated) continue;
					activationKind = teamForBase.getActivation();
					teamClass = teamForBase.loadTeamClass();
					if (teamClass == null) {
						log(new ClassNotFoundException("Not found: "+teamForBase.teamName+" in bundle "+teamForBase.getAspectBinding().aspectPlugin), "Failed to load team "+teamForBase);
						continue;						
					}
				} else {
					continue;
				}
			}
			if (activationKind == ActivationKind.NONE) 
				continue;
			Team instance = instantiateAndActivate(teamForBase.getAspectBinding(), teamForBase, activationKind);
			if (instance != null)
				teamInstances.add(instance);
		}

		if (!permissionManagerReady)
			permissionManager.addBaseBundleObligations(teamInstances, teamsForBase, baseBundle);
	}

	public static @Nullable Pair<URL,String> findTeamClassResource(String className, Bundle bundle) {
		for (String candidate : possibleTeamNames(className)) {
			URL result = bundle.getResource(candidate.replace('.', '/')+".class");
			if (result != null)
				return new Pair<>(result, candidate);
		}
		return null;
	}

	/** 
	 * Starting from currentName compute a list of potential binary names of (nested) teams
	 * using "$__OT__" as the separator, to find class parts of nested teams.  
	 */
	public static List<String> possibleTeamNames(String currentName) {
		List<String> result = new ArrayList<String>();
		result.add(currentName);
		char sep = '.'; // assume source name
		if (currentName.indexOf('$') > -1)
			// binary name
			sep = '$';
		int from = currentName.length()-1;
		while (true) {
			int pos = currentName.lastIndexOf(sep, from);
			if (pos == -1)
				break;
			String prefix = currentName.substring(0, pos); 
			String postfix = currentName.substring(pos+1);
			if (sep=='$') {
				if (!postfix.startsWith("__OT__"))
					result.add(0, currentName = prefix+"$__OT__"+postfix);
			} else {
				// heuristic: 
				// only replace if parent element looks like a class (expected to start with uppercase)
				int prevDot = prefix.lastIndexOf('.');
				if (prevDot > -1 && Character.isUpperCase(prefix.charAt(prevDot+1))) 
					result.add(0, currentName = prefix+"$__OT__"+postfix);
				else 
					break;
			}
			from = pos-1;
		}
		return result;
	}

	/**
	 * Check if the given team is ready. If so instantiate it and if activationKind requires also activate it.
	 */
	@Nullable Team instantiateAndActivate(AspectBinding aspectBinding, TeamBinding team, ActivationKind activationKind)
	{
		String teamName = team.teamName;
		// don't try to instantiate before all base classes successfully loaded.
		synchronized(aspectBinding) {
			if (!isReadyToLoad(aspectBinding, team, teamName, activationKind)) {
				if (this.useDynamicWeaving)
					TeamManager.prepareTeamActivation(team.teamClass);
				return null;
			}
			for (TeamBinding equivalent : team.equivalenceSet)
				equivalent.isActivated = true;
		}

		try {
			long time = 0;
			if (Util.PROFILE) time= System.nanoTime();

			@SuppressWarnings("null")@NonNull Team instance = team.teamClass.newInstance();
			TransformerPlugin.registerTeamInstance(instance);
			log(IStatus.INFO, "Instantiated team "+teamName);
			
			try {
				switch (activationKind) {
				case ALL_THREADS:
					instance.activate(Team.ALL_THREADS);
					log(IStatus.INFO, "Activated team "+teamName);
					break;
				case THREAD:
					instance.activate();
					log(IStatus.INFO, "Activated team "+teamName);
					break;
				//$CASES-OMITTED$
				default:
					break;
				}
				if (Util.PROFILE) Util.profile(time, ProfileKind.Activation, teamName);
			} catch (NoClassDefFoundError|ClassCircularityError e) {
				try { // clean up:
					switch (activationKind) {
					case ALL_THREADS: instance.deactivate(Team.ALL_THREADS); break;
					case THREAD: instance.deactivate(); break;
					default: break;
					}
				} catch (Throwable t) { /* ignore */ }
				for (TeamBinding eq : team.equivalenceSet)
					eq.isActivated = false;
				@SuppressWarnings("null") @NonNull // known API
				String notFoundName = e.getMessage().replace('/', '.');
				synchronized (this.deferredTeams) {
					this.deferredTeams.add(new WaitingTeamRecord(team, activationKind, notFoundName));
				}
			} catch (Throwable t) {
				// application errors during activation
				log(t, "Failed to activate team "+teamName);
			}
			return instance;
		} catch (ClassCircularityError e) {
			for (TeamBinding eq : team.equivalenceSet)
				eq.isActivated = false;
			@SuppressWarnings("null") @NonNull // known API
			String notFoundName = e.getMessage().replace('/', '.');
			synchronized (this.deferredTeams) {
				this.deferredTeams.add(new WaitingTeamRecord(team, activationKind, notFoundName));
			}
		} catch (Throwable e) {
			// application error during constructor execution?
			log(e, "Failed to instantiate team "+teamName);
		}
		return null;
	}

	private boolean isReadyToLoad(AspectBinding aspectBinding,
									TeamBinding team, String teamName,
									ActivationKind activationKind)
	{
		String unloadableBaseClass = findUnloadableBaseClass(team);
		if (unloadableBaseClass != null) {
			synchronized (deferredTeams) {
				WaitingTeamRecord record = new WaitingTeamRecord(team, activationKind, unloadableBaseClass);
				deferredTeams.add(record);
			}
			log(IStatus.INFO, "Defer instantation/activation of team "+teamName);
			return false;
		}
		return true;
	}
	
	private @Nullable String findUnloadableBaseClass(TeamBinding team) {
		// easy tests first:
		for (@SuppressWarnings("null")@NonNull String baseclass : team.baseClassNames) {
			if (this.beingDefined.contains(baseclass))
				return baseclass;
		}
		// definite, more expensive tests:
		Class<?> teamClass = team.teamClass;
		if (teamClass != null) {
			// use a throw-away class loader so we have a fresh chance to load any failed classes later
			// (only initiating class loader remembers the failure, if this is discarded, the slate is clean):
			ClassLoader tryLoader = new ClassLoader(teamClass.getClassLoader()) {};
			for (@SuppressWarnings("null")@NonNull String baseclass : team.baseClassNames)
				try {
					tryLoader.loadClass(baseclass);
				} catch (Throwable t) {
					return baseclass;
				}
		}
		return null;
	}
}
