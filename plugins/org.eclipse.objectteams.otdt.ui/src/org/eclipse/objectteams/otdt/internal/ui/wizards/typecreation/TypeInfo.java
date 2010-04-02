/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TypeInfo.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * @author kaschja
 * @version $Id: TypeInfo.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class TypeInfo
{
    private String     _typeName;
    
    private IPackageFragmentRoot _pkgFragmentRoot;
	private IPackageFragment       _pkgFragment;
	
	private String       _enclosingTypeName    = ""; //$NON-NLS-1$
	private String       _superClassName       = "java.lang.Object"; //$NON-NLS-1$
	private List<String> _superInterfacesNames = new ArrayList<String>();
	private int          _modifiers            = Flags.AccPublic; 
	
	private boolean _isCreateMainMethod               = false;
	private boolean _isCreateConstructor              = false;
	private boolean _isCreateAbstractInheritedMethods = false;
	
	private boolean _isRole       = false;		
	private boolean _isInlineType = false;
	
	private IType   _currentType  = null;


	public TypeInfo(String typeName, IPackageFragmentRoot pkgFragRoot, IPackageFragment pkgFrag)
	{
	    _typeName = typeName;
	    _pkgFragmentRoot = pkgFragRoot;
	    _pkgFragment       = pkgFrag;
	}
	
	public void setModifier(int modifiers)
	{
	    _modifiers = modifiers;
	}
	
	public void setCurrentType(IType type) {
		_currentType = type;
	}
	
	public IType getCurrentType() {
		return _currentType;
	}
	
	/**
	 * 
	 * @param fullyQualifiedSuperClassName
	 */
	public void setSuperClassName(String fullyQualifiedSuperClassName)
	{
	    _superClassName = fullyQualifiedSuperClassName;
	}
	
	public void setSuperInterfacesNames(List<String> superInterfacesNames)
	{
	    _superInterfacesNames = superInterfacesNames; 
	}

	/**
	 * @param fullyQualifiedEnclosingTypeName
	 */
	public void setEnclosingTypeName(String fullyQualifiedEnclosingTypeName)
	{
	    _enclosingTypeName = fullyQualifiedEnclosingTypeName;
	}

	public void setIsRole(boolean isRole)
	{
	    _isRole = isRole;
	}
	

	public void setInline(boolean isInline)
	{
	    _isInlineType = isInline;
	}
	
	public void setCreateMainMethod(boolean isCreate)
	{
	    _isCreateMainMethod = isCreate;
	}
	
	public void setCreateConstructor(boolean isCreate)
	{
	    _isCreateConstructor = isCreate;
	}
	
	public void setCreateAbstractInheritedMethods(boolean isCreate)
	{
	    _isCreateAbstractInheritedMethods = isCreate;
	}
	
	
	/* from here get-methods */
	
    public boolean isCreateAbstractInheritedMethods()
    {
        return _isCreateAbstractInheritedMethods;
    }
    
    public boolean isCreateConstructor()
    {
        return _isCreateConstructor;
    }
    
    public boolean isCreateMainMethod()
    {
        return _isCreateMainMethod;
    }
    
    public boolean isInlineType()
    {
        return _isInlineType;
    }

    /**
     * @return fully qualified enclosing type name
     */
    public String getEnclosingTypeName()
    {
        return _enclosingTypeName;
    }
    
    public boolean isRole()
    {
        return _isRole;
    }
    
    public int getModifiers()
    {
        return _modifiers;
    }
    
    public IPackageFragment getPkgFragment()
    {
        return _pkgFragment;
    }
    
    public IPackageFragmentRoot getPkgFragmentRoot()
    {
        return _pkgFragmentRoot;
    }
    
    /**
     * @return fully qualified superclass name
     */
    public String getSuperClassName()
    {
        return _superClassName;
    }
    
    public List<String> getSuperInterfacesNames()
    {
        return _superInterfacesNames;
    }
    
    public String getTypeName()
    {
        return _typeName;
    }
}
