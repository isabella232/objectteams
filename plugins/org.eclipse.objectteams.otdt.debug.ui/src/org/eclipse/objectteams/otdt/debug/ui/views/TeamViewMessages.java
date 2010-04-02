/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamViewMessages.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.views;

import org.eclipse.osgi.util.NLS;

public class TeamViewMessages extends NLS
{
	private static final String BUNDLE_NAME = "org.eclipse.objectteams.otdt.debug.ui.views.TeamViewMessages"; //$NON-NLS-1$

	public static String SortTeamByName_0;

	public static String SortTeamByActivation_0;

	public static String SortTeamByActivation_1;
	
	public static String SortTeamByInstantiation_0;

	public static String SortTeamDescription_0;
	
	public static String TeamView_0;

	public static String TeamView_1;
	
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, TeamViewMessages.class);
	}

	private TeamViewMessages()
	{
	}
}
