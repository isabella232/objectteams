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
 * $Id: AllTests.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom.converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.objectteams.otdt.tests.AbstractJavaModelTests;

/**
 * Enter a class description here!
 * 
 * @author jwloka
 * @version $Id: AllTests.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class AllTests extends TestCase
{

    public AllTests(String name) 
    {
        super(name);
    }

    public static Class[] getAllTestClasses()
    {
        return new Class[]
        {
            BaseCallMessageSendTest.class,
            BaseCallMessageSendTest2.class,
            BaseConstructorInvocationTest.class,
            CallinMappingDeclarationTest.class,
            CalloutMappingDeclarationTest.class,            
            FieldAccessSpecTest.class,
            GuardPredicateTest.class,
            LiftingTypeTest.class,
            MethodSpecTest.class,
            ParameterMappingCalloutTest.class,
            ParameterMappingCallinTest.class,
            PrecedenceDeclarationTest.class,
            RoleTypeDeclarationTest.class,
            TSuperConstructorInvocationTest.class,
            TSuperMessageSendTest.class,
            WithinStatementTest.class,
            
            DOMRegressionTests.class
        };
    }

    public static Test suite()
    {
        TestSuite ts = new TestSuite("All DOM-Converter Tests");

        Class[] testClasses = getAllTestClasses();
        // Reset forgotten subsets of tests
//        AbstractJavaModelTests.testsNames = null;
//        AbstractJavaModelTests.testsNumbers = null;
//        AbstractJavaModelTests.testsRange = null;

        for (int idx = 0; idx < testClasses.length; idx++)
        {
            Class testClass = testClasses[idx];

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
