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
 * $Id: CompilerConfigurationBlock.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock;
import org.eclipse.jdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.jdt.internal.ui.preferences.ScrolledPageContent;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * Partly copied from org.eclipse.jdt.internal.ui.preferences.CompilerConfigurationBlock.
 * 
 * This class defines all configurable options of the OT/J compiler.
 * + private static final Key PREF_PB_
 *   also add in getKeys()
 * + createCompilerPreferenceTabContent() -> create label
 *   -> use OTPreferencesMessages
 *   
 * Other classes/methods involved are (Problem reporting):
 * + CompilerOptions: 
 *   - constant "String OPTION_ReportXxx"
 *   - constant "long Xxx" bit for use in mask
 *   - default "warningThreshold": include Xxx-bit if appropriate
 *   - getMap(): transfer severity (bitset) to optionsMap
 *   - set(Map): transfer from optionsMap to severity (bitset) 
 *   - toString()
 * + ProblemReporter
 *   - getIrritant: map problemId (from IProblem) to irritant (=option)
 *   - individual report-methods: don't hardcode severity!
 * + DefaultCodeFormatter.getDefaultCompilerOptions()
 * 
 * Integration into the plugin:
 * + OTDTPlugin.OT_COMPILER_XXX: constants for configurable options (sync with OPTION_ReportXxx)
 * + OTCompilerPreferencePage 
 * 		(basically unchanged except for reference to CompilerConfigurationBlock and one ID) 
 * + plugin.xml:
 * 		define preference/property-page extensions
 * 
 * For enable/disable options these differences apply:
 * + CompilerOptions:
 *   - no longs/masks but a boolean field
 *
 * Created on Sep 11, 2005
 * 
 * @author stephan
 * @version $Id: CompilerConfigurationBlock.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class CompilerConfigurationBlock extends OptionsConfigurationBlock {

	// Preference store keys, see JavaCore.getOptions
	/* Note(SH): 
     * We must use the JDTCoreKeys in order to store our preferences in the same preference store (and thus file).
	 * Only keys from this store will be passed to the compiler 
     */
	
	// proplem severities:
	private static final Key PREF_PB_BASECALL = getJDTCoreKey(OTDTPlugin.OT_COMPILER_BASE_CALL);
	private static final Key PREF_PB_BASECLASS_CYCLE = getJDTCoreKey(OTDTPlugin.OT_COMPILER_BASECLASS_CYCLE);
	private static final Key PREF_PB_UNSAFE_ROLE_INSTANTIATION = getJDTCoreKey(OTDTPlugin.OT_COMPILER_UNSAFE_ROLE_INSTANTIATION);

//	private static final Key PREF_PB_EFFECTLESS_FIELDACCESS = getJDTCoreKey(OTDTPlugin.OT_COMPILER_EFFECTLESS_FIELDACCESS);
	private static final Key PREF_PB_FRAGILE_CALLIN = getJDTCoreKey(OTDTPlugin.OT_COMPILER_FRAGILE_CALLIN);
//	private static final Key PREF_PB_UNUSED_PARAMMAP = getJDTCoreKey(OTDTPlugin.OT_COMPILER_UNUSED_PARAMMAP);
	
	private static final Key PREF_PB_POTENTIAL_AMBIGUOUS_PLAYEDBY = getJDTCoreKey(OTDTPlugin.OT_COMPILER_POTENTIAL_AMBIGUOUS_PLAYEDBY);
	private static final Key PREF_PB_ABSTRACT_POTENTIAL_RELEVANT_ROLE = getJDTCoreKey(OTDTPlugin.OT_COMPILER_ABSTRACT_POTENTIAL_RELEVANT_ROLE);
	
	private static final Key PREF_PB_DECAPSULATION = getJDTCoreKey(OTDTPlugin.OT_COMPILER_DECAPSULATION);
	
	private static final Key PREF_PB_DEPRECATED_PATH_SYNTAX = getJDTCoreKey(OTDTPlugin.OT_COMPILER_DEPRECATED_PATH_SYNTAX);

	private static final Key PREF_PB_BINDING_CONVENTIONS = getJDTCoreKey(OTDTPlugin.OT_COMPILER_BINDING_CONVENTIONS);

	private static final Key PREF_PB_INFERRED_CALLOUT = getJDTCoreKey(OTDTPlugin.OT_COMPILER_INFERRED_CALLOUT);
	
	private static final Key PREF_PB_WEAVE_INTO_SYSTEM_CLASS = getJDTCoreKey(OTDTPlugin.OT_COMPILER_WEAVE_INTO_SYSTEM_CLASS);
	
	private static final Key PREF_PB_OVERRIDE_FINAL_ROLE = getJDTCoreKey(OTDTPlugin.OT_COMPILER_OVERRIDE_FINAL_ROLE);
	
	private static final Key PREF_PB_EXCEPTION_IN_GUARD = getJDTCoreKey(OTDTPlugin.OT_COMPILER_EXCEPTION_IN_GUARD);

	private static final Key PREF_PB_AMBIGUOUS_LOWERING = getJDTCoreKey(OTDTPlugin.OT_COMPILER_AMBIGUOUS_LOWERING);

	private static final Key PREF_PB_ADAPTING_DEPRECATED = getJDTCoreKey(OTDTPlugin.OT_COMPILER_ADAPTING_DEPRECATED);

	private static final Key PREF_PB_IGNORING_ROLE_RETURN = getJDTCoreKey(OTDTPlugin.OT_COMPILER_IGNORING_ROLE_RETURN);

//	private static final Key PREF_PB_INCOMPLETE_BUILD = getJDTCoreKey(OTDTPlugin.OT_COMPILER_INCOMPLETE_BUILD);
	
	// feature enablement:
	private static final Key PREF_OPT_SCOPED_KEYWORDS = getJDTCoreKey(OTDTPlugin.OT_COMPILER_SCOPED_KEYWORDS);

	// values
	private static final String ERROR= JavaCore.ERROR;
	private static final String WARNING= JavaCore.WARNING;
	private static final String IGNORE= JavaCore.IGNORE;

	private static final String ENABLED= JavaCore.ENABLED;
	private static final String DISABLED= JavaCore.DISABLED;
	
	private static final String SETTINGS_SECTION_NAME = null; // ?? see ProblemSeveritiesConfigurationBlock 
	
	// would be needed if some controls are enabled/disabled programmatically:
	// private IStatus fComplianceStatus;
	// private ArrayList fComplianceControls;
	private Composite fControlsComposite;
	private ControlEnableState fBlockEnableState;
	
	public CompilerConfigurationBlock(IStatusChangeListener context, IProject project, IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
		
		fBlockEnableState= null;
		// currently unused: 
		//fComplianceControls= new ArrayList();		
		//ComplianceStatus= new StatusInfo();

	}
	
	private static Key[] getKeys() {
		return new Key[] {
				PREF_PB_BASECALL,
				PREF_PB_BASECLASS_CYCLE,
				PREF_PB_UNSAFE_ROLE_INSTANTIATION, 
//				PREF_PB_EFFECTLESS_FIELDACCESS, 
				PREF_PB_FRAGILE_CALLIN,
				PREF_PB_IGNORING_ROLE_RETURN,
//				PREF_PB_UNUSED_PARAMMAP,
				PREF_PB_POTENTIAL_AMBIGUOUS_PLAYEDBY, PREF_PB_ABSTRACT_POTENTIAL_RELEVANT_ROLE,
				PREF_PB_DECAPSULATION,
				PREF_PB_BINDING_CONVENTIONS,
				PREF_PB_DEPRECATED_PATH_SYNTAX,
				PREF_PB_INFERRED_CALLOUT,
				PREF_PB_ADAPTING_DEPRECATED,
				PREF_PB_WEAVE_INTO_SYSTEM_CLASS,
				PREF_PB_OVERRIDE_FINAL_ROLE,
				PREF_PB_EXCEPTION_IN_GUARD,
				PREF_PB_AMBIGUOUS_LOWERING,
//				PREF_PB_INCOMPLETE_BUILD,
				PREF_OPT_SCOPED_KEYWORDS
			};
	}
	
	/*
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		setShell(parent.getShell());
		
		Composite complianceComposite= createCompilerPreferenceTabContent(parent);
		
		validateSettings(null, null, null);
	
		return complianceComposite;
	}
	
	public void enablePreferenceContent(boolean enable) {
		if (fControlsComposite != null && !fControlsComposite.isDisposed()) {
			if (enable) {
				if (fBlockEnableState != null) {
					fBlockEnableState.restore();
					fBlockEnableState= null;
				}
			} else {
				if (fBlockEnableState == null) {
					fBlockEnableState= ControlEnableState.disable(fControlsComposite);
				}
			}
		}
	}
	

	private Composite createCompilerPreferenceTabContent(Composite folder) {
		String[] errorWarningIgnore= new String[] { ERROR, WARNING, IGNORE };
		
		String[] errorWarningIgnoreLabels= new String[] {
			PreferencesMessages.ProblemSeveritiesConfigurationBlock_error,  
			PreferencesMessages.ProblemSeveritiesConfigurationBlock_warning, 
			PreferencesMessages.ProblemSeveritiesConfigurationBlock_ignore
		};

		String[] enabledDisabled= new String[] { ENABLED, DISABLED };

	
		// widget layout mainly copied from ProblemSeveritiesConfigurationBlock:
		int nColumns= 3;
		final ScrolledPageContent sc1 = new ScrolledPageContent(folder);
		
		Composite composite= sc1.getBody();
		GridLayout layout= new GridLayout(nColumns, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		composite.setLayout(layout);
		
		Label description= new Label(composite, SWT.LEFT | SWT.WRAP);
		description.setFont(description.getFont());
		description.setText(OTPreferencesMessages.OTCompilerConfiguration_common_description); 
		description.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false, nColumns - 1, 1));
		
		Composite inner;
		String label;
		
		// --- Problem configuration ---
		inner = myCreateSection(nColumns, composite, OTPreferencesMessages.OTCompilerProblemConfiguration_description);
		
		label= OTPreferencesMessages.OTCompilerProblemConfiguration_otjld_ref_description;
		addLabel(inner, nColumns, label, SWT.ITALIC);
		
		label= OTPreferencesMessages.OTCompilerConfigurationBlock_not_exactly_one_basecall_label;
		addComboBox(inner, label, PREF_PB_BASECALL, errorWarningIgnore, errorWarningIgnoreLabels, 0);			
		
		label= OTPreferencesMessages.OTCompilerConfigurationBlock_unsafe_role_instantiation_label;
		addComboBox(inner, label, PREF_PB_UNSAFE_ROLE_INSTANTIATION, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

//		label= OTPreferencesMessages.OTCompilerConfigurationBlock_effectless_fieldaccess_label;
//		addComboBox(inner, label, PREF_PB_EFFECTLESS_FIELDACCESS, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_fragile_callin_label;
		addComboBox(inner, label, PREF_PB_FRAGILE_CALLIN, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_ignoring_role_result;
		addComboBox(inner, label, PREF_PB_IGNORING_ROLE_RETURN, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		//		label= OTPreferencesMessages.OTCompilerConfigurationBlock_unused_parammap_label;
//		addComboBox(inner, label, PREF_PB_UNUSED_PARAMMAP, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_potential_ambiguous_playedby_label;
		addComboBox(inner, label, PREF_PB_POTENTIAL_AMBIGUOUS_PLAYEDBY, errorWarningIgnore, errorWarningIgnoreLabels, 0);
		
		label= OTPreferencesMessages.OTCompilerConfigurationBlock_baseclass_cycle_label;
		addComboBox(inner, label, PREF_PB_BASECLASS_CYCLE, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_ambiguous_lowering_label;
		addComboBox(inner, label, PREF_PB_AMBIGUOUS_LOWERING, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_abstract_potential_relevant_role_label;
		addComboBox(inner, label, PREF_PB_ABSTRACT_POTENTIAL_RELEVANT_ROLE, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_decapsulation_label;
		addComboBox(inner, label, PREF_PB_DECAPSULATION, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_bindingconventions_label;
		addComboBox(inner, label, PREF_PB_BINDING_CONVENTIONS, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_deprecated_path_syntax_label;
		addComboBox(inner, label, PREF_PB_DEPRECATED_PATH_SYNTAX, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_inferred_callout_label;
		addComboBox(inner, label, PREF_PB_INFERRED_CALLOUT, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_adapting_deprecated_label;
		addComboBox(inner, label, PREF_PB_ADAPTING_DEPRECATED, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_binding_to_system_class;
		addComboBox(inner, label, PREF_PB_WEAVE_INTO_SYSTEM_CLASS, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_override_final_role;
		addComboBox(inner, label, PREF_PB_OVERRIDE_FINAL_ROLE, errorWarningIgnore, errorWarningIgnoreLabels, 0);			
		
		label= OTPreferencesMessages.OTCompilerConfigurationBlock_exception_in_guard;
		addComboBox(inner, label, PREF_PB_EXCEPTION_IN_GUARD, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

//		label= OTPreferencesMessages.OTCompilerConfigurationBlock_incomplete_build_label;
//		addComboBox(inner, label, PREF_PB_INCOMPLETE_BUILD, errorWarningIgnore, errorWarningIgnoreLabels, 0);			

		// ---- Feature enablement ----
		inner = myCreateSection(nColumns, composite, OTPreferencesMessages.OTCompilerFeatureEnablement_description);

		label= OTPreferencesMessages.OTCompilerConfigurationBlock_opt_scoped_keywords;
		addCheckBox(inner, label, PREF_OPT_SCOPED_KEYWORDS, enabledDisabled, 0);
		
		// not sure whether this works
		IDialogSettings section= JavaPlugin.getDefault().getDialogSettings().getSection(SETTINGS_SECTION_NAME);
		restoreSectionExpansionStates(section);

		// store in this field in order to support enabling/disabling via enablePreferenceContent()
		fControlsComposite = composite;
		return composite;
	}

	private Composite myCreateSection(int nColumns, Composite composite, String label) {
		ExpandableComposite excomposite = createStyleSection(composite, label, nColumns);
		Composite inner= new Composite(excomposite, SWT.NONE);
		inner.setFont(composite.getFont());
		inner.setLayout(new GridLayout(nColumns, false));
		excomposite.setClient(inner);
		return inner;
	}

	private void addLabel(Composite composite, int nColumns, String descriptionText, int fontStyle) 
	{
		Label description= new Label(composite, SWT.WRAP);
		description.setText(descriptionText);
		if (fontStyle != SWT.NORMAL) {
			Font f = composite.getFont();
			FontData[] datas = f.getFontData();
			datas[0].setStyle(fontStyle);
			description.setFont(new Font(f.getDevice(), datas[0]));
		}
		GridData gd= new GridData();
		gd.horizontalSpan= nColumns;
		//gd.widthHint= fPixelConverter.convertWidthInCharsToPixels(60);
		description.setLayoutData(gd);
	}
	
	/* (non-javadoc)
	 * Update fields and validate.
	 * @param changedKey Key that changed, or null, if all changed.
	 */	
	protected void validateSettings(Key changedKey, String olValue, String newValue) {
		
		if (changedKey != null) {
			if (PREF_PB_BASECALL.equals(changedKey) || PREF_PB_DECAPSULATION.equals(changedKey)) 
			{				
				// not yet used really.
				updateEnableStates();
			} else {
				return;
			}
		} else {
			updateEnableStates();
		}		
	}
	
	
	private void updateEnableStates() {
		// TODO(SH): enable check boxes for details:
//		boolean enableBasecall = !checkValue(PREF_PB_BASECALL, IGNORE);
//		getCheckBox(PREF_PB_DEPRECATION_IN_DEPRECATED_CODE).setEnabled(enableDeprecation);
//		getCheckBox(PREF_PB_DEPRECATION_WHEN_OVERRIDING).setEnabled(enableDeprecation);

		// TODO(SH): enable check boxes for details:
//		boolean enableDecapsulation = !checkValue(PREF_PB_DECAPSULATION, IGNORE);
//		getCheckBox(PREF_PB_DEPRECATION_IN_DEPRECATED_CODE).setEnabled(enableDeprecation);
//		getCheckBox(PREF_PB_DEPRECATION_WHEN_OVERRIDING).setEnabled(enableDeprecation);

	}

	
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		String title= PreferencesMessages.ComplianceConfigurationBlock_needsbuild_title;
		
		String message;
		if (workspaceSettings) {
			message= PreferencesMessages.ComplianceConfigurationBlock_needsfullbuild_message;
		} else {
			message= PreferencesMessages.ComplianceConfigurationBlock_needsprojectbuild_message;
		}
		return new String[] { title, message };
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock#dispose()
	 */
	public void dispose() {
		IDialogSettings section= JavaPlugin.getDefault().getDialogSettings().addNewSection(SETTINGS_SECTION_NAME);
		storeSectionExpansionStates(section);
		super.dispose();
	}

}
