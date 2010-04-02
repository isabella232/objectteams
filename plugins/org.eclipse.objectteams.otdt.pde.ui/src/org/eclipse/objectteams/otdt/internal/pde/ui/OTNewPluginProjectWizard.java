/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
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
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.pde.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.pde.internal.ui.wizards.plugin.NewPluginProjectWizard;
import org.eclipse.pde.internal.ui.wizards.plugin.NewProjectCreationPage;

/**
 * Wizard to create a plug-in project with OT/Equinox enabled (classpath, plugin dependency).
 * @author resix
 */
@SuppressWarnings("restriction")
public class OTNewPluginProjectWizard extends NewPluginProjectWizard {

	static final String NEW_OTPDE_PROJECT = "wizard/newotpprj_wiz.png"; //$NON-NLS-1$

	public OTNewPluginProjectWizard() {
		super();
        setDefaultPageImageDescriptor(OTPDEUIPlugin.getDefault().getImageRegistry().getDescriptor(NEW_OTPDE_PROJECT));
		setWindowTitle(OTPDEUIMessages.NewOTPProjectWizard_title); 
	}
	
	@Override
	public void addPages() {
		super.addPages();
		IWizardPage mainPage= getPage("main"); //$NON-NLS-1$
		if (mainPage != null)
			mainPage.setTitle(OTPDEUIMessages.NewOTPProjectWizard_MainPage_title); 
	}

	public boolean performFinish()
	{ 
		within (new OTPluginDependenciesAdapter()) {
			if (!super.performFinish()) {
				return false;
			}
		}
	
		IWizardPage fMainPage = getPage("main");  //$NON-NLS-1$
		if (fMainPage instanceof NewProjectCreationPage)
		{
			IProject project = ((NewProjectCreationPage)fMainPage).getProjectHandle();
			
			try
			{
				OTPluginProject.makeOTPlugin(project);
				ClasspathComputerAdapter.sortClasspathEntries(project);
				return true;
			}
			catch (CoreException ex)
			{
				ErrorDialog.openError(getShell(), 
									  OTPDEUIMessages.OTNewPluginProjectWizard_ProjectCreationError, 
									  OTPDEUIMessages.OTNewPluginProjectWizard_CantAddOTSpecifics, 
									  ex.getStatus());
				OTPDEUIPlugin.getDefault().getLog().log(OTPDEUIPlugin.createErrorStatus("Project creation error", ex));
			}
		}
		else
			assert(false); // something changed in superclass, we must adapt to
    	  	
    	
		return false;
	}

}
