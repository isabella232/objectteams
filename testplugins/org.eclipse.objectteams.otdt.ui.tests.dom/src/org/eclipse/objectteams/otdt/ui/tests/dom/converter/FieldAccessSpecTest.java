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
 * $Id: FieldAccessSpecTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccessSpec;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;

/**
 * @author jsv
 */
public class FieldAccessSpecTest extends FileBasedDOMTest
{
    public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

	private ASTParser _parser;
	private ICompilationUnit _simpleTeam;
    private ICompilationUnit _baseClassCompUnit;
	
	private TypeDeclaration _typeDecl; // a java class common to all within tests
	private TypeDeclaration _role;
    private TypeDeclaration _base;
	
	private FieldAccessSpec _testObj;
    private Object mapping;
	
	public FieldAccessSpecTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(FieldAccessSpecTest.class);
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
	            "fieldAccessSpec.teampkg",
	            "Team1.java");

        _baseClassCompUnit = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "fieldAccessSpec.basepkg",
                "MyClass.java");

		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject( getJavaProject(TEST_PROJECT) );
		_parser.setSource(_simpleTeam);
        _parser.setResolveBindings(true);
        
		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit)root;
		_typeDecl = (TypeDeclaration)compUnit.types().get(0);
		_role = _typeDecl.getTypes()[0];
        
        _parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
        _parser.setProject( getJavaProject(TEST_PROJECT) );
        _parser.setSource(_baseClassCompUnit);
        _parser.setResolveBindings(true);
        
        compUnit = (CompilationUnit)_parser.createAST( new NullProgressMonitor() );
        _base = (TypeDeclaration)compUnit.types().get(0);        
	}

	public void testInstanceType1()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
		FieldAccessSpec testObj = (FieldAccessSpec)mapping.getBaseMappingElement();
		
		assertTrue(testObj instanceof FieldAccessSpec);
	}

    public void testHasSignature_true()
    {
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(3);
        _testObj = (FieldAccessSpec)mapping.getBaseMappingElement();
        
        boolean actual = _testObj.hasSignature();
        
        assertTrue("FieldAccessSpec should have a signature", actual);
    }
    
	public void testHasSignature_false()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
		_testObj = (FieldAccessSpec)mapping.getBaseMappingElement();
		
		boolean actual = _testObj.hasSignature();
		
		assertFalse("FieldAccessSpec should not have a signature", actual);
	}
	
	public void testGetParent_InstanceType1()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
		_testObj = (FieldAccessSpec)mapping.getBaseMappingElement();	
		
		ASTNode actual = _testObj.getParent();

		assertTrue(
				"ParentNode has wrong Type, Should be CalloutMappingDeclaration",
				actual instanceof CalloutMappingDeclaration);
	}
	
	public void testSubtreeMatch1()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(3);
		_testObj = (FieldAccessSpec)mapping.getBaseMappingElement();
		
		boolean actual = _testObj.subtreeMatch(new ASTMatcher(), _testObj);
		
		assertTrue("FieldAccessSpecs don't match", actual);
	}
	
	public void testCopySubtree1()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(3);
		_testObj = (FieldAccessSpec)mapping.getBaseMappingElement();		
		
		FieldAccessSpec clonedTestObject = 
			(FieldAccessSpec)ASTNode.copySubtree(AST.newAST(AST.JLS3), _testObj);
		
		boolean actual = _testObj.subtreeMatch(new ASTMatcher(), clonedTestObject);
		assertTrue("Copy of subtree not correct", actual);
	}
	
	public void testGetModifiers_Get()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
		_testObj = (FieldAccessSpec)mapping.getBaseMappingElement();
		
		int actual = mapping.bindingOperator().getBindingModifier();
		
        assertTrue(
            "Callout should have the get modifier", 
            Modifier.isGet(actual));
        assertEquals(
                "Callout should be a getter",
                Modifier.OT_GET_CALLOUT,
                mapping.bindingOperator().getBindingModifier());
	}
	
	public void testGetModifiers_Set()
	{
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(3);
		_testObj = (FieldAccessSpec)mapping.getBaseMappingElement();
		
		int actual = mapping.bindingOperator().getBindingModifier();
        
		assertTrue(
            "Callout should have the set modifier", 
            Modifier.isSet(actual));
        assertEquals(
                "Callout should be a setter", 
                Modifier.OT_SET_CALLOUT,
                mapping.bindingOperator().getBindingModifier());
	}
	
	public void testGetName_string()
    {
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
		_testObj = (FieldAccessSpec)mapping.getBaseMappingElement();
		
		String actual = _testObj.getName().getIdentifier();
		
		assertEquals("FieldAccess has wrong name ",
                "_string",
                actual);
    }
    
    public void testResolveBinding()
    {
        CalloutMappingDeclaration mapping = (CalloutMappingDeclaration)_role.bodyDeclarations().get(1);
        FieldDeclaration fieldDecl = (FieldDeclaration)_base.bodyDeclarations().get(0);
        
        _testObj = (FieldAccessSpec)mapping.getBaseMappingElement();
        
        VariableDeclarationFragment varDeclFrag =
            (VariableDeclarationFragment)fieldDecl.fragments().get(0);
        
        IVariableBinding expected = varDeclFrag.resolveBinding();
        IVariableBinding actual = _testObj.resolveBinding();

        assertEquals(expected.getKey(), actual.getKey());
    }
    

    
    
}
