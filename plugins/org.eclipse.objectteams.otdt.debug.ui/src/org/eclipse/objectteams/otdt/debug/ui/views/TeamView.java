/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamView.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 * IBM Corporation - copies of individual methods from super class.
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.views;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.objectteams.otdt.debug.IOTDTDebugPreferenceConstants;
import org.eclipse.objectteams.otdt.debug.OTDebugElementsContainer;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugUIPlugin;
import org.eclipse.objectteams.otdt.debug.ui.internal.actions.ChangeTeamActivationAction;
import org.eclipse.objectteams.otdt.debug.ui.internal.actions.SortTeamAction;
import org.eclipse.objectteams.otdt.debug.ui.internal.actions.UpdateTeamViewAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;

/**
 * The TeamView aka "Team Monitor" for observing instantiation
 * and (de)activation of teams.
 * 
 * This class was originally developed against Eclipse <= 3.2.
 * For changes in VariablesView see 
 * 		https://bugs.eclipse.org/bugs/show_bug.cgi?id=153500
 * 
 * @author gis
 * 
 * $Id: TeamView.java 23432 2010-02-03 23:13:42Z stephan $
 */
public class TeamView extends VariablesView implements ILaunchesListener2
{
	private static final String ACTION_ACTIVATE_TEAM = "ActivateTeam"; //$NON-NLS-1$
	private static final String ACTION_DEACTIVATE_TEAM = "DeactivateTeam"; //$NON-NLS-1$
	private static final String ACTION_UPDATE_TEAMVIEW = "action.update.teamview"; //$NON-NLS-1$
	private String _sortMode;
	private boolean _updatePermantently = false;
	
	private static boolean DEBUG= false;

	public TeamView()
	{
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		_sortMode = getDefaultSortMode();
	}

	@Override
	protected String getPresentationContextId() {
		return OTDebugUIPlugin.TEAM_VIEW_ID;
	}
	
	public String getDefaultSortMode()
	{
		return IOTDTDebugPreferenceConstants.TEAMS_BY_ACTIVATION_ORDER;
	}

	@Override // COPY_AND_PASTE from super, edited:
	public void contextActivated(ISelection selection)
	{
		if (DEBUG) System.out.println("TV: contextActivated: "+selection); //$NON-NLS-1$
		
		if (!isAvailable() || !isVisible()) {
			return;
		}

		if (selection instanceof IStructuredSelection) {
//ObjectTeams: if there are multiselected DebugElements - show nothing
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			if(structuredSelection.size() == 1)
  // orig:				
				setViewerInput(structuredSelection.getFirstElement());
  // :giro
			else
				setViewerInput(null);
// carp+SH}
		}
		showViewer();

		updateAction(VARIABLES_FIND_ELEMENT_ACTION);
		updateAction(FIND_ACTION);
	}
	
	@Override // COPY_AND_PASTE from super, edited:
	protected void setViewerInput(Object context)
	{
		if (DEBUG) System.out.println("TV: setViewerInput: "+context); //$NON-NLS-1$

//{ObjectTeams
		if(!isSuspended(context) && !_updatePermantently)
			return;
// SH}
		
		if (context == null) {
			// Clear the detail pane
//{ObjectTeams: workaround invisible field fDetailPane:
	/* orig:
			fDetailPane.display(null);
	  :giro */
			refreshDetailPaneContents();
// SH}
		}
		
		Object current= getViewer().getInput();
		
		if (current == null && context == null) {
			return;
		}
		
//{ObjectTeams:
		boolean hasContextChanged= false;
		if ((context instanceof IStackFrame) || (context instanceof IThread) || (context instanceof IDebugTarget))
		{
			ILaunch launch = ((IDebugElement)context).getLaunch();
			OTDebugElementsContainer newInput = (OTDebugElementsContainer) launch.getAdapter(OTDebugElementsContainer.class);
			if (newInput != null) { // null for non-OT launches!
				hasContextChanged= newInput.setContext((IDebugElement)context);
				newInput.setSortMode(_sortMode);
				context= newInput;
			}
		}
// SH}
		// OT: first condition added:
		if (!hasContextChanged && current != null && current.equals(context)) {
			return;
		}
		
		showViewer();
		getViewer().setInput(context);
	}

