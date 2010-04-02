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
 * $Id: TeamInheritanceTest.java 23494 2010-02-05 23:06:44Z stephan $
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
 * This class contains tests concerning team inheritance.
 * 
 * @author kaschja
 * @version $Id: TeamInheritanceTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class TeamInheritanceTest extends TestBase
{
    public TeamInheritanceTest(String testName)
    {
        super(testName);
    }

    /**
	 * A team class inherits from a non-team class.
	 * Comment:
	 * This is legal since Trac #144.
	 */ 
	public void testTeamInheritance1() 
	{
	    createFile("OrdinaryClass","public class OrdinaryClass {}");
	    			
	    createFile("MyTeam","public team class MyTeam extends OrdinaryClass {}");
				
		compileFile("MyTeam");
				
	    assertTrue(isCompilationSuccessful());
    }
    
	/**
	 * A regular class (non-team class) inherits from a team class.
	 * Comment:
	 * A regular Java class can only extend another Java class,not a team class.
	 */
	public void testInvalidTeamInheritance2()
	{
		createFile("MyTeam","public team class MyTeam {}");
		
		createFile("MyClass","public class MyClass extends MyTeam {}");
		
		compileFile("MyClass");
				
		assertFalse(isCompilationSuccessful());
	}

	/** 
	 * 	A team class inherits from an abstract team class.
	 *  It does not implement the inherited abstract method.
	 *  Comment:
	 *  A team has to implement the inherited abstract methods of the superteam.
	 */
	public void testNotImplementedAbstractMethod1()
	{
		createFile("Superteam","public abstract team class Superteam " +
			  NL + "{ " +	
			  NL + "	abstract void teamMethod(); " +
			  NL + "}");
		
		createFile("Subteam","public team class Subteam extends Superteam" +
			  NL + "{ " +	
			  NL + "}");
				
		compileFile("Subteam");
		
		assertFalse(isCompilationSuccessful());	
	}
	
	/** 
	 * 	A team class contains a role class.
	 *  Another team class that inherits from this team, does not have to
	 *  contain the inherited role explicitly.
	 */  
	public void testImplicitInheritance1()
	{
		createFile("Superteam","public team class Superteam " +
			  NL + "{ " +	
			  NL + "	protected class Role {}; " +
			  NL + "}");
		
		createFile("Subteam","public team class Subteam extends Superteam" +
			  NL + "{ " +	
			  NL + "}");
				
		compileFile("Superteam");
		
		assertTrue(isCompilationSuccessful());	
	}
	
	/** 
	 * @see testImplicitInheritance1
	 * additional instantiation of inherited role
	 */  
	public void testImplicitInheritance2()
	{
		createFile("Superteam","public team class Superteam " +
			  NL + "{ " +	
			  NL + "	protected class Role {}; " +
			  NL + "}");
	
		createFile("Subteam","public team class Subteam extends Superteam" +
			  NL + "{ " +
			  NL + "  Role _myRole = new Role(); " +
			  NL + "}");
			
		compileFile("Superteam");
	
		assertTrue(isCompilationSuccessful());	
	}
	
	public void testMissingOTRE1() {
		createFile("T",
				"import static org.objectteams.Team.ALL_THREADS;\n" +
				"class B {}\n" +
				"public team class T {\n" +
				"    protected class R playedBy B {}\n" +
				"    Thread a = ALL_THREADS;\n" +
				"}");
		compileFile("T", createClassPathNoOTRE("T"));
		assertTrue(hasAtLeastExpectedProblems(new int[] {IProblem.ImportNotFound, IProblem.IsClassPathCorrect, IProblem.HierarchyHasProblems}));
	}
}
