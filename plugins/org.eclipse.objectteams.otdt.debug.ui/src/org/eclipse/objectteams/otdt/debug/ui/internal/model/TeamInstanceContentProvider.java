/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamInstanceContentProvider.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.internal.model;

import org.eclipse.debug.internal.ui.model.elements.VariableContentProvider;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugUIPlugin;

/**
 * Content provider for the fields of a team instance.
 * Sole purpose compared to its superclass: make it applicable to the TeamView, too. 
 * 
 * Note, that filtering takes place within the debug.adaptor.VariablesViewAdaptor.
 * 
 * @author stephan
 * @since 1.1.2
 */
public class TeamInstanceContentProvider extends VariableContentProvider 
{
	@Override
	protected boolean supportsContextId(String id) {
		return id.equals(OTDebugUIPlugin.TEAM_VIEW_ID);
	}
}
