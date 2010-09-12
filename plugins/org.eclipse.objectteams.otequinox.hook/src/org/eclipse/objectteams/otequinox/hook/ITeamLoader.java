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
package org.eclipse.objectteams.otequinox.hook;


import org.osgi.framework.Bundle;

/**
 * Interface to the team loading mechanism.
 * 
 * @author stephan
 * @since OTDT 1.1.4
 */
public interface ITeamLoader {

	/**
 	 * 	Load all teams registered as adapting `baseBundle'.
	 *  Also load all adapted base classes from the base bundle.
	 * 
	 * @param baseBundle   the bundle to be adapted 
	 * @param classScanner object to be used for scanning class attributes
	 * @return whether loading teams was successful.
	 *         If so, instantiateTeams() should be invoked later.
	 */
	public boolean loadTeams(Bundle baseBundle, ClassScanner classScanner);

    /**
	 * Load all internal teams of bundle, i.e., teams that adapt classes from their
	 * enclosing plug-in only.
	 *  
	 * @param bundle this bundle is aspect and base at the same time
	 * @param scanner class scanner which collects the affected base and role classes.
	 * @return
	 */
	public boolean loadInternalTeams(Bundle bundle, ClassScanner scanner);

	/**
	 * Instantiate all teams for baseBundle which have been loaded via loadTeams().
	 * @param baseBundle
	 * @param triggerClassName class who's loading triggered this instantiation request.
	 */
	public void instantiateTeams(final Bundle baseBundle, String triggerClassName);

	/**
	 * Triggered once class <code>org.objectteams.Team</code> has been loaded and can be further initialized.
	 */
	void initializeOOTeam(Class<?> clazz);
}