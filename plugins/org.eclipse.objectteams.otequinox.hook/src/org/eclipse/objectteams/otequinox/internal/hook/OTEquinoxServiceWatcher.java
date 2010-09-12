/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MasterTeamLoader.java 15426 2007-02-25 12:52:19Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal.hook;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Properties;

import org.eclipse.objectteams.otequinox.hook.IOTEquinoxService;
import org.eclipse.objectteams.otequinox.hook.IOTTransformer;
import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.baseadaptor.hooks.AdaptorHook;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * This class waits for the IOTEquinoxService to be registered and
 * announces it to the TransformerHook.
 * 
 * @author stephan
 * @since OTDT 1.1.4
 */
public class OTEquinoxServiceWatcher implements AdaptorHook {
	
	// whom to inform:
	private TransformerHook hook;

	public OTEquinoxServiceWatcher(TransformerHook hook) {
		this.hook= hook;
	}
	
	private void initialize (final BundleContext context) 
	{	
		String  transformerFilter = "(objectclass="+IOTEquinoxService.class.getName()+")";  //$NON-NLS-1$ //$NON-NLS-2$
		//Add listener to listen for the registration of the OT/Equinox service:
		ServiceListener transformerListener = new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
				if(event.getType() == ServiceEvent.REGISTERED)
					connectOTEquinoxService(context);
			}
		};
		try {
			context.addServiceListener(transformerListener,transformerFilter);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		transformerFilter = "(objectclass="+IOTTransformer.class.getName()+")";  //$NON-NLS-1$ //$NON-NLS-2$
		//Add listener to listen for the registration of the OTRE service:
		transformerListener = new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
				if(event.getType() == ServiceEvent.REGISTERED)
					connectOTTransformerService(context);
			}
		};
		try {
			context.addServiceListener(transformerListener,transformerFilter);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		ServiceReference ref= context.getServiceReference(PackageAdmin.class.getName());
		if (ref!=null)
			this.hook.connectPackageAdmin((PackageAdmin)context.getService(ref));

	}

	private void connectOTEquinoxService (BundleContext context) {
		ServiceReference ref= context.getServiceReference(IOTEquinoxService.class.getName());
		if (ref!=null)
			this.hook.connectOTEquinoxService((IOTEquinoxService)context.getService(ref));
	}	

	private void connectOTTransformerService (BundleContext context) {
		ServiceReference ref= context.getServiceReference(IOTTransformer.class.getName());
		if (ref!=null)
			this.hook.connectOTTransformerService((IOTTransformer)context.getService(ref));
	}
	
	/** 
	 * Capture the system bundle at start-up:
	 * (see {@link AdaptorHook#frameworkStart(BundleContext)}
	 */
	public void frameworkStart(BundleContext systemContext) throws BundleException {
		initialize(systemContext);
	}

	// === other methods implementing AdaptorHook do nothing: ===
	
	public void addProperties(Properties properties) {
		// do nothing
	}

	public FrameworkLog createFrameworkLog() {
		// do nothing
		return null;
	}
	public void frameworkStop(BundleContext context) throws BundleException {
		// do nothing		
	}

	public void frameworkStopping(BundleContext context) {
		// do nothing
	}

	public void handleRuntimeError(Throwable error) {
		// do nothing
	}

	public void initialize(BaseAdaptor adaptor) {
		// do nothing
	}

	public URLConnection mapLocationToURLConnection(String location)
			throws IOException {
		// do nothing
		return null;
	}

	public boolean matchDNChain(String pattern, String[] dnChain) {
		// do nothing
		return false;
	}	
}
