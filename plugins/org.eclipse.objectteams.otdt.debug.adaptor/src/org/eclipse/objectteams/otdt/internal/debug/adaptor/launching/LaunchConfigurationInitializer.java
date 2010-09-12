/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: LaunchConfigurationInitializer.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.launching;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.LaunchUtils;

import base org.eclipse.debug.internal.core.LaunchConfigurationType;
import base org.eclipse.debug.internal.core.LaunchConfigurationWorkingCopy;

/**
 * This team observes creation of launch configurations and initializes the 
 * org.eclipse.objectteams.launch attribute accordingly.
 * 
 * @author stephan
 * @since 1.2.1
 */
@SuppressWarnings("restriction")
public team class LaunchConfigurationInitializer {

	/** 
	 * Observe creation of new instances.
	 */
	protected class LaunchConfigurationType playedBy LaunchConfigurationType {

		void acceptInstance(LaunchConfigWC inst) 
			<- after ILaunchConfigurationWorkingCopy newInstance(IContainer container, String name)
			with { inst <- (LaunchConfigurationWorkingCopy)result }

		void acceptInstance(LaunchConfigWC inst) {
			// nop, just lifting. Role will take over.
		}
	}
	
	/** 
	 * While an instance is still new (= role exists) wait for the project name to be set.
	 */
	protected class LaunchConfigWC playedBy LaunchConfigurationWorkingCopy
			base when (LaunchConfigurationInitializer.this.hasRole(base, LaunchConfigWC.class))
	{
		// callout interface:
		boolean getAttribute(String key, boolean dflt) -> boolean getAttribute(String key, boolean dflt);
		void setAttribute(String key, boolean val)     -> void setAttribute(String key, boolean val);
		
		void setAttribute(String key, String value) <- after void setAttribute(String key, String value);
		void setAttribute(String key, String value) {
			if (key.equals(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME)) {
				try {
					if (   LaunchUtils.isOTJProject(value)
						&& !getAttribute(OTDebugPlugin.OT_LAUNCH, false))
					{
						setAttribute(OTDebugPlugin.OT_LAUNCH, true);
					}
				} catch (CoreException e) {
					OTDebugPlugin.getExceptionHandler().logException("Error getting a launch configuration attribute", e); //$NON-NLS-1$
				}
				// this role has served its purpose - unregister now:
				LaunchConfigurationInitializer.this.unregisterRole(this, LaunchConfigWC.class);
			}
			return;
		}
	}
}
