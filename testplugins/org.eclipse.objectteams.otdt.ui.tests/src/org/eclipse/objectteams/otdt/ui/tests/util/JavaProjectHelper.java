/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JavaProjectHelper.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.util;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.objectteams.otdt.ui.tests.TestPlugin;

/**
 * Helper methods to set up a IJavaProject.
 */
public class JavaProjectHelper extends org.eclipse.jdt.testplugin.JavaProjectHelper
{

    public static final IPath RT_STUBS = new Path("testresources/rtstubs.jar");

    /**
     * OT: new method: OT specific project creation.
     * Creates a IJavaProject.
     */
    public static IJavaProject createOTJavaProject(String projectName,
            String binFolderName) throws CoreException
    {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(projectName);
        if (!project.exists())
        {
            project.create(null);
        }
        else
        {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        }

        if (!project.isOpen())
        {
            project.open(null);
        }

        IPath outputLocation;
        if (binFolderName != null && binFolderName.length() > 0)
        {
            IFolder binFolder = project.getFolder(binFolderName);
            if (!binFolder.exists())
            {
                CoreUtility.createFolder(binFolder, false, true, null);
            }
            outputLocation = binFolder.getFullPath();
        }
        else
        {
            outputLocation = project.getFullPath();
        }

        if (!project.hasNature(JavaCore.NATURE_ID) &&
                !project.hasNature(JavaCore.OTJ_NATURE_ID))
        {
            addNatureToProject(project, JavaCore.NATURE_ID, null);
            //add OT nature to project
            addNatureToProject(project, JavaCore.OTJ_NATURE_ID, null);
        }
        IProjectDescription description = project.getDescription();
        //add OT build spec to project
        description.setBuildSpec(OTDTPlugin.createProjectBuildCommands(description));
		project.setDescription(description, null);

        IJavaProject jproject = JavaCore.create(project);
        
        jproject.setOutputLocation(outputLocation, null);
        jproject.setRawClasspath(new IClasspathEntry[0], null);
		
        OTREContainer.initializeOTJProject(project);

        return jproject;
    }

    /**
     * OT:  use our TestPlugin:
     * Try to find rt.jar
     */
    public static IPath[] findRtJar()
    {
        File rtStubs = TestPlugin.getDefault().getFileInPlugin(
                RT_STUBS);
        if (rtStubs != null && rtStubs.exists())
        {
            return new IPath[] { new Path(rtStubs.getPath()), null, null };
        }

        return null;
    }

    /**
     * Additional Method:
     * Sets autobuilding state for the test workspace.
     */
    public static boolean setAutoBuilding(boolean state) throws CoreException
    {
        // disable auto build
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc = workspace.getDescription();
        boolean result = desc.isAutoBuilding();
        desc.setAutoBuilding(state);
        workspace.setDescription(desc);
        return result;
    }

}

