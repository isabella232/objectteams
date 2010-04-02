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
 * $Id: OTSubTypeHierarchyTest010.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.subhierarchy;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;
import org.eclipse.objectteams.otdt.core.TypeHelper;

/**
 * @author mkr
 *
 * $Id: OTSubTypeHierarchyTest010.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OTSubTypeHierarchyTest010 extends FileBasedModelTest
{
	
	private IType _focusType;
    private IType _T1;
    private IType _T2;
    private IType _T3;
    private IType _T1R1;
    private IType _T2R1;
    private IType _T3R1;
    private IType _interfaceA;
	private IType _objectType;
    
    
	public OTSubTypeHierarchyTest010(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSubTypeHierarchyTest010.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(OTSubTypeHierarchyTest010.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test010";
		
		_T1 = 
			getType(getTestProjectDir(),
					srcFolder,
					pkg,
					"T1");

        _T2 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "T2");
        
        _T3 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "T3");

        _interfaceA = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "InterfaceA");
        
        _objectType = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");

        _T1R1 = TypeHelper.findNestedRoleType(_T1, "T1.R1");
        _T2R1 = TypeHelper.findNestedRoleType(_T2, "T2.R1");
        _T3R1 = TypeHelper.findNestedRoleType(_T3, "T3.R1");

    }

    
	public void testCreation()
	{
		assertCreation(_T1R1);
        assertCreation(_T2R1);
        assertCreation(_T3R1);
        assertCreation(_interfaceA);
    }
    
    
    public void testGetSubtypes_T1R1() throws JavaModelException
    {
        _focusType = _T1R1;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getSubtypes(_focusType);
        IType[] expected = new IType[] { _T2R1 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetSubtypes_T2R1() throws JavaModelException
    {
        _focusType = _T2R1;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getSubtypes(_focusType);
        IType[] expected = new IType[] { _T3R1 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetSubtypes_interfaceA() throws JavaModelException
    {
        _focusType = _interfaceA;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getSubtypes(_focusType);
        IType[] expected = new IType[] { _T2R1, _T3R1 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

        
    public void testGetAllSubtypes_T1R1() throws JavaModelException
    {
        _focusType = _T1R1;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getAllSubtypes(_focusType);
        IType[] expected = new IType[] {
        								_T2R1,
										_T3R1
										};
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    
    public void testGetAllSubtypes_T2R1() throws JavaModelException
    {
        _focusType = _T2R1;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getAllSubtypes(_focusType);
        IType[] expected = new IType[] { _T3R1 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }
    
    public void testGetAllSubtypes_interfaceA() throws JavaModelException
    {
        _focusType = _interfaceA;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getAllSubtypes(_focusType);
        IType[] expected = new IType[] {
        								_T2R1,
										_T3R1
										};
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }
    
    public void testGetRootClasses_T1R1() throws JavaModelException
    {
        _focusType = _T1R1;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getRootClasses();
        IType[] expected = new IType[] { _objectType };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }
    
    public void testGetRootClasses_interfaceA() throws JavaModelException
    {
        _focusType = _interfaceA;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getRootClasses();
        IType[] expected = new IType[] { _objectType };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }
    
    public void testGetRootInterfaces_interfaceA() throws JavaModelException
    {
        _focusType = _interfaceA;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getRootInterfaces();
        IType[] expected = new IType[] { _focusType };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }
    
    
    public void testGetType_T1R1() throws JavaModelException
    {
        _focusType = _T1R1;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType actual = hierarchy.getType();
        IType expected = _focusType;
           
        assertTrue(compareTypes(expected, actual));        
    }
    
    public void testGetImplementingClasses_interfaceA() throws JavaModelException
    {
        _focusType = _interfaceA;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getImplementingClasses(_focusType);
        IType[] expected = new IType[] {
        								_T2R1,
										_T3R1
										};
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }
    

    public void testGetAllInterfaces_interfaceA() throws JavaModelException
    {
        _focusType = _interfaceA;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());


        IType[] actual = hierarchy.getAllInterfaces();
        IType[] expected = new IType[] { _interfaceA };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetAllClasses_T1R1() throws JavaModelException
    {
        _focusType = _T1R1;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getAllClasses();
        IType[] expected = new IType[] {
        								_T1R1,
										_T2R1,
										_T3R1,
										_objectType
										};
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetTSuperTypes_T1R1() throws JavaModelException
    {
        _focusType = _T1R1;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getTSuperTypes(_T3R1);
        IType[] expected = new IType[] { _T2R1 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    
    public void testGetExplicitSuperclass_T2R1_in_T2R1() throws JavaModelException
    {
        _focusType = _T2R1;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType actual = hierarchy.getExplicitSuperclass(_T2R1);
        IType expected = _objectType;
           
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetExplicitSuperclass_T3R1_in_T2R1() throws JavaModelException
    {
        _focusType = _T2R1;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType actual = hierarchy.getExplicitSuperclass(_T3R1);
        IType expected = _objectType;
           
        assertTrue(compareTypes(expected, actual));        
    }

    
    public void testGetExplicitSuperclass_T1R1_in_T2R1() throws JavaModelException
    {
        _focusType = _T2R1;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType actual = hierarchy.getExplicitSuperclass(_T1R1);
        IType expected = _objectType;
           
        assertTrue(compareTypes(expected, actual));        
    }
    
}
