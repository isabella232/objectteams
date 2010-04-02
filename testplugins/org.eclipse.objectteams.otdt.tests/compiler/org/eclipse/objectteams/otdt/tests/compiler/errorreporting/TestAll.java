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
 * $Id: TestAll.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.errorreporting;

import junit.framework.TestSuite;

/**
 * This class collects all test cases in one test suite.
 *
 * @author brcan
 * @version $Id: TestAll.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class TestAll extends TestSuite
{
    public TestAll()
    {
        super();
    }
    
    public static TestSuite suite()
    {
    	TestSuite suite = new TestSuite("All Compiler Tests");

		// containment tests
		suite.addTestSuite(CompilationUnitContainmentTest.class);
		suite.addTestSuite(RoleContainmentTest.class);
		suite.addTestSuite(TeamContainmentTest.class);
		// inheritance tests
		suite.addTestSuite(RoleBaseInheritanceTest.class);
		suite.addTestSuite(RoleInheritanceTest.class);
		suite.addTestSuite(TeamInheritanceTest.class);
		// Copy Inheritance
		suite.addTestSuite(CopyInheritanceTest.class);
    	// binding tests
    	suite.addTestSuite(CallinBindingTest.class);
		suite.addTestSuite(CalloutBindingTest.class);
    	// lifting and lowering tests
    	suite.addTestSuite(LiftingTest.class);
    	suite.addTestSuite(LoweringTest.class);
    	// parameter mapping tests
    	suite.addTestSuite(ParameterMappingsTest.class);
		// team activation tests
		suite.addTestSuite(TeamActivationTest.class);
    	// parser tests
		suite.addTestSuite(ParserTest.class);
		//Code snippets and wrong code tests
		suite.addTestSuite(SourceSnippetRecoveryTest.class);
		//External Role tests
		suite.addTestSuite(ExternalRoleTest.class);
		
    	
    	return suite;
    }
}
