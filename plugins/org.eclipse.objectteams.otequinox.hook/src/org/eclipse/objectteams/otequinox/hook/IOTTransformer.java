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
package org.eclipse.objectteams.otequinox.hook;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.util.Collection;
import java.util.List;

/**
 * Service interface for access to the OTRE.
 * @author stephan
 * @since 0.7.0
 */
public interface IOTTransformer {
	
	/** Create a new instance of the OTRE transformer (of class org.eclipse.objectteams.otre.jplis.ObjectTeamsTransformer). */
	ClassFileTransformer getNewTransformer();

	/**
	 * Read all OT-relevant bytecode attributes for a class.
	 * @param openStream an opened input stream providing the class bytes
	 * @param file       file name of the class file
	 * @param loader	 class loader to use for subsequent class lookup
	 * @return returns as a token the transformer which can be passed back to {@link #fetchAdaptedBases(Object)} for retrieving information found during scanning.
	 */
	Object readOTAttributes(InputStream openStream, String file, ClassLoader loader) throws ClassFormatError, IOException;

	/**
	 * Get all adapted base classes that were recorded during a prior call to {@link #readOTAttributes(InputStream, String, ClassLoader)}. 
	 * @param token  the result return from the call to {@link #readOTAttributes(InputStream, String, ClassLoader)}
	 * @return a collection of fully qualified names of bound base classes.
	 */
	Collection<String> fetchAdaptedBases(Object token);

	/**
	 * Retrieve the list of all roles from a given team 
	 * @param teamClassName name of the team class
	 * @return list of fully qualified role names (in attribute syntax using '.' and '$' delimiters)
	 */
	List<String> getRolesPerTeam(String teamClassName);
}
