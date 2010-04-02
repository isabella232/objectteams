/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleType.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IOTTypeHierarchy;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.ExceptionHandler;

/**
 * IRoleType implementation
 * 
 * @author jwloka
 * @version $Id: RoleType.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class RoleType extends OTType implements IRoleType
{
    private IType  _baseClass;
    private String _baseAnchor;
    private String _baseClassName;

	public RoleType(IType correspondingJavaType,
					IJavaElement parent,
					int flags,
					String baseClassName,
					String baseClassAnchor)
    {
        super(ROLE | (TypeHelper.isTeam(flags) ? TEAM : 0), correspondingJavaType, parent, flags);
        this._baseClassName = baseClassName;
        this._baseAnchor= baseClassAnchor;
    }

    /**
	 * A role's team is represented by the element's team 
	 */
    public IOTType getTeam()
    {
    	// try directly within OT-Model:
    	if (this._parent != null && this._parent instanceof IOTType)
    		return (IOTType)this._parent;
    	// try a detour through Java-Model:
    	IType teamType = getTeamJavaType();
        if (teamType != null)
	    	return OTModelManager.getOTElement(teamType);
        // nope.
        return null;
    }
    
    public IType getTeamJavaType() {
    	return ((IType)getCorrespondingJavaElement()).getDeclaringType();
    }

	public IMethodMapping[] getMethodMappings()
	{
		return getMethodMappings(CALLINS | CALLOUTS);
	}

	/**
	 * @param type An ORed combination of IRoleType.CALLINS and IRoleType.CALLOUTS 
	 */
	public IMethodMapping[] getMethodMappings(int type)
	{
		List<IMethodMapping> result   = new LinkedList<IMethodMapping>();
		IJavaElement[]       children = getChildren();
		
		for (int idx = 0; idx < children.length; idx++)
		{
			if (children[idx] instanceof IMethodMapping)
			{
				IMethodMapping mapping = (IMethodMapping) children[idx];
				switch(mapping.getElementType())
				{
					case IOTJavaElement.CALLIN_MAPPING:
					    if ((type & CALLINS) != 0)
					        result.add(mapping);
					    break;
					case IOTJavaElement.CALLOUT_MAPPING:
					    if ((type & CALLOUTS) != 0)
					        result.add(mapping);
					    break;
                    case IOTJavaElement.CALLOUT_TO_FIELD_MAPPING:
                        if ((type & CALLOUTS) != 0)
                            result.add(mapping);
                        break;
				}
			}
		}
		
		return result.toArray(new IMethodMapping[result.size()]);
	}

	public boolean isRoleFile()
	{
	    return false;
	}
	
	public void setBaseClass(IType baseClass)
	{
		_baseClass = baseClass;
	}

	public IType getBaseClass() throws JavaModelException
	{
		if (_baseClass == null)
		{
			JavaModelException jex = null;
			
			try
            {
//			    System.out.println("RoleType.getBaseClass(): " + getElementName());
                _baseClass = findBaseClass();
            }
			catch (JavaModelException ex)
			{
				jex = ex;
			}
            catch (Exception ex)
            {
            	// just be sure we get all kind of exceptions
            	ExceptionHandler.getOTDTCoreExceptionHandler().logException(
            		MessageFormat.format("Resolving of base class ''{0}'' failed!", new Object[] {_baseClassName}), //$NON-NLS-1$
            		ex);
            }

            // when having syntax/compilation errors, we can't ensure to find our baseclass
            if (_baseClass == null && (_baseClassName != null || jex != null))
            {
            	IStatus status = new Status(
            			IStatus.WARNING, 
						JavaCore.PLUGIN_ID, 
						IStatus.OK, 
            			MessageFormat.format("Base class for role {0} not found.",  //$NON-NLS-1$
							new Object[] { getTypeQualifiedName('.') }),
						jex
            			);
            	throw new JavaModelException(new CoreException(status));
            }
		}
		
		return _baseClass;
	}
	
	public String getBaseclassName() {
	    if (_baseAnchor != null)
	        return _baseAnchor + '.' + _baseClassName;
		return _baseClassName;
	}

	public String getFullBaseclassName() {
		if (this._baseAnchor == null || this._baseAnchor.length() == 0)
			return this._baseClassName;
		return this._baseClassName+"<@"+this._baseAnchor+">"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean equals(Object obj)
	{
		if(!(obj instanceof RoleType))
		{
		    return false;
		}

		RoleType other = (RoleType)obj;
		
		// only compare base class name, resolving the class is too expensive and unnecessary.
		if ((_baseClassName==null) != (other._baseClassName==null))
		{
			return false;
		}
		if (_baseClassName != null && !_baseClassName.equals(other._baseClassName)) 
		{
			return false;
		}
		
	    return super.equals(other);
	}
	
    @SuppressWarnings("nls")
	public String toString()
	{
		return "role " + getElementName() + " for type: " + getCorrespondingJavaElement().toString();
	}

	/**
	 * Tries to resolve this role's base class in the JavaModel.
	 * 
	 * @return resolved JavaModel type or null if nothing found 
	 */
    private IType findBaseClass() throws JavaModelException
    {
        if (_baseClassName == null)
            return findSuperBaseClass();
        
        if (_baseAnchor != null)
        {
            IOTType baseAnchorType = resolveBaseAnchor();
            if (baseAnchorType == null)
                return null;
            
            // lookup via the anchor needs simple name:
            String simpleBaseName = this._baseClassName;
            int dotpos = this._baseClassName.lastIndexOf('.');
            if (dotpos != -1)
            	simpleBaseName = this._baseClassName.substring(dotpos+1);
            
            IType[] baseClasses = baseAnchorType.getRoleTypes(IOTType.ALL, simpleBaseName);
            if (baseClasses.length >= 1)
                return baseClasses[0]; // the bottom-most in the hierarchy
        }
        // resolve relative to the current type:
        return resolveInType(this, _baseClassName);
    }

    // FIXME remove recursive building of hierarchy through getBaseClass -> findSuperBaseClass -> getBaseOf -> tsuper.getBaseClass()
    private IType findSuperBaseClass() throws JavaModelException
    {
        IOTTypeHierarchy hierarchy = newSuperOTTypeHierarchy(new NullProgressMonitor());
        IType[] tsupers = hierarchy.getTSuperTypes((IType) this.getCorrespondingJavaElement());
        for (int i = 0; i < tsupers.length; i++)
        {
            IType baseType = getBaseOf(tsupers[i]);
            if (baseType != null)
                return baseType;
        }

        IType explicitSuper = hierarchy.getExplicitSuperclass((IType) this.getCorrespondingJavaElement());
        return getBaseOf(explicitSuper);
    }

    private IType getBaseOf(IType type) throws JavaModelException
    {
        if (type != null && type.exists())
        {
            IOTType otType = OTModelManager.getOTElement(type);
            if (otType == null) // i.e. java.lang.Object
                return null;
            
            if (otType.isRole())
            {
                IRoleType tsuperRole = (IRoleType) otType;
                IType tsuperBase = tsuperRole.getBaseClass();
                if (tsuperBase != null)
                    return tsuperBase;
            }
        }
        
        return null;
    }

    private IOTType resolveBaseAnchor() throws JavaModelException
    {
        if (_baseAnchor == null)
            return null;
        
        IOTType enclosingTeam = getTeam();
        if (enclosingTeam == null || !enclosingTeam.exists())
            return null;
        
        IType currentType= enclosingTeam;
        String anchorField= _baseAnchor;
        int pos= _baseAnchor.lastIndexOf('.');
        if (pos>0) {
        	anchorField= _baseAnchor.substring(pos+1);
        	currentType= resolveInType(enclosingTeam, _baseAnchor.substring(0, pos));
        }

        IType anchorType = null;
        if (anchorField.equals(new String(IOTConstants.BASE))) // enclosing is a role itself, 'base' is its base instance
        {
        	IOTType currentOTType= OTModelManager.getOTElement(currentType);
            if (currentOTType == null || !currentOTType.isRole())
                return null;

            IRoleType enclosingRole = (IRoleType)currentOTType;
            assert(enclosingRole != this);
            
            anchorType = enclosingRole.getBaseClass();
        }
        else
        {
	        IField field = currentType.getField(anchorField);
	        if (!field.exists())
	            return null;
            
	        String fieldType = Signature.toString(field.getTypeSignature());
	        anchorType = resolveInType(enclosingTeam, fieldType);
        }
        
        if (anchorType == null || !anchorType.exists())
            return null;

        return OTModelManager.getOTElement(anchorType);
    }
    
    IType resolveInType(IOTType referenceType, String type) throws JavaModelException
    {
    	// perform a search by name in the scope of this type
    	String[][] qualifiedTypes = referenceType.resolveType(type);

    	// class name must exist and be unique
		if ((qualifiedTypes != null) && (qualifiedTypes.length == 1))
		{
			String fqBaseName = Util.concat(qualifiedTypes[0][0], '.', qualifiedTypes[0][1]);
			// get JavaModel element by resolved name
			return referenceType.getJavaProject().findType(fqBaseName);			
		}
        
		return null;
    }
    
    public IType[] getTSuperRoles() throws JavaModelException {
    	ArrayList<IType> tsuperRoles = new ArrayList<IType>();
    	IType teamType = getTeam();
    	if (teamType == null)
    		throw new JavaModelException(new JavaModelStatus(IJavaModelStatus.ERROR, "Enclosing team not found for "+this.getElementName()+" perhaps this element is not on the build path?"));
    	String superteamName = teamType.getSuperclassName();
    	if (superteamName != null) {
    		if (superteamName.indexOf('.') != -1) {
    			// have qualifed super team name, find it directly:
    			IType superTeam = getJavaProject().findType(superteamName);
    			if (superTeam != null) {
    				IType tsuperRole = superTeam.getType(getElementName());
    				if (tsuperRole != null && tsuperRole.exists()) {
    					this._flags |= ExtraCompilerModifiers.AccOverriding;
    					tsuperRoles.add(tsuperRole);
    				}
    			}
    		} else {
    			// only have a simply super team name, resolve it now:
				String[][] resolvedSuperTeams = teamType.resolveType(superteamName);
		    	if (resolvedSuperTeams != null)
		    		for (String[] resolvedSuperTeam : resolvedSuperTeams)
						checkAddTSuperRole(resolvedSuperTeam[0]+'.'+resolvedSuperTeam[1], tsuperRoles);
    		}
    	}
    	// no direct tsuper, search in tsuper-team if some exist:
    	IOTType teamOTType = OTModelManager.getOTElement(teamType);
    	if (teamOTType.isRole())
    		for (IType tsuperTeam : ((IRoleType)teamOTType).getTSuperRoles()) 
    			checkAddTSuperRole(tsuperTeam.getFullyQualifiedName('.'), tsuperRoles);
    	return tsuperRoles.toArray(new IType[tsuperRoles.size()]);
    }
    private void checkAddTSuperRole(String qualifiedTeamName, ArrayList<IType> tsuperRoles) throws JavaModelException {
		IType tsuperRole= getJavaProject().findType(qualifiedTeamName+'.'+getElementName());
		if (tsuperRole != null) {
			// in case the client was only interested in the fact that we have a tsuper role
			// store this flag to avoid duplicate search for tsuper roles:
			this._flags |= ExtraCompilerModifiers.AccOverriding;
			tsuperRoles.add(tsuperRole);
		}
    }
}

