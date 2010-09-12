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
 * $Id: DebugTestsPlugin.java 23492 2010-02-05 22:57:56Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.tests;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class DebugTestsPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static DebugTestsPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public DebugTestsPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static DebugTestsPlugin getDefault() {
		return plugin;
	}

    protected String getPluginID()
    {
        return "org.eclipse.objectteams.otdt.debug.tests";
    }
    
    /**
     * Returns the OS path to the directory that contains this plugin and adds a subpath if wanted.
     * 
     */
    protected File getPluginDirectoryPath(IPath test_src_dir)
    {
        try
        {
            URL platformURL = Platform
                .getBundle(getPluginID())
                .getEntry("/");
            if (test_src_dir != null)
                platformURL = new URL(platformURL, test_src_dir.toString());
            
            return new File(Platform.asLocalURL(platformURL).getFile());
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}
