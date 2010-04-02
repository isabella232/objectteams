/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JDTLaunchingAdaptor.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.launching;


import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.objectteams.otdt.core.ext.OTRuntimeClasspathProvider;
import org.eclipse.objectteams.otdt.debug.IOTLaunchConstants;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;
import org.eclipse.objectteams.otdt.debug.OTVMRunnerAdaptor;
import org.eclipse.objectteams.otdt.debug.TeamBreakpointInstaller;
import org.eclipse.pde.internal.ui.IPDEUIConstants;

import base org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import base org.eclipse.jdt.launching.StandardClasspathProvider;

/**
 * This team observes java launches and potentially modifies them for OT/J capabilities.
 * <ul>
 * <li>Add BCEL and JMangler to the classpath (role {@link ClasspathExtender})
 * <li>Maintain an OTVMRunnerAdaptor for adapting vm- and program args (role {@link JDTLaunchingAdaptor.AbstractJavaLaunchConfigurationDelegate}).
 * </ul>
 * Role {@link JDTLaunchingAdaptor.JavaLaunchDelegate} only binds the above behavior into Java launches.
 * See {@link JUnitLaunchingAdaptor.JUnitLaunchConfigurationDelegate} for equal binding to JUnit launches.
 *  
 * @author stephan
 * @since 1.2.1
 */
