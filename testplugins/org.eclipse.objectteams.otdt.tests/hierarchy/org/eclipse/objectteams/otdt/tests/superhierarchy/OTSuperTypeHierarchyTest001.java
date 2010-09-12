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
 * $Id: OTSuperTypeHierarchyTest001.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.objectteams.otdt.core.PhantomType;
import org.eclipse.objectteams.otdt.core.hierarchy.OTTypeHierarchies;
import org.eclipse.objectteams.otdt.tests.hierarchy.FileBasedHierarchyTest;

/**
 * @author Michael Krueger (mkr)
 * @version $Id: OTSuperTypeHierarchyTest001.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OTSuperTypeHierarchyTest001 extends FileBasedHierarchyTest
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
	
	private IType _objectType;
	
    
	public OTSuperTypeHierarchyTest001(String name)
	{
		super(name);
	}
	
    
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSuperTypeHierarchyTest001.class);
		}
		@SuppressWarnings("unused")
		junit.framework.TestSuite suite = 
			new Suite(OTSuperTypeHierarchyTest001.class.getName());
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

    public void testGetExplicitSuperclass_T8R2() throws JavaModelException
    {
        _focusType = _T8_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType expected = _T6_R1;
        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(_testObj, _focusType);

        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetExplicitSuperclass_T8R2_phantomMode() throws JavaModelException
    {
        _focusType = _T8_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
        
        IType expected = _phantom_T8_R1;
        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(_testObj, _focusType);

        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetTSuperTypes_T8R2() throws JavaModelException
    {
        _focusType = _T8_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T2_R2 };        
        IType[] actual = OTTypeHierarchies.getInstance().getTSuperTypes(_testObj, _focusType);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetTSuperTypes_T8R2_phantomMode() throws JavaModelException
    {
        _focusType = _T8_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
        
        IType[] expected = new IType[] { _phantom_T6_R2 };        
        IType[] actual = OTTypeHierarchies.getInstance().getTSuperTypes(_testObj, _focusType);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllTSuperTypes_T8R2() throws JavaModelException
    {
        _focusType = _T8_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T2_R2,
                                         _T1_R2
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getAllTSuperTypes(_testObj, _focusType);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetAllTSuperTypes_T8R2_phantomMode() throws JavaModelException
    {
        _focusType = _T8_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
        
        IType[] expected = new IType[] { _phantom_T6_R2,
                                         _T2_R2,
                                         _T1_R2
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getAllTSuperTypes(_testObj, _focusType);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetSuperclasses_T8R2() throws JavaModelException
    {
        _focusType = _T8_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T6_R1,
                                         _T2_R2,
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _focusType);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetSuperclasses_T8R2_phantomMode() throws JavaModelException
    {
        _focusType = _T8_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
        
        IType[] expected = new IType[] { _phantom_T8_R1,
                                         _phantom_T6_R2
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _focusType);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }
    
        
	public void testGetAllSuperclasses_T8R2() throws JavaModelException
	{
		_focusType = _T8_R2;
		_testObj = createSuperTypeHierarchy(_focusType);
        
		IType[] expected = new IType[] { _objectType,
								         _T6_R1,
								         _T2_R2,
								         _T2_R1,
								         _T1_R2,
								         _T1_R1
								       };
		
		IType[] actual = _testObj.getAllSuperclasses(_focusType);
		
		assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}

    public void testGetAllSuperclasses_T8R2_phantomMode() throws JavaModelException
    {
        _focusType = _T8_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);

        IType[] expected = new IType[] { _objectType,
                                         _phantom_T8_R1,
                                         _T6_R1,
                                         _phantom_T6_R2,
                                         _T2_R2,
                                         _T2_R1,
                                         _T1_R2,
                                         _T1_R1
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }


    public void testGetSuperclasses_T6R1() throws JavaModelException
    {
        _focusType = _T6_R1;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T2_R1,
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    
    public void testGetSuperclasses_T6R1_phantomMode() throws JavaModelException
    {
        _focusType = _T6_R1;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
        
        IType[] expected = new IType[] { _objectType,
                                         _T2_R1,
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    
    public void testGetAllSuperclasses_T6R1() throws JavaModelException
    {
        _focusType = _T6_R1;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T2_R1,
                                         _T1_R1
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }


    public void testGetSuperclasses_T2R2() throws JavaModelException
    {
        _focusType = _T2_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T2_R1,
                                         _T1_R2
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    
    public void testGetAllSuperclasses_T2R2() throws JavaModelException
    {
        _focusType = _T2_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T2_R1,
                                         _T1_R1,
                                         _T1_R2
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);
        
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    
    
    public void testGetAllSuperclasses_T2R1() throws JavaModelException
    {
        _focusType = _T2_R1;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T1_R1
                                         };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }
    
    
    public void testGetAllSuperclasses_T1R1() throws JavaModelException
    {
        _focusType = _T1_R1;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }
    
    
    public void testGetAllSuperclasses_T1R2() throws JavaModelException
    {
        _focusType = _T1_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }

    
    public void testGetSuperclasses_T7R3() throws JavaModelException
    {
        _focusType = _T7_R3;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T5_R1,
                                         _T5_R3
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }


    public void testGetSuperclasses_T7R3_phantomMode() throws JavaModelException
    {
        _focusType = _T7_R3;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
        
        IType[] expected = new IType[] { _phantom_T7_R1,
                                         _T5_R3
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    
    public void testGetAllSuperclasses_T7R3() throws JavaModelException
    {
        _focusType = _T7_R3;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T5_R1,
                                         _T2_R1,
                                         _T1_R1,
                                         _T5_R3
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    
    public void testGetAllSuperclasses_T7R3_phantomMode() throws JavaModelException
    {
        _focusType = _T7_R3;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
        
        IType[] expected = new IType[] { _objectType,
                                         _phantom_T7_R1,
                                         _T5_R1,
                                         _T2_R1,
                                         _T1_R1,
                                         _T5_R3
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    
	public void testGetAllSuperclasses_T7R2() throws JavaModelException
	{
		_focusType = _T7_R2;
		_testObj = createSuperTypeHierarchy(_focusType);
		
		IType[] expected = new IType[] { _objectType,
								         _T7_R3,
								         _T5_R1,
								         _T5_R3,
								         _T5_R2,
								         _T2_R2,
								         _T2_R1,
								         _T1_R2,
								         _T1_R1
                                       };
		
		IType[] actual = _testObj.getAllSuperclasses(_T7_R2);

        assertEquals(expected.length, actual.length);		
		assertTrue(compareTypes(expected, actual));
	}
    
    
    public void testGetAllSuperclasses_T7R2_phantomMode() throws JavaModelException
    {
        _focusType = _T7_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
        
        IType[] expected = new IType[] { _objectType,
                                         _T7_R3,
                                         _phantom_T7_R1,
                                         _T5_R1,
                                         _T5_R3,
                                         _T5_R2,
                                         _T2_R2,
                                         _T2_R1,
                                         _T1_R2,
                                         _T1_R1
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_T7_R2);

        assertEquals(expected.length, actual.length);       
        assertTrue(compareTypes(expected, actual));
    }
    

    public void testGetAllSuperclasses_T7R3inT7R2() throws JavaModelException
    {
        _focusType = _T7_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T5_R1,
                                         _T5_R3,
                                         _T2_R1,
                                         _T1_R1
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_T7_R3);
        
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    

    public void testGetAllSuperclasses_T2R1inT7R2() throws JavaModelException
    {
        _focusType = _T7_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T1_R1
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_T2_R1);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

        
    public void testGetAllSuperclasses_T5R2() throws JavaModelException
    {
        _focusType = _T5_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T5_R3,
                                         _T5_R1,
                                         _T2_R2,
                                         _T2_R1,
                                         _T1_R1,
                                         _T1_R2
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    
    
    public void testGetAllSuperclasses_T5R3() throws JavaModelException
    {
        _focusType = _T5_R3;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        
        IType[] expected = new IType[] { _objectType,
                                         _T5_R1,
                                         _T2_R1,
                                         _T1_R1
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    
    
    public void testGetAllSuperclasses_T5R1() throws JavaModelException
    {
        _focusType = _T5_R1;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T2_R1,
                                         _T1_R1
                                         };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    
    public void testGetSuperclasses_T4R2() throws JavaModelException
    {
        _focusType = _T4_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T2_R2,
                                         _T2_R1
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    
    public void testGetSuperclasses_T4R2_phantomMode() throws JavaModelException
    {
        _focusType = _T4_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
        
        IType[] expected = new IType[] { _T2_R2,
                                         _phantom_T4_R1
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    
    public void testGetAllSuperclasses_T4R2() throws JavaModelException
    {
        _focusType = _T4_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T2_R2,
                                         _T2_R1,
                                         _T1_R1,
                                         _T1_R2
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    

    public void testGetAllSuperclasses_T4R2_phantomMode() throws JavaModelException
    {
        _focusType = _T4_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
        
        IType[] expected = new IType[] { _objectType,
                                         _phantom_T4_R1,
                                         _T2_R2,
                                         _T2_R1,
                                         _T1_R1,
                                         _T1_R2
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    
    
    public void testGetAllSuperclasses_T3R2() throws JavaModelException
    {
        _focusType = _T3_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T3_R1,
                                         _T2_R2,
                                         _T2_R1,
                                         _T1_R1,
                                         _T1_R2
                                       };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSuperclasses_T3R1() throws JavaModelException
    {
        _focusType = _T3_R1;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T2_R1,
                                         _T1_R1,
                                         };
        
        IType[] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }
    
    
    public void testGetAllTSuperTypes_T5R1inT7R2() throws JavaModelException
    {
        _focusType = _T7_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T2_R1,
                                         _T1_R1,
                                       };
        
        IType[] actual = OTTypeHierarchies.getInstance().getAllTSuperTypes(_testObj, _T5_R1);
        assertEquals(expected.length, actual.length);
        
        assertTrue(compareTypes(expected, actual));
    }
    
    
    public void testToString_forNoException() throws JavaModelException
    {
        _focusType = _T5_R1;
        _testObj = createSuperTypeHierarchy(_focusType);

        try
        {
        	_testObj.toString();		
		}
		catch (Exception ex)
		{
			assertTrue(ex.getClass().toString() + " in OTTypeHierarchy.toString()", false);
		}
		
        assertTrue(true);
    }
    
    public void testLinearizationR2() throws JavaModelException {
    	_focusType = _T7_R2;
    	_testObj = createSuperTypeHierarchy(_focusType);
    	OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
    	IType superType = _testObj.getSuperclass(_focusType);
    	IType[] expected = new IType[] {_T5_R2, _T2_R2, _T1_R2, _T7_R3, _T5_R3, _phantom_T7_R1, _T5_R1, _T2_R1, _T1_R1}; 
    	for (int i=0; i<expected.length; i++) {
    		assertTrue(compareTypes(expected[i], superType));
    		superType = _testObj.getSuperclass(superType);
    	}
    }
    
}
