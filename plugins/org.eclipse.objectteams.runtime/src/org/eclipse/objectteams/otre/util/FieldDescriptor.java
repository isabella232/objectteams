/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2004-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: FieldDescriptor.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.util;

/**
 * @author resix
 */
public class FieldDescriptor {
	private String fieldName;
	private String fieldSignature;
	private boolean isStaticField;

	public FieldDescriptor(String name, String signature, boolean is_static) {
		fieldName = name;
		fieldSignature = signature;
		isStaticField = is_static;
	}
	
	/**
	 * @return Returns the fieldName.
	 */
	public String getFieldName() {
		return fieldName;
	}
	
	/**
	 * @return Returns the fieldSignature.
	 */
	public String getFieldSignature() {
		return fieldSignature;
	}
	
	/**
	 * @return Returns the isStaticField.
	 */
	public boolean isStaticField() {
		return isStaticField;
	}
}
