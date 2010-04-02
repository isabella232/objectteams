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
 * $Id: OTSuperTypeHierarchyTest002.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.superhierarchy;

import junit.framework.Test;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.tests.hierarchy.FileBasedHierarchyTest;

/**
 * @author Michael Krueger (mkr)
 * @version $Id: OTSuperTypeHierarchyTest002.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OTSuperTypeHierarchyTest002 extends FileBasedHierarchyTest
{
    
    private IType _T1;
    private IType _T2;
    private IType _T3;
	private IType _T1_R1;
	private IType _T2_R1;
	private IType _T3_R1;
    private IType _classA;
	private IType _objectType;
	
	public OTSuperTypeHierarchyTest002(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSuperTypeHierarchyTest002.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(OTSuperTypeHierarchyTest002.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test002";
		
        _T1 = getType(getTestProjectDir(), srcFolder, pkg, "T1");
        _T2 = getType(getTestProjectDir(), srcFolder, pkg, "T2");
        _T3 = getType(getTestProjectDir(), srcFolder, pkg, "T3");
        
		_T1_R1 = getRole(_T1, "T1.R1");
		_T2_R1 = getRole(_T2, "T2.R1");
        _T3_R1 = getRole(_T3, "T3.R1");

        _classA =  getType(getTestProjectDir(), srcFolder, pkg, "ClassA");

        _objectType = getType(getTestProjectDir(), "rt.jar", "java.lang", "Object");
	}
	    
    public void testCreation()
    {
        assertCreation(_T1);
        assertCreation(_T2);
        assertCreation(_T3);
        assertCreation(_T1_R1);
        assertCreation(_T2_R1);
        assertCreation(_T3_R1);
        assertCreation(_classA);
    }
    
	public void testGetAllSuperclasses_T3R1() throws JavaModelException
	{
		_focusType = _T3_R1;
        _testObj = createOTSuperTypeHierarchy(_focusType);
		
		IType[] expected = new IType[] { _objectType,
								         _T2_R1,
								         _T1_R1,
								         _classA };
		
		IType [] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}

	public void testGetAllSuperclasses_T2R1() throws JavaModelException
	{
		_focusType = _T2_R1;
		_testObj = createOTSuperTypeHierarchy(_focusType);

		IType[] expected = new IType[] { _objectType,
								         _T1_R1,
								         _classA };
		
		IType [] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);
		assertTrue(compareTypes(expected, actual));
	}
	
	public void testGetAllClasses_T2R1() throws JavaModelException
	{
		_focusType = _T2_R1;
		_testObj = createOTSuperTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] { _objectType,
								         _T1_R1,
										 _T2_R1,
								         _classA };
		
		IType [] actual = _testObj.getAllClasses();

        assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}

    public void testGetSuperclasses_T2R1() throws JavaModelException
    {
        _focusType = _T2_R1;
        _testObj = createOTSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T1_R1,
                                         _classA };
        
        IType [] actual = _testObj.getSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetExplicitSuperclass_T2R1() throws JavaModelException
    {
        _focusType = _T2_R1;
        _testObj = createOTSuperTypeHierarchy(_focusType);
        
        IType expected = _classA;
        
        IType actual = _testObj.getExplicitSuperclass(_focusType);

        assertTrue(compareTypes(expected, actual));
    }

    public void testGetExplicitSuperclass_T3R1() throws JavaModelException
    {
        _focusType = _T3_R1;
        _testObj = createOTSuperTypeHierarchy(_focusType);
        
        IType expected = _classA;
        
        IType actual = _testObj.getExplicitSuperclass(_focusType);

        assertTrue(compareTypes(expected, actual));
    }

	public void testGetAllSuperclasses_T1R1() throws JavaModelException
	{
		_focusType = _T1_R1;
		_testObj = createOTSuperTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] { _objectType };		
		IType [] actual =_testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}

	public void testGetAllTSuperTypes() throws JavaModelException
	{
		_focusType = _T3_R1;
		_testObj = createOTSuperTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] { _T2_R1,
		         						 _T1_R1 };
		IType [] actual = _testObj.getAllTSuperTypes(_focusType);
		assertEquals(expected.length, actual.length);
		
		assertTrue(compareTypes(expected, actual));
	}
	
}
