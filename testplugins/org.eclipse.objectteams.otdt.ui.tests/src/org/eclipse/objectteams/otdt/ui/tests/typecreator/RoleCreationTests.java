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
 * $Id: RoleCreationTests.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.typecreator;

import java.util.ArrayList;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.RoleCreator;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.RoleTypeInfo;
import org.eclipse.objectteams.otdt.ui.tests.FileBasedUITest;

/**
 * @author kaschja
 * @version $Id: RoleCreationTests.java 23495 2010-02-05 23:15:16Z stephan $
 */
public class RoleCreationTests extends FileBasedUITest
{
    /*this test class contains tests for internal defined roles*/
    private static boolean DOINLINE = true;
    private static String SRC_FOLDER_NAME = "internaldefinedrole";
    
    private RoleCreator _roleCreator;

    
    public RoleCreationTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (false)
        {
            Suite suite = new Suite(RoleCreationTests.class.getName());
            suite.addTest(new RoleCreationTests("testRoleCreation_WithExplicitSuperClassAndInheritedMethod_FromSameTeam"));
            return suite;
        }
        Suite suite = new Suite(RoleCreationTests.class);
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("CreationTestProject");
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        _roleCreator = new RoleCreator();
    }
    
//	  /**
//	  * only for debug purposes
//	  * if created directory is not deleted after a test run
//	  * the next test run will fail because the types to be created already exist
//	  */
//	 public void tearDownSuite()
//	 {
//	     //don't call super method, so created directory will not be deleted
//	 }    
    
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

    /**
     * This method tests the creation of a simple role class (no super role, no super interface, no base class)
     * initially the role's hosting team class is empty
     */
    public void testRoleCreation_SimpleRole() throws InterruptedException,
	                                                 CoreException
	{
        /*creation of role*/

    	IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag   = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		RoleTypeInfo typeInfo = new RoleTypeInfo("SimpleRole", pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg.TeamForSimpleRole");
		typeInfo.setInline(false);
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/

        IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForSimpleRole");
        assertNotNull(enclosingTeamJavaElem);
		
        IType roleJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg.TeamForSimpleRole", "SimpleRole");
        assertNotNull(roleJavaElem);
        assertTrue(roleJavaElem.exists());
        
        IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
        assertNotNull(roleOTElem);
        assertTrue(roleOTElem.isRole());
	}
    
    /**
     * This method tests the creation of a simple role class (no super role, no super interface, no base class)
     * initially the role's hosting team already contains one role class
     */
    public void testRoleCreation_SimpleRole_InTeamPreviouslyWithOneRole() throws InterruptedException,
	                                                                             CoreException
	{
        /*creation of role*/

        IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		RoleTypeInfo typeInfo = new RoleTypeInfo("SimpleRole", pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg.TeamForSimpleRole_PreviouslyWithOneRole");
		typeInfo.setInline(DOINLINE);
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/

        IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForSimpleRole_PreviouslyWithOneRole");
        assertNotNull(enclosingTeamJavaElem);
		
        IType roleJavaElem = enclosingTeamJavaElem.getType("SimpleRole");
        assertNotNull(roleJavaElem);
        assertTrue(roleJavaElem.exists());
        
        IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
        assertNotNull(roleOTElem);
        assertTrue(roleOTElem.isRole());
	}
    
    /**
     * This method tests the creation of a simple role class (no super role, no super interface, no base class)
     * initially the role's hosting team already contains several role classes
     */
    public void testRoleCreation_SimpleRole_InTeamPreviouslyWithSeveralRoles() throws InterruptedException,
	                                                                                  CoreException
	{
        /*creation of role*/

        IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		RoleTypeInfo typeInfo = new RoleTypeInfo("SimpleRole", pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg.TeamForSimpleRole_PreviouslyWithTwoRoles");
		typeInfo.setInline(DOINLINE);
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/

        IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForSimpleRole_PreviouslyWithTwoRoles");
        assertNotNull(enclosingTeamJavaElem);
		
        IType roleJavaElem = enclosingTeamJavaElem.getType("SimpleRole");
        assertNotNull(roleJavaElem);
        assertTrue(roleJavaElem.exists());
        
        IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
        assertNotNull(roleOTElem);
        assertTrue(roleOTElem.isRole());
	}    
    
    /**
     * This method tests the creation of a role class that has one super interface
     * initially the role's hosting team class is empty
     */
    public void testRoleCreation_WithOneSuperInterface() throws InterruptedException,
																CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		ArrayList<String> interfaceNameList = new ArrayList<String>();
		interfaceNameList.add("ordinarypkg.InterfaceWithOneMethod");		
		
		RoleTypeInfo typeInfo = new RoleTypeInfo("RoleWithOneSuperInterface", pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg.TeamForRoleWithOneSuperInterface");
		typeInfo.setInline(DOINLINE);
		typeInfo.setSuperInterfacesNames(interfaceNameList);
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForRoleWithOneSuperInterface");
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType("RoleWithOneSuperInterface");
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());
		
		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		String[] interfaceNames = roleOTElem.getSuperInterfaceNames();
		assertNotNull(interfaceNames);
		assertTrue(interfaceNames.length == 1);
		assertEquals("InterfaceWithOneMethod", interfaceNames[0]);
		
		String[] interfacesSignatures = roleOTElem.getSuperInterfaceTypeSignatures();
		assertNotNull(interfacesSignatures);
		assertTrue(interfacesSignatures.length == 1);
		assertEquals("QInterfaceWithOneMethod;", interfacesSignatures[0]);
	}    

    /**
     * This method tests the creation of a role class that has several super interfaces
     * initially the role's hosting team class is empty
     */
    public void testRoleCreation_WithSeveralSuperInterfaces() throws InterruptedException,
	                                                                 CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		ArrayList<String> interfaceNameList = new ArrayList<String>();
		interfaceNameList.add("ordinarypkg.InterfaceWithOneMethod");
		interfaceNameList.add("ordinarypkg.InterfaceWithTwoMethods");
		
		RoleTypeInfo typeInfo = new RoleTypeInfo("RoleWithSeveralSuperInterfaces", pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg.TeamForRoleWithSeveralSuperInterfaces");
		typeInfo.setInline(DOINLINE);
		typeInfo.setSuperInterfacesNames(interfaceNameList);
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForRoleWithSeveralSuperInterfaces");
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType("RoleWithSeveralSuperInterfaces");
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());
		
		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		String[] interfaceNames = roleOTElem.getSuperInterfaceNames();
		assertNotNull(interfaceNames);
		assertTrue(interfaceNames.length > 0);
		assertTrue(interfaceNames.length == 2);
		assertEquals("InterfaceWithOneMethod", interfaceNames[0]);
		assertEquals("InterfaceWithTwoMethods", interfaceNames[1]);
		
		String[] interfacesSignatures = roleOTElem.getSuperInterfaceTypeSignatures();
		assertNotNull(interfacesSignatures);
		assertTrue(interfacesSignatures.length > 0);
		assertTrue(interfacesSignatures.length == 2);
		assertEquals("QInterfaceWithOneMethod;", interfacesSignatures[0]);
		assertEquals("QInterfaceWithTwoMethods;", interfacesSignatures[1]);
	}        
    
    /**
     * This method tests the creation of a role class that has one super interface
     * the role class should contain the abstract method that it inherits from its super interface
     * initially the role's hosting team class is empty
     */
    public void testRoleCreation_WithOneSuperInterfaceAndInheritedMethod() throws InterruptedException,
	                                                                              CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		ArrayList<String> interfaceNameList = new ArrayList<String>();
		interfaceNameList.add("ordinarypkg.InterfaceWithOneMethod");
		
		RoleTypeInfo typeInfo = new RoleTypeInfo("RoleWithSuperInterfaceAndInheritedMethod", pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg.TeamForRoleWithOneSuperInterfaceAndInheritedMethod");
		typeInfo.setInline(DOINLINE);
		typeInfo.setSuperInterfacesNames(interfaceNameList);
		typeInfo.setCreateAbstractInheritedMethods(true);
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForRoleWithOneSuperInterfaceAndInheritedMethod");
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType("RoleWithSuperInterfaceAndInheritedMethod");
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());
		
		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		String[] interfaceNames = roleOTElem.getSuperInterfaceNames();
		assertNotNull(interfaceNames);
		assertTrue(interfaceNames.length == 1);
		assertEquals("InterfaceWithOneMethod", interfaceNames[0]);
		
		String[] interfacesSignatures = roleOTElem.getSuperInterfaceTypeSignatures();
		assertNotNull(interfacesSignatures);
		assertTrue(interfacesSignatures.length == 1);
		assertEquals("QInterfaceWithOneMethod;", interfacesSignatures[0]);
		
		IMethod[] methods = roleJavaElem.getMethods();
		assertNotNull(methods);
		assertTrue(methods.length > 0);
		assertTrue(methods.length == 1);
		assertEquals("methodToImplement", methods[0].getElementName());
	}
 
    /**
     * This method tests the creation of a role class that has several super interfaces
     * the role class should contain all abstract methods that it inherits from its several super interfaces
     * initially the role's hosting team class is empty
     */    
    public void testRoleCreation_WithSeveralSuperInterfacesAndInheritedMethods() throws InterruptedException,
	                                                                                    CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		ArrayList<String> interfaceNameList = new ArrayList<String>();
		interfaceNameList.add("ordinarypkg.InterfaceWithOneMethod");
		interfaceNameList.add("ordinarypkg.InterfaceWithTwoMethods");
		
		RoleTypeInfo typeInfo = new RoleTypeInfo("RoleWithSeveralSuperInterfacesAndInheritedMethods", pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg.TeamForRoleWithSeveralSuperInterfacesAndInheritedMethods");
		typeInfo.setInline(DOINLINE);
		typeInfo.setSuperInterfacesNames(interfaceNameList);
		typeInfo.setCreateAbstractInheritedMethods(true);
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForRoleWithSeveralSuperInterfacesAndInheritedMethods");
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType("RoleWithSeveralSuperInterfacesAndInheritedMethods");
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());
		
		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		String[] interfaceNames = roleOTElem.getSuperInterfaceNames();
		assertNotNull(interfaceNames);
		assertTrue(interfaceNames.length > 0);
		assertTrue(interfaceNames.length == 2);
		assertEquals("InterfaceWithOneMethod", interfaceNames[0]);
		assertEquals("InterfaceWithTwoMethods", interfaceNames[1]);
		
		String[] interfacesSignatures = roleOTElem.getSuperInterfaceTypeSignatures();
		assertNotNull(interfacesSignatures);
		assertTrue(interfacesSignatures.length > 0);
		assertTrue(interfacesSignatures.length == 2);
		assertEquals("QInterfaceWithOneMethod;", interfacesSignatures[0]);
		assertEquals("QInterfaceWithTwoMethods;", interfacesSignatures[1]);
		
		IMethod[] methods = roleJavaElem.getMethods();
		assertNotNull(methods);
		assertTrue(methods.length > 0);
		assertTrue(methods.length == 3);
		
		IMethod method = roleJavaElem.getMethod("methodToImplement", new String[0]);
		assertNotNull(method);
		assertTrue(method.exists());
		
		method = roleJavaElem.getMethod("methodToImplementA", new String[0]);
		assertNotNull(method);
		assertTrue(method.exists());
		
		method = roleJavaElem.getMethod("methodToImplementB", new String[0]);
		assertNotNull(method);
		assertTrue(method.exists());
	}    
    
    /**
     * This method tests the creation of a role class that is bound to a base class
     * the base class is an ordinary class
     * initially the role's hosting team class is empty
     */
    public void testRoleCreation_WithOrdinaryBase() throws InterruptedException,
	                                                       CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		RoleTypeInfo typeInfo = new RoleTypeInfo("RoleWithOrdinaryBase", pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg.TeamForRoleWithOrdinaryBase");
		typeInfo.setInline(DOINLINE);
		typeInfo.setBaseTypeName("ordinarypkg.EmptyClass");
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForRoleWithOrdinaryBase");
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType("RoleWithOrdinaryBase");
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());
		
		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		assertTrue(roleOTElem instanceof IRoleType);
		IRoleType roleOTRoleElem = (IRoleType) roleOTElem;
		IType baseJavaElem = roleOTRoleElem.getBaseClass();
		
		assertNotNull(baseJavaElem);
		assertEquals("ordinarypkg.EmptyClass", baseJavaElem.getFullyQualifiedName());
	}
    
    /**
     * This method tests the creation of a role class that is bound to a base class
     * the base class is a team class
     * initially the role's hosting team class is empty
     */    
    public void testRoleCreation_WithTeamBase() throws InterruptedException,
	                                                   CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		RoleTypeInfo typeInfo = new RoleTypeInfo("RoleWithTeamBase", pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg.TeamForRoleWithTeamBase");
		typeInfo.setInline(DOINLINE);
		typeInfo.setBaseTypeName("teampkg.EmptyBaseTeam");
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForRoleWithTeamBase");
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType("RoleWithTeamBase");
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());
		
		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		assertTrue(roleOTElem instanceof IRoleType);
		IRoleType roleOTRoleElem = (IRoleType) roleOTElem;
		IType baseJavaElem = roleOTRoleElem.getBaseClass();
		
		assertNotNull(baseJavaElem);
		assertEquals("teampkg.EmptyBaseTeam", baseJavaElem.getFullyQualifiedName());
		
		IOTType baseOTElem = OTModelManager.getOTElement(baseJavaElem);
		assertNotNull(baseOTElem);
		assertTrue(baseOTElem.isTeam());
	}  
    
    
    /**
     * This method tests the creation of a role class that has an explicit superclass.
     * The explicit superclass resides in the same team class as the newly created role class.
     */
    public void testRoleCreation_WithExplicitSuperClass_FromSameTeam() throws InterruptedException,
	                                                                          CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		final String TEAM_CLASS_NAME = "TeamForRoleWithExplicitSuperclass_PreviouslyWithOneRole";
		final String ROLE_CLASS_NAME = "RoleWithExplicitSuperClass";

		RoleTypeInfo typeInfo = new RoleTypeInfo(ROLE_CLASS_NAME, pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg."+TEAM_CLASS_NAME);
		typeInfo.setInline(DOINLINE);
		typeInfo.setSuperClassName("teampkg.TeamForRoleWithExplicitSuperclass_PreviouslyWithOneRole.SuperRole");
		typeInfo.setCurrentType(pkgFrag.getCompilationUnit(TEAM_CLASS_NAME+".java").getType(ROLE_CLASS_NAME));
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", TEAM_CLASS_NAME);
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType(ROLE_CLASS_NAME);
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());

		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		assertEquals("SuperRole", roleJavaElem.getSuperclassName());
		assertEquals("QSuperRole;", roleJavaElem.getSuperclassTypeSignature());
	}
    
    
    /**
     * This method tests the creation of a role class that has an explicit superclass.
     * The explicit superclass does not reside in the same team class as the newly created role class,
     * but in a super team of that team. 
     */
    public void testRoleCreation_WithExplicitSuperClass_FromSuperTeam() throws InterruptedException,
	                                                                           CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment     pkgFrag     = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		final String TEAM_CLASS_NAME = "TeamForRoleWithExplicitSuperclass_PreviouslyEmpty";
		final String ROLE_CLASS_NAME = "RoleWithExplicitSuperClass";

		RoleTypeInfo typeInfo = new RoleTypeInfo(ROLE_CLASS_NAME, pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg."+TEAM_CLASS_NAME);
		typeInfo.setInline(DOINLINE);
//TODO (kaschja) ??? must "teampkg.TeamWithRoleWithAbstractMethod.RoleWithAbstractMethod" be set as super role class ??? (because it is the team class where the super role comes from)	
		typeInfo.setSuperClassName("teampkg.TeamForRoleWithExplicitSuperclass_PreviouslyEmpty.RoleWithAbstractMethod");
		typeInfo.setCurrentType(pkgFrag.getCompilationUnit(TEAM_CLASS_NAME+".java").getType(ROLE_CLASS_NAME));
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", TEAM_CLASS_NAME);
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType(ROLE_CLASS_NAME);
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());
		
		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		assertEquals("RoleWithAbstractMethod", roleJavaElem.getSuperclassName());
		assertEquals("QRoleWithAbstractMethod;", roleJavaElem.getSuperclassTypeSignature());
	}
    
    
    /**
     * This method tests the creation of a role class that has an explicit superclass.
     * the role class should contain the abstract method that it inherits from its superclass
     * The explicit superclass resides in the same team class as the newly created role class.
     */
    public void testRoleCreation_WithExplicitSuperClassAndInheritedMethod_FromSameTeam() throws InterruptedException,
	                                                                                            CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		final String TEAM_CLASS_NAME = "TeamForRoleWithExplicitSuperclassAndInheritedMethod_PreviouslyWithOneRole";
		final String ROLE_CLASS_NAME = "RoleWithExplicitSuperClassAndInheritedMethod";

		RoleTypeInfo typeInfo = new RoleTypeInfo(ROLE_CLASS_NAME, pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg."+TEAM_CLASS_NAME);
		typeInfo.setInline(DOINLINE);
		typeInfo.setSuperClassName("teampkg.TeamForRoleWithExplicitSuperclassAndInheritedMethod_PreviouslyWithOneRole.RoleWithAbstractMethod");
		typeInfo.setCreateAbstractInheritedMethods(true);
		typeInfo.setCurrentType(pkgFrag.getCompilationUnit(TEAM_CLASS_NAME+".java").getType(ROLE_CLASS_NAME));
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", TEAM_CLASS_NAME);
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType(ROLE_CLASS_NAME);
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());
		
		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		assertEquals("RoleWithAbstractMethod", roleJavaElem.getSuperclassName());
		assertEquals("QRoleWithAbstractMethod;", roleJavaElem.getSuperclassTypeSignature());
		
		IMethod[] methods = roleJavaElem.getMethods();
		assertNotNull(methods);
		assertTrue(methods.length > 0);
		assertTrue(methods.length == 1);
		assertEquals("methodToImplement1", methods[0].getElementName());
	}
    
    
    /**
     * This method tests the creation of a role class that has an explicit superclass.
     * The role class should contain the abstract method that it inherits from its superclass.
     * The explicit superclass does not reside in the same team class as the newly created role class
     * but in the super team of that team.
     */
    public void testRoleCreation_WithExplicitSuperClassAndInheritedMethod_FromSuperTeam() throws InterruptedException,
	                                                                                             CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment     pkgFrag     = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		final String TEAM_CLASS_NAME = "TeamForRoleWithExplicitSuperclassAndInheritedMethod_PreviouslyEmpty";
		final String ROLE_CLASS_NAME = "RoleWithExplicitSuperClassAndInheritedMethod";
		
		RoleTypeInfo typeInfo = new RoleTypeInfo(ROLE_CLASS_NAME, pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg."+TEAM_CLASS_NAME);
		typeInfo.setInline(DOINLINE);
//TODO (kaschja) ??? must "teampkg.TeamWithRoleWithAbstractMethod.RoleWithAbstractMethod" be set as super role class ??? (because it is the team class where the super role comes from)
//		typeInfo.setSuperClassName("teampkg.TeamForRoleWithExplicitSuperclassAndInheritedMethod_PreviouslyEmpty.RoleWithAbstractMethod");
		typeInfo.setSuperClassName("teampkg.TeamWithRoleWithAbstractMethod.RoleWithAbstractMethod");		
		typeInfo.setCurrentType(pkgFrag.getCompilationUnit(TEAM_CLASS_NAME+".java").getType(ROLE_CLASS_NAME));
		
		
		typeInfo.setCreateAbstractInheritedMethods(true);
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForRoleWithExplicitSuperclassAndInheritedMethod_PreviouslyEmpty");
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType("RoleWithExplicitSuperClassAndInheritedMethod");
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());
		
		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		assertEquals("RoleWithAbstractMethod", roleJavaElem.getSuperclassName());
		assertEquals("QRoleWithAbstractMethod;", roleJavaElem.getSuperclassTypeSignature());
		
		IMethod[] methods = roleJavaElem.getMethods();
		assertNotNull(methods);
		assertTrue(methods.length > 0);
		assertTrue(methods.length == 1);
		assertEquals("methodToImplement1", methods[0].getElementName());
	}
    
    
    /**
     * This method tests the creation of a role class that has an implicit superclass.
     * The role class should contain the abstract method that it inherits from its superclass.
     */
    public void testRoleCreation_WithImplicitSuperClassAndInheritedMethod() throws InterruptedException,
	                                                                               CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment     pkgFrag     = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		final String TEAM_CLASS_NAME = "TeamForRoleWithImplicitSuperClassAndInheritedMethod";
		final String ROLE_CLASS_NAME = "RoleWithAbstractMethod";
		
		RoleTypeInfo typeInfo = new RoleTypeInfo(ROLE_CLASS_NAME, pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg."+TEAM_CLASS_NAME);
		typeInfo.setInline(DOINLINE);
		typeInfo.setCreateAbstractInheritedMethods(true);
		typeInfo.setCurrentType(pkgFrag.getCompilationUnit(TEAM_CLASS_NAME+".java").getType(ROLE_CLASS_NAME));
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForRoleWithImplicitSuperClassAndInheritedMethod");
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType("RoleWithAbstractMethod");
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());
		
		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		IMethod[] methods = roleJavaElem.getMethods();
		assertNotNull(methods);
		assertTrue(methods.length > 0);
		assertTrue(methods.length == 1);
		assertEquals("methodToImplement1", methods[0].getElementName());		
	}
    
    
    /**
     * This method tests the creation of a role class that has
     * an implicit superclass and an explicit superclass and several super interfaces.
     * The role class should contain all abstract methods that it inherits from 
     * its several super classes and interfaces
     */
    public void testRoleCreation_WithSeveralSuperTypesAndInheritedMethods() throws InterruptedException,
	                                                                               CoreException
	{
		/*creation of role*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment     pkgFrag     = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		ArrayList<String> interfaceNameList = new ArrayList<String>();
		interfaceNameList.add("ordinarypkg.InterfaceWithOneMethod");
		interfaceNameList.add("ordinarypkg.InterfaceWithTwoMethods");
		
		//the role to be created should be named "RoleWithAbstractMethod" although it contains several abstract methods
		//in order to establish the implicit inheritence relationship with TeamWithRoleWithAbstractMethod.RoleWithAbstractMethod
		final String ROLE_CLASS_NAME = "RoleWithAbstractMethod"; 
		final String TEAM_CLASS_NAME = "TeamForRoleWithSeveralSuperTypesAndInheritedMethods";
		
		RoleTypeInfo typeInfo = new RoleTypeInfo(ROLE_CLASS_NAME, pkgFragRoot, pkgFrag);
		typeInfo.setEnclosingTypeName("teampkg."+TEAM_CLASS_NAME);
		typeInfo.setInline(DOINLINE);
		typeInfo.setSuperClassName("teampkg.TeamForRoleWithSeveralSuperTypesAndInheritedMethods.AbstractSuperRole");
		typeInfo.setSuperInterfacesNames(interfaceNameList);
		typeInfo.setCreateAbstractInheritedMethods(true);
		typeInfo.setCurrentType(pkgFrag.getCompilationUnit(TEAM_CLASS_NAME+".java").getType(ROLE_CLASS_NAME));
		
		assertNotNull(_roleCreator);
		_roleCreator.setTypeInfo(typeInfo);
		_roleCreator.createType(new NullProgressMonitor());
		
		/*test of role existence and properties*/
		
		IType enclosingTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForRoleWithSeveralSuperTypesAndInheritedMethods");
		assertNotNull(enclosingTeamJavaElem);
		
		IType roleJavaElem = enclosingTeamJavaElem.getType("RoleWithAbstractMethod");
		assertNotNull(roleJavaElem);
		assertTrue(roleJavaElem.exists());
		
		IOTType roleOTElem = OTModelManager.getOTElement(roleJavaElem);
		assertNotNull(roleOTElem);
		assertTrue(roleOTElem.isRole());
		
		assertEquals("AbstractSuperRole", roleJavaElem.getSuperclassName());
		assertEquals("QAbstractSuperRole;", roleJavaElem.getSuperclassTypeSignature());
		
		String[] interfaceNames = roleJavaElem.getSuperInterfaceNames();
		assertNotNull(interfaceNames);
		assertTrue(interfaceNames.length > 0);
		assertTrue(interfaceNames.length == 2);
		assertEquals("InterfaceWithOneMethod", interfaceNames[0]);
		assertEquals("InterfaceWithTwoMethods", interfaceNames[1]);
		
		String[] interfaceSignatures = roleJavaElem.getSuperInterfaceTypeSignatures();
		assertNotNull(interfaceSignatures);
		assertTrue(interfaceSignatures.length > 0);
		assertTrue(interfaceSignatures.length == 2);
		assertEquals("QInterfaceWithOneMethod;", interfaceSignatures[0]);
		assertEquals("QInterfaceWithTwoMethods;", interfaceSignatures[1]);
		
		IMethod[] methods = roleJavaElem.getMethods();
		assertNotNull(methods);
		assertTrue(methods.length > 0);
		assertTrue(methods.length == 5);
		
		//method inherited from "InterfaceWithOneMethod"
		IMethod method = roleJavaElem.getMethod("methodToImplement", new String[0]);
		assertNotNull(method);
		assertTrue(method.exists());
		
		//methods inherited from "InterfaceWithTwoMethods"
		method = roleJavaElem.getMethod("methodToImplementA", new String[0]);
		assertNotNull(method);
		assertTrue(method.exists());
		method = roleJavaElem.getMethod("methodToImplementB", new String[0]);
		assertNotNull(method);
		assertTrue(method.exists());
		
		//method inherited from explicit superclass "AbstractSuperRole"
		method = roleJavaElem.getMethod("methodToImplementX", new String[0]);
		assertNotNull(method);
		assertTrue(method.exists());
		
		//method inherited from implicit superclass "TeamWithRoleWithAbstractMethod.RoleWithAbstractMethod" 
		method = roleJavaElem.getMethod("methodToImplement1", new String[0]);
		assertNotNull(method);
		assertTrue(method.exists());
	}
}
