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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.objectteams.otdt.internal.pde.ui.OTPDEUIPlugin;

/** Handle markers for diagnostics relating to the migration org.objectteams -> org.eclipse.objectteams. */
public class MigrationMarkers {
	
	private static final String PROBLEM_ID = "problemID"; //$NON-NLS-1$
	public static final int PROBLEM_ID_BUILDER = 			1;
	public static final int PROBLEM_ID_NATURE = 			2;
	public static final int PROBLEM_ID_OTEQUINOX_BUNDLE = 	3;
	public static final int PROBLEM_ID_EXTENSIONPOINT = 	4;

	private static final String SOURCE_ID_OTDT = "OTDT"; //$NON-NLS-1$
	
	/** 
	 * Add a marker.
	 * @param 	the resource to which the marker should be added
	 * @param   the marker message
	 * @param 	problemID one of {@link #PROBLEM_ID_BUILDER}, {@link #PROBLEM_ID_NATURE}, 
	 * 			{@link #PROBLEM_ID_OTEQUINOX_BUNDLE} or  {@link #PROBLEM_ID_EXTENSIONPOINT}. 
	 */
	public static void addProblemMarker(IResource resource, String message, int problemID) {
		addProblemMarker(resource, message, problemID, -1);
	}
	
	/** 
	 * Add a marker.
	 * @param 	the resource to which the marker should be added
	 * @param   the marker message
	 * @param 	problemID one of {@link #PROBLEM_ID_BUILDER}, {@link #PROBLEM_ID_NATURE}, 
	 * 			{@link #PROBLEM_ID_OTEQUINOX_BUNDLE} or  {@link #PROBLEM_ID_EXTENSIONPOINT}. 
	 * @param   lineNo line number within the resource where the error occurred
	 */
	public static void addProblemMarker(IResource resource, String message, int problemID, int lineNo) {
		try {
			IMarker marker = resource.createMarker(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER);
			marker.setAttributes(
				new String[] {
					IMarker.MESSAGE, 
					IMarker.SEVERITY, 
					IJavaModelMarker.CATEGORY_ID, 
					MigrationMarkers.PROBLEM_ID,
					IMarker.SOURCE_ID},
				new Object[] {
					message,
					new Integer(IMarker.SEVERITY_ERROR),
					new Integer(CategorizedProblem.CAT_BUILDPATH),
					new Integer(problemID),
					MigrationMarkers.SOURCE_ID_OTDT
				}
			);
			if (lineNo > -1)
				marker.setAttribute(IMarker.LINE_NUMBER, new Integer(lineNo));
		} catch (CoreException ce) {
			OTPDEUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, OTPDEUIPlugin.PLUGIN_ID, 
												    "Error setting problem marker", ce)); //$NON-NLS-1$
		}
	}

	/**
	 * Remove all migration markers specified by the arguments:
	 * @param resource only markers of this resource
	 * @param handledIDs only markers with a {@link #PROBLEM_ID} from this set 
	 */
	public static void removeMarkers(IResource resource, int[] handledIDs) {
		try {
			for (IMarker existingMarker : resource.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO)) {
				if (   existingMarker.getAttribute(IJavaModelMarker.CATEGORY_ID, 0) == CategorizedProblem.CAT_BUILDPATH
					&& MigrationMarkers.SOURCE_ID_OTDT.equals(existingMarker.getAttribute(IMarker.SOURCE_ID))) 
				{
					int id = existingMarker.getAttribute(PROBLEM_ID, 0);
					for (int handledID : handledIDs)
						if (handledID == id) {
							existingMarker.delete();
							break;
						}
				}
			}
		} catch (CoreException ce) {
			OTPDEUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, OTPDEUIPlugin.PLUGIN_ID, 
												    "Error deleting problem markers", ce)); //$NON-NLS-1$
		}
	}

	public static int getProblemID(IMarker marker) {
		return marker.getAttribute(PROBLEM_ID, 0);
	}
}
