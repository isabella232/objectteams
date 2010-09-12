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
 * $Id: RoleContainmentTest.java 23494 2010-02-05 23:06:44Z stephan $
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
 * This class contains tests concerning roles.
 * 
 * @author kaschja
 * @version $Id: RoleContainmentTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class RoleContainmentTest extends TestBase
{	
    public RoleContainmentTest(String testName)
    {
        super(testName);
    }

    /**
	 * A role that is not declared as abstract contains an abstract method.
	 * Comment:
	 * A role has to be declared as abstract if it contains an abstract method. 
	 */
	public void testAbstractMethodInNonAbstractRole1() 
	{
		createFile("MyTeam","public team class MyTeam { " +
				NL + "	protected class MyRole {" +
				NL + "	    public abstract void roleMethod();" +
				NL + "  }"+
				NL + "}");
		
		compileFile("MyTeam");
		
		assertTrue(hasExpectedProblems(new int[] { IProblem.AbstractMethodsInConcreteClass, IProblem.AbstractMethodInAbstractClass } ));
	}
}
