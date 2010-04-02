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
 * $Id: CalloutMappingTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

/**
 * $Id: CalloutMappingTest.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * testcase:
 * a team class with a bound role class and various callout binding declarations
 */
public class CalloutMappingTest extends FileBasedModelTest
{
    
    private IType _teamJavaElem;
    private IType _roleJavaElem;
    
    public CalloutMappingTest(String name)
    {
        super(name);
    }
    static boolean ALL_TESTS=true;
    public static Test suite()
    {
        if (ALL_TESTS) return new Suite(CalloutMappingTest.class);
        junit.framework.TestSuite suite = new Suite(CalloutMappingTest.class.getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("CalloutMapping");
        super.setUpSuite();
    }
    
    public void setUp() throws Exception
    {
    		super.setUp();
    	
        try
        {
            ICompilationUnit teamUnit = getCompilationUnit(
                    "CalloutMapping", 
                    "src",
                    "teampkg", 
                    "SampleTeam" + ".java");

            _teamJavaElem = teamUnit.getType("SampleTeam");

            _roleJavaElem = _teamJavaElem.getType("SampleRole");

        }
        catch (JavaModelException ex)
        {
            ex.printStackTrace();
        }
    }    
    
    private IRoleType getRoleOTElem()
    {
        if ((_roleJavaElem != null) && (_roleJavaElem.exists()))
        {
            IOTType roleOTElem = OTModelManager.getOTElement(_roleJavaElem);
            
            if ((roleOTElem != null) && (roleOTElem instanceof IRoleType))
            {
                return ((IRoleType) roleOTElem);
            }
        }
        return null;
    }
    
    public void testExistenceOfMappings() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        
        IMethodMapping[] allMethodMappings = roleOTElem.getMethodMappings();
        assertEquals(4, allMethodMappings.length);
        
        IMethodMapping[] calloutMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLOUTS);
        assertEquals(4, calloutMethodMappings.length);
        
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        assertEquals(0, callinMethodMappings.length);
    }

    public void testMapping1FromMemento() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] calloutMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLOUTS);
        
        IMethodMapping mapping = calloutMethodMappings[0];
        assertNotNull(mapping);
        
        String token = mapping.getHandleIdentifier();
        
        SourceType sourceType = (SourceType)roleOTElem.getCorrespondingJavaElement();
		IJavaElement fromMemento = JavaCore.create(token, sourceType.getCompilationUnit().getOwner());
        
        assertEquals("Elements should be equal", mapping, fromMemento);
        
        assertTrue("Element should exist", fromMemento.exists());
        
        assertTrue("Mapping should have signature", ((IMethodMapping)fromMemento).hasSignature());
    }

    public void testMapping2FromMemento() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] calloutMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLOUTS);
        
        IMethodMapping mapping = calloutMethodMappings[1];
        assertNotNull(mapping);
        
        String token = mapping.getHandleIdentifier();
        
        SourceType sourceType = (SourceType)roleOTElem.getCorrespondingJavaElement();
		IJavaElement fromMemento = JavaCore.create(token, sourceType.getCompilationUnit().getOwner());
        
        assertEquals("Elements should be equal", mapping, fromMemento);
        
        assertTrue("Element should exist", fromMemento.exists());
        
        assertFalse("Mapping should not have signature", ((IMethodMapping)fromMemento).hasSignature());
    }

    public void testMapping3FromMemento() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] calloutMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLOUTS);
        
        IMethodMapping mapping = calloutMethodMappings[2];
        assertNotNull(mapping);
        
        String token = mapping.getHandleIdentifier();
        
        SourceType sourceType = (SourceType)roleOTElem.getCorrespondingJavaElement();
		IJavaElement fromMemento = JavaCore.create(token, sourceType.getCompilationUnit().getOwner());
        
        assertEquals("Elements should be equal", mapping, fromMemento);
        
        assertTrue("Element should exist", fromMemento.exists());
        
        ICalloutMapping mappingFromMemento = (ICalloutMapping)fromMemento;
		assertTrue("Mapping should have signature", mappingFromMemento.hasSignature());
        
        assertTrue("Expecting 1 role parameter", mappingFromMemento.getRoleMethodHandle().getArgumentTypes().length == 1);
        assertTrue("Expecting 2 base parameters", mappingFromMemento.getBaseMethodHandle().getArgumentTypes().length == 2);
    }

    public void testMapping4FromMemento() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] calloutMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLOUTS);
        
        IMethodMapping mapping = calloutMethodMappings[3];
        assertNotNull(mapping);
        
        String token = mapping.getHandleIdentifier();
        
        SourceType sourceType = (SourceType)roleOTElem.getCorrespondingJavaElement();
		IJavaElement fromMemento = JavaCore.create(token, sourceType.getCompilationUnit().getOwner());
        
        assertEquals("Elements should be equal", mapping, fromMemento);
        
        assertTrue("Element should exist", fromMemento.exists());
        
        assertTrue("Expecting callout to field", fromMemento instanceof ICalloutToFieldMapping);
        ICalloutToFieldMapping mappingFromMemento = (ICalloutToFieldMapping)fromMemento;
		assertTrue("Mapping should have signature", mappingFromMemento.hasSignature());
        
        assertFalse("Expecting no setter", mappingFromMemento.getBaseFieldHandle().isSetter());
    }
    
}    
