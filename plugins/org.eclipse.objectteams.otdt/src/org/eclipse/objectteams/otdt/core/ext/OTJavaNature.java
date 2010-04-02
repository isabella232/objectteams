/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTJavaNature.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.ext;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ExternalJavaProject;


/**
 * @author jwloka
 * @version $Id: OTJavaNature.java 23427 2010-02-03 22:23:59Z stephan $
 */
@SuppressWarnings("restriction")
public class OTJavaNature implements IProjectNature
{
	private IProject _prj;

    public void configure() throws CoreException
    {
        IProjectDescription projectDescription = _prj.getDescription();
        ICommand command = projectDescription.newCommand();
        command.setBuilderName(JavaCore.OTJ_BUILDER_ID);

        ICommand[] buildCommands    = projectDescription.getBuildSpec();
        ICommand[] newBuildCommands;

		if (contains(buildCommands, JavaCore.OTJ_BUILDER_ID))
		{
			return; // safety, do nothing
		}

        if (contains(buildCommands, JavaCore.BUILDER_ID))
        {
            newBuildCommands =
                replace(buildCommands, JavaCore.BUILDER_ID, command);
        }
        else
        {
            newBuildCommands = insert(buildCommands, command);
        }				

        projectDescription.setBuildSpec(newBuildCommands);
        _prj.setDescription(projectDescription, null);
    }

    public void deconfigure() throws CoreException
    {
        IProjectDescription description   = _prj.getDescription();
        ICommand[]          buildCommands = description.getBuildSpec();
        ICommand            command       = description.newCommand();
        command.setBuilderName(JavaCore.BUILDER_ID);

        ICommand[] newBuildCommands;

        if (contains(buildCommands, JavaCore.OTJ_BUILDER_ID))
        {
            newBuildCommands =
                replace(buildCommands, JavaCore.OTJ_BUILDER_ID, command);
        }
        else
        {
            newBuildCommands =
                remove(buildCommands, JavaCore.OTJ_BUILDER_ID);
        }

        description.setBuildSpec(newBuildCommands);
        _prj.setDescription(description, null);
    }

    public IProject getProject()
    {
        return _prj;
    }

    public void setProject(IProject value)
    {
        _prj = value;
    }
    
    public static boolean hasOTJavaNature(IProject project) {
    	try {
			return project.hasNature(JavaCore.OTJ_NATURE_ID);
		} catch (CoreException e) {
			if (ExternalJavaProject.EXTERNAL_PROJECT_NAME.equals(project.getName())) // see JavaProject
				return true;
		}
		return false;
    }

	private boolean contains(ICommand[] commands, String builderId)
    {
        for (int i = 0; i < commands.length; i++)
        {
            if (commands[i].getBuilderName().equals(builderId))
				return true;
        }

		return false;
    }

    private ICommand[] replace(
        ICommand[] sourceCommands, String oldBuilderId, ICommand newCommand)
    {
        ICommand[] newCommands = new ICommand[sourceCommands.length];

        for (int i = 0; i < sourceCommands.length; i++)
        {
            if (sourceCommands[i].getBuilderName().equals(oldBuilderId))
            {
                newCommands[i] = newCommand;
            }
            else
            {
                newCommands[i] = sourceCommands[i];
            }
        }

        return newCommands;
    }

    private ICommand[] insert(ICommand[] sourceCommands, ICommand command)
    {
        ICommand[] newCommands = new ICommand[sourceCommands.length + 1];
        newCommands[0] = command;

        for (int i = 0; i < sourceCommands.length; i++)
        {
            newCommands[i + 1] = sourceCommands[i];
        }

        return newCommands;
    }

    private ICommand[] remove(ICommand[] sourceCommands, String builderId)
    {
        ICommand[] newCommands     = new ICommand[sourceCommands.length - 1];
        int        newCommandIndex = 0;

        for (int i = 0; i < sourceCommands.length; i++)
        {
            if (!sourceCommands[i].getBuilderName().equals(builderId))
            {
                newCommands[newCommandIndex++] = sourceCommands[i];
            }
        }

        return newCommands;
    }
}
