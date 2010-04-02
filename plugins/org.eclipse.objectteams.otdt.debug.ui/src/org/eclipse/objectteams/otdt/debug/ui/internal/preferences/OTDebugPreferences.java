/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTDebugPreferences.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.internal.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.debug.internal.core.StepFilterManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugOptionsManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugUIPlugin;
import org.eclipse.swt.graphics.RGB;

/**
 * @author gis
 */
public class OTDebugPreferences extends AbstractPreferenceInitializer {

	// preference keys:
	public final static String OT_GENERATED_CODE_COLOR = "org.eclipse.objectteams.otdt.debug.ui.OtGeneratedCodeColor"; //$NON-NLS-1$
	public final static String OT_SPECIAL_CODE_COLOR   = "org.eclipse.objectteams.otdt.debug.ui.OtSpecialCodeColor"; //$NON-NLS-1$
    public static final String DEBUG_FILTERS_ENABLED_BOOL = "prefs.ot.debugfilters.enabled"; //$NON-NLS-1$
    
    // key is the first constant plugs one of the tokens:
    public static final String DEBUG_CALLIN_STEPPING = "org.eclipse.objectteams.otdt.debug.ui.OtCallinStepping."; // need to append token //$NON-NLS-1$
    public static final String[] CALLIN_STEPPING_TOKENS = {"role", "recurse", "orig"};   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    // special value:
    public static final String CALLIN_STEPPING_NONE = "none"; //$NON-NLS-1$
    
    public static final String[] STEP_FILTER_PACKAGES = 
    {
        "de.fub.bytecode.*",                    //$NON-NLS-1$
        "gnu.regexp.*",                         //$NON-NLS-1$
        "org.eclipse.objectteams.otre",          //$NON-NLS-1$
        "org.eclipse.objectteams.otre.jplis",    //$NON-NLS-1$
        "org.eclipse.objectteams.otre.util"      //$NON-NLS-1$
    };

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = OTDebugUIPlugin.getDefault().getPreferenceStore();
		
		PreferenceConverter.setDefault(prefs, OTDebugPreferences.OT_GENERATED_CODE_COLOR, new RGB(182, 182, 209));
		PreferenceConverter.setDefault(prefs, OTDebugPreferences.OT_SPECIAL_CODE_COLOR,   new RGB(24, 152, 16));
		
		prefs.setDefault(OTDebugPreferences.DEBUG_FILTERS_ENABLED_BOOL, true);
		
		for (String token : CALLIN_STEPPING_TOKENS)
			prefs.setDefault(OTDebugPreferences.DEBUG_CALLIN_STEPPING+token, true);
	}
    
    public static void propagateFilterFlag(IPreferenceStore prefs)
    {
        setUseOTStepFilters(prefs.getBoolean(DEBUG_FILTERS_ENABLED_BOOL));
    }

    public static void setUseOTStepFilters(boolean enable)
    {
		IPreferenceStore jdiDebugStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		IPreferenceStore debugUIStore = DebugUIPlugin.getDefault().getPreferenceStore();
		
	    String stepFilterPackages = jdiDebugStore.getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST);
	    String newStepFilterPackages;
		if (enable)
		{
	        newStepFilterPackages = addOTStepFilters(stepFilterPackages);
		}
		else
		{
	        newStepFilterPackages = removeOTStepFilters(stepFilterPackages);
		}
		
		debugUIStore.setValue(StepFilterManager.PREF_USE_STEP_FILTERS, enable);
		jdiDebugStore.setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, newStepFilterPackages);
		jdiDebugStore.setValue(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS, enable);
    }

    private static String addOTStepFilters(String values)
    {
        List result = new ArrayList();
        String[] origEntries = JavaDebugOptionsManager.parseList(values);
        for (int i = 0; i < origEntries.length; i++)
	        result.add(origEntries[i]);
        
        String[] entriesToAdd = OTDebugPreferences.STEP_FILTER_PACKAGES;
        
        for (int i = 0; i < entriesToAdd.length; i++)
        {
            String entryToAdd = entriesToAdd[i];
            if (!result.contains(entryToAdd))
                result.add(entryToAdd);
        }
        
        return JavaDebugOptionsManager.serializeList((String[])result.toArray(new String[result.size()]));
    }
    
    private static String removeOTStepFilters(String values)
    {
        String[] entriesToRemove = OTDebugPreferences.STEP_FILTER_PACKAGES;
        Arrays.sort(entriesToRemove);
        String[] orig = JavaDebugOptionsManager.parseList(values);
        List result = new ArrayList(entriesToRemove.length);
        
        for (int i = 0; i < orig.length; i++)
        {
            String origValue = orig[i];
            if (Arrays.binarySearch(entriesToRemove, origValue) < 0)
                result.add(origValue);
        }
        
        return JavaDebugOptionsManager.serializeList((String[])result.toArray(new String[result.size()]));
    }
    /** Propagate callin stepping configuration down to the org.eclipse.objectteams.otdt.debug plugin. */
	public static void setCallinStepping(String token, boolean value) {
		IPreferenceStore prefs = OTDebugUIPlugin.getDefault().getPreferenceStore();
		prefs.setValue(DEBUG_CALLIN_STEPPING+token, value);
		OTDebugPlugin.getDefault().setCallinSteppingConfig(getCallinSteppingString());
	}	
	
	public static String getCallinSteppingString() {
		IPreferenceStore prefs = OTDebugUIPlugin.getDefault().getPreferenceStore();
		String callinStepping = null;
		for (String token : CALLIN_STEPPING_TOKENS) {
			if (prefs.getBoolean(DEBUG_CALLIN_STEPPING+token)) {
				if (callinStepping == null)
					callinStepping = token;
				else
					callinStepping = callinStepping + ',' +token;
			}
		}
		if (callinStepping == null)
			callinStepping = CALLIN_STEPPING_NONE;
		return callinStepping;
	}
}
