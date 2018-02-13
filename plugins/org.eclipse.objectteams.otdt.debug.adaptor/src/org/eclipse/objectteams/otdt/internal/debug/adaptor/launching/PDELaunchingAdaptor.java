/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008, 2015 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.launching;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.objectteams.otdt.core.ext.WeavingScheme;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;
import org.eclipse.objectteams.otdt.debug.OTBreakpointInstaller;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.DebugMessages;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.OTDebugAdaptorPlugin;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.dynamic.RedefineClassesBPListener;
import org.eclipse.objectteams.otequinox.TransformerPlugin;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ISharedPluginModel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import base org.eclipse.pde.internal.launching.launcher.BundleLauncherHelper;
import base org.eclipse.pde.internal.ui.launcher.JREBlock;
import base org.eclipse.pde.launching.AbstractPDELaunchConfiguration;
import base org.eclipse.pde.launching.JUnitLaunchConfigurationDelegate;
import base org.eclipse.pde.ui.launcher.AbstractLauncherTab;
import base org.eclipse.pde.ui.launcher.MainTab;
import base org.eclipse.pde.ui.launcher.OSGiSettingsTab;

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
	
	static final String ENABLE_OTEQUINOX      = "-Dot.equinox=1"; //$NON-NLS-1$     // this also causes the WORKAROUND_REPOSITORY flag being set to true in OTRE.
	static final String DISABLE_OTEQUINOX     = "-Dot.equinox=false"; //$NON-NLS-1$ // prevents OTWeavingHook installation
	static final String OT_DEBUG_VMARG        = "-Dot.debug"; //$NON-NLS-1$
	static final String OTE_AGENT_ARG		  = "-javaagent:" + TransformerPlugin.getOtequinoxAgentPath();
	static final String OT_WEAVING			  = "-Dot.weaving="; // need to append either "otre" or "otdre"

	static final String[] OT_VM_ARGS          = { ENABLE_OTEQUINOX, OT_WEAVING };
	static final String[] OTDRE_VM_ARGS          = { ENABLE_OTEQUINOX, OTE_AGENT_ARG, OT_WEAVING };
	static final String[] OT_VM_DEBUG_ARGS    = { ENABLE_OTEQUINOX, OT_DEBUG_VMARG, OTE_AGENT_ARG, OT_WEAVING };
	static final String[] VM_ARGS          = { DISABLE_OTEQUINOX };
	static final String[] VM_DEBUG_ARGS    = { DISABLE_OTEQUINOX, OT_DEBUG_VMARG };

	/** select proper set of arguments for an OT-launch, insert otequinox.hook using it's actual install location. */
	static String[] getOTArgs(String mode, String weavingMode) {
		String[] otArgs;
		if (mode != null && mode.equals(ILaunchManager.DEBUG_MODE)) {
			otArgs = OT_VM_DEBUG_ARGS;
		} else {
			if (WeavingScheme.OTDRE.name().equals(weavingMode))
				otArgs = OTDRE_VM_ARGS;
			else
				otArgs = OT_VM_ARGS;
		}
		int length = otArgs.length;
		System.arraycopy(otArgs, 0, otArgs = new String[otArgs.length], 0, length);
		otArgs[length-1] += weavingMode;
		return otArgs;
	}
	/** 
	 * Extend pre-built vm arguments with OT/Equinox specifics (depending on run/debug mode).
	 */
	static String[] extendVMArguments(String[] args, String mode, String weavingMode) {
		String[] otArgs = getOTArgs(mode, weavingMode);
		if (otArgs == null)
			return args;
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
	static String extendVMArguments(String args, ISharedPluginModel hookModel, String mode, String weavingMode) {
		String[] otArgss = getOTArgs(mode, weavingMode);
		if (otArgss == null)
			return args;
						
		String otArgs = Util.concatWith(otArgss, ' ');
	
		if (args == null || args.length() == 0)
			return otArgs;
		
		return args+' '+otArgs;
	}

	/** 
	 * Installs breakpoints needed for the TeamMonitor into org.objectteams.Team.
	 * Needs to find a project with OTJavaNature to do this.
	 */
	static void installOOTBreakpoints(IProject[] projects, String weavingMode) throws CoreException
	{
		if (projects != null)
			for (IProject project : projects) 
				// find org.objectteams.Team in any OT/J Project:
				if (project.getNature(JavaCore.OTJ_NATURE_ID) != null) {
					IJavaProject javaProject = JavaCore.create(project);
					OTBreakpointInstaller.installOTBreakpoints(javaProject,
							RedefineClassesBPListener.get(WeavingScheme.valueOf(weavingMode)));
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
		String weavingMode;
		
		void prepareLaunch(ILaunchConfiguration configuration, String mode) throws DebugException
		{
			this.mode = mode;
			if (isOTLaunch(configuration))
				try {
					IProject[] projects = getProjectsForProblemSearch(configuration, mode);
					determineWeavingMode(projects);
					if (ILaunchManager.DEBUG_MODE.equals(mode))
						PDELaunchingAdaptor.installOOTBreakpoints(projects, weavingMode);
				} catch (DebugException dex) {
					throw dex;
				} catch (CoreException ex) {
					logException(ex, Status.WARNING, DebugMessages.OTLaunching_no_OTJ_project_found);
				}
		}

		private void determineWeavingMode(IProject[] projects) throws CoreException {
			this.weavingMode = null;
			if (projects != null) {
				IProject otjProject = null;
				for (IProject project : projects) {
					// check weaving mode in all relevant OT/J Project:
					if (project.getNature(JavaCore.OTJ_NATURE_ID) != null) {
						String wMode = JavaCore.create(project).getOption(JavaCore.COMPILER_OPT_WEAVING_SCHEME, false);
						if (wMode != null) {
							if (this.weavingMode != null && !wMode.equals(this.weavingMode))
								throw new DebugException(new Status(IStatus.ERROR, OTDebugPlugin.PLUGIN_ID,
										NLS.bind(DebugMessages.OTLaunching_conflicting_weaving_modes,
												new String[] {
													otjProject.getName(), this.weavingMode,
													project.getName(), wMode
												})));
							this.weavingMode = wMode;
						}
						otjProject = project;
					}
				}
				if (this.weavingMode == null && otjProject != null)
					this.weavingMode = JavaCore.getDefaultOptions().get(JavaCore.COMPILER_OPT_WEAVING_SCHEME);
			}
			if (this.weavingMode == null)
				logException(null, Status.WARNING, DebugMessages.OTLaunching_no_OTJ_project_found);
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
				return PDELaunchingAdaptor.extendVMArguments(args, this.mode, this.weavingMode);
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

		@SuppressWarnings({ "decapsulation", "rawtypes" })
		protected ISharedPluginModel getBundle(String id) => get Map fAllBundles
				with { result <- (ISharedPluginModel)result.get(id) }
		
		// Extend VM arguments:
		String extendVMArgument(ILaunchConfiguration config) <- replace String getVMArguments(ILaunchConfiguration config);
		callin String extendVMArgument(ILaunchConfiguration config) throws CoreException 
		{
			String result = base.extendVMArgument(config);
			if (isOTLaunch(config))
				return PDELaunchingAdaptor.extendVMArguments(result, null /*getBundle(OTEQUINOX_HOOK)*/, this.mode, this.weavingMode);
			else
				return result+' '+DISABLE_OTEQUINOX;
		}
			
		// install breakpoints and record launch mode (run/debug):
		prepareLaunch <- before launch;		
	}
	
	protected class SetOTEquinoxStartlevel playedBy BundleLauncherHelper {

		void getMergedBundleMap(Map<IPluginModelBase, String> map, boolean enable)
		<- after
		Map<IPluginModelBase, String> getMergedBundleMap(ILaunchConfiguration configuration, boolean osgi) 
			with { map <- result, enable <- isOTLaunch(configuration) }

		static void getMergedBundleMap(Map<IPluginModelBase, String> map, boolean enable) {
			for (IPluginModelBase plugin: map.keySet())
				if (plugin.getPluginBase().getId().equals("org.eclipse.objectteams.otequinox")) {
					if (enable)
						map.put(plugin, "1:true");
					else
						map.put(plugin,  "default:default");
					return;
				}
		}		
	}

	/** 
	 * UI: This role allows us to insert our "Object Teams Runtime" block just after the JREBlock.
	 */
	protected class JREBlock playedBy JREBlock {
		// build the GUI:
		void appendOTOption(Composite parent) <- after void createControl(Composite parent)
			base when (PDELaunchingAdaptor.this.currentTab != null); // only within the LauncherTab#launcherTabCFlow() (see below)

		void appendOTOption(Composite parent) {
			PDELaunchingAdaptor.this.currentTab.createOTRESection(parent);
		}		
	}
	
	/**
	 * This role manages the UI-part of this team: 
	 * <ul>
	 * <li>insert a new group after the JREBlock.</li>
	 * <li>read (initializeFrom) and apply (performApply) the new flag.</li></ul>
	 */
	@SuppressWarnings("abstractrelevantrole")
	protected abstract class LauncherTab extends OTREBlock playedBy AbstractLauncherTab {

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
		callin void launcherTabCFlow(Composite parent) {
			try {
				PDELaunchingAdaptor.this.currentTab = this;
				base.launcherTabCFlow(parent);
			} finally {
				PDELaunchingAdaptor.this.currentTab = null;
			}
		}

		@Override
		boolean hasOTJProject(ILaunchConfiguration config) {
			return true; // assume we might have an OT project - even without scanning through all projects; always want to enable our options
		}
	}
	protected class MainTab extends LauncherTab playedBy MainTab {
		launcherTabCFlow <- replace createControl;
		// connect triggers to inherited methods:
		void initializeFrom(ILaunchConfiguration config) <- after void initializeFrom(ILaunchConfiguration config)
			when (this._otreToggleButton != null); // i.e.: is this the tab containing the JREBlock?

		void performApply(ILaunchConfigurationWorkingCopy config) 
				<- after void performApply(ILaunchConfigurationWorkingCopy config);		
	}
	protected class OSGiSettingsTab extends LauncherTab playedBy OSGiSettingsTab {
		launcherTabCFlow <- replace createControl;
		// connect triggers to inherited methods:
		void initializeFrom(ILaunchConfiguration config) <- after void initializeFrom(ILaunchConfiguration config)
			when (this._otreToggleButton != null); // i.e.: is this the tab containing the JREBlock?

		void performApply(ILaunchConfigurationWorkingCopy config) 
				<- after void performApply(ILaunchConfigurationWorkingCopy config);		
	}
}
