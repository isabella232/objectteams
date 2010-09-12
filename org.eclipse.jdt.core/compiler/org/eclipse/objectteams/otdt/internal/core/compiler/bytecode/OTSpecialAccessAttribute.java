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
 * $Id: $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.WeakenedTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;

/**
 * This class combines information about several situations where accessing
 * one element accross classes requires special treatment:
 * + decapsulation (base method accessed by role)              => OTRE removes protection
 * + callout-to-field (base field accessed by role)            => OTRE adds setter/getter
 * + base-class access -- two situations
 *     base-class decapsulation (role accesses invisible base-class) => OTRE removes protection
 *     super-base-class access (team adapts a super of a declared base-class)
 *
 * @author stephan
 */
public class OTSpecialAccessAttribute extends AbstractAttribute {
	// access kinds:
	private static final int DECAPSULATION_METHOD_ACCESS= 1;
	private static final int CALLOUT_FIELD_ACCESS = 2;
	private static final int SUPER_METHOD_ACCESS = 3;

	/** Descriptor for a decapsulated base-method. */
	private class DecapsulatedMethodDesc {
		ReferenceBinding boundBaseclass;
		MethodBinding method;
		DecapsulatedMethodDesc(ReferenceBinding boundBaseclass, MethodBinding method) {
			this.boundBaseclass = boundBaseclass;
			this.method = method;
			if (CopyInheritance.isCreator(method))
				// creator is declared in the enclosing team
				this.boundBaseclass = this.boundBaseclass.enclosingType();
		}

		void write() {
			writeByte((byte)DECAPSULATION_METHOD_ACCESS);
			if (this.method.isConstructor()) { // no accessor method, old style attribute
				writeName(this.method.declaringClass.attributeName());
				writeName(this.method.selector);
				writeName(this.method.signature());
			} else {
				// encode targetClass!selector(static) or targetClass?selector (virtual):
				char sep = this.method.isStatic() ? '!' : '?';
				char[] encodedName = CharOperation.concatWith(
													new char[][] {
														this.method.declaringClass.attributeName(),
														this.method.selector
													}, sep);
				writeName(this.boundBaseclass.attributeName()); // where to weave into
				writeName(encodedName);
				writeName(this.method.signature());
			}
		}

		public String toString() {
			return new String(this.method.readableName());
		}
	}
	private List<DecapsulatedMethodDesc> _decapsulatedMethods = new ArrayList<DecapsulatedMethodDesc>();

	/** Descriptor for a callout-bound base field. */
	private class CalloutToFieldDesc {
		// flags:
		private static final int CALLOUT_GET_FIELD = 0; // (== !CALLOUT_SET_FIELD)
		private static final int CALLOUT_SET_FIELD = 1;
		private static final int CALLOUT_STATIC_FIELD = 2;

		FieldBinding field;
		ReferenceBinding targetClass;
		int flags; // use the above constants
		CalloutToFieldDesc(FieldBinding field, ReferenceBinding targetClass, int calloutModifier)
		{
			this.field = field;
			this.targetClass = targetClass;
			this.flags = (calloutModifier == TerminalTokens.TokenNameget) ?
							CALLOUT_GET_FIELD : CALLOUT_SET_FIELD;
			if (field.isStatic())
				this.flags |= CALLOUT_STATIC_FIELD;

		}

		public int calloutModifier() {
			return ((this.flags & CALLOUT_SET_FIELD) != 0) ?
					TerminalTokens.TokenNameset :
					TerminalTokens.TokenNameget;
		}

		void write() {
			writeByte((byte)CALLOUT_FIELD_ACCESS);
			writeByte((byte)this.flags);
			writeName(this.targetClass.attributeName());
			writeName(this.field.name);
			writeName(this.field.type.signature());
		}

