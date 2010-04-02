/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: NewTeamWizardPageListener.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards.listeners;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.ui.wizards.NewTeamWizardPage;
import org.eclipse.objectteams.otdt.internal.ui.wizards.NewTypeWizardPage;
import org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;


/**
 * The main listener class for org.eclipse.objectteams.otdt.internal.ui.wizards.NewTeamWizardPage.
 * It listens to changes of the entry fields and clicks on the browse buttons
 * of the observed NewTeamWizardPage
 * 
 * 
 * @author kaschja
 * @version $Id: NewTeamWizardPageListener.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class NewTeamWizardPageListener extends NewTypeWizardPageListener
{

	public NewTeamWizardPageListener(NewTeamWizardPage observedPage)
	{
		super(observedPage);
	}
	

	protected IType chooseSuperType()
	{
		IPackageFragmentRoot root= getObservedPage().getPackageFragmentRoot();
		if (root == null) 
		{
			return null;
		}

		return chooseTeam(root.getJavaProject(), 
						  getObservedPage().getShell(),
						  getObservedPage().getWizard().getContainer(),
						  OTNewWizardMessages.NewTeamWizardPage_ChooseSuperTypeDialog_title,
		                  OTNewWizardMessages.NewTeamWizardPage_ChooseSuperTypeDialog_description,
						  getObservedPage().getSuperTypeName());
	}

	// ------ validation --------
	public IStatus[] getRelevantStates(boolean ignoreFirstField)
	{
		if (ignoreFirstField)
			return new IStatus[] {
				getObservedPage().getContainerStatus(),
				
				_packageStatus,
                _enclosingTeamStatus,
				    
                // not this one: _typeNameStatus,
				_modifierStatus,
				_superTypeStatus,
				_superInterfacesStatus 
				
			};
		else
		// status of all used components
			return new IStatus[]
		    {
				getObservedPage().getContainerStatus(),
				
				_packageStatus,
                _enclosingTeamStatus,
				    
				_typeNameStatus,
				_modifierStatus,
				_superTypeStatus,
				_superInterfacesStatus 
			};	
	}


	protected IStatus validateEnclosingType()
	{
		IStatus status = super.validateEnclosingType();
		if ( status.getSeverity() == IStatus.ERROR )
		{
			//deselect and disable Inlined-Checkbox
			getObservedPage().getInlineSelectionDialogField().setSelection(false);
			getObservedPage().getInlineSelectionDialogField().setEnabled(false);
		}
		handleAccessModifierButtons();
		getObservedPage().updateEnableState();
		return status;
	}
	
	@Override
	void validateSuperClass(StatusInfo status) {
		super.validateSuperClass(status);
		
	    // deselect Bindingeditor when supertype == o.o.Team 
		IType superType      = getObservedPage().getSuperType();
		if (superType == null)
			return;
		IOTType superTeam = OTModelManager.getOTElement(superType);
        if (! superType.getFullyQualifiedName().equals(String.valueOf(IOTConstants.STR_ORG_OBJECTTEAMS_TEAM)) && superTeam != null)
        {
            SelectionButtonDialogFieldGroup bindingEditorGroup = ((NewTeamWizardPage)getObservedPage()).getBindingEditorButtons();
            bindingEditorGroup.setEnabled(true);
        }
	}
	
	protected void handleEnclosingTypeDialogFieldIsEmpty(StatusInfo status)
	{
	    getObservedPage().getPackageDialogField().setEnabled(true);
		
	    //disable Inlined-Checkbox
		getObservedPage().getInlineSelectionDialogField().setSelection(false);
		getObservedPage().getInlineSelectionDialogField().setEnabled(false);
		
		super.handleEnclosingTypeDialogFieldIsEmpty(status);
	}	

	protected void handleAccessModifierButtons()
	{
	    SelectionButtonDialogFieldGroup modifierButtons = getObservedPage().getAccessModifierButtons();
	    
	    if (getObservedPage().getEnclosingTypeName().equals("")) //$NON-NLS-1$
	    {
		    int modifiers = getObservedPage().getModifiers();
		    if (Flags.isPrivate(modifiers) || Flags.isProtected(modifiers))
		    {
		       modifierButtons.setSelection(NewTypeWizardPage.PRIVATE_INDEX, false);
		       modifierButtons.setSelection(NewTypeWizardPage.PROTECTED_INDEX, false);
		       modifierButtons.setSelection(NewTypeWizardPage.PUBLIC_INDEX, true);
		    }
		    modifierButtons.enableSelectionButton(NewTypeWizardPage.PRIVATE_INDEX, false);
		    modifierButtons.enableSelectionButton(NewTypeWizardPage.PROTECTED_INDEX, false);	        
	    }
	    else
	    {
	        modifierButtons.enableSelectionButton(NewTypeWizardPage.PRIVATE_INDEX, true);
		    modifierButtons.enableSelectionButton(NewTypeWizardPage.PROTECTED_INDEX, true);	        
	    }
	}


	/**
	 * Gets called when the package field has changed. The method 
	 * validates the package name and returns the status of the validation.
	 * The validation also updates the package fragment model.
	 * 
	 * @return the status of the validation
	 */
	protected IStatus validatePackage() {
	    StatusInfo status = new StatusInfo();

	    updatePackageStatusLabel();
        getObservedPage().getPackageDialogField().enableButton(getObservedPage().getPackageFragmentRoot() != null);

        String packName = getObservedPage().getPackageName();
        if (packName.trim().length() > 0)
        {
            IStatus val = NewTypeWizardPage.validatePackageName(packName, getObservedPage().getJavaProject());
            if (val.getSeverity() == IStatus.ERROR)
            {
                status.setError(Messages.format(NewWizardMessages.NewTypeWizardPage_error_InvalidPackageName, val.getMessage())); 
                return status;
            }
            else if (val.getSeverity() == IStatus.WARNING)
            {
                status.setWarning(Messages.format(NewWizardMessages.NewTypeWizardPage_warning_DiscouragedPackageName, val.getMessage())); 
                // continue
            }
        }

        IPackageFragmentRoot root = getObservedPage().getPackageFragmentRoot();
        if (root != null)
        {
            if (root.getJavaProject().exists() && packName.trim().length() > 0)
            {
                try
                {
                    IPath rootPath   = root.getPath();
                    IPath outputPath = root.getJavaProject().getOutputLocation();
                    
                    if (rootPath.isPrefixOf(outputPath)
                        && !rootPath.equals(outputPath))
                    {
                        // if the bin folder is inside of our root, dont allow to name a package
                        // like the bin folder
                        IPath packagePath = rootPath.append(packName.replace('.', '/'));
                        
                        if (outputPath.isPrefixOf(packagePath))
                        {
                            status.setError(NewWizardMessages.NewTypeWizardPage_error_ClashOutputLocation);
                            return status;
                        }
                    }
                }
                catch (JavaModelException ex)
                {
                    OTDTUIPlugin.getExceptionHandler().logCoreException("", ex); //$NON-NLS-1$
                    // let pass			
                }
            }       
			getObservedPage().setPackageFragment(root.getPackageFragment(packName), true);
        }
        else
        {
            status.setError(""); //$NON-NLS-1$
        }
    	return status;
	}


	protected void updatePackageStatusLabel() {
		String packName = getObservedPage().getPackageName();
		
		if (packName.length() == 0)
		{
			getObservedPage().getPackageDialogField().setStatus(NewWizardMessages.NewTypeWizardPage_default);
		}
		else
		{
			getObservedPage().getPackageDialogField().setStatus(""); //$NON-NLS-1$
		}
	}

}
