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
 * $Id: OTJavaMethodSearchTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.search;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

/**
 * @author svacina
 * 
 * @version $Id: OTJavaMethodSearchTests.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OTJavaMethodSearchTests extends OTJavaSearchTestBase
{
	public OTJavaMethodSearchTests(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
	    if (false)
	    {
	        System.err.println("Warning, only part of the OTJavaMethodSearchTest are being executed!");
			Suite suite = new Suite(OTJavaMethodSearchTests.class.getName());
			suite.addTest(new OTJavaMethodSearchTests("test035"));
			return suite;
	    }
	    
		return new Suite(OTJavaMethodSearchTests.class);
	}

	/**
	 * Search for:<br>
	 *	- method references<br> 
	 * Search pattern:<br>
	 *	- fully qualified name<br>
	 * Searched element:<br>
	 *	- Abstract method declaration<br>
	 * Expected search result:<br>
	 *	Reference in <br>
	 *		- role method (testMethod)<br>
	 *		- role method spec in a callout without signature (abstract method decl in the same role)<br>
	 */
	public void test001() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p.Team1.Role1.roleMethod()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p"), 
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role1.testMethod() [roleMethod()]\n" + 
							"src/p/Team1.java p.Team1$Role1 roleMethod -> baseMethod [roleMethod]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 * 	- method references<br>
	 * Search pattern:<br>
	 * 	- simple name<br>
	 * Searched element:<br>
	 *	- Abstract role method declaration<br>
	 * Expected search result:<br>
	 *	Reference in <br>
	 *		- role method (testMethod)<br>
	 *		- role method spec in a callout without signature (abstract method decl in the same role)<br>
	 */
	public void test002() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"roleMethod()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p"), 
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role1.testMethod() [roleMethod()]\n" +
							"src/p/Team1.java p.Team1$Role1 roleMethod -> baseMethod [roleMethod]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declarations<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- Abstract role method declaration<br>
	 * Expected search result:<br>
	 *	Declarations in<br>
	 *		- role<br>
	 */
	public void test003() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p.Team1.Role1.roleMethod()", 
				METHOD, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p"), 
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role1.roleMethod() [roleMethod]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declarations<br>
	 * Search pattern:<br>
	 * 	- simple name<br>
	 * Searched element:<br>
	 * 	- Abstract role method declaration<br>
	 * Expected search result:<br>
	 *	Declarations in<br>
	 *		- role<br>
	 */
	public void test004() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"roleMethod()", 
				METHOD, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p"),
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role1.roleMethod() [roleMethod]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method references<br>
	 *  - method declarations<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * 	- combined method pattern<br>
	 * Searched element:<br>
	 * 	- Abstract role method declaration<br>
	 * Expected search result:<br>
	 *	Declarations in<br>
	 *		- role<br>
	 *  References in<br> 
	 *		- role method (testMethod)<br>
	 *		- role method spec in a callout without signature (abstract method decl in the same role)<br>
	 *see TPX-293(fixed)<br>
	 */
	public void test005() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p.Team1.Role1.roleMethod()", 
				METHOD, 
				ALL_OCCURRENCES,
				getJavaSearchScopeFromPackage("p"),
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role1.roleMethod() [roleMethod]\n" + 
							"src/p/Team1.java void p.Team1$Role1.testMethod() [roleMethod()]\n" + 
							"src/p/Team1.java p.Team1$Role1 roleMethod -> baseMethod [roleMethod]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method references<br>
	 *  - method declarations<br>
	 * Search pattern:<br>
	 * 	- simple name<br>
	 * 	- combined method pattern<br>
	 * Searched element:<br>
	 * 	- Abstract role method declaration<br>
	 * Expected search result:<br>
	 *	Declarations in<br>
	 *		- role<br>
	 *  References in <br>
	 *		- role method (testMethod)<br>
	 *		- role method spec in a callout without signature (abstract method decl in the same role)<br>
	 */
	public void test006() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"roleMethod()", 
				METHOD, 
				ALL_OCCURRENCES,
				getJavaSearchScopeFromPackage("p"), 
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role1.roleMethod() [roleMethod]\n" + 
							"src/p/Team1.java void p.Team1$Role1.testMethod() [roleMethod()]\n" +
							"src/p/Team1.java p.Team1$Role1 roleMethod -> baseMethod [roleMethod]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *  - method declarations<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- role method declaration<br>
	 * Expected search result:<br>
	 *	Declarations in<br>
	 *		- role<br>
	 */
	public void test007() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p.Team1.Role2.role2Method()", 
				METHOD, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p"), 
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role2.role2Method() [role2Method]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *  - method declarations<br>
	 * Search pattern:<br>
	 * 	- simple name<br>
	 * Searched element:<br>
	 * 	- role method declaration<br>
	 * Expected search result:<br>
	 *	Declarations in<br>
	 *		- role<br>
	 */
	public void test008() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"role2Method()", 
				METHOD, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p"), 
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role2.role2Method() [role2Method]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *  - method declarations<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 *	- combined pattern<br>
	 * Searched element:<br>
	 * 	- role method declaration<br>
	 * Expected search result:<br>
	 *	Declarations in<br>
	 *		- role<br>
	 *  References in<br>
	 *		- role method<br>
	 *		- role method spec in callin without signature (method declaration in same class)<br>
	 * see TPX-344 (fixed)<br>
	 */
	public void test009() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p.Team1.Role2.role2Method()", 
				METHOD, 
				ALL_OCCURRENCES,
				getJavaSearchScopeFromPackage("p"), 
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role2.role2Method() [role2Method]\n" + 
							"src/p/Team1.java void p.Team1$Role2.testMethod() [role2Method()]\n" + 
							"src/p/Team1.java p.Team1$Role2 role2Method <- baseMethod [role2Method]",
				resultCollector);
	}
	
	/** 
	 * Search for:<br>
	 *  - method declarations<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- simple name<br>
	 *	- combined pattern<br>
	 * Searched element:<br>
	 * 	- role method declaration<br>
	 * Expected search result:<br>
	 *	Declarations in<br>
	 *		- role<br>
	 *  References in<br>
	 *		- role method<br>
	 *		- role method spec in callin without signature (method declaration in same class)<br>
	 */
	public void test010() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"role2Method()", 
				METHOD, 
				ALL_OCCURRENCES,
				getJavaSearchScopeFromPackage("p"), 
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role2.role2Method() [role2Method]\n" + 
							"src/p/Team1.java void p.Team1$Role2.testMethod() [role2Method()]\n" +
							"src/p/Team1.java p.Team1$Role2 role2Method <- baseMethod [role2Method]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- simple name<br>
	 * Searched element:<br>
	 * 	- role method declaration<br>
	 * Expected search result:<br>
	 *  References in<br>
	 *		- role method<br>
	 *		- role method spec in callin without signature (method declaration in same class)<br>
	*/
	public void test011() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"role2Method()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p"), 
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role2.testMethod() [role2Method()]\n" +
							"src/p/Team1.java p.Team1$Role2 role2Method <- baseMethod [role2Method]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- role method declaration<br>
	 * Expected search result:<br>
	 *  References in<br>
	 *		- role method<br>
	 *		- role method spec in callin without signature (method declaration in same class)<br>
	 */
	public void test012() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p.Team1.Role2.role2Method()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p"), 
				resultCollector);
		
		assertSearchResults("src/p/Team1.java void p.Team1$Role2.testMethod() [role2Method()]\n" + 
							"src/p/Team1.java p.Team1$Role2 role2Method <- baseMethod [role2Method]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- abstract method declaration<br>
	 * Expected search result:<br>
	 *  References in<br>
	 *		- role method (testMethod)<br>
	 *		- role method spec in callout with signature (abstract method declaration in same class)<br>
	 */
	public void test013() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_long_methodspecs.Team1.Role1.roleMethodWithAbstractDecl", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_long_methodspecs"), 
				resultCollector);
		
		assertSearchResults("src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role1.testMethod() [roleMethodWithAbstractDecl()]\n" + 
							"src/p_long_methodspecs/Team1.java p_long_methodspecs.Team1$Role1 roleMethodWithAbstractDecl() -> baseMethod() [roleMethodWithAbstractDecl()]",
				resultCollector);
	}
	
	/**
	 * Search for:
	 *	- method references
	 * Search pattern:
	 * 	- fully qualified name
	 * Searched element:
	 * 	- "method declaration" in callout with signature
	 * Expected search result:
	 *   - references in
	 *		- role method (testMethod)
	 *		- role method spec in callout with signature (no abstract method declaration)
	 */
	public void test014() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_long_methodspecs.Team1.Role1.roleMethodWithoutAbstractDecl", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_long_methodspecs"), 
				resultCollector);
		//TODO(jsv) the role method spec in a callout is reported as reference. after improvement resolved change expected test result
		assertSearchResults("src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role1.testMethod() [roleMethodWithoutAbstractDecl()]\n" + 
							"src/p_long_methodspecs/Team1.java p_long_methodspecs.Team1$Role1 roleMethodWithoutAbstractDecl() -> baseMethod() [roleMethodWithoutAbstractDecl()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- simple name<br>
	 * Searched element:<br>
	 * 	- abstract method declaration<br>
	 * Expected search result:<br>
	 *  References in<br>
	 *		- role method (testMethod)<br>
	 *		- role method spec in callout with signature (abstract method declaration in same class)<br>
	 */
	public void test015() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"roleMethodWithAbstractDecl()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_long_methodspecs"), 
				resultCollector);
		
		assertSearchResults("src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role1.testMethod() [roleMethodWithAbstractDecl()]\n" +
							"src/p_long_methodspecs/Team1.java p_long_methodspecs.Team1$Role1 roleMethodWithAbstractDecl() -> baseMethod() [roleMethodWithAbstractDecl()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- simple name<br>
	 * Searched element:<br>
	 * 	- "method declaration" in callout with signature<br>
	 * Expected search result:<br>
	 *  References in<br>
	 *		- role method (testMethod)<br>
	 *		- role method spec in callout with signature (no abstract method declaration)<br>
	 */
	public void test016() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"roleMethodWithoutAbstractDecl()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_long_methodspecs"), 
				resultCollector);
		//TODO(jsv) the role method spec in a callout is reported as reference. after improvement resolved change expected test result
		assertSearchResults("src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role1.testMethod() [roleMethodWithoutAbstractDecl()]\n" +
							"src/p_long_methodspecs/Team1.java p_long_methodspecs.Team1$Role1 roleMethodWithoutAbstractDecl() -> baseMethod() [roleMethodWithoutAbstractDecl()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declaration<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- "method declaration" in callout binding<br> 
	 * Expected search result:<br>
	 *  Declaration in<br>
	 *		- none<br>
	 */
	public void test017() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_long_methodspecs.Team1.Role1.roleMethodWithoutAbstractDecl()", 
				METHOD, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_long_methodspecs"), 
				resultCollector);
		//TODO(jsv) the role method spec in a callout is reported as reference. after improvement resolved change expected test result		
		assertSearchResults("",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declaration<br>
	 * Search pattern:<br>
	 * 	- simple name<br>
	 * Searched element:<br>
	 * 	- "method declaration" in callout binding<br> 
	 * Expected search result:<br>
	 *  Declaration in<br>
	 *		- none<br>
	 */
	public void test018() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"roleMethodWithoutAbstractDecl()", 
				METHOD, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_long_methodspecs"),
				resultCollector);
		//TODO(jsv) the role method spec in a callout is reported as reference. after improvement resolved change expected test result
		assertSearchResults("",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declaration<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 *	- combined pattern<br>
	 * Searched element:<br>
	 * 	- abstarct method declaration<br>
	 * Expected search result:<br>
	 *  Declaration in<br>
	 *		- role<br>
	 *	References in<br>
	 *		- role method(test method)<br>
	 *		- method spec in callout mapping with signature (method declaration in the same role)<br>
	 */
	public void test019() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_long_methodspecs.Team1.Role1.roleMethodWithAbstractDecl()", 
				METHOD, 
				ALL_OCCURRENCES,
				getJavaSearchScopeFromPackage("p_long_methodspecs"),
				resultCollector);
		
		assertSearchResults("src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role1.roleMethodWithAbstractDecl() [roleMethodWithAbstractDecl]\n" + 
							"src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role1.testMethod() [roleMethodWithAbstractDecl()]\n" + 
							"src/p_long_methodspecs/Team1.java p_long_methodspecs.Team1$Role1 roleMethodWithAbstractDecl() -> baseMethod() [roleMethodWithAbstractDecl()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declaration<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 *	- combined pattern<br>
	 * Searched element:<br>
	 * 	- "method declaration" in callout binding<br> 
	 * Expected search result:<br>
	 * 	Declaration in<br>
	 *		- none<br>
	 *	References in<br>
	 *		- role method(test method)<br>
	 *		- method spec in callout mapping with signature (method declaration in the same role)<br>
	 */
	public void test020() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_long_methodspecs.Team1.Role1.roleMethodWithoutAbstractDecl()", 
				METHOD, 
				ALL_OCCURRENCES,
				getJavaSearchScopeFromPackage("p_long_methodspecs"),
				resultCollector);
		//TODO(jsv) the role method spec in a callout is reported as reference. after improvement resolved change expected test result
		assertSearchResults("src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role1.testMethod() [roleMethodWithoutAbstractDecl()]\n" + 
							"src/p_long_methodspecs/Team1.java p_long_methodspecs.Team1$Role1 roleMethodWithoutAbstractDecl() -> baseMethod() [roleMethodWithoutAbstractDecl()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declaration<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- role method<br> 
	 * Expected search result:<br>
	 *  Declaration in<br>
	 *		- role<br>
	 *	References in<br>
	 *		- role method<br>
	 *		- role method spec in callin binding with signature<br>
	 */
	
	public void test021() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_long_methodspecs.Team1.Role2.role2Method()", 
				METHOD, 
				ALL_OCCURRENCES,
				getJavaSearchScopeFromPackage("p_long_methodspecs"), 
				resultCollector);
		
		assertSearchResults("src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role2.role2Method() [role2Method]\n" + 
							"src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role2.testMethod() [role2Method()]\n" + 
							"src/p_long_methodspecs/Team1.java p_long_methodspecs.Team1$Role2 role2Method() <- baseMethod() [role2Method()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declaration<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- simple name<br>
	 * Searched element:<br>
	 * 	- role method<br> 
	 * Expected search result:<br>
	 *  Declaration in<br>
	 *		- role<br>
	 *	References in<br>
	 *		- role method<br>
	 *		- role method spec in callin binding with signature<br>
	 */
	public void test022() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"role2Method()", 
				METHOD, 
				ALL_OCCURRENCES,
				getJavaSearchScopeFromPackage("p_long_methodspecs"), 
				resultCollector);
		
		assertSearchResults("src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role2.role2Method() [role2Method]\n" + 
							"src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role2.testMethod() [role2Method()]\n" +
							"src/p_long_methodspecs/Team1.java p_long_methodspecs.Team1$Role2 role2Method() <- baseMethod() [role2Method()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- simple name<br>
	 * Searched element:<br>
	 * 	- role method<br> 
	 * Expected search result:<br>
	 *	References in<br>
	 *		- role method<br>
	 *		- role method spec in callin binding with signature<br>
	 */
	public void test023() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"role2Method()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_long_methodspecs"), 
				resultCollector);
		
		assertSearchResults("src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role2.testMethod() [role2Method()]\n" +
							"src/p_long_methodspecs/Team1.java p_long_methodspecs.Team1$Role2 role2Method() <- baseMethod() [role2Method()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- role method<br> 
	 * Expected search result:<br>
	 *	References in<br>
	 *		- role method<br>
	 *		- role method spec in callin binding with signature<br>
	 */
	public void test024() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_long_methodspecs.Team1.Role2.role2Method()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_long_methodspecs"), 
				resultCollector);
		
		assertSearchResults("src/p_long_methodspecs/Team1.java void p_long_methodspecs.Team1$Role2.testMethod() [role2Method()]\n" + 
							"src/p_long_methodspecs/Team1.java p_long_methodspecs.Team1$Role2 role2Method() <- baseMethod() [role2Method()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declarations (in hierarchy) -> decl & fully qualified name<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- base method<br> 
	 * Expected search result:<br>
	 *	Declarations in<br>
	 *		- base class AA <br>
	 *		- base class A (extends AA) (explicit inheritance)<br>
	 *see TPX-332<br>
	 */
	public void test025() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p2.AA.mm1()", 
				METHOD, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p2"), 
				resultCollector);
		
		assertSearchResults("src/p2/A.java void p2.A.mm1() [mm1]\n" + 
							"src/p2/AA.java void p2.AA.mm1() [mm1]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- base method<br> 
	 * Expected search result:<br>
	 *	References in 
	 *		<li> base method spec in callout mapping with signature. corresponding abstract declaration of role method is inherited from super role<br>
	 */
	public void test026() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p.Base1.baseMethod()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p"), 
				resultCollector);
		
		assertSearchResults("src/p/Team1.java p.Team1$Role1 roleMethod -> baseMethod [baseMethod]\n" +
							"src/p/Team1.java p.Team1$Role1 roleMethod3 -> baseMethod [baseMethod]\n" +
							"src/p/Team1.java p.Team1$Role2 role2Method <- baseMethod [baseMethod]"
							,
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declarations (in hierarchy) -> decl & fully qualified name<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- role method<br> 
	 * Expected search result:<br>
	 *	Declarations in<br>
	 *		- role class TestTeam1.TestRole1 <br>
	 *		- role class TestTeam2.TestRole1 (TestTeam2 extends TestTeam1) (implicit inheritance)<br>
	 */
	public void test027() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_implicit_inheritance.TestTeam1.TestRole1.roleMethod()", 
				METHOD, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_implicit_inheritance"), 
				resultCollector);
		
		assertSearchResults("src/p_implicit_inheritance/TestTeam1.java void p_implicit_inheritance.TestTeam1$TestRole1.roleMethod() [roleMethod]\n" + 
							"src/p_implicit_inheritance/TestTeam2.java void p_implicit_inheritance.TestTeam2$TestRole1.roleMethod() [roleMethod]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method reference<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- private role method<br> 
	 * Expected search result:<br>
	 *	References in<br>
	 *		- ref in TestTeam1.TestRole1.roleMethod() <br>
	 *		- ref in TestTeam2.TestRole1.foo()<br>
	 */
	public void test028() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_implicit_inheritance.TestTeam1.TestRole1.privateRoleMethod()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_implicit_inheritance"), 
				resultCollector);
		
		assertSearchResults("Search for references of private role method, fully qualified name",
							"src/p_implicit_inheritance/TestTeam1.java void p_implicit_inheritance.TestTeam1$TestRole1.roleMethod() [privateRoleMethod()]\n" +
							"src/p_implicit_inheritance/TestTeam2.java void p_implicit_inheritance.TestTeam2$TestRole1.foo() [privateRoleMethod()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declaration<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- declaration of a role method which overrides an implicit inherited role method<br> 
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- Declaration in TestTeam2.TestRole1<br>
	 */
	public void test029() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_implicit_inheritance.TestTeam2.TestRole1.roleMethod()", 
				METHOD, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_implicit_inheritance"), 
				resultCollector);
		
		assertSearchResults("Search for role method decl which overrides an implicit inherited method. fq name",
				"src/p_implicit_inheritance/TestTeam2.java void p_implicit_inheritance.TestTeam2$TestRole1.roleMethod() [roleMethod]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declaration<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- declaration of a role method which does not override an implicit inherited role method<br> 
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- Declaration in TestTeam2.TestRole1<br>
	 */
	public void test030() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_implicit_inheritance.TestTeam2.TestRole1.roleMethod2()", 
				METHOD, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_implicit_inheritance"), 
				resultCollector);
		
		assertSearchResults("Search for role method decl which does not override an implicit inherited method. fq name",
				"src/p_implicit_inheritance/TestTeam2.java void p_implicit_inheritance.TestTeam2$TestRole1.roleMethod2() [roleMethod2]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declaration<br>
	 *  - method references<br>
	 * Search pattern:<br>
	 *  - combined method pattern
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- all occurences of a role method which overrides an implicit inherited role method<br> 
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- Declaration in TestTeam2.TestRole1<br>
	 *  no references
	 */
	//see TPX-359 (rename method refactoring needs this feature)
	public void test031() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_implicit_inheritance.TestTeam2.TestRole1.roleMethod()", 
				METHOD, 
				ALL_OCCURRENCES,
				getJavaSearchScopeFromPackage("p_implicit_inheritance"), 
				resultCollector);
		
		assertSearchResults("Search for role method decl which overrides an implicit inherited method. fq name",
				"src/p_implicit_inheritance/TestTeam2.java void p_implicit_inheritance.TestTeam2$TestRole1.roleMethod() [roleMethod]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method declaration<br>
	 *  - method references<br>
	 * Search pattern:<br>
	 *  - combined method pattern
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- declaration of a role method which does not override an implicit inherited role method<br> 
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- Declaration in TestTeam2.TestRole1<br>
	 *  no references
	 */
	public void test032() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_implicit_inheritance.TestTeam2.TestRole1.roleMethod2()", 
				METHOD, 
				ALL_OCCURRENCES,
				getJavaSearchScopeFromPackage("p_implicit_inheritance"), 
				resultCollector);
		
		assertSearchResults("Search for role method decl which does not override an implicit inherited method. fq name",
				"src/p_implicit_inheritance/TestTeam2.java void p_implicit_inheritance.TestTeam2$TestRole1.roleMethod2() [roleMethod2]",
				resultCollector);
	}
	
	
	/**
	 * Search for:<br>
	 *  - method references<br>
	 * Search pattern:<br>
	 *  - use IMethod<br>
	 * Searched element:<br>
	 * 	- method zork overridden in TestTeam2.TestRole1<br> 
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- Reference in TestTeam1.TestRole1.privateRoleMethod, but not tsr.zork() referring to regular super role<br>
	 *  no references
	 */
	public void testImplicitInheritance1() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaProject project= getJavaProject("OTJavaSearch");
		IType type= project.findType("p_implicit_inheritance.TestTeam2.TestRole1");
		IMethod method= type.getMethod("zork", new String[0]);
		search(method, 
			   REFERENCES,
			   getJavaSearchScopeFromPackage("p_implicit_inheritance"), 
			   resultCollector);
		
		assertSearchResults("Search for role method call within tsuper method. fq name",
				"src/p_implicit_inheritance/TestTeam1.java void p_implicit_inheritance.TestTeam1$TestRole1.privateRoleMethod() [zork()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *  - method declarations<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- callin method in a bound role  
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- 1 Declaration in Team1.Role1<br>
	 */
	public void test033() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_callin_method.Team1.Role1.callinMethod()", 
				METHOD, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_callin_method"), 
				resultCollector);
		
		assertSearchResults("Search for declaration of callin method. fq name",
				"src/p_callin_method/Team1.java void p_callin_method.Team1$Role1.callinMethod() [callinMethod]",
				resultCollector);
	}
	
	
	/**
	 * Search for:<br>
	 *  - method declarations<br>
	 * Search pattern:<br>
	 * 	- JavaElement<br>
	 * Searched element:<br>
	 * 	- callin method in a bound role  
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- 1 Declaration in Team1.Role1<br>
	 */
	public void test034() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		
		IType type = getRole(getTestProjectDir(),
	    		"src",
				"p_callin_method",
				"Team1",
				"Role1");
		
		IMethod method = type.getMethods()[0];
		
		search(
				method, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_callin_method"), 
				resultCollector);
		
		assertSearchResults("Search for declaration of callin method. javaModel",
				"src/p_callin_method/Team1.java void p_callin_method.Team1$Role1.callinMethod() [callinMethod]",
				resultCollector);
	}

	/**
	 * Search for:<br>
	 *  - method declarations and references<br>
	 * Search pattern:<br>
	 * 	- JavaElement<br>
	 * Searched element:<br>
	 * 	- callin method in a bound role and the method references  
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- 1 Declaration in Team1.Role1<br>
	 *		- 1 Reference in a callin (short sig)
	 */
	public void test035() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		
		IType type = getRole(getTestProjectDir(),
	    		"src",
				"p_callin_method",
				"Team1",
				"Role1");
		
		IMethod method = type.getMethods()[0];
		
		search(
				method, 
				ALL_OCCURRENCES,
				getJavaSearchScopeFromPackage("p_callin_method"), 
				resultCollector);
		
		assertSearchResults("Search for all occurrences of callin method. javaModel",
				"src/p_callin_method/Team1.java void p_callin_method.Team1$Role1.callinMethod() [callinMethod]\n" +
				"src/p_callin_method/Team1.java p_callin_method.Team1$Role1 callinMethod <- baseMethod [callinMethod]",
				resultCollector);
	}
	
	/**
	 * Reference to private role method (with focus set):
	 * Search for:<br>
	 *	- method references<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- role method declaration<br>
	 * Expected search result:<br>
	 *  References in<br>
	 *		- role callin method<br>
	 */
	public void test036() throws CoreException
	{
		ICompilationUnit unit = getCompilationUnit("OTJavaSearch", "src", "p_callin_method", "Team1.java");
		IType type = unit.getType("Team1");
		IType role = type.getType("Role1");
		IMethod method = role.getMethod("privateRoleMethod", new String[] { "QString;" });

		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_callin_method.Team1.Role1.privateRoleMethod(String)", 
				METHOD, 
				REFERENCES,
				method, /* focus */
				getJavaSearchScopeFromPackage("p_callin_method"), 
				resultCollector);
		
		assertSearchResults("src/p_callin_method/Team1.java void p_callin_method.Team1$Role1.callinMethodParam(String) [privateRoleMethod(str)]",
				resultCollector);
	}

	/**
	 * Search for:<br>
	 *  - method references<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- references of a private base method whose name is used in the subclass to declare a non private method  
	 * Expected search result:<br>
	 *	References in<br>
	 *		- Reference fo foo() in A.m()<br>
	 */
	// see TPX-363
	public void testTPX_363_1() throws CoreException
	{
		IType classA = getType(getTestProjectDir(), "src", "p_TPX_363", "A");
		IMethod methodFoo = classA.getMethod("foo", new String[0]);
		
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				methodFoo, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_TPX_363"), 
				resultCollector);
		
		assertSearchResults("see TPX-363, fq name",
				"src/p_TPX_363/A.java void p_TPX_363.A.m() [foo()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *  - method declaration<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- declaration of a private base method whose name is used in the subclass to declare a non private method  
	 * Expected search result:<br>
	 *	Declaration<br>
	 *		- private void A.foo()<br>
	 */
	// see TPX-363
	public void testTPX_363_2() throws CoreException
	{
		IType classA = getType(getTestProjectDir(), "src", "p_TPX_363", "A");
		IMethod methodFoo = classA.getMethod("foo", new String[0]);
		
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				methodFoo, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_TPX_363"), 
				resultCollector);
		
		assertSearchResults("see TPX-363, fq name",
				"src/p_TPX_363/A.java void p_TPX_363.A.foo() [foo]\n" +
				"src/p_TPX_363/B.java void p_TPX_363.B.foo() [foo]",
				resultCollector);
	}
	
	//see TPX-229
	public void testTPX_229_1() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p3.TeamA.RoleA.foo()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p3"), 
				resultCollector);
		
		assertSearchResults("src/p3/TeamA.java void p3.TeamA$RoleA.bar() [foo()]\n" +
		        			"src/p3/TeamB.java p3.TeamB$RoleB foo -> foo [foo]",
				resultCollector);
	}

	public void testTPX_229_2() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p3.TeamB.RoleB.foo()", 
				METHOD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p3"), 
				resultCollector);
		
		assertSearchResults("src/p3/TeamB.java p3.TeamB$RoleB foo -> foo [foo]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *  - method declarations<br>
	 * Search pattern:<br>
	 * 	- JavaElement<br>
	 * Searched element:<br>
	 * 	- callin method in a bound role  
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- 1 Declaration in Team1.Role1<br>
	 */
	public void testTPX_483() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		
		IType type = getRole(getTestProjectDir(),
	    		"src",
				"p_callin_method",
				"Team1",
				"Role1");
		
		IMethod method = type.getMethods()[1];
		
		search(
				method, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_callin_method"), 
				resultCollector);
		
		assertSearchResults("Search for declaration of callin method. javaModel",
				"src/p_callin_method/Team1.java void p_callin_method.Team1$Role1.callinMethodParam(String) [callinMethodParam]",
				resultCollector);
	}

	/**
	 * Search for:<br>
	 *	- method reference<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- overridden method gulp()<br> 
	 * Expected search result:<br>
	 *	Reference in<br>
	 *		- ref in TestTeam1.TestRole1.bar() <br>
	 *		- NOT ref in TestTeam2.TestRole1.bar()<br>
	 * built for implicit inheritance in analogy to https://bugs.eclipse.org/bugs/show_bug.cgi?id=160301
	 */
	public void test160301() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IJavaProject project= getJavaProject("OTJavaSearch");
		IType type= project.findType("p_implicit_inheritance.TestTeam1.TestRole1");
		IMethod method= type.getMethod("gulp", new String[0]);
		search( method,
				REFERENCES,
				getJavaSearchScopeFromPackage("p_implicit_inheritance"), 
				resultCollector);
		
		assertSearchResults("Search for references of implicitly overridden role method, fully qualified name",
							"src/p_implicit_inheritance/TestTeam1.java void p_implicit_inheritance.TestTeam1$TestRole1.bar() [gulp()]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- method reference<br>
	 * Search pattern:<br>
	 * 	- fully qualified name<br>
	 * Searched element:<br>
	 * 	- overridden method gulp()<br> 
	 * Expected search result:<br>
	 *	Reference in<br>
	 *		- ref in TestTeam1.TestRole1.bar() <br>
	 *		- NOT ref in TestTeam2.TestRole1.bar()<br>
	 * built for implicit inheritance in analogy to https://bugs.eclipse.org/bugs/show_bug.cgi?id=160301
	 */
	public void test160301b() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search("p_implicit_inheritance.TestTeam1.TestRole2.gulp()",
			   METHOD,
			   REFERENCES,
			   getJavaSearchScopeFromPackage("p_implicit_inheritance"), 
			   resultCollector);
		
		assertSearchResults("Search for references of implicitly overridden role method, fully qualified name",
							"src/p_implicit_inheritance/TestTeam1.java void p_implicit_inheritance.TestTeam1$TestRole2.good() [gulp()]",
				resultCollector);
	}

	// FIXME: add tests for role files, callouts with and without abstract method declaration, callins and callouts with short and long method specs, multiple base methods...
	
	// TODO(jsv) clean up block
	//	IType type = getCompilationUnit("OTJavaSearch", "src", "p",
	//			"Base1.java").getType("Base1");
	
	//	IType type = getType(getTestProjectDir(),
	//    		"src",
	//			"p",
	//			"Base1");
	//	
	//	
	//	IRoleType role = getRole(getTestProjectDir(),
	//            "src",
	//            "p",
	//            "Team1",
	//            "Role1");
	
	//	IMethod method = role.getMethods()[0];
	
	//	search(method, DECLARATIONS, getJavaSearchScope(), resultCollector);
}
