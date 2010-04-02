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
 * $Id: SortTeamAction.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.objectteams.otdt.debug.IOTDTDebugPreferenceConstants;
import org.eclipse.objectteams.otdt.debug.OTDebugElementsContainer;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugImages;
import org.eclipse.objectteams.otdt.debug.ui.views.TeamView;
import org.eclipse.objectteams.otdt.debug.ui.views.TeamViewMessages;

/**
 * @author ike
 * This Action sorts the teams in TeamMonitor.
 * 
 * $Id: SortTeamAction.java 23432 2010-02-03 23:13:42Z stephan $
 */
public class SortTeamAction extends Action
{
	private TeamView _teamView;
	private String _sortMode; 

	public SortTeamAction(TeamView teamView, String sortMode)
	{
		super("", AS_RADIO_BUTTON); //$NON-NLS-1$

		if (sortMode == IOTDTDebugPreferenceConstants.TEAMS_BY_ACTIVATION_TIME)
		{
			setText(TeamViewMessages.SortTeamByActivation_0);
			setImageDescriptor(OTDebugImages.get(OTDebugImages.SORT_TEAMS_BY_ACTIVATION_TIME));
		}

		if (sortMode == IOTDTDebugPreferenceConstants.TEAMS_BY_ACTIVATION_ORDER)
		{
			setText(TeamViewMessages.SortTeamByActivation_1);
			setImageDescriptor(OTDebugImages.get(OTDebugImages.SORT_TEAMS_BY_ACTIVATION_ORDER));
		}

		if (sortMode == IOTDTDebugPreferenceConstants.TEAMS_BY_INSTANTIATION)
		{
			setText(TeamViewMessages.SortTeamByInstantiation_0);
			setImageDescriptor(OTDebugImages.get(OTDebugImages.SORT_TEAMS_BY_INSTANTIATION));
		}

		if (sortMode == IOTDTDebugPreferenceConstants.TEAMS_BY_NAME)
		{
			setText(TeamViewMessages.SortTeamByName_0);  
			setImageDescriptor(OTDebugImages.get(OTDebugImages.SORT_TEAMS_BY_NAME));  
		}

		_teamView = teamView;
		_sortMode = sortMode;
		setChecked(sortMode == _teamView.getDefaultSortMode());
	}

	public void run()
	{
		if (isChecked())
		{
			_teamView.setSortMode(getSortMode());
			OTDebugElementsContainer container = (OTDebugElementsContainer)_teamView.getViewer().getInput();
			container.setSortMode(getSortMode());
			_teamView.getViewer().setInput(container);
		}
	}

	private String getSortMode()
	{
		return _sortMode;
	}
}
