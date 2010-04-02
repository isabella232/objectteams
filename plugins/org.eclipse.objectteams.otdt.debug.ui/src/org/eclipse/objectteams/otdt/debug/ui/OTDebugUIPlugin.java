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
 * $Id: OTDebugUIPlugin.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.ColorManager;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.objectteams.otdt.core.exceptions.ExceptionHandler;
import org.eclipse.objectteams.otdt.debug.OTDebugElementsContainer;
import org.eclipse.objectteams.otdt.debug.TeamInstance;
import org.eclipse.objectteams.otdt.debug.ui.internal.CopyInheritanceBreakpointManager;
import org.eclipse.objectteams.otdt.debug.ui.internal.OTDebugElementAdapterFactory;
import org.eclipse.objectteams.otdt.debug.ui.internal.preferences.OTDebugPreferences;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class OTDebugUIPlugin extends AbstractUIPlugin 
{
	// preference constants
	public static final String PLUGIN_ID = "org.eclipse.objectteams.otdt.debug.ui"; //$NON-NLS-1$
	public static final String TEAM_DETAIL_PANE_ORIENTATION = "ot.teamview.detail.orientation"; //$NON-NLS-1$

    // this id is also used in plugin.xml:
    public static final String TEAM_VIEW_ID = "org.eclipse.objectteams.otdt.debug.ui.views.team"; //$NON-NLS-1$

    
	private static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$
	public static final String HELP_TEAM_VIEW = PREFIX + "team_view_context"; //$NON-NLS-1$

	//The shared instance.
	private static OTDebugUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
    private CopyInheritanceBreakpointManager _copyInheritanceBPManager;

	// TODO: use JDIDebugUIPlugin to contribute to the UI
	
	/**
	 * The constructor.
	 */
	public OTDebugUIPlugin() 
	{
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.eclipse.objectteams.otdt.debug.ui.OTDebugUIPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		OTDebugPreferences.propagateFilterFlag(getPreferenceStore());
		
		// this breakpoint manager listens to two kinds of changes:
		_copyInheritanceBPManager = new CopyInheritanceBreakpointManager();
		JDIDebugModel.addJavaBreakpointListener(_copyInheritanceBPManager);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(_copyInheritanceBPManager, IResourceChangeEvent.POST_CHANGE);
		
		IAdapterManager manager= Platform.getAdapterManager();
		OTDebugElementAdapterFactory propertiesFactory = new OTDebugElementAdapterFactory();
		manager.registerAdapters(propertiesFactory, OTDebugElementsContainer.class);
		manager.registerAdapters(propertiesFactory, TeamInstance.class);
		
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
	    JDIDebugModel.removeJavaBreakpointListener(_copyInheritanceBPManager);
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static OTDebugUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = OTDebugUIPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	/**
	 * Returns the a color based on the type of output.
	 * Valid types:
	 * <li>OT_GENERATED_CODE_COLOR</li>
	 * <li>CONSOLE_SYS_ERR_RGB</li>
	 */
	public static Color getPreferenceColor(String type) {
		return ColorManager.getDefault().getColor(PreferenceConverter.getColor(getDefault().getPreferenceStore(), type));
	}

	
	public static ExceptionHandler getExceptionHandler()
	{
		return new ExceptionHandler(PLUGIN_ID);
	}
    
    protected void initializeImageRegistry(ImageRegistry reg)
    {
        super.initializeImageRegistry(reg);
        OTDebugImages.register();
    }
}
