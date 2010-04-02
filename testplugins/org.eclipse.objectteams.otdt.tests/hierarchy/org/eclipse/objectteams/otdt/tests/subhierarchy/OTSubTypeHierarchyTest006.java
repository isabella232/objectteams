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
 * $Id: OTSubTypeHierarchyTest006.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.subhierarchy;

import junit.framework.Test;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.PhantomType;
import org.eclipse.objectteams.otdt.tests.hierarchy.FileBasedHierarchyTest;

/**
 * @author Michael Kr?ger (mkr)
 *
 * $Id: OTSubTypeHierarchyTest006.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OTSubTypeHierarchyTest006 extends FileBasedHierarchyTest
{
    private IType _T1;
    private IType _T2;
    private IType _T3;
    
    private IType _T1_R1;
    private IType _T1_R2;
    
    private IType _T2_R1;
    private IType _phantom_T2_R2;

    private IType _phantom_T3_R1;
    private IType _T3_R2;
    
    private IType _classA;    
    @SuppressWarnings("unused")
	private IType _objectType;
    private IType _MyTeam_MyRole;
    private IType _MySubTeam_MyRole;
    private IType _MyOtherSubTeam_MyRole;

    private IType _MyTeam;
    private IType _MySubTeam;
    private IType _MyOtherSubTeam;
    	
	public OTSubTypeHierarchyTest006(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSubTypeHierarchyTest006.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(OTSubTypeHierarchyTest006.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test006";
		
        _T1 = getType(getTestProjectDir(), srcFolder, pkg, "T1");
        _T2 = getType(getTestProjectDir(), srcFolder, pkg, "T2");
        _T3 = getType(getTestProjectDir(), srcFolder, pkg, "T3");
        
        _T1_R1 = getRole(_T1, "T1.R1");
        _T1_R2 = getRole(_T1, "T1.R2");
        
        _T2_R1 = getRole(_T2, "T2.R1");
        _phantom_T2_R2 = new PhantomType(_T2, _T1_R2);
        
        _phantom_T3_R1 = new PhantomType(_T3, _T2_R1);
        _T3_R2 = getRole(_T3, "T3.R2");
        
        _classA =
            getType(getTestProjectDir(), srcFolder, pkg, "ClassA");             
        
        _objectType =
            getType(getTestProjectDir(), "rt.jar", "java.lang", "Object");
        
        setupExtraStuff();
	}
	
	private void setupExtraStuff() throws JavaModelException
    {
		String srcFolder = "src";
		String pkg = "simple";
		
        _MyTeam = getType(getTestProjectDir(), srcFolder, pkg, "MyTeam");
        _MySubTeam = getType(getTestProjectDir(), srcFolder, pkg, "MySubTeam");
        _MyOtherSubTeam = getType(getTestProjectDir(), srcFolder, pkg, "MyOtherSubTeam");
        
        _MyTeam_MyRole = getRole(_MyTeam, "MyTeam.MyRole");
        _MySubTeam_MyRole = new PhantomType(_MySubTeam, _MyTeam_MyRole);
        _MyOtherSubTeam_MyRole = getRole(_MyOtherSubTeam, "MyOtherSubTeam.MyRole");
    }

    public void testCreation()
	{
		assertCreation(_T1);
		assertCreation(_T2);
		assertCreation(_T3);
		
		assertCreation(_T1_R1);
		assertCreation(_T1_R2);
		
		assertCreation(_T2_R1);
		assertCreation(_phantom_T2_R2);
		
		assertCreation(_phantom_T3_R1);
		assertCreation(_T3_R2);
		
		assertCreation(_MyTeam);
		assertCreation(_MySubTeam);
		
		assertCreation(_MyTeam_MyRole);
		assertCreation(_MySubTeam_MyRole);
	}
	
    
	public void testGetAllSubtypes_ClassA() throws JavaModelException
    {
        _focusType = _classA;
        _testObj = createOTTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T1_R1, _T2_R1, _T3_R2 };        
        IType [] actual = _testObj.getAllSubtypes(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetAllSubtypes_MyTeam_MyRole_phantomMode() throws JavaModelException
    {
        _focusType = _MyTeam_MyRole;
        _testObj = createOTTypeHierarchy(_focusType);
        _testObj.setPhantomMode(true);
        
        IType[] expected = new IType[] {
                _MySubTeam_MyRole,
                _MyOtherSubTeam_MyRole
        };
		IType [] actual = _testObj.getAllSubtypes(_focusType);

		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSubtypes_ClassA_phantomMode() throws JavaModelException
    {
        _focusType = _classA;
        _testObj = createOTTypeHierarchy(_focusType);
        _testObj.setPhantomMode(true);
        
        IType[] expected = new IType[] { _T1_R1,
                                         _T2_R1,
                                         _T3_R2,
                                         _phantom_T3_R1 };        
        IType [] actual = _testObj.getAllSubtypes(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetSubtypes_ClassA() throws JavaModelException
    {
        _focusType = _classA;
        _testObj = createOTTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T1_R1, _T2_R1 };        
        IType [] actual = _testObj.getSubtypes(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetSubtypes_T2R1_in_ClassA() throws JavaModelException
    {
        _focusType = _classA;
        _testObj = createOTTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T3_R2 };        
        IType [] actual = _testObj.getSubtypes(_T2_R1);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetSubtypes_T2R1_in_ClassA_phantomMode() throws JavaModelException
    {
        _focusType = _classA;
        _testObj = createOTTypeHierarchy(_focusType);
        _testObj.setPhantomMode(true);
        
        IType[] expected = new IType[] { _T3_R2, _phantom_T3_R1 };        
        IType [] actual = _testObj.getSubtypes(_T2_R1);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
	
}
