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
 * $Id: RoleFilesAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

/**
 * MIGRATION_STATE: complete.
 *
 * Attached to a synthetic class <team>$RoFi__OT__, this attribute lists all
 * known roles stored in role files.
 *
 * @author stephan
 * @version $Id: RoleFilesAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class RoleFilesAttribute extends ListValueAttribute {

	char[][] roleNames;

	/**
	 * @param name
	 * @param count
	 * @param elementSize
	 */
	public RoleFilesAttribute(char[][] names) {
		super(IOTConstants.ROLE_FILES, names.length, 2);
		this.roleNames = names;
	}

    public RoleFilesAttribute(
            ClassFileStruct reader,
            int             readOffset,
            int[]           constantPoolOffsets)
    {
        super(IOTConstants.ROLE_FILES, 0, 2);
        readList(reader, readOffset, 0 /* no structOffset */, constantPoolOffsets);
    }

	public char[][] getNames() {
		return this.roleNames;
	}

    protected void writeElementValue(int i) {
        writeName(this.roleNames[i]);
    }

    void read(int i) {
        if (i==0)
            this.roleNames = new char[this._count][];
    	this.roleNames[i] = consumeName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#toString(int)
     */
    protected String toString(int i) {
        return new String(this.roleNames[i]);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
	 */
	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		checkBindingMismatch(binding, 0);
		((ReferenceBinding)binding).model._roleFilesAttribute = this;
	}


}
