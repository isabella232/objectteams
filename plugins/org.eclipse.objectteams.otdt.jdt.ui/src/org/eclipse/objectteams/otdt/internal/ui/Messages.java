/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Messages.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getName();

	public static String Completion_method_binding_label;
	public static String Completion_callout_label;
	public static String Completion_callin_label;

	public static String Completion_callout_to_field_label;

	public static String Completion_default_lifting_constructor_label;

	public static String OTLayoutActionGroup_MenuOTPresentations;
	public static String OTLayoutActionGroup_MenuShowCallinLabels;
	public static String OTLayoutActionGroup_MenuDontShowCallinLabels;

	public static String PackageExplorer_DisplayRoleFilesAction;
	public static String PackageExplorer_DisplayRoleFilesDescription;
	public static String PackageExplorer_DisplayRoleFilesTooltip;

	public static String QuickOutline__and_role_files;

	public static String ViewAdaptor_guard_predicate_postfix;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() { /* do not instantiate */ }
}
