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
package org.eclipse.objectteams.otdt.tests.otmodel.equals;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.core.OTType;
import org.eclipse.objectteams.otdt.internal.core.RoleType;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author brcan
 * @version $Id: Test1.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class Test1 extends FileBasedModelTest
{
    private static final int OTELEMENT_TYPE = IOTType.TEAM;
    
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
        setTestProjectDir(getTestSetting().getTestProject());
        getTestSetting().setTeamClass("EmptyTeam");
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
    		super.setUp();
        getTestSetting().setUp();
    }

    public void testEquals_emptyTeam() throws JavaModelException
    {
    	IOTType otType1 = createTeamClass("teampkg.EmptyTeam");        
        IOTType otType2 = getOTInstanceViaModelManager("teampkg", "EmptyTeam");

        assertFalse(otType1 == otType2);
        assertTrue(otType1.equals(otType2));
    }

    public void testEquals_teamWithEmptyRoleClass() throws JavaModelException
    {
    	IRoleType roleType1 = createRoleClass("teampkg.TeamWithRoleClass.RoleClass", "teampkg", "TeamWithRoleClass");        
        IRoleType roleType2 = getRoleClass("teampkg", "TeamWithRoleClass", "RoleClass");
        
        assertFalse(roleType1 == roleType2);
        assertTrue(roleType1.equals(roleType2));
    }
    
    public void testEquals_teamWithRoleClassContainingCalloutMapping()
    {
    }
    
    public void testEquals_teamWithRoleClassContainingCallinMapping()
    {
        
    }
    
    private IOTType createTeamClass(String qualName) throws JavaModelException
    {
        IType type1 = findType(qualName);
        
        return new OTType(
		            	OTELEMENT_TYPE, 
		            	type1, 
		            	null, // root element in OTModel
		            	ClassFileConstants.AccPublic | ClassFileConstants.AccTeam);
    }
    
    private IRoleType createRoleClass(String qualName,
            						  String pkgName,
            						  String typeName ) throws JavaModelException
    {
        IType type1 = findType(qualName);
        IType parent = getTeamClass(pkgName, typeName);
        
        return new RoleType(type1,
                			OTModelManager.getOTElement(parent),
                			ClassFileConstants.AccPublic | ExtraCompilerModifiers.AccRole,
                			null, null);// no base class given, unbound role
    }

    private IOTType getOTInstanceViaModelManager(String pkgName, String typeName) throws JavaModelException
    {
        IType type2 = getTeamClass(pkgName, typeName);
        
        assertNotNull(type2);
        assertTrue(type2.exists());
        return OTModelManager.getOTElement(type2);
    }

    private IType getTeamClass(String pkgName, String typeName) throws JavaModelException
    {
        ICompilationUnit unit = getCompilationUnit(
                getTestProjectDir(),
                "src",
                pkgName,
                typeName + ".java");
        
        IType type2 = unit.getType(typeName);
        return type2;
    }

    private IRoleType getRoleClass(String pkgName, String typeName, String roleName) throws JavaModelException
    {
        IType teamClass = getTeamClass(pkgName, typeName);
        return (IRoleType)OTModelManager.getOTElement(teamClass.getType(roleName));
    }
    
    private IType findType(String qualName) throws JavaModelException
    {
        IJavaProject project = getJavaProject(getTestProjectDir());
        IType type1 = project.findType(qualName);
        assertNotNull(type1);
        assertTrue(type1.exists());
        
        return type1;
    }
}
