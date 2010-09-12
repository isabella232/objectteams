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
 * $Id: OTTypeList.java 23416 2010-02-03 19:59:31Z stephan $
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
import java.util.Map;

import org.eclipse.objectteams.otdt.core.IOTType;


/**
 * Hashed list of IOTTypes.
 * 
 * @author kaiser
 * @version $Id: OTTypeList.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class OTTypeList
{
    /** Hashed map for ElementName:IOTType */
	private Map<String, IOTType> _data;

    public OTTypeList()
    {
        _data = new HashMap<String, IOTType>();
    }

	public OTTypeList(IOTType otType)
	{
		this();
		add(otType);
	}

	public IOTType get(String fullyQualifiedName)
	{
		return _data.get(fullyQualifiedName);
	}

	/** Add the non existing elements and replaces existing ones. */
	public void add(IOTType elem)
	{
		if (elem == null)
		{
			return;
		}
		
		_data.put(elem.getFullyQualifiedName(), elem);
	}

	public void addAll(OTTypeList list)
	{
		if (list != null)
		{
			for (OTTypeIterator iter = list.getIterator(); iter.hasNext(); )
			{
				IOTType cur = iter.getNext();
				_data.put(cur.getFullyQualifiedName(), cur);
			}
		}
	}
	
	public void remove(String simpleName)
	{
		if (simpleName == null)
		{
			return;
		}
		_data.remove(simpleName);
	}
	
	public int getSize()
	{
		return _data.size();
	}
	
	public OTTypeIterator getIterator()
	{
		final Iterator<IOTType> _rawIter = _data.values().iterator();
    	
		return new OTTypeIterator()
		{
			public boolean hasNext()
			{
    			return _rawIter.hasNext();
			}
			
			public OTType getNext()
			{
				return (OTType)_rawIter.next();
			}
		};
	}

	public interface OTTypeIterator
	{
		public boolean hasNext();
		public OTType getNext();
	}
}
