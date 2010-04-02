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
 * $Id: ExternalRoleTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.errorreporting;

import java.io.File;

import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.tests.compiler.TestBase;

/**
 * @author macwitte
 */
public class ExternalRoleTest extends TestBase implements IOTConstants{

	public ExternalRoleTest(String testName)
	{
		super(testName);
	}

	/**
	 * Test for external defined role with special team-package declaration
	 */
	public void testExternalRoleWithCompiledTeam()
	{
		createFile("MyRole","team package MyTeam;" + 
				NL + "public class MyRole {" +
				NL + "}");
		
		createFile("MyTeam","public team class MyTeam { " +
                NL + "      private MyRole r = null;" +
				NL + "}");
	
		compileFile("MyTeam");
        assertTrue(isCompilationSuccessful());

		compileFile("MyTeam"+File.separator+"MyRole");			
		assertTrue(isCompilationSuccessful());
	}

	/**
	 * Test for external defined role with special team-package declaration
	 */
	public void testExternalRoleWithUncompiledTeam()
	{
		createFile("MyRole","team package MyTeam;" + 
						NL + "public class MyRole {" +
                        NL + "    void foo() { System.out.println(MyTeam.this); }" +
						NL + "}");
						
		createFile("MyTeam","public team class MyTeam { " +
                        NL + "      private MyRole r = null;" +
						NL + "}");
		
		compileFile("MyTeam"+File.separator+"MyRole");
		assertTrue(isCompilationSuccessful());
	}

	/**
	 * Test for external defined role with special team-package declaration
	 */
	public void testExternalRoleWithQualifiedPackageName()
	{
		createFile("MyRole","team package org.eclipse.objectteams.MyTeam;" + 
						NL + "public class MyRole {" +
						NL + "}");
						
		createFile("MyTeam","package org.eclipse.objectteams;" +
						NL + "public team class MyTeam { " +
                        NL + "      private MyRole r = null;" +
						NL + "}");
		
		compileFile("org"+File.separator+"objectteams"+File.separator+"MyTeam"+File.separator+"MyRole");
        assertTrue(isCompilationSuccessful());

		compileFile("org"+File.separator+"objectteams"+File.separator+"MyTeam");
		assertTrue(isCompilationSuccessful());
	}

	/**
	 * Test for external defined role with special team-package declaration
	 * Expected Team has missing team Modifier
	 */
	public void testExternalRoleWithMissingTeamModifier()
	{
		createFile("MyRole","team package MyTeam;" + 
						NL + "public class MyRole {" +
						NL + "}");
						
		createFile("MyTeam","public class MyTeam { " +
                        NL + "      private MyRole r = null;" +
						NL + "}");
		
		compileFile("MyTeam"+File.separator+"MyRole");
		assertFalse(isCompilationSuccessful());
	}
	
	/**
	 * Test for external defined role with special team-package declaration
	 * Expected Team does not exist
	 */
	public void testExternalRoleWithMissingTeam()
	{
		createFile("MyRole","team package MyTeam;" + 
						NL + "public class MyRole {" +
						NL + "}");
								
		compileFile("MyTeam"+File.separator+"MyRole");
		assertFalse(isCompilationSuccessful());
	}		
	
	//http://nevermind.cs.tu-berlin.de/~gis/ot/2.4.2-otjld-inaccessible-base-class-9.html
	public void inaccessiblebaseclass()
	{
		createFile("MyClass","package p1;"+
						NL + "public class MyClass {"+
						NL + " class InnerClass {}"+
						NL + "}");
		
		createFile("MyTeam","package p2;"+
						NL + "public team class MyTeam {"+
						NL + "public class MyRole extends p1.MyClass playedBy InnerClass {}"+
						NL + "}");
	
		compileFile("p1"+File.separator+"MyClass");
        assertTrue(isCompilationSuccessful());

		compileFile("p2"+File.separator+"MyTeam");			
		assertFalse(isCompilationSuccessful());
	
	}
	

		
}
