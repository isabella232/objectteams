/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BoundClassesHierarchyAttribute.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

/**
 * Represents the "BoundClassesHierarchy" attribute.
 * List of pairs (subtypename x supertypename).
 *
 * Location:
 * A team class containing bound roles.
 *
 * Content:
 * A list of pairs of sub type names and super type names.
 * Each class in this list is either a role class or bound base class of the current team.
 * Each sub class is a (indirect) sub type of the corresponding super type
 *
 * Purpose:
 * The OTRE uses this attribute for lookup of sub-type relations without needing
 * to ask the Repository.
 * The binding information (sub -> super) is stored and used for later transformations.
 *
 *
 * @author stephan
 * @version $Id: BoundClassesHierarchyAttribute.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class BoundClassesHierarchyAttribute extends ListValueAttribute {

	// storage during compilation (avoid duplicates):
    private HashMap<String, String> subToSuper = new HashMap<String, String>();
    // storage for writing to class file (support linear access by index):
    char[][] subNames;
    char[][] superNames;

    /**
     * Create an empty attribute for a bound classes hierarchy
     */
    public BoundClassesHierarchyAttribute() {
        super(IOTConstants.BOUND_CLASSES_HIERARCHY, 0, 4); // 2 names
    }

	/** Add a sub-super pair to this attribute. */
    public void add(char[] subName, char[] superName) {
        this.subToSuper.put(String.valueOf(subName), String.valueOf(superName));
    }

    @Override
    public boolean setupForWriting() {
    	this._count = this.subToSuper.size();
    	if (this._count > 0) {
    		this.subNames = new char[this._count][];
    		this.superNames = new char[this._count][];
    		int i = 0;
    		for(Entry<String, String> entry : this.subToSuper.entrySet()) {
    			this.subNames[i]     = entry.getKey().toCharArray();
    			this.superNames[i++] = entry.getValue().toCharArray();
    		}
    	}
    	return super.setupForWriting();
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#writeElementValue(int)
     */
    protected void writeElementValue(int i) {
        writeName(this.subNames[i]);
        writeName(this.superNames[i]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
     */
    public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
        // nothing, don't read from classfile
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#toString(int)
     */
    @SuppressWarnings("nls")
	protected String toString(int i) {
    	if (this.subNames != null)
    		return String.valueOf(this.subNames[i])+"->"+String.valueOf(this.superNames[i]);
        return "(pending)";
    }

}
