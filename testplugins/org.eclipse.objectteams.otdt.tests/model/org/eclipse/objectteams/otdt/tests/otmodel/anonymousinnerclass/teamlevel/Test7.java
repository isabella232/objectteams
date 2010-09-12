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
 * $Id: Test7.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.anonymousinnerclass.teamlevel;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * $Id: Test7.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * testcase:
 * a team class with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is a team class with method with declared lifting
 */
public class Test7 extends FileBasedModelTest
{
    
    private IType _teamJavaElem = null;
    
    public Test7(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test7.class);
        }
        junit.framework.TestSuite suite = new Suite(Test7.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("AnonymousInnerclass");
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
		super.setUp();
        try
        {
            ICompilationUnit teamUnit = getCompilationUnit(
                "AnonymousInnerclass",
                "teamlevel",
                "teamlevel.teampkg",
                "Test7_TeamB.java");
            _teamJavaElem = teamUnit.getType("Test7_TeamB");
            
            System.out.println("\nAnonymousInnerclassTeamLevelTest7:");
            System.out.println("Teamklasse: \n" +_teamJavaElem);
        }
        catch (JavaModelException ex)
        {
            ex.printStackTrace();
        }
    }         
    
    public void testExistenceOfAnonymousType() throws JavaModelException
    {
        assertNotNull(_teamJavaElem);
        assertTrue(_teamJavaElem.exists());
        
        IField teamlevelAttr = _teamJavaElem.getField("teamlevelAttr");
        assertNotNull(teamlevelAttr);
        assertTrue(teamlevelAttr.exists());
        
        IType anonymousType = teamlevelAttr.getType("",1);
        assertNotNull(anonymousType);
        assertTrue(anonymousType.exists());
    }
    
    
    private IType getAnonymousType() throws JavaModelException
    {
        if ((_teamJavaElem != null) && (_teamJavaElem.exists()))
        {
            IField teamlevelAttr = _teamJavaElem.getField("teamlevelAttr");
            
            if ((teamlevelAttr != null) && (teamlevelAttr.exists()))
            {
                IType anonymousType = teamlevelAttr.getType("",1);
                
                if ((anonymousType != null) && (anonymousType.exists()))
                {
                    return anonymousType;
                }
            }
        }
        return null;
    }
    
    public void testExistenceOfAnonymousTypeInOTModel() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);
        
        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);
    }
    
    public void testTeamPropertyOfAnonymousType() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);
        
        IOTType anonymousTypeOTElem = OTModelManager.getOTElement(anonymousTypeJavaElem);
        assertNotNull(anonymousTypeOTElem);
        
        assertTrue(anonymousTypeOTElem.isTeam());
    }
    
    public void testContainmentOfMethodInAnonymousType() throws JavaModelException
    {
        IType anonymousTypeJavaElem = getAnonymousType();
        assertNotNull(anonymousTypeJavaElem);
        
        IMethod method = anonymousTypeJavaElem.getMethod("method", new String[]{"QSampleBase;"});
        assertNotNull(method);
        assertTrue(method.exists());
    }
    
    //TODO (kaschja) create test for declared lifting by the time declared lifting is integrated into the OT-model
}
