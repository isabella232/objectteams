/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTDebugAdaptorPlugin.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/** This Activator enables the debug adaptor to manage preferences of its own.
 *  
 *  @author stephan
 *  @since 1.2.0
 */
public class OTDebugAdaptorPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.objectteams.otdt.internal.debug.adaptor"; //$NON-NLS-1$
	
	private static OTDebugAdaptorPlugin instance;
	
	public OTDebugAdaptorPlugin() {
		instance = this;
	}

	public static AbstractUIPlugin getDefault() {
		return instance; 
	}
}
