/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTLaunchConfigMigrationDelegate.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug;

import java.util.HashMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.core.LaunchConfiguration;
import org.eclipse.debug.internal.core.LaunchConfigurationInfo;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * This class migrates old OT-launches (Java App, OT/Equinox FW, OTEclipse App) into regular
 * Eclipse launches, but marked as OTlaunch, thereby enabling the OTRE, OT/Equinox, resp.
 * 
 * @author stephan
 * @since 1.2.2
 */
public class OTLaunchConfigMigrationDelegate implements ILaunchConfigurationMigrationDelegate {

	final static HashMap<String,String> oldToNew = new HashMap<String, String>();
	static {
		oldToNew.put(OTDebugPlugin.OT_LAUNCH_CONFIGURATION_TYPE, 	IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		// 'referencing' constants from downstream plug-ins (old, removed launch configuration types)
		oldToNew.put("org.objectteams.otdt.pde.ui.OTEquinoxLaunchConfigurationType", 		  "org.eclipse.pde.ui.EquinoxLauncher");  //$NON-NLS-1$ //$NON-NLS-2$
		oldToNew.put("org.objectteams.otdt.pde.ui.EclipseApplicationLaunchConfigurationType", "org.eclipse.pde.ui.RuntimeWorkbench"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	
	@SuppressWarnings("nls")
	public boolean isCandidate(ILaunchConfiguration candidate) throws CoreException {
		String candidateType = candidate.getType().getIdentifier();
		return   candidateType.startsWith("org.objectteams.otdt")
			  && oldToNew.containsKey(candidateType);
	}

	public void migrate(final ILaunchConfiguration candidate) throws CoreException {
		final String newId = oldToNew.get(candidate.getType().getIdentifier());
		if (newId == null) return;
		ILaunchConfiguration wrapper = new LaunchConfiguration(candidate.getMemento()) {
			@Override
			protected LaunchConfigurationInfo getInfo() throws CoreException {
				return new LaunchConfigurationInfo() {
					@Override
					protected ILaunchConfigurationType getType() {
						// revert to plain Eclipse launch type:
						return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(newId);
					}
					@SuppressWarnings("unchecked")
					@Override
					protected TreeMap getAttributes() {
						try {
							TreeMap orig = (TreeMap) candidate.getWorkingCopy().getAttributes();
							// but ensure it is marked as an OT-launch:
							orig.put(OTDebugPlugin.OT_LAUNCH, Boolean.TRUE);
							return orig;
						} catch (CoreException e) { /* silent. */ }
						return new TreeMap(); // must not return null
					}
				};
			}
		};
		wrapper.getWorkingCopy().doSave();
	}

}
