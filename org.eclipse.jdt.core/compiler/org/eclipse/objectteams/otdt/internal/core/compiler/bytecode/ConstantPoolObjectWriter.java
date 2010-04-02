/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ConstantPoolObjectWriter.java 23416 2010-02-03 19:59:31Z stephan $
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
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;

/**
 * MIGRATION_STATE: complete.
 *
 * @author Markus Witte
 * @version $Id: ConstantPoolObjectWriter.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class ConstantPoolObjectWriter implements ClassFileConstants  {

	private ClassFile dstClassFile;

	/**
	 * @param dstClassFile
	 */
	public ConstantPoolObjectWriter(ClassFile dstClassFile) {
		this.dstClassFile=dstClassFile;
	}

	/**
	 * This method adds a ConstantPool Entry in the corresponding cache of this ConstantPool
	 * @param cpo the ConstantPoolObject which should be added
	 * @return a new reference into this ConstantPool
	 */
	int writeConstantPoolObject(ConstantPoolObject cpo) {
		int type = cpo.getType();
		switch(type){
			case StringTag : 					return this.dstClassFile.constantPool.literalIndex(cpo.getString());
			case IntegerTag : 					return this.dstClassFile.constantPool.literalIndex(cpo.getInteger());
			case FloatTag : 					return this.dstClassFile.constantPool.literalIndex(cpo.getFloat());
			case LongTag : 						return this.dstClassFile.constantPool.literalIndex(cpo.getLong());
			case DoubleTag : 					return this.dstClassFile.constantPool.literalIndex(cpo.getDouble());
			case ClassTag :
				return this.dstClassFile.codeStream.recordTypeBinding(cpo.getClassObject()); // record type anchor, too.
			case FieldRefTag :
				FieldBinding fieldBinding = cpo.getFieldRef();
				return this.dstClassFile.constantPool.literalIndexForField(
														fieldBinding.declaringClass.constantPoolName(),
														fieldBinding.name,
														fieldBinding.type.signature());
			case MethodRefTag :
				MethodBinding methodBinding = cpo.getMethodRef();
				return this.dstClassFile.constantPool.literalIndexForMethod(
														methodBinding.declaringClass.constantPoolName(),
														methodBinding.selector,
														methodBinding.signature(),
														false); // class
			case InterfaceMethodRefTag :
				methodBinding = cpo.getInterfaceMethodRef();
				return this.dstClassFile.constantPool.literalIndexForMethod(
														methodBinding.declaringClass.constantPoolName(),
														methodBinding.selector,
														methodBinding.signature(),
														true); // interface
			case Utf8Tag :
				return this.dstClassFile.constantPool.literalIndex(cpo.getUtf8());
			case NameAndTypeTag :		//...
			default:
				throw new RuntimeException();
		}
	}

    public void writeConstantPoolObject(byte[] code, int offset, int length, ConstantPoolObject cpo)
    {
        int index = writeConstantPoolObject(cpo);
        switch (length) {
        case 1:
            write1(code, offset, index);
            break;
        case 2:
            write2(code, offset, index);
            break;
        default:
            throw new InternalCompilerError("unexpected value length "+length); //$NON-NLS-1$
        }
    }

    public void writeUtf8 (byte[] code, int offset, char[] value)
    {
        int index = this.dstClassFile.constantPool.literalIndex(value);
        write2(code, offset, index);
    }
    public void write2(byte[] code, int offset, int value)
    {
        code[offset]   = (byte)(value >> 8);
        code[offset+1] = (byte)value;
    }
    private void write1(byte[] code, int offset, int value)
    {
        code[offset] = (byte)value;
    }
}
