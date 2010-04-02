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
 * $Id: WordValueAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.classfmt.FieldInfo;
import org.eclipse.jdt.internal.compiler.classfmt.MethodInfo;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;


/**
 * MIGRATION_STATE: complete.
 *
 * Bytecode attributes with exactly one (fixed-length) value (word=2bytes).
 * Currently handled:
 * <ul>
 * <li>OTClassFlags
 *
 * Location:
 * A team class or a role type
 *
 * Content:
 * An integer encoding the following flags:
 * OT_CLASS_TEAM            = 1
 * OT_CLASS_ROLE            = 2
 * OT_CLASS_ROLE_LOCAL      = 4
 * OT_CLASS_PURELY_COPIED   = 8
 *   means: no source present for this class
 * OT_CLASS_ROLE_FILE       = 16
 * OT_CLASS_FLAG_HAS_TSUPER = 32
 * OT_CLASS_CONFINED        = 64;
 *   means: superclass Object should be updated to __OT__Confined on loading
 *
 * <li>Modifiers
 * <li>RoleClassMethodModifiers
 * <li>OTCompilerVersion
 * <li>CallinFlags
 * Represents the "CallinFlags" attribute.
 *
 * Location:
 * A role method or a callin wrapper or a base call surrogate (for return type)
 *
 * Content:
 * An integer encoding the following flags:
 * - CALLIN_FLAG_OVERRIDING 					= 1;
 * - CALLIN_FLAG_WRAPPER    					= 2;
 * - CALLIN_FLAG_DEFINITELY_MISSING_BASECALL 	= 8;
 * - CALLIN_FLAG_POTENTIALLY_MISSING_BASECALL 	= 16;
 * - CALLIN_FLAG_BASE_SUPER_CALL              	= 32;
 * - bits 9-12 (CALLIN_RETURN_MASK)
 *
 * Purpose:
 * Used by OTRE:
 * - CALLIN_FLAG_OVERRIDING: if set, this method is overriding an inherited version.
 * 		The OTRE uses this information to realize the inheritance of method bindings.
 * 		(OTRE internal: This flag prevents the OTRE from generating an empty base-call-surrogate
 *  	which would wrongly override an inherited (non-empty) version.)
 * - CALLIN_FLAG_WRAPPER: if set, this method is the generated team-level callin wrapper.
 * Only used by the compiler:
 * - CALLIN_FLAG_DEFINITELY_MISSING_BASECALL:	For base call flow analysis including super calls
 * - CALLIN_FLAG_POTENTIALLY_MISSING_BASECALL: 	For base call flow analysis including super calls
 * - CALLIN_FLAG_BASE_SUPER_CALL: 				Is the base call targeting the base's super? 
 * 		OTRE part is handled via OTSpecialAccess attribute (kind SUPER_METHOD_ACCESS).
 * - bits 9-12 (CALLIN_RETURN_MASK):			Encoding of original non-reference return type.
 *
 * </ul>
 * @author stephan
 * @version $Id: WordValueAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class WordValueAttribute
        extends AbstractAttribute
{
    // ============== STATIC API ===================

	public static void maybeCreateClassFlagsAttribute(TypeDeclaration type) {
		TypeModel model = null;
		int classFlags = 0;
		if (type.binding.isDirectRole()) {
			model = type.getRoleModel();
			classFlags = IOTConstants.OT_CLASS_ROLE;
		} else if (type.binding.isRole()) {
			model = type.getRoleModel();
			classFlags = IOTConstants.OT_CLASS_ROLE_LOCAL;
		}
		if (type.isTeam()) {
			if (model == null)
				model = type.getTeamModel();
			classFlags |= IOTConstants.OT_CLASS_TEAM;
		}
		if (type.isPurelyCopied)
			classFlags |= IOTConstants.OT_CLASS_PURELY_COPIED;
		if (type.isRoleFile())
			classFlags |= IOTConstants.OT_CLASS_ROLE_FILE;
		if (   TypeAnalyzer.isTopConfined(type.binding)
			|| TypeAnalyzer.extendsOTConfined(type))
			classFlags |= IOTConstants.OT_CLASS_CONFINED;

		if (model != null)
			model.addAttribute(new WordValueAttribute(OT_CLASS_FLAGS, classFlags));
	}

	public static void addClassFlags(TypeModel classModel, int flags) {
		if (classModel._attributes != null) {
			for (AbstractAttribute attribute : classModel._attributes) {
				if (attribute.nameEquals(OT_CLASS_FLAGS)) {
					((WordValueAttribute)attribute)._value |= flags;
					return;
				}
			}
		}
		classModel.addAttribute(new WordValueAttribute(OT_CLASS_FLAGS, flags));
	}

	public static WordValueAttribute readClassFlags (
            ClassFileStruct reader,
            int             readOffset,
            int[]           constantPoolOffsets)
	{
        int  value = reader.u2At(readOffset);
        return new WordValueAttribute(reader, OT_CLASS_FLAGS, value);
	}

    public static AbstractAttribute readCompilerVersion(
			ClassFileReader reader,
			int 			readOffset,
			int[] 			constantPoolOffsets)
    {
        int  value = reader.u2At(readOffset);
        return new WordValueAttribute(reader, OT_COMPILER_VERSION, value);
	}

	/**
     * Create a new "Modifiers" attribute. This attribute stores modifier flags
     * for role interfaces in the bytecode that are used during compilation.
     * The actual interface methods must be reset to public and non-static in the bytecode,
     * but within the compiler protection is checked based upon the modifiers
     * that originally resulted from role-splitting.
     * @see <a href="http://trac.objectteams.org/ot/ticket/147">Trac #147</a>
     *
     * @param modifiers (only short portion is stored)
     * @return a new WordValueAttribute
     */
    public static WordValueAttribute modifiersAttribute(int modifiers)
    {
        return new WordValueAttribute(MODIFIERS_NAME, modifiers);
    }

    /**
     * Read and evaluate a "Modifiers" attribute from byte code.
     * @param method  this method shall be modified.
     * @param readOffset where to read
     */
    public static void readModifiers(
            MethodInfo      method,
            int             readOffset)
    {
        int  value = method.u2At(readOffset);
        method.setAccessFlags(value);
    }

    /**
	 * just like readModifiers(MethodInfo,int)
	 */
	public static void readModifiers(FieldInfo field, int readOffset) {
        int  value = field.u2At(readOffset);
		field.setAccessFlags(value);
	}
	/**
	 * This variant is used for methods of a role class. Similar to the Modifiers attribute,
	 * this attribute stores flags in the bytecode that have to be adjusted in order to please the JVM.
	 * @see <a href="http://trac.objectteams.org/ot/ticket/147">Trac #147</a>
	 */
    public static WordValueAttribute roleClassMethodModifiersAttribute(int modifiers)
    {
        return new WordValueAttribute(ROLECLASS_METHOD_MODIFIERS_NAME, modifiers);
    }

    public static void readRoleClassMethodModifiersAttribute(MethodInfo info, int readOffset) {
		int binaryFlags = info.getModifiers() & ~ExtraCompilerModifiers.AccVisibilityMASK; // reset these bits first
		int newFlags = info.u2At(readOffset);
    	info.setAccessFlags(binaryFlags | newFlags);
	}

    /**
     * Read and evaluate a "CallinFlags" attribute from byte code.
     * @param method  this method shall be modified.
     * @param readOffset where to read
     */
    public static WordValueAttribute readCallinFlags(
            MethodInfo      method,
            int             readOffset)
    {
        int  value = method.u2At(readOffset);
        method.setAccessFlags(method.getModifiers()|ExtraCompilerModifiers.AccCallin);
        // create and store an attribute anyway, in order to store the actual bits.
        WordValueAttribute result = callinFlagsAttribute(value);
        result._methodInfo = method;
        return result;
    }


    /**
     * Create a "CallinFlags" attribute which stores these bits for callin methods:
     * OVERRIDING, WRAPPER, {POTENTIALLY,DEFINITELY}_MISSING_BASECALL, BASE_SUPER_CALL,
     * plus the encoded return type (bits 9-12).
     */
    public static WordValueAttribute callinFlagsAttribute(int modifiers) {
        return new WordValueAttribute(CALLIN_FLAGS, modifiers);
    }

    public static WordValueAttribute compilerVersionAttribute()
    {
    	assert(OT_VERSION_MAJOR <= 7);
    	assert(OT_VERSION_MINOR <= 9);
    	assert(OT_REVISION <= 31);
    	return new WordValueAttribute(OT_COMPILER_VERSION,
    			  (OT_VERSION_MAJOR << 9)
				+ (OT_VERSION_MINOR << 5)
				+ (OT_REVISION));
    }

    // ============== INSTANCE FEATURES ===================


    public void addBits(int flags) {
    	this._value |= flags;
    }
    // the attribute value
    private int _value;


    /**
     * INTERNAL USE ONLY. Use static factory methods instead.
     * @param name
     * @param value
     */
    protected WordValueAttribute(char[] name, int value) {
        super(name);
        this._value = value;
    }
    protected WordValueAttribute(ClassFileStruct reader, char[] name, int value)
    {
    	this(name, value);
    	this._reader = reader;
    }

    int size() {
    	return 8;
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#write(org.eclipse.jdt.internal.compiler.ClassFile)
     */
    public void write(ClassFile classFile)
    {
    	super.write(classFile);
        if (this._contentsOffset + 8 > this._contents.length) {
        	this._contents = classFile.getResizedContents(8);
        }
        // write the name
        int attributeNameIndex = this._constantPool.literalIndex(this._name);
        this._contents[this._contentsOffset++] = (byte) (attributeNameIndex >> 8);
        this._contents[this._contentsOffset++] = (byte) attributeNameIndex;
        // The length of a word value attribute is 2 (fixed-length).
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 2;
        // write the value
        this._contents[this._contentsOffset++] = (byte) (this._value >> 8);
        this._contents[this._contentsOffset++] = (byte) this._value;

        writeBack(classFile);
    }

    /**
     * Evaluate class level attribute(s).
     */
    public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		if (CharOperation.equals(this._name, OT_CLASS_FLAGS))
        {
            checkBindingMismatch(binding, 0);
            BinaryTypeBinding type = (BinaryTypeBinding)binding;
            if ((this._value & OT_CLASS_ROLE) != 0)
            	type.modifiers |= ExtraCompilerModifiers.AccRole;
            if ((this._value & OT_CLASS_TEAM) != 0)
            	type.modifiers |= ClassFileConstants.AccTeam;
            if ((this._value & OT_CLASS_FLAG_HAS_TSUPER) != 0)
            	type.modifiers |= ExtraCompilerModifiers.AccOverriding;
            if ((this._value & OT_CLASS_ROLE_LOCAL) != 0)
            	type.setIsRoleLocal();
            if ((this._value & (OT_CLASS_ROLE_FILE|OT_CLASS_PURELY_COPIED)) != 0)
            	type.roleModel.setExtraRoleFlags(this._value & (OT_CLASS_ROLE_FILE|OT_CLASS_PURELY_COPIED));
            if ((this._value & OT_CLASS_CONFINED) != 0) {
           		boolean wasMissingType = false;
        		if (missingTypeNames != null) {
        			for (int i = 0, max = missingTypeNames.length; i < max; i++) {
        				if (CharOperation.equals(IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED, missingTypeNames[i])) {
        					wasMissingType = true;
        					break;
        				}
        			}
        		}
            	ReferenceBinding superclass= environment.getTypeFromCompoundName(IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED, false, wasMissingType);
            	if (superclass instanceof UnresolvedReferenceBinding)
            		superclass= ((UnresolvedReferenceBinding)superclass).resolve(environment, false);
            	type.resetSuperclass(superclass);
            }
            	// TODO (SH): might also need to compute type.enclosingType from its name.
            // TODO(SH): not yet evaluated: purely-copied, rolefile. Do we need to?
        } else if (CharOperation.equals(this._name, IOTConstants.OT_COMPILER_VERSION)) {
        	checkBindingMismatch(binding, 0);
            BinaryTypeBinding type = (BinaryTypeBinding)binding;
        	if (type.isRole())
        		type.roleModel._compilerVersion = this._value;
        	if (type.isTeam())
        		type.getTeamModel()._compilerVersion = this._value;
        	if (this._value < IOTConstants.OT_COMPILER_VERSION_MIN)
        		environment.problemReporter.incompatibleOTJByteCodeVersion(((BinaryTypeBinding)binding).getFileName(), getBytecodeVersionString(this._value));
        }
    }


    /** can only transfer some flags once we have the method binding */
    public boolean evaluate(MethodInfo info, MethodBinding method, LookupEnvironment environment) {
    	if (this._methodInfo != info) return false;
    	// MODIFIERS and ROLECLASS_METHOD_MODIFIERS_NAME and CALLS_BASE_CTOR are already evaluated at the MethodInfo level.
    	if (CharOperation.equals(this._name, IOTConstants.CALLIN_FLAGS)) {
	        MethodModel.getModel(method).callinFlags = this._value & 0xFF;
	        int typeCode = this._value & IOTConstants.CALLIN_RETURN_MASK;
	    	if (typeCode != 0) {
	    		TypeBinding returnType;
	    		switch (typeCode) {
	    		case IOTConstants.CALLIN_RETURN_VOID: returnType = TypeBinding.VOID; break;
	    		case IOTConstants.CALLIN_RETURN_BOOLEAN: returnType = TypeBinding.BOOLEAN; break;
	    		case IOTConstants.CALLIN_RETURN_BYTE: returnType = TypeBinding.BYTE; break;
	    		case IOTConstants.CALLIN_RETURN_CHAR: returnType = TypeBinding.CHAR; break;
	    		case IOTConstants.CALLIN_RETURN_SHORT: returnType = TypeBinding.SHORT; break;
	    		case IOTConstants.CALLIN_RETURN_DOUBLE: returnType = TypeBinding.DOUBLE; break;
	    		case IOTConstants.CALLIN_RETURN_FLOAT: returnType = TypeBinding.FLOAT; break;
	    		case IOTConstants.CALLIN_RETURN_INT: returnType = TypeBinding.INT; break;
	    		case IOTConstants.CALLIN_RETURN_LONG: returnType = TypeBinding.LONG; break;
	    		default:
	    			throw new InternalCompilerError("Unexpected callin return type code "+typeCode); //$NON-NLS-1$
	    		}
	    		MethodModel.saveReturnType(method, returnType);
	    	}
	        return true;
    	}
        return false; // not handled, keep it.
    }

    @SuppressWarnings("nls")
	public String toString()
    {
        return "OT-Attribute "+
            new String(this._name)+": "+this._value;
    }

	public int getValue() {
		return this._value;
	}

	@SuppressWarnings("nls")
	public static String getBytecodeVersionString(int compilerVersion) {
		String version;
		if (compilerVersion > 0)
			version =   String.valueOf(compilerVersion >>> 9)
				+ "." + String.valueOf((compilerVersion >>> 5) & 0x0f)
				+ "." + String.valueOf(compilerVersion & 0x1f);
		else
			version = "(undefined)";
		return version;
	}

}
