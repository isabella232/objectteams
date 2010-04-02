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
 * $Id: DeclaredLiftingTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel;

import junit.framework.Test;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.tests.StringBasedTest;

/**
 * @author kaschja
 */
//TODO (kaschja) create tests by the time declared lifting has been made visible to the OT-Model  
public class DeclaredLiftingTest extends StringBasedTest
{
	public DeclaredLiftingTest()
	{
		super("Declared Lifting Tests");
	}
	
	public static Test suite()
    {
        return new Suite(DeclaredLiftingTest.class);
    }
	
    /**
     * a team class with 
     * a bound role class and
     * a teamlevel-method with declared lifting
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        createProjectFolder("basepkg");
        createProjectFile(
            "/basepkg/SampleBase.java",
            "package basepkg;\n"
                    + "public class SampleBase \n"
                    + "{\n"
                    + "    public void baseMethod(String stringParameter) {}\n"
                    + "}\n");
        createProjectFolder("teampkg");
        createProjectFile(
            "/teampkg/SampleTeam.java",
            "import basepkg.SampleBase;\n"
                    + "package teampkg;\n"
                    + "public team class SampleTeam \n"
                    + "{\n"
                    + "    public void teamlevelMethod(SampleBase as SampleRole sampleParameter){}"
                    + "		public class SampleRole playedBy SampleBase{}\n"
                    + "}\n");
    }
      
    public void test1() throws JavaModelException
    {        
        IType baseJavaElem = getTestProject().findType("basepkg.SampleBase");
        
        IMethod[] baseMethods = baseJavaElem.getMethods();
        assertTrue(baseMethods.length == 1);
        
        IMethod method = baseMethods[0];
        assertNotNull(method);
        
        System.out.println("Base-Methode");
        System.out.println("number of parameters: " + method.getNumberOfParameters()); 
        
        System.out.println("parameter names:");  
        String[] paraNames = method.getParameterNames();
        for (int idx = 0; idx < paraNames.length; idx++)
        {
            System.out.println(paraNames[idx]);
        }
        
        System.out.println("parameter types:");  
        String[] paraTypes = method.getParameterTypes();
        for (int idx = 0; idx < paraTypes.length; idx++)
        {
            System.out.println(paraTypes[idx]);
        }
            
        System.out.println("signature: " + method.getSignature() + "\n");
        
        //*********************************************
        
        IType teamJavaElem = getTestProject().findType("teampkg.SampleTeam");
        
        IMethod[] teamLevelMethods = teamJavaElem.getMethods();
        assertTrue(teamLevelMethods.length == 1);
        
        method = teamLevelMethods[0];
        assertNotNull(method);
        
        System.out.println("Teamlevel-Methode");
        System.out.println("number of parameters: " + method.getNumberOfParameters()); 
        
        System.out.println("parameter names:");  
        paraNames = method.getParameterNames();
        for (int idx = 0; idx < paraNames.length; idx++)
        {
            System.out.println(paraNames[idx]);
        }
        
        System.out.println("parameter types:");  
        paraTypes = method.getParameterTypes();
        for (int idx = 0; idx < paraTypes.length; idx++)
        {
            System.out.println(paraTypes[idx]);
        }
            
        System.out.println("signature: " + method.getSignature() + "\n");
        
       System.out.println("toString: " + method.toString());
    }
}
