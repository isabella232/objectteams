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
 * $Id: RoleBaseBindingsAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import java.util.LinkedList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

/**
 * MIGRATION_STATE: complete.
 *
 * Represents the "CallinRoleBaseBindings" attribute.
 * List of pairs (rolename x basename).
 * Records the bound roles of one team.
 *
 * Location:
 * A team class containing a role class bound by 'playedBy' in this team or in any
 * super team (this attribute is copied into every sub team).
 *
 * Content:
 * A list of pairs of role class names and base class names.
 *
 * Purpose:
 * The OTRE uses this attribute to ensure, that all bound roles of a team are loaded next
 * (prior to the base classes).
 * The binding information (role <-> base) is stored and used for later transformations.
 *
 *
 * @author stephan
 * @version $Id: RoleBaseBindingsAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class RoleBaseBindingsAttribute extends ListValueAttribute {

    private LinkedList<char[]> roleNames = new LinkedList<char[]>();
    private LinkedList<char[]> baseNames = new LinkedList<char[]>();

    /**
     * Create an empty attribute for role base bindings.
     */
    public RoleBaseBindingsAttribute() {
        super(IOTConstants.ROLE_BASE_BINDINGS, 0, 4); // 2 names
    }

	/** Add a role-base pair to this attribute. */
    public void add(char[] roleName, char[] baseName, boolean baseIsInterface) {
    	if (baseIsInterface)
    		baseName = CharOperation.concat(new char[]{'^'}, baseName);
        this._count++;
        this.roleNames.add(roleName);
        this.baseNames.add(baseName);
    }
    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#writeElementValue(int)
     */
    protected void writeElementValue(int i) {
        writeName(this.roleNames.get(i));
        writeName(this.baseNames.get(i));
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
     */
    public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
        // nothing, don't read from classfile
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#toString(int)
     */
    protected String toString(int i) {
        // TODO Auto-generated method stub
        return null;
    }

}
