/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BinaryRoleType.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;

/**
 * @author gis
 */
public class BinaryRoleType extends RoleType 
{
	public BinaryRoleType(
			IType correspondingJavaType, 
			IJavaElement parent,
			int flags, 
			String baseClassName,
			String baseClassAnchor)
	{
		super(correspondingJavaType, parent, flags, baseClassName, baseClassAnchor);
	}

	@Override
	IType resolveInType(IOTType referenceType, String type)
			throws JavaModelException 
	{
		// binary type may have resolved type name
        if (type.indexOf('.') != -1)  // already qualified/resolved (from binary)? 
        	// directly find the type:
        	return this.getCorrespondingJavaElement().getJavaProject().findType(type);

		return super.resolveInType(referenceType, type);
	}
}
