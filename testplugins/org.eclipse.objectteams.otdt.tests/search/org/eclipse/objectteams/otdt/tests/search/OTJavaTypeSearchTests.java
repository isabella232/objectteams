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
 * $Id: OTJavaTypeSearchTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.search;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.objectteams.otdt.core.IRoleType;

/**
 * @author svacina
 * 
 * @version $Id: OTJavaTypeSearchTests.java 23494 2010-02-05 23:06:44Z stephan $
 */

//Note: in case of search for type declaration with fully qualified name use the search method with the current JavaModel
//		and not the fully qualified name as string -> Workbench use the javaElement to build the search pattern   
public class OTJavaTypeSearchTests extends OTJavaSearchTestBase
{
	private Map originalOptions;

	public OTJavaTypeSearchTests(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
	    if (false)
	    {
	        System.err.println("Warning, only part of the OTJavaTypeSearchTest are being executed!");
			Suite suite = new Suite(OTJavaTypeSearchTests.class.getName());
			suite.addTest(new OTJavaTypeSearchTests("test016"));
			return suite;
	    }
	    return buildModelTestSuite(OTJavaTypeSearchTests.class);
		//return new Suite(OTJavaTypeSearchTests.class);
	}

	// the following inspired from JavaSearchJavadocTests:
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
	}
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		this.originalOptions = this.javaProject.getOptions(true);
		this.javaProject.setOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
	}
		
	public void tearDown() throws Exception {
		this.javaProject.setOptions(originalOptions);
		super.tearDown();
	}
	private void setJavadocOptions() {
		this.javaProject.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);
		this.javaProject.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.ERROR);
	}
	private void disableJavadocOptions() {
		this.javaProject.setOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.DISABLED);
	}
	
	/**
	 * Search for:<br>
	 *	- Team declaration<br> 
	 * Search pattern:<br>
	 *	- fully qualified name<br>
	 * Searched element:<br>
	 *	- Team declaration<br>
	 * Expected search result:<br>
	 *	Declaration in <br>
	 *		- package(p)<br>
	 */
	public void test001() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_type_search.T1", 
				TYPE, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("Team decl, fq name",
							"src/p_type_search/T1.java p_type_search.T1 [T1]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- Team declaration<br> 
	 * Search pattern:<br>
	 *	- simple name<br>
	 * Searched element:<br>
	 *	- Team (no role) declaration<br>
	 * Expected search result:<br>
	 *	Declaration in <br>
	 *		- package(p)<br>
	 */
	public void test002() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"T1", 
				TYPE, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("Team decl, simple name",
							"src/p_type_search/T1.java p_type_search.T1 [T1]",
				resultCollector);
	}
		
	/**
	 * Search for:<br>
	 *	- Interface References in the implementors<br> 
	 * Search pattern:<br>
	 *	- fully qualified name<br>
	 * Searched element:<br>
	 *	- Type declarations (team/role/base) declaration<br>
	 * Expected search result:<br>
	 *	References after "implements" in<br>
	 *		- B1 (base)<br>
	 *		- T1 (team)<brt>
	 *		- T2.T3 (nested team)<br>
	 *		- T2.R0 (role)<br>
	 *		- T6.R6 (role file)<br>
	 */
	public void test003() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_type_search.I1", 
				TYPE, 
				IMPLEMENTORS,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search","p_type_search.T6"}),
				resultCollector);
		
		assertSearchResults("Interface References in the implementors, fq name",
							"src/p_type_search/B1.java p_type_search.B1 [I1]\n" + 
							"src/p_type_search/T1.java p_type_search.T1 [I1]\n" +
							"src/p_type_search/T2.java p_type_search.T2$R0 [I1]\n" +
							"src/p_type_search/T2.java p_type_search.T2$T3 [I1]\n" +
							"src/p_type_search/T6/R6.java p_type_search.T6.R6 [I1]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- References in Implementors of a Interface<br> 
	 * Search pattern:<br>
	 *	- simple name<br>
	 * Searched element:<br>
	 *	- Type declarations (team/role/base) declaration<br>
	 * Expected search result:<br>
	 *	References after "implements" in<br>
	 *		- B1 (base)<br>
	 *		- T1 (team)<brt>
	 *		- T2.T3 (nested team)<br>
	 *		- T2.R0 (role)<br>
	 *		- T6.R6 (role file)<br>
	 */
	public void test004() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"I1", 
				TYPE, 
				IMPLEMENTORS,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search","p_type_search.T6"}),
				resultCollector);
		
		assertSearchResults("Interface References in the implementors, simple name",
							"src/p_type_search/B1.java p_type_search.B1 [I1]\n" + 
							"src/p_type_search/T1.java p_type_search.T1 [I1]\n" +
							"src/p_type_search/T2.java p_type_search.T2$R0 [I1]\n" +
							"src/p_type_search/T2.java p_type_search.T2$T3 [I1]\n" +
							"src/p_type_search/T6/R6.java p_type_search.T6.R6 [I1]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- Role declaration<br> 
	 * Search pattern:<br>
	 *	- fully qualified name<br>
	 * Searched element:<br>
	 *	- role declaration<br>
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- T2<br>
	 */
	public void test005() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_type_search.T2.R1", 
				TYPE, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("role declaration, fully qualified name",
							"src/p_type_search/T2.java p_type_search.T2$R1 [R1]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- Role declaration<br> 
	 * Search pattern:<br>
	 *	- simple name<br>
	 * Searched element:<br>
	 *	- role declaration<br>
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- T2<br>
	 */
	public void test006() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"R1", 
				TYPE, 
				DECLARATIONS,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("role declaration, simple",
							"src/p_type_search/T2.java p_type_search.T2$R1 [R1]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- Role reference in lifting type<br> 
	 * Search pattern:<br>
	 *	- fully qualified name<br>
	 * Searched element:<br>
	 *	- role reference<br>
	 * Expected search result:<br>
	 *	Reference in<br>
	 *		- T2.teamMethod(R1 as B1 role)<br>
	 */
	public void testRoleLiftingReferences1() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_type_search.T2.R1", 
				TYPE, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("Role reference in lifting type, fq name",
							"src/p_type_search/T2.java void p_type_search.T2.teamMethod(B1) [R1]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- Role reference in lifting type<br> 
	 * Search pattern:<br>
	 *	- simple name<br>
	 * Searched element:<br>
	 *	- role reference<br>
	 * Expected search result:<br>
	 *	Reference in<br>
	 *		- T2.teamMethod(R1 as B1 role)<br>
	 */
	public void testRoleLiftingReferences2() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"R1", 
				TYPE, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("Role reference in lifting type, simple name",
							"src/p_type_search/T2.java void p_type_search.T2.teamMethod(B1) [R1]",
				resultCollector);
	}
	
	// Fine Grained: restrict to class instance creation
	public void testRoleLiftingReferences3() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"R1", 
				TYPE, 
				CLASS_INSTANCE_CREATION_TYPE_REFERENCE,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("Role reference in lifting type - restricted to class instance creation",
							"src/p_type_search/T2.java void p_type_search.T2.teamMethod(B1) [R1]",
				resultCollector);
	}

	// Fine Grained: restrict to class instance creation
	public void testRoleLiftingReferences4() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"R8", 
				TYPE, 
				CLASS_INSTANCE_CREATION_TYPE_REFERENCE,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("Role lifting references - restricted to class instance creation",
							"src/p_type_search/T8.java p_type_search.T8$R8 consumeR8(R8) <- getB3() [R8]\n" + 
							"src/p_type_search/T8.java p_type_search.T8$R8 getR8() -> getB3() [R8]",
				resultCollector);
	}

	// Fine Grained: restrict to class instance creation
	public void testRoleReferenceInCallin1() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"R6", 
				TYPE, 
				CLASS_INSTANCE_CREATION_TYPE_REFERENCE,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("Role reference in method spec, lifting position - restricted to class instance creation",
				"src/p_type_search/T2.java void p_type_search.T2.teamMethod(B3) [R6]\n" +	// declared lifting 
				"src/p_type_search/T2.java p_type_search.T2$R6 role6Method(R6) <- compareTo(B3) [R6]", // callin arg lifting
				resultCollector);
	}

	// Fine Grained: restrict to class instance creation
	public void testRoleReferenceInCallin2() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"R6", 
				TYPE, 
				CLASS_INSTANCE_CREATION_TYPE_REFERENCE|PARAMETER_DECLARATION_TYPE_REFERENCE,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("Role reference in method spec, lifting position - restricted to class instance creation or method parameter",
				"src/p_type_search/T2.java void p_type_search.T2.teamMethod(B3) [R6]\n" +	// declared lifting
				"src/p_type_search/T2.java boolean p_type_search.T2$R6.role6Method(R6) [R6]\n" + // method parameter
				"src/p_type_search/T2.java p_type_search.T2$R6 role6Method(R6) <- compareTo(B3) [R6]", // callin arg lifting
				resultCollector);
	}

	// Fine Grained: restrict to class instance creation
	public void testRoleReferenceInCallin3() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"R6", 
				TYPE, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("Role reference in method spec, lifting position - all references",
				"src/p_type_search/T2.java void p_type_search.T2.teamMethod(B3) [R6]\n" +	// declared lifting
				"src/p_type_search/T2.java boolean p_type_search.T2$R6.role6Method(R6) [R6]\n" + // method parameter
				"src/p_type_search/T2.java p_type_search.T2$R6 role6Method(R6) <- compareTo(B3) [R6]\n" +  // callin arg lifting
				"src/p_type_search/T6.java p_type_search.T6 [R6]", // javadoc @role tag
				resultCollector);
	}
	
	// see https://svn.objectteams.org/trac/ot/ticket/81
	public void testRoleReferenceInPrecedences()  throws CoreException 
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"R71", 
				TYPE, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("Role reference in precedence declaration, simple name",
							"src/p_type_search/T7.java p_type_search.T7 [R71]",
				resultCollector);
	}

	// see https://svn.objectteams.org/trac/ot/ticket/82
	public void testRoleReferenceInPredicate()  throws CoreException 
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"R72", 
				TYPE, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_type_search"), 
				resultCollector);
		
		assertSearchResults("Role reference in precedence declaration, simple name",
							"src/p_type_search/T7.java p_type_search.T7 [R72]\n"+   // precedence
							"src/p_type_search/T7.java boolean p_type_search.T7$R72._OT$base_when() [R72]", // predicate
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- Role declaration<br> 
	 * Search pattern:<br>
	 *	- javaModel<br>
	 * Searched element:<br>
	 *	- role declaration in role file<br>
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- p_type_search.T6<br>
	 */
	public void test009() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IType role = getType(getTestProjectDir(),
		    		"src",
					"p_type_search.T6",
					"R6");
		search(
				role,
				DECLARATIONS,
				getJavaSearchScopeFromProject(),
				resultCollector);
		
		assertSearchResults("Role declaration in role file, JavaModel",
				"src/p_type_search/T6/R6.java p_type_search.T6.R6 [R6]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- Role declaration<br> 
	 * Search pattern:<br>
	 *	- simple name<br>
	 * Searched element:<br>
	 *	- role declaration in role file<br>
	 * Expected search result:<br>
	 *	Declaration in<br>
	 *		- p_type_search.T6<br>
	 */
	public void test010() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"R6", 
				TYPE, 
				DECLARATIONS,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search.T6"}), 
				resultCollector);
		
		assertSearchResults("Role declaration in role file, simple name",
				"src/p_type_search/T6/R6.java p_type_search.T6.R6 [R6]",
				resultCollector);
	}
	
	//base class as return type in callin callout with signature
	/**
	 * Search for:<br>
	 *	- Base class references<br> 
	 * Search pattern:<br>
	 *	- JavaModel<br>
	 * Searched element:<br>
	 *	- Base class references in role/base method specs<br>
	 * Expected search result:<br>
	 *	References in<br>
	 *		- return type in B1.baseMethod3<br>
	 *		- return type in T2.R1.roleMethod<br>
	 *		- return types in callin C1 roleMethod() <- before C1 baseMethod3();<br>
	 *		- return types in callout C1 roleMethod2() -> C1 baseMethod3();<br>
	 */
	public void test011() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IType type = getType(getTestProjectDir(),
	    		"src",
				"p_type_search",
				"C1");
		
		search(
				type, 
				REFERENCES,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search","p_type_search.T6"}), 
				resultCollector);
		
		assertSearchResults("Base class references in callins/callouts as return types, JavaModel",
				"src/p_type_search/B1.java C1 p_type_search.B1.baseMethod3(B4) [C1]\n" + 
				"src/p_type_search/T2.java C1 p_type_search.T2$R1.roleMethod(B4) [C1]\n" + 
				"src/p_type_search/T2.java p_type_search.T2$R1 roleMethod(B4) <- baseMethod3(B4) [C1]\n" + 
				"src/p_type_search/T2.java p_type_search.T2$R1 roleMethod(B4) <- baseMethod3(B4) [C1]\n" + 
				"src/p_type_search/T2.java p_type_search.T2$R1 roleMethod2(B4) -> baseMethod3(B4) [C1]\n" +
				"src/p_type_search/T2.java p_type_search.T2$R1 roleMethod2(B4) -> baseMethod3(B4) [C1]",
				resultCollector);
	}	
	
	//Base class in type binding (after playedBy (in nested teams und rolefiles))
	/**
	 * Search for:<br>
	 *	- Base class references<br> 
	 * Search pattern:<br>
	 *	- JavaModel<br>
	 * Searched element:<br>
	 *	- Base class references after playedBy keyword<br>
	 * Expected search result:<br>
	 *	References in<br>
	 *		- p_type_search.T6.R6 (role file)<br> 
	 *		- p_type_search.T2.T3.R5 (nested team)<br>
	 *      - p_type_search.B3 (as superclass)
	 */
	public void test012() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IType type = getType(getTestProjectDir(),
	    		"src",
				"p_type_search",
				"B2");
		
		search(
				type, 
				REFERENCES,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search","p_type_search.T6"}), 
				resultCollector);
		
		assertSearchResults("Base class after playedBy in roleFile and nested Team, JavaModel",
		        "src/p_type_search/B3.java p_type_search.B3 [B2]\n" +
		        "src/p_type_search/T2.java p_type_search.T2$R6 [B2]\n" + // javadoc reference (@see) 
				"src/p_type_search/T2.java p_type_search.T2$T3$R5 [B2]\n" + 
				"src/p_type_search/T6/R6.java p_type_search.T6.R6 [B2]",
				resultCollector);
	}

	//Base class in type binding (only after playedBy (in nested teams und rolefiles))
	/**
	 * Search for:<br>
	 *	- Base class references<br> 
	 * Search pattern:<br>
	 *	- JavaModel<br>
	 * Searched element:<br>
	 *	- Base class references after playedBy keyword<br>
	 * Expected search result:<br>
	 *	References in<br>
	 *		- p_type_search.T6.R6 (role file)<br> 
	 *		- p_type_search.T2.T3.R5 (nested team)<br>
     *      - DON'T show B3 (cf. above)
	 */
	public void test012a() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IType type = getType(getTestProjectDir(),
	    		"src",
				"p_type_search",
				"B2");
		
		search(
				type, 
				PLAYEDBY_REFERENCES,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search","p_type_search.T6"}), 
				resultCollector);
		
		assertSearchResults("Base class after playedBy in roleFile and nested Team, JavaModel",
				"src/p_type_search/T2.java p_type_search.T2$T3$R5 [B2]\n" + 
				"src/p_type_search/T6/R6.java p_type_search.T6.R6 [B2]",
				resultCollector);
	}
	//Base class in javadoc @role tag
	/**
	 * Search for:<br>
	 *	- Role class references<br> 
	 * Search pattern:<br>
	 *	- JavaModel<br>
	 * Searched element:<br>
	 *	- Role class references after @role tag<br>
	 * Expected search result:<br>
	 *	References in<br>
	 *		- p_type_search.T6 (role tag)
	 */
	public void test012b() throws CoreException
	{
		setJavadocOptions();
		
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		resultCollector.showInsideDoc = true;

		IType teamType = getType(getTestProjectDir(),
	    		"src",
				"p_type_search",
				"T6");
		IType type = getRole(teamType,
				"T6.R6");
		
		search(
				type, 
				REFERENCES,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search","p_type_search.T6"}), 
				resultCollector);
		
		assertSearchResults("Javadoc role reference, JavaModel",
				"src/p_type_search/T6.java p_type_search.T6 [R6] EXACT_MATCH INSIDE_JAVADOC",
				resultCollector);
	}
	//Base class in javadoc @role tag, filtered, not a playedBy reference.
	/**
	 * Search for:<br>
	 *	- Base class references<br> 
	 * Search pattern:<br>
	 *	- JavaModel<br>
	 * Searched element:<br>
	 *	- Class references after playedBy keyword<br>
	 * Expected search result:<br>
     *  - no matches
	 */
	public void test012c() throws CoreException
	{
		setJavadocOptions();
				
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IType teamType = getType(getTestProjectDir(),
	    		"src",
				"p_type_search",
				"T6");
		IType type = getRole(teamType,
				"T6.R6");
		
		search(
				type, 
				PLAYEDBY_REFERENCES,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search","p_type_search.T6"}), 
				resultCollector);
		
		assertSearchResults("Javadoc role reference, JavaModel",
				"",
				resultCollector);
	}
	
	// Base class in lifting type
	/**
	 * Search for:<br>
	 *	- Base class references<br> 
	 * Search pattern:<br>
	 *	- JavaModel<br>
	 * Searched element:<br>
	 *	- Base class references in lifting type<br>
	 * Expected search result:<br>
	 *	References in<br>
	 *		- role declaration (public class R6 playedBy B3)<br> 
	 *		- base side of lifting type (teamMethod(B3 as R6 role))<br>
     *      - regular base method (B3.compareTo(B3))<br>
     *      - regular base method (B3.getB3()B3)<br>
     *      - base method spec in callin (<- replace compareTo(B3)<br>
     *      - playedBy of T8.R8
     *      - callin binding T8.R8.consumeR8()<-after getB3()
     *      - callout binding T8.R8.getR8()->getB3()
	 */
	public void test013() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IType type = getType(getTestProjectDir(),
	    		"src",
				"p_type_search",
				"B3");
		
		search(
				type, 
				REFERENCES,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search","p_type_search.T6"}), 
				resultCollector);
		
		assertSearchResults("Base class after playedBy in roleFile and nested Team, JavaModel",
				"src/p_type_search/B3.java boolean p_type_search.B3.compareTo(B3) [B3]\n" +
				"src/p_type_search/B3.java B3 p_type_search.B3.getB3() [B3]\n" +
				"src/p_type_search/T2.java void p_type_search.T2.teamMethod(B3) [B3]\n" +
				"src/p_type_search/T2.java p_type_search.T2$R6 [B3]\n" + 
				"src/p_type_search/T2.java p_type_search.T2$R6 role6Method(R6) <- compareTo(B3) [B3]\n" + 
				"src/p_type_search/T8.java p_type_search.T8$R8 [B3]\n" +
				"src/p_type_search/T8.java p_type_search.T8$R8 consumeR8(R8) <- getB3() [B3]\n" + 
				"src/p_type_search/T8.java p_type_search.T8$R8 getR8() -> getB3() [B3]",
				resultCollector);
	}
	
	// Base class as argument in method spec 
	/**
	 * Search for:<br>
	 *	- Base class references<br> 
	 * Search pattern:<br>
	 *	- JavaModel<br>
	 * Searched element:<br>
	 *	- Base class references in method spec signature<br>
	 * Expected search result:<br>
	 *	References in<br>
	 *		- callin / callout / method declaration<br> 
	 */
	public void test014() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IType type = getType(getTestProjectDir(),
	    		"src",
				"p_type_search",
				"B4");
		
		search(
				type, 
				REFERENCES,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search","p_type_search.T6"}), 
				resultCollector);
		
		assertSearchResults("Base class as argument in callin callout with signature, JavaModel",
				"src/p_type_search/B1.java C1 p_type_search.B1.baseMethod3(B4) [B4]\n" + 
				"src/p_type_search/T2.java C1 p_type_search.T2$R1.roleMethod(B4) [B4]\n" + 
				"src/p_type_search/T2.java p_type_search.T2$R1 roleMethod(B4) <- baseMethod3(B4) [B4]\n" + 
				"src/p_type_search/T2.java p_type_search.T2$R1 roleMethod(B4) <- baseMethod3(B4) [B4]\n" + 
				"src/p_type_search/T2.java p_type_search.T2$R1 roleMethod2(B4) -> baseMethod3(B4) [B4]\n" + 
				"src/p_type_search/T2.java p_type_search.T2$R1 roleMethod2(B4) -> baseMethod3(B4) [B4]",
				resultCollector);
	}
	
	// Base class as role attribute
	/**
	 * Search for:<br>
	 *	- Base class references<br> 
	 * Search pattern:<br>
	 *	- JavaModel<br>
	 * Searched element:<br>
	 *	- Base class references in private role attribute<br>
	 * Expected search result:<br>
	 *	References in<br>
	 *		- p_type_search.T2.T3.R5<br> 
	 */
	public void test015() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IType type = getType(getTestProjectDir(),
	    		"src",
				"p_type_search",
				"B5");
		
		search(
				type, 
				REFERENCES,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search","p_type_search.T6"}), 
				resultCollector);
		
		assertSearchResults("Base class in private role attribute, JavaModel",
				"src/p_type_search/T2.java p_type_search.T2$T3$R5.b5 [B5]",
				resultCollector);
	}
	//TODO(jsv) test case for searching references of base class in parameter mapping

	/**
	 * Search for:<br>
	 *	- Team reference<br> 
	 * Search pattern:<br>
	 *	- JavaModel<br>
	 * Searched element:<br>
	 *	- Team reference in the team package<br>
	 * Expected search result:<br>
	 *	Reference in <br>
	 *		- team package of role T6.R6<br>
	 */
	public void test016() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IType type = getType(getTestProjectDir(),
	    		"src",
				"p_type_search",
				"T6");
		
		search(
				type, 
				REFERENCES,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search","p_type_search.T6"}), 
				resultCollector);
		
		assertSearchResults("Team reference in Team package, JavaModel",
							"src/p_type_search/T6/R6.java [p_type_search.T6]",
				resultCollector);
	}
	
	/**
	 * Search for:<br>
	 *	- Reference to externalized role using <code>type anchor</code>(base)<br> 
	 * Search pattern:<br>
	 *	- JavaModel<br>
	 * Searched element:<br>
	 *	- Reference to externalized role in field declaration, callin method, and after playedBy<br>
	 * Expected search result:<br>
	 *	Reference in <br>
	 *		- field declaration in role T4.R7<br>
	 *		- callin method in role T4.R7<br>
	 *		- after playedBy in role T4.R2<br>
	 */
	public void test017() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IRoleType type = getRole(getTestProjectDir(),
	    		"src",
				"p_type_search",
				"T5",
				"R8");
		
		search(
				type.getCorrespondingJavaElement(), 
				REFERENCES,
				getJavaSearchScopeFromPackages(new String[]{"p_type_search"}), 
				resultCollector);
		
		assertSearchResults("Reference to externalized role in field declaration, callin method, and after playedBy, JavaModel",
							"src/p_type_search/T4.java p_type_search.T4$R7.aRoleOfMyBase [base.R8]\n" +
		        			"src/p_type_search/T4.java void p_type_search.T4$R7.tm(base.R8) [base.R8]\n" +
		        			"src/p_type_search/T4.java p_type_search.T4$R7$R2 [base.R8]",
				resultCollector);
	}

}
