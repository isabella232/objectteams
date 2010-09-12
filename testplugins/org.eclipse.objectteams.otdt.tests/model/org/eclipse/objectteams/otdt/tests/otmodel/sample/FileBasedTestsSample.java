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
 * $Id: FileBasedTestsSample.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.sample;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author jwloka
 * @version $Id: FileBasedTestsSample.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * This test case shows how to use certain test data from the virtual workspace.
 */
public class FileBasedTestsSample extends FileBasedModelTest
{
    public FileBasedTestsSample(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(FileBasedTestsSample.class);
        }
        junit.framework.TestSuite suite = new Suite(FileBasedTestsSample.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("TestDataSample");
        
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
    }
    
    public void test001() throws JavaModelException
    {
        ICompilationUnit unit = getCompilationUnit(
            "TestDataSample",
            "src",
            "test0001",
            "TestJava.java");
        IType type = unit.getType("Test");
        assertNotNull(type);
        assertEquals("Test", type.getElementName());
    }
}
