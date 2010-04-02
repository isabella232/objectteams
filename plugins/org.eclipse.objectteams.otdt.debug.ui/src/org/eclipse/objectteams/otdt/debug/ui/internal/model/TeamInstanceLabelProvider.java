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
 * $Id: TeamInstanceLabelProvider.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.internal.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnPresentation;
import org.eclipse.debug.internal.ui.model.elements.VariableLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.objectteams.otdt.debug.OTDebugElementsContainer;
import org.eclipse.objectteams.otdt.debug.TeamInstance;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugImages;

/**
 * Labels (text/image) for team instances in the TeamView.
 * 
 * @author stephan
 * @since 1.1.2
 */
public class TeamInstanceLabelProvider extends VariableLabelProvider 
{
	@Override
	protected String getLabel(TreePath elementPath, IPresentationContext context, String columnId) 
			throws CoreException 
	{
//{ObjecgtTeams: new implementation, re-using only getColumnText():
		Object element = elementPath.getLastSegment();
		if (element instanceof TeamInstance) {	
			IVariable variable= (IVariable) element;
			if (isNameColumn(columnId))
				return variable.getReferenceTypeName();
			else
				return getColumnText(variable, variable.getValue(), context, columnId);
		}
		return "<unknown element>"; //$NON-NLS-1$ // should never be seen
// SH}
	}
	@Override
	protected ImageDescriptor getImageDescriptor(
			TreePath elementPath, IPresentationContext presentationContext, String columnId)
			throws CoreException 
	{
		// completely new implementation for the OTDT:
		if (isNameColumn(columnId)) {
			TeamInstance teamInstance = (TeamInstance) elementPath.getLastSegment();
			OTDebugElementsContainer _otDebugElementsContainer = (OTDebugElementsContainer) teamInstance.getLaunch().getAdapter(OTDebugElementsContainer.class);
	
			if (_otDebugElementsContainer != null && _otDebugElementsContainer.getTeamInstance(teamInstance) != null)
			{
				int activationState = teamInstance.getActivationState(_otDebugElementsContainer.getContext());
				return getImageForState(activationState);
			}
			
			//DefaultImage for Teams is inactive-Image
			return getImageForState(0);
		} 
		return null;
	}

	private boolean isNameColumn(String columnId) {
		return columnId == null
		|| VariableColumnPresentation.COLUMN_VARIABLE_NAME.equals(columnId);
	}
	
    private ImageDescriptor getImageForState(int activationState)
    {
        switch (activationState)
        {
        case TeamInstance.IS_INACTIVE:
            return OTDebugImages.get(OTDebugImages.TEAM_INACTIVATED);
        case TeamInstance.IS_IMPLICITACTIVE:
        	return OTDebugImages.get(OTDebugImages.TEAM_IMPLICIT_ACTIVATED);            
        case TeamInstance.IS_ACTIVE:
        	return OTDebugImages.get(OTDebugImages.TEAM_ACTIVATED);
        default:
        	return OTDebugImages.get(OTDebugImages.TEAM_INACTIVATED);
        }
    } 
}
