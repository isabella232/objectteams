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
 * $Id: OTSamplesPlugin.java 23482 2010-02-05 20:16:19Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.samples;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author gis
 */
public class OTSamplesPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "org.eclipse.objectteams.otdt.samples"; //$NON-NLS-1$
	private static OTSamplesPlugin instance;

	public OTSamplesPlugin()
    {
        super();
        instance = this;
    }

	public static OTSamplesPlugin getDefault()
	{
		return instance;
	}

	public static Status createErrorStatus(String message, Throwable ex)
	{
		return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, ex);
	}
}
