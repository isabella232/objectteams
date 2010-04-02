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
 * $Id: TypeHelperTest001.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author mkr
 * @version $Id: TypeHelperTest001.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class TypeHelperTest001 extends FileBasedModelTest
{

    private IType _t4;
    private IRoleType _t1r1;
    private IRoleType _t2r1;
    private IRoleType _t3r1;
    @SuppressWarnings("unused")
	private IRoleType _t4r1;

    private IMethod _m1;
    private IMethod _m2;
    private IMethod _m3;
    
    private IMethodMapping _mm1;
    private IMethodMapping _mm2;
    
    public TypeHelperTest001(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new Suite(TypeHelperTest001.class);
    }
    
    /**
     * Creates a team hierarchy with implicit role type inheritance.
     */
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("Hierarchy");
        super.setUpSuite();
        
        _t4 =
            getType(getTestProjectDir(),
                "analysis",
                "rolehierarchy2",
                "Team4");
        
        _t4r1 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy2",
                "Team4",
                "R1");


        _t3r1 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy2",
                "Team3",
                "R1");

        _t2r1 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy2",
                "Team2",
                "R1");

        _t1r1 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy2",
                "Team1",
                "R1");

        _m1 = _t1r1.getMethods()[0];
        _m2 = _t1r1.getMethods()[1];
        _m3 = _t1r1.getMethods()[2];

        _mm1 = _t2r1.getMethodMappings()[0];
        _mm2 = _t3r1.getMethodMappings()[0];
        
    }
        
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testExists_testObject()
    	throws JavaModelException
	{
	    assertNotNull(_t4);
	    assertTrue(_t4.exists());
	}

    public void testGetMethodMapping1() throws JavaModelException
    {
        IMethodMapping expected = _mm1; 
        IMethodMapping actual = TypeHelper.getMethodMapping(_m1, _t4);
        
        assertEquals(expected, actual);
    }
    
    public void testGetMethodMapping2() throws JavaModelException
    {
        IMethodMapping expected = _mm2; 
        IMethodMapping actual = TypeHelper.getMethodMapping(_m2, _t4);
        
        assertEquals(expected, actual);
    }
    
    public void testGetMethodMapping3() throws JavaModelException
    {
        IMethodMapping expected = null; 
        IMethodMapping actual = TypeHelper.getMethodMapping(_m3, _t4);
        
        assertEquals(expected, actual);
    }
   
}