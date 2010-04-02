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
 * $Id: CompleteRoleHierarchyWithClasses.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.hierarchy;

import java.util.ArrayList;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author jwloka
 * @version $Id: CompleteRoleHierarchyWithClasses.java 23494 2010-02-05 23:06:44Z stephan $
 */
/*
 * The testdata setting looks like this:
 *                                                                              
 * SuperSuperTeam
 *             R1                                             SuperClass
 *      /\                                                       /\
 *       |                                                        |
 *   SuperTeam                                                    |
 *          R1------------------------------------------------> AClass
 *      /\                      explicit inheritance             /\
 *       |                      ~~~~~~~~~~~~~~~~~~~~              |'''''''''''''''''|
 *     ATeam                                                      |                 |
 *        R1                                                  SubClass1         SubClass2
 *      /\                                                       /\                /\
 *       |'''''''''''''''''''|                |''''''''''''|'''''''''''''|          |
 *    SubTeam1            SubTeam2            |            |             |          |
 *          R1                  R1------------------>SubSubClass11 SubSubClass12    |
 *      /\                                    |                                     | 
 *       |'''''''''''|                        |                                     |
 * SubSubTeam11  SubSubTeam12                 |                                     |
 *           R1            R1-----------------|                                     |
 *            |                                                                     |
 *            |---------------------------------------------------------------------|
 * 
 * The focus role is ATeam.R1 .
 */
public class CompleteRoleHierarchyWithClasses extends FileBasedModelTest
{
    private IRoleType _focusRole;
    private IRoleType _implSuperRole;
    private IRoleType _implSuperSuperRole;
    private IRoleType _implSubRole1;
    private IRoleType _implSubRole2;
    private IRoleType _implSubSubRole11;
    private IRoleType _implSubSubRole12;
    private IType     _class;
    private IType     _superClass;
    private IType     _subClass1;
    private IType     _subClass2;
    private IType     _subSubClass11;
    private IType     _subSubClass12;
    private IType	  _object;
    
    private OTTypeHierarchy _hierarchy;
    
    
    public CompleteRoleHierarchyWithClasses(String name)
    {
        super(name);
    }
    
    public static Test suite()
    {
        if (true)
        {
            return new Suite(CompleteRoleHierarchyWithClasses.class);
        }
        junit.framework.TestSuite suite = 
            new Suite(CompleteRoleHierarchyWithClasses.class.getName());
        return suite;
    }
    
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("Hierarchy");
        super.setUpSuite();
        
        _focusRole = 
            getRole(getTestProjectDir(),
                "complete_rolehierarchy_with_classes",
                "test002.inlined",
                "ATeam",
                "R1");
 
        _hierarchy = new OTTypeHierarchy(_focusRole, _focusRole.getJavaProject(), true);
        _hierarchy.refresh(new NullProgressMonitor());        
        
        _implSuperRole = 
            getRole(getTestProjectDir(),
                "complete_rolehierarchy_with_classes",
                "test002.inlined",
                "SuperTeam",
                "R1");
        
        _implSuperSuperRole = 
            getRole(getTestProjectDir(),
                "complete_rolehierarchy_with_classes",
                "test002.inlined",
                "SuperSuperTeam",
                "R1");
        
        _implSubRole1 = 
            getRole(getTestProjectDir(),
                    "complete_rolehierarchy_with_classes",
                    "test002.inlined",
                    "SubTeam1",
                    "R1");
        
        _implSubRole2 = 
            getRole(getTestProjectDir(),
                    "complete_rolehierarchy_with_classes",
                    "test002.inlined",
                    "SubTeam2",
                    "R1");
        
        _implSubSubRole11 =
            getRole(getTestProjectDir(),
                    "complete_rolehierarchy_with_classes",
                    "test002.inlined",
                    "SubSubTeam11",
                    "R1");
        
        _implSubSubRole12 =
            getRole(getTestProjectDir(),
                    "complete_rolehierarchy_with_classes",
                    "test002.inlined",
                    "SubSubTeam12",
                    "R1");
        
        _class =
            getType(getTestProjectDir(), 
                "complete_rolehierarchy_with_classes", 
                "test002.standard", 
                "AClass");
        
        _superClass =
            getType(getTestProjectDir(), 
                    "complete_rolehierarchy_with_classes", 
                    "test002.standard", 
                    "SuperClass");
        
        _subClass1 =
            getType(getTestProjectDir(), 
                    "complete_rolehierarchy_with_classes", 
                    "test002.standard", 
                    "SubClass1");
        
