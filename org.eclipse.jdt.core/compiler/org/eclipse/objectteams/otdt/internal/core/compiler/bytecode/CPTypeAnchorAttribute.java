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
 * $Id: CPTypeAnchorAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;

/**
 * MIGRATION_STATE: complete.
 *
 * List of Class_info entries in the constant pool, which represent an anchored type.
 * This attribute stores the anchor (encoded as a path, currently just readableName()).
 * Possibly the anchor is not even needed, just the fact, that the Class_info is an
 * externalized role type.
 *
 * That information is used by
 * - ConstantPoolObjectMapper to not map features used via an externalized role.
 *
 * Flow of information:
 * + Elements for this attribute are generated in CodeStream.writeTypeBinding(TypeBinding)
 * + A reference to the attribute is stored in TypeModel._typeAnchors (which triggers writing to .class file).
 * + ConstantPoolObjectReader.getType queries the type model for a matching anchor and possibly creates
 *   a RoleTypeBinding from this information
 * + ConstantPoolObjectMapper refuses to map any feature with a RoleTypeBinding as declaring class.
 *
 * @author stephan
 * @version $Id: CPTypeAnchorAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class CPTypeAnchorAttribute extends ListValueAttribute {

	private List<TypeAnchor> anchors = new LinkedList<TypeAnchor>();

	/** Elements in this attribute's list are pairs of this type. */
	private class TypeAnchor {
		/** A descriptor of the anchor. Currently only a single readableName(). */
		char[] path;
		/** the constant pool entry, this anchor applies to. */
		int idx;
		TypeAnchor (int idx, char[] path) {
			this.path = path;
			this.idx = idx;
		}
	}


	/**
	 * Create an empty attribute; add elements with addAnchoredField.
	 */
	public CPTypeAnchorAttribute() {
		super(CLASS_INFO_ANCHORS, 0, 4); // count still unknown, element size is an index and a name
	}

	/**
	 * Create an attribute from .class file.
	 *
	 * @param reader
	 * @param readOffset
	 * @param constantPoolOffsets
	 */
	public CPTypeAnchorAttribute(
	        ClassFileStruct reader,
	        int             readOffset,
	        int[]           constantPoolOffsets)
	{
		super(IOTConstants.CLASS_INFO_ANCHORS, 0, 4); // count still unknown?
	    readList(reader, readOffset, 0 /* no structOffset */, constantPoolOffsets);
	}


	/** Record that a class is used externalized, ie., with a type anchor.
	 *
	 * @param bestName the type anchor
	 * @param cpIndex  constant pool index of a class
	 */
	public void addTypeAnchor(ITeamAnchor bestName, int cpIndex) {
		this.anchors.add(new TypeAnchor(cpIndex, bestName.internalName())); // TODO (SH) : path!
		this._count++;
	}

	void writeElementValue(int i) {
		TypeAnchor ta = this.anchors.get(i);
		writeUnsignedShort(ta.idx);
		writeName(ta.path);
	}
    void read(int i)
    {
    	int idx = consumeShort();
    	char[] name = consumeName();
    	this.anchors.add(new TypeAnchor(idx, name));
    }

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
	 */
	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		checkBindingMismatch(binding, 0);
		if (binding instanceof ReferenceBinding) {
			ReferenceBinding refBinding = (ReferenceBinding)binding;
			refBinding.model.setTypeAnchors(this);
		}
	}

	public char[] getPath(int cpIndex) {
		for (Iterator<TypeAnchor> anchorInfos = this.anchors.iterator(); anchorInfos.hasNext();) {
			TypeAnchor anchor = anchorInfos.next();
			if (anchor.idx == cpIndex)
				return anchor.path;
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#toString(int)
	 */
	String toString(int i) {
		TypeAnchor ta = this.anchors.get(i);
		return '['+ta.idx+']'+new String(ta.path);
	}
}
