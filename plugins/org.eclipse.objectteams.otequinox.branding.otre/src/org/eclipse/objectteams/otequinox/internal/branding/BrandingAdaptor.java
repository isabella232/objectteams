/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id:AboutAdaptor.java 15202 2007-01-28 11:30:50Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal.branding;

import java.util.HashSet;

import org.eclipse.objectteams.otequinox.TransformerPlugin;
import org.eclipse.objectteams.otequinox.branding.Activator;
import org.eclipse.swt.graphics.Image;

import base org.eclipse.ui.internal.about.AboutBundleData;
import base org.eclipse.ui.internal.about.AboutPluginsPage.BundleTableLabelProvider;

/**
 * This aspect adapts the branding of the Eclipse workbench by
 * - adapting the about plug-ins dialog, displaying as a self-application
 *   which plug-ins have been adapted by an OT/J aspect.
 * 
 * @author stephan
 * @version $Id:AboutAdaptor.java 15202 2007-01-28 11:30:50Z stephan $
 */
@SuppressWarnings("restriction")
public team class BrandingAdaptor 
{
	public BrandingAdaptor() {
	}

	/** Append OT-adaptations to the version number of any adapted bundle. */
	protected class AboutBundleAdaptor playedBy AboutBundleData 
		base when (isAdaptedBaseBundle(base))
	{
		callin String getVersion() 
		{
			String adaptationString = ""; //$NON-NLS-1$
			HashSet<String> reportedPlugins = new HashSet<String>(); 
			adaptationString = BrandingMessages.BrandingAdaptor_OT_adapted_by;
			for (String element : TransformerPlugin.getDefault().getAdaptingAspectPlugins(getID())) 
			{
				if (!reportedPlugins.contains(element))
					adaptationString = adaptationString+"\n* "+element; //$NON-NLS-1$
				reportedPlugins.add(element);
			}
			return base.getVersion() + adaptationString;
		}
		getVersion <- replace getVersion;
		
		String  getID() 			 -> String  getId();
		boolean isSigned()			 -> boolean isSigned();
		boolean isSignedDetermined() -> boolean isSignedDetermined();		
	}
	
	/** 
	 * For all adapted bundles replace the singed/unsigned icons with
	 * a variant that is decorated with a aspect binding icon.
	 */
	protected class IconAdaptor playedBy BundleTableLabelProvider {

		Image getAdaptedSigningColumnImage(AboutBundleAdaptor data) <- replace Image getColumnImage(Object element, int columnIndex)
				base when (columnIndex == 0 && isAdaptedBaseBundle(element))
				with { data <- (AboutBundleData)element }

		@SuppressWarnings("basecall")
		callin Image getAdaptedSigningColumnImage(AboutBundleAdaptor data) {
			if (data.isSignedDetermined()) {
				if (data.isSigned())
					return Activator.getDefault().getImage(Activator.IMG_SIGNED_ADAPTED);
				else
					return Activator.getDefault().getImage(Activator.IMG_UNSIGNED_ADAPTED);
			}
			return base.getAdaptedSigningColumnImage(data);
		}		
	}

	/** Is 'element' an AboutBundleData object representing a bundle that is adapted by an OT/Equinox bundle? */
	public static boolean isAdaptedBaseBundle(Object element) {
		// use qualified name, because AboutBundleData is a role's base class thus considered alien to this team:
		if (element instanceof org.eclipse.ui.internal.about.AboutBundleData) {
			String symbolicName = ((org.eclipse.ui.internal.about.AboutBundleData)element).getId();
			return TransformerPlugin.getDefault().isAdaptedBasePlugin(symbolicName);
		}
		return false;
	}
}