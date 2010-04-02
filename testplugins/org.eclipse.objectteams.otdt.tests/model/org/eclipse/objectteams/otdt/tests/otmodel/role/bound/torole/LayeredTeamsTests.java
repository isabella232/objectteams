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
 * $Id: LayeredTeamsTests.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.role.bound.torole;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author stephan
 * @version $Id: LayeredTeamsTests.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * This class contains testing methods for a test setting with an empty role class
 * where the role class is bound to a baseclass which is a role of a lower team class
 */
public class LayeredTeamsTests extends FileBasedModelTest
{
    private IOTType _roleOTElem;


	public LayeredTeamsTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(LayeredTeamsTests.class);
        }
        junit.framework.TestSuite suite = new Suite(LayeredTeamsTests.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir(getTestSetting().getTestProject());
        super.setUpSuite();
    }
       
    public void testFieldAnchor() throws Exception
    {
        getTestSetting().setTeamClass("Team_1");
        getTestSetting().setUp();
        performTest("TeamB.java", "TeamB", getTestSetting().getRoleJavaElement());
    }
    
    /** Same as above but using old path syntax. */
    public void testFieldAnchorPath() throws Exception
    {
        getTestSetting().setTeamClass("Team_1b");
        getTestSetting().setUp();
        performTest("TeamB.java", "TeamB", getTestSetting().getRoleJavaElement());
    }
    
    
    public void testBaseAnchor() throws Exception
    {
        getTestSetting().setTeamClass("Team_2");
        getTestSetting().setUp();
        performTest("TeamB.java", "TeamB", getAndTestInnerRole());
    }
    
    /** Same as above but using old path syntax. */
    public void testBaseAnchorPath() throws Exception
    {
        getTestSetting().setTeamClass("Team_2b");
        getTestSetting().setUp();
        performTest("TeamB.java", "TeamB", getAndTestInnerRole());
    }
    
    public void testQualifiedBaseAnchor() throws Exception
    {
        getTestSetting().setTeamClass("Team_3");
        getTestSetting().setUp();
        performTest("TeamB.java", "TeamB", getAndTestInnerRole());
    }
    // no path variant of Team_3: path syntax "SampleRole.base.LowerRole" is illegal.
    
    // witness for IllegalArgumentException in Signature.appendTypeSignature(char[], int, boolean, StringBuffer, boolean)
    // requires capability to handle type anchors in char-encoded signatures
    public void testAnchoredCalloutSignature() throws Exception
    {
        getTestSetting().setTeamClass("Team_4");
        getTestSetting().setUp();
        performTest("TeamB.java", "TeamB", getTestSetting().getRoleJavaElement());
        for (IJavaElement member : _roleOTElem.getChildren()) {
        	if (member.getElementType() == IOTJavaElement.CALLOUT_MAPPING) {
        		assertEquals("Callout signature not as expected", 
        					 "isEqual(LowerRole<@teamB>) -> isEqual(LowerRole<@teamB>)",
        					 member.getElementName());
        	}
        }
    }

    
	private IType getAndTestInnerRole() throws JavaModelException {
		IType roleMid= getTestSetting().getRoleJavaElement();
        assertTrue(roleMid.exists());
        IJavaElement[] inner= roleMid.getChildren();
        assertTrue(inner != null && inner.length > 0);
        assertTrue(inner[0] instanceof IType);
        IType type = (IType)inner[0];
        assertEquals(type.getElementName(), "InnerRole");
		return type;
	}
    
    
    
    void performTest(String baseCU, String baseTeam, IType roleJavaElement) 
    	throws JavaModelException
    {
		ICompilationUnit baseUnit = getCompilationUnit(
                getTestProjectDir(),
                "boundtorole",
                "boundtorole.basepkg",
                baseCU);
		IType baseTeamJavaElem = baseUnit.getType(baseTeam);
        assertTrue("base team should exist", baseTeamJavaElem.exists());
        IJavaElement[] baseChildren= baseTeamJavaElem.getChildren();
        assertTrue("team should have children",baseChildren != null && baseChildren.length>0);
        assertTrue("first child should be the role (IType)", baseChildren[0] instanceof IType);
        IType baseJavaElem= (IType) baseChildren[0];
        assertNotNull(baseJavaElem);
        assertTrue(baseJavaElem.exists());        
        assertEquals(baseJavaElem.getElementName(), "LowerRole");
        
		assertNotNull(roleJavaElement);
        assertTrue(roleJavaElement.exists());

        _roleOTElem = OTModelManager.getOTElement(roleJavaElement);
        assertNotNull(_roleOTElem);
        assertTrue(_roleOTElem instanceof IRoleType);
        IRoleType roleRoleOTElem = (IRoleType) _roleOTElem;

        IType baseClassJavaElem = roleRoleOTElem.getBaseClass();
        assertNotNull(baseClassJavaElem);

        assertEquals("base class should be expected lower role", baseJavaElem, baseClassJavaElem);
        
        IOTType baseClassOTElem = OTModelManager.getOTElement(baseClassJavaElem);
        assertNotNull(baseClassOTElem);
        assertTrue("base class should be role", baseClassOTElem.isRole());
    } 
}
