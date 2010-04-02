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
 * $Id: ActionMessages.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.internal.actions;

import org.eclipse.osgi.util.NLS;

public class ActionMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.objectteams.otdt.debug.ui.internal.actions.ActionMessages"; //$NON-NLS-1$
	public static String ChangeTeamActivationAction_activate_label;
	public static String ChangeTeamActivationAction_activate_description;
	public static String ChangeTeamActivationAction_deactivate_label;
	public static String ChangeTeamActivationAction_deactivate_description;
	public static String ChangeTeamActivationAction_error_title;
	public static String ChangeTeamActivationAction_error_exception;
	public static String ChangeTeamActivationAction_error_no_thread_suspended;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ActionMessages.class);
	}

	private ActionMessages() {
	}
}
