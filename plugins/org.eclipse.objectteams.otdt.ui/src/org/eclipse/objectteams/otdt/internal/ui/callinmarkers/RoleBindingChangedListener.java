/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleBindingChangedListener.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.callinmarkers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * This listener receives notification of changes to any java elements.
 * It reacts upon:
 * <ul>
 * <li>changes to methods inside base classes;
 * 	   when a bound base method is removed or added
 *     a callin marker is removed or newly created respectively.
 * <li>changes to a role class; 
 *     these trigger that callin markers for its baseclass need to be re-computed.
 * </ul>
 *
 * @author brcan
 * @version $Id: RoleBindingChangedListener.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class RoleBindingChangedListener implements IElementChangedListener
{
	public void elementChanged(ElementChangedEvent event)
	{
		Set<ICompilationUnit> invalidatedCUs = new HashSet<ICompilationUnit>();
        updateCallinMarkers(new IJavaElementDelta[] { event.getDelta() }, invalidatedCUs);
        for (ICompilationUnit cu : invalidatedCUs)
			OTDTUIPlugin.getDefault().getCallinMarkerCreator().invalidateBaseMarkable(new ResourceMarkable(cu.getResource()));
	}

	private void updateCallinMarkers(IJavaElementDelta[] deltas, Set<ICompilationUnit> invalidatedCUs)
	{
		if (deltas != null)
		{
//		    if (deltas.length > 0)
//		        _CallinMarkerCreator2.reset();
		    Set<IOTType> invalidatedRoles = new HashSet<IOTType>();
		    Set<IMember> addedBaseMembers = new HashSet<IMember>();
		    ICompilationUnit curCU = null;
			
			for (int idx = 0; idx < deltas.length; idx++)
			{
				IJavaElementDelta curDelta = deltas[idx];

				final IJavaElement curElem = curDelta.getElement();
	
				// check for nested deltas
				if (curElem instanceof IParent)
				{
					// visit child's deltas recursively				
					updateCallinMarkers(curDelta.getAffectedChildren(), invalidatedCUs);
				}
				// addition/removal of role types:
				if (   curElem instanceof IType 
					&& (   curDelta.getKind() == IJavaElementDelta.REMOVED
						|| curDelta.getKind() == IJavaElementDelta.ADDED
						|| curDelta.getFlags() == IJavaElementDelta.F_SUPER_TYPES)) 
				{
					IType roleType = (IType)curElem;
					IOTType otType = OTModelManager.getOTElement(roleType);
					if (otType != null && otType.isRole()) {
						if (invalidatedRoles.add(otType)) // only if not already handled
							invalidateRole((IRoleType)otType, roleType);
					}
				}
				// changes of base methods & fields:
				if (curElem instanceof IMethod || curElem instanceof IField)
				{
					handleDeltaKind(curDelta, (IMember)curElem, addedBaseMembers);
					curCU = ((IMember)curElem).getCompilationUnit();
				}
				// any changes in method mappings:
				if (curElem instanceof IMethodMapping) {
					// changes in method mappings invalidate the current role's baseclass:
					IJavaElement parent = curElem.getParent();
					if (parent instanceof IRoleType) {
						IRoleType roleParent = (IRoleType)parent;
						if (invalidatedRoles.add(roleParent)) // only if not already handled
							invalidateRole(roleParent, (IType) roleParent.getCorrespondingJavaElement());
					}
				}
			}
			if (!addedBaseMembers.isEmpty()) {
				if (addedBaseMembers.size() < 3 && !invalidatedCUs.contains(curCU))
					for (IMember baseMember : addedBaseMembers)
						baseMemberAdded(baseMember);
				else
					invalidatedCUs.add(curCU);
			}
		}
	}
	private void invalidateRole(IRoleType otType, IType roleType) {
		IType baseClass = null;
		try {
			baseClass = otType.getBaseClass();
		} catch (JavaModelException e) { 
			/* ignore, proceed with null baseclass */ 
		}
		OTDTUIPlugin.getDefault().getCallinMarkerCreator().invalidateRole(roleType, baseClass);
	}

    private void handleDeltaKind(IJavaElementDelta delta, IMember baseMember, Set<IMember> addedBaseMembers)
    {
		if (delta.getKind() == IJavaElementDelta.ADDED)
		{
			// don't yet handle, first count how many additions we have
            addedBaseMembers.add(baseMember);
		}
		else if (delta.getKind() == IJavaElementDelta.REMOVED)
		{
			baseMemberRemoved(baseMember, baseMember.getResource());
		}
    }
    
    // when renaming a method/field, we apparently get first an add-event, then a remove event.
	private void baseMemberAdded(IMember baseMember)
	{
	    IStatusLineManager statusLine = null;
	    IWorkbenchPage page = OTDTUIPlugin.getActivePage();
	    if (page != null && page.getActivePart() != null)
	    {
	        IWorkbenchPartSite site = page.getActivePart().getSite();
		    if (site instanceof IViewSite)
		        statusLine = ((IViewSite) site).getActionBars().getStatusLineManager();
	    }
	    
	    OTDTUIPlugin.getDefault().getCallinMarkerCreator().updateCallinMarker(baseMember, statusLine);
	}    

    private void baseMemberRemoved(IMember baseMember, IResource resource)
    {
		CallinMarkerRemover.removeCallinMarker(baseMember, resource);
	}
}

