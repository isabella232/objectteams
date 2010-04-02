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
 * $Id: NewTeamCreationWizard.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.objectteams.otdt.internal.ui.bindingeditor.BindingEditorDialog;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.TeamCreator;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.TypeCreator;
import org.eclipse.objectteams.otdt.ui.ImageManager;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.swt.widgets.Display;


/**
 * A wizard for creating a new team.
 *
 * @author brcan
 * @version $Id: NewTeamCreationWizard.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class NewTeamCreationWizard extends NewTypeCreationWizard
{
	
    private NewTeamWizardPage _teamPage;


    public NewTeamCreationWizard()
    {
        super();
        
        setDefaultPageImageDescriptor(
            OTDTUIPlugin.getDefault().getImageRegistry().getDescriptor(
                ImageManager.NEW_TEAM));
        setDialogSettings(JavaPlugin.getDefault().getDialogSettings());
        setWindowTitle(OTNewWizardMessages.NewTeamCreationWizard_title);
    }

    /*
     * @see Wizard#createPages
     */
    public void addPages()
    {
        super.addPages();
        _teamPage = new NewTeamWizardPage();
        addPage(_teamPage);
        _teamPage.init(getSelection());
    }

	/**
	 * @return The page referenced by this wizard. This is an object of type 
	 *         org.eclipse.objectteams.otdt.internal.ui.wizards.NewTeamWizardPage
	 */
	public NewTypeWizardPage getPage()
	{
		return _teamPage;
	}
	
	
	protected void finishPage(IProgressMonitor monitor)
	throws InterruptedException, CoreException
	{	
	    super.finishPage(monitor);
	    
		if (getPage().isOpenBindingEditorSelected())
	    {
	        final IType createdTeam = getCreatedType();
	        if (createdTeam == null)
	        {
	        	// something went wrong, bail out
	        	return;
	        }
	              
	        Display.getDefault().asyncExec(new Runnable() {
				public void run() {
			        BindingEditorDialog dlg = new BindingEditorDialog(getShell(), createdTeam);
			        dlg.open();
				}
			});
    
	    }
	}
	
	/**
	 * @return A new object of type org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.TeamCreator
	 */
	protected TypeCreator createTypeCreator()
	{
		return new TeamCreator();	
	}
}
