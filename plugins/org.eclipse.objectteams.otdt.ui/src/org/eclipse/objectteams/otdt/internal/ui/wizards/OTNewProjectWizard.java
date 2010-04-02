/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTNewProjectWizard.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.objectteams.otdt.ui.ImageConstants;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * @author brcan
 * @version $Id: OTNewProjectWizard.java 23435 2010-02-04 00:14:38Z stephan $
 */

public class OTNewProjectWizard 
	extends 	BasicNewProjectResourceWizard
    implements 	IExecutableExtension
{
    private IConfigurationElement 		 _configElement;
    private JavaCapabilityConfigurationPage 	 _javaPage;
    private WizardNewProjectCreationPage _mainPage;
//	private IJavaProject _javaProject;
	private IWorkbench _workbench;
    
    public OTNewProjectWizard()
    {
        super();
        setDefaultPageImageDescriptor(OTDTUIPlugin.getDefault().getImageRegistry().getDescriptor(ImageConstants.NEW_OT_PROJECT));
        setDialogSettings(OTDTUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(OTDTUIPlugin.getResourceString("NewOTProjectCreationWizard.title")); //$NON-NLS-1$
    }
    
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		_workbench= workbench;
	}
    
    public void addPages()
    {
        _mainPage = new WizardNewProjectCreationPage("NewOTProjectCreationWizard"); //$NON-NLS-1$
        _mainPage.setTitle(OTDTUIPlugin.getResourceString("NewOTProjectCreationWizard.MainPage.title")); //$NON-NLS-1$
        _mainPage.setDescription(OTDTUIPlugin.getResourceString("NewOTProjectCreationWizard.MainPage.description")); //$NON-NLS-1$
        addPage(_mainPage);

        _javaPage = new JavaCapabilityConfigurationPage() {
		
			@Override
			public void setVisible(boolean visible)
			{
				updateJavaPage();
				super.setVisible(visible);
			}
		};

		// We can't add otre.jar to the classpath here, as that would overwrite the default classpath, i.e. the source folder (carp)
//        _javaPage.setDefaultClassPath(new IClasspathEntry[] { JavaCore.newContainerEntry(OTREContainer.OTRE_CONTAINER_PATH) }, true);
        addPage(_javaPage);
    }
    
	private void updateJavaPage() 
	{
		IJavaProject jproject= JavaCore.create(_mainPage.getProjectHandle());
		if (!jproject.equals(_javaPage.getJavaProject())) 
		{
			IClasspathEntry[] buildPath= {
				JavaCore.newSourceEntry(jproject.getPath().append("src")), //$NON-NLS-1$
				JavaRuntime.getDefaultJREContainerEntry()
			};
			IPath outputLocation= jproject.getPath().append("bin"); //$NON-NLS-1$
			_javaPage.init(jproject, outputLocation, buildPath, false);	
		}
	}
    
	private void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		try {		
			monitor.beginTask(OTNewWizardMessages.NewProjectWizard_task_title, 4); // 4 steps

			IProject project= _mainPage.getProjectHandle();
			IPath locationPath= _mainPage.getLocationPath();
		
			// create the project
			IProjectDescription desc= project.getWorkspace().newProjectDescription(project.getName());
			if (!_mainPage.useDefaults()) {
				desc.setLocation(locationPath);
			}
			desc.setNatureIds(OTDTPlugin.createProjectNatures(desc));
			desc.setBuildSpec(OTDTPlugin.createProjectBuildCommands(desc));

			project.create(desc, new SubProgressMonitor(monitor, 1));
			project.open(new SubProgressMonitor(monitor, 1));
			
			updateJavaPage();
			_javaPage.configureJavaProject(new SubProgressMonitor(monitor, 1));
	
			OTREContainer.initializeOTJProject(project);
			
			// change to the perspective specified in the plugin.xml		
			BasicNewProjectResourceWizard.updatePerspective(_configElement);
			BasicNewResourceWizard.selectAndReveal(project, _workbench.getActiveWorkbenchWindow());
			
		} finally {
			monitor.done();
		}
	}

	public boolean performFinish() {
		WorkspaceModifyOperation op= new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				finishPage(monitor);
			}
		};
		try {
			getContainer().run(false, true, op);
		} catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof CoreException)
			{
				CoreException core = (CoreException) ex.getTargetException();
				ErrorDialog.openError(getShell(), OTNewWizardMessages.NewProjectWizard_error_title, core.getMessage(), core.getStatus());
			}
			
			String title = OTNewWizardMessages.NewProjectWizard_wizard_creation_failure_title;
			String message = OTNewWizardMessages.NewProjectWizard_wizard_creation_failure_message;
			OTDTUIPlugin.getExceptionHandler().logException("Wizard title" + title + ": "+ message, ex); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		} catch  (InterruptedException e) {
			return false; // canceled
		}
		return true;
	}
			
	

	/**
	 * Stores the configuration element for the wizard.
	 * The config element will be used in performFinish()
	 * to set the result perspective.
	 */ 
	public void setInitializationData(
		IConfigurationElement cfig,
		String propertyName,
		Object data)
	{
	   _configElement = cfig;
	}
}
