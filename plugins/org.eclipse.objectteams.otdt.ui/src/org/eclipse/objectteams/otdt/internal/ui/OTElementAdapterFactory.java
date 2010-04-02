/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTElementAdapterFactory.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;


/**
 * This factory provides a WorkbenchAdapter for IAdaptable OT elements and
 * needs to be registered by the platforms AdapterManager.
 * 
 * @author kaiser
 * @version $Id: OTElementAdapterFactory.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class OTElementAdapterFactory implements IAdapterFactory
{
	WorkbenchAdapter _otAdapter   = new WorkbenchAdapter();
	Class[]          _allAdapters = { IWorkbenchAdapter.class };

    public Object getAdapter(Object adaptableObject, Class adapterType)
    {
    	Object adapter = null;
    	
    	if (IWorkbenchAdapter.class.equals(adapterType))
    	{
    		adapter = _otAdapter;
    	}
    	
    	return adapter;
    }

    public Class[] getAdapterList()
    {
        return _allAdapters;
    }
}
