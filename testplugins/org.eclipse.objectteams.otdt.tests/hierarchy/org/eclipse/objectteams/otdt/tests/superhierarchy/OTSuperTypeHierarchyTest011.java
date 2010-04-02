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
 * $Id: OTSuperTypeHierarchyTest011.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.objectteams.otdt.tests.hierarchy.FileBasedHierarchyTest;

/**
 * 
 * @author Michael Krueger (mkr)
 * @version $Id: OTSuperTypeHierarchyTest011.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTSuperTypeHierarchyTest011 extends FileBasedHierarchyTest
{
    
    private IType _T10;
    private IType _T21;    

    private IType _T21T11T0R1;
    private IType _phantom_T21T11T0R2;

    private IType _T10T0R1;
    private IType _T10T0R2;
    
	private IType _objectType;

    public OTSuperTypeHierarchyTest011(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSuperTypeHierarchyTest011.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(OTSuperTypeHierarchyTest011.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test011";
		
		_T10 = getType(getTestProjectDir(), srcFolder, pkg, "T10");

        _T21 =  getType(getTestProjectDir(), srcFolder, pkg, "T21");
                
        _objectType =
            getType(getTestProjectDir(), "rt.jar", "java.lang", "Object");

        _T21T11T0R1 = getRole(_T21, "T21.T11.T0.R1");
        _T10T0R1 = getRole(_T10, "T10.T0.R1");
        _T10T0R2 = getRole(_T10, "T10.T0.R2");
        _phantom_T21T11T0R2 = new PhantomType(_T21, _T10T0R2);
    }
    
	public void testCreation()
	{
		assertCreation(_T10);
        assertCreation(_T21);
        assertCreation(_T21T11T0R1);
        assertCreation(_phantom_T21T11T0R2);        
        assertCreation(_T10T0R1);
        assertCreation(_T10T0R2);
    }

    public void testGetTSuperTypes_T21T11T0R1() throws JavaModelException
    {
        _focusType = _T21T11T0R1;
        _testObj = createOTSuperTypeHierarchy(_focusType);
    
        IType[] actual = _testObj.getTSuperTypes(_focusType);
        IType[] expected = new IType[] { _T10T0R1 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    
    public void testGetSuperclasses_T21T11T0R1() throws JavaModelException
    {
        _focusType = _T21T11T0R1;
        _testObj = createOTSuperTypeHierarchy(_focusType);
   
        IType[] actual = _testObj.getSuperclasses(_focusType);
        IType[] expected = new IType[] { _T10T0R1,
                                         _T10T0R2 };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }
    
    public void testGetAllClasses_T21T11T0R1() throws JavaModelException
    {
        _focusType = _T21T11T0R1;
        _testObj = createOTSuperTypeHierarchy(_focusType);
        
        IType[] actual = _testObj.getAllClasses();
        IType[] expected = new IType[] { _T21T11T0R1,
        								 _T10T0R1,
										 _T10T0R2,
										 _objectType };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }
    
    public void testGetAllClasses_T10T0R1() throws JavaModelException
    {
        _focusType = _T10T0R1;
        _testObj = createOTSuperTypeHierarchy(_focusType);
        
        IType[] actual = _testObj.getAllClasses();
        IType[] expected = new IType[] { _T10T0R1,
										 _T10T0R2,
										 _objectType };
   
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }
}
