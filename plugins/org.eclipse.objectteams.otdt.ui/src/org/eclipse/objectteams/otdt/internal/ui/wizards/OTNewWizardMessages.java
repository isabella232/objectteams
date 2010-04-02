/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTNewWizardMessages.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class OTNewWizardMessages extends NLS {

	private static final String BUNDLE_NAME= "org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages";//$NON-NLS-1$


	private OTNewWizardMessages() {
		// Do not instantiate
	}

	public static String NewProjectWizard_task_title;
	public static String NewProjectWizard_wizard_creation_failure_title;
	public static String NewProjectWizard_wizard_creation_failure_message;
	public static String NewProjectWizard_error_title;

	// common errors:
	public static String NewTypeWizardPage_same_enclosing_and_super_error;
	public static String NewTypeWizardPage_super_of_regular_is_role_error;

	// NewRole:
	public static String NewRoleCreationWizard_title;
	
	public static String NewRoleWizardPage_title;
	public static String NewRoleWizardPage_description;

	public static String NewRoleWizardPage_enclosingtype_label;
	public static String NewRoleWizardPage_ChooseEnclosingTypeDialog_title;
	public static String NewRoleWizardPage_ChooseEnclosingTypeDialog_description;

	public static String NewRoleWizardPage_inlined_checkbox_label;

	public static String NewRoleWizardPage_SuperclassDialog_title;
	public static String NewRoleWizardPage_SuperclassDialog_message;
	public static String NewRoleWizardPage_superclass_explicit_label;

	public static String NewRoleWizardPage_superclass_implicit_label;

	public static String NewRoleWizardPage_BaseclassDialog_title;
	public static String NewRoleWizardPage_BaseclassDialog_message;
	public static String NewRoleWizardPage_baseclass_label;
	public static String NewRoleWizardPage_baseclass_selection_button;

	// errors:
	public static String NewRoleWizardPage_already_has_this_super;
	public static String NewRoleWizardPage_explicit_and_implicit_subclassing_error;
	public static String NewRoleWizardPage_incompatible_supers_error;
	public static String NewRoleWizardPage_super_is_overridden_error;
	public static String NewRoleWizardPage_super_is_role_of_different_team_error;
	
	public static String NewRole_base_class_equals_enclosing;
	public static String NewRole_base_class_equals_member;
	public static String NewRole_role_hides_team;
	
	// NewTeam:
	public static String NewTeamCreationWizard_title;
	public static String NewTeamWizardPage_title;
	public static String NewTeamWizardPage_description;

	public static String NewTeamWizardPage_ChooseSuperTypeDialog_title;
	public static String NewTeamWizardPage_ChooseSuperTypeDialog_description;
	public static String NewTeamWizardPage_superclass_label;

	public static String NewTeamWizardPage_methods_constructors;

	public static String NewTeamWizardPage_BindingEditor_selection;
	public static String NewTeamWizardPage_BindingEditor_description;

	public static String TeamSelectionDialog_upperLabel;
	public static String TeamSelectionDialog_lowerLabel;
	public static String TeamSelectionDialog_notypes_title;
	public static String TeamSelectionDialog_notypes_message;




	static {
		NLS.initializeMessages(BUNDLE_NAME, OTNewWizardMessages.class);
	}
}
