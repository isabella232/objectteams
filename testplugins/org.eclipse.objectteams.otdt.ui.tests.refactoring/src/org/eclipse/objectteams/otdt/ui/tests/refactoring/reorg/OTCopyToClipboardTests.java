/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring.reorg;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceManipulation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.TypedSource;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaElementTransfer;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgUtils;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.refactoring.reorg.CopyToClipboardAction;
import org.eclipse.jdt.internal.ui.refactoring.reorg.TypedSourceTransfer;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTestSetup;
import org.eclipse.jdt.ui.tests.refactoring.infra.MockClipboard;
import org.eclipse.jdt.ui.tests.refactoring.infra.MockWorkbenchSite;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.ui.tests.refactoring.OTRefactoringTestSetup;

//{OT_COPY_PASTE: Copy of class org.eclipse.jdt.ui.tests.reorg.CopyToClipboardActionTest
//Reason: adapt tests for the OTCopyToClipboardAction, add additional tests for OT elements
//        remove tests which won't work with the given OT type declarations
//		  test enumeration is kept, new OT tests are testEnabledOT0 - testEnabledOT6
@SuppressWarnings("restriction")
public class OTCopyToClipboardTests extends RefactoringTest
{
    private static final String CU_T1_NAME = "T1";
    private static final String CU_B1_NAME = "B1";

    private ILabelProvider         _labelProvider;
    private Clipboard              _clipboard;
    private ICompilationUnit       _cuT1;
    private ICompilationUnit       _cuB1;
	
