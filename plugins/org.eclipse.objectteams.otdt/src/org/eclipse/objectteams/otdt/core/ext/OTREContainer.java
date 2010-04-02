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
 * $Id: OTREContainer.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.ext;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author gis
 */
public class OTREContainer implements IClasspathContainer
{
    public static final String OTRE_CONTAINER_NAME   = "OTRE"; //$NON-NLS-1$
    public static final String OTRE_JAR_FILENAME     = "otre.jar"; //$NON-NLS-1$
    public static final String OTRE_MIN_JAR_FILENAME = "otre_min.jar"; //$NON-NLS-1$
    
    public static final IPath  OTRE_CONTAINER_PATH = new Path(OTRE_CONTAINER_NAME);
    public static final String OTRE_JAR_PATH  	   = OTDTPlugin.OTDT_INSTALLDIR + "/lib/" + OTRE_JAR_FILENAME; //$NON-NLS-1$
    public static final String OTRE_MIN_JAR_PATH   = OTDTPlugin.OTDT_INSTALLDIR + "/lib/" + OTRE_MIN_JAR_FILENAME; //$NON-NLS-1$

    private IClasspathEntry[] _cpEntries;

    public IClasspathEntry[] getClasspathEntries()
    {
        if (_cpEntries == null)
        {
            _cpEntries = new IClasspathEntry[1];
    		IPath fullPath = new Path(OTRE_JAR_PATH);
    		IClasspathEntry varOTREEntry = JavaCore.newVariableEntry(fullPath, fullPath, fullPath, true);
    		_cpEntries[0] = JavaCore.getResolvedClasspathEntry(varOTREEntry);
        }
        
        return _cpEntries;
    }

    public String getDescription()
    {
        return OTCoreExtMessages.OTREContainer__Description;
    }

    public int getKind()
    {
        return IClasspathContainer.K_APPLICATION;
    }

    public IPath getPath()
    {
        return OTRE_CONTAINER_PATH;
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
}
