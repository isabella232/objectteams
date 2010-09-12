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
 * $Id: ReferencedTeamsAttribute.java 23416 2010-02-03 19:59:31Z stephan $
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
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;

/**
 * MIGRATION_STATE: complete.
 *
 * Represents the "ReferencedTeams" attribute by which dependencies
 * among classes are stored in the byte code, such that the OTRE
 * can load teams before their base classes.
 *
 * Location:
 * An arbitrary class which references a Team. If a team extends another team
 * the super team is also part of the "ReferencedTeams" attribute. Additionally every team
 * is part  of its own "ReferencedTeams" attribute.
 *
 * Content:
 * A list of (fully qualified) team names.
 *
 * Purpose:
 * The OTRE uses this attribute to load all teams and belonging roles and collect the contained
 * binding attributes BEFORE the coresponding base classes are loaded.
 * Thus the information about callins to be woven into the base code  is known when a base class
 * is loaded and transformed.
 *
 * @author stephan
 * @version $Id: ReferencedTeamsAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class ReferencedTeamsAttribute extends ListValueAttribute {

    private TypeModel _type;
    private ReferenceBinding[] _teams; // array of ReferenceBinding

    /**
     * @param type the referencing type represented by its TypeModel
     */
    public ReferencedTeamsAttribute(TypeModel type) {
        super(IOTConstants.REFERENCED_TEAMS,
                0,  // count is not yet known, add in write(..)
                2); // each element is a name reference
        this._type = type;
    }

    @Override
    public boolean setupForWriting() {
        // before starting to write out the attribute prepare the data:
        this._teams = this._type.getReferencedTeams();
        this._count = this._teams.length;
    	return this._count > 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#writeElementValue(int)
     */
    protected void writeElementValue(int i) {
        ReferenceBinding teamBinding = this._teams[i];
        writeName(teamBinding.attributeName());
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
        return new String(this._teams[i].attributeName());
    }

}
