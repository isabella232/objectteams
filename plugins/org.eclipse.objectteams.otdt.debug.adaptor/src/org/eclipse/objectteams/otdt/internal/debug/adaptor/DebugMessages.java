/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: DebugMessages.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor;

import org.eclipse.osgi.util.NLS;

public class DebugMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.objectteams.otdt.internal.debug.adaptor.debugMessages"; //$NON-NLS-1$
	public static String OTLaunching_loading_failed_msg;
	public static String OTLaunching_OTRE_checkbox_label;
	public static String OTLaunching_OTRE_group_title;
	public static String OTLaunching_OTEquinox_checkbox_label;
	public static String OTLaunching_no_OTJ_project_found;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, DebugMessages.class);
	}

	private DebugMessages() {
		// don't instantiate
	}
}
