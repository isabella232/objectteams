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
 * $Id: Test5c.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toteam;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

/**
 * $Id: Test5c.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * This class contains testing methods for a test setting with a role class with a method
 * and a method mapping
 * whereas the method is concrete and has no parameters,
 * the method mapping is a before-callin mapping
 * and the role class is bound to a baseclass which is a team class
 */
public class Test5c extends Test5_MethodMappingGeneral
{

    private int _expectedCallinKind;
    
    public Test5c(String name)
    {
        super(name);
        setMappingName("roleMethod <- baseMethod");
        setExpectedCallinKind(ICallinMapping.KIND_BEFORE);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test5c.class);
        }
        junit.framework.TestSuite suite = new Suite(Test5c.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        getTestSetting().setTeamClass("Team_5c");
    }
    
    protected int getExpectedCallinKind()
    {
        return _expectedCallinKind;
    }
   
    protected void setExpectedCallinKind(int kind)
    {
        _expectedCallinKind = kind;
    }
    
    public void testMappingCallinProperty() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());
        
        IOTType roleOTElem = OTModelManager.getOTElement(getTestSetting().getRoleJavaElement());
        assertNotNull(roleOTElem);
        assertTrue(roleOTElem instanceof IRoleType);
        IRoleType roleRoleOTElem = (IRoleType) roleOTElem;        

        IMethodMapping[] mappings = roleRoleOTElem.getMethodMappings();
        assertTrue(mappings.length == 1);
        assertEquals(mappings[0].getElementName(), getMappingName());
        
        assertTrue(mappings[0].getMappingKind() == IOTJavaElement.CALLIN_MAPPING);
        assertTrue(mappings[0] instanceof ICallinMapping);
    }
    
    
    public void testMappingPropertyBoundBaseMethod() throws JavaModelException
    {
        ICompilationUnit baseUnit = getCompilationUnit(
                getTestProjectDir(),
                "boundtoteam",
                "boundtoteam.teampkg",
                "TeamC.java");
            
        IType baseJavaElem = baseUnit.getType("TeamC");
        assertNotNull(baseJavaElem);
        assertTrue(baseJavaElem.exists());
        
        IMethod baseMethod = baseJavaElem.getMethod(getBaseMethodName(), new String[0]);
        assertNotNull(baseMethod);
        assertTrue(baseMethod.exists());
        
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());
        
        IOTType roleOTElem = OTModelManager.getOTElement(getTestSetting().getRoleJavaElement());
        assertNotNull(roleOTElem);
        assertTrue(roleOTElem instanceof IRoleType);
        IRoleType roleRoleOTElem = (IRoleType) roleOTElem;
        
        IMethodMapping[] mappings = roleRoleOTElem.getMethodMappings();
        assertTrue(mappings.length == 1);
        assertTrue(mappings[0] instanceof ICallinMapping);
        
        ICallinMapping callinMapping = (ICallinMapping) mappings[0];
        assertNotNull(callinMapping);
        IMethod[] boundBaseMethods = callinMapping.getBoundBaseMethods();
        
        assertTrue(boundBaseMethods.length == 1);
        assertEquals(boundBaseMethods[0], baseMethod);
    }
    
    public void testCallinKind() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());
        
        IOTType roleOTElem = OTModelManager.getOTElement(getTestSetting().getRoleJavaElement());
        assertNotNull(roleOTElem);
        assertTrue(roleOTElem instanceof IRoleType);
        IRoleType roleRoleOTElem = (IRoleType) roleOTElem;        

        IMethodMapping[] mappings = roleRoleOTElem.getMethodMappings();
        assertTrue(mappings.length == 1);
        assertEquals(mappings[0].getElementName(), getMappingName());
        
        assertTrue(mappings[0] instanceof ICallinMapping);
        ICallinMapping callinMapping = (ICallinMapping) mappings[0];
        
        assertTrue(callinMapping.getCallinKind() == getExpectedCallinKind());
    }
    
    public void testRelationRoleToBase() throws JavaModelException
    {
        ICompilationUnit baseUnit = getCompilationUnit(
                getTestProjectDir(),
                "boundtoteam",
                "boundtoteam.teampkg",
                "TeamC.java");
        IType baseJavaElem = baseUnit.getType("TeamC");
        assertNotNull(baseJavaElem);
        assertTrue(baseJavaElem.exists());        
        
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());

        IOTType roleOTElem = OTModelManager.getOTElement(getTestSetting().getRoleJavaElement());
        assertNotNull(roleOTElem);
        assertTrue(roleOTElem instanceof IRoleType);
        IRoleType roleRoleOTElem = (IRoleType) roleOTElem;
        
        assertEquals(baseJavaElem, roleRoleOTElem.getBaseClass());
    } 
    
}
