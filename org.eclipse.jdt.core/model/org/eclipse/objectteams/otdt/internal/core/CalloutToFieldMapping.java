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
 * $Id: CalloutToFieldMapping.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.util.FieldData;
import org.eclipse.objectteams.otdt.core.util.MethodData;

/**
 * Callout to field mapping implementation.
 * 
 * @author brcan
 */
public class CalloutToFieldMapping extends AbstractCalloutMapping implements ICalloutToFieldMapping
{
	private boolean   _isOverride;
    private IField    _baseField;
    private FieldData _baseFieldHandle;

	public CalloutToFieldMapping(
            int declarationSourceStart,
			int sourceStart,
			int sourceEnd,
            int declarationSourceEnd,
            IRoleType role,
            IMethod correspondingJavaElem, 
			MethodData roleMethodHandle,
            FieldData baseFieldHandle,
            boolean hasSignature,
            boolean isOverride)
	{
		this(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, CALLOUT_TO_FIELD_MAPPING, role, correspondingJavaElem, roleMethodHandle, baseFieldHandle, hasSignature, isOverride, true);
	}

	protected CalloutToFieldMapping(
			int declarationSourceStart, 
			int sourceStart,
			int sourceEnd,
			int declarationSourceEnd,
			int elementType,
			IType parentRole,
			IMethod correspondingJavaElem, 
			MethodData roleMethodHandle,
			FieldData baseFieldHandle,
			boolean hasSignature,
			boolean isOverride)
	{
		this(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, CALLOUT_TO_FIELD_MAPPING, parentRole, correspondingJavaElem, roleMethodHandle, baseFieldHandle, hasSignature, isOverride, true);
	}
	
	protected CalloutToFieldMapping(
			int declarationSourceStart, 
			int sourceStart,
			int sourceEnd,
            int declarationSourceEnd,
			int elementType,
            IType parentRole,
            IMethod correspondingJavaElem, 
			MethodData roleMethodHandle,
            FieldData baseFieldHandle,
            boolean hasSignature,
            boolean isOverride,
            boolean addAsChild)
	{
		super(declarationSourceStart,
				sourceStart,
				sourceEnd,
                declarationSourceEnd, 
                elementType, 
                correspondingJavaElem, 
                parentRole, 
                roleMethodHandle, 
                hasSignature,
                addAsChild);
        
		_isOverride      = isOverride;
        _baseFieldHandle = baseFieldHandle;
	}
    
    public IMethodMapping createStealthMethodMapping()
    {
        CalloutToFieldMapping result = new CalloutToFieldMapping(
                getDeclarationSourceStart(),
				getSourceStart(),
				getSourceEnd(),
                getDeclarationSourceEnd(),
                IJavaElement.METHOD, /* pretending to be a method */
                (IType) getCorrespondingJavaElement().getParent(),
                getIMethod(),
                getRoleMethodHandle(),
                getBaseFieldHandle(),
                hasSignature(),
                isOverride(),
                false // don't add as child!
        );
		result._mimicMethodDecl = true;
        result._originalMethodMapping = this;
        return result;
    }
    
    public boolean isOverride() {
    	return this._isOverride;
    }
    
    @SuppressWarnings("nls")
	public String getElementName()
    {
		if (this._mimicMethodDecl) {
			
			MethodData roleMethodHandle = this.getRoleMethodHandle();
			if (roleMethodHandle != null)
				return roleMethodHandle.getSelector();
			return "(unknown role method)";
		}		

        StringBuffer name = new StringBuffer(super.getElementName());
        name.append(" -> ");
        
        if (hasSignature())
        {
            name.append(_baseFieldHandle.toString());
        }
        else
        {
            name.append(_baseFieldHandle.getSelector());
        }
        
        return name.toString();
    }

    public int getMappingKind()
    {
        return CALLOUT_TO_FIELD_MAPPING;
    }   

	public IField getBoundBaseField() throws JavaModelException
	{
        if (_baseField == null)
        {
            _baseField = findBaseField();
        }
        
        return _baseField;
    }

