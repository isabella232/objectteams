/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTNameUtils.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.compiler;

import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.BASE_PREDICATE_PREFIX;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.OT_DELIM_LEN;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.OT_DELIM_NAME;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.OT_DOLLAR_NAME;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.PREDICATE_METHOD_NAME;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.TSUPER_OT_NAME;



import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * API class providing various operations for special names in generated OT/J code. 
 * @author stephan
 * @since 1.3.2
 */
public class OTNameUtils {

	// for inferred callout to field:
	private static final char[] SET = "set".toCharArray(); //$NON-NLS-1$
	private static final char[] GET = "get".toCharArray(); //$NON-NLS-1$
	
	/**
	 * Is selector the name of a predicate method?
	 */
	public static boolean isPredicate(char[] selector) {
		if (!CharOperation.prefixEquals(OT_DOLLAR_NAME, selector))
			return false;
		return (   CharOperation.prefixEquals(PREDICATE_METHOD_NAME, selector)
			    || CharOperation.prefixEquals(BASE_PREDICATE_PREFIX, selector));
	}

	/** Remove all occurrences of '__OT__' from a given type name. */
	public static char[] removeOTDelim(char[] typeName) {
		if (!CharOperation.contains(OT_DELIM_NAME, typeName))
			return typeName;
		char[] strippedName = new char[0];
		int start = 0;
		int pos = CharOperation.indexOf(OT_DELIM_NAME, typeName, true, start);
		while (pos > -1) {
			strippedName = CharOperation.concat(
								strippedName,
								CharOperation.subarray(typeName, start, pos));
			start = pos;
			pos = CharOperation.indexOf(OT_DELIM_NAME, typeName, true, start+1);
		}
		strippedName = CharOperation.concat(
								strippedName,
								CharOperation.subarray(typeName, start+OT_DELIM_LEN, -1));
		return strippedName;
	}

	/** 
	 * Given a fieldName (e.g. val) construct an accessor method name (getVal, or setVal) for inferred callout.
	 * @param isSetter asking for a "set" accessor?
	 * @param fieldName 
	 */
	public static char[] accessorName(boolean isSetter, char[] fieldName) {
		if (fieldName == null)
			return null;
		char[] capitalized= new char[fieldName.length];
		System.arraycopy(fieldName, 0, capitalized, 0, fieldName.length);
		capitalized[0] = Character.toUpperCase(fieldName[0]);
		char[] prefix = isSetter ? OTNameUtils.SET : OTNameUtils.GET;
		return CharOperation.concat(prefix, capitalized);
	}

	/** Does name denote a synthetic marker interface used for marking tsuper methods? */
	public static boolean isTSuperMarkerInterface(char[] name) {
		int lastDollar = CharOperation.lastIndexOf('$', name);
		if (lastDollar > -1)
			name = CharOperation.subarray(name, lastDollar+1, -1);
	    return CharOperation.prefixEquals(TSUPER_OT_NAME, name);
	}

	/** Given that type is a role, determine confinedness by the simple name. */
	public static boolean isTopConfined(String elementName) {
		char[] name = elementName.toCharArray();
		return     CharOperation.equals(name, IOTConstants.ICONFINED)
				|| CharOperation.equals(name, IOTConstants.OTCONFINED)
				|| CharOperation.equals(name, IOTConstants.CONFINED);
	}

	/**
	 * does compoundName denote a predefined confined type?
	 */
	public static boolean isPredefinedConfined(char[][] compoundName)
	{
		if (compoundName.length == 3) {
			return
				   CharOperation.equals(compoundName, IOTConstants.ORG_OBJECTTEAMS_ICONFINED)
				|| CharOperation.equals(compoundName, IOTConstants.ORG_OBJECTTEAMS_ITEAM_ICONFINED)
				|| CharOperation.equals(compoundName, IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED)
				|| CharOperation.equals(compoundName, IOTConstants.ORG_OBJECTTEAMS_TEAM_CONFINED);
		}
		return false;
	}


}
