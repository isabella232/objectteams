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
 * $Id: TeamCreationTests.java 23495 2010-02-05 23:15:16Z stephan $
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
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.TeamCreator;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.TypeInfo;
import org.eclipse.objectteams.otdt.ui.tests.FileBasedUITest;

/**
 * @author kaschja
 * @version $Id: TeamCreationTests.java 23495 2010-02-05 23:15:16Z stephan $
 */
public class TeamCreationTests extends FileBasedUITest
{
    
    private static final String SRC_FOLDER_NAME = "teams";
    
    private TeamCreator _teamCreator;
    
    
    public TeamCreationTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (false)
        {
            Suite suite = new Suite(TeamCreationTests.class.getName());
            suite.addTest(new TeamCreationTests("testCreation_TeamWithSuperInterfaceAndInheritedMethod"));
            return suite;
        }
        junit.framework.TestSuite suite = new Suite(TeamCreationTests.class);
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
        _teamCreator = new TeamCreator();
    }

//    /**
//     * only for debug purposes
//     * suppresses the deletion of the folders and files that have been created during the test run
//     */
//    public void tearDownSuite()
//    {
//        //don't call super method, so created directory will not be deleted
//    }
    
    
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
    

    public void testCreation_EmptyTeam() throws JavaModelException,
																    InterruptedException,
																    CoreException
	{
        IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
        IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
        assertNotNull(pkgFragRoot);
        assertNotNull(pkgFrag);
        
        TypeInfo typeInfo = new TypeInfo("EmptyTeam", pkgFragRoot, pkgFrag);
        typeInfo.setModifier(Flags.AccPublic+Flags.AccTeam);
        typeInfo.setSuperClassName("org.objectteams.Team");
        
        assertNotNull(_teamCreator);
        _teamCreator.setTypeInfo(typeInfo);
        _teamCreator.createType(new NullProgressMonitor());
        
        IType createdJavaType = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "EmptyTeam");
        assertNotNull(createdJavaType);
        
        IOTType createdOTType = OTModelManager.getOTElement(createdJavaType);
        assertNotNull(createdOTType);
        assertTrue(createdOTType.isTeam());
	}

    
    public void testCreation_NestedTeam() throws JavaModelException,
																    InterruptedException,
																    CoreException
	{
        IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
        IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
        assertNotNull(pkgFragRoot);
        assertNotNull(pkgFrag);
        
        TypeInfo typeInfo = new TypeInfo("NestedTeam", pkgFragRoot, pkgFrag);
        typeInfo.setModifier(Flags.AccPublic+Flags.AccTeam);
        typeInfo.setEnclosingTypeName("teampkg.TeamForNestedTeam");
		typeInfo.setInline(true);
		typeInfo.setCreateAbstractInheritedMethods(true);
        
        assertNotNull(_teamCreator);
        _teamCreator.setTypeInfo(typeInfo);
        _teamCreator.createType(new NullProgressMonitor());
        
        IType enclosingJavaType = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamForNestedTeam");
        IType createdJavaType = enclosingJavaType.getType("NestedTeam");
        assertNotNull(createdJavaType);
        assertTrue("created type should exist", createdJavaType.exists());
        
        IOTType createdOTType = OTModelManager.getOTElement(createdJavaType);
        assertNotNull(createdOTType);
        assertTrue(createdOTType.isTeam());
        
        IMethod[] methods = createdJavaType.getMethods();
        assertEquals("Should not have created methods", 0, methods.length);
	}

 
    public void testCreation_TeamWithConstructor() throws JavaModelException,
																    InterruptedException,
																    CoreException
	{
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);

		String TEAM_CLASS_NAME = "TeamWithConstructor";
        TypeInfo typeInfo = new TypeInfo(TEAM_CLASS_NAME, pkgFragRoot, pkgFrag);
        typeInfo.setModifier(Flags.AccPublic+Flags.AccTeam);
        typeInfo.setSuperClassName("org.objectteams.Team");
        typeInfo.setCreateConstructor(true);
		typeInfo.setCurrentType(pkgFrag.getCompilationUnit(TEAM_CLASS_NAME+".java").getType(TEAM_CLASS_NAME));

        assertNotNull(_teamCreator);
        _teamCreator.setTypeInfo(typeInfo);
		_teamCreator.createType(new NullProgressMonitor());
		
		IType createdJavaType = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", TEAM_CLASS_NAME);
		assertNotNull(createdJavaType);
		
		IOTType createdOTType = OTModelManager.getOTElement(createdJavaType);
		assertNotNull(createdOTType);
		assertTrue(createdOTType.isTeam());
		
		// constructor is a method by the name of the enclosing class:
		IMethod constructor = createdJavaType.getMethod(TEAM_CLASS_NAME, new String[0]);
		assertNotNull(constructor);
		assertTrue(constructor.exists());
	}    
    
    public void testCreation_TeamWithMainMethod() throws JavaModelException,
																		    InterruptedException,
																		    CoreException
	{
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);

        TypeInfo typeInfo = new TypeInfo("TeamWithMainMethod", pkgFragRoot, pkgFrag);
        typeInfo.setModifier(Flags.AccPublic+Flags.AccTeam);
        typeInfo.setSuperClassName("org.objectteams.Team");
        typeInfo.setCreateMainMethod(true);
		
        assertNotNull(_teamCreator);
        _teamCreator.setTypeInfo(typeInfo);
		_teamCreator.createType(new NullProgressMonitor());
		
		IType createdJavaType = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamWithMainMethod");
		assertNotNull(createdJavaType);
		
		IOTType createdOTType = OTModelManager.getOTElement(createdJavaType);
		assertNotNull(createdOTType);
		assertTrue(createdOTType.isTeam());
		
		String[] paraTypes = {"[QString;"};
		IMethod method = createdJavaType.getMethod("main", paraTypes);
		assertNotNull(method);
		assertTrue(method.exists());
}    
    
    public void testCreation_EmptyTeamWithEmptySuperTeam() throws JavaModelException,
																    InterruptedException,
																    CoreException
	{
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);

		String TEAM_CLASS_NAME = "EmptyTeamWithEmptySuperTeam";
        TypeInfo typeInfo = new TypeInfo(TEAM_CLASS_NAME, pkgFragRoot, pkgFrag);
        typeInfo.setModifier(Flags.AccPublic+Flags.AccTeam);
        typeInfo.setSuperClassName("teampkg.EmptyTeam");
		typeInfo.setCurrentType(pkgFrag.getCompilationUnit(TEAM_CLASS_NAME+".java").getType(TEAM_CLASS_NAME));
		
        assertNotNull(_teamCreator);
        _teamCreator.setTypeInfo(typeInfo);
		_teamCreator.createType(new NullProgressMonitor());
		
		IType subTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "EmptyTeamWithEmptySuperTeam");
		assertNotNull(subTeamJavaElem);
		
		IOTType subTeamOTElem = OTModelManager.getOTElement(subTeamJavaElem);
		assertNotNull(subTeamOTElem);
		assertTrue(subTeamOTElem.isTeam());	
		
		assertEquals(subTeamOTElem.getSuperclassName(), "EmptyTeam");
		assertEquals(subTeamOTElem.getSuperclassTypeSignature(), "QEmptyTeam;");
	}
    
    
    public void testCreation_EmptyTeamWithSuperInterface() throws JavaModelException,
																    InterruptedException,
																    CoreException
	{
		IType interfaceJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "ordinarypkg", "InterfaceWithOneMethod");
		assertNotNull(interfaceJavaElem);
		assertTrue(interfaceJavaElem.exists());
        
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);

		ArrayList<String> interfaceNames = new ArrayList<String>();
		interfaceNames.add("ordinary.InterfaceWithOneMethod");
		
        TypeInfo typeInfo = new TypeInfo("EmptyTeamWithSuperInterface", pkgFragRoot, pkgFrag);
        typeInfo.setModifier(Flags.AccPublic+Flags.AccTeam);
        typeInfo.setSuperInterfacesNames(interfaceNames);
		
        assertNotNull(_teamCreator);
        _teamCreator.setTypeInfo(typeInfo);
		_teamCreator.createType(new NullProgressMonitor());
		
		IType createdJavaType = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "EmptyTeamWithSuperInterface");
		assertNotNull(createdJavaType);
		
		IOTType createdOTType = OTModelManager.getOTElement(createdJavaType);
		assertNotNull(createdOTType);
		assertTrue(createdOTType.isTeam());
		
		String[] interfaces = createdOTType.getSuperInterfaceNames();
		assertNotNull(interfaces);
		assertTrue(interfaces.length == 1);
		assertEquals(interfaces[0], "InterfaceWithOneMethod");
		
		String[] interfaceSignatures = createdOTType.getSuperInterfaceTypeSignatures();
		assertNotNull(interfaceSignatures);
		assertTrue(interfaceSignatures.length == 1);
		assertEquals(interfaceSignatures[0], "QInterfaceWithOneMethod;");
	}
    
    
    public void testCreation_TeamWithSuperInterfaceAndInheritedMethod() throws JavaModelException,
																														    InterruptedException,
																														    CoreException
	{
		IType interfaceJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "ordinarypkg", "InterfaceWithOneMethod");
		assertNotNull(interfaceJavaElem);
		assertTrue(interfaceJavaElem.exists());
		
		/*creation of team*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);

		ArrayList<String> interfaceNames = new ArrayList<String>();
		interfaceNames.add("ordinarypkg.InterfaceWithOneMethod");
		
		String TEAM_CLASS_NAME = "TeamWithSuperInterfaceAndInheritedMethod";
        TypeInfo typeInfo = new TypeInfo(TEAM_CLASS_NAME, pkgFragRoot, pkgFrag);
        typeInfo.setModifier(Flags.AccPublic+Flags.AccTeam);
        typeInfo.setSuperInterfacesNames(interfaceNames);
        typeInfo.setCreateAbstractInheritedMethods(true);
		typeInfo.setCurrentType(pkgFrag.getCompilationUnit(TEAM_CLASS_NAME+".java").getType(TEAM_CLASS_NAME));
		
        assertNotNull(_teamCreator);
        _teamCreator.setTypeInfo(typeInfo);
		_teamCreator.createType(new NullProgressMonitor());
		
		IType createdJavaType = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamWithSuperInterfaceAndInheritedMethod");
		assertNotNull(createdJavaType);
		
		IOTType createdOTType = OTModelManager.getOTElement(createdJavaType);
		assertNotNull(createdOTType);
		assertTrue(createdOTType.isTeam());
		
		String[] interfaces = createdOTType.getSuperInterfaceNames();
		assertNotNull(interfaces);
		assertTrue(interfaces.length == 1);
		assertEquals(interfaces[0], "InterfaceWithOneMethod");
		
		String[] interfaceSignatures = createdOTType.getSuperInterfaceTypeSignatures();
		assertNotNull(interfaceSignatures);
		assertTrue(interfaceSignatures.length == 1);
		assertEquals(interfaceSignatures[0], "QInterfaceWithOneMethod;");
		
		IMethod method = createdJavaType.getMethod("methodToImplement", new String[0]);
		assertNotNull(method);
		assertTrue(method.exists());
	}
    
    
    public void testCreation_TeamWithSuperTeamAndInheritedMethod() throws JavaModelException,
																												    InterruptedException,
																												    CoreException
	{
		IType superTeamJavaElem = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "AbstractTeamWithAbstractMethod");
		assertNotNull(superTeamJavaElem);
		assertTrue(superTeamJavaElem.exists());
		
		/*creation of team*/
		
		IPackageFragmentRoot pkgFragRoot = getPackageFragmentRoot(getTestProjectDir(), SRC_FOLDER_NAME);
		IPackageFragment       pkgFrag       = getPackageFragment(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg");
		assertNotNull(pkgFragRoot);
		assertNotNull(pkgFrag);
		
		String TEAM_CLASS_NAME = "TeamWithSuperTeamAndInheritedMethod";
		TypeInfo typeInfo = new TypeInfo(TEAM_CLASS_NAME, pkgFragRoot, pkgFrag);
        typeInfo.setModifier(Flags.AccPublic+Flags.AccTeam);
		typeInfo.setSuperClassName("teampkg.AbstractTeamWithAbstractMethod");
		typeInfo.setCreateAbstractInheritedMethods(true);
		typeInfo.setCurrentType(pkgFrag.getCompilationUnit(TEAM_CLASS_NAME+".java").getType(TEAM_CLASS_NAME));

		assertNotNull(_teamCreator);
		_teamCreator.setTypeInfo(typeInfo);
		_teamCreator.createType(new NullProgressMonitor());
		
		IType createdJavaType = getJavaType(getTestProjectDir(), SRC_FOLDER_NAME, "teampkg", "TeamWithSuperTeamAndInheritedMethod");
		assertNotNull(createdJavaType);
		
		IOTType createdOTType = OTModelManager.getOTElement(createdJavaType);
		assertNotNull(createdOTType);
		assertTrue(createdOTType.isTeam());
		
		assertEquals(createdJavaType.getSuperclassName(), "AbstractTeamWithAbstractMethod");
		assertEquals(createdJavaType.getSuperclassTypeSignature(), "QAbstractTeamWithAbstractMethod;");
		
		IMethod method = createdJavaType.getMethod("methodToImplement", new String[0]);
		assertNotNull(method);
		assertTrue(method.exists());
	}            
}
