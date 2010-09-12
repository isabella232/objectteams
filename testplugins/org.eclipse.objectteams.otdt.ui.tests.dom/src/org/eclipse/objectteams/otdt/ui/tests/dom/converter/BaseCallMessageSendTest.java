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
 * $Id: BaseCallMessageSendTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.dom.BaseCallMessageSend;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;


/**
 * @author mkr
 * @version $Id: BaseCallMessageSendTest.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class BaseCallMessageSendTest extends FileBasedDOMTest
{
    
    public static final String TEST_PROJECT = "DOM_AST";
    private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

    // Java class used for all within tests
    private TypeDeclaration _role;
    private BaseCallMessageSend _testObj;
    private TypeDeclaration _typeDecl;

    public BaseCallMessageSendTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(BaseCallMessageSendTest.class);
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();
		ICompilationUnit teamClass = getCompilationUnit(
	                                      getTestProjectDir(),
	                                      "src",
	                                      "basecall.teampkg",
	                                      "Team1.java");
		ASTParser parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		parser.setProject(getJavaProject(TEST_PROJECT));
		parser.setSource(teamClass);
        parser.setResolveBindings(true);
        
		ASTNode root = parser.createAST( new NullProgressMonitor() );        
        CompilationUnit compUnit = (CompilationUnit) root;
        _typeDecl = (TypeDeclaration)compUnit.types().get(0);
        _role = _typeDecl.getTypes()[0];
	}

	protected void setUp() throws Exception 
	{
		super.setUp();
	}

    
    public void testInstanceType1()
    {
        MethodDeclaration method = _role.getMethods()[0];
        ExpressionStatement exprStatement =
            (ExpressionStatement)method.getBody().statements().get(0);

        Expression testObj = exprStatement.getExpression();
        
        assertTrue("Base call not an instance of BaseCallMessageSend",
                   testObj instanceof BaseCallMessageSend);
    }

    public void testInstanceType2()
    {
        MethodDeclaration method = _role.getMethods()[2];
        VariableDeclarationStatement varDecl =
            (VariableDeclarationStatement)method.getBody().statements().get(0);
        VariableDeclarationFragment varDeclFrag =
            (VariableDeclarationFragment)varDecl.fragments().get(0);

        Expression testObj = varDeclFrag.getInitializer();
        
        assertTrue("Base call not an instance of BaseCallMessageSend",
                   testObj instanceof BaseCallMessageSend);
    }

    
    
    public void testGetNodeType1()
    {
        MethodDeclaration method = _role.getMethods()[0];
        ExpressionStatement exprStatement =
            (ExpressionStatement)method.getBody().statements().get(0);
        _testObj = (BaseCallMessageSend)exprStatement.getExpression();
        
        int actual = _testObj.getNodeType();
        
        assertEquals("Base Call has wrong node type",
                     ASTNode.BASE_CALL_MESSAGE_SEND,
                     actual);
    }
    
    public void testGetName1()
    {
        MethodDeclaration method = _role.getMethods()[0];
        ExpressionStatement exprStatement =
            (ExpressionStatement)method.getBody().statements().get(0);
        _testObj = (BaseCallMessageSend)exprStatement.getExpression();

        String actual = _testObj.getName().getIdentifier();
        
        assertEquals("Base call has wrong name ",
                     "roleMethod0",
                     actual);
    }
    
    public void testGetArguments_2Args()
    {
        MethodDeclaration method = _role.getMethods()[0];
        ExpressionStatement exprStatement =
            (ExpressionStatement)method.getBody().statements().get(0);
        _testObj = (BaseCallMessageSend)exprStatement.getExpression();
        
        List actual = _testObj.getArguments();
        
        assertEquals("Base call has wrong number of arguments",
                     2,
                     actual.size());
    }

    public void testGetArguments_NoArgs()
    {
        MethodDeclaration method = _role.getMethods()[1];
        ExpressionStatement exprStatement =
            (ExpressionStatement)method.getBody().statements().get(0);
        _testObj = (BaseCallMessageSend)exprStatement.getExpression();
        
        List actual = _testObj.getArguments();
        
        assertEquals("Base call has wrong number of arguments",
                     0,
                     actual.size());
    }
    
    
    public void testChildNodesHaveCorrectParent1()
    {
        MethodDeclaration constructor = _role.getMethods()[1];
        ExpressionStatement statement =
            (ExpressionStatement)constructor.getBody().statements().get(0);
        _testObj = (BaseCallMessageSend)statement.getExpression();
        
        List childNodes = _testObj.getArguments();

        for (Iterator iter = childNodes.iterator(); iter.hasNext();) 
        {
            Expression curChild = (Expression) iter.next();
            assertEquals("Base call arguments have wrong parent node",
                         _testObj,
                         curChild.getParent());
        }
    }
     
    public void testChildNodesHaveCorrectParent2()
    {
        MethodDeclaration method = _role.getMethods()[0];
        ExpressionStatement exprStatement =
            (ExpressionStatement)method.getBody().statements().get(0);
        _testObj = (BaseCallMessageSend)exprStatement.getExpression();
        
        SimpleName childNode = _testObj.getName();
        
        assertEquals("Base call selector has wrong parent",
                     _testObj,
                     childNode.getParent());
    }
    
    public void testSubtreeMatch1()
    {
        MethodDeclaration method = _role.getMethods()[0];
        ExpressionStatement exprStatement =
            (ExpressionStatement)method.getBody().statements().get(0);
        _testObj = (BaseCallMessageSend)exprStatement.getExpression();
        
        boolean actual = _testObj.subtreeMatch(new ASTMatcher(), _testObj);

        assertTrue("Base call message sends don't match", actual);
    }
    
    public void testToString1()
    {
        MethodDeclaration method = _role.getMethods()[0];
        ExpressionStatement exprStatement =
            (ExpressionStatement)method.getBody().statements().get(0);
        _testObj = (BaseCallMessageSend)exprStatement.getExpression();
        
        String actual = _testObj.toString();
        String expected = "base.roleMethod0(arg0, arg1)";
        
        assertEquals("Base call message send: wrong naive flat string representation",
                      expected, actual);
    }
    
    public void testResolveMethodBinding()
    {
        MethodDeclaration method = _role.getMethods()[0];
        ExpressionStatement exprStatement =
            (ExpressionStatement)method.getBody().statements().get(0);

        _testObj = (BaseCallMessageSend)exprStatement.getExpression();
        
        // Note(SH): base call message send no longer resolves to the enclosing method
        //IMethodBinding expected = method.resolveBinding();
        IMethodBinding actual = _testObj.resolveMethodBinding();
        
        assertNotNull(actual);
        //Not correct: assertEquals(expected, actual);
        //Not implemented: assertTrue(actual.isBaseCallSurrogate());
    }

//    public void testResolveReferencedBaseMethodBinding()
//  mkr: either remove test or implement resolve binding.
//  Note(SH): base call does NOT resolve to a base method.
    
    public void testNoOtherStatement() {
        MethodDeclaration method = _role.getMethods()[0];

        assertEquals("Body should have exactly one statement", 1, method.getBody().statements().size());
    }
    
}
