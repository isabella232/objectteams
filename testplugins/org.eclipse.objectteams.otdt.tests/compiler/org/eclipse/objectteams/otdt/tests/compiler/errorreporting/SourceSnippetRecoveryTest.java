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
 * $Id: SourceSnippetRecoveryTest.java 23494 2010-02-05 23:06:44Z stephan $
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
 * This class contains tests concerning source code snippets, or wrong code.
 * 
 * @author Markus Witte
 */
public class SourceSnippetRecoveryTest extends TestBase {
	
	public SourceSnippetRecoveryTest(String testName)
	{
		super(testName);
	}

	/**
	 * A "replace-callin binding" is used with a base call.
	 * Bound role method must be declared as callin method.
	 */
	public void testIncompleteCallinBinding()
	{
		createFile("MyClass","public class MyClass " +
			  NL + "{ " +
			  NL + "    baseMethod <- met1, met2;" +
			  NL + "} ");
	      
		
		compileFile("MyClass");
		
		assertTrue(hasExpectedProblems(new int[] {IProblem.ParsingErrorMergeTokens})); // silly but factual
	}	
}
