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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIThisVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.objectteams.otdt.debug.IOTDebugEventListener;
import org.eclipse.objectteams.otdt.debug.OTDebugElementsContainer;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;
import org.eclipse.objectteams.otdt.debug.TeamInstance;
import org.eclipse.objectteams.otdt.debug.core.breakpoints.OOTBreakpoints;

public class TeamBreakpointListener implements IJavaBreakpointListener
{
	private static TeamBreakpointListener _singleton;

	public static TeamBreakpointListener getInstance()
	{
		if(_singleton == null)
			_singleton = new TeamBreakpointListener();
		
		return _singleton;
	}
	
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint)
	{
	    OTDebugElementsContainer otDebugElementsContainer = getDebugElementsContainer(thread.getLaunch());
	    if (otDebugElementsContainer == null)
	    	return DONT_CARE; // something's broken for OT, don't interfere the debugger

	    // without a marker it can't be a valid OOT breakpoint
	    if (!breakpoint.getMarker().exists())
	    	return DONT_CARE;

		try
		{
			if (OOTBreakpoints.isOOTConstructorBreakpoint(breakpoint))
			{
			  // A team is being instantiated!
              JDIThisVariable teamVariable = (JDIThisVariable) thread.findVariable(OOTBreakpoints.FIELD_THIS);
              if (otDebugElementsContainer.getTeamInstance(teamVariable) == null) {
	              TeamInstance newTeam= otDebugElementsContainer.addTeamInstance(teamVariable);
	              notifyTeamInstantiation(otDebugElementsContainer, newTeam);
              }
              return IJavaBreakpointListener.DONT_SUSPEND;
			}
			
			if (OOTBreakpoints.isOOTFinalizeBreakpoint(breakpoint))
			{
			  // A team is being disposed!
              IJavaVariable teamVariable = thread.findVariable(OOTBreakpoints.FIELD_THIS);
              int idx= otDebugElementsContainer.removeTeamInstance(teamVariable);
              if (idx != -1)
            	  notifyTeamFinalize(otDebugElementsContainer, idx);
              return IJavaBreakpointListener.DONT_SUSPEND;
			}
			
			if (OOTBreakpoints.isOOTActiveMethodBreakpoint(breakpoint))
			{
				IJavaVariable teamVariable = thread.findVariable(OOTBreakpoints.FIELD_THIS);
				TeamInstance teamInstance = otDebugElementsContainer.getTeamInstance(teamVariable);

				IJavaVariable teamActiveThread = thread.findVariable(OOTBreakpoints.LOCAL_THREAD_ACT_DEACT);
				IValue threadValue = teamActiveThread.getValue();
				long threadID = ((JDIObjectValue)threadValue).getUniqueId();
				teamInstance.setActiveForThreadID(threadID);
				
				IJavaVariable teamGlobalThread = thread.findVariable(OOTBreakpoints.FIELD_ALL_THREADS);
				IValue globalThreadValue = teamGlobalThread.getValue();
				long globalThreadID = ((JDIObjectValue)globalThreadValue).getUniqueId();
				teamInstance.setGlobalThreadID(globalThreadID);
				
				notifyActivationState(teamInstance);
				return IJavaBreakpointListener.DONT_SUSPEND;
			}

			if (OOTBreakpoints.isOOTDeactiveMethodBreakpoint(breakpoint))
			{
				IJavaVariable teamVariable = thread.findVariable(OOTBreakpoints.FIELD_THIS);
				TeamInstance teamInstance = otDebugElementsContainer.getTeamInstance(teamVariable);
				
				IJavaVariable teamDeactiveThread= thread.findVariable(OOTBreakpoints.LOCAL_THREAD_ACT_DEACT);
				IValue threadValue = teamDeactiveThread.getValue();
				long threadID = ((JDIObjectValue)threadValue).getUniqueId();
				teamInstance.setInactiveForThreadID(new Long(threadID));
				
				notifyActivationState(teamInstance);
				return IJavaBreakpointListener.DONT_SUSPEND;
			}
			
			if (OOTBreakpoints.isOOTImplicitActiveMethodBreakpoint(breakpoint))
			{
				IJavaVariable teamVariable = thread.findVariable(OOTBreakpoints.FIELD_THIS);
				TeamInstance teamInstance = otDebugElementsContainer.getTeamInstance(teamVariable);
				
				// don't use fragile access to local variable 
				// but directly use the current thread:
				long threadID= ((JDIThread)thread).getUnderlyingThread().uniqueID();
				teamInstance.setImplicitActiveForThreadID(threadID);
				
				notifyActivationState(teamInstance);
				return IJavaBreakpointListener.DONT_SUSPEND;
			}

			if (OOTBreakpoints.isOOTImplicitDeactiveMethodBreakpoint(breakpoint))
			{
				IJavaVariable teamVariable = thread.findVariable(OOTBreakpoints.FIELD_THIS);
				TeamInstance teamInstance = otDebugElementsContainer.getTeamInstance(teamVariable);
				
				// don't use fragile access to local variable 
				// but directly use the current thread:
				long threadID = ((JDIThread)thread).getUnderlyingThread().uniqueID();
				teamInstance.setImplicitInactiveForThreadID(threadID);
				
				notifyActivationState(teamInstance);
				return IJavaBreakpointListener.DONT_SUSPEND;
			}
		}
		catch (Exception ex)
		{
	 		OTDebugPlugin.getExceptionHandler().logException("Teammonitor can't read infos from debugTarget anymore. Disconnected?", ex); //$NON-NLS-1$
			//if something fails, let the debugger go on
			return IJavaBreakpointListener.DONT_SUSPEND;
		}

      return IJavaBreakpointListener.DONT_CARE;
	}
	
    protected OTDebugElementsContainer getDebugElementsContainer(ILaunch launch)
    {
        return (OTDebugElementsContainer) launch.getAdapter(OTDebugElementsContainer.class);
    }

    private void notifyTeamInstantiation(OTDebugElementsContainer container, TeamInstance newTeam)
    {
        for (IOTDebugEventListener listener : OTDebugPlugin.getDefault().getOTDebugEventListeners())
            listener.teamInstantiated(newTeam);
    }

    private void notifyTeamFinalize(OTDebugElementsContainer container, int idx)
    {
        for (IOTDebugEventListener listener : OTDebugPlugin.getDefault().getOTDebugEventListeners())
            listener.teamDisposed(idx);
    }
    
    private void notifyActivationState(TeamInstance teamInstance)
    {
        for (IOTDebugEventListener listener : OTDebugPlugin.getDefault().getOTDebugEventListeners())
            listener.activationStateChanged(teamInstance);
    }
    
	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {}
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {}
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {}
	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {}
	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {}
	public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type)
	{
		return IJavaBreakpointListener.DONT_CARE;
	}

	public void dispose()
	{
		_singleton = null;
	}
}
