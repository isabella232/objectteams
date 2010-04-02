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
 * $Id: InlineAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

/**
 * MIGRATION_STATE: complete.
 *
 * This class implements byte code attributes which store a given byte array
 * inline (ie., without indirection using the constant pool).
 *
 * Currently handled:
 * <ul>
 * 	<li>SourceDebugExtension
 *       see JSR 045
 * </ul>
 *
 * @author stephan
 */
public class InlineAttribute extends AbstractAttribute {


    public static InlineAttribute sourceDebugExtensionAttribute(char[] contents) {
    	return new InlineAttribute(SOURCE_DEBUG_EXTENSION, contents);
    }

    // ============== INSTANCE FEATURES ===================

    // the attribute value
	protected char[] _value;

	/**
	 * @param name  Attribute Name
	 * @param value contents to be stored directly in this attribute.
	 */
	protected InlineAttribute(char[] name, char[] value) {
		super(name);
		this._value = value;
	}

    int size() {
    	return 6 + this._value.length;
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#write(org.eclipse.jdt.internal.compiler.ClassFile)
     */
    public void write(ClassFile classFile)
    {
    	super.write(classFile);

    	int size = size();
        if (this._contentsOffset + size > this._contents.length) {
        	this._contents = classFile.getResizedContents(size);
        }
        // write the name
        int attributeNameIndex = this._constantPool.literalIndex(this._name);
        this._contents[this._contentsOffset++] = (byte) (attributeNameIndex >> 8);
        this._contents[this._contentsOffset++] = (byte) attributeNameIndex;
        // write the length
        int length = this._value.length;
        this._contents[this._contentsOffset++] = (byte) (length >>> 24);
        this._contents[this._contentsOffset++] = (byte) (length >> 16);
        this._contents[this._contentsOffset++] = (byte) (length >> 8);
        this._contents[this._contentsOffset++] = (byte) length;
        // write the value
		for (int i = 0; i < length; i++) {
			this._contents[this._contentsOffset++] = (byte)this._value[i];
		}

        writeBack(classFile);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
	 */
	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		// nothing to evaluate yet. SourceDebugExtension is not read from byte code.
	}

}
