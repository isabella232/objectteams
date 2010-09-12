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
 * $Id: OTSubTypeHierarchyTest004.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.hierarchy.OTTypeHierarchies;
import org.eclipse.objectteams.otdt.tests.hierarchy.FileBasedHierarchyTest;

/**
 * @author Michael Krueger (mkr)
 * @version $Id: OTSubTypeHierarchyTest004.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTSubTypeHierarchyTest004 extends FileBasedHierarchyTest 
{
	
	private ITypeHierarchy _testObj;
	private IType _focusType;
	@SuppressWarnings("unused")
	private IType _objectType;
	private IType _T20;
	private IType _T21;
	
	private IType _T20T10T00R0;
	private IType _T20T10T00R1;
	private IType _T21T10T00R1;
	private IType _T21T10T00R2;
	private IType _T21T11T00R0;
	private IType _T21T11T00R1;
	private IType _T21T11T00R2;
    
    
	public OTSubTypeHierarchyTest004(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSubTypeHierarchyTest004.class);
		}
		@SuppressWarnings("unused")
		junit.framework.TestSuite suite = 
			new Suite(OTSubTypeHierarchyTest004.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test004";
		
		_T20 = 
			getType(getTestProjectDir(),
					srcFolder,
					pkg,
					"T20");

        _T21 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "T21");
        
        
        _objectType = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");

        _T20T10T00R0 = TypeHelper.findNestedRoleType(_T20, "T20.T10.T00.R0");
        _T20T10T00R1 = TypeHelper.findNestedRoleType(_T20, "T20.T10.T00.R1");

        _T21T10T00R1 = TypeHelper.findNestedRoleType(_T21, "T21.T10.T00.R1");
        _T21T10T00R2 = TypeHelper.findNestedRoleType(_T21, "T21.T10.T00.R2");
        
        _T21T11T00R0 = TypeHelper.findNestedRoleType(_T21, "T21.T11.T00.R0");
        _T21T11T00R1 = TypeHelper.findNestedRoleType(_T21, "T21.T11.T00.R1");
        _T21T11T00R2 = TypeHelper.findNestedRoleType(_T21, "T21.T11.T00.R2");
    }

    
	public void testCreation()
	{
		assertCreation(_T20);
		assertCreation(_T21);
		
		assertCreation(_T20T10T00R0);
		assertCreation(_T20T10T00R1);

		assertCreation(_T21T10T00R1);
		assertCreation(_T21T10T00R2);
		
		assertCreation(_T21T11T00R0);
		assertCreation(_T21T11T00R1);
		assertCreation(_T21T11T00R2);
    }
 
	public void testGetAllSubtypes_T20T10T00R0() throws JavaModelException
	{
	    _focusType = _T20T10T00R0;
        _testObj = createTypeHierarchy(_focusType);

	    IType[] expected = { _T21T11T00R0, _T21T11T00R1, _T21T11T00R2 };
	    IType[] actual = _testObj.getAllSubtypes(_focusType);
	    
	    assertTrue(compareTypes(expected, actual));
	}

	public void testGetAllSubtypes_T20T10T00R1() throws JavaModelException
	{
	    _focusType = _T20T10T00R1;
	    TypeHierarchy hierarchy = new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
	    hierarchy.refresh(new NullProgressMonitor());

	    IType[] expected = { _T21T10T00R1, _T21T10T00R2, _T21T11T00R1, _T21T11T00R2 };
	    IType[] actual = hierarchy.getAllSubtypes(_focusType);
	    
	    assertTrue(compareTypes(expected, actual));
	}
	
	public void testGetSubtypes_T20T10T00R0() throws JavaModelException
	{
	    _focusType = _T20T10T00R0;
        _testObj = createTypeHierarchy(_focusType);

	    IType[] expected = {
	    					_T21T11T00R0 // indirect sub 
	    					};
	    IType[] actual = _testObj.getSubtypes(_focusType);
	    
	    assertEquals(expected.length, actual.length);
	    assertTrue(compareTypes(expected, actual));
	}
	
	public void testGetTSuperTypes_T21T11T00R0() throws JavaModelException
	{
	    _focusType = _T21T11T00R0;
	    TypeHierarchy hierarchy = new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
	    hierarchy.refresh(new NullProgressMonitor());

	    IType[] expected = { _T20T10T00R0 };
	    IType[] actual = OTTypeHierarchies.getInstance().getTSuperTypes(hierarchy, _focusType);
	    
	    assertTrue(compareTypes(expected, actual));
	}

	public void testGetTSuperTypes_T21T11T00R1() throws JavaModelException
	{
	    _focusType = _T21T11T00R1;
	    TypeHierarchy hierarchy = new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
	    hierarchy.refresh(new NullProgressMonitor());

	    IType[] expected = { _T21T10T00R1 };
	    IType[] actual = OTTypeHierarchies.getInstance().getTSuperTypes(hierarchy, _focusType);
	    
	    assertTrue(compareTypes(expected, actual));
	}

	
	
}
