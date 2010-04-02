/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Technical University Berlin.
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
 * Marco Mosconi - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.pde.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.objectteams.otdt.core.ext.OTREContainer;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModelBase;
import org.eclipse.pde.ui.templates.PluginReference;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action to add (TBD: or remove) OT support on projects.
 * To be invoked via the "Configure" context-menu in Package Explorer.
 * @author mosconi
 * @since 1.3.2
 */
@SuppressWarnings("restriction")
public class ToggleOTSupportAction implements IObjectActionDelegate {

	private Shell shell;
	private ISelection selection;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (!(selection instanceof IStructuredSelection))
			return;
		for (Object element : ((IStructuredSelection) selection).toArray()) {
			IProject project = null;
			if (element instanceof IProject) {
				project = (IProject) element;
			}
			else if (element instanceof IAdaptable) {
				project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
			}
			if (project != null) {
				toggleOTSupport(project);
			}
		}
	}

	private void toggleOTSupport(IProject project)  {
		try {
			IProjectDescription description = project.getDescription();
			if (description.hasNature(JavaCore.OTJ_NATURE_ID)) {
				removeOTSupport(project);
			}
			else {
				addOTSupport(project);
			}
		}
		catch (CoreException e) {
			ErrorDialog.openError(shell, "Project Configuration Error", "Error Configuring Project " + project.getName() + ".", e.getStatus());
			OTPDEUIPlugin.getDefault().getLog().log(OTPDEUIPlugin.createErrorStatus("Project configuration error", e));
		}
	}

	/**
	 * Adds OT/J nature, builder, and dependencies to the given project
	 */
	private void addOTSupport(IProject project) throws CoreException {
		OTPluginProject.addOTNatureAndBuilder(project);
		if (PDE.hasPluginNature(project)) {
			// add plug-in dependency to org.eclipse.objectteams.otequinox:
			WorkspacePluginModelBase fModel = new WorkspaceBundlePluginModel(
					project.getFile(ICoreConstants.BUNDLE_FILENAME_DESCRIPTOR),
					project.getFile(ICoreConstants.PLUGIN_FILENAME_DESCRIPTOR));
			IPluginBase pluginBase = fModel.getPluginBase();
			IPluginReference ref = new PluginReference(
					"org.eclipse.objectteams.otequinox", null, 0);
			IPluginImport iimport = fModel.getPluginFactory().createImport();
			iimport.setId(ref.getId());
			iimport.setVersion(ref.getVersion());
			iimport.setMatch(ref.getMatch());
			pluginBase.add(iimport);
			fModel.save();
		}
		OTREContainer.initializeOTJProject(project);
		if (PDE.hasPluginNature(project)) {
			ClasspathComputerAdapter.sortClasspathEntries(project);
		}
	}

	/**
	 * Removes OT/J nature, builder, and dependencies from the given project
	 */
	private void removeOTSupport(IProject project) throws CoreException {
		//TODO: yet to be implemented (also needs additional hooking in plugin.xml)
	}

}
