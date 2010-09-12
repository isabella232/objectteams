/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007, 2009 Technical University Berlin, Germany.
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
package org.eclipse.objectteams.otequinox.hook;

import org.osgi.framework.Bundle;

/**
 * This slice of the IOTEquinoxService provides features to query the
 * registry of aspectBindings.
 * 
 * @author stephan
 * @since OTDT 1.1.4
 */
public interface IAspectRegistry {

	/** Are we running within the OTDT? */
	public boolean isOTDT();
	
	/** Is `symbolicName' the name of a base plugin for which an adapting team is registered? */
	public boolean isAdaptedBasePlugin(String baseBundleName);

	/**
  	 * Get the names of aspect plugins adapting a given base plugin.
	 * @param basePlugin base plugin.
	 * @return non-null array of symbolic names of aspect plugins.
 	 */
	public String[] getAdaptingAspectPlugins(Bundle baseBundle);

	/**
	 *  Get the plugin IDs of all base plugins adapted by this aspect plugin.
	 *  If this plugin is not an aspect plugin return null.
	 *  @param aspectBundle potential aspect plugin
	 *  @return array of base plugin IDs or null.
	 */
	public String[] getAdaptedBasePlugins(Bundle aspectBundle);

	/**
	 * Does `bundle' have internal teams, i.e., teams that adapt classes from their
	 * enclosing plug-in only?
	 * @param bundle
	 * @return
	 */
	public boolean hasInternalTeams(Bundle bundle);

	/**
	 * Does the symbolic name refer to an aspect bundle for which some permission was denied?
	 * @param symbolicName
	 * @return
	 */
	public boolean isDeniedAspectPlugin(String symbolicName);
}