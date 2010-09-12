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
 * $Id: LocalClassTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.anonymousinnerclass.rolelevel.internal;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * $Id: LocalClassTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * superclass for all testcases
 * in which the anonymous class is declared as a local class insight a method
 */
public abstract class LocalClassTest extends GeneralTest
{    
    
    private final String METHOD_NAME = "rolelevelMethod";
  
    
    public LocalClassTest(String name)
    {
        super(name);
    }
    
    public void testExistenceOfAnonymousType() throws JavaModelException
    {
        assertNotNull(_roleJavaElem);
        assertTrue(_roleJavaElem.exists());
         
        IMethod rolelevelMethod = _roleJavaElem.getMethod(METHOD_NAME, new String[0]);
        assertNotNull(rolelevelMethod);
        assertTrue(rolelevelMethod.exists());
        
        IType anonymousType = rolelevelMethod.getType("",1);
        assertNotNull(anonymousType);
        assertTrue(anonymousType.exists());
    }  
 
    
    protected IType getAnonymousType() throws JavaModelException
    {
        IType methodOwner = _roleJavaElem;
        
        if ((methodOwner != null) && (methodOwner.exists()))
        {
            IMethod enclosingMethod = methodOwner.getMethod(METHOD_NAME, new String[0]);
            
            if ((enclosingMethod != null) && (enclosingMethod.exists()))
            {
                IType anonymousType = enclosingMethod.getType("",1);
                
                if ((anonymousType != null) && (anonymousType.exists()))
                {
                    return anonymousType;
                }
            }
        }
        return null;
    }        
}
