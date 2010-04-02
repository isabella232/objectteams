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
 * $Id: AttributeAssignmentTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.anonymousinnerclass.rolelevel.internal;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * $Id: AttributeAssignmentTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * superclass for all testcases
 * in which an instance of an anonymous class is assigned to an attribut
 */
public abstract class AttributeAssignmentTest extends GeneralTest
{
    
    private final String ATTR_NAME = "rolelevelAttr";
    
    public AttributeAssignmentTest(String name)
    {
        super(name);
    }
    
    protected IType getAnonymousType() throws JavaModelException
    {
        IType fieldOwner = _roleJavaElem;
        
        if ((fieldOwner != null) && (fieldOwner.exists()))
        {
            IField enclosingField = fieldOwner.getField(ATTR_NAME);
            
            if ((enclosingField != null) && (enclosingField.exists()))
            {
                IType anonymousType = enclosingField.getType("",1);
                
                if ((anonymousType != null) && (anonymousType.exists()))
                {
                    return anonymousType;
                }
            }
        }
        return null;
    } 
    
    public void testExistenceOfAnonymousType() throws JavaModelException
    {
        assertNotNull(_roleJavaElem);
        assertTrue(_roleJavaElem.exists());
        
        IField rolelevelAttr = _roleJavaElem.getField(ATTR_NAME);
        assertNotNull(rolelevelAttr);
        assertTrue(rolelevelAttr.exists());
        
        IType anonymousType = rolelevelAttr.getType("",1);
        assertNotNull(anonymousType);
        assertTrue(anonymousType.exists());
    }    
}
