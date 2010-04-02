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
 * $Id: TypeDeclarationTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.ast;

import junit.framework.Test;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.tests.compiler.SourceElementRequestorTest;

/**
 * @author haebor
 * @version $Id: TypeDeclarationTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class TypeDeclarationTest extends SourceElementRequestorTest
{
    
    private boolean _testFlag;

    public TypeDeclarationTest(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(TypeDeclarationTest.class);
        }
        junit.framework.TestSuite suite = new Suite(TypeDeclarationTest.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        this.PROJECT_DIR = "ExternalDefinedRole";
        super.setUpSuite();
    }

    protected void setUp() throws Exception
    {
        super.setUp();
    }
	
	public void enterType(TypeInfo typeInfo) {
        _testFlag = typeInfo.isRoleFile;
	}
 
    public void testRoleInterface_RolefileFlag() throws JavaModelException
    {
        org.eclipse.jdt.core.ICompilationUnit unit = getCompilationUnit(
                "ExternalDefinedRole",
                "unbound",
                "unbound.teampkg.Team_5a",
                "SampleRole.java");
        
        String    src = unit.getSource();
        
	    String fileName = unit.getCorrespondingResource().toString();

	    fullParse(src, fileName);
	    
	    assertTrue(_testFlag);
    }

    public void testRoleClass_RolefileFlag() throws JavaModelException
    {
        org.eclipse.jdt.core.ICompilationUnit unit = getCompilationUnit(
                "ExternalDefinedRole",
                "unbound",
                "unbound.teampkg.Team_5b",
                "SampleRole.java");
        
        String    src = unit.getSource();
        IResource res = unit.getCorrespondingResource();        
                    
	    String fileName = res.toString();
	    fullParse(src, fileName);
	    
	    assertTrue(_testFlag);
    }

    
}
