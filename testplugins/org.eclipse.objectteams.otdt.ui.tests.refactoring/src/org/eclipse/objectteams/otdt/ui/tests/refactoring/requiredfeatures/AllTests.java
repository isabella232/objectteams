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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring.requiredfeatures;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author svacina
 * 
 * Runs all test cases which are needed for refactoring
 */
public class AllTests
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AllTests.suite());
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(
                "Tests for required features");
     
        //TODO(jsv) test only the usage of specific components used in Refactoring Processor
        
        // Hierarchy
        suite.addTest(org.eclipse.objectteams.otdt.tests.superhierarchy.AllTests.suite());
        // TypeHelper.getInheritedMethods
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.util.TypeHelperGetInheritedMethodsTest.suite());
        // SearchEngine
        suite.addTest(org.eclipse.objectteams.otdt.tests.search.OTJavaMethodSearchTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.tests.search.OTJavaTypeSearchTests.suite());
        //TODO(jsv): add required selection tests 
        return suite;
    }
}