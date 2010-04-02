/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTRefactoringPlugin.java 23473 2010-02-05 19:46:08Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.refactoring;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class OTRefactoringPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.objectteams.otdt.refactoring"; //$NON-NLS-1$
	private static AbstractUIPlugin instance;
	
	public OTRefactoringPlugin() {
		instance = this;
	}

	public static AbstractUIPlugin getInstance() {
		return instance;
	}
}
