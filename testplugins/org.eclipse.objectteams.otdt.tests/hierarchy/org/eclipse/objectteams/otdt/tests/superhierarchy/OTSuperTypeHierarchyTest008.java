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
 * $Id: OTSuperTypeHierarchyTest008.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.objectteams.otdt.tests.hierarchy.FileBasedHierarchyTest;

import org.eclipse.objectteams.otdt.core.hierarchy.OTTypeHierarchies;

/**
 * 
 * @author Michael Krueger (michael)
 * @version $Id: OTSuperTypeHierarchyTest008.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTSuperTypeHierarchyTest008 extends FileBasedHierarchyTest
{
	
    private IType _T21;
    private IType _T22;    
	private IType _objectType;
    private IType _T21T12R1;
    private IType _T21T12R2;
    private IType _T21T12R3;
    private IType _T21T11R1;
    private IType _T21T11R2;
    private IType _T21T11R3;
    private IType _T22T12R1;
    private IType _T22T12R2;
    private IType _T22T12R3;
    private IType _T22T11R1;
    private IType _T22T11R2;
    private IType _T22T11R3;
    
	public OTSuperTypeHierarchyTest008(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSuperTypeHierarchyTest008.class);
		}
		@SuppressWarnings("unused")
		junit.framework.TestSuite suite = 
			new Suite(OTSuperTypeHierarchyTest008.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test008";
		
		_T22 = getType(getTestProjectDir(), srcFolder, pkg, "T22");
        _T21 = getType(getTestProjectDir(), srcFolder, pkg, "T21");        
        _objectType =
            getType(getTestProjectDir(), "rt.jar", "java.lang", "Object");

        _T21T12R1 = getRole(_T21, "T21.T12.R1");
        _T21T12R2 = getRole(_T21, "T21.T12.R2");
        _T21T12R3 = getRole(_T21, "T21.T12.R3");
        _T21T11R1 = getRole(_T21, "T21.T11.R1");
        _T21T11R2 = getRole(_T21, "T21.T11.R2");
        _T21T11R3 = getRole(_T21, "T21.T11.R3");
        _T22T12R1 = getRole(_T22, "T22.T12.R1");
        _T22T12R2 = getRole(_T22, "T22.T12.R2");
        _T22T12R3 = getRole(_T22, "T22.T12.R3");
        _T22T11R1 = getRole(_T22, "T22.T11.R1");
        _T22T11R2 = getRole(_T22, "T22.T11.R2");
        _T22T11R3 = getRole(_T22, "T22.T11.R3");
    }
	
	public void testCreation()
	{
		assertCreation(_T21);
        assertCreation(_T22);
        assertCreation(_T21T12R1);
        assertCreation(_T21T12R2);
        assertCreation(_T21T12R3);
        assertCreation(_T21T11R1);
        assertCreation(_T21T11R2);
        assertCreation(_T21T11R3);

        assertCreation(_T22T12R1);
        assertCreation(_T22T12R2);
        assertCreation(_T22T12R3);
        assertCreation(_T22T11R1);
        assertCreation(_T22T11R2);
        assertCreation(_T22T11R3);
    }

	public void testGetAllSuperclasses_T22T11R2() throws JavaModelException
    {
        _focusType = _T22T11R2;
        
        ITypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getAllSuperclasses(_focusType);
        IType[] expected = new IType[] { _objectType,
                                            _T22T11R1,
                                            _T21T11R1,
                                            _T21T11R2,
                                        };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetAllSuperclasses_T22T12R3() throws JavaModelException
    {
        _focusType = _T22T12R3;
        
        ITypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getAllSuperclasses(_focusType);
        IType[] expected = new IType[] { _objectType,
                                         _T21T11R1,
                                         _T21T11R2,
                                         _T21T11R3,
                                         _T21T12R1,
                                         _T21T12R2,
                                         _T21T12R3,
                                         _T22T11R1,
                                         _T22T11R2,
                                         _T22T11R3,
                                         _T22T12R1,
                                         _T22T12R2,
                                        };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetTSuperTypes_T22T12R3() throws JavaModelException
    {
        _focusType = _T22T12R3;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] actual = OTTypeHierarchies.getInstance().getTSuperTypes(_testObj, _focusType);
        IType[] expected = new IType[] { _T21T12R3,
                                         _T22T11R3 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllTSuperTypes_T22T12R3() throws JavaModelException
    {
        _focusType = _T22T12R3;
        ITypeHierarchy hier = createSuperTypeHierarchy(_focusType);
        
        IType[] actual = OTTypeHierarchies.getInstance().getAllTSuperTypes(hier, _focusType);
        IType[] expected = new IType[] { _T21T12R3,
                                         _T22T11R3,
                                         _T21T11R3 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetSuperclass_UnsupportedOperationException() throws JavaModelException
    {
        _focusType = _T22T12R3;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        boolean result = false;
        try
		{
        	_testObj.getSuperclass(_focusType);
		}
        catch(Exception exc)
		{
        	result = exc instanceof UnsupportedOperationException;
		}
        assertFalse(result);
    }
    
    public void testGetExplicitSuperclass_T22T12R3() throws JavaModelException
    {
        _focusType = _T22T12R3;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(_testObj, _focusType);
		IType expected = _T22T12R2;
		
		assertTrue(compareTypes(expected, actual));
    }

    public void testGetSuperclasses_T22T12R3() throws JavaModelException
    {
        _focusType = _T22T12R3;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _focusType);
        IType[] expected = new IType[] { _T22T12R2,
                                         _T21T12R3,
                                         _T22T11R3 };
        
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetSuperclasses_T22T12R2inT22T12R3() throws JavaModelException
    {
        _focusType = _T22T12R3;
        _testObj = createSuperTypeHierarchy(_focusType);

        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _T22T12R2);
        IType[] expected = new IType[] { _T22T12R1,
                                         _T21T12R2,
                                         _T22T11R2 };
        
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetSuperclasses_T22T12R1inT21T12R1() throws JavaModelException
    {
        _focusType = _T21T12R1;
        _testObj = createTypeHierarchy(_focusType);

        IType[] actual = OTTypeHierarchies.getInstance().getSuperclasses(_testObj, _T22T12R1);
        IType[] expected = new IType[] { _T21T12R1, // direct tsuper
                                         _T22T11R1, // direct tsuper
                                         _objectType };
        
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    public void testGetAllSuperclasses_T22T12R1inT21T12R1() throws JavaModelException
    {
        _focusType = _T21T12R1;
        _testObj = createTypeHierarchy(_focusType);

        IType[] actual = _testObj.getAllSuperclasses(_T22T12R1);
        IType[] expected = new IType[] { _T21T12R1,
//                                         _T22T11R1, // not related to focusType
                                         _T21T11R1,
                                         _objectType };
        
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

}
