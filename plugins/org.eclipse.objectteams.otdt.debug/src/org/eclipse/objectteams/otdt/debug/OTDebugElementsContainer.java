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
 * $Id: OTDebugElementsContainer.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIThisVariable;
import org.eclipse.objectteams.otdt.debug.internal.util.TeamActivationOrderComparator;
import org.eclipse.objectteams.otdt.debug.internal.util.TeamActivationTimeComparator;
import org.eclipse.objectteams.otdt.debug.internal.util.TeamInstantiantionComparator;
import org.eclipse.objectteams.otdt.debug.internal.util.TeamNameComparator;

/**
 * One instance of this class exists per launch (ensured by OTDebugElementsContainerFactory).
 * At each point in time, each OTDebugElementsContainer also refers to a specific "context" - 
 * either of:
 * <ul>
 * <li> ILaunch </li>
 * <li> IThread </li>
 * <li> IStackFrame </li>
 * </ul>
 * Depending on the context of the Container a current thread can be requested (IThread/IStackFrame).
 * 
 * A list of known team instances is maintained, which is always sorted according to the 
 * current sorting mode.
 * 
 * @author ike
 * $Id: OTDebugElementsContainer.java 23427 2010-02-03 22:23:59Z stephan $
 */
public class OTDebugElementsContainer implements IAdaptable
{
    private ArrayList<TeamInstance> _teamInstances = new ArrayList<TeamInstance>();
    private IDebugElement _context;
	private String _sortMode;

	/** @category lifecycle */
	public Object getAdapter(Class adapter)
	{
		return AdapterManager.getDefault().getAdapter(this, adapter);
	}
	
	/** @category lifecycle */
	public IDebugElement getContext()
	{
		return _context;
	}

	/**
	 * @category lifecycle 
	 * 
	 * @param context
	 * @return true if the context actually changed (refresh needed).
	 */
	public boolean setContext(IDebugElement context)
	{
		if (this._context == context)
			return false;
		_context = context;
		return true; 
	}
	
	/** @category lifecycle */
	public void dispose()
	{
		_teamInstances.clear();
	}
	
	/** @throws DebugException 
	 * @category modification */
    public TeamInstance addTeamInstance(JDIThisVariable teamVariable) 
    		throws DebugException
    {
    	TeamInstance teamInstance = getTeamInstance(teamVariable);
    	if (teamInstance== null)
        {
        	teamInstance = new TeamInstance(teamVariable);
        	_teamInstances.add(teamInstance);
    		sortTeamInstances();
        }
        
        return teamInstance;
    }
    
    /** @category modification */
    public int removeTeamInstance(IJavaVariable teamVariable)
    {
    	for (int i=0; i<this._teamInstances.size(); i++)
    	{
    		if(this._teamInstances.get(i).equals(teamVariable))
    		{
    			_teamInstances.remove(i);
    			return i;
    		}
		}
    	return -1;
    }
    
    /** @category modification */
	public void setSortMode(String sortMode)
	{
		String oldSortMode= _sortMode;
		_sortMode = sortMode;
		if (sortMode != null && !sortMode.equals(oldSortMode))
			sortTeamInstances();
	}

	/** @category modification */
	private void sortTeamInstances()
	{
		if (_sortMode == null)
			return;
		if(_sortMode.equals(IOTDTDebugPreferenceConstants.TEAMS_BY_ACTIVATION_TIME))
		{
			Collections.sort(_teamInstances, new TeamActivationTimeComparator());
		}
		if(_sortMode.equals(IOTDTDebugPreferenceConstants.TEAMS_BY_ACTIVATION_ORDER))
		{
			Collections.sort(_teamInstances, new TeamActivationOrderComparator());
		}
		if(_sortMode.equals(IOTDTDebugPreferenceConstants.TEAMS_BY_INSTANTIATION))
		{
			Collections.sort(_teamInstances, new TeamInstantiantionComparator());
		}
		if(_sortMode.equals(IOTDTDebugPreferenceConstants.TEAMS_BY_NAME))
		{
			Collections.sort(_teamInstances, new TeamNameComparator());
		}
	}

	/** @category query */
	public boolean hasTeamInstances()
	{
		return !_teamInstances.isEmpty();
	}

	/** @category query */
	public int getChildCount() {
		return _teamInstances.size();
	}

	/** @category query */
    public TeamInstance getTeamInstance(IJavaVariable teamVariable)
    {
    	for (Iterator iter = _teamInstances.iterator(); iter.hasNext();)
    	{
			TeamInstance teamInstance = (TeamInstance) iter.next();
    		if(teamInstance.equals(teamVariable))
    			return teamInstance;
    	}
		return null;
    }
    
    /** @category query */
    public int getIndexOfTeamInstance(TeamInstance teamInstance) {
    	for (int i = 0; i < this._teamInstances.size(); i++)
			if (this._teamInstances.get(i).equals(teamInstance))
				return i;

    	return -1;
    }

    /** @category query */
    public ArrayList<TeamInstance> getTeamInstances()
    {
		return _teamInstances;
    }

    /** 
     * @category query
     * 
     * If the current context is either an IJavaThread or an IStackFrame,
     * return the (corresponding) thread.
     */
	public IJavaThread getContextThread() {
		if (this._context instanceof IJavaThread)
			return (IJavaThread) this._context;
		if (this._context instanceof IStackFrame) 
			return (IJavaThread)((IStackFrame) this._context).getThread();
		return null; // no specific thread selected
	}

	/** 
	 * @category query
	 * 
	 * If the current context has a thread, ask whether it is suspended,
	 * otherwise ask whether some thread is suspended (by using canResume()).
	 */
	public boolean isSuspended() {
		IJavaThread contextThread = getContextThread();
		if (contextThread != null && contextThread.isSuspended())
			return true;
		IDebugTarget target= null;
		if (this._context instanceof IDebugTarget)
			target = (IDebugTarget) this._context;
		else
			target= this._context.getDebugTarget();
		return target.canResume();
	}

}