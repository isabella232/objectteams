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
 * $Id: ASTRewritingModifyingCalloutMappingDeclarationTest.java 23496 2010-02-05 23:20:15Z stephan $
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
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodBindingOperator;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTNodeCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

// TODO(jsv) create own OT package for this test
public class ASTRewritingModifyingCalloutMappingDeclarationTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingCalloutMappingDeclarationTest.class;
	
	public ASTRewritingModifyingCalloutMappingDeclarationTest(String name) {
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
		suite.addTest(new ASTRewritingModifyingCalloutMappingDeclarationTest("test0009"));
		return suite;
	}
	
	/**
	 * add 2 parameter mappings to calloutMappingDeclaration without parameter mappings
	 */
	public void test0001() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0001", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) -> String baseMethod(String s2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		Expression expression1 = ASTNodeCreator.createExpression(ast,"s1+\"Test\"");
		ParameterMapping mapping1 = ASTNodeCreator.createParameterMapping(ast,expression1,ast.newSimpleName("s2"),"->",false);
		Expression expression2 = ASTNodeCreator.createExpression(ast,"result+\"addOnString\""); 	
		ParameterMapping mapping2 = ASTNodeCreator.createParameterMapping(ast,expression2,ast.newSimpleName("result"),"<-",false);
		
		CalloutMappingDeclaration callout = (CalloutMappingDeclaration) role.bodyDeclarations().get(0); 
		callout.getParameterMappings().add(mapping1);
		callout.getParameterMappings().add(mapping2);
		
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) -> String baseMethod(String s2) with {\n");
		buf.append("            s1 + \"Test\" -> s2,\n");
		buf.append("            result <- result + \"addOnString\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * delete all parameter mappings from calloutMappingDeclaration
	 */
	public void test0002() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) -> String baseMethod(String s2) with {\n");
		buf.append("            s1 + \"Test\" -> s2,\n");
		buf.append("            result <- result + \"addOnString\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CalloutMappingDeclaration callout = (CalloutMappingDeclaration) role.bodyDeclarations().get(0);
		callout.getParameterMappings().remove(1);
		callout.getParameterMappings().remove(0);
				
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) -> String baseMethod(String s2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * add one parameter mapping
	 */
	public void test0003() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0003", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) -> String baseMethod(String s2) with {\n");
		buf.append("            s1 + \"Test\" -> s2\n");
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
		ParameterMapping mapping1 = ASTNodeCreator.createParameterMapping(ast,expression1,ast.newSimpleName("result"),"<-",false);
		
		CalloutMappingDeclaration callout = (CalloutMappingDeclaration) role.bodyDeclarations().get(0); 
		callout.getParameterMappings().add(mapping1);
				
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) -> String baseMethod(String s2) with {\n");
		buf.append("            s1 + \"Test\" -> s2,\n");
		buf.append("            result <- result + \"addOnString\"\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}

	/**
	 * change callout kind
	 */
	public void test0004() throws MalformedTreeException, JavaModelException, BadLocationException {		
	    IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0004", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0004;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) -> String baseMethod(String s2) with {\n");
		buf.append("            s1 + \"Test\" -> s2\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
			
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		
		CalloutMappingDeclaration callout = (CalloutMappingDeclaration) role.bodyDeclarations().get(0); 
		callout.bindingOperator().setBindingKind(MethodBindingOperator.KIND_CALLOUT_OVERRIDE);
				
		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0004;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) => String baseMethod(String s2) with {\n");
		buf.append("            s1 + \"Test\" -> s2\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
	
	/**
	 * change role and base methodSpec in calloutMappingDeclaration 
	 */
	public void test0005() throws MalformedTreeException, JavaModelException, BadLocationException {		
        //FIXME(jsv): Handling of MethodSpecs needs to be implemented in Parser!
        //fail("Feature not yet implemented!");
                
        IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0005", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0005;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String roleMethod(String s1) -> String baseMethod(String s2) with {\n");
		buf.append("            s1 + \"Test\" -> s2\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		ICompilationUnit cu= pack1.createCompilationUnit("Team.java", buf.toString(), false, null);
		
		CompilationUnit astRoot= createCU(cu, false);
		
		astRoot.recordModifications();
		
		AST ast = astRoot.getAST();
		
		TypeDeclaration aTeam = (TypeDeclaration) astRoot.types().get(0);
		RoleTypeDeclaration role = (RoleTypeDeclaration) aTeam.bodyDeclarations().get(0);
		CalloutMappingDeclaration callout = (CalloutMappingDeclaration) role.bodyDeclarations().get(0); 
		
		List argList1 = new ArrayList();
		argList1.add(ASTNodeCreator.createArgument(ast,0,ASTNodeCreator.createType(ast, "String"),"s1",0,null));
		    
		List argList2 = new ArrayList();
		argList2.add(ASTNodeCreator.createArgument(ast,0,ASTNodeCreator.createType(ast, "String"),"s2",0,null));    
		
		MethodSpec methodSpec1 = ASTNodeCreator.createMethodSpec(
		        ast,
		        "newRoleMethod",
		        ASTNodeCreator.createType(ast, "String"),
		        argList1,
		        true);
		
		MethodSpec methodSpec2 = ASTNodeCreator.createMethodSpec(
		        ast,
		        "newBaseMethod",
		        ASTNodeCreator.createType(ast, "String"),
		        argList2,
		        true);
		        
		callout.setRoleMappingElement(methodSpec1);
		callout.setBaseMappingElement(methodSpec2);
		callout.bindingOperator().setBindingKind(MethodBindingOperator.KIND_CALLOUT_OVERRIDE);

		String preview = evaluateRewrite(cu.getSource(), astRoot);
		
		buf= new StringBuffer();
		buf.append("package test0005;\n");
		buf.append("public team class Team {\n");
		buf.append("	public class Role playedBy Base {\n");
		buf.append("        String newRoleMethod(String s1) => String newBaseMethod(String s2) with {\n");
		buf.append("            s1 + \"Test\" -> s2\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		
		assertEqualString(preview, buf.toString());
	}
}
	