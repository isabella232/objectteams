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
 * $Id: OTSpecificSelectionWithinRoleTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.selection;

import org.eclipse.jdt.core.tests.compiler.parser.AbstractSelectionTest;

/**
 * Testing OT-specific selections within a role class.
 * 
 * @author brcan
 */
@SuppressWarnings("nls")
public class OTSpecificSelectionWithinRoleTests extends AbstractSelectionTest
{
	public OTSpecificSelectionWithinRoleTests(String testName) 
	{
		super(testName);
	}
	
	//type declarations
	//NOTE(gbr): type declarations are tested differently than type references
	//(see org.eclipse.objectteams.otdt.tests.selection.codeselect.CodeSelectionTests). 	

//	/**
//	 * Select role class name of inlined role class declaration.
//	 */
//	public void test01() 
//	{
//		String str = 
//			"public team class T1 {\n" +
//			"  public class R1 {\n" +
//			"  }\n" +
//			"}\n";
//	
//		String selectionStartBehind = "public class ";
//		String selectionEndBehind = "R1";
//		
//		String expectedCompletionNodeToString = "<SelectOnType:R1>";
//		String completionIdentifier = "R1";
//		String expectedUnitDisplayString =
//			"public team class T1 {\n" +
//			"  public role class <SelectOnType:R1> {\n" +
//			"  }\n" +
//			"  public T1() {\n" +
//			"  }\n" +
//			"}\n";
//		String expectedReplacedSource = "R1";
//		String testName = "<select role class name of inlined role class declaration.>";
//	
//		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
//		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
//			
//		this.checkDietParse(
//			str.toCharArray(),
//			selectionStart,
//			selectionEnd,
//			expectedCompletionNodeToString,
//			expectedUnitDisplayString,
//			completionIdentifier,
//			expectedReplacedSource,
//			testName);
//
//}
	
	//type references
	/**
	 * Select role class name of externalized role.
	 */
	public void test02() 
	{
		String str = 
			"public team class T1 {\n" +
			"  final T1 t1 = new T1();\n" +
			"  t1.R1 r1;\n" +
			"  public class R1 {\n" +
			"  }\n" +
			"}\n";
	
		String selectionStartBehind = "t1.";
		String selectionEndBehind = ".R1";
		
		String expectedCompletionNodeToString = "<SelectOnType:t1.R1>";
		String completionIdentifier = "R1";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" +
			"    }\n" +
			"  }\n" +
			"  final T1 t1;\n" +
			"  <SelectOnType:t1.R1> r1;\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "t1.R1";
		String testName = "<select role class name of externalized role>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkDietParse(
			str.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
	}
	
