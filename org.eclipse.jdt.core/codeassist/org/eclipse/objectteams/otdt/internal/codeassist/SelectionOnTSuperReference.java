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
 * $Id: SelectionOnTSuperReference.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.codeassist;

import org.eclipse.jdt.internal.codeassist.select.SelectionNodeFound;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TsuperReference;

/**
 * @author haebor
 */
public class SelectionOnTSuperReference extends TsuperReference
{

    /*
     * Selection node build by the parser in any case it was intending to
     * reduce a tsuper reference containing the assist identifier.
     * e.g.
     *
     *	class X extends Z {
     *    class Y {
     *    	void foo() {
     *      	[start]tsuper[end].bar();
     *      }
     *    }
     *  }
     *
     *	---> class X {
     *		   class Y {
     *           void foo() {
     *             <SelectOnTSuperReference:tsuper>
     *           }
     *         }
     *       }
     *
     */
    public SelectionOnTSuperReference(int pos, int sourceEnd)
    {
    	super(pos, sourceEnd);
    }

    public StringBuffer printExpression(int indent, StringBuffer output)
    {

    	output.append("<SelectOnTSuper:"); //$NON-NLS-1$
    	return super.printExpression(0, output).append('>');
    }

    public TypeBinding resolveType(BlockScope scope)
    {
    	TypeBinding tSuperRole = super.resolveType(scope);

    	if (tSuperRole == null || !tSuperRole.isValidBinding())
    		throw new SelectionNodeFound();
    	else {
            throw new SelectionNodeFound(tSuperRole);
    	}
    }

}
