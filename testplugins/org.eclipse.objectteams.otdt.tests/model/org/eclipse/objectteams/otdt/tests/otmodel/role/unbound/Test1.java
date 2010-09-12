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
 * $Id: Test1.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.role.unbound;

import junit.framework.Test;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleFileType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.tests.otmodel.RetargetableFileBasedModelTest;

/**
 * @author kaschja
 * @version $Id: Test1.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * This class contains testing methods for a test setting with an empty role class
 */
public class Test1 extends RetargetableFileBasedModelTest
{
    public Test1(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test1.class);
        }
        junit.framework.TestSuite suite = new Suite(Test1.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        getTestSetting().setTeamClass("Team_1");
    }
    
    protected void setUp() throws Exception
    {
		super.setUp();
        getTestSetting().setUp();
    }
    
    public void testExistenceOfTypesInJavaModel() throws JavaModelException
    {
        assertNotNull(getTestSetting().getTeamJavaElement());
        assertTrue(getTestSetting().getTeamJavaElement().exists());
        
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists()); 
    }
    
    public void testExistenceofTypeInOTModel() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());
        
        IOTType roleOTElem = OTModelManager.getOTElement(getTestSetting().getRoleJavaElement());
        assertNotNull(roleOTElem);
    }
    
    public void testRoleProperty() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());
        
        IOTType roleOTElem = OTModelManager.getOTElement(getTestSetting().getRoleJavaElement());
        assertNotNull(roleOTElem);
        
        assertTrue(roleOTElem.isRole());
        assertTrue(roleOTElem instanceof IRoleType);
    }
    
    public void testRelationTeamToRole() throws JavaModelException
    {
        assertNotNull(getTestSetting().getTeamJavaElement());
        assertTrue(getTestSetting().getTeamJavaElement().exists());
        
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());
        
        IOTType teamOTElem = OTModelManager.getOTElement(getTestSetting().getTeamJavaElement());
        assertNotNull(teamOTElem);
        
        IOTType roleOTElem = OTModelManager.getOTElement(getTestSetting().getRoleJavaElement());
        assertNotNull(roleOTElem);

//{OTModelUpdate
        IJavaElement[] childrenOfTeam = teamOTElem.getChildren();
//haebor}        
        assertNotNull(childrenOfTeam);
        if (roleOTElem instanceof IRoleFileType) // role files are not members of the team!
	        assertTrue(childrenOfTeam.length == 0);
        else {
            assertTrue(childrenOfTeam.length == 1);
            assertEquals(childrenOfTeam[0].getElementName(), roleOTElem.getElementName());
        }

//{OTModelUpdate        
        IType[] innerTypesOfTeam = teamOTElem.getInnerTypes();
//haebor}        
        assertNotNull(innerTypesOfTeam);
        
        if (roleOTElem instanceof IRoleFileType) // role files are not members of the team!
	        assertTrue(innerTypesOfTeam.length == 0);
        else {
	        assertTrue(innerTypesOfTeam.length == 1);
	        assertEquals(innerTypesOfTeam[0].getElementName(), roleOTElem.getElementName());
        }
    }
    
    public void testRelationRoleToTeam() throws JavaModelException
    {
        assertNotNull(getTestSetting().getTeamJavaElement());
        assertTrue(getTestSetting().getTeamJavaElement().exists());
        
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());
        
        IOTType teamOTElem = OTModelManager.getOTElement(getTestSetting().getTeamJavaElement());
        assertNotNull(teamOTElem);
        
        IOTType roleOTElem = OTModelManager.getOTElement(getTestSetting().getRoleJavaElement());
        assertNotNull(roleOTElem);

        assertTrue(roleOTElem instanceof IRoleType);
        IRoleType roleRoleOTElem = (IRoleType) roleOTElem;
        
        assertEquals(roleRoleOTElem.getTeam(), teamOTElem);
        if (roleRoleOTElem instanceof IRoleFileType) {
            assertFalse(roleRoleOTElem.getParent().equals(teamOTElem)); // role file has CU as parent
        }
        else {
            assertEquals(roleRoleOTElem.getParent(), teamOTElem);
        }
    }
    
    public void testGetRoles() throws JavaModelException
    {
        assertNotNull(getTestSetting().getTeamJavaElement());
        assertTrue(getTestSetting().getTeamJavaElement().exists());
        
        IOTType teamOTElem = OTModelManager.getOTElement(getTestSetting().getTeamJavaElement());
        assertNotNull(teamOTElem);

        IType[] actual = teamOTElem.getRoleTypes();
        IType[] expected = getTestSetting().getRoleJavaElements();
        
        assertEquals("Different number of roles!", expected.length, actual.length);
        assertTrue("Different set of roles!", compareTypes(expected, actual));
    }
    
}
