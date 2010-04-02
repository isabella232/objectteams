/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTDTUIPluginConstants.java 23434 2010-02-03 23:52:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui;

import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;

/**
 * A collection of all plugin related ids
 * NOTE: Keep them up to date with ids plugin.xml
 * 
 * @author kaiser
 * @version $Id: OTDTUIPluginConstants.java 23434 2010-02-03 23:52:31Z stephan $
 */
@SuppressWarnings("nls")
public interface OTDTUIPluginConstants
{
	public static final String UIPLUGIN_ID 		   = OTDTPlugin.PLUGIN_ID + ".ui";

	public static final String RESOURCES_ID        = UIPLUGIN_ID + ".OTPluginResources";

	// perspectives
	public static final String PERSPECTIVE_ID      = UIPLUGIN_ID + ".OTJavaPerspective";

	// wizards
	public static final String NEW_TEAM_WIZARD_ID  = UIPLUGIN_ID + ".wizards.NewTeamCreationWizard";
	public static final String NEW_ROLE_WIZARD_ID  = UIPLUGIN_ID + ".wizards.NewRoleCreationWizard";
}