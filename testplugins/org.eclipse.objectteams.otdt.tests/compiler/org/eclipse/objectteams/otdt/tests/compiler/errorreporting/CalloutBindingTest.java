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
 * $Id: CalloutBindingTest.java 23494 2010-02-05 23:06:44Z stephan $
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
 * This class contains tests concerning callout bindings.
 * 
 * @author kaschja
 * @version $Id: CalloutBindingTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class CalloutBindingTest extends TestBase
{
	public CalloutBindingTest(String testName)
	{
		super(testName);
	}

// There are no ambiguous base-method matches (there's always a best match 
// (one to one))
//  
//	/**
//	 * A callout binding with ambiguous Base-Method Signature 
//	 */
//	public void testCalloutBindingAmbiguousBaseMethod1() 
//	{
//
//		createFile("MyBase","public class MyBase " +
//			  NL + "{ " +
//			  NL + "    protected void baseMethod(short arg){}" +
//			  NL + "    public void baseMethod(int arg){}" +
//			  NL + "} ");
//	      
//		createFile("MySuperTeam","public team class MySuperTeam " +
//			  NL + "{ " +	
//			  NL + "	public class MyRole playedBy MyBase " +
//			  NL + "    { " +	
//			  NL + "	    public  void roleMethod(long arg){};" +
//			  NL + "    } " +
//			  NL + "}");
//
//		createFile("MyTeam","public team class MyTeam extends MySuperTeam" +
//			  NL + "{ " +	
//			  NL + "	public class MyRole" +
//			  NL + "    { " +	
//			  NL + "        roleMethod => baseMethod; " +
//			  NL + "    } " +
//			  NL + "}");
//			  		
//		compileFile("MyTeam");
//		
//		//should throw warning about ambiguous base-method
//		assertTrue(hasExpectedProblems(new int[] { IProblem.CalloutBindingTooManyBaseMatches }));
//	}
//
//	/**
//	 * A callout binding with ambiguous Base-Method Signature 
//	 */
//	public void testCalloutBindingAmbiguousBaseMethod2() 
//	{
//
//		createFile("MyBase","public class MyBase " +
//			  NL + "{ " +
//			  NL + "    public void baseMethod(long arg){}" +
//			  NL + "    public void baseMethod(int arg){}" +
//			  NL + "} ");
//	      
//		createFile("MySuperTeam","public team class MySuperTeam " +
//			  NL + "{ " +	
//			  NL + "	public class MyRole playedBy MyBase " +
//			  NL + "    { " +	
//			  NL + "	    public  void roleMethod(short arg){};" +
//			  NL + "    } " +
//			  NL + "}");
//
//		createFile("MyTeam","public team class MyTeam extends MySuperTeam" +
//			  NL + "{ " +	
//			  NL + "	public class MyRole" +
//			  NL + "    { " +	
//			  NL + "        roleMethod => baseMethod; " +
//			  NL + "    } " +
//			  NL + "}");
//			  		
//		compileFile("MyTeam");
//		
//		//should throw warning about ambiguous base-method
//		assertTrue(hasExpectedProblems(new int[] { IProblem.CalloutBindingTooManyBaseMatches }));
//	}
//
//	/**
//	 * A callout binding with ambiguous Base-Method Signature 
//	 */
//	public void testCalloutBindingAmbiguousBaseMethod3() 
//	{
//
//		createFile("MyBase","public class MyBase " +
//			  NL + "{ " +
//			  NL + "    public void baseMethod(String arg){}" +
//			  NL + "    public void baseMethod(Integer arg){}" +
//			  NL + "} ");
//	      
//		createFile("MySuperTeam","public team class MySuperTeam " +
//			  NL + "{ " +	
//			  NL + "	public class MyRole playedBy MyBase " +
//			  NL + "    { " +	
//			  NL + "	    public  void roleMethod(String arg){};" +
//			  NL + "	    public  void roleMethod(Integer arg){};" +
//			  NL + "    } " +
//			  NL + "}");
//
//		createFile("MyTeam","public team class MyTeam extends MySuperTeam" +
//			  NL + "{ " +	
//			  NL + "	public class MyRole" +
//			  NL + "    { " +	
//			  NL + "        roleMethod => baseMethod; " +
//			  NL + "    } " +
//			  NL + "}");
//			  		
//		compileFile("MyTeam");
//		
//		//should throw warning about ambiguous base-method
//		assertTrue(hasExpectedProblems(new int[] { IProblem.CalloutBindingTooManyBaseMatches }));
//	}

	/**
	 * A callout binding declaration must not include a modifier 
	 * (before/replace/after).
	 */
	public void testCalloutBindingDeclarationWithModifier1() 
	{

	    createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(){}" +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "	    public  abstract void roleMethod();" +
			  NL + "        roleMethod -> after baseMethod; " +
			  NL + "    } "+
			  NL + "}");
		
		compileFile("MyTeam");
		
		assertTrue(hasExpectedProblems(new int[] { IProblem.UnresolvedCalloutMethodSpec, IProblem.ParsingErrorDeleteToken}));
	}
	
	/**
	 * A callout binding declaration must not include a modifier 
	 * (before/replace/after).
	 */
	public void testCalloutBindingMethodArgumentWithModifier1() 
	{

	    createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(final String str){}" +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "	    public  abstract void roleMethod(final String str);" +
			  NL + "        roleMethod -> after baseMethod; " +
			  NL + "    } "+
			  NL + "}");
		
		compileFile("MyTeam");

		assertTrue(hasExpectedProblems(new int[] { IProblem.UnresolvedCalloutMethodSpec, IProblem.ParsingErrorDeleteToken}));
	}
	
	/**
	 * A role method must not be bound to a non-existing base method.
	 */
	public void testBindingOfNonExistingMethod1()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "	    public abstract void roleMethod();" +
			  NL + "        roleMethod -> baseMethod; " +
			  NL + "    } "+
			  NL + "}");
		
		compileFile("MyTeam");

		//TODO(SH):  this is what I expect in 3.2, but other non-resolve error msg would be acceptable, too 
		assertTrue(hasAtLeastExpectedProblems(new int[]{ IProblem.UnresolvedCalloutMethodSpec}));		
	}

	/**
	 * A non-existing role method must not be bound to a base method.
	 */
	public void testBindingOfNonExistingMethod2()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(){} " +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "        roleMethod -> baseMethod; " +
			  NL + "    } "+
			  NL + "}");
		
		compileFile("MyTeam");
		
		//TODO(SH):  this is what I expect in 3.2, but other non-resolve error msg would be acceptable, too 
		assertTrue(hasExpectedProblems(new int[]{ IProblem.UnresolvedCalloutMethodSpec}, null ));		
	}	
	
	/**
	 * An abstract role method must not be bound with the "=>"-operator. 
	 */
	public void testBindingWithWrongOperator1()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(){} " +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "        public abstract void roleMethod();" +
			  NL + "        roleMethod => baseMethod; " +
			  NL + "    } "+
			  NL + "}");
		
		compileFile("MyTeam");
		
		assertTrue(hasExpectedProblems(new int[]{ IProblem.AbstractMethodBoundAsOverride }, null ));					
	}
	
	/**
	 * A non-abstract role method must not be bound with the "->"-operator. 
	 */
	public void testBindingWithWrongOperator2()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(){} " +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "        public void roleMethod(){}" +
			  NL + "        roleMethod -> baseMethod; " +
			  NL + "    } "+
			  NL + "}");
		
		compileFile("MyTeam");

		assertFalse(isCompilationSuccessful());
		assertTrue(hasExpectedProblems(new int[]{ IProblem.RegularCalloutOverrides, IProblem.CalloutOverridesLocal } ));		
	}

	/**
	 * A role method must not be bound to the same base method multiple times.
	 */
	public void testMultipleCalloutBinding1()
	{
		createFile("MyBase","public class MyBase" 
			  + NL + "{"
			  + NL + "    public void baseMethodA(){}" 
			  + NL + "    public void baseMethodB(){}" 
			  + NL + "}");
	      
		createFile("MyTeam",
				   "public team class MyTeam"
			+ NL + "{ " 
			+ NL + "	protected class MyRole playedBy MyBase" 
			+ NL + "    { "
			+ NL + "        public abstract void roleMethod();" 
			+ NL + "        void roleMethod() -> void baseMethodA();" 
			+ NL + "        void roleMethod() -> void baseMethodB();"
			+ NL + "    } "
			+ NL + "}");
				
		compileFile("MyTeam");
		
		assertTrue( hasExpectedProblems(new int[] { IProblem.DuplicateCalloutBinding, IProblem.DuplicateCalloutBinding} ));
	}		
		
	/**
	 * A role method must not be bound to various base methods by callout. 
	 */
	public void testMultipleCalloutBinding2()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethodA(){} " +
		      NL + "    public void baseMethodB(){} " +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "        public abstract void roleMethod();" +
			  NL + "        roleMethod -> baseMethodA; " +
		      NL + "        roleMethod -> baseMethodB; " +
			  NL + "    } "+
			  NL + "}");
				
		compileFile("MyTeam");
		
		assertTrue( hasExpectedProblems(new int[] { IProblem.DuplicateCalloutBinding, IProblem.DuplicateCalloutBinding} ));
	}
	
	/**
	 * A role method must not be bound to various base methods by callout,
	 * no matter where the binding is implemented.
	 */
	public void testMultipleCalloutBinding3()
	{
		createFile("MyBase",
				  "public class MyBase " 
			+ NL + "{" 
			+ NL + "    public void baseMethodA(){}" 
			+ NL + "    public void baseMethodB(){}" 
			+ NL + "}");

		createFile("MySuperTeam",
				   "public team class MySuperTeam " 
			+ NL + "{ " 
			+ NL + "	protected class MyRole playedBy MyBase " 
			+ NL + "    {" 
			+ NL + "        public abstract void roleMethod();" 
			+ NL + "        roleMethod -> baseMethodA;"  
			+ NL + "    }"
			+ NL + "}");
	      
		createFile("MyTeam",
				   "public team class MyTeam extends MySuperTeam" 
			+ NL + "{ "
			+ NL + "	  protected class MyRole playedBy MyBase" 
			+ NL + "    {" 
//			+ NL + "        public abstract void roleMethod();" 
			+ NL + "        roleMethod -> baseMethodB;"  
			+ NL + "    }"
			+ NL + "}");
				
		compileFile("MyTeam");
		
		assertTrue( hasExpectedProblems(new int[] { IProblem.RegularCalloutOverridesCallout} ));
	}	

	/**
	 * A role method must not be bound to the same base method multiple times.
	 */
	public void testMultipleCalloutBinding4()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(){} " +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "        public abstract void roleMethod();" +
			  NL + "        roleMethod -> baseMethod; " +
			  NL + "        roleMethod -> baseMethod; " +
			  NL + "    } "+
			  NL + "}");
				
		compileFile("MyTeam");
		
		assertTrue( hasExpectedProblems( new int[] { IProblem.DuplicateCalloutBinding, IProblem.DuplicateCalloutBinding } ));
	}		

	public void testBindingMatchingRoleBaseMethod()
	{
		createFile("MyException", "public class MyException extends Exception{}");
		
		createFile("AClass",
				  "public class AClass {}"
			+ NL);
		
		createFile("BClass",
				  "public class BClass extends AClass{}"
			+ NL);
		
		createFile("MyBase",
				   "public class MyBase " 
			+ NL + "{ "
			+ NL + "    public void baseMethodAClass(AClass o){}"
			+ NL + "}"
			+ NL);
	      
		createFile("MyTeam",
				   "public team class MyTeam "
			+ NL + "{ "	
			+ NL + "	protected class MyRole playedBy MyBase "
			+ NL + "    { "	
			+ NL + "	    public abstract void roleMethodBClass(BClass o);"
			+ NL + "      roleMethodBClass -> baseMethodAClass;"
			+ NL + "    } "
			+ NL + "}"
			+ NL);
		
		
		compileFile("MyTeam");
		
		assertTrue( isCompilationSuccessful() );
	}

	public void testBindingMatchingRoleBaseMethod2()
	{
		createFile("MyException", "public class MyException extends Exception{}");
		
		createFile("AClass",
				  "public class AClass {}"
			+ NL);
		
		createFile("BClass",
				  "public class BClass extends AClass{}"
			+ NL);
		
		createFile("MyBase",
				   "public class MyBase " 
			+ NL + "{ "
			+ NL + "    public BClass baseMethodBClassReturn(){ return null; }"
			+ NL + "}"
			+ NL);
	      
		createFile("MyTeam",
				   "public team class MyTeam "
			+ NL + "{ "	
			+ NL + "	protected class MyRole playedBy MyBase "
			+ NL + "    { "	
			+ NL + "	    public abstract AClass roleMethodAClassReturn2();"
			+ NL + "      roleMethodAClassReturn2 -> baseMethodBClassReturn;"
			+ NL + "    } "
			+ NL + "}"
			+ NL);
		
		
		compileFile("MyTeam");
		
		assertTrue( isCompilationSuccessful());
	}

	/**
	 * The signatures of role method and bound base method
	 * must have compatible declarations
	 */
	public void testBindingMismatchingRoleBaseMethod1()
	{
		createFile("MyBase",
				   "public class MyBase " 
			+ NL + "{ "
			+ NL + "    public void baseMethod(int i){}"
			+ NL + "}"
			+ NL);
	      
		createFile("MyTeam",
				   "public team class MyTeam "
			+ NL + "{ "	
			+ NL + "	protected class MyRole playedBy MyBase "
			+ NL + "    { "	
			+ NL + "	    public abstract void roleMethod();"
			+ NL + "        roleMethod -> baseMethod; "
			+ NL + "    } "
			+ NL + "}"
			+ NL);
		
		
		compileFile("MyTeam");

		assertTrue( hasExpectedProblems(new int[] { IProblem.TooFewArgumentsInCallout } ));
	}

	/**
	 * The signatures of role method and bound base method
	 * must have compatible declarations. This is the not-so-easy example!
	 */
	public void testBindingMismatchingRoleBaseMethod2()
	{
		createFile("MyException", "public class MyException extends Exception{static final long serialVersionUID=1234;}");
		
		createFile("AClass",
				  "public class AClass {}"
			+ NL);
		
		createFile("BClass",
				  "public class BClass extends AClass{}"
			+ NL);
		
		createFile("MyBase",
				   "public class MyBase " 
			+ NL + "{ "
			+ NL + "    public void baseMethodBClass(BClass o) throws MyException{}"
			+ NL + "}"
			+ NL);
	      
		createFile("MyTeam",
				   "public team class MyTeam "
			+ NL + "{ "	
			+ NL + "	protected class MyRole playedBy MyBase "
			+ NL + "    { "	
			+ NL + "	    public abstract void roleMethodAClass(AClass o);"
			+ NL + "      roleMethodAClass -> baseMethodBClass;"
			+ NL + "    } "
			+ NL + "}"
			+ NL);
		
		
		compileFile("MyTeam");
		assertTrue( hasExpectedProblems(new int[] { IProblem.IncompatibleMappedCalloutArgument, IProblem.CalloutUndeclaredException} ));
	}


	public void testBindingMismatchingRoleBaseMethod3()
	{
		createFile("MyException", "public class MyException extends Exception{static final long serialVersionUID=1234;}");
		
		createFile("AClass",
				  "public class AClass {}"
			+ NL);
		
		createFile("BClass",
				  "public class BClass extends AClass{}"
			+ NL);
		
		createFile("MyBase",
				   "public class MyBase " 
			+ NL + "{ "
			+ NL + "    public void baseMethodAClass(AClass o){}"
			+ NL + "}"
			+ NL);
	      
		createFile("MyTeam",
				   "public team class MyTeam "
			+ NL + "{ "	
			+ NL + "	protected class MyRole playedBy MyBase "
			+ NL + "    { "	
			+ NL + "	    public abstract AClass roleMethodAClassReturn1();"
			+ NL + "      roleMethodAClassReturn1 -> baseMethodAClass;"
			+ NL + "    } "
			+ NL + "}"
			+ NL);
		
		
		compileFile("MyTeam");
		
		assertFalse( isCompilationSuccessful() );
	}

	/**
	 * The signatures of role method and bound base method
	 * must have conform exception declarations (throw-clauses)
	 * 
	 * The role method must not declare throwing an exception
	 * while the base method does not
	 * 
	 * Actually, why shouldn't you be able to add a throws-clause in the role-method?
	 * IMHO, this shouldn't be an error. (carp) 
	 */
	public void testBindingWithDifferentExceptionDeclarations1()
	{
		createFile("MyException", "public class MyException extends Exception{static final long serialVersionUID=1234;}");
		
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(){}" +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "	    public abstract void roleMethod() throws MyException;" +
			  NL + "        roleMethod -> baseMethod; " +
			  NL + "    } "+
			  NL + "}");
		
		
		compileFile("MyTeam");
		
		assertTrue(isCompilationSuccessful());
	}

	/**
	 * The signatures of role method and bound base method
	 * must have conform exception declarations (throw-clauses)
	 * 
	 * The role method must declare throwing an exception
	 * if the base method does
	 */
	public void testBindingWithDifferentExceptionDeclarations2()
	{
		createFile("MyException", "public class MyException extends Exception{static final long serialVersionUID=1234;}");
		
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod() throws MyException " +
			  NL + "    {" +
			  NL + "        throw new MyException();" +
			  NL + "    }" +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "	    public abstract void roleMethod();" +
			  NL + "        roleMethod -> baseMethod; " +
			  NL + "    } "+
			  NL + "}");
		
		compileFile("MyTeam");
		
		assertTrue(hasExpectedProblems(new int[] {IProblem.CalloutUndeclaredException}));			
	}
	
	/**
	 * The signatures of role method and bound base method
	 * must have conform exception declarations (throw-clauses)
	 * 	 
	 * The declared exceptions must not be of different types.
	 */
	public void testBindingWithDifferentExceptionDeclarations3()
	{	
		createFile("MyException", "public class MyException extends Exception{static final long serialVersionUID=1234;}");
		createFile("YourException", "public class YourException extends Exception{static final long serialVersionUID=1234;}");
		
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod() throws MyException " +
		      NL + "    {" +
			  NL + "        throw new MyException();" +
			  NL + "    }" +			  
			  NL + "}");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "	    public abstract void roleMethod() throws YourException;" +
			  NL + "        roleMethod -> baseMethod; " +
			  NL + "    } "+
			  NL + "}");
		
		compileFile("MyTeam");
		
		assertTrue(hasExpectedProblems(new int[] {IProblem.CalloutUndeclaredException}));			
	}	
	
	public void testBrokenBindingDeclaration1()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod()" +
			  NL + "    {" +
			  NL + "    }" +			  
			  NL + "}");

		createFile("MyTeam",
			"public team class MyTeam" +
			NL + "{ " +	
			NL + "	protected class MyRole playedBy MyBase " +
			NL + "  { " +	
			NL + "      public void roleMethod1() {}" +
			NL + "      public void roleMethod2() {}" +
			NL + "      public void roleMethod3() {}" +
			NL + "      public void roleMethod4() {}" +
			NL + "      public void roleMethod5() {}" +
			NL + "      public void roleMethod6() {}" +
			NL + "" +
			NL + "      public void roleMethod11() {}" +
			NL + "      public void roleMethod12() {}" +
			NL + "      public void roleMethod13() {}" +
			NL + "      public void roleMethod14() {}" +
			NL + "      public void roleMethod15() {}" +
			NL + "      public void roleMethod16() {}" +
			NL + "" +
			NL + "      void roleMethod1() -> baseMethod();" +
			NL + "      roleMethod2() -> void baseMethod();" +
			NL + "      roleMethod3() -> baseMethod();" +
			NL + "      roleMethod4() -> baseMethod;" +
			NL + "      roleMethod5 -> baseMethod();" +
			NL + "      roleMethod6() -> baseMethod();" +
			NL + "" +
			NL + "      void roleMethod11 -> baseMethod;" +
			NL + "      roleMethod12 -> void baseMethod;" +
			NL + "      roleMethod13 -> baseMethod;" +
			NL + "      void roleMethod14 -> void baseMethod;" +
			NL + "      void roleMethod15() -> baseMethod;" +
			NL + "      roleMethod16 -> void baseMethod();" +
			NL + "  } "+
			NL + "}");

        compileFile("MyTeam");
        
        //TODO: km: SH, please check
        // assertTrue( hasExpectedProblems(new int[] { IProblem.SyntaxErrorInCallout} ));
	}

	public void testRemainingAbstractMethod1()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod()" +
			  NL + "    {" +
			  NL + "    }" +			  
			  NL + "}");

		createFile("MyTeam",
				 "public team class MyTeam" +
			NL + "{ " +	
			NL + "	protected class MyRole playedBy MyBase " +
			NL + "  { " +	
			NL + "      public abstract void abstractRoleMethod();" +
			NL + "" +
			NL + "      public abstract void roleMethod();" +
			NL + "" +
			NL + "      void roleMethod() -> void baseMethod();" +
			NL + "  } "+
			NL + "}");

		compileFile("MyTeam");
        
		assertTrue( hasExpectedProblems(new int[] {  IProblem.AbstractMethodsInConcreteClass, IProblem.AbstractMethodInAbstractClass } ));
	}

	public void testArrayParameters1()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(int[] baseArray)" +
			  NL + "    {" +
			  NL + "    }" +			  
			  NL + "}");

		createFile("MyTeam",
				 "public team class MyTeam" +
			NL + "{ " +	
			NL + "	protected class MyRole playedBy MyBase " +
			NL + "  { " +	
			NL + "      public abstract void abstractRoleMethod(int[] roleArray);" +
			NL + "" +
			NL + "      void abstractRoleMethod(int[] r) -> void baseMethod(int[] b);" +
			NL + "  } "+
			NL + "}");

		compileFile("MyTeam");
        
		assertTrue(isCompilationSuccessful());		

	}

	public void testArrayParameters2()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(Object[] baseArray)" +
			  NL + "    {" +
			  NL + "    }" +			  
			  NL + "}");

		createFile("MyTeam",
				 "public team class MyTeam" +
			NL + "{ " +	
			NL + "	protected class MyRole playedBy MyBase " +
			NL + "  { " +	
			NL + "      public abstract void abstractRoleMethod(Object[] roleArray);" +
			NL + "" +
			NL + "      void abstractRoleMethod(Object[] r) -> void baseMethod(Object[] b);" +
			NL + "  } "+
			NL + "}");

		compileFile("MyTeam");
        
		assertTrue(isCompilationSuccessful());		

	}

	
	public void testArrayLowering1()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(MyBase[] baseArray)" +
			  NL + "    {" +
			  NL + "    }" +			  
			  NL + "}");

		createFile("MyTeam",
				 "public team class MyTeam" +
			NL + "{ " +	
			NL + "	protected class MyRole playedBy MyBase " +
			NL + "  { " +	
			NL + "      public abstract void abstractRoleMethod(MyRole[] roleArray);" +
			NL + "" +
			NL + "      void abstractRoleMethod(MyRole[] r) -> void baseMethod(MyBase[] b);" +
			NL + "  } "+
			NL + "}");

		compileFile("MyTeam");
        
		assertTrue(isCompilationSuccessful());		

	}
	
	/**
	 * 	Simple test that should work.
	 *
	 */
	public void testSimpleWorkingExample()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(Object obj)" +
			  NL + "    {" +
			  NL + "    }" +			  
			  NL + "}");

		createFile("MyTeam",
				 "public team class MyTeam" +
			NL + "{ " +	
			NL + "	protected class MyRole playedBy MyBase " +
			NL + "  { " +	
			NL + "      public abstract void abstractRoleMethod(Object obj);" +
			NL + "" +
			NL + "      void abstractRoleMethod(Object obj) -> void baseMethod(Object obj);" +
			NL + "  } "+
			NL + "}");

		compileFile("MyTeam");
        
		assertTrue(isCompilationSuccessful());		

	}

	/**
	 * 	Simple test with static base method.
	 */
	public void testStaticBaseMethod()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public static void baseMethod(Object obj)" +
			  NL + "    {" +
			  NL + "    }" +			  
			  NL + "}");

		createFile("MyTeam",
				 "public team class MyTeam" +
			NL + "{ " +	
			NL + "	protected class MyRole playedBy MyBase " +
			NL + "  { " +	
			NL + "      public abstract void abstractRoleMethod(Object obj);" +
			NL + "" +
			NL + "      void abstractRoleMethod(Object obj) -> void baseMethod(Object obj);" +
			NL + "  } "+
			NL + "}");

		compileFile("MyTeam");
        
		assertTrue(isCompilationSuccessful());		

	}
	
	/**
	 * A common callout binding scenario consisting of a base class, a team
	 * with a default constructor, an abstract role class, a role class with
	 * multiple callout bindings and a main method for team instantiation.
	 */
	public void testCalloutBinding1() 
	{
		createFile("MyBase",
					"public class MyBase" 
			+ NL + "{" 
			+ NL + "      public void myBaseMethod(int a)" 
			+ NL + "      {" 
			+ NL + "          System.out.println(\"laber\");" 
			+ NL + "      }" 
			+ NL + "      public void myVoidBaseMethod()" 
			+ NL + "      {" 
			+ NL + "          System.out.println(\"blubber\");" 
			+ NL + "      }" 
			+ NL + "}" 
			+ NL);
			
		createFile("MyTeam",
				   "public team class MyTeam"   
			+ NL + "{" 
			+ NL + "      public MyTeam()" 
			+ NL + "      {" 
			+ NL + "         super();"  
			+ NL + "         Role r = new Role(new MyBase());"
			+ NL + "         r.myCallout(3);"
			+ NL + "         r.myAbstractCallout(4);"
			+ NL + "         r.myAbstractInheritedVoidCallout();"
			+ NL + "      }" 	
			+ NL + ""
			+ NL + "      public abstract class SuperRole" 
			+ NL + "      {"
            + NL + ""            
            + NL + "          void myCallout(int a)"
            + NL + "          {" 
            + NL + "              System.out.println(\"Hallo\");" 
            + NL + "          }"
            + NL + ""            
			+ NL + "          abstract void myAbstractInheritedVoidCallout();"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      public class Role extends SuperRole playedBy MyBase" 
			+ NL + "      {"
			+ NL + "          protected abstract void myAbstractCallout(int a);"
			+ NL + ""  
			+ NL + "          public void method()"
			+ NL + "          {"
			+ NL + "             System.out.println(\"normal method\"); "
			+ NL + "          }"
			+ NL + ""
			+ NL + "          protected void myCallout(int a) => void myBaseMethod(int a);" 
			+ NL + "          void myAbstractCallout(int a) -> void myBaseMethod(int a);"			
			+ NL + "          protected void myAbstractInheritedVoidCallout() -> void myVoidBaseMethod();"			
			+ NL + "      }"
			+ NL + "      public static void main(String[] args){" 
			+ NL + "         new MyTeam();" 
			+ NL + "      };"
			+ NL + "}"
			+ NL);
	
		compileFile("MyTeam");
	
		assertTrue(isCompilationSuccessful());
	}
	
	public void testResultLifting1()
	{
		
		createFile("MyBase",
					"public class MyBase" 
			+ NL + "{" 
			+ NL + "      public MyBase foo()" 
			+ NL + "      {" 
			+ NL + "          return this;" 
			+ NL + "      }"
			+ NL + "}"
			+ NL);  
		
		createFile("MyTeam",
					"public team class MyTeam"
			+ NL + "{" 
			+ NL + "      protected class MyRole playedBy MyBase"
			+ NL + "      {"
			+ NL + "            public void print() {System.out.println(\"OK\");}"
			+ NL + "            protected abstract MyRole bar();"
			+ NL + "            bar -> foo;"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      MyTeam()"
			+ NL + "      {"
			+ NL + "            MyRole r1 = new MyRole(new MyBase());"
			+ NL + "            MyRole r2 = r1.bar();"
			+ NL + "            r2.print();"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      public static void main(String[] args)"
			+ NL + "      {"
			+ NL + "            new MyTeam();"
			+ NL + "      }"
			+ NL + "}"
			+ NL);

		compileFile("MyTeam");

		assertTrue(isCompilationSuccessful());
	}
	
	public void testResultLifting2()
	{
		
		createFile("MyBase",
					"public class MyBase" 
			+ NL + "{" 
			+ NL + "      public MyBase foo()" 
			+ NL + "      {" 
			+ NL + "          return this;" 
			+ NL + "      }"
			+ NL + "}"
			+ NL);  
	
		createFile("MyTeamInterface",
					"public team class MyTeamInterface"
			+ NL + "{" 
			+ NL + "      public interface MyInterface playedBy MyBase"
			+ NL + "      {" 
			+ NL + "            MyInterface bar();"
			+ NL + "      		void print();"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      protected class MyRole implements MyInterface"
			+ NL + "      {"
			+ NL + "            public void print() {System.out.println(\"OK\");}"
			+ NL + "            bar -> foo;"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      MyTeamInterface()"
			+ NL + "      {"
			+ NL + "            MyRole r = new MyRole(new MyBase());"
			+ NL + "            MyInterface rI = r.bar();"
			+ NL + "            rI.print();"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      public static void main(String[] args)"
			+ NL + "      {"
			+ NL + "            new MyTeamInterface();"
			+ NL + "      }"
			+ NL + "}"
			+ NL);

		compileFile("MyTeamInterface");

		assertTrue(isCompilationSuccessful());
	}
	
	public void testResultLifting3()
	{
		
		createFile("MyBase",
					"public class MyBase" 
			+ NL + "{" 
			+ NL + "      public MyBase foo()" 
			+ NL + "      {" 
			+ NL + "          return this;" 
			+ NL + "      }"
			+ NL + "}"
			+ NL);  
	
		createFile("MyBase2",
					"public class MyBase2" 
			+ NL + "{" 
			+ NL + "      public MyBase2 foo()" 
			+ NL + "      {" 
			+ NL + "          return this;" 
			+ NL + "      }"
			+ NL + "}"
			+ NL);  

		createFile("MyTeam",
					"public team class MyTeam"
			+ NL + "{" 
			+ NL + "      abstract protected class MyInnerClass"// playedBy MyBase2"
			+ NL + "      {" 
			+ NL + "            protected abstract MyRole bar();"
			+ NL + "      		public abstract void print();"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      protected class MyRole extends MyInnerClass playedBy MyBase"
			+ NL + "      {"
			+ NL + "            public void print() {System.out.println(\"OK\");}"
			+ NL + "            bar -> foo;"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      MyTeam()"
			+ NL + "      {"
			+ NL + "            MyRole r = new MyRole(new MyBase());"
			+ NL + "            MyInnerClass rI = r.bar();"
			+ NL + "            rI.print();"
			+ NL + "      }"
			+ NL + ""
			+ NL + "      public static void main(String[] args)"
			+ NL + "      {"
			+ NL + "            new MyTeam();"
			+ NL + "      }"
			+ NL + "}"
			+ NL);

		compileFile("MyTeam");

		assertTrue(isCompilationSuccessful());
	}

	public void testSimpleSample()
	{
		
		createFile("MyBase",
					"public class MyBase" 
			+ NL + "{" 
			+ NL + "      public String getValue(int i)" 
			+ NL + "      {" 
			+ NL + "          return \"Hallo\";" 
			+ NL + "      }"
			+ NL + "}"
			+ NL);  

		createFile("MyTeam",
					"public team class MyTeam"
			+ NL + "{" 
			+ NL + "      protected class MyRole playedBy MyBase"
			+ NL + "      {"
			+ NL + "            public abstract String getValue(int i);"
			+ NL + "            getValue -> getValue;"
			+ NL + "      }"
			+ NL + "}"
			+ NL);

		createFile("Main",
					"public class Main" 
			+ NL + "{" 
			+ NL + "      public static void main(String[] args)" 
			+ NL + "      {" 
			+ NL + "          MyTeam teem = new MyTeam();" 
			+ NL + "      }"
			+ NL + "}"
			+ NL);  

		compileFile("Main");

		assertTrue(isCompilationSuccessful());
	}


	public void testIncompatibleReturnTypes()
	{
		
		createFile("MyBase",
					"public class MyBase" 
			+ NL + "{" 
			+ NL + "      public long getValue()" 
			+ NL + "      {" 
			+ NL + "          return 1L;" 
			+ NL + "      }"
			+ NL + "}"
			+ NL);  

		createFile("MyTeam",
					"public team class MyTeam"
			+ NL + "{" 
			+ NL + "      protected class MyRole playedBy MyBase"
			+ NL + "      {"
			+ NL + "            public abstract int getValue();"
			+ NL + "            getValue -> getValue;"
			+ NL + "      }"
			+ NL + "}"
			+ NL);

		createFile("Main",
					"public class Main" 
			+ NL + "{" 
			+ NL + "      public static void main(String[] args)" 
			+ NL + "      {" 
			+ NL + "          MyTeam teem = new MyTeam();" 
			+ NL + "      }"
			+ NL + "}"
			+ NL);  

		compileFile("Main");

		assertFalse(isCompilationSuccessful());
	}


	public void testCalloutOverrideWithCallout()
	{
		
		createFile("MyBase",
					"public class MyBase" 
			+ NL + "{" 	
			+ NL + "      public int getValue()" 
			+ NL + "      {" 
			+ NL + "          return 1;" 
			+ NL + "      }"
			+ NL + "      public int getValueBad()" 
			+ NL + "      {" 
			+ NL + "          return 666;" 
			+ NL + "      }"
			+ NL + "}"
			+ NL);  

		createFile("MyTeam",
					"public team class MyTeam"
			+ NL + "{" 
			+ NL + "      public class MyRole playedBy MyBase"
			+ NL + "      {"
			+ NL + "            protected abstract int getValue();"
			+ NL + "            int getValue() -> int getValueBad();"
			+ NL + "      }"
			+ NL + "}"
			+ NL);

		createFile("MySubTeam",
					"public team class MySubTeam extends MyTeam							"
			+ NL + "{" 
			+ NL + "      public class MyRole"
			+ NL + "      {"
			+ NL + "            int getValue() => int getValue();"
			+ NL + "      }"
			+ NL + "       public int getValue(MyBase as MyRole role) { return  role.getValue(); }"
			+ NL + "}"
			+ NL);

		createFile("Main",
					"public class Main" 
			+ NL + "{" 
			+ NL + "      public static void main(String[] args)" 
			+ NL + "      {" 
			+ NL + "          MySubTeam teem = new MySubTeam();" 
			+ NL + "          System.out.println(teem.getValue(new MyBase()));" 
			+ NL + "      }"
			+ NL + "}"
			+ NL);  

		compileFile("Main");

		assertTrue(isCompilationSuccessful());
	}

	/**
	 * An overloaded base-method cannot be bound without specifying the full signature.
	 */
	public void testAmbiguousBinding1()
	{
		createFile("MyBase","public class MyBase " +
			  NL + "{ " +
			  NL + "    public void baseMethod(){} " +
			  NL + "    protected void baseMethod(String arg){} " +
			  NL + "} ");
	      
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase " +
			  NL + "    { " +	
			  NL + "        public abstract void roleMethod();" +
			  NL + "        roleMethod -> baseMethod; " +
			  NL + "    } "+
			  NL + "}");
				
		compileFile("MyTeam");
		
		assertFalse(isCompilationSuccessful());
	}		



}
