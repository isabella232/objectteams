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
 * $Id: InternalCompilerError.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.exceptions;

/**
 * @author gis
 */
@SuppressWarnings("serial")
public class InternalCompilerError extends Error
{
    public InternalCompilerError()
    {
        super();
    }
    
    public InternalCompilerError(String message)
    {
        super(message);
    }
    
    public InternalCompilerError(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    private static final String INTRO = new String(
            "You discovered a bug in the Object Teams Development Tooling." + (char)Character.LINE_SEPARATOR +  //$NON-NLS-1$
    		"Please consider filing a bug including this stacktrace and a description how to reproduce at https://bugs.eclipse.org/bugs/enter_bug.cgi?product=Objectteams" + (char)Character.LINE_SEPARATOR + //$NON-NLS-1$
    		"Thank you -- the OTDT Development Team." + (char)Character.LINE_SEPARATOR); //$NON-NLS-1$
    
	public String getMessage()
    {
        return INTRO + super.getMessage();
    }
	
	public static void log(String message)
	{
	    new InternalCompilerError(message).printStackTrace(System.out);
	}
	
	public static void log(String message, Throwable cause)
	{
	    new InternalCompilerError(message, cause).printStackTrace(System.out);
	}
}
