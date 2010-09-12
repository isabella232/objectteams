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
 * $Id: MasterTeamLoader.java 15426 2007-02-25 12:52:19Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal.hook;

import java.util.HashSet;

import org.eclipse.objectteams.otequinox.hook.IAspectRegistry;
import org.eclipse.objectteams.otequinox.hook.ILogger;
import org.eclipse.objectteams.otequinox.hook.IOTEquinoxService;
import org.osgi.framework.Bundle;

/**
 * Implements IAspectRegistry via an indirection in order to catch
 * query attempts before the OT/Equinox plugin is properly initialized.
 *  
 * @author stephan
 * @since OTDT 1.1.4
 */
@SuppressWarnings("nls")
public class SafeAspectRegistry implements IAspectRegistry {

	// references to other important objects:
	private TransformerHook hook;
	private IOTEquinoxService otEquinox;

	// record bundles that were loaded before the transformer was in place:
	private HashSet<String> nonAdaptableBundles = new HashSet<String>();
	private enum InitState { OK, NOT_YET, TOO_LATE }
	
	SafeAspectRegistry(TransformerHook hook) {
		this.hook= hook;
	}
	void connectOTEquinoxService(IOTEquinoxService otEquinoxService, ILogger log) {
		this.otEquinox= otEquinoxService;
		// Check whether any of the plugins, which have been loaded before the TransformerPlugin,
		// are declared to be adapted by an aspect => ERROR.  
		for (String bundle: nonAdaptableBundles)
			if (otEquinoxService.isAdaptedBasePlugin(bundle))
				log.log(Util.ERROR, "Trying to adapt non-adaptable platform bundle "+bundle);
	}

	/** see {@link org.eclipse.objectteams.otequinox.hook.IAspectRegistry#isOTDT()} */
	public boolean isOTDT() {
		return (this.otEquinox != null) && this.otEquinox.isOTDT();
	}
	
	/** see {@link org.eclipse.objectteams.otequinox.hook.IAspectRegistry#isAdaptedBasePlugin(String)} */
	public boolean isAdaptedBasePlugin(String baseBundleName) {
		InitState state= checkInitialization(baseBundleName);
		if (state == InitState.NOT_YET)
			return false;
		boolean result= this.otEquinox.isAdaptedBasePlugin(baseBundleName);
		if (!result) return false;
		if (state == InitState.TOO_LATE) 
			throw new RuntimeException("Boot order problem: base bundle "+baseBundleName
									  +" was loaded before the transformer plug-in was ready!");
		return result;
	}
	
	/** see {@link org.eclipse.objectteams.otequinox.hook.IAspectRegistry#getAdaptingAspectPlugins(Bundle)} */
	public String[] getAdaptingAspectPlugins(Bundle baseBundle) {
		InitState state= checkInitialization(baseBundle.getSymbolicName());
		if (state == InitState.NOT_YET)
			return new String[0];
		String[] result= this.otEquinox.getAdaptingAspectPlugins(baseBundle);
		if (result.length > 0) {
			if (state == InitState.TOO_LATE) 
				throw new RuntimeException( "Boot order problem: base bundle "+baseBundle.getSymbolicName()
										   +" was loaded before the transformer plug-in was ready!");
		}
		return result;
	}

	/** see {@link org.eclipse.objectteams.otequinox.hook.IAspectRegistry#getAdaptedBasePlugins(Bundle)} */
	public String[] getAdaptedBasePlugins(Bundle aspectBundle) {
		if (aspectBundle == this.hook.otEquinoxBundle) 
			return null; // don't adapt the adaptor
		if (this.otEquinox == null)
			return null;
		return this.otEquinox.getAdaptedBasePlugins(aspectBundle);
	}

	/** see {@link org.eclipse.objectteams.otequinox.hook.IAspectRegistry#hasInternalTeams(Bundle)} */
	public boolean hasInternalTeams(Bundle baseBundle) {
		if (this.otEquinox == null)
			return false;
		return this.otEquinox.hasInternalTeams(baseBundle);
	}

	private InitState checkInitialization(String baseBundle) {
		if (nonAdaptableBundles.contains(baseBundle)) {
			if (this.otEquinox == null)
				return InitState.NOT_YET;
			return InitState.TOO_LATE;
		}
		if (this.otEquinox == null) {
			// can't ask the registry before this field is initialized.
			if (!Util.isPlatformBundle(baseBundle))
				// no transformer, no log! 
				// But note, that the same problem should also be detected by sanityCheck!
				System.err.println("Accessing non-adaptable element "+baseBundle); 
			nonAdaptableBundles.add(baseBundle);
			return InitState.NOT_YET;
		}	
		return InitState.OK;
	}
	/** see {@link org.eclipse.objectteams.otequinox.hook.IAspectRegistry#isDeniedAspectPlugin(String)} */
	public boolean isDeniedAspectPlugin(String symbolicName) {
		if (this.otEquinox != null)
			return this.otEquinox.isDeniedAspectPlugin(symbolicName);
		return false;
	}
}
