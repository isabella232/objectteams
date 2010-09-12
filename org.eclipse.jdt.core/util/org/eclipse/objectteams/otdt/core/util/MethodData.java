/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MethodData.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 		Fraunhofer FIRST - Initial API and implementation
 * 		Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.util;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.TypeParameterElementInfo;



/**
 * The implementation of methods getTypeParameterBounds & getTypeParameterNames
 * has been copied from class org.eclipse.jdt.internal.core.SourceMethodElementInfo 
 * of the Eclipse JDT.
 * 
 * @author kaiser
 * @version $Id$
 */
public class MethodData
{
    private static final String [] EMPTY_STRING_ARRAY = new String[0];
    
	private String   _selector;
	// attention: this field seems to use the constant pool encoding of types:
    private String[] _argumentsTypes;
    private String[] _argumentsNames;
	// attention: this field seems to use the constant pool encoding of types:
    private String   _returnType;

	static final ITypeParameter[] NO_TYPE_PARAMETERS = new ITypeParameter[0];
    public ITypeParameter[] typeParameters = NO_TYPE_PARAMETERS;

 
    private boolean _isDeclaration = false;
    private boolean _covariantReturn= false;
    
    // FIXME(SH): fill with values:
    private int sourceStart, sourceEnd;

    /** Create a long method spec. */
    public MethodData(String selector, String[] types, String[] names, String returnType, boolean isDeclaration)
	{
		_selector  = selector;
		if (_selector == null) // e.g. void foo -> bar; -- missing return-type or name
			_selector = ""; // FIXME: should this better be dealt during error recovery? //$NON-NLS-1$

		_argumentsTypes = types;
		_argumentsNames = names;
		_returnType = returnType;
		
		assert types != null: "Long method spec must have types"; //$NON-NLS-1$
		if (names == null)
			_argumentsNames = EMPTY_STRING_ARRAY;
		
		_isDeclaration = isDeclaration;
	}
    
    public MethodData(String selector, String[] types, String[] names, String returnType, boolean isDeclaration,
    				  boolean covariantReturn) 
    {
    	this(selector, types, names, returnType, isDeclaration);
    	this._covariantReturn= covariantReturn;
    }
    
    /** Create a short method spec (no signature). */
    public MethodData(String selector, boolean isDeclaration)
	{
		_selector  = selector;
		_returnType = null; // TODO(SH): is this OK??
		
	    _argumentsTypes = EMPTY_STRING_ARRAY;
	    _argumentsNames = EMPTY_STRING_ARRAY;
		
		_isDeclaration = isDeclaration;
	}
    /**
     * Constructor for use by ClassFileInfo (for byte-code browsing).
     * @param selector
     * @param signature
     */
    public MethodData(String selector, String signature) {
    	signature = signature.replace('/', '.');
    	_selector = selector;
    	_argumentsTypes = Signature.getParameterTypes(signature);
    	_returnType = Signature.getReturnType(signature);
    	_argumentsNames = EMPTY_STRING_ARRAY;
    }

    public MethodData(String selector, String signature, boolean covariantReturn) {
		this(selector, signature);
		this._covariantReturn= covariantReturn;
	}

	public boolean isIncomplete()
	{
		return _argumentsTypes == EMPTY_STRING_ARRAY;
	}

    public String[] getArgumentTypes()
    {
        return _argumentsTypes;
    }
    
    public String[] getArgumentNames()
    {
    	return _argumentsNames;
    }

    public String getSelector()
    {
        return _selector;
    }
    
    public String getReturnType()
    {
    	return _returnType;
    }

    /** Similar to {@link IMethod#getSignature()}, but works as a handle only method. */
	public String getSignature() {
		return Signature.createMethodSignature(this._argumentsTypes, this._returnType);
	}
	
    public boolean isDeclaration() {
    	return _isDeclaration;
    }
    
    public boolean hasCovariantReturn() {
    	return this._covariantReturn;
    }
    // copied from org.eclipse.jdt.internal.core.SourceMethodElementInfo
    public char[][][] getTypeParameterBounds() {
    	int length = this.typeParameters.length;
    	char[][][] typeParameterBounds = new char[length][][];
    	for (int i = 0; i < length; i++) {
    		try {
    			TypeParameterElementInfo info = (TypeParameterElementInfo) ((JavaElement)this.typeParameters[i]).getElementInfo();
    			typeParameterBounds[i] = info.bounds;
    		} catch (JavaModelException e) {
    			// type parameter does not exist: ignore
    		}
    	}
    	return typeParameterBounds;
    }
    // copied from org.eclipse.jdt.internal.core.SourceMethodElementInfo
    public char[][] getTypeParameterNames() {
    	int length = this.typeParameters.length;
    	if (length == 0) return CharOperation.NO_CHAR_CHAR;
    	char[][] typeParameterNames = new char[length][];
    	for (int i = 0; i < length; i++) {
    		typeParameterNames[i] = this.typeParameters[i].getElementName().toCharArray();
    	}
    	return typeParameterNames;
    }

    public String toString()
    {
    	String signature = _selector + "("; //$NON-NLS-1$
    	
    	if (_argumentsTypes != null)
    	{
	    	for (int idx=0; idx < _argumentsTypes.length; idx++)
	    	{
	    		signature += (idx == 0 ? "" : ", ")  //$NON-NLS-1$ //$NON-NLS-2$
	    			+ Signature.getSimpleName(Signature.toString(_argumentsTypes[idx]));
	    	}
    	}
    	signature += ")"; //$NON-NLS-1$
    	
    	return signature;
    }

	public int getSourceStart() {
		return this.sourceStart;
	}

	public int getSourceEnd() {
		return this.sourceEnd;
	}
}
