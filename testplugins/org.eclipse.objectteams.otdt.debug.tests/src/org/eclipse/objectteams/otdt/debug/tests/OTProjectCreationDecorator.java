/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.debug.tests;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class OTProjectCreationDecorator extends AbstractOTDTDebugTest {

	public static boolean fgReady = false;
	
    /**
     * Constructor
     * @param name
     */
    public OTProjectCreationDecorator(String name) {
        super(name);
        fgReady = true;
    }
    
    public static boolean isReady() {
    	return fgReady;
    }

//    public void testTurnOffAutobuild() throws Exception {
//        IWorkspace workspace = ResourcesPlugin.getWorkspace();
//        IWorkspaceDescription description = workspace.getDescription();
//        workspace.isAutoBuilding();
//        description.setAutoBuilding(false);
//        workspace.setDescription(description);
//        assertFalse(workspace.isAutoBuilding());
//    }

    public void testPerspectiveSwtich() {
        DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
            public void run() {
                IWorkbench workbench = PlatformUI.getWorkbench();
                IPerspectiveDescriptor descriptor = workbench.getPerspectiveRegistry().findPerspectiveWithId(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
                IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
				activePage.setPerspective(descriptor);
				// hide variables and breakpoints view to reduce simaltaneous conflicting requests on debug targets
                IViewReference ref = activePage.findViewReference(IDebugUIConstants.ID_VARIABLE_VIEW);
                activePage.hideView(ref);
                ref = activePage.findViewReference(IDebugUIConstants.ID_BREAKPOINT_VIEW);
                activePage.hideView(ref);
            }
        });
    }

    public void testProjectCreation() throws Exception {
        // delete any pre-existing project
        IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject("DebugTests");
        if (pro.exists()) {
            pro.delete(true, true, null);
        }
        IWorkspaceDescription wsDescription = ResourcesPlugin.getWorkspace().getDescription();
        wsDescription.setAutoBuilding(false);
        ResourcesPlugin.getWorkspace().setDescription(wsDescription);
        
        // create & configure project and import source
        fJavaProject = org.eclipse.objectteams.otdt.ui.tests.util.JavaProjectHelper.createOTJavaProject("DebugTests", "bin");
        fJavaProject.setOption(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
        fJavaProject.setOption(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
        fJavaProject.setOption(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
        fJavaProject.setOption(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
        IPackageFragmentRoot src = JavaProjectHelper.addSourceContainer(fJavaProject, "src");
        
//{ObjectTeams: rootfile-path is now org.eclipse.objectteams.otdt.debug.tests + "testprograms"      
        File root = DebugTestsPlugin.getDefault().getPluginDirectoryPath(JavaProjectHelper.TEST_SRC_DIR);
//ike}
        JavaProjectHelper.importFilesFromDirectory(root, src.getPath(), null);
        
        // import classfiles
        root = DebugTestsPlugin.getDefault().getPluginDirectoryPath(new Path("classfiles"));
        JavaProjectHelper.importFilesFromDirectory(root, src.getPath(), null);
       
        // add rt.jar
        IVMInstall vm = JavaRuntime.getDefaultVMInstall();
        assertNotNull("No default JRE", vm);
        JavaProjectHelper.addContainerEntry(fJavaProject, new Path(JavaRuntime.JRE_CONTAINER));
//{ObjectTeams: add otre.jar:
        JavaProjectHelper.addContainerEntry(fJavaProject, OTREContainer.OTRE_CONTAINER_PATH);
// SH}
        pro = fJavaProject.getProject();

        // all set up, can start auto-building now:
        wsDescription.setAutoBuilding(true);
        ResourcesPlugin.getWorkspace().setDescription(wsDescription);
        
        // create launch configuration folder

        IFolder folder = pro.getFolder("launchConfigurations");
        if (folder.exists()) {
            folder.delete(true, null);
        }
        folder.create(true, true, null);

        // delete any existing launch configs
        ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
        for (int i = 0; i < configs.length; i++) {
            configs[i].delete();
        }

        // this one used by StratumTests:
        createLaunchConfiguration("Breakpoints");
//{ObjectTeams: own tests:        
        createLaunchConfiguration("rofitests.RoFiTeam", OTDebugPlugin.OT_LAUNCH_CONFIGURATION_TYPE);
        createLaunchConfiguration("copyinheritancetests.SubTeam", OTDebugPlugin.OT_LAUNCH_CONFIGURATION_TYPE);
        createLaunchConfiguration("copyinheritancetests.SubTeam2", OTDebugPlugin.OT_LAUNCH_CONFIGURATION_TYPE);
        createLaunchConfiguration("copyinheritancetests.SubTeam3", OTDebugPlugin.OT_LAUNCH_CONFIGURATION_TYPE);
// SH}
    }

    /**
     * Create a project with non-default, mulitple output locations.
     * 
     * @throws Exception
     */
    public void _testMultipleOutputProjectCreation() throws Exception {
        // delete any pre-existing project
        IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject("MultiOutput");
        if (pro.exists()) {
            pro.delete(true, true, null);
        }
        // create project with two src folders and output locations
        IJavaProject project = org.eclipse.objectteams.otdt.ui.tests.util.JavaProjectHelper.createOTJavaProject("MultiOutput", (String)null);
        JavaProjectHelper.addSourceContainer(project, "src1", "bin1");
        JavaProjectHelper.addSourceContainer(project, "src2", "bin2");

        // add rt.jar
        IVMInstall vm = JavaRuntime.getDefaultVMInstall();
        assertNotNull("No default JRE", vm);
        JavaProjectHelper.addContainerEntry(project, new Path(JavaRuntime.JRE_CONTAINER));
    }

    /**
     * Set up preferences that need to be changed for the tests
     */
    public void _testSetPreferences() {
        IPreferenceStore debugUIPreferences = DebugUIPlugin.getDefault().getPreferenceStore();
        // Don't prompt for perspective switching
        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND, MessageDialogWithToggle.ALWAYS);
        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE, MessageDialogWithToggle.ALWAYS);
        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE, MessageDialogWithToggle.NEVER);
        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD, MessageDialogWithToggle.ALWAYS);
        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR, MessageDialogWithToggle.ALWAYS);
        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH, MessageDialogWithToggle.NEVER);

        String property = System.getProperty("debug.workbenchActivation");
        if (property != null && property.equals("off")) {
            debugUIPreferences.setValue(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR, false);
            debugUIPreferences.setValue(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT, false);
            debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, false);
            debugUIPreferences.setValue(IDebugUIConstants.PREF_ACTIVATE_WORKBENCH, false);
        }

        IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
        // Turn off suspend on uncaught exceptions
        jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);
        jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, false);
        // Don't warn about HCR failures
        jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED, false);
        jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED, false);
        jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS, false);
        // Set the timeout preference to a high value, to avoid timeouts while
        // testing
        JDIDebugModel.getPreferences().setDefault(JDIDebugModel.PREF_REQUEST_TIMEOUT, 10000);
        // turn off monitor information
        jdiUIPreferences.setValue(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO, false);
    }

    public void testBuild() throws Exception {
        // force a full build and wait
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
        waitForBuild();
    }

