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
 * $Id: CompilationUnitContainmentTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.errorreporting;

import org.eclipse.objectteams.otdt.tests.compiler.TestBase;


/**
 * This class contains tests concerning compilation units.
 *
 * @author brcan
 * @version $Id: CompilationUnitContainmentTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class CompilationUnitContainmentTest extends TestBase 
{
    public CompilationUnitContainmentTest(String testName)
    {
        super(testName);
    }

    /**
	 * The compilation unit's name doesn't match with the type declaration's name.
	 * Comment:
	 * Just like classes in Java, the name of the team has to be identical to
	 * the file name. 
	 */
    public void testInvalidCompilationUnitName1() 
	{
		createFile("MyTeam","public team class MyTeamWrongName {}");
		
		compileFile("MyTeam");
		
		assertFalse(isCompilationSuccessful());
	}
}