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
package org.eclipse.objectteams.otdt.test.builder;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.objectteams.otre.jplis.ObjectTeamsTransformer;

/**
 * Utility class for launching a program in a controlled environment.
 * Disposing the application's object and the OTLaunchEnvironment
 * should enable the JVM to also dispose all classes loaded in the process.
 * 
 * @author stephan
 * @version $Id: OTLaunchEnvironment.java 14220 2006-09-04 22:13:40Z stephan $
 */
public class OTLaunchEnvironment extends URLClassLoader 
{
	ObjectTeamsTransformer transformer = null;
	/** Where to look for class files. */
	IPath bindir;
	/**
	 * Setup a launch environment for the given paths. 
	 * @param workspaceRoot path to the workspace root
	 * @param bindir        absolute workspace path to the output location holding class files.
	 * @throws MalformedURLException if the OTRE_JAR_PATH is not a valid path.
	 */
	OTLaunchEnvironment(IPath workspaceRoot, IPath bindir) throws MalformedURLException {
		super(new URL[]{getOTREURL()});
		this.bindir = workspaceRoot.append(bindir);
	}
	/**
	 * Same as above but supports the use of OTRE transformers, if `useTransformer == true'  
	 */
	OTLaunchEnvironment(IPath workspaceRoot, IPath bindir, boolean useTransformer) 
			throws MalformedURLException 
	{
		this(workspaceRoot, bindir);
		if (useTransformer)
			this.transformer = new ObjectTeamsTransformer();
	}
	
	static URL getOTREURL() throws MalformedURLException {
		IClasspathEntry[] entries = new OTREContainer().getClasspathEntries();
		return new URL("file:"+entries[0].getPath()); //$NON-NLS-1$
	}
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		String fileName = name.replace('.', File.separatorChar)+".class"; //$NON-NLS-1$
		File file = new File(bindir.append(fileName).toOSString());
		if (file.exists()) {
			try {
				return defineClassFromFile(name, file);
			} catch (Exception e) {
				// we have a file but it was invalid, don't continue
				throw new ClassNotFoundException(name);
			}
		}
		// this will use the URL pointing to the OTRE:
		return super.findClass(name);
	}
	
	/* We found an existing file, load its bytes and define the class. */
	private Class<?> defineClassFromFile(String name, File file) 
		throws Exception 
	{
		FileInputStream fis = new FileInputStream(file);
		byte[] bytes = new byte[(int) file.length()];
		fis.read(bytes);
		if (transformer != null) 
			bytes = transformer.transform(this, name, null, null, bytes);
		return defineClass(name, bytes, 0, bytes.length);
	}
	
	/**
	 * Load class `className' and invoke its method `methodName'.
	 * The method must be static and without arguments.
	 *  
	 * @param className
	 * @param methodName
	 * @return the methods return (possibly boxed).
	 * 
	 * @throws Exception too many to list explicitly ;-)
	 */
	public Object launch(String className, String methodName) 
			throws Exception 
	{
		Class clazz = this.loadClass(className);
		Method method = clazz.getMethod(methodName, new Class[0]);		
		return method.invoke(null, new Object[0]);
	}
}
