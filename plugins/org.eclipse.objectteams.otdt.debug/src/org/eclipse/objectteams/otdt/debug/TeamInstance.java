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
 * $Id: TeamInstance.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIThisVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.sun.jdi.ThreadReference;

/**
 * @author ike
 */
//TODO(ike): what about launches and thread termination->notify and update _activethreads
public class TeamInstance extends JDIThisVariable implements IAdaptable
{
	public static final int IS_INACTIVE = 0;
	public static final int IS_IMPLICITACTIVE = 1;
	public static final int IS_ACTIVE = 2;

    private List<TeamThread> _activethreads;
    private List<TeamThread> _deactivethreads;
    private List<TeamThread> _implicitActivethreads;
    private int _activationState; // TODO(SH): use for caching
    private TeamThread _globalThread;;
    private long _creationTime;
    private long _activationTime;
    
    public TeamInstance(JDIThisVariable var) throws DebugException
    {
    	super((JDIDebugTarget)var.getDebugTarget(),
    		 ((JDIObjectValue)var.getValue()).getUnderlyingObject());
    	_activethreads = new ArrayList<TeamThread>();
    	_deactivethreads = new ArrayList<TeamThread>();
    	_implicitActivethreads = new ArrayList<TeamThread>();
    	_globalThread = new TeamThread(-1, 0);
    	_creationTime = System.currentTimeMillis();
    	_activationTime = 0;
    }
    
    public long getCreationTime()
    {
    	return _creationTime;
    }
    
    public long getActivationTime()
    {
    	return _activationTime;
    }

    public void setImplicitActiveForThreadID(long threadID)
    {
    	TeamThread teamThread = new TeamThread(threadID, System.currentTimeMillis());
    	_implicitActivethreads.add(teamThread);
    }
    
    public void setImplicitInactiveForThreadID(long threadID)
    {
    	TeamThread teamThread = new TeamThread(threadID, 0);
    	_implicitActivethreads.remove(teamThread);
    }

    public void setActiveForThreadID(long threadID)
    {
    	TeamThread teamThread = new TeamThread(threadID, System.currentTimeMillis());
    	_activethreads.add(teamThread);
    	_deactivethreads.remove(teamThread);
    }
    
    public void setInactiveForThreadID(long threadID)
    {
    	TeamThread teamThread = new TeamThread(threadID, System.currentTimeMillis());
    	_activethreads.remove(teamThread);
    	_deactivethreads.add(teamThread);
    }

    public List getThreads()
    {
    	return _activethreads;
    }

	public Object getAdapter(Class adapter)
	{
		return AdapterManager.getDefault().getAdapter(this, adapter);
	}
	
	public int getActivationState(IDebugElement debugElement)
	{
		IThread thread= null;
		if (debugElement instanceof IStackFrame) {
			IStackFrame stackFrame= (IStackFrame)debugElement;
			thread= stackFrame.getThread();
		} else if (debugElement instanceof IThread) {
			thread = (IThread)debugElement;
		} if (thread != null)
			return getActivationState(thread);
		return globalActivationState();
	}

	private int getActivationState(IThread thread)
	{
		if(isActiveFor(thread))
			return TeamInstance.IS_ACTIVE;
		else
			if(isImplicitActiveFor(thread))
				return TeamInstance.IS_IMPLICITACTIVE;
			else
				return TeamInstance.IS_INACTIVE;
	}
	
	public boolean isActiveFor(IThread thread)
	{
		boolean isActive= false;
		ThreadReference threadRef= null;
		if (thread != null) {
			threadRef= ((JDIThread)thread).getUnderlyingThread();
			TeamThread teamThread = new TeamThread(threadRef.uniqueID(), 0);
			isActive = _activethreads.contains(teamThread);
			if (isActive) {
				setActivationTime(_activethreads.get(_activethreads.indexOf(teamThread)).time);
				return true;
			}
			if (_deactivethreads.contains(teamThread))
				return false;
		}

		//team is perhaps global active 
		if(!isActive)
			isActive = (_activethreads.contains(_globalThread));

		return isActive;
	}

	private  int globalActivationState() {
		if (_activethreads.contains(_globalThread))
			return IS_ACTIVE;
		return IS_INACTIVE;
	}
	
	private void setActivationTime(long time)
	{
		_activationTime = time;
	}

	public boolean isImplicitActiveFor(IThread thread)
	{
		ThreadReference threadRef = ((JDIThread)thread).getUnderlyingThread();
		long threadID = threadRef.uniqueID();
		TeamThread teamThread= new TeamThread(threadID, 0);
		boolean isImplicitActive = _implicitActivethreads.contains(teamThread);
		
		if(isImplicitActive)
		{
			setActivationTime(_implicitActivethreads.get(_implicitActivethreads.indexOf(teamThread)).time);
		}
		
		return isImplicitActive;
	}
	
	public void setGlobalThreadID(long globalThreadID)
	{
		_globalThread.threadID = globalThreadID;
	}
}