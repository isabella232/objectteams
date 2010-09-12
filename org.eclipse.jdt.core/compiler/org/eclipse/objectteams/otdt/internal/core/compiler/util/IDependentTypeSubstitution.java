/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2010 Stephan Herrmann
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: IDependentTypeSubstitution.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;

/**
 * Function type for type substitions to be performed on a dependent type.
 */
public interface IDependentTypeSubstitution {
	/**
	 * @param original 		the atomic type to substitute
	 * @param typeArguments type arguments to re-apply during/after substitution
	 * @param dimensions    array dimensions to re-apply during/after substitution
	 * @return null signals an error has been reported, 
	 *    returning original signals no substitutions were needed,
	 *    otherwise a substituted type is return. 
	 */
	TypeBinding substitute(DependentTypeBinding original, TypeBinding[] typeArguments, int dimensions);
}
