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
 * $Id: OTTypeHierarchyBuilderTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.subhierarchy;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyBuilder;
import org.eclipse.jdt.internal.core.hierarchy.IndexBasedHierarchyBuilder;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * 
 * @author Michael Krueger (mkr)
 * @version $Id: OTTypeHierarchyBuilderTest.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTTypeHierarchyBuilderTest extends FileBasedModelTest 
{
	
	@SuppressWarnings("unused")
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
    
    
	public OTTypeHierarchyBuilderTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
			return new Suite(OTTypeHierarchyBuilderTest.class);
		@SuppressWarnings("unused")
		junit.framework.TestSuite suite = 
			new Suite(OTTypeHierarchyBuilderTest.class.getName());
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
 
	public void testGetResult_T20T10T00R0() throws CoreException
	{
	    _focusType = _T20T10T00R0;
	    TypeHierarchy hierarchy = new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
	    HierarchyBuilder builder = new IndexBasedHierarchyBuilder(hierarchy, SearchEngine.createJavaSearchScope(new IJavaElement[] {_focusType.getJavaProject()} ));

	    IType[] expected = { _T21T11T00R0, _T21T11T00R1, _T21T11T00R2 };
	    
	    builder.build(true);
	    IType[] actual = hierarchy.getAllSubtypes(_focusType);
	    
	    assertTrue(compareTypes(expected, actual));
	}

	public void testGetResult_T20T10T00R1() throws CoreException
	{
	    _focusType = _T20T10T00R1;
	    TypeHierarchy hierarchy = new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
	    HierarchyBuilder builder = new IndexBasedHierarchyBuilder(hierarchy, SearchEngine.createJavaSearchScope(new IJavaElement[] {_focusType.getJavaProject()} ));
	    
	    IType[] expected = { _T21T10T00R1, _T21T10T00R2, _T21T11T00R1, _T21T11T00R2 };

	    builder.build(true);
	    IType[] actual = hierarchy.getAllSubtypes(_focusType);
	    
	    assertTrue(compareTypes(expected, actual));
	}

	public void testBug411591() throws CoreException
	{
	    _focusType = javaProject.findType("java.lang.Object");
	    TypeHierarchy hierarchy = new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
	    HierarchyBuilder builder = new IndexBasedHierarchyBuilder(hierarchy, SearchEngine.createJavaSearchScope(new IJavaElement[] {_focusType.getJavaProject()} ));
   
	    builder.build(true);
	    IType[] actual = hierarchy.getAllSubtypes(_focusType);
	    assertNotNull(actual);
	    
	    // no real assert not throwing NPE is all we need to check
	}
	
}