//    public void testTurnOnAutoBuild() throws Exception {
//        // turn on autobuild again.
//        IWorkspace workspace = ResourcesPlugin.getWorkspace();
//        IWorkspaceDescription description = workspace.getDescription();
//        workspace.isAutoBuilding();
//        description.setAutoBuilding(true);
//        workspace.setDescription(description);
//        assertTrue(workspace.isAutoBuilding());
//    }

    /**
     * test if builds completed successfully and output directory contains class
     * files.
     */
    public void _testOutputFolderNotEmpty() throws Exception {
        waitForBuild();
        IPath outputLocation = fJavaProject.getOutputLocation();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IResource resource = root.findMember(outputLocation);
        assertNotNull("Project output location is null", resource);
        assertTrue("Project output location does not exist", resource.exists());
        assertTrue("Project output is not a folder", (resource.getType() == IResource.FOLDER));
        IFolder folder = (IFolder) resource;
        IResource[] children = folder.members();
        assertTrue("output folder is empty", children.length > 0);
    }

//    public void testForUnexpectedErrorsInProject() throws Exception {
//        waitForBuild();
//        IProject project = fJavaProject.getProject();
//        IMarker[] markers = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
//        int errors = 0;
//        for (int i = 0; i < markers.length; i++) {
//            IMarker marker = markers[i];
//            Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
//            IResource resource = marker.getResource();
//            
//            System.out.println(resource.getName());
//            
//            if (severity != null && severity.intValue() >= IMarker.SEVERITY_ERROR) {
//                errors++;
//            }
//        }
//        assertTrue("Unexpected compile errors in project. Expected 1, found " + markers.length, errors == 1);
//    }

    public void _testClassFilesGenerated() throws Exception {
        waitForBuild();
        IPath outputLocation = fJavaProject.getOutputLocation();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IFolder folder = (IFolder) root.findMember(outputLocation);
        IResource[] children = folder.members();
        int classFiles = 0;
        for (int i = 0; i < children.length; i++) {
            IResource child = children[i];
            if (child.getType() == IResource.FILE) {
                IFile file = (IFile) child;
                String fileExtension = file.getFileExtension();
                if (fileExtension.equals("class")) {
                    classFiles++;
                }
            }
        }
        assertTrue("No class files exist", (classFiles > 0));
    }
}