	public OTCopyToClipboardTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new OTRefactoringTestSetup(new TestSuite(OTCopyToClipboardTests.class));
	}

	protected void setUp() throws Exception
    {
        super.setUp();
        _clipboard = new MockClipboard(Display.getDefault());
        
        _cuT1 = createCU(getPackageP(), CU_T1_NAME + ".java", 
                "package p;" + "\n" +
                "import java.util.List;" + "\n" +
                "public team class T1" + "\n" +
                "{" + "\n" +
                "int x;" + "\n" +
                "public void t1m(){}" + "\n" +
                "public team class TR1" + "\n" +
                "{" + "\n" +
                "public void tr1m(){}" + "\n" +
                "public class R1 extends S1 playedBy B1" + "\n" +
                "{" + "\n" +
                "public abstract int rm();" + "\n" +
                "rm -> bm;" + "\n" +
                "sm <- after bm;" + "\n" +
                "rm => get x;" + "\n" +
                "}" + "\n" +
                "}" + "\n" +
                "}");

        _cuB1 = createCU(getPackageP(), CU_B1_NAME + ".java", 
                "package p;" + "\n" +
                "public class B1" + "\n" +
                "{" + "\n" +
                "private int x;" + "\n" +
                "public int bm(){}" + "\n" +
                "}");

        _labelProvider = new JavaElementLabelProvider(
                JavaElementLabelProvider.SHOW_VARIABLE + 
                JavaElementLabelProvider.SHOW_PARAMETERS + 
                JavaElementLabelProvider.SHOW_TYPE);

        assertTrue("T1.java does not exist", _cuT1.exists());
        assertTrue("B1.java does not exist", _cuB1.exists());
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        performDummySearch();
        _clipboard.dispose();
        _labelProvider.dispose();
        delete(_cuT1);
        delete(_cuB1);
    }	
	
	private static void delete(ISourceManipulation element) {
		try {
			if (element != null && ((IJavaElement)element).exists())
				element.delete(true, null);
		} catch(JavaModelException e) {
			//ignore, we must keep going
		}		
	}
	
	private void checkDisabled(Object[] elements){
		CopyToClipboardAction copyAction= new CopyToClipboardAction(new MockWorkbenchSite(elements), _clipboard);
		copyAction.setAutoRepeatOnFailure(true);
		copyAction.update(copyAction.getSelection());
		assertTrue("action should be disabled", ! copyAction.isEnabled());
	}

	private void checkEnabled(Object[] elements) throws Exception{
		CopyToClipboardAction copyAction= new CopyToClipboardAction(new MockWorkbenchSite(elements), _clipboard);
		copyAction.setAutoRepeatOnFailure(true);
		copyAction.update(copyAction.getSelection());
		assertTrue("action should be enabled", copyAction.isEnabled());
		copyAction.run();
		checkClipboard(elements);
	}

	private void checkClipboard(Object[] elementsCopied) throws Exception {
		IResource[] resourcesCopied= getResources(elementsCopied);
		IJavaElement[] javaElementsCopied= getJavaElements(elementsCopied);
		IType[] mainTypesCopied= ReorgUtils.getMainTypes(javaElementsCopied);
		
		IResource[] resourcesExpected= computeResourcesExpectedInClipboard(resourcesCopied, mainTypesCopied, javaElementsCopied);
		IJavaElement[] javaElementsExpected= computeJavaElementsExpectedInClipboard(javaElementsCopied, mainTypesCopied);
		
		String[] clipboardFiles= getClipboardFiles();
		IResource[] clipboardResources= getClipboardResources();
		String clipboardText= getClipboardText();
		IJavaElement[] clipboardJavaElements= getClipboardJavaElements();
		TypedSource[] clipboardTypedSources= getClipboardTypedSources();

		checkNames(resourcesCopied, javaElementsCopied, clipboardText);
		checkFiles(resourcesCopied, javaElementsCopied, mainTypesCopied, clipboardFiles);
		checkTypedSources(javaElementsCopied, clipboardTypedSources);
		checkElements(resourcesExpected, clipboardResources);
		checkElements(javaElementsExpected, clipboardJavaElements);
	}
	
	private void checkTypedSources(IJavaElement[] javaElementsCopied, TypedSource[] clipboardTypedSources) throws Exception {
		TypedSource[] typedSources= TypedSource.createTypedSources(javaElementsCopied);
		assertEquals("different number", typedSources.length, clipboardTypedSources.length);		
		TypedSource.sortByType(typedSources);
		TypedSource.sortByType(clipboardTypedSources);
		for (int i= 0; i < typedSources.length; i++) {
			assertEquals("different typed sources", typedSources[i], clipboardTypedSources[i]);
		}
	}

	private IResource[] computeResourcesExpectedInClipboard(IResource[] resourcesCopied, IType[] mainTypesCopied, IJavaElement[] javaElementsCopied) throws JavaModelException {
		IResource[] cuResources= ReorgUtils.getResources(getCompilationUnits(javaElementsCopied));
		return ReorgUtils.union(cuResources, ReorgUtils.union(resourcesCopied, ReorgUtils.getResources(ReorgUtils.getCompilationUnits(mainTypesCopied))));
	}

	private static IJavaElement[] computeJavaElementsExpectedInClipboard(IJavaElement[] javaElementsExpected, IType[] mainTypesCopied) throws JavaModelException {
		return ReorgUtils.union(javaElementsExpected, ReorgUtils.getCompilationUnits(mainTypesCopied));
	}

	private String getName(IResource resource){
		return _labelProvider.getText(resource);
	}
	private String getName(IJavaElement javaElement){
		return _labelProvider.getText(javaElement);
	}

	private static void checkElements(Object[] copied, Object[] retreivedFromClipboard) {
		assertEquals("different number of elements", copied.length, retreivedFromClipboard.length);
		sortByName(copied);
		sortByName(retreivedFromClipboard);
		for (int i= 0; i < retreivedFromClipboard.length; i++) {
			Object retreived= retreivedFromClipboard[i];
			assertTrue("element does not exist", exists(retreived));
			assertTrue("different copied " + getName(copied[i]) + " retreived: " + getName(retreived) , copied[i].equals(retreivedFromClipboard[i]));
		}
	}

	private static boolean exists(Object element) {
		if (element instanceof IJavaElement)
			return ((IJavaElement)element).exists();
		if (element instanceof IResource)
			return ((IResource)element).exists();
		assertTrue(false);
		return false;
	}

	private static String getName(Object object) {
		if (object instanceof IJavaElement)
			return ((IJavaElement)object).getElementName();
		if (object instanceof IResource)
			return ((IResource)object).getName();
		return object == null ? null : object.toString();
	}

	private static void sortByName(Object[] copied) {
		Arrays.sort(copied, new Comparator(){
			public int compare(Object arg0, Object arg1) {
				return getName(arg0).compareTo(getName(arg1));
			}
		});
	}

	private void checkNames(IResource[] resourcesCopied, IJavaElement[] javaElementsCopied, String clipboardText){
		List stringLines= Arrays.asList(Strings.convertIntoLines(clipboardText));
		assertEquals("different number of names", resourcesCopied.length + javaElementsCopied.length, stringLines.size());
		for (int i= 0; i < resourcesCopied.length; i++) {
			String name= getName(resourcesCopied[i]);
			assertTrue("name not in set:" + name, stringLines.contains(name));
		}
		for (int i= 0; i < javaElementsCopied.length; i++) {
			IJavaElement element= javaElementsCopied[i];
			if (! ReorgUtils.isInsideCompilationUnit(element)){
				String name= getName(element);
				assertTrue("name not in set:" + name, stringLines.contains(name));				
			}
		}
	}	
	
	private static void checkFiles(IResource[] resourcesCopied, IJavaElement[] javaElementsCopied, IType[] mainTypes, String[] clipboardFiles) {
		int expected= 0;
		expected += resourcesCopied.length;
		expected += countResources(javaElementsCopied);
		expected += mainTypes.length;
		
		//we cannot compare file names here because they're absolute and depend on the worspace location
		assertEquals("different number of files in clipboard", expected, clipboardFiles.length);
	}

	private static int countResources(IJavaElement[] javaElementsCopied) {
		int count= 0;
		for (int i= 0; i < javaElementsCopied.length; i++) {
			IJavaElement element= javaElementsCopied[i];
			switch (element.getElementType()) {
				case IJavaElement.JAVA_PROJECT :
				case IJavaElement.PACKAGE_FRAGMENT_ROOT :
				case IJavaElement.PACKAGE_FRAGMENT :
				case IJavaElement.COMPILATION_UNIT :
				case IJavaElement.CLASS_FILE :
					count++;
			}
		}
		return count;
	}

	private static IJavaElement[] getCompilationUnits(IJavaElement[] javaElements) {
		List cus= ReorgUtils.getElementsOfType(javaElements, IJavaElement.COMPILATION_UNIT);
		return (ICompilationUnit[]) cus.toArray(new ICompilationUnit[cus.size()]);
	}

	private static IResource[] getResources(Object[] elements) {
		return ReorgUtils.getResources(Arrays.asList(elements));
	}

	private static IJavaElement[] getJavaElements(Object[] elements) {
		return ReorgUtils.getJavaElements(Arrays.asList(elements));
	}

	private IJavaElement[] getClipboardJavaElements() {
		IJavaElement[] elements= (IJavaElement[])_clipboard.getContents(JavaElementTransfer.getInstance());
		return elements == null ? new IJavaElement[0]: elements; 
	}

	private String[] getClipboardFiles() {
		String[] files= (String[])_clipboard.getContents(FileTransfer.getInstance());
		return files == null ? new String[0]: files;
	}
	
	private IResource[] getClipboardResources() {
		IResource[] resources= (IResource[])_clipboard.getContents(ResourceTransfer.getInstance());
		return resources == null ? new IResource[0]: resources; 
	}

	private TypedSource[] getClipboardTypedSources() {
		TypedSource[] typedSources= (TypedSource[])_clipboard.getContents(TypedSourceTransfer.getInstance());
		return typedSources == null ? new TypedSource[0]: typedSources; 
	}

	private String getClipboardText() {
		return (String)_clipboard.getContents(TextTransfer.getInstance());
	}
	
	///---------tests

	public void testDisabled0() {
		Object[] elements= {};
		checkDisabled(elements);
	}	

	public void testDisabled1() throws Exception {
		Object[] elements= {null};
		checkDisabled(elements);
	}	

	public void testDisabled2() throws Exception {
		Object[] elements= {this};
		checkDisabled(elements);
	}	

	public void testDisabled3() throws Exception {
		Object[] elements= {RefactoringTestSetup.getProject(), getPackageP()};
		checkDisabled(elements);
	}	

	public void testDisabled4() throws Exception{
		checkDisabled(new Object[]{getPackageP(), _cuT1});
	}

	public void testDisabled5() throws Exception{
		checkDisabled(new Object[]{getRoot(), _cuT1});
	}

	public void testDisabled12() throws Exception{
		checkDisabled(new Object[]{getRoot().getJavaProject(), _cuT1});
	}

	public void testDisabled15() throws Exception {
		Object fieldF= _cuT1.getType("T1").getField("x");
		Object classA= _cuT1.getType("T1");
		Object[] elements= {fieldF, classA};
		checkDisabled(elements);
	}	

	public void testDisabled16() throws Exception {
		Object fieldF= _cuT1.getType("T1").getField("x");
		Object[] elements= {fieldF, _cuT1};
		checkDisabled(elements);
	}	

	public void testDisabled20() throws Exception {
		Object fieldF= _cuT1.getType("T1").getField("x");
		Object[] elements= {fieldF, getRoot()};
		checkDisabled(elements);
	}	

	public void testDisabled21() throws Exception {
		Object fieldF= _cuT1.getType("T1").getField("x");
		Object[] elements= {fieldF, RefactoringTestSetup.getProject()};
		checkDisabled(elements);
	}	

	public void testDisabled22() throws Exception {
		Object typeT1= _cuT1.getType("T1");
		Object typeB1= _cuB1.getType("B1");
		Object[] elements= {typeT1, typeB1};
		checkDisabled(elements);
	}
	
	public void testEnabled0() throws Exception {
		Object[] elements= {RefactoringTestSetup.getProject()};
		checkEnabled(elements);
	}	

	public void testEnabled1() throws Exception {
		Object[] elements= {getPackageP()};
		checkEnabled(elements);
	}	

	public void testEnabled2() throws Exception {
		Object[] elements= {getRoot()};
		checkEnabled(elements);
	}	

	public void testEnabled3() throws Exception {
		Object[] elements= {RefactoringTestSetup.getDefaultSourceFolder()};
		checkEnabled(elements);
	}
	
	public void testEnabled5() throws Exception{
		checkEnabled(new Object[]{getRoot()});
	}

	public void testEnabled6() throws Exception{
		checkEnabled(new Object[]{_cuT1});
	}

	public void testEnabled7() throws Exception{
		checkEnabled(new Object[]{getRoot().getJavaProject()});
	}
		
	public void testEnabled8() throws Exception{
		checkEnabled(new Object[]{getPackageP()});
	}
	
	public void testEnabled10() throws Exception{
		Object packDecl= _cuT1.getPackageDeclarations()[0];
		Object[] elements= {packDecl};
		checkEnabled(elements);
	}

	public void testEnabled11() throws Exception{
		Object importD= _cuT1.getImports()[0];
		Object[] elements= {importD};
		checkEnabled(elements);
	}

	public void testEnabled12() throws Exception{
//		printTestDisabledMessage("disabled due to bug 37750");
//		if (true)
//			return;
		IJavaElement importContainer= _cuT1.getImportContainer();
		Object[] elements= {importContainer};
		checkEnabled(elements);
	}

	public void testEnabled13() throws Exception{
		Object classA= _cuT1.getType("T1");
		Object[] elements= {classA};
		checkEnabled(elements);
	}

	public void testEnabled14() throws Exception{
		Object methodT1m= _cuT1.getType("T1").getMethod("t1m", new String[0]);
		Object[] elements= {methodT1m};
		checkEnabled(elements);
	}

	public void testEnabled15() throws Exception{
		Object fieldX= _cuT1.getType("T1").getField("x");
		Object[] elements= {fieldX};
		checkEnabled(elements);
	}

	public void testEnabled19() throws Exception{
//		printTestDisabledMessage("disabled due to bug 37750");
//		if (true)
//			return;

		Object classA= _cuT1.getType("T1");
		Object importContainer= _cuT1.getImportContainer();
		Object packDecl= _cuT1.getPackageDeclarations()[0];
		Object[] elements= {classA, importContainer, packDecl};
		checkEnabled(elements);
	}

	public void testEnabled22() throws Exception{
//		printTestDisabledMessage("bug 39410");
		Object classA= _cuT1.getType("T1");
		Object packDecl= _cuT1.getPackageDeclarations()[0];
		Object[] elements= {classA, packDecl};
		checkEnabled(elements);
	}
	
	public void testEnabledOT0() throws Exception
    {
        IType teamT1 = _cuT1.getType("T1");
        IType nestedTeamTR1 = teamT1.getType("TR1");
        IMethod methodTr1m = nestedTeamTR1.getMethod("tr1m", new String[0]);
        Object[] elements = { methodTr1m };
        checkEnabled(elements);
    }

    public void testEnabledOT1() throws Exception
    {
        IType teamT1 = _cuT1.getType("T1");
        IType nestedTeamTR1 = teamT1.getType("TR1");
        Object elem = nestedTeamTR1.getType("R1");
        if (elem instanceof IType)
        {
            IType type = (IType)elem;
            IRoleType roleR1 = (IRoleType)OTModelManager.getOTElement(type); 
            Object[] elements = { roleR1 };
            checkEnabled(elements);
        }
    }

    public void testEnabledOT2() throws Exception
    {
        IType teamT1 = _cuT1.getType("T1");
        IType nestedTeamTR1 = teamT1.getType("TR1");
        IType type = nestedTeamTR1.getType("R1");
        IRoleType roleR1 = (IRoleType)OTModelManager.getOTElement(type);
        IMethod methodRm = roleR1.getMethod("rm", new String[0]);
        Object[] elements = { methodRm };
        checkEnabled(elements);
    }

    public void testEnabledOT3() throws Exception
    {
        IType teamT1 = _cuT1.getType("T1");
        IType nestedTeamTR1 = teamT1.getType("TR1");
        IType type = nestedTeamTR1.getType("R1");
        IRoleType roleR1 = (IRoleType)OTModelManager.getOTElement(type);
        IMethodMapping[] mapping = roleR1.getMethodMappings(IRoleType.CALLOUTS);
        ICalloutMapping callout = null;
		for (int idx = 0; idx < mapping.length; idx++)
        {
            if (mapping[idx].getElementType() == IOTJavaElement.CALLOUT_MAPPING)
            {
		        callout = (ICalloutMapping)mapping[idx];
            }
        }
        Object[] elements = { callout };
        checkEnabled(elements);
    }

    public void testEnabledOT4() throws Exception
    {
        IType teamT1 = _cuT1.getType("T1");
        IType nestedTeamTR1 = teamT1.getType("TR1");
        IType type = nestedTeamTR1.getType("R1");
        IRoleType roleR1 = (IRoleType)OTModelManager.getOTElement(type);
        IMethodMapping[] mapping = roleR1.getMethodMappings(IRoleType.CALLINS);
        ICallinMapping callin = (ICallinMapping)mapping[0];
        Object[] elements = { callin };
        checkEnabled(elements);        
    }

    public void testEnabledOT5() throws Exception
    {
        IType teamT1 = _cuT1.getType("T1");
        IType nestedTeamTR1 = teamT1.getType("TR1");
        IType type = nestedTeamTR1.getType("R1");
        IRoleType roleR1 = (IRoleType)OTModelManager.getOTElement(type);
        IMethodMapping[] mapping = roleR1.getMethodMappings(IRoleType.CALLOUTS);
        ICalloutToFieldMapping calloutToField = null;
		for (int idx = 0; idx < mapping.length; idx++)
        {
            if (mapping[idx].getElementType() == IOTJavaElement.CALLOUT_TO_FIELD_MAPPING)
            {
                calloutToField = (ICalloutToFieldMapping)mapping[idx];
            }
        }
        Object[] elements = { calloutToField };
        checkEnabled(elements);        
    }

    public void testEnabledOT6() throws Exception
    {
        IType teamT1 = _cuT1.getType("T1");
        IField fieldX = teamT1.getField("x");
        IMethod methodT1m = teamT1.getMethod("t1m", new String[0]);
        IType nestedTeamTR1 = teamT1.getType("TR1");
        Object[] elements = { fieldX, methodT1m, nestedTeamTR1 };
        checkEnabled(elements);
    }
	
	
}
//sko}