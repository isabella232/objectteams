/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 GK Software AG
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
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.ParameterInfo;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.structure.ChangeSignatureProcessor;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.MySetup;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.RefactoringTest;

/**
 * Some helper methods in this class have been inspired by the test class
 * <code>ChangeSignatureTests</code> in the test suite
 * <code>org.eclipse.jdt.ui.tests.refactoring</code> provided by Eclipse JDT.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public class ChangeSignatureTests extends RefactoringTest {
	
    private static final String REFACTORING_PATH = "ChangeSignature/";

	public ChangeSignatureTests(String name) {
		super(name);
	}

    public static Test suite()
    {
        return new MySetup(new TestSuite(ChangeSignatureTests.class));
    }

    public static Test setUpTest(Test test)
    {
        return new MySetup(test);
    }

    protected String getRefactoringPath()
    {
        return REFACTORING_PATH;
    }

    private Refactoring createRefactoring(ChangeSignatureProcessor processor)
    {
        return new ProcessorBasedRefactoring(processor);
    }

	private ChangeSignatureProcessor createProcessor(IMethod method) throws JavaModelException
    {
        return new ChangeSignatureProcessor(method);
    }


	private String getSimpleTestFileName(boolean canReorder, boolean input){
		String fileName = "A_" + getName();
		if (canReorder)
			fileName += input ? "_in": "_out";
		return fileName + ".java";
	}

	private String getTestFileName(boolean canReorder, boolean input){
		String fileName= getTestFolderPath(canReorder);
		return fileName + getSimpleTestFileName(canReorder, input);
	}

	private String getTestFolderPath(boolean canModify) {
		String fileName= TEST_PATH_PREFIX + getRefactoringPath();
		fileName += (canModify ? "canModify/": "cannotModify/");
		return fileName;
	}

	//---helpers

	protected ICompilationUnit createCUfromTestFile(IPackageFragment pack, boolean canRename, boolean input) throws Exception {
		return createCU(pack, getSimpleTestFileName(canRename, input), getFileContents(getTestFileName(canRename, input)));
	}

	static ParameterInfo[] createNewParamInfos(String[] newTypes, String[] newNames, String[] newDefaultValues) {
		if (newTypes == null)
			return new ParameterInfo[0];
		ParameterInfo[] result= new ParameterInfo[newTypes.length];
		for (int i= 0; i < newDefaultValues.length; i++) {
			result[i]= ParameterInfo.createInfoForAddedParameter(newTypes[i], newNames[i], newDefaultValues[i]);
		}
		return result;
	}

	static void addInfos(List list, ParameterInfo[] newParamInfos, int[] newIndices) {
		if (newParamInfos == null || newIndices == null)
			return;
		for (int i= newIndices.length - 1; i >= 0; i--) {
			list.add(newIndices[i], newParamInfos[i]);
		}
	}

	private void helperAdd(String[] signature, ParameterInfo[] newParamInfos, int[] newIndices, boolean createDelegate, String expectedInfo) throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), true, true);
		IType classA= getType(cu, "A");
		IMethod method = classA.getMethod("m", signature);
		assertTrue("method does not exist", method.exists());
		assertTrue("refactoring not available", RefactoringAvailabilityTester.isChangeSignatureAvailable(method));

		ChangeSignatureProcessor processor= new ChangeSignatureProcessor(method);
		Refactoring ref= new ProcessorBasedRefactoring(processor);

		processor.setDelegateUpdating(createDelegate);
		addInfos(processor.getParameterInfos(), newParamInfos, newIndices);
		RefactoringStatus initialConditions= ref.checkInitialConditions(new NullProgressMonitor());
		assertTrue("precondition was supposed to pass:"+initialConditions.getEntryWithHighestSeverity(), initialConditions.isOK());
		RefactoringStatus result= performRefactoring(ref, true);
		if (expectedInfo != null) {
			assertTrue("precondition was supposed to create an info:"+result.getEntryWithHighestSeverity(), result.hasInfo());
			assertEquals("wrong info", expectedInfo, result.getEntryMatchingSeverity(RefactoringStatus.INFO).getMessage());
		} else {
			assertEquals("precondition was supposed to pass", null, result);
		}

		IPackageFragment pack= (IPackageFragment)cu.getParent();
		String newCuName= getSimpleTestFileName(true, true);
		ICompilationUnit newcu= pack.getCompilationUnit(newCuName);
		assertTrue(newCuName + " does not exist", newcu.exists());
		String expectedFileContents= getFileContents(getTestFileName(true, false));
		assertEqualLines("invalid renaming", expectedFileContents, newcu.getSource());

//		assertParticipant(classA);
	}

	private void helperDelete(String[] signature, int[] deleteIndices, boolean createDelegate, String expectedInfo) throws Exception {
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), true, true);
		IType classA= getType(cu, "A");
		IMethod method = classA.getMethod("m", signature);
		assertTrue("method does not exist", method.exists());
		assertTrue("refactoring not available", RefactoringAvailabilityTester.isChangeSignatureAvailable(method));

		ChangeSignatureProcessor processor= new ChangeSignatureProcessor(method);
		Refactoring ref= new ProcessorBasedRefactoring(processor);

		processor.setDelegateUpdating(createDelegate);
		markAsDeleted(processor.getParameterInfos(), deleteIndices);
		RefactoringStatus initialConditions= ref.checkInitialConditions(new NullProgressMonitor());
		assertTrue("precondition was supposed to pass:"+initialConditions.getEntryWithHighestSeverity(), initialConditions.isOK());
		RefactoringStatus result= performRefactoring(ref, true);
		if (expectedInfo != null) {
			assertTrue("precondition was supposed to create an info:"+result.getEntryWithHighestSeverity(), result.hasInfo());
			assertEquals("wrong info", expectedInfo, result.getEntryMatchingSeverity(RefactoringStatus.INFO).getMessage());
		} else {
			assertEquals("precondition was supposed to pass", null, result);
		}

		IPackageFragment pack= (IPackageFragment)cu.getParent();
		String newCuName= getSimpleTestFileName(true, true);
		ICompilationUnit newcu= pack.getCompilationUnit(newCuName);
		assertTrue(newCuName + " does not exist", newcu.exists());
		String expectedFileContents= getFileContents(getTestFileName(true, false));
		assertEqualLines("invalid renaming", expectedFileContents, newcu.getSource());

//		assertParticipant(classA);
	}

	private void helperPermute(String[] newOrder, String[] signature, boolean createDelegate) throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), true, true);
		IType classA= getType(cu, "A");
		IMethod method = classA.getMethod("m", signature);
		assertTrue("method does not exist", method.exists());
		assertTrue("refactoring not available", RefactoringAvailabilityTester.isChangeSignatureAvailable(method));

		ChangeSignatureProcessor processor= new ChangeSignatureProcessor(method);
		Refactoring ref= new ProcessorBasedRefactoring(processor);

		processor.setDelegateUpdating(createDelegate);
		permute(processor.getParameterInfos(), newOrder);
		ref.checkInitialConditions(new NullProgressMonitor());
		RefactoringStatus result= performRefactoring(ref, true);
		assertEquals("precondition was supposed to pass", null, result);

		IPackageFragment pack= (IPackageFragment)cu.getParent();
		String newCuName= getSimpleTestFileName(true, true);
		ICompilationUnit newcu= pack.getCompilationUnit(newCuName);
		assertTrue(newCuName + " does not exist", newcu.exists());
		String expectedFileContents= getFileContents(getTestFileName(true, false));
//		assertEquals("invalid renaming", expectedFileContents, newcu.getSource());
		assertEqualLines(expectedFileContents, newcu.getSource());

//		assertParticipant(classA);
	}

	private void permute(List infos, String[] newOrder) {
		int[] permutation= createPermutation(infos, newOrder);
		List swapped= new ArrayList(infos.size());
		ParameterInfo[] newInfos= new  ParameterInfo[infos.size()];
		for (int i= 0; i < permutation.length; i++) {
			newInfos[i]= (ParameterInfo)infos.get(permutation[i]);
		}
		infos.clear();
		for (int i= 0; i < newInfos.length; i++) {
			infos.add(newInfos[i]);
		}
	}

	private static int[] createPermutation(List infos, String[] newOrder) {
		int[] result= new int[infos.size()];
		for (int i= 0; i < result.length; i++) {
			result[i]= indexOfOldName(infos, newOrder[i]);
		}
		return result;
	}

	private static int indexOfOldName(List infos, String string) {
		for (Iterator iter= infos.iterator(); iter.hasNext();) {
			ParameterInfo info= (ParameterInfo) iter.next();
			if (info.getOldName().equals(string))
				return infos.indexOf(info);
		}
		assertTrue(false);
		return -1;
	}

	private void markAsDeleted(List list, int[] deleted) {
		if (deleted == null)
			return;
		for (int i= 0; i < deleted.length; i++) {
			((ParameterInfo)list.get(deleted[i])).markAsDeleted();
		}
	}

	// add argument at base side of callin
	public void testAdd01()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"0"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {1};
		helperAdd(signature, newParamInfo, newIndices, true/*delegate*/, null/*expectInfo*/);
	}

	// add argument at role side (inherited from non-role) of callout
	public void testAdd02()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"0"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {1};
		helperAdd(signature, newParamInfo, newIndices, false/*delegate*/, null/*expectInfo*/);
	}

	// add argument at role side of callin
	public void testAdd03()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"0"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {1};
		helperAdd(signature, newParamInfo, newIndices, false/*delegate*/, "Adding arguments at the role side of a callin binding is not fully supported. Please manually update transitive references."/*expectedInfo*/);
	}

	// add argument at base side of callout
	public void testAdd04()throws Exception{
		String[] signature= {"I"};
		String[] newNames= {"x"};
		String[] newTypes= {"int"};
		String[] newDefaultValues= {"0"};
		ParameterInfo[] newParamInfo= createNewParamInfos(newTypes, newNames, newDefaultValues);
		int[] newIndices= {1};
		helperAdd(signature, newParamInfo, newIndices, false/*delegate*/, "Adding arguments at the base side of a callout binding is not fully supported. Please manually update transitive references."/*expectedInfo*/);
	}

	// delete argument at base side of callin
	public void testDelete01()throws Exception{
		int[] deleteIndices= {1};
		helperDelete(new String[]{"I", "I"}, deleteIndices, true/*delegate*/, "Deleting arguments at the base side of a callin binding is not fully supported. Please manually update transitive references."/*expectInfo*/);
	}

	// delete argument at role side (inherited from non-role) of callout
	public void testDelete02()throws Exception{
		int[] deleteIndices= {1};
		helperDelete(new String[]{"I", "I"}, deleteIndices, false/*delegate*/, "Deleting arguments at the role side of a callout binding is not fully supported. Please manually update transitive references."/*expectInfo*/);
	}

	public void testReorder01() throws Exception {
		helperPermute(new String[]{"ignore", "b", "a"}, new String[]{"I", "Z", "QString;"}, false);
	}
}
