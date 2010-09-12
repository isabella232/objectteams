/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTJLDViewActionDelegate.java 23436 2010-02-04 00:29:04Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.help.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.objectteams.otdt.ui.help.OTJLDError;
import org.eclipse.objectteams.otdt.ui.help.views.OTJLDView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.browser.BrowserViewer;

/**
 * @author mosconi
 */
@SuppressWarnings("restriction")
public class OTJLDViewActionDelegate implements IViewActionDelegate {
	
	private OTJLDView view = null;
	private BrowserViewer viewer = null;
	
	public void init(IViewPart view) {
		if (viewer == null) {
			this.view = (OTJLDView) view;
			this.viewer = this.view.getViewer();			
		}
	}

	public void run(IAction action) {
		if (action.getId().equals("org.eclipse.objectteams.otdt.ui.help.OTJLDView.next")) //$NON-NLS-1$
			viewer.forward();
		if (action.getId().equals("org.eclipse.objectteams.otdt.ui.help.OTJLDView.back")) //$NON-NLS-1$
			viewer.back();
		if (action.getId().equals("org.eclipse.objectteams.otdt.ui.help.OTJLDView.home")) //$NON-NLS-1$
			this.view.setURL(OTJLDError.getHomepageURL().getURL());
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
