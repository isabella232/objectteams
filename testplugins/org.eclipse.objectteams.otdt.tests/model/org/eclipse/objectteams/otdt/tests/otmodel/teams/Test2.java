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
 * $Id: Test2.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.teams;

import junit.framework.Test;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

/**
 * @author kaschja
 * @version $Id: Test2.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * This test class contains tests for a team class with one method without parameters
 */
public class Test2 extends Test1
{
    
    public Test2(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test2.class);
        }
        junit.framework.TestSuite suite = new Suite(Test2.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir(getTestSetting().getTestProject());
        super.setUpSuite();
        getTestSetting().setTeamClass("Team3a");
    }

    public void testContainment1() throws JavaModelException
    {
        assertNotNull(getTestSetting().getTeamJavaElement());
        assertTrue(getTestSetting().getTeamJavaElement().exists());          
        
        assertNotNull(getTestSetting().getTeamJavaElement().getMethod("teamlevelMethod", new String[0]));
    }
    
    public void testGetRoleType_internalRole()
    {
        IType jmElem = getTestSetting().getTeamJavaElement();
        IOTType teamType = OTModelManager.getOTElement(jmElem);
        
        assertNotNull("OTElement doesn't exist!", teamType);
        
        IType expected = teamType.getRoleType("InternalRole1");
        
        assertNotNull("Role type not found", expected);
        assertTrue("Role type couldn't be created by the parser", expected.exists());
    }
    
    public void testGetRoleType_externalRole()
    {
        IType jmElem = getTestSetting().getTeamJavaElement();
        IOTType teamType = OTModelManager.getOTElement(jmElem);
        
        assertNotNull("OTElement doesn't exist!", teamType);
        
        IType expected = teamType.getRoleType("ExternalRole1");
        
        assertNotNull("Role type not found", expected);
        assertTrue("Role type couldn't be created by the parser", expected.exists());
    }

}
