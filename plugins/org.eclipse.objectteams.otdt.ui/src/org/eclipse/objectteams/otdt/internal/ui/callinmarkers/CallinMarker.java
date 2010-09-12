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
 * $Id: CallinMarker.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.callinmarkers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;

/**
 * @author gis
 */
public class CallinMarker
{
    public static final String CALLIN_ID          = "org.eclipse.objectteams.otdt.ui.callinMarker"; //$NON-NLS-1$
    public static final String CALLOUT_ID         = "org.eclipse.objectteams.otdt.ui.calloutMarker"; //$NON-NLS-1$
    public static final String PLAYEDBY_ID        = "org.eclipse.objectteams.otdt.ui.playedByMarker"; //$NON-NLS-1$
    public static final String ATTR_BASE_ELEMENT  = "org.eclipse.objectteams.otdt.ui.markerAttr.BaseElement"; //$NON-NLS-1$
    public static final String ATTR_ROLE_ELEMENTS = "org.eclipse.objectteams.otdt.ui.markerAttr.RoleElements"; //$NON-NLS-1$

    private Map<String, Object> _attribs = new HashMap<String, Object>(11);
    private String id;
    
    public CallinMarker(String markerKind)
    {
        super();
        this.id = markerKind;
        _attribs.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_INFO));
		_attribs.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_HIGH));
    }

    public void setLineNumber(int line)
    {
		_attribs.put(IMarker.LINE_NUMBER, new Integer(line));
    }
    
    public void setNameRange(ISourceRange nameRange) {
    	_attribs.put(IMarker.CHAR_START, nameRange.getOffset());
    	_attribs.put(IMarker.CHAR_END, nameRange.getOffset()+nameRange.getLength());
    }
    
    /** define the roles or callin mappings referenced by the base element. */
    public <M extends IMember> void setRoleElement(Set<M> roleElements)
    {
    	StringBuffer encoded = new StringBuffer();
    	for (IMember m : roleElements) {
			encoded.append(m.getHandleIdentifier());
			encoded.append('\n');
		}
		this._attribs.put(CallinMarker.ATTR_ROLE_ELEMENTS, encoded.toString());
    }

    /** set the base element (class or method) to which this marker is attached. */
    public void setBaseElement(IMember baseElement)
    {
		_attribs.put(CallinMarker.ATTR_BASE_ELEMENT, baseElement.getHandleIdentifier());
		if (baseElement.getElementType() == IJavaElement.TYPE) {
			_attribs.put(IMarker.MESSAGE, OTDTUIPlugin.getResourceString("CallinMarker.playedby_tooltip")+' '+baseElement.getElementName()); //$NON-NLS-1$
		} else if (this.id == CALLIN_ID) {
			_attribs.put(IMarker.MESSAGE, OTDTUIPlugin.getResourceString("CallinMarker.callin_tooltip")+' '+baseElement.getElementName()+"()"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (this.id == CALLOUT_ID) {
			String baseMemberName = baseElement.getElementName();
			if (baseElement.getElementType() == IJavaElement.METHOD) // not for callout to field
				baseMemberName += "()"; //$NON-NLS-1$
			_attribs.put(IMarker.MESSAGE, OTDTUIPlugin.getResourceString("CallinMarker.callout_tooltip") + ' ' + baseMemberName); //$NON-NLS-1$ 
		}
    }

    /**
     * Actually create the marker.
     * PRE: all relevant attributes have been set before.
     * 
     * @param target the target element where to attach the marker (based on a resource or a java element)
     * @throws CoreException thrown when a marker operation failed.
     */
    @SuppressWarnings("unchecked") // IMarker.getAttributes() yields raw type
	public void create(AbstractMarkable target) throws CoreException
    {
		IMarker marker = target.createMarker(this.id);
		Map markerAttributes = marker.getAttributes();
		if (markerAttributes != null)
			_attribs.putAll(markerAttributes); // markers for JavaElements have already one attribute
		marker.setAttributes(_attribs);
		_attribs.clear(); // clear attributes for reuse of this object
    }

	/** get all playedBy and callin markers for the given resource. */
	public static IMarker[] getAllBindingMarkers(IResource resource) throws CoreException {
		IMarker[] markers1 = resource.findMarkers(PLAYEDBY_ID, true, IResource.DEPTH_INFINITE);
		IMarker[] markers2 = resource.findMarkers(CALLIN_ID, true, IResource.DEPTH_INFINITE);
		IMarker[] markers3 = resource.findMarkers(CALLOUT_ID, true, IResource.DEPTH_INFINITE);
		int len1 = markers1.length, len2 = markers2.length, len3 = markers3.length;
		IMarker[] result = new IMarker[len1+len2+len3];
		System.arraycopy(markers1, 0, result, 0, len1);
		System.arraycopy(markers2, 0, result, len1, len2);
		System.arraycopy(markers3, 0, result, len1+len2, len3);
		return result;
	}

	public static boolean isTypeMarker(IMarker marker) {
		try {
			return marker.getType().equals(PLAYEDBY_ID);
		} catch (CoreException e) {
			return false;
		}
	}

}
