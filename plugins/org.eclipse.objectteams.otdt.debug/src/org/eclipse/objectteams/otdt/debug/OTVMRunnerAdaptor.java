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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.objectteams.otdt.debug.internal.TempFileManager;


/**
 * @author gis
 */
@SuppressWarnings("nls")
public class OTVMRunnerAdaptor
{
	private static final String OT_DEBUG_VMARG = "-Dot.debug";
	private static final String OT_DEBUG_CALLIN_STEPPING_VMARG = "-Dot.debug.callin.stepping";
	private static final String OT_TEAMCONFIG_VMARG = "-Dot.teamconfig";
	private static List<String> JPLIS_VMARGS;

	private ILaunchConfiguration _launchConfig;
	private String _mode;
	private ILaunch _launch;

	public void setAdaptationArgs(ILaunchConfiguration configuration, String mode, ILaunch launch) throws CoreException
	{
		_launchConfig = configuration;
		_mode = mode;
		_launch = launch;
	}
	
	public VMRunnerConfiguration adapt(VMRunnerConfiguration vmRunnerConfig) throws CoreException
	{
		adaptVMArgs(vmRunnerConfig);
		
		return vmRunnerConfig;
	}

	protected void cloneRunner(VMRunnerConfiguration vmRunnerConfig, VMRunnerConfiguration newConfig)
	{
		// Only main type and class path have to be set in the constructor already
    	newConfig.setBootClassPath(vmRunnerConfig.getBootClassPath());
    	newConfig.setEnvironment(vmRunnerConfig.getEnvironment());
    	newConfig.setProgramArguments(vmRunnerConfig.getProgramArguments());
    	newConfig.setResumeOnStartup(vmRunnerConfig.isResumeOnStartup());
    	newConfig.setVMArguments(vmRunnerConfig.getVMArguments());
    	newConfig.setVMSpecificAttributesMap(vmRunnerConfig.getVMSpecificAttributesMap());
    	newConfig.setWorkingDirectory(vmRunnerConfig.getWorkingDirectory());
	}

	protected void adaptVMArgs(VMRunnerConfiguration newConfig) throws CoreException
    {
	    List<String> otVMArgs = getOTVMArgs();
	    String teamConfigArg = generateTeamConfigArgument(_launch, _launchConfig);
	    if (teamConfigArg != null)
	    	otVMArgs.add(teamConfigArg);
	    if (ILaunchManager.DEBUG_MODE.equals(_mode)) 
	    {
	    	otVMArgs.add(OT_DEBUG_VMARG);
	    	
	    	String callinSteppingVMArg = getCallinSteppingVMArg();
	    	if (callinSteppingVMArg != null)
	    		otVMArgs.add(callinSteppingVMArg);
	    }

	    String[] vmArgs = newConfig.getVMArguments();
	    if (vmArgs.length > 0)
	    {
	    	for (int i = 0; i < vmArgs.length; i++)
				otVMArgs.add(vmArgs[i]);
	    }
	    
	    String[] newArgs = new String[otVMArgs.size()];
	    
	    // new launches need to enclose file paths in quotes (cmdline is one string parsed by ArgumentParser),
	    // which are removed here (cmdline is array of strings):
	    for (int i = 0; i<newArgs.length; i++) {
	    	newArgs[i] = otVMArgs.get(i).replaceAll("\"", "");
	    }
	    newConfig.setVMArguments(newArgs);
    }
	
	/**
	 * API version: given a list of vm arguments add ot-specific vm arguments.
	 * 
	 * @param vmArguments list to be augmented.
	 * @throws CoreException
	 */
	public void adaptVMArguments(List vmArguments) throws CoreException 
	{
	    vmArguments.addAll(getOTVMArgs());
	    String teamConfigArg = generateTeamConfigArgument(_launch, _launchConfig);
	    if (teamConfigArg != null)
	    	vmArguments.add(teamConfigArg);
	    if (ILaunchManager.DEBUG_MODE.equals(_mode))
	    	vmArguments.add(OT_DEBUG_VMARG);
	}

