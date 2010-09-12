/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTDebugElementAdapterFactory.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.objectteams.otdt.debug.OTDebugElementsContainer;
import org.eclipse.objectteams.otdt.debug.TeamInstance;
import org.eclipse.objectteams.otdt.debug.ui.internal.model.OTDebugElementsContainerContentProvider;
import org.eclipse.objectteams.otdt.debug.ui.internal.model.OTDebugElementsContainerLabelProvider;
import org.eclipse.objectteams.otdt.debug.ui.internal.model.OTDefaultModelProxyFactory;
import org.eclipse.objectteams.otdt.debug.ui.internal.model.OTVariableColumnFactoryAdapter;
import org.eclipse.objectteams.otdt.debug.ui.internal.model.TeamInstanceContentProvider;
import org.eclipse.objectteams.otdt.debug.ui.internal.model.TeamInstanceLabelProvider;

/** 
 * This factory installs our content/label providers into the TeamView
 */
public class OTDebugElementAdapterFactory implements IAdapterFactory 
{
	
	private static IModelProxyFactory fgModelProxyFactoryAdapter= new OTDefaultModelProxyFactory();
	private static IColumnPresentationFactory fgVariableColumnFactory = new OTVariableColumnFactoryAdapter();

    private static IElementLabelProvider   _LPElementContainer= new OTDebugElementsContainerLabelProvider();
	private static IElementLabelProvider   _LPTeamInstance=     new TeamInstanceLabelProvider();
    
	private static IElementContentProvider _CPElementContainer= new OTDebugElementsContainerContentProvider();
    private static IElementContentProvider _CPTeamInstance=     new TeamInstanceContentProvider();


	public Object getAdapter(Object adaptableObject, Class adapterType)
	{ 
        if (adapterType.equals(IElementContentProvider.class)) 
        {
        	if(adaptableObject instanceof OTDebugElementsContainer)
        		return _CPElementContainer;

        	if(adaptableObject instanceof TeamInstance)
        		return _CPTeamInstance;
        }
        
        if (adapterType.equals(IElementLabelProvider.class))
        {
        	if(adaptableObject instanceof OTDebugElementsContainer)
        		return _LPElementContainer;

        	if(adaptableObject instanceof TeamInstance)
        		return _LPTeamInstance;
        }
        if (adapterType.equals(IModelProxyFactory.class)) {
        	if (adaptableObject instanceof OTDebugElementsContainer)
        		return fgModelProxyFactoryAdapter;
        }

        if (adapterType.equals(IColumnPresentationFactory.class)) {
        	if (adaptableObject instanceof OTDebugElementsContainer) {
        		return fgVariableColumnFactory;
        	}
        }
        
		return null;
	}

	public Class[] getAdapterList()
	{
		return new Class[]{IElementContentProvider.class,
						   IElementLabelProvider.class,
						   IModelProxyFactory.class,
						   IColumnPresentationFactory.class};
	}

}
