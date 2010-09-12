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
 * $Id: CallinBindingTest.java 23494 2010-02-05 23:06:44Z stephan $
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
 * This class contains tests concerning callin bindings.
 * 
 * @author kaschja
 * @version $Id: CallinBindingTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class CallinBindingTest extends TestBase
{
    public CallinBindingTest(String testName)
    {
        super(testName);
    }

	/**
	 * A "replace-callin binding" is used with a base call.
	 * Bound role method is declared as callin method.
	 */
	public void testCallinInMethodDeclaration() 
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public int baseMethod(int i) {return 1;}" +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "	    callin int roleMethod(int i)" +
			  NL + "        {" +
			  NL + "            return base.roleMethod(i);  " +
			  NL + "        }" +
			  NL + "        roleMethod <- replace baseMethod; " +
			  NL + "    } "+
			  NL + "}");
		
		compileFile("MyTeam");
		
		assertTrue(isCompilationSuccessful());
	}
	
	/**
	 * A base-call must have arguments matching the role-method's signature.
	 */
	public void testBrokenBaseCall() 
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(int i) {}" +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "	    callin void roleMethod(int i)" +
			  NL + "        {" +
			  NL + "            base.roleMethod();  " +
			  NL + "        }" +
			  NL + "        roleMethod <- replace baseMethod; " +
			  NL + "    } "+
			  NL + "}");
		
		compileFile("MyTeam");
		
		assertTrue(hasExpectedProblems(new int[] { IProblem.BaseCallDoesntMatchRoleMethodSignature }));
	}
	
    /**
	 * A base call in a role method.
	 * Bound role method must be declared as callin method.
	 */
	public void testMissingCallinModifierInMethodDeclaration1() 
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod() {}" +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "	    public void roleMethod()" +
			  NL + "        {" +
			  NL + "            base.roleMethod();  " +
			  NL + "        }" +
			  NL + "    } "+
			  NL + "}");
		
		compileFile("MyTeam");
		
		assertTrue(hasExpectedProblems(new int[] {IProblem.BasecallInRegularMethod }));
	}
	
	/**
	 * A callin binding declaration must include a modifier 
	 * (before/replace/after).
	 */
	public void testCallinBindingDeclarationWithoutModifier1() 
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod() {}" +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "	    public void roleMethod() {}" +
			  NL + "        roleMethod <- baseMethod;" +
			  NL + "    } "+
			  NL + "}");
	
		compileFile("MyTeam");
		
		assertTrue(hasExpectedProblems(new int[] { IProblem.CallinReplaceKeyWordNotOptional }));
	}

	/**
	 * A method in a regular class must not be declared as callin method.
	 */
	public void testCallinModifierDeclaredInMethodOfRegularClass1()
	{
		createFile("MyClass","public class MyClass" +
			  NL + "{" +
			  NL + "   callin void classMethod() {}" +
			  NL + "}");
		
		compileFile("MyClass");
		
		assertTrue(hasExpectedProblems(new int[] {IProblem.OTKeywordInRegularClass}));
	}
	
	/**
	 * A callin method must not be called by the role itself
	 */
	public void testCallinMethodCalledInOtherMethodOfSameRole1()
	{
		createFile("MyBase","public class MyBase {}");
		
		createFile("MyTeam","public team class MyTeam" +
			  NL + "{" +
		      NL + "   protected class MyRole playedBy MyBase" +
		      NL + "   {" +
		      NL + "       callin void callinMethod() {}" +
		      NL + "       void roleMethod()" +
		      NL + "       {" +
		      NL + "           callinMethod();" + 
		      NL + "       }" +
		      NL + "   }" +
			  NL + "}");
			  
		compileFile("MyTeam");
		
		assertTrue(hasExpectedProblems(new int[] {IProblem.CallToCallin, IProblem.DefinitelyMissingBaseCall}));
	}
	
	/**
	 * It is an allowed alternative to declare a callin-binding 
	 * with full method signature
	 */
	public void testMethodsWithSignaturesInCallinBindings1()
	{
		createFile("MyBase","public class MyBase" +
			  NL + "{" +
			  NL + "   public void baseMethodA() {}" + 
			  NL + "   public void baseMethodB() {}" +																				
			  NL + "   public void baseMethodC() {}" +																				
			  NL + "}");															
							
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{" +																						
			  NL + "   protected class MyRole playedBy MyBase" + 
			  NL + "   {" +																			
			  NL + "       void callinMethodA() {}" +																		
		      NL + "       callin void callinMethodB() {}" +																		
		      NL + "       void callinMethodA() <- after void baseMethodA();" +														
			  NL + "	   void callinMethodB() <- replace void baseMethodB(),  void baseMethodC();" +							
			  NL + "   }" +
			  NL + "}");
	
		compileFile("MyTeam");

		assertTrue(isCompilationSuccessful());	
	}
	
	/**
	 * It is an allowed alternative to declare a callin-binding
	 * without method signature
	 */
	public void testMethodsWithoutSignaturesInCallinBindings1()
	{
		createFile("MyBase","public class MyBase" +
			  NL + "{" +
			  NL + "   public void baseMethodA() {}" + 
			  NL + "   public void baseMethodB() {}" +																				
			  NL + "   public void baseMethodC() {}" +																				
			  NL + "}");															
							
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{" +																						
			  NL + "   protected class MyRole playedBy MyBase" + 
			  NL + "   {" +																			
			  NL + "       void callinMethodA() {}" +																		
			  NL + "       callin void callinMethodB() {}" +																		
			  NL + "       callinMethodA <- before baseMethodA;" +														
			  NL + "	   callinMethodB <- replace baseMethodB, baseMethodC;" +							
			  NL + "   }" +
			  NL + "}");
	
		compileFile("MyTeam");

		assertTrue(isCompilationSuccessful());	
	}
}
