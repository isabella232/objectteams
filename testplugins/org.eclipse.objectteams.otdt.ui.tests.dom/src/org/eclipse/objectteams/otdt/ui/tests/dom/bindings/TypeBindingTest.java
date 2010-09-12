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
 * $Id: TypeBindingTest.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom.bindings;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;
import org.eclipse.objectteams.otdt.ui.tests.dom.TypeDeclarationFinder;

/**
 * @author Michael Krueger
 * @version $Id: TypeBindingTest.java 23496 2010-02-05 23:20:15Z stephan $
 */
public class TypeBindingTest extends FileBasedDOMTest
{
	public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;
	
	private ASTParser _parser;
	private ICompilationUnit _cuTA;
    private ICompilationUnit _cuTB;

    private RoleTypeDeclaration _roleTAT2R1;
    private RoleTypeDeclaration _roleTBT1R1;
    private RoleTypeDeclaration _roleTBT2R1;

    private RoleTypeDeclaration _focus;
    
	public TypeBindingTest(String name) 
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(TypeBindingTest.class);
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();
	}
	
	protected void setUp() throws Exception 
	{
		super.setUp();
		_cuTA = getCompilationUnit(
				getTestProjectDir(),
				"src",
				"roleTypeDeclaration.teampkg",
		        "TA.java");
        _cuTB = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "roleTypeDeclaration.teampkg",
                "TB.java");
		
		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject(super.getJavaProject(TEST_PROJECT));
        _parser.setResolveBindings(true);
		_parser.setSource(_cuTA);
		
		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
        TypeDeclarationFinder finder = new TypeDeclarationFinder();

        finder.setName("TA.T2.R1");
		compUnit.accept(finder);
		_roleTAT2R1 = (RoleTypeDeclaration)finder.getTypeDeclaration();

        _parser.setSource(_cuTB);
        _parser.setResolveBindings(true);
        
        root = _parser.createAST( new NullProgressMonitor() );
        compUnit = (CompilationUnit) root;

        finder.setName("TB.T1.R1");
        compUnit.accept(finder);
        _roleTBT1R1 = (RoleTypeDeclaration)finder.getTypeDeclaration();

        finder.setName("TB.T2.R1");
        compUnit.accept(finder);
        _roleTBT2R1 = (RoleTypeDeclaration)finder.getTypeDeclaration();        
	}
	
	public void testInstanceTypes()
	{
        assertNotNull(_roleTAT2R1);
        assertNotNull(_roleTBT1R1);
        assertNotNull(_roleTBT2R1);
		assertTrue(_roleTAT2R1 instanceof RoleTypeDeclaration);
        assertTrue(_roleTBT1R1 instanceof RoleTypeDeclaration);
        assertTrue(_roleTBT2R1 instanceof RoleTypeDeclaration);
	}
	
    public void testGetSuperRoles()
    {
        _focus = _roleTBT2R1;
        ITypeBinding binding = (ITypeBinding)_focus.resolveBinding();
        
        ITypeBinding[] expected = new ITypeBinding[]
                                   {
                                    _roleTBT1R1.resolveBinding(),
                                    _roleTAT2R1.resolveBinding()
                                   };
        ITypeBinding[] actual = binding.getSuperRoles();

        assertEquals(expected.length, actual.length);
        // compare the optimal name, since TAT2R1 has no key
        assertEquals(expected[0].getOptimalName(), actual[0].getOptimalName());
        assertEquals(expected[1].getOptimalName(), actual[1].getOptimalName());
    }
}
