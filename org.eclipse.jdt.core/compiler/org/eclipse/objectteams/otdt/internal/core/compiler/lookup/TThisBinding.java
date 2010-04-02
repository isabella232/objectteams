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
 * $Id: TThisBinding.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

/**
 * NEW for OTDT.
 *
 * This class model the implicit enclosing Team instance that is used
 * for type checking of role types that have no explicit anchor.
 *
 * @author stephan
 * @version $Id: TThisBinding.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class TThisBinding extends FieldBinding
{

	/**
	 * Only TeamModel should use this:
	 * @param teamBinding
	 */
    public TThisBinding (ReferenceBinding teamBinding) {
    	super(IOTConstants.TTHIS, teamBinding, ClassFileConstants.AccFinal, teamBinding, Constant.NotAConstant);
    }

    // support for assembly of bestNamePath:
    protected TeamAnchor getClone() {
    	return new TThisBinding((ReferenceBinding)this.type);
    }

    /**
     *  Get the implicit Team-this for a given role.
     *  @param role
     */
    public static VariableBinding getTThisForRole (ReferenceBinding role)
    {
    	if (role == null)
    		return null;
        if (role.isDirectRole())
            return role.enclosingType().getTeamModel().getTThis();
        return null;
    }

    /**
     * Get the implicit Team-this for a given role.
     * This is also the place to implement the checks for identical Team instances
     * (not yet implemented).
     * @param role
     * @param site The class in which the current expression occurred.
     */
    public static VariableBinding getTThisForRole (ReferenceBinding role, ReferenceBinding site)
    {
        role = RoleTypeCreator.findExactRole(role, site);
        return getTThisForRole(role);
    }

    public String toString() {
        return new String(readableName());
    }
    public char[] readableName() {
    	if (this.name != IOTConstants.TTHIS)
    		return this.name;
        char[] tmp = CharOperation.concat(this.name, this.type.sourceName(), '[');
        return CharOperation.append(tmp, ']');
    }

    @Override
    public ITeamAnchor setPathPrefix(ITeamAnchor prefix) {
    	assert (prefix.getResolvedType().isCompatibleWith(this.type));
    	return prefix; // appending tthis makes no sense
    }
}
