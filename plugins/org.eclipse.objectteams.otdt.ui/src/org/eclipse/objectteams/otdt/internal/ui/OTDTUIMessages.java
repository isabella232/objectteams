/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009, Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTDTUIMessages.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui;

import org.eclipse.osgi.util.NLS;

public class OTDTUIMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.objectteams.otdt.internal.ui.OTDTUIMessages"; //$NON-NLS-1$
	public static String CompareWithBaseMethodAction_errorTitle;
	public static String CompareWithBaseMethodAction_ambiguousBindingsError;
	public static String CompareWithBaseMethodAction_multipleBaseMethodsError;
	public static String CompareBoundMethods_base_method_label;
	public static String CompareBoundMethods_compare_title;
	public static String CompareBoundMethods_compare_tooltip;
	public static String CompareBoundMethods_role_method_label;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, OTDTUIMessages.class);
	}

	private OTDTUIMessages() {
	}
}
