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
 * $Id: BaseConstructorInvocationTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.dom.BaseConstructorInvocation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;


/**
 * @author mkr
 * @version $Id: BaseConstructorInvocationTest.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class BaseConstructorInvocationTest extends FileBasedDOMTest
{
    public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

    private TypeDeclaration _typeDecl;
	private TypeDeclaration _role;
	
    private BaseConstructorInvocation _testObj;

    public BaseConstructorInvocationTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(BaseConstructorInvocationTest.class);
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();

        ICompilationUnit _teamClass = getCompilationUnit(
                                          getTestProjectDir(),
	                                      "src",
	                                      "baseconstructor.teampkg",
	                                      "Team1.java");
        
		ASTParser parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
        parser.setResolveBindings(true);
		parser.setProject( getJavaProject(TEST_PROJECT) );
		parser.setSource(_teamClass);
		
        ASTNode root = parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit)root;
		_typeDecl = (TypeDeclaration)compUnit.types().get(0);

        _role = _typeDecl.getTypes()[0];
    }

	protected void setUp() throws Exception 
	{
		super.setUp();
	}

    public void testInstanceType1()
    {
        MethodDeclaration constructor = _role.getMethods()[0];

        Statement testObj = (Statement)constructor.getBody().statements().get(0);

        assertTrue("Template for tests, always true",
                   testObj instanceof BaseConstructorInvocation);
    }

    public void testGetNodeType1()
    {
        MethodDeclaration constructor = _role.getMethods()[0];

        _testObj = (BaseConstructorInvocation)constructor.getBody().statements().get(0);

        int actual = _testObj.getNodeType();
        
        assertEquals("BaseConstructorMessageSend has wrong node type",
                     ASTNode.BASE_CONSTRUCTOR_INVOCATION,
                     actual);
    }

    public void testGetArguments_NoArgs()
    {
        MethodDeclaration constructor = _role.getMethods()[0];
        _testObj = (BaseConstructorInvocation)constructor.getBody().statements().get(0);
        
        List actual = _testObj.getArguments();
        
        assertEquals("BaseConstructorMessageSend has wrong number of arguments",
                     0,
                     actual.size());
    }

    public void testGetArguments_2Args()
    {
        RoleTypeDeclaration role = _typeDecl.getRoles()[0];
        MethodDeclaration constructor = role.getMethods()[1];
        _testObj = (BaseConstructorInvocation)constructor.getBody().statements().get(0);
        
        List actual = _testObj.getArguments();
        
        assertEquals("BaseConstructorMessageSend has wrong number of arguments",
                     2,
                     actual.size());
    }

    
    public void testChildNodesHaveCorrectParent1()
    {
        MethodDeclaration constructor = _role.getMethods()[1];
        _testObj = (BaseConstructorInvocation) constructor.getBody().statements().get(0);
        
        List childNodes = _testObj.getArguments();

        for (Iterator iter = childNodes.iterator(); iter.hasNext();) 
        {
            Expression curChild = (Expression) iter.next();
            assertEquals("Base call arguments have wrong parent node",
                         _testObj,
                         curChild.getParent());
        }
    }
    
    public void testSubtreeMatch1()
    {
        MethodDeclaration constructor = _role.getMethods()[1];
        _testObj = (BaseConstructorInvocation)constructor.getBody().statements().get(0);
        
        boolean actual = _testObj.subtreeMatch(new ASTMatcher(), _testObj);

        assertTrue("Base constructor calls don't match", actual);
        
    }

    public void testtoString1()
    {
        MethodDeclaration constructor = _role.getMethods()[1];
        _testObj = (BaseConstructorInvocation)constructor.getBody().statements().get(0);
        
        String actual = _testObj.toString();
        String expected = "base(dummy0, dummy1);";

        assertEquals("Base constructor call has wrong naive flat string representation",
                expected, actual);
    }
    
    public void testResolveConstructorBinding()
    {
        RoleTypeDeclaration role = _typeDecl.getRoles()[0];
        MethodDeclaration constructor = role.getMethods()[1];

        _testObj = (BaseConstructorInvocation)constructor.getBody().statements().get(0);

        ITypeBinding baseClass = role.resolveBinding().getBaseClass();
        IMethodBinding baseConstructorBinding = baseClass.getDeclaredMethods()[1];
        
        IMethodBinding expected = baseConstructorBinding;
        IMethodBinding actual = _testObj.resolveConstructorBinding();
        
        assertNotNull(actual);
        assertEquals(expected, actual);
    }
}
