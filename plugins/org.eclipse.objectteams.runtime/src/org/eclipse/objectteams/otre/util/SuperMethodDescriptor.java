/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SuperMethodDescriptor.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.util;

/** Representation of a base-super method call, requiring a special access method. */
public class SuperMethodDescriptor {
	public String methodName;
	public String declaringClass;
	public String superClass;
	public String signature;
	public SuperMethodDescriptor(String methodName, String declaringClass,
			String superClass, String signature) {
		super();
		this.methodName = methodName;
		this.declaringClass = declaringClass;
		this.superClass = superClass;
		this.signature = signature;
	}
}
