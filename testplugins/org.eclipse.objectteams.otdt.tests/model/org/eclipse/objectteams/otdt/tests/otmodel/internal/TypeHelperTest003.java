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
 * $Id: TypeHelperTest003.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.internal;

import junit.framework.Test;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author mkr
 * @version $Id: TypeHelperTest003.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class TypeHelperTest003 extends FileBasedModelTest
{
    
    private IOTType _testObject;
    
    private IRoleType _t1r1;
    private IRoleType _t3r1;
    private IRoleType _t4r1;
    private IRoleType _t5r1;

    private IMethod   _t5r1m1;
    private IMethod   _t4r1m1;
    private IMethod   _t4r1m2;
    @SuppressWarnings("unused")
	private IMethod   _t3r1m2;
    private IMethod   _t3r1m3;
    private IMethod   _t1r1m2;
    
    public TypeHelperTest003(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new Suite(TypeHelperTest003.class);
    }
    
    /**
     * Creates a team hierarchy with implicit role type inheritance.
     */
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("Hierarchy");
        super.setUpSuite();
        
        IType teamIType =
            getType(getTestProjectDir(),
                "analysis",
                "rolehierarchy",
                "Team5");
        _testObject = OTModelManager.getOTElement(teamIType);
        
        _t5r1 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy",
                "Team5",
                "R1");

        
        
        _t4r1 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy",
                "Team4",
                "R1");


        _t3r1 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy",
                "Team3",
                "R1");


        _t1r1 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy",
                "Team1",
                "R1");
        
        _t5r1m1 = _t5r1.getMethods()[0];
        _t4r1m1 = _t4r1.getMethods()[0];
        _t4r1m2 = _t4r1.getMethods()[1];
        _t3r1m2 = _t3r1.getMethods()[0];
        _t3r1m3 = _t3r1.getMethods()[1];
        _t1r1m2 = _t1r1.getMethods()[1];
        
    }
        
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testExists_testObject()
    	throws JavaModelException
	{
	    assertNotNull(_testObject);
	    assertTrue(_testObject.exists());
        assertTrue(_testObject.isTeam());
	}

    public void testGetInheritedRoleMethods() throws JavaModelException
    {
        IMethod[] expected = new IMethod[] { _t4r1m1, _t4r1m2, _t3r1m3, _t1r1m2 };
        IMethod[] actual = TypeHelper.getInheritedRoleMethods(_testObject, "R1");
        
        assertTrue( compareMethods(expected, actual) );
    }
    
    public void testGetAllRoleMethods() throws JavaModelException
    {
        IMethod[] expected = new IMethod[] { _t5r1m1, _t4r1m2, _t3r1m3, _t1r1m2 };
        IMethod[] actual = TypeHelper.getAllRoleMethods(_testObject, "R1");
        
        assertTrue( compareMethods(expected, actual) );
    }
    
}