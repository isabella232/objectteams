/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTLaunchConfigurationTab.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.launching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.debug.IOTLaunchConstants;
import org.eclipse.objectteams.otdt.ui.ImageConstants;
import org.eclipse.objectteams.otdt.ui.ImageManager;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;

/**
 * This class provides the "Team Activation" tab to the launch dialog.
 * 
 * @author gis
 * @version $Id: OTLaunchConfigurationTab.java 23456 2010-02-04 20:44:45Z stephan $
 */
public class OTLaunchConfigurationTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab
{
	private List<IType> _teamModel = new ArrayList<IType>();
    
    private TeamConfig _teamConfig;
    private IProject _project = null;
	
    public void createControl(Composite parent)
    {
       	_teamConfig = new TeamConfig(parent, SWT.NONE, this);
    	setControl(_teamConfig);
    	setMessage(OTDTUIPlugin.getResourceString("TeamConfig.cannot_instantiate_message")); //$NON-NLS-1$
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
    {
        // nothing to do -- empty defaults
    }

    public void initializeFrom(ILaunchConfiguration configuration)
    {
        _teamModel.clear();
        
    	List teamHandles = new LinkedList(); 
    	boolean teamsActive = true;
    	try {
			teamHandles =  configuration.getAttribute(IOTLaunchConstants.ATTR_TEAMCONFIG_LIST, teamHandles);	
			teamsActive = configuration.getAttribute(IOTLaunchConstants.ATTR_TEAMCONFIG_ACTIVE, true);
		} catch (CoreException ce) {
		    OTDTUIPlugin.getExceptionHandler().logCoreException("Cannot read team configuration", ce); //$NON-NLS-1$
		}

		_teamConfig.clearTeamList();
		List<String> badTeams = new LinkedList<String>();
		for (Iterator iter = teamHandles.iterator(); iter.hasNext();) {
			String teamHandle = (String) iter.next();
			IType type = (IType) JavaCore.create(teamHandle);
			if (type != null)
			{
			    if (type.exists())
			    {
			        IOTType otType = OTModelManager.getOTElement(type);
			        if (otType != null)
			        {
			            _teamModel.add(otType);
			            continue;
			        }
			    }
            	badTeams.add(type.getFullyQualifiedName());
			}
			else
			    badTeams.add(teamHandle);
		}

		reportBadTeams(badTeams);
		
		_teamConfig.setActive(teamsActive);
		_teamConfig.setTeamInput(_teamModel);
		_teamConfig.checkEnablement();
		
		String projectName = getProjectFromConfig(configuration);
		if (projectName.length() != 0)
		    _project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		else
		    _project = null;
    }

    private void reportBadTeams(List<String> badTeams)
    {
        // TODO (carp): ideally, we need some sort of listener that notices renaming and deleting of classes
        // so that the launch configuration can be updated.
        if (!badTeams.isEmpty())
		{
		    StringBuffer message = new StringBuffer(OTDTUIPlugin.getResourceString("TeamConfig.not_found_message")); //$NON-NLS-1$
		    for (Iterator<String> iter = badTeams.iterator(); iter.hasNext();)
            {
                String badTeam = iter.next();
                message.append(badTeam + '\n');
            }
		    setModified(); // otherwise, the bad configuration will be kept!
		    setErrorMessage(message.toString()); // this should actually be a warning, not an error
		}
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration)
    {
    	removeUnavailableTeamsErrorMessage(); // not really 
    	
        List<String> teamHandles = getTeamModelAsHandles();

    	configuration.setAttribute(IOTLaunchConstants.ATTR_TEAMCONFIG_LIST, teamHandles);
    	configuration.setAttribute(IOTLaunchConstants.ATTR_TEAMCONFIG_ACTIVE, _teamConfig.isActive());
    }

    private void removeUnavailableTeamsErrorMessage()
    {
        // well, actually this removes any error message of this tab, but this is our
        // only one, so far (and actually should be a warning instead of an error)
        setErrorMessage(null);
    	getLaunchConfigurationDialog().updateMessage();
    }

    public String getName()
    {
    	return OTDTUIPlugin.getResourceString("TeamConfig.tab_title"); //$NON-NLS-1$
    }
    
    public Image getImage() 
    {
    	return ImageManager.getSharedInstance().get(ImageConstants.TEAM_IMG);
    }

	/**
	 * 
	 */
	public void setModified() 
	{
		setDirty(true);
		updateLaunchConfigurationDialog();
	}
	
	private String getProjectFromConfig(ILaunchConfiguration config) 
	{
		String projectName= ""; //$NON-NLS-1$
		try {
			projectName= config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");	 //$NON-NLS-1$
		} catch (CoreException ce) {
			OTDTUIPlugin.getExceptionHandler().logCoreException("Unable to retrieve _project from launch configuration", ce); //$NON-NLS-1$
		}
		return projectName;
	}

	public IProject getProject() {
		return _project;
	}
	
	public List<IType> getTeamModel()
    {
        return _teamModel;
    }

    /**
     * Converts the List<IOTType> _teamModel to a new List<String>, consisting of all
     * ITypes' HandleIdentifiers.
     * @return the new list
     */
    private List<String> getTeamModelAsHandles()
    {
        List<String> teamHandles = new LinkedList<String>();
        for (Iterator<IType> iter = _teamModel.iterator(); iter.hasNext();)
        {
            IType type = iter.next();
            teamHandles.add(type.getHandleIdentifier());
        }
        return teamHandles;
    }
    
    public IRunnableContext getRunnableContext()
    {
        return getLaunchConfigurationDialog();
    }
}
