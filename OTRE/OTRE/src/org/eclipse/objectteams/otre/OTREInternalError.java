/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2003-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

/**
 * @author stephan 
 */
public class OTREInternalError extends Error {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String _bugmsg = 
        "An error occurred in the Object Teams runtime environment.\n"+
        "We would appreciate if you send a bug report to bugs@ObjectTeams.org.\n"+
        "Please include your program (if possible) and the following diagnostic\n"+
        "in your report. -- Thank you. The OT/J developers\n";
    /**
     * 
     */
    public OTREInternalError() {
        super(_bugmsg);
    }

    /**
     * @param message
     */
    public OTREInternalError(String message) {
        super(_bugmsg+message);
    }

    /**
     * @param cause
     */
    public OTREInternalError(Throwable cause) {
        super(_bugmsg+cause.toString());
    }

    /**
     * @param message
     * @param cause
     */
    public OTREInternalError(String message, Throwable cause) {
        super(_bugmsg+message+cause.toString());
    }

}
