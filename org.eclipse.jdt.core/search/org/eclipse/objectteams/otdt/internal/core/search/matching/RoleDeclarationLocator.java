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
 * $Id: RoleDeclarationLocator.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.search.matching;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.core.search.matching.MatchingNodeSet;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationLocator;

/**
 * NEW for OTDT
 * 
 * A Locator for Roles (all non-teams are IMPOSSIBLE_MATCHes) 
 * @author gis
 */
public class RoleDeclarationLocator extends TypeDeclarationLocator
{
    public RoleDeclarationLocator(RoleTypePattern pattern)
    {
        super(pattern);
    }

    public int match(TypeDeclaration type, MatchingNodeSet nodeSet)
    {
        if (!Flags.isRole(type.modifiers))
            return IMPOSSIBLE_MATCH;
        
        return super.match(type, nodeSet);
    }

    protected int resolveLevelForType(char[] qualifiedPattern, TypeBinding type)
    {
        if (type.leafComponentType().isBaseType())
            return IMPOSSIBLE_MATCH;

        // leaf must be a reference binding then
        if (!((ReferenceBinding) type.leafComponentType()).isRole())
            return IMPOSSIBLE_MATCH;
        
        return super.resolveLevelForType(qualifiedPattern, type);
    }
}
