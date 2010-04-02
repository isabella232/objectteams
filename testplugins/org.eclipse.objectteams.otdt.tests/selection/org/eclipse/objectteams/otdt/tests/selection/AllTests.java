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
 * $Id: AllTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.selection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.objectteams.otdt.tests.SuiteOfTestCases;
import org.eclipse.objectteams.otdt.tests.selection.codeselect.CodeSelectionTests;

/**
 * Enter a class description here!
 * 
 * @author jwloka
 * @version $Id: AllTests.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class AllTests extends TestCase {

    public AllTests(String name) 
    {
        super(name);
    }

    public static List<Class<? extends TestCase>> getAllTestClasses()
    {
    	ArrayList<Class<? extends TestCase>> tests = new ArrayList<Class<? extends TestCase>>(6);
    	tests.add(SelectionWithinTeamTests.class);
    	tests.add(SelectionWithinRoleTests.class);
    	tests.add(OTSpecificSelectionWithinTeamTests.class);
		tests.add(OTSpecificSelectionWithinRoleTests.class);
		tests.add(OTSelectionJavadocTest.class);
		tests.add(CodeSelectionTests.class);
		return tests;
    }

    public static Test suite()
    {
        TestSuite ts = new TestSuite("All OT Selection Tests");

        for (Class<? extends TestCase> testClass : getAllTestClasses())
        {
            // call the suite() method and add the resulting suite to the suite
            try
            {
            	// attention: some elements are tests some are suites:
           		if (SuiteOfTestCases.class.isAssignableFrom(testClass)) {
	                Method suiteMethod = testClass.getDeclaredMethod(
	                    "suite", new Class[0]); //$NON-NLS-1$
	                Test suite = (Test) suiteMethod.invoke(null, new Object[0]);
	                ts.addTest(suite);
            	} else {
            		ts.addTestSuite(testClass);
            	}
            }
            catch (IllegalAccessException ex)
            {
                ex.printStackTrace();
            }
            catch (NoSuchMethodException ex)
            {
                ex.printStackTrace();
            }
            catch (InvocationTargetException ex)
            {
                ex.getTargetException().printStackTrace();
            }
        }
        return ts;
    }
}
