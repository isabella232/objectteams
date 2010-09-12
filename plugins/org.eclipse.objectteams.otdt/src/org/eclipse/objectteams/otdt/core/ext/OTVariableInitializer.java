/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTVariableInitializer.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.ext;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;


/**
 * ClassPathVariableInitializer to initialize the OTRE_INSTALLDIR variable.
 * 
 * @author gis
 * @version $Id: OTVariableInitializer.java 23427 2010-02-03 22:23:59Z stephan $
 */
public class OTVariableInitializer extends ClasspathVariableInitializer
{
    public OTVariableInitializer()
    {
        super();
    }

    public void initialize(String variable)
    {
    	String installPath = null;
    	if (OTDTPlugin.OTDT_INSTALLDIR.equals(variable))
    	{
			installPath = getOTDTPluginInstallationPath();
		} else {
			OTDTPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, OTDTPlugin.PLUGIN_ID, "Mismatching name for classpath variable during initialization")); //$NON-NLS-1$
		}

    	try {
			JavaCore.setClasspathVariable(variable, new Path(installPath), new NullProgressMonitor());
		} catch (JavaModelException e) {
			OTDTPlugin.getExceptionHandler().logException(e);
		}
    }

	public static String getOTDTPluginInstallationPath()
	{
		try 
		{
			URL installDirectory = OTDTPlugin.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
			
			// On Windows, the next line leads to something like "/C:/Programme/Eclipse/plugins/my.plugin
			// If we simply make an org.eclipse.core.runtime.Path out of it, the leading '/' makes the
			// parsing fail (device, e.g. 'C:' is not detected). We must use java.io.File to parse it
			// properly.
			String path = FileLocator.toFileURL(installDirectory).getPath();
			return new File(path).getPath();
		}
		catch (Exception ex)
		{
			OTDTPlugin.getExceptionHandler().logException(ex);
			return null;
		}
	}

	public static String getInstallatedPath(Plugin hostPlugin, String bundleName, String optionalRelativeDir)
	{
		try 
		{
			URL installDirectory = getBundle(bundleName).getEntry("/"); //$NON-NLS-1$

			// On Windows, the next line leads to something like "/C:/Programme/Eclipse/plugins/my.plugin
			// If we simply make an org.eclipse.core.runtime.Path out of it, the leading '/' makes the
			// parsing fail (device, e.g. 'C:' is not detected). We must use java.io.File to parse it
			// properly.
			String path = FileLocator.toFileURL(installDirectory).getPath();
			File f = new File(path + optionalRelativeDir);
			if (!f.exists())
				f = new File(path); // proceed with only "path"
			return f.getPath();
		}
		catch (Exception ex)
		{
			OTDTPlugin.getExceptionHandler().logException(ex);
			return null;
		}
	}
	
	private static Bundle getBundle(String symbolicName) {
		for (Bundle bundle : OTDTPlugin.getDefault().getBundle().getBundleContext().getBundles())
			if (bundle.getSymbolicName().equals(symbolicName)) {
				if (bundle.getState() == Bundle.UNINSTALLED)
					OTDTPlugin.getDefault().getLog().log(new Status(IStatus.INFO, OTDTPlugin.PLUGIN_ID, "Skipping uninstalled bundle "+bundle.getSymbolicName()+"."+bundle.getVersion()));
				else
					return bundle;
			}
		return null;
	}
}
