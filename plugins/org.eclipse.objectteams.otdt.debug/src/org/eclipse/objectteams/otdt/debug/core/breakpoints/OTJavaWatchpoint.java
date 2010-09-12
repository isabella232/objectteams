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
 * $Id: OTJavaWatchpoint.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.core.breakpoints;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaWatchpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ModificationWatchpointEvent;

/**
 * FIXME(SH): THIS CLASS IS NOT USED.
 * 
 * @author ike
 * 
 * This class is for dealing with the Modification Watchpoint and getting the modified value.
 * 
 * $Id: OTJavaWatchpoint.java 23427 2010-02-03 22:23:59Z stephan $
 */
public class OTJavaWatchpoint extends JavaWatchpoint
{
    private Hashtable _threadToObjectRefTable;
// unused    private String _searchField;
    
    public OTJavaWatchpoint(IResource resource, String typeName, String fieldName, int lineNumber, int charStart, int charEnd, int hitCount, boolean register, Map attributes, String searchString) throws DebugException
    {
        super(resource, typeName, fieldName, lineNumber, charStart, charEnd, hitCount, register, attributes);
        
        _threadToObjectRefTable = new Hashtable();
// unused        _searchField = searchString;
    }
    
    /** Overrides the JavaWatchpoint.handleBreakpointEvent() method to handle
     *  the ModificationWatchpointEvent and to store the value, which
     *  will be set.
     */
    @Override
    public boolean handleBreakpointEvent(Event event, JDIThread thread, boolean suspendVote) 
    {
        if (event instanceof ModificationWatchpointEvent)
        {
            ModificationWatchpointEvent modEvent = (ModificationWatchpointEvent)event;
            Value currentValue = modEvent.valueCurrent();
            Value valueToBe = modEvent.valueToBe();
            ObjectReference objectRef = modEvent.object();
            
            Hashtable objectRefs;
            if (!_threadToObjectRefTable.containsKey(thread))
            {
                objectRefs = new Hashtable();
            }
            else
            {
                objectRefs = (Hashtable)_threadToObjectRefTable.get(thread);
            }
            
            Value [] values;
            if (!objectRefs.containsKey(objectRef))
            {
                values = new Value[2];
                
            }
            else
            {
                values = (Value [])objectRefs.get(objectRef);
            }
            
            values[0] = currentValue;
            values[1] = valueToBe;
            objectRefs.put(objectRef, values);
            
            _threadToObjectRefTable.put(thread, objectRefs);
        }
        return super.handleBreakpointEvent(event, thread, suspendVote);
    }
    
    public Value getValueToBe(IJavaThread thread, IJavaVariable variable) throws DebugException
    {
        Value [] values = getValues(thread, variable);
        
        if (values != null && values.length >=2)
            return values[1];
        else
            return null;
    }
    
    public Value getCurrentValue(IJavaThread thread, IJavaVariable variable) throws DebugException
    {
        Value [] values = getValues(thread, variable);
        
        if (values != null && values.length >=2)
            return values[0];
        else
            return null;
    }
    
    private Value[] getValues(IJavaThread thread, IJavaVariable variable) throws DebugException
    {
        Hashtable objRefTable = (Hashtable) _threadToObjectRefTable.get(thread);
        
        for (Iterator iter = objRefTable.keySet().iterator(); iter.hasNext();)
        {
            Value objRef = (ObjectReference) iter.next();
            JDIObjectValue value = (JDIObjectValue)variable.getValue();

            if (value.getUnderlyingObject().equals(objRef))
            {
                return (Value [])objRefTable.get(objRef);
            }
        }
        return null;
    }
}
