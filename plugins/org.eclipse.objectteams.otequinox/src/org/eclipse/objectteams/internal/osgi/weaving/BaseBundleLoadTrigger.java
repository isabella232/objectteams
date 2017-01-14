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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.objectteams.internal.osgi.weaving.AspectBinding.BaseBundle;
import org.eclipse.objectteams.internal.osgi.weaving.OTWeavingHook.WeavingScheme;
import org.osgi.framework.Bundle;
import org.osgi.framework.hooks.weaving.WovenClass;

/**
 * Each instance of this class represents the fact that a given base bundle has aspect bindings,
 * which require to load / instantiate / activate one or more teams at a suitable point in time.
 */
@NonNullByDefault
public class BaseBundleLoadTrigger {

	private AspectBindingRegistry aspectBindingRegistry;
	@SuppressWarnings("deprecation")
	private @Nullable org.osgi.service.packageadmin.PackageAdmin admin;

	private String baseBundleName;	
	@Nullable private BaseBundle baseBundle; // null when representing an aspectBundle with SELF-adapting teams
	private boolean otreAdded = false;
	private List<AspectBinding> aspectBindings = new ArrayList<>();

	public BaseBundleLoadTrigger(String bundleSymbolicName, @Nullable BaseBundle baseBundle, AspectBindingRegistry aspectBindingRegistry, 
			@SuppressWarnings("deprecation") @Nullable org.osgi.service.packageadmin.PackageAdmin admin) 
	{
		this.baseBundleName = bundleSymbolicName;
		this.baseBundle = baseBundle;
		this.aspectBindingRegistry = aspectBindingRegistry;
		this.admin = admin;
	}
	
	WeavingScheme getWeavingScheme() {
		@NonNull WeavingScheme scheme = OTWeavingHook.DEFAULT_WEAVING_SCHEME;
		for (AspectBinding aspectBinding : aspectBindings) {
			if (aspectBinding.weavingScheme != WeavingScheme.Unknown) {
				scheme = aspectBinding.weavingScheme;
				if (OTWeavingHook.DEFAULT_WEAVING_SCHEME == WeavingScheme.Unknown)
					OTWeavingHook.DEFAULT_WEAVING_SCHEME = scheme;
				break;
			}
		}
		return scheme;
	}

	/**
	 * Signal that the given class is being loaded and trigger any necessary steps:
	 * (1) add import to OTRE (now)
	 * (2) scan team (now)
	 * (3) add imports base->aspect (now)
	 * (4) load & instantiate & activate (now or later).
	 */
	@SuppressWarnings("deprecation") // uses deprecated PackageAdmin
	public void fire(WovenClass baseClass, Set<String> beingDefined, OTWeavingHook hook) {

		// (1) OTRE import added once per base bundle:
		WeavingScheme weavingScheme = getWeavingScheme();
		synchronized(this) {
			final BaseBundle baseBundle2 = baseBundle;
			if (!otreAdded) {
				otreAdded = true;
				addOTREImport(baseBundle2, baseBundleName, baseClass, weavingScheme == WeavingScheme.OTDRE);
			}
		}
		
		// for each team in each aspect binding:
		synchronized (aspectBindings) {
			if (aspectBindings.isEmpty())
				aspectBindings.addAll(aspectBindingRegistry.getAdaptingAspectBindings(baseBundleName));
		}
		List<WaitingTeamRecord> deferredTeamClasses = new ArrayList<>();
		for (AspectBinding aspectBinding : aspectBindings) {
			if (!aspectBinding.hasScannedTeams)
				log(IStatus.INFO, "Preparing aspect binding for base bundle "+baseBundleName);
			final org.osgi.service.packageadmin.PackageAdmin admin2 = admin;
			if (admin2 == null) {
				log(IStatus.ERROR, "Cannot find aspect bundle "+aspectBinding.aspectPlugin);
				continue;					
			} else {
				Bundle aspectBundle = aspectBinding.aspectBundle;
				if (aspectBundle == null) {
					Bundle[] aspectBundles = admin2.getBundles(aspectBinding.aspectPlugin, null);
					if (aspectBundles == null || aspectBundles.length == 0) {
						log(IStatus.ERROR, "Cannot find aspect bundle "+aspectBinding.aspectPlugin);
						continue;
					}
					aspectBundle = aspectBundles[0];
					assert aspectBundle != null : "Package admin should not return a null array element";
				}
				// (2) scan all teams in affecting aspect bindings:
				if (!aspectBinding.hasScannedTeams) {
					Collection<String> boundBases = aspectBinding.scanTeamClasses(aspectBundle, DelegatingTransformer.newTransformer(weavingScheme, hook, baseClass.getBundleWiring()));
					aspectBindingRegistry.addBoundBaseClasses(boundBases);
				}
				
				// (3) add dependencies to the base bundle:
				if (weavingScheme == WeavingScheme.OTRE) // OTDRE accesses aspects by generic interface in o.o.Team
					aspectBinding.addImports(baseClass);

			}
		}
		// (4) try optional steps concerning all teams for this base (across all involved aspect bindings):
		TeamLoader loading = new TeamLoader(deferredTeamClasses, beingDefined, weavingScheme == WeavingScheme.OTDRE);
		final BaseBundle baseBundle3 = this.baseBundle;
		if (baseBundle3 != null) {
			loading.loadTeamsForBase(baseBundle3, baseClass, hook.getAspectPermissionManager());
		} else {
			// FIXME: handle SELF adapting aspect binding!! (perhaps using a special kind of BaseBundle??
		}

		// if some had to be deferred collect them now:
		if (!deferredTeamClasses.isEmpty()) {
			hook.addDeferredTeamClasses(deferredTeamClasses);
		}

		// mark done for this base class 
		// (do outside the loop in case multiple aspect bindings point to the same baseBundle)
		for (AspectBinding aspectBinding : aspectBindings) {
			String baseClassName = baseClass.getClassName();
			assert baseClassName != null : "WovenClass.getClassName() should not answer null";
			aspectBinding.cleanUp(baseClassName);
		}
	}

	static void addOTREImport(@Nullable BaseBundle baseBundle, String baseBundleName, WovenClass baseClass, boolean useDynamicWeaver) 
	{
		if (baseBundle != null) {
			if (baseBundle.otreAdded)
				return;
			baseBundle.otreAdded = true;
		}
		log(IStatus.INFO, "Adding OTRE import to "+baseBundleName);
		List<String> imports = baseClass.getDynamicImports();
		imports.add("org.objectteams");
		if (useDynamicWeaver)
			imports.add("org.eclipse.objectteams.otredyn.runtime"); // for access to TeamManager
	}

	public boolean isDone() {
		for (AspectBinding binding : aspectBindings)
			if (!binding.isDone())
				return false;
		return true;
	}

	public boolean areAllAspectsDenied() {
		for (AspectBinding binding : aspectBindings)
			if (!binding.hasBeenDenied)
				return false;
		return true;
	}
}
