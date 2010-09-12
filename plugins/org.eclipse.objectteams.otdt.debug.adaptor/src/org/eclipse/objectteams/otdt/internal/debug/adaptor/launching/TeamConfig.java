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
 * $Id: TeamConfig.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.launching;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.objectteams.otdt.ui.dialogs.ISearchFilter;
import org.eclipse.objectteams.otdt.ui.dialogs.TeamSelectionDialog;

/**
 * Configuration data for the "Team Activation" tab.
 * 
 * Created on 02.02.2005
 * @author gis
 */
@SuppressWarnings("restriction")
public class TeamConfig extends Composite
{

	private Button _downButton;
	private Button _upButton;
	private Button _removeButton;
	private Button _addButton;
	private OTLaunchConfigurationTab _otLaunchConfigTab;
    private TableViewer _teamList;
    private Button _activeCheckButton;
    
    public TeamConfig(Composite parent, int style, OTLaunchConfigurationTab otlcTab)
    {
        super(parent, style);
        _otLaunchConfigTab = otlcTab;
        
        GridLayout grid = new GridLayout();
        grid.numColumns = 1;
        grid.marginWidth = 3;
        setLayout(grid);
        
        _activeCheckButton = new Button(this, SWT.CHECK);
        _activeCheckButton.setText(OTDTUIPlugin.getResourceString("TeamConfig.activate_checkbox_description")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		_activeCheckButton.setLayoutData(data);
		_activeCheckButton.setFont(this.getFont());
		_activeCheckButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
            	boolean active = isTeamConfigActive();	
            	_teamList.getControl().setEnabled(active);
            	_otLaunchConfigTab.setModified();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        
		Composite mainCompo = new Composite(this, SWT.NULL);
		mainCompo.setLayout(new FormLayout());
		data = new GridData(GridData.FILL_BOTH);
		mainCompo.setLayoutData(data);
		
        _teamList = new TableViewer(mainCompo, SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
        AppearanceAwareLabelProvider labelProvider = new AppearanceAwareLabelProvider();
        labelProvider.setTextFlags(JavaElementLabels.T_POST_QUALIFIED);
        _teamList.setLabelProvider(labelProvider);
        
        _teamList.setContentProvider(new IStructuredContentProvider() {
            @SuppressWarnings("unchecked") // cast to generic list not supported
			public Object[] getElements(Object inputElement)
            {
                if (inputElement instanceof List)
                {
                    List<IType> list = (List<IType>) inputElement;
                    return list.toArray(new IType[list.size()]);
                }
                return null;
            }

            public void dispose() {/*empty*/}
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {/*empty*/}
        });
        
        _teamList.addPostSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                checkEnablement();
            }
        });
        
        final FormData formData = new FormData();
        formData.right = new FormAttachment(100, -162);
        formData.bottom = new FormAttachment(100, -5);
        formData.top = new FormAttachment(0, 5);
        formData.left = new FormAttachment(0, 2);
        Control teamListControl = _teamList.getControl();
        teamListControl.setLayoutData(formData);

        final Composite composite_1 = new Composite(mainCompo, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        composite_1.setLayout(gridLayout);
        final FormData formData_1 = new FormData();
        formData_1.left = new FormAttachment(teamListControl, 5, SWT.RIGHT);
        formData_1.bottom = new FormAttachment(teamListControl, 0, SWT.BOTTOM);
        formData_1.right = new FormAttachment(100, -5);
        formData_1.top = new FormAttachment(teamListControl, 0, SWT.TOP);
        composite_1.setLayoutData(formData_1);
        _addButton = new Button(composite_1, SWT.NONE);
        final GridData gridData = new GridData(GridData.FILL_HORIZONTAL| GridData.VERTICAL_ALIGN_BEGINNING);
        _addButton.setLayoutData(gridData);
        _addButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
               	askAddTeam();
            }
			public void widgetDefaultSelected(SelectionEvent e) {
			    widgetSelected(e);
            }
        });
        _addButton.setText(OTDTUIPlugin.getResourceString("TeamConfig.add_team_button_label")); //$NON-NLS-1$

        _removeButton = new Button(composite_1, SWT.NONE);
        final GridData gridData_1 = new GridData(GridData.FILL_HORIZONTAL| GridData.VERTICAL_ALIGN_BEGINNING);
        _removeButton.setLayoutData(gridData_1);
        _removeButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
            	removeSelected();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        _removeButton.setText(OTDTUIPlugin.getResourceString("TeamConfig.remove_team_button_label")); //$NON-NLS-1$
        
        _upButton = new Button(composite_1, SWT.NONE);
        final GridData gridData_2 = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        gridData_2.horizontalSpan = 2;
        _upButton.setLayoutData(gridData_2);
        _upButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
            	moveSelectedUp();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        _upButton.setText(OTDTUIPlugin.getResourceString("TeamConfig.team_up_button_label")); //$NON-NLS-1$
        
        _downButton = new Button(composite_1, SWT.NONE);
        final GridData gridData_3 = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        _downButton.setLayoutData(gridData_3);
        _downButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
            	moveSelectedDown();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        _downButton.setText(OTDTUIPlugin.getResourceString("TeamConfig.team_down_button_label")); //$NON-NLS-1$
        
    }

    public void dispose()
    {
        super.dispose();
    }

    protected void checkSubclass()
    {
        super.checkSubclass();
    }
    
