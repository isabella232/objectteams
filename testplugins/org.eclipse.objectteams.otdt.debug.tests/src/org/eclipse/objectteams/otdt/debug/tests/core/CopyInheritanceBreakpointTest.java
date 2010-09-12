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
 * $Id: CopyInheritanceBreakpointTest.java 23492 2010-02-05 22:57:56Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.tests.core;

import junit.framework.AssertionFailedError;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;
import org.eclipse.objectteams.otdt.debug.tests.AbstractOTDTDebugTest;
import org.eclipse.objectteams.otdt.debug.tests.Problem;

public class CopyInheritanceBreakpointTest extends AbstractOTDTDebugTest {

	public CopyInheritanceBreakpointTest(String name) {
		super(name);
	}
	
	public void testBreakInPhantomRoleMethod() throws Exception {
		// set breakpoint in this type:
		String breakTeamName  = "copyinheritancetests.SuperTeam";
		// launch this type:
		String launchTypeName = "copyinheritancetests.SubTeam";
		// expect this type at the top stack frame:
		String breakTypeName  = "copyinheritancetests.SubTeam$__OT__R";
		// expect this source file to be found via SMAP:
		String sourceFile     = "SuperTeam.java";
		
		createLineBreakpoint(6, breakTeamName); // sysout within roleMethod1
		
		IJavaThread thread= null;
		try {
			waitForBuild();
			expectNoErrors("DebugTests");

			thread= launchToBreakpoint(launchTypeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			IJavaReferenceType type = ((IJavaStackFrame)thread.getTopStackFrame()).getReferenceType();
			assertEquals("Stopped in wrong type", breakTypeName, type.getName());
			
			String methodName = ((IJavaStackFrame)thread.getTopStackFrame()).getMethodName();
			assertEquals("Stopped in wrong method", "roleMethod1", methodName);
			
			String sourceName = ((IJavaStackFrame)thread.getTopStackFrame()).getSourceName(ISMAPConstants.OTJ_STRATUM_NAME);
			assertEquals("Lookup gave wrong source name", sourceFile, sourceName);
			
		} catch (AssertionFailedError fail) {
			if (thread != null) {
				IConsole console = DebugUITools.getConsole(thread.getLaunch().getProcesses()[0]);
				if (console instanceof TextConsole) {
					TextConsole textConsole = (TextConsole)console;
					String string = textConsole.getDocument().get();
					System.out.println("Console output follows:");
					System.out.println(string);
				}
				System.out.println();
			}
			throw fail;
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}
	
	public void testBreakInRoleMethod() throws Exception {
		// set breakpoint in this type:
		String breakTeamName  = "copyinheritancetests.SuperTeam";
		// launch this type:
		String launchTypeName = "copyinheritancetests.SubTeam2";
		// expect this type at the top stack frame:
		String breakTypeName  = "copyinheritancetests.SubTeam2$__OT__R";
		// expect this source file to be found via SMAP:
		String sourceFile     = "SuperTeam.java";
		
		createLineBreakpoint(6, breakTeamName); // sysout with roleMethod1
		
		IJavaThread thread= null;
		try {
			waitForBuild();
			expectNoErrors("DebugTests");
			
			thread= launchToBreakpoint(launchTypeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			IJavaReferenceType type = ((IJavaStackFrame)thread.getTopStackFrame()).getReferenceType();
			assertEquals("Stopped in wrong type", breakTypeName, type.getName());
			
			String methodName = ((IJavaStackFrame)thread.getTopStackFrame()).getMethodName();
			assertEquals("Stopped in wrong method", "roleMethod1", methodName);
			
			String sourceName = ((IJavaStackFrame)thread.getTopStackFrame()).getSourceName(ISMAPConstants.OTJ_STRATUM_NAME);
			assertEquals("Lookup gave wrong source name", sourceFile, sourceName);
			
		} catch (AssertionFailedError fail) {
			if (thread != null) {
				IConsole console = DebugUITools.getConsole(thread.getLaunch().getProcesses()[0]);
				if (console instanceof TextConsole) {
					TextConsole textConsole = (TextConsole)console;
					String string = textConsole.getDocument().get();
					System.out.println("Console output follows:");
					System.out.println(string);
				}
				System.out.println();
			}
			throw fail;
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}

	public void testBreakInRoleFileMethod() throws Exception {
		// set breakpoint in this type:
		String breakRoleName  = "copyinheritancetests.SuperTeam3.R";
		// launch this type:
		String launchTypeName = "copyinheritancetests.SubTeam3";
		// expect this type at the top stack frame:
		String breakTypeName  = "copyinheritancetests.SubTeam3$__OT__R";
		// expect this source file to be found via SMAP:
		String sourceFile     = "R.java";
		
		createLineBreakpoint(4, breakRoleName); // sysout within roleMethod1
		
		IJavaThread thread= null;
		try {
			waitForBuild();
			expectNoErrors("DebugTests");

			thread= launchToBreakpoint(launchTypeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			
			IJavaReferenceType type = ((IJavaStackFrame)thread.getTopStackFrame()).getReferenceType();
			assertEquals("Stopped in wrong type", breakTypeName, type.getName());
			
			String methodName = ((IJavaStackFrame)thread.getTopStackFrame()).getMethodName();
			assertEquals("Stopped in wrong method", "roleMethod1", methodName);
			
			String sourceName = ((IJavaStackFrame)thread.getTopStackFrame()).getSourceName(ISMAPConstants.OTJ_STRATUM_NAME);
			assertEquals("Lookup gave wrong source name", sourceFile, sourceName);
			
		} catch (AssertionFailedError fail) {
			if (thread != null) {
				IConsole console = DebugUITools.getConsole(thread.getLaunch().getProcesses()[0]);
				if (console instanceof TextConsole) {
					TextConsole textConsole = (TextConsole)console;
					String string = textConsole.getDocument().get();
					System.out.println("Console output follows:");
					System.out.println(string);
				}
				System.out.println();
			}
			throw fail;
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}		
	}

	void expectNoErrors(String projectName) {
		Problem[] problems = getProblemsFor(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
		if (problems != null && problems.length > 0) {
			for (Problem problem : problems) {
				System.out.println(problem.toString());
			}
			fail("Should not have compile errors");
		}
	}
}
