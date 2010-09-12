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
package org.eclipse.objectteams.otdt.ui.tests.refactoring.rename;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;

/**
 * FIXME(SH):
 * Once this test class is reactivated, also check the following conditions which are
 * known not to work currently (e.g., OTDT 1.1.6):
 * + Rename role type: 
 *   - inline (not dialog-based) refactoring links all references of a role type but not: 
 *     - its declaration 
 *     - class literals 
 *     (linked-mode in the Editor).
 *   - class literals in a predicate are not renamed
 * 
 * @author brcan
 *  
 */
@SuppressWarnings({"restriction", "nls"})
public class RenameTypeTests extends RefactoringTest
{
    private static final String REFACTORING_PATH = "RenameType/";
    
    private static IPackageFragment _teamPkg;
    
    private RenameProcessor _processor = null;

    
    public RenameTypeTests(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new MySetup(new TestSuite(RenameTypeTests.class));
    }

    public static Test setUpTest(Test someTest)
    {
        return new MySetup(someTest);
    }

    protected String getRefactoringPath()
    {
        return REFACTORING_PATH;
    }

//	MIGRATE
	private RenameRefactoring createRefactoring(IType type, String newName)
    	throws CoreException
    {
        _processor = new RenameTypeProcessor(type);
        RenameTypeProcessor renameTypeProcessor = (RenameTypeProcessor)_processor;
        renameTypeProcessor.setNewElementName(newName);
	    RenameRefactoring ref = new RenameRefactoring(renameTypeProcessor);
        return ref;
    }

    private void createTeamPackageFragment() throws Exception
    {
        _teamPkg = getRoot().createPackageFragment("p.T1", true, null);
    }
    
    private IPackageFragment getUpdatedTeamPackageFragment() throws Exception
    {
        return getRoot().getPackageFragment("p.T2");
    }

    private IPackageFragment getTeamPackage()
    {
        return _teamPkg;
    }

    private void performRenameRefactoring_failing(
            String[] cuNames,
            String targetTypeName,
            String newName,
			boolean roleFilesExist) throws Exception
    {
        if (roleFilesExist) {
			createTeamPackageFragment();
		}
        ICompilationUnit[] cus = createCUs(cuNames);
        IType targetType = getType(cus[0], targetTypeName);
        Refactoring ref = createRefactoring(targetType, newName);
        RefactoringStatus result = performRefactoring(ref);
        assertNotNull("precondition was supposed to fail", result);
    }

//	MIGRATE
//    private String[] performRenameRefactoringOneCU_passing(
//            String oldCuName,
//            String oldName,
//            String newName,
//            String newCUName,
//            boolean updateReferences,
//            boolean updateTextualMatches) throws Exception
//    {
//        ICompilationUnit cu = createCUfromTestFile(getPackageP(), oldCuName);
//        IType classA = getType(cu, oldName);
//
//        IPackageFragment pack = (IPackageFragment) cu.getParent();
//        String[] renameHandles = null;
//        if (classA.getDeclaringType() == null &&
//                cu.getElementName().startsWith(classA.getElementName()))
//        {
//            renameHandles = ParticipantTesting.createHandles(classA, cu, cu.getResource());
//        }
//        else
//        {
//            renameHandles = ParticipantTesting.createHandles(classA);
//        }
//        RenameRefactoring ref = createRefactoring(classA, newName);
//        RenameTypeProcessor processor = (RenameTypeProcessor) ref.getProcessor();
//        processor.setUpdateReferences(updateReferences);
//        processor.setUpdateTextualMatches(updateTextualMatches);
//        assertEquals("was supposed to pass", null, performRefactoring(ref));
//        ICompilationUnit newcu = pack.getCompilationUnit(newCUName + ".java");
//        assertTrue("cu " + newcu.getElementName() + " does not exist", newcu.exists());
//        assertEqualLines(
//                "invalid renaming!",
//                getFileContents(getOutputTestFileName(newCUName)),
//                newcu.getSource());
//        return renameHandles;
//    }

