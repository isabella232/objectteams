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
package org.eclipse.objectteams.otdt.ui.tests.refactoring.extractmethod;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractTempRefactoring;
import org.eclipse.jdt.ui.tests.refactoring.infra.TextRangeUtil;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author stephan
 * 
 */
@SuppressWarnings("restriction")
public class ExtractTempTests extends AbstractSelectionTestCase
{
	private static ExtractTempTestSetup _testSetup;

    public ExtractTempTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
		_testSetup = new ExtractTempTestSetup(new TestSuite(ExtractTempTests.class));
		return _testSetup;
	}

	protected IPackageFragmentRoot getRoot()
	{
		return _testSetup.getRoot();
	}
	
	protected String getResourceLocation()
	{
		return "ExtractTemp/";
	}
	
	protected String adaptName(String name)
	{
	    if (getName().startsWith("testFocusType"))
	    {
	        return super.adaptName(name);
	    }
		return name + "_" + getName() + ".java";
	}	
	
	protected void performTest(
	        int startLine,
	        int startColumn,
	        int endLine,
	        int endColumn,
	        boolean declareFinal,
	        String outputFolder) throws Exception
	{
		performTest(_testSetup.getStatementsPackage(),
					new String[]{"T"},
					startLine, 
					startColumn, endLine, endColumn, 
					COMPARE_WITH_OUTPUT, declareFinal, 
					outputFolder);
	}
	
	protected void performTest(
	        IPackageFragment packageFragment,
	        String[] ids,
	        int startLine,
	        int startColumn,
	        int endLine,
	        int endColumn,
	        int mode,
	        String outputFolder) throws Exception
	{
		performTest(packageFragment, ids, startLine, startColumn, endLine, endColumn, mode, false, outputFolder);
	}
	
	protected void performTest(
	        IPackageFragment packageFragment,
	        String[] ids,
	        int startLine,
	        int startColumn,
	        int endLine,
	        int endColumn,
	        int mode,
	        boolean declareFinal,
	        String outputFolder) throws Exception
	{
	    ICompilationUnit[] compUnits = createCUs(packageFragment, ids);
		ISourceRange selection= TextRangeUtil.getSelection(compUnits[0], startLine, startColumn, endLine, endColumn);
		ExtractTempRefactoring refactoring = new ExtractTempRefactoring(
		        compUnits[0],
		        selection.getOffset(),
		        selection.getLength());
		refactoring.setTempName("extracted");
		refactoring.setDeclareFinal(declareFinal);
		RefactoringStatus status =
		    refactoring.checkInitialConditions(new NullProgressMonitor());
		switch (mode)
		{
			case VALID_SELECTION:
				assertTrue(status.isOK());
				break;
			case INVALID_SELECTION:
				if (!status.isOK())
					return;
		}
		
		String out = null;
		switch (mode)
		{
			case COMPARE_WITH_OUTPUT:
			    out = getProofedContent(outputFolder, ids[0]);
			    break;		
		}
		performTest(compUnits[0], refactoring, mode, out, true);
	}
	
    private ICompilationUnit[] createCUs(
            IPackageFragment packageFragment,
            String[] ids)
		throws Exception
	{
	    ICompilationUnit[] cus = new ICompilationUnit[ids.length];
	
	    for (int idx = 0; idx < ids.length; idx++)
	    {
	        Assert.isNotNull(ids[idx]);
	        cus[idx] = createCU(packageFragment, ids[idx]);
	    }
	    return cus;
	}

	protected int getCheckingStyle()
	{
		return CheckConditionsOperation.FINAL_CONDITIONS;
	}

	/********** tests **********/
	//=====================================================================================
	// Testing within statement
	//=====================================================================================

	//extract team instantiation expression "new T_x()" from within.team_expression
	public void testWithin1() throws Exception
    {
	    performTest(9, 16, 9, 35,
	    			false,
	            	"statements_out");
    }
	// body is single statement instead of block
	public void testWithin2() throws Exception
    {
	    performTest(9, 16, 9, 35,
	            	false,
	            	"statements_out");
    }
	
	// two element path, extracted is a dependent type
	public void testTypeAnchor1() throws Exception 
	{
		performTest(15,13,15,18,
					true,
					"statements_out");
	}

	// three element path, extracted is not a dependent type
	public void testTypeAnchor2() throws Exception 
	{
		performTest(21,13,21,20,
					true,
					"statements_out");
	}

	// three element path, extracted is a dependent type
	public void testTypeAnchor3() throws Exception 
	{
		performTest(19,13,19,20,
					true,
					"statements_out");
	}
}
