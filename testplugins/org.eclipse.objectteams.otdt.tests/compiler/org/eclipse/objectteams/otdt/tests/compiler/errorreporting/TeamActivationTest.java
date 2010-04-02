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
 * $Id: TeamActivationTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.errorreporting;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.objectteams.otdt.tests.compiler.TestBase;

/**
 * This class is for testing team activation.
 * Note, test cases which tried to test sequences of activation statements are removed,
 * because 
 * (a) this can only be tested when also executing the program, and 
 * (b) test goals stated in comments were wrong. 
 * 
 * @author kaschja
 * @version $Id: TeamActivationTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class TeamActivationTest extends TestBase
{
    public TeamActivationTest(String testName)
    {
        super(testName);
    }
	
	/**
	 * team activation with "within" must have team as argument
	 */
	public void testWithinWithTeamAsArgument() {
		createFile("MyTeam","public team class MyTeam {}");
										
		createFile("MyClient","public class MyClient"+
			  NL + "{"+
			  NL + "	public static void main(String[] argv) {"+
			  NL + "		MyTeam myTeam = new MyTeam();"+
			  NL + "		within(myTeam) {"+
			  NL + "			System.out.println(\"MyClient.main.within\");"+
			  NL + "		}"+
			  NL + "	}"+
			  NL + "}");

		compileFile("MyClient");
		
		assertTrue(isCompilationSuccessful());
	}
	/**
	 * team activation with "within" dont have normal class as argument
	 */
	public void testWithinWithRegularClassAsArgument() {
		createFile("MyClass","public class MyClass {}");
										
		createFile("MyClient","public class MyClient"+
			  NL + "{"+
			  NL + "	public static void main(String[] argv) {"+
			  NL + "		MyClass myClass = new MyClass();"+
			  NL + "		within(myClass) {"+
			  NL + "			System.out.println(\"MyClient.main.within\");"+
			  NL + "		}"+
			  NL + "	}"+
			  NL + "}");

		compileFile("MyClient");
		
		assertTrue(hasExpectedProblems(new int[] {IProblem.WithinStatementNeedsTeamInstance}));
	}
	
}
