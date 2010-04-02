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
 * $Id: SelectionOnBaseReference.java 23416 2010-02-03 19:59:31Z stephan $
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
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.BaseReference;

/**
 * @author haebor
 */
public class SelectionOnBaseReference extends BaseReference
{

    /*
     * Selection node build by the parser in any case it was intending to
     * reduce a base reference containing the assist identifier.
     * e.g.
     *
     *	class X extends Z {
     *    class Y {
     *    	void foo() {
     *      	[start]base[end].bar();
     *      }
     *    }
     *  }
     *
     *	---> class X {
     *		   class Y {
     *           void foo() {
     *             <SelectOnBase:_OT$base>
     *           }
     *         }
     *       }
     *
     */
    public SelectionOnBaseReference(int pos, int sourceEnd)
    {
    	super(pos, sourceEnd);
    }

    public StringBuffer printExpression(int indent, StringBuffer output)
    {
    	output.append("<SelectOnBase:"); //$NON-NLS-1$
    	return super.printExpression(0, output).append('>');
    }

    public TypeBinding resolveType(BlockScope scope)
    {
    	ReferenceBinding binding = (ReferenceBinding) super.resolveType(scope);

//        ReferenceBinding superclass = (SourceTypeBinding)((MemberTypeBinding)this.resolvedType).enclosingType.superclass;
//        String roleName = new String(this.resolvedType.sourceName());
//        ReferenceBinding base = scope.findDirectMemberType((IOTConstants.OT_DELIM + roleName).toCharArray(), (ReferenceBinding) this.resolvedType);
//        ReferenceBinding base = scope.findDirectMemberType((IOTConstants.OT_DELIM + roleName).toCharArray(), (ReferenceBinding) this.resolvedType);
    	if (binding == null || !binding.isValidBinding() || binding.baseclass() == null)
    	{
    		throw new SelectionNodeFound();
    	}
    	else
    	{
    		throw new SelectionNodeFound(binding.baseclass());
    	}
    }

}
