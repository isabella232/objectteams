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
 * $Id: UpdateTeamViewAction.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.objectteams.otdt.debug.OTDebugElementsContainer;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugImages;
import org.eclipse.objectteams.otdt.debug.ui.views.TeamView;

public class UpdateTeamViewAction extends Action
{
	private TeamView _teamView;

	public UpdateTeamViewAction(TeamView teamView)
	{
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		setToolTipText(ActionMessages.UpdateTeamViewAction_permanently_update_tooltip);
		setDescription(ActionMessages.UpdateTeamViewAction_permanently_update_description);
		setImageDescriptor(OTDebugImages.get(OTDebugImages.UPDATE_TEAM_VIEW_ACTION));
		_teamView = teamView;
	}
	
	@Override
	public void run()
	{
		_teamView.setUpdatePermanently(isChecked());
		OTDebugElementsContainer container = (OTDebugElementsContainer)_teamView.getViewer().getInput();
		if(isChecked())
		{
			_teamView.getViewer().setInput(container);
		}
	}
}