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
 * $Id: HierarchyResolverTestWithSrc003.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.superhierarchy;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author anklam
 *
 * @version $Id: HierarchyResolverTestWithSrc003.java 23494 2010-02-05 23:06:44Z stephan $
 */
@SuppressWarnings("unused")
public class HierarchyResolverTestWithSrc003 extends FileBasedModelTest {
	
	private ITypeHierarchy _testObj;
	private NullProgressMonitor _monitor;
	
	private IType _focusType;
	
	private IRoleType _T1_R1;
	private IRoleType _T1_R2;
	
	private IRoleType _T2_R2;
	
	private IRoleType _T3_R1;
	private IRoleType _T3_R2;

	private IRoleType _T4_R1;
	private IRoleType _T4_R2;
	private IRoleType _T4_R3;

	private IType _interfaceA;
    
    private IType _objectType;
	
	public HierarchyResolverTestWithSrc003(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(HierarchyResolverTestWithSrc003.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(HierarchyResolverTestWithSrc003.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		_monitor = new NullProgressMonitor();
		
		String srcFolder = "src";
		String pkg = "test003";
		
		_T1_R1 = 
			getRole(getTestProjectDir(),
					srcFolder,
					pkg,
					"T1",
			        "R1");
		
		_T1_R2 = 
			getRole(getTestProjectDir(),
					srcFolder,
					pkg,
					"T1",
			        "R2");
		
		_T2_R2 = 
			getRole(getTestProjectDir(),
					srcFolder,
					pkg,
					"T2",
			        "R2");
        
		
		_T3_R1 = 
			getRole(getTestProjectDir(),
					srcFolder,
					pkg,
					"T3",
			        "R1");
		
		_T3_R2 = 
			getRole(getTestProjectDir(),
					srcFolder,
					pkg,
					"T3",
			        "R2");
		
		
		_T4_R1 = 
			getRole(getTestProjectDir(),
					srcFolder,
					pkg,
					"T4",
			        "R1");
		
		_T4_R2 = 
			getRole(getTestProjectDir(),
					srcFolder,
					pkg,
					"T4",
			        "R2");
		
		_T4_R3 = 
			getRole(getTestProjectDir(),
					srcFolder,
					pkg,
					"T4",
			        "R3");

        _interfaceA = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "InterfaceA");
        
        _objectType = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");
	}
	
	
    public void testGetSuperInterfacesFor_T2R2() 
    throws JavaModelException
    {
        _focusType = (IType)_T2_R2.getCorrespondingJavaElement();
        
        
        ITypeHierarchy hierarchy = _focusType.newTypeHierarchy(_monitor);

        IType[] expected = new IType[] { _interfaceA };
        
        IType [] actual = ((TypeHierarchy)hierarchy).getSuperInterfaces(_focusType);
        assertEquals(expected.length, actual.length);
        
        assertTrue("Wrong Types", compareTypes(expected, actual));
    }
}
