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
package org.eclipse.objectteams.otdt.ui.tests.hierarchy.contentprovider;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.typehierarchy.SuperTypeHierarchyViewer;
import org.eclipse.jdt.internal.ui.typehierarchy.TraditionalHierarchyViewer;
import org.eclipse.jdt.internal.ui.typehierarchy.TraditionalHierarchyViewer.TraditionalHierarchyContentProvider;
import org.eclipse.jdt.internal.ui.typehierarchy.TypeHierarchyLifeCycle;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.objectteams.otdt.ui.tests.FileBasedUITest;

/**
 * @author haebor
 *
 * 18.07.2005
 */
public class TraditionalHierarchyContentProviderTests extends FileBasedUITest
{
    private static final String FAKED_PLUGIN_ID = "org.eclipse.objectteams.otdt.tests";
    
    private static final String PROJECT_DIR = "Hierarchy";
    private static final String SRC_FOLDER = "src";
    
    private TraditionalHierarchyContentProvider _testObject;
    //call doHierarchyRefresh(IJavaElement element, null) on _lifeCycle
    //to create a hierarchy on element.
    private TypeHierarchyLifeCycle _lifeCycle;
    
    private IType _objectType;

    private IType _T1;
    private IType _T2;
    private IType _T3;
    private IType _T4;
    private IType _T5;
    private IType _T6;
    private IType _T7;
    private IType _T8;
    
	private IType _T1_R1;
	private IType _T1_R2;
	
	private IType _T2_R1;
	private IType _T2_R2;
	
	private IType _T3_R1;
	private IType _T3_R2;
	
	private IType _T4_R2;
	
	private IType _T5_R1;
	private IType _T5_R2;
	private IType _T5_R3;
	
	private IType _T6_R1;
	
	private IType _T7_R2;
	private IType _T7_R3;
	
	private IType _T8_R2;
	
	private IType[] _allTypesInProject;

    
    public TraditionalHierarchyContentProviderTests(String name)
    {
        super(name);
    }

    protected String getPluginID()
    {
        //overwrites the ID because tests are using the 
        //workspace of org.eclipse.objectteams.otdt.tests
        return FAKED_PLUGIN_ID;
    }
    
    public static Test suite()
    {
        if (true)
        {
            return new Suite(TraditionalHierarchyContentProviderTests.class);
        }
        junit.framework.TestSuite suite = new Suite(TraditionalHierarchyContentProviderTests.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir(PROJECT_DIR);
        
        super.setUpSuite();		
        
		initializeTypes();
        _lifeCycle = new TypeHierarchyLifeCycle(true);
        _testObject = 
            new TraditionalHierarchyViewer.TraditionalHierarchyContentProvider(_lifeCycle);

    }

	private void initializeTypes() throws JavaModelException {
		String pkg = "test001";

		_T1 = 
			getType(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T1");
		
		_T2 = 
			getType(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T2");

		_T3 = 
			getType(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T3");

		
		_T4 = 
			getType(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T4");

		_T5 = 
			getType(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T5");

		_T6 = 
			getType(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T6");

		
		_T7 = 
			getType(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T7");

		_T8 = 
			getType(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T8");

        _objectType = 
            getType(getTestProjectDir(),
                    "rt.jar",
                    "java.lang",
                    "Object");
        
		_T1_R1 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T1",
			        "R1").getCorrespondingJavaElement();
		
		_T1_R2 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T1",
			        "R2").getCorrespondingJavaElement();
		
		_T2_R1 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T2",
			        "R1").getCorrespondingJavaElement();
		
		_T2_R2 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T2",
			        "R2").getCorrespondingJavaElement();
		
		_T3_R1 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T3",
			        "R1").getCorrespondingJavaElement();
		
		_T3_R2 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T3",
			        "R2").getCorrespondingJavaElement();
		
		_T4_R2 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T4",
			        "R2").getCorrespondingJavaElement();
		
		_T5_R1 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T5",
			        "R1").getCorrespondingJavaElement();
		
		_T5_R2 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T5",
			        "R2").getCorrespondingJavaElement();
		
		_T5_R3 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T5",
			        "R3").getCorrespondingJavaElement();
		
		_T6_R1 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T6",
			        "R1").getCorrespondingJavaElement();
		
		_T7_R2 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T7",
			        "R2").getCorrespondingJavaElement();
		
		_T7_R3 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T7",
			        "R3").getCorrespondingJavaElement();
		
		_T8_R2 = (IType)
			getRole(getTestProjectDir(),
					SRC_FOLDER,
					pkg,
					"T8",
			        "R2").getCorrespondingJavaElement();
        
		
		_allTypesInProject = new IType[]
		                              {
		        _T1, _T2, _T3, _T4, _T5, _T6, _T7, _T8,
		        _T1_R1, _T1_R2, 
		        _T2_R1, _T2_R2,
		        _T3_R1, _T3_R2,
		        _T4_R2,
		        _T5_R1, _T5_R2, _T5_R3,
		        _T6_R1,
		        _T7_R2, _T7_R3,
		        _T8_R2
		                              };
	}
    void setHierarchyFocus(IType type) {
    	try
    	{
    		_lifeCycle.doHierarchyRefresh(type, new NullProgressMonitor());
    	}
    	catch (JavaModelException exc)
    	{
    		exc.printStackTrace();
    		fail("JavaModelException while refreshing hierarchy");
    	}    	
    }
    
    IType[] getParentChain_T5R2() {
    	return new IType[] { _T2_R2, _T1_R2, _T5_R3, _T5_R1, _T2_R1, _T1_R1, _objectType };
    }

    public void testGetParents_T5R2()
    {
    	IType current = _T5_R2;
        setHierarchyFocus(current);
        
        IType[] expected = getParentChain_T5R2();
        int i=0;
        while ((current = (IType) _testObject.getParent(current)) != null) {
        	assertEquals("Unexpected parent ("+i+"): "+current.getElementName(), expected[i++], current);
        }
    }

    // challenge whether gc can collect necessary roles:
    public void testGetParents_T5R2_gc() throws JavaModelException
    {
    	IType current = _T5_R2;
        setHierarchyFocus(current);
        _T2_R2 = _T1_R2 = _T5_R3 = _T5_R1 = _T2_R1 = _T1_R1 = _objectType = null;
        System.gc();
        initializeTypes();       
        
        IType[] expected = getParentChain_T5R2();
        int i=0;
        while ((current = (IType) _testObject.getParent(current)) != null) {
        	assertEquals("Unexpected parent ("+i+"): "+current.getElementName(), expected[i++], current);
        }
    }

    public void testPathToT5R2()
    {
        setHierarchyFocus(_T5_R2);
        
        IType[] expected = getParentChain_T5R2();
        for (int i=expected.length-1; i>0; i--) {
        	IType[] subs = castToIType(_testObject.getChildren(expected[i]));
        	assertEquals("Unexpected number of children of "+expected[i].getElementName(), 1, subs.length);
        	IType expectedType = expected[i-1];
			IType actualType = subs[0];
			assertEquals("Unexpected child ("+i+"): "+actualType.getElementName(), expectedType, actualType);
        }
    }
    
    
    private IType[] castToIType(Object[] types)
    {
        if(types == null)
        {
           return null;
        }
        IType[] result = new IType[types.length];
        
		for (int idx = 0; idx < types.length; idx++)
		{
		    result[idx] = (IType)types[idx];
		}
        return result;
    }
}