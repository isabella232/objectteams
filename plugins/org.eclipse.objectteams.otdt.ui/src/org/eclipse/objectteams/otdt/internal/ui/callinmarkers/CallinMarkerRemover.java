/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CallinMarkerRemover.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.callinmarkers;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;

/**
 * @author gis
 */
public class CallinMarkerRemover
{
    public static void removeCallinMarkers(IResource resource) throws CoreException
    {
    	for (String id : new String[] { CallinMarker.PLAYEDBY_ID, CallinMarker.CALLIN_ID, CallinMarker.CALLOUT_ID} )
			if (resource.exists())
				resource.deleteMarkers(id, true, IResource.DEPTH_INFINITE);
    }
    
	public static void removeCallinMarker(IMember member, IResource resource)
    {
        // we need to pass the resource, as the method might already be removed and hence would
        // not be able to give us a resource.
    	if (resource.exists())
    	{
	        try
            {
                IMarker marker;
                if (member.getElementType() == IJavaElement.METHOD) {
	                marker = getCallinMarker(member, CallinMarker.CALLIN_ID, resource);
	                if (marker != null)
	                    marker.delete();
                }
                // method or field:
                marker = getCallinMarker(member, CallinMarker.CALLOUT_ID, resource);
                if (marker != null)
                    marker.delete();
            }
	        catch (ResourceException ex) {
	        	// tree might be locked for modifications
	        	// FIXME(SH): handle this case, currently we just ignore this situation
	        }
            catch (CoreException ex)
            {
    			OTDTUIPlugin.getExceptionHandler().
				 logException("Problems removing callin marker", ex); //$NON-NLS-1$
            }
    	}
    }
    
    /**
     * Finds the marker attached to the given method.
     * Note: may return null if nothing found.
     */
    private static IMarker getCallinMarker(IMember baseElement, String markerKind, IResource resource) throws JavaModelException, CoreException
    {
        IMarker[] markers = resource.findMarkers(markerKind, true, IResource.DEPTH_INFINITE);

        String methodId = baseElement.getHandleIdentifier();
        
        for (int i = 0; i < markers.length; i++)
        {
            if (methodId.equals(markers[i].getAttribute(CallinMarker.ATTR_BASE_ELEMENT, null)))
                return markers[i];
        }
        return null;
    }
    

    public static void removeCallinMarkers(IClassFile element) throws CoreException
    {
		if (element.exists())
		{
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IMarker[] allMarkers = CallinMarker.getAllBindingMarkers(root);
			
			for (IMarker marker : allMarkers)
				if (JavaCore.isReferencedBy(element, marker))
					marker.delete();
    	}
    }
    

    
}