	private boolean isSuspended(Object context)
	{
		if (context instanceof OTDebugElementsContainer)
			return ((OTDebugElementsContainer)context).isSuspended();
		return true;
	}

	public IJavaThread getSelectedThread() {
		Object input= getViewer().getInput();
		if (input instanceof OTDebugElementsContainer)
			return ((OTDebugElementsContainer)input).getContextThread();
		return null; // no input!
	}
	
	@Override
	public void modelChanged(IModelDelta delta, IModelProxy proxy) {
		throw new RuntimeException("TeamView.modelChanged() should not be called"); //$NON-NLS-1$
	}
	
	public void launchesTerminated(ILaunch[] launches)
	{
		//clear TeamView
		;
	}

	public void launchesRemoved(ILaunch[] launches) {}
	public void launchesAdded(ILaunch[] launches) {}
	public void launchesChanged(ILaunch[] launches) {}

	public void dispose()
	{
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	public Viewer createViewer(Composite parent)
	{
		TreeModelViewer variablesViewer = (TreeModelViewer) super.createViewer(parent);
		variablesViewer.removeModelChangedListener(this); // only register the proxy.
		return variablesViewer;
	}
	
	protected void createActions() 
	{
		super.createActions();
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager viewMenu = actionBars.getMenuManager();
		createSortActions(viewMenu);
		// button:
		setAction(ACTION_UPDATE_TEAMVIEW, new UpdateTeamViewAction(this));
		
		setAction(ACTION_ACTIVATE_TEAM, new ChangeTeamActivationAction(this, true));
		setAction(ACTION_DEACTIVATE_TEAM, new ChangeTeamActivationAction(this, false));
	} 	

	private void createSortActions(IMenuManager viewMenu)
	{
		final SortTeamAction sortAction1 = new SortTeamAction(this, IOTDTDebugPreferenceConstants.TEAMS_BY_ACTIVATION_ORDER);
		final SortTeamAction sortAction2 = new SortTeamAction(this, IOTDTDebugPreferenceConstants.TEAMS_BY_ACTIVATION_TIME);
		final SortTeamAction sortAction3 = new SortTeamAction(this, IOTDTDebugPreferenceConstants.TEAMS_BY_INSTANTIATION);
		final SortTeamAction sortAction4 = new SortTeamAction(this, IOTDTDebugPreferenceConstants.TEAMS_BY_NAME);
		
		final MenuManager layoutSubMenu = new MenuManager(TeamViewMessages.TeamView_0);
		layoutSubMenu.setRemoveAllWhenShown(true);
		layoutSubMenu.add(sortAction1);
		layoutSubMenu.add(sortAction2);
		layoutSubMenu.add(sortAction3);
		layoutSubMenu.add(sortAction4);
		viewMenu.add(layoutSubMenu);
		viewMenu.add(new Separator());

		layoutSubMenu.addMenuListener(new IMenuListener()
		{
			public void menuAboutToShow(IMenuManager manager)
			{
				layoutSubMenu.add(sortAction1);
				layoutSubMenu.add(sortAction2);
				layoutSubMenu.add(sortAction3);
				layoutSubMenu.add(sortAction4);
			}
		});
	}

	@Override
	protected void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		menu.add(getAction(ACTION_ACTIVATE_TEAM)); 
		menu.add(getAction(ACTION_DEACTIVATE_TEAM));
	}
	
	protected String getToggleActionLabel()
	{
		return TeamViewMessages.TeamView_1; 
	}

	public void setSortMode(String sortMode)
	{
		_sortMode = sortMode;
	}

	protected void configureToolBar(IToolBarManager tbm)
	{
		super.configureToolBar(tbm);
		tbm.add(new Separator(IDebugUIConstants.EMPTY_REGISTER_GROUP));		
		tbm.add(new Separator(IDebugUIConstants.REGISTER_GROUP));
		tbm.add(getAction(ACTION_UPDATE_TEAMVIEW));
	}

	public void setUpdatePermanently(boolean updatePermanently)
	{
		_updatePermantently = updatePermanently;
		
		//by default clear viewer
		if(!updatePermanently)
			setViewerInput(null);

	}
}
