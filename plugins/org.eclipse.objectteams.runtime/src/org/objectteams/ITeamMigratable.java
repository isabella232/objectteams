/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2008 Berlin Institute of Technology, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ITeamMigratable.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.objectteams;

/**
 * Marker interface: if a role declares to implement this interface
 * the compiler will generate the method defined herein, and prepare
 * the role so that the migration will indeed be possible.
 * Note, that a migratable role does not obey the family guarantee.
 * 
 * @author stephan
 * @since 1.2.5
 */
public interface ITeamMigratable {
	/**
	 * Migrate the current role to the otherTeam.
	 * 
	 * @param otherTeam new team that should adopt this role
	 * @return the migrated (and re-typed) role (actually of type R<@otherTeam>).  FIXME(SH)
	 */
	<R> R migrateToTeam(final ITeam otherTeam);
}
