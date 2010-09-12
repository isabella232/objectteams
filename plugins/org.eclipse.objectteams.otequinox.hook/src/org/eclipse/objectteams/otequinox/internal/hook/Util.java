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
 * $Id: Util.java 23461 2010-02-04 22:10:39Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal.hook;

import java.util.HashSet;

import org.eclipse.objectteams.otequinox.hook.ILogger;
import org.eclipse.osgi.framework.adaptor.BundleWatcher;
import org.osgi.framework.Bundle;

public class Util 
{
	// from org.eclipse.core.runtime.IStatus:
	public static final int OK = 0;
	public static final int INFO = 0x01;
	public static final int WARNING = 0x02;
	public static final int ERROR = 0x04;
	
	// configure OT/Equinox debugging:
	public static int WARN_LEVEL = INFO;
	public static boolean PROFILE= false;
	static {
		String level = System.getProperty("otequinox.debug"); //$NON-NLS-1$
		if (level != null) {
			level = level.toUpperCase();
			if (level.equals("OK"))            //$NON-NLS-1$
				WARN_LEVEL = OK;
			else if (level.equals("INFO"))     //$NON-NLS-1$
				WARN_LEVEL = INFO;
			else if (level.startsWith("WARN")) //$NON-NLS-1$
				WARN_LEVEL = WARNING;
			else if (level.startsWith("ERR"))  //$NON-NLS-1$
				WARN_LEVEL = ERROR;
			else
				WARN_LEVEL = OK;
		}
		PROFILE= (System.getProperty("otequinox.profile") != null); //$NON-NLS-1$
	}

	/** Profiling data: */
	enum ProfileKind { BaseTransformation, AspectTransformation, SuperClassFetching }
	private static long[] profileTimes= new long[ProfileKind.values().length];
	private static long systemStartTime= System.nanoTime();

	static HashSet<String> PLATFORM_BUNDLES = null;
	
	@SuppressWarnings("nls")
	private static void checkInit() {
		if (PLATFORM_BUNDLES == null) {
			PLATFORM_BUNDLES = new HashSet<String>();
			for (String bundle : new String[] { "org.eclipse.equinox.common",
												"org.eclipse.update.configurator",
												"org.eclipse.core.runtime",
												"org.eclipse.equinox.registry",
												"org.eclipse.equinox.app",
												"org.eclipse.equinox.ds",
												"org.eclipse.equinox.event",
												"org.eclipse.equinox.util",
												"org.eclipse.osgi.services",
												"org.eclipse.core.runtime.compatibility.auth",
												"org.eclipse.equinox.preferences",
												"org.eclipse.equinox.simpleconfigurator",
												"org.eclipse.core.jobs",
												"org.eclipse.core.runtime.compatibility",
												"org.eclipse.equinox.p2.core",
												"org.eclipse.equinox.p2.reconciler.dropins",
												"org.eclipse.equinox.p2.directorywatcher",
												"org.eclipse.ecf",
												"org.eclipse.ecf.identity",
												"org.eclipse.ecf.filetransfer",
												"org.eclipse.ecf.provider.filetransfer",
												"org.eclipse.ecf.provider.filetransfer.httpclient",
												"org.apache.commons.httpclient"
				})
				PLATFORM_BUNDLES.add(bundle);
		}
	}

	static boolean isPlatformBundle(String bundleName) {
		checkInit();
		return PLATFORM_BUNDLES.contains(bundleName);
	}
	
	static void reportBundleStateChange(Bundle bundle, int type, ILogger logger) {
		if (Util.WARN_LEVEL == Util.OK) {
			String msg = "";                                                  //$NON-NLS-1$
			switch (type) {
			case BundleWatcher.START_INSTALLING: msg += "Installing "; break; //$NON-NLS-1$
			case BundleWatcher.END_INSTALLING:   msg += "Installed  "; break; //$NON-NLS-1$
			case BundleWatcher.START_ACTIVATION: msg += "Activating "; break; //$NON-NLS-1$
			case BundleWatcher.END_ACTIVATION:   msg += "Activated  "; break; //$NON-NLS-1$
			}
			logger.log(OK, msg+bundle.toString());
		}
	}

	@SuppressWarnings("nls")
	public static void profile(long startTime, ProfileKind kind, String msg, ILogger logger) 
	{
		long now= System.nanoTime();
		long delta= (now-startTime) / getActiveCount();
		long total= (profileTimes[kind.ordinal()]+= delta);
		msg = msg.substring(msg.lastIndexOf('.')+1);
		logger.doLog(INFO, "Profile "+kind.name()+": "+m(delta)+"("+m(total)+"/"+m(now-systemStartTime)+") ["+msg+"]");
	}
	// nano-to milli conversion
	private static double m(long l) {
		return (l/1000000.0);
	}

	private static int getActiveCount() {
		ThreadGroup group= Thread.currentThread().getThreadGroup();
		ThreadGroup parent= group.getParent();
		while (parent != null) {
			group= parent;
			parent= group.getParent();
		}
		return group.activeCount();
	}
}
