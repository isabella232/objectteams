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
 * $Id: OTSuperTypeHierarchyTest015.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.objectteams.otdt.core.hierarchy.OTTypeHierarchies;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * 
 * @author michael
 * @version $Id: OTSuperTypeHierarchyTest015.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTSuperTypeHierarchyTest015 extends FileBasedModelTest {
	
	@SuppressWarnings("unused")
	private ITypeHierarchy _testObj;
	private IType _focusType;
	private IType _objectType;

    private IType _A;
    private IType _B;
    private IType _C;
    
    
    
	public OTSuperTypeHierarchyTest015(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSuperTypeHierarchyTest015.class);
		}
		@SuppressWarnings("unused")
		junit.framework.TestSuite suite = 
			new Suite(OTSuperTypeHierarchyTest015.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test015";
		
		_A = 
			getType(getTestProjectDir(),
					srcFolder,
					pkg,
					"A");

        _B = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "B");

        
        _C = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "C");

        
        _objectType = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");

    }

    
	public void testCreation()
	{
		assertCreation(_A);
        assertCreation(_B);
        assertCreation(_C);
    }

    
    public void testGetExplicitSuperclass_B() throws JavaModelException
    {
        _focusType = _B;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(hierarchy, _focusType);
        IType expected = _A;
           
        assertTrue(compareTypes(expected, actual));        
    }

    
    public void testGetExplicitSuperclass_C() throws JavaModelException
    {
        _focusType = _C;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType actual = OTTypeHierarchies.getInstance().getExplicitSuperclass(hierarchy, _focusType);
        IType expected = _B;
           
        assertTrue(compareTypes(expected, actual));        
    }
    
    public void testGetAllSuperclasses_C() throws JavaModelException
    {
        _focusType = _C;
        
        TypeHierarchy hierarchy =
            new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), false);
        hierarchy.refresh(new NullProgressMonitor());

        IType[] actual = hierarchy.getAllSuperclasses(_focusType);
        IType[] expected = new IType[] { _A, _B, _objectType };

        assertEquals(expected.length, actual.length);        
        assertTrue(compareTypes(expected, actual));        
    }

    
    
    
    
    
    
    
}
