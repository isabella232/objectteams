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
 * $Id: PrecedenceDeclarationTest.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom.converter;

import java.util.List;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PrecedenceDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteFlattener;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;

@SuppressWarnings("restriction")
public class PrecedenceDeclarationTest extends FileBasedDOMTest
{
	    public static final String TEST_PROJECT = "DOM_AST";
	    
		private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

		private ASTParser _parser;
		private ICompilationUnit _simpleTeam;

	    // Java class used for all within tests
		private CompilationUnit _cu;
		private TypeDeclaration _typeDecl;

	    public PrecedenceDeclarationTest(String name)
		{
			super(name);
		}
		
		public static Test suite()
		{
			return new Suite(PrecedenceDeclarationTest.class);
		}
		
		public void setUpSuite() throws Exception
		{
			setTestProjectDir(TEST_PROJECT);
			super.setUpSuite();
			_simpleTeam = getCompilationUnit(
		            getTestProjectDir(),
		            "src",
		            "precedences.teampkg",
		            "Team2.java");

	        _parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
			_parser.setProject( getJavaProject(TEST_PROJECT) );
			_parser.setSource(_simpleTeam);
			
	        ASTNode root = _parser.createAST( new NullProgressMonitor() );
			_cu = (CompilationUnit) root;
			_typeDecl = (TypeDeclaration)_cu.types().get(0);
		}

		protected void setUp() throws Exception 
		{
			super.setUp();
		}

		public void testPrecedencesInRole() {
			TypeDeclaration[] types = _typeDecl.getTypes();
	        TypeDeclaration role1 = types[0];
	        List precedences = role1.precedences();
	        assertFalse("precedences are non-null", precedences == null);
	        assertTrue("Role1 has 3 precedence lists", precedences.size() == 3);
	        
	        PrecedenceDeclaration prec;
	        prec = (PrecedenceDeclaration)precedences.get(0);
	        List elements = prec.elements();
	        assertFalse("1. list is non-null", elements == null);
			assertTrue("1. list has 2 elements", elements.size() == 2);
			assertEquals("expecting element", elements.get(0).toString(), "callin1");
			assertEquals("expecting element", elements.get(1).toString(), "callin2");
			assertFalse("has 'after' keyword", prec.isAfter());

			prec = (PrecedenceDeclaration)precedences.get(1);
	        elements = prec.elements();
	        assertFalse("2. list is non-null", elements == null);
			assertTrue("2. list has 2 elements", elements.size() == 2);
			assertEquals("expecting element", elements.get(0).toString(), "callin3");
			assertEquals("expecting element", elements.get(1).toString(), "callin2");
			assertFalse("has 'after' keyword", prec.isAfter());

			prec = (PrecedenceDeclaration)precedences.get(2);
	        elements = prec.elements();
	        assertFalse("3 list is non-null", elements == null);
			assertTrue("3. list has 2 elements", elements.size() == 2);
			assertEquals("expecting element", elements.get(0).toString(), "callinA3");
			assertEquals("expecting element", elements.get(1).toString(), "callinA2");
			assertTrue("has 'after' keyword", prec.isAfter());
		}

		public void testPrecedencesInTeam() {
	        List precedences = _typeDecl.precedences();
	        assertFalse("precedences are non-null", precedences == null);
	        assertTrue("Team2 has 2 precedence lists", precedences.size() == 2);
	        
	        PrecedenceDeclaration prec;
	        prec = (PrecedenceDeclaration)precedences.get(0);
	        List elements = prec.elements();
	        assertFalse("1. list is non-null", elements == null);
			assertTrue("1. list has 2 elements", elements.size() == 2);
			assertEquals("expecting element", elements.get(0).toString(), "Role1.callin2");
			assertEquals("expecting element", elements.get(1).toString(), "Role1.callin1");
		}

		public void testRolePrecedences() {
	        List precedences = _typeDecl.precedences();
	        assertFalse("precedences are non-null", precedences == null);
	        assertTrue("Team2 has 2 precedence lists", precedences.size() == 2);
	        
	        PrecedenceDeclaration prec;
	        prec = (PrecedenceDeclaration)precedences.get(1);
	        List elements = prec.elements();
	        assertFalse("2. list is non-null", elements == null);
			assertTrue("2. list has 2 elements", elements.size() == 2);
			assertEquals("expecting element", elements.get(0).toString(), "Role2");
			assertEquals("expecting element", elements.get(1).toString(), "Role1");
		}
		
		public void testPrecedenceFlattening()  {
		    ASTRewriteFlattener _rewriteFlattener = new ASTRewriteFlattener(new RewriteEventStore());
	        _cu.accept(_rewriteFlattener);

	        String actual = _rewriteFlattener.getResult();
	        String expected = 
	        	"package precedences.teampkg;"+
	        	"import precedences.basepkg.MyClass;"+
				"public team class Team1 {"+
					"public class Role1 playedBy MyClass {"+
						"roleMethod0 <- before baseMethod1;"+
						"callin1: roleMethod1 <- before baseMethod2;"+
						"callin2: roleMethod2 <- before baseMethod2,baseMethod4,baseMethod5;"+
						"callin3: roleMethod3 <- before baseMethod0,baseMethod4;"+
				        "callinA2: roleMethod2 <- after baseMethod2;"+
				        "callinA3: roleMethod3 <- after baseMethod3;"+
						"public void roleMethod0(){}"+
						"public void roleMethod1(){}"+
						"public void roleMethod2(){}"+
						"public void roleMethod3(){}"+
						"precedence callin1, callin2;"+
						"precedence callin3, callin2;" +
						"precedence after callinA3, callinA2;"+
					"}"+
					"public class Role2 {}"+	
					"precedence Role1.callin2, Role1.callin1;"+
					"precedence Role2, Role1;"+
				"}";
	        
	        assertEquals("Wrong CU-Code", expected, actual);
		}
}
