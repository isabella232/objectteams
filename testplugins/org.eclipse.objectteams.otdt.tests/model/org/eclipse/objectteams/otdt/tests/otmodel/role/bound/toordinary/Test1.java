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
package org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toordinary;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.tests.otmodel.RetargetableFileBasedModelTest;

/**
 * @author kaschja
 * @version $Id: Test1.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * This class contains testing methods for a test setting with an empty role class
 * whereas the role class is bound to a baseclass
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
        IJavaElement[] childrenOfTeam = teamOTElem.getRoleTypes();
//haebor}        
        assertNotNull(childrenOfTeam);
        assertEquals(1, childrenOfTeam.length);
        assertEquals(childrenOfTeam[0].getElementName(), roleOTElem.getElementName());

// FIXME(SH): check whether there is any use in supporting getInnerTypes()
////{OTModelUpdate
//        IType[] innerTypesOfTeam = teamOTElem.getInnerTypes();
////haebor}        
//        assertNotNull(innerTypesOfTeam);
//        assertTrue(innerTypesOfTeam.length == 1);
//        assertEquals(innerTypesOfTeam[0].getElementName(), roleOTElem.getElementName());
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
        
        assertEquals(roleRoleOTElem.getTeamJavaType(), teamOTElem.getCorrespondingJavaElement());
        assertEquals(roleRoleOTElem.getTeam(), teamOTElem);
    }
    
    public void testRelationRoleToBase() throws JavaModelException
    {
        // TODO(jwl): Resource access hardcoded here!
        ICompilationUnit baseUnit = getCompilationUnit(
                getTestProjectDir(),
                "boundtoordinary",
                "boundtoordinary.basepkg",
                "SampleBase.java");
            
        IType baseJavaElem = baseUnit.getType("SampleBase");
        assertNotNull(baseJavaElem);
        assertTrue(baseJavaElem.exists());        
        
        IType curRole = getTestSetting().getRoleJavaElement();
        
        assertNotNull(curRole);
        assertTrue(curRole.exists());

        IOTType roleOTElem = OTModelManager.getOTElement(curRole);
        assertNotNull(roleOTElem);
        assertTrue(roleOTElem instanceof IRoleType);

        IRoleType roleRoleOTElem = (IRoleType) roleOTElem;
        assertEquals(baseJavaElem, roleRoleOTElem.getBaseClass());
    }        
}
