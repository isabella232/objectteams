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
 * $Id: OTSubTypeHierarchyTest018.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * 
 * @author Michael Krueger (mkr)
 * @version $Id: OTSubTypeHierarchyTest018.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTSubTypeHierarchyTest018 extends FileBasedModelTest {
	
	private ITypeHierarchy _testObj;
	@SuppressWarnings("unused")
	private IType _focusType;
    private IType _TA1;
    private IType _TA2;
    private IType _TA3;
    private IType _TZ1;
    private IType _TZ2;

    private IType _TA1_TB1_R1;
    private IType _TA2_TB1_R1;
    private IType _TA2_TB2_R1;
    private IType _TA2_TB2_R2;
    private IType _TA3_TB2_R2;

    private IType _TZ1_TX1;    

    private IType _TZ1_TX1_TB2_R2;    
    private IType _TZ1_TX2_TB2_R2;    
    private IType _TZ2_TX2_TB2_R1;    

    
    @SuppressWarnings("unused")
	private IType _objectType;
    
    
	public OTSubTypeHierarchyTest018(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSubTypeHierarchyTest018.class);
		}
		@SuppressWarnings("unused")
		junit.framework.TestSuite suite = 
			new Suite(OTSubTypeHierarchyTest018.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test018";

        _TA1 = getType(getTestProjectDir(), srcFolder, pkg, "TA1");
        _TA2 = getType(getTestProjectDir(), srcFolder, pkg, "TA2");
        _TA3 = getType(getTestProjectDir(), srcFolder, pkg, "TA3");
        _TZ1 = getType(getTestProjectDir(), srcFolder, pkg, "TZ1");
        _TZ2 = getType(getTestProjectDir(), srcFolder, pkg, "TZ2");

        _objectType = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");

        _TA1_TB1_R1 = TypeHelper.findNestedRoleType(_TA1, "TA1.TB1.R1");
        _TA2_TB1_R1 = TypeHelper.findNestedRoleType(_TA2, "TA2.TB1.R1");
        _TA2_TB2_R1 = TypeHelper.findNestedRoleType(_TA2, "TA2.TB2.R1");
        _TA2_TB2_R2 = TypeHelper.findNestedRoleType(_TA2, "TA2.TB2.R2");
        _TA3_TB2_R2 = TypeHelper.findNestedRoleType(_TA3, "TA3.TB2.R2");
        _TZ1_TX1 = TypeHelper.findNestedRoleType(_TZ1, "TZ1.TX1");
        _TZ1_TX1_TB2_R2 = TypeHelper.findNestedRoleType(_TZ1, "TZ1.TX1.TB2.R2");
        _TZ1_TX2_TB2_R2 = TypeHelper.findNestedRoleType(_TZ1, "TZ1.TX2.TB2.R2");
        _TZ2_TX2_TB2_R1 = TypeHelper.findNestedRoleType(_TZ2, "TZ2.TX2.TB2.R1");


    }

    private void initHierarchy(IType focus, boolean computeSubtypes)
            throws JavaModelException
    {        
    	_testObj = new TypeHierarchy(focus, null, focus.getJavaProject(), computeSubtypes);
    	_testObj.refresh(new NullProgressMonitor());
        _focusType = focus;
    }
        
	public void testCreation()
	{
        assertCreation(_TA1);
        assertCreation(_TA2);
        assertCreation(_TZ1);
        assertCreation(_TZ2);

        assertCreation(_TA1_TB1_R1);
        assertCreation(_TA2_TB1_R1);
        assertCreation(_TA2_TB2_R1);
        assertCreation(_TA2_TB2_R2);
        assertCreation(_TA3_TB2_R2);

        assertCreation(_TZ1_TX1_TB2_R2);
        assertCreation(_TZ1_TX2_TB2_R2);
        assertCreation(_TZ2_TX2_TB2_R1);

        assertCreation(_TZ1_TX1);
    }


    public void testGetAllSubtypes_TA1_TB1_R1() throws JavaModelException
    {
        initHierarchy(_TA1_TB1_R1, true);
        
        IType[] expected = new IType[] { _TA2_TB1_R1,
                                         _TA2_TB2_R1,
                                         _TA2_TB2_R2,
                                         _TA3_TB2_R2,
                                         _TZ1_TX1_TB2_R2,
                                         _TZ1_TX2_TB2_R2,
                                         _TZ2_TX2_TB2_R1
                                       };    
        
        
        IType[] actual = _testObj.getAllSubtypes(_TA1_TB1_R1);
        
        assertEquals(expected.length, actual.length);
        assertTrue(compareTypes(expected, actual));
    }

    
}
