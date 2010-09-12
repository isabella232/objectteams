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
package org.eclipse.objectteams.otdt.tests.otmodel.internal;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author anklam
 *
 * @version $Id: AllTests.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("All OT Internal Model-Tests");
		//$JUnit-BEGIN$
		suite.addTest(TypeHelperTest003.suite());
		suite.addTest(TypeHelperTest004.suite());
		suite.addTest(TypeHelperTest002.suite());
		suite.addTest(TypeHelperTest001.suite());
		suite.addTest(OTModelManagerTest.suite());
		//$JUnit-END$
		return suite;
	}
}