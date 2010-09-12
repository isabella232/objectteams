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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.test.builder;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.builder.TestingEnvironment;
import org.eclipse.objectteams.otdt.internal.compiler.adaptor.BuildManager;

public class OTBuilderTests extends BuilderTests {

	OTTestingEnvironment otenv;

	public OTBuilderTests(String name) {
		super(name);
	}

	/** Sets up this test.
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.otenv = new OTTestingEnvironment();
		this.otenv.activate();
		env = new TestingEnvironment();
		env.openEmptyWorkspace();
		env.resetWorkspace();
	
		BuildManager.DEBUG = 2;
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.otenv.deactivate();
		this.otenv = null;
	}
	/** Verifies that the given element has problems.
	 * Old implementation from BuilderTests
	 */
	protected void expectingProblemsFor(IPath expected) {
		if (DEBUG)
			printProblemsFor(expected);
	
		/* get the leaf problems for this type */
		Problem[] problems = env.getProblemsFor(expected);
		assertTrue("missing expected problem with " + expected.toString(), problems.length > 0); //$NON-NLS-1$
	}

}