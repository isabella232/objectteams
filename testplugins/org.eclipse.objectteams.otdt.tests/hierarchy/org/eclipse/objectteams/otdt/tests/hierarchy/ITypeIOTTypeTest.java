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
 * $Id: ITypeIOTTypeTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.hierarchy;

import junit.framework.Test;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IOTTypeHierarchy;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.PhantomType;

/**
 * Testing OTTypeHiearchy with respect to IType vs. IOTType instances as input
 * @author Carsten Pfeiffer (carp)
 *
 * $Id: ITypeIOTTypeTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class ITypeIOTTypeTest extends FileBasedHierarchyTest
{
    @SuppressWarnings("unused")
	private IType _objectType;

    private IType _MyTeam;
    private IType _MySubTeam;
    private IType _MyOtherSubTeam;
    
    private IType _MyTeam_MyRole;
    private IType _MySubTeam_MyRole; // phantom-type!
    private IType _MyOtherSubTeam_MyRole;
    
    private IOTType _OT_MyTeam;
    private IOTType _OT_MySubTeam;
    @SuppressWarnings("unused")
	private IOTType _OT_MyOtherSubTeam;

    private IOTType _OT_MyTeam_MyRole;
    private IOTType _OT_MyOtherSubTeam_MyRole;
    	
	public ITypeIOTTypeTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(ITypeIOTTypeTest.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(ITypeIOTTypeTest.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "simple";
		
        _objectType =
            getType(getTestProjectDir(), "rt.jar", "java.lang", "Object");
        
        _MyTeam = getType(getTestProjectDir(), srcFolder, pkg, "MyTeam");
        _MySubTeam = getType(getTestProjectDir(), srcFolder, pkg, "MySubTeam");
        _MyOtherSubTeam = getType(getTestProjectDir(), srcFolder, pkg, "MyOtherSubTeam");
        
        _MyTeam_MyRole = getRole(_MyTeam, "MyTeam.MyRole");
        _MySubTeam_MyRole = new PhantomType(_MySubTeam, _MyTeam_MyRole);
        _MyOtherSubTeam_MyRole = getRole(_MyOtherSubTeam, "MyOtherSubTeam.MyRole");
        
        _OT_MyTeam = OTModelManager.getOTElement(_MyTeam);
        _OT_MySubTeam = OTModelManager.getOTElement(_MySubTeam);
        _OT_MyOtherSubTeam = OTModelManager.getOTElement(_MyOtherSubTeam);
        _OT_MyTeam_MyRole = OTModelManager.getOTElement(_MyTeam_MyRole);
        _OT_MyOtherSubTeam_MyRole = OTModelManager.getOTElement(_MyOtherSubTeam_MyRole);
	}
	
    public void testCreation()
	{
		assertCreation(_MyTeam);
		assertCreation(_MySubTeam);
		
		assertCreation(_MyTeam_MyRole);
		assertCreation(_MySubTeam_MyRole);
		
		assertCreation(_OT_MyTeam);
		assertCreation(_OT_MySubTeam);
		assertCreation(_OT_MyTeam_MyRole);
	}
	    
    public void testHierarchyCreation_equalFocusType() throws JavaModelException
    {
        IOTTypeHierarchy first  = createOTTypeHierarchy(_MyTeam);
        IOTTypeHierarchy second = createOTTypeHierarchy(_OT_MyTeam);
        
		assertEquals(first.getFocusType(), second.getFocusType());		
    }
    
    public void testGetOTSuperTypeHierarchy() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MyTeam);
        
		ITypeHierarchy first  = _testObj.getOTSuperTypeHierarchy(_MyTeam);
		ITypeHierarchy second = _testObj.getOTSuperTypeHierarchy(_OT_MyTeam);

		assertEquals(first, second);		
    }
    
    public void testGetSubtypes() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MyTeam);
        
		IType [] first  = _testObj.getSubtypes(_MyTeam);
		IType [] second = _testObj.getSubtypes(_OT_MyTeam);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }

    public void testGetAllSuperclasses() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MySubTeam);
        
		IType [] first  = _testObj.getAllSuperclasses(_MySubTeam);
		IType [] second = _testObj.getAllSuperclasses(_OT_MySubTeam);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }

    // TODO: actually use test data with interfaces
    public void testGetAllSuperInterfaces() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MyTeam);
        
		IType [] first  = _testObj.getAllSuperInterfaces(_MyTeam);
		IType [] second = _testObj.getAllSuperInterfaces(_OT_MyTeam);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }
	
    public void testGetAllSupertypes() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MySubTeam);
        
		IType [] first  = _testObj.getAllSupertypes(_MySubTeam);
		IType [] second = _testObj.getAllSupertypes(_OT_MySubTeam);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }

	public void testGetAllSubtypes() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MyTeam);
        
		IType [] first  = _testObj.getAllSubtypes(_MyTeam);
		IType [] second = _testObj.getAllSubtypes(_OT_MyTeam);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }
	
	public void testGetCachedFlags() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MyTeam);
        
		int first  = _testObj.getCachedFlags(_MyTeam);
		int second = _testObj.getCachedFlags(_OT_MyTeam);

		assertTrue(first == second);
    }

	public void testGetSubclasses() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MyTeam);
        
		IType [] first  = _testObj.getSubclasses(_MyTeam);
		IType [] second = _testObj.getSubclasses(_OT_MyTeam);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }

	public void testGetSuperclass() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MySubTeam);
        
		IType first  = _testObj.getSuperclass(_MySubTeam);
		IType second = _testObj.getSuperclass(_OT_MySubTeam);

		assertTrue(compareTypes(first, second));
    }

	public void testGetSuperclasses() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MySubTeam);
        
		IType [] first  = _testObj.getSuperclasses(_MySubTeam);
		IType [] second = _testObj.getSuperclasses(_OT_MySubTeam);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }

	// TODO: actually use test data with interfaces
	public void testGetSuperInterfaces() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MyTeam);
        
		IType [] first  = _testObj.getSuperInterfaces(_MyTeam);
		IType [] second = _testObj.getSuperInterfaces(_OT_MyTeam);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }

	public void testGetSupertypes() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MySubTeam);
        
		IType [] first  = _testObj.getSupertypes(_MySubTeam);
		IType [] second = _testObj.getSupertypes(_OT_MySubTeam);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }

	public void testGetTSuperTypes() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MyOtherSubTeam_MyRole);
        
		IType [] first  = _testObj.getTSuperTypes(_MyOtherSubTeam_MyRole);
		IType [] second = _testObj.getTSuperTypes(_OT_MyOtherSubTeam_MyRole);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }

    public void testGetAllTSuperTypes() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MyOtherSubTeam_MyRole);
        
		IType [] first  = _testObj.getAllTSuperTypes(_MyOtherSubTeam_MyRole);
		IType [] second = _testObj.getAllTSuperTypes(_OT_MyOtherSubTeam_MyRole);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }

    public void testGetExplicitSuperclass() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MySubTeam);
        
		IType first  = _testObj.getExplicitSuperclass(_MySubTeam);
		IType second = _testObj.getExplicitSuperclass(_OT_MySubTeam);

		assertTrue(compareTypes(first, second));
    }

    // TODO: actually use test data with interfaces
    public void testGetExtendingInterfaces() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MyTeam);
        
		IType [] first  = _testObj.getExtendingInterfaces(_MyTeam);
		IType [] second = _testObj.getExtendingInterfaces(_OT_MyTeam);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }

    // TODO: actually use test data with interfaces
    public void testGetImplementingClasses() throws JavaModelException
    {
        _testObj = createOTTypeHierarchy(_MyTeam);
        
		IType [] first  = _testObj.getImplementingClasses(_MyTeam);
		IType [] second = _testObj.getImplementingClasses(_OT_MyTeam);

		assertEquals(first.length, second.length);		
		assertTrue(compareTypes(first, second));
    }
}
