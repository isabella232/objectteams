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
 * $Id: OTTypeHierarchyCache.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;


class OTTypeHierarchyCache
{
	Map<IType, ITypeHierarchy> _otHierarchies  = new HashMap<IType, ITypeHierarchy>();
	Map<IType, ITypeHierarchy> _typeHierarchyConnectors = new HashMap<IType, ITypeHierarchy>();
	
    
	private static boolean cacheHierarchy(ITypeHierarchy hierarchy, Map<IType, ITypeHierarchy> hierarchies)
	{
		if (hierarchy == null)
		{
			throw new IllegalArgumentException();
		}
		
		IType focus = hierarchy.getType();
		if (focus != null)
		{
			hierarchies.put(focus, hierarchy);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean cacheOTTypeHierarchy(OTTypeHierarchy hierarchy)
	{
		return cacheHierarchy(hierarchy, _otHierarchies);
	}
	
	public boolean cacheTypeHierarchyQuery(TypeHierarchyConnector hierarchy)
	{
		return cacheHierarchy(hierarchy, _typeHierarchyConnectors);
	}

	public OTTypeHierarchy getOTTypeHierarchy(IType type)
	{
		if (type == null)
		{
			throw new IllegalArgumentException();
		}
		Object result = _otHierarchies.get(type);
		return result != null ? (OTTypeHierarchy)result : null;	
	}

	public TypeHierarchyConnector getTypeHierachyQuery(IType type)
	{
		if (type == null)
		{
			throw new IllegalArgumentException();
		}
		Object result = _typeHierarchyConnectors.get(type);
		return result != null ? (TypeHierarchyConnector)result: null;	
	}
	
	public void clear()
	{
		_otHierarchies.clear();
		_typeHierarchyConnectors.clear();
	}
	
	public void remove(IType type)
	{
		if (type == null)
		{
				throw new IllegalArgumentException();
		}
		
		_otHierarchies.remove(type);
		_typeHierarchyConnectors.remove(type);		
	}

}