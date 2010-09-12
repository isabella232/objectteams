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
 * $Id: CallinMappingTest.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

/**
 * $Id: CallinMappingTest.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * testcase:
 * a team class with a bound role class and various callin binding declarations
 * callin bindings:
 *     - both the base method and the role method exist
 *     - the role method does not exist
 *     - the base method does not exist
 *     - neither role method nor base method exist
 *     - multiple (three) base methods are mapped to the same role method
 *       whereas two of those base methods exist and one of those does not exist
 *     - one base method is mapped to multiple (2) role methods 
 */
public class CallinMappingTest extends FileBasedModelTest
{
    
    private IType _teamJavaElem;
    private IType _roleJavaElem;
    private IType _baseJavaElem;
    
    public CallinMappingTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(CallinMappingTest.class);
        }
        junit.framework.TestSuite suite = new Suite(CallinMappingTest.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("CallinMapping");
        super.setUpSuite();
    }
    
    public void setUp() throws Exception
    {
    		super.setUp();
    	
        try
        {
            ICompilationUnit teamUnit = getCompilationUnit(
                    "CallinMapping", 
                    "src",
                    "teampkg", 
                    "SampleTeam" + ".java");

            _teamJavaElem = teamUnit.getType("SampleTeam");

            _roleJavaElem = _teamJavaElem.getType("SampleRole");
            
            ICompilationUnit baseUnit = getCompilationUnit(
                    "CallinMapping", 
                    "src",
                    "basepkg", 
                    "SampleBase" + ".java");

            _baseJavaElem = baseUnit.getType("SampleBase");           
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

    public void testExistenceOfMethods() throws JavaModelException
    {
        assertNotNull(_baseJavaElem);
        assertTrue(_baseJavaElem.exists());
        
        IMethod[] baseMethods = _baseJavaElem.getMethods();
        assertNotNull(baseMethods);
        assertEquals(6, baseMethods.length);
        
        assertTrue((_baseJavaElem.getMethod("baseMethod1", new String[0])).exists());        
        assertTrue((_baseJavaElem.getMethod("baseMethod2", new String[0])).exists());
        assertTrue((_baseJavaElem.getMethod("baseMethod3", new String[0])).exists());
        assertTrue((_baseJavaElem.getMethod("baseMethod4", new String[0])).exists());
        
        assertFalse((_baseJavaElem.getMethod("baseMethodA", new String[0])).exists());        
        assertFalse((_baseJavaElem.getMethod("baseMethodB", new String[0])).exists());
        assertFalse((_baseJavaElem.getMethod("baseMethodC", new String[0])).exists());

        
        IMethod[] roleMethods = _roleJavaElem.getMethods();
        assertNotNull(roleMethods);
        assertEquals(6, roleMethods.length);
        
        assertTrue((_roleJavaElem.getMethod("roleMethod1", new String[0])).exists());
        assertTrue((_roleJavaElem.getMethod("roleMethod2", new String[0])).exists());
        assertTrue((_roleJavaElem.getMethod("roleMethod3", new String[0])).exists());
        assertTrue((_roleJavaElem.getMethod("roleMethod4", new String[0])).exists());
        
        assertFalse((_roleJavaElem.getMethod("roleMethodA", new String[0])).exists());
        assertFalse((_roleJavaElem.getMethod("roleMethodB", new String[0])).exists());        
    }
    
    
    public void testExistenceOfMappings() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        
        IMethodMapping[] allMethodMappings = roleOTElem.getMethodMappings();
        assertEquals(8, allMethodMappings.length);
        
        IMethodMapping[] calloutMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLOUTS);
        assertEquals(0, calloutMethodMappings.length);
        
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        assertEquals(8, callinMethodMappings.length);
    }
    
    public void testMapping1() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        IMethodMapping mapping = callinMethodMappings[0];
        assertNotNull(mapping);
        
        assertEquals("roleMethod1 <- baseMethod1", mapping.getElementName());
        assertEquals(IOTJavaElement.CALLIN_MAPPING, mapping.getMappingKind());
        
        IMethod roleMethod = mapping.getRoleMethod();
        assertNotNull(roleMethod);
        assertEquals("roleMethod1", roleMethod.getElementName());
        
        ICallinMapping callinMapping = (ICallinMapping) mapping;
        IMethod[] boundBaseMethods = callinMapping.getBoundBaseMethods();
        assertNotNull(boundBaseMethods);
        assertEquals(1, boundBaseMethods.length);
        assertEquals("baseMethod1", boundBaseMethods[0].getElementName());
    }

    public void testMapping1FromMemento() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        IMethodMapping mapping = callinMethodMappings[0];
        assertNotNull(mapping);
        
        String token = mapping.getHandleIdentifier();
        
        SourceType sourceType = (SourceType)roleOTElem.getCorrespondingJavaElement();
		IJavaElement fromMemento = JavaCore.create(token, sourceType.getCompilationUnit().getOwner());
        
        assertEquals("Elements should be equal", mapping, fromMemento);
        
        assertTrue("Element should exist", fromMemento.exists());
        
        assertFalse("Mapping should not have signature", ((IMethodMapping)fromMemento).hasSignature());
    }
    
    public void testMapping2() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        IMethodMapping mapping = callinMethodMappings[1];
        assertNotNull(mapping);
 
        assertEquals("roleMethodA <- baseMethod2", mapping.getElementName());
        assertEquals(IOTJavaElement.CALLIN_MAPPING, mapping.getMappingKind());
        
        IMethod roleMethod = mapping.getRoleMethod();
        assertNull(roleMethod);
        
        ICallinMapping callinMapping = (ICallinMapping) mapping;
        IMethod[] boundBaseMethods = callinMapping.getBoundBaseMethods();
        assertNotNull(boundBaseMethods);
        assertEquals(1, boundBaseMethods.length);
        assertEquals("baseMethod2", boundBaseMethods[0].getElementName());
    }
 
    public void testMapping3() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        IMethodMapping mapping = callinMethodMappings[2];
        assertNotNull(mapping);
        
        assertEquals("roleMethod2 <- baseMethodA", mapping.getElementName());
        assertEquals(IOTJavaElement.CALLIN_MAPPING, mapping.getMappingKind());
        
        IMethod roleMethod = mapping.getRoleMethod();
        assertNotNull(roleMethod);
        assertEquals("roleMethod2", roleMethod.getElementName());
        
        ICallinMapping callinMapping = (ICallinMapping) mapping;
        IMethod[] boundBaseMethods = callinMapping.getBoundBaseMethods();
        assertEquals(0, boundBaseMethods.length);
    }
    
    public void testMapping4() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        IMethodMapping mapping = callinMethodMappings[3];
        assertNotNull(mapping);
        
        assertEquals("roleMethodB <- baseMethodB", mapping.getElementName());
        assertEquals(IOTJavaElement.CALLIN_MAPPING, mapping.getMappingKind());
        
        IMethod roleMethod = mapping.getRoleMethod();
        assertNull(roleMethod);
  
        ICallinMapping callinMapping = (ICallinMapping) mapping;
        IMethod[] boundBaseMethods = callinMapping.getBoundBaseMethods();
        assertEquals(0, boundBaseMethods.length);
    }
    
    public void testMapping5() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        IMethodMapping mapping = callinMethodMappings[4];
        assertNotNull(mapping);
        
        assertEquals("roleMethod3 <- {baseMethod3,baseMethod4,baseMethodC}", mapping.getElementName());
        assertEquals(IOTJavaElement.CALLIN_MAPPING, mapping.getMappingKind());
        
        IMethod roleMethod = mapping.getRoleMethod();
        assertNotNull(roleMethod);
        assertEquals("roleMethod3", roleMethod.getElementName());
        
        ICallinMapping callinMapping = (ICallinMapping) mapping;
        IMethod[] boundBaseMethods = callinMapping.getBoundBaseMethods();
        assertNotNull(boundBaseMethods);
        assertEquals(2, boundBaseMethods.length);
        assertEquals("baseMethod3", boundBaseMethods[0].getElementName());
        assertEquals("baseMethod4", boundBaseMethods[1].getElementName());
    }
    
    public void testMapping5FromMemento() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        IMethodMapping mapping = callinMethodMappings[4];
        assertNotNull(mapping);
        
        assertNotNull(mapping);
        
        String token = mapping.getHandleIdentifier();
        
        SourceType sourceType = (SourceType)roleOTElem.getCorrespondingJavaElement();
		IJavaElement fromMemento = JavaCore.create(token, sourceType.getCompilationUnit().getOwner());
        
        assertEquals("Elements should be equal", mapping, fromMemento);
        
        assertTrue("Element should exist", fromMemento.exists());

        ICallinMapping callinMapping = (ICallinMapping) fromMemento;
        IMethod[] boundBaseMethods = callinMapping.getBoundBaseMethods();
        assertNotNull(boundBaseMethods);
        assertEquals(2, boundBaseMethods.length);
        assertEquals("baseMethod3", boundBaseMethods[0].getElementName());
        assertEquals("baseMethod4", boundBaseMethods[1].getElementName());
    }
    
    public void testMapping6() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        IMethodMapping mapping = callinMethodMappings[5];
        assertNotNull(mapping);
        
        assertEquals("roleMethod4 <- baseMethod4", mapping.getElementName());
        assertEquals(IOTJavaElement.CALLIN_MAPPING, mapping.getMappingKind());
        
        IMethod roleMethod = mapping.getRoleMethod();
        assertNotNull(roleMethod);
        assertEquals("roleMethod4", roleMethod.getElementName());
        
        ICallinMapping callinMapping = (ICallinMapping) mapping;
        IMethod[] boundBaseMethods = callinMapping.getBoundBaseMethods();
        assertNotNull(boundBaseMethods);
        assertEquals(1, boundBaseMethods.length);
        assertEquals("baseMethod4", boundBaseMethods[0].getElementName());
    }
    
    public void testMapping7() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        IMethodMapping mapping = callinMethodMappings[6];
        assertNotNull(mapping);
        
        assertEquals("roleMethod5(int) <- baseMethod5(int)", mapping.getElementName());
        assertEquals(IOTJavaElement.CALLIN_MAPPING, mapping.getMappingKind());
        
        IMethod roleMethod = mapping.getRoleMethod();
        assertNotNull(roleMethod);
        assertEquals("roleMethod5", roleMethod.getElementName());
        
        ICallinMapping callinMapping = (ICallinMapping) mapping;
        IMethod[] boundBaseMethods = callinMapping.getBoundBaseMethods();
        assertNotNull(boundBaseMethods);
        assertEquals(1, boundBaseMethods.length);
        assertEquals("baseMethod5", boundBaseMethods[0].getElementName());
    }
    
    public void testMapping7FromMemento() throws JavaModelException
    {
    	IRoleType roleOTElem = getRoleOTElem();
    	assertNotNull(roleOTElem);
    	IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
    	
    	IMethodMapping mapping = callinMethodMappings[6];
    	assertNotNull(mapping);
    	
    	String token = mapping.getHandleIdentifier();
    	
    	SourceType sourceType = (SourceType)roleOTElem.getCorrespondingJavaElement();
    	IJavaElement fromMemento = JavaCore.create(token, sourceType.getCompilationUnit().getOwner());
    	
    	assertEquals("Elements should be equal", mapping, fromMemento);
    	
    	assertTrue("Element should exist", fromMemento.exists());
    	
    	assertTrue("Mapping should have signature", ((IMethodMapping)fromMemento).hasSignature());
    }
    
    public void testMapping8() throws JavaModelException 
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        IMethodMapping mapping = callinMethodMappings[7];
        assertNotNull(mapping);
        
        assertEquals("roleMethod6() <- baseMethod6() <T>", mapping.getElementName());
        assertEquals(IOTJavaElement.CALLIN_MAPPING, mapping.getMappingKind());
        assertTrue(mapping.getRoleMethod().getTypeParameters().length == 1);
        assertEquals(new String(mapping.getRoleMethodHandle().getTypeParameterNames()[0]), "T");
        assertEquals(CharOperation.toString(mapping.getRoleMethodHandle().getTypeParameterBounds()[0]), "Object");
        
        ICallinMapping callinMapping = (ICallinMapping) mapping;
        IMethod[] boundBaseMethods = callinMapping.getBoundBaseMethods();
        assertNotNull(boundBaseMethods);
        assertEquals(1, boundBaseMethods.length);
        assertEquals("baseMethod6", boundBaseMethods[0].getElementName());
        assertTrue(callinMapping.hasCovariantReturn());
    }
    
    public void testMappingModifier1() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[0];        
        assertEquals(ICallinMapping.KIND_AFTER, callinMapping.getCallinKind());
    }
    
    public void testMappingModifier2() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
 
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[1];        
        assertEquals(ICallinMapping.KIND_BEFORE, callinMapping.getCallinKind());
    }
    
    public void testMappingModifier3() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[2];        
        assertEquals(ICallinMapping.KIND_REPLACE, callinMapping.getCallinKind());
    } 
    
    public void testMappingModifier4() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[3];        
        assertEquals(ICallinMapping.KIND_AFTER, callinMapping.getCallinKind());
    }
    
    public void testMappingModifier5() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
 
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[4];        
        assertEquals(ICallinMapping.KIND_BEFORE, callinMapping.getCallinKind());
    }
    
    public void testMappingModifier6() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[5];        
        assertEquals(ICallinMapping.KIND_REPLACE, callinMapping.getCallinKind());
    }
    
    public void testMappingModifier7() throws JavaModelException
    {
        IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[6];        
        assertEquals(ICallinMapping.KIND_AFTER, callinMapping.getCallinKind());
    }
    
    public void testSource1() throws JavaModelException
    {
    	IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[0];
        
        assertEquals("roleMethod1 <- after baseMethod1;", callinMapping.getSource());
    }
    
    public void testSource2() throws JavaModelException
    {
    	IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[1];
        
        assertEquals("roleMethodA <- before baseMethod2;", callinMapping.getSource());
    }
    
    public void testSource3() throws JavaModelException
    {
    	IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[2];
        
        assertEquals("roleMethod2 <- replace baseMethodA;", callinMapping.getSource());
    }
    
    public void testSource4() throws JavaModelException
    {
    	IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[3];
        
        assertEquals("roleMethodB <- after baseMethodB;", callinMapping.getSource());
    }
    
    public void testSource5() throws JavaModelException
    {
    	IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[4];
        
        assertEquals("roleMethod3 <- before baseMethod3, baseMethod4, baseMethodC;", callinMapping.getSource());
    }

    public void testSource6() throws JavaModelException
    {
    	IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[5];
        
        assertEquals("roleMethod4 <- replace baseMethod4;", callinMapping.getSource());
    }
        
    public void testSource7() throws JavaModelException
    {
    	IRoleType roleOTElem = getRoleOTElem();
        assertNotNull(roleOTElem);
        IMethodMapping[] callinMethodMappings = roleOTElem.getMethodMappings(IRoleType.CALLINS);
        
        ICallinMapping callinMapping = (ICallinMapping) callinMethodMappings[6];
        
        assertEquals("void roleMethod5(int ir)  <- after void baseMethod5(int ib) with { ir <- ib }", callinMapping.getSource());
    }
}    
