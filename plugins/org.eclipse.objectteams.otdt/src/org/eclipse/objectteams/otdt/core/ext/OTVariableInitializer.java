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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;


/**
 * ClassPathVariableInitializer to initialize the OTRE_INSTALL variable.
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
    	if (OTDTPlugin.OTDT_INSTALLDIR.equals(variable))
    	{
			setPluginInstallationPathVariable(OTDTPlugin.getDefault(), variable);
		}
    }

	public static void setPluginInstallationPathVariable(Plugin relativePlugin, String variable)
	{
		try 
		{
			URL installDirectory = relativePlugin.getBundle().getEntry("/"); //$NON-NLS-1$

			// On Windows, the next line leads to something like "/C:/Programme/Eclipse/plugins/my.plugin
			// If we simply make an org.eclipse.core.runtime.Path out of it, the leading '/' makes the
			// parsing fail (device, e.g. 'C:' is not detected). We must use java.io.File to parse it
			// properly.
			String path = FileLocator.toFileURL(installDirectory).getPath();
			String fixedPath = new File(path).getPath();
			JavaCore.setClasspathVariable(variable, new Path(fixedPath), new NullProgressMonitor());
		}
		catch (Exception ex)
		{
			OTDTPlugin.getExceptionHandler().logException(ex);
		}
	}
}
