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
 * $Id: MethodSpecTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;

/**
 * @author jsv
 * @version $Id: MethodSpecTest.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class MethodSpecTest extends FileBasedDOMTest
{
    public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

	private ASTParser _parser;
	private ICompilationUnit _simpleTeam;
    
	private TypeDeclaration _typeDecl; // a java class common to all within tests
	private TypeDeclaration _role;
    
	private MethodSpec _testObj;
	
	public MethodSpecTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(MethodSpecTest.class);
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
	            "methodSpec.teampkg",
	            "Team1.java");
        
		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject( getJavaProject(TEST_PROJECT) );
		_parser.setSource(_simpleTeam);
        _parser.setResolveBindings(true);
		
		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit)root;
		_typeDecl = (TypeDeclaration)compUnit.types().get(0);
		_role = _typeDecl.getTypes()[0];        
    }

	public void testInstanceType1()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
		Object testObj = mapping.getRoleMappingElement();
		
		assertTrue(testObj instanceof MethodSpec);
	}
	
	public void testParameters_Two()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
		_testObj = (MethodSpec)mapping.getRoleMappingElement();
		
		List actual = _testObj.parameters();
		
		assertEquals(2, actual.size());
	}
		
	public void testParameters_Empty()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(3);
		_testObj = (MethodSpec)mapping.getRoleMappingElement();
		
		List actual = _testObj.parameters();
		
		assertTrue(actual.isEmpty());
	}
	
	public void testHasSignature_true()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
		_testObj = (MethodSpec)mapping.getRoleMappingElement();
		
		boolean actual = _testObj.hasSignature();
		
		assertTrue("MethodSpec should have a signature", actual);
	}
	
    public void testHasSignature_false()
    {
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(5);
        _testObj = (MethodSpec)mapping.getRoleMappingElement();
        
        boolean actual = _testObj.hasSignature();

        assertFalse("MethodSpec should not have a signature", actual);
    }
    
	public void testSubtreeMatch1()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
		_testObj = (MethodSpec)mapping.getRoleMappingElement();
		
		boolean actual = _testObj.subtreeMatch(new ASTMatcher(), _testObj);
		
		assertTrue("MethodSpecs don't match", actual);
	}
	
	public void testCopySubtree1()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
		_testObj = (MethodSpec)mapping.getRoleMappingElement();		
		
		MethodSpec clonedTestObject = 
			(MethodSpec)ASTNode.copySubtree(AST.newAST(AST.JLS3), _testObj);
		boolean actual = _testObj.subtreeMatch(new ASTMatcher(), clonedTestObject);

        assertTrue("Copy of subtree not correct", actual);
	}
	
	public void testGetParent_InstanceType1()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
		_testObj = (MethodSpec)mapping.getRoleMappingElement();	
		
		ASTNode actual = _testObj.getParent();

		assertTrue(
				"ParentNode has wrong Type, Should be CalloutMappingDeclaration",
				actual instanceof CalloutMappingDeclaration );
	}
	
	public void testGetParent_InstanceType2()
	{
        CallinMappingDeclaration mapping = (CallinMappingDeclaration)_role.bodyDeclarations().get(7);
		_testObj = (MethodSpec)mapping.getRoleMappingElement();	
		
		ASTNode actual = _testObj.getParent();

		assertTrue(
				"ParentNode has wrong Type. Should be CallinMappingDeclaration",
				actual instanceof CallinMappingDeclaration );
	}
	
	public void testGetName1()
    {
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
        _testObj = (MethodSpec)mapping.getRoleMappingElement();
		String actual = _testObj.getName().getIdentifier();
		
		assertEquals("MethodSpec has wrong name ",
                "roleGetString",
                actual);
    }
        
    public void testResolveBinding_roleMethSpec()
    {        
        CallinMappingDeclaration mapping = (CallinMappingDeclaration)_role.bodyDeclarations().get(7);
        MethodDeclaration methDecl = (MethodDeclaration)_role.bodyDeclarations().get(6);
        
        _testObj = (MethodSpec)mapping.getRoleMappingElement();
        
        IMethodBinding expected = methDecl.resolveBinding();
        IMethodBinding actual = _testObj.resolveBinding();
           
        assertEquals(expected, actual);
    }
    
    public void testResolveBinding_baseMethSpec()
    {        
        CallinMappingDeclaration mapping = (CallinMappingDeclaration)_role.bodyDeclarations().get(7);
        
        _testObj = (MethodSpec)mapping.getBaseMappingElements().get(0);
        
        // Note: more than one createAST call causes that bindings resolved from one
        // AST won't be equal to ones resolved from the other AST. Se we can't compare 
        // bindings from _base with ones from _role. Instead, navigate the bindings within
        // one AST.

        RoleTypeDeclaration role = _typeDecl.getRoles()[0];
		IMethodBinding[] methods = role.resolveBinding().getBaseClass().getDeclaredMethods();
		IMethodBinding baseMethodBinding = null;
		// don't access via index since bindings are sorted by the compiler
		for (IMethodBinding binding : methods) {
			if (binding.getName().equals("baseMethod0")) {
				baseMethodBinding = binding;
				break;
			}
		}
		
        IMethodBinding expected = baseMethodBinding;
        assertNotNull(expected);
        IMethodBinding actual = _testObj.resolveBinding();
        assertEquals(expected, actual);
    }

}
