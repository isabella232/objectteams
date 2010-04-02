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
 * $Id: TypeHierarchyConnector.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.TypeVector;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;

/**
 * @author anklam
 *
 * This class is used to access the information of a TypeHierarchy.
 * 
 * @version $Id: TypeHierarchyConnector.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class TypeHierarchyConnector extends TypeHierarchy
{
    
    public TypeHierarchyConnector(IType focus, IJavaProject project, boolean computeSubtypes)
	{
        super(focus, new ICompilationUnit[0], project, computeSubtypes);
	}
    
    public TypeHierarchyConnector(IType type, IJavaSearchScope scope, boolean computeSubtypes) {
		super(type, new ICompilationUnit[0], scope, computeSubtypes);
	}

	@SuppressWarnings("rawtypes") // accessing super field with raw type
	public Map getClasstoSuperclass()
    {
        return this.classToSuperclass;
    }
    
    @SuppressWarnings("rawtypes") // accessing super field with raw type
    public Map getTypeToSubtypes()
    {
        return this.typeToSubtypes;
    }

    @SuppressWarnings("rawtypes") // accessing super field with raw type
    public Map getTypeToSuperInterfaces()
    {
        return this.typeToSuperInterfaces;
    }

    @SuppressWarnings("rawtypes") // accessing super field with raw type
    public Map getTypeFlags()
    {
    	return this.typeFlags;
    }
    
	public IType[] getRootClasses()
    {
		// This is a workaround.
        // because a role focus type is contained as
        // root class even with a resolved superclass.        
        if (this.classToSuperclass.keySet().contains(this.focusType))
        {
        	TypeVector result = new TypeVector(super.getRootClasses());
            result.remove(this.focusType);
        	return result.elements();            
        }
        else
        {
            return super.getRootClasses();
        }
	}
}
