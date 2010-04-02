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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring.reorg;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.tests.refactoring.infra.MockClipboard;
import org.eclipse.jdt.ui.tests.refactoring.infra.MockWorkbenchSite;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.jdt.internal.ui.refactoring.reorg.CopyToClipboardAction;
import org.eclipse.jdt.internal.ui.refactoring.reorg.PasteAction;


public class OTPasteActionTest extends RefactoringTest
{

	private Clipboard _clipboard;
	private IPackageFragment	_packageQ;
	private static final String REFACTORING_PATH= "Paste/";

	public OTPasteActionTest(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new MySetup(new TestSuite(OTPasteActionTest.class));
	}

	protected String getRefactoringPath() {
		return REFACTORING_PATH;
	}

	protected void setUp() throws Exception {
		super.setUp();
		_clipboard= new MockClipboard(Display.getDefault());
		_packageQ = MySetup.getDefaultSourceFolder().createPackageFragment("q", true, null);
	}
	
	protected void tearDown() throws Exception 
	{
		if (_packageQ.exists())
        {
            _packageQ.delete(true, null);
        }
		_clipboard.dispose();
		super.tearDown();
	}

	private static Object[] merge(Object[] array1, Object[] array2) {
		Set elements= new HashSet(array1.length + array2.length);
		elements.addAll(Arrays.asList(array1));
		elements.addAll(Arrays.asList(array2));
		return elements.toArray();
	}
	
	private PasteAction verifyEnabled(IResource[] copySelectedResources, IJavaElement[] copySelectedJavaElements, IResource[] pasteSelectedResources, IJavaElement[] pasteSelectedJavaElements) throws JavaModelException {
		PasteAction pasteAction= new PasteAction(new MockWorkbenchSite(merge(pasteSelectedResources, pasteSelectedJavaElements)), _clipboard);
		CopyToClipboardAction copyToClipboardAction= new CopyToClipboardAction(new MockWorkbenchSite(merge(copySelectedResources, copySelectedJavaElements)), _clipboard);
		copyToClipboardAction.setAutoRepeatOnFailure(true);
		copyToClipboardAction.update(copyToClipboardAction.getSelection());
		assertTrue("copy not enabled", copyToClipboardAction.isEnabled());
		copyToClipboardAction.run();
		
		pasteAction.update(pasteAction.getSelection());
		assertTrue("paste should be enabled", pasteAction.isEnabled());
		return pasteAction;
	}

	private void compareContents(String cuName) throws JavaModelException, IOException {
		assertEqualLines(cuName, getFileContents(getOutputTestFileName(cuName)), getPackageP().getCompilationUnit(cuName + ".java").getSource());
	}
	
	private void delete(ICompilationUnit cu) throws Exception {
		try {
			performDummySearch();
			cu.delete(true, new NullProgressMonitor());
		} catch (JavaModelException e) {
			e.printStackTrace();
			//ingore and keep going
		}
	}

	public void testPasteTeamclassIntoTeamclass() throws Exception
	{
		ICompilationUnit cuTSource = createCUfromTestFile(getPackageP(),
			"TSource");
		ICompilationUnit cuTDest = createCUfromTestFile(getPackageP(),
			"TDest");

		try
		{
			IType teamTSource = cuTSource.getType("TSource");
			IType teamTDest = cuTDest.getType("TDest");
			
			assertTrue("TSource does not exist", teamTSource.exists());
			assertTrue("TDest does not exist", teamTDest.exists());

			IJavaElement[] copyJavaElements = { teamTSource };
			IResource[] copyResources = {};
			IJavaElement[] pasteJavaElements = { teamTDest };
			IResource[] pasteResources = {};
			PasteAction paste = verifyEnabled(copyResources,
					copyJavaElements, pasteResources, pasteJavaElements);
			paste.run((IStructuredSelection)paste.getSelection());
			
			compareContents("TSource");
			compareContents("TDest");
		}
		finally
		{
			delete(cuTSource);
		}
	}
	