@SuppressWarnings("restriction")
public team class JDTLaunchingAdaptor {

	/**
	 * This role extends the classpath with BCEL and JMangler paths if OT/J is enabled for the launch.
	 */
	protected class ClasspathExtender playedBy StandardClasspathProvider {

		IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration config) 
				<- replace IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration config);

		callin IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration config) throws CoreException 
		{
	    	IRuntimeClasspathEntry[] origEntries = base.computeUnresolvedClasspath(config);
	    	
	    	boolean useJMangler = isNormalOTJLaunch(config);
	    	if (!useJMangler && !isJPLISOTJLaunch(config))
	    		return origEntries;
	    	
	    	// add BCEL, JMangler-core and JMangler-start (classpath / bootclasspath)
	    	int oldLength = origEntries.length;
			IRuntimeClasspathEntry[] otRuntimeEntries = computePathsToAdd(origEntries, useJMangler);
			
			// merge results:
	    	IRuntimeClasspathEntry[] result = new IRuntimeClasspathEntry[oldLength + otRuntimeEntries.length];
			System.arraycopy(origEntries, 0, result, 0, oldLength);
			System.arraycopy(otRuntimeEntries, 0, result, oldLength, otRuntimeEntries.length);
	        return result;
		}
		
	    static IRuntimeClasspathEntry[] computePathsToAdd( IRuntimeClasspathEntry[] origEntries, boolean useJMangler )
		{
			boolean hasBCEL = false;
			boolean hasJManglerCore = false;
			boolean hasJManglerStart = false;
			boolean hasOTRE_min = false;
	
			for (int i = 0; i < origEntries.length; i++)
	        {
	            IRuntimeClasspathEntry entry = origEntries[i];
				if (OTRuntimeClasspathProvider.BCEL_JAR.equals(entry.getPath()))
					hasBCEL = true;
				else if (OTREContainer.OTRE_JAR_PATH.equals(entry.getPath().toString()))
					hasOTRE_min = true;
	        }
	
			List<IRuntimeClasspathEntry> result = new LinkedList<IRuntimeClasspathEntry>();
			IRuntimeClasspathEntry entry;
	
			if (!hasBCEL) {
				entry = JavaRuntime.newArchiveRuntimeClasspathEntry(OTRuntimeClasspathProvider.BCEL_JAR);
				entry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
				result.add(entry);			
			}
	
			if (!hasJManglerCore && useJMangler) {
				throw new RuntimeException("JMangler is no longer supported");
			}  
			
			if (!hasJManglerStart && useJMangler) { 	
				throw new RuntimeException("JMangler is no longer supported");			
			}		    	
			
			if (!hasOTRE_min && !useJMangler) {
				entry = JavaRuntime.newArchiveRuntimeClasspathEntry(JavaCore.getResolvedVariablePath(new Path(OTREContainer.OTRE_MIN_JAR_PATH)));
				entry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
				result.add(entry);
			}
			
			return result.toArray(new IRuntimeClasspathEntry[result.size()]);
		}

	}
	
	/**
	 * This role performs the adaptations of vmargs and program args (including main class name).
	 */
	protected class AbstractJavaLaunchConfigurationDelegate playedBy AbstractJavaLaunchConfigurationDelegate 
	{
		IJavaProject getJavaProject(ILaunchConfiguration arg0) -> IJavaProject getJavaProject(ILaunchConfiguration arg0);

		OTVMRunnerAdaptor fAdaptor;
		boolean useJMangler = false;
		String fOriginalMain;

		// --- Initiate adaptations: (this callin actually applies to sub-base-classes)
		void prepareLaunch(ILaunchConfiguration config, String mode, ILaunch launch)
				<- before void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor);
		void prepareLaunch(ILaunchConfiguration config, String mode, ILaunch launch) throws CoreException 
		{
			this.fOriginalMain = null; // reset potential left over from previous launching
			this.useJMangler = isNormalOTJLaunch(config);
			if (!this.useJMangler && !isJPLISOTJLaunch(config)) {
				this.fAdaptor = null;
				return;
			}
			this.fAdaptor = new OTVMRunnerAdaptor();
			this.fAdaptor.setAdaptationArgs(config, mode, launch);
			// install OT-breakpoints
			if (ILaunchManager.DEBUG_MODE.equals(mode))
				TeamBreakpointInstaller.installTeamBreakpoints(getJavaProject(config));
		}

		// --- Main Type Name: ---
		String verifyMainTypeName(ILaunchConfiguration config) 
				<- replace String verifyMainTypeName(ILaunchConfiguration config)
			when (this.useJMangler);

		callin String verifyMainTypeName(ILaunchConfiguration config) throws CoreException {
			throw new RuntimeException("JMangler is no longer supported");
		}

		// --- VM Arguments: ---
		String getVMArguments(ILaunchConfiguration configuration) 
				<- replace String getVMArguments(ILaunchConfiguration configuration)
			when (this.fAdaptor != null);


		callin String getVMArguments(ILaunchConfiguration config) throws CoreException {
			String vmArgs = base.getVMArguments(config);
			return this.fAdaptor.adaptVMArgumentString(vmArgs);
		}

		// --- Program Arguments: ---
		String getProgramArguments(ILaunchConfiguration config) 
				<- replace String getProgramArguments(ILaunchConfiguration config)
			when (this.fOriginalMain != null);

		callin String getProgramArguments(ILaunchConfiguration config) throws CoreException {
			String programArguments = base.getProgramArguments(config);
			return this.fOriginalMain + ' ' + programArguments;
		}		
	}
	
	static boolean isNormalOTJLaunch(ILaunchConfiguration config) {
		try {
			return    config.getAttribute(OTDebugPlugin.OT_LAUNCH, false)                               // OT/J ?
				  && !config.getAttribute(IOTLaunchConstants.ATTR_USE_JPLIS, false)					    // not JPLIS ?
				  && (config.getAttribute(IPDEUIConstants.LAUNCHER_PDE_VERSION, (String)null) == null); // not PDE ?
		} catch (CoreException e) {
			return false; // don't apply adaptations to bogus config
		}
	}
		
	static boolean isJPLISOTJLaunch(ILaunchConfiguration config) {
		try {
			return    config.getAttribute(OTDebugPlugin.OT_LAUNCH, false)                               // OT/J ?
				  &&  config.getAttribute(IOTLaunchConstants.ATTR_USE_JPLIS, false)					    // JPLIS ?
				  && (config.getAttribute(IPDEUIConstants.LAUNCHER_PDE_VERSION, (String)null) == null); // not PDE ?
		} catch (CoreException e) {
			return false; // don't apply adaptations to bogus config
		}
	}
}
