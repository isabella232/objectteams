/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 GK Software AG, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel;

public class RetargetableFileBasedModelTest extends FileBasedModelTest {

	protected static String projectDirectory;

	public RetargetableFileBasedModelTest(String name) {
		super(name);
	}

    public void setUpSuite() throws Exception
    {
    	if (RetargetableFileBasedModelTest.projectDirectory != null) {
    		// don't reuse JavaModelCache, type names are shared between suites
    		simulateRestart();
	        // use projectDirectory injected by the enclosing suite
	        getTestSetting().resetProjectDirectory(RetargetableFileBasedModelTest.projectDirectory);
		}
    	setTestProjectDir(getTestSetting().getTestProject());
        super.setUpSuite();
    }

	public static void setProjectDirectory(String directory) {
		projectDirectory = directory;
	}
}
