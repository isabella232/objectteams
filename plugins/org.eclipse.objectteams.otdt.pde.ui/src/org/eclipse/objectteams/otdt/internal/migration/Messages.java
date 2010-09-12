/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann
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
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.migration;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.objectteams.otdt.internal.migration.messages"; //$NON-NLS-1$
	
	public static String MigrationResolutions_update_nature_and_builder_label;
	public static String MigrationResolutions_update_bundle_name_label;
	public static String MigrationResolutions_update_extension_label;
	
	public static String ProjectMigration_old_nature_message;
	public static String ProjectMigration_old_builder_message;

	public static String OTEquinoxMigration_old_otequinox_bundle_message;
	public static String OTEquinoxMigration_old_otequinox_extensionpoint_message;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() { // not to be instantiated
	}
}
