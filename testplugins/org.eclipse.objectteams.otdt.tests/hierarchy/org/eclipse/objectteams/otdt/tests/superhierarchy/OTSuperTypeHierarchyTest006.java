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
 * $Id: OTSuperTypeHierarchyTest006.java 23494 2010-02-05 23:06:44Z stephan $
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
 * @author michael
 * @version $Id: OTSuperTypeHierarchyTest006.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OTSuperTypeHierarchyTest006 extends FileBasedHierarchyTest
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
	private IType _objectType;
	
	public OTSuperTypeHierarchyTest006(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSuperTypeHierarchyTest006.class);
		}
		@SuppressWarnings("unused")
		junit.framework.TestSuite suite = 
			new Suite(OTSuperTypeHierarchyTest006.class.getName());
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
    }
    
    public void testGetAllSuperclasses_T1R1() throws JavaModelException
    {
        _focusType = _T1_R1;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                						 _classA };
        
        IType [] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSuperclasses_T2R2() throws JavaModelException
    {
        _focusType = _T1_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType };
        
        IType [] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSuperclasses_T2R1() throws JavaModelException
    {
        _focusType = _T2_R1;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T1_R1,
                                         _classA };
        
        IType [] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetAllSuperclasses_T3R2() throws JavaModelException
    {
        _focusType = _T3_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _objectType,
                                         _T2_R1,
                                         _T1_R1,
                                         _T1_R2,
                                         _classA };
        
        IType [] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSuperclasses_T3R2_phantomMode() throws JavaModelException
    {
        _focusType = _T3_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
        
        IType[] expected = new IType[] { _objectType,
                                         _T2_R1,
                                         _T1_R1,
                                         _T1_R2,
                                         _phantom_T2_R2,
                                         _phantom_T3_R1,
                                         _classA };
        
        IType [] actual = _testObj.getAllSuperclasses(_focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    
	public void testGetExplicitSuperclass_T3R2() throws JavaModelException
    {
        _focusType = _T3_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
              
        IType expected = _T2_R1;        
        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(_testObj, _focusType);

        assertTrue(compareTypes(expected, actual));
    }

    public void testGetExplicitSuperclass_T3R2_phantomMode() throws JavaModelException
    {
        _focusType = _T3_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        OTTypeHierarchies.getInstance().setPhantomMode(_testObj, true);
              
        IType expected = _phantom_T3_R1;
        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(_testObj, _focusType);

        assertTrue(compareTypes(expected, actual));
    }

    
	public void testGetAllTSuperTypes_T3R2() throws JavaModelException
    {
        _focusType = _T3_R2;
        _testObj = createSuperTypeHierarchy(_focusType);

        IType[] expected = new IType[] { _T1_R2 };
        
        IType [] actual = OTTypeHierarchies.getInstance().getAllTSuperTypes(_testObj, _focusType);

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

	public void testGetAllTSuperTypes_T2R1() throws JavaModelException
    {
        _focusType = _T2_R1;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _T1_R1 };
        IType [] actual = OTTypeHierarchies.getInstance().getAllTSuperTypes(_testObj, _focusType);

        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }
	
}
