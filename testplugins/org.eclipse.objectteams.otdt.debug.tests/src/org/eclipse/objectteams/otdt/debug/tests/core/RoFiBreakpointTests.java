/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoFiBreakpointTests.java 23485 2010-02-05 22:00:22Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.tests.core;

import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;
import org.eclipse.objectteams.otdt.debug.tests.AbstractOTDTDebugTest;

public class RoFiBreakpointTests extends AbstractOTDTDebugTest {

	public RoFiBreakpointTests(String name) {
		super(name);
	}

	
	
	/**
	 * Test breaking in a team method
	 * 
	 * @throws Exception
	 */
	public void testBreakInTeamMethod1() throws Exception {
		String typeName = "rofitests.RoFiTeam";
		createLineBreakpoint(6, typeName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaReferenceType type = ((IJavaStackFrame)thread.getTopStackFrame()).getReferenceType();
			assertEquals("Stopped in wrong type", typeName, type.getName());
			String methodName = ((IJavaStackFrame)thread.getTopStackFrame()).getMethodName();
			assertEquals("Stopped in wrong method", "doit", methodName);

			assertEquals("Wrong source name", "RoFiTeam.java", ((IJavaStackFrame)thread.getTopStackFrame()).getSourceName(ISMAPConstants.OTJ_STRATUM_NAME));
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	
	/**
	 * Test breaking in a method of a role file.
	 * 
	 * @throws Exception
	 */
	public void testBreakInRoFiMethod1() throws Exception {
		String typeName = "rofitests.RoFiTeam";
		String roleName = "rofitests.RoFiTeam.RoFiRole";
		String roleBinaryName = "rofitests.RoFiTeam$__OT__RoFiRole";
		createLineBreakpoint(5, roleName);		
		
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaReferenceType type = ((IJavaStackFrame)thread.getTopStackFrame()).getReferenceType();
			assertEquals("Stopped in wrong type", roleBinaryName, type.getName());
			String methodName = ((IJavaStackFrame)thread.getTopStackFrame()).getMethodName();
			assertEquals("Stopped in wrong method", "doRolish", methodName);
			
			
			assertEquals("Wrong source name", "RoFiRole.java", ((IJavaStackFrame)thread.getTopStackFrame()).getSourceName(ISMAPConstants.OTJ_STRATUM_NAME));
//			IPackageFragmentRoot root = getPackageFragmentRoot(getJavaProject(), "src");
//			IJavaSourceLocation location = new PackageFragmentRootSourceLocation(root);
//			String[] javaSourcePaths = type.getSourcePaths("java");
//			String[] otjSourcePaths = type.getSourcePaths(ISMAPConstants.OTJ_STRATUM_NAME);
//			System.out.println("j:"+javaSourcePaths.length+", otj:"+otjSourcePaths.length);
//			
//			ICompilationUnit cu = (ICompilationUnit) location.findSourceElement(otjSourcePaths[0]);
//			assertNotNull("CU should not be null", cu);
//			assertTrue("CU should exist", cu.exists());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
}
