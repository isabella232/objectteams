/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ShowOTJLDAction.java 23436 2010-02-04 00:29:04Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.help.actions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.objectteams.otdt.ui.Messages;
import org.eclipse.objectteams.otdt.ui.help.OTHelpPlugin;
import org.eclipse.objectteams.otdt.ui.help.OTJLDError;
import org.eclipse.objectteams.otdt.ui.help.OTJLDError.OTURL;
import org.eclipse.objectteams.otdt.ui.help.views.OTJLDView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * @author gis
 */
public class ShowOTJLDAction implements IViewActionDelegate
{
	private IWorkbenchPartSite m_site;
    private OTJLDView m_otjldView;
    private OTURL[] m_urls;
    
    public void init(IViewPart view)
    {
        m_site = view.getSite();
    }
    
    public ShowOTJLDAction() { /* empty */ }
    
    private ShowOTJLDAction(IWorkbenchPartSite site, String text) {
    	m_site = site;
        m_urls = new OTJLDError(text).getURLs();
    }
    
    /** 
     * Create a configured action with appropriate label and image.
     * @param site connection to the workbench
     * @param text the error message text containing the OTJLD reference. 
     */
    public static IAction createAction(IWorkbenchPartSite site, String text) {
    	final ShowOTJLDAction action = new ShowOTJLDAction(site, text);
    	return new Action(Messages.UpdateRulerAction_goto_otjld_command_label) {
			@Override
			public void run() {
				action.run(null);
			}
			@Override
			public ImageDescriptor getImageDescriptor() {
				return OTHelpPlugin.getOTJLDImage();
			}
		};
    }

    public void run(IAction action)
    {
        if (m_urls == null || m_urls.length == 0) // TODO (carp): error message in statusbar
            return;
        
        try
        {
            if (OTJLDView.hasBrowser())
            {
            	IWorkbenchPage page = m_site != null ? m_site.getPage() : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				m_otjldView = (OTJLDView) page.showView(OTHelpPlugin.OTJLD_VIEW);
				m_otjldView.setURL(m_urls[0].getURL());
            }
        }
        catch (PartInitException ex)
        {
            OTHelpPlugin.getExceptionHandler().logException("Unable to initialize browser", ex); //$NON-NLS-1$
        }
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
        try
        {
            evaluateSelection(selection);
            action.setEnabled(m_urls != null && m_urls.length > 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void evaluateSelection(ISelection selection)
    {
        m_urls = null;
        
        if (!(selection instanceof IStructuredSelection))
            return;
        
        IStructuredSelection sel = (IStructuredSelection) selection;
        if (sel.size() != 1) // precondition failed, should be handled by enablesFor in plugin.xml
            return;
        
        Object item = sel.getFirstElement();
        IMarker marker = null;
        if (item instanceof MarkerItem)
        	marker = ((MarkerItem) item).getMarker();
        else if (item instanceof IMarker)
            marker = (IMarker) item;
        
        if (marker == null) return; // either no type matched, or MarkerCategory.getMarker() returned null. 
        
        try
        {
            if (!IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER.equals(marker.getType()))
                return;

            String text = marker.getAttribute(IMarker.MESSAGE, null);
            if (text == null)
                return;
            OTJLDError error = new OTJLDError(text);
            m_urls = error.getURLs();
        }
        catch (CoreException ex)
        {
            OTHelpPlugin.getExceptionHandler().logCoreException("Cannot retrieve marker from selection", ex); //$NON-NLS-1$
        }
    }
}
