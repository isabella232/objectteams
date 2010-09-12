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
 * $Id: ASTRewritingModifyingCallinMappingDeclarationTest.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/

package org.eclipse.objectteams.otdt.ui.tests.dom.rewrite;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.GuardPredicateDeclaration;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.rewrite.ASTNodeCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

// TODO(jsv) create own OT package for this test
@SuppressWarnings("nls")
public class ASTRewritingModifyingCallinMappingDeclarationTest extends ASTRewritingModifyingTest {
	private static final Class<?> THIS = ASTRewritingModifyingCallinMappingDeclarationTest.class;
	
	public ASTRewritingModifyingCallinMappingDeclarationTest(String name) {
		super(name);
	}
	
	public static Test allTests() {
		return new Suite(THIS);
	}
	
	public static Test suite() {
		if (true) {
			return allTests();
		}
		TestSuite suite= new Suite("one test");
		suite.addTest(new ASTRewritingModifyingCallinMappingDeclarationTest("test0009"));
		return suite;
	}
	
	/**
	 * add 2 parameter mappings to callinMappingDeclaration without parameter mappings
	 */
	@SuppressWarnings("unchecked") // callin.getParameterMappings().add(..);
	public void test0001() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0001", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		Expression expression1 = ASTNodeCreator.createExpression(ast,"s1+\"Test\"");
		ParameterMapping mapping1 = ASTNodeCreator.createParameterMapping(ast,expression1,ast.newSimpleName("s2"),"<-",false);
		
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 
		callin.getParameterMappings().add(mapping1);
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * delete all parameter mappings from callinMappingDeclaration
	 */
	public void test0002() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0002", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2) with {\n");
		buf.append("            s2 <- s1 + \"Test\",\n");
		buf.append("            result + \"addOnString\" -> result\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
				
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0);
		callin.getParameterMappings().remove(1);
		callin.getParameterMappings().remove(0);
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * add one parameter mapping
	 */
	@SuppressWarnings("unchecked") // callin.getParameterMappings().add(..);
	public void test0003() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0003", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		Expression expression1 = ASTNodeCreator.createExpression(ast,"result+\"addOnString\""); 	
		ParameterMapping mapping1 = ASTNodeCreator.createParameterMapping(ast,expression1,ast.newSimpleName("result"),"->",false);
		
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 
		callin.getParameterMappings().add(mapping1);
				
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2) with {\n");
		buf.append("            s2 <- s1 + \"Test\",\n");
		buf.append("            result + \"addOnString\" -> result\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}

	/**
	 * change callin modifier
	 */
	public void test0004() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0004", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0004;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
			
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 
		callin.setCallinModifier(Modifier.OT_AFTER_CALLIN);
				
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0004;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- after String baseMethod(String s2) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * change role methodSpec in callinMappingDeclaration 
	 * @throws JavaModelException
	 * @throws BadLocationException
	 * @throws MalformedTreeException
	 */
	@SuppressWarnings("unchecked") // argList1.add(..)
	public void test0005() throws MalformedTreeException, JavaModelException, BadLocationException {
        
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0005", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0005;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 

		List argList1 = new ArrayList();
		argList1.add(ASTNodeCreator.createArgument(ast,0,ASTNodeCreator.createType(ast, "String"),"s1",0,null));
		
		MethodSpec methodSpec1 = ASTNodeCreator.createMethodSpec(
		        ast,
		        "newRoleMethod",
		        ASTNodeCreator.createType(ast, "String"),
		        argList1,
		        true);
		
		callin.setRoleMappingElement(methodSpec1);

		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0005;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String newRoleMethod(String s1) <- before String baseMethod(String s2) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}

	/**
	 * add base mapping element (guard present)
	 */
	@SuppressWarnings("unchecked") // argList1.add(..)
	public void test0006() throws MalformedTreeException, JavaModelException, BadLocationException {		
        
        IPackageFragment pack1= sourceFolder.createPackageFragment("test0006", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0006;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2) when (true) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu = pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 

		List argList1 = new ArrayList();
		argList1.add(ASTNodeCreator.createArgument(ast,0,ASTNodeCreator.createType(ast, "String"),"s3",0,null));
		
		MethodSpec methodSpec1 = ASTNodeCreator.createMethodSpec(
		        ast,
		        "fooBaseMethod",
		        ASTNodeCreator.createType(ast, "String"),
		        argList1,
		        true);
		
		callin.getBaseMappingElements().add(methodSpec1);

		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0006;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2), String fooBaseMethod(String s3) when (true) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * remove first base mapping element
	 */
	public void test0007() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0007", false, null);
		StringBuffer buf= new StringBuffer();
		
		buf.append("package test0007;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2), String fooBaseMethod(String s3) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu = pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
			
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 
		callin.getBaseMappingElements().remove(0);

		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0007;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String fooBaseMethod(String s3) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
	
	
	/**
	 * add guard predicate
	 */
	public void test0008() throws MalformedTreeException, JavaModelException, BadLocationException {		
        
        IPackageFragment pack1= sourceFolder.createPackageFragment("test0008", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0008;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu = pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 
		
		GuardPredicateDeclaration guard = ASTNodeCreator.createGuardPredicate(ast, /*isBase*/false, ast.newBooleanLiteral(true));
		
		callin.setGuardPredicate(guard);

		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0008;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2) when (true) with {\n");
		buf.append("            s2 <- s1 + \"Test\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}

	/**
	 * remove signatures, 2 base methods.
	 */
	public void test0009() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0009", false, null);
		StringBuffer buf= new StringBuffer();
		
		buf.append("package test0009;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2), String fooBaseMethod(String s3);\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu = pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
			
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 
		callin.removeSignatures();

		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0009;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        roleMethod <- before baseMethod, fooBaseMethod;\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}

	/**
	 * remove signatures, 2 base methods, some arg-lists are empty, w/ linebreaks
	 */
	public void test0010() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0010", false, null);
		StringBuffer buf= new StringBuffer();
		
		buf.append("package test0010;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod()// should keep this comment\n");
		buf.append("		<- before\n");
		buf.append("			String baseMethod(),\n");
		buf.append("			String fooBaseMethod(String s3);\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu = pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
			
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 
		callin.removeSignatures();

		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0010;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        roleMethod// should keep this comment\n");
		buf.append("		<- before\n");
		buf.append("			baseMethod,\n");
		buf.append("			fooBaseMethod;\n");		
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}

	/**
	 * remove signatures, 1 base method, type parameter
	 */
	public void test0011() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0011", false, null);
		StringBuffer buf= new StringBuffer();
		
		buf.append("package test0011;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        <E> String roleMethod(E s1) <- before String baseMethod(String s2);\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu = pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
			
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 
		callin.removeSignatures();

		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0011;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        roleMethod <- before baseMethod;\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}

	/**
	 * add signatures, 2 base methods.
	 */
	public void test0012() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0012", false, null);
		StringBuffer buf= new StringBuffer();
		
		buf.append("package test0012;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        roleMethod <- before baseMethod, fooBaseMethod;\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu = pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
			
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 
		addSignature(astRoot.getAST(),
					 (MethodSpec)callin.getRoleMappingElement(),
					 "String", new String[] {"String"}, new String[] {"s1"} );
		addSignature(astRoot.getAST(),
				 (MethodSpec)callin.getBaseMappingElements().get(0),
				 "String", new String[] {"String"}, new String[] {"s2"} );
		addSignature(astRoot.getAST(),
				 (MethodSpec)callin.getBaseMappingElements().get(1),
				 "String", new String[] {"String", "Object"}, new String[] {"s3", "ignored"} );

		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0012;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) <- before String baseMethod(String s2), String fooBaseMethod(String s3, Object ignored);\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}

	/**
	 * add signatures, 1 base method, type parameter
	 */
	public void test0013() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0013", false, null);
		StringBuffer buf= new StringBuffer();
		
		buf.append("package test0013;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        roleMethod <- before baseMethod;\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu = pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		AST ast = astRoot.getAST();
		
		astRoot.recordModifications();
			
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CallinMappingDeclaration callin = (CallinMappingDeclaration) role.bodyDeclarations().get(0); 
		MethodSpec roleMethodSpec = (MethodSpec)callin.getRoleMappingElement();
		TypeParameter typeParameter = ast.newTypeParameter();
		typeParameter.setName(ast.newSimpleName("E"));
		roleMethodSpec.typeParameters().add(typeParameter);
		addSignature(ast,
					 roleMethodSpec,
					 "E", new String[] {"String"}, new String[] {"s1"} );
		addSignature(ast,
				 (MethodSpec)callin.getBaseMappingElements().get(0),
				 "E", new String[] {"String"}, new String[] {"s2"} );

		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0013;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        <E> E roleMethod(String s1) <- before E baseMethod(String s2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}

	@SuppressWarnings("unchecked")
	private void addSignature(AST ast, MethodSpec mspec, String returnType, String[] argTypes, String[] argNames) {
		mspec.setSignatureFlag(true);
		mspec.setReturnType2(ast.newSimpleType(ast.newSimpleName(returnType)));
		for (int i = 0; i < argTypes.length; i++) {
			SingleVariableDeclaration arg = ast.newSingleVariableDeclaration();
			arg.setName(ast.newSimpleName(argNames[i]));
			arg.setType(ast.newSimpleType(ast.newSimpleName(argTypes[i])));
			mspec.parameters().add(arg);
		}
	}
}
	