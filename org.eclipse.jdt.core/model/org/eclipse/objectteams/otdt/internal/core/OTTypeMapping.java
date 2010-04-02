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
 * $Id: OTTypeMapping.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.objectteams.otdt.core.IOTType;


/**
 * Maps an IType from the JavaModel to an OTType. 
 * 
 * @author kaiser
 * @version $Id: OTTypeMapping.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class OTTypeMapping
{
	private CompilationUnitMapping _data;

    public OTTypeMapping()
    {
    	_data = new CompilationUnitMapping();
    }

	public void put(IType type, IOTType otType)
	{		
		if (type != null && otType != null)
		{
			IJavaElement key= getParent(type);
						
			if (_data.contains(key))
			{
				_data.getOTTypes(key).add(otType);        
			}
			else
			{
				_data.add(key, new OTTypeList(otType));
			}						
		}
	}

	/**
	 * Removes the given IType from cache and all its children. The entire 
	 * mapping (ICompilationUnit::OTTypeList) is remove if no more bound type 
	 * is cached.
	 */
	public void remove(IType type)
	{
		if (type != null)
		{
			ICompilationUnit unit = type.getCompilationUnit(); 
			
			OTTypeList list   = _data.getOTTypes(unit);
			IOTType    otType = get(type);

			if (otType != null)
			{							
				IType[] innerTypes = otType.getInnerTypes();
				for (int idx = 0; idx < innerTypes.length; idx++)
				{
				    // TODO(jwl): simplify later
					remove((IType)((IOTType)innerTypes[idx]).getCorrespondingJavaElement());
				}					
			
				list.remove(type.getElementName());
			}
			
			if (list.getSize() == 0)
			{
				_data.remove(unit);
			}
		}
	}

	/**
	 * Removes an changed IType, if it is not of the same instance as the given
	 * IType. This method is used for JavaElementDelta.ElementChanged. It 
	 * preserves the newly created type from removal.
	 * 
	 * @see org.eclipse.objectteams.otdt.core.OTModelReconcileListener
	 */
	public void removeChangedElement(IType elem)
	{
		IOTType otType = get(elem);
				
		// removes changed element only if they are not equal, this is necessary
		// to avoid removal if an element has been newly created
		if ((otType != null) && (otType.getCorrespondingJavaElement() != elem))
		{
			remove(elem);
		}		
	}

    public IOTType get(IType type)
	{
		IOTType otElem = null;
		
		if (type != null) 
		{
			IJavaElement key = getParent(type);
			if (key != null)
			{
				OTTypeList elems = _data.getOTTypes(key);
				
				if (elems != null)
				{
					otElem = elems.get(type.getFullyQualifiedName());				
				}
			}
		}
		
		return otElem;
	}
	
	public List<OTType> getOTElements()
	{
		return _data.getOTElements();
	}
	
    public boolean contains(IType type)
    {
        return (get(type) != null);
    }

	/**
	 * Get type's parent (for use as a hash key).
	 * @param type
	 * @return
	 */
	private IJavaElement getParent(IType type) {
		IJavaElement parent;
		if (type.isBinary())
		{
			parent = type.getClassFile();
		}
		else
		{
			parent = type.getCompilationUnit();
		}
		return parent;
	}


}
