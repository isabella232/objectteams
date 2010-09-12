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
 * $Id: RoleTypeDeclarationTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;

/**
 * @author ikeman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RoleTypeDeclarationTest extends FileBasedDOMTest
{
	public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;
	
	private ASTParser _parser;
	private ICompilationUnit _simpleTeam;
	private TypeDeclaration _typeDecl;
	private TypeDeclaration _testObj1;
	private TypeDeclaration _testObj2;    
	private TypeDeclaration _testObj3;    
	
	public RoleTypeDeclarationTest(String name) 
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(RoleTypeDeclarationTest.class);
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
				"roleTypeDeclaration.teampkg",
		"MyTeam.java");
		
		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject(super.getJavaProject(TEST_PROJECT));
		_parser.setSource(_simpleTeam);
		
		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
		_typeDecl = (TypeDeclaration)compUnit.types().get(0);
		_testObj1 = _typeDecl.getTypes()[0];
		_testObj2 = _typeDecl.getTypes()[1];
		_testObj3 = _typeDecl.getTypes()[2];
	}
	
	public void testInstanceTypes()
	{
		assertTrue("First role was not detected as RoleTypeDeclaration",_testObj1 instanceof RoleTypeDeclaration);
		assertTrue("Second role was not detected as RoleTypeDeclaration",_testObj2 instanceof RoleTypeDeclaration);
		assertTrue("Third role was not detected as RoleTypeDeclaration",_testObj3 instanceof RoleTypeDeclaration);
	}
	
	//test for first Role in roleTypeDeclaration.teampkg.MyTeam
	public void testIsTeam1()
	{
		boolean actual = _testObj1.isTeam();
		
		assertFalse("This RoleTypeDeclaration is not a Team.", actual);
	}
	
	//test for second Role in roleTypeDeclaration.teampkg.MyTeam    
	public void testIsTeam2()
	{
		boolean actual = _testObj2.isTeam();
		
		assertTrue("This RoleTypeDeclaration is a Team.", actual);
	}
	
	//test for third Role in roleTypeDeclaration.teampkg.MyTeam   
	public void testIsTeam3()
	{
		boolean actual = _testObj3.isTeam();
		
		assertFalse("This RoleTypeDeclaration is not a Team.", actual);
	}    
	
	
	//test for first Role in roleTypeDeclaration.teampkg.MyTeam
	public void testGetBase1_notNull()
	{
		Type baseClass = ((RoleTypeDeclaration)_testObj1).getBaseClassType();
		
		assertNotNull("RoleTypeDeclaration !!!has!!! a BaseClass.", baseClass);
	}
	
	//test for second Role in roleTypeDeclaration.teampkg.MyTeam   
	public void testGetBase2_notNull()
	{
		Type baseClass = ((RoleTypeDeclaration)_testObj2).getBaseClassType();
		
		assertNotNull("RoleTypeDeclaration !!!has!!! a BaseClass.", baseClass);
	}
	
	//test for third Role in roleTypeDeclaration.teampkg.MyTeam   
	public void testGetBase3_Null()
	{
		Type baseClass = ((RoleTypeDeclaration)_testObj3).getBaseClassType();
		
		assertNull("RoleTypeDeclaration !!!hasn't!!! a BaseClass.", baseClass);
	}    
	
	//test for first Role in roleTypeDeclaration.teampkg.MyTeam
	public void testGetTeam1_notNull()
	{
		Type teamClass = ((RoleTypeDeclaration)_testObj1).getTeamClassType();
		
		assertNotNull("RoleTypeDeclaration !!!has!!! a TeamClass.", teamClass);
	}
	
	//test for second Role in roleTypeDeclaration.teampkg.MyTeam   
	public void testGetTeam2_notNull()
	{
		Type teamClass = ((RoleTypeDeclaration)_testObj2).getTeamClassType();
		
		assertNotNull("RoleTypeDeclaration !!!has!!! a TeamClass.", teamClass);
	}
	
	//test for third Role in roleTypeDeclaration.teampkg.MyTeam   
	public void testGetTeam3_notNull()
	{
		Type teamClass = ((RoleTypeDeclaration)_testObj3).getTeamClassType();
		
		assertNotNull("RoleTypeDeclaration !!!hasn't!!! a TeamClass.", teamClass);
	}    

    //test for first Role in roleTypeDeclaration.teampkg.MyTeam
    public void testGetCallOuts1_notEmpty()
    {
        CalloutMappingDeclaration[] callouts = ((RoleTypeDeclaration)_testObj1).getCallOuts();
        boolean isEmpty = callouts.length == 0; 
        
        assertFalse("List of CalloutMappingDeclaration isn't empty", isEmpty);
    }
    
    //test for third Role in roleTypeDeclaration.teampkg.MyTeam
    public void testGetCallOuts3_isEmpty()
    {
        CalloutMappingDeclaration[] callouts = ((RoleTypeDeclaration)_testObj3).getCallOuts();
        boolean isEmpty = callouts.length == 0; 
        
        assertTrue("List of CalloutMappingDeclaration is empty", isEmpty);
    }

    //test for second Role in roleTypeDeclaration.teampkg.MyTeam
    public void testGetCallIns2_notEmpty()
    {
        CallinMappingDeclaration[] callins = ((RoleTypeDeclaration)_testObj2).getCallIns();
        boolean isEmpty = callins.length == 0; 
        
        assertFalse("List of CallinMappingDeclaration isn't empty", isEmpty);
    }
    
    //test for third Role in roleTypeDeclaration.teampkg.MyTeam
    public void testGetCallIns3_isEmpty()
    {
        CallinMappingDeclaration[] callins = ((RoleTypeDeclaration)_testObj3).getCallIns();
        boolean isEmpty = callins.length == 0; 
        
        assertTrue("List of CallinMappingDeclaration is empty", isEmpty);
    }

    public void testSubTreeMatch1()
    {
        boolean actual = _testObj1.subtreeMatch(new ASTMatcher(), _testObj1);

        assertTrue("Both nodes are equal, even the same.", actual);
    } 
    
    public void testCopySubtree1()
    {
        RoleTypeDeclaration clonedTestObject = 
            (RoleTypeDeclaration)ASTNode.copySubtree(AST.newAST(AST.JLS3), _testObj1);

        boolean actual = _testObj1.subtreeMatch(new ASTMatcher(), clonedTestObject);
        
        assertTrue("Copy of subtree not correct", actual);
    }
}
