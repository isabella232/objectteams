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
 * $Id: Test1.java 23494 2010-02-05 23:06:44Z stephan $
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

/**
 * $Id: Test1.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * testcase:
 * a role class (defined insight the file of its team class) with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is an ordinary class with a method
 */
public class Test1 extends AttributeAssignmentTest
{
    private final String ANONYMOUS_METHOD_NAME = "additionalMethod";

    
    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test1.class);
        }
        junit.framework.TestSuite suite = new Suite(Test1.class
            .getName());
        return suite;
    }    
    
    public Test1(String name)
    {
        super(name);
    }    
    
    protected String getTeamName()
    {
        return "Test1_SampleTeam";
    }    
    
    protected String getRoleName()
    {
        return "SampleRole";
    }
    
    public void testContainmentOfMethodInAnonymousType() throws JavaModelException
    {
        IType anonymousType = getAnonymousType();
        assertNotNull(anonymousType);
        
        IMethod methodOfAnonymousType = anonymousType.getMethod(ANONYMOUS_METHOD_NAME, new String[0]);
        assertNotNull(methodOfAnonymousType);
        assertTrue(methodOfAnonymousType.exists());
    } 
}
