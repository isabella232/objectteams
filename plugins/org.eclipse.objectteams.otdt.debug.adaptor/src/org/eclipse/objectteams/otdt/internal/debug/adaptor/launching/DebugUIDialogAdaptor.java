/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: DebugUIDialogAdaptor.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;
import org.eclipse.pde.internal.ui.IPDEUIConstants;

import base org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import base org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationTabGroupViewer;
import base org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;

/**
 * This team conditionally adds the "Team Activation" tab to the launch configuration dialog.
 * Only the most specific subteam is activated.
 * 
 * @author stephan
 * @since 1.2.1
 */
@SuppressWarnings("restriction")
public team class DebugUIDialogAdaptor {

	
	protected class LaunchConfigurationTabGroup playedBy AbstractLaunchConfigurationTabGroup {

		boolean isOTJlaunch = false;
		
		/** Catch the dialog to find out if the current launch is ot-enabled. */
		void checkProject(LaunchConfigurationsDialog dialog) 
				<- before void createTabs(ILaunchConfigurationDialog dialog, String mode)
				with { dialog <- (LaunchConfigurationsDialog)dialog } 
		void checkProject(LaunchConfigurationsDialog dialog) {
			isOTJlaunch = dialog != null && dialog.isOTJlaunch();
		}

		/** Intercept installation of the tabs, as to conditionally insert our tab. */
		void setTabs(ILaunchConfigurationTab[] tabs) <- replace void setTabs(ILaunchConfigurationTab[] tabs)
				when(this.isOTJlaunch && tabs != null);
		callin void setTabs(ILaunchConfigurationTab[] tabs) {
			int len = tabs.length;
			int insertPos = tabs.length;
			for(int i=0; i<len; i++) {
				if (tabs[i] instanceof AbstractLaunchConfigurationTab) {
					AbstractLaunchConfigurationTab launchConfigurationTab = (AbstractLaunchConfigurationTab)tabs[i];
					if ("org.eclipse.jdt.debug.ui.javaArgumentsTab".equals(launchConfigurationTab.getId())) { //$NON-NLS-1$
						insertPos = i+1;
						break;
					}
				} else if (tabs[i] instanceof OTLaunchConfigurationTab) {
					base.setTabs(tabs); // already present, don't insert again (for legacy launches).
					return;
				}
			}
			ILaunchConfigurationTab[] newTabs = new ILaunchConfigurationTab[len+1];
			for (int i=0,j=0; j<=len; j++)
				if (j != insertPos)
					newTabs[j] = tabs[i++];
			newTabs[insertPos] = new OTLaunchConfigurationTab();
			base.setTabs(newTabs);
		}
	}
	/** Gate to base-level information, here: whether current launch is ot-enabled. */
	protected class LaunchConfigurationsDialog playedBy LaunchConfigurationsDialog 
	{
		@SuppressWarnings("decapsulation")
		LaunchConfigurationTabGroupViewer getTabViewer() -> LaunchConfigurationTabGroupViewer getTabViewer();
		
		protected boolean isOTJlaunch() {
			LaunchConfigurationTabGroupViewer viewer = getTabViewer();
			if (viewer == null) return false;
			ILaunchConfigurationWorkingCopy workingCopy = viewer.getWorkingCopy();
			if (workingCopy == null) return false;
			try {
				return workingCopy.getAttribute(OTDebugPlugin.OT_LAUNCH, false)
					&& workingCopy.getAttribute(IPDEUIConstants.LAUNCHER_PDE_VERSION, (String)null) == null;
			} catch (CoreException e) {
				return false;
			}
		}
	}
	/** Only exposing one inaccessible method. */
	protected class LaunchConfigurationTabGroupViewer playedBy LaunchConfigurationTabGroupViewer {
		@SuppressWarnings("decapsulation")
		ILaunchConfigurationWorkingCopy getWorkingCopy() -> ILaunchConfigurationWorkingCopy getWorkingCopy();
	}

}
