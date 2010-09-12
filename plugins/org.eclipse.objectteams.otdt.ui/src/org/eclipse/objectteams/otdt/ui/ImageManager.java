/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ImageManager.java 23434 2010-02-03 23:52:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * Tool class to access plugin images
 * 
 * @author kaiser
 * @version $Id: ImageManager.java 23434 2010-02-03 23:52:31Z stephan $
 */
public class ImageManager implements ImageConstants
{
    private static final String[] _pluginIcons =
		{
			CALLOUTBINDING_IMG, 
			CALLINBINDING_REPLACE_IMG, 
			CALLINBINDING_BEFORE_IMG,
			CALLINBINDING_AFTER_IMG, 
			CALLINMETHOD_IMG,
			BOUNDROLE_IMG,
			GUARD_IMG,
			BASEGUARD_IMG,
			ROLECLASS_IMG, 
			ROLECLASS_PROTECTED_IMG,
			ROLE_ALT_IMG,
			TEAM_IMG,
			TEAM_ROLE_IMG,
			TEAM_ROLE_PROTECTED_IMG,
			TEAM_ALT_IMG,
			ROLE_OVR,
			TEAM_OVR,
			TEAM_ROLE_OVR,
			HIDE_TEAMPACKAGE,
			NEW_OT_PROJECT,
			NEW_TEAM,
			NEW_ROLE
		};

	private static ImageManager _singleton;

	/**
	 * Avoid multiple instances
	 */
	protected ImageManager()
	{
	}

	/**
	 * The only way to access plugin images
	 * @return PluginImages instance
	 */
	public static ImageManager getSharedInstance()
	{
		if (_singleton == null)
		{
			_singleton = new ImageManager();
		} 
		
		return _singleton;
	}

	/**
	 * Add all plugin specific images to given ImageRegistry
	 * @param reg - ImageRegistry to use 
	 */
	public void registerPluginImages(ImageRegistry reg)
	{
		ImageDescriptor descr;
		
		try
		{
			String[] pluginIcons = pluginIcons();
			URL baseURL = new URL(getInstallLocation(), IMG_PATH);
			for (int idx = 0; idx < pluginIcons.length; idx++)
			{
				descr = ImageDescriptor.createFromURL(
						new URL(baseURL, pluginIcons[idx]));
				reg.put(pluginIcons[idx], descr);
			}
		}
		catch (MalformedURLException ex)
		{
			OTDTUIPlugin.getExceptionHandler().logException(ex);
		}
	}

	private URL getInstallLocation() {
		return getPlugin().getBundle().getEntry("/"); //$NON-NLS-1$
	}

	/** Override to specify which plugin we are serving. */
	protected AbstractUIPlugin getPlugin() {
		return OTDTUIPlugin.getDefault();
	}
	
	/** Override to specify which icons should be registered. */
	protected String[] pluginIcons() {
		return _pluginIcons;
	}

	/**
	 * Setup images for different action states
	 * @param action - action to initialise
	 * @param imgKey - image name to use 
	 */	
	public void setActionImageDescriptors(IAction action, String imgName)
	{
		try
		{
			ImageDescriptor descr;
			URL             baseURL = new URL(getInstallLocation(), IMG_PATH); 

			// set different types image descriptors according to action state
			descr   = ImageDescriptor.createFromURL(new URL(baseURL, DEFAULT_PATH+imgName));
			action.setImageDescriptor( descr );

			descr   = ImageDescriptor.createFromURL(new URL(baseURL, HOVER_PATH+imgName));
			action.setHoverImageDescriptor(descr);

			descr   = ImageDescriptor.createFromURL(new URL(baseURL, DISABLED_PATH+imgName));
			action.setDisabledImageDescriptor(descr);
		}
		catch (MalformedURLException ex)
		{
			OTDTUIPlugin.getExceptionHandler().logException(ex);
		}
	}
	
	/**
	 * Get image from this Plugin's ImageRegistry
	 * @param imageKey - image key from the set returned by pluginIcons().
	 * @return image object
	 */
	public Image get(String imageKey)
	{
		// redirect to image registry
		return getPlugin().getImageRegistry().get(imageKey);		
	}

	
	public ImageDescriptor getDescriptor(String imageKey)
	{
		return getPlugin().getImageRegistry().getDescriptor(imageKey);
	}

}