    private void performRenameRefactoring_passing(
            String[] cuNames,
            String targetTypeName,
            String newName,
            boolean roleFilesExist,
            boolean updateReferences,
            boolean updateTextualMatches) throws Exception
    {
//    	MIGRATE
        if (roleFilesExist)
        {
            createTeamPackageFragment();
        }
        ICompilationUnit[] cus = createCUs(cuNames);
        IType targetType = getType(cus[0], targetTypeName);
        IPackageFragment pack = (IPackageFragment) cus[0].getParent();
        RenameRefactoring refactoring = createRefactoring(targetType, newName);
        RenameTypeProcessor renameTypeProcessor = (RenameTypeProcessor)_processor;
        renameTypeProcessor.setUpdateReferences(updateReferences);
        renameTypeProcessor.setUpdateTextualMatches(updateTextualMatches);
		RefactoringStatus result = performRefactoring(refactoring);
		assertEquals("was supposed to pass", null, result);
		//if a top-level type is renamed, its compilation unit is also renamed
		if (targetType.getDeclaringType() == null &&
		        cus[0].getElementName().startsWith(targetType.getElementName()))
		{
		    ICompilationUnit newCu = pack.getCompilationUnit(newName + ".java");
			assertTrue("cu " + newCu.getElementName() + " does not exist", newCu.exists());
			assertEqualLines("invalid renaming!",
			        getFileContents(getOutputTestFileName(newName)), newCu.getSource());
			if (roleFilesExist)
			{
			    IPackageFragment updatedTeamPackage = getUpdatedTeamPackageFragment();
		        for (int idx = 0; idx < cus.length; idx++)
				{
		            if (cus[idx].getElementName().startsWith("R") ||
		                    cus[idx].getElementName().startsWith("TR"))
		                
		            {
		                cus[idx] = updatedTeamPackage.getCompilationUnit(cus[idx].getElementName());
			    		assertEqualLines("invalid renaming!",
			    		        getFileContents(createOutputTestFileName(cus, idx)), cus[idx].getSource());
		            }
				}
			}
		}
		else
		{
	        for (int idx = 0; idx < cus.length; idx++)
			{
	    		assertEqualLines("invalid renaming!",
	    		        getFileContents(createOutputTestFileName(cus, idx)), cus[idx].getSource());
			}

		}
    }
    
    private String getSimpleNameOfCu(String compUnit)
    {
        int dot = compUnit.lastIndexOf('.');
        return compUnit.substring(0, dot);
    }
    
    private String createOutputTestFileName(ICompilationUnit[] cus, int idx)
    {
        return getOutputTestFileName(getSimpleNameOfCu(cus[idx].getElementName()));
    }

    private ICompilationUnit[] createCUs(String[] cuNames) throws Exception
    {
        ICompilationUnit[] cus = new ICompilationUnit[cuNames.length];

        for (int idx = 0; idx < cuNames.length; idx++)
        {
            Assert.isNotNull(cuNames[idx]);
            if (cuNames[idx].startsWith("R") || cuNames[idx].startsWith("TR"))
            {
                cus[idx] = createCUfromTestFile(getTeamPackage(), cuNames[idx]);
            }
            else
            {
	            cus[idx] = createCUfromTestFile(getPackageP(), cuNames[idx]);
            }
        }
        return cus;
    }
    
