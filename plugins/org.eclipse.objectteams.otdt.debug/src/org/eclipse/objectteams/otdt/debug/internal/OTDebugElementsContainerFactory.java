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
 * $Id: OTDebugElementsContainerFactory.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.internal;

import java.util.HashMap;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.objectteams.otdt.debug.OTDebugElementsContainer;

/**
 * @author ike
 *
 * $Id: OTDebugElementsContainerFactory.java 23427 2010-02-03 22:23:59Z stephan $
 */
public class OTDebugElementsContainerFactory implements IAdapterFactory, ILaunchesListener2 {
    
    private HashMap<Object, OTDebugElementsContainer> _launchToDebugModel = new HashMap<Object, OTDebugElementsContainer>();
    
    public OTDebugElementsContainerFactory()
    {
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
    }

    public Object getAdapter(Object adaptableObject, Class adapterType)
    {
        if (!_launchToDebugModel.containsKey(adaptableObject))
        {
            if (OTDebugElementsContainer.class.equals(adapterType))
            {
                _launchToDebugModel.put(adaptableObject, new OTDebugElementsContainer());
            }
        }
        
        return _launchToDebugModel.get(adaptableObject);
    }
    
    public Class[] getAdapterList()
    {
        return new Class [] { OTDebugElementsContainer.class };
    }

    //TODO(ike): check, why there are sometimes 2 launches
    public void launchesTerminated(ILaunch[] launches)
    {
        for (int idx = 0; idx < launches.length; idx++)
        {
            _launchToDebugModel.remove(launches[idx]);   
        }
    }
    
    public void launchesRemoved(ILaunch[] launches) {}
    public void launchesAdded(ILaunch[] launches) {}
    public void launchesChanged(ILaunch[] launches) {}
    
    public void dispose()
    {
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
        _launchToDebugModel.clear();
    }
}