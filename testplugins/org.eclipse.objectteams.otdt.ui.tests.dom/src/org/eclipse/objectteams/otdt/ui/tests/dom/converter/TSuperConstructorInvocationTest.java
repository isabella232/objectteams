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
 * $Id: TSuperConstructorInvocationTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TSuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;


/**
 * @author mkr
 * @version $Id: TSuperConstructorInvocationTest.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class TSuperConstructorInvocationTest extends FileBasedDOMTest
{
    public static final String TEST_PROJECT = "DOM_AST";
    private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

    /** Java class used for all within tests */
    private TypeDeclaration _role;
    private TypeDeclaration _typeDecl;
    private MethodDeclaration _constructor0;
    private MethodDeclaration _constructor1;
    private Statement _tsuper0;
    private Statement _tsuper1;
    
    
    private TSuperConstructorInvocation _testObj;

    public TSuperConstructorInvocationTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(TSuperConstructorInvocationTest.class);
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
        _constructor0 = _role.getMethods()[0];
        _constructor1 = _role.getMethods()[1];
        _tsuper0 = (Statement)_constructor0.getBody().statements().get(0);
        _tsuper1 = (Statement)_constructor1.getBody().statements().get(0);

	}

	protected void setUp() throws Exception 
	{
		super.setUp();
	}

    
    public void testInstanceType1()
    {
        assertTrue("tsuper call not an instance of TSuperConstructorInvocation",
                   _tsuper0 instanceof TSuperConstructorInvocation);
    }

    public void testInstanceType2()
    {
        assertTrue("tsuper call not an instance of TSuperConstructorInvocation",
                   _tsuper1 instanceof TSuperConstructorInvocation);
    }

    public void testNodeType1()
    {
        _testObj = (TSuperConstructorInvocation)_tsuper1;
        
        int actual = _testObj.getNodeType();
        
        assertEquals("tsuper call has wrong node type",
                     ASTNode.TSUPER_CONSTRUCTOR_INVOCATION,
                     actual);
    }

   
    public void testGetArguments2_TwoArgs()
    {
        _testObj = (TSuperConstructorInvocation)_tsuper1;
        
        List actual = _testObj.getArguments();
        
        assertEquals("tsuper call has wrong number of arguments",
                     2,
                     actual.size());
    }

    public void testGetArguments1_NoArgs()
    {
        _testObj = (TSuperConstructorInvocation)_tsuper0;
        
        List actual = _testObj.getArguments();
        
        assertEquals("tsuper call has wrong number of arguments",
                     0,
                     actual.size());
    }

    public void testChildNodesHaveCorrectParent1()
    {
        _testObj = (TSuperConstructorInvocation)_tsuper1;
        
        List childNodes = _testObj.getArguments();

        for (Iterator iter = childNodes.iterator(); iter.hasNext();) 
        {
            Expression curChild = (Expression) iter.next();
            assertEquals("tsuper call arguments have wrong parent node",
                         _testObj,
                         curChild.getParent());
        }

    }
     
    public void testSubtreeMatch1()
    {
        _testObj = (TSuperConstructorInvocation)_tsuper1;
        
        boolean actual = _testObj.subtreeMatch(new ASTMatcher(), _testObj);

        assertTrue("tsuper call message sends don't match", actual);
 
    }

    public void testToString1()
    {
        _testObj = (TSuperConstructorInvocation)_tsuper1;
        
        String actual = _testObj.toString();
        String expected = "tsuper(arg0, arg1)";
        
        assertEquals(
            "tsuper call message send: wrong naive flat string representation",
            expected, 
            actual);
    }
    
    public void testResolveBinding()
    {
        _testObj = (TSuperConstructorInvocation)_tsuper1;
        
        ITypeBinding itb = _typeDecl.resolveBinding();
        ITypeBinding tsuperRoleClass = null;
        for (ITypeBinding role : itb.getSuperclass().getDeclaredTypes()) {
        	if (role.isClass() && role.getName().equals("Role0")) {
        		tsuperRoleClass = role;
        		break;
        	}
        }
        assertNotNull("tsuper Role not found", tsuperRoleClass);
        IMethodBinding tSuperConstructor =
            tsuperRoleClass.getDeclaredMethods()[1];
        
        IMethodBinding expected = tSuperConstructor;
        IMethodBinding actual = _testObj.resolveConstructorBinding();
        assertEquals(expected, actual);
    }
        
    
}