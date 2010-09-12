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
 * $Id: OTJavaFieldSearchTests.java 23494 2010-02-05 23:06:44Z stephan $
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

/**
 * @author svacina
 * 
 * @version $Id: OTJavaFieldSearchTests.java 23494 2010-02-05 23:06:44Z stephan $
 */

//Note: in case of search for type declaration with fully qualified name use the search method with the current JavaModel
//		and not the fully qualified name as string -> Workbench use the javaElement to build the search pattern   
public class OTJavaFieldSearchTests extends OTJavaSearchTestBase
{
	public OTJavaFieldSearchTests(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
	    if (false)
	    {
	        System.err.println("Warning, only part of the OTJavaTypeSearchTest are being executed!");
			Suite suite = new Suite(OTJavaFieldSearchTests.class.getName());
			suite.addTest(new OTJavaFieldSearchTests("test016"));
			return suite;
	    }
	    
		return new Suite(OTJavaFieldSearchTests.class);
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
				"p_field_search.FieldAccessInRoleMethod.R.name", 
				FIELD, 
				READ_ACCESSES|WRITE_ACCESSES,
				getJavaSearchScopeFromPackage("p_field_search"), 
				resultCollector);
		
		assertSearchResults("Team decl, fq name",
							"src/p_field_search/FieldAccessInRoleMethod.java void p_field_search.FieldAccessInRoleMethod$R.setName(String) [name]",
				resultCollector);
	}
	
	public void test002() throws CoreException
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
				"p_field_search.FieldAccessInCallinMethod.R.name", 
				FIELD, 
				READ_ACCESSES|WRITE_ACCESSES,
				getJavaSearchScopeFromPackage("p_field_search"), 
				resultCollector);
		
		assertSearchResults("Team decl, fq name",
							"src/p_field_search/FieldAccessInCallinMethod.java void p_field_search.FieldAccessInCallinMethod$R.setName(String) [name]",
				resultCollector);
	}
	
	/**
     * Parameter mappings contain field references.
     */
	public void test003() throws CoreException 
	{
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		search(
				"p_field_search.FieldAccessInParameterMapping.Role.value", 
				FIELD, 
				REFERENCES,
				getJavaSearchScopeFromPackage("p_field_search"), 
				resultCollector);
		
		assertSearchResults("Expecting two exact matching references to Role.value",
				"src/p_field_search/FieldAccessInParameterMapping.java p_field_search.FieldAccessInParameterMapping$Role rm1() -> baseMethod() [value] EXACT_MATCH\n" + 
				"src/p_field_search/FieldAccessInParameterMapping.java p_field_search.FieldAccessInParameterMapping$Role rm2() <- baseMethod4() [value] EXACT_MATCH",
				resultCollector);
	}
}
