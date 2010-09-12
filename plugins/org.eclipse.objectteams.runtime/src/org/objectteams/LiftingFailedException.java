/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2003-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: LiftingFailedException.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.objectteams;

/**
 * This exception signals that lifting failed due to unresolved
 * binding ambiguity.
 */
public class LiftingFailedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object base;
	private String roleType;
	
	/**
	 * @param base the object that should be lifted
	 * @param roleType the name of the role type for which
	 *                 lifting was attempted.
	 */
	public LiftingFailedException(Object base, String roleType) {
		this.base = base;
		this.roleType = roleType;
	}
	
	public String getMessage() {
		return "\nFailed to lift '" + base + "' of " + base.getClass()
				+ " to type '" + roleType
				+ "'\n(See OT/J definition para. 2.3.4(c)).";
	}
}
