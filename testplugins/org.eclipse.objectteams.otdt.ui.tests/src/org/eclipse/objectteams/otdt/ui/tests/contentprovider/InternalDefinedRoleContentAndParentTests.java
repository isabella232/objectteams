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
 * $Id: InternalDefinedRoleContentAndParentTests.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.contentprovider;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.ui.tests.FileBasedUITest;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerContentProvider;

/**
 * @author kaschja
 * @version $Id: InternalDefinedRoleContentAndParentTests.java 23495 2010-02-05 23:15:16Z stephan $
 *
 * This test class contains test methods that test the methods
 * PackageExplorerContentProvider.getChildren and PackageExplorerContentProvider.getParent
 * with several internal defined role classes as argument
 */
@SuppressWarnings("restriction")
public class InternalDefinedRoleContentAndParentTests extends FileBasedUITest
{
	final int METHOD_IDX = 0;
	final int MAPPING_IDX = 1;
    
    private PackageExplorerContentProvider _testObj;
    
    public InternalDefinedRoleContentAndParentTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(InternalDefinedRoleContentAndParentTests.class);
        }
        junit.framework.TestSuite suite = new Suite(InternalDefinedRoleContentAndParentTests.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("InternalDefinedRole");
        
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _testObj = new PackageExplorerContentProvider(true);
    }
 

    private IType getJavaType(String projectName, String srcFolderName, String pkgName, String typeName) throws JavaModelException
    {
        ICompilationUnit typeUnit = getCompilationUnit(
                projectName,
                srcFolderName,
                pkgName,
                typeName +".java");
        IType typeJavaElem = typeUnit.getType(typeName);
       
        if ((typeJavaElem != null) && (typeJavaElem.exists()))
        {
            return typeJavaElem;
        }
        return null;
    }    
    
    private IOTJavaElement getRole(String srcFolderName, String pkgName, String teamName, String roleName) throws JavaModelException
    {
        IType teamJavaElem = getJavaType(getTestProjectDir(), srcFolderName, pkgName, teamName);
        
        if (teamJavaElem != null)
        {
            IType roleJavaElem = teamJavaElem.getType(roleName);
            if ((roleJavaElem != null) && (roleJavaElem.exists()))
            {
                IOTJavaElement roleOTElem = OTModelManager.getOTElement(roleJavaElem);
                if (roleOTElem != null)
                {
                    return roleOTElem;
                }
            }
        }
        return null;
    }
    
    
    public void testRoleContent_EmptyRole() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_1", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object[] children = _testObj.getChildren(roleOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 0);
    }
    
    public void testRoleContent_Field() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_2", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object[] children = _testObj.getChildren(roleOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IField);
        
        IField field = (IField) children[0];
        assertEquals(field.getElementName(), "roleAttr");
        assertEquals(field.getTypeSignature(), "QString;");
    }
 
    public void testRoleContent_ParameterlessMethod() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3a", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object[] children = _testObj.getChildren(roleOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IMethod);
        
        IMethod method = (IMethod) children[0];
        assertEquals(method.getElementName(), "roleMethod");
        assertEquals(method.getSignature(), "()V");
    }
    
    public void testRoleContent_MethodWithParameter() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3b", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object[] children = _testObj.getChildren(roleOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IMethod);
        
        IMethod method = (IMethod) children[0];
        assertEquals(method.getElementName(), "roleMethod");
        assertEquals(method.getSignature(), "(QString;)V");        
    }

    public void testRoleContent_MethodWithReturnValue() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3c", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object[] children = _testObj.getChildren(roleOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IMethod);
        
        IMethod method = (IMethod) children[0];
        assertEquals(method.getElementName(), "roleMethod");
        assertEquals(method.getSignature(), "()QString;");        
    }
    
    public void testRoleContent_MethodWithThrowClause() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3d", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object[] children = _testObj.getChildren(roleOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IMethod);
        
        IMethod method = (IMethod) children[0];
        assertEquals(method.getElementName(), "roleMethod");
        assertEquals(method.getSignature(), "()V");
        
        String[] exceptionTypes = method.getExceptionTypes();
        assertNotNull(exceptionTypes);
        assertTrue(exceptionTypes.length == 1);
        assertEquals(exceptionTypes[0], "QException;");
    }
    
    public void testRoleContent_Innerclass() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_4a", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object[] children = _testObj.getChildren(roleOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IType);
        
        IType innerType = (IType) children[0];
        assertEquals(innerType.getElementName(), "AnInnerClass");
    }    

    public void testRoleContent_InnerTeamClass() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_4b", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object[] children = _testObj.getChildren(roleOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IType);
        
        IType innerType = (IType) children[0];
        assertEquals(innerType.getElementName(), "AnInnerTeamClass");
    }
    
    
    public void testRoleContent_BoundRoleWithAbstractMethodAndCalloutMapping() throws JavaModelException
    {
      IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5a", "SampleRole");
      assertNotNull(roleOTElem);
      
      Object[] children = _testObj.getChildren(roleOTElem);
      assertNotNull(children);
      
      //*********
//      Logger.println(children.length);
//      for (int idx = 0; idx < children.length; idx++)
//     {
//        Logger.println(children[idx].toString());
//      }
      assertTrue(children.length == 2);
      //*********
      
      assertTrue(children[MAPPING_IDX] instanceof ICalloutMapping);
      assertEquals("roleMethod -> baseMethod", ((ICalloutMapping)children[MAPPING_IDX]).getElementName());
      
      assertTrue(children[METHOD_IDX] instanceof IMethod);
      IMethod method = (IMethod) children[METHOD_IDX];
      assertEquals(method.getElementName(), "roleMethod");
      assertEquals(method.getSignature(), "()V");        
    }
    
    public void testRoleContent_BoundRoleWithConcreteMethodAndCalloutMapping() throws JavaModelException
    {
      IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5b", "SampleRole");
      assertNotNull(roleOTElem);
      
      Object[] children = _testObj.getChildren(roleOTElem);
      assertNotNull(children);
      assertTrue(children.length == 2);
      
      assertTrue(children[MAPPING_IDX] instanceof ICalloutMapping);
      //TODO (kaschja) change mapping name by the time there is a distinction of '->' and '=>' in the OT-Model
      assertEquals("roleMethod -> baseMethod", ((ICalloutMapping)children[MAPPING_IDX]).getElementName());
      
      assertTrue(children[METHOD_IDX] instanceof IMethod);
      IMethod method = (IMethod) children[METHOD_IDX];
      assertEquals(method.getElementName(), "roleMethod");
      assertEquals(method.getSignature(), "()V");        
    }
    
    public void testRoleContent_BoundRoleWithMethodAndBeforeCallinMapping() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5c", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object[] children = _testObj.getChildren(roleOTElem);
        assertNotNull(children);
        assertTrue(children.length == 2);
        
        assertTrue(children[MAPPING_IDX] instanceof ICallinMapping);
        ICallinMapping mapping = (ICallinMapping)children[MAPPING_IDX];
        assertEquals("roleMethod <- baseMethod", mapping.getElementName());
        assertTrue(mapping.getCallinKind() == ICallinMapping.KIND_BEFORE);
        
        assertTrue(children[METHOD_IDX] instanceof IMethod);
        IMethod method = (IMethod) children[METHOD_IDX];
        assertEquals(method.getElementName(), "roleMethod");
        assertEquals(method.getSignature(), "()V");                
    }
    
    public void testRoleContent_BoundRoleWithMethodAndReplaceCallinMapping() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5d", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object[] children = _testObj.getChildren(roleOTElem);
        assertNotNull(children);
        assertTrue(children.length == 2);
        
        assertTrue(children[MAPPING_IDX] instanceof ICallinMapping);
        ICallinMapping mapping = (ICallinMapping)children[MAPPING_IDX];
        assertEquals("roleMethod <- baseMethod", mapping.getElementName());
        assertTrue(mapping.getCallinKind() == ICallinMapping.KIND_REPLACE);
        
        assertTrue(children[METHOD_IDX] instanceof IMethod);
        IMethod method = (IMethod) children[METHOD_IDX];
        assertEquals(method.getElementName(), "roleMethod");
        assertEquals(method.getSignature(), "()V");                
    }
    
    public void testRoleContent_BoundRoleWithMethodAndAfterCallinMapping() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5e", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object[] children = _testObj.getChildren(roleOTElem);
        assertNotNull(children);
        assertTrue(children.length == 2);
        
        assertTrue(children[MAPPING_IDX] instanceof ICallinMapping);
        ICallinMapping mapping = (ICallinMapping)children[MAPPING_IDX];
        assertEquals("roleMethod <- baseMethod", mapping.getElementName());
        assertTrue(mapping.getCallinKind() == ICallinMapping.KIND_AFTER);
        
        assertTrue(children[METHOD_IDX] instanceof IMethod);
        IMethod method = (IMethod) children[METHOD_IDX];
        assertEquals(method.getElementName(), "roleMethod");
        assertEquals(method.getSignature(), "()V");                
    }    

