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
 * $Id: TSuperMessageSendTest.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom.converter;


import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TSuperMessageSend;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;


/**
 * @author mkr
 * @version $Id: TSuperMessageSendTest.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class TSuperMessageSendTest extends FileBasedDOMTest
{
    public static final String TEST_PROJECT = "DOM_AST";
    private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

    /** Java class used for all within tests */
    private TypeDeclaration _role;
    private MethodDeclaration _roleMethod0;
    private MethodDeclaration _roleMethod1;
    private TSuperMessageSend _testObj;
    private TypeDeclaration _typeDecl;

    public TSuperMessageSendTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(TSuperMessageSendTest.class);
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();
		ICompilationUnit _teamClass = getCompilationUnit(
	                                      getTestProjectDir(),
	                                      "src",
	                                      "tsupercall.teampkg",
	                                      "Team1.java");
		ASTParser parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		parser.setProject( getJavaProject(TEST_PROJECT) );
		parser.setSource(_teamClass);
        parser.setResolveBindings(true);
        
		ASTNode root = parser.createAST( new NullProgressMonitor() );        
        CompilationUnit compUnit = (CompilationUnit) root;
        _typeDecl = (TypeDeclaration)compUnit.types().get(0);
        _role = _typeDecl.getTypes()[0];
        _roleMethod0 = _role.getMethods()[2];
        _roleMethod1 = _role.getMethods()[3];
	}

	protected void setUp() throws Exception 
	{
		super.setUp();
	}

    
    public void testInstanceType1()
    {
        ExpressionStatement exprStatement =
            (ExpressionStatement)_roleMethod0.getBody().statements().get(0);

        Expression testObj = exprStatement.getExpression();
        
        assertTrue("tsuper call not an instance of TSuperMessageSend",
                   testObj instanceof TSuperMessageSend);
    }

    public void testInstanceType2()
    {
        ExpressionStatement exprStatement =
            (ExpressionStatement)_roleMethod1.getBody().statements().get(0);

        Expression testObj = exprStatement.getExpression();
        
        assertTrue("tsuper call not an instance of TSuperMessageSend",
                   testObj instanceof TSuperMessageSend);
    }

    public void testNodeType1()
    {
        ExpressionStatement exprStatement =
            (ExpressionStatement)_roleMethod0.getBody().statements().get(0);
        _testObj = (TSuperMessageSend)exprStatement.getExpression();
        
        int actual = _testObj.getNodeType();
        
        assertEquals("tsuper call has wrong node type",
                     ASTNode.TSUPER_MESSAGE_SEND,
                     actual);
    }

    public void testGetName1()
    {
        ExpressionStatement exprStatement =
            (ExpressionStatement)_roleMethod0.getBody().statements().get(0);
        _testObj = (TSuperMessageSend)exprStatement.getExpression();

        String actual = _testObj.getName().getIdentifier();
        
        assertEquals("tsuper call has wrong name ",
                     "method0",
                     actual);
    }
   
    public void testGetArguments2_TwoArgs()
    {
        ExpressionStatement exprStatement =
            (ExpressionStatement)_roleMethod1.getBody().statements().get(0);
        _testObj = (TSuperMessageSend)exprStatement.getExpression();
        
        List actual = _testObj.getArguments();
        
        assertEquals("tsuper call has wrong number of arguments",
                     2,
                     actual.size());
    }

    public void testGetArguments1_NoArgs()
    {
        ExpressionStatement exprStatement =
            (ExpressionStatement)_roleMethod0.getBody().statements().get(0);
        _testObj = (TSuperMessageSend)exprStatement.getExpression();
        
        List actual = _testObj.getArguments();
        
        assertEquals("tsuper call has wrong number of arguments",
                     0,
                     actual.size());
    }

    public void testChildNodesHaveCorrectParent1()
    {
        ExpressionStatement exprStatement =
            (ExpressionStatement)_roleMethod1.getBody().statements().get(0);
        _testObj = (TSuperMessageSend)exprStatement.getExpression();
        
        List childNodes = _testObj.getArguments();

        for (Iterator iter = childNodes.iterator(); iter.hasNext();) 
        {
            Expression curChild = (Expression) iter.next();
            assertEquals("tsuper call arguments have wrong parent node",
                         _testObj,
                         curChild.getParent());
        }

    }
     
    public void testChidlNodeHaveCorrectParrent2()
    {
        ExpressionStatement exprStatement =
            (ExpressionStatement)_roleMethod1.getBody().statements().get(0);
        _testObj = (TSuperMessageSend)exprStatement.getExpression();
        
        SimpleName childNode = _testObj.getName();
        
        assertEquals("tsuper call selector has wrong parent",
                     _testObj,
                     childNode.getParent());
    }

    public void testSubtreeMatch1()
    {
        ExpressionStatement exprStatement =
            (ExpressionStatement)_roleMethod1.getBody().statements().get(0);
        _testObj = (TSuperMessageSend)exprStatement.getExpression();
        
        boolean actual = _testObj.subtreeMatch(new ASTMatcher(), _testObj);

        assertTrue("tsuper call message sends don't match", actual);
 
    }

    public void testToString1()
    {
        ExpressionStatement exprStatement =
            (ExpressionStatement)_roleMethod1.getBody().statements().get(0);
        _testObj = (TSuperMessageSend)exprStatement.getExpression();
        
        String actual = _testObj.toString();
        String expected = "tsuper.method1(arg0, arg1)";
        
        assertEquals("tsuper call message send: wrong naive flat string representation",
                      expected, actual);
    }
    
    public void testResolveBinding()
    {
        ExpressionStatement exprStatement =
            (ExpressionStatement)_roleMethod1.getBody().statements().get(0);
        
        _testObj = (TSuperMessageSend)exprStatement.getExpression();
        
        ITypeBinding itb = _typeDecl.resolveBinding();
        IMethodBinding expected = null;
		// don't access method via index since bindings are sorted by the compiler
        ITypeBinding tsuperRoleClass = null;
        for (ITypeBinding role : itb.getSuperclass().getDeclaredTypes()) {
        	if (role.isClass() && role.getName().equals("Role0")) {
        		tsuperRoleClass = role;
        		break;
        	}
        }
        assertNotNull("tsuper Role not found", tsuperRoleClass);
		for (IMethodBinding tSuperMethod: tsuperRoleClass.getDeclaredMethods()) 
        {
        	if (tSuperMethod.getName().equals("method1")) {
        		expected = tSuperMethod;
        		break;
        	}        		
        }
        
        IMethodBinding actual = _testObj.resolveMethodBinding();
        
        assertEquals(expected, actual);
    }
    
}