		@SuppressWarnings("nls")
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(this.field.readableName());
			if ((this.flags & CALLOUT_GET_FIELD) != 0)
				result.append(" get");
			else
				result.append(" set");
			if ((this.flags & CALLOUT_STATIC_FIELD) != 0)
				result.append(" (static)");
			return  result.toString();
		}
	}
	private List<CalloutToFieldDesc> _calloutToFields = new ArrayList<CalloutToFieldDesc>();

	/** Descriptor for base.super.m() special method access. */
	public class SuperMethodDesc
	{
		MethodBinding method;
		public SuperMethodDesc(MethodBinding method) {
			this.method = method;
		}

		void write() {
			writeByte((byte)SUPER_METHOD_ACCESS);
			writeName(this.method.declaringClass.attributeName());
			writeName(this.method.declaringClass.superclass().attributeName());
			writeName(this.method.selector);
			writeName(this.method.signature());
		}

		public String toString() {
			return "superaccess for "+new String(this.method.readableName()); //$NON-NLS-1$
		}


	}
	private List<SuperMethodDesc> _superMethods = new ArrayList<SuperMethodDesc>();

	private ReferenceBinding _site;

	// The following three lists are in sync (if non-null), ie., using same indices.
	/* All adapted base classes: */
	private List<ReferenceBinding> _adaptedBaseclasses = new ArrayList<ReferenceBinding>();
	/* Used only during reading, resolved types will be stored in _adaptedBaseclasses. */
	private List<char[]> _baseclassNames = null;
	/* TRUE means: use in a role referring to its decapsulated base class.
	 * FALSE means: use in a team referring to a super base class, which is indirectly adapted.*/
	private List<Boolean> _baseclassDecapsulation = new ArrayList<Boolean>();


	public OTSpecialAccessAttribute (ReferenceBinding site) {
		super(IOTConstants.OTSPECIAL_ACCESS);
		this._site = site;
	}

	public void addDecapsulatedMethodAccess(ReferenceBinding boundBaseclass, MethodBinding method) {
		this._decapsulatedMethods.add(new DecapsulatedMethodDesc(boundBaseclass, method));
	}

	public void addCalloutFieldAccess(FieldBinding field, ReferenceBinding targetClass, int calloutModifier) {
		this._calloutToFields.add(new CalloutToFieldDesc(field, targetClass, calloutModifier));
	}

	public void addSuperMethodAccess(MethodBinding method) {
		this._superMethods.add(new SuperMethodDesc(method));
	}

	public void addBaseClassDecapsulation(ReferenceBinding baseclass) {
		for (int i=0; i<this._adaptedBaseclasses.size(); i++)
			if (this._adaptedBaseclasses.get(i).equals(baseclass)) {
				if (!this._baseclassDecapsulation.get(i).booleanValue())
					this._baseclassDecapsulation.set(i, Boolean.TRUE);
				return; // already present
			}
		this._adaptedBaseclasses.add(baseclass);
		this._baseclassDecapsulation.add(Boolean.TRUE);
	}

	public void addAdaptedBaseClass(ReferenceBinding baseclass) {
		for (int i=0; i<this._adaptedBaseclasses.size(); i++)
			if (this._adaptedBaseclasses.get(i).equals(baseclass)) {
				return; // already present
			}
		this._adaptedBaseclasses.add(baseclass.getRealType());
		this._baseclassDecapsulation.add(Boolean.FALSE);
	}

	@Override
	public void write(ClassFile classFile) {
        super.write(classFile);

        int attributeSize  = 4; // initially empty, except for two counts
		attributeSize += this._decapsulatedMethods.size() * 7; // 1 byte kind, 3 names
		attributeSize += this._calloutToFields.size() * 8;     // 1 byte kind, 1 byte flags, 3 names
		attributeSize += this._superMethods.size() * 9;        // 1 byte kind, 4 names
		attributeSize += this._adaptedBaseclasses.size() * 3;  // 1 name + 1 byte flag

        if (this._contentsOffset + 6 + attributeSize >= this._contents.length)
        	this._contents = classFile.getResizedContents(6 + attributeSize);

        writeName         (this._name);
        writeInt          (attributeSize);

        writeUnsignedShort(  this._decapsulatedMethods.size()
        		           + this._calloutToFields.size()
        		           + this._superMethods.size());

        for (DecapsulatedMethodDesc method : this._decapsulatedMethods)
			method.write();
        for (CalloutToFieldDesc field : this._calloutToFields)
			field.write();
        for (SuperMethodDesc method : this._superMethods)
			method.write();

		// adapted baseclasses (direct or indirect decapsulation)
        int size = this._adaptedBaseclasses.size();
        writeUnsignedShort(size);
        for (int i=0; i<size; i++) {
        	writeName(this._adaptedBaseclasses.get(i).attributeName());
        	writeByte((byte)(this._baseclassDecapsulation.get(i).booleanValue()?1:0));
        }
        writeBack(classFile);
	}


    /**
     * Read the attribute from byte code.
     *
	 * @param reader
	 * @param readOffset
	 * @param constantPoolOffsets
	 */
	public OTSpecialAccessAttribute(ClassFileStruct reader, int readOffset, int[] constantPoolOffsets) {
		super(IOTConstants.OTSPECIAL_ACCESS);
		this._reader = reader;
		this._readOffset = readOffset;
		this._constantPoolOffsets = constantPoolOffsets;

		int count = consumeShort();
		for (int i=0; i<count; i++)
			readElement();

		count = consumeShort();
		this._baseclassNames = new ArrayList<char[]>(count);
		for (int i=0; i<count; i++) {
			this._baseclassNames.add(consumeName());
			this._baseclassDecapsulation.add(consumeByte()==1?Boolean.TRUE:Boolean.FALSE);
		}
	}

	private void readElement() {
		int kind = consumeByte();
		switch(kind) {
		case DECAPSULATION_METHOD_ACCESS:
			this._readOffset += 6;
			break;
		case CALLOUT_FIELD_ACCESS:
			this._readOffset += 7;
			break;
		case SUPER_METHOD_ACCESS:
			this._readOffset += 8;
			break;
		}

	}

	@Override
	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		checkBindingMismatch(binding, 0);
		this._site = (ReferenceBinding)binding;
		// don't see a need to evaluate all this:
