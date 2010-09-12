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
 * $Id: CallinMappingDeclarationTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;


/**
 * @author mkr
 * @version $Id: CallinMappingDeclarationTest.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * NOTE: ParameterMappings are tested in ParameterMappingTest, including the PARAMETER_MAPPINGS_PROPERTY
 * (attribute of CallinMappingDeclaration).
 */
@SuppressWarnings("nls")
public class CallinMappingDeclarationTest extends FileBasedDOMTest
{
    public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

	private ASTParser _parser;
	private ICompilationUnit _simpleTeam;

    // Java class used for all within tests
	private TypeDeclaration _typeDecl;
	private CallinMappingDeclaration _testObj;

    public CallinMappingDeclarationTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(CallinMappingDeclarationTest.class);
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();
		_simpleTeam = getCompilationUnit(
	            getTestProjectDir(),
	            "src",
	            "callins.teampkg",
	            "Team1.java");

        _parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject( getJavaProject(TEST_PROJECT) );
		_parser.setSource(_simpleTeam);
		_parser.setResolveBindings(true);
		
        ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
		_typeDecl = (TypeDeclaration)compUnit.types().get(0);
	}

	protected void setUp() throws Exception 
	{
		super.setUp();
	}

    public void testGetName_generated()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(0);
        assertNull("Explicit name", _testObj.getName());
        String actual = _testObj.resolveBinding().getName();
        String expected = "</DOM_AST/src/callins/teampkg/Team1:9,8>"; // "<File:Line,Col>"

        assertTrue("Generated name mismatch", actual.startsWith(expected));
    }

    public void testGetName_named()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(2);
        String actual = _testObj.getName().getIdentifier();
        String expected = "callinName";

        assertEquals(expected, actual);
    }

    public void testGetCallinModifiers_BeforeBinding()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(0);
        
        assertTrue("Missing 'before' modifier",
                     Modifier.isBefore(_testObj.getCallinModifier()));
    }
	
    public void testGetCallinModifiers_ReplaceBinding()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(1);

        assertTrue("Missing 'replace' modifier",
                     Modifier.isReplace(_testObj.getCallinModifier()));
    }

    public void testGetCallinModifiers_AfterBinding()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(2);

        assertTrue("Missing 'after' modifier",
               Modifier.isAfter(_testObj.getCallinModifier()));
    }

    public void testGetCallinModifiers_isCallin()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(2);

        assertFalse("Callin modifier set for method mapping",
               Modifier.isCallin(_testObj.getCallinModifier()));
    }

	public void testCovariantReturn() {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(5);

        Object right= _testObj.getBaseMappingElements().get(0);
        assertNotNull(right);
        assertTrue(right instanceof MethodSpec);
        assertTrue("Covariant return type set for method mapping",
               ((MethodSpec)right).hasCovariantReturn());
        MethodSpec left= (MethodSpec)_testObj.getRoleMappingElement();
        assertFalse(left.typeParameters().isEmpty());
        TypeParameter param= (TypeParameter)left.typeParameters().get(0);
        assertEquals(param.getName().getIdentifier(), "T");
        assertFalse(param.typeBounds().isEmpty());
        SimpleType type= (SimpleType)param.typeBounds().get(0);
        assertEquals(type.getName().getFullyQualifiedName(), "MyClass");
    }

    public void testGetBaseMappingElements_OneToOneBinding()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(0);
        List actual = _testObj.getBaseMappingElements();

        assertEquals(1, actual.size());
    }
    
    public void testGetBaseMappingElements_ThreeElems()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(3);
        List actual = _testObj.getBaseMappingElements();

        assertEquals("Wrong number of MethodSpec",
                     3, actual.size());
    }
    
    public void testGetRoleMappingElement_InstanceType()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(3);
        
        assertTrue("Left side of callin not a MethodSpec",
                   _testObj.getRoleMappingElement() instanceof MethodSpec);
    }
    
    public void testGetBaseMappingElements_InstanceType()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(3);
        
        assertTrue("Right side of callin not a MethodSpec",
                   _testObj.getBaseMappingElements().get(0) instanceof MethodSpec);
        assertTrue("Right side of callin not a MethodSpec",
                _testObj.getBaseMappingElements().get(1) instanceof MethodSpec);
        assertTrue("Right side of callin not a MethodSpec",
                _testObj.getBaseMappingElements().get(2) instanceof MethodSpec);
       
    }
   
    public void testGetParameterMappings_NotNull()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(4);
        
        assertTrue("Parameter mapping null", _testObj.getParameterMappings() != null);
    }

    public void testGetParameterMappings_CorrectParent()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(4);
        List actualNodes = _testObj.getParameterMappings();
        for (Iterator iter = actualNodes.iterator(); iter.hasNext();) 
        {
            ParameterMapping curActual = (ParameterMapping) iter.next();
            assertTrue("CallinMappingDeclaration has ParameterMapping with wrong parent",
                       _testObj == curActual.getParent());
        }        
    }

    public void testGetParameterMappings_Empty()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(3);
        List actual = _testObj.getParameterMappings();

        assertTrue("Parameter mapping not empty", actual.isEmpty());
    }

    public void testParameterMappings_OneMapping()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(4);
        List actual = _testObj.getParameterMappings();

        assertEquals("Parameter mapping not empty", 1, actual.size());
    }

    public void testSubtreeMatch1()
    {
        TypeDeclaration[] types = _typeDecl.getTypes();
        TypeDeclaration role = types[0];

        _testObj = (CallinMappingDeclaration)role.bodyDeclarations().get(4);
        boolean actual = _testObj.subtreeMatch(new ASTMatcher(), _testObj);

        assertTrue("Callin mappings don't match", actual);
    }
    public void testBugTrac17() throws JavaModelException {
    	ICompilationUnit brokenCU= getCompilationUnit(
	            getTestProjectDir(),
	            "src",
	            "callins.teampkg",
	            "BrokenTeam.java");
		_parser.setSource(brokenCU);
		_parser.setResolveBindings(true);
		
        ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
		TypeDeclaration teamType = (TypeDeclaration)compUnit.types().get(0);
		TypeDeclaration roleType= (TypeDeclaration) teamType.bodyDeclarations().get(0);
		CallinMappingDeclaration callinMapping= (CallinMappingDeclaration) roleType.bodyDeclarations().get(0);
		assertEquals("foo <- after bar;", callinMapping.toString());
		// TODO(SH): adjust once recovery of parameter mappings is implemented.
    }
    
}
