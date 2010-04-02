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
 * $Id: ExceptionHandler.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.exceptions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

/**
 * Use this class to handle exceptions that should be logged.
 * 
 * @author brcan
 * @version $Id: ExceptionHandler.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class ExceptionHandler
{	
    private String _pluginId;
    
    /**
     * Use OT*Plugin.getExceptionHandler() instead of instantiating this yourself.
     * Note: Make sure to use the correct plugin, where your class resides.
     */
    public ExceptionHandler(String pluginId)
    {
    	_pluginId = pluginId;
    }

	public static ExceptionHandler getOTDTCoreExceptionHandler()
	{
		return new ExceptionHandler(JavaCore.PLUGIN_ID);
	}
    
    public void logException(Throwable ex)
    {
    	logException(null, ex);
    }

	public void logException(String msg, Throwable ex)
	{
		logErrorLog(_pluginId, ex);
	}

	public void logCoreException(String msg, CoreException ex)
	{
		logErrorLog(msg, ex);
	}

	/**
	 * Log to Error Log.
	 */
	private void logErrorLog(String msg, Throwable ex)
	{
		Bundle bundle = Platform.getBundle(_pluginId);
		if (bundle == null)
		{
			System.err.println("Warning: " + _pluginId + " is not a valid Plugin identifier!"); //$NON-NLS-1$ //$NON-NLS-2$
			ex.printStackTrace(System.err);
		}
		else
		{
			Platform.getLog(bundle).log(new Status(
								  IStatus.ERROR,
								  _pluginId,
								  IStatus.OK, 
								  msg,
								  ex));
		}
	}
}