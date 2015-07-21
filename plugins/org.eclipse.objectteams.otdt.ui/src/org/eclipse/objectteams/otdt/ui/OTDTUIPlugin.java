/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTDTUIPlugin.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.OTDTUIPluginConstants;
import org.eclipse.objectteams.otdt.internal.ui.OTElementAdapterFactory;
import org.eclipse.objectteams.otdt.internal.ui.callinmarkers.CallinMarkerCreator2;
import org.eclipse.objectteams.otdt.internal.ui.callinmarkers.RoleBindingChangedListener;
import org.eclipse.objectteams.otdt.internal.ui.preferences.GeneralPreferences;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * ObjectTeams Development Tooling User Interface Plugin.
 *
 * @author kaiser
 * @version $Id: OTDTUIPlugin.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class OTDTUIPlugin extends AbstractUIPlugin implements OTDTUIPluginConstants
{
	private OTElementAdapterFactory _otElementAdapterFactory;

    private static OTDTUIPlugin _singleton;
	
    private ResourceBundle 	_resourceBundle;

    private RoleBindingChangedListener _baseClassChangedListener;

    private CallinMarkerCreator2 _callinMarkerCreator;
    
    public OTDTUIPlugin()
    {
        super();
        _singleton = this;
        
        try {
			_resourceBundle = ResourceBundle.getBundle( RESOURCES_ID );
		} catch (MissingResourceException ex) {
			logException("Error initializing resource bundle of OTDT/UI", ex); //$NON-NLS-1$
			_resourceBundle = null;
		}
    }

    public static OTDTUIPlugin getDefault() {
        return _singleton;
    }

    /**
     * Answer the active workbench page
     * @return active workbench page or null if no workbench window is active.
     */
	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}

	private IWorkbenchPage internalGetActivePage() {
		IWorkbenchWindow window= getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;
		return getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
		
    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = OTDTUIPlugin.getDefault()._resourceBundle;
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            return key;
        }
    }

    /**
     * Add Object Teams flavoured images to the image registry.
     */
    protected void initializeImageRegistry(ImageRegistry reg) {
    	ImageManager.getSharedInstance().registerPluginImages(reg);    	
    }

	public void start(BundleContext context) throws Exception
    {
        super.start(context);
        
        GeneralPreferences.initDefaults(getPreferenceStore());
        
        registerAdapter();

        addJavaElementChangedListeners();

        getCallinMarkerCreator(); // initialize
    }
    
    public void stop(BundleContext context) throws Exception
    {
		unregisterAdapter();
		removeElementChangedListeners();
			
		if (_callinMarkerCreator != null)
		{
		    _callinMarkerCreator.uninstallListener();
		    _callinMarkerCreator = null;
		}
		
		// we don't have many other places where we could clean up OTDTCore. JavaCore is not ours :-/
		OTModelManager.dispose();
		super.stop(context);
	}


	public static void logException(String message, Throwable exception) {
		_singleton.getLog().log(new Status(IStatus.ERROR, UIPLUGIN_ID, IStatus.OK, message, exception));
	}

    public static void log(Throwable t) {
    	_singleton.getLog().log(new Status(IStatus.ERROR, UIPLUGIN_ID, t.getMessage(), t));
	}

	public static Status createErrorStatus(String message, Throwable exception)
	{
	    return new Status(IStatus.ERROR, UIPLUGIN_ID, IStatus.OK, message, exception);
	}

	private void registerAdapter()
    {
		_otElementAdapterFactory = new OTElementAdapterFactory();
		IAdapterManager manager = Platform.getAdapterManager();		
		manager.registerAdapters(_otElementAdapterFactory, IOTJavaElement.class);
    }
    
    private void unregisterAdapter()
    {
		IAdapterManager manager = Platform.getAdapterManager();		
		manager.unregisterAdapters(_otElementAdapterFactory, IOTJavaElement.class);
    }
    
	private void addJavaElementChangedListeners()
    {
	    _baseClassChangedListener = new RoleBindingChangedListener();
	    
		JavaCore.addElementChangedListener(_baseClassChangedListener,
				   ElementChangedEvent.POST_RECONCILE|
				   ElementChangedEvent.POST_CHANGE);
    }

    private void removeElementChangedListeners()
    {
		JavaCore.removeElementChangedListener(_baseClassChangedListener);
		_baseClassChangedListener = null;
    }

    /**
     * @noreference external clients should not call this method, public only for org.eclipse.objectteams.otdt.ui.tests
     */
	public CallinMarkerCreator2 getCallinMarkerCreator()
	{
	    if (_callinMarkerCreator == null)
			try {
				_callinMarkerCreator = new CallinMarkerCreator2();
			} catch (IllegalStateException e) {
				logException("Failed to activate callin marker creator", e); //$NON-NLS-1$
			}
	    
	    return _callinMarkerCreator;
	}
}
