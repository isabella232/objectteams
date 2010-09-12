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
 * $Id: OTSuperTypeHierarchyTest013.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.hierarchy.OTTypeHierarchies;

/**
 * 
 * @author Michael Krueger (mkr)
 * @version $Id: OTSuperTypeHierarchyTest013.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTSuperTypeHierarchyTest013 extends FileBasedModelTest
{
	
	private IType _focusType;
	private IType _objectType;
    private IType _TA1;
    private IType _TA2;    
    private IType _TA1TB2TC2R1;
	private IType _TA1TB2TC1R1;
	private IType _TA1TB1TC2R1;
	private IType _TA1TB1TC1R1;
    private IType _TA2TB2TC2R1;
    private IType _TA2TB2TC1R1;
	private IType _TA2TB1TC2R1;
	private IType _TA2TB1TC1R1;
    private IType _C0;
	private IType _C1;
    private IType _C2;
    private IType _C3;
    
	public OTSuperTypeHierarchyTest013(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSuperTypeHierarchyTest013.class);
		}
		@SuppressWarnings("unused")
		junit.framework.TestSuite suite = 
			new Suite(OTSuperTypeHierarchyTest013.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test013";
		
		_TA1 = 
			getType(getTestProjectDir(),
					srcFolder,
					pkg,
					"TA1");

        _TA2 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "TA2");

        _C0 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "C0");
        
        _C1 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "C1");

        _C2 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "C2");
        
        _C3 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "C3");

        _objectType = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");

        _TA1TB2TC2R1 = TypeHelper.findNestedRoleType(_TA1, "TA1.TB2.TC2.R1");
        _TA1TB2TC1R1 = TypeHelper.findNestedRoleType(_TA1, "TA1.TB2.TC1.R1");
        _TA1TB1TC2R1 = TypeHelper.findNestedRoleType(_TA1, "TA1.TB1.TC2.R1");
        _TA1TB1TC1R1 = TypeHelper.findNestedRoleType(_TA1, "TA1.TB1.TC1.R1");

        _TA2TB2TC2R1 = TypeHelper.findNestedRoleType(_TA2, "TA2.TB2.TC2.R1");
        _TA2TB2TC1R1 = TypeHelper.findNestedRoleType(_TA2, "TA2.TB2.TC1.R1");
        _TA2TB1TC2R1 = TypeHelper.findNestedRoleType(_TA2, "TA2.TB1.TC2.R1");
        _TA2TB1TC1R1 = TypeHelper.findNestedRoleType(_TA2, "TA2.TB1.TC1.R1");

    }

    
	public void testCreation()
	{
		assertCreation(_TA1);
        assertCreation(_TA2);
        assertCreation(_C0);
        assertCreation(_C1);
        assertCreation(_C2);
        assertCreation(_C3);
        
        assertCreation(_TA2TB2TC2R1);
        assertCreation(_TA2TB2TC1R1);
        assertCreation(_TA2TB1TC2R1);
        assertCreation(_TA2TB1TC1R1);
        assertCreation(_TA1TB2TC2R1);
        assertCreation(_TA1TB2TC1R1);
        assertCreation(_TA1TB1TC2R1);
        assertCreation(_TA1TB1TC1R1);
	}
    
    
    public void testGetTSuperTypes_TA2TB2TC2R1() throws JavaModelException
    {
        _focusType = _TA2TB2TC2R1;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = OTTypeHierarchies.getInstance().getTSuperTypes(hierarchy, _focusType);
        IType[] expected = new IType[] {
        								_TA2TB2TC1R1,
										_TA2TB1TC2R1,
										_TA1TB2TC2R1
        								};
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }


    public void testGetSuperclasses_TA2TB2TC2R1() throws JavaModelException
    {
        _focusType = _TA2TB2TC2R1;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(hierarchy, _focusType);
        IType[] expected = new IType[] {
        								_TA2TB2TC1R1,
										_TA2TB1TC2R1,
										_TA1TB2TC2R1,
										_C1
                                       };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetAllSuperclasses_TA2TB2TC2R1() throws JavaModelException
    {
        _focusType = _TA2TB2TC2R1;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getAllSuperclasses(_focusType);
        IType[] expected = new IType[] {
        								_TA1TB2TC2R1,
										_TA1TB2TC1R1,
										_TA1TB1TC2R1,
										_TA1TB1TC1R1,										
        								_TA2TB2TC1R1,
										_TA2TB1TC2R1,
										_TA2TB1TC1R1,
										_C0,
										_C1,
//										_C2, 			// illegal inheritance, causes incompatible supers
//										_C3, 			// illegal inheritance, causes incompatible supers
										_objectType
        								};
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    
    public void testGetExplicitSuperclass_TA2TB2TC2R1() throws JavaModelException
    {
        _focusType = _TA2TB2TC2R1;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(hierarchy, _focusType);
        IType expected = _C1;
   
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetAllClasses_TA2TB2TC2R1() throws JavaModelException
    {
        _focusType = _TA2TB2TC2R1;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getAllClasses();
        IType[] expected = new IType[] {
        								_TA1TB2TC2R1,
										_TA1TB2TC1R1,
										_TA1TB1TC2R1,
										_TA1TB1TC1R1,
        								_TA2TB2TC2R1,
        								_TA2TB2TC1R1,
										_TA2TB1TC2R1,
										_TA2TB1TC1R1,
										_C0,
										_C1,
										_C2, 			// illegal inheritance, causes incompatible supers
										_C3, 			// illegal inheritance, causes incompatible supers
										_objectType
        								};
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }
    
}
