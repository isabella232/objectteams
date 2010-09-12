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
 * $Id: OTSubTypeHierarchyTest016.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.hierarchy.OTTypeHierarchies;

/**
 * 
 * @author Michael Krueger (mkr)
 * @version $Id: OTSubTypeHierarchyTest016.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTSubTypeHierarchyTest016 extends FileBasedModelTest {
	
	@SuppressWarnings("unused")
	private ITypeHierarchy _testObj;
	private IType _focusType;
    private IType _C1;
    private IType _I1;
    private IType _T1;
    private IType _T1R1;
    private IType _T1R2;
    private IType _T1R3;
    private IType _T1R4;
    private IType _T1R5;
    private IType _T2;
    private IType _T2R1;
    private IType _T2R2;
    private IType _T2R3;
    private IType _T2R4;
    
    @SuppressWarnings("unused")
	private IType _objectType;
    
    
	public OTSubTypeHierarchyTest016(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSubTypeHierarchyTest016.class);
		}
		@SuppressWarnings("unused")
		junit.framework.TestSuite suite = 
			new Suite(OTSubTypeHierarchyTest016.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test016";

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

        _objectType = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");

        _T1R1 = TypeHelper.findNestedRoleType(_T1, "T1.R1");
        _T1R2 = TypeHelper.findNestedRoleType(_T1, "T1.R2");
        _T1R3 = TypeHelper.findNestedRoleType(_T1, "T1.R3");
        _T1R4 = TypeHelper.findNestedRoleType(_T1, "T1.R4");
        _T1R5 = TypeHelper.findNestedRoleType(_T1, "T1.R5");
        _T2R1 = TypeHelper.findNestedRoleType(_T2, "T2.R1");
        _T2R2 = TypeHelper.findNestedRoleType(_T2, "T2.R2");
        _T2R3 = TypeHelper.findNestedRoleType(_T2, "T2.R3");
        _T2R4 = TypeHelper.findNestedRoleType(_T2, "T2.R4");

    }

    
	public void testCreation()
	{
        assertCreation(_C1);
        assertCreation(_T1);
        assertCreation(_T1R1);
        assertCreation(_T1R2);
        assertCreation(_T1R3);
        assertCreation(_T1R4);
        assertCreation(_T2);
        assertCreation(_T2R1);
        assertCreation(_T2R2);
        assertCreation(_T2R3);
        assertCreation(_T2R4);
    }
    
    public void testGetSubtypes_C1() throws JavaModelException
    {
        _focusType = _C1;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getSubtypes(_focusType);
        IType[] expected = new IType[] { _T1R5 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    
    public void testGetSubtypes_T1R1() throws JavaModelException
    {
        _focusType = _T1R1;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getSubtypes(_focusType);
        IType[] expected = new IType[] { _T1R2, _T2R1 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetSubtypes_T1R2() throws JavaModelException
    {
        _focusType = _T1R2;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getSubtypes(_focusType);
        IType[] expected = new IType[] { _T1R3, _T2R2 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetSubtypes_T1R3() throws JavaModelException
    {
        _focusType = _T1R3;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getSubtypes(_focusType);
        IType[] expected = new IType[] { _T1R4, _T2R3 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    
    public void testGetSubtypes_T1R4() throws JavaModelException
    {
        _focusType = _T1R4;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getSubtypes(_focusType);
        IType[] expected = new IType[] { _T2R4 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    
    public void testGetExplicitSuperclass_T1R2() throws JavaModelException
    {
        _focusType = _T1R2;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(hierarchy, _focusType);
        IType expected = _T1R1;
   
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetExplicitSuperclass_T1R3() throws JavaModelException
    {
        _focusType = _T1R3;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(hierarchy, _focusType);
        IType expected = _T1R2;
   
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetExplicitSuperclass_T1R4() throws JavaModelException
    {
        _focusType = _T1R4;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(hierarchy, _focusType);
        IType expected = _T1R3;
   
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetExplicitSuperclass_T1R5() throws JavaModelException
    {
        _focusType = _T1R5;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(hierarchy, _focusType);
        IType expected = _C1;

        String errorMsg = _focusType.getElementName()
                          + " extends "
                          + expected.getElementName()
                          + " not in TypeHierarchy!";
        
        assertTrue(errorMsg, compareTypes(expected, actual));        
    }

    public void testTypeHierarchy_GetSuperclass_T1R5() throws JavaModelException
    {
        _focusType = _T1R5;
        
        
        ITypeHierarchy hierarchy = _focusType.newSupertypeHierarchy(new NullProgressMonitor());

        IType actual = hierarchy.getSuperclass(_focusType);
        IType expected = _C1;
   
        String errorMsg = _focusType.getElementName()
                          + " extends "
                          + expected.getElementName()
                          + " not in TypeHierarchy!";

        assertTrue(errorMsg, compareTypes(expected, actual));        
    }
    
    
    public void testGetSuperInterfaces_T1R5() throws JavaModelException
    {
        _focusType = _T1R5;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getSuperInterfaces(_focusType);
        IType[] expected = new IType[] { _I1 };
   
        assertTrue(compareTypes(expected, actual));        
    }

    
    public void testGetTSuperTypes_T2R1() throws JavaModelException
    {
        _focusType = _T2R1;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = OTTypeHierarchies.getInstance().getTSuperTypes(hierarchy, _focusType);
        IType[] expected = new IType[] {_T1R1 };
   
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetTSuperTypes_T2R2() throws JavaModelException
    {
        _focusType = _T2R2;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = OTTypeHierarchies.getInstance().getTSuperTypes(hierarchy, _focusType);
        IType[] expected = new IType[] {_T1R2 };
   
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetTSuperTypes_T2R3() throws JavaModelException
    {
        _focusType = _T2R3;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = OTTypeHierarchies.getInstance().getTSuperTypes(hierarchy, _focusType);
        IType[] expected = new IType[] {_T1R3 };
   
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetTSuperTypes_T2R4() throws JavaModelException
    {
        _focusType = _T2R4;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = OTTypeHierarchies.getInstance().getTSuperTypes(hierarchy, _focusType);
        IType[] expected = new IType[] {_T1R4 };
   
        assertTrue(compareTypes(expected, actual));        
    }
 
}
