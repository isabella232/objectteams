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
 * $Id: SelectionOnMethodSpec.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.codeassist;

import org.eclipse.jdt.internal.codeassist.select.SelectionNodeFound;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutScope;

/**
 * @author haebor
 */
public class SelectionOnMethodSpec extends MethodSpec
{
	BlockScope scope = null;

	public SelectionOnMethodSpec(char[] ident, long pos)
	{
		super(ident, pos);
	}

	public SelectionOnMethodSpec(AbstractMethodDeclaration md)
	{
	    super(md);
	}

	@Override
	public void resolveTypes(CallinCalloutScope aScope, boolean isBaseSide) {
		super.resolveTypes(aScope, isBaseSide);
		this.scope = aScope;
	}
	@Override
    public void resolveFinished()
    {
    	MethodBinding binding = this.resolvedMethod;
		// tolerate some error cases
		if(binding == null ||
					!(binding.isValidBinding() ||
						binding.problemId() == ProblemReasons.NotVisible
						|| binding.problemId() == ProblemReasons.InheritedNameHidesEnclosingName
						|| binding.problemId() == ProblemReasons.NonStaticReferenceInConstructorInvocation
						|| binding.problemId() == ProblemReasons.NonStaticReferenceInStaticContext))
		{
			throw new SelectionNodeFound();
		}
		else
		{
//{ObjectTeams:
			//method is part of a role
		    if(binding.declaringClass.isRole())
		    {
				// method is copy inherited: use the original binding:
		    	if(binding.copyInheritanceSrc != null)
		    		throw new SelectionNodeFound(binding.copyInheritanceSrc);
		    }
 //haebor}
		}
        throw new SelectionNodeFound(this.resolvedMethod);
    }

    @Override
    public StringBuffer print(int indent, StringBuffer output) {
		output.append("<SelectOnMethodSpec:"); //$NON-NLS-1$
		super.print(indent, output);
		return output.append(">"); //$NON-NLS-1$
    }
}
