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
 * $Id: AbstractAttribute.java 23416 2010-02-03 19:59:31Z stephan $
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.classfmt.MethodInfo;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.ModelElement;

/**
 * MIGRATION_STATE: complete. 2 fixme(generic) remain.
 *
 * Abstraction over all OT-specific bytecode attributes.
 *
 * @author stephan
 * @version $Id: AbstractAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public abstract class AbstractAttribute
        implements IOTConstants
{

    /**
     * Attribute name as stored in the class file.
     */
    protected char[] _name;

    /** The byte code to write into. */
    protected byte[] _contents;

    /** The current offset into _contents. */
    protected int _contentsOffset;

    /** For lookup of strings etc. */
    protected ConstantPool _constantPool;

    // these are set while reading from class file:
    protected ClassFileStruct _reader;
    protected int             _readOffset;
    protected int             _structOffset;
    protected int[]           _constantPoolOffsets;

    // for method attributes, set during creation, checked during evaluate(..):
    protected MethodInfo _methodInfo;


    // debug
    protected final static boolean DEBUG = (System.getProperty("ot.otdt.debug") != null); //$NON-NLS-1$
    Throwable instantiationTime;
    /**
     * Create an attribute by the given name.
     */
    protected AbstractAttribute(char[] name) {
        this._name = name;
        if (DEBUG)
        	this.instantiationTime = new Throwable("Attribute was created in this stack"); //$NON-NLS-1$
    }

    public boolean nameEquals(char[] name) {
    	return CharOperation.equals(this._name, name);
    }
    public boolean nameEquals(AbstractAttribute other) {
    	return nameEquals(other._name);
    }

    public void merge (ModelElement model, AbstractAttribute other) {
    	throw new InternalCompilerError("Merge not supported for Attribute "+new String(this._name)); //$NON-NLS-1$
    }

    /**
     * API to setup this attribute for writing.
     *
     * @return whether this attribute contains relevant data.
     */
    public boolean setupForWriting() {
    	return true;
    }
    /**
     * API: Write this attribute to the class file.
     * Need to override in subclasses to do useful stuff.
     *
     * @param file class file structure to write to.
     */
    public void write(ClassFile classFile) {
        this._contents       = classFile.contents;
        this._contentsOffset = classFile.contentsOffset;
        this._constantPool   = classFile.constantPool;
    }

    /** Write the attribute into an allocated array of bytes. */
    public void generate(byte[] target, int offset, ConstantPool constantPool) {
    	ClassFile dummyClass = new ClassFile() {};
    	dummyClass.contents = target;
    	dummyClass.contentsOffset = offset;
    	dummyClass.constantPool = constantPool;
    	write(dummyClass);
    }
    int size() {
    	throw new InternalCompilerError("Generate not supported for Attribute "+new String(this._name)); //$NON-NLS-1$
    }
    /**
     * Evaluate the attribute and setup `binding' accordingly.
     * @param binding Must be a valid binding for the current attribute kind.
     * @param environment may be used to lookup types.
     * @param missingTypeNames TODO
     */
    public abstract void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames);

    /**
     * Checks whether binding is a class maching _reader and is of the expected kind (role/team).
     *
     * @param binding Must be a valid binding for the current attribute kind.
     * @param classKind 0, AccRole or AccTeam.
     */
    public void checkBindingMismatch(Binding binding, int classKind) {
    	Exception ex = null;
		if (   !(binding instanceof ReferenceBinding)
			|| !CharOperation.equals(((ReferenceBinding)binding).constantPoolName(),
									 ((ClassFileReader)this._reader).getName()))
		{
			ex = new Exception("evaluating attribute with mismatching binding"); //$NON-NLS-1$
		} else {
			String className = new String(((ReferenceBinding)binding).readableName());
			switch (classKind) {
			case ExtraCompilerModifiers.AccRole :
					if (!((ReferenceBinding)binding).isRole())
						ex = new Exception("Need a role for this attribute, found "+className); //$NON-NLS-1$
					break;
			case ClassFileConstants.AccTeam :
				if (!((ReferenceBinding)binding).isTeam())
					ex = new Exception("Need a team for this attribute, found "+className); //$NON-NLS-1$
				break;
			}
		}
		if (ex != null)
			Config.logException("Error reading byte code attribute", ex); //$NON-NLS-1$
		if (ex != null && this.instantiationTime != null)
			Config.logException("Instantiation time was", this.instantiationTime); //$NON-NLS-1$
    }

    private final void resizeByteArray() {
    	int length = this._contents.length;
    	int requiredSize = length + length;
/*    	copied from CodeStream, but omitting the following:
 		if (_contentsOffset > requiredSize) {
    		// must be sure to grow by enough
    		requiredSize = _contentsOffset + length;
    	}
*/
    	System.arraycopy(this._contents, 0, this._contents = new byte[requiredSize], 0, length);
    }

    /**
     * Write a name reference (via constant pool) into the code array.
     * @param name
     */
    public void writeName(char[] name) {
    	if (this._contentsOffset + 2 > this._contents.length) {
    		resizeByteArray();
    	}
        int valueIndex = this._constantPool.literalIndex(name);
        this._contents[this._contentsOffset++] = (byte) (valueIndex >> 8);
        this._contents[this._contentsOffset++] = (byte) valueIndex;
    }

	public void writeByte(byte b) {
    	if (this._contentsOffset + 1 > this._contents.length) {
    		resizeByteArray();
    	}
        this._contents[this._contentsOffset++] = b;
	}


    protected void writeUnsignedShort(int value) {
    	if (this._contentsOffset + 2 > this._contents.length) {
    		resizeByteArray();
    	}
        this._contents[this._contentsOffset++] = (byte) (value >>> 8);
        this._contents[this._contentsOffset++] = (byte) value;
    }

    protected void writeInt(int value) {
    	if (this._contentsOffset + 4 > this._contents.length) {
    		resizeByteArray();
    	}
        this._contents[this._contentsOffset++] = (byte) (value >> 24);
        this._contents[this._contentsOffset++] = (byte) (value >> 16);
        this._contents[this._contentsOffset++] = (byte) (value >> 8);
        this._contents[this._contentsOffset++] = (byte) value;
    }

	/**
	 * Store the new contents and offset to the classfile
	 * @param classFile
	 */
	protected void writeBack(ClassFile classFile) {
		classFile.contentsOffset = this._contentsOffset;
	    classFile.contents = this._contents; // in case it was resized.

	}

	/**
	 * Read a name from the constant pool and advance the _readOffset counter.
	 */
    protected char[] consumeName() {
    	int offset = this._reader.u2At(this._readOffset);
    	this._readOffset += 2;
		int utf8Offset = this._constantPoolOffsets[offset] -this._structOffset;
		return this._reader.utf8At(utf8Offset + 3, this._reader.u2At(utf8Offset + 1));
    }

	/**
	 * Read a short from the constant pool and advance the _readOffset counter.
	 */
    protected int consumeShort() {
    	int result = this._reader.u2At(this._readOffset);
    	this._readOffset += 2;
    	return result;
    }

    /**
	 * Read an int from the constant pool and advance the _readOffset counter.
	 */
    protected int consumeInt() {
    	int result = (int)this._reader.u4At(this._readOffset);
    	this._readOffset += 4;
    	return result;
    }

	/**
	 * Read a byte from the constant pool and advance the _readOffset counter.
	 */
    protected int consumeByte() {
    	int result = this._reader.u1At(this._readOffset);
    	this._readOffset++;
    	return result;
    }

    /**
     * Helper to convert a full name to its constant pool encoding.
     */
    protected char[] toConstantPoolName(char[] name) {
        char[] constantPoolName = CharOperation.concat(name, new char[0]); // copy
        CharOperation.replace(constantPoolName, '.', '/');
        return constantPoolName;
    }

    /**
     * Evaluate this attribute only if it applies for the given method info.
     * @param info
     * @param methodBinding
     * @param environment for resolving types during evaluation
     * @return true if attribute actually matched and was consumed.
     */
    public boolean evaluate(MethodInfo info, MethodBinding methodBinding, LookupEnvironment environment)
    {
        // NOOP. Override to do useful things.
        return false;
    }

    /**
     * Evaluate this attribute only if it applies for the given field binding.
     * @param fieldBinding
     * @return true if attribute actually matched and was processed.
     */
    public boolean evaluate(FieldBinding binding) {
        // NOOP. Override to do useful things.
        return false;
    }

	/**
	 * Latest hook into reading of binary types.
	 * @param type
	 * @param state (ITranslationStates)
	 */
	public void evaluateLateAttribute(ReferenceBinding type, int state) {
		// NOOP, Override to do useful things.
	}


	// ----- Utilities for evaluation: ---------

	/**
	 * Resolve a type from its constant pool name.
	 *
	 * @param environment      for the actual resolving.
	 * @param constantPoolName as read from the attribute
	 * @param missingTypeNames names of those types that were missing when compiling the current type
	 * @return resolved type
	 */
	protected ReferenceBinding getResolvedType(LookupEnvironment environment, char[] constantPoolName, char[][][] missingTypeNames) {
		ReferenceBinding type = environment.getTypeFromConstantPoolName(
		    constantPoolName, 0, -1, false, missingTypeNames); // FIXME(GENERIC): determine last parameter!
		if (type instanceof UnresolvedReferenceBinding)
		    type = resolveReferenceType(environment, (UnresolvedReferenceBinding)type);
		return type;
	}

	ReferenceBinding resolveReferenceType(LookupEnvironment environment, UnresolvedReferenceBinding type) {
			return type.resolve(environment, false);
	}
}
