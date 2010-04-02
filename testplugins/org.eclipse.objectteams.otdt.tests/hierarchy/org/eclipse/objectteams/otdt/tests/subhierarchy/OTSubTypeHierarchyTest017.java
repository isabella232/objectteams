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
 * $Id: OTSubTypeHierarchyTest017.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.subhierarchy;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * 
 * @author Michael Krueger (mkr)
 * @version $Id: OTSubTypeHierarchyTest017.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTSubTypeHierarchyTest017 extends FileBasedModelTest {
	
	private ITypeHierarchy _testObj;
	@SuppressWarnings("unused")
	private IType _focusType;
    private IType _TA;
    private IType _TB;
    private IType _TC;
    private IType _TD;
    private IType _TE;

    private IType _TA_R1;
    private IType _TA_R2;
    private IType _TA_R3;
    private IType _TB_R1;
    private IType _TB_R2;
    private IType _TC_R2;
    private IType _TC_R3;
    private IType _TD_R1;
    private IType _TE_R2;
    private IType _TE_R3;

    
    
    @SuppressWarnings("unused")
	private IType _objectType;
    
    
	public OTSubTypeHierarchyTest017(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSubTypeHierarchyTest017.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(OTSubTypeHierarchyTest017.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test017";

        _TA = getType(getTestProjectDir(), srcFolder, pkg, "TA");
        _TB = getType(getTestProjectDir(), srcFolder, pkg, "TB");
        _TC = getType(getTestProjectDir(), srcFolder, pkg, "TC");
        _TD = getType(getTestProjectDir(), srcFolder, pkg, "TD");
        _TE = getType(getTestProjectDir(), srcFolder, pkg, "TE");

        _objectType = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");

        _TA_R1 = TypeHelper.findNestedRoleType(_TA, "TA.R1");
        _TA_R2 = TypeHelper.findNestedRoleType(_TA, "TA.R2");
        _TA_R3 = TypeHelper.findNestedRoleType(_TA, "TA.R3");
        _TB_R1 = TypeHelper.findNestedRoleType(_TB, "TB.R1");
        _TB_R2 = TypeHelper.findNestedRoleType(_TB, "TB.R2");
        _TC_R2 = TypeHelper.findNestedRoleType(_TC, "TC.R2");
        _TC_R3 = TypeHelper.findNestedRoleType(_TC, "TC.R3");
        _TD_R1 = TypeHelper.findNestedRoleType(_TD, "TD.R1");
        _TE_R2 = TypeHelper.findNestedRoleType(_TE, "TE.R2");
        _TE_R3 = TypeHelper.findNestedRoleType(_TE, "TE.R3");

    }

    private void initHierarchy(IType focus, boolean computeSubtypes)
            throws JavaModelException
    {        
    	_testObj = new OTTypeHierarchy(focus, focus.getJavaProject(), computeSubtypes);
    	_testObj.refresh(new NullProgressMonitor());
        _focusType = focus;
    }
        
	public void testCreation()
	{
        assertCreation(_TA);
        assertCreation(_TB);
        assertCreation(_TD);
        assertCreation(_TE);

        assertCreation(_TA_R1);
        assertCreation(_TA_R2);
        assertCreation(_TA_R3);

        assertCreation(_TB_R1);
        assertCreation(_TB_R2);

        assertCreation(_TC_R2);
        assertCreation(_TC_R3);

        assertCreation(_TD_R1);
        
        assertCreation(_TE_R2);
        assertCreation(_TE_R3);        
    }

    public void testGetSubtypes_TD_R1() throws JavaModelException
    {
        initHierarchy(_TA_R1, true);
        
        IType[] expected = new IType[] { _TB_R2, _TE_R2 };
        IType[] actual = _testObj.getSubtypes(_TD_R1);
        
        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetAllSubtypes_TD_R1() throws JavaModelException
    {
        initHierarchy(_TA_R1, true);
        
        IType[] expected = new IType[] { _TA_R3, _TB_R2, _TE_R2, _TE_R3 };
        IType[] actual = _testObj.getAllSubtypes(_TD_R1);
        
        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetSubtypes_TB_R2() throws JavaModelException
    {
        initHierarchy(_TA_R1, true);
        
        IType[] expected = new IType[] { _TA_R3, _TC_R2, _TC_R3, _TE_R2, _TE_R3};
        IType[] actual = _testObj.getSubtypes(_TB_R2);
        
        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }

    public void testGetAllSubtypes_TB_R2() throws JavaModelException
    {
        initHierarchy(_TA_R1, true);
        
        IType[] expected = new IType[] { _TA_R3, _TC_R2, _TC_R3, _TE_R2, _TE_R3 };
        IType[] actual = _testObj.getAllSubtypes(_TB_R2);
        
        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }

    
}
