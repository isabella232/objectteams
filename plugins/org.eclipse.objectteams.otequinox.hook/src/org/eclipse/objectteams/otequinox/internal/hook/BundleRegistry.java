/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BundleRegistry.java 23461 2010-02-04 22:10:39Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal.hook;

import java.util.HashMap;

import org.eclipse.objectteams.otequinox.hook.ClassScanner;
import org.eclipse.objectteams.otequinox.hook.IAspectRegistry;
import org.eclipse.objectteams.otequinox.hook.ITeamLoader;
import org.osgi.framework.Bundle;

/** Maintain mappings from plugin ids to base/aspect bundles.
 *  Please just think of this class as a team and {Base,Aspect}BundleRole
 *  as its roles ;-)
 */
public class BundleRegistry {
	
	HashMap<String,BaseBundleRole> adaptedBaseBundles = new HashMap<String, BaseBundleRole>();

	HashMap<String,AspectBundleRole> aspectBundles = new HashMap<String, AspectBundleRole>();

	public boolean isAdaptedBaseBundle(String symbolicName) {
		return adaptedBaseBundles.containsKey(symbolicName);
	}

	void createAspectRole (String symbolicName) {
		aspectBundles.put(symbolicName, new AspectBundleRole(true));	
	}
	
	void resetLoading (String[] aspects) {
		for (String aspect : aspects) 
			aspectBundles.get(aspect).isLoading = false;
	}

	/** Found an adapted base directly.
	 *  After this the bundle is waiting for transformation. 
	 */
	BaseBundleRole createWaitingBase(Bundle baseBundle) 
	{
		String symbolicName = baseBundle.getSymbolicName();
		BaseBundleRole baseRole = this.adaptedBaseBundles.get(symbolicName);
		if (baseRole == null) {
			baseRole = new BaseBundleRole(baseBundle);
			this.adaptedBaseBundles.put(symbolicName, baseRole);
		}
		baseRole.setup(baseBundle, BaseBundleRole.State.WAIT_FOR_TEAM);
		return baseRole;
	}

	/** Delegate to the TransformerPlugin to perform PHASE 1 of aspect activation:
	 *  + loading of teams adapting baseBundle
	 * @param baseBundle TODO
	 * @param registry
	 * @param loader
 	 * @param scanner  instance to use for scanning OT byte code attributes.
	 * @return  a collection of names of adapted base classes (non-null)
	 */
	void checkLoadTeams (Bundle baseBundle, IAspectRegistry registry, ITeamLoader loader, ClassScanner scanner) 
	{
	// DEBUG:
	//		if (isKnowID(baseBundle.getSymbolicName()))
	//			System.out.println(">>3>>"+baseBundle.getSymbolicName());
		
		String[] aspectBundleNames = registry.getAdaptingAspectPlugins(baseBundle);
		if (aspectBundleNames.length == 0)
			return;
		
		try {
			for (String aspect : aspectBundleNames)
				this.createAspectRole(aspect); // enable transformation for these plugins
			
			// requesting PHASE 2 (request might be answered during loadTeams)
			BaseBundleRole baseRole = this.createWaitingBase(baseBundle);
					
			baseRole.loadTeams(loader, scanner);
		} finally {
			this.resetLoading(aspectBundleNames);
		}
	}
}
