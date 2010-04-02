/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MasterTeamLoader.java 15426 2007-02-25 12:52:19Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal.hook;

import org.eclipse.objectteams.otequinox.hook.HookConfigurator;
import org.eclipse.objectteams.otequinox.hook.ILogger;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;


/**
 * Log either to console or - as soon as it is initialized - via the TransformerPlugin.
 * 
 * @author stephan
 * @since OTDT 1.1.4
 */
public class Logger implements ILogger
{
	private FrameworkLog fwLog;
	
	public Logger(FrameworkLog fwLog) {
		this.fwLog = fwLog;
	}

	public void log(Throwable t, String msg) {
		log(HookConfigurator.class.getPackage().getName(), t, msg);
	}
	public void log(String pluginID, Throwable t, String msg) {
		if (this.fwLog != null) {
			this.fwLog.log(new FrameworkLogEntry(pluginID, FrameworkLogEntry.ERROR, 0, msg, 0, t, null));
			return;
		} else {			
			// no success logging, print to console instead:
			System.err.println("OT/Equinox: "+msg); //$NON-NLS-1$
			t.printStackTrace();
		}
	}
	
	public void log(int status, String msg) {
		if (status >= Util.WARN_LEVEL)		
			doLog(HookConfigurator.class.getPackage().getName(), status, msg);
	}
	public void log(String pluginID, int status, String msg) {
		if (status >= Util.WARN_LEVEL)
			doLog(pluginID, status, msg);
	}
	
	public void doLog(int status, String msg) {
		doLog(HookConfigurator.class.getPackage().getName(), status, msg);
	}
	public void doLog(String pluginID, int status, String msg) {
		if (this.fwLog != null) {
			this.fwLog.log(new FrameworkLogEntry(pluginID, status, 0, msg, 0, null, null));
		} else {
			// no success logging, print to console instead:
			msg = "OT/Equinox: "+msg; //$NON-NLS-1$
			if ((status & Util.ERROR) != 0)
				System.err.println(msg);
			else
				System.out.println(msg);
		}
	}
}
