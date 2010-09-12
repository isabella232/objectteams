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
 * $Id: LiftingTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.errorreporting;

import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.tests.compiler.TestBase;

/**
 * This class contains tests concerning the Lifting mechanism in ObjectTeams.
 *
 * @author brcan
 * @version $Id: LiftingTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class LiftingTest extends TestBase implements IOTConstants
{
    public LiftingTest(String testName)
    {
        super(testName);
    }

    /**
	 * A test for declared lifting.
	 */
	public void testDeclaredLifting1()
	{
		createFile("MyBase","public class MyBase {}");
		
		createFile("MyTeam","public team class MyTeam { " +
				NL + "	    protected class MyRole playedBy MyBase {" +
				NL + "	    }" +
				NL + "	    public void method(MyBase as MyRole obj){" +
				NL + "		    System.out.println(\"method(\"+obj+\")\");" +
				NL + "	}" +
				NL + "  public static void main (String[] args) {" +
				NL + "      (new MyTeam()).method(new MyBase());" +
				NL + "  }"+
				NL + "}");
		
		compileFile("MyTeam");
			
		assertTrue(isCompilationSuccessful());
	}
	
	/**
	 * A test for smart lifting.
	 */
	public void testSmartLifting1()
	{
		createFile("Base2", "public class Base2 {}");
		
		createFile("Base3", "public class Base3 extends Base2{}");
    	
		createFile("MyTeam","public team class MyTeam extends org.objectteams.Team {" +
				NL + "  public static void main(String[] args){" +
				NL +	"       System.out.println(\"MyTeam\");" +
				NL +		"       new MyTeam();" +
				NL +		"  }" +
				NL +		"	public MyTeam() {" +
				NL +		"       super();" +
				NL +		"		System.out.println(\"MyTeam.MyTeam()\");" +
				NL +		"       System.out.println("+new String(_OT_LIFT_TO)+OT_DELIM+"Role2(this, new Base2(), 0));" +
				NL +		"	}" +										
				NL +		"	public class Role1 {}" +													
				NL +		"	public class Role2 extends Role1 playedBy Base2{}" +
				NL +		"   public class Role3 extends Role2 playedBy Base3{}" +
				NL +		"   public class Role4 extends Role3 playedBy Base3{}" +
				NL +	"   public class Role6 extends Role4 playedBy Base3{}" +							        										
				NL +		"   public class Role5 extends Role4 playedBy Base3{}" +
				NL +		"}");
		
		compileFile("MyTeam");
		
        // TODO (SH): what problem expected? definite ambiguity 7.3(b)? 
        // need to replace explicit lift-invocation with some legal syntax.
		assertFalse(isCompilationSuccessful());
	}
}

