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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class OTRefactoringTestPlugin extends Plugin
{
    //The shared instance.
    private static OTRefactoringTestPlugin plugin;

    public OTRefactoringTestPlugin()
    {
        super();
        plugin = this;
    }

    /**
     * Returns the shared instance.
     */
    public static OTRefactoringTestPlugin getDefault()
    {
        return plugin;
    }

    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    public File getFileInPlugin(IPath path)
    {
        try
        {
            URL installURL = new URL(getDefault().getBundle().getEntry("/"), path.toString());
            URL localURL = Platform.asLocalURL(installURL);
            return new File(localURL.getFile());
        }
        catch (IOException ex)
        {
            return null;
        }
    }

    public InputStream getTestResourceStream(String fileName)
            throws IOException
    {
        IPath path = new Path("testdata").append(fileName);
        URL url = new URL(getDefault().getBundle().getEntry("/"), path.toString());
        return url.openStream();
    }
}