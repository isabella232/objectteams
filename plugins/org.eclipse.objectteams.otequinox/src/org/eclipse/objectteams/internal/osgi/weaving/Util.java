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
package org.eclipse.objectteams.internal.osgi.weaving;

import static org.eclipse.objectteams.otequinox.TransformerPlugin.doLog;

import static org.eclipse.core.runtime.IStatus.*;

public class Util 
{

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
		String profile = System.getProperty("otequinox.profile"); //$NON-NLS-1$
		PROFILE= profile != null && !"false".equals(profile); //$NON-NLS-1$
	}

	/** Profiling data: */
	enum ProfileKind { Transformation, NoTransformation, Activation, Scan, SuperClassFetching, RedefineClasses }
	private static long[] profileTimes= new long[ProfileKind.values().length];
	private static long systemStartTime= System.nanoTime();

	@SuppressWarnings("nls")
	public static void profile(long startTime, ProfileKind kind, String msg) 
	{
		long now= System.nanoTime();
		long delta= (now-startTime);// / getActiveCount();
		long total= (profileTimes[kind.ordinal()]+= delta);
		if (kind != ProfileKind.Scan)
			msg = msg.substring(msg.lastIndexOf('.')+1);
//		doLog(INFO, "Profile "+kind.name()+": "+m(delta)+"("+m(total)+"/"+m(now-systemStartTime)+") ["+msg+"]");
		System.out.println("OT/Equinox Profile "+kind.name()+": "+m(delta)+"("+m(total)+"/"+m(now-systemStartTime)+") ["+msg+"]");;
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
