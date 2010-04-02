/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CalloutMappingsAttribute.java 23417 2010-02-03 20:13:55Z stephan $
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
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;

/**
 * Represents the "CalloutMappings" attribute
 *
 * Location:
 * A bound role class with a callout binding.
 *
 * Content:
 * A list of method bindings consisting of:
 * 		role method name
 * 		role method signature
 * 		.. more elements may need to be added ..
 *
 * Purpose:
 * Only used in the compiler:
 * + create CallinCalloutBindings from this information,
 *   - used specifically for improved error reporting.
 *   - could also be usefull for search etc??
 *
 * @author stephan
 * @version $Id: CalloutMappingsAttribute.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class CalloutMappingsAttribute extends ListValueAttribute {

	RoleModel _role;
    private CallinCalloutBinding[] _mappings; // array of CallinCalloutBinding
    private char[][] _methodNames= null;
    private char[][] _methodSignatures = null;
    private char[][] _baseMethodNames = null;
    private char[][] _baseMethodSignatures = null;
    private byte[]   _flags = null;

    public static final byte CALLOUT_GET = 		  1<<0;
    public static final byte CALLOUT_SET =        1<<1;
    private static final byte CALLOUT_PUBLIC =    1<<2;
    private static final byte CALLOUT_PROTECTED = 1<<3;
    private static final byte CALLOUT_PRIVATE =   1<<4;

    private static final int ELEMENT_LENGTH = 11;
    	// 5 name references and 1 flag-byte:
    	//     role: method-name, method-signature
    	//     base: declaring-class, method-name, method-signature
	/**
	 * Create an attribute from source
	 * @param role fetch all further info from this role
	 */
	public CalloutMappingsAttribute(RoleModel role) {
	     super(IOTConstants.CALLOUT_MAPPINGS,
                0,  // count is not yet known, add in write(..)
                ELEMENT_LENGTH);
        this._role = role;
	}

	/**
	 * Create an attribute from .class file.
	 *
	 * @param reader
	 * @param readOffset
	 * @param constantPoolOffsets
	 */
	public CalloutMappingsAttribute(
			ClassFileStruct reader,
	        int             readOffset,
	        int[]           constantPoolOffsets)
	{
		super(IOTConstants.CALLOUT_MAPPINGS, 0, ELEMENT_LENGTH); // count still unknown?
	    readList(reader, readOffset, 0 /* no structOffset */, constantPoolOffsets);
	}
	public int getNumMappings() {
		return this._methodNames.length;
	}

	public String getRoleMethodNameAt(int i) {
		return String.valueOf(this._methodNames[i]);
	}
	public String getRoleMethodSignatureAt(int i) {
		return String.valueOf(this._methodSignatures[i]);
	}
	public String getBaseMethodNameAt(int i) {
		return String.valueOf(this._baseMethodNames[i]);
	}
	public String getBaseMethodSignatureAt(int i) {
		return String.valueOf(this._baseMethodSignatures[i]);
	}
	public int getCalloutFlagsAt(int i) {
		return this._flags[i] & (CALLOUT_GET|CALLOUT_SET);
	}
	public int getDeclaredModifiersAt(int i) {
		switch (this._flags[i] & (CALLOUT_PUBLIC|CALLOUT_PROTECTED|CALLOUT_PRIVATE)) {
		case CALLOUT_PUBLIC 	: return ClassFileConstants.AccPublic;
		case CALLOUT_PROTECTED  : return ClassFileConstants.AccProtected;
		case CALLOUT_PRIVATE	: return ClassFileConstants.AccPrivate;
		default					: return 0;
		}
	}
    void read(int i)
    {
    	if (this._methodNames== null) {
    		this._methodNames          = new char[this._count][];
    		this._methodSignatures     = new char[this._count][];
    		this._flags                = new byte[this._count];
    		this._baseMethodNames      = new char[this._count][];
    		this._baseMethodSignatures = new char[this._count][];
    	}
    	this._methodNames[i]      = consumeName();
    	this._methodSignatures[i] = consumeName();
    	this._flags[i]			 = (byte)consumeByte();
    	// ignored:
    	consumeName(); // base declaring class
    	this._baseMethodNames[i]      = consumeName();
    	this._baseMethodSignatures[i] = consumeName();
    }

	@Override
	public boolean setupForWriting() {
    	if (this._role.isIgnoreFurtherInvestigation())
    		return false; // don't write this attribute, resolved information might be missing.
        // before starting to write out the attribute prepare the data:
        this._mappings = this._role.getBinding().callinCallouts;
        this._count = filterGoodBindings();
		return this._count > 0;
	}

    private int filterGoodBindings () {
    	int i=0;
    	for (int j = 0; j < this._mappings.length; j++) {
			this._mappings[i] = this._mappings[j];
			CallinCalloutBinding currenMapping = this._mappings[j];
			if (   currenMapping.isCallout()
				&& ((currenMapping.tagBits & TagBits.HasMappingIncompatibility) == 0)
				&& currenMapping.hasValidRoleMethod()
				&& currenMapping.hasValidBaseMethods())
				i++;
		}
    	return i;
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#writeElementValue(int)
     */
	void writeElementValue(int i) {
        CallinCalloutBinding mapping = this._mappings[i];
        writeName(mapping._roleMethodBinding.constantPoolName());
        writeName(mapping._roleMethodBinding.signature());
        byte flags = 0;
       	switch (mapping.calloutModifier) {
       	case TerminalTokens.TokenNameget: flags = CALLOUT_GET; break;
       	case TerminalTokens.TokenNameset: flags = CALLOUT_SET; break;
       	}
       	switch (mapping.declaredModifiers) {
       	case ClassFileConstants.AccPublic:    flags |= CALLOUT_PUBLIC;    break;
       	case ClassFileConstants.AccProtected: flags |= CALLOUT_PROTECTED; break;
       	case ClassFileConstants.AccPrivate:   flags |= CALLOUT_PRIVATE;   break;
       	}
        writeByte(flags);
        MethodBinding baseMethod = mapping._baseMethods[0];
        writeName(baseMethod.declaringClass.attributeName());
        writeName(baseMethod.constantPoolName());
        writeName(baseMethod.signature());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
	 */
	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		checkBindingMismatch(binding, ExtraCompilerModifiers.AccRole);
		((ReferenceBinding)binding).roleModel.addAttribute(this);
	}
	// Evaluate CalloutMethodMappingAttribute late, because we need our methods to be in place.
	public void evaluateLateAttribute(ReferenceBinding type, int state)
	{
    	if (state != ITranslationStates.STATE_FAULT_IN_TYPES)
    		return;

		this._mappings = new CallinCalloutBinding[this._count];
		if (this._count == 0)
			return;
		nextMethod:
		for (int i = 0; i < this._methodNames.length; i++) {
			char[] methodName = this._methodNames[i];
			MethodBinding[] methods = null;
			ReferenceBinding site = type;
			while (site != null) {
				methods = site.getMethods(methodName);
				for (int j = 0; j < methods.length; j++) {
					if (methodMatchesSignature(methods[j], this._methodSignatures[i]))
					{
						int calloutModifiers =0;
						if ((this._flags[i] & CALLOUT_GET) != 0)
							calloutModifiers = TerminalTokens.TokenNameget;
						else if ((this._flags[i] & CALLOUT_SET) != 0)
							calloutModifiers = TerminalTokens.TokenNameset;
						int declaredModifiers = getDeclaredModifiersAt(i);
						this._mappings[i] = new CallinCalloutBinding(
								false, // override: neither known nor relevant, currently
								methods[j],
								type,
								calloutModifiers,
								declaredModifiers);
						continue nextMethod;
					}
				}
				site = site.superclass();
			}
			// TODO(SH): could we have generated such an attribute??
			// See TPX-326(1).
			throw new InternalCompilerError("callout binding refers to inexistent role method: "+ //$NON-NLS-1$
											new String(this._methodNames[i])+new String(this._methodSignatures[i]));
		}
		type.addCallinCallouts(this._mappings);
	}

	private boolean methodMatchesSignature(MethodBinding methodBinding, char[] signature) {
		if (CharOperation.equals(methodBinding.signature(), signature))
			return true;
	
		char[][] signatureTypes = scanSignature(signature);
		
		TypeBinding type = methodBinding.returnType;
		if (!typeMatchesSignature(type, signatureTypes[0]))
			return false;
		
		char[][] types = CharOperation.subarray(signatureTypes, 1, -1);
		if (types.length != methodBinding.parameters.length)
			return false;
		for (int i=0; i<types.length; i++)
			if (!typeMatchesSignature(methodBinding.parameters[i], types[i]))
				return false;
		return true;
	}

	private boolean typeMatchesSignature(TypeBinding type, char[] signature) {
		char[] bindingType;
		char[] signatureType = signature;
		if (!type.isValidBinding()) {
			signatureType = getSignatureSimpleName(signatureType);
			bindingType = type.internalName();
		} else {
			bindingType = type.signature();
		}
		return CharOperation.equals(bindingType, signatureType);
	}
	
	private char[] getSignatureSimpleName(char[] signatureType) {
		if (signatureType[signatureType.length-1]!=';')
			return signatureType;
		int start = CharOperation.lastIndexOf('/', signatureType);
		if (start == -1)
			start = 0;
		return CharOperation.subarray(signatureType, start+1, signatureType.length-1);
	}

	public static char[][] scanSignature(char[] methodDescriptor) throws IllegalArgumentException {
		// modelled after BinaryTypeBinding.createMethod (simplified)
		int numOfParams = 0;
		char nextChar;
		int index = 0; // first character is always '(' so skip it
		while ((nextChar = methodDescriptor[++index]) != ')') {
			if (nextChar != '[') {
				numOfParams++;
				if (nextChar == 'L')
					while ((nextChar = methodDescriptor[++index]) != ';'){/*empty*/}
			}
		}

		// Ignore synthetic argument for member types or enum types.
		int startIndex = 0;
		// NOTE: no callout to ctor, thus no check 'isConstructor()'
		
		int size = numOfParams - startIndex;
		char[][] result= new char[size+1][]; // [0] is returnType
		if (size > 0) {
			index = 1;
			int end = 0;   // first character is always '(' so skip it
			for (int i = 0; i < numOfParams; i++) {
				while ((nextChar = methodDescriptor[++end]) == '['){/*empty*/}
				if (nextChar == 'L')
					while ((nextChar = methodDescriptor[++end]) != ';'){/*empty*/}

				if (i >= startIndex) {   // skip the synthetic arg if necessary
					result[i - startIndex + 1] = CharOperation.subarray(methodDescriptor, index, end+1);
				}
				index = end + 1;
			}
		}
		result[0] = CharOperation.subarray(methodDescriptor, index + 1, -1);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#toString(int)
	 */
	String toString(int i) {
		if (this._mappings != null)
			return this._mappings[i].toString();
		StringBuffer buf = new StringBuffer();
		buf.append(this._methodNames[i]);
		buf.append(this._methodSignatures[i]);
		buf.append(" -> "); //$NON-NLS-1$
		if ((this._flags[i] & CALLOUT_GET) != 0)
			buf.append("get "); //$NON-NLS-1$
		if ((this._flags[i] & CALLOUT_SET) != 0)
			buf.append("set "); //$NON-NLS-1$
		buf.append(this._baseMethodNames[i]);
		buf.append(this._baseMethodSignatures[i]);
		return buf.toString();
	}

}
