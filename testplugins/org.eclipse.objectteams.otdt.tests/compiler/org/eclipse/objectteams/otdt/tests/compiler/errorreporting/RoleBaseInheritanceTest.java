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
 * $Id: RoleBaseInheritanceTest.java 23494 2010-02-05 23:06:44Z stephan $
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
 * Tests the relationship between role and base classes.
 * 
 * @author kaschja
 * @version $Id: RoleBaseInheritanceTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class RoleBaseInheritanceTest extends TestBase
{
    public RoleBaseInheritanceTest(String testName)
    {
        super(testName);
    }

    /**
	 * A role class must not be bound to a non-existing base class.
	 */
	public void testBindingOfNonExistingBaseClass1() 
	{
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +
		      NL + "	class MyRole playedBy NonExistingBase " +
		      NL + "    { " +
			  NL + "	} " +
			  NL + "}");
	
		compileFile("MyTeam");
	
		assertFalse(isCompilationSuccessful());
	}
	
	/**
	 * A regular class (non-team) must not be bound to a base class. 
	 */
	public void testBindingRegularClassWithBaseClass1()
	{
		createFile("MyBase","public class MyBase {}");
		
		createFile("MyClass","public class MyClass " +
				NL + "{" +
				NL + "		class MyRole playedBy MyBase {}" +
				NL + "}");
				
        compileFile("MyClass");    
                    
		assertFalse(isCompilationSuccessful());	
	}

	/**
	 * A role class must not be bound to a primitive type.
	 * In this testing sample the primitive type is int.
	 */
	public void testBindingOfPrimitiveType1()
	{
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +
			  NL + "	class MyRole playedBy int " +
			  NL + "    { " +
			  NL + "	} " +
			  NL + "}");
	
		compileFile("MyTeam");
	
		assertFalse(isCompilationSuccessful());		
	}
	
	/**
	 * A role class must not be bound to an array.
	 * In this testing sample the array element type is the primitive type int.
	 */
	public void testBindingOfArrayType1()
	{
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +
			  NL + "	class MyRole playedBy int[] " +
			  NL + "    { " +
			  NL + "	} " +
			  NL + "}");
	
		compileFile("MyTeam");
	
		assertFalse(isCompilationSuccessful());		
	}
	
	/**
	 * A role class must not be bound to an array.
	 * In this testing sample the array element type is a class type.
	 */
	public void testBindingOfArrayType2()
	{
		createFile("MyBase", "public class MyBase{}");
		
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +
			  NL + "	class MyRole playedBy MyBase[] " +
			  NL + "    { " +
			  NL + "	} " +
			  NL + "}");
	
		compileFile("MyTeam");
	
		assertFalse(isCompilationSuccessful());		
	}
	
		
	/**
	 * A role class must not be bound to a base class
	 * if both classes have identical names
	 * In this testing sample the classes reside in the same package
	 */
	public void testBindingWithNamingClash1()
	{
		createFile("Role", "public class Role{}");
		
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +
			  NL + "	class Role playedBy Role " +
			  NL + "    { " +
			  NL + "	} " +
			  NL + "}");
	
		compileFile("MyTeam");
	
		assertFalse(isCompilationSuccessful());		
	}
	

//TODO fix problem with creating packages with method TestBase.createFile	
	/**
	 * A role class must not be bound to a base class
	 * if both classes have identical names
	 * In this testing sample the classes reside in different packages
	 */
//	public void testBindingWithNamingClash2()
//	{
//		createFile("basepackage/Role", "package basepackage;\npublic class Role{}");
//		
//		createFile("teampackage/MyTeam",
//                   "package teampackage;" +
//			  NL + "public team class MyTeam " +
//			  NL + "{ " +
//			  NL + "	class Role playedBy Role " +
//			  NL + "    { " +
//			  NL + "	} " +
//			  NL + "}");
//	
//		compileFile("MyTeam");
//	
//		assertFalse(isCompilationSuccessful());		
//	}	
	
	/**
	 * A role class must not be bound to a base class
	 * if there is another role in the same team
	 * which has the same name as the bound base class.
	 */
	public void testBindingWithNamingClash3()
	{
		createFile("RoleB", "public class RoleB{}");
		
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +
			  NL + "	class RoleA playedBy RoleB" +
			  NL + "    { " +
			  NL + "	} " +
		      NL + "	class RoleB" +
			  NL + "    { " +
			  NL + "	} " + 			  
			  NL + "}");
	
		compileFile("MyTeam");
	
		assertFalse(isCompilationSuccessful());		
	}	

	/**
	 * A role class must not be bound to a base class
	 * if there is a role in a superteam
	 * which has the same name as the bound base class.
	 */
	public void testBindingWithNamingClash4()
	{
		createFile("RoleB", "public class RoleB{}");

		createFile("Superteam","public team class Superteam " +
			  NL + "{ " +
			  NL + "	class RoleB" +
			  NL + "    { " +
			  NL + "	} " +		  
			  NL + "}");
		
		createFile("Subteam","public team class Subteam extends Superteam" +
			  NL + "{ " +
			  NL + "	class RoleA playedBy RoleB" +
			  NL + "    { " +
			  NL + "	} " +  
			  NL + "}");
	
		compileFile("Subteam");
	
		assertFalse(isCompilationSuccessful());		
	}	
	
	/**
	 * A role class must not be bound to a base class
	 * if there is a role in a subteam
	 * which has the same name as the bound base class.
	 */
	public void testBindingWithNamingClash5()
	{
		createFile("RoleB", "public class RoleB{}");
		
		createFile("Superteam","public team class Superteam " +
			  NL + "{ " +
			  NL + "	class RoleA playedBy RoleB" +
			  NL + "    { " +
			  NL + "	} " +		  
			  NL + "}");
			  
		createFile("Subteam","public team class Subteam extends Superteam" +
			  NL + "{ " +
			  NL + "	class RoleB" +
			  NL + "    { " +
			  NL + "	} " +		  
			  NL + "}");			  

		compileFile("Subteam");
	
		assertFalse(isCompilationSuccessful());		
	}				
}
