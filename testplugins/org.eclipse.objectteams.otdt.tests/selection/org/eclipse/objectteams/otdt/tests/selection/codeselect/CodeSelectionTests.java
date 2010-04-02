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
 * $Id: CodeSelectionTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.selection.codeselect;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.objectteams.otdt.tests.AbstractJavaModelTests;

/**
 * @author brcan
 */
public class CodeSelectionTests extends AbstractJavaModelTests
{
    public static Test suite()
    {
        if (false)
        {
            TestSuite suite = new Suite(CodeSelectionTests.class.getName());
            suite.addTest(new CodeSelectionTests("testLocalNameForClassFile"));
            return suite;
        }
        return new Suite(CodeSelectionTests.class);
    }

    public CodeSelectionTests(String name)
    {
        super(name);
    }

    public void setUpSuite() throws Exception
    {
        super.setUpSuite();

        setUpJavaProject("CodeSelection");
    }

    public void tearDownSuite() throws Exception
    {
        deleteProject("CodeSelection");

        super.tearDownSuite();
    }

//    public void testAbstractMethod() throws JavaModelException
//    {
//        ICompilationUnit cu = getCompilationUnit(
//                "Resolve",
//                "src",
//                "",
//                "ResolveAbstractMethod.java");
//        IJavaElement[] elements = codeSelect(cu, "foo", "foo");
//        assertElementsEqual(
//                "Unexpected elements",
//                "foo() [in SuperInterface [in SuperInterface.java [in <default> [in src [in Resolve]]]]]",
//                elements);
//    }

    //testing bound base classes
    public void testBaseclass1() throws JavaModelException
    {
        ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg1", "T1.java");
        IJavaElement[] elements = codeSelect(cu, "B1", "B1");
        assertElementsEqual(
                "Unexpected elements",
                "B1 [in B1.java [in basepkg [in src [in CodeSelection]]]]",
                elements);
    }
    
    public void testBaseclass2() throws JavaModelException
    {
        ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg1", "T2.java");
        IJavaElement[] elements = codeSelect(cu, "basepkg.B1", "basepkg.B1");
        assertElementsEqual(
                "Unexpected elements",
                "B1 [in B1.java [in basepkg [in src [in CodeSelection]]]]",
                elements);
    }
    
