/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTCompilerPreferencePage.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;

import org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage;

/**
 * OT_COPY_PASTE
 * The page to configure the compiler options.
 * Copied from org.eclipse.jdt.internal.ui.preferences.CompliancePreferencePage
 * 
 * This page is used by "Window->Preferences->Java->Compiler (OT/J)"
 * 
 * Created on Sep 11, 2005
 * 
 * @author stephan
 */
public class OTCompilerPreferencePage extends PropertyAndPreferencePage {

	// sync with ids in plugin.xml:
	// id of the page in the org.eclipse.ui.preferencePages extension
	public static final String PREF_ID= "org.eclipse.objectteams.otdt.ui.preferences.CompilerPreferencePage"; //$NON-NLS-1$
	// id of the pages in the org.eclipse.ui.propertyPages extension
	public static final String PROP_ID= "org.eclipse.objectteams.otdt.ui.propertyPages.CompilerPreferencePage"; //$NON-NLS-1$
	
	private CompilerConfigurationBlock fConfigurationBlock;

	public OTCompilerPreferencePage() {
		setPreferenceStore(JavaPlugin.getDefault().getPreferenceStore());
		// only shown on the preference page:
		//setDescription(OTPreferencesMessages.CompilerConfigurationBlock_common_description); 
		
		// only used when page is shown programatically
		setTitle(OTPreferencesMessages.OTCompilerPreferencePage_title);		 
	}

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		IWorkbenchPreferenceContainer container= (IWorkbenchPreferenceContainer) getContainer();
		fConfigurationBlock= new CompilerConfigurationBlock(getNewStatusChangedListener(), getProject(), container);
		
		super.createControl(parent);
		
		// TODO(SH): do we have more appropriate help?
		if (isProjectPreferencePage()) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaHelpContextIds.COMPILER_PROPERTY_PAGE);
		} else {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaHelpContextIds.COMPILER_PREFERENCE_PAGE);
		}
	}

	protected Control createPreferenceContent(Composite composite) {
		return fConfigurationBlock.createContents(composite);
	}
	
	protected boolean hasProjectSpecificOptions(IProject project) {
		return fConfigurationBlock.hasProjectSpecificOptions(project);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageID()
	 */
	protected String getPreferencePageID() {
		return PREF_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageID()
	 */
	protected String getPropertyPageID() {
		return PROP_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.dispose();
		}
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#enableProjectSpecificSettings(boolean)
	 */
	protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
		super.enableProjectSpecificSettings(useProjectSpecificSettings);
		if (fConfigurationBlock != null) {
			fConfigurationBlock.useProjectSpecificSettings(useProjectSpecificSettings);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#enablePreferenceContent(boolean)
	 */
	protected void enablePreferenceContent(boolean enable) {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.enablePreferenceContent(enable);
		}
	}
	
	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		if (fConfigurationBlock != null) {
			fConfigurationBlock.performDefaults();
		}
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (fConfigurationBlock != null && !fConfigurationBlock.performOk()) {
			return false;
		}	
		return super.performOk();
	}
	
	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performApply()
	 */
	public void performApply() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.performApply();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	public void setElement(IAdaptable element) {
		super.setElement(element);
		setDescription(null); // no description for property page
	}
	
}
