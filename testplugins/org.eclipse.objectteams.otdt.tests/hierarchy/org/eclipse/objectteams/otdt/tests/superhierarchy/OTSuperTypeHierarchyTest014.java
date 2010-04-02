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
 * $Id: OTSuperTypeHierarchyTest014.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.superhierarchy;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * 
 * @author michael
 * @version $Id: OTSuperTypeHierarchyTest014.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTSuperTypeHierarchyTest014 extends FileBasedModelTest {
	
	private ITypeHierarchy _testObj;
	private IType _focusType;
    private IType _T1;
    private IType _T1R1;
    private IType _C1;    
	@SuppressWarnings("unused")
	private IType _javaLangObject;
    private IType _I1;
    private IType _I2;
        
    
	public OTSuperTypeHierarchyTest014(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSuperTypeHierarchyTest014.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(OTSuperTypeHierarchyTest014.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test014";
		
		_T1 = 
			getType(getTestProjectDir(),
					srcFolder,
					pkg,
					"T1");

        _C1 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "C1");
        
        _I1 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "I1");

        _I2 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "I2");

        
        _javaLangObject = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");

        _T1R1 = getRole(_T1, "T1.R1");
    }
	
	public void testCreation()
	{
		assertCreation(_T1);
        assertCreation(_C1);
        assertCreation(_T1R1);
        assertCreation(_I1);
        assertCreation(_I2);
    }

	
	public void testGetAllSuperInterfaces_C1() throws JavaModelException
    {
        _focusType = _C1;
        
        _testObj = new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), false);        
        _testObj.refresh(new NullProgressMonitor());

        IType[] actual = _testObj.getAllSuperInterfaces(_focusType);
        IType[] expected = new IType[] { _I1, _I2 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    
	public void testGetAllSuperInterfaces_T1R1() throws JavaModelException
    {
        _focusType = _T1R1;
        
        _testObj = new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), false);
        _testObj.refresh(new NullProgressMonitor());

        IType[] actual = _testObj.getAllSuperInterfaces(_focusType);
        IType[] expected = new IType[] { _I1, _I2 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
	
	public void testGetAllSuperInterfaces_I1() throws JavaModelException
    {
        _focusType = _I1;
        
        _testObj = new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), false);
        _testObj.refresh(new NullProgressMonitor());

        IType[] actual = _testObj.getAllSuperInterfaces(_focusType);
        IType[] expected = new IType[] { _I2 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

	
}
