/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PDELaunchingAdaptor.java 23461 2010-02-04 22:10:39Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.launching;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;
import org.eclipse.objectteams.otdt.debug.TeamBreakpointInstaller;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.DebugMessages;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.OTDebugAdaptorPlugin;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import base org.eclipse.pde.internal.ui.launcher.JREBlock;
import base org.eclipse.pde.ui.launcher.AbstractLauncherTab;
import base org.eclipse.pde.launching.AbstractPDELaunchConfiguration;
import base org.eclipse.pde.launching.JUnitLaunchConfigurationDelegate;

/**
 * This team adapts all Eclipse and OSGi launches (Launcher) and launch configurations (JREBlock and LauncherTab).
 * 
 * @author stephan
 * @since 1.2.2
 */
@SuppressWarnings("restriction")
public team class PDELaunchingAdaptor {
	
	/** Mediating between LauncherTab and JREBlock: */
	LauncherTab currentTab = null;
	
	static final String OSGI_EXTENSIONS       = "-Dosgi.framework.extensions=org.eclipse.objectteams.otequinox.hook"; //$NON-NLS-1$
	static final String HOOK_CONFIGURATOR     = "-Dosgi.hook.configurators.include=org.eclipse.objectteams.otequinox.hook.HookConfigurator";//$NON-NLS-1$
	static final String CLASSLOADER_LOCKING   = "-Dosgi.classloader.lock=classname"; //$NON-NLS-1$
	static final String ENABLE_OTEQUINOX      = "-Dot.equinox=1"; //$NON-NLS-1$     // this also causes the WORKAROUND_REPOSITORY flag being set to true in OTRE.
	static final String DISABLE_OTEQUINOX     = "-Dot.equinox=false"; //$NON-NLS-1$ // prevents TransformerHook installation and start of TransformerPlugin
	static final String OT_DEBUG_VMARG        = "-Dot.debug"; //$NON-NLS-1$
	static final String[] OT_VM_ARGS          = { OSGI_EXTENSIONS, HOOK_CONFIGURATOR, CLASSLOADER_LOCKING, ENABLE_OTEQUINOX };
	static final String[] OT_VM_DEBUG_ARGS    = { OSGI_EXTENSIONS, HOOK_CONFIGURATOR, CLASSLOADER_LOCKING, ENABLE_OTEQUINOX, OT_DEBUG_VMARG };
	static final String[] VM_ARGS          = { CLASSLOADER_LOCKING, DISABLE_OTEQUINOX };
	static final String[] VM_DEBUG_ARGS    = { CLASSLOADER_LOCKING, DISABLE_OTEQUINOX, OT_DEBUG_VMARG };

	/** 
	 * Extend pre-built vm arguments with OT/Equinox specifics (depending on run/debug mode).
	 */
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
	static String[] addDisableOTEquinoxArgument(String[] args) {
		String[] combinedArgs = new String[args.length + 1];
		System.arraycopy(args, 0, combinedArgs, 0, args.length);
		combinedArgs[args.length] = DISABLE_OTEQUINOX;
		return combinedArgs;		
	}
	/* alternate version for single string signature. */
	static String extendVMArguments(String args, String mode) {
		String otArgs;
		if (mode != null && mode.equals(ILaunchManager.DEBUG_MODE))
			otArgs = Util.concatWith(OT_VM_DEBUG_ARGS, ' ');
		else
			otArgs = Util.concatWith(OT_VM_ARGS, ' ');
	
		if (args == null || args.length() == 0)
			return otArgs;
		
		return args+' '+otArgs;
	}

	/** 
	 * Installs breakpoints needed for the TeamMonitor into org.objectteams.Team.
	 * Needs to find a project with OTJavaNature to do this.
	 */
	static void installOOTBreakpoints(IProject[] projects) throws CoreException
	{
		if (projects != null)
			for (IProject project : projects) 
				// find org.objectteams.Team in any OT/J Project:
				if (project.getNature(JavaCore.OTJ_NATURE_ID) != null) {
					TeamBreakpointInstaller.installTeamBreakpoints(JavaCore.create(project));
					return; // good, done.
				}
		logException(null, Status.WARNING, DebugMessages.OTLaunching_no_OTJ_project_found);
	}
	
	static void logException(CoreException ex, int level, String msg) {
		OTDebugPlugin.getDefault().getLog().log(
						new Status(level, OTDebugAdaptorPlugin.PLUGIN_ID, 0, msg, ex));
	}
	
	// helper needed to protect a guard from exception:
	static boolean isOTLaunch (ILaunchConfiguration configuration) {
		try {
			return configuration.getAttribute(OTDebugPlugin.OT_LAUNCH, false);
		} catch (CoreException ce) {
			return false;
		}
	}
	
	/** Generalizes over normal pde launches and JUnit plugin launches. */
	abstract protected class AbstractLauncher 
	{
		abstract IProject[] getProjectsForProblemSearch(ILaunchConfiguration config, String mode)
			throws CoreException;
		
		String mode;
		
		void prepareLaunch(ILaunchConfiguration configuration, String mode) 
		{
			this.mode = mode;
			if (isOTLaunch(configuration) && ILaunchManager.DEBUG_MODE.equals(mode))
				try {
					PDELaunchingAdaptor.installOOTBreakpoints(getProjectsForProblemSearch(configuration, mode));
				} catch (CoreException ex) {
					logException(ex, Status.WARNING, DebugMessages.OTLaunching_no_OTJ_project_found);
				}
		}
	}
	
	/**
	 * This role adapts any pde-launch where the OT/Equinox flag is set to true:
	 * <ul>
	 * <li>add OT-specific arguments to the vm args (extendVMArguments)</li>
	 * <li>install the breakpoints needed by the TeamMonitor (prepareLaunch)</li>
	 * </ul>
	 */
	protected class Launcher extends AbstractLauncher playedBy AbstractPDELaunchConfiguration 
	{	
		@SuppressWarnings("decapsulation")
		getProjectsForProblemSearch -> getProjectsForProblemSearch;
		
		// Extend VM arguments:
		String[] extendVMArguments(ILaunchConfiguration config) <- replace String[] getVMArguments(ILaunchConfiguration config);
		callin String[] extendVMArguments(ILaunchConfiguration config) throws CoreException 
		{
			String[] args = base.extendVMArguments(config);
			if (isOTLaunch(config))
				return PDELaunchingAdaptor.extendVMArguments(args, this.mode);
			else
				return PDELaunchingAdaptor.addDisableOTEquinoxArgument(args);
		}
			
		// install breakpoints and record launch mode (run/debug):
		prepareLaunch <- before launch;
	}
	
	/** Unfortunately JUnit launches are slightly different (and not related by inheritance) */
	protected class JUnitLauncher extends AbstractLauncher playedBy JUnitLaunchConfigurationDelegate 
	{
		@SuppressWarnings("decapsulation")
		getProjectsForProblemSearch -> getProjectsForProblemSearch;
		
		// Extend VM arguments:
		String extendVMArgument(ILaunchConfiguration config) <- replace String getVMArguments(ILaunchConfiguration config);
		callin String extendVMArgument(ILaunchConfiguration config) throws CoreException 
		{
			String result = base.extendVMArgument(config);
			if (isOTLaunch(config))
				return PDELaunchingAdaptor.extendVMArguments(result, this.mode);
			else
				return result+' '+DISABLE_OTEQUINOX;
		}
			
		// install breakpoints and record launch mode (run/debug):
		prepareLaunch <- before launch;		
	}

	/** 
	 * UI: This role allows us to insert our "Object Teams Runtime" block just after the JREBlock.
	 */
	protected class JREBlock playedBy JREBlock {
		// build the GUI:
		void appendOTOption(Composite parent) <- after void createControl(Composite parent)
			base when (PDELaunchingAdaptor.this.currentTab != null); // only within the LauncherTab#launcherTabCFlow() (see below)

		void appendOTOption(Composite parent) {
			PDELaunchingAdaptor.this.currentTab.createOTRESection(parent, false/*useSWTFactory*/);
		}		
	}
	
	/**
	 * This role manages the UI-part of this team: 
	 * <ul>
	 * <li>insert a new group after the JREBlock.</li>
	 * <li>read (initializeFrom) and apply (performApply) the new flag.</li></ul>
	 */
	protected class LauncherTab extends OTREBlock playedBy AbstractLauncherTab {

		LauncherTab(AbstractLauncherTab b) {
			// different label than default:
			this.enableCheckboxLabel = DebugMessages.OTLaunching_OTEquinox_checkbox_label;
		}
		
		// callout interface:
		@SuppressWarnings("decapsulation")
		Button createCheckButton(Composite parent, String label) -> Button createCheckButton(Composite parent, String label);
		
		@SuppressWarnings("decapsulation")
		void setDirty(boolean dirty) -> void setDirty(boolean dirty);

		void updateLaunchConfigurationDialog() -> void updateLaunchConfigurationDialog();
		
		// CFlow to let the JREBlock trigger building the GUI:
		launcherTabCFlow <- replace createControl;
		callin void launcherTabCFlow(Composite parent) {
			try {
				PDELaunchingAdaptor.this.currentTab = this;
				base.launcherTabCFlow(parent);
			} finally {
				PDELaunchingAdaptor.this.currentTab = null;
			}
		}
		
		// connect triggers to inherited methods:
		void initializeFrom(ILaunchConfiguration config) <- after void initializeFrom(ILaunchConfiguration config)
			when (this._otreToggleButton != null); // i.e.: is this the tab containing the JREBlock?

		@Override
		boolean hasOTJProject(ILaunchConfiguration config) {
			return true; // assume we might have an OT project - even without scanning through all projects; always want to enable our options
		}

		void performApply(ILaunchConfigurationWorkingCopy config) 
				<- after void performApply(ILaunchConfigurationWorkingCopy config);		
	}
}
