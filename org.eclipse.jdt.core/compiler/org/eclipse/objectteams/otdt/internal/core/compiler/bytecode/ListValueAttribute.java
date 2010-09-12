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
 * $Id: ListValueAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;


/**
 * MIGRATION_STATE: complete.
 *
 * Bytecode attributes with a uniform list of values.
 * Currently handled (by sub-classes):
 * <ul>
 * <li>TypeAnchorList   by class AnchorListAttribute
 * <li>ReferenceTeams   by class ReferencedTeamsAttribute
 * <li>RoleBaseBindings by class RoleBaseBindingsAttribute
 * </ul>
 *
 * @author stephan
 * @version $Id: ListValueAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public abstract class ListValueAttribute
        extends AbstractAttribute
{

    /** Number of elements in this list. */
    int _count;

    /** Size (in bytes) of one element. */
    private int _elementSize;

    ListValueAttribute(char[] name, int count, int elementSize) {
        super(name);
        this._count       = count;
        this._elementSize = elementSize;
    }

    @Override
    public boolean setupForWriting() {
    	return this._count > 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#write(org.eclipse.jdt.internal.compiler.ClassFile)
     */
    public void write(ClassFile classFile)
    {
        // this code is mainly stolen from ClassFile.addAttributes().
        super.write(classFile);

        int attributeSize  = getAttributeSize(); 
        if (this._contentsOffset + 6 + attributeSize >= this._contents.length) {
        	this._contents = classFile.getResizedContents(6 + attributeSize);
        }
        writeName         (this._name);
        writeInt          (attributeSize);
        writeUnsignedShort(this._count);
        for (int i=0; i<this._count; i++) {
            writeElementValue(i);
        }
        writeBack(classFile);
    }

	/* Only attributes with variable length elements need to override this. */
	protected int getAttributeSize() {
		return this._count*this._elementSize+2; // 2=elementCount
	}

    /**
     * Hook method for writing one element of the list.
     * @param i number of the element to write
     */
    abstract void writeElementValue (int i);


    void readList(
            ClassFileStruct reader,
            int readOffset,
            int structOffset,
            int[] constantPoolOffsets)
    {
        this._count = reader.u2At(readOffset);
        this._reader              = reader;
        this._readOffset          = readOffset + 2;
        this._structOffset        = structOffset;
        this._constantPoolOffsets = constantPoolOffsets;
        for (int i=0; i<this._count;i++) {
            read(i); // read must advance _reatOffset appropriately
        }
    }

    void read(int i)
    {
    	this._readOffset += this._elementSize;
        // NOOP. override to do usefull things. (not all sub-classes need this).
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding)
     */
    public abstract void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames);

    @SuppressWarnings("nls")
	public String toString()
    {
        String str = "OT-Attribute "+new String(this._name)+": [";
        for (int i=0; i<this._count; i++) {
            str = str+toString(i)+((i<this._count-1)?", ":"");
        }
        return str+"]";
    }

    // debug hook method: print one element.
    abstract String toString(int i);
}
