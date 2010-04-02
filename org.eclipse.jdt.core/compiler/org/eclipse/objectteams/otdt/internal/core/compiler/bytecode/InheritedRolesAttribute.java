/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: InheritedRolesAttribute.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

/**
 * This attribute stores the names of all roles which have a tsuper version in the super team.
 * Needed by the OTRE to create calls to the registration method _OT$registerObserver().
 * By such a call a team signals that it needs to be notified when the super-team is activated
 * in order to propagate this activation from tsuper roles down to tsub roles (serving as bases).
 *
 * @author stephan
 *
 */
public class InheritedRolesAttribute extends ListValueAttribute {

    private ReferenceBinding _teamBinding;
    private ReferenceBinding[] _roles;

    /**
     * @param type the team whose roles should be stored.
     */
    public InheritedRolesAttribute(ReferenceBinding teamBinding) {
        super(IOTConstants.INHERITED_ROLES,
                0,  // count is not yet known, add in setupForWriting(..)
                2); // each element is a name reference
        this._teamBinding = teamBinding;
    }

    @Override
    public boolean setupForWriting() {
        // before starting to write out the attribute prepare the data:
    	this._count=0;
    	ReferenceBinding[] memberTypes = this._teamBinding.memberTypes();
		for(ReferenceBinding role : memberTypes)
    		if (role.isRole() && role.roleModel.hasRelevantTSuperRole()) // enum-memberTypes are not roles!
    			this._count++;
    	this._roles = new ReferenceBinding[this._count];
    	int j=0;
    	for(int i=0; i<memberTypes.length; i++)
    		if (memberTypes[i].isRole() && memberTypes[i].roleModel.hasRelevantTSuperRole())
    			this._roles[j++] = memberTypes[i];
    	return this._count > 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#writeElementValue(int)
     */
    protected void writeElementValue(int i) {
        writeName(this._roles[i].attributeName());
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
     */
    public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
        // nop: this is a one-way attribute: not read from byte code.
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#toString(int)
     */
    protected String toString(int i) {
        return new String(this._roles[i].attributeName());
    }

}
