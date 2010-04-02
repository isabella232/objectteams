/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Germany and Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AspectBindingRequestAnswer.java 23468 2010-02-04 22:34:27Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox;

import org.eclipse.objectteams.otequinox.hook.AspectPermission;

/** 
 * Answer for an aspect binding request. See extension point org.eclipse.objectteams.otequinox.aspectBindingNegotiators.
 * 
 * @author stephan
 * @see 1.2.6
 */
public class AspectBindingRequestAnswer 
{
	/** Should this answer be remembered persistently? */
	public boolean persistent;
	/** Should this answer be applied to all subsequent requests? */
	public boolean allRequests;
	/** The actual answer. */
	public AspectPermission permission;
	
	/**
	 * @param persistent  Should this answer be remembered persistently?
	 * @param allRequests Should this answer be applied to all subsequent requests?
	 * @param permission  One of DENY, GRANT, UNDEFINED (let others decide).
	 */
	public AspectBindingRequestAnswer(boolean persistent, boolean allRequests, AspectPermission permission) {
		this.persistent = persistent;
		this.allRequests = allRequests;
		this.permission = permission;
	}
	
}
