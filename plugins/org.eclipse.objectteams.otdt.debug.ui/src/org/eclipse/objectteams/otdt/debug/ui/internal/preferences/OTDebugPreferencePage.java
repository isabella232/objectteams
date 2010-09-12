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
 * $Id: OTDebugPreferencePage.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.internal.preferences;

import static org.eclipse.objectteams.otdt.debug.ui.internal.preferences.OTDebugPreferences.CALLIN_STEPPING_TOKENS;
import static org.eclipse.objectteams.otdt.debug.ui.internal.preferences.OTDebugPreferences.DEBUG_CALLIN_STEPPING;
import static org.eclipse.objectteams.otdt.debug.ui.internal.preferences.OTDebugPreferences.DEBUG_FILTERS_ENABLED_BOOL;
import static org.eclipse.objectteams.otdt.debug.ui.internal.preferences.OTDebugPreferences.OT_GENERATED_CODE_COLOR;
import static org.eclipse.objectteams.otdt.debug.ui.internal.preferences.OTDebugPreferences.OT_SPECIAL_CODE_COLOR;

import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.preferences.BooleanFieldEditor2;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugUIPlugin;
import org.eclipse.objectteams.otdt.debug.ui.internal.preferences.OTDebugPreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.Workbench;

/**
 * @author gis, stephan
 */
public class OTDebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    private static final String JAVA_STEP_FILTER_PREFERENCE_PAGE = "org.eclipse.jdt.debug.ui.JavaStepFilterPreferencePage"; //$NON-NLS-1$

	protected boolean _debugFiltersEnabled;

    private Composite _parentComposite;
	private ColorFieldEditor fGeneratedCodeColorFieldEditor;
	private ColorFieldEditor fSpecialCodeColorFieldEditor;
	private BooleanFieldEditor2 fDebugFilter;
	/** sync with {@link OTDebugPreferences#CALLIN_STEPPING_TOKENS}: */
	private String[] callinLabels = {OTDebugPreferencesMessages.OTDebugPreferencePage_callin_step_role_label, 
								     OTDebugPreferencesMessages.OTDebugPreferencePage_callin_step_recurse_label, 
								     OTDebugPreferencesMessages.OTDebugPreferencePage_callin_step_base_label};
	
    public OTDebugPreferencePage()
    {
    	super(GRID);
    	setTitle(OTDebugPreferencesMessages.OTDebugPreferencePage_title);
    }

    public void createControl(Composite parent)
    {
        super.createControl(parent);
        _parentComposite = parent;
    }
    
    protected IPreferenceStore doGetPreferenceStore()
    {
        return OTDebugUIPlugin.getDefault().getPreferenceStore();
    }
    
    protected void createFieldEditors()
    {
    	// container group:
    	Composite groupAll= new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= 0;
		layout.verticalSpacing= convertVerticalDLUsToPixels(10);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		groupAll.setLayout(layout);
		
		// block 1 group: stack frame filtering:
		Group group = new Group(groupAll, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(OTDebugPreferencesMessages.OTDebugPreferencePage_enable_filter_title);
		
		fDebugFilter = new BooleanFieldEditor2(DEBUG_FILTERS_ENABLED_BOOL, 
											  OTDebugPreferencesMessages.OTDebugPreferencePage_enableFilter_label, 
											  SWT.NONE, group); 
		addField(fDebugFilter);
		fDebugFilter.getChangeControl(groupAll).addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }
			public void widgetSelected(SelectionEvent e) {
				setDebugFiltersEnabled(fDebugFilter.getBooleanValue());
			}
		});
		
		// block 2 group: stack frame coloring:
		group = new Group(groupAll, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(OTDebugPreferencesMessages.OTDebugPreferencePage_stackFrameColoringGroup_label);

		Composite spacer = SWTFactory.createComposite(group, 2, 1, GridData.FILL_HORIZONTAL);
		fGeneratedCodeColorFieldEditor = new ColorFieldEditor(OT_GENERATED_CODE_COLOR, 
															  OTDebugPreferencesMessages.OTDebugPreferencePage_colorGeneratedCode_label, 
															  spacer);
		fGeneratedCodeColorFieldEditor.fillIntoGrid(spacer, 2);
		addField(fGeneratedCodeColorFieldEditor);
		
		fSpecialCodeColorFieldEditor = new ColorFieldEditor(OT_SPECIAL_CODE_COLOR, 
															OTDebugPreferencesMessages.OTDebugPreferencePage_colorSpecialCode_label, 
															spacer);
		fSpecialCodeColorFieldEditor.fillIntoGrid(spacer, 2);
		addField(fSpecialCodeColorFieldEditor);
		
		// block 3 group: callin dispatch visualization:
		group = new Group(groupAll, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(OTDebugPreferencesMessages.OTDebugPreferencePage_callin_stepping_title);
		
		for (int i=0; i<CALLIN_STEPPING_TOKENS.length; i++) {
			final String token = CALLIN_STEPPING_TOKENS[i];
			final BooleanFieldEditor2 editor = new BooleanFieldEditor2(DEBUG_CALLIN_STEPPING+token, 
														  this.callinLabels[i], 
														  SWT.NONE, group); 
			addField(editor);
			editor.getChangeControl(group).addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) { }
				public void widgetSelected(SelectionEvent e) {
					OTDebugPreferences.setCallinStepping(token, editor.getBooleanValue());
				}
			});
		}
		Label description= new Label(group, SWT.LEFT | SWT.WRAP);
		Font font = description.getFont();
		FontData[] data = font.getFontData();
		data[0].setStyle(SWT.ITALIC);
		description.setFont(new Font(font.getDevice(), data[0]));
		description.setText(OTDebugPreferencesMessages.OTDebugPreferencePage_callin_stepping_hint); 
		description.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 0, 1));

    }

    protected void performDefaults()
    {
        OTDebugPreferences.propagateFilterFlag(getPreferenceStore());
        super.performDefaults();
    }
    
    /////////////// HACK synchronizing with JDT Debug step filter configuration
    
    private void setDebugFiltersEnabled(boolean enable)
	{
		if (_debugFiltersEnabled == enable)
		    return;

		_debugFiltersEnabled = enable;
		
		OTDebugPreferences.setUseOTStepFilters(enable);
		updateStepFilteringPrefPage();
	}
    
    private void updateStepFilteringPrefPage()
    {
        List prefs = Workbench.getInstance().getPreferenceManager().getElements(PreferenceManager.PRE_ORDER);
		for (Iterator iter = prefs.iterator(); iter.hasNext();)
        {
            PreferenceNode node = (PreferenceNode) iter.next();
            if(node.getId().indexOf(JAVA_STEP_FILTER_PREFERENCE_PAGE) != -1)
            {
                forcePreferencePageRecreation(node);
            }
        }
    }

    private void forcePreferencePageRecreation(PreferenceNode node)
    {
        IPreferencePage oldPage = node.getPage();
        if (oldPage != null)
        {
            node.setPage(null);
	        node.createPage();
	        node.getPage().createControl(_parentComposite);
	        
	        oldPage.dispose();
        }
    }

	public void init(IWorkbench workbench) { }
}
