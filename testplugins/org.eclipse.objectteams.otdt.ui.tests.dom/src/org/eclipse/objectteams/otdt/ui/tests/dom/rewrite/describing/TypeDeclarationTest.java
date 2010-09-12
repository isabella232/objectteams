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
 * $Id: TypeDeclarationTest.java 23496 2010-02-05 23:20:15Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom.rewrite.describing;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.tests.model.SuiteOfTestCases.Suite;
import org.eclipse.objectteams.otdt.ui.tests.dom.converter.RoleTypeDeclarationTest;

public class TypeDeclarationTest extends AstRewritingDescribingTest {
	private static final Class THIS = TypeDeclarationTest.class;

	public TypeDeclarationTest(String name) {
		super(name);
	}
	
	public static Test allTests() {
		return new Suite(THIS);
	}
	
	public static Test suite() {
		return allTests();
	}
	
	
	public void testRoleTypeDeclChanges1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T {\n");
		buf.append("    public class R extends java.util.List implements Runnable, Serializable playedBy String {\n");
		buf.append("    }\n");		
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST3(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		{  // change to protected team, rename type, rename supertype, rename first interface, rename base-class
			TypeDeclaration teamType= findTypeDeclaration(astRoot, "T");
			RoleTypeDeclaration roleType= findRoleTypeDeclaration(teamType, "R");

			// change flags
			rewrite.getListRewrite(roleType, RoleTypeDeclaration.MODIFIERS2_PROPERTY).replace(
												(Modifier)roleType.modifiers().get(0), 
												ast.newModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD), 
												null);

			// change to team
			rewrite.set(roleType, RoleTypeDeclaration.TEAM_PROPERTY, Boolean.TRUE, null);

			// change name
			SimpleName name= roleType.getName();
			SimpleName newName= ast.newSimpleName("R1");
			rewrite.replace(name, newName, null);
			
			Type superClass= roleType.getSuperclassType();
			assertTrue("Has super type", superClass != null);
			SimpleType newSuperclass= ast.newSimpleType(ast.newSimpleName("Object"));
			rewrite.replace(superClass, newSuperclass, null);

			List superInterfaces= roleType.superInterfaceTypes();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());
			SimpleType newSuperinterface= ast.newSimpleType(ast.newSimpleName("Cloneable"));
			rewrite.replace((ASTNode) superInterfaces.get(0), newSuperinterface, null);
			
			Type baseClass= roleType.getBaseClassType();
			assertTrue("Has base type", baseClass != null);
			SimpleType newBaseclass= ast.newSimpleType(ast.newSimpleName("System"));
			rewrite.replace(baseClass, newBaseclass, null);

		}

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public team class T {\n");
		buf.append("    protected team class R1 extends Object implements Cloneable, Serializable playedBy System {\n");
		buf.append("    }\n");		
		buf.append("}\n");			
		assertEqualString(preview, buf.toString());

	}
	public void testRoleTypeDeclChanges2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test2", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test2;\n");
		buf.append("public team class T {\n");
		buf.append("    public team class R1 implements Runnable, Serializable playedBy String {}\n");
		buf.append("    public class R2 extends Object implements Runnable {}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("T.java", buf.toString(), false, null);			

		CompilationUnit astRoot= createAST3(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		
		TypeDeclaration teamType= findTypeDeclaration(astRoot, "T");
		{  
			// change R1 to non-team, add supertype, remmove first interface, remove base-class
			RoleTypeDeclaration role1Type= findRoleTypeDeclaration(teamType, "R1");

			// change to non-team
			rewrite.set(role1Type, RoleTypeDeclaration.TEAM_PROPERTY, Boolean.FALSE, null);


			Type superClass= role1Type.getSuperclassType();
			assertTrue("Has no super type", superClass == null);
			SimpleType newSuperclass= ast.newSimpleType(ast.newSimpleName("Object"));
			rewrite.set(role1Type, RoleTypeDeclaration.SUPERCLASS_TYPE_PROPERTY, newSuperclass, null);

			List superInterfaces= role1Type.superInterfaceTypes();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());
			rewrite.getListRewrite(role1Type, RoleTypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY).remove((Type)superInterfaces.get(0), null);
			
			Type baseClass= role1Type.getBaseClassType();
			assertTrue("Has base type", baseClass != null);
			rewrite.remove(baseClass, null);

		}
		{  
			// change R2: remove supertype, add super-interface, add base-class
			RoleTypeDeclaration role2Type= findRoleTypeDeclaration(teamType, "R2");

			Type superClass= role2Type.getSuperclassType();
			assertTrue("Has super type", superClass != null);
			rewrite.remove(superClass, null);

			List superInterfaces= role2Type.superInterfaceTypes();
			assertTrue("Has super interfaces", !superInterfaces.isEmpty());
			Type newSuperInterface = ast.newSimpleType(ast.newSimpleName("Cloneable"));
			rewrite.getListRewrite(role2Type, RoleTypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY).insertLast(
							newSuperInterface, 
							null);
			
			Type baseClass= role2Type.getBaseClassType();
			assertTrue("Has no base type", baseClass == null);
			SimpleType newBaseclass= ast.newSimpleType(ast.newSimpleName("System"));
			rewrite.set(role2Type, RoleTypeDeclaration.BASECLASS_TYPE_PROPERTY, newBaseclass, null);

		}

		String preview= evaluateRewrite(cu, rewrite);
		
		buf= new StringBuffer();
		buf.append("package test2;\n");
		buf.append("public team class T {\n");
		buf.append("    public class R1 extends Object implements Serializable {}\n");
		buf.append("    public class R2 implements Runnable, Cloneable playedBy System {}\n");
		buf.append("}\n");			
		assertEqualString(preview, buf.toString());

	}
}
