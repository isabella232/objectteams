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
 * $Id: OTTypeHierarchyHelper.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.TypeVector;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.PhantomType;

/**
 * @author Michael Krueger (mkr)
 * @version $Id: OTTypeHierarchyHelper.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class OTTypeHierarchyHelper
{

    protected static boolean isTeam(IType type)
    {
        IOTType possibleTeam = OTModelManager.getOTElement(type);
        
        return (possibleTeam != null && possibleTeam.isTeam());
    }
    
    protected static IType getJavaModelIType(IType type)
    {
        IType rawType = type;
        
        if (type instanceof IOTType)
        {
            rawType = (IType)((IOTType)type).getCorrespondingJavaElement();
        }
        return rawType;
    }

    protected static boolean isRole(IType type)
    {
        if (type instanceof PhantomType)
        {
            return true;
        }
        else
        {
            IOTType possibleRole = OTModelManager.getOTElement(type);        
            return (possibleRole != null && possibleRole.isRole());
        }
    }

    @SuppressWarnings("nls")
	protected static String toString(Map<IType,?> map)
    {
    	StringBuffer buffer = new StringBuffer();
        for (Iterator<IType> iter = map.keySet().iterator(); iter.hasNext();)
        {
			IType keyType = iter.next();
            Object obj = map.get(keyType);
            
            buffer.append(toString(keyType));
            buffer.append(" -> ");
            if (obj instanceof IType[])
            {
                buffer.append(toString((IType[])obj));
            }
            else if (obj instanceof TypeVector)
            {
            	buffer.append(toString((TypeVector)obj));
            }
            else if (obj instanceof IType)
            {
            	buffer.append(toString((IType)obj));
            }
            else
            {
            	buffer.append(obj);
            }
            buffer.append("\n");
		}
    	return buffer.toString();
    }

	protected static String toString(IType type)
    {
        if (type instanceof PhantomType)
        {
        	return type.toString();
        }
        else
        {
        	return type.getFullyQualifiedName('.');
        }
	}

	protected static String toString(TypeVector types)
    {
    	return toString(types.elements());
    }
    
    @SuppressWarnings("nls")
	protected static String toString(IType[] types)
    {
        StringBuffer buffer = new StringBuffer();
        for (int idx = 0; idx < types.length; idx++)
        {
			IType type = types[idx];
            if (idx != 0)
            {
            	buffer.append(", ");
            }
			buffer.append(toString(type));
		}
        return buffer.toString();
    }

	/**
	 * @param typeToSubtypes
	 * @param typeToSubtypes2
	 */
	protected static void addAllCheckingDuplicates(Map<IType, TypeVector> typesMap, Map<IType,?> addTypesMap)
    {
        for (Iterator<IType> iter = addTypesMap.keySet().iterator(); iter.hasNext();) 
        {
            IType keyType = iter.next();
            TypeVector types = typesMap.get(keyType);
            TypeVector addTypes = (TypeVector)addTypesMap.get(keyType);
            if (types == null)
            {
            	typesMap.put(keyType, addTypes);
            }
            else
            {
            	addAllCheckingDuplicates(types, addTypes);
            }
        }            
    }

	/**
	 * @param types
	 * @param addTypes
	 */
	protected static void addAllCheckingDuplicates(TypeVector types, TypeVector addTypes)
    {
		IType[] newTypes = addTypes.elements();
        for (int idx = 0; idx < newTypes.length; idx++)
        {
			IType newType = newTypes[idx];
			if ( !(types.contains(newType)) )
            {
                    types.add(newType);
            }
		}
    }

    

    
}
