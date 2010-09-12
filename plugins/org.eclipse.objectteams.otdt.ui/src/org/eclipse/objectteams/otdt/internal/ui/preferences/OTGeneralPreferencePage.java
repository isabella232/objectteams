/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTGeneralPreferencePage.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author gis
 */
public class OTGeneralPreferencePage extends PreferencePage implements IWorkbenchPreferencePage 
{
    private Button _callinMarkerButton;

    public OTGeneralPreferencePage()
    {
        super(OTPreferencesMessages.preferences_general_title);
    }

    protected IPreferenceStore doGetPreferenceStore()
    {
        return OTDTUIPlugin.getDefault().getPreferenceStore();
    }
    
    protected Control createContents(Composite parent)
    {
        Composite result = new Composite(parent, SWT.NULL);
        result.setLayout(new GridLayout(2, false));
        
        _callinMarkerButton = new Button(result, SWT.CHECK);
        _callinMarkerButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        _callinMarkerButton.setText(OTPreferencesMessages.preferences_general_callinmarker_label);
      
        // km: (TPX-432) read and set values before any eventhandler is registered
        initValues();

        return result;
    }

    protected void initValues()
    {
        IPreferenceStore prefs = getPreferenceStore();
        _callinMarkerButton.setSelection(prefs.getBoolean(GeneralPreferences.CALLIN_MARKER_ENABLED_BOOL));
    }
    
    protected void initDefaults()
    {
        IPreferenceStore prefs = getPreferenceStore();
        _callinMarkerButton.setSelection(prefs.getDefaultBoolean(GeneralPreferences.CALLIN_MARKER_ENABLED_BOOL));
    }
    
    @Override
    public boolean performOk()
    {
        IPreferenceStore prefs = getPreferenceStore();
        prefs.setValue(GeneralPreferences.CALLIN_MARKER_ENABLED_BOOL, _callinMarkerButton.getSelection());
        return true;
    }
    
    protected void performDefaults()
    {
        IPreferenceStore prefs = getPreferenceStore();
        GeneralPreferences.initDefaults(prefs);
        initDefaults();

        super.performDefaults();
    }

	public void init(IWorkbench workbench) {
		// nothing to do		
	}
}
