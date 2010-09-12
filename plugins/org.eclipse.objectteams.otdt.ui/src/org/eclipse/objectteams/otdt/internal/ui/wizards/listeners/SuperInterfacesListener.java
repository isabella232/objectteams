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
 * $Id: SuperInterfacesListener.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards.listeners;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.SuperInterfaceSelectionDialog;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.objectteams.otdt.internal.ui.wizards.NewTypeWizardPage;


/**
 * A listener for org.eclipse.objectteams.otdt.internal.ui.wizards.NewTypeWizardPage.
 * It listens to changes of the interface list dialog field of its observed page.
 * 
 * @author kaschja
 * @version $Id: SuperInterfacesListener.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class SuperInterfacesListener implements IListAdapter 
{
	
	private NewTypeWizardPage _page;
	
	
	public SuperInterfacesListener(NewTypeWizardPage page)
	{
		_page = page;
	}
	
	public void customButtonPressed(ListDialogField field, int index) 
	{
		chooseSuperInterfaces(field);
	}	
	public void selectionChanged(ListDialogField field) {}
    public void doubleClicked(ListDialogField field){}
	
	
	protected NewTypeWizardPage getPage()
	{
		return _page;
	}
		
	
	private void chooseSuperInterfaces(ListDialogField field) 
	{
		IPackageFragmentRoot root= getPage().getPackageFragmentRoot();
		if (root == null) 
		{
			return;
		}	

		SuperInterfaceSelectionDialog dialog;
		 
		// Note(km): need to change 3rd param from getPage().getSuperInterfacesDialogField()
		//           to get it compiled.
		dialog = new SuperInterfaceSelectionDialog(getPage().getShell(), 
		                                           getPage().getWizard().getContainer(), 
		                                           getPage(), 
		                                           root.getJavaProject());
		dialog.setTitle( NewWizardMessages.NewTypeWizardPage_InterfacesDialog_class_title);
		dialog.setMessage(NewWizardMessages.NewTypeWizardPage_InterfacesDialog_message);
		dialog.open();
		return;
	}	
}
