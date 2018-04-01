/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ProblemReferenceBinding.java 23405 2010-02-03 17:02:18Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for bug 349326 - [1.7] new warning for missing try-with-resources
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.lang.reflect.Field;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;

/**
 * OTDT change:
 *
 * What: improve readableName
 * How:  if possible delegate to original rather then assembling locally.
 *
 * What: use this class for anchored types, too.
 */
@SuppressWarnings("rawtypes")
public class ProblemReferenceBinding extends ReferenceBinding {
	ReferenceBinding closestMatch;
	private int problemReason;

// NOTE: must only answer the subset of the name related to the problem

public ProblemReferenceBinding(char[][] compoundName, ReferenceBinding closestMatch, int problemReason) {
	this.compoundName = compoundName;
	this.closestMatch = closestMatch;
	this.problemReason = problemReason;
}

public TypeBinding clone(TypeBinding enclosingType) {
	throw new IllegalStateException(); // shouldn't get here.
}
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#closestMatch()
 */
public TypeBinding closestMatch() {
	return this.closestMatch;
}

//{ObjectTeams: special uses:
// for role types (otTagArg only serves to disambiguate invocations wrt ProblemReferenceBinding(char[][],ReferenceBinding,int))
public ProblemReferenceBinding(char[] name, ReferenceBinding closestMatch, int problemReason, boolean otTagArg) {
	this(new char[][] {name}, closestMatch, problemReason);
}
// for anchored types:
public ITeamAnchor anchor;
public ProblemReferenceBinding(ITeamAnchor anchor, char[] typeName, ReferenceBinding closestMatch, int problemReason) {
	this(new char[][] {typeName}, closestMatch, problemReason);
	this.anchor = anchor;
}
// SH}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#closestMatch()
 */
public ReferenceBinding closestReferenceMatch() {
	return this.closestMatch;
}

public boolean hasTypeBit(int bit) {
	if (this.closestMatch != null)
		return this.closestMatch.hasTypeBit(bit);
	return false;
}

/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/
public int problemId() {
	return this.problemReason;
}

public static String problemReasonString(int problemReason) {
	try {
		Class reasons = ProblemReasons.class;
		String simpleName = reasons.getName();
		int lastDot = simpleName.lastIndexOf('.');
		if (lastDot >= 0) {
			simpleName = simpleName.substring(lastDot+1);
		}
		Field[] fields = reasons.getFields();
		for (int i = 0, length = fields.length; i < length; i++) {
			Field field = fields[i];
			if (!field.getType().equals(int.class)) continue;
			if (field.getInt(reasons) == problemReason) {
				return simpleName + '.' + field.getName();
			}
		}
	} catch (IllegalAccessException e) {
		// do nothing
	}
	return "unknown"; //$NON-NLS-1$
}

public void setTypeAnnotations(AnnotationBinding[] annotations, boolean evalNullAnnotations) {
	return; // reject misguided attempts.
}
//{ObjectTeams: try to squeeze more info out of closestMatch:
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#readableName()
 */
public char[] readableName() {
	if (this.closestMatch != null && !(this.closestMatch instanceof MissingTypeBinding))
		return this.closestMatch.readableName();
	return super.readableName();
}
@Override
public boolean isCompatibleWith(TypeBinding otherType) {
	// EXPERIMENTAL: allow compatibility of problem types:
	if (this.closestMatch != null) {
		if (otherType instanceof ProblemReferenceBinding) {
			ReferenceBinding otherClosestMatch = ((ProblemReferenceBinding)otherType).closestMatch;
			if (otherClosestMatch != null)
				return this.closestMatch.isCompatibleWith(otherClosestMatch);
			return this.closestMatch.isCompatibleWith(otherType);
		}
	}
	return super.isCompatibleWith(otherType);
}
// SH}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#shortReadableName()
 */
public char[] shortReadableName() {
//{ObjectTeams: keep original behavior here:
/* orig:
	return readableName();
  :giro */
	return super.readableName();
// SH}
}

public char[] sourceName() {
	return this.compoundName.length == 0 ? null : this.compoundName[this.compoundName.length - 1]; // last segment of [java][util][Map$Entry]
}

public String toString() {
	StringBuffer buffer = new StringBuffer(10);
	buffer.append("ProblemType:[compoundName="); //$NON-NLS-1$
	buffer.append(this.compoundName == null ? "<null>" : new String(CharOperation.concatWith(this.compoundName,'.'))); //$NON-NLS-1$
	buffer.append("][problemID=").append(problemReasonString(this.problemReason)); //$NON-NLS-1$
	buffer.append("][closestMatch="); //$NON-NLS-1$
	buffer.append(this.closestMatch == null ? "<null>" : this.closestMatch.toString()); //$NON-NLS-1$
	buffer.append("]"); //$NON-NLS-1$
	return buffer.toString();
}
}
