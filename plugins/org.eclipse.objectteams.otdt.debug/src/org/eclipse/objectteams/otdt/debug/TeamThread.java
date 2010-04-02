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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug;

public class TeamThread
{
	public long threadID = 0;
	public long time = 0;

	public TeamThread(long threadID, long time)
	{
		this.threadID = threadID;
		this.time = time;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof TeamThread)
			return (threadID == ((TeamThread)obj).threadID);
			
		return super.equals(obj);
	}
	
	@Override
	public String toString()
	{
		return "threadID="+threadID+",time="+time;  //$NON-NLS-1$ //$NON-NLS-2$
	}
}