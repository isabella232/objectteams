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
 * $Id: ResolvedCalloutToFieldMapping.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.objectteams.otdt.core.util.FieldData;
import org.eclipse.objectteams.otdt.core.util.MethodData;

public class ResolvedCalloutToFieldMapping extends CalloutToFieldMapping {
	private String uniqueKey;

	/*
	 * See class comments.
	 */
	public ResolvedCalloutToFieldMapping(   
			int        declarationSourceStart,
			int        sourceStart,
			int		   sourceEnd,
			int        declarationSourceEnd,
			int		   elementType,
			IType  role,
			IMethod	corrJavaMethod,
            MethodData roleMethodHandle,
            FieldData baseFieldHandle,
            boolean hasSignature,
            boolean isOverride,
	        String uniqueKey) {		
		
		super(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, elementType, role, corrJavaMethod, roleMethodHandle, baseFieldHandle, hasSignature, isOverride, false);
		this.uniqueKey = uniqueKey;
	}
	
	public String getKey() {
		return this.uniqueKey;
	}

	public boolean isResolved() {
		return true;
	}
}
