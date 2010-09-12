/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ActivationKind.java 23461 2010-02-04 22:10:39Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox;

/**
 * Possible values for the "activation" attribute of a "team" element within an "aspectBinding" extension.
 *  
 * @author stephan
 * @since 1.2.7 (was inline previously)
 */
public enum ActivationKind {
	/** Don't activate team by default. */
	NONE, 
	/** Activate team for current thread. */
	THREAD, 
	/** Globally activate team. */
	ALL_THREADS;
}