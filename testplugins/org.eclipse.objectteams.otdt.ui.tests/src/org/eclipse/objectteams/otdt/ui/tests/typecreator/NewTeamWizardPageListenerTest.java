/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: NewTeamWizardPageListenerTest.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.typecreator;

import junit.framework.TestCase;

import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.objectteams.otdt.internal.ui.wizards.NewTeamWizardPage;
import org.eclipse.objectteams.otdt.internal.ui.wizards.listeners.NewTeamWizardPageListener;

/**
 * @author anklam
 *
 * $Id: NewTeamWizardPageListenerTest.java 23495 2010-02-05 23:15:16Z stephan $
 */
public class NewTeamWizardPageListenerTest extends TestCase
{
	private NewTeamWizardPage _teamWizardPage;
	
	protected void setUp() throws Exception
	{
		super.setUp();
		_teamWizardPage = new NewTeamWizardPage();
		NewTeamWizardPageListener listener = new NewTeamWizardPageListener(_teamWizardPage);
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testValidateTypeName()
	{
		StringDialogField teamName = _teamWizardPage.getTypeNameDialogField();
		// validateTypeName() is called indirectly after setText()
		teamName.setText("TEST");
	}
}
