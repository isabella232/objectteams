/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CorrectionMessages.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.text.correction;

import org.eclipse.osgi.util.NLS;

public class CorrectionMessages extends NLS {
	private static final String BUNDLE_NAME= CorrectionMessages.class.getName();

	private CorrectionMessages() {
		// Do not instantiate
	}




	public static String OTQuickfix_addteam_description;
	public static String OTQuickfix_changerolevisibility_description;
	public static String OTQuickfix_addabstract_description;
	public static String OTQuickfix_addcallinmodifier_description;

	public static String OTQuickfix_removecallinmodifier_description;
	public static String OTQuickfix_addmethodmodifier_description;
	public static String OTQuickfix_removemethodmodifier_description;
	public static String OTQuickfix_AddCallinModifier;
	public static String OTQuickfix_ChangeCallinModifier;

	public static String OTQuickfix_ChangeCalloutToOverride;
	public static String OTQuickfix_ChangeCalloutToRegular;

	public static String OTQuickfix_addbindingprecedence_description;
	public static String OTQuickfix_addroleprecedence_description;
	public static String OTQuickfix_swapordermenu_label;
	public static String OTQuickfix_swapordermenu_description;
	public static String OTQuickfix_swapprecedenceorder_label;
	public static String OTQuickfix_swapprecedenceorder_description;
	
	public static String OTQuickfix_addtypeparametertocallin_label;

	public static String OTQuickfix_migrateroletypesyntax_description;

	public static String OTQuickFix_Type_add_base_import_to_enclosing_team;
	public static String OTQuickFix_Type_change_type_to_anchored;
	public static String OTQuickFix_Type_convertimporttobase_description;
	public static String OTQuickFix_Type_convert_fqn_to_importtobase_description;
	
	public static String OTQuickfix_makeanchorfinal_description;

	public static String OTQuickfix_materialize_inferred_callout;
	public static String OTQuickfix_materialize_inferred_callout_to_field;
	public static String OTQuickfix_materialize_inferred_callouts;

	public static String OTQuickfix_change_assignment_to_settercall;

	public static String OTQuickfix_change_fieldaccess_to_gettercall;

	public static String OTQuickfix_add_missing_role_tag;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, CorrectionMessages.class);
	}
}