    //added for the SourceTypeConverter
    public FieldData getBaseFieldHandle()
    {
        return _baseFieldHandle;
    }

    public boolean equals(Object obj)
    {
        if(!(obj instanceof CalloutToFieldMapping))
        {
            return false;
        }

        return super.equals(obj);
    }
    
    @SuppressWarnings("nls")
	public String toString()
    {
        return "callout to field" + super.toString();
    }

    /**
     * Performs resolving of bound base field
     */
    private IField findBaseField() throws JavaModelException
    {
        IType   baseClass   = ((IRoleType)getParent()).getBaseClass();
        IType[] typeParents = TypeHelper.getSuperTypes(baseClass);
                
        return findField(typeParents, _baseFieldHandle);
    }
    
    /**
     * Tries to find an IField matching the given field handle in a set
     * of types.
     * 
     * @return the first matching IField in the set of types or null if
     * nothing found
     */
    private IField findField(IType[] types, FieldData fieldHandle)
        throws JavaModelException
    {
        // cycle through types...
        for (int idx = 0; idx < types.length; idx++)
        {
            IField[] fields = types[idx].getFields();
            // ... and compare with each field defined in current type
            for (int idy = 0; idy < fields.length; idy++)
            {
                IField tmpField = fields[idy];
                // check for equal field name                
                if (tmpField.getElementName().equals(fieldHandle.getSelector()))
                {
                    // return immmediately on first match
                    return tmpField;
                }
            }
        }
        return null;
    }
    
    // ==== memento generation: ====
    @Override
    protected char getMappingKindChar() {
    	if (this._baseFieldHandle.isSetter()) {
	    	if (this._isOverride)
	    		return 'S';
	    	return 's';
    	} else {
	    	if (this._isOverride)
	    		return 'G';
	    	return 'g';
    	}
    }
    @Override
    protected void getBaseMethodsForHandle(StringBuffer buff) {
    	JavaElement.escapeMementoName(buff, this._baseFieldHandle.getSelector());
    	buff.append(JavaElement.JEM_METHOD);
    	JavaElement.escapeMementoName(buff, this._baseFieldHandle.getFieldType());
    }
    // ==== retreive from memento:
    public static FieldData createFieldData(MementoTokenizer memento, boolean isSetter) {
    	String selector = memento.nextToken();
    	String cur = memento.nextToken();
    	if (cur.charAt(0) == JavaElement.JEM_METHOD)
    		cur = memento.nextToken(); // skip initial separator
		StringBuffer buffer = new StringBuffer();
		while (cur.length() == 1 && Signature.C_ARRAY == cur.charAt(0)) { // backward compatible with 3.0 mementos
			buffer.append(Signature.C_ARRAY);
			if (!memento.hasMoreTokens())
				break;
			cur = memento.nextToken();
		}
		buffer.append(cur);
		String fieldType = buffer.toString();
		memento.nextToken(); // skip separator
    	return new FieldData(selector, fieldType, isSetter);
    }
    // ====
    
	// implementation and alternate API of resolved(Binding)	
	public OTJavaElement resolved(char[] uniqueKey) {
		ResolvedCalloutToFieldMapping resolvedHandle = 
			new ResolvedCalloutToFieldMapping(
					getDeclarationSourceStart(),
					getSourceStart(),
					getSourceEnd(),
			    	getDeclarationSourceEnd(),
			    	getElementType(),
			        (IType) getParent(), 
			    	getIMethod(),
			        getRoleMethodHandle(),
			        _baseFieldHandle, 
			        hasSignature(), 		
			        isOverride(),
			        new String(uniqueKey));

		if(isStealthMethodMapping())
			resolvedHandle._originalMethodMapping = _originalMethodMapping;
		
		return resolvedHandle;
	}
	
	// disable method that does not apply to c-t-f:
	@Override
	public String[] getExceptionTypes() throws JavaModelException {
		return new String[0];
	}
}
