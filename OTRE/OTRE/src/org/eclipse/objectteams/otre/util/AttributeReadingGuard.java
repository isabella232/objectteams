/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AttributeReadingGuard.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christine Hundt
 * @author Stephan Herrmann
 */
public class AttributeReadingGuard {
	
	private static Map<ClassLoader, AttributeReadingGuard> instances = new HashMap<ClassLoader, AttributeReadingGuard>();
    private static AttributeReadingGuard defaultInstance = new AttributeReadingGuard();
    
    private ArrayList<String> servedClasses = new ArrayList<String>();
    
    // this one flag is really global (concerns the one main class of the application):
    private static boolean firstLoaded = true;

    /**
     * @param className
     * @return
     */
    public boolean iAmTheFirst(String className) {
        return !this.servedClasses.contains(className);    
    }
    
    /**
     * Processing the given class is done.
     * @param className
     */
    public void workDone(String className) {
    	this.servedClasses.add(className);    
    }
    
    /**
     * @return whether this class is the first being loaded => possibly the main class.
     */
    public static synchronized boolean isFirstLoadedClass() {
    	if (!firstLoaded)
    		return false;
    	firstLoaded = false;
    	return true;
    }

    /** First loaded class has no main => it was a false alarm. */
	public static void reset() {
		firstLoaded = true;
	}

	/**
	 * Since actual data are stored in an instance, static methods need to retrieve the appropriate
     * instance regarding the given class loader.
	 */
	public static AttributeReadingGuard getInstanceForLoader(ClassLoader loader) {
		if (loader == null)
			return defaultInstance;
		
		AttributeReadingGuard instance = instances.get(loader);
		if (instance == null)
			instances.put(loader, instance = new AttributeReadingGuard());
		return instance;
	}
}
