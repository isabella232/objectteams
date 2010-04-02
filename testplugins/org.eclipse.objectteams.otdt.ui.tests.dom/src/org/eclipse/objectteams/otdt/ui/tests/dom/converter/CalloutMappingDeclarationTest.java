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
 * $Id: CalloutMappingDeclarationTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccessSpec;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;


/**
 * @author jsv
 * @version $Id: CalloutMappingDeclarationTest.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * NOTE: ParameterMappings are tested in ParameterMappingTest, including the PARAMETER_MAPPINGS_PROPERTY
 * (attribute of CalloutMappingDeclaration).
 */
@SuppressWarnings("nls")
public class CalloutMappingDeclarationTest extends FileBasedDOMTest
{
    public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

	private ASTParser _parser;
	private ICompilationUnit _simpleTeam;
	private TypeDeclaration _typeDecl; // a java class common to all within tests
	private TypeDeclaration _role;
	// one more version with a syntax error:
	private ICompilationUnit _simpleTeamBuggy;
	private TypeDeclaration _typeDeclBuggy; 
	private TypeDeclaration _roleBuggy;
	
	private CalloutMappingDeclaration _testObj;

	public CalloutMappingDeclarationTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(CalloutMappingDeclarationTest.class);
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
	            "callouts.teampkg",
	            "Team1.java");
		
		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject( getJavaProject(TEST_PROJECT) );
		_parser.setSource(_simpleTeam);
		
		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
		_typeDecl = (TypeDeclaration)compUnit.types().get(0);
		_role = _typeDecl.getTypes()[0];
	}

	private void setupForBuggyTeam() throws JavaModelException {
		_simpleTeamBuggy = getCompilationUnit(
	            getTestProjectDir(),
	            "src",
	            "callouts.teampkg",
	            "TeamBuggy.java");
		
		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject( getJavaProject(TEST_PROJECT) );
		_parser.setSource(_simpleTeamBuggy);
		
		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
		_typeDeclBuggy = (TypeDeclaration)compUnit.types().get(0);
		_roleBuggy = _typeDeclBuggy.getTypes()[0];
	}

	public void testInstanceType1()
	{
		// extract the callout mapping as body declaration
		BodyDeclaration testObj = (BodyDeclaration)_role.bodyDeclarations().get(2);
	
		assertTrue(
				"BodyDeclaration is not an instance of CalloutMappingDeclaration",
				testObj instanceof CalloutMappingDeclaration);
	}
	
	public void testGetNodeType1()
	{
		// extract the callout mapping as body declaration
	    BodyDeclaration testObj = (BodyDeclaration)_role.bodyDeclarations().get(2);
		
		int actual = testObj.getNodeType();
		
		assertEquals(
				"CalloutMappingDeclaration has wrong NodeType", 
				ASTNode.CALLOUT_MAPPING_DECLARATION, 
				actual);
	}
	
	public void testIsCalloutOverride_false()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(2);		

        boolean actual = _testObj.isCalloutOverride();
		
		assertFalse("Callout is not overridden", actual);
	}
	
	public void testIsCalloutOverride_true()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(3);
		
		boolean actual = _testObj.isCalloutOverride();
		
		assertTrue("Callout is overridden", actual);
	}
	
	public void testHasSignature_true()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(3);
		
		boolean actual = _testObj.hasSignature(); 
		
		assertTrue(
				"Callout binding should have a signature", 
				actual);
	}
	
	public void testHasSignature_false()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(2);
		
		boolean actual = _testObj.hasSignature();
		
		assertFalse(
				"Callout binding should not have a signature", 
				actual);
	}
	
	public void testCalloutWithModifier() {
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(10);
		
		boolean found = false;
		boolean other = false;
		for (Object modObj : _testObj.modifiers()) {
			Modifier modifier = (Modifier) modObj;
			if (modifier.isProtected()) {
				found = true;
				break;
			} else {
				other = true;
				System.out.println("Unexpected modifier "+other);
			}
		}
		
		assertTrue(
				"Callout binding should have a protected modifier", 
				found);		
		assertFalse(
				"Callout binding should not have other modifier", 
				other);
	}

	public void testGetParent_notNull1()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(2);

		ASTNode actual = _testObj.getParent(); 
		
		assertNotNull(
				"ParentNode of CalloutMappingDeclaration has to be not null",
				actual);
	}

	public void testGetParentNode_notNull2()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(3);

		ASTNode actual = _testObj.getParent(); 
		
		assertNotNull(
				"ParentNode of CalloutMappingDeclaration has to be not null",
				actual);
	}	

	public void testGetParent_InstanceType1()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(2);
		
		ASTNode actual = _testObj.getParent();

		assertTrue(
				"ParentNode has wrong Type",
				actual instanceof RoleTypeDeclaration );
	}

	public void testGetParent_InstanceType2()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(3);

		ASTNode actual = _testObj.getParent();
		
		assertTrue(
				"ParentNode has wrong Type",
				actual instanceof RoleTypeDeclaration );
	}
	
	public void testGetLeftMethodSpec_notNull()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(2);
		
		MethodSpec actual = (MethodSpec)_testObj.getRoleMappingElement();
		
		assertNotNull("LeftMethodSpec must not be null", actual);
	}	

	public void testGetRightMethodSpec_notNull()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(2);
		
		MethodSpec actual = (MethodSpec)_testObj.getBaseMappingElement();

		assertNotNull("RightMethodSpec must not be null", actual);
	}
	
	public void testHasSignature_SameFlag()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(3);
		MethodSpec leftMethodSpec  = (MethodSpec)_testObj.getRoleMappingElement();
		MethodSpec rightMethodSpec = (MethodSpec)_testObj.getBaseMappingElement();
		
		boolean actual = leftMethodSpec.hasSignature() == rightMethodSpec.hasSignature();   
		
		assertTrue(
				"The base and base method should have the same signature flag",
				actual);
	}
	
	public void testGetRightMethodSpec_InstanceType1()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(2);
		
		Object actual = _testObj.getBaseMappingElement();

		assertTrue(
				"In this case the base accessor should be a MethodSpec", 
				actual instanceof MethodSpec);
	}
	
	public void testGetRightMethodSpec_InstanceType2()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(5);
		
		Object actual = _testObj.getBaseMappingElement();
		
		assertTrue(
				"In this case the base accessor should be a FieldAccessSpec", 
				actual instanceof FieldAccessSpec);
	}
	
	public void testGetRightMethodSpec_InstanceType3()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(7);
		
		Object actual = _testObj.getBaseMappingElement();
		
		assertTrue(
				"In this case the base accessor should be a FieldAccessSpec", 
				actual instanceof FieldAccessSpec);
	}
	
	public void testGetParameterMappings_Empty()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(7);
		List parameterMappings = _testObj.getParameterMappings();
		
		boolean actual = parameterMappings.isEmpty();
		
		assertTrue(
				"List ParameterMappings is not empty",
				 actual);
	}
	
	public void testGetParameterMappings_NotEmpty()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(9);
		List parameterMappings = _testObj.getParameterMappings();
		
		boolean actual = parameterMappings.isEmpty();
		
 		assertFalse("List ParameterMappings is empty", actual);
 	} 
	
	/** 
     * compares the AST references of calloutMappingDeclaration and 
     * roleMethodSpec 
     */
	public void testGetAST_Identity1()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(2);
		MethodSpec roleMethodSpec = (MethodSpec)_testObj.getRoleMappingElement();
		
		assertTrue(
				"Role Methodspec is not associate to the same AST like parent CalloutMappingDeclaration",
                _testObj.getAST() == roleMethodSpec.getAST());
	}

	/** 
     * compares the AST references of calloutMappingDeclaration and baseMethodSpec
     */
	public void testGetAST_Identity2()
	{
		_testObj= (CalloutMappingDeclaration)_role.bodyDeclarations().get(2);
		MethodSpec baseMethodSpec = (MethodSpec)_testObj.getBaseMappingElement();
		
		assertTrue(
				"Base Methodspec is not associate to the same AST like parent CalloutMappingDeclaration",
				_testObj.getAST() == baseMethodSpec.getAST());
	}
	
	/** 
     * compares the AST references of calloutMappingDeclaration and 
     * baseFieldAccessSpec 
     */	
	public void testGetAST_Identity3()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(5);
		FieldAccessSpec baseFiedAccessSpec = 
			(FieldAccessSpec)_testObj.getBaseMappingElement();
		
		assertTrue(
				"Base FieldAccessySpec is not associate to the same AST like parent CalloutMappingDeclaration",
				_testObj.getAST() == baseFiedAccessSpec.getAST());
	}
	
	/** 
     * compares the AST references of calloutMappingDeclaration and first 
     * ParameterMapping 
     */	
	public void testGetAST_Identity4()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(9);
		// get first ParameterMapping
		ParameterMapping parameterMapping = 
			(ParameterMapping)_testObj.getParameterMappings().get(0); 
			
		assertTrue(
				"First ParameterMapping is not associate to the same AST like parent CalloutMappingDeclaration",
				_testObj.getAST() == parameterMapping.getAST());
	}
	
	public void testSubtreeMatch1()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(9);
		
		boolean actual = _testObj.subtreeMatch(new ASTMatcher(), _testObj);
		
		assertTrue("Callout mappings don't match", actual);
	}
	
	public void testCopySubtree1()
	{
		_testObj = (CalloutMappingDeclaration)_role.bodyDeclarations().get(9);
		
		CalloutMappingDeclaration clonedTestObject = 
			(CalloutMappingDeclaration)ASTNode.copySubtree(AST.newAST(AST.JLS3), _testObj);
		boolean actual = _testObj.subtreeMatch(new ASTMatcher(), clonedTestObject);

        assertTrue("Copy of subtree not correct", actual);
	}
	
	public void testSourceRangeOfBuggyCallout() throws JavaModelException
	{
		setupForBuggyTeam();
		
		_testObj = (CalloutMappingDeclaration)_roleBuggy.bodyDeclarations().get(3);
		
		int start = _testObj.getRoleMappingElement().getStartPosition(); 
		int length = _testObj.getRoleMappingElement().getLength(); 
		
		assertEquals(436, start);
		assertEquals(14, length); // "void fooBar2()"
	}

}
