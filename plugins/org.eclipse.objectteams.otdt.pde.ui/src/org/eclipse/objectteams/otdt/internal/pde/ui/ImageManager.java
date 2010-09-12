/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: DebugUIDialogAdaptor2.java 18886 2008-08-17 14:37:14Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.pde.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * Provide cached access to images of this plugin.
 * 
 * @author stephan
 * @since 1.0.0
 */
public class ImageManager extends org.eclipse.objectteams.otdt.ui.ImageManager {

	private static ImageManager _singleton;

	/**
	 * Avoid multiple instances
	 */
	protected ImageManager() {}

	/**
	 * The only way to access plugin images
	 * @return PluginImages instance
	 */
	public static ImageManager getSharedInstance() {
		if (_singleton == null)
			_singleton = new ImageManager();
		
		return _singleton;
	}
	
	// overriding causes paths to be resolved relative to this plug-in.
	@Override
	protected AbstractUIPlugin getPlugin() {
		return OTPDEUIPlugin.getDefault();
	}
	
	// specify which icons to register:
	@Override
	protected String[] pluginIcons() {
		return new String[]{ OTNewPluginProjectWizard.NEW_OTPDE_PROJECT, PackageExplorerAdaptor.PLUGIN_FORCED_EXPORTS };
	}
}
