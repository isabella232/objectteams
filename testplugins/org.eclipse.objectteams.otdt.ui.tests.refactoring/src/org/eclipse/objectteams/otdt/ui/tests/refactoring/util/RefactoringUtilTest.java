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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring.util;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.FileBasedRefactoringTest;

/**
 * @author svacina
 * @version $Id: RefactoringUtilTest.java 6446 2005-08-02 10:49:59Z svacina $
 */
public class RefactoringUtilTest extends FileBasedRefactoringTest
{

	private IType _a;
	private IType _aa;
	private IType _b;
	private IType _c;
	private IType _itest1;
	private IType _itest2;
	
	private IRoleType _team1role1;
	private IRoleType _team2role1;
	
	private IRoleType _team3role2;
	private IRoleType _team4role2;
	
	private IMethod _team2role1m1;
	private IMethod _team2role1m2;
	private IMethod _itest2m1;
	
	private IMethod _cm1;
	private IMethod _cm2;
	private IMethod _team4role2m1;
	private IMethod _team4role2m2;
	
	
    public RefactoringUtilTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new Suite(RefactoringUtilTest.class);
    }
    
    /**
     * Creates a team hierarchy with implicit role type inheritance.
     */
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("RefactoringUtil");
        super.setUpSuite();
                
        _a = getType(getTestProjectDir(),
        		"src",
				"rolehierarchy1",
				"A");
        
        _aa = getType(getTestProjectDir(),
        		"src",
				"rolehierarchy1",
				"AA");
        
        _b = getType(getTestProjectDir(),
        		"src",
				"rolehierarchy1",
				"B");
        
        _c = getType(getTestProjectDir(),
        		"src",
				"rolehierarchy1",
				"C");
        
        _itest1 = getType(getTestProjectDir(),
        		"src",
				"rolehierarchy1",
				"ITest1");
        
        _itest2 = getType(getTestProjectDir(),
        		"src",
				"rolehierarchy1",
				"ITest2");
        
        _team1role1 =
            getRole(getTestProjectDir(),
                "src",
                "rolehierarchy1",
                "Team1",
                "Role1");
        
        _team2role1 =
            getRole(getTestProjectDir(),
                "src",
                "rolehierarchy1",
                "Team2",
                "Role1");
        
        _team3role2 =
            getRole(getTestProjectDir(),
                "src",
                "rolehierarchy1",
                "Team3",
                "Role2");
        
        _team4role2 =
            getRole(getTestProjectDir(),
                "src",
                "rolehierarchy1",
                "Team4",
                "Role2");
        
        _team2role1m1 = _team2role1.getMethods()[0];
        _team2role1m2 = _team2role1.getMethods()[1];
        
        _itest2m1 = _itest2.getMethods()[0];
        
        _cm1 = _c.getMethods()[0];
        _cm2 = _c.getMethods()[1];
        _team4role2m2 = _team4role2.getMethods()[1];
        _team4role2m1 = _team4role2.getMethods()[0];
        
        
        
    }
        
    protected void setUp() throws Exception
    {
        super.setUp();
    }
    
    public void testIsDeclaredInInterface_yes() throws JavaModelException
	{
    	fail("Not yet implemented");
//    	MIGRATE
//    	IMethod expected = _itest2m1;
//    	IMethod actual = RefactoringUtil.isDeclaredInInterface(_team2role1m1, new NullProgressMonitor());
//    	assertTrue(expected.equals(actual));
//	}
//    
//    public void testIsDeclaredInInterface_no1() throws JavaModelException
//	{
//    	IMethod actual = RefactoringUtil.isDeclaredInInterface(_team2role1m2, new NullProgressMonitor());
//    	assertNull(actual);
//	}
//    
//    public void testIsDeclaredInInterface_no2() throws JavaModelException
//	{
//    	IMethod actual = RefactoringUtil.isDeclaredInInterface(_team4role2m2, new NullProgressMonitor());
//    	assertNull(actual);
//	}
//    
//    public void testOverridesAnotherMethod_yes() throws JavaModelException
//	{
//    	IMethod expected = _cm2;
//    	IMethod actual = RefactoringUtil.overridesAnotherMethod(_team4role2m2, new NullProgressMonitor());
//    	assertTrue(expected.equals(actual));
//	}
//    
//    public void testOverridesAnotherMethod_no() throws JavaModelException
//	{
//    	IMethod actual = RefactoringUtil.overridesAnotherMethod(_team2role1m2, new NullProgressMonitor());
//    	assertNull(actual);
	}

    //TODO(jsv) test with nested teams
}