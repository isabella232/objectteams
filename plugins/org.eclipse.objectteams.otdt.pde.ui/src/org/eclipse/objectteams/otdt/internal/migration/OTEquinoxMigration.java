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

import org.eclipse.core.resources.IFile;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.builders.IHeader;
import org.w3c.dom.Element;

import base org.eclipse.pde.internal.core.builders.BundleErrorReporter;
import base org.eclipse.pde.internal.core.builders.ExtensionsErrorReporter;

/**
 * Hook into existing validations as to find project setups needing an update for
 * the migration org.objectteams -> org.eclipse.objectteams.
 * 
 * Handles: MANIFEST.MF and plugin.xml.
 */
@SuppressWarnings("restriction")
public team class OTEquinoxMigration 
{
	
	public static final String OLD_OTEQUINOX_NAME = "org.objectteams.otequinox"; //$NON-NLS-1$
	public static final String OLD_OTEQUINOX_EXTENSIONPOINT = "org.objectteams.otequinox.aspectBindings"; //$NON-NLS-1$

	/**
	 * Checks whether a RequireBundle directive mentions obsolete org.objectteams.otequinox
	 */
	@SuppressWarnings("decapsulation")
	protected class BundleErrorReporter playedBy BundleErrorReporter 
	{		
		int getPackageLine(IHeader header, ManifestElement element) 
		-> int getPackageLine(IHeader header, ManifestElement element);

		IPluginModelBase getFModel() -> get IPluginModelBase fModel;
		
		removeMarkers <- before validateRequireBundle;
		
		void removeMarkers() {
			// remove old markers:
			MigrationMarkers.removeMarkers(getFModel().getUnderlyingResource(), 
										   new int[] {MigrationMarkers.PROBLEM_ID_OTEQUINOX_BUNDLE});
		}
		
		validateBundleName <- before validateBundleVersionAttribute;
		
		void validateBundleName(IHeader header, ManifestElement element) 
		{
			if (OTEquinoxMigration.OLD_OTEQUINOX_NAME.equals(element.getValue())) {
				MigrationMarkers.addProblemMarker(
						getFModel().getUnderlyingResource(), 
						Messages.OTEquinoxMigration_old_otequinox_bundle_message,
						MigrationMarkers.PROBLEM_ID_OTEQUINOX_BUNDLE,
						getPackageLine(header, element));
			}
		}
	}

	/**
	 * Check whether an extension refers to aspectBinding extension point by its old obsolete name.
	 */
	@SuppressWarnings("decapsulation")
	protected class ExtensionAnalyzer playedBy ExtensionsErrorReporter 
	{	
		IFile getFFile() -> get IFile fFile;

		int getLine(Element element) -> int getLine(Element element);
		
		removeMarkers <- before validateContent;
		
		void removeMarkers() {			
			MigrationMarkers.removeMarkers(getFFile(), 
										   new int[] {MigrationMarkers.PROBLEM_ID_EXTENSIONPOINT});
		}
		
		void checkAspectBinding(Element element) <- after void validateExtension(Element element);

		protected void checkAspectBinding(Element element) 
		{
			Object pointID = element.getAttribute("point"); //$NON-NLS-1$
			if (OLD_OTEQUINOX_EXTENSIONPOINT.equals(pointID))
				MigrationMarkers.addProblemMarker(getFFile(), 
						Messages.OTEquinoxMigration_old_otequinox_extensionpoint_message,
						MigrationMarkers.PROBLEM_ID_EXTENSIONPOINT, 
						getLine(element));
		}
	}
}
