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
package org.eclipse.objectteams.otdt.debug.internal.util;

import java.util.Comparator;

import org.eclipse.objectteams.otdt.debug.TeamInstance;

public class TeamActivationOrderComparator implements Comparator
{
	public int compare(Object o1, Object o2)
	{
		TeamInstance team1 = (TeamInstance)o1;
		TeamInstance team2 = (TeamInstance)o2;
		
		if (team1.getActivationTime() > team2.getActivationTime())
			return -1;

		if (team1.getActivationTime() == team2.getActivationTime())
			return 0;

		if (team1.getActivationTime() < team2.getActivationTime())
			return 1;

		return 0;
	}
}