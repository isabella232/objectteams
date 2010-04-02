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
 * $Id: OTSuperTypeHierarchyTest007.java 23494 2010-02-05 23:06:44Z stephan $
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
 * @author Michael Krueger (michael)
 * @version $Id: OTSuperTypeHierarchyTest007.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OTSuperTypeHierarchyTest007 extends FileBasedHierarchyTest
{
	private IType _T1;
    private IType _T2;
    
	private IType _T1_R1;
	private IType _T1_R2;
	
	private IType _T2_R1;
	private IType _T2_R2;
	
    private IType _classA;
    private IType _classB;
    
    private IType _interfaceA;
    private IType _interfaceB;
    private IType _interfaceC;
    
    private IType _objectType;
	
	public OTSuperTypeHierarchyTest007(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSuperTypeHierarchyTest007.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(OTSuperTypeHierarchyTest007.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test007";
		
		_T1 = getType(getTestProjectDir(), srcFolder, pkg, "T1");
		_T2 = getType(getTestProjectDir(), srcFolder, pkg, "T2");
		
		_T1_R1 = getRole(_T1, "T1.R1");
		_T1_R2 = getRole(_T1, "T1.R2");
		
		_T2_R1 = getRole(_T2, "T2.R1");
        _T2_R2 = getRole(_T2, "T2.R2");
		        
        _classA =
            getType(getTestProjectDir(), srcFolder, pkg, "ClassA");
        _classB =
            getType(getTestProjectDir(), srcFolder, pkg, "ClassB");
        _interfaceA =
            getType(getTestProjectDir(), srcFolder, pkg, "InterfaceA");
        _interfaceB =
            getType(getTestProjectDir(), srcFolder, pkg, "InterfaceB");
        _interfaceC =
            getType(getTestProjectDir(), srcFolder, pkg, "InterfaceC");
        _objectType =
            getType(getTestProjectDir(), "rt.jar", "java.lang", "Object");
	}
	
    
    public void testCreation()
    {
        assertCreation(_T1);
        assertCreation(_T2);

        assertCreation(_T1_R1);
        assertCreation(_T1_R2);

        assertCreation(_T2_R1);
        assertCreation(_T2_R2);
    }
    
	public void testGetAllSuperclassesFor_T2R1() throws JavaModelException
	{
		_focusType = _T2_R1;
        _testObj = createOTSuperTypeHierarchy(_focusType);

        IType[] expected = new IType[] { _objectType,
                                         _classB,
                                         _classA,
                                         _T1_R1 };
		IType [] actual = _testObj.getAllSuperclasses(_focusType);
        
		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}

	public void testGetAllSuperclassesFor_T2R2() throws JavaModelException
	{
		_focusType = _T2_R2;
        _testObj = createOTSuperTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] { _objectType, _T1_R2 };		
		IType [] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}
	
	
	public void testGetAllSuperInterfacesFor_T2R2() throws JavaModelException
	{
		_focusType = _T2_R2;
        _testObj = createOTSuperTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] {
                                         _interfaceA,
                                         _interfaceB,
                                         _interfaceC };		
		IType [] actual = _testObj.getAllSuperInterfaces(_focusType);
        
		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}
	
	public void testGetSuperInterfaces_T1R2() throws JavaModelException
	{
		_focusType = _T1_R2;
        _testObj = createOTSuperTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] { _interfaceA };
		IType [] actual = _testObj.getSuperInterfaces(_focusType);
        
		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}
	
	public void testGetAllSuperclassesFor_interfaceB() throws JavaModelException
	{
		_focusType = _interfaceB;
        _testObj = createOTSuperTypeHierarchy(_focusType);
        
		IType[] expected = new IType[0];		
		IType [] actual = _testObj.getAllSuperclasses(_focusType);
        
		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}
	
	public void testGetAllSuperInterfacesFor_interfaceB() throws JavaModelException
	{
		_focusType = _interfaceB;
        _testObj = createOTSuperTypeHierarchy(_focusType);		

        IType[] expected = new IType[] { _interfaceA };		
		IType [] actual = _testObj.getAllSuperInterfaces(_focusType);
        
		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}

	public void testGetAllSuperInterfacesFor_ClassA() throws JavaModelException
	{
		_focusType = _classA;
		_testObj = createOTSuperTypeHierarchy(_focusType);
		
		IType[] expected = new IType[0];
		IType [] actual = _testObj.getAllSuperInterfaces(_focusType);
		
		assertEquals(expected.length, actual.length);      
		assertTrue(compareTypes(expected, actual));
	}
	
    public void testGetSuperInterfaces_interfaceB_In_T2R2() throws JavaModelException
    {
        _focusType = _T2_R2;
        _testObj = createOTSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _interfaceA };        
        IType [] actual = _testObj.getSuperInterfaces(_interfaceB);
        
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    
	public void testGetAllSuperclassesFor_classB() throws JavaModelException
	{
		_focusType = _classB;
        _testObj = createOTSuperTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] { _objectType,
                                         _classA };		
		IType [] actual = _testObj.getAllSuperclasses(_focusType);
        
		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}
	
	public void testGetExplicitSuperclassFor_classB() throws JavaModelException
	{
		_focusType = _classB;
        _testObj = createOTSuperTypeHierarchy(_focusType);		

        IType expected = _classA;		
		IType actual = _testObj.getExplicitSuperclass(_focusType);

		assertTrue(compareTypes(expected, actual));
	}
	
	public void testGetAllInterfaces_T2R2() throws JavaModelException
	{
		_focusType = _T2_R2;
        _testObj = createOTSuperTypeHierarchy(_focusType);

        IType[] expected = new IType[] {
                                         _interfaceA,
                                         _interfaceB,
                                         _interfaceC };
		IType [] actual = _testObj.getAllInterfaces();
        
		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}

    public void testGetImplementingClasses_interfaceA_in_T2R2() throws JavaModelException
    {
        _focusType = _T2_R2;
        _testObj = createOTSuperTypeHierarchy(_focusType);      

        IType[] expected = new IType[] { _T1_R2, _T2_R2 };     
        IType [] actual = _testObj.getImplementingClasses(_interfaceA);
        
        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }
    
}
