/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2009 Stephan Herrmann.
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
 * Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.compiler;

/**
 * Kinds to differentiate inferred callouts.
 * 
 * @author stephan
 * @since 1.3.2
 */
public enum InferenceKind { 
	/** Not an inferred callout */
	NONE,
	/** Callout inferred from a declared super interface. */
	INTERFACE,
	/** Callout inferred from a self call. */
	SELFCALL, 
	/** Callout inferred from a reading field access (via 'this'). */
	FIELDGET, 
	/** Callout inferred from a writing field access (via 'this'). */
	FIELDSET 
}