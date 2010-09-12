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
 * $Id: TeamContentTests.java 23495 2010-02-05 23:15:16Z stephan $
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
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.ui.tests.FileBasedUITest;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerContentProvider;

/**
 * @author kaschja
 * @version $Id: TeamContentTests.java 23495 2010-02-05 23:15:16Z stephan $
 *
 * This test class contains test methods that test the method
 * PackageExplorerContentProvider.getChildren
 * with several team classes as argument
 */
@SuppressWarnings("restriction")
public class TeamContentTests extends FileBasedUITest
{
    
    private PackageExplorerContentProvider _testObj;
    
    
    public TeamContentTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(TeamContentTests.class);
        }
        junit.framework.TestSuite suite = new Suite(TeamContentTests.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("TeamProject");
        
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
    
    private IOTJavaElement getTeam(String srcFolderName, String pkgName, String teamName) throws JavaModelException
    {
        IType teamJavaElem = getJavaType(getTestProjectDir(), srcFolderName, pkgName, teamName);
        
        if (teamJavaElem != null)
        {
            IOTJavaElement teamOTElem = OTModelManager.getOTElement(teamJavaElem);
            if (teamOTElem != null)
            {
                return teamOTElem;
            }
        }
        return null;
    }
    
    
    public void testTeamContent_EmptyTeam() throws JavaModelException
    {
        IOTJavaElement teamOTElem =  getTeam("src", "teampkg", "EmptyTeam");
        assertNotNull(teamOTElem);
        
        Object[] children = _testObj.getChildren(teamOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 0);
    }
    
    public void testTeamContent_Field() throws JavaModelException
    {
        IOTJavaElement teamOTElem =  getTeam("src", "teampkg", "TeamWithField");
        assertNotNull(teamOTElem);
        
        Object[] children = _testObj.getChildren(teamOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IField);
        
        IField field = (IField) children[0];
        assertEquals(field.getElementName(), "teamlevelField");
        assertEquals(field.getTypeSignature(), "QString;");
    }
 
    public void testTeamContent_ParameterlessMethod() throws JavaModelException
    {
        IOTJavaElement teamOTElem =  getTeam("src", "teampkg", "TeamWithParameterlessMethod");
        assertNotNull(teamOTElem);
        
        Object[] children = _testObj.getChildren(teamOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IMethod);
        
        IMethod method = (IMethod) children[0];
        assertEquals(method.getElementName(), "teamlevelMethod");
        assertEquals(method.getSignature(), "()V");
    }
    
    public void testTeamContent_MethodWithParameter() throws JavaModelException
    {
        IOTJavaElement teamOTElem =  getTeam("src", "teampkg", "TeamWithMethodWithParameter");
        assertNotNull(teamOTElem);
        
        Object[] children = _testObj.getChildren(teamOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IMethod);
        
        IMethod method = (IMethod) children[0];
        assertEquals(method.getElementName(), "teamlevelMethod");
        assertEquals(method.getSignature(), "(QString;)V");        
    }

    public void testTeamContent_MethodWithReturnValue() throws JavaModelException
    {
        IOTJavaElement teamOTElem =  getTeam("src", "teampkg", "TeamWithMethodWithReturnValue");
        assertNotNull(teamOTElem);
        
        Object[] children = _testObj.getChildren(teamOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IMethod);
        
        IMethod method = (IMethod) children[0];
        assertEquals(method.getElementName(), "teamlevelMethod");
        assertEquals(method.getSignature(), "()QString;");        
    }
    
    public void testTeamContent_MethodWithThrowClause() throws JavaModelException
    {
        IOTJavaElement teamOTElem =  getTeam("src", "teampkg", "TeamWithMethodWithThrowClause");
        assertNotNull(teamOTElem);
        
        Object[] children = _testObj.getChildren(teamOTElem);
        
        assertNotNull(children);
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IMethod);
        
        IMethod method = (IMethod) children[0];
        assertEquals(method.getElementName(), "teamlevelMethod");
        assertEquals(method.getSignature(), "()V");
        
        String[] exceptionTypes = method.getExceptionTypes();
        assertNotNull(exceptionTypes);
        assertTrue(exceptionTypes.length == 1);
        assertEquals(exceptionTypes[0], "QException;");
    }
    
    public void testTeamContent_InnerRoleClass() throws JavaModelException
    {
        IOTJavaElement teamOTElem =  getTeam("src", "teampkg", "TeamWithRoleClass");
        assertNotNull(teamOTElem);
        
        Object[] children = _testObj.getChildren(teamOTElem);
        
        assertNotNull(children);
        
//        Logger.println(children.length);
//        Logger.println(((IRoleType)children[0]).getElementName());
        
        assertTrue(children.length == 1);
        
        assertTrue(children[0] instanceof IRoleType);
        
        IRoleType innerType = (IRoleType) children[0];
        assertEquals(innerType.getElementName(), "RoleClass");
    }    

    public void testTeamContent_InnerTeamClass() throws JavaModelException
    {
        IOTJavaElement teamOTElem =  getTeam("src", "teampkg", "TeamWithInnerTeamClass");
        assertNotNull(teamOTElem);
        
        Object[] children = _testObj.getChildren(teamOTElem);
        
        assertNotNull(children);
        
//        Logger.println(children.length);
//        Logger.println(((IRoleType)children[0]).getElementName());
        
        assertTrue(children.length == 1);
        assertTrue(children[0] instanceof IRoleType);
        
        IRoleType innerType = (IRoleType) children[0];
        assertEquals(innerType.getElementName(), "InnerTeamClass");
        //TODO (kaschja) comment in next line by the time team cascading is supported 
        //assertTrue(innerType.isTeam());
    }
    
//TODO (kaschja) test containment of external defined role classes in a team depending on user defined visibility specification for external role packages  
//    public void testTeamContent_ExternalDefinedRoleClass() throws JavaModelException
//    {
//        PackageExplorerPart part = new PackageExplorerPart();
//        part.createPartControl(null);
//        
//        PackageExplorerContentProvider contentProviderToTest = part.getContentProvider();
//        HideExternalRolePackagesAction hideRolesAction = contentProviderToTest.getHideRolesAction();        
//        IOTJavaElement teamOTElem =  getTeam("src", "teampkg", "TeamWithExternalDefinedRoleClass");
//        Object[] children;
//        
//        assertNotNull(contentProviderToTest);
//        assertNotNull(hideRolesAction);
//        assertNotNull(teamOTElem);
//        
//        
//        if (!hideRolesAction.isRolePackageVisible())
//        {
//            //switch the state to "role package is visible" 
//            _hideExternalRolesAction.run();
//        }
//        assertTrue(hideRolesAction.isRolePackageVisible());
//        
//        children = contentProviderToTest.getChildren(teamOTElem);
//        
//        assertNotNull(children);
//        assertTrue(children.length == 1);
//        assertTrue(children[0] instanceof IRoleType);
//        
//        IRoleType innerType = (IRoleType) children[0];
//        assertEquals(innerType.getElementName(), "RoleClass");            
//        
//        
//        //switch the state to "role package is NOT visible" 
//        hideRolesAction.run();
//        assertTrue(!hideRolesAction.isRolePackageVisible());
//        
//        children = contentProviderToTest.getChildren(teamOTElem);
//        assertNotNull(children);
//        assertTrue(children.length == 0);        
//    }    
}