	public void testPasteNestedTeamclass() throws Exception
	{
		ICompilationUnit cuTSource = createCUfromTestFile(getPackageP(),
				"TSource");
		ICompilationUnit cuTDest = createCUfromTestFile(getPackageP(), "TDest");

		try
		{
			IType teamTDest_TN = cuTSource.getType("TSource").getType("TN");
			IType teamTDest = cuTDest.getType("TDest");

			assertTrue("TSource.TN does not exist", teamTDest_TN.exists());
			assertTrue("TDest does not exist", teamTDest.exists());

			IJavaElement[] copyJavaElements = { teamTDest_TN };
			IResource[] copyResources = {};
			IJavaElement[] pasteJavaElements = { teamTDest };
			IResource[] pasteResources = {};
			PasteAction paste = verifyEnabled(copyResources,
					copyJavaElements, pasteResources, pasteJavaElements);
			paste.run((IStructuredSelection)paste.getSelection());
			compareContents("TSource");
			compareContents("TDest");
		}
		finally
		{
			delete(cuTSource);
			delete(cuTDest);
		}
	}

	public void testPasteRoleclass() throws Exception
	{
		ICompilationUnit cuTSource = createCUfromTestFile(getPackageP(),
				"TSource");
		ICompilationUnit cuTDest = createCUfromTestFile(getPackageP(), "TDest");

		try
		{
			IType roleR = cuTSource.getType("TSource").getType("R");
			IType teamTDest = cuTDest.getType("TDest");

			assertTrue("R does not exist", roleR.exists());
			assertTrue("TDest does not exist", teamTDest.exists());

			IJavaElement[] copyJavaElements = { roleR };
			IResource[] copyResources = {};
			IJavaElement[] pasteJavaElements = { teamTDest };
			IResource[] pasteResources = {};
			PasteAction paste = verifyEnabled(copyResources,
					copyJavaElements, pasteResources, pasteJavaElements);
			paste.run((IStructuredSelection)paste.getSelection());
			compareContents("TSource");
			compareContents("TDest");
		}
		finally
		{
			delete(cuTSource);
			delete(cuTDest);
		}
	}

	public void testPasteCalloutMapping() throws Exception
	{
		ICompilationUnit cuTSource= createCUfromTestFile(getPackageP(), "TSource");
		ICompilationUnit cuTDest= createCUfromTestFile(getPackageP(), "TDest");
		
		try 
		{
			IRoleType roleSrc = (IRoleType)OTModelManager.getOTElement(cuTSource.getType("TSource").getType("R"));
			IMethodMapping calloutMapping = roleSrc.getMethodMappings()[0];
			IType roleTDest_R = cuTDest.getType("TDest").getType("R");
	
			assertTrue("callout does not exist", calloutMapping.exists());
			assertTrue("R does not exist", roleTDest_R.exists());
	
			IJavaElement[] copyJavaElements= {calloutMapping};
			IResource[] copyResources= {};
			IJavaElement[] pasteJavaElements= {roleTDest_R};
			IResource[] pasteResources= {};
			PasteAction paste= verifyEnabled(copyResources, copyJavaElements, pasteResources, pasteJavaElements);
			paste.run((IStructuredSelection)paste.getSelection());
			compareContents("TSource");
			compareContents("TDest");
		} 
		finally
		{
			delete(cuTSource);
			delete(cuTDest);
		}
	}

	public void testPasteCalloutMappingParam() throws Exception
	{
		ICompilationUnit cuTSource= createCUfromTestFile(getPackageP(), "TSource");
		ICompilationUnit cuTDest= createCUfromTestFile(getPackageP(), "TDest");
		
		try 
		{
			IRoleType roleSrc = (IRoleType)OTModelManager.getOTElement(cuTSource.getType("TSource").getType("R"));
			IMethodMapping calloutMapping = roleSrc.getMethodMappings()[0];
			IType roleTDest_R = cuTDest.getType("TDest").getType("R");
	
			assertTrue("callout does not exist", calloutMapping.exists());
			assertTrue("R does not exist", roleTDest_R.exists());
	
			IJavaElement[] copyJavaElements= {calloutMapping};
			IResource[] copyResources= {};
			IJavaElement[] pasteJavaElements= {roleTDest_R};
			IResource[] pasteResources= {};
			PasteAction paste= verifyEnabled(copyResources, copyJavaElements, pasteResources, pasteJavaElements);
			paste.run((IStructuredSelection)paste.getSelection());
			compareContents("TSource");
			compareContents("TDest");
		} 
		finally
		{
			delete(cuTSource);
			delete(cuTDest);
		}
	}

