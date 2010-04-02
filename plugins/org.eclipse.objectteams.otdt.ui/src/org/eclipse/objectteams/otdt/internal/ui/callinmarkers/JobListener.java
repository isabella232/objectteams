/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JobListener.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.callinmarkers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;


abstract class JobListener extends JobChangeAdapter
{
    private final IStatusLineManager _statusLine;
    
    public JobListener(IStatusLineManager statusLine)
    {
        _statusLine = statusLine;
    }
    
	public void done(IJobChangeEvent event)
	{
	    IStatus status = event.getResult();
	    switch (status.getCode())
	    {
	    	case IStatus.ERROR:
	    	    if (_statusLine != null)
	    	        _statusLine.setErrorMessage(OTDTUIPlugin.getResourceString("CallinMarkerCreator2.search_failed_message")); //$NON-NLS-1$
	    		OTDTUIPlugin.getExceptionHandler().logException(status.getMessage(), status.getException());
	    	    break;
	    	case IStatus.CANCEL:
	    	    if (_statusLine != null)
	    	        _statusLine.setMessage(OTDTUIPlugin.getResourceString("CallinMarkerCreator2.search_canceled_message")); //$NON-NLS-1$
	    	    break;
	    	default:
	    	case IStatus.OK:
	    	    break; // do nothing
	    }
	    
	    jobFinished(status.getCode());
	}
	
	protected abstract void jobFinished(int status);
}