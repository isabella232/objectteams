/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleTypeInfo.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * @author kaschja
 * @version $Id: RoleTypeInfo.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class RoleTypeInfo extends TypeInfo
{

    private String _baseTypeName;
    
    
	public RoleTypeInfo(String typeName, IPackageFragmentRoot pkgFragRoot, IPackageFragment pkgFrag)
	{
	    super(typeName, pkgFragRoot, pkgFrag);
	    setIsRole(true);
	}
    
    public void setBaseTypeName(String baseTypeName)
    {
        _baseTypeName = baseTypeName;
    }
    
    public String getBaseTypeName()
    {
        return _baseTypeName;
    }
    
    /**
     * specifies that the type is a role
     * parameter will be ignored
     */
    public void setIsRole(boolean isRole)
    {
        super.setIsRole(true);
    }
    
    public boolean isRole()
    {
        return true;
    }    
}
