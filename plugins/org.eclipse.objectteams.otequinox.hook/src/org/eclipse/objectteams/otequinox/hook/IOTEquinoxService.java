/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany.
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
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.hook;

/**
 * This interface unites the aspect registry and the team loading mechanism
 * to a OT/Equinox service. 
 * This service is published by the otequinox plugin and consumed by the transformer hook.
 * 
 * @author stephan
 * @since OTDT 1.1.4
 */
public interface IOTEquinoxService extends IAspectRegistry, ITeamLoader {
	IByteCodeAnalyzer getByteCodeAnalyzer();
}