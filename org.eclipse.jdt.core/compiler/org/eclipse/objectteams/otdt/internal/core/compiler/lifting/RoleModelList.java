/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleModelList.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lifting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;

/**
 * MIGRATION_STATE: complete.
 *
 * @author jwloka
 * @version $Id: RoleModelList.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class RoleModelList
{
	interface IRoleModelIterator
	{
		public boolean hasNext();
		public RoleModel getNext();
	}
	ArrayList<RoleModel> _data = new ArrayList<RoleModel>();

    public RoleModelList()
    {
        super();
    }

    public void add(RoleModel item)
    {
    	if (item != null)
    	{
    		this._data.add(item);
    	}
    }

    public void addList(RoleModelList newList)
    {
    	RoleModel[] src1  = newList.toArray();
		RoleModel[] src2 = toArray();
		RoleModel[] dest = new RoleModel[src1.length+src2.length];

    	System.arraycopy(src1, 0, dest, 0, src1.length);
		System.arraycopy(src2, 0, dest, src1.length, src2.length);

    	this._data = new ArrayList<RoleModel>(Arrays.asList(dest));
    }

    public RoleModel get(int idx)
    {
    	return this._data.get(idx);
    }

    public int getSize()
    {
    	return this._data.size();
    }

    IRoleModelIterator getIterator()
    {
    	return new IRoleModelIterator()
    	{
    		Iterator<RoleModel> rawIter = RoleModelList.this._data.iterator();

    		public boolean hasNext()
    		{
    			return this.rawIter.hasNext();
    		}

    		public RoleModel getNext()
    		{
    			return this.rawIter.next();
    		}
    	};
    }

    public void remove(int i) {
        this._data.remove(i);
    }

    public RoleModel[] toArray()
    {
		RoleModel[] result = new RoleModel[this._data.size()];

    	int idx = 0;
    	for (IRoleModelIterator iter = getIterator(); iter.hasNext(); idx++)
        {
        	result[idx] = iter.getNext();
        }

    	return result;
    }

    public boolean isEmpty()
    {
    	return this._data.isEmpty();
    }
}
