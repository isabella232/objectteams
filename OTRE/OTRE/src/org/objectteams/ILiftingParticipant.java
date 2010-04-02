/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2009 Stephan Herrmann
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ILiftingParticipant.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 	Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.objectteams;

/**
 * A lifting participant hooks into the lifting process.
 * 
 * @author stephan
 * @since 1.3.1
 */
public interface ILiftingParticipant {
	/**
	 * This method is called when lifting does not find a suitable role within the
	 * team's internal role cache. If this method returns a non-null value,
	 * this value is considered by the runtime as being the desired role 
	 * (i.e., it must be castable to that role type), and no new role is created. 
	 * If this method returns null, lifting proceeds as normal, i.e., 
	 * a fresh role is created using the default lifting constructor.
	 * 
	 * @param teamInstance
	 * @param baseInstance
	 * @param roleClassName
	 * @return either null or an instance of the class specified by roleClassName
	 */
	Object createRole(ITeam teamInstance, Object baseInstance, String roleClassName);
}
