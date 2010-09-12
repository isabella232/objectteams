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
 * $Id: StaticReplaceBindingsAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.ModelElement;

/**
 * MIGRATION_STATE: complete.
 *
 * Represents the "StaticReplaceBindings" attribute
 *
 * Location:
 * A team class containing a role with a replace callin binding for static methods.
 *
 * Content:
 * A list of method bindings consisting of:
 * 	    role class name
 * 		role method name
 * 		role method signature
 * 		A list of tuples:
 * 			+ base class name
 *          + base method name
 *          + base method signature
 *          + param mapping positions
 *          + translation-flags
 *
 * Purpose:
 * The OTRE uses this attribute to determine
 * 		a) TODO
 *
 *
 * The value of this attribute is a nested list, therefor everything
 * is handcoded here (similar to CallinMethodMappingsAttribute, but cannot reuse).
 *
 * @author stephan
 * @version $Id: StaticReplaceBindingsAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class StaticReplaceBindingsAttribute extends AbstractAttribute {


	/**
     * Local structure to store values independent of source/byte code.
     */
    private class Mapping {
    	char[] _roleClass, _roleSelector, _roleSignature, _liftMethodName, _liftMethodSignature;
    	BaseMethod[] _baseMethods;
    	Mapping(char[] roleClass, char[] selector, char[] signature, char[] liftMethodName, char[] liftMethodSignature)
    	{
    		this._roleClass     = roleClass;
    		this._roleSelector  = selector;
    		this._roleSignature = signature;
    		this._liftMethodName = liftMethodName;
    		this._liftMethodSignature = liftMethodSignature;
    	}

    	@SuppressWarnings("nls")
		public String toString() {
    		String result =
    			new String(this._roleClass)+"."+new String(this._roleSelector) + new String(this._roleSignature)+" <- ";
    		for (int i = 0; i < this._baseMethods.length; i++) {
				result += this._baseMethods[i].toString();
			}
    		if (this._liftMethodName.length > 0)
    			result += "\nlift by: "+new String(this._liftMethodName)+new String(this._liftMethodSignature);
    		return result;
    	}
    }
    /**
     * Local structure to store values when read from byte code.
     */
    private class BaseMethod {
    	static final int FLAG_CALLIN = 1;
    	static final int FLAG_ROLE_METHOD = 2;
    	static final int FLAG_STATIC = 4;

    	private static final int ATTRIBUTE_LEN = 13; // 3 names (1 class name, 1 selector, 1 signature) 1+4 bytes flags + 2 len
    	char[] _baseclass, _selector, _signature;
    	boolean _isCallin;
    	boolean _isRoleMethod;
    	boolean _isStatic;
    	int[] _positions;
    	int  _translationFlags;
    	BaseMethod (char[] baseclass, char[] sel, char[] sig,
    			    boolean isCallin, boolean isRoleMethod, boolean isStatic,
    			    int[] positions, int translations)
		{
    		this._baseclass = baseclass;
    		this._selector = sel;
    		this._signature = sig;
    		this._isCallin     = isCallin;
    		this._isRoleMethod = isRoleMethod;
    		this._isStatic     = isStatic;
    		this._positions = positions;
    		this._translationFlags = translations;
		}
		@SuppressWarnings("nls")
		public String toString() {
			String result = (this._isCallin ?     "callin " : "")+
							(this._isRoleMethod ? "role "   : "")+
							(this._isStatic ?     "static " : "")+
			                 new String(this._baseclass)+"."+new String(this._selector)+new String(this._signature);
			if (this._positions != null) {
				result += "(";
				for (int j = 0; j < this._positions.length; j++) {
					result += this._positions[j]+",";
				}
				result += ")";
			}
			return result;
		}

    }
    private CallinMappingDeclaration[] _mappingDeclarations;
    private Mapping[] _mappings; // this holds the data read from byte code
    private int _size;           // attribute size in bytes

    /**
     * @param decls the mapping declarations to encode and store in this attribute.
     */
    public StaticReplaceBindingsAttribute(CallinMappingDeclaration[] decls)
    {
        super(IOTConstants.STATIC_REPLACE_BINDINGS);

        // lengths/counts:
        this._size = 0;
        int len = decls.length;
        // subtract errors:
        for (int i = 0; i < decls.length; i++) {
			if (decls[i].ignoreFurtherInvestigation)
				len--;
		}
        this._mappingDeclarations = new CallinMappingDeclaration[len];
        int idx = 0;
        for (int i = 0; i < decls.length; i++) {
			CallinMappingDeclaration decl = decls[i];
			if (!decl.ignoreFurtherInvestigation)
				this._mappingDeclarations[idx++] = decl;
		}
    }

    /* Check whether and mapping declarations are pending to be translated to Mappings. */
    private void checkTranslateMappings() {
    	if (this._mappingDeclarations == null)
    		return; // nothing to translate
        int idx = 0;
    	if (this._mappings != null) {
    		// grow array:
    		int len1 = this._mappings.length;
    		int len2 = this._mappingDeclarations.length;
			System.arraycopy(
					this._mappings, 0,
					this._mappings = new Mapping[len1+len2], 0,
					len1);
			idx = len1;
    	} else {
	    	this._mappings = new Mapping[this._mappingDeclarations.length];
    	}
        for (int i = 0; i < this._mappingDeclarations.length; i++)
			this._mappings[idx++] = translateMapping(this._mappingDeclarations[i]);

        this._mappingDeclarations = null; // consumed
    }

    /* Translate one mapping declaration to a Mapping. Also increase _size accordingly. */
	private Mapping translateMapping(CallinMappingDeclaration decl) {
		ReferenceBinding roleClass = decl.scope.enclosingSourceType();
		ReferenceBinding baseClass = null;
		if (roleClass != null)
			baseClass = roleClass.baseclass();
		if (baseClass != null && RoleTypeBinding.isRoleType(baseClass))
			baseClass = ((RoleTypeBinding)baseClass).getRealClass();
		this._size += 10; // 5 names: 1 roleclass, 2 selectors, 2 signatures
		Mapping currentMapping = new Mapping(
									decl.roleMethodSpec.resolvedMethod.declaringClass.sourceName(),
									decl.roleMethodSpec.selector,
									decl.roleMethodSpec.signature(),
						            decl.liftMethod != null ?
							            	decl.liftMethod.selector : new char[0],
									decl.liftMethod != null ?
							            	decl.liftMethod.signature() : new char[0]);

		MethodSpec[] baseMethods = decl.baseMethodSpecs;
		currentMapping._baseMethods = new BaseMethod[baseMethods.length];
		this._size += 2; // 1 count
		for (int j = 0; j < baseMethods.length; j++) {
			MethodSpec bm = baseMethods[j];

			this._size += BaseMethod.ATTRIBUTE_LEN;
			if (decl.positions != null)
				this._size += 2 * decl.positions.length;

			currentMapping._baseMethods[j] = new BaseMethod(
					baseClass != null ? baseClass.attributeName() : new char[0],
					bm.selector,
					bm.resolvedMethod.signature(),
					bm.isCallin(),
					baseClass != null && baseClass.isRole(),
					bm.isStatic(),
					decl.positions,
					bm.getTranslationFlags());
		}
		return currentMapping;
	}

	/**
     * Read the attribute from byte code.
     *
	 * @param info
	 * @param readOffset
	 * @param structOffset
	 * @param constantPoolOffsets
	 */
	public StaticReplaceBindingsAttribute(ClassFileStruct reader, int readOffset, int[] constantPoolOffsets) {
		super(IOTConstants.STATIC_REPLACE_BINDINGS);
		this._reader = reader;
		this._readOffset = readOffset;
		this._constantPoolOffsets = constantPoolOffsets;

		this._size = 0;
		int numMappings = consumeShort();
		this._mappings = new Mapping[numMappings];
		for (int i=0; i<numMappings; i++)
			this._mappings[i] = readMapping();
	}

	/**
	 * Merge two attributes encoding method mappings from different roles of the same team.
	 */
	public void merge(ModelElement model, AbstractAttribute other)
	{
		assert other instanceof StaticReplaceBindingsAttribute;
		StaticReplaceBindingsAttribute otherSRBA = (StaticReplaceBindingsAttribute)other;

		if (otherSRBA._mappings != null) {
			// adding translated mappings
			if (this._mappings != null) {
				// merge
				int l = this._mappings.length;
				int lOther = otherSRBA._mappings.length;
				System.arraycopy(
						this._mappings, 0,
						this._mappings = new Mapping[l+lOther], 0,
						l);
				System.arraycopy(
						otherSRBA._mappings, 0,
						this._mappings, l,
						lOther);
			} else {
				// store
				int len = otherSRBA._mappings.length;
				System.arraycopy(
						otherSRBA._mappings, 0,
						this._mappings = new Mapping[len], 0, len);
			}
			this._size += otherSRBA._size;
		}
		if (otherSRBA._mappingDeclarations != null) {
			// adding mapping declarations
			if (this._mappingDeclarations != null) {
				// merge
				int l = this._mappingDeclarations.length;
				int lOther = otherSRBA._mappingDeclarations.length;
				System.arraycopy(
						this._mappingDeclarations, 0,
						this._mappingDeclarations = new CallinMappingDeclaration[l+lOther], 0,
						l);
				System.arraycopy(
						otherSRBA._mappingDeclarations, 0,
						this._mappingDeclarations, l,
						lOther);
			} else {
				// store
				int len = otherSRBA._mappingDeclarations.length;
				System.arraycopy(
						otherSRBA._mappingDeclarations, 0,
						this._mappingDeclarations = new CallinMappingDeclaration[len], 0, len);
			}
		}
		// not affecting ReferenceBindings since this attribute is redundant with CallinMethodMappingsAttribute
	}

	/**
	 * Read one method mapping
	 */
	private Mapping readMapping() {
		this._size += 10; // 5 names:
		Mapping map = new Mapping(consumeName(), consumeName(), consumeName(), consumeName(), consumeName());
		this._size += 2;
		int numBaseMethods = consumeShort();
		map._baseMethods = new BaseMethod[numBaseMethods];
		for (int i=0; i<numBaseMethods; i++) {
			map._baseMethods[i] = readBaseMethod();
		}
		return map;
	}

	/**
	 * Read data for one bound base method.
	 */
	private BaseMethod readBaseMethod() {
		this._size += BaseMethod.ATTRIBUTE_LEN;
		char[] className = consumeName();
		char[] methName  = consumeName();
		char[] signature = consumeName();
		int flags = consumeByte();
		boolean isCallin     = (flags & BaseMethod.FLAG_CALLIN) != 0;
		boolean isRoleMethod = (flags & BaseMethod.FLAG_ROLE_METHOD) != 0;
		boolean isStatic     = (flags & BaseMethod.FLAG_STATIC) != 0;
		int[] positions = null;
		int n = consumeShort();
		if (n > 0) {
			this._size += 2 * n;
			positions = new int[n];
			for (int i = 0; i < positions.length; i++) {
				positions[i] = consumeShort();
			}
		}
		int translationFlags = consumeInt();
		return new BaseMethod(className, methName, signature, isCallin, isRoleMethod, isStatic, positions, translationFlags);
	}

	/* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#write(org.eclipse.jdt.internal.compiler.ClassFile)
     */
    public void write(ClassFile classFile) {
		checkTranslateMappings();

        super.write(classFile);

       	writeValues(classFile);

       	writeBack(classFile);
    }

    /**
     * Write the contents from values stored in _mappings
	 * @param classFile
	 */
	private void writeValues(ClassFile classFile) {
        if (this._contentsOffset + 8 + this._size >= this._contents.length) { // 8: name(2) + size(4) + elementCount(2)
        	this._contents = classFile.getResizedContents(8 + this._size);
        }
        writeName         (this._name);
        writeInt          (this._size + 2);  // + elementCount
        writeUnsignedShort(this._mappings.length);
        for (int i=0; i<this._mappings.length; i++) {
            Mapping map = this._mappings[i];
            writeName(map._roleClass);
            writeName(map._roleSelector);
            writeName(map._roleSignature);
            writeName(map._liftMethodName);
            writeName(map._liftMethodSignature);
            writeBaseMethods(this._mappings[i]._baseMethods);
        }
	}


    private void writeBaseMethods(BaseMethod[] methods) {
    	writeUnsignedShort(methods.length);
    	for (int i = 0; i < methods.length; i++) {
    		writeName(methods[i]._baseclass);
			writeName(methods[i]._selector);
			writeName(methods[i]._signature);
			int flags = 0;
			if (methods[i]._isCallin)     flags |= BaseMethod.FLAG_CALLIN;
			if (methods[i]._isRoleMethod) flags |= BaseMethod.FLAG_ROLE_METHOD;
			if (methods[i]._isStatic)     flags |= BaseMethod.FLAG_STATIC;
			writeByte((byte)flags);
			int[] positions = methods[i]._positions;
			if (positions == null) {
				writeUnsignedShort(0);
			} else {
				writeUnsignedShort(positions.length);
				for (int j = 0; j < positions.length; j++) {
					writeUnsignedShort(positions[j]);
				}
			}
			writeInt(methods[i]._translationFlags);
		}
    }
	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
	 */
	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		checkBindingMismatch(binding, ClassFileConstants.AccTeam);
		((ReferenceBinding)binding).getTeamModel().addAttribute(this);
	}
	// Evaluate CallinMethodMappingAttribute late, because we need our methods to be in place.
    public void evaluateLateAttribute(ReferenceBinding roleBinding, int state)
    {
    	// currently nothing to evaluate, just keep the attribute in the team model.
    }

	// ==== public accessors for use by org.eclipse.jdt.internal.core.ClassFileInfo ====
	@SuppressWarnings("nls")
	public String toString() {
		String result = new String(this._name);
		if (this._mappings != null) {
			for (int i = 0; i < this._mappings.length; i++) {
				result += "\n"+this._mappings[i];
			}
		} else if (this._mappingDeclarations != null) {
			for (int i = 0; i < this._mappingDeclarations.length; i++) {
				result += "\n" + this._mappingDeclarations[i];
			}
		}
		return result;
	}
}
