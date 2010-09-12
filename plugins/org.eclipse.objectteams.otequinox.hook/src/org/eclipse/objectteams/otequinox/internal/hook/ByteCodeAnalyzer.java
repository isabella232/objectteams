/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TransformerPlugin.java 15357 2007-02-18 17:01:36Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal.hook;

/** 
 * This class is used to peek the byte code before passing it to the transformers or the JVM. 
 * 
 * @author stephan
 */
public class ByteCodeAnalyzer {

	// cannot import jdt.core, so repeat some constants here:
	static final int AccTeam = 0x8000;
	static final int Utf8Tag = 1;
	static final int LongTag = 5;
	static final int DoubleTag = 6;
	static final int[] CPEntryLengths = new int[] {
		0,
		3, // UTF-8
		0,
		5, // integer
		5, // float
		9, // long
		9, // double
		3, // class
		3, // string
		5, // field
		5, // method
		5, // ifc method
		5,  // name and type
	};

	/** Peek the bytecode for class flags to test if current class is a team. */
	static boolean isTeam(byte[] classbytes) 
	{
		int constantPoolCount = combineTwoBytes(classbytes, 8);
		int readOffset = 10;
		
		// skip over constant pool:
		for (int i = 1; i < constantPoolCount; i++) {
			int tag = classbytes[readOffset];
			if (tag == Utf8Tag) 
				readOffset += combineTwoBytes(classbytes, readOffset + 1);
			if (tag < CPEntryLengths.length)
				readOffset += CPEntryLengths[tag];
			else 
				throw new RuntimeException("bad tag?"); //$NON-NLS-1$
			if (tag == DoubleTag || tag == LongTag)
				i++;
		}
		int classFlags = combineTwoBytes(classbytes, readOffset);
		return (classFlags & AccTeam) != 0;
	}

	/* helper for above: read an unsigned short. */
	static int combineTwoBytes(byte [] bytes, int start) {
		int first  = bytes[start];
		int second = bytes[start+1];
	    int twoBytes = 0;
	
	    twoBytes = twoBytes|(first&0xff);
	    twoBytes = twoBytes<<8;
	    twoBytes = twoBytes|(second&0xff);
	    return twoBytes;
	}

}
