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
 * $Id: SingleValueAttribute.java 23416 2010-02-03 19:59:31Z stephan $
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.FieldModel;


/**
 * MIGRATION_STATE: complete, 1 fixme(generic) remains.
 *
 * Bytecode attributes with exactly one (fixed-length) value (string).
 * Currently handled:
 * <ul>
 * <li>PlayedBy
 * 		Represents the "PlayedBy" attribute
 *
 * 		Location:
 * 		A role class which, or which super role class, is bound to a base class.
 *
 * 		Content:
 * 		The name of the bound base class.
 *
 * 		Purpose:
 * 		The OTRE uses this attribute to create BoundBaseClass instances.
 *      The OTDT uses this attribute to read the playedBy declaration from binary classes (needs to restore anchored types here)
 *
 * <li>FieldTypeAnchor
 * 	    Represents the "FieldTypeAnchor" attribute.
 *
 *      Location:
 *      A field whose type is an externalized role.
 *
 *      Content:
 *      A flat encoding of the anchor path, i.e., a '.'-separated list of fields,
 *      possibly prefixed with a type (for a static field as first segment of the path).
 *      The type uses '.' to separate packages, '$' to separate inner types.
 *
 *      Purpose:
 *      For type checking the compiler uses this attribute to restore the RoleTypeBinding
 *      for a field read from a class file.
 *
 * </ul>
 * @author stephan
 * @version $Id: SingleValueAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class SingleValueAttribute
        extends AbstractAttribute
{
    // ============== STATIC API ===================

    public static final char[] ANCHOR_DELIM = "<@".toCharArray(); //$NON-NLS-1$

	/**
     * Create a new "PlayedBy" attribute.
     * FIXME(SH): need to include anchor information into this attribute.
     *            see: 2.1.5-otjld_bound-to-role-11
     *            cf. also: CodeStream.recordTypeBinding(), ConstantPoolObjectReader.getType()
     */
    public static SingleValueAttribute playedByAttribute(char[] baseClass)
    {
        return new SingleValueAttribute(PLAYEDBY_NAME, baseClass);
    }

    /**
     *  Read a PlayedBy from byte code.
     */
    public static SingleValueAttribute readPlayedBy(
            ClassFileStruct reader,
            int             readOffset,
            int[]           constantPoolOffsets)
    {
        int    utf8Offset = constantPoolOffsets[reader.u2At(readOffset)];
        char[] value      = reader.utf8At(utf8Offset + 3, reader.u2At(utf8Offset + 1));
        SingleValueAttribute result = playedByAttribute(value);
        result._reader = reader;
        return result;
    }

    /**
     * Create a "FieldTypeAnchor" attribute.
     */
    public static SingleValueAttribute fieldTypeAnchorAttribute(char[] typeAnchor) {
    	return new SingleValueAttribute(FIELD_TYPE_ANCHOR, typeAnchor);
    }

    /**
     * Read a "FieldTypeAnchor" attribute from byte code.
     */
    public static SingleValueAttribute readFieldTypeAnchor(
            ClassFileStruct reader,
            int             readOffset,
			int             structOffset,
            int[]           constantPoolOffsets)
    {
        int idx = reader.u2At(readOffset);
        // attention: reader will add structOffset, but we appearently need to read at
        // a location without structOffset! =:-0
		int    utf8Offset = constantPoolOffsets[idx] - structOffset;
		int    num        = reader.u2At(utf8Offset + 1);
        char[] value      = reader.utf8At(utf8Offset + 3, num);
        return fieldTypeAnchorAttribute(value);
    }


    // the attribute value
    private char[] _value;


    /**
     * INTERNAL USE ONLY. Use static factory methods instead.
     * @param name
     * @param value
     */
    protected SingleValueAttribute(char[] name, char[] value) {
        super(name);
        this._value = value;
    }

    public char[] getValue() {
    	return this._value;
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
        // The length of a singele value attribute is 2 (fixed-length).
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 0;
        this._contents[this._contentsOffset++] = 2;
        // write the value
        int valueIndex = this._constantPool.literalIndex(this._value);
        this._contents[this._contentsOffset++] = (byte) (valueIndex >> 8);
        this._contents[this._contentsOffset++] = (byte) valueIndex;

        writeBack(classFile);
    }

    /**
     * Evaluate class level attribute(s).
     */
    public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		if (CharOperation.equals(this._name, PLAYEDBY_NAME))
        {
			checkBindingMismatch(binding, ExtraCompilerModifiers.AccRole);
            ReferenceBinding roleType = (ReferenceBinding)binding;
            if (CharOperation.indexOf(ANCHOR_DELIM, this._value, true) > -1) {
            	// decode anchored type:
            	char[] typeName   = singleRoleName(this._value);
            	char[][] anchorPath = anchorPath(this._value);
            	ReferenceBinding staticPart = null;
            	int i;
            	for (i=0; i<anchorPath.length-1; i++) {
            		ReferenceBinding envType = environment.askForType(CharOperation.subarray(anchorPath, 0, i+1));
            		if (envType != null)
            			staticPart = envType;
            		else if (staticPart != null)
            			break; // seen enough
            	}
            	ReferenceBinding currentType = staticPart;
            	ITeamAnchor anchor = null; // accumulate anchor path here
            	while (i<anchorPath.length) {
            		FieldBinding f = currentType.getField(anchorPath[i], true);
            		if (f == null || !(f.type instanceof ReferenceBinding))
            			return; // not a valid anchor path.
            		currentType = (ReferenceBinding) f.type;
            		if (anchor == null)
            			anchor = f;
            		else
            			anchor = f.setPathPrefix(anchor);
            		i++;
            	}
            	if (anchor == null) return; // no success!
            	ReferenceBinding baseType = anchor.getMemberTypeOfType(typeName);
            	if (baseType == null) return; // no success!
				roleType.baseclass = (ReferenceBinding) anchor.getRoleTypeBinding(baseType, null/*typeArguments*/, 0); // FIXME(SH) retrieve type arguments from attribute (need to store first ;-)
            } else {
            	roleType.baseclass = getResolvedType(environment, toConstantPoolName(this._value), missingTypeNames);
            }
			roleType.baseclass.setIsBoundBase(roleType);
        }
    }
    private char[] singleRoleName(char[] anchoredName) {
       int pos = CharOperation.indexOf('<', this._value);
       char[] typeName   = CharOperation.subarray(this._value, 0, pos);
       int dollar = CharOperation.indexOf('$', typeName);
       if (dollar > -1)
               typeName = CharOperation.subarray(typeName, dollar+1, -1);
       return typeName;
    }

    private char[][] anchorPath(char[] anchoredName) {
       int pos = CharOperation.indexOf('@', this._value);
       char[] anchorName = CharOperation.subarray(this._value, pos+1, this._value.length-1);
        return CharOperation.splitOn('.', anchorName);
    }

    /**
     * Evaluate field level attribute.
     */
    public boolean evaluate (FieldBinding binding) {
    	if (CharOperation.equals(this._name, FIELD_TYPE_ANCHOR)) {
    		FieldModel model = FieldModel.getModel(binding);
    		// store for further processing from BinaryTypeBinding.resolveTypesFor(FieldBinding)
    		model.typeAnchor = this._value;
    		return true;
    	}
    	return false;
    }

    public String toString()
    {
        return "OT-Attribute "+ //$NON-NLS-1$
            new String(this._name)+": "+ //$NON-NLS-1$
            new String(this._value);
    }
}
