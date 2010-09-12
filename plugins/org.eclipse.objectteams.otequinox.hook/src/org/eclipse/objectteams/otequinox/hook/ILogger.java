/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008, 2009 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.hook;

import org.eclipse.osgi.framework.log.FrameworkLogEntry;

/**
 * Our own variant of a logger interface, just slightly tailored variants of log methods.
 * @author stephan
 * @since 1.2.0
 */
public interface ILogger {
	
	int ERROR   = FrameworkLogEntry.ERROR;
	int WARNING = FrameworkLogEntry.WARNING;
	int INFO    = FrameworkLogEntry.INFO;
	int OK      = FrameworkLogEntry.OK;
	
	public void log(Throwable t, String msg);
	public void log(int status, String msg);
	/** Same as above but do not filter by warnlevel. */
	public void doLog(int status, String msg);
	public void log(String pluginID, Throwable ex, String msg);
	public void log(String pluginID, int status, String msg);
}
