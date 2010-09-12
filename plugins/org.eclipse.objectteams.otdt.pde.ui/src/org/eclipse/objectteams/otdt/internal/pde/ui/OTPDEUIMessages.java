/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007, 2009 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTPDEUIMessages.java 23470 2010-02-05 19:13:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.pde.ui;

import org.eclipse.osgi.util.NLS;

public class OTPDEUIMessages extends NLS 
{
	private static final String BUNDLE_NAME = OTPDEUIMessages.class.getName();


	public static String NewOTPProjectWizard_title;
	public static String NewOTPProjectWizard_MainPage_title;
	
	public static String NoOTJPluginProject;
	
	public static String OTNewPluginProjectWizard_CantAddOTSpecifics;
	public static String OTNewPluginProjectWizard_ProjectCreationError;

	public static String Validation_MissingActivationPolicy_error;
	public static String Resolution_AddBundleActivationPolicy_label;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, OTPDEUIMessages.class);
	}
}
