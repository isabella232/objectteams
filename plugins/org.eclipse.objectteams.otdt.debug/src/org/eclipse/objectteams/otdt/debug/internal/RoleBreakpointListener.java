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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.internal;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

/**
 * The purpose of this listener is to prevent that attempts are made
 * to install a breakpoint of a role into the enclosing team or a 
 * sibling role. In Java, all inner classes reside in the file of
 * the same outermost enclosing type. This is not true for roles.
 * 
 * @author stephan
 * @since 1.1.4
 */
public class RoleBreakpointListener implements IJavaBreakpointListener 
{
	private static RoleBreakpointListener _singleton= null;
	public static RoleBreakpointListener getInstance() {
		if(_singleton == null)
			_singleton = new RoleBreakpointListener();
		return _singleton;
	}

	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {}
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {}
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {}
	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {}
	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {}
	
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		return IJavaBreakpointListener.DONT_CARE;
	}

	public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type)
	{
		try {
			String typeName = type.getName();
			String breakpointTypeName = breakpoint.getTypeName();
			if (   typeName.contains(IOTConstants.OT_DELIM)
				|| breakpointTypeName.contains(IOTConstants.OT_DELIM)) 
			{
				if (!breakpointTypeName.equals(typeName))
					// for roles we need to be stricter than for regular inner classes:
					// don't try to install a role breakpoint into any other type 
					return IJavaBreakpointListener.DONT_INSTALL;
			}
		} catch (Exception e) {
			// could not retrieve name, don't interfere with current action.
		}
		return IJavaBreakpointListener.DONT_CARE;
	}

	public void dispose()
	{
		_singleton = null;
	}
}
