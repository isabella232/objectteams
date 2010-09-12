/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007, 2010 Technical University Berlin, Germany.
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
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugUIPlugin;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.actions.ShowOTInternalVariablesAction;

import base org.eclipse.debug.internal.ui.model.elements.StackFrameContentProvider;
import base org.eclipse.debug.internal.ui.model.elements.VariableContentProvider;

/**
 * Implements variables filtering for VariablesView and TeamView.
 * <ul>
 * <li>TeamView always filters statics.</li>
 * <li>Conditionally filter internal OT/J variables (starting with _OT$); 
 *     This filter is controlled by view specific preferences.</li>
 * </ul>
 * 
 * @author stephan
 * @since 1.1.6
 */
@SuppressWarnings("restriction")
public team class VariablesViewAdaptor 
{
	/** Adapt the content provider for root elements of the variables view. */
	protected class StackFrameContentProvider playedBy StackFrameContentProvider {

		Object[] getAllChildren(Object parent, IPresentationContext context, IViewerUpdate monitor) 
			<- replace Object[] getAllChildren(Object parent, IPresentationContext context, IViewerUpdate monitor);

		callin Object[] getAllChildren(Object parent, IPresentationContext context, IViewerUpdate monitor) 
				throws CoreException 
		{
			Object[] rawChildren = base.getAllChildren(parent, context, monitor);
	        
			if (!context.getId().equals(IDebugUIConstants.ID_VARIABLE_VIEW))
	        	return rawChildren;
	        
			// is filtering needed?
        	if (ShowOTInternalVariablesAction.includeOTInternal(context))
				return rawChildren;

        	// at this point always filter internals, but toplevel never has statics to filter:
	        return filterChildren(rawChildren, true/*OTInternals*/, false/*statics*/); 
		}
		
	}

	/** Adapt the content provider for non-toplevel elements. */
	protected class ContentProvider playedBy VariableContentProvider 
	{
		Object[] getValueChildren(IDebugElement parent, IValue value, IPresentationContext context) 
		<- replace Object[] getValueChildren(IDebugElement parent, IValue value, IPresentationContext context);

		callin Object[] getValueChildren(IDebugElement parent, IValue value, IPresentationContext context)
				throws CoreException 
		{
			Object[] rawChildren = base.getValueChildren(parent, value, context);
			
			// is filtering needed?
			boolean filterOTInternals = ! ShowOTInternalVariablesAction.includeOTInternal(context);		
			
			// TeamView constantly filters statics:
			boolean filterStatics = context.getId().equals(OTDebugUIPlugin.TEAM_VIEW_ID);
			
			if (filterOTInternals || filterStatics)			
				return filterChildren(rawChildren, filterOTInternals, filterStatics);
			
			return rawChildren;
		}

	}

	/**
	 * Filter from rawChildren ot-internal variables and/or static fields.
	 */
	static Object[] filterChildren(Object[] rawChildren, boolean filterOTInternals, boolean filterStatics) {
		List<Object> visible = new ArrayList<Object>();
		
		for (int i= 0; i<rawChildren.length; i++)
			if (!isFiltered((IVariable)rawChildren[i], filterOTInternals, filterStatics))
				visible.add(rawChildren[i]);
		
		// has filtering taken place?
		if (visible.size() == rawChildren.length)
			return rawChildren;
		return visible.toArray(new Object[visible.size()]);
	}
	// helper checking conditions for above method:
	static boolean isFiltered(IVariable var, boolean filterOTInternals, boolean filterStatics) {
		try {
			if (   filterOTInternals
				&& var.getName().startsWith(IOTConstants.OT_DOLLAR)
				&& !var.getName().equals(String.valueOf(IOTConstants._OT_BASE))) // always show _OT$base
				return true;
			if (filterStatics && var instanceof IJavaFieldVariable)
				if (((IJavaFieldVariable)var).isStatic())
					return true;
		} catch (DebugException e) {
			return true; // cannot display anyway.
		}
		return false;
	}
	
	
}
