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
 * $Id: PlainAttribute.java 23416 2010-02-03 19:59:31Z stephan $
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
 * Attribute without a value.
 * Uses:
 *    CallsBaseConstructor
 *
 * @author stephan
 * @version $Id: PlainAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class PlainAttribute extends AbstractAttribute {

    /**
     * @param name
     */
    public PlainAttribute(char[] name) {
        super(name);
    }

    int size() {
    	return 6;
    }

    public void write (ClassFile classFile) {
        super.write(classFile);
        if (this._contentsOffset + 6 > this._contents.length) {
        	this._contents = classFile.getResizedContents(8);
        }
        // write the name
        int attributeNameIndex = this._constantPool.literalIndex(this._name);
        this._contents[this._contentsOffset++] = (byte) (attributeNameIndex >> 8);
        this._contents[this._contentsOffset++] = (byte) attributeNameIndex;
        // The length of a plain attribute is 0 (fixed-length).
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 0;
        writeBack(classFile);
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
     */
    public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) { /* noop */ }

}
