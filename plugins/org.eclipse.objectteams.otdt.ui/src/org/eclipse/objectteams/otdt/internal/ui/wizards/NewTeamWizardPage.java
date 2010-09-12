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
 * $Id: NewTeamWizardPage.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.objectteams.otdt.internal.ui.wizards.listeners.NewTeamWizardPageListener;
import org.eclipse.objectteams.otdt.internal.ui.wizards.listeners.NewTypeWizardPageListener;
import org.eclipse.swt.SWT;

/**
 * The class <code>NewTeamWizardPage</code> contains controls
 * for a 'New Team WizardPage'.
 *
 * @author brcan
 * @version $Id: NewTeamWizardPage.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class NewTeamWizardPage extends NewTypeWizardPage
{
    private static final int CREATE_MAIN_INDEX          = 0; 
	private static final int CREATE_CONSTRUCTOR_INDEX   = 1; 		
	private static final int CREATE_INHERITED_INDEX     = 2;
	private static final int CREATE_BINDINGEDITOR_INDEX = 0;

	
    public NewTeamWizardPage()
    {
        super("NewTeamWizardPage"); //$NON-NLS-1$

        setTitle(OTNewWizardMessages.NewTeamWizardPage_title);
        setDescription(OTNewWizardMessages.NewTeamWizardPage_description);
    }

	protected StringButtonDialogField createSuperClassDialogField(NewTypeWizardPageListener adapter)
	{
		StringButtonDialogField result = super.createSuperClassDialogField(adapter);
		result.setLabelText(OTNewWizardMessages.NewTeamWizardPage_superclass_label);
		
		return result;
	}
	
	protected SelectionButtonDialogFieldGroup createMethodStubsButtons()
	{
		String[] buttonNames3 = new String[]
		{
			NewWizardMessages.NewClassWizardPage_methods_main, 
			OTNewWizardMessages.NewTeamWizardPage_methods_constructors, 
			NewWizardMessages.NewClassWizardPage_methods_inherited
		};		

        SelectionButtonDialogFieldGroup result = new SelectionButtonDialogFieldGroup(SWT.CHECK, buttonNames3, 1);
		result.setLabelText(NewWizardMessages.NewClassWizardPage_methods_label);
		
		return result;
	}
	
	protected SelectionButtonDialogFieldGroup createBindingEditorButtons()
	{
		String[] buttonNames1 = new String[]
		{
			OTNewWizardMessages.NewTeamWizardPage_BindingEditor_selection
		};		
		
        SelectionButtonDialogFieldGroup result = new SelectionButtonDialogFieldGroup(SWT.CHECK, buttonNames1, 1);
		result.setLabelText(OTNewWizardMessages.NewTeamWizardPage_BindingEditor_description); 
		
		return result;
	}
	
	protected void initTypePage(IJavaElement elem) 
	{
		super.initTypePage(elem);
		setSuperTypeName("org.objectteams.Team"); //$NON-NLS-1$
	}
	
	protected void initAccessModifierButtons()
	{
	    getAccessModifierButtons().setSelection(PUBLIC_INDEX, true);
	    getAccessModifierButtons().enableSelectionButton(DEFAULT_INDEX, true);
	    
		getAccessModifierButtons().enableSelectionButton(PRIVATE_INDEX, false);
		getAccessModifierButtons().enableSelectionButton(PROTECTED_INDEX, true);
	}	

	
	/**
	 * Sets the selection state of the method stub checkboxes.
	 */
	protected void initMethodStubButtons() 
	{		
		getMethodStubsButtons().setSelection(CREATE_MAIN_INDEX, false);
		getMethodStubsButtons().setSelection(CREATE_CONSTRUCTOR_INDEX, false);
		getMethodStubsButtons().setSelection(CREATE_INHERITED_INDEX, true);
		
		getMethodStubsButtons().setEnabled(true);
	}
	
	public boolean isCreateMainSelected()
	{
		return getMethodStubsButtons().getSelectionButton(CREATE_MAIN_INDEX).getSelection();
	}
	
	public boolean isCreateInheritedSelected()
	{
		return getMethodStubsButtons().getSelectionButton(CREATE_INHERITED_INDEX).getSelection();
	}
	
	public boolean isCreateConstructorsSelected()
	{
		return getMethodStubsButtons().getSelectionButton(CREATE_CONSTRUCTOR_INDEX).getSelection();
	}
	

	protected void initBindingEditorButtons() 
	{		
		getBindingEditorButtons().setSelection(CREATE_BINDINGEDITOR_INDEX, false);
		
		getBindingEditorButtons().setEnabled(false);
	}
	
	public boolean isOpenBindingEditorSelected()
	{
	    return getBindingEditorButtons().getSelectionButton(CREATE_BINDINGEDITOR_INDEX).getSelection();
	}
	
	protected NewTypeWizardPageListener createPageListener()
	{
		return new NewTeamWizardPageListener(this);
	}
	
	@Override
	public int getModifiers() {
		return super.getModifiers() + Flags.AccTeam;
	}
}