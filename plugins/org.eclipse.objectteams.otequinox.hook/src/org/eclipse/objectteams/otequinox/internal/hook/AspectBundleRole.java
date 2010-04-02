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
 * $Id: AspectBundleRole.java 23461 2010-02-04 22:10:39Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal.hook;

public class AspectBundleRole {

	boolean isLoading = false;
	
	AspectBundleRole(boolean isLoading) {
		this.isLoading = isLoading;
	}

	static boolean isLoadingTeams(BundleRegistry bundleRegistry, String bundleID) 
	{
		AspectBundleRole aspect = bundleRegistry.aspectBundles.get(bundleID);
		if (aspect == null)
			return false;
		return aspect.isLoading;
	}

	static void markLoadingTeams(BundleRegistry bundleRegistry, String bundleID, boolean flag) {
		AspectBundleRole aspect = bundleRegistry.aspectBundles.get(bundleID);
		if (aspect != null)
			aspect.isLoading = flag;	
	}

}
