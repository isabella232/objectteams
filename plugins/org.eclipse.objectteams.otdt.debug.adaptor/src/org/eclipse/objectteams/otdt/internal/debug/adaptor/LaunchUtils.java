/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: LaunchUtils.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;

/** 
 * Simple queries relating to launches/launch-configs. 
 * @author stephan
 * @since 1.2.1 
 */
public class LaunchUtils {

	/** Does elem belong to an OT launch? */
	static boolean isOTLaunch(Object elem) {
		try {
			if (!(elem instanceof IDebugElement))
				return false;
			ILaunchConfiguration launchConfiguration = ((IDebugElement)elem).getLaunch().getLaunchConfiguration();
			return launchConfiguration.getAttribute(OTDebugPlugin.OT_LAUNCH, false);
		} catch (Exception e) {
			return false;
		}
	}

	/** Does projectName denote an OT/J-enabled project? */
	public static boolean isOTJProject(String projectName) {
		if (projectName != null && projectName.length() > 0) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project != null && project.exists())
				try {
					return project.hasNature(JavaCore.OTJ_NATURE_ID);
				} catch (CoreException e) { /* ignore */ }
		}
		return false;
	}

}
