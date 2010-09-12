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
 * $Id: ParserTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.errorreporting;

import org.eclipse.objectteams.otdt.tests.compiler.TestBase;

/**
 * This class contains tests concerning the parser.
 *
 * @author brcan
 * @version $Id: ParserTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class ParserTest extends TestBase
{
    public ParserTest(String testName)
    {
        super(testName);
    }

    /**
	 * A test consisting of a main class, a base class, a team with two
	 * methods (team-level methods) and a role which has a callin method
	 * and a callin + callout binding.
	 */
// TODO(jwl): What is the gist we are actually testing here?
    public void testParser()
	{
		createFile("Main",
			"public class Main"
			+ NL + "{"
			+ NL + "    public static void main(String[] args)"
			+ NL + "    {"
			+ NL + "        MyClass baseObj      = new MyClass();"
			+ NL + "        MyTeam  teamInstance = new MyTeam();"    	
			+ NL + "        within(teamInstance)"
			+ NL + "    	   {"
			+ NL + "            System.out.println();"
			+ NL + "            System.out.println(\" Main.main --> MyTeam.whisper Hello \");"
			+ NL + "            teamInstance.whisper(baseObj, \"Hello\");"
			+ NL + "        }"
			+ NL + "    }"
			+ NL + "}");
						
		createFile("MyClass",
			"public class MyClass	"
		    + NL + "{"
		    + NL + "    public void say(String msg)"
		    + NL + "    {"
		    + NL + "        System.out.println(\" MyBase says : \" + msg);"
		    + NL + "    }"    
		    + NL + "    public void whisper(String msg)"
		    + NL + "    {"
		    + NL + "        System.out.println(\" MyBase whispers : \" + msg);"
		    + NL + "    }"
		    + NL + "}");						
				
		createFile("MyTeam",
			"public team class MyTeam"
		    + NL + "{"
		    + NL + "    public class MyRole playedBy MyClass"
		    + NL + "    {"
		    + NL + "        callin void say(String msg)"
		    + NL + "        {"
		    + NL + "            System.out.println(\" MyRole says : \" + msg);"
		    + NL + "            base.say(msg);"
		    + NL + "        }"
		    + NL + "        say <- replace whisper;"
		    + NL + "        public abstract void whisper(String msg);"
		    + NL + "        whisper -> whisper;"
		    + NL + "    }"
		    + NL + "    public void say(String msg)"
		    + NL + "    {"
		    + NL + "        System.out.println(\" MyTeam says : \" + msg);"
		    + NL + "    }"
		    + NL + "    public void whisper(MyClass as MyRole role, String msg)"
		    + NL + "	   {"
		    + NL + "        System.out.println(\" MyTeam whispers : \" + msg );"
		    + NL + "        role.whisper(msg);"
		    + NL + "    }"
		    + NL + "}");
		
		compileFile("Main");

		assertTrue(isCompilationSuccessful());
	}
    
    public void testScopedKeywordJavadoc()
	{
		createFile("Main",
			"public class Main"
			+ NL + "{"
			+ NL + "    /**"
			+ NL + "     * @param base an input"
			+ NL + "     */"
			+ NL + "    public void foo(String base)"
			+ NL + "    {"
			+ NL + "        System.out.println(base);"
			+ NL + "    }"
			+ NL + "}");
						

		
		compileFile("Main");

		assertTrue(isCompilationSuccessful());
	}
}
