/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTRuntimeClasspathProvider.java 21932 2009-07-30 16:53:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.ext;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;


/**
 * @author gis
 * @version $Id: OTRuntimeClasspathProvider.java 21932 2009-07-30 16:53:44Z stephan $
 */
public class OTRuntimeClasspathProvider extends StandardClasspathProvider
{
	public static final String PROVIDER_ID = "org.eclipse.objectteams.otdt.OTRuntimeClasspathProvider"; //$NON-NLS-1$

	public static IPath BCEL_JAR = getVariablePath("/lib/BCEL.jar"); //$NON-NLS-1$

    public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException
    {
    	// add BCEL, JMangler-core and JMangler-start (classpath / bootclasspath)
    	IRuntimeClasspathEntry[] origEntries = super.computeUnresolvedClasspath(configuration);

    	int oldLength = origEntries.length;
		IRuntimeClasspathEntry[] otRuntimeEntries = computePathsToAdd(origEntries);	
    	IRuntimeClasspathEntry[] result = new IRuntimeClasspathEntry[oldLength + otRuntimeEntries.length];
		System.arraycopy(origEntries, 0, result, 0, oldLength);

		// add the missing OT runtime paths to the result
		for (int i = 0; i < otRuntimeEntries.length; i++)
        {
            result[oldLength + i] = otRuntimeEntries[i];
        }
    	
//		IJavaProject project = JavaRuntime.getJavaProject(configuration);
//		if (project == null) {
//			// no project - use JRE's libraries by default
    	
        return result;
    }

    private static IRuntimeClasspathEntry[] computePathsToAdd( IRuntimeClasspathEntry[] origEntries )
	{
		boolean hasBCEL = false;
		boolean hasJManglerCore = false;
		boolean hasJManglerStart = false;

		for (int i = 0; i < origEntries.length; i++)
        {
            IRuntimeClasspathEntry entry = origEntries[i];
			if (BCEL_JAR.equals(entry.getPath()))
				hasBCEL = true;
        }

		List<IRuntimeClasspathEntry> result = new LinkedList<IRuntimeClasspathEntry>();
		IRuntimeClasspathEntry entry;

//TODO (carp): make these class paths variable classpaths and compute the absolute path later
// Also fix getVariablePath() then.
		if (!hasBCEL)
		{
			entry = JavaRuntime.newArchiveRuntimeClasspathEntry(BCEL_JAR);
			entry.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
			result.add(entry);			
		}

		if (!hasJManglerCore)
		{
			throw new RuntimeException("JMangler is no longer supported");			
		}  
		
		if (!hasJManglerStart)
		{ 	
			throw new RuntimeException("JMangler is no longer supported");			
		}		    	
		
		return result.toArray(new IRuntimeClasspathEntry[result.size()]);
	}

    private static IPath getVariablePath(String filename)
    {
		Path path = new Path(OTDTPlugin.OTRUNTIME_INSTALLDIR + filename);
		//return path;
		return JavaCore.getResolvedVariablePath(path);
    }

    public IRuntimeClasspathEntry[] resolveClasspath(
        IRuntimeClasspathEntry[] entries,
        ILaunchConfiguration configuration)
        throws CoreException
    {
		IRuntimeClasspathEntry[] result = super.resolveClasspath(entries, configuration);
		return result;
    }

}
