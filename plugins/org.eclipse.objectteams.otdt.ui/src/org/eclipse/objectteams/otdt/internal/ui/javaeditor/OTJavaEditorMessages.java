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
 * $Id: OTJavaEditorMessages.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.javaeditor;

import org.eclipse.osgi.util.NLS;;

public class OTJavaEditorMessages extends NLS {
	
	private static final String BUNDLE_NAME = "org.eclipse.objectteams.otdt.internal.ui.javaeditor.OTJavaEditorMessages"; //$NON-NLS-1$
	
	public static String RoleOverrideIndicator_open_error_message;
	public static String RoleOverrideIndicator_open_error_messageHasLogEntry;
	public static String RoleOverrideIndicator_open_error_title;
	public static String RoleOverrideIndicator_overrides;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, OTJavaEditorMessages.class);
	}

	private OTJavaEditorMessages() {
	}

}
