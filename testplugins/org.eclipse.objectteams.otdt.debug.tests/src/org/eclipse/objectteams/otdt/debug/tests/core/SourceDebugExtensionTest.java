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
 * $Id: SourceDebugExtensionTest.java 23492 2010-02-05 22:57:56Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.tests.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.objectteams.otdt.debug.tests.AbstractOTDTDebugTest;
import org.eclipse.objectteams.otdt.debug.tests.ClassAttributeReader;

/**
 * @author ike
 *
 * Tests the availability and correctness of SDE in a given classfile.
 * $Id: SourceDebugExtensionTest.java 23492 2010-02-05 22:57:56Z stephan $
 */
public class SourceDebugExtensionTest extends AbstractOTDTDebugTest
{

    public SourceDebugExtensionTest(String name)
    {
        super(name);
    }

    public void testSDEAbsenceForSimpleClass() throws Exception
    {
        ClassAttributeReader reader = getAttributeReader("SimpleClass");
        assertNotNull("An error occurs while creating ClassAttributeReader", reader);
        assertFalse("SDE should be absent.", reader.isSDEAvailable());
    }

    public void testSDEAbsenceForSimpleTeam() throws Exception
    {
        ClassAttributeReader reader = getAttributeReader("SimpleTeam");
        assertNotNull("An error occurs while creating ClassAttributeReader", reader);
        assertFalse("SDE should be absent.", reader.isSDEAvailable());
    }
    
    public void testSDEAbsenceForSimpleTeam2() throws Exception
    {
        ClassAttributeReader reader = getAttributeReader("SimpleTeam2");
        assertNotNull("An error occurs while creating ClassAttributeReader", reader);
        assertFalse("SDE should be absent.", reader.isSDEAvailable());
    }
    
    public void testSDEExistenceForSimpleSDEClass() throws Exception
    {
        ClassAttributeReader reader = getAttributeReader("SimpleSDEClass");
        assertNotNull("An error occurs while creating ClassAttributeReader", reader);
        assertTrue("SDE should exist.", reader.isSDEAvailable());
    }
    
    private ClassAttributeReader getAttributeReader(String classname) throws Exception
    {
        IPath classFilePath = getAbsoluteOSClassFilePath(classname);
        ClassAttributeReader classReader = new ClassAttributeReader(classFilePath);
        if (classReader.getError() != null)
            return null;
        
        return classReader;
    }

}
