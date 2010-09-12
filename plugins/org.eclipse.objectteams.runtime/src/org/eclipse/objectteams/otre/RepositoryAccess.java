/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann
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
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import java.util.HashMap;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.util.ClassLoaderRepository;

/** 
 * Provides a classloader aware access to one or more class repositories.
 * All access methods are static, but context is passed by setting a per-thread
 * current repository using {@link #setClassLoader(ClassLoader)}.
 * @since 0.7.0 
 */
public class RepositoryAccess {

	/** One class repository per class loader. */
	private static HashMap<ClassLoader,ClassLoaderRepository> repositories = new HashMap<ClassLoader,ClassLoaderRepository>();
	/** One current repository per thread. */
	private static ThreadLocal<ClassLoaderRepository> currentRepository = new ThreadLocal<ClassLoaderRepository>();
	 
	/** 
	 * Setup a repository for the given class loader and make it the current repository for the current thread.
	 * @param loader class loader, may be null
	 * @return the previously active class repository for this thread
	 */
	public static synchronized ClassLoaderRepository setClassLoader(ClassLoader loader) {
		ClassLoaderRepository clr = null;
		if (loader != null) { // avoid creating ClassLoaderRepository with null loader
			clr = repositories.get(loader);
			if (clr == null)
				repositories.put(loader, clr = new ClassLoaderRepository(loader));
		}
		ClassLoaderRepository prev = currentRepository.get();
		currentRepository.set(clr);
		return prev;
	}
	 
	/** Reset the class repository for the current thread. */
	public static synchronized void resetRepository(ClassLoaderRepository repository) {
		currentRepository.set(repository);
	}
	 
	public static JavaClass lookupClass(String className)
	 		throws ClassNotFoundException 
	{
		ClassLoaderRepository clr = currentRepository.get();
		if (clr != null)
			return clr.loadClass(className);
		return Repository.lookupClass(className);
	}

	public static JavaClass[] getSuperClasses(String className) 
			throws ClassNotFoundException 
	{
		 ClassLoaderRepository clr = currentRepository.get();
		 JavaClass jc;
		 if (clr != null)
			 jc = clr.loadClass(className);
		 else
			 jc = Repository.lookupClass(className);
		 return jc.getSuperClasses();
	}

	public static boolean implementationOf(String className, String ifcName) 
			throws ClassNotFoundException 
	{
		ClassLoaderRepository clr = currentRepository.get();
		JavaClass jc, ifc;
		if (clr != null) {
			jc = clr.loadClass(className);
			ifc= clr.loadClass(ifcName);
		} else {
			jc = Repository.lookupClass(className);
			ifc= Repository.lookupClass(ifcName);
		}
		return jc.implementationOf(ifc);
	}

	public static boolean safeSubclassOf(ObjectType subClass, ObjectType superClass) {
		try {
			String subClassName = subClass.getClassName();
			String superClassName = superClass.getClassName();
			return instanceOf(subClassName, superClassName);
		} catch (ClassNotFoundException e) {
			// consider classes as incommensurable if they can't both be loaded in the current class loader
			return false;
		} catch (ClassCircularityError e) {
			// assume that circularity was caused by resolving framework classes during class loading
			return false;
		}
	}

	protected static boolean instanceOf(String subClassName, String superClassName) 
			throws ClassNotFoundException 
	{
		ClassLoaderRepository clr = currentRepository.get();
		JavaClass subJClass, superJClass;
		if (clr != null) {
			subJClass = clr.loadClass(subClassName);
			superJClass= clr.loadClass(superClassName);
		} else {
			subJClass = Repository.lookupClass(subClassName);
			superJClass= Repository.lookupClass(superClassName);
		}
		return subJClass.instanceOf(superJClass);
	}
}
