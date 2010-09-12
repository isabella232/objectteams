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
 * $Id: CallinPrecedenceAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.PrecedenceBinding;

/**
 * MIGRATION_STATE: complete.
 *
 * TODO Description of this attribute
 *
 * @author stephan
 * @version $Id: CallinPrecedenceAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class CallinPrecedenceAttribute extends ListValueAttribute {

	private char[][] callinNames;

	public CallinPrecedenceAttribute(ReferenceBinding site, CallinCalloutBinding[] callins)
	{
		super(IOTConstants.CALLIN_PRECEDENCE, callins.length, 2); // each element is a name reference
		int count = 0;
		for (int i = 0; i < callins.length; i++) {
			Binding callinBinding= callins[i];
			if (callinBinding != null && callinBinding.isValidBinding())
				count++;
		}
		this.callinNames = new char[count][];
		this._count = count;
		count = 0;
		for (int i = 0; i < callins.length; i++) {
			CallinCalloutBinding callinBinding = callins[i];
			if (callinBinding != null && callinBinding.isValidBinding())
				this.callinNames[count++] = getQualifiedName(site, callinBinding);
		}
	}

	/**
	 * Prepend type path to callin when looking from site.
	 * @param site
	 * @param callinBinding
	 * @return a '.' separated path
	 */
	private char[] getQualifiedName(ReferenceBinding site, CallinCalloutBinding callinBinding) {
		String name = new String(callinBinding.name);
		ReferenceBinding current = callinBinding._declaringRoleClass;
		while (current != null && current != site) {
			name = new String(current.sourceName())+'.'+name;
			current = current.enclosingType();
		}
		return name.toCharArray();
	}

	/**
     * Create an attribute from byte code.
     *
     * (Invoked from AbstractAttribute.readAttribute(char[],MethodInfo,int,int,int[]))
	 */
	public CallinPrecedenceAttribute(ClassFileStruct reader, int readOffset, int[] constantPoolOffsets)
	{
        super(IOTConstants.CALLIN_PRECEDENCE, 0, 2);
        readList(reader, readOffset, 0 /* no structOffset */, constantPoolOffsets);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#writeElementValue(int)
	 */
	void writeElementValue(int i) {
		writeName(this.callinNames[i]);
	}

    void read(int i)
    {
        if (i==0)
            this.callinNames = new char[this._count][];
        this.callinNames[i] = consumeName();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
	 */
	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		checkBindingMismatch(binding, ClassFileConstants.AccTeam);
		if (((ReferenceBinding)binding).isTeam())
			((ReferenceBinding)binding).getTeamModel().addAttribute(this);
	}
	// Evaluate Precedence late, because we need our methods and mappings to be in place.
	public void evaluateLateAttribute(ReferenceBinding enclosingType, int state)
	{
		if (state != ITranslationStates.STATE_LATE_ATTRIBUTES_EVALUATED)
			return;
		CallinCalloutBinding[] mappings = new CallinCalloutBinding[this.callinNames.length];
		for (int i = 0; i < this.callinNames.length; i++) {
			char[][] parts = CharOperation.splitOn('.', this.callinNames[i]);
			int j = 0;
			ReferenceBinding currentType = enclosingType;
			while (j<parts.length-1) {
				currentType = currentType.getMemberType(
								CharOperation.concat(IOTConstants.OT_DELIM_NAME,parts[j++]));
			}
			if (currentType == null) // found in Dehla's error log.
				throw new InternalCompilerError("Can't resolve type for precedence declaration "+new String(this.callinNames[i])); //$NON-NLS-1$
			CallinCalloutBinding[] callinCallouts = currentType.callinCallouts;
			if (callinCallouts != null) {
				for (int k = 0; k < callinCallouts.length; k++) {
					if (   callinCallouts[k].type == CallinCalloutBinding.CALLIN
						&& CharOperation.equals(callinCallouts[k].name, parts[j]))
					{
						mappings[i] = callinCallouts[k];
						break;
					}
				}
			}
			if (mappings[i] == null)
				throw new InternalCompilerError("Precedence attribute has unresolved method mapping"); //$NON-NLS-1$
		}
		int len = enclosingType.precedences.length;
		if (len == 0)
			enclosingType.precedences = new PrecedenceBinding[1];
		else
			System.arraycopy(enclosingType.precedences, 0,
					         enclosingType.precedences = new PrecedenceBinding[len+1], 0,
							 len);
		enclosingType.precedences[len] = new PrecedenceBinding(enclosingType, mappings);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#toString(int)
	 */
	String toString(int i) {
		return new String(this.callinNames[i]);
	}

}
