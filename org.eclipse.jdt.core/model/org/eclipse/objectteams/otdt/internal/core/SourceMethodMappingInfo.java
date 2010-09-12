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
package org.eclipse.objectteams.otdt.internal.core;

import org.eclipse.jdt.internal.core.AnnotatableInfo;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Capture all that information regarding a method mapping that may change while
 * still the mapping is considered the same (as detected by CallxyMapping.equals(..)).
 */
public class SourceMethodMappingInfo extends AnnotatableInfo {

	// greater than ICallinMapping.KIND_xyz
	private static final int CALLOUT= 4;
	private static final int GET 	= 5;
	private static final int SET 	= 6;
	
	private String[] roleParameterNames;
	// other bits would change the effective signature

	private int nBaseMethods;
	private String[][] baseParameterNames;
	private String[][] baseParameterTypes;
	private String[] baseReturnTypes;
	
	private int          _mappingKind=0;
	private boolean      _hasSignature;
	private boolean      _isOverride; // callout override.
	private int			 _declaredModifiers;
	
	private char[] callinName = null;
	
	public void setRoleArgumentNames(String[] parameterNames) {
		this.roleParameterNames = parameterNames;		
	}

	public void setBaseArgumentNames(String[][] parameterNames) {
		checkBaseCount(parameterNames.length);
		this.baseParameterNames = parameterNames;		
	}

	public void setBaseArgumentTypes(String[][] parameterTypes) {
		checkBaseCount(parameterTypes.length);
		this.baseParameterTypes = parameterTypes;
	}
	
	public void setBaseReturnType(String[] returnTypes) {
		checkBaseCount(returnTypes.length);
		this.baseReturnTypes = returnTypes;
	}

	private void checkBaseCount(int baseCount) {
		if (this.nBaseMethods == 0)
			this.nBaseMethods = baseCount;
		else if (this.nBaseMethods != baseCount)
			throw new IllegalArgumentException("Mismatching array size: "+this.nBaseMethods+" vs. "+baseCount); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void setCallinKind(int kind, boolean hasSignature, char[] name) {
		this._mappingKind = kind;
		this._hasSignature = hasSignature;
		this.callinName = name;
	}
	
	public void setCalloutKind(boolean override, int declaredModifiers) {
		this._isOverride = override;
		this._declaredModifiers = declaredModifiers;
		this._mappingKind = CALLOUT;
		
	}

	public void setCalloutKind(boolean override, int declaredModifiers, boolean isSetter) {
		this._isOverride = override;
		this._declaredModifiers = declaredModifiers;
		this._mappingKind = isSetter ? SET : GET;
		
	}
	
	public boolean modifiersEqual(SourceMethodMappingInfo other) {
		if (this._hasSignature != other._hasSignature)
			return false;
		if (this._mappingKind != other._mappingKind)
			return false;
		if (!isCallin())
			return this._isOverride == other._isOverride && this._declaredModifiers == other._declaredModifiers;
		return true;
	}
	
	public boolean signaturesEqual(SourceMethodMappingInfo other) {
		if (!Util.equalArraysOrNull(this.roleParameterNames, other.roleParameterNames))
			return false;
		if (this.nBaseMethods != other.nBaseMethods)
			return false;
		for(int i=0; i<this.nBaseMethods; i++) {
			if (!Util.equalArraysOrNull(this.baseParameterNames[i], other.baseParameterNames[i]))
				return false;
			if (!Util.equalArraysOrNull(this.baseParameterTypes[i], other.baseParameterTypes[i]))
				return false;
			if (!this.baseReturnTypes[i].equals(other.baseReturnTypes[i]))
				return false;
		}
		return true;
	}

	public char[] getCallinName() {
		return this.callinName;
	}

	public boolean isCallin() {
		return this._mappingKind < CALLOUT;
	}
}