	public String adaptVMArgumentString(String vmArguments) throws CoreException 
	{
		String sep = " ";
	    StringBuffer result = new StringBuffer(vmArguments);
	    
		for (String arg : getJplisVmargs())
			result.append(sep).append(arg);
	    
	    String callinSteppingVMArg = getCallinSteppingVMArg();
	    if (callinSteppingVMArg != null)
	    	result.append(sep).append(callinSteppingVMArg);
	    
	    String teamConfigArg = generateTeamConfigArgument(_launch, _launchConfig);
	    if (teamConfigArg != null)
	    	result.append(sep).append(teamConfigArg);
	    if (ILaunchManager.DEBUG_MODE.equals(_mode))
	    	result.append(sep).append(OT_DEBUG_VMARG);
	    
	    return result.toString();
	}

	private List<String> getJplisVmargs() {
		if (JPLIS_VMARGS == null) {
			JPLIS_VMARGS = new ArrayList<String>();
			JPLIS_VMARGS.add("-Dot.otdt");
			JPLIS_VMARGS.add("-javaagent:" + "\""+OTREContainer.getOtreAgentJarPath().toOSString()+'"'); // support blanks in path
		}
		return JPLIS_VMARGS;
	}

	private String getCallinSteppingVMArg() {
		String value = OTDebugPlugin.getDefault().getCallinSteppingConfig();
		if (value == null) return null;
		return OT_DEBUG_CALLIN_STEPPING_VMARG+'='+value;
	}

	protected List<String> getOTVMArgs()
	{
        return new ArrayList<String>(getJplisVmargs());
	}
	
    /**
	 * Teams to activate are read from ILaunchConfiguration configuration.
	 * The list of team names are written to temporary file.
	 * The commandline par -Dot.teamconfig=filename is returned and can
	 * be added to the vmArgs variable. 
     * @param launch
	 * @param configuration	The launch configuration object.
	 * @return 
	 */
	private String generateTeamConfigArgument(ILaunch launch, ILaunchConfiguration configuration) throws CoreException
	{
		List<String> teamList = getTeamConfigList(configuration);
		if (teamList.isEmpty())
			return null;

		// write the teams into a temporary file:
        File teamFile = createTeamConfigFile(launch, teamList);
        
		return OT_TEAMCONFIG_VMARG+"=\"" + teamFile.getAbsolutePath()+'"';
	}

	private File createTeamConfigFile(ILaunch launch, List<String> teamList) throws CoreException
    {
        try {
            TempFileManager manager = OTDebugPlugin.getDefault().getTempFileManager();
            File tempFile = manager.createTempFile(launch, ".otteamconfig", ".conf");

		    PrintStream printStream = new PrintStream(new FileOutputStream(tempFile));
        	for (Iterator<String> iter = teamList.iterator(); iter.hasNext();) {
        		String element = iter.next();
				printStream.println(element);
			}
        	printStream.close();
        	return tempFile;
        } catch (Exception e) {
            IStatus status = OTDebugPlugin.createErrorStatus("Error writing static teams configuration", e);
        	throw new CoreException(status);
        }
    }

    /**
	 * Returns the list of teams which will be woven into the launched application 
	 * specified by the given launch configuration, as a list of strings. 
	 * The returned list is empty if no teams are are specified or if the teams
	 * are deactivated in the configuratio tab.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the list of teams from the team configuration specified by the given launch
	 *         configuration, possibly an empty list
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	private List<String> getTeamConfigList(ILaunchConfiguration configuration)
			throws CoreException 
	{
		boolean teamsActive = configuration.getAttribute(IOTLaunchConstants.ATTR_TEAMCONFIG_ACTIVE, true);
		if (!teamsActive)
			return new LinkedList<String>();
		
		List teamHandles = configuration.getAttribute(IOTLaunchConstants.ATTR_TEAMCONFIG_LIST, new LinkedList());	
		List<String> teamNames = new LinkedList<String>();
		List<String> badHandles = new LinkedList<String>();
		
		for (Iterator iter = teamHandles.iterator(); iter.hasNext();)
        {
            String teamHandle = (String) iter.next();
            IType teamType = (IType) JavaCore.create(teamHandle);
            if (teamType != null && teamType.exists())
                teamNames.add(teamType.getFullyQualifiedName());
            else
                badHandles.add(teamHandle);
        }
		
		if (!badHandles.isEmpty())
		{
		    IStatus status = OTDebugPlugin.createErrorStatus("Cannot determine types: " + badHandles.toString());
		    throw new CoreException(status);
		}
		
    	return teamNames;
    }
}