    /*********** tests ***********/
    //passing
	public void testRenameTeamclass() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1" }, "T1", "T2", false, true, false);
	}

	public void testRenameNestedTeamclass() throws Exception {
		performRenameRefactoring_passing(new String[] { "T" }, "TR1", "TR2", false, true, false);
	}

	public void testUpdateReferencesToTeamclass() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1" }, "T1", "T2", false, true, false);
	}

	public void testUpdateReferencesToNestedTeamclass() throws Exception {
		performRenameRefactoring_passing(new String[] { "T" }, "TR1", "TR2", false, true, false);
	}

	public void testRenameRoleclass() throws Exception {
		performRenameRefactoring_passing(new String[] { "T" }, "R1", "R2", false, true, false);
	}

	public void testRenameRoleclassPrecedence() throws Exception {
		performRenameRefactoring_passing(new String[] { "T" }, "R2", "ROut", false, true, false);
	}

	public void testRenameRoleclassLiteralInPredicate() throws Exception {
		performRenameRefactoring_passing(new String[] { "T" }, "R1", "ROut", false, true, false);
	}

	public void testRenameInnermostRole() throws Exception {
		performRenameRefactoring_passing(new String[] { "T" }, "IR1", "IR2", false, true, false);
	}

	public void testUpdateReferencesToRoleclass() throws Exception {
		performRenameRefactoring_passing(new String[] { "T" }, "R1", "R2", false, true, false);
	}

	public void testUpdateReferencesToInnermostRole() throws Exception {
		performRenameRefactoring_passing(new String[] { "T" }, "IR1", "IR2", false, true, false);
	}

	// see https://svn.objectteams.org/trac/ot/ticket/80
	public void testUpdateImplicitSuperRoles() throws Exception {
		performRenameRefactoring_passing(new String[] { "T2", "T1" }, "R1", "R3", false, true, false);
	}

	public void testUpdateImplicitSubRoles() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1", "T2" }, "R1", "R3", false, true, false);
	}

	public void testRenameImplicitSuperRoles1() throws Exception {
		performRenameRefactoring_passing(new String[] { "T8", "T2", "T3", "T4", "T5", "T6", "T7", "T1" }, "R2", "R_New", false, true, false);
	}

	public void testRenameImplicitSuperRoles2() throws Exception {
		performRenameRefactoring_passing(new String[] { "T2", "T1" }, "R1", "R4", false, true, false);
	}

	public void testRenameImplicitSubRoles1() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8" }, "R2", "R_New", false, true, false);
	}

	public void testRenameImplicitSubRoles2() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1", "T2" }, "R1", "R4", false, true, false);
	}

	// see https://svn.objectteams.org/trac/ot/ticket/79
	public void testUpdateReferenceInTeamPackage1() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1", "R1" }, "T1", "T2", true, true, false);

	}

	public void testUpdateReferenceInTeamPackage2() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1", "R1", "R2" }, "T1", "T2", true, true, false);

	}

	public void testUpdateReferenceUsingTypeAnchor1() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1", "T2" }, "R1", "R3", false, true, false);
	}

	public void testUpdateReferenceUsingTypeAnchor2() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1", "T2" }, "R1", "R3", false, true, false);
	}

	public void testUpdateReferenceUsingTypeAnchor3() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1", "T2" }, "R1", "R3", false, true, false);
	}

	public void testUpdateReferenceUsingTypeAnchor4() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1", "T2" }, "R1", "R3", false, true, false);
	}

	public void testUpdateReferenceUsingTypeAnchor5() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1", "T2" }, "R1", "R3", false, true, false);
	}

	public void testEnclosedNestedTeamHasNewName() throws Exception {
		performRenameRefactoring_failing(new String[] { "B" }, "B", "A", false);
	}

	public void testEnclosedRoleHasNewName() throws Exception {
		performRenameRefactoring_failing(new String[] { "B" }, "B", "A", false);
	}

	public void testEnclosedRoleInNestedTeamHasNewName() throws Exception {
		performRenameRefactoring_failing(new String[] { "B" }, "B", "A", false);
	}

	public void testEnclosingNestedTeamHasNewName() throws Exception {
		performRenameRefactoring_failing(new String[] { "B" }, "A", "C", false);
	}

	public void testEnclosingTeamHasNewName() throws Exception {
		performRenameRefactoring_failing(new String[] { "B" }, "A", "B", false);
	}

	public void testUpdatePhantomTypeReference() throws Exception {
		performRenameRefactoring_passing(new String[] { "T1", "T2", "T3" }, "R1", "R3", false, true, false);
	}

	public void testNameConflictInSubclass() throws Exception {
		performRenameRefactoring_failing(new String[] { "T1", "T2" }, "R1", "R2", false);
	}

	public void testNameConflictInSuperclass() throws Exception {
		performRenameRefactoring_failing(new String[] { "T1", "T2" }, "R1", "R2", false);
	}

	public void testNameConflictWithRegularClass1() throws Exception {
		performRenameRefactoring_failing(new String[] { "T1", "T2", "A" }, "R1", "A", false);
	}

	public void testNameConflictWithRegularClass2() throws Exception {
		performRenameRefactoring_failing(new String[] { "A", "T1" }, "A", "R1", false);
	}

	public void testImplicitOverridingInSuperTeam() throws Exception {
		performRenameRefactoring_failing(new String[] { "T", "TSuper" }, "R", "R1", false);
	}

	public void testImplicitOverridingInSubTeam() throws Exception {
		performRenameRefactoring_failing(new String[] { "T", "TSub" }, "R", "R1", false);
	}

	public void testNameClashInSameTeam() throws Exception {
		performRenameRefactoring_failing(new String[] { "T" }, "R1", "R2", false);
	}

	public void testShadowedName1() throws Exception {
		performRenameRefactoring_failing(new String[] { "T" }, "R1", "R2", false);
	}

	public void testShadowedName2() throws Exception {
		performRenameRefactoring_failing(new String[] { "T" }, "R1", "R2", false);
	}

	public void testShadowedNameOfSuperType1() throws Exception {
		performRenameRefactoring_failing(new String[] { "T1", "T2" }, "R1", "R2", false);
	}

	public void testShadowedNameOfSuperType2() throws Exception {
		performRenameRefactoring_failing(new String[] { "T1", "T2" }, "R1", "R2", false);
	}

	public void testShadowedNameOfSubType1() throws Exception {
		performRenameRefactoring_failing(new String[] { "T1", "T2" }, "R1", "R2", false);
	}
		
	public void testNameClashWithRoleFile() throws Exception {
		performRenameRefactoring_failing(new String[] { "T1", "R2" }, "R1", "R2", true);
	}
		
	 public void testShadowedNameRoleFile1() throws Exception {
		performRenameRefactoring_failing(new String[] { "R1", "T1" }, "R1", "R2", true);
	}

	public void testShadowedNameRoleFile2() throws Exception {
		performRenameRefactoring_failing(new String[] { "T1", "R2" }, "R1", "R2", true);
	}

	public void testShadowedNameRoleFile3() throws Exception {
		performRenameRefactoring_failing(new String[] { "R1", "T1", "T2" }, "R1", "R2", true);
	}

	public void testShadowedNameRoleFile5() throws Exception {
		performRenameRefactoring_failing(new String[] { "T2", "T1", "RT" }, "R1", "R2", true);
	}

	public void testShadowedNameRoleFile6() throws Exception {
		performRenameRefactoring_failing(new String[] { "T2", "T1", "R2" }, "R1", "R2", true);
	}
		
	public void testImplicitOverridingWithRoleFile1() throws Exception {
		performRenameRefactoring_failing(new String[] { "T2", "T1", "R1" }, "R", "R1", true);
	}

	 public void testImplicitOverridingWithRoleFile2() throws Exception {
		performRenameRefactoring_failing(new String[] { "T", "R1", "T1" }, "R", "R1", true);
	}
	
	// failing
	
	// FIXME(jogeb): phantom roles within phantom roles wont be found by the
	// OTTypeHierarchy or change strategy in RenameTypeAdapter
	public void _testShadowedNameOfSubType2() throws Exception {
		performRenameRefactoring_failing(new String[] { "T1", "T2" }, "R1", "R2", false);
	}

	public void _testShadowedNameRoleFile4() throws Exception {
		performRenameRefactoring_failing(new String[] { "RT", "T1", "T2" }, "R1", "R2", true);
	}
		
}
