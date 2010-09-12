/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2009 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BaseBundleRole.java 23468 2010-02-04 22:34:27Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal.hook;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.objectteams.otequinox.hook.ClassScanner;
import org.eclipse.objectteams.otequinox.hook.HookConfigurator;
import org.eclipse.objectteams.otequinox.hook.ILogger;
import org.eclipse.objectteams.otequinox.hook.ITeamLoader;
import org.osgi.framework.Bundle;

/** 
 * This class decorates each base bundle for which adapting aspect bundles exist.
 * 
 * @author stephan
 * @version $Id: BaseBundleRole.java 23468 2010-02-04 22:34:27Z stephan $
 */
@SuppressWarnings("nls")
public class BaseBundleRole {

	enum State {
		INITIAL,           // nothing known yet.
		WAIT_FOR_TEAM,     // a base bundle for which an aspect was found which cannot yet be loaded. 
		ACTIVATED,         // was waiting, activation has been done, 
		TEAMS_LOADED,      // ready to be loaded/transformed
		TEAMS_INSTANTIATED // in this final state the team has been instantiated
	}
	
	/** Pseudo ID of a basePlugin specifying that the team(s) adapt base classes from their own plugin. */
	static final String SELF = "SELF";
	
	/* Symbolic name of the base bundle. */
	final String symbolicName;
	/* The corresponding base bundle itself. */
	Bundle bundle;
	/* The bundles by which teams are loaded. */
	ArrayList<Bundle> aspectBundles = new ArrayList<Bundle>();
	
	/* State according to the above state model. */
	State state = State.INITIAL;

	// used by the ClassLoaderDelegateHook to cut recursive search:
	HashSet<String> missingClassNames = new HashSet<String>(); // currently unused
	HashMap<String,Class<?>> knownAlienClasses= new HashMap<String, Class<?>>();
	
	BaseBundleRole(Bundle bundle) {
		this.bundle = bundle;
		this.symbolicName = bundle.getSymbolicName();
	}

	void setup(Bundle bundle, State state) {
		this.state = state;
		this.bundle = bundle;
	}
	
	/** Found an adapted base via the aspect: create a role, 
	 *  or retrieve and enhance an existing one (by adding the aspect bundle). */
	static BaseBundleRole createBaseBundleRole(
			BundleRegistry  bundleRegistry, 
			Bundle          baseBundle,
			Bundle          aspectBundle) 
	{
		String symbolicName = baseBundle.getSymbolicName();
		BaseBundleRole baseRole = bundleRegistry.adaptedBaseBundles.get(symbolicName);
		if (baseRole == null) {
			baseRole = new BaseBundleRole(baseBundle);
			bundleRegistry.adaptedBaseBundles.put(symbolicName, baseRole);
		}
		baseRole.aspectBundles.add(aspectBundle);
		return baseRole;
	}
	
	/**
	 * Perform actions due when the base plugin has finished its activation.
	 * Mainly delegate these actions to the bundle's BaseBundleRole.
	 * 
	 * @param bundleRegistry
	 * @param bundle
	 * @param loader
	 * @return a Runnable encapsulating the action of instantiating teams  (or null).
	 */
	static void endActivation(
				BundleRegistry         bundleRegistry, 
				Bundle                 bundle, 
				SafeAspectRegistry     aspectRegistry,
				ITeamLoader            loader) 
	{
		String symbolicName = bundle.getSymbolicName();
		BaseBundleRole baseRole = bundleRegistry.adaptedBaseBundles.get(symbolicName);
		if (baseRole != null) {
			String[] aspects = aspectRegistry.getAdaptingAspectPlugins(bundle);
			if (aspects.length == 0)
				return; // error during service access
			
			// before retrieving aspect roles ensure base bundle has its 
			// classloader initialized (which triggers aspect role creation):
			bundle.getResource("META-INF/MANIFEST.MF"); // ignore result //$NON-NLS-1$
			
			try {
				for (String aspect : aspects) {
					AspectBundleRole aspectBundleRole = bundleRegistry.aspectBundles.get(aspect);
					aspectBundleRole.isLoading = true;
				}
				baseRole.endActivation(loader);
			} finally {
				for (String aspect : aspects) {
					AspectBundleRole aspectBundleRole = bundleRegistry.aspectBundles.get(aspect);
					aspectBundleRole.isLoading = false;
				}				
			}			
		}
	}
	private void endActivation(final ITeamLoader loader) 
	{
		ILogger log = HookConfigurator.getLogger();

		switch (this.state) {
			case WAIT_FOR_TEAM:
				// FIXME(SH): remove once we are sure it never happens
				log.log(Util.ERROR, "Asynchronuous activation while initializing class loader: "+this);
				this.state = State.ACTIVATED;
				break;
			case TEAMS_LOADED:
				// fetch and answer this request
				this.state = State.TEAMS_INSTANTIATED;
				
				// Delegate to the TransformerPlugin to perform PHASE 2 of aspect activation:
				// + loading adapted base classes
				// + instantiating loaded teams.
				log.log(Util.OK, "PHASE 2: Handling base plugin with aspects pending for instantiation: "
						+this.bundle.getSymbolicName());
				loader.instantiateTeams(this.bundle, /*triggerClassname*/null);
				break;
			default: // noop
		}
	}

	/**
	 * Load all known teams adapting this base bundle.
	 * @param loader     the team loader service from org.eclipse.objectteams.otequinox.
	 */
	void loadTeams(ITeamLoader loader, ClassScanner scanner) {
		ILogger log = HookConfigurator.getLogger();

		if (this.state != State.WAIT_FOR_TEAM)
			log.log(Util.ERROR, "Unexpected state when loading teams for "+this);
		
		// perform PHASE 1:
		log.log(Util.OK, "PHASE 1: Load team classes adapting plugin "+this.symbolicName);
		
		if (loader.loadTeams(this.bundle, scanner)) 
		{
			if (this.state == State.ACTIVATED) {
				// FIXME(SH): remove once we are sure it never happens
				
				this.state = State.TEAMS_INSTANTIATED;
				
				log.log(Util.ERROR, "!!!! Unexpected control flow !!!!");
				
				log.log(Util.INFO, "PHASE 2: Handling base plugin with aspects pending for instantiation: "+this.symbolicName);
				loader.instantiateTeams(this.bundle, /*triggerClassname*/null);
			} else {
				this.state = State.TEAMS_LOADED;
			}
		} else {
			this.state = State.TEAMS_INSTANTIATED;
		}
	}
	
	@Override
	public String toString() {
		String result= "Base Bundle "+this.symbolicName +"("+this.state+")";
		for (Bundle aspect : this.aspectBundles) {
			result += "\n\t"+aspect;
		}
		return result;
	}
	
}
