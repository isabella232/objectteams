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
 * $Id: OTSubTypeHierarchyTest019.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author Michael Krueger (mkr)
 *
 * $Id: OTSubTypeHierarchyTest019.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class OTSubTypeHierarchyTest019 extends FileBasedModelTest
{
	
	private TypeHierarchy _testObj;

	private IType _focusType;
	
    private IType _TA;
    private IType _TB;
    private IType _TC;

    private IType _TB_R1;
    private IType _TB_R2;
    private IType _TB_R3;
    private IType _TB_R4;
    private IType _TB_R5;
    private IType _TB_R6;
    private IType _TB_R7;
    private IType _TB_R8;
    private IType _TB_R9;
    private IType _TB_R10;
    private IType _TB_R11;
    private IType _TB_R12;
    private IType _TB_R13;
    private IType _TB_R14;
    private IType _TB_R15;

    
	public OTSubTypeHierarchyTest019(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTSubTypeHierarchyTest019.class);
		}
		@SuppressWarnings("unused")
		junit.framework.TestSuite suite = 
			new Suite(OTSubTypeHierarchyTest019.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test019";

        _TA = getType(getTestProjectDir(),
                srcFolder,
                pkg,
                "TA");
		
		_TB = getType(getTestProjectDir(),
                srcFolder,
                pkg,
                "TB");

		_TC = getType(getTestProjectDir(),
                srcFolder,
                pkg,
                "TC");

		_TB_R1 = getRole(_TB, "TB.R1");
        _TB_R2 = getRole(_TB, "TB.R2");
        _TB_R3 = getRole(_TB, "TB.R3");
        _TB_R4 = getRole(_TB, "TB.R4");
        _TB_R5 = getRole(_TB, "TB.R5");
        _TB_R6 = getRole(_TB, "TB.R6");
        _TB_R7 = getRole(_TB, "TB.R7");
        _TB_R8 = getRole(_TB, "TB.R8");
        _TB_R9 = getRole(_TB, "TB.R9");
        _TB_R10 = getRole(_TB, "TB.R10");
        _TB_R11 = getRole(_TB, "TB.R11");
        _TB_R12 = getRole(_TB, "TB.R12");
        _TB_R13 = getRole(_TB, "TB.R13");
        _TB_R14 = getRole(_TB, "TB.R14");
        _TB_R15 = getRole(_TB, "TB.R15");
		
	}
	
    public void testCreation()
    {
        assertCreation(_TA);
        assertCreation(_TB);
        assertCreation(_TC);

        assertCreation(_TB_R1);
        assertCreation(_TB_R2);
        assertCreation(_TB_R3);
        assertCreation(_TB_R4);
        assertCreation(_TB_R5);
        assertCreation(_TB_R6);
        assertCreation(_TB_R7);
        assertCreation(_TB_R8);
        assertCreation(_TB_R9);
        assertCreation(_TB_R10);
        assertCreation(_TB_R11);
        assertCreation(_TB_R12);
        assertCreation(_TB_R13);
        assertCreation(_TB_R14);
        assertCreation(_TB_R15);    
    }
    
	public void testOTTypeHiearchyCreation() throws JavaModelException
	{
		_focusType = _TB_R1;

		_testObj = new TypeHierarchy(_focusType, null, _focusType.getJavaProject(), true);
        _testObj.refresh(new NullProgressMonitor());
        
		assertTrue(true);
	}
}
	
