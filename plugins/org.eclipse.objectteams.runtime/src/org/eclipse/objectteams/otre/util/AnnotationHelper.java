/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2009 Stephan Herrmann
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 			Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.util;

import org.eclipse.objectteams.otre.ObjectTeamsTransformation;
import org.objectteams.ImplicitTeamActivation;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.generic.ConstantPoolGen;

/** 
 * Helper class for parsing / skipping runtime visible annotations.
 * 
 * @author stephan
 * @since 1.4.0
 */
public class AnnotationHelper {

	/**
	 * Does any of the given attributes contain an {@link ImplicitTeamActivation} annotation?
	 * @param attrs attributes to inspect
	 * @param cpg   constant pool for string lookup
	 * @return true if an {@link ImplicitTeamActivation} annotation was found.
	 */
	public static boolean containsImplicitActivationAttribute(Attribute[] attrs, ConstantPoolGen cpg) {
		if (attrs != null) {
			for (Attribute attr : attrs) {
				if (attr instanceof Unknown && ((Unknown)attr).getName().equals("RuntimeVisibleAnnotations")) {
					Unknown unknown = (Unknown) attr;
					byte[] bytes = unknown.getBytes();
					int len = ObjectTeamsTransformation.combineTwoBytes(bytes, 0);
					int i = 2;
					String[] names = new String[1];
					for (int n=0; n<len; n++) {
						i = ObjectTeamsTransformation.scanStrings(names, bytes, i, cpg);
						if ("Lorg/objectteams/ImplicitTeamActivation;".equals(names[0]))
							return true;
						i = skipNameValuePairs(bytes, i, names[0], cpg);
					}
				}						
			}
		}
		return false;
	}

	private static int  skipNameValuePairs(byte[] bytes, int i, String typeName, ConstantPoolGen cpg) {
		int numPairs = ObjectTeamsTransformation.combineTwoBytes(bytes, i);
		i+=2;
		for (int p=0; p<numPairs; p++)
			i = skipElementValue(bytes, i+2 /*skip name*/, typeName, cpg);
		return i;
	}

	private static int skipElementValue(byte[] bytes, int i, String typeName, ConstantPoolGen cpg) {
		short tag = bytes[i++];
		switch (tag) {
		case 'B': // byte
		case 'C': // char
		case 'D': // double
		case 'F': // float
		case 'I': // int
		case 'J': // long
		case 'S': // short
		case 'Z': // boolean
		case 's': // String
		case 'c': // Class
			i+=2; break;
		case 'e': // Enum constant
			i+=4; break;
		case '@': // Annotation
			String[] typeName2 = new String[1];
			i = ObjectTeamsTransformation.scanStrings(typeName2, bytes, i, cpg); // nested annotation type			
			i = skipNameValuePairs(bytes, i, typeName2[0], cpg);
			break;
		case '[': // Array
			int numArrayVals = ObjectTeamsTransformation.combineTwoBytes(bytes, i);
			i+=2;
			for (int j = 0; j < numArrayVals; j++)
				i = skipElementValue(bytes, i, typeName, cpg);
			break;
		default:
			throw new RuntimeException("Unexpected element value kind in annotation: " + typeName);
		}
		return i;
	}

}
