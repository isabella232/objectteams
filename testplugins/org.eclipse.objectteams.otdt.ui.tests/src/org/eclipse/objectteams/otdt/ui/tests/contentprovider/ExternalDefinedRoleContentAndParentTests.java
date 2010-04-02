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
 * $Id: ExternalDefinedRoleContentAndParentTests.java 23495 2010-02-05 23:15:16Z stephan $
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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.packageview.PackageExplorerAdaptor;
import org.eclipse.objectteams.otdt.ui.tests.FileBasedUITest;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerContentProvider;

/**
 * @author kaschja
 * @version $Id: ExternalDefinedRoleContentAndParentTests.java 23495 2010-02-05 23:15:16Z stephan $
 *
 * This test class contains test methods that test the methods
 * PackageExplorerContentProvider.getChildren and PackageExplorerContentProvider.getParent
 * with several external defined role classes as argument
 */
@SuppressWarnings("restriction")
public class ExternalDefinedRoleContentAndParentTests extends FileBasedUITest
{
	final int METHOD_IDX = 0;
	final int MAPPING_IDX = 1;

    private PackageExplorerContentProvider _testObj;

    
    public ExternalDefinedRoleContentAndParentTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(ExternalDefinedRoleContentAndParentTests.class);
        }
        junit.framework.TestSuite suite = new Suite(ExternalDefinedRoleContentAndParentTests.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("ExternalDefinedRole");
        
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _testObj = new PackageExplorerContentProvider(true);
        setShowTeamPackages(false);
    }

	private void setShowTeamPackages(boolean show) {
		PackageExplorerAdaptor adaptor = PackageExplorerAdaptor.getInstance();
        adaptor.setShowTeamPackages(show, _testObj);
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
    
    private IRoleType getRole(String srcFolderName, String pkgName, String teamName, String roleName) throws JavaModelException
    {
        IType teamJavaElem = getJavaType(getTestProjectDir(), srcFolderName, pkgName, teamName);
        IOTJavaElement teamOTElem = OTModelManager.getOTElement(teamJavaElem);
        assertNotNull(teamOTElem);
        
        IOTType teamType = (IOTType) teamOTElem;
        IType roleJavaElem = teamType.getRoleType(roleName);
//            IType roleJavaElem = teamJavaElem.getType(roleName); // IOTType.getType() does not return role files!
        if ((roleJavaElem != null) && (roleJavaElem.exists()))
        {
            IRoleType roleOTElem = (IRoleType) OTModelManager.getOTElement(roleJavaElem);
            return roleOTElem;
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
        assertEquals("roleAttr", field.getElementName());
        assertEquals("QString;", field.getTypeSignature());
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
        assertEquals("roleMethod", method.getElementName());
        assertEquals("()V", method.getSignature());        
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
        assertEquals("roleMethod", method.getElementName());
        assertEquals("(QString;)V", method.getSignature());        
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
        assertEquals("roleMethod", method.getElementName());
        assertEquals("()QString;", method.getSignature());        
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
        assertEquals("roleMethod", method.getElementName());
        assertEquals("()V", method.getSignature());        
        
        String[] exceptionTypes = method.getExceptionTypes();
        assertNotNull(exceptionTypes);
        assertTrue(exceptionTypes.length == 1);
        assertEquals("QException;", exceptionTypes[0]);
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
        assertEquals("AnInnerClass", innerType.getElementName());
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
        assertEquals("AnInnerTeamClass", innerType.getElementName());
    }
    
    
    public void testRoleContent_BoundRoleWithAbstractMethodAndCalloutMapping() throws JavaModelException
    {
      IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5a", "SampleRole");
      assertNotNull(roleOTElem);
      
      Object[] children = _testObj.getChildren(roleOTElem);
      assertNotNull(children);
      assertTrue(children.length == 2);
      
      assertTrue(children[MAPPING_IDX] instanceof ICalloutMapping);
      assertEquals("roleMethod -> baseMethod", ((ICalloutMapping)children[MAPPING_IDX]).getElementName());
      
      assertTrue(children[METHOD_IDX] instanceof IMethod);
      IMethod method = (IMethod) children[METHOD_IDX];
      assertEquals("roleMethod", method.getElementName());
      assertEquals("()V", method.getSignature());        
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
      assertEquals("roleMethod", method.getElementName());
      assertEquals("()V", method.getSignature());        
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
        assertEquals("roleMethod", method.getElementName());
        assertEquals("()V", method.getSignature());                
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
        assertEquals("roleMethod", method.getElementName());
        assertEquals("()V", method.getSignature());                
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
        assertEquals("roleMethod", method.getElementName());
        assertEquals("()V", method.getSignature());                
    }    

//************************************************************
//    from here: testRoleParent
//************************************************************    
    public void testRoleParent_EmptyRole() throws JavaModelException
    {
        IRoleType roleOTElem =  getRole("unbound", "teampkg", "Team_1", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals("Team_1", ((IType)parent).getElementName());
    }
    
    public void testRoleParent_EmptyRole_PhysicalView() throws JavaModelException
    {
        IRoleType roleOTElem =  getRole("unbound", "teampkg", "Team_1", "SampleRole");
        assertNotNull(roleOTElem);
        
        setShowTeamPackages(true);
        Object parent = _testObj.getParent(roleOTElem);
        assertNotNull(parent);
        assertTrue("direct parent should be ICompilationUnit", parent instanceof ICompilationUnit);
        parent = _testObj.getParent(parent);
        assertTrue("second level parent should be IPackageFragment", parent instanceof IPackageFragment);
        assertEquals("Team package should have team name", "teampkg.Team_1", ((IPackageFragment)parent).getElementName());
    }
    
    public void testRoleParent_RoleWithField() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_2", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals("Team_2", ((IType)parent).getElementName());
    }    
    
    public void testRoleParent_RoleWithParameterlessMethod() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3a", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals("Team_3a", ((IType)parent).getElementName());
    }

    public void testRoleParent_RoleWithMethodWithParameter() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3b", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals("Team_3b", ((IType)parent).getElementName());
    }    
    
    public void testRoleParent_RoleWithMethodWithReturnValue() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3c", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals("Team_3c", ((IType)parent).getElementName());
    }
    
    public void testRoleParent_RoleWithMethodWithThrowClause() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_3d", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals("Team_3d", ((IType)parent).getElementName());
    } 
    
    public void testRoleParent_RoleWithInnerclass() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_4a", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals("Team_4a", ((IType)parent).getElementName());
    }

    public void testRoleParent_RoleWithInnerTeamClass() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("unbound", "teampkg", "Team_4b", "SampleRole");
        assertNotNull(roleOTElem);
        
        Object parent = _testObj.getParent(roleOTElem);        
        assertNotNull(parent);
        assertTrue(parent instanceof IType);
        assertEquals("Team_4b", ((IType)parent).getElementName());
    }
    
    public void testRoleParent_BoundRoleWithAbstractMethodAndCalloutMapping() throws JavaModelException
    {
      IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5a", "SampleRole");
      assertNotNull(roleOTElem);
	  Object parent = _testObj.getParent(roleOTElem);
	  
	  assertNotNull(parent);
	  assertTrue(parent instanceof IType);
	  assertEquals("Team_5a", ((IType)parent).getElementName());
    }
    
    public void testRoleParent_BoundRoleWithConcreteMethodAndCalloutMapping() throws JavaModelException
    {
      IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5b", "SampleRole");
      assertNotNull(roleOTElem);
	  Object parent = _testObj.getParent(roleOTElem);
	  
	  assertNotNull(parent);
	  assertTrue(parent instanceof IType);
	  assertEquals("Team_5b", ((IType)parent).getElementName());
    }
    
    public void testRoleParent_BoundRoleWithMethodAndBeforeCallinMapping() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5c", "SampleRole");
        assertNotNull(roleOTElem);
  	    Object parent = _testObj.getParent(roleOTElem);
	  
	    assertNotNull(parent);
	    assertTrue(parent instanceof IType);
	    assertEquals("Team_5c", ((IType)parent).getElementName());
    }
    
    public void testRoleParent_BoundRoleWithMethodAndReplaceCallinMapping() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5d", "SampleRole");
        assertNotNull(roleOTElem);
  	    Object parent = _testObj.getParent(roleOTElem);
	  
	    assertNotNull(parent);
	    assertTrue(parent instanceof IType);
	    assertEquals("Team_5d", ((IType)parent).getElementName());
    }
    
    public void testRoleParent_BoundRoleWithMethodAndAfterCallinMapping() throws JavaModelException
    {
        IOTJavaElement roleOTElem =  getRole("boundtoordinary", "teampkg", "Team_5e", "SampleRole");
        assertNotNull(roleOTElem);
  	    Object parent = _testObj.getParent(roleOTElem);
	  
	    assertNotNull(parent);
	    assertTrue(parent instanceof IType);
	    assertEquals("Team_5e", ((IType)parent).getElementName());
    }        

}
