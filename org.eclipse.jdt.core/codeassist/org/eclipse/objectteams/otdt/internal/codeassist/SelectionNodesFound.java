/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SelectionNodesFound.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.codeassist;

import org.eclipse.jdt.internal.compiler.lookup.Binding;

/**
 * @author haebor
 */
@SuppressWarnings("serial")
public class SelectionNodesFound extends RuntimeException
{

	public Binding[] _bindings;
	public boolean _isDeclaration;

	public SelectionNodesFound()
	{
    	this(null); // we found a problem in the selection node
    }

	public SelectionNodesFound(Binding[] bindings)
	{
    	this(bindings, false);
    }

    public SelectionNodesFound(Binding[] bindings, boolean isDeclaration)
    {
    	this._bindings = bindings;
    	this._isDeclaration = isDeclaration;
    }
}
