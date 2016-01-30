/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2016 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany, and others.
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
package org.eclipse.objectteams.otdt.internal.core;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IFieldAccessSpec;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.internal.core.util.FieldData;
import org.eclipse.objectteams.otdt.internal.core.util.MethodData;

/**
 * Callout to field mapping implementation.
 * 
 * @author brcan
 */
public class CalloutToFieldMapping extends AbstractCalloutMapping implements ICalloutToFieldMapping
{
	private boolean   isOverride;
	private int 	  declaredModifiers;
    private IField    baseField;
    private IFieldAccessSpec baseFieldHandle;

	public CalloutToFieldMapping(
            int declarationSourceStart,
			int sourceStart,
			int sourceEnd,
            int declarationSourceEnd,
            IType role,
            IMethod correspondingJavaElem, 
			MethodData roleMethodHandle,
            IFieldAccessSpec baseFieldHandle,
            boolean hasSignature,
            boolean isOverride,
            int declaredModifiers,
            boolean addAsChild)
	{
		this(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, CALLOUT_TO_FIELD_MAPPING, role, correspondingJavaElem, roleMethodHandle, baseFieldHandle, hasSignature, isOverride, declaredModifiers, addAsChild);
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
			IFieldAccessSpec baseFieldHandle,
			boolean hasSignature,
			boolean isOverride,
            int declaredModifiers)
	{
		this(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, CALLOUT_TO_FIELD_MAPPING, parentRole, correspondingJavaElem, roleMethodHandle, baseFieldHandle, hasSignature, isOverride, declaredModifiers, true);
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
            IFieldAccessSpec baseFieldHandle,
            boolean hasSignature,
            boolean isOverride,
            int declaredModifiers,
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
        
		this.isOverride      = isOverride;
        this.baseFieldHandle = baseFieldHandle;
        this.declaredModifiers = declaredModifiers;
	}
    
    public boolean isOverride() {
    	return this.isOverride;
    }
    
    @Override
    public int getFlags() throws JavaModelException {
    	return this.declaredModifiers;
    }

    @SuppressWarnings("nls")
	public String getElementName()
    {
		
        StringBuffer name = new StringBuffer(super.getElementName());
        name.append(" -> ");
        
        if (hasSignature())
        {
            name.append(this.baseFieldHandle.toString());
        }
        else
        {
            name.append(this.baseFieldHandle.getSelector());
        }
        
        return name.toString();
    }

    public int getMappingKind()
    {
        return CALLOUT_TO_FIELD_MAPPING;
    }   

	public IField getBoundBaseField() throws JavaModelException
	{
        if (this.baseField == null)
        {
            this.baseField = findBaseField();
        }
        
        return this.baseField;
    }

    //added for the SourceTypeConverter
    public IFieldAccessSpec getBaseFieldHandle()
    {
        return this.baseFieldHandle;
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
        IType   baseClass   = getDeclaringRole().getBaseClass();
        IType[] typeParents = TypeHelper.getSuperTypes(baseClass);
                
        return findField(typeParents, this.baseFieldHandle);
    }
    
    /**
     * Tries to find an IField matching the given field handle in a set
     * of types.
     * 
     * @return the first matching IField in the set of types or null if
     * nothing found
     */
    private IField findField(IType[] types, IFieldAccessSpec fieldHandle)
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
    	if (this.baseFieldHandle.isSetter()) {
	    	if (this.isOverride)
	    		return 'S';
	    	return 's';
    	} else {
	    	if (this.isOverride)
	    		return 'G';
	    	return 'g';
    	}
    }
    @Override
    protected void getBaseMethodsForHandle(StringBuffer buff) {
    	escapeMementoName(buff, this.baseFieldHandle.getSelector());
    	buff.append(JavaElement.JEM_METHOD);
    	escapeMementoName(buff, this.baseFieldHandle.getFieldType());
    }
    // ==== retreive from memento:
    public static IFieldAccessSpec createFieldData(MementoTokenizer memento, boolean isSetter) {
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
			        this.baseFieldHandle, 
			        hasSignature(), 		
			        isOverride(),
			        this.declaredModifiers,
			        new String(uniqueKey));

		return resolvedHandle;
	}
	
	// disable method that does not apply to c-t-f:
	@Override
	public String[] getExceptionTypes() throws JavaModelException {
		return new String[0];
	}

	public ILocalVariable[] getParameters() throws JavaModelException {
		// TODO Auto-generated method stub
		// see Bug 338593 - [otmodel] Add new API to ease the retrieval of the parameter annotations for an IMethodMapping
		return null;
	}
}
