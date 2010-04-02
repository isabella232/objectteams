/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTHelpPlugin.java 23436 2010-02-04 00:29:04Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.help;

import org.eclipse.objectteams.otdt.core.exceptions.ExceptionHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class OTHelpPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static OTHelpPlugin plugin;
	
	public static final String PLUGIN_ID = "org.eclipse.objectteams.otdt.ui.help"; //$NON-NLS-1$
	public static final String OTJLD_VIEW = "org.eclipse.objectteams.otdt.ui.help.views.OTJLDView"; //$NON-NLS-1$
		
	/**
	 * The constructor.
	 */
	public OTHelpPlugin() {
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static OTHelpPlugin getDefault() {
		return plugin;
	}
	
	public static ExceptionHandler getExceptionHandler()
	{
		return new ExceptionHandler(PLUGIN_ID);
	}
}
