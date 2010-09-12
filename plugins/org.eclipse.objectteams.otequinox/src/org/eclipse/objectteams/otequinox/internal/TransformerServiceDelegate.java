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
package org.eclipse.objectteams.otequinox.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.util.Collection;
import java.util.List;

import org.eclipse.objectteams.otre.jplis.ObjectTeamsTransformer;
import org.eclipse.objectteams.otre.util.CallinBindingManager;

import org.eclipse.objectteams.otequinox.hook.IOTTransformer;

/**
 * Service implementation for providing org.eclipse.objectteams.otequinox.hook with an
 * access to the OTRE.
 * 
 * @author stephan
 * @since 0.7.0
 */
public class TransformerServiceDelegate implements IOTTransformer {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.objectteams.otequinox.hook.IOTTransformer#getNewTransformer()
	 */
	public ClassFileTransformer getNewTransformer() {
		return new ObjectTeamsTransformer();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.objectteams.otequinox.hook.IOTTransformer#readOTAttributes(java.io.InputStream, java.lang.String, java.lang.ClassLoader)
	 */
	public Object readOTAttributes(InputStream openStream, String file, ClassLoader loader) 
			throws ClassFormatError, IOException 
	{
		ObjectTeamsTransformer transformer = new ObjectTeamsTransformer();
		transformer.readOTAttributes(openStream, file, loader);
		return transformer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.objectteams.otequinox.hook.IOTTransformer#fetchAdaptedBases(java.lang.Object)
	 */
	public Collection<String> fetchAdaptedBases(Object token) {
		try {
			return ((ObjectTeamsTransformer)token).fetchAdaptedBases();
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("Provided token is not an ObjectTeamsTransformer");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.objectteams.otequinox.hook.IOTTransformer#getRolesPerTeam(java.lang.String)
	 */
	public List<String> getRolesPerTeam(String teamClassName) {
		return CallinBindingManager.getRolePerTeam(teamClassName);
	}

}
