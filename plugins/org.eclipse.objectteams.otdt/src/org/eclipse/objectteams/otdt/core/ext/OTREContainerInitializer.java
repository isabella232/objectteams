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
 * $Id: OTREContainerInitializer.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.ext;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * This class creates the OTREContainer. Installed from plugin.xml as an extension.
 * 
 * @author gis
 */
public class OTREContainerInitializer extends ClasspathContainerInitializer
{

    public OTREContainerInitializer()
    {
        super();
    }

    public void initialize(IPath containerPath, IJavaProject project)
            throws CoreException
    {
        if (containerPath == null || containerPath.isEmpty() || !containerPath.segment(0).equals(OTREContainer.OTRE_CONTAINER_NAME))
            return;
        
        JavaCore.setClasspathContainer(
                containerPath, 
                new IJavaProject[] { project }, 
                new IClasspathContainer[] { new OTREContainer() }, 
                null);
    }
}
