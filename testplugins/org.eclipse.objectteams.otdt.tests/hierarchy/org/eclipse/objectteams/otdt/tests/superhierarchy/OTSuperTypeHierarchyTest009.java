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
 * $Id: OTSuperTypeHierarchyTest009.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.objectteams.otdt.core.TypeHelper;

/**
 * 
 * @author michael
 * @version $Id: OTSuperTypeHierarchyTest009.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTSuperTypeHierarchyTest009 extends FileBasedModelTest {
	
	@SuppressWarnings("unused")
	private ITypeHierarchy _testObj;
	private IType _focusType;
    private IType _T10;
    private IType _T11;    
	private IType _objectType;
    private IType _T10T00;
    private IType _T11T00;
    private IType _T10T00R0;
    private IType _T11T00R0;
    
    
    
	public OTSuperTypeHierarchyTest009(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSuperTypeHierarchyTest009.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(OTSuperTypeHierarchyTest009.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test009";
		
		_T10 = 
			getType(getTestProjectDir(),
					srcFolder,
					pkg,
					"T10");

        _T11 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "T11");
        
        
        _objectType = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");

        _T10T00R0 = TypeHelper.findNestedRoleType(_T10, "T10.T00.R0");
        _T11T00R0 = TypeHelper.findNestedRoleType(_T11, "T11.T00.R0");
        _T10T00 = TypeHelper.findNestedRoleType(_T10, "T10.T00");
        _T11T00 = TypeHelper.findNestedRoleType(_T11, "T11.T00");

    }

    
	public void testCreation()
	{
		assertCreation(_T10);
        assertCreation(_T11);
        assertCreation(_T10T00);
        assertCreation(_T11T00);
        assertCreation(_T10T00R0);
        assertCreation(_T11T00R0);

        
    }

    
    public void testGetTSuperTypes_T11T00R0() throws JavaModelException
    {
        _focusType = _T11T00R0;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getTSuperTypes(_focusType);
        IType[] expected = new IType[] { _T10T00R0 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    
    
    public void testGetSuperclass_T11T00R0() throws JavaModelException
    {
        _focusType = _T11T00R0;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        boolean result = false;
        try
		{
		        hierarchy.getSuperclass(_focusType);
		}
        catch(Exception exc)
		{
        	result = exc instanceof UnsupportedOperationException;
		}
        assertTrue(result);
    }

    
	public void testGetAllSuperclasses_T11T00R0() throws JavaModelException
    {
        _focusType = _T11T00R0;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getAllSuperclasses(_focusType);
        IType[] expected = new IType[] { _objectType,
                                         _T10T00R0,
                                       };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    
    public void testGetSuperInterfaces_T11T00R0() throws JavaModelException
    {
        _focusType = _T11T00R0;
        
        OTTypeHierarchy hierarchy =
            new OTTypeHierarchy(_focusType, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getSuperInterfaces(_focusType);
        IType[] expected = new IType[0];
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    
    
    
    
    
    
}
