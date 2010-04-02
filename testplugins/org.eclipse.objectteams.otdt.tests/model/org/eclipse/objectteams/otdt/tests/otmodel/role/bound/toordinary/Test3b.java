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
 * $Id: Test3b.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toordinary;

import junit.framework.Test;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author kaschja
 * @version $Id: Test3b.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * This class contains testing methods for a test setting with a role class with a method
 * whereas the method has an input parameter
 * and the role class is bound to a baseclass
 */
public class Test3b extends Test3a
{
    private final String PARA_NAME = "paraObj";
    private final String PARA_TYPE = "QString;"; 
    
    public Test3b(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test3b.class);
        }
        junit.framework.TestSuite suite = new Suite(Test3b.class
            .getName());
        return suite;
    }
    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        getTestSetting().setTeamClass("Team_3b");
    }
    
    public void testInputParameterOfMethodInRoleClass() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());

        IMethod method = getTestSetting().getRoleJavaElement().getMethod(METHOD_NAME, new String[] { PARA_TYPE });
        assertNotNull(method);
        assertTrue(method.exists());
        
        int numOfParas = method.getNumberOfParameters();
        assertTrue(numOfParas == 1);
        
        String[] paraNames = method.getParameterNames();
        assertTrue(paraNames.length == 1);
        assertEquals(paraNames[0], PARA_NAME);
        
        String[] paraTypes = method.getParameterTypes();
        assertTrue(paraTypes.length == 1);
        assertEquals(paraTypes[0], PARA_TYPE);
    }
    
    public void testContainmentOfMethodInRoleClass() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());

        IMethod method = getTestSetting().getRoleJavaElement().getMethod(METHOD_NAME, new String[] { PARA_TYPE });
        assertNotNull(method);
        assertTrue(method.exists());        
    }
}
