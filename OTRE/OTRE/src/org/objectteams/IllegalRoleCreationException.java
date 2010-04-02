/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2007-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: IllegalRoleCreationException.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.objectteams;

/**
 * Exception to be thrown when a bound role is being instantiated but
 * the constructor does not assign a base object.
 *  
 * @author stephan
 */
@SuppressWarnings("serial")
public class IllegalRoleCreationException extends RuntimeException {
	public IllegalRoleCreationException() {
		super();
	}
	
	@Override
	public String getMessage() {
		return "Cannot instantiate a bound role using a default constructor of its tsuper class";
	}
}
