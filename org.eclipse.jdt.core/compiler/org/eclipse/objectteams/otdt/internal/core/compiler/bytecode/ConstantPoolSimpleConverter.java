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
 * $Id: ConstantPoolSimpleConverter.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;

/**
 * MIGRATION_STATE: complete.
 *
 * Simplified conversion of byte code attributes.
 *
 * @author stephan
 * @version $Id: ConstantPoolSimpleConverter.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class ConstantPoolSimpleConverter extends ClassFileStruct {

	int srcOffset;
	byte[] dest;
	ClassFile dstClassFile;

	/**
	 * @param srcRole    where the byte code is read from
	 * @param srcBytes   bytes to read from
	 * @param srcOffset  offset into srcBytes
	 * @param destBytes  where to write to
	 * @param dstClassFile ClassFile of the destination class
	 */
	public static ConstantPoolSimpleConverter create(
			RoleModel srcRole, MethodBinding srcMethod,
			byte[] destBytes, ClassFile dstClassFile)
	{
		if (srcRole != null)
			return new ConstantPoolSimpleConverter(srcRole, srcMethod, destBytes, dstClassFile);
		else
			return new ConstantPoolSimpleConverter(srcMethod, destBytes, dstClassFile);
	}

	private ConstantPoolSimpleConverter(
			RoleModel srcRole, MethodBinding srcMethod,
			byte[] destBytes, ClassFile dstClassFile)
	{
		super(srcRole.getByteCode(), srcRole.getConstantPoolOffsets(), 0);
		this.srcOffset = srcRole.getByteCodeOffset(srcMethod);
		this.dest = destBytes;
		this.dstClassFile = dstClassFile;
	}

	private ConstantPoolSimpleConverter(
			MethodBinding srcMethod, byte[] destBytes, ClassFile dstClassFile)
	{
		super(srcMethod.model.getBytes(), srcMethod.model.getConstantPoolOffsets(), 0);
		this.srcOffset = srcMethod.model.getStructOffset();
		this.dest = destBytes;
		this.dstClassFile = dstClassFile;
	}
	
	public ConstantPoolSimpleConverter(byte[] bytes, int[] constantPoolOffsets, int methodOffset, byte[] destBytes, ClassFile dstClassFile) {
		super(bytes, constantPoolOffsets, 0);
		this.srcOffset = methodOffset;
		this.dest = destBytes;
		this.dstClassFile = dstClassFile;
	}

	/** Read a name at offset and write it back into the destination with adjustment. */
	public char[] updateName (int offset) {
        int ref =  OTByteCodes.getWord(this.reference, this.srcOffset+offset);
		char[] name = getUtf8(ref);
		writeName(offset, name);
		return name;
	}

	/** Write a name into destintion with adjustment. */
	public void writeName(int offset, char[] name) {
		write2(this.dest, offset, this.dstClassFile.constantPool.literalIndex(name));
	}

	private char[] getUtf8(int index){
		int start = this.constantPoolOffsets[index];
		assert(u1At(start)==ClassFileConstants.Utf8Tag);
		return utf8At(start + 3, u2At(start + 1));
	}

    private void write2(byte[] code, int offset, int value) {
        code[offset]   = (byte)(value >> 8);
        code[offset+1] = (byte)value;
    }

}
