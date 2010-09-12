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
 * $Id: ASTRewritingModifyingRoleTest.java 23496 2010-02-05 23:20:15Z stephan $
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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.GuardPredicateDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

// TODO(jsv) create own OT package for this test
public class ASTRewritingModifyingRoleTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingRoleTest.class;
	
	public ASTRewritingModifyingRoleTest(String name) {
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
		suite.addTest(new ASTRewritingModifyingRoleTest("test0009"));
		return suite;
	}
	
	/**
	 * change name of role
	 */
	public void test0001() throws Exception {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0001", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		role.setName(ast.newSimpleName("NewRole"));
			
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class NewRole playedBy Base {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * delete base class
	 */
	public void test0002() throws Exception {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		role.setBaseClassType(null);
			
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * add base class
	 */
	public void test0003() throws Exception {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0003", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		role.setBaseClassType(ast.newSimpleType(ast.newName(new String[] {"Base"})));
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * change base class
	 */
	public void test0004() throws Exception {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0004", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0004;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		role.setBaseClassType(ast.newSimpleType(ast.newName(new String[] {"NewBase"})));
			
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0004;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy NewBase {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * add calloutmappingdeclaration
	 */
	public void test0005() throws Exception {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0005", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0005;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		
		CalloutMappingDeclaration callout = ast.newCalloutMappingDeclaration();
		MethodSpec baseMethodSpec = ast.newMethodSpec();
		MethodSpec roleMethodSpec = ast.newMethodSpec();
		
		baseMethodSpec.setName(ast.newSimpleName("baseMethodSpec"));
		roleMethodSpec.setName(ast.newSimpleName("roleMethodSpec"));
		baseMethodSpec.setSignatureFlag(true);
		baseMethodSpec.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		roleMethodSpec.setSignatureFlag(true);
		roleMethodSpec.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		
		callout.setBaseMappingElement(baseMethodSpec);
		callout.setRoleMappingElement(roleMethodSpec);
		callout.setSignatureFlag(true);		
			
		role.bodyDeclarations().add(callout);
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0005;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("\n");
		buf.append("        void roleMethodSpec() -> void baseMethodSpec();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * remove calloutMappingDeclaration
	 */ 
	public void test0006() throws Exception {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0006", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0006;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("\n");
		buf.append("        void roleMethodSpec() -> void baseMethodSpec();\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		role.bodyDeclarations().remove(0);
				
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0006;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * add callinmappingdeclaration
	 */
	public void test0007() throws Exception {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0007", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0007;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		
		CallinMappingDeclaration callin = ast.newCallinMappingDeclaration();
		
		MethodSpec baseMethodSpec = ast.newMethodSpec();
		MethodSpec roleMethodSpec = ast.newMethodSpec();
		
		baseMethodSpec.setName(ast.newSimpleName("baseMethodSpec"));
		roleMethodSpec.setName(ast.newSimpleName("roleMethodSpec"));
		baseMethodSpec.setSignatureFlag(true);
		baseMethodSpec.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		roleMethodSpec.setSignatureFlag(true);
		roleMethodSpec.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		
		callin.getBaseMappingElements().add(baseMethodSpec);
		callin.setRoleMappingElement(roleMethodSpec);
		
		callin.setCallinModifier(Modifier.OT_BEFORE_CALLIN);
		role.bodyDeclarations().add(callin);
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0007;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("\n");
		buf.append("        void roleMethodSpec() <- before void baseMethodSpec();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * remove callinmappingdeclaration
	 */
	public void test0008() throws Exception {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0008", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0008;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("\n");
		buf.append("        void roleMethodSpec() <- before void baseMethodSpec();\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		
		role.bodyDeclarations().remove(0);
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0008;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
	/**
	 * add role guard predicate
	 */
	public void test0009() throws Exception {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0009", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0009;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base\n");
		buf.append("    {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		
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

		role.setGuardPredicate(guard);
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0009;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base\n");
		buf.append("        base when (4 == 5)\n"); 
		buf.append("    {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * remove role guard predicate
	 */
	public void test0010() throws Exception {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0010", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0010;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base \n");
		buf.append("        when (true)\n");
		buf.append("    {\n");
		buf.append("\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		
		role.setGuardPredicate(null);
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0010;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base\n");
		buf.append("    {\n");
		buf.append("\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
	
	
	/**
	 * changed role guard predicate
	 */
	public void test0011() throws Exception {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0011", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0011;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base\n");
		buf.append("		when (true)\n");
		buf.append("	{\n");
		buf.append("\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		
		GuardPredicateDeclaration guard = role.getGuardPredicate();
		
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
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base\n");
		buf.append("		base when (23 != 23)\n");
		buf.append("	{\n");
		buf.append("\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
}
	