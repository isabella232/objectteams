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
 * $Id: OTSpecificSelectionWithinTeamTests.java 23494 2010-02-05 23:06:44Z stephan $
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
 * Testing OT-specific selections within a team class.
 * 
 * @author jwloka
 * @version $Id: OTSpecificSelectionWithinTeamTests.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OTSpecificSelectionWithinTeamTests extends AbstractSelectionTest 
{
	public OTSpecificSelectionWithinTeamTests(String testName) 
	{
		super(testName);
	}

	//type declarations
	//NOTE(gbr): type declarations are tested differently than type references
	//(see org.eclipse.objectteams.otdt.tests.selection.codeselect.CodeSelectionTests).

//	/**
//	 * Select team class name of team class declaration.
//	 */
//	public void test01()
//	{
//		String str = 
//		    "public team class T1 {\n" +
//			"\n" +
//			"}\n"; 
//	
//		String selectionStartBehind = "class ";
//		String selectionEndBehind = "T1";
//		
//		String expectedCompletionNodeToString = "<SelectOnType:T1>";
//		String completionIdentifier = "T1";
//		String expectedUnitDisplayString =
//			"public team class <SelectOnType:T1> {\n" +
//			"\n" +
//			"	public T1() {\n" +
//			"	\n" +
//			"	}\n" +
//			"}\n";
//		
//		String expectedReplacedSource = "T1";
//		String testName = "<select team class name of team class declaration>";
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
//	
//	/**
//	 * Select team class name of nested team class declaration.
//	 */
//	public void test02() 
//	{
//		String str = 
//			"public team class T1 {\n" +
//			"{\n" +
//			"	public team class TR1 {\n" +
//			"	\n" +
//			"	}\n" +
//			"}\n";
//	
//		String selectionStartBehind = "\n{\n\n\tpublic team class ";
//		String selectionEndBehind = "TR1";
//		
//		String expectedCompletionNodeToString = "<SelectOnType:TR1>";
//		String completionIdentifier = "TR1";
//		String expectedUnitDisplayString =
//			"public team class T1 {\n" +
//			"\n" + 
//			"	public T1() {\n" +
//			"	\n" +
//			"	}\n" +
//			"	public team class <SelectOnType:TR1> {\n" +
//			"	\n" +
//			"	}\n" +
//			"}\n";
//		
//		String expectedReplacedSource = "TR1";
//		String testName = "<select team class name of nested team class declaration>";
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
//}
	
	//type references	
	/**
	 * Select team class name of anchor type.
	 */
	public void test03() 
	{
		String str = 
			"public team class T1 {\n" +
			"  T1 t1 = new T1();\n" +
			"  t1.R1 r1;\n" +
			"  public class R1 {\n" +
			"  }\n" +
			"}\n";
	
		String selectionStartBehind = "T1 {\n  ";
		String selectionEndBehind = "  T1";
		
		String expectedCompletionNodeToString = "<SelectOnType:T1>";
		String completionIdentifier = "T1";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" +
			"    }\n" +
			"  }\n" +
			"  <SelectOnType:T1> t1;\n" +
			"  t1.R1 r1;\n" +
			"  public T1() {\n" +
			"  }\n" +			
			"}\n";
		
		String expectedReplacedSource = "T1";
		String testName = "<select team class name of anchored type>";
	
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
	 * Select team class name in <code>within</code>-expression.
	 */
	public void test04()
	{
		String str = 
		    "public team class T1 {\n" +
		    "  void foo() {\n" +
			"    within (new T2()) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
	
		String selectionStartBehind = "new ";
		String selectionEndBehind = "T2";
		
		String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new T2()>";
		String completionIdentifier = "T2";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public T1() {\n" +
			"  }\n" +			
		    "  void foo() {\n" +
			"    <SelectOnAllocationExpression:new T2()>;\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "new T2()";
		String testName = "<select team instantiation in within expression>";
	
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
	 * Select team class name in <code>playedBy</code>-relation.
	 */
	public void test05()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public class R1 playedBy T2 {\n" +
			"  \n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "playedBy ";
		String selectionEndBehind = "T2";
		
		String expectedCompletionNodeToString = "<SelectOnType:T2>";
		String completionIdentifier = "T2";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 playedBy <SelectOnType:T2> {\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "T2";
		String testName = "<select team class name in playedBy relation>";
	
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
	 * Select team class name in import declaration.
	 */
	public void test06()
	{
		String str = 
		    "import x.y.T2\n" +
		    "public team class T1 {\n" +
			"  public class R1 playedBy T2 {\n" +
			"  \n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "y.";
		String selectionEndBehind = "y.T2";
		
		String expectedCompletionNodeToString = "<SelectOnImport:x.y.T2>";
		String completionIdentifier = "T2";
		String expectedUnitDisplayString =
		    "import <SelectOnImport:x.y.T2>;\n" +
			"public team class T1 {\n" +
			"  public role class R1 playedBy T2 {\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "x.y.T2";
		String testName = "<select team class name in import declaration>";
	
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
	 * Select team class name in lifting method (declared lifting).
	 * The team class is the role class.
	 */
	public void test07()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public void m1(B1 as T2 arg) {\n" +
			"  }\n" +
			"  public team class T2 playedBy B1 {\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "as ";
		String selectionEndBehind = "as T2";
		
		String expectedCompletionNodeToString = "<SelectOnType:T2>";
		String completionIdentifier = "T2";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public role team class T2 playedBy B1 {\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"  public void m1(B1 as <SelectOnType:T2> arg) {\n" +
			"  }\n" +
			"}\n"; 
		
		String expectedReplacedSource = "T2";
		String testName = "<select team class name in lifting method>";
	
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
	 * Select team class name in lifting method (declared lifting).
	 * The team class is the base class.
	 */
	public void test08()
	{
		String str = 
		    "public team class T1 {\n" +
			"  public void m1(T2 as R1 arg) {\n" +
			"  }\n" +
			"  public class R1 playedBy T2 {\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "(";
		String selectionEndBehind = "(T2";
		
		String expectedCompletionNodeToString = "<SelectOnType:T2>";
		String completionIdentifier = "T2";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public role class R1 playedBy T2 {\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"  public void m1(<SelectOnType:T2> as R1 arg) {\n" +
			"  }\n" +
			"}\n"; 
		
		String expectedReplacedSource = "T2";
		String testName = "<select team class name in lifting method>";
	
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
	 * Select team class name in team-level guard.
	 */
	public void test09()
	{
		String str = 
			"public class T2 {\n" +
		    "  public team class T1 when (T2.this != null) {\n" +
			"  }\n" +
			"}\n";
	
		String selectionStartBehind = "(";
		String selectionEndBehind = "(T2";
		
		String expectedCompletionNodeToString = "<SelectOnType:T2>";
		String completionIdentifier = "T2";
		String expectedUnitDisplayString =
			"public class T2 {\n" +
		    "  public team class T1 {\n" +
			"    public T1() {\n" +
			"    }\n" +
			"    protected synchronized boolean _OT$when() {\n" + 
			"      return (<SelectOnType:T2>.this != null);\n" + 
			"    }\n" +			
			"  }\n" +
			"  public T2() {\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "T2";
		String testName = "<select team class name in team-level guard>";
	
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
	
	//TODO(gbr) select team class name in team package

	//method declarations
	//NOTE(gbr): method declarations are tested differently than method references
	//(see org.eclipse.objectteams.otdt.tests.selection.codeselect.CodeSelectionTests).
	
	//method references
	/**
	 * Select team method name in team-level method.
	 */
	public void test10()
	{
		String str = 
		    "public team class T1 {\n" +
		    "  public void m1(int x) {\n" +
			"  }\n" +		    
			"  public void m2(int x) {\n" +
			"    m1(x);\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "m2(int x) {\n    ";
		String selectionEndBehind = "    m1";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:m1(x)>";
		String completionIdentifier = "m1";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public T1() {\n" +
			"  }\n" +
			"  public void m1(int x) {\n" +
			"  }\n" +
			"  public void m2(int x) {\n" +
			"    <SelectOnMessageSend:m1(x)>;\n" +
			"  }\n" +
			"}\n"; 
		
		String expectedReplacedSource = "m1(x)";
		String testName = "<select team method name in team-level method>";
	
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
	 * Select team method name in <code>within</code> block.
	 */
	public void test11()
	{
		String str = 
		    "public team class T1 {\n" +
		    "  public void m1() {\n" +
			"    within (new T1()) {\n" +
			"      m1();\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
	
		String selectionStartBehind = "new T1()) {\n      ";
		String selectionEndBehind = "      m1";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:m1()>";
		String completionIdentifier = "m1";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public T1() {\n" +
			"  }\n" +
		    "  public void m1() {\n" +
			"    {\n" +
			"      <SelectOnMessageSend:m1()>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "m1()";
		String testName = "<select team method name in within-block>";
	
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
	 * Select team method name in team-level guard.
	 */
	public void test12()
	{
		String str = 
		    "public team class T1 when (this.isValid()) {\n" +
		    "  public boolean isValid() {\n" +
		    "    return true;" +
		    "  }\n" +
			"}\n";
	
		String selectionStartBehind = "when (this.";
		String selectionEndBehind = "this.isValid";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:this.isValid()>";
		String completionIdentifier = "isValid";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
			"  public T1() {\n" + 
			"  }\n" + 
		    "  public boolean isValid() {\n" +
		    "  }\n" +
			"  protected synchronized boolean _OT$when() {\n" + 
			"    return <SelectOnMessageSend:this.isValid()>;\n" + 
			"  }\n" + 
			"}\n";
		
		String expectedReplacedSource = "this.isValid()";
		String testName = "<select team method name in team-level guard>";
	
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

	//field declarations
	//NOTE(gbr): field declarations are tested differently than field references
	//(see org.eclipse.objectteams.otdt.tests.selection.codeselect.CodeSelectionTests).
	
	//field references
	/**
	 * Select team field name in team-level method.
	 */
	public void test13()
	{
		String str = 
		    "public team class T1 {\n" +
		    "  private int f;\n" +
			"  public void m2(int y) {\n" +
			"    f = y;\n" +
			"  }\n" +
			"}\n"; 
	
		String selectionStartBehind = "m2(int y) {\n    ";
		String selectionEndBehind = "    f";
		
		String expectedCompletionNodeToString = "<SelectOnName:f>";
		String completionIdentifier = "f";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
		    "  private int f;\n" +
			"  public T1() {\n" + 
			"  }\n" + 
			"  public void m2(int y) {\n" +
			"    <SelectOnName:f>;\n" +
			"  }\n" +
			"}\n"; 
		
		String expectedReplacedSource = "f";
		String testName = "<select team field name in team-level method>";
	
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
	 * Select team field name in <code>within</code> block.
	 */
	public void test14()
	{
		String str = 
		    "public team class T1 {\n" +
		    "  private int f;\n" +
		    "  void foo() {\n" +
		    "    within (new T1()) {\n" +
			"      f++;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
	
		String selectionStartBehind = "new T1()) {\n      ";
		String selectionEndBehind = "      f";
		
		String expectedCompletionNodeToString = "<SelectOnName:f>";
		String completionIdentifier = "f";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
		    "  private int f;\n" +
			"  public T1() {\n" + 
			"  }\n" + 
		    "  void foo() {\n" +
			"    {\n" +
			"      <SelectOnName:f>;\n" +
			"    }\n" +
			"  }\n" +
			"}\n";
		
		String expectedReplacedSource = "f";
		String testName = "<select team field name in within-block>";
	
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
	 * Select team field name in team-level guard.
	 */
	public void test15()
	{
		String str = 
		    "public team class T1 when (this.f != 0) {\n" +
		    "  private int f;\n" +
			"}\n";
	
		String selectionStartBehind = "(this.";
		String selectionEndBehind = "this.f";
		
		String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.f>";
		String completionIdentifier = "f";
		String expectedUnitDisplayString =
		    "public team class T1 {\n" +
		    "  private int f;\n" +
			"  public T1() {\n" + 
			"  }\n" + 
			"  protected synchronized boolean _OT$when() {\n" + 
			"    return (<SelectionOnFieldReference:this.f> != 0);\n" + 
			"  }\n" + 
			"}\n";
		
		String expectedReplacedSource = "this.f";
		String testName = "<select team field name in team-level guard>";
	
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

}