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
 * $Id: ASTRewritingModifyingTeamTest.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/

package org.eclipse.objectteams.otdt.ui.tests.dom.rewrite;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.GuardPredicateDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

//TODO(jsv) create own OT package for this test
public class ASTRewritingModifyingTeamTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingTeamTest.class;
	
	public ASTRewritingModifyingTeamTest(String name) {
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
		suite.addTest(new ASTRewritingModifyingTeamTest("test0009"));
		return suite;
	}
	
	/**
	 * insert a new role
	 */
	public void test0001() throws Exception {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0001", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("public team class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = ast.newRoleTypeDeclaration();
		role.setName(ast.newSimpleName("Y"));
		role.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		aTeam.bodyDeclarations().add(role);	
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("public team class X {\n");
		buf.append("\n");
		buf.append("    public class Y {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * Delete role from team
	 */
	public void test0002() throws Exception {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0002", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("public team class X {\n");
		buf.append("public class Y {\n");
		buf.append("}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		aTeam.bodyDeclarations().remove(0);	
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("public team class X {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * Replace role
	 */
	public void test0003() throws Exception {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0003", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("public team class X {\n");
		buf.append("	public class X {\n");
		buf.append("	}\n");
		buf.append("	public class Y {\n");
		buf.append("	}\n");
		buf.append("	public class Z {\n");
		buf.append("	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = ast.newRoleTypeDeclaration();
		role.setName(ast.newSimpleName("YY"));
		role.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		aTeam.bodyDeclarations().remove(1);
		aTeam.bodyDeclarations().add(1,role);
				
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("public team class X {\n");
		buf.append("	public class X {\n");
		buf.append("	}\n");
		buf.append("	public class YY {\n");
		buf.append("    }\n");
		buf.append("    public class Z {\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	// Insert a role with playedBy via modification and cloning.
	public void test0004() throws Exception {
		
		// generate some AST:
		String generated = 
		 	"public team class MyTeam {\n" + 
		 	"	protected class DisposeWatcher playedBy Item {\n" +
		 	"       void bar() { nop(); }\n" +
		 	"	}\n" + 
		 	"}\n";
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(generated.toCharArray());
		
		Block block = (Block) parser.createAST(null);
		TypeDeclarationStatement st = (TypeDeclarationStatement) block.statements().get(0);
		TypeDeclaration decl = (TypeDeclaration) st.getDeclaration();
		ASTNode[] myNodes = (ASTNode[]) decl.bodyDeclarations().toArray(new ASTNode[1]);
		
		// create a CU to copy into:
		String existing =
			"public team class MyTeam {\n" +
			"	protected class OtherRole {}\n" +
			"	void foo() { System.out.println(this); }\n" +
			"}\n";
		parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(existing.toCharArray());
		CompilationUnit _astCU = (CompilationUnit)parser.createAST(null);
		TypeDeclaration teamDecl = (TypeDeclaration)_astCU.types().get(0);
		
		// copy generated into CU:
		_astCU.recordModifications();
		teamDecl.bodyDeclarations().add(ASTNode.copySubtree(_astCU.getAST(), myNodes[0]));
		
		// apply change:
		Document doc = new Document(existing);
		TextEdit edits = _astCU.rewrite(doc, null);
		edits.apply(doc);
		
		// compare result with expected:
		String newSource = doc.get();
		String expected = 
			"public team class MyTeam {\n" +
			"	protected class OtherRole {}\n" +
			"	void foo() { System.out.println(this); }\n" +
		 	"    protected class DisposeWatcher playedBy Item {\n"+
		 	"        void bar() {\n" +
		 	"            nop();\n" +
		 	"        }\n" +
		 	"    }\n" +
			"}\n";
		assertEquals(expected, newSource);
	}
	
	// insert a role with playedBy and base predicate :
	public void test0005() throws Exception {
		
		// generate some AST:
		String generated = 
		 	"public team class MyTeam {\n" + 
		 	"	protected class DisposeWatcher playedBy Item \n" +
		 	"         base when (hasRole(base)) {\n" +
		 	"       void bar() { nop(); }\n" +
		 	"	}\n" + 
		 	"}\n";
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(generated.toCharArray());

		
		Block block = (Block) parser.createAST(null);
		TypeDeclarationStatement st = (TypeDeclarationStatement) block.statements().get(0);
		TypeDeclaration decl = (TypeDeclaration) st.getDeclaration();
		ASTNode[] myNodes = (ASTNode[]) decl.bodyDeclarations().toArray(new ASTNode[1]);
		
		// create a CU to copy into:
		String existing =
			"public team class MyTeam {\n" +
			"	protected class OtherRole {}\n" +
			"	void foo() { System.out.println(this); }\n" +
			"}\n";
		parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(existing.toCharArray());
		CompilationUnit _astCU = (CompilationUnit)parser.createAST(null);
		TypeDeclaration teamDecl = (TypeDeclaration)_astCU.types().get(0);
		
		// copy generated into CU:
		_astCU.recordModifications();
		teamDecl.bodyDeclarations().add(ASTNode.copySubtree(_astCU.getAST(), myNodes[0]));
		
		// apply change:
		Document doc = new Document(existing);
		TextEdit edits = _astCU.rewrite(doc, null);
		edits.apply(doc);
		
		// compare result with expected:
		String newSource = doc.get();
		String expected = 
			"public team class MyTeam {\n" +
			"	protected class OtherRole {}\n" +
			"	void foo() { System.out.println(this); }\n" +
		 	"    protected class DisposeWatcher playedBy Item\n" +
		 	"        base when (hasRole(base)) {\n" +
		 	"        void bar() {\n" +
		 	"            nop();\n" +
		 	"        }\n" +
		 	"    }\n" +
			"}\n";
		assertEquals(expected, newSource);
	}
	
	// insert a team with a predicate :
	public void test0006() throws Exception {
		
		// generate some AST:
		String generated = 
		 	"public team class MyTeam \n" + 
		 	"   when (isSunday(today())) {\n" +
		 	"       void bar() { nop(); }\n" +
		 	"}\n";
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(generated.toCharArray());

		
		Block block = (Block) parser.createAST(null);
		System.out.println(block);
		TypeDeclarationStatement st = (TypeDeclarationStatement) block.statements().get(0);
		TypeDeclaration decl = (TypeDeclaration) st.getDeclaration();
		
		// create a CU to copy into:
		String existing =
			"import javax.swing.JFrame;\n";
		parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(existing.toCharArray());
		CompilationUnit _astCU = (CompilationUnit)parser.createAST(null);
		
		// copy generated into CU:
		_astCU.recordModifications();
		_astCU.types().add(ASTNode.copySubtree(_astCU.getAST(), decl));
		
		// apply change:
		Document doc = new Document(existing);
		TextEdit edits = _astCU.rewrite(doc, null);
		edits.apply(doc);
		
		// compare result with expected:
		String newSource = doc.get();
		String expected = 
			"import javax.swing.JFrame;\n\n" +
		 	"public team class MyTeam\n" +
		 	"    when (isSunday(today())) {\n" +
		 	"    void bar() {\n" +
		 	"        nop();\n" +
		 	"    }\n" +
			"}\n";
		assertEquals(expected, newSource);
	}
	
	/**
	 * add team guard predicate
	 */
	public void test0009() throws Exception {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0009", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0009;\n");
		buf.append("public team class Team\n"); 
		buf.append("{\n");
		buf.append("	public class Role playedBy Base\n");
		buf.append("    {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);

		GuardPredicateDeclaration guard = ast.newGuardPredicateDeclaration();
		
		guard.setBase(true);
		
		InfixExpression expr = ast.newInfixExpression();
		NumberLiteral lhs = ast.newNumberLiteral();
		lhs.setToken("4");
		expr.setLeftOperand(lhs);
		NumberLiteral rhs = ast.newNumberLiteral();
		rhs.setToken("5");
		expr.setRightOperand(rhs);
		expr.setOperator(Operator.EQUALS);
		guard.setExpression(expr);

		aTeam.setGuardPredicate(guard);
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0009;\n");
		buf.append("public team class Team\n");
		buf.append("    base when (4 == 5)\n"); 
		buf.append("{\n");
		buf.append("	public class Role playedBy Base\n");
		buf.append("    {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * remove team guard predicate
	 */
	public void test0010() throws Exception {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0010", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0010;\n");
		buf.append("public team class Team\n");
		buf.append("    when (true)\n");
		buf.append("{\n");
		buf.append("	public class Role playedBy Base\n");
		buf.append("    {\n");
		buf.append("\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		
		aTeam.setGuardPredicate(null);
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0010;\n");
		buf.append("public team class Team\n");
		buf.append("{\n");
		buf.append("	public class Role playedBy Base\n");
		buf.append("    {\n");
		buf.append("\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
	
	
	/**
	 * changed team guard predicate
	 */
	public void test0011() throws Exception {		
	    IPackageFragment pack1= sourceFolder.createPackageFragment("test0011", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0011;\n");
		buf.append("public team class Team\n");
		buf.append("	when (true)\n");
		buf.append("{\n");
		buf.append("	public class Role playedBy Base\n");
		buf.append("	{\n");
		buf.append("\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		
		GuardPredicateDeclaration guard = aTeam.getGuardPredicate();
		
		guard.setBase(true);
		
		InfixExpression expr = ast.newInfixExpression();
		NumberLiteral lhs = ast.newNumberLiteral();
		lhs.setToken("23");
		expr.setLeftOperand(lhs);
		NumberLiteral rhs = ast.newNumberLiteral();
		rhs.setToken("23");
		expr.setRightOperand(rhs);
		expr.setOperator(Operator.NOT_EQUALS);
		guard.setExpression(expr);
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0011;\n");
		buf.append("public team class Team\n");
		buf.append("	base when (23 != 23)\n");
		buf.append("{\n");
		buf.append("	public class Role playedBy Base\n");
		buf.append("	{\n");
		buf.append("\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}

}
	