/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.migration;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.objectteams.otdt.internal.pde.ui.OTPDEUIPlugin;

import base org.eclipse.core.internal.events.BuildManager;

/**
 * This team provides diagnostics to detect old configuration details in projects
 * using the obsolete prefix "org.objectteams" instead of "org.eclipse.objectteams":
 * - OTJavaNature
 * - OTJBuilder
 */
@SuppressWarnings("restriction")
public team class OTJProjectMigration {
	
	public static final String OLD_OT_BUILDER = "org.objectteams.otdt.builder.OTJBuilder"; //$NON-NLS-1$
	public static final String OLD_OT_NATURE = "org.objectteams.otdt.OTJavaNature"; //$NON-NLS-1$
	
	protected class BuildManager playedBy BuildManager {

		checkBuilder <- before getBuilder;

		private void checkBuilder(IProject project,	ICommand command) {
			// remove old markers:
			MigrationMarkers.removeMarkers(project, 
									new int[] {MigrationMarkers.PROBLEM_ID_NATURE, MigrationMarkers.PROBLEM_ID_BUILDER});
			// check builder:
			if (OLD_OT_BUILDER.equals(command.getBuilderName()))
				MigrationMarkers.addProblemMarker(project, 
									Messages.ProjectMigration_old_builder_message, 
									MigrationMarkers.PROBLEM_ID_BUILDER);
			// check nature:
			try {
				if (project.hasNature(OLD_OT_NATURE))
					MigrationMarkers.addProblemMarker(project, 
									Messages.ProjectMigration_old_nature_message, 
									MigrationMarkers.PROBLEM_ID_NATURE);
			} catch (CoreException ce) {
				OTPDEUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, OTPDEUIPlugin.PLUGIN_ID, 
												    "Can't read project nature", ce)); //$NON-NLS-1$
			}
		}		
	}
}
