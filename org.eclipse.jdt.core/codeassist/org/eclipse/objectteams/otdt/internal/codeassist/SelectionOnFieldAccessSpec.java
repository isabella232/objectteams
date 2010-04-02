/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2007, 2010 Technical University Berlin, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SelectionOnFieldAccessSpec.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.codeassist;

import org.eclipse.jdt.internal.codeassist.select.SelectionNodeFound;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.FieldAccessSpec;

/** Make rhs of callout-to-field selectable. */
public class SelectionOnFieldAccessSpec extends FieldAccessSpec {

	public SelectionOnFieldAccessSpec(char[] name,
									  TypeReference type,
									  long nameSourcePositions,
									  int calloutModifier)
	{
		super(name, type, nameSourcePositions, calloutModifier);
	}

	@Override
	public void resolveFinished() {
		// cf. SelectionOnMethodSpec:
		FieldBinding binding = this.resolvedField;
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
			//field is part of a role
		    if(binding.declaringClass.isRole()) {
				// field is copy inherited: use the original binding:
		    	if(binding.copyInheritanceSrc != null)
		    		throw new SelectionNodeFound(binding.copyInheritanceSrc);

		    	//have to generate a new binding here!
		    	ReferenceBinding roleClass = binding.declaringClass.roleModel.getClassPartBinding();
		    	FieldBinding newBinding = roleClass.getField(binding.name,true);
		    	throw new SelectionNodeFound(newBinding);
		    }
		}
		throw new SelectionNodeFound(this.resolvedField);
	}

    @Override
    public StringBuffer print(int indent, StringBuffer output) {
		output.append("<SelectOnFieldAccessSpec:"); //$NON-NLS-1$
		super.print(indent, output);
		return output.append(">"); //$NON-NLS-1$
    }
}