	public void testPasteCallinMappingParam() throws Exception
	{
		ICompilationUnit cuTSource= createCUfromTestFile(getPackageP(), "TSource");
		ICompilationUnit cuTDest= createCUfromTestFile(getPackageP(), "TDest");
		
		try 
		{
			IRoleType roleSrc = (IRoleType)OTModelManager.getOTElement(cuTSource.getType("TSource").getType("R"));
			IMethodMapping calloutMapping = roleSrc.getMethodMappings()[0];
			IType roleTDest_R = cuTDest.getType("TDest").getType("R");
	
			assertTrue("callout does not exist", calloutMapping.exists());
			assertTrue("R does not exist", roleTDest_R.exists());
	
			IJavaElement[] copyJavaElements= {calloutMapping};
			IResource[] copyResources= {};
			IJavaElement[] pasteJavaElements= {roleTDest_R};
			IResource[] pasteResources= {};
			PasteAction paste= verifyEnabled(copyResources, copyJavaElements, pasteResources, pasteJavaElements);
			paste.run((IStructuredSelection)paste.getSelection());
			compareContents("TSource");
			compareContents("TDest");
		} 
		finally
		{
			delete(cuTSource);
			delete(cuTDest);
		}
	}

	public void testPasteCalloutToFieldMapping() throws Exception
	{
		ICompilationUnit cuTSource = createCUfromTestFile(getPackageP(),
				"TSource");
		ICompilationUnit cuTDest = createCUfromTestFile(getPackageP(), "TDest");

		try
		{
			IRoleType roleSrc = (IRoleType)OTModelManager
					.getOTElement(cuTSource.getType("TSource").getType("R"));
			IMethodMapping calloutToFieldMapping = roleSrc.getMethodMappings()[0];
			IType roleTDest_R = cuTDest.getType("TDest").getType("R");

			assertTrue("callout does not exist", calloutToFieldMapping.exists());
			assertTrue("R does not exist", roleTDest_R.exists());

			IJavaElement[] copyJavaElements = { calloutToFieldMapping };
			IResource[] copyResources = {};
			IJavaElement[] pasteJavaElements = { roleTDest_R };
			IResource[] pasteResources = {};
			PasteAction paste = verifyEnabled(copyResources,
					copyJavaElements, pasteResources, pasteJavaElements);
			paste.run((IStructuredSelection)paste.getSelection());
			compareContents("TSource");
			compareContents("TDest");
		}
		finally
		{
			delete(cuTSource);
			delete(cuTDest);
		}
	}
	
	public void testPasteCallinMapping() throws Exception
	{
		ICompilationUnit cuTSource = createCUfromTestFile(getPackageP(),
				"TSource");
		ICompilationUnit cuTDest = createCUfromTestFile(getPackageP(), "TDest");

		try
		{
			IRoleType roleSrc = (IRoleType)OTModelManager
					.getOTElement(cuTSource.getType("TSource").getType("R"));
			IMethodMapping callinMapping = roleSrc.getMethodMappings()[0];
			IType roleTDest_R = cuTDest.getType("TDest").getType("R");

			assertTrue("callout does not exist", callinMapping.exists());
			assertTrue("R does not exist", roleTDest_R.exists());

			IJavaElement[] copyJavaElements = { callinMapping };
			IResource[] copyResources = {};
			IJavaElement[] pasteJavaElements = { roleTDest_R };
			IResource[] pasteResources = {};
			PasteAction paste = verifyEnabled(copyResources,
					copyJavaElements, pasteResources, pasteJavaElements);
			paste.run((IStructuredSelection)paste.getSelection());
			compareContents("TSource");
			compareContents("TDest");
		}
		finally
		{
			delete(cuTSource);
			delete(cuTDest);
		}
	}
	
}
