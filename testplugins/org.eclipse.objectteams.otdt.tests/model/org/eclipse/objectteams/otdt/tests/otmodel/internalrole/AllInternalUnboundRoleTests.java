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
 * $Id: AllInternalUnboundRoleTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.internalrole;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.objectteams.otdt.tests.otmodel.TestDataHandler;
import org.eclipse.objectteams.otdt.tests.otmodel.TestSetting;

/**
 * @author jwloka 
 * @version $Id: AllInternalUnboundRoleTests.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class AllInternalUnboundRoleTests extends TestCase
{
    public AllInternalUnboundRoleTests(String name) 
    {
        super(name);
    }
    
    public static Class[] getAllTestClasses()
    {
        return new Class[]
        {
            org.eclipse.objectteams.otdt.tests.otmodel.role.unbound.Test1.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.unbound.Test2.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.unbound.Test3a.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.unbound.Test3b.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.unbound.Test3c.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.unbound.Test3d.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.unbound.Test4a.class,
            org.eclipse.objectteams.otdt.tests.otmodel.role.unbound.Test4b.class
        };
    }

    public static TestSetting getActualTestSetting()
    {
        return new TestSetting(
            				"InternalDefinedRole",
            				"unbound",
            				"unbound.teampkg");
    }
    
    
    public static Test suite()
    {
        TestSuite ts = new TestSuite(AllInternalUnboundRoleTests.class.getName());

        Class[] testClasses = getAllTestClasses();
        // Reset forgotten subsets of tests
//        AbstractJavaModelTests.testsNames = null;
//        AbstractJavaModelTests.testsNumbers = null;
//        AbstractJavaModelTests.testsRange = null;

        // call the suite() method and add the resulting suite to the suite
        for (int idx = 0; idx < testClasses.length; idx++)
        {
            Class<?> curTestClass = testClasses[idx];

            try
            {
                TestDataHandler.addTestSetting(curTestClass, getActualTestSetting());
                
                Method suiteMethod = curTestClass.getDeclaredMethod(
                    "suite", new Class[0]); //$NON-NLS-1$
                Test suite = (Test) suiteMethod.invoke(null, new Object[0]);
                ts.addTest(suite);
            }
            catch (IllegalAccessException ex)
            {
                ex.printStackTrace();
            }
            catch (InvocationTargetException ex)
            {
                ex.getTargetException().printStackTrace();
            }
            catch (NoSuchMethodException ex)
            {
                ex.printStackTrace();
            }
        }
        return ts;
    }
}
