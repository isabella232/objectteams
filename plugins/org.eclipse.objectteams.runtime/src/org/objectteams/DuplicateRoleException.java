/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2004-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: DuplicateRoleException.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.objectteams;

/**
 * Signal a violation of OTJLD 2.4.1(c).
 * Also Team.getRole(Object) may throw a DuplicateRoleException if
 * more than one role is found for the given base object
 * (in that case those roles are found in different role-caches).
 * 
 * 
 * @author stephan
 * @version $Id: DuplicateRoleException.java 23408 2010-02-03 18:07:35Z stephan $
 */
public class DuplicateRoleException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public DuplicateRoleException(String roleClassName) {
		super("Failed to create a role instance of type "+roleClassName+"\n"+
			  "A role for the given base object already exists (OTJLD 2.4.1(c)).");
	}
	
	public DuplicateRoleException(String roleName1, String roleName2) {
		super("Ambiguous role instances: found a role in hierarchies "+
				roleName1+" and "+roleName2);
	}
}
