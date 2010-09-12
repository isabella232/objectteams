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
 * $Id: CalloutMapping.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;


import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.util.MethodData;


/**
 * Callout Mapping implementation
 * @author jwloka
 * @version $Id: CalloutMapping.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class CalloutMapping extends AbstractCalloutMapping implements ICalloutMapping
{
	private boolean    _isOverride;
	private IMethod    _baseMethod;
	private MethodData _baseMethodHandle; // Note: may be null!
	private int		   _declaredModifiers;
	
    public CalloutMapping(int        declarationSourceStart,
    					  int        sourceStart,
    					  int        sourceEnd,
						  int        declarationSourceEnd,
						  IRoleType  role,
						  IMethod	corrJavaMethod,
                          MethodData roleMethodHandle,
                          MethodData baseMethodHandle,
                          boolean hasSignature,
                          boolean isOverride,
                          int     declaredModifiers)
    {
        this(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, CALLOUT_MAPPING, role, corrJavaMethod, roleMethodHandle, baseMethodHandle, hasSignature, isOverride, declaredModifiers);
    }

    protected CalloutMapping(
            int        declarationSourceStart,
			int        sourceStart,
			int		   sourceEnd,
			int        declarationSourceEnd,
			int        elementType,
			IType      parentRole,
			IMethod	   corrJavaMethod,
            MethodData roleMethodHandle,
            MethodData baseMethodHandle,
            boolean hasSignature,
            boolean isOverride,
            int	    declaredModifiers)
	{
		super(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, elementType, corrJavaMethod, parentRole, roleMethodHandle, hasSignature);

		this._isOverride = isOverride;
		this._baseMethodHandle = baseMethodHandle;
		this._declaredModifiers = declaredModifiers;
	}

    protected CalloutMapping(
            int        declarationSourceStart,
			int        sourceStart,
			int		   sourceEnd,
			int        declarationSourceEnd,
			int        elementType,
			IType      parentRole,
			IMethod	   corrJavaMethod,
            MethodData roleMethodHandle,
            MethodData baseMethodHandle,
            boolean hasSignature,
            boolean isOverride,
            int     declaredModifiers,
            boolean addAsChild)
	{
		super(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, elementType, corrJavaMethod, parentRole, roleMethodHandle, hasSignature, addAsChild);

		this._isOverride = isOverride;
		this._baseMethodHandle = baseMethodHandle;
		this._declaredModifiers = declarationSourceEnd;
	}

    public IMethodMapping createStealthMethodMapping()
    {
        CalloutMapping result = new CalloutMapping(
		        getDeclarationSourceStart(),
				getSourceStart(),
				getSourceEnd(),
		        getDeclarationSourceEnd(),
		        IJavaElement.METHOD, /* pretending to be a method */
		        (IType) getCorrespondingJavaElement().getParent(),
		        getIMethod(),
		        getRoleMethodHandle(),
		        getBaseMethodHandle(),
		        hasSignature(),
		        isOverride(),
		        getDeclaredModifiers(),
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
			return "(unknown role method)"; //$NON-NLS-1$
		}		
		
		StringBuffer name = new StringBuffer(super.getElementName());
		name.append(" -> ");
	    
		if (_baseMethodHandle == null)
		{
			name.append("(unknown)");
		}
		else
		{
			if (hasSignature())
			{
				name.append(_baseMethodHandle.toString());
			}
			else
			{
				name.append(_baseMethodHandle.getSelector());
			}
		}
	    
	    return name.toString();
	}

	public int getMappingKind()
	{
		return CALLOUT_MAPPING;
	}	
		
	public int getDeclaredModifiers() {
		return this._declaredModifiers;
	}

	public IMethod getBoundBaseMethod() throws JavaModelException
	{
		// TODO (carp/jwl): does reconciling throw away the cached _baseMethod or will this stay forever?
		if (_baseMethod == null)
		{
            _baseMethod = findBaseMethod();
		}
		
		return _baseMethod;
	}

	public boolean equals(Object obj)
    {
		if(!(obj instanceof CalloutMapping))
		{
		    return false;
		}

		return super.equals(obj);
    }
	
    @SuppressWarnings("nls")
	public String toString()
	{
		return "callout " + super.toString();
	}

	/**
	 * Performs resolving of bound base method
	 */
    private IMethod findBaseMethod() throws JavaModelException
    {
    	if (_baseMethodHandle == null)
    		return null;
    	
    	IType   baseClass   = ((IRoleType)getParent()).getBaseClass();
		IType[] typeParents = TypeHelper.getSuperTypes(baseClass);
    	    	
    	return findMethod(typeParents, _baseMethodHandle);
    }
    
	// added for the SourceTypeConverter
    public MethodData getBaseMethodHandle()
    {
    	return _baseMethodHandle;
    }
    // ==== memento generation: ====
    @Override
    protected char getMappingKindChar() {
    	if (this._isOverride)
    		return 'O';
    	return 'o';
    }
    @Override
    protected void getBaseMethodsForHandle(StringBuffer buff) {
    	if (this._baseMethodHandle != null) // as documented, _baseMethodHandle can be null: no base methods to encode
    		getMethodForHandle(this._baseMethodHandle, buff);
    }
    // ====
    	
	// implementation and alternate API of resolved(Binding)
	public OTJavaElement resolved(char[] uniqueKey) {
		ResolvedCalloutMapping resolvedHandle = 
			new ResolvedCalloutMapping(
					getDeclarationSourceStart(),
					getSourceStart(),
					getSourceEnd(),
			    	getDeclarationSourceEnd(),
			    	getElementType(),
			        (IType) getParent(), 
			    	getIMethod(),
			        getRoleMethodHandle(),
			        _baseMethodHandle, 
			        hasSignature(), 
			        isOverride(),
			        getDeclaredModifiers(),
					new String(uniqueKey));
		
		if(isStealthMethodMapping())
			resolvedHandle._originalMethodMapping = _originalMethodMapping;
		
		return resolvedHandle;
	}

	public String[] getExceptionTypes() throws JavaModelException
	{
		MethodData roleMethodHandle = getRoleMethodHandle();
		if (   roleMethodHandle != null
			&& !roleMethodHandle.isIncomplete())
		{
			try {
				return getIMethod().getExceptionTypes();
			} catch (JavaModelException jme) {
				return new String[0]; // stealth for shorthand has no exception types
			}
		}
	    return getIMethod().getExceptionTypes();
	}
}