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
 * $Id: LiftingTypeTest.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom.converter;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LiftingType;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;

/**
 * @author jsv
 */
public class LiftingTypeTest extends FileBasedDOMTest
{
    public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

	private ASTParser _parser;
	private ICompilationUnit _simpleTeam;
	
	private TypeDeclaration _typeDecl; // a java class common to all within tests
	private MethodDeclaration _methodDecl;	
	private SingleVariableDeclaration _variableDecl;
	
	private LiftingType _testObj;
	
	public LiftingTypeTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(LiftingTypeTest.class);
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
	            "liftingType.teampkg",
	            "Team1.java");
		
		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject( getJavaProject(TEST_PROJECT) );
		_parser.setSource(_simpleTeam);
		
		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
		_typeDecl = (TypeDeclaration)compUnit.types().get(0);
		_methodDecl = (MethodDeclaration)_typeDecl.bodyDeclarations().get(0);	
		_variableDecl = (SingleVariableDeclaration)_methodDecl.parameters().get(0);
	}

	public void testInstanceType1()
	{
		Type testObj = _variableDecl.getType();
		
		assertTrue(testObj instanceof LiftingType);
	}
	
	public void testSubtreeMatch1()
	{
		_testObj = (LiftingType)_variableDecl.getType();
		
		boolean actual = _testObj.subtreeMatch(new ASTMatcher(), _testObj);
		
		assertTrue("LiftingTypes don't match", actual);
	}
	
	public void testCopySubtree1()
	{
		_testObj = (LiftingType)_variableDecl.getType();		
		LiftingType clonedTestObject = 
			(LiftingType)ASTNode.copySubtree(AST.newAST(AST.JLS3), _testObj);
		
		boolean actual = _testObj.subtreeMatch(new ASTMatcher(), clonedTestObject);

        assertTrue("Copy of subtree not correct", actual);
	}
	
	public void testGetBaseType_MyClass()
	{
		_testObj = (LiftingType)_variableDecl.getType();
        Name typeName = ((SimpleType)_testObj.getBaseType()).getName();
    
        String actual = ((SimpleName)typeName).getIdentifier();
		
		assertEquals("Base type has wrong name ",
                "MyClass",
                actual);
	}
	
	public void testGetRoleType_MyRole()
	{
		_testObj = (LiftingType)_variableDecl.getType();
        Name typeName = ((SimpleType)_testObj.getRoleType()).getName();
	
        String actual = ((SimpleName)typeName).getIdentifier();
		
		assertEquals("Role type has wrong name ",
                "MyRole",
                actual);
	}
	
	public void testSignature()
	{
		_testObj = (LiftingType)_variableDecl.getType();
        String sig = Util.getSignature(_testObj);
		
		assertEquals("Base type has wrong name ",
                "QMyClass;",
                sig);
	}
	
}
