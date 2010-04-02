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
 * $Id: BaseClassTagsAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

/**
 * MIGRATION_STATE: complete.
 *
 * Represents the "BaseClassTags" attribute.
 * Bytecode attribute for the mappings of base classes to base class tags
 * of one Team.
 *
 * Location:
 * A team class with bound role classes. Every base class bound to a role class in this team gets a unique value attached.
 *
 * Content:
 * A list of pairs of base class name + tag.
 *
 * Purpose:
 * The lift methods use this tag to differentiate the dynamic type of the base object.
 * The OTRE generates team specific base class tags fields into every base class which is adapted by a team.
 * This tag fields are initialized by the tags of this attribute.
 *
 *
 * @author stephan
 * @version $Id: BaseClassTagsAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class BaseClassTagsAttribute extends ListValueAttribute {

    // names of base classes:
    private char[][] _names;
    // tags of above base classes:
    private int[]    _tags;

    /**
     * Create a BaseClassTagsAttribute ready to hold `count' elements.
     */
    public BaseClassTagsAttribute(int count) {
        super(BASE_CLASS_TAGS, count, 4);
        this._names = new char[count][];
        this._tags  = new int[count];
    }

	/**
     * Add a single mapping.
     * @param baseName name of a base class.
     * @param baseTag tag of that class
     * @param idx index into this list,
     * 		either within the range defined in the ctor,
     * 		or -1 (meaning: grow the arrays).
     *
     */
    public void setBaseTag(char[] baseName, int baseTag, int idx)
    {
    	for (char[] name : this._names) {
			if (CharOperation.equals(baseName, name))
				return; // already present
		}
    	if (idx == -1) {
    		int oldCount = this._count;
    		idx = this._count++;
    		System.arraycopy(this._names, 0, this._names = new char[this._count][], 0, oldCount);
    		System.arraycopy(this._tags,  0, this._tags  = new int[this._count],    0, oldCount);
    	}
        this._names[idx] = baseName;
        this._tags [idx] = baseTag;
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#writeElementValue(int)
     */
    protected void writeElementValue(int i) {
        writeName         (this._names[i]);
        writeUnsignedShort(this._tags[i]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
     */
    public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
        // nothing to do: this attribute is not read from class files.
    	// Note, that base tags need to be unique only per exact team,
    	//       because they are only used in liftMethods
    	//       which are generated exactly for each team (with bound roles).
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#toString(int)
     */
    protected String toString(int i) {
        return new String(this._names[i])+"->"+this._tags[i]; //$NON-NLS-1$
    }
}