        _subClass2 =
            getType(getTestProjectDir(), 
                    "complete_rolehierarchy_with_classes", 
                    "test002.standard", 
                    "SubClass2");
        
        _subSubClass11 =
            getType(getTestProjectDir(), 
                    "complete_rolehierarchy_with_classes", 
                    "test002.standard", 
                    "SubSubClass11");
        
        _subSubClass12 =
            getType(getTestProjectDir(), 
                    "complete_rolehierarchy_with_classes", 
                    "test002.standard", 
                    "SubSubClass12");
        
        _object = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");

        
    }
    
    public void testContainment_implSuperRole()
    {
        assertTrue(_hierarchy.contains((IType)_implSuperRole.getCorrespondingJavaElement()));
    }
 
    public void testGetExplicitSuperclass_ofFocusRole() throws JavaModelException
    {
        assertEquals(_class, _hierarchy.getExplicitSuperclass((IType)_focusRole.getCorrespondingJavaElement()));
    }        
    
    
    public void testGetExplicitSuperclass_ofImplSuperSuperRole() throws JavaModelException
    {
        assertEquals(_object, _hierarchy.getExplicitSuperclass((IType)_implSuperSuperRole.getCorrespondingJavaElement()));
    }
    
    
    public void testGetExplicitSuperclass_ofImplSuperRole() throws JavaModelException
    {
        assertEquals(_class, _hierarchy.getExplicitSuperclass((IType)_implSuperRole.getCorrespondingJavaElement()));
    }

    
    public void testGetExplicitSuperclass_ofImplSubRole1() throws JavaModelException
    {
        assertEquals(_class, _hierarchy.getExplicitSuperclass((IType)_implSubRole1.getCorrespondingJavaElement()));
    }
    
    
    public void testGetExplicitSuperclass_ofImplSubRole2() throws JavaModelException
    {
        assertEquals(_subSubClass11, _hierarchy.getExplicitSuperclass((IType)_implSubRole2.getCorrespondingJavaElement()));
    }
    
    
    public void testGetExplicitSuperclass_ofImplSubSubRole11() throws JavaModelException
    {
        assertEquals(_subClass2, _hierarchy.getExplicitSuperclass((IType)_implSubSubRole11.getCorrespondingJavaElement()));
    }
    
    
    public void testGetExplicitSuperclass_ofImplSubSubRole12() throws JavaModelException
    {
        assertEquals(_subClass1, _hierarchy.getExplicitSuperclass((IType)_implSubSubRole12.getCorrespondingJavaElement()));
    }
    
    
    public void testGetAllClasses()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_focusRole);
        expectedList.add(_implSubRole1);
        expectedList.add(_implSubRole2);
        expectedList.add(_implSubSubRole11);
        expectedList.add(_implSubSubRole12);
        expectedList.add(_object);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_subClass1);
        expectedList.add(_subClass2);
        expectedList.add(_subSubClass11);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        IType[] actual = _hierarchy.getAllClasses();
        
        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllTypes()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_focusRole);
        expectedList.add(_implSubRole1);
        expectedList.add(_implSubRole2);
        expectedList.add(_implSubSubRole11);
        expectedList.add(_implSubSubRole12);
        expectedList.add(_superClass);
        expectedList.add(_object);
        expectedList.add(_class);
        expectedList.add(_subClass1);
        expectedList.add(_subClass2);
        expectedList.add(_subSubClass11);
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        
        assertTrue(compareTypes(expected, _hierarchy.getAllTypes()));
    }
    
    public void testGetAllSuperclasses_ofFocusRole()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_object);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]); 
        IType[] actual = _hierarchy.getAllSuperclasses((IType)_focusRole.getCorrespondingJavaElement());
        
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSuperclasses_ofImplSuperSuperRole()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_object);
    
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        IType[] actual = _hierarchy.getAllSuperclasses((IType)_implSuperSuperRole.getCorrespondingJavaElement());
        
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSuperclasses_ofImplSuperRole()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_object);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        IType[] actual = _hierarchy.getAllSuperclasses((IType)_implSuperRole.getCorrespondingJavaElement());

        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSuperclasses_ofImplSubRole1()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_focusRole);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_object);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        IType[] actual = _hierarchy.getAllSuperclasses((IType)_implSubRole1.getCorrespondingJavaElement());
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSuperclasses_ofImplSubRole2()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_focusRole);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_subClass1);
        expectedList.add(_subSubClass11);
        expectedList.add(_object);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        IType[] actual = _hierarchy.getAllSuperclasses((IType)_implSubRole2.getCorrespondingJavaElement());
        
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSuperclasses_ofImplSubSubRole11()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_focusRole);
        expectedList.add(_implSubRole1);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_subClass2);
        expectedList.add(_object);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        IType[] actual = _hierarchy.getAllSuperclasses((IType)_implSubSubRole11.getCorrespondingJavaElement());
        
		assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSuperclasses_ofImplSubSubRole12()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_focusRole);
        expectedList.add(_implSubRole1);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_subClass1);
        expectedList.add(_object);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);        
        IType[] actual = _hierarchy.getAllSuperclasses((IType)_implSubSubRole12.getCorrespondingJavaElement());
        
		assertTrue(compareTypes(expected, actual));
    }
    
    
    public void testGetAllSupertypes_ofFocusRole()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_object);
        expectedList.add(_superClass);
        expectedList.add(_class);
       
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]); 
        IType[] actual = _hierarchy.getAllSupertypes((IType)_focusRole.getCorrespondingJavaElement());
       
        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSupertypes_ofImplSuperSuperRole()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_object);

        IType[] expected = expectedList.toArray(new IType[expectedList.size()]); 
        IType[] actual = _hierarchy.getAllSupertypes((IType)_implSuperSuperRole.getCorrespondingJavaElement());

        assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSupertypes_ofImplSuperRole()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_object);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);        
        IType[] actual = _hierarchy.getAllSupertypes((IType)_implSuperRole.getCorrespondingJavaElement());
        
        assertEquals(expected.length, actual.length);
		assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSupertypes_ofImplSubRole1()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_focusRole);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_object);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        IType[] actual = _hierarchy.getAllSupertypes((IType)_implSubRole1.getCorrespondingJavaElement());
        
		assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSupertypes_ofImplSubRole2()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_focusRole);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_subClass1);
        expectedList.add(_subSubClass11);
        expectedList.add(_object);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        IType[] actual = _hierarchy.getAllSupertypes((IType)_implSubRole2.getCorrespondingJavaElement());
        
        assertEquals(expected.length, actual.length);
		assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSupertypes_ofImplSubSubRole11()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_focusRole);
        expectedList.add(_implSubRole1);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_subClass2);
        expectedList.add(_object);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        IType[] actual = _hierarchy.getAllSupertypes((IType)_implSubSubRole11.getCorrespondingJavaElement());
        
        assertEquals(expected.length, actual.length);
		assertTrue(compareTypes(expected, actual));
    }
    
    public void testGetAllSupertypes_ofImplSubSubRole12()
    {
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_implSuperSuperRole);
        expectedList.add(_implSuperRole);
        expectedList.add(_focusRole);
        expectedList.add(_implSubRole1);
        expectedList.add(_superClass);
        expectedList.add(_class);
        expectedList.add(_subClass1);
        expectedList.add(_object);
        
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        IType[] actual = _hierarchy.getAllSupertypes((IType)_implSubSubRole12.getCorrespondingJavaElement());
        
        assertEquals(expected.length, actual.length);
		assertTrue(compareTypes(expected, actual));
    }
    
    // succeeds although hierarchy was not built for type.
    public void testGetAllSubtypes_ofSubClass2()
    {
        ArrayList<IRoleType> expectedList = new ArrayList<IRoleType>();
        expectedList.add(_implSubSubRole11);
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        
        assertTrue(compareTypes(expected, _hierarchy.getAllSubtypes(_subClass2)));       
    }

    // succeeds although hierarchy was not built for type.
    public void testGetAllSubtypes_ofSubSubClass11()
    {
        ArrayList<IRoleType> expectedList = new ArrayList<IRoleType>();
        expectedList.add(_implSubRole2);
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        
        assertTrue(compareTypes(expected, _hierarchy.getAllSubtypes(_subSubClass11)));       
    }

    // succeeds although hierarchy was not built for type.
    public void testGetAllSubtypes_ofSubSubClass12()
    {
        IType[] expected = new IType[0];
        assertTrue(compareTypes(expected, _hierarchy.getAllSubtypes(_subSubClass12)));       
    }
    
    public void testGetAllSubtypes_ofImplSuperSuperRole()
    {
        ArrayList<IRoleType> expectedList = new ArrayList<IRoleType>();
        expectedList.add(_implSuperRole);
        expectedList.add(_focusRole);
        expectedList.add(_implSubRole1);
        expectedList.add(_implSubRole2);
        expectedList.add(_implSubSubRole11);
        expectedList.add(_implSubSubRole12);
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        
        assertTrue(compareTypes(expected, _hierarchy.getAllSubtypes((IType)_implSuperSuperRole.getCorrespondingJavaElement())));                
    }
}
