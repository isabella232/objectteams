/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Activator.java 23461 2010-02-04 22:10:39Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.branding;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * This bundle activator is only responsible for managing two icons,
 * see {@link #getImage(String)}.
 * 
 * @author stephan
 * @since 1.4.0
 */
public class Activator extends AbstractUIPlugin {

	/** "Signed" icon with aspect binding decoration. */
	public static final String IMG_SIGNED_ADAPTED = "org.eclipse.objectteams.otequinox.branding.signed_adapted";     //$NON-NLS-1$

	/** "Unsigned" icon with aspect binding decoration. */
	public static final String IMG_UNSIGNED_ADAPTED = "org.eclipse.objectteams.otequinox.branding.unsigned_adapted"; //$NON-NLS-1$

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.objectteams.otequinox.branding"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private ImageRegistry imageRegistry;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/** 
	 * Get an image identified by the given key. Supported values are
	 * {@link #IMG_SIGNED_ADAPTED} and {@link #IMG_UNSIGNED_ADAPTED}.
	 * @param key
	 * @return a cached imaged, if valid key was given, otherwise null.
	 */
	public Image getImage(String key) {
		if (this.imageRegistry == null)
			initializeImageRegistry();
		return this.imageRegistry.get(key);
	}

	private void initializeImageRegistry() {
		this.imageRegistry = new ImageRegistry();
		this.imageRegistry.put(IMG_SIGNED_ADAPTED, imageDescriptorFromPlugin(PLUGIN_ID, "icons/signed_adapted.gif")); //$NON-NLS-1$
		this.imageRegistry.put(IMG_UNSIGNED_ADAPTED, imageDescriptorFromPlugin(PLUGIN_ID, "icons/unsigned_adapted.gif")); //$NON-NLS-1$
	}
}
