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
 * $Id: OrdinaryClassesHierarchyWithSubRolesTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.superhierarchy;

import java.util.ArrayList;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author kaschja
 * @version $Id: OrdinaryClassesHierarchyWithSubRolesTest.java 23494 2010-02-05 23:06:44Z stephan $
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
 * The focus type is SuperClass .
 */
public class OrdinaryClassesHierarchyWithSubRolesTest extends FileBasedModelTest
{
    private ITypeHierarchy _testObj;
    
    private IRoleType _aRole;
    private IRoleType _implSuperRole;
    @SuppressWarnings("unused")
	private IRoleType _implSuperSuperRole;
    private IRoleType _implSubRole1;
    private IRoleType _implSubRole2;
    private IRoleType _implSubSubRole11;
    private IRoleType _implSubSubRole12;
    private IType     _aClass;
    private IType     _superClass;
    private IType     _subClass1;
    private IType     _subClass2;
    private IType     _subSubClass11;
    private IType     _subSubClass12;
    
    
    public OrdinaryClassesHierarchyWithSubRolesTest(String name)
    {
        super(name);
    }
    
    public static Test suite()
    {
        if (true)
        {
            return new Suite(OrdinaryClassesHierarchyWithSubRolesTest.class);
        }
        junit.framework.TestSuite suite = 
            new Suite(OrdinaryClassesHierarchyWithSubRolesTest.class.getName());
        return suite;
    }
    
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("Hierarchy");
        super.setUpSuite();
        
        _aRole = 
            getRole(getTestProjectDir(),
                "complete_rolehierarchy_with_classes",
                "test002.inlined",
                "ATeam",
                "R1");
        
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
                "SuperTeam",
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
        
        _aClass =
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
        
    }
    
    public void testGetAllSubtypes_ofSuperClass() throws JavaModelException
    {
        assertNotNull(_superClass);
        assertTrue(_superClass.exists());
        _testObj = new OTTypeHierarchy(_superClass, _superClass.getJavaProject(), true);
        _testObj.refresh(new NullProgressMonitor());
        
        ArrayList<IType> expectedList = new ArrayList<IType>();
        expectedList.add(_aClass);
        expectedList.add(_subClass1);
        expectedList.add(_subClass2);
        expectedList.add(_subSubClass11);
        expectedList.add(_subSubClass12);
        
        expectedList.add(_implSuperRole);
        expectedList.add(_aRole);
        expectedList.add(_implSubRole1);        
        expectedList.add(_implSubRole2);
        expectedList.add(_implSubSubRole11);
        expectedList.add(_implSubSubRole12);
        IType[] expected = expectedList.toArray(new IType[expectedList.size()]);
        IType[] actual = _testObj.getAllSubtypes(_superClass);
        
        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }
}
