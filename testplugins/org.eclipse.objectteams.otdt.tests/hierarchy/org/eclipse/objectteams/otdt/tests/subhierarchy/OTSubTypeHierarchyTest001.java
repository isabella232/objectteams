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
 * $Id: OTSubTypeHierarchyTest001.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.subhierarchy;

import java.util.HashMap;

import junit.framework.Test;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.PhantomType;
import org.eclipse.objectteams.otdt.tests.hierarchy.FileBasedHierarchyTest;

/**
 * @author Michael Krueger (mkr)
 * @version $Id: OTSubTypeHierarchyTest001.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OTSubTypeHierarchyTest001 extends FileBasedHierarchyTest
{
	
    private IType _T1;
    private IType _T2;
    private IType _T3;
    private IType _T4;
    private IType _T5;
    private IType _T6;
    private IType _T7;
    private IType _T8;
    
    private IType _T1_R1;
    private IType _T1_R2;   

    private IType _T2_R1;
    private IType _T2_R2;
    
    private IType _T3_R1;
    private IType _T3_R2;
    
    private IType _phantom_T4_R1;
    private IType _T4_R2;
    
    private IType _T5_R1;
    private IType _T5_R2;
    private IType _T5_R3;
    
    private IType _T6_R1;
    private IType _phantom_T6_R2;
    
    private IType _phantom_T7_R1;
    private IType _T7_R2;
    private IType _T7_R3;   
    
    private IType _phantom_T8_R1;
    private IType _T8_R2;
    
    @SuppressWarnings("unused")
	private IType _objectType;
	
	public OTSubTypeHierarchyTest001(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSubTypeHierarchyTest001.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(OTSubTypeHierarchyTest001.class.getName());
		return suite;
	}
	
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("Hierarchy");
        super.setUpSuite();
        
        String srcFolder = "src";
        String pkg = "test001";
        
        _T1 = getType(getTestProjectDir(), srcFolder, pkg, "T1");
        _T2 = getType(getTestProjectDir(), srcFolder, pkg, "T2");
        _T3 = getType(getTestProjectDir(), srcFolder, pkg, "T3");
        _T4 = getType(getTestProjectDir(), srcFolder, pkg, "T4");
        _T5 = getType(getTestProjectDir(), srcFolder, pkg, "T5");
        _T6 = getType(getTestProjectDir(), srcFolder, pkg, "T6");
        _T7 = getType(getTestProjectDir(), srcFolder, pkg, "T7");
        _T8 = getType(getTestProjectDir(), srcFolder, pkg, "T8");

        _T1_R1 = getRole(_T1, "T1.R1");
        _T1_R2 = getRole(_T1, "T1.R2");

        _T2_R1 = getRole(_T2, "T2.R1");
        _T2_R2 = getRole(_T2, "T2.R2");

        _T3_R1 = getRole(_T3, "T3.R1");
        _T3_R2 = getRole(_T3, "T3.R2");

        _phantom_T4_R1 = new PhantomType(_T4, _T2_R1);
        _T4_R2 = getRole(_T4, "T4.R2");

        _T5_R1 = getRole(_T5, "T5.R1");
        _T5_R2 = getRole(_T5, "T5.R2");
        _T5_R3 = getRole(_T5, "T5.R3");

        _T6_R1 = getRole(_T6, "T6.R1");
        _phantom_T6_R2 = new PhantomType(_T6, _T2_R2);
        
        _phantom_T7_R1 = new PhantomType(_T7, _T5_R1);
        _T7_R2 = getRole(_T7, "T7.R2");
        _T7_R3 = getRole(_T7, "T7.R3");

        _phantom_T8_R1 = new PhantomType(_T8, _T6_R1);
        _T8_R2 = getRole(_T8, "T8.R2");
        
        _objectType = getType(getTestProjectDir(), "rt.jar", "java.lang", "Object");
    }
	
    public void testCreation()
    {
        assertCreation(_T1);
        assertCreation(_T2);
        assertCreation(_T3);
        assertCreation(_T4);
        assertCreation(_T5);
        assertCreation(_T6);
        assertCreation(_T7);
        assertCreation(_T8);

        assertCreation(_T1_R1);
        assertCreation(_T1_R2);

        assertCreation(_T2_R1);
        assertCreation(_T2_R2);

        assertCreation(_T3_R1);
        assertCreation(_T3_R2);

        assertCreation(_phantom_T4_R1);
        assertCreation(_T4_R2);

        assertCreation(_T5_R1);
        assertCreation(_T5_R2);
        assertCreation(_T5_R3);

        assertCreation(_T6_R1);
        assertCreation(_phantom_T6_R2);
        
        assertCreation(_phantom_T7_R1);
        assertCreation(_T7_R2);
        assertCreation(_T7_R3);

        assertCreation(_phantom_T8_R1);
        assertCreation(_T8_R2);
    }
    
	public void testGetAllSubtypes_T1_R1() throws JavaModelException
	{
		_focusType = _T1_R1;
        _testObj = createOTTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] {
								         _T2_R1, _T2_R2,
								         _T3_R1, _T3_R2,
								         _T4_R2,
										 _T5_R1, _T5_R2, _T5_R3,
										 _T6_R1,
										 _T7_R2, _T7_R3,
										 _T8_R2
								         };
		
		IType [] actual = _testObj.getAllSubtypes(_focusType);

		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}
    

	public void testGetAllSubtypes_T1_R2() throws JavaModelException
	{
		_focusType = _T1_R2;
        _testObj = createOTTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] {
								         _T2_R2,
								         _T3_R2,
								         _T4_R2,
										 _T5_R2,
										 _T7_R2,
										 _T8_R2
								         };
		
		IType [] actual = _testObj.getAllSubtypes(_focusType);

		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}
	
	public void testGetSubtypes_T2_R1() throws JavaModelException
	{
		_focusType = _T2_R1;
        _testObj = createOTTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] {
										_T4_R2,
								        _T2_R2,
								        _T3_R1,
										_T5_R1,
										_T6_R1
										};
		
		IType [] actual = _testObj.getSubtypes(_focusType);

		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}
	
	
	public void testGetSubclasses_T2_R1() throws JavaModelException
	{
		_focusType = _T2_R1;
        _testObj = createOTTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] {
										_T4_R2,
										_T2_R2,
								        _T3_R1,
										_T5_R1,
										_T6_R1
										};
		
		IType [] actual = _testObj.getSubclasses(_focusType);

		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}

	
	public void testGetSubclasses_T5_R1() throws JavaModelException
	{
		_focusType = _T5_R1;
        _testObj = createOTTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] { _T5_R3, _T7_R3 };
		
		IType [] actual = _testObj.getSubclasses(_focusType);

		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}
	
	
	public void testGetSubclasses_T6_R1() throws JavaModelException
	{
		_focusType = _T6_R1;
        _testObj = createOTTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] { _T8_R2,
                                         _T2_R2 };
		
		IType [] actual = _testObj.getSubclasses(_focusType);

		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}

    public void testGetSubclasses_T6_R1_phantomMode() throws JavaModelException
    {
        _focusType = _T6_R1;
        _testObj = createOTTypeHierarchy(_focusType);
        _testObj.setPhantomMode(true);
        
        IType[] expected = new IType[] { _phantom_T8_R1,
                                         _phantom_T6_R2,
                                         _T8_R2};
        
        IType [] actual = _testObj.getSubclasses(_focusType);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetSubclasses_T6_R1_in_T2_R2() throws JavaModelException
    {
        _focusType = _T2_R2;
        _testObj = createOTTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T8_R2,
                                         _T2_R2 };
        
        IType [] actual = _testObj.getSubclasses(_T6_R1);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetSubtypes_T6_R1() throws JavaModelException
    {
        _focusType = _T6_R1;
        _testObj = createOTTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T8_R2,
                                         _T2_R2 };
        
        IType [] actual = _testObj.getSubtypes(_focusType);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetSubtypes_T6_R1_phantomMode() throws JavaModelException
    {
        _focusType = _T6_R1;
        _testObj = createOTTypeHierarchy(_focusType);
        _testObj.setPhantomMode(true);
        
        IType[] expected = new IType[] { _phantom_T6_R2,
                                         _phantom_T8_R1,
                                         _T8_R2 };        
        IType [] actual = _testObj.getSubtypes(_focusType);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetAllSubtypes_T6_R1_in_T2R1() throws JavaModelException
    {
        _focusType = (_T2_R1);
        _testObj = createOTTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T8_R2, _T2_R2 };
        IType [] actual = _testObj.getAllSubtypes(_T6_R1);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }

    public void testPhantomTypeEquality1() throws JavaModelException
    {
        PhantomType phantom1 = new PhantomType(_T6, _T2_R2);
        PhantomType phantom2 = new PhantomType(_T6, _T2_R2);
                
        assertEquals(phantom1.hashCode(), phantom2.hashCode());
        assertTrue(phantom1.equals(phantom2));
    }

    public void testPhantomTypeHashability1() throws JavaModelException
    {
    	PhantomType phantom1 = new PhantomType(_T6, _T2_R2);
        PhantomType phantom2 = new PhantomType(_T6, _T2_R2);
        
        HashMap<PhantomType, Double> map = new HashMap<PhantomType, Double>();
        Double dummy = new Double(0.0);
        map.put(phantom1, dummy);
        
        Double expected = dummy;
        Double actual = map.get(phantom2);

        assertEquals(expected, actual);
    }
    
    public void testPhantomTypeEquality2() throws JavaModelException
    {
        PhantomType phantom1 = new PhantomType(_T6, _T2_R2);
        PhantomType phantom2 = new PhantomType(_T6, _T2_R2);
        
        PhantomType phantom11 = new PhantomType(phantom1, _T1_R1);
        PhantomType phantom12 = new PhantomType(phantom2, _T1_R1);
        
        assertEquals(phantom11.hashCode(), phantom12.hashCode());
        assertTrue(phantom11.equals(phantom12));
    }

    public void testPhantomTypeHashability2() throws JavaModelException
    {
        PhantomType phantom1 = new PhantomType(_T6, _T2_R2);
        PhantomType phantom2 = new PhantomType(_T6, _T2_R2);
        
        PhantomType phantom11 = new PhantomType(phantom1, _T1_R1);
        PhantomType phantom12 = new PhantomType(phantom2, _T1_R1);
        
        HashMap<PhantomType, Double> map = new HashMap<PhantomType, Double>();
        Double dummy = new Double(0.0);
        map.put(phantom11, dummy);
        
        Double expected = dummy;
        Double actual = map.get(phantom12);
        assertEquals(expected, actual);
    }

}
