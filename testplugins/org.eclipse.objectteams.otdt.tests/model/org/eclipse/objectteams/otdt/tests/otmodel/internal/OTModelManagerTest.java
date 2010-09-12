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
 * $Id: OTModelManagerTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.internal;

import junit.framework.Test;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * 
 * @author michael
 * @version $Id: OTModelManagerTest.java 23494 2010-02-05 23:06:44Z stephan $ 
 * 
 */
public class OTModelManagerTest extends FileBasedModelTest {
	
	@SuppressWarnings("unused")
	private ITypeHierarchy _testObj;
	private IType _focusType;
    private IType _T20;
    private IType _T21;    
	@SuppressWarnings("unused")
	private IType _objectType;
    private IType _T21T10T00;
    private IType _T21T11T00;
    
    
    
	public OTModelManagerTest(String name)
	{
		super(name);
	}
	
	public static Test suite()
	{
		if (true)
		{
			return new Suite(OTModelManagerTest.class);
		}
		junit.framework.TestSuite suite = 
			new Suite(OTModelManagerTest.class.getName());
		return suite;
	}
	
	public void setUpSuite() throws Exception
	{
		setTestProjectDir("Hierarchy");
		super.setUpSuite();
		
		String srcFolder = "src";
		String pkg = "test004";
		
		_T20 = 
			getType(getTestProjectDir(),
					srcFolder,
					pkg,
					"T20");

        _T21 = 
            getType(getTestProjectDir(),
                    srcFolder,
                    pkg,
                    "T21");
        
        
        _objectType = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");

        _T21T10T00 = TypeHelper.findNestedRoleType(_T21, "T21.T10.T00");
        _T21T11T00 = TypeHelper.findNestedRoleType(_T21, "T21.T11.T00");
    }
	
	public void testCreation()
	{
		assertCreation(_T20);
        assertCreation(_T21);
        assertCreation(_T21T10T00);
        assertCreation(_T21T11T00);
    }

	public void testOTModelManager_JavaToOTToJava() throws JavaModelException
    {
        _focusType = _T21T10T00;
        IOTType iotFocusType = OTModelManager.getOTElement(_focusType);
        IType actual = (IType)iotFocusType.getCorrespondingJavaElement();
        IType expected = _focusType;
   
        assertEquals(expected, actual);        
    }
    
    public void testOTModelManager_getOTElement() throws JavaModelException
    {
        _focusType = _T21T10T00;
        IOTType iot = OTModelManager.getOTElement(_focusType);
        String actual = iot.getFullyQualifiedName();
        String expected = _focusType.getFullyQualifiedName();
   
        assertEquals(expected, actual);        
    }
    
    public void testOTModelManager_getOTElement_Failed()
    {
        _focusType = _T21T10T00;
        IOTType iotFocusType = OTModelManager.getOTElement(_focusType);
        IType actual = (IType)iotFocusType.getCorrespondingJavaElement();
        IType expected = _T21T11T00;
   
        assertNotSame(expected, actual);  
    }
}
