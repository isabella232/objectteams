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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;

/**
 * @author ike
 *  
 */
public class StepFromLinenumberGenerator implements IDebugEventFilter, ISMAPConstants
{
    private static StepFromLinenumberGenerator _stepGenerator;
    
    public static StepFromLinenumberGenerator getInstance()
    {
        if (_stepGenerator == null)
        {
            _stepGenerator = new StepFromLinenumberGenerator();
        }
        
        return _stepGenerator;
    }
    
    public DebugEvent[] filterDebugEvents(DebugEvent[] events)
    {
        for (int index = 0; index < events.length; index++)
        {
            DebugEvent event = events[index];
            
            JDIThread thread = getThreadFromEvent(event);
            
            if (thread == null)
            {
                return events;
            }
                
            try
            {
                IStackFrame topStackframe = getStackFrame(thread);
                                
                if (isResumeEvent(event) || (topStackframe == null))
                {
                    return events;
                }
                
                //every time a STEP_OVER_LINENUMBER is detected, step over it
                if (isStepOverElement(topStackframe) && isSuspendEvent(event))
                {
                    generateStepOver(thread);
                    return filterEvent(events, index);
                }
                
                //every time a STEP_INTO_LINENUMBER is detected step into it
                if (isStepIntoElement(topStackframe) && isSuspendEvent(event))
                {
                    generateStepInto(thread);
                    return filterEvent(events, index);
                }
                
            }
            catch (DebugException e)
            {
                //TODO(ike): handle exception
                e.printStackTrace();
                return events;
            }
        }
        
        return events;
    }

    private boolean isResumeEvent(DebugEvent event)
    {
        return (event.getKind() == DebugEvent.RESUME);
    }

    private boolean isSuspendEvent(DebugEvent event)
    {
        return (event.getKind() == DebugEvent.SUSPEND);
    }

    private boolean isStepOverElement(IStackFrame topStackframe) throws DebugException
    {
        if (topStackframe.getLineNumber() == STEP_OVER_LINENUMBER)
            return true;
        
        return false;
    }

    private boolean isStepIntoElement(IStackFrame topStackframe) throws DebugException
    {
        if (topStackframe.getLineNumber() == STEP_INTO_LINENUMBER)
            return true;
        
        return false;
    }
    
    private IStackFrame getStackFrame(JDIThread thread) throws DebugException 
    {
        if (thread.hasStackFrames())
        {
            return thread.getTopStackFrame();
        }
        return null;
    }
    
    private JDIThread getThreadFromEvent(DebugEvent event)
    {
        if (event.getSource() instanceof JDIThread)
        {
            return (JDIThread) event.getSource();
        }
        
        return null;
    }
    
    
    /** Filter event with given index.
     */
    private DebugEvent[] filterEvent(DebugEvent[] events, int index)
    {
        DebugEvent[] filtered = new DebugEvent[events.length - 1];
        if (filtered.length > 0)
        {
            int j = 0;
            for (int idx = 0; idx < events.length; idx++)
            {
                if (idx != index)
                {
                    filtered[j] = events[idx];
                    j++;
                }
            }
        }
        return filtered;
    }
    
    /** Generates and performs a single stepInto in the given thread.
     */
    private void generateStepInto(JDIThread thread)
    {
        IJavaDebugTarget debugTarget = (IJavaDebugTarget) thread.getDebugTarget();
        
        try
        {
            thread.stepInto();
        }
        catch (DebugException e)
        {
            DebugPlugin.getDefault().fireDebugEventSet( new DebugEvent[] { new DebugEvent(debugTarget, DebugEvent.CHANGE) });
        }
    }
    
    
    /** Generates and performs a single stepOver in the given thread.
     */
    private void generateStepOver(JDIThread thread)
    {
        IJavaDebugTarget debugTarget = (IJavaDebugTarget) thread.getDebugTarget();
        
        try
        {
            thread.stepOver();
        }
        catch (DebugException e)
        {
            DebugPlugin.getDefault().fireDebugEventSet( new DebugEvent[] { new DebugEvent(debugTarget, DebugEvent.CHANGE) });
        }
    }
}
