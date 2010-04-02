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
 * $Id: NewRoleWizardPage.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.corext.refactoring.StubTypeContext;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.CompletionContextRequestor;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.ControlContentAssistHelper;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaTypeCompletionProcessor;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonStatusDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.objectteams.otdt.internal.ui.wizards.listeners.NewRoleWizardPageListener;
import org.eclipse.objectteams.otdt.internal.ui.wizards.listeners.NewTypeWizardPageListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * The class <code>NewRoleWizardPage</code> contains controls
 * for a 'New Role WizardPage'.
 *
 * @author brcan
 * @version $Id: NewRoleWizardPage.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class NewRoleWizardPage extends NewTypeWizardPage
{
	private static final int CREATE_INHERITED_INDEX = 0;

	private StringDialogField 	    _implicitSuperclassDialogField;
	private StringButtonDialogField _baseClassDialogField;

	
	public NewRoleWizardPage()
	{
		super("NewRoleWizardPage"); //$NON-NLS-1$

		setTitle(OTNewWizardMessages.NewRoleWizardPage_title);
		setDescription(OTNewWizardMessages.NewRoleWizardPage_description);

		_baseClassDialogField = createBaseClassDialogField(getListener());
		_implicitSuperclassDialogField = createImplicitSuperclassDialogField();
	}
	
	/**
	 * @return the baseclass name
	 */
	public String getBaseClassName()
	{
		return getBaseClassDialogField().getText();
	}

	public String getImplicitSuperclassName()
	{
	    return getImplicitSuperclassDialogField().getText();
	}

	/**
	 * @param name the new baseclass name
	 */		
	public void setBaseClassName(String name) 
	{
		getBaseClassDialogField().setText(name);
	}
		
	public void setImplicitSuperclassName(String name)
	{
	    getImplicitSuperclassDialogField().setText(name);
	}
	
	/**
	 * @return false
	 * a role instance always depends on a team instance
	 * thats why having a main method makes no sense
	 */
	public boolean isCreateMainSelected()
	{
		return false;
	}
	
	public boolean isCreateInheritedSelected()
	{
		return getMethodStubsButtons().getSelectionButton(CREATE_INHERITED_INDEX).getSelection();
	}
	
	/**
	 * @return false
	 * in the majority of cases roles don't have any constructor
	 */
	public boolean isCreateConstructorsSelected()
	{
		return false;
	}
	
	public StringButtonDialogField getBaseClassDialogField()
	{
		return _baseClassDialogField;
	}

	public StringDialogField getImplicitSuperclassDialogField()
	{
	    return _implicitSuperclassDialogField;
	}

	protected NewTypeWizardPageListener createPageListener()
	{
		return new NewRoleWizardPageListener(this);
	}

	protected StringButtonStatusDialogField createPackageDialogField(NewTypeWizardPageListener listener)
	{
	    return null;
	}
	
	protected void createPackageControls(Composite composite, int nColumns) 
	{
	    //do nothing
	}
	
	protected SelectionButtonDialogFieldGroup createBindingEditorButtons()
	{
	    //do nothing
	    return null;
	}
	
	protected StringButtonDialogField createSuperClassDialogField(NewTypeWizardPageListener adapter)
	{
		StringButtonDialogField result = super.createSuperClassDialogField(adapter);
		result.setLabelText(OTNewWizardMessages.NewRoleWizardPage_superclass_explicit_label);
		
		return result;
	}

	protected void createInheritanceControls(Composite composite, int nColumns)
	{
		createImplicitSuperclassControls(composite, nColumns);		
        super.createInheritanceControls(composite, nColumns);        
		createBaseClassControls(composite, nColumns);
	}

	protected SelectionButtonDialogFieldGroup createMethodStubsButtons()
	{
		String[] buttonName = new String[] { NewWizardMessages.NewClassWizardPage_methods_inherited };		
		SelectionButtonDialogFieldGroup result = new SelectionButtonDialogFieldGroup(SWT.CHECK, buttonName, 1);
		result.setLabelText(NewWizardMessages.NewClassWizardPage_methods_label); 

		return result;
	}

	/**
	 * Creates the controls for the baseclass name field. Expects a <code>GridLayout</code> 
	 * with at least 3 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createBaseClassControls(Composite composite, int nColumns)
	{
		_baseClassDialogField.doFillIntoGrid(composite, nColumns);
		
		Text text= _baseClassDialogField.getTextControl(null);
		LayoutUtil.setWidthHint(text, getMaxFieldWidth());
		
		JavaTypeCompletionProcessor baseClassCompletionProcessor= new JavaTypeCompletionProcessor(false, false);
		baseClassCompletionProcessor.setCompletionContextRequestor(new CompletionContextRequestor() {
			public StubTypeContext getStubTypeContext() {
				return getSuperClassStubTypeContext();
			}
		});

		ControlContentAssistHelper.createTextContentAssistant(text, baseClassCompletionProcessor);
		TextFieldNavigationHandler.install(text);
	}

	protected void initTypePage(IJavaElement elem) 
	{
		super.initTypePage(elem);
		getEnclosingTypeSelectionField().setSelection(true);
		getEnclosingTypeSelectionField().setEnabled(false);
		setImplicitSuperclassName(""); //$NON-NLS-1$
		setSuperTypeName("java.lang.Object");		 //$NON-NLS-1$
		getInlineSelectionDialogField().setSelection(true); // default to inline roles
		getInlineSelectionDialogField().setEnabled(true);
	}
	
	protected void initAccessModifierButtons()
	{
		// Note(SH): we are still creating all selection buttons to stay consistent
		// with other type creation wizards. (As long as the strange order
		// "public, default, private, protected" is pertained, we even MUST create
		// all four buttons, since we need the last one..).
	    getAccessModifierButtons().setSelection(PUBLIC_INDEX, false);	
	    getAccessModifierButtons().enableSelectionButton(DEFAULT_INDEX, false);
		getAccessModifierButtons().enableSelectionButton(PRIVATE_INDEX, false);
		getAccessModifierButtons().setSelection(PROTECTED_INDEX, true);
	}
	
	protected void initMethodStubButtons()
	{
		getMethodStubsButtons().setSelection(CREATE_INHERITED_INDEX, true);
		getMethodStubsButtons().setEnabled(true);
	}
		
	private StringButtonDialogField createBaseClassDialogField(NewTypeWizardPageListener listener)
	{
		StringButtonDialogField result = new StringButtonDialogField(listener);
		result.setDialogFieldListener(listener);
		result.setLabelText(OTNewWizardMessages.NewRoleWizardPage_baseclass_label); 
		result.setButtonLabel(OTNewWizardMessages.NewRoleWizardPage_baseclass_selection_button);
        
		return result;
	}    
    
	private StringDialogField createImplicitSuperclassDialogField()
	{
		StringDialogField result = new StringDialogField();
		result.setLabelText(OTNewWizardMessages.NewRoleWizardPage_superclass_implicit_label);
		result.setEnabled(false);
        
		return result;
	}
	
	/**
	 * Creates the controls for the type name field. Expects a <code>GridLayout</code> with at 
	 * least 2 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	private void createImplicitSuperclassControls(Composite composite, int nColumns) 
	{
		_implicitSuperclassDialogField.doFillIntoGrid(composite, nColumns - 1);
		DialogField.createEmptySpace(composite);		
		LayoutUtil.setWidthHint(_implicitSuperclassDialogField.getTextControl(null), getMaxFieldWidth());
	}

    protected void initBindingEditorButtons()
    {
    }

    public boolean isOpenBindingEditorSelected()
    {
        return false;
    }
    
    @Override
	protected String getEnclosingTypeLabel() {
		return OTNewWizardMessages.NewRoleWizardPage_enclosingtype_label;
	}
}