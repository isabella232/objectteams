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
 * $Id: OTPreferencesMessages.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * This class integrates OT-specific messages for the property/preference pages
 * with those provided by the JDT (OT-specific messages have precedence).
 *
 * Created on Sep 11, 2005
 * 
 * @author stephan
 * @version $Id: OTPreferencesMessages.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class OTPreferencesMessages {

	private static final String BUNDLE_NAME= "org.eclipse.objectteams.otdt.internal.ui.preferences.OTPreferencesMessages";//$NON-NLS-1$


	private OTPreferencesMessages() {
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, OTPreferencesMessages.class);
	}
	
	public static String OTCompilerPreferencePageName;
	public static String OTCompilerPropertyPageName;

	public static String OTCompilerPreferencePage_title;
	
	public static String OTCompilerConfiguration_common_description;
	public static String OTCompilerProblemConfiguration_description;
	public static String OTCompilerProblemConfiguration_otjld_ref_description;
	public static String OTCompilerFeatureEnablement_description;

	public static String OTCompilerConfigurationBlock_not_exactly_one_basecall_label;
	public static String OTCompilerConfigurationBlock_unsafe_role_instantiation_label;
	public static String OTCompilerConfigurationBlock_effectless_fieldaccess_label;
	public static String OTCompilerConfigurationBlock_fragile_callin_label;
	public static String OTCompilerConfigurationBlock_unused_parammap_label;
	public static String OTCompilerConfigurationBlock_potential_ambiguous_playedby_label;
	public static String OTCompilerConfigurationBlock_ambiguous_lowering_label;
	public static String OTCompilerConfigurationBlock_abstract_potential_relevant_role_label;
	public static String OTCompilerConfigurationBlock_decapsulation_label;
	public static String OTCompilerConfigurationBlock_bindingconventions_label;
	public static String OTCompilerConfigurationBlock_deprecated_path_syntax_label;
	public static String OTCompilerConfigurationBlock_inferred_callout_label;
	public static String OTCompilerConfigurationBlock_adapting_deprecated_label;
	public static String OTCompilerConfigurationBlock_incomplete_build_label;
	public static String OTCompilerConfigurationBlock_binding_to_system_class;
	public static String OTCompilerConfigurationBlock_override_final_role;
	public static String OTCompilerConfigurationBlock_exception_in_guard;
	public static String OTCompilerConfigurationBlock_opt_scoped_keywords;
	
	public static String preferences_general_title;
	public static String preferences_general_callinmarker_label;
	public static String preferences_general_debugfilters_label;
}