//************************************************************
//    from here: testRoleParent
//************************************************************    
    public void testRoleParent_EmptyRole() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_1", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals(((IType)parent).getElementName(), "Team_1");
    }
    
    public void testRoleParent_RoleWithField() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_2", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals(((IType)parent).getElementName(), "Team_2");
    }    
    
    public void testRoleParent_RoleWithParameterlessMethod() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3a", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals(((IType)parent).getElementName(), "Team_3a");
    }

    public void testRoleParent_RoleWithMethodWithParameter() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3b", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals(((IType)parent).getElementName(), "Team_3b");
    }    
    
    public void testRoleParent_RoleWithMethodWithReturnValue() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3c", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals(((IType)parent).getElementName(), "Team_3c");
    }
    
    public void testRoleParent_RoleWithMethodWithThrowClause() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3d", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals(((IType)parent).getElementName(), "Team_3d");
    } 
    
    public void testRoleParent_RoleWithInnerclass() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_4a", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals(((IType)parent).getElementName(), "Team_4a");
    }

    public void testRoleParent_RoleWithInnerTeamClass() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_4b", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals(((IType)parent).getElementName(), "Team_4b");
    }
    
    public void testRoleParent_BoundRoleWithAbstractMethodAndCalloutMapping() throws JavaModelException
    {
      IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5a", "SampleRole");
      assertNotNull(roleOTElem);
	  Object parent = _testObj.getParent(roleOTElem);
	  
	  assertNotNull(parent);
	  assertTrue(parent instanceof IType);
	  assertEquals(((IType)parent).getElementName(), "Team_5a");
    }
    
    public void testRoleParent_BoundRoleWithConcreteMethodAndCalloutMapping() throws JavaModelException
    {
      IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5b", "SampleRole");
      assertNotNull(roleOTElem);
	  Object parent = _testObj.getParent(roleOTElem);
	  
	  assertNotNull(parent);
	  assertTrue(parent instanceof IType);
	  assertEquals(((IType)parent).getElementName(), "Team_5b");
    }
    
    public void testRoleParent_BoundRoleWithMethodAndBeforeCallinMapping() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5c", "SampleRole");
        assertNotNull(roleOTElem);
  	    Object parent = _testObj.getParent(roleOTElem);
	  
	    assertNotNull(parent);
	    assertTrue(parent instanceof IType);
	    assertEquals(((IType)parent).getElementName(), "Team_5c");
    }
    
    public void testRoleParent_BoundRoleWithMethodAndReplaceCallinMapping() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5d", "SampleRole");
        assertNotNull(roleOTElem);
  	    Object parent = _testObj.getParent(roleOTElem);
	  
	    assertNotNull(parent);
	    assertTrue(parent instanceof IType);
	    assertEquals(((IType)parent).getElementName(), "Team_5d");
    }
    
    public void testRoleParent_BoundRoleWithMethodAndAfterCallinMapping() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5e", "SampleRole");
        assertNotNull(roleOTElem);
  	    Object parent = _testObj.getParent(roleOTElem);
	  
	    assertNotNull(parent);
	    assertTrue(parent instanceof IType);
	    assertEquals(((IType)parent).getElementName(), "Team_5e");
    }        
    
}
