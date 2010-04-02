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
 * $Id: Test5_MethodMappingGeneral.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.role.bound.toordinary;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

/**
 * @author kaschja
 * @version $Id: Test5_MethodMappingGeneral.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * This class contains testing methods for a test setting with a role class with a method
 * and a method mapping
 * whereas the method has no parameters
 * and the role class is bound to a baseclass
 */
public abstract class Test5_MethodMappingGeneral extends Test1
{
    private String _roleMethodName   = "roleMethod";
    private String _baseMethodName = "baseMethod";    
    private String _mappingName;
    
    public Test5_MethodMappingGeneral(String name)
    {
        super(name);
    }
    
    protected String getMappingName()
    {
        return _mappingName;
    }
    
    protected String getRoleMethodName()
    {
        return _roleMethodName;
    }
    
    protected String getBaseMethodName()
    {
        return _baseMethodName;
    }
    
    protected void setMappingName(String mappingName)
    {
        _mappingName = mappingName;
    }
    
    
    public void testContainmentOfMethodInRoleClass() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());

        IMethod method = getTestSetting().getRoleJavaElement().getMethod(getRoleMethodName(), new String[0]);
        assertNotNull(method);
        assertTrue(method.exists());        
    }
    
    
    public void testExistenceOfMethodMapping() throws JavaModelException
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
    }
    
    public void testMappingPropertyBoundRoleMethod() throws JavaModelException
    {
        assertNotNull(getTestSetting().getRoleJavaElement());
        assertTrue(getTestSetting().getRoleJavaElement().exists());
        
        IOTType roleOTElem = OTModelManager.getOTElement(getTestSetting().getRoleJavaElement());
        assertNotNull(roleOTElem);
        assertTrue(roleOTElem instanceof IRoleType);
        IRoleType roleRoleOTElem = (IRoleType) roleOTElem;        

        IMethodMapping[] mappings = roleRoleOTElem.getMethodMappings();
        assertTrue(mappings.length == 1);
        
        IMethod roleMethod = getTestSetting().getRoleJavaElement().getMethod(getRoleMethodName(), new String[0]);
        assertNotNull(roleMethod);
        assertTrue(roleMethod.exists());
        
        IMethod boundRoleMethod = mappings[0].getRoleMethod();
        assertNotNull(boundRoleMethod);
        assertTrue(roleMethod.exists());
        
        assertEquals(boundRoleMethod, roleMethod);
    }    
}
