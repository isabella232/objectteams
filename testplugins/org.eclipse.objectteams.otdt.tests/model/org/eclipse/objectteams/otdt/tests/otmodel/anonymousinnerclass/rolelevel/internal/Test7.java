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
package org.eclipse.objectteams.otdt.tests.otmodel.anonymousinnerclass.rolelevel.internal;

import junit.framework.Test;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

/**
 * $Id: Test7.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * testcase:
 * a role class (defined insight the file of its team class) with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is a team class with method with declared lifting
 */
public class Test7 extends AttributeAssignmentTest
{

    private String ANONYMOUS_METHOD_NAME = "method";
    //semicolon at the end of the typestring is there on purpose
    private String ANONYMOUS_INPUTPARA_BASETYPE = "QSampleBase;"; 
    
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
    
    public Test7(String name)
    {
        super(name);
    }

    protected String getTeamName()
    {
        return "Test7_TeamB";
    }
    
    protected String getRoleName()
    {
        return "SampleRole"; 
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
        
        IMethod method = anonymousTypeJavaElem.getMethod(ANONYMOUS_METHOD_NAME, new String[]{ANONYMOUS_INPUTPARA_BASETYPE});
        assertNotNull(method);
        assertTrue(method.exists());
    }
    
    //TODO (kaschja) create test for declared lifting by the time declared lifting is integrated into the OT-model
}
