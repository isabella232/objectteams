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
 * $Id: AnchoredTypeTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;

/**
 * @author stephan
 * @since 1.2.5
 */
@SuppressWarnings({ "nls", "restriction" })
public class AnchoredTypeTest extends FileBasedDOMTest
{
    public static final String TEST_PROJECT = "DOM_AST";
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

	private ASTParser _parser;
	private ICompilationUnit _simpleTeam;
	
	private TypeDeclaration _typeDecl; // a java class common to all within tests
	private MethodDeclaration _methodDecl;	
	private SingleVariableDeclaration _argumentDecl;
	private VariableDeclarationStatement _localDecls;
	private VariableDeclarationFragment _localDecl;
	
	
	private Type _testObj;
	
	public AnchoredTypeTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(AnchoredTypeTest.class);
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
	            "anchoredType.teampkg",
	            "Team1.java");
		
		_parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject( getJavaProject(TEST_PROJECT) );
		_parser.setSource(_simpleTeam);
		
		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		CompilationUnit compUnit = (CompilationUnit) root;
		_typeDecl = (TypeDeclaration)compUnit.types().get(0);
		_methodDecl = (MethodDeclaration)_typeDecl.bodyDeclarations().get(0);	
		_argumentDecl = (SingleVariableDeclaration)_methodDecl.parameters().get(1);
		_localDecls = (VariableDeclarationStatement)_methodDecl.getBody().statements().get(0);
		_localDecl = (VariableDeclarationFragment)_localDecls.fragments().get(0);
	}


	public void testSignature()
	{
		_testObj = _argumentDecl.getType();
        String sig = Util.getSignature(_testObj);
		
		assertEquals("Argument type has wrong name ",
                "QMyRole<@team1>;",
                sig);
	}
	
	public void testAllocation()
	{
		ClassInstanceCreation creation = (ClassInstanceCreation) _localDecl.getInitializer();
        String sig = Util.getSignature(creation.getType());
		
		assertEquals("Argument type has wrong name ",
                "QMyRole<@team1>;",
                sig);
	}
}