   public void testBaseclass3() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg1", "T3.java");
       IJavaElement[] elements = codeSelect(cu, "T1", "T1");
       assertElementsEqual(
               "Unexpected elements",
               "T1 [in T1.java [in teampkg1 [in src [in CodeSelection]]]]",
               elements);
   }
   
   public void testBaseclass4() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg1", "T4.java");
       IJavaElement[] elements = codeSelect(cu, "teampkg2.T0", "teampkg2.T0");
       assertElementsEqual(
               "Unexpected elements",
               "T0 [in T0.java [in teampkg2 [in src [in CodeSelection]]]]",
               elements);
   }
   
   public void testBaseclass5() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg1", "T5.java");
       IJavaElement[] elements = codeSelect(cu, "t4.R1", "t4.R1");
       assertElementsEqual(
               "Unexpected elements",
               "R1 [in T4 [in T4.java [in teampkg1 [in src [in CodeSelection]]]]]",
               elements);
   }

   public void testBaseclass6() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg1", "T6.java");
       IJavaElement[] elements = codeSelect(cu, "base.R1", "base.R1");
       assertElementsEqual(
               "Unexpected elements",
               "R1 [in T5 [in T5.java [in teampkg1 [in src [in CodeSelection]]]]]",              
               elements);
   }

   public void testBaseclass6b() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg1", "T6.java");
       IJavaElement[] elements = codeSelect(cu, "R1<@base>", "R1");
       assertElementsEqual(
               "Unexpected elements",
               "R1 [in T5 [in T5.java [in teampkg1 [in src [in CodeSelection]]]]]",
               elements);
   }

   public void testBaseclass6c() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg1", "T6.java");
       IJavaElement[] elements = codeSelect(cu, "R1<@TR6.base> {}", "R1");
       assertElementsEqual(
               "Unexpected elements",
               "R1 [in T5 [in T5.java [in teampkg1 [in src [in CodeSelection]]]]]",
               elements);
   }
   
   // select the superclass ref from one rofi to another
   public void testRoleSuperclass() throws JavaModelException 
   {
	   ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg2.T2", "R2.java");
       IJavaElement[] elements = codeSelect(cu, "R1", "R1");
       assertElementsEqual(
               "Unexpected elements",
               "R1 [in R1.java [in teampkg2.T2 [in src [in CodeSelection]]]]",
               elements);
   }
   
   //testing type declarations
   public void testTeamDeclaration() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg2", "T0.java");
       IJavaElement[] elements = codeSelect(cu, "T0", "T0");
       assertElementsEqual(
               "Unexpected elements",
               "T0 [in T0.java [in teampkg2 [in src [in CodeSelection]]]]",
               elements);
   }

   public void testNestedTeamDeclaration() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg2", "T1.java");
       IJavaElement[] elements = codeSelect(cu, "TR1", "TR1");
       assertElementsEqual(
               "Unexpected elements",
               "TR1 [in T1 [in T1.java [in teampkg2 [in src [in CodeSelection]]]]]",
               elements);
   }

   public void testInlinedRoleDeclaration() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg2", "T1.java");
       IJavaElement[] elements = codeSelect(cu, "R1 {", "R1");
       assertElementsEqual(
               "Unexpected elements",
               "R1 [in T1 [in T1.java [in teampkg2 [in src [in CodeSelection]]]]]",
               elements);
   }
   
   //TODO(gbr) test role declaration in role file.
   
   //testing method declarations
   public void testMethodDeclaration1() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg2", "T1.java");
       IJavaElement[] elements = codeSelect(cu, "t1m1()", "t1m1");
       assertElementsEqual(
               "Unexpected elements",
               "t1m1() [in T1 [in T1.java [in teampkg2 [in src [in CodeSelection]]]]]",
               elements);
   }

   public void testMethodDeclaration2() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg2", "T1.java");
       IJavaElement[] elements = codeSelect(cu, "tr1m1()", "tr1m1");
       assertElementsEqual(
               "Unexpected elements",
               "tr1m1() [in TR1 [in T1 [in T1.java [in teampkg2 [in src [in CodeSelection]]]]]]",
               elements);
   }

   public void testMethodDeclaration3() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg2", "T1.java");
       IJavaElement[] elements = codeSelect(cu, "r1m1(){/*roleMethod*/}", "r1m1");
       assertElementsEqual(
               "Unexpected elements",
               "r1m1() [in R1 [in T1 [in T1.java [in teampkg2 [in src [in CodeSelection]]]]]]",
               elements);
   }

   //testing field declarations
   public void testFieldDeclaration1() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg2", "T1.java");
       IJavaElement[] elements = codeSelect(cu, "t1_f", "t1_f");
       assertElementsEqual(
               "Unexpected elements",
               "t1_f [in T1 [in T1.java [in teampkg2 [in src [in CodeSelection]]]]]",
               elements);
   }

   public void testFieldDeclaration2() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg2", "T1.java");
       IJavaElement[] elements = codeSelect(cu, "tr1_f", "tr1_f");
       assertElementsEqual(
               "Unexpected elements",
               "tr1_f [in TR1 [in T1 [in T1.java [in teampkg2 [in src [in CodeSelection]]]]]]",
               elements);
   }

   public void testFieldDeclaration3() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "teampkg2", "T1.java");
       IJavaElement[] elements = codeSelect(cu, "r1_f;//roleField", "r1_f");
       assertElementsEqual(
               "Unexpected elements",
               "r1_f [in R1 [in T1 [in T1.java [in teampkg2 [in src [in CodeSelection]]]]]]",
               elements);
   }
   // actually a dup of https://bugs.eclipse.org/bugs/show_bug.cgi?id=204417
   public void _testRegression1() throws JavaModelException
   {
       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "regression", "Client1.java");
       IJavaElement[] elements = codeSelect(cu, "A(a.cs)", "A");
       assertElementsEqual(
               "Unexpected elements",
               "A [in A.java [in regression [in src [in CodeSelection]]]]",
               elements);
   }
 

	/**
	 * Select argument name inside a callin parameter mapping.
	 */
	public void testParamMapping1() throws JavaModelException
	{
	       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "mappings", "MappingsTeam1.java");
	       IJavaElement[] elements = codeSelect(cu, "i <- x,", "i");
	       assertElementsEqual(
	               "Unexpected elements",
	               "i [in rm1(int) [in R [in MappingsTeam1 [in MappingsTeam1.java [in mappings [in src [in CodeSelection]]]]]]]",
	               elements);
	}


	/**
	 * Select argument name inside a callout parameter mapping.
	 */
	public void testParamMapping2() throws JavaModelException
	{
	       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "mappings", "MappingsTeam1.java");
	       IJavaElement[] elements = codeSelect(cu, "x,//callout mapping", "x");
	       assertElementsEqual(
	               "Unexpected elements",
	               "x [in rm2(int) [in R [in MappingsTeam1 [in MappingsTeam1.java [in mappings [in src [in CodeSelection]]]]]]]",
	               // codeSelect cannot handle children of OTJavaElement :(, uses corresponding SourceMethod as parent
	               // "x [in rm2(int)->bm1(int) [in R [in MappingsTeam1 [in MappingsTeam1.java [in mappings [in src [in CodeSelection]]]]]]]",
	               elements);
	}
	
	/**
	 * Select method call inside a callout parameter mapping.
	 */
	public void testParamMapping3() throws JavaModelException 
	{
	       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "mappings", "MappingsTeam1.java");
	       IJavaElement[] elements = codeSelect(cu, "doubleIt(result)", "doubleIt");
	       assertElementsEqual(
	               "Unexpected elements",
	               "doubleIt(int) [in R [in MappingsTeam1 [in MappingsTeam1.java [in mappings [in src [in CodeSelection]]]]]]",
	               // codeSelect cannot handle children of OTJavaElement :(, uses corresponding SourceMethod as parent
	               // "x [in rm2(int)->bm1(int) [in R [in MappingsTeam1 [in MappingsTeam1.java [in mappings [in src [in CodeSelection]]]]]]]",
	               elements);		
	}

	/**
	 * Select base field reference inside a callout-to-field parameter mapping.
	 */
	public void testParamMapping4() throws JavaModelException 
	{
	       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "mappings", "MappingsTeam1.java");
	       IJavaElement[] elements = codeSelect(cu, "jon } // c-t-f", "jon");
	       assertElementsEqual(
	               "Unexpected elements",
	               "jon [in B1 [in B1.java [in basepkg [in src [in CodeSelection]]]]]",
	               elements);		
	}

	/**
	 * Select base field reference inside a callin parameter mapping.
	 */
	public void testParamMapping5() throws JavaModelException 
	{
	       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "mappings", "MappingsTeam1.java");
	       IJavaElement[] elements = codeSelect(cu, "jon } // callin", "jon");
	       assertElementsEqual(
	               "Unexpected elements",
	               "jon [in B1 [in B1.java [in basepkg [in src [in CodeSelection]]]]]",
	               elements);		
	}

	public void testInferredCallout1() throws JavaModelException
	{
	       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "mappings", "InferredCallouts.java");
	       IJavaElement[] elements = codeSelect(cu, "bm1(4)", "bm1");
	       assertElementsEqual(
	               "Unexpected elements",
	               "bm1(int) [in B1 [in B1.java [in basepkg [in src [in CodeSelection]]]]]",
	               elements);		
	}
	
	public void testInferredCallout2() throws JavaModelException
	{
	       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "mappings", "InferredCallouts.java");
	       IJavaElement[] elements = codeSelect(cu, "jon = 3L; // qualified version", "jon");
	       assertElementsEqual(
	               "Unexpected elements",
	               "jon [in B1 [in B1.java [in basepkg [in src [in CodeSelection]]]]]",
	               elements);		
	}
	
	public void testInferredCallout3() throws JavaModelException
	{
	       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "mappings", "InferredCallouts.java");
	       IJavaElement[] elements = codeSelect(cu, "jon; // callout-to-field read", "jon");
	       assertElementsEqual(
	               "Unexpected elements",
	               "jon [in B1 [in B1.java [in basepkg [in src [in CodeSelection]]]]]",
	               elements);		
	}
	
	public void testInferredCallout4() throws JavaModelException
	{
	       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "mappings", "InferredCallouts.java");
	       IJavaElement[] elements = codeSelect(cu, "jon = 3L; // unqualified version", "jon");
	       assertElementsEqual(
	               "Unexpected elements",
	               "jon [in B1 [in B1.java [in basepkg [in src [in CodeSelection]]]]]",
	               elements);		
	}
	
	public void testInferredCallout5() throws JavaModelException
	{
	       ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "mappings", "InferredCallouts.java");
	       IJavaElement[] elements = codeSelect(cu, "jon; // unqualified: callout-to-field read", "jon");
	       assertElementsEqual(
	               "Unexpected elements",
	               "jon [in B1 [in B1.java [in basepkg [in src [in CodeSelection]]]]]",
	               elements);		
	}

	public void testRoleCreation1()  throws JavaModelException 
	{
		ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "statements", "RoleCreation.java");
		IJavaElement[] elements = codeSelect(cu, "R<@t>(\"1\");", "R");
		assertElementsEqual(
					"Unexpected elements",
					"R(String) [in R [in RoleCreation [in RoleCreation.java [in statements [in src [in CodeSelection]]]]]]",
					elements);
	}

	public void testRoleCreation2()  throws JavaModelException 
	{
		ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "statements", "RoleCreation.java");
		IJavaElement[] elements = codeSelect(cu, "R(\"2\");", "R");
		assertElementsEqual(
					"Unexpected elements",
					"R(String) [in R [in RoleCreation [in RoleCreation.java [in statements [in src [in CodeSelection]]]]]]",
					elements);
	}
	
	// awaiting progress in Trac #192
	public void _testRoleCreation3()  throws JavaModelException 
	{
		ProblemRequestor problemRequestor = new ProblemRequestor();
		WorkingCopyOwner owner = newWorkingCopyOwner(problemRequestor);

		ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "statements", "RoleCreation.java");
		cu.getWorkingCopy(owner, null);
		IJavaElement[] elements = codeSelect(cu, "RProtected<@t>(\"3\");", "RProtected");
		assertProblems("Unexpected problems", 
						"----------\n" + 
						"1. ERROR in /CodeSelection/src/statements/RoleCreation.java\n" + 
						"Illegal parameterized use of non-public role RProtected (OTJLD 1.2.3(b)).\n" + 
						"----------\n" + 
						"2. ERROR in /CodeSelection/src/statements/RoleCreation.java\n" + 
						"Illegal parameterized use of non-public role RProtected (OTJLD 1.2.3(b)).\n" + 
						"----------\n", 
						problemRequestor);
		
		assertElementsEqual(
					"Unexpected elements",
					"RProtected(String) [in RProtected [in RoleCreation [in RoleCreation.java [in statements [in src [in CodeSelection]]]]]]",
					elements);
	}

	public void testRoleCreation4()  throws JavaModelException 
	{
		ICompilationUnit cu = getCompilationUnit("CodeSelection", "src", "statements", "RoleCreation.java");
		IJavaElement[] elements = codeSelect(cu, "RProtected(\"4\");", "RProtected");
		assertElementsEqual(
					"Unexpected elements",
					"RProtected(String) [in RProtected [in RoleCreation [in RoleCreation.java [in statements [in src [in CodeSelection]]]]]]",
					elements);
	}
}
