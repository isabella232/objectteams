/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;

public class AccessRule {

	public static final int IgnoreIfBetter = 0x02000000; // value must be greater than IProblem#ForbiddenReference and DiscouragedReference

	public final char[] pattern;
//{ObjectTeams: needlessly final in jdt since https://bugs.eclipse.org/bugs/show_bug.cgi?id=571159
	public int problemId;
// SH}

//{ObjectTeams: Field for data flow from PDEAdaptor to ...
	public Object[] aspectBindingData;
// SH}

	public AccessRule(char[] pattern, int problemId) {
		this(pattern, problemId, false);
	}

	public AccessRule(char[] pattern, int problemId, boolean keepLooking) {
		this.pattern = pattern;
		this.problemId = keepLooking ? problemId | IgnoreIfBetter : problemId;
	}

	@Override
	public int hashCode() {
		return this.problemId * 17 + CharOperation.hashCode(this.pattern);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AccessRule)) return false;
		AccessRule other = (AccessRule) obj;
//{ObjectTeams: filter AdaptedPluginAccess:
	  if (   !(this.problemId == IProblem.AdaptedPluginAccess && other.problemId == 0)
		  && !(other.problemId == IProblem.AdaptedPluginAccess && this.problemId == 0))
// SH}
		if (this.problemId != other.problemId) return false;
		return CharOperation.equals(this.pattern, other.pattern);
	}

	public int getProblemId() {
		return this.problemId & ~IgnoreIfBetter;
	}

	public boolean ignoreIfBetter() {
		return (this.problemId & IgnoreIfBetter) != 0;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("pattern="); //$NON-NLS-1$
		buffer.append(this.pattern);
		switch (getProblemId()) {
			case IProblem.ForbiddenReference:
				buffer.append(" (NON ACCESSIBLE"); //$NON-NLS-1$
				break;
			case IProblem.DiscouragedReference:
				buffer.append(" (DISCOURAGED"); //$NON-NLS-1$
				break;
//{ObjectTeams:
			case IProblem.AdaptedPluginAccess:
				buffer.append(" (ADAPTED"); //$NON-NLS-1$
				break;
// SH}
			default:
				buffer.append(" (ACCESSIBLE"); //$NON-NLS-1$
				break;
		}
		if (ignoreIfBetter())
			buffer.append(" | IGNORE IF BETTER"); //$NON-NLS-1$
		buffer.append(')');
		return buffer.toString();
	}
}
