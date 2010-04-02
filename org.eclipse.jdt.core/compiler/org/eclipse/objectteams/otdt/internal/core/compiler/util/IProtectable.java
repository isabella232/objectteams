/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2009 Technical University Berlin, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: IProtectable.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Abstraction of role members that can have different levels of protection: methods and fields.
 *
 * @author stephan
 * @since 1.2.6
 */
public interface IProtectable {
	ReferenceBinding getDeclaringClass();
	boolean isPublic();
	boolean isProtected();
	boolean isDefault();
	boolean isPrivate();
	boolean isStatic();
	int modifiers();
}
