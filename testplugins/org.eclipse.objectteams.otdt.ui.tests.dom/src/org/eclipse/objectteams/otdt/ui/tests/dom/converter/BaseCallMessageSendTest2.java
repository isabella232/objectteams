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
 * $Id$
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
import org.eclipse.jdt.core.dom.BaseCallMessageSend;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;


/**
 * @author mkr
 * @version $Id: BaseCallMessageSendTest.java 12156 2006-05-20 20:19:55 +0000 (Sa, 20 Mai 2006) stephan $
 */
public class BaseCallMessageSendTest2 extends FileBasedDOMTest
{
    
    public static final String TEST_PROJECT = "DOM_AST";
    private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

    // Java class used for all within tests
    private TypeDeclaration _role;
    private BaseCallMessageSend _testObj;
    private TypeDeclaration _typeDecl;

    public BaseCallMessageSendTest2(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(BaseCallMessageSendTest2.class);
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();
		ICompilationUnit teamClass = getCompilationUnit(
	                                      getTestProjectDir(),
	                                      "src",
	                                      "basecall2.teampkg",
	                                      "Team1.java");
		ASTParser parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		parser.setProject(getJavaProject(TEST_PROJECT));
		parser.setSource(teamClass);
	
		ASTNode root = parser.createAST( new NullProgressMonitor() );        
        CompilationUnit compUnit = (CompilationUnit) root;
        _typeDecl = (TypeDeclaration)compUnit.types().get(0);
        _role = _typeDecl.getTypes()[0];
	}

	protected void setUp() throws Exception 
	{
		super.setUp();
	}

    public void testGetName1()
    {
        MethodDeclaration method = _role.getMethods()[0];
        TryStatement tryStatement = (TryStatement)method.getBody().statements().get(0);
        Block block = tryStatement.getBody();
        
        ExpressionStatement exprStatement = (ExpressionStatement)block.statements().get(0);
            
        _testObj = (BaseCallMessageSend)exprStatement.getExpression();

        String actual = _testObj.getName().getIdentifier();
        
        assertEquals("Base call has wrong name ",
                     "roleMethod0",
                     actual);
    }
    
    public void testGetName2()
    {
        MethodDeclaration method = _role.getMethods()[1];
        ExpressionStatement exprStatement =
            (ExpressionStatement)method.getBody().statements().get(0);
        _testObj = (BaseCallMessageSend)exprStatement.getExpression();

        String actual = _testObj.getName().getIdentifier();
        
        assertEquals("Base call has wrong name ",
                     "roleMethod1",
                     actual);
    }
}   
