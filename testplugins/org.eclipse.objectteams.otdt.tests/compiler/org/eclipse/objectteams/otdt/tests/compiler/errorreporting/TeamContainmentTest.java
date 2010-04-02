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
 * $Id: TeamContainmentTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.errorreporting;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.tests.compiler.TestBase;


/**
 * This class contains tests concerning team inheritance.
 * 
 * @author kaschja
 * @version $Id: TeamContainmentTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class TeamContainmentTest extends TestBase
{
    public TeamContainmentTest(String testName)
    {
        super(testName);
    }

	/* An empty team with no package statement should compile without 
		 * problems. 
		 */
		public void testEmptyTeam1() 
		{
			createFile("MyTeam","public team class MyTeam " +
				  NL + "{ " +				 					  
				  NL + "}");
	
			compileFile("MyTeam");
	
			assertTrue(isCompilationSuccessful());
		}

	/* An empty team with a specific package statement should compile without 
	 * problems. 
	 */
	public void testEmptyTeam2() 
	{
		createFile("MyTeam","package teams;" + 
			  NL + "public team class MyTeam " +
			  NL + "{ " +				 					  
			  NL + "}");
	
		compileFile("teams"+File.separator+"MyTeam");
	
		assertTrue(isCompilationSuccessful());
	}
	
	/* An empty team with a specific package statement should compile without 
	 * problems. 
	 */
	public void testEmptyTeam3() 
	{
		createFile("MyTeam","" + "package pkg1.pkg2.pkg3;" + 
			  NL + "public team class MyTeam " +
			  NL + "{ " +				 					  
			  NL + "}");
	
		compileFile("pkg1"+File.separator+"pkg2"+File.separator+"pkg3"+File.separator+"MyTeam");
	
		assertTrue(isCompilationSuccessful());
	}

    /** 
	 * A team that is not declared as abstract contains an abstract role.
	 * Comment:
	 * A team has to be declared as abstract if it contains an abstract relevant role.
     * For the notion of "relevant roles" see OT lang.def. 7.1(c) (SH).
	 */
	public void testAbstractRoleInNonAbstractTeam1() 
	{
        // TODO (SH): differentiate relevant/irrelevant roles (s.above).
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +	
			  NL + "	abstract protected class MyRole " +
			  NL + "    {" +
		      NL + "	    abstract void roleMethod();" +
		      NL + "	}" +			  
			  NL + "}");
		
		compileFile("MyTeam");
		
		assertTrue(isCompilationSuccessful());
	}
	
	/**
	 * A team that is not declared as abstract contains an abstract 
	 * team-level method.
	 * Comment:
	 * A team has to be declared as abstract if it contains an abstract
	 * team-level method.
	 */
	public void testAbstractMethodInNonAbstractTeam1() 
	{
		createFile("MyTeam","public team class MyTeam " +
			  NL + "{ " +				 
			  NL + "	abstract void teamMethod();" +					  
			  NL + "}");
		
		compileFile("MyTeam");
	
		assertTrue(hasExpectedProblems(new int[]{ IProblem.AbstractMethodsInConcreteClass, IProblem.AbstractMethodInAbstractClass}));
	}
	
	public void testRoleLackingJavadoc1() 
	{
		createFile("MyBase","/** Comment for MyBase. */" +
				  NL + "public class MyBase " +				
				  NL + "{ " +	
				  NL + "}");
		
		createFile("MyTeam","/** Comment for MyTeam. */" +
			  NL + "public team class MyTeam " +				
			  NL + "{ " +	
			  NL + "	public class MyRole playedBy MyBase" +
			  NL + "    {" +
		      NL + "	}" +			  
			  NL + "}");
		
		Map<String,String> options= new HashMap<String,String>();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, CompilerOptions.ERROR);

		compileFile("MyTeam", null, options);
		
		assertTrue(hasExpectedProblems(new int[]{ IProblem.JavadocMissing})); // reported only once!
	}
	
	public void testRoleLackingJavadoc2() 
	{
		createFile("MyBase","/** Comment for MyBase. */" +
				  NL + "public class MyBase " +				
				  NL + "{ " +	
				  NL + "}");
		
		createFile("MyTeam","/** Comment for MyTeam. */" +
			  NL + "public team class MyTeam " +				
			  NL + "{ " +	
			  NL + "	protected class MyRole playedBy MyBase" +
			  NL + "    {" +
		      NL + "	}" +			  
			  NL + "}");
		
		Map<String,String> options= new HashMap<String,String>();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, CompilerOptions.ERROR);

		compileFile("MyTeam", null, options);
		
		assertTrue(isCompilationSuccessful());
	}

}
