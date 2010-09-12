/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MonitorMessages.java 23462 2010-02-04 22:13:22Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.eclipse.monitor;

import org.eclipse.osgi.util.NLS;

public class MonitorMessages extends NLS {
	private static final String BUNDLE_NAME = MonitorMessages.class.getName();
	
	public static String button_text_refresh;
	public static String button_tooltip_refresh;

	public static String button_text_auto_refresh;
	public static String button_tooltip_auto_refresh;

	public static String job_name_refresh;

	public static String heading_team_class;
	public static String heading_num_roles;

	public static String tooltip_not_computed;
	public static String tooltip_roles_of;
	public static String tooltip_no_roles;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, MonitorMessages.class);
	}

	private MonitorMessages() {
	}
}
