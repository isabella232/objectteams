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
 * $Id: TestDataHandler.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  jwloka
 * @version $Id: TestDataHandler.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class TestDataHandler
{
    private static TestDataHandler _singleton;
    private Map<String, TestSetting> _mapping = new HashMap<String, TestSetting>(); // test case::test setting
     
    private TestDataHandler()
    {
        _singleton = this;
    }
    
    /**
     * Assigns a <code>FileBasedTest</code>-test case to a specific <code>TestSetting</code>.
     */
    public static void addTestSetting(Class testCase, TestSetting ts)
    {
        if (ts != null)
        {
            getMapping().put(testCase.getName(), ts);
        }
    }
    
    public static TestSetting getTestSetting(Class testClass)
    {
        return (TestSetting)getMapping().get(testClass.getName());
    }
    
    private static Map<String,TestSetting> getMapping()
    {
        if (_singleton == null)
        {
            new TestDataHandler();
        }
        return _singleton._mapping;
    }
}
