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

import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.GuardPredicateDeclaration;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteFlattener;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;
import org.eclipse.objectteams.otdt.ui.tests.dom.FileBasedDOMTest;

@SuppressWarnings({ "nls", "restriction" })
public class GuardPredicateTest extends FileBasedDOMTest {

    public static final String TEST_PROJECT = "DOM_AST";
    
	private static final int JAVA_LANGUAGE_SPEC_LEVEL = AST.JLS3;

	private ASTParser _parser;
	private ICompilationUnit _simpleTeam;

    // Java class used for all within tests
	private CompilationUnit _cu;
	private TypeDeclaration _typeDecl;
	
	public GuardPredicateTest(String name) {
		super(name);
	}
	
	public static Test suite()
	{
		return new Suite(GuardPredicateTest.class);
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir(TEST_PROJECT);
		super.setUpSuite();
		_simpleTeam = getCompilationUnit(
	            getTestProjectDir(),
	            "src",
	            "predicates.teampkg",
	            "Team2.java");

        _parser = ASTParser.newParser(JAVA_LANGUAGE_SPEC_LEVEL);
		_parser.setProject( getJavaProject(TEST_PROJECT) );
	}

	protected void setUp() throws Exception 
	{
		super.setUp();
	}
	
	@SuppressWarnings({ "unchecked" })
	private void initAST(ICompilationUnit icu, boolean resolve, String targetLevel) 
	{
		_parser.setSource(icu);
		
		Map options = getJavaProject(TEST_PROJECT).getOptions(true);
		options.put(CompilerOptions.OPTION_TargetPlatform, targetLevel);
		_parser.setCompilerOptions(options);
		_parser.setResolveBindings(resolve);

		
		ASTNode root = _parser.createAST( new NullProgressMonitor() );
		_cu = (CompilationUnit) root;
		_typeDecl = (TypeDeclaration)_cu.types().get(0);
		
	}
	

	public void testPredicateInRole() {
		initAST(_simpleTeam, false, CompilerOptions.VERSION_1_5);

		TypeDeclaration[] types = _typeDecl.getTypes();
        RoleTypeDeclaration role1 = (RoleTypeDeclaration)types[0];
        GuardPredicateDeclaration guard = role1.getGuardPredicate();
        assertFalse("guard predicate is non-null", guard == null);
        assertTrue("guard is base guard", guard.isBase());
        
	    ASTRewriteFlattener _rewriteFlattener = new ASTRewriteFlattener(new RewriteEventStore());
        guard.accept(_rewriteFlattener);

        String actual = _rewriteFlattener.getResult();
        String expected = "base when (Team2.this.hasRole(base,R.class))"; //$NON-NLS-1$
        assertEquals("Wrong guard Code", expected, actual);
	}
	
	@SuppressWarnings({ "restriction"})
	public void testPredicateRegression() throws JavaModelException {
		initAST(getCompilationUnit(
	            getTestProjectDir(),
	            "src",
	            "predicates.teampkg",
	            "TeamBug.java"),
	            true, CompilerOptions.VERSION_1_6);
		
		TypeDeclaration[] types = _typeDecl.getTypes();
        RoleTypeDeclaration role1 = (RoleTypeDeclaration)types[0];
        CallinMappingDeclaration callinDecl = role1.getCallIns()[0];
        GuardPredicateDeclaration guard = callinDecl.getGuardPredicate();
        assertFalse("guard predicate is non-null", guard == null);
        assertFalse("guard is not base guard", guard.isBase());
        
	    ASTRewriteFlattener _rewriteFlattener = new ASTRewriteFlattener(new RewriteEventStore());
        guard.accept(_rewriteFlattener);

        String actual = _rewriteFlattener.getResult();
        String expected = "when (true)"; //$NON-NLS-1$
        assertEquals("Wrong guard Code", expected, actual);
	}
}
