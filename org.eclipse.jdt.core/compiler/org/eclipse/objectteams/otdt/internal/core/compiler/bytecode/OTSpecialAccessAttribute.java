/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006, 2015 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions.WeavingScheme;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
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
	// OTREDyn has one more field in the attribute, use disjoint kinds:
	private static final int DYN_DECAPSULATION_METHOD_ACCESS= 4;
	private static final int DYN_CALLOUT_FIELD_ACCESS = 5;
	private static final int DYN_SUPER_METHOD_ACCESS = 6;

	/**
	 * For OTREDyn, each attribute of this type maintains a set of locally unique (per team) access IDs.
	 * These IDs are later made unique per team-hierarchy by adding {@link #accessIdOffset}.
	 * This for of ids is then stored in the attribute.
	 * Stored ids are consumed and translated by OTREDyn to obtain those global IDs that uniquely identify the
	 * base feature within a generated _OT$access or _OT$accessStatic method.
	 * 
	 * AccessIds are generated during resolve and stored in these AST nodes:
	 * - MethodSpec / FieldAccessSpec
	 *   - From here it is directly picked up by CallinImplementorDyn to insert
	 *     the accessId as an argument for the generated _OT$access[Static] call.
	 * - MessageSend; accessId is preset for message sends implementing decapsulating BaseAllocationExpression
	 *   - regular base class:
	 *     - detected during AllocationExpression.resolveType, throws ConstructorDecapsulationException
	 *     - allocation is then replaced by a MessageSend to the _OT$access method
	 *   - base is role:
	 *     - generated AST has accessId = -1 to be updated during MessageSend.resolveType() if decaps needed
	 * The UpdatableAccessId representing the accessId in any of these generated ASTs / synthetic binding
	 * is updated after disambiguation from TeamModel.updateDecapsAccessIds().
	 */
	public int nextAccessId = 0;

	/** Updated by {@link TeamModel#updateDecapsAccessIds()} to denote the space of ids used by super teams. */
	public int accessIdOffset;

	/** Descriptor for a decapsulated base-method. */
	private class DecapsulatedMethodDesc {
		ReferenceBinding boundBaseclass;
		MethodBinding method;
		private int accessId;
		private boolean visibleInBaseclass;
		DecapsulatedMethodDesc(ReferenceBinding boundBaseclass, MethodBinding method, boolean isVisibleInBaseclass) {
			this.boundBaseclass = boundBaseclass;
			this.method = method;
			this.visibleInBaseclass = isVisibleInBaseclass;
			if (CopyInheritance.isCreator(method))
				// creator is declared in the enclosing team
				this.boundBaseclass = this.boundBaseclass.enclosingType();
			if (OTSpecialAccessAttribute.this._weavingScheme == WeavingScheme.OTDRE) {
				for (DecapsulatedMethodDesc methodDesc : OTSpecialAccessAttribute.this._decapsulatedMethods) {
					if (methodDesc.method == method) {
						this.accessId = methodDesc.accessId; // share the accessId from another callout to the same method
						return;
					}
				}
				this.accessId = OTSpecialAccessAttribute.this.nextAccessId++;
			}
		}

		void write() {
			if (OTSpecialAccessAttribute.this._weavingScheme == WeavingScheme.OTDRE)
				writeByte((byte)DYN_DECAPSULATION_METHOD_ACCESS);
			else
				writeByte((byte)DECAPSULATION_METHOD_ACCESS);
			ReferenceBinding declaringClass = this.method.declaringClass;
			if (this.method.isConstructor()) { // no accessor method, old style attribute
				writeName(declaringClass.attributeName());
				writeName(this.method.selector);
				writeName(this.method.signature());
			} else {
				// encode targetClass!selector(static) or targetClass?selector (virtual):
				char sep = this.method.isStatic() ? '!' : '?';
				if (this.visibleInBaseclass && OTSpecialAccessAttribute.this._weavingScheme == WeavingScheme.OTDRE)
					declaringClass = this.boundBaseclass; // avoid scattering the dispatch code over the base hierarchy
				char[] encodedName = CharOperation.concatWith(
													new char[][] {
														declaringClass.attributeName(),
														this.method.selector
													}, sep);
				char[] weaveIntoClasses = this.boundBaseclass.attributeName();
				if (!this.visibleInBaseclass && OTSpecialAccessAttribute.this._weavingScheme == WeavingScheme.OTDRE) {
					// for OTDRE pass all classes to weave from boundBaseclass up to the actual declaring class (:-separated)
					ReferenceBinding someClass = this.boundBaseclass.getRealClass();
					if (someClass != null && TypeBinding.notEquals(someClass, declaringClass)) {
						while ((someClass = someClass.superclass()) != null) {
							weaveIntoClasses = CharOperation.concat(weaveIntoClasses, someClass.attributeName(), ':');
							if (TypeBinding.equalsEquals(someClass, declaringClass))
								break;
						}
					}
				}
				writeName(weaveIntoClasses);
				writeName(encodedName);
				writeName(this.method.signature());
			}
			if (OTSpecialAccessAttribute.this._weavingScheme == WeavingScheme.OTDRE)
				writeUnsignedShort(this.accessId + OTSpecialAccessAttribute.this.accessIdOffset);
		}

		public String toString() {
			return new String(this.method.readableName());
		}
	}
	List<DecapsulatedMethodDesc> _decapsulatedMethods = new ArrayList<DecapsulatedMethodDesc>();

	/** Descriptor for a callout-bound base field. */
	public class CalloutToFieldDesc {
		// flags:
		private static final int CALLOUT_GET_FIELD = 0; // (== !CALLOUT_SET_FIELD)
		private static final int CALLOUT_SET_FIELD = 1;
		private static final int CALLOUT_STATIC_FIELD = 2;

		FieldBinding field;
		ReferenceBinding targetClass;
		int flags; // use the above constants
		int accessId;
		CalloutToFieldDesc cpInheritanceSrc;
		CalloutToFieldDesc(FieldBinding field, ReferenceBinding targetClass, int calloutModifier, CalloutToFieldDesc cpInheritanceSrc)
		{
			this.field = field;
			this.targetClass = targetClass;
			this.flags = (calloutModifier == TerminalTokens.TokenNameget) ?
							CALLOUT_GET_FIELD : CALLOUT_SET_FIELD;
			if (field.isStatic())
				this.flags |= CALLOUT_STATIC_FIELD;
			if (OTSpecialAccessAttribute.this._weavingScheme == WeavingScheme.OTDRE) {
				if (cpInheritanceSrc == null) {
					for (CalloutToFieldDesc ctf : OTSpecialAccessAttribute.this._calloutToFields) {
						if (ctf.field == field) {
							this.accessId = ctf.accessId; // share the accessId from another callout to the same field
							return;
						}
					}
					this.accessId = OTSpecialAccessAttribute.this.nextAccessId++;
				} else {
					this.cpInheritanceSrc = cpInheritanceSrc;
				}
			}
		}

		public int calloutModifier() {
			return ((this.flags & CALLOUT_SET_FIELD) != 0) ?
					TerminalTokens.TokenNameset :
					TerminalTokens.TokenNameget;
		}

		void write() {
			if (OTSpecialAccessAttribute.this._weavingScheme == WeavingScheme.OTDRE) {
				writeByte((byte)DYN_CALLOUT_FIELD_ACCESS);
				writeUnsignedShort(getId());
			} else {
				writeByte((byte)CALLOUT_FIELD_ACCESS);
			}
			writeByte((byte)this.flags);
			writeName(this.targetClass.attributeName());
			writeName(this.field.name);
			writeName(this.field.type.signature());
		}

		private int getId() {
			if (this.cpInheritanceSrc != null)
				return this.cpInheritanceSrc.getId();
			return this.accessId + OTSpecialAccessAttribute.this.accessIdOffset;
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
	List<CalloutToFieldDesc> _calloutToFields = new ArrayList<CalloutToFieldDesc>();

	/** Descriptor for base.super.m() special method access. */
	public class SuperMethodDesc
	{
		MethodBinding method;
		public SuperMethodDesc(MethodBinding method) {
			this.method = method;
		}

		void write() {
			writeByte((byte)(OTSpecialAccessAttribute.this._weavingScheme == WeavingScheme.OTDRE ? DYN_SUPER_METHOD_ACCESS : SUPER_METHOD_ACCESS));
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

	WeavingScheme _weavingScheme;

	public OTSpecialAccessAttribute (ReferenceBinding site, WeavingScheme weavingScheme) {
		super(IOTConstants.OTSPECIAL_ACCESS);
		this._site = site;
		this._weavingScheme = weavingScheme;
	}

	public int addDecapsulatedMethodAccess(ReferenceBinding boundBaseclass, MethodBinding method, boolean isVisibleInBaseclass) {
		DecapsulatedMethodDesc desc = new DecapsulatedMethodDesc(boundBaseclass, method, isVisibleInBaseclass);
		this._decapsulatedMethods.add(desc);
		return desc.accessId;
	}

	public int addCalloutFieldAccess(FieldBinding field, ReferenceBinding targetClass, int calloutModifier, CalloutToFieldDesc cpInheritanceSrc) {
		CalloutToFieldDesc calloutToFieldDesc = new CalloutToFieldDesc(field, targetClass, calloutModifier, cpInheritanceSrc);
		this._calloutToFields.add(calloutToFieldDesc);
		return calloutToFieldDesc.accessId;
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
        int M_SIZE = this._weavingScheme == WeavingScheme.OTDRE ? 8 : 6;
        int F_SIZE = this._weavingScheme == WeavingScheme.OTDRE ? 9 : 7;
		attributeSize += this._decapsulatedMethods.size() * (1+M_SIZE); // 1 byte kind, 3 names (+1 short for otredyn)
		attributeSize += this._calloutToFields.size() * (1+F_SIZE);		// 1 byte kind, 1 byte flags, 3 names (+1 byte for otredyn) 
		attributeSize += this._superMethods.size() * 9;        			// 1 byte kind, 4 names
		attributeSize += this._adaptedBaseclasses.size() * 3;  			// 1 name + 1 byte flag

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
		case DYN_DECAPSULATION_METHOD_ACCESS:
			this._readOffset += 2; // extra id
				//$FALL-THROUGH$
		case DECAPSULATION_METHOD_ACCESS:
			this._readOffset += 6;
			break;
		case DYN_CALLOUT_FIELD_ACCESS:
			this._readOffset += 2; // extra id
				//$FALL-THROUGH$
		case CALLOUT_FIELD_ACCESS:
			this._readOffset += 7;
			break;
		case DYN_SUPER_METHOD_ACCESS:
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
					if (newBase.isRoleType()) {
						if (newBase instanceof WeakenedTypeBinding)
							newBase = ((WeakenedTypeBinding)newBase).getStrongType();
						if (((RoleTypeBinding)newBase)._teamAnchor.isBaseAnchor()) {
							FieldBinding newField = newBase.getRealClass().getField(fieldDesc.field.name, false);
							role.addAccessedBaseField(newField, fieldDesc.calloutModifier(), fieldDesc);
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
