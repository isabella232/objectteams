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
 * $Id: OTSubHierarchyContentProviderTests.java 23495 2010-02-05 23:15:16Z stephan $
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
import org.eclipse.jdt.internal.ui.typehierarchy.SubTypeHierarchyViewer;
import org.eclipse.jdt.internal.ui.typehierarchy.TypeHierarchyLifeCycle;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.objectteams.otdt.ui.tests.FileBasedUITest;

/**
 * @author haebor
 *
 * 18.07.2005
 */
public class OTSubHierarchyContentProviderTests extends FileBasedUITest
{
    private static final String FAKED_PLUGIN_ID = "org.eclipse.objectteams.otdt.tests";
    
    private static final String PROJECT_DIR = "Hierarchy";
    private static final String SRC_FOLDER = "src";
    
    private SubTypeHierarchyViewer.SubTypeHierarchyContentProvider _testObject;
    //call doHierarchyRefresh(IJavaElement element, null) on _lifeCycle
    //to create a hierarchy on element.
    private TypeHierarchyLifeCycle _lifeCycle;

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

    
    public OTSubHierarchyContentProviderTests(String name)
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
            return new Suite(OTSubHierarchyContentProviderTests.class);
        }
        junit.framework.TestSuite suite = new Suite(OTSubHierarchyContentProviderTests.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir(PROJECT_DIR);
        
        super.setUpSuite();
        
		
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
        _lifeCycle = new TypeHierarchyLifeCycle(false);
        _testObject = 
            new SubTypeHierarchyViewer.SubTypeHierarchyContentProvider(
                    _lifeCycle);
    }
    
    public void testHierarchyCreated()
    {
        try
        {
            _lifeCycle.doHierarchyRefresh(_T1_R1, new NullProgressMonitor());
        }
        catch (JavaModelException exc)
        {
            exc.printStackTrace();
        }

        ITypeHierarchy hierarchy = _lifeCycle.getHierarchy();
        assertNotNull(hierarchy);
    }
    
    public void testGetParentAlwaysNull()
    {
        Object expected = null;
        Object actual;
        
        for (int idx = 0; idx < _allTypesInProject.length; idx++)
        {
            IType cur = _allTypesInProject[idx];
	        actual = _testObject.getParent(cur);
	        assertEquals("Parent not null for " + cur.getElementName() + " ", expected, actual);
        }
    }
    
    public void testRecursiveGetChildren_T1R1()
    {
        try
        {
            _lifeCycle.doHierarchyRefresh(_T1_R1, new NullProgressMonitor());
        }
        catch (JavaModelException exc)
        {
            exc.printStackTrace();
        }
        
        TreeNode actualRoot;
        TreeNode expectedRoot;
        
        //pretty ugly, maybe replace this with an automatic hierarchy creation
        //that generates the hierarchy according to some String or with the help 
        //of an array of types that denotes a path :-I
        expectedRoot = new TreeNode(_T1_R1);
        
        expectedRoot.setChildrenByElements(new Object[] {_T2_R1});
        
        expectedRoot.getChildNode(_T2_R1).setChildrenByElements(new Object[] {_T2_R2, _T3_R1, _T5_R1, _T6_R1, _T4_R2});
        
        expectedRoot.getChildNode(_T2_R1).getChildNode(_T2_R2).setChildrenByElements(new Object[] {_T3_R2, _T4_R2, _T5_R2, _T8_R2});
        expectedRoot.getChildNode(_T2_R1).getChildNode(_T3_R1).setChildrenByElements(new Object[] {_T3_R2});
        expectedRoot.getChildNode(_T2_R1).getChildNode(_T5_R1).setChildrenByElements(new Object[] {_T5_R3, _T7_R3});
        expectedRoot.getChildNode(_T2_R1).getChildNode(_T6_R1).setChildrenByElements(new Object[] {_T8_R2, _T2_R2});

        expectedRoot.getChildNode(_T2_R1).getChildNode(_T2_R2).getChildNode(_T5_R2).setChildrenByElements(new Object[] {_T7_R2});
        expectedRoot.getChildNode(_T2_R1).getChildNode(_T5_R1).getChildNode(_T5_R3).setChildrenByElements(new Object[] {_T7_R3, _T5_R2});
        expectedRoot.getChildNode(_T2_R1).getChildNode(_T5_R1).getChildNode(_T7_R3).setChildrenByElements(new Object[] {_T7_R2});
        
        expectedRoot.getChildNode(_T2_R1).getChildNode(_T5_R1).getChildNode(_T5_R3).getChildNode(_T7_R3).setChildrenByElements(new Object[] {_T7_R2});
        expectedRoot.getChildNode(_T2_R1).getChildNode(_T5_R1).getChildNode(_T5_R3).getChildNode(_T5_R2).setChildrenByElements(new Object[] {_T7_R2});

        expectedRoot.getChildNode(_T2_R1).getChildNode(_T6_R1).getChildNode(_T2_R2).setChildrenByElements(new Object[] {_T3_R2, _T5_R2, _T4_R2, _T8_R2});
        expectedRoot.getChildNode(_T2_R1).getChildNode(_T6_R1).getChildNode(_T2_R2).getChildNode(_T5_R2).setChildrenByElements(new Object[]{_T7_R2});
        
        actualRoot = fillTree(_testObject, new TreeNode(_T1_R1));
        
        assertTrue("Hierarchy trees don't match", expectedRoot.equalsAsserted(actualRoot, new ITypeComparator(), 0));
    }
    
    private TreeNode fillTree(ITreeContentProvider contentProvider, TreeNode curNode)
    {
        Object[] children = contentProvider.getChildren(curNode.getElement());
        TreeNode[] treeChildren = curNode.setChildrenByElements(children);
        
        for (int idx = 0; idx < treeChildren.length; idx++)
        {
            fillTree(contentProvider, treeChildren[idx]);
        }
        return curNode;
    }
}