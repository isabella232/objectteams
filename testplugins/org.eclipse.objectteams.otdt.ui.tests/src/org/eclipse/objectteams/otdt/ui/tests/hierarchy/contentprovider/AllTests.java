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
 * $Id: AllTests.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.hierarchy.contentprovider;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ike
 *
 * $Id: AllTests.java 23495 2010-02-05 23:15:16Z stephan $
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for org.eclipse.objectteams.otdt.ui.tests.hierarchy.contentprovider");
        //$JUnit-BEGIN$
        suite.addTest(OTSubHierarchyContentProviderTests.suite());
        suite.addTest(SuperHierarchyContentProviderTests.suite());
        //$JUnit-END$
        return suite;
    }
}
