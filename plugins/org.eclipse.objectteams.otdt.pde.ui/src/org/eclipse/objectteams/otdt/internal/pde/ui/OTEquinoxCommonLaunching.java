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
 * $Id: OTEquinoxCommonLaunching.java 23470 2010-02-05 19:13:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.pde.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;
import org.eclipse.objectteams.otdt.debug.TeamBreakpointInstaller;

/**
 * Shared implementation of OTEclipseApplicationLaunchConfiguration and OTEquinoxLaunchConfiguration
 * 
 * @author stephan
 * @since OTDT 1.1.3
 */
public class OTEquinoxCommonLaunching 
{
	static final String HOOK_CONFIGURATOR = "-Dosgi.hook.configurators.include=org.eclipse.objectteams.otequinox.hook.HookConfigurator";//$NON-NLS-1$
	static final String CLASSLOADER_LOCKING = "-Dosgi.classloader.lock=classname"; //$NON-NLS-1$
	static final String REPOSITORY_WORKAROUND = "-Dot.equinox"; //$NON-NLS-1$ // this causes the WORKAROUND_REPOSITORY flag being set to true in OTRE.
	static final String OT_DEBUG_VMARG = "-Dot.debug"; //$NON-NLS-1$
	static final String[] OT_VM_ARGS = { HOOK_CONFIGURATOR, CLASSLOADER_LOCKING, REPOSITORY_WORKAROUND };
	static final String[] OT_VM_DEBUG_ARGS = { HOOK_CONFIGURATOR, CLASSLOADER_LOCKING, REPOSITORY_WORKAROUND, OT_DEBUG_VMARG };

	static String[] extendVMArguments(String[] args, String mode) {
		String[] otArgs = OT_VM_ARGS;
		if (mode != null && mode.equals(ILaunchManager.DEBUG_MODE))
			otArgs = OT_VM_DEBUG_ARGS;
	
		if (args == null || args.length == 0)
			return otArgs;
	
		String[] combinedArgs = new String[args.length + otArgs.length];
		System.arraycopy(args, 0, combinedArgs, 0, args.length);
		System.arraycopy(otArgs, 0, combinedArgs, args.length, otArgs.length);
		return combinedArgs;
	}
	
	static void installOOTBreakpoints(IProject[] projects)
			throws CoreException 
	{
		IJavaProject jp = null;
		if (projects != null) {
			for (IProject project : projects) {
				// find org.objectteams.Team in any OT/J Project:
				if (project.getNature(JavaCore.OTJ_NATURE_ID) != null) {
					jp = JavaCore.create(project);
					TeamBreakpointInstaller.installTeamBreakpoints(jp);
					break;
				}
			}
		}
		if (jp == null)
			OTDebugPlugin.getDefault().getLog().log(
					new Status(Status.WARNING, 
							   OTPDEUIPlugin.PLUGIN_ID, 
							   0, 
							   OTPDEUIMessages.NoOTJPluginProject, 
							   null));
	}
}
