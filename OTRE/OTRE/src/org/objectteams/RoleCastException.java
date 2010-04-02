/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2004-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleCastException.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.objectteams;

/**
 * This exception is thrown if a cast to a role class fails due to
 * different enclosing team instances.
 * @author Stephan Herrmann
 */
public class RoleCastException extends ClassCastException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String MSG = 
		"Different enclosing team instances (see OT/J language definition para. 1.2.4(b)).";

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage() {
		return MSG;
	}
}