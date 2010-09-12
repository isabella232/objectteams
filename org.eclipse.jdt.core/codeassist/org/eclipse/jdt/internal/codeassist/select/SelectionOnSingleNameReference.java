/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Selection node build by the parser in any case it was intending to
 * reduce a single name reference containing the assist identifier.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      [start]ba[end]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnName:ba>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.FieldAccessSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CalloutImplementor;

public class SelectionOnSingleNameReference extends SingleNameReference {
public SelectionOnSingleNameReference(char[] source, long pos) {
	super(source, pos);
}
public TypeBinding resolveType(BlockScope scope) {
	if (this.actualReceiverType != null) {
		this.binding = scope.getField(this.actualReceiverType, this.token, this);
		if (this.binding != null && this.binding.isValidBinding()) {
			throw new SelectionNodeFound(this.binding);
		}
	}
	// it can be a package, type, member type, local variable or field
	this.binding = scope.getBinding(this.token, Binding.VARIABLE | Binding.TYPE | Binding.PACKAGE, this, true /*resolve*/);
	if (!this.binding.isValidBinding()) {
		if (this.binding instanceof ProblemFieldBinding) {
			// tolerate some error cases
			if (this.binding.problemId() == ProblemReasons.NotVisible
					|| this.binding.problemId() == ProblemReasons.InheritedNameHidesEnclosingName
					|| this.binding.problemId() == ProblemReasons.NonStaticReferenceInConstructorInvocation
					|| this.binding.problemId() == ProblemReasons.NonStaticReferenceInStaticContext){
				throw new SelectionNodeFound(this.binding);
			}
			scope.problemReporter().invalidField(this, (FieldBinding) this.binding);
		} else if (this.binding instanceof ProblemReferenceBinding || this.binding instanceof MissingTypeBinding) {
			// tolerate some error cases
			if (this.binding.problemId() == ProblemReasons.NotVisible){
				throw new SelectionNodeFound(this.binding);
			}
			scope.problemReporter().invalidType(this, (TypeBinding) this.binding);
		} else {
// {ObjectTeams: inferred callout to field?
			CalloutMappingDeclaration callout = CalloutImplementor.inferCalloutAccess(scope, null, this, this.token, ((this.bits & IsStrictlyAssigned) != 0), this.expectedType);
			if (callout != null) {
				FieldBinding calloutTarget = ((FieldAccessSpec)callout.baseMethodSpec).resolvedField;
				if (calloutTarget != null)
					throw new SelectionNodeFound(calloutTarget);
			}
// SH}
			scope.problemReporter().unresolvableReference(this, this.binding);
		}
		throw new SelectionNodeFound();
	}

//{ObjectTeams: when resolved to a local variable within a callin/callout wrapper, re-resolve to something externally visible:
	this.binding = CallinCalloutScope.maybeReResolveReference(this, this.binding);
// SH}
	throw new SelectionNodeFound(this.binding);
}
//{ObjectTeams: callable from TypeAnchorReference:
@Override
public void resolveFinished() {
	if (this.token == IOTConstants._OT_BASE)
		throw new SelectionNodeFound(this.resolvedType); // no real field exists, use the type
	throw new SelectionNodeFound(this.binding); // other anchors have a real field
}
// SH}
public StringBuffer printExpression(int indent, StringBuffer output) {
	output.append("<SelectOnName:"); //$NON-NLS-1$
	return super.printExpression(0, output).append('>');
}
}
