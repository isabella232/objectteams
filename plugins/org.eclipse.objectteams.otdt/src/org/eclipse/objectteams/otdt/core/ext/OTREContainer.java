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
 * $Id: OTREContainer.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.ext;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * This class serves the "OTRE" classpath container.
 * It also provides access to resolved paths for all OTRE-related jars.
 * @author gis
 */
public class OTREContainer implements IClasspathContainer
{
	/** Name of this classpath container, which hosts the full OTRE. */
	public static final String OTRE_CONTAINER_NAME     = "OTRE"; //$NON-NLS-1$
    
    // these are served from the current plugin:
    private static IPath  OTRE_MIN_JAR_PATH;
    private static IPath  OTRE_AGENT_JAR_PATH;
    
    public static IPath  BCEL_PATH; // will be initialized in {@link findBCEL(BundleContext)}

    // details of this container: name and hosting plugin:
    private static final IPath  OTRE_CONTAINER_PATH = new Path(OTRE_CONTAINER_NAME);
    private static final String OT_RUNTIME_PLUGIN = "org.eclipse.objectteams.runtime"; //$NON-NLS-1$

    // file names for the above OTRE_X_JAR_PATH constants:
    private static final String OTRE_MIN_JAR_FILENAME   = "otre_min.jar"; //$NON-NLS-1$
    private static final String OTRE_AGENT_JAR_FILENAME = "otre_agent.jar"; //$NON-NLS-1$

    // data for initializing the above BCEL_PATH:
    private static final String BCEL_BUNDLE_NAME = "org.apache.bcel"; //$NON-NLS-1$
    private static final String BCEL_VERSION_RANGE = "[5.2.0,5.3.0)"; //$NON-NLS-1$

    private IClasspathEntry[] _cpEntries;

    public IClasspathEntry[] getClasspathEntries()
    {
        if (_cpEntries == null)
        {
            IPath fullPath = new Path(OTVariableInitializer.getInstallatedPath(OTDTPlugin.getDefault(), OT_RUNTIME_PLUGIN, "bin")); //$NON-NLS-1$
            _cpEntries = new IClasspathEntry[] {JavaCore.newLibraryEntry(fullPath, fullPath, new Path("/"))}; //$NON-NLS-1$
        }
        
        return _cpEntries;
    }

    public String getDescription()
    {
        return OTCoreExtMessages.OTREContainer__Description;
    }

    public int getKind()
    {
    	// don't mark as K_SYSTEM or K_SYSTEM_DEFAULT, which would prevent jdt.debug from adding this to the runtime classpath. 
    	return IClasspathContainer.K_APPLICATION;
    }

    public IPath getPath()
    {
        return OTRE_CONTAINER_PATH;
    }

    public static IPath getOtreMinJarPath () {
    	if (OTRE_MIN_JAR_PATH == null)
            OTRE_MIN_JAR_PATH   = OTDTPlugin.getResolvedVariablePath(OTDTPlugin.OTDT_INSTALLDIR, "lib/"+OTRE_MIN_JAR_FILENAME); //$NON-NLS-1$
    	return OTRE_MIN_JAR_PATH;
    }
    
    public static IPath getOtreAgentJarPath() {
    	if (OTRE_AGENT_JAR_PATH == null)
            OTRE_AGENT_JAR_PATH = OTDTPlugin.getResolvedVariablePath(OTDTPlugin.OTDT_INSTALLDIR, "lib/"+OTRE_AGENT_JAR_FILENAME); //$NON-NLS-1$
    	return OTRE_AGENT_JAR_PATH;
    }
    
	/**
	 * Adds the ObjectTeams classes to the given JavaProject's classpath,
	 * and ensures the Java compliance is >= 1.5
	 */
	public static void initializeOTJProject(IProject project) throws CoreException
	{		
		if (project == null)
		{
			return; 
		}
	
		if (project.hasNature(JavaCore.NATURE_ID))
		{
			IJavaProject 	  javaPrj   = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
			IClasspathEntry[] classpath = javaPrj.getRawClasspath();
			
			if (!isOTREAlreadyInClasspath(classpath))
			{
	            addOTREToClasspath(javaPrj, classpath);
			}
			String javaVersion = javaPrj.getOption(JavaCore.COMPILER_COMPLIANCE, true);
			if (javaVersion.compareTo(JavaCore.VERSION_1_5)<0) {
				javaPrj.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
				javaPrj.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
				javaPrj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
				javaPrj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
				javaPrj.setOption(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
				javaPrj.setOption(JavaCore.COMPILER_CODEGEN_INLINE_JSR_BYTECODE, JavaCore.ENABLED);
			}
		}
	}

	private static boolean isOTREAlreadyInClasspath(IClasspathEntry[] classpath)
	{
	    for (int idx = 0; classpath != null && idx < classpath.length; idx++)
	    {
	        IClasspathEntry entry = classpath[idx];
	        if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && entry.getPath().equals(OTRE_CONTAINER_PATH))
	    	{
	    		return true;
	    	}
	    }
	    
	    return false;
	}

	/**
	 * Add the object teams foundation classes to the classpath.
	 */
	private static void addOTREToClasspath(IJavaProject javaPrj, IClasspathEntry[] classpath)
			throws JavaModelException, CoreException
	{
		IClasspathEntry[] newClasspath = new IClasspathEntry[classpath.length + 1];
	
		System.arraycopy( classpath, 0, newClasspath, 0, classpath.length );
	
		newClasspath[classpath.length] = JavaCore.newContainerEntry(OTRE_CONTAINER_PATH, false);
	    
		if (newClasspath[classpath.length] != null)
		{
			javaPrj.setRawClasspath( newClasspath, null );
		}
		else
		{
			Status reason = new Status(
							IStatus.ERROR, 
							OTDTPlugin.PLUGIN_ID, 
							IStatus.OK,
							OTCoreExtMessages.OTREContainer_otre_not_found, null);
	    			
			throw new CoreException( reason );
		}
	}
	
	/** Fetch the location of the bcel bundle into BCEL_PATH. */
	static void findBCEL(BundleContext context) throws IOException {
		ServiceReference ref= context.getServiceReference(PackageAdmin.class.getName());
		if (ref == null)
			throw new IllegalStateException("Cannot connect to PackageAdmin"); //$NON-NLS-1$
		PackageAdmin packageAdmin = (PackageAdmin)context.getService(ref);
		for (Bundle bundle : packageAdmin.getBundles(BCEL_BUNDLE_NAME, BCEL_VERSION_RANGE)) {			
			BCEL_PATH = new Path(FileLocator.toFileURL(bundle.getEntry("/")).getFile()); //$NON-NLS-1$
			return;
		}
		throw new RuntimeException("bundle org.apache.bcel not found"); //$NON-NLS-1$
	}

	public String getResolvedPathString() {
		return getClasspathEntries()[0].getPath().toOSString();
	}
}
