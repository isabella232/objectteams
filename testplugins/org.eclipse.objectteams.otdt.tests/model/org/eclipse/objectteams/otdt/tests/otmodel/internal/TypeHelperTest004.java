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
 * $Id: TypeHelperTest004.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author mkr
 * @version $Id: TypeHelperTest004.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class TypeHelperTest004 extends FileBasedModelTest
{

    private IRoleType _t1r1;
    private IRoleType _t2r1;
    private IRoleType _t2r2;
    private IRoleType _t3r1;
    private IRoleType _t3r2;
    private IType     _c1;
    @SuppressWarnings("unused")
	private IMethod   _c1m1;
    private IMethod   _c1m2;
    private IMethod   _t1r1m3;
    private IMethod   _t2r1m1;
    private IMethod   _t2r2m3;

    public TypeHelperTest004(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new Suite(TypeHelperTest004.class);
    }
    
    /**
     * Creates a team hierarchy with implicit role type inheritance.
     */
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("Hierarchy");
        super.setUpSuite();
                
        _t1r1 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy3",
                "T1",
                "R1");
        
        _t2r1 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy3",
                "T2",
                "R1");

        _t2r2 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy3",
                "T2",
                "R2");
        
        _t3r1 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy3",
                "T3",
                "R1");
        
        _t3r2 =
            getRole(getTestProjectDir(),
                "analysis",
                "rolehierarchy3",
                "T3",
                "R2");

        _c1 =
            getType(getTestProjectDir(),
                "analysis",
                "rolehierarchy3",
                "C1");
        
        _c1m1 = _c1.getMethods()[0];
        _c1m2 = _c1.getMethods()[1];
        _t1r1m3 = _t1r1.getMethods()[0];
        _t2r1m1 = _t2r1.getMethods()[0];
        _t2r2m3 = _t2r2.getMethods()[0];
    }
        
    protected void setUp() throws Exception
    {
        super.setUp();
    }
    
    public void testExists_T3R2()
    	throws JavaModelException
	{
	    assertNotNull(_t3r2);
	    assertTrue(_t3r2.exists());
        assertTrue(_t3r2.isRole());
	}

    public void testExists_T3R1()
        throws JavaModelException
    {
        assertNotNull(_t3r1);
        assertTrue(_t3r1.exists());
        assertTrue(_t3r1.isRole());
    }

    public void testGetInheritedRoleMethods_MixedHierarchy1() throws JavaModelException
    {
        IMethod[] expected = new IMethod[] {_t2r1m1, _t1r1m3, _c1m2 };
        IMethod[] actual = TypeHelper.getRoleMethodsComplete(_t3r1);
        
        assertTrue( compareMethods(expected, actual) );
    }
    
    public void testGetInheritedRoleMethods_MixedHierarchy2() throws JavaModelException
    {
        IMethod[] expected = new IMethod[] {_t2r1m1, _t2r2m3, _c1m2};
        IMethod[] actual = TypeHelper.getRoleMethodsComplete(_t3r2);
        
        assertTrue( compareMethods(expected, actual) );
    }
    
        
}