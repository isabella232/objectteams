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
 * $Id: LoweringTest.java 23494 2010-02-05 23:06:44Z stephan $
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
 * This class contains tests concerning the Lowering mechanism in ObjectTeams.
 *
 * @author brcan
 * @version $Id: LoweringTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class LoweringTest extends TestBase
{
    public LoweringTest(String testName)
    {
        super(testName);
    }

    /**
	 * Testing arraylowering; simple structure of classes, 
	 * complex lowering.
	 */
	public void testArrayLowering1() 
	{
		createFile("MyBase","public class MyBase" 
			+ NL + "{" 
			+ NL + ""  
			+ NL + "      public void myBaseLowerMethod(MyBase[][] b1, MyBase[][] b2)" 
			+ NL + "      {" 
			+ NL + "      }" 
			+ NL + "}" 
			+ NL);

		createFile("MyTeam",
				   "public team class MyTeam"   
			+ NL + "{" 
			+ NL + ""  	
			+ NL + "      public class Role playedBy MyBase" 
			+ NL + "      {"
			+ NL + "" 
			+ NL + "          abstract void myLoweringCallout(Role[][] r1, Role[][] r2);"
			+ NL + ""  
			+ NL + "          void myLoweringCallout(Role[][] r1, Role[][] r2) -> void myBaseLowerMethod(MyBase[][] r1, MyBase[][] r2);"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      public static void main(String[] args){"
			+ NL + "        new MyTeam();"
			+ NL + "      }" 
			+ NL + "}"
			+ NL);
		
		compileFile("MyTeam");
		
		assertTrue(isCompilationSuccessful());
	}
	
	/**
	 * A test for lowering.
	 */
	public void testLowering1() 
	{
		createFile("MyBase","public class MyBase" 
			+ NL + "{" 
			+ NL + "      public void myBaseMethod(int a)" 
			+ NL + "      {" 
			+ NL + "          System.out.println(\"normal\");" 
			+ NL + "      }"
			+ NL + ""  
			+ NL + "      public int myBaseLowerMethod(MyBase b)" 
			+ NL + "      {" 
			+ NL + "          System.out.println(\"lowered\");" 
			+ NL + "          return 5;"
			+ NL + "      }" 
			+ NL + "}" 
			+ NL);

		createFile("MyBase2","public class MyBase2 extends MyBase" 
			+ NL + "{" 
			+ NL + "      public void myBaseMethod(int a)" 
			+ NL + "      {" 
			+ NL + "          System.out.println(\"normal\");" 
			+ NL + "      }"
			+ NL + ""  
			+ NL + "      public int myBaseLowerMethod(MyBase b)" 
			+ NL + "      {" 
			+ NL + "          System.out.println(\"lowered\");"
			+ NL + "          return 5;" 
			+ NL + "      }" 
			+ NL + "}" 
			+ NL);
						
		createFile("MyTeam",
				   "public team class MyTeam extends org.objectteams.Team"   
			+ NL + "{" 
			+ NL + "      public MyTeam()" 
			+ NL + "      {" 
			+ NL + "         Role r = new Role(new MyBase());"
			+ NL + "         r.myCallout(4);"
			+ NL + "      }"
			+ NL + ""  	
			+ NL + "      public class Role0" 
			+ NL + "      {"
			+ NL + "" 
			+ NL + "          void myCallout(int a)"
			+ NL + "          {" 
			+ NL + "              System.out.println(\"Hallo\");" 
			+ NL + "          }"
			+ NL + "      }"
			+ NL + ""  
			+ NL + "      public class Role extends Role0 playedBy MyBase" 
			+ NL + "      {"
			+ NL + "" 
			+ NL + "          abstract int myLoweringCallout(Role r);"
			+ NL + ""  
			+ NL + "          void myCallout(int a) => void myBaseMethod(int a);" 			
			+ NL + "          int myLoweringCallout(Role r) -> int myBaseLowerMethod(MyBase r);"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      public class Role2 playedBy MyBase2" 
			+ NL + "      {"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      public static void main(String[] args){"
			+ NL + "        new MyTeam();"
			+ NL + "      }" 
			+ NL + "}"
			+ NL);
		
		compileFile("MyTeam");
		
		assertTrue(isCompilationSuccessful());
	}

}
