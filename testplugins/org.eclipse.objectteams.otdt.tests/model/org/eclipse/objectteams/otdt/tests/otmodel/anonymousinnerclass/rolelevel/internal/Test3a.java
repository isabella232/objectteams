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
 * $Id: Test3a.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.anonymousinnerclass.rolelevel.internal;

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
 * $Id: Test3a.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * testcase:
 *  a role class (defined insight the file of its team class) with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is a role class with an after-callin method mapping 
 */
public class Test3a extends AttributeAssignmentTest
{

    private final String ANONYMOUS_MAPPING_NAME = "roleMethod() <- baseMethod()";
    private final String SUPERROLE_NAME = "Role1"; 
    private final String MAPPED_ROLEMETHOD_NAME = "roleMethod";
    private final String BASE_PKG = SRC_FOLDER + "." + "basepkg";
    private final String BASE_NAME = "SampleBase";
    private final String MAPPED_BASEMETHOD_NAME = "baseMethod";
 
    
    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test3a.class);
        }
        junit.framework.TestSuite suite = new Suite(Test3a.class
            .getName());
        return suite;
    }    

    public Test3a(String name)
    {
        super(name);
    }    
    
    protected String getTeamName()
    {
        return "Test3a_SampleTeam";
    }
    
    protected String getRoleName()
    {
        return "Role2";
    }
 
    public void testExistenceOfAnonymousTypeInOTModel() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);
        
        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);
    }
    
    public void testRolePropertyOfAnonymousType() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);
        
        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);
        
        assertTrue(anonymousTypeOTElem.isRole());
        assertTrue(anonymousTypeOTElem instanceof IRoleType);
    }
    
    public void testContainmentOfMethodMappingInAnonymousType() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);
        
        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);
        
        assertTrue(anonymousTypeOTElem instanceof IRoleType);
        IRoleType role = (IRoleType) anonymousTypeOTElem;
        assertNotNull(role);
        
        IMethodMapping[] mappings = role.getMethodMappings();
        assertNotNull(mappings);
        assertTrue(mappings.length == 1);
        assertEquals(mappings[0].getElementName(), ANONYMOUS_MAPPING_NAME);
    }

    public void testMappingPropertyBoundRoleMethod() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);
        
        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);
        
        assertTrue(anonymousTypeOTElem instanceof IRoleType);
        IRoleType role = (IRoleType) anonymousTypeOTElem;
        assertNotNull(role);
        
        IMethodMapping[] mappings = role.getMethodMappings();
        assertNotNull(mappings);
        assertTrue(mappings.length == 1);
        assertEquals(mappings[0].getElementName(), ANONYMOUS_MAPPING_NAME);

        
        ICompilationUnit teamUnit = getCompilationUnit(
                PROJECT,
                SRC_FOLDER,
                TEAM_PKG,
                getTeamName() +".java");
        IType teamJavaElem = teamUnit.getType(getTeamName());

        IType superRoleJavaElem = teamJavaElem.getType(SUPERROLE_NAME);
        assertNotNull(superRoleJavaElem);
        assertTrue(superRoleJavaElem.exists());
        
        IMethod superRoleMethod = superRoleJavaElem.getMethod(MAPPED_ROLEMETHOD_NAME, new String[0]);
        assertNotNull(superRoleMethod);
        
        assertEquals(mappings[0].getRoleMethod(), superRoleMethod);
    }
    
    public void testCallinPropertyOfMethodMapping() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);
        
        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);
        
        assertTrue(anonymousTypeOTElem instanceof IRoleType);
        IRoleType role = (IRoleType) anonymousTypeOTElem;
        assertNotNull(role);
        
        IMethodMapping[] mappings = role.getMethodMappings();
        assertNotNull(mappings);
        assertTrue(mappings.length == 1);
        assertEquals(mappings[0].getElementName(), ANONYMOUS_MAPPING_NAME);
        
        assertTrue(mappings[0].getMappingKind() == IOTJavaElement.CALLIN_MAPPING);
        assertTrue(mappings[0] instanceof ICallinMapping);
    }
    
    public void testAfterPropertyOfCallinMethodMapping() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);
        
        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);
        
        assertTrue(anonymousTypeOTElem instanceof IRoleType);
        IRoleType role = (IRoleType) anonymousTypeOTElem;
        assertNotNull(role);
        
        IMethodMapping[] mappings = role.getMethodMappings();
        assertNotNull(mappings);
        assertTrue(mappings.length == 1);
        assertEquals(mappings[0].getElementName(), ANONYMOUS_MAPPING_NAME);
        
        assertTrue(mappings[0] instanceof ICallinMapping);
        ICallinMapping callinMapping = (ICallinMapping) mappings[0];
        assertNotNull(callinMapping);
        
        assertTrue(callinMapping.getCallinKind() == ICallinMapping.KIND_AFTER);
    }
    
    public void testMappingPropertyBoundBaseMethod() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);
        
        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);
        
        assertTrue(anonymousTypeOTElem instanceof IRoleType);
        IRoleType role = (IRoleType) anonymousTypeOTElem;
        assertNotNull(role);
        
        IMethodMapping[] mappings = role.getMethodMappings();
        assertNotNull(mappings);
        assertTrue(mappings.length == 1);
        assertEquals(mappings[0].getElementName(), ANONYMOUS_MAPPING_NAME);
        
        assertTrue(mappings[0] instanceof ICallinMapping);
        ICallinMapping callinMapping = (ICallinMapping)  mappings[0];

        IMethod[] boundBaseMethods = callinMapping.getBoundBaseMethods();
        assertNotNull(boundBaseMethods);
        assertTrue(boundBaseMethods.length == 1);        
        
        //-------------------
        
        ICompilationUnit baseUnit = getCompilationUnit(
                PROJECT,
                SRC_FOLDER,
                BASE_PKG,
                BASE_NAME +".java");
        IType baseJavaElem = baseUnit.getType(BASE_NAME);        
        assertNotNull(baseJavaElem);
        assertTrue(baseJavaElem.exists());
        
        IMethod baseMethod = baseJavaElem.getMethod(MAPPED_BASEMETHOD_NAME, new String[0]);
        assertNotNull(baseMethod);
        assertTrue(baseMethod.exists());
        
        //-------------------
        assertEquals(boundBaseMethods[0], baseMethod);
    }
}
