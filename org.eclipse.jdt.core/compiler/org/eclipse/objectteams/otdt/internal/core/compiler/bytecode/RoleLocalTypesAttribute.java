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
 * $Id: RoleLocalTypesAttribute.java 23416 2010-02-03 19:59:31Z stephan $
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
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;

/**
 * MIGRATION_STATE: complete.
 *
 * Represents the "ReferencedTeams" attribute by which dependencies
 * among classes are stored in the byte code, such that the OTRE
 * can load teams before their base classes.
 *
 * @author stephan
 * @version $Id: RoleLocalTypesAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class RoleLocalTypesAttribute extends ListValueAttribute {

    private RoleModel _role;
    private ReferenceBinding[] _localTypes; // array of ReferenceBinding
    private char[][] _names = new char[0][];

    /**
     * @param type the referencing type represented by its RoleModel
     */
    public RoleLocalTypesAttribute(RoleModel type) {
        super(IOTConstants.ROLE_LOCAL_TYPES,
                0,  // count is not yet known, add in write(..)
                2); // each element is a name reference
        this._role = type;
    }

    /**
     * Create an attribute from byte code.
     *
     * (Invoked from ClassFileReader).
     */
    public RoleLocalTypesAttribute(
            ClassFileStruct reader,
            int             readOffset,
            int[]           constantPoolOffsets)
    {
        super(IOTConstants.ROLE_LOCAL_TYPES, 0, 2);
        readList(reader, readOffset, 0 /* no structOffset */, constantPoolOffsets);
    }

    @Override
    public boolean setupForWriting() {
        // before starting to write out the attribute prepare the data:
        this._localTypes = this._role.getLocalTypes();
        this._count = this._localTypes.length;
        int k=0; // index for shrinking the array, if types have no constantPoolName.
        for (int i=0; i<this._localTypes.length; i++) {
        	if (this._localTypes[i].constantPoolName() == null)
        		// assuming this only happens after an error has been reported
        		// Note that this is difficult to check, since the ASTNode that
        		// carries the flag ignoreFurtherInvestigation may be at any distance.
        		this._count--;
        	else
        		this._localTypes[k++] = this._localTypes[i];
        }
        return this._count > 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#writeElementValue(int)
     */
    protected void writeElementValue(int i) {
        ReferenceBinding typeBinding = this._localTypes[i];
        writeName(typeBinding.constantPoolName()); // don't use attribute name here (has $Local$ prefix ...)
    }

    void read(int i)
    {
        if (i==0)
            this._names = new char[this._count][];
        this._names[i] = consumeName();
    }


    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
     */
    public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
    	checkBindingMismatch(binding, ExtraCompilerModifiers.AccRole);
    	this._localTypes = new ReferenceBinding[this._count];

    	ReferenceBinding refBinding = (ReferenceBinding)binding;
		RoleModel role = refBinding.roleModel;
		if (role == null)
    		refBinding.roleModel = role = new RoleModel(refBinding);
        for (int i=0; i<this._names.length; i++) {
        	this._localTypes[i] = environment.getTypeFromConstantPoolName(this._names[i], 0, -1, false, missingTypeNames); //FIXME(GENERIC): check param 'false'!
        	if (this._localTypes[i] instanceof UnresolvedReferenceBinding)
        		this._localTypes[i] = resolveReferenceType(environment, (UnresolvedReferenceBinding)this._localTypes[i]);
        	role.addBinaryLocalType(this._localTypes[i]);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#toString(int)
     */
    protected String toString(int i) {
        return new String(this._localTypes[i].constantPoolName());
    }

}
