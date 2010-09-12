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
 * $Id: ParameterMappingCalloutTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;

/**
 * @author ike
 *
 * Tests the DOM-astNode parameterMapping.
 * 
 */
public class ParameterMappingCalloutTest extends FileBasedDOMTest 
{
    public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

	private ASTParser _parser;
	private ICompilationUnit _simpleTeam;
	private TypeDeclaration _typeDecl;
    private TypeDeclaration _role;
	
	public ParameterMappingCalloutTest(String name) 
	{
		super(name);
	}

	public static Test suite()
	{
		return new Suite(ParameterMappingCalloutTest.class);
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();
	}

	protected void setUp() throws Exception 
	{
		super.setUp();
		_simpleTeam = getCompilationUnit(
	            getTestProjectDir(),
	            "src",
	            "parameterMapping.teampkg",
	            "MyTeam.java");
		
		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject(super.getJavaProject(TEST_PROJECT));
		_parser.setSource(_simpleTeam);
		
		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
		_typeDecl = (TypeDeclaration)compUnit.types().get(0);
        _role = _typeDecl.getTypes()[0];
	}
	
	//test is for first calloutMapping in Testdata (Role)
	public void testGetParameterMappings_notEmpty()
	{
		//get first role
		CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(4);

		List mappings = calloutDecl.getParameterMappings();
		
		assertNotNull("ParametermappingList should be not empty", mappings);
		assertFalse(mappings.isEmpty());
	}		
	
	//test is for first calloutMapping in Testdata (Role)
	public void testGetDirection()
	{
		CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(4);
		List parameterMappings = calloutDecl.getParameterMappings();
        
		ParameterMapping testObj = (ParameterMapping)parameterMappings.get(0);
		String actual = testObj.getDirection();
		String expected = "->";
		
		assertEquals("Wrong direction", expected, actual);
	}
	
	//test is for first calloutMapping in Testdata (Role)
	public void testGetIdentifier()
	{
		CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(4);
		List parameterMappings = calloutDecl.getParameterMappings();
        
		ParameterMapping testObj = (ParameterMapping)parameterMappings.get(0);
		String actual = testObj.getIdentifier().getIdentifier();
		String expected = "val";
		
		assertEquals("Identifier is wrong", expected, actual);
	}

	//test is for first calloutMapping in Testdata (Role)
	public void testGetExpression_InstanceType1()
	{
		CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(4);
		List parameterMappings = calloutDecl.getParameterMappings();
        
        ParameterMapping testObj = (ParameterMapping)parameterMappings.get(0);
		Expression actual = (Expression)testObj.getExpression();
		
		assertTrue(
            "Wrong Type of given expression, type is " + actual.getClass(), 
            actual instanceof MethodInvocation);
	}	
	
	//test is for second calloutMapping in Testdata (Role)
	public void testGetExpression_InstanceType2()
	{
		CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(5);
		List parameterMappings = calloutDecl.getParameterMappings();
        
		ParameterMapping testObj = (ParameterMapping)parameterMappings.get(0);
		Expression actual = (Expression)testObj.getExpression();
		
		assertTrue(
            "Wrong Type of given expression, type is " + actual.getClass(), 
            actual instanceof ClassInstanceCreation);
	}	

	//test is for fourth calloutMapping in Testdata (Role)
	public void testGetExpression_InstanceType3()
	{
		CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(7);
		List parameterMappings = calloutDecl.getParameterMappings();
	
        ParameterMapping testObj = (ParameterMapping)parameterMappings.get(0);
        Expression actual = (Expression)testObj.getExpression();
		
		assertTrue(
            "Wrong Type of given expression, type is  " + actual.getClass(), 
            actual instanceof SimpleName);
	}	
	
	//test is for third calloutMapping in Testdata (Role)
	public void testValidMappingListSize()
	{
		CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(6);
		int actual = calloutDecl.getParameterMappings().size();
		int expected = 2;

		assertEquals("ParameterMappingList has wrong size", expected, actual);
	}

    //test is for first calloutMapping in Testdata (Role)
    public void testHasResultFlag_false()
    {
        CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(4);
        List parameterMappings = calloutDecl.getParameterMappings();
        
        ParameterMapping testObj = (ParameterMapping)parameterMappings.get(0);
        boolean actual = testObj.hasResultFlag();
        
        assertFalse("Mapping is a not a result mapping", actual);
    }

	//test is for third calloutMapping in Testdata (Role)
	public void testHasResultFlag_true1()
	{
		CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(6);
		List parameterMappings = calloutDecl.getParameterMappings();
		
		//test the second parameterMapping of this CalloutMappingDeclaration
		ParameterMapping testObj = (ParameterMapping)parameterMappings.get(1);
		boolean actual = testObj.hasResultFlag();
		
		assertTrue("Mapping is a result mapping, but was not detected", actual);
	}

    //test is for fourth calloutMapping in Testdata (Role)
    public void testHasResultFlag_true2()
    {
        CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(7);
        List parameterMappings = calloutDecl.getParameterMappings();
        
        //test the third parameterMapping of this CalloutMappingDeclaration
        ParameterMapping testObj = (ParameterMapping)parameterMappings.get(2);
        boolean actual = testObj.hasResultFlag();
        
        assertTrue("Mapping is a result mapping, but was not detected", actual);
    }       
	//test is for fourth calloutMapping in Testdata (Role)
	public void testValidMappingListSize1()
	{
		CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(7);
		int actual = calloutDecl.getParameterMappings().size();
		int expected = 3;

		assertEquals("ParameterMappingList has wrong size", expected, actual);
	}	
    
    //test is for fourth calloutMapping in Testdata (Role)
    public void testSubTreeMatch()
    {
        CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(7);
        List parameterMappings = calloutDecl.getParameterMappings();
        ParameterMapping testObj = (ParameterMapping)parameterMappings.get(2);
        
        boolean actual = testObj.subtreeMatch(new ASTMatcher(), testObj);

        assertTrue("Both nodes are equal, even the same.", actual);
    } 

	public void testCopySubtree()
	{
        CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration)_role.bodyDeclarations().get(7);
        List parameterMappings = calloutDecl.getParameterMappings();
        ParameterMapping testObj = (ParameterMapping)parameterMappings.get(2);

	    ParameterMapping clonedTestObject = 
			(ParameterMapping)ASTNode.copySubtree(AST.newAST(AST.JLS3), testObj);
		boolean actual = testObj.subtreeMatch(new ASTMatcher(), clonedTestObject);

        assertTrue("Copy of subtree not correct", actual);
	}
}
