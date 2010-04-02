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
 * $Id: Test3d.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam;

import junit.framework.Test;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author kaschja
 * @version $Id: Test3d.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * This class contains testing methods for a test setting with a role class with a method
 * whereas the method has a throw-clause
 * and the role class is bound to a baseclass which is a team class
 */
public class Test3d extends Test3a
{
    private final String METHOD_NAME   = "roleMethod";
    private final String EXC_TYPE          = "QException;";  

    
    public Test3d(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test3d.class);
        }
        junit.framework.TestSuite suite = new Suite(Test3d.class
            .getName());
        return suite;
    }

    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        getTestSetting().setTeamClass("Team_3d");
    }

    public void testExceptionsOfMethod() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());

        IMethod method = getTestSetting().getRoleJavaElement().getMethod(METHOD_NAME, new String[0]);
        assertNotNull(method);
        assertTrue(method.exists());
        
        String[] exceptionTypes = method.getExceptionTypes();
        assertTrue(exceptionTypes.length == 1);
        assertEquals(exceptionTypes[0], EXC_TYPE);
    }
}
