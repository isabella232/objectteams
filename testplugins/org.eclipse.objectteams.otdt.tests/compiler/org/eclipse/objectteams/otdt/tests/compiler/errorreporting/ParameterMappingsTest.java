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
 * $Id: ParameterMappingsTest.java 23494 2010-02-05 23:06:44Z stephan $
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
 * This class contains tests concerning the parameter mappings in callin and
 * callout bindings. 
 *
 * @author brcan
 * @version $Id: ParameterMappingsTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class ParameterMappingsTest extends TestBase
{
    public ParameterMappingsTest(String testName)
    {
        super(testName);
    }

	/**
	 * A base method with a parameter is used in a callout binding.
	 */
	public void testCalloutParameterMappingResult1()
	{
		createFile("MyBase",
				   "public class MyBase {                          \r\n"+
				   "    public int baseMethod() { return 2; }    \r\n"+
				   "}                                              \r\n"
		);
	
		createFile("SuperRoleClass",
				   "public class SuperRoleClass {                  \r\n"+
				   "    public int roleMethod() { return 1000; }   \r\n"+
				   "}                                              \r\n"
		);
	
		createFile("MyTeam",
					"public team class MyTeam {                    \r\n"+
					"	protected class MyRole extends SuperRoleClass playedBy MyBase {             \r\n"+
					"		int roleMethod() => int baseMethod() with { \r\n"+
					"			result <- result * 5               \r\n"+
					"		}	                                   \r\n"+
					"	}                                          \r\n"+
					"}											   \r\n"
		);
		
		compileFile("MyTeam");
		
		assertTrue(isCompilationSuccessful());		
	}

    /**
	 * A base method with a parameter is used in a callout binding.
	 */
	public void testCalloutParameterMappings1()
	{
		createFile("MyBase",
				   "public class MyBase {                          \r\n"+
				   "    public void baseMethod(boolean valueB) {}    \r\n"+
				   "}                                              \r\n"
		);
	
		createFile("MyTeam",
					"public team class MyTeam {                    \r\n"+
					"	protected class MyRole playedBy MyBase {             \r\n"+
					"		public abstract void roleMethod(int valueA); \r\n"+
					"		void roleMethod(int valueA) -> void baseMethod(boolean valueB) with { \r\n"+
					"			valueA == 1 -> valueB            \r\n"+
					"		}	                                   \r\n"+
					"	}                                          \r\n"+
					"}											   \r\n"
		);
		
		compileFile("MyTeam");
		
		assertTrue(isCompilationSuccessful());		
	}

	/**
	 * A base method with a parameter is used in a callin binding.
	 */
	public void testCallinParameterMappings1()
	{
		createFile("MyBase",
				   "public class MyBase {                          \r\n"+
				   "    public void baseMethod(float valueB) {}    \r\n"+
				   "}                                              \r\n"
		);
	
		createFile("MyTeam",
					"public team class MyTeam {                    \r\n"+
					"	protected class MyRole playedBy MyBase {             \r\n"+
					"		public void roleMethod(float valueA) { \r\n"+
					"		}	                                   \r\n"+
					"		void roleMethod(float valueA) <- before void baseMethod(float valueB) with { \r\n"+
					"			valueA <- valueB * 3.14f            \r\n"+
					"		}	                                   \r\n"+
					"	}                                          \r\n"+
					"}											   \r\n"
		);
		
		compileFile("MyTeam");
		
		assertTrue(isCompilationSuccessful());		
	}
	
	/**
	 * A base method with a parameter list is used in a callout binding.
	 */
	public void testCalloutParameterMappingList1()
	{
		createFile("MyBase",
				   "public class MyBase {						   \r\n"+
				   "    public void baseMethod(float valueB, float valueC) {	   \r\n"+
				   "	}										   \r\n"+
				   "}                                              \r\n"
		);
	
		createFile("MyTeam",
					"public team class MyTeam{					   \r\n"+
					"	protected class MyRole playedBy MyBase {             \r\n"+
					"		public abstract void roleMethod(float valueA); \r\n"+
					"		void roleMethod(float valueA) -> void baseMethod(float valueB, float valueC) with {	\r\n"+
					"			valueA * 3.14f -> valueB ,		   \r\n"+
					"			valueA * 3.14f -> valueC		   \r\n"+
					"		}									   \r\n"+
					"	}										   \r\n"+
					"}											   \r\n"
		);
		
		compileFile("MyTeam");
		
		assertTrue(isCompilationSuccessful());		
	}

	/**
	 * A base method with a parameter list is used in a callin binding.
	 */
	public void testCallinParameterMappingList1()
	{
		createFile("MyBase",
				   "public class MyBase {						   \r\n"+
				   "    public void baseMethod(float valueB, float valueC) {	   \r\n"+
				   "	}										   \r\n"+
				   "}                                              \r\n"
		);
	
		createFile("MyTeam",
					"public team class MyTeam{					   \r\n"+
					"	protected class MyRole playedBy MyBase {             \r\n"+
					"		public void roleMethod(float valueA) { \r\n"+
					"		}									   \r\n"+
					"		void roleMethod(float valueA) <- after void baseMethod(float valueB, float valueC) with {	\r\n"+
					"			valueA <- valueC * 3.14f		   \r\n"+
					"		}									   \r\n"+
					"	}										   \r\n"+
					"}											   \r\n"
		);
	
		compileFile("MyTeam");
		
		assertTrue(isCompilationSuccessful());
	}


	/**
	 * Broken callin-binding:
	 * the base-method's argument list is different from the one in the binding declaration. 
	 */
	public void testCallinParameterMappingList2()
	{
		createFile("MyBase",
				   "public class MyBase {						   \r\n"+
				   "    public void baseMethod(float valueB) {	   \r\n"+
				   "	}										   \r\n"+
				   "}                                              \r\n"
		);
	
		createFile("MyTeam",
					"public team class MyTeam{					   \r\n"+
					"	protected class MyRole playedBy MyBase {             \r\n"+
					"		public void roleMethod(float valueA) { \r\n"+
					"		}									   \r\n"+
					"		void roleMethod(float valueA) <- replace void baseMethod(float valueB, float valueC) with {	\r\n"+
					"			valueA <- valueB * 3.14,		   \r\n"+
					"			valueA <- valueC * 3.14			   \r\n"+
					"		}									   \r\n"+
					"	}										   \r\n"+
					"}											   \r\n"
		);
	
		compileFile("MyTeam");
		
		assertFalse(isCompilationSuccessful());
	}
}
