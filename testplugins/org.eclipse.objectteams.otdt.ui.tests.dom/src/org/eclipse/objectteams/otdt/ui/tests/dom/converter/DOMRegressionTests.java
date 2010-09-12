/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom.converter;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;

public class DOMRegressionTests extends FileBasedDOMTest {
	
    public static final String TEST_PROJECT = "DOM_AST";
    private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

    
	public DOMRegressionTests(String name) {
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(DOMRegressionTests.class);
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();

	}

	protected void setUp() throws Exception 
	{
		super.setUp();
	}
	
	//  Bug 316666 -  [dom] IllegalArgumentException in very broken source
	public void testBug316666() throws JavaModelException {
		ICompilationUnit teamClass = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "regression.bug316666",
                "MyTeam.java");
		ASTParser parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		parser.setProject(getJavaProject(TEST_PROJECT));
		parser.setSource(teamClass);
		parser.setResolveBindings(true);
		
		ASTNode root = parser.createAST( new NullProgressMonitor() );        
		CompilationUnit compUnit = (CompilationUnit) root;
		TypeDeclaration topTeam = (TypeDeclaration) compUnit.types().get(0);
		TypeDeclaration nestedTeam = (TypeDeclaration) topTeam.getTypes()[1];
		// note: the bug was related to mistakenly converted generated fields.
		assertEquals("No fields expected",nestedTeam.getFields().length, 0);
	}
	
	// version with somewhat repaired structure in the source file (similar(?) to what the parser actually saw):
	public void testBug316666_repaired() throws JavaModelException {
		ICompilationUnit teamClass = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "regression.bug316666",
                "MyTeamRepaired.java");
		ASTParser parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		parser.setProject(getJavaProject(TEST_PROJECT));
		parser.setSource(teamClass);
		parser.setResolveBindings(true);
		
		ASTNode root = parser.createAST( new NullProgressMonitor() );        
		CompilationUnit compUnit = (CompilationUnit) root;
		TypeDeclaration topTeam = (TypeDeclaration) compUnit.types().get(0);
		TypeDeclaration nestedTeam = (TypeDeclaration) topTeam.getTypes()[0];
		// note: the bug was related to mistakenly converted generated fields.
		assertEquals("No fields expected",nestedTeam.getFields().length, 0);
	}

}
