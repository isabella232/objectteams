/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2004-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: UnsupportedFeatureException.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.objectteams;

/**
 * @author resix
 */
public class UnsupportedFeatureException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String _bugmsg = "\nThe program encountered an unsupported situation! ";

	/**
	 * 
	 */
	public UnsupportedFeatureException() {
		super(_bugmsg);
	}

	/**
	 * @param message
	 */
	public UnsupportedFeatureException(String message) {
		super(_bugmsg + "\n" + message);
		StackTraceElement[] ste = new StackTraceElement[0];
		setStackTrace(ste);
	}

	/**
	 * @param cause
	 */
	public UnsupportedFeatureException(Throwable cause) {
		super(_bugmsg + cause.toString());
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnsupportedFeatureException(String message, Throwable cause) {
		super(_bugmsg + message/*+cause.toString()*/);
	}
}
