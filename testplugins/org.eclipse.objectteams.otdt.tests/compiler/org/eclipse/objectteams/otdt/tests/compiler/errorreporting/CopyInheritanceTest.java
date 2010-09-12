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
 * $Id: CopyInheritanceTest.java 23494 2010-02-05 23:06:44Z stephan $
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
 * @author macwitte/haebor
 */
public class CopyInheritanceTest extends TestBase {

	/**
	 * Testcases for Copy-Inheritance
	 */
	public CopyInheritanceTest(String testName) {
		super(testName);
	}

	/**
	 * Test copy inheritance for string-array-references 
	 */
	public void testCopyStringArrayReferences() 
	{

     
		createFile("MySuperTeam","public team class MySuperTeam " +
			  NL + "{ " +	
			  NL + "	public class MyRole" +
			  NL + "    { " +	
			  NL + "	    String[] strArray;" +
			  NL + "	    public  void roleMethod()" +
		      NL + "	    {" +
			  NL + "	    	strArray = new String[1];" +
			  NL + "	    	strArray[0] = \"adsf\";" +
			  NL + "	    }" +
			  NL + "    } " +
			  NL + "}");

		createFile("MyTeam","public team class MyTeam extends MySuperTeam" +
			  NL + "{ " +	
			  NL + "}");
		  		
		compileFile("MyTeam");
	
		assertTrue(isCompilationSuccessful());
	}
	
	/**
	 * Test copy inheritance for role-array-references 
	 */
	public void testCopyRoleArrayReferences() 
	{

     
		createFile("MySuperTeam","public team class MySuperTeam " +
			  NL + "{ " +	
			  NL + "	public class MyRole" +
			  NL + "    { " +	
			  NL + "	    MyRole[] roleArray;" +
			  NL + "	    public  void roleMethod()" +
			  NL + "	    {" +
			  NL + "	    	roleArray = new MyRole[1];" +
			  NL + "	    	roleArray[0] = new MyRole();" +
			  NL + "	    }" +
			  NL + "    } " +
			  NL + "}");

		createFile("MyTeam","public team class MyTeam extends MySuperTeam" +
			  NL + "{ " +	
			  NL + "}");
		  		
		compileFile("MyTeam");
	
		assertTrue(isCompilationSuccessful());
	}
	
	/**
	 * Test copy inheritance for int-array-references 
	 */
	public void testCopyIntArrayReferences() 
	{

     
		createFile("MySuperTeam","public team class MySuperTeam " +
			  NL + "{ " +	
			  NL + "	public class MyRole" +
			  NL + "    { " +	
			  NL + "	    int[] intArray;" +
			  NL + "	    public  void roleMethod()" +
			  NL + "	    {" +
			  NL + "	    	intArray = new int[1];" +
			  NL + "	    	intArray[0] = 42;" +
			  NL + "	    }" +
			  NL + "    } " +
			  NL + "}");

		createFile("MyTeam","public team class MyTeam extends MySuperTeam" +
			  NL + "{ " +	
			  NL + "}");
		  		
		compileFile("MyTeam");
	
		assertTrue(isCompilationSuccessful());
	}	

	/**
	 * Test copy inheritance for array-length-field access 
	 */
	public void testCopyIntArrayLengthFieldAccess() 
	{

		createFile("MySuperTeam","public team class MySuperTeam " +
			  NL + "{ " +	
			  NL + "	public class MyRole" +
			  NL + "    { " +	
			  NL + "	    int[] intArray;" +
			  NL + "	    public  void roleMethod()" +
			  NL + "	    {" +
			  NL + "	    	intArray = new int[42];" +
			  NL + "	    	intArray[0] = intArray.length;" +
			  NL + "	    }" +
			  NL + "    } " +
			  NL + "}");

		createFile("MyTeam","public team class MyTeam extends MySuperTeam" +
			  NL + "{ " +	
			  NL + "}");
		  		
		compileFile("MyTeam");
	
		assertTrue(isCompilationSuccessful());
	}	


	/**
	 * Test copy inheritance with missing import in subclass 
	 */
	public void testMissingImport1() 
	{
		createFile("MySuperTeam","" +
			  NL + "public team class MySuperTeam " +
			  NL + "{ " +	
			  NL + "	public class MyRole {}" +
			  NL + "}");

		createFile("MyTeam","" +
			  NL + "public team class MyTeam extends MySuperTeam" +
			  NL + "{ " +	
			  NL + "	public class MyRole" +
			  NL + "    { " +	
			  NL + "	    BigDecimal roleMethod()" +
			  NL + "	    {" +
			  NL + "	    	return null;" +
			  NL + "	    }" +
			  NL + "    } " +			  
			  NL + "}");
		  		
		compileFile("MyTeam");
	
		assertFalse(isCompilationSuccessful());
	}	

	/**
	 * Test copy inheritance with missing import in subclass 
	 */
	public void testMissingImport2() 
	{
		createFile("MySuperTeam","" +
			  NL + "public team class MySuperTeam " +
			  NL + "{ " +	
			  NL + "	public class MyRole {" +
			  //NL + "       public void method(){}" +
			  NL + "    }" +
			  NL + "}");

		createFile("MyTeam","" +
			  NL + "public team class MyTeam extends MySuperTeam" +
			  NL + "{ " +	
			  NL + "	public class MyRole" +
			  NL + "    { " +	
			  NL + "	    BigDecimal roleMethod()" +
			  NL + "	    {" +
			  NL + "	    	return null;" +
			  NL + "	    }" +
			  NL + "	    void roleMethod1()" +
			  NL + "	    {" +
			  NL + "	    	return null;" +
			  NL + "	    }" +

			  NL + "    } " +			  
			  NL + "}");
		  		
		compileFile("MyTeam");
	
		assertFalse(isCompilationSuccessful());
	}	

}
