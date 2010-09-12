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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.bindingeditor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.objectteams.otdt.internal.ui.bindingeditor.Messages"; //$NON-NLS-1$

	public static String AddTypeBindingDialog_no_roles_available_error;

	public static String AddTypeBindingDialog_role_selection_title;

	public static String BindingEditorDialog_dialog_title;

	public static String BindingEditor_connector_title;
	public static String BindingEditor_role_types_title;
	public static String BindingEditor_base_types_title;
	public static String BindingEditor_add_type_binding_button;
	public static String BindingEditor_remove_button;

	public static String BindingEditor_method_binding_tab;
	public static String BindingEditor_param_mapping_tab;
	
	public static String BindingConfiguration_new_method_label;
	public static String BindingConfiguration_role_methods_label;
	public static String BindingConfiguration_base_methods_label;
	public static String BindingConfiguration_apply_button;

	public static String BindingConfiguration_error_unspecific;
	public static String BindingConfiguration_error_retrieving_role_methods;
	public static String BindingConfiguration_error_binding_creation_failed;

	public static String OpenBindingEditorAction_error_title;
	public static String OpenBindingEditorAction_error_no_team_selected;

	public static String BindingConfiguration_error_cant_edit_rolefile;

	public static String BindingConfiguration_error_cant_edit_rolefile_nested;



	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
