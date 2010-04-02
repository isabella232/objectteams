/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ProblemMethodBinding.java 23405 2010-02-03 17:02:18Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.AnchorMapping;

/**
 * OTDT change:
 *
 * What: If a closest match is given, replace it with instantiated copy.
 */
public class ProblemMethodBinding extends MethodBinding {

	private int problemReason;
	public MethodBinding closestMatch; // TODO (philippe) should rename into #alternateMatch

public ProblemMethodBinding(char[] selector, TypeBinding[] args, int problemReason) {
	this.selector = selector;
	this.parameters = (args == null || args.length == 0) ? Binding.NO_PARAMETERS : args;
	this.problemReason = problemReason;
	this.thrownExceptions = Binding.NO_EXCEPTIONS;
}
public ProblemMethodBinding(char[] selector, TypeBinding[] args, ReferenceBinding declaringClass, int problemReason) {
	this.selector = selector;
	this.parameters = (args == null || args.length == 0) ? Binding.NO_PARAMETERS : args;
	this.declaringClass = declaringClass;
	this.problemReason = problemReason;
	this.thrownExceptions = Binding.NO_EXCEPTIONS;
}
public ProblemMethodBinding(MethodBinding closestMatch, char[] selector, TypeBinding[] args, int problemReason) {
	this(selector, args, problemReason);
	this.closestMatch = closestMatch;
//{ObjectTeams:
	if (closestMatch != null) {
		// check for need to instantiate parameters;
		TypeBinding[] newParameters = AnchorMapping.instantiateParameters(null, closestMatch.parameters, closestMatch);
		// if instantiating was actually performed, we need a clone to hold the new parameters:
		if (newParameters != closestMatch.parameters) {
			// clone 'closestMatch' depending on its dynamic type:
			MethodBinding clone = getMethodClone(closestMatch);
			if (clone != null) {
				clone.parameters = newParameters;
				this.closestMatch = clone;
			}
		}
	}
// SH}
	if (closestMatch != null && problemReason != ProblemReasons.Ambiguous) this.declaringClass = closestMatch.declaringClass;
}

//{ObjectTeams: Clone closestMatch before manipulation.
private MethodBinding getMethodClone(MethodBinding closestMatch) {
	if(closestMatch instanceof ParameterizedGenericMethodBinding) {
		ParameterizedGenericMethodBinding pgmb = (ParameterizedGenericMethodBinding)closestMatch;
		MethodBinding clone = new ParameterizedGenericMethodBinding(closestMatch, pgmb.typeArguments, pgmb.environment());
		clone.declaringClass = closestMatch.declaringClass;
		return clone;
	} else if(closestMatch instanceof ParameterizedMethodBinding) {
		if (closestMatch.declaringClass instanceof ParameterizedTypeBinding)
			return new ParameterizedMethodBinding((ParameterizedTypeBinding)closestMatch.declaringClass, closestMatch);
		else
			return null;
	} else {
		return new MethodBinding(closestMatch, closestMatch.declaringClass);
	}
}
// SH}

/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/

public final int problemId() {
	return this.problemReason;
}
}
