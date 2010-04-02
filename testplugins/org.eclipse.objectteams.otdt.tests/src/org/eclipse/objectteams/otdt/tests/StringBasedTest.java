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
 * $Id: StringBasedTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

/**
 * @author jwloka
 * @version $Id: StringBasedTest.java 23494 2010-02-05 23:06:44Z stephan $ 
 *
 * NOTE: Preliminary version of a test suite for testing the OTModel. The creation 
 * of test classes will be changed soon!   
 */
public class StringBasedTest extends ModifyingResourceTests
{
    public static final String TEST_PROJECT = "OTModelTest";

    private IJavaProject _testPrj;

    public StringBasedTest(String name)
    {
        super(name);
    }

    protected void createProjectFile(String path, String content)
        throws CoreException
    {
        super.createFile("/" + TEST_PROJECT + path, content);
    }

    protected void createProjectFolder(String folderName) throws CoreException
    {
        super.createFolder("/" + TEST_PROJECT + "/" + folderName);
    }

    protected IJavaProject getTestProject()
    {
        return _testPrj;
    }

    protected void setUp() throws Exception
    {
		super.setUp();
        _testPrj = createJavaProject(TEST_PROJECT, new String[]
        {
            ""
        }, "");
    }

    public void tearDown() throws Exception
    {
        deleteProject(TEST_PROJECT);
    }

    public void setTestProject(IJavaProject prj)
    {
        _testPrj = prj;
    }

    protected IOTType getOTElem(String name) throws JavaModelException
    {
       IType javaElem = getTestProject().findType(name);
       return OTModelManager.getOTElement(javaElem);       
    }
}