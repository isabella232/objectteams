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
 * $Id: CompilationUnitMapping.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.objectteams.otdt.internal.core.OTTypeList.OTTypeIterator;


/**
 * Maps an ICompilationUnit to the contained IOTTypes.
 * 
 * @author kaiser
 * @version $Id: CompilationUnitMapping.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class CompilationUnitMapping
{
	/** hashes ICompilationUnit:OTTypeList */
	private Map<IJavaElement, OTTypeList> _data;

    public CompilationUnitMapping()
    {
    	_data = new HashMap<IJavaElement, OTTypeList>();
    }

	public OTTypeList getOTTypes(IJavaElement unit)
	{
		return _data.get(getHashkeyElement(unit));
	}

	public void add(IJavaElement unit, OTTypeList otElems)
	{		
		_data.put(getHashkeyElement(unit), otElems == null ? new OTTypeList() : otElems);
	}

	public void remove(IJavaElement unit)
	{
		_data.remove(getHashkeyElement(unit));
	}

    public boolean contains(IJavaElement unit)
    {
        return _data.containsKey(getHashkeyElement(unit));
    }

	/**
	 * This method needs to return an ordenary list, due to nameclashes when an
	 * OTTypeList is used. An OTTypeList can be seen as hashed list. 
	 * 
	 * @return All IOTTypes which the mapping consists of. 
	 */
	public List<OTType> getOTElements()
    {
        List<OTType> result = new LinkedList<OTType>();
        
        for (Iterator<OTTypeList> iter = _data.values().iterator(); iter.hasNext(); )
        {
        	OTTypeList cuTypes = iter.next(); 

			for (OTTypeIterator iterator = cuTypes.getIterator(); iterator.hasNext();)
			{
				result.add(iterator.getNext());	
			}        	
        }
        
        return result;
    }
    
	private IJavaElement getHashkeyElement(IJavaElement elem)
	{
	    return elem;
	    
	    // This below code here causes that WorkingCopies and ICompilationUnits are
	    // treated identically. I.e. OTModelManager.getOTType() would return the identical
	    // IOTType for a working copy as it would return for the real ICompilationUnit.
	    // I don't think this is correct.
	    // Testcases that break with this behavior are RenameMethodInInterfaceTests.testMethodAlreadyExists*
	    // (carp)
	    
//		if (elem != null)
//		{
//			if (elem.getElementType() == IJavaElement.COMPILATION_UNIT) 
//			{
//				ICompilationUnit unit = (ICompilationUnit)elem; 
//				if (unit.isWorkingCopy())
//				{
//					return unit.getPrimary();
//				}
//			}
//		}
//		return elem;
	}
}
