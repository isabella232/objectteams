/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTREBlock.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.launching;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.SWTFactory;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.DebugMessages;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.OTDebugAdaptorPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Common addition for all launches, Java App or OSGi.
 * Adds the "Object Teams Runtime" section.
 * 
 * Applied via subclasses (roles).
 * 
 * @author stephan
 * @since 1.2.2
 */
@SuppressWarnings("restriction")
public abstract class OTREBlock 
{
	protected String enableCheckboxLabel = DebugMessages.OTLaunching_OTRE_checkbox_label; // overidable default
	
	Button _otreToggleButton;
	boolean _useOTRE;
	
	public abstract Button createCheckButton(Composite group, String resourceString);
	public abstract void setDirty(boolean dirty);
	public abstract void updateLaunchConfigurationDialog();
	
	// create the "Object Teams Runtime" section:
	public Group createOTRESection(Composite parent, boolean useSWTFactory) {
		Group group;
		if (useSWTFactory) {
			group = SWTFactory.createGroup(parent, DebugMessages.OTLaunching_OTRE_group_title+':', 1, 1, GridData.FILL_HORIZONTAL);
		} else {
			group = new Group(parent, SWT.NONE);
			group.setText(DebugMessages.OTLaunching_OTRE_group_title);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			group.setLayout(layout);
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
        this._otreToggleButton = createCheckButton(group, this.enableCheckboxLabel); 
        this._otreToggleButton.addSelectionListener(new SelectionAdapter() 
	        {
				public void widgetSelected(SelectionEvent e) {
	                OTREBlock.this._useOTRE = OTREBlock.this._otreToggleButton.getSelection();
	                setDirty(true);
	                updateLaunchConfigurationDialog();
	            }
	        });
        
        return group;
	}
	
	// read values from 'config':
	public void initializeFrom(ILaunchConfiguration config) {
        try {
			tryInitializeFrom(config, hasOTJProject(config));
        }
        catch (CoreException ex) {
            OTDebugAdaptorPlugin.getDefault().getLog().log(
            		new Status(Status.ERROR, 
            				   OTDebugAdaptorPlugin.PLUGIN_ID, 
            				   DebugMessages.OTLaunching_loading_failed_msg,  
            				   ex));
        }
	}
	protected void tryInitializeFrom(ILaunchConfiguration config, boolean hasOTJProject) throws CoreException {
		this._otreToggleButton.setEnabled(hasOTJProject);
		this._useOTRE = config.getAttribute(OTDebugPlugin.OT_LAUNCH, this._useOTRE);
		this._otreToggleButton.setSelection(this._useOTRE);
	}
	
	boolean hasOTJProject(ILaunchConfiguration config) {
		try {
			String projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
			if (projectName != null) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (project != null && project.hasNature(JavaCore.OTJ_NATURE_ID))
					return true;
			}
		} catch (CoreException e) {
			// problems with either config or project, obviously not a sound OT-launch
		}
		return false;
	}
	
	// apply the value from the checkbox:
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (this._otreToggleButton != null)
			config.setAttribute(OTDebugPlugin.OT_LAUNCH, this._useOTRE);
    }
}
