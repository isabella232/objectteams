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
 * $Id: OrdinaryInterfaceHierarchyTests.java 23494 2010-02-05 23:06:44Z stephan $
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

/**
 * @author kaschja
 * @version $Id: OrdinaryInterfaceHierarchyTests.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OrdinaryInterfaceHierarchyTests extends FileBasedModelTest
{
    private ITypeHierarchy _testObject;
    
    private IType _superSuperInterface;
    private IType _superInterface1;
    private IType _superInterface2;
    private IType _focusInterface;
    private IType _subInterface;
    private IType _subSubInterface1;
    private IType _subSubInterface2;

    private IType _javaLangObject;
    
    
    public OrdinaryInterfaceHierarchyTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(OrdinaryInterfaceHierarchyTests.class);
        }
        junit.framework.TestSuite suite = new Suite(OrdinaryInterfaceHierarchyTests.class.getName());
        return suite;
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("Hierarchy");
        super.setUpSuite();
        _focusInterface = 
            getType(getTestProjectDir(),
                "src",
                "standard.interface_hierarchy",
                "AnInterface");
        
        _testObject = new OTTypeHierarchy(_focusInterface, _focusInterface.getJavaProject(), true);
        _testObject.refresh(new NullProgressMonitor());
        
        _javaLangObject = getJavaLangObject(_focusInterface.getJavaProject());
        
        _superSuperInterface =
            getType(getTestProjectDir(),
                    "src",
                    "standard.interface_hierarchy",
                    "SuperSuperInterface");
        _superInterface1 =
            getType(getTestProjectDir(),
                    "src",
                    "standard.interface_hierarchy",
                    "SuperInterface1");
        _superInterface2 =
            getType(getTestProjectDir(),
                    "src",
                    "standard.interface_hierarchy",
                    "SuperInterface2");
        _subInterface =
            getType(getTestProjectDir(),
                    "src",
                    "standard.interface_hierarchy",
                    "SubInterface");
        _subSubInterface1 =
            getType(getTestProjectDir(),
                    "src",
                    "standard.interface_hierarchy",
                    "SubSubInterface1");
        _subSubInterface2 =
            getType(getTestProjectDir(),
                    "src",
                    "standard.interface_hierarchy",
                    "SubSubInterface2");
        
    }
 
    public void testExistence_SuperSuperInterface()
    	throws JavaModelException
	{
	    assertNotNull(_superSuperInterface);
	    assertTrue(_superSuperInterface.exists());
	}

    public void testExistence_SuperInterface1()
    	throws JavaModelException
	{
	    assertNotNull(_superInterface1);
	    assertTrue(_superInterface1.exists());
	}

    public void testExistence_SuperInterface2()
    	throws JavaModelException
	{
	    assertNotNull(_superInterface2);
	    assertTrue(_superInterface2.exists());
	}

    public void testExistence_FocusInterface()
    	throws JavaModelException
	{
	    assertNotNull(_focusInterface);
	    assertTrue(_focusInterface.exists());
	}

    public void testExistence_SubInterface()
    	throws JavaModelException
	{
	    assertNotNull(_subInterface);
	    assertTrue(_subInterface.exists());
	}

    public void testExistence_SubSubInterface1()
    	throws JavaModelException
	{
	    assertNotNull(_subSubInterface1);
	    assertTrue(_subSubInterface1.exists());
	}

    public void testExistence_SubSubInterface2()
    	throws JavaModelException
	{
	    assertNotNull(_subSubInterface2);
	    assertTrue(_subSubInterface2.exists());
	}
    
    
    public void testGetAllTypes()
    		throws JavaModelException
	{
	    IType[] expected = new IType[]{
	            _javaLangObject,
	            _superSuperInterface,
	            _superInterface1,
	            _superInterface2,
	            _focusInterface,
	            _subInterface,
	            _subSubInterface1,
	            _subSubInterface2};
	    IType[] actual = _testObject.getAllTypes();
	    
	    assertEquals(expected.length, actual.length);
	    assertTrue(compareTypes(expected, actual));
	}
    
    public void testGetAllClasses()
		throws JavaModelException
	{
	    IType[] expected = new IType[] { _javaLangObject };
	    IType[] actual = _testObject.getAllClasses();
	    
	    assertEquals(expected.length, actual.length);
	    assertTrue(compareTypes(expected, actual));
	}
    
    public void testGetSuperclass_ofFocusInterface()
    {
        assertNull("Interfaces have no superclass", _testObject.getSuperclass(_focusInterface));
    }
    
    public void testGetSubclass_ofFocusInterface()
    {
        IType[] actual = _testObject.getSubclasses(_focusInterface);
        IType[] expected = new IType[] {};

        assertTrue(compareTypes(expected, actual));       
    }
    
    public void testGetSubtypes_ofFocusInterface()
    {
        IType[] expected = new IType[]{_subInterface};
        IType[] actual = _testObject.getSubtypes(_focusInterface);
        
        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetRootInterfaces()
    {
        IType[] actual = _testObject.getRootInterfaces();
        IType[] expected = new IType[] { _superInterface2, _superSuperInterface } ;
        
        assertTrue(compareTypes(expected, actual));       
    }
    
    public void testGetAllInterfaces_SevenInterfaces()
		throws JavaModelException
	{
		IType[] expected = new IType[]{
		        _superSuperInterface,
		        _superInterface1,
		        _superInterface2,
		        _focusInterface,
		        _subInterface,
		        _subSubInterface1,
		        _subSubInterface2};
		IType[] actual = _testObject.getAllInterfaces();
		
		assertEquals(expected.length, actual.length);
		assertTrue(compareTypes(expected, actual));
	}
    
    public void testGetAllSuperInterfaces_FiveParents()
		throws JavaModelException
	{
		IType[] expected = new IType[]{
		        _superSuperInterface,
		        _superInterface1,
		        _superInterface2,
		        _focusInterface,
		        _subInterface };
		IType[] actual = _testObject.getAllSuperInterfaces(_subSubInterface1);
		
		assertEquals(expected.length, actual.length);
		assertTrue(compareTypes(expected, actual));
	}
    
    public void testGetExtendingInterfaces_OneChild()
		throws JavaModelException
	{
		IType[] expected = new IType[]{ _superInterface1 };
		IType[] actual = _testObject.getExtendingInterfaces(_superSuperInterface);
		
		assertEquals(expected.length, actual.length);
		assertTrue(compareTypes(expected, actual));
	}    
}
