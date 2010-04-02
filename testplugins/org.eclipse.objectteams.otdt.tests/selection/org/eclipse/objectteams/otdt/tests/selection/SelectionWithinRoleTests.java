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
 * $Id: SelectionWithinRoleTests.java 23494 2010-02-05 23:06:44Z stephan $
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
 * Enter a class description here!
 * 
 * @author jwloka
 * @version $Id: SelectionWithinRoleTests.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class SelectionWithinRoleTests extends AbstractSelectionTest 
{
	public SelectionWithinRoleTests(String testName) 
	{
		super(testName);
	}
	/**
	 * Select explicit superclass of role class
	 */
	public void test01() {
	
		String str = 
			"import java.io.*;							\n" + 
			"											\n" + 
			"public team class T1 {						\n" +
			"	public class R1 extends IOException {	\n" + 
			"	}										\n" +
			"}											\n"; 
	
		String selectionStartBehind = "extends ";
		String selectionEndBehind = "IOException";
		
		String expectedCompletionNodeToString = "<SelectOnType:IOException>";
		String completionIdentifier = "IOException";
		String expectedUnitDisplayString =
			"import java.io.*;\n" + 
			"public team class T1 {\n" + 
			"  public role class R1 extends <SelectOnType:IOException> {\n" +
			"    public R1() {\n" + 
			"    }\n" +
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		String expectedReplacedSource = "IOException";
		String testName = "<select explicit superclass of role>";
	
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
	 * Select explicit superinterface of role class
	 */
	public void test02() {
	
		String str = 
			"import java.io.*;													\n" + 
			"																	\n" + 
			"public team class T1 {												\n" +
			"	public class R1 extends IOException implements Serializable {	\n" + 
			" 		int foo(){} 												\n" +
			"	}																\n" +
			"}																	\n"; 
	
		String selectionStartBehind = "implements ";
		String selectionEndBehind = "Serializable";
		
		String expectedCompletionNodeToString = "<SelectOnType:Serializable>";
		String completionIdentifier = "Serializable";
		String expectedUnitDisplayString =
			"import java.io.*;\n" + 
			"public team class T1 {\n" + 
			"  public role class R1 extends IOException implements <SelectOnType:Serializable> {\n" +
			"    public R1() {\n" + 
			"    }\n" +
			"    int foo() {\n" + 
			"    }\n" + 
			"  }\n" +
			"  public T1() {\n" +
			"  }\n" +
			"}\n";
		String expectedReplacedSource = "Serializable";
		String testName = "<select explicit superinterface of role>";
	
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
	 * Select qualified explicit superclass of role class
	 */
	public void test03() {
	
		String str = 
			"public team class T1 {								\n" +
			"	public class R1 extends java.io.IOException {	\n" +
			"	}												\n" + 
			"}													\n"; 
	
		String selectionStartBehind = "java.io.";
		String selectionEndBehind = "IOException";
		
		String expectedCompletionNodeToString = "<SelectOnType:java.io.IOException>";
		String completionIdentifier = "IOException";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 extends <SelectOnType:java.io.IOException> {\n" +
			"    public R1() {\n" +
			"    }\n" +
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "java.io.IOException";
		String testName = "<select qualified explicit superclass of role>";
	
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
	 * Select package from qualified explicit superclass in role class
	 */
	public void test04() {
	
		String str = 
			"public team class T1 {								\n" +
			"	public class R1 extends java.io.IOException {	\n" +
			"	}												\n" + 
			"}													\n"; 
	
		String selectionStartBehind = "java.";
		String selectionEndBehind = "java.io";
		
		String expectedCompletionNodeToString = "<SelectOnType:java.io>";
		String completionIdentifier = "io";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 extends <SelectOnType:java.io> {\n" +
			"    public R1() {\n" +
			"    }\n" +
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "java.io.IOException";
		String testName = "<select package from qualified explicit superclass of role>";
	
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
	 * Select message send in role method
	 */
	public void test05() {
	
		String str = 
			"public team class T1 {								\n" +
			"	public class R1 extends java.io.IOException {	\n" +
			"		int foo(){									\n" +
			"			System.out.println(\"hello\");			\n";
	
		String selectionStartBehind = "System.out.";
		String selectionEndBehind = "println";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:System.out.println(\"hello\")>";
		String completionIdentifier = "println";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 extends java.io.IOException {\n" +
			"    public R1() {\n" +
			"    }\n" +
			"    int foo() {\n" + 
			"      <SelectOnMessageSend:System.out.println(\"hello\")>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "System.out.println(\"hello\")";
		String testName = "<select message send in role method>";
	
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
	 * Select message send with recovery before in role method
	 */
	public void test06() {
	
		String str =
			"public team class T1 {								\n" +
			"	public class R1 extends 						\n" +
			"		int foo(){									\n" +
			"			System.out.println(\"hello\");			\n";
	
		String selectionStartBehind = "System.out.";
		String selectionEndBehind = "println";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:System.out.println(\"hello\")>";
		String completionIdentifier = "println";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" +
			"    }\n" +
			"    int foo() {\n" + 
			"      <SelectOnMessageSend:System.out.println(\"hello\")>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "System.out.println(\"hello\")";
		String testName = "<select message send with recovery before in role method>";
	
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
	 * Select message send in role method with sibling method 
	 */
	public void test07() {
	
		String str = 
			"public team class T1 {								\n" +
			"	public class R1 extends 						\n" +
			"		int foo(){									\n" +
			"			this.bar(\"hello\");					\n" +
			"		int bar(String s){							\n" +
			"			return s.length();						\n"	+
			"		}											\n" +
			"	}												\n" +
			"}													\n";
	
		String selectionStartBehind = "this.";
		String selectionEndBehind = "this.bar";
		
		String expectedCompletionNodeToString = "<SelectOnMessageSend:this.bar(\"hello\")>";
		String completionIdentifier = "bar";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" +
			"    }\n" +
			"    int foo() {\n" + 
			"      <SelectOnMessageSend:this.bar(\"hello\")>;\n" + 
			"    }\n" + 
			"    int bar(String s) {\n" + 
			"    }\n" +
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "this.bar(\"hello\")";
		String testName = "<select message send in role method with sibling method>";
	
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
	 * Select field reference in role method
	 */
	public void test08() {
	
		String str =
			"public team class T1 {								\n" +
			"	public class R1	{		 						\n" +
			"		int num = 0;								\n" +
			"		int foo(){									\n" +
			"		int j = this.num;							\n" +
			"	}												\n" +
			"}													\n";
	
		String selectionStartBehind = "this.";
		String selectionEndBehind = "this.num";
		
		String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.num>";
		String completionIdentifier = "num";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    int num;\n" + 
			"    public R1() {\n" +
			"    }\n" +
			"    int foo() {\n" + 
			"      int j = <SelectionOnFieldReference:this.num>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "this.num";
		String testName = "<select field reference in role method>";
	
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
	 * Select implicitly inherited field reference in role method
	 */
	public void test08b() {
	
		String str =
			"team class T1 {					 				\n" +
			"	public class R1	{		 						\n" +
			"		int num = 0;								\n" +
			"	}												\n" +
			"}													\n" +
			"public team class T2 extends T1 {					\n" +
			"	public class R1	{		 						\n" +
			"		int foo(){									\n" +
			"		int j = this.num;							\n" +
			"	}												\n" +
			"}													\n";
	
		String selectionStartBehind = "this.";
		String selectionEndBehind = "this.num";
		
		String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.num>";
		String completionIdentifier = "num";
		String expectedUnitDisplayString =
			"team class T1 {\n" +
			"  public role class R1 {\n" +
			"    int num;\n" + 
			"    public R1() {\n" +
			"    }\n" +
			"  }\n" + 
			"  T1() {\n" +
			"  }\n" + 
			"}\n" +
			"public team class T2 extends T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" +
			"    }\n" +
			"    int foo() {\n" + 
			"      int j = <SelectionOnFieldReference:this.num>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T2() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "this.num";
		String testName = "<select field reference in role method>";
	
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
	 * Select field reference with syntax errors in role method
	 */
	public void test09() {
	
		String str = 
			"public team class T1 {								\n" +
			"	public class R1	{		 						\n" +
			"		int num										\n" +
			"		int foo(){									\n" +
			"		int j = this.num;							\n" +
			"	}												\n" +
			"}													\n";
	
		String selectionStartBehind = "this.";
		String selectionEndBehind = "this.num";
		
		String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.num>";
		String completionIdentifier = "num";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    int num;\n" + 
			"    public R1() {\n" +
			"    }\n" +
			"    int foo() {\n" + 
			"      int j = <SelectionOnFieldReference:this.num>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "this.num";
		String testName = "<select field reference with syntax errors in role method>";
	
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
	 * Select field reference inside message receiver within role method
	 */
	public void test10() {
	
		String str = 
			"public team class T1 {								\n" +
			"	public class R1	{		 						\n" +
			"		R1 x;										\n" +
			"		int foo(){									\n" +
			"			int j = this.x.foo();					\n" +
			"		}											\n" +
			"	}												\n" +
			"}													\n";
			
		String selectionStartBehind = "this.";
		String selectionEndBehind = "this.x";
		
		String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.x>";
		String completionIdentifier = "x";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    R1 x;\n" +
			"    public R1() {\n" +
			"    }\n" +
			"    int foo() {\n" + 
			"      int j = <SelectionOnFieldReference:this.x>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "this.x";
		String testName = "<select field reference inside message receiver within role method>";
	
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
	 * Select allocation in role method
	 */
	public void test11() {
	
		String str = 
			"public team class T1 {								\n" +
			"	public class R1	{		 						\n" +
			"		R1(int i){}									\n" +
			"		int foo(){									\n" +
			"			int j = 0;								\n" +
			"			R1 x = new R1(j);						\n" +
			"		}											\n" +
			"	}												\n" +
			"}													\n";
			
		String selectionStartBehind = "new ";
		String selectionEndBehind = "new R1";
		
		String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new R1(j)>";
		String completionIdentifier = "R1";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    R1(int i) {\n" + 
			"    }\n" + 
			"    int foo() {\n" + 
			"      int j;\n" + 
			"      R1 x = <SelectOnAllocationExpression:new R1(j)>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		
		String expectedReplacedSource = "new R1(j)";
		String testName = "<select allocation in role method>";
	
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
	 * Select qualified name reference receiver in role method
	 */
	public void test13() {
	
		String str = 
			"public team class T1 {							\n" +
			"	public class R1	{		 					\n" +
			"		int foo(){								\n" +
			"			java.lang.System.out.println();		\n" +
			"		}										\n" +
			"	}											\n" +
			"}												\n";
			
		String selectionStartBehind = "java.lang.";
		String selectionEndBehind = "java.lang.System";
		
		String expectedCompletionNodeToString = "<SelectOnName:java.lang.System>";
		String completionIdentifier = "System";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" + 
			"    }\n" + 
			"    int foo() {\n" + 
			"      <SelectOnName:java.lang.System>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "java.lang.System.out";
		String testName = "<select qualified name receiver in role method>";
	
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
	 * Select qualified name reference in role method
	 */
	public void test14() {
	
		String str = 
			"public team class T1 {							\n" +
			"	public class R1	{		 					\n" +
			"		int foo(){								\n" +
			"			System sys = java.lang.System;		\n" +
			"		}										\n" +
			"	}											\n" +
			"}												\n";		
			
		String selectionStartBehind = "java.lang.";
		String selectionEndBehind = "java.lang.System";
		
		String expectedCompletionNodeToString = "<SelectOnName:java.lang.System>";
		String completionIdentifier = "System";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" + 
			"    }\n" + 
			"    int foo() {\n" + 
			"      System sys = <SelectOnName:java.lang.System>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "java.lang.System";
		String testName = "<select qualified name in role method>";
	
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
	 * Select variable type with modifier in role method
	 */
	public void test15() {
	
		String str = 
			"public team class T1 {							\n" +
			"	public class R1	{		 					\n" +
			"		int foo(){								\n" +
			"			final System sys = null;			\n" +
			"		}										\n" +
			"	}											\n" +
			"}												\n";		
			
		String selectionStartBehind = "final ";
		String selectionEndBehind = "final System";
		
		String expectedCompletionNodeToString = "<SelectOnType:System>";
		String completionIdentifier = "System";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" + 
			"    }\n" + 
			"    int foo() {\n" + 
			"      final <SelectOnType:System> sys;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "System";
		String testName = "<select variable type with modifier in role method>";
	
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
	 * Select variable type in role method
	 */
	public void test16() {
	
		String str = 
			"public team class T1 {							\n" +
			"	public class R1	{		 					\n" +
			"		int foo(){								\n" +
			"			System sys = null;					\n" +
			"		}										\n" +
			"	}											\n" +
			"}												\n";	
			
		String selectionStartBehind = "\n			";
		String selectionEndBehind = "\n			System";
		
		String expectedCompletionNodeToString = "<SelectOnType:System>";
		String completionIdentifier = "System";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" + 
			"    }\n" + 
			"    int foo() {\n" + 
			"      <SelectOnType:System> sys;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "System";
		String testName = "<select variable type in role method>";
	
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
	 * Select name in role method
	 */
	public void test17() {
	
		String str =
			"public team class T1 {							\n" +
			"	public class R1	{		 					\n" +
			"		int foo(){								\n" +
			"			System								\n" +
			"		}										\n" +
			"	}											\n" +
			"}												\n";	
			
		String selectionStartBehind = "\n			";
		String selectionEndBehind = "\n			System";
		
		String expectedCompletionNodeToString = "<SelectOnName:System>";
		String completionIdentifier = "System";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" + 
			"    }\n" + 
			"    int foo() {\n" + 
			"      <SelectOnName:System>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
			
		String expectedReplacedSource = "System";
		String testName = "<select name in team method>";
	
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
	 * Select anonymous type in role method
	 */
	public void test18() {
	
		String str =
			"public team class T1 {							\n" +
			"	public class R1	{		 					\n" +
			"		int foo(){								\n" +
			"			new Object(){						\n" +
			"				int bar(){}						\n" +
			"			}									\n" +
			"		}										\n" +
			"	}											\n" +
			"}												\n";	
			
		String selectionStartBehind = "new ";
		String selectionEndBehind = "new Object";
		
		String expectedCompletionNodeToString = 
			"<SelectOnAllocationExpression:new Object() {\n" +
			"}>";
		String completionIdentifier = "Object";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" + 
			"    }\n" + 
			"    int foo() {\n" + 
			"      <SelectOnAllocationExpression:new Object() {\n" +
			"      }>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
			
		String expectedReplacedSource = "new Object()";
		String testName = "<select anonymous type in role method>";
	
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
	 * Select cast type in role method
	 */
	public void test19() {
	
		String str = 
			"public team class T1 {							\n" +
			"	public class R1	{		 					\n" +
			"		Object foo(){							\n" +
			"			return (Object) this;				\n" +
			"			}									\n" +
			"		}										\n" +
			"	}											\n" +
			"}												\n";	
			
		String selectionStartBehind = "return (";
		String selectionEndBehind = "return (Object";
		
		String expectedCompletionNodeToString = "<SelectOnType:Object>";
		String completionIdentifier = "Object";
		String expectedUnitDisplayString =
			"public team class T1 {\n" +
			"  public role class R1 {\n" +
			"    public R1() {\n" + 
			"    }\n" + 
			"    Object foo() {\n" + 
			"      return (<SelectOnType:Object>) this;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public T1() {\n" +
			"  }\n" + 
			"}\n";
			
		String expectedReplacedSource = "Object";
		String testName = "<select cast type in role method>";
	
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
	 * Select package of role file
	 */
	public void test20() {
	
		String str =
			"team package x.y.T1;						\n" +
			"public class R1	{		 				\n" +
			"	int foo(){								\n" +
			"	}										\n" +
			"}											\n";	
			
		String selectionStartBehind = "x.";
		String selectionEndBehind = "x.y";
		
		String expectedCompletionNodeToString = "<SelectOnPackage:x.y>";
		String completionIdentifier = "y";
		String expectedUnitDisplayString =
			"team package <SelectOnPackage:x.y>;\n" + 
			"public role class R1 {\n" +
			"  public R1() {\n" + 
			"  }\n" + 
			"  int foo() {\n" + 
			"  }\n" + 
			"}\n";
			
		String expectedReplacedSource = "x.y.T1";
		String testName = "<select package of role file>";
	
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
	 * Select import in role file
	 */
	public void test21() {
	
		String str =
			"team package a.b.T1;						\n" +
			"import x.y.Other;							\n" +
			"public class R1	{		 				\n" +
			"	int foo(){								\n" +
			"	}										\n" +
			"}											\n";	
			
		String selectionStartBehind = "y.";
		String selectionEndBehind = "y.Other";
		
		String expectedCompletionNodeToString = "<SelectOnImport:x.y.Other>";
		String completionIdentifier = "Other";
		String expectedUnitDisplayString =
			"team package a.b.T1;\n" + 
			"import <SelectOnImport:x.y.Other>;\n" + 
			"public role class R1 {\n" +
			"  public R1() {\n" + 
			"  }\n" + 
			"  int foo() {\n" + 
			"  }\n" + 
			"}\n";
			
		String expectedReplacedSource = "x.y.Other";
		String testName = "<select import in role file>";
	
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
	 * Select import on demand in role file
	 */
	public void test22() {
	
		String str =
			"team package a.b.T1;						\n" +
			"import x.y.other.*;						\n" +
			"public class R1	{		 				\n" +
			"	int foo(){								\n" +
			"	}										\n" +
			"}											\n";	
			
		String selectionStartBehind = "y.";
		String selectionEndBehind = "y.other";
		
		String expectedCompletionNodeToString = "<SelectOnImport:x.y.other>";
		String completionIdentifier = "other";
		String expectedUnitDisplayString =
			"team package a.b.T1;\n" + 
			"import <SelectOnImport:x.y.other>;\n" + 
			"public role class R1 {\n" +
			"  public R1() {\n" + 
			"  }\n" + 
			"  int foo() {\n" + 
			"  }\n" + 
			"}\n";
			
		String expectedReplacedSource = "x.y.other";
		String testName = "<select import on demand in role file>";
	
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
	 * Select array initializer type in role method
	 */
	public void test23() {
	
		String str =
			"public team class T1 {				 				\n" +
			"	public class R1	{		 						\n" +
			"		int foo(){									\n" +
			"			String[] p = new String[]{\"Left\"};	\n" +
			"		}											\n" +
			"	}												\n" +
			"}													\n";	
			
		String selectionStartBehind = "new ";
		String selectionEndBehind = "new String";
		String expectedCompletionNodeToString = "<SelectOnType:String>";
		String completionIdentifier = "String";
		String expectedUnitDisplayString =
			"public team class T1 {\n" + 
			"  public role class R1 {\n" +
			"    public R1() {\n" + 
			"    }\n" + 
			"    int foo() {\n" + 
			"      String[] p = <SelectOnType:String>;\n" + 
			"    }\n" + 
			"  }\n" +
			"  public T1() {\n" + 
			"  }\n" + 
			"}\n";
			
		String expectedReplacedSource = "String";
		String testName = "<select array initializer type in role method>";
	
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
	 * Select super in role method
	 */
	public void test25() {
	
		String str =
			"public team class T1 {	 				\n" +
			"	public class R1	{					\n" +
			"		Object foo() {					\n" +
			"			return super.foo();			\n" +
			"		}								\n" +
			"	}									\n" +
			"}										\n";	
			
		String selectionStartBehind = "return ";
		String selectionEndBehind = "return super";
		
		String expectedCompletionNodeToString = "<SelectOnSuper:super>";
		
		String completionIdentifier = "super";
		String expectedUnitDisplayString =
			"public team class T1 {\n" + 
			"  public role class R1 {\n" +
			"    public R1() {\n" + 
			"    }\n" + 
			"    Object foo() {\n" + 
			"      return <SelectOnSuper:super>;\n" + 
			"    }\n" + 
			"  }\n" +
			"  public T1() {\n" + 
			"  }\n" + 
			"}\n";
			
		String expectedReplacedSource = "super";
		String testName = "<select super in role method>";
	
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
	 * Select qualified super in anonymous instance in role method
	 */
	public void test26() {
	
		String str =
			"public team class T1 {	 					\n" +
			"	public class R1	{						\n" +
			"		Object foo() {						\n" +
			"			new X(){						\n" +
			"				Object bar(){				\n" +
			"					return R1.super.foo();	\n" +
			"				}							\n" +
			"			}								\n" +
			"		}									\n" +
			"	}										\n" +
			"}											\n";	
			
		String selectionStartBehind = "R1.";
		String selectionEndBehind = "R1.super";
		
		String expectedCompletionNodeToString = "<SelectOnQualifiedSuper:R1.super>";
		
		String completionIdentifier = "super";
		String expectedUnitDisplayString =
			"public team class T1 {\n" + 
			"  public role class R1 {\n" +
			"    public R1() {\n" + 
			"    }\n" + 
			"    Object foo() {\n" + 
			"      new X() {\n" + 
			"        Object bar() {\n" + 
			"          return <SelectOnQualifiedSuper:R1.super>;\n" + 
			"        }\n" + 
			"      };\n" + 
			"    }\n" + 
			"  }\n" +
			"  public T1() {\n" + 
			"  }\n" + 
			"}\n";
		String expectedReplacedSource = "R1.super";
		String testName = "<select qualified super in anonymous instance in role method>";
	
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
	 * Select super constructor call within role constructor
	 */
	public void test27() {
	
		String str =
			"public team class T1 {	 				\n" +
			"	public class R1	{					\n" +
			"		R1() {							\n" +
			"			super();					\n" +
			"		}								\n" +
			"	}									\n" +
			"}										\n";	
			
		String selectionStartBehind = "\n\t\t\t";
		String selectionEndBehind = "super";
		
		String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:super()>;";
		
		String completionIdentifier = "super";
		String expectedUnitDisplayString =
			"public team class T1 {\n" + 
			"  public role class R1 {\n" +
			"    R1() {\n" + 
			"      <SelectOnExplicitConstructorCall:super()>;\n" + 
			"    }\n" + 
			"  }\n" +
			"  public T1() {\n" + 
			"  }\n" + 
			"}\n";
			
		String expectedReplacedSource = "super()";
		String testName = "<select super constructor call within role constructor>";
	
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
	 * Select qualified super constructor call within local class in role class
	 */
	public void test28() {
	
		String str =
			"public team class G {						\n" +
			"	class M {								\n" +
			"		static Object foo() {				\n" +
			"			class X extends M {				\n" +
			"				X (){						\n" +
			"					new G().super();		\n" +
			"				}							\n" +
			"			}								\n" +
			"		}									\n" +
			"	}										\n" +
			"}											\n";
			
		String selectionStartBehind = "new G().";
		String selectionEndBehind = "new G().super";
		
		String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:new G().super()>;";
		
		String completionIdentifier = "super";
		String expectedUnitDisplayString =
			"public team class G {\n" + 
			"  role class M {\n" + 
			"    M() {\n" + 
			"    }\n" + 
			"    static Object foo() {\n" + 
			"      class X extends M {\n" + 
			"        X() {\n" + 
			"          <SelectOnExplicitConstructorCall:new G().super()>;\n" + 
			"        }\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public G() {\n" + 
			"  }\n" + 
			"}\n";	
		String expectedReplacedSource = "new G().super()";
		String testName = "<select qualified super constructor call>";
	
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
	 * Select qualified super constructor call with arguments in local class within role class
	 */
	public void test29() {
	
		String str =
			"public team class G {							\n" +
			"	class M {}									\n" +
			"	static Object foo() {						\n" +
			"		class X extends M {						\n" +
			"			X (){								\n" +
			"				new G().super(23 + \"hello\");	\n" +
			"			}									\n" +
			"		}										\n" +
			"	}											\n" +
			"}												\n";
			
		String selectionStartBehind = "new G().";
		String selectionEndBehind = "new G().super";
		
		String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:new G().super((23 + \"hello\"))>;";
		
		String completionIdentifier = "super";
		String expectedUnitDisplayString =
			"public team class G {\n" + 
			"  role class M {\n" + 
			"    M() {\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public G() {\n" + 
			"  }\n" + 
			"  static Object foo() {\n" + 
			"    class X extends M {\n" + 
			"      X() {\n" + 
			"        <SelectOnExplicitConstructorCall:new G().super((23 + \"hello\"))>;\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n";	
		String expectedReplacedSource = "new G().super(23 + \"hello\")";
		String testName = "<select qualified super constructor call with arguments in local class within role class>";
	
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
	 * Select super constructor call with arguments in role constructor
	 */
	public void test30() {
	
		String str =
			"public team class G {					\n" +
			"	class M {							\n" +
			"		M() {							\n" +
			"			super(new M());				\n" +
			"		}								\n" +
			"	}									\n" +
			"}										\n";
			
		String selectionStartBehind = "M() {\t\t\t\t\t\t\t\n\t\t\t";
		String selectionEndBehind = "super";
		
		String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:super(new M())>;";
		
		String completionIdentifier = "super";
		String expectedUnitDisplayString =
			"public team class G {\n" + 
			"  role class M {\n" + 
			"    M() {\n" + 
			"      <SelectOnExplicitConstructorCall:super(new M())>;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public G() {\n" + 
			"  }\n" + 
			"}\n";	
			
		String expectedReplacedSource = "super(new M())";
		String testName = "<select super constructor call with arguments in role constructor>";
	
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
	 * Regression test for 1FVQ0LK
	 */
	public void test31() {
	
		String str =
			"public team class G {					\n" +
			"	class M {							\n" +
			"		Y f;							\n" +
			"		void foo() {					\n" +
			"			new Bar(fred());			\n" +
			"			Z z= new Z();				\n" +
			"		}								\n" +
			"	}									\n" +
			"}										\n";
			
		String selectionStartBehind = "\n\t\t";
		String selectionEndBehind = "Y";
		
		String expectedCompletionNodeToString = "<SelectOnType:Y>";
		
		String completionIdentifier = "Y";
		String expectedUnitDisplayString =
			"public team class G {\n" + 
			"  role class M {\n" + 
			"    <SelectOnType:Y> f;\n" +
			"    M() {\n" +
			"    }\n" +
			"    void foo() {\n" +
			"    }\n" +
			"  }\n" + 
			"  public G() {\n" + 
			"  }\n" + 
			"}\n";	
			
		String expectedReplacedSource = "Y";
		String testName = "<regression test for 1FVQ0LK>";
	
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
	 * Select qualified this constructor call of role class
	 */
	public void test32() {
	
		String str =
			"public team class G {						\n" +
			"	class M {								\n" +
			"		static Object foo() {				\n" +
			"			class X {						\n" +
			"				X (){						\n" +
			"				}							\n" +
			"				X (int x){					\n" +
			"					new G().this();			\n" +
			"				}							\n" +
			"			}								\n" +
			"		}									\n" +
			"	}										\n" +
			"}											\n";
			
		String selectionStartBehind = "new G().";
		String selectionEndBehind = "new G().this";
		
		String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:new G().this()>;";
		
		String completionIdentifier = "this";
		String expectedUnitDisplayString =
			"public team class G {\n" + 
			"  role class M {\n" + 
			"    M() {\n" +
			"    }\n" +
			"    static Object foo() {\n" + 
			"      class X {\n" + 
			"        X() {\n" +
			"          super();\n"+
			"        }\n" + 
			"        X(int x) {\n" + 
			"          <SelectOnExplicitConstructorCall:new G().this()>;\n" + 
			"        }\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  public G() {\n" + 
			"  }\n" + 
			"}\n";	
		String expectedReplacedSource = "new G().this()";
		String testName = "<select qualified this constructor call of role class>";
	
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
	 * bugs 14468 select inside instanceof statement in role method
	 */
	public void test33() {
	
		String str =
			"public team class T1 {							\n" +
			"	class R1 {									\n" +
			"		void foo() {							\n" +
			"    		y = x instanceof Object;			\n" +
			"  		}										\n" +
			"	}											\n" +
			"}												\n";
			
		String selection = "Object";
		
		String expectedCompletionNodeToString = "<SelectOnType:Object>";
		
		String completionIdentifier = "Object";
		String expectedUnitDisplayString =
			"public team class T1 {\n"+
			"  role class R1 {\n"+
			"    R1() {\n" +
			"    }\n"+
			"    void foo() {\n"+
			"      <SelectOnType:Object>;\n"+
			"    }\n"+
			"  }\n"+
			"  public T1() {\n"+
			"  }\n"+
			"}\n";

		String expectedReplacedSource = "Object";
		String testName = "<select inside instanceof statement in role method>";
	
		int selectionStart = str.indexOf(selection);
		int selectionEnd = str.indexOf(selection) + selection.length() - 1;
			
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
	 * bugs 14468 select inside instanceof statement in team method
	 */
	public void test34() {
	
		String str =
			"public team class T1 {							\n" +
			"	class R1 {									\n" +
			"		void foo() {							\n" +
			"			boolean y = x instanceof Object;	\n" +
			"  		}										\n" +
			"	}											\n" +
			"}												\n";
			
		String selection = "Object";
		
		String expectedCompletionNodeToString = "<SelectOnType:Object>";
		
		String completionIdentifier = "Object";
		String expectedUnitDisplayString =
			"public team class T1 {\n"+
			"  role class R1 {\n"+
			"    R1() {\n" +
			"    }\n"+
			"    void foo() {\n"+
			"      boolean y = <SelectOnType:Object>;\n"+
			"    }\n"+
			"  }\n"+
			"  public T1() {\n"+
			"  }\n"+
			"}\n";

		String expectedReplacedSource = "Object";
		String testName = "<select inside instanceof statement in role method>";
	
		int selectionStart = str.indexOf(selection);
		int selectionEnd = str.indexOf(selection) + selection.length() - 1;
			
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
	 * bugs 14468 select inside instanceof statement in role class
	 */
	public void test35() {
	
		String str =
			"public team class T1 {						\n" +
			"	class R1 {								\n" +
			"		boolean y = x instanceof Object;	\n" +
			"	}										\n" +
			"}											\n";
			
		String selection = "Object";
		
		String expectedCompletionNodeToString = "<SelectOnType:Object>";
		
		String completionIdentifier = "Object";
		String expectedUnitDisplayString =
			"public team class T1 {\n"+
			"  role class R1 {\n"+
			"    boolean y = <SelectOnType:Object>;\n"+
			"    R1() {\n" +
			"    }\n"+
			"  }\n"+
			"  public T1() {\n"+
			"  }\n"+
			"}\n";
		String expectedReplacedSource = "Object";
		String testName = "<select inside instanceof statement in role class>";
	
		int selectionStart = str.indexOf(selection);
		int selectionEnd = str.indexOf(selection) + selection.length() - 1;
			
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
	 * bugs 28064 select anonymous type in field decl of team class
	 */
	public void test36() {
	
		String str =
			"public team class T1 {			\n" +
			"	class R1 {					\n" +
			"  		R1 x = new R1(){}		\n" +
			"	}							\n" +
			"}								\n";
			
		String selection = "R1";
		
		String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new R1() {\n" +
												"}>";
		
		String completionIdentifier = "R1";
		String expectedUnitDisplayString =
			"public team class T1 {\n"+
			"  role class R1 {\n"+
			"    R1 x = <SelectOnAllocationExpression:new R1() {\n" +
			"    }>;\n"+
			"    R1() {\n"+
			"    }\n"+
			"  }\n"+
			"  public T1() {\n"+
			"  }\n"+
			"}\n";
		String expectedReplacedSource = "new R1()";
		String testName = "<select anonymous type in field decl of role class>";
	
		int selectionStart = str.lastIndexOf(selection);
		int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;
			
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