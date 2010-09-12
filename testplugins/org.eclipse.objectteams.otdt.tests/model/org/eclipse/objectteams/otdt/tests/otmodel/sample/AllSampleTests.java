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
 * $Id: AllSampleTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.sample;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Description of the class.
 * 
 * @author jwloka
 * @version $Id: AllSampleTests.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class AllSampleTests extends TestCase
{
    public AllSampleTests(String name) 
    {
        super(name);
    }

    public static Class[] getAllTestClasses()
    {
        return new Class[]
        {
//			enter your test classes here!     
        };
    }

    public static Test suite()
    {
        TestSuite ts = new TestSuite(AllSampleTests.class.getName());

        Class[] testClasses = getAllTestClasses();
        // Reset forgotten subsets of tests
//        AbstractJavaModelTests.testsNames = null;
//        AbstractJavaModelTests.testsNumbers = null;
//        AbstractJavaModelTests.testsRange = null;

        for (int idx = 0; idx < testClasses.length; idx++)
        {
            Class<?> testClass = testClasses[idx];

            // call the suite() method and add the resulting suite to the suite
            try
            {
                Method suiteMethod = testClass.getDeclaredMethod(
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