//private IMethod getMethod() {
//	ITextSelection textSelection= getTextSelection();
//	IEditorInput input = getActiveEditor().getEditorInput();
//	ICodeAssist codeAssist = null;
//	Object element = JavaUI.getWorkingCopyManager().getWorkingCopy(input);
//	if (element == null) {
//		element = input.getAdapter(IClassFile.class);
//	}
//	if (element instanceof ICodeAssist) {
//		codeAssist = ((ICodeAssist)element);
//	} else {
//		// editor does not support code assist
//		showErrorMessage(ActionMessages.getString("StepIntoSelectionActionDelegate.Step_into_selection_only_available_for_types_in_Java_projects._1")); //$NON-NLS-1$
//		return null;
//	}
//	
//	IMethod method = null;
//	try {
//		IJavaElement[] resolve = codeAssist.codeSelect(textSelection.getOffset(), 0);
//		for (int i = 0; i < resolve.length; i++) {
//			IJavaElement javaElement = resolve[i];
//			if (javaElement instanceof IMethod) {
//				method = (IMethod)javaElement;
//				break;
//			}
//		}
//	} catch (CoreException e) {
//		JDIDebugPlugin.log(e);
//	}
//	if (method == null) {
//		// no resolved method
//		showErrorMessage(ActionMessages.getString("StepIntoSelectionActionDelegate.No_Method")); //$NON-NLS-1$
//	}
//	return method;
//}
