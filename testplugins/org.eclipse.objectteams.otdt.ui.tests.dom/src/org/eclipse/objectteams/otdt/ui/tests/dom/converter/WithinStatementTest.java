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
 * $Id: WithinStatementTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WithinStatement;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;


/**
 * @author mkr
 * @version $Id: WithinStatementTest.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class WithinStatementTest extends FileBasedDOMTest
{
    public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

	private ASTParser _parser;
	private ICompilationUnit _simpleClass;
    /** Java class used for all within tests */
	private TypeDeclaration _typeDecl;
    private WithinStatement _testObj;

    public WithinStatementTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(WithinStatementTest.class);
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();
		_simpleClass = getCompilationUnit(
	            getTestProjectDir(),
	            "src",
	            "withinstatement.basepkg",
	            "MyClass.java");

		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject( getJavaProject(TEST_PROJECT) );
		_parser.setSource(_simpleClass);

        ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
		_typeDecl = (TypeDeclaration)compUnit.types().get(0);
	}

	protected void setUp() throws Exception 
	{
		super.setUp();
	}


    public void testInstanceType()
    {
        MethodDeclaration methodDecl =
            WithinStatementTest.getMethodDeclarationByName(_typeDecl,
                                                           "withinSimple");
        List statements = methodDecl.getBody().statements();
        
        Statement testObj = (Statement) statements.get(0);
        assertTrue("Statement is not an instance of WithinStatement",
                   testObj instanceof WithinStatement);
    }

    
	public void testGetNodeType()
	{
		MethodDeclaration methodDecl =
            WithinStatementTest.getMethodDeclarationByName(_typeDecl,
                                                           "withinSimple");
		List statements = methodDecl.getBody().statements();
				
		_testObj = (WithinStatement) statements.get(0);
	
        assertEquals(
            "WithinStatement has wrong NodeType",
            ASTNode.WITHIN_STATEMENT, 
            _testObj.getNodeType());
	}
	
   public void testGetBody_Empty()
   {
		MethodDeclaration methodDecl =
            WithinStatementTest.getMethodDeclarationByName(_typeDecl,
                                                           "withinSimpleEmptyBody");
		List statements = methodDecl.getBody().statements();
		Statement firstStatement = (Statement) statements.get(0);
		
		_testObj = (WithinStatement) firstStatement;

        assertTrue(
            "withing: body block is not empty",
            ((Block)_testObj.getBody()).statements().isEmpty());
   }
	
	public void testGetBody_NotEmpty()
    {
		MethodDeclaration methodDecl =
            WithinStatementTest.getMethodDeclarationByName(_typeDecl,
                                                              "withinSimple");
		List statements = methodDecl.getBody().statements();
		Statement firstStatement = (Statement) statements.get(0);

		_testObj = (WithinStatement) firstStatement;

        assertEquals("within: body block is empty",
                    1, ((Block)_testObj.getBody()).statements().size());
	}
    
    public void testBody_ParentIdentity()
    {
        MethodDeclaration methodDecl = WithinStatementTest.getMethodDeclarationByName(_typeDecl, "withinSimple");
        List statements = methodDecl.getBody().statements();
        Statement firstStatement = (Statement) statements.get(0);

        _testObj = (WithinStatement) firstStatement;
        Block body = (Block)_testObj.getBody();
        ASTNode bodyParent = body.getParent();
        
        assertTrue("within: ", _testObj == bodyParent );
  
    }
    
    /** Tests if body belong to the same AST as the within statement */
    public void testBody_ASTIdentity()
    {
        MethodDeclaration methodDecl = WithinStatementTest.getMethodDeclarationByName(_typeDecl, "withinSimple");
        List statements = methodDecl.getBody().statements();
        Statement firstStatement = (Statement) statements.get(0);

        _testObj = (WithinStatement) firstStatement;
        Block body = (Block)_testObj.getBody();

        assertTrue(
            "within: body (Block) doesn't belong to AST", 
            _testObj.getAST() == body.getAST() );
    }

    /** Tests if statements in body belong to the same AST as the within statement */
    public void testStatements_ASTIdentity()
    {
        MethodDeclaration methodDecl = WithinStatementTest.getMethodDeclarationByName(_typeDecl, "withinSimple");
        List statements = methodDecl.getBody().statements();
        Statement firstStatement = (Statement) statements.get(0);

        _testObj = (WithinStatement) firstStatement;
        Block body = (Block)_testObj.getBody();
        Statement bodyStatement = (Statement)body.statements().get(0);

        assertTrue("within: statements don't belong to AST",
                   _testObj.getAST() == bodyStatement.getAST() );
    }

    /** Tests if team expression belongs to the same AST as the within statement */
    public void testTeamExpression_ASTIdentity()
    {
        MethodDeclaration methodDecl = WithinStatementTest.getMethodDeclarationByName(_typeDecl, "withinSimple");
        List statements = methodDecl.getBody().statements();
        Statement firstStatement = (Statement) statements.get(0);

        _testObj = (WithinStatement) firstStatement;

        assertTrue("within: team expression doesn't belong to AST",
                   _testObj.getAST() == _testObj.getTeamExpression().getAST());
    }

    public void testGetTeamExpression1_InstanceType()
    {
        MethodDeclaration methodDecl = WithinStatementTest.getMethodDeclarationByName(_typeDecl, "withinNewTeam");
        List statements = methodDecl.getBody().statements();
        Statement firstStatement = (Statement)statements.get(0);

        _testObj = (WithinStatement) firstStatement;

        assertTrue("within: Team expression is not a ClassInstanceCreation",
                   _testObj.getTeamExpression() instanceof ClassInstanceCreation);
    }
   
    public void testGetTeamExpression2_InstanceType()
    {
        MethodDeclaration methodDecl = WithinStatementTest.getMethodDeclarationByName(_typeDecl, "withinSimple");
        List statements = methodDecl.getBody().statements();
        Statement firstStatement = (Statement)statements.get(0);

        _testObj = (WithinStatement) firstStatement;

        assertTrue("within: Team expression is not a SimpleName",
                   _testObj.getTeamExpression() instanceof SimpleName);
    }

    public void testGetTeamExpression3_InstanceType()
    {
        MethodDeclaration methodDecl = WithinStatementTest.getMethodDeclarationByName(_typeDecl, "withinViaMethod");
        List statements = methodDecl.getBody().statements();
        Statement firstStatement = (Statement)statements.get(0);

        _testObj = (WithinStatement) firstStatement;
        
        assertTrue("within: Team expression is not a SimpleName",
                   _testObj.getTeamExpression() instanceof MethodInvocation);
    }
    
    public void testGetTeamExpression_Parent()
    {
        MethodDeclaration methodDecl = WithinStatementTest.getMethodDeclarationByName(_typeDecl, "withinNewTeam");
        List statements = methodDecl.getBody().statements();
        Statement firstStatement = (Statement)statements.get(0);

        _testObj = (WithinStatement) firstStatement;
        ASTNode teamExpressionParent = _testObj.getTeamExpression().getParent();

        assertTrue("within: Team expression is not a SimpleName",
                   _testObj == teamExpressionParent);
    }

    public void testGetTeamExpression_Identifier1()
    {
        MethodDeclaration methodDecl = WithinStatementTest.getMethodDeclarationByName(_typeDecl, "withinNewTeam");
        List statements = methodDecl.getBody().statements();
        Statement firstStatement = (Statement)statements.get(0);

        _testObj = (WithinStatement) firstStatement;
        ClassInstanceCreation teamExpression = (ClassInstanceCreation)_testObj.getTeamExpression();
        
        assertEquals("within: Team expression is not a SimpleName",
                   "new Team1()", teamExpression.toString());
    }
    
    public void testGetTeamExpression_Identifier2()
    {
        MethodDeclaration methodDecl = WithinStatementTest.getMethodDeclarationByName(_typeDecl, "withinSimple");
        List statements = methodDecl.getBody().statements();
        Statement firstStatement = (Statement)statements.get(0);

        _testObj = (WithinStatement) firstStatement;
        SimpleName teamExpression = (SimpleName)_testObj.getTeamExpression();
        
        assertEquals("within: Team expression is not a SimpleName",
                   "myTeam", teamExpression.toString());
    }

    public void testToString()
    {
        MethodDeclaration methodDecl =
            WithinStatementTest.getMethodDeclarationByName(_typeDecl,
                                                              "withinNewTeam");
        List statements = methodDecl.getBody().statements();
        _testObj = (WithinStatement) statements.get(0);

        String actual = _testObj.toString();
        String expected = 
        	"within(new Team1()) {\n"+
        	"  foo();\n"+
        	"}\n";
        
        assertEquals(
            "within statement: wrong naive flat string representation",
            expected, 
            actual);
    }
    
    public void testSubtreeMatch1()
    {
        MethodDeclaration methodDecl = WithinStatementTest.getMethodDeclarationByName(_typeDecl, "withinNewTeam");
        List statements = methodDecl.getBody().statements();
        Statement first = (Statement)statements.get(0);
        _testObj = (WithinStatement) first;

        boolean actual = _testObj.subtreeMatch(new ASTMatcher(), _testObj);

        assertTrue("Within statements don't match", actual);
    }
    
	/**
     * Helper method to find the named MethodDeclaration
     * in the given TypeDeclaration.
     * 
     * @return the found MethodDeclaration or null
     */
	private static MethodDeclaration getMethodDeclarationByName(TypeDeclaration type, String name)
    {
		MethodDeclaration[] methods = type.getMethods();
		int numberOfMethods = methods.length;
		
        if (numberOfMethods == 0)
		{
			return null;
		}
		
        for (int idx = 0; idx < numberOfMethods; idx++)
		{
			if (name.equals(methods[idx].getName().getIdentifier()))
			{
				return methods[idx];
			}
			
		}
		
        return null; // method not found
	};

}
