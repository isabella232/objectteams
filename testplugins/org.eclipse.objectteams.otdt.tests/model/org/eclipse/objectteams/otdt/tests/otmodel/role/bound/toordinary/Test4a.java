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
 * $Id: Test4a.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toordinary;

import junit.framework.Test;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author kaschja
 * @version $Id: Test4a.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * This class contains testing methods for a test setting with a role class with an innerclass
 * whereas the innerclass is an ordinary class
 * and the role class is bound to a baseclass
 */
public class Test4a extends Test1
{
    protected String getInnerclassName()
    {
        return "AnInnerClass";
    }
    
    public Test4a(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test4a.class);
        }
        junit.framework.TestSuite suite = new Suite(Test4a.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        getTestSetting().setTeamClass("Team_4a");
    }

    public void testContainmentOfInnerclassInRoleClass() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());
        
        IType innerclass = getTestSetting().getRoleJavaElement().getType(getInnerclassName());
        assertNotNull(innerclass);
        assertTrue(innerclass.exists());
    }    
}