//	private class WidgetListener implements ModifyListener {
//		public void modifyText(ModifyEvent e) {
//			_otLaunchConfigTab.setModified();
//		}
//		public void widgetDefaultSelected(SelectionEvent e) {
//		}
//	}
//	private WidgetListener fListener = new WidgetListener();
	
    
    private void addTeam(IType chosenTeam) 
    {
    	if (chosenTeam != null) 
    	{
    	    IType teamType = chosenTeam;
    	    if (!(chosenTeam instanceof IOTType))
    	        teamType = OTModelManager.getOTElement(chosenTeam);
    	    
    	    if (teamType != null)
    	    {
    	        // TODO (carp): perform further checks (e.g. must have public default constructor)
				_otLaunchConfigTab.getTeamsModel().add(teamType);
            	_otLaunchConfigTab.setModified();
		        checkEnablement();
		        _teamList.refresh();
    	    }
    	    else
    	    {
    	        MessageDialog.openError(
    	                getShell(), 
    	                OTDTUIPlugin.getResourceString("TeamConfig.error_adding_team_title"),  //$NON-NLS-1$
    	                MessageFormat.format(
    	                        OTDTUIPlugin.getResourceString("TeamConfig.error_adding_team_message"), new Object[] { chosenTeam.getFullyQualifiedName() })); //$NON-NLS-1$
    	    }
    	}
    }
    
    public void clearTeamList() {
        _otLaunchConfigTab.getTeamsModel().clear();
    	checkEnablement();
        _teamList.refresh();
    }

    public boolean isTeamConfigActive() {
    	return _activeCheckButton.getSelection();
    }
    
    public void setActive(boolean active) {
    	_activeCheckButton.setSelection(active);
    	_teamList.getControl().setEnabled(active);
    }

    private IType chooseTeamFromClasspath(IProject project) {
    	if (project == null || !project.exists())
    		return null;
    	
    	IJavaProject java_project = JavaCore.create(project);
    	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{ java_project});

    	TeamSelectionDialog dialog = new TeamSelectionDialog(
    	        TeamConfig.this.getShell(), 
    	        _otLaunchConfigTab.getRunnableContext(), 
    	        scope);

		dialog.setTitle(OTDTUIPlugin.getResourceString("TeamConfig.choose_team_dialog_title")); //$NON-NLS-1$
		dialog.setMessage(OTDTUIPlugin.getResourceString("TeamConfig.choose_team_dialog_description")); //$NON-NLS-1$
		dialog.setFilter("*"); //$NON-NLS-1$
		dialog.addSearchFilter(new ISearchFilter() {
            public IOTType[] filterTypes(IOTType[] types)
            {
                List<IType> teamsModel = _otLaunchConfigTab.getTeamsModel();
                List<IOTType> result = new ArrayList<IOTType>(types.length);
                for (int i = 0; i < types.length; i++)
                {
                    IOTType type = types[i];
                    // hide abstract teams and those already in the list
                    if (!Flags.isAbstract(type.getFlags()) && !teamsModel.contains(type))
                        result.add(type);
                }
                
                return result.toArray(new IOTType[result.size()]);
            }
        });

	
		if (dialog.open() == Window.OK) {
			IType selectedType = (IType) dialog.getFirstResult();
			return selectedType;
		}
    		
		return null;
	}

    private void removeSelected()
    {
        IStructuredSelection selection = (IStructuredSelection)_teamList.getSelection();
        for (@SuppressWarnings("rawtypes") Iterator iterator = selection.iterator(); iterator.hasNext();)
        {
            IType selectedType = (IType) iterator.next();
            _otLaunchConfigTab.getTeamsModel().remove(selectedType);
        }
        _otLaunchConfigTab.setModified();
        checkEnablement();
        _teamList.refresh();
    }

    private void askAddTeam()
    {
       	IProject project = _otLaunchConfigTab.getProject();
    	if (project == null)
    	{
    	    MessageDialog.openInformation(getShell(), OTDTUIPlugin.getResourceString("TeamConfig.project_not_set_title"), OTDTUIPlugin.getResourceString("TeamConfig.project_not_set_message")); //$NON-NLS-1$ //$NON-NLS-2$
    	    return;
    	}

    	// TODO: check if the selected team has a default constructor, otherwise give an error message.
        IType chosenTeam = chooseTeamFromClasspath(project);
        if (chosenTeam != null)
        {
            addTeam(chosenTeam);
        }
    }

    private void moveSelectedUp()
    {
        List<IType> teamModel = _otLaunchConfigTab.getTeamsModel();
        
        final int currentIndex = getTeamSelectionIndex();
        if (currentIndex > 0) {
        	IType currentTeam = teamModel.get(currentIndex);
        	IType aboveTeam = teamModel.get(currentIndex - 1);
        	teamModel.set(currentIndex - 1, currentTeam);
        	teamModel.set(currentIndex, aboveTeam);
        	_teamList.refresh();
        	_otLaunchConfigTab.setModified();
        	
        	checkMoveEnablement();
        }
    }

    private void moveSelectedDown()
    {
        List<IType> teamModel = _otLaunchConfigTab.getTeamsModel();
        
        final int currentIndex = getTeamSelectionIndex();
        if (currentIndex < _otLaunchConfigTab.getTeamsModel().size() - 1) {
        	IType currentTeam = teamModel.get(currentIndex);
        	IType belowTeam = teamModel.get(currentIndex + 1);
        	teamModel.set(currentIndex + 1, currentTeam);
        	teamModel.set(currentIndex, belowTeam);
        	_teamList.refresh();
        	_otLaunchConfigTab.setModified();
        	
        	checkMoveEnablement();
        }
    }

    
    void checkEnablement()
    {
        checkMoveEnablement();
        checkActiveEnablement();
        
        if (_teamList.getSelection().isEmpty())
    	{
    	    _removeButton.setEnabled(false);
    	}
        else
        {
    	    _removeButton.setEnabled(true);
        }
    }

    private void checkActiveEnablement()
    {
        boolean enable = !_otLaunchConfigTab.getTeamsModel().isEmpty();
        _activeCheckButton.setEnabled(enable);
    }
    
    private void checkMoveEnablement()
    {
        final int index = getTeamSelectionIndex();

        if (index == -1)
    	{
    	    _upButton.setEnabled(false);
    	    _downButton.setEnabled(false);
    	    return;
    	}
        
        final int count = _otLaunchConfigTab.getTeamsModel().size();
        boolean canMove = count >= 2;

        if (index == 0)
        {
    	    _upButton.setEnabled(false);
    	    _downButton.setEnabled(canMove);
    	}
        else if (index == count - 1)
        {
    	    _downButton.setEnabled(false);
    	    _upButton.setEnabled(canMove);
        }
        else
        {
		    _upButton.setEnabled(canMove);
		    _downButton.setEnabled(canMove);
        }
    }

    private int getTeamIndex(Object element)
    {
        return _otLaunchConfigTab.getTeamsModel().indexOf(element);
    }
    
    private int getTeamSelectionIndex()
    {
        IStructuredSelection selection = (IStructuredSelection) _teamList.getSelection();
        if (selection == null || selection.isEmpty())
            return -1;
        
        return getTeamIndex(selection.getFirstElement());
    }

    public void setTeamInput(List<IType> teamModel)
    {
        _teamList.setInput(teamModel);
    }
}
