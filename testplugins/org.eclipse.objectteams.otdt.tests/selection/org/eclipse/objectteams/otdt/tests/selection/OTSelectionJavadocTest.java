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
 * $Id: OTSelectionJavadocTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.selection;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.compiler.parser.SelectionJavadocTest;

/**
 * Testing selection of OT-specific javadoc.
 * 
 * @author stephan
 * @since 1.2.5
 */
public class OTSelectionJavadocTest extends SelectionJavadocTest {

	public OTSelectionJavadocTest(String testName) {
		super(testName);
	}
	
	public static Test suite() {
		return buildAllCompliancesTestSuite(OTSelectionJavadocTest.class);
	}

	/**
	 * Select role file name after @role javadoc tag
	 */
	public void testRoleTag1()
	{
		setUnit("T1.java",
			"/**\n" +
		    "  * @role MyRoFi\n" +
		    "  */\n" +
		    "public team class T1 {\n" +
			"}\n");
	
		findJavadoc("MyRoFi");
		
		assertValid("/**<SelectOnType:MyRoFi>*/\n");
	}


}
