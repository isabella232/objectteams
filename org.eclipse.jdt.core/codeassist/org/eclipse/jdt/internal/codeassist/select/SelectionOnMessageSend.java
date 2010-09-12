/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SelectionOnMessageSend.java 23404 2010-02-03 14:10:22Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Selection node build by the parser in any case it was intending to
 * reduce a message send containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      this.[start]bar[end](1, 2)
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnMessageSend:this.bar(1, 2)>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;

public class SelectionOnMessageSend extends MessageSend {

	/*
	 * Cannot answer default abstract match, iterate in superinterfaces of declaring class
	 * for a better match (default abstract match came from scope lookups).
	 */
	private MethodBinding findNonDefaultAbstractMethod(MethodBinding methodBinding) {

		ReferenceBinding[] itsInterfaces = methodBinding.declaringClass.superInterfaces();
		if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
			ReferenceBinding[] interfacesToVisit = itsInterfaces;
			int nextPosition = interfacesToVisit.length;

			for (int i = 0; i < nextPosition; i++) {
				ReferenceBinding currentType = interfacesToVisit[i];
				MethodBinding[] methods = currentType.getMethods(methodBinding.selector);
				if(methods != null) {
					for (int k = 0; k < methods.length; k++) {
						if(methodBinding.areParametersEqual(methods[k]))
							return methods[k];
					}
				}

				if ((itsInterfaces = currentType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (next == interfacesToVisit[b]) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
		}
		return methodBinding;
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		output.append("<SelectOnMessageSend:"); //$NON-NLS-1$
		if (!this.receiver.isImplicitThis()) this.receiver.printExpression(0, output).append('.');
		output.append(this.selector).append('(');
		if (this.arguments != null) {
			for (int i = 0; i < this.arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.arguments[i].printExpression(0, output);
			}
		}
		return output.append(")>"); //$NON-NLS-1$
	}

	public TypeBinding resolveType(BlockScope scope) {

		super.resolveType(scope);

		// tolerate some error cases
		if(this.binding == null ||
					!(this.binding.isValidBinding() ||
						this.binding.problemId() == ProblemReasons.NotVisible
						|| this.binding.problemId() == ProblemReasons.InheritedNameHidesEnclosingName
						|| this.binding.problemId() == ProblemReasons.NonStaticReferenceInConstructorInvocation
						|| this.binding.problemId() == ProblemReasons.NonStaticReferenceInStaticContext)) {
			throw new SelectionNodeFound();
		} else {
//{ObjectTeams: handle cases where receiver is a role

		    //handle copy inherited methods differently since the java model can't.
			if(this.binding.copyInheritanceSrc != null)
			{
				throw new SelectionNodeFound(this.binding.copyInheritanceSrc);
			}

			MethodModel model = this.binding.model;
			if (model != null && model._inferredCallout != null)
				throw new SelectionNodeFound(model._inferredCallout.baseMethodSpec.resolvedMethod);

			//method is part of a role but not copy inherited
		    if(this.binding.declaringClass.isRole())
		    {
		        //have to generate a new binding here! FIXME(SH): WHY?
		        ReferenceBinding roleClass = this.binding.declaringClass.roleModel.getClassPartBinding();
		        MethodBinding newBinding = // FIXME(SH): findExactMethod does not handle inherited methods!
		            scope.findExactMethod(roleClass, this.binding.selector, this.binding.parameters, this);
		        throw new SelectionNodeFound(newBinding);
		    }
 //haebor}
			if(this.binding.isDefaultAbstract()) {
				throw new SelectionNodeFound(findNonDefaultAbstractMethod(this.binding)); // 23594
			} else {
				throw new SelectionNodeFound(this.binding);
			}
		}
	}
}
