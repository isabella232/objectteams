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
 * $Id: AllTests.java 23492 2010-02-05 22:57:56Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.tests;

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.jdt.debug.tests.ProjectCreationDecorator;
import org.eclipse.objectteams.otdt.debug.tests.core.JSR045Tests;
import org.eclipse.objectteams.otdt.debug.tests.core.SourceDebugExtensionTest;
import org.eclipse.objectteams.otdt.debug.tests.core.StratumTests;
import org.eclipse.swt.widgets.Display;

/**
 * Tests for automatic testrun
 */
public class AllTests extends TestSuite
{

    /**
     * Flag that indicates test are in progress
     */
    protected boolean fTesting = true;

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite()
    {
        return new AllTests();
    }

    /**
     * Construct the test suite.
     * ADD TESTS HERE
     */
    public AllTests()
    {
		addTest(new TestSuite(OTProjectCreationDecorator.class));
		
		addTest(new TestSuite(StratumTests.class));
		
        addTest(new TestSuite(SourceDebugExtensionTest.class));
        
    }

    /**
     * Runs the tests and collects their result in a TestResult. The debug tests
     * cannot be run in the UI thread or the event waiter blocks the UI when a
     * resource changes.
     */
    public void run(final TestResult result)
    {
        final Display display = Display.getCurrent();
        Thread thread = null;
        try
        {
            Runnable r = new Runnable()
            {
                public void run()
                {
                    for (Enumeration e = tests(); e.hasMoreElements();)
                    {
                        if (result.shouldStop())
                            break;
                        Test test = (Test) e.nextElement();
                        runTest(test, result);
                    }
                    fTesting = false;
                    display.wake();
                }
            };
            thread = new Thread(r);
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        while (fTesting)
        {
            try
            {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
    }

}
