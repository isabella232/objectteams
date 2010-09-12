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
 * $Id: OTSuperTypeHierarchyTest007_Stress.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.superhierarchy;

import junit.framework.Test;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.hierarchy.OTTypeHierarchies;
import org.eclipse.objectteams.otdt.tests.hierarchy.FileBasedHierarchyTest;

/**
 * @author Michael Krueger (michael)
 * @version $Id: OTSuperTypeHierarchyTest007_Stress.java 23494 2010-02-05 23:06:44Z stephan $
 */
@SuppressWarnings("unused")
public class OTSuperTypeHierarchyTest007_Stress extends FileBasedHierarchyTest
{
    private IType _T1;
    private IType _T2;
    
	private IType _T1_R1;
	private IType _T1_R2;
	
	private IType _T2_R1;
	private IType _T2_R2;
	
	private IType _classA;
    private IType _classB;
    
    private IType _interfaceA;
    private IType _interfaceB;
    private IType _interfaceC;
    
    private IType _objectType;
	
	public OTSuperTypeHierarchyTest007_Stress(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSuperTypeHierarchyTest007_Stress.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(OTSuperTypeHierarchyTest007_Stress.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test007";
		
        _T1 = getType(getTestProjectDir(), srcFolder, pkg, "T1");
        _T2 = getType(getTestProjectDir(), srcFolder, pkg, "T2");
        
        _T1_R1 = getRole(_T1, "T1.R1");
        _T1_R2 = getRole(_T1, "T1.R2");
        
        _T2_R1 = getRole(_T2, "T2.R1");
        _T2_R2 = getRole(_T2, "T2.R2");
                
        _classA =
            getType(getTestProjectDir(), srcFolder, pkg, "ClassA");
        _classB =
            getType(getTestProjectDir(), srcFolder, pkg, "ClassB");
        _interfaceA =
            getType(getTestProjectDir(), srcFolder, pkg, "InterfaceA");
        _interfaceB =
            getType(getTestProjectDir(), srcFolder, pkg, "InterfaceB");
        _interfaceC =
            getType(getTestProjectDir(), srcFolder, pkg, "InterfaceC");
        _objectType =
            getType(getTestProjectDir(), "rt.jar", "java.lang", "Object");
        
	}
	
	public void testGetAllSuperInterfacesFor_T2R2_Run001()
        throws JavaModelException
	{
        runActualTestCase();
	}

    public void testGetAllSuperInterfacesFor_T2R2_Run002()
        throws JavaModelException
    {
        runActualTestCase();
    }
    
    public void testGetAllSuperInterfacesFor_T2R2_Run003()
        throws JavaModelException
    {
        runActualTestCase();
    }
    
    public void testGetAllSuperInterfacesFor_T2R2_Run004()
        throws JavaModelException
    {
        runActualTestCase();
    }
    
    public void testGetAllSuperInterfacesFor_T2R2_Run005()
        throws JavaModelException
    {
        runActualTestCase();
    }
    
    public void testGetAllSuperInterfacesFor_T2R2_Run006()
        throws JavaModelException
    {
        runActualTestCase();
    }
    
    public void testGetAllSuperInterfacesFor_T2R2_Run007()
        throws JavaModelException
    {
        runActualTestCase();
    }
    
    public void testGetAllSuperInterfacesFor_T2R2_Run008()
        throws JavaModelException
    {
        runActualTestCase();
    }
    
    public void testGetAllSuperInterfacesFor_T2R2_Run009()
        throws JavaModelException
    {
        runActualTestCase();
    }
    
    public void testGetAllSuperInterfacesFor_T2R2_Run010()
        throws JavaModelException
    {
        runActualTestCase();
    } 

    private void runActualTestCase() throws JavaModelException
    {
        _focusType = _T2_R2;
        _testObj = createSuperTypeHierarchy(_focusType);
        
        IType[] expected = new IType[] { _interfaceA,
                                         _interfaceB,
                                         _interfaceC };        
        IType [] actual = OTTypeHierarchies.getInstance().getAllSuperInterfaces(_testObj, _focusType);
        
        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));
    }
}
