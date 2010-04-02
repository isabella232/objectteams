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
 * $Id: NewRoleWizardPageListener.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards.listeners;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jface.window.Window;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.core.OTJavaElement;
import org.eclipse.objectteams.otdt.internal.ui.wizards.NewRoleWizardPage;
import org.eclipse.objectteams.otdt.internal.ui.wizards.NewTypeWizardPage;
import org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * The main listener class for org.eclipse.objectteams.otdt.internal.ui.wizards.NewRoleWizardPage.
 * It listens to changes of the entry fields and clicks on the browse buttons
 * of the observed NewRoleWizardPage.
 * 
 * @author kaschja
 * @version $Id: NewRoleWizardPageListener.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class NewRoleWizardPageListener extends NewTypeWizardPageListener
{
    protected static final int BASE = 20;

	private IStatus _baseStatus = new StatusInfo();
	// cache this info (hierarchy operation is expensive)
	private IType[] fSuperTeams = null;

    public NewRoleWizardPageListener(NewTypeWizardPage observedPage)
    {
        super(observedPage);
    }

    protected int getChangedElement(DialogField field)
    {
		if (getObservedPage() instanceof NewRoleWizardPage)
		{
			NewRoleWizardPage page = (NewRoleWizardPage) getObservedPage();
	
			if (field == page.getBaseClassDialogField())
			{
			    return BASE;
			}
		}
		
		return super.getChangedElement(field);
    }
    
	protected void performReviews(int change)
	{
		super.performReviews(change);
		
		if ((change == CONTAINER)
			|| (change == PACKAGE)
			|| (change == ENCLOSINGTYPE)
			|| (change == BASE)
			|| (change == NAME) )
		{
		    _baseStatus = validateBaseClass();
			handleImplicitSuperclassDialogField();
		}
	}    
    
	protected void handleChangeControlPressed(DialogField field)
	{
		if (!(getObservedPage() instanceof NewRoleWizardPage) )
		{
			return;
		}        
		NewRoleWizardPage page = (NewRoleWizardPage) getObservedPage();

		if (field == page.getBaseClassDialogField()) 
		{
			IType type = chooseBaseType();
			if (type != null) 
			{
				String str = type.getFullyQualifiedName('.');
				page.setBaseClassName(str);			
			}    
		}		
		else
		{
			super.handleChangeControlPressed(field);
		}
	}	

	protected IStatus validateTypeName()
	{
		// FIXME(SH): admit parameterized name!
		IStatus superStatus = super.validateTypeName();
		if (superStatus.getSeverity() == IStatus.ERROR) 
		{
			return superStatus;
		} 
		
		NewRoleWizardPage page = (NewRoleWizardPage) getObservedPage();
        
        String enclosingTypeName = page.getEnclosingTypeName();
        // strip package name off of enclosing type
	    enclosingTypeName = enclosingTypeName.substring(enclosingTypeName.lastIndexOf('.') + 1);            
        
        String simpleRoleName = page.getTypeName();
        if (simpleRoleName.equals(enclosingTypeName))
	    {
            StatusInfo status = new StatusInfo();
            status.setError(OTNewWizardMessages.NewRole_role_hides_team);
            return status;
	    }
	    
        return superStatus;
	}
	
	protected IType chooseEnclosingType() 
	{
		IPackageFragmentRoot root= getObservedPage().getPackageFragmentRoot();
		if (root == null) 
			return null;
		
		return chooseTeam( root, 
						   getObservedPage().getShell(),
						   getObservedPage().getWizard().getContainer(),
						   org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages.NewRoleWizardPage_ChooseEnclosingTypeDialog_title,
						   org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages.NewRoleWizardPage_ChooseEnclosingTypeDialog_description,
						   Signature.getSimpleName(getObservedPage().getEnclosingTypeName()));
	}		

	protected IType chooseSuperType()
	{
		IPackageFragmentRoot root = getObservedPage().getPackageFragmentRoot();
		if (root == null) 
		{
			return null;
		}
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { root.getJavaProject() });
	
		FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(
					getObservedPage().getShell(), 
					false,
					getObservedPage().getWizard().getContainer(),
					scope,
					IJavaSearchConstants.CLASS);
		dialog.setTitle(OTNewWizardMessages.NewRoleWizardPage_SuperclassDialog_title);
		dialog.setMessage(OTNewWizardMessages.NewRoleWizardPage_SuperclassDialog_message);
		dialog.setInitialPattern(getObservedPage().getSuperTypeName());
	
		if (dialog.open() == Window.OK) 
		{	
			return (IType) dialog.getFirstResult();
		}
		return null;
	}
	
	private IType chooseBaseType()
	{
		IPackageFragmentRoot root = getObservedPage().getPackageFragmentRoot();
		if (root == null) 
		{
			return null;
		}	
			
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { root.getJavaProject() });
	
		FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(getObservedPage().getShell(), 
															 false,
		                                                     getObservedPage().getWizard().getContainer(),
		                                                     scope,
		                                                     IJavaSearchConstants.TYPE);
		dialog.setTitle(OTNewWizardMessages.NewRoleWizardPage_BaseclassDialog_title); 
		dialog.setMessage(OTNewWizardMessages.NewRoleWizardPage_BaseclassDialog_message);
		dialog.setInitialPattern( ((NewRoleWizardPage)getObservedPage()).getBaseClassName());
		dialog.setValidator(new ISelectionStatusValidator()
		{
			public IStatus validate(Object[] selection) {
				if (   selection != null && selection.length > 0 
					&& selection[0] instanceof IType) 
						return validateBaseClassName(
								getObservedPage().getEnclosingType(), 
								((IType)selection[0]).getElementName());
				return StatusInfo.OK_STATUS;
			};
		});
	
		if (dialog.open() == FilteredTypesSelectionDialog.OK) 
		{
			return (IType) dialog.getFirstResult();
		}
		return null;
	}	
	
	protected void handleEnclosingTypeDialogFieldIsEmpty(StatusInfo status)
	{
	    status.setError(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewTypeWizardPage_error_EnclosingTypeEnterName);
	}	    
	
	@Override
	protected IStatus validateEnclosingType() {
		IStatus status = super.validateEnclosingType();
		if (status.isOK()) 
			cacheSuperTeams();
		return status;
	}

	private void cacheSuperTeams() {
		try {
		    IType enclosingType = this.getObservedPage().getEnclosingType();
			ITypeHierarchy hierarchy = enclosingType.newSupertypeHierarchy(null);
			fSuperTeams = hierarchy.getAllSuperclasses(enclosingType);
		}
		catch (JavaModelException ex) {
		    OTDTUIPlugin.getExceptionHandler().logException("Problems creating supertype hierarchy", ex); //$NON-NLS-1$
		}
	}

	// ------ validation --------
	public IStatus[] getRelevantStates(boolean ignoreFirstField)
	{
		if (ignoreFirstField) 
			return new IStatus[] 
		    {
				getObservedPage().getContainerStatus(),
                _packageStatus,				
				_enclosingTeamStatus,
				// not this one: _typeNameStatus,
				_baseStatus,				
				_modifierStatus,
				_superTypeStatus,
				_superInterfacesStatus,
			};
		else
		// status of all used components
			return new IStatus[] 
		    {
				getObservedPage().getContainerStatus(),
                _packageStatus,				
				_enclosingTeamStatus,
				_typeNameStatus,
				_baseStatus,				
				_modifierStatus,
				_superTypeStatus,
				_superInterfacesStatus,
			};
	}

    private void handleImplicitSuperclassDialogField()
    {
		if (!(getObservedPage() instanceof NewRoleWizardPage))
		    return;

		NewRoleWizardPage page = (NewRoleWizardPage) getObservedPage();					
		page.setImplicitSuperclassName(""); //$NON-NLS-1$
		
		if (hasTypeNameError()) // don't proceed if there is an error already
		    return;
		
		IType enclosingTeam = page.getEnclosingType();
		
		if (enclosingTeam != null)
		{
			if (fSuperTeams == null) 
				cacheSuperTeams();
			
		    for (int idx = 0; idx < fSuperTeams.length; idx++)
            {		        
		        IType memberType = fSuperTeams[idx].getType(page.getTypeName());
		        IOTType otType = OTModelManager.getOTElement(memberType);
		        
	            if (otType != null && otType instanceof IRoleType)
	            {		                					    
                    String fullQualName = memberType.getFullyQualifiedName('.');
		            page.setImplicitSuperclassName(fullQualName);
		            return;
	            }
            }
		}
	}

    private boolean hasTypeNameError()
    {
		IStatus[] stati = new IStatus[] { 
		        getObservedPage().getContainerStatus(),
		        _packageStatus,
		        _enclosingTeamStatus,
		        _typeNameStatus
		};

		return hasErrorStatus(stati);
    }

    private IStatus validateBaseClass()
    {
		NewRoleWizardPage page = (NewRoleWizardPage) getObservedPage();
		String baseclassName = page.getBaseClassName();

        if (baseclassName.length() == 0) 
            return StatusInfo.OK_STATUS; // a Role without a playedBy relation is just fine

        // ERRORS:
        IStatus validJava = JavaConventions.validateJavaTypeName(baseclassName);
        if (validJava.getSeverity() == IStatus.ERROR) 
            return new StatusInfo(IStatus.ERROR,
            		Messages.format(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewTypeWizardPage_error_InvalidTypeName, 
       				validJava.getMessage())); 
        
        // check shadowing (ERROR):
        IType enclosingType = getObservedPage().getEnclosingType();
        IStatus status = validateBaseClassName(enclosingType, baseclassName);
        if (!status.isOK())
        	return status;
        
        // WARNINGS:
        if (validJava.getSeverity() == IStatus.WARNING) 
        	return new StatusInfo(IStatus.WARNING,
        			Messages.format(org.eclipse.jdt.internal.ui.wizards.NewWizardMessages.NewTypeWizardPage_warning_TypeNameDiscouraged, 
        					validJava.getMessage()));

        return StatusInfo.OK_STATUS;
    }

	IStatus validateBaseClassName(IType enclosingType, String name) {
		while (enclosingType != null) {
			if (name.equals(enclosingType.getElementName())) {
				return new StatusInfo(StatusInfo.ERROR,
					Messages.format(
						OTNewWizardMessages.NewRole_base_class_equals_enclosing, 
						name));
			}
			try {
				for (IJavaElement member : enclosingType.getChildren()) {
					if (   member.getElementType() == OTJavaElement.ROLE
						&& member.getElementName().equals(name))
						return new StatusInfo(StatusInfo.ERROR,
							Messages.format(
								OTNewWizardMessages.NewRole_base_class_equals_member, 
								name));
				}
			} catch (JavaModelException e) { /* nop */ }					
			enclosingType = enclosingType.getDeclaringType();
		}
		return StatusInfo.OK_STATUS;
	} 

}