//		for (TeamFieldDesc field : _teamFields)
//			field.evaluate(binding, environment);
		if (((ReferenceBinding)binding).isRole())
			((ReferenceBinding)binding).roleModel.setSpecialAccess(this);
		if (this._baseclassNames != null)
			for (int i=0; i<this._baseclassNames.size(); i++) {
				char[] name = this._baseclassNames.get(i);
				if (name != null && name.length > 0)
					if (this._baseclassDecapsulation.get(i).booleanValue())
						this._adaptedBaseclasses.add(environment.getTypeFromConstantPoolName(name, 0, -1, false, missingTypeNames));
			}
	}

	/**
	 * Add the field accesses of this attribute to the given sub team.
	 * @deprecated This method is not finished!
	 */
	@Deprecated()
	public void addFieldAccessesTo(TeamModel subTeam) {
		if (this._calloutToFields != null) {
			// FIXME(SH): need to evaluate _calloutToFields from byte code!
			for (CalloutToFieldDesc fieldDesc : this._calloutToFields) {
				ReferenceBinding oldBase = fieldDesc.field.declaringClass;
				for(RoleModel role : subTeam.getRoles(false)) {
					ReferenceBinding newBase = role.getBaseTypeBinding();
					if (newBase == null)
						continue;  // current role is not relevant (not bound)
					if (!CharOperation.equals(newBase.sourceName(), oldBase.sourceName()))
						continue;  // current role is bound to a different base
					if (RoleTypeBinding.isRoleType(newBase)) {
						if (newBase instanceof WeakenedTypeBinding)
							newBase = ((WeakenedTypeBinding)newBase).getStrongType();
						if (((RoleTypeBinding)newBase)._teamAnchor.isBaseAnchor()) {
							FieldBinding newField = newBase.getRealClass().getField(fieldDesc.field.name, false);
							role.addAccessedBaseField(newField, fieldDesc.calloutModifier());
						}
					}
				}
			}
		}

	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this._site.readableName());
		result.append(" requires special access to these elements:");
		for (DecapsulatedMethodDesc method : this._decapsulatedMethods) {
			result.append("\n\tmethod ");
			result.append(method.toString());
		}
		for (CalloutToFieldDesc field : this._calloutToFields) {
			result.append("\n\tfield ");
			result.append(field.toString());
		}
		for (SuperMethodDesc method : this._superMethods) {
			result.append("\n\t");
			result.append(method.toString());
		}
		for (ReferenceBinding baseclass : this._adaptedBaseclasses) {
			result.append("\n\tbase class ");
			result.append(baseclass.readableName());
		}
		return result.toString();
	}

}