	//type references
	/**
	 * Select role class name of externalized role, new syntax
	 */
	public void testExternalizedRoleField() 
	{
		String str = 
			"public team class T1 {\n" +
			"  final T1 t1 = new T1();\n" +
			"  /*here*/R1<@t1> r1;\n" +
			"  public class R1 {\n" +
			"  }\n" +
			"}\n";
	
		String selectionStartBehind = "/*here*/";
		String selectionEndBehind = "/R1";
		
		String expectedCompletionNodeToString = "<SelectOnType:R1<@t1>>";
		String completionIdentifier = "R1";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" +
			"    }\n" +
			"  }\n" +
			"  final T1 t1;\n" +
			"  <SelectOnType:R1<@t1>> r1;\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "R1";
		String testName = "<select role class name of externalized role - new syntax>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkDietParse(
			str.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
	}
		//type references
	/**
	 * Select role class name of externalized role, new syntax
	 */
	public void testExternalizedRoleAllocation() 
	{
		String str = 
			"public team class T1 {\n" +
			"  void test(final T1 t1) {\n" +
			"    R1<@t1> r1 = new R1<@t1>();\n" +
			"  }\n" +
			"  public class R1 {\n" +
			"  }\n" +
			"}\n";
	
		String selectionStartBehind = " new ";
		String selectionEndBehind = "new R1";
		
		String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new R1<@t1>()>";
		String completionIdentifier = "R1";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" +
			"    }\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"  void test(final T1 t1) {\n" +
			"    R1<@t1> r1 = <SelectOnAllocationExpression:new R1<@t1>()>;\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "new R1<@t1>()";
		String testName = "<select role class name in allocation of externalized role - new syntax>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkMethodParse(
			str.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
	}
	
	/**
	 * Select role class name in lifting method (declared lifting).
	 */
	public void test03()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public void m1(B1 as R1 arg) {\n" +
			"  }\n" +
			"  public class R1 playedBy B1 {\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "as ";
		String selectionEndBehind = "as R1";
		
		String expectedCompletionNodeToString = "<SelectOnType:R1>";
		String completionIdentifier = "R1";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"  public void m1(B1 as <SelectOnType:R1> arg) {\n" +
			"  }\n" +
			"}\n"; 
		
		String expectedReplacedSource = "R1";
		String testName = "<select role class name in lifting method>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkDietParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}

	/**
	 * Select role class name in <code>playedBy</code>-relation.
	 */
	public void test04()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy base.R2 {\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "base.";
		String selectionEndBehind = "R2";
		
		String expectedCompletionNodeToString = "<SelectOnType:_OT$base.R2>";
		String completionIdentifier = "R2";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 playedBy <SelectOnType:_OT$base.R2> {\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "base.R2";
		String testName = "<select role class name in playedBy relation>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkDietParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}

	/**
	 * Select role class name in method spec. Role class is a return type in the method spec.
	 */
	public void test05()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    R2 m1() -> R2 n1();\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "B1 {\n    ";
		String selectionEndBehind = "    R2";
		
		String expectedCompletionNodeToString = "<SelectOnType:R2>";
		String completionIdentifier = "R2";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    <SelectOnType:R2> m1() -> R2 n1();\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "R2";
		String testName = "<select role class name in method spec representing the return type>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkDietParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}
	
	/**
	 * Select role class name in method spec. Role class is a parameter type in the method spec.
	 */
	public void test06()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    void m1(R2 r2) -> void n1(R2 r2);\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "m1(";
		String selectionEndBehind = "m1(R2";
		
		String expectedCompletionNodeToString = "<SelectOnType:R2>";
		String completionIdentifier = "R2";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    void m1(<SelectOnType:R2> r2) -> void n1(R2 r2);\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "R2";
		String testName = "<select role class name in method spec representing a parameter type>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkDietParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}

	//method declarations
	//NOTE(gbr): method declarations are tested differently than method references
	//(see org.eclipse.objectteams.otdt.tests.selection.codeselect.CodeSelectionTests).
	
	//method references	
	/**
	 * Select role method name in a role method.
	 */
	public void test07()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    public void rm1() {\n" +
			"    }\n" +
			"    public void rm2() {\n" +
			"      rm1();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "rm2() {\n      ";
		String selectionEndBehind = "      rm1";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:rm1()>";
		String completionIdentifier = "rm1";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    public void rm1() {\n" +
			"    }\n" +
			"    public void rm2() {\n" +
			"      <SelectOnMessageSend:rm1()>;\n" +
			"    }\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n"; 
		
		String expectedReplacedSource = "rm1()";
		String testName = "<select role method name in a role method>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkMethodParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}
		
	/**
	 * Select role method name in a callin binding.
	 */
	public void test08()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    public void rm1() {\n" +
			"    }\n" +
			"    rm1 <- after n1;\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "}\n    ";
		String selectionEndBehind = "    rm1";
		
		String expectedCompletionNodeToString = "<SelectOnMethodSpec:rm1>";
		String completionIdentifier = "rm1";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    public void rm1() {\n" +
			"    }\n" +
			"    <<select role method name in a callin bin:5,4>:\n" +
			"      <SelectOnMethodSpec:rm1> <- after n1;\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "rm1";
		String testName = "<select role method name in a callin binding>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkDietParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}

	//TODO(gbr) left method spec of a callout binding is a role method declaration
	//	         -> move to test class CodeSelectionTests
//	/**
//	 * Select role method name in a callout binding.
//	 */
//	public void test09()
//	{
//		String str = 
//		    "public team class T1 {\n" +
//			"  public class R1 playedBy B1 {\n" +
//			"    public void rm1() {\n" +
//			"    }\n" +
//			"    rm1 -> n1;\n" +
//			"  }\n" +
//			"}\n"; 
//	
//		String selectionStartBehind = "}\n    ";
//		String selectionEndBehind = "    rm1";
//		
//		String expectedCompletionNodeToString = "<SelectOnMessageSend:rm1>";
//		String completionIdentifier = "rm1";
//		String expectedUnitDisplayString =
//		    "public team class T1 {\n" +
//			"  public class R1 playedBy B1 {\n" +
//			"    public abstract void rm1();\n" +
//			"    <SelectOnMessageSend:rm1> -> n1;\n" +
//			"  }\n" +
//			"  public T1() {\n" +
//			"  }\n" +
//			"}\n";
//		
//		String expectedReplacedSource = "rm1";
//		String testName = "<select role method name in a callout binding>";
//	
//		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
//		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
//			
//		this.checkDietParse(
//			str.toCharArray(), 
//			selectionStart,
//			selectionEnd,
//			expectedCompletionNodeToString,
//			expectedUnitDisplayString,
//			completionIdentifier,
//			expectedReplacedSource,
//			testName); 
//	}

	/**
	 * Select role method name in an expression inside a parameter mapping.
	 */
	public void test10()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
	        "    public abstract int rm1();\n" +
	        "    int rm1() -> int n1(int x) with {\n" +
	        "      rm1() -> x,\n" +
	        "      result <- result\n" +
	        "    }\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "with {\n      ";
		String selectionEndBehind = "      rm1";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:rm1()>";
		String completionIdentifier = "rm1";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
	        "    public abstract int rm1();\n" +
	        "    int rm1() -> int n1(int x) with {\n" +
	        "      <SelectOnMessageSend:rm1()> -> <MISSING>\n" +
	        "    }\n" +
	        "  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "rm1()";
		String testName = "<select role method name in an expression inside a parameter mapping.>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkMethodParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}

	/**
	 * Select role method name in an expression inside a parameter mapping (RHS)
	 */
	public void test10a()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
	        "    public abstract int rm1();\n" +
	        "    int rm1() -> int n1(int x) with {\n" +
	        "      rm1() -> x,\n" +
	        "      result <- rm1()\n" +
	        "    }\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "      result <- ";
		String selectionEndBehind = "<- rm1";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:rm1()>";
		String completionIdentifier = "rm1";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
	        "    public abstract int rm1();\n" +
	        "    int rm1() -> int n1(int x) with {\n" +
	        "      rm1() -> x,\n"+
	        "      result <- <SelectOnMessageSend:rm1()>\n" +
	        "    }\n" +
	        "  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "rm1()";
		String testName = "<select role method name in an expression inside a parameter mapping.>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkMethodParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}
	/**
	 * Select role method name in a callin method.
	 */
	public void test11()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    public void rm1() {\n" +
			"    }\n" +
			"    public callin void rm2() {\n" +
			"      rm1();\n" +
			"      base.rm2();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "rm2() {\n      ";
		String selectionEndBehind = "      rm1";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:rm1()>";
		String completionIdentifier = "rm1";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    public void rm1() {\n" +
			"    }\n" +
			"    public callin void rm2() {\n" +
			"      <SelectOnMessageSend:rm1()>;\n" +
			"    }\n" +
			"  }\n" +
			"  public T1() {\n" + 
			"  }\n" +			
			"}\n"; 
		
		String expectedReplacedSource = "rm1()";
		String testName = "<select role method name in a callin method>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkMethodParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}


	/**
	 * Select base call in a callin method.
	 */
	public void test11b()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    public void rm1() {\n" +
			"    }\n" +
			"    public callin void rm2() {\n" +
			"      rm1();\n" +
			"      base.rm2();\n" +
			"    }\n" +
			"    rm2 <- replace foo;" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "base.";
		String selectionEndBehind = "base.rm2";
		
		String expectedCompletionNodeToString = "<SelectOnBaseCallMessageSend:base.rm2()>";
		String expectedSelectionIdentifier = "rm2";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    public void rm1() {\n" +
			"    }\n" +
			"    public callin void rm2() {\n" +
			"      <SelectOnBaseCallMessageSend:base.rm2()>;\n" +
			"    }\n" +
			"    <<select base call in a callin me:9,4>:\n" +
			"      rm2 <- replace foo;\n" +
			"  }\n" +
			"  public T1() {\n" + 
			"  }\n" +			
			"}\n"; 
		
		String expectedSelectedSource = "base.rm2()";
		String testName = "<select base call in a callin method>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkMethodParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			expectedSelectionIdentifier,
			expectedSelectedSource,
			testName); 
	}

	/**
	 * Select role method name in role-level guard.
	 */
	public void test12()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 when (this.isNotNull()) {\n" +
			"    public boolean isNotNull() {\n" +
			"      return true;" +
			"    }\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "when (this.";
		String selectionEndBehind = "this.isNotNull";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:this.isNotNull()>";
		String completionIdentifier = "isNotNull";
		String expectedUnitDisplayString =
			"public team class T1 {\n" + 
			"  public role class R1 playedBy B1 {\n" + 
			"    public boolean isNotNull() {\n" + 
			"    }\n" + 
			"    protected synchronized boolean _OT$when() {\n" + 
			"      return <SelectOnMessageSend:this.isNotNull()>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" + 
			"  }\n" + 
			"}\n";
		
		String expectedReplacedSource = "this.isNotNull()";
		String testName = "<select role method name in role-level guard>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkDietParse(
			str.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
	}

	/**
	 * Select base method name in role-level base guard.
	 * Former bug: base.m() was interpreted as base-call due to doubly 
	 *   parsing the predicate (2nd parse with incorrect parser state). 
	 */
	public void test12base()
	{
		String str = 
		    "public team class T1 {\n" +
		    "  void foo() { return; }\n" +
			"  public class R1 playedBy B1 base when (base.isReady()) {\n" +
			"    public boolean isNotNull() {\n" +
			"      return true;" +
			"    }\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "when (base.";
		String selectionEndBehind = "base.isReady";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:base.isReady()>";
		String completionIdentifier = "isReady";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" + 
			"    public boolean isNotNull() {\n" + 
			"    }\n" + 
			"    protected static synchronized boolean _OT$base_when(final _OT$unknownBaseType base) {\n" + 
			"      return <SelectOnMessageSend:base.isReady()>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" + 
			"  }\n" + 
			"  void foo() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "base.isReady()";
		String testName = "<select role method name in role-level base guard>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkMethodParse(
			str.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
	}

	//field declarations
	//NOTE(gbr): field declarations are tested differently than field references
	//(see org.eclipse.objectteams.otdt.tests.selection.codeselect.CodeSelectionTests).
	
	//field references
	/**
	 * Select role field name in a role method.
	 */
	public void test13()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    private int f;\n" +
			"    public void rm1() {\n" +
			"      this.f = 1;\n" +
			"    }\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "rm1() {\n      this.";
		String selectionEndBehind = "      this.f";
		
		String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.f>";
		String completionIdentifier = "f";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    private int f;\n" +
			"    public void rm1() {\n" +
			"      <SelectionOnFieldReference:this.f>;\n" +
			"    }\n" +
			"  }\n" +
			"  public T1() {\n" + 
			"  }\n" + 			
			"}\n"; 
		
		String expectedReplacedSource = "this.f";
		String testName = "<select role field name in a role method>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkMethodParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}

	/**
	 * Select role field name in a callin method.
	 */
	public void test14()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    private int f;\n" +
			"    public callin void rm1() {\n" +
			"      f = 2;\n" +
			"      base.rm1();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "rm1() {\n      ";
		String selectionEndBehind = "      f";
		
		String expectedCompletionNodeToString = "<SelectOnName:f>";
		String completionIdentifier = "f";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    private int f;\n" +
			"    public callin void rm1() {\n" +
			"      <SelectOnName:f>;\n" +
			"    }\n" +
			"  }\n" +
			"  public T1() {\n" + 
			"  }\n" + 			
			"}\n"; 
		
		String expectedReplacedSource = "f";
		String testName = "<select role field name in a callin method>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkMethodParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}


	/**
	 * Select role field name in a callin method, syntactically a field reference.
	 */
	public void test14b()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    private int f;\n" +
			"    public callin void rm1() {\n" +
			"      this.f = 2;\n" +
			"      base.rm1();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "rm1() {\n      this.";
		String selectionEndBehind = "      this.f";
		
		String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.f>";
		String completionIdentifier = "f";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    private int f;\n" +
			"    public callin void rm1() {\n" +
			"      <SelectionOnFieldReference:this.f>;\n" +
			"    }\n" +
			"  }\n" +
			"  public T1() {\n" + 
			"  }\n" + 			
			"}\n"; 
		
		String expectedReplacedSource = "this.f";
		String testName = "<select role field name in a callin method>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkMethodParse(
			str.toCharArray(), 
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName); 
	}

	/**
	 * Select role field name in an expression inside a parameter mapping.
	 */
	public void test15()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    private int f;\n" +
	        "    public abstract int rm1();\n" +
	        "    int rm1() -> int n1(int x) with {\n" +
	        "      f -> x,\n" +
	        "      result <- result\n" +
	        "    }\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "with {\n      ";
		String selectionEndBehind = "      f";
		
		String expectedCompletionNodeToString = "<SelectOnName:f>";
		String completionIdentifier = "f";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    private int f;\n" +
	        "    public abstract int rm1();\n" +
	        "    int rm1() -> int n1(int x) with {\n"+
	        "      <SelectOnName:f> -> <MISSING>\n" +
	        "    }\n" +
	        "  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "f";
		String testName = "<select role field name in an expression inside a parameter mapping.>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkMethodParse(
			str.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
	}

	/**
	 * Select role field name in role-level guard.
	 */
	public void test16()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 when (this.f != 0) {\n" +
			"    private int f;\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "when (this.";
		String selectionEndBehind = "f";
		
		String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.f>";
		String completionIdentifier = "f";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    private int f;\n" +
			"    protected synchronized boolean _OT$when() {\n" + 
			"      return (<SelectionOnFieldReference:this.f> != 0);\n" + 
			"    }\n" + 			
			"  }\n" +
			"  public T1() {\n" + 
			"  }\n" + 			
			"}\n"; 
		
		String expectedReplacedSource = "this.f";
		String testName = "<select role field name in role-level guard>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkDietParse(
			str.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
	}

	/**
	 * Select method spec of buggy callout (mixin short and long variant)
	 */
	public void test17()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    void toString() => toString;\n" +
			"    hashCode => hashCode;\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "void ";
		String selectionEndBehind = "void toString";
		
		String expectedCompletionNodeToString = "<SelectOnMethodSpec:void toString()>";
		String completionIdentifier = "toString";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    <SelectOnMethodSpec:void toString()> =>  <nullBaseMethod>;\n" + 
			"    hashCode => hashCode;\n" +
			"  }\n" +
			"  public T1() {\n" + 
			"  }\n" + 			
			"}\n"; 
		
		String expectedReplacedSource = "toString";
		String testName = "<select method spec in buggy callout>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
		this.checkDietParse(
			str.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
	}
	
	public void test18() 
	{

		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy B1 {\n" +
			"    int f() {\n" +
			"       return bm1(3);\n"+
			"    }\n"+
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "return ";
		String selectionEndBehind = "bm1";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:bm1(3)>";
		String completionIdentifier = "bm1";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public role class R1 playedBy B1 {\n" +
			"    int f() {\n" +
			"      return <SelectOnMessageSend:bm1(3)>;\n"+
			"    }\n"+
			"  }\n" +
			"  public T1() {\n"+
			"  }\n"+
			"}\n"; 
		
		String expectedReplacedSource = "bm1(3)";
		String testName = "<select application of inferred callout>";
	
		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;
			
					
		this.checkMethodParse(
			str.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		
	}

}
