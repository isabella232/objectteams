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
 * $Id: ResultReference.java 19881 2009-04-13 23:35:46Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * NEW for OTDT.
 *
 * Special reference "result" referring to the result of a bound method.
 * Only the target side is supported, where result may appear within
 * arbitrary expressions. At the source side, "result" is just a name,
 * not a reference.
 *
 * @author stephan
 * @version $Id: ResultReference.java 19881 2009-04-13 23:35:46Z stephan $
 */
public class ResultReference extends SingleNameReference {

    /** We use the method mapping to find the resolved type. */
    private AbstractMethodMappingDeclaration _mapping = null;

    /**
     * Create a ResultReference by copying from a SingleNameReference
     * @param ref template
     * @param mapping link it with this mapping.
     */
    public ResultReference(SingleNameReference ref, AbstractMethodMappingDeclaration mapping)
    {
    	super(ref.token, ((long)ref.sourceStart<<32)+ref.sourceEnd);
        this.bits =  Binding.LOCAL; // restrictiveFlag (from NameReference)
        this._mapping    = mapping;
        this.constant = Constant.NotAConstant;
    }

    /**
     * "result" is resolved by lookup in the (hopefully resolved) mapping.
     */
    public TypeBinding resolveType(BlockScope scope) {
    	if (this._mapping.resultVar == null) {
    		// TODO (SH): check what was the reason for non-existent resultVar.
    		scope.problemReporter().resultMappingForVoidMethod(this._mapping, this);
    		return null;
    	}
        this.binding = this._mapping.resultVar.binding;
        this.resolvedType = this._mapping.resultVar.type.resolvedType;
        this.constant = Constant.NotAConstant;
        assert (this.resolvedType != null);
        return this.resolvedType;
    }
}
