/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: VariableBinding.java 23405 2010-02-03 17:02:18Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.TeamAnchor;

/**
 * OTDT changes:
 *
 * What: Management of paths of variable bindings used as team anchor
 *       for an externalized role.
 * How:  Implemented in intermediate superclass TeamAnchor.
 *
 * What: differentiate pretty-printed and raw name of generated variables (_OT$)
 * How:  methods readableName(), internalName()
 *
 * @version $Id: VariableBinding.java 23405 2010-02-03 17:02:18Z stephan $
 */
public abstract class VariableBinding
//{ObjectTeams: pulled up additions to new intermediate superclass:
// orig: extends Binding
		extends TeamAnchor {
// SH}

	public int modifiers;
//{ObjectTeams: pulled up to TeamAnchor:
	//public TypeBinding type;
// SH}
	public char[] name;
	protected Constant constant;
	public int id; // for flow-analysis (position in flowInfo bit vector)
	public long tagBits;

	public VariableBinding(char[] name, TypeBinding type, int modifiers, Constant constant) {
		this.name = name;
		this.type = type;
		this.modifiers = modifiers;
		this.constant = constant;
		if (type != null) {
			this.tagBits |= (type.tagBits & TagBits.HasMissingType);
		}
	}

	public Constant constant() {
		return this.constant;
	}

	public abstract AnnotationBinding[] getAnnotations();

	public final boolean isBlankFinal(){
		return (this.modifiers & ExtraCompilerModifiers.AccBlankFinal) != 0;
	}
	/* Answer true if the receiver is final and cannot be changed
	*/

	public final boolean isFinal() {
		return (this.modifiers & ClassFileConstants.AccFinal) != 0;
	}
	public char[] readableName() {
//{ObjectTeams: pretty printing for generated names:
	if (CharOperation.prefixEquals(IOTConstants.OT_DOLLAR_NAME, this.name))
		return CharOperation.subarray(this.name, IOTConstants.OT_DOLLAR_LEN, -1);
// SH}
		return this.name;
	}
//{ObjectTeams: un-pretty-printed (original) version:
public char[] internalName() {
		return this.name;
	}
// SH}
	public void setConstant(Constant constant) {
		this.constant = constant;
	}
	public String toString() {
		StringBuffer output = new StringBuffer(10);
		ASTNode.printModifiers(this.modifiers, output);
		if ((this.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
			output.append("[unresolved] "); //$NON-NLS-1$
		}
		output.append(this.type != null ? this.type.debugName() : "<no type>"); //$NON-NLS-1$
		output.append(" "); //$NON-NLS-1$
		output.append((this.name != null) ? new String(this.name) : "<no name>"); //$NON-NLS-1$
		return output.toString();
	}
}
