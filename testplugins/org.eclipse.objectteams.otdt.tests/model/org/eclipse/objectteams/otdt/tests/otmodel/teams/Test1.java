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
 * $Id: Test1.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.teams;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author kaschja
 * @version $Id: Test1.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * This test class contains tests for an empty team class
 */
public class Test1 extends FileBasedModelTest
{
    public Test1(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test1.class);
        }
        junit.framework.TestSuite suite = new Suite(Test1.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir(getTestSetting().getTestProject());
        super.setUpSuite();
        getTestSetting().setTeamClass("Team1");
    }
    
    protected void setUp() throws Exception
    {
		super.setUp();
        getTestSetting().setUp();
    }
    
    public void testExistenceOfTypeInJavaModel()
    {
        assertNotNull(getTestSetting().getTeamJavaElement());
        assertTrue(getTestSetting().getTeamJavaElement().exists());
    }
    
    public void testExistenceOfTypeInOTModel() throws JavaModelException 
    {
        assertNotNull(getTestSetting().getTeamJavaElement());
        assertTrue(getTestSetting().getTeamJavaElement().exists());        
        
        IOTType teamOTElem = OTModelManager.getOTElement(getTestSetting().getTeamJavaElement());
        assertNotNull(teamOTElem);
    }
 
    public void testTeamProperty() throws JavaModelException
    {
        assertNotNull(getTestSetting().getTeamJavaElement());
        assertTrue(getTestSetting().getTeamJavaElement().exists());        
        
        IOTType teamOTElem = OTModelManager.getOTElement(getTestSetting().getTeamJavaElement());
        assertNotNull(teamOTElem);        

        assertTrue(teamOTElem.isTeam());        
    }
